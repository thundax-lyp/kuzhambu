# RUNBOOK Discovery Search Skeleton

## Purpose

本文档用于执行 Discovery `Search` 子能力域骨架搭建。

目标不是交付简化 MVP，而是一次性落定：

- `discovery_search_log`、`discovery_search_click`、`discovery_query_understanding` 三类数据结构。
- Discovery Search 的 domain / application / infra / interface 四层包结构。
- Portal/Admin 共用搜索接口和 Admin 搜索分析接口的协议边界。
- Elasticsearch 默认检索适配入口。
- 复杂能力的统一占位策略：方法保留正式签名，当前阶段在方法体内明确抛出 `BizException` 或 `UnsupportedOperationException`，不写临时假逻辑。

本文档只覆盖 Discovery `Search` 子域，不覆盖问答会话、问答消息、问答来源、单文档追加式问答和 Portal 搜索页面实现。

## Scope

本轮纳入范围：

- Java servers Discovery 模块骨架与包结构。
- Discovery Search 三张表对应 DO、Entity、Repository、Mapper、ApplicationService、Controller、Request/Response。
- Elasticsearch 检索文档模型与 Gateway 抽象。
- 搜索日志创建、点击日志创建、查询理解占位入口。
- Admin 路由预留和后端 starter 扫描接入。
- Architecture test、contract test、application test 的骨架。

本轮不纳入范围：

- 实际 Elasticsearch 查询 DSL。
- 同义词扩展、停用词过滤、实体识别、实体链接。
- Classics 内容索引重建和增量更新。
- Portal 搜索页面、Admin 搜索分析页面。
- 搜索结果高亮、相关性调优、深链状态恢复。
- QA 会话、回答生成、AI 联调。

## Fixed Decisions

- 业务子域命名固定为 `search`。
- 后台接口前缀固定为 `/api/discovery/search-admin`。
- Portal/Common 搜索接口前缀固定为 `/api/portal/discovery/search`。
- 本轮内容源范围固定为 `SANCAI_ENTRY`、`WANGQI_DOCUMENT`、`MING_CUSTOMS`，不接入其他业务域内容。
- 管理端搜索分析接口先提供只读分页与详情协议，不在本轮提供完整页面。
- Search 相关表固定采用“数据库主键 + 业务号”双轨：数据库表保留 `id bigint`，对外引用与跨表关联使用 `search_log_id`、`search_click_id`、`query_understanding_id`。
- Admin 搜索分析接口权限码固定为 `discovery:search:view`。
- 复杂能力占位时，application 层抛 `BizException`，domain 层抛 `DomainException`，不得抛 `IllegalArgumentException`。
- Elasticsearch 为默认 infra 实现；后续若需要替换为等价检索适配，只允许替换 `infra.client` 与 `application.support` 内检索装配代码。

## Data Structure Definitions

### Table Definition `discovery_search_log`

用途：记录一次搜索请求的输入、理解结果和返回摘要。

| 字段名 | 类型 | 非空 | 说明 |
| --- | --- | --- | --- |
| `id` | `bigint` | 是 | 数据库主键 |
| `search_log_id` | `varchar(64)` | 是 | 业务日志号 |
| `query_text` | `varchar(500)` | 是 | 原始搜索词 |
| `normalized_query_text` | `varchar(500)` | 否 | 清洗后的搜索词 |
| `display_query_text` | `varchar(500)` | 否 | 前端回显搜索词 |
| `intent_type` | `varchar(64)` | 是 | 查询意图 |
| `search_scopes_json` | `text` | 否 | 检索范围 JSON |
| `result_total_count` | `int` | 是 | 总命中数 |
| `group_total_count` | `int` | 是 | 结果分组数 |
| `search_status` | `varchar(32)` | 是 | `SUCCEEDED` / `FAILED` |
| `failure_code` | `varchar(128)` | 否 | 失败码 |
| `failure_message` | `varchar(500)` | 否 | 失败摘要 |
| `operator_type` | `varchar(32)` | 是 | `USER` / `ADMIN` / `ANONYMOUS` |
| `operator_id` | `varchar(64)` | 否 | 操作者标识 |
| `request_id` | `varchar(128)` | 否 | 请求标识 |
| `trace_id` | `varchar(128)` | 否 | 链路标识 |
| `created_at` | `datetime` | 是 | 创建时间 |

