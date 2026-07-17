from collections.abc import Iterable
from dataclasses import dataclass, field
from typing import Any, cast

from kuzhambu_workers.ai.graphs.basic import build_basic_graph
from kuzhambu_workers.ai.graphs.image_generation import build_image_generation_graph
from kuzhambu_workers.ai.graphs.text import build_text_graph
from kuzhambu_workers.ai.openai_compatible import OpenAiChatCompletionChunk
from kuzhambu_workers.core.errors import unsupported_capability
from kuzhambu_workers.schemas.ai import AiCapability, AiInvokeRequest

CANONICAL_CAPABILITIES = tuple(capability.value for capability in AiCapability)

CLASSICS_TEXT_USECASES = frozenset(
    {
        "CLASSICS_SANCAI_TRANSLATE",
        "CLASSICS_SANCAI_SUMMARY",
        "CLASSICS_WANGQI_SUMMARY",
        "CLASSICS_MING_CUSTOMS_SUMMARY",
        "OPENAI_COMPATIBLE_CHAT_COMPLETION",
    }
)


@dataclass(frozen=True)
class GraphRegistry:
    graphs: dict[AiCapability, Any]
    classics_text_graphs: dict[str, Any] = field(default_factory=dict)

    @classmethod
    def build_default(cls) -> "GraphRegistry":
        text_graph = build_text_graph()
        graphs = {capability: build_basic_graph() for capability in AiCapability}
        graphs[AiCapability.IMAGE_GEN] = build_image_generation_graph()
        return cls(
            graphs=graphs,
            classics_text_graphs={operation: text_graph for operation in CLASSICS_TEXT_USECASES},
        )

    def capabilities(self) -> Iterable[str]:
        return (capability.value for capability in self.graphs)

    def invoke(self, request: AiInvokeRequest) -> dict[str, Any]:
        graph = self.classics_text_graphs.get(request.operation) or self.graphs.get(
            request.capability
        )
        if graph is None:
            raise unsupported_capability(request.capability.value)
        state = graph.invoke({"request": request})
        return cast(dict[str, Any], state["result"])

    def stream_chat_completion(
        self,
        request: AiInvokeRequest,
        *,
        response_format: dict[str, Any] | None = None,
    ) -> Iterable[OpenAiChatCompletionChunk]:
        graph = self.classics_text_graphs.get(request.operation) or self.graphs.get(
            request.capability
        )
        if graph is None:
            raise unsupported_capability(request.capability.value)
        state = graph.invoke({"request": request, "streamResponseFormat": response_format})
        result = state["result"]
        chunks = result["chunks"]
        return cast(Iterable[OpenAiChatCompletionChunk], chunks)
