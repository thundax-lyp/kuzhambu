# Wangqi Document Design

## Purpose

本文档定义王圻文档知识库的数据结构和接口设计。本文参考 redesign 中的王圻材料，但不照搬其大对象模型、数据结构或接口形态；本设计必须从王圻文档管理业务出发，按 DDD 风格重新划分业务边界、聚合和表结构。

王圻文档域是文档内容域，不承载 AI 执行、标签治理、跨库搜索、跨库问答、分享链接或通用文件存储。它只拥有王圻文档正文、原始文件引用、摘要、问答对、版本历史、公开私有状态、时间线浏览字段和王圻范围内的导出记录。

## Business Fit Rules

- 业务优先于旧设计：文档、正文、原始文件、摘要、标签展示、问答对、版本、时间线和导出必须能直接对应王圻文档知识库的实际操作。
- 原 redesign 只能作为需求线索和素材来源，不能作为表结构、接口路径、模块划分或聚合边界的设计依据。
- 不把跨域实现状态写入王圻文档主数据；AI 候选、搜索索引、问答会话、分享链接和存储实现由各自业务域维护。
- 不为未来增强、路线图或预留能力建表、建接口或增加状态字段。
- 批量处理只作为王圻文档公开状态等业务操作的批量入口，不建立独立 Batch Task 业务域。

## Module

- 模块名称：Wangqi Document
- 业务域：wangqi
- 对应需求文档：[WANGQI-DOCUMENT-REQUIREMENTS.md](../10-requirements/WANGQI-DOCUMENT-REQUIREMENTS.md)
- 后端 biz 子工程：`kuzhambu-servers/biz/kuzhambu-biz-wangqi`
- 后端 infra 子工程：`kuzhambu-servers/infra/kuzhambu-infra-wangqi`
- 后台接口入口：`kuzhambu-servers/interfaces/kuzhambu-admin-api`
- 前台接口入口：`kuzhambu-servers/interfaces/kuzhambu-portal-api`
- 前端入口：`kuzhambu-apps/admin-web`、`kuzhambu-apps/portal-web`
- Python worker 能力：无直接归属；AI 处理由 AI Refinement / workers 提供候选结果

## Business Boundary

- 本模块负责：王圻文档创建、查看、编辑、删除、正文阅读、原始文件对象引用、文件替换记录、摘要、标签展示缓存、问答对、版本历史、版本恢复、公开私有状态、时间线浏览和王圻导出记录。
- 本模块不负责：AI 模型调用执行、标签权威治理、跨库搜索、跨库问答会话、分享链接、通用文件上传、文件物理存储、HTML 导出模板资源管理。
- 依赖的其他业务域能力：Storage 提供文件对象；Taxonomy 提供标签权威数据；AI Refinement 提供候选结果；QA 提供单文档问答会话；Audit 记录写操作；Auth/Core 提供操作者和权限。
- 对外提供的业务能力：王圻文档管理、文档上下文精修、版本恢复、时间线浏览、单文档问答上下文和导出。

## DDD Model

### Aggregates

- `WangqiDocument`：文档聚合根，拥有标题、正文、摘要、标签展示缓存、当前原始文件引用、时间信息、公开私有状态和字数统计。
- `WangqiDocumentQaPair`：文档上下文问答对，绑定一个文档，支持人工维护和 AI 候选确认后写入。
- `WangqiDocumentVersion`：文档正式版本快照，服务于版本历史、对比和恢复。
- `WangqiExportJob`：王圻导出产物记录，覆盖 CSV、JSON 和 HTML 设定集。

### Value Objects

- `WangqiDocumentId`：文档 ULID。
- `ContentVisibility`：`PUBLIC` / `PRIVATE`。
- `WangqiContentFormat`：`MARKDOWN` / `TEXT`。
- `WangqiQaSource`：`MANUAL` / `AI_APPLIED`。
- `WangqiVersionChangeType`：`MANUAL_SAVE` / `AI_APPLY` / `RESTORE`。
- `WangqiExportType`：`CSV` / `JSON` / `HTML_DATASET`。
- `WangqiExportScopeType`：`FILTER` / `SELECTED`。

## Data Model

Wangqi 表固定使用 `wangqi_` 前缀。所有对外标识使用 ULID，数据库内部可使用 `bigint id`。

### wangqi_document

保存王圻文档主数据。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `document_id` | `documentId` | 是 | 文档 ULID |
| `title` | `title` | 是 | 文档标题 |
| `content` | `content` | 否 | 文档正文 |
| `content_format` | `contentFormat` | 是 | `MARKDOWN` / `TEXT` |
| `summary` | `summary` | 否 | 文档摘要 |
| `tags_snapshot` | `tagsSnapshot` | 否 | 标签展示缓存 JSON |
| `file_object_id` | `fileObjectId` | 否 | 当前原始文件 Storage 对象 ULID |
| `word_count` | `wordCount` | 是 | 字数统计 |
| `document_time` | `documentTime` | 否 | 文档时间线排序时间 |
| `visibility` | `visibility` | 是 | `PUBLIC` / `PRIVATE` |
| `owner_user_id` | `ownerUserId` | 是 | 归属用户 ULID |
| `current_version` | `currentVersion` | 是 | 当前正式版本号 |

