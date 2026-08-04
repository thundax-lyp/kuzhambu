#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SEED_SQL_DIR="${ROOT_DIR}/build/seed-sql"
CLASSICS_SQL="${SEED_SQL_DIR}/classics.sql"
KNOWLEDGE_SQL="${SEED_SQL_DIR}/knowledge.sql"

if [[ ! -f "${ROOT_DIR}/db/schema/classics.sql" ]]; then
    echo "Missing db/schema/classics.sql" >&2
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

if [[ ! -f "${ROOT_DIR}/db/data-source/wangqi-documents-full.json" ]]; then
    echo "Missing db/data-source/wangqi-documents-full.json" >&2
    exit 1
fi

if [[ ! -f "${ROOT_DIR}/db/data-source/ming-customs.json" ]]; then
    echo "Missing db/data-source/ming-customs.json" >&2
    exit 1
fi

node "${ROOT_DIR}/scripts/seed/generate-classics-sql.mjs"
node "${ROOT_DIR}/scripts/seed/generate-sancai-knowledge-sql.mjs"

if [[ ! -f "${CLASSICS_SQL}" ]]; then
    echo "Missing build/seed-sql/classics.sql" >&2
    exit 1
fi

if [[ ! -f "${KNOWLEDGE_SQL}" ]]; then
    echo "Missing build/seed-sql/knowledge.sql" >&2
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

if ! jq -e 'all(.[]; has("lifecycleStatus") and (has("status") | not))' "${ROOT_DIR}/db/data-source/sancai-manuscripts.json" >/dev/null; then
    echo "Invalid sancai lifecycle source fields" >&2
    exit 1
fi

if ! jq -e 'all(.[]; .lifecycleStatus == "DRAFT")' "${ROOT_DIR}/db/data-source/sancai-manuscripts.json" >/dev/null; then
    echo "Sancai seed manuscripts must start as DRAFT" >&2
    exit 1
fi

if ! jq -e 'type == "array" and length == 14 and all(.[]; .lifecycleStatus == "DRAFT" and has("documentTime") and has("eventOccurredLabel"))' "${ROOT_DIR}/db/data-source/wangqi-documents-full.json" >/dev/null; then
    echo "Invalid wangqi document source schema" >&2
    exit 1
fi

if ! jq -e '.schema == "classics_ming_customs_seed" and (.items | length > 0) and all(.items[]; .lifecycleStatus == "DRAFT" and has("sourceRecordId") and has("contentUpdatedAt"))' "${ROOT_DIR}/db/data-source/ming-customs.json" >/dev/null; then
    echo "Invalid ming customs source schema" >&2
    exit 1
fi

required_tables=(
    classics_sancai_category
    classics_sancai_volume
    classics_sancai_entry
    classics_wangqi_document
    classics_wangqi_document_event
    classics_ming_customs_entry
    classics_publication_job
)

for table in "${required_tables[@]}"; do
    if ! grep -q "CREATE TABLE IF NOT EXISTS \`${table}\`" "${ROOT_DIR}/db/schema/classics.sql"; then
        echo "Missing table in schema: ${table}" >&2
        exit 1
    fi
done

if ! grep -q "INSERT INTO \`classics_sancai_category\`" "${CLASSICS_SQL}"; then
    echo "Missing sancai category data" >&2
    exit 1
fi

if ! grep -q "INSERT INTO \`classics_sancai_entry\`" "${CLASSICS_SQL}"; then
    echo "Missing sancai entry data" >&2
    exit 1
fi

if ! grep -q "三才图会条目问答" "${CLASSICS_SQL}"; then
    echo "Missing sancai QA generation marker" >&2
    exit 1
fi

if ! grep -q "INSERT INTO \`classics_content_tag\` (\`content_type\`, \`content_id\`, \`tag_id\`" "${CLASSICS_SQL}"; then
    echo "Missing sancai content tag ids" >&2
    exit 1
fi

if ! grep -q "三才图会标签库种子" "${KNOWLEDGE_SQL}"; then
    echo "Missing sancai knowledge tag seed data" >&2
    exit 1
fi

if ! grep -q "三才图会内容标签引用投影" "${KNOWLEDGE_SQL}"; then
    echo "Missing sancai knowledge tag content refs" >&2
    exit 1
fi

if ! grep -q "INSERT INTO \`classics_wangqi_document\`" "${CLASSICS_SQL}"; then
    echo "Missing wangqi document data" >&2
    exit 1
