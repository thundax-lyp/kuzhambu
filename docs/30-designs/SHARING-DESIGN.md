# Sharing Design

## Purpose

本文档定义 Sharing 业务域的数据结构与接口设计。本文参考 redesign 中的 Share、批量分享、公开私有说明、分享访问页和访问统计材料，但不照搬其大对象模型、数据结构或接口形态；本设计必须从已分享内容和分享链接资产的集中管理业务出发，按 DDD 风格重新划分数据归属、接口和表结构。

Sharing 是附着在内容域之上的独立业务域，不拥有三库内容正文、静态 HTML 展示页面、导出文件、CDN 分发或内容编辑能力。它消费内容域、Auth/Core、Storage 和 Audit 能力，负责跨库分享链接资产、单链接多内容、公开私有访问控制、风险确认、集中管理、撤销恢复、过期、只读访问页、分享页内 Storage 对象访问和访问统计。

## Business Fit Rules

- 业务优先于旧设计：已分享内容集中管理、跨库选内容、单链接多内容、集合创建、公开分享、私有分享、过期、撤销、恢复、只读访问页、访问统计和私有内容公开分享风险提示必须能直接对应 Sharing 业务。
- 原 redesign 只能作为需求线索和素材来源，不能作为表结构、接口路径、模块划分或聚合边界的设计依据。
- 分享链接公开状态与内容平台内私有状态相互独立；Sharing 不得反向修改内容域公开或私有状态。
- 集合创建分享链接只是多条单篇分享创建请求的集合操作，不建立独立任务、任务状态或结果表。
- 分享访问页内图片、附件等 Storage 对象必须通过 Sharing 自己的访问接口读取；Sharing 校验分享链接和目标后，再调用 Storage Application Service，不暴露通用公开 Storage 访问接口。
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

- 本模块负责：已创建分享链接集中查看、分享链接创建、集合创建分享链接、分享目标快照、公开或私有访问控制、风险确认、链接复制所需 URL 生成、撤销、恢复、过期判断、只读访问页数据组装、分享页内 Storage 对象访问、目标删除占位和访问统计。
- 本模块不负责：静态 HTML 展示页面、数据导出文件、CDN 分发、内容编辑、内容公开私有状态维护、通用文件上传和邮件通知。
- 依赖的其他业务域能力：Sancai/Wangqi/Ming Customs 提供分享目标详情、标题、可见性、删除状态和对象引用校验；Auth/Core 提供当前用户和权限；Storage 提供对象内容读取；Audit 记录高价值分享操作。
- 对外提供的业务能力：已分享内容集中管理、跨库分享链接、分享访问页、私有分享登录访问、分享统计和集合创建结果。

## DDD Model

### Aggregates

- `SharingLink`：分享链接聚合根，拥有链接 token、所有者、标题、公开私有访问策略、状态、过期时间、风险确认和访问计数。
- `SharingTarget`：分享目标，绑定一个分享链接，保存内容类型、内容业务标识、标题快照、知识库名称、排序和目标可用状态。

### Value Objects

- `SharingContentType`：`SANCAI_ENTRY` / `WANGQI_DOCUMENT` / `MING_CUSTOM`。
- `SharingVisibility`：`PUBLIC` / `PRIVATE`。
- `SharingStatus`：`ACTIVE` / `REVOKED` / `EXPIRED`。
- `SharingAccessResult`：`SUCCESS` / `REVOKED` / `EXPIRED` / `NEED_LOGIN` / `FORBIDDEN` / `NOT_FOUND`，作为接口返回值和运行日志值，不建访问明细表。

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
| `sort_order` | `sortOrder` | 是 | 展示排序 |

约束：
- `target_id` 唯一。
- 同一分享内 `content_type + content_id` 不得重复。
- 分享目标被删除时，访问页运行时通过内容域判定不可用，展示 `title_snapshot` 和占位提示，其余目标继续展示。
- `sharing_target` 不保存正文、图片、文件、标签全集或权限规则副本。

## Risk Prompt Text

公开分享私有内容时固定提示：

`你正在公开分享平台内私有内容。创建后，获得链接的人无需登录即可查看这些内容。此操作不会改变内容在平台内的私有状态，但会绕过平台内列表、搜索和问答的可见性限制。是否继续？`

私有内容创建私有分享时固定提示：

`该分享包含平台内私有内容。私有分享仅创建者和管理员可访问，内容在平台内的私有状态不变。`

## Application Layer

