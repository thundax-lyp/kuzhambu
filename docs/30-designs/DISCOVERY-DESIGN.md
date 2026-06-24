# Discovery Design

## Purpose

本文档定义 Discovery 域设计，覆盖跨库搜索和智能问答。

当前阶段优先落地 `Search` 子能力域骨架；问答相关能力继续保留在本设计文档中，但本轮不进入运行时代码交付范围。

## Module

```text
kuzhambu-servers/biz/discovery/
  kuzhambu-discovery-interface/
  kuzhambu-discovery-application/
  kuzhambu-discovery-domain/
  kuzhambu-discovery-infra/
```

## Business Boundary

Discovery 拥有搜索查询、搜索日志、问答会话、问答消息、来源引用和调试信息。Discovery 消费 Classics 内容、Knowledge 同义词和实体、AI 回答生成能力、System 权限上下文。

当前阶段固定边界：

- Search 内容源只接 `SANCAI_ENTRY`、`WANGQI_DOCUMENT`、`MING_CUSTOMS`。
- Search 结果只消费用户当前可见内容；权限过滤必须发生在结果出参前。
- Search 当前不接入 Knowledge 图谱读取，不以知识图谱作为必需前置。
- Search 当前不直接调用 AI 域，不实现查询改写和实体增强的真实执行链路。

## DDD Model

- `SearchQuery`
- `SearchResultGroup`
- `SearchLog`
- `SearchClick`
- `QueryUnderstanding`
- `QaSession`
- `QaMessage`
- `QaSource`
- `QaDebugContext`

### Search 子域模型

- `SearchLog`：记录一次搜索请求的输入、范围、结果数量和失败摘要。
- `SearchClick`：记录一次搜索结果点击，保存内容快照标识和命中位置。
- `QueryUnderstanding`：记录查询清洗、改写、实体识别和同义词扩展的结果；当前阶段允许是占位结构。
- `SearchScope`：承载知识库、门类、标签、状态、可见性和时间范围。
- `SearchKeyword`：承载原始 query、清洗后 query 和展示 query。
- `SearchIntentType`：当前固定为 `KEYWORD_SEARCH`、`NATURAL_LANGUAGE_SEARCH`、`UNKNOWN`。

## Data Model

表名前缀统一使用 `discovery_`。

核心表：

- `discovery_search_log`
- `discovery_search_click`
- `discovery_query_understanding`
- `discovery_qa_session`
- `discovery_qa_message`
- `discovery_qa_source`
- `discovery_qa_debug_context`

搜索索引不是业务真相源，索引结构由 infra 适配维护。

### Search 表结构

`discovery_search_log`

- 主键：`id bigint`
- 业务号：`search_log_id varchar(64)`
- 核心字段：`query_text`、`normalized_query_text`、`display_query_text`、`intent_type`
- 检索范围：`search_scopes_json`
- 结果摘要：`result_total_count`、`group_total_count`
- 状态字段：`search_status`、`failure_code`、`failure_message`
- 请求上下文：`operator_type`、`operator_id`、`request_id`、`trace_id`
- 时间字段：`created_at`

`discovery_search_click`

- 主键：`id bigint`
- 业务号：`search_click_id varchar(64)`
- 关联字段：`search_log_id`
- 内容字段：`content_domain`、`content_type`、`content_id`、`content_title`
- 命中位置：`result_group_key`、`result_rank`、`group_rank`
- 跳转字段：`target_path`
- 请求上下文：`operator_type`、`operator_id`、`request_id`、`trace_id`
- 时间字段：`created_at`

`discovery_query_understanding`

- 主键：`id bigint`
- 业务号：`query_understanding_id varchar(64)`
- 关联字段：`search_log_id`
- 查询字段：`query_text`、`normalized_query_text`、`rewritten_query_text`
- 理解字段：`intent_type`、`recognized_entities_json`、`expanded_synonyms_json`
- 状态字段：`understanding_status`、`failure_code`、`failure_message`
- 请求上下文：`request_id`、`trace_id`
- 时间字段：`created_at`

当前规则：

