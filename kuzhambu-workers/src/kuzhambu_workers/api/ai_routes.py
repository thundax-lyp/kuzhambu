from collections.abc import AsyncIterator, Callable
from datetime import datetime, timezone
from typing import NamedTuple

from fastapi import APIRouter, Request
from fastapi.responses import JSONResponse, StreamingResponse
from pydantic import ValidationError

from kuzhambu_workers.ai.graph_registry import GraphRegistry
from kuzhambu_workers.core.config import load_settings
from kuzhambu_workers.core.errors import WorkerError, protocol_failure, to_error_payload
from kuzhambu_workers.core.security import verify_internal_request
from kuzhambu_workers.schemas.ai import (
    AiInvokeRequest,
    AiInvokeResponse,
    AiResult,
    ArtifactReference,
    FailureStage,
    ResultFormat,
)
from kuzhambu_workers.schemas.common import UsageSummary, WorkerErrorPayload, WorkerStatus
from kuzhambu_workers.schemas.stream import StreamEventType
from kuzhambu_workers.streaming.events import started_event, stream_event
from kuzhambu_workers.streaming.sse import encode_sse

router = APIRouter(prefix="/internal/ai", tags=["AI Debug"])
_REGISTRY = GraphRegistry.build_default()
AiRequestValidator = Callable[[AiInvokeRequest], WorkerErrorPayload | None]
DEBUG_INTERFACE_NOTICE = (
    "通用 AI 接口仅用于调试、平台联调和协议验证；真实业务必须使用基于 usecase "
    "定义的稳定接口，不得把该通用接口作为业务域长期集成入口。"
)


class ParsedAiRequest(NamedTuple):
    body: bytes
    request: AiInvokeRequest | None
    error_response: JSONResponse | None


@router.post(
    "/invoke",
    response_model=None,
    summary="Debug AI invoke",
    description=DEBUG_INTERFACE_NOTICE,
)
async def invoke(request: Request) -> JSONResponse:
    return await invoke_ai_request(request)


@router.post(
    "/stream",
    response_model=None,
    summary="Debug AI stream",
    description=DEBUG_INTERFACE_NOTICE,
)
async def stream(request: Request) -> StreamingResponse | JSONResponse:
    return await stream_ai_request(request)


async def invoke_ai_request(
    request: Request,
    *,
    registry: GraphRegistry = _REGISTRY,
    validate: AiRequestValidator | None = None,
) -> JSONResponse:
    parsed = await parse_verified_ai_request(request)
    if parsed.error_response is not None:
        return parsed.error_response

    ai_request = _require_parsed_request(parsed)
    validation_failure = _validate_ai_request(ai_request, validate)
    if validation_failure is not None:
        return validation_failure

    return invoke_ai_graph(ai_request, registry=registry)


async def stream_ai_request(
    request: Request,
    *,
    registry: GraphRegistry = _REGISTRY,
    validate: AiRequestValidator | None = None,
) -> StreamingResponse | JSONResponse:
    parsed = await parse_verified_ai_request(request)
    if parsed.error_response is not None:
        return parsed.error_response

    ai_request = _require_parsed_request(parsed)
    validation_failure = _validate_ai_request(ai_request, validate)
    if validation_failure is not None:
        return validation_failure

    return stream_ai_graph(ai_request, registry=registry)


async def parse_verified_ai_request(request: Request) -> ParsedAiRequest:
    body = await request.body()
    parsed = _parse_request(body)
    if isinstance(parsed, JSONResponse):
        return ParsedAiRequest(body=body, request=None, error_response=parsed)

    auth_failure = _verify(request, body, parsed)
    if auth_failure is not None:
        return ParsedAiRequest(body=body, request=None, error_response=auth_failure)

    return ParsedAiRequest(body=body, request=parsed, error_response=None)


def invoke_ai_graph(
    request: AiInvokeRequest,
    *,
    registry: GraphRegistry = _REGISTRY,
) -> JSONResponse:
    try:
        result = AiResult.model_validate(registry.invoke(request))
        artifact_reference = _artifact_reference_from_result(result)
        response = AiInvokeResponse(
            requestId=request.requestId,
            traceId=request.traceId,
            status=WorkerStatus.SUCCEEDED,
            capability=request.capability,
            result=None if artifact_reference is not None else result,
            usage=UsageSummary(),
            fallbackUsed=False,
            artifactReference=artifact_reference,
        )
        return JSONResponse(response.model_dump(mode="json"))
    except Exception as exc:
        return _failed_response(
            request,
            WorkerErrorPayload.model_validate(to_error_payload(exc)),
            failure_stage=FailureStage.WORKER_RESULT,
        )


