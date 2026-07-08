# RUNBOOK: Operations 自动化恢复动作编排闭环

## 目标

补齐 Operations 台账记录里的自动化恢复动作编排、状态查询、结果追踪和权限约束。范围限定在 Operations `health`、`alert`、`restore`、`task`，不改 Classics 页面。

## 交付边界

- 后端只改 `kuzhambu-servers/biz/operations/` 下的 Operations application 和 interface 代码。
- 前端只改 `kuzhambu-apps/admin-web/src/pages/operations/health/` 下的健康检查页面、服务和测试。
- 文档只新增本文件：`docs/30-designs/RUNBOOK-OPERATIONS-RESTORE-ORCHESTRATION.md`。
- 不新增数据库表，不新增数据库字段，不修改 `db/schema/*.sql` 或 `db/data/*.sql`。

## 相关文件总览

后端 application：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/command/OperationsHealthAlertRecoverCommand.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/service/impl/HealthAlertApplicationServiceImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthRecoveryLinkFactory.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/service/impl/RestoreApplicationServiceImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/health/service/impl/HealthAlertApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/restore/service/impl/RestoreApplicationServiceImplTest.java`

后端 interface：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/assembler/OperationsHealthAlertInterfaceAssembler.java`

前端：

- `kuzhambu-apps/admin-web/src/pages/operations/health/health-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/health/health-service.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/health/health-types.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/health/health-page.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/health/health-service-contract.test.ts`

文档：

- `docs/30-designs/RUNBOOK-OPERATIONS-RESTORE-ORCHESTRATION.md`

## 数据结构变更

### 数据库结构变更

无数据库结构变更：

- 不新增表。
- 不新增字段。
- 不新增索引。
- 不修改 `db/schema/operations.sql`。
- 不修改 `db/data/operations.sql`。
- 不修改 `db/data/system.sql`。
- 不修改 `db/data-source/system.json`。

### 应用字段变更

新增 Java command 字段：

文件：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/command/OperationsHealthAlertRecoverCommand.java`

| 字段 | 类型 | 来源 | 用途 |
| --- | --- | --- | --- |
| `recoveredByUserId` | `Long` | 当前 admin subject ID | 自动恢复动作写入备份、恢复和任务快照发起人 |

新增 Java 常量：

文件：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthRecoveryLinkFactory.java`

| 常量 | 值 | 用途 |
| --- | --- | --- |
| `ACTION_RUN_RESTORE` | `"RUN_RESTORE"` | 标识告警恢复需要触发恢复编排 |

含义：

- 只有 `operations_health_alert.recovery_action = "RUN_RESTORE"` 的告警恢复动作会触发恢复编排。
- 现有 `OPEN_*`、`RUN_MANUAL_BACKUP`、`NONE` 行为保持原语义。

字段来源：

- `OperationsHealthAlertInterfaceAssembler` 从 `KuzhambuContextHolder.currentSubjectId()` 读取当前 admin 用户 ID。

用途：

- 自动恢复动作触发手动备份时，写入 `operations_backup.requester_user_id`。
- 自动恢复动作触发恢复时，写入 `operations_restore.requester_user_id`。
- 恢复任务快照写入 `operations_long_task_snapshot.requested_by_user_id`。

### 既有表字段写入口径

表：`operations_health_alert`

| 字段 | 写入值 | 写入时机 |
| --- | --- | --- |
| `alert_status` | `RECOVERED` | 告警恢复动作成功后 |
| `recovered_at` | 当前时间 | 告警恢复动作成功后 |
| `failure_reason` | `NULL` | 告警恢复动作成功后 |
| `recovery_action` | 不改 | 始终不由恢复动作改写 |
| `recovery_target` | 不改 | 始终不由恢复动作改写 |
| `source_ref_type` | 不改 | 始终不由恢复动作改写 |
| `source_ref_id` | 不改 | 始终不由恢复动作改写 |

表：`operations_restore`

| 字段 | 写入值 | 写入时机 |
| --- | --- | --- |
| `backup_id` | 恢复命令 `backupId` 或 `recoveryTarget.backupId` | 创建恢复记录 |
| `pre_restore_backup_id` | PRE_RESTORE 备份 ID | 创建恢复记录 |
| `restore_mode` | `REAL` 或 `DRILL` | 创建恢复记录 |
| `restore_status` | `RUNNING` | 创建恢复记录 |
| `restore_status` | `SUCCEEDED` 或 `FAILED` | 恢复完成 |
| `write_block_enabled` | `true` | 写入阻断开启成功后 |
| `write_block_started_at` | 写入阻断开启时间 | 写入阻断开启成功后 |
| `write_block_released_at` | 写入阻断释放时间 | finally 释放阻断后 |
| `failure_reason` | 恢复失败原因，最长 1000 字符 | 恢复失败 |
| `requester_user_id` | 当前 admin 用户 ID | 创建恢复记录 |
| `started_at` | 恢复开始时间 | 创建恢复记录 |
| `completed_at` | 恢复结束时间 | 恢复完成 |

