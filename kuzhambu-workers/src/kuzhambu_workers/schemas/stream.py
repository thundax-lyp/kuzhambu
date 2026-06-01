from enum import Enum
from typing import Any

from pydantic import BaseModel, Field


class StreamEventType(str, Enum):
    STARTED = "started"
    DELTA = "delta"
    PROGRESS = "progress"
    ARTIFACT = "artifact"
    USAGE = "usage"
    WARNING = "warning"
    ERROR = "error"
    COMPLETED = "completed"


class StreamEvent(BaseModel):
    eventId: str
    requestId: str
    traceId: str
    stage: str
    timestamp: str
    event: StreamEventType
    delta: dict[str, Any] | None = None
    progress: dict[str, Any] | None = None
    artifact: dict[str, Any] | None = None
    usage: dict[str, Any] | None = None
    warning: dict[str, Any] | None = None
    error: dict[str, Any] | None = None
    result: dict[str, Any] | None = None
    extra: dict[str, Any] = Field(default_factory=dict)
