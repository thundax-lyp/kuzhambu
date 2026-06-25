# Knowledge-Centric Loop Runbook

## Purpose

本文档用于执行以 Knowledge 为中心的跨域闭环任务，覆盖 `Discovery + AI + Knowledge + Classics` 的后端编排、前端入口、测试数据、验证与收口。

本文档是阶段执行手册，不是稳定治理规则。任务关闭后，应删除本文档及残留引用。

## Scope

覆盖：

- `kuzhambu-servers/biz/knowledge/` 的 taxonomy、graph extraction、refinement 与对外协作语义。
- `kuzhambu-servers/biz/ai/` 的 Discovery / Knowledge usecase 调用编排。
- `kuzhambu-servers/biz/discovery/` 的 query understanding、search enhancement、QA、source 和 debug context。
- `kuzhambu-servers/biz/classics/` 提供给 Knowledge / Discovery 的内容快照、标签和来源协作。
- `kuzhambu-workers/` 已有 discovery / knowledge usecase 的 Java 接入验证。
- `kuzhambu-apps/admin-web/` 与 `kuzhambu-apps/portal-web/` 的必要入口。
- 初始化数据、fixture、验证脚本、coverage 文档和最终清场。

不覆盖：

- 新的顶层业务域拆分。
- Knowledge 图谱可视化大屏。
- 与本轮主路径无关的批量视觉资产、Operations 报表或 Storage 重构。

## Goal

形成一条“内容进入 Knowledge 治理，再被 Discovery 通过 AI 增强消费”的最小完整闭环：

1. `Classics` 提供正式内容、标签绑定和可见性边界。
2. `Knowledge` 治理统一标签、同义词、实体关系和图谱候选/正式结果。
3. `AI` 作为唯一治理入口调度 discovery / knowledge usecase。
4. `Discovery` 消费 Knowledge 的同义词、标签、实体或图谱候选增强搜索与问答。
5. 前端可以真实触发至少一条闭环主路径。
6. 测试数据、验证脚本和清理动作可追溯。

## Execution Rules

- RUNBOOK 必须作为本轮唯一执行手册，要求清晰、准确、无歧义。
- 凡是“大任务”，必须继续拆成单次只涉及 `2-6` 个文件的小任务后再执行。
- 单个小任务必须满足：
  - 目标单一
  - 文件范围明确
  - 数据结构明确
  - 验收结果明确
- 如果一个动作需要同时改动超过 `6` 个文件，必须继续拆分为多个阶段任务。
- 如果一个动作仍无法明确到具体类、接口、表或页面文件，不得直接开始编码。
- 优先复用现有 application / domain / infra / interface 结构，不得为了赶进度跨层穿透。

## Target Loop

本轮目标链路固定为：

1. `Classics` 提供可见内容快照，来源于：
   - [ClassicsSearchSourceContent.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/search/result/ClassicsSearchSourceContent.java)
   - [ClassicsSearchContentApplicationServiceImpl.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/search/service/impl/ClassicsSearchContentApplicationServiceImpl.java)
2. `Knowledge` 提供增强知识，优先消费：
   - taxonomy：`Tag`、`TagAlias`、`Synonym`、`TagContentRef`
   - graph：`KnowledgeEntity`、`KnowledgeRelation`、`GraphVersion`
3. `Discovery` 在搜索或问答前先拉取 Knowledge 增强数据，生成 `QueryUnderstanding`。
4. `Discovery` 再组装候选上下文，通过 `AI` 调 workers：
   - `query-understanding`
   - `query-rewrite`
   - `answer-generation`
5. `Discovery` 将最终结果落到：
   - `discovery_search_query_log`
   - `discovery_qa_session`
   - `discovery_qa_message`
   - `discovery_qa_message_source`
   - `discovery_qa_retrieval_trace`

## Closed Scope

本轮先做最小闭环，不在首轮展开：

- Knowledge 图谱可视化浏览
- QA 会话导出产物生成
- AI stream 全链路
- Discovery 高级排序调优
- 图谱批量重生成

## Code Map

### AI

已有可复用入口：

- [AiInvokeCommand.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/command/AiInvokeCommand.java)
- [AiInvokeResult.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/result/AiInvokeResult.java)
- [AiStreamEventResult.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/result/AiStreamEventResult.java)
- [AiWorkerInvocationApplicationService.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/service/AiWorkerInvocationApplicationService.java)
- [AiWorkerInvocationApplicationServiceImpl.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/service/impl/AiWorkerInvocationApplicationServiceImpl.java)
- [WorkerAiHttpClient.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/client/WorkerAiHttpClient.java)
- [WorkerAiDtos.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/client/dto/WorkerAiDtos.java)

已有 Knowledge 模板，可作为 Discovery 参照：

