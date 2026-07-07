# RUNBOOK Operations 健康细分页与外部探针闭环

## 目标

把 Operations 剩余项“健康细分页、外部探针配置、探针结果展示”推进到可运行、可验证、可收口的已完成状态。

完成后：

- 管理员可以进入独立页面 `/operations/health` 查看健康检查分页。
- 健康分页支持按组件、健康状态、探针来源、探针目标和检查时间范围筛选。
- 外部 HTTP 探针通过配置启用，默认关闭。
- 外部探针结果写入现有 `operations_health_check`。
- 探针结果在页面表格和详情抽屉中展示，并能关联健康告警。
- Dashboard 只保留健康摘要、趋势和跳转入口，不承载完整分页。

## 已确认约束

- 本次只做 HTTP 外部探针，不做 DB、Redis、Elasticsearch 等专用探针。
- Operations 只拥有 `operations_*` 自有台账，不复制外部系统主事实。
- 外部探针结果写入现有 `operations_health_check`，不新增探针结果表。
- 健康状态只允许 `UP`、`DEGRADED`、`DOWN`。
- 外部探针默认 `enabled=false`，禁用时不发起 HTTP 调用，不产生外部探针健康记录。
- 探针失败、超时、返回非预期 HTTP 状态必须写入 `DOWN`。
- `DOWN` 和 `DEGRADED` 结果继续联动现有 `OperationsHealthAlertStrategy`。
- 健康细分页沿用 `operations:health:view`，不新增权限码。
- 本次完成后，`docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md` 可将“健康检查与运行状态”改为 `已完成`；自动化恢复动作编排不纳入本次目标。
- 任务关闭前删除本 RUNBOOK。

## 数据结构变更

### 后端健康分页 Query

文件：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/query/OperationsHealthPageQuery.java`

保留现有字段：

| 字段 | Java 类型 | 说明 |
| --- | --- | --- |
| `component` | `String` | 组件名，精确匹配 |
| `healthStatus` | `String` | `UP`、`DEGRADED`、`DOWN` |

新增字段：

| 字段 | Java 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `probeSource` | `String` | 否 | 探针来源，精确匹配；本次支持 `LOCAL`、`HTTP` |
| `probeTarget` | `String` | 否 | 探针目标，模糊匹配 |
| `checkedAtStart` | `Date` | 否 | 检查时间起点，包含边界 |
| `checkedAtEnd` | `Date` | 否 | 检查时间终点，包含边界 |

### 后端健康分页 Request

文件：`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/request/OperationsHealthPageRequest.java`

保留现有分页字段和筛选字段：

| JSON 字段 | Java 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `component` | `String` | 否 | 组件名，精确匹配 |
| `healthStatus` | `String` | 否 | `UP`、`DEGRADED`、`DOWN` |
| `pageNo` | `Integer` | 是 | 页码 |
| `pageSize` | `Integer` | 是 | 每页条数 |

新增字段：

| JSON 字段 | Java 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `probeSource` | `String` | 否 | 探针来源，精确匹配 |
| `probeTarget` | `String` | 否 | 探针目标，模糊匹配 |
| `checkedAtStart` | `Date` | 否 | 检查时间起点，包含边界 |
| `checkedAtEnd` | `Date` | 否 | 检查时间终点，包含边界 |

### 后端健康分页 Response

文件：`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/response/OperationsHealthPageResponse.java`

响应字段不新增，只要求前端完整消费已有字段：

| JSON 字段 | Java 类型 | 说明 |
| --- | --- | --- |
| `checkId` | `Long` | 健康记录 ID |
| `component` | `String` | 组件名 |
| `healthStatus` | `String` | `UP`、`DEGRADED`、`DOWN` |
| `latencyMs` | `Integer` | 探测耗时，单位毫秒 |
| `message` | `String` | 探测说明或失败原因 |
| `probeSource` | `String` | 采集来源 |
| `probeTarget` | `String` | 探针目标 |
| `detailsJson` | `String` | 诊断 JSON 字符串 |
| `checkedAt` | `Date` | 检查时间 |

### Repository 方法签名

文件：`kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/repository/HealthCheckRepository.java`

将现有方法：

```java
PageResult<HealthCheckRecord> page(String component, String healthStatus, int pageNo, int pageSize);
```

调整为：

```java
PageResult<HealthCheckRecord> page(
        String component,
        String healthStatus,
        String probeSource,
        String probeTarget,
        Date checkedAtStart,
        Date checkedAtEnd,
        int pageNo,
        int pageSize);
