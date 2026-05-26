# Sancai Knowledge Design

## Purpose

本文档定义三才图会知识库的数据结构和接口设计。本文参考 redesign 中的《三才图会》材料，但不照搬其大对象模型、数据结构或接口形态；本设计必须从《三才图会》业务需求出发，按 DDD 风格重新划分业务边界、聚合和表结构。

三才知识域是内容域，不承载 AI 执行、标签治理、搜索问答、分享链接或通用文件存储。它只拥有《三才图会》书目结构、条目内容、条目版本、视觉资产和三才范围内的导出/静态展示产物记录。

## Business Fit Rules

- 业务优先于旧设计：门类、卷、条目、正文、译文、图像、摘要、问答对、视觉资产、导出和静态展示页面必须能直接对应三才知识库的实际操作。
- 原 redesign 只能作为需求线索和素材来源，不能作为表结构、接口路径、模块划分或聚合边界的设计依据。
- 不把跨域实现状态写入三才主数据；搜索、问答检索、知识图谱、AI 任务、分享链接和存储实现由各自业务域维护。
- 不为未来增强、路线图或预留能力建表、建接口或增加状态字段。
- 批量处理只作为三才条目、视觉资产、公开状态等业务操作的批量入口，不建立独立 Batch Task 业务域。

## Module

- 模块名称：Sancai Knowledge
- 业务域：sancai
- 对应需求文档：[SANCAI-KNOWLEDGE-REQUIREMENTS.md](../10-requirements/SANCAI-KNOWLEDGE-REQUIREMENTS.md)
- 后端 biz 子工程：`kuzhambu-servers/biz/kuzhambu-biz-sancai`
- 后端 infra 子工程：`kuzhambu-servers/infra/kuzhambu-infra-sancai`
- 后台接口入口：`kuzhambu-servers/interfaces/kuzhambu-admin-api`
- 前台接口入口：`kuzhambu-servers/interfaces/kuzhambu-portal-api`
- 前端入口：`kuzhambu-apps/admin-web`、`kuzhambu-apps/portal-web`
- Python worker 能力：无直接归属；AI 处理由 AI Refinement / workers 提供候选结果

## Business Boundary

- 本模块负责：三才图会门类、卷、卷首辅助内容、条目、正文、译文、摘要、问答对、条目图片引用、视觉资产版本、条目版本、草稿、生命周期、公开私有状态、三才导出记录和静态展示页面记录。
- 本模块不负责：AI 翻译执行、标签治理、跨库搜索、问答检索、知识图谱抽取、分享链接、通用文件上传、图片加工、CDN 分发。
- 依赖的其他业务域能力：Storage 提供文件对象；Taxonomy 提供标签权威数据；AI Refinement 提供候选结果；Audit 记录写操作；Auth/Core 提供操作者和权限。
- 对外提供的业务能力：三才书目浏览、条目管理、视觉资产管理、版本恢复、导出和静态展示页面生成。

## DDD Model

### Aggregates

- `SancaiCatalog`：门类和卷结构。门类、卷是稳定书目结构，主要用于导航、排序和范围选择。
- `SancaiEntry`：条目聚合根，拥有正文、译文、生命周期、可见性、摘要、问答对、图片引用和展示缓存。
- `SancaiEntryVersion`：条目正式版本快照，服务于版本历史、对比和恢复。
- `SancaiDraft`：用户编辑草稿，服务于自动保存和草稿恢复。
- `SancaiVisualAsset`：视觉资产聚合，绑定一个条目，拥有原图引用、图片理解、融合描述、视觉描述、AI 生成图和当前使用版本。
- `SancaiExportJob`：三才导出产物记录，覆盖 CSV、JSON、HTML 设定集和视觉资产设定集。
- `SancaiShowcasePage`：三才静态展示页面生成记录。

### Value Objects

- `SancaiCategoryCode`：门类代码，正式门类为 `tw/dl/rw/sl/gs/qy/st/yf/rs/yz/zb/ws/ns/cm`，卷首辅助内容为 `js`。
- `SancaiVolumeRef`：`categoryCode + volumeNo + volumeId`。
- `SancaiEntryLifecycleStatus`：`DRAFT` / `PUBLISHED` / `ARCHIVED`。
- `ContentVisibility`：`PUBLIC` / `PRIVATE`。
- `SancaiEntryStatusSet`：翻译、配图、视觉资产、完善状态的组合状态。
- `VisualAssetKind`：`ORIGINAL_IMAGE` / `IMAGE_ANALYSIS` / `FUSION_DESCRIPTION` / `VISUAL_DESCRIPTION` / `GENERATED_IMAGE`。

