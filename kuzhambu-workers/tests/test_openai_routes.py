import json
from pathlib import Path
from re import DOTALL, search
from time import time
from typing import Any

from fastapi.testclient import TestClient

from kuzhambu_workers.ai.openai_compatible import OpenAiChatCompletionChunk
from kuzhambu_workers.core.security import sign_request
from kuzhambu_workers.main import app
from kuzhambu_workers.schemas.common import UsageSummary

_PATH = "/internal/openai/v1/chat-completions"
_IMAGE_PATH = "/internal/openai/v1/images/generations"
_REPO_ROOT = Path(__file__).resolve().parents[2]
_IMAGE_GENERATION_META = (
    _REPO_ROOT / "db/data-source/ai-prompts/classics/image-generation/meta.json"
)
_IMAGE_GENERATION_SAMPLE = (
    _REPO_ROOT / "db/data-source/ai-prompts/classics/image-generation/sample.md"
)
PNG_1X1 = (
    b"\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR\x00\x00\x00\x01\x00\x00\x00\x01"
    b"\x08\x06\x00\x00\x00\x1f\x15\xc4\x89\x00\x00\x00\rIDATx\x9cc\xf8"
    b"\xff\xff?\x00\x05\xfe\x02\xfeA\xe2i\xb3\x00\x00\x00\x00IEND\xaeB`\x82"
)


def test_openai_compatible_chat_completion_invokes_graph(monkeypatch) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    captured: dict[str, Any] = {}

    class FakeRegistry:
        def invoke(self, request):
            captured["model_config"] = request.modelConfig.model_dump(mode="json")
            captured["messages"] = [
                message.model_dump(mode="json") for message in request.prompt.messages
            ]
            captured["parameters"] = request.modelConfig.parameters
            captured["response_format"] = request.input.payload.get("responseFormat")
            return {
                "format": "TEXT",
                "payload": "answer",
                "usage": {"inputTokens": 3, "outputTokens": 5, "latencyMs": 8},
                "rawFinishReason": "stop",
            }

    monkeypatch.setattr("kuzhambu_workers.api.openai_routes._REGISTRY", FakeRegistry())
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
    assert captured["response_format"] is None
    assert captured["messages"] == [
        {"role": "system", "content": "system prompt"},
        {"role": "user", "content": "user prompt"},
    ]


def test_openai_compatible_chat_completion_preserves_multipart_vision_message(
    monkeypatch,
) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    captured: dict[str, Any] = {}

    class FakeRegistry:
        def invoke(self, request):
            captured["messages"] = [
                message.model_dump(mode="json") for message in request.prompt.messages
            ]
            return {
                "format": "TEXT",
                "payload": "vision answer",
                "usage": {},
                "rawFinishReason": "stop",
            }

    monkeypatch.setattr("kuzhambu_workers.api.openai_routes._REGISTRY", FakeRegistry())
    vision_content = [
        {"type": "text", "text": "describe this image"},
        {"type": "image_url", "image_url": {"url": "https://image.example/cup.png"}},
    ]
    body = _body(
        stream=False,
        extra={
            "model": "BYTEDANCE/doubao-vision",
            "messages": [{"role": "user", "content": vision_content}],
            "extendParams": {
                "baseUrl": "https://ark.example/api/v3",
                "apiKey": "ark-key",
                "capabilityTags": ["vision"],
            },
        },
    )

    response = TestClient(app).post(
        _PATH, content=body, headers=_headers(body, service="kuzhambu-ai")
    )

    assert response.status_code == 200
    assert captured["messages"] == [{"role": "user", "content": vision_content}]


def test_openai_compatible_chat_completion_passes_response_format_to_graph(
    monkeypatch,
) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    captured: dict[str, Any] = {}

    class FakeRegistry:
        def invoke(self, request):
            captured["response_format"] = request.input.payload.get("responseFormat")
            return {"format": "TEXT", "payload": "answer", "usage": {}}

    monkeypatch.setattr("kuzhambu_workers.api.openai_routes._REGISTRY", FakeRegistry())
    response_format = _json_schema_response_format()
    body = _body(stream=False, extra={"response_format": response_format})

    response = TestClient(app).post(
        _PATH, content=body, headers=_headers(body, service="kuzhambu-ai")
    )

    assert response.status_code == 200
    assert captured["response_format"] == response_format


