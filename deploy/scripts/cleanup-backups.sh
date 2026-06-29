#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=backup-lib.sh
source "${SCRIPT_DIR}/backup-lib.sh"

ensure_backup_root

RETENTION_DAYS="${KUZHAMBU_BACKUP_RETENTION_DAYS:-30}"

backup_log "cleaning backup artifacts older than ${RETENTION_DAYS} days in ${KUZHAMBU_BACKUP_ROOT_PATH}"

find "${KUZHAMBU_BACKUP_ROOT_PATH}" -type f \( \
  -name 'backup_*.sql' -o \
  -name 'backup_*.sql.sha256' -o \
  -name 'backup_*.storage.tar.gz' -o \
  -name 'backup_*.storage.tar.gz.sha256' -o \
  -name 'prerestore_*.sql' -o \
  -name 'prerestore_*.sql.sha256' -o \
  -name 'prerestore_*.storage.tar.gz' -o \
  -name 'prerestore_*.storage.tar.gz.sha256' \
\) -mtime +"${RETENTION_DAYS}" -print -delete

backup_log "cleanup completed"
