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

- 已完成部分台账模型的 domain/infra 落地：
  - `backup`、`restore`、`health`、`long task` 已具备值对象、领域实体、仓储端口，以及对应的 DO、Mapper、Persistence Assembler、RepositoryImpl。
- `cleanup` 当前仅完成领域实体、值对象与仓储端口，尚未看到对应的 DO、Mapper、Persistence Assembler、RepositoryImpl。
- 除报表外，`kuzhambu-operations-application` 与 `kuzhambu-operations-interface` 仍未形成备份恢复、清理任务、健康检查、长任务、运维入口的 application/interface 闭环。

已完成：

- 报表闭环已形成当前阶段可运行交付：
  - `report` 已完成 domain + persistence，并补齐 `requestId`、`traceId`、`templateVersion`、`artifactFilename` 字段。
  - `kuzhambu-operations-application` 已完成 `generate/page/detail` 应用服务、任务执行单元、状态流转、worker 调用、Storage 产物回写。
  - `kuzhambu-operations-interface` 已完成 admin `generate/page/detail` 接口、独立 `XxxResponse`、`operations:report:view` / `operations:report:generate` 权限约束。
  - `Classics / AI / Discovery / Knowledge` 已提供按统一 summary 规格暴露的 `@LayerPublicApi` 聚合读取入口，周报按日、月报按周的 bucket 规则已固化到代码。
  - 已存在 application / infra / interface / 跨域 summary 对应测试，报表链路具备基本验证闭环。

未完成：

- 仪表盘聚合展示、按权限聚合图表化展示、自动备份、手动备份入口、恢复前快照调用链、恢复期间写入阻断、清理任务执行入口、健康检查摘要与运行指标查看、长任务状态查看、System 日志/审计入口仍未形成可运行交付。
- Operations 其余能力仍缺少面向 admin 页面或完整运行编排的交付；当前已形成测试闭环的重点集中在报表链路。

## Requirement Coverage Matrix

| 需求项 | 状态 | 已完成部分 | 未完成部分 | 责任域 |
| --- | --- | --- | --- | --- |
| 仪表盘与聚合展示 | 未完成 | 仅有需求/设计边界定义 | 内容统计、访问统计、活跃用户、功能频率、热门内容、趋势图、标签覆盖率变化、按权限聚合与图表化展示均未见代码落地 | Operations |
| 报表 | 已完成 | 已完成周报/月报任务生成、HTML/PDF worker 渲染、Storage 产物回写、报表记录分页/详情查询、状态流转、独立 response、`operations:report:view` / `operations:report:generate` 权限控制，以及跨域 summary 聚合读取 | admin 页面接入与专属下载表现不在本矩阵范围内；后续如增加调度报表可另行扩展 | Operations |
| 备份与恢复 | 部分完成 | `BackupRecord`、`RestoreRecord` 及各自 persistence 已完成，可承载备份/恢复台账字段 | 启动自动备份、24 小时自动备份、手动备份、恢复前快照创建、恢复期间阻止写入、恢复入口与查询闭环未完成 | Operations |
| 清理任务 | 部分完成 | `CleanupJob`、`CleanupItem`、`CleanupJobRepository` 已完成领域层建模 | `cleanup` 持久化实现缺失；过期备份/分享/草稿/导出产物清理入口、执行链路、结果查询与单项失败追踪未完成 | Operations |
| 健康检查与运行状态 | 部分完成 | `HealthCheckRecord`、`LongTaskSnapshot` 及 health/task persistence 已完成，可承载健康记录与长任务快照 | 健康检查摘要、运行指标查看、关键组件检查写入源、长任务查询入口与展示闭环未完成 | Operations |
| 运维入口 | 未完成 | 仅有需求/设计边界定义 | System 日志入口、业务审计入口、集中触发报表/备份/恢复/清理/运行状态动作的 admin 入口未完成 | Operations |
| 台账记录 | 部分完成 | report 已完成 domain + persistence + application + interface + test 闭环；backup、restore、health、long task 已完成 domain + persistence；cleanup 已完成 domain 建模 | cleanup 持久化缺失；backup/restore/health/task 仍缺少 application/interface/query/test 闭环 | Operations |
| TODO 与治理文档清理 | 已完成 | `TODO.md` TODO 列表已清空，相关 runbook 已移除 | 无 | Governance |

## Follow-up Backlog

### B1 Operations 非报表应用层闭环

状态：进行中  
目标：完成 `kuzhambu-operations-application` 的备份恢复、清理任务、健康检查、长任务与运维入口编排服务，并完成对 repository、权限校验、调度与运行态数据源的应用层接入。

### B2 Operations 非报表接口与入口

状态：未开始  
目标：补齐备份恢复、清理任务、健康检查、长任务、运维入口对应的 admin interface/controller/API，包括动作触发、状态查询、结果追踪与权限约束，形成可调用闭环。

### B3 Cleanup 持久化补齐

状态：未开始  
目标：补齐 `operations_cleanup_job` 与 `operations_cleanup_item` 的 DO、Mapper、Persistence Assembler、RepositoryImpl，使 cleanup 台账与需求口径一致。

### B4 指标、调度与运行态接入

状态：未开始  
目标：接入自动备份调度、运行指标/组件健康采集源、恢复前快照/写入阻断编排、长任务写入源，以及 System 日志/审计入口。

### B5 非报表任务交付验证

状态：未开始  
目标：补齐 backup/restore/cleanup/health/task 关键链路的契约与集成测试，覆盖创建、更新、查询，以及清理/恢复失败原因可追溯能力。
