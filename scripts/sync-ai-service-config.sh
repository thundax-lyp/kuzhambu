#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${1:-${ROOT_DIR}/dev.env}"

usage() {
    cat <<'USAGE'
Usage:
  scripts/sync-ai-service-config.sh [env_file]

Reads local AI service connection settings from env_file and upserts
ai_service_config. Missing API keys or base URLs preserve existing database
values; real secrets are never read from db/data/ai.sql.

Required database env:
  MYSQL_HOST, MYSQL_PORT, MYSQL_DATABASE, MYSQL_USER, MYSQL_PASSWORD

Optional:
  MYSQL_CLIENT_BIN=mysql

AI service env groups:
  KUZHAMBU_AI_PRIMARY_SERVICE_ID=900001
  KUZHAMBU_AI_PRIMARY_API_SOURCE=OPENAI_COMPATIBLE
  KUZHAMBU_AI_PRIMARY_BASE_URL=
  KUZHAMBU_AI_PRIMARY_API_KEY=
  KUZHAMBU_AI_PRIMARY_ENABLED=1
  KUZHAMBU_AI_PRIMARY_STATUS=AVAILABLE

  KUZHAMBU_AI_BACKUP_SERVICE_ID=
  KUZHAMBU_AI_BACKUP_API_SOURCE=OPENAI_COMPATIBLE
  KUZHAMBU_AI_BACKUP_BASE_URL=
  KUZHAMBU_AI_BACKUP_API_KEY=
  KUZHAMBU_AI_BACKUP_ENABLED=1
  KUZHAMBU_AI_BACKUP_STATUS=UNAVAILABLE

  KUZHAMBU_AI_TEXT2IMAGE_SERVICE_ID=900002
  KUZHAMBU_AI_TEXT2IMAGE_API_SOURCE=OPENAI_COMPATIBLE
  KUZHAMBU_AI_TEXT2IMAGE_BASE_URL=
  KUZHAMBU_AI_TEXT2IMAGE_API_KEY=
  KUZHAMBU_AI_TEXT2IMAGE_ENABLED=1
  KUZHAMBU_AI_TEXT2IMAGE_STATUS=AVAILABLE
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

append_service_sql() {
    local prefix="$1"
    local role="$2"
    local service_id_var="KUZHAMBU_AI_${prefix}_SERVICE_ID"
    local service_id="${!service_id_var:-}"

    if [[ -z "${service_id}" ]]; then
        echo "skip ${role}: ${service_id_var} is empty"
        return
    fi

    local api_source_var="KUZHAMBU_AI_${prefix}_API_SOURCE"
    local base_url_var="KUZHAMBU_AI_${prefix}_BASE_URL"
    local api_key_var="KUZHAMBU_AI_${prefix}_API_KEY"
    local enabled_var="KUZHAMBU_AI_${prefix}_ENABLED"
    local status_var="KUZHAMBU_AI_${prefix}_STATUS"

    local api_source="${!api_source_var:-OPENAI_COMPATIBLE}"
    local base_url="${!base_url_var:-}"
    local api_key="${!api_key_var:-}"
    local enabled="${!enabled_var:-1}"
    local status="${!status_var:-AVAILABLE}"

    cat >> "${SQL_FILE}" <<SQL
INSERT INTO \`ai_service_config\` (
    \`service_id\`, \`service_role\`, \`api_source\`, \`base_url\`, \`encrypted_api_key\`,
    \`enabled\`, \`status\`, \`last_checked_at\`, \`configured_at\`
) VALUES (
    $(sql_number "${service_id}"), $(sql_string "${role}"), $(sql_string "${api_source}"),
    COALESCE($(sql_string "${base_url}"), ''),
    $(sql_string "${api_key}"),
    $(sql_number "${enabled}"), $(sql_string "${status}"), NULL, NOW(3)
)
ON DUPLICATE KEY UPDATE
    \`api_source\` = VALUES(\`api_source\`),
    \`base_url\` = COALESCE(NULLIF(VALUES(\`base_url\`), ''), \`base_url\`),
    \`encrypted_api_key\` = COALESCE(VALUES(\`encrypted_api_key\`), \`encrypted_api_key\`),
    \`enabled\` = VALUES(\`enabled\`),
    \`status\` = VALUES(\`status\`),
    \`configured_at\` = NOW(3);

SQL

    echo "queue ${role}: service_id=${service_id}"
}

SQL_FILE="$(mktemp /tmp/kuzhambu-ai-service-config-XXXXXX.sql)"
chmod 600 "${SQL_FILE}"
trap 'rm -f "${SQL_FILE}"' EXIT

cat > "${SQL_FILE}" <<'SQL'
SET NAMES utf8mb4;

SQL

append_service_sql "PRIMARY" "PRIMARY"
append_service_sql "BACKUP" "BACKUP"
append_service_sql "TEXT2IMAGE" "TEXT2IMAGE"

if ! grep -q "INSERT INTO \`ai_service_config\`" "${SQL_FILE}"; then
    echo "error: no AI service config was queued" >&2
    exit 1
fi

MYSQL_PWD="${MYSQL_PASSWORD}" "${MYSQL_CLIENT_BIN}" \
    --host="${MYSQL_HOST}" \
    --port="${MYSQL_PORT}" \
    --user="${MYSQL_USER}" \
    "${MYSQL_DATABASE}" < "${SQL_FILE}"

echo "synced AI service config from ${ENV_FILE}"
