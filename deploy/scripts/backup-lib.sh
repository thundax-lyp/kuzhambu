#!/usr/bin/env bash

set -euo pipefail

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly DEFAULT_WHITELIST_FILE="${SCRIPT_DIR}/business-table-whitelist.txt"

backup_log() {
  printf '[backup] %s\n' "$*"
}

backup_fail() {
  printf '[backup][error] %s\n' "$*" >&2
  exit 1
}

require_command() {
  command -v "$1" >/dev/null 2>&1 || backup_fail "missing required command: $1"
}

require_env() {
  local name="$1"
  if [[ -z "${!name:-}" ]]; then
    backup_fail "missing required environment variable: ${name}"
  fi
}

resolve_now() {
  date '+%Y%m%d-%H%M%S'
}

load_table_whitelist() {
  local whitelist_file="${1:-$DEFAULT_WHITELIST_FILE}"
  [[ -f "$whitelist_file" ]] || backup_fail "whitelist file not found: $whitelist_file"

  mapfile -t BACKUP_TABLES < <(grep -v '^[[:space:]]*#' "$whitelist_file" | awk 'NF > 0')
  [[ "${#BACKUP_TABLES[@]}" -gt 0 ]] || backup_fail "whitelist file is empty: $whitelist_file"
}

mysql_args() {
  require_env KUZHAMBU_DB_HOST
  require_env KUZHAMBU_DB_PORT
  require_env KUZHAMBU_DB_NAME
  require_env KUZHAMBU_DB_USERNAME
  require_env KUZHAMBU_DB_PASSWORD

  printf -- "--host=%s --port=%s --user=%s --password=%s %s" \
    "${KUZHAMBU_DB_HOST}" \
    "${KUZHAMBU_DB_PORT}" \
    "${KUZHAMBU_DB_USERNAME}" \
    "${KUZHAMBU_DB_PASSWORD}" \
    "${KUZHAMBU_DB_NAME}"
}

ensure_backup_root() {
  : "${KUZHAMBU_BACKUP_ROOT_PATH:=/backup/kuzhambu}"
  mkdir -p "${KUZHAMBU_BACKUP_ROOT_PATH}"
}

storage_mode() {
  printf '%s' "${KUZHAMBU_OSS_TYPE:-local}"
}

storage_archive_name() {
  local base_name="$1"
  printf '%s.storage.tar.gz' "${base_name}"
}

sql_checksum_name() {
  local sql_file="$1"
  printf '%s.sha256' "${sql_file}"
}

write_sha256() {
  local file_path="$1"
  sha256sum "$file_path" > "${file_path}.sha256"
}

read_sha256_value() {
  local file_path="$1"
  [[ -f "${file_path}.sha256" ]] || backup_fail "checksum file not found: ${file_path}.sha256"
  awk '{print $1}' "${file_path}.sha256"
}

backup_local_storage() {
  local base_name="$1"
  local output_dir="$2"

  : "${KUZHAMBU_BACKUP_LOCAL_STORAGE_ROOT_PATH:=/app/storage/object}"
  [[ -d "${KUZHAMBU_BACKUP_LOCAL_STORAGE_ROOT_PATH}" ]] \
    || backup_fail "local storage root not found: ${KUZHAMBU_BACKUP_LOCAL_STORAGE_ROOT_PATH}"

  local archive_file="${output_dir}/$(storage_archive_name "$base_name")"
  local storage_root
  local storage_parent
  local storage_name

  storage_root="$(cd "${KUZHAMBU_BACKUP_LOCAL_STORAGE_ROOT_PATH}" && pwd)"
  storage_parent="$(dirname "${storage_root}")"
  storage_name="$(basename "${storage_root}")"

  [[ "${archive_file}" != "${storage_root}"* ]] \
    || backup_fail "backup output directory must not be inside the local storage root"

  tar -C "${storage_parent}" -czf "${archive_file}" "${storage_name}"
  write_sha256 "${archive_file}"

  backup_log "local storage archived to ${archive_file}"
}

aws_env() {
  require_env KUZHAMBU_OSS_S3_REGION
  require_env KUZHAMBU_OSS_S3_BUCKET
  require_env KUZHAMBU_OSS_S3_ACCESS_KEY
  require_env KUZHAMBU_OSS_S3_SECRET_KEY

  export AWS_ACCESS_KEY_ID="${KUZHAMBU_OSS_S3_ACCESS_KEY}"
  export AWS_SECRET_ACCESS_KEY="${KUZHAMBU_OSS_S3_SECRET_KEY}"
  export AWS_DEFAULT_REGION="${KUZHAMBU_OSS_S3_REGION}"
  if [[ -n "${KUZHAMBU_OSS_S3_ENDPOINT:-}" ]]; then
    export AWS_ENDPOINT_URL_S3="${KUZHAMBU_OSS_S3_ENDPOINT}"
  fi
}

