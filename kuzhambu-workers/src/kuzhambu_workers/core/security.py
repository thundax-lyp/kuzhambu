from collections.abc import Mapping
from dataclasses import dataclass
from hashlib import sha256
from hmac import compare_digest, new
from time import time

from kuzhambu_workers.core.config import WorkerSettings
from kuzhambu_workers.core.errors import WorkerError
from kuzhambu_workers.schemas.common import WorkerErrorType

SERVICE_HEADER = "x-kuzhambu-service"
REQUEST_ID_HEADER = "x-kuzhambu-request-id"
TRACE_ID_HEADER = "x-kuzhambu-trace-id"
TIMESTAMP_HEADER = "x-kuzhambu-timestamp"
SIGNATURE_HEADER = "x-kuzhambu-signature"

COMMON_PATHS = {
    "/internal/health",
    "/internal/capabilities",
}

SERVICE_PATHS = {
    "kuzhambu-ai": {
        "/internal/ai/invoke",
        "/internal/ai/stream",
    },
    "kuzhambu-classics": {
        "/internal/render/classics-export",
        "/internal/render/classics-export/stream",
        "/internal/render/sancai-showcase",
        "/internal/render/sancai-showcase/stream",
    },
    "kuzhambu-operations": {
        "/internal/render/operations-report",
        "/internal/render/operations-report/stream",
    },
}


@dataclass(frozen=True)
class VerifiedService:
    service: str
    request_id: str
    trace_id: str


def verify_internal_request(
    *,
    method: str,
    path: str,
    headers: Mapping[str, str],
    body: bytes,
    settings: WorkerSettings,
    request_id: str,
    trace_id: str,
    now_ms: int | None = None,
) -> VerifiedService:
    normalized_headers = {key.lower(): value for key, value in headers.items()}
    service = _required_header(normalized_headers, SERVICE_HEADER)
    header_request_id = _required_header(normalized_headers, REQUEST_ID_HEADER)
    header_trace_id = _required_header(normalized_headers, TRACE_ID_HEADER)
    timestamp = _required_header(normalized_headers, TIMESTAMP_HEADER)
    signature = _required_header(normalized_headers, SIGNATURE_HEADER)

    if header_request_id != request_id or header_trace_id != trace_id:
        raise _auth_error("HEADER_BODY_MISMATCH", "请求头与请求体追踪标识不一致。", retryable=False)
    if service not in settings.allowed_services:
        raise _auth_error("SERVICE_NOT_ALLOWED", "调用方服务不在允许列表中。", retryable=False)
    if path not in COMMON_PATHS and path not in SERVICE_PATHS.get(service, set()):
        raise _auth_error("PATH_FORBIDDEN", "调用方服务不允许访问该路径。", retryable=False)
    if not settings.internal_secret:
        raise _auth_error("WORKER_SECRET_MISSING", "worker 内部密钥未配置。", retryable=True)

    request_time = _parse_timestamp(timestamp)
    current_time = now_ms if now_ms is not None else int(time() * 1000)
    if abs(current_time - request_time) > settings.max_clock_skew_ms:
        raise _auth_error("TIMESTAMP_OUT_OF_RANGE", "请求时间戳超出允许偏差。", retryable=False)

    expected = sign_request(
        method, path, timestamp, header_request_id, body, settings.internal_secret
    )
    if not compare_digest(expected, signature):
        raise _auth_error("SIGNATURE_MISMATCH", "请求签名不匹配。", retryable=False)

    return VerifiedService(service=service, request_id=header_request_id, trace_id=header_trace_id)


def sign_request(
    method: str,
    path: str,
    timestamp: str,
    request_id: str,
    body: bytes,
    secret: str,
) -> str:
    if not secret:
        raise ValueError("secret must not be blank")
    message = signing_input(method, path, timestamp, request_id, body).encode()
    return new(secret.encode(), message, "sha256").hexdigest()


def signing_input(method: str, path: str, timestamp: str, request_id: str, body: bytes) -> str:
    return "\n".join(
        [
            method.upper(),
            path,
            timestamp,
            request_id,
            sha256(body).hexdigest(),
        ]
    )


def _required_header(headers: Mapping[str, str], name: str) -> str:
    value = headers.get(name)
    if value is None or value == "":
        raise _auth_error("MISSING_HEADER", f"缺少内部认证请求头：{name}。", retryable=False)
    return value


def _parse_timestamp(timestamp: str) -> int:
    try:
        return int(timestamp)
    except ValueError as exc:
        raise _auth_error("BAD_TIMESTAMP", "请求时间戳格式不合法。", retryable=False) from exc


def _auth_error(code: str, message: str, *, retryable: bool) -> WorkerError:
    return WorkerError(
        WorkerErrorType.WORKER_PROTOCOL_FAILURE,
        code,
        message,
        retryable=retryable,
    )