## Data Model

Sancai 表固定使用 `sancai_` 前缀。所有对外标识使用 ULID，数据库内部可使用 `bigint id`。

### sancai_category

保存三才图会门类和卷首辅助内容。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `category_code` | `categoryCode` | 是 | 门类代码 |
| `name` | `name` | 是 | 门类名称 |
| `formal` | `formal` | 是 | 是否正式 14 门类 |
| `sort_order` | `sortOrder` | 是 | 稳定排序 |
| `description` | `description` | 否 | 说明 |

约束：
- `category_code` 唯一。
- `js` 是卷首辅助内容，不计入 14 个正式门类。

### sancai_volume

保存卷结构。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `volume_id` | `volumeId` | 是 | 卷 ULID |
| `category_code` | `categoryCode` | 是 | 所属门类代码 |
| `volume_no` | `volumeNo` | 否 | 正式卷号，1-106 |
| `title` | `title` | 是 | 卷名 |
| `auxiliary` | `auxiliary` | 是 | 是否卷首辅助内容 |
| `sort_order` | `sortOrder` | 是 | 稳定排序 |
| `entry_count` | `entryCount` | 是 | 条目数量缓存 |

约束：
- `volume_id` 唯一。
- `category_code + volume_no` 在正式卷内唯一。
- 卷首辅助内容可没有正式 `volume_no`。

### sancai_entry

保存条目主数据。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `entry_id` | `entryId` | 是 | 条目 ULID |
| `category_code` | `categoryCode` | 是 | 门类代码 |
| `volume_id` | `volumeId` | 是 | 卷 ULID |
| `entry_no` | `entryNo` | 否 | 卷内序号 |
| `title` | `title` | 是 | 条目标题 |
| `original_text` | `originalText` | 否 | 原文 |
| `translation_text` | `translationText` | 否 | 译文 |
| `summary` | `summary` | 否 | 摘要 |
| `tags_snapshot` | `tagsSnapshot` | 否 | 标签展示缓存 JSON |
| `lifecycle_status` | `lifecycleStatus` | 是 | `DRAFT` / `PUBLISHED` / `ARCHIVED` |
| `visibility` | `visibility` | 是 | `PUBLIC` / `PRIVATE` |
| `owner_user_id` | `ownerUserId` | 是 | 归属用户 ULID |
| `translation_status` | `translationStatus` | 是 | `MISSING` / `DONE` |
| `image_status` | `imageStatus` | 是 | `MISSING` / `DONE` |
| `visual_asset_status` | `visualAssetStatus` | 是 | `MISSING` / `IN_PROGRESS` / `DONE` |
| `refinement_status` | `refinementStatus` | 是 | `RAW` / `REFINED` |
| `current_version` | `currentVersion` | 是 | 当前正式版本号 |

约束：
- `entry_id` 唯一。
- 生命周期与公开私有状态相互独立。
- 归档条目不进入默认列表、搜索、问答、导出和静态展示。
- 删除使用状态字段或操作时间表达，不直接破坏分享目标占位能力。

### sancai_entry_image

保存条目图片引用，不保存文件路径。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `entry_id` | `entryId` | 是 | 条目 ULID |
| `object_id` | `objectId` | 是 | Storage 对象 ULID |
| `image_role` | `imageRole` | 是 | `ORIGINAL` / `GENERATED` |
| `current_used` | `currentUsed` | 是 | 是否当前展示使用 |
| `sort_order` | `sortOrder` | 是 | 排序 |
| `caption` | `caption` | 否 | 图片说明 |

约束：
- `entry_id + object_id` 唯一。
- 原图和 AI 生成图必须可区分。

### sancai_entry_qa

保存条目上下文内联问答对。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `qa_id` | `qaId` | 是 | 问答对 ULID |
| `entry_id` | `entryId` | 是 | 条目 ULID |
| `question` | `question` | 是 | 问题 |
| `answer` | `answer` | 是 | 答案 |
| `source` | `source` | 是 | `MANUAL` / `AI_APPLIED` |
| `sort_order` | `sortOrder` | 是 | 排序 |

