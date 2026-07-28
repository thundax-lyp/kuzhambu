---
name: kuzhambu-run-local
description: "Kuzhambu project-local slash-command workflow for starting the local test environment. Direct invocation only; requires at least one explicit target parameter: admin, portal, or both. Guides the agent through env loading, port conflict handling, backend/frontend proxy alignment, service startup, readiness checks, and URL reporting without using a bundled launcher script."
---

# Kuzhambu Run Local

启动 Kuzhambu 本地测试环境，并在完成后输出可访问 URL。

## 调用方式

本 skill 只用于 slash command 直接调用，不定义自然语言触发词。

参数规则：

- 必须显式传入 `admin`、`portal` 中至少一个。
- 可以同时传入 `admin portal`。
- 未指定 env 文件时，默认使用仓库根目录 `dev.env`；该文件必须已从 `.env.example` 创建并保持未跟踪。

## 启动范围

- `admin`：启动 `kuzhambu-workers`、`kuzhambu-admin-starter`、`admin-web`。
- `portal`：启动 `kuzhambu-portal-starter`、`portal-web`。
- 同时传入 `admin portal` 时，workers 只启动一次，portal 不单独要求 workers。

## 必读上下文

1. Read root `AGENTS.md` for local starter commands and repository-level rules.
2. Read `docs/AGENTS.md` for document routing.
3. Inspect these runtime files before choosing commands:
   - `dev.env` or the env file explicitly provided by the user. If no custom env file is provided and repo-root `dev.env` is absent, stop and tell the user to create it from `.env.example` before continuing.
   - `kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/resources/application.yml` when `admin` is requested.
   - `kuzhambu-servers/starter/kuzhambu-portal-starter/src/main/resources/application.yml` when `portal` is requested.
   - `kuzhambu-apps/admin-web/vite.config.ts` and `kuzhambu-apps/admin-web/src/api/http.ts` when `admin` is requested.
   - `kuzhambu-apps/portal-web/vite.config.ts` and `kuzhambu-apps/portal-web/src/api/http.ts` when `portal` is requested.

## Workflow

### 1. Validate Parameters

- Accept only explicit `admin` and/or `portal` targets.
- If neither target is present, stop and ask for at least one target.
- If the user provides an env file, use it.
- If no custom env file is provided, require repo-root `dev.env`.
- If the selected env file is absent or unreadable, stop before any startup command. For the default path, tell the user to create it from `.env.example` and keep it untracked; for a custom path, ask for a usable env file path.

### 2. Load Environment

Load env values in every shell session that launches services.

After the selected env file exists and is readable, validate whether it is shell-sourceable in a throwaway subshell:

```sh
(set -a && source dev.env && set +a)
```

If that succeeds, use the repository's normal local-run pattern:

```sh
set -a
source dev.env
set +a
```

If that fails, or if the env file contains unquoted values with spaces such as cron expressions, do not shell-evaluate it. Launch each service through a dotenv-safe inline parser instead:

```sh
python3 - dev.env KEY=value OTHER_KEY= -- command arg... <<'PY'
import os
import sys

env_file = sys.argv[1]
separator = sys.argv.index("--")
overrides = sys.argv[2:separator]
command = sys.argv[separator + 1 :]

with open(env_file, encoding="utf-8") as handle:
    for raw_line in handle:
        line = raw_line.strip()
        if not line or line.startswith("#"):
            continue
        if line.startswith("export "):
            line = line[len("export ") :].strip()
        if "=" not in line:
            continue
        key, value = line.split("=", 1)
        key = key.strip()
        value = value.strip()
        if len(value) >= 2 and value[0] == value[-1] and value[0] in {"'", '"'}:
            value = value[1:-1]
        os.environ[key] = value

for override in overrides:
    key, value = override.split("=", 1)
    os.environ[key] = value

os.execvp(command[0], command)
PY
```

If a custom env file is used, substitute its path for `dev.env`. Put selected port overrides in the `KEY=value` section before `--`.

Do not print secrets from the env file.

### 3. Choose Ports

Use the repo defaults unless occupied:

- Workers: derive from `KUZHAMBU_AI_WORKER_BASE_URL`, default `8000`.
- Admin backend: `KUZHAMBU_ADMIN_SERVER_PORT`, default `20010`.
- Portal backend: `KUZHAMBU_PORTAL_SERVER_PORT`, default `20020`.
- Admin web: default `5173`.
- Portal web: default `5174`.

Check each requested port before launch, for example:

```sh
lsof -nP -iTCP:<port> -sTCP:LISTEN
```

