from typing import Any

from kuzhambu_workers.schemas.ai import AiOutputSchema


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
