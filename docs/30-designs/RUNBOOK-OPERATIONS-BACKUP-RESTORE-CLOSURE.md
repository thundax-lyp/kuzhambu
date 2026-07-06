# Operations Backup Restore Closure Runbook

## Purpose

本 RUNBOOK 用于把 Operations 的“备份与恢复”从部分完成推进到已完成。

完成态只认四件事：

- 系统启动后自动创建一条 `AUTO` 备份。
- 系统每天凌晨 2:00 通过 cron 创建一条 `AUTO` 备份。
- 恢复执行期间，admin 与 portal 的业务写入请求被 Web 层真实阻断。
- 恢复演练作为一等恢复模式进入数据库、接口、台账和 admin 页面。

## Source Documents

- `docs/10-requirements/OPERATIONS-REQUIREMENTS.md`
- `docs/30-designs/OPERATIONS-DESIGN.md`
- `docs/30-designs/OPERATIONS-BACKUP-RESTORE-SPECIAL-DESIGN.md`
- `docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md`
- `docs/00-governance/SERVERS-ARCHITECTURE.md`

## Decisions

- 自动备份使用 `BackupType.AUTO`。
- 自动备份的 `operations_backup.requester_user_id` 固定为 `NULL`，admin 页面展示为“系统自动”。
- 每日自动备份使用 cron，默认 `0 0 2 * * ?`，即每天凌晨 2:00。
- 备份和恢复使用单 admin-starter 实例内互斥；多实例分布式锁不在本轮。
- 恢复模式使用 `restore_mode` 字段区分，枚举值为 `REAL` 和 `DRILL`。
- 恢复状态继续使用 `RUNNING`、`SUCCEEDED`、`FAILED`；真实恢复成功和演练成功由 `restore_mode` 区分。
- 恢复演练不覆盖生产业务数据，但必须完成备份文件校验、checksum 校验、`PRE_RESTORE` 快照创建、写入阻断开启与释放、演练结果记录。
- 写入阻断放在 Web 层，早于业务 controller 和 application 写入动作生效。

## Data Structure Changes

### operations_backup

不新增字段。

现有字段语义调整：

- `backup_type varchar(16) NOT NULL`：允许值包含 `AUTO`、`MANUAL`、`PRE_RESTORE`。
- `requester_user_id bigint DEFAULT NULL`：`AUTO` 备份固定为 `NULL`；`MANUAL` 和 `PRE_RESTORE` 保留真实管理员 ID。
- `failure_reason varchar(1024) DEFAULT NULL`：自动备份因互斥跳过、脚本失败或超时时写入原因。

### operations_restore

在 `db/schema/operations.sql` 的 `operations_restore` 表中新增字段：

```sql
`restore_mode` varchar(16) NOT NULL DEFAULT 'REAL' COMMENT '恢复模式：REAL 真实恢复，DRILL 恢复演练',
`write_block_started_at` datetime(3) DEFAULT NULL COMMENT '写入阻断开启时间',
`write_block_released_at` datetime(3) DEFAULT NULL COMMENT '写入阻断释放时间',
```

字段位置：

- `restore_mode` 放在 `pre_restore_backup_id` 之后、`restore_status` 之前。
- `write_block_started_at` 放在 `write_block_enabled` 之后、`failure_reason` 之前。
- `write_block_released_at` 放在 `write_block_started_at` 之后、`failure_reason` 之前。

新增索引：

```sql
KEY `idx_operations_restore_mode_status` (`restore_mode`, `restore_status`, `started_at`)
```

### Java Fields

`RestoreRecord` 新增字段：

- `private String restoreMode;`
- `private Date writeBlockStartedAt;`
- `private Date writeBlockReleasedAt;`

`RestoreDO` 新增字段：

- `private String restoreMode;`
- `private Date writeBlockStartedAt;`
- `private Date writeBlockReleasedAt;`

`OperationsRestoreExecuteCommand` 新增字段：

- `private String restoreMode;`

