# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `operations_health_check`：补齐健康记录采集来源字段
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-HEALTH-METRICS.md`
    - 范围对象：`db/schema/operations.sql`、`kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/model/entity/HealthCheckRecord.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/persistence/dataobject/HealthCheckDO.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/persistence/assembler/HealthCheckPersistenceAssembler.java`
    - 处理动作：为健康检查记录增加 `probe_source`、`probe_target`、`details_json` 字段并同步 domain/infra 映射。
    - 验收点：schema、DO、domain entity 和 assembler 字段一致，旧字段语义不变。
    - 重要度：10/10

- [ ] `OperationsHealth summary/page`：返回健康采集来源字段
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-HEALTH-METRICS.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/result/OperationsHealthSummaryResult.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/result/OperationsHealthPageResult.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/response/OperationsHealthSummaryResponse.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/response/OperationsHealthPageResponse.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/assembler/OperationsHealthInterfaceAssembler.java`
    - 处理动作：让健康摘要和分页接口返回 `probeSource`、`probeTarget`，分页接口额外返回 `detailsJson`。
    - 验收点：`/api/operations/health/summary` 与 `/api/operations/health/page` 的 response 字段与 RUNBOOK 契约一致。
    - 重要度：9/10

- [ ] `OperationsHealthCollector`：新增后台健康采集入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-HEALTH-METRICS.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthProbe.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthCollector.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/LocalOperationsHealthProbe.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthCollectorTest.java`
    - 处理动作：新增 probe/collector 支持后台采集并写入 `operations_health_check`。
    - 验收点：采集状态固定写入 `UP`、`DEGRADED`、`DOWN`，单个 probe 失败不阻断其他 probe。
    - 重要度：10/10

- [ ] `HealthCheckRepository.listTrend`：补齐健康趋势聚合查询
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-HEALTH-METRICS.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/model/valueobject/HealthTrendBucket.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/repository/HealthCheckRepository.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/repository/impl/HealthCheckRepositoryImpl.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/test/java/com/thundax/kuzhambu/operations/infra/health/repository/impl/HealthCheckRepositoryImplTest.java`
    - 处理动作：基于 `operations_health_check` 实现 `HOUR` 和 `DAY` 两种健康趋势聚合。
    - 验收点：趋势查询返回 `bucket`、`upCount`、`degradedCount`、`downCount`、`avgLatencyMs`，且不新增趋势表。
    - 重要度：9/10

- [ ] `OperationsHealth trend API`：暴露健康趋势接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-HEALTH-METRICS.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/query/OperationsHealthTrendQuery.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/result/OperationsHealthTrendResult.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/request/OperationsHealthTrendRequest.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/response/OperationsHealthTrendResponse.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/OperationsHealthAdminController.java`
    - 处理动作：新增 `POST /api/operations/health/trend` 查询入口。
    - 验收点：接口使用 `operations:health:view` 权限并由后端返回 `bucket`。
    - 重要度：9/10

- [ ] `OperationsDashboardApplicationService`：建立运营看板应用层契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-HEALTH-METRICS.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/service/OperationsDashboardApplicationService.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/service/impl/OperationsDashboardApplicationServiceImpl.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/result/OperationsDashboardOverviewResult.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/query/OperationsDashboardOverviewQuery.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/dashboard/service/impl/OperationsDashboardApplicationServiceImplTest.java`
    - 处理动作：新增 Dashboard overview 应用服务并返回稳定聚合 result。
    - 验收点：默认周期为 `WEEK`，`CUSTOM` 校验起止时间，未接真实数据源的字段返回 `0` 或空数组。
    - 重要度：10/10

- [ ] `OperationsDashboardAdminController`：暴露运营看板 admin 接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-HEALTH-METRICS.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/controller/request/OperationsDashboardOverviewRequest.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/controller/OperationsDashboardAdminController.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/controller/response/OperationsDashboardOverviewResponse.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/assembler/OperationsDashboardInterfaceAssembler.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/controller/OperationsDashboardAdminControllerTest.java`
    - 处理动作：新增 `POST /api/operations/dashboard/overview` admin 接口。
    - 验收点：接口使用 `operations:dashboard:view` 权限且 response 不复用其他业务域 response 类。
    - 重要度：10/10

- [ ] `admin-web operations dashboard service`：建立运营看板前端服务契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-HEALTH-METRICS.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-types.ts`、`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-service.ts`、`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-service-contract.test.ts`
    - 处理动作：新增 dashboard overview 与 health trend 的前端类型、请求方法和契约测试。
    - 验收点：contract test 精确断言 `/operations/dashboard/overview`、`/operations/health/trend` 的 URL 与 body 字段。
    - 重要度：9/10

- [ ] `admin-web /operations/dashboard`：实现运营看板页面
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-HEALTH-METRICS.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.tsx`、`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.css`、`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.test.tsx`、`kuzhambu-apps/admin-web/src/router/index.tsx`
    - 处理动作：新增运营看板页面、路由、样式和页面测试。
    - 验收点：页面具备周期选择、刷新、指标卡、趋势图、健康明细抽屉、排行区和运维入口，且无权限时不请求接口。
    - 重要度：10/10

- [ ] `admin-web operations tasks`：迁移健康摘要出任务页
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-HEALTH-METRICS.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/tasks/tasks-page.tsx`、`kuzhambu-apps/admin-web/src/pages/operations/tasks/tasks-service.ts`、`kuzhambu-apps/admin-web/src/pages/operations/tasks/tasks-types.ts`、`kuzhambu-apps/admin-web/src/pages/operations/tasks/tasks-page.test.tsx`
    - 处理动作：删除任务页健康摘要请求与卡片，保留任务筛选、分页、详情抽屉和返回看板入口。
    - 验收点：任务页不再请求 `/operations/health/summary`，任务台账行为不变。
    - 重要度：8/10

- [ ] `Operations menu route permissions`：对齐运营看板菜单、路由和权限
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-HEALTH-METRICS.md`
    - 范围对象：`db/data-source/system.json`、`db/data/system.sql`、`kuzhambu-apps/admin-web/src/router/index.tsx`
    - 处理动作：核对 `/operations/dashboard` 菜单、权限种子和前端路由一致性。
    - 验收点：`运营看板` 使用 `operations:dashboard:view`，且 dashboard 不提供 `/operations/reports` 可点击死链。
    - 重要度：8/10

- [ ] `Operations readiness docs`：更新覆盖矩阵并清理 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-HEALTH-METRICS.md`、`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-HEALTH-METRICS.md`
    - 处理动作：在实现完成后更新 Operations 覆盖矩阵并删除临时 RUNBOOK。
    - 验收点：覆盖矩阵反映 Dashboard 与 Health Metrics 真实交付状态，RUNBOOK 已清理。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
