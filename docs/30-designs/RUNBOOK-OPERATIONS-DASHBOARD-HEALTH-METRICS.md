# Operations Dashboard Health Metrics Runbook

## Purpose

本文档用于推进 `Operations Dashboard + Health Metrics` 闭环。目标是在 admin 侧补齐 `/operations/dashboard` 运营运维看板、健康指标采集源、健康明细趋势、权限控制和前后端验证。

本文档是临时执行手册。任务完成、覆盖矩阵更新并通过审核后，应删除本 RUNBOOK。

## Confirmed Decisions

- 本轮主目标只覆盖 `/operations/dashboard` 与 Health Metrics 闭环，不顺手实现 `/operations/reports` 页面。
- Dashboard 数据来源复用或扩展各业务域 application result / read model，不允许 Operations 直读其他业务域主表。
- 健康检查采用后台采集写入 `operations_health_check`，页面只读取已采集结果，不在页面请求时同步探测 DB、Redis 或 worker。
- 健康摘要从 `/operations/tasks` 迁移到 `/operations/dashboard`，`/operations/tasks` 回归长任务和批量任务台账。
- 新写入的健康状态固定为 `UP`、`DEGRADED`、`DOWN`；旧数据兼容只在展示层处理。
- 权限分层：
  - `operations:dashboard:view`：查看运营看板聚合摘要。
  - `operations:health:view`：查看健康明细和趋势。
  - `operations:task:view`：查看长任务明细。
- 健康趋势先基于 `operations_health_check` 聚合，不新增独立趋势表。
- 健康趋势首版只支持 `HOUR` 和 `DAY`。
- 健康明细首版作为 Dashboard 内抽屉或下钻区域，不新增独立菜单。
- `/operations/reports` 未实现前，Dashboard 不提供可点击死链。
- Dashboard 首版 response 结构必须稳定；暂未接入真实统计源的跨域指标允许返回 `0` 或空数组。
- 前端图表先使用 Ant Design、CSS、轻量 SVG 或现有项目组件实现，不引入新图表依赖。
- 推荐执行顺序：T1、T2、T3、T4a、T4b、T5a、T5b、T6、T7、T8、T9、T10。

## Current Baseline

- `docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md` 将“仪表盘与聚合展示”标记为未完成。
- `db/schema/operations.sql` 已存在 `operations_health_check`：
  - `id`
  - `check_id`
  - `component`
  - `health_status`
  - `latency_ms`
  - `message`
  - `checked_at`
- `db/schema/operations.sql` 已存在 `operations_long_task_snapshot`：
  - `id`
  - `snapshot_id`
  - `source_domain`
  - `task_type`
  - `task_key`
  - `task_status`
  - `total_count`
  - `success_count`
  - `failed_count`
  - `failure_reason`
  - `requested_by_user_id`
  - `started_at`
  - `completed_at`
  - `snapshot_at`
- 后端已有健康接口：
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/OperationsHealthAdminController.java`
  - `POST /api/operations/health/summary`
  - `POST /api/operations/health/page`
- 后端已有长任务接口：
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/task/controller/OperationsTaskAdminController.java`
  - `POST /api/operations/task/page`
  - `POST /api/operations/task/detail`
- 前端已有任务页：
  - `kuzhambu-apps/admin-web/src/pages/operations/tasks/tasks-page.tsx`
  - `kuzhambu-apps/admin-web/src/pages/operations/tasks/tasks-service.ts`
  - `kuzhambu-apps/admin-web/src/pages/operations/tasks/tasks-types.ts`
  - `kuzhambu-apps/admin-web/src/pages/operations/tasks/tasks-page.css`
- 前端路由尚未接入 `/operations/dashboard`：
  - `kuzhambu-apps/admin-web/src/router/index.tsx`
- 菜单种子已存在 `/operations/dashboard`：
  - `db/data-source/system.json`
  - `db/data/system.sql`

## Target Data Contracts

