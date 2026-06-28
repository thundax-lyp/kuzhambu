# RUNBOOK: AI Facade Isolation

## 1. 目标

把 `ai` 域改造成和 `storage` 一样的单体内统一跨域边界：

- 新增独立模块 `kuzhambu-ai-facade`
- 外域不再直接依赖 `kuzhambu-ai-application`
- 外域不再直接依赖 `kuzhambu-ai-domain`
- 外域统一通过 `AiFacade` 调用 `ai`
- `FacadeImpl`、`FacadeAssembler` 继续放在 `kuzhambu-ai-application`

本 RUNBOOK 只覆盖当前已经存在的跨域调用，不顺手扩展 `ai-interface` 对 admin 配置、提示词、模型管理的内部后台入口。

## 1.1 RUNBOOK 书写约束

- 任务描述必须清晰、明确，不写“顺手处理”“按需调整”“必要时优化”这类模糊词。
- 如果一个任务会同时改动超过 5 个文件，必须继续拆分，直到单个任务只覆盖 2-5 个文件。
- 如果一个任务只改 1 个文件，但无法独立验证，应和直接相关文件合并，形成 2-5 个文件的小任务。
- 数据结构变更必须明确到字段；不得只写“替换为 facade dto”。
- 相关文件必须精确列出到文件路径；不得只写模块名，不得用 `*.java`、`...`、`support/*` 这类模糊写法作为执行清单。
- 改名任务必须同时说明目标包位；不得只写类名变化。

## 2. 当前直依赖现状

### 2.1 POM 直依赖

- `kuzhambu-knowledge-application/pom.xml`
  - 直接依赖 `kuzhambu-ai-domain`
  - 直接依赖 `kuzhambu-ai-application`
- `kuzhambu-discovery-application/pom.xml`
  - 直接依赖 `kuzhambu-ai-domain`
- `kuzhambu-classics-application/pom.xml`
  - 直接依赖 `kuzhambu-ai-domain`
- `kuzhambu-operations-application/pom.xml`
  - 直接依赖 `kuzhambu-ai-application`

### 2.2 生产代码直依赖

#### operations -> ai-application

- [DefaultOperationsReportMetricsGateway.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGateway.java)
  - 依赖 `AiReportApplicationService`
  - 调用 `summary(Date, Date, String)`

#### discovery -> ai-domain

- [QaApplicationServiceImpl.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImpl.java)
  - 依赖 `DiscoveryAiDomainService`
  - 使用 `DiscoveryAiRequest`
  - 使用 `DiscoveryAiResult`
  - 调用 `generateAnswer(...)`

- [QueryUnderstandingApplicationServiceImpl.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/QueryUnderstandingApplicationServiceImpl.java)
  - 依赖 `DiscoveryAiDomainService`
  - 使用 `DiscoveryAiRequest`
  - 使用 `DiscoveryAiResult`
  - 调用 `understandQuery(...)`

#### classics -> ai-domain

- [ClassicsContentApplicationServiceImpl.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java)
  - 依赖 `AiCandidateDomainService`
  - 使用 `AiCandidateApplyCheck`
  - 间接依赖 `AiCandidate`
  - 调用 `requirePendingForApply(...)`
  - 调用 `markApplied(...)`

#### knowledge -> ai-application + ai-domain

- [KnowledgeGraphExtractionApplicationServiceImpl.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/service/impl/KnowledgeGraphExtractionApplicationServiceImpl.java)
  - 依赖 `AiBatchJobApplicationService`
  - 依赖 `KnowledgeAiExtractionDomainService`
  - 依赖 `AiInvocationRepository`
  - 依赖 `AiCandidateDomainService`
  - 使用 `AiBatchJobCreateCommand`
  - 使用 `AiBatchJobResult`
  - 使用 `KnowledgeAiExtractionRequest`
  - 使用 `KnowledgeAiExtractionResult`
  - 使用 `AiCandidateApplyCheck`
  - 使用 `AiCallRecord`
  - 使用 `AiCandidate`
  - 调用：
    - `create(...)`
    - `get(...)`
    - `canDispatchNextUnit(...)`
    - `recordSuccess(...)`
    - `recordFailure(...)`
    - `cancel(...)`
    - `extractRelations(...)`
    - `extractGraph(...)`
    - `extractLineage(...)`
    - `getCallRecord(...)`
    - `getCandidate(...)`
    - `requirePendingForApply(...)`
    - `markApplied(...)`

