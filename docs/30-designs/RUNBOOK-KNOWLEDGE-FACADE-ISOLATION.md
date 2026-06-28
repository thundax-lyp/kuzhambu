# RUNBOOK: Knowledge Facade Isolation

## 1. 目标

本次试点目标只有一个：

- 新增 `kuzhambu-knowledge-facade`
- 用统一 `KnowledgeFacade` 承接外域对 `knowledge` 的跨域调用
- 剥离外域对 `kuzhambu-knowledge-application` 和 `kuzhambu-knowledge-domain` 的直接依赖

本次不做的事情：

- 不重写 `knowledge` 域内部 application / domain 的职责边界
- 不提前暴露当前没有外域消费的 `knowledge` 能力
- 不修改 `knowledge-interface -> knowledge-application` 的域内依赖关系

## 2. RUNBOOK 书写约束

本 RUNBOOK 后续执行必须遵守以下约束：

- 表述必须简洁、明确，不允许“视情况调整”“后续再看”这类模糊语句
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

### 3.1 operations -> knowledge-application

当前消费点：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGateway.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGatewayTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/pom.xml`

当前直接依赖：

- `KnowledgeReportApplicationService.summary(Date periodStart, Date periodEnd, String bucketType)`
- 返回类型：`KnowledgeReportSummaryResult`

### 3.2 discovery -> knowledge-application

当前消费点：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/DiscoveryKnowledgeEnhancementProvider.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/QueryUnderstandingApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/pom.xml`

当前直接依赖：

- `KnowledgeTaxonomyReadApplicationService.expandSynonyms(String term)`
- `KnowledgeTaxonomyReadApplicationService.getTagHint(String term)`
- `KnowledgeTaxonomyReadApplicationService.listEntityHints(String term)`
- 返回类型：
  - `DiscoverySynonymExpandResult`
  - `DiscoveryTagHintResult`
  - `List<DiscoveryEntityHintResult>`

### 3.3 classics -> knowledge-domain

当前消费点：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsTagBindingSupport.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/pom.xml`

当前直接依赖：

- `KnowledgeTagBindingDomainService.resolveOrCreateManualTag(String name)`
- `KnowledgeTagBindingDomainService.resolveOrCreateAiTag(String name)`
- `KnowledgeTagBindingDomainService.syncContentTagRef(TagId tagId, ContentType contentType, Long contentId, String contentTitle, TagSource source)`
- `KnowledgeTagBindingDomainService.removeContentTagRef(TagId tagId, ContentType contentType, Long contentId)`
- 直接使用 `knowledge-domain` 类型：
  - `Tag`
  - `TagId`
  - `ContentType`
  - `TagSource`

## 4. 目标边界

### 4.1 模块目标

新增模块：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade`

目标依赖方向：

- 外域 `*-application` 只能依赖 `kuzhambu-knowledge-facade`
- `kuzhambu-knowledge-application` 可以依赖 `kuzhambu-knowledge-facade`
- 外域不再依赖：
  - `kuzhambu-knowledge-application`
  - `kuzhambu-knowledge-domain`

### 4.2 门面目标

统一外部入口：

- `com.thundax.kuzhambu.knowledge.facade.KnowledgeFacade`

本次只暴露现有消费点所需能力：

- `summary(...)`
- `expandSynonyms(...)`
- `getTagHint(...)`
- `listEntityHints(...)`
- `resolveOrCreateManualTag(...)`
- `resolveOrCreateAiTag(...)`
- `syncContentTagRef(...)`
- `removeContentTagRef(...)`

本次不暴露：

- `knowledge` 域当前没有外域消费的其他 taxonomy / report / review 能力

## 5. 新协议与字段定义

### 5.1 报表 summary 协议

新增 request：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/request/KnowledgeSummaryFacadeRequest.java`

字段：

- `Date periodStart`
- `Date periodEnd`
- `String bucketType`

新增 response：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/response/KnowledgeSummaryFacadeResponse.java`

字段：

- `Date periodStart`
- `Date periodEnd`
- `BigDecimal tagCoverageRate`
- `List<KnowledgeTopTagFacadeDto> topTags`
- `List<KnowledgeCategoryDistributionFacadeDto> categoryDistributions`
- `List<KnowledgeMonthlyNewTagFacadeDto> monthlyNewTags`

