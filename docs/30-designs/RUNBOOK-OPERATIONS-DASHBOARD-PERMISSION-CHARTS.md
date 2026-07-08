# RUNBOOK Operations Dashboard 按权限裁剪聚合图表

## 目标

把 Operations 看板从“部分完成”推进到“已完成”：用户具备 `operations:dashboard:view` 时可以进入 `/operations/dashboard`，但页面中的聚合数据、图表、排行、健康告警和运维入口必须继续按当前用户权限裁剪。

完成后：

- 后端不把未授权聚合字段返回给前端。
- 前端不渲染未授权控件、图表、排行和入口。
- 前端不发起未授权关联接口请求。
- `operations:dashboard:view` 只代表进入看板壳，不代表查看所有图表。

## 已确认边界

- 后端实现只落在 `kuzhambu-servers/biz/operations`。
- 前端实现只落在 `kuzhambu-apps/admin-web/src/pages/operations/dashboard`。
- 权限读取复用 `kuzhambu-common-security` 当前安全上下文和 `PermissionAuthorizationService`，不从 Operations 直接读取 System 表。
- 不新增权限码、不新增 dashboard 权限配置表、不新增第二套权限体系。
- 不改 Classics、Discovery、AI、Knowledge provider 的 facade contract 和内部查询。
- 对跨域 provider 的处理边界：无该域任何授权字段时不调用该域 facade；有该域部分授权字段时可以调用该域 facade，但 Operations 只装配授权字段。
- 未授权字段返回 `null`，不是 `0` 或空数组；`0` 表示真实统计为零，空数组表示有权但无数据。
- 本次不新增报表入口；`dashboard-page.tsx` 不添加 `operations:report:view` 入口卡。

## 数据结构变更

### 后端 application 新增权限快照

文件：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/support/OperationsDashboardPermissionSnapshot.java`

新增 `record OperationsDashboardPermissionSnapshot`，字段固定为：

| 字段 | Java 类型 | 说明 |
| --- | --- | --- |
| `canViewClassicsContentSummary` | `boolean` | 任一内容查看权限满足时为 true：`classics:content:view`、`classics:sancai:view`、`classics:wangqi:view`、`classics:mingcustoms:view` |
| `canViewClassicsSharingSummary` | `boolean` | `classics:sharing:view` |
| `canViewDiscoverySearchSummary` | `boolean` | `discovery:search:view` |
| `canViewDiscoveryQaSummary` | `boolean` | `discovery:qa:view` |
| `canViewAiInvocationSummary` | `boolean` | `ai:invocation:view` |
| `canViewKnowledgeTaxonomySummary` | `boolean` | `knowledge:taxonomy:view` |
| `canViewHealthSummary` | `boolean` | `operations:health:view` |
| `canViewTaskSummary` | `boolean` | `operations:task:view` |

新增派生方法：

| 方法 | 返回类型 | 说明 |
| --- | --- | --- |
| `canLoadClassicsSummary()` | `boolean` | `canViewClassicsContentSummary || canViewClassicsSharingSummary` |
| `canLoadDiscoverySummary()` | `boolean` | `canViewDiscoverySearchSummary || canViewDiscoveryQaSummary` |
| `canLoadAiSummary()` | `boolean` | `canViewAiInvocationSummary` |
| `canLoadKnowledgeSummary()` | `boolean` | `canViewKnowledgeTaxonomySummary` |
| `hasAnyChartPermission()` | `boolean` | 任一图表权限为 true，用于前端空态对应的后端测试语义 |

### 后端 application 新增权限解析器

文件：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/support/OperationsDashboardPermissionResolver.java`

新增 `@Component OperationsDashboardPermissionResolver`：

| 成员 | 类型 | 说明 |
| --- | --- | --- |
| `permissionAuthorizationService` | `PermissionAuthorizationService` | 通过构造器注入 |
| `resolve()` | `OperationsDashboardPermissionSnapshot` | 从当前登录上下文解析权限快照 |

