from base64 import b64encode
from collections.abc import AsyncIterator
from datetime import datetime, timezone

from fastapi import APIRouter, Request
from fastapi.responses import JSONResponse, StreamingResponse
from pydantic import ValidationError

from kuzhambu_workers.core.config import WorkerSettings, load_settings
from kuzhambu_workers.core.errors import WorkerError, protocol_failure, to_error_payload
from kuzhambu_workers.core.security import verify_internal_request
from kuzhambu_workers.render.artifact_store import RequestArtifactStore
from kuzhambu_workers.render.browser_pool import BrowserPool
from kuzhambu_workers.render.classics_export import RenderedArtifact, render_classics_export
from kuzhambu_workers.render.operations_report import render_operations_report
from kuzhambu_workers.render.sancai_showcase import render_sancai_showcase
from kuzhambu_workers.schemas.common import (
    UsageSummary,
    WorkerErrorPayload,
    WorkerErrorType,
    WorkerStatus,
)
from kuzhambu_workers.schemas.render import (
    ArtifactEncoding,
    RenderArtifact,
    RenderOutputFormat,
    RenderRequest,
    RenderResponse,
    RenderType,
)
from kuzhambu_workers.schemas.stream import StreamEventType
from kuzhambu_workers.streaming.events import artifact_chunk_event, started_event, stream_event
from kuzhambu_workers.streaming.sse import encode_sse

router = APIRouter(prefix="/internal/render", tags=["Render"])
CLASSICS_EXPORT_NOTICE = (
    "Classics 导出 usecase 接口。调用方必须先完成权限过滤、风险确认和内容快照准备。"
)
SANCAI_SHOWCASE_NOTICE = (
    "三才图会静态展示 usecase 接口。调用方必须传入完整展示快照，workers 不回查业务数据。"
)
OPERATIONS_REPORT_NOTICE = "Operations 报表 usecase 接口。调用方必须传入已聚合的报表快照。"


@router.post(
    "/classics-export",
    response_model=None,
    summary="Classics export",
    description=CLASSICS_EXPORT_NOTICE,
)
async def classics_export(request: Request) -> JSONResponse:
    return await _invoke(request, RenderType.CLASSICS_EXPORT)


@router.post(
    "/sancai-showcase",
    response_model=None,
    summary="Sancai showcase",
    description=SANCAI_SHOWCASE_NOTICE,
)
async def sancai_showcase(request: Request) -> JSONResponse:
    return await _invoke(request, RenderType.SANCAI_SHOWCASE)


@router.post(
    "/operations-report",
    response_model=None,
    summary="Operations report",
    description=OPERATIONS_REPORT_NOTICE,
)
async def operations_report(request: Request) -> JSONResponse:
    return await _invoke(request, RenderType.OPERATIONS_REPORT)


@router.post(
    "/classics-export/stream",
    response_model=None,
    summary="Classics export stream",
    description=CLASSICS_EXPORT_NOTICE,
)
async def classics_export_stream(request: Request) -> StreamingResponse | JSONResponse:
    return await _stream(request, RenderType.CLASSICS_EXPORT)


@router.post(
    "/sancai-showcase/stream",
    response_model=None,
    summary="Sancai showcase stream",
    description=SANCAI_SHOWCASE_NOTICE,
)
async def sancai_showcase_stream(request: Request) -> StreamingResponse | JSONResponse:
    return await _stream(request, RenderType.SANCAI_SHOWCASE)


@router.post(
    "/operations-report/stream",
    response_model=None,
    summary="Operations report stream",
    description=OPERATIONS_REPORT_NOTICE,
)
async def operations_report_stream(request: Request) -> StreamingResponse | JSONResponse:
    return await _stream(request, RenderType.OPERATIONS_REPORT)


async def _invoke(request: Request, expected_type: RenderType) -> JSONResponse:
    body = await request.body()
    settings = load_settings()
    parsed = _parse_request(body)
    if isinstance(parsed, JSONResponse):
        return parsed

    auth_failure = _verify(request, body, parsed, settings)
    if auth_failure is not None:
        return auth_failure

    try:
        _assert_render_type(parsed, expected_type)
        artifact = await _render(parsed, settings)
        response = RenderResponse(
            requestId=parsed.requestId,
            traceId=parsed.traceId,
            status=WorkerStatus.SUCCEEDED,
            renderType=parsed.renderType,
            artifact=_inline_artifact(artifact),
            summary=artifact.summary,
            usage=UsageSummary(),
        )
        return JSONResponse(response.model_dump(mode="json"))
    except Exception as exc:
        return _failed_response(parsed, WorkerErrorPayload.model_validate(to_error_payload(exc)))