约束：

- `search_scopes_json` 原样保存筛选条件，不拆库内明细列。
- `failure_message` 只保存稳定摘要，不保存堆栈。

### Table Definition `discovery_search_click`

用途：记录搜索结果点击。

| 字段名 | 类型 | 非空 | 说明 |
| --- | --- | --- | --- |
| `id` | `bigint` | 是 | 数据库主键 |
| `search_click_id` | `varchar(64)` | 是 | 业务点击号 |
| `search_log_id` | `varchar(64)` | 是 | 关联搜索日志号 |
| `content_domain` | `varchar(64)` | 是 | 内容域，当前固定 `CLASSICS` |
| `content_type` | `varchar(64)` | 是 | 内容类型 |
| `content_id` | `varchar(64)` | 是 | 内容业务标识 |
| `content_title` | `varchar(255)` | 否 | 点击时标题快照 |
| `result_group_key` | `varchar(64)` | 是 | 搜索结果分组键 |
| `result_rank` | `int` | 是 | 全结果位置 |
| `group_rank` | `int` | 是 | 组内位置 |
| `target_path` | `varchar(500)` | 否 | 深链路径 |
| `operator_type` | `varchar(32)` | 是 | 操作者类型 |
| `operator_id` | `varchar(64)` | 否 | 操作者标识 |
| `request_id` | `varchar(128)` | 否 | 请求标识 |
| `trace_id` | `varchar(128)` | 否 | 链路标识 |
| `created_at` | `datetime` | 是 | 创建时间 |

### Table Definition `discovery_query_understanding`

用途：记录查询理解、改写和增强结果。

| 字段名 | 类型 | 非空 | 说明 |
| --- | --- | --- | --- |
| `id` | `bigint` | 是 | 数据库主键 |
| `query_understanding_id` | `varchar(64)` | 是 | 业务理解号 |
| `search_log_id` | `varchar(64)` | 是 | 关联搜索日志号 |
| `query_text` | `varchar(500)` | 是 | 原始搜索词 |
| `normalized_query_text` | `varchar(500)` | 否 | 清洗后的搜索词 |
| `rewritten_query_text` | `varchar(500)` | 否 | 改写后的搜索词 |
| `intent_type` | `varchar(64)` | 是 | 查询意图 |
| `recognized_entities_json` | `text` | 否 | 实体识别结果 JSON |
| `expanded_synonyms_json` | `text` | 否 | 同义词扩展结果 JSON |
| `understanding_status` | `varchar(32)` | 是 | `SUCCEEDED` / `FAILED` / `SKIPPED` |
| `failure_code` | `varchar(128)` | 否 | 失败码 |
| `failure_message` | `varchar(500)` | 否 | 失败摘要 |
| `request_id` | `varchar(128)` | 否 | 请求标识 |
| `trace_id` | `varchar(128)` | 否 | 链路标识 |
| `created_at` | `datetime` | 是 | 创建时间 |

### Domain Entity Definition

`SearchLog`

- `searchLogId: String`
- `queryText: String`
- `normalizedQueryText: String`
- `displayQueryText: String`
- `intentType: SearchIntentType`
- `searchScope: SearchScope`
- `resultTotalCount: Integer`
- `groupTotalCount: Integer`
- `searchStatus: String`
- `failureCode: String`
- `failureMessage: String`
- `operatorType: String`
- `operatorId: String`
- `requestId: String`
- `traceId: String`
- `createdAt: Instant`

`SearchClick`

