# Taxonomy Design

## Purpose

本文档定义标签与同义词业务域的数据结构和接口设计。本文参考 redesign 中的统一标签材料，但不照搬其大对象模型、数据结构或接口形态；本设计必须从跨知识库标签治理、标签关联和同义词维护业务出发，按 DDD 风格重新划分业务边界、聚合和表结构。

Taxonomy 是治理域，不承载内容正文、AI 标签提取执行、搜索结果排序、问答会话或内容页内编辑。它只拥有统一标签、标签分类、标签别名、内容标签关联、同义词词典和标签治理操作日志。

## Business Fit Rules

- 业务优先于旧设计：标签审核、分类、合并、废弃、别名、关联内容、使用统计、同义词维护和扩展查询必须能直接对应标签治理业务。
- 原 redesign 只能作为需求线索和素材来源，不能作为表结构、接口路径、模块划分或聚合边界的设计依据。
- 内容域只提交标签关联请求；Taxonomy 维护权威标签关系，内容域保留的 `tags_snapshot` 只是展示缓存。
- AI 标签提取候选由 AI Refinement 管理；Taxonomy 只接收确认后的标签，并把新标签置入治理流程。
- 不为未来增强、路线图或预留能力建表、建接口或增加状态字段。

## Module

- 模块名称：Taxonomy
- 业务域：taxonomy
- 对应需求文档：[TAXONOMY-REQUIREMENTS.md](../10-requirements/TAXONOMY-REQUIREMENTS.md)
- 后端 biz 子工程：`kuzhambu-servers/biz/kuzhambu-biz-taxonomy`
- 后端 infra 子工程：`kuzhambu-servers/infra/kuzhambu-infra-taxonomy`
- 后台接口入口：`kuzhambu-servers/interfaces/kuzhambu-admin-api`
- 前台接口入口：`kuzhambu-servers/interfaces/kuzhambu-portal-api`
- 前端入口：`kuzhambu-apps/admin-web`、`kuzhambu-apps/portal-web`
- Python worker 能力：无直接归属

## Business Boundary

- 本模块负责：统一标签、标签分类、标签别名、标签审核、标签合并、标签废弃、内容标签关联、标签使用统计、标签云数据、同义词维护、正反向同义词查询和治理操作日志。
- 本模块不负责：AI 标签提取执行、内容正文保存、内容页内候选结果编辑、搜索结果排序、问答回答生成、标签语义检索底层实现。
- 依赖的其他业务域能力：Auth/Core 提供操作者和权限；Audit 记录高价值治理动作；内容域提供内容业务标识和可见性过滤口径；Search/QA 消费标签和同义词扩展结果。
- 对外提供的业务能力：标签治理、内容标签权威关联、标签统计、标签云数据和同义词扩展。

## DDD Model

### Aggregates

- `TaxonomyTag`：统一标签聚合根，拥有名称、分类、描述、状态、来源和合并目标。
- `TaxonomyCategory`：标签分类，服务于标签审核通过和管理归类。
- `TaxonomyTagAlias`：标签别名，支持按别名召回主标签。
- `TaxonomyContentTagRelation`：内容标签关联，跨三才、王圻和明代习俗保存权威标签关系。
- `TaxonomySynonym`：同义词词典，支持正向和反向扩展。
- `TaxonomyOperationLog`：标签治理操作记录，保存审核、合并、废弃等业务轨迹。

### Value Objects

- `TaxonomyTagId`：标签 ULID。
- `TaxonomyCategoryId`：分类 ULID。
- `TaxonomyContentType`：`SANCAI_ENTRY` / `WANGQI_DOCUMENT` / `MING_CUSTOM`。
- `TaxonomyTagStatus`：`PENDING_REVIEW` / `ACTIVE` / `REJECTED` / `DEPRECATED` / `MERGED`。
- `TaxonomyTagSource`：`AI_EXTRACTED` / `MANUAL`。
- `TaxonomyRelationSource`：`AI_APPLIED` / `MANUAL`。
- `TaxonomyOperationType`：`REVIEW_APPROVE` / `REVIEW_REJECT` / `MERGE` / `DEPRECATE` / `ALIAS_ADD` / `ALIAS_REMOVE`。

