# Operations Design

## Purpose

本文档定义运维管理业务域的数据结构和接口设计，按 DDD 风格划分管理员查看平台运行状态和执行维护动作的业务边界、聚合和表结构。

Operations 是运维管理域，不拥有三库内容、AI 配置、搜索问答结果、分享链接业务规则、文件资源扫描或底层存储巡检。它消费各业务域统计和运行信息，负责运维仪表盘、周报月报、备份恢复、日志查询入口、健康检查、清理任务和长任务状态聚合展示。

## Business Fit Rules

- 业务能力：运维仪表盘、内容统计、AI 调用统计、访问统计、周报月报、备份恢复、日志筛选、健康检查、过期清理和长任务状态查看必须能直接对应运维业务。
- Operations 只聚合展示跨域统计，不反向拥有内容、AI、搜索、问答、分享或导出业务数据。
- 备份恢复和清理任务是运维动作，必须可追溯；通用业务审计仍归属 Audit。

## Module

- 模块名称：Operations
- 业务域：operations
- 对应需求文档：[OPERATIONS-REQUIREMENTS.md](../10-requirements/OPERATIONS-REQUIREMENTS.md)
- 后端 biz 子工程：`kuzhambu-servers/biz/kuzhambu-biz-operations`
- 后端 infra 子工程：`kuzhambu-servers/infra/kuzhambu-infra-operations`
- 后台接口入口：`kuzhambu-servers/interfaces/kuzhambu-admin-api`
- 前台接口入口：无
- 前端入口：`kuzhambu-apps/admin-web`
- Python worker 能力：可通过 HTTP 提供报表渲染或备份文件校验辅助，业务结果由 Java 主系统控制

## Business Boundary

- 本模块负责：运维仪表盘、统计聚合、周报月报生成记录、报表导出记录、系统启动备份记录、24 小时自动备份记录、手动备份、备份恢复、恢复前快照、写入阻断状态、日志查看入口、健康检查记录、运行指标展示、过期备份清理、过期分享清理、过期草稿清理、过期导出产物清理和长任务状态聚合。
- 本模块不负责：业务内容编辑、AI 提示词和模型配置管理、搜索或问答结果生成、文件资源扫描、孤立文件清理、底层存储巡检、分享访问控制、内容导出业务生成。
- 依赖的其他业务域能力：Core/Auth 提供 admin 权限；Audit 记录关键维护动作；AI Config Prompt 提供 AI 调用和模型状态统计；Search 提供搜索日志；Sharing 提供过期分享和访问统计；三类内容域提供内容统计、导出产物和草稿清理对象；Storage 保存备份或报表文件对象。
- 对外提供的业务能力：管理员运维仪表盘、报表导出、备份恢复、日志查询、健康状态、清理任务和跨域任务状态查询。

## DDD Model

### Aggregates

- `OperationsDashboard`：运维仪表盘聚合视图，由跨域统计数据实时组装，不持久拥有业务统计明细。
- `OperationsReport`：周报或月报生成记录，保存报表周期、格式、状态和文件对象引用。
- `OperationsBackup`：备份记录，保存备份类型、触发方式、文件对象引用、校验信息、状态和保留期限。
- `OperationsRestore`：恢复记录，保存目标备份、恢复前快照、写入阻断状态、恢复结果和失败原因。
- `OperationsCleanupJob`：清理任务，保存清理类型、执行结果、数量和单项失败。
- `OperationsHealthCheck`：健康检查记录，保存组件、状态、耗时和检查信息。

### Value Objects

- `OperationsReportType`：`WEEKLY` / `MONTHLY`。
- `OperationsReportFormat`：`PDF` / `HTML`。
- `OperationsJobStatus`：`PENDING` / `RUNNING` / `COMPLETED` / `FAILED` / `CANCELLED`。
- `OperationsBackupType`：`STARTUP` / `SCHEDULED` / `MANUAL` / `PRE_RESTORE`。
- `OperationsCleanupType`：`BACKUP` / `SHARING` / `DRAFT` / `EXPORT_ARTIFACT` / `RUNNING_LOG`。
- `OperationsHealthStatus`：`UP` / `DEGRADED` / `DOWN`。
- `OperationsTaskSource`：`AI_REFINEMENT` / `DATA_REFINEMENT` / `KNOWLEDGE_GRAPH` / `SHARING` / `CONTENT_EXPORT`。

## Data Model

Operations 表固定使用 `operations_` 前缀。所有对外标识使用 ULID，数据库内部可使用 `bigint id`。

### operations_report

保存周报和月报生成记录。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `report_id` | `reportId` | 是 | 报表 ULID |
| `report_type` | `reportType` | 是 | 周报或月报 |
| `format` | `format` | 是 | PDF 或 HTML |
| `period_start` | `periodStart` | 是 | 统计周期开始 |
| `period_end` | `periodEnd` | 是 | 统计周期结束 |
| `storage_object_id` | `storageObjectId` | 否 | 报表文件对象 ULID |
| `report_status` | `reportStatus` | 是 | 生成状态 |
| `failure_reason` | `failureReason` | 否 | 失败原因 |
| `requester_user_id` | `requesterUserId` | 是 | 发起管理员 |
| `requested_at` | `requestedAt` | 是 | 发起时间 |
| `completed_at` | `completedAt` | 否 | 完成时间 |