- `searchClickId: String`
- `searchLogId: String`
- `contentDomain: String`
- `contentType: String`
- `contentId: String`
- `contentTitle: String`
- `resultGroupKey: String`
- `resultRank: Integer`
- `groupRank: Integer`
- `targetPath: String`
- `operatorType: String`
- `operatorId: String`
- `requestId: String`
- `traceId: String`
- `createdAt: Instant`

`QueryUnderstanding`

- `queryUnderstandingId: String`
- `searchLogId: String`
- `queryText: String`
- `normalizedQueryText: String`
- `rewrittenQueryText: String`
- `intentType: SearchIntentType`
- `recognizedEntitiesJson: String`
- `expandedSynonymsJson: String`
- `understandingStatus: String`
- `failureCode: String`
- `failureMessage: String`
- `requestId: String`
- `traceId: String`
- `createdAt: Instant`

### Value Object Definition

`SearchScope`

- `knowledgeBases: List<String>`
- `categoryCodes: List<String>`
- `tagNames: List<String>`
- `contentStatuses: List<String>`
- `visibilityScopes: List<String>`
- `dateFrom: Instant`
- `dateTo: Instant`

`SearchKeyword`

- `rawText: String`
- `normalizedText: String`
- `displayText: String`

### Elasticsearch Document Definition

`DiscoverySearchDocument`

- `documentId: String`
- `contentDomain: String`
- `contentType: String`
- `contentId: String`
- `knowledgeBase: String`
- `categoryCode: String`
- `categoryName: String`
- `title: String`
- `summary: String`
- `bodyText: String`
- `tagNames: List<String>`
- `status: String`
- `visibility: String`
- `publishedAt: Instant`
- `updatedAt: Instant`
- `sourcePath: String`

约束：

- `bodyText` 只保存纯文本索引内容，不保存 HTML。
- `sourcePath` 只保存站内跳转路径，不保存完整域名。

## Interface Definitions

### Portal Search API

路径：`POST /api/portal/discovery/search/search`

请求体 `DiscoverySearchRequest`：

| 字段 | 类型 | 非空 | 说明 |
| --- | --- | --- | --- |
| `queryText` | `String` | 是 | 搜索词 |
| `knowledgeBases` | `List<String>` | 否 | 知识库范围 |
| `categoryCodes` | `List<String>` | 否 | 门类筛选 |
| `tagNames` | `List<String>` | 否 | 标签筛选 |
| `contentStatuses` | `List<String>` | 否 | 状态筛选 |
| `visibilityScopes` | `List<String>` | 否 | 可见性筛选 |
| `dateFrom` | `String` | 否 | ISO-8601 起始时间 |
| `dateTo` | `String` | 否 | ISO-8601 结束时间 |
| `pageNo` | `Integer` | 是 | 页码 |
| `pageSize` | `Integer` | 是 | 每页数量 |

响应体 `DiscoverySearchResponse`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `searchLogId` | `String` | 本次搜索日志号 |
| `queryText` | `String` | 原始搜索词 |
| `displayQueryText` | `String` | 用于前端回显的搜索词 |
| `totalCount` | `Integer` | 总命中数 |
| `groupCount` | `Integer` | 分组数 |
| `groups` | `List<DiscoverySearchGroupResponse>` | 分组结果 |

`DiscoverySearchGroupResponse`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `groupKey` | `String` | 分组键 |
| `groupTitle` | `String` | 分组标题 |
| `count` | `Integer` | 组内命中数 |
| `items` | `List<DiscoverySearchItemResponse>` | 组内结果 |

`DiscoverySearchItemResponse`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `contentDomain` | `String` | 内容域 |
| `contentType` | `String` | 内容类型 |
| `contentId` | `String` | 内容业务标识 |
| `title` | `String` | 标题 |
| `summary` | `String` | 摘要 |
| `highlightText` | `String` | 高亮文本，当前可为空 |
| `resultRank` | `Integer` | 全结果位置 |
| `groupRank` | `Integer` | 组内位置 |
| `targetPath` | `String` | 跳转路径 |

当前阶段要求：

- 即使高亮未实现，`highlightText` 字段也必须保留。
- 若搜索后端未实现，接口返回业务异常，不返回空列表。

