# AI Implementation Coverage

## Purpose

本文档记录 AI 精修能力在 Java 与 Workers 的当前接入状态，用于跟踪后续闭环进度。

## Status Definition

- `已完成`：当前仓库形成可追溯交付，且可走通调用链路。
- `部分完成`：能力有约束或边界不匹配，当前未形成可执行闭环。
- `未完成`：能力的关键调用链或入口尚未在当前仓库里形成闭环。
- `外部依赖`：能力边界不属于当前域，或由其他域/系统承担。

## Current Baseline

已完成：

- Classics 精修入口已对接六类同步候选能力（`translate/summary/tags/qa/visual/split`）的 Java → Worker 调用链路（Sancai、Wangqi、Ming Customs）。
- AI 设计文档定义的默认精修任务协议（`task/add -> task/get/page -> task/cancel`）已经在代码中落地；当前实现覆盖任务受理、查询、分页、取消、12 小时超时清理与 `TASK_EXPIRED` 失败态收口。
- Knowledge 图谱抽取已对接三类候选能力（`relation_extraction/knowledge_graph/lineage_extraction`）的 Knowledge → AI → Worker 调用链路，并补齐批量任务、取消和重生成所需的 AI 协作台账。
- AI 域当前已具备候选结果台账读取、拒绝和“标记已应用”协作入口，可支持 Classics / Knowledge 在业务确认后回写 AI 候选状态。
- AI 域已形成统一最终态协议：调用结果、调用记录、候选结果和 Worker stream `completed/error` 统一使用 `failureStage / fallbackUsed / artifactReference` 口径；文件类结果由 Java 下载 Workers 临时产物并转存 `Storage`。
- Discovery 的 query understanding、answer generation 与 stream answer 已统一消费 AI 最终态，并把最终 AI `callId` 稳定挂到 QA trace。
- AI 域当前已形成“治理入口 -> Workers 执行 -> 候选结果/任务台账 -> Classics 页面消费”的闭环；三才图会视觉资产已接通 `image_analysis / fusion / visual / image_gen` 的单条任务、批量任务、候选治理、失败重试和正式版本写回。
- `task/stream` 仍停留在设计层的可选协议，未在当前代码中实现。

未完成：

- Classics 的 `translate-batch-item` usecase 仍只存在于 worker 侧，Java 精修 resolver 目前未消费。
- Knowledge `tag_extraction` 与 Platform 两类能力在 worker 表内存在，但当前 Java 侧尚未提供对应调用入口。

## 已完成

