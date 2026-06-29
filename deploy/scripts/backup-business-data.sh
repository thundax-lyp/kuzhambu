#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=backup-lib.sh
source "${SCRIPT_DIR}/backup-lib.sh"

require_command mysqldump
require_command sha256sum
require_command tar

ensure_backup_root

BACKUP_TYPE="${BACKUP_TYPE:-MANUAL}"
BACKUP_PREFIX="${BACKUP_PREFIX:-backup}"
WHITELIST_FILE="${WHITELIST_FILE:-${DEFAULT_WHITELIST_FILE}}"
TIMESTAMP="${TIMESTAMP:-$(resolve_now)}"
BASE_NAME="${BACKUP_PREFIX}_${TIMESTAMP}"
OUTPUT_DIR="${KUZHAMBU_BACKUP_ROOT_PATH}"
SQL_FILE="${OUTPUT_DIR}/${BASE_NAME}.sql"

load_table_whitelist "${WHITELIST_FILE}"

backup_log "starting ${BACKUP_TYPE} backup: ${BASE_NAME}"
backup_log "loaded ${#BACKUP_TABLES[@]} tables from ${WHITELIST_FILE}"

MYSQL_ARGS="$(mysql_args)"
mysqldump ${MYSQL_ARGS} "${BACKUP_TABLES[@]}" > "${SQL_FILE}"
write_sha256 "${SQL_FILE}"
backup_storage "${BASE_NAME}" "${OUTPUT_DIR}"

backup_log "sql backup written to ${SQL_FILE}"
backup_log "checksum written to $(sql_checksum_name "${SQL_FILE}")"
backup_log "backup completed successfully"
printf 'BACKUP_BASE_NAME=%s\n' "${BASE_NAME}"
printf 'BACKUP_SQL_FILE=%s\n' "${SQL_FILE}"
printf 'BACKUP_SQL_CHECKSUM=%s\n' "$(read_sha256_value "${SQL_FILE}")"
printf 'BACKUP_STORAGE_ARCHIVE=%s\n' "${OUTPUT_DIR}/$(storage_archive_name "${BASE_NAME}")"
printf 'BACKUP_STORAGE_CHECKSUM=%s\n' \
  "$(read_sha256_value "${OUTPUT_DIR}/$(storage_archive_name "${BASE_NAME}")")"