### Dashboard Request

新增 `OperationsDashboardOverviewRequest`，字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `periodType` | `String` | 否 | `WEEK`、`MONTH`、`CUSTOM`；为空默认 `WEEK` |
| `periodStart` | `Date` | 否 | `CUSTOM` 时必填；沿用现有 report request 时间类型 |
| `periodEnd` | `Date` | 否 | `CUSTOM` 时必填；沿用现有 report request 时间类型 |

### Dashboard Response

新增 `OperationsDashboardOverviewResponse`，字段至少包含：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `periodStart` | `Date` | 统计开始时间 |
| `periodEnd` | `Date` | 统计结束时间 |
| `contentCount` | `Long` | 内容总数 |
| `translatedContentCount` | `Long` | 已翻译内容数 |
| `imageReadyContentCount` | `Long` | 已配图内容数 |
| `visualAssetReadyContentCount` | `Long` | 视觉资产就绪内容数 |
| `shareVisitCount` | `Long` | 分享访问量 |
| `aiInvocationCount` | `Long` | AI 调用数 |
| `aiSucceededInvocationCount` | `Long` | AI 成功调用数 |
| `aiFailedInvocationCount` | `Long` | AI 失败调用数 |
| `aiAvgLatencyMs` | `BigDecimal` | AI 平均耗时 |
| `aiTotalCostAmount` | `BigDecimal` | AI 总成本 |
| `searchCount` | `Long` | 搜索次数 |
| `qaCount` | `Long` | 问答次数 |
| `avgSearchLatencyMs` | `BigDecimal` | 搜索平均耗时 |
| `tagCoverageRate` | `BigDecimal` | 标签覆盖率 |
| `unhealthyComponentCount` | `Integer` | 非健康组件数 |
| `runningTaskCount` | `Integer` | 运行中任务数 |
| `failedTaskCount` | `Integer` | 失败任务数 |
| `contentGrowthSeries` | `List<BucketCountResponse>` | 内容增长趋势 |
| `searchTrendSeries` | `List<BucketCountResponse>` | 搜索趋势 |
| `qaTrendSeries` | `List<BucketCountResponse>` | 问答趋势 |
| `tagGrowthSeries` | `List<BucketCountResponse>` | 标签新增趋势 |
| `healthSummaries` | `List<OperationsHealthSummaryResponse>` | 健康摘要 |
| `taskStatusSummaries` | `List<TaskStatusSummaryResponse>` | 长任务状态分布 |
| `topContents` | `List<TopContentResponse>` | 热门内容 |
| `topQueries` | `List<TopQueryResponse>` | 热门 query |
| `topTags` | `List<TopTagResponse>` | 热门标签 |
| `topAiCapabilities` | `List<TopAiCapabilityResponse>` | 热门 AI capability |

趋势对象 `BucketCountResponse` 字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `bucket` | `String` | 后端返回的聚合桶，不允许前端自行推导 |
| `count` | `Long` | 当前桶计数 |

状态分布对象 `TaskStatusSummaryResponse` 字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `taskStatus` | `String` | 任务状态 |
| `count` | `Long` | 当前状态任务数 |

热门内容对象 `TopContentResponse` 字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `contentId` | `Long` | 内容 ID |
| `contentType` | `String` | 内容类型 |
| `title` | `String` | 内容标题 |
| `visitCount` | `Long` | 访问次数 |

热门 query 对象 `TopQueryResponse` 字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `queryText` | `String` | query 文本 |
| `count` | `Long` | 出现次数 |

热门标签对象 `TopTagResponse` 字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `tagName` | `String` | 标签名 |
| `contentRefCount` | `Long` | 关联内容数 |

热门 AI capability 对象 `TopAiCapabilityResponse` 字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `capability` | `String` | AI capability |
| `invocationCount` | `Long` | 调用次数 |

### Health Data Structure Changes