`OperationsRestorePageQuery` 新增字段：

- `private String restoreMode;`

`OperationsRestoreExecuteResult`、`OperationsRestorePageResult`、`OperationsRestoreDetailResult` 新增字段：

- `private String restoreMode;`
- `private Date writeBlockStartedAt;`
- `private Date writeBlockReleasedAt;`

`OperationsRestoreExecuteRequest` 新增字段：

- `private String restoreMode;`

`OperationsRestorePageRequest` 新增字段：

- `private String restoreMode;`

`OperationsRestoreExecuteResponse`、`OperationsRestorePageResponse`、`OperationsRestoreDetailResponse` 新增字段：

- `private String restoreMode;`
- `private Date writeBlockStartedAt;`
- `private Date writeBlockReleasedAt;`

### TypeScript Fields

`OperationsRestoreRecord` 新增字段：

```ts
restoreMode?: "REAL" | "DRILL" | string | null;
writeBlockStartedAt?: string | null;
writeBlockReleasedAt?: string | null;
```

`RestoreExecuteCommand` 新增字段：

```ts
restoreMode: "REAL" | "DRILL";
```

`RestoreLedgerQuery` 新增字段：

```ts
restoreMode?: "REAL" | "DRILL" | string | null;
```

## Task 1: Backup Entry And Mutual Exclusion

目标：自动备份和手动备份共用 application 备份闭环，并与恢复互斥。

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/service/BackupApplicationService.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/service/impl/BackupApplicationServiceImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/command/OperationsBackupExecuteCommand.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/support/OperationsBackupExecutionGuard.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/backup/service/impl/BackupApplicationServiceImplTest.java`

实现要求：

- `BackupApplicationService` 增加内部方法 `executeAutoBackup()`。
- `executeAutoBackup()` 创建 `BackupType.AUTO` 记录，`requesterUserId` 为 `null`。
- `execute(OperationsBackupExecuteCommand command)` 继续创建 `BackupType.MANUAL`。
- 新增 `OperationsBackupExecutionGuard`，方法为 `tryEnterBackup()`、`tryEnterRestore()`、`exit()`。
- 同一时间只允许一个备份或恢复动作运行。
- 自动备份遇到 guard 占用时写入 `AUTO` + `FAILED` 记录，`failureReason` 为 `Operations backup skipped because another backup or restore is running.`。
- 手动备份遇到 guard 占用时返回业务失败，不调用脚本执行器。

验收：

- 测试覆盖 `AUTO` 备份成功落库。
- 测试覆盖 `AUTO` 备份 requester 为空。
- 测试覆盖自动备份失败落库。
- 测试覆盖 guard 占用时脚本不执行。

## Task 2: Backup Scheduler

目标：启动自动备份和每日凌晨 2:00 自动备份由 Operations application 触发。

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/support/OperationsBackupScheduleProperties.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/support/OperationsBackupScheduler.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/backup/support/OperationsBackupSchedulerTest.java`

配置字段：

- `kuzhambu.operations.backup.schedule.enabled`：默认 `true`。
- `kuzhambu.operations.backup.schedule.startup-enabled`：默认 `true`。
- `kuzhambu.operations.backup.schedule.daily-cron`：默认 `0 0 2 * * ?`。

实现要求：

- `OperationsBackupScheduler` 监听 `ApplicationReadyEvent`。
- `startup-enabled=true` 时，ready 后调用 `executeAutoBackup()`。
- `@Scheduled(cron = "${kuzhambu.operations.backup.schedule.daily-cron:0 0 2 * * ?}")` 触发每日自动备份。
- `schedule.enabled=false` 时，启动备份和每日 cron 都不调用 `executeAutoBackup()`。

验收：

- 测试覆盖应用 ready 后触发自动备份。
- 测试覆盖 `schedule.enabled=false` 不触发。
- 测试覆盖 `startup-enabled=false` 不触发启动备份。
- 测试覆盖每日调度方法调用 `executeAutoBackup()`。

