from base64 import b64encode

import pytest

from kuzhambu_workers.core.errors import WorkerError
from kuzhambu_workers.render.sancai_showcase import render_sancai_showcase
from kuzhambu_workers.schemas.render import RenderRequest


def test_sancai_showcase_renders_offline_html() -> None:
    artifact = render_sancai_showcase(_request("HTML"))
    html = artifact.data.decode("utf-8")

    assert artifact.filename == "showcase.html"
    assert artifact.content_type == "text/html; charset=utf-8"
    assert artifact.size_bytes == len(artifact.data)
    assert artifact.sha256.startswith("sha256:")
    assert artifact.summary.itemCount == 2
    assert "三才图会" in html
    assert "天地玄黄" in html
    assert html.index("generated.png") < html.index("original.png")
    assert 'src="generated.png"' in html
    assert 'src="original.png"' in html
    assert 'data-current="true"><img src="original.png"' in html
    assert "暂无配图" in html


def test_sancai_showcase_inlines_assets_and_reports_metadata(tmp_path) -> None:
    image_path = tmp_path / "entry.png"
    image_path.write_bytes(b"image-bytes")
    request = _request("HTML")
    request.input.payload.pop("catalog")
    request.input.payload["catalogs"] = [{"id": 1, "title": "天地", "entryCount": 1}]
    request.input.payload["volumes"] = [{"id": 11, "categoryId": 1, "title": "卷一"}]
    request.input.payload["visibilityRisk"] = {
        "status": "CONTAINS_PRIVATE",
        "privateConfirmed": True,
    }
    request.input.payload["assets"] = [
        {
            "resourceId": "asset-entry-original",
            "temporaryUrl": image_path.as_uri(),
            "filename": "entry.png",
            "contentType": "image/png",
            "sha256": "sha256:sample",
        }
    ]
    request.input.payload["entries"] = [
        {
            "id": 1001,
            "categoryId": 1,
            "volumeId": 11,
            "title": "天地",
            "originalText": "原文",
            "translationText": "译文",
            "tags": ["天文"],
            "images": [
                {
                    "resourceId": "asset-entry-original",
                    "imageType": "ORIGINAL",
                    "caption": "原图",
                    "currentUsed": True,
                    "priority": 1,
                }
            ],
            "visualAsset": {
                "resourceId": "asset-entry-current",
                "visualDescription": "视觉描述",
                "currentUsed": True,
            },
        }
    ]

    artifact = render_sancai_showcase(request)
    html = artifact.data.decode("utf-8")

    assert f"data:image/png;base64,{b64encode(b'image-bytes').decode('ascii')}" in html
    assert str(image_path.as_uri()) not in html
    assert "搜索条目标题、正文或译文" in html
    assert "全部门类" in html
    assert "全部卷" in html
    assert "紧凑浏览" in html
    assert artifact.summary.itemCount == 1
    assert artifact.summary.metadata["catalogCount"] == 1
    assert artifact.summary.metadata["volumeCount"] == 1
    assert artifact.summary.metadata["assetCount"] == 1
    assert artifact.summary.metadata["visibilityRiskStatus"] == "CONTAINS_PRIVATE"


def test_sancai_showcase_rejects_non_html_output() -> None:
    with pytest.raises(WorkerError):
        render_sancai_showcase(_request("PDF"))


def test_sancai_showcase_rejects_non_snapshot_lists() -> None:
    request = _request("HTML")
    request.input.payload["entries"] = {"bad": "shape"}

    with pytest.raises(WorkerError):
        render_sancai_showcase(request)


def _request(output_format: str) -> RenderRequest:
    return RenderRequest.model_validate(
        {
            "requestId": "req-1",
            "traceId": "trace-1",
            "callerDomain": "CLASSICS",
            "operation": "SANCAI_SHOWCASE",
            "renderType": "SANCAI_SHOWCASE",
            "template": {
                "templateId": "sancai-showcase-default",
                "templateVersion": "2026.06.01",
            },
            "output": {
                "format": output_format,
                "filenameHint": "showcase.html",
                "locale": "zh-CN",
            },
            "input": {
                "snapshotId": "snapshot-1",
                "contentType": "SANCAI_SHOWCASE_SNAPSHOT",
                "payload": {
                    "metadata": {
                        "title": "三才图会",
                        "edition": "sample",
                    },
                    "catalog": [{"id": "cat-1", "label": "天文卷"}],
                    "entries": [
                        {
                            "id": "entry-1",
                            "title": "第一条",
                            "text": "天地玄黄",
                            "images": [
                                {
                                    "imageId": 8001,
                                    "src": "original.png",
                                    "alt": "原图",
                                    "caption": "当前图片",
                                    "currentUsed": True,
                                    "priority": 2,
                                },
                                {
                                    "imageId": 8002,
                                    "src": "generated.png",
                                    "alt": "生成图",
                                    "caption": "生成图片",
                                    "currentUsed": False,
                                    "priority": 1,
                                },
                            ],
                        },
                        {"id": "entry-2", "title": "第二条", "body": "宇宙洪荒"},
                    ],
                    "assets": [{"id": "asset-1", "name": "inline image"}],
                },
            },
            "options": {"stream": False, "includeMetadata": True},
        }
    )