fi

if ! grep -q "Local document IDs are deterministic 1..14" "${CLASSICS_SQL}"; then
    echo "Missing deterministic wangqi document import marker" >&2
    exit 1
fi

if ! grep -q "(1, .*'MARKDOWN'.*'DRAFT'.*'NONE'.*-12622809943000)" "${CLASSICS_SQL}"; then
    echo "Missing first imported draft markdown wangqi document data" >&2
    exit 1
fi

if ! grep -q "(14, .*'MARKDOWN'.*'DRAFT'.*'NONE'.*-12601814743000)" "${CLASSICS_SQL}"; then
    echo "Missing last imported draft markdown wangqi document data" >&2
    exit 1
fi

if ! grep -q "INSERT INTO \`classics_wangqi_document_event\`" "${CLASSICS_SQL}"; then
    echo "Missing wangqi document event data" >&2
    exit 1
fi

expected_wangqi_tags=$(jq '[.[].tags // [] | length] | add' "${ROOT_DIR}/db/data-source/wangqi-documents-full.json")
actual_wangqi_tags=$(grep -c 'INSERT INTO `classics_content_tag`.*V0FOR1FJX0RPQ1VNRU5U' "${CLASSICS_SQL}")
if [[ "$actual_wangqi_tags" -ne "$expected_wangqi_tags" ]]; then
    echo "Wangqi tag count drift: expected ${expected_wangqi_tags}, got ${actual_wangqi_tags}" >&2
    exit 1
fi

expected_wangqi_qa=$(jq '[.[].qa_pairs // [] | length] | add' "${ROOT_DIR}/db/data-source/wangqi-documents-full.json")
actual_wangqi_qa=$(grep -c 'INSERT INTO `classics_content_qa_pair`.*V0FOR1FJX0RPQ1VNRU5U' "${CLASSICS_SQL}")
if [[ "$actual_wangqi_qa" -ne "$expected_wangqi_qa" ]]; then
    echo "Wangqi QA count drift: expected ${expected_wangqi_qa}, got ${actual_wangqi_qa}" >&2
    exit 1
fi

expected_ming_entries=$(jq '.items | length' "${ROOT_DIR}/db/data-source/ming-customs.json")
actual_ming_entries=$(grep -c 'INSERT INTO `classics_ming_customs_entry`' "${CLASSICS_SQL}")
if [[ "$actual_ming_entries" -ne "$expected_ming_entries" ]]; then
    echo "Ming customs entry count drift: expected ${expected_ming_entries}, got ${actual_ming_entries}" >&2
    exit 1
fi

expected_ming_tags=$(jq '[.items[].tags // [] | length] | add' "${ROOT_DIR}/db/data-source/ming-customs.json")
actual_ming_tags=$(grep -c 'INSERT INTO `classics_content_tag`.*TUlOR19DVVNUT01T' "${CLASSICS_SQL}")
if [[ "$actual_ming_tags" -ne "$expected_ming_tags" ]]; then
    echo "Ming customs tag count drift: expected ${expected_ming_tags}, got ${actual_ming_tags}" >&2
    exit 1
fi

expected_ming_qa=$(jq '[.items[].qa // [] | length] | add' "${ROOT_DIR}/db/data-source/ming-customs.json")
actual_ming_qa=$(grep -c 'INSERT INTO `classics_content_qa_pair`.*TUlOR19DVVNUT01T' "${CLASSICS_SQL}")
if [[ "$actual_ming_qa" -ne "$expected_ming_qa" ]]; then
    echo "Ming customs QA count drift: expected ${expected_ming_qa}, got ${actual_ming_qa}" >&2
    exit 1
fi

if ! grep -q "ALTER TABLE \`classics_wangqi_document\` AUTO_INCREMENT = 15" "${CLASSICS_SQL}"; then
    echo "Missing wangqi document auto increment reset" >&2
    exit 1
fi

if ! grep -q "ALTER TABLE \`classics_wangqi_document_event\` AUTO_INCREMENT = 15" "${CLASSICS_SQL}"; then
    echo "Missing wangqi document event auto increment reset" >&2
    exit 1
fi

if ! grep -q "\`content_updated_at\`" "${CLASSICS_SQL}"; then
    echo "Missing explicit content_updated_at in classics data" >&2
    exit 1
fi

echo "Classics schema and generated seed SQL are valid"
