# RUNBOOK: Classics Facade Isolation

## 1. 目标

本次试点目标只有一个：

- 新增 `kuzhambu-classics-facade`
- 用统一 `ClassicsFacade` 承接外域对 `classics` 的跨域调用
- 剥离外域对 `kuzhambu-classics-application` 的直接依赖

本次不做的事情：

- 不重写 `classics` 域内部 application / domain 的职责边界
- 不改动 `starter -> classics-application` 的启动装配依赖
- 不提前暴露当前没有外域消费的 `classics` 能力

## 2. RUNBOOK 书写约束

本 RUNBOOK 后续执行必须遵守以下约束：

- 表述必须清晰、准确，不允许“视情况调整”“后续再看”这类模糊语句
- 一个执行任务必须控制在 `2-5` 个文件；超过则继续拆分成多个小任务
- 每个执行任务都必须写明：
  - 数据结构变更
  - 接口定义
  - 相关文件
- 数据结构变更必须明确到字段，不允许只写“新增 dto/request/response”
- 相关文件必须写明确路径，不允许写 `*.java`、`...`、`相关测试`
- 改名的文件不允许只改类名；必须同时落到正确包位：
  - `facade/request`
  - `facade/response`
  - `facade/dto`

## 3. 当前直接依赖清单

### 3.1 operations -> classics-application

当前消费点：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGateway.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGatewayTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/pom.xml`

当前直接依赖：

- `ClassicsReportApplicationService.summary(Date periodStart, Date periodEnd, String bucketType)`
- 返回类型：`ClassicsReportSummaryResult`

### 3.2 discovery-application -> classics-application

当前消费点：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/ClassicsSearchContentProvider.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaContextAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaSourceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImplAdminReadTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/pom.xml`

当前直接依赖：

- `ClassicsSearchContentApplicationService.listPublicContents()`
- `ClassicsSearchContentApplicationService.getPublicContent(String contentType, String contentId)`
- 返回类型：`ClassicsSearchSourceContent`

### 3.3 discovery-interface -> classics-application

当前消费点：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/mq/RocketMqDiscoverySearchIndexSyncConsumer.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/mq/RocketMqDiscoverySearchIndexSyncConsumerTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/pom.xml`

当前直接依赖：

- `ClassicsSearchIndexSyncMessage`
- `ClassicsSearchIndexSyncEventType`

### 3.4 classics-domain 外域直依赖现状

当前外域没有直接依赖 `com.thundax.kuzhambu.classics.domain.*`。

本次仍然要收口：

- `operations -> kuzhambu-classics-application`
- `discovery-application -> kuzhambu-classics-application`
- `discovery-interface -> kuzhambu-classics-application`

## 4. 目标边界

### 4.1 模块目标

新增模块：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade`

目标依赖方向：

- 外域 `*-application` / `*-interface` 只能依赖 `kuzhambu-classics-facade`
- `kuzhambu-classics-application` 可以依赖 `kuzhambu-classics-facade`
- 外域不再依赖：
  - `kuzhambu-classics-application`

### 4.2 门面目标

统一外部入口：

- `com.thundax.kuzhambu.classics.facade.ClassicsFacade`

本次只暴露现有消费点所需能力：

- `summary(...)`
- `listPublicContents()`
- `getPublicContent(...)`

本次同步迁移的共享协议：

- `ClassicsSearchIndexSyncMessageFacadeDto`
- `ClassicsSearchIndexSyncEventFacadeDto`

本次不暴露：

- `classics` 域当前没有外域消费的其他 content / sharing / asset 能力

## 5. 新协议与字段定义

### 5.1 report summary 协议

新增 request：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/request/ClassicsSummaryFacadeRequest.java`

字段：

- `Date periodStart`
- `Date periodEnd`
- `String bucketType`

新增 response：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/response/ClassicsSummaryFacadeResponse.java`

字段：

- `Date periodStart`
- `Date periodEnd`
- `Long contentCount`
- `Long translatedContentCount`
- `Long imageReadyContentCount`
- `Long visualAssetReadyContentCount`
- `Long shareVisitCount`
- `List<ClassicsTopContentFacadeDto> topContents`
- `List<ClassicsContentGrowthPointFacadeDto> contentGrowthSeries`

新增 dto：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsTopContentFacadeDto.java`
  - `Long contentId`
  - `String contentType`
  - `String title`
  - `Long visitCount`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsContentGrowthPointFacadeDto.java`
  - `String bucket`
  - `Long createdCount`

映射来源：

- `ClassicsReportSummaryResult`
- `ClassicsReportSummaryResult.TopContentResult`
- `ClassicsReportSummaryResult.ContentGrowthPointResult`

### 5.2 public search content 协议

