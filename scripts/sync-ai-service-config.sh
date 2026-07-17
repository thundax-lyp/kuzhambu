#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${1:-${ROOT_DIR}/dev.env}"

usage() {
    cat <<'USAGE'
Usage:
  scripts/sync-ai-service-config.sh [env_file]

Reads local AI service connection settings from env_file and upserts
ai_model endpoint settings. Missing API keys or base URLs preserve existing database
values; real secrets are never read from db/data/ai.sql.

Required database env:
  MYSQL_HOST, MYSQL_PORT, MYSQL_DATABASE, MYSQL_USER, MYSQL_PASSWORD

Optional:
  MYSQL_CLIENT_BIN=mysql

AI service env groups:
  KUZHAMBU_AI_PRIMARY_MODEL_ID=900102
  KUZHAMBU_AI_PRIMARY_API_SOURCE=OPENAI
  KUZHAMBU_AI_PRIMARY_BASE_URL=
  KUZHAMBU_AI_PRIMARY_API_KEY=
  KUZHAMBU_AI_PRIMARY_ENABLED=1

  KUZHAMBU_AI_VISION_MODEL_ID=900101
  KUZHAMBU_AI_VISION_API_SOURCE=OPENAI
  KUZHAMBU_AI_VISION_BASE_URL=
  KUZHAMBU_AI_VISION_API_KEY=
  KUZHAMBU_AI_VISION_ENABLED=1

  KUZHAMBU_AI_BACKUP_MODEL_ID=
  KUZHAMBU_AI_BACKUP_API_SOURCE=OPENAI
  KUZHAMBU_AI_BACKUP_BASE_URL=
  KUZHAMBU_AI_BACKUP_API_KEY=
  KUZHAMBU_AI_BACKUP_ENABLED=1

  KUZHAMBU_AI_TEXT2IMAGE_MODEL_ID=900201
  KUZHAMBU_AI_TEXT2IMAGE_API_SOURCE=BYTEDANCE
  KUZHAMBU_AI_TEXT2IMAGE_BASE_URL=
  KUZHAMBU_AI_TEXT2IMAGE_API_KEY=
  KUZHAMBU_AI_TEXT2IMAGE_ENABLED=1
USAGE
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
    usage
    exit 0
fi

if [[ ! -f "${ENV_FILE}" ]]; then
    echo "error: env file not found: ${ENV_FILE}" >&2
    exit 1
fi

set -a
# shellcheck disable=SC1090
source "${ENV_FILE}"
set +a

required_env=(
    MYSQL_HOST
    MYSQL_PORT
    MYSQL_DATABASE
    MYSQL_USER
    MYSQL_PASSWORD
)

for env_name in "${required_env[@]}"; do
    if [[ -z "${!env_name:-}" ]]; then
        echo "error: missing required env ${env_name}" >&2
        exit 1
    fi
done

MYSQL_CLIENT_BIN="${MYSQL_CLIENT_BIN:-mysql}"
if ! command -v "${MYSQL_CLIENT_BIN}" >/dev/null 2>&1; then
    echo "error: mysql client not found: ${MYSQL_CLIENT_BIN}" >&2
    exit 1
fi

sql_string() {
    local value="${1:-}"
    if [[ -z "${value}" ]]; then
        printf "NULL"
        return
    fi
    value="${value//\\/\\\\}"
    value="${value//\'/\'\'}"
    printf "'%s'" "${value}"
}

sql_number() {
    local value="${1:-}"
    if [[ -z "${value}" ]]; then
        printf "NULL"
        return
    fi
    if [[ ! "${value}" =~ ^[0-9]+$ ]]; then
        echo "error: invalid numeric value '${value}'" >&2
        exit 1
    fi
    printf "%s" "${value}"
}

append_model_sql() {
    local prefix="$1"
    local model_role="$2"
    local model_id_var="KUZHAMBU_AI_${prefix}_MODEL_ID"
    local model_id="${!model_id_var:-}"

    if [[ -z "${model_id}" ]]; then
        echo "skip ${model_role}: ${model_id_var} is empty"
        return
    fi

    local api_source_var="KUZHAMBU_AI_${prefix}_API_SOURCE"
    local base_url_var="KUZHAMBU_AI_${prefix}_BASE_URL"
    local api_key_var="KUZHAMBU_AI_${prefix}_API_KEY"
    local enabled_var="KUZHAMBU_AI_${prefix}_ENABLED"

    local api_source="${!api_source_var:-OPENAI}"
    local base_url="${!base_url_var:-}"
    local api_key="${!api_key_var:-}"
    local enabled="${!enabled_var:-1}"

    cat >> "${SQL_FILE}" <<SQL
UPDATE \`ai_model\`
SET
    \`api_source\` = $(sql_string "${api_source}"),
    \`base_url\` = COALESCE(NULLIF($(sql_string "${base_url}"), ''), \`base_url\`),
    \`encrypted_api_key\` = COALESCE($(sql_string "${api_key}"), \`encrypted_api_key\`),
    \`enabled\` = $(sql_number "${enabled}")
WHERE \`id\` = $(sql_number "${model_id}");

SQL

    echo "queue ${model_role}: model_id=${model_id}"
}

SQL_FILE="$(mktemp /tmp/kuzhambu-ai-service-config-XXXXXX.sql)"
chmod 600 "${SQL_FILE}"
trap 'rm -f "${SQL_FILE}"' EXIT

cat > "${SQL_FILE}" <<'SQL'
SET NAMES utf8mb4;

SQL

append_model_sql "PRIMARY" "PRIMARY"
append_model_sql "VISION" "VISION"
append_model_sql "BACKUP" "BACKUP"
append_model_sql "TEXT2IMAGE" "TEXT2IMAGE"

if ! grep -q "UPDATE \`ai_model\`" "${SQL_FILE}"; then
    echo "error: no AI model endpoint config was queued" >&2
    exit 1
fi

MYSQL_PWD="${MYSQL_PASSWORD}" "${MYSQL_CLIENT_BIN}" \
    --host="${MYSQL_HOST}" \
    --port="${MYSQL_PORT}" \
    --user="${MYSQL_USER}" \
    "${MYSQL_DATABASE}" < "${SQL_FILE}"

echo "synced AI model endpoint config from ${ENV_FILE}"
