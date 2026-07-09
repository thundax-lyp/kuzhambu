from typing import Any, TypedDict

from langgraph.graph import END, START, StateGraph

from kuzhambu_workers.ai.image_generation import generate_image
from kuzhambu_workers.schemas.ai import AiInvokeRequest, ResultFormat


class ImageGenerationGraphState(TypedDict, total=False):
    request: AiInvokeRequest
    result: dict[str, Any]


def build_image_generation_graph() -> Any:
    graph = StateGraph(ImageGenerationGraphState)
    graph.add_node("generate_image", _generate_image)
    graph.add_edge(START, "generate_image")
    graph.add_edge("generate_image", END)
    return graph.compile()


def _generate_image(state: ImageGenerationGraphState) -> ImageGenerationGraphState:
    artifact = generate_image(state["request"])
    return {
        **state,
        "result": {
            "format": ResultFormat.ARTIFACT.value,
            "payload": {
                "data": artifact.data,
                "contentType": artifact.content_type,
                "filename": artifact.filename,
            },
            "usage": artifact.usage.model_dump(mode="json"),
        },
    }
