# Discovery Design

## Purpose

本文档定义 Discovery 域设计，覆盖跨库搜索和智能问答。

当前阶段已完成 `Search` 子能力域运行时闭环，并完成 Discovery QA 的 Portal 问答和 Admin 运维闭环。

## Module

```text
kuzhambu-servers/biz/discovery/
  kuzhambu-discovery-interface/
  kuzhambu-discovery-application/
  kuzhambu-discovery-domain/
  kuzhambu-discovery-infra/
```

## Business Boundary

Discovery 拥有搜索查询、检索统计事件、问答会话、问答消息、来源引用、provider trace 和知识同步状态。Discovery 消费 Classics 内容、Knowledge 同义词和实体、公共 Knowledge Base adapter、System 权限上下文。

当前阶段固定边界：

- Search 内容源只接 `SANCAI_ENTRY`、`WANGQI_DOCUMENT`、`MING_CUSTOMS`。
- Search 查询只消费未删除内容；权限组只控制搜索接口入口，结果字段必须保持有限输出。
- Search 当前不接入 Knowledge 图谱读取，不以知识图谱作为必需前置。
- Search 查询理解可通过 AI 域 usecase 调度；正式 Discovery QA 问答不依赖 Workers 作为运行时入口。

## DDD Model

- `SearchQuery`
- `SearchResultGroup`
- `SearchEvent`
- `SearchClickEvent`
- `QueryUnderstanding`
- `QaSession`
- `QaMessage`
- `QaSource`
- `QaRetrievalTrace`
- `KnowledgeSyncItem`

### Search 子域模型

- `SearchEvent`：记录一次搜索请求的输入、范围、结果数量和失败摘要。
- `SearchClickEvent`：记录一次搜索结果点击，保存内容快照标识和命中位置。
- `QueryUnderstanding`：记录查询清洗、改写、实体识别和同义词扩展的结果；当前阶段允许是占位结构。
- `SearchScope`：承载知识库、门类、标签、状态、可见性和时间范围。
- `SearchKeyword`：承载原始 query、清洗后 query 和展示 query。
- `SearchIntentType`：当前固定为 `KEYWORD_SEARCH`、`NATURAL_LANGUAGE_SEARCH`、`UNKNOWN`。

## Data Model

表名前缀统一使用 `discovery_`。

核心表：

- `discovery_search_event`
- `discovery_search_click_event`
- `discovery_query_understanding`
- `discovery_qa_session`
- `discovery_qa_message`
- `discovery_qa_message_source`
- `discovery_qa_retrieval_trace`
- `discovery_qa_knowledge_sync_item`
- `discovery_qa_session_export`

搜索索引不是业务真相源，索引结构由 infra 适配维护。

### Search 索引文档结构

`discovery_search_document`

- 身份字段：`contentType`、`contentId`
- 展示字段：`title`、`summary`、`bodyText`
- 分组字段：`groupKey`、`groupTitle`
- 过滤字段：`visibility`、`lifecycleStatus`、`tags`
- 深链字段：`targetPath`
- 幂等字段：`sourceVersionNo`
- 删除态字段：`deleted`、`deletedAt`

当前规则：

- 索引文档是派生读模型，不承载业务真相。
- `sourceVersionNo` 固定使用 Classics 内容表上的 `currentVersionNo`。
- `DELETE` 不做物理删除，而是先写入删除态；物理清理由计划任务按保留期清理。

### Search 表结构

`discovery_search_event`

- 主键：`id bigint`
- 业务号：`search_event_id varchar(64)`
- 核心字段：`query_text`、`normalized_query_text`、`display_query_text`、`intent_type`
- 检索范围：`search_scopes_json`
- 结果摘要：`result_total_count`、`group_total_count`
- 状态字段：`search_status`、`failure_code`、`failure_message`
- 请求上下文：`operator_type`、`operator_id`、`request_id`、`trace_id`
- 时间字段：`created_at`

`discovery_search_click_event`

- 主键：`id bigint`
- 业务号：`search_click_event_id varchar(64)`
- 关联字段：`search_event_id`
- 内容字段：`content_domain`、`content_type`、`content_id`、`content_title`
- 命中位置：`result_group_key`、`result_rank`、`group_rank`
- 跳转字段：`target_path`
- 请求上下文：`operator_type`、`operator_id`、`request_id`、`trace_id`
- 时间字段：`created_at`

`discovery_query_understanding`

- 主键：`id bigint`
- 业务号：`query_understanding_id varchar(64)`
- 关联字段：`search_event_id`
- 查询字段：`query_text`、`normalized_query_text`、`rewritten_query_text`
- 理解字段：`intent_type`、`recognized_entities_json`、`expanded_synonyms_json`
- 状态字段：`understanding_status`、`failure_code`、`failure_message`
- 请求上下文：`request_id`、`trace_id`
- 时间字段：`created_at`

当前规则：

