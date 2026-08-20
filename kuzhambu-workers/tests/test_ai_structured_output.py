from kuzhambu_workers.ai.structured_output import (
    openai_response_format,
    requires_structured_output,
)
from kuzhambu_workers.schemas.ai import AiInvokeRequest


def test_text_request_does_not_require_structured_output() -> None:
    request = AiInvokeRequest.model_validate(_request_payload("summary"))

    assert requires_structured_output(request) is False
    assert openai_response_format(request) is None


def test_structured_capability_requires_json_response_format() -> None:
    request = AiInvokeRequest.model_validate(_request_payload("tags"))

    assert requires_structured_output(request) is True
    assert openai_response_format(request) == {"type": "json_object"}


def test_schema_output_requests_compatible_json_response_format() -> None:
    payload = _request_payload("tags")
    payload["outputSchema"] = {
        "type": "object",
        "properties": {"tags": {"type": "array", "items": {"type": "string"}}},
        "required": ["tags"],
    }
    request = AiInvokeRequest.model_validate(payload)

    assert openai_response_format(request) == {"type": "json_object"}


def test_force_json_requires_json_response_format() -> None:
    payload = _request_payload("summary")
    payload["options"] = {"stream": False, "forceJson": True, "locale": "zh-CN"}
    request = AiInvokeRequest.model_validate(payload)

    assert requires_structured_output(request) is True
    assert openai_response_format(request) == {"type": "json_object"}


def _request_payload(capability: str) -> dict:
    return {
        "requestId": "req-1",
        "traceId": "trace-1",
        "callerDomain": "AI",
        "operation": "TEST",
        "capability": capability,
        "scope": "SANCAI",
        "modelConfig": {
            "serviceRole": "PRIMARY",
            "apiSource": "OPENAI_COMPATIBLE",
            "baseUrl": "https://model.example/v1",
            "apiKey": "process-only",
            "modelName": "model",
            "capabilityTags": ["text"],
        },
        "prompt": {
            "messages": [
                {"role": "system", "content": "system prompt"},
                {"role": "user", "content": "user prompt"},
            ],
        },
        "input": {
            "contentType": "SANCAI_ENTRY",
            "payload": {"text": "hello"},
        },
        "outputSchema": {"type": "text"},
    }
