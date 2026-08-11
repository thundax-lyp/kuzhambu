# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 当前任务项按数字编号顺序执行；不得跳过前置契约任务直接做后续调用方或重命名任务。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `Operations application / scheduler and construction`：移除无参自动备份与 Command/Query 构造位置 allowlist
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-03-OPERATIONS.md`
    - 范围对象：`BackupApplicationService.java`、`BackupApplicationServiceImpl.java`、`OperationsBackupScheduler.java`、`BackupSchedulerApplicationService.java`、`OperationsCleanupScheduler.java`、`OperationsCleanupSchedulerFacadeAssembler.java`、`OperationsReportAdminController.java`、`OperationsReportInterfaceAssembler.java`、`OperationsApplicationArchitectureTest.java`、`OperationsBackupSchedulerTest.java`、`BackupApplicationServiceImplTest.java`，均位于 `kuzhambu-servers/biz/operations/`。
    - 处理动作：建立保留 AUTO 语义的调度专用入口，并删除 application 边界与构造位置 allowlist。
    - 验收点：自动备份仍写入 AUTO/skipped 记录，且 `METHOD_SHAPE` 与 `COMMAND_QUERY_CONSTRUCTION` allowlist 无匹配。
    - 重要度：10/10

- [ ] `Operations interface / assembler nullness`：移除 task、report、restore Assembler 的 null contract
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-03-OPERATIONS.md`
    - 范围对象：`OperationsTaskInterfaceAssembler.java`、`OperationsReportInterfaceAssembler.java`、`OperationsRestoreInterfaceAssembler.java`、`OperationsTaskAdminController.java`、`OperationsReportAdminController.java`、`OperationsRestoreAdminController.java`、`OperationsApplicationArchitectureTest.java`，均位于 `kuzhambu-servers/biz/operations/`。
    - 处理动作：为所有 public Assembler 方法建立 non-null contract，并由 Controller 保留既有空 result 返回行为。
    - 验收点：不存在 `COMMAND_QUERY_ASSEMBLER_NULL_RETURN:` allowlist，且相关 application/interface 测试通过。
    - 重要度：9/10

- [ ] `Operations domain / repository naming`：规范 cleanup、health Repository 方法名并同步调用点
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-03-OPERATIONS.md`
    - 范围对象：`CleanupJobRepository.java`、`CleanupJobRepositoryImpl.java`、`HealthAlertRepository.java`、`HealthAlertRepositoryImpl.java`、`OperationsHealthAlertStrategy.java`、`OperationsDomainArchitectureTest.java`、`HealthAlertRepositoryImplTest.java`、`OperationsHealthAlertStrategyTest.java`、`OperationsDashboardApplicationServiceImplTest.java`、`CleanupApplicationServiceImplTest.java`，均位于 `kuzhambu-servers/biz/operations/`。
    - 处理动作：将 `deleteItemsByJobId` 重命名为 `deleteByJobId`，并将 `getOpenBySource` 重命名为 `findOpenBySource`。
    - 验收点：旧方法名在 `biz/operations` 无匹配，domain/application/infra 测试通过。
    - 重要度：8/10

- [ ] `Operations interface / response annotations`：为 backup、cleanup、dashboard Response 补齐模型注解
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-03-OPERATIONS.md`
    - 范围对象：`OperationsBackupDetailResponse.java`、`OperationsBackupExecuteResponse.java`、`OperationsBackupPageResponse.java`、`OperationsCleanupDetailResponse.java`、`OperationsCleanupExecuteResponse.java`、`OperationsCleanupPageResponse.java`、`OperationsDashboardOverviewResponse.java`、`OperationsInterfaceArchitectureTest.java`，均位于 `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/`。
    - 处理动作：为列出的 Response 及 dashboard 内嵌 Response 补齐 required model annotations。
    - 验收点：对应 Response allowance 已删除，且 interface 架构测试通过。
    - 重要度：7/10

- [ ] `Operations interface / health-report response annotations`：为 health、report Response 补齐模型注解
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-03-OPERATIONS.md`
    - 范围对象：`OperationsHealthAlertPageResponse.java`、`OperationsHealthAlertSummaryResponse.java`、`OperationsHealthPageResponse.java`、`OperationsHealthSummaryResponse.java`、`OperationsHealthTrendResponse.java`、`OperationsReportDetailResponse.java`、`OperationsReportGenerateResponse.java`、`OperationsReportPageResponse.java`、`OperationsInterfaceArchitectureTest.java`，均位于 `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/`。
    - 处理动作：为列出的 health/report Response 补齐 required model annotations。
    - 验收点：对应 Response allowance 已删除，且 interface 架构测试通过。
    - 重要度：7/10

- [ ] `Operations interface / restore-task response annotations`：完成 Response allowlist 清零
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-03-OPERATIONS.md`
    - 范围对象：`OperationsRestoreDetailResponse.java`、`OperationsRestoreExecuteResponse.java`、`OperationsRestorePageResponse.java`、`OperationsTaskDetailResponse.java`、`OperationsTaskPageResponse.java`、`OperationsInterfaceArchitectureTest.java`，均位于 `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/`。
    - 处理动作：补齐 restore/task Response 模型注解并删除剩余 Response allowlist。
    - 验收点：`legacyResponseAnnotationAllowances` 无匹配，且 interface 架构测试通过。
    - 重要度：7/10

- [ ] `Operations interface / controller verbs`：迁移 Controller action method 与 path，并更新 report E2E mock
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-03-OPERATIONS.md`
    - 范围对象：`OperationsBackupAdminController.java`、`OperationsCleanupAdminController.java`、`OperationsDashboardAdminController.java`、`OperationsHealthAdminController.java`、`OperationsHealthAlertAdminController.java`、`OperationsReportAdminController.java`、`OperationsRestoreAdminController.java`、`OperationsTaskAdminController.java`、`OperationsInterfaceArchitectureTest.java`（均位于 `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/`）；`kuzhambu-apps/admin-web/e2e/operations/report/report.spec.ts`。
    - 处理动作：按 RUNBOOK 映射替换不在白名单内的 Controller action method/path，并同步 report E2E mock。
    - 验收点：Controller verb allowlist 已删除，interface 架构测试和 report E2E 通过。
    - 重要度：9/10

- [ ] `Operations allowlist closure`：完成全量验证并清理任务现场
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-03-OPERATIONS.md`、`docs/00-governance/TODO-RULES.md`
    - 范围对象：`OperationsInterfaceArchitectureTest.java`、`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-03-OPERATIONS.md`、`TODO.md`
    - 处理动作：清零最后的 Assembler class allowlist，运行 RUNBOOK Verification，并在通过后删除 RUNBOOK 与本 TODO 项。
    - 验收点：验证命令全部通过、allowlist 搜索无输出、RUNBOOK 已删除且 `TODO.md` 不保留已完成项。
    - 重要度：10/10

## 待讨论项
