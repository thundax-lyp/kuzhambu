# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 当前任务项按数字编号顺序执行；不得跳过前置契约任务直接做后续调用方或重命名任务。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `classics-allowlist-06-cleanup`：13 执行验证并清理现场
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-06-CLASSICS-CONTENT.md`
    - 范围对象：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-06-CLASSICS-CONTENT.md`、`TODO.md`
    - 处理动作：运行 RUNBOOK 规定的 formatter、测试、静态检查，并清理已完成 RUNBOOK、已完成 TODO 和临时产物。
    - 验收点：RUNBOOK Verification 全部通过，RUNBOOK 文件被删除，`TODO.md` 不保留已完成任务，`git status` 无非任务范围文件。
    - 重要度：10/10

## 待讨论项
