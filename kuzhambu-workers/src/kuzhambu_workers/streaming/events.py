from itertools import count
from typing import Any, Protocol

from kuzhambu_workers.schemas.ai import ArtifactReference, FailureStage
from kuzhambu_workers.schemas.stream import StreamEvent, StreamEventType

_EVENT_COUNTER = count(1)


class ArtifactChunkEventInput(Protocol):
    @property
    def artifact_id(self) -> str: ...

    @property
    def format(self) -> str: ...

    @property
    def filename(self) -> str: ...

    @property
    def content_type(self) -> str: ...

    @property
    def encoding(self) -> str: ...

    @property
    def chunk_index(self) -> int: ...

    @property
    def chunk_count(self) -> int: ...

    @property
    def chunk(self) -> str: ...

    @property
    def chunk_sha256(self) -> str: ...

    @property
    def total_size_bytes(self) -> int: ...

    @property
    def sha256(self) -> str: ...


def next_event_id() -> str:
    return f"evt_{next(_EVENT_COUNTER):06d}"


def stream_event(
    event: StreamEventType,
    *,
    request_id: str,
    trace_id: str,
    stage: str,
    timestamp: str,
    event_id: str | None = None,
    **payload: Any,
) -> StreamEvent:
    return StreamEvent(
        eventId=event_id or next_event_id(),
        requestId=request_id,
        traceId=trace_id,
        stage=stage,
        timestamp=timestamp,
        event=event,
        **payload,
    )


def started_event(request_id: str, trace_id: str, timestamp: str) -> StreamEvent:
    return stream_event(
        StreamEventType.STARTED,
        request_id=request_id,
        trace_id=trace_id,
        stage="start",
        timestamp=timestamp,
        extra={"message": "started"},
    )


def completed_event(
    request_id: str,
    trace_id: str,
    timestamp: str,
    result: dict[str, Any],
    usage: dict[str, Any],
) -> StreamEvent:
    return stream_event(
        StreamEventType.COMPLETED,
        request_id=request_id,
        trace_id=trace_id,
        stage="completed",
        timestamp=timestamp,
        result=result,
        usage=usage,
    )


def delta_event(
    request_id: str,
    trace_id: str,
    timestamp: str,
    delta: str,
) -> StreamEvent:
    return stream_event(
        StreamEventType.DELTA,
        request_id=request_id,
        trace_id=trace_id,
        stage="model_stream",
        timestamp=timestamp,
        delta={"text": delta},
    )


def usage_event(
    request_id: str,
    trace_id: str,
    timestamp: str,
    usage: dict[str, Any],
) -> StreamEvent:
    return stream_event(
        StreamEventType.USAGE,
        request_id=request_id,
        trace_id=trace_id,
        stage="usage",
        timestamp=timestamp,
        usage=usage,
    )


def final_state_extra(
    *,
    status: str,
    failure_stage: FailureStage | None,
    fallback_used: bool,
    artifact_reference: ArtifactReference | None,
    error_type: str | None = None,
    error_message: str | None = None,
) -> dict[str, Any]:
    artifact_reference_payload = (
        None if artifact_reference is None else artifact_reference.model_dump(mode="json")
    )
    return {
        "status": status,
        "failureStage": None if failure_stage is None else failure_stage.value,
        "fallbackUsed": fallback_used,
        "artifactReference": artifact_reference_payload,
        "errorType": error_type,
        "errorMessage": error_message,
    }


def artifact_chunk_event(
    *,
    request_id: str,
    trace_id: str,
    timestamp: str,
    chunk: ArtifactChunkEventInput,
) -> StreamEvent:
    return stream_event(
        StreamEventType.ARTIFACT,
        request_id=request_id,
        trace_id=trace_id,
        stage="artifact_chunk",
        timestamp=timestamp,
        artifact={
            "artifactId": chunk.artifact_id,
            "format": chunk.format,
            "filename": chunk.filename,
            "contentType": chunk.content_type,
            "encoding": chunk.encoding,
            "chunkIndex": chunk.chunk_index,
            "chunkCount": chunk.chunk_count,
            "chunk": chunk.chunk,
            "chunkSha256": chunk.chunk_sha256,
            "totalSizeBytes": chunk.total_size_bytes,
            "sha256": chunk.sha256,
        },
    )
