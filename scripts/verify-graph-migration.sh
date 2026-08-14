#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CONTENT_TYPE="SANCAI_ENTRY"
BASELINE_FILE="${KUZHAMBU_GRAPH_MIGRATION_BASELINE_FILE:-}"
MYSQL_BIN="${MYSQL_BIN:-mysql}"

echo "Verify graph migration counts for content_type=${CONTENT_TYPE}"

if [[ -z "${BASELINE_FILE}" ]]; then
    echo "KUZHAMBU_GRAPH_MIGRATION_BASELINE_FILE is required; refusing to infer mappings or auto publish." >&2
    exit 2
fi

if [[ ! -f "${BASELINE_FILE}" ]]; then
    echo "Graph migration baseline file does not exist: ${BASELINE_FILE}" >&2
    exit 2
fi

if [[ -z "${KUZHAMBU_DB_NAME:-}" ]]; then
    echo "KUZHAMBU_DB_NAME is required." >&2
    exit 2
fi

MYSQL_ARGS=("--batch" "--skip-column-names" "--database=${KUZHAMBU_DB_NAME}")
if [[ -n "${KUZHAMBU_DB_HOST:-}" ]]; then
    MYSQL_ARGS+=("--host=${KUZHAMBU_DB_HOST}")
fi
if [[ -n "${KUZHAMBU_DB_PORT:-}" ]]; then
    MYSQL_ARGS+=("--port=${KUZHAMBU_DB_PORT}")
fi
if [[ -n "${KUZHAMBU_DB_USER:-}" ]]; then
    MYSQL_ARGS+=("--user=${KUZHAMBU_DB_USER}")
fi
if [[ -n "${KUZHAMBU_DB_PASSWORD:-}" ]]; then
    MYSQL_PWD="${KUZHAMBU_DB_PASSWORD}" "${MYSQL_BIN}" --version >/dev/null
fi

query_scalar() {
    local sql="$1"
    if [[ -n "${KUZHAMBU_DB_PASSWORD:-}" ]]; then
        MYSQL_PWD="${KUZHAMBU_DB_PASSWORD}" "${MYSQL_BIN}" "${MYSQL_ARGS[@]}" --execute="${sql}"
    else
        "${MYSQL_BIN}" "${MYSQL_ARGS[@]}" --execute="${sql}"
    fi
}

expected_value() {
    local key="$1"
    local value
    value="$(awk -F '=' -v key="${key}" '$1 == key {print $2}' "${BASELINE_FILE}")"
    if [[ -z "${value}" ]]; then
        echo "Missing graph migration baseline key: ${key}" >&2
        exit 2
    fi
    echo "${value}"
}

assert_count() {
    local key="$1"
    local sql="$2"
    local expected actual
    expected="$(expected_value "${key}")"
    actual="$(query_scalar "${sql}")"
    if [[ "${actual}" != "${expected}" ]]; then
        echo "Graph migration mismatch for ${key}: expected=${expected}, actual=${actual}" >&2
        exit 1
    fi
    echo "Graph migration ${key}: ${actual}"
}

assert_count \
    "material_count" \
    "select count(*) from knowledge_graph_material where content_type = '${CONTENT_TYPE}';"
assert_count \
    "material_node_count" \
    "select count(*) from knowledge_graph_material_node n join knowledge_graph_material m on m.id = n.material_id where m.content_type = '${CONTENT_TYPE}';"
assert_count \
    "material_edge_count" \
    "select count(*) from knowledge_graph_material_edge e join knowledge_graph_material m on m.id = e.material_id where m.content_type = '${CONTENT_TYPE}';"
assert_count \
    "published_node_mapping_count" \
    "select count(*) from knowledge_graph_published_node_material where content_type = '${CONTENT_TYPE}';"
assert_count \
    "published_edge_mapping_count" \
    "select count(*) from knowledge_graph_published_edge_material where content_type = '${CONTENT_TYPE}';"
assert_count \
    "node_mapping_count" \
    "select count(*) from knowledge_graph_material_node_mapping nm join knowledge_graph_material m on m.id = nm.material_id where m.content_type = '${CONTENT_TYPE}';"
assert_count \
    "edge_mapping_count" \
    "select count(*) from knowledge_graph_material_edge_mapping em join knowledge_graph_material m on m.id = em.material_id where m.content_type = '${CONTENT_TYPE}';"

echo "Graph migration count verification complete from ${ROOT_DIR}"
