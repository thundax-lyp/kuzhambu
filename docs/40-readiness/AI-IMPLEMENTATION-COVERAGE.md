# AI Implementation Coverage

## Purpose

本文档记录 AI 精修能力、管理端治理能力在 Java、Workers 与 Admin Web 的当前接入状态，用于跟踪后续闭环进度。

## Status Definition

- `已完成`：当前仓库形成可追溯交付，且可走通调用链路。
- `部分完成`：能力有约束或边界不匹配，当前未形成可执行闭环。
- `未完成`：能力的关键调用链或入口尚未在当前仓库里形成闭环。
- `外部依赖`：能力边界不属于当前域，或由其他域/系统承担。

## Current Baseline

已完成：

- Classics 精修入口已对接同步候选能力（`translate/summary/tags/qa/visual/split`）与 Sancai batch item 翻译的 Java → Worker 调用链路。
- AI 设计文档定义的默认精修任务协议（`task/add -> task/get/page -> task/cancel`）已经在代码中落地；当前实现覆盖任务受理、查询、分页、取消、12 小时超时清理与 `TASK_EXPIRED` 失败态收口。
- Knowledge 图谱抽取已对接三类候选能力（`relation_extraction/knowledge_graph/lineage_extraction`）的 Knowledge → AI → Worker 调用链路，并补齐批量任务、取消和重生成所需的 AI 协作台账。
- AI 域当前已具备候选结果台账读取、拒绝和“标记已应用”协作入口，可支持 Classics / Knowledge 在业务确认后回写 AI 候选状态。
- AI 域已形成统一最终态协议：调用结果、调用记录、候选结果和 Worker stream `completed/error` 统一使用 `failureStage / fallbackUsed / artifactReference` 口径；文件类结果由 Java 下载 Workers 临时产物并转存 `Storage`。
- Discovery 的 query understanding、answer generation 与 stream answer 已统一消费 AI 最终态；Wangqi 单文档 QA 正式回答链路已通过 `DiscoveryAiApplicationService#generateAnswer` 消费 `DISCOVERY_ANSWER_GENERATION`，并把最终 AI `callId`、状态和失败字段稳定挂到 QA trace。
- AI 域当前已形成“治理入口 -> Workers 执行 -> 候选结果/任务台账 -> Classics 页面消费”的闭环；三才图会视觉资产已接通 `image_analysis / fusion / visual / image_gen` 的单条任务、批量任务、候选治理、失败重试和正式版本写回。
- Classics 三才图会视觉资产 `image_analysis / image_gen` 已接通 `task/stream` 展示闭环：Java 后端通过 `/api/ai/refinement/task/stream?taskId=...` 代理 worker SSE，Admin Web 展示增量过程、最终任务结果刷新候选区，stream error 可在任务快照刷新前直接重试。
- Knowledge 标签候选抽取已通过 `TaxonomyApplicationService#extractTags` 接入 `KNOWLEDGE_TAG_EXTRACTION`，并在标签治理入口形成人工审核应用闭环。
- AI 管理端治理闭环已完成：Admin Web 已提供服务配置、模型配置、能力映射、提示词版本、调用统计、动作状态 6 个页面；后端补齐调用记录分页、调用统计汇总、动作状态批量读取等 Admin 契约；菜单权限由 `db/data-source/system.json` 生成 `db/data/system.sql` 并通过一致性校验。
- 本轮未新增 AI schema 字段；流式展示复用 `ai_refinement_task.stream_enabled / candidate_id / result_format / result_preview / failure_stage / error_type / error_message` 等既有字段，前端事件结构使用 `eventType / eventId / stage / deltaText / resultFormat / resultPayload / failureStage / errorType / errorMessage / status`。

未完成：

- 当前无 AI Worker 能力等待 Java 治理入口闭环；管理端治理类需求已从后端能力推进到 Admin Web 可操作页面。

## Recent Validation

