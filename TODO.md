# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `Implementation Coverage`：同步 AI 和 Classics 实现覆盖状态
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-AI-STREAMING-CANDIDATES.md`、`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`、`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：记录 Classics 三才视觉流式候选闭环的实现覆盖状态和剩余风险。
    - 验收点：coverage 文档包含 stream 展示、候选生成、失败重试、main 同步和验证结果的最新口径。
    - 重要度：8/10

- [ ] `RUNBOOK 清理`：完成闭环后删除临时 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/00-governance/DOCUMENT-RULES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-CLASSICS-AI-STREAMING-CANDIDATES.md`
    - 处理动作：在实现、文档同步、验证和 main 同步完成后删除临时 RUNBOOK。
    - 验收点：PR 收口前不再保留本 RUNBOOK，且稳定文档已承载必要口径。
    - 重要度：8/10

## 待审阅任务项

## 待讨论项
