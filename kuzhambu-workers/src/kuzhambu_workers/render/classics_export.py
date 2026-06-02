import csv
import json
from dataclasses import dataclass
from hashlib import sha256
from html import escape
from io import BytesIO, StringIO
from pathlib import Path
from zipfile import ZIP_DEFLATED, ZipFile

from kuzhambu_workers.core.errors import WorkerError, WorkerErrorType
from kuzhambu_workers.schemas.render import RenderOutputFormat, RenderRequest, RenderSummary


@dataclass(frozen=True)
class RenderedArtifact:
    format: RenderOutputFormat
    filename: str
    content_type: str
    data: bytes
    size_bytes: int
    sha256: str
    summary: RenderSummary


def render_classics_export(request: RenderRequest) -> RenderedArtifact:
    items = _items(request.input.payload)
    title = str(
        request.input.payload.get("title") or request.output.filenameHint or "classics-export"
    )
    filename = _filename(request.output.filenameHint, request.output.format)

    if request.output.format == RenderOutputFormat.CSV:
        data = _render_csv(items).encode("utf-8")
        content_type = "text/csv; charset=utf-8"
    elif request.output.format == RenderOutputFormat.JSON:
        data = _render_json(request.input.payload).encode("utf-8")
        content_type = "application/json"
    elif request.output.format == RenderOutputFormat.HTML:
        data = _render_html(title, items).encode("utf-8")
        content_type = "text/html; charset=utf-8"
    elif request.output.format == RenderOutputFormat.ZIP:
        data = _render_zip(title, items, request.input.payload)
        content_type = "application/zip"
    else:
        raise WorkerError(
            WorkerErrorType.RENDER_INPUT_FAILURE,
            "UNSUPPORTED_CLASSICS_EXPORT_FORMAT",
            "Classics 导出不支持该输出格式。",
            detail={"format": request.output.format.value},
        )

    return RenderedArtifact(
        format=request.output.format,
        filename=filename,
        content_type=content_type,
        data=data,
        size_bytes=len(data),
        sha256=_digest(data),
        summary=RenderSummary(itemCount=len(items), warnings=[]),
    )


def _items(payload: dict) -> list[dict]:
    raw_items = payload.get("items")
    if raw_items is None:
        return []
    if not isinstance(raw_items, list):
        raise WorkerError(
            WorkerErrorType.RENDER_INPUT_FAILURE,
            "INVALID_CLASSICS_ITEMS",
            "Classics 导出条目必须是列表。",
        )
    return [item if isinstance(item, dict) else {"text": str(item)} for item in raw_items]


def _render_csv(items: list[dict]) -> str:
    output = StringIO()
    writer = csv.DictWriter(output, fieldnames=["id", "title", "text"], extrasaction="ignore")
    writer.writeheader()
    for item in items:
        writer.writerow(
            {
                "id": item.get("id", ""),
                "title": item.get("title", ""),
                "text": item.get("text", ""),
            }
        )
    return output.getvalue()


def _render_json(payload: dict) -> str:
    return json.dumps(payload, ensure_ascii=False, separators=(",", ":"))


def _render_html(title: str, items: list[dict]) -> str:
    template = (Path(__file__).parent / "templates" / "classics_export.html").read_text(
        encoding="utf-8"
    )
    rows = "\n".join(
        "<article>"
        f"<h2>{escape(str(item.get('title') or item.get('id') or 'Untitled'))}</h2>"
        f"<p>{escape(str(item.get('text') or ''))}</p>"
        "</article>"
        for item in items
    )
    return template.replace("{{ title }}", escape(title)).replace("{{ items }}", rows)


def _render_zip(title: str, items: list[dict], payload: dict) -> bytes:
    buffer = BytesIO()
    with ZipFile(buffer, "w", ZIP_DEFLATED) as archive:
        archive.writestr("data.json", _render_json(payload))
        archive.writestr("data.csv", _render_csv(items))
        archive.writestr("index.html", _render_html(title, items))
    return buffer.getvalue()


def _filename(filename_hint: str, output_format: RenderOutputFormat) -> str:
    suffix = output_format.value.lower()
    fallback = f"classics-export.{suffix}"
    name = Path(filename_hint or fallback).name or fallback
    if not name.lower().endswith(f".{suffix}"):
        name = f"{Path(name).stem}.{suffix}"
    return name


def _digest(data: bytes) -> str:
    return f"sha256:{sha256(data).hexdigest()}"
