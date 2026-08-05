from collections.abc import AsyncIterator, Callable
from datetime import datetime, timezone
from typing import Any, NamedTuple

from fastapi import APIRouter, Request
from fastapi.responses import JSONResponse, StreamingResponse
from pydantic import ValidationError

from kuzhambu_workers.ai.graph_registry import GraphRegistry
from kuzhambu_workers.ai.openai_compatible import iter_chat_completion_chunks
from kuzhambu_workers.ai.structured_output import requires_structured_output
from kuzhambu_workers.core.config import load_settings
from kuzhambu_workers.core.errors import (
    WorkerError,
    WorkerErrorType,
    protocol_failure,
)
from kuzhambu_workers.core.security import verify_internal_request
from kuzhambu_workers.render.artifact_store import RequestArtifactStore
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
from kuzhambu_workers.streaming.events import (
    delta_event,
    final_state_extra,
    started_event,
    stream_event,
    usage_event,
)
from kuzhambu_workers.streaming.sse import encode_sse

router = APIRouter(prefix="/internal/ai", tags=["AI"])
_REGISTRY = GraphRegistry.build_default()
AiRequestValidator = Callable[[AiInvokeRequest], WorkerErrorPayload | None]
AI_INTERFACE_NOTICE = (
    "统一 AI 执行接口。Java AI 域必须在调用前完成业务类型识别、业务配置选择、"
    "提示词渲染、模型配置组装、权限、审计和任务台账处理；workers 只执行本次已组装的无状态 AI 请求。"
)


class ParsedAiRequest(NamedTuple):
    body: bytes
    request: AiInvokeRequest | None
    error_response: JSONResponse | None


@router.post(
    "/invoke",
    response_model=None,
    summary="AI invoke",
    description=AI_INTERFACE_NOTICE,
)
async def invoke(request: Request) -> JSONResponse:
    return await invoke_ai_request(request)


