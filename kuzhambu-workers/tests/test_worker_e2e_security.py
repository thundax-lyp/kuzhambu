import json
from time import time

from fastapi.testclient import TestClient

from kuzhambu_workers.core.security import sign_request
from kuzhambu_workers.main import app


def test_worker_e2e_rejects_unsigned_request(monkeypatch) -> None:
    _configure(monkeypatch, "kuzhambu-ai")
    response = TestClient(app).post("/internal/ai/invoke", content=_ai_body())

    assert response.status_code == 401
    assert response.json()["error"]["code"] == "MISSING_HEADER"


def test_worker_e2e_rejects_bad_signature(monkeypatch) -> None:
    _configure(monkeypatch, "kuzhambu-ai")
    body = _ai_body()
    headers = _headers(body, "/internal/ai/invoke", "kuzhambu-ai")
    headers["X-Kuzhambu-Signature"] = "bad"

    response = TestClient(app).post("/internal/ai/invoke", content=body, headers=headers)

    assert response.status_code == 401
    assert response.json()["error"]["code"] == "SIGNATURE_MISMATCH"


def test_worker_e2e_rejects_forbidden_service(monkeypatch) -> None:
    _configure(monkeypatch, "kuzhambu-ai")
    body = _render_body("CLASSICS_EXPORT", "HTML")

    response = TestClient(app).post(
        "/internal/render/classics-export",
        content=body,
        headers=_headers(body, "/internal/render/classics-export", "kuzhambu-ai"),
    )

    assert response.status_code == 403
    assert response.json()["error"]["code"] == "PATH_FORBIDDEN"


def test_worker_e2e_render_stream_error_terminates_without_completed(monkeypatch) -> None:
    _configure(monkeypatch, "kuzhambu-classics")
    body = _render_body("SANCAI_SHOWCASE", "HTML")

    response = TestClient(app).post(
        "/internal/render/classics-export/stream",
        content=body,
        headers=_headers(body, "/internal/render/classics-export/stream", "kuzhambu-classics"),
    )

    assert response.status_code == 200
    assert "event: error" in response.text
    assert "event: completed" not in response.text
    assert "RENDER_TYPE_PATH_MISMATCH" in response.text


def _configure(monkeypatch, service: str) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", service)
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")


def _ai_body() -> bytes:
    payload = {
        "requestId": "req-1",
        "traceId": "trace-1",
        "callerDomain": "AI",
        "operation": "E2E",
        "capability": "summary",
        "scope": "SANCAI",
        "modelConfig": {
            "serviceRole": "PRIMARY",
            "apiSource": "OPENAI_COMPATIBLE",
            "baseUrl": "https://model.example/v1",
            "apiKey": "process-only",
            "modelName": "model",
        },
        "prompt": {"messages": [{"role": "user", "content": "hello"}]},
        "input": {"contentType": "TEXT", "payload": {"text": "hello"}},
        "outputSchema": {"type": "text"},
        "options": {"stream": False, "forceJson": False, "locale": "zh-CN"},
    }
    return json.dumps(payload, ensure_ascii=False, separators=(",", ":")).encode()


def _render_body(render_type: str, output_format: str) -> bytes:
    payload = {
        "requestId": "req-1",
        "traceId": "trace-1",
        "callerDomain": "CLASSICS",
        "operation": render_type,
        "renderType": render_type,
        "template": {"templateId": "default", "templateVersion": "2026.06.01"},
        "output": {"format": output_format, "filenameHint": "export.html", "locale": "zh-CN"},
        "input": {
            "snapshotId": "snapshot-1",
            "contentType": f"{render_type}_SNAPSHOT",
            "payload": {
                "metadata": {"title": "三才图会"},
                "catalog": [],
                "entries": [],
                "items": [],
            },
        },
        "options": {"stream": True, "includeMetadata": True},
    }
    return json.dumps(payload, ensure_ascii=False, separators=(",", ":")).encode()


def _headers(body: bytes, path: str, service: str) -> dict[str, str]:
    timestamp = str(int(time() * 1000))
    signature = sign_request("POST", path, timestamp, "req-1", body, "worker-secret")
    return {
        "X-Kuzhambu-Service": service,
        "X-Kuzhambu-Request-Id": "req-1",
        "X-Kuzhambu-Trace-Id": "trace-1",
        "X-Kuzhambu-Timestamp": timestamp,
        "X-Kuzhambu-Signature": signature,
    }
