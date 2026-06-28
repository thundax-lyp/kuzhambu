# RUNBOOK: Discovery Facade Isolation

## 1. 目标

为 `discovery` 域新增 `kuzhambu-discovery-facade` 模块，建立统一 `DiscoveryFacade` 外域边界。

本轮只覆盖当前真实跨域调用：

- `operations-application -> discovery-application.report`

完成后目标：

- 外域不再直接依赖 `kuzhambu-discovery-application`
- `operations` 通过 `DiscoveryFacade.summary(...)` 读取 discovery 统计摘要
- `operations -> discovery` 的 POM allowlist 和 cross-application allowlist 收缩为零

## 2. 范围

### 2.1 纳入本轮

- `DiscoveryReportApplicationService.summary(...)` 的 facade 化
- `DiscoveryReportSummaryResult` 到 facade 协议的迁移
- `operations` 消费方迁移
- 相关测试和 architecture allowlist 收缩

### 2.2 不纳入本轮

- `QaApplicationService`
- `SearchApplicationService`
- `QueryUnderstandingApplicationService`
- `SearchIndexApplicationService`
- `SearchIndexSyncApplicationService`
- `SearchIndexCleanupApplicationService`

这些能力当前没有外域业务模块直接消费，不在本轮扩边界。

## 3. 当前调用点

### 3.1 外域生产调用点

1. `operations` 报表摘要读取 discovery 统计
   - 文件：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGateway.java`
   - 当前依赖：`DiscoveryReportApplicationService`
   - 当前返回：`DiscoveryReportSummaryResult`

### 3.2 外域测试调用点

1. `operations` 报表网关测试
   - 文件：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGatewayTest.java`

### 3.3 当前 POM 直依赖

1. `operations-application -> discovery-application`
   - 文件：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/pom.xml`

### 3.4 当前 allowlist 债项

1. POM allowlist
   - 文件：`kuzhambu-servers/common/kuzhambu-common-test/src/main/java/com/thundax/kuzhambu/common/test/architecture/RepositoryArchitectureRuleSupport.java`
   - 当前债项：`operations -> kuzhambu-discovery-application`

2. cross-application allowlist
   - 文件：`kuzhambu-servers/common/kuzhambu-common-test/src/main/java/com/thundax/kuzhambu/common/test/architecture/CrossApplicationIsolationArchitectureRuleSupport.java`
   - 当前债项：`operations -> discovery`

## 4. 目标边界

### 4.1 Facade 模块

新增模块：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-facade`

新增统一入口：

- `com.thundax.kuzhambu.discovery.facade.DiscoveryFacade`

### 4.2 Facade 方法

本轮只定义一个外域动作：

```java
DiscoverySummaryFacadeResponse summary(DiscoverySummaryFacadeRequest request);
```

## 5. 数据结构变更

### 5.1 Request

#### `DiscoverySummaryFacadeRequest`

目标包位：

- `com.thundax.kuzhambu.discovery.facade.request`

字段：

- `Date periodStart`
- `Date periodEnd`
- `String bucketType`

来源：

- `DiscoveryReportApplicationService.summary(Date periodStart, Date periodEnd, String bucketType)` 方法参数

### 5.2 Response

#### `DiscoverySummaryFacadeResponse`

目标包位：

- `com.thundax.kuzhambu.discovery.facade.response`

字段：

- `Date periodStart`
- `Date periodEnd`
- `Long searchCount`
- `Long qaCount`
- `Long avgSearchLatencyMs`
- `List<DiscoveryTopQueryFacadeDto> topQueries`
- `List<DiscoverySearchTrendPointFacadeDto> searchTrendSeries`
- `List<DiscoveryQaTrendPointFacadeDto> qaTrendSeries`

来源：

- `com.thundax.kuzhambu.discovery.application.report.result.DiscoveryReportSummaryResult`

### 5.3 DTO

#### `DiscoveryTopQueryFacadeDto`

目标包位：

- `com.thundax.kuzhambu.discovery.facade.dto`

字段：

- `String queryText`
- `Long count`

来源：

- `DiscoveryReportSummaryResult.TopQueryResult`

#### `DiscoverySearchTrendPointFacadeDto`

目标包位：

- `com.thundax.kuzhambu.discovery.facade.dto`

字段：

- `String bucket`
- `Long searchCount`

来源：

- `DiscoveryReportSummaryResult.SearchTrendPointResult`

#### `DiscoveryQaTrendPointFacadeDto`

目标包位：

- `com.thundax.kuzhambu.discovery.facade.dto`

字段：

- `String bucket`
- `Long qaCount`

来源：

- `DiscoveryReportSummaryResult.QaTrendPointResult`

## 6. Provider 设计

### 6.1 外域可见层

- `DiscoveryFacade`
- `DiscoverySummaryFacadeRequest`
- `DiscoverySummaryFacadeResponse`
- `DiscoveryTopQueryFacadeDto`
- `DiscoverySearchTrendPointFacadeDto`
- `DiscoveryQaTrendPointFacadeDto`

都放在 `kuzhambu-discovery-facade`。

### 6.2 Provider 实现层

以下实现放在 `kuzhambu-discovery-application`：

- `com.thundax.kuzhambu.discovery.application.facade.impl.DiscoveryFacadeImpl`
- `com.thundax.kuzhambu.discovery.application.facade.assembler.DiscoveryFacadeAssembler`

