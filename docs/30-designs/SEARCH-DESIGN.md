# Search Design

## Purpose

本文档定义跨库搜索业务域的数据结构和接口设计。本文参考 redesign 中的搜索与查询增强材料，但不照搬其大对象模型、数据结构或接口形态；本设计必须从三才图会、王圻文档和明代习俗统一检索业务出发，按 DDD 风格重新划分业务边界、聚合和表结构。

Search 是检索域，不拥有三库内容正文、标签词典、同义词词典、知识图谱浏览或问答生成。它消费内容域、Taxonomy 和可选 Knowledge Graph 能力，负责查询理解、查询改写、权限过滤、相关性排序、结果分组、关键词高亮、搜索日志和点击日志。

## Business Fit Rules

- 业务优先于旧设计：跨库检索、分组结果、关键词高亮、高级筛选、查询理解、同义词扩展、实体识别、权限过滤、相关性排序、搜索日志和深链状态必须能直接对应搜索业务。
- 原 redesign 只能作为需求线索和素材来源，不能作为表结构、接口路径、模块划分或聚合边界的设计依据。
- Search 不维护同义词词典和标签治理，必须消费 Taxonomy。
- Knowledge Graph 可用于实体识别和实体链接增强，但搜索不得依赖知识图谱作为必需前置。
- 不为未来增强、路线图或预留能力建表、建接口或增加状态字段。

## Module

- 模块名称：Search
- 业务域：search
- 对应需求文档：[SEARCH-REQUIREMENTS.md](../10-requirements/SEARCH-REQUIREMENTS.md)
- 后端 biz 子工程：`kuzhambu-servers/biz/kuzhambu-biz-search`
- 后端 infra 子工程：`kuzhambu-servers/infra/kuzhambu-infra-search`
- 后台接口入口：`kuzhambu-servers/interfaces/kuzhambu-admin-api`
- 前台接口入口：`kuzhambu-servers/interfaces/kuzhambu-portal-api`
- 前端入口：`kuzhambu-apps/admin-web`、`kuzhambu-apps/portal-web`
- Python worker 能力：可通过 HTTP 提供查询理解、实体识别或重排序能力，业务结果由 Java 主系统控制

## Business Boundary

- 本模块负责：跨三库统一搜索、查询理解、查询清洗、停用词过滤、同义词扩展调用、实体识别和链接调用、查询改写、权限过滤编排、相关性排序、关键词高亮、结果分组、搜索日志、点击日志和搜索配置。
- 本模块不负责：内容正文维护、标签治理、同义词维护、知识图谱浏览、智能问答回答生成、内容导出。
- 依赖的其他业务域能力：Sancai/Wangqi/Ming Customs 提供内容和可见性；Taxonomy 提供标签、别名和同义词扩展；Knowledge Graph 提供可选实体链接；Auth/Core 提供当前用户和权限。
- 对外提供的业务能力：统一搜索、查询增强、搜索质量日志和点击行为数据。

## DDD Model

### Aggregates

- `SearchQuery`：一次搜索请求，包含原始查询、过滤条件、查询理解结果、改写结果和结果数量。
- `SearchResultGroup`：搜索结果分组，按知识库组织结果并在组内排序。
- `SearchConfig`：搜索配置，保存同义词扩展数量、停用词、重排序候选数等可调参数。
- `SearchClickLog`：结果点击日志，用于搜索质量分析。

### Value Objects

- `SearchContentType`：`SANCAI_ENTRY` / `WANGQI_DOCUMENT` / `MING_CUSTOM`。
- `SearchIntent`：`BIOGRAPHY` / `RELATION` / `LOCATION` / `TIME` / `DESCRIPTION` / `LIST` / `GENERAL`。
- `SearchScope`：`ALL` / `SANCAI` / `WANGQI` / `MING_CUSTOMS`。
- `SearchResultVisibility`：权限过滤后的可见性判断结果。
- `SearchDeepLinkState`：查询词、筛选器、排序和分页的 URL 状态。

## Data Model

Search 表固定使用 `search_` 前缀。所有对外标识使用 ULID，数据库内部可使用 `bigint id`。

### search_config

保存搜索配置。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `config_id` | `configId` | 是 | 配置 ULID |
| `config_key` | `configKey` | 是 | 配置键 |
| `config_value` | `configValue` | 是 | 配置值 JSON |
| `description` | `description` | 否 | 配置说明 |
| `enabled` | `enabled` | 是 | 是否启用 |
| `operator_user_id` | `operatorUserId` | 是 | 操作人 |
| `operated_at` | `operatedAt` | 是 | 操作时间 |

约束：
- `config_key` 唯一。
- 同义词扩展必须有数量限制。
- 停用词和重排序候选数通过配置表达，不硬编码在业务流程中。

### search_query_log