表：`operations_backup`

| 字段 | 写入值 | 写入时机 |
| --- | --- | --- |
| `backup_type` | `PRE_RESTORE` | 创建恢复前快照记录 |
| `backup_status` | `RUNNING` | 创建恢复前快照记录 |
| `backup_status` | `SUCCEEDED` 或 `FAILED` | 快照完成 |
| `file_name` | 快照文件名 | 快照成功 |
| `file_size_bytes` | 快照文件大小 | 快照成功 |
| `checksum` | 快照校验值 | 快照成功 |
| `failure_reason` | 快照失败原因 | 快照失败 |
| `requester_user_id` | 当前 admin 用户 ID | 创建恢复前快照记录 |
| `started_at` | 快照开始时间 | 创建恢复前快照记录 |
| `completed_at` | 快照完成时间 | 快照完成 |
| `expires_at` | 开始时间后 30 天 | 创建恢复前快照记录 |

表：`operations_long_task_snapshot`

| 字段 | 写入值 | 写入时机 |
| --- | --- | --- |
| `source_domain` | `operations` | 创建任务快照 |
| `task_type` | `RESTORE` | 创建任务快照 |
| `task_key` | `restore:<restoreId>` | 创建任务快照 |
| `task_status` | `RUNNING` | 创建任务快照 |
| `task_status` | `SUCCEEDED` 或 `FAILED` | 恢复完成 |
| `total_count` | `1` | 创建任务快照 |
| `success_count` | `0` | 创建任务快照 |
| `success_count` | 成功 `1`，失败 `0` | 恢复完成 |
| `failed_count` | `0` | 创建任务快照 |
| `failed_count` | 成功 `0`，失败 `1` | 恢复完成 |
| `failure_reason` | `restore.failureReason` | 恢复失败 |
| `requested_by_user_id` | 当前 admin 用户 ID | 创建任务快照 |
| `started_at` | 恢复开始时间 | 创建任务快照 |
| `completed_at` | 恢复结束时间 | 恢复完成 |
| `snapshot_at` | 当前时间 | 创建和更新任务快照 |

## 后端任务拆分

### 后端任务 1：告警恢复命令与操作者透传

文件数：2

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/command/OperationsHealthAlertRecoverCommand.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/assembler/OperationsHealthAlertInterfaceAssembler.java`

要求：

- `OperationsHealthAlertRecoverCommand` 携带 `recoveredByUserId`。
- `OperationsHealthAlertInterfaceAssembler.toCommand(OperationsHealthAlertRecoverRequest)` 写入当前 admin 用户 ID。
- 请求模型 `OperationsHealthAlertRecoverRequest` 不新增入参，避免前端伪造操作者。

验收：

- 后端接收到 `/api/operations/health/alerts/recover` 请求后，application command 中能拿到当前 admin 用户 ID。
- 当前 subject 不是 admin 用户或 subject ID 非数字时，`recoveredByUserId` 为 `null`，不从请求体取值。

### 后端任务 2：健康告警自动恢复动作编排

文件数：3

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/service/impl/HealthAlertApplicationServiceImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthRecoveryLinkFactory.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/health/service/impl/HealthAlertApplicationServiceImplTest.java`

要求：

- `recoveryAction = RUN_MANUAL_BACKUP`：调用 `BackupApplicationService.execute(new OperationsBackupExecuteCommand(recoveredByUserId))`。
- `recoveryAction = RUN_RESTORE`：从 `recoveryTarget` 解析 `backupId`，调用 `RestoreApplicationService.execute(new OperationsRestoreExecuteCommand(BackupId.of(backupId), recoveredByUserId))`。
- `recoveryAction = OPEN_*` 或 `NONE`：只标记告警为 `RECOVERED`。
- 自动动作执行成功后再更新告警状态。
- 自动动作执行失败时不更新告警状态，让前端显示失败。
- 测试覆盖手动备份动作和恢复动作。

验收：

- `RUN_MANUAL_BACKUP` 能触发手动备份并写入备份台账。
- `RUN_RESTORE` 能从 `recoveryTarget` 中的 JSON 文本解析 `backupId` 并触发恢复。
- `OPEN_HEALTH_DETAIL`、`OPEN_BACKUP_RESTORE`、`OPEN_CLEANUP_DETAIL`、`OPEN_TASK_DETAIL`、`NONE` 不触发自动动作，只标记告警恢复。
- 自动动作异常时，告警仍保持原状态，不写 `RECOVERED`。

