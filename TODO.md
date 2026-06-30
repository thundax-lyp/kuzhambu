# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `Implementation Coverage/RUNBOOK`：执行全域验证并完成文档收口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-WORKERS-CLASSICS-TEXT-CLOSURE.md`、`docs/00-governance/TODO-RULES.md`、`docs/40-readiness/PR-WORKFLOW.md`
    - 范围对象：`docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`、`docs/40-readiness/WORKERS-IMPLEMENTATION-COVERAGE.md`、`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-AI-WORKERS-CLASSICS-TEXT-CLOSURE.md`
    - 处理动作：执行 `kuzhambu-servers`、`kuzhambu-apps`、`kuzhambu-workers` 全域 `format/lint/test/build`，更新 Implementation Coverage，并在任务完成后清理 RUNBOOK
    - 验收点：完成 servers/apps/workers 全域验证，Coverage 文档更新到真实闭环状态，已完成且无剩余价值的 RUNBOOK 被删除
    - 重要度：10/10

## 待讨论项
