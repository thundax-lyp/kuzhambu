from collections.abc import Iterable
from dataclasses import dataclass
from typing import Any, cast

from kuzhambu_workers.ai.graphs.basic import build_basic_graph
from kuzhambu_workers.core.errors import unsupported_capability
from kuzhambu_workers.schemas.ai import AiCapability, AiInvokeRequest

CANONICAL_CAPABILITIES = tuple(capability.value for capability in AiCapability)


@dataclass(frozen=True)
class GraphRegistry:
    graphs: dict[AiCapability, Any]

    @classmethod
    def build_default(cls) -> "GraphRegistry":
        return cls(graphs={capability: build_basic_graph() for capability in AiCapability})

    def capabilities(self) -> Iterable[str]:
        return (capability.value for capability in self.graphs)

    def invoke(self, request: AiInvokeRequest) -> dict[str, Any]:
        graph = self.graphs.get(request.capability)
        if graph is None:
            raise unsupported_capability(request.capability.value)
        state = graph.invoke({"request": request})
        return cast(dict[str, Any], state["result"])
