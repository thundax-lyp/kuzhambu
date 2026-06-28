# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `ai-facade-allowlist`：收缩 ai 相关架构 allowlist 并按需同步治理文档
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/common/kuzhambu-common-test/src/main/java/com/thundax/kuzhambu/common/test/architecture/RepositoryArchitectureRuleSupport.java`、`kuzhambu-servers/common/kuzhambu-common-test/src/main/java/com/thundax/kuzhambu/common/test/architecture/CrossApplicationIsolationArchitectureRuleSupport.java`、`docs/00-governance/SERVERS-ARCHITECTURE.md`、`docs/00-governance/SERVERS-ARCHITECTURE-RULES.md`
    - 处理动作：收缩 ai 相关 legacy allowlist，并在治理口径变化时同步文档。
    - 验收点：ai 相关跨域直接依赖 allowlist 被删除或收窄；如果治理文档更新，内容与 `AiFacade` 统一跨域边界口径一致。
    - 重要度：7/10

## 待审阅任务项

## 待讨论项
