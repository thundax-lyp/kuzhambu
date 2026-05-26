# Ming Customs Design

## Purpose

本文档定义明代习俗知识库的数据结构和接口设计。本文参考 redesign 中的明代习俗材料，但不照搬其大对象模型、数据结构或接口形态；本设计必须从明代社会习俗专题内容管理业务出发，按 DDD 风格重新划分业务边界、聚合和表结构。

明代习俗域是专题内容域，不承载 AI 执行、标签治理、跨库搜索、跨库问答、分享链接或通用文件存储。它只拥有习俗条目、概述、正文、分类、关键词、原文摘录、问答对、版本历史、公开私有状态和明代习俗范围内的导出记录。

## Business Fit Rules

- 业务优先于旧设计：习俗条目、概述、正文、分类、关键词、标签展示、原文摘录、详情弹窗、标签云筛选、问答对、版本和导出必须能直接对应明代习俗知识库的实际操作。
- 原 redesign 只能作为需求线索和素材来源，不能作为表结构、接口路径、模块划分或聚合边界的设计依据。
- 不把跨域实现状态写入明代习俗主数据；AI 候选、搜索索引、问答会话、分享链接和标签权威关系由各自业务域维护。
- 不为未来增强、路线图或预留能力建表、建接口或增加状态字段。
- 批量处理只作为明代习俗公开状态等业务操作的批量入口，不建立独立 Batch Task 业务域。

## Module

- 模块名称：Ming Customs
- 业务域：mingcustoms
- 对应需求文档：[MING-CUSTOMS-REQUIREMENTS.md](../10-requirements/MING-CUSTOMS-REQUIREMENTS.md)
- 后端 biz 子工程：`kuzhambu-servers/biz/kuzhambu-biz-mingcustoms`
- 后端 infra 子工程：`kuzhambu-servers/infra/kuzhambu-infra-mingcustoms`
- 后台接口入口：`kuzhambu-servers/interfaces/kuzhambu-admin-api`
- 前台接口入口：`kuzhambu-servers/interfaces/kuzhambu-portal-api`
- 前端入口：`kuzhambu-apps/admin-web`、`kuzhambu-apps/portal-web`
- Python worker 能力：无直接归属；AI 处理由 AI Refinement / workers 提供候选结果

## Business Boundary

- 本模块负责：明代习俗条目创建、查看、编辑、删除、概述、正文、分类、关键词、标签展示缓存、原文摘录、问答对、版本历史、版本恢复、公开私有状态、标签云筛选入口和明代习俗导出记录。
- 本模块不负责：AI 模型调用执行、标签权威治理、跨库搜索、跨库问答会话、分享链接、通用文件上传、三才静态展示页面生成、王圻文档文件替换。
- 依赖的其他业务域能力：Taxonomy 提供标签权威数据和标签云统计；AI Refinement 提供候选结果；Audit 记录写操作；Auth/Core 提供操作者和权限。
- 对外提供的业务能力：明代习俗专题内容管理、详情阅读、标签云筛选、上下文精修、版本恢复和导出。

## DDD Model

### Aggregates

- `MingCustomEntry`：习俗条目聚合根，拥有标题、概述、正文、分类、关键词、标签展示缓存、原文摘录、公开私有状态和字数统计。
- `MingCustomQaPair`：习俗上下文问答对，绑定一个习俗条目，支持人工维护和 AI 候选确认后写入。
- `MingCustomVersion`：习俗正式版本快照，服务于版本历史、对比和恢复。
- `MingCustomExportJob`：明代习俗导出产物记录，覆盖 CSV、JSON 和 HTML 设定集。

### Value Objects

- `MingCustomId`：习俗条目 ULID。
- `ContentVisibility`：`PUBLIC` / `PRIVATE`。
- `MingCustomCategory`：习俗分类。
- `MingCustomKeywordSet`：关键词集合。
- `MingCustomQaSource`：`MANUAL` / `AI_APPLIED`。
- `MingCustomVersionChangeType`：`MANUAL_SAVE` / `AI_APPLY` / `RESTORE`。
- `MingCustomExportType`：`CSV` / `JSON` / `HTML_DATASET`。
- `MingCustomExportScopeType`：`CATEGORY` / `TAG` / `FILTER` / `SELECTED`。

## Data Model

Ming Customs 表固定使用 `ming_customs_` 前缀。所有对外标识使用 ULID，数据库内部可使用 `bigint id`。

### ming_customs_entry

