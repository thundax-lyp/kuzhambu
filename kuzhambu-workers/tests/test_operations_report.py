import pytest

from kuzhambu_workers.core.errors import WorkerError
from kuzhambu_workers.render.artifact_store import RequestArtifactStore
from kuzhambu_workers.render.operations_report import render_operations_report
from kuzhambu_workers.schemas.render import RenderRequest


@pytest.mark.asyncio
async def test_operations_report_renders_html() -> None:
    artifact = await render_operations_report(_request("HTML", "report.html"))
    html = artifact.data.decode("utf-8")

    assert artifact.filename == "report.html"
    assert artifact.content_type == "text/html; charset=utf-8"
    assert artifact.size_bytes == len(artifact.data)
    assert artifact.sha256.startswith("sha256:")
    assert artifact.summary.itemCount == 2
    assert "Operations 周报" in html
    assert "备份完成" in html


@pytest.mark.asyncio
async def test_operations_report_renders_pdf_with_browser_pool(tmp_path) -> None:
    renderer = FakePdfRenderer()
    artifact = await render_operations_report(
        _request("PDF", "report.pdf"),
        pdf_renderer=renderer,
    )
    store = RequestArtifactStore("req-1", tmp_path, chunk_bytes=4)
    metadata = store.put_bytes(
        data=artifact.data,
        format=artifact.format.value,
        filename=artifact.filename,
        content_type=artifact.content_type,
    )

    assert artifact.content_type == "application/pdf"
    assert artifact.data == b"%PDF-operations"
    assert "Operations 周报" in renderer.html
    assert store.chunks(metadata.artifact_id)[0].chunk_index == 0


@pytest.mark.asyncio
async def test_operations_report_requires_pdf_renderer() -> None:
    with pytest.raises(WorkerError):
        await render_operations_report(_request("PDF", "report.pdf"))


@pytest.mark.asyncio
async def test_operations_report_rejects_invalid_output_format() -> None:
    with pytest.raises(WorkerError):
        await render_operations_report(_request("ZIP", "report.zip"))


class FakePdfRenderer:
    def __init__(self) -> None:
        self.html = ""

    async def html_to_pdf(self, html: str) -> bytes:
        self.html = html
        return b"%PDF-operations"


def _request(output_format: str, filename: str) -> RenderRequest:
    return RenderRequest.model_validate(
        {
            "requestId": "req-1",
            "traceId": "trace-1",
            "callerDomain": "OPERATIONS",
            "operation": "OPERATIONS_REPORT",
            "renderType": "OPERATIONS_REPORT",
            "template": {
                "templateId": "operations-report-default",
                "templateVersion": "2026.06.01",
            },
            "output": {
                "format": output_format,
                "filenameHint": filename,
                "locale": "zh-CN",
            },
            "input": {
                "snapshotId": "snapshot-1",
                "contentType": "OPERATIONS_REPORT_SNAPSHOT",
                "payload": {
                    "title": "Operations 周报",
                    "period": "2026-W22",
                    "metrics": [
                        {"label": "任务数", "value": 12},
                        {"label": "失败数", "value": 1},
                    ],
                    "records": [
                        {"time": "10:00", "type": "backup", "message": "备份完成"},
                        {"time": "11:00", "type": "job", "message": "任务重试"},
                    ],
                },
            },
            "options": {"stream": False, "includeMetadata": True},
        }
    )
