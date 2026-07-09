import json
from hashlib import sha256
from time import time

import pytest
from fastapi.testclient import TestClient

from kuzhambu_workers.ai.image_generation import GeneratedImageArtifact
from kuzhambu_workers.ai.openai_compatible import (
    OpenAiChatCompletionChunk,
    OpenAiChatCompletionResult,
)
from kuzhambu_workers.ai.usecase_registry import USECASES, AiUsecaseDomain
from kuzhambu_workers.core.security import sign_request
from kuzhambu_workers.main import app
from kuzhambu_workers.schemas.ai import AiCapability
from kuzhambu_workers.schemas.common import UsageSummary

IMAGE_ANALYSIS_PATH = "/internal/ai/classics/sancai/image-analysis"
IMAGE_ANALYSIS_OPERATION = "CLASSICS_SANCAI_IMAGE_ANALYSIS"
FUSION_PATH = "/internal/ai/classics/sancai/fusion"
FUSION_OPERATION = "CLASSICS_SANCAI_FUSION"
IMAGE_GEN_PATH = "/internal/ai/classics/sancai/image-gen"
IMAGE_GEN_OPERATION = "CLASSICS_SANCAI_IMAGE_GEN"

CLASSICS_USECASES = tuple(
    usecase for usecase in USECASES if usecase.domain == AiUsecaseDomain.CLASSICS
)
PNG_1X1 = (
    b"\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR\x00\x00\x00\x01\x00\x00\x00\x01"
    b"\x08\x06\x00\x00\x00\x1f\x15\xc4\x89\x00\x00\x00\nIDATx\x9cc\xf8\x0f"
    b"\x00\x01\x01\x01\x00\x18\xdd\x8d\xb0\x00\x00\x00\x00IEND\xaeB`\x82"
)


@pytest.mark.parametrize("usecase", CLASSICS_USECASES)
def test_classics_usecase_routes_accept_matching_request(monkeypatch, tmp_path, usecase) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    monkeypatch.setenv("KUZHAMBU_WORKER_TEMP_DIR", str(tmp_path))
    monkeypatch.setattr(
        "kuzhambu_workers.ai.graphs.basic.invoke_chat_completion",
        lambda request: _model_result(_model_content(request.capability)),
    )
    monkeypatch.setattr(
        "kuzhambu_workers.ai.graphs.text.invoke_chat_completion",
        lambda request: _model_result("classics answer"),
    )
    monkeypatch.setattr(
        "kuzhambu_workers.api.ai_routes.iter_chat_completion_chunks",
        lambda request: iter(
            [
                OpenAiChatCompletionChunk(delta="classics", usage=None, finish_reason=None),
                OpenAiChatCompletionChunk(
                    delta="",
                    usage=UsageSummary(latencyMs=12, inputTokens=3, outputTokens=4),
                    finish_reason="stop",
                ),
            ]
        ),
    )
    monkeypatch.setattr(
        "kuzhambu_workers.ai.graphs.image_generation.generate_image",
        lambda request: _generated_image(),
    )
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
    if usecase.capability == AiCapability.IMAGE_GEN:
        events = _events(response.text)
        completed = next(event for event in events if event["event"] == "completed")
        assert "event: error" not in response.text
        assert completed["result"] is None
        assert completed["extra"]["status"] == "SUCCEEDED"
        assert completed["extra"]["artifactReference"]["contentType"] == "image/png"
        return
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
    monkeypatch.setattr(
        "kuzhambu_workers.api.ai_routes.iter_chat_completion_chunks",
        lambda request: iter(
            [
                OpenAiChatCompletionChunk(delta="markdown", usage=None, finish_reason=None),
                OpenAiChatCompletionChunk(
                    delta="",
                    usage=UsageSummary(latencyMs=12, inputTokens=3, outputTokens=4),
                    finish_reason="stop",
                ),
            ]
        ),
    )
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