约束：
- `report_id` 唯一。
- 报表文件由 Storage 保存，本表只保存对象引用。
- 统计数据展示和报表生成仅 admin 可用。

### operations_backup

保存备份记录。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `backup_id` | `backupId` | 是 | 备份 ULID |
| `backup_type` | `backupType` | 是 | 启动、定时、手动或恢复前快照 |
| `backup_status` | `backupStatus` | 是 | 备份状态 |
| `storage_object_id` | `storageObjectId` | 否 | 备份文件对象 ULID |
| `file_name` | `fileName` | 否 | 备份文件名 |
| `file_size_bytes` | `fileSizeBytes` | 否 | 备份文件大小 |
| `checksum` | `checksum` | 否 | 备份校验值 |
| `failure_reason` | `failureReason` | 否 | 失败原因 |
| `requester_user_id` | `requesterUserId` | 否 | 手动备份发起管理员 |
| `started_at` | `startedAt` | 是 | 开始时间 |
| `completed_at` | `completedAt` | 否 | 完成时间 |
| `expires_at` | `expiresAt` | 是 | 保留到期时间 |

约束：
- `backup_id` 唯一。
- 备份保留期限固定为 30 天。
- 导出产物是临时产物，不进入数据备份范围。
- 恢复数据前必须先创建 `backup_type=PRE_RESTORE` 的恢复前快照。

### operations_restore

保存恢复记录。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `restore_id` | `restoreId` | 是 | 恢复 ULID |
| `backup_id` | `backupId` | 是 | 目标备份 ULID |
| `pre_restore_backup_id` | `preRestoreBackupId` | 是 | 恢复前快照 ULID |
| `restore_status` | `restoreStatus` | 是 | 恢复状态 |
| `write_block_enabled` | `writeBlockEnabled` | 是 | 是否已阻断新写入 |
| `failure_reason` | `failureReason` | 否 | 失败原因 |
| `requester_user_id` | `requesterUserId` | 是 | 发起管理员 |
| `started_at` | `startedAt` | 是 | 开始时间 |
| `completed_at` | `completedAt` | 否 | 完成时间 |

约束：
- `restore_id` 唯一。
- 恢复期间必须阻止新的写入操作。
- 恢复失败时必须保留恢复前快照。

### operations_cleanup_job

保存清理任务。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `cleanup_id` | `cleanupId` | 是 | 清理任务 ULID |
| `cleanup_type` | `cleanupType` | 是 | 清理类型 |
| `cleanup_status` | `cleanupStatus` | 是 | 清理状态 |
| `total_count` | `totalCount` | 是 | 总数 |
| `success_count` | `successCount` | 是 | 成功数 |
| `failed_count` | `failedCount` | 是 | 失败数 |
| `requester_user_id` | `requesterUserId` | 否 | 手动发起管理员 |
| `started_at` | `startedAt` | 是 | 开始时间 |
| `completed_at` | `completedAt` | 否 | 完成时间 |

约束：
- `cleanup_id` 唯一。
- 清理操作必须可追溯。
- 过期分享、草稿和导出产物的业务状态更新必须通过对应业务域能力完成。

### operations_cleanup_item

保存清理任务单项结果。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `cleanup_item_id` | `cleanupItemId` | 是 | 清理单项 ULID |
| `cleanup_id` | `cleanupId` | 是 | 清理任务 ULID |
| `target_type` | `targetType` | 是 | 目标类型 |
| `target_id` | `targetId` | 是 | 目标业务 ULID |
| `item_status` | `itemStatus` | 是 | `SUCCESS` / `FAILED` |
| `failure_reason` | `failureReason` | 否 | 失败原因 |
| `processed_at` | `processedAt` | 是 | 处理时间 |

约束：
- `cleanup_item_id` 唯一。
- 单项失败不得阻断其他对象清理。

### operations_health_check

保存健康检查记录。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `check_id` | `checkId` | 是 | 检查 ULID |
| `component` | `component` | 是 | 组件名称 |
| `health_status` | `healthStatus` | 是 | 健康状态 |
| `latency_ms` | `latencyMs` | 否 | 检查耗时 |
| `message` | `message` | 否 | 检查信息 |
| `checked_at` | `checkedAt` | 是 | 检查时间 |

约束：
- `check_id` 唯一。
- 健康检查展示仅 admin 可用。

## Application Layer

