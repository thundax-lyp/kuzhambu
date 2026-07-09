# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `7. 格式和验证`：运行后端、前端和文档检查
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-EDIT-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/`、`kuzhambu-apps/`、`TODO.md`、`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-EDIT-CLOSURE.md`
    - 处理动作：运行 RUNBOOK 指定的 Maven、pnpm 和 `git diff --check` 验证。
    - 验收点：相关格式、静态检查、单测和构建通过，或失败原因明确记录为非本任务既有问题。
    - 重要度：10/10
- [ ] `8. 同步 main 并最终收口`：同步 main 分支代码、清理 RUNBOOK 和 TODO
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-EDIT-CLOSURE.md`
    - 范围对象：`main`、`feat/classics-sancai-edit-closure`、`TODO.md`、`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-EDIT-CLOSURE.md`
    - 处理动作：同步最新 `main` 到当前分支，确认无冲突后复核验证状态，删除已完成 TODO 项并删除 RUNBOOK。
    - 验收点：当前分支包含最新 `main`，Implementation Coverage 已更新，已完成任务不留在 `TODO.md`，RUNBOOK 被清理。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
