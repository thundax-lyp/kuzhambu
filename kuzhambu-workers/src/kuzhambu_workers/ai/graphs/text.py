import json
from typing import Any, TypedDict

from langgraph.graph import END, START, StateGraph

from kuzhambu_workers.schemas.ai import AiCapability, AiInvokeRequest, ResultFormat


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
    return {
        **state,
        "result": {
            "format": ResultFormat.TEXT.value,
            "payload": _text_output_as_provider_json(request),
        },
    }


def _text_output_as_provider_json(request: AiInvokeRequest) -> str:
    payload = _first_payload_text(request)
    if payload:
        content = f"[{request.operation}] {payload}"
    elif request.capability == AiCapability.TRANSLATE:
        content = "已完成古文翻译占位结果"
    else:
        content = "已完成文本生成占位结果"
    return json.dumps(
        {
            "choices": [
                {
                    "message": {
                        "content": content,
                    },
                }
            ]
        },
        ensure_ascii=False,
    )


def _first_payload_text(request: AiInvokeRequest) -> str:
    input_payload = request.input.payload
    if not input_payload:
        return ""

    text = input_payload.get("text")
    if isinstance(text, str):
        stripped = text.strip()
        if stripped:
            return stripped

    summary = input_payload.get("summary")
    if isinstance(summary, str):
        stripped = summary.strip()
        if stripped:
            return stripped

    for value in input_payload.values():
        if isinstance(value, str):
            stripped = value.strip()
            if stripped:
                return stripped
    return ""