`resolve()` 内使用 `permissionAuthorizationService.isPermittedAny(...)`，保留现有前缀权限和 `super` 语义。

### 后端 application 修改 summary gateway 入参

文件：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/support/OperationsDashboardSummaryGateway.java`

方法签名从：

```java
OperationsCrossDomainSummary loadSummary(Date periodStart, Date periodEnd, String bucketType);
```

改为：

```java
OperationsCrossDomainSummary loadSummary(
        Date periodStart,
        Date periodEnd,
        String bucketType,
        OperationsDashboardPermissionSnapshot permissions);
```

### 后端 application 响应字段裁剪

文件：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/result/OperationsDashboardOverviewResult.java`

不新增字段，不删除字段。以下现有字段必须支持按权限为 `null`：

| 权限 | 字段 |
| --- | --- |
| `classics:content:view` 或 `classics:sancai:view` 或 `classics:wangqi:view` 或 `classics:mingcustoms:view` | `contentCount`、`translatedContentCount`、`imageReadyContentCount`、`visualAssetReadyContentCount`、`contentGrowthSeries`、`topContents` |
| `classics:sharing:view` | `shareVisitCount` |
| `discovery:search:view` | `searchCount`、`avgSearchLatencyMs`、`searchTrendSeries`、`topQueries` |
| `discovery:qa:view` | `qaCount`、`qaTrendSeries` |
| `ai:invocation:view` | `aiInvocationCount`、`aiSucceededInvocationCount`、`aiFailedInvocationCount`、`aiAvgLatencyMs`、`aiTotalCostAmount`、`topAiCapabilities` |
| `knowledge:taxonomy:view` | `tagCoverageRate`、`tagGrowthSeries`、`topTags` |
| `operations:health:view` | `unhealthyComponentCount`、`activeAlertCount`、`criticalAlertCount`、`warningAlertCount`、`highestAlertLevel`、`latestAlert`、`healthSummaries` |
| `operations:task:view` | `runningTaskCount`、`failedTaskCount`、`taskStatusSummaries` |

`periodStart` 和 `periodEnd` 不按图表权限裁剪，只要 overview 调用成功就返回。

### 后端 interface 响应字段裁剪

文件：`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/controller/response/OperationsDashboardOverviewResponse.java`

不新增字段，不删除字段。字段可空语义必须和 `OperationsDashboardOverviewResult` 保持一致。

文件：`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/assembler/OperationsDashboardInterfaceAssembler.java`

必须修正当前 assembler 的空集合兜底：

| 当前字段 | 当前行为 | 完成态行为 |
| --- | --- | --- |
| `contentGrowthSeries`、`searchTrendSeries`、`qaTrendSeries`、`tagGrowthSeries` | `null` 转 `List.of()` | `null` 保持 `null` |
| `healthSummaries` | `null` 转 `List.of()` | `null` 保持 `null` |
| `taskStatusSummaries` | `null` 转 `List.of()` | `null` 保持 `null` |
| `topContents`、`topQueries`、`topTags`、`topAiCapabilities` | `null` 转 `List.of()` | `null` 保持 `null` |
| `latestAlert` | 额外读取当前权限后裁剪 | 删除 assembler 内权限判断，完全信任 application 已裁剪结果 |

### 前端本地权限结构

文件：`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.tsx`

新增页面私有接口 `DashboardPermissionCapabilities`：

| 字段 | TypeScript 类型 | 说明 |
| --- | --- | --- |
| `canViewDashboard` | `boolean` | `operations:dashboard:view` |
| `canViewClassicsContentSummary` | `boolean` | 任一内容查看权限满足时为 true：`classics:content:view`、`classics:sancai:view`、`classics:wangqi:view`、`classics:mingcustoms:view` |
| `canViewClassicsSharingSummary` | `boolean` | `classics:sharing:view` |
| `canViewDiscoverySearchSummary` | `boolean` | `discovery:search:view` |
| `canViewDiscoveryQaSummary` | `boolean` | `discovery:qa:view` |
| `canViewAiInvocationSummary` | `boolean` | `ai:invocation:view` |
| `canViewKnowledgeTaxonomySummary` | `boolean` | `knowledge:taxonomy:view` |
| `canViewHealthSummary` | `boolean` | `operations:health:view` |
| `canManageHealthAlert` | `boolean` | `operations:health:manage` |
| `canViewTaskSummary` | `boolean` | `operations:task:view` |
| `hasAnyChartPermission` | `boolean` | 内容、分享、搜索、问答、AI、标签、健康、任务任一图表权限为 true |