新增 dto：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/dto/KnowledgeTopTagFacadeDto.java`
  - `String tagName`
  - `Long contentRefCount`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/dto/KnowledgeCategoryDistributionFacadeDto.java`
  - `String categoryName`
  - `Long tagCount`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/dto/KnowledgeMonthlyNewTagFacadeDto.java`
  - `String bucket`
  - `Long tagCount`

映射来源：

- `KnowledgeReportSummaryResult`
- `KnowledgeReportSummaryResult.TopTagResult`
- `KnowledgeReportSummaryResult.CategoryDistributionResult`
- `KnowledgeReportSummaryResult.MonthlyNewTagResult`

### 5.2 discovery taxonomy 协议

新增 request：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/request/KnowledgeDiscoveryTermFacadeRequest.java`

字段：

- `String term`

新增 response：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/response/KnowledgeSynonymExpandFacadeResponse.java`
  - `String term`
  - `String normalizedTerm`
  - `List<String> expandedTerms`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/response/KnowledgeTagHintFacadeResponse.java`
  - `String term`
  - `String normalizedTerm`
  - `String matchedTagName`
  - `String matchedAliasName`
  - `Long contentRefCount`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/response/KnowledgeEntityHintsFacadeResponse.java`
  - `List<KnowledgeEntityHintFacadeDto> entityHints`

新增 dto：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/dto/KnowledgeEntityHintFacadeDto.java`
  - `String term`
  - `String normalizedTerm`
  - `String entityName`
  - `String entityType`
  - `Long contentRefCount`

映射来源：

- `DiscoverySynonymExpandResult`
- `DiscoveryTagHintResult`
- `DiscoveryEntityHintResult`

### 5.3 classics tag binding 协议

新增 request：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/request/KnowledgeResolveTagFacadeRequest.java`
  - `String tagName`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/request/KnowledgeContentTagRefFacadeRequest.java`
  - `Long tagId`
  - `String contentType`
  - `Long contentId`
  - `String contentTitle`
  - `String tagSource`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/request/KnowledgeRemoveContentTagRefFacadeRequest.java`
  - `Long tagId`
  - `String contentType`
  - `Long contentId`

新增 response：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/response/KnowledgeTagFacadeResponse.java`

字段：

- `Long tagId`
- `String tagName`

设计约束：

- `classics` 外域只需要 `tagId` 和展示用 `tagName`
- 不允许把 `Tag` 实体、`TagId` 值对象、`ContentType` 枚举、`TagSource` 枚举直接暴露到 facade 外
- `contentType` 兼容当前映射：
  - `SANCAI_ENTRY -> SANCAI_ENTRY`
  - `WANGQI_DOCUMENT -> WANGQI_DOCUMENT`
  - `MING_CUSTOMS -> MING_CUSTOM`
- `tagSource` 兼容当前映射：
  - `MANUAL -> MANUAL`
  - `AI -> AI_EXTRACTED`

## 6. provider 落点

`KnowledgeFacade` 实现与 assembler 放在 provider `application`：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/facade/impl/KnowledgeFacadeImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/facade/assembler/KnowledgeFacadeAssembler.java`

可继续复用的 provider 现有能力：

- `KnowledgeReportApplicationService`
- `KnowledgeTaxonomyReadApplicationService`
- `KnowledgeTagBindingDomainService`

## 7. 执行任务

### T1. 新增 facade 模块骨架

范围对象：

- `kuzhambu-servers/biz/knowledge/pom.xml`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/pom.xml`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/KnowledgeFacade.java`

处理动作：

- 新增 `kuzhambu-knowledge-facade` 模块
- 将模块挂到 `biz/knowledge` 聚合 `pom`
- 建立空的 `KnowledgeFacade` 接口

数据结构变更：

- 无

接口定义：

- `KnowledgeFacade`

验收点：

- `mvn -pl biz/knowledge/kuzhambu-knowledge-facade -am -DskipTests package` 可通过

### T2. 定义 summary facade 协议

范围对象：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/KnowledgeFacade.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/request/KnowledgeSummaryFacadeRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/response/KnowledgeSummaryFacadeResponse.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/dto/KnowledgeTopTagFacadeDto.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/dto/KnowledgeCategoryDistributionFacadeDto.java`

