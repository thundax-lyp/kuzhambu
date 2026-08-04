#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FASTGPT_ENV_FILE="${1:-${SCRIPT_DIR}/.env}"
KUZHAMBU_ENV_FILE="${2:-${SCRIPT_DIR}/generated/kuzhambu-fastgpt.env}"
COMPOSE_FILE="${3:-${SCRIPT_DIR}/docker-compose.yml}"

if [[ ! -f "${FASTGPT_ENV_FILE}" ]]; then
    echo "Missing FastGPT env file: ${FASTGPT_ENV_FILE}" >&2
    exit 1
fi

if [[ ! -f "${KUZHAMBU_ENV_FILE}" ]]; then
    echo "Missing generated Kuzhambu FastGPT env file: ${KUZHAMBU_ENV_FILE}" >&2
    exit 1
fi

set -a
# shellcheck disable=SC1090
source "${FASTGPT_ENV_FILE}"
# shellcheck disable=SC1090
source "${KUZHAMBU_ENV_FILE}"
set +a

PROJECT_NAME="${FASTGPT_COMPOSE_PROJECT_NAME:-kuzhambu-fastgpt}"
BASE_URL="${FASTGPT_SMOKE_BASE_URL:-}"

if [[ -z "${BASE_URL}" ]]; then
    BASE_URL="http://127.0.0.1:${FASTGPT_HTTP_PORT:-13000}"
fi

require_env() {
    local name="$1"
    if [[ -z "${!name:-}" ]]; then
        echo "Missing required env: ${name}" >&2
        exit 1
    fi
}

step() {
    echo "[fastgpt-smoke] $*"
}

api_get() {
    curl --fail --silent --show-error --max-time 20 "$@"
}

api_json() {
    local method="$1"
    local path="$2"
    local payload="$3"
    curl --fail --silent --show-error --max-time 30 \
        -X "${method}" \
        "${BASE_URL}${path}" \
        -H "Authorization: Bearer ${KUZHAMBU_KNOWLEDGE_FASTGPT_API_KEY}" \
        -H "Content-Type: application/json" \
        -d "${payload}"
}

json_health_ok() {
    python3 -c '
import json
import sys
body = json.load(sys.stdin)
data = body.get("data") or {}
sys.exit(0 if body.get("code") == 200 and (data.get("valid") is True or data.get("available") is True) else 1)
'
}

json_dataset_visible() {
    local dataset_id="$1"
    python3 -c '
import json
import sys
dataset_id = sys.argv[1]
body = json.load(sys.stdin)
data = body.get("data") or {}
items = data.get("list") if isinstance(data, dict) else data
if not isinstance(items, list):
    items = []
for item in items:
    value = item.get("_id") or item.get("datasetId") or item.get("id")
    if value == dataset_id:
        sys.exit(0)
sys.exit(1)
' "${dataset_id}"
}

json_collection_id() {
    python3 -c '
import json
import sys
body = json.load(sys.stdin)
data = body.get("data")
value = None
if isinstance(data, dict):
    value = data.get("_id") or data.get("collectionId") or data.get("id")
elif isinstance(data, str):
    value = data
print(value or "")
'
}

json_code_ok() {
    python3 -c '
import json
import sys
body = json.load(sys.stdin)
sys.exit(0 if body.get("code") == 200 else 1)
'
}

json_insert_len_one() {
    python3 -c '
import json
import sys
body = json.load(sys.stdin)
data = body.get("data") if isinstance(body.get("data"), dict) else {}
insert_len = data.get("insertLen", body.get("insertLen", 0))
sys.exit(0 if body.get("code") == 200 and insert_len == 1 else 1)
'
}

json_data_page_ok() {
    python3 -c '
import json
import sys
body = json.load(sys.stdin)
data = body.get("data") if isinstance(body.get("data"), dict) else {}
sys.exit(0 if body.get("code") == 200 and isinstance(data.get("total", 0), int) else 1)
'
}

