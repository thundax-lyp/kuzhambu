# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `05 Operations restore persistence`：补齐恢复新增字段持久化读写
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-BACKUP-RESTORE-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/restore/persistence/dataobject/RestoreDO.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/restore/persistence/assembler/RestorePersistenceAssembler.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/restore/persistence/mapper/RestoreMapper.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/restore/repository/impl/RestoreRepositoryImpl.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/test/java/com/thundax/kuzhambu/operations/infra/restore/repository/impl/RestoreRepositoryImplTest.java`
    - 处理动作：让恢复新增字段通过 DO、assembler、mapper 和 repository 完整 insert、update、select、page。
    - 验收点：新增字段可插入、可更新、可查询，并支持 `restoreMode=DRILL` 分页过滤。
    - 重要度：10/10

- [ ] `06 Operations restore application models`：补齐恢复 application 命令、查询和结果字段
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-BACKUP-RESTORE-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/command/OperationsRestoreExecuteCommand.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/query/OperationsRestorePageQuery.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/result/OperationsRestoreExecuteResult.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/result/OperationsRestorePageResult.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/result/OperationsRestoreDetailResult.java`
    - 处理动作：为恢复 command/query/result 增加 `restoreMode` 和写阻断时间字段。
    - 验收点：application 模型能表达真实恢复、恢复演练、写阻断开启时间和释放时间。
    - 重要度：9/10

- [ ] `07 Operations restore application flow`：补齐真实恢复/演练和写阻断编排
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-BACKUP-RESTORE-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/service/impl/RestoreApplicationServiceImpl.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/support/OperationsRestoreWriteBlocker.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/support/OperationsBackupExecutionGuard.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/support/OperationsBackupScriptExecutor.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/restore/service/impl/RestoreApplicationServiceImplTest.java`
    - 处理动作：按 `REAL`/`DRILL` 分支执行真实恢复或演练，并在恢复全流程中开启、释放和记录写阻断。
    - 验收点：真实恢复、恢复演练、脚本失败释放阻断、阻断开启失败不执行脚本、恢复期间自动备份无法进入 guard 均有测试覆盖。
    - 重要度：10/10

- [ ] `08 Common web restore write block`：补齐恢复期间 Web 写入阻断
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-BACKUP-RESTORE-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/common/kuzhambu-common-web/src/main/java/com/thundax/kuzhambu/common/web/restore/RestoreWriteBlockState.java`、`kuzhambu-servers/common/kuzhambu-common-web/src/main/java/com/thundax/kuzhambu/common/web/restore/RestoreWriteBlockFilter.java`、`kuzhambu-servers/common/kuzhambu-common-web/src/main/java/com/thundax/kuzhambu/common/web/restore/RestoreWriteBlockProperties.java`、`kuzhambu-servers/common/kuzhambu-common-web/src/test/java/com/thundax/kuzhambu/common/web/restore/RestoreWriteBlockFilterTest.java`
    - 处理动作：新增 Web 层阻断状态、配置和 filter，阻断恢复期间业务写方法并放行必要路径。
    - 验收点：业务写入被 423 阻断，Operations 查询和恢复执行路径放行，阻断响应结构稳定。
    - 重要度：10/10

- [ ] `09 Operations restore interface requests`：补齐恢复接口请求模式字段
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-BACKUP-RESTORE-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/request/OperationsRestoreExecuteRequest.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/request/OperationsRestorePageRequest.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/assembler/OperationsRestoreInterfaceAssembler.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/OperationsRestoreAdminController.java`
    - 处理动作：让恢复执行和分页请求透传 `restoreMode` 到 application command/query。
    - 验收点：restore execute/page 路径不变，request 的 `restoreMode` 能进入 application。
    - 重要度：9/10

- [ ] `10 Operations restore interface responses`：补齐恢复响应字段与接口契约测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-BACKUP-RESTORE-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/response/OperationsRestoreExecuteResponse.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/response/OperationsRestorePageResponse.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/response/OperationsRestoreDetailResponse.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/OperationsRestoreContractTest.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/OperationsRestoreAdminControllerTest.java`
    - 处理动作：让恢复响应透出 `restoreMode`、`writeBlockStartedAt` 和 `writeBlockReleasedAt` 并更新契约测试。
    - 验收点：请求字段、分页字段、响应字段和 controller 透传均有测试覆盖。
    - 重要度：9/10

- [ ] `11 Admin web backup restore service`：补齐前端备份恢复类型和接口契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-BACKUP-RESTORE-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/backup-restore/backup-restore-types.ts`、`kuzhambu-apps/admin-web/src/pages/operations/backup-restore/backup-restore-service.ts`、`kuzhambu-apps/admin-web/src/pages/operations/backup-restore/backup-restore-service-contract.test.ts`
    - 处理动作：补齐 `AUTO`、`restoreMode`、写阻断时间字段和 restore execute/page 请求契约。
    - 验收点：恢复演练请求 body、恢复模式筛选 body、恢复响应新增字段解析均有前端契约测试。
    - 重要度：9/10

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
