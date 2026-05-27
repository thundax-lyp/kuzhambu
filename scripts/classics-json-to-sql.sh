#!/usr/bin/env bash

# Generate Classics seed SQL from a structured Sancai JSON snapshot.
#
# This script adapts the legacy classics_sancai_tree snapshot shape to the
# current Classics schema. It writes SQL only; it does not connect to MySQL.

set -euo pipefail

SNAPSHOT="./sancai_tree_snapshot.json"
OUTPUT="db/data/classics.sql"

usage() {
    cat <<'USAGE'
Usage:
  scripts/classics-json-to-sql.sh [snapshot_file] [output_sql]

Defaults:
  snapshot_file  ./sancai_tree_snapshot.json
  output_sql     db/data/classics.sql

Examples:
  scripts/classics-json-to-sql.sh
  scripts/classics-json-to-sql.sh ./sancai_tree_snapshot.json db/data/classics.sql
USAGE
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
    usage
    exit 0
fi

if [[ $# -gt 0 ]]; then
    SNAPSHOT="$1"
fi

if [[ $# -gt 1 ]]; then
    OUTPUT="$2"
fi

if [[ $# -gt 2 ]]; then
    echo "error: unexpected argument '$3'" >&2
    usage
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

mkdir -p "$(dirname "$OUTPUT")"
TMP_OUTPUT=$(mktemp /tmp/classics-data-XXXXXX.sql)
trap 'rm -f "$TMP_OUTPUT"' EXIT

cat > "$TMP_OUTPUT" <<'SQL'
SET NAMES utf8mb4;

-- Seed data generated from sancai_tree_snapshot.json.
-- The source snapshot uses legacy field names; this file targets the current Classics schema.

SQL

jq -r '
def sql_text(v):
  if v == null then
    "NULL"
  else
    "CAST(FROM_BASE64(\"" + (v | tostring | @base64) + "\") AS CHAR CHARACTER SET utf8mb4)"
  end;

def sql_json(v):
  if v == null then
    "NULL"
  else
    "CAST(" + sql_text(v) + " AS JSON)"
  end;

def category_type(c):
  if ((c.formal // 1) | tostring) == "0" or ((c.formal // 1) == false) then
    "AUXILIARY"
  else
    "FORMAL"
  end;

def volume_type(v):
  if ((v.auxiliary // 0) | tostring) == "1" or ((v.auxiliary // 0) == true) then
    "AUXILIARY"
  else
    "MAIN"
  end;

"-- 三才图会门类",
(
  .categories
  | to_entries[]
  | .key as $category_index
  | .value as $category
  | "INSERT INTO `classics_sancai_category` (`id`, `title`, `category_type`, `priority`) VALUES (" +
    (($category_index + 1) | tostring) + ", " +
    sql_text($category.title // $category.name) + ", " +
    sql_text(category_type($category)) + ", " +
    (($category.priority // $category.sort_order // ($category_index + 1)) | tostring) +
    ") ON DUPLICATE KEY UPDATE " +
    "`title` = VALUES(`title`), `category_type` = VALUES(`category_type`), `priority` = VALUES(`priority`);"
),
"",
"-- 三才图会卷",
(
  .categories
  | to_entries[]
  | .key as $category_index
  | .value as $category
  | ($category.volumes // [])[] as $volume
  | "INSERT INTO `classics_sancai_volume` (`id`, `category_id`, `title`, `volume_type`, `priority`) VALUES (" +
    (($volume.id // $volume.volume_id) | tostring) + ", " +
    (($category_index + 1) | tostring) + ", " +
    sql_text($volume.title) + ", " +
    sql_text(volume_type($volume)) + ", " +
    (($volume.priority // $volume.sort_order) | tostring) +
    ") ON DUPLICATE KEY UPDATE " +
    "`category_id` = VALUES(`category_id`), `title` = VALUES(`title`), `volume_type` = VALUES(`volume_type`), `priority` = VALUES(`priority`);"
),
"",
"-- 三才图会条目",
(
  [
    .categories[]
    | (.volumes // [])[] as $volume
    | ($volume.entries // [])[]
    | {volume_id: ($volume.id // $volume.volume_id), entry: .}
  ]
  | to_entries[]
  | .key as $entry_index
  | .value as $row
  | $row.entry as $entry
  | "INSERT INTO `classics_sancai_entry` (`id`, `volume_id`, `title`, `original_text`, `translation_text`, `summary`, `lifecycle_status`, `visibility`, `translation_status`, `image_status`, `visual_asset_status`, `refinement_status`, `priority`) VALUES (" +
    (($entry.id // $entry.entry_id) | tostring) + ", " +
    ($row.volume_id | tostring) + ", " +
    sql_text($entry.title) + ", " +
    sql_text($entry.original_text) + ", " +
    sql_text($entry.translation_text) + ", " +
    sql_text($entry.summary) + ", " +
    sql_text($entry.lifecycle_status // "PUBLISHED") + ", " +
    sql_text($entry.visibility // "PUBLIC") + ", " +
    sql_text($entry.translation_status // "MISSING") + ", " +
    sql_text($entry.image_status // "MISSING") + ", " +
    sql_text($entry.visual_asset_status // "MISSING") + ", " +
    sql_text($entry.refinement_status // "RAW") + ", " +
    (($entry_index + 1) | tostring) +
    ") ON DUPLICATE KEY UPDATE " +
    "`volume_id` = VALUES(`volume_id`), `title` = VALUES(`title`), `original_text` = VALUES(`original_text`), `translation_text` = VALUES(`translation_text`), `summary` = VALUES(`summary`), `lifecycle_status` = VALUES(`lifecycle_status`), `visibility` = VALUES(`visibility`), `translation_status` = VALUES(`translation_status`), `image_status` = VALUES(`image_status`), `visual_asset_status` = VALUES(`visual_asset_status`), `refinement_status` = VALUES(`refinement_status`), `priority` = VALUES(`priority`);"
),
"",
"-- 三才图会条目标签",
(
  [
    .categories[]
    | (.volumes // [])[]
    | (.entries // [])[] as $entry
    | (($entry.tags_snapshot // null) | if . == null then [] elif type == "string" then (try fromjson catch []) elif type == "array" then . else [] end)[] as $tag
    | {entry_id: ($entry.id // $entry.entry_id), tag: $tag}
  ]
  | to_entries[]
  | .key as $tag_index
  | .value as $row
  | "INSERT INTO `classics_content_tag` (`content_type`, `content_id`, `tag_name_snapshot`, `source`, `status`, `priority`) VALUES (" +
    sql_text("SANCAI_ENTRY") + ", " +
    ($row.entry_id | tostring) + ", " +
    sql_text($row.tag) + ", " +
    sql_text("MANUAL") + ", " +
    sql_text("ACTIVE") + ", " +
    (($tag_index + 1) | tostring) +
    ") ON DUPLICATE KEY UPDATE " +
    "`source` = VALUES(`source`), `status` = VALUES(`status`), `priority` = VALUES(`priority`);"
)
' "$SNAPSHOT" >> "$TMP_OUTPUT"

mv "$TMP_OUTPUT" "$OUTPUT"
trap - EXIT

echo "generated: $OUTPUT"
