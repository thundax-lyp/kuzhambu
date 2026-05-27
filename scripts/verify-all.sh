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
    "docs/00-governance/SERVERS-ARCHITECTURE.md"
    "docs/00-governance/SERVERS-ARCHITECTURE-RULES.md"
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
"${ROOT_DIR}/scripts/verify-classics.sh"
JAVA_SPEC_VERSION="$(java -XshowSettings:properties -version 2>&1 | awk -F '= ' '/java.specification.version/ {print $2; exit}')"
case "${JAVA_SPEC_VERSION}" in
    1.8|8|9|10)
        echo "Java 11+ is required for backend verification; current java.specification.version=${JAVA_SPEC_VERSION}" >&2
        exit 1
        ;;
esac

required_server_paths=(
    "kuzhambu-servers/common"
    "kuzhambu-servers/biz"
    "kuzhambu-servers/starter"
    "kuzhambu-servers/biz/system/kuzhambu-system-interface"
    "kuzhambu-servers/biz/system/kuzhambu-system-application"
    "kuzhambu-servers/biz/system/kuzhambu-system-domain"
    "kuzhambu-servers/biz/system/kuzhambu-system-infra"
    "kuzhambu-servers/starter/kuzhambu-admin-starter"
    "kuzhambu-servers/starter/kuzhambu-portal-starter"
)

for path in "${required_server_paths[@]}"; do
    if [[ ! -e "${ROOT_DIR}/${path}" ]]; then
        echo "Missing required server path: ${path}" >&2
        exit 1
    fi
done

if [[ -d "${ROOT_DIR}/kuzhambu-servers/interfaces" ]]; then
    echo "Legacy kuzhambu-servers/interfaces must be migrated to starter/domain interface modules" >&2
    exit 1
fi

(cd "${ROOT_DIR}/kuzhambu-servers" && mvn -q test)

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
