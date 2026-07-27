#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${ROOT_DIR}/dev.env"
BASE_URL=""
USERNAME="${KUZHAMBU_SMOKE_ADMIN_USERNAME:-developer}"
PASSWORD="${KUZHAMBU_SMOKE_ADMIN_PASSWORD:-Q1w2e3r$}"
CAPTCHA="${KUZHAMBU_SMOKE_CAPTCHA:-6666}"
LIVE_AI=0
INCLUDE_MUTATING=0
ACCESS_TOKEN="${KUZHAMBU_SMOKE_ACCESS_TOKEN:-}"
FAILURES=0
PASSES=0
SKIPS=0
LAST_RESPONSE_FILE=""

usage() {
    cat <<'USAGE'
Usage:
  scripts/smoke-ai-backend.sh [options]

Options:
  --env FILE             Env file to source. Default: ./dev.env
  --base-url URL         Admin API base URL. Default from dev.env:
                         http://127.0.0.1:${KUZHAMBU_ADMIN_SERVER_PORT}${KUZHAMBU_ADMIN_SERVER_CONTEXT_PATH}
  --username NAME        Admin username. Default: developer
  --password PASSWORD    Admin password. Default: Q1w2e3r$
  --token TOKEN          Reuse an existing Access-Token instead of logging in.
  --live-ai             Call worker/model-backed AI invoke endpoints.
  --include-mutating     Run temporary create/update/cancel style smoke checks.
  -h, --help             Show this help.

Environment overrides:
  KUZHAMBU_SMOKE_ADMIN_USERNAME
  KUZHAMBU_SMOKE_ADMIN_PASSWORD
  KUZHAMBU_SMOKE_CAPTCHA
  KUZHAMBU_SMOKE_ACCESS_TOKEN

Notes:
  Default mode is non-destructive and covers all read/list/page AI backend endpoints.
  --live-ai can spend model quota and requires worker/model endpoint configuration.
  --include-mutating writes temporary model/batch rows and cancels the temporary batch.
USAGE
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        --env)
            ENV_FILE="$2"
            shift 2
            ;;
        --base-url)
            BASE_URL="${2%/}"
            shift 2
            ;;
        --username)
            USERNAME="$2"
            shift 2
            ;;
        --password)
            PASSWORD="$2"
            shift 2
            ;;
        --token)
            ACCESS_TOKEN="$2"
            shift 2
            ;;
        --live-ai)
            LIVE_AI=1
            shift
            ;;
        --include-mutating)
            INCLUDE_MUTATING=1
            shift
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            echo "error: unknown argument: $1" >&2
            usage >&2
            exit 2
            ;;
    esac
done

require_command() {
    if ! command -v "$1" >/dev/null 2>&1; then
        echo "error: required command not found: $1" >&2
        exit 2
    fi
}

require_command curl
require_command jq
require_command node

if [[ -f "${ENV_FILE}" ]]; then
    set -a
    # shellcheck disable=SC1090
    source "${ENV_FILE}"
    set +a
fi

detect_current_worktree_admin_base_url() {
    if ! command -v lsof >/dev/null 2>&1; then
        return 1
    fi
    local line pid socket port cwd
    while read -r line; do
        pid="$(awk '{print $1}' <<<"${line}")"
        socket="$(awk '{print $2}' <<<"${line}")"
        port="${socket##*:}"
        if [[ -z "${pid}" || -z "${port}" || ! "${port}" =~ ^[0-9]+$ ]]; then
            continue
        fi
        cwd="$(lsof -a -p "${pid}" -d cwd -Fn 2>/dev/null | sed -n 's/^n//p' | head -1)"
        if [[ "${cwd}" == "${ROOT_DIR}"* ]]; then
            printf 'http://127.0.0.1:%s%s' "${port}" "${KUZHAMBU_ADMIN_SERVER_CONTEXT_PATH:-/kuzhambu-admin-api}"
            return 0
        fi
    done < <(lsof -nP -iTCP -sTCP:LISTEN 2>/dev/null | awk '$1 == "java" && $9 ~ /:[0-9]+$/ {print $2, $9}')
    return 1
}