- `OperationsDashboardApplicationService`：内容、访问、AI 调用、功能使用频率、热门内容和趋势统计聚合。
- `OperationsReportApplicationService`：周报月报生成、PDF/HTML 导出、报表结果查询和 Storage 写入。
- `OperationsBackupApplicationService`：启动备份、24 小时自动备份、手动备份、备份列表和备份保留清理。
- `OperationsRestoreApplicationService`：恢复前快照、写入阻断、备份恢复、恢复结果记录和失败处理。
- `OperationsLogApplicationService`：运行日志查看、级别筛选、模块筛选、时间筛选和关键词检索。
- `OperationsHealthApplicationService`：健康检查、运行指标采集和组件状态展示。
- `OperationsCleanupApplicationService`：过期备份、过期分享、过期草稿、过期导出产物和运行日志清理。
- `OperationsTaskStatusApplicationService`：跨域长任务和批量操作状态聚合。

事务边界：
- 恢复前快照成功后才能进入恢复流程。
- 恢复流程必须先启用写入阻断，再执行数据恢复。
- 恢复完成或失败后必须释放写入阻断并记录结果。
- 清理任务按目标逐项处理，单项失败不得回滚已成功单项。
- 周报月报文件写入 Storage 后再标记为完成。

## Interface Layer

后台接口固定使用 `/api/admin/operations/**`。Operations 不提供 portal 接口。

### Dashboard API

- `GET /operations/dashboard`：读取运维仪表盘。
- `GET /operations/statistics/content`：内容数量、增长趋势、翻译覆盖率、配图覆盖率和标签覆盖率。
- `GET /operations/statistics/usage`：访问量、活跃用户、功能使用频率和热门内容排行。
- `GET /operations/statistics/ai`：AI 调用次数、延迟、失败率和成本统计。

### Report API

- `POST /operations/reports`：生成周报或月报。
- `GET /operations/reports`：查询报表列表。
- `GET /operations/reports/{reportId}`：查询报表结果。

### Backup And Restore API

- `POST /operations/backups`：触发手动备份。
- `GET /operations/backups`：查询备份列表。
- `POST /operations/restores`：发起恢复。
- `GET /operations/restores/{restoreId}`：查询恢复结果。

### Log And Health API

- `GET /operations/logs`：按级别、模块、时间和关键词查询运行日志。
- `GET /operations/health`：读取健康检查结果。
- `GET /operations/metrics`：读取运行指标。

### Cleanup And Task API

- `POST /operations/cleanup-jobs`：触发清理任务。
- `GET /operations/cleanup-jobs`：查询清理任务列表。
- `GET /operations/cleanup-jobs/{cleanupId}`：查询清理任务详情。
- `GET /operations/tasks`：查询长任务和批量操作运行状态。

## Infrastructure Layer

- Repository：`OperationsReportRepository`、`OperationsBackupRepository`、`OperationsRestoreRepository`、`OperationsCleanupJobRepository`、`OperationsCleanupItemRepository`、`OperationsHealthCheckRepository`。
- Mapper：按表建立 MyBatis Mapper。
- PersistenceAssembler：每个聚合根使用独立 `*PersistenceAssembler`。
- 外部客户端：Core/Auth Application Service、Audit Application Service、Storage Application Service、AI Config Prompt Application Service、Search Application Service、Sharing Application Service、Sancai Application Service、Wangqi Application Service、Ming Customs Application Service、AI Refinement Application Service、Data Refinement Application Service、Knowledge Graph Application Service。
- 文件日志读取：通过受控日志读取组件访问运行日志，不直接暴露服务器任意文件路径。
- 调度：启动备份、24 小时自动备份和清理任务由 Java 主系统调度。

## Data Ownership

- 本模块拥有：`operations_report`、`operations_backup`、`operations_restore`、`operations_cleanup_job`、`operations_cleanup_item`、`operations_health_check`。
- 本模块只读引用：各业务域统计、任务状态、日志摘要和 Storage 文件对象。
- 禁止跨域直接访问：不得直接修改内容表、AI 配置表、搜索日志、问答会话、分享链接、导出记录或 Storage 物理文件；必须通过对应业务域能力执行清理或状态变更。
- Flyway 脚本归属：`kuzhambu-admin-api` 的 `db/migration/V1__init.sql`，按 Operations 分段。

## Observability

- 运行日志：记录备份失败、恢复失败、写入阻断异常、日志读取异常、健康检查异常和清理失败。
- 访问日志：由接口层统一记录。
- 审计日志：手动备份、恢复、清理任务、报表导出和日志查看应形成业务审计。
- 关键指标：备份成功率、恢复成功率、清理成功率、日志查询次数、健康检查失败数、报表生成次数和长任务失败数。

## Acceptance

- 管理员能触发手动备份并看到结果。
- 管理员能查看备份列表并发起恢复。
- 恢复数据前必须生成恢复前快照。
- 恢复期间新的写入操作被阻断。
- 管理员能查看内容、访问、AI 调用和热门内容统计。
- 管理员能生成并导出周报或月报。
- 管理员能按级别、模块、时间和关键词检索运行日志。
- 系统关键异常可在日志中追溯。
- 管理员能触发过期备份、过期分享、过期草稿和过期导出产物清理。
- 管理员能查看长任务和批量操作的运行状态、成功数、失败数和失败原因。
