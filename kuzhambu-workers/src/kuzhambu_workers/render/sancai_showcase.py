from base64 import b64encode
from hashlib import sha256
from html import escape
from mimetypes import guess_type
from pathlib import Path
from urllib.request import urlopen

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
    catalog = _catalog_payload(payload)
    volumes = _list_payload(payload, "volumes")
    entries = _list_payload(payload, "entries")
    assets = _list_payload(payload, "assets")
    asset_lookup = _asset_lookup(assets)
    html = _render_html(
        title=str(metadata.get("title") or payload.get("title") or "Sancai Showcase"),
        metadata=metadata,
        catalog=catalog,
        volumes=volumes,
        entries=entries,
        assets=assets,
        asset_lookup=asset_lookup,
        visibility_risk=_visibility_risk(payload),
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
                "volumeCount": len(volumes),
                "assetCount": len(assets),
                "visibilityRiskStatus": _visibility_risk(payload),
            },
        ),
    )


def _catalog_payload(payload: dict) -> list[dict]:
    if "catalogs" in payload:
        return _list_payload(payload, "catalogs")
    return _list_payload(payload, "catalog")


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


def _visibility_risk(payload: dict) -> str:
    visibility_risk = payload.get("visibilityRisk", {})
    if isinstance(visibility_risk, dict):
        return str(visibility_risk.get("status") or "PUBLIC_ONLY")
    return "PUBLIC_ONLY"


def _asset_lookup(assets: list[dict]) -> dict[str, dict]:
    lookup: dict[str, dict] = {}
    for asset in assets:
        resource_id = str(asset.get("resourceId") or asset.get("id") or "")
        if not resource_id:
            continue
        lookup[resource_id] = {
            "src": _asset_data_url(asset),
            "contentType": str(asset.get("contentType") or ""),
            "filename": str(asset.get("filename") or asset.get("name") or resource_id),
        }
    return lookup


def _asset_data_url(asset: dict) -> str:
    temporary_url = str(asset.get("temporaryUrl") or "")
    if not temporary_url:
        return ""
    try:
        with urlopen(temporary_url, timeout=5) as response:
            data = response.read()
            content_type = (
                str(asset.get("contentType") or "")
                or response.headers.get_content_type()
                or guess_type(str(asset.get("filename") or ""))[0]
                or "application/octet-stream"
            )
    except Exception:
        return ""
    encoded = b64encode(data).decode("ascii")
    return f"data:{content_type};base64,{encoded}"


def _render_html(
    *,
    title: str,
    metadata: dict,
    catalog: list[dict],
    volumes: list[dict],
    entries: list[dict],
    assets: list[dict],
    asset_lookup: dict[str, dict],
    visibility_risk: str,
) -> str:
    template = (Path(__file__).parent / "templates" / "sancai_showcase.html").read_text(
        encoding="utf-8"
    )
    return (
        template.replace("{{ title }}", escape(title))
        .replace("{{ metadata }}", _metadata_html(metadata))
        .replace("{{ catalog }}", _catalog_html(catalog))
        .replace("{{ catalog_options }}", _catalog_options_html(catalog))
        .replace("{{ volumes }}", _volumes_html(volumes))
        .replace("{{ volume_options }}", _volume_options_html(volumes))
        .replace("{{ entries }}", _entries_html(entries, asset_lookup))
        .replace("{{ assets }}", _assets_html(assets))
        .replace("{{ visibility_risk }}", escape(visibility_risk))
    )


def _metadata_html(metadata: dict) -> str:
    return "\n".join(
        f"<li><span>{escape(str(key))}</span><strong>{escape(str(value))}</strong></li>"
        for key, value in metadata.items()
    )


def _catalog_html(catalog: list[dict]) -> str:
    return "\n".join(
        "<li>"
        f'<button type="button" data-catalog="{escape(str(item.get("id") or ""), quote=True)}">'
        f"{escape(str(item.get('label') or item.get('title') or item.get('id') or 'Untitled'))}"
        "</button>"
        f"<small>{escape(str(item.get('entryCount') or ''))}</small>"
        "</li>"
        for item in catalog
    )