保存搜索行为日志。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `query_id` | `queryId` | 是 | 查询 ULID |
| `user_id` | `userId` | 否 | 用户 ULID |
| `raw_query` | `rawQuery` | 是 | 原始查询词 |
| `normalized_query` | `normalizedQuery` | 否 | 清洗后查询词 |
| `intent` | `intent` | 否 | 识别意图 |
| `rewritten_query` | `rewrittenQuery` | 否 | 改写后查询 |
| `filters_json` | `filtersJson` | 否 | 筛选条件 JSON |
| `expanded_terms_json` | `expandedTermsJson` | 否 | 同义词扩展结果 JSON |
| `linked_entities_json` | `linkedEntitiesJson` | 否 | 实体链接结果 JSON |
| `result_count` | `resultCount` | 是 | 权限过滤后的结果数 |
| `searched_at` | `searchedAt` | 是 | 搜索时间 |

约束：
- 搜索日志不得保存用户无权访问内容的详情快照。
- 权限过滤后无结果时，`result_count=0`。

### search_click_log

保存搜索结果点击日志。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `click_id` | `clickId` | 是 | 点击 ULID |
| `query_id` | `queryId` | 是 | 查询 ULID |
| `user_id` | `userId` | 否 | 用户 ULID |
| `content_type` | `contentType` | 是 | 内容类型 |
| `content_id` | `contentId` | 是 | 内容业务 ULID |
| `result_rank` | `resultRank` | 是 | 点击时排名 |
| `clicked_at` | `clickedAt` | 是 | 点击时间 |

约束：
- 点击日志只记录用户实际可见结果。
- 点击内容必须能跳转到对应内容详情。

## Application Layer

- `SearchApplicationService`：统一搜索、分组结果、关键词高亮、深链状态解析和分页。
- `QueryUnderstandingApplicationService`：查询清洗、停用词过滤、意图识别和查询改写。
- `SearchPermissionApplicationService`：对候选结果按当前用户权限过滤。
- `SearchRankingApplicationService`：组内相关性排序和重排序。
- `SearchLogApplicationService`：搜索日志、点击日志和质量分析数据写入。
- `SearchConfigApplicationService`：搜索配置查询和维护。

事务边界：
- 一次搜索请求完成后写入搜索日志，日志不得阻断搜索结果返回。
- 点击日志必须在点击动作发生时记录。
- 配置变更必须记录 Audit。
- 权限过滤必须在结果展示前完成。

## Interface Layer

后台接口固定使用 `/api/admin/search/**`，portal 接口固定使用 `/api/portal/search/**`。

### Search API

- `GET /search`：跨库统一搜索。
- `GET /search/deeplink`：解析搜索结果深链状态。
- `POST /search/clicks`：记录搜索结果点击。

### Query API

- `GET /search/query/understand`：查询理解和意图识别。
- `GET /search/query/rewrite`：查询改写。
- `GET /search/query/entities`：实体识别和实体链接。

### Config API

- `GET /search/configs`：查询搜索配置。
- `PUT /search/configs/{configKey}`：更新搜索配置。
- `GET /search/logs`：后台查询搜索日志。
- `GET /search/logs/{queryId}/clicks`：后台查询点击日志。

portal 不提供配置和日志管理接口。

## Infrastructure Layer

- Repository：`SearchConfigRepository`、`SearchQueryLogRepository`、`SearchClickLogRepository`。
- Mapper：按表建立 MyBatis Mapper。
- PersistenceAssembler：每个聚合根使用独立 `*PersistenceAssembler`。
- 外部客户端：Sancai Application Service、Wangqi Application Service、Ming Customs Application Service、Taxonomy Application Service、Knowledge Graph Application Service、Auth/Core Application Service、Python worker HTTP Client。
- Elasticsearch：三库内容索引和全文检索落地，不作为业务数据所有权。
- 缓存：停用词、搜索配置、同义词扩展结果和热门查询可短缓存。

## Data Ownership

- 本模块拥有：`search_config`、`search_query_log`、`search_click_log`。
- 本模块只读引用：三才、王圻、明代习俗内容，Taxonomy 标签与同义词，Knowledge Graph 实体，Auth/Core 用户和权限。
- 禁止跨域直接访问：不得直接修改内容表、标签表、知识图谱表或问答会话表。
- Flyway 脚本归属：`kuzhambu-admin-api` 的 `db/migration/V1__init.sql`，按 Search 分段。

## Observability

- 运行日志：记录查询理解失败、索引查询失败、权限过滤异常和重排序异常。
- 访问日志：由接口层统一记录。
- 审计日志：搜索配置变更应形成业务审计。
- 关键指标：搜索次数、无结果率、平均结果数、点击率、同义词扩展命中率、实体链接命中率。

## Acceptance

- 用户输入关键词后能看到分组结果。
- 用户能看到组内按相关性排列的结果。
- 用户无权访问的内容不会出现在结果中。
- 用户能通过知识库、门类、标签、状态或时间筛选结果。
- 用户搜索别名时能召回主词相关内容。
- 用户搜索实体名称时能看到相关内容。
- 用户能从结果跳转到对应详情页。
- 用户打开搜索结果深链时能恢复查询状态。
- 无结果时展示明确空状态，并提供修改关键词或清除筛选的提示。
