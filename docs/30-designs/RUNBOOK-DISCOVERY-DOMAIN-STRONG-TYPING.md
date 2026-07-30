# Discovery Domain Strong Typing Runbook

## Purpose

本 RUNBOOK 用于继续收口 Discovery 领域模型、仓储端口和受影响调用链的强类型化改造。目标是把 `com.thundax.kuzhambu.discovery.domain.*.model.entity.*` 中表达业务身份、状态、角色、来源、外部引用、请求链路和操作人语义的 `Long` / `String` 基础类型，改为明确的领域强类型，并把 repository 端口同步改为强类型契约。

当前代码已经做过一轮 ID 口径调整：QA 和 search 的主业务标识均由 `Long id` 承载；search 实体和 DO 通过 `getSearchEventId()`、`setSearchEventId(String)` 等兼容访问器把 Long 转成数字字符串。因此本 RUNBOOK 后续以 Long-backed ID 强类型为目标，不再把 search 三个主 ID 设计为 `BaseStringId`。

## Scope

纳入本次闭环的 Java 模块：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface`

纳入前端影响盘点的当前页面：

- `kuzhambu-apps/portal-web/src/pages/discovery/search/search-page.tsx`
- `kuzhambu-apps/portal-web/src/pages/discovery/search/components/search-controls.tsx`
- `kuzhambu-apps/portal-web/src/pages/discovery/search/components/search-results.tsx`
- `kuzhambu-apps/portal-web/src/pages/discovery/search/search-service.ts`
- `kuzhambu-apps/portal-web/src/pages/discovery/search/search-types.ts`
- `kuzhambu-apps/portal-web/src/pages/discovery/qa/qa-page.tsx`
- `kuzhambu-apps/portal-web/src/pages/discovery/qa/components/qa-composer.tsx`
- `kuzhambu-apps/portal-web/src/pages/discovery/qa/components/qa-timeline.tsx`
- `kuzhambu-apps/portal-web/src/pages/discovery/qa/qa-service.ts`
- `kuzhambu-apps/portal-web/src/pages/discovery/qa/qa-types.ts`

当前 `admin-web` 未发现 Discovery 专属页面文件。本轮不新增 admin-web 页面；只保持 admin HTTP response 的基础类型契约不变。

## Non-goals

- 不调整数据库字段类型、表结构、索引或初始化数据。
- 不改变 HTTP request/response、facade DTO 和 infra DO 的基础类型序列化口径。
- 不引入 discovery 到其他业务域 `domain` 模块的 Maven 依赖；跨域身份引用用本域 Ref/Id 值对象承载底层值。
- 不在 discovery `valueobject/*Id.java` 中新增 `static` 工厂方法；基础值转换统一放入 `domain/<subdomain>/codec/*Codec.java`。
- 不把搜索索引文档 `DiscoverySearchDocument` 改为领域模型。

## Confirmed Decisions

- Search 对外 ID 不再支持非数字字符串。`searchEventId`、`searchClickEventId`、`queryUnderstandingId` 统一按 Long-backed ID 强类型处理；HTTP/application result 如需字符串展示，只输出数字字符串。
- Search 的 `requestId`、`traceId` 复用 `com.thundax.kuzhambu.common.core.traceability.valueobject.RequestId` 和 `TraceId`，不在 discovery 里新增同义 `RequestRef`、`TraceRef`。
- `QaKnowledgeSyncItem.sourceId` 是知识同步条目的业务唯一键，格式 `{contentType}:{contentId}`，该结论来自 `DISCOVERY-QA-KNOWLEDGE-SPECIAL-DESIGN.md` 的 Metadata Fields。数据库 `id` 是持久化行标识，只服务 `updateById`、审计和内部定位，不作为 repository 业务契约返回值。

## Current Code Snapshot

- Discovery domain 目前只有 search 子域的 `SearchKeyword`、`SearchScope` 两个值对象；QA 子域尚无 `model/valueobject` 和 `codec` 包。
- QA 实体仍以 `Long id` 作为主标识承载字段，通过 `getSessionId()`、`getMessageId()`、`getSourceId()`、`getTraceId()`、`getExportId()`、`getBatchId()` 等兼容访问器表达业务 ID。
- Search 实体和 search DO 均以 `Long id` 或 `Long searchEventId` 承载主键和外键；`searchEventId`、`searchClickEventId`、`queryUnderstandingId` 的 String 访问器只是兼容转换层。
- Search 三张 DO 使用 `@TableId(type = IdType.AUTO)`，`QaKnowledgeSyncItemDO` 使用 `@TableId(type = IdType.INPUT)`；`QaKnowledgeSyncItemRepositoryImpl.save` 当前用 `SnowflakeIdGenerator` 生成数据库 `id`，但业务查找、幂等 upsert、分页去重、接口响应和知识库 item key 均围绕 `sourceId`。
- `DiscoveryDomainArchitectureTest` 已经调用 `assertValueObjectIdSourcesDeclareNoStaticMethods(Path.of("src/main/java"))` 和 `assertBaseIdTypes(classes, BASE_PACKAGE)`，后续只需要确认新增 discovery `*Id` 被这些规则覆盖。

## Data Structure Changes

### QA Entity Field Changes

| File | Field | Current | Target | Persistence / API boundary |
| --- | --- | --- | --- | --- |
| `QaSession.java` | `id` | `Long` | `QaSessionId` | `QaSessionDO.id` 仍为 `Long`；HTTP `id/sessionId` 仍输出数字字符串或 `Long` |
| `QaSession.java` | `ownerType`, `ownerId` | `String`, `String` | `QaOwnerRef` | DO/request/response 保持 `ownerType`, `ownerId` |
| `QaSession.java` | `knowledgeBaseName` | `String` | `KnowledgeBaseName` | DO/request/response 保持 `String` |
| `QaSession.java` | `scope` | `String` | `QaSessionScope` | DO/request/response 保持 `String` |
| `QaSession.java` | `contextMode` | `String` | `QaContextMode` | DO/request/response 保持 `String` |
| `QaSession.java` | `contextContentType`, `contextContentId` | `String`, `Long` | `QaContextContentRef` | DO/request/response 保持双字段 |
| `QaSession.java` | `status` | `String` | `QaSessionStatus` | DO/request/response 保持 `String` |
| `QaMessage.java` | `id` | `Long` | `QaMessageId` | `QaMessageDO.id` 仍为 `Long` |
| `QaMessage.java` | `sessionId` | `Long` | `QaSessionId` | `QaMessageDO.sessionId` 仍为 `Long` |
| `QaMessage.java` | `role` | `String` | `QaMessageRole` | DO/request/response 保持 `String` |
| `QaMessage.java` | `answerStatus` | `String` | `QaAnswerStatus` | DO/request/response 保持 `String` |
| `QaSource.java` | `id` | `Long` | `QaSourceId` | `QaSourceDO.id` 仍为 `Long` |
| `QaSource.java` | `sourceBusinessId` | `String` | `KnowledgeSourceId` | `QaSourceDO.sourceBusinessId` 仍为 `String` |
| `QaSource.java` | `messageId` | `Long` | `QaMessageId` | `QaSourceDO.messageId` 仍为 `Long` |
| `QaSource.java` | `contentType`, `contentId` | `String`, `Long` | `KnowledgeContentRef` | DO/request/response 保持双字段 |
| `QaSource.java` | `knowledgeBase` | `String` | `KnowledgeBaseName` | DO/response 保持 `String` |
| `QaSource.java` | `sourceStatus` | `String` | `QaSourceStatus` | DO/response 保持 `String` |
| `QaRetrievalTrace.java` | `id` | `Long` | `QaRetrievalTraceId` | `QaRetrievalTraceDO.id` 仍为 `Long` |
| `QaRetrievalTrace.java` | `messageId` | `Long` | `QaMessageId` | `QaRetrievalTraceDO.messageId` 仍为 `Long` |
| `QaRetrievalTrace.java` | `provider` | `String` | `QaKnowledgeProvider` | DO/response 保持 `String` |
| `QaRetrievalTrace.java` | `externalKnowledgeBaseId` | `String` | `ExternalKnowledgeBaseId` | DO/response 保持 `String` |
| `QaRetrievalTrace.java` | `externalKnowledgeItemIds` | `String` | `ExternalKnowledgeItemRefs` | DO/response 保持单个 `String` |
| `QaRetrievalTrace.java` | `externalChatId` | `String` | `ExternalChatId` | DO/response 保持 `String` |
| `QaRetrievalTrace.java` | `providerRequestId` | `String` | `ProviderRequestId` | DO/response 保持 `String` |
| `QaRetrievalTrace.java` | `aiCallId` | `Long` | `AiCallRef` | DO/response 保持 `Long` |
| `QaRetrievalTrace.java` | `aiStatus` | `String` | `AiInvocationStatusRef` | DO/response 保持 `String` |
| `QaSessionExport.java` | `id` | `Long` | `QaSessionExportId` | `QaSessionExportDO.id` 仍为 `Long` |
| `QaSessionExport.java` | `sessionId` | `Long` | `QaSessionId` | `QaSessionExportDO.sessionId` 仍为 `Long` |
| `QaSessionExport.java` | `format` | `String` | `QaExportFormat` | DO/response 保持 `String` |
| `QaSessionExport.java` | `storageObjectId` | `Long` | `StorageObjectRef` | DO/response 保持 `Long` |
| `QaSessionExport.java` | `exportStatus` | `String` | `QaExportStatus` | DO/response 保持 `String` |
| `QaSessionExport.java` | `requesterUserId` | `Long` | `RequesterUserRef` | DO/request/response 保持 `Long` |
| `QaKnowledgeSyncBatch.java` | `id` | `Long` | `QaKnowledgeSyncBatchId` | `QaKnowledgeSyncBatchDO.id` 仍为 `Long` |
| `QaKnowledgeSyncBatch.java` | `triggerType` | `String` | `QaKnowledgeSyncTriggerType` | DO/result 保持 `String` |
| `QaKnowledgeSyncBatch.java` | `provider` | `String` | `QaKnowledgeProvider` | DO/result 保持 `String` |
| `QaKnowledgeSyncItem.java` | `id` | `Long` | 保留 `Long` 或命名为 internal id | 只服务 `updateById`；不作为 repository save 返回值 |
| `QaKnowledgeSyncItem.java` | `sourceId` | `String` | `KnowledgeSourceId` | DO/result/response 保持 `String`；业务唯一键 |
| `QaKnowledgeSyncItem.java` | `contentType`, `contentId` | `String`, `Long` | `KnowledgeContentRef` | DO/request/response 保持双字段 |
| `QaKnowledgeSyncItem.java` | `knowledgeBaseName` | `String` | `KnowledgeBaseName` | DO/result/response 保持 `String` |
| `QaKnowledgeSyncItem.java` | `provider` | `String` | `QaKnowledgeProvider` | DO/result/response 保持 `String` |
| `QaKnowledgeSyncItem.java` | `externalKnowledgeBaseId` | `String` | `ExternalKnowledgeBaseId` | DO/result/response 保持 `String` |
| `QaKnowledgeSyncItem.java` | `externalKnowledgeItemId` | `String` | `ExternalKnowledgeItemId` | DO/result/response 保持 `String` |
| `QaKnowledgeSyncItem.java` | `syncStatus` | `String` | `QaKnowledgeSyncStatus` | DO/result/response 保持 `String` |

### Search Entity Field Changes

| File | Field | Current | Target | Persistence / API boundary |
| --- | --- | --- | --- | --- |
| `SearchEvent.java` | `id` | `Long` | `SearchEventId` | `SearchEventDO.id` 仍为 `Long`; HTTP `searchEventId` 输出数字字符串 |
| `SearchEvent.java` | `queryText`, `normalizedQueryText`, `displayQueryText` | `String`, `String`, `String` | `SearchKeyword` | DO/response 保持三个字符串字段 |
| `SearchEvent.java` | `intentType` | `SearchIntentType` | 保持 `SearchIntentType` | DO 保持 `String` |
| `SearchEvent.java` | `searchScope` | `SearchScope` | 保持 `SearchScope` | DO 保持 `searchScopesJson` |
| `SearchEvent.java` | `searchStatus` | `String` | `SearchStatus` | DO/response 保持 `String` |
| `SearchEvent.java` | `operatorType`, `operatorId` | `String`, `String` | `SearchOperatorRef` | DO/request/response 保持双字段 |
| `SearchEvent.java` | `requestId` | `String` | `RequestId` | DO/request/response 保持 `String` |
| `SearchEvent.java` | `traceId` | `String` | `TraceId` | DO/request/response 保持 `String` |
| `SearchClickEvent.java` | `id` | `Long` | `SearchClickEventId` | `SearchClickEventDO.id` 仍为 `Long`; HTTP `searchClickEventId` 输出数字字符串 |
| `SearchClickEvent.java` | `searchEventId` | `Long` | `SearchEventId` | DO/request 保持 `Long` 或数字字符串转换 |
| `SearchClickEvent.java` | `contentDomain`, `contentType`, `contentId` | `String`, `String`, `String` | `SearchContentRef` | DO/request/response 保持三字段 |
| `SearchClickEvent.java` | `operatorType`, `operatorId` | `String`, `String` | `SearchOperatorRef` | DO/request 保持双字段 |
| `SearchClickEvent.java` | `requestId` | `String` | `RequestId` | DO/request 保持 `String` |
| `SearchClickEvent.java` | `traceId` | `String` | `TraceId` | DO/request 保持 `String` |
| `QueryUnderstanding.java` | `id` | `Long` | `QueryUnderstandingId` | `QueryUnderstandingDO.id` 仍为 `Long` |
| `QueryUnderstanding.java` | `searchEventId` | `Long` | `SearchEventId` | DO/request 保持 `Long` 或数字字符串转换 |
| `QueryUnderstanding.java` | `queryText`, `normalizedQueryText` | `String`, `String` | `SearchKeyword` | DO/result 保持字符串字段 |
| `QueryUnderstanding.java` | `rewrittenQueryText` | `String` | `RewrittenQueryText` | DO/result 保持 `String` |
| `QueryUnderstanding.java` | `understandingStatus` | `String` | `QueryUnderstandingStatus` | DO/result 保持 `String` |
| `QueryUnderstanding.java` | `requestId` | `String` | `RequestId` | DO/request/result 保持 `String` |
| `QueryUnderstanding.java` | `traceId` | `String` | `TraceId` | DO/request/result 保持 `String` |

## Value Object Files To Add

### QA value objects

新增在 `kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/valueobject/`：

- `QaSessionId.java`
- `QaMessageId.java`
- `QaSourceId.java`
- `QaRetrievalTraceId.java`
- `QaSessionExportId.java`
- `QaKnowledgeSyncBatchId.java`
- `QaOwnerRef.java`
- `QaContextContentRef.java`
- `KnowledgeContentRef.java`
- `KnowledgeSourceId.java`
- `StorageObjectRef.java`
- `RequesterUserRef.java`
- `AiCallRef.java`
- `KnowledgeBaseName.java`
- `QaKnowledgeProvider.java`
- `ExternalKnowledgeBaseId.java`
- `ExternalKnowledgeItemId.java`
- `ExternalKnowledgeItemRefs.java`
- `ExternalChatId.java`
- `ProviderRequestId.java`
- `QaOwnerType.java`
- `QaSessionScope.java`
- `QaContextMode.java`
- `QaSessionStatus.java`
- `QaMessageRole.java`
- `QaAnswerStatus.java`
- `QaSourceStatus.java`
- `QaExportFormat.java`
- `QaExportStatus.java`
- `QaKnowledgeSyncTriggerType.java`
- `QaKnowledgeSyncStatus.java`
- `AiInvocationStatusRef.java`

### Search value objects

新增在 `kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/valueobject/`：

- `SearchEventId.java`
- `SearchClickEventId.java`
- `QueryUnderstandingId.java`
- `SearchOperatorRef.java`
- `SearchContentRef.java`
- `SearchStatus.java`
- `QueryUnderstandingStatus.java`
- `RewrittenQueryText.java`

### Codecs

新增在 `kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/codec/`：

- `QaSessionIdCodec.java`
- `QaMessageIdCodec.java`
- `QaSourceIdCodec.java`
- `QaRetrievalTraceIdCodec.java`
- `QaSessionExportIdCodec.java`
- `QaKnowledgeSyncBatchIdCodec.java`
- `QaOwnerRefCodec.java`
- `QaContextContentRefCodec.java`
- `KnowledgeContentRefCodec.java`
- `KnowledgeSourceIdCodec.java`
- `StorageObjectRefCodec.java`
- `RequesterUserRefCodec.java`
- `AiCallRefCodec.java`
- `QaStringValueCodec.java`

新增在 `kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/codec/`：

- `SearchEventIdCodec.java`
- `SearchClickEventIdCodec.java`
- `QueryUnderstandingIdCodec.java`
- `SearchOperatorRefCodec.java`
- `SearchContentRefCodec.java`
- `SearchStatusCodec.java`
- `QueryUnderstandingStatusCodec.java`
- `SearchTraceabilityCodec.java`

ID 类型规则：

- `QaSessionId`、`QaMessageId`、`QaSourceId`、`QaRetrievalTraceId`、`QaSessionExportId`、`QaKnowledgeSyncBatchId`、`SearchEventId`、`SearchClickEventId`、`QueryUnderstandingId` 继承 `BaseLongId`。
- `KnowledgeSourceId`、`ExternalKnowledgeBaseId`、`ExternalKnowledgeItemId`、`ExternalChatId`、`ProviderRequestId` 底层是字符串，继承 `BaseStringId` 或使用不可变单值包装。
- `*Id.java` 只保留 public 构造器，不放 `static` 方法。
- nullable、字符串解析、基础值输出、列表转换统一放进对应 codec。

## Repository Contract Changes

| File | Current signature | Target signature |
| --- | --- | --- |
| `QaSessionRepository.java` | `QaSession getById(Long id)` | `QaSession getById(QaSessionId id)` |
| `QaSessionRepository.java` | `default QaSession getBySessionId(Long sessionId)` | 删除或改为 `getBySessionId(QaSessionId sessionId)` |
| `QaSessionRepository.java` | `List<QaSession> listByOwnerUserId(String ownerType, String ownerId, Integer limit)` | `List<QaSession> listByOwnerUserId(QaOwnerRef owner, Integer limit)` |
| `QaSessionRepository.java` | `Long save(QaSession entity)` | `QaSessionId save(QaSession entity)` |
| `QaSessionRepository.java` | `int markRemoved(Long id, Date removedAt)` | `int markRemoved(QaSessionId id, Date removedAt)` |
| `QaMessageRepository.java` | `QaMessage getById(Long id)` | `QaMessage getById(QaMessageId id)` |
| `QaMessageRepository.java` | `default QaMessage getByMessageId(Long messageId)` | 删除或改为 `getByMessageId(QaMessageId messageId)` |
| `QaMessageRepository.java` | `List<QaMessage> listBySessionId(Long sessionId)` | `List<QaMessage> listBySessionId(QaSessionId sessionId)` |
| `QaMessageRepository.java` | `Long save(QaMessage entity)` | `QaMessageId save(QaMessage entity)` |
| `QaSourceRepository.java` | `List<QaSource> listByMessageId(Long messageId)` | `List<QaSource> listByMessageId(QaMessageId messageId)` |
| `QaSourceRepository.java` | `Long save(QaSource entity)` | `QaSourceId save(QaSource entity)` |
| `QaSourceRepository.java` | `int deleteByMessageId(Long messageId)` | `int deleteByMessageId(QaMessageId messageId)` |
| `QaRetrievalTraceRepository.java` | `QaRetrievalTrace getById(Long id)` | `QaRetrievalTrace getById(QaRetrievalTraceId id)` |
| `QaRetrievalTraceRepository.java` | `default QaRetrievalTrace getByTraceId(Long traceId)` | 删除或改为 `getByTraceId(QaRetrievalTraceId traceId)` |
| `QaRetrievalTraceRepository.java` | `QaRetrievalTrace getByMessageId(Long messageId)` | `QaRetrievalTrace getByMessageId(QaMessageId messageId)` |
| `QaRetrievalTraceRepository.java` | `Long save(QaRetrievalTrace entity)` | `QaRetrievalTraceId save(QaRetrievalTrace entity)` |
| `QaSessionExportRepository.java` | `QaSessionExport getById(Long id)` | `QaSessionExport getById(QaSessionExportId id)` |
| `QaSessionExportRepository.java` | `default QaSessionExport getByExportId(Long exportId)` | 删除或改为 `getByExportId(QaSessionExportId exportId)` |
| `QaSessionExportRepository.java` | `Long save(QaSessionExport entity)` | `QaSessionExportId save(QaSessionExport entity)` |
| `QaKnowledgeSyncBatchRepository.java` | `QaKnowledgeSyncBatch getById(Long id)` | `QaKnowledgeSyncBatch getById(QaKnowledgeSyncBatchId id)` |
| `QaKnowledgeSyncBatchRepository.java` | `default QaKnowledgeSyncBatch getByBatchId(Long batchId)` | 删除或改为 `getByBatchId(QaKnowledgeSyncBatchId batchId)` |
| `QaKnowledgeSyncBatchRepository.java` | `Long save(QaKnowledgeSyncBatch entity)` | `QaKnowledgeSyncBatchId save(QaKnowledgeSyncBatch entity)` |
| `QaKnowledgeSyncItemRepository.java` | `QaKnowledgeSyncItem getBySourceId(String sourceId)` | `QaKnowledgeSyncItem getBySourceId(KnowledgeSourceId sourceId)` |
| `QaKnowledgeSyncItemRepository.java` | `List<QaKnowledgeSyncItem> listBySyncStatus(String syncStatus, Integer limit)` | `List<QaKnowledgeSyncItem> listBySyncStatus(QaKnowledgeSyncStatus syncStatus, Integer limit)` |
| `QaKnowledgeSyncItemRepository.java` | `Long save(QaKnowledgeSyncItem entity)` | `KnowledgeSourceId save(QaKnowledgeSyncItem entity)` |
| `SearchEventRepository.java` | `SearchEvent getById(Long id)` | `SearchEvent getById(SearchEventId id)` |
| `SearchEventRepository.java` | `default SearchEvent getBySearchEventId(String searchEventId)` | 删除或改为 `getBySearchEventId(SearchEventId searchEventId)` |
| `SearchEventRepository.java` | `Long save(SearchEvent entity)` | `SearchEventId save(SearchEvent entity)` |
| `SearchEventRepository.java` | `page(String queryText, String intentType, String searchStatus, String operatorId, int pageNo, int pageSize)` | `page(SearchKeyword keyword, SearchIntentType intentType, SearchStatus status, SearchOperatorRef operator, int pageNo, int pageSize)` |
| `SearchClickEventRepository.java` | `SearchClickEvent getById(Long id)` | `SearchClickEvent getById(SearchClickEventId id)` |
| `SearchClickEventRepository.java` | `default SearchClickEvent getBySearchClickEventId(String searchClickEventId)` | 删除或改为 `getBySearchClickEventId(SearchClickEventId searchClickEventId)` |
| `SearchClickEventRepository.java` | `Long save(SearchClickEvent entity)` | `SearchClickEventId save(SearchClickEvent entity)` |
| `QueryUnderstandingRepository.java` | `QueryUnderstanding getBySearchEventId(Long searchEventId)` | `QueryUnderstanding getBySearchEventId(SearchEventId searchEventId)` |
| `QueryUnderstandingRepository.java` | `default QueryUnderstanding getBySearchEventId(String searchEventId)` | 删除 |
| `QueryUnderstandingRepository.java` | `Long save(QueryUnderstanding entity)` | `QueryUnderstandingId save(QueryUnderstanding entity)` |

`QaKnowledgeSyncItemRepository.save` 返回 `KnowledgeSourceId`。当前 `QaKnowledgeSyncItemDO.id` 仍保留为数据库行标识，可在 infra 内由 `SnowflakeIdGenerator` 生成并写入 DO；repository 业务契约不得把这个数据库 `id` 暴露为同步条目的领域 ID。`update` 可继续依赖已加载实体携带的数据库 `id` 调用 `updateById`，但应用层判断 insert/update 仍应以 `getBySourceId(KnowledgeSourceId)` 的结果为准。

## Implementation Batches

每个批次控制在 2-8 个生产文件；测试文件跟随对应批次修改，不计入批次上限。

### Batch 1: QA value objects and codecs

生产文件：

- `QaSessionId.java`
- `QaMessageId.java`
- `QaSourceId.java`
- `QaRetrievalTraceId.java`
- `QaSessionExportId.java`
- `QaKnowledgeSyncBatchId.java`
- `QaSessionIdCodec.java`
- `QaMessageIdCodec.java`

目标：

- 建立 QA Long-backed ID 写法。
- 跑 `DiscoveryDomainArchitectureTest`，确认 `*Id` 无 static 方法和 BaseId 规则。

### Batch 2: QA refs, statuses, and text values

生产文件：

- `QaOwnerRef.java`
- `QaContextContentRef.java`
- `KnowledgeContentRef.java`
- `KnowledgeSourceId.java`
- `QaSessionStatus.java`
- `QaMessageRole.java`
- `QaKnowledgeSyncStatus.java`
- `QaStringValueCodec.java`

目标：

- 覆盖 QA owner、content、status、knowledge source 的最小值对象。
- 先满足 `QaSession`、`QaMessage`、`QaKnowledgeSyncItem` 的改造，不一次性塞满全部 QA 类型。

### Batch 3: Search value objects and codecs

生产文件：

- `SearchEventId.java`
- `SearchClickEventId.java`
- `QueryUnderstandingId.java`
- `SearchOperatorRef.java`
- `SearchContentRef.java`
- `SearchStatus.java`
- `QueryUnderstandingStatus.java`
- `SearchEventIdCodec.java`

目标：

- Search 主 ID 全部 Long-backed。
- 数字字符串解析只放在 codec 或 interface/application 边界。

### Batch 4: QA entities and repositories

生产文件：

- `QaSession.java`
- `QaMessage.java`
- `QaKnowledgeSyncItem.java`
- `QaSessionRepository.java`
- `QaMessageRepository.java`
- `QaKnowledgeSyncItemRepository.java`

目标：

- 精确替换上述文件内字段和仓储签名。
- `QaKnowledgeSyncItem.id` 不作为业务 ID；`sourceId` 改为 `KnowledgeSourceId`。

### Batch 5: QA infra persistence boundary

生产文件：

- `QaPersistenceAssembler.java`
- `QaSessionRepositoryImpl.java`
- `QaMessageRepositoryImpl.java`
- `QaKnowledgeSyncItemRepositoryImpl.java`

目标：

- DO 和 mapper 保持基础类型。
- repository impl 到 mapper/DO 边界调用 codec。
- `QaKnowledgeSyncItemRepositoryImpl.save` 返回 `KnowledgeSourceId`，同时由 infra 生成并写入 DO 的 `id`。

### Batch 6: QA application and interface adapters

生产文件：

- `QaApplicationServiceImpl.java`
- `KnowledgeQaApplicationServiceImpl.java`
- `KnowledgeSyncApplicationServiceImpl.java`
- `QaSourceAssembler.java`
- `QaTraceAssembler.java`
- `DiscoveryQaAdminInterfaceAssembler.java`
- `DiscoveryQaPortalInterfaceAssembler.java`

目标：

- application 编排内部使用强类型。
- interface response 继续输出基础类型。
- 同步条目 insert/update 继续以 `getBySourceId(KnowledgeSourceId)` 结果判断。

### Batch 7: Search entities, repositories, and infra

生产文件：

- `SearchEvent.java`
- `SearchClickEvent.java`
- `QueryUnderstanding.java`
- `SearchEventRepository.java`
- `SearchClickEventRepository.java`
- `QueryUnderstandingRepository.java`
- `SearchEventPersistenceAssembler.java`
- `SearchClickEventPersistenceAssembler.java`

目标：

- 删除 search entity 内 `Long.valueOf(String)` 解析职责。
- search DO 保持基础类型。
- repository save 返回强类型 ID。

### Batch 8: Search application and interface adapters

生产文件：

- `SearchApplicationServiceImpl.java`
- `QueryUnderstandingApplicationServiceImpl.java`
- `DiscoverySearchPortalInterfaceAssembler.java`
- `DiscoverySearchStatisticsInterfaceAssembler.java`
- `DiscoverySearchRequest.java`
- `DiscoverySearchClickEventRequest.java`
- `DiscoverySearchResponse.java`
- `DiscoverySearchEventResponse.java`

目标：

- `searchEventId`、`searchClickEventId`、`queryUnderstandingId` 只接受数字字符串并在边界转换。
- `requestId`、`traceId` 进入 application 后转换为 common-core `RequestId`、`TraceId`。
- response 字段保持基础类型，不向 HTTP 暴露值对象结构。

## Test File Map

测试文件按影响面跟随对应 batch 修改；每次提交仍按仓库提交规则控制 1-5 个文件。

### Domain architecture tests

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/test/java/com/thundax/kuzhambu/discovery/domain/DiscoveryDomainArchitectureTest.java`

必须覆盖：

- 新增 `domain/qa/model/valueobject/*Id.java`。
- 新增 `domain/search/model/valueobject/*Id.java`。
- 新增 `domain/qa/codec/*.java` 和 `domain/search/codec/*.java` 的 placement。

### QA application and infra tests

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImplAdminReadTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeQaApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeSyncApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSessionRepositoryImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaMessageRepositoryImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaKnowledgeSyncItemRepositoryImplTest.java`

必须覆盖：

- repository stub 返回 `QaSessionId`、`QaMessageId`、`KnowledgeSourceId`。
- `QaKnowledgeSyncItemRepository.save` 返回 `KnowledgeSourceId` 后，application 不再把返回值写入数据库 `id`。
- interface response 中 `sourceId` 仍是 `SANCAI_ENTRY:1001` 这类字符串。

### Search application and infra tests

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/QueryUnderstandingApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/search/persistence/assembler/SearchEventPersistenceAssemblerTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/search/repository/impl/SearchClickEventRepositoryImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/controller/DiscoverySearchPortalControllerTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/assembler/DiscoverySearchPortalInterfaceAssemblerTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/DiscoverySearchStatisticsControllerTest.java`

必须覆盖：

- 所有 `searchEventId` fixture 使用 `"1"`、`"2"` 这类数字字符串。
- 不再使用 `"search-1"`、`"s-1"` 作为有效搜索事件 ID。
- `requestId`、`traceId` 在 application 内部转换为 common-core `RequestId`、`TraceId`，HTTP fixture 仍为字符串。

### Portal frontend tests

- `kuzhambu-apps/portal-web/src/pages/discovery/search/search-page.test.tsx`
- `kuzhambu-apps/portal-web/src/pages/discovery/search/search-page-results.test.tsx`
- `kuzhambu-apps/portal-web/src/pages/discovery/search/search-page-filters.test.tsx`
- `kuzhambu-apps/portal-web/src/pages/discovery/search/search-page-pagination.test.tsx`
- `kuzhambu-apps/portal-web/src/pages/discovery/search/search-service.test.ts`
- `kuzhambu-apps/portal-web/src/pages/discovery/qa/qa-page.test.tsx`
- `kuzhambu-apps/portal-web/src/pages/discovery/qa/qa-page-session.test.tsx`
- `kuzhambu-apps/portal-web/src/pages/discovery/qa/qa-page-context.test.tsx`

必须覆盖：

- 搜索结果点击发送数字字符串 `searchEventId`。
- 搜索筛选、分页和空态不受 ID 类型调整影响。
- QA 新建会话、选择会话、发送问题、查看来源四个操作继续使用基础类型字段。

## Frontend Impact And Acceptance

本轮后端 HTTP 字段名不变，前端主要验收“数字字符串 ID”兼容性和用户操作不回退。若测试发现前端类型假设不匹配，只改以下 portal-web 文件，不改 admin-web。

### Portal search page

相关文件：

- `kuzhambu-apps/portal-web/src/pages/discovery/search/search-page.tsx`
- `kuzhambu-apps/portal-web/src/pages/discovery/search/components/search-controls.tsx`
- `kuzhambu-apps/portal-web/src/pages/discovery/search/components/search-results.tsx`
- `kuzhambu-apps/portal-web/src/pages/discovery/search/search-service.ts`
- `kuzhambu-apps/portal-web/src/pages/discovery/search/search-types.ts`

控件和操作：

- 搜索输入框：用户输入关键词后点击搜索按钮或按 Enter；请求仍发送 `queryText`、`requestId`、`traceId`。
- 筛选控件：知识库、分类、标签、内容状态、可见性和日期筛选仍按现有控件提交；不新增强类型字段。
- 结果列表卡片/行：点击结果标题或目标链接时，click event 请求必须携带数字字符串 `searchEventId`。
- 分页控件：切换页码和每页条数时继续使用当前 `pageNo`、`pageSize`；不把 page 状态和 `SearchEventId` 混用。
- 空态/失败态：搜索失败时不显示 `[object Object]` 类型的值对象文本。

验收：

- `searchEventId` 从接口返回 `"1"` 这类数字字符串时，点击任一结果能调用 `/api/portal/discovery/search/click`。
- 前端测试 fixture 中不得再使用 `"search-1"`、`"s-1"` 作为 search event ID。
- URL、按钮文案、表格/卡片展示不出现值对象 JSON 包裹结构，例如 `{"value":1}`。

### Portal QA page

相关文件：

- `kuzhambu-apps/portal-web/src/pages/discovery/qa/qa-page.tsx`
- `kuzhambu-apps/portal-web/src/pages/discovery/qa/components/qa-composer.tsx`
- `kuzhambu-apps/portal-web/src/pages/discovery/qa/components/qa-timeline.tsx`
- `kuzhambu-apps/portal-web/src/pages/discovery/qa/qa-service.ts`
- `kuzhambu-apps/portal-web/src/pages/discovery/qa/qa-types.ts`

控件和操作：

- 新建会话按钮：返回的 `sessionId` 仍可被前端作为数字字符串或 number 存储并用于后续提问。
- 会话列表项：点击会话列表项时，详情请求继续传基础类型 `sessionId`。
- 提问输入框：用户输入问题后点击发送按钮或按 Enter；请求仍发送 `sessionId`、`question`、`requestId`、`traceId`。
- 流式回答区域：SSE/stream 响应中的 message id、source id、trace id 展示仍为基础类型文本。
- 来源列表：点击来源条目进入内容详情时继续使用 `contentType` + `contentId`，不使用内部数据库 `QaSourceId`。

验收：

- 新建会话、选择会话、发送问题、查看来源四个操作不因强类型化改变请求字段名。
- 页面不展示值对象结构文本。
- `sourceId` 展示仍为 `SANCAI_ENTRY:1001` 这类业务来源号。

### Admin sync UI

当前 `admin-web` 未发现 Discovery 知识同步页面。本轮只保证 admin HTTP response 基础字段不变：

- `QaSyncItemResponse.sourceId` 仍为字符串业务唯一键。
- `QaSyncItemResponse.contentType`、`contentId`、`syncStatus`、`externalKnowledgeItemId` 字段名不变。
- 如果后续新增 admin 同步页面，控件颗粒度应包含：`syncStatus` 下拉筛选、`contentType` 下拉筛选、刷新按钮、手动同步按钮、删除同步按钮、同步条目表格列 `sourceId/contentType/contentId/syncStatus/failureReason/updatedAt`。

## High-risk Call Sites

- `QaApplicationServiceImpl.java`：`markRemoved`、`getBySessionId`、`listByMessageId`、`getByMessageId`、会话导出文件名拼接需要显式取 `value()`。
- `KnowledgeQaApplicationServiceImpl.java`：问题消息和答案消息保存后返回值会从 `Long` 变为 `QaMessageId`，后续 source/trace 写入必须传强类型。
- `KnowledgeSyncApplicationServiceImpl.java`：`sourceId(command)` 从 `String` 调整为 `KnowledgeSourceId`，分页筛选里的 sync status 调整为 `QaKnowledgeSyncStatus`。
- `KnowledgeSyncApplicationServiceImpl.java`：insert/update 判断继续以 `getBySourceId(KnowledgeSourceId)` 是否返回实体为准；`save` 返回 `KnowledgeSourceId` 后，不能再把返回值写回数据库 `id` 字段。
- `SearchApplicationServiceImpl.java`：`searchEventRepository.save(searchEvent)` 返回值会从 `Long` 变为 `SearchEventId`；`searchEvent.setId(...)`、`toSearchResult(...)`、`toSearchEventResult(...)` 和 `getEvent(Long)` 需要明确转换边界。
- `SearchApplicationServiceImpl.java`：`recordClick` 当前从 `SearchClickEventCreateCommand.searchEventId` 构造点击事件，必须把 String/Long 兼容解析移到 codec 或 interface/application 边界。
- `QueryUnderstandingApplicationServiceImpl.java`：`toSucceededEntity`、`toFailedEntity` 当前构造 `QueryUnderstanding` 时传入 `null` 字符串业务 ID 和 `query.getSearchEventId()`；改造后要分别处理 `QueryUnderstandingId` 生成/保存返回值、`SearchEventId` 关联和 result 输出。
- `DiscoverySearchPortalInterfaceAssemblerTest.java`、`DiscoverySearchStatisticsControllerTest.java`、`DiscoverySearchPortalControllerTest.java`：现有 fixture 中存在 `"search-1"`、`"s-1"` 等非数字搜索事件 ID，必须统一成数字字符串。

## Verification

先运行窄格式化，再运行 discovery 相关检查：

```sh
cd kuzhambu-servers
mvn -pl biz/discovery -am spotless:apply
mvn -pl biz/discovery -am spotless:check
mvn -pl biz/discovery -am checkstyle:check
mvn -pl biz/discovery -am test
```

如果只完成某个子域，可先跑更窄测试：

```sh
cd kuzhambu-servers
mvn -pl biz/discovery/kuzhambu-discovery-domain test
mvn -pl biz/discovery/kuzhambu-discovery-infra test
mvn -pl biz/discovery/kuzhambu-discovery-application test
mvn -pl biz/discovery/kuzhambu-discovery-interface test
```

如修改 portal-web：

```sh
cd kuzhambu-apps
pnpm --filter portal-web run format
pnpm run format:check
pnpm run lint
pnpm --filter portal-web run test
```

改造后必须人工检查：

- `git diff` 中 DO、mapper、HTTP request/response 没有被误改成强类型。
- `domain/**/model/valueobject/*Id.java` 没有 `static` 方法。
- `domain/**/model/entity/*.java` 类级 Lombok 注解仍只有 `@Getter`、`@Setter`、`@NoArgsConstructor`、`@AllArgsConstructor`。
- repository 端口不再以 `Long`/`String` 表达已盘点的业务 ID、状态和引用。
- portal search 点击结果时，click 请求里的 `searchEventId` 是数字字符串。
- portal QA 来源展示仍显示 `sourceId` 业务来源号，不显示数据库 `id`。

## Residual Risks

- `QaKnowledgeSyncItem.sourceId` 是业务唯一键，但当前代码未在仓库内找到建表 SQL 或唯一索引定义证据。强类型改造不调整数据库结构；如果实际数据库缺少 `source_id` 唯一约束，并发 `syncContent` 可能在 `getBySourceId` 后插入重复记录。该问题应作为单独数据库收口项处理。
- 每个 implementation batch 控制生产文件数，不代表一次 commit 必须只提交一个 batch；提交时仍遵守仓库“1-5 文件、聚焦提交”的约定。

## Closure

本 RUNBOOK 是临时执行手册。强类型化改造完成并通过验证后删除本文件；如发现新的稳定规则，迁移到 `docs/00-governance/SERVERS-UNIFIED-ID-DESIGN.md` 或 `docs/00-governance/SERVERS-ARCHITECTURE-RULES.md` 后再删除。