```

### 外部探针配置 Properties

新增文件：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/OperationsExternalHealthProbeProperties.java`

配置前缀：`kuzhambu.operations.health.probes`

顶层字段：

| 字段 | Java 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `enabled` | `boolean` | `false` | 是否启用外部 HTTP 探针 |
| `timeoutMs` | `int` | `3000` | 单次 HTTP 探测超时时间 |
| `targets` | `List<Target>` | 空列表 | HTTP 探针目标集合 |

`Target` 字段：

| 字段 | Java 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `enabled` | `boolean` | `true` | 单目标开关 |
| `component` | `String` | 无 | 写入 `HealthCheckRecord.component` |
| `url` | `String` | 无 | 写入 `HealthCheckRecord.probeTarget` |
| `expectedStatus` | `int` | `200` | 期望 HTTP 状态码 |
| `degradedLatencyMs` | `int` | `1000` | 超过该耗时且状态码符合时写入 `DEGRADED` |

### 外部探针 detailsJson

新增文件：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/HttpOperationsHealthProbe.java`

`detailsJson` 必须是 JSON object 字符串，字段固定为：

| JSON 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `url` | `string` | 是 | 探针目标 URL |
| `expectedStatus` | `number` | 是 | 期望 HTTP 状态码 |
| `actualStatus` | `number` | 否 | 实际 HTTP 状态码；超时或异常时为空 |
| `timeoutMs` | `number` | 是 | 配置超时时间 |
| `degradedLatencyMs` | `number` | 是 | 降级耗时阈值 |
| `latencyMs` | `number` | 否 | 实际耗时 |
| `errorType` | `string` | 否 | `TIMEOUT`、`IO_ERROR`、`INVALID_STATUS` 等 |
| `errorMessage` | `string` | 否 | 异常说明 |

### 健康告警分页 Query

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/query/OperationsHealthAlertPageQuery.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/request/OperationsHealthAlertPageRequest.java`

保留现有字段：

| 字段 | Java 类型 | 说明 |
| --- | --- | --- |
| `component` | `String` | 组件筛选 |
| `alertLevel` | `String` | `WARNING`、`CRITICAL` |
| `alertStatus` | `String` | `ACTIVE`、`ACKED`、`RECOVERED` |
| `sourceRefType` | `String` | 来源类型 |
| `sourceRefId` | `Long` | 来源业务 ID |

新增字段：

| 字段 | Java 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `latestCheckId` | `Long` | 否 | 按最新健康检查记录 ID 精确筛选告警，用于健康记录行进入关联告警 |

### 健康告警 Repository 方法签名

文件：`kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/repository/HealthAlertRepository.java`

将现有方法：

```java
PageResult<HealthAlertRecord> page(
        String component,
        String alertLevel,
        String alertStatus,
        String sourceRefType,
        Long sourceRefId,
        int pageNo,
        int pageSize);
```

调整为：

```java
PageResult<HealthAlertRecord> page(
        String component,
        String alertLevel,
        String alertStatus,
        String sourceRefType,
        Long sourceRefId,
        Long latestCheckId,
        int pageNo,
        int pageSize);
```

## 外部探针结果规则