- `SharingLinkApplicationService`：创建分享、读取分享详情、撤销、恢复、过期判断和分享 URL 生成。
- `SharingTargetApplicationService`：目标快照、目标可用性检查、删除占位和只读访问页数据组装。
- `SharingCollectionCreateApplicationService`：集合创建分享链接、逐条失败隔离和即时结果组装。
- `SharingAccessApplicationService`：公开访问、私有访问登录校验、访问计数递增和访问结果返回。
- `SharingObjectAccessApplicationService`：校验分享链接、分享目标和 Storage 对象归属后，读取分享页内对象内容。
- `SharingRiskApplicationService`：私有内容风险判断、固定文案返回和确认记录。

事务边界：
- 创建分享时必须同时保存链接和目标快照。
- 公开分享私有内容必须在保存前完成风险确认。
- 集合创建按单项独立事务执行，单项失败不得回滚已成功链接。
- 集合创建不保存独立任务状态；成功数、失败数和失败原因只作为本次接口响应返回。
- 访问成功后递增访问计数；访问明细不入 Sharing 表。
- 读取分享页内 Storage 对象前，必须先校验分享链接未撤销、未过期、访问者满足公开或私有访问规则，并确认对象属于该分享链接中的某个目标。
- 撤销、恢复和过期状态变更必须记录 Audit。

## Interface Layer

后台接口固定使用 `/api/admin/sharing/**`，portal 接口固定使用 `/api/portal/sharing/**`。

### Admin Sharing API

- `POST /sharing/links`：创建单个分享链接。
- `POST /sharing/links/collection`：集合创建分享链接，逐条返回创建结果。
- `GET /sharing/links`：查询当前用户分享列表。
- `GET /sharing/links/{shareId}`：查询分享详情。
- `POST /sharing/links/{shareId}/revoke`：撤销分享。
- `POST /sharing/links/{shareId}/restore`：恢复分享。
- `GET /sharing/links/{shareId}/stats`：查询访问统计。

### Risk API

- `POST /sharing/risk-check`：检查选中目标是否需要风险确认并返回固定文案。
- `POST /sharing/risk-confirmations`：确认私有内容分享风险。

### Portal Access API

- `GET /sharing/{shareToken}`：访问分享只读页。
- `GET /sharing/{shareToken}/targets`：读取分享目标列表和占位状态。
- `GET /sharing/{shareToken}/objects/{objectId}`：读取分享页内 Storage 对象内容。

portal 访问使用 `shareToken`，不得暴露 `shareId`。

## Infrastructure Layer

- Repository：`SharingLinkRepository`、`SharingTargetRepository`。
- Mapper：按表建立 MyBatis Mapper。
- PersistenceAssembler：每个聚合根使用独立 `*PersistenceAssembler`。
- 外部客户端：Sancai Application Service、Wangqi Application Service、Ming Customs Application Service、Auth/Core Application Service、Storage Application Service、Audit Application Service。
- 缓存：分享详情和目标快照可短缓存；撤销、过期和私有访问判断不得依赖过期缓存。

## Data Ownership

- 本模块拥有：`sharing_link`、`sharing_target`。
- 本模块只读引用：三才、王圻、明代习俗内容，Auth/Core 用户和权限，Storage 文件对象。
- 禁止跨域直接访问：不得直接修改内容表、导出表、静态展示页面、文件物理数据或内容公开私有状态。
- Flyway 脚本归属：`kuzhambu-admin-api` 的 `db/migration/V1__init.sql`，按 Sharing 分段。

## Observability

- 运行日志：记录分享创建失败、目标快照失败、私有访问拒绝、过期判断异常、分享页内对象访问拒绝和集合创建单项失败。
- 访问日志：由接口层统一记录。
- 审计日志：创建公开分享私有内容、撤销、恢复和管理员访问私有分享统计应形成业务审计。
- 关键指标：有效链接数、公开链接数、私有链接数、过期链接数、访问次数、集合创建成功数、集合创建失败数和目标删除占位次数。

## Acceptance

- 用户能创建分享链接并复制。
- 用户能对多选内容集合创建分享链接。
- 集合创建分享链接完成后，用户能看到成功数、失败数和失败原因。
- 公开分享私有内容必须出现固定风险确认文案。
- 私有分享未登录时引导登录，非创建者且非管理员不可访问。
- 被撤销或过期的链接不可访问。
- 分享目标被删除时访问页显示占位提示，其余目标正常展示。
