from typing import Any, TypedDict

from langgraph.graph import END, START, StateGraph

from kuzhambu_workers.schemas.ai import AiCapability, AiInvokeRequest, ResultFormat


class BasicGraphState(TypedDict, total=False):
    request: AiInvokeRequest
    result: dict[str, Any]


def build_basic_graph() -> Any:
    graph = StateGraph(BasicGraphState)
    graph.add_node("execute", _execute)
    graph.add_edge(START, "execute")
    graph.add_edge("execute", END)
    return graph.compile()


def _execute(state: BasicGraphState) -> BasicGraphState:
    request = state["request"]
    return {
        **state,
        "result": {
            "format": _result_format(request.capability).value,
            "payload": _placeholder_payload(request),
        },
    }


def _result_format(capability: AiCapability) -> ResultFormat:
    if capability in {
        AiCapability.TAGS,
        AiCapability.QA,
        AiCapability.SPLIT,
        AiCapability.QUERY_UNDERSTANDING,
        AiCapability.KNOWLEDGE_GRAPH,
        AiCapability.RELATION_EXTRACTION,
        AiCapability.LINEAGE_EXTRACTION,
        AiCapability.PROMPT_SUGGESTION,
    }:
        return ResultFormat.STRUCTURED
    if capability in {AiCapability.IMAGE_ANALYSIS, AiCapability.FUSION}:
        return ResultFormat.MARKDOWN
    if capability == AiCapability.IMAGE_GEN:
        return ResultFormat.ARTIFACT
    return ResultFormat.TEXT


def _placeholder_payload(request: AiInvokeRequest) -> str | dict[str, Any]:
    if _result_format(request.capability) == ResultFormat.STRUCTURED:
        return {"capability": request.capability.value, "placeholder": True}
    if request.capability == AiCapability.IMAGE_GEN:
        return {
            "artifactType": "IMAGE",
            "contentType": "image/png",
            "encoding": "SSE_ARTIFACT_CHUNK",
            "sizeBytes": 0,
            "sha256": "",
        }
    return ""
