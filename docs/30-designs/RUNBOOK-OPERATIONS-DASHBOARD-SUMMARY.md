# RUNBOOK Operations Dashboard Summary

## 目标

将 Operations admin 仪表盘与聚合展示推进到完成态：`POST /operations/dashboard/overview` 必须接入 Classics、AI、Discovery、Knowledge 四个业务域的真实 summary，删除 `OperationsDashboardApplicationServiceImpl` 中四域统计的 `0`、`BigDecimal.ZERO` 和 `List.of()` 占位。

本任务关闭时：

- Dashboard 与 Report 使用同一组跨域 summary 来源。
- Operations 只返回本域独立 `Result` / `Response` / 前端 `Record`。
- Discovery `avgSearchLatencyMs` 来自真实搜索日志耗时字段，不再固定为 `0L`。
- 前端只展示后端真实数据；真实无数据时展示空态，不使用 mock、样例排行或硬编码替代。

## 非目标

- 不新增 Operations 聚合结果持久化表。
- 不让 Operations 直接读取其他业务域 `infra`、`mapper`、`dataobject`、`repository.impl` 或主表。
- 不让 Operations 直接调用 workers AI 接口。
- 不扩展 Knowledge `categoryDistributions` 到 dashboard；本轮只使用现有 `tagCoverageRate`、`topTags`、`monthlyNewTags` 映射出的 `tagGrowthSeries`。
- 不改造备份、恢复、清理、健康检查、长任务台账页面。
- 不引入第二套前端请求、权限、状态或样式体系。

## 已确认决策

- Discovery `avgSearchLatencyMs` 必须本轮补齐真实数据来源。
- 自定义时间范围 bucket 规则固定为：跨度 `<= 31` 天使用 `DAY`，跨度 `> 31` 天使用 `WEEK`。
- Dashboard 与 Report 抽取复用 Operations application 内部 summary gateway；Report 再包装为 section payload。
- 四域 summary 读取失败不得静默吞成 `0` 或空数组；本轮按异常暴露，后续如需局部降级再扩展 `summaryWarnings` 契约。
- 前端允许真实空态，但不得把后端缺字段或异常吞成空数据。

## 当前事实

- `DefaultOperationsReportMetricsGateway.java` 已通过 `ClassicsFacade`、`AiFacade`、`DiscoveryFacade`、`KnowledgeFacade` 为报表快照读取四域 summary。
- `OperationsDashboardApplicationServiceImpl.java` 当前只读取健康检查和长任务状态；四域统计仍返回固定占位。
- `ClassicsReportApplicationServiceImpl.java` 已提供真实 `contentCount`、覆盖率计数、分享访问、热门内容和内容增长序列。
- `AiReportApplicationServiceImpl.java` 已提供真实调用次数、成功失败、平均耗时、成本和热门 capability。
- `DiscoveryReportApplicationServiceImpl.java` 已提供真实搜索次数、问答次数、热门查询、搜索趋势和问答趋势；`avgSearchLatencyMs` 当前固定 `0L`。
- `KnowledgeReportApplicationServiceImpl.java` 已提供真实标签覆盖率、热门标签、分类分布和月新增标签。
- `SearchLog.java` 与 `SearchLogDO.java` 当前没有搜索耗时字段。
- Admin Web `operations/dashboard` 页面已有周期分段控件、刷新按钮、核心指标卡、趋势条形图、健康巡检列表、健康明细抽屉、排行列表和运维入口。

## 精确数据结构变更

### Discovery 搜索日志

新增字段用于记录一次搜索从 application 接收到请求到搜索结果生成或失败落库前的耗时。

文件：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/entity/SearchLog.java`

- 新增字段：`private Long searchLatencyMs;`
- 构造参数位置：放在 `groupTotalCount` 之后、`searchStatus` 之前。

文件：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/dataobject/SearchLogDO.java`

- 新增字段：`private Long searchLatencyMs;`
- 对应数据库列：`search_latency_ms BIGINT NULL`
- 字段位置：放在 `groupTotalCount` 之后、`searchStatus` 之前。