def _catalog_options_html(catalog: list[dict]) -> str:
    return "\n".join(
        f'<option value="{escape(str(item.get("id") or ""), quote=True)}">'
        f"{escape(str(item.get('label') or item.get('title') or item.get('id') or 'Untitled'))}"
        "</option>"
        for item in catalog
    )


def _volumes_html(volumes: list[dict]) -> str:
    return "\n".join(
        "<li>"
        f'<button type="button" data-volume="{escape(str(item.get("id") or ""), quote=True)}">'
        f"{escape(str(item.get('title') or item.get('id') or 'Untitled'))}"
        "</button>"
        "</li>"
        for item in volumes
    )


def _volume_options_html(volumes: list[dict]) -> str:
    return "\n".join(
        f'<option value="{escape(str(item.get("id") or ""), quote=True)}">'
        f"{escape(str(item.get('title') or item.get('id') or 'Untitled'))}"
        "</option>"
        for item in volumes
    )


def _entries_html(entries: list[dict], asset_lookup: dict[str, dict]) -> str:
    return "\n".join(
        "<article "
        f'data-title="{escape(str(entry.get("title") or ""), quote=True)}" '
        f'data-body="{escape(_entry_search_text(entry), quote=True)}" '
        f'data-category="{escape(str(entry.get("categoryId") or ""), quote=True)}" '
        f'data-volume="{escape(str(entry.get("volumeId") or ""), quote=True)}">'
        f"<h2>{escape(str(entry.get('title') or entry.get('id') or 'Untitled'))}</h2>"
        f"{_entry_text_html(entry)}"
        f"{_entry_images(entry, asset_lookup)}"
        "</article>"
        for entry in entries
    )


def _entry_search_text(entry: dict) -> str:
    return " ".join(
        str(entry.get(key) or "")
        for key in ["title", "text", "body", "originalText", "translationText"]
    )


def _entry_text_html(entry: dict) -> str:
    original = str(entry.get("originalText") or entry.get("text") or entry.get("body") or "")
    translation = str(entry.get("translationText") or "")
    tags = entry.get("tags") if isinstance(entry.get("tags"), list) else []
    tag_html = "".join(f"<span>{escape(str(tag))}</span>" for tag in tags)
    visual_asset = entry.get("visualAsset") if isinstance(entry.get("visualAsset"), dict) else {}
    visual_text = str(visual_asset.get("visualDescription") or "")
    return (
        f'<p class="entry-original">{escape(original)}</p>'
        f'<p class="entry-translation">{escape(translation)}</p>'
        f'<div class="entry-tags">{tag_html}</div>'
        f'<p class="entry-visual">{escape(visual_text)}</p>'
    )


def _entry_images(entry: dict, asset_lookup: dict[str, dict]) -> str:
    images = entry.get("images", [])
    if not isinstance(images, list):
        return _missing_image_html()
    image_items = [image for image in images if isinstance(image, dict)]
    if not image_items:
        return _missing_image_html()
    return "".join(
        f'<figure class="entry-image" data-current="{_current_flag(image)}">'
        f"{_image_content_html(image, asset_lookup)}"
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


def _image_content_html(image: dict, asset_lookup: dict[str, dict]) -> str:
    resource_id = str(image.get("resourceId") or "")
    src = str(asset_lookup.get(resource_id, {}).get("src") or image.get("src") or "")
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
    def asset_label(asset: dict) -> str:
        return str(
            asset.get("filename")
            or asset.get("name")
            or asset.get("resourceId")
            or asset.get("id")
            or "asset"
        )

    return "\n".join(
        "<li>"
        f"{escape(asset_label(asset))}"
        f"<small>{escape(str(asset.get('contentType') or ''))}</small>"
        "</li>"
        for asset in assets
    )


def _filename(filename_hint: str) -> str:
    name = Path(filename_hint or "sancai-showcase.html").name or "sancai-showcase.html"
    if not name.lower().endswith(".html"):
        name = f"{Path(name).stem}.html"
    return name


def _digest(data: bytes) -> str:
    return f"sha256:{sha256(data).hexdigest()}"