处理动作：

- 在 `KnowledgeFacade` 增加 `summary(...)`
- 新增 summary request / response / dto

数据结构变更：

- `KnowledgeSummaryFacadeRequest`
  - `Date periodStart`
  - `Date periodEnd`
  - `String bucketType`
- `KnowledgeSummaryFacadeResponse`
  - `Date periodStart`
  - `Date periodEnd`
  - `BigDecimal tagCoverageRate`
  - `List<KnowledgeTopTagFacadeDto> topTags`
  - `List<KnowledgeCategoryDistributionFacadeDto> categoryDistributions`
  - `List<KnowledgeMonthlyNewTagFacadeDto> monthlyNewTags`
- `KnowledgeTopTagFacadeDto`
  - `String tagName`
  - `Long contentRefCount`
- `KnowledgeCategoryDistributionFacadeDto`
  - `String categoryName`
  - `Long tagCount`

接口定义：

- `KnowledgeFacade.summary(KnowledgeSummaryFacadeRequest request)`

验收点：

- summary 协议字段和 `KnowledgeReportSummaryResult` 一一对应

### T3. 补齐 summary 剩余 dto 与 provider 映射

范围对象：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/dto/KnowledgeMonthlyNewTagFacadeDto.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/facade/impl/KnowledgeFacadeImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/facade/assembler/KnowledgeFacadeAssembler.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/pom.xml`

处理动作：

- provider `application` 依赖 `knowledge-facade`
- 实现 `summary(...)`
- 补齐 `monthlyNewTags` dto 映射

数据结构变更：

- `KnowledgeMonthlyNewTagFacadeDto`
  - `String bucket`
  - `Long tagCount`

接口定义：

- `KnowledgeFacadeImpl.summary(...)`
- `KnowledgeFacadeAssembler.toSummaryResponse(...)`

验收点：

- `KnowledgeFacadeImpl.summary(...)` 只返回 facade response

### T4. 定义 discovery taxonomy facade 协议

范围对象：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/KnowledgeFacade.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/request/KnowledgeDiscoveryTermFacadeRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/response/KnowledgeSynonymExpandFacadeResponse.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/response/KnowledgeTagHintFacadeResponse.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/dto/KnowledgeEntityHintFacadeDto.java`

处理动作：

- 在 `KnowledgeFacade` 增加：
  - `expandSynonyms(...)`
  - `getTagHint(...)`
  - `listEntityHints(...)`
- 定义 term request 与对应 response / dto

数据结构变更：

- `KnowledgeDiscoveryTermFacadeRequest`
  - `String term`
- `KnowledgeSynonymExpandFacadeResponse`
  - `String term`
  - `String normalizedTerm`
  - `List<String> expandedTerms`
- `KnowledgeTagHintFacadeResponse`
  - `String term`
  - `String normalizedTerm`
  - `String matchedTagName`
  - `String matchedAliasName`
  - `Long contentRefCount`
- `KnowledgeEntityHintFacadeDto`
  - `String term`
  - `String normalizedTerm`
  - `String entityName`
  - `String entityType`
  - `Long contentRefCount`

接口定义：

- `KnowledgeFacade.expandSynonyms(KnowledgeDiscoveryTermFacadeRequest request)`
- `KnowledgeFacade.getTagHint(KnowledgeDiscoveryTermFacadeRequest request)`
- `KnowledgeFacade.listEntityHints(KnowledgeDiscoveryTermFacadeRequest request)`

验收点：

- 不再使用 `Discovery*Result` 作为外部协议

### T5. 落 discovery taxonomy provider 映射

