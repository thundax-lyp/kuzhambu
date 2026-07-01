# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `kuzhambu-apps/`：按 format -> lint -> build -> test 顺序完成前端收口验证
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/40-readiness/PR-WORKFLOW.md`、`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/`
    - 处理动作：按前端 formatter、lint、build、test 顺序执行 apps 全域验证
    - 验收点：`kuzhambu-apps/` 按 `format -> lint -> build -> test` 顺序通过，且结果可写入 PR 验证记录
    - 重要度：10/10

- [ ] `kuzhambu-workers/`：按 format -> lint -> test 顺序完成 workers 收口验证
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/40-readiness/PR-WORKFLOW.md`、`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-workers/`
    - 处理动作：按 Ruff format、Ruff check、pytest 顺序执行 workers 全域验证
    - 验收点：`kuzhambu-workers/` 按 `format -> lint -> test` 顺序通过，且结果可写入 PR 验证记录
    - 重要度：10/10

- [ ] `RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`：在本轮需求关闭后清理执行手册
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 范围对象：`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 处理动作：在功能、测试、文档和验证全部完成后删除本轮 RUNBOOK
    - 验收点：PR 收口前该 RUNBOOK 已清理，且 TODO 仅保留真实剩余未完成任务
    - 重要度：8/10

## 待讨论项
