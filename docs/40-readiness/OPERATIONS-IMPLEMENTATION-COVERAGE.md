# Operations Implementation Coverage

## Purpose

本文档记录 Operations 域需求在当前仓库的实现覆盖状态，用于识别已形成闭环的能力，以及仍需补齐的 application/interface/运行态缺口。

本文档不替代 `docs/10-requirements/OPERATIONS-REQUIREMENTS.md` 和 `docs/30-designs/OPERATIONS-DESIGN.md`。

## Status Definition

- `已完成`：对应需求可在仓库中形成可追溯交付物并具备运行时闭环。
- `部分完成`：核心模型/持久化与部分链路已就绪，仍缺少关键层或接口闭环。
- `未完成`：当前仓库未形成可执行实现、接口、测试或关键功能链路。
- `外部依赖`：能力边界不属于 Operations，或依赖其他域完成。

## Current Baseline

部分完成：

- 统一运维首页已完成基础入口，系统日志与审计入口已补齐权限视图与跳转闭环。

已完成：

- Operations cleanup 目标类型扩展闭环已形成当前阶段可运行交付：
  - `cleanup` 已完成领域模型、持久化实现、应用层、接口层、admin 列表、详情查询、详情 item 展示、真实执行、调度化清理和长期规则策略。
  - 清理任务已覆盖 `EXPIRED_BACKUP`、`EXPIRED_EXPORT`、`EXPIRED_SHARE`、`EXPIRED_DRAFT`、`EXPIRED_REPORT`、`EXPIRED_HEALTH_CHECK`、`EXPIRED_LONG_TASK`。
  - 清理明细已覆盖 `backup`、`export`、`share`、`draft`、`report`、`health-check`、`long-task` target type。
  - 运行态清理保守处理：报表只清理 `SUCCEEDED` / `FAILED`，长任务只清理非 `RUNNING`，健康检查按采集时间清理历史记录。
  - `operations_cleanup_job` 与 `operations_cleanup_item` 会记录任务汇总、目标明细、item 状态、失败原因和处理时间；任一 item 失败时 job 进入 `FAILED` 并联动健康告警。
  - `.env.example`、`deploy/.env.example` 与 `admin-starter application.yml` 已同步新增 report、health check、long task policy 配置。
  - `admin-web` `/operations/cleanup` 已展示全部清理类型，并在列表与详情中展示失败原因、失败项和告警入口。
- Operations Dashboard 真实跨域 summary 已形成当前阶段可运行交付：
  - `OperationsDashboardSummaryGateway` 已统一读取 `Classics / AI / Discovery / Knowledge` summary facade，overview 不再以稳定 `0` 或空数组作为内容、AI、搜索、标签指标占位。
  - `OperationsDashboardApplicationService` 已按 `periodType` 生成跨域 `periodStart`、`periodEnd` 和 `bucketType`；`WEEK` 使用 `DAY` bucket，`MONTH` 使用 `WEEK` bucket，`CUSTOM` 在 31 天内使用 `DAY`、超过 31 天使用 `WEEK`。
  - `OperationsDashboardOverview` 已映射真实 `contentCount`、`publishedContentCount`、`draftContentCount`、`shareVisitCount`、`aiInvocationCount`、`aiSuccessCount`、`aiFailureCount`、`aiAvgLatencyMs`、`aiTotalCostAmount`、`searchCount`、`qaCount`、`avgSearchLatencyMs`、`tagCoverageRate`、趋势序列和排行数据。
  - `admin-web` `/operations/dashboard` 已展示真实内容访问、AI 调用与成本、搜索/问答、标签覆盖、内容增长趋势、AI 能力排行和空状态。
- 健康指标闭环已形成当前阶段可运行交付：
  - `operations_health_check` 已补齐 `probe_source`、`probe_target`、`details_json` 字段与 `idx_operations_health_probe` 索引。
  - `HealthCheckRecord` / `HealthCheckDO` / persistence assembler 已同步采集来源、目标和诊断 JSON。
  - 健康 summary/page 已返回采集来源信息，page 记录返回诊断 JSON。
  - `OperationsHealthProbe`、`LocalOperationsHealthProbe`、`OperationsHealthCollector` 已提供后台采集写入入口，健康状态统一为 `UP`、`DEGRADED`、`DOWN`。
  - `POST /api/operations/health/trend` 已支持按 `component`、`probeSource`、`periodStart`、`periodEnd`、`bucketType` 聚合 `HOUR` / `DAY` 趋势。
  - `admin-web` `/operations/dashboard` 已展示健康摘要、健康趋势和健康明细抽屉；`/operations/tasks` 不再请求健康摘要。
