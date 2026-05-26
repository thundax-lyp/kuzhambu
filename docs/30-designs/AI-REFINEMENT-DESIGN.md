# AI Refinement Design

## Purpose

本文档定义 AI 内容精修业务域的数据结构和接口设计。本文参考 redesign 中的 AI 翻译、标签提取、摘要、问答对、图片理解和条目拆分材料，但不照搬其大对象模型、数据结构或接口形态；本设计必须从 AI 结果进入候选区、用户确认后应用的业务流程出发，按 DDD 风格重新划分业务边界、聚合和表结构。

AI Refinement 是候选结果与精修编排域，不承载 AI 服务配置、提示词治理、内容长期维护、视觉资产完整创作、智能问答会话或知识图谱提取。它只拥有 AI 精修请求、候选结果、批量精修进度、失败原因、取消状态和图片理解缓存。

## Business Fit Rules

- 业务优先于旧设计：翻译、摘要、标签、问答对、图片理解、条目拆分、候选确认、拒绝、重试、批量取消和部分失败必须能直接对应 AI 精修业务。
- AI 结果不得直接覆盖正式内容；本模块只保存候选结果，确认应用由对应内容业务域完成最终写入和版本快照。
- 批量处理只作为本模块 AI 精修能力的内部编排，不建立独立 Batch Task 业务域。

## Module

- 模块名称：AI Refinement
- 业务域：airefinement
- 对应需求文档：[AI-REFINEMENT-REQUIREMENTS.md](../10-requirements/AI-REFINEMENT-REQUIREMENTS.md)
- 后端 biz 子工程：`kuzhambu-servers/biz/kuzhambu-biz-ai-refinement`
- 后端 infra 子工程：`kuzhambu-servers/infra/kuzhambu-infra-ai-refinement`
- 后台接口入口：`kuzhambu-servers/interfaces/kuzhambu-admin-api`
- 前台接口入口：无
- 前端入口：`kuzhambu-apps/admin-web`
- Python worker 能力：按需通过 HTTP 调用，但不承载最终业务写入

## Business Boundary

- 本模块负责：三才翻译候选、三库摘要候选、三库标签候选、三库问答对候选、三才图片理解候选、三才条目拆分候选、AI 调用失败反馈、重试、候选确认状态、候选拒绝状态、批量精修进度和图片理解缓存。
- 本模块不负责：AI 服务和提示词配置、内容正式字段长期维护、三才视觉资产信息融合和生图、智能问答会话、知识图谱提取、标签治理。
- 依赖的其他业务域能力：AI Config Prompt 提供模型和提示词配置；Sancai/Wangqi/Ming Customs 提供内容上下文并承接确认应用；Taxonomy 承接标签权威治理；Storage 提供图片对象读取；Audit 记录确认应用和拒绝。
- 对外提供的业务能力：AI 精修候选生成、候选管理、批量精修状态、失败重试和图片理解缓存复用。

## DDD Model

### Aggregates

- `AiRefinementCandidate`：候选结果聚合根，记录一次 AI 精修结果、目标内容、结果载荷、状态、失败原因和确认应用信息。
- `AiRefinementBatch`：本域批量精修聚合，记录批量翻译、批量标签提取和批量图片理解的进度、取消和统计。
- `AiRefinementBatchItem`：批量精修明细，记录单个目标的执行状态、候选结果或失败原因。
- `ImageAnalysisCache`：图片理解缓存，按图片对象和内容哈希保存底层 AI 分析结果。

### Value Objects

- `AiRefinementOperationType`：`TRANSLATE` / `SUMMARY` / `TAGS` / `QA_PAIRS` / `IMAGE_ANALYSIS` / `SPLIT_ENTRY`。
- `AiRefinementContentType`：`SANCAI_ENTRY` / `WANGQI_DOCUMENT` / `MING_CUSTOM`。
- `AiRefinementCandidateStatus`：`PENDING` / `APPLIED` / `REJECTED` / `FAILED` / `CANCELLED`。
- `AiRefinementBatchStatus`：`RUNNING` / `COMPLETED` / `PARTIAL_FAILED` / `CANCELLED` / `FAILED`。
- `AiRefinementResultFormat`：`TEXT` / `MARKDOWN` / `JSON`。

## Data Model

AI Refinement 表固定使用 `ai_refinement_` 前缀；图片理解缓存使用 `image_analysis_cache`。所有对外标识使用 ULID，数据库内部可使用 `bigint id`。

### ai_refinement_candidate

