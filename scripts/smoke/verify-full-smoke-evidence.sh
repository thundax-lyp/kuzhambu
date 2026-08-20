#!/usr/bin/env bash
set -euo pipefail

expected_run_id=""
if [[ "${1:-}" == "--run-id" ]]; then
    expected_run_id="${2:-}"
    shift 2
fi

evidence_file="${1:-}"

if [[ -z "${evidence_file}" || ! -f "${evidence_file}" ]]; then
    echo "Full smoke evidence is required: pass the JSON file path as the first argument" >&2
    exit 2
fi

if ! command -v jq >/dev/null 2>&1; then
    echo "jq is required to verify full smoke evidence" >&2
    exit 2
fi

required_collections=(
    accepted successfulJobs publishedContents readyDocuments extractionTasks adoptedTasks publishedMaterials
    visibleGraphs
)

fail() {
    echo "Full smoke evidence failed: $*" >&2
    exit 1
}

jq -e '
    (.smokeRunId | type == "string" and length > 0)
    and (.generatedAt | type == "number" and . > 0)
    and (.mode == "fresh-full")
    and (.expected | type == "object")
    and (.parameters.batchSize | type == "number" and . > 0)
    and (.parameters.pollIntervalSeconds | type == "number" and . > 0)
    and (.parameters.deadlineSeconds | type == "number" and . > 0)
' "${evidence_file}" >/dev/null || fail "missing required run metadata or parameters"

if [[ -n "${expected_run_id}" ]]; then
    jq -e --arg run_id "${expected_run_id}" '.smokeRunId == $run_id' "${evidence_file}" >/dev/null \
        || fail "evidence was not generated for this smoke run"
fi

for content_type in SANCAI_ENTRY WANGQI_DOCUMENT MING_CUSTOMS; do
    jq -e --arg type "${content_type}" '
        .expected[$type] | type == "array" and length > 0 and (unique | length == length)
    ' "${evidence_file}" >/dev/null || fail "${content_type} expected set is missing, empty, or duplicated"

    for collection in "${required_collections[@]}"; do
        jq -e --arg type "${content_type}" --arg collection "${collection}" '
            .[$collection][$type] | type == "array" and (unique | length == length)
        ' "${evidence_file}" >/dev/null || fail "${content_type} ${collection} is missing or duplicated"

        jq -e --arg type "${content_type}" --arg collection "${collection}" '
            (.expected[$type] | sort) == (.[$collection][$type] | sort)
        ' "${evidence_file}" >/dev/null || fail "${content_type} ${collection} does not equal expected"
    done

    if [[ "${content_type}" != "SANCAI_ENTRY" ]]; then
        continue
    fi

    for collection in portalList portalDetails; do
        jq -e --arg type "${content_type}" --arg collection "${collection}" '
            .[$collection][$type] | type == "array" and (unique | length == length)
        ' "${evidence_file}" >/dev/null || fail "${content_type} ${collection} is missing or duplicated"

        jq -e --arg type "${content_type}" --arg collection "${collection}" '
            (.expected[$type] | sort) == (.[$collection][$type] | sort)
        ' "${evidence_file}" >/dev/null || fail "${content_type} ${collection} does not equal expected"
    done
done

jq -e '
    (.failures // []) | type == "array" and length == 0
' "${evidence_file}" >/dev/null || fail "evidence contains failures"

echo "Full smoke evidence passed: ${evidence_file}"