## Task 3: Starter And Environment Configuration

目标：本地样例、部署样例和 starter 配置使用同一套自动备份变量。

文件：

- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/resources/application.yml`
- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/test/java/com/thundax/kuzhambu/starter/admin/AdminStarterArchitectureTest.java`
- `.env.example`
- `deploy/.env.example`

配置字段：

- `KUZHAMBU_OPERATIONS_BACKUP_SCHEDULE_ENABLED=true`
- `KUZHAMBU_OPERATIONS_BACKUP_STARTUP_ENABLED=true`
- `KUZHAMBU_OPERATIONS_BACKUP_DAILY_CRON=0 0 2 * * ?`
- `KUZHAMBU_BACKUP_ROOT_PATH=/backup/kuzhambu`

实现要求：

- `application.yml` 映射 `schedule.enabled` 到 `KUZHAMBU_OPERATIONS_BACKUP_SCHEDULE_ENABLED`。
- `application.yml` 映射 `schedule.startup-enabled` 到 `KUZHAMBU_OPERATIONS_BACKUP_STARTUP_ENABLED`。
- `application.yml` 映射 `schedule.daily-cron` 到 `KUZHAMBU_OPERATIONS_BACKUP_DAILY_CRON`。
- `.env.example` 与 `deploy/.env.example` 使用完全相同的变量名。
- `AdminStarterArchitectureTest` 保留 `@EnableScheduling` 验证。

验收：

- 配置文件能明确看出默认每天凌晨 2:00 自动备份。
- 新增变量不包含真实密钥。

## Task 4: Restore Schema And Domain

目标：恢复演练和写入阻断时间进入 schema 与领域模型。

文件：

- `db/schema/operations.sql`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/restore/model/entity/RestoreRecord.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/restore/model/enums/RestoreMode.java`

实现要求：

- `operations_restore` 新增 `restore_mode`、`write_block_started_at`、`write_block_released_at`。
- `RestoreRecord` 新增 `restoreMode`、`writeBlockStartedAt`、`writeBlockReleasedAt`。
- `RestoreMode` 枚举包含 `REAL`、`DRILL`、`value()`、`from(String value)`。
- `RestoreMode.from()` 对未知值抛领域异常。

验收：

- schema 字段名、默认值和 Java 字段名一一对应。
- `restore_mode` 默认值为 `REAL`。

## Task 5: Restore Persistence

目标：新增恢复字段完整通过 infra 层读写。

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/restore/persistence/dataobject/RestoreDO.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/restore/persistence/assembler/RestorePersistenceAssembler.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/restore/persistence/mapper/RestoreMapper.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/restore/repository/impl/RestoreRepositoryImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/test/java/com/thundax/kuzhambu/operations/infra/restore/repository/impl/RestoreRepositoryImplTest.java`

实现要求：

- `RestoreDO` 新增 `restoreMode`、`writeBlockStartedAt`、`writeBlockReleasedAt`。
- `RestorePersistenceAssembler` 在 DO 和 domain 之间双向映射三个字段。
- `RestoreMapper` 的 insert、update、select、page 查询包含三个字段。
- `RestoreRepositoryImpl.page(...)` 增加 `restoreMode` 查询条件。

验收：

- 测试覆盖新增字段 insert 后可读。
- 测试覆盖新增字段 update 后可读。
- 测试覆盖按 `restoreMode=DRILL` 分页过滤。

## Task 6: Restore Application Models