- 2026-07-07：`feat/classics-ai-streaming-candidates` 已合入最新 `origin/main`。
- 2026-07-07：`cd kuzhambu-servers && mvn -pl biz/ai/kuzhambu-ai-interface,biz/ai/kuzhambu-ai-application,biz/ai/kuzhambu-ai-infra -am spotless:apply && mvn spotless:check && mvn checkstyle:check && mvn -pl biz/ai/kuzhambu-ai-interface,biz/ai/kuzhambu-ai-application,biz/ai/kuzhambu-ai-infra,biz/classics/kuzhambu-classics-application -am test` 通过。
- 2026-07-07：`cd kuzhambu-apps && npm run format:check && npm run lint && npm --workspace kuzhambu-admin-web run build` 通过。
- 2026-07-07：`npm --workspace kuzhambu-admin-web run test -- --maxWorkers=1` 通过，45 个 test files / 153 tests 全绿；原始 `npm --workspace kuzhambu-admin-web run test` 在全量并发下出现非本任务用例 30 秒超时波动，失败用例单独复跑通过。
- 2026-07-08：`cd kuzhambu-servers && mvn -pl biz/discovery/kuzhambu-discovery-interface,biz/discovery/kuzhambu-discovery-infra,biz/ai/kuzhambu-ai-interface,biz/ai/kuzhambu-ai-infra,biz/classics/kuzhambu-classics-facade -am -Dsurefire.failIfNoSpecifiedTests=false test` 通过，覆盖 Discovery QA、AI facade/application/infra 和 Classics facade 相关测试。
- 2026-07-08：`cd kuzhambu-workers && .venv/bin/python -m ruff format --check . && .venv/bin/python -m ruff check . && .venv/bin/python -m pytest -p no:capture tests/test_worker_e2e_ai_usecase_discovery.py` 通过，覆盖 Discovery answer-generation worker usecase 契约。
- 2026-07-09：`node scripts/generate-system-data-sql.ts --check` 通过，确认 AI 菜单权限 `system.json -> system.sql` 生成结果一致。
- 2026-07-09：`cd kuzhambu-servers && mvn spotless:check && mvn checkstyle:check && mvn test` 通过。
- 2026-07-09：`cd kuzhambu-apps && pnpm run format:check && pnpm run lint && pnpm run test && pnpm run build` 通过；其中 `admin-web` 为 64 个 test files / 275 tests，`portal-web` 为 13 个 test files / 50 tests。

## 管理端治理闭环

| page | route | primary files | backend contract | status | note |
| --- | --- | --- | --- | --- | --- |
| 服务配置 | `/ai/services` | `kuzhambu-apps/admin-web/src/pages/ai/services/services-page.tsx` | `/api/ai/config/service/*` | 已完成 | 支持 PRIMARY/BACKUP 查看、编辑、启停；不展示明文 API Key |
| 模型配置 | `/ai/models` | `kuzhambu-apps/admin-web/src/pages/ai/model-configs/model-configs-page.tsx` | `/api/ai/config/model/*` | 已完成 | 支持模型新增、编辑、启停、删除、检测和检测历史 |
| 能力映射 | `/ai/capability-mappings` | `kuzhambu-apps/admin-web/src/pages/ai/capability-mappings/capability-mappings-page.tsx` | `/api/ai/config/capability/mapping/*` | 已完成 | 支持 scope + capability 到模型的映射配置和标签匹配提示 |
| 提示词版本 | `/ai/prompts` | `kuzhambu-apps/admin-web/src/pages/ai/prompts/prompts-page.tsx` | `/api/ai/prompt/*` | 已完成 | 支持模板查询、版本编辑、变量预览、版本查看、对比、回滚和优化建议预览 |
| 调用统计 | `/ai/invocations` | `kuzhambu-apps/admin-web/src/pages/ai/invocations/invocations-page.tsx` | `/api/ai/invocation/call/summary`、`/api/ai/invocation/call/page` | 已完成 | 支持周期统计、能力排行、调用记录分页和调用详情 |
| 动作状态 | `/ai/action-status` | `kuzhambu-apps/admin-web/src/pages/ai/action-status/action-status-page.tsx` | `/api/ai/config/action/status/list`、`/api/ai/config/action/status/refresh` | 已完成 | 支持 scope/capability/available 筛选、单条刷新和批量刷新 |

## 已完成

