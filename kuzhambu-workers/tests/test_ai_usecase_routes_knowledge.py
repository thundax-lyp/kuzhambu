import json
from time import time

import pytest
from fastapi.testclient import TestClient

from kuzhambu_workers.ai.usecase_registry import USECASES, AiUsecaseDomain
from kuzhambu_workers.core.security import sign_request
from kuzhambu_workers.main import app
from kuzhambu_workers.schemas.ai import AiCapability

KNOWLEDGE_USECASES = tuple(
    usecase for usecase in USECASES if usecase.domain == AiUsecaseDomain.KNOWLEDGE
)


@pytest.mark.parametrize("usecase", KNOWLEDGE_USECASES)
def test_knowledge_usecase_routes_accept_matching_request(monkeypatch, usecase) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    body = _body(operation=usecase.operation, capability=usecase.capability.value)

    response = TestClient(app).post(
        usecase.path,
        content=body,
        headers=_headers(body, usecase.path),
    )

    assert response.status_code == 200
    payload = response.json()
    assert payload["status"] == "SUCCEEDED"
    assert payload["capability"] == usecase.capability.value
    assert payload["result"]["format"] == usecase.output.value
    expected_keys = _expected_payload_keys(usecase.capability)
    if expected_keys is not None:
        assert set(payload["result"]["payload"]) == expected_keys


def test_knowledge_usecase_route_rejects_capability_mismatch(monkeypatch) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    path = "/internal/ai/knowledge/relation-extraction"
    body = _body(
        operation="KNOWLEDGE_RELATION_EXTRACTION",
        capability=AiCapability.TAGS.value,
    )

    response = TestClient(app).post(path, content=body, headers=_headers(body, path))

    assert response.status_code == 400
    assert response.json()["status"] == "FAILED"
    assert response.json()["error"]["code"] == "BAD_REQUEST"


def _body(*, operation: str, capability: str) -> bytes:
    payload = {
        "requestId": "req-1",
        "traceId": "trace-1",
        "callerDomain": "AI",
        "operation": operation,
        "capability": capability,
        "scope": "knowledge",
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
        "input": {"contentType": "KNOWLEDGE_SNAPSHOT", "payload": {"text": "hello"}},
        "outputSchema": {"type": "structured"},
        "options": {"stream": False, "forceJson": False, "locale": "zh-CN"},
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


def _expected_payload_keys(capability: AiCapability) -> set[str] | None:
    if capability == AiCapability.RELATION_EXTRACTION:
        return {"entities", "relations", "sourceSnippets", "warnings"}
    if capability == AiCapability.KNOWLEDGE_GRAPH:
        return {"entities", "relations", "entryRefs", "warnings"}
    if capability == AiCapability.LINEAGE_EXTRACTION:
        return {"nodes", "relations", "sourceSnippets", "warnings"}
    return None
