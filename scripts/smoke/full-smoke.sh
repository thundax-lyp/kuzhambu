#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
DEPLOY_DIR="${REPO_ROOT}/deploy"
FASTGPT_DIR="${DEPLOY_DIR}/fastgpt"

KUZHAMBU_ENV_FILE="${1:-${DEPLOY_DIR}/.env}"
FASTGPT_ENV_FILE="${2:-${FASTGPT_DIR}/.env}"
KUZHAMBU_PROJECT="${KUZHAMBU_SMOKE_PROJECT:-kuzhambu-smoke-full}"
FASTGPT_PROJECT="${FASTGPT_COMPOSE_PROJECT_NAME:-kuzhambu-smoke-fastgpt}"
NETWORK_NAME="${KUZHAMBU_SMOKE_NETWORK:-kuzhambu-smoke-net}"
COMPOSE_OVERRIDE="${KUZHAMBU_SMOKE_COMPOSE_OVERRIDE:-/tmp/kuzhambu-smoke-compose.yml}"
FASTGPT_COMPOSE_OVERRIDE="${FASTGPT_SMOKE_COMPOSE_OVERRIDE:-/tmp/kuzhambu-fastgpt-smoke.yml}"
BUILD_IMAGES="${KUZHAMBU_SMOKE_BUILD_IMAGES:-}"
LOAD_IMAGES="${KUZHAMBU_SMOKE_LOAD_IMAGES:-}"
IMAGE_FILES_DIR="${KUZHAMBU_IMAGE_FILES_DIR:-}"
SMOKE_MYSQL_PORT="${KUZHAMBU_SMOKE_MYSQL_PORT:-33306}"
SEED_ENV_FILE="${KUZHAMBU_SMOKE_SEED_ENV_FILE:-/tmp/kuzhambu-smoke-seed.env}"
SMOKE_RUN_ID="${KUZHAMBU_SMOKE_RUN_ID:-$(date -u +%Y%m%dT%H%M%SZ)-$(openssl rand -hex 8)}"

if [[ ! -f "${KUZHAMBU_ENV_FILE}" ]]; then
    echo "Missing Kuzhambu env file: ${KUZHAMBU_ENV_FILE}" >&2
    exit 1
fi

if [[ ! -f "${FASTGPT_ENV_FILE}" ]]; then
    echo "Missing FastGPT env file: ${FASTGPT_ENV_FILE}" >&2
    exit 1
fi

step() {
    echo "[full-smoke] $*"
}

compose() {
    docker compose --env-file "${KUZHAMBU_ENV_FILE}" \
        --env-file "${FASTGPT_DIR}/generated/kuzhambu-fastgpt.env" \
        -f "${DEPLOY_DIR}/docker-compose.yml" \
        -f "${COMPOSE_OVERRIDE}" \
        -p "${KUZHAMBU_PROJECT}" "$@"
}

fastgpt_compose() {
    docker compose --env-file "${FASTGPT_ENV_FILE}" \
        -f "${FASTGPT_COMPOSE_OVERRIDE}" \
        -p "${FASTGPT_PROJECT}" "$@"
}

wait_http_ok() {
    local url="$1"
    local name="$2"
    local attempts="${3:-60}"
    local index=1
    while ((index <= attempts)); do
        if curl --silent --show-error --fail --max-time 5 "${url}" >/dev/null; then
            return 0
        fi
        sleep 2
        index=$((index + 1))
    done

    echo "${name} did not become ready: ${url}" >&2
    return 1
}

env_value() {
    local file="$1"
    local name="$2"
    local fallback="$3"
    local value
    value="$(grep -E "^${name}=" "${file}" | tail -1 | cut -d= -f2- || true)"
    if [[ -z "${value}" ]]; then
        value="${fallback}"
    fi
    value="${value%\"}"
    value="${value#\"}"
    value="${value%\'}"
    value="${value#\'}"
    printf '%s' "${value}"
}

BUILD_IMAGES="${BUILD_IMAGES:-$(env_value "${KUZHAMBU_ENV_FILE}" KUZHAMBU_SMOKE_BUILD_IMAGES false)}"
LOAD_IMAGES="${LOAD_IMAGES:-$(env_value "${KUZHAMBU_ENV_FILE}" KUZHAMBU_SMOKE_LOAD_IMAGES true)}"
IMAGE_FILES_DIR="${IMAGE_FILES_DIR:-$(env_value "${KUZHAMBU_ENV_FILE}" KUZHAMBU_IMAGE_FILES_DIR "${DEPLOY_DIR}/image-files")}"

wait_mysql() {
    local attempts=60
    local index=1
    local root_password
    root_password="$(env_value "${KUZHAMBU_ENV_FILE}" MYSQL_ROOT_PASSWORD kuzhambu)"
    while ((index <= attempts)); do
        if compose exec -T mysql mysqladmin ping \
            -uroot "-p${root_password}" --silent >/dev/null 2>&1; then
            return 0
        fi
        sleep 2
        index=$((index + 1))
    done

    echo "MySQL did not become ready" >&2
    return 1
}

