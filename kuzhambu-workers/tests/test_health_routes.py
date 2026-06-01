from fastapi.testclient import TestClient

from kuzhambu_workers.api.health_routes import AI_CAPABILITIES
from kuzhambu_workers.main import app


def test_capabilities_returns_workers_contract(monkeypatch) -> None:
    monkeypatch.setenv("KUZHAMBU_WORKER_BROWSER_MAX_PAGES", "6")
    monkeypatch.setenv("KUZHAMBU_WORKER_MAX_REQUEST_BYTES", "1024")
    monkeypatch.setenv("KUZHAMBU_WORKER_MAX_ARTIFACT_BYTES", "2048")
    monkeypatch.setenv("KUZHAMBU_WORKER_ARTIFACT_CHUNK_BYTES", "256")

    response = TestClient(app).get("/internal/capabilities")

    assert response.status_code == 200
    body = response.json()
    assert body["ai"]["capabilities"] == AI_CAPABILITIES
    assert body["ai"]["stream"] is True
    assert body["render"]["stream"] is True
    assert body["render"]["pdfEngine"] == "PLAYWRIGHT_CHROMIUM_PRINT"
    assert body["render"]["browserPool"] == {"enabled": True, "maxPages": 6}
    assert body["limits"] == {
        "maxRequestBytes": 1024,
        "maxArtifactBytes": 2048,
        "artifactChunkBytes": 256,
    }


def test_health_has_no_external_dependency_checks() -> None:
    body = TestClient(app).get("/internal/health").json()

    assert "database" not in body
    assert "redis" not in body
    assert "mq" not in body
