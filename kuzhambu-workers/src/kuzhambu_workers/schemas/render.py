from enum import Enum
from typing import Any

from pydantic import BaseModel, Field, model_validator

from kuzhambu_workers.schemas.common import UsageSummary, WorkerErrorPayload, WorkerStatus


class RenderType(str, Enum):
    CLASSICS_EXPORT = "CLASSICS_EXPORT"
    OPERATIONS_REPORT = "OPERATIONS_REPORT"


class RenderOutputFormat(str, Enum):
    CSV = "CSV"
    JSON = "JSON"
    HTML = "HTML"
    ZIP = "ZIP"
    PDF = "PDF"


class ArtifactEncoding(str, Enum):
    BASE64 = "BASE64"
    TEXT = "TEXT"
    STREAM = "STREAM"


class RenderTemplate(BaseModel):
    templateId: str
    templateVersion: str
    parameters: dict[str, Any] = Field(default_factory=dict)


class RenderOutput(BaseModel):
    format: RenderOutputFormat
    filenameHint: str
    locale: str = "zh-CN"


class RenderInput(BaseModel):
    snapshotId: str | None = None
    contentType: str
    payload: dict[str, Any] = Field(default_factory=dict)


class RenderOptions(BaseModel):
    stream: bool = False
    includeMetadata: bool = True


class RenderRequest(BaseModel):
    requestId: str
    traceId: str
    callerDomain: str
    operation: str
    renderType: RenderType
    template: RenderTemplate
    output: RenderOutput
    input: RenderInput
    options: RenderOptions = Field(default_factory=RenderOptions)


class RenderArtifact(BaseModel):
    format: RenderOutputFormat
    filename: str
    contentType: str
    encoding: ArtifactEncoding
    content: str | None = None
    artifactId: str | None = None
    chunkCount: int | None = None
    sizeBytes: int
    sha256: str

    @model_validator(mode="after")
    def validate_encoding_contract(self) -> "RenderArtifact":
        if self.encoding == ArtifactEncoding.STREAM:
            if not self.artifactId:
                raise ValueError("STREAM artifact requires artifactId")
            if self.content is not None:
                raise ValueError("STREAM artifact must not include inline content")
        elif self.content is None:
            raise ValueError("inline artifact requires content")
        return self


class RenderSummary(BaseModel):
    itemCount: int = 0
    warnings: list[dict[str, Any]] = Field(default_factory=list)
    metadata: dict[str, Any] = Field(default_factory=dict)


class RenderResponse(BaseModel):
    requestId: str
    traceId: str
    status: WorkerStatus
    renderType: RenderType
    artifact: RenderArtifact | None = None
    summary: RenderSummary = Field(default_factory=RenderSummary)
    usage: UsageSummary = Field(default_factory=UsageSummary)
    error: WorkerErrorPayload | None = None