约束：
- 问答对属于条目上下文，不是独立数据精修工作台对象。

### sancai_entry_version

保存条目正式版本快照。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `version_id` | `versionId` | 是 | 版本 ULID |
| `entry_id` | `entryId` | 是 | 条目 ULID |
| `version_no` | `versionNo` | 是 | 版本号 |
| `snapshot_json` | `snapshotJson` | 是 | 条目快照 JSON |
| `change_type` | `changeType` | 是 | `MANUAL_SAVE` / `AI_APPLY` / `RESTORE` |
| `change_summary` | `changeSummary` | 否 | 变更摘要 |
| `versioned_at` | `versionedAt` | 是 | 版本生成时间 |

约束：
- 手动保存、AI 结果应用和历史恢复产生正式版本。
- 自动保存不产生正式版本。
- 恢复历史版本本身也生成新版本。

### sancai_entry_draft

保存用户自动保存草稿。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `draft_id` | `draftId` | 是 | 草稿 ULID |
| `entry_id` | `entryId` | 是 | 条目 ULID |
| `user_id` | `userId` | 是 | 用户 ULID |
| `draft_json` | `draftJson` | 是 | 草稿内容 JSON |
| `autosaved_at` | `autosavedAt` | 是 | 服务端草稿保存时间 |

约束：
- 同一用户同一条目最多保留一份当前草稿。
- 草稿恢复后可删除或继续覆盖。
- 当前端同时发现浏览器本地草稿和服务端草稿时，必须展示两份草稿保存时间，默认推荐较新的草稿，用户可选择恢复本地草稿、恢复服务端草稿或放弃恢复。

### sancai_visual_asset

保存条目视觉资产版本。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `asset_id` | `assetId` | 是 | 视觉资产 ULID |
| `entry_id` | `entryId` | 是 | 条目 ULID |
| `version_no` | `versionNo` | 是 | 资产版本号 |
| `source_object_id` | `sourceObjectId` | 否 | 原图 Storage 对象 |
| `generated_object_id` | `generatedObjectId` | 否 | AI 生成图 Storage 对象 |
| `image_analysis` | `imageAnalysis` | 否 | 图片理解结果 |
| `fusion_description` | `fusionDescription` | 否 | 融合描述 |
| `visual_description` | `visualDescription` | 否 | 视觉描述 |
| `text_weight` | `textWeight` | 是 | 文本权重 |
| `image_weight` | `imageWeight` | 是 | 图片权重 |
| `generation_params` | `generationParams` | 否 | 生图参数 JSON |
| `current_used` | `currentUsed` | 是 | 是否当前使用版本 |
| `status` | `status` | 是 | `DRAFT` / `READY` / `FAILED` |

约束：
- 视觉资产必须绑定明确条目。
- 用户可以选择当前使用版本。
- 生图失败不得影响已有原图和历史产物。

### sancai_export_job

保存三才导出产物记录。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `export_id` | `exportId` | 是 | 导出 ULID |
| `export_type` | `exportType` | 是 | `CSV` / `JSON` / `HTML_DATASET` / `VISUAL_ASSET_HTML` |
| `scope_type` | `scopeType` | 是 | `CATEGORY` / `VOLUME` / `FILTER` / `SELECTED` |
| `scope_json` | `scopeJson` | 是 | 范围参数 JSON |
| `object_id` | `objectId` | 否 | Storage 导出产物对象 |
| `entry_count` | `entryCount` | 是 | 条目数量 |
| `asset_count` | `assetCount` | 是 | 资产数量 |
| `contains_private` | `containsPrivate` | 是 | 是否包含私有内容 |
| `status` | `status` | 是 | `PENDING` / `DONE` / `FAILED` / `EXPIRED` |
| `content_changed` | `contentChanged` | 是 | 导出后内容是否可能已变更 |
| `requester_user_id` | `requesterUserId` | 是 | 导出请求用户 |
| `requested_at` | `requestedAt` | 是 | 导出请求时间 |
| `expires_at` | `expiresAt` | 是 | 过期时间 |

约束：
- 导出前必须按权限过滤内容。
- 包含私有条目时必须二次确认。
- 过期导出记录不可继续下载。
- 导出完成后，导出范围内条目或视觉资产发生正式变更时，相关未过期导出记录应标记 `content_changed=true`；该标记只用于提示“内容可能已变更”，不影响下载。