新增 request：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/request/ClassicsPublicContentFacadeRequest.java`

字段：

- `String contentType`
- `String contentId`

新增 response：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/response/ClassicsPublicContentsFacadeResponse.java`
  - `List<ClassicsPublicContentFacadeDto> contents`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/response/ClassicsPublicContentFacadeResponse.java`
  - `ClassicsPublicContentFacadeDto content`

新增 dto：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsPublicContentFacadeDto.java`
  - `String contentType`
  - `String contentId`
  - `String knowledgeBase`
  - `String categoryCode`
  - `String categoryName`
  - `String title`
  - `String summary`
  - `List<String> textSegments`
  - `List<String> tagNames`
  - `String status`
  - `String visibility`
  - `Integer currentVersionNo`
  - `Date publishedAt`
  - `Date updatedAt`

映射来源：

- `ClassicsSearchSourceContent`

### 5.3 search index sync 协议

新增 dto：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsSearchIndexSyncMessageFacadeDto.java`
  - `String eventId`
  - `ClassicsSearchIndexSyncEventFacadeDto eventType`
  - `String contentType`
  - `String contentId`
  - `Integer currentVersionNo`
  - `Date occurredAt`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsSearchIndexSyncEventFacadeDto.java`
  - `UPSERT`
  - `DELETE`

映射来源：

- `ClassicsSearchIndexSyncMessage`
- `ClassicsSearchIndexSyncEventType`

设计约束：

- `discovery-interface` 只依赖 facade 层消息协议，不再依赖 `classics-application` 的 `searchsync.model`
- `classics` provider 内部仍可保留原有 publish support 和 publisher 结构，不在本次重写 RocketMQ 装配方式

## 6. provider 落点

`ClassicsFacade` 实现与 assembler 放在 provider `application`：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/facade/impl/ClassicsFacadeImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/facade/assembler/ClassicsFacadeAssembler.java`

可继续复用的 provider 现有能力：

- `ClassicsReportApplicationService`
- `ClassicsSearchContentApplicationService`

search index sync 协议迁移落点：

- provider 侧发布辅助和 MQ publisher 改为依赖 `classics-facade` dto
- discovery consumer 改为消费 `classics-facade` dto

## 7. 执行任务

### T1. 建 classics-facade 模块骨架

- 数据结构变更：
  - 新增空协议入口 `ClassicsFacade`
- 接口定义：
  - 先只声明空 `ClassicsFacade`，后续任务补方法
- 相关文件：
  - `kuzhambu-servers/biz/classics/pom.xml`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/pom.xml`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/ClassicsFacade.java`

### T2. 定义 summary facade 协议

- 数据结构变更：
  - 新增 `ClassicsSummaryFacadeRequest.periodStart/periodEnd/bucketType`
  - 新增 `ClassicsSummaryFacadeResponse.periodStart/periodEnd/contentCount/translatedContentCount/imageReadyContentCount/visualAssetReadyContentCount/shareVisitCount/topContents/contentGrowthSeries`
  - 新增 `ClassicsTopContentFacadeDto.contentId/contentType/title/visitCount`
  - 新增 `ClassicsContentGrowthPointFacadeDto.bucket/createdCount`
- 接口定义：
  - `ClassicsFacade.summary(ClassicsSummaryFacadeRequest request)`
- 相关文件：
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/ClassicsFacade.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/request/ClassicsSummaryFacadeRequest.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/response/ClassicsSummaryFacadeResponse.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsTopContentFacadeDto.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsContentGrowthPointFacadeDto.java`

### T3. 桥接 summary facade provider

- 数据结构变更：
  - 在 provider assembler 中增加 `ClassicsReportSummaryResult -> ClassicsSummaryFacadeResponse` 映射
- 接口定义：
  - `ClassicsFacadeImpl.summary(...)`
- 相关文件：
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/pom.xml`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/facade/impl/ClassicsFacadeImpl.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/facade/assembler/ClassicsFacadeAssembler.java`

### T4. 定义 public search content facade 协议

- 数据结构变更：
  - 新增 `ClassicsPublicContentFacadeRequest.contentType/contentId`
  - 新增 `ClassicsPublicContentsFacadeResponse.contents`
  - 新增 `ClassicsPublicContentFacadeResponse.content`
  - 新增 `ClassicsPublicContentFacadeDto.contentType/contentId/knowledgeBase/categoryCode/categoryName/title/summary/textSegments/tagNames/status/visibility/currentVersionNo/publishedAt/updatedAt`
- 接口定义：
  - `ClassicsFacade.listPublicContents()`
  - `ClassicsFacade.getPublicContent(ClassicsPublicContentFacadeRequest request)`
- 相关文件：
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/ClassicsFacade.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/request/ClassicsPublicContentFacadeRequest.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/response/ClassicsPublicContentsFacadeResponse.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/response/ClassicsPublicContentFacadeResponse.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsPublicContentFacadeDto.java`

### T5. 桥接 public search content facade provider

- 数据结构变更：
  - 在 provider assembler 中增加 `ClassicsSearchSourceContent -> ClassicsPublicContentFacadeDto` 映射
- 接口定义：
  - `ClassicsFacadeImpl.listPublicContents()`
  - `ClassicsFacadeImpl.getPublicContent(...)`
- 相关文件：
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/facade/impl/ClassicsFacadeImpl.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/facade/assembler/ClassicsFacadeAssembler.java`

### T6. 定义 search index sync facade 协议

