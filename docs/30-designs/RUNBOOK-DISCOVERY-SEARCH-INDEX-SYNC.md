# RUNBOOK: Discovery Search Index Sync

## 1. Goal

本 RUNBOOK 的目标是把 Discovery Search 从“手动全量重建”推进到“事务后增量同步 + 管理端全量重建兜底”。

本轮完成后应满足：

- Classics 内容写入成功后，不在主事务内直接更新 Elasticsearch。
- Classics 内容写入成功后，在 `afterCommit` 阶段投递 RocketMQ 索引同步消息。
- Discovery 消费消息后，按 `currentVersionNo` 做幂等增量 `upsert / delete`。
- `DELETE` 进入索引删除态后，能由计划任务按保留期做定期物理清理。
- Admin 侧已有 `index/rebuild` 继续保留，作为消息失败或索引漂移时的恢复兜底入口。

## 2. Fixed Decisions

- 消息队列使用仓库现有 `RocketMQ` 接入，不引入新的 MQ 技术栈。
- 本轮不引入 outbox 表；采用 `TransactionSynchronizationManager.afterCommit` 发送消息。
- 幂等基准固定使用 `currentVersionNo`，不使用 Audit 日志时间。
- 接受 `afterCommit` 直接发送 MQ 的一致性语义；若发送失败，不额外补事务内持久化兜底，由管理端 `rebuild` 恢复。
- 消费端收到消息后，不直接信任消息体内容，不在消息体中携带完整搜索文档。
- 消费端只使用消息体中的 `contentType + contentId + currentVersionNo + eventType`，随后回查最新业务数据。
- 旧消息判定规则固定为：`message.currentVersionNo < db.currentVersionNo` 时直接跳过。
- `DELETE` 不做 ES 物理删除；改为把索引文档标记为删除态，并写入该次消息的 `currentVersionNo`。
- `UPSERT` 消息只在 `message.currentVersionNo = db.currentVersionNo` 且当前内容仍可公开消费时继续执行。
- `currentVersionNo` 被视为搜索面变更的统一版本号；凡是影响 Discovery 搜索索引字段的业务变更，都必须推进 `currentVersionNo`。
- 管理端 `POST /api/discovery/search-admin/index/rebuild` 继续保留，不改路径。
- 本轮内容源范围仍只处理 `SANCAI_ENTRY / WANGQI_DOCUMENT / MING_CUSTOMS`。
- 删除态清理属于本轮正式范围，不允许推迟到后续子任务再补。

## 3. Data Field Changes

### 3.1 MQ Payload

新增搜索索引同步消息体 `ClassicsSearchIndexSyncMessage`，字段固定如下：

- `eventId: String`
- `eventType: String`
  - 固定值：`UPSERT`、`DELETE`
- `contentType: String`
  - 固定值：`SANCAI_ENTRY`、`WANGQI_DOCUMENT`、`MING_CUSTOMS`
- `contentId: String`
- `currentVersionNo: Integer`
- `occurredAt: Date`

### 3.4 MQ Configuration Keys

本轮新增并固定以下配置键，不允许临时改名：

- `kuzhambu.discovery.search.index-sync.topic`
- `kuzhambu.discovery.search.index-sync.producer-tag-upsert`
- `kuzhambu.discovery.search.index-sync.producer-tag-delete`
- `kuzhambu.discovery.search.index-sync.consumer-group`
- `kuzhambu.discovery.search.index-sync.deleted-retention-days`
- `kuzhambu.discovery.search.index-sync.deleted-cleanup-cron`

### 3.2 Application Result Fields

下列 application 层结果对象新增字段：

- `ClassicsSearchSourceContent.currentVersionNo: Integer`
- `SearchSourceContent.currentVersionNo: Integer`

### 3.3 Elasticsearch Document Fields

下列 ES 文档字段新增：

- `DiscoverySearchDocument.sourceVersionNo: Integer`
- `DiscoverySearchDocument.deleted: Boolean`
- `DiscoverySearchDocument.deletedAt: Instant`

该字段用于消费端幂等判定：

- 消息 `currentVersionNo < sourceVersionNo`：丢弃
- 消息 `currentVersionNo = sourceVersionNo`：允许覆盖或跳过
- 消息 `currentVersionNo > sourceVersionNo`：执行更新

删除态规则固定如下：

- `deleted = false`：当前文档可参与搜索
- `deleted = true`：当前文档保留在索引中，但搜索查询必须过滤掉该文档
- `deletedAt = null`：当前文档不是删除态
- `deletedAt != null`：表示该文档进入删除态的时间，用于计划任务超时清理

## 4. Message And Sync Rules

### 4.1 Producer Rules

