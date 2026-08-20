from time import sleep

import httpx
import pytest

from kuzhambu_workers.ai import openai_compatible
from kuzhambu_workers.ai.openai_compatible import iter_chat_completion_chunks
from kuzhambu_workers.core.errors import WorkerError, WorkerErrorType
from kuzhambu_workers.schemas.ai import AiInvokeRequest


def test_iter_chat_completion_chunks_parses_delta_and_usage() -> None:
    def handler(_: httpx.Request) -> httpx.Response:
        return httpx.Response(
            200,
            text=(
                'data: {"choices":[{"delta":{"content":"hel"}}]}\n\n'
                'data: {"choices":[{"delta":{"content":"lo"},"finish_reason":"stop"}],'
                '"usage":{"prompt_tokens":3,"completion_tokens":4}}\n\n'
                "data: [DONE]\n\n"
            ),
        )

    client = httpx.Client(transport=httpx.MockTransport(handler))
    request = AiInvokeRequest.model_validate(_request_payload())

    chunks = list(iter_chat_completion_chunks(request, client=client))

    assert [chunk.delta for chunk in chunks] == ["hel", "lo"]
    assert chunks[-1].usage is not None
    assert chunks[-1].usage.inputTokens == 3
    assert chunks[-1].usage.outputTokens == 4
    assert chunks[-1].provider_usage is True
    assert chunks[-1].finish_reason == "stop"


def test_iter_chat_completion_chunks_ignores_role_only_chunk() -> None:
    def handler(_: httpx.Request) -> httpx.Response:
        return httpx.Response(
            200,
            text=(
                'data: {"choices":[{"delta":{"role":"assistant"}}]}\n\n'
                'data: {"choices":[{"delta":{"content":"answer"},"finish_reason":"stop"}]}\n\n'
            ),
        )

    client = httpx.Client(transport=httpx.MockTransport(handler))
    request = AiInvokeRequest.model_validate(_request_payload())

    chunks = list(iter_chat_completion_chunks(request, client=client))

    assert chunks[0].delta == ""
    assert chunks[1].delta == "answer"
    assert chunks[1].finish_reason == "stop"


def test_iter_chat_completion_chunks_ignores_empty_delta_chunks() -> None:
    def handler(_: httpx.Request) -> httpx.Response:
        return httpx.Response(
            200,
            text=(
                'data: {"choices":[{"delta":{"content":null}}]}\n\n'
                'data: {"choices":[{"delta":{"reasoning_content":"thinking"}}]}\n\n'
                'data: {"choices":[{"delta":{"content":"answer"},"finish_reason":"stop"}]}\n\n'
            ),
        )

    client = httpx.Client(transport=httpx.MockTransport(handler))
    request = AiInvokeRequest.model_validate(_request_payload())

    chunks = list(iter_chat_completion_chunks(request, client=client))

    assert [chunk.delta for chunk in chunks[:3]] == ["", "", "answer"]
    assert chunks[2].finish_reason == "stop"


def test_iter_chat_completion_chunks_adds_latency_usage_when_provider_omits_usage() -> None:
    def handler(_: httpx.Request) -> httpx.Response:
        return httpx.Response(
            200,
            text='data: {"choices":[{"delta":{"content":"answer"},"finish_reason":"stop"}]}\n\n',
        )

    client = httpx.Client(transport=httpx.MockTransport(handler))
    request = AiInvokeRequest.model_validate(_request_payload())

    chunks = list(iter_chat_completion_chunks(request, client=client))

    assert chunks[0].delta == "answer"
    assert chunks[-1].usage is not None
    assert chunks[-1].usage.inputTokens == 0
    assert chunks[-1].usage.outputTokens == 0
    assert chunks[-1].provider_usage is False


def test_iter_chat_completion_chunks_ignores_null_usage_fields() -> None:
    def handler(_: httpx.Request) -> httpx.Response:
        return httpx.Response(
            200,
            text=(
                'data: {"choices":[{"delta":{"content":"answer"}}],"usage":null}\n\n'
                'data: {"choices":[],"usage":{"prompt_tokens":3,"completion_tokens":4}}\n\n'
            ),
        )

    client = httpx.Client(transport=httpx.MockTransport(handler))
    request = AiInvokeRequest.model_validate(_request_payload())

    chunks = list(iter_chat_completion_chunks(request, client=client))

    assert chunks[0].delta == "answer"
    assert chunks[0].usage is None
    assert chunks[0].provider_usage is False
    assert chunks[1].usage is not None
    assert chunks[1].provider_usage is True
    assert chunks[1].usage.inputTokens == 3
    assert chunks[1].usage.outputTokens == 4


def test_iter_chat_completion_chunks_rejects_invalid_chunk() -> None:
    def handler(_: httpx.Request) -> httpx.Response:
        return httpx.Response(200, text="data: not-json\n\n")

    client = httpx.Client(transport=httpx.MockTransport(handler))
    request = AiInvokeRequest.model_validate(_request_payload())

    with pytest.raises(WorkerError) as raised:
        list(iter_chat_completion_chunks(request, client=client))

    assert raised.value.error_type == WorkerErrorType.OUTPUT_FORMAT_FAILURE
    assert raised.value.code == "MODEL_STREAM_CHUNK_INVALID"


def test_iter_chat_completion_chunks_enforces_total_execution_limit() -> None:
    class SlowStream(httpx.SyncByteStream):
        def __iter__(self):
            sleep(0.05)
            yield b'data: {"choices":[{"delta":{"content":"answer"}}]}\n\n'

        def close(self) -> None:
            return None

    def handler(_: httpx.Request) -> httpx.Response:
        return httpx.Response(
            200,
            stream=SlowStream(),
        )

    client = httpx.Client(transport=httpx.MockTransport(handler))
    payload = _request_payload()
    payload["modelConfig"]["timeoutMs"] = 10
    request = AiInvokeRequest.model_validate(payload)

    with pytest.raises(WorkerError) as raised:
        list(iter_chat_completion_chunks(request, client=client))

    assert raised.value.error_type == WorkerErrorType.WORKER_TIMEOUT
    assert raised.value.code == "MODEL_TIMEOUT"
    assert raised.value.detail == {"timeoutType": "TOTAL_EXECUTION"}


def test_stream_total_execution_uses_configured_timeout_with_response_margin() -> None:
    assert openai_compatible._stream_total_execution_seconds(900_000) == 895.0


def _request_payload() -> dict:
    return {
        "requestId": "req-1",
        "traceId": "trace-1",
        "callerDomain": "AI",
        "operation": "TEST",
        "capability": "answer_generation",
        "scope": "DISCOVERY",
        "modelConfig": {
            "serviceRole": "PRIMARY",
            "apiSource": "OPENAI_COMPATIBLE",
            "baseUrl": "https://model.example/v1",
            "apiKey": "process-only",
            "modelName": "model",
            "capabilityTags": ["text", "streaming_text"],
            "parameters": {"temperature": 0.2},
            "timeoutMs": 60000,
        },
        "prompt": {
            "messages": [
                {"role": "system", "content": "system prompt"},
                {"role": "user", "content": "user prompt"},
            ],
        },
        "input": {
            "contentType": "DISCOVERY_CONTEXT",
            "payload": {"query": "hello"},
        },
        "outputSchema": {"type": "text"},
        "options": {"stream": True, "forceJson": False, "locale": "zh-CN"},
    }
