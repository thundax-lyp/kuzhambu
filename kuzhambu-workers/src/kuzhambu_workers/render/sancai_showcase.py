from hashlib import sha256
from html import escape
from pathlib import Path

from kuzhambu_workers.core.errors import WorkerError, WorkerErrorType
from kuzhambu_workers.render.classics_export import RenderedArtifact
from kuzhambu_workers.schemas.render import RenderOutputFormat, RenderRequest, RenderSummary


def render_sancai_showcase(request: RenderRequest) -> RenderedArtifact:
    if request.output.format != RenderOutputFormat.HTML:
        raise WorkerError(
            WorkerErrorType.RENDER_INPUT_FAILURE,
            "UNSUPPORTED_SANCAI_SHOWCASE_FORMAT",
            "三才图会静态展示只支持 HTML 输出。",
            detail={"format": request.output.format.value},
        )

    payload = request.input.payload
    metadata = payload.get("metadata", {})
    catalog = _list_payload(payload, "catalog")
    entries = _list_payload(payload, "entries")
    assets = _list_payload(payload, "assets")
    html = _render_html(
        title=str(metadata.get("title") or payload.get("title") or "Sancai Showcase"),
        metadata=metadata,
        catalog=catalog,
        entries=entries,
        assets=assets,
    ).encode("utf-8")

    return RenderedArtifact(
        format=RenderOutputFormat.HTML,
        filename=_filename(request.output.filenameHint),
        content_type="text/html; charset=utf-8",
        data=html,
        size_bytes=len(html),
        sha256=_digest(html),
        summary=RenderSummary(
            itemCount=len(entries),
            warnings=[],
            metadata={
                "catalogCount": len(catalog),
                "assetCount": len(assets),
            },
        ),
    )


def _list_payload(payload: dict, key: str) -> list[dict]:
    raw = payload.get(key, [])
    if not isinstance(raw, list):
        raise WorkerError(
            WorkerErrorType.RENDER_INPUT_FAILURE,
            "INVALID_SANCAI_SHOWCASE_SNAPSHOT",
            "三才图会静态展示快照字段必须是列表。",
            detail={"field": key},
        )
    return [item if isinstance(item, dict) else {"text": str(item)} for item in raw]


def _render_html(
    *,
    title: str,
    metadata: dict,
    catalog: list[dict],
    entries: list[dict],
    assets: list[dict],
) -> str:
    template = (Path(__file__).parent / "templates" / "sancai_showcase.html").read_text(
        encoding="utf-8"
    )
    return (
        template.replace("{{ title }}", escape(title))
        .replace("{{ metadata }}", _metadata_html(metadata))
        .replace("{{ catalog }}", _catalog_html(catalog))
        .replace("{{ entries }}", _entries_html(entries))
        .replace("{{ assets }}", _assets_html(assets))
    )


def _metadata_html(metadata: dict) -> str:
    return "\n".join(
        f"<li><span>{escape(str(key))}</span><strong>{escape(str(value))}</strong></li>"
        for key, value in metadata.items()
    )


def _catalog_html(catalog: list[dict]) -> str:
    return "\n".join(
        f"<li>{escape(str(item.get('label') or item.get('title') or item.get('id') or 'Untitled'))}"
        "</li>"
        for item in catalog
    )


def _entries_html(entries: list[dict]) -> str:
    return "\n".join(
        "<article>"
        f"<h2>{escape(str(entry.get('title') or entry.get('id') or 'Untitled'))}</h2>"
        f"<p>{escape(str(entry.get('text') or entry.get('body') or ''))}</p>"
        f"{_entry_images(entry)}"
        "</article>"
        for entry in entries
    )


def _entry_images(entry: dict) -> str:
    images = entry.get("images", [])
    if not isinstance(images, list):
        return _missing_image_html()
    image_items = [image for image in images if isinstance(image, dict)]
    if not image_items:
        return _missing_image_html()
    return "".join(
        f'<figure class="entry-image" data-current="{_current_flag(image)}">'
        f"{_image_content_html(image)}"
        f"<figcaption>{escape(str(image.get('caption') or ''))}</figcaption>"
        "</figure>"
        for image in sorted(image_items, key=_image_sort_key)
    )


def _image_sort_key(image: dict) -> tuple[int, int]:
    priority = image.get("priority")
    image_id = image.get("imageId")
    return (
        priority if isinstance(priority, int) else 2_147_483_647,
        image_id if isinstance(image_id, int) else 0,
    )


def _current_flag(image: dict) -> str:
    return "true" if image.get("currentUsed") is True else "false"


def _image_content_html(image: dict) -> str:
    src = str(image.get("src") or "")
    if not src:
        return '<div class="image-placeholder">图片资源暂缺</div>'
    return (
        f'<img src="{escape(src, quote=True)}" '
        f'alt="{escape(str(image.get("alt") or ""), quote=True)}">'
    )


def _missing_image_html() -> str:
    return (
        '<figure class="entry-image entry-image-missing" data-current="false">'
        '<div class="image-placeholder">暂无配图</div>'
        "<figcaption>暂无图片资源</figcaption>"
        "</figure>"
    )


def _assets_html(assets: list[dict]) -> str:
    return "\n".join(
        f"<li>{escape(str(asset.get('name') or asset.get('id') or 'asset'))}</li>"
        for asset in assets
    )


def _filename(filename_hint: str) -> str:
    name = Path(filename_hint or "sancai-showcase.html").name or "sancai-showcase.html"
    if not name.lower().endswith(".html"):
        name = f"{Path(name).stem}.html"
    return name


def _digest(data: bytes) -> str:
    return f"sha256:{sha256(data).hexdigest()}"