@router.post(
    "/stream",
    response_model=None,
    summary="AI stream",
    description=AI_INTERFACE_NOTICE,
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
        raw_result = registry.invoke(request)
        result = _coerce_text_payload(request, raw_result)
        usage = _usage_from_graph_result(raw_result)
        artifact_reference = _artifact_reference_from_result(request, result)
        response = AiInvokeResponse(
            requestId=request.requestId,
            traceId=request.traceId,
            status=WorkerStatus.SUCCEEDED,
            capability=request.capability,
            result=None if artifact_reference is not None else result,
            usage=usage,
            fallbackUsed=False,
            artifactReference=artifact_reference,
            errorType=None,
            errorMessage=None,
        )
        return JSONResponse(response.model_dump(mode="json"))
    except Exception as exc:
        return _failed_response(
            request,
            WorkerErrorPayload.model_validate(_worker_error_payload(exc)),
            failure_stage=_failure_stage(exc),
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
            if request.capability.value == "image_gen":
                raw_result = registry.invoke(request)
                result = _coerce_text_payload(request, raw_result)
                usage = _usage_from_graph_result(raw_result)
                artifact_reference = _artifact_reference_from_result(request, result)
                if artifact_reference is None:
                    raise protocol_failure(
                        "WORKER_RESULT_INVALID",
                        "图片生成结果缺少 artifactReference。",
                    )
                yield encode_sse(
                    stream_event(
                        StreamEventType.COMPLETED,
                        request_id=request.requestId,
                        trace_id=request.traceId,
                        stage="completed",
                        timestamp=_now(),
                        result=None,
                        usage=usage.model_dump(mode="json"),
                        extra=final_state_extra(
                            status=WorkerStatus.SUCCEEDED.value,
                            failure_stage=None,
                            fallback_used=False,
                            artifact_reference=artifact_reference,
                        ),
                    )
                )
                return
            deltas: list[str] = []
            usage = UsageSummary()
            for chunk in iter_chat_completion_chunks(request):
                if chunk.delta:
                    deltas.append(chunk.delta)
                    yield encode_sse(
                        delta_event(
                            request.requestId,
                            request.traceId,
                            _now(),
                            chunk.delta,
                        )
                    )
                if chunk.usage is not None:
                    usage = chunk.usage
                    yield encode_sse(
                        usage_event(
                            request.requestId,
                            request.traceId,
                            _now(),
                            usage.model_dump(mode="json"),
                        )
                    )
            result = _stream_completed_result(request, "".join(deltas))
            result = _coerce_text_payload(
                request,
                {
                    "format": result.format.value,
                    "payload": result.payload,
                    "usage": usage.model_dump(mode="json"),
                },
            )
            yield encode_sse(
                stream_event(
                    StreamEventType.COMPLETED,
                    request_id=request.requestId,
                    trace_id=request.traceId,
                    stage="completed",
                    timestamp=_now(),
                    result=result.model_dump(mode="json"),
                    usage=usage.model_dump(mode="json"),
                    extra=final_state_extra(
                        status=WorkerStatus.SUCCEEDED.value,
                        failure_stage=None,
                        fallback_used=False,
                        artifact_reference=None,
                    ),
                )
            )
        except Exception as exc:
            error = WorkerErrorPayload.model_validate(_worker_error_payload(exc))
            yield encode_sse(
                stream_event(
                    StreamEventType.ERROR,
                    request_id=request.requestId,
                    trace_id=request.traceId,
                    stage="error",
                    timestamp=_now(),
                    error=error.model_dump(mode="json"),
                    extra=final_state_extra(
                        status=WorkerStatus.FAILED.value,
                        failure_stage=_stream_failure_stage(exc),
                        fallback_used=False,
                        artifact_reference=None,
                        error_type=error.type,
                        error_message=error.message,
                    ),
                )
            )

    return StreamingResponse(events(), media_type="text/event-stream")


def _stream_result_format(request: AiInvokeRequest) -> ResultFormat:
    if requires_structured_output(request):
        return ResultFormat.STRUCTURED
    if request.capability.value in {"image_analysis", "fusion"}:
        return ResultFormat.MARKDOWN
    return ResultFormat.TEXT


def _stream_completed_result(request: AiInvokeRequest, content: str) -> AiResult:
    return AiResult(format=_stream_result_format(request), payload=content)


def _coerce_text_payload(
    request: AiInvokeRequest,
    result: dict[str, Any],
) -> AiResult:
    ai_result = AiResult.model_validate(result)
    if ai_result.format != ResultFormat.TEXT:
        return ai_result
    if not isinstance(ai_result.payload, str) or not ai_result.payload.strip():
        raise protocol_failure(
            "WORKER_RESULT_INVALID",
            "AI worker 的 TEXT 结果不能为空。",
            detail={
                "requestId": request.requestId,
                "traceId": request.traceId,
            },
        )
    return AiResult(format=ai_result.format, payload=ai_result.payload)


def _usage_from_graph_result(result: dict[str, Any]) -> UsageSummary:
    usage = result.get("usage")
    if isinstance(usage, dict):
        return UsageSummary.model_validate(usage)
    return UsageSummary()


def _worker_error_payload(exc: Exception) -> dict[str, Any]:
    if isinstance(exc, WorkerError):
        return exc.to_payload()
    return protocol_failure(
        "WORKER_RESULT_UNEXPECTED",
        "AI worker 结果处理失败。",
        detail={"errorClass": type(exc).__name__},
    ).to_payload()


def _failure_stage(exc: Exception) -> FailureStage:
    if not isinstance(exc, WorkerError):
        return FailureStage.WORKER_RESULT
    if exc.code.startswith("WORKER_RESULT"):
        return FailureStage.WORKER_RESULT
    if exc.error_type == WorkerErrorType.OUTPUT_FORMAT_FAILURE:
        return FailureStage.WORKER_RESULT
    return FailureStage.WORKER_REQUEST


def _stream_failure_stage(exc: Exception) -> FailureStage:
    if isinstance(exc, WorkerError) and exc.code == "MODEL_STREAM_CHUNK_INVALID":
        return FailureStage.WORKER_STREAM
    return _failure_stage(exc)


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
        errorType=error.type,
        errorMessage=error.message,
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
            "errorType": error.type,
            "errorMessage": error.message,
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


def _artifact_reference_from_result(
    request: AiInvokeRequest,
    result: AiResult,
) -> ArtifactReference | None:
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
        return _artifact_reference_from_image_result(request, payload)
    return ArtifactReference.model_validate(payload)


def _artifact_reference_from_image_result(
    request: AiInvokeRequest,
    payload: dict[str, Any],
) -> ArtifactReference:
    data = payload.get("data")
    content_type = payload.get("contentType")
    filename = payload.get("filename")
    if not isinstance(data, bytes):
        raise protocol_failure(
            "WORKER_RESULT_INVALID",
            "图片生成结果缺少图片 bytes。",
        )
    if not isinstance(content_type, str) or not content_type.strip():
        raise protocol_failure(
            "WORKER_RESULT_INVALID",
            "图片生成结果缺少 contentType。",
        )
    if not isinstance(filename, str) or not filename.strip():
        raise protocol_failure(
            "WORKER_RESULT_INVALID",
            "图片生成结果缺少 filename。",
        )
    settings = load_settings()
    if len(data) > settings.max_artifact_bytes:
        raise WorkerError(
            WorkerErrorType.IMAGE_INPUT_FAILURE,
            "IMAGE_ARTIFACT_TOO_LARGE",
            "图片生成结果超过 workers artifact 大小限制。",
            detail={
                "sizeBytes": len(data),
                "maxArtifactBytes": settings.max_artifact_bytes,
            },
        )
    store = RequestArtifactStore(
        request.requestId,
        settings.temp_dir,
        settings.artifact_chunk_bytes,
        settings.artifact_ttl_hours,
    )
    metadata = store.put_bytes(
        data=data,
        format=ResultFormat.ARTIFACT.value,
        filename=filename,
        content_type=content_type,
    )
    return ArtifactReference(
        artifactId=metadata.artifact_id,
        downloadPath=metadata.download_path,
        contentType=metadata.content_type,
        filename=metadata.filename,
        sizeBytes=metadata.size_bytes,
        sha256=metadata.sha256,
        expiresAt=metadata.expires_at,
    )
