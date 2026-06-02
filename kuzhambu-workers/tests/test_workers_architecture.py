from pathlib import Path

import tomli

from kuzhambu_workers.ai.usecase_registry import USECASE_PATHS
from kuzhambu_workers.core import service_paths
from kuzhambu_workers.core.security import SERVICE_PATHS

REPO_ROOT = Path(__file__).resolve().parents[1]
PACKAGE_ROOT = REPO_ROOT / "src" / "kuzhambu_workers"
ALLOWED_PACKAGE_GROUPS = {
    "ai",
    "api",
    "core",
    "render",
    "schemas",
    "streaming",
}
FORBIDDEN_BUCKET_DIRS = {
    "common",
    "utils",
    "misc",
    "helpers",
}
FORBIDDEN_BASE_DEPENDENCIES = {
    "alembic",
    "celery",
    "dramatiq",
    "kafka-python",
    "kombu",
    "pika",
    "redis",
    "rq",
    "sqlalchemy",
}
EXPECTED_IMPORT_LINTER_CONTRACTS = {
    "WORKERS_CORE_NO_FEATURE_DEPENDENCY",
    "WORKERS_SCHEMA_NO_RUNTIME_DEPENDENCY",
    "WORKERS_FEATURE_NO_API_DEPENDENCY",
    "WORKERS_AI_NO_RENDER_DEPENDENCY",
    "WORKERS_RENDER_NO_AI_DEPENDENCY",
    "WORKERS_STREAMING_NO_FEATURE_DEPENDENCY",
}


def test_workers_package_groups_are_explicit() -> None:
    package_groups = {
        path.name
        for path in PACKAGE_ROOT.iterdir()
        if path.is_dir() and not path.name.startswith("__")
    }

    assert package_groups == ALLOWED_PACKAGE_GROUPS
    assert package_groups.isdisjoint(FORBIDDEN_BUCKET_DIRS)


def test_workers_base_dependencies_do_not_include_stateful_infra() -> None:
    pyproject = _pyproject()
    dependencies = {
        _dependency_name(dependency) for dependency in pyproject["project"].get("dependencies", [])
    }

    assert dependencies.isdisjoint(FORBIDDEN_BASE_DEPENDENCIES)


def test_workers_import_linter_contracts_match_rules() -> None:
    pyproject = _pyproject()
    contracts = {contract["name"] for contract in pyproject["tool"]["importlinter"]["contracts"]}

    assert contracts == EXPECTED_IMPORT_LINTER_CONTRACTS


def test_ai_usecase_paths_are_shared_by_registry_and_security_allowlist() -> None:
    ai_paths = SERVICE_PATHS["kuzhambu-ai"]

    assert USECASE_PATHS == service_paths.AI_USECASE_PATHS
    assert set(service_paths.AI_USECASE_PATHS).issubset(ai_paths)


def _pyproject() -> dict:
    return tomli.loads((REPO_ROOT / "pyproject.toml").read_text(encoding="utf-8"))


def _dependency_name(dependency: str) -> str:
    return (
        dependency.split("[", 1)[0]
        .split("<", 1)[0]
        .split(">", 1)[0]
        .split("=", 1)[0]
        .split("~", 1)[0]
        .strip()
        .lower()
        .replace("_", "-")
    )
