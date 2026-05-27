#!/usr/bin/env bash

# Export Sancai content from SQL to a structured JSON snapshot.

set -euo pipefail

SCRIPT_NAME=$(basename "$0")
MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-}"
MYSQL_DATABASE="${MYSQL_DATABASE:-kuzhambu}"
MYSQL_CLIENT_BIN="${MYSQL_CLIENT_BIN:-mysql}"

OUTPUT="${1:-./sancai_tree_snapshot.json}"

usage() {
    cat <<'EOF'
Usage:
  classics-sql-to-json.sh [output_file]

Environment variables:
  MYSQL_HOST       MySQL host (default: 127.0.0.1)
  MYSQL_PORT       MySQL port (default: 3306)
  MYSQL_USER       MySQL user (default: root)
  MYSQL_PASSWORD   MySQL password (default: empty)
  MYSQL_DATABASE   Target database (default: kuzhambu)
  MYSQL_CLIENT_BIN mysql client binary (default: mysql)
EOF
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
            'snapshot_version', '1.0',
            'exported_at', NOW(3),
            'category_count', (SELECT COUNT(*) FROM classics_sancai_category),
            'volume_count', (SELECT COUNT(*) FROM classics_sancai_volume),
            'entry_count', (SELECT COUNT(*) FROM classics_sancai_entry),
            'categories',
            COALESCE(
                (
                    SELECT JSON_ARRAYAGG(
                        JSON_OBJECT(
                            'category_code', cat.category_code,
                            'name', cat.name,
                            'formal', cat.formal,
                            'sort_order', cat.sort_order,
                            'description', cat.description,
                            'volumes',
                            COALESCE(
                                (
                                    SELECT JSON_ARRAYAGG(
                                        JSON_OBJECT(
                                            'volume_id', vol.volume_id,
                                            'volume_no', vol.volume_no,
                                            'title', vol.title,
                                            'auxiliary', vol.auxiliary,
                                            'sort_order', vol.sort_order,
                                            'entry_count', vol.entry_count,
                                            'entries',
                                            COALESCE(
                                                (
                                                    SELECT JSON_ARRAYAGG(
                                                        JSON_OBJECT(
                                                            'entry_id', ent.entry_id,
                                                            'entry_no', ent.entry_no,
                                                            'title', ent.title,
                                                            'original_text', ent.original_text,
                                                            'translation_text', ent.translation_text,
                                                            'summary', ent.summary,
                                                            'tags_snapshot', ent.tags_snapshot,
                                                            'lifecycle_status', ent.lifecycle_status,
                                                            'visibility', ent.visibility,
                                                            'owner_user_id', ent.owner_user_id,
                                                            'translation_status', ent.translation_status,
                                                            'image_status', ent.image_status,
                                                            'visual_asset_status', ent.visual_asset_status,
                                                            'refinement_status', ent.refinement_status,
                                                            'current_version', ent.current_version,
                                                            'entry_images',
                                                            COALESCE(
                                                                (
                                                                    SELECT JSON_ARRAYAGG(
                                                                        JSON_OBJECT(
                                                                            'object_key', so.object_key,
                                                                            'bucket_name', so.bucket_name,
                                                                            'image_role', ei.image_role,
                                                                            'current_used', ei.current_used,
                                                                            'sort_order', ei.sort_order,
                                                                            'caption', ei.caption
                                                                        )
                                                                    )
                                                                    FROM classics_sancai_entry_image ei
                                                                    LEFT JOIN storage_object so ON so.id = ei.object_id
                                                                    WHERE ei.entry_id = ent.entry_id
                                                                ),
                                                                JSON_ARRAY()
                                                            ),
                                                            'entry_qas',
                                                            COALESCE(
                                                                (
                                                                    SELECT JSON_ARRAYAGG(
                                                                        JSON_OBJECT(
                                                                            'discovery_qa_id', eq.discovery_qa_id,
                                                                            'question', eq.question,
                                                                            'answer', eq.answer,
                                                                            'source', eq.source,
                                                                            'sort_order', eq.sort_order
                                                                        )
                                                                    )
                                                                    FROM classics_sancai_entry_qa eq
                                                                    WHERE eq.entry_id = ent.entry_id
                                                                ),
                                                                JSON_ARRAY()
                                                            ),
                                                            'entry_versions',
                                                            COALESCE(
                                                                (
                                                                    SELECT JSON_ARRAYAGG(
                                                                        JSON_OBJECT(
                                                                            'version_id', ev.version_id,
                                                                            'version_no', ev.version_no,
                                                                            'snapshot_json', ev.snapshot_json,
                                                                            'change_type', ev.change_type,
                                                                            'change_summary', ev.change_summary,
                                                                            'versioned_at', ev.versioned_at
                                                                        )
                                                                    )
                                                                    FROM classics_sancai_entry_version ev
                                                                    WHERE ev.entry_id = ent.entry_id
                                                                ),
                                                                JSON_ARRAY()
                                                            ),
                                                            'entry_drafts',
                                                            COALESCE(
                                                                (
                                                                    SELECT JSON_ARRAYAGG(
                                                                        JSON_OBJECT(
                                                                            'draft_id', ed.draft_id,
                                                                            'user_id', ed.user_id,
                                                                            'draft_json', ed.draft_json,
                                                                            'autosaved_at', ed.autosaved_at
                                                                        )
                                                                    )
                                                                    FROM classics_sancai_entry_draft ed
                                                                    WHERE ed.entry_id = ent.entry_id
                                                                ),
                                                                JSON_ARRAY()
                                                            ),
                                                            'entry_visual_assets',
                                                            COALESCE(
                                                                (
                                                                    SELECT JSON_ARRAYAGG(
                                                                        JSON_OBJECT(
                                                                            'asset_id', va.asset_id,
                                                                            'version_no', va.version_no,
                                                                            'source_object_key', so_src.object_key,
                                                                            'source_bucket_name', so_src.bucket_name,
                                                                            'generated_object_key', so_gen.object_key,
                                                                            'generated_bucket_name', so_gen.bucket_name,
                                                                            'image_analysis', va.image_analysis,
                                                                            'fusion_description', va.fusion_description,
                                                                            'visual_description', va.visual_description,
                                                                            'text_weight', va.text_weight,
                                                                            'image_weight', va.image_weight,
                                                                            'generation_params', va.generation_params,
                                                                            'current_used', va.current_used,
                                                                            'status', va.status
                                                                        )
                                                                    )
                                                                    FROM classics_sancai_visual_asset va
                                                                    LEFT JOIN storage_object so_src ON so_src.id = va.source_object_id
                                                                    LEFT JOIN storage_object so_gen ON so_gen.id = va.generated_object_id
                                                                    WHERE va.entry_id = ent.entry_id
                                                                ),
                                                                JSON_ARRAY()
                                                            )
                                                        )
                                                    )
                                                    FROM classics_sancai_entry ent
                                                    WHERE ent.volume_id = vol.volume_id
                                                ),
                                                JSON_ARRAY()
                                            )
                                        )
                                    )
                                    FROM classics_sancai_volume vol
                                    WHERE vol.category_code = cat.category_code
                                ),
                                JSON_ARRAY()
                            )
                        )
                    )
                    FROM classics_sancai_category cat
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
