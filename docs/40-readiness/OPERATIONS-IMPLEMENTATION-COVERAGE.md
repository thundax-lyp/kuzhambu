# Operations Implementation Coverage

## Purpose

本文档记录 Operations 域需求在当前仓库的实现覆盖状态，用于下一阶段补齐 application/interface 及运行闭环。

本文档不替代 `docs/10-requirements/OPERATIONS-REQUIREMENTS.md` 和 `docs/30-designs/OPERATIONS-DESIGN.md`。

## Status Definition

- `已完成`：对应需求可在仓库中形成可追溯交付物并具备运行时闭环。
- `部分完成`：核心模型/持久化与部分链路已就绪，仍缺少关键层或接口闭环。
- `未完成`：当前仓库未形成可执行实现、接口、测试或关键功能链路。
- `外部依赖`：能力边界不属于 Operations，或依赖其他域完成。

## Current Baseline

已完成：

- 已完成 Operations 域核心基础骨架建模：`kuzhambu-operations-domain` 与 `kuzhambu-operations-infra` 已覆盖 `operations_report`、`operations_backup`、`operations_restore`、`operations_cleanup_job`、`operations_cleanup_item`、`operations_health_check`、`operations_long_task_snapshot` 的值对象、领域实体与仓储端口。
- 已补齐上述台账的持久化映射层（DO、Mapper、Assembler、RepositoryImpl）：
  - report、backup、restore、cleanup、health、long task。
- 已完成 runbook `RUNBOOK-OPERATIONS-DOMAIN-INFRA.md` 对应的 domain/infra 落地工作。
- `kuzhambu-operations` 模块安装与静态检查在当前范围内可通过。

部分完成：

- domain/infra 与 application 的解耦边界尚未闭合：`kuzhambu-operations-application` 与 `kuzhambu-operations-interface` 尚未按设计完成接入。
- Operations 聚合统计、报表导出、备份恢复编排、清理入口、健康检查汇总与长任务查询等运行时入口尚未在 application/interface 层形成闭环。
- 台账与运行时行为的契约/集成测试尚未形成完整覆盖。

未完成：

- 自动备份调度、运行指标/健康聚合适配、系统日志与审计入口、长任务展示页面、跨域统计查询与权限聚合还未完成应用层与接入层交付。
- 依赖 `kuzhambu-operations-application` 与 `kuzhambu-operations-interface` 的运维交付能力与用户可见路径尚未上线。

## Requirement Coverage Matrix

| 需求项 | 状态 | 已完成部分 | 未完成部分 | 责任域 |
| --- | --- | --- | --- | --- |
| 运营台账域模型与持久化 | 已完成 | `ReportRecord`、`BackupRecord`、`RestoreRecord`、`CleanupJob`/`CleanupItem`、`HealthCheckRecord`、`LongTaskSnapshot` 与各自仓储端口及 DO/Mapper/Assembler/RepoImpl 已完成 | 无（本轮范围内） | Operations |
| 仪表盘与聚合展示 | 部分完成 | 需求与设计已明确，跨域读取边界定义完成 | 聚合查询与页面渲染链路未落地 | Operations |
| 报表生成与记录 | 部分完成 | `operations_report` 记录模型与存储完成 | 报表生成编排、调度、导出产物落地与查询闭环未完成 | Operations |
| 备份与恢复 | 部分完成 | `operations_backup`、`operations_restore` 记录模型与存储完成 | 自动/手动触发、恢复流程编排、恢复前快照调用链未完成 | Operations |
| 清理任务与清理项 | 已完成（骨架） | `operations_cleanup_job`/`operations_cleanup_item` 聚合与持久化完成 | 清理入口、执行器接入、结果告警与可视化闭环未完成 | Operations |
| 健康检查记录 | 已完成（骨架） | `operations_health_check` 模型与存储完成 | 健康指标采集适配器与对外查看接口未完成 | Operations |
| 长任务运行快照 | 已完成（骨架） | `operations_long_task_snapshot` 模型与存储完成 | 运行快照写入源与前端展示闭环未完成 | Operations |
| TODO 与治理文档清理 | 已完成 | `TODO.md` TODO 列表已清空，相关 runbook 已移除 | 无 | Governance |

## Follow-up Backlog

### B1 Operations 应用层闭环

状态：进行中  
目标：完成 `kuzhambu-operations-application` 的报表、备份恢复、清理任务、健康检查、长任务与入口编排服务，并完成对 repository 的应用层接入。

### B2 Operations 接口与入口

状态：未开始  
目标：补齐 admin interface/controller/API，包括运维动作触发、状态查询、结果追踪与权限约束，形成可调用闭环。

### B3 指标与健康运行态接入

状态：未开始  
目标：接入运行指标/组件健康采集源并将摘要写入/同步到 `operations_health_check` 与长任务快照视图。

### B4 任务交付验证

状态：未开始  
目标：补齐 operations 关键链路的契约与集成测试，覆盖 report/backup/restore/cleanup/health/task 的创建、更新、查询、清理/恢复失败原因可追溯能力。
