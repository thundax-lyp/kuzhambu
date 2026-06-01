import pytest

from kuzhambu_workers.core.config import load_settings
from kuzhambu_workers.core.errors import WorkerError
from kuzhambu_workers.core.security import (
    sign_request,
    signing_input,
    verify_internal_request,
)


def test_signing_input_matches_contract() -> None:
    body = b'{"requestId":"req-1"}'

    assert signing_input("POST", "/internal/ai/invoke", "1710000000000", "req-1", body) == (
        "POST\n"
        "/internal/ai/invoke\n"
        "1710000000000\n"
        "req-1\n"
        "656dbf9957b0fe60245a1b9610b422a76a8effab505654f5b9929f54e50c052f"
    )


def test_verify_internal_request_accepts_valid_signature(monkeypatch) -> None:
    settings = _settings(monkeypatch)
    body = b'{"requestId":"req-1","traceId":"trace-1"}'
    headers = _headers(body, service="kuzhambu-ai", path="/internal/ai/invoke")

    verified = verify_internal_request(
        method="POST",
        path="/internal/ai/invoke",
        headers=headers,
        body=body,
        settings=settings,
        request_id="req-1",
        trace_id="trace-1",
        now_ms=1_710_000_000_000,
    )

    assert verified.service == "kuzhambu-ai"
    assert verified.request_id == "req-1"
    assert verified.trace_id == "trace-1"


@pytest.mark.parametrize(
    ("headers_update", "code"),
    [
        ({"X-Kuzhambu-Signature": "bad"}, "SIGNATURE_MISMATCH"),
        ({"X-Kuzhambu-Service": "unknown-service"}, "SERVICE_NOT_ALLOWED"),
        ({"X-Kuzhambu-Request-Id": "other"}, "HEADER_BODY_MISMATCH"),
    ],
)
def test_verify_internal_request_rejects_invalid_headers(monkeypatch, headers_update, code) -> None:
    settings = _settings(monkeypatch)
    body = b'{"requestId":"req-1","traceId":"trace-1"}'
    headers = _headers(body, service="kuzhambu-ai", path="/internal/ai/invoke")
    headers.update(headers_update)

    with pytest.raises(WorkerError) as raised:
        verify_internal_request(
            method="POST",
            path="/internal/ai/invoke",
            headers=headers,
            body=body,
            settings=settings,
            request_id="req-1",
            trace_id="trace-1",
            now_ms=1_710_000_000_000,
        )

    assert raised.value.code == code


def test_verify_internal_request_rejects_timestamp_skew(monkeypatch) -> None:
    settings = _settings(monkeypatch)
    body = b'{"requestId":"req-1","traceId":"trace-1"}'
    headers = _headers(body, service="kuzhambu-ai", path="/internal/ai/invoke")

    with pytest.raises(WorkerError) as raised:
        verify_internal_request(
            method="POST",
            path="/internal/ai/invoke",
            headers=headers,
            body=body,
            settings=settings,
            request_id="req-1",
            trace_id="trace-1",
            now_ms=1_710_001_000_001,
        )

    assert raised.value.code == "TIMESTAMP_OUT_OF_RANGE"


def test_verify_internal_request_rejects_path_for_service(monkeypatch) -> None:
    settings = _settings(monkeypatch)
    body = b'{"requestId":"req-1","traceId":"trace-1"}'
    headers = _headers(body, service="kuzhambu-classics", path="/internal/ai/invoke")

    with pytest.raises(WorkerError) as raised:
        verify_internal_request(
            method="POST",
            path="/internal/ai/invoke",
            headers=headers,
            body=body,
            settings=settings,
            request_id="req-1",
            trace_id="trace-1",
            now_ms=1_710_000_000_000,
        )

    assert raised.value.code == "PATH_FORBIDDEN"


def _settings(monkeypatch):
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai,kuzhambu-classics")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    return load_settings()


def _headers(body: bytes, *, service: str, path: str) -> dict[str, str]:
    timestamp = "1710000000000"
    signature = sign_request("POST", path, timestamp, "req-1", body, "worker-secret")
    return {
        "X-Kuzhambu-Service": service,
        "X-Kuzhambu-Request-Id": "req-1",
        "X-Kuzhambu-Trace-Id": "trace-1",
        "X-Kuzhambu-Timestamp": timestamp,
        "X-Kuzhambu-Signature": signature,
    }