### Portal Search Click API

路径：`POST /api/portal/discovery/search/click`

请求体 `DiscoverySearchClickRequest`：

| 字段 | 类型 | 非空 | 说明 |
| --- | --- | --- | --- |
| `searchLogId` | `String` | 是 | 搜索日志号 |
| `contentDomain` | `String` | 是 | 内容域 |
| `contentType` | `String` | 是 | 内容类型 |
| `contentId` | `String` | 是 | 内容业务标识 |
| `contentTitle` | `String` | 否 | 点击时标题快照 |
| `resultGroupKey` | `String` | 是 | 结果分组键 |
| `resultRank` | `Integer` | 是 | 全结果位置 |
| `groupRank` | `Integer` | 是 | 组内位置 |
| `targetPath` | `String` | 否 | 跳转路径 |

响应体：

- `Boolean`

### Admin Search Log Page API

路径：`POST /api/discovery/search-admin/logs/page`

权限：

- `@HasPermission("discovery:search:view")`

请求体 `DiscoverySearchLogPageRequest`：

| 字段 | 类型 | 非空 | 说明 |
| --- | --- | --- | --- |
| `queryText` | `String` | 否 | 搜索词筛选 |
| `intentTypes` | `List<String>` | 否 | 意图筛选 |
| `searchStatuses` | `List<String>` | 否 | 状态筛选 |
| `operatorId` | `String` | 否 | 操作者筛选 |
| `dateFrom` | `String` | 否 | 起始时间 |
| `dateTo` | `String` | 否 | 结束时间 |
| `pageNo` | `Integer` | 是 | 页码 |
| `pageSize` | `Integer` | 是 | 每页数量 |

响应体：

- `PageResponse<DiscoverySearchLogResponse>`

`DiscoverySearchLogResponse`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `searchLogId` | `String` | 搜索日志号 |
| `queryText` | `String` | 原始搜索词 |
| `displayQueryText` | `String` | 回显搜索词 |
| `intentType` | `String` | 意图 |
| `resultTotalCount` | `Integer` | 总结果数 |
| `groupTotalCount` | `Integer` | 分组数 |
| `searchStatus` | `String` | 搜索状态 |
| `operatorId` | `String` | 操作者 |
| `createdAt` | `String` | 创建时间 |

### Admin Search Log Detail API

路径：`POST /api/discovery/search-admin/logs/get`

权限：

- `@HasPermission("discovery:search:view")`

请求体 `DiscoverySearchLogGetRequest`：

| 字段 | 类型 | 非空 | 说明 |
| --- | --- | --- | --- |
| `searchLogId` | `String` | 是 | 搜索日志号 |

响应体 `DiscoverySearchLogDetailResponse`：

在 `DiscoverySearchLogResponse` 基础上新增：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `normalizedQueryText` | `String` | 清洗后的搜索词 |
| `searchScopesJson` | `String` | 检索范围 JSON |
| `failureCode` | `String` | 失败码 |
| `failureMessage` | `String` | 失败摘要 |
| `requestId` | `String` | 请求标识 |
| `traceId` | `String` | 链路标识 |

## File-Level Task Plan

### T1 建立 Discovery Search 设计与覆盖基线

目标：先把文档和覆盖基线补到位，后续代码按文档落地。

涉及文件：

- `docs/30-designs/DISCOVERY-DESIGN.md`
- `docs/40-readiness/DISCOVERY-IMPLEMENTATION-COVERAGE.md`

具体操作：

- 在 `DISCOVERY-DESIGN.md` 中新增 Search 子域的 package、表、接口和 Elasticsearch 适配说明。
- 新建 `DISCOVERY-IMPLEMENTATION-COVERAGE.md`，按 `已完成 / 部分完成 / 未完成` 记录本轮骨架范围。

验收点：

- Discovery 设计文档能独立说明 Search 子域边界。
- 覆盖文档明确区分“骨架已完成”和“运行能力未完成”。