约束：
- `document_id` 唯一。
- 王圻文档没有草稿、发布或归档生命周期；平台内可见性只由 `visibility` 表达。
- 文件缺失或 `file_object_id` 为空时，正文仍应可阅读。
- `tags_snapshot` 只做展示缓存，标签权威关系由 Taxonomy 维护。

### wangqi_document_qa

保存文档上下文内联问答对。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `qa_id` | `qaId` | 是 | 问答对 ULID |
| `document_id` | `documentId` | 是 | 文档 ULID |
| `question` | `question` | 是 | 问题 |
| `answer` | `answer` | 是 | 答案 |
| `source` | `source` | 是 | `MANUAL` / `AI_APPLIED` |
| `sort_order` | `sortOrder` | 是 | 排序 |

约束：
- 问答对属于文档上下文，不是独立数据精修工作台对象。
- 用户放弃 AI 候选问答对时，不得修改已有问答对。

### wangqi_document_version

保存文档正式版本快照。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `version_id` | `versionId` | 是 | 版本 ULID |
| `document_id` | `documentId` | 是 | 文档 ULID |
| `version_no` | `versionNo` | 是 | 版本号 |
| `snapshot_json` | `snapshotJson` | 是 | 文档快照 JSON |
| `change_type` | `changeType` | 是 | `MANUAL_SAVE` / `AI_APPLY` / `RESTORE` |
| `change_summary` | `changeSummary` | 否 | 变更摘要 |
| `versioned_at` | `versionedAt` | 是 | 版本生成时间 |

约束：
- 手动保存、AI 结果应用和历史恢复产生正式版本。
- 恢复历史版本本身也生成新版本。
- 文件替换不得破坏正文；正式保存时快照必须包含正文和当前文件对象引用。

### wangqi_export_job

保存王圻导出产物记录。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `export_id` | `exportId` | 是 | 导出 ULID |
| `export_type` | `exportType` | 是 | `CSV` / `JSON` / `HTML_DATASET` |
| `scope_type` | `scopeType` | 是 | `FILTER` / `SELECTED` |
| `scope_json` | `scopeJson` | 是 | 范围参数 JSON |
| `object_id` | `objectId` | 否 | Storage 导出产物对象 |
| `document_count` | `documentCount` | 是 | 文档数量 |
| `contains_private` | `containsPrivate` | 是 | 是否包含私有文档 |
| `status` | `status` | 是 | `PENDING` / `DONE` / `FAILED` / `EXPIRED` |
| `content_changed` | `contentChanged` | 是 | 导出后内容是否可能已变更 |
| `requester_user_id` | `requesterUserId` | 是 | 导出请求用户 |
| `requested_at` | `requestedAt` | 是 | 导出请求时间 |
| `expires_at` | `expiresAt` | 是 | 过期时间 |

约束：
- 导出前必须按权限过滤文档。
- 包含私有文档时必须二次确认。
- 过期导出记录不可继续下载。
- 导出完成后，导出范围内文档发生正式变更时，相关未过期导出记录应标记 `content_changed=true`；该标记只用于提示“内容可能已变更”，不影响下载。
- HTML 设定集模板是随系统发布的静态资源，不属于本模块数据。

## Application Layer

- `WangqiDocumentApplicationService`：文档创建、编辑、删除、详情读取、列表查询、公开私有状态变更和批量状态变更。
- `WangqiDocumentFileApplicationService`：原始文件关联、替换和 Storage 引用维护。
- `WangqiDocumentRefinementApplicationService`：摘要、标签展示缓存和问答对的确认应用。
- `WangqiDocumentVersionApplicationService`：版本创建、版本列表、版本对比和历史恢复。
- `WangqiDocumentTimelineApplicationService`：时间线查询。
- `WangqiExportApplicationService`：CSV、JSON 和 HTML 设定集导出。

事务边界：
- 文档正式保存必须更新主数据并创建版本快照。
- AI 候选结果只有用户确认应用后才写入摘要、标签展示缓存或问答对。
- 文件替换必须先获得新 Storage 对象，再切换当前文件引用并维护 Storage 引用关系。
- 删除、文件替换、公开私有状态变更、版本恢复和导出创建必须记录 Audit。

## Interface Layer

后台接口固定使用 `/api/admin/wangqi/**`。

### Document API