保存明代习俗条目主数据。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `custom_id` | `customId` | 是 | 习俗条目 ULID |
| `title` | `title` | 是 | 习俗标题 |
| `summary` | `summary` | 否 | 习俗概述 |
| `content` | `content` | 否 | Markdown 正文 |
| `category` | `category` | 否 | 习俗分类 |
| `chapter` | `chapter` | 否 | 来源章 |
| `section` | `section` | 否 | 来源节 |
| `keywords_snapshot` | `keywordsSnapshot` | 否 | 关键词展示缓存 JSON |
| `tags_snapshot` | `tagsSnapshot` | 否 | 标签展示缓存 JSON |
| `original_excerpts` | `originalExcerpts` | 否 | 原文摘录 JSON |
| `word_count` | `wordCount` | 是 | 字数统计 |
| `visibility` | `visibility` | 是 | `PUBLIC` / `PRIVATE` |
| `owner_user_id` | `ownerUserId` | 是 | 创建者用户 ULID |
| `current_version` | `currentVersion` | 是 | 当前正式版本号 |
| `created_at` | `createdAt` | 是 | 创建时间 |
| `updated_at` | `updatedAt` | 是 | 更新时间 |
| `deleted_at` | `deletedAt` | 否 | 删除时间 |

约束：
- `custom_id` 唯一。
- 明代习俗没有草稿、发布或归档生命周期；平台内可见性只由 `visibility` 表达。
- Markdown 渲染必须在接口或前端展示层进行安全处理，数据库保存原始 Markdown 内容。
- 无标签、无关键词、无原文摘录的习俗条目仍可保存。
- `tags_snapshot` 只做展示缓存，标签权威关系和标签云统计由 Taxonomy 维护。

### ming_customs_qa

保存习俗上下文内联问答对。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `qa_id` | `qaId` | 是 | 问答对 ULID |
| `custom_id` | `customId` | 是 | 习俗条目 ULID |
| `question` | `question` | 是 | 问题 |
| `answer` | `answer` | 是 | 答案 |
| `source` | `source` | 是 | `MANUAL` / `AI_APPLIED` |
| `sort_order` | `sortOrder` | 是 | 排序 |
| `created_at` | `createdAt` | 是 | 创建时间 |
| `updated_at` | `updatedAt` | 是 | 更新时间 |

约束：
- 问答对属于习俗上下文，不是独立数据精修工作台对象。
- 用户放弃 AI 候选问答对时，不得修改已有问答对。

### ming_customs_version

保存习俗正式版本快照。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `version_id` | `versionId` | 是 | 版本 ULID |
| `custom_id` | `customId` | 是 | 习俗条目 ULID |
| `version_no` | `versionNo` | 是 | 版本号 |
| `snapshot_json` | `snapshotJson` | 是 | 习俗快照 JSON |
| `change_type` | `changeType` | 是 | `MANUAL_SAVE` / `AI_APPLY` / `RESTORE` |
| `change_summary` | `changeSummary` | 否 | 变更摘要 |
| `created_by` | `createdBy` | 是 | 操作者用户 ULID |
| `created_at` | `createdAt` | 是 | 创建时间 |

约束：
- 手动保存、AI 结果应用和历史恢复产生正式版本。
- 恢复历史版本本身也生成新版本。

### ming_customs_export_job

保存明代习俗导出产物记录。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `export_id` | `exportId` | 是 | 导出 ULID |
| `export_type` | `exportType` | 是 | `CSV` / `JSON` / `HTML_DATASET` |
| `scope_type` | `scopeType` | 是 | `CATEGORY` / `TAG` / `FILTER` / `SELECTED` |
| `scope_json` | `scopeJson` | 是 | 范围参数 JSON |
| `object_id` | `objectId` | 否 | Storage 导出产物对象 |
| `custom_count` | `customCount` | 是 | 习俗条目数量 |
| `contains_private` | `containsPrivate` | 是 | 是否包含私有习俗 |
| `status` | `status` | 是 | `PENDING` / `DONE` / `FAILED` / `EXPIRED` |
| `created_by` | `createdBy` | 是 | 创建人 |
| `created_at` | `createdAt` | 是 | 创建时间 |
| `expires_at` | `expiresAt` | 是 | 过期时间 |

约束：
- 导出前必须按权限过滤习俗条目。
- 包含私有习俗时必须二次确认。
- 过期导出记录不可继续下载。
- HTML 设定集模板是随系统发布的静态资源，不属于本模块数据。

## Application Layer

- `MingCustomEntryApplicationService`：习俗创建、编辑、删除、详情读取、列表查询、详情弹窗数据、公开私有状态变更和批量状态变更。
- `MingCustomRefinementApplicationService`：摘要、标签展示缓存、关键词和问答对的确认应用。
- `MingCustomTagCloudApplicationService`：标签云查询和标签筛选入口。
- `MingCustomVersionApplicationService`：版本创建、版本列表、版本对比和历史恢复。
- `MingCustomExportApplicationService`：CSV、JSON 和 HTML 设定集导出。

