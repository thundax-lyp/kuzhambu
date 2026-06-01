import json

from kuzhambu_workers.schemas.stream import StreamEventType
from kuzhambu_workers.streaming.events import completed_event, started_event, stream_event
from kuzhambu_workers.streaming.sse import encode_sse, encode_sse_lines, sse_comment


def test_encode_sse_outputs_event_and_json_data() -> None:
    event = stream_event(
        StreamEventType.DELTA,
        event_id="evt-1",
        request_id="req-1",
        trace_id="trace-1",
        stage="model_stream",
        timestamp="2026-06-01T10:00:00.000Z",
        delta={"text": "白话"},
    )

    encoded = encode_sse(event)

    assert encoded.startswith("event: delta\n")
    assert encoded.endswith("\n\n")
    data = json.loads(encoded.split("data: ", 1)[1])
    assert data == {
        "eventId": "evt-1",
        "requestId": "req-1",
        "traceId": "trace-1",
        "stage": "model_stream",
        "timestamp": "2026-06-01T10:00:00.000Z",
        "delta": {"text": "白话"},
    }


def test_started_event_contains_common_fields_and_message() -> None:
    event = started_event("req-1", "trace-1", "2026-06-01T10:00:00.000Z")

    data = json.loads(encode_sse(event).split("data: ", 1)[1])

    assert data["eventId"].startswith("evt_")
    assert data["requestId"] == "req-1"
    assert data["traceId"] == "trace-1"
    assert data["stage"] == "start"
    assert data["timestamp"] == "2026-06-01T10:00:00.000Z"
    assert data["message"] == "started"


def test_completed_event_contains_result_and_usage() -> None:
    event = completed_event(
        "req-1",
        "trace-1",
        "2026-06-01T10:00:05.000Z",
        {"format": "TEXT", "payload": "完整最终结果"},
        {"latencyMs": 5000},
    )

    data = json.loads(encode_sse(event).split("data: ", 1)[1])

    assert data["stage"] == "completed"
    assert data["result"] == {"format": "TEXT", "payload": "完整最终结果"}
    assert data["usage"] == {"latencyMs": 5000}


def test_encode_sse_lines_concatenates_events() -> None:
    events = [
        stream_event(
            StreamEventType.WARNING,
            event_id="evt-1",
            request_id="req-1",
            trace_id="trace-1",
            stage="warn",
            timestamp="2026-06-01T10:00:00.000Z",
            warning={"message": "warn"},
        ),
        stream_event(
            StreamEventType.ERROR,
            event_id="evt-2",
            request_id="req-1",
            trace_id="trace-1",
            stage="error",
            timestamp="2026-06-01T10:00:01.000Z",
            error={"message": "error"},
        ),
    ]

    encoded = encode_sse_lines(events)

    assert encoded.count("event: ") == 2
    assert "event: warning" in encoded
    assert "event: error" in encoded


def test_sse_comment_sanitizes_newlines() -> None:
    assert sse_comment("hello\nworld") == ": hello world\n\n"
