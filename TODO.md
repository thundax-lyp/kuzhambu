# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `ai-facade/report-protocol`：定义 AI 报表摘要 facade 协议
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/AiFacade.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/AiReportSummaryFacadeRequest.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/response/AiReportSummaryFacadeResponse.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/dto/AiTopCapabilityFacadeDto.java`
    - 处理动作：为 `summary(...)` 建立报表摘要 request/response/dto 协议。
    - 验收点：`AiReportSummaryFacadeRequest` 包含 `periodStart/periodEnd/bucketType`，`AiReportSummaryFacadeResponse` 包含 `periodStart/periodEnd/invocationCount/succeededInvocationCount/failedInvocationCount/avgLatencyMs/totalCostAmount/topCapabilities`，`AiTopCapabilityFacadeDto` 包含 `capability/invocationCount`。
    - 重要度：8/10

- [ ] `ai-facade/discovery-protocol`：定义 discovery AI facade 协议
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/AiFacade.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/DiscoveryAiFacadeRequest.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/response/DiscoveryAiFacadeResponse.java`
    - 处理动作：为 `understandDiscoveryQuery(...)` 和 `generateDiscoveryAnswer(...)` 建立 discovery 协议。
    - 验收点：`DiscoveryAiFacadeRequest` 包含 `serviceId/serviceRole/modelId/modelName/promptVersionId/requestId/traceId/promptMessagesJson/promptVariablesJson/promptHash/inputPayloadJson/outputSchemaJson/stream/forceJson/locale`，`DiscoveryAiFacadeResponse` 包含 `callId/candidateId/status/capability/resultFormat/resultPayload/errorType/errorMessage`。
    - 重要度：9/10

- [ ] `ai-facade/knowledge-protocol`：定义 knowledge 抽取 facade 协议
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/AiFacade.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/KnowledgeAiExtractionFacadeRequest.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/response/KnowledgeAiExtractionFacadeResponse.java`
    - 处理动作：为 `extractKnowledgeRelations/Graph/Lineage` 建立 knowledge 抽取协议。
    - 验收点：`KnowledgeAiExtractionFacadeRequest` 包含 `taskType/scopeType/scopeJson/sourceContentType/sourceContentId/requestedBy/serviceId/serviceRole/modelId/modelName/promptVersionId/requestId/traceId/promptMessagesJson/promptVariablesJson/promptHash/inputPayloadJson/outputSchemaJson/forceJson/locale`，`KnowledgeAiExtractionFacadeResponse` 包含 `callId/candidateId/status/capability/resultFormat/resultPayload/errorType/errorMessage`。
    - 重要度：9/10

- [ ] `ai-facade/batch-protocol`：定义 AI 批任务 facade 协议
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/AiFacade.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/CreateAiBatchJobFacadeRequest.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/AiBatchJobFailureFacadeRequest.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/response/AiBatchJobFacadeResponse.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/response/AiBatchJobActionFacadeResponse.java`
    - 处理动作：为 batch job 创建、读取、状态回写和取消建立 facade 协议。
    - 验收点：`CreateAiBatchJobFacadeRequest` 包含 `scope/capability/contentType/totalCount/failureSummaryJson`，`AiBatchJobFailureFacadeRequest` 包含 `batchId/failureSummaryJson`，`AiBatchJobFacadeResponse` 包含 `batchId/scope/capability/contentType/status/totalCount/successCount/failedCount/cancelledCount/failureSummaryJson/requestedAt/cancelledAt/completedAt`，`AiBatchJobActionFacadeResponse` 至少包含 `batchId`。
    - 重要度：9/10

