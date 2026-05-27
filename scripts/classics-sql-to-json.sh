#!/usr/bin/env bash

# Export current Classics Sancai data to a structured JSON snapshot.

set -euo pipefail

MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-}"
MYSQL_DATABASE="${MYSQL_DATABASE:-kuzhambu}"
MYSQL_CLIENT_BIN="${MYSQL_CLIENT_BIN:-mysql}"
OUTPUT="${1:-./sancai_tree_snapshot.json}"

usage() {
    cat <<'USAGE'
Usage:
  scripts/classics-sql-to-json.sh [output_file]

Environment variables:
  MYSQL_HOST           MySQL host (default: 127.0.0.1)
  MYSQL_PORT           MySQL port (default: 3306)
  MYSQL_USER           MySQL user (default: root)
  MYSQL_PASSWORD       MySQL password (default: empty)
  MYSQL_DATABASE       Target database (default: kuzhambu)
  MYSQL_CLIENT_BIN     mysql client binary (default: mysql)
USAGE
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
    usage
    exit 0
fi

if ! command -v "$MYSQL_CLIENT_BIN" >/dev/null 2>&1; then
    echo "error: $MYSQL_CLIENT_BIN not found" >&2
    exit 1
fi

if ! command -v jq >/dev/null 2>&1; then
    echo "error: jq not found" >&2
    exit 1
fi

SQL_QUERY=$(cat <<'SQL'
SELECT
    JSON_PRETTY(
        JSON_OBJECT(
            'schema', 'classics_sancai_tree',
            'snapshot_version', '2.0',
            'exported_at', NOW(3),
            'category_count', (SELECT COUNT(*) FROM classics_sancai_category),
            'volume_count', (SELECT COUNT(*) FROM classics_sancai_volume),
            'entry_count', (SELECT COUNT(*) FROM classics_sancai_entry),
            'categories',
            COALESCE(
                (
                    SELECT JSON_ARRAYAGG(category_payload)
                    FROM (
                        SELECT JSON_OBJECT(
                            'id', cat.id,
                            'title', cat.title,
                            'category_type', cat.category_type,
                            'priority', cat.priority,
                            'volumes',
                            COALESCE(
                                (
                                    SELECT JSON_ARRAYAGG(volume_payload)
                                    FROM (
                                        SELECT JSON_OBJECT(
                                            'id', vol.id,
                                            'category_id', vol.category_id,
                                            'title', vol.title,
                                            'volume_type', vol.volume_type,
                                            'priority', vol.priority,
                                            'entries',
                                            COALESCE(
                                                (
                                                    SELECT JSON_ARRAYAGG(entry_payload)
                                                    FROM (
                                                        SELECT JSON_OBJECT(
                                                            'id', ent.id,
                                                            'volume_id', ent.volume_id,
                                                            'title', ent.title,
                                                            'original_text', ent.original_text,
                                                            'translation_text', ent.translation_text,
                                                            'summary', ent.summary,
                                                            'lifecycle_status', ent.lifecycle_status,
                                                            'visibility', ent.visibility,
                                                            'translation_status', ent.translation_status,
                                                            'image_status', ent.image_status,
                                                            'visual_asset_status', ent.visual_asset_status,
                                                            'refinement_status', ent.refinement_status,
                                                            'priority', ent.priority,
                                                            'tags_snapshot',
                                                            COALESCE(
                                                                (
                                                                    SELECT JSON_ARRAYAGG(ct.tag_name_snapshot)
                                                                    FROM classics_content_tag ct
                                                                    WHERE ct.content_type = 'SANCAI_ENTRY'
                                                                      AND ct.content_id = ent.id
                                                                      AND ct.status = 'ACTIVE'
                                                                    ORDER BY ct.priority
                                                                ),
                                                                JSON_ARRAY()
                                                            )
                                                        ) AS entry_payload
                                                        FROM classics_sancai_entry ent
                                                        WHERE ent.volume_id = vol.id
                                                        ORDER BY ent.priority
                                                    ) ordered_entries
                                                ),
                                                JSON_ARRAY()
                                            )
                                        ) AS volume_payload
                                        FROM classics_sancai_volume vol
                                        WHERE vol.category_id = cat.id
                                        ORDER BY vol.priority
                                    ) ordered_volumes
                                ),
                                JSON_ARRAY()
                            )
                        ) AS category_payload
                        FROM classics_sancai_category cat
                        ORDER BY cat.priority
                    ) ordered_categories
                ),
                JSON_ARRAY()
            )
        )
    ) AS payload
FROM DUAL;
SQL
)

mysql_args=(
    -h "$MYSQL_HOST"
    -P "$MYSQL_PORT"
    -u "$MYSQL_USER"
    -D "$MYSQL_DATABASE"
    --batch
    --raw
    --skip-column-names
)

if [[ -n "$MYSQL_PASSWORD" ]]; then
    mysql_args+=(-p"$MYSQL_PASSWORD")
fi

"$MYSQL_CLIENT_BIN" "${mysql_args[@]}" -N -e "$SQL_QUERY" > "$OUTPUT.raw"

if [[ ! -s "$OUTPUT.raw" ]]; then
    echo "error: export query returned empty result" >&2
    rm -f "$OUTPUT.raw"
    exit 1
fi

jq -c '.' "$OUTPUT.raw" > "$OUTPUT"
rm -f "$OUTPUT.raw"

echo "exported: $OUTPUT"