- [KnowledgeGraphCandidateApplySupport.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/support/KnowledgeGraphCandidateApplySupport.java)
  - 使用 `AiCandidate`

## 3. 目标边界

`ai` 域对外统一只暴露一个 `AiFacade`，不再按 `application` 或 `domain service` 原样泄漏。

### 3.1 对外统一入口

- 新增：
  - `biz/ai/kuzhambu-ai-facade/`
  - `com.thundax.kuzhambu.ai.facade.AiFacade`

### 3.2 Facade 能力面

本轮最小能力面只覆盖现有外域调用：

- `summary(...)`
  - 供 `operations` 读取 AI 报表摘要
- `understandDiscoveryQuery(...)`
  - 供 `discovery` 做查询理解
- `generateDiscoveryAnswer(...)`
  - 供 `discovery` 生成问答回答
- `extractKnowledgeRelations(...)`
- `extractKnowledgeGraph(...)`
- `extractKnowledgeLineage(...)`
  - 供 `knowledge` 发起图谱抽取
- `createBatchJob(...)`
- `getBatchJob(...)`
- `canDispatchNextBatchUnit(...)`
- `recordBatchSuccess(...)`
- `recordBatchFailure(...)`
- `cancelBatchJob(...)`
  - 供 `knowledge` 编排批任务
- `getCallRecord(...)`
- `getCandidate(...)`
  - 供 `knowledge` 同步任务状态
- `requirePendingCandidate(...)`
- `markCandidateApplied(...)`
  - 供 `knowledge`、`classics` 应用候选结果

本轮不先暴露：

- `rewriteQuery(...)`
- `streamAnswer(...)`
- `reject(...)`

理由：

- 当前外域没有调用点
- 先按最小现状收敛，不预开无消费协议

## 4. 协议与数据结构变更

### 4.1 总原则

- 外域不再使用 `ai.application.*` 的 `Command/Result`
- 外域不再使用 `ai.domain.*` 的 `entity/valueobject/service/repository`
- 所有跨域协议改成 `facade/request/response/dto`
- 改名不是纯机械替换；凡是从 `application/domain` 迁到 `facade` 的协议类，必须同时移动到对应 `facade/request`、`facade/response`、`facade/dto` 正确包位，避免只改类名不改归属
- 协议对象统一使用不可变风格：
  - `@Getter`
  - `@Builder`
  - `private constructor`

### 4.2 需要新增的 Facade 协议

#### 报表摘要

- `AiReportSummaryFacadeRequest`
  - `periodStart`
  - `periodEnd`
  - `bucketType`

- `AiReportSummaryFacadeResponse`
  - `periodStart`
  - `periodEnd`
  - `invocationCount`
  - `succeededInvocationCount`
  - `failedInvocationCount`
  - `avgLatencyMs`
  - `totalCostAmount`
  - `topCapabilities`

- `AiTopCapabilityFacadeDto`
  - `capability`
  - `invocationCount`

#### Discovery AI

- `DiscoveryAiFacadeRequest`
  - `serviceId`
  - `serviceRole`
  - `modelId`
  - `modelName`
  - `promptVersionId`
  - `requestId`
  - `traceId`
  - `promptMessagesJson`
  - `promptVariablesJson`
  - `promptHash`
  - `inputPayloadJson`
  - `outputSchemaJson`
  - `stream`
  - `forceJson`
  - `locale`

- `DiscoveryAiFacadeResponse`
  - `callId`
  - `candidateId`
  - `status`
  - `capability`
  - `resultFormat`
  - `resultPayload`
  - `errorType`
  - `errorMessage`

#### Knowledge 抽取

- `KnowledgeAiExtractionFacadeRequest`
  - `taskType`
  - `scopeType`
  - `scopeJson`
  - `sourceContentType`
  - `sourceContentId`
  - `requestedBy`
  - `serviceId`
  - `serviceRole`
  - `modelId`
  - `modelName`
  - `promptVersionId`
  - `requestId`
  - `traceId`
  - `promptMessagesJson`
  - `promptVariablesJson`
  - `promptHash`
  - `inputPayloadJson`
  - `outputSchemaJson`
  - `forceJson`
  - `locale`

- `KnowledgeAiExtractionFacadeResponse`
  - `callId`
  - `candidateId`
  - `status`
  - `capability`
  - `resultFormat`
  - `resultPayload`
  - `errorType`
  - `errorMessage`

#### 批任务

- `CreateAiBatchJobFacadeRequest`
  - `scope`
  - `capability`
  - `contentType`
  - `totalCount`
  - `failureSummaryJson`

