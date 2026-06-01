from fastapi.testclient import TestClient

from kuzhambu_workers.ai.usecase_registry import USECASES
from kuzhambu_workers.main import app


def test_openapi_schema_uses_internal_path() -> None:
    response = TestClient(app).get("/internal/openapi.json")

    assert response.status_code == 200
    body = response.json()
    assert body["info"]["title"] == "Kuzhambu Workers"
    assert "/internal/ai/invoke" in body["paths"]
    assert "/internal/render/classics-export" in body["paths"]


def test_openapi_marks_generic_ai_interfaces_as_debug_only() -> None:
    body = TestClient(app).get("/internal/openapi.json").json()

    assert (
        "真实业务必须使用基于 usecase"
        in body["paths"]["/internal/ai/invoke"]["post"]["description"]
    )
    assert (
        "真实业务必须使用基于 usecase"
        in body["paths"]["/internal/ai/stream"]["post"]["description"]
    )
    assert body["paths"]["/internal/ai/invoke"]["post"]["summary"] == "Debug AI invoke"
    assert body["paths"]["/internal/ai/stream"]["post"]["summary"] == "Debug AI stream"


def test_openapi_marks_render_interfaces_as_usecase_interfaces() -> None:
    body = TestClient(app).get("/internal/openapi.json").json()

    descriptions = {
        path: body["paths"][path]["post"]["description"]
        for path in [
            "/internal/render/classics-export",
            "/internal/render/sancai-showcase",
            "/internal/render/operations-report",
            "/internal/render/classics-export/stream",
            "/internal/render/sancai-showcase/stream",
            "/internal/render/operations-report/stream",
        ]
    }

    assert "Classics 导出 usecase 接口" in descriptions["/internal/render/classics-export"]
    assert "三才图会静态展示 usecase 接口" in descriptions["/internal/render/sancai-showcase"]
    assert "Operations 报表 usecase 接口" in descriptions["/internal/render/operations-report"]
    assert all("真实业务必须使用基于 usecase" not in value for value in descriptions.values())


def test_openapi_exposes_ai_usecase_paths_with_business_boundaries() -> None:
    body = TestClient(app).get("/internal/openapi.json").json()

    for usecase in USECASES:
        assert usecase.path in body["paths"]
        operation = body["paths"][usecase.path]["post"]
        assert operation["summary"] == usecase.summary
        assert "调用方固定为 kuzhambu-ai" in operation["description"]
        assert f"capability 必须为 `{usecase.capability.value}`" in operation["description"]
        assert f"options.stream 必须为 `{str(usecase.stream).lower()}`" in operation["description"]
        assert "数据库" not in operation["description"]
        assert "Redis" not in operation["description"]
        assert "MQ" not in operation["description"]


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
