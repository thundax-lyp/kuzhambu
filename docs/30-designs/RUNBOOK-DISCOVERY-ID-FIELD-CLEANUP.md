# Discovery Id Field Cleanup Runbook

## Purpose

本 RUNBOOK 用于一次性清理 Discovery 领域内实体自身身份字段重复问题。目标是统一区分“实体自身身份”和“外部引用”：

- 实体自身身份字段统一命名为 `id`。
- 引用其他实体身份时命名为 `{domain}Id`。
- 数据库本表身份列统一为 `id`。
- 数据库引用列继续使用 `{domain}_id`。
- 本次只做 `id/xxxId` 字段清理，不做强类型化。

强类型化 RUNBOOK 已移到项目外 `../RUNBOOK-DISCOVERY-DOMAIN-STRONG-TYPING.md` 搁置，待本次 ID 清理完成后再按新 ID 口径恢复。

## Scope

纳入本次闭环：

- Java backend：`kuzhambu-servers/biz/discovery/`
- Database schema/data：`db/schema/discovery.sql`、`db/data/discovery.sql`
- Admin frontend：`kuzhambu-apps/admin-web/src/pages/discovery/`
- Portal e2e：`kuzhambu-apps/portal-web/e2e/discovery/`
- Discovery 设计文档：`docs/30-designs/DISCOVERY-DESIGN.md`、`docs/30-designs/DISCOVERY-QA-KNOWLEDGE-SPECIAL-DESIGN.md`

## Non-goals

- 不新增 `valueobject`、`codec`、`BaseLongId`、`BaseStringId`。
- 不改变业务状态、角色、provider、content ref 等非身份字段类型。
- 不清理 discovery 之外其他业务域。
- 不保留 HTTP 对外本体身份旧字段名；对外本体身份也统一改为 `id`。

## Identity Rules

固定规则：

- `id` 表示当前实体自身身份。
- `{domain}Id` 表示对其他实体、其他业务对象或外部对象的引用。
- 本体重复字段必须删除，例如 `QaSession.sessionId`、`QaMessage.messageId`、`SearchEvent.searchEventId`。
- 引用字段必须保留，例如 `QaMessage.sessionId`、`QaSource.messageId`、`SearchClickEvent.searchEventId`。
- `QaKnowledgeSyncItem.sourceId` 保留；它是内容来源自然唯一键，不是同步项自身身份。
- `QaSource.sourceBusinessId` 保留；它是外部知识来源号，不是来源记录自身身份。
- 取消 Discovery repository 内部 `SnowflakeIdGenerator`，ID 由数据库 `AUTO_INCREMENT` 生成，并通过 MyBatis 回填。

## Data Structure Changes

数据库通过目标表结构重建完成，不写存量迁移 SQL，不考虑现状数据转换。

