# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `16 Operations backup restore coverage closure`：更新覆盖矩阵并清理 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-BACKUP-RESTORE-CLOSURE.md`
    - 范围对象：`docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-OPERATIONS-BACKUP-RESTORE-CLOSURE.md`
    - 处理动作：将“备份与恢复”覆盖状态更新为已完成，并在 PR 收口前删除临时 RUNBOOK。
    - 验收点：Coverage 记录启动自动备份、每日 2:00 自动备份、恢复写阻断、真实恢复/演练台账和前端展示，RUNBOOK 无残留。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
