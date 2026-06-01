import json
from time import time

import pytest
from fastapi.testclient import TestClient

from kuzhambu_workers.ai.usecase_registry import USECASES, AiUsecaseDomain
from kuzhambu_workers.core.security import sign_request
from kuzhambu_workers.main import app
from kuzhambu_workers.schemas.ai import AiCapability

DISCOVERY_USECASES = tuple(
    usecase for usecase in USECASES if usecase.domain == AiUsecaseDomain.DISCOVERY
)


@pytest.mark.parametrize("usecase", DISCOVERY_USECASES)
def test_discovery_usecase_routes_accept_matching_request(monkeypatch, usecase) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    body = _body(
        operation=usecase.operation,
        capability=usecase.capability.value,
        stream=usecase.stream,
    )

    response = TestClient(app).post(
        usecase.path,
        content=body,
        headers=_headers(body, usecase.path),
    )

    assert response.status_code == 200
    if usecase.stream:
        assert response.headers["content-type"].startswith("text/event-stream")
        assert "event: started" in response.text
        assert "event: completed" in response.text
        assert f'"format":"{usecase.output.value}"' in response.text
    else:
        payload = response.json()
        assert payload["status"] == "SUCCEEDED"
        assert payload["capability"] == usecase.capability.value
        assert payload["result"]["format"] == usecase.output.value


def test_discovery_stream_path_rejects_non_stream_request(monkeypatch) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    path = "/internal/ai/discovery/answer-generation/stream"
    body = _body(
        operation="DISCOVERY_ANSWER_GENERATION_STREAM",
        capability=AiCapability.ANSWER_GENERATION.value,
        stream=False,
    )

    response = TestClient(app).post(path, content=body, headers=_headers(body, path))

    assert response.status_code == 400
    assert response.json()["status"] == "FAILED"
    assert response.json()["error"]["code"] == "BAD_REQUEST"


def test_discovery_answer_path_rejects_stream_request(monkeypatch) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    path = "/internal/ai/discovery/answer-generation"
    body = _body(
        operation="DISCOVERY_ANSWER_GENERATION",
        capability=AiCapability.ANSWER_GENERATION.value,
        stream=True,
    )

    response = TestClient(app).post(path, content=body, headers=_headers(body, path))

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
        "scope": "discovery",
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
        "input": {"contentType": "DISCOVERY_QUERY", "payload": {"query": "hello"}},
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