文件：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/assembler/SearchLogPersistenceAssembler.java`

- `toObject` 新增：`dataObject.setSearchLatencyMs(entity.getSearchLatencyMs());`
- `toDomain` 新增：`entity.setSearchLatencyMs(dataObject.getSearchLatencyMs());`

文件：`db/schema/discovery.sql`

- 表：`discovery_search_log`
- 新增列：`search_latency_ms`
- 类型：`BIGINT`
- 允许空：`NULL`
- 含义：搜索耗时，单位毫秒。
- 位置：放在 `group_total_count` 之后、`search_status` 之前。
- 说明：仓库当前业务域 schema 真相源固定在 `db/schema/`；本任务不新增独立 migration 目录。

### Operations Dashboard 契约

本轮不新增 dashboard response 字段，只将现有字段从真实 summary 填充。

后端 result 文件：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/result/OperationsDashboardOverviewResult.java`

- 保持现有字段：
  - `periodStart`
  - `periodEnd`
  - `contentCount`
  - `translatedContentCount`
  - `imageReadyContentCount`
  - `visualAssetReadyContentCount`
  - `shareVisitCount`
  - `aiInvocationCount`
  - `aiSucceededInvocationCount`
  - `aiFailedInvocationCount`
  - `aiAvgLatencyMs`
  - `aiTotalCostAmount`
  - `searchCount`
  - `qaCount`
  - `avgSearchLatencyMs`
  - `tagCoverageRate`
  - `unhealthyComponentCount`
  - `runningTaskCount`
  - `failedTaskCount`
  - `contentGrowthSeries`
  - `searchTrendSeries`
  - `qaTrendSeries`
  - `tagGrowthSeries`
  - `healthSummaries`
  - `taskStatusSummaries`
  - `topContents`
  - `topQueries`
  - `topTags`
  - `topAiCapabilities`

后端 response 文件：`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/controller/response/OperationsDashboardOverviewResponse.java`

- 保持与 result 同名字段，不新增 `categoryDistributions`，不新增 `summaryWarnings`。

前端类型文件：`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-types.ts`

- 保持 `OperationsDashboardOverviewRecord` 现有字段，与后端 response 一一对应。
- 不新增 `categoryDistributions`，不新增 `summaryWarnings`。

## 全量文件清单

### 后端必改文件

- `db/schema/discovery.sql`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/entity/SearchLog.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/dataobject/SearchLogDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/assembler/SearchLogPersistenceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/report/service/impl/DiscoveryReportApplicationServiceImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGateway.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/service/impl/OperationsDashboardApplicationServiceImpl.java`

### 后端新增文件

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/support/OperationsDashboardSummaryGateway.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/support/DefaultOperationsDashboardSummaryGateway.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/support/OperationsDashboardSummaryModels.java`

### 后端测试文件

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/report/service/impl/DiscoveryReportApplicationServiceImplTest.java`
- 新增 `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/search/persistence/assembler/SearchLogPersistenceAssemblerTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGatewayTest.java`
- 新增 `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/dashboard/support/DefaultOperationsDashboardSummaryGatewayTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/dashboard/service/impl/OperationsDashboardApplicationServiceImplTest.java`

### 前端文件

- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-types.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-service.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-service-contract.test.ts`

## 字段映射矩阵