- Search 相关表固定采用“数据库主键 + 业务号”双轨。
- `search_scopes_json`、`recognized_entities_json`、`expanded_synonyms_json` 当前只做 JSON 原样存取，不拆分二级列。
- 搜索索引是派生读模型，不替代 `discovery_search_event` 等业务表。

## Application Layer

- `SearchApplicationService`
- `QueryUnderstandingApplicationService`
- `QaApplicationService`
- `QaSessionApplicationService`
- `KnowledgeSyncApplicationService`

Application 层负责权限过滤、查询理解、同义词扩展、实体增强、搜索结果分组、问答会话管理、知识库同步、来源引用和 provider trace 记录。

### Search Application Service

- `SearchApplicationService`
  - `search(SearchQuery)`
  - `recordClick(SearchClickEventCreateCommand)`
  - `pageEvents(SearchEventPageQuery)`
  - `getEvent(String searchEventId)`
- `SearchIndexApplicationService`
  - `rebuildIndex()`
  - `syncDocument(SearchIndexSyncCommand)`
- `SearchIndexCleanupApplicationService`
  - `cleanupDeletedDocumentsOlderThan(Instant threshold)`
- `QueryUnderstandingApplicationService`
  - `understand(SearchQuery)`

当前阶段规则：

- `SearchApplicationService` 是 Search 子域唯一用例入口。
- 复杂能力尚未实现时，application 层统一抛 `BizException`，不得返回伪成功空结果。
- `QueryUnderstandingApplicationService` 当前可保留正式方法签名，但方法体允许抛“未实现”业务异常。

## Interface Layer

Admin 入口：

- 搜索质量分析。
- 问答知识库健康、重建、同步状态、来源和 provider trace 运维。

Portal/Admin 通用入口：

- 跨库搜索。
- OpenAI-compatible 智能问答。
- 王圻文档单文档追加式问答。
- 会话列表、删除和导出。

### Search 当前接口口径

Portal/Common：

- `POST /api/portal/discovery/search/search`
- `POST /api/portal/discovery/search/preview`
- `POST /api/portal/discovery/search/click`

Admin：

- `POST /api/discovery/search/preview`
- `POST /api/discovery/search-statistics/events/page`
- `POST /api/discovery/search-statistics/events/get`
- `POST /api/discovery/search-statistics/index/rebuild`

当前协议要求：

- Portal 搜索接口返回 `searchEventId`、`queryText`、`displayQueryText`、`totalCount`、`groupCount` 和分组结果。
- 分组结果包含 `groupKey`、`groupTitle`、`count` 和 `items`。
- 结果项固定保留 `highlightText` 字段，即使当前阶段不实现高亮。
- Portal/Admin 搜索预览接口只展示 Search 索引派生读模型中的有限字段；能从搜索结果命中的内容即可按 `contentType`、`contentId` 和 `deleted=false` 读取预览，不再二次执行可见性权限判断。
- Portal/Admin 搜索结果点击默认打开内部预览抽屉，通过 `contentType` 与 `contentId` 内部传递数据并调用 Search 预览接口。
- `targetPath` 只作为来源元数据和点击事件字段保留，不作为 Portal/Admin 搜索页的前端路由跳转依据。
- Admin 检索统计接口权限码固定为 `discovery:search:view`。

### QA 当前接口口径

Portal：

- `POST /api/portal/discovery/qa/session/open`
- `POST /api/portal/discovery/qa/session/page`
- `POST /api/portal/discovery/qa/session/get`
- `POST /api/portal/discovery/qa/session/delete`
- `POST /api/portal/discovery/qa/session/export`
- `POST /api/portal/discovery/qa/chat/completions`

Admin：

- `POST /api/discovery/qa-admin/knowledge/health`
- `POST /api/discovery/qa-admin/knowledge/rebuild`
- `POST /api/discovery/qa-admin/knowledge/sync`
- `POST /api/discovery/qa-admin/knowledge/sync/page`
- `POST /api/discovery/qa-admin/session/get`
- `POST /api/discovery/qa-admin/session/delete`
- `POST /api/discovery/qa-admin/session/export`
- `POST /api/discovery/qa-admin/source/list`
- `POST /api/discovery/qa-admin/trace/get`

当前协议要求：

- Portal QA 问答入口采用 OpenAI-compatible `model/messages/choices` 响应习惯，答案从 `choices[0].message.content` 读取。
- Portal QA 请求中的 `model` 是逻辑知识库名，不是 provider app id。
- Portal QA 只暴露 Discovery API，不接收 provider app、dataset、collection 或 file 路由配置。
- Portal QA 会话删除是软删除，只允许删除 owner 匹配的未删除会话；删除后 Portal 列表、详情、追问和导出均不可再访问该会话。
- Portal QA 会话导出固定生成 CSV，写入 `discovery_qa_session_export`，上传到 Storage，返回 `exportId`、`storageObjectId`、`filename`、`contentType` 和导出状态。
- 来源列表从回答顶层 `sources` 返回；返回前必须按当前 Kuzhambu 可见性重新校验，不可见来源标记为 `UNAVAILABLE`。
- Admin QA 暴露知识库健康、重建、同步状态、会话详情、会话删除、会话 CSV 导出、来源列表和 provider trace，不暴露 provider 路由配置。
- Admin QA 可以读取和导出已删除会话，导出 CSV 保留会话删除状态用于审计。