- [KnowledgeAiExtractionApplicationServiceImpl.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/knowledge/service/impl/KnowledgeAiExtractionApplicationServiceImpl.java)
- [KnowledgeAiWorkerUsecaseResolver.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/knowledge/support/KnowledgeAiWorkerUsecaseResolver.java)
- [KnowledgeAiWorkerUsecaseSpec.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/knowledge/support/KnowledgeAiWorkerUsecaseSpec.java)

### Discovery

已有搜索运行时：

- [SearchQuery.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/query/SearchQuery.java)
- [SearchResult.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/result/SearchResult.java)
- [SearchSourceContent.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/result/SearchSourceContent.java)
- [SearchApplicationServiceImpl.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImpl.java)
- [ClassicsSearchContentProvider.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/ClassicsSearchContentProvider.java)
- [QueryUnderstandingApplicationServiceImpl.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/QueryUnderstandingApplicationServiceImpl.java)

已有 QueryUnderstanding 持久化模型：

- [QueryUnderstanding.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/entity/QueryUnderstanding.java)
- [QueryUnderstandingRepository.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/repository/QueryUnderstandingRepository.java)
- [QueryUnderstandingDO.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/dataobject/QueryUnderstandingDO.java)
- [QueryUnderstandingMapper.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/mapper/QueryUnderstandingMapper.java)
- [QueryUnderstandingRepositoryImpl.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/repository/impl/QueryUnderstandingRepositoryImpl.java)

当前缺失的 QA 运行时代码需要补齐，建议新增子域：

- `application/qa/`
- `domain/qa/`
- `infra/qa/`
- `interfaces/portal/qa/`
- `interfaces/admin/qa/`

### Knowledge

taxonomy 主入口：

- [TaxonomyApplicationService.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/TaxonomyApplicationService.java)
- [TaxonomyApplicationServiceImpl.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImpl.java)
- [KnowledgeTaxonomyController.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/KnowledgeTaxonomyController.java)
- [Synonym.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/model/entity/Synonym.java)
- [Tag.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/model/entity/Tag.java)
- [TagAlias.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/model/entity/TagAlias.java)
- [TagContentRef.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/model/entity/TagContentRef.java)
- [SynonymRepository.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/repository/SynonymRepository.java)

graph / extraction 主入口：

- [KnowledgeGraphExtractionApplicationServiceImpl.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/service/impl/KnowledgeGraphExtractionApplicationServiceImpl.java)
- [KnowledgeGraphCandidateApplySupport.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/support/KnowledgeGraphCandidateApplySupport.java)
- [GraphExtractionTask.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/model/entity/GraphExtractionTask.java)
- [GraphVersion.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/model/entity/GraphVersion.java)
- [KnowledgeEntity.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/model/entity/KnowledgeEntity.java)
- [KnowledgeRelation.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/model/entity/KnowledgeRelation.java)

### Classics

本轮只复用，不改真相源定义：

- [ClassicsSearchContentApplicationService.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/search/service/ClassicsSearchContentApplicationService.java)
- [ClassicsSearchContentApplicationServiceImpl.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/search/service/impl/ClassicsSearchContentApplicationServiceImpl.java)
- [ClassicsTagBindingSupport.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsTagBindingSupport.java)

### Frontend

Admin Web 已有 Knowledge 页面：

- `src/pages/knowledge/taxonomy/*`
- `src/pages/knowledge/graph-extraction/*`
- `src/pages/knowledge/graph-results/*`
- `src/pages/knowledge/refinement/*`

Portal Web 当前没有 Discovery 搜索/问答页面，预计新增：

- `src/pages/discovery/search-page.tsx`
- `src/pages/discovery/search-service.ts`
- `src/pages/discovery/search-types.ts`
- `src/pages/discovery/qa-page.tsx`
- `src/pages/discovery/qa-service.ts`
- `src/pages/discovery/qa-types.ts`

## File-by-File Workboard

以下任务按“单次改动 2-6 个文件”组织。执行时按顺序推进，允许在同一阶段内微调，但不得跨阶段大面积扩散。

### Stage A. AI Discovery Usecase Resolver

#### Task A1. 新增 Discovery usecase spec 与 resolver

- 目标：定义 Discovery usecase 到 worker path 的稳定映射。
- 文件范围：
  - `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/discovery/support/DiscoveryAiWorkerUsecaseSpec.java`
  - `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/discovery/support/DiscoveryAiWorkerUsecaseResolver.java`
  - `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/discovery/support/DiscoveryAiWorkerUsecaseResolverTest.java`
- 数据结构：
  - `operation`
  - `capability`
  - `workerPath`
  - `stream`
- 验收：
  - 可以稳定解析 `DISCOVERY_QUERY_UNDERSTANDING`
  - 可以稳定解析 `DISCOVERY_QUERY_REWRITE`
  - 可以稳定解析 `DISCOVERY_ANSWER_GENERATION`
  - 可以稳定解析 `DISCOVERY_ANSWER_GENERATION_STREAM`

#### Task A2. 新增 Discovery AI request/result/domain service