| Operations Dashboard 字段 | 来源 summary | 来源字段 |
| --- | --- | --- |
| `contentCount` | Classics | `contentCount` |
| `translatedContentCount` | Classics | `translatedContentCount` |
| `imageReadyContentCount` | Classics | `imageReadyContentCount` |
| `visualAssetReadyContentCount` | Classics | `visualAssetReadyContentCount` |
| `shareVisitCount` | Classics | `shareVisitCount` |
| `contentGrowthSeries[].bucket` | Classics | `contentGrowthSeries[].bucket` |
| `contentGrowthSeries[].count` | Classics | `contentGrowthSeries[].createdCount` |
| `topContents[]` | Classics | `topContents[]` |
| `aiInvocationCount` | AI | `invocationCount` |
| `aiSucceededInvocationCount` | AI | `succeededInvocationCount` |
| `aiFailedInvocationCount` | AI | `failedInvocationCount` |
| `aiAvgLatencyMs` | AI | `avgLatencyMs` |
| `aiTotalCostAmount` | AI | `totalCostAmount` |
| `topAiCapabilities[]` | AI | `topCapabilities[]` |
| `searchCount` | Discovery | `searchCount` |
| `qaCount` | Discovery | `qaCount` |
| `avgSearchLatencyMs` | Discovery | `avgSearchLatencyMs` |
| `searchTrendSeries[].bucket` | Discovery | `searchTrendSeries[].bucket` |
| `searchTrendSeries[].count` | Discovery | `searchTrendSeries[].searchCount` |
| `qaTrendSeries[].bucket` | Discovery | `qaTrendSeries[].bucket` |
| `qaTrendSeries[].count` | Discovery | `qaTrendSeries[].qaCount` |
| `topQueries[]` | Discovery | `topQueries[]` |
| `tagCoverageRate` | Knowledge | `tagCoverageRate` |
| `tagGrowthSeries[].bucket` | Knowledge | `monthlyNewTags[].bucket` |
| `tagGrowthSeries[].count` | Knowledge | `monthlyNewTags[].tagCount` |
| `topTags[]` | Knowledge | `topTags[]` |

## 目标架构

Operations application 内部新增或抽取一个 summary gateway，Dashboard 与 Report 共同使用：

- 输入：`periodStart`、`periodEnd`、`bucketType`
- 输出：Operations application 内部 summary 快照模型
- 依赖：
  - `ClassicsFacade.summary(ClassicsSummaryFacadeRequest)`
  - `AiFacade.summary(AiReportSummaryFacadeRequest)`
  - `DiscoveryFacade.summary(DiscoverySummaryFacadeRequest)`
  - `KnowledgeFacade.summary(KnowledgeSummaryFacadeRequest)`

gateway 只返回 Operations application 内部模型；`OperationsDashboardApplicationServiceImpl` 组装 `OperationsDashboardOverviewResult`；`OperationsDashboardInterfaceAssembler` 组装 `OperationsDashboardOverviewResponse`。

健康检查和长任务状态继续由 Operations 自有 repository 提供，不进入跨域 summary gateway。

## 小任务拆分

### 任务 1：Discovery 搜索耗时真实落库

目标：让 `avgSearchLatencyMs` 有真实字段来源。

涉及文件控制在 5 个核心文件：

- `db/schema/discovery.sql`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/entity/SearchLog.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/dataobject/SearchLogDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/assembler/SearchLogPersistenceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImpl.java`

实现要求：

- `db/schema/discovery.sql` 为 `discovery_search_log` 增加 `search_latency_ms BIGINT NULL`。
- 在 `SearchApplicationServiceImpl.search` 开始处记录 `startNanos = System.nanoTime()`。
- 成功路径 `buildSucceededSearchLog` 写入 `searchLatencyMs`。
- 失败路径 `buildFailedSearchLog` 写入 `searchLatencyMs`。
- 耗时计算使用 `TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos)`。
- 不使用前端时间、HTTP 网关时间或 QA trace 耗时代替搜索耗时。

测试文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImplTest.java`
- 新增 `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/search/persistence/assembler/SearchLogPersistenceAssemblerTest.java`

验收：

- 成功搜索日志 `searchLatencyMs` 非空且 `>= 0`。
- 失败搜索日志 `searchLatencyMs` 非空且 `>= 0`。
- assembler 往返不丢字段。
- `db/schema/discovery.sql` 包含 `` `search_latency_ms` bigint `` 或等价 MySQL 8 DDL。

### 任务 2：Discovery summary 计算真实平均搜索耗时

目标：删除 `DiscoveryReportApplicationServiceImpl.summary` 中 `avgSearchLatencyMs` 固定 `0L`。

