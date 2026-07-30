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

if [[ ! -f "${ROOT_DIR}/db/data/knowledge.sql" ]]; then
    echo "Missing db/data/knowledge.sql" >&2
    exit 1
fi

if [[ ! -f "${ROOT_DIR}/db/data-source/sancai-tags.json" ]]; then
    echo "Missing db/data-source/sancai-tags.json" >&2
    exit 1
fi

if [[ ! -f "${ROOT_DIR}/db/data-source/sancai-manuscripts.json" ]]; then
    echo "Missing db/data-source/sancai-manuscripts.json" >&2
    exit 1
fi

if ! jq -e '.schema == "classics_sancai_tag_seed"' "${ROOT_DIR}/db/data-source/sancai-tags.json" >/dev/null; then
    echo "Invalid sancai tag seed schema" >&2
    exit 1
fi

if ! jq -e 'type == "array"' "${ROOT_DIR}/db/data-source/sancai-manuscripts.json" >/dev/null; then
    echo "Invalid sancai manuscript source schema" >&2
    exit 1
fi

required_tables=(
    classics_sancai_category
    classics_sancai_volume
    classics_sancai_entry
    classics_wangqi_document
    classics_wangqi_document_event
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

if ! grep -q "三才图会条目问答" "${ROOT_DIR}/db/data/classics.sql"; then
    echo "Missing sancai QA generation marker" >&2
    exit 1
fi

if ! grep -q "INSERT INTO \`classics_content_tag\` (\`content_type\`, \`content_id\`, \`tag_id\`" "${ROOT_DIR}/db/data/classics.sql"; then
    echo "Missing sancai content tag ids" >&2
    exit 1
fi

if ! grep -q "三才图会标签库种子" "${ROOT_DIR}/db/data/knowledge.sql"; then
    echo "Missing sancai knowledge tag seed data" >&2
    exit 1
fi

if ! grep -q "三才图会内容标签引用投影" "${ROOT_DIR}/db/data/knowledge.sql"; then
    echo "Missing sancai knowledge tag content refs" >&2
    exit 1
fi

if ! grep -q "INSERT INTO \`classics_wangqi_document\`" "${ROOT_DIR}/db/data/classics.sql"; then
    echo "Missing wangqi document data" >&2
    exit 1
fi

if ! grep -q "Local document IDs are deterministic 1..14" "${ROOT_DIR}/db/data/classics.sql"; then
    echo "Missing deterministic wangqi document import marker" >&2
    exit 1
fi

if ! grep -q "(1, .*'MARKDOWN'.*'PUBLIC'.*'1570-01-01 00:00:00.000')" "${ROOT_DIR}/db/data/classics.sql"; then
    echo "Missing first imported public markdown wangqi document data" >&2
    exit 1
fi

if ! grep -q "(14, .*'MARKDOWN'.*'PUBLIC'.*'1570-09-01 00:00:00.000')" "${ROOT_DIR}/db/data/classics.sql"; then
    echo "Missing last imported public markdown wangqi document data" >&2
    exit 1
fi

if ! grep -q "INSERT INTO \`classics_wangqi_document_event\`" "${ROOT_DIR}/db/data/classics.sql"; then
    echo "Missing wangqi document event data" >&2
    exit 1
fi

if ! grep -q "ALTER TABLE \`classics_wangqi_document\` AUTO_INCREMENT = 15" "${ROOT_DIR}/db/data/classics.sql"; then
    echo "Missing wangqi document auto increment reset" >&2
    exit 1
fi

if ! grep -q "ALTER TABLE \`classics_wangqi_document_event\` AUTO_INCREMENT = 15" "${ROOT_DIR}/db/data/classics.sql"; then
    echo "Missing wangqi document event auto increment reset" >&2
    exit 1
fi

if ! grep -q "\`content_updated_at\`" "${ROOT_DIR}/db/data/classics.sql"; then
    echo "Missing explicit content_updated_at in classics data" >&2
    exit 1
fi

echo "Classics schema and data files are present"
