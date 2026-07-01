from collections.abc import Iterable
from dataclasses import dataclass, field
from typing import Any, cast

from kuzhambu_workers.ai.graphs.basic import build_basic_graph
from kuzhambu_workers.ai.graphs.text import build_text_graph
from kuzhambu_workers.core.errors import unsupported_capability
from kuzhambu_workers.schemas.ai import AiCapability, AiInvokeRequest

CANONICAL_CAPABILITIES = tuple(capability.value for capability in AiCapability)

CLASSICS_TEXT_USECASES = frozenset(
    {
        "CLASSICS_SANCAI_TRANSLATE",
        "CLASSICS_SANCAI_SUMMARY",
        "CLASSICS_WANGQI_SUMMARY",
        "CLASSICS_MING_CUSTOMS_SUMMARY",
    }
)


@dataclass(frozen=True)
class GraphRegistry:
    graphs: dict[AiCapability, Any]
    classics_text_graphs: dict[str, Any] = field(default_factory=dict)

    @classmethod
    def build_default(cls) -> "GraphRegistry":
        text_graph = build_text_graph()
        return cls(
            graphs={capability: build_basic_graph() for capability in AiCapability},
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
