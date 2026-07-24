---
name: kuzhambu-run-local
description: "Kuzhambu project-local slash-command skill for starting the local test environment. Direct invocation only; requires at least one explicit target parameter: admin, portal, or both. Handles local env loading, port conflicts, backend/frontend proxy alignment, process startup, and URL reporting."
---

# Run Local

启动 Kuzhambu 本地测试环境，并在完成后输出可访问 URL。

## 调用方式

本 skill 只用于 slash command 直接调用，不定义自然语言触发词。

参数规则：

- 必须显式传入 `admin`、`portal` 中至少一个。
- 可以同时传入 `admin portal`。
- 未指定 env 文件时，默认使用仓库根目录 `dev.env`。

## 启动范围

- `admin`：启动 `kuzhambu-workers`、`kuzhambu-admin-starter`、`admin-web`。
- `portal`：启动 `kuzhambu-portal-starter`、`portal-web`。
- 同时传入 `admin portal` 时，workers 只启动一次，portal 不单独要求 workers。

## 工作流

1. 确认当前目录是 Kuzhambu 仓库根目录，且存在 `docs/AGENTS.md`、`kuzhambu-servers/`、`kuzhambu-apps/`。
2. 运行脚本：

   ```sh
   .codex/skills/kuzhambu-run-local/scripts/start-local-test-env.sh admin
   .codex/skills/kuzhambu-run-local/scripts/start-local-test-env.sh portal
   .codex/skills/kuzhambu-run-local/scripts/start-local-test-env.sh admin portal
   ```

3. 如果用户明确指定 env 文件，追加 `--env-file <path>`；否则不要追加，脚本会使用 `dev.env`。
4. 脚本会：
   - 读取 env 文件。
   - 检查默认端口是否已被占用。
   - 如有端口冲突，选择后续可用端口。
   - 后端端口变化时，通过 `KUZHAMBU_*_SERVER_PORT` 同步 Vite 代理目标，浏览器端 API base 继续使用相对路径。
   - 启动 Java starter 前，先按 Maven reactor 安装所选 starter 及其依赖模块。
   - 如果 portal web 端口变化，同步 `KUZHAMBU_PORTAL_WEB_BASE_URL`，保证 admin 生成的分享 URL 指向实际 portal 地址。
   - 如果 admin backend 端口变化，同步 operations health probe URL，避免探测旧端口。
   - 后台启动进程并写入 `.codex/local-test-env/logs/`、`.codex/local-test-env/pids/`。
   - 输出最终访问 URL 和健康检查 URL。

## 端口约定

- Workers 默认从 `KUZHAMBU_AI_WORKER_BASE_URL` 推导端口，缺省为 `8000`。
- Admin backend 默认 `KUZHAMBU_ADMIN_SERVER_PORT`，缺省为 `20010`。
- Portal backend 默认 `KUZHAMBU_PORTAL_SERVER_PORT`，缺省为 `20020`。
- Admin web 默认 `5173`。
- Portal web 默认 `5174`。

不要为了处理端口冲突修改 `vite.config.ts`；使用脚本设置的 `KUZHAMBU_*_SERVER_PORT` 环境变量即可。不要导出 `VITE_*_API_BASE_URL`，避免浏览器端绕过 Vite 代理。

脚本依赖 `lsof` 做端口占用和 PID 归属判断；如果环境缺少 `lsof`，应在启动前失败并提示安装，而不是继续误判端口状态。PID 复用必须允许 `pnpm`、`mvn` 等父进程的子进程持有监听端口。

## 完成输出

完成后向用户报告：

- 启动了哪些 target。
- 每个服务的 URL。
- 后端健康检查 URL。
- 日志路径。
- 如更换过端口，明确说明原端口被占用以及最终端口。