新增页面私有方法 `resolveDashboardPermissionCapabilities(): DashboardPermissionCapabilities`，方法内部只调用 `hasPermission(...)`，不读取后端响应推断权限。

### 前端入口结构

文件：`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.tsx`

修改页面私有接口 `OperationEntry`：

| 字段 | 修改前类型 | 修改后类型 | 说明 |
| --- | --- | --- | --- |
| `permission` | `string` | 删除 | 单权限字段不足以表达备份/恢复任一权限 |
| `permissions` | 无 | `string[]` | 任一权限满足时展示入口 |

`operationEntries` 固定为：

| 入口 title | `permissions` |
| --- | --- |
| `任务台账` | `["operations:task:view"]` |
| `备份恢复` | `["operations:backup:view", "operations:restore:view"]` |
| `清理维护` | `["operations:cleanup:view"]` |
| `系统日志` | `["system:log:view"]` |
| `审计日志` | `["audit:view"]` |

## 权限矩阵

| 看板区域 | 控件/展示 | 后端字段 | 所需权限 |
| --- | --- | --- | --- |
| 页面壳 | 页面标题、周期切换、刷新按钮 | `periodStart`、`periodEnd` | `operations:dashboard:view` |
| 内容指标卡 | `Statistic` 标题“内容总量”及副文案 | `contentCount`、`translatedContentCount`、`imageReadyContentCount`、`visualAssetReadyContentCount` | 任一内容查看权限：`classics:content:view`、`classics:sancai:view`、`classics:wangqi:view`、`classics:mingcustoms:view` |
| 分享访问文案 | 内容指标卡副文案中的“分享访问” | `shareVisitCount` | `classics:sharing:view` |
| 内容增长趋势 | `TrendPanel` 标题“内容增长趋势” | `contentGrowthSeries` | 任一内容查看权限 |
| 热门内容 | `RankingList` 标题“热门内容” | `topContents` | 任一内容查看权限 |
| 搜索指标卡 | `Statistic` 标题“搜索 / 问答”中的搜索数和平均搜索延迟 | `searchCount`、`avgSearchLatencyMs` | `discovery:search:view` |
| 问答指标卡 | `Statistic` 标题“搜索 / 问答”中的问答数 | `qaCount` | `discovery:qa:view` |
| 搜索趋势 | `TrendPanel` 标题“搜索趋势” | `searchTrendSeries` | `discovery:search:view` |
| 问答趋势 | 新增 `TrendPanel` 实例，标题“问答趋势” | `qaTrendSeries` | `discovery:qa:view` |
| 热门搜索 | `RankingList` 标题“热门搜索” | `topQueries` | `discovery:search:view` |
| AI 指标卡 | `Statistic` 标题“AI 调用成功率” | `aiInvocationCount`、`aiSucceededInvocationCount`、`aiFailedInvocationCount`、`aiAvgLatencyMs`、`aiTotalCostAmount` | `ai:invocation:view` |
| AI 能力排行 | `RankingList` 标题“AI 能力” | `topAiCapabilities` | `ai:invocation:view` |
| 标签排行 | `RankingList` 标题“标签覆盖” | `topTags` | `knowledge:taxonomy:view` |
| 标签趋势 | 新增 `TrendPanel` 实例，标题“标签增长趋势” | `tagGrowthSeries` | `knowledge:taxonomy:view` |
| 健康指标卡 | `Statistic` 标题“异常组件 / 失败任务”中的异常组件 | `unhealthyComponentCount` | `operations:health:view` |
| 任务指标卡 | `Statistic` 标题“异常组件 / 失败任务”中的失败任务、运行中任务 | `failedTaskCount`、`runningTaskCount` | `operations:task:view` |
| 告警横幅 | `Alert`、按钮“查看告警” | `activeAlertCount`、`criticalAlertCount`、`latestAlert` | `operations:health:view` |
| 健康巡检卡 | `Card` 标题“健康巡检”、按钮式健康项、链接“查看全部” | `healthSummaries` | `operations:health:view` |
| 健康趋势卡 | `Card` 标题“健康趋势” | `getHealthTrend(...)` 响应 | `operations:health:view` |
| 告警抽屉 | `KuzhambuDrawer` 内告警列表、确认、恢复按钮 | `getHealthAlerts(...)` 响应 | 查看：`operations:health:view`；确认/恢复：`operations:health:manage` |
| 运维入口：任务台账 | 入口卡“任务台账” | 无 | `operations:task:view` |
| 运维入口：备份恢复 | 入口卡“备份恢复” | 无 | `operations:backup:view` 或 `operations:restore:view` |
| 运维入口：清理维护 | 入口卡“清理维护” | 无 | `operations:cleanup:view` |
| 运维入口：系统日志 | 入口卡“系统日志” | 无 | `system:log:view` |
| 运维入口：审计日志 | 入口卡“审计日志” | 无 | `audit:view` |

