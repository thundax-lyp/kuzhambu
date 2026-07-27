# Workers Implementation Coverage

## Status

- 当前状态：已完成
- 覆盖范围：统一 AI 执行路由、graph registry、OpenAI-compatible 调用、SSE final-state、临时 artifact、render worker。
- 真相源：`docs/10-requirements/WORKERS-REQUIREMENTS.md`、`docs/20-interfaces/WORKERS-AI-INTERFACE.md`、`docs/20-interfaces/WORKERS-RENDER-INTERFACE.md`、本文件。

## Scope Boundary

本文件只判断 worker 侧路由、registry、协议和测试是否完成。Java 业务域是否消费对应能力，以 AI / Classics / Knowledge / Discovery 各自 coverage 为准。

## Completion Summary

- Classics、Discovery、Knowledge、Platform 的 AI capability 已注册并可通过统一 AI 执行路由调用。
- Workers 仅承载无状态 AI 执行；业务类型识别、业务配置选择、提示词渲染、模型配置组装、权限、审计和任务台账处理由 Java AI 域在调用前完成。
- Workers AI graph 已切换为真实 OpenAI-compatible `/chat/completions` 调用。
- 同步响应和 SSE `completed/error` 已统一输出 `failureStage`、`fallbackUsed`、`artifactReference`。
- 结构化 JSON 输出、Knowledge payload 归一、provider usage、`latencyMs` 和模型错误归一化已完成。
- 临时 artifact 下载入口 `GET /internal/artifacts/{artifactId}` 已完成，并提供 12 小时清理策略。
- `CLASSICS_SANCAI_IMAGE_GEN` 已支持图片生成、artifact 写入、SSE final-state 和内部下载校验。
- Classics export render 已覆盖 Wangqi / Ming Customs 的 CSV、JSON、HTML、ZIP 快照输出。
- 三才图会展示已改由 portal 公开接口在线渲染，workers 不再提供 sancai showcase 静态包生成能力。

## Open Items

- 无当前 worker 阻塞项。
- 新增 Java 业务 AI 能力时必须同步补 AI 业务配置、提示词、模型配置、Java 调用入口、候选应用方和 readiness 矩阵；workers 只有在 capability 或统一协议变化时才需要更新。

## Validation Evidence

- 2026-07-09：Workers 全量 `ruff format --check`、`ruff check`、`pytest -p no:capture` 通过，242 tests passed。
- 2026-07-09：图片生成定向测试 40 tests 全绿，覆盖 artifact、SSE final-state、下载校验和失败分类。
- 2026-07-27：根据 2026-07-20 至 2026-07-24 提交记录完成代码反查；`kuzhambu_workers/api/ai_routes.py` 仅暴露 `/internal/ai/invoke` 和 `/internal/ai/stream`，`kuzhambu_workers/core/security.py` 仅授权 `kuzhambu-ai` 访问统一 AI 路径，`tests/test_workers_architecture.py` 断言旧业务 usecase 路径不得恢复。

## Requirement Coverage Matrix

| 域 | 能力范围 | 状态 | 说明 |
| --- | --- | --- | --- |
| Classics | 三才文本精修 | 已完成 | translate、summary、tags、qa、split 已注册 |
| Classics | 三才视觉资产 | 已完成 | image_analysis、fusion、visual、image_gen 已注册 |
| Classics | Wangqi / Ming Customs 精修 | 已完成 | summary、tags、qa 已注册 |
| Classics | export render | 已完成 | Wangqi、Ming Customs 导出渲染已覆盖 |
| Classics | Sancai portal display | 已调整 | portal 在线展示读取公开接口，workers 仅保留导出渲染 |
| Discovery | 查询理解和问答 | 已完成 | query-understanding、query-rewrite、answer-generation、stream 已注册 |
| Knowledge | 图谱抽取 | 已完成 | relation、graph、lineage、tag extraction 已注册 |
| Platform | Prompt 和版本摘要 | 已完成 | prompt suggestion、version summary 已注册 |
