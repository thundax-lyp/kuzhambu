# AI Implementation Coverage

## Purpose

本文档记录 AI 精修与 Java/Worker 能力的交付状态，用于跟踪 usecase 闭环和下一步补齐项。

## Status Definition

- `implemented`：已在仓库内形成可追溯交付，且当前可走通完整调用路径。
- `excluded`：本轮明确排除或有明确边界约束，不纳入本轮交付。
- `not_implemented`：能力存在能力定义或 worker 能力，但 Java 调用入口未接入。

## 已实现并已接入 usecase path

| domain | contentType | capability | javaEntry | operation | workerPath | status | note |
| --- | --- | --- | --- | --- | --- | --- | --- |
| classics | SANCAI_ENTRY | translate | AiRefinementController#translate | CLASSICS_SANCAI_TRANSLATE | /internal/ai/classics/sancai/translate | implemented | 六类同步候选型精修能力接入 usecase path |
| classics | SANCAI_ENTRY | summary | AiRefinementController#summarize | CLASSICS_SANCAI_SUMMARY | /internal/ai/classics/sancai/summary | implemented | 六类同步候选型精修能力接入 usecase path |
| classics | SANCAI_ENTRY | tags | AiRefinementController#generateTags | CLASSICS_SANCAI_TAGS | /internal/ai/classics/sancai/tags | implemented | 六类同步候选型精修能力接入 usecase path |
| classics | SANCAI_ENTRY | qa | AiRefinementController#generateQa | CLASSICS_SANCAI_QA | /internal/ai/classics/sancai/qa | implemented | 六类同步候选型精修能力接入 usecase path |
| classics | SANCAI_ENTRY | visual | AiRefinementController#describeVisual | CLASSICS_SANCAI_VISUAL_DESCRIPTION | /internal/ai/classics/sancai/visual-description | implemented | 六类同步候选型精修能力接入 usecase path |
| classics | SANCAI_ENTRY | split | AiRefinementController#splitEntry | CLASSICS_SANCAI_SPLIT | /internal/ai/classics/sancai/split | implemented | 六类同步候选型精修能力接入 usecase path |
| classics | WANGQI_DOCUMENT | summary | AiRefinementController#summarize | CLASSICS_WANGQI_SUMMARY | /internal/ai/classics/wangqi/summary | implemented | 六类同步候选型精修能力接入 usecase path |
| classics | WANGQI_DOCUMENT | tags | AiRefinementController#generateTags | CLASSICS_WANGQI_TAGS | /internal/ai/classics/wangqi/tags | implemented | 六类同步候选型精修能力接入 usecase path |
| classics | WANGQI_DOCUMENT | qa | AiRefinementController#generateQa | CLASSICS_WANGQI_QA | /internal/ai/classics/wangqi/qa | implemented | 六类同步候选型精修能力接入 usecase path |
| classics | MING_CUSTOMS | summary | AiRefinementController#summarize | CLASSICS_MING_CUSTOMS_SUMMARY | /internal/ai/classics/ming-customs/summary | implemented | 六类同步候选型精修能力接入 usecase path |
| classics | MING_CUSTOMS | tags | AiRefinementController#generateTags | CLASSICS_MING_CUSTOMS_TAGS | /internal/ai/classics/ming-customs/tags | implemented | 六类同步候选型精修能力接入 usecase path |
| classics | MING_CUSTOMS | qa | AiRefinementController#generateQa | CLASSICS_MING_CUSTOMS_QA | /internal/ai/classics/ming-customs/qa | implemented | 六类同步候选型精修能力接入 usecase path |

## AI 有入口但本轮明确排除

| domain | contentType | capability | javaEntry | operation | workerPath | status | note |
| --- | --- | --- | --- | --- | --- | --- | --- |
| classics | SANCAI_ENTRY | image_analysis | AiRefinementController#analyzeImage | CLASSICS_SANCAI_IMAGE_ANALYSIS | /internal/ai/classics/sancai/image-analysis | excluded | workers contract requires stream=true but current Java refinement entry is synchronous |

## workers 已存在但 Java AI 未接入

| domain | contentType | capability | javaEntry | operation | workerPath | status | note |
| --- | --- | --- | --- | --- | --- | --- | --- |
| classics | SANCAI_ENTRY | translate | - | CLASSICS_SANCAI_TRANSLATE_BATCH_ITEM | /internal/ai/classics/sancai/translate-batch-item | not_implemented | 对应 usecase 未在 Classics 精修 resolver 中配置 |
| classics | SANCAI_ENTRY | fusion | - | CLASSICS_SANCAI_FUSION | /internal/ai/classics/sancai/fusion | not_implemented | 对应 usecase 未在 Java 精修入口中接入 |
| classics | SANCAI_ENTRY | image_gen | - | CLASSICS_SANCAI_IMAGE_GEN | /internal/ai/classics/sancai/image-gen | not_implemented | 对应 usecase 未在 Java 精修入口中接入 |
| discovery | - | query_understanding | - | DISCOVERY_QUERY_UNDERSTANDING | /internal/ai/discovery/query-understanding | not_implemented | 当前 Java AI 精修未提供该域的调用入口 |
| discovery | - | query_understanding | - | DISCOVERY_QUERY_REWRITE | /internal/ai/discovery/query-rewrite | not_implemented | 当前 Java AI 精修未提供该域的调用入口 |
| discovery | - | answer_generation | - | DISCOVERY_ANSWER_GENERATION | /internal/ai/discovery/answer-generation | not_implemented | 当前 Java AI 精修未提供该域的调用入口 |
| discovery | - | answer_generation | - | DISCOVERY_ANSWER_GENERATION_STREAM | /internal/ai/discovery/answer-generation/stream | not_implemented | 当前 Java AI 精修未提供该域的调用入口 |
| knowledge | - | relation_extraction | - | KNOWLEDGE_RELATION_EXTRACTION | /internal/ai/knowledge/relation-extraction | not_implemented | 当前 Java AI 精修未提供该域的调用入口 |
| knowledge | - | knowledge_graph | - | KNOWLEDGE_GRAPH_EXTRACTION | /internal/ai/knowledge/graph-extraction | not_implemented | 当前 Java AI 精修未提供该域的调用入口 |
| knowledge | - | lineage_extraction | - | KNOWLEDGE_LINEAGE_EXTRACTION | /internal/ai/knowledge/lineage-extraction | not_implemented | 当前 Java AI 精修未提供该域的调用入口 |
| knowledge | - | tags | - | KNOWLEDGE_TAG_EXTRACTION | /internal/ai/knowledge/tag-extraction | not_implemented | 当前 Java AI 精修未提供该域的调用入口 |
| platform | - | prompt_suggestion | - | PLATFORM_PROMPT_SUGGESTION | /internal/ai/platform/prompt-suggestion | not_implemented | 当前 Java AI 精修未提供该域的调用入口 |
| platform | - | version_summary | - | PLATFORM_VERSION_SUMMARY | /internal/ai/platform/version-summary | not_implemented | 当前 Java AI 精修未提供该域的调用入口 |
