import pytest

from kuzhambu_workers.ai.graph_registry import CANONICAL_CAPABILITIES, GraphRegistry
from kuzhambu_workers.ai.openai_compatible import (
    OpenAiChatCompletionChunk,
    OpenAiChatCompletionResult,
)
from kuzhambu_workers.core.errors import WorkerError
from kuzhambu_workers.schemas.ai import AiCapability, AiInvokeRequest
from kuzhambu_workers.schemas.common import UsageSummary


def test_registry_contains_all_canonical_capabilities() -> None:
    registry = GraphRegistry.build_default()

    assert tuple(registry.capabilities()) == CANONICAL_CAPABILITIES
    assert set(CANONICAL_CAPABILITIES) == {capability.value for capability in AiCapability}


def test_registry_invokes_text_graph_for_classics_translate(monkeypatch) -> None:
    monkeypatch.setattr(
        "kuzhambu_workers.ai.graphs.text.invoke_chat_completion",
        lambda request: _model_result(f"[{request.operation}] hello"),
    )
    registry = GraphRegistry.build_default()
    request = AiInvokeRequest.model_validate(
        _request_payload("translate", operation="CLASSICS_SANCAI_TRANSLATE")
    )

    result = registry.invoke(request)

    assert result["format"] == "TEXT"
    assert result["payload"] == "[CLASSICS_SANCAI_TRANSLATE] hello"


@pytest.mark.parametrize(
    ("capability", "expected_keys"),
    [
        ("relation_extraction", {"entities", "relations", "sourceSnippets", "warnings"}),
        ("knowledge_graph", {"entities", "relations", "entryRefs", "warnings"}),
        ("lineage_extraction", {"nodes", "relations", "sourceSnippets", "warnings"}),
    ],
)
def test_registry_returns_stable_payload_shape_for_knowledge_capabilities(
    monkeypatch,
    capability: str,
    expected_keys: set[str],
) -> None:
    monkeypatch.setattr(
        "kuzhambu_workers.ai.graphs.basic.invoke_chat_completion",
        lambda request: _model_result(_knowledge_content(request.capability)),
    )
    registry = GraphRegistry.build_default()
    request = AiInvokeRequest.model_validate(_request_payload(capability))

    result = registry.invoke(request)

    assert result["format"] == "STRUCTURED"
    assert set(result["payload"]) == expected_keys


def test_registry_rejects_unregistered_capability() -> None:
    registry = GraphRegistry(graphs={}, classics_text_graphs={})
    request = AiInvokeRequest.model_validate(_request_payload("translate"))

    with pytest.raises(WorkerError) as raised:
        registry.invoke(request)

    assert raised.value.code == "UNSUPPORTED_CAPABILITY"


def test_registry_streams_chat_completion_through_registered_graph(monkeypatch) -> None:
    captured = {"called": False}

    def fake_chunks(request, *, response_format=None):
        captured["called"] = True
        assert response_format == {"type": "json_object"}
        return iter([OpenAiChatCompletionChunk(delta="hello", usage=None, finish_reason="stop")])

    monkeypatch.setattr(
        "kuzhambu_workers.ai.graph_registry.iter_chat_completion_chunks",
        fake_chunks,
    )
    registry = GraphRegistry.build_default()
    request = AiInvokeRequest.model_validate(_request_payload("answer_generation"))

    chunks = list(
        registry.stream_chat_completion(
            request,
            response_format={"type": "json_object"},
        )
    )

    assert captured["called"] is True
    assert chunks[0].delta == "hello"


def _model_result(content: str) -> OpenAiChatCompletionResult:
    return OpenAiChatCompletionResult(
        content=content,
        usage=UsageSummary(latencyMs=12, inputTokens=3, outputTokens=4),
        raw_finish_reason="stop",
    )


def _knowledge_content(capability: AiCapability) -> str:
    if capability == AiCapability.RELATION_EXTRACTION:
        return '{"entities":[],"relations":[],"sourceSnippets":[],"warnings":[]}'
    if capability == AiCapability.KNOWLEDGE_GRAPH:
        return '{"entities":[],"relations":[],"entryRefs":[],"warnings":[]}'
    if capability == AiCapability.LINEAGE_EXTRACTION:
        return '{"nodes":[],"relations":[],"sourceSnippets":[],"warnings":[]}'
    return '{"items":[]}'


def _request_payload(capability: str, *, operation: str = "TEST") -> dict:
    return {
        "requestId": "req-1",
        "traceId": "trace-1",
        "callerDomain": "AI",
        "operation": operation,
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