if [[ -z "${BASE_URL}" ]]; then
    if BASE_URL="$(detect_current_worktree_admin_base_url)"; then
        :
    else
        admin_port="${KUZHAMBU_ADMIN_SERVER_PORT:-20010}"
        admin_context="${KUZHAMBU_ADMIN_SERVER_CONTEXT_PATH:-/kuzhambu-admin-api}"
        BASE_URL="http://127.0.0.1:${admin_port}${admin_context}"
    fi
fi

TMP_DIR="$(mktemp -d /tmp/kuzhambu-ai-smoke-XXXXXX)"
trap 'rm -rf "${TMP_DIR}"' EXIT

log_pass() {
    PASSES=$((PASSES + 1))
    printf 'PASS %s\n' "$1"
}

log_skip() {
    SKIPS=$((SKIPS + 1))
    printf 'SKIP %s - %s\n' "$1" "$2"
}

log_fail() {
    FAILURES=$((FAILURES + 1))
    printf 'FAIL %s - %s\n' "$1" "$2" >&2
}

response_data() {
    jq -c '.data' "${LAST_RESPONSE_FILE}"
}

api_post() {
    local name="$1"
    local path="$2"
    local body="$3"
    local response_file="${TMP_DIR}/${name//[^A-Za-z0-9_.-]/_}.json"
    local status_file="${TMP_DIR}/${name//[^A-Za-z0-9_.-]/_}.status"
    local status

    status="$(curl -sS -o "${response_file}" -w '%{http_code}' \
        -X POST "${BASE_URL}${path}" \
        -H 'Content-Type: application/json' \
        ${ACCESS_TOKEN:+-H "Access-Token: ${ACCESS_TOKEN}"} \
        -d "${body}" || true)"
    printf '%s' "${status}" > "${status_file}"
    LAST_RESPONSE_FILE="${response_file}"

    if [[ "${status}" != "200" ]]; then
        log_fail "${name}" "HTTP ${status}; body=$(head -c 300 "${response_file}")"
        return 1
    fi
    local code
    code="$(jq -r '.code // empty' "${response_file}" 2>/dev/null || true)"
    if [[ "${code}" != "COMMON-00000" ]]; then
        log_fail "${name}" "code=${code:-missing}; message=$(jq -r '.message // empty' "${response_file}" 2>/dev/null)"
        return 1
    fi
    log_pass "${name}"
    return 0
}

api_get_raw() {
    local name="$1"
    local path="$2"
    local response_file="${TMP_DIR}/${name//[^A-Za-z0-9_.-]/_}.raw"
    local status
    status="$(curl -sS -m 5 -o "${response_file}" -w '%{http_code}' \
        "${BASE_URL}${path}" \
        ${ACCESS_TOKEN:+-H "Access-Token: ${ACCESS_TOKEN}"} || true)"
    LAST_RESPONSE_FILE="${response_file}"
    if [[ "${status}" == "200" ]]; then
        log_pass "${name}"
        return 0
    fi
    log_fail "${name}" "HTTP ${status}; body=$(head -c 300 "${response_file}")"
    return 1
}

login() {
    if [[ -n "${ACCESS_TOKEN}" ]]; then
        log_pass "auth/token/reuse"
        return
    fi

    api_post "auth/pre-auth-session" "/api/auth/session/pre-auth-session" '{}' >/dev/null || exit 1
    local login_token public_key encrypted_password
    login_token="$(jq -r '.data.loginToken' "${LAST_RESPONSE_FILE}")"
    public_key="$(jq -r '.data.publicKey' "${LAST_RESPONSE_FILE}")"
    encrypted_password="$(PUBLIC_KEY="${public_key}" PLAIN_PASSWORD="${PASSWORD}" NODE_PATH="${ROOT_DIR}/kuzhambu-apps/node_modules/.pnpm/sm-crypto@0.4.0/node_modules" \
        node -e 'const { sm2 } = require("sm-crypto"); console.log(sm2.doEncrypt(process.env.PLAIN_PASSWORD, process.env.PUBLIC_KEY, 0));')"

    local body
    body="$(jq -n \
        --arg loginToken "${login_token}" \
        --arg userName "${USERNAME}" \
        --arg password "${encrypted_password}" \
        --arg captcha "${CAPTCHA}" \
        '{loginToken:$loginToken,userName:$userName,password:$password,captcha:$captcha}')"
    api_post "auth/login" "/api/auth/session/login" "${body}" >/dev/null || exit 1
    ACCESS_TOKEN="$(jq -r '.data.token' "${LAST_RESPONSE_FILE}")"
    if [[ -z "${ACCESS_TOKEN}" || "${ACCESS_TOKEN}" == "null" ]]; then
        echo "error: login response did not contain data.token" >&2
        exit 1
    fi
    log_pass "auth/token/acquired"
}