### 后端任务 3：恢复执行结果写入任务台账

文件数：2

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/service/impl/RestoreApplicationServiceImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/restore/service/impl/RestoreApplicationServiceImplTest.java`

要求：

- 创建 `RestoreRecord` 后立即创建 `LongTaskSnapshot`。
- 任务快照创建字段固定为：
  - `sourceDomain = "operations"`
  - `taskType = "RESTORE"`
  - `taskKey = "restore:<restoreId>"`
  - `taskStatus = "RUNNING"`
  - `totalCount = 1`
  - `successCount = 0`
  - `failedCount = 0`
- 恢复完成后更新同一条任务快照：
  - 成功：`taskStatus = "SUCCEEDED"`、`successCount = 1`、`failedCount = 0`
  - 失败：`taskStatus = "FAILED"`、`successCount = 0`、`failedCount = 1`、`failureReason = restore.failureReason`
- 测试覆盖恢复成功和失败两种任务快照结果。

验收：

- 恢复台账仍保留 `operations_restore` 和 PRE_RESTORE 快照原有行为。
- 任务台账可通过 `operations/task/page` 按 `sourceDomain=operations`、`taskType=RESTORE`、`taskStatus` 查询恢复结果。
- 任务详情可通过 `operations/task/detail` 查看 `taskKey`、成功/失败计数、失败原因和完成时间。

## 前端任务拆分

### 前端任务 1：健康告警服务契约

文件数：3

文件：

- `kuzhambu-apps/admin-web/src/pages/operations/health/health-service.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/health/health-types.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/health/health-service-contract.test.ts`

接口：

- `getOperationsHealthAlerts(query)` 调用 `POST /operations/health/alerts/page`。
- `confirmOperationsHealthAlert({ alertId })` 调用 `POST /operations/health/alerts/ack`。
- `recoverOperationsHealthAlert({ alertId })` 调用 `POST /operations/health/alerts/recover`。

请求体字段：

- `alertId: number`

响应字段：

- `getOperationsHealthAlerts(query)` 返回 `OperationsPageRecord<OperationsHealthAlertRecord>`。
- `confirmOperationsHealthAlert({ alertId })` 返回 `void`。
- `recoverOperationsHealthAlert({ alertId })` 返回 `void`。

类型：

- `OperationsHealthAlertRecord.alertId`
- `OperationsHealthAlertRecord.component`
- `OperationsHealthAlertRecord.alertType`
- `OperationsHealthAlertRecord.alertLevel`
- `OperationsHealthAlertRecord.alertStatus`
- `OperationsHealthAlertRecord.sourceRefType`
- `OperationsHealthAlertRecord.sourceRefId`
- `OperationsHealthAlertRecord.latestCheckId`
- `OperationsHealthAlertRecord.message`
- `OperationsHealthAlertRecord.suggestion`
- `OperationsHealthAlertRecord.recoveryAction`
- `OperationsHealthAlertRecord.recoveryTarget`
- `OperationsHealthAlertRecord.firstTriggeredAt`
- `OperationsHealthAlertRecord.lastTriggeredAt`
- `OperationsHealthAlertRecord.ackedAt`
- `OperationsHealthAlertRecord.ackedByUserId`
- `OperationsHealthAlertRecord.recoveredAt`
- `OperationsHealthAlertRecord.failureReason`

验收：

- service contract 测试必须断言三个 endpoint 和 body 字段。
- 页面不再从 `operations/dashboard/dashboard-service.ts` 调用健康告警接口。

### 前端任务 2：健康检查页面告警控件

文件数：2

文件：

- `kuzhambu-apps/admin-web/src/pages/operations/health/health-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/health/health-page.test.tsx`

控件和操作：

健康记录表格：

- 控件：每行 `详情` 按钮。
  - 操作：点击后打开健康详情抽屉。
  - 本任务不改变该按钮行为。
- 控件：每行 `查看告警` 按钮。
  - 操作：点击后打开关联告警抽屉。
  - 请求：调用 `getOperationsHealthAlerts({ latestCheckId: record.checkId, pageNo: 1, pageSize: 10 })`。
  - 加载态：显示 `加载中...`。
  - 空态：显示 `暂无关联告警`。
  - 失败态：显示 `关联告警加载失败`。

关联告警抽屉：

- 标题：`关联告警 #<checkId>`。
- 数据来源：当前健康记录 `checkId` 对应的告警分页结果。
- 表格列：
  - `级别`
  - `状态`
  - `消息`
  - `建议`
  - `恢复动作`
  - `最后触发`
  - `操作`

`确认` 按钮：