## 任务拆分

### 任务 1：后端权限快照与跨域 facade 调用裁剪

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/support/OperationsDashboardPermissionSnapshot.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/support/OperationsDashboardPermissionResolver.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/support/OperationsDashboardSummaryGateway.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/support/DefaultOperationsDashboardSummaryGateway.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/dashboard/support/DefaultOperationsDashboardSummaryGatewayTest.java`

动作：

- 新增权限快照和权限解析器。
- 修改 `OperationsDashboardSummaryGateway.loadSummary(...)` 入参，增加 `OperationsDashboardPermissionSnapshot permissions`。
- `DefaultOperationsDashboardSummaryGateway` 根据 `permissions.canLoadClassicsSummary()`、`canLoadDiscoverySummary()`、`canLoadAiSummary()`、`canLoadKnowledgeSummary()` 决定是否调用对应 facade。
- 未调用的 facade 在 `OperationsCrossDomainSummary` 中返回 `null`。
- 测试验证无权限时四个 facade 均不调用；只有搜索权限时只调用 Discovery facade；只有任务/健康权限时四个跨域 facade 均不调用。

验收：

- 未授权跨域 facade 没有 Mockito interaction。
- `super`、父级权限和精确权限都能得到正确快照。

### 任务 2：后端 overview 字段级裁剪

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/service/impl/OperationsDashboardApplicationServiceImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/result/OperationsDashboardOverviewResult.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/dashboard/service/impl/OperationsDashboardApplicationServiceImplTest.java`

动作：

- `overview(...)` 开始处调用 `OperationsDashboardPermissionResolver.resolve()`。
- 调用 summary gateway 时传入权限快照。
- 按权限快照装配 `OperationsDashboardOverviewResult` 字段：授权字段正常取值，未授权字段传 `null`。
- `canViewHealthSummary == false` 时不调用 `healthCheckRepository.listLatestByComponent()` 和 `healthAlertRepository.listOpenSummary()`。
- `canViewTaskSummary == false` 时不调用 `longTaskSnapshotRepository.page(...)`。
- `OperationsDashboardOverviewResult` 不新增字段；字段裁剪在 `OperationsDashboardApplicationServiceImpl` 内完成，不改变 JSON 字段。

验收：

- 只有 `operations:dashboard:view` 时，除 `periodStart/periodEnd` 外所有聚合字段为 `null`。
- 只有 `operations:health:view` 时，只返回健康和告警字段，不返回任务、内容、搜索、问答、AI、标签字段。
- 只有 `operations:task:view` 时，只返回任务字段，不查询健康仓储。
- 只有 `discovery:search:view` 时，返回 `searchCount/avgSearchLatencyMs/searchTrendSeries/topQueries`，`qaCount/qaTrendSeries` 为 `null`。