read_smoke() {
    api_post "ai.config.model.list" "/api/ai/config/model/list" '{"enabled":true}' || true
    local model_id
    model_id="$(jq -r '.data[0].id // empty' "${LAST_RESPONSE_FILE}")"
    if [[ -n "${model_id}" ]]; then
        api_post "ai.config.model.get" "/api/ai/config/model/get" "{\"id\":${model_id}}" || true
    else
        log_skip "ai.config.model.get" "model/list returned no rows"
    fi

    api_post "ai.config.capability.list" "/api/ai/config/capability/list" '{"enabled":true}' || true
    api_post "ai.config.capability.get" "/api/ai/config/capability/get" '{"capability":"classics_translate"}' || true

    api_post "ai.config.business-config.list" "/api/ai/config/business-config/list" '{"enabled":true}' || true
    local business_config_id
    business_config_id="$(jq -r '.data[0].id // empty' "${LAST_RESPONSE_FILE}")"
    if [[ -n "${business_config_id}" ]]; then
        api_post "ai.config.business-config.get" "/api/ai/config/business-config/get" "{\"id\":${business_config_id}}" || true
    else
        log_skip "ai.config.business-config.get" "business-config/list returned no rows"
    fi

    api_post "ai.config.prompt.template.list" "/api/ai/config/prompt/template/list" '{"enabled":true}' || true
    local prompt_template_id prompt_capability current_version_no
    prompt_template_id="$(jq -r '.data[0].id // empty' "${LAST_RESPONSE_FILE}" 2>/dev/null || true)"
    prompt_capability="$(jq -r '.data[0].capability // empty' "${LAST_RESPONSE_FILE}" 2>/dev/null || true)"
    current_version_no="$(jq -r '.data[0].currentVersionNo // empty' "${LAST_RESPONSE_FILE}" 2>/dev/null || true)"
    if [[ -n "${prompt_template_id}" ]]; then
        api_post "ai.config.prompt.template.get" "/api/ai/config/prompt/template/get" "{\"id\":${prompt_template_id}}" || true
        if [[ -n "${prompt_capability}" ]]; then
            api_post "ai.config.prompt.template.get-by-capability" "/api/ai/config/prompt/template/get-by-capability" "{\"capability\":\"${prompt_capability}\"}" || true
        else
            log_skip "ai.config.prompt.template.get-by-capability" "template/list returned no capability"
        fi
        api_post "ai.config.prompt.version.current" "/api/ai/config/prompt/version/current" "{\"id\":${prompt_template_id}}" || true
        api_post "ai.config.prompt.version.list" "/api/ai/config/prompt/version/list" "{\"id\":${prompt_template_id}}" || true
        if [[ -n "${current_version_no}" && "${current_version_no}" != "null" ]]; then
            api_post "ai.config.prompt.version.compare" "/api/ai/config/prompt/version/compare" \
                "{\"id\":${prompt_template_id},\"leftVersionNo\":${current_version_no},\"rightVersionNo\":${current_version_no}}" || true
        else
            log_skip "ai.config.prompt.version.compare" "template/list returned no currentVersionNo"
        fi

        api_post "ai.config.prompt.variable.list" "/api/ai/config/prompt/variable/list" "{\"id\":${prompt_template_id}}" || true
        local provided_names
        provided_names="$(jq -c '[.data[]? | select(.required == true) | .variableName]' "${LAST_RESPONSE_FILE}" 2>/dev/null || printf '[]')"
        api_post "ai.config.prompt.variable.validate" "/api/ai/config/prompt/variable/validate" \
            "$(jq -n --argjson id "${prompt_template_id}" --argjson providedNames "${provided_names}" '{id:$id,providedNames:$providedNames}')" || true
    else
        log_skip "ai.config.prompt.template.get" "template/list returned no rows"
        log_skip "ai.config.prompt.template.get-by-capability" "template/list returned no rows"
        log_skip "ai.config.prompt.version.current" "template/list returned no rows"
        log_skip "ai.config.prompt.version.list" "template/list returned no rows"
        log_skip "ai.config.prompt.version.compare" "template/list returned no rows"
        log_skip "ai.config.prompt.variable.list" "template/list returned no rows"
        log_skip "ai.config.prompt.variable.validate" "template/list returned no rows"
    fi

    api_post "ai.refinement.task.page" "/api/ai/refinement/task/page" '{"pageNo":1,"pageSize":1}' || true
    local task_id
    task_id="$(jq -r '.data.items[0].taskId // empty' "${LAST_RESPONSE_FILE}" 2>/dev/null || true)"
    if [[ -n "${task_id}" ]]; then
        api_post "ai.refinement.task.get" "/api/ai/refinement/task/get" "{\"taskId\":${task_id}}" || true
        log_skip "ai.refinement.task.stream" "default mode does not subscribe to historical task streams"
    else
        log_skip "ai.refinement.task.get" "task/page returned no rows"
        log_skip "ai.refinement.task.stream" "task/page returned no rows"
    fi

    api_post "ai.invocation.invocation-log.page" "/api/ai/invocation/invocation-log/page" '{"pageNo":1,"pageSize":1}' || true
    local call_id
    call_id="$(jq -r '.data.records[0].callId // empty' "${LAST_RESPONSE_FILE}" 2>/dev/null || true)"
    if [[ -n "${call_id}" ]]; then
        api_post "ai.invocation.invocation-log.get" "/api/ai/invocation/invocation-log/get" "{\"callId\":${call_id}}" || true
    else
        log_skip "ai.invocation.invocation-log.get" "invocation-log/page returned no rows"
    fi
    api_post "ai.invocation.invocation-log.summary" "/api/ai/invocation/invocation-log/summary" '{}' || true

    api_post "ai.invocation.candidate.list" "/api/ai/invocation/candidate/list" '{}' || true
    local candidate_id
    candidate_id="$(jq -r '.data[0].candidateId // empty' "${LAST_RESPONSE_FILE}" 2>/dev/null || true)"
    if [[ -n "${candidate_id}" ]]; then
        api_post "ai.invocation.candidate.get" "/api/ai/invocation/candidate/get" "{\"candidateId\":${candidate_id}}" || true
    else
        log_skip "ai.invocation.candidate.get" "candidate/list returned no rows"
        log_skip "ai.invocation.candidate.reject" "requires an existing disposable candidate"
        log_skip "ai.invocation.candidate.mark-applied" "requires an existing disposable candidate"
    fi

    log_skip "ai.invocation.batch.get" "requires a batch id; use --include-mutating to create one"
    log_skip "ai.invocation.batch.can-dispatch" "requires a batch id; use --include-mutating to create one"
}