文件：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/HttpOperationsHealthProbe.java`

| 条件 | `healthStatus` | `latencyMs` | `message` | `probeSource` | `probeTarget` |
| --- | --- | --- | --- | --- | --- |
| HTTP 状态码等于 `expectedStatus` 且耗时小于等于 `degradedLatencyMs` | `UP` | 实际耗时 | `http status {actualStatus}` | `HTTP` | 配置 URL |
| HTTP 状态码等于 `expectedStatus` 且耗时大于 `degradedLatencyMs` | `DEGRADED` | 实际耗时 | `http status {actualStatus}, latency degraded` | `HTTP` | 配置 URL |
| HTTP 状态码不等于 `expectedStatus` | `DOWN` | 实际耗时 | `http status {actualStatus}, expected {expectedStatus}` | `HTTP` | 配置 URL |
| 请求超时 | `DOWN` | 空或实际耗时 | `http probe timeout` | `HTTP` | 配置 URL |
| 请求异常 | `DOWN` | 空或实际耗时 | `http probe failed` | `HTTP` | 配置 URL |

## 后端任务拆分

### 任务 1：健康分页筛选字段贯通

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/query/OperationsHealthPageQuery.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/request/OperationsHealthPageRequest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/assembler/OperationsHealthInterfaceAssembler.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/OperationsHealthAdminControllerTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/OperationsHealthContractTest.java`

动作：

- Request 和 Query 增加 `probeSource`、`probeTarget`、`checkedAtStart`、`checkedAtEnd`。
- assembler 把四个新增字段从 request 传入 query。
- controller 测试锁定新增字段能传给 application service。
- contract 测试锁定 `/api/operations/health/page` 响应仍包含 `probeSource`、`probeTarget`、`detailsJson`。

验收：

- `OperationsHealthAdminControllerTest` 断言 application query 中四个新增字段不丢失。
- `OperationsHealthContractTest` 保持分页响应字段稳定。

### 任务 2：健康分页持久化筛选

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/repository/HealthCheckRepository.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/service/impl/HealthCheckApplicationServiceImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/repository/impl/HealthCheckRepositoryImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/persistence/mapper/HealthCheckMapper.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/test/java/com/thundax/kuzhambu/operations/infra/health/repository/impl/HealthCheckRepositoryImplTest.java`

动作：

- repository `page` 方法签名增加四个筛选参数。
- application service 调用 repository 时传入新增筛选参数。
- mapper 查询增加：
  - `component` 精确匹配。
  - `health_status` 精确匹配。
  - `probe_source` 精确匹配。
  - `probe_target` 模糊匹配。
  - `checked_at >= checkedAtStart`。
  - `checked_at <= checkedAtEnd`。
- 排序固定为 `checked_at desc, check_id desc`。

验收：

- repository 测试覆盖 `probeSource`、`probeTarget`、时间范围和排序。
- 现有 summary、trend 查询不受影响。

### 任务 3：外部 HTTP 探针实现

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/OperationsExternalHealthProbeProperties.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/OperationsExternalHealthProbeConfiguration.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/HttpOperationsHealthProbe.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/health/support/HttpOperationsHealthProbeTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/health/support/OperationsExternalHealthProbeConfigurationTest.java`

动作：

- 新增 properties 并绑定 `kuzhambu.operations.health.probes`。
- configuration 在 `enabled=true` 时把有效 target 装配为 `OperationsHealthProbe`。
- target 无效时跳过该 target：`component` 为空、`url` 为空、`url` 非 HTTP/HTTPS、`enabled=false`。
- `HttpOperationsHealthProbe` 只返回 `OperationsHealthProbeResult`，不写库。
- HTTP 客户端必须应用 `timeoutMs`。
- `detailsJson` 严格按本文字段输出。

验收：

- disabled 顶层配置不注册外部探针。
- enabled 顶层配置只注册有效且启用的 targets。
- 200 低耗时返回 `UP`。
- 200 高耗时返回 `DEGRADED`。
- 非期望状态、超时和异常返回 `DOWN`。

### 任务 4：外部探针配置样例

文件：

- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/resources/application.yml`
- `.env.example`
- `deploy/.env.example`
- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/test/java/com/thundax/kuzhambu/starter/admin/AdminStarterArchitectureTest.java`

动作：

- starter 默认配置增加 `kuzhambu.operations.health.probes.enabled=false`。
- starter 默认配置增加 `timeoutMs=3000` 和空 target 示例结构，不写生产地址。
- `.env.example` 与 `deploy/.env.example` 增加相同环境变量样例。
- architecture 测试断言 starter 暴露外部探针配置前缀。

环境变量样例：