目标：application command、query 和 result 准确承载恢复模式与写入阻断时间。

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/command/OperationsRestoreExecuteCommand.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/query/OperationsRestorePageQuery.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/result/OperationsRestoreExecuteResult.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/result/OperationsRestorePageResult.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/result/OperationsRestoreDetailResult.java`

实现要求：

- `OperationsRestoreExecuteCommand` 新增 `restoreMode`。
- `OperationsRestorePageQuery` 新增 `restoreMode`。
- 三个 result 新增 `restoreMode`、`writeBlockStartedAt`、`writeBlockReleasedAt`。
- `restoreMode` 为空时由 service 按 `REAL` 处理，模型层不写默认逻辑。

验收：

- result 构造、builder 或 Lombok 字段与 assembler 调用保持一致。
- page query 能表达全部模式、真实恢复和恢复演练。

## Task 7: Restore Application Flow

目标：真实恢复和恢复演练共用恢复台账，并准确开启、释放、记录写入阻断。

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/service/impl/RestoreApplicationServiceImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/support/OperationsRestoreWriteBlocker.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/support/OperationsBackupExecutionGuard.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/support/OperationsBackupScriptExecutor.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/restore/service/impl/RestoreApplicationServiceImplTest.java`

实现要求：

- `RestoreApplicationServiceImpl` 校验 `restoreMode`，空值按 `REAL`。
- 创建 `RestoreRecord` 时写入 `restoreMode`。
- 恢复开始前通过 `OperationsBackupExecutionGuard.tryEnterRestore()` 获得互斥。
- `OperationsRestoreWriteBlocker.enable(restoreId)` 成功后，记录 `writeBlockEnabled=true` 和 `writeBlockStartedAt`。
- `finally` 中调用 `OperationsRestoreWriteBlocker.disable(restoreId)`，并记录 `writeBlockReleasedAt`。
- `OperationsBackupScriptExecutor` 增加 `executeRestoreDrill(String backupBaseName, String preRestoreTimestamp)`。
- `REAL` 调用 `executeRestore(...)`。
- `DRILL` 调用 `executeRestoreDrill(...)`。

验收：

- 测试覆盖 `REAL` 恢复成功。
- 测试覆盖 `DRILL` 演练成功。
- 测试覆盖脚本失败后仍写入 `writeBlockReleasedAt`。
- 测试覆盖阻断开启失败时脚本不执行。
- 测试覆盖恢复期间自动备份无法进入 guard。

## Task 8: Web Write Blocking

目标：恢复期间业务写入请求在 Web 层被阻断。

文件：

- `kuzhambu-servers/common/kuzhambu-common-web/src/main/java/com/thundax/kuzhambu/common/web/restore/RestoreWriteBlockState.java`
- `kuzhambu-servers/common/kuzhambu-common-web/src/main/java/com/thundax/kuzhambu/common/web/restore/RestoreWriteBlockFilter.java`
- `kuzhambu-servers/common/kuzhambu-common-web/src/main/java/com/thundax/kuzhambu/common/web/restore/RestoreWriteBlockProperties.java`
- `kuzhambu-servers/common/kuzhambu-common-web/src/test/java/com/thundax/kuzhambu/common/web/restore/RestoreWriteBlockFilterTest.java`

阻断规则：

- 阻断 HTTP 方法：`POST`、`PUT`、`PATCH`、`DELETE`。
- 放行 HTTP 方法：`GET`、`HEAD`、`OPTIONS`。
- 放行路径必须配置化，至少包含：
  - `/api/operations/restore/execute`
  - `/api/operations/restore/page`
  - `/api/operations/restore/detail`
  - `/api/operations/backup/page`
  - `/api/operations/backup/detail`
  - `/actuator/health`
- 登录和验证码路径按现有 System 实际路径加入配置，不写模糊描述。
- 阻断响应 HTTP 状态为 `423 Locked`。
- 阻断响应业务码为 `OPERATIONS-RESTORE-WRITE-BLOCKED`。
- 阻断响应 message 为 `系统正在执行恢复，请稍后重试。`

实现要求：

- `RestoreWriteBlockState` 提供 `enable(String reason)`、`disable()`、`isBlocked()`。
- `RestoreWriteBlockFilter` 在进入 controller 前判断阻断状态。
- 放行路径由 `RestoreWriteBlockProperties` 提供。
- filter 不直接依赖 Operations application。

验收：

