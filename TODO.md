# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

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