- 健康告警与异常恢复提示已形成当前阶段可运行交付：
  - `operations_health_alert` 已落库，精确字段包括 `alert_id`、`component`、`alert_type`、`alert_level`、`alert_status`、`source_ref_type`、`source_ref_id`、`latest_check_id`、`message`、`suggestion`、`recovery_action`、`recovery_target`、`first_triggered_at`、`last_triggered_at`、`acked_at`、`acked_by_user_id`、`recovered_at`、`failure_reason`。
  - `HealthAlertRecord`、`HealthAlertRepository`、`HealthAlertDO`、`HealthAlertMapper`、`HealthAlertRepositoryImpl` 已完成领域与持久化闭环。
  - `OperationsHealthAlertStrategy` 已在健康采集后按 `DOWN`/`DEGRADED` 生成或恢复告警；`OperationsHealthAlertPolicyProperties` 已提供阈值配置入口。
  - `HealthAlertApplicationService` 已提供分页、确认、恢复能力；`OperationsHealthAlertAdminController` 已暴露 `/api/operations/health/alerts/page`、`/api/operations/health/alerts/ack`、`/api/operations/health/alerts/recover`。
  - 健康告警恢复已支持自动化动作编排：`RUN_MANUAL_BACKUP` 会触发手动备份并写入备份台账，`RUN_RESTORE` 会从 `recoveryTarget.backupId` 触发恢复执行，`OPEN_*` 与 `NONE` 仍只标记告警恢复；自动动作失败时不改写告警为 `RECOVERED`。
  - 恢复执行结果已同步写入长任务台账：恢复创建后新增 `operations_long_task_snapshot`，字段口径为 `source_domain=operations`、`task_type=RESTORE`、`task_key=restore:<restoreId>`、`task_status=RUNNING`，恢复完成后更新为 `SUCCEEDED` 或 `FAILED` 并写入成功/失败计数、失败原因和完成时间。
  - 失败来源已联动健康告警：长任务、备份、恢复、清理失败会记录 `sourceRefType`、`sourceRefId`、`failureReason` 和处置建议。
  - `OperationsDashboardOverview` 已补齐 `activeAlertCount`、`criticalAlertCount`、`warningAlertCount`、`highestAlertLevel`、`latestAlert`；`admin-web` `/operations/dashboard` 已展示健康告警横幅、告警抽屉、确认/标记恢复按钮、健康明细关联告警和来源跳转。
  - `admin-web` `/operations/health` 关联告警抽屉已接入 `确认` 与 `恢复` 操作，按 `operations:health:manage` 控制按钮禁用态；恢复操作提供二次确认，成功后刷新 health alerts、task page 和 restore page 查询。
  - `admin-web` `/operations/tasks`、`/operations/backup-restore`、`/operations/cleanup` 已在失败行和详情抽屉展示失败原因与“查看告警”入口。
- 运维入口日志审计闭环已形成：`/operations/dashboard` 已新增 `system:log:view` 与 `audit:view` 权限入口、`系统日志` 与 `审计日志` 点击跳转到对应页面，`admin-web` 对应页面增加目标页权限门禁与接口请求拦截，系统日志 `@HasPermission` 已收敛为 `system:log:view`，`e2e` 与单元测试覆盖入口可见性、无权限隐藏与跳转约束。
- 报表闭环已形成当前阶段可运行交付：
  - `report` 已完成 domain + persistence，并补齐 `requestId`、`traceId`、`templateVersion`、`artifactFilename` 字段。
  - `kuzhambu-operations-application` 已完成 `generate/page/detail` 应用服务、任务执行单元、状态流转、worker 调用、Storage 产物回写。
  - `kuzhambu-operations-interface` 已完成 admin `generate/page/detail` 接口、独立 `XxxResponse`、`operations:report:view` / `operations:report:generate` 权限约束。
  - `Classics / AI / Discovery / Knowledge` 已提供按统一 summary 规格暴露的 `@LayerPublicApi` 聚合读取入口，周报按日、月报按周的 bucket 规则已固化到代码。
  - 已存在 application / infra / interface / 跨域 summary 对应测试，报表链路具备基本验证闭环。
