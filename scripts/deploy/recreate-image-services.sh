#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
DEPLOY_DIR="${REPO_ROOT}/deploy"
ENV_FILE="${DEPLOY_DIR}/.env"
PROJECT_NAME=""
COMPOSE_FILES=("${DEPLOY_DIR}/docker-compose.yml")
SERVICES=()
BUSINESS_SERVICES=(admin-web portal-web admin-starter portal-starter workers)

usage() {
    cat <<'USAGE'
Usage: scripts/deploy/recreate-image-services.sh [options] [service ...]

Recreate services after their Docker image tags have been loaded or replaced.
Without service arguments, recreates all Kuzhambu business services.

Options:
  --env FILE            Compose environment file (default: deploy/.env)
  --project-name NAME   Compose project name
  --compose-file FILE   Additional compose file; may be repeated
  -h, --help            Show this help
USAGE
}

while (($# > 0)); do
    case "$1" in
        --env)
            ENV_FILE="$2"
            shift 2
            ;;
        --project-name)
            PROJECT_NAME="$2"
            shift 2
            ;;
        --compose-file)
            COMPOSE_FILES+=("$2")
            shift 2
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        --)
            shift
            SERVICES+=("$@")
            break
            ;;
        -*)
            echo "Unsupported option: $1" >&2
            usage >&2
            exit 2
            ;;
        *)
            SERVICES+=("$1")
            shift
            ;;
    esac
done

if [[ ! -f "${ENV_FILE}" ]]; then
    echo "Missing compose environment file: ${ENV_FILE}" >&2
    exit 2
fi

if ((${#SERVICES[@]} == 0)); then
    SERVICES=("${BUSINESS_SERVICES[@]}")
fi

for service in "${SERVICES[@]}"; do
    case " ${BUSINESS_SERVICES[*]} " in
        *" ${service} "*) ;;
        *)
            echo "Only Kuzhambu business services can be recreated: ${service}" >&2
            exit 2
            ;;
    esac
done

compose=(docker compose --env-file "${ENV_FILE}")
for compose_file in "${COMPOSE_FILES[@]}"; do
    compose+=(-f "${compose_file}")
done
if [[ -n "${PROJECT_NAME}" ]]; then
    compose+=(-p "${PROJECT_NAME}")
fi

"${compose[@]}" up -d --no-deps --force-recreate "${SERVICES[@]}"

for service in "${SERVICES[@]}"; do
    container_id="$("${compose[@]}" ps -q "${service}")"
    if [[ -z "${container_id}" ]]; then
        echo "Service was not recreated: ${service}" >&2
        exit 1
    fi
    read -r running started_at image_id < <(
        docker inspect --format '{{.State.Running}} {{.State.StartedAt}} {{.Image}}' "${container_id}"
    )
    if [[ "${running}" != "true" ]]; then
        echo "Service is not running after recreation: ${service}" >&2
        exit 1
    fi
    printf '[recreate-image-services] %s started=%s image=%s\n' \
        "${service}" "${started_at}" "${image_id}"
done
