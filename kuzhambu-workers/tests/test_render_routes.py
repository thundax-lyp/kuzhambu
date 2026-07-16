import json
from base64 import b64decode
from io import BytesIO
from time import time
from zipfile import ZipFile

from fastapi.testclient import TestClient

from kuzhambu_workers.core.security import sign_request
from kuzhambu_workers.main import app


def test_render_sync_returns_inline_artifact(monkeypatch) -> None:
    _configure(monkeypatch, "kuzhambu-classics")
    body = _body("CLASSICS_EXPORT", "ZIP", stream=False)

    response = TestClient(app).post(
        "/internal/render/classics-export",
        content=body,
        headers=_headers(body, "/internal/render/classics-export", "kuzhambu-classics"),
    )

    assert response.status_code == 200
    payload = response.json()
    assert payload["status"] == "SUCCEEDED"
    assert payload["renderType"] == "CLASSICS_EXPORT"
    assert payload["artifact"]["encoding"] == "BASE64"
    assert payload["artifact"]["sha256"].startswith("sha256:")


def test_render_sync_preserves_wangqi_export_payload(monkeypatch) -> None:
    _configure(monkeypatch, "kuzhambu-classics")
    body = _body(
        "CLASSICS_EXPORT",
        "ZIP",
        stream=False,
        payload={
            "title": "王圻文档导出",
            "contentType": "WANGQI_DOCUMENT",
            "scopeType": "SELECTED_ITEMS",
            "items": [
                {
                    "id": 400000000001,
                    "title": "王圻文档",
                    "text": "## 王圻文献正文",
                    "summary": "记录王圻古籍条目。",
                    "visibility": "PUBLIC",
                    "documentTime": "2026-01-01T00:00:00.000+00:00",
                    "sourceFileStorageObjectId": 7001,
                }
            ],
        },
    )

    response = TestClient(app).post(
        "/internal/render/classics-export",
        content=body,
        headers=_headers(body, "/internal/render/classics-export", "kuzhambu-classics"),
    )

    assert response.status_code == 200
    artifact = response.json()["artifact"]
    with ZipFile(BytesIO(b64decode(artifact["content"]))) as archive:
        for filename in ["data.csv", "data.json", "index.html"]:
            content = archive.read(filename).decode("utf-8")
            assert "400000000001" in content
            assert "王圻文档" in content
            assert "## 王圻文献正文" in content


def test_render_stream_returns_progress_artifact_and_completed(monkeypatch, tmp_path) -> None:
    _configure(monkeypatch, "kuzhambu-classics")
    monkeypatch.setenv("KUZHAMBU_WORKER_TEMP_DIR", str(tmp_path))
    monkeypatch.setenv("KUZHAMBU_WORKER_ARTIFACT_CHUNK_BYTES", "16")
    body = _body("CLASSICS_EXPORT", "ZIP", stream=True)

    response = TestClient(app).post(
        "/internal/render/classics-export/stream",
        content=body,
        headers=_headers(body, "/internal/render/classics-export/stream", "kuzhambu-classics"),
    )

    assert response.status_code == 200
    assert response.headers["content-type"].startswith("text/event-stream")
    text = response.text
    assert "event: started" in text
    assert "event: progress" in text
    assert "event: artifact" in text
    assert "event: completed" in text
    assert '"encoding":"BASE64_CHUNK"' in text
    assert not (tmp_path / "req-1").exists()


def test_render_route_rejects_forbidden_service(monkeypatch) -> None:
    _configure(monkeypatch, "kuzhambu-ai")
    body = _body("CLASSICS_EXPORT", "HTML", stream=False)

    response = TestClient(app).post(
        "/internal/render/classics-export",
        content=body,
        headers=_headers(body, "/internal/render/classics-export", "kuzhambu-ai"),
    )

    assert response.status_code == 403
    assert response.json()["error"]["code"] == "PATH_FORBIDDEN"


def test_render_route_rejects_type_path_mismatch(monkeypatch) -> None:
    _configure(monkeypatch, "kuzhambu-classics")
    body = _body("OPERATIONS_REPORT", "HTML", stream=False)

    response = TestClient(app).post(
        "/internal/render/classics-export",
        content=body,
        headers=_headers(body, "/internal/render/classics-export", "kuzhambu-classics"),
    )

    assert response.status_code == 200
    payload = response.json()
    assert payload["status"] == "FAILED"
    assert payload["error"]["code"] == "RENDER_TYPE_PATH_MISMATCH"


def _configure(monkeypatch, service: str) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", service)
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")


def _body(
    render_type: str, output_format: str, *, stream: bool, payload: dict | None = None
) -> bytes:
    body = {
        "requestId": "req-1",
        "traceId": "trace-1",
        "callerDomain": "CLASSICS",
        "operation": render_type,
        "renderType": render_type,
        "template": {
            "templateId": "default",
            "templateVersion": "2026.06.01",
        },
        "output": {
            "format": output_format,
            "filenameHint": "export.zip" if output_format == "ZIP" else "export.html",
            "locale": "zh-CN",
        },
        "input": {
            "snapshotId": "snapshot-1",
            "contentType": f"{render_type}_SNAPSHOT",
            "payload": payload
            or {
                "title": "三才图会导出",
                "metadata": {"title": "三才图会"},
                "catalog": [{"id": "cat-1", "label": "目录"}],
                "entries": [{"id": "entry-1", "title": "第一条", "text": "天地玄黄"}],
                "items": [{"id": "1", "title": "第一条", "text": "天地玄黄"}],
            },
        },
        "options": {"stream": stream, "includeMetadata": True},
    }
    return json.dumps(body, ensure_ascii=False, separators=(",", ":")).encode()


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
