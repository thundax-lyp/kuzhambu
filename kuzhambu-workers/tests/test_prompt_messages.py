from langchain_core.messages import AIMessage, HumanMessage, SystemMessage, ToolMessage

from kuzhambu_workers.ai.model_adapters import prepare_openai_compatible_invocation
from kuzhambu_workers.ai.prompt_messages import build_langchain_messages
from kuzhambu_workers.ai.structured_output import (
    output_schema_payload,
    structured_output_instruction,
)
from kuzhambu_workers.schemas.ai import AiInvokeRequest, AiOutputSchema


def test_build_langchain_messages_uses_rendered_prompt_messages() -> None:
    request = AiInvokeRequest.model_validate(_request_payload())

    messages = build_langchain_messages(request.prompt)

    assert [type(message) for message in messages] == [
        SystemMessage,
        HumanMessage,
        AIMessage,
        ToolMessage,
    ]
    assert [message.content for message in messages] == [
        "system prompt",
        "user prompt",
        "assistant answer",
        "tool result",
    ]


def test_model_adapter_uses_request_model_config_only() -> None:
    request = AiInvokeRequest.model_validate(_request_payload())

    invocation = prepare_openai_compatible_invocation(request.modelConfig)

    assert invocation.model_name == "model"
    assert invocation.base_url == "https://model.example/v1"
    assert invocation.parameters == {"temperature": 0.2}
    assert invocation.timeout_ms == 60000
    assert invocation.supports_streaming is True


def test_structured_output_instruction_for_text_is_empty() -> None:
    assert structured_output_instruction(AiOutputSchema(type="text")) is None


def test_structured_output_instruction_for_schema_is_explicit() -> None:
    schema = AiOutputSchema.model_validate(
        {
            "type": "object",
            "schema": {"required": ["answer"]},
        }
    )

    assert (
        structured_output_instruction(schema) == "请严格返回符合 outputSchema.schema 的 JSON 结构。"
    )
    assert output_schema_payload(schema) == {
        "type": "object",
        "schema": {"required": ["answer"]},
    }


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
                {
                    "role": "system",
                    "content": "system prompt",
                },
                {
                    "role": "user",
                    "content": "user prompt",
                },
                {
                    "role": "assistant",
                    "content": "assistant answer",
                },
                {
                    "role": "tool",
                    "content": "tool result",
                },
            ],
            "variables": {"query": "hello"},
            "templateId": "tpl-1",
            "promptVersionId": "ver-1",
        },
        "input": {
            "contentType": "DISCOVERY_CONTEXT",
            "payload": {"query": "hello"},
        },
        "outputSchema": {"type": "text"},
    }