- 测试覆盖 `POST /api/classics/sancai/save` 被阻断。
- 测试覆盖 `GET /api/operations/backup/page` 放行。
- 测试覆盖 `POST /api/operations/restore/execute` 放行。
- 测试覆盖阻断响应结构。

## Task 9: Restore Interface Requests

目标：admin restore 请求与 assembler 支持恢复模式和模式筛选。

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/request/OperationsRestoreExecuteRequest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/request/OperationsRestorePageRequest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/assembler/OperationsRestoreInterfaceAssembler.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/OperationsRestoreAdminController.java`

接口字段：

- `OperationsRestoreExecuteRequest.restoreMode`：`REAL` 或 `DRILL`，不传时 application 按 `REAL`。
- `OperationsRestorePageRequest.restoreMode`：`REAL`、`DRILL` 或空。

实现要求：

- execute request 转 command 时传递 `restoreMode`。
- page request 转 query 时传递 `restoreMode`。
- controller 入口路径不变。

验收：

- 不改变 `POST /api/operations/restore/execute` 路径。
- 不改变 `POST /api/operations/restore/page` 路径。

## Task 10: Restore Interface Responses And Tests

目标：admin restore 响应和契约测试透出恢复模式与写入阻断时间。

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/response/OperationsRestoreExecuteResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/response/OperationsRestorePageResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/response/OperationsRestoreDetailResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/OperationsRestoreContractTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/OperationsRestoreAdminControllerTest.java`

响应字段：

- `restoreMode`
- `writeBlockStartedAt`
- `writeBlockReleasedAt`

验收：

- contract 测试覆盖 execute request 字段 `backupId`、`restoreMode`。
- contract 测试覆盖 page request 字段 `backupId`、`restoreMode`、`restoreStatus`、`requesterUserId`、`pageNo`、`pageSize`。
- contract 测试覆盖 response 字段 `restoreId`、`backupId`、`preRestoreBackupId`、`restoreMode`、`restoreStatus`、`writeBlockEnabled`、`writeBlockStartedAt`、`writeBlockReleasedAt`、`failureReason`、`startedAt`、`completedAt`。
- controller 测试覆盖 `restoreMode` 从 request 传入 command。

## Task 11: Frontend Types And Service

目标：前端类型和接口调用支持自动备份展示、恢复模式提交和恢复模式筛选。

文件：

- `kuzhambu-apps/admin-web/src/pages/operations/backup-restore/backup-restore-types.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/backup-restore/backup-restore-service.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/backup-restore/backup-restore-service-contract.test.ts`

类型要求：

- `OperationsBackupRecord.backupType` 支持 `AUTO`。
- `OperationsRestoreRecord.restoreMode` 支持 `REAL`、`DRILL`。
- `OperationsRestoreRecord.writeBlockStartedAt` 类型为 `string | null | undefined`。
- `OperationsRestoreRecord.writeBlockReleasedAt` 类型为 `string | null | undefined`。
- `RestoreExecuteCommand.restoreMode` 必填，值为 `REAL` 或 `DRILL`。
- `RestoreLedgerQuery.restoreMode` 支持 `REAL`、`DRILL` 或空。

接口要求：

- `recoverBackup({ backupId, restoreMode })` 请求体必须包含 `restoreMode`。
- 真实恢复传 `REAL`。
- 恢复演练传 `DRILL`。
- `pageRestores(...)` 请求体支持 `restoreMode` 筛选。

验收：

- service contract 测试覆盖 `POST /operations/restore/execute` body 为 `{ backupId: 9001, restoreMode: "DRILL" }`。
- service contract 测试覆盖 `POST /operations/restore/page` body 包含 `restoreMode: "DRILL"`。
- service contract 测试覆盖 restore response 解析包含 `restoreMode`、`writeBlockStartedAt`、`writeBlockReleasedAt`。

## Task 12: Frontend Page Controls

目标：`/operations/backup-restore` 页面把自动备份、恢复模式、演练状态和写入阻断状态展示清楚。

文件：

