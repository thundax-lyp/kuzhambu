# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `12 Admin web backup restore controls`：补齐备份恢复页面控件和操作
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-BACKUP-RESTORE-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/backup-restore/backup-restore-page.tsx`、`kuzhambu-apps/admin-web/src/pages/operations/backup-restore/backup-restore-page.css`、`kuzhambu-apps/admin-web/src/pages/operations/backup-restore/backup-restore-page.test.tsx`
    - 处理动作：新增自动备份展示、恢复模式筛选、演练按钮、真实恢复按钮、写阻断列和详情抽屉字段。
    - 验收点：`AUTO` 显示、系统自动发起人、`DRILL` 筛选、演练/恢复按钮调用和详情抽屉新增字段均有页面测试。
    - 重要度：9/10

- [ ] `13 Operations restore drill scripts`：补齐恢复演练脚本模式
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-BACKUP-RESTORE-CLOSURE.md`
    - 范围对象：`deploy/scripts/restore-business-data.sh`、`deploy/scripts/backup-lib.sh`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/support/DefaultOperationsBackupScriptExecutor.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/backup/support/DefaultOperationsBackupScriptExecutorTest.java`
    - 处理动作：新增 `RESTORE_MODE=DRILL` 演练校验分支并让 executor 传入演练环境变量。
    - 验收点：脚本语法检查通过，演练模式校验备份与存储产物但不导入 SQL 或覆盖存储。
    - 重要度：9/10

- [ ] `14 Branch sync main`：实现完成后同步 main 分支代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`main` 分支、`codex/operations-backup-restore-runbook` 分支、`/Volumes/storage/workspace/kuzhambu-operations-backup-restore-runbook`
    - 处理动作：在收口前把最新 `main` 合入当前 worktree 分支并处理冲突。
    - 验收点：当前分支包含最新 `main`，且无未解决冲突。
    - 重要度：8/10

- [ ] `15 Operations backup restore final verification`：同步 main 后运行最终验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-BACKUP-RESTORE-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/`、`kuzhambu-apps/`、`deploy/scripts/`
    - 处理动作：在合入最新 `main` 后运行后端、前端和脚本验证。
    - 验收点：Maven formatter/static/test、admin-web format/lint/test 和备份恢复脚本语法检查均通过。
    - 重要度：10/10

- [ ] `16 Operations backup restore coverage closure`：更新覆盖矩阵并清理 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-BACKUP-RESTORE-CLOSURE.md`
    - 范围对象：`docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-OPERATIONS-BACKUP-RESTORE-CLOSURE.md`
    - 处理动作：将“备份与恢复”覆盖状态更新为已完成，并在 PR 收口前删除临时 RUNBOOK。
    - 验收点：Coverage 记录启动自动备份、每日 2:00 自动备份、恢复写阻断、真实恢复/演练台账和前端展示，RUNBOOK 无残留。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
