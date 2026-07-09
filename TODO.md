# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `08-main-coverage-runbook-closure`：同步 main 并完成 Knowledge 验收文档收口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-RUNTIME-VALIDATION-CLOSURE.md`
    - 范围对象：`docs/40-readiness/KNOWLEDGE-RUNTIME-SMOKE-EVIDENCE.md`、`docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-KNOWLEDGE-RUNTIME-VALIDATION-CLOSURE.md`、`TODO.md`
    - 处理动作：同步最新 `origin/main`，确认验证结果仍有效后更新 Implementation Coverage 为已完成、清理 RUNBOOK 并收窄或删除已完成 TODO
    - 验收点：分支基于最新 `origin/main`，`KNOWLEDGE-IMPLEMENTATION-COVERAGE.md` 的运行时验证为 `已完成`，RUNBOOK 已删除，TODO 仅保留未完成事项
    - 重要度：10/10


## 待审阅任务项

## 待讨论项