## Data Model

Taxonomy 表固定使用 `taxonomy_` 前缀。所有对外标识使用 ULID，数据库内部可使用 `bigint id`。

### taxonomy_category

保存标签分类。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `category_id` | `categoryId` | 是 | 分类 ULID |
| `name` | `name` | 是 | 分类名称 |
| `description` | `description` | 否 | 分类说明 |
| `sort_order` | `sortOrder` | 是 | 排序 |
| `enabled` | `enabled` | 是 | 是否启用 |
| `created_at` | `createdAt` | 是 | 创建时间 |
| `updated_at` | `updatedAt` | 是 | 更新时间 |

约束：
- `category_id` 唯一。
- `name` 唯一。
- 停用分类不得用于新的审核通过操作，历史标签分类保留。

### taxonomy_tag

保存统一标签。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `tag_id` | `tagId` | 是 | 标签 ULID |
| `tag_name` | `tagName` | 是 | 标签名称 |
| `category_id` | `categoryId` | 否 | 分类 ULID |
| `description` | `description` | 否 | 标签说明 |
| `status` | `status` | 是 | 标签状态 |
| `source` | `source` | 是 | `AI_EXTRACTED` / `MANUAL` |
| `merge_target_tag_id` | `mergeTargetTagId` | 否 | 合并目标标签 ULID |
| `created_by` | `createdBy` | 否 | 创建人 |
| `reviewed_by` | `reviewedBy` | 否 | 审核人 |
| `reviewed_at` | `reviewedAt` | 否 | 审核时间 |
| `created_at` | `createdAt` | 是 | 创建时间 |
| `updated_at` | `updatedAt` | 是 | 更新时间 |

约束：
- `tag_id` 唯一。
- `tag_name` 唯一。
- AI 自动提取的新标签默认进入 `PENDING_REVIEW`。
- `ACTIVE` 标签才能作为正式治理标签参与检索、筛选或同义词扩展。
- `REJECTED`、`DEPRECATED`、`MERGED` 标签不得用于新的内容关联。
- `MERGED` 标签必须记录 `merge_target_tag_id`。

### taxonomy_tag_alias

保存标签别名。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `alias_id` | `aliasId` | 是 | 别名 ULID |
| `tag_id` | `tagId` | 是 | 目标标签 ULID |
| `alias_name` | `aliasName` | 是 | 别名 |
| `source` | `source` | 是 | `MANUAL` / `MERGE` |
| `created_by` | `createdBy` | 是 | 创建人 |
| `created_at` | `createdAt` | 是 | 创建时间 |

约束：
- `alias_id` 唯一。
- `alias_name` 全局唯一。
- 标签合并后，源标签名称和源标签已有别名必须并入目标标签别名。

### taxonomy_content_tag_relation

保存内容和标签的权威关联。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `relation_id` | `relationId` | 是 | 关联 ULID |
| `content_type` | `contentType` | 是 | 内容类型 |
| `content_id` | `contentId` | 是 | 内容业务 ULID |
| `tag_id` | `tagId` | 是 | 标签 ULID |
| `source` | `source` | 是 | `AI_APPLIED` / `MANUAL` |
| `created_by` | `createdBy` | 否 | 创建人 |
| `created_at` | `createdAt` | 是 | 创建时间 |

约束：
- `relation_id` 唯一。
- `content_type + content_id + tag_id` 唯一。
- 关联只能指向 `ACTIVE` 标签。
- 标签合并时，源标签关联必须迁移到目标标签；如迁移后产生重复关联，应保留一条。
- 内容删除时由内容域请求清理关联，Taxonomy 不直接删除内容数据。

