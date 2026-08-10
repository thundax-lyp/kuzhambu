# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 当前任务项按数字编号顺序执行；不得跳过前置契约任务直接做后续调用方或重命名任务。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `ARCHUNIT-SYSTEM-AUTH-04-09`：清理 System 认证与审计 allowlist 清理现场
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`；`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-04-SYSTEM-AUTH-AUDIT.md`
    - 范围对象：`TODO.md`；`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-04-SYSTEM-AUTH-AUDIT.md`
    - 处理动作：在本批 allowlist 清零且验证通过后，删除临时 RUNBOOK 并移除本批已完成 TODO。
    - 验收点：`TODO.md` 不再保留本批已完成任务，临时 RUNBOOK 已删除或按治理要求迁移后删除。
    - 重要度：7/10

## 待讨论项
