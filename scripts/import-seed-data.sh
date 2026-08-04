#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

ENV_FILE="${REPO_ROOT}/dev.env"
GENERATE=true
NODE_ARGS=()

usage() {
    node "${SCRIPT_DIR}/seed/import-to-database.mjs" --help
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        --)
            shift
            ;;
        --env)
            ENV_FILE="$2"
            NODE_ARGS+=("$1" "$2")
            shift 2
            ;;
        --skip-generate)
            GENERATE=false
            shift
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            NODE_ARGS+=("$1")
            if [[ "$1" == "--jobs" || "$1" == "--schema" || "$1" == "--data" ]]; then
                NODE_ARGS+=("$2")
                shift 2
            else
                shift
            fi
            ;;
    esac
done

if [[ ! -f "${ENV_FILE}" ]]; then
    echo "error: env file not found: ${ENV_FILE}" >&2
    exit 1
fi

if [[ ! -d "${SCRIPT_DIR}/node_modules/mysql2" ]]; then
    pnpm --dir "${SCRIPT_DIR}" install
fi

if [[ "${GENERATE}" == "true" ]]; then
    node "${SCRIPT_DIR}/seed/generate-system-sql.mjs"
    node "${SCRIPT_DIR}/seed/generate-ai-sql.mjs"
    node "${SCRIPT_DIR}/seed/generate-sancai-knowledge-sql.mjs"
    node "${SCRIPT_DIR}/seed/generate-classics-sql.mjs"
fi

echo "Importing seed data with env: ${ENV_FILE}"
node "${SCRIPT_DIR}/seed/import-to-database.mjs" "${NODE_ARGS[@]}"