### taxonomy_synonym

保存同义词词典。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `synonym_id` | `synonymId` | 是 | 同义词 ULID |
| `term` | `term` | 是 | 主词 |
| `synonym` | `synonym` | 是 | 同义词 |
| `enabled` | `enabled` | 是 | 是否启用 |
| `created_by` | `createdBy` | 是 | 创建人 |
| `created_at` | `createdAt` | 是 | 创建时间 |
| `updated_at` | `updatedAt` | 是 | 更新时间 |

约束：
- `synonym_id` 唯一。
- `term + synonym` 唯一。
- 正向查询匹配 `term`，反向查询匹配 `synonym`。
- 同义词扩展必须由调用方传入数量限制或使用系统默认限制。

### taxonomy_operation_log

保存标签治理操作日志。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `operation_id` | `operationId` | 是 | 操作 ULID |
| `operation_type` | `operationType` | 是 | 操作类型 |
| `tag_id` | `tagId` | 是 | 主标签 ULID |
| `target_tag_id` | `targetTagId` | 否 | 目标标签 ULID |
| `detail_json` | `detailJson` | 否 | 操作明细 JSON |
| `operator_user_id` | `operatorUserId` | 是 | 操作者用户 ULID |
| `created_at` | `createdAt` | 是 | 操作时间 |

约束：
- 标签审核、合并、废弃和别名变更必须写入操作日志。
- 操作日志只记录 Taxonomy 业务轨迹；全局审计仍由 Audit 业务域记录。

## Application Layer

- `TaxonomyTagApplicationService`：标签列表、搜索、详情、审核、废弃、合并和关联内容查询。
- `TaxonomyCategoryApplicationService`：标签分类创建、编辑、停用和排序。
- `TaxonomyAliasApplicationService`：标签别名新增、删除和查询。
- `TaxonomyRelationApplicationService`：内容标签关联写入、替换、清理和按标签查询内容。
- `TaxonomyStatsApplicationService`：标签使用排行、知识库分布、来源占比、月度新增趋势和标签云数据。
- `TaxonomySynonymApplicationService`：同义词新增、编辑、删除、搜索、正向查询、反向查询和扩展。

事务边界：
- 标签审核通过必须更新标签状态、分类和操作日志。
- 标签审核拒绝必须更新标签状态、清理可用关联并写入操作日志。
- 标签合并必须在同一事务内迁移关联、迁移别名、创建源标签名称别名、更新源标签状态并写入操作日志。
- 标签废弃必须更新状态并写入操作日志。
- 内容标签替换必须保证同一内容同一标签不会重复关联。

## Interface Layer

后台接口固定使用 `/api/admin/taxonomy/**`。

### Tag API

- `GET /taxonomy/tags`：分页查询标签，支持名称、分类、状态、来源和知识库筛选。
- `GET /taxonomy/tags/{tagId}`：读取标签详情。
- `GET /taxonomy/tags/review`：查询待审核标签。
- `POST /taxonomy/tags/{tagId}/review/approve`：审核通过并选择分类。
- `POST /taxonomy/tags/{tagId}/review/reject`：审核拒绝。
- `POST /taxonomy/tags/merge`：合并标签。
- `POST /taxonomy/tags/{tagId}/deprecate`：废弃标签。
- `GET /taxonomy/tags/{tagId}/contents`：按标签查询关联内容。

### Category And Alias API

- `GET /taxonomy/categories`：查询标签分类。
- `POST /taxonomy/categories`：创建分类。
- `PUT /taxonomy/categories/{categoryId}`：更新分类。
- `POST /taxonomy/categories/{categoryId}/disable`：停用分类。
- `GET /taxonomy/tags/{tagId}/aliases`：查询标签别名。
- `POST /taxonomy/tags/{tagId}/aliases`：新增标签别名。
- `DELETE /taxonomy/tags/{tagId}/aliases/{aliasId}`：删除标签别名。

