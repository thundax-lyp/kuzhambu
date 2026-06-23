# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/pom.xml,docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md,TODO.md`：重挂 Knowledge 审计任务依赖
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-COMMON-AUDIT-REFACTOR.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/pom.xml`、`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`、`TODO.md`
    - 处理动作：让 Knowledge 精修审计接入改为依赖 `common-audit`
    - 验收点：Knowledge 审计后续任务不再指向 `system-application`
    - 重要度：7/10

## 待审阅任务项

## 待讨论项