### T2 建立 domain 层搜索实体和值对象骨架

目标：先固定领域模型命名和字段，不实现复杂规则。

涉及文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/entity/SearchLog.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/entity/SearchClick.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/entity/QueryUnderstanding.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/valueobject/SearchScope.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/valueobject/SearchKeyword.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/enums/SearchIntentType.java`

具体操作：

- 为三类实体定义稳定字段，只放结构字段，不写跨域取数逻辑。
- `SearchScope` 固定承载知识库范围、门类、标签、状态、时间条件。
- `SearchKeyword` 固定承载原始 query、清洗后 query、显示 query。
- `SearchIntentType` 先定义 `KEYWORD_SEARCH`、`NATURAL_LANGUAGE_SEARCH`、`UNKNOWN` 三个枚举值。

验收点：

- domain 层类名、包名、字段名可直接被 application / infra / interface 复用。
- 本任务不引入 Spring、MyBatis、HTTP 注解。

### T3 建立 domain Repository 与规则占位

目标：把持久化端口和规则出口先定下来。

涉及文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/repository/SearchLogRepository.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/repository/SearchClickRepository.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/repository/QueryUnderstandingRepository.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/service/SearchDomainService.java`

具体操作：

- 三个 Repository 只暴露当前阶段确定需要的保存、分页读取、按主键读取方法。
- `SearchDomainService` 先实现基础 query 判空、scope 判空、page 参数归一化。
- 对“同义词扩展”“实体识别增强”这类复杂规则，先保留正式方法签名，并在方法体内抛 `DomainException`。

验收点：

- repository 方法命名与后续 Mapper/RepositoryImpl 一一对应。
- `SearchDomainService` 只包含当前域内规则，不访问其他域。

### T4 建立 application 输入输出协议

目标：先稳定 application 层的命令、查询和结果模型。

涉及文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/query/SearchQuery.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/query/SearchLogPageQuery.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/command/SearchClickCreateCommand.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/result/SearchResult.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/result/SearchGroupResult.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/result/SearchLogResult.java`

具体操作：

- `SearchQuery` 固定 Portal/Common 搜索请求字段。
- `SearchLogPageQuery` 固定 Admin 搜索日志分页查询字段。
- `SearchClickCreateCommand` 固定点击日志写入口字段。
- `SearchResult`、`SearchGroupResult` 固定搜索结果、结果分组、命中位置、高亮文本字段。
- `SearchLogResult` 固定管理端查看日志所需字段。

验收点：

- 当前阶段 application 模型不依赖 interface request/response。
- Search 结果模型已经包含后续高亮和来源跳转所需字段，即使当前未实现。

### T5 建立 application service 与 support 入口

目标：把 Search 子域正式用例入口搭起来，并明确哪些方法当前抛异常。

涉及文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/SearchApplicationService.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/QueryUnderstandingApplicationService.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/QueryUnderstandingApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/SearchIndexGateway.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/SearchPermissionFilter.java`

具体操作：

- `SearchApplicationService` 暴露：搜索、记录点击、分页查看搜索日志、查看单条搜索日志。
- `QueryUnderstandingApplicationService` 暴露：创建或更新查询理解记录。
- `SearchApplicationServiceImpl` 完成主链路装配，但复杂检索执行先调用 `SearchIndexGateway`；若底层未实现，抛 `BizException`，错误语义固定为“SEARCH_BACKEND_NOT_IMPLEMENTED”。
- `SearchPermissionFilter` 只定义过滤接口和输入输出结构，不在本轮实现具体规则。

验收点：

- `SearchApplicationServiceImpl` 不依赖 infra 包。
- 所有“未实现”能力都通过统一异常出口暴露，不写返回空列表的伪成功逻辑。

### T6 建立 infra 持久化对象与 Mapper

目标：把三张表对应的 infra 骨架补齐。

