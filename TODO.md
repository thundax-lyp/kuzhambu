# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `operations dashboard application support`：新增看板权限快照与解析器
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-PERMISSION-CHARTS.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/support/OperationsDashboardPermissionSnapshot.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/support/OperationsDashboardPermissionResolver.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/dashboard/support/OperationsDashboardPermissionResolverTest.java`
    - 处理动作：新增 dashboard 权限快照、权限解析器和权限解析测试覆盖。
    - 验收点：精确权限、父级权限和 `super` 均能解析出 RUNBOOK 定义的权限快照。
    - 重要度：10/10

- [ ] `operations dashboard summary gateway`：按权限跳过跨域 summary facade
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-PERMISSION-CHARTS.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/support/OperationsDashboardSummaryGateway.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/support/DefaultOperationsDashboardSummaryGateway.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/dashboard/support/DefaultOperationsDashboardSummaryGatewayTest.java`
    - 处理动作：给 summary gateway 增加权限快照入参，并按域级加载权限决定是否调用跨域 facade。
    - 验收点：无对应域图表权限时该域 facade 没有调用记录，返回 summary 为 `null`。
    - 重要度：10/10

- [ ] `operations dashboard overview application`：按字段权限裁剪 overview 结果
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-PERMISSION-CHARTS.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/service/impl/OperationsDashboardApplicationServiceImpl.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/result/OperationsDashboardOverviewResult.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/dashboard/service/impl/OperationsDashboardApplicationServiceImplTest.java`
    - 处理动作：在 overview 编排中按权限快照装配授权字段并将未授权字段保持为 `null`。
    - 验收点：只有 dashboard 权限时除周期外聚合字段均为 `null`，健康和任务仓储在无权时不被调用。
    - 重要度：10/10

- [ ] `operations dashboard interface`：保留 overview 响应 null 语义
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-PERMISSION-CHARTS.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/assembler/OperationsDashboardInterfaceAssembler.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/controller/response/OperationsDashboardOverviewResponse.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/controller/OperationsDashboardAdminControllerTest.java`
    - 处理动作：删除 interface assembler 的二次权限判断和空集合兜底。
    - 验收点：application 返回 `null` 的 list 与 `latestAlert` 在 interface response 中仍为 `null`。
    - 重要度：9/10

- [ ] `admin-web operations dashboard requests`：裁剪 dashboard 前端权限能力与请求触发
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-PERMISSION-CHARTS.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-service.ts`、`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-service-contract.test.ts`、`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.tsx`、`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.test.tsx`
    - 处理动作：新增页面权限能力结构并按健康权限控制健康趋势、告警分页和刷新请求。
    - 验收点：无 `operations:health:view` 时不调用 `getHealthTrend` 和 `getHealthAlerts`，overview 请求体不包含权限字段。
    - 重要度：10/10

- [ ] `admin-web operations dashboard entries`：按多权限规则裁剪运维入口卡
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-PERMISSION-CHARTS.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.tsx`、`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.test.tsx`
    - 处理动作：把 `OperationEntry.permission` 改为 `permissions` 并按任一权限展示入口。
    - 验收点：只有 `operations:restore:view` 时展示“备份恢复”，无入口权限时展示现有空入口状态。
    - 重要度：8/10

- [ ] `admin-web operations dashboard charts`：按权限裁剪指标卡、趋势图和排行
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-PERMISSION-CHARTS.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.tsx`、`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.css`、`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.test.tsx`
    - 处理动作：按 RUNBOOK 权限矩阵裁剪 `Card`、`TrendPanel`、`RankingList`、告警横幅、健康抽屉和无图表空态。
    - 验收点：无图表权限时不渲染任何图表标题，单项搜索、问答、标签、健康和任务权限只展示对应控件。
    - 重要度：10/10

- [ ] `feat/operations-dashboard-permission-charts`：同步 main 分支代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`feat/operations-dashboard-permission-charts`、`main`
    - 处理动作：在收口前同步最新 `main` 代码并处理与本任务相关的冲突。
    - 验收点：当前分支包含最新 `main`，同步后相关验证仍通过或记录明确阻塞原因。
    - 重要度：9/10

- [ ] `operations dashboard backend validation`：运行 Operations 后端验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-PERMISSION-CHARTS.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/dashboard/support/OperationsDashboardPermissionResolverTest.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/dashboard/support/DefaultOperationsDashboardSummaryGatewayTest.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/dashboard/service/impl/OperationsDashboardApplicationServiceImplTest.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/controller/OperationsDashboardAdminControllerTest.java`
    - 处理动作：运行 RUNBOOK 列出的 Operations 后端窄验证与收口验证。
    - 验收点：Operations dashboard 相关 Maven 测试、`spotless:check`、`checkstyle:check` 和 `test` 通过或记录明确阻塞原因。
    - 重要度：10/10

- [ ] `admin-web operations dashboard validation`：运行 dashboard 前端验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-PERMISSION-CHARTS.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.tsx`、`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.css`、`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.test.tsx`、`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-service-contract.test.ts`
    - 处理动作：运行 RUNBOOK 列出的 admin-web dashboard 前端窄验证与收口验证。
    - 验收点：dashboard Vitest、`format:check`、`lint` 和 `test` 通过或记录明确阻塞原因。
    - 重要度：10/10

- [ ] `operations dashboard readiness docs`：更新覆盖矩阵并清理临时 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-PERMISSION-CHARTS.md`
    - 范围对象：`docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-PERMISSION-CHARTS.md`、`TODO.md`
    - 处理动作：将按权限裁剪聚合图表标记为已完成并删除已完成 TODO 与临时 RUNBOOK。
    - 验收点：Implementation Coverage 反映完成态，已关闭 TODO 从 `TODO.md` 删除，RUNBOOK 文件被清理。
    - 重要度：10/10

## 待讨论项
