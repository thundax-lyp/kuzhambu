# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `classics-application`：T2 创建事务后发送支持类
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-INDEX-SYNC.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/searchsync/support/ClassicsSearchIndexSyncPublishSupport.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/searchsync/ClassicsSearchIndexSyncPublishSupportTest.java`
    - 处理动作：封装 `afterCommit` 发送逻辑并提供 `publishUpsertAfterCommit(...)` 和 `publishDeleteAfterCommit(...)`。
    - 验收点：仅在事务提交后调用 publisher，且无事务上下文时抛清晰异常。
    - 重要度：9/10

- [ ] `classics-interface+starter`：T3 接入 RocketMQ producer 实现与配置
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-INDEX-SYNC.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/searchsync/mq/RocketMqClassicsSearchIndexSyncPublisher.java`、`kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/resources/application.yml`、`.env.example`、`deploy/.env.example`
    - 处理动作：用现有 `KuzhambuMqSender` 实现 producer，并补齐 topic、tag、retention、cleanup cron 的配置和环境变量。
    - 验收点：producer 使用固定 topic、`UPSERT / DELETE` tag、`contentType:contentId:currentVersionNo` key，且配置键与环境变量名与 RUNBOOK 一致。
    - 重要度：8/10

- [ ] `classics-search+discovery-search`：T4 为公开内容查询补充单条读取能力
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-INDEX-SYNC.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/search/service/ClassicsSearchContentApplicationService.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/search/service/impl/ClassicsSearchContentApplicationServiceImpl.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/SearchContentProvider.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/ClassicsSearchContentProvider.java`
    - 处理动作：增加“按 `contentType + contentId` 读取当前公开内容”的 application 能力并暴露给 Discovery。
    - 验收点：Discovery application 可通过 provider 读取三类内容的当前公开单条结果，且不直接依赖 Classics mapper/DO。
    - 重要度：9/10

- [ ] `classics-search+discovery-search`：T5 传播 `currentVersionNo` 到搜索内容结果对象
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-INDEX-SYNC.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/search/result/ClassicsSearchSourceContent.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/search/service/impl/ClassicsSearchContentApplicationServiceImpl.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/result/SearchSourceContent.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/ClassicsSearchContentProvider.java`
    - 处理动作：把 `currentVersionNo` 从 Classics 搜索结果一路传到 Discovery 搜索结果。
    - 验收点：三类内容构造搜索结果时都写入 `currentVersionNo`，且 Discovery 侧 `SearchSourceContent` 保留该字段。
    - 重要度：8/10

- [ ] `discovery-application`：T6 扩展 Discovery 索引同步应用服务契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-INDEX-SYNC.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/SearchIndexSyncApplicationService.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchIndexSyncApplicationServiceImpl.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/SearchIndexGateway.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchIndexSyncApplicationServiceImplTest.java`
    - 处理动作：新增 `syncUpsert(...)` 和 `syncDelete(...)`，并在 application 层固定版本判定与删除态更新入口。
    - 验收点：`syncUpsert(...)` 会回查当前公开内容，`syncDelete(...)` 只驱动删除态更新，测试覆盖旧版本丢弃、同版本幂等和新版本更新。
    - 重要度：10/10

- [ ] `discovery-infra`：T7 扩展 ES 文档字段与 Gateway 幂等更新能力
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-INDEX-SYNC.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/client/DiscoverySearchDocument.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/client/DiscoverySearchDocumentAssembler.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/client/ElasticsearchSearchIndexGateway.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/client/ElasticsearchSearchIndexGatewayTest.java`
    - 处理动作：给 ES 文档增加 `sourceVersionNo / deleted / deletedAt` 并实现按版本 `upsert`、删除态更新和超时墓碑清理。
    - 验收点：assembler 正确映射 `currentVersionNo`，gateway 支持按 `documentId` 写删除态并按 `deletedAt` 阈值清理超时墓碑文档。
    - 重要度：10/10