| Table | 删除字段 | 保留/调整字段 | 索引变更 | DO 变更 |
| --- | --- | --- | --- | --- |
| `discovery_qa_session` | `session_id bigint NOT NULL` | `id bigint NOT NULL AUTO_INCREMENT` | 删除 `uk_discovery_qa_session_id(session_id)`；`idx_discovery_qa_session_owner`、`idx_discovery_qa_session_context`、`idx_discovery_qa_session_removed_opened` 保留 | `QaSessionDO.sessionId` 删除；`id` 改数据库自增回填 |
| `discovery_qa_message` | `message_id bigint NOT NULL` | `id bigint NOT NULL AUTO_INCREMENT`；`session_id bigint NOT NULL` 保留引用 | 删除 `uk_discovery_qa_message_id(message_id)`；保留 `idx_discovery_qa_message_session(session_id, sent_at)`、`idx_discovery_qa_message_status(answer_status, sent_at)` | `QaMessageDO.messageId` 删除；`sessionId` 保留 |
| `discovery_qa_message_source` | `source_id bigint NOT NULL` | `id bigint NOT NULL AUTO_INCREMENT`；`message_id bigint NOT NULL`、`source_business_id varchar(128) NOT NULL` 保留 | 删除 `uk_discovery_qa_message_source_id(source_id)`；保留 `idx_discovery_qa_message_source_message(message_id, source_rank)`、`idx_discovery_qa_message_source_content(content_type, content_id)` | `QaSourceDO.sourceId` 删除；`sourceBusinessId`、`messageId` 保留 |
| `discovery_qa_retrieval_trace` | `trace_id bigint NOT NULL` | `id bigint NOT NULL AUTO_INCREMENT`；`message_id bigint NOT NULL` 保留引用 | 删除 `uk_discovery_qa_retrieval_trace_id(trace_id)`；保留 `idx_discovery_qa_retrieval_trace_message(message_id)`、`idx_discovery_qa_retrieval_trace_provider(provider, retrieved_at)` | `QaRetrievalTraceDO.traceId` 删除；`messageId` 保留 |
| `discovery_qa_session_export` | `export_id bigint NOT NULL` | `id bigint NOT NULL AUTO_INCREMENT`；`session_id bigint NOT NULL` 保留引用 | 删除 `uk_discovery_qa_session_export_id(export_id)`；保留 `idx_discovery_qa_session_export_session(session_id, requested_at)`、`idx_discovery_qa_session_export_status(export_status, requested_at)` | `QaSessionExportDO.exportId` 删除；`sessionId` 保留 |
| `discovery_qa_knowledge_sync_batch` | `batch_id bigint NOT NULL` | `id bigint NOT NULL AUTO_INCREMENT` | 删除 `uk_discovery_qa_knowledge_sync_batch_id(batch_id)`；保留 `idx_discovery_qa_knowledge_sync_batch_trigger(trigger_type, started_at)`、`idx_discovery_qa_knowledge_sync_batch_provider(provider, started_at)` | `QaKnowledgeSyncBatchDO.batchId` 删除 |
| `discovery_qa_knowledge_sync_item` | 无 | `id bigint NOT NULL AUTO_INCREMENT`；`source_id varchar(128) NOT NULL` 保留自然唯一键 | 保留 `uk_discovery_qa_knowledge_sync_item_source(source_id)`、`idx_discovery_qa_knowledge_sync_item_type(content_type, content_id)`、`idx_discovery_qa_knowledge_sync_item_status(sync_status, updated_at)` | `QaKnowledgeSyncItemDO.id`、`sourceId` 都保留 |
| `discovery_search_event` | `search_event_id varchar(64) NOT NULL` | `id bigint NOT NULL AUTO_INCREMENT` | 删除 `uk_discovery_search_event_id(search_event_id)`；保留 `idx_discovery_search_event_status(search_status, created_at)`、`idx_discovery_search_event_operator(operator_id, created_at)`、`idx_discovery_search_event_intent(intent_type, created_at)` | `SearchEventDO.searchEventId` 删除 |
| `discovery_search_click_event` | `search_click_event_id varchar(64) NOT NULL` | `id bigint NOT NULL AUTO_INCREMENT`；`search_event_id bigint NOT NULL` 保留引用并由 varchar 改 bigint | 删除 `uk_discovery_search_click_event_id(search_click_event_id)`；保留 `idx_discovery_search_click_event_event(search_event_id, created_at)`、`idx_discovery_search_click_event_content(content_type, content_id)`、`idx_discovery_search_click_event_operator(operator_id, created_at)` | `SearchClickEventDO.searchClickEventId` 删除；`searchEventId` 类型由 `String` 改 `Long` |
| `discovery_query_understanding` | `query_understanding_id varchar(64) NOT NULL` | `id bigint NOT NULL AUTO_INCREMENT`；`search_event_id bigint DEFAULT NULL` 保留引用并由 varchar 改 bigint | 删除 `uk_discovery_query_understanding_id(query_understanding_id)`；保留 `idx_discovery_query_understanding_event(search_event_id)`、`idx_discovery_query_understanding_status(understanding_status, created_at)`、`idx_discovery_query_understanding_intent(intent_type, created_at)` | `QueryUnderstandingDO.queryUnderstandingId` 删除；`searchEventId` 类型由 `String` 改 `Long` |

DO 注解要求：