- [ ] `ai-facade/invocation-protocol`：定义 AI 调用记录与候选请求协议
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/AiFacade.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/GetAiCallRecordFacadeRequest.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/GetAiCandidateFacadeRequest.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/RequirePendingAiCandidateFacadeRequest.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/MarkAiCandidateAppliedFacadeRequest.java`
    - 处理动作：为调用记录读取和候选应用建立 facade 请求协议。
    - 验收点：`GetAiCallRecordFacadeRequest` 包含 `callId`，`GetAiCandidateFacadeRequest` 包含 `candidateId`，`RequirePendingAiCandidateFacadeRequest` 包含 `candidateId/contentType/contentId/capability`，`MarkAiCandidateAppliedFacadeRequest` 包含 `candidateId/resultFormat/resultPayload/appliedAt`。
    - 重要度：9/10

- [ ] `ai-facade/invocation-dto`：定义 AI 调用记录与候选 dto
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/dto/AiCallRecordFacadeDto.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/dto/AiCandidateFacadeDto.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/dto/AiUsageSnapshotFacadeDto.java`
    - 处理动作：把调用记录、候选和 usage 快照固定为 facade dto。
    - 验收点：`AiCallRecordFacadeDto` 包含 `callId/batchId/scope/capability/contentType/contentId/objectId/serviceId/serviceRole/modelId/modelName/promptVersionId/requestId/traceId/status/streamUsed/streamCompleted/fallbackUsed/errorType/errorMessage/warningsJson/requestedAt/completedAt/usage`，`AiCandidateFacadeDto` 包含 `candidateId/callId/batchId/capability/contentType/contentId/objectId/resultFormat/resultPayload/status/promptVersionId/modelName/errorType/errorMessage/requestedAt/appliedAt`，`AiUsageSnapshotFacadeDto` 包含 `promptTokens/completionTokens/totalTokens/latencyMs/costAmount/currency`。
    - 重要度：9/10

- [ ] `ai-application/facade-entry`：接入 ai-facade 模块并建立 facade 实现骨架
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-application/pom.xml`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/facade/impl/AiFacadeImpl.java`
    - 处理动作：让 `ai-application` 依赖 `ai-facade` 并新增 `AiFacadeImpl` 骨架。
    - 验收点：`ai-application` 可以编译通过 `AiFacadeImpl`，且不把 facade 实现放错到 `domain/interface/infra` 包。
    - 重要度：10/10

- [ ] `ai-application/report-batch-bridge`：把报表摘要与批任务能力接入 AiFacade
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/facade/impl/AiFacadeImpl.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/facade/assembler/AiFacadeAssembler.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/report/service/impl/AiReportApplicationServiceImpl.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/batch/service/impl/AiBatchJobApplicationServiceImpl.java`
    - 处理动作：把报表摘要和批任务 provider 能力桥接到 `AiFacadeImpl`。
    - 验收点：`AiFacadeImpl` 可通过 assembler 完成 `AiReportSummaryResult -> AiReportSummaryFacadeResponse`、`AiBatchJobResult -> AiBatchJobFacadeResponse` 字段映射，并暴露 `summary/createBatchJob/getBatchJob/canDispatchNextBatchUnit/recordBatchSuccess/recordBatchFailure/cancelBatchJob`。
    - 重要度：9/10

- [ ] `ai-application/discovery-knowledge-bridge`：把 discovery AI 与 knowledge 抽取能力接入 AiFacade
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/facade/impl/AiFacadeImpl.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/facade/assembler/AiFacadeAssembler.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/discovery/service/impl/DiscoveryAiApplicationServiceImpl.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/knowledge/service/impl/KnowledgeAiExtractionApplicationServiceImpl.java`
    - 处理动作：把 discovery AI 与 knowledge 抽取 provider 能力桥接到 `AiFacadeImpl`。
    - 验收点：`AiFacadeImpl` 暴露 `understandDiscoveryQuery/generateDiscoveryAnswer/extractKnowledgeRelations/extractKnowledgeGraph/extractKnowledgeLineage`，并完成 `DiscoveryAiFacadeRequest/Response` 与 `KnowledgeAiExtractionFacadeRequest/Response` 的字段映射。
    - 重要度：9/10

- [ ] `ai-application/invocation-bridge`：把候选与调用记录能力接入 AiFacade
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/facade/impl/AiFacadeImpl.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/facade/assembler/AiFacadeAssembler.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/service/AiCandidateDomainService.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/repository/AiInvocationRepository.java`
    - 处理动作：把调用记录读取和候选应用能力桥接到 `AiFacadeImpl`。
    - 验收点：`AiFacadeImpl` 暴露 `getCallRecord/getCandidate/requirePendingCandidate/markCandidateApplied`，并通过 assembler 把 `AiCallRecord/AiCandidate` 映射为 facade dto 字段。
    - 重要度：9/10

- [ ] `operations-ai-facade`：迁移 operations 报表指标读取到 AiFacade
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/pom.xml`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGateway.java`
    - 处理动作：把 `operations-application` 从 `AiReportApplicationService` 切到 `AiFacade.summary(...)`。
    - 验收点：`operations-application` 删除 `kuzhambu-ai-application` 依赖，新增 `kuzhambu-ai-facade` 依赖，生产代码不再导入 `ai.application.report.*`。
    - 重要度：8/10

- [ ] `discovery-ai-facade`：迁移 discovery 查询理解与问答到 AiFacade
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/pom.xml`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImpl.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/QueryUnderstandingApplicationServiceImpl.java`
    - 处理动作：把 `DiscoveryAiDomainService` 和 `DiscoveryAiRequest/Result` 全部切到 `AiFacade` 协议。
    - 验收点：`discovery-application` 删除 `kuzhambu-ai-domain` 依赖，新增 `kuzhambu-ai-facade` 依赖，生产代码不再导入 `ai.domain.discovery.*`。
    - 重要度：9/10

- [ ] `classics-ai-facade`：迁移 classics AI 候选应用到 AiFacade
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/pom.xml`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
    - 处理动作：把 `AiCandidateDomainService/AiCandidateApplyCheck/AiCandidate` 全部切到 `AiFacade` 协议。
    - 验收点：`classics-application` 删除 `kuzhambu-ai-domain` 依赖，新增 `kuzhambu-ai-facade` 依赖，生产代码不再导入 `ai.domain.invocation.*`。
    - 重要度：9/10

