import json
from time import time
from typing import Any

from fastapi.testclient import TestClient

from kuzhambu_workers.api.ai_routes import invoke_ai_graph
from kuzhambu_workers.core.security import sign_request
from kuzhambu_workers.main import app
from kuzhambu_workers.schemas.ai import AiInvokeRequest


def test_ai_invoke_returns_success(monkeypatch) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    body = _body("translate", operation="CLASSICS_SANCAI_TRANSLATE")

    response = TestClient(app).post(
        "/internal/ai/invoke",
        content=body,
        headers=_headers(body, "/internal/ai/invoke"),
    )

    assert response.status_code == 200
    payload = response.json()
    assert payload["requestId"] == "req-1"
    assert payload["traceId"] == "trace-1"
    assert payload["status"] == "SUCCEEDED"
    assert payload["capability"] == "translate"
    assert payload["failureStage"] is None
    assert payload["fallbackUsed"] is False
    assert payload["artifactReference"] is None
    assert payload["result"]["format"] == "TEXT"
    assert payload["result"]["payload"] == "[CLASSICS_SANCAI_TRANSLATE] hello"


def test_ai_stream_returns_started_and_completed(monkeypatch) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    body = _body("summary", stream=True, operation="CLASSICS_SANCAI_SUMMARY")

    response = TestClient(app).post(
        "/internal/ai/stream",
        content=body,
        headers=_headers(body, "/internal/ai/stream"),
    )

    assert response.status_code == 200
    assert response.headers["content-type"].startswith("text/event-stream")
    text = response.text
    assert "event: started" in text
    assert "event: completed" in text
    assert '"status":"SUCCEEDED"' in text
    assert '"failureStage":null' in text
    assert '"fallbackUsed":false' in text
    assert '"format":"TEXT"' in text
    assert "[CLASSICS_SANCAI_SUMMARY]" in text


def test_ai_invoke_rejects_bad_signature(monkeypatch) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    body = _body("translate")
    headers = _headers(body, "/internal/ai/invoke")
    headers["X-Kuzhambu-Signature"] = "bad"

    response = TestClient(app).post("/internal/ai/invoke", content=body, headers=headers)

    assert response.status_code == 401
    assert response.json()["error"]["code"] == "SIGNATURE_MISMATCH"


def test_ai_invoke_maps_graph_errors_to_failed_response(monkeypatch) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    body = _body("image_generation")

    response = TestClient(app).post(
        "/internal/ai/invoke",
        content=body,
        headers=_headers(body, "/internal/ai/invoke"),
    )

    assert response.status_code == 400
    payload = response.json()
    assert payload["failureStage"] == "REQUEST_VALIDATE"
    assert payload["fallbackUsed"] is False
    assert payload["error"]["code"] == "BAD_REQUEST"


def test_ai_invoke_rejects_empty_text_payload() -> None:
    request = AiInvokeRequest.model_validate_json(_body("summary"))
    response = invoke_ai_graph(request, registry=_text_payload_registry(""))
    response_payload = json.loads(response.body)
    assert response_payload["status"] == "FAILED"
    assert response_payload["failureStage"] == "WORKER_RESULT"
    assert response_payload["error"]["code"] == "WORKER_RESULT_INVALID"


def test_ai_invoke_rejects_text_payload_missing_fields() -> None:
    request = AiInvokeRequest.model_validate_json(_body("summary"))
    response = invoke_ai_graph(
        request,
        registry=_text_payload_registry('{"choices":[{"message":{}}]}'),
    )
    response_payload = json.loads(response.body)
    assert response_payload["status"] == "FAILED"
    assert response_payload["failureStage"] == "WORKER_RESULT"
    assert response_payload["error"]["code"] == "WORKER_RESULT_INVALID"


def test_ai_invoke_rejects_invalid_text_payload_json() -> None:
    request = AiInvokeRequest.model_validate_json(_body("summary"))
    response = invoke_ai_graph(
        request,
        registry=_text_payload_registry("{invalid-json"),
    )
    response_payload = json.loads(response.body)
    assert response_payload["status"] == "FAILED"
    assert response_payload["failureStage"] == "WORKER_RESULT"
    assert response_payload["error"]["code"] == "WORKER_RESULT_INVALID"


def _body(capability: str, *, stream: bool = False, operation: str = "TEST") -> bytes:
    payload = {
        "requestId": "req-1",
        "traceId": "trace-1",
        "callerDomain": "AI",
        "operation": operation,
        "capability": capability,
        "scope": "SANCAI",
        "modelConfig": {
            "serviceRole": "PRIMARY",
            "apiSource": "OPENAI_COMPATIBLE",
            "baseUrl": "https://model.example/v1",
            "apiKey": "process-only",
            "modelName": "model",
            "capabilityTags": ["text"],
            "parameters": {},
            "timeoutMs": 60000,
        },
        "prompt": {
            "messages": [
                {
                    "role": "system",
                    "content": "system prompt",
                },
                {
                    "role": "user",
                    "content": "user prompt",
                },
            ]
        },
        "input": {
            "contentType": "SANCAI_ENTRY",
            "payload": {"text": "hello"},
        },
        "outputSchema": {"type": "text"},
        "options": {"stream": stream, "forceJson": False, "locale": "zh-CN"},
    }
    return json.dumps(payload, ensure_ascii=False, separators=(",", ":")).encode()


def _text_payload_registry(payload: str) -> Any:
    return _TextPayloadRegistry(payload=payload)


class _TextPayloadRegistry:
    def __init__(self, payload: Any) -> None:
        self.payload = payload

    def invoke(self, request: AiInvokeRequest) -> dict[str, Any]:
        return {"format": "TEXT", "payload": self.payload}


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
