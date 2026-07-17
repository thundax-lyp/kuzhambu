import json

import httpx
import pytest

from kuzhambu_workers.ai.openai_compatible import (
    build_chat_completion_request,
    invoke_chat_completion,
)
from kuzhambu_workers.core.errors import WorkerError, WorkerErrorType
from kuzhambu_workers.schemas.ai import AiInvokeRequest


def test_build_chat_completion_request_uses_model_config_and_prompt_messages() -> None:
    request = AiInvokeRequest.model_validate(_request_payload())

    chat_request = build_chat_completion_request(
        request,
        stream=False,
        response_format={"type": "json_object"},
    )

    assert chat_request.model == "model"
    assert chat_request.messages == [
        {"role": "system", "content": "system prompt"},
        {"role": "user", "content": "user prompt"},
    ]
    assert chat_request.stream is False
    assert chat_request.response_format == {"type": "json_object"}
    assert chat_request.parameters == {"temperature": 0.2}


def test_invoke_chat_completion_posts_openai_compatible_request() -> None:
    captured: dict[str, object] = {}

    def handler(request: httpx.Request) -> httpx.Response:
        captured["url"] = str(request.url)
        captured["authorization"] = request.headers["Authorization"]
        captured["content_type"] = request.headers["Content-Type"]
        captured["body"] = json.loads(request.content)
        return httpx.Response(
            200,
            json={
                "choices": [
                    {
                        "message": {"content": "answer"},
                        "finish_reason": "stop",
                    }
                ],
                "usage": {"prompt_tokens": 7, "completion_tokens": 11},
            },
        )

    client = httpx.Client(transport=httpx.MockTransport(handler))
    request = AiInvokeRequest.model_validate(_request_payload())

    result = invoke_chat_completion(request, client=client)

    assert captured["url"] == "https://model.example/v1/chat/completions"
    assert captured["authorization"] == "Bearer process-only"
    assert captured["content_type"] == "application/json"
    assert captured["body"] == {
        "model": "model",
        "messages": [
            {"role": "system", "content": "system prompt"},
            {"role": "user", "content": "user prompt"},
        ],
        "stream": False,
        "temperature": 0.2,
    }
    assert result.content == "answer"
    assert result.usage.inputTokens == 7
    assert result.usage.outputTokens == 11
    assert result.usage.costAmount == "0.00"
    assert result.usage.latencyMs >= 0
    assert result.raw_finish_reason == "stop"
    assert result.provider_usage is True


def test_invoke_chat_completion_marks_missing_usage_as_synthetic() -> None:
    def handler(_: httpx.Request) -> httpx.Response:
        return httpx.Response(
            200,
            json={
                "choices": [
                    {
                        "message": {"content": "answer"},
                        "finish_reason": "stop",
                    }
                ],
            },
        )

    client = httpx.Client(transport=httpx.MockTransport(handler))
    request = AiInvokeRequest.model_validate(_request_payload())

    result = invoke_chat_completion(request, client=client)

    assert result.content == "answer"
    assert result.usage.inputTokens == 0
    assert result.usage.outputTokens == 0
    assert result.provider_usage is False


@pytest.mark.parametrize(
    ("status_code", "expected_type", "expected_code", "expected_retryable"),
    [
        (429, WorkerErrorType.MODEL_TRANSPORT_FAILURE, "MODEL_RATE_LIMITED", True),
        (500, WorkerErrorType.MODEL_TRANSPORT_FAILURE, "MODEL_PROVIDER_UNAVAILABLE", True),
        (400, WorkerErrorType.MODEL_SEMANTIC_FAILURE, "MODEL_REQUEST_REJECTED", False),
    ],
)
def test_invoke_chat_completion_maps_provider_errors(
    status_code: int,
    expected_type: WorkerErrorType,
    expected_code: str,
    expected_retryable: bool,
) -> None:
    def handler(_: httpx.Request) -> httpx.Response:
        return httpx.Response(
            status_code,
            json={"error": {"type": "provider_error"}},
        )

    client = httpx.Client(transport=httpx.MockTransport(handler))
    request = AiInvokeRequest.model_validate(_request_payload())

    with pytest.raises(WorkerError) as raised:
        invoke_chat_completion(request, client=client)

    assert raised.value.error_type == expected_type
    assert raised.value.code == expected_code
    assert raised.value.retryable is expected_retryable
    assert raised.value.detail == {
        "statusCode": status_code,
        "providerErrorType": "provider_error",
    }


def test_invoke_chat_completion_maps_transport_error() -> None:
    def handler(request: httpx.Request) -> httpx.Response:
        raise httpx.ConnectError("connection failed", request=request)

    client = httpx.Client(transport=httpx.MockTransport(handler))
    request = AiInvokeRequest.model_validate(_request_payload())

    with pytest.raises(WorkerError) as raised:
        invoke_chat_completion(request, client=client)

    assert raised.value.error_type == WorkerErrorType.MODEL_TRANSPORT_FAILURE
    assert raised.value.code == "MODEL_TRANSPORT_ERROR"
    assert raised.value.retryable is True


def test_invoke_chat_completion_rejects_empty_model_output() -> None:
    def handler(_: httpx.Request) -> httpx.Response:
        return httpx.Response(200, json={"choices": [{"message": {"content": ""}}]})

    client = httpx.Client(transport=httpx.MockTransport(handler))
    request = AiInvokeRequest.model_validate(_request_payload())

    with pytest.raises(WorkerError) as raised:
        invoke_chat_completion(request, client=client)

    assert raised.value.error_type == WorkerErrorType.OUTPUT_FORMAT_FAILURE
    assert raised.value.code == "MODEL_OUTPUT_EMPTY"


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
    }