```sh
KUZHAMBU_OPERATIONS_HEALTH_PROBES_ENABLED=false
KUZHAMBU_OPERATIONS_HEALTH_PROBES_TIMEOUT_MS=3000
KUZHAMBU_OPERATIONS_HEALTH_PROBES_TARGETS_0_COMPONENT=admin-starter
KUZHAMBU_OPERATIONS_HEALTH_PROBES_TARGETS_0_URL=http://127.0.0.1:8080/internal/health
KUZHAMBU_OPERATIONS_HEALTH_PROBES_TARGETS_0_EXPECTED_STATUS=200
KUZHAMBU_OPERATIONS_HEALTH_PROBES_TARGETS_0_DEGRADED_LATENCY_MS=1000
KUZHAMBU_OPERATIONS_HEALTH_PROBES_TARGETS_0_ENABLED=true
```

验收：

- 默认部署不会自动发起外部 HTTP 探测。
- env example 可以直接指导本地启用一个探针。

### 任务 5：健康告警 latestCheckId 接口字段贯通

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/query/OperationsHealthAlertPageQuery.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/request/OperationsHealthAlertPageRequest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/assembler/OperationsHealthAlertInterfaceAssembler.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/OperationsHealthAlertAdminControllerTest.java`

动作：

- 告警 page request 和 query 新增 `latestCheckId`。
- interface assembler 传递 `latestCheckId`。
- 保留现有 `component`、`alertLevel`、`alertStatus`、`sourceRefType`、`sourceRefId` 筛选语义。

验收：

- controller 测试断言 request 的 `latestCheckId` 能传入 application query。
- 现有告警抽屉按组件、状态、来源筛选不受影响。

### 任务 6：健康告警 latestCheckId 持久化筛选

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/repository/HealthAlertRepository.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/service/impl/HealthAlertApplicationServiceImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/repository/impl/HealthAlertRepositoryImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/persistence/mapper/HealthAlertMapper.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/test/java/com/thundax/kuzhambu/operations/infra/health/repository/impl/HealthAlertRepositoryImplTest.java`

动作：

- repository page 方法新增 `latestCheckId` 参数。
- application service 调用 repository 时传入 `latestCheckId`。
- mapper 查询增加 `latest_check_id = latestCheckId` 精确匹配。
- 保留现有排序和分页语义。

验收：

- repository 测试覆盖按 `latestCheckId` 查询。
- 健康记录表格行可以用 `checkId` 精确查询关联告警。

## 前端数据结构

### 健康分页类型

新增文件：`kuzhambu-apps/admin-web/src/pages/operations/health/health-types.ts`

类型：

```ts
export type OperationsHealthStatus = "UP" | "DEGRADED" | "DOWN";

export interface OperationsHealthRecord {
    checkId: number;
    component?: string | null;
    healthStatus?: OperationsHealthStatus | null;
    latencyMs?: number | null;
    message?: string | null;
    probeSource?: string | null;
    probeTarget?: string | null;
    detailsJson?: string | null;
    checkedAt?: string | null;
}

export interface OperationsHealthPageQuery {
    component?: string | null;
    healthStatus?: OperationsHealthStatus | null;
    probeSource?: string | null;
    probeTarget?: string | null;
    checkedAtStart?: string | null;
    checkedAtEnd?: string | null;
    pageNo?: number | null;
    pageSize?: number | null;
}

export interface OperationsHealthAlertPageQuery {
    component?: string | null;
    alertLevel?: "WARNING" | "CRITICAL" | null;
    alertStatus?: "ACTIVE" | "ACKED" | "RECOVERED" | null;
    sourceRefType?: string | null;
    sourceRefId?: number | null;
    latestCheckId?: number | null;
    pageNo?: number | null;
    pageSize?: number | null;
}

export interface OperationsPageRecord<TRecord> {
    pageNo?: number | null;
    pageSize?: number | null;
    count?: number | null;
    records?: TRecord[] | null;
}
```

### 健康分页服务

新增文件：`kuzhambu-apps/admin-web/src/pages/operations/health/health-service.ts`

服务方法：

```ts
export const getOperationsHealthPage = (query: OperationsHealthPageQuery = {}) => {
    return postJson<OperationsPageRecord<OperationsHealthRecord>, OperationsHealthPageQuery>(
        "/operations/health/page",
        { body: query }
    );
};
```

