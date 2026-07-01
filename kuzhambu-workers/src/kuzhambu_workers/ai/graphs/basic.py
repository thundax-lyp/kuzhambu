import json
from typing import Any, TypedDict

from langgraph.graph import END, START, StateGraph

from kuzhambu_workers.schemas.ai import (
    AiCapability,
    AiInvokeRequest,
    GraphExtractionPayload,
    KnowledgeEntityCandidate,
    KnowledgeEntryRef,
    KnowledgeLineageNode,
    KnowledgeRelationCandidate,
    KnowledgeSourceSnippet,
    LineageExtractionPayload,
    RelationExtractionPayload,
    ResultFormat,
)


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
    if request.capability == AiCapability.RELATION_EXTRACTION:
        return RelationExtractionPayload(
            entities=[
                KnowledgeEntityCandidate(
                    name="候选实体",
                    entityType="CONCEPT",
                    description="基于输入文本提取的占位候选实体",
                )
            ],
            relations=[
                KnowledgeRelationCandidate(
                    sourceName="候选实体",
                    targetName="目标实体",
                    relationType="RELATED_TO",
                    evidence="输入文本中的关系线索占位片段",
                )
            ],
            sourceSnippets=[
                KnowledgeSourceSnippet(
                    snippet="输入文本中的关系线索占位片段",
                    sourceRef=_source_ref(request),
                )
            ],
            warnings=["placeholder_only"],
        ).model_dump(mode="json")
    if request.capability == AiCapability.KNOWLEDGE_GRAPH:
        return GraphExtractionPayload(
            entities=[
                KnowledgeEntityCandidate(
                    name="候选实体",
                    entityType="CONCEPT",
                    description="基于输入文本提取的占位图谱实体",
                )
            ],
            relations=[
                KnowledgeRelationCandidate(
                    sourceName="候选实体",
                    targetName="目标实体",
                    relationType="ASSOCIATED_WITH",
                    evidence="输入文本中的图谱关系占位片段",
                )
            ],
            entryRefs=[
                KnowledgeEntryRef(
                    contentType=request.input.contentType,
                    contentId=request.input.contentId,
                    title="输入内容引用占位",
                )
            ],
            warnings=["placeholder_only"],
        ).model_dump(mode="json")
    if request.capability == AiCapability.LINEAGE_EXTRACTION:
        return LineageExtractionPayload(
            nodes=[
                KnowledgeLineageNode(
                    name="始祖节点",
                    nodeType="PERSON",
                    generation=1,
                    gender=None,
                ),
                KnowledgeLineageNode(
                    name="后代节点",
                    nodeType="PERSON",
                    generation=2,
                    gender=None,
                ),
            ],
            relations=[
                KnowledgeRelationCandidate(
                    sourceName="始祖节点",
                    targetName="后代节点",
                    relationType="PARENT_OF",
                    evidence="输入文本中的世系线索占位片段",
                )
            ],
            sourceSnippets=[
                KnowledgeSourceSnippet(
                    snippet="输入文本中的世系线索占位片段",
                    sourceRef=_source_ref(request),
                )
            ],
            warnings=["placeholder_only"],
        ).model_dump(mode="json")
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
    return _text_placeholder_payload(request)


def _text_placeholder_payload(request: AiInvokeRequest) -> str:
    content = _first_payload_text(request)
    if not content:
        content = f"已完成 {request.operation} 占位结果"
    return json.dumps(
        {
            "choices": [
                {
                    "message": {
                        "content": f"[{request.operation}] {content}",
                    }
                }
            ]
        },
        ensure_ascii=False,
    )


def _first_payload_text(request: AiInvokeRequest) -> str:
    input_payload = request.input.payload
    if not input_payload:
        return ""

    for key in ("text", "summary", "query"):
        value = input_payload.get(key)
        if isinstance(value, str):
            stripped = value.strip()
            if stripped:
                return stripped

    for value in input_payload.values():
        if isinstance(value, str):
            stripped = value.strip()
            if stripped:
                return stripped
    return ""


def _source_ref(request: AiInvokeRequest) -> str:
    if request.input.contentId:
        return f"{request.input.contentType}:{request.input.contentId}"
    return request.input.contentType