- `AiBatchJobFacadeResponse`
  - `batchId`
  - `scope`
  - `capability`
  - `contentType`
  - `status`
  - `totalCount`
  - `successCount`
  - `failedCount`
  - `cancelledCount`
  - `failureSummaryJson`
  - `requestedAt`
  - `cancelledAt`
  - `completedAt`

- `AiBatchJobActionFacadeResponse`
  - 用于 `createBatchJob(...)`
  - 最小字段只需 `batchId`

- `AiBatchJobFailureFacadeRequest`
  - `batchId`
  - `failureSummaryJson`

#### 调用记录与候选

- `GetAiCallRecordFacadeRequest`
  - `callId`

- `GetAiCandidateFacadeRequest`
  - `candidateId`

- `RequirePendingAiCandidateFacadeRequest`
  - `candidateId`
  - `contentType`
  - `contentId`
  - `capability`

- `MarkAiCandidateAppliedFacadeRequest`
  - `candidateId`
  - `resultFormat`
  - `resultPayload`
  - `appliedAt`

- `AiCallRecordFacadeDto`
  - `callId`
  - `batchId`
  - `scope`
  - `capability`
  - `contentType`
  - `contentId`
  - `objectId`
  - `serviceId`
  - `serviceRole`
  - `modelId`
  - `modelName`
  - `promptVersionId`
  - `requestId`
  - `traceId`
  - `status`
  - `streamUsed`
  - `streamCompleted`
  - `fallbackUsed`
  - `errorType`
  - `errorMessage`
  - `warningsJson`
  - `requestedAt`
  - `completedAt`
  - `usage`

- `AiUsageSnapshotFacadeDto`
  - `promptTokens`
  - `completionTokens`
  - `totalTokens`
  - `latencyMs`
  - `costAmount`
  - `currency`

- `AiCandidateFacadeDto`
  - `candidateId`
  - `callId`
  - `batchId`
  - `capability`
  - `contentType`
  - `contentId`
  - `objectId`
  - `resultFormat`
  - `resultPayload`
  - `status`
  - `promptVersionId`
  - `modelName`
  - `errorType`
  - `errorMessage`
  - `requestedAt`
  - `appliedAt`

### 4.3 旧结构到新结构映射

- `AiReportApplicationService.summary(...)`
  - 改为 `AiFacade.summary(AiReportSummaryFacadeRequest)`
- `AiReportSummaryResult`
  - 改为 `AiReportSummaryFacadeResponse`

- `DiscoveryAiDomainService`
  - 改为 `AiFacade`
- `DiscoveryAiRequest`
  - 改为 `DiscoveryAiFacadeRequest`
- `DiscoveryAiResult`
  - 改为 `DiscoveryAiFacadeResponse`

- `KnowledgeAiExtractionDomainService`
  - 改为 `AiFacade`
- `KnowledgeAiExtractionRequest`
  - 改为 `KnowledgeAiExtractionFacadeRequest`
- `KnowledgeAiExtractionResult`
  - 改为 `KnowledgeAiExtractionFacadeResponse`

- `AiBatchJobApplicationService`
  - 改为 `AiFacade`
- `AiBatchJobCreateCommand`
  - 改为 `CreateAiBatchJobFacadeRequest`
- `AiBatchJobResult`
  - 改为 `AiBatchJobFacadeResponse`

- `AiInvocationRepository.getCallRecord(...)`
  - 改为 `AiFacade.getCallRecord(...)`
- `AiInvocationRepository.getCandidate(...)`
  - 改为 `AiFacade.getCandidate(...)`
- `AiCallRecord`
  - 改为 `AiCallRecordFacadeDto`
- `AiCandidate`
  - 改为 `AiCandidateFacadeDto`

- `AiCandidateDomainService.requirePendingForApply(...)`
  - 改为 `AiFacade.requirePendingCandidate(...)`
- `AiCandidateApplyCheck`
  - 改为 `RequirePendingAiCandidateFacadeRequest`
- `AiCandidateDomainService.markApplied(...)`
  - 改为 `AiFacade.markCandidateApplied(...)`

## 5. 涉及文件

### 5.1 新增文件

#### provider: ai-facade 模块

