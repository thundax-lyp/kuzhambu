from base64 import b64decode
from io import BytesIO
from zipfile import ZipFile

import pytest

from kuzhambu_workers.core.errors import WorkerError
from kuzhambu_workers.render.artifact_store import RequestArtifactStore
from kuzhambu_workers.render.classics_export import render_classics_export
from kuzhambu_workers.schemas.render import RenderRequest


def test_classics_export_renders_csv_with_metadata() -> None:
    artifact = render_classics_export(_request("CSV", "export.csv"))

    assert artifact.filename == "export.csv"
    assert artifact.content_type == "text/csv; charset=utf-8"
    assert artifact.size_bytes == len(artifact.data)
    assert artifact.sha256.startswith("sha256:")
    assert artifact.summary.itemCount == 2
    assert "第一条" in artifact.data.decode("utf-8")


def test_classics_export_renders_json() -> None:
    artifact = render_classics_export(_request("JSON", "export.json"))

    decoded = artifact.data.decode("utf-8")
    assert artifact.content_type == "application/json"
    assert '"title":"三才图会导出"' in decoded
    assert '"items":[' in decoded


def test_classics_export_renders_html_and_can_chunk(tmp_path) -> None:
    artifact = render_classics_export(_request("HTML", "export.html"))
    store = RequestArtifactStore("req-1", tmp_path, chunk_bytes=12)
    metadata = store.put_bytes(
        data=artifact.data,
        format=artifact.format.value,
        filename=artifact.filename,
        content_type=artifact.content_type,
    )

    chunks = store.chunks(metadata.artifact_id)

    assert "<!doctype html>" in artifact.data.decode("utf-8")
    assert chunks[0].chunk_index == 0
    assert b"".join(b64decode(chunk.chunk) for chunk in chunks) == artifact.data


def test_classics_export_renders_zip_and_can_chunk(tmp_path) -> None:
    artifact = render_classics_export(_request("ZIP", "export.zip"))
    store = RequestArtifactStore("req-1", tmp_path, chunk_bytes=64)
    metadata = store.put_bytes(
        data=artifact.data,
        format=artifact.format.value,
        filename=artifact.filename,
        content_type=artifact.content_type,
    )

    chunks = store.chunks(metadata.artifact_id)

    with ZipFile(BytesIO(artifact.data)) as archive:
        assert sorted(archive.namelist()) == ["data.csv", "data.json", "index.html"]
    assert chunks[-1].chunk_index == metadata.chunk_count - 1


def test_classics_export_rejects_invalid_items() -> None:
    request = _request("CSV", "export.csv")
    request.input.payload["items"] = {"bad": "shape"}

    with pytest.raises(WorkerError):
        render_classics_export(request)


def _request(output_format: str, filename: str) -> RenderRequest:
    return RenderRequest.model_validate(
        {
            "requestId": "req-1",
            "traceId": "trace-1",
            "callerDomain": "CLASSICS",
            "operation": "CLASSICS_EXPORT",
            "renderType": "CLASSICS_EXPORT",
            "template": {
                "templateId": "classics-export-default",
                "templateVersion": "2026.06.01",
            },
            "output": {
                "format": output_format,
                "filenameHint": filename,
                "locale": "zh-CN",
            },
            "input": {
                "snapshotId": "snapshot-1",
                "contentType": "CLASSICS_EXPORT_SNAPSHOT",
                "payload": {
                    "title": "三才图会导出",
                    "items": [
                        {"id": "1", "title": "第一条", "text": "天地玄黄"},
                        {"id": "2", "title": "第二条", "text": "宇宙洪荒"},
                    ],
                },
            },
            "options": {"stream": False, "includeMetadata": True},
        }
    )