- 数据结构变更：
  - 新增 `ClassicsSearchIndexSyncMessageFacadeDto.eventId/eventType/contentType/contentId/currentVersionNo/occurredAt`
  - 新增 `ClassicsSearchIndexSyncEventFacadeDto.UPSERT/DELETE`
- 接口定义：
  - 本任务不新增 facade 方法，只新增 facade 协议 dto
- 相关文件：
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsSearchIndexSyncMessageFacadeDto.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsSearchIndexSyncEventFacadeDto.java`

### T7. operations 改用 ClassicsFacade 读取 summary

- 数据结构变更：
  - `operations` 改为消费 `ClassicsSummaryFacadeRequest` / `ClassicsSummaryFacadeResponse`
- 接口定义：
  - `DefaultOperationsReportMetricsGateway` 从 `ClassicsReportApplicationService.summary(...)` 改为 `ClassicsFacade.summary(...)`
- 相关文件：
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGateway.java`
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGatewayTest.java`
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-application/pom.xml`

### T8. discovery search provider 改用 ClassicsFacade 读取公开检索内容

- 数据结构变更：
  - `discovery` 改为消费 `ClassicsPublicContentFacadeDto`
- 接口定义：
  - `ClassicsSearchContentProvider` 从 `ClassicsSearchContentApplicationService.listPublicContents/getPublicContent` 改为 `ClassicsFacade.listPublicContents/getPublicContent`
- 相关文件：
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/ClassicsSearchContentProvider.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImplTest.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/pom.xml`

### T9. discovery QA provider 接口切换

- 数据结构变更：
  - `QaApplicationServiceImpl` 改为消费 `ClassicsPublicContentFacadeDto`
- 接口定义：
  - `QaApplicationServiceImpl` 从 `ClassicsSearchContentApplicationService.listPublicContents()` 改为 `ClassicsFacade.listPublicContents()`
- 相关文件：
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImpl.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImplTest.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImplAdminReadTest.java`

### T10. discovery QA assembler 模型切换

- 数据结构变更：
  - `QaContextAssembler`、`QaSourceAssembler` 改为消费 `ClassicsPublicContentFacadeDto`
- 接口定义：
  - `QaContextAssembler` 和 `QaSourceAssembler` 的输入模型从 `ClassicsSearchSourceContent` 改为 `ClassicsPublicContentFacadeDto`
- 相关文件：
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaContextAssembler.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaSourceAssembler.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImplTest.java`

### T11. discovery-interface 改用 classics-facade 检索同步消息协议

- 数据结构变更：
  - `discovery-interface` 改为消费 `ClassicsSearchIndexSyncMessageFacadeDto` 和 `ClassicsSearchIndexSyncEventFacadeDto`
- 接口定义：
  - `RocketMqDiscoverySearchIndexSyncConsumer` 的泛型入参从 application model 改为 facade dto
- 相关文件：
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/mq/RocketMqDiscoverySearchIndexSyncConsumer.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/mq/RocketMqDiscoverySearchIndexSyncConsumerTest.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/pom.xml`

### T12. classics provider 改用 classics-facade 检索同步消息协议

- 数据结构变更：
  - provider 发布链改为使用 `ClassicsSearchIndexSyncMessageFacadeDto` 和 `ClassicsSearchIndexSyncEventFacadeDto`
- 接口定义：
  - `ClassicsSearchIndexSyncPublisher.publish(...)` 的参数类型改为 facade dto
- 相关文件：
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/searchsync/service/ClassicsSearchIndexSyncPublisher.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/searchsync/support/ClassicsSearchIndexSyncPublishSupport.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/searchsync/mq/RocketMqClassicsSearchIndexSyncPublisher.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/searchsync/ClassicsSearchIndexSyncPublishSupportTest.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/pom.xml`

### T13. 补齐 classics facade 架构与 provider 测试

- 数据结构变更：
  - 新增 facade 架构测试
  - 新增 `ClassicsFacadeImpl` provider 映射测试
- 接口定义：
  - 覆盖 `summary/listPublicContents/getPublicContent` 的 facade provider 行为
- 相关文件：
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/test/java/com/thundax/kuzhambu/classics/facade/ClassicsFacadeArchitectureTest.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/facade/impl/ClassicsFacadeImplTest.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/report/service/impl/ClassicsReportApplicationServiceImplTest.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/search/ClassicsSearchContentApplicationServiceImplTest.java`

### T14. 收缩 classics 跨域白名单并同步规则文档

- 数据结构变更：
  - 无新增协议；只收缩 legacy allowlist
- 接口定义：
  - 无新增 facade 接口；收缩 `operations/discovery -> classics-application` 的门禁豁免
- 相关文件：
  - `kuzhambu-servers/common/kuzhambu-common-test/src/main/java/com/thundax/kuzhambu/common/test/architecture/RepositoryArchitectureRuleSupport.java`
  - `kuzhambu-servers/common/kuzhambu-common-test/src/main/java/com/thundax/kuzhambu/common/test/architecture/CrossApplicationIsolationArchitectureRuleSupport.java`
  - `docs/00-governance/SERVERS-ARCHITECTURE-RULES.md`