## Infrastructure Layer

- Elasticsearch 或等价检索适配。
- QA 回答生成通过 `kuzhambu-common-knowledge` 的 OpenAI-compatible adapter 访问。
- 内容读取必须通过 Classics application 能力，不直接读 Classics 表。

### Search Infra 适配

- `SearchIndexGateway`：Search 子域检索抽象。
- `ElasticsearchSearchIndexGateway`：默认实现。
- `DiscoverySearchDocument`：索引文档模型。
- `DiscoverySearchIndexProperties`：索引名、别名和批量参数配置。
- `RocketMqDiscoverySearchIndexSyncProducer` / `RocketMqDiscoverySearchIndexSyncConsumer`：索引同步消息入口。

当前阶段规则：

- Elasticsearch 是默认实现，不是唯一合法实现。
- application 层只能依赖 `SearchIndexGateway`，不得直接依赖 ES 客户端。
- Search 运行时已实现真实检索、全量 `rebuild`、增量 `upsert`、删除态写入和删除态物理清理。
- Elasticsearch 查询必须过滤 `deleted = true` 的文档。

### QA Knowledge Base 适配

当前阶段固定采用：

- Discovery 通过 `kuzhambu-common-knowledge` 的 OpenAI-compatible adapter 访问外部知识库能力。
- Discovery 负责将 Classics 可消费内容同步为知识条目，并记录 `sourceId`、`contentType`、`contentId`、`knowledgeBaseName`、`currentVersionNo`、`knowledgeRevision`、`syncStatus`、`failureReason`、`syncedAt` 和 `updatedAt`。
- Discovery 问答请求只接收会话、消息、上下文内容和逻辑模型，不接收 provider 路由参数。
- provider 请求、外部知识库、外部条目、外部会话、耗时、失败原因和 raw 响应写入 provider trace。

当前阶段不采用：

- Portal/Admin 前端直连 provider。
- Workers 承载正式 Discovery QA 问答运行时或知识同步任务。
- Discovery API 透出 provider app、dataset、collection 或 file 控制。

### Search 索引同步

当前阶段固定采用：

- Classics 写路径事务提交后调用 `ClassicsSearchIndexSyncPublishSupport`
- 通过 RocketMQ 发送 `UPSERT / DELETE` 消息
- 消息体固定包含 `contentType`、`contentId`、`operation`、`currentVersionNo`
- Discovery 消费端按 `contentType + contentId` 回查当前最新公开内容
- `UPSERT` 以 `currentVersionNo` 和 ES 文档 `sourceVersionNo` 做幂等判定
- `DELETE` 只把文档写成删除态，不做即时物理删除
- 定时任务按 `deletedAt + retention` 清理过期删除态文档
- Admin `rebuild` 继续作为全量修复兜底

当前阶段不采用：

- Outbox
- 分布式事务
- MQ exactly-once

## Data Ownership

Discovery 是 `discovery_*` 表的唯一写入方。搜索索引是派生读模型，不替代 Classics 内容真相源。

Search 子域的检索统计事件、点击事件和查询理解记录均由 Discovery 自身写入；Classics 只提供可搜索内容，不写入 `discovery_*` 表。

Search 索引同步消息由 Classics 写路径发出，但 Elasticsearch 文档写入、删除态控制和物理清理由 Discovery 独占负责。

## Observability

- 记录查询词、识别意图、结果数量、点击、问答来源、同步状态、provider trace 和失败原因。
- 管理员可查看问答知识库健康、同步记录、来源和 provider trace。

当前阶段至少记录：

- 搜索词
- 查询意图
- 范围 JSON
- 总结果数
- 分组数
- 点击目标
- 请求标识和链路标识
- 失败码和失败摘要
- 索引同步消息的内容类型、内容标识和版本号

## Acceptance

- 搜索和问答不依赖知识图谱作为必需前置。
- 权限过滤发生在结果展示和问答上下文生成前。
- Portal QA 正式问答入口固定为 `POST /api/portal/discovery/qa/chat/completions`。
- Admin QA 可查看知识库健康、同步状态、来源列表和 provider trace。
- Frontend 不直连 provider，Workers 不承载正式 Discovery QA 问答运行时或知识同步任务。

### Search 当前阶段验收

- Search 子域的 domain / application / infra / interface 包结构完整可落代码。
- `discovery_search_event`、`discovery_search_click_event`、`discovery_query_understanding` 的字段定义已稳定。
- Portal 搜索、点击接口与 Admin 检索统计事件分页、详情接口的路径和字段已稳定。
- Elasticsearch 入口、真实检索抽象、增量同步规则和删除态清理规则已稳定。
