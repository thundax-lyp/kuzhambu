import json
from typing import Any

from kuzhambu_workers.ai.errors import model_output_invalid_json
from kuzhambu_workers.schemas.ai import AiCapability, AiInvokeRequest, AiOutputSchema

STRUCTURED_CAPABILITIES = frozenset(
    {
        AiCapability.TAGS,
        AiCapability.QA,
        AiCapability.SPLIT,
        AiCapability.QUERY_UNDERSTANDING,
        AiCapability.KNOWLEDGE_GRAPH,
        AiCapability.RELATION_EXTRACTION,
        AiCapability.LINEAGE_EXTRACTION,
        AiCapability.PROMPT_SUGGESTION,
    }
)

KNOWLEDGE_PAYLOAD_FIELDS = {
    AiCapability.RELATION_EXTRACTION: ("entities", "relations", "sourceSnippets", "warnings"),
    AiCapability.KNOWLEDGE_GRAPH: ("entities", "relations", "entryRefs", "warnings"),
    AiCapability.LINEAGE_EXTRACTION: ("nodes", "relations", "sourceSnippets", "warnings"),
}


def structured_output_instruction(output_schema: AiOutputSchema) -> str | None:
    if output_schema.type.lower() in {"text", "markdown"}:
        return None
    if output_schema.schema_ is None:
        return "请返回可解析的 JSON 结构。"
    return "请严格返回符合 outputSchema.schema 的 JSON 结构。"


def output_schema_payload(output_schema: AiOutputSchema) -> dict[str, Any]:
    payload: dict[str, Any] = {"type": output_schema.type}
    if output_schema.schema_ is not None:
        payload["schema"] = output_schema.schema_
    return payload


def requires_structured_output(request: AiInvokeRequest) -> bool:
    output_type = request.outputSchema.type.lower()
    return (
        output_type not in {"text", "markdown"}
        or request.options.forceJson
        or request.capability in STRUCTURED_CAPABILITIES
    )


def openai_response_format(request: AiInvokeRequest) -> dict[str, Any] | None:
    if not requires_structured_output(request):
        return None
    if request.outputSchema.schema_ is not None:
        return {
            "type": "json_schema",
            "json_schema": {
                "name": _response_schema_name(request),
                "strict": True,
                "schema": request.outputSchema.schema_,
            },
        }
    return {"type": "json_object"}


def parse_structured_output(
    content: str,
    capability: AiCapability,
    output_schema: AiOutputSchema | None = None,
) -> dict[str, Any] | list[Any]:
    try:
        payload = json.loads(content)
    except json.JSONDecodeError as exc:
        raise model_output_invalid_json(detail={"capability": capability.value}) from exc
    except ValueError as exc:
        raise model_output_invalid_json(detail={"capability": capability.value}) from exc

    if not isinstance(payload, dict | list):
        raise model_output_invalid_json(detail={"capability": capability.value})
    if output_schema is not None and output_schema.schema_ is not None:
        _validate_schema(payload, output_schema.schema_, capability)
    if isinstance(payload, dict):
        return _normalize_structured_object(payload, capability)
    return payload


def _response_schema_name(request: AiInvokeRequest) -> str:
    return f"{request.scope}_{request.capability.value}_{request.operation}".lower()


def _validate_schema(payload: Any, schema: dict[str, Any], capability: AiCapability) -> None:
    violation = _schema_violation(payload, schema, "$")
    if violation is None:
        return
    raise model_output_invalid_json(
        detail={
            "capability": capability.value,
            "schemaPath": violation,
        }
    )


def _schema_violation(payload: Any, schema: dict[str, Any], path: str) -> str | None:
    expected_type = schema.get("type")
    if isinstance(expected_type, list):
        if not any(_matches_schema_type(payload, type_name) for type_name in expected_type):
            return path
    elif isinstance(expected_type, str) and not _matches_schema_type(payload, expected_type):
        return path

    enum_values = schema.get("enum")
    if isinstance(enum_values, list) and payload not in enum_values:
        return path

    if isinstance(payload, dict):
        required = schema.get("required", [])
        if isinstance(required, list):
            for field in required:
                if isinstance(field, str) and field not in payload:
                    return f"{path}.{field}"
        properties = schema.get("properties", {})
        if isinstance(properties, dict):
            for field, field_schema in properties.items():
                if field in payload and isinstance(field_schema, dict):
                    violation = _schema_violation(payload[field], field_schema, f"{path}.{field}")
                    if violation is not None:
                        return violation
        return None

    if isinstance(payload, list):
        item_schema = schema.get("items")
        if isinstance(item_schema, dict):
            for index, item in enumerate(payload):
                violation = _schema_violation(item, item_schema, f"{path}[{index}]")
                if violation is not None:
                    return violation
    return None


def _matches_schema_type(payload: Any, expected_type: str) -> bool:
    if expected_type == "object":
        return isinstance(payload, dict)
    if expected_type == "array":
        return isinstance(payload, list)
    if expected_type == "string":
        return isinstance(payload, str)
    if expected_type == "integer":
        return isinstance(payload, int) and not isinstance(payload, bool)
    if expected_type == "number":
        return isinstance(payload, int | float) and not isinstance(payload, bool)
    if expected_type == "boolean":
        return isinstance(payload, bool)
    if expected_type == "null":
        return payload is None
    return True


def _normalize_structured_object(
    payload: dict[str, Any],
    capability: AiCapability,
) -> dict[str, Any]:
    expected_fields = KNOWLEDGE_PAYLOAD_FIELDS.get(capability)
    if expected_fields is None:
        return payload

    normalized = dict(payload)
    for field in expected_fields:
        value = normalized.get(field, [])
        if not isinstance(value, list):
            raise model_output_invalid_json(
                detail={
                    "capability": capability.value,
                    "field": field,
                }
            )
        normalized[field] = value
    return {field: normalized[field] for field in expected_fields}