- `kuzhambu-apps/admin-web/src/pages/operations/backup-restore/backup-restore-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/backup-restore/backup-restore-page.css`
- `kuzhambu-apps/admin-web/src/pages/operations/backup-restore/backup-restore-page.test.tsx`

控件要求：

- 备份类型 `Select` 增加选项：
  - `全部备份` -> `ALL`
  - `自动备份` -> `AUTO`
  - `手动备份` -> `MANUAL`
  - `恢复前快照` -> `PRE_RESTORE`
- 恢复状态 `Select` 保留选项：
  - `全部状态` -> `ALL`
  - `进行中` -> `RUNNING`
  - `成功` -> `SUCCEEDED`
  - `失败` -> `FAILED`
- 新增恢复模式 `Select`，`aria-label` 为 `恢复模式`：
  - `全部模式` -> `ALL`
  - `真实恢复` -> `REAL`
  - `恢复演练` -> `DRILL`
- 备份台账表格调整：
  - `类型` 列：`AUTO` 显示 `自动备份`，`MANUAL` 显示 `手动备份`，`PRE_RESTORE` 显示 `恢复前快照`。
  - 新增 `发起人` 列：`requesterUserId` 为空显示 `系统自动`，非空显示用户 ID。
- 备份台账操作列：
  - `查看` 按钮打开备份详情抽屉。
  - `演练` 按钮对 `SUCCEEDED` 备份可见，点击弹出确认，确认后调用 `recoverBackup({ backupId, restoreMode: "DRILL" })`。
  - `恢复` danger 按钮对 `SUCCEEDED` 备份可见，点击弹出危险确认，确认后调用 `recoverBackup({ backupId, restoreMode: "REAL" })`。
- 恢复台账表格新增列：
  - `模式`：`REAL` 显示 `真实恢复`，`DRILL` 显示 `恢复演练`。
  - `写阻断`：运行中且 `writeBlockEnabled=true` 显示 `阻断中`；有 `writeBlockReleasedAt` 显示 `已释放`；否则显示 `未启用`。
  - `阻断开启`：展示 `writeBlockStartedAt`。
  - `阻断释放`：展示 `writeBlockReleasedAt`。
- 恢复详情抽屉新增描述项：
  - `恢复模式`
  - `写阻断开启时间`
  - `写阻断释放时间`
- 顶部摘要卡调整：
  - `最近一次成功备份` 允许展示 `AUTO` 备份。
  - 新增或改造摘要卡展示 `恢复演练` 成功次数和失败次数。

操作文案：

- 演练确认标题：`执行恢复演练`
- 演练确认说明：`演练会创建 PRE_RESTORE 快照并验证备份可恢复性，不覆盖生产业务数据。`
- 演练确认按钮：`执行演练`
- 真实恢复确认标题：`执行真实恢复`
- 真实恢复确认说明：`真实恢复会创建 PRE_RESTORE 快照，开启写入阻断，并覆盖业务恢复集中的当前数据。`
- 真实恢复确认按钮：`执行真实恢复`

验收：

- 页面测试覆盖 `AUTO` 显示为 `自动备份`。
- 页面测试覆盖 requester 为空显示 `系统自动`。
- 页面测试覆盖恢复模式 `Select` 发起 `DRILL` 筛选。
- 页面测试覆盖点击 `演练` 调用 `recoverBackup` 且 `restoreMode=DRILL`。
- 页面测试覆盖点击 `恢复` 调用 `recoverBackup` 且 `restoreMode=REAL`。
- 页面测试覆盖恢复详情抽屉展示 `恢复模式`、`写阻断开启时间`、`写阻断释放时间`。

## Task 13: Script Drill Mode

目标：脚本支持恢复演练，且演练结果能被 application 写入恢复台账。

文件：

