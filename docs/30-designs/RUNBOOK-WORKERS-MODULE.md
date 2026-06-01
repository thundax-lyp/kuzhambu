# RUNBOOK Workers Module

## Purpose

本文档定义 `kuzhambu-workers/` 完整代码落地手册，覆盖 Python workers 工程骨架、内部安全、AI invoke/stream、render stream、SSE artifact 分片、Playwright/Chromium PDF 和验证收口。

执行顺序固定为：`scaffold -> core -> schemas -> api -> ai -> streaming artifact -> render -> playwright -> verification -> cleanup`。每个执行单元关联文件保持 2-5 个，提交保持小步闭环。

本 RUNBOOK 是临时执行手册，任务关闭时应删除。

## Scope

覆盖：

- FastAPI 应用入口和本地运行基线。
- Pydantic schema、稳定错误对象和响应模型。
- HMAC 内部服务认证、服务白名单和路径授权。
- `/internal/health` 和 `/internal/capabilities`。
- `/internal/ai/invoke` 和 `/internal/ai/stream`。
- LangGraph graph registry 和 canonical capability matrix。
- SSE 事件编码、AI delta、usage、error、completed。
- 请求生命周期内 artifact store 和 SSE artifact chunk。
- `/internal/render/classics-export`、`/internal/render/sancai-showcase`、`/internal/render/operations-report`。
- Playwright/Chromium print、Browser Pool 和 PDF render。
- pytest、ruff、README 和 PR workflow 同步。

不覆盖：

- Java AI 域 `WorkerAiClient` 新增或重构。
- Java Classics/Operations 真实调用 render workers。
- 前端页面。
- 数据库、Redis、MQ 或任务队列。
- 模型供应商真实账号和线上密钥配置。

## Stable Inputs

- 需求：[`WORKERS-REQUIREMENTS.md`](../10-requirements/WORKERS-REQUIREMENTS.md)
- AI 接口：[`WORKERS-AI-INTERFACE.md`](../20-interfaces/WORKERS-AI-INTERFACE.md)
- Render 接口：[`WORKERS-RENDER-INTERFACE.md`](../20-interfaces/WORKERS-RENDER-INTERFACE.md)
- 设计：[`WORKERS-DESIGN.md`](./WORKERS-DESIGN.md)
- Python 工程入口：[`kuzhambu-workers/README.md`](../../kuzhambu-workers/README.md)
- Python 工程配置：[`kuzhambu-workers/pyproject.toml`](../../kuzhambu-workers/pyproject.toml)

## Global Rules

- Workers 不连接数据库、Redis、MQ 或任务队列。
- Workers 不接收用户 access token，不做用户权限判断。
- Workers 不回调 Java servers 读取提示词、模型配置、候选结果或任务状态。
- 每次请求必须包含完整执行上下文。
- 大型 AI/render 产物只在当前 SSE 连接内用 `artifact` 事件分片传输。
- Artifact store 只保存请求生命周期内的临时产物，请求结束必须清理。
- Python lint 和 formatter 统一使用 `ruff`。
- PDF 使用 Playwright-managed Chromium，基线为 `playwright==1.59.0` 和 `mcr.microsoft.com/playwright/python:v1.59.0-jammy`。
- 每个 TODO 对应 2-5 个关联文件；超过范围必须继续拆分。

## Execution Units

### W1 Project Scaffold

目标：建立可导入、可启动、可测试的 Python 包骨架。

关联文件：

- `kuzhambu-workers/src/kuzhambu_workers/__init__.py`
- `kuzhambu-workers/src/kuzhambu_workers/main.py`
- `kuzhambu-workers/src/kuzhambu_workers/api/__init__.py`
- `kuzhambu-workers/tests/test_health.py`

验收：

- FastAPI app 可导入。
- pytest 能发现测试。
- `ruff format --check .` 和 `ruff check .` 在 workers 目录可运行。

### W2 Core Configuration And Logging

目标：实现环境变量配置、默认参数和脱敏日志基础。

关联文件：