- Search 相关表固定采用“数据库主键 + 业务号”双轨。
- `search_scopes_json`、`recognized_entities_json`、`expanded_synonyms_json` 当前只做 JSON 原样存取，不拆分二级列。
- 搜索索引是派生读模型，不替代 `discovery_search_log` 等业务表。

## Application Layer

- `SearchApplicationService`
- `QueryUnderstandingApplicationService`
- `QaApplicationService`
- `QaSessionApplicationService`

Application 层负责权限过滤、查询理解、同义词扩展、实体增强、搜索结果分组、问答上下文组装、来源引用和会话管理。

### Search Application Service

- `SearchApplicationService`
  - `search(SearchQuery)`
  - `recordClick(SearchClickCreateCommand)`
  - `pageLogs(SearchLogPageQuery)`
  - `getLog(String searchLogId)`
- `QueryUnderstandingApplicationService`
  - `understand(SearchQuery)`

当前阶段规则：

- `SearchApplicationService` 是 Search 子域唯一用例入口。
- 复杂能力尚未实现时，application 层统一抛 `BizException`，不得返回伪成功空结果。
- `QueryUnderstandingApplicationService` 当前可保留正式方法签名，但方法体允许抛“未实现”业务异常。

## Interface Layer

Admin 入口：

- 搜索质量分析。
- 问答上下文调试。

Portal/Admin 通用入口：

- 跨库搜索。
- 智能问答。
- 王圻文档单文档追加式问答。
- 会话列表、删除和导出。

### Search 当前接口口径

Portal/Common：

- `POST /api/portal/discovery/search/search`
- `POST /api/portal/discovery/search/click`

Admin：

- `POST /api/discovery/search-admin/logs/page`
- `POST /api/discovery/search-admin/logs/get`

当前协议要求：

- Portal 搜索接口返回 `searchLogId`、`queryText`、`displayQueryText`、`totalCount`、`groupCount` 和分组结果。
- 分组结果包含 `groupKey`、`groupTitle`、`count` 和 `items`。
- 结果项固定保留 `highlightText` 字段，即使当前阶段不实现高亮。
- Admin 日志接口权限码固定为 `discovery:search:view`。

## Infrastructure Layer

- Elasticsearch 或等价检索适配。
- AI 回答生成客户端通过 AI 域 application 能力访问。
- 内容读取必须通过 Classics application 能力，不直接读 Classics 表。

### Search Infra 适配

- `SearchIndexGateway`：Search 子域检索抽象。
- `ElasticsearchSearchIndexGateway`：默认实现。
- `DiscoverySearchDocument`：索引文档模型。
- `DiscoverySearchIndexProperties`：索引名、别名和批量参数配置。

当前阶段规则：

- Elasticsearch 是默认实现，不是唯一合法实现。
- application 层只能依赖 `SearchIndexGateway`，不得直接依赖 ES 客户端。
- `search`、`rebuildIndex`、`upsertDocuments` 当前允许在 infra 中显式抛 `UnsupportedOperationException`，用于表达“接口已定、执行未接入”。

## Data Ownership

Discovery 是 `discovery_*` 表的唯一写入方。搜索索引是派生读模型，不替代 Classics 内容真相源。

Search 子域的搜索日志、点击日志和查询理解记录均由 Discovery 自身写入；Classics 只提供可搜索内容，不写入 `discovery_*` 表。

## Observability

- 记录查询词、识别意图、结果数量、点击、问答来源和失败原因。
- 管理员可查看问答调试上下文。

当前阶段至少记录：

- 搜索词
- 查询意图
- 范围 JSON
- 总结果数
- 分组数
- 点击目标
- 请求标识和链路标识
- 失败码和失败摘要

## Acceptance

- 搜索和问答不依赖知识图谱作为必需前置。
- 权限过滤发生在结果展示和问答上下文生成前。

### Search 当前阶段验收

- Search 子域的 domain / application / infra / interface 包结构完整可落代码。
- `discovery_search_log`、`discovery_search_click`、`discovery_query_understanding` 的字段定义已稳定。
- Portal 搜索、点击接口与 Admin 日志分页、详情接口的路径和字段已稳定。
- Elasticsearch 入口、检索抽象和异常占位策略已稳定。