mutating_smoke() {
    local suffix="smoke-$(date +%s)"
    local model_body model_id update_body
    model_body="$(jq -n --arg suffix "${suffix}" '{
        apiSource:"OPENAI",
        baseUrl:"http://127.0.0.1/smoke",
        apiKey:"",
        modelName:("kuzhambu-smoke-" + $suffix),
        displayName:("冒烟临时模型 " + $suffix),
        capabilities:["TEXT2TEXT"],
        defaultParamsJson:"{\"temperature\":0.1}",
        description:"Temporary AI backend smoke model.",
        enabled:false
    }')"
    if api_post "ai.config.model.create" "/api/ai/config/model/create" "${model_body}"; then
        model_id="$(jq -r '.data.id // empty' "${LAST_RESPONSE_FILE}")"
        if [[ -n "${model_id}" ]]; then
            update_body="$(jq -n --argjson id "${model_id}" --arg suffix "${suffix}" '{
                id:$id,
                apiSource:"OPENAI",
                baseUrl:"http://127.0.0.1/smoke-updated",
                apiKey:"",
                modelName:("kuzhambu-smoke-" + $suffix),
                displayName:("冒烟临时模型更新 " + $suffix),
                capabilities:["TEXT2TEXT"],
                defaultParamsJson:"{\"temperature\":0.1}",
                description:"Temporary AI backend smoke model updated.",
                enabled:false
            }')"
            api_post "ai.config.model.update" "/api/ai/config/model/update" "${update_body}" || true
            api_post "ai.config.model.delete" "/api/ai/config/model/delete" "{\"id\":${model_id}}" || true
        else
            log_skip "ai.config.model.update" "model/create did not return id"
            log_skip "ai.config.model.delete" "model/create did not return id"
        fi
    fi

    log_skip "ai.config.business-config.create" "capability is enum-backed and unique; no disposable capability exists"
    log_skip "ai.config.business-config.update" "safe no-op update is covered manually by config persistence tests"
    log_skip "ai.config.business-config.delete" "would remove seed business config"
    log_skip "ai.config.prompt.template.save" "no delete endpoint exists for a disposable prompt template"
    log_skip "ai.config.prompt.version.rollback" "would mutate seed prompt current version"
    log_skip "ai.config.prompt.optimization.suggest" "covered by direct service tests; endpoint requires edit permission but has no persisted effect"

    local batch_body batch_id
    batch_body='{"scope":"SMOKE","capability":"classics_summary","contentType":"SANCAI_ENTRY","totalCount":2,"failureSummaryJson":"{}"}'
    if api_post "ai.invocation.batch.create" "/api/ai/invocation/batch/create" "${batch_body}"; then
        batch_id="$(jq -r '.data.id // empty' "${LAST_RESPONSE_FILE}")"
        if [[ -n "${batch_id}" ]]; then
            api_post "ai.invocation.batch.get" "/api/ai/invocation/batch/get" "{\"batchId\":${batch_id}}" || true
            api_post "ai.invocation.batch.can-dispatch" "/api/ai/invocation/batch/can-dispatch" "{\"batchId\":${batch_id}}" || true
            api_post "ai.invocation.batch.record-success" "/api/ai/invocation/batch/record-success" "{\"batchId\":${batch_id}}" || true
            api_post "ai.invocation.batch.record-failure" "/api/ai/invocation/batch/record-failure" "{\"batchId\":${batch_id},\"failureSummaryJson\":\"{\\\"smoke\\\":true}\"}" || true
            api_post "ai.invocation.batch.cancel" "/api/ai/invocation/batch/cancel" "{\"batchId\":${batch_id}}" || true
        else
            log_skip "ai.invocation.batch.get" "batch/create did not return id"
            log_skip "ai.invocation.batch.can-dispatch" "batch/create did not return id"
            log_skip "ai.invocation.batch.record-success" "batch/create did not return id"
            log_skip "ai.invocation.batch.record-failure" "batch/create did not return id"
            log_skip "ai.invocation.batch.cancel" "batch/create did not return id"
        fi
    fi

    local task_batch_body task_batch_id
    task_batch_body='{"scope":"SMOKE","capability":"classics_summary","contentType":"SANCAI_ENTRY","totalCount":1,"failureSummaryJson":"{}"}'
    if api_post "ai.refinement.task.batch.create" "/api/ai/refinement/task/batch/create" "${task_batch_body}"; then
        task_batch_id="$(jq -r '.data.batchId // empty' "${LAST_RESPONSE_FILE}")"
        if [[ -n "${task_batch_id}" ]]; then
            api_post "ai.refinement.task.batch.get" "/api/ai/refinement/task/batch/get" "{\"batchId\":${task_batch_id}}" || true
            api_post "ai.refinement.task.batch.cancel" "/api/ai/refinement/task/batch/cancel" "{\"batchId\":${task_batch_id}}" || true
        else
            log_skip "ai.refinement.task.batch.get" "batch/create did not return batchId"
            log_skip "ai.refinement.task.batch.cancel" "batch/create did not return batchId"
        fi
    fi
}