| domain | contentType | capability | javaEntry | operation | workerPath | status | note |
| --- | --- | --- | --- | --- | --- | --- | --- |
| classics | SANCAI_ENTRY | translate | AiRefinementController#translate | CLASSICS_SANCAI_TRANSLATE | /internal/ai/classics/sancai/translate | 已完成 | 六类同步候选型精修能力接入 usecase path |
| classics | SANCAI_ENTRY | summary | AiRefinementController#summarize | CLASSICS_SANCAI_SUMMARY | /internal/ai/classics/sancai/summary | 已完成 | 六类同步候选型精修能力接入 usecase path |
| classics | SANCAI_ENTRY | tags | AiRefinementController#generateTags | CLASSICS_SANCAI_TAGS | /internal/ai/classics/sancai/tags | 已完成 | 六类同步候选型精修能力接入 usecase path |
| classics | SANCAI_ENTRY | qa | AiRefinementController#generateQa | CLASSICS_SANCAI_QA | /internal/ai/classics/sancai/qa | 已完成 | 六类同步候选型精修能力接入 usecase path |
| classics | SANCAI_ENTRY | image_analysis | AiRefinementController#analyzeImage | CLASSICS_SANCAI_IMAGE_ANALYSIS | /internal/ai/classics/sancai/image-analysis | 已完成 | 已切到标准 classics usecase path 与 stream final-state 协议 |
| classics | SANCAI_ENTRY | fusion | AiRefinementController#fuseSancaiVisualAsset | CLASSICS_SANCAI_FUSION | /internal/ai/classics/sancai/fusion | 已完成 | 已接入三才视觉资产精修入口，支持候选治理与正式字段写回 |
| classics | SANCAI_ENTRY | visual | AiRefinementController#describeVisual | CLASSICS_SANCAI_VISUAL_DESCRIPTION | /internal/ai/classics/sancai/visual-description | 已完成 | 六类同步候选型精修能力接入 usecase path |
| classics | SANCAI_ENTRY | image_gen | AiRefinementController#generateSancaiImage | CLASSICS_SANCAI_IMAGE_GEN | /internal/ai/classics/sancai/image-gen | 已完成 | 已接入三才视觉资产精修入口，支持 artifact 转存、候选治理与新版本写回 |
| classics | SANCAI_ENTRY | split | AiRefinementController#splitEntry | CLASSICS_SANCAI_SPLIT | /internal/ai/classics/sancai/split | 已完成 | 六类同步候选型精修能力接入 usecase path |
| classics | WANGQI_DOCUMENT | summary | AiRefinementController#summarize | CLASSICS_WANGQI_SUMMARY | /internal/ai/classics/wangqi/summary | 已完成 | 六类同步候选型精修能力接入 usecase path |
| classics | WANGQI_DOCUMENT | tags | AiRefinementController#generateTags | CLASSICS_WANGQI_TAGS | /internal/ai/classics/wangqi/tags | 已完成 | 六类同步候选型精修能力接入 usecase path |
| classics | WANGQI_DOCUMENT | qa | AiRefinementController#generateQa | CLASSICS_WANGQI_QA | /internal/ai/classics/wangqi/qa | 已完成 | 六类同步候选型精修能力接入 usecase path |
| classics | MING_CUSTOMS | summary | AiRefinementController#summarize | CLASSICS_MING_CUSTOMS_SUMMARY | /internal/ai/classics/ming-customs/summary | 已完成 | 六类同步候选型精修能力接入 usecase path |
| classics | MING_CUSTOMS | tags | AiRefinementController#generateTags | CLASSICS_MING_CUSTOMS_TAGS | /internal/ai/classics/ming-customs/tags | 已完成 | 六类同步候选型精修能力接入 usecase path |
| classics | MING_CUSTOMS | qa | AiRefinementController#generateQa | CLASSICS_MING_CUSTOMS_QA | /internal/ai/classics/ming-customs/qa | 已完成 | 六类同步候选型精修能力接入 usecase path |
| classics | SANCAI_ENTRY / WANGQI_DOCUMENT / MING_CUSTOMS | task_management | AiRefinementTaskController#add/get/page/cancel | AI_REFINEMENT_TASK_MANAGEMENT | - | 已完成 | 已实现设计文档定义的默认任务协议；代码未提供 `task/stream` 前端订阅入口 |
| classics | SANCAI_ENTRY | batch_visual_processing | AiRefinementTaskController#createBatch/get/page/cancelBatch | AI_REFINEMENT_TASK_BATCH_MANAGEMENT | - | 已完成 | 三才图会已接通批量图片理解与视觉资产处理任务的创建、分页、取消和失败聚合 |
| discovery | - | query_understanding | DiscoveryAiApplicationService#understandQuery | DISCOVERY_QUERY_UNDERSTANDING | /internal/ai/discovery/query-understanding | 已完成 | 已统一消费 AI 最终态字段；失败时按最终错误口径落库 |
| discovery | - | query_understanding | DiscoveryAiApplicationService#rewriteQuery | DISCOVERY_QUERY_REWRITE | /internal/ai/discovery/query-rewrite | 已完成 | 已统一消费 AI 最终态字段；失败时按最终错误口径落库 |
| discovery | - | answer_generation | DiscoveryAiApplicationService#generateAnswer | DISCOVERY_ANSWER_GENERATION | /internal/ai/discovery/answer-generation | 已完成 | 最终回答消费 AI 最终态字段，并把 `callId` 挂到 QA trace |
| discovery | - | answer_generation | DiscoveryAiApplicationService#streamAnswer | DISCOVERY_ANSWER_GENERATION_STREAM | /internal/ai/discovery/answer-generation/stream | 已完成 | stream 最终态消费已收口，并把最终 `callId` 挂到 QA trace |
| knowledge | SANCAI_ENTRY | relation_extraction | KnowledgeGraphExtractionApplicationService#requestRelationExtraction | KNOWLEDGE_RELATION_EXTRACTION | /internal/ai/knowledge/relation-extraction | 已完成 | 已形成任务台账、AI 调用、候选应用、批量与重生成闭环 |
| knowledge | SANCAI_ENTRY | knowledge_graph | KnowledgeGraphExtractionApplicationService#requestGraphExtraction | KNOWLEDGE_GRAPH_EXTRACTION | /internal/ai/knowledge/graph-extraction | 已完成 | 已形成任务台账、AI 调用、候选应用、批量与重生成闭环 |
| knowledge | SANCAI_ENTRY | lineage_extraction | KnowledgeGraphExtractionApplicationService#requestLineageExtraction | KNOWLEDGE_LINEAGE_EXTRACTION | /internal/ai/knowledge/lineage-extraction | 已完成 | 已形成任务台账、AI 调用、候选应用、批量与重生成闭环 |

## 未完成

| domain | contentType | capability | javaEntry | operation | workerPath | status | note |
| --- | --- | --- | --- | --- | --- | --- | --- |
| classics | SANCAI_ENTRY | translate | - | CLASSICS_SANCAI_TRANSLATE_BATCH_ITEM | /internal/ai/classics/sancai/translate-batch-item | 未完成 | 对应 usecase 未在 Classics 精修 resolver 中配置 |
| knowledge | - | tags | - | KNOWLEDGE_TAG_EXTRACTION | /internal/ai/knowledge/tag-extraction | 未完成 | 当前 Java AI 精修未提供该域的调用入口 |
| platform | - | prompt_suggestion | - | PLATFORM_PROMPT_SUGGESTION | /internal/ai/platform/prompt-suggestion | 未完成 | 当前 Java AI 精修未提供该域的调用入口 |
| platform | - | version_summary | - | PLATFORM_VERSION_SUMMARY | /internal/ai/platform/version-summary | 未完成 | 当前 Java AI 精修未提供该域的调用入口 |
