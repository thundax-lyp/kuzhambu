# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `discovery-docs`：T11 更新设计与覆盖文档
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-INDEX-SYNC.md`
    - 范围对象：`docs/30-designs/DISCOVERY-DESIGN.md`、`docs/40-readiness/DISCOVERY-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：同步 RocketMQ 增量同步、`afterCommit` 发送、`currentVersionNo` 幂等、删除态计划清理和 `rebuild` 兜底口径。
    - 验收点：设计文档和覆盖文档都能准确反映本轮能力边界、完成度和剩余风险。
    - 重要度：7/10

## 待讨论项