- `@TableId(type = IdType.INPUT)` 改为数据库自增口径，优先使用 `@TableId(type = IdType.AUTO)`。
- 删除 repository 中所有 `new SnowflakeIdGenerator()` 字段和 `setId(nextId)` 逻辑。
- `mapper.insert(dataObject)` 后从 `dataObject.getId()` 获取回填 ID 并返回。

## Api Contract Changes

对外本体身份字段统一改为 `id`；引用字段继续保留 `{domain}Id`。

| Contract | 删除/改名 | 新字段 | 说明 |
| --- | --- | --- | --- |
| `QaSessionResult` | `sessionId` | `id` | 会话自身身份 |
| `QaMessageResult` | `messageId` | `id` | 消息自身身份；`sessionId` 保留引用 |
| `QaTraceResult` | `traceId` | `id` | trace 自身身份；`messageId` 保留引用 |
| `QaSessionExportResult` | `exportId` | `id` | 导出自身身份；`sessionId` 保留引用 |
| `KnowledgeSyncBatchResult` | `batchId` | `id` | 同步批次自身身份 |
| `SearchEventResult` | `searchEventId` | `id` | 搜索事件自身身份 |
| `SearchResult` / `SearchPageResult` / search response | `searchEventId` | `id` | 搜索响应自身事件号改为 `id` |
| `SearchClickEventCreateCommand` | 无 | `searchEventId` 类型改 `Long` | 这是引用字段，名称保留 |
| `DiscoverySearchClickEventRequest` | 无 | `searchEventId` 类型改 Long 字符串入参可解析 | 点击操作引用搜索事件 |
| `QueryUnderstanding` result | `queryUnderstandingId` 如存在 | `id` | 查询理解自身身份 |

## File-Level Task Packs

每个小任务控制在 2-6 个文件。执行时按顺序完成一个小任务并跑窄测试/编译，再进入下一个。

### Task 1: Schema Rebuild

Files:

- `db/schema/discovery.sql`
- `db/data/discovery.sql`
- `docs/30-designs/DISCOVERY-DESIGN.md`
- `docs/30-designs/DISCOVERY-QA-KNOWLEDGE-SPECIAL-DESIGN.md`

Actions:

- 按 `Data Structure Changes` 精确删除字段、改引用列类型、删除唯一键。
- 将 Discovery 业务表 `id` 统一改为 `bigint NOT NULL AUTO_INCREMENT`。
- 如 `db/data/discovery.sql` 写入已删除字段，直接按新 schema 重写，不保留兼容字段。
- 更新设计文档中的字段清单，把实体自身 `sessionId/messageId/exportId/batchId/searchEventId` 改为 `id`。

### Task 2: QA Session

Files:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaSession.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaSessionRepository.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaSessionDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/mapper/QaSessionMapper.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSessionRepositoryImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/assembler/QaPersistenceAssembler.java`

Actions:

- 删除 `QaSession.sessionId` 和 `QaSessionDO.sessionId`。
- `QaSessionRepository.getBySessionId(Long)` 改为 `getById(Long)`。
- `markRemoved(Long sessionId, Date removedAt)` 保持方法语义可改为 `markRemoved(Long id, Date removedAt)`，SQL 条件改 `where id = #{id}`。
- `QaSessionRepositoryImpl.save` 删除 `SnowflakeIdGenerator`，insert 后返回 `dataObject.getId()`。
- `QaPersistenceAssembler` 删除 sessionId 映射。

### Task 3: QA Message

Files:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaMessage.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaMessageRepository.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaMessageDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaMessageRepositoryImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/assembler/QaPersistenceAssembler.java`

Actions:

- 删除 `QaMessage.messageId` 和 `QaMessageDO.messageId`。
- `QaMessageRepository.getByMessageId(Long)` 改为 `getById(Long)`。
- `listBySessionId(Long sessionId)` 保留引用查询。
- `QaMessageRepositoryImpl.save` 删除 `SnowflakeIdGenerator`，insert 后返回回填 `id`。
- assembler 删除 messageId 映射，保留 sessionId 映射。

### Task 4: QA Source And Trace

Files:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaSource.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaRetrievalTrace.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaSourceRepository.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaRetrievalTraceRepository.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSourceRepositoryImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaRetrievalTraceRepositoryImpl.java`

