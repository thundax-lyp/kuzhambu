from pathlib import Path

from kuzhambu_workers.core.config import (
    DEFAULT_ARTIFACT_CHUNK_BYTES,
    DEFAULT_BROWSER_MAX_PAGES,
    DEFAULT_BROWSER_PAGE_TIMEOUT_MS,
    DEFAULT_BROWSER_POOL_SIZE,
    DEFAULT_MAX_ARTIFACT_BYTES,
    DEFAULT_MAX_CLOCK_SKEW_MS,
    DEFAULT_MAX_REQUEST_BYTES,
    DEFAULT_RENDER_TIMEOUT_MS,
    DEFAULT_TIMEOUT_MS,
    load_settings,
)


def test_load_settings_uses_design_defaults(monkeypatch) -> None:
    for key in tuple(_ENV_KEYS):
        monkeypatch.delenv(key, raising=False)

    settings = load_settings()

    assert settings.allowed_services == ()
    assert settings.internal_secret == ""
    assert settings.max_clock_skew_ms == DEFAULT_MAX_CLOCK_SKEW_MS
    assert settings.log_level == "INFO"
    assert settings.temp_dir.name == "kuzhambu-workers"
    assert settings.max_request_bytes == DEFAULT_MAX_REQUEST_BYTES
    assert settings.default_timeout_ms == DEFAULT_TIMEOUT_MS
    assert settings.max_artifact_bytes == DEFAULT_MAX_ARTIFACT_BYTES
    assert settings.artifact_chunk_bytes == DEFAULT_ARTIFACT_CHUNK_BYTES
    assert settings.browser_pool_size == DEFAULT_BROWSER_POOL_SIZE
    assert settings.browser_max_pages == DEFAULT_BROWSER_MAX_PAGES
    assert settings.browser_page_timeout_ms == DEFAULT_BROWSER_PAGE_TIMEOUT_MS
    assert settings.render_timeout_ms == DEFAULT_RENDER_TIMEOUT_MS


def test_load_settings_reads_environment(monkeypatch) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_ALLOWED_SERVICES", "kuzhambu-ai, kuzhambu-classics")
    monkeypatch.setenv("KUZHAMBU_WORKER_INTERNAL_SECRET", "worker-secret")
    monkeypatch.setenv("KUZHAMBU_WORKER_LOG_LEVEL", "debug")
    monkeypatch.setenv("KUZHAMBU_WORKER_TEMP_DIR", "/tmp/kuzhambu-test")
    monkeypatch.setenv("KUZHAMBU_WORKER_BROWSER_MAX_PAGES", "8")

    settings = load_settings()

    assert settings.allowed_services == ("kuzhambu-ai", "kuzhambu-classics")
    assert settings.internal_secret == "worker-secret"
    assert settings.log_level == "DEBUG"
    assert settings.temp_dir == Path("/tmp/kuzhambu-test")
    assert settings.browser_max_pages == 8


_ENV_KEYS = {
    "KUZHAMBU_WORKER_ALLOWED_SERVICES",
    "KUZHAMBU_WORKER_INTERNAL_SECRET",
    "KUZHAMBU_WORKER_MAX_CLOCK_SKEW_MS",
    "KUZHAMBU_WORKER_LOG_LEVEL",
    "KUZHAMBU_WORKER_TEMP_DIR",
    "KUZHAMBU_WORKER_MAX_REQUEST_BYTES",
    "KUZHAMBU_WORKER_DEFAULT_TIMEOUT_MS",
    "KUZHAMBU_WORKER_MAX_ARTIFACT_BYTES",
    "KUZHAMBU_WORKER_ARTIFACT_CHUNK_BYTES",
    "KUZHAMBU_WORKER_BROWSER_POOL_SIZE",
    "KUZHAMBU_WORKER_BROWSER_MAX_PAGES",
    "KUZHAMBU_WORKER_BROWSER_PAGE_TIMEOUT_MS",
    "KUZHAMBU_WORKER_RENDER_TIMEOUT_MS",
}