- `kuzhambu-workers/src/kuzhambu_workers/core/config.py`
- `kuzhambu-workers/src/kuzhambu_workers/core/logging.py`
- `kuzhambu-workers/tests/test_config.py`
- `kuzhambu-workers/tests/test_logging.py`

验收：

- 默认值与 `WORKERS-DESIGN.md` 一致。
- 日志脱敏覆盖 key、token、signature、完整 prompt 和完整 payload。

### W3 Stable Errors

目标：实现稳定错误类型、错误响应和异常映射。

关联文件：

- `kuzhambu-workers/src/kuzhambu_workers/core/errors.py`
- `kuzhambu-workers/src/kuzhambu_workers/schemas/common.py`
- `kuzhambu-workers/tests/test_errors.py`

验收：

- 支持 protocol、timeout、unavailable、model、render、unsupported、internal 等错误类型。
- 错误响应不暴露密钥、完整 prompt、完整业务输入或临时路径。

### W4 Internal HMAC Security

目标：实现内部 HMAC 签名校验、服务白名单和路径授权。

关联文件：

- `kuzhambu-workers/src/kuzhambu_workers/core/security.py`
- `kuzhambu-workers/src/kuzhambu_workers/schemas/common.py`
- `kuzhambu-workers/tests/test_security.py`

验收：

- 签名输入和算法与接口文档一致。
- 覆盖成功、签名失败、时间偏差、服务名不允许、路径不允许、requestId/traceId 不一致。

### W5 Health And Capabilities API

目标：实现 `/internal/health` 和 `/internal/capabilities`。

关联文件：

- `kuzhambu-workers/src/kuzhambu_workers/api/health_routes.py`
- `kuzhambu-workers/src/kuzhambu_workers/main.py`
- `kuzhambu-workers/tests/test_health_routes.py`

验收：

- health 不检查 DB、Redis 或 MQ。
- capabilities 返回 canonical AI capability、render endpoints、PDF engine、Browser Pool 和 limits。

### W6 AI Schemas

目标：实现 AI invoke、response、usage、prompt、modelConfig、SSE event schema。

关联文件：

- `kuzhambu-workers/src/kuzhambu_workers/schemas/ai.py`
- `kuzhambu-workers/src/kuzhambu_workers/schemas/stream.py`
- `kuzhambu-workers/tests/test_ai_schemas.py`

验收：

- 请求响应字段与 `WORKERS-AI-INTERFACE.md` 对齐。
- `image_gen`、`fusion`、`version_summary` 在 capability enum 中存在。
- 未知 capability 被拒绝或映射为 `UNSUPPORTED_CAPABILITY`。

### W7 LangGraph Registry

目标：实现 canonical capability registry 和最小 LangGraph 执行包装。

关联文件：

- `kuzhambu-workers/src/kuzhambu_workers/ai/graph_registry.py`
- `kuzhambu-workers/src/kuzhambu_workers/ai/graphs/basic.py`
- `kuzhambu-workers/tests/test_graph_registry.py`

验收：

- canonical matrix 中所有 capability 可解析。
- 单 prompt 能力也通过 graph 执行入口包装。
- 未支持或未知能力返回稳定错误。

### W8 Model Adapter And Prompt Messages

目标：实现 LangChain message 组装、OpenAI-compatible adapter 占位和结构化输出约束。

关联文件：

- `kuzhambu-workers/src/kuzhambu_workers/ai/model_adapters.py`
- `kuzhambu-workers/src/kuzhambu_workers/ai/prompt_messages.py`
- `kuzhambu-workers/src/kuzhambu_workers/ai/structured_output.py`
- `kuzhambu-workers/tests/test_prompt_messages.py`

验收：

- workers 优先使用 `prompt.messages`。
- 不回调 AI 域读取模板或变量。
- 支持文本、视觉、结构化输出和流式文本 adapter 边界。

### W9 SSE Encoding

目标：实现稳定 SSE 事件编码和事件生成 helper。

关联文件：

