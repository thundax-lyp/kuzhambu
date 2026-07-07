# Operations Health Recovery Loop Runbook

## Purpose

本文档定义 Operations“健康检查与运行状态”最终收口实施手册。目标是把现有健康采集、备份恢复、清理、报表和长任务台账收束成“发现异常 -> 记录告警 -> 页面提示 -> 人工确认 -> 处置跳转 -> 恢复归档”的可审核闭环。

本文档只用于本轮审核和执行拆解。审核通过前不实现代码、不改 `TODO.md`、不提交。任务关闭后删除本文档及残留引用。

## Confirmed Decisions

- 新增独立表 `operations_health_alert`，不把告警状态塞进 `operations_health_check`。
- 本轮只做“处置建议 + 稳定跳转入口”，不做自动重试。
- 新增 `operations:health:manage` 管理权限；`operations:health:view` 只允许查看。
- 健康类告警由连续 `UP` 采样自动恢复；一次性失败对象允许管理员手动恢复。
- `DEGRADED` 连续 3 次触发 `WARNING`；`DOWN` 1 次触发 `CRITICAL`；连续 2 次 `UP` 自动恢复健康类告警。
- 采集过期默认 10 分钟触发 `WARNING`。
- 恢复写阻断超过 30 分钟未释放触发 `CRITICAL`。
- `REPORT` 失败纳入后端告警策略；报表 admin 页面未开放前，前端只展示处置建议，不生成死链。

## Scope

覆盖：

- Operations 后端健康告警数据结构、领域模型、策略服务和 admin API。
- Operations 健康、备份、恢复、清理、报表、长任务失败到健康告警的来源映射。
- Admin Web `/operations/dashboard` 顶部告警提示、告警列表抽屉、健康明细抽屉中的确认与跳转。
- Admin Web `/operations/tasks`、`/operations/backup-restore`、`/operations/cleanup` 的失败状态提示控件。
- 定向后端、前端测试和文档收口。

不覆盖：

- 外部短信、邮件、IM 或第三方告警系统。
- 自动重试、自动恢复执行器或后台补偿任务。
- System 日志和业务审计入口收口。
- 备份恢复脚本语义重写。
- 分布式多实例探针调度。

## Data Structure Changes

### New Table

在 `db/schema/operations.sql` 新增：

```sql
CREATE TABLE IF NOT EXISTS `operations_health_alert` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `alert_id` bigint NOT NULL,
    `component` varchar(128) NOT NULL,
    `alert_type` varchar(32) NOT NULL,
    `alert_level` varchar(16) NOT NULL,
    `alert_status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
    `source_ref_type` varchar(32) NOT NULL,
    `source_ref_id` bigint DEFAULT NULL,
    `latest_check_id` bigint DEFAULT NULL,
    `message` varchar(1024) DEFAULT NULL,
    `suggestion` varchar(1024) DEFAULT NULL,
    `recovery_action` varchar(64) DEFAULT NULL,
    `recovery_target` text DEFAULT NULL,
    `first_triggered_at` datetime(3) NOT NULL,
    `last_triggered_at` datetime(3) NOT NULL,
    `acked_at` datetime(3) DEFAULT NULL,
    `acked_by_user_id` bigint DEFAULT NULL,
    `recovered_at` datetime(3) DEFAULT NULL,
    `failure_reason` varchar(1024) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_operations_health_alert_id` (`alert_id`),
    KEY `idx_operations_health_alert_status` (`alert_status`, `alert_level`, `last_triggered_at`),
    KEY `idx_operations_health_alert_component` (`component`, `alert_status`, `last_triggered_at`),
    KEY `idx_operations_health_alert_source` (`source_ref_type`, `source_ref_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运维健康告警表';