live_ai_body() {
    local capability="$1"
    local prompt_version_id="${2:-940106}"
    local model_id="${3:-900102}"
    local request_id="smoke-${capability}-$(date +%s%N)"
    jq -n \
        --arg requestId "${request_id}" \
        --arg traceId "${request_id}" \
        --arg capability "${capability}" \
        --argjson promptVersionId "${prompt_version_id}" \
        --argjson modelId "${model_id}" \
        --arg messages '[{"role":"system","content":"你是冒烟测试助手，只返回简短结果。"},{"role":"user","content":"请返回 ok。"}]' \
        --arg variables '{"title":"冒烟","sourceText":"天地玄黄。","document":"甲器用于祭礼。","contentType":"SANCAI_ENTRY","query":"礼制","question":"甲器用于什么？","sources":"甲器用于祭礼。","targetCapability":"classics_summary","currentPrompt":"请摘要。","afterContent":"冒烟测试内容。"}' \
        --arg inputPayload '{"entryId":1,"title":"冒烟","sourceText":"天地玄黄。","document":"甲器用于祭礼。","query":"礼制","question":"甲器用于什么？","sources":["甲器用于祭礼。"]}' \
        '{
            scope:"SMOKE",
            operation:"SMOKE",
            contentType:"SANCAI_ENTRY",
            contentId:1,
            requestedBy:1,
            modelId:$modelId,
            promptVersionId:$promptVersionId,
            requestId:$requestId,
            traceId:$traceId,
            promptMessagesJson:$messages,
            promptVariablesJson:$variables,
            inputPayloadJson:$inputPayload,
            outputSchemaJson:"{\"type\":\"text\"}",
            forceJson:false,
            locale:"zh-CN",
            capability:$capability,
            createCandidate:false
        }'
}