涉及文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/report/service/impl/DiscoveryReportApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/report/service/impl/DiscoveryReportApplicationServiceImplTest.java`

实现要求：

- 仅统计 `searchLatencyMs != null` 的搜索日志。
- 没有可统计耗时日志时返回 `0L`，这是“真实无样本”，不是占位。
- 平均值使用四舍五入到毫秒：`Math.round(...)`。
- 保持 `topQueries`、`searchTrendSeries`、`qaTrendSeries` 现有语义不变。

验收：

- 多条日志 `[100, 200, null]` 返回 `150L`。
- 全部为 `null` 返回 `0L`。
- 不影响搜索次数和趋势统计。

### 任务 3：Operations 复用四域 summary gateway

目标：Dashboard 与 Report 使用一致的跨域 summary 来源。

涉及文件控制在 5 个核心文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGateway.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/OperationsReportMetricsGateway.java`
- 新增 `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/support/OperationsDashboardSummaryGateway.java`
- 新增 `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/support/DefaultOperationsDashboardSummaryGateway.java`
- 新增或复用内部模型文件：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/support/OperationsDashboardSummaryModels.java`

实现要求：

- `DefaultOperationsDashboardSummaryGateway` 注入四域 facade。
- gateway 负责构造四域 summary request。
- Report gateway 复用 dashboard summary gateway 的结果，再包装成 `OperationsReportSection`。
- 不在 gateway 内吞异常并转成空对象。
- 四域 facade 返回 `null` 时抛出明确异常，避免 dashboard 显示伪 0。

内部模型建议：

- `OperationsCrossDomainSummary`
  - `ClassicsSummaryFacadeResponse classicsSummary`
  - `AiReportSummaryFacadeResponse aiSummary`
  - `DiscoverySummaryFacadeResponse discoverySummary`
  - `KnowledgeSummaryFacadeResponse knowledgeSummary`

测试文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGatewayTest.java`
- 新增 `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/dashboard/support/DefaultOperationsDashboardSummaryGatewayTest.java`

验收：

- Dashboard gateway 对四域 facade 各调用一次。
- Report gateway section payload 来自同一 summary gateway。
- 任一 summary 为 `null` 时测试断言失败路径明确。

### 任务 4：Operations Dashboard overview 替换占位

目标：`OperationsDashboardApplicationServiceImpl.overview` 返回真实四域统计。

涉及文件控制在 5 个核心文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/service/impl/OperationsDashboardApplicationServiceImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/result/OperationsDashboardOverviewResult.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/assembler/OperationsDashboardInterfaceAssembler.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/controller/response/OperationsDashboardOverviewResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/dashboard/service/impl/OperationsDashboardApplicationServiceImplTest.java`

实现要求：

- `resolvePeriodRange` 保持现有周、月、自定义校验。
- 新增或内聚 `resolveBucketType(periodStart, periodEnd, periodType)`：
  - `WEEK` -> `DAY`
  - `MONTH` -> `WEEK`
  - `CUSTOM` 且跨度 `<= 31` 天 -> `DAY`
  - `CUSTOM` 且跨度 `> 31` 天 -> `WEEK`
- 字段映射：
  - Classics `contentGrowthSeries.createdCount` -> Dashboard `BucketCountResult.count`
  - Discovery `searchTrendSeries.searchCount` -> Dashboard `BucketCountResult.count`
  - Discovery `qaTrendSeries.qaCount` -> Dashboard `BucketCountResult.count`
  - Knowledge `monthlyNewTags.tagCount` -> Dashboard `tagGrowthSeries.count`
- `topAiCapabilities` 必须从 AI summary 填充，当前页面不展示也必须返回，保证契约完整。
- `tagCoverageRate` 直接使用 Knowledge summary 的 `BigDecimal`。
- 保留健康检查与任务状态现有逻辑。

验收：

- 测试断言每个 dashboard 字段来自对应 summary。
- 测试断言 `WEEK`、`MONTH`、`CUSTOM <= 31 天`、`CUSTOM > 31 天` 的 bucket type。
- 测试断言没有四域 summary 时不返回伪 0。
- `OperationsDashboardOverviewResponse` 与前端 `OperationsDashboardOverviewRecord` 字段保持一致。

### 任务 5：Admin Web Operations Dashboard 前端闭环

目标：页面控件和展示区域全部消费真实 overview response。

涉及文件控制在 5 个核心文件：

- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-types.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-service.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-service-contract.test.ts`

控件与操作要求：