```

字段语义：

- `alert_id`：Operations 自有告警业务 ID。
- `component`：组件名；健康探针使用 `HealthCheckRecord.component`，任务类使用固定组件名。
- `alert_type`：`HEALTH_DOWN`、`HEALTH_DEGRADED`、`HEALTH_STALE`、`BACKUP_FAILED`、`RESTORE_FAILED`、`RESTORE_WRITE_BLOCK_STALE`、`CLEANUP_FAILED`、`REPORT_FAILED`、`LONG_TASK_FAILED`。
- `alert_level`：`WARNING` 或 `CRITICAL`。
- `alert_status`：`ACTIVE`、`ACKED` 或 `RECOVERED`。
- `source_ref_type`：`HEALTH_CHECK`、`BACKUP`、`RESTORE`、`CLEANUP`、`REPORT`、`LONG_TASK`。
- `source_ref_id`：来源业务 ID；健康采集过期类告警允许为空。
- `latest_check_id`：健康类告警最近关联 `operations_health_check.check_id`；非健康类允许为空。
- `message`：页面主提示文案，不包含敏感值。
- `suggestion`：页面处置建议，不包含命令输出原文。
- `recovery_action`：前端动作枚举。
- `recovery_target`：JSON 文本，只保存路由、业务 ID 和筛选条件。
- `failure_reason`：策略或恢复动作失败原因，截断到 1024 字符。

### Stable Enum Values

`recovery_action` 固定值：

- `OPEN_HEALTH_DETAIL`
- `OPEN_BACKUP_RESTORE`
- `OPEN_CLEANUP_DETAIL`
- `OPEN_TASK_DETAIL`
- `RUN_MANUAL_BACKUP`
- `NONE`

`component` 固定建议：

- 健康探针：沿用探针组件名，如 `admin-server`。
- 备份：`operations-backup`。
- 恢复：`operations-restore`。
- 清理：`operations-cleanup`。
- 报表：`operations-report`。
- 长任务：`operations-task`。

`recovery_target` 示例：

```json
{"route":"/operations/backup-restore","backupId":9001,"action":"manualBackup"}
```

```json
{"route":"/operations/cleanup","cleanupId":7001}
```

```json
{"route":"/operations/tasks","snapshotId":6001}
```

## Backend Tasks

### Task 1: Alert Domain And Persistence

目标文件：

- `db/schema/operations.sql`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/model/entity/HealthAlertRecord.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/model/valueobject/HealthAlertId.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/codec/HealthAlertIdCodec.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/repository/HealthAlertRepository.java`

实现要求：

- `HealthAlertRecord` 字段与 `operations_health_alert` 表业务字段一一对应。
- `HealthAlertRepository` 至少提供 `insert`、`update`、`getById`、`page`、`findOpenBySource`、`listOpenByComponent`、`listOpenSummary`。
- `findOpenBySource` 只查询 `ACTIVE` / `ACKED`，用于幂等更新未恢复告警。

验收点：

- 新表字段、索引、注释与本 RUNBOOK 完全一致。
- 同一 `source_ref_type + source_ref_id + alert_type` 未恢复告警不会重复插入。

### Task 2: Alert Infra Mapping

目标文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/persistence/dataobject/HealthAlertDO.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/persistence/mapper/HealthAlertMapper.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/persistence/assembler/HealthAlertPersistenceAssembler.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/repository/impl/HealthAlertRepositoryImpl.java`

实现要求：

- `HealthAlertDO` 使用 `@TableName("operations_health_alert")`。
- 分页默认按 `alert_status`、`alert_level`、`last_triggered_at desc` 排序。
- `recoveryTarget` 作为字符串透传，不在 repository 内解析 JSON。

验收点：

- Repository 测试覆盖 insert、update、page、findOpenBySource。
- null `source_ref_id` 只能用于 `HEALTH_STALE`，其他类型必须由应用层拒绝。

### Task 3: Alert Application Strategy

目标文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthAlertPolicyProperties.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthAlertStrategy.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthRecoveryLinkFactory.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/service/impl/HealthCheckApplicationServiceImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthCollector.java`

配置字段：

- `kuzhambu.operations.health.alert.degraded-threshold=3`
- `kuzhambu.operations.health.alert.recovery-up-threshold=2`
- `kuzhambu.operations.health.alert.stale-minutes=10`
- `kuzhambu.operations.health.alert.write-block-stale-minutes=30`

实现要求：

- `OperationsHealthCollector.collect()` 写入每条 `HealthCheckRecord` 后调用策略。
- 策略根据最近采样生成或恢复健康类告警。
- `DOWN` 立即生成 `CRITICAL HEALTH_DOWN`。
- 连续 3 次 `DEGRADED` 生成 `WARNING HEALTH_DEGRADED`。
- 连续 2 次 `UP` 恢复同组件 `HEALTH_DOWN` / `HEALTH_DEGRADED` 未恢复告警。
- 采集过期扫描由 dashboard/summary 查询前触发轻量判断，不新增调度器。
- 策略失败不得回滚原始健康记录写入。

验收点：

- Strategy 单测覆盖 DOWN、连续 DEGRADED、连续 UP 恢复、采集过期。
- 生成的 `message`、`suggestion`、`recoveryAction`、`recoveryTarget` 都来自后端。

### Task 4: Failure Source Linkage