建议扩展 `operations_health_check`，字段精确如下：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `probe_source` | `varchar(64) NOT NULL DEFAULT 'LOCAL'` | 采集来源，如 `LOCAL`、`DATABASE`、`REDIS`、`WORKER` |
| `probe_target` | `varchar(128) DEFAULT NULL` | 采集目标，如 datasource 名称、worker 名称或内部 endpoint |
| `details_json` | `text DEFAULT NULL` | 扩展诊断信息 JSON，只放非敏感信息 |

建议新增索引：

```sql
KEY `idx_operations_health_probe` (`probe_source`, `probe_target`, `checked_at`)
```

必须同步更新文件：

- `db/schema/operations.sql`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/model/entity/HealthCheckRecord.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/persistence/dataobject/HealthCheckDO.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/persistence/assembler/HealthCheckPersistenceAssembler.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/result/OperationsHealthSummaryResult.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/result/OperationsHealthPageResult.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/response/OperationsHealthSummaryResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/response/OperationsHealthPageResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/assembler/OperationsHealthInterfaceAssembler.java`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-types.ts`

## Task Breakdown

每个小任务应控制在 2-5 个主要文件；如发现必须超过 5 个文件，拆成更小 PR 或更小提交单元。

### T1 Health Schema And Model Fields

目标：让健康记录能追踪采集来源和目标。

文件：

- `db/schema/operations.sql`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/model/entity/HealthCheckRecord.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/persistence/dataobject/HealthCheckDO.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/persistence/assembler/HealthCheckPersistenceAssembler.java`

字段变更：

- `HealthCheckRecord` 增加 `probeSource`、`probeTarget`、`detailsJson`。
- `HealthCheckDO` 增加 `probeSource`、`probeTarget`、`detailsJson`。
- `HealthCheckPersistenceAssembler` 双向映射新增字段。

验收：

- 旧字段语义不变。
- 新字段允许旧数据使用默认值或空值。
- repository 现有分页和 latest-by-component 查询不破坏。

### T2 Health Application And Interface Fields

目标：健康 summary/page 接口返回采集来源和目标。

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/result/OperationsHealthSummaryResult.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/result/OperationsHealthPageResult.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/response/OperationsHealthSummaryResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/response/OperationsHealthPageResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/assembler/OperationsHealthInterfaceAssembler.java`

字段变更：

- Summary result/response 增加 `probeSource`、`probeTarget`。
- Page result/response 增加 `probeSource`、`probeTarget`、`detailsJson`。
- `detailsJson` 只在 page/detail 类明细响应中返回，dashboard 卡片不展示完整 JSON。

验收：

- `POST /api/operations/health/summary` 返回组件、状态、耗时、时间、来源。
- `POST /api/operations/health/page` 返回完整健康记录和诊断 JSON。

### T3 Health Collector

目标：新增后台健康采集入口，写入 `operations_health_check`。

建议文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthProbe.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthCollector.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/LocalOperationsHealthProbe.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthCollectorTest.java`

采集记录字段：

- `checkId`：使用现有 ID 生成策略。
- `component`：如 `admin-server`、`database`、`render-worker`。
- `healthStatus`：固定为 `UP`、`DEGRADED`、`DOWN`。
- `latencyMs`：采集耗时，失败时也记录。
- `message`：短说明，最大 1024 字符。
- `checkedAt`：采集完成时间。
- `probeSource`：`LOCAL`、`DATABASE`、`REDIS`、`WORKER`。
- `probeTarget`：具体目标名。
- `detailsJson`：非敏感诊断 JSON。

验收：

- collector 不在 dashboard 请求链路中同步执行。
- probe 单项失败不会阻断其他 probe 写入。
- 写入失败要有可测试的失败路径。

### T4a Health Trend Repository Query

目标：基于 `operations_health_check` 聚合健康趋势，不新增趋势表。

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/model/valueobject/HealthTrendBucket.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/repository/HealthCheckRepository.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/repository/impl/HealthCheckRepositoryImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/test/java/com/thundax/kuzhambu/operations/infra/health/repository/impl/HealthCheckRepositoryImplTest.java`