live_ai_smoke() {
    local text_body
    text_body="$(live_ai_body "classics_translate" 940106)"
    api_post "ai.refinement.translate.live" "/api/ai/refinement/translate" "${text_body}" || true
    api_post "ai.refinement.summary.live" "/api/ai/refinement/summary" "$(live_ai_body "classics_summary" 940101)" || true
    api_post "ai.refinement.tags.live" "/api/ai/refinement/tags" "$(live_ai_body "classics_tags" 940102 | jq '.forceJson=true | .outputSchemaJson="{\"type\":\"object\"}"')" || true
    api_post "ai.refinement.qa.live" "/api/ai/refinement/qa" "$(live_ai_body "classics_qa" 940103 | jq '.forceJson=true | .outputSchemaJson="{\"type\":\"object\"}"')" || true
    api_post "ai.refinement.fusion.live" "/api/ai/refinement/fusion" "$(live_ai_body "classics_image_prompt_fusion" 940109)" || true
    api_post "ai.refinement.visual.live" "/api/ai/refinement/visual" "$(live_ai_body "classics_visual_describe" 940110)" || true
    api_post "ai.refinement.split.live" "/api/ai/refinement/split" "$(live_ai_body "classics_split" 940112 | jq '.forceJson=true | .outputSchemaJson="{\"type\":\"object\"}"')" || true
    api_post "ai.refinement.image-gen.live" "/api/ai/refinement/image-gen" "$(live_ai_body "classics_image_generate" 940108 900201)" || true
    api_post "ai.refinement.image-analysis.live" "/api/ai/refinement/image-analysis" "$(live_ai_body "classics_image_describe" 940107 900101)" || true

    api_post "ai.platform.prompt-suggestion.live" "/api/ai/platform/prompt-suggestion" "$(live_ai_body "prompt_suggestion" 940116 | jq '.contentType="AI_PROMPT" | .forceJson=true | .outputSchemaJson="{\"type\":\"object\"}"')" || true
    api_post "ai.platform.version-summary.live" "/api/ai/platform/version-summary" "$(live_ai_body "platform_version_summary" 940117 | jq '.contentType="AI_PROMPT"')" || true

    log_skip "ai.refinement.task.add" "live async task creation is not run by default; call endpoint manually when worker stream behavior is under test"
    log_skip "ai.refinement.task.cancel" "requires a disposable task id"
}

printf 'AI backend smoke target: %s\n' "${BASE_URL}"
login
read_smoke

if [[ "${INCLUDE_MUTATING}" == "1" ]]; then
    mutating_smoke
fi

if [[ "${LIVE_AI}" == "1" ]]; then
    live_ai_smoke
else
    log_skip "ai.refinement.*.live" "use --live-ai to call worker/model-backed refinement endpoints"
    log_skip "ai.platform.*.live" "use --live-ai to call worker/model-backed platform endpoints"
fi

printf 'AI backend smoke summary: pass=%d skip=%d fail=%d\n' "${PASSES}" "${SKIPS}" "${FAILURES}"
if [[ "${FAILURES}" -gt 0 ]]; then
    exit 1
fi
