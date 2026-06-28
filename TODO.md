# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `knowledge-facade-tag-binding-provider`：落 classics tag binding provider 映射
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/facade/impl/KnowledgeFacadeImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/facade/assembler/KnowledgeFacadeAssembler.java`
    - 处理动作：实现 tag resolve、content tag ref sync、content tag ref remove 的 provider 映射
    - 验收点：provider 内已完成 `Tag -> KnowledgeTagFacadeResponse` 和 request string 到 domain enum 的映射
    - 重要度：9/10

- [ ] `operations-knowledge-facade`：operations 改用 KnowledgeFacade
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGateway.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGatewayTest.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/pom.xml`
    - 处理动作：将 operations 报表 summary 读取改为通过 `KnowledgeFacade`
    - 验收点：`operations-application` 不再依赖 `kuzhambu-knowledge-application`，并已消费 `KnowledgeSummaryFacadeResponse`
    - 重要度：8/10

- [ ] `discovery-knowledge-facade`：discovery 改用 KnowledgeFacade
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/DiscoveryKnowledgeEnhancementProvider.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/QueryUnderstandingApplicationServiceImplTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/pom.xml`
    - 处理动作：将 discovery 的 taxonomy 增强读取改为通过 `KnowledgeFacade`
    - 验收点：`discovery-application` 不再依赖 `kuzhambu-knowledge-application`，并已消费 synonym、tag hint、entity hints facade response
    - 重要度：8/10

- [ ] `classics-knowledge-facade`：classics 改用 KnowledgeFacade
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsTagBindingSupport.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceImplTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/pom.xml`
    - 处理动作：将 classics 的 tag resolve 与 tag ref 同步改为通过 `KnowledgeFacade`
    - 验收点：`classics-application` 不再依赖 `kuzhambu-knowledge-domain`，并已消费 `KnowledgeTagFacadeResponse` 与 tag ref request
    - 重要度：8/10

- [ ] `knowledge-facade-test`：补齐 knowledge facade 架构与 provider 测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/test/java/com/thundax/kuzhambu/knowledge/facade/KnowledgeFacadeArchitectureTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/facade/impl/KnowledgeFacadeImplTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/report/service/impl/KnowledgeReportApplicationServiceImplTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/KnowledgeTaxonomyReadApplicationServiceImplTest.java`
    - 处理动作：补齐 knowledge facade 模块架构测试和 provider 映射测试
    - 验收点：facade 协议和 provider 映射都有测试覆盖，现有 report/taxonomy 语义测试继续通过
    - 重要度：7/10

- [ ] `knowledge-facade-allowlist`：收缩 knowledge 跨域白名单
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/common/kuzhambu-common-test/src/main/java/com/thundax/kuzhambu/common/test/architecture/RepositoryArchitectureRuleSupport.java`、`kuzhambu-servers/common/kuzhambu-common-test/src/main/java/com/thundax/kuzhambu/common/test/architecture/CrossApplicationIsolationArchitectureRuleSupport.java`、`docs/00-governance/SERVERS-ARCHITECTURE-RULES.md`
    - 处理动作：删除 `operations/discovery -> knowledge-application` 和 `classics -> knowledge-domain` 的 legacy allowlist 并同步文档
    - 验收点：相关模块在新规则下仍可通过架构测试，治理文档已同步到当前口径
    - 重要度：7/10

## 待审阅任务项

## 待讨论项