涉及文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/dataobject/SearchLogDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/dataobject/SearchClickDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/dataobject/QueryUnderstandingDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/mapper/SearchLogMapper.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/mapper/SearchClickMapper.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/mapper/QueryUnderstandingMapper.java`

具体操作：

- 为三类 DO 建立固定字段，与 domain entity 字段一一映射。
- 为三类 Mapper 定义 `insert`、`selectById`、`selectPage` 所需方法。
- 当前阶段若分页 SQL 还未完全确定，可先留最小查询方法，但不得缺失类型签名。

验收点：

- `DO` 只包含表字段，不混入业务转换逻辑。
- `Mapper` 只使用 `@Mapper`。

### T7 建立 infra RepositoryImpl 与 PersistenceAssembler

目标：把 domain Repository 与 Mapper 接起来。

涉及文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/assembler/SearchLogPersistenceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/assembler/SearchClickPersistenceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/assembler/QueryUnderstandingPersistenceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/repository/impl/SearchLogRepositoryImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/repository/impl/SearchClickRepositoryImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/repository/impl/QueryUnderstandingRepositoryImpl.java`

具体操作：

- 三个 PersistenceAssembler 实现 `toObject` / `toDomain`。
- 三个 RepositoryImpl 只做 Mapper 调用和转换，不做 query 理解、权限过滤、检索逻辑。
- 分页结果可先按现有 `PageResult` 所需最小字段返回。

验收点：

- RepositoryImpl 只依赖本域 Mapper 和 Assembler。
- infra 层没有 HTTP、OpenAPI 注解。

### T8 建立 Elasticsearch 客户端骨架

目标：把默认搜索适配入口落下来，但不实现真实 DSL。

涉及文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/client/DiscoverySearchDocument.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/client/ElasticsearchSearchIndexGateway.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/client/DiscoverySearchIndexProperties.java`

具体操作：

- `DiscoverySearchDocument` 固定 Elasticsearch 索引文档字段。
- `DiscoverySearchIndexProperties` 固定索引名、别名、批量写入大小等配置字段。
- `ElasticsearchSearchIndexGateway` 实现 `SearchIndexGateway`；`search`、`rebuildIndex`、`upsertDocuments` 方法当前统一抛 `UnsupportedOperationException`，异常消息固定标明方法名。

验收点：

- Elasticsearch 相关代码只出现在 `infra.client`。
- application 层只依赖 `SearchIndexGateway` 抽象，不直接依赖 ES 客户端。

### T9 建立 interface 层 Portal 搜索接口

目标：把对外搜索协议先稳定下来。

涉及文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/controller/DiscoverySearchPortalController.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/controller/request/DiscoverySearchRequest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/controller/request/DiscoverySearchClickRequest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/controller/response/DiscoverySearchResponse.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/controller/response/DiscoverySearchGroupResponse.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/assembler/DiscoverySearchPortalInterfaceAssembler.java`

具体操作：

- `DiscoverySearchPortalController` 提供 `search` 和 `click` 两个 POST 接口。
- Request/Response 字段与 application 层一一映射，不直接暴露 domain 模型。
- 若 application 抛出“搜索后端未实现”，接口层直接透传为标准业务异常响应，不做吞错。

验收点：

- Portal Controller 符合 `@WrappedApiController`、`@Tag`、类级 `@RequestMapping`、方法级 `@Operation` 规范。
- `click` 接口和 `search` 接口协议固定下来，即使前端暂时未接入。

### T10 建立 interface 层 Admin 搜索日志接口

目标：把管理端分析入口协议定下来，页面可以后做。

