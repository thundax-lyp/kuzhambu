#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

if [[ ! -f "${ROOT_DIR}/db/schema/classics.sql" ]]; then
    echo "Missing db/schema/classics.sql" >&2
    exit 1
fi

if [[ ! -f "${ROOT_DIR}/db/data/classics.sql" ]]; then
    echo "Missing db/data/classics.sql" >&2
    exit 1
fi

required_tables=(
    classics_sancai_category
    classics_sancai_volume
    classics_sancai_entry
    classics_wangqi_document
    classics_ming_customs_entry
    classics_share_link
)

for table in "${required_tables[@]}"; do
    if ! grep -q "CREATE TABLE IF NOT EXISTS \`${table}\`" "${ROOT_DIR}/db/schema/classics.sql"; then
        echo "Missing table in schema: ${table}" >&2
        exit 1
    fi
done

if ! grep -q "INSERT INTO \`classics_sancai_category\`" "${ROOT_DIR}/db/data/classics.sql"; then
    echo "Missing sancai category data" >&2
    exit 1
fi

if ! grep -q "INSERT INTO \`classics_sancai_entry\`" "${ROOT_DIR}/db/data/classics.sql"; then
    echo "Missing sancai entry data" >&2
    exit 1
fi

echo "Classics schema and data files are present"
