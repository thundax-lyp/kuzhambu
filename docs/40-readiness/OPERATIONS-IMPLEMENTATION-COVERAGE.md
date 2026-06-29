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

- `cleanup` 当前仅完成领域实体、值对象与仓储端口，尚未看到对应的 DO、Mapper、Persistence Assembler、RepositoryImpl。
- `health`、`long task` 已具备值对象、领域实体、仓储端口，以及对应的 DO、Mapper、Persistence Assembler、RepositoryImpl，但仍缺少 application/interface/admin 展示闭环。
- 备份恢复链路已经闭环，但自动备份调度、真实写入阻断、健康检查、清理任务、长任务和统一运维入口仍未全部补齐。

已完成：

- 报表闭环已形成当前阶段可运行交付：
  - `report` 已完成 domain + persistence，并补齐 `requestId`、`traceId`、`templateVersion`、`artifactFilename` 字段。
  - `kuzhambu-operations-application` 已完成 `generate/page/detail` 应用服务、任务执行单元、状态流转、worker 调用、Storage 产物回写。
  - `kuzhambu-operations-interface` 已完成 admin `generate/page/detail` 接口、独立 `XxxResponse`、`operations:report:view` / `operations:report:generate` 权限约束。
  - `Classics / AI / Discovery / Knowledge` 已提供按统一 summary 规格暴露的 `@LayerPublicApi` 聚合读取入口，周报按日、月报按周的 bucket 规则已固化到代码。
  - 已存在 application / infra / interface / 跨域 summary 对应测试，报表链路具备基本验证闭环。
- 备份恢复闭环已形成当前阶段可运行交付：
  - `backup`、`restore` 已完成 domain + persistence + application + interface + admin page + 菜单权限种子数据。
  - `kuzhambu-operations-application` 已完成手动备份执行、恢复前 `PRE_RESTORE` 快照创建、备份/恢复分页与详情查询，以及脚本执行结果回写台账。
  - `kuzhambu-operations-interface` 已完成 admin `execute/page/detail` 接口、独立 `XxxResponse`、`operations:backup:view` / `operations:backup:execute` / `operations:restore:view` / `operations:restore:execute` 权限约束。
  - `admin-web` 已完成 `/operations/backup-restore` 页面、台账展示、详情抽屉、手动备份与恢复动作触发。
  - `db/data-source/system.json` 与 `db/data/system.sql` 已对齐 `Operations/备份恢复` 菜单、路由与权限口径。
- 备份恢复部署与执行器配套已形成：
  - 已完成专项设计文档、Docker Compose 挂载调整、`admin-starter` 下的备份/恢复/清理脚本、业务表白名单和校验输出格式。

未完成：

- 仪表盘聚合展示、按权限聚合图表化展示、自动备份、恢复期间真实写入阻断、清理任务执行入口、健康检查摘要与运行指标查看、长任务状态查看、System 日志/审计入口仍未形成可运行交付。
- `cleanup`、`health`、`long task` 仍缺少 application/interface/admin 页面/测试闭环。

## Requirement Coverage Matrix

| 需求项 | 状态 | 已完成部分 | 未完成部分 | 责任域 |
| --- | --- | --- | --- | --- |
| 仪表盘与聚合展示 | 未完成 | 仅有需求/设计边界定义 | 内容统计、访问统计、活跃用户、功能频率、热门内容、趋势图、标签覆盖率变化、按权限聚合与图表化展示均未见代码落地 | Operations |
| 报表 | 已完成 | 已完成周报/月报任务生成、HTML/PDF worker 渲染、Storage 产物回写、报表记录分页/详情查询、状态流转、独立 response、`operations:report:view` / `operations:report:generate` 权限控制，以及跨域 summary 聚合读取 | admin 页面接入与专属下载表现不在本矩阵范围内；后续如增加调度报表可另行扩展 | Operations |
| 备份与恢复 | 部分完成 | 已完成 `BackupRecord`、`RestoreRecord`、各自 persistence、手动备份执行、恢复前 `PRE_RESTORE` 快照创建、备份/恢复 `execute/page/detail` 接口、`admin-web` 备份恢复页面、菜单与权限种子，以及脚本执行结果回写 | 启动自动备份、24 小时自动备份、恢复期间真实写入阻断、真实恢复演练与长期调度仍未完成 | Operations |
| 清理任务 | 部分完成 | `CleanupJob`、`CleanupItem`、`CleanupJobRepository` 已完成领域层建模 | `cleanup` 持久化实现缺失；过期备份/分享/草稿/导出产物清理入口、执行链路、结果查询与单项失败追踪未完成 | Operations |
| 健康检查与运行状态 | 部分完成 | `HealthCheckRecord`、`LongTaskSnapshot` 及 health/task persistence 已完成，可承载健康记录与长任务快照 | 健康检查摘要、运行指标查看、关键组件检查写入源、长任务查询入口与展示闭环未完成 | Operations |
| 运维入口 | 部分完成 | 已完成备份恢复 admin 入口、路由、菜单权限种子，以及报表链路接口；管理员可从 `Operations` 菜单进入备份恢复页并触发动作 | System 日志入口、业务审计入口、清理任务、健康检查、长任务、统一运维首页仍未完成 | Operations |
| 台账记录 | 部分完成 | report 已完成 domain + persistence + application + interface + test 闭环；backup、restore 已完成 domain + persistence + application + interface + admin page + test 闭环；health、long task 已完成 domain + persistence；cleanup 已完成 domain 建模 | cleanup 持久化缺失；health/task 仍缺少 application/interface/query/test 闭环 | Operations |
| TODO 与治理文档清理 | 已完成 | `TODO.md` 已清空，备份恢复专项 RUNBOOK 已删除 | 无 | Governance |

## Follow-up Backlog

### B1 Operations 非报表剩余应用层闭环

状态：进行中  
目标：完成 `kuzhambu-operations-application` 中剩余的清理任务、健康检查、长任务与统一运维入口编排服务，并完成对 repository、权限校验、调度与运行态数据源的应用层接入。

### B2 Operations 剩余接口与入口

状态：未开始  
目标：补齐清理任务、健康检查、长任务、System 日志/审计入口对应的 admin interface/controller/API，包括动作触发、状态查询、结果追踪与权限约束，形成可调用闭环。

### B3 Cleanup 持久化补齐

状态：未开始  
目标：补齐 `operations_cleanup_job` 与 `operations_cleanup_item` 的 DO、Mapper、Persistence Assembler、RepositoryImpl，使 cleanup 台账与需求口径一致。

### B4 调度、阻断与运行态接入

状态：未开始  
目标：接入自动备份调度、运行指标/组件健康采集源、恢复期间真实写入阻断、长任务写入源，以及 System 日志/审计入口。

### B5 剩余非报表任务交付验证

状态：未开始  
目标：补齐 cleanup/health/task 关键链路的契约与集成测试，并补一轮 backup/restore 真实恢复演练记录，覆盖创建、更新、查询，以及清理/恢复失败原因可追溯能力。
