from fastapi.testclient import TestClient

from kuzhambu_workers.main import app


def test_openapi_schema_uses_internal_path() -> None:
    response = TestClient(app).get("/internal/openapi.json")

    assert response.status_code == 200
    body = response.json()
    assert body["info"]["title"] == "Kuzhambu Workers"
    assert "/internal/ai/invoke" in body["paths"]
    assert "/internal/render/classics-export" in body["paths"]


def test_swagger_ui_uses_internal_path() -> None:
    response = TestClient(app).get("/internal/docs")

    assert response.status_code == 200
    assert "swagger-ui" in response.text
    assert "/internal/openapi.json" in response.text