- `kuzhambu-workers/src/kuzhambu_workers/streaming/events.py`
- `kuzhambu-workers/src/kuzhambu_workers/streaming/sse.py`
- `kuzhambu-workers/tests/test_sse.py`

验收：

- 支持 `started`、`delta`、`progress`、`artifact`、`usage`、`warning`、`error`、`completed`。
- 每个事件包含 `eventId`、`requestId`、`traceId`、`stage`、`timestamp`。
- JSON data 编码稳定。

### W10 AI Invoke And Stream Routes

目标：实现 `/internal/ai/invoke` 和 `/internal/ai/stream` 路由编排。

关联文件：

- `kuzhambu-workers/src/kuzhambu_workers/api/ai_routes.py`
- `kuzhambu-workers/src/kuzhambu_workers/main.py`
- `kuzhambu-workers/tests/test_ai_routes.py`

验收：

- invoke 返回同步 JSON。
- stream 返回 SSE，完成事件包含最终 result。
- 连接异常和 graph 异常映射为稳定错误事件或失败响应。

### W11 Artifact Store And Chunking

目标：实现请求生命周期内 artifact store 和 SSE 分片读取。

关联文件：

- `kuzhambu-workers/src/kuzhambu_workers/render/artifact_store.py`
- `kuzhambu-workers/src/kuzhambu_workers/streaming/events.py`
- `kuzhambu-workers/tests/test_artifact_store.py`

验收：

- 不提供跨请求下载接口。
- chunkIndex 连续、chunkSha256 和整体 sha256 可校验。
- 请求完成、失败或中断后清理产物。

### W12 Render Schemas

目标：实现 render 请求、响应、artifact 和 summary schema。

关联文件：

- `kuzhambu-workers/src/kuzhambu_workers/schemas/render.py`
- `kuzhambu-workers/tests/test_render_schemas.py`

验收：

- 支持 `CLASSICS_EXPORT`、`SANCAI_SHOWCASE`、`OPERATIONS_REPORT`。
- 支持 `CSV`、`JSON`、`HTML`、`ZIP`、`PDF`。
- 大型产物必须走 stream artifact chunk。

### W13 Classics Export Renderer

目标：实现 Classics 导出 CSV、JSON、HTML、ZIP 产物生成。

关联文件：

- `kuzhambu-workers/src/kuzhambu_workers/render/classics_export.py`
- `kuzhambu-workers/src/kuzhambu_workers/render/templates/classics_export.html`
- `kuzhambu-workers/tests/test_classics_export.py`

验收：

- 输入快照生成对应格式。
- 产物包含文件名、contentType、sizeBytes、sha256。
- HTML/ZIP 产物可通过 artifact chunk 输出。

### W14 Sancai Showcase Renderer

目标：实现三才图会静态展示页面渲染。

关联文件：

- `kuzhambu-workers/src/kuzhambu_workers/render/sancai_showcase.py`
- `kuzhambu-workers/src/kuzhambu_workers/render/templates/sancai_showcase.html`
- `kuzhambu-workers/tests/test_sancai_showcase.py`

验收：

- 支持数据集元信息、目录、条目正文、图片引用快照。
- HTML 支持离线打开和浏览器打印。
- 不回查 Storage 或业务数据库。

### W15 Browser Pool

目标：实现 Playwright/Chromium Browser Pool。

关联文件：

- `kuzhambu-workers/src/kuzhambu_workers/render/browser_pool.py`
- `kuzhambu-workers/tests/test_browser_pool.py`
- `kuzhambu-workers/README.md`

验收：

- `playwright==1.59.0`。
- 支持 browser pool size、max pages、page timeout。
- 超时和异常路径释放 page/context。
- README 记录 `python -m playwright install --with-deps chromium` 和 `install --list`。

### W16 Operations Report Renderer

目标：实现 Operations HTML/PDF 报表渲染。

关联文件：

- `kuzhambu-workers/src/kuzhambu_workers/render/operations_report.py`
- `kuzhambu-workers/src/kuzhambu_workers/render/templates/operations_report.html`
- `kuzhambu-workers/src/kuzhambu_workers/render/browser_pool.py`
- `kuzhambu-workers/tests/test_operations_report.py`