### sancai_showcase_page

保存三才静态展示页面生成记录。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `showcase_id` | `showcaseId` | 是 | 静态页面 ULID |
| `scope_json` | `scopeJson` | 是 | 范围参数 JSON |
| `object_id` | `objectId` | 是 | Storage HTML 产物对象 |
| `entry_count` | `entryCount` | 是 | 条目数量 |
| `contains_private` | `containsPrivate` | 是 | 是否包含私有内容 |
| `private_risk_confirmed` | `privateRiskConfirmed` | 是 | 私有内容风险是否已确认 |
| `status` | `status` | 是 | `PENDING` / `DONE` / `FAILED` |
| `requester_user_id` | `requesterUserId` | 是 | 静态页面生成请求用户 |
| `requested_at` | `requestedAt` | 是 | 静态页面生成请求时间 |

约束：
- 默认只包含公开且未归档条目。
- 包含私有条目时必须管理员确认。
- 静态展示页面一旦生成即脱离平台权限、撤销和内容状态变更自动控制。

### Static Showcase Risk Copy

三才静态展示页面包含平台内私有条目时，生成前必须展示固定确认文案：

`你正在将平台内私有内容写入三才图会静态展示页面。生成后的 HTML 文件将脱离平台登录、权限、撤销和搜索可见性控制；任何获得文件或访问地址的人都可能查看这些内容。此操作不会改变内容在平台内的私有状态。是否继续生成？`

约束：
- `contains_private = true` 时必须要求管理员完成确认，确认后才能创建生成记录。
- `private_risk_confirmed` 只表达该静态展示页面生成请求已完成风险确认；确认人、确认时间和确认上下文进入 Audit。
- `contains_private = false` 时无需展示风险确认文案，`private_risk_confirmed` 固定为 `false`。

## Application Layer

- `SancaiCatalogApplicationService`：门类、卷和导航查询。
- `SancaiEntryApplicationService`：条目创建、编辑、删除、发布、归档、公开私有状态变更。
- `SancaiEntryDraftApplicationService`：自动保存、草稿读取、草稿恢复和草稿清理。
- `SancaiEntryVersionApplicationService`：版本创建、版本列表、版本对比和历史恢复。
- `SancaiEntryMediaApplicationService`：原图绑定、删除、排序、当前使用图管理。
- `SancaiVisualAssetApplicationService`：图片理解结果应用、融合描述、视觉描述、AI 生成图产物登记和当前版本选择。
- `SancaiExportApplicationService`：CSV、JSON、HTML 设定集和视觉资产设定集导出。
- `SancaiShowcaseApplicationService`：静态展示页面生成记录和产物管理。

事务边界：
- 条目正式保存必须更新条目主数据并创建版本快照。
- AI 候选结果只有用户确认应用后才写入条目或视觉资产。
- 图片引用变化必须同步调用 Storage 建立或清理引用。
- 发布、归档、公开私有状态变更必须记录 Audit。

## Interface Layer

后台接口固定使用 `/api/admin/sancai/**`。

### Catalog API

- `GET /sancai/categories`：查询门类。
- `GET /sancai/categories/{categoryCode}/volumes`：查询门类卷列表。
- `GET /sancai/navigation`：查询门类、卷、条目数量导航树。

### Entry API

- `GET /sancai/entries`：分页查询条目，支持门类、卷、状态、公开状态、翻译状态、配图状态、视觉资产状态、关键词。
- `GET /sancai/entries/{entryId}`：读取条目详情。
- `POST /sancai/entries`：创建条目。
- `PUT /sancai/entries/{entryId}`：保存条目正式版本。
- `DELETE /sancai/entries/{entryId}`：删除条目。
- `POST /sancai/entries/{entryId}/publish`：发布草稿条目。
- `POST /sancai/entries/{entryId}/archive`：归档条目。
- `POST /sancai/entries/{entryId}/restore-archived`：恢复归档条目。
- `PUT /sancai/entries/{entryId}/visibility`：修改公开或私有状态。
- `POST /sancai/entries/batch/visibility`：批量修改公开或私有状态。

### Draft And Version API

