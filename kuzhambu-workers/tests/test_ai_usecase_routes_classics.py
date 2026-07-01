import json
from time import time

import pytest
from fastapi.testclient import TestClient

from kuzhambu_workers.ai.usecase_registry import USECASES, AiUsecaseDomain
from kuzhambu_workers.core.security import sign_request
from kuzhambu_workers.main import app
from kuzhambu_workers.schemas.ai import AiCapability

IMAGE_ANALYSIS_PATH = "/internal/ai/classics/sancai/image-analysis"
IMAGE_ANALYSIS_OPERATION = "CLASSICS_SANCAI_IMAGE_ANALYSIS"
IMAGE_GEN_PATH = "/internal/ai/classics/sancai/image-gen"
IMAGE_GEN_OPERATION = "CLASSICS_SANCAI_IMAGE_GEN"

CLASSICS_USECASES = tuple(
    usecase for usecase in USECASES if usecase.domain == AiUsecaseDomain.CLASSICS
)


@pytest.mark.parametrize("usecase", CLASSICS_USECASES)
def test_classics_usecase_routes_accept_matching_request(monkeypatch, usecase) -> None:
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
        headers=_headers(body, usecase.path, service="kuzhambu-ai"),
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


def test_classics_usecase_route_rejects_capability_mismatch(monkeypatch) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    path = "/internal/ai/classics/sancai/translate"
    body = _body(
        operation="CLASSICS_SANCAI_TRANSLATE",
        capability=AiCapability.SUMMARY.value,
        stream=False,
    )

    response = TestClient(app).post(
        path,
        content=body,
        headers=_headers(body, path, service="kuzhambu-ai"),
    )

    assert response.status_code == 400
    assert response.json()["status"] == "FAILED"
    assert response.json()["error"]["code"] == "BAD_REQUEST"


def test_classics_image_analysis_route_contract_is_stable(monkeypatch) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    body = _body(
        operation=IMAGE_ANALYSIS_OPERATION,
        capability=AiCapability.IMAGE_ANALYSIS.value,
        stream=True,
    )

    response = TestClient(app).post(
        IMAGE_ANALYSIS_PATH,
        content=body,
        headers=_headers(body, IMAGE_ANALYSIS_PATH, service="kuzhambu-ai"),
    )

    assert response.status_code == 200
    assert response.headers["content-type"].startswith("text/event-stream")
    assert "event: started" in response.text
    assert "event: completed" in response.text
    assert '"format":"MARKDOWN"' in response.text


def test_classics_image_gen_route_contract_is_stable(monkeypatch) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    body = _body(
        operation=IMAGE_GEN_OPERATION,
        capability=AiCapability.IMAGE_GEN.value,
        stream=True,
    )

    response = TestClient(app).post(
        IMAGE_GEN_PATH,
        content=body,
        headers=_headers(body, IMAGE_GEN_PATH, service="kuzhambu-ai"),
    )

    assert response.status_code == 200
    assert response.headers["content-type"].startswith("text/event-stream")
    assert "event: started" in response.text
    assert "event: completed" in response.text
    assert '"format":"ARTIFACT"' in response.text
    assert '"artifactType":"IMAGE"' in response.text
    assert '"encoding":"SSE_ARTIFACT_CHUNK"' in response.text


def test_classics_usecase_route_rejects_stream_mismatch(monkeypatch) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    path = "/internal/ai/classics/sancai/image-analysis"
    body = _body(
        operation="CLASSICS_SANCAI_IMAGE_ANALYSIS",
        capability=AiCapability.IMAGE_ANALYSIS.value,
        stream=False,
    )

    response = TestClient(app).post(
        path,
        content=body,
        headers=_headers(body, path, service="kuzhambu-ai"),
    )

    assert response.status_code == 400
    assert response.json()["status"] == "FAILED"
    assert response.json()["error"]["code"] == "BAD_REQUEST"


def test_classics_usecase_route_rejects_business_service_identity(monkeypatch) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai,kuzhambu-classics")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    path = "/internal/ai/classics/sancai/translate"
    body = _body(
        operation="CLASSICS_SANCAI_TRANSLATE",
        capability=AiCapability.TRANSLATE.value,
        stream=False,
    )

    response = TestClient(app).post(
        path,
        content=body,
        headers=_headers(body, path, service="kuzhambu-classics"),
    )

    assert response.status_code == 403
    assert response.json()["error"]["code"] == "PATH_FORBIDDEN"


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
