import json
from time import time

from fastapi.testclient import TestClient

from kuzhambu_workers.core.security import sign_request
from kuzhambu_workers.main import app


def test_worker_e2e_ai_usecase_rejects_business_service_identity(monkeypatch) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai,kuzhambu-classics")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    path = "/internal/ai/classics/sancai/translate"
    body = _body(operation="CLASSICS_SANCAI_TRANSLATE", capability="translate", stream=False)

    response = TestClient(app).post(
        path,
        content=body,
        headers=_headers(body, path, service="kuzhambu-classics"),
    )

    assert response.status_code == 403
    assert response.json()["error"]["code"] == "PATH_FORBIDDEN"


def test_worker_e2e_ai_usecase_rejects_path_capability_mismatch(monkeypatch) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    path = "/internal/ai/classics/sancai/translate"
    body = _body(operation="CLASSICS_SANCAI_TRANSLATE", capability="summary", stream=False)

    response = TestClient(app).post(
        path,
        content=body,
        headers=_headers(body, path, service="kuzhambu-ai"),
    )

    assert response.status_code == 400
    assert response.json()["status"] == "FAILED"
    assert response.json()["error"]["code"] == "BAD_REQUEST"


def _body(*, operation: str, capability: str, stream: bool) -> bytes:
    payload = {
        "requestId": "req-1",
        "traceId": "trace-1",
        "callerDomain": "AI",
        "operation": operation,
        "capability": capability,
        "scope": "classics",
        "modelConfig": {
            "serviceRole": "PRIMARY",
            "apiSource": "OPENAI_COMPATIBLE",
            "baseUrl": "https://model.example/v1",
            "apiKey": "process-only",
            "modelName": "model",
        },
        "prompt": {
            "messages": [
                {"role": "system", "content": "system"},
                {"role": "user", "content": "user"},
            ]
        },
        "input": {"contentType": "CLASSICS_SNAPSHOT", "payload": {"text": "hello"}},
        "outputSchema": {"type": "text"},
        "options": {"stream": stream, "forceJson": False, "locale": "zh-CN"},
    }
    return json.dumps(payload, ensure_ascii=False, separators=(",", ":")).encode()


def _headers(body: bytes, path: str, *, service: str) -> dict[str, str]:
    timestamp = str(int(time() * 1000))
    signature = sign_request("POST", path, timestamp, "req-1", body, "worker-secret")
    return {
        "X-Kuzhambu-Service": service,
        "X-Kuzhambu-Request-Id": "req-1",
        "X-Kuzhambu-Trace-Id": "trace-1",
        "X-Kuzhambu-Timestamp": timestamp,
        "X-Kuzhambu-Signature": signature,
    }