目标文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/service/impl/BackupApplicationServiceImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/service/impl/RestoreApplicationServiceImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/service/impl/CleanupApplicationServiceImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportTaskExecutor.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/task/service/impl/TaskApplicationServiceImpl.java`

实现要求：

- 备份失败生成 `BACKUP_FAILED`，`CRITICAL`，`recoveryAction=RUN_MANUAL_BACKUP`。
- 恢复失败生成 `RESTORE_FAILED`，`CRITICAL`，`recoveryAction=OPEN_BACKUP_RESTORE`。
- 写阻断超过 30 分钟生成 `RESTORE_WRITE_BLOCK_STALE`，`CRITICAL`。
- 清理失败生成 `CLEANUP_FAILED`，`WARNING`，`recoveryAction=OPEN_CLEANUP_DETAIL`。
- 报表失败生成 `REPORT_FAILED`，`WARNING`，`recoveryAction=NONE`。
- 长任务 `FAILED` 生成 `LONG_TASK_FAILED`，`WARNING`，`recoveryAction=OPEN_TASK_DETAIL`。

验收点：

- 每个失败来源只写入或更新一条未恢复告警。
- 成功后的同来源告警按业务规则进入 `RECOVERED`，或等待管理员手动恢复。

### Task 5: Alert Admin API And Dashboard Response

目标文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/service/HealthAlertApplicationService.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/service/impl/HealthAlertApplicationServiceImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/OperationsHealthAlertAdminController.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/assembler/OperationsHealthAlertInterfaceAssembler.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/controller/response/OperationsDashboardOverviewResponse.java`

新增接口：

- `POST /api/operations/health/alerts/page`
- `POST /api/operations/health/alerts/ack`
- `POST /api/operations/health/alerts/recover`

请求/响应文件：

- `OperationsHealthAlertPageRequest.java`
- `OperationsHealthAlertAckRequest.java`
- `OperationsHealthAlertRecoverRequest.java`
- `OperationsHealthAlertPageResponse.java`
- `OperationsHealthAlertSummaryResponse.java`

`OperationsDashboardOverviewResponse` 新增字段：

- `private Integer activeAlertCount;`
- `private Integer criticalAlertCount;`
- `private Integer warningAlertCount;`
- `private String highestAlertLevel;`
- `private OperationsHealthAlertSummaryResponse latestAlert;`

验收点：

- `alerts/page` 使用 `operations:health:view`。
- `alerts/ack` 和 `alerts/recover` 使用 `operations:health:manage`。
- ack/recover 写入 System 业务审计。
- Dashboard overview 不向无 `operations:health:view` 权限用户暴露告警详情。

## Frontend Tasks

### Task 6: Dashboard Types And Services

目标文件：

- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-types.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-service.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-service-contract.test.ts`

类型新增：

- `OperationsHealthAlertLevel = "WARNING" | "CRITICAL"`
- `OperationsHealthAlertStatus = "ACTIVE" | "ACKED" | "RECOVERED"`
- `OperationsHealthRecoveryAction = "OPEN_HEALTH_DETAIL" | "OPEN_BACKUP_RESTORE" | "OPEN_CLEANUP_DETAIL" | "OPEN_TASK_DETAIL" | "RUN_MANUAL_BACKUP" | "NONE"`
- `OperationsHealthAlertRecord`
- `OperationsHealthAlertSummaryRecord`

服务新增：

- `getHealthAlerts(query)`
- `ackHealthAlert(payload)`
- `recoverHealthAlert(payload)`

验收点：

- dashboard overview 类型包含 `activeAlertCount`、`criticalAlertCount`、`warningAlertCount`、`highestAlertLevel`、`latestAlert`。
- service contract 测试断言 API path 与请求体字段。

### Task 7: Dashboard Alert Banner And Drawer

目标文件：

- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.css`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.test.tsx`

控件要求：

- 在页面内容顶部新增 `Alert` 控件。
- `Alert` type：`CRITICAL` 使用 `error`，`WARNING` 使用 `warning`。
- `Alert` message：`latestAlert.message`；没有 message 时显示“存在未恢复健康告警”。
- `Alert` description：显示 `latestAlert.suggestion`、最近触发时间和未恢复总数。
- `Alert` action：`Button` 文案“查看告警”，点击打开告警列表抽屉。
- 告警列表抽屉使用 `KuzhambuDrawer`。
- 抽屉内使用 `List` 或 `Table` 展示：级别、组件、状态、消息、最近触发时间、操作。
- 每条告警操作包含：
  - `Button` “确认”：只在 `ACTIVE` 且有 `operations:health:manage` 时显示。
  - `Button` “标记恢复”：只在非健康采样自动恢复类且有 `operations:health:manage` 时显示。
  - `Button` “去处理”：当 `recoveryAction !== "NONE"` 时显示。
- “异常组件 / 失败任务”指标卡改为可点击控件；点击后打开告警列表抽屉，并默认筛选 `ACTIVE`。

操作要求：

- “确认”调用 `ackHealthAlert` 后刷新 overview 和告警列表。
- “标记恢复”调用 `recoverHealthAlert` 后刷新 overview 和告警列表。
- “去处理”只根据 `recoveryAction` 和 `recoveryTarget.route` 跳转，不解析 `message` 文本。

验收点：

- 无未恢复告警时不渲染顶部 `Alert`。
- 没有 `operations:health:manage` 权限时不显示确认和恢复按钮。
- 抽屉在 375px 宽度下按钮不重叠。

### Task 8: Dashboard Health Detail Controls

目标文件：

- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.css`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.test.tsx`

控件要求：

- 健康巡检卡片每个 `operations-dashboard-health-item` 增加告警角标。
- 角标使用 `KuzhambuTag`：`CRITICAL` 为 `danger`，`WARNING` 为 `warning`。
- 健康明细抽屉新增“关联告警”区域。
- “关联告警”区域展示 message、suggestion、lastTriggeredAt。
- 抽屉底部新增 `Button` “查看全部告警”，点击打开告警列表抽屉并筛选当前 component。

验收点：

- 健康明细仍展示采集来源、目标、延迟、检查时间、消息。
- 当前 component 无告警时显示 `Empty description="暂无关联告警"`。

### Task 9: Task Page Failure Hint

目标文件：

- `kuzhambu-apps/admin-web/src/pages/operations/tasks/tasks-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/tasks/tasks-types.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/tasks/tasks-page.test.tsx`

控件要求：

- 任务列表 `FAILED` 行增加 `KuzhambuTag type="danger"`。
- 任务详情抽屉在 `taskStatus === "FAILED"` 时新增 `Alert type="warning"`。
- `Alert` message 显示“长任务执行失败”。
- `Alert` description 显示 `failureReason` 和“请查看来源域任务状态，必要时重新发起业务动作。”。
- 如果详情接口返回关联告警，则显示 `Button` “查看告警”；本轮可通过 `snapshotId` 在告警列表中筛选。

验收点：

- 失败任务不改变原有详情字段展示。
- 无失败原因时显示“未返回失败原因”。

### Task 10: Backup Restore And Cleanup Failure Hints

目标文件：

- `kuzhambu-apps/admin-web/src/pages/operations/backup-restore/backup-restore-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/backup-restore/backup-restore-types.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-types.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-page.test.tsx`

控件要求：

- 备份详情抽屉在 `backupStatus === "FAILED"` 时显示 `Alert type="error"`。
- 备份失败 `Alert` action 显示 `Button` “重新手动备份”，点击复用现有手动备份动作。
- 恢复详情抽屉在 `restoreStatus === "FAILED"` 时显示 `Alert type="error"`。
- 恢复失败 `Alert` description 必须展示 `preRestoreBackupId` 和 `failureReason`。
- 清理详情抽屉在 `cleanupStatus === "FAILED"` 时显示 `Alert type="warning"`。
- 清理 item 表中 `itemStatus === "FAILED"` 行显示失败原因，并提供 `KuzhambuTag type="danger"`。

验收点：

- 不新增自动重试。
- 所有失败提示都使用接口字段，不根据中文文案推断状态。

## Validation

后端：

```sh
cd kuzhambu-servers
mvn -pl biz/operations -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/operations -am test
```

前端：

```sh
cd kuzhambu-apps
npm --workspace kuzhambu-admin-web run format
npm run format:check
npm run lint
npm --workspace kuzhambu-admin-web run test -- operations
```

手动验收：

- 构造 1 次 `DOWN` 采样，看板顶部出现 `CRITICAL` 告警。
- 构造同组件连续 3 次 `DEGRADED` 采样，看板顶部出现 `WARNING` 告警。
- 构造同组件连续 2 次 `UP` 采样，健康类告警进入 `RECOVERED`。
- 构造超过 10 分钟无新采样，看板出现采集过期 `WARNING`。
- 构造备份失败，看板提供“重新手动备份”入口。
- 构造恢复失败，恢复详情展示恢复前快照 ID 和失败原因。
- 构造清理失败 item，清理详情展示失败 item 和失败原因。
- 构造长任务失败，任务详情展示失败提示。
- 无 `operations:health:manage` 权限时，页面只显示查看入口，不显示确认或恢复按钮。

## Review Checklist

- `operations_health_alert` 字段是否满足追溯、确认、恢复和页面动作需要。
- 每个任务是否保持 2-5 个主要文件的实现颗粒度。
- 前端控件和操作是否都由接口字段驱动。
- 是否避免自动重试和外部告警系统扩张。
- 是否避免保存敏感配置、凭证、完整脚本输出或完整业务输入。
