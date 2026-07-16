# Workers Implementation Coverage

## Status

- 当前状态：已完成
- 覆盖范围：AI usecase 路由、graph registry、OpenAI-compatible 调用、SSE final-state、临时 artifact、render worker。
- 真相源：`docs/10-requirements/WORKERS-REQUIREMENTS.md`、`docs/20-interfaces/WORKERS-AI-INTERFACE.md`、`docs/20-interfaces/WORKERS-RENDER-INTERFACE.md`、本文件。

## Scope Boundary

本文件只判断 worker 侧路由、registry、协议和测试是否完成。Java 业务域是否消费对应能力，以 AI / Classics / Knowledge / Discovery 各自 coverage 为准。

## Completion Summary

- Classics、Discovery、Knowledge、Platform 的 AI usecase 已注册并可通过 `GraphRegistry.invoke` 调用。
- Workers AI graph 已切换为真实 OpenAI-compatible `/chat/completions` 调用。
- 同步响应和 SSE `completed/error` 已统一输出 `failureStage`、`fallbackUsed`、`artifactReference`。
- 结构化 JSON 输出、Knowledge payload 归一、provider usage、`latencyMs` 和模型错误归一化已完成。
- 临时 artifact 下载入口 `GET /internal/artifacts/{artifactId}` 已完成，并提供 12 小时清理策略。
- `CLASSICS_SANCAI_IMAGE_GEN` 已支持图片生成、artifact 写入、SSE final-state 和内部下载校验。
- Classics export render 已覆盖 Wangqi / Ming Customs 的 CSV、JSON、HTML、ZIP 快照输出。
- 三才图会展示已改由 portal 公开接口在线渲染，workers 不再提供 sancai showcase 静态包生成能力。

## Open Items

- 无当前 worker 阻塞项。
- 新增 Java 业务 usecase 时必须同步补 Workers registry、路由测试和对应业务域 coverage。

## Validation Evidence

- 2026-07-09：Workers 全量 `ruff format --check`、`ruff check`、`pytest -p no:capture` 通过，242 tests passed。
- 2026-07-09：图片生成定向测试 40 tests 全绿，覆盖 artifact、SSE final-state、下载校验和失败分类。

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