| domain | contentType | capability | javaEntry | operation | workerPath | status | note |
| --- | --- | --- | --- | --- | --- | --- | --- |
| classics | SANCAI_ENTRY | translate | AiRefinementController#translate | CLASSICS_SANCAI_TRANSLATE | /internal/ai/classics/sancai/translate | 已完成 | 六类同步候选型精修能力接入 usecase path |
| classics | SANCAI_ENTRY | translate_batch_item | ClassicsAiWorkerUsecaseResolver#resolve | CLASSICS_SANCAI_TRANSLATE_BATCH_ITEM | /internal/ai/classics/sancai/translate-batch-item | 已完成 | Java 精修 resolver 已接入 worker batch item usecase path |
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
| classics | SANCAI_ENTRY / WANGQI_DOCUMENT / MING_CUSTOMS | task_management | AiRefinementTaskController#add/get/page/cancel/stream | AI_REFINEMENT_TASK_MANAGEMENT | - | 已完成 | 已实现设计文档定义的默认任务协议；`task/stream` 已用于三才视觉资产流式过程展示 |
| classics | SANCAI_ENTRY | batch_visual_processing | AiRefinementTaskController#createBatch/get/page/cancelBatch | AI_REFINEMENT_TASK_BATCH_MANAGEMENT | - | 已完成 | 三才图会已接通批量图片理解与视觉资产处理任务的创建、分页、取消和失败聚合 |
| discovery | - | query_understanding | DiscoveryAiApplicationService#understandQuery | DISCOVERY_QUERY_UNDERSTANDING | /internal/ai/discovery/query-understanding | 已完成 | 已统一消费 AI 最终态字段；失败时按最终错误口径落库 |
| discovery | - | query_understanding | DiscoveryAiApplicationService#rewriteQuery | DISCOVERY_QUERY_REWRITE | /internal/ai/discovery/query-rewrite | 已完成 | 已统一消费 AI 最终态字段；失败时按最终错误口径落库 |
| discovery | WANGQI_DOCUMENT | answer_generation | DiscoveryAiApplicationService#generateAnswer | DISCOVERY_ANSWER_GENERATION | /internal/ai/discovery/answer-generation | 已完成 | Wangqi 单文档 QA 正式回答消费 AI 最终态字段，并把 `callId/status/errorType/errorMessage` 挂到 QA trace |
| discovery | - | answer_generation | DiscoveryAiApplicationService#streamAnswer | DISCOVERY_ANSWER_GENERATION_STREAM | /internal/ai/discovery/answer-generation/stream | 已完成 | stream 最终态消费已收口，并把最终 `callId` 挂到 QA trace |
| knowledge | SANCAI_ENTRY | relation_extraction | KnowledgeGraphExtractionApplicationService#requestRelationExtraction | KNOWLEDGE_RELATION_EXTRACTION | /internal/ai/knowledge/relation-extraction | 已完成 | 已形成任务台账、AI 调用、候选应用、批量与重生成闭环 |
| knowledge | SANCAI_ENTRY | knowledge_graph | KnowledgeGraphExtractionApplicationService#requestGraphExtraction | KNOWLEDGE_GRAPH_EXTRACTION | /internal/ai/knowledge/graph-extraction | 已完成 | 已形成任务台账、AI 调用、候选应用、批量与重生成闭环 |
| knowledge | SANCAI_ENTRY | lineage_extraction | KnowledgeGraphExtractionApplicationService#requestLineageExtraction | KNOWLEDGE_LINEAGE_EXTRACTION | /internal/ai/knowledge/lineage-extraction | 已完成 | 已形成任务台账、AI 调用、候选应用、批量与重生成闭环 |
| knowledge | - | tags | TaxonomyApplicationService#extractTags | KNOWLEDGE_TAG_EXTRACTION | /internal/ai/knowledge/tag-extraction | 已完成 | 已通过 Knowledge 标签治理入口接入 AI candidate 闭环 |
| platform | - | prompt_suggestion | PlatformAiController#buildPromptSuggestion | PLATFORM_PROMPT_SUGGESTION | /internal/ai/platform/prompt-suggestion | 已完成 | 已接入 Java Platform AI 入口，默认创建候选结果供人工确认 |
| platform | - | version_summary | PlatformAiController#summarizeVersion | PLATFORM_VERSION_SUMMARY | /internal/ai/platform/version-summary | 已完成 | 已接入 Java Platform AI 入口，默认只记录调用结果 |

## 未完成

| domain | contentType | capability | javaEntry | operation | workerPath | status | note |
| --- | --- | --- | --- | --- | --- | --- | --- |