- Producer 只在业务事务成功提交后发送消息。
- 业务事务回滚时不得发送任何索引同步消息。
- 同一业务动作只发送一条消息。
- 可公开消费内容新增、编辑、发布、转公开时发送 `UPSERT`。
- 可公开消费内容删除、转私有、转不可发布状态时发送 `DELETE`。

### 4.2 Consumer Rules

- Consumer 收到消息后，先按 `contentType + contentId` 查询当前最新业务内容。
- 若消息 `eventType = DELETE`：
  - 不比较 DB 当前公开性
  - 若 `message.currentVersionNo < sourceVersionNo`：直接跳过
  - 若 `message.currentVersionNo >= sourceVersionNo`：将 ES 文档更新为删除态
  - 删除态更新必须写入：
    - `sourceVersionNo = message.currentVersionNo`
    - `deleted = true`
    - `deletedAt = occurredAt`
- 若消息 `eventType = UPSERT`：
  - 若查不到当前内容：直接跳过，不执行 `delete`
  - 若 `message.currentVersionNo < db.currentVersionNo`：直接跳过
  - 若 `message.currentVersionNo = db.currentVersionNo`：
    - 当前内容不可公开消费：直接跳过，不执行 `delete`
    - 当前内容可公开消费：继续比较 ES 文档 `sourceVersionNo`
  - 比较 ES 版本时：
    - `db.currentVersionNo < sourceVersionNo`：跳过
    - `db.currentVersionNo = sourceVersionNo`：允许覆盖或跳过
    - `db.currentVersionNo > sourceVersionNo`：执行 `upsert`
  - `upsert` 成功后必须写入：
    - `sourceVersionNo = db.currentVersionNo`
    - `deleted = false`
    - `deletedAt = null`

### 4.3 Deleted Tombstone Cleanup Rules

- 计划任务只处理 `deleted = true` 且 `deletedAt` 早于保留期限阈值的文档。
- 物理清理时，按 `documentId` 删除 Elasticsearch 文档。
- 计划任务不负责业务回查，不负责重新判断公开性。
- 若新 `UPSERT` 在计划任务清理前到达，仍按 `sourceVersionNo` 判定并可把 `deleted` 改回 `false`。
- 默认保留期由 `kuzhambu.discovery.search.index-sync.deleted-retention-days` 控制。
- 默认执行周期由 `kuzhambu.discovery.search.index-sync.deleted-cleanup-cron` 控制。

## 5. File Tasks

以下任务按执行顺序排列。每个任务必须一次只覆盖列出的文件，不允许额外扩散。

### T1. 定义索引同步消息契约

目标：

- 定义 `UPSERT / DELETE` 消息类型和标准消息体。

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/searchsync/model/ClassicsSearchIndexSyncEventType.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/searchsync/model/ClassicsSearchIndexSyncMessage.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/searchsync/service/ClassicsSearchIndexSyncPublisher.java`

完成标准：

- `eventType` 只允许 `UPSERT / DELETE`
- 消息字段完整包含 `eventId / eventType / contentType / contentId / currentVersionNo / occurredAt`
- publisher 接口方法签名只接受标准消息体

### T2. 创建事务后发送支持类

目标：

- 在 Classics application 内封装 `afterCommit` 发送逻辑，避免每个应用服务手写事务回调。

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/searchsync/support/ClassicsSearchIndexSyncPublishSupport.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/searchsync/ClassicsSearchIndexSyncPublishSupportTest.java`

完成标准：

- 支持 `publishUpsertAfterCommit(...)`
- 支持 `publishDeleteAfterCommit(...)`
- 仅在事务提交后调用 publisher
- 无事务上下文时抛出清晰异常，不允许静默降级

### T3. 接入 RocketMQ producer 实现与配置

目标：

- 用现有 `KuzhambuMqSender` 实现搜索索引同步消息发送。

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/searchsync/mq/RocketMqClassicsSearchIndexSyncPublisher.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/pom.xml`
- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/resources/application.yml`
- `.env.example`
- `deploy/.env.example`

完成标准：

- producer 统一发送到固定 topic
- `UPSERT / DELETE` 使用 tag 区分
- `key` 固定为 `contentType:contentId:currentVersionNo`
- admin starter 中存在以下默认配置：
  - `kuzhambu.discovery.search.index-sync.topic`
  - `kuzhambu.discovery.search.index-sync.producer-tag-upsert`
  - `kuzhambu.discovery.search.index-sync.producer-tag-delete`
  - `kuzhambu.discovery.search.index-sync.deleted-retention-days`
  - `kuzhambu.discovery.search.index-sync.deleted-cleanup-cron`