新增 repository 方法：

```java
List<HealthTrendBucket> listTrend(
        String component,
        String probeSource,
        Date periodStart,
        Date periodEnd,
        String bucketType);
```

`HealthTrendBucket` 字段：

- `bucket`
- `upCount`
- `degradedCount`
- `downCount`
- `avgLatencyMs`

验收：

- `bucketType=HOUR` 按小时聚合。
- `bucketType=DAY` 按日聚合。
- `component` 和 `probeSource` 为空时不过滤。
- 查询只读 `operations_health_check`。

### T4b Health Trend Application And Interface

目标：暴露健康趋势接口。

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/query/OperationsHealthTrendQuery.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/result/OperationsHealthTrendResult.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/request/OperationsHealthTrendRequest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/response/OperationsHealthTrendResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/OperationsHealthAdminController.java`

请求字段：

- `component`
- `probeSource`
- `periodStart`：`Date`
- `periodEnd`：`Date`
- `bucketType`：`HOUR`、`DAY`

响应字段：

- `bucket`
- `upCount`
- `degradedCount`
- `downCount`
- `avgLatencyMs`

验收：

- 新接口建议为 `POST /api/operations/health/trend`。
- 权限使用 `operations:health:view`。
- `bucket` 由后端返回。

### T5a Dashboard Application Contract

目标：建立 Dashboard application contract 和聚合服务骨架。

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/service/OperationsDashboardApplicationService.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/service/impl/OperationsDashboardApplicationServiceImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/result/OperationsDashboardOverviewResult.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/query/OperationsDashboardOverviewQuery.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/dashboard/service/impl/OperationsDashboardApplicationServiceImplTest.java`

验收：

- 默认 `periodType` 为 `WEEK`。
- `CUSTOM` 时校验 `periodStart` 和 `periodEnd`。
- 暂未接入真实统计源的字段返回 `0` 或空数组，不返回 `null`。
- 不直读其他业务域主表。

### T5b Dashboard Interface Contract

目标：提供 `/api/operations/dashboard/overview`。

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/controller/request/OperationsDashboardOverviewRequest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/controller/OperationsDashboardAdminController.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/controller/response/OperationsDashboardOverviewResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/assembler/OperationsDashboardInterfaceAssembler.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/controller/OperationsDashboardAdminControllerTest.java`

验收：

- controller 路径为 `/api/operations/dashboard`。
- overview 方法路径为 `overview`。
- 权限为 `operations:dashboard:view`。
- response 不复用其他业务域 response 类。

### T6 Admin Web Dashboard Service

目标：前端先建立 dashboard 数据契约。

文件：

- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-types.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-service.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-service-contract.test.ts`

前端类型字段：

- `DashboardOverview.periodStart`
- `DashboardOverview.periodEnd`
- `DashboardOverview.contentCount`
- `DashboardOverview.translatedContentCount`
- `DashboardOverview.imageReadyContentCount`
- `DashboardOverview.visualAssetReadyContentCount`
- `DashboardOverview.shareVisitCount`
- `DashboardOverview.aiInvocationCount`
- `DashboardOverview.aiFailedInvocationCount`
- `DashboardOverview.aiAvgLatencyMs`
- `DashboardOverview.aiTotalCostAmount`
- `DashboardOverview.searchCount`
- `DashboardOverview.qaCount`
- `DashboardOverview.avgSearchLatencyMs`
- `DashboardOverview.tagCoverageRate`
- `DashboardOverview.unhealthyComponentCount`
- `DashboardOverview.runningTaskCount`
- `DashboardOverview.failedTaskCount`
- `DashboardOverview.healthSummaries`
- `DashboardOverview.contentGrowthSeries`
- `DashboardOverview.searchTrendSeries`
- `DashboardOverview.qaTrendSeries`

