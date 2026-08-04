# AI Implementation Coverage

## Status

- 当前状态：已完成
- 覆盖范围：AI 精修任务、候选结果、调用记录、管理端治理、Java -> Workers 调用、Workers artifact 转存 Storage。
- 真相源：`docs/10-requirements/AI-REQUIREMENTS.md`、`docs/20-interfaces/WORKERS-AI-INTERFACE.md`、本文件。

## Completion Summary

- AI 默认精修任务协议已落地：`task/add`、`task/get`、`task/page`、`task/cancel`、`task/stream`。
- 候选结果台账已支持读取、拒绝、标记已应用，供 Classics 和 Knowledge 在人工确认后回写状态。
- Java 与 Workers 已统一最终态字段：`failureStage`、`fallbackUsed`、`artifactReference`。
- 文件类 AI 结果已支持 Java 下载 Workers 临时 artifact 并转存 Storage。
- Classics 已接入文本精修、标签、问答、三才视觉资产、批量视觉处理和流式过程展示。
- Knowledge 已接入标签抽取、关系抽取、知识图谱抽取和世系抽取。
- Discovery 已接入查询理解、查询改写、回答生成和流式回答，并在 QA trace 展示 AI 调用状态。
- Admin Web 已完成 5 个 AI 治理页面：服务配置、模型配置、提示词、业务配置、调用统计。
- Java AI 调用入口已改为按业务配置解析提示词模板、当前版本和模型配置，再调用 Workers 统一 AI 执行入口。

## Open Items

- 无阻塞项。
- 后续如新增 AI capability，必须同步补齐 AI 业务配置、提示词、模型配置、Java 调用入口、候选应用方和 readiness 矩阵；workers 只有在 capability 或统一协议变化时才需要更新。

## Validation Evidence

- 2026-07-09：`node scripts/seed/generate-system-sql.mjs --check` 通过。
- 2026-07-09：`cd kuzhambu-servers && mvn spotless:check checkstyle:check test` 通过。
- 2026-07-09：`cd kuzhambu-apps && pnpm run format:check && pnpm run lint && pnpm run build && pnpm run test` 通过。
- 2026-07-09：`cd kuzhambu-workers && .venv/bin/python -m ruff format --check . && .venv/bin/python -m ruff check . && .venv/bin/python -m pytest -p no:capture` 通过。
- 2026-07-27：根据 2026-07-20 至 2026-07-24 提交记录完成代码反查；`kuzhambu-apps/admin-web/src/router/index.tsx` 和 `kuzhambu-apps/admin-web/src/pages/ai/business-configs/business-configs-page.tsx` 确认业务配置页面已接入，`AiBusinessInvokeConfigResolver` 确认运行时按业务配置渲染提示词并解析模型。
- 运行时证据：`docs/40-readiness/AI-RUNTIME-SMOKE-EVIDENCE.md`。

## Admin Management Matrix

| 能力 | 路由 | 状态 |
| --- | --- | --- |
| 服务配置 | `/ai/services` | 已完成 |
| 模型配置 | `/ai/models` | 已完成 |
| 提示词 | `/ai/prompts` | 已完成 |
| 业务配置 | `/ai/business-configs` | 已完成 |
| 调用统计 | `/ai/invocations` | 已完成 |

## Runtime Capability Matrix

| 调用方 | 能力范围 | 状态 | 说明 |
| --- | --- | --- | --- |
| Classics | 三才、王圻、明代习俗文本精修 | 已完成 | `translate / summary / tags / qa / split` 等同步候选能力已接通 |
| Classics | 三才视觉资产 | 已完成 | `image_analysis / fusion / visual / image_gen` 单条、批量、流式和候选应用闭环已完成 |
| Knowledge | 标签与图谱抽取 | 已完成 | `tag_extraction / relation_extraction / knowledge_graph / lineage_extraction` 已接通 |
| Discovery | 查询理解与问答 | 已完成 | query understanding、rewrite、answer generation、stream answer 已消费 AI final-state |
| Platform | Prompt 建议与版本摘要 | 已完成 | 已接入 Java Platform AI 入口 |
