from datetime import datetime, timezone

from fastapi import APIRouter, Request
from fastapi.responses import JSONResponse, Response

from kuzhambu_workers.core.config import load_settings
from kuzhambu_workers.core.errors import WorkerError, WorkerErrorPayload
from kuzhambu_workers.core.security import (
    REQUEST_ID_HEADER,
    TRACE_ID_HEADER,
    verify_internal_request,
)
from kuzhambu_workers.render.artifact_store import RequestArtifactStore

router = APIRouter(prefix="/internal/artifacts", tags=["Artifacts"])


@router.get("/{artifact_id}")
async def download_artifact(artifact_id: str, request: Request) -> Response | JSONResponse:
    body = b""
    request_id = request.headers.get(REQUEST_ID_HEADER, "")
    trace_id = request.headers.get(TRACE_ID_HEADER, "")
    settings = load_settings()
    try:
        verify_internal_request(
            method=request.method,
            path=request.url.path,
            headers=request.headers,
            body=body,
            settings=settings,
            request_id=request_id,
            trace_id=trace_id,
        )
        store = RequestArtifactStore(
            request_id or "artifact-download",
            settings.temp_dir,
            settings.artifact_chunk_bytes,
            settings.artifact_ttl_hours,
        )
        metadata = store.get_metadata(artifact_id)
        if _is_expired(metadata.expires_at):
            return _error_json("ARTIFACT_EXPIRED", "artifact 已过期。", 410)
        return Response(
            content=store.read_bytes(artifact_id),
            media_type=metadata.content_type,
            headers={
                "Content-Disposition": f'attachment; filename="{metadata.filename}"',
                "X-Kuzhambu-Artifact-Id": metadata.artifact_id,
                "X-Kuzhambu-Artifact-Sha256": metadata.sha256,
                "X-Kuzhambu-Artifact-Expires-At": metadata.expires_at,
                "X-Kuzhambu-Artifact-Size-Bytes": str(metadata.size_bytes),
            },
        )
    except FileNotFoundError:
        return _error_json("ARTIFACT_NOT_FOUND", "artifact 不存在。", 404)
    except WorkerError as exc:
        return JSONResponse(
            {
                "status": "FAILED",
                "error": WorkerErrorPayload.model_validate(exc.to_payload()).model_dump(mode="json"),
            },
            status_code=_status_code(exc.code),
        )


def _is_expired(expires_at: str) -> bool:
    expires = datetime.fromisoformat(expires_at.replace("Z", "+00:00"))
    return datetime.now(timezone.utc) >= expires


def _error_json(code: str, message: str, status_code: int) -> JSONResponse:
    return JSONResponse(
        {
            "status": "FAILED",
            "error": {
                "type": "WORKER_PROTOCOL_FAILURE",
                "code": code,
                "message": message,
                "retryable": False,
                "detail": {},
            },
        },
        status_code=status_code,
    )


def _status_code(code: str) -> int:
    if code in {"SERVICE_NOT_ALLOWED", "PATH_FORBIDDEN"}:
        return 403
    if code == "BAD_REQUEST":
        return 400
    return 401
