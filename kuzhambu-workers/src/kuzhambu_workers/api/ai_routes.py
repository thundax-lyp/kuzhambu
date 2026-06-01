from datetime import datetime, timezone

from fastapi import APIRouter, Request
from fastapi.responses import JSONResponse, StreamingResponse
from pydantic import ValidationError

from kuzhambu_workers.ai.graph_registry import GraphRegistry
from kuzhambu_workers.core.config import load_settings
from kuzhambu_workers.core.errors import WorkerError, protocol_failure, to_error_payload
from kuzhambu_workers.core.security import verify_internal_request
from kuzhambu_workers.schemas.ai import AiInvokeRequest, AiInvokeResponse, AiResult
from kuzhambu_workers.schemas.common import UsageSummary, WorkerErrorPayload, WorkerStatus
from kuzhambu_workers.schemas.stream import StreamEventType
from kuzhambu_workers.streaming.events import started_event, stream_event
from kuzhambu_workers.streaming.sse import encode_sse

router = APIRouter(prefix="/internal/ai")
_REGISTRY = GraphRegistry.build_default()


@router.post("/invoke", response_model=None)
async def invoke(request: Request) -> JSONResponse:
    body = await request.body()
    parsed = _parse_request(body)
    if isinstance(parsed, JSONResponse):
        return parsed

    auth_failure = _verify(request, body, parsed)
    if auth_failure is not None:
        return auth_failure

    try:
        result = _REGISTRY.invoke(parsed)
        response = AiInvokeResponse(
            requestId=parsed.requestId,
            traceId=parsed.traceId,
            status=WorkerStatus.SUCCEEDED,
            capability=parsed.capability,
            result=AiResult.model_validate(result),
            usage=UsageSummary(),
        )
        return JSONResponse(response.model_dump(mode="json"))
    except Exception as exc:
        return _failed_response(parsed, to_error_payload(exc))


@router.post("/stream", response_model=None)
async def stream(request: Request) -> StreamingResponse | JSONResponse:
    body = await request.body()
    parsed = _parse_request(body)
    if isinstance(parsed, JSONResponse):
        return parsed

    auth_failure = _verify(request, body, parsed)
    if auth_failure is not None:
        return auth_failure

    async def events():
        timestamp = _now()
        yield encode_sse(started_event(parsed.requestId, parsed.traceId, timestamp))
        try:
            result = _REGISTRY.invoke(parsed)
            yield encode_sse(
                stream_event(
                    StreamEventType.COMPLETED,
                    request_id=parsed.requestId,
                    trace_id=parsed.traceId,
                    stage="completed",
                    timestamp=_now(),
                    result=result,
                    usage=UsageSummary().model_dump(mode="json"),
                    extra={"status": WorkerStatus.SUCCEEDED.value},
                )
            )
        except Exception as exc:
            error = to_error_payload(exc)
            yield encode_sse(
                stream_event(
                    StreamEventType.ERROR,
                    request_id=parsed.requestId,
                    trace_id=parsed.traceId,
                    stage="error",
                    timestamp=_now(),
                    error=error.model_dump(mode="json"),
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
        return _error_json(error, 400)


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
        return _error_json(exc.to_payload(), _status_code(exc.code))
    return None


def _failed_response(request: AiInvokeRequest, error: WorkerErrorPayload) -> JSONResponse:
    response = AiInvokeResponse(
        requestId=request.requestId,
        traceId=request.traceId,
        status=WorkerStatus.FAILED,
        capability=request.capability,
        result=None,
        usage=UsageSummary(),
        error=error,
    )
    return JSONResponse(response.model_dump(mode="json"))


def _error_json(error: WorkerErrorPayload, status_code: int) -> JSONResponse:
    return JSONResponse(
        {
            "status": WorkerStatus.FAILED.value,
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