def test_openai_compatible_chat_completion_streams_response_format(monkeypatch) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    captured: dict[str, Any] = {}
    response_format = _json_schema_response_format()

    class FakeRegistry:
        def stream_chat_completion(self, request, *, response_format=None):
            captured["response_format"] = response_format
            return iter([OpenAiChatCompletionChunk(delta="", usage=None, finish_reason="stop")])

    monkeypatch.setattr("kuzhambu_workers.api.openai_routes._REGISTRY", FakeRegistry())
    body = _body(stream=True, extra={"response_format": response_format})

    response = TestClient(app).post(
        _PATH, content=body, headers=_headers(body, service="kuzhambu-ai")
    )

    assert response.status_code == 200
    assert captured["response_format"] == response_format


def test_openai_compatible_chat_completion_streams_openai_chunks(monkeypatch) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")

    class FakeRegistry:
        def stream_chat_completion(self, request, *, response_format=None):
            assert request.options.stream is True
            assert response_format is None
            return iter(
                [
                    OpenAiChatCompletionChunk(delta="hel", usage=None, finish_reason=None),
                    OpenAiChatCompletionChunk(delta="lo", usage=None, finish_reason=None),
                    OpenAiChatCompletionChunk(delta="", usage=None, finish_reason="stop"),
                ]
            )

    monkeypatch.setattr("kuzhambu_workers.api.openai_routes._REGISTRY", FakeRegistry())
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


def test_openai_compatible_chat_completion_streams_usage_chunk(monkeypatch) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")

    class FakeRegistry:
        def stream_chat_completion(self, request, *, response_format=None):
            return iter(
                [
                    OpenAiChatCompletionChunk(
                        delta="",
                        usage=_usage(3, 5),
                        finish_reason=None,
                        provider_usage=True,
                    )
                ]
            )

    monkeypatch.setattr("kuzhambu_workers.api.openai_routes._REGISTRY", FakeRegistry())
    body = _body(stream=True, extra={"stream_options": {"include_usage": True}})

    response = TestClient(app).post(
        _PATH, content=body, headers=_headers(body, service="kuzhambu-ai")
    )

    assert response.status_code == 200
    assert '"choices":[]' in response.text
    assert '"usage":{"prompt_tokens":3,"completion_tokens":5,"total_tokens":8}' in response.text
    assert "data: [DONE]" in response.text


def test_openai_compatible_chat_completion_omits_synthetic_stream_usage(
    monkeypatch,
) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")

    class FakeRegistry:
        def stream_chat_completion(self, request, *, response_format=None):
            return iter(
                [
                    OpenAiChatCompletionChunk(
                        delta="answer",
                        usage=None,
                        finish_reason="stop",
                    ),
                    OpenAiChatCompletionChunk(
                        delta="",
                        usage=_usage(0, 0),
                        finish_reason=None,
                        provider_usage=False,
                    ),
                ]
            )

    monkeypatch.setattr("kuzhambu_workers.api.openai_routes._REGISTRY", FakeRegistry())
    body = _body(stream=True, extra={"stream_options": {"include_usage": True}})

    response = TestClient(app).post(
        _PATH, content=body, headers=_headers(body, service="kuzhambu-ai")
    )

    assert response.status_code == 200
    assert '"content":"answer"' in response.text
    assert '"usage"' not in response.text
    assert "data: [DONE]" in response.text


def test_openai_compatible_chat_completion_rejects_multiple_choices_before_graph(
    monkeypatch,
) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    captured = {"called": False}

    class FakeRegistry:
        def invoke(self, request):
            captured["called"] = True
            return {}

    monkeypatch.setattr("kuzhambu_workers.api.openai_routes._REGISTRY", FakeRegistry())
    body = _body(stream=False, extra={"n": 2})

    response = TestClient(app).post(
        _PATH, content=body, headers=_headers(body, service="kuzhambu-ai")
    )

    assert response.status_code == 400
    assert captured["called"] is False
    assert response.json()["error"]["code"] == "MODEL_CONFIG_INVALID"


def test_openai_compatible_image_generation_rejects_invalid_format_before_graph(
    monkeypatch,
) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    captured = {"called": False}

    class FakeRegistry:
        def invoke(self, request):
            captured["called"] = True
            return {}

    monkeypatch.setattr("kuzhambu_workers.api.openai_routes._REGISTRY", FakeRegistry())
    body = _image_body(extra={"response_format": "bad"})

    response = TestClient(app).post(
        _IMAGE_PATH,
        content=body,
        headers=_headers(body, service="kuzhambu-ai", path=_IMAGE_PATH),
    )

    assert response.status_code == 400
    assert captured["called"] is False
    assert response.json()["error"]["code"] == "MODEL_CONFIG_INVALID"


