#!/usr/bin/env bash

# Restore Sancai content from a structured JSON snapshot to SQL.

set -euo pipefail

SCRIPT_NAME=$(basename "$0")
MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-}"
MYSQL_DATABASE="${MYSQL_DATABASE:-kuzhambu}"
MYSQL_CLIENT_BIN="${MYSQL_CLIENT_BIN:-mysql}"

DRY_RUN=0
SNAPSHOT="./sancai_tree_snapshot.json"

usage() {
    cat <<'EOF'
Usage:
  classics-json-to-sql.sh [--dry-run] [snapshot_file]

Environment variables:
  MYSQL_HOST           MySQL host (default: 127.0.0.1)
  MYSQL_PORT           MySQL port (default: 3306)
  MYSQL_USER           MySQL user (default: root)
  MYSQL_PASSWORD       MySQL password (default: empty)
  MYSQL_DATABASE       Target database (default: kuzhambu)
  MYSQL_CLIENT_BIN     mysql client binary (default: mysql)

Examples:
  classics-json-to-sql.sh
  classics-json-to-sql.sh --dry-run ./sancai_tree_snapshot.json
EOF
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        -h|--help)
            usage
            exit 0
            ;;
        --dry-run)
            DRY_RUN=1
            shift
            ;;
        *)
            if [[ "$SNAPSHOT" == "./sancai_tree_snapshot.json" ]]; then
                SNAPSHOT="$1"
            else
                echo "error: unexpected argument '$1'" >&2
                usage
                exit 1
            fi
            shift
            ;;
    esac
done

if ! command -v "$MYSQL_CLIENT_BIN" >/dev/null 2>&1; then
    echo "error: $MYSQL_CLIENT_BIN not found" >&2
    exit 1
fi

if ! command -v jq >/dev/null 2>&1; then
    echo "error: jq not found" >&2
    exit 1
fi

if [[ ! -f "$SNAPSHOT" ]]; then
    echo "error: snapshot not found: $SNAPSHOT" >&2
    exit 1
fi

if ! jq -e '.schema == "classics_sancai_tree"' "$SNAPSHOT" >/dev/null; then
    echo "error: invalid snapshot format, expected schema=classics_sancai_tree" >&2
    exit 1
fi

TMP_SQL=$(mktemp /tmp/classics-json-to-sql-XXXXXX.sql)
trap 'rm -f "$TMP_SQL"' EXIT

cat > "$TMP_SQL" <<'SQL'
SET autocommit=0;
START TRANSACTION;
SQL

cat >> "$TMP_SQL" <<'SQL'
-- category
SQL
jq -r '
def b64_text(v):
  if v == null then
    "NULL"
  else
    "CAST(FROM_BASE64(\"" + (v|@base64) + "\") AS CHAR CHARACTER SET utf8mb4)"
  end;

def bool_num(v):
  if v == true or v == 1 then "1" else "0" end;

def storage_object_id_expr($object_key; $bucket_name):
  if $object_key == null then
    "NULL"
  else
    "(SELECT id FROM storage_object WHERE object_key = " + b64_text($object_key) + " AND " +
    (if $bucket_name == null then "bucket_name IS NULL" else "bucket_name = " + b64_text($bucket_name) end) + " LIMIT 1)"
  end;