async def _stream(request: Request, expected_type: RenderType) -> StreamingResponse | JSONResponse:
    body = await request.body()
    settings = load_settings()
    parsed = _parse_request(body)
    if isinstance(parsed, JSONResponse):
        return parsed

    auth_failure = _verify(request, body, parsed, settings)
    if auth_failure is not None:
        return auth_failure

    async def events() -> AsyncIterator[str]:
        store = RequestArtifactStore(
            parsed.requestId,
            settings.temp_dir,
            settings.artifact_chunk_bytes,
        )
        yield encode_sse(started_event(parsed.requestId, parsed.traceId, _now()))
        try:
            _assert_render_type(parsed, expected_type)
            yield encode_sse(
                stream_event(
                    StreamEventType.PROGRESS,
                    request_id=parsed.requestId,
                    trace_id=parsed.traceId,
                    stage="render",
                    timestamp=_now(),
                    progress={"current": 1, "total": 2, "message": "rendering"},
                )
            )
            artifact = await _render(parsed, settings)
            metadata = store.put_bytes(
                data=artifact.data,
                format=artifact.format.value,
                filename=artifact.filename,
                content_type=artifact.content_type,
            )
            for chunk in store.chunks(metadata.artifact_id):
                yield encode_sse(
                    artifact_chunk_event(
                        request_id=parsed.requestId,
                        trace_id=parsed.traceId,
                        timestamp=_now(),
                        chunk=chunk,
                    )
                )
            yield encode_sse(
                stream_event(
                    StreamEventType.COMPLETED,
                    request_id=parsed.requestId,
                    trace_id=parsed.traceId,
                    stage="completed",
                    timestamp=_now(),
                    artifact={
                        "format": artifact.format.value,
                        "filename": artifact.filename,
                        "contentType": artifact.content_type,
                        "encoding": ArtifactEncoding.STREAM.value,
                        "artifactId": metadata.artifact_id,
                        "chunkCount": metadata.chunk_count,
                        "sizeBytes": metadata.size_bytes,
                        "sha256": metadata.sha256,
                    },
                    usage=UsageSummary().model_dump(mode="json"),
                    extra={
                        "status": WorkerStatus.SUCCEEDED.value,
                        "summary": artifact.summary.model_dump(mode="json"),
                    },
                )
            )
        except Exception as exc:
            error = WorkerErrorPayload.model_validate(to_error_payload(exc))
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
        finally:
            store.cleanup()

    return StreamingResponse(events(), media_type="text/event-stream")


def _parse_request(body: bytes) -> RenderRequest | JSONResponse:
    try:
        return RenderRequest.model_validate_json(body)
    except ValidationError as exc:
        error = protocol_failure(
            "BAD_REQUEST",
            "Render worker 请求体不合法。",
            detail={"errors": exc.errors(include_input=False)},
        ).to_payload()
        return _error_json(WorkerErrorPayload.model_validate(error), 400)


def _verify(
    request: Request,
    body: bytes,
    parsed: RenderRequest,
    settings: WorkerSettings,
) -> JSONResponse | None:
    try:
        verify_internal_request(
            method=request.method,
            path=request.url.path,
            headers=request.headers,
            body=body,
            settings=settings,
            request_id=parsed.requestId,
            trace_id=parsed.traceId,
        )
    except WorkerError as exc:
        return _error_json(
            WorkerErrorPayload.model_validate(exc.to_payload()),
            _status_code(exc.code),
        )
    return None


async def _render(request: RenderRequest, settings: WorkerSettings) -> RenderedArtifact:
    if request.renderType == RenderType.CLASSICS_EXPORT:
        return render_classics_export(request)
    if request.renderType == RenderType.SANCAI_SHOWCASE:
        return render_sancai_showcase(request)
    if request.renderType == RenderType.OPERATIONS_REPORT:
        if request.output.format == RenderOutputFormat.PDF:
            pool = BrowserPool(
                pool_size=settings.browser_pool_size,
                max_pages=settings.browser_max_pages,
                page_timeout_ms=settings.browser_page_timeout_ms,
            )
            try:
                return await render_operations_report(request, pdf_renderer=pool)
            finally:
                await pool.stop()
        return await render_operations_report(request)
    raise WorkerError(
        WorkerErrorType.UNSUPPORTED_CAPABILITY,
        "UNSUPPORTED_RENDER_TYPE",
        "workers 不支持请求的渲染类型。",
        detail={"renderType": request.renderType.value},
    )


def _assert_render_type(request: RenderRequest, expected_type: RenderType) -> None:
    if request.renderType != expected_type:
        raise protocol_failure(
            "RENDER_TYPE_PATH_MISMATCH",
            "请求路径与 renderType 不一致。",
            detail={"renderType": request.renderType.value, "expected": expected_type.value},
        )


def _inline_artifact(artifact: RenderedArtifact) -> RenderArtifact:
    if artifact.format in {
        RenderOutputFormat.CSV,
        RenderOutputFormat.JSON,
        RenderOutputFormat.HTML,
    }:
        encoding = ArtifactEncoding.TEXT
        content = artifact.data.decode("utf-8")
    else:
        encoding = ArtifactEncoding.BASE64
        content = b64encode(artifact.data).decode()
    return RenderArtifact(
        format=artifact.format,
        filename=artifact.filename,
        contentType=artifact.content_type,
        encoding=encoding,
        content=content,
        sizeBytes=artifact.size_bytes,
        sha256=artifact.sha256,
    )


def _failed_response(request: RenderRequest, error: WorkerErrorPayload) -> JSONResponse:
    response = RenderResponse(
        requestId=request.requestId,
        traceId=request.traceId,
        status=WorkerStatus.FAILED,
        renderType=request.renderType,
        artifact=None,
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
