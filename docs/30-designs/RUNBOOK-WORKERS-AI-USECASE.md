# RUNBOOK Workers AI Usecase

## Purpose

本文档定义 workers AI usecase 接口落地手册，目标是把真实业务 AI 调用从通用调试接口收敛到稳定 usecase path，并保持 workers 内部复用现有 LangGraph、model adapter、SSE 和安全基础能力。

本 RUNBOOK 是临时执行手册，任务关闭时应删除。

## Scope

覆盖：

- AI usecase 接口文档、设计文档和 OpenAPI 描述。
- usecase path 到 capability、stream、output 的注册表。
- FastAPI usecase routes。
- HMAC path allowlist。
- usecase path 与 request capability/options 的一致性校验。
- OpenAPI 和路由级测试。

不覆盖：

- Java AI 域 `WorkerAiClient` 接入 usecase path。
- 业务域调用 AI 域的 application 接口调整。
- 真实模型供应商调用实现。
- 候选结果、调用记录、提示词或模型配置持久化。
- 数据库、Redis、MQ 或任务队列。

## Stable Inputs

- Usecase 接口：[`WORKERS-AI-USECASE-INTERFACE.md`](../20-interfaces/WORKERS-AI-USECASE-INTERFACE.md)
- 通用 AI 协议：[`WORKERS-AI-INTERFACE.md`](../20-interfaces/WORKERS-AI-INTERFACE.md)
- Workers 需求：[`WORKERS-REQUIREMENTS.md`](../10-requirements/WORKERS-REQUIREMENTS.md)
- AI 需求：[`AI-REQUIREMENTS.md`](../10-requirements/AI-REQUIREMENTS.md)
- Workers 设计：[`WORKERS-DESIGN.md`](./WORKERS-DESIGN.md)

## Global Rules

- 每个执行单元关联文件控制在 2-5 个。
- 每完成一个 TODO 必须删除或收窄对应 TODO，并小步提交。
- Workers 不连接数据库、Redis、MQ。
- Usecase routes 只允许 `kuzhambu-ai` 服务身份调用。
- Usecase routes 不读取提示词、模型配置或业务内容；请求体必须包含完整上下文。
- Usecase routes 必须经由 LangGraph graph registry。
- 通用 `/internal/ai/invoke` 和 `/internal/ai/stream` 保留为调试接口，但 OpenAPI 必须明确标注。

## Execution Units

### U1 Usecase Contract Sync

目标：同步稳定接口文档、workers 设计和 README 的 usecase 口径。

关联文件：

- `docs/20-interfaces/WORKERS-AI-USECASE-INTERFACE.md`
- `docs/20-interfaces/WORKERS-AI-INTERFACE.md`
- `docs/30-designs/WORKERS-DESIGN.md`
- `kuzhambu-workers/README.md`

验收：

- 文档列出 Classics、Discovery、Knowledge、Platform AI usecase path。
- 通用 AI 接口明确为调试、联调和协议验证用途。
- 真实业务必须使用 usecase path 的规则在文档中一致。

### U2 Usecase Registry

目标：实现 usecase path 到 capability、stream 和描述的注册表。

关联文件：

- `kuzhambu-workers/src/kuzhambu_workers/ai/usecase_registry.py`
- `kuzhambu-workers/tests/test_ai_usecase_registry.py`

验收：

- 注册表覆盖 `WORKERS-AI-USECASE-INTERFACE.md` 中所有 path。
- path、capability、stream、summary、description 可查询。
- 未知 path 返回稳定错误或空结果。

### U3 AI Route Shared Orchestration

目标：抽取通用 AI invoke/stream 路由与 usecase 路由共享的执行编排。

关联文件：

- `kuzhambu-workers/src/kuzhambu_workers/api/ai_routes.py`
- `kuzhambu-workers/tests/test_ai_routes.py`
- `kuzhambu-workers/tests/test_worker_e2e_ai.py`

验收：

- 通用调试接口行为不变。
- invoke 和 stream 的解析、HMAC、graph 调用和错误映射可被 usecase route 复用。
- 既有 AI 路由测试通过。

### U4 Classics AI Usecase Routes

目标：实现 Classics AI usecase routes。

关联文件：

- `kuzhambu-workers/src/kuzhambu_workers/api/ai_usecase_routes.py`
- `kuzhambu-workers/src/kuzhambu_workers/main.py`
- `kuzhambu-workers/tests/test_ai_usecase_routes_classics.py`

验收：

