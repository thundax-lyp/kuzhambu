from dataclasses import dataclass
from os import getenv
from pathlib import Path
from tempfile import gettempdir

DEFAULT_MAX_CLOCK_SKEW_MS = 300_000
DEFAULT_MAX_REQUEST_BYTES = 10 * 1024 * 1024
DEFAULT_TIMEOUT_MS = 60_000
DEFAULT_MAX_ARTIFACT_BYTES = 100 * 1024 * 1024
DEFAULT_ARTIFACT_CHUNK_BYTES = 256 * 1024
DEFAULT_ARTIFACT_TTL_HOURS = 12
DEFAULT_ARTIFACT_CLEANUP_INTERVAL_SECONDS = 3600
DEFAULT_BROWSER_POOL_SIZE = 1
DEFAULT_BROWSER_MAX_PAGES = 4
DEFAULT_BROWSER_PAGE_TIMEOUT_MS = 30_000
DEFAULT_RENDER_TIMEOUT_MS = 120_000


@dataclass(frozen=True)
class WorkerSettings:
    allowed_services: tuple[str, ...]
    internal_secret: str
    max_clock_skew_ms: int
    log_level: str
    temp_dir: Path
    max_request_bytes: int
    default_timeout_ms: int
    max_artifact_bytes: int
    artifact_chunk_bytes: int
    artifact_ttl_hours: int
    artifact_cleanup_interval_seconds: int
    browser_pool_size: int
    browser_max_pages: int
    browser_page_timeout_ms: int
    render_timeout_ms: int


def load_settings() -> WorkerSettings:
    return WorkerSettings(
        allowed_services=_csv("KUZHAMBU_WORKER_ALLOWED_SERVICES"),
        internal_secret=getenv("KUZHAMBU_WORKER_INTERNAL_SECRET", ""),
        max_clock_skew_ms=_integer(
            "KUZHAMBU_WORKER_MAX_CLOCK_SKEW_MS",
            DEFAULT_MAX_CLOCK_SKEW_MS,
        ),
        log_level=getenv("KUZHAMBU_WORKER_LOG_LEVEL", "INFO").upper(),
        temp_dir=Path(
            getenv(
                "KUZHAMBU_WORKER_TEMP_DIR",
                str(Path(gettempdir()) / "kuzhambu-workers"),
            )
        ),
        max_request_bytes=_integer("KUZHAMBU_WORKER_MAX_REQUEST_BYTES", DEFAULT_MAX_REQUEST_BYTES),
        default_timeout_ms=_integer("KUZHAMBU_WORKER_DEFAULT_TIMEOUT_MS", DEFAULT_TIMEOUT_MS),
        max_artifact_bytes=_integer(
            "KUZHAMBU_WORKER_MAX_ARTIFACT_BYTES",
            DEFAULT_MAX_ARTIFACT_BYTES,
        ),
        artifact_chunk_bytes=_integer(
            "KUZHAMBU_WORKER_ARTIFACT_CHUNK_BYTES",
            DEFAULT_ARTIFACT_CHUNK_BYTES,
        ),
        artifact_ttl_hours=_integer(
            "KUZHAMBU_WORKER_ARTIFACT_TTL_HOURS",
            DEFAULT_ARTIFACT_TTL_HOURS,
        ),
        artifact_cleanup_interval_seconds=_integer(
            "KUZHAMBU_WORKER_ARTIFACT_CLEANUP_INTERVAL_SECONDS",
            DEFAULT_ARTIFACT_CLEANUP_INTERVAL_SECONDS,
        ),
        browser_pool_size=_integer("KUZHAMBU_WORKER_BROWSER_POOL_SIZE", DEFAULT_BROWSER_POOL_SIZE),
        browser_max_pages=_integer("KUZHAMBU_WORKER_BROWSER_MAX_PAGES", DEFAULT_BROWSER_MAX_PAGES),
        browser_page_timeout_ms=_integer(
            "KUZHAMBU_WORKER_BROWSER_PAGE_TIMEOUT_MS",
            DEFAULT_BROWSER_PAGE_TIMEOUT_MS,
        ),
        render_timeout_ms=_integer("KUZHAMBU_WORKER_RENDER_TIMEOUT_MS", DEFAULT_RENDER_TIMEOUT_MS),
    )


def _csv(name: str) -> tuple[str, ...]:
    value = getenv(name, "")
    return tuple(item.strip() for item in value.split(",") if item.strip())


def _integer(name: str, default: int) -> int:
    value = getenv(name)
    if value is None or value == "":
        return default
    return int(value)
