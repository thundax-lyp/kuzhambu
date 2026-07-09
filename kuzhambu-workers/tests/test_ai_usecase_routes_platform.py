import json
from time import time

import pytest
from fastapi.testclient import TestClient

from kuzhambu_workers.ai.openai_compatible import OpenAiChatCompletionResult
from kuzhambu_workers.ai.usecase_registry import USECASES, AiUsecaseDomain
from kuzhambu_workers.core.security import sign_request
from kuzhambu_workers.main import app
from kuzhambu_workers.schemas.ai import AiCapability
from kuzhambu_workers.schemas.common import UsageSummary

PLATFORM_USECASES = tuple(
    usecase for usecase in USECASES if usecase.domain == AiUsecaseDomain.PLATFORM
)


@pytest.mark.parametrize("usecase", PLATFORM_USECASES)
def test_platform_usecase_routes_accept_matching_request(monkeypatch, usecase) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    monkeypatch.setattr(
        "kuzhambu_workers.ai.graphs.basic.invoke_chat_completion",
        lambda request: _model_result(_model_content(request.capability)),
    )
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


def test_platform_usecase_route_rejects_capability_mismatch(monkeypatch) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    path = "/internal/ai/platform/version-summary"
    body = _body(
        operation="PLATFORM_VERSION_SUMMARY",
        capability=AiCapability.PROMPT_SUGGESTION.value,
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
        "scope": "platform",
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
        "input": {"contentType": "AI_PLATFORM_SNAPSHOT", "payload": {"text": "hello"}},
        "outputSchema": {"type": "text"},
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


def _model_content(capability: AiCapability) -> str:
    if capability == AiCapability.PROMPT_SUGGESTION:
        return '{"suggestions":[]}'
    return "platform answer"


def _model_result(content: str) -> OpenAiChatCompletionResult:
    return OpenAiChatCompletionResult(
        content=content,
        usage=UsageSummary(latencyMs=12, inputTokens=3, outputTokens=4),
        raw_finish_reason="stop",
    )
