# Discovery Implementation Coverage

## Status

- 当前状态：已完成，存在外部 provider 验收风险。
- 覆盖范围：跨库搜索、查询理解、实体增强、QA 会话、王圻单文档问答、来源引用、会话删除、CSV 导出、Admin 运维入口和 FastGPT 诊断跳转。
- 真相源：`docs/10-requirements/DISCOVERY-REQUIREMENTS.md`、本文件。

## Completion Summary

- Search 已接入 Classics 三类内容源：`SANCAI_ENTRY`、`WANGQI_DOCUMENT`、`MING_CUSTOMS`。
- Elasticsearch 默认适配、索引文档映射、全量重建、增量同步、删除态清理和点击事件已完成。
- Portal 搜索已支持分组展示、筛选、关键词高亮、URL 状态恢复、结果深链、点击回写和无结果空态。
- Admin 搜索运维已支持检索统计事件、检索统计 summary、热门搜索词、失败次数、零结果次数、点击数和平均搜索耗时。
- 查询理解已接入 Knowledge 标签和实体提示，并通过 AI 执行 query understanding / rewrite。
- QA 已完成 Portal `chat/completions`、会话持久化、来源引用、知识同步、provider trace 落库和 Admin 运维页；来源与 provider trace 诊断查看转到 FastGPT 产品。
- 王圻单文档 QA 已完成上下文 URL、首问/追问上下文透传、后端上下文校验和 AI trace 展示。
- 会话删除和 CSV 导出已完成，导出产物写入 Storage。

## Open Items

- 无代码层阻塞项。
- dev FastGPT 环境当前无 embedding，`chat/completions` 返回 provider 400，QA 来源跳转冒烟未通过；该项属于外部 provider 验收风险，不改变当前代码闭环状态。
- Search 仅覆盖 Classics 三类内容源；后续扩展新内容源时需重新确认 `SearchScope`、索引字段和权限过滤。
- 增量同步采用 `afterCommit + RocketMQ`，不采用 outbox；数据库提交成功但 MQ 发送失败时依赖重试或 Admin rebuild 兜底。

## Validation Evidence

- Search / QA runtime、索引同步、搜索高亮、检索统计、Portal 搜索状态、王圻单文档问答上下文已完成阶段性验证。
- Playwright 冒烟证据以当前分支验证命令为准。
- Discovery QA dev 冒烟证据归档于 `/tmp/discovery-quality-qa-20260709153738/`。
- 2026-07-27：根据 2026-07-20 至 2026-07-24 提交记录完成代码反查；`ElasticsearchSearchIndexGateway` 确认索引写入、查询和预览均限制 PUBLIC 内容，`search-statistics-service.ts` 确认 Admin 搜索统计事件与 summary 接口，`qa-console-service.ts` 和 `qa-diagnostics-panel.tsx` 确认会话分页/删除/导出与 FastGPT 诊断跳转。

## Requirement Coverage Matrix

| 子域 | 需求范围 | 状态 | 说明 |
| --- | --- | --- | --- |
| Search | 跨库搜索 | 已完成 | 固定覆盖 Classics 三类内容源 |
| Search | 分组、排序、高亮、筛选 | 已完成 | Portal 展示和后端查询字段已对齐 |
| Search | 权限过滤 | 已完成 | Discovery 搜索索引只承载 PUBLIC 内容，非公开内容不进入检索读模型 |
| Search | 日志、点击、分析 | 已完成 | 检索统计事件、点击事件和 Admin summary 已接通 |
| Search | 增量同步 | 已完成 | afterCommit 发 MQ，Discovery 消费端按版本幂等更新 |
| Query | 查询理解、清洗、改写 | 已完成 | AI 调度和结果落库已接通 |
| Query | 标签和实体增强 | 已完成 | 消费 Knowledge 读协作服务 |
| QA | 多库问答和多轮会话 | 已完成 | Portal chat/completions、消息、来源和 trace 已落库 |
| QA | 王圻单文档问答 | 已完成 | URL 上下文、会话校验、首问和追问已闭环 |
| QA | 删除和导出 | 已完成 | Portal/Admin 删除、CSV 导出和 Storage 上传已闭环 |
| Admin | 运维和同步状态 | 已完成 | health、rebuild、sync page、session page/get/delete/export 和 FastGPT 诊断跳转已提供 |
| 验收 | 外部 provider 冒烟 | 部分通过 | 代码链路可用；FastGPT dev provider 当前返回 400 |
