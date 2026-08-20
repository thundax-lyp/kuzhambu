from typing import Any

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
    # "OpenAI-compatible" is not a promise that the provider implements the
    # newer json_schema extension. The Java boundary validates outputSchema
    # after parsing, so json_object preserves the contract for every compatible
    # provider while avoiding an unsupported provider-side extension.
    return {"type": "json_object"}