- `.env.example` 与 `deploy/.env.example` 补齐可覆盖的以下环境变量：
  - `KUZHAMBU_DISCOVERY_SEARCH_INDEX_SYNC_TOPIC`
  - `KUZHAMBU_DISCOVERY_SEARCH_INDEX_SYNC_TAG_UPSERT`
  - `KUZHAMBU_DISCOVERY_SEARCH_INDEX_SYNC_TAG_DELETE`
  - `KUZHAMBU_DISCOVERY_SEARCH_INDEX_SYNC_DELETED_RETENTION_DAYS`
  - `KUZHAMBU_DISCOVERY_SEARCH_INDEX_SYNC_DELETED_CLEANUP_CRON`

### T4. 为公开内容查询补充单条读取能力

目标：

- 在不读取 mapper/DO 的前提下，为 Discovery 消费端提供“按内容类型和 ID 读取当前公开内容”能力。

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/search/service/ClassicsSearchContentApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/search/service/impl/ClassicsSearchContentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/SearchContentProvider.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/ClassicsSearchContentProvider.java`

完成标准：

- `ClassicsSearchContentApplicationService` 新增单条读取方法
- 读取结果只返回“当前仍可公开消费内容”
- `SearchContentProvider` 同步暴露单条查询方法
- Discovery application 不直接依赖 Classics mapper/DO

### T5. 传播 `currentVersionNo` 到搜索内容结果对象

目标：

- 为后续 ES 文档和消费者幂等判定提供版本号。

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/search/result/ClassicsSearchSourceContent.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/search/service/impl/ClassicsSearchContentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/result/SearchSourceContent.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/ClassicsSearchContentProvider.java`

完成标准：

- 三类内容构造搜索结果时都写入 `currentVersionNo`
- Discovery application 收到的 `SearchSourceContent` 保留该字段

### T6. 扩展 Discovery 索引同步应用服务契约

目标：

- 在 Discovery application 内提供消费端用的单条增量同步入口。

文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/SearchIndexSyncApplicationService.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchIndexSyncApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/SearchIndexGateway.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchIndexSyncApplicationServiceImplTest.java`

完成标准：

- application service 至少包含 `syncUpsert(...)`、`syncDelete(...)`
- `syncUpsert(...)` 通过 `SearchContentProvider` 回查最新公开内容
- `syncDelete(...)` 通过 gateway 删除索引文档
- 测试覆盖“旧版本丢弃 / 同版本幂等 / 新版本更新 / 当前内容不存在时删除”

### T7. 扩展 ES 文档字段与 Gateway 幂等更新能力

目标：

- 给 ES 文档增加 `sourceVersionNo`，并支持单条 `upsert/delete`。

文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/client/DiscoverySearchDocument.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/client/DiscoverySearchDocumentAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/client/ElasticsearchSearchIndexGateway.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/client/ElasticsearchSearchIndexGatewayTest.java`

完成标准：

- `DiscoverySearchDocument` 新增 `sourceVersionNo`
- `DiscoverySearchDocument` 新增 `deleted`
- `DiscoverySearchDocument` 新增 `deletedAt`
- assembler 正确映射 `SearchSourceContent.currentVersionNo`
- gateway 支持读取现存文档版本与删除态，并按版本判定是否执行 `upsert`
- gateway 支持按 `documentId` 将文档更新为删除态，而不是物理删除
- gateway 支持按 `deletedAt` 阈值物理清理超时删除态文档

### T8. 接入 Discovery MQ consumer 与开关配置

目标：

- 在 Discovery admin 接口侧消费索引同步消息，并避免 portal starter 误启动 consumer。

文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/mq/RocketMqDiscoverySearchIndexSyncConsumer.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/mq/RocketMqDiscoverySearchIndexSyncConsumerTest.java`
- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/resources/application.yml`
- `kuzhambu-servers/starter/kuzhambu-portal-starter/src/main/resources/application.yml`

完成标准：

- consumer 只消费 Discovery 索引同步 topic
- consumer 调用 `SearchIndexSyncApplicationService`
- admin starter 显式配置以下键：
  - `kuzhambu.discovery.search.index-sync.topic`
  - `kuzhambu.discovery.search.index-sync.consumer-group`
- portal starter 显式关闭 consumer，避免重复消费

### T8b. 接入删除态文档计划清理任务

目标：

- 为删除态索引文档增加定时物理清理任务，补齐 tombstone 生命周期闭环。

文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/SearchIndexCleanupApplicationService.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchIndexCleanupApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/task/DiscoverySearchDeletedDocumentCleanupTask.java`
- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/resources/application.yml`

完成标准：

