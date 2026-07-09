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


def openai_response_format(request: AiInvokeRequest) -> dict[str, str] | None:
    if not requires_structured_output(request):
        return None
    return {"type": "json_object"}


def parse_structured_output(content: str, capability: AiCapability) -> dict[str, Any] | list[Any]:
    try:
        payload = json.loads(content)
    except json.JSONDecodeError as exc:
        raise model_output_invalid_json(detail={"capability": capability.value}) from exc
    except ValueError as exc:
        raise model_output_invalid_json(detail={"capability": capability.value}) from exc

    if not isinstance(payload, dict | list):
        raise model_output_invalid_json(detail={"capability": capability.value})
    if isinstance(payload, dict):
        return _normalize_structured_object(payload, capability)
    return payload


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