- 周期控件：`Segmented`，可访问名保持 `看板周期`；选项为 `近 7 天` -> `WEEK`、`近 30 天` -> `MONTH`。
- 刷新按钮：`Button` 文案保持 `刷新`，点击后必须同时 invalidate `["operations", "dashboard", "overview"]` 和 `["operations", "health", "trend"]`。
- 核心指标卡：
  - `内容总量` 使用 `contentCount`，副文本使用 `translatedContentCount`、`imageReadyContentCount`。
  - `搜索 / 问答` 使用 `searchCount`、`qaCount`，副文本使用 `avgSearchLatencyMs`。
  - `AI 调用成功率` 使用 `aiSucceededInvocationCount / aiInvocationCount`，副文本使用 `aiFailedInvocationCount`、`aiAvgLatencyMs`。
  - `异常组件 / 失败任务` 使用 `unhealthyComponentCount / failedTaskCount`，副文本使用 `runningTaskCount`。
- 趋势控件：
  - `内容增长趋势` 使用 `contentGrowthSeries`。
  - `搜索趋势` 使用 `searchTrendSeries`。
  - 本轮不新增 `问答趋势`、`标签增长趋势` 可视化，除非同一页面已有容器可低风险承载。
- 排行控件：
  - `热门内容` 使用 `topContents`，显示 `title/contentType/visitCount`。
  - `热门搜索` 使用 `topQueries`，显示 `queryText/count`。
  - `标签覆盖` 使用 `topTags`，显示 `tagName/contentRefCount`。
- 健康控件：
  - `健康巡检` 列表继续使用 `healthSummaries`。
  - 点击 `.operations-dashboard-health-item` 打开 `KuzhambuDrawer`。
  - 抽屉标题为 `${component} 健康明细`，展示 `healthStatus`、`probeSource`、`probeTarget`、`latencyMs`、`checkedAt`、`message`。
- 运维入口：
  - 保持 `任务台账`、`备份恢复`、`清理维护` 三个 Link，不改路由。

前端空态要求：

- 趋势无数据显示 `暂无趋势数据`。
- 健康摘要无数据显示 `暂无健康摘要`。
- 健康趋势无数据显示 `暂无健康趋势`。
- 热门内容无数据显示 `暂无内容排行`。
- 热门搜索无数据显示 `暂无查询排行`。
- 标签覆盖无数据显示 `暂无标签排行`。
- 不允许在前端构造示例排行或默认趋势点。

前端测试要求：

- `dashboard-service-contract.test.ts` 保持 `POST /operations/dashboard/overview` body 字段断言。
- `dashboard-page.test.tsx` 增加断言：
  - 切换 `看板周期` 到 `近 30 天` 后使用 `periodType: "MONTH"` 请求 overview。
  - 点击 `刷新` 后重新触发 overview 与 health trend 查询。
  - 后端返回空数组时显示上述空态文案。
  - 点击健康项后抽屉展示健康明细字段。

前端控件验收矩阵：

| 控件或区域 | 用户操作 | 请求或状态变化 | 绑定字段 | 验收点 |
| --- | --- | --- | --- | --- |
| `Segmented` 看板周期 | 点击 `近 7 天` | 调用 `getDashboardOverview({ periodType: "WEEK" })` | `periodType` | query key 包含 `WEEK` |
| `Segmented` 看板周期 | 点击 `近 30 天` | 调用 `getDashboardOverview({ periodType: "MONTH" })` | `periodType` | query key 包含 `MONTH` |
| `Button` 刷新 | 点击 `刷新` | invalidate overview 和 health trend 两组 query | 无新增字段 | 两个 service 都重新请求 |
| `内容总量` Statistic | 页面加载 | 无额外操作 | `contentCount`、`translatedContentCount`、`imageReadyContentCount` | 不使用本地默认样例 |
| `搜索 / 问答` Statistic | 页面加载 | 无额外操作 | `searchCount`、`qaCount`、`avgSearchLatencyMs` | 平均搜索延迟展示真实 ms 或真实空值 `-` |
| `AI 调用成功率` Statistic | 页面加载 | 无额外操作 | `aiSucceededInvocationCount`、`aiInvocationCount`、`aiFailedInvocationCount`、`aiAvgLatencyMs` | 成功数和总数来自后端 |
| `异常组件 / 失败任务` Statistic | 页面加载 | 无额外操作 | `unhealthyComponentCount`、`failedTaskCount`、`runningTaskCount` | 保持 Operations 自有数据来源 |
| `内容增长趋势` TrendPanel | 页面加载 | 无额外操作 | `contentGrowthSeries[].bucket/count` | 空数组显示 `暂无趋势数据` |
| `搜索趋势` TrendPanel | 页面加载 | 无额外操作 | `searchTrendSeries[].bucket/count` | 空数组显示 `暂无趋势数据` |
| `健康巡检` 列表 | 点击健康项 | 设置 `selectedHealth` 并打开 `KuzhambuDrawer` | `healthSummaries[]` | 抽屉展示对应项明细 |
| `健康趋势` 卡片 | 页面加载 | 调用 `getHealthTrend` | `upCount`、`degradedCount`、`downCount`、`avgLatencyMs` | 不与四域 summary 混用 |
| `热门内容` RankingList | 页面加载 | 无额外操作 | `topContents[]` | 空数组显示 `暂无内容排行` |
| `热门搜索` RankingList | 页面加载 | 无额外操作 | `topQueries[]` | 空数组显示 `暂无查询排行` |
| `标签覆盖` RankingList | 页面加载 | 无额外操作 | `topTags[]` | 空数组显示 `暂无标签排行` |
| 运维入口 Link | 点击入口 | 跳转既有路由 | 无新增字段 | `任务台账`、`备份恢复`、`清理维护` 路由不变 |

