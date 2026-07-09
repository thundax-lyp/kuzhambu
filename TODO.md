# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `02 kuzhambu-workers OpenAI-compatible sync client`：实现同步 `/chat/completions` 调用
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-WORKERS-REAL-AI-LOOP.md`
    - 范围对象：`kuzhambu-workers/src/kuzhambu_workers/ai/openai_compatible.py`、`kuzhambu-workers/src/kuzhambu_workers/ai/usage.py`、`kuzhambu-workers/src/kuzhambu_workers/ai/prompt_messages.py`、`kuzhambu-workers/tests/test_ai_openai_compatible.py`
    - 处理动作：用 `httpx` 实现 OpenAI-compatible 同步请求、响应解析和 usage 映射。
    - 验收点：请求 URL、headers、body、provider content、provider usage、timeout、429、4xx、5xx 和连接失败均有 mocked transport 测试覆盖。
    - 重要度：10/10

- [ ] `03 kuzhambu-workers structured output`：实现结构化输出约束和解析
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-WORKERS-REAL-AI-LOOP.md`
    - 范围对象：`kuzhambu-workers/src/kuzhambu_workers/ai/structured_output.py`、`kuzhambu-workers/src/kuzhambu_workers/ai/openai_compatible.py`、`kuzhambu-workers/src/kuzhambu_workers/schemas/ai.py`、`kuzhambu-workers/tests/test_ai_structured_output.py`、`kuzhambu-workers/tests/test_graph_registry.py`
    - 处理动作：固定 JSON 输出约束、JSON parse 和 Knowledge payload shape。
    - 验收点：结构化 capability 自动发送 JSON 约束，非法 JSON 和非法 Knowledge 字段返回稳定 `OUTPUT_FORMAT_FAILURE`，`GraphRegistry` 不再断言 placeholder。
    - 重要度：10/10

- [ ] `04 kuzhambu-workers LangGraph invoke`：接入真实同步 graph 执行
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-WORKERS-REAL-AI-LOOP.md`
    - 范围对象：`kuzhambu-workers/src/kuzhambu_workers/ai/graphs/basic.py`、`kuzhambu-workers/src/kuzhambu_workers/ai/graphs/text.py`、`kuzhambu-workers/src/kuzhambu_workers/api/ai_routes.py`、`kuzhambu-workers/tests/test_ai_routes.py`、`kuzhambu-workers/tests/test_ai_usecase_routes_knowledge.py`
    - 处理动作：移除 AI placeholder 生成路径并让同步 route/usecase 通过 LangGraph 调用真实模型。
    - 验收点：生产 AI 代码无 placeholder 命中，`/internal/ai/invoke` 返回真实 provider content，`usage.latencyMs` 非默认值，`image_gen` 返回稳定不支持错误。
    - 重要度：10/10

- [ ] `05 kuzhambu-workers AI SSE`：接入真实流式 graph 执行
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-WORKERS-REAL-AI-LOOP.md`
    - 范围对象：`kuzhambu-workers/src/kuzhambu_workers/ai/openai_compatible.py`、`kuzhambu-workers/src/kuzhambu_workers/api/ai_routes.py`、`kuzhambu-workers/src/kuzhambu_workers/streaming/events.py`、`kuzhambu-workers/tests/test_ai_streaming_model.py`、`kuzhambu-workers/tests/test_ai_usecase_routes_discovery.py`
    - 处理动作：把 provider stream chunk 转换为 workers SSE `delta`、`usage`、`completed` 或 `error` 事件。
    - 验收点：`/internal/ai/stream` 和 stream usecase 输出真实 `started`、`delta`、`usage`、`completed`，非法 chunk 只输出 `error` 且不输出 `completed`。
    - 重要度：10/10

- [ ] `06 kuzhambu-workers AI e2e`：补齐端到端和契约回归
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-WORKERS-REAL-AI-LOOP.md`
    - 范围对象：`kuzhambu-workers/tests/test_worker_e2e_ai.py`、`kuzhambu-workers/tests/test_worker_e2e_ai_usecase_classics.py`、`kuzhambu-workers/tests/test_worker_e2e_ai_usecase_discovery.py`、`kuzhambu-workers/tests/test_worker_e2e_ai_usecase_security.py`、`kuzhambu-workers/tests/test_openapi.py`
    - 处理动作：用 mocked provider 锁定 AI route、usecase route、SSE、security 和 OpenAPI 不回归。
    - 验收点：E2E 不依赖真实 API Key 或外网，HMAC、path allowlist 和 OpenAPI usecase path 测试通过。
    - 重要度：9/10

- [ ] `07 feat/workers-real-ai-loop`：同步 main 分支代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：当前分支 `feat/workers-real-ai-loop`
    - 处理动作：完成 AI 实现和 E2E 回归后，同步 `origin/main` 到当前功能分支并处理冲突。
    - 验收点：当前分支包含最新 `origin/main`，同步后的 workers 验证仍通过。
    - 重要度：9/10

- [ ] `08 kuzhambu-workers final validation`：运行同步 main 后的全量验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-WORKERS-REAL-AI-LOOP.md`
    - 范围对象：`kuzhambu-workers/`
    - 处理动作：在同步 `origin/main` 后执行 workers formatter、Ruff lint、pytest 和 placeholder/sensitive-field 复核命令。
    - 验收点：`ruff format --check`、`ruff check`、`pytest -p no:capture` 全部通过，placeholder 搜索无生产残留，敏感字段搜索只保留合法 redaction 断言。
    - 重要度：10/10

- [ ] `09 docs/40-readiness/WORKERS-IMPLEMENTATION-COVERAGE.md`：更新 Workers Implementation Coverage
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/40-readiness/WORKERS-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：记录 workers 真实 AI 执行闭环、验证命令和剩余缺口。
    - 验收点：Implementation Coverage 准确反映同步、SSE、结构化输出、错误归一化、usage/latency 的实现状态和验证结果。
    - 重要度：8/10

- [ ] `10 RUNBOOK-WORKERS-REAL-AI-LOOP`：清理临时 RUNBOOK 和 TODO 收口
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-WORKERS-REAL-AI-LOOP.md`、`TODO.md`
    - 处理动作：任务关闭时删除临时 RUNBOOK 并删除或收窄已完成 TODO。
    - 验收点：PR 收口前无已完成任务残留，临时 RUNBOOK 已删除或仅保留仍未关闭范围。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
