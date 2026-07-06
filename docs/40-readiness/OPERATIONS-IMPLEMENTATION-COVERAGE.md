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

- `cleanup` 已完成领域模型、持久化实现、应用层、接口层、admin 列表、详情查询、详情 item 展示和当前范围真实执行；当前已能发现并执行过期备份记录、Classics 过期导出任务、过期分享链接和草稿分享链接清理。长期规则策略、调度化清理和更多目标类型仍待后续扩展。
- `dashboard` 已完成 Operations 聚合接口、admin 页面、菜单权限和任务页迁移；当前跨域内容/AI/搜索/标签统计仍以稳定 `0` 或空数组占位，后续需要接入各业务域真实 summary 读取源。
- 备份恢复链路具备基础闭环，但自动备份调度、恢复期间真实写入阻断、统一运维首页与系统/审计入口仍未全部补齐。

已完成：

- 健康指标闭环已形成当前阶段可运行交付：
  - `operations_health_check` 已补齐 `probe_source`、`probe_target`、`details_json` 字段与 `idx_operations_health_probe` 索引。
  - `HealthCheckRecord` / `HealthCheckDO` / persistence assembler 已同步采集来源、目标和诊断 JSON。
  - 健康 summary/page 已返回采集来源信息，page 记录返回诊断 JSON。
  - `OperationsHealthProbe`、`LocalOperationsHealthProbe`、`OperationsHealthCollector` 已提供后台采集写入入口，健康状态统一为 `UP`、`DEGRADED`、`DOWN`。
  - `POST /api/operations/health/trend` 已支持按 `component`、`probeSource`、`periodStart`、`periodEnd`、`bucketType` 聚合 `HOUR` / `DAY` 趋势。
  - `admin-web` `/operations/dashboard` 已展示健康摘要、健康趋势和健康明细抽屉；`/operations/tasks` 不再请求健康摘要。
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

- 跨业务域真实 Dashboard 指标接入、按权限裁剪聚合图表、自动备份、恢复期间真实写入阻断、健康告警策略和 System 日志/审计入口仍未形成可运行交付。

## Requirement Coverage Matrix

| 需求项 | 状态 | 已完成部分 | 未完成部分 | 责任域 |
| --- | --- | --- | --- | --- |
| 仪表盘与聚合展示 | 部分完成 | 已完成 `OperationsDashboardApplicationService`、`POST /api/operations/dashboard/overview`、`OperationsDashboardOverviewResponse`、`/operations/dashboard` 前端页面、周期切换、刷新、指标卡、趋势区、排行区、健康明细抽屉和运维入口；接口在未接入真实跨域统计源时返回稳定 `0` 或空数组 | 内容、访问、AI、搜索、问答、标签等跨域指标仍需接入真实 summary 源；按权限裁剪聚合图表仍未完成 | Operations, Admin Web |
| 报表 | 已完成 | 已完成周报/月报任务生成、HTML/PDF worker 渲染、Storage 产物回写、报表记录分页/详情查询、状态流转、独立 response、`operations:report:view` / `operations:report:generate` 权限控制，以及跨域 summary 聚合读取 | admin 页面接入与专属下载表现不在本矩阵范围内；后续如增加调度报表可另行扩展 | Operations |
| 备份与恢复 | 部分完成 | 已完成 `BackupRecord`、`RestoreRecord`、各自 persistence、手动备份执行、恢复前 `PRE_RESTORE` 快照创建、备份/恢复 `execute/page/detail` 接口、`admin-web` 备份恢复页面、菜单与权限种子，以及脚本执行结果回写 | 启动自动备份、24 小时自动备份、恢复期间真实写入阻断、真实恢复演练与长期调度仍未完成 | Operations |
| 清理任务 | 部分完成 | `CleanupJob` / `CleanupItem` 已完成领域模型与持久化，应用层 `execute/page/detail` 与 admin 接口已上线，前端清理台账页已接通，`operations:cleanup:view/execute` 与页面基础回归测试已存在；当前真实执行覆盖过期备份、Classics 过期导出、过期分享和草稿分享，详情页可展示每条 cleanup item 的目标、状态、失败原因和处理时间；若清理目标涉及导出产物对象，其底层生命周期已可复用 Storage 自动 orphan 清理能力 | 调度化清理、长期规则策略和更多目标类型扩展未闭环 | Operations |
| 健康检查与运行状态 | 部分完成 | `HealthCheckRecord`、`LongTaskSnapshot` 已完成 domain + persistence + application + interface；健康记录已包含 `probeSource`、`probeTarget`、`detailsJson`；已具备本地健康采集器、健康摘要、组件分页、健康趋势、长任务分页与详情查询；`admin-web` 看板已消费健康摘要/趋势，任务页保留长任务台账 | 健康告警策略、失败恢复联动、更多外部探针和长任务异常恢复仍未完成 | Operations, Admin Web |
| 运维入口 | 部分完成 | 已完成 `/operations/dashboard` 统一入口、任务台账、备份恢复和清理维护跳转；菜单种子中 `运营看板` 使用 `operations:dashboard:view`；未实现的报表记录菜单已隐藏且 URL 为空，避免 `/operations/reports` 可点击死链 | System 日志/审计入口、健康细分页和更多运维操作编排仍未完成 | Operations, Admin Web, System |
| 台账记录 | 部分完成 | report、backup、restore、cleanup 已完成 domain + persistence + application + interface + admin page；cleanup 真实执行会写入 job summary 和 item 明细；health 与 long task 已完成 domain + persistence + application + interface，健康来源字段、趋势查询和看板消费已落地 | 长任务异常恢复与跨模块联调验证未完成 | Operations |
| TODO 与治理文档清理 | 已完成 | `TODO.md` 已清空，Operations Dashboard + Health Metrics 临时 RUNBOOK 已删除 | 无 | Governance |

## Follow-up Backlog

### B1 Operations 非报表剩余应用层闭环

状态：进行中  
目标：清理任务当前范围真实执行逻辑、健康组件采集来源入库和统一运维入口基础聚合已补齐；后续继续补齐跨域真实统计源、长任务写入源接入，以及阈值/告警策略。

### B2 Operations 剩余接口与入口

状态：进行中  
目标：补齐健康细分页面、System 日志/审计入口对应的 admin interface/controller/API，包括动作触发、状态查询、结果追踪与权限约束，形成可调用闭环。

### B3 Cleanup 持久化补齐

状态：已完成  
目标：清理台账持久化基础设施已就绪，进入接入真实清理执行规则与任务编排层阶段。

### B4 调度、阻断与运行态接入

状态：未开始
目标：接入自动备份调度、更多运行指标探针、恢复期间真实写入阻断、长任务写入源，以及 System 日志/审计入口。

### B5 剩余非报表任务交付验证

状态：进行中
目标：cleanup 关键链路已补齐真实执行与详情 item 回归；health/task 看板消费与接口趋势已补齐基础契约；后续补一轮 backup/restore 真实恢复演练记录，覆盖创建、更新、查询，以及恢复失败原因可追溯能力。
