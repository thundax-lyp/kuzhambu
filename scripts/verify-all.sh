#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

if command -v /usr/libexec/java_home >/dev/null 2>&1; then
    JAVA_17_HOME="$(/usr/libexec/java_home -v 17 2>/dev/null || true)"
    if [[ -n "${JAVA_17_HOME}" ]]; then
        export JAVA_HOME="${JAVA_17_HOME}"
        export PATH="${JAVA_HOME}/bin:${PATH}"
    fi
fi

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

echo "Verify backend Maven skeleton"
(cd "${ROOT_DIR}/kuzhambu-servers" && mvn -q validate)

echo "Verify frontend package manifests"
node -e "JSON.parse(require('fs').readFileSync('${ROOT_DIR}/kuzhambu-apps/package.json', 'utf8'))"
node -e "JSON.parse(require('fs').readFileSync('${ROOT_DIR}/kuzhambu-apps/admin-web/package.json', 'utf8'))"
node -e "JSON.parse(require('fs').readFileSync('${ROOT_DIR}/kuzhambu-apps/portal-web/package.json', 'utf8'))"

echo "Verify Python worker manifest"
python3 - <<PY
import pathlib
import sys

try:
    import tomllib
except ModuleNotFoundError:
    import tomli as tomllib

path = pathlib.Path("${ROOT_DIR}/kuzhambu-workers/pyproject.toml")
tomllib.loads(path.read_text())
sys.exit(0)
PY

echo "Verify complete"
