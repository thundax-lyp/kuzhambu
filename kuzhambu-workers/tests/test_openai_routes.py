import json
from base64 import b64encode
from time import time

from fastapi.testclient import TestClient

from kuzhambu_workers.ai.image_generation import GeneratedImageArtifact
from kuzhambu_workers.ai.openai_compatible import (
    OpenAiChatCompletionChunk,
    OpenAiChatCompletionResult,
)
from kuzhambu_workers.core.security import sign_request
from kuzhambu_workers.main import app
from kuzhambu_workers.schemas.common import UsageSummary

_PATH = "/internal/openai/v1/chat-completions"
_IMAGE_PATH = "/internal/openai/v1/images/generations"
PNG_1X1 = (
    b"\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR\x00\x00\x00\x01\x00\x00\x00\x01"
    b"\x08\x06\x00\x00\x00\x1f\x15\xc4\x89\x00\x00\x00\rIDATx\x9cc\xf8"
    b"\xff\xff?\x00\x05\xfe\x02\xfeA\xe2i\xb3\x00\x00\x00\x00IEND\xaeB`\x82"
)


def test_openai_compatible_chat_completion_invokes_provider(monkeypatch) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    captured: dict[str, object] = {}

    def fake_invoke(request, *, client=None, response_format=None):
        captured["model_config"] = request.modelConfig.model_dump(mode="json")
        captured["messages"] = [
            message.model_dump(mode="json") for message in request.prompt.messages
        ]
        captured["parameters"] = request.modelConfig.parameters
        captured["response_format"] = response_format
        return OpenAiChatCompletionResult(
            content="answer",
            usage=UsageSummary(inputTokens=3, outputTokens=5, latencyMs=8),
            raw_finish_reason="stop",
        )

    monkeypatch.setattr("kuzhambu_workers.api.openai_routes.invoke_chat_completion", fake_invoke)
    body = _body(stream=False, extra={"temperature": 0.2})

    response = TestClient(app).post(
        _PATH, content=body, headers=_headers(body, service="kuzhambu-ai")
    )

    assert response.status_code == 200
    payload = response.json()
    assert payload["id"] == "req-openai-1"
    assert payload["object"] == "chat.completion"
    assert payload["model"] == "gpt-4o-mini"
    assert payload["choices"][0]["message"] == {"role": "assistant", "content": "answer"}
    assert payload["choices"][0]["finish_reason"] == "stop"
    assert payload["usage"] == {
        "prompt_tokens": 3,
        "completion_tokens": 5,
        "total_tokens": 8,
    }
    assert captured["model_config"]["serviceRole"] == "OPENAI"
    assert captured["model_config"]["apiSource"] == "OPENAI_COMPATIBLE"
    assert captured["model_config"]["baseUrl"] == "https://provider.example/v1"
    assert captured["model_config"]["modelName"] == "gpt-4o-mini"
    assert captured["parameters"] == {"temperature": 0.2}
    assert captured["messages"] == [
        {"role": "system", "content": "system prompt"},
        {"role": "user", "content": "user prompt"},
    ]


def test_openai_compatible_chat_completion_streams_openai_chunks(monkeypatch) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")

    def fake_chunks(request):
        assert request.options.stream is True
        return iter(
            [
                OpenAiChatCompletionChunk(delta="hel", usage=None, finish_reason=None),
                OpenAiChatCompletionChunk(delta="lo", usage=None, finish_reason=None),
                OpenAiChatCompletionChunk(delta="", usage=None, finish_reason="stop"),
            ]
        )

    monkeypatch.setattr(
        "kuzhambu_workers.api.openai_routes.iter_chat_completion_chunks", fake_chunks
    )
    body = _body(stream=True)

    response = TestClient(app).post(
        _PATH, content=body, headers=_headers(body, service="kuzhambu-ai")
    )

    assert response.status_code == 200
    assert "data: " in response.text
    assert '"object":"chat.completion.chunk"' in response.text
    assert '"content":"hel"' in response.text
    assert '"content":"lo"' in response.text
    assert '"finish_reason":"stop"' in response.text
    assert "data: [DONE]" in response.text


