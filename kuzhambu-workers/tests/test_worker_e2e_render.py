import json
from base64 import b64decode
from hashlib import sha256
from time import time

from fastapi.testclient import TestClient

from kuzhambu_workers.core.security import sign_request
from kuzhambu_workers.main import app


def test_worker_e2e_render_stream_reassembles_artifact(monkeypatch, tmp_path) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-classics")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    monkeypatch.setenv("KUZHAMBU_WORKER_TEMP_DIR", str(tmp_path))
    monkeypatch.setenv("KUZHAMBU_WORKER_ARTIFACT_CHUNK_BYTES", "32")
    body = _body("CLASSICS_EXPORT", "ZIP", stream=True)

    response = TestClient(app).post(
        "/internal/render/classics-export/stream",
        content=body,
        headers=_headers(body, "/internal/render/classics-export/stream"),
    )

    assert response.status_code == 200
    events = _events(response.text)
    artifacts = [event["artifact"] for event in events if event["event"] == "artifact"]
    completed = next(event for event in events if event["event"] == "completed")
    payload = b"".join(b64decode(chunk["chunk"]) for chunk in artifacts)
    chunk_indexes = [chunk["chunkIndex"] for chunk in artifacts]

    assert chunk_indexes == list(range(len(artifacts)))
    assert completed["artifact"]["chunkCount"] == len(artifacts)
    assert f"sha256:{sha256(payload).hexdigest()}" == completed["artifact"]["sha256"]
    assert b"index.html" in payload
    assert not (tmp_path / "req-1").exists()


def _events(text: str) -> list[dict]:
    events = []
    event_name = ""
    for line in text.splitlines():
        if line.startswith("event: "):
            event_name = line.removeprefix("event: ")
        if line.startswith("data: "):
            event = json.loads(line.removeprefix("data: "))
            event["event"] = event_name
            events.append(event)
    return events


def _body(render_type: str, output_format: str, *, stream: bool) -> bytes:
    payload = {
        "requestId": "req-1",
        "traceId": "trace-1",
        "callerDomain": "CLASSICS",
        "operation": render_type,
        "renderType": render_type,
        "template": {"templateId": "default", "templateVersion": "2026.06.01"},
        "output": {"format": output_format, "filenameHint": "export.zip", "locale": "zh-CN"},
        "input": {
            "snapshotId": "snapshot-1",
            "contentType": f"{render_type}_SNAPSHOT",
            "payload": {
                "metadata": {"title": "三才图会"},
                "scopeType": "ALL",
                "entries": [{"id": "entry-1", "title": "第一条", "text": "天地玄黄"}],
                "items": [{"id": "entry-1", "title": "第一条", "text": "天地玄黄"}],
            },
        },
        "options": {"stream": stream, "includeMetadata": True},
    }
    return json.dumps(payload, ensure_ascii=False, separators=(",", ":")).encode()


def _headers(body: bytes, path: str) -> dict[str, str]:
    timestamp = str(int(time() * 1000))
    signature = sign_request("POST", path, timestamp, "req-1", body, "worker-secret")
    return {
        "X-Kuzhambu-Service": "kuzhambu-classics",
        "X-Kuzhambu-Request-Id": "req-1",
        "X-Kuzhambu-Trace-Id": "trace-1",
        "X-Kuzhambu-Timestamp": timestamp,
        "X-Kuzhambu-Signature": signature,
    }
