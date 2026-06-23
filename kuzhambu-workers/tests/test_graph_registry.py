import pytest

from kuzhambu_workers.ai.graph_registry import CANONICAL_CAPABILITIES, GraphRegistry
from kuzhambu_workers.core.errors import WorkerError
from kuzhambu_workers.schemas.ai import AiCapability, AiInvokeRequest


def test_registry_contains_all_canonical_capabilities() -> None:
    registry = GraphRegistry.build_default()

    assert tuple(registry.capabilities()) == CANONICAL_CAPABILITIES
    assert set(CANONICAL_CAPABILITIES) == {capability.value for capability in AiCapability}


def test_registry_invokes_basic_graph_for_single_prompt() -> None:
    registry = GraphRegistry.build_default()
    request = AiInvokeRequest.model_validate(_request_payload("translate"))

    result = registry.invoke(request)

    assert result == {"format": "TEXT", "payload": ""}


def test_registry_returns_structured_placeholder_for_structured_capability() -> None:
    registry = GraphRegistry.build_default()
    request = AiInvokeRequest.model_validate(_request_payload("tags"))

    result = registry.invoke(request)

    assert result == {
        "format": "STRUCTURED",
        "payload": {"capability": "tags", "placeholder": True},
    }


@pytest.mark.parametrize(
    ("capability", "expected_keys"),
    [
        ("relation_extraction", {"entities", "relations", "sourceSnippets", "warnings"}),
        ("knowledge_graph", {"entities", "relations", "entryRefs", "warnings"}),
        ("lineage_extraction", {"nodes", "relations", "sourceSnippets", "warnings"}),
    ],
)
def test_registry_returns_stable_payload_shape_for_knowledge_capabilities(
    capability: str,
    expected_keys: set[str],
) -> None:
    registry = GraphRegistry.build_default()
    request = AiInvokeRequest.model_validate(_request_payload(capability))

    result = registry.invoke(request)

    assert result["format"] == "STRUCTURED"
    assert set(result["payload"]) == expected_keys


def test_registry_rejects_unregistered_capability() -> None:
    registry = GraphRegistry(graphs={})
    request = AiInvokeRequest.model_validate(_request_payload("translate"))

    with pytest.raises(WorkerError) as raised:
        registry.invoke(request)

    assert raised.value.code == "UNSUPPORTED_CAPABILITY"


def _request_payload(capability: str) -> dict:
    return {
        "requestId": "req-1",
        "traceId": "trace-1",
        "callerDomain": "AI",
        "operation": "TEST",
        "capability": capability,
        "scope": "SANCAI",
        "modelConfig": {
            "serviceRole": "PRIMARY",
            "apiSource": "OPENAI_COMPATIBLE",
            "baseUrl": "https://model.example/v1",
            "apiKey": "process-only",
            "modelName": "model",
            "capabilityTags": ["text"],
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
            ],
        },
        "input": {
            "contentType": "SANCAI_ENTRY",
            "payload": {"text": "hello"},
        },
        "outputSchema": {"type": "text"},
    }