- `deploy/scripts/restore-business-data.sh`
- `deploy/scripts/backup-lib.sh`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/support/DefaultOperationsBackupScriptExecutor.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/backup/support/DefaultOperationsBackupScriptExecutorTest.java`

脚本环境变量：

- `RESTORE_MODE=REAL`：执行真实恢复。
- `RESTORE_MODE=DRILL`：执行演练校验。

演练要求：

- 校验备份 SQL 文件存在。
- 校验 SQL checksum。
- 校验 storage archive 存在。
- 校验 storage checksum。
- 校验业务恢复集白名单存在。
- 不导入 SQL。
- 不覆盖 storage 目录或对象。
- 输出可被 application 捕获的成功或失败原因。

验收：

- `bash -n deploy/scripts/restore-business-data.sh` 通过。
- `bash -n deploy/scripts/backup-lib.sh` 通过。
- executor 测试覆盖 `RESTORE_MODE=DRILL` 环境变量传入。

## Task 14: Coverage Closure

目标：实现完成后同步覆盖矩阵，并在 PR 收口时删除本 RUNBOOK。

文件：

- `docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md`
- `docs/30-designs/RUNBOOK-OPERATIONS-BACKUP-RESTORE-CLOSURE.md`

完成要求：

- 覆盖矩阵“备份与恢复”状态改为 `已完成`。
- 已完成部分写明：
  - 启动自动备份。
  - 每天凌晨 2:00 自动备份。
  - 恢复期间真实写入阻断。
  - 真实恢复与恢复演练台账状态。
  - admin 页面展示自动备份、恢复演练和写阻断状态。
- 未完成部分不得保留本 RUNBOOK 范围内事项。
- PR 合并前删除本 RUNBOOK。

## Execution Order

1. Task 1 至 Task 3：自动备份入口、互斥、cron 和配置。
2. Task 4 至 Task 7：恢复模式、持久化、application flow 和写入阻断时间。
3. Task 8 至 Task 10：Web 阻断和 admin restore 接口契约。
4. Task 11 至 Task 12：前端类型、接口、控件、操作和页面测试。
5. Task 13：脚本演练模式。
6. Task 14：覆盖矩阵收口并删除本 RUNBOOK。

## Verification

Java 后端验证：

```sh
cd kuzhambu-servers
mvn -pl biz/operations/kuzhambu-operations-application spotless:apply
mvn -pl biz/operations/kuzhambu-operations-interface spotless:apply
mvn -pl biz/operations/kuzhambu-operations-infra spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn test
```

前端验证：

```sh
cd kuzhambu-apps
npm --workspace kuzhambu-admin-web run format
npm run format:check
npm run lint
npm run test
```

脚本验证：

```sh
bash -n deploy/scripts/backup-lib.sh
bash -n deploy/scripts/backup-business-data.sh
bash -n deploy/scripts/restore-business-data.sh
bash -n deploy/scripts/cleanup-backups.sh
```

手动冒烟：

- 启动 admin-starter 后确认出现 `AUTO` 备份记录，页面显示 `自动备份` 和 `系统自动`。
- 将 cron 临时配置为近期开火时间，确认自动备份由调度产生。
- 点击备份台账 `演练`，确认恢复台账出现 `DRILL`，页面显示 `恢复演练`。
- 演练运行期间发起任一业务写入，确认返回写入阻断错误且业务数据未变化。
- 点击备份台账 `恢复`，确认恢复台账出现 `REAL`，页面显示 `真实恢复`。
- 人为制造脚本失败，确认 `writeBlockReleasedAt` 有值、失败原因可见、`PRE_RESTORE` 快照保留。

## Done Criteria

- `operations_backup` 可追溯 `AUTO`、`MANUAL`、`PRE_RESTORE`。
- `operations_restore` 精确记录 `restore_mode`、`write_block_started_at`、`write_block_released_at`。
- 启动自动备份和每天凌晨 2:00 自动备份均可运行、可关闭、可测试。
- 恢复期间 Web 层真实阻断业务写入，并在恢复结束后可靠释放。
- admin 页面能完成手动备份、恢复演练、真实恢复、台账筛选、详情查看。
- 覆盖矩阵具备把“备份与恢复”更新为已完成的事实基础。