事务边界：
- 习俗正式保存必须更新主数据并创建版本快照。
- AI 候选结果只有用户确认应用后才写入摘要、标签展示缓存、关键词或问答对。
- 删除、公开私有状态变更、版本恢复和导出创建必须记录 Audit。

## Interface Layer

后台接口固定使用 `/api/admin/ming-customs/**`。

### Entry API

- `GET /ming-customs/entries`：分页查询习俗，支持关键词、分类、标签、公开状态和排序。
- `GET /ming-customs/entries/{customId}`：读取习俗详情。
- `POST /ming-customs/entries`：创建习俗。
- `PUT /ming-customs/entries/{customId}`：保存习俗正式版本。
- `DELETE /ming-customs/entries/{customId}`：删除习俗。
- `PUT /ming-customs/entries/{customId}/visibility`：修改公开或私有状态。
- `POST /ming-customs/entries/batch/visibility`：批量修改公开或私有状态。
- `GET /ming-customs/tag-cloud`：查询标签云。

### Summary Tag And QA API

- `PUT /ming-customs/entries/{customId}/summary`：保存摘要。
- `PUT /ming-customs/entries/{customId}/tags`：保存标签展示缓存并同步请求 Taxonomy 维护权威关系。
- `PUT /ming-customs/entries/{customId}/keywords`：保存关键词展示缓存。
- `GET /ming-customs/entries/{customId}/qa-pairs`：读取问答对。
- `POST /ming-customs/entries/{customId}/qa-pairs`：新增问答对。
- `PUT /ming-customs/entries/{customId}/qa-pairs/{qaId}`：更新问答对。
- `DELETE /ming-customs/entries/{customId}/qa-pairs/{qaId}`：删除问答对。
- `POST /ming-customs/entries/{customId}/ai/summary`：触发摘要候选生成。
- `POST /ming-customs/entries/{customId}/ai/tags`：触发标签候选生成。
- `POST /ming-customs/entries/{customId}/ai/qa-pairs`：触发问答对候选生成。

### Version API

- `GET /ming-customs/entries/{customId}/versions`：读取版本历史。
- `GET /ming-customs/entries/{customId}/versions/{versionId}`：读取版本详情。
- `POST /ming-customs/entries/{customId}/versions/{versionId}/restore`：恢复历史版本。

### Export API

- `POST /ming-customs/exports`：创建导出任务。
- `GET /ming-customs/exports`：查询我的导出记录。
- `GET /ming-customs/exports/{exportId}`：读取导出记录。
- `DELETE /ming-customs/exports/{exportId}`：删除导出记录。

portal 接口固定使用 `/api/portal/ming-customs/**`：
- `GET /ming-customs/entries`
- `GET /ming-customs/entries/{customId}`
- `GET /ming-customs/tag-cloud`

portal 默认只返回公开且未删除习俗；登录用户是否能访问私有内容由资源访问策略决定。

## Infrastructure Layer

- Repository：`MingCustomEntryRepository`、`MingCustomQaRepository`、`MingCustomVersionRepository`、`MingCustomExportRepository`。
- Mapper：按表建立 MyBatis Mapper。
- PersistenceAssembler：每个聚合根使用独立 `*PersistenceAssembler`。
- 外部客户端：Taxonomy Application Service、AI Refinement Application Service、Audit Application Service。
- 缓存：习俗详情摘要、标签云、导出记录。
- 文件存储：本模块不直接管理原始文件。

## Data Ownership

- 本模块拥有：`ming_customs_entry`、`ming_customs_qa`、`ming_customs_version`、`ming_customs_export_job`。
- 本模块只读引用：Taxonomy 标签、Auth/Core 用户和权限。
- 禁止跨域直接访问：不得直接维护 Taxonomy 标签主数据，不得直接写入搜索索引或问答会话。
- Flyway 脚本归属：`kuzhambu-admin-api` 的 `db/migration/V1__init.sql`，按 Ming Customs 分段。

## Observability

- 运行日志：记录习俗保存、版本恢复、AI 候选应用、标签云查询异常和导出失败原因。
- 访问日志：由接口层统一记录。
- 审计日志：习俗、摘要、标签、关键词、问答对、公开私有状态、版本恢复和导出创建都应形成业务审计。
- 关键指标：习俗数、私有数、摘要覆盖率、标签覆盖率、问答对覆盖率、导出数量。

## Acceptance

- 用户能按标签筛选习俗条目。
- 用户能通过关键词搜索习俗条目。
- 用户能打开习俗详情并阅读安全渲染后的内容。
- 用户无权访问的私有习俗不会出现在列表、搜索和标签云中。
- 用户能在习俗上下文中维护摘要和问答对。
- 用户能维护习俗内容并查看历史版本。
- 用户能多选习俗条目并批量修改公开或私有状态。
- 用户能选择明代习俗范围和格式后生成导出产物。
- 过期导出记录不可继续下载。
