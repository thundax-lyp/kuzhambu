from pathlib import Path

import tomli

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
FORMAL_DISCOVERY_QA_FORBIDDEN_TOKENS = {
    "DiscoveryQa",
    "chat/completions",
    "question/ask",
    "knowledge/sync",
    "knowledge_sync",
    "sync_knowledge",
    "syncKnowledge",
}


def test_WORKERS_PACKAGE_GROUPS_and_WORKERS_FORBID_BUCKET_DIR() -> None:
    package_groups = {
        path.name
        for path in PACKAGE_ROOT.iterdir()
        if path.is_dir() and not path.name.startswith("__")
    }

    assert package_groups == ALLOWED_PACKAGE_GROUPS, "WORKERS_PACKAGE_GROUPS"
    assert package_groups.isdisjoint(FORBIDDEN_BUCKET_DIRS), "WORKERS_FORBID_BUCKET_DIR"


def test_WORKERS_DEPENDENCY_NO_DATABASE_REDIS_OR_QUEUE() -> None:
    pyproject = _pyproject()
    dependencies = {
        _dependency_name(dependency) for dependency in pyproject["project"].get("dependencies", [])
    }

    assert dependencies.isdisjoint(FORBIDDEN_BASE_DEPENDENCIES), (
        "WORKERS_DEPENDENCY_NO_DATABASE / WORKERS_DEPENDENCY_NO_REDIS / WORKERS_DEPENDENCY_NO_QUEUE"
    )


def test_WORKERS_ARCHITECTURE_IMPORT_LINTER_CONTRACTS_match_rules() -> None:
    pyproject = _pyproject()
    contracts = {contract["name"] for contract in pyproject["tool"]["importlinter"]["contracts"]}

    assert contracts == EXPECTED_IMPORT_LINTER_CONTRACTS, "WORKERS_TEST_ARCHITECTURE_CONTRACT"


def test_WORKERS_AI_SERVICE_BOUNDARY_uses_unified_paths() -> None:
    ai_paths = SERVICE_PATHS["kuzhambu-ai"]

    assert "/internal/ai/invoke" in ai_paths, "WORKERS_AI_UNIFIED_INVOKE"
    assert "/internal/ai/stream" in ai_paths, "WORKERS_AI_UNIFIED_STREAM"
    assert not any(path.startswith("/internal/ai/classics/") for path in ai_paths), (
        "WORKERS_AI_NO_BUSINESS_USECASE_PATH"
    )


def test_WORKERS_NO_FORMAL_DISCOVERY_QA_RUNTIME_OR_KNOWLEDGE_SYNC() -> None:
    source_text = "\n".join(
        path.read_text(encoding="utf-8")
        for path in PACKAGE_ROOT.rglob("*.py")
        if "__pycache__" not in path.parts
    )
    app_routes = SERVICE_PATHS["kuzhambu-ai"]

    assert not any(token in source_text for token in FORMAL_DISCOVERY_QA_FORBIDDEN_TOKENS), (
        "WORKERS_NO_DISCOVERY_QA_RUNTIME / WORKERS_NO_KNOWLEDGE_SYNC_TASK"
    )
    assert "/internal/discovery/qa" not in app_routes, "WORKERS_NO_DISCOVERY_QA_RUNTIME"


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
