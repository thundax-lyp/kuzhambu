from datetime import datetime, timezone

from fastapi import APIRouter

from kuzhambu_workers import __version__
from kuzhambu_workers.core.config import load_settings

router = APIRouter(prefix="/internal", tags=["Health"])
STARTED_AT = datetime.now(timezone.utc)

AI_CAPABILITIES = [
    "translate",
    "summary",
    "version_summary",
    "tags",
    "qa",
    "image_analysis",
    "image_gen",
    "visual",
    "fusion",
    "split",
    "query_understanding",
    "answer_generation",
    "knowledge_graph",
    "relation_extraction",
    "lineage_extraction",
    "prompt_suggestion",
]


@router.get("/health")
def health() -> dict[str, str]:
    return {
        "status": "UP",
        "service": "kuzhambu-workers",
        "version": __version__,
        "startedAt": _isoformat(STARTED_AT),
        "time": _isoformat(datetime.now(timezone.utc)),
    }


@router.get("/capabilities")
def capabilities() -> dict:
    settings = load_settings()
    return {
        "ai": {
            "endpoints": ["/internal/ai/invoke", "/internal/ai/stream"],
            "stream": True,
            "capabilities": AI_CAPABILITIES,
            "resultFormats": ["TEXT", "MARKDOWN", "JSON", "STRUCTURED", "ARTIFACT"],
        },
        "render": {
            "endpoints": [
                "/internal/render/classics-export",
                "/internal/render/operations-report",
                "/internal/render/classics-export/stream",
                "/internal/render/operations-report/stream",
            ],
            "stream": True,
            "formats": ["CSV", "JSON", "HTML", "ZIP", "PDF"],
            "pdfEngine": "PLAYWRIGHT_CHROMIUM_PRINT",
            "browserPool": {
                "enabled": True,
                "maxPages": settings.browser_max_pages,
            },
        },
        "limits": {
            "maxRequestBytes": settings.max_request_bytes,
            "maxArtifactBytes": settings.max_artifact_bytes,
            "artifactChunkBytes": settings.artifact_chunk_bytes,
        },
    }


def _isoformat(value: datetime) -> str:
    return value.isoformat(timespec="milliseconds").replace("+00:00", "Z")
