# Sharing Design

## Purpose

本文档定义内容分享业务域的数据结构和接口设计。本文参考 redesign 中的 Share、批量分享、公开私有说明、分享访问页和访问统计材料，但不照搬其大对象模型、数据结构或接口形态；本设计必须从三类知识库内容生成只读分享链接的业务流程出发，按 DDD 风格重新划分业务边界、聚合和表结构。

Sharing 是分享链接域，不拥有三库内容正文、静态 HTML 展示页面、导出文件、CDN 分发或内容编辑能力。它消费内容域、Auth/Core、Storage 和 Audit 能力，负责跨库分享链接、单链接多内容、公开私有访问控制、风险确认、撤销恢复、过期、只读访问页和访问统计。

## Business Fit Rules

- 业务优先于旧设计：跨库选内容、单链接多内容、批量创建、公开分享、私有分享、过期、撤销、恢复、只读访问页、访问统计和私有内容公开分享风险提示必须能直接对应分享业务。
- 原 redesign 只能作为需求线索和素材来源，不能作为表结构、接口路径、模块划分或聚合边界的设计依据。
- 分享链接公开状态与内容平台内私有状态相互独立；Sharing 不得反向修改内容域公开或私有状态。
- 分享链接、访问凭证和 URL 参数必须使用独立随机 token，不得直接暴露业务 ULID。
- 不为未来增强、路线图或预留能力建表、建接口或增加状态字段。

## Module

- 模块名称：Sharing
- 业务域：sharing
- 对应需求文档：[SHARING-REQUIREMENTS.md](../10-requirements/SHARING-REQUIREMENTS.md)
- 后端 biz 子工程：`kuzhambu-servers/biz/kuzhambu-biz-sharing`
- 后端 infra 子工程：`kuzhambu-servers/infra/kuzhambu-infra-sharing`
- 后台接口入口：`kuzhambu-servers/interfaces/kuzhambu-admin-api`
- 前台接口入口：`kuzhambu-servers/interfaces/kuzhambu-portal-api`
- 前端入口：`kuzhambu-apps/admin-web`、`kuzhambu-apps/portal-web`
- Python worker 能力：无

## Business Boundary

- 本模块负责：分享链接创建、批量创建、分享目标快照、公开或私有访问控制、风险确认、链接复制所需 URL 生成、撤销、恢复、过期判断、只读访问页数据组装、目标删除占位和访问统计。
- 本模块不负责：静态 HTML 展示页面、数据导出文件、CDN 分发、内容编辑、内容公开私有状态维护、通用文件上传和邮件通知。
- 依赖的其他业务域能力：Sancai/Wangqi/Ming Customs 提供分享目标详情、标题、可见性和删除状态；Auth/Core 提供当前用户和权限；Storage 可保存导出的访问统计文件；Audit 记录高价值分享操作。
- 对外提供的业务能力：跨库分享链接、分享访问页、私有分享登录访问、分享统计和批量创建结果。

## DDD Model

### Aggregates

- `SharingLink`：分享链接聚合根，拥有链接 token、所有者、标题、公开私有访问策略、状态、过期时间、风险确认和访问计数。
- `SharingTarget`：分享目标，绑定一个分享链接，保存内容类型、内容业务标识、标题快照、知识库名称、排序和目标可用状态。
- `SharingBatch`：批量创建分享链接任务，记录请求参数、成功数、失败数、取消状态和单项结果。
- `SharingAccessLog`：分享访问日志，记录访问结果、访问者类型、失败原因和访问时间。

### Value Objects

- `SharingContentType`：`SANCAI_ENTRY` / `WANGQI_DOCUMENT` / `MING_CUSTOM`。
- `SharingVisibility`：`PUBLIC` / `PRIVATE`。
- `SharingStatus`：`ACTIVE` / `REVOKED` / `EXPIRED`。
- `SharingTargetStatus`：`AVAILABLE` / `DELETED`。
- `SharingBatchStatus`：`RUNNING` / `COMPLETED` / `CANCELLED`。
- `SharingAccessResult`：`SUCCESS` / `REVOKED` / `EXPIRED` / `NEED_LOGIN` / `FORBIDDEN` / `NOT_FOUND`。

## Data Model

Sharing 表固定使用 `sharing_` 前缀。所有对外标识使用 ULID，数据库内部可使用 `bigint id`。分享访问 URL 使用 `share_token`，不得使用 `share_id`。

### sharing_link