aws_s3_cmd() {
  local endpoint_args=()
  if [[ -n "${KUZHAMBU_OSS_S3_ENDPOINT:-}" ]]; then
    endpoint_args+=(--endpoint-url "${KUZHAMBU_OSS_S3_ENDPOINT}")
  fi
  if [[ "${KUZHAMBU_OSS_S3_PATH_STYLE_ACCESS:-true}" == "true" ]]; then
    endpoint_args+=(--no-progress)
  fi
  aws "${endpoint_args[@]}" s3 "$@"
}

backup_s3_storage() {
  local base_name="$1"
  local output_dir="$2"

  require_command aws
  aws_env

  : "${KUZHAMBU_OSS_S3_LOCATION_PREFIX:=}"

  local stage_dir="${output_dir}/.${base_name}.storage-stage"
  local archive_file="${output_dir}/$(storage_archive_name "$base_name")"
  local bucket_uri="s3://${KUZHAMBU_OSS_S3_BUCKET}"
  local source_uri

  if [[ -n "${KUZHAMBU_OSS_S3_LOCATION_PREFIX}" ]]; then
    source_uri="${bucket_uri%/}/${KUZHAMBU_OSS_S3_LOCATION_PREFIX#/}"
  else
    source_uri="${bucket_uri}"
  fi

  rm -rf "${stage_dir}"
  mkdir -p "${stage_dir}"

  aws_s3_cmd sync "${source_uri}" "${stage_dir}"
  tar -C "${output_dir}" -czf "${archive_file}" ".${base_name}.storage-stage"
  write_sha256 "${archive_file}"
  rm -rf "${stage_dir}"

  backup_log "s3 storage archived to ${archive_file} from ${source_uri}"
}

backup_storage() {
  local base_name="$1"
  local output_dir="$2"
  case "$(storage_mode)" in
    local)
      backup_local_storage "${base_name}" "${output_dir}"
      ;;
    s3)
      backup_s3_storage "${base_name}" "${output_dir}"
      ;;
    *)
      backup_fail "unsupported storage mode: $(storage_mode)"
      ;;
  esac
}

restore_local_storage() {
  local archive_file="$1"

  : "${KUZHAMBU_BACKUP_LOCAL_STORAGE_ROOT_PATH:=/app/storage/object}"
  local storage_root
  local storage_parent

  storage_root="$(cd "${KUZHAMBU_BACKUP_LOCAL_STORAGE_ROOT_PATH}" && pwd)"
  storage_parent="$(dirname "${storage_root}")"

  rm -rf "${storage_root}"
  mkdir -p "${storage_parent}"
  tar -C "${storage_parent}" -xzf "${archive_file}"
}

restore_s3_storage() {
  local archive_file="$1"

  require_command aws
  aws_env

  : "${KUZHAMBU_OSS_S3_LOCATION_PREFIX:=}"

  local extract_dir
  local source_dir
  local bucket_uri="s3://${KUZHAMBU_OSS_S3_BUCKET}"
  local target_uri

  extract_dir="$(mktemp -d)"
  tar -C "${extract_dir}" -xzf "${archive_file}"
  source_dir="$(find "${extract_dir}" -mindepth 1 -maxdepth 1 -type d | head -n 1)"
  [[ -n "${source_dir}" ]] || backup_fail "invalid s3 storage archive: ${archive_file}"

  if [[ -n "${KUZHAMBU_OSS_S3_LOCATION_PREFIX}" ]]; then
    target_uri="${bucket_uri%/}/${KUZHAMBU_OSS_S3_LOCATION_PREFIX#/}"
  else
    target_uri="${bucket_uri}"
  fi

  aws_s3_cmd sync "${source_dir}" "${target_uri}" --delete
  rm -rf "${extract_dir}"
}

restore_storage() {
  local archive_file="$1"
  case "$(storage_mode)" in
    local)
      restore_local_storage "${archive_file}"
      ;;
    s3)
      restore_s3_storage "${archive_file}"
      ;;
    *)
      backup_fail "unsupported storage mode: $(storage_mode)"
      ;;
  esac
}