- 位置：关联告警抽屉表格 `操作` 列。
- 可用条件：用户具备 `operations:health:manage` 且 `alertStatus = "ACTIVE"`。
- 禁用条件：用户缺少 `operations:health:manage` 或 `alertStatus != "ACTIVE"`。
- 点击操作：直接调用 `confirmOperationsHealthAlert({ alertId })`。
- 成功反馈：显示 `告警已确认`。
- 成功刷新：刷新 `["operations", "health", "alerts"]` 查询。
- 失败反馈：显示 `告警确认失败` 或后端错误消息。

`恢复` 按钮：

- 位置：关联告警抽屉表格 `操作` 列。
- 可用条件：用户具备 `operations:health:manage` 且 `alertStatus != "RECOVERED"`。
- 禁用条件：用户缺少 `operations:health:manage` 或 `alertStatus = "RECOVERED"`。
- 点击操作：先弹出 `useKuzhambuConfirm().danger` 二次确认。
- 二次确认标题：`执行告警恢复`。
- 二次确认主文案：`确认处理告警 #<alertId> 吗？`。
- 二次确认说明：
  - `recoveryAction` 以 `RUN_` 开头：说明会触发自动化恢复动作，并回写告警与任务台账。
  - `recoveryAction` 不以 `RUN_` 开头：说明只会将告警标记为已恢复。
- 二次确认按钮：`执行恢复`。
- 确认操作：调用 `recoverOperationsHealthAlert({ alertId })`。
- 成功反馈：显示 `恢复动作已完成`。
- 成功刷新：
  - 刷新 `["operations", "health", "alerts"]` 查询。
  - 刷新 `["operations", "task", "page"]` 查询。
  - 刷新 `["operations", "restore", "page"]` 查询。
- 失败反馈：显示 `恢复动作失败` 或后端错误消息。

权限态：

- 无 `operations:health:manage` 权限时，`确认` 和 `恢复` 按钮禁用；后端仍以 `@HasPermission("operations:health:manage")` 作为最终权限约束。

验收：

- 健康页测试必须覆盖打开关联告警抽屉。
- 健康页测试必须覆盖 `确认` 按钮调用 `confirmOperationsHealthAlert({ alertId })`。
- 健康页测试必须覆盖 `恢复` 按钮二次确认后调用 `recoverOperationsHealthAlert({ alertId })`。
- 健康页测试必须覆盖空态和失败态。

## 权限

后端权限入口：

- 文件：`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/OperationsHealthAlertAdminController.java`
  - `POST /api/operations/health/alerts/page`：`operations:health:view`
  - `POST /api/operations/health/alerts/ack`：`operations:health:manage`
  - `POST /api/operations/health/alerts/recover`：`operations:health:manage`
- 文件：`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/OperationsRestoreAdminController.java`
  - `POST /api/operations/restore/execute`：`operations:restore:execute`
  - `POST /api/operations/restore/page`：`operations:restore:view`
  - `POST /api/operations/restore/detail`：`operations:restore:view`
- 文件：`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/task/controller/OperationsTaskAdminController.java`
  - `POST /api/operations/task/page`：`operations:task:view`
  - `POST /api/operations/task/detail`：`operations:task:view`

前端权限入口：

- 文件：`kuzhambu-apps/admin-web/src/pages/operations/health/health-page.tsx`
  - `operations:health:view` 控制健康检查页面主体查询。
  - `operations:health:manage` 控制关联告警抽屉内 `确认` 和 `恢复` 按钮可用状态。

## 验证

后端 application 最小验证：

```sh
cd kuzhambu-servers
mvn -pl biz/operations/kuzhambu-operations-application -am -Dtest=RestoreApplicationServiceImplTest,HealthAlertApplicationServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test
```

后端 interface 验证：

```sh
cd kuzhambu-servers
mvn -pl biz/operations/kuzhambu-operations-interface -am test
```

前端最小验证：

```sh
cd kuzhambu-apps
pnpm --filter kuzhambu-admin-web exec vitest run src/pages/operations/health/health-page.test.tsx src/pages/operations/health/health-service-contract.test.ts
```

前端门禁：

```sh
cd kuzhambu-apps
pnpm --filter kuzhambu-admin-web run format:check
pnpm --filter kuzhambu-admin-web run lint
pnpm --filter kuzhambu-admin-web run build
```

## 回滚

- 回滚 `HealthAlertApplicationServiceImpl.java` 和 `OperationsHealthRecoveryLinkFactory.java` 后，告警恢复只保留状态标记，不触发自动备份或自动恢复。
- 回滚 `RestoreApplicationServiceImpl.java` 后，恢复仍会写恢复台账和 PRE_RESTORE 快照，但不再写 `operations_long_task_snapshot`。
- 回滚 `health-page.tsx`、`health-service.ts`、`health-types.ts` 后，健康检查页面不再提供告警确认和恢复控件；后端 API 可继续保留。
