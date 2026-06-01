from fastapi.testclient import TestClient

from kuzhambu_workers.main import app


def test_health_returns_up() -> None:
    response = TestClient(app).get("/internal/health")

    assert response.status_code == 200
    assert response.json() == {
        "status": "UP",
        "service": "kuzhambu-workers",
        "version": "0.0.1-dev",
    }