操作：

- `getDashboardOverview(query)` 调用 `/operations/dashboard/overview`。
- `getHealthTrend(query)` 调用 `/operations/health/trend`。
- `DashboardPeriodType` 固定为 `"WEEK" | "MONTH" | "CUSTOM"`。
- `HealthTrendBucketType` 固定为 `"HOUR" | "DAY"`。

验收：

- contract test 断言 URL 和 body 字段精确匹配。
- 无权限时页面层不调用 service。

### T7 Admin Web Dashboard Page

目标：实现 `/operations/dashboard` 页面。

文件：

- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.css`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.test.tsx`
- `kuzhambu-apps/admin-web/src/router/index.tsx`

控件和操作：

- 顶部周期控件：
  - `Select`：选项 `本周`、`本月`、`自定义`。
  - 自定义时显示两个日期输入或 `DatePicker.RangePicker`。
  - `DatePicker.RangePicker`：只在 `Select=自定义` 时可见。
  - `Button`：`刷新`，点击后重新请求 dashboard overview 和 health trend。
- 指标卡片区：
  - `Card`：内容总数。
  - `Card`：翻译覆盖率。
  - `Card`：AI 调用数和失败数。
  - `Card`：搜索/问答次数。
  - `Card`：健康异常组件数。
  - `Card`：运行中/失败任务数。
- 趋势区：
  - 内容增长小折线图。
  - 搜索趋势小折线图。
  - 问答趋势小折线图。
  - 标签新增趋势小折线图。
- 健康区：
  - 组件状态列表。
  - 每行展示组件名、状态 tag、probe source、latency、checkedAt。
  - `Button`：查看明细，跳到健康明细区域或打开抽屉。
  - `Select`：健康趋势粒度，选项 `按小时`、`按日`。
  - 小折线图：展示 `upCount`、`degradedCount`、`downCount` 或平均耗时。
  - 健康明细抽屉：
    - `Drawer` 标题为“健康明细”。
    - `Input`：组件名筛选。
    - `Select`：`probeSource`，选项 `全部`、`LOCAL`、`DATABASE`、`REDIS`、`WORKER`。
    - `Select`：`healthStatus`，选项 `全部`、`UP`、`DEGRADED`、`DOWN`。
    - `Button`：查询。
    - `Button`：重置。
    - `Table` 列：组件、状态、采集来源、采集目标、耗时、检查时间、说明。
    - `Pagination`：翻页时请求 `/operations/health/page`。
    - `Button`：关闭抽屉。
- 热门排行区：
  - 热门内容列表。
  - 热门 query 列表。
  - 热门标签列表。
  - 热门 AI capability 列表。
- 运维入口区：
  - `Button` 或 `Link`：任务台账，跳转 `/operations/tasks`。
  - `Button` 或 `Link`：备份恢复，跳转 `/operations/backup-restore`。
  - `Button` 或 `Link`：清理维护，跳转 `/operations/cleanup`。
  - 报表记录若 `/operations/reports` 未实现，则不展示可点击入口，或展示禁用态并标注“待接入”。

状态：

- loading：关键区域显示 `Spin` 或骨架。
- empty：显示 `Empty`，文案为“暂无运营看板数据”或“暂无健康趋势”。
- error：显示 `Alert`，提供 `重试` 按钮。
- no permission：无 `operations:dashboard:view` 时显示无权限态，不发请求。

验收：

- 页面标题为“运营看板”。
- 用户点击“刷新”后重新请求。
- 切换周期后请求 body 中 `periodType` 改变。
- 自定义周期为空时不发请求，并显示表单校验提示。
- 打开健康明细抽屉后才请求 `/operations/health/page`。
- 切换健康趋势粒度后请求 body 中 `bucketType` 改变。
- 健康长 message 不撑破卡片或表格。
- 移动宽度下指标卡和列表单列排列。

