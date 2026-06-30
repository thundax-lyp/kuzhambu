# Workers Implementation Coverage

## Purpose

本文档记录 Worker 能力与 Java/AI usecase 对接状态，用于核对 worker 能力覆盖面与接入完整性。

本清单只回答“worker 侧路由与 usecase 是否已注册并可被调用”，不直接代表对应 Java 业务域已经把该能力接入到最终页面或业务闭环。

## Status Definition

- `已完成`：对应调用路径与 worker usecase 已在代码与路由层确认接通。
- `未完成`：worker 能力存在但调用链、路由、权限或注册尚未闭环。
- `外部依赖`：能力边界不属于本域，或由其他模块完成。

## Current Baseline

已完成：

- Classics、Discovery、Knowledge、Platform 的 usecase 已完成 worker 注册与调用链可追溯确认。
- 关键入口 `workerPath/operation` 已与 `kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke` 对齐。
- `Workers Implementation Coverage` 与各业务域 coverage 的口径不同：此处的 `已完成` 表示 worker 侧服务路径和 registry 已就绪；Java 侧是否已消费仍以 `AI / Classics / Knowledge / Discovery` 各自的 coverage 为准。
- Workers 已补齐统一最终态协议：同步响应与 SSE `completed/error` 都输出 `failureStage / fallbackUsed / artifactReference`。
- Workers 已补齐 `GET /internal/artifacts/{artifactId}` 临时产物下载入口，并提供超过 `12` 小时 artifact 的后台清理任务。

## Requirement Coverage Matrix

| domain | contentType | capability | operation | workerPath | stream | workerEntry | status | note |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| classics | SANCAI_ENTRY | translate | CLASSICS_SANCAI_TRANSLATE | /internal/ai/classics/sancai/translate | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | 已完成 | 路由与 graph 注册器协同提供 usecase 调用 |
| classics | SANCAI_ENTRY | translate | CLASSICS_SANCAI_TRANSLATE_BATCH_ITEM | /internal/ai/classics/sancai/translate-batch-item | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | 已完成 | 路由与 graph 注册器协同提供 usecase 调用 |
| classics | SANCAI_ENTRY | summary | CLASSICS_SANCAI_SUMMARY | /internal/ai/classics/sancai/summary | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | 已完成 | 路由与 graph 注册器协同提供 usecase 调用 |
| classics | SANCAI_ENTRY | tags | CLASSICS_SANCAI_TAGS | /internal/ai/classics/sancai/tags | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | 已完成 | 路由与 graph 注册器协同提供 usecase 调用 |
| classics | SANCAI_ENTRY | qa | CLASSICS_SANCAI_QA | /internal/ai/classics/sancai/qa | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | 已完成 | 路由与 graph 注册器协同提供 usecase 调用 |
| classics | SANCAI_ENTRY | image_analysis | CLASSICS_SANCAI_IMAGE_ANALYSIS | /internal/ai/classics/sancai/image-analysis | true | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | 已完成 | 路由与 graph 注册器协同提供 usecase 调用 |
| classics | SANCAI_ENTRY | fusion | CLASSICS_SANCAI_FUSION | /internal/ai/classics/sancai/fusion | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | 已完成 | 路由与 graph 注册器协同提供 usecase 调用 |
| classics | SANCAI_ENTRY | visual | CLASSICS_SANCAI_VISUAL_DESCRIPTION | /internal/ai/classics/sancai/visual-description | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | 已完成 | 路由与 graph 注册器协同提供 usecase 调用 |
| classics | SANCAI_ENTRY | image_gen | CLASSICS_SANCAI_IMAGE_GEN | /internal/ai/classics/sancai/image-gen | true | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | 已完成 | 路由与 graph 注册器协同提供 usecase 调用 |
| classics | SANCAI_ENTRY | split | CLASSICS_SANCAI_SPLIT | /internal/ai/classics/sancai/split | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | 已完成 | 路由与 graph 注册器协同提供 usecase 调用 |
| classics | WANGQI_DOCUMENT | summary | CLASSICS_WANGQI_SUMMARY | /internal/ai/classics/wangqi/summary | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | 已完成 | 路由与 graph 注册器协同提供 usecase 调用 |
| classics | WANGQI_DOCUMENT | tags | CLASSICS_WANGQI_TAGS | /internal/ai/classics/wangqi/tags | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | 已完成 | 路由与 graph 注册器协同提供 usecase 调用 |
| classics | WANGQI_DOCUMENT | qa | CLASSICS_WANGQI_QA | /internal/ai/classics/wangqi/qa | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | 已完成 | 路由与 graph 注册器协同提供 usecase 调用 |
| classics | MING_CUSTOMS | summary | CLASSICS_MING_CUSTOMS_SUMMARY | /internal/ai/classics/ming-customs/summary | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | 已完成 | 路由与 graph 注册器协同提供 usecase 调用 |
| classics | MING_CUSTOMS | tags | CLASSICS_MING_CUSTOMS_TAGS | /internal/ai/classics/ming-customs/tags | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | 已完成 | 路由与 graph 注册器协同提供 usecase 调用 |
| classics | MING_CUSTOMS | qa | CLASSICS_MING_CUSTOMS_QA | /internal/ai/classics/ming-customs/qa | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | 已完成 | 路由与 graph 注册器协同提供 usecase 调用 |
| discovery | - | query_understanding | DISCOVERY_QUERY_UNDERSTANDING | /internal/ai/discovery/query-understanding | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | 已完成 | usecase 已在 registry 预期列表中注册 |
| discovery | - | query_understanding | DISCOVERY_QUERY_REWRITE | /internal/ai/discovery/query-rewrite | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | 已完成 | usecase 已在 registry 预期列表中注册 |
| discovery | - | answer_generation | DISCOVERY_ANSWER_GENERATION | /internal/ai/discovery/answer-generation | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | 已完成 | usecase 已在 registry 预期列表中注册 |
| discovery | - | answer_generation | DISCOVERY_ANSWER_GENERATION_STREAM | /internal/ai/discovery/answer-generation/stream | true | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | 已完成 | usecase 已在 registry 预期列表中注册 |
| knowledge | - | relation_extraction | KNOWLEDGE_RELATION_EXTRACTION | /internal/ai/knowledge/relation-extraction | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | 已完成 | usecase 已在 registry 预期列表中注册 |
| knowledge | - | knowledge_graph | KNOWLEDGE_GRAPH_EXTRACTION | /internal/ai/knowledge/graph-extraction | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | 已完成 | usecase 已在 registry 预期列表中注册 |
| knowledge | - | lineage_extraction | KNOWLEDGE_LINEAGE_EXTRACTION | /internal/ai/knowledge/lineage-extraction | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | 已完成 | usecase 已在 registry 预期列表中注册 |
| knowledge | - | tags | KNOWLEDGE_TAG_EXTRACTION | /internal/ai/knowledge/tag-extraction | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | 已完成 | usecase 已在 registry 预期列表中注册 |
| platform | - | prompt_suggestion | PLATFORM_PROMPT_SUGGESTION | /internal/ai/platform/prompt-suggestion | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | 已完成 | usecase 已在 registry 预期列表中注册 |
| platform | - | version_summary | PLATFORM_VERSION_SUMMARY | /internal/ai/platform/version-summary | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | 已完成 | usecase 已在 registry 预期列表中注册 |
