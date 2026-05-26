#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "Verify repository governance files"

required_files=(
    "AGENTS.md"
    "docs/AGENTS.md"
    "docs/00-governance/ARCHITECTURE.md"
    "docs/00-governance/DOCUMENT-RULES.md"
    "docs/00-governance/NAMING-AND-PLACEMENT-RULES.md"
    "docs/00-governance/TODO-RULES.md"
    "docs/40-readiness/PR-WORKFLOW.md"
    ".github/pull_request_template.md"
    ".github/workflows/pr-verify.yml"
)

for path in "${required_files[@]}"; do
    if [[ ! -f "${ROOT_DIR}/${path}" ]]; then
        echo "Missing required file: ${path}" >&2
        exit 1
    fi
done

if find "${ROOT_DIR}/docs" -name '*.md' -print | grep -q ' '; then
    echo "Document filenames must not contain spaces" >&2
    exit 1
fi

echo "Verify complete"