### 告警分页服务字段

现有文件：`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-service.ts`

在现有 `OperationsHealthAlertPageQuery` 中新增字段：

| 字段 | TypeScript 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `latestCheckId` | `number \| null` | 否 | 当前健康记录 ID，用于从健康分页行查询关联告警 |

保留现有字段：

| 字段 | TypeScript 类型 | 说明 |
| --- | --- | --- |
| `component` | `string \| null` | 组件筛选 |
| `alertLevel` | `OperationsHealthAlertLevel \| null` | 告警级别 |
| `alertStatus` | `OperationsHealthAlertStatus \| null` | 告警状态 |
| `sourceRefType` | `string \| null` | 来源类型 |
| `sourceRefId` | `number \| null` | 来源业务 ID |
| `pageNo` | `number \| null` | 页码 |
| `pageSize` | `number \| null` | 每页条数 |

## 前端请求映射

健康分页页内状态到 `/operations/health/page` 请求字段的映射固定为：

| 控件 | 页内状态字段 | 请求字段 | 转换规则 |
| --- | --- | --- | --- |
| 组件 `Input` | `componentKeyword` | `component` | `trim()` 后为空则传 `null` |
| 健康状态 `Select` | `healthStatus` | `healthStatus` | `全部` 传 `null` |
| 探针来源 `Select` | `probeSource` | `probeSource` | `全部` 传 `null` |
| 探针目标 `Input` | `probeTargetKeyword` | `probeTarget` | `trim()` 后为空则传 `null` |
| 检查时间 `RangePicker` 起点 | `checkedAtRange[0]` | `checkedAtStart` | 转为后端当前通用日期字符串格式；未选择传 `null` |
| 检查时间 `RangePicker` 终点 | `checkedAtRange[1]` | `checkedAtEnd` | 转为后端当前通用日期字符串格式；未选择传 `null` |
| 表格分页页码 | `pagination.current` | `pageNo` | 默认 `1` |
| 表格分页条数 | `pagination.pageSize` | `pageSize` | 默认使用当前项目列表页惯例值 |

交互规则：

- 点击 `查询`：使用当前控件值请求，`pageNo` 固定重置为 `1`。
- 点击 `重置`：清空所有筛选控件，`pageNo` 固定为 `1`，立即请求。
- 点击 `刷新`：保留所有筛选控件和当前分页，立即请求。
- 切换页码：保留筛选控件，只更新 `pageNo`。
- 切换每页条数：保留筛选控件，更新 `pageSize`，`pageNo` 固定重置为 `1`。
- 按组件 Input 回车：等同点击 `查询`。
- 按探针目标 Input 回车：等同点击 `查询`。

## 前端任务拆分

### 任务 1：健康分页服务与契约测试

文件：

- `kuzhambu-apps/admin-web/src/pages/operations/health/health-types.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/health/health-service.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/health/health-service-contract.test.ts`

动作：

- 新增健康分页类型。
- 新增 `getOperationsHealthPage`。
- contract 测试锁定请求地址 `/operations/health/page`。
- contract 测试锁定请求体字段：`component`、`healthStatus`、`probeSource`、`probeTarget`、`checkedAtStart`、`checkedAtEnd`、`pageNo`、`pageSize`。

验收：

- service 不依赖 Dashboard service。
- 健康页类型可以独立演进，不把 Dashboard overview 类型搬进健康页。
- contract 测试必须覆盖空筛选字段会按 `null` 或省略值进入请求体，不能发送未 trim 的空字符串。

### 任务 2：健康分页页面控件

文件：

- `kuzhambu-apps/admin-web/src/pages/operations/health/health-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/health/health-page.css`
- `kuzhambu-apps/admin-web/src/pages/operations/health/health-page.test.tsx`

页面布局：

