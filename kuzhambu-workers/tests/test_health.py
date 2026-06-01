from fastapi.testclient import TestClient

from kuzhambu_workers.main import app


def test_health_returns_up() -> None:
    response = TestClient(app).get("/internal/health")

    assert response.status_code == 200
    body = response.json()
    assert body["status"] == "UP"
    assert body["service"] == "kuzhambu-workers"
    assert body["version"] == "0.0.1-dev"
    assert body["startedAt"].endswith("Z")
    assert body["time"].endswith("Z")