Actions:

- 删除 `QaSource.sourceId`，保留 `sourceBusinessId`。
- 删除 `QaRetrievalTrace.traceId`。
- `QaRetrievalTraceRepository.getByTraceId(Long)` 改为 `getById(Long)`。
- `QaSourceRepository.save`、`QaRetrievalTraceRepository.save` 返回数据库回填 `id`。
- 后续 Task 需要同步 `QaPersistenceAssembler`、DO 和 application 使用点。

### Task 5: QA Source And Trace Persistence

Files:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaSourceDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaRetrievalTraceDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/assembler/QaPersistenceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaSourceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaTraceAssembler.java`

Actions:

- 删除 `QaSourceDO.sourceId`、`QaRetrievalTraceDO.traceId`。
- `QaSourceAssembler` 不再设置 Long `sourceId`；来源号继续写 `sourceBusinessId`。
- `QaTraceAssembler` result 组装将自身 trace 身份写入 `id`。
- CSV 导出中来源号列使用 `sourceBusinessId`，trace 自身列改名 `id`。

### Task 6: QA Export And Sync Batch

Files:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaSessionExport.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaKnowledgeSyncBatch.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaSessionExportRepository.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaKnowledgeSyncBatchRepository.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSessionExportRepositoryImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaKnowledgeSyncBatchRepositoryImpl.java`

Actions:

- 删除 `QaSessionExport.exportId`、`QaKnowledgeSyncBatch.batchId`。
- `getByExportId`、`getByBatchId` 改为 `getById`。
- repository impl 取消 `SnowflakeIdGenerator`，save 后返回回填 `id`。

### Task 7: QA Export And Sync Persistence

Files:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaSessionExportDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaKnowledgeSyncBatchDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/assembler/QaPersistenceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/result/QaSessionExportResult.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/result/KnowledgeSyncBatchResult.java`

Actions:

- 删除 DO 中 `exportId`、`batchId`。
- result 字段 `exportId`、`batchId` 改为 `id`。
- `QaApplicationServiceImpl` 和 `KnowledgeSyncApplicationServiceImpl` 在 Task 8 同步调用点。

### Task 8: QA Application And Interface

Files:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeQaApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeSyncApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/result/QaSessionResult.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/result/QaMessageResult.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/result/QaTraceResult.java`

Actions:

- result 本体身份字段改为 `id`。
- 新建会话、消息、trace、export、batch 时不提前分配 ID。
- 保存后使用 repository 返回的 `id` 写后续引用字段。
- 文件名和 owner key 原来使用 `sessionId/exportId` 的地方改用 `session.id/export.id`。

### Task 9: QA HTTP Responses

Files:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/response/DiscoveryQaResponses.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/assembler/DiscoveryQaPortalInterfaceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/response/DiscoveryQaAdminResponses.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/assembler/DiscoveryQaAdminInterfaceAssembler.java`

Actions:

- Response 中本体身份字段改为 `id`：会话、消息、trace、export、sync batch。
- Request 中引用字段保留：`sessionId`、`messageId`、`traceId` 只有当它们表示引用或链路号时保留。
- `@Schema(name = ...)` 和 `@JsonProperty` 同步改名。

### Task 10: Search Domain And Persistence

Files:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/entity/SearchEvent.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/entity/SearchClickEvent.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/entity/QueryUnderstanding.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/dataobject/SearchEventDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/dataobject/SearchClickEventDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/dataobject/QueryUnderstandingDO.java`

Actions:

- 删除 `SearchEvent.searchEventId`、`SearchClickEvent.searchClickEventId`、`QueryUnderstanding.queryUnderstandingId`。
- 保留 `SearchClickEvent.searchEventId` 和 `QueryUnderstanding.searchEventId`，并把类型从 `String` 改为 `Long`。
- DO 同步删除本体字段并调整引用字段类型。

### Task 11: Search Repositories And Assemblers

