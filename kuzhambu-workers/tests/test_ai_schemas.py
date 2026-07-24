import pytest
from pydantic import ValidationError

from kuzhambu_workers.schemas.ai import (
    AiCapability,
    AiInvokeRequest,
    AiInvokeResponse,
    ResultFormat,
)
from kuzhambu_workers.schemas.common import WorkerStatus
from kuzhambu_workers.schemas.stream import StreamEvent, StreamEventType


def test_ai_invoke_request_accepts_contract_shape() -> None:
    request = AiInvokeRequest.model_validate(_request_payload("translate"))

    assert request.requestId == "req-1"
    assert request.traceId == "trace-1"
    assert request.capability == AiCapability.TRANSLATE
    assert request.prompt.messages[0].role == "system"
    assert request.outputSchema.type == "text"


def test_ai_invoke_request_wraps_flat_output_schema_constraints() -> None:
    payload = _request_payload("tags")
    payload["outputSchema"] = {
        "type": "object",
        "properties": {
            "tags": {
                "type": "array",
                "items": {"type": "string"},
                "minItems": 3,
                "maxItems": 8,
            }
        },
        "required": ["tags"],
    }

    request = AiInvokeRequest.model_validate(payload)

    assert request.outputSchema.schema_ == {
        "type": "object",
        "properties": {
            "tags": {
                "type": "array",
                "items": {"type": "string"},
                "minItems": 3,
                "maxItems": 8,
            }
        },
        "required": ["tags"],
    }


@pytest.mark.parametrize(
    "capability",
    ["image_gen", "fusion", "version_summary", "visual"],
)
def test_ai_capability_contains_canonical_values(capability: str) -> None:
    request = AiInvokeRequest.model_validate(_request_payload(capability))

    assert request.capability.value == capability


def test_ai_invoke_request_rejects_unknown_capability() -> None:
    with pytest.raises(ValidationError):
        AiInvokeRequest.model_validate(_request_payload("image_generation"))


def test_ai_response_accepts_success_payload() -> None:
    response = AiInvokeResponse.model_validate(
        {
            "requestId": "req-1",
            "traceId": "trace-1",
            "status": "SUCCEEDED",
            "capability": "summary",
            "result": {
                "format": "TEXT",
                "payload": "摘要文本",
            },
            "usage": {
                "latencyMs": 10,
                "inputTokens": 1,
                "outputTokens": 2,
                "costAmount": "0.00",
            },
            "warnings": [],
            "error": None,
        }
    )

    assert response.status == WorkerStatus.SUCCEEDED
    assert response.result is not None
    assert response.result.format == ResultFormat.TEXT


def test_stream_event_requires_common_fields() -> None:
    event = StreamEvent.model_validate(
        {
            "eventId": "evt-1",
            "requestId": "req-1",
            "traceId": "trace-1",
            "stage": "model_stream",
            "timestamp": "2026-06-01T10:00:00.000Z",
            "event": "delta",
            "delta": {"text": "白话"},
        }
    )

    assert event.event == StreamEventType.DELTA
    assert event.delta == {"text": "白话"}


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
            "parameters": {"temperature": 0.2},
            "timeoutMs": 60000,
        },
        "prompt": {
            "messages": [
                {
                    "role": "system",
                    "content": "system prompt",
                },
                {
                    "role": "user",
                    "content": "user prompt",
                },
            ],
            "variables": {"text": "hello"},
        },
        "input": {
            "contentType": "SANCAI_ENTRY",
            "contentId": "10001",
            "payload": {"text": "hello"},
        },
        "outputSchema": {"type": "text"},
        "options": {"stream": False, "forceJson": False, "locale": "zh-CN"},
    }
