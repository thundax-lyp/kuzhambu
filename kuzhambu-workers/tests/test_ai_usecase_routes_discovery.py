import json
from time import time

import pytest
from fastapi.testclient import TestClient

from kuzhambu_workers.ai.openai_compatible import (
    OpenAiChatCompletionChunk,
    OpenAiChatCompletionResult,
)
from kuzhambu_workers.ai.usecase_registry import USECASES, AiUsecaseDomain
from kuzhambu_workers.core.security import sign_request
from kuzhambu_workers.main import app
from kuzhambu_workers.schemas.ai import AiCapability
from kuzhambu_workers.schemas.common import UsageSummary

DISCOVERY_USECASES = tuple(
    usecase for usecase in USECASES if usecase.domain == AiUsecaseDomain.DISCOVERY
)
DISCOVERY_QA_FORBIDDEN_PATH_PARTS = (
    "/internal/ai/discovery/qa/session",
    "/internal/discovery/qa",
    "/internal/qa/discovery",
    "chat/completions",
    "question/ask",
    "knowledge/sync",
    "knowledge-sync",
)


@pytest.mark.parametrize("usecase", DISCOVERY_USECASES)
def test_discovery_usecase_routes_accept_matching_request(monkeypatch, usecase) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    monkeypatch.setattr(
        "kuzhambu_workers.ai.graphs.basic.invoke_chat_completion",
        lambda request: _model_result(_model_content(request.capability)),
    )
    monkeypatch.setattr(
        "kuzhambu_workers.api.ai_routes.iter_chat_completion_chunks",
        lambda request: iter(
            [
                OpenAiChatCompletionChunk(delta="answer", usage=None, finish_reason=None),
                OpenAiChatCompletionChunk(
                    delta="",
                    usage=UsageSummary(latencyMs=12, inputTokens=3, outputTokens=4),
                    finish_reason="stop",
                ),
            ]
        ),
    )
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
        assert "event: delta" in response.text
        assert "event: usage" in response.text
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


def test_discovery_answer_path_accepts_single_document_context(monkeypatch) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    monkeypatch.setattr(
        "kuzhambu_workers.ai.graphs.basic.invoke_chat_completion",
        lambda request: _model_result("answer"),
    )
    path = "/internal/ai/discovery/answer-generation"
    body = _body(
        operation="DISCOVERY_ANSWER_GENERATION",
        capability=AiCapability.ANSWER_GENERATION.value,
        stream=False,
        input_payload={
            "query": "这篇文档讲什么?",
            "contextMode": "SINGLE_DOCUMENT",
            "contextContentType": "WANGQI_DOCUMENT",
            "contextContentId": "10001",
        },
    )

    response = TestClient(app).post(path, content=body, headers=_headers(body, path))

    assert response.status_code == 200
    payload = response.json()
    assert payload["status"] == "SUCCEEDED"
    assert payload["capability"] == AiCapability.ANSWER_GENERATION.value


def test_discovery_usecase_routes_do_not_expose_formal_qa_or_sync_runtime() -> None:
    paths = {usecase.path for usecase in DISCOVERY_USECASES}
    openapi_paths = set(TestClient(app).app.openapi()["paths"])

    assert paths == {
        "/internal/ai/discovery/query-understanding",
        "/internal/ai/discovery/query-rewrite",
        "/internal/ai/discovery/answer-generation",
        "/internal/ai/discovery/answer-generation/stream",
    }
    for path in paths:
        assert not any(part in path for part in DISCOVERY_QA_FORBIDDEN_PATH_PARTS)
    assert not any(path.startswith("/internal/ai/discovery/qa/session") for path in openapi_paths)


def _body(
    *,
    operation: str,
    capability: str,
    stream: bool,
    input_payload: dict[str, str] | None = None,
) -> bytes:
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
        "input": {
            "contentType": "DISCOVERY_QUERY",
            "payload": input_payload or {"query": "hello"},
        },
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


def _model_content(capability: AiCapability) -> str:
    if capability == AiCapability.QUERY_UNDERSTANDING:
        return '{"items":[]}'
    return "answer"


def _model_result(content: str) -> OpenAiChatCompletionResult:
    return OpenAiChatCompletionResult(
        content=content,
        usage=UsageSummary(latencyMs=12, inputTokens=3, outputTokens=4),
        raw_finish_reason="stop",
    )