### Relation And Stats API

- `PUT /taxonomy/relations/{contentType}/{contentId}`：替换内容标签关联。
- `DELETE /taxonomy/relations/{contentType}/{contentId}`：清理内容标签关联。
- `GET /taxonomy/stats/usage-top`：标签使用 Top 20。
- `GET /taxonomy/stats/kb-distribution`：按知识库查看标签使用分布。
- `GET /taxonomy/stats/source-ratio`：AI 自动提取与人工标签占比。
- `GET /taxonomy/stats/monthly-new`：月度新增标签趋势。
- `GET /taxonomy/tag-cloud`：查询标签云数据。

### Synonym API

- `GET /taxonomy/synonyms`：查询同义词。
- `POST /taxonomy/synonyms`：新增同义词。
- `PUT /taxonomy/synonyms/{synonymId}`：更新同义词。
- `DELETE /taxonomy/synonyms/{synonymId}`：删除同义词。
- `GET /taxonomy/synonyms/expand`：按正向和反向规则扩展查询词。

portal 接口固定使用 `/api/portal/taxonomy/**`：
- `GET /taxonomy/tags`
- `GET /taxonomy/tags/{tagId}`
- `GET /taxonomy/tags/{tagId}/contents`
- `GET /taxonomy/synonyms/expand`

portal 只返回可用标签、可用别名和启用同义词；内容结果必须结合内容域可见性过滤。

## Infrastructure Layer

- Repository：`TaxonomyTagRepository`、`TaxonomyCategoryRepository`、`TaxonomyAliasRepository`、`TaxonomyRelationRepository`、`TaxonomySynonymRepository`、`TaxonomyOperationLogRepository`。
- Mapper：按表建立 MyBatis Mapper。
- PersistenceAssembler：每个聚合根使用独立 `*PersistenceAssembler`。
- 外部客户端：Audit Application Service、内容域只读查询接口。
- 缓存：可用标签、标签别名、同义词扩展、标签云数据。
- 搜索索引：本模块不直接实现语义索引；必要的索引刷新由 Search 消费标签变更事件。

## Data Ownership

- 本模块拥有：`taxonomy_category`、`taxonomy_tag`、`taxonomy_tag_alias`、`taxonomy_content_tag_relation`、`taxonomy_synonym`、`taxonomy_operation_log`。
- 本模块只读引用：Auth/Core 用户和权限、内容域公开私有状态。
- 禁止跨域直接访问：不得直接修改三才、王圻或明代习俗内容表；不得直接写入搜索索引或问答会话。
- Flyway 脚本归属：`kuzhambu-admin-api` 的 `db/migration/V1__init.sql`，按 Taxonomy 分段。

## Observability

- 运行日志：记录标签审核、合并、废弃、关联迁移、同义词扩展失败原因。
- 访问日志：由接口层统一记录。
- 审计日志：标签审核、合并、废弃、分类维护、别名维护和同义词维护都应形成业务审计。
- 关键指标：标签数、待审核数、废弃数、合并数、标签关联数、同义词数、扩展命中数。

## Acceptance

- 管理员能处理待审核标签。
- 管理员审核通过标签时能选择分类。
- 管理员拒绝标签后，该标签不再出现在可用标签集合中。
- 标签合并后源标签关联内容可通过目标标签找到。
- 标签合并后源标签名称和源标签别名可作为目标标签别名使用。
- 管理员废弃标签后，该标签不再用于新的检索扩展，但历史统计仍可查看。
- 管理员能查看标签使用 Top 20、知识库分布、AI 自动提取与人工标签占比、月度新增趋势。
- 管理员能新增、编辑、删除和搜索同义词。
- 用户搜索别名时能召回主词相关内容。
- 用户通过同义词正向或反向搜索时能召回相关内容。
- 内容没有标签时仍可保存。
