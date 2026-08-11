# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 当前任务项按数字编号顺序执行；不得跳过前置契约任务直接做后续调用方或重命名任务。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `Operations allowlist closure`：完成全量验证并清理任务现场
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-03-OPERATIONS.md`、`docs/00-governance/TODO-RULES.md`
    - 范围对象：`OperationsInterfaceArchitectureTest.java`、`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-03-OPERATIONS.md`、`TODO.md`
    - 处理动作：清零最后的 Assembler class allowlist，运行 RUNBOOK Verification，并在通过后删除 RUNBOOK 与本 TODO 项。
    - 验收点：验证命令全部通过、allowlist 搜索无输出、RUNBOOK 已删除且 `TODO.md` 不保留已完成项。
    - 重要度：10/10

## 待讨论项