涉及文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/DiscoverySearchAdminController.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/request/DiscoverySearchLogPageRequest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/request/DiscoverySearchLogGetRequest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/response/DiscoverySearchLogResponse.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/assembler/DiscoverySearchAdminInterfaceAssembler.java`

具体操作：

- `DiscoverySearchAdminController` 提供日志分页、日志详情两个 POST 接口。
- 请求响应字段按搜索分析使用场景设计，不与 Portal 搜索响应混用。
- 权限码本轮先固定为 `discovery:search:view`。

验收点：

- 后台接口路径、权限码、请求响应模型均已固定。
- Admin 接口即使页面未实现，也能被 contract test 覆盖。

### T11 建立 starter 扫描与 OpenAPI 分组接入

目标：让 Discovery 接口能被 admin/portal starter 扫描到。

涉及文件：

- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/java/com/thundax/kuzhambu/starter/admin/KuzhambuAdminApplication.java`
- `kuzhambu-servers/starter/kuzhambu-portal-starter/src/main/java/com/thundax/kuzhambu/starter/portal/KuzhambuPortalApplication.java`
- `kuzhambu-servers/common/kuzhambu-common-openapi/src/main/java/com/thundax/kuzhambu/common/openapi/configure/` 下对应业务分组配置文件

具体操作：

- Admin starter 扫描 Discovery `interfaces.admin`。
- Portal starter 扫描 Discovery `interfaces.portal`。
- OpenAPI 分组新增或接入 Discovery module 分组，不与 Knowledge 或 Classics 混组。

验收点：

- Discovery Controller 在启动扫描范围内。
- OpenAPI 分组规则能稳定包含 Discovery 接口。

### T12 建立测试骨架与最小验证入口

目标：先把骨架测试位铺好，后续实现直接补测试内容。

涉及文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/DiscoverySearchAdminControllerTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/controller/DiscoverySearchPortalControllerTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/client/ElasticsearchSearchIndexGatewayTest.java`
- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/test/java/com/thundax/kuzhambu/starter/admin/AdminStarterArchitectureTest.java`
- `kuzhambu-servers/starter/kuzhambu-portal-starter/src/test/java/com/thundax/kuzhambu/starter/portal/PortalStarterArchitectureTest.java`

具体操作：

- ControllerTest 锁定路径、HTTP method、请求响应形状和权限要求。
- ApplicationServiceImplTest 锁定“未实现检索后端时抛 BizException”的行为。
- GatewayTest 锁定“当前方法明确抛 UnsupportedOperationException”的行为。
- Starter Architecture Test 增加 Discovery package 扫描断言。

验收点：

- 测试名称与职责清晰，后续实现不需要重命名测试类。
- 本轮测试可以先覆盖结构和异常出口，不要求真实 ES 联通。

## Exception Policy

当前阶段允许保留占位异常的方法：

- `SearchDomainService.expandSynonyms(...)`
- `SearchDomainService.linkEntities(...)`
- `QueryUnderstandingApplicationService.understand(...)`
- `ElasticsearchSearchIndexGateway.search(...)`
- `ElasticsearchSearchIndexGateway.rebuildIndex(...)`
- `ElasticsearchSearchIndexGateway.upsertDocuments(...)`

异常规则：

- domain 层：抛 `DomainException`
- application 层：抛 `BizException`
- infra 客户端未实现：抛 `UnsupportedOperationException`
- interface 层：不得直接抛裸 `RuntimeException`

禁止事项：

- 不得用“返回空列表”伪装成功。
- 不得把“未实现”写成 TODO 注释而没有异常出口。
- 不得在 Controller 中直接 `throw new UnsupportedOperationException()`。

## Validation Order

执行代码任务时按以下顺序验证：

1. 对本轮新增 Java 文件先跑 Discovery 模块内最窄 `spotless:apply`
2. `mvn -pl biz/discovery -am spotless:check`
3. `mvn -pl biz/discovery -am checkstyle:check`
4. `mvn -pl biz/discovery -am test`
5. 若 starter 或 OpenAPI 被修改，再补 admin / portal starter 对应测试

## Closure Rules

本 RUNBOOK 完成的标志：

- Discovery Search 的 package、表结构、Repository、ApplicationService、Controller、Gateway 抽象全部落地。
- 所有复杂能力都以统一异常出口显式标识“未实现”，没有伪成功逻辑。
- `DISCOVERY-DESIGN.md` 与 `DISCOVERY-IMPLEMENTATION-COVERAGE.md` 已同步。

本 RUNBOOK 关闭前必须删除，不保留在长期文档中。