- 顶部工具条：
  - `Input`：组件，placeholder 为 `组件`，绑定 `componentKeyword`，允许清空，回车触发查询。
  - `Select`：健康状态，绑定 `healthStatus`，选项为 `全部`、`UP`、`DEGRADED`、`DOWN`。
  - `Select`：探针来源，绑定 `probeSource`，选项为 `全部`、`LOCAL`、`HTTP`。
  - `Input`：探针目标，placeholder 为 `探针目标`，绑定 `probeTargetKeyword`，允许清空，支持输入 URL 片段，回车触发查询。
  - `RangePicker`：检查时间范围，绑定 `checkedAtRange`，允许清空开始和结束时间。
  - `Button`：查询，点击后 `pageNo` 重置为 `1` 并请求列表。
  - `Button`：重置，清空筛选条件并请求第一页。
  - `Button`：刷新，保留当前筛选和页码重新请求。
- 表格：
  - `component` 列：显示组件名。
  - `healthStatus` 列：使用 `Tag` 展示，`UP` 为成功色、`DEGRADED` 为警告色、`DOWN` 为错误色。
  - `probeSource` 列：显示 `LOCAL` / `HTTP`。
  - `probeTarget` 列：长 URL 使用省略和 tooltip。
  - `latencyMs` 列：显示 `{latencyMs} ms`，空值显示 `-`。
  - `message` 列：长文本省略和 tooltip。
  - `checkedAt` 列：显示检查时间。
  - 操作列：`详情` 按钮、`查看告警` 按钮。
- 分页：
  - 使用后端 `count`、`pageNo`、`pageSize`。
  - 切换页码或 pageSize 时保留筛选条件。
- 页面反馈：
  - 首次进入页面立即请求第一页。
  - 请求中表格显示 loading。
  - 请求失败显示当前项目已有错误提示组件或 `message.error`。
  - `records` 为空时显示表格空状态。

用户操作：

- 用户输入组件并点击查询，只按组件筛选。
- 用户选择 `HTTP` 来源并点击查询，只显示外部 HTTP 探针记录。
- 用户输入探针目标片段并点击查询，按 URL 片段模糊筛选。
- 用户选择时间范围并点击查询，按 `checkedAtStart` 和 `checkedAtEnd` 查询。
- 用户点击重置，所有控件回到默认值并加载第一页。
- 用户点击刷新，控件值不变并重新加载当前页。
- 用户点击表格行的详情，打开详情抽屉。
- 用户点击表格行的查看告警，打开告警抽屉并按 `latestCheckId=checkId` 查询告警。

验收：

- 页面测试覆盖查询、重置、刷新、分页切换。
- 页面测试覆盖 `HTTP` 来源筛选和探针目标筛选。
- 页面测试覆盖表格状态 tag、空值展示和长文本 tooltip。
- 页面测试覆盖首次进入自动加载第一页和请求失败提示。

### 任务 3：健康详情与告警联动

文件：

- `kuzhambu-apps/admin-web/src/pages/operations/health/health-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/health/health-page.css`
- `kuzhambu-apps/admin-web/src/pages/operations/health/health-page.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-service.ts`

详情抽屉控件：

- `Descriptions`：展示 `checkId`、组件、健康状态、耗时、探针来源、探针目标、检查时间。
- `Typography.Paragraph`：展示 `message`。
- 诊断 JSON 区：
  - 如果 `detailsJson` 可解析为 JSON，使用格式化后的多行文本展示。
  - 如果 `detailsJson` 不可解析，展示原始字符串。
  - 如果为空，展示空状态。

告警联动：

- 复用 Dashboard 已有 `/operations/health/alerts/page` service。
- 点击 `查看告警` 时，请求参数固定包含 `latestCheckId=checkId`、`pageNo=1`、`pageSize=10`。
- 告警分页 service 的 query 类型必须包含 `latestCheckId?: number | null`。
- 告警抽屉展示告警级别、状态、消息、建议、恢复动作、最后触发时间。
- 当前记录没有关联告警时，告警抽屉展示空状态，不关闭抽屉。
- 告警抽屉请求失败时，保留抽屉并展示错误提示。

验收：

- 可解析 JSON 被格式化展示。
- 非 JSON 原样展示，不报错。
- 空 details 展示空状态。
- 点击查看告警会按当前 `checkId` 查询并打开告警抽屉。

### 任务 4：路由与 Dashboard 入口

文件：

- `kuzhambu-apps/admin-web/src/router/index.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.test.tsx`

动作：

