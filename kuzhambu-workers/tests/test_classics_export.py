import json
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


def test_classics_export_preserves_sancai_image_metadata_without_expanding_csv() -> None:
    payload = {
        "title": "三才图会导出",
        "contentType": "SANCAI_ENTRY",
        "items": [
            {
                "id": 3001,
                "title": "天地",
                "text": "天地玄黄",
                "images": [
                    {
                        "currentUsed": True,
                        "imageId": 8001,
                        "imageType": "ORIGINAL",
                        "originalFilename": "sancai.png",
                        "priority": 2,
                        "size": 2048,
                        "storageObjectId": 7001,
                        "title": "原图",
                    },
                    {
                        "currentUsed": False,
                        "imageId": 8002,
                        "imageType": "GENERATED",
                        "originalFilename": "generated.png",
                        "priority": 1,
                        "size": 1024,
                        "storageObjectId": 7002,
                        "title": "生成图",
                    },
                ],
            },
            {"id": 3002, "title": "无图条目", "text": "宇宙洪荒", "images": []},
        ],
    }

    json_artifact = render_classics_export(_request("JSON", "export.json", payload))
    html_artifact = render_classics_export(_request("HTML", "export.html", payload))
    csv_artifact = render_classics_export(_request("CSV", "export.csv", payload))

    decoded_json = json.loads(json_artifact.data.decode("utf-8"))
    decoded_html = html_artifact.data.decode("utf-8")
    decoded_csv = csv_artifact.data.decode("utf-8")

    assert decoded_json["items"][0]["images"][0]["currentUsed"] is True
    assert decoded_json["items"][0]["images"][1]["currentUsed"] is False
    assert "图片元数据" in decoded_html
    assert "图片ID: 8001" in decoded_html
    assert "当前: 否" in decoded_html
    assert len(decoded_csv.splitlines()) == 3
    assert "8001" not in decoded_csv


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


@pytest.mark.parametrize(
    "payload",
    [
        {
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
        {
            "title": "明代习俗导出",
            "contentType": "MING_CUSTOMS",
            "scopeType": "SELECTED_ITEMS",
            "items": [
                {
                    "id": 500000000001,
                    "title": "岁时礼仪: 元旦朝贺",
                    "text": "## 正旦朝贺",
                    "summary": "记录明代正旦朝贺与家族拜礼。",
                    "visibility": "PUBLIC",
                    "category": "RITUAL",
                }
            ],
        },
    ],
)
@pytest.mark.parametrize("output_format", ["CSV", "JSON", "HTML", "ZIP"])
def test_classics_export_preserves_wangqi_and_ming_item_fields(
    payload: dict, output_format: str
) -> None:
    artifact = render_classics_export(
        _request(output_format, f"export.{output_format.lower()}", payload)
    )

    item = payload["items"][0]
    expected_values = [str(item["id"]), str(item["title"]), str(item["text"])]

    if output_format == "ZIP":
        with ZipFile(BytesIO(artifact.data)) as archive:
            for filename in ["data.csv", "data.json", "index.html"]:
                content = archive.read(filename).decode("utf-8")
                for expected in expected_values:
                    assert expected in content
        return

    decoded = artifact.data.decode("utf-8")
    for expected in expected_values:
        assert expected in decoded


def test_classics_export_rejects_invalid_items() -> None:
    request = _request("CSV", "export.csv")
    request.input.payload["items"] = {"bad": "shape"}

    with pytest.raises(WorkerError):
        render_classics_export(request)


def _request(output_format: str, filename: str, payload: dict | None = None) -> RenderRequest:
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
                "payload": payload
                or {
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