- `biz/ai/kuzhambu-ai-facade/pom.xml`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/AiFacade.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/AiReportSummaryFacadeRequest.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/response/AiReportSummaryFacadeResponse.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/dto/AiTopCapabilityFacadeDto.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/DiscoveryAiFacadeRequest.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/response/DiscoveryAiFacadeResponse.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/KnowledgeAiExtractionFacadeRequest.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/response/KnowledgeAiExtractionFacadeResponse.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/CreateAiBatchJobFacadeRequest.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/AiBatchJobFailureFacadeRequest.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/response/AiBatchJobFacadeResponse.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/response/AiBatchJobActionFacadeResponse.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/GetAiCallRecordFacadeRequest.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/GetAiCandidateFacadeRequest.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/RequirePendingAiCandidateFacadeRequest.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/MarkAiCandidateAppliedFacadeRequest.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/dto/AiCallRecordFacadeDto.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/dto/AiCandidateFacadeDto.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/dto/AiUsageSnapshotFacadeDto.java`

#### provider: ai-application 实现

- `biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/facade/impl/AiFacadeImpl.java`
- `biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/facade/assembler/AiFacadeAssembler.java`

### 5.2 需要修改的 provider 文件

- `biz/ai/pom.xml`
  - 新增 `kuzhambu-ai-facade` module
- `biz/ai/kuzhambu-ai-application/pom.xml`
  - 新增对 `kuzhambu-ai-facade` 的依赖

#### 可能被 facade impl 直接复用的现有 provider 文件

- [AiReportApplicationServiceImpl.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/report/service/impl/AiReportApplicationServiceImpl.java)
- [DiscoveryAiApplicationServiceImpl.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/discovery/service/impl/DiscoveryAiApplicationServiceImpl.java)
- [KnowledgeAiExtractionApplicationServiceImpl.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/knowledge/service/impl/KnowledgeAiExtractionApplicationServiceImpl.java)
- [AiBatchJobApplicationServiceImpl.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/batch/service/impl/AiBatchJobApplicationServiceImpl.java)
- [AiCandidateDomainService.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/service/AiCandidateDomainService.java)
- [AiInvocationRepository.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/repository/AiInvocationRepository.java)

### 5.3 需要修改的 consumer 文件

#### operations

- [kuzhambu-operations-application/pom.xml](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/operations/kuzhambu-operations-application/pom.xml)
- [DefaultOperationsReportMetricsGateway.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGateway.java)

#### discovery

- [kuzhambu-discovery-application/pom.xml](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/pom.xml)
- [QaApplicationServiceImpl.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImpl.java)
- [QueryUnderstandingApplicationServiceImpl.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/QueryUnderstandingApplicationServiceImpl.java)

#### classics

- [kuzhambu-classics-application/pom.xml](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/classics/kuzhambu-classics-application/pom.xml)
- [ClassicsContentApplicationServiceImpl.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java)

#### knowledge

- [kuzhambu-knowledge-application/pom.xml](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/pom.xml)
- [KnowledgeGraphExtractionApplicationServiceImpl.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/service/impl/KnowledgeGraphExtractionApplicationServiceImpl.java)
- [KnowledgeGraphCandidateApplySupport.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/support/KnowledgeGraphCandidateApplySupport.java)

### 5.4 需要修改的测试文件

#### operations

- `biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGatewayTest.java`
  - 新增

#### discovery

- `biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/QueryUnderstandingApplicationServiceImplTest.java`
- `biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImplTest.java`
- `biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImplAdminReadTest.java`

#### classics

- `biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceImplTest.java`
- `biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`

#### knowledge

- `biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/graph/KnowledgeGraphExtractionApplicationServiceTest.java`
- `biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/graph/KnowledgeGraphCandidateApplySupportTest.java`

#### ai provider

- `biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/facade/impl/AiFacadeImplTest.java`
- `biz/ai/kuzhambu-ai-facade/src/test/java/com/thundax/kuzhambu/ai/facade/architecture/AiFacadeArchitectureTest.java`

## 6. 执行顺序

### T1. 新增 ai-facade 模块骨架

文件数：3

- `biz/ai/pom.xml`
- `biz/ai/kuzhambu-ai-facade/pom.xml`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/AiFacade.java`

处理动作：

- 把 `kuzhambu-ai-facade` 加入 `biz/ai` reactor
- 建立 `AiFacade` 空协议入口

### T2. 落报表摘要协议

文件数：4

- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/AiReportSummaryFacadeRequest.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/response/AiReportSummaryFacadeResponse.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/dto/AiTopCapabilityFacadeDto.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/AiFacade.java`

处理动作：

- 给 `AiFacade` 增加 `summary(...)`
- 明确报表摘要 request/response/dto 字段

### T3. 落 discovery 协议

文件数：3

- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/DiscoveryAiFacadeRequest.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/response/DiscoveryAiFacadeResponse.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/AiFacade.java`

