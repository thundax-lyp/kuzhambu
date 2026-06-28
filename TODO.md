# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

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
