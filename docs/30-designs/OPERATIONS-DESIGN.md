# Operations Design

## Purpose

本文档定义 Operations 域设计，覆盖运营运维看板、报表、任务台账、维护操作记录、备份恢复入口和运行状态查看。

## Module

```text
kuzhambu-servers/biz/operations/
  kuzhambu-operations-interface/
  kuzhambu-operations-application/
  kuzhambu-operations-domain/
  kuzhambu-operations-infra/
```

## Business Boundary

Operations 是独立运营运维域，不属于 System，也不放入 starter。Operations 可以聚合其他业务域统计，但不拥有其他业务域的业务事实。

## DDD Model

- `DashboardView`
- `ReportJob`
- `ReportExport`
- `MaintenanceTask`
- `MaintenanceRecord`
- `BackupRecord`
- `RestoreSnapshot`
- `RuntimeHealthSnapshot`
- `LongTaskSnapshot`

## Data Model

表名前缀统一使用 `operations_`。

核心表：

- `operations_report_job`
- `operations_report_export`
- `operations_maintenance_task`
- `operations_maintenance_record`
- `operations_backup_record`
- `operations_restore_snapshot`
- `operations_health_snapshot`
- `operations_long_task_snapshot`

聚合统计结果可以来自其他业务域查询，不复制其他业务域主事实。

## Application Layer

- `OperationsDashboardApplicationService`
- `ReportApplicationService`
- `MaintenanceApplicationService`
- `BackupApplicationService`
- `RuntimeHealthApplicationService`
- `LongTaskApplicationService`

Application 层负责编排跨域统计读取、报表生成、维护任务触发、备份恢复入口和运行状态聚合。

## Interface Layer

Admin 入口：

- 看板统计。
- 周报和月报生成及导出。
- 备份列表、手动备份和恢复入口。
- 日志查询入口。
- 健康检查和运行指标。
- 长任务和批量操作状态。
- 清理任务入口。

Portal 入口：

- 不提供 Operations 能力。

## Infrastructure Layer

- Repository 持久化 Operations 自有表。
- 报表导出适配 PDF 或 HTML。
- 健康检查和日志摘要读取适配运行环境。

## Data Ownership

Operations 是 `operations_*` 表的唯一写入方。过期分享、过期导出和草稿清理规则仍归 Classics；AI 调用统计归 AI；搜索问答统计归 Discovery；标签和图谱质量统计归 Knowledge。

## Observability

- 维护操作记录操作者、时间、结果和失败原因。
- 恢复操作必须记录恢复前快照。
- 运行状态和长任务状态可被管理员追溯。

## Acceptance

- Operations 有独立 domain 和 infra，不污染 starter。
- 看板聚合不复制其他业务域主事实。