### T8 Move Health Summary Out Of Tasks Page

目标：任务页只保留长任务台账。

文件：

- `kuzhambu-apps/admin-web/src/pages/operations/tasks/tasks-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/tasks/tasks-service.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/tasks/tasks-types.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/tasks/tasks-page.test.tsx`

变更：

- 删除 `getHealthSummary` 调用。
- 删除健康摘要卡片。
- 保留任务筛选控件：来源域输入框、任务类型输入框、状态 `Select`、重置按钮。
- 保留任务表格、分页、详情抽屉。
- 顶部增加返回运营看板入口：`Link` 到 `/operations/dashboard`。

验收：

- 任务页不请求 `/operations/health/summary`。
- 任务筛选、分页、详情抽屉行为不变。

### T9 Menu And Permission Alignment

目标：菜单、路由、权限与页面一致。

文件：

- `db/data-source/system.json`
- `db/data/system.sql`
- `kuzhambu-apps/admin-web/src/router/index.tsx`

检查点：

- `运营看板` URL 为 `/operations/dashboard`。
- `运营看板` perms 包含 `operations:dashboard:view`。
- 如新增健康明细独立路由，必须决定是否新增菜单；不新增菜单时只作为 dashboard 内部 drill-down。
- `/operations/reports` 未实现时，不在 dashboard 提供可点击跳转。

验收：

- 菜单种子与前端路由一致。
- 未授权用户不会看到敏感聚合数据。

### T10 Coverage Docs And Runbook Cleanup

目标：实现完成后收口文档。

文件：

- `docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md`
- `docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-HEALTH-METRICS.md`

变更：

- 将“仪表盘与聚合展示”状态调整为真实交付状态。
- 将“健康检查与运行状态”写清采集源、趋势、页面入口的完成状态。
- 保留自动备份、恢复写入阻断、System 日志/审计入口的未完成项。
- 任务关闭前删除本 RUNBOOK。

## Validation Commands

后端：

```sh
cd kuzhambu-servers
mvn -pl biz/operations -am spotless:apply
mvn -pl biz/operations -am spotless:check
mvn -pl biz/operations -am checkstyle:check
mvn -pl biz/operations -am test
```

如修改 Classics、AI、Discovery 或 Knowledge summary 字段，补跑对应模块窄测试。

前端：

```sh
cd kuzhambu-apps
npm --workspace kuzhambu-admin-web run format
npm run format:check
npm run lint
npm --workspace kuzhambu-admin-web run test
```

手工冒烟：

- 用具备 `operations:dashboard:view` 的管理员登录。
- 打开 `/operations/dashboard`。
- 切换“本周”“本月”“自定义”周期。
- 点击“刷新”。
- 查看健康组件列表和健康趋势。
- 点击任务台账、备份恢复、清理维护入口。
- 移除 `operations:dashboard:view` 后确认页面不请求 dashboard 接口。
- 移除 `operations:health:view` 后确认健康明细入口不可用或不请求明细接口。

## Review Checklist

- [ ] 数据结构变更已精确同步到 schema、DO、domain entity、assembler、result、response 和前端 type。
- [ ] Dashboard 后端 request/result/response 独立建模。
- [ ] Dashboard controller 使用 `operations:dashboard:view`。
- [ ] Health trend controller 使用 `operations:health:view`。
- [ ] 前端 `/operations/dashboard` 路由已接入。
- [ ] 前端控件覆盖周期选择、刷新、健康明细、趋势、排行和运维入口。
- [ ] `/operations/tasks` 不再请求健康摘要。
- [ ] `/operations/reports` 未实现时没有可点击死链。
- [ ] 菜单 JSON 与 SQL 对齐。
- [ ] 后端格式化、checkstyle 和测试通过。
- [ ] 前端 format、lint 和测试通过。
- [ ] 覆盖矩阵已更新。
- [ ] 任务关闭前已删除本 RUNBOOK。