范围对象：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/response/KnowledgeEntityHintsFacadeResponse.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/facade/impl/KnowledgeFacadeImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/facade/assembler/KnowledgeFacadeAssembler.java`

处理动作：

- 实现 synonym / tag hint / entity hints 映射
- 为 entity hints 增加聚合 response

数据结构变更：

- `KnowledgeEntityHintsFacadeResponse`
  - `List<KnowledgeEntityHintFacadeDto> entityHints`

接口定义：

- `KnowledgeFacadeImpl.expandSynonyms(...)`
- `KnowledgeFacadeImpl.getTagHint(...)`
- `KnowledgeFacadeImpl.listEntityHints(...)`
- `KnowledgeFacadeAssembler.toEntityHintsResponse(...)`

验收点：

- provider 对 discovery 场景只暴露 facade response / dto

### T6. 定义 classics tag binding facade 协议

范围对象：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/KnowledgeFacade.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/request/KnowledgeResolveTagFacadeRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/request/KnowledgeContentTagRefFacadeRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/request/KnowledgeRemoveContentTagRefFacadeRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/response/KnowledgeTagFacadeResponse.java`

处理动作：

- 在 `KnowledgeFacade` 增加：
  - `resolveOrCreateManualTag(...)`
  - `resolveOrCreateAiTag(...)`
  - `syncContentTagRef(...)`
  - `removeContentTagRef(...)`
- 用 facade request/response 替换 `Tag`、`TagId`、`ContentType`、`TagSource`

数据结构变更：

- `KnowledgeResolveTagFacadeRequest`
  - `String tagName`
- `KnowledgeContentTagRefFacadeRequest`
  - `Long tagId`
  - `String contentType`
  - `Long contentId`
  - `String contentTitle`
  - `String tagSource`
- `KnowledgeRemoveContentTagRefFacadeRequest`
  - `Long tagId`
  - `String contentType`
  - `Long contentId`
- `KnowledgeTagFacadeResponse`
  - `Long tagId`
  - `String tagName`

接口定义：

- `KnowledgeFacade.resolveOrCreateManualTag(KnowledgeResolveTagFacadeRequest request)`
- `KnowledgeFacade.resolveOrCreateAiTag(KnowledgeResolveTagFacadeRequest request)`
- `KnowledgeFacade.syncContentTagRef(KnowledgeContentTagRefFacadeRequest request)`
- `KnowledgeFacade.removeContentTagRef(KnowledgeRemoveContentTagRefFacadeRequest request)`

验收点：

- classics 侧后续不再需要 `knowledge-domain` 类型

### T7. 落 classics tag binding provider 映射

范围对象：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/facade/impl/KnowledgeFacadeImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/facade/assembler/KnowledgeFacadeAssembler.java`

处理动作：

- 实现 tag resolve / sync / remove
- 在 assembler 内完成：
  - `Tag -> KnowledgeTagFacadeResponse`
  - `KnowledgeContentTagRefFacadeRequest.contentType -> ContentType`
  - `KnowledgeContentTagRefFacadeRequest.tagSource -> TagSource`
  - `KnowledgeRemoveContentTagRefFacadeRequest.contentType -> ContentType`

数据结构变更：

- 无新增字段
- 明确 request string 到 domain enum 的映射规则

接口定义：

- `KnowledgeFacadeImpl.resolveOrCreateManualTag(...)`
- `KnowledgeFacadeImpl.resolveOrCreateAiTag(...)`
- `KnowledgeFacadeImpl.syncContentTagRef(...)`
- `KnowledgeFacadeImpl.removeContentTagRef(...)`

验收点：

- provider 内部承担所有 enum / value object 映射

### T8. operations 切到 KnowledgeFacade

范围对象：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGateway.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGatewayTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/pom.xml`

处理动作：

- 去掉 `KnowledgeReportApplicationService`
- 改为注入 `KnowledgeFacade`
- POM 从 `kuzhambu-knowledge-application` 改为 `kuzhambu-knowledge-facade`

数据结构变更：

- `DefaultOperationsReportMetricsGateway` 改为消费 `KnowledgeSummaryFacadeResponse`

接口定义：

- `KnowledgeFacade.summary(...)`

验收点：

- `operations-application` 不再依赖 `knowledge-application`

### T9. discovery 切到 KnowledgeFacade