Files:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/repository/SearchEventRepository.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/repository/SearchClickEventRepository.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/repository/QueryUnderstandingRepository.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/repository/impl/SearchEventRepositoryImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/repository/impl/SearchClickEventRepositoryImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/repository/impl/QueryUnderstandingRepositoryImpl.java`

Actions:

- 自身查询改 `getById(Long id)`。
- `QueryUnderstandingRepository.getBySearchEventId(Long searchEventId)` 保留引用查询。
- repository impl 取消 `SnowflakeIdGenerator`，insert 后返回回填 `id`。

### Task 12: Search Application And HTTP

Files:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/QueryUnderstandingApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/result/SearchResult.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/result/SearchEventResult.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/controller/response/DiscoverySearchResponse.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/response/DiscoverySearchEventResponse.java`

Actions:

- Search 本体响应字段 `searchEventId` 改为 `id`。
- 点击请求 `searchEventId` 保留为引用字段，类型按 Long 处理。
- admin 搜索事件详情请求如当前按 `searchEventId` 查自身，改为 `id`。

### Task 13: Admin Frontend QA

Files:

- `kuzhambu-apps/admin-web/src/pages/discovery/qa/qa-types.ts`
- `kuzhambu-apps/admin-web/src/pages/discovery/qa/qa-service.ts`
- `kuzhambu-apps/admin-web/src/pages/discovery/qa/qa-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/discovery/qa/qa-session-table.tsx`
- `kuzhambu-apps/admin-web/src/pages/discovery/qa/qa-session-detail-drawer.tsx`
- `kuzhambu-apps/admin-web/src/pages/discovery/qa/qa-message-panel.tsx`

Controls and operations:

- 会话列表左侧 `QaSessionTable`：
  - session item key 从 `session.sessionId` 改为 `session.id`。
  - “新建对话”按钮不受影响。
  - “选择会话”按钮 `onSelect` 参数改传 `id`。
  - 删除图标按钮 `onDelete` 参数改传 `id`。
  - “导出对话”按钮对当前选中会话使用 `selectedSessionId` 的变量名可重命名为 `selectedSessionId` 或 `selectedSessionKey`，请求字段仍按后端引用语义传 `sessionId`。
- 会话详情抽屉 `QaSessionDetailDrawer`：
  - 描述项标题从 `sessionId` 改为 `id`。
  - 标题 `会话 ${session.sessionId}` 改为 `会话 ${session.id}`。
- 消息面板：
  - 消息自身 key 从 `message.messageId` 改为 `message.id`。
  - 消息引用会话的 `sessionId` 如展示引用关系则保留。

### Task 14: Admin Frontend QA Console

Files:

- `kuzhambu-apps/admin-web/src/pages/discovery/qa-console/qa-console-types.ts`
- `kuzhambu-apps/admin-web/src/pages/discovery/qa-console/qa-console-service.ts`
- `kuzhambu-apps/admin-web/src/pages/discovery/qa-console/qa-console-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/discovery/qa-console/qa-session-table.tsx`
- `kuzhambu-apps/admin-web/src/pages/discovery/qa-console/qa-session-detail-drawer.tsx`
- `kuzhambu-apps/admin-web/src/pages/discovery/qa-console/qa-console-page.test.tsx`

Controls and operations:

- 会话管理筛选区：
  - 标题 Input、创建时间 RangePicker、查询按钮不受 ID 字段命名影响。
- 会话记录 Table：
  - `rowKey` 从 `record.sessionId` 改为 `record.id`。
  - “查看”按钮调用 `onOpen(String(record.id ?? ""))`，后端请求引用字段仍为 `sessionId` 或改为 `id` 取决于接口任务；本 RUNBOOK 要求本体查询接口改为 `id`。
  - “导出”按钮调用 `onExport(String(record.id ?? ""))`。
  - “删除”按钮调用 `onDelete(String(record.id ?? ""))`。
- 操作提示文案：
  - `会话 ${variables.sessionId}` 改为 `会话 ${variables.id}` 或 `会话 ${selectedSessionId}`，避免旧字段名。

