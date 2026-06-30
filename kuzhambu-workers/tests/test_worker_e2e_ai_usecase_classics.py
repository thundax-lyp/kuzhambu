import json
from time import time

from fastapi.testclient import TestClient

from kuzhambu_workers.core.security import sign_request
from kuzhambu_workers.main import app


import pytest


@pytest.mark.parametrize(
    ("operation", "capability", "path"),
    [
        (
            "CLASSICS_SANCAI_TRANSLATE",
            "translate",
            "/internal/ai/classics/sancai/translate",
        ),
        (
            "CLASSICS_SANCAI_SUMMARY",
            "summary",
            "/internal/ai/classics/sancai/summary",
        ),
        (
            "CLASSICS_WANGQI_SUMMARY",
            "summary",
            "/internal/ai/classics/wangqi/summary",
        ),
        (
            "CLASSICS_MING_CUSTOMS_SUMMARY",
            "summary",
            "/internal/ai/classics/ming-customs/summary",
        ),
    ],
)
def test_worker_e2e_classics_usecase_text_returns_non_empty_payload(
    monkeypatch, operation: str, capability: str, path: str
) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    body = _body(
        operation=operation,
        capability=capability,
        scope="classics",
        stream=False,
    )

    response = TestClient(app).post(path, content=body, headers=_headers(body, path))

    assert response.status_code == 200
    payload = response.json()
    assert payload["status"] == "SUCCEEDED"
    assert payload["capability"] == capability
    assert payload["result"]["format"] == "TEXT"
    assert isinstance(payload["result"]["payload"], str)
    assert payload["result"]["payload"] != ""
    assert json.loads(payload["result"]["payload"])["choices"][0]["message"]["content"] != ""


def _body(*, operation: str, capability: str, scope: str, stream: bool) -> bytes:
    payload = {
        "requestId": "req-1",
        "traceId": "trace-1",
        "callerDomain": "AI",
        "operation": operation,
        "capability": capability,
        "scope": scope,
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


def _headers(body: bytes, path: str) -> dict[str, str]:
    timestamp = str(int(time() * 1000))
    signature = sign_request("POST", path, timestamp, "req-1", body, "worker-secret")
    return {
        "X-Kuzhambu-Service": "kuzhambu-ai",
        "X-Kuzhambu-Request-Id": "req-1",
        "X-Kuzhambu-Trace-Id": "trace-1",
        "X-Kuzhambu-Timestamp": timestamp,
        "X-Kuzhambu-Signature": signature,
    }