- 备份恢复闭环已形成当前阶段可运行交付：
  - `backup`、`restore` 已完成 domain + persistence + application + interface + admin page + 菜单权限种子数据。
  - `kuzhambu-operations-application` 已完成手动备份执行、启动自动备份、每日 2:00 自动备份调度、恢复前 `PRE_RESTORE` 快照创建、备份/恢复分页与详情查询，以及脚本执行结果回写台账。
  - 自动备份由 `OperationsBackupScheduler` 调度，默认配置为 `startupEnabled=true`、`scheduleEnabled=true`、`dailyCron=0 0 2 * * ?`；环境变量为 `KUZHAMBU_OPERATIONS_BACKUP_STARTUP_ENABLED`、`KUZHAMBU_OPERATIONS_BACKUP_SCHEDULE_ENABLED`、`KUZHAMBU_OPERATIONS_BACKUP_DAILY_CRON`。
  - `operations_backup` 已精确使用 `backup_type` 区分 `MANUAL` / `AUTO` / `PRE_RESTORE`，自动备份 `requester` 为空；`BackupRequestContext` 增加 `source`、`reason`、`targetRestoreId` 以支撑互斥与来源追踪。
  - `operations_restore` 已精确补齐 `restore_mode`、`write_block_enabled`、`write_block_started_at`、`write_block_released_at` 字段；`RestoreMode` 支持 `REAL` / `DRILL`。
  - 恢复执行期间由 `OperationsRestoreWriteBlocker` 联动 `RestoreWriteBlockState`，通过 `RestoreWriteBlockFilter` 对非只读 Web 请求返回 `RESTORE_WRITE_BLOCKED`，并在恢复结束后释放阻断状态。
  - `kuzhambu-operations-interface` 已完成 admin `execute/page/detail` 接口、独立 `XxxResponse`、`operations:backup:view` / `operations:backup:execute` / `operations:restore:view` / `operations:restore:execute` 权限约束。
  - `admin-web` 已完成 `/operations/backup-restore` 页面、台账展示、详情抽屉、手动备份与恢复动作触发；恢复卡片提供“真实恢复”和“恢复演练”两个按钮，详情抽屉展示恢复模式、写阻断状态、写阻断开始时间和释放时间。
  - `deploy/scripts/restore-business-data.sh` 已支持 `RESTORE_MODE=REAL|DRILL`；演练模式执行 SQL 语法、checksum、Storage 归档与白名单校验，不执行数据导入或对象覆盖。
  - `db/data-source/system.json` 与 `db/data/system.sql` 已对齐 `Operations/备份恢复` 菜单、路由与权限口径。
- 备份恢复部署与执行器配套已形成：
  - 已完成专项设计文档、Docker Compose 挂载调整、`admin-starter` 下的备份/恢复/清理脚本、业务表白名单和校验输出格式。

- 按权限裁剪聚合图表已完成：
  - 已完成后端 `operations dashboard` 跨域 summary 的权限域入参裁剪、字段级空值语义、以及 interface 层空语义透传；
  - 已完成前端基于 `operations` 权限矩阵的图表/指标卡/排行/告警/入口/空态裁剪与查询触发控制。

## Requirement Coverage Matrix