保存分享链接。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `share_id` | `shareId` | 是 | 分享 ULID |
| `share_token` | `shareToken` | 是 | 分享访问随机 token |
| `owner_user_id` | `ownerUserId` | 是 | 分享创建者 |
| `title` | `title` | 是 | 分享标题 |
| `visibility` | `visibility` | 是 | 公开或私有 |
| `status` | `status` | 是 | 链接状态 |
| `contains_private` | `containsPrivate` | 是 | 创建时是否包含平台内私有内容 |
| `risk_confirmed` | `riskConfirmed` | 是 | 是否已完成风险确认 |
| `risk_confirmer_user_id` | `riskConfirmerUserId` | 否 | 风险确认人 |
| `risk_confirmed_at` | `riskConfirmedAt` | 否 | 风险确认时间 |
| `issued_at` | `issuedAt` | 是 | 链接签发时间 |
| `expires_at` | `expiresAt` | 否 | 过期时间 |
| `revoked_at` | `revokedAt` | 否 | 撤销时间 |
| `restored_at` | `restoredAt` | 否 | 恢复时间 |
| `last_accessed_at` | `lastAccessedAt` | 否 | 最近访问时间 |
| `access_count` | `accessCount` | 是 | 成功访问次数 |

约束：
- `share_id` 唯一。
- `share_token` 唯一，必须由安全随机数生成。
- `visibility=PUBLIC` 且 `contains_private=true` 时，必须先完成二次确认。
- `visibility=PRIVATE` 时，仅创建者和管理员可访问。
- `status=REVOKED` 或 `status=EXPIRED` 时链接不可访问。

### sharing_target

保存分享目标。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `target_id` | `targetId` | 是 | 目标 ULID |
| `share_id` | `shareId` | 是 | 分享 ULID |
| `content_type` | `contentType` | 是 | 内容类型 |
| `content_id` | `contentId` | 是 | 内容业务 ULID |
| `knowledge_base` | `knowledgeBase` | 是 | 知识库名称 |
| `title_snapshot` | `titleSnapshot` | 是 | 标题快照 |
| `content_private_snapshot` | `contentPrivateSnapshot` | 是 | 创建时内容是否为平台内私有 |
| `target_status` | `targetStatus` | 是 | 目标可用状态 |
| `sort_order` | `sortOrder` | 是 | 展示排序 |
| `snapshotted_at` | `snapshottedAt` | 是 | 快照时间 |

约束：
- `target_id` 唯一。
- 同一分享内 `content_type + content_id` 不得重复。
- 分享目标被删除时，本表标记 `target_status=DELETED`，访问页展示占位，其余目标继续展示。

### sharing_batch

保存批量创建分享链接任务。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `batch_id` | `batchId` | 是 | 批量任务 ULID |
| `requester_user_id` | `requesterUserId` | 是 | 发起用户 |
| `visibility` | `visibility` | 是 | 本批次默认公开或私有 |
| `total_count` | `totalCount` | 是 | 总数 |
| `success_count` | `successCount` | 是 | 成功数 |
| `failed_count` | `failedCount` | 是 | 失败数 |
| `status` | `status` | 是 | 批量状态 |
| `requested_at` | `requestedAt` | 是 | 发起时间 |
| `cancelled_at` | `cancelledAt` | 否 | 取消时间 |
| `completed_at` | `completedAt` | 否 | 完成时间 |

约束：
- `batch_id` 唯一。
- 单条失败不得影响其他内容。
- 用户取消后，已创建成功的分享链接必须保留。

### sharing_batch_item

保存批量创建单项结果。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `batch_item_id` | `batchItemId` | 是 | 批量单项 ULID |
| `batch_id` | `batchId` | 是 | 批量任务 ULID |
| `content_type` | `contentType` | 是 | 内容类型 |
| `content_id` | `contentId` | 是 | 内容业务 ULID |
| `share_id` | `shareId` | 否 | 成功创建的分享 ULID |
| `item_status` | `itemStatus` | 是 | `PENDING` / `SUCCESS` / `FAILED` |
| `failure_reason` | `failureReason` | 否 | 失败原因 |
| `processed_at` | `processedAt` | 否 | 处理时间 |

约束：
- `batch_item_id` 唯一。
- 批量结果必须展示成功数、失败数和失败原因。

### sharing_access_log

保存分享访问日志。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `access_id` | `accessId` | 是 | 访问 ULID |
| `share_id` | `shareId` | 是 | 分享 ULID |
| `visitor_user_id` | `visitorUserId` | 否 | 登录访问用户 |
| `access_result` | `accessResult` | 是 | 访问结果 |
| `failure_reason` | `failureReason` | 否 | 失败原因 |
| `accessed_at` | `accessedAt` | 是 | 访问时间 |

约束：
- `access_id` 唯一。
- 只有成功访问才递增 `sharing_link.access_count`。
- 访问日志不得保存分享目标正文。