def stream_ai_graph(
    request: AiInvokeRequest,
    *,
    registry: GraphRegistry = _REGISTRY,
) -> StreamingResponse:
    async def events() -> AsyncIterator[str]:
        timestamp = _now()
        yield encode_sse(started_event(request.requestId, request.traceId, timestamp))
        try:
            result = AiResult.model_validate(registry.invoke(request))
            artifact_reference = _artifact_reference_from_result(result)
            yield encode_sse(
                stream_event(
                    StreamEventType.COMPLETED,
                    request_id=request.requestId,
                    trace_id=request.traceId,
                    stage="completed",
                    timestamp=_now(),
                    result=None
                    if artifact_reference is not None
                    else result.model_dump(mode="json"),
                    usage=UsageSummary().model_dump(mode="json"),
                    extra={
                        "status": WorkerStatus.SUCCEEDED.value,
                        "failureStage": None,
                        "fallbackUsed": False,
                        "artifactReference": None
                        if artifact_reference is None
                        else artifact_reference.model_dump(mode="json"),
                    },
                )
            )
        except Exception as exc:
            error = WorkerErrorPayload.model_validate(to_error_payload(exc))
            yield encode_sse(
                stream_event(
                    StreamEventType.ERROR,
                    request_id=request.requestId,
                    trace_id=request.traceId,
                    stage="error",
                    timestamp=_now(),
                    error=error.model_dump(mode="json"),
                    extra={
                        "status": WorkerStatus.FAILED.value,
                        "failureStage": FailureStage.WORKER_RESULT.value,
                        "fallbackUsed": False,
                        "artifactReference": None,
                    },
                )
            )

    return StreamingResponse(events(), media_type="text/event-stream")


def _parse_request(body: bytes) -> AiInvokeRequest | JSONResponse:
    try:
        return AiInvokeRequest.model_validate_json(body)
    except ValidationError as exc:
        error = protocol_failure(
            "BAD_REQUEST",
            "AI worker 请求体不合法。",
            detail={"errors": exc.errors(include_input=False)},
        ).to_payload()
        return _error_json(WorkerErrorPayload.model_validate(error), 400)


def _verify(request: Request, body: bytes, parsed: AiInvokeRequest) -> JSONResponse | None:
    try:
        verify_internal_request(
            method=request.method,
            path=request.url.path,
            headers=request.headers,
            body=body,
            settings=load_settings(),
            request_id=parsed.requestId,
            trace_id=parsed.traceId,
        )
    except WorkerError as exc:
        return _error_json(
            WorkerErrorPayload.model_validate(exc.to_payload()),
            _status_code(exc.code),
        )
    return None


def _require_parsed_request(parsed: ParsedAiRequest) -> AiInvokeRequest:
    if parsed.request is None:
        raise RuntimeError("AI request is unavailable after successful parse and verify.")
    return parsed.request


def _validate_ai_request(
    request: AiInvokeRequest,
    validate: AiRequestValidator | None,
) -> JSONResponse | None:
    if validate is None:
        return None
    error = validate(request)
    if error is None:
        return None
    return _failed_response(request, error, status_code=_status_code(error.code))


def _failed_response(
    request: AiInvokeRequest,
    error: WorkerErrorPayload,
    *,
    status_code: int = 200,
    failure_stage: FailureStage | None = None,
) -> JSONResponse:
    response = AiInvokeResponse(
        requestId=request.requestId,
        traceId=request.traceId,
        status=WorkerStatus.FAILED,
        capability=request.capability,
        result=None,
        usage=UsageSummary(),
        failureStage=failure_stage,
        fallbackUsed=False,
        artifactReference=None,
        error=error,
    )
    return JSONResponse(response.model_dump(mode="json"), status_code=status_code)


def _error_json(error: WorkerErrorPayload, status_code: int) -> JSONResponse:
    return JSONResponse(
        {
            "status": WorkerStatus.FAILED.value,
            "failureStage": FailureStage.REQUEST_VALIDATE.value,
            "fallbackUsed": False,
            "artifactReference": None,
            "error": error.model_dump(mode="json"),
        },
        status_code=status_code,
    )


def _status_code(code: str) -> int:
    if code in {"SERVICE_NOT_ALLOWED", "PATH_FORBIDDEN"}:
        return 403
    if code == "BAD_REQUEST":
        return 400
    return 401


def _now() -> str:
    return datetime.now(timezone.utc).isoformat(timespec="milliseconds").replace("+00:00", "Z")


def _artifact_reference_from_result(result: AiResult) -> ArtifactReference | None:
    if result.format != ResultFormat.ARTIFACT:
        return None
    payload = result.payload
    if not isinstance(payload, dict):
        return None
    if not {
        "artifactId",
        "downloadPath",
        "contentType",
        "filename",
        "sizeBytes",
        "sha256",
        "expiresAt",
    }.issubset(payload):
        return None
    return ArtifactReference.model_validate(payload)
