#!/usr/bin/env bash

# Generate Classics seed SQL from structured Sancai source data.
#
# This script adapts the classics_sancai_tree source shape to the
# current Classics schema. It writes SQL only; it does not connect to MySQL.

set -euo pipefail

SOURCE="db/data-source/sancai-tree.json"
OUTPUT="db/data/classics.sql"
TAG_SEED="db/data-source/sancai-tags.json"

usage() {
    cat <<'USAGE'
Usage:
  scripts/classics-json-to-sql.sh [source_json] [output_sql] [tag_seed_json]

Defaults:
  source_json    db/data-source/sancai-tree.json
  output_sql     db/data/classics.sql
  tag_seed_json  db/data-source/sancai-tags.json

Examples:
  scripts/classics-json-to-sql.sh
  scripts/classics-json-to-sql.sh db/data-source/sancai-tree.json db/data/classics.sql
  scripts/classics-json-to-sql.sh db/data-source/sancai-tree.json db/data/classics.sql db/data-source/sancai-tags.json
USAGE
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
    usage
    exit 0
fi

if [[ $# -gt 0 ]]; then
    SOURCE="$1"
fi

if [[ $# -gt 1 ]]; then
    OUTPUT="$2"
fi

if [[ $# -gt 2 ]]; then
    TAG_SEED="$3"
fi

if [[ $# -gt 3 ]]; then
    echo "error: unexpected argument '$4'" >&2
    usage
    exit 1
fi

if ! command -v jq >/dev/null 2>&1; then
    echo "error: jq not found" >&2
    exit 1
fi

if [[ ! -f "$SOURCE" ]]; then
    echo "error: source data not found: $SOURCE" >&2
    exit 1
fi

if [[ ! -f "$TAG_SEED" ]]; then
    echo "error: tag seed not found: $TAG_SEED" >&2
    exit 1
fi

if ! jq -e '.schema == "classics_sancai_tree"' "$SOURCE" >/dev/null; then
    echo "error: invalid source data format, expected schema=classics_sancai_tree" >&2
    exit 1
fi

if ! jq -e '.schema == "classics_sancai_tag_seed"' "$TAG_SEED" >/dev/null; then
    echo "error: invalid tag seed format, expected schema=classics_sancai_tag_seed" >&2
    exit 1
fi

mkdir -p "$(dirname "$OUTPUT")"
TMP_OUTPUT=$(mktemp /tmp/classics-data-XXXXXX)
PRESERVED_TAIL=$(mktemp /tmp/classics-data-tail-XXXXXX)
trap 'rm -f "$TMP_OUTPUT" "$PRESERVED_TAIL"' EXIT

if [[ -f "$OUTPUT" ]]; then
    awk '/^-- Generated from db\/data-source\/wangqi-documents-full\.json/ {found=1} found {print}' "$OUTPUT" > "$PRESERVED_TAIL"
fi

cat > "$TMP_OUTPUT" <<'SQL'
SET NAMES utf8mb4;

-- Seed data generated from db/data-source/sancai-tree.json.
-- The source data uses Sancai tree field names; this file targets the current Classics schema.

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

def sql_datetime(v):
  if v == null then
    "NULL"
  else
    "\u0027" + (v | tostring) + "\u0027"
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

def generated_tag_id(tag_seed; tag):
  first(tag_seed.tags[] | select(.name == tag) | .tag_id);

. as $root
| $tag_seed[0] as $tag_seed_doc
|
"-- 三才图会门类",
(
  $root.categories
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
  $root.categories
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
    $root.categories[]
    | (.volumes // [])[] as $volume
    | ($volume.entries // [])[]
    | {volume_id: ($volume.id // $volume.volume_id), entry: .}
  ]
  | to_entries[]
  | .key as $entry_index
  | .value as $row
  | $row.entry as $entry
  | "INSERT INTO `classics_sancai_entry` (`id`, `volume_id`, `title`, `original_text`, `translation_text`, `summary`, `lifecycle_status`, `visibility`, `translation_status`, `image_status`, `visual_asset_status`, `refinement_status`, `priority`, `current_version_id`, `current_version_no`, `current_versioned_at`, `content_updated_at`) VALUES (" +
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
    (($entry_index + 1) | tostring) + ", " +
    (($entry.current_version_id // "NULL") | tostring) + ", " +
    (if (($entry.current_version // 0) | tonumber) > 0 then (($entry.current_version // $entry.current_version_no) | tostring) else "NULL" end) + ", " +
    sql_datetime($entry.current_versioned_at) + ", " +
    sql_datetime($entry.content_updated_at // "2026-01-01 00:00:00.000") +
    ") ON DUPLICATE KEY UPDATE " +
    "`volume_id` = VALUES(`volume_id`), `title` = VALUES(`title`), `original_text` = VALUES(`original_text`), `translation_text` = VALUES(`translation_text`), `summary` = VALUES(`summary`), `lifecycle_status` = VALUES(`lifecycle_status`), `visibility` = VALUES(`visibility`), `translation_status` = VALUES(`translation_status`), `image_status` = VALUES(`image_status`), `visual_asset_status` = VALUES(`visual_asset_status`), `refinement_status` = VALUES(`refinement_status`), `priority` = VALUES(`priority`), `current_version_id` = VALUES(`current_version_id`), `current_version_no` = VALUES(`current_version_no`), `current_versioned_at` = VALUES(`current_versioned_at`), `content_updated_at` = VALUES(`content_updated_at`);"
),
"",
"-- 三才图会条目问答",
"DELETE FROM `classics_content_qa_pair` WHERE `content_type` = \u0027SANCAI_ENTRY\u0027;",
(
  [
    $root.categories[]
    | (.volumes // [])[] as $volume
    | ($volume.entries // [])[] as $entry
    | ($entry.entry_qas // [])[] as $qa
    | select(($qa.question // "") != "" and ($qa.answer // "") != "")
    | {entry_id: ($entry.id // $entry.entry_id), qa: $qa}
  ] as $qa_rows
  | if ($qa_rows | length) > 0 then
      $qa_rows
      | to_entries[]
      | .key as $qa_index
      | .value as $row
      | "INSERT INTO `classics_content_qa_pair` (`content_type`, `content_id`, `question`, `answer`, `source`, `priority`) VALUES (" +
        sql_text("SANCAI_ENTRY") + ", " +
        ($row.entry_id | tostring) + ", " +
        sql_text($row.qa.question) + ", " +
        sql_text($row.qa.answer) + ", " +
        sql_text($row.qa.source // "MANUAL") + ", " +
        (($qa_index + 3001) | tostring) +
        ") ON DUPLICATE KEY UPDATE " +
        "`question` = VALUES(`question`), `answer` = VALUES(`answer`), `source` = VALUES(`source`), `priority` = VALUES(`priority`);"
    else
      empty
    end
),
"",
"-- 三才图会条目标签",
(
  [
    $tag_seed_doc.entries[]
    | . as $entry
    | ($entry.tags // [])[] as $tag
    | {entry_id: $entry.content_id, tag: $tag}
  ]
  | to_entries[]
  | .key as $tag_index
  | .value as $row
  | "INSERT INTO `classics_content_tag` (`content_type`, `content_id`, `tag_id`, `tag_name_snapshot`, `source`, `status`, `priority`) VALUES (" +
    sql_text("SANCAI_ENTRY") + ", " +
    ($row.entry_id | tostring) + ", " +
    (generated_tag_id($tag_seed_doc; $row.tag) | tostring) + ", " +
    sql_text($row.tag) + ", " +
    sql_text("MANUAL") + ", " +
    sql_text("ACTIVE") + ", " +
    (($tag_index + 1) | tostring) +
    ") ON DUPLICATE KEY UPDATE " +
    "`tag_id` = VALUES(`tag_id`), `source` = VALUES(`source`), `status` = VALUES(`status`), `priority` = VALUES(`priority`);"
)
' --slurpfile tag_seed "$TAG_SEED" "$SOURCE" >> "$TMP_OUTPUT"

if [[ -s "$PRESERVED_TAIL" ]]; then
    printf '\n' >> "$TMP_OUTPUT"
    cat "$PRESERVED_TAIL" >> "$TMP_OUTPUT"
fi

cp "$TMP_OUTPUT" "$OUTPUT"
trap - EXIT

echo "generated: $OUTPUT"