- `PUT /sancai/entries/{entryId}/draft`：自动保存草稿。
- `GET /sancai/entries/{entryId}/draft`：读取当前用户草稿。
- `POST /sancai/entries/{entryId}/draft/restore`：恢复草稿。
- `GET /sancai/entries/{entryId}/versions`：读取版本历史。
- `GET /sancai/entries/{entryId}/versions/{versionId}`：读取版本详情。
- `POST /sancai/entries/{entryId}/versions/{versionId}/restore`：恢复历史版本。

### Summary And QA API

- `PUT /sancai/entries/{entryId}/summary`：保存摘要。
- `GET /sancai/entries/{entryId}/qa-pairs`：读取问答对。
- `POST /sancai/entries/{entryId}/qa-pairs`：新增问答对。
- `PUT /sancai/entries/{entryId}/qa-pairs/{qaId}`：更新问答对。
- `DELETE /sancai/entries/{entryId}/qa-pairs/{qaId}`：删除问答对。

### Media And Visual Asset API

- `GET /sancai/entries/{entryId}/images`：读取条目图片。
- `POST /sancai/entries/{entryId}/images`：绑定原图。
- `DELETE /sancai/entries/{entryId}/images/{objectId}`：删除图片引用。
- `GET /sancai/entries/{entryId}/visual-assets`：读取视觉资产历史。
- `POST /sancai/entries/{entryId}/visual-assets`：登记视觉资产版本。
- `PUT /sancai/entries/{entryId}/visual-assets/{assetId}/current`：选择当前使用版本。
- `POST /sancai/entries/batch/visual-assets`：批量登记或处理视觉资产结果。

### Export And Showcase API

- `POST /sancai/exports`：创建导出任务。
- `GET /sancai/exports`：查询我的导出记录。
- `GET /sancai/exports/{exportId}`：读取导出记录。
- `DELETE /sancai/exports/{exportId}`：删除导出记录。
- `POST /sancai/showcases`：生成静态展示页面。
- `GET /sancai/showcases`：查询静态展示页面记录。

portal 接口固定使用 `/api/portal/sancai/**`：
- `GET /sancai/categories`
- `GET /sancai/volumes`
- `GET /sancai/entries`
- `GET /sancai/entries/{entryId}`

portal 默认只返回公开且未归档条目；登录用户是否能访问私有内容由资源访问策略决定。

## Infrastructure Layer

- Repository：`SancaiCategoryRepository`、`SancaiVolumeRepository`、`SancaiEntryRepository`、`SancaiEntryVersionRepository`、`SancaiDraftRepository`、`SancaiVisualAssetRepository`、`SancaiExportRepository`、`SancaiShowcaseRepository`。
- Mapper：按表建立 MyBatis Mapper。
- PersistenceAssembler：每个聚合根使用独立 `*PersistenceAssembler`。
- 外部客户端：Storage Application Service、Taxonomy Application Service、Audit Application Service。
- 缓存：门类卷导航、条目详情摘要、静态展示页记录。
- 文件存储：只保存 Storage `objectId`，不保存物理路径。

## Data Ownership

- 本模块拥有：`sancai_category`、`sancai_volume`、`sancai_entry`、`sancai_entry_image`、`sancai_entry_qa`、`sancai_entry_version`、`sancai_entry_draft`、`sancai_visual_asset`、`sancai_export_job`、`sancai_showcase_page`。
- 本模块只读引用：Storage 文件对象、Taxonomy 标签、Auth/Core 用户和权限。
- 禁止跨域直接访问：不得直接读取 Storage 物理路径，不得直接维护 Taxonomy 标签主数据。
- Flyway 脚本归属：`kuzhambu-admin-api` 的 `db/migration/V1__init.sql`，按 Sancai 分段。

## Observability

- 运行日志：记录条目保存、版本恢复、视觉资产登记、导出和静态页面生成失败原因。
- 访问日志：由接口层统一记录。
- 审计日志：条目、图片引用、视觉资产当前版本、导出和静态展示页面生成都应形成业务审计。
- 关键指标：条目数、已发布数、私有数、归档数、翻译覆盖率、配图覆盖率、视觉资产覆盖率、导出数量。

## Acceptance

- 用户能按门类、卷、条目逐级浏览。
- 用户能编辑条目并形成正式版本。
- 自动保存不污染正式版本。
- 用户能恢复草稿和历史版本。
- 条目图片引用不保存物理路径。
- 用户能维护摘要、问答对和视觉资产当前版本。
- 私有、分享和静态展示状态互不隐式影响。
- 导出和静态展示页面生成前按权限过滤内容。
