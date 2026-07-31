#!/usr/bin/env bash

# Generate Classics seed SQL from structured source data.
#
# It writes SQL only; it does not connect to MySQL.

set -euo pipefail

SOURCE="db/data-source/sancai-manuscripts.json"
OUTPUT="db/data/classics.sql"
TAG_SEED="db/data-source/sancai-tags.json"
WANGQI_SOURCE="db/data-source/wangqi-documents-full.json"
MING_SOURCE="db/data-source/ming-customs.json"

usage() {
    cat <<'USAGE'
Usage:
  scripts/classics-json-to-sql.sh [source_json] [output_sql] [tag_seed_json]

Defaults:
  source_json    db/data-source/sancai-manuscripts.json
  output_sql     db/data/classics.sql
  tag_seed_json  db/data-source/sancai-tags.json

Examples:
  scripts/classics-json-to-sql.sh
  scripts/classics-json-to-sql.sh db/data-source/sancai-manuscripts.json db/data/classics.sql
  scripts/classics-json-to-sql.sh db/data-source/sancai-manuscripts.json db/data/classics.sql db/data-source/sancai-tags.json
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

if [[ ! -f "$WANGQI_SOURCE" || ! -f "$MING_SOURCE" ]]; then
    echo "error: Classics source data is incomplete" >&2
    exit 1
fi

if ! jq -e 'type == "array"' "$SOURCE" >/dev/null; then
    echo "error: invalid source data format, expected manuscript array" >&2
    exit 1
fi

if ! jq -e 'type == "array" and all(.[]; .lifecycleStatus == "DRAFT")' "$SOURCE" >/dev/null; then
    echo "error: invalid Sancai lifecycleStatus" >&2
    exit 1
fi

if ! jq -e 'type == "array" and all(.[]; .lifecycleStatus == "DRAFT" and (.documentTime | type == "number"))' "$WANGQI_SOURCE" >/dev/null; then
    echo "error: invalid Wangqi source data" >&2
    exit 1
fi

if ! jq -e '.schema == "classics_ming_customs_seed" and (.items | type == "array" and length > 0) and all(.items[]; .lifecycleStatus == "DRAFT" and (.sourceRecordId | type == "number") and (.contentUpdatedAt | type == "number"))' "$MING_SOURCE" >/dev/null; then
    echo "error: invalid Ming customs source data" >&2
    exit 1
fi

if ! jq -e '.schema == "classics_sancai_tag_seed"' "$TAG_SEED" >/dev/null; then
    echo "error: invalid tag seed format, expected schema=classics_sancai_tag_seed" >&2
    exit 1
fi

mkdir -p "$(dirname "$OUTPUT")"
TMP_OUTPUT=$(mktemp /tmp/classics-data-XXXXXX)
cleanup() {
    rm -f "$TMP_OUTPUT"
}
trap cleanup EXIT

cat > "$TMP_OUTPUT" <<'SQL'
SET NAMES utf8mb4;

-- Seed data generated from structured JSON sources under db/data-source/.
-- Sancai ids and priorities and Wangqi local ids are derived by this script.

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

def sql_epoch_ms(v):
  if v == null then
    "NULL"
  else
    (v | tostring | gsub("T"; " ")) as $raw
    | if ($raw | test("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}(\\.[0-9]{1,6})?$")) then
        ($raw | capture("^(?<base>\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})(\\.(?<frac>[0-9]{1,6}))?$")) as $parts
        | "TIMESTAMPDIFF(MICROSECOND, \u00271970-01-01 08:00:00.000000\u0027, \u0027" +
          $parts.base + "." + (($parts.frac // "") + "000000")[0:6] +
          "\u0027) DIV 1000"
      else
        error("Invalid Asia/Shanghai display datetime: " + $raw)
      end
  end;

def stable_hash(v):
  reduce ((v | tostring | gsub("^\\s+|\\s+$"; "") | explode)[]) as $code
    (7; ((. * 131 + $code) % 900000000));

def generated_entry_key(entry):
  entry.category + "\u0000" + entry.volume + "\u0000" + entry.title + "\u0000" + entry.content;

def generated_tag_id(tag_seed; tag):
  if ((tag_seed.tags | map(.name) | index(tag)) == null) then
    error("Unknown Sancai manuscript tag: " + (tag | tostring))
  elif tag == "世系图" then
    500001
  else
    501000 + stable_hash(tag)
  end;

def generated_category_id(categories; category_name):
  (categories | map(.category) | index(category_name)) + 1;

def generated_volume_id(volumes; category_name; volume_name):
  first(
    volumes
    | to_entries[]
    | select(.value.category == category_name and .value.volume == volume_name)
    | .value.volume_id
  );

def generated_entry_id(entries; entry_index; entry):
  300000000000
    + stable_hash(generated_entry_key(entry)) * 100
    + ([entries[0:entry_index + 1][] | select(generated_entry_key(.) == generated_entry_key(entry))] | length);

def generated_category_priority(category_index):
  category_index * 10;

def generated_volume_priority(volume_id):
  volume_id;

def volume_number_from_title(title):
  if (title | endswith("图序")) then 0
  elif (title | endswith("卷二十")) then 20
  elif (title | endswith("卷十九")) then 19
  elif (title | endswith("卷十八")) then 18
  elif (title | endswith("卷十七")) then 17
  elif (title | endswith("卷十六")) then 16
  elif (title | endswith("卷十五")) then 15
  elif (title | endswith("卷十四")) then 14
  elif (title | endswith("卷十三")) then 13
  elif (title | endswith("卷十二")) then 12
  elif (title | endswith("卷十一")) then 11
  elif (title | endswith("卷十")) then 10
  elif (title | endswith("卷九")) then 9
  elif (title | endswith("卷八")) then 8
  elif (title | endswith("卷七")) then 7
  elif (title | endswith("卷六")) then 6
  elif (title | endswith("卷五")) then 5
  elif (title | endswith("卷四")) then 4
  elif (title | endswith("卷三")) then 3
  elif (title | endswith("卷二")) then 2
  elif (title | endswith("卷一")) then 1
  else null
  end;

. as $entries
| $tag_seed[0] as $tag_seed_doc
| (
    reduce $entries[] as $entry (
      [];
      if (map(.category) | index($entry.category)) then
        .
      else
        . + [{
          category: $entry.category,
          categoryType: ($entry.categoryType // "FORMAL")
        }]
      end
    )
  ) as $categories
| (
    reduce $entries[] as $entry (
      [];
      if (map(.category + "\u0000" + .volume) | index($entry.category + "\u0000" + $entry.volume)) then
        .
      else
        . + [
          {
            category: $entry.category,
            volume: $entry.volume,
            volumeType: ($entry.volumeType // "MAIN"),
            volume_id:
              (if (generated_category_id($categories; $entry.category) == 1) then
              1
            else
                ((generated_category_id($categories; $entry.category) - 1) * 100
                  + (
                    volume_number_from_title($entry.volume)
                    // ([.[] | select(.category == $entry.category)] | length)
                  ))
            end)
          }
        ]
      end
    )
  ) as $volumes
|
"-- 三才图会门类",
(
  $categories
  | to_entries[]
  | .key as $category_index
  | .value as $category
  | "INSERT INTO `classics_sancai_category` (`id`, `title`, `category_type`, `priority`) VALUES (" +
    (($category_index + 1) | tostring) + ", " +
    sql_text($category.category) + ", " +
    sql_text($category.categoryType) + ", " +
    (generated_category_priority($category_index) | tostring) +
    ") ON DUPLICATE KEY UPDATE " +
    "`title` = VALUES(`title`), `category_type` = VALUES(`category_type`), `priority` = VALUES(`priority`);"
),
"",
"-- 三才图会卷",
(
  $volumes[]
  | . as $volume
  | "INSERT INTO `classics_sancai_volume` (`id`, `category_id`, `title`, `volume_type`, `priority`) VALUES (" +
    ($volume.volume_id | tostring) + ", " +
    (generated_category_id($categories; $volume.category) | tostring) + ", " +
    sql_text($volume.volume) + ", " +
    sql_text($volume.volumeType) + ", " +
    (generated_volume_priority($volume.volume_id) | tostring) +
    ") ON DUPLICATE KEY UPDATE " +
    "`category_id` = VALUES(`category_id`), `title` = VALUES(`title`), `volume_type` = VALUES(`volume_type`), `priority` = VALUES(`priority`);"
),
"",
"-- 三才图会条目",
(
  $entries
  | to_entries[]
  | .key as $entry_index
  | .value as $entry
  | "INSERT INTO `classics_sancai_entry` (`id`, `volume_id`, `title`, `original_text`, `translation_text`, `summary`, `lifecycle_status`, `transition_status`, `current_publication_job_id`, `translation_status`, `image_status`, `visual_asset_status`, `refinement_status`, `priority`, `current_version_id`, `current_version_no`, `current_versioned_at`, `content_updated_at`) VALUES (" +
    (generated_entry_id($entries; $entry_index; $entry) | tostring) + ", " +
    (generated_volume_id($volumes; $entry.category; $entry.volume) | tostring) + ", " +
    sql_text($entry.title) + ", " +
    sql_text($entry.content) + ", " +
    sql_text($entry.translation) + ", " +
    sql_text($entry.summary) + ", " +
    sql_text($entry.lifecycleStatus // "DRAFT") + ", " +
    sql_text("NONE") + ", " +
    "NULL, " +
    sql_text(if (($entry.translation // "") == "") then "MISSING" else "READY" end) + ", " +
    sql_text("MISSING") + ", " +
    sql_text("MISSING") + ", " +
    sql_text("RAW") + ", " +
    (($entry_index + 1) | tostring) + ", " +
    "NULL, " +
    "NULL, " +
    "NULL, " +
    sql_epoch_ms("2026-01-01 00:00:00.000") +
    ") ON DUPLICATE KEY UPDATE " +
    "`volume_id` = VALUES(`volume_id`), `title` = VALUES(`title`), `original_text` = VALUES(`original_text`), `translation_text` = VALUES(`translation_text`), `summary` = VALUES(`summary`), `lifecycle_status` = VALUES(`lifecycle_status`), `transition_status` = VALUES(`transition_status`), `current_publication_job_id` = VALUES(`current_publication_job_id`), `translation_status` = VALUES(`translation_status`), `image_status` = VALUES(`image_status`), `visual_asset_status` = VALUES(`visual_asset_status`), `refinement_status` = VALUES(`refinement_status`), `priority` = VALUES(`priority`), `current_version_id` = VALUES(`current_version_id`), `current_version_no` = VALUES(`current_version_no`), `current_versioned_at` = VALUES(`current_versioned_at`), `content_updated_at` = VALUES(`content_updated_at`);"
),
"",
"-- 三才图会条目问答",
"DELETE FROM `classics_content_qa_pair` WHERE `content_type` = \u0027SANCAI_ENTRY\u0027;",
(
  [
    $entries
    | to_entries[]
    | .key as $entry_index
    | .value as $entry
    | ($entry.qa // [])[] as $qa
    | select(($qa.q // "") != "" and ($qa.a // "") != "")
    | {entry_id: generated_entry_id($entries; $entry_index; $entry), qa: $qa}
  ] as $qa_rows
  | if ($qa_rows | length) > 0 then
      $qa_rows
      | to_entries[]
      | .key as $qa_index
      | .value as $row
      | "INSERT INTO `classics_content_qa_pair` (`content_type`, `content_id`, `question`, `answer`, `source`, `priority`) VALUES (" +
        sql_text("SANCAI_ENTRY") + ", " +
        ($row.entry_id | tostring) + ", " +
        sql_text($row.qa.q) + ", " +
        sql_text($row.qa.a) + ", " +
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
    $entries
    | to_entries[]
    | .key as $entry_index
    | .value as $entry
    | ($entry.tags // [])[] as $tag
    | {entry_id: generated_entry_id($entries; $entry_index; $entry), tag: $tag}
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

cat >> "$TMP_OUTPUT" <<'SQL'

-- 王圻文档
-- Generated from db/data-source/wangqi-documents-full.json. Local document IDs are deterministic 1..14.
DELETE FROM `classics_content_tag` WHERE `content_type` = 'WANGQI_DOCUMENT' AND (`content_id` BETWEEN 1 AND 14 OR `content_id` IN (400000000001, 400000000002));
DELETE FROM `classics_content_qa_pair` WHERE `content_type` = 'WANGQI_DOCUMENT' AND (`content_id` BETWEEN 1 AND 14 OR `content_id` IN (400000000001, 400000000002));
DELETE FROM `classics_content_version` WHERE `content_type` = 'WANGQI_DOCUMENT' AND (`content_id` BETWEEN 1 AND 14 OR `content_id` IN (400000000001, 400000000002));
DELETE FROM `classics_wangqi_document_event` WHERE `document_id` BETWEEN 1 AND 14 OR `document_id` IN (400000000001, 400000000002);
DELETE FROM `classics_wangqi_document` WHERE `id` BETWEEN 1 AND 14 OR `id` IN (400000000001, 400000000002);

SQL

jq -r '
def sql_text(v):
  if v == null then "NULL"
  else "CAST(FROM_BASE64(\"" + (v | tostring | @base64) + "\") AS CHAR CHARACTER SET utf8mb4)"
  end;

sort_by(.id) as $documents
| (
    $documents
    | to_entries[]
    | (.key + 1) as $local_id
    | .value as $document
    | "INSERT INTO `classics_wangqi_document` (`id`, `title`, `summary`, `content_format`, `content`, `document_time`, `storage_object_id`, `lifecycle_status`, `transition_status`, `current_publication_job_id`, `current_version_id`, `current_version_no`, `current_versioned_at`, `content_updated_at`) VALUES (" +
      ($local_id | tostring) + ", " +
      sql_text($document.title) + ", " +
      sql_text($document.summary) + ", " +
      "\u0027MARKDOWN\u0027, " +
      sql_text($document.content) + ", " +
      ($document.documentTime | tostring) + ", NULL, " +
      "\u0027" + $document.lifecycleStatus + "\u0027, \u0027NONE\u0027, NULL, NULL, NULL, NULL, " +
      ($document.documentTime | tostring) + ");"
  ),
  "",
  (
    $documents
    | to_entries[]
    | (.key + 1) as $local_id
    | .value as $document
    | "INSERT INTO `classics_wangqi_document_event` (`id`, `document_id`, `title`, `occurred_at`, `occurred_label`, `summary`, `priority`) VALUES (" +
      ($local_id | tostring) + ", " +
      ($local_id | tostring) + ", " +
      sql_text($document.title) + ", " +
      ($document.documentTime | tostring) + ", " +
      sql_text($document.eventOccurredLabel) + ", " +
      sql_text($document.summary) + ", " +
      ($local_id | tostring) + ");"
  ),
  "",
  (
    [
      $documents
      | to_entries[]
      | (.key + 1) as $local_id
      | .value as $document
      | ($document.tags // [])[]
      | {content_id: $local_id, tag: .}
    ]
    | to_entries[]
    | "INSERT INTO `classics_content_tag` (`content_type`, `content_id`, `tag_name_snapshot`, `source`, `status`, `priority`) VALUES (" +
      sql_text("WANGQI_DOCUMENT") + ", " +
      (.value.content_id | tostring) + ", " +
      sql_text(.value.tag) + ", " +
      sql_text("MANUAL") + ", " +
      sql_text("ACTIVE") + ", " +
      ((.key + 4001) | tostring) + ");"
  ),
  "",
  (
    [
      $documents
      | to_entries[]
      | (.key + 1) as $local_id
      | .value as $document
      | ($document.qa_pairs // [])[]
      | {content_id: $local_id, qa: .}
    ]
    | to_entries[]
    | "INSERT INTO `classics_content_qa_pair` (`content_type`, `content_id`, `question`, `answer`, `source`, `priority`) VALUES (" +
      sql_text("WANGQI_DOCUMENT") + ", " +
      (.value.content_id | tostring) + ", " +
      sql_text(.value.qa.question) + ", " +
      sql_text(.value.qa.answer) + ", " +
      sql_text("MANUAL") + ", " +
      ((.key + 5001) | tostring) + ");"
  ),
  "",
  "ALTER TABLE `classics_wangqi_document` AUTO_INCREMENT = 15;",
  "ALTER TABLE `classics_wangqi_document_event` AUTO_INCREMENT = 15;"
' "$WANGQI_SOURCE" >> "$TMP_OUTPUT"

cat >> "$TMP_OUTPUT" <<'SQL'

-- 明代习俗
DELETE FROM `classics_content_tag` WHERE `content_type` = 'MING_CUSTOMS';
DELETE FROM `classics_content_qa_pair` WHERE `content_type` = 'MING_CUSTOMS';
DELETE FROM `classics_ming_customs_keyword`;
DELETE FROM `classics_ming_customs_entry`;
SQL

jq -r '
def sql_text(v):
  if v == null then "NULL"
  else "CAST(FROM_BASE64(\"" + (v | tostring | @base64) + "\") AS CHAR CHARACTER SET utf8mb4)"
  end;

.items as $entries
| (
    $entries[]
    | "INSERT INTO `classics_ming_customs_entry` (`id`, `title`, `category`, `chapter`, `section`, `summary`, `content_format`, `content`, `original_excerpts`, `lifecycle_status`, `transition_status`, `current_publication_job_id`, `current_version_id`, `current_version_no`, `current_versioned_at`, `content_updated_at`) VALUES (" +
      ((.sourceRecordId + 500000000000) | tostring) + ", " +
      sql_text(.title) + ", " +
      sql_text(.category) + ", " +
      sql_text(.chapter) + ", " +
      sql_text(.section) + ", " +
      sql_text(.summary) + ", " +
      sql_text(.contentFormat) + ", " +
      sql_text(.content) + ", " +
      sql_text(.originalExcerpts) + ", " +
      sql_text(.lifecycleStatus) + ", " +
      sql_text("NONE") + ", NULL, NULL, NULL, NULL, " +
      (.contentUpdatedAt | tostring) +
      ") ON DUPLICATE KEY UPDATE `title` = VALUES(`title`), `category` = VALUES(`category`), `chapter` = VALUES(`chapter`), `section` = VALUES(`section`), `summary` = VALUES(`summary`), `content_format` = VALUES(`content_format`), `content` = VALUES(`content`), `original_excerpts` = VALUES(`original_excerpts`), `lifecycle_status` = VALUES(`lifecycle_status`), `transition_status` = VALUES(`transition_status`), `current_publication_job_id` = VALUES(`current_publication_job_id`), `current_version_id` = VALUES(`current_version_id`), `current_version_no` = VALUES(`current_version_no`), `current_versioned_at` = VALUES(`current_versioned_at`), `content_updated_at` = VALUES(`content_updated_at`);"
  ),
  "",
  (
    [
      $entries[]
      | (.sourceRecordId + 500000000000) as $content_id
      | (.keywords // [])[]
      | {content_id: $content_id, keyword: .}
    ]
    | to_entries[]
    | "INSERT INTO `classics_ming_customs_keyword` (`id`, `custom_id`, `keyword`, `priority`) VALUES (" +
      ((.key + 510000000001) | tostring) + ", " +
      (.value.content_id | tostring) + ", " +
      sql_text(.value.keyword) + ", " +
      ((.key + 900001) | tostring) +
      ") ON DUPLICATE KEY UPDATE `custom_id` = VALUES(`custom_id`), `keyword` = VALUES(`keyword`), `priority` = VALUES(`priority`);"
  ),
  "",
  (
    [
      $entries[]
      | (.sourceRecordId + 500000000000) as $content_id
      | (.tags // [])[]
      | {content_id: $content_id, tag: .}
    ]
    | to_entries[]
    | "INSERT INTO `classics_content_tag` (`content_type`, `content_id`, `tag_name_snapshot`, `source`, `status`, `priority`) VALUES (" +
      sql_text("MING_CUSTOMS") + ", " +
      (.value.content_id | tostring) + ", " +
      sql_text(.value.tag) + ", " +
      sql_text("MANUAL") + ", " +
      sql_text("ACTIVE") + ", " +
      ((.key + 6001) | tostring) + ");"
  ),
  "",
  (
    [
      $entries[]
      | (.sourceRecordId + 500000000000) as $content_id
      | (.qa // [])[]
      | {content_id: $content_id, qa: .}
    ]
    | to_entries[]
    | "INSERT INTO `classics_content_qa_pair` (`content_type`, `content_id`, `question`, `answer`, `source`, `priority`) VALUES (" +
      sql_text("MING_CUSTOMS") + ", " +
      (.value.content_id | tostring) + ", " +
      sql_text(.value.qa.question) + ", " +
      sql_text(.value.qa.answer) + ", " +
      sql_text("MANUAL") + ", " +
      ((.key + 7001) | tostring) + ");"
  )
' "$MING_SOURCE" >> "$TMP_OUTPUT"

cp "$TMP_OUTPUT" "$OUTPUT"
cleanup
trap - EXIT

echo "generated: $OUTPUT"
