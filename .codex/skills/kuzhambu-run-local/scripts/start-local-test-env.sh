#!/usr/bin/env bash
set -euo pipefail

usage() {
    cat <<'EOF'
Usage:
  start-local-test-env.sh [--env-file PATH] admin [portal]
  start-local-test-env.sh [--env-file PATH] portal

Targets:
  admin   Start workers, admin-starter, and admin-web.
  portal  Start portal-starter and portal-web.
EOF
}

repo_root="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
cd "$repo_root"

env_file="dev.env"
targets=()

while [[ $# -gt 0 ]]; do
    case "$1" in
        --env-file)
            if [[ $# -lt 2 ]]; then
                echo "ERROR: --env-file requires a path." >&2
                exit 2
            fi
            env_file="$2"
            shift 2
            ;;
        admin|portal)
            targets+=("$1")
            shift
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            echo "ERROR: unknown argument: $1" >&2
            usage >&2
            exit 2
            ;;
    esac
done

if [[ ${#targets[@]} -eq 0 ]]; then
    echo "ERROR: at least one target is required: admin or portal." >&2
    usage >&2
    exit 2
fi

if [[ ! -f docs/AGENTS.md || ! -d kuzhambu-servers || ! -d kuzhambu-apps ]]; then
    echo "ERROR: run this script from the Kuzhambu repository root." >&2
    exit 2
fi

if [[ -f "$env_file" ]]; then
    set -a
    # shellcheck disable=SC1090
    source "$env_file"
    set +a
else
    echo "WARN: env file not found: $env_file; using process environment and project defaults." >&2
fi

state_dir=".codex/local-test-env"
log_dir="$state_dir/logs"
pid_dir="$state_dir/pids"
mkdir -p "$log_dir" "$pid_dir"

has_target() {
    local target="$1"
    local item
    for item in "${targets[@]}"; do
        [[ "$item" == "$target" ]] && return 0
    done
    return 1
}

port_from_url() {
    local url="$1"
    local fallback="$2"
    if [[ "$url" =~ :([0-9]+)(/|$) ]]; then
        echo "${BASH_REMATCH[1]}"
    else
        echo "$fallback"
    fi
}

port_in_use() {
    local port="$1"
    lsof -nP -iTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1
}

next_free_port_from() {
    local port="$1"
    shift
    local reserved=("$@")
    local reserved_port
    local is_reserved
    while port_in_use "$port"; do
        port=$((port + 1))
    done
    while true; do
        is_reserved=0
        for reserved_port in "${reserved[@]}"; do
            if [[ "$port" == "$reserved_port" ]]; then
                is_reserved=1
                break
            fi
        done
        [[ "$is_reserved" -eq 0 ]] && break
        port=$((port + 1))
        while port_in_use "$port"; do
            port=$((port + 1))
        done
    done
    echo "$port"
}

note_port() {
    local name="$1"
    local requested="$2"
    local actual="$3"
    if [[ "$requested" != "$actual" ]]; then
        port_notes+=("$name: $requested occupied, using $actual")
    fi
}

pid_owns_port() {
    local pid="$1"
    local port="$2"
    local owner_pid
    while IFS= read -r owner_pid; do
        [[ "$owner_pid" == "$pid" ]] && return 0
    done < <(lsof -nP -t -iTCP:"$port" -sTCP:LISTEN 2>/dev/null || true)
    return 1
}

is_running() {
    local name="$1"
    local pid_file="$pid_dir/$name.pid"
    local port_file="$pid_dir/$name.port"
    local pid
    local port

    if [[ ! -f "$pid_file" || ! -f "$port_file" ]]; then
        rm -f "$pid_file" "$port_file"
        return 1
    fi

    pid="$(cat "$pid_file")"
    port="$(cat "$port_file")"
    if [[ "$pid" =~ ^[0-9]+$ ]] && [[ "$port" =~ ^[0-9]+$ ]] && kill -0 "$pid" >/dev/null 2>&1 && pid_owns_port "$pid" "$port"; then
        return 0
    fi

    rm -f "$pid_file" "$port_file"
    return 1
}

select_port() {
    local name="$1"
    local requested="$2"
    shift 2
    local port_file="$pid_dir/$name.port"
    if is_running "$name" && [[ -f "$port_file" ]]; then
        cat "$port_file"
        return 0
    fi
    next_free_port_from "$requested" "$@"
}

start_process() {
    local name="$1"
    local port="$2"
    local cwd="$3"
    shift 3
    local log_file="$log_dir/$name.log"
    local pid_file="$pid_dir/$name.pid"
    local port_file="$pid_dir/$name.port"

    if is_running "$name"; then
        echo "$name already running with pid $(cat "$pid_file"); log: $log_file"
        return 0
    fi

    (
        cd "$cwd"
        nohup "$@" >"$repo_root/$log_file" 2>&1 &
        echo $! >"$repo_root/$pid_file"
        echo "$port" >"$repo_root/$port_file"
    )
    echo "started $name pid $(cat "$pid_file"); log: $log_file"
}

wait_for_http() {
    local name="$1"
    local url="$2"
    local timeout_seconds="${3:-180}"
    local started_at
    started_at="$(date +%s)"

    while true; do
        if curl -sS --connect-timeout 2 --max-time 5 -o /dev/null "$url" >/dev/null 2>&1; then
            echo "$name is reachable: $url"
            return 0
        fi
        if (( $(date +%s) - started_at >= timeout_seconds )); then
            echo "WARN: timed out waiting for $name: $url" >&2
            echo "      inspect log: $repo_root/$log_dir/$name.log" >&2
            return 1
        fi
        sleep 2
    done
}

port_notes=()

workers_requested="$(port_from_url "${KUZHAMBU_AI_WORKER_BASE_URL:-http://127.0.0.1:8000}" 8000)"
admin_backend_requested="${KUZHAMBU_ADMIN_SERVER_PORT:-20010}"
portal_backend_requested="${KUZHAMBU_PORTAL_SERVER_PORT:-20020}"
admin_web_requested="${KUZHAMBU_ADMIN_WEB_PORT:-5173}"
portal_web_requested="${KUZHAMBU_PORTAL_WEB_PORT:-5174}"

workers_port="$workers_requested"
if has_target admin; then
    workers_port="$(select_port workers "$workers_requested")"
fi
admin_backend_port="$admin_backend_requested"
admin_web_port="$admin_web_requested"
portal_backend_port="$portal_backend_requested"
portal_web_port="$portal_web_requested"

if has_target admin; then
    admin_backend_port="$(select_port admin-starter "$admin_backend_requested" "$workers_port")"
    admin_web_port="$(select_port admin-web "$admin_web_requested" "$workers_port" "$admin_backend_port")"
fi
if has_target portal; then
    portal_backend_port="$(select_port portal-starter "$portal_backend_requested" "$workers_port" "$admin_backend_port" "$admin_web_port")"
    portal_web_port="$(select_port portal-web "$portal_web_requested" "$workers_port" "$admin_backend_port" "$admin_web_port" "$portal_backend_port")"
fi

if has_target admin; then
    note_port "workers" "$workers_requested" "$workers_port"
    note_port "admin backend" "$admin_backend_requested" "$admin_backend_port"
    note_port "admin web" "$admin_web_requested" "$admin_web_port"
fi
if has_target portal; then
    note_port "portal backend" "$portal_backend_requested" "$portal_backend_port"
    note_port "portal web" "$portal_web_requested" "$portal_web_port"
fi

export KUZHAMBU_ADMIN_SERVER_PORT="$admin_backend_port"
export KUZHAMBU_PORTAL_SERVER_PORT="$portal_backend_port"
export KUZHAMBU_AI_WORKER_BASE_URL="http://127.0.0.1:$workers_port"

if has_target admin; then
    start_process workers "$workers_port" kuzhambu-workers \
        bash -lc ".venv/bin/uvicorn kuzhambu_workers.main:app --host 0.0.0.0 --port '$workers_port'"

    start_process admin-starter "$admin_backend_port" kuzhambu-servers/starter/kuzhambu-admin-starter \
        mvn spring-boot:run

    start_process admin-web "$admin_web_port" kuzhambu-apps/admin-web \
        pnpm run dev -- --port "$admin_web_port"
fi

if has_target portal; then
    start_process portal-starter "$portal_backend_port" kuzhambu-servers/starter/kuzhambu-portal-starter \
        mvn spring-boot:run

    start_process portal-web "$portal_web_port" kuzhambu-apps/portal-web \
        pnpm run dev -- --port "$portal_web_port"
fi

admin_health_url="http://127.0.0.1:$admin_backend_port${KUZHAMBU_ADMIN_SERVER_CONTEXT_PATH:-/kuzhambu-admin-api}/actuator/health"
portal_health_url="http://127.0.0.1:$portal_backend_port${KUZHAMBU_PORTAL_SERVER_CONTEXT_PATH:-/kuzhambu-api}/actuator/health"
workers_health_url="http://127.0.0.1:$workers_port/internal/health"
admin_web_url="http://127.0.0.1:$admin_web_port/"
portal_web_url="http://127.0.0.1:$portal_web_port/"

wait_failures=0
if has_target admin; then
    wait_for_http workers "$workers_health_url" 120 || wait_failures=$((wait_failures + 1))
    wait_for_http admin-starter "$admin_health_url" 180 || wait_failures=$((wait_failures + 1))
    wait_for_http admin-web "$admin_web_url" 120 || wait_failures=$((wait_failures + 1))
fi
if has_target portal; then
    wait_for_http portal-starter "$portal_health_url" 180 || wait_failures=$((wait_failures + 1))
    wait_for_http portal-web "$portal_web_url" 120 || wait_failures=$((wait_failures + 1))
fi

echo
if [[ "$wait_failures" -eq 0 ]]; then
    echo "Kuzhambu local test environment started."
else
    echo "Kuzhambu local test environment launched with $wait_failures readiness warning(s)."
fi
echo "Targets: ${targets[*]}"
echo "Logs: $repo_root/$log_dir"
echo

if [[ ${#port_notes[@]} -gt 0 ]]; then
    echo "Port changes:"
    printf '  - %s\n' "${port_notes[@]}"
    echo
fi

if has_target admin; then
    echo "Admin web: $admin_web_url"
    echo "Admin backend health: $admin_health_url"
    echo "Workers health: $workers_health_url"
fi

if has_target portal; then
    echo "Portal web: $portal_web_url"
    echo "Portal backend health: $portal_health_url"
fi

exit "$wait_failures"