require_env FASTGPT_BOOTSTRAP_LLM_MODEL
require_env FASTGPT_BOOTSTRAP_EMBEDDING_MODEL
require_env KUZHAMBU_KNOWLEDGE_FASTGPT_API_KEY
require_env KUZHAMBU_KNOWLEDGE_FASTGPT_KNOWLEDGE_BASE_ID

step "checking compose services"
docker compose --env-file "${FASTGPT_ENV_FILE}" -f "${COMPOSE_FILE}" -p "${PROJECT_NAME}" ps >/dev/null

step "checking bootstrap records in MongoDB"
docker compose --env-file "${FASTGPT_ENV_FILE}" -f "${COMPOSE_FILE}" -p "${PROJECT_NAME}" exec -T fastgpt-mongo \
    mongo "mongodb://${FASTGPT_MONGO_USERNAME:-fastgpt}:${FASTGPT_MONGO_PASSWORD:-fastgpt}@fastgpt-mongo:27017/fastgpt?authSource=admin" --quiet --eval "
const llm = db.system_models.findOne({model: '${FASTGPT_BOOTSTRAP_LLM_MODEL}', 'metadata.type': 'llm', 'metadata.isActive': true});
const embedding = db.system_models.findOne({model: '${FASTGPT_BOOTSTRAP_EMBEDDING_MODEL}', 'metadata.type': 'embedding', 'metadata.isActive': true});
const dataset = db.datasets.findOne({_id: ObjectId('${KUZHAMBU_KNOWLEDGE_FASTGPT_KNOWLEDGE_BASE_ID}'), deleteTime: null});
if (!llm) throw new Error('LLM model missing');
if (!embedding) throw new Error('embedding model missing');
if (!dataset) throw new Error('Kuzhambu dataset missing');
print('FastGPT model and dataset records are present');
" >/dev/null

step "checking OpenAPI health"
api_get "${BASE_URL}/api/support/openapi/health?apiKey=${KUZHAMBU_KNOWLEDGE_FASTGPT_API_KEY}" \
    | json_health_ok

step "checking dataset visibility"
api_json POST "/api/core/dataset/list" "{\"pageNum\":1,\"pageSize\":10}" \
    | json_dataset_visible "${KUZHAMBU_KNOWLEDGE_FASTGPT_KNOWLEDGE_BASE_ID}"

step "creating temporary collection"
collection_name="kuzhambu-fastgpt-smoke-$(date +%s)"
collection_id="$(
    api_json POST "/api/core/dataset/collection/create" \
        "{\"datasetId\":\"${KUZHAMBU_KNOWLEDGE_FASTGPT_KNOWLEDGE_BASE_ID}\",\"name\":\"${collection_name}\",\"type\":\"virtual\"}" \
        | json_collection_id
)"

if [[ -z "${collection_id}" || "${collection_id}" == "null" ]]; then
    echo "FastGPT collection create did not return an ID" >&2
    exit 1
fi

cleanup() {
    curl --silent --show-error --max-time 20 \
        -X DELETE \
        "${BASE_URL}/api/core/dataset/collection/delete?id=${collection_id}" \
        -H "Authorization: Bearer ${KUZHAMBU_KNOWLEDGE_FASTGPT_API_KEY}" >/dev/null || true
}
trap cleanup EXIT

step "disabling temporary collection"
api_json POST "/api/core/dataset/collection/update" \
    "{\"id\":\"${collection_id}\",\"forbid\":true}" | json_code_ok

step "checking collection data list endpoint"
api_json POST "/api/core/dataset/data/v2/list" \
    "{\"collectionId\":\"${collection_id}\",\"offset\":0,\"pageSize\":30}" | json_data_page_ok

step "pushing temporary data"
api_json POST "/api/core/dataset/data/pushData" \
    "{\"collectionId\":\"${collection_id}\",\"data\":[{\"q\":\"kuzhambu smoke question\",\"a\":\"kuzhambu smoke answer\",\"chunkIndex\":0}]}" \
    | json_insert_len_one

step "enabling temporary collection"
api_json POST "/api/core/dataset/collection/update" \
    "{\"id\":\"${collection_id}\",\"forbid\":false}" | json_code_ok

step "deleting temporary collection"
cleanup
trap - EXIT

echo "FastGPT smoke passed"
