# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

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
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/QueryUnderstandingApplicationServiceImplTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImplTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/pom.xml`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/graph/KnowledgeGraphExtractionApplicationServiceTest.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGatewayTest.java`
    - 处理动作：把外域测试中的 AI 依赖替身统一切到 `AiFacade`，并删除仅为测试保留的 `ai-domain/ai-application` test scope 依赖。
    - 验收点：上述测试文件全部只 mock 或 fake `AiFacade`，不再 mock `ai-application` 或 `ai-domain` 类型；`kuzhambu-knowledge-application/pom.xml` 删除 `ai-domain/ai-application` 的 test scope 依赖；如 `DefaultOperationsReportMetricsGatewayTest.java` 不存在则新增。
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
