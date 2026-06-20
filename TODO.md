# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `classics share cleanup`：清理分享快照任务现场
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-CLASSICS-SHARE-SNAPSHOT.md`
    - 范围对象：`TODO.md`、`docs/30-designs/RUNBOOK-CLASSICS-SHARE-SNAPSHOT.md`、本地临时调试产物、当前工作区
    - 处理动作：PR 收口前清理已完成 TODO、删除或收窄临时 RUNBOOK、移除临时调试产物并确认工作区不混入无关修改
    - 验收点：`git status` 只剩预期改动，临时 RUNBOOK 已按治理规则处理，TODO 不保留已完成项
    - 重要度：8/10

## 待讨论项
