import pytest

from kuzhambu_workers.ai.structured_output import (
    openai_response_format,
    parse_structured_output,
    requires_structured_output,
)
from kuzhambu_workers.core.errors import WorkerError, WorkerErrorType
from kuzhambu_workers.schemas.ai import AiCapability, AiInvokeRequest


def test_text_request_does_not_require_structured_output() -> None:
    request = AiInvokeRequest.model_validate(_request_payload("summary"))

    assert requires_structured_output(request) is False
    assert openai_response_format(request) is None


def test_structured_capability_requires_json_response_format() -> None:
    request = AiInvokeRequest.model_validate(_request_payload("tags"))

    assert requires_structured_output(request) is True
    assert openai_response_format(request) == {"type": "json_object"}


def test_schema_output_requests_json_schema_response_format() -> None:
    payload = _request_payload("tags")
    payload["outputSchema"] = {
        "type": "object",
        "properties": {"tags": {"type": "array", "items": {"type": "string"}}},
        "required": ["tags"],
    }
    request = AiInvokeRequest.model_validate(payload)

    assert openai_response_format(request) == {
        "type": "json_schema",
        "json_schema": {
            "name": "sancai_tags_test",
            "schema": {
                "type": "object",
                "properties": {"tags": {"type": "array", "items": {"type": "string"}}},
                "required": ["tags"],
            },
        },
    }


def test_parse_structured_output_enforces_array_bounds() -> None:
    output_schema = AiInvokeRequest.model_validate(_bounded_schema_request_payload()).outputSchema

    with pytest.raises(WorkerError) as raised:
        parse_structured_output('{"tags":["a"]}', AiCapability.TAGS, output_schema)

    assert raised.value.error_type == WorkerErrorType.OUTPUT_FORMAT_FAILURE
    assert raised.value.code == "MODEL_OUTPUT_INVALID_JSON"
    assert raised.value.detail == {
        "capability": "tags",
        "schemaPath": "$.tags",
    }


def test_force_json_requires_json_response_format() -> None:
    payload = _request_payload("summary")
    payload["options"] = {"stream": False, "forceJson": True, "locale": "zh-CN"}
    request = AiInvokeRequest.model_validate(payload)

    assert requires_structured_output(request) is True
    assert openai_response_format(request) == {"type": "json_object"}


def test_parse_structured_output_returns_json_object() -> None:
    payload = parse_structured_output('{"answer":"ok"}', AiCapability.QA)

    assert payload == {"answer": "ok"}


def test_parse_structured_output_returns_json_array() -> None:
    payload = parse_structured_output('[{"tag":"a"}]', AiCapability.TAGS)

    assert payload == [{"tag": "a"}]


def test_parse_structured_output_rejects_invalid_json() -> None:
    with pytest.raises(WorkerError) as raised:
        parse_structured_output("not-json", AiCapability.TAGS)

    assert raised.value.error_type == WorkerErrorType.OUTPUT_FORMAT_FAILURE
    assert raised.value.code == "MODEL_OUTPUT_INVALID_JSON"


def test_parse_structured_output_enforces_configured_schema_required_fields() -> None:
    output_schema = AiInvokeRequest.model_validate(_schema_request_payload()).outputSchema

    with pytest.raises(WorkerError) as raised:
        parse_structured_output('{"items":[]}', AiCapability.TAGS, output_schema)

    assert raised.value.error_type == WorkerErrorType.OUTPUT_FORMAT_FAILURE
    assert raised.value.code == "MODEL_OUTPUT_INVALID_JSON"
    assert raised.value.detail == {
        "capability": "tags",
        "schemaPath": "$.tags",
    }


def test_parse_structured_output_enforces_nested_configured_schema() -> None:
    output_schema = AiInvokeRequest.model_validate(_schema_request_payload()).outputSchema

    with pytest.raises(WorkerError) as raised:
        parse_structured_output('{"tags":[1]}', AiCapability.TAGS, output_schema)

    assert raised.value.error_type == WorkerErrorType.OUTPUT_FORMAT_FAILURE
    assert raised.value.code == "MODEL_OUTPUT_INVALID_JSON"
    assert raised.value.detail == {
        "capability": "tags",
        "schemaPath": "$.tags[0]",
    }


@pytest.mark.parametrize(
    ("capability", "content", "expected"),
    [
        (
            AiCapability.RELATION_EXTRACTION,
            '{"entities":[{"name":"A"}]}',
            {"entities": [{"name": "A"}], "relations": [], "sourceSnippets": [], "warnings": []},
        ),
        (
            AiCapability.KNOWLEDGE_GRAPH,
            '{"relations":[{"sourceName":"A","targetName":"B"}]}',
            {
                "entities": [],
                "relations": [{"sourceName": "A", "targetName": "B"}],
                "entryRefs": [],
                "warnings": [],
            },
        ),
        (
            AiCapability.LINEAGE_EXTRACTION,
            '{"nodes":[{"name":"A"}]}',
            {"nodes": [{"name": "A"}], "relations": [], "sourceSnippets": [], "warnings": []},
        ),
    ],
)
def test_parse_structured_output_normalizes_knowledge_payload_shape(
    capability: AiCapability,
    content: str,
    expected: dict,
) -> None:
    assert parse_structured_output(content, capability) == expected


def test_parse_structured_output_rejects_invalid_knowledge_field_type() -> None:
    with pytest.raises(WorkerError) as raised:
        parse_structured_output('{"entities":{}}', AiCapability.RELATION_EXTRACTION)

    assert raised.value.error_type == WorkerErrorType.OUTPUT_FORMAT_FAILURE
    assert raised.value.code == "MODEL_OUTPUT_INVALID_JSON"
    assert raised.value.detail == {
        "capability": "relation_extraction",
        "field": "entities",
    }


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


def _schema_request_payload() -> dict:
    payload = _request_payload("tags")
    payload["outputSchema"] = {
        "type": "object",
        "schema": {
            "type": "object",
            "properties": {"tags": {"type": "array", "items": {"type": "string"}}},
            "required": ["tags"],
        },
    }
    return payload


def _bounded_schema_request_payload() -> dict:
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
    return payload