- 覆盖 `classics/sancai/*`、`classics/wangqi/*`、`classics/ming-customs/*` 路径。
- path 与 `capability` 不匹配时失败。
- stream-only 或 non-stream path 与 `options.stream` 不匹配时失败。
- HMAC path allowlist 生效。

### U5 Discovery AI Usecase Routes

目标：实现 Discovery AI usecase routes。

关联文件：

- `kuzhambu-workers/src/kuzhambu_workers/api/ai_usecase_routes.py`
- `kuzhambu-workers/tests/test_ai_usecase_routes_discovery.py`

验收：

- 覆盖查询理解、查询改写、回答生成和流式回答生成。
- 流式回答生成返回 SSE started/completed 或 error。
- 非流式回答生成返回同步 JSON。

### U6 Knowledge And Platform AI Usecase Routes

目标：实现 Knowledge 和 Platform AI usecase routes。

关联文件：

- `kuzhambu-workers/src/kuzhambu_workers/api/ai_usecase_routes.py`
- `kuzhambu-workers/tests/test_ai_usecase_routes_knowledge.py`
- `kuzhambu-workers/tests/test_ai_usecase_routes_platform.py`

验收：

- 覆盖实体关系抽取、图谱抽取、世系图抽取、标签候选抽取。
- 覆盖提示词优化建议和版本摘要。
- path 与 capability 校验覆盖成功和失败路径。

### U7 Security Allowlist

目标：把 AI usecase path 接入内部 HMAC path 授权。

关联文件：

- `kuzhambu-workers/src/kuzhambu_workers/core/security.py`
- `kuzhambu-workers/tests/test_security.py`
- `kuzhambu-workers/tests/test_worker_e2e_security.py`

验收：

- `kuzhambu-ai` 可访问所有 AI usecase path。
- Classics、Discovery、Knowledge、Operations 服务身份不能直接访问 AI usecase path。
- 错误签名、缺签名、path forbidden 均返回稳定错误。

### U8 OpenAPI Coverage

目标：让 Swagger/OpenAPI 明确展示 usecase path，并隐藏误导性业务入口。

关联文件：

- `kuzhambu-workers/src/kuzhambu_workers/api/ai_usecase_routes.py`
- `kuzhambu-workers/tests/test_openapi.py`

验收：

- `/internal/docs` 展示 AI usecase path。
- 通用 `/internal/ai/invoke` 和 `/internal/ai/stream` 标注调试用途。
- usecase path 标注业务用途、capability、stream 和输入快照边界。

### U9 End-To-End Verification

目标：补充 AI usecase 协议级端到端测试。

关联文件：

- `kuzhambu-workers/tests/test_worker_e2e_ai_usecase_classics.py`
- `kuzhambu-workers/tests/test_worker_e2e_ai_usecase_discovery.py`
- `kuzhambu-workers/tests/test_worker_e2e_ai_usecase_security.py`

验收：

- Classics 同步 usecase happy path 可跑。
- Discovery stream usecase happy path 可跑。
- 直接业务服务身份访问 AI usecase 被拒绝。
- path/capability mismatch 被拒绝。

### U10 Cleanup And PR Readiness

目标：清理执行现场并准备 PR。

关联文件：

- `TODO.md`
- `docs/30-designs/RUNBOOK-WORKERS-AI-USECASE.md`
- `kuzhambu-workers/README.md`
- `.github/pull_request_template.md`

验收：

- 已完成 TODO 删除或收窄。
- RUNBOOK 在 PR 收口前删除，除非仍有未完成执行价值。
- PR 描述记录验证命令和结果。
- 工作区无无关修改。

## Validation Commands

Workers 局部验证：

```sh
cd kuzhambu-workers
.venv/bin/ruff format --check .
.venv/bin/ruff check .
.venv/bin/pytest
```

单步窄验证：

```sh
cd kuzhambu-workers
.venv/bin/ruff format --check .
.venv/bin/ruff check .
.venv/bin/pytest tests/test_ai_usecase_registry.py
```

PR workflow 会在 workers 目录变更时显式执行：

```sh
ruff format --check .
ruff check .
pytest
```

## Exit Criteria

- AI usecase path 覆盖 AI、Classics、Discovery 和 Knowledge 需求中的 workers 执行用例。
- 通用 AI 调试接口保留但不再作为真实业务集成入口。
- HMAC path 授权覆盖所有 usecase。
- OpenAPI 展示 usecase path、用途和边界。
- workers 全量 ruff 和 pytest 通过。
- TODO 和 RUNBOOK 按规则收口。
