#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=backup-lib.sh
source "${SCRIPT_DIR}/backup-lib.sh"

require_command sha256sum
require_command tar

ensure_backup_root

if [[ "${1:-}" == "" ]]; then
  backup_fail "usage: $0 <backup-basename-without-extension>"
fi

[[ "${KUZHAMBU_RESTORE_ALLOW:-}" == "YES" ]] \
  || backup_fail "set KUZHAMBU_RESTORE_ALLOW=YES to acknowledge destructive restore"

BACKUP_NAME="$1"
WHITELIST_FILE="${WHITELIST_FILE:-${DEFAULT_WHITELIST_FILE}}"
RESTORE_MODE="${RESTORE_MODE:-REAL}"
POST_RESTORE_COMMAND="${KUZHAMBU_POST_RESTORE_COMMAND:-}"
RUN_PRE_RESTORE="${RUN_PRE_RESTORE:-true}"
PRE_RESTORE_TIMESTAMP="${PRE_RESTORE_TIMESTAMP:-$(resolve_now)}"
SQL_FILE="${KUZHAMBU_BACKUP_ROOT_PATH}/${BACKUP_NAME}.sql"
SQL_SHA_FILE="${SQL_FILE}.sha256"
STORAGE_ARCHIVE="${KUZHAMBU_BACKUP_ROOT_PATH}/$(storage_archive_name "${BACKUP_NAME}")"
STORAGE_SHA_FILE="${STORAGE_ARCHIVE}.sha256"

validate_restore_mode "${RESTORE_MODE}"

[[ -f "${SQL_FILE}" ]] || backup_fail "backup sql file not found: ${SQL_FILE}"
[[ -f "${SQL_SHA_FILE}" ]] || backup_fail "backup sql checksum file not found: ${SQL_SHA_FILE}"
[[ -f "${STORAGE_ARCHIVE}" ]] || backup_fail "storage archive file not found: ${STORAGE_ARCHIVE}"
[[ -f "${STORAGE_SHA_FILE}" ]] || backup_fail "storage archive checksum file not found: ${STORAGE_SHA_FILE}"

sha256sum -c "${SQL_SHA_FILE}"
sha256sum -c "${STORAGE_SHA_FILE}"
validate_storage_archive "${STORAGE_ARCHIVE}"

load_table_whitelist "${WHITELIST_FILE}"

if [[ "${RUN_PRE_RESTORE}" == "true" ]]; then
  backup_log "creating pre-restore snapshot"
  PRE_RESTORE_OUTPUT="$(
    BACKUP_TYPE="PRE_RESTORE" \
    BACKUP_PREFIX="prerestore" \
    TIMESTAMP="${PRE_RESTORE_TIMESTAMP}" \
    "${SCRIPT_DIR}/backup-business-data.sh"
  )"
  printf '%s\n' "${PRE_RESTORE_OUTPUT}"
fi

if [[ "${RESTORE_MODE}" == "DRILL" ]]; then
  backup_log "restore drill validated ${#BACKUP_TABLES[@]} business tables"
  backup_log "restore drill completed successfully"
  printf 'RESTORE_MODE=%s\n' "${RESTORE_MODE}"
  printf 'RESTORE_BACKUP_NAME=%s\n' "${BACKUP_NAME}"
  printf 'DRILL_VALIDATED_TABLE_COUNT=%s\n' "${#BACKUP_TABLES[@]}"
  if [[ "${RUN_PRE_RESTORE}" == "true" ]]; then
    printf 'PRE_RESTORE_BASE_NAME=%s\n' "prerestore_${PRE_RESTORE_TIMESTAMP}"
  fi
  exit 0
fi

require_command mysql

MYSQL_ARGS="$(mysql_args)"

backup_log "clearing ${#BACKUP_TABLES[@]} business tables before import"
mysql ${MYSQL_ARGS} -e "SET FOREIGN_KEY_CHECKS=0;"
for table_name in "${BACKUP_TABLES[@]}"; do
  mysql ${MYSQL_ARGS} -e "DELETE FROM \`${table_name}\`;"
done
mysql ${MYSQL_ARGS} -e "SET FOREIGN_KEY_CHECKS=1;"

backup_log "importing ${SQL_FILE}"
mysql ${MYSQL_ARGS} < "${SQL_FILE}"

backup_log "restoring storage archive ${STORAGE_ARCHIVE}"
restore_storage "${STORAGE_ARCHIVE}"

if [[ -n "${POST_RESTORE_COMMAND}" ]]; then
  backup_log "running post restore command"
  sh -c "${POST_RESTORE_COMMAND}"
fi

backup_log "restore completed successfully"
printf 'RESTORE_MODE=%s\n' "${RESTORE_MODE}"
printf 'RESTORE_BACKUP_NAME=%s\n' "${BACKUP_NAME}"
if [[ "${RUN_PRE_RESTORE}" == "true" ]]; then
  printf 'PRE_RESTORE_BASE_NAME=%s\n' "prerestore_${PRE_RESTORE_TIMESTAMP}"
fi
