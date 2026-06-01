import pytest
from pydantic import ValidationError

from kuzhambu_workers.schemas.common import WorkerStatus
from kuzhambu_workers.schemas.render import (
    ArtifactEncoding,
    RenderOutputFormat,
    RenderRequest,
    RenderResponse,
    RenderType,
)


def test_render_request_accepts_contract_shape() -> None:
    request = RenderRequest.model_validate(_request_payload("CLASSICS_EXPORT", "ZIP"))

    assert request.requestId == "req-1"
    assert request.traceId == "trace-1"
    assert request.renderType == RenderType.CLASSICS_EXPORT
    assert request.output.format == RenderOutputFormat.ZIP
    assert request.options.stream is False


@pytest.mark.parametrize(
    "render_type",
    ["CLASSICS_EXPORT", "SANCAI_SHOWCASE", "OPERATIONS_REPORT"],
)
def test_render_type_contains_supported_values(render_type: str) -> None:
    request = RenderRequest.model_validate(_request_payload(render_type, "HTML"))

    assert request.renderType.value == render_type


@pytest.mark.parametrize("output_format", ["CSV", "JSON", "HTML", "ZIP", "PDF"])
def test_output_format_contains_supported_values(output_format: str) -> None:
    request = RenderRequest.model_validate(_request_payload("CLASSICS_EXPORT", output_format))

    assert request.output.format.value == output_format


def test_render_request_rejects_unknown_type_or_format() -> None:
    with pytest.raises(ValidationError):
        RenderRequest.model_validate(_request_payload("DOC_EXPORT", "ZIP"))

    with pytest.raises(ValidationError):
        RenderRequest.model_validate(_request_payload("CLASSICS_EXPORT", "EXE"))


def test_render_response_accepts_inline_artifact() -> None:
    response = RenderResponse.model_validate(
        {
            "requestId": "req-1",
            "traceId": "trace-1",
            "status": "SUCCEEDED",
            "renderType": "CLASSICS_EXPORT",
            "artifact": {
                "format": "HTML",
                "filename": "export.html",
                "contentType": "text/html; charset=utf-8",
                "encoding": "TEXT",
                "content": "<html></html>",
                "sizeBytes": 13,
                "sha256": "sha256:inline",
            },
            "summary": {"itemCount": 1, "warnings": []},
            "usage": {"latencyMs": 10},
            "error": None,
        }
    )

    assert response.status == WorkerStatus.SUCCEEDED
    assert response.artifact is not None
    assert response.artifact.encoding == ArtifactEncoding.TEXT


def test_stream_artifact_uses_artifact_chunks_not_inline_content() -> None:
    response = RenderResponse.model_validate(
        {
            "requestId": "req-1",
            "traceId": "trace-1",
            "status": "SUCCEEDED",
            "renderType": "OPERATIONS_REPORT",
            "artifact": {
                "format": "PDF",
                "filename": "report.pdf",
                "contentType": "application/pdf",
                "encoding": "STREAM",
                "artifactId": "art-1",
                "chunkCount": 3,
                "sizeBytes": 4096,
                "sha256": "sha256:stream",
            },
        }
    )

    assert response.artifact is not None
    assert response.artifact.content is None
    assert response.artifact.chunkCount == 3


def test_stream_artifact_rejects_inline_content() -> None:
    payload = {
        "requestId": "req-1",
        "traceId": "trace-1",
        "status": "SUCCEEDED",
        "renderType": "OPERATIONS_REPORT",
        "artifact": {
            "format": "PDF",
            "filename": "report.pdf",
            "contentType": "application/pdf",
            "encoding": "STREAM",
            "artifactId": "art-1",
            "content": "JVBERi0x",
            "sizeBytes": 8,
            "sha256": "sha256:stream",
        },
    }

    with pytest.raises(ValidationError):
        RenderResponse.model_validate(payload)


def _request_payload(render_type: str, output_format: str) -> dict:
    return {
        "requestId": "req-1",
        "traceId": "trace-1",
        "callerDomain": "CLASSICS",
        "operation": "CLASSICS_EXPORT",
        "renderType": render_type,
        "template": {
            "templateId": "classics-export-default",
            "templateVersion": "2026.06.01",
        },
        "output": {
            "format": output_format,
            "filenameHint": "export.zip",
            "locale": "zh-CN",
        },
        "input": {
            "snapshotId": "snapshot-1",
            "contentType": "CLASSICS_EXPORT_SNAPSHOT",
            "payload": {"title": "三才图会导出", "items": []},
        },
        "options": {"stream": False, "includeMetadata": True},
    }
