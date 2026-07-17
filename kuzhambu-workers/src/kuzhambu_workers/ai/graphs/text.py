from typing import Any, TypedDict

from langgraph.graph import END, START, StateGraph

from kuzhambu_workers.ai.openai_compatible import (
    OpenAiChatCompletionResult,
    invoke_chat_completion,
)
from kuzhambu_workers.schemas.ai import AiInvokeRequest, ResultFormat


class TextGraphState(TypedDict, total=False):
    request: AiInvokeRequest
    result: dict[str, Any]


def build_text_graph() -> Any:
    graph = StateGraph(TextGraphState)
    graph.add_node("invoke_text", _invoke_text)
    graph.add_edge(START, "invoke_text")
    graph.add_edge("invoke_text", END)
    return graph.compile()


def _invoke_text(state: TextGraphState) -> TextGraphState:
    request = state["request"]
    model_result = _invoke_chat_completion(request)
    return {
        **state,
        "result": {
            "format": ResultFormat.TEXT.value,
            "payload": model_result.content,
            "usage": model_result.usage.model_dump(mode="json"),
            "rawFinishReason": model_result.raw_finish_reason,
        },
    }


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
