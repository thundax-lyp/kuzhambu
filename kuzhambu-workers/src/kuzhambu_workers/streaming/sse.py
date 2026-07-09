import json

from kuzhambu_workers.schemas.stream import StreamEvent, StreamEventType


def encode_sse(event: StreamEvent) -> str:
    return f"event: {event.event.value}\ndata: {_event_data(event)}\n\n"


def _event_data(event: StreamEvent) -> str:
    data = event.model_dump(mode="json", exclude={"event", "extra"}, exclude_none=True)
    if event.event == StreamEventType.COMPLETED and event.result is None:
        data["result"] = None
    extra = event.extra if isinstance(event.extra, dict) else {}
    if isinstance(extra, dict):
        if extra:
            data["extra"] = extra
        data.update(extra)
    return json.dumps(data, ensure_ascii=False, separators=(",", ":"))


def encode_sse_lines(events: list[StreamEvent]) -> str:
    return "".join(encode_sse(event) for event in events)


def sse_comment(message: str) -> str:
    safe_message = message.replace("\n", " ")
    return f": {safe_message}\n\n"
