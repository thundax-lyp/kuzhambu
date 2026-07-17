from typing import Any, TypedDict

from langgraph.graph import END, START, StateGraph

from kuzhambu_workers.ai.openai_compatible import (
    OpenAiChatCompletionResult,
    invoke_chat_completion,
    iter_chat_completion_chunks,
)
from kuzhambu_workers.ai.structured_output import (
    parse_structured_output,
    requires_structured_output,
)
from kuzhambu_workers.core.errors import unsupported_capability
from kuzhambu_workers.schemas.ai import AiCapability, AiInvokeRequest, ResultFormat


class BasicGraphState(TypedDict, total=False):
    request: AiInvokeRequest
    streamResponseFormat: dict[str, Any] | None
    result: dict[str, Any]


def build_basic_graph() -> Any:
    graph = StateGraph(BasicGraphState)
    graph.add_node("execute", _execute)
    graph.add_edge(START, "execute")
    graph.add_edge("execute", END)
    return graph.compile()


def _execute(state: BasicGraphState) -> BasicGraphState:
    request = state["request"]
    if request.capability == AiCapability.IMAGE_GEN:
        raise unsupported_capability(request.capability.value)

    if "streamResponseFormat" in state:
        return {
            **state,
            "result": {
                "format": "STREAM",
                "chunks": iter_chat_completion_chunks(
                    request,
                    response_format=state.get("streamResponseFormat"),
                ),
            },
        }

    model_result = _invoke_chat_completion(request)
    result_format = _result_format(request)
    payload: str | dict[str, Any] | list[Any] = model_result.content
    if result_format == ResultFormat.STRUCTURED:
        payload = parse_structured_output(model_result.content, request.capability)

    return {
        **state,
        "result": {
            "format": result_format.value,
            "payload": payload,
            "usage": model_result.usage.model_dump(mode="json"),
            "rawFinishReason": model_result.raw_finish_reason,
            "providerUsage": model_result.provider_usage,
        },
    }


def _result_format(request: AiInvokeRequest) -> ResultFormat:
    if requires_structured_output(request):
        return ResultFormat.STRUCTURED
    if request.capability in {AiCapability.IMAGE_ANALYSIS, AiCapability.FUSION}:
        return ResultFormat.MARKDOWN
    return ResultFormat.TEXT


def _response_format(request: AiInvokeRequest) -> dict[str, Any] | None:
    response_format = request.input.payload.get("responseFormat")
    if isinstance(response_format, dict):
        return response_format
    return None


def _invoke_chat_completion(request: AiInvokeRequest) -> OpenAiChatCompletionResult:
    response_format = _response_format(request)
    if response_format is None:
        return invoke_chat_completion(request)
    return invoke_chat_completion(request, response_format=response_format)