- [ ] `knowledge-ai-facade-stage1`：迁移 knowledge 图谱抽取与批任务到 AiFacade
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/pom.xml`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/service/impl/KnowledgeGraphExtractionApplicationServiceImpl.java`
    - 处理动作：先把 `AiBatchJobApplicationService` 和 `KnowledgeAiExtractionDomainService` 切到 `AiFacade`。
    - 验收点：`knowledge-application` 删除 `kuzhambu-ai-domain` 与 `kuzhambu-ai-application` 依赖，新增 `kuzhambu-ai-facade` 依赖，且 `KnowledgeGraphExtractionApplicationServiceImpl` 不再导入 `ai.application.batch.*` 与 `ai.domain.knowledge.*`。
    - 重要度：10/10

- [ ] `knowledge-ai-facade-stage2`：迁移 knowledge 候选与调用记录读取到 AiFacade
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/service/impl/KnowledgeGraphExtractionApplicationServiceImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/support/KnowledgeGraphCandidateApplySupport.java`
    - 处理动作：把 `AiInvocationRepository/AiCandidateDomainService/AiCallRecord/AiCandidate` 切到 `AiFacade` 和 facade dto。
    - 验收点：`KnowledgeGraphExtractionApplicationServiceImpl` 与 `KnowledgeGraphCandidateApplySupport` 不再导入 `ai.domain.invocation.*`，候选与调用记录状态同步只通过 `AiFacade` 完成。
    - 重要度：10/10

- [ ] `ai-facade-test`：补齐 AiFacade provider 单测与架构测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/facade/impl/AiFacadeImplTest.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/test/java/com/thundax/kuzhambu/ai/facade/architecture/AiFacadeArchitectureTest.java`
    - 处理动作：为 `AiFacadeImpl` 和 `ai-facade` 模块补单测与架构测试。
    - 验收点：`AiFacadeImplTest` 覆盖 facade 到 provider 的主要字段映射与调用路径，`AiFacadeArchitectureTest` 能门禁 facade 包位和命名。
    - 重要度：8/10

- [ ] `consumer-ai-facade-test`：收口外域 AI facade 测试替身
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/QueryUnderstandingApplicationServiceImplTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImplTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/graph/KnowledgeGraphExtractionApplicationServiceTest.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGatewayTest.java`
    - 处理动作：把外域测试中的 AI 依赖替身统一切到 `AiFacade`。
    - 验收点：上述测试文件全部只 mock 或 fake `AiFacade`，不再 mock `ai-application` 或 `ai-domain` 类型；如 `DefaultOperationsReportMetricsGatewayTest.java` 不存在则新增。
    - 重要度：8/10

- [ ] `ai-facade-allowlist`：收缩 ai 相关架构 allowlist 并按需同步治理文档
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/common/kuzhambu-common-test/src/main/java/com/thundax/kuzhambu/common/test/architecture/RepositoryArchitectureRuleSupport.java`、`kuzhambu-servers/common/kuzhambu-common-test/src/main/java/com/thundax/kuzhambu/common/test/architecture/CrossApplicationIsolationArchitectureRuleSupport.java`、`docs/00-governance/SERVERS-ARCHITECTURE.md`、`docs/00-governance/SERVERS-ARCHITECTURE-RULES.md`
    - 处理动作：收缩 ai 相关 legacy allowlist，并在治理口径变化时同步文档。
    - 验收点：ai 相关跨域直接依赖 allowlist 被删除或收窄；如果治理文档更新，内容与 `AiFacade` 统一跨域边界口径一致。
    - 重要度：7/10

## 待审阅任务项

## 待讨论项
