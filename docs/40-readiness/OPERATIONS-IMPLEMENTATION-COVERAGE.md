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

- `cleanup` 已完成领域模型、持久化实现、应用层与接口层闭环，并已接通 admin 列表与详情查询；当前清理目标发现与执行逻辑仍为占位实现，未形成真实清理能力。
- `health` 与 `long task` 已完成领域模型、持久化、应用层、接口层闭环，支持健康摘要、组件级分页和长任务分页/详情查询，前端 `Operations` 台账页已接入健康摘要与长任务运行态展示；运行指标采集源仍未接入。
- 备份恢复链路具备基础闭环，但自动备份调度、恢复期间真实写入阻断、统一运维首页与系统/审计入口仍未全部补齐。

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

- 仪表盘聚合展示、按权限聚合图表化展示、自动备份、恢复期间真实写入阻断、健康指标趋势/告警体系、System 日志/审计入口仍未形成可运行交付。

## Requirement Coverage Matrix

| 需求项 | 状态 | 已完成部分 | 未完成部分 | 责任域 |
| --- | --- | --- | --- | --- |
| 仪表盘与聚合展示 | 未完成 | 仅有需求/设计边界定义 | 内容统计、访问统计、活跃用户、功能频率、热门内容、趋势图、标签覆盖率变化、按权限聚合与图表化展示未见代码落地；已有 `Operations` 任务门面页实现健康摘要与长任务运行态展示 | Operations |
| 报表 | 已完成 | 已完成周报/月报任务生成、HTML/PDF worker 渲染、Storage 产物回写、报表记录分页/详情查询、状态流转、独立 response、`operations:report:view` / `operations:report:generate` 权限控制，以及跨域 summary 聚合读取 | admin 页面接入与专属下载表现不在本矩阵范围内；后续如增加调度报表可另行扩展 | Operations |
| 备份与恢复 | 部分完成 | 已完成 `BackupRecord`、`RestoreRecord`、各自 persistence、手动备份执行、恢复前 `PRE_RESTORE` 快照创建、备份/恢复 `execute/page/detail` 接口、`admin-web` 备份恢复页面、菜单与权限种子，以及脚本执行结果回写 | 启动自动备份、24 小时自动备份、恢复期间真实写入阻断、真实恢复演练与长期调度仍未完成 | Operations |
| 清理任务 | 部分完成 | `CleanupJob` / `CleanupItem` 已完成领域模型与持久化，应用层 `execute/page/detail` 与 admin 接口已上线，前端清理台账页已接通，`operations:cleanup:view/execute` 与页面基础回归测试已存在 | 清理目标发现与真实执行逻辑待接入，失败项失败原因链路与长期规则策略未闭环 | Operations |
| 健康检查与运行状态 | 部分完成 | `HealthCheckRecord`、`LongTaskSnapshot` 已完成 domain + persistence + application + interface，支持健康摘要、组件分页查询、长任务分页与详情查询，`operations:health:view`/`operations:task:view` 接口已就绪 | 健康指标采集源、运行态趋势展示、告警策略与失败恢复联动未完成 | Operations |
| 运维入口 | 部分完成 | 已完成备份恢复与报表 admin 入口、路由与菜单种子；清理/健康/长任务接口可用于统一运维入口聚合消费；`Operations` 门面页已提供清理、备份恢复、报表快速跳转 | 健康细分页、System 日志/审计入口、统一运维首页仍未完成 | Operations |
| 台账记录 | 部分完成 | report、backup、restore、cleanup 已完成 domain + persistence + application + interface + admin page；health 与 long task 已完成 domain + persistence + application + interface，基础查询能力可消费 | 清理真实执行归档、健康指标来源写入、长任务异常恢复与跨模块联调验证未完成 | Operations |
| TODO 与治理文档清理 | 已完成 | `TODO.md` 已清空，备份恢复专项 RUNBOOK 已删除 | 无 | Governance |

## Follow-up Backlog

### B1 Operations 非报表剩余应用层闭环

状态：进行中  
目标：补齐清理任务真实执行逻辑、健康组件采集来源入库、长任务写入源接入，以及统一运维入口编排层的状态聚合与阈值策略。

### B2 Operations 剩余接口与入口

状态：进行中  
目标：补齐清理任务、健康细分页面、System 日志/审计入口对应的 admin interface/controller/API，包括动作触发、状态查询、结果追踪与权限约束，形成可调用闭环。

### B3 Cleanup 持久化补齐

状态：已完成  
目标：清理台账持久化基础设施已就绪，进入接入真实清理执行规则与任务编排层阶段。

### B4 调度、阻断与运行态接入

状态：未开始  
目标：接入自动备份调度、运行指标/组件健康采集源、恢复期间真实写入阻断、长任务写入源，以及 System 日志/审计入口。

### B5 剩余非报表任务交付验证

状态：未开始  
目标：补齐 cleanup/health/task 关键链路的契约与集成测试，并补一轮 backup/restore 真实恢复演练记录，覆盖创建、更新、查询，以及清理/恢复失败原因可追溯能力。
