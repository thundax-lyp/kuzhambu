#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="${1:-${SCRIPT_DIR}/.env}"
COMPOSE_FILE="${2:-${SCRIPT_DIR}/docker-compose.yml}"
OUTPUT_DIR="${SCRIPT_DIR}/generated"
OUTPUT_FILE="${OUTPUT_DIR}/kuzhambu-fastgpt.env"

if [[ ! -f "${ENV_FILE}" ]]; then
    echo "Missing FastGPT env file: ${ENV_FILE}" >&2
    exit 1
fi

set -a
# shellcheck disable=SC1090
source "${ENV_FILE}"
set +a

PROJECT_NAME="${FASTGPT_COMPOSE_PROJECT_NAME:-kuzhambu-fastgpt}"

mkdir -p "${OUTPUT_DIR}"

docker compose --env-file "${ENV_FILE}" -f "${COMPOSE_FILE}" -p "${PROJECT_NAME}" up -d fastgpt-app
docker compose --env-file "${ENV_FILE}" -f "${COMPOSE_FILE}" -p "${PROJECT_NAME}" run --rm fastgpt-bootstrap \
    | grep '^KUZHAMBU_KNOWLEDGE_' > "${OUTPUT_FILE}"

chmod 600 "${OUTPUT_FILE}"
echo "Generated ${OUTPUT_FILE}"
