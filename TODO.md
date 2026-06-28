# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

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