.categories[]
| "INSERT INTO classics_sancai_category (`category_code`, `name`, `formal`, `sort_order`, `description`) VALUES (" +
  b64_text(.category_code) + ", " +
  b64_text(.name) + ", " +
  bool_num(.formal) + ", " +
  ((.sort_order // 0) | tostring) + ", " +
  b64_text(.description) +
") ON DUPLICATE KEY UPDATE " +
  "`name` = VALUES(`name`), `formal` = VALUES(`formal`), `sort_order` = VALUES(`sort_order`), `description` = VALUES(`description`);"
' "$SNAPSHOT" >> "$TMP_SQL"

cat >> "$TMP_SQL" <<'SQL'

-- volume
SQL
jq -r '
def b64_text(v):
  if v == null then
    "NULL"
  else
    "CAST(FROM_BASE64(\"" + (v|@base64) + "\") AS CHAR CHARACTER SET utf8mb4)"
  end;

.categories[]
| .volumes[]
| "INSERT INTO classics_sancai_volume (`volume_id`, `category_code`, `volume_no`, `title`, `auxiliary`, `sort_order`, `entry_count`) VALUES (" +
  (.volume_id // 0 | tostring) + ", " +
  b64_text(.category_code) + ", " +
  (if .volume_no == null then "NULL" else (.volume_no | tostring) end) + ", " +
  b64_text(.title) + ", " +
  (if .auxiliary == true or .auxiliary == 1 then "1" else "0" end | tostring) + ", " +
  ((.sort_order // 0) | tostring) + ", " +
  ((.entry_count // 0) | tostring) +
") ON DUPLICATE KEY UPDATE " +
  "`title` = VALUES(`title`), `auxiliary` = VALUES(`auxiliary`), `sort_order` = VALUES(`sort_order`), `entry_count` = VALUES(`entry_count`), `volume_no` = VALUES(`volume_no`), `category_code` = VALUES(`category_code`);"
' "$SNAPSHOT" >> "$TMP_SQL"

cat >> "$TMP_SQL" <<'SQL'

-- entry
SQL
jq -r '
def b64_text(v):
  if v == null then
    "NULL"
  else
    "CAST(FROM_BASE64(\"" + (v|@base64) + "\") AS CHAR CHARACTER SET utf8mb4)"
  end;

.categories[]
| .volumes[]
| .entries[]
| "INSERT INTO classics_sancai_entry (`entry_id`, `category_code`, `volume_id`, `entry_no`, `title`, `original_text`, `translation_text`, `summary`, `tags_snapshot`, `lifecycle_status`, `visibility`, `owner_user_id`, `translation_status`, `image_status`, `visual_asset_status`, `refinement_status`, `current_version`) VALUES (" +
  (.entry_id | tostring) + ", " +
  b64_text(.category_code) + ", " +
  (.volume_id | tostring) + ", " +
  (if .entry_no == null then "NULL" else (.entry_no | tostring) end) + ", " +
  b64_text(.title) + ", " +
  b64_text(.original_text) + ", " +
  b64_text(.translation_text) + ", " +
  b64_text(.summary) + ", " +
  b64_text(.tags_snapshot) + ", " +
  b64_text(.lifecycle_status) + ", " +
  b64_text(.visibility) + ", " +
  (.owner_user_id | tostring) + ", " +
  b64_text(.translation_status) + ", " +
  b64_text(.image_status) + ", " +
  b64_text(.visual_asset_status) + ", " +
  b64_text(.refinement_status) + ", " +
  (.current_version // 0 | tostring) +
") ON DUPLICATE KEY UPDATE " +
  "`title` = VALUES(`title`), `original_text` = VALUES(`original_text`), `translation_text` = VALUES(`translation_text`), `summary` = VALUES(`summary`), `tags_snapshot` = VALUES(`tags_snapshot`), `lifecycle_status` = VALUES(`lifecycle_status`), `visibility` = VALUES(`visibility`), `owner_user_id` = VALUES(`owner_user_id`), `translation_status` = VALUES(`translation_status`), `image_status` = VALUES(`image_status`), `visual_asset_status` = VALUES(`visual_asset_status`), `refinement_status` = VALUES(`refinement_status`), `current_version` = VALUES(`current_version`), `volume_id` = VALUES(`volume_id`), `category_code` = VALUES(`category_code`), `entry_no` = VALUES(`entry_no`);"
' "$SNAPSHOT" >> "$TMP_SQL"

cat >> "$TMP_SQL" <<'SQL'

-- entry images
SQL
jq -r '
def b64_text(v):
  if v == null then
    "NULL"
  else
    "CAST(FROM_BASE64(\"" + (v|@base64) + "\") AS CHAR CHARACTER SET utf8mb4)"
  end;

def storage_object_id_expr($object_key; $bucket_name):
  if $object_key == null then
    "NULL"
  else
    "(SELECT id FROM storage_object WHERE object_key = " + b64_text($object_key) + " AND " +
    (if $bucket_name == null then "bucket_name IS NULL" else "bucket_name = " + b64_text($bucket_name) end) + " LIMIT 1)"
  end;

.categories[]
| .volumes[]
| .entries[] as $entry
| $entry.entry_images[]?
| "INSERT INTO classics_sancai_entry_image (`entry_id`, `object_id`, `image_role`, `current_used`, `sort_order`, `caption`) VALUES (" +
  ($entry.entry_id | tostring) + ", " +
  storage_object_id_expr(.object_key; .bucket_name) + ", " +
  b64_text(.image_role) + ", " +
  (if .current_used == true or .current_used == 1 then "1" else "0" end) + ", " +
  ((.sort_order // 0) | tostring) + ", " +
  b64_text(.caption) +
") ON DUPLICATE KEY UPDATE " +
  "`image_role` = VALUES(`image_role`), `current_used` = VALUES(`current_used`), `sort_order` = VALUES(`sort_order`), `caption` = VALUES(`caption`);"
' "$SNAPSHOT" >> "$TMP_SQL"

cat >> "$TMP_SQL" <<'SQL'

-- entry qa
SQL
jq -r '
def b64_text(v):
  if v == null then
    "NULL"
  else
    "CAST(FROM_BASE64(\"" + (v|@base64) + "\") AS CHAR CHARACTER SET utf8mb4)"
  end;

.categories[]
| .volumes[]
| .entries[] as $entry
| $entry.entry_qas[]?
| "INSERT INTO classics_sancai_entry_qa (`discovery_qa_id`, `entry_id`, `question`, `answer`, `source`, `sort_order`) VALUES (" +
  (.discovery_qa_id | tostring) + ", " +
  ($entry.entry_id | tostring) + ", " +
  b64_text(.question) + ", " +
  b64_text(.answer) + ", " +
  b64_text(.source) + ", " +
  ((.sort_order // 0) | tostring) +
") ON DUPLICATE KEY UPDATE " +
  "`question` = VALUES(`question`), `answer` = VALUES(`answer`), `source` = VALUES(`source`), `sort_order` = VALUES(`sort_order`), `entry_id` = VALUES(`entry_id`);"
' "$SNAPSHOT" >> "$TMP_SQL"

cat >> "$TMP_SQL" <<'SQL'

-- entry versions
SQL
jq -r '
def b64_text(v):
  if v == null then
    "NULL"
  else
    "CAST(FROM_BASE64(\"" + (v|@base64) + "\") AS CHAR CHARACTER SET utf8mb4)"
  end;

.categories[]
| .volumes[]
| .entries[] as $entry
| $entry.entry_versions[]?
| "INSERT INTO classics_sancai_entry_version (`version_id`, `entry_id`, `version_no`, `snapshot_json`, `change_type`, `change_summary`, `versioned_at`) VALUES (" +
  (.version_id | tostring) + ", " +
  ($entry.entry_id | tostring) + ", " +
  ((.version_no // 0) | tostring) + ", " +
  b64_text(.snapshot_json) + ", " +
  b64_text(.change_type) + ", " +
  b64_text(.change_summary) + ", " +
  b64_text(.versioned_at) +
") ON DUPLICATE KEY UPDATE " +
  "`version_no` = VALUES(`version_no`), `snapshot_json` = VALUES(`snapshot_json`), `change_type` = VALUES(`change_type`), `change_summary` = VALUES(`change_summary`), `versioned_at` = VALUES(`versioned_at`), `entry_id` = VALUES(`entry_id`);"
' "$SNAPSHOT" >> "$TMP_SQL"

cat >> "$TMP_SQL" <<'SQL'

-- entry drafts
SQL
jq -r '
def b64_text(v):
  if v == null then
    "NULL"
  else
    "CAST(FROM_BASE64(\"" + (v|@base64) + "\") AS CHAR CHARACTER SET utf8mb4)"
  end;

.categories[]
| .volumes[]
| .entries[] as $entry
| $entry.entry_drafts[]?
| "INSERT INTO classics_sancai_entry_draft (`draft_id`, `entry_id`, `user_id`, `draft_json`, `autosaved_at`) VALUES (" +
  (.draft_id | tostring) + ", " +
  ($entry.entry_id | tostring) + ", " +
  (.user_id | tostring) + ", " +
  b64_text(.draft_json) + ", " +
  b64_text(.autosaved_at) +
") ON DUPLICATE KEY UPDATE " +
  "`user_id` = VALUES(`user_id`), `draft_json` = VALUES(`draft_json`), `autosaved_at` = VALUES(`autosaved_at`), `entry_id` = VALUES(`entry_id`);"
' "$SNAPSHOT" >> "$TMP_SQL"

cat >> "$TMP_SQL" <<'SQL'

-- entry visual assets
SQL
jq -r '
def b64_text(v):
  if v == null then
    "NULL"
  else
    "CAST(FROM_BASE64(\"" + (v|@base64) + "\") AS CHAR CHARACTER SET utf8mb4)"
  end;

def storage_object_id_expr($object_key; $bucket_name):
  if $object_key == null then
    "NULL"
  else
    "(SELECT id FROM storage_object WHERE object_key = " + b64_text($object_key) + " AND " +
    (if $bucket_name == null then "bucket_name IS NULL" else "bucket_name = " + b64_text($bucket_name) end) + " LIMIT 1)"
  end;

.categories[]
| .volumes[]
| .entries[] as $entry
| $entry.entry_visual_assets[]?
| "INSERT INTO classics_sancai_visual_asset (`asset_id`, `entry_id`, `version_no`, `source_object_id`, `generated_object_id`, `image_analysis`, `fusion_description`, `visual_description`, `text_weight`, `image_weight`, `generation_params`, `current_used`, `status`) VALUES (" +
  (.asset_id | tostring) + ", " +
  ($entry.entry_id | tostring) + ", " +
  ((.version_no // 0) | tostring) + ", " +
  storage_object_id_expr(.source_object_key; .source_bucket_name) + ", " +
  storage_object_id_expr(.generated_object_key; .generated_bucket_name) + ", " +
  b64_text(.image_analysis) + ", " +
  b64_text(.fusion_description) + ", " +
  b64_text(.visual_description) + ", " +
  ((.text_weight // 50) | tostring) + ", " +
  ((.image_weight // 50) | tostring) + ", " +
  b64_text(.generation_params) + ", " +
  (if .current_used == true or .current_used == 1 then "1" else "0" end) + ", " +
  b64_text(.status) +
") ON DUPLICATE KEY UPDATE " +
  "`version_no` = VALUES(`version_no`), `source_object_id` = VALUES(`source_object_id`), `generated_object_id` = VALUES(`generated_object_id`), `image_analysis` = VALUES(`image_analysis`), `fusion_description` = VALUES(`fusion_description`), `visual_description` = VALUES(`visual_description`), `text_weight` = VALUES(`text_weight`), `image_weight` = VALUES(`image_weight`), `generation_params` = VALUES(`generation_params`), `current_used` = VALUES(`current_used`), `status` = VALUES(`status`), `entry_id` = VALUES(`entry_id`);"
' "$SNAPSHOT" >> "$TMP_SQL"

cat >> "$TMP_SQL" <<'SQL'

COMMIT;
SQL

mysql_args=(
    -h "$MYSQL_HOST"
    -P "$MYSQL_PORT"
    -u "$MYSQL_USER"
    -D "$MYSQL_DATABASE"
    --batch
)

if [[ -n "$MYSQL_PASSWORD" ]]; then
    mysql_args+=(-p"$MYSQL_PASSWORD")
fi

if [[ "${DRY_RUN:-0}" -eq 1 ]]; then
    echo "Generated SQL: $TMP_SQL"
    echo "Preview (first 40 lines):"
    sed -n '1,40p' "$TMP_SQL"
    exit 0
fi

"$MYSQL_CLIENT_BIN" "${mysql_args[@]}" < "$TMP_SQL"

echo "restored from: $SNAPSHOT"