- [ ] `discovery-interface+starter`：T8 接入 Discovery MQ consumer 与开关配置
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-INDEX-SYNC.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/mq/RocketMqDiscoverySearchIndexSyncConsumer.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/mq/RocketMqDiscoverySearchIndexSyncConsumerTest.java`、`kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/resources/application.yml`、`kuzhambu-servers/starter/kuzhambu-portal-starter/src/main/resources/application.yml`
    - 处理动作：在 admin 侧接入索引同步消息消费，并用 starter 配置明确 admin 开启、portal 关闭。
    - 验收点：consumer 只消费 Discovery 索引同步 topic 并调用 `SearchIndexSyncApplicationService`，portal starter 不会误启动 consumer。
    - 重要度：9/10

- [ ] `discovery-cleanup`：T8b 接入删除态文档计划清理任务
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-INDEX-SYNC.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/SearchIndexCleanupApplicationService.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchIndexCleanupApplicationServiceImpl.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/task/DiscoverySearchDeletedDocumentCleanupTask.java`、`kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/resources/application.yml`
    - 处理动作：新增按保留期清理删除态索引文档的 application service 和定时 task。
    - 验收点：task 按 cron 触发 cleanup，只清理 `deleted = true` 且 `deletedAt` 超过保留期的文档，并且 task 不直接操作 ES client。
    - 重要度：8/10

- [ ] `classics-sancai`：T9a 在 Sancai 写路径接入事务后消息发送
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-INDEX-SYNC.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/SancaiApplicationServiceImplTest.java`
    - 处理动作：在 Sancai 会影响公开检索面的主写路径接入事务后消息发送。
    - 验收点：创建、更新、发布、转私有等路径会按公开性发送 `UPSERT / DELETE`，测试校验 `contentType / contentId / currentVersionNo / eventType`。
    - 重要度：8/10

- [ ] `classics-wangqi`：T9b 在 Wangqi 写路径接入事务后消息发送
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-INDEX-SYNC.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/service/impl/WangqiDocumentApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/wangqi/WangqiDocumentApplicationServiceImplTest.java`
    - 处理动作：在 Wangqi 会影响公开检索面的主写路径接入事务后消息发送。
    - 验收点：创建、更新、发布、转私有等路径会按公开性发送 `UPSERT / DELETE`，测试校验 `contentType / contentId / currentVersionNo / eventType`。
    - 重要度：8/10

- [ ] `classics-mingcustoms`：T10a 在 MingCustoms 写路径接入事务后消息发送
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-INDEX-SYNC.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/service/impl/MingCustomsApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/mingcustoms/MingCustomsApplicationServiceImplTest.java`
    - 处理动作：在 MingCustoms 主内容编辑路径接入事务后消息发送。
    - 验收点：公开状态发送 `UPSERT`、不可公开状态发送 `DELETE`，且所有消息都带 `currentVersionNo`。
    - 重要度：8/10

- [ ] `classics-content-governance`：T10b 在内容治理写路径接入事务后消息发送
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-INDEX-SYNC.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceImplTest.java`
    - 处理动作：给摘要、标签、问答、AI 应用结果等治理写路径接入事务后消息发送。
    - 验收点：所有影响搜索面的治理变更都会推进 `currentVersionNo` 并发送索引同步消息。
    - 重要度：9/10

- [ ] `discovery-docs`：T11 更新设计与覆盖文档
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-INDEX-SYNC.md`
    - 范围对象：`docs/30-designs/DISCOVERY-DESIGN.md`、`docs/40-readiness/DISCOVERY-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：同步 RocketMQ 增量同步、`afterCommit` 发送、`currentVersionNo` 幂等、删除态计划清理和 `rebuild` 兜底口径。
    - 验收点：设计文档和覆盖文档都能准确反映本轮能力边界、完成度和剩余风险。
    - 重要度：7/10

## 待讨论项