### Task 15: Admin Frontend Search

Files:

- `kuzhambu-apps/admin-web/src/pages/discovery/search/search-types.ts`
- `kuzhambu-apps/admin-web/src/pages/discovery/search/search-service.ts`
- `kuzhambu-apps/admin-web/src/pages/discovery/search/search-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/discovery/search/search-page.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/discovery/search-statistics/search-statistics-types.ts`
- `kuzhambu-apps/admin-web/src/pages/discovery/search-statistics/search-statistics-page.tsx`

Controls and operations:

- 搜索页：
  - 搜索结果响应 `response.searchEventId` 改为 `response.id`。
  - 点击结果时 `createClickCommand(response.id, group, item)`，请求字段仍叫 `searchEventId`，因为它是点击事件引用搜索事件。
  - 搜索输入框、搜索按钮、预览操作不受控件层影响。
- 搜索统计页 Table：
  - “检索编号”列 `dataIndex` 从 `searchEventId` 改为 `id`。
  - 展开行缓存 key 从 `record.searchEventId` 改为 `record.id`。
  - 展开操作请求从 `{ searchEventId: record.searchEventId }` 改为 `{ id: record.id }`。
  - 详情 Descriptions 中本体编号项 key/children 改为 `id`。
- 重建索引按钮、筛选 Input、状态筛选、日期 RangePicker 不受 ID 字段命名影响。

### Task 16: Frontend Tests And Portal E2E

Files:

- `kuzhambu-apps/admin-web/src/pages/discovery/qa/qa-page.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/discovery/qa-console/qa-console-page.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/discovery/search/search-page.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/discovery/search-statistics/search-statistics-page.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/discovery/common/discovery-service-contract.test.ts`
- `kuzhambu-apps/portal-web/e2e/discovery/search/search.spec.ts`

Actions:

- Mock response 中本体字段改为 `id`。
- 用户点击会话、删除、导出、展开搜索统计行、点击搜索结果的断言改用 `id`。
- Portal search e2e 中 mocked `searchEventId` 改为响应 `id`；点击请求仍断言包含引用字段 `searchEventId`。

### Task 17: Backend Tests

Files:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSessionRepositoryImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaMessageRepositoryImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSourceRepositoryImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/controller/DiscoverySearchPortalControllerTest.java`

Actions:

- 更新 repository save 断言：不再断言 `setId(nextId)`，改断言 insert 后返回 DO 回填 `id`。
- 更新自身查询：`getById`。
- 更新 response JSON 字段：本体身份为 `id`。

## Verification

Backend:

```sh
cd kuzhambu-servers
mvn -pl biz/discovery -am spotless:apply
mvn -pl biz/discovery -am spotless:check
mvn -pl biz/discovery -am checkstyle:check
mvn -pl biz/discovery -am test
```

Frontend:

```sh
cd kuzhambu-apps
pnpm --filter kuzhambu-admin-web run format
pnpm run format:check
pnpm run lint
pnpm --filter kuzhambu-admin-web run test
pnpm --filter kuzhambu-admin-web run build
pnpm --filter @kuzhambu/portal-web run e2e -- e2e/discovery/search.spec.ts e2e/discovery/qa.spec.ts
```

Manual checks:

- Domain entity 中不再存在本体重复身份字段。
- Repository 自身身份查询统一是 `getById`。
- 引用字段没有误删：`QaMessage.sessionId`、`QaSource.messageId`、`SearchClickEvent.searchEventId`。
- HTTP 对外本体身份字段统一为 `id`。
- Frontend 会话列表、会话管理表格、搜索统计表格、搜索结果点击操作都使用新 `id`。
- Schema 中本表身份列只剩 `id`，引用列仍保留 `{domain}_id`。

## Closure

本 RUNBOOK 完成后删除本文件，或把数据库重建和验证证据沉淀到 `docs/40-readiness/` 后删除。完成后再从项目外 `../RUNBOOK-DISCOVERY-DOMAIN-STRONG-TYPING.md` 恢复强类型化计划，并按新的 `id` 口径重写。