def test_openai_compatible_image_generation_rejects_effective_extend_count_before_graph(
    monkeypatch,
) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    captured = {"called": False}

    class FakeRegistry:
        def invoke(self, request):
            captured["called"] = True
            return {}

    monkeypatch.setattr("kuzhambu_workers.api.openai_routes._REGISTRY", FakeRegistry())
    body = _image_body(extra={"n": 1, "extendParams": {"n": 2}})

    response = TestClient(app).post(
        _IMAGE_PATH,
        content=body,
        headers=_headers(body, service="kuzhambu-ai", path=_IMAGE_PATH),
    )

    assert response.status_code == 400
    assert captured["called"] is False
    assert response.json()["error"]["code"] == "MODEL_CONFIG_INVALID"


def test_openai_compatible_image_generation_rejects_stream_before_graph(
    monkeypatch,
) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    captured = {"called": False}

    class FakeRegistry:
        def invoke(self, request):
            captured["called"] = True
            return {}

    monkeypatch.setattr("kuzhambu_workers.api.openai_routes._REGISTRY", FakeRegistry())
    body = _image_body(extra={"extendParams": {"stream": True}})

    response = TestClient(app).post(
        _IMAGE_PATH,
        content=body,
        headers=_headers(body, service="kuzhambu-ai", path=_IMAGE_PATH),
    )

    assert response.status_code == 400
    assert captured["called"] is False
    assert response.json()["error"]["code"] == "MODEL_CONFIG_INVALID"


def test_openai_compatible_image_generation_rejects_graph_routing_override(
    monkeypatch,
) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    captured = {"called": False}

    class FakeRegistry:
        def invoke(self, request):
            captured["called"] = True
            return {}

    monkeypatch.setattr("kuzhambu_workers.api.openai_routes._REGISTRY", FakeRegistry())
    body = _image_body(
        extra={
            "capability": "summary",
            "operation": "OPENAI_COMPATIBLE_CHAT_COMPLETION",
        }
    )

    response = TestClient(app).post(
        _IMAGE_PATH,
        content=body,
        headers=_headers(body, service="kuzhambu-ai", path=_IMAGE_PATH),
    )

    assert response.status_code == 400
    assert captured["called"] is False
    assert response.json()["error"]["code"] == "MODEL_CONFIG_INVALID"


def test_openai_compatible_image_generation_rejects_multiple_images_before_graph(
    monkeypatch,
) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    captured = {"called": False}

    class FakeRegistry:
        def invoke(self, request):
            captured["called"] = True
            return {}

    monkeypatch.setattr("kuzhambu_workers.api.openai_routes._REGISTRY", FakeRegistry())
    body = _image_body(extra={"n": 2})

    response = TestClient(app).post(
        _IMAGE_PATH,
        content=body,
        headers=_headers(body, service="kuzhambu-ai", path=_IMAGE_PATH),
    )

    assert response.status_code == 400
    assert captured["called"] is False
    assert response.json()["error"]["code"] == "MODEL_CONFIG_INVALID"


def test_openai_compatible_image_generation_rejects_oversized_artifact(
    monkeypatch,
    tmp_path,
) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    monkeypatch.setenv("KUZHAMBU_WORKER_TEMP_DIR", str(tmp_path))
    monkeypatch.setenv("KUZHAMBU_WORKER_MAX_ARTIFACT_BYTES", "1")

    class FakeRegistry:
        def invoke(self, request):
            return {
                "format": "ARTIFACT",
                "payload": {
                    "data": PNG_1X1,
                    "contentType": "image/png",
                    "filename": "image.png",
                },
                "usage": {},
            }

    monkeypatch.setattr("kuzhambu_workers.api.openai_routes._REGISTRY", FakeRegistry())
    body = _image_body()

    response = TestClient(app).post(
        _IMAGE_PATH,
        content=body,
        headers=_headers(body, service="kuzhambu-ai", path=_IMAGE_PATH),
    )

    assert response.status_code == 502
    assert response.json()["error"]["code"] == "IMAGE_ARTIFACT_TOO_LARGE"
    assert not (tmp_path / "artifacts").exists()