保存 AI 精修候选结果。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `candidate_id` | `candidateId` | 是 | 候选 ULID |
| `batch_id` | `batchId` | 否 | 批量精修 ULID |
| `operation_type` | `operationType` | 是 | 精修操作类型 |
| `content_type` | `contentType` | 是 | 内容类型 |
| `content_id` | `contentId` | 是 | 内容业务 ULID |
| `object_id` | `objectId` | 否 | 图片 Storage 对象 ULID |
| `result_format` | `resultFormat` | 是 | 结果格式 |
| `result_payload` | `resultPayload` | 否 | 候选结果载荷 |
| `status` | `status` | 是 | 候选状态 |
| `prompt_id` | `promptId` | 否 | 调用时提示词版本 ULID |
| `model_name` | `modelName` | 否 | 调用时模型名快照 |
| `error_message` | `errorMessage` | 否 | 失败原因 |
| `applier_user_id` | `applierUserId` | 否 | 确认应用人 |
| `applied_at` | `appliedAt` | 否 | 确认应用时间 |

约束：
- `candidate_id` 唯一。
- AI 结果只进入候选，不直接覆盖正式内容。
- `APPLIED` 候选不得再次拒绝或重复应用。
- `REJECTED` 候选不得再应用，可重新发起新候选。
- 翻译候选结果格式为 `TEXT`；图片理解候选结果格式为 `MARKDOWN`；标签、问答对和拆分结果格式为 `JSON`。

### ai_refinement_batch

保存本域批量精修任务。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `batch_id` | `batchId` | 是 | 批量精修 ULID |
| `operation_type` | `operationType` | 是 | 批量操作类型 |
| `content_type` | `contentType` | 是 | 内容类型 |
| `status` | `status` | 是 | 批量状态 |
| `total_count` | `totalCount` | 是 | 总数 |
| `success_count` | `successCount` | 是 | 成功数 |
| `failed_count` | `failedCount` | 是 | 失败数 |
| `cancelled_count` | `cancelledCount` | 是 | 取消数 |
| `cancelled_at` | `cancelledAt` | 否 | 取消时间 |

约束：
- 只用于 AI Refinement 域内批量翻译、批量标签提取和批量图片理解。
- 用户取消批量操作后，已完成结果必须保留，未开始项目标记取消。
- 单条失败不得影响其他目标。

### ai_refinement_batch_item

保存批量精修明细。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `batch_item_id` | `batchItemId` | 是 | 明细 ULID |
| `batch_id` | `batchId` | 是 | 批量精修 ULID |
| `content_id` | `contentId` | 是 | 内容业务 ULID |
| `object_id` | `objectId` | 否 | 图片 Storage 对象 ULID |
| `candidate_id` | `candidateId` | 否 | 候选 ULID |
| `status` | `status` | 是 | 明细状态 |
| `error_message` | `errorMessage` | 否 | 失败原因 |

约束：
- 同一批量内同一内容和同一图片对象不得重复。
- 失败明细必须保留失败原因。

### image_analysis_cache

保存图片理解底层 AI 分析缓存。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `cache_id` | `cacheId` | 是 | 缓存 ULID |
| `object_id` | `objectId` | 是 | Storage 对象 ULID |
| `content_hash` | `contentHash` | 是 | 图片内容哈希 |
| `analysis_markdown` | `analysisMarkdown` | 是 | AI 分析结果 |
| `prompt_id` | `promptId` | 否 | 提示词版本 ULID |
| `model_name` | `modelName` | 否 | 模型名快照 |

约束：
- `object_id + content_hash` 唯一。
- 使用已有结果时可直接生成候选。
- 强制重新分析时更新缓存或生成新的缓存记录，由应用服务按内容哈希判断。
- 用户编辑候选结果后，不得自动覆盖底层缓存。

## Application Layer

- `AiTranslationApplicationService`：三才单条和批量翻译候选生成、格式提取和失败处理。
- `AiTagExtractionApplicationService`：三库单条和批量标签候选生成。
- `AiSummaryApplicationService`：三库摘要候选生成。
- `AiQaPairApplicationService`：三库问答对候选生成。
- `AiImageAnalysisApplicationService`：三才图片理解候选、缓存复用、强制重新分析和批量分析。
- `AiEntrySplitApplicationService`：三才条目拆分候选、预览结果和批量保存申请。
- `AiRefinementCandidateApplicationService`：候选读取、编辑后保存、确认应用、拒绝和重试。
- `AiRefinementBatchApplicationService`：批量进度、取消、成功失败统计和明细查询。

事务边界：
- 候选生成成功必须保存候选结果；失败必须保存失败原因。
- 候选确认应用必须调用对应内容域应用服务完成正式写入和版本快照，再将候选标记为 `APPLIED`。
- 候选拒绝只更新候选状态，不得修改正式内容。
- 批量取消只影响未开始和运行中明细，已完成候选保留。
- 图片理解使用缓存时仍生成新的候选记录；候选编辑不覆盖缓存。

