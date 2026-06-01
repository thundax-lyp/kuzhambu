import json
from time import time

from fastapi.testclient import TestClient

from kuzhambu_workers.core.security import sign_request
from kuzhambu_workers.main import app


def test_worker_e2e_ai_invoke_and_stream(monkeypatch) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    client = TestClient(app)
    invoke_body = _body("summary", stream=False)

    invoke_response = client.post(
        "/internal/ai/invoke",
        content=invoke_body,
        headers=_headers(invoke_body, "/internal/ai/invoke"),
    )

    assert invoke_response.status_code == 200
    assert invoke_response.json()["status"] == "SUCCEEDED"
    assert invoke_response.json()["result"] == {"format": "TEXT", "payload": ""}

    stream_body = _body("answer_generation", stream=True)
    stream_response = client.post(
        "/internal/ai/stream",
        content=stream_body,
        headers=_headers(stream_body, "/internal/ai/stream"),
    )

    assert stream_response.status_code == 200
    assert "event: started" in stream_response.text
    assert "event: completed" in stream_response.text
    assert '"status":"SUCCEEDED"' in stream_response.text


def _body(capability: str, *, stream: bool) -> bytes:
    payload = {
        "requestId": "req-1",
        "traceId": "trace-1",
        "callerDomain": "AI",
        "operation": "E2E",
        "capability": capability,
        "scope": "SANCAI",
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
        "input": {"contentType": "TEXT", "payload": {"text": "hello"}},
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