def test_openai_compatible_image_generation_uses_seed_sample_config(
    monkeypatch,
    tmp_path,
) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    monkeypatch.setenv("KUZHAMBU_WORKER_TEMP_DIR", str(tmp_path))
    captured: dict[str, Any] = {}

    class FakeRegistry:
        def invoke(self, request):
            captured["model_config"] = request.modelConfig.model_dump(mode="json")
            captured["prompt"] = [
                message.model_dump(mode="json") for message in request.prompt.messages
            ]
            return {
                "format": "ARTIFACT",
                "payload": {
                    "data": PNG_1X1,
                    "contentType": "image/png",
                    "filename": "image.png",
                },
                "usage": {"inputTokens": 1, "outputTokens": 1, "latencyMs": 12},
            }

    monkeypatch.setattr("kuzhambu_workers.api.openai_routes._REGISTRY", FakeRegistry())
    body = _image_body()

    response = TestClient(app).post(
        _IMAGE_PATH,
        content=body,
        headers=_headers(body, service="kuzhambu-ai", path=_IMAGE_PATH),
    )

    assert response.status_code == 200
    payload = response.json()
    image_url = payload["data"][0]["url"]
    assert payload["data"][0]["b64_json"] is None
    assert image_url.startswith("/internal/artifacts/art_")
    artifact_id = image_url.removeprefix("/internal/artifacts/")
    assert (tmp_path / "artifacts" / f"{artifact_id}.bin").read_bytes() == PNG_1X1
    assert captured["model_config"]["serviceRole"] == "BYTEDANCE"
    assert captured["model_config"]["apiSource"] == "OPENAI_COMPATIBLE"
    assert captured["model_config"]["baseUrl"] == "https://ark.example/api/v3"
    assert captured["model_config"]["apiKey"] == "ark-key"
    assert captured["model_config"]["modelName"] == "doubao-seedream-5-0-pro-260628"
    assert captured["model_config"]["capabilityTags"] == ["image_gen"]
    assert captured["model_config"]["parameters"]["response_format"] == "url"
    assert captured["model_config"]["parameters"]["size"] == "2K"
    assert captured["model_config"]["parameters"]["stream"] is False
    assert captured["model_config"]["parameters"]["watermark"] is True
    assert captured["prompt"] == [
        {"role": "user", "content": _image_generation_sample_payload()["prompt"]}
    ]


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
        extra_payload = dict(extra)
        extend_params = extra_payload.pop("extendParams", None)
        if isinstance(extend_params, dict):
            payload["extendParams"].update(extend_params)
        payload.update(extra_payload)
    return json.dumps(payload, ensure_ascii=False, separators=(",", ":")).encode()


def _image_body(extra: dict[str, object] | None = None) -> bytes:
    meta = json.loads(_IMAGE_GENERATION_META.read_text(encoding="utf-8"))
    sample = _image_generation_sample_payload()
    payload = {
        "requestId": "req-openai-1",
        "traceId": "trace-openai-1",
        "model": f"BYTEDANCE/{sample['model']}",
        "prompt": sample["prompt"],
        "response_format": sample["response_format"],
        "capability": meta["capability"],
        "scope": meta["scope"],
        "extendParams": {
            "baseUrl": "https://ark.example/api/v3",
            "apiKey": "ark-key",
            "capabilityTags": ["image_gen"],
            "size": sample["size"],
            "stream": sample["stream"],
            "watermark": sample["watermark"],
        },
    }
    if extra is not None:
        extra_payload = dict(extra)
        extend_params = extra_payload.pop("extendParams", None)
        if isinstance(extend_params, dict):
            payload["extendParams"].update(extend_params)
        payload.update(extra_payload)
    return json.dumps(payload, ensure_ascii=False, separators=(",", ":")).encode()


def _image_generation_sample_payload() -> dict[str, object]:
    sample_markdown = _IMAGE_GENERATION_SAMPLE.read_text(encoding="utf-8")
    matched = search(r"-d '(\{.*?\})'", sample_markdown, flags=DOTALL)
    if matched is None:
        raise AssertionError("image generation sample JSON payload not found")
    return json.loads(matched.group(1))


def _json_schema_response_format() -> dict[str, object]:
    return {
        "type": "json_schema",
        "json_schema": {
            "name": "answer",
            "schema": {"type": "object", "properties": {"answer": {"type": "string"}}},
        },
    }


def _usage(input_tokens: int, output_tokens: int) -> UsageSummary:
    return UsageSummary(inputTokens=input_tokens, outputTokens=output_tokens, latencyMs=12)


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