- 目标：给 Discovery 提供 AI 域内部统一调用口径。
- 文件范围：
  - `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/discovery/model/valueobject/DiscoveryAiRequest.java`
  - `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/discovery/model/valueobject/DiscoveryAiResult.java`
  - `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/discovery/service/DiscoveryAiDomainService.java`
- 数据结构：
  - `requestId`
  - `traceId`
  - `serviceId`
  - `serviceRole`
  - `modelId`
  - `modelName`
  - `promptVersionId`
  - `promptMessagesJson`
  - `promptVariablesJson`
  - `inputPayloadJson`
  - `outputSchemaJson`
  - `locale`
  - `stream`
- 验收：
  - request/result 字段口径与 Knowledge AI 调用保持一致

#### Task A3. 新增 Discovery AI application service

- 目标：复用 `AiWorkerInvocationApplicationService` 执行 Discovery usecase。
- 文件范围：
  - `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/discovery/service/DiscoveryAiApplicationService.java`
  - `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/discovery/service/impl/DiscoveryAiApplicationServiceImpl.java`
  - `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/discovery/service/impl/DiscoveryAiApplicationServiceImplTest.java`
  - `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/command/AiInvokeCommand.java`
- 数据结构：
  - `AiInvokeCommand` 中 Discovery 调用需要的字段映射
- 验收：
  - 能根据 usecase resolver 组装 `AiInvokeCommand`
  - 能返回同步 `DiscoveryAiResult`

### Stage B. Knowledge Read Facade For Discovery

#### Task B1. 新增 Discovery 可消费的 taxonomy 只读结果模型

- 目标：避免 Discovery 直接复用 Admin page query。
- 文件范围：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/result/DiscoverySynonymExpandResult.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/result/DiscoveryTagHintResult.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/result/DiscoveryEntityHintResult.java`
- 数据结构：
  - `term`
  - `normalizedTerm`
  - `expandedTerms`
  - `matchedTagName`
  - `matchedAliasName`
  - `contentRefCount`
- 验收：
  - 读模型不带分页页面专用字段

#### Task B2. 新增 Knowledge taxonomy read application service

- 目标：向 Discovery 暴露稳定只读协作接口。
- 文件范围：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/KnowledgeTaxonomyReadApplicationService.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/KnowledgeTaxonomyReadApplicationServiceImpl.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/KnowledgeTaxonomyReadApplicationServiceImplTest.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/repository/SynonymRepository.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/repository/TagRepository.java`
- 数据结构：
  - 输入 `queryText`
  - 输出同义词扩展列表、标签提示列表
- 验收：
  - Discovery 可通过 application service 拿到增强数据
  - 不直接依赖 mapper / DO / repository impl

### Stage C. Discovery QueryUnderstanding

#### Task C1. 扩展 QueryUnderstanding application contract

- 目标：把“返回字符串”升级为结构化结果。
- 文件范围：
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/QueryUnderstandingApplicationService.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/result/QueryUnderstandingResult.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/query/SearchQuery.java`
- 数据结构：
  - `normalizedQueryText`
  - `rewrittenQueryText`
  - `intentType`
  - `expandedSynonyms`
  - `recognizedEntities`
  - `requestId`
  - `traceId`
- 验收：
  - application contract 不再只返回 `String`

#### Task C2. 实现 QueryUnderstanding runtime 主链路

- 目标：规则归一化 + Knowledge 增强 + AI 改写 + 落库。
- 文件范围：
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/QueryUnderstandingApplicationServiceImpl.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/DiscoveryKnowledgeEnhancementProvider.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/QueryUnderstandingPayloadBuilder.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/QueryUnderstandingApplicationServiceImplTest.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/entity/QueryUnderstanding.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/repository/QueryUnderstandingRepository.java`
- 数据结构：
  - `recognizedEntitiesJson`
  - `expandedSynonymsJson`
  - `understandingStatus`
  - `failureCode`
  - `failureMessage`
- 验收：
  - 不再抛 `not-implemented`
  - 能持久化一条 `QueryUnderstanding`

#### Task C3. 对接 Search 主链路消费 QueryUnderstanding

- 目标：让搜索可消费增强结果。
- 文件范围：
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImpl.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/result/SearchResult.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImplTest.java`
- 数据结构：
  - `displayQueryText`
  - 搜索 query 使用 `rewrittenQueryText` 或 `normalizedQueryText`
- 验收：
  - Search 能接住 QueryUnderstanding 结果继续检索

### Stage D. Discovery QA Domain

#### Task D1. 新增 QA domain entity 与 repository contract

- 目标：建立 Discovery QA 领域模型。
- 文件范围：
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaSession.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaMessage.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaSource.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaRetrievalTrace.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaSessionRepository.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaMessageRepository.java`
- 数据结构：
  - 对齐 `discovery_qa_session` / `discovery_qa_message`
- 验收：
  - entity / repository 命名与 schema 一一对应

#### Task D2. 补齐 QA source / retrieval repository contract