处理动作：

- 给 `AiFacade` 增加 `understandDiscoveryQuery(...)`
- 给 `AiFacade` 增加 `generateDiscoveryAnswer(...)`

### T4. 落 knowledge 抽取协议

文件数：3

- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/KnowledgeAiExtractionFacadeRequest.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/response/KnowledgeAiExtractionFacadeResponse.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/AiFacade.java`

处理动作：

- 给 `AiFacade` 增加 `extractKnowledgeRelations(...)`
- 给 `AiFacade` 增加 `extractKnowledgeGraph(...)`
- 给 `AiFacade` 增加 `extractKnowledgeLineage(...)`

### T5. 落批任务协议

文件数：5

- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/CreateAiBatchJobFacadeRequest.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/AiBatchJobFailureFacadeRequest.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/response/AiBatchJobFacadeResponse.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/response/AiBatchJobActionFacadeResponse.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/AiFacade.java`

处理动作：

- 给 `AiFacade` 增加 `createBatchJob(...)`
- 给 `AiFacade` 增加 `getBatchJob(...)`
- 给 `AiFacade` 增加 `canDispatchNextBatchUnit(...)`
- 给 `AiFacade` 增加 `recordBatchSuccess(...)`
- 给 `AiFacade` 增加 `recordBatchFailure(...)`
- 给 `AiFacade` 增加 `cancelBatchJob(...)`

### T6. 落调用记录与候选协议

文件数：5

- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/GetAiCallRecordFacadeRequest.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/GetAiCandidateFacadeRequest.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/RequirePendingAiCandidateFacadeRequest.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/MarkAiCandidateAppliedFacadeRequest.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/AiFacade.java`

处理动作：

- 给 `AiFacade` 增加 `getCallRecord(...)`
- 给 `AiFacade` 增加 `getCandidate(...)`
- 给 `AiFacade` 增加 `requirePendingCandidate(...)`
- 给 `AiFacade` 增加 `markCandidateApplied(...)`

### T7. 落调用记录与候选 dto

文件数：3

- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/dto/AiCallRecordFacadeDto.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/dto/AiCandidateFacadeDto.java`
- `biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/dto/AiUsageSnapshotFacadeDto.java`

处理动作：

- 把 `AiCallRecord` / `AiCandidate` / `AiUsageSnapshot` 跨域字段固定为 facade dto

### T8. provider 侧接入 facade 模块

文件数：2

- `biz/ai/kuzhambu-ai-application/pom.xml`
- `biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/facade/impl/AiFacadeImpl.java`

处理动作：

- `ai-application` 依赖 `ai-facade`
- 新增 `AiFacadeImpl` 骨架

### T9. provider 侧补协议装配

文件数：4

- `biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/facade/impl/AiFacadeImpl.java`
- `biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/facade/assembler/AiFacadeAssembler.java`
- `biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/report/service/impl/AiReportApplicationServiceImpl.java`
- `biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/batch/service/impl/AiBatchJobApplicationServiceImpl.java`

处理动作：

- 把报表摘要和批任务能力接到 `AiFacadeImpl`
- 在 assembler 中明确 `AiReportSummaryResult`、`AiBatchJobResult` 到 facade response 的字段映射

### T10. provider 侧补 discovery/knowledge 调用桥接

文件数：4

- `biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/facade/impl/AiFacadeImpl.java`
- `biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/facade/assembler/AiFacadeAssembler.java`
- `biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/discovery/service/impl/DiscoveryAiApplicationServiceImpl.java`
- `biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/knowledge/service/impl/KnowledgeAiExtractionApplicationServiceImpl.java`

处理动作：

- 把 discovery AI 和 knowledge 抽取能力接到 `AiFacadeImpl`
- 明确 request/response 字段映射

### T11. provider 侧补候选与调用记录桥接

文件数：4

- `biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/facade/impl/AiFacadeImpl.java`
- `biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/facade/assembler/AiFacadeAssembler.java`
- `biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/service/AiCandidateDomainService.java`
- `biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/repository/AiInvocationRepository.java`

处理动作：

- 把 `getCallRecord/getCandidate/requirePendingCandidate/markCandidateApplied` 接到 facade
- 在 assembler 中明确 `AiCallRecord`、`AiCandidate` 到 facade dto 的字段映射

### T12. 迁移 operations consumer