- `GET /wangqi/documents`：分页查询文档，支持关键词、公开状态、标签、时间范围和排序。
- `GET /wangqi/documents/timeline`：按时间线查询文档。
- `GET /wangqi/documents/{documentId}`：读取文档详情。
- `POST /wangqi/documents`：创建文档。
- `PUT /wangqi/documents/{documentId}`：保存文档正式版本。
- `DELETE /wangqi/documents/{documentId}`：删除文档。
- `PUT /wangqi/documents/{documentId}/visibility`：修改公开或私有状态。
- `POST /wangqi/documents/batch/visibility`：批量修改公开或私有状态。

### File API

- `PUT /wangqi/documents/{documentId}/file`：关联或替换原始文件对象。
- `DELETE /wangqi/documents/{documentId}/file`：移除原始文件引用。

### Summary Tag And QA API

- `PUT /wangqi/documents/{documentId}/summary`：保存摘要。
- `PUT /wangqi/documents/{documentId}/tags`：保存标签展示缓存并同步请求 Taxonomy 维护权威关系。
- `GET /wangqi/documents/{documentId}/qa-pairs`：读取问答对。
- `POST /wangqi/documents/{documentId}/qa-pairs`：新增问答对。
- `PUT /wangqi/documents/{documentId}/qa-pairs/{qaId}`：更新问答对。
- `DELETE /wangqi/documents/{documentId}/qa-pairs/{qaId}`：删除问答对。
- `POST /wangqi/documents/{documentId}/ai/summary`：触发摘要候选生成。
- `POST /wangqi/documents/{documentId}/ai/tags`：触发标签候选生成。
- `POST /wangqi/documents/{documentId}/ai/qa-pairs`：触发问答对候选生成。

### Version API

- `GET /wangqi/documents/{documentId}/versions`：读取版本历史。
- `GET /wangqi/documents/{documentId}/versions/{versionId}`：读取版本详情。
- `POST /wangqi/documents/{documentId}/versions/{versionId}/restore`：恢复历史版本。

### Export API

- `POST /wangqi/exports`：创建导出任务。
- `GET /wangqi/exports`：查询我的导出记录。
- `GET /wangqi/exports/{exportId}`：读取导出记录。
- `DELETE /wangqi/exports/{exportId}`：删除导出记录。

portal 接口固定使用 `/api/portal/wangqi/**`：
- `GET /wangqi/documents`
- `GET /wangqi/documents/timeline`
- `GET /wangqi/documents/{documentId}`

portal 默认只返回公开且未删除文档；登录用户是否能访问私有内容由资源访问策略决定。

## Infrastructure Layer

- Repository：`WangqiDocumentRepository`、`WangqiDocumentQaRepository`、`WangqiDocumentVersionRepository`、`WangqiExportRepository`。
- Mapper：按表建立 MyBatis Mapper。
- PersistenceAssembler：每个聚合根使用独立 `*PersistenceAssembler`。
- 外部客户端：Storage Application Service、Taxonomy Application Service、AI Refinement Application Service、QA Application Service、Audit Application Service。
- 缓存：文档详情摘要、时间线列表、导出记录。
- 文件存储：只保存 Storage `objectId`，不保存物理路径。

## Data Ownership

- 本模块拥有：`wangqi_document`、`wangqi_document_qa`、`wangqi_document_version`、`wangqi_export_job`。
- 本模块只读引用：Storage 文件对象、Taxonomy 标签、Auth/Core 用户和权限。
- 禁止跨域直接访问：不得直接读取 Storage 物理路径，不得直接维护 Taxonomy 标签主数据，不得直接写入搜索索引或问答会话。
- Flyway 脚本归属：`kuzhambu-admin-api` 的 `db/migration/V1__init.sql`，按 Wangqi 分段。

## Observability

- 运行日志：记录文档保存、文件替换、版本恢复、AI 候选应用、导出失败原因。
- 访问日志：由接口层统一记录。
- 审计日志：文档、文件引用、摘要、标签、问答对、公开私有状态、版本恢复和导出创建都应形成业务审计。
- 关键指标：文档数、私有数、正文覆盖率、原始文件覆盖率、摘要覆盖率、问答对覆盖率、导出数量。

## Acceptance

- 用户能新增文档并维护正文。
- 用户能关联或替换文档原始文件，文件替换不得破坏正文。
- 用户能在文档详情中查看正文、摘要、标签和问答对。
- 用户能触发摘要、标签和问答对生成，并在确认后应用。
- 用户能从文档详情发起围绕当前文档的问答。
- 用户能在列表浏览和时间线浏览之间切换。
- 用户能查看和恢复历史版本。
- 用户能多选文档并批量修改公开或私有状态。
- 非授权用户无法在列表、搜索、单文档问答或默认导出中使用私有文档。
- 用户能选择王圻文档范围和格式后生成导出产物。
- 过期导出记录不可继续下载。
