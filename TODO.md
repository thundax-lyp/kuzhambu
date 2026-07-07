# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `main sync before validation`：最终验证前同步 main 分支代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`feat/classics-batch-candidate-governance` 分支
    - 处理动作：在最终验证前同步最新 `origin/main` 到当前特性分支并解决冲突。
    - 验收点：当前分支包含最新 `origin/main`，同步冲突已解决且未混入无关修改。
    - 重要度：10/10

- [ ] `classics batch candidate final validation`：执行同步 main 后的最终验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-BATCH-CANDIDATE-GOVERNANCE.md`
    - 范围对象：`kuzhambu-servers/`、`kuzhambu-apps/admin-web/`
    - 处理动作：在同步最新 `origin/main` 后运行 RUNBOOK 指定的后端 formatter、静态检查、测试和前端 format、lint、test、build。
    - 验收点：后端 Maven 检查和测试通过，前端 format/lint/test/build 通过，失败时收窄到具体未完成任务。
    - 重要度：10/10

- [ ] `classics implementation coverage closeout`：更新 Classics 完成状态并清理 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-BATCH-CANDIDATE-GOVERNANCE.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-CLASSICS-BATCH-CANDIDATE-GOVERNANCE.md`、`TODO.md`
    - 处理动作：在能力完成且验证通过后更新 Implementation Coverage 为已完成，并删除已无继续价值的 RUNBOOK。
    - 验收点：覆盖清单中“跨内容批量候选治理”改为已完成，B2 改为已完成，RUNBOOK 被清理，相关 TODO 删除或收窄。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