## Interface Layer

后台接口固定使用 `/api/admin/ai-refinement/**`。

### Candidate API

- `GET /ai-refinement/candidates/{candidateId}`：读取候选详情。
- `PUT /ai-refinement/candidates/{candidateId}`：编辑候选结果。
- `POST /ai-refinement/candidates/{candidateId}/apply`：确认应用候选。
- `POST /ai-refinement/candidates/{candidateId}/reject`：拒绝候选。
- `POST /ai-refinement/candidates/{candidateId}/retry`：重试失败候选。

### Translation API

- `POST /ai-refinement/sancai/entries/{entryId}/translate`：生成三才翻译候选。
- `POST /ai-refinement/sancai/entries/batch/translate`：批量生成三才翻译候选。

### Tag Summary And QA API

- `POST /ai-refinement/{contentType}/{contentId}/tags`：生成标签候选。
- `POST /ai-refinement/{contentType}/batch/tags`：批量生成标签候选。
- `POST /ai-refinement/{contentType}/{contentId}/summary`：生成摘要候选。
- `POST /ai-refinement/{contentType}/{contentId}/qa-pairs`：生成问答对候选。

### Image Analysis And Split API

- `POST /ai-refinement/sancai/entries/{entryId}/images/{objectId}/analysis`：生成图片理解候选。
- `POST /ai-refinement/sancai/images/batch/analysis`：批量生成图片理解候选。
- `POST /ai-refinement/sancai/entries/{entryId}/split`：生成条目拆分候选。
- `POST /ai-refinement/candidates/{candidateId}/split/save`：确认保存拆分子条目。

### Batch API

- `GET /ai-refinement/batches/{batchId}`：查询批量进度和统计。
- `GET /ai-refinement/batches/{batchId}/items`：查询批量明细。
- `POST /ai-refinement/batches/{batchId}/cancel`：取消未完成批量操作。

本模块不提供 portal 接口。

## Infrastructure Layer

- Repository：`AiRefinementCandidateRepository`、`AiRefinementBatchRepository`、`AiRefinementBatchItemRepository`、`ImageAnalysisCacheRepository`。
- Mapper：按表建立 MyBatis Mapper。
- PersistenceAssembler：每个聚合根使用独立 `*PersistenceAssembler`。
- 外部客户端：AI Config Prompt Application Service、Sancai Application Service、Wangqi Application Service、Ming Customs Application Service、Taxonomy Application Service、Storage Application Service、Audit Application Service、Python worker HTTP Client。
- 缓存：图片理解缓存由 `image_analysis_cache` 持久化，运行时可加本地缓存。
- 消息：批量精修可通过 RocketMQ 分发本域任务明细，但最终候选写入仍由本模块负责。

## Data Ownership

- 本模块拥有：`ai_refinement_candidate`、`ai_refinement_batch`、`ai_refinement_batch_item`、`image_analysis_cache`。
- 本模块只读引用：AI Config Prompt 配置、Storage 文件对象、内容域目标对象、Auth/Core 用户和权限。
- 禁止跨域直接访问：不得直接修改三才、王圻、明代习俗正式内容表；不得直接维护 Taxonomy 权威标签；不得写入 QA 会话或 Knowledge Graph 结果。
- Flyway 脚本归属：`kuzhambu-admin-api` 的 `db/migration/V1__init.sql`，按 AI Refinement 分段。

## Observability

- 运行日志：记录 AI 调用失败、候选格式异常、批量取消、单条失败和确认应用失败原因。
- 访问日志：由接口层统一记录。
- 审计日志：候选确认应用、拒绝、批量取消和强制重新分析都应形成业务审计。
- 关键指标：候选数、应用率、拒绝率、失败率、批量成功数、图片缓存命中率、平均生成耗时。

## Acceptance

- 用户能看到 AI 调用 loading 状态。
- AI 调用失败时用户能看到原因并重试。
- 用户拒绝候选结果后正式内容不变。
- 批量操作部分失败时成功结果保留。
- 批量翻译、批量标签提取和批量图片理解完成后，用户能看到成功数、失败数和失败原因。
- 用户取消批量翻译、批量标签提取或批量图片理解后，已完成结果仍可查看和应用。
- 三才图会翻译结果以纯译文形式进入候选区。
- 标签提取结果进入候选区，用户能编辑标签后再接受。
- 图片理解结果能以 Markdown 形式预览和编辑。
- 用户能对同一图片触发强制重新分析。
- 用户能预览、调整并批量保存条目拆分结果。
