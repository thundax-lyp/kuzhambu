# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `T06 Branch sync`：同步 main 分支最新代码
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DELETE-SHARE-RISK-CLOSURE.md`
    - 范围对象：`codex/classics-delete-share-risk-runbook` worktree、`main` 分支
    - 处理动作：实现完成后同步 `main` 最新代码并解决冲突
    - 验收点：当前分支包含 `main` 最新代码，且没有未解决冲突
    - 重要度：9/10

- [ ] `T07 Verification`：完成后端和前端验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DELETE-SHARE-RISK-CLOSURE.md`
    - 范围对象：`kuzhambu-servers`、`kuzhambu-apps`
    - 处理动作：同步 `main` 后运行 RUNBOOK 中 Maven 和 npm 格式、静态检查、测试命令
    - 验收点：后端 Maven 检查与测试通过，前端 npm format、lint、test 通过
    - 重要度：9/10

- [ ] `T08 Classics implementation coverage`：更新 Classics 实现覆盖口径
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-CLASSICS-DELETE-SHARE-RISK-CLOSURE.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：把 Classics 删除后分享闭环标记为已完成，并同步 Admin Web / Portal Web 删除占位口径
    - 验收点：Coverage 中不再保留“删除后分享目标状态同步、风险态重算未完成”的描述
    - 重要度：10/10

- [ ] `T09 Runbook and TODO closure`：清理临时 RUNBOOK 并收窄 TODO
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-CLASSICS-DELETE-SHARE-RISK-CLOSURE.md`
    - 范围对象：`docs/30-designs/RUNBOOK-CLASSICS-DELETE-SHARE-RISK-CLOSURE.md`、`TODO.md`
    - 处理动作：删除临时 RUNBOOK，并按实际完成情况删除或收窄 TODO
    - 验收点：临时 RUNBOOK 已清理，`TODO.md` 不保留已完成任务
    - 重要度：10/10

## 待讨论项
