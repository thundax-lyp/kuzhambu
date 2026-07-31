# Operations Design

## Purpose

本文档定义 Operations 域设计，覆盖运营运维仪表盘、跨域统计聚合、报表生成记录、备份恢复入口、清理任务入口、健康检查查看和运行状态追踪。

## Module

```text
kuzhambu-servers/biz/operations/
  kuzhambu-operations-interface/
  kuzhambu-operations-application/
  kuzhambu-operations-domain/
  kuzhambu-operations-infra/
```

## Business Boundary

Operations 是独立运营运维域，不属于 System，也不放入 starter。Operations 面向 admin 提供运营运维控制台能力，负责聚合其他业务域统计结果，并维护本域发起或管理的运维动作台账。

Operations 可以读取和聚合其他业务域数据，但不拥有其他业务域主事实，不复制其他业务域主表结构，也不承担通用系统日志或业务审计真相源职责。

## Design Principles

- 聚合统计与运维台账分离建模。
- 只为本域独立生命周期对象建表。
- 其他业务域主事实只读不写。
- System 提供权限、日志和审计能力，Operations 只提供访问入口和聚合视图。
- render workers 只负责报表产物生成，不承载业务权限、业务审计或运维台账。

## DDD Model

聚合读模型：

- `DashboardView`
- `UsageMetricsView`
- `ReportSnapshot`
- `RuntimeOverview`
- `LogEntryPoint`
- `AuditEntryPoint`

自有台账模型：

- `ReportRecord`
- `BackupRecord`
- `RestoreRecord`
- `CleanupJob`
- `CleanupItem`
- `HealthCheckRecord`
- `LongTaskSnapshot`

## Data Model

表名前缀统一使用 `operations_`。

Operations 只为自有台账建表；聚合展示结果默认不落本域主表，除非确实需要稳定追溯的运行状态快照。

核心表：

- `operations_report`
- `operations_backup`
- `operations_restore`
- `operations_cleanup_job`
- `operations_cleanup_item`
- `operations_health_check`
- `operations_long_task_snapshot`

表职责：

- `operations_report`：记录报表请求、统计周期、生成状态、失败原因和导出产物定位信息。
- `operations_backup`：记录自动或手动备份的状态、结果、产物和保留期限。
- `operations_restore`：记录恢复来源备份、恢复前快照、写入阻断状态和恢复结果。
- `operations_cleanup_job`：记录一次清理任务的类型、时间范围、执行状态和汇总结果。
- `operations_cleanup_item`：记录清理任务下各目标对象的处理结果。
- `operations_health_check`：记录关键组件健康状态、耗时和说明信息。
- `operations_long_task_snapshot`：记录需要在 Operations 中稳定追溯的长任务或批量任务状态快照。

不建表对象：

- 仪表盘聚合结果。
- 日志正文。
- 业务审计正文。
- 其他业务域主对象的统计事实。

## Read And Write Split

读侧：

- 从 Classics、AI、Discovery、Knowledge、Storage 和 System 读取聚合所需统计结果或入口信息。
- 读取本域报表、备份、恢复、清理、健康检查和长任务台账。
- 读取 System 提供的日志和审计访问能力，但不复制日志正文和审计正文。

写侧：

- 只写 `operations_*` 自有台账。
- 不直接写其他业务域主表。
- 不直接写 `system_log`、`system_audit_log` 或 `system_audit_meta`。

## Application Layer

- `OperationsDashboardApplicationService`
- `ReportApplicationService`
- `BackupApplicationService`
- `RestoreApplicationService`
- `CleanupApplicationService`
- `HealthCheckApplicationService`
- `LongTaskApplicationService`
- `OperationsEntryPointApplicationService`

Application 层职责：

- 编排跨域统计读取。
- 生成报表快照并调用 render workers。
- 发起和记录备份、恢复、清理等运维动作。
- 汇总长任务和批量任务运行状态。
- 提供日志与审计访问入口编排。

## Interface Layer

Admin 入口：

- 仪表盘统计和趋势图。
- 周报和月报生成、导出与历史记录。
- 备份列表、手动备份和恢复入口。
- 清理任务入口、结果查看和失败项追踪。
- 健康检查和运行指标查看。
- 长任务和批量任务状态查看。
- System 日志和审计的访问入口。

Portal 入口：

- 不提供 Operations 能力。

## Infrastructure Layer

- Repository 持久化 Operations 自有台账表。
- Statistics Gateway 读取其他业务域聚合所需数据。
- Worker Client 调用 render workers 生成 HTML 或 PDF 报表。
- Runtime Probe 读取健康检查和运行指标。
- Entry Point Adapter 对接 System 日志和审计查询入口。

## Data Ownership

Operations 是 `operations_*` 表的唯一写入方。

归属约束：

- Classics 持有内容、翻译、配图、导出、发布任务、Portal 访问和草稿事实。
- AI 持有 AI 调用事实。
- Discovery 持有搜索和问答事实。
- Knowledge 持有标签和图谱质量事实。
- Storage 持有文件对象事实。
- System 持有用户、权限、认证、系统日志和业务审计事实。

## Observability

- 报表、备份、恢复、清理和健康检查动作必须记录操作者、时间、结果和失败原因。
- 恢复操作必须记录恢复前快照。
- 长任务和批量任务状态必须可被管理员追溯。
- Operations 自身写操作仍应复用 System 提供的业务审计能力。

## Acceptance

- Operations 有独立 domain、application、interface 和 infra，不污染 starter。
- 看板聚合不复制其他业务域主事实。
- 日志和审计只作为入口能力集成，不在 Operations 内部重建真相源。
- 自有台账对象都能映射到明确的领域模型和 `operations_*` 表。
