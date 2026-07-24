from fastapi.testclient import TestClient

from kuzhambu_workers.main import app


def test_openapi_schema_uses_internal_path() -> None:
    response = TestClient(app).get("/internal/openapi.json")

    assert response.status_code == 200
    body = response.json()
    assert body["info"]["title"] == "Kuzhambu Workers"
    assert "/internal/ai/invoke" in body["paths"]
    assert "/internal/render/classics-export" in body["paths"]


def test_openapi_marks_unified_ai_interfaces_as_runtime_interfaces() -> None:
    body = TestClient(app).get("/internal/openapi.json").json()

    assert "统一 AI 执行接口" in body["paths"]["/internal/ai/invoke"]["post"]["description"]
    assert "统一 AI 执行接口" in body["paths"]["/internal/ai/stream"]["post"]["description"]
    assert body["paths"]["/internal/ai/invoke"]["post"]["summary"] == "AI invoke"
    assert body["paths"]["/internal/ai/stream"]["post"]["summary"] == "AI stream"
    assert body["paths"]["/internal/ai/invoke"]["post"]["tags"] == ["AI"]
    assert body["paths"]["/internal/ai/stream"]["post"]["tags"] == ["AI"]


def test_openapi_marks_render_interfaces_as_usecase_interfaces() -> None:
    body = TestClient(app).get("/internal/openapi.json").json()

    descriptions = {
        path: body["paths"][path]["post"]["description"]
        for path in [
            "/internal/render/classics-export",
            "/internal/render/operations-report",
            "/internal/render/classics-export/stream",
            "/internal/render/operations-report/stream",
        ]
    }

    assert "Classics 导出 usecase 接口" in descriptions["/internal/render/classics-export"]
    assert "Operations 报表 usecase 接口" in descriptions["/internal/render/operations-report"]
    assert all(body["paths"][path]["post"]["tags"] == ["Render"] for path in descriptions)


def test_openapi_does_not_expose_ai_business_usecase_paths() -> None:
    body = TestClient(app).get("/internal/openapi.json").json()

    assert all(not path.startswith("/internal/ai/classics/") for path in body["paths"])
    assert all(not path.startswith("/internal/ai/discovery/") for path in body["paths"])
    assert all(not path.startswith("/internal/ai/knowledge/") for path in body["paths"])
    assert all(not path.startswith("/internal/ai/platform/") for path in body["paths"])


def test_openapi_defines_unified_ai_tag() -> None:
    body = TestClient(app).get("/internal/openapi.json").json()

    tags = [tag["name"] for tag in body["tags"]]

    assert "AI" in tags
    assert "OpenAI Compatible" in tags
    assert "Render" in tags
    assert "Health" in tags


def test_swagger_ui_uses_internal_path() -> None:
    response = TestClient(app).get("/internal/docs")

    assert response.status_code == 200
    assert "swagger-ui" in response.text
    assert "/internal/openapi.json" in response.text


def test_redoc_uses_internal_path() -> None:
    response = TestClient(app).get("/internal/redoc")

    assert response.status_code == 200
    assert "redoc" in response.text.lower()
    assert "/internal/openapi.json" in response.text