验收：

- HTML 报表可生成。
- PDF 使用 Browser Pool 和 Chromium print。
- PDF 产物通过 SSE artifact chunk 输出。

### W17 Render Routes

目标：实现 render 同步和 stream 路由。

关联文件：

- `kuzhambu-workers/src/kuzhambu_workers/api/render_routes.py`
- `kuzhambu-workers/src/kuzhambu_workers/main.py`
- `kuzhambu-workers/tests/test_render_routes.py`

验收：

- 同步接口支持小型 text/base64 产物。
- stream 接口支持 progress、artifact chunk 和 completed。
- HMAC、服务名和路径授权生效。

### W18 Packaging And Dev Commands

目标：补齐 Python package、ruff、pytest、运行命令和本地开发说明。

关联文件：

- `kuzhambu-workers/pyproject.toml`
- `kuzhambu-workers/README.md`
- `docs/40-readiness/PR-WORKFLOW.md`
- `.github/workflows/pr-verify.yml`

验收：

- PR workflow 显式包含 workers 的 `ruff format --check`、`ruff check`、`pytest`。
- README 给出本地安装、运行、测试和 Playwright 安装命令。
- 不使用 shell 脚本隐藏 PR 必过检查。

### W19 End-To-End Worker Verification

目标：补充 workers 协议级端到端测试。

关联文件：

- `kuzhambu-workers/tests/test_worker_e2e_ai.py`
- `kuzhambu-workers/tests/test_worker_e2e_render.py`
- `kuzhambu-workers/tests/test_worker_e2e_security.py`

验收：

- AI invoke/stream happy path 可跑。
- render stream artifact chunk 可组装并校验 sha256。
- 未签名、错误签名、越权服务和 stream 中断路径被覆盖。

### W20 Cleanup And PR Readiness

目标：清理临时执行痕迹并完成 PR 准备。

关联文件：

- `TODO.md`
- `docs/30-designs/RUNBOOK-WORKERS-MODULE.md`
- `kuzhambu-workers/README.md`
- `.github/workflows/pr-verify.yml`

验收：

- 已完成 TODO 删除或收窄。
- RUNBOOK 在 PR 收口前删除，除非仍有未完成执行价值。
- PR 描述包含验证命令和结果。
- 工作区无无关修改。

## Validation Commands

Workers 局部验证：

```sh
cd kuzhambu-workers
ruff format --check .
ruff check .
pytest
```

Playwright 环境验证：

```sh
cd kuzhambu-workers
python -m playwright install --list
```

后端回归验证：

```sh
cd kuzhambu-servers
mvn -q clean spotless:check checkstyle:check test
```

PR 必过验证应由 `.github/workflows/pr-verify.yml` 显式列出，不得只代理到 shell 脚本。

## Commit Plan

建议提交顺序：

1. `Feat(workers): 初始化工程骨架`
2. `Feat(workers): 实现内部安全和基础接口`
3. `Feat(workers): 实现AI执行协议`
4. `Feat(workers): 实现SSE和产物分片`
5. `Feat(workers): 实现render基础能力`
6. `Feat(workers): 接入Playwright PDF渲染`
7. `Test(workers): 补充协议级验证`
8. `Chore(workers): 接入PR验证`
9. `Chore(workers): 清理执行现场`

每个提交必须对应已完成 TODO 的删除或收窄。不得把未完成的 TODO 标记为完成后保留。

## Exit Criteria

- Workers 可以无 DB、Redis、MQ 启动。
- `health` 和 `capabilities` 可返回稳定响应。
- AI invoke/stream 支持 canonical capability matrix。
- Render stream 支持 artifact chunk。
- PDF 使用 Playwright/Chromium Browser Pool。
- ruff 和 pytest 通过。
- PR workflow 显式验证 workers。
- TODO 已清理到真实剩余任务。
- RUNBOOK 已在收口提交中删除，或明确仍未收口。