文件数：2

- `biz/operations/kuzhambu-operations-application/pom.xml`
- `biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGateway.java`

处理动作：

- `operations-application` 删除 `kuzhambu-ai-application`
- 改依赖 `kuzhambu-ai-facade`
- `AiReportApplicationService.summary(...) -> AiFacade.summary(...)`

### T13. 迁移 discovery consumer

文件数：3

- `biz/discovery/kuzhambu-discovery-application/pom.xml`
- `biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImpl.java`
- `biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/QueryUnderstandingApplicationServiceImpl.java`

处理动作：

- `discovery-application` 删除 `kuzhambu-ai-domain`
- 改依赖 `kuzhambu-ai-facade`
- `DiscoveryAiDomainService`、`DiscoveryAiRequest`、`DiscoveryAiResult` 全部切到 facade

### T14. 迁移 classics consumer

文件数：2

- `biz/classics/kuzhambu-classics-application/pom.xml`
- `biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`

处理动作：

- `classics-application` 删除 `kuzhambu-ai-domain`
- 改依赖 `kuzhambu-ai-facade`
- `AiCandidateDomainService`、`AiCandidateApplyCheck`、`AiCandidate` 全部切到 facade

### T15. 迁移 knowledge consumer 第一段

文件数：2

- `biz/knowledge/kuzhambu-knowledge-application/pom.xml`
- `biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/service/impl/KnowledgeGraphExtractionApplicationServiceImpl.java`

处理动作：

- `knowledge-application` 删除 `kuzhambu-ai-domain`
- `knowledge-application` 删除 `kuzhambu-ai-application`
- 改依赖 `kuzhambu-ai-facade`
- 先把 `AiBatchJobApplicationService`、`KnowledgeAiExtractionDomainService` 切到 facade

### T16. 迁移 knowledge consumer 第二段

文件数：2

- `biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/service/impl/KnowledgeGraphExtractionApplicationServiceImpl.java`
- `biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/support/KnowledgeGraphCandidateApplySupport.java`

处理动作：

- 把 `AiInvocationRepository`、`AiCandidateDomainService`、`AiCandidate` 切到 facade
- 把 `AiCallRecord`、`AiCandidate` 读取改成 facade dto

### T17. ai-facade 测试与门禁

文件数：2

- `biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/facade/impl/AiFacadeImplTest.java`
- `biz/ai/kuzhambu-ai-facade/src/test/java/com/thundax/kuzhambu/ai/facade/architecture/AiFacadeArchitectureTest.java`

处理动作：

- 补 `AiFacadeImpl` 单测
- 补 `ai-facade` 架构测试

### T18. 外域测试收口

文件数：5

- `biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/QueryUnderstandingApplicationServiceImplTest.java`
- `biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImplTest.java`
- `biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`
- `biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/graph/KnowledgeGraphExtractionApplicationServiceTest.java`
- `biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGatewayTest.java`

处理动作：

- 全部改为 mock 或 fake `AiFacade`

### T19. ai 相关 allowlist 与文档收口

文件数：2-4

- `common/kuzhambu-common-test/src/main/java/com/thundax/kuzhambu/common/test/architecture/RepositoryArchitectureRuleSupport.java`
- `common/kuzhambu-common-test/src/main/java/com/thundax/kuzhambu/common/test/architecture/CrossApplicationIsolationArchitectureRuleSupport.java`
- `docs/00-governance/SERVERS-ARCHITECTURE.md`
- `docs/00-governance/SERVERS-ARCHITECTURE-RULES.md`

处理动作：

- 收缩 ai 相关 legacy allowlist
- 如 facade 口径无新增，不动治理文档；如新增规则，再精确同步

## 7. 验收点

- `kuzhambu-ai-facade` 模块存在并可编译
- `operations/discovery/classics/knowledge` 不再直接依赖 `kuzhambu-ai-application`
- `operations/discovery/classics/knowledge` 不再直接依赖 `kuzhambu-ai-domain`
- 外域生产代码中不再出现 `com.thundax.kuzhambu.ai.application.*` 或 `com.thundax.kuzhambu.ai.domain.*` 导入
- `AiFacadeImpl` 覆盖当前已有外域调用能力
- 相关模块最小测试和架构测试通过

## 8. 非目标

- 不重做 `ai-interface` 内部 admin API
- 不在本轮统一重构 `ai` 域内部 `application/domain` 结构
- 不提前暴露当前没有消费点的 streaming/reject 能力
