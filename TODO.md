# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `10 Operations Implementation Coverage`：更新实现覆盖说明
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-OPERATIONS-RESTORE-ORCHESTRATION.md`
    - 范围对象：`docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：同步 Operations health/alert/restore/task 自动恢复闭环的实现覆盖状态。
    - 验收点：Implementation Coverage 准确反映本次 health/alert/restore/task 闭环能力和剩余缺口。
    - 重要度：8/10

- [ ] `11 RUNBOOK cleanup`：清理临时 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-OPERATIONS-RESTORE-ORCHESTRATION.md`
    - 处理动作：在实现、验证、main 同步和 Implementation Coverage 完成后删除临时 RUNBOOK。
    - 验收点：RUNBOOK 文件从工作区移除，TODO 仅保留真实未关闭事项。
    - 重要度：7/10

## 待审阅任务项

## 待讨论项