wait_elasticsearch() {
    local attempts=90
    local index=1
    while ((index <= attempts)); do
        if compose exec -T elasticsearch curl --silent --show-error --fail \
            --max-time 5 "http://127.0.0.1:9200/_cluster/health?wait_for_status=yellow&timeout=1s" \
            >/dev/null 2>&1; then
            return 0
        fi
        sleep 2
        index=$((index + 1))
    done

    echo "Elasticsearch did not become ready" >&2
    return 1
}

write_kuzhambu_override() {
    cat > "${COMPOSE_OVERRIDE}" <<YAML
services:
  mysql:
    ports:
      - "127.0.0.1:${SMOKE_MYSQL_PORT}:3306"

networks:
  default:
    name: ${NETWORK_NAME}
    external: true
YAML
}

write_seed_env() {
    local database
    local username
    local password
    database="$(env_value "${KUZHAMBU_ENV_FILE}" MYSQL_DATABASE kuzhambu)"
    username="$(env_value "${KUZHAMBU_ENV_FILE}" MYSQL_USERNAME kuzhambu)"
    password="$(env_value "${KUZHAMBU_ENV_FILE}" MYSQL_PASSWORD kuzhambu)"
    cp "${KUZHAMBU_ENV_FILE}" "${SEED_ENV_FILE}"
    cat >> "${SEED_ENV_FILE}" <<ENV

KUZHAMBU_DB_DRIVER=com.mysql.cj.jdbc.Driver
KUZHAMBU_DB_URL=jdbc:mysql://127.0.0.1:${SMOKE_MYSQL_PORT}/${database}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
KUZHAMBU_DB_USERNAME=${username}
KUZHAMBU_DB_PASSWORD=${password}
ENV
}

write_fastgpt_override() {
    sed \
        -e '/container_name:/d' \
        -e "s#- ./bootstrap:/bootstrap:ro#- ${FASTGPT_DIR}/bootstrap:/bootstrap:ro#" \
        "${FASTGPT_DIR}/docker-compose.yml" > "${FASTGPT_COMPOSE_OVERRIDE}"
    cat >> "${FASTGPT_COMPOSE_OVERRIDE}" <<YAML

networks:
  default:
    name: ${NETWORK_NAME}
    external: true
YAML
}

step "creating shared smoke network"
docker network create "${NETWORK_NAME}" >/dev/null 2>&1 || true

step "writing compose overrides"
write_kuzhambu_override
write_fastgpt_override
mkdir -p "${FASTGPT_DIR}/generated"
touch "${FASTGPT_DIR}/generated/kuzhambu-fastgpt.env"

if [[ "${LOAD_IMAGES}" == "true" ]]; then
    step "loading Kuzhambu image archives"
    "${SCRIPT_DIR}/load-image-files.sh" "${IMAGE_FILES_DIR}"
else
    step "skipping Kuzhambu image archive loading"
fi

step "resetting smoke projects"
compose down -v --remove-orphans >/dev/null 2>&1 || true
fastgpt_compose down -v --remove-orphans >/dev/null 2>&1 || true

step "bootstrapping FastGPT"
FASTGPT_COMPOSE_PROJECT_NAME="${FASTGPT_PROJECT}" \
    "${FASTGPT_DIR}/bootstrap-fastgpt.sh" "${FASTGPT_ENV_FILE}" "${FASTGPT_COMPOSE_OVERRIDE}"

if [[ "${BUILD_IMAGES}" == "true" ]]; then
    step "building Kuzhambu images"
    compose build admin-web portal-web admin-starter portal-starter workers elasticsearch
else
    step "skipping Kuzhambu image build"
fi

step "starting Kuzhambu infrastructure"
compose up -d mysql redis elasticsearch rocketmq-namesrv rocketmq-broker workers
wait_mysql
wait_elasticsearch

step "importing database schema and seed data"
write_seed_env
"${REPO_ROOT}/scripts/import-seed-data.sh" \
    --env "${SEED_ENV_FILE}" \
    --rebuild \
    --include-test

step "starting Kuzhambu application"
compose up -d admin-starter portal-starter admin-web portal-web nginx

step "checking health endpoints"
nginx_port="$(env_value "${KUZHAMBU_ENV_FILE}" NGINX_HTTP_PORT 8080)"
wait_http_ok "http://127.0.0.1:${nginx_port}/kuzhambu-admin-api/actuator/health" "admin health" 90
wait_http_ok "http://127.0.0.1:${nginx_port}/kuzhambu-api/actuator/health" "portal health" 90
wait_http_ok "http://127.0.0.1:${nginx_port}/internal/workers/health" "workers health" 90

step "checking portal static route"
wait_http_ok "http://127.0.0.1:${nginx_port}/kuzhambu/" "portal web" 30

evidence_file="${KUZHAMBU_SMOKE_EVIDENCE_FILE:-}"
if [[ -z "${evidence_file}" ]]; then
    echo "KUZHAMBU_SMOKE_EVIDENCE_FILE is required for full smoke verification" >&2
    exit 1
fi
if [[ -e "${evidence_file}" ]]; then
    echo "Full smoke evidence file must not exist before the run: ${evidence_file}" >&2
    exit 1
fi

step "verifying full smoke evidence"
"${SCRIPT_DIR}/verify-full-smoke-evidence.sh" --run-id "${SMOKE_RUN_ID}" "${evidence_file}"

echo "Docker full smoke passed"