| 需求项 | 状态 | 已完成部分 | 未完成部分 | 责任域 |
| --- | --- | --- | --- | --- |
| 仪表盘与聚合展示 | 已完成 | 已完成 `OperationsDashboardApplicationService`、`POST /api/operations/dashboard/overview`、`OperationsDashboardOverviewResponse`、`/operations/dashboard` 前端页面、周期切换、刷新、指标卡、趋势区、排行区、健康明细抽屉和运维入口；已通过 `OperationsDashboardSummaryGateway` 接入 Classics 内容/访问、AI 调用/成本/能力排行、Discovery 搜索/问答/平均搜索耗时、Knowledge 标签覆盖/增长/排行真实 summary；已完成按 `operations` 权限的图表与入口裁剪 | 无 | Operations, Admin Web |
| 报表 | 已完成 | 已完成周报/月报任务生成、HTML/PDF worker 渲染、Storage 产物回写、报表记录分页/详情查询、状态流转、独立 response、`operations:report:view` / `operations:report:generate` 权限控制，以及跨域 summary 聚合读取 | admin 页面接入与专属下载表现不在本矩阵范围内；后续如增加调度报表可另行扩展 | Operations |
| 备份与恢复 | 已完成 | 已完成 `BackupRecord`、`RestoreRecord`、各自 persistence、手动备份执行、启动自动备份、每日 2:00 自动备份、恢复前 `PRE_RESTORE` 快照创建、恢复期间 Web 写阻断、`REAL` / `DRILL` 恢复台账、备份/恢复 `execute/page/detail` 接口、`admin-web` 备份恢复页面与详情字段、菜单与权限种子，以及脚本执行结果回写 | 无 | Operations |
| 清理任务 | 已完成 | `CleanupJob` / `CleanupItem` 已完成领域模型与持久化，应用层 `execute/page/detail` 与 admin 接口已上线，前端清理台账页已接通，`operations:cleanup:view/execute` 与页面回归测试已存在；真实执行覆盖过期备份、Classics 过期导出、过期分享、草稿分享、过期报表、过期健康检查和过期长任务快照；详情页可展示每条 cleanup item 的目标、状态、失败原因和处理时间；已完成 `kuzhambu.operations.cleanup.schedule` 调度配置、`OperationsCleanupScheduler` 默认每日 `03:30` 自动执行、启动清理默认关闭、按固定顺序执行七类 policy、单 policy disabled 跳过、单类型异常不阻断后续类型、自动任务 `requesterUserId = null` 识别为系统自动、admin 清理页自动/人工触发来源展示，以及 `.env.example` / `deploy/.env.example` 运行时样例；若清理目标涉及导出产物对象，其底层生命周期已可复用 Storage 自动 orphan 清理能力 | 无 | Operations |
| 健康检查与运行状态 | 已完成 | `HealthCheckRecord`、`LongTaskSnapshot` 已完成 domain + persistence + application + interface；健康记录已包含 `probeSource`、`probeTarget`、`detailsJson`，并支持按 `component`、`healthStatus`、`probeSource`、`probeTarget`、`checkedAtStart`、`checkedAtEnd` 细分页筛选；已具备本地健康采集器、配置化 HTTP 外部探针、健康摘要、组件分页、健康趋势、长任务分页与详情查询；`operations_health_alert`、`HealthAlertRecord`、`OperationsHealthAlertStrategy`、`HealthAlertApplicationService`、`OperationsHealthAlertAdminController` 已完成健康告警策略、异常状态记录、确认、恢复、自动恢复动作编排和按 `latestCheckId` 查询关联告警接口；`RUN_MANUAL_BACKUP` 可触发备份台账写入，`RUN_RESTORE` 可触发恢复台账和长任务快照写入；长任务、备份、恢复、清理失败已联动告警来源和失败原因；`admin-web` 看板已消费健康摘要/趋势/告警，任务、备份恢复和清理页面已展示失败提示与告警入口，`/operations/health` 已提供健康细分页筛选控件、结果表格、诊断 JSON 抽屉、关联告警抽屉、`确认` 按钮、`恢复` 按钮、权限禁用态和恢复二次确认，菜单种子已补齐 `operations:health:view` 权限入口 | 无 | Operations, Admin Web |
| 运维入口 | 已完成 | 已完成 `/operations/dashboard` 统一入口，补齐 `系统日志` 与 `审计日志` 运维入口与跳转、目标页 `hasPermission` 门禁、系统日志接口权限收敛为 `system:log:view`，并补齐 `operations-entry-*`、菜单 icon key 与 E2E 验证；`/operations/dashboard` 与系统/审计日志页面形成闭环校验。 | 无 | Operations, Admin Web, System |
| 台账记录 | 已完成 | report、backup、restore、cleanup 已完成 domain + persistence + application + interface + admin page；cleanup 真实执行会写入 job summary 和 item 明细；health、health alert 与 long task 已完成 domain + persistence + application + interface，健康来源字段、趋势查询、告警记录和看板消费已落地；健康告警恢复已能编排 `RUN_MANUAL_BACKUP` 与 `RUN_RESTORE`，恢复执行结果已写入 `operations_restore` 与 `operations_long_task_snapshot`，admin-web 健康页已提供确认、恢复、权限禁用和恢复结果刷新闭环 | 无 | Operations |
| TODO 与治理文档清理 | 已完成 | 本阶段已按任务提交记录完成验证、main 同步、Implementation Coverage 更新与 TODO 收口；`RUNBOOK-OPERATIONS-CLEANUP-TARGET-TYPES.md` 临时 RUNBOOK 已删除 | 无 | Governance |

## Follow-up Backlog

### B1 Operations 非报表剩余应用层闭环

状态：已完成  
目标：清理任务真实执行逻辑、调度化清理与长期规则策略、健康组件采集来源入库、健康告警策略、自动化恢复动作编排、失败来源联动、统一运维入口基础聚合、跨域真实 summary 和 cleanup 运行态来源接入已补齐。

### B2 Operations 剩余接口与入口

状态：已完成  
目标：健康细分页面、健康探针结果入口、健康告警确认/恢复操作、恢复结果任务台账追踪、权限约束和 cleanup 新增运行态来源入口已补齐。

### B3 Cleanup 持久化补齐

状态：已完成  
目标：清理台账持久化基础设施、真实清理执行规则、调度器入口和长期 policy 配置已就绪。

### B4 运行态接入

状态：已完成
目标：自动备份调度、恢复期间真实写入阻断、配置化 HTTP 外部健康探针、健康告警、失败来源提示、自动化恢复动作编排和 cleanup 运行态目标清理已补齐。

### B5 剩余非报表任务交付验证

状态：已完成
目标：cleanup 关键链路已补齐真实执行、调度化执行上下文、长期 policy、admin 自动/人工来源展示、详情 item 回归和运行态目标类型扩展；health/task 看板消费、接口趋势、健康告警策略、自动化恢复动作编排、恢复结果任务台账追踪、失败来源联动、健康细分页、外部 HTTP 探针和页面失败提示已补齐契约；backup/restore 已覆盖自动备份、真实恢复、恢复演练、写阻断字段和失败原因可追溯能力；跨域 Dashboard 已接入真实 summary 并完成后端和 admin-web 定向验证。