If a requested port is occupied, choose the next available port for that service and record the mapping for the final report. Only reserve ports for targets being launched; for example, `portal` alone does not reserve workers or admin ports.

### 4. Prepare Java Starters

Before starting any Java starter, install the selected starter and its reactor dependencies from `kuzhambu-servers/`:

```sh
cd kuzhambu-servers
mvn -pl starter/kuzhambu-admin-starter -am -DskipTests install
mvn -pl starter/kuzhambu-portal-starter -am -DskipTests install
```

Run only the command(s) for the requested target(s). For `admin portal`, either run both commands or one combined reactor command that includes both starter modules.

### 5. Launch Services

Use separate long-running terminal sessions for each service. Keep the sessions open and report their names or log locations.

For `admin`:

- Start workers with the selected workers port, for example:

  ```sh
  cd kuzhambu-workers
  KUZHAMBU_WORKER_INTERNAL_SECRET="${KUZHAMBU_AI_WORKER_INTERNAL_SECRET}" \
  KUZHAMBU_WORKER_ALLOWED_SERVICES="${KUZHAMBU_AI_WORKER_SERVICE_NAME}" \
  .venv/bin/uvicorn kuzhambu_workers.main:app --host 0.0.0.0 --port <workers-port>
  ```

  The admin starter reads the AI worker auth values from `KUZHAMBU_AI_WORKER_*`,
  while `kuzhambu-workers` expects `KUZHAMBU_WORKER_*`. Preserve this mapping so
  local admin AI calls do not fail with worker authorization errors.

- Start admin backend from `kuzhambu-servers/starter/kuzhambu-admin-starter` with:
  - `KUZHAMBU_ADMIN_SERVER_PORT=<selected-admin-backend-port>`
  - `KUZHAMBU_AI_WORKER_BASE_URL=http://127.0.0.1:<selected-workers-port>`
  - `KUZHAMBU_OPERATIONS_HEALTH_PROBES_TARGETS_0_URL=http://127.0.0.1:<selected-admin-backend-port><admin-context-path>/actuator/health`
  - If `portal` is also requested, `KUZHAMBU_PORTAL_WEB_BASE_URL=http://127.0.0.1:<selected-portal-web-port>`

- Start admin web from `kuzhambu-apps/admin-web` with:
  - `KUZHAMBU_ADMIN_SERVER_PORT=<selected-admin-backend-port>`
  - `VITE_ADMIN_API_BASE_URL=` explicitly empty so app-local `.env` cannot force absolute browser API URLs.
  - `pnpm run dev -- --port <selected-admin-web-port>`

For `portal`:

- Start portal backend from `kuzhambu-servers/starter/kuzhambu-portal-starter` with:
  - `KUZHAMBU_PORTAL_SERVER_PORT=<selected-portal-backend-port>`

- Start portal web from `kuzhambu-apps/portal-web` with:
  - `KUZHAMBU_PORTAL_SERVER_PORT=<selected-portal-backend-port>`
  - `VITE_PORTAL_API_BASE_URL=` explicitly empty so app-local `.env` cannot force absolute browser API URLs.
  - `pnpm run dev -- --port <selected-portal-web-port>`

Do not modify `vite.config.ts` for local port retargeting. Vite proxy target should be retargeted through `KUZHAMBU_*_SERVER_PORT`, while browser API clients keep relative defaults such as `/kuzhambu-admin-api/api` and `/kuzhambu-api/api`.

### 6. Verify Readiness

Use HTTP status-aware checks. `curl` must fail on 4xx/5xx:

```sh
curl --fail http://127.0.0.1:<port>/<context-path>/actuator/health
curl --fail http://127.0.0.1:<web-port>/
curl --fail http://127.0.0.1:<workers-port>/internal/health
```

Only report a service as ready when its readiness URL returns a successful status. If a service is not ready, inspect the relevant terminal output or log before reporting.

## 端口约定

- Workers 默认从 `KUZHAMBU_AI_WORKER_BASE_URL` 推导端口，缺省为 `8000`。
- Admin backend 默认 `KUZHAMBU_ADMIN_SERVER_PORT`，缺省为 `20010`。
- Portal backend 默认 `KUZHAMBU_PORTAL_SERVER_PORT`，缺省为 `20020`。
- Admin web 默认 `5173`。
- Portal web 默认 `5174`。

## 完成输出

完成后向用户报告：

- 启动了哪些 target。
- 每个服务的 URL。
- 后端健康检查 URL。
- 日志路径。
- 如更换过端口，明确说明原端口被占用以及最终端口。
