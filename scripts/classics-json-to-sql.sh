#!/usr/bin/env bash

# Generate Classics seed SQL from structured Sancai source data.
#
# This script adapts the classics_sancai_tree source shape to the
# current Classics schema. It writes SQL only; it does not connect to MySQL.

set -euo pipefail

SOURCE="db/data-source/sancai-manuscripts.json"
OUTPUT="db/data/classics.sql"
TAG_SEED="db/data-source/sancai-tags.json"

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

if ! jq -e 'type == "array"' "$SOURCE" >/dev/null; then
    echo "error: invalid source data format, expected manuscript array" >&2
    exit 1
fi

if ! jq -e '.schema == "classics_sancai_tag_seed"' "$TAG_SEED" >/dev/null; then
    echo "error: invalid tag seed format, expected schema=classics_sancai_tag_seed" >&2
    exit 1
fi

mkdir -p "$(dirname "$OUTPUT")"
TMP_OUTPUT=$(mktemp /tmp/classics-data-XXXXXX)
PRESERVED_TAIL=$(mktemp /tmp/classics-data-tail-XXXXXX)
EPOCH_NORMALIZER_DIR=$(mktemp -d /tmp/classics-epoch-normalizer-XXXXXX)
EPOCH_NORMALIZER="$EPOCH_NORMALIZER_DIR/ClassicsEpochNormalizer.java"
cleanup() {
    rm -f "$TMP_OUTPUT" "$PRESERVED_TAIL"
    rm -rf "$EPOCH_NORMALIZER_DIR"
}
trap cleanup EXIT

if [[ -f "$OUTPUT" ]]; then
    awk '/^-- Generated from db\/data-source\/wangqi-documents-full\.json/ {found=1} found {print}' "$OUTPUT" > "$PRESERVED_TAIL"
fi

cat > "$TMP_OUTPUT" <<'SQL'
SET NAMES utf8mb4;

-- Seed data generated from db/data-source/sancai-manuscripts.json.
-- Database ids and priorities are derived by generation script, not stored in source data.

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
  | "INSERT INTO `classics_sancai_entry` (`id`, `volume_id`, `title`, `original_text`, `translation_text`, `summary`, `lifecycle_status`, `visibility`, `translation_status`, `image_status`, `visual_asset_status`, `refinement_status`, `priority`, `current_version_id`, `current_version_no`, `current_versioned_at`, `content_updated_at`) VALUES (" +
    (generated_entry_id($entries; $entry_index; $entry) | tostring) + ", " +
    (generated_volume_id($volumes; $entry.category; $entry.volume) | tostring) + ", " +
    sql_text($entry.title) + ", " +
    sql_text($entry.content) + ", " +
    sql_text($entry.translation) + ", " +
    sql_text($entry.summary) + ", " +
    sql_text($entry.status // "PUBLISHED") + ", " +
    sql_text("PUBLIC") + ", " +
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
    "`volume_id` = VALUES(`volume_id`), `title` = VALUES(`title`), `original_text` = VALUES(`original_text`), `translation_text` = VALUES(`translation_text`), `summary` = VALUES(`summary`), `lifecycle_status` = VALUES(`lifecycle_status`), `visibility` = VALUES(`visibility`), `translation_status` = VALUES(`translation_status`), `image_status` = VALUES(`image_status`), `visual_asset_status` = VALUES(`visual_asset_status`), `refinement_status` = VALUES(`refinement_status`), `priority` = VALUES(`priority`), `current_version_id` = VALUES(`current_version_id`), `current_version_no` = VALUES(`current_version_no`), `current_versioned_at` = VALUES(`current_versioned_at`), `content_updated_at` = VALUES(`content_updated_at`);"
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

if [[ -s "$PRESERVED_TAIL" ]]; then
    printf '\n' >> "$TMP_OUTPUT"
    cat "$PRESERVED_TAIL" >> "$TMP_OUTPUT"
fi

cat > "$EPOCH_NORMALIZER" <<'JAVA'
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ClassicsEpochNormalizer {

    private static final Pattern EPOCH_EXPRESSION = Pattern.compile(
            "TIMESTAMPDIFF\\(MICROSECOND, '1970-01-01 08:00:00\\.000000', "
                    + "'(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}(?:\\.\\d{1,6})?)'\\) DIV 1000");
    private static final ZoneId DISPLAY_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    public static void main(String[] args) throws IOException {
        Path path = Path.of(args[0]);
        String sql = Files.readString(path);
        Matcher matcher = EPOCH_EXPRESSION.matcher(sql);
        StringBuffer normalized = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(normalized, Long.toString(toEpochMillis(matcher.group(1))));
        }
        matcher.appendTail(normalized);
        Files.writeString(path, normalized.toString());
    }

    private static long toEpochMillis(String displayTime) {
        String text = displayTime.replace('T', ' ');
        if (!text.contains(".")) {
            text = text + ".000000";
        } else {
            int fractionLength = text.length() - text.indexOf('.') - 1;
            text = text + "000000".substring(fractionLength);
        }
        return LocalDateTime.parse(text, DISPLAY_FORMAT)
                .atZone(DISPLAY_ZONE)
                .toInstant()
                .toEpochMilli();
    }
}
JAVA

java "$EPOCH_NORMALIZER" "$TMP_OUTPUT"

cp "$TMP_OUTPUT" "$OUTPUT"
cleanup
trap - EXIT

echo "generated: $OUTPUT"
