from enum import Enum
from typing import Any

from pydantic import BaseModel, Field


class WorkerStatus(str, Enum):
    SUCCEEDED = "SUCCEEDED"
    FAILED = "FAILED"
    PARTIAL = "PARTIAL"


class WorkerErrorType(str, Enum):
    WORKER_PROTOCOL_FAILURE = "WORKER_PROTOCOL_FAILURE"
    WORKER_TIMEOUT = "WORKER_TIMEOUT"
    WORKER_UNAVAILABLE = "WORKER_UNAVAILABLE"
    MODEL_TRANSPORT_FAILURE = "MODEL_TRANSPORT_FAILURE"
    MODEL_SEMANTIC_FAILURE = "MODEL_SEMANTIC_FAILURE"
    OUTPUT_FORMAT_FAILURE = "OUTPUT_FORMAT_FAILURE"
    IMAGE_INPUT_FAILURE = "IMAGE_INPUT_FAILURE"
    RENDER_INPUT_FAILURE = "RENDER_INPUT_FAILURE"
    RENDER_TEMPLATE_FAILURE = "RENDER_TEMPLATE_FAILURE"
    RENDER_OUTPUT_FAILURE = "RENDER_OUTPUT_FAILURE"
    UNSUPPORTED_CAPABILITY = "UNSUPPORTED_CAPABILITY"
    INTERNAL_FAILURE = "INTERNAL_FAILURE"


class WorkerErrorPayload(BaseModel):
    type: WorkerErrorType
    code: str
    message: str
    retryable: bool = False
    detail: dict[str, Any] = Field(default_factory=dict)


class UsageSummary(BaseModel):
    latencyMs: int = 0
    inputTokens: int = 0
    outputTokens: int = 0
    costAmount: str = "0.00"
