# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `classics-facade-summary-contract`：定义 summary facade 协议
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/ClassicsFacade.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/request/ClassicsSummaryFacadeRequest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/response/ClassicsSummaryFacadeResponse.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsTopContentFacadeDto.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsContentGrowthPointFacadeDto.java`
    - 处理动作：定义 `summary` request/response/dto 并补到 `ClassicsFacade`
    - 验收点：`summary` 协议字段与 RUNBOOK 一致，且协议类包位正确
    - 重要度：8/10

- [ ] `classics-facade-summary-provider`：桥接 summary facade provider
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/pom.xml`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/facade/impl/ClassicsFacadeImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/facade/assembler/ClassicsFacadeAssembler.java`
    - 处理动作：在 provider 侧实现 `summary` facade 桥接与 result 映射
    - 验收点：`ClassicsFacadeImpl.summary(...)` 已可通过 `ClassicsReportApplicationService` 返回 `ClassicsSummaryFacadeResponse`
    - 重要度：8/10

- [ ] `classics-facade-public-content-contract`：定义 public search content facade 协议
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/ClassicsFacade.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/request/ClassicsPublicContentFacadeRequest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/response/ClassicsPublicContentsFacadeResponse.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/response/ClassicsPublicContentFacadeResponse.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsPublicContentFacadeDto.java`
    - 处理动作：定义 public content request/response/dto 并补到 `ClassicsFacade`
    - 验收点：`listPublicContents/getPublicContent` 协议字段与 RUNBOOK 一致，且协议类包位正确
    - 重要度：8/10

- [ ] `classics-facade-public-content-provider`：桥接 public search content facade provider
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/facade/impl/ClassicsFacadeImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/facade/assembler/ClassicsFacadeAssembler.java`
    - 处理动作：在 provider 侧实现 `listPublicContents/getPublicContent` facade 桥接与内容映射
    - 验收点：`ClassicsFacadeImpl` 已可返回 `ClassicsPublicContentFacadeDto`
    - 重要度：8/10

- [ ] `classics-facade-search-sync-contract`：定义 search index sync facade 协议
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsSearchIndexSyncMessageFacadeDto.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsSearchIndexSyncEventFacadeDto.java`
    - 处理动作：定义检索同步消息 facade dto 和事件枚举
    - 验收点：同步消息字段与事件枚举值与 RUNBOOK 一致
    - 重要度：7/10

- [ ] `operations-classics-facade`：operations 改用 ClassicsFacade 读取 summary
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGateway.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGatewayTest.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/pom.xml`
    - 处理动作：将 `operations` 的 classics summary 读取改为通过 `ClassicsFacade`
    - 验收点：`operations-application` 不再依赖 `kuzhambu-classics-application` 的 report service
    - 重要度：8/10

- [ ] `discovery-search-classics-facade`：discovery search provider 改用 ClassicsFacade 读取公开内容
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/ClassicsSearchContentProvider.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImplTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/pom.xml`
    - 处理动作：将 `discovery` 搜索内容提供方改为通过 `ClassicsFacade`
    - 验收点：`ClassicsSearchContentProvider` 已消费 `ClassicsPublicContentFacadeDto`
    - 重要度：8/10

- [ ] `discovery-qa-classics-facade`：discovery QA provider 接口切换
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImpl.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImplTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImplAdminReadTest.java`
    - 处理动作：将 `QaApplicationServiceImpl` 的 classics 公开内容读取改为通过 `ClassicsFacade`
    - 验收点：`QaApplicationServiceImpl` 不再依赖 `ClassicsSearchContentApplicationService`
    - 重要度：8/10

- [ ] `discovery-qa-classics-facade-model`：discovery QA assembler 模型切换
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaContextAssembler.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaSourceAssembler.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImplTest.java`
    - 处理动作：将 QA assembler 输入模型从 `ClassicsSearchSourceContent` 改为 `ClassicsPublicContentFacadeDto`
    - 验收点：QA 组装链不再依赖 `classics-application` result 类型
    - 重要度：7/10

- [ ] `discovery-interface-classics-facade`：discovery-interface 改用 classics-facade 检索同步消息协议
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/mq/RocketMqDiscoverySearchIndexSyncConsumer.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/mq/RocketMqDiscoverySearchIndexSyncConsumerTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/pom.xml`
    - 处理动作：将 `discovery-interface` 检索同步消息改为依赖 `classics-facade` dto
    - 验收点：`discovery-interface` 不再依赖 `classics.application.searchsync.model`
    - 重要度：8/10

- [ ] `classics-search-sync-facade-dto`：classics provider 改用 classics-facade 检索同步消息协议
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/searchsync/service/ClassicsSearchIndexSyncPublisher.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/searchsync/support/ClassicsSearchIndexSyncPublishSupport.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/searchsync/mq/RocketMqClassicsSearchIndexSyncPublisher.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/searchsync/ClassicsSearchIndexSyncPublishSupportTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/pom.xml`
    - 处理动作：将 classics provider 发布链的检索同步消息改为使用 facade dto
    - 验收点：provider 发布链与 MQ publisher 已统一使用 `classics-facade` 同步消息协议
    - 重要度：7/10

- [ ] `classics-facade-test`：补齐 classics facade 架构与 provider 测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/test/java/com/thundax/kuzhambu/classics/facade/ClassicsFacadeArchitectureTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/facade/impl/ClassicsFacadeImplTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/report/service/impl/ClassicsReportApplicationServiceImplTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/search/ClassicsSearchContentApplicationServiceImplTest.java`
    - 处理动作：补齐 classics facade 模块架构测试和 provider 映射测试
    - 验收点：facade 协议和 provider 映射都有测试覆盖，现有 report/search 语义测试继续通过
    - 重要度：7/10

- [ ] `classics-facade-allowlist`：收缩 classics 跨域白名单
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/common/kuzhambu-common-test/src/main/java/com/thundax/kuzhambu/common/test/architecture/RepositoryArchitectureRuleSupport.java`、`kuzhambu-servers/common/kuzhambu-common-test/src/main/java/com/thundax/kuzhambu/common/test/architecture/CrossApplicationIsolationArchitectureRuleSupport.java`、`docs/00-governance/SERVERS-ARCHITECTURE-RULES.md`
    - 处理动作：删除 `operations/discovery -> classics-application` 的 legacy allowlist 并同步文档
    - 验收点：相关模块在新规则下仍可通过架构测试，治理文档已同步到当前口径
    - 重要度：7/10

## 待审阅任务项

## 待讨论项