范围对象：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/DiscoveryKnowledgeEnhancementProvider.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/QueryUnderstandingApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/pom.xml`

处理动作：

- 去掉 `KnowledgeTaxonomyReadApplicationService`
- 改为注入 `KnowledgeFacade`
- POM 从 `kuzhambu-knowledge-application` 改为 `kuzhambu-knowledge-facade`

数据结构变更：

- `DiscoveryKnowledgeEnhancementProvider` 改为消费：
  - `KnowledgeSynonymExpandFacadeResponse`
  - `KnowledgeTagHintFacadeResponse`
  - `KnowledgeEntityHintsFacadeResponse`

接口定义：

- `KnowledgeFacade.expandSynonyms(...)`
- `KnowledgeFacade.getTagHint(...)`
- `KnowledgeFacade.listEntityHints(...)`

验收点：

- `discovery-application` 不再依赖 `knowledge-application`

### T10. classics 切到 KnowledgeFacade

范围对象：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsTagBindingSupport.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/pom.xml`

处理动作：

- 去掉 `KnowledgeTagBindingDomainService`
- 改为注入 `KnowledgeFacade`
- `bindTag(...)` 只拿 facade response 里的 `tagId/tagName`
- 在 `classics` consumer 内完成：
  - `ClassicsContentType -> KnowledgeContentTagRefFacadeRequest.contentType`
  - `ClassicsContentSource -> KnowledgeContentTagRefFacadeRequest.tagSource`
- POM 从 `kuzhambu-knowledge-domain` 改为 `kuzhambu-knowledge-facade`

数据结构变更：

- `ClassicsTagBindingSupport` 改为消费：
  - `KnowledgeTagFacadeResponse`
  - `KnowledgeContentTagRefFacadeRequest`
  - `KnowledgeRemoveContentTagRefFacadeRequest`

接口定义：

- `KnowledgeFacade.resolveOrCreateManualTag(...)`
- `KnowledgeFacade.resolveOrCreateAiTag(...)`
- `KnowledgeFacade.syncContentTagRef(...)`
- `KnowledgeFacade.removeContentTagRef(...)`

验收点：

- `classics-application` 不再依赖 `knowledge-domain`

### T11. 新增 facade 架构与 provider 测试

范围对象：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/test/java/com/thundax/kuzhambu/knowledge/facade/KnowledgeFacadeArchitectureTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/facade/impl/KnowledgeFacadeImplTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/report/service/impl/KnowledgeReportApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/KnowledgeTaxonomyReadApplicationServiceImplTest.java`

处理动作：

- 新增 facade 模块架构测试
- 新增 facade provider 映射测试
- 复用现有 report / taxonomy 测试作为 provider 语义兜底

数据结构变更：

- 无生产字段新增

接口定义：

- `KnowledgeFacadeArchitectureTest`
- `KnowledgeFacadeImplTest`

验收点：

- facade 协议和 provider 映射都有测试覆盖

### T12. 收缩 POM 白名单与跨域 allowlist

范围对象：

- `kuzhambu-servers/common/kuzhambu-common-test/src/main/java/com/thundax/kuzhambu/common/test/architecture/RepositoryArchitectureRuleSupport.java`
- `kuzhambu-servers/common/kuzhambu-common-test/src/main/java/com/thundax/kuzhambu/common/test/architecture/CrossApplicationIsolationArchitectureRuleSupport.java`
- `docs/00-governance/SERVERS-ARCHITECTURE-RULES.md`

处理动作：

- 删除 `operations/discovery -> knowledge-application` legacy allowlist
- 删除 `classics -> knowledge-domain` legacy allowlist
- 如新增 `knowledge-facade` 规则需要文档补充，则同步更新文档

数据结构变更：

- 无

接口定义：

- 无新增对外接口；仅收缩架构白名单

验收点：

- 新规则下相关模块仍可通过架构测试

## 8. 完成判定

本 RUNBOOK 完成必须同时满足：

- `operations-application` 不再依赖 `kuzhambu-knowledge-application`
- `discovery-application` 不再依赖 `kuzhambu-knowledge-application`
- `classics-application` 不再依赖 `kuzhambu-knowledge-domain`
- 外域生产代码不再 import `com.thundax.kuzhambu.knowledge.application.*`
- 外域生产代码不再 import `com.thundax.kuzhambu.knowledge.domain.*`
- `KnowledgeFacade` 协议、provider 实现、测试、allowlist 收缩全部完成

完成后必须删除本 RUNBOOK。