- 目标：补全来源与调试追溯模型。
- 文件范围：
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaSourceRepository.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaRetrievalTraceRepository.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/test/java/com/thundax/kuzhambu/discovery/domain/DiscoveryDomainArchitectureTest.java`
- 数据结构：
  - 对齐 `discovery_qa_message_source` / `discovery_qa_retrieval_trace`
- 验收：
  - domain 架构测试通过

#### Task D3. 新增 QA infra dataobject / mapper

- 目标：把既有 schema 对上 infra 持久化。
- 文件范围：
  - `.../infra/qa/persistence/dataobject/QaSessionDO.java`
  - `.../infra/qa/persistence/dataobject/QaMessageDO.java`
  - `.../infra/qa/persistence/dataobject/QaSourceDO.java`
  - `.../infra/qa/persistence/dataobject/QaRetrievalTraceDO.java`
  - `.../infra/qa/persistence/mapper/QaSessionMapper.java`
  - `.../infra/qa/persistence/mapper/QaMessageMapper.java`
- 数据结构：
  - 列名与 `db/schema/discovery.sql` 完全一致
- 验收：
  - mapper 与 DO 完整覆盖既有 QA 表

#### Task D4. 新增 QA infra assembler / repository impl

- 目标：完成 domain 与持久化桥接。
- 文件范围：
  - `.../infra/qa/persistence/assembler/QaPersistenceAssembler.java`
  - `.../infra/qa/repository/impl/QaSessionRepositoryImpl.java`
  - `.../infra/qa/repository/impl/QaMessageRepositoryImpl.java`
  - `.../infra/qa/repository/impl/QaSourceRepositoryImpl.java`
  - `.../infra/qa/repository/impl/QaRetrievalTraceRepositoryImpl.java`
  - `.../src/test/java/.../infra/qa/*RepositoryImplTest.java`
- 数据结构：
  - `session_id`
  - `message_id`
  - `source_id`
  - `trace_id`
- 验收：
  - repository 能完成最小写入和读取

### Stage E. Discovery QA Application

#### Task E1. 新增 QA application command/query/result

- 目标：定义 QA 用例输入输出。
- 文件范围：
  - `.../application/qa/command/OpenQaSessionCommand.java`
  - `.../application/qa/command/AskQuestionCommand.java`
  - `.../application/qa/query/QaSessionPageQuery.java`
  - `.../application/qa/result/QaSessionResult.java`
  - `.../application/qa/result/QaAnswerResult.java`
  - `.../application/qa/result/QaSourceResult.java`
- 数据结构：
  - `sessionId`
  - `question`
  - `answer`
  - `sources`
  - `traceSummary`
- 验收：
  - application contract 能完整表达一次问答

#### Task E2. 新增 QA application service

- 目标：编排来源检索、AI 回答生成和结果落库。
- 文件范围：
  - `.../application/qa/service/QaApplicationService.java`
  - `.../application/qa/service/impl/QaApplicationServiceImpl.java`
  - `.../application/qa/support/QaContextAssembler.java`
  - `.../application/qa/support/QaSourceAssembler.java`
  - `.../application/qa/support/QaTraceAssembler.java`
  - `.../src/test/java/.../application/qa/service/impl/QaApplicationServiceImplTest.java`
- 数据结构：
  - `context_turn_count`
  - `candidate_count`
  - `context_snapshot`
  - `source_rank`
  - `score`
- 验收：
  - 能创建 session
  - 能保存 user / assistant message
  - 能保存 source 与 retrieval trace

#### Task E3. 接入 AI 与 Classics 内容来源

- 目标：让 QA application service 能真正拿上下文并调 AI。
- 文件范围：
  - `.../application/qa/service/impl/QaApplicationServiceImpl.java`
  - `.../application/search/support/ClassicsSearchContentProvider.java`
  - `.../application/search/result/SearchSourceContent.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/search/service/ClassicsSearchContentApplicationService.java`
  - `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/discovery/service/DiscoveryAiDomainService.java`
- 数据结构：
  - `title_snapshot`
  - `location_label`
  - `snippet`
  - `knowledge_base`
- 验收：
  - QA 主链路可拿到来源并生成回答

### Stage F. Discovery Controllers

#### Task F1. 新增 Portal QA controller

- 目标：开放 Portal 问答最小接口。
- 文件范围：
  - `.../interfaces/portal/qa/controller/DiscoveryQaPortalController.java`
  - `.../interfaces/portal/qa/controller/request/DiscoveryQaRequests.java`
  - `.../interfaces/portal/qa/controller/response/DiscoveryQaResponses.java`
  - `.../interfaces/portal/qa/assembler/DiscoveryQaPortalInterfaceAssembler.java`
  - `.../src/test/java/.../interfaces/portal/qa/controller/DiscoveryQaPortalControllerTest.java`
- 数据结构：
  - open session request
  - ask question request
  - answer response
- 验收：
  - Portal 可通过 HTTP 触发 QA 主链路

#### Task F2. 新增 Admin QA controller

- 目标：提供最小调试 / 查看入口。
- 文件范围：
  - `.../interfaces/admin/qa/controller/DiscoveryQaAdminController.java`
  - `.../interfaces/admin/qa/controller/request/DiscoveryQaAdminRequests.java`
  - `.../interfaces/admin/qa/controller/response/DiscoveryQaAdminResponses.java`
  - `.../interfaces/admin/qa/assembler/DiscoveryQaAdminInterfaceAssembler.java`
  - `.../src/test/java/.../interfaces/admin/qa/controller/DiscoveryQaAdminControllerTest.java`
- 数据结构：
  - session page
  - message detail
  - retrieval trace detail
- 验收：
  - Admin 至少能查看 session / source / trace

### Stage G. Portal Web

#### Task G1. 新增 Discovery 搜索页面与 service

- 目标：Portal 提供真实搜索入口。
- 文件范围：
  - `kuzhambu-apps/portal-web/src/pages/discovery/search-page.tsx`
  - `kuzhambu-apps/portal-web/src/pages/discovery/search-service.ts`
  - `kuzhambu-apps/portal-web/src/pages/discovery/search-types.ts`
  - `kuzhambu-apps/portal-web/src/pages/discovery/search-page.test.tsx`
  - `kuzhambu-apps/portal-web/src/app.tsx`
- 数据结构：
  - `DiscoverySearchResponse`
  - `DiscoverySearchGroup`
  - `DiscoverySearchItem`
- 验收：
  - 页面能提交 query 并渲染 groups

#### Task G2. 新增 Discovery QA 页面与 service

- 目标：Portal 提供最小问答入口。
- 文件范围：
  - `kuzhambu-apps/portal-web/src/pages/discovery/qa-page.tsx`
  - `kuzhambu-apps/portal-web/src/pages/discovery/qa-service.ts`
  - `kuzhambu-apps/portal-web/src/pages/discovery/qa-types.ts`
  - `kuzhambu-apps/portal-web/src/pages/discovery/qa-page.test.tsx`
  - `kuzhambu-apps/portal-web/src/app.tsx`
- 数据结构：
  - `QaSession`
  - `QaMessage`
  - `QaSource`
- 验收：
  - 页面能提问并展示 answer + sources

#### Task G3. Portal 导航与样式收口

- 目标：把入口接到现有 portal 骨架中。
- 文件范围：
  - `kuzhambu-apps/portal-web/src/pages/home/home-page.tsx`
  - `kuzhambu-apps/portal-web/src/styles.css`
  - `kuzhambu-apps/portal-web/src/components/ui/*` 中最多 4 个必要文件
- 数据结构：
  - 无新增后端数据结构
- 验收：
  - 首页或导航可进入 search / qa 页面

### Stage H. Admin Web

#### Task H1. 新增 Discovery Admin 调试 service 与 types

- 目标：给 Admin 提供调试数据读取口径。
- 文件范围：
  - `kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-service.ts`
  - `kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-types.ts`
  - `kuzhambu-apps/admin-web/src/pages/discovery/search-admin/search-admin-service.ts`
  - `kuzhambu-apps/admin-web/src/pages/discovery/search-admin/search-admin-types.ts`
- 数据结构：
  - session page result
  - trace detail result
  - query understanding detail result
- 验收：
  - service/types 与 controller 协议一一对应

#### Task H2. 新增 Discovery Admin 页面

- 目标：最小可观察，不做复杂分析。
- 文件范围：
  - `kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-page.tsx`
  - `kuzhambu-apps/admin-web/src/pages/discovery/search-admin/search-admin-page.tsx`
  - `kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-page.test.tsx`
  - `kuzhambu-apps/admin-web/src/pages/discovery/search-admin/search-admin-page.test.tsx`
  - 路由注册文件 1-2 个
- 数据结构：
  - session list
  - message detail
  - source list
  - retrieval trace
- 验收：
  - Admin 可查看 query understanding 与 QA trace

### Stage I. Test Data And Seeds

#### Task I1. 补最小联调 seed

- 目标：提供跨域最小闭环测试数据。
- 文件范围：
  - `db/data/classics.sql`
  - `db/data/knowledge.sql`
  - `db/data/discovery.sql`
  - `db/data/ai.sql`
- 数据结构：
  - 至少 1 条 `SANCAI_ENTRY`
  - 至少 1 条 `WANGQI_DOCUMENT`
  - 至少 1 条 `MING_CUSTOMS`
  - 至少 1 组 synonym / tag / alias / tagContentRef
- 验收：
  - 本地库可支撑 search + qa 联调

#### Task I2. 补菜单与前端入口 seed

- 目标：保证 Admin 菜单可进入 Discovery 调试页。
- 文件范围：
  - `db/data-source/system.json`
  - `db/data/system.sql`
  - 若需要，相关生成脚本 1-2 个
- 数据结构：
  - discovery admin 菜单
  - 权限码
- 验收：
  - 登录后可见新页面入口

### Stage J. Verification And Cleanup

#### Task J1. 后端验证

- 文件范围：
  - 新增或修改的 Java test 文件 2-6 个一组逐批完成
- 验收：
  - `spotless:check`
  - `checkstyle:check`
  - 相关模块 `mvn test`

#### Task J2. 前端验证

- 文件范围：
  - 新增或修改的前端测试文件 2-6 个一组逐批完成
- 验收：
  - `format:check`
  - `lint`
  - `test`
  - `build`

#### Task J3. 收口与清场

- 文件范围：
  - `docs/40-readiness/{AI,DISCOVERY,KNOWLEDGE,CLASSICS}-IMPLEMENTATION-COVERAGE.md`
  - 本 RUNBOOK
  - 临时脚本 / 临时数据 / 临时截图
- 验收：
  - 文档口径一致
  - 无无用临时文件残留
  - 本 RUNBOOK 若已无价值则删除

## Data Structures

### Discovery Search Input / Output

- `SearchQuery`
  - `queryText`
  - `scope`
  - `filters`
- `SearchSourceContent`
  - `contentDomain`
  - `contentType`
  - `contentId`
  - `knowledgeBase`
  - `categoryCode`
  - `categoryName`
  - `title`
  - `summary`
  - `textSegments`
  - `tagNames`
  - `status`
  - `visibility`
  - `currentVersionNo`
- `SearchResult`
  - `searchLogId`
  - `displayQueryText`
  - `groups`

### Query Understanding

- `QueryUnderstanding`
  - `queryUnderstandingId`
  - `searchLogId`
  - `queryText`
  - `normalizedQueryText`
  - `rewrittenQueryText`
  - `intentType`
  - `recognizedEntitiesJson`
  - `expandedSynonymsJson`
  - `understandingStatus`
  - `failureCode`
  - `failureMessage`
  - `requestId`
  - `traceId`

### Knowledge Taxonomy Data

- `Synonym`
  - 预期至少包含主词、同义词、状态
- `Tag`
  - 主标签名称、分类、来源、审核状态、启用状态
- `TagAlias`
  - 标签别名
- `TagContentRef`
  - `contentType`
  - `contentId`
  - `contentTitle`
  - `tagId`

### Discovery QA Data

本轮以 `db/schema/discovery.sql` 中既有表为准：

- `discovery_qa_session`
  - `session_id`
  - `owner_user_id`
  - `title`
  - `scope`
  - `context_mode`
  - `context_content_type`
  - `context_content_id`
  - `status`
- `discovery_qa_message`
  - `message_id`
  - `session_id`
  - `role`
  - `content`
  - `message_status`
  - `context_turn_count`
  - `failure_reason`
- `discovery_qa_message_source`
  - `source_id`
  - `message_id`
  - `content_type`
  - `content_id`
  - `knowledge_base`
  - `title_snapshot`
  - `location_label`
  - `snippet`
  - `source_rank`
  - `score`
  - `source_status`
- `discovery_qa_retrieval_trace`
  - `trace_id`
  - `message_id`
  - `raw_question`
  - `rewritten_question`
  - `scope`
  - `filters_json`
  - `expanded_terms_json`
  - `linked_entities_json`
  - `candidate_count`
  - `context_snapshot`

## Milestones

### M1. 固定主路径

- 确认本轮主路径以 `Knowledge` 为中心，而不是同时铺满所有 Discovery QA 细节。
- 确认最小交付顺序：
  - `Knowledge taxonomy -> Discovery query understanding`
  - `Knowledge extraction -> Discovery answer generation`
  - `Classics content snapshot -> Discovery source citation`

交付文件：

- 本 RUNBOOK
- `docs/40-readiness/{AI,DISCOVERY,KNOWLEDGE,CLASSICS}-IMPLEMENTATION-COVERAGE.md` 的阶段口径更新说明草案

### M2. 后端闭环

任务拆分：

1. `AI` 域补 Discovery usecase resolver
   - 新增建议：
     - `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/discovery/support/DiscoveryAiWorkerUsecaseSpec.java`
     - `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/discovery/support/DiscoveryAiWorkerUsecaseResolver.java`
   - 复用：
     - `AiInvokeCommand`
     - `AiWorkerInvocationApplicationService`
   - 目标：
     - 固定 `DISCOVERY_QUERY_UNDERSTANDING`
     - 固定 `DISCOVERY_QUERY_REWRITE`
     - 固定 `DISCOVERY_ANSWER_GENERATION`
     - 固定 `DISCOVERY_ANSWER_GENERATION_STREAM`

2. `AI` 域补 Discovery application service
   - 新增建议：
     - `.../application/discovery/service/DiscoveryAiApplicationService.java`
     - `.../application/discovery/service/impl/DiscoveryAiApplicationServiceImpl.java`
     - `.../domain/discovery/model/valueobject/DiscoveryAiRequest.java`
     - `.../domain/discovery/model/valueobject/DiscoveryAiResult.java`
     - `.../domain/discovery/service/DiscoveryAiDomainService.java`
   - 数据结构对齐：
     - `promptMessagesJson`
     - `promptVariablesJson`
     - `inputPayloadJson`
     - `outputSchemaJson`
     - `requestId`
     - `traceId`

3. `Discovery` 实现 QueryUnderstanding 运行时
   - 修改：
     - [QueryUnderstandingApplicationService.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/QueryUnderstandingApplicationService.java)
     - [QueryUnderstandingApplicationServiceImpl.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/QueryUnderstandingApplicationServiceImpl.java)
   - 可能新增 support：
     - `.../application/search/support/DiscoveryKnowledgeEnhancementProvider.java`
     - `.../application/search/support/QueryUnderstandingPayloadBuilder.java`
   - 目标：
     - 读取 `Knowledge` 同义词
     - 产出 `normalizedQueryText`
     - 产出 `expandedSynonymsJson`
     - 产出 `recognizedEntitiesJson`
     - 调用 AI 域获取 `rewrittenQueryText` 或结构化理解结果
     - 落库 `QueryUnderstanding`

4. `Knowledge` 提供 Discovery 可消费的只读增强口径
   - 优先复用：
     - `TaxonomyApplicationService#pageSynonyms(...)`
     - `TagDetailResult`
     - `TagContentRefResult`
   - 若现有 application service 不适合运行时内部消费，新增建议：
     - `.../application/taxonomy/service/KnowledgeTaxonomyReadApplicationService.java`
     - `.../application/taxonomy/result/DiscoverySynonymExpandResult.java`
     - `.../application/taxonomy/result/DiscoveryTagHintResult.java`
   - 禁止：
     - Discovery 直接读 `knowledge` mapper / DO / repository impl

5. `Discovery QA` 最小运行时落库
   - 新增建议子域文件：
     - `application/qa/command/*`
     - `application/qa/query/*`
     - `application/qa/result/*`
     - `application/qa/service/QaApplicationService.java`
     - `application/qa/service/impl/QaApplicationServiceImpl.java`
     - `domain/qa/model/entity/QaSession.java`
     - `domain/qa/model/entity/QaMessage.java`
     - `domain/qa/model/entity/QaSource.java`
     - `domain/qa/model/entity/QaRetrievalTrace.java`
     - `domain/qa/repository/*`
     - `infra/qa/persistence/dataobject/*`
     - `infra/qa/persistence/mapper/*`
     - `infra/qa/repository/impl/*`
   - 表映射必须对齐 `db/schema/discovery.sql` 已有 QA 表，不另起新表名

6. `Discovery` Portal / Admin 接口
   - Portal 建议新增：
     - `interfaces/portal/qa/controller/DiscoveryQaPortalController.java`
     - `interfaces/portal/qa/controller/request/*`
     - `interfaces/portal/qa/controller/response/*`
     - `interfaces/portal/qa/assembler/*`
   - Admin 建议新增：
     - `interfaces/admin/qa/controller/DiscoveryQaAdminController.java`
     - `interfaces/admin/qa/controller/request/*`
     - `interfaces/admin/qa/controller/response/*`
   - Portal Search controller 可能需要在请求或响应中补 QueryUnderstanding 透传字段

7. `Classics` 来源拼装
   - 复用：
     - `ClassicsSearchSourceContent`
     - `ClassicsSearchContentApplicationServiceImpl`
   - 必要时新增：
     - `ClassicsQaSourceSnapshotResult`
     - `ClassicsSourceCitationApplicationService`
   - 目标：
     - 给 `Discovery QA` 提供稳定 `title_snapshot / snippet / location_label`

8. 保证权限顺序
   - `SearchPermissionFilter`
   - `DefaultSearchPermissionFilter`
   - QA 运行时要在来源进入 AI 上下文前完成同级过滤

### M3. 前端入口

Admin Web：

1. 若只做最小可观测入口，优先补：
   - `src/pages/knowledge/graph-extraction/*` 上增加与 Discovery 消费关系的说明或跳转
   - 新增 `src/pages/discovery/search-admin/*`
   - 新增 `src/pages/discovery/qa-admin/*`
2. 同步修改路由注册文件和菜单映射文件

Portal Web：

1. 新增 Discovery 搜索页
   - `src/pages/discovery/search-page.tsx`
   - `src/pages/discovery/search-service.ts`
   - `src/pages/discovery/search-types.ts`
2. 新增 Discovery QA 页
   - `src/pages/discovery/qa-page.tsx`
   - `src/pages/discovery/qa-service.ts`
   - `src/pages/discovery/qa-types.ts`
3. 修改：
   - `src/app.tsx`
   - 可能的导航组件或首页入口 `src/pages/home/home-page.tsx`
4. 页面最小能力：
   - 输入 query / question
   - 展示 search groups
   - 展示 answer
   - 展示 cited sources
   - 跳转对应分享或内容详情路径占位

### M4. 测试数据与验证

- 补齐跨域联调需要的内容数据、标签数据、同义词数据、候选结果或 mock worker 响应。
- 后端至少补齐：
  - Discovery query understanding application test
  - Discovery QA application / controller test
  - AI discovery usecase resolver / invocation test
  - Knowledge -> Discovery 协作数据读取测试
- 前端至少补齐：
  - 相关页面单测
  - 最小 Playwright 或联调冒烟
- 如需 dev 数据同步，更新对应脚本或初始化数据来源，并记录恢复方式。

测试数据文件落点：

- schema：
  - [db/schema/discovery.sql](/Volumes/storage/workspace/kuzhambu/db/schema/discovery.sql)
  - [db/schema/knowledge.sql](/Volumes/storage/workspace/kuzhambu/db/schema/knowledge.sql)
  - [db/schema/classics.sql](/Volumes/storage/workspace/kuzhambu/db/schema/classics.sql)
  - [db/schema/ai.sql](/Volumes/storage/workspace/kuzhambu/db/schema/ai.sql)
- 初始化数据：
  - [db/data/discovery.sql](/Volumes/storage/workspace/kuzhambu/db/data/discovery.sql)
  - [db/data/knowledge.sql](/Volumes/storage/workspace/kuzhambu/db/data/knowledge.sql)
  - [db/data/classics.sql](/Volumes/storage/workspace/kuzhambu/db/data/classics.sql)
  - [db/data/ai.sql](/Volumes/storage/workspace/kuzhambu/db/data/ai.sql)
  - [db/data-source/system.json](/Volumes/storage/workspace/kuzhambu/db/data-source/system.json)

建议新增或补齐的测试：

- AI
  - `.../ai/application/discovery/support/DiscoveryAiWorkerUsecaseResolverTest.java`
  - `.../ai/application/discovery/service/impl/DiscoveryAiApplicationServiceImplTest.java`
- Discovery
  - `.../discovery/application/search/service/impl/QueryUnderstandingApplicationServiceImplTest.java`
  - `.../discovery/application/qa/service/impl/QaApplicationServiceImplTest.java`
  - `.../discovery/interfaces/portal/qa/controller/DiscoveryQaPortalControllerTest.java`
  - `.../discovery/interfaces/admin/qa/controller/DiscoveryQaAdminControllerTest.java`
- Knowledge
  - `.../knowledge/application/taxonomy/service/impl/*` 中补 Discovery 消费侧测试
- Frontend
  - `portal-web/src/pages/discovery/*.test.tsx`
  - `admin-web/src/pages/discovery/*.test.tsx`

### M5. 清理现场

- 更新 `docs/40-readiness/{AI,DISCOVERY,KNOWLEDGE,CLASSICS}-IMPLEMENTATION-COVERAGE.md`。
- 如果本轮改变接口或稳定契约，更新 `docs/20-interfaces/` 与 `docs/30-designs/`。
- 删除 `TODO.md` 中已完成任务，收窄未完成项。
- 删除无保留价值的临时脚本、fixture、手工调试数据和 RUNBOOK。

清理时逐项检查：

1. 删除临时 discovery mock payload、手工 curl 脚本、临时截图
2. 删除只为联调临时新增但没有复用价值的 seed
3. 回收 RUNBOOK 中已完成但不再需要保留的执行痕迹
4. 更新 coverage 文档后再决定是否删除本 RUNBOOK

## Acceptance

满足以下条件时，本轮可视为阶段闭环成立：

- Discovery 查询理解不再返回未实现异常。
- Discovery 至少能消费一类 Knowledge 增强数据。
- Discovery 至少能通过 AI 域完成一次回答生成。
- 回答结果带来源，来源能定位到对应 Classics 内容。
- 前端存在至少一个真实入口可触发这条链路。
- 测试数据与最小自动化验证齐备。
- coverage 文档同步完成。

## Validation

后端最小验证：

- `mvn -pl ... spotless:apply`
- `mvn spotless:check`
- `mvn checkstyle:check`
- `mvn -pl ... test`

前端最小验证：

- `npm --workspace ... run format`
- `npm run format:check`
- `npm run lint`
- `npm run test`
- `npm run build`

如新增验证脚本，遵守 `Prepare / Execute / Assert / Restore` 四段式协议。

## Cleanup Checklist

- 删除本轮临时联调脚本、临时 SQL、临时 JSON、临时截图和无复用价值的 fixture。
- 删除 `TODO.md` 中已完成项，剩余项收窄为未完成内容。
- 若 RUNBOOK 内容已无执行价值，删除 `docs/30-designs/RUNBOOK-KNOWLEDGE-CENTRIC-LOOP.md`。
- 检查 `git diff`，只保留本轮相关修改。
- 检查 coverage 文档、接口文档和需求/设计文档是否与最终代码口径一致。