- application service 支持“按保留期限物理清理删除态文档”
- task 按 cron 定时触发 cleanup
- admin starter 提供默认 `deleted-retention-days` 和 `deleted-cleanup-cron`
- task 只调用 Discovery application service，不直接操作 Elasticsearch client
- `T7` 中的 gateway 测试覆盖“只清理超时删除态文档，不清理未超时或未删除文档”

### T9a. 在 Sancai 写路径接入事务后消息发送

目标：

- 在 Sancai 主写路径接入 `afterCommit` 索引同步消息。

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/SancaiApplicationServiceImplTest.java`

完成标准：

- 创建、更新、发布、转私有等会影响公开检索面的操作全部接入消息发送
- 可公开状态发送 `UPSERT`
- 不可公开状态发送 `DELETE`
- 测试验证 publisher 收到正确 `contentType/contentId/currentVersionNo/eventType`

### T9b. 在 Wangqi 写路径接入事务后消息发送

目标：

- 在 Wangqi 主写路径接入 `afterCommit` 索引同步消息。

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/service/impl/WangqiDocumentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/wangqi/WangqiDocumentApplicationServiceImplTest.java`

完成标准：

- 创建、更新、发布、转私有等会影响公开检索面的操作全部接入消息发送
- 可公开状态发送 `UPSERT`
- 不可公开状态发送 `DELETE`
- 测试验证 publisher 收到正确 `contentType/contentId/currentVersionNo/eventType`

### T10a. 在 MingCustoms 写路径接入事务后消息发送

目标：

- 在 MingCustoms 主写路径接入事务后消息发送。

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/service/impl/MingCustomsApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/mingcustoms/MingCustomsApplicationServiceImplTest.java`

完成标准：

- MingCustoms 主内容编辑路径接入同步
- 可公开状态发送 `UPSERT`
- 不可公开状态发送 `DELETE`
- 所有消息都必须带 `currentVersionNo`
- 所有会影响搜索面的字段变更都必须推进 `currentVersionNo`

### T10b. 在内容治理写路径接入事务后消息发送

目标：

- 覆盖统一内容治理写路径，避免只有页面主编辑入口能触发同步。

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceImplTest.java`

完成标准：

- ClassicsContentApplicationServiceImpl 内会改变摘要、标签、问答、AI 应用结果的路径接入同步
- 所有消息都必须带 `currentVersionNo`
- 所有会影响搜索面的字段变更都必须推进 `currentVersionNo`

### T11. 更新设计与覆盖文档

目标：

- 把消息队列增量同步和 `currentVersionNo` 幂等规则写入正式文档。

文件：

- `docs/30-designs/DISCOVERY-DESIGN.md`
- `docs/40-readiness/DISCOVERY-IMPLEMENTATION-COVERAGE.md`

完成标准：

- Discovery design 明确：
  - RocketMQ 增量同步
  - `afterCommit` 发送
  - `currentVersionNo` 幂等
  - `rebuild` 兜底
- coverage 文档明确当前完成度和剩余风险

## 6. Validation Order

实现完成后按以下顺序验证，不要跳步：

1. `mvn -pl biz/classics/kuzhambu-classics-application spotless:apply`
2. `mvn -pl biz/discovery/kuzhambu-discovery-application,biz/discovery/kuzhambu-discovery-infra,biz/discovery/kuzhambu-discovery-interface spotless:apply`
3. `mvn spotless:check`
4. `mvn checkstyle:check`
5. 先跑新增单测：
   - `ClassicsSearchIndexSyncPublishSupportTest`
   - `SearchIndexSyncApplicationServiceImplTest`
   - `ElasticsearchSearchIndexGatewayTest`
   - `RocketMqDiscoverySearchIndexSyncConsumerTest`
   - `SearchIndexCleanupApplicationServiceImplTest`
6. 再跑被改写路径的 application 测试：
   - `SancaiApplicationServiceImplTest`
   - `WangqiDocumentApplicationServiceImplTest`
   - `MingCustomsApplicationServiceImplTest`
   - `ClassicsContentApplicationServiceImplTest`
7. 最后跑最小 architecture / controller 验证：
   - `AdminStarterArchitectureTest`
   - `DiscoverySearchAdminControllerTest`

## 7. Done Criteria

仅当以下条件同时满足，本 RUNBOOK 才算完成：

- 三类内容写路径都能在事务提交后发送 MQ 消息
- Discovery consumer 能消费消息并完成幂等 `upsert / delete`
- ES 文档包含 `sourceVersionNo`、`deleted`、`deletedAt`
- 管理端 `index/rebuild` 仍可用
- 旧版本消息不会覆盖新版本索引
- 所有影响搜索面的业务变更都会推进 `currentVersionNo`
- 删除态文档可被计划任务按保留期物理清理
- 文档已同步
- 本 RUNBOOK 在任务完成后删除