## 验证命令

后端按任务运行窄范围格式化和测试。

Operations：

```sh
cd kuzhambu-servers
mvn -pl biz/operations/kuzhambu-operations-application,biz/operations/kuzhambu-operations-interface -am spotless:apply
mvn -pl biz/operations/kuzhambu-operations-application,biz/operations/kuzhambu-operations-interface -am spotless:check
mvn -pl biz/operations/kuzhambu-operations-application,biz/operations/kuzhambu-operations-interface -am checkstyle:check
mvn -pl biz/operations/kuzhambu-operations-application,biz/operations/kuzhambu-operations-interface -am test
```

Discovery：

```sh
cd kuzhambu-servers
mvn -pl biz/discovery/kuzhambu-discovery-application,biz/discovery/kuzhambu-discovery-infra -am spotless:apply
mvn -pl biz/discovery/kuzhambu-discovery-application,biz/discovery/kuzhambu-discovery-infra -am spotless:check
mvn -pl biz/discovery/kuzhambu-discovery-application,biz/discovery/kuzhambu-discovery-infra -am checkstyle:check
mvn -pl biz/discovery/kuzhambu-discovery-application,biz/discovery/kuzhambu-discovery-infra -am test
```

前端：

```sh
cd kuzhambu-apps
npm --workspace kuzhambu-admin-web run format
npm --workspace kuzhambu-admin-web run test -- operations/dashboard
npm run format:check
npm run lint
npm run build
```

收口前：

```sh
cd kuzhambu-servers
mvn spotless:check
mvn checkstyle:check
mvn test

cd ../kuzhambu-apps
npm run format:check
npm run lint
npm run build
npm run test
```

## 审核清单

- [ ] Discovery search log 新增 `searchLatencyMs` / `search_latency_ms` 并成功、失败路径都写入。
- [ ] Discovery summary 的 `avgSearchLatencyMs` 来自真实 `searchLatencyMs`。
- [ ] Dashboard 与 Report 复用同一 Operations application summary gateway。
- [ ] `OperationsDashboardApplicationServiceImpl` 已删除四域统计固定占位。
- [ ] `OperationsDashboardOverviewResult`、`OperationsDashboardOverviewResponse`、`OperationsDashboardOverviewRecord` 字段一致。
- [ ] 前端周期分段、刷新按钮、核心指标卡、趋势、排行、健康抽屉均有测试覆盖。
- [ ] 前端无 mock、无硬编码样例排行、无用空数组掩盖后端缺字段。
- [ ] 周视图按 `DAY`，月视图按 `WEEK`，自定义 `<= 31` 天按 `DAY`，自定义 `> 31` 天按 `WEEK`。
- [ ] Operations 不依赖其他业务域 infra、mapper、dataobject、repository.impl 或 controller response。
- [ ] 任务完成前删除本 RUNBOOK。
