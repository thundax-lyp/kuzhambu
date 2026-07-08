# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `Operations cleanup closeout`：同步 main 并完成文档与临时文件收口
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-OPERATIONS-CLEANUP-TARGET-TYPES.md`
    - 范围对象：`git main sync`、`TODO.md`、`docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-OPERATIONS-CLEANUP-TARGET-TYPES.md`
    - 处理动作：收口前同步最新 `main`，更新 Implementation Coverage，删除已完成 RUNBOOK，并按完成状态清理 TODO。
    - 验收点：当前分支包含最新 `main` 代码，`清理任务` 覆盖状态为 `已完成`，RUNBOOK 已删除，TODO 删除已完成项或仅保留真实剩余项。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