### 任务 3：后端 interface 保留 null 语义

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/assembler/OperationsDashboardInterfaceAssembler.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/controller/response/OperationsDashboardOverviewResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/controller/OperationsDashboardAdminControllerTest.java`

动作：

- `OperationsDashboardOverviewResponse` 字段不增不删。
- `OperationsDashboardInterfaceAssembler.toResponse(...)` 删除健康权限读取逻辑，不再使用 `KuzhambuContextHolder` 和 `PrefixPermissionMatcher`。
- 所有 `List` 字段保持 application 的 `null` 语义；只有 application 返回非空 list 时才 map。
- `latestAlert` 直接映射 `result.getLatestAlert()`；application 返回 `null` 时 response 为 `null`。
- controller 上 `@HasPermission("operations:dashboard:view")` 不变。

验收：

- controller 测试覆盖：application result 中 list 为 `null` 时 response 对应 list 仍为 `null`。
- controller 测试覆盖：`latestAlert == null` 时不因当前权限做二次判断。
- contract 字段名不变。

### 任务 4：前端权限能力与请求裁剪

文件：

- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-types.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-service.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-service-contract.test.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.test.tsx`

动作：

- `dashboard-types.ts` 不新增后端字段；继续保持 overview 字段可空。
- `dashboard-service.ts` 中 `getDashboardOverview` 请求体仍只有 `periodType`、`periodStart`、`periodEnd`，不传权限。
- `dashboard-page.tsx` 新增页面私有 `DashboardPermissionCapabilities` 和 `resolveDashboardPermissionCapabilities()`。
- 页面渲染只使用 `DashboardPermissionCapabilities` 中的字段做权限分支，不在 JSX 中散落重复 `hasPermission(...)`。
- `trendQuery` 和 `alertQuery` 的 `enabled` 从 `canViewDashboard` 改为 `canViewDashboard && canViewHealthSummary`。
- `refreshDashboard()` 只在 `canViewHealthSummary` 时 invalidate `["operations", "health", "trend"]` 和 `["operations", "health", "alerts"]`。
- `OperationEntry` 删除 `permission: string`，新增 `permissions: string[]`。
- `visibleOperationEntries` 改为 `entry.permissions.some(hasPermission)`。
- 不新增报表入口。

控件和操作验收：

- `Segmented`“看板周期”：无论图表权限如何，只要有 `operations:dashboard:view` 就可切换周期，并只触发 overview 查询。
- `Button`“刷新”：无健康权限时只刷新 overview；有健康权限时同时刷新 overview、健康趋势、告警分页。
- 无 `operations:health:view` 时，不调用 `getHealthTrend`、`getHealthAlerts`，不展示 `Alert` 告警横幅，不展示“查看告警”按钮，不打开告警抽屉。
- 无 `operations:health:manage` 但有 `operations:health:view` 时，告警抽屉可打开，但不展示“确认”和“恢复”操作按钮。
- 只有 `operations:restore:view` 时展示“备份恢复”入口卡。

### 任务 5：前端图表、卡片、排行和空态裁剪

文件：

- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.css`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.test.tsx`

动作：