def test_classics_image_gen_route_contract_is_stable(monkeypatch, tmp_path) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    monkeypatch.setenv("KUZHAMBU_WORKER_TEMP_DIR", str(tmp_path))
    monkeypatch.setattr(
        "kuzhambu_workers.ai.graphs.image_generation.generate_image",
        lambda request: _generated_image(),
    )
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
    assert "event: error" not in response.text
    events = _events(response.text)
    completed = next(event for event in events if event["event"] == "completed")
    artifact = completed["extra"]["artifactReference"]
    assert completed["result"] is None
    assert completed["extra"]["status"] == "SUCCEEDED"
    assert completed["extra"]["failureStage"] is None
    assert completed["extra"]["fallbackUsed"] is False
    assert artifact["contentType"] == "image/png"
    assert artifact["filename"] == "sancai-image.png"
    assert artifact["sizeBytes"] == len(PNG_1X1)
    assert artifact["sha256"] == f"sha256:{sha256(PNG_1X1).hexdigest()}"
    assert '"fallbackUsed":false' in response.text

    download_response = TestClient(app).get(
        artifact["downloadPath"],
        headers=_headers(b"", artifact["downloadPath"], service="kuzhambu-ai", method="GET"),
    )

    assert download_response.status_code == 200
    assert download_response.content == PNG_1X1
    assert download_response.headers["content-type"].startswith("image/png")
    assert download_response.headers["X-Kuzhambu-Artifact-Id"] == artifact["artifactId"]
    assert download_response.headers["X-Kuzhambu-Artifact-Sha256"] == artifact["sha256"]


def test_classics_fusion_route_contract_is_stable(monkeypatch) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    monkeypatch.setattr(
        "kuzhambu_workers.ai.graphs.basic.invoke_chat_completion",
        lambda request: _model_result("fusion markdown"),
    )
    body = _body(
        operation=FUSION_OPERATION,
        capability=AiCapability.FUSION.value,
        stream=False,
    )

    response = TestClient(app).post(
        FUSION_PATH,
        content=body,
        headers=_headers(body, FUSION_PATH, service="kuzhambu-ai"),
    )

    assert response.status_code == 200
    payload = response.json()
    assert payload["status"] == "SUCCEEDED"
    assert payload["capability"] == AiCapability.FUSION.value
    assert payload["failureStage"] is None
    assert payload["fallbackUsed"] is False
    assert payload["artifactReference"] is None
    assert payload["result"]["format"] == "MARKDOWN"


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
    output_type = "artifact" if capability == AiCapability.IMAGE_GEN.value else "text"
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
        "outputSchema": {"type": output_type},
        "options": {"stream": stream, "forceJson": False, "locale": "zh-CN"},
    }
    return json.dumps(payload, ensure_ascii=False, separators=(",", ":")).encode()


def _headers(
    body: bytes,
    path: str,
    *,
    service: str,
    method: str = "POST",
) -> dict[str, str]:
    timestamp = str(int(time() * 1000))
    signature = sign_request(method, path, timestamp, "req-1", body, "worker-secret")
    return {
        "X-Kuzhambu-Service": service,
        "X-Kuzhambu-Request-Id": "req-1",
        "X-Kuzhambu-Trace-Id": "trace-1",
        "X-Kuzhambu-Timestamp": timestamp,
        "X-Kuzhambu-Signature": signature,
    }


def _events(text: str) -> list[dict]:
    events = []
    event_name = ""
    for line in text.splitlines():
        if line.startswith("event: "):
            event_name = line.removeprefix("event: ")
        if line.startswith("data: "):
            event = json.loads(line.removeprefix("data: "))
            event["event"] = event_name
            events.append(event)
    return events


def _model_result(content: str) -> OpenAiChatCompletionResult:
    return OpenAiChatCompletionResult(
        content=content,
        usage=UsageSummary(latencyMs=12, inputTokens=3, outputTokens=4),
        raw_finish_reason="stop",
    )


def _model_content(capability: AiCapability) -> str:
    if capability in {
        AiCapability.TAGS,
        AiCapability.QA,
        AiCapability.SPLIT,
    }:
        return '{"items":[]}'
    return "classics answer"


def _generated_image() -> GeneratedImageArtifact:
    return GeneratedImageArtifact(
        data=PNG_1X1,
        content_type="image/png",
        filename="sancai-image.png",
        usage=UsageSummary(latencyMs=12, inputTokens=3, outputTokens=4),
    )
