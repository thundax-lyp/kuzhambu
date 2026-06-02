from hashlib import sha256
from html import escape
from pathlib import Path
from typing import Protocol

from kuzhambu_workers.core.errors import WorkerError, WorkerErrorType
from kuzhambu_workers.render.classics_export import RenderedArtifact
from kuzhambu_workers.schemas.render import RenderOutputFormat, RenderRequest, RenderSummary


class PdfRenderer(Protocol):
    async def html_to_pdf(self, html: str) -> bytes: ...


async def render_operations_report(
    request: RenderRequest,
    *,
    pdf_renderer: PdfRenderer | None = None,
) -> RenderedArtifact:
    payload = request.input.payload
    html = _render_html(payload)
    if request.output.format == RenderOutputFormat.HTML:
        data = html.encode("utf-8")
        content_type = "text/html; charset=utf-8"
    elif request.output.format == RenderOutputFormat.PDF:
        if pdf_renderer is None:
            raise WorkerError(
                WorkerErrorType.RENDER_OUTPUT_FAILURE,
                "PDF_RENDERER_REQUIRED",
                "PDF 渲染需要 Browser Pool。",
            )
        data = await pdf_renderer.html_to_pdf(html)
        content_type = "application/pdf"
    else:
        raise WorkerError(
            WorkerErrorType.RENDER_INPUT_FAILURE,
            "UNSUPPORTED_OPERATIONS_REPORT_FORMAT",
            "Operations 报表只支持 HTML 或 PDF 输出。",
            detail={"format": request.output.format.value},
        )

    return RenderedArtifact(
        format=request.output.format,
        filename=_filename(request.output.filenameHint, request.output.format),
        content_type=content_type,
        data=data,
        size_bytes=len(data),
        sha256=_digest(data),
        summary=RenderSummary(
            itemCount=len(_records(payload)),
            warnings=[],
            metadata={
                "metricCount": len(_metrics(payload)),
            },
        ),
    )


def _render_html(payload: dict) -> str:
    template = (Path(__file__).parent / "templates" / "operations_report.html").read_text(
        encoding="utf-8"
    )
    title = escape(str(payload.get("title") or "Operations Report"))
    period = escape(str(payload.get("period") or ""))
    return (
        template.replace("{{ title }}", title)
        .replace("{{ period }}", period)
        .replace("{{ metrics }}", _metrics_html(_metrics(payload)))
        .replace("{{ records }}", _records_html(_records(payload)))
    )


def _metrics(payload: dict) -> list[dict]:
    return _list_payload(payload, "metrics")


def _records(payload: dict) -> list[dict]:
    return _list_payload(payload, "records")


def _list_payload(payload: dict, key: str) -> list[dict]:
    raw = payload.get(key, [])
    if not isinstance(raw, list):
        raise WorkerError(
            WorkerErrorType.RENDER_INPUT_FAILURE,
            "INVALID_OPERATIONS_REPORT_SNAPSHOT",
            "Operations 报表快照字段必须是列表。",
            detail={"field": key},
        )
    return [item if isinstance(item, dict) else {"text": str(item)} for item in raw]


def _metrics_html(metrics: list[dict]) -> str:
    return "\n".join(
        "<li>"
        f"<span>{escape(str(metric.get('label') or metric.get('name') or 'Metric'))}</span>"
        f"<strong>{escape(str(metric.get('value') or 0))}</strong>"
        "</li>"
        for metric in metrics
    )


def _records_html(records: list[dict]) -> str:
    return "\n".join(
        "<tr>"
        f"<td>{escape(str(record.get('time') or ''))}</td>"
        f"<td>{escape(str(record.get('type') or ''))}</td>"
        f"<td>{escape(str(record.get('message') or record.get('text') or ''))}</td>"
        "</tr>"
        for record in records
    )


def _filename(filename_hint: str, output_format: RenderOutputFormat) -> str:
    suffix = output_format.value.lower()
    fallback = f"operations-report.{suffix}"
    name = Path(filename_hint or fallback).name or fallback
    if not name.lower().endswith(f".{suffix}"):
        name = f"{Path(name).stem}.{suffix}"
    return name


def _digest(data: bytes) -> str:
    return f"sha256:{sha256(data).hexdigest()}"