- 路由新增 `/operations/health`，组件为 `OperationsHealthPage`。
- Dashboard 健康摘要区增加 `查看全部` 按钮。
- 点击 `查看全部` 跳转 `/operations/health`。
- Dashboard 健康抽屉只展示单条摘要详情，不增加分页筛选控件。

验收：

- router 能渲染健康页。
- Dashboard 测试覆盖按钮可见和跳转。

## 菜单与权限种子任务

文件：

- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/resources/db/data-source/system.json`
- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/resources/db/data/system.sql`
- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/test/java/com/thundax/kuzhambu/starter/admin/AdminStarterArchitectureTest.java`

动作：

- 在 Operations 菜单下新增“健康检查”页面菜单。
- 路由固定为 `/operations/health`。
- 权限码沿用 `operations:health:view`。
- icon key 使用现有 Operations 菜单体系内已支持的图标 key。
- seed JSON 和 SQL 必须保持同一菜单名称、路由、权限码。

验收：

- starter architecture 测试断言 JSON 和 SQL 都包含 `/operations/health`。
- 无 `operations:health:view` 权限的用户不显示健康检查菜单。

## 文档收口任务

文件：

- `docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md`
- `docs/30-designs/RUNBOOK-OPERATIONS-HEALTH-PROBE-LOOP.md`

动作：

- 全部实现和验证通过后，将“健康检查与运行状态”状态改为 `已完成`。
- 已完成部分明确写入：
  - 健康细分页已完成。
  - 外部 HTTP 探针配置已完成。
  - 探针结果展示已完成。
  - 健康记录筛选覆盖组件、状态、来源、目标和检查时间。
  - 外部探针失败继续联动健康告警。
- 未完成部分删除“更多外部探针、健康细分页”。
- 自动化恢复动作编排如仍需保留，移动到后续 backlog，不阻塞本次“健康检查与运行状态”完成态。
- 任务关闭前删除本 RUNBOOK。

验收：

- 覆盖矩阵不再把本次三项目标列为未完成。
- RUNBOOK 无残留引用。

## 验证命令

### 后端定向验证

```sh
cd kuzhambu-servers
mvn -pl biz/operations/kuzhambu-operations-application -Dtest=HttpOperationsHealthProbeTest,OperationsExternalHealthProbeConfigurationTest,OperationsHealthCollectorTest test
mvn -pl biz/operations/kuzhambu-operations-infra -Dtest=HealthCheckRepositoryImplTest,HealthAlertRepositoryImplTest test
mvn -pl biz/operations/kuzhambu-operations-interface -Dtest=OperationsHealthAdminControllerTest,OperationsHealthAlertAdminControllerTest,OperationsHealthContractTest test
mvn -pl starter/kuzhambu-admin-starter -Dtest=AdminStarterArchitectureTest test
```

### 后端门禁

```sh
cd kuzhambu-servers
mvn -pl biz/operations,starter/kuzhambu-admin-starter -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/operations,starter/kuzhambu-admin-starter -am test
```

### 前端定向验证

```sh
cd kuzhambu-apps
npm --workspace kuzhambu-admin-web run format
npm --workspace kuzhambu-admin-web run test -- health
npm --workspace kuzhambu-admin-web run test -- dashboard
```

### 前端门禁

```sh
cd kuzhambu-apps
npm run format:check
npm run lint
npm run build
npm run test
```

## 审核清单

- 外部 HTTP 探针默认关闭。
- 外部 HTTP 探针关闭时不会产生 HTTP 调用和健康记录。
- 外部 HTTP 探针开启后，每个有效 target 都能写入 `operations_health_check`。
- 单个 target 无效、超时或异常不会阻断其他 target。
- `DOWN` / `DEGRADED` 外部探针结果会触发现有健康告警策略。
- 健康细分页可以按组件、状态、来源、目标和时间范围定位探针记录。
- 健康详情抽屉能展示格式化 JSON，也能降级展示非 JSON 原文。
- 表格行可以进入关联告警。
- Dashboard 只保留摘要和跳转，不重复实现完整分页。
- 配置样例、菜单权限、接口契约、前端服务契约和覆盖矩阵全部同步。