## Risk Prompt Text

公开分享私有内容时固定提示：

`你正在公开分享平台内私有内容。创建后，获得链接的人无需登录即可查看这些内容。此操作不会改变内容在平台内的私有状态，但会绕过平台内列表、搜索和问答的可见性限制。是否继续？`

私有内容创建私有分享时固定提示：

`该分享包含平台内私有内容。私有分享仅创建者和管理员可访问，内容在平台内的私有状态不变。`

## Application Layer

- `SharingLinkApplicationService`：创建分享、读取分享详情、撤销、恢复、过期判断和分享 URL 生成。
- `SharingTargetApplicationService`：目标快照、目标可用性检查、删除占位和只读访问页数据组装。
- `SharingBatchApplicationService`：批量创建、取消、单项失败隔离和批量结果查询。
- `SharingAccessApplicationService`：公开访问、私有访问登录校验、访问统计和访问日志。
- `SharingRiskApplicationService`：私有内容风险判断、固定文案返回和确认记录。

事务边界：
- 创建分享时必须同时保存链接和目标快照。
- 公开分享私有内容必须在保存前完成风险确认。
- 批量创建按单项独立事务执行，单项失败不得回滚已成功链接。
- 访问成功后递增访问计数并写入访问日志；日志失败不得阻断只读展示。
- 撤销、恢复和过期状态变更必须记录 Audit。

## Interface Layer

后台接口固定使用 `/api/admin/sharing/**`，portal 接口固定使用 `/api/portal/sharing/**`。

### Admin Sharing API

- `POST /sharing/links`：创建单个分享链接。
- `GET /sharing/links`：查询当前用户分享列表。
- `GET /sharing/links/{shareId}`：查询分享详情。
- `POST /sharing/links/{shareId}/revoke`：撤销分享。
- `POST /sharing/links/{shareId}/restore`：恢复分享。
- `GET /sharing/links/{shareId}/stats`：查询访问统计。

### Batch API

- `POST /sharing/batches`：批量创建分享链接。
- `GET /sharing/batches/{batchId}`：查询批量创建结果。
- `POST /sharing/batches/{batchId}/cancel`：取消批量创建。

### Risk API

- `POST /sharing/risk-check`：检查选中目标是否需要风险确认并返回固定文案。
- `POST /sharing/risk-confirmations`：确认私有内容分享风险。

### Portal Access API

- `GET /sharing/{shareToken}`：访问分享只读页。
- `GET /sharing/{shareToken}/targets`：读取分享目标列表和占位状态。

portal 访问使用 `shareToken`，不得暴露 `shareId`。

## Infrastructure Layer

- Repository：`SharingLinkRepository`、`SharingTargetRepository`、`SharingBatchRepository`、`SharingBatchItemRepository`、`SharingAccessLogRepository`。
- Mapper：按表建立 MyBatis Mapper。
- PersistenceAssembler：每个聚合根使用独立 `*PersistenceAssembler`。
- 外部客户端：Sancai Application Service、Wangqi Application Service、Ming Customs Application Service、Auth/Core Application Service、Storage Application Service、Audit Application Service。
- 缓存：分享详情和目标快照可短缓存；撤销、过期和私有访问判断不得依赖过期缓存。

## Data Ownership

- 本模块拥有：`sharing_link`、`sharing_target`、`sharing_batch`、`sharing_batch_item`、`sharing_access_log`。
- 本模块只读引用：三才、王圻、明代习俗内容，Auth/Core 用户和权限，Storage 文件对象。
- 禁止跨域直接访问：不得直接修改内容表、导出表、静态展示页面、文件物理数据或内容公开私有状态。
- Flyway 脚本归属：`kuzhambu-admin-api` 的 `db/migration/V1__init.sql`，按 Sharing 分段。

## Observability

- 运行日志：记录分享创建失败、目标快照失败、私有访问拒绝、过期判断异常和批量单项失败。
- 访问日志：由接口层统一记录。
- 审计日志：创建公开分享私有内容、撤销、恢复、批量创建和管理员访问私有分享统计应形成业务审计。
- 关键指标：有效链接数、公开链接数、私有链接数、过期链接数、访问次数、批量成功率和目标删除占位次数。

## Acceptance

- 用户能创建分享链接并复制。
- 用户能对多选内容批量创建分享链接。
- 批量创建分享链接完成后，用户能看到成功数、失败数和失败原因。
- 公开分享私有内容必须出现固定风险确认文案。
- 私有分享未登录时引导登录，非创建者且非管理员不可访问。
- 被撤销或过期的链接不可访问。
- 分享目标被删除时访问页显示占位提示，其余目标正常展示。
