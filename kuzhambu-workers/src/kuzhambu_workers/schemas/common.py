from enum import Enum
from typing import Any

from pydantic import BaseModel, Field

from kuzhambu_workers.core.errors import WorkerErrorType


class WorkerStatus(str, Enum):
    SUCCEEDED = "SUCCEEDED"
    FAILED = "FAILED"
    PARTIAL = "PARTIAL"


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