- `Card`“内容总量”只在 `canViewClassicsContentSummary` 时渲染；副文案中的“分享访问”只在 `canViewClassicsSharingSummary` 时渲染。
- `Card`“搜索 / 问答”拆成一个条件指标卡：`canViewDiscoverySearchSummary || canViewDiscoveryQaSummary` 为 true 时渲染；只有搜索权限时标题为“搜索”，只展示搜索数和平均搜索延迟；只有问答权限时标题为“问答”，只展示问答数；两者都有时标题为“搜索 / 问答”，展示两者组合值。
- `Card`“AI 调用成功率”只在 `canViewAiInvocationSummary` 时渲染。
- `Card`“异常组件 / 失败任务”拆成一个条件指标卡：`canViewHealthSummary || canViewTaskSummary` 为 true 时渲染；只有健康权限时标题为“异常组件”，只展示异常组件数；只有任务权限时标题为“失败任务”，展示失败任务数和运行中任务副文案；两者都有时标题为“异常组件 / 失败任务”，展示组合值。
- `TrendPanel`“内容增长趋势”只在 `canViewClassicsContentSummary` 时渲染。
- `TrendPanel`“搜索趋势”只在 `canViewDiscoverySearchSummary` 时渲染。
- 新增 `TrendPanel`“问答趋势”，只在 `canViewDiscoveryQaSummary` 时渲染，数据源固定为 `overview?.qaTrendSeries`。
- 新增 `TrendPanel`“标签增长趋势”，只在 `canViewKnowledgeTaxonomySummary` 时渲染，数据源固定为 `overview?.tagGrowthSeries`。
- `RankingList`“热门内容”只在 `canViewClassicsContentSummary` 时渲染。
- `RankingList`“热门搜索”只在 `canViewDiscoverySearchSummary` 时渲染。
- `RankingList`“标签覆盖”只在 `canViewKnowledgeTaxonomySummary` 时渲染。
- `RankingList`“AI 能力”只在 `canViewAiInvocationSummary` 时渲染。
- `Card`“健康巡检”和 `Card`“健康趋势”只在 `canViewHealthSummary` 时渲染。
- `Title`“运维入口”保留；入口卡仍按各入口权限过滤。
- 如果用户只有 `operations:dashboard:view` 且没有任何图表权限，页面主体展示 `Empty` 文案“当前账号暂无可查看的看板图表”，但仍展示有权限的运维入口。

测试验收：

- 无图表权限时，不出现“内容总量”“搜索 / 问答”“AI 调用成功率”“异常组件 / 失败任务”“内容增长趋势”“搜索趋势”“健康巡检”“健康趋势”“热门内容”“热门搜索”“标签覆盖”“AI 能力”。
- 只有 `discovery:search:view` 时，出现搜索指标、搜索趋势、热门搜索；不出现问答趋势。
- 只有 `discovery:qa:view` 时，出现问答指标、问答趋势；不出现搜索趋势、热门搜索。
- 只有 `knowledge:taxonomy:view` 时，出现标签增长趋势和标签覆盖排行。
- 只有 `classics:sharing:view` 时，不展示内容总量卡；如果没有其他图表权限，分享访问也不单独成卡。

## 验证命令

后端窄验证：

```sh
cd kuzhambu-servers
mvn -pl biz/operations/kuzhambu-operations-application -Dtest=OperationsDashboardApplicationServiceImplTest,DefaultOperationsDashboardSummaryGatewayTest test
mvn -pl biz/operations/kuzhambu-operations-interface -Dtest=OperationsDashboardAdminControllerTest test
```

后端收口验证：

```sh
cd kuzhambu-servers
mvn -pl biz/operations -am spotless:apply
mvn -pl biz/operations -am spotless:check
mvn -pl biz/operations -am checkstyle:check
mvn -pl biz/operations -am test
```

前端窄验证：

```sh
cd kuzhambu-apps
pnpm --filter kuzhambu-admin-web run format
pnpm --filter kuzhambu-admin-web exec vitest run src/pages/operations/dashboard/dashboard-page.test.tsx src/pages/operations/dashboard/dashboard-service-contract.test.ts
```

前端收口验证：

```sh
cd kuzhambu-apps
pnpm run format:check
pnpm run lint
pnpm run test
```

## 收口标准

- 后端无该域任何权限时不调用该域跨域 facade。
- 后端同一跨域 facade 返回的混合数据按字段权限裁剪，未授权字段为 `null`。
- 后端无健康权限时不查询健康仓储；无任务权限时不查询任务仓储。
- interface assembler 不把 application 的 `null` list 转为空数组。
- 前端未授权图表、排行、卡片、按钮和入口不渲染。
- 前端未授权关联接口不请求。
- 变更只落在确认范围内，不改 System 业务行为。
- RUNBOOK 在任务关闭时删除，不作为长期治理文档保留。