实现方式：

- `DiscoveryFacadeImpl.summary(...)` 调用 `DiscoveryReportApplicationService.summary(...)`
- `DiscoveryFacadeAssembler` 负责 `DiscoveryReportSummaryResult -> DiscoverySummaryFacadeResponse`

## 7. 执行任务

所有任务必须保持 `2-5` 个文件，不允许大块提交。

### T1. 新增 discovery-facade 模块骨架

- 数据结构变更：无
- 接口定义：
  - 新增空模块，准备承载 `DiscoveryFacade`
- 相关文件：
  - `kuzhambu-servers/biz/discovery/pom.xml`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-facade/pom.xml`

### T2. 定义 summary facade 入口与 request

- 数据结构变更：
  - 新增 `DiscoverySummaryFacadeRequest`
  - 字段：`periodStart`、`periodEnd`、`bucketType`
- 接口定义：
  - 新增 `DiscoveryFacade.summary(...)`
- 相关文件：
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-facade/src/main/java/com/thundax/kuzhambu/discovery/facade/DiscoveryFacade.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-facade/src/main/java/com/thundax/kuzhambu/discovery/facade/request/DiscoverySummaryFacadeRequest.java`

### T3. 定义 summary facade response 与 dto

- 数据结构变更：
  - 新增 `DiscoverySummaryFacadeResponse`
  - 新增 `DiscoveryTopQueryFacadeDto`
  - 新增 `DiscoverySearchTrendPointFacadeDto`
  - 新增 `DiscoveryQaTrendPointFacadeDto`
  - 字段必须与第 5 节一致
- 接口定义：无新增方法
- 相关文件：
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-facade/src/main/java/com/thundax/kuzhambu/discovery/facade/response/DiscoverySummaryFacadeResponse.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-facade/src/main/java/com/thundax/kuzhambu/discovery/facade/dto/DiscoveryTopQueryFacadeDto.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-facade/src/main/java/com/thundax/kuzhambu/discovery/facade/dto/DiscoverySearchTrendPointFacadeDto.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-facade/src/main/java/com/thundax/kuzhambu/discovery/facade/dto/DiscoveryQaTrendPointFacadeDto.java`

### T4. 新增 provider facade 入口骨架

- 数据结构变更：无
- 接口定义：
  - 新增 `DiscoveryFacadeImpl`
  - 新增 `DiscoveryFacadeAssembler`
- 相关文件：
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/pom.xml`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/facade/impl/DiscoveryFacadeImpl.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/facade/assembler/DiscoveryFacadeAssembler.java`

### T5. 桥接 summary provider 能力

- 数据结构变更：
  - 将 `DiscoveryReportSummaryResult` 映射到 facade response/dto
- 接口定义：
  - `DiscoveryFacadeImpl.summary(...)` 正式可用
- 相关文件：
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/facade/impl/DiscoveryFacadeImpl.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/facade/assembler/DiscoveryFacadeAssembler.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/report/service/DiscoveryReportApplicationService.java`

### T6. operations 改用 discovery facade

- 数据结构变更：
  - `operations` 不再直接消费 `DiscoveryReportSummaryResult`
  - 改为消费 `DiscoverySummaryFacadeResponse`
- 接口定义：
  - `DefaultOperationsReportMetricsGateway` 改调 `DiscoveryFacade.summary(...)`
- 相关文件：
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-application/pom.xml`
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGateway.java`
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGatewayTest.java`

### T7. 补齐 facade 协议与 provider 测试

- 数据结构变更：验证协议对象满足 facade 架构约束
- 接口定义：验证 `DiscoveryFacadeImpl.summary(...)` provider 行为
- 相关文件：
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-facade/src/test/java/com/thundax/kuzhambu/discovery/facade/DiscoveryFacadeArchitectureTest.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/facade/impl/DiscoveryFacadeImplTest.java`

### T8. 收缩 discovery 跨域 allowlist

- 数据结构变更：无
- 接口定义：无
- 相关文件：
  - `kuzhambu-servers/common/kuzhambu-common-test/src/main/java/com/thundax/kuzhambu/common/test/architecture/RepositoryArchitectureRuleSupport.java`
  - `kuzhambu-servers/common/kuzhambu-common-test/src/main/java/com/thundax/kuzhambu/common/test/architecture/CrossApplicationIsolationArchitectureRuleSupport.java`

## 8. 验收标准

满足以下条件才算完成：

1. `operations-application` 不再依赖 `kuzhambu-discovery-application`
2. `DefaultOperationsReportMetricsGateway` 只通过 `DiscoveryFacade` 读取 discovery summary
3. `RepositoryArchitectureRuleSupport` 中删除 `operations -> kuzhambu-discovery-application`
4. `CrossApplicationIsolationArchitectureRuleSupport` 中删除 `operations -> discovery`
5. `DiscoveryFacadeArchitectureTest` 和 `DiscoveryFacadeImplTest` 通过
6. `DefaultOperationsReportMetricsGatewayTest` 通过

## 9. 收口

当第 8 节全部满足后：

- 删除本 RUNBOOK
- 在 `TODO.md` 中删除已完成项
- 单独提交 RUNBOOK 删除动作
