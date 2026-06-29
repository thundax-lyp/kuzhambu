#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=backup-lib.sh
source "${SCRIPT_DIR}/backup-lib.sh"

require_command mysql
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
POST_RESTORE_COMMAND="${KUZHAMBU_POST_RESTORE_COMMAND:-}"
RUN_PRE_RESTORE="${RUN_PRE_RESTORE:-true}"
SQL_FILE="${KUZHAMBU_BACKUP_ROOT_PATH}/${BACKUP_NAME}.sql"
SQL_SHA_FILE="${SQL_FILE}.sha256"
STORAGE_ARCHIVE="${KUZHAMBU_BACKUP_ROOT_PATH}/$(storage_archive_name "${BACKUP_NAME}")"
STORAGE_SHA_FILE="${STORAGE_ARCHIVE}.sha256"

[[ -f "${SQL_FILE}" ]] || backup_fail "backup sql file not found: ${SQL_FILE}"
[[ -f "${SQL_SHA_FILE}" ]] || backup_fail "backup sql checksum file not found: ${SQL_SHA_FILE}"
[[ -f "${STORAGE_ARCHIVE}" ]] || backup_fail "storage archive file not found: ${STORAGE_ARCHIVE}"
[[ -f "${STORAGE_SHA_FILE}" ]] || backup_fail "storage archive checksum file not found: ${STORAGE_SHA_FILE}"

sha256sum -c "${SQL_SHA_FILE}"
sha256sum -c "${STORAGE_SHA_FILE}"

if [[ "${RUN_PRE_RESTORE}" == "true" ]]; then
  backup_log "creating pre-restore snapshot"
  BACKUP_TYPE="PRE_RESTORE" BACKUP_PREFIX="prerestore" "${SCRIPT_DIR}/backup-business-data.sh"
fi

load_table_whitelist "${WHITELIST_FILE}"
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
