from itertools import count
from typing import Any

from kuzhambu_workers.render.artifact_store import ArtifactChunk
from kuzhambu_workers.schemas.stream import StreamEvent, StreamEventType

_EVENT_COUNTER = count(1)


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


def artifact_chunk_event(
    *,
    request_id: str,
    trace_id: str,
    timestamp: str,
    chunk: ArtifactChunk,
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
