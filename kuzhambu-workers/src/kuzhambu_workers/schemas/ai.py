from enum import Enum
from typing import Any

from pydantic import BaseModel, Field

from kuzhambu_workers.schemas.common import UsageSummary, WorkerErrorPayload, WorkerStatus


class AiCapability(str, Enum):
    TRANSLATE = "translate"
    SUMMARY = "summary"
    VERSION_SUMMARY = "version_summary"
    TAGS = "tags"
    QA = "qa"
    IMAGE_ANALYSIS = "image_analysis"
    IMAGE_GEN = "image_gen"
    VISUAL = "visual"
    FUSION = "fusion"
    SPLIT = "split"
    QUERY_UNDERSTANDING = "query_understanding"
    ANSWER_GENERATION = "answer_generation"
    KNOWLEDGE_GRAPH = "knowledge_graph"
    RELATION_EXTRACTION = "relation_extraction"
    LINEAGE_EXTRACTION = "lineage_extraction"
    PROMPT_SUGGESTION = "prompt_suggestion"


class ResultFormat(str, Enum):
    TEXT = "TEXT"
    MARKDOWN = "MARKDOWN"
    JSON = "JSON"
    STRUCTURED = "STRUCTURED"
    ARTIFACT = "ARTIFACT"


class FailureStage(str, Enum):
    REQUEST_VALIDATE = "REQUEST_VALIDATE"
    WORKER_REQUEST = "WORKER_REQUEST"
    WORKER_STREAM = "WORKER_STREAM"
    WORKER_RESULT = "WORKER_RESULT"
    ARTIFACT_DOWNLOAD = "ARTIFACT_DOWNLOAD"
    STORAGE_PERSIST = "STORAGE_PERSIST"
    CANDIDATE_PERSIST = "CANDIDATE_PERSIST"


class MessageRole(str, Enum):
    SYSTEM = "system"
    USER = "user"
    ASSISTANT = "assistant"
    TOOL = "tool"


class AiModelConfig(BaseModel):
    serviceRole: str
    apiSource: str
    baseUrl: str
    apiKey: str
    modelName: str
    capabilityTags: list[str] = Field(default_factory=list)
    parameters: dict[str, Any] = Field(default_factory=dict)
    timeoutMs: int = 60_000


class AiMessage(BaseModel):
    role: MessageRole
    content: str | list[dict[str, Any]]


class AiPrompt(BaseModel):
    templateId: str | None = None
    promptVersionId: str | None = None
    versionNo: int | None = None
    messages: list[AiMessage]
    variables: dict[str, Any] = Field(default_factory=dict)
    promptHash: str | None = None


class AiInput(BaseModel):
    contentType: str
    contentId: str | None = None
    payload: dict[str, Any] = Field(default_factory=dict)


class AiOutputSchema(BaseModel):
    type: str
    schema_: dict[str, Any] | None = Field(default=None, alias="schema")


class AiOptions(BaseModel):
    stream: bool = False
    forceJson: bool = False
    locale: str = "zh-CN"


class AiInvokeRequest(BaseModel):
    requestId: str
    traceId: str
    callerDomain: str
    operation: str
    capability: AiCapability
    scope: str
    modelConfig: AiModelConfig
    prompt: AiPrompt
    input: AiInput
    outputSchema: AiOutputSchema
    options: AiOptions = Field(default_factory=AiOptions)


class AiResult(BaseModel):
    format: ResultFormat
    payload: Any


class ArtifactReference(BaseModel):
    artifactId: str
    downloadPath: str
    contentType: str
    filename: str
    sizeBytes: int
    sha256: str
    expiresAt: str


class KnowledgeEntityCandidate(BaseModel):
    name: str
    entityType: str
    description: str | None = None


class KnowledgeRelationCandidate(BaseModel):
    sourceName: str
    targetName: str
    relationType: str
    evidence: str | None = None


class KnowledgeSourceSnippet(BaseModel):
    snippet: str
    sourceRef: str | None = None


class KnowledgeEntryRef(BaseModel):
    contentType: str
    contentId: str | None = None
    title: str | None = None


class KnowledgeLineageNode(BaseModel):
    name: str
    nodeType: str
    generation: int | None = None
    gender: str | None = None


class RelationExtractionPayload(BaseModel):
    entities: list[KnowledgeEntityCandidate] = Field(default_factory=list)
    relations: list[KnowledgeRelationCandidate] = Field(default_factory=list)
    sourceSnippets: list[KnowledgeSourceSnippet] = Field(default_factory=list)
    warnings: list[str] = Field(default_factory=list)


class GraphExtractionPayload(BaseModel):
    entities: list[KnowledgeEntityCandidate] = Field(default_factory=list)
    relations: list[KnowledgeRelationCandidate] = Field(default_factory=list)
    entryRefs: list[KnowledgeEntryRef] = Field(default_factory=list)
    warnings: list[str] = Field(default_factory=list)


class LineageExtractionPayload(BaseModel):
    nodes: list[KnowledgeLineageNode] = Field(default_factory=list)
    relations: list[KnowledgeRelationCandidate] = Field(default_factory=list)
    sourceSnippets: list[KnowledgeSourceSnippet] = Field(default_factory=list)
    warnings: list[str] = Field(default_factory=list)


class AiInvokeResponse(BaseModel):
    requestId: str
    traceId: str
    status: WorkerStatus
    capability: AiCapability
    result: AiResult | None = None
    usage: UsageSummary = Field(default_factory=UsageSummary)
    # Batch callers aggregate each unit by requestId + traceId and rely on
    # failureStage/errorType/errorMessage for per-unit failure summaries.
    failureStage: FailureStage | None = None
    fallbackUsed: bool = False
    artifactReference: ArtifactReference | None = None
    warnings: list[dict[str, Any]] = Field(default_factory=list)
    error: WorkerErrorPayload | None = None
    errorType: str | None = None
    errorMessage: str | None = None