def test_openai_compatible_image_generation_invokes_bytedance_text2image(monkeypatch) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    captured: dict[str, object] = {}

    def fake_generate_image(request):
        captured["model_config"] = request.modelConfig.model_dump(mode="json")
        captured["prompt"] = [
            message.model_dump(mode="json") for message in request.prompt.messages
        ]
        return GeneratedImageArtifact(
            data=PNG_1X1,
            content_type="image/png",
            filename="image.png",
            usage=UsageSummary(inputTokens=1, outputTokens=1, latencyMs=12),
        )

    monkeypatch.setattr("kuzhambu_workers.api.openai_routes.generate_image", fake_generate_image)
    body = _image_body()

    response = TestClient(app).post(
        _IMAGE_PATH,
        content=body,
        headers=_headers(body, service="kuzhambu-ai", path=_IMAGE_PATH),
    )

    assert response.status_code == 200
    payload = response.json()
    assert payload["data"] == [{"b64_json": b64encode(PNG_1X1).decode("ascii"), "url": None}]
    assert captured["model_config"]["serviceRole"] == "BYTEDANCE"
    assert captured["model_config"]["apiSource"] == "OPENAI_COMPATIBLE"
    assert captured["model_config"]["baseUrl"] == "https://ark.example/api/v3"
    assert captured["model_config"]["apiKey"] == "ark-key"
    assert captured["model_config"]["modelName"] == "doubao-seedream"
    assert captured["model_config"]["parameters"]["size"] == "2K"
    assert captured["prompt"] == [{"role": "user", "content": "draw a cup"}]


def test_openai_compatible_chat_completion_rejects_disallowed_service(monkeypatch) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-classics")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    body = _body(stream=False)

    response = TestClient(app).post(
        _PATH,
        content=body,
        headers=_headers(body, service="kuzhambu-classics"),
    )

    assert response.status_code == 403
    assert response.json()["error"]["code"] == "PATH_FORBIDDEN"


def _body(*, stream: bool, extra: dict[str, object] | None = None) -> bytes:
    payload = {
        "requestId": "req-openai-1",
        "traceId": "trace-openai-1",
        "model": "OPENAI/gpt-4o-mini",
        "messages": [
            {"role": "system", "content": "system prompt"},
            {"role": "user", "content": "user prompt"},
        ],
        "stream": stream,
        "extendParams": {
            "baseUrl": "https://provider.example/v1",
            "apiKey": "process-only",
            "capabilityTags": ["text", "streaming_text"],
        },
    }
    if extra is not None:
        payload.update(extra)
    return json.dumps(payload, ensure_ascii=False, separators=(",", ":")).encode()


def _image_body() -> bytes:
    payload = {
        "requestId": "req-openai-1",
        "traceId": "trace-openai-1",
        "model": "BYTEDANCE/doubao-seedream",
        "prompt": "draw a cup",
        "response_format": "b64_json",
        "extendParams": {
            "baseUrl": "https://ark.example/api/v3",
            "apiKey": "ark-key",
            "size": "2K",
        },
    }
    return json.dumps(payload, ensure_ascii=False, separators=(",", ":")).encode()


def _headers(body: bytes, *, service: str, path: str = _PATH) -> dict[str, str]:
    timestamp = str(int(time() * 1000))
    signature = sign_request("POST", path, timestamp, "req-openai-1", body, "worker-secret")
    return {
        "X-Kuzhambu-Service": service,
        "X-Kuzhambu-Request-Id": "req-openai-1",
        "X-Kuzhambu-Trace-Id": "trace-openai-1",
        "X-Kuzhambu-Timestamp": timestamp,
        "X-Kuzhambu-Signature": signature,
    }
