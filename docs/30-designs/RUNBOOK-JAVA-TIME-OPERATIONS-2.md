# operations Java Time 迁移 RUNBOOK（第 2/3 批）

## Purpose

独立完成 `operations` 本批旧时间类型迁移，闭合字段、签名、调用方、持久化与协议边界。本文件包含执行所需的完整上下文，不依赖其他迁移 RUNBOOK。

## Scope

- 本批包含 **47** 个生产文件。
- 每个 Task 包含 2–8 个生产文件。
- 字段、签名和操作行以当前分支 `HEAD` 为盘点基线。
- 仅可依据真实编译或调用关系补充遗漏文件，并在任务结果中记录证据。

## Non-goals

- 不机械迁移其他业务域。
- 不修改与时间类型无关的业务逻辑。
- 不把精确时间点降为 `LocalDate`。
- 不默认修改数据库列类型；发现无法无损映射时先停止并补充数据库决策。

## Migration Rules

- domain、application、facade 的时间点使用 `Instant`。
- interface 的自然日期使用 `LocalDate`；精确时间点使用 `Instant`，显式偏移协议使用 `OffsetDateTime`。
- infra 的时间戳列使用 `Instant`，SQL `DATE` 列使用 `LocalDate`。
- `new Date()` 改为 `Instant.now()`；关键规则需要可控时间时注入 `Clock`。
- `before/after` 改为 `isBefore/isAfter`；毫秒算术改为 `Duration` 或 `ChronoUnit`。
- 禁止通过系统默认时区做隐式转换。

## File Inventory

| 层 | 主题 | 文件 | 字段 | 签名与操作 |
|---|---|---|---|---|
| `application` | `health` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/query/OperationsHealthPageQuery.java` | `checkedAtStart`(L18), `checkedAtEnd`(L19) | — |
| `application` | `health` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/query/OperationsHealthTrendQuery.java` | `periodStart`(L16), `periodEnd`(L17) | — |
| `application` | `health` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/result/OperationsHealthAlertPageResult.java` | `firstTriggeredAt`(L28), `lastTriggeredAt`(L29), `ackedAt`(L30), `recoveredAt`(L32) | — |
| `application` | `health` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/result/OperationsHealthPageResult.java` | `checkedAt`(L23) | — |
| `application` | `health` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/result/OperationsHealthSummaryResult.java` | `checkedAt`(L22) | — |
| `application` | `health` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/service/impl/HealthAlertApplicationServiceImpl.java` | — | 操作行 L77, L87 |
| `application` | `health` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthAlertStrategy.java` | — | 操作行 L90, L91, L92, L189, L236 |
| `application` | `health` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthCollector.java` | — | L90 `private HealthCheckRecord failureRecord(OperationsHealthProbe probe, String message, Date checkedAt) {`<br>操作行 L69 |
| `domain` | `health` | `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/model/entity/HealthAlertRecord.java` | `firstTriggeredAt`(L29), `lastTriggeredAt`(L30), `ackedAt`(L31), `recoveredAt`(L33) | — |
| `domain` | `health` | `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/model/entity/HealthCheckRecord.java` | `checkedAt`(L24) | — |
| `domain` | `health` | `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/repository/HealthCheckRepository.java` | — | L21 `Date checkedAtStart,`<br>L22 `Date checkedAtEnd,`<br>L35 `default List<HealthCheckId> listExpiredCheckIds(Date checkedBefore, int limit) {`<br>操作行 L27 |
| `infra` | `health` | `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/persistence/dataobject/HealthAlertDO.java` | `firstTriggeredAt`(L32), `lastTriggeredAt`(L33), `ackedAt`(L34), `recoveredAt`(L36) | — |
| `infra` | `health` | `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/persistence/dataobject/HealthCheckDO.java` | `checkedAt`(L28) | — |
| `infra` | `health` | `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/repository/impl/HealthCheckRepositoryImpl.java` | — | L73 `Date checkedAtStart,`<br>L74 `Date checkedAtEnd,`<br>L127 `public List<HealthCheckId> listExpiredCheckIds(Date checkedBefore, int limit) {`<br>L146 `Date checkedAtStart,`<br>操作行 L90, L147, L173 |
| `interface` | `health` | `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/request/OperationsHealthPageRequest.java` | `checkedAtStart`(L37), `checkedAtEnd`(L41) | — |
| `interface` | `health` | `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/request/OperationsHealthTrendRequest.java` | `periodStart`(L28), `periodEnd`(L32) | — |
| `interface` | `health` | `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/response/OperationsHealthAlertPageResponse.java` | `firstTriggeredAt`(L74), `lastTriggeredAt`(L78), `ackedAt`(L82), `recoveredAt`(L90) | — |
| `interface` | `health` | `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/response/OperationsHealthAlertSummaryResponse.java` | `lastTriggeredAt`(L70) | — |
| `interface` | `health` | `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/response/OperationsHealthPageResponse.java` | `checkedAt`(L24) | — |
| `interface` | `health` | `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/response/OperationsHealthSummaryResponse.java` | `checkedAt`(L23) | — |
| `application` | `report` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/command/OperationsReportGenerateCommand.java` | `periodStart`(L17), `periodEnd`(L18) | — |
| `application` | `report` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/query/OperationsReportPageQuery.java` | `periodStart`(L19), `periodEnd`(L20) | — |
| `application` | `report` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/result/OperationsReportDetailResult.java` | `periodStart`(L19), `periodEnd`(L20), `requestedAt`(L29), `completedAt`(L30) | — |
| `application` | `report` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/result/OperationsReportPageResult.java` | `periodStart`(L19), `periodEnd`(L20), `requestedAt`(L26), `completedAt`(L27) | — |
| `application` | `report` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/service/impl/ReportApplicationServiceImpl.java` | — | 操作行 L57 |
| `application` | `report` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportTaskExecutor.java` | — | 操作行 L96, L103 |
| `application` | `report` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/OperationsReportSnapshotAssembler.java` | — | 操作行 L57 |
| `application` | `report` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/OperationsReportSupportModels.java` | `periodStart`(L26), `periodEnd`(L27), `generatedAt`(L30) | — |
| `domain` | `report` | `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/report/model/entity/ReportRecord.java` | `periodStart`(L20), `periodEnd`(L21), `requestedAt`(L30), `completedAt`(L31) | — |
| `domain` | `report` | `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/report/repository/ReportRepository.java` | — | L18 `Date periodStart,`<br>L19 `Date periodEnd,`<br>L29 `default List<ReportId> listExpiredReportIds(Date requestedBefore, int limit) {` |
| `infra` | `report` | `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/persistence/dataobject/ReportDO.java` | `periodStart`(L23), `periodEnd`(L24), `requestedAt`(L33), `completedAt`(L34) | — |
| `infra` | `report` | `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/repository/impl/ReportRepositoryImpl.java` | — | L42 `Date periodStart,`<br>L43 `Date periodEnd,`<br>L92 `public List<ReportId> listExpiredReportIds(Date requestedBefore, int limit) {`<br>L112 `Date periodStart,`<br>操作行 L113 |
| `interface` | `report` | `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/report/controller/request/OperationsReportGenerateRequest.java` | `periodStart`(L33), `periodEnd`(L38) | — |
| `interface` | `report` | `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/report/controller/request/OperationsReportPageRequest.java` | `periodStart`(L37), `periodEnd`(L41) | — |
| `interface` | `report` | `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/report/controller/response/OperationsReportDetailResponse.java` | `periodStart`(L19), `periodEnd`(L20), `requestedAt`(L29), `completedAt`(L30) | — |
| `interface` | `report` | `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/report/controller/response/OperationsReportPageResponse.java` | `periodStart`(L19), `periodEnd`(L20), `requestedAt`(L26), `completedAt`(L27) | — |
| `application` | `restore` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/result/OperationsRestoreDetailResult.java` | `writeBlockStartedAt`(L21), `writeBlockReleasedAt`(L22), `startedAt`(L25), `completedAt`(L26) | L36 `Date startedAt,`<br>操作行 L37 |
| `application` | `restore` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/result/OperationsRestoreExecuteResult.java` | `writeBlockStartedAt`(L21), `writeBlockReleasedAt`(L22), `startedAt`(L24), `completedAt`(L25) | L34 `Date startedAt,`<br>操作行 L35 |
| `application` | `restore` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/result/OperationsRestorePageResult.java` | `writeBlockStartedAt`(L21), `writeBlockReleasedAt`(L22), `startedAt`(L25), `completedAt`(L26) | L36 `Date startedAt,`<br>操作行 L37 |
| `application` | `restore` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/service/impl/RestoreApplicationServiceImpl.java` | — | L266 `private BackupRecord buildPreRestoreRecord(Long requesterUserId, Date startedAt, String preRestoreTimestamp) {`<br>L399 `private String formatTimestamp(Date date) {`<br>操作行 L154, L199, L220, L250, L279, L289, L301, L306, L314 |
| `application` | `restore` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/support/OperationsRestoreWriteBlocker.java` | — | L13 `public Date enable(RestoreId restoreId) {`<br>L23 `public Date disable(RestoreId restoreId) {`<br>操作行 L20, L26, L32 |
| `domain` | `restore` | `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/restore/model/entity/RestoreRecord.java` | `writeBlockStartedAt`(L22), `writeBlockReleasedAt`(L23), `startedAt`(L26), `completedAt`(L27) | — |
| `infra` | `restore` | `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/restore/persistence/dataobject/RestoreDO.java` | `writeBlockStartedAt`(L26), `writeBlockReleasedAt`(L27), `startedAt`(L30), `completedAt`(L31) | — |
| `interface` | `restore` | `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/configure/OperationsRestoreWriteBlockConfiguration.java` | — | L29 `public Date enable(RestoreId restoreId) {`<br>L30 `Date enabledAt = super.enable(restoreId);`<br>L36 `public Date disable(RestoreId restoreId) {`<br>L37 `Date disabledAt = super.disable(restoreId);` |
| `interface` | `restore` | `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/response/OperationsRestoreDetailResponse.java` | `writeBlockStartedAt`(L22), `writeBlockReleasedAt`(L23), `startedAt`(L26), `completedAt`(L27) | — |
| `interface` | `restore` | `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/response/OperationsRestoreExecuteResponse.java` | `writeBlockStartedAt`(L22), `writeBlockReleasedAt`(L23), `startedAt`(L25), `completedAt`(L26) | — |
| `interface` | `restore` | `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/response/OperationsRestorePageResponse.java` | `writeBlockStartedAt`(L22), `writeBlockReleasedAt`(L23), `startedAt`(L26), `completedAt`(L27) | — |

## Plan

### Task 1: health 时间类型闭环

涉及生产文件：**8** 个。

#### Files And Changes

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/query/OperationsHealthPageQuery.java`
  - L18 `checkedAtStart`：`Date` → `Instant`。
  - L19 `checkedAtEnd`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/query/OperationsHealthTrendQuery.java`
  - L16 `periodStart`：`Date` → `Instant`。
  - L17 `periodEnd`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/result/OperationsHealthAlertPageResult.java`
  - L28 `firstTriggeredAt`：`Date` → `Instant`。
  - L29 `lastTriggeredAt`：`Date` → `Instant`。
  - L30 `ackedAt`：`Date` → `Instant`。
  - L32 `recoveredAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/result/OperationsHealthPageResult.java`
  - L23 `checkedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/result/OperationsHealthSummaryResult.java`
  - L22 `checkedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/service/impl/HealthAlertApplicationServiceImpl.java`
  - L77, L87：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthAlertStrategy.java`
  - L90, L91, L92, L189, L236：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthCollector.java`
  - L90：将签名 `private HealthCheckRecord failureRecord(OperationsHealthProbe probe, String message, Date checkedAt) {` 的 `Date` 与本调用链目标类型同步。
  - L69：调整当前时间构造、比较、算术或转换。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

### Task 2: health 时间类型闭环

涉及生产文件：**8** 个。

#### Files And Changes

- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/model/entity/HealthAlertRecord.java`
  - L29 `firstTriggeredAt`：`Date` → `Instant`。
  - L30 `lastTriggeredAt`：`Date` → `Instant`。
  - L31 `ackedAt`：`Date` → `Instant`。
  - L33 `recoveredAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/model/entity/HealthCheckRecord.java`
  - L24 `checkedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/repository/HealthCheckRepository.java`
  - L21：将签名 `Date checkedAtStart,` 的 `Date` 与本调用链目标类型同步。
  - L22：将签名 `Date checkedAtEnd,` 的 `Date` 与本调用链目标类型同步。
  - L35：将签名 `default List<HealthCheckId> listExpiredCheckIds(Date checkedBefore, int limit) {` 的 `Date` 与本调用链目标类型同步。
  - L27：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/persistence/dataobject/HealthAlertDO.java`
  - L32 `firstTriggeredAt`：`Date` → `Instant`。
  - L33 `lastTriggeredAt`：`Date` → `Instant`。
  - L34 `ackedAt`：`Date` → `Instant`。
  - L36 `recoveredAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/persistence/dataobject/HealthCheckDO.java`
  - L28 `checkedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/repository/impl/HealthCheckRepositoryImpl.java`
  - L73：将签名 `Date checkedAtStart,` 的 `Date` 与本调用链目标类型同步。
  - L74：将签名 `Date checkedAtEnd,` 的 `Date` 与本调用链目标类型同步。
  - L127：将签名 `public List<HealthCheckId> listExpiredCheckIds(Date checkedBefore, int limit) {` 的 `Date` 与本调用链目标类型同步。
  - L146：将签名 `Date checkedAtStart,` 的 `Date` 与本调用链目标类型同步。
  - L90, L147, L173：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/request/OperationsHealthPageRequest.java`
  - L37 `checkedAtStart`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L41 `checkedAtEnd`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/request/OperationsHealthTrendRequest.java`
  - L28 `periodStart`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L32 `periodEnd`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

### Task 3: health 时间类型闭环

涉及生产文件：**4** 个。

#### Files And Changes

- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/response/OperationsHealthAlertPageResponse.java`
  - L74 `firstTriggeredAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L78 `lastTriggeredAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L82 `ackedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L90 `recoveredAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/response/OperationsHealthAlertSummaryResponse.java`
  - L70 `lastTriggeredAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/response/OperationsHealthPageResponse.java`
  - L24 `checkedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/response/OperationsHealthSummaryResponse.java`
  - L23 `checkedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

### Task 4: report 时间类型闭环

涉及生产文件：**8** 个。

#### Files And Changes

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/command/OperationsReportGenerateCommand.java`
  - L17 `periodStart`：`Date` → `Instant`。
  - L18 `periodEnd`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/query/OperationsReportPageQuery.java`
  - L19 `periodStart`：`Date` → `Instant`。
  - L20 `periodEnd`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/result/OperationsReportDetailResult.java`
  - L19 `periodStart`：`Date` → `Instant`。
  - L20 `periodEnd`：`Date` → `Instant`。
  - L29 `requestedAt`：`Date` → `Instant`。
  - L30 `completedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/result/OperationsReportPageResult.java`
  - L19 `periodStart`：`Date` → `Instant`。
  - L20 `periodEnd`：`Date` → `Instant`。
  - L26 `requestedAt`：`Date` → `Instant`。
  - L27 `completedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/service/impl/ReportApplicationServiceImpl.java`
  - L57：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportTaskExecutor.java`
  - L96, L103：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/OperationsReportSnapshotAssembler.java`
  - L57：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/OperationsReportSupportModels.java`
  - L26 `periodStart`：`Date` → `Instant`。
  - L27 `periodEnd`：`Date` → `Instant`。
  - L30 `generatedAt`：`Date` → `Instant`。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

### Task 5: report 时间类型闭环

涉及生产文件：**8** 个。

#### Files And Changes

- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/report/model/entity/ReportRecord.java`
  - L20 `periodStart`：`Date` → `Instant`。
  - L21 `periodEnd`：`Date` → `Instant`。
  - L30 `requestedAt`：`Date` → `Instant`。
  - L31 `completedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/report/repository/ReportRepository.java`
  - L18：将签名 `Date periodStart,` 的 `Date` 与本调用链目标类型同步。
  - L19：将签名 `Date periodEnd,` 的 `Date` 与本调用链目标类型同步。
  - L29：将签名 `default List<ReportId> listExpiredReportIds(Date requestedBefore, int limit) {` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/persistence/dataobject/ReportDO.java`
  - L23 `periodStart`：`Date` → `Instant`。
  - L24 `periodEnd`：`Date` → `Instant`。
  - L33 `requestedAt`：`Date` → `Instant`。
  - L34 `completedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/repository/impl/ReportRepositoryImpl.java`
  - L42：将签名 `Date periodStart,` 的 `Date` 与本调用链目标类型同步。
  - L43：将签名 `Date periodEnd,` 的 `Date` 与本调用链目标类型同步。
  - L92：将签名 `public List<ReportId> listExpiredReportIds(Date requestedBefore, int limit) {` 的 `Date` 与本调用链目标类型同步。
  - L112：将签名 `Date periodStart,` 的 `Date` 与本调用链目标类型同步。
  - L113：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/report/controller/request/OperationsReportGenerateRequest.java`
  - L33 `periodStart`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L38 `periodEnd`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/report/controller/request/OperationsReportPageRequest.java`
  - L37 `periodStart`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L41 `periodEnd`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/report/controller/response/OperationsReportDetailResponse.java`
  - L19 `periodStart`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L20 `periodEnd`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L29 `requestedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L30 `completedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/report/controller/response/OperationsReportPageResponse.java`
  - L19 `periodStart`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L20 `periodEnd`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L26 `requestedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L27 `completedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

### Task 6: restore 时间类型闭环

涉及生产文件：**8** 个。

#### Files And Changes

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/result/OperationsRestoreDetailResult.java`
  - L21 `writeBlockStartedAt`：`Date` → `Instant`。
  - L22 `writeBlockReleasedAt`：`Date` → `Instant`。
  - L25 `startedAt`：`Date` → `Instant`。
  - L26 `completedAt`：`Date` → `Instant`。
  - L36：将签名 `Date startedAt,` 的 `Date` 与本调用链目标类型同步。
  - L37：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/result/OperationsRestoreExecuteResult.java`
  - L21 `writeBlockStartedAt`：`Date` → `Instant`。
  - L22 `writeBlockReleasedAt`：`Date` → `Instant`。
  - L24 `startedAt`：`Date` → `Instant`。
  - L25 `completedAt`：`Date` → `Instant`。
  - L34：将签名 `Date startedAt,` 的 `Date` 与本调用链目标类型同步。
  - L35：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/result/OperationsRestorePageResult.java`
  - L21 `writeBlockStartedAt`：`Date` → `Instant`。
  - L22 `writeBlockReleasedAt`：`Date` → `Instant`。
  - L25 `startedAt`：`Date` → `Instant`。
  - L26 `completedAt`：`Date` → `Instant`。
  - L36：将签名 `Date startedAt,` 的 `Date` 与本调用链目标类型同步。
  - L37：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/service/impl/RestoreApplicationServiceImpl.java`
  - L266：将签名 `private BackupRecord buildPreRestoreRecord(Long requesterUserId, Date startedAt, String preRestoreTimestamp) {` 的 `Date` 与本调用链目标类型同步。
  - L399：将签名 `private String formatTimestamp(Date date) {` 的 `Date` 与本调用链目标类型同步。
  - L154, L199, L220, L250, L279, L289, L301, L306, L314：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/support/OperationsRestoreWriteBlocker.java`
  - L13：将签名 `public Date enable(RestoreId restoreId) {` 的 `Date` 与本调用链目标类型同步。
  - L23：将签名 `public Date disable(RestoreId restoreId) {` 的 `Date` 与本调用链目标类型同步。
  - L20, L26, L32：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/restore/model/entity/RestoreRecord.java`
  - L22 `writeBlockStartedAt`：`Date` → `Instant`。
  - L23 `writeBlockReleasedAt`：`Date` → `Instant`。
  - L26 `startedAt`：`Date` → `Instant`。
  - L27 `completedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/restore/persistence/dataobject/RestoreDO.java`
  - L26 `writeBlockStartedAt`：`Date` → `Instant`。
  - L27 `writeBlockReleasedAt`：`Date` → `Instant`。
  - L30 `startedAt`：`Date` → `Instant`。
  - L31 `completedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/configure/OperationsRestoreWriteBlockConfiguration.java`
  - L29：将签名 `public Date enable(RestoreId restoreId) {` 的 `Date` 与本调用链目标类型同步。
  - L30：将签名 `Date enabledAt = super.enable(restoreId);` 的 `Date` 与本调用链目标类型同步。
  - L36：将签名 `public Date disable(RestoreId restoreId) {` 的 `Date` 与本调用链目标类型同步。
  - L37：将签名 `Date disabledAt = super.disable(restoreId);` 的 `Date` 与本调用链目标类型同步。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

### Task 7: restore 时间类型闭环

涉及生产文件：**3** 个。

#### Files And Changes

- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/response/OperationsRestoreDetailResponse.java`
  - L22 `writeBlockStartedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L23 `writeBlockReleasedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L26 `startedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L27 `completedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/response/OperationsRestoreExecuteResponse.java`
  - L22 `writeBlockStartedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L23 `writeBlockReleasedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L25 `startedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L26 `completedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/response/OperationsRestorePageResponse.java`
  - L22 `writeBlockStartedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L23 `writeBlockReleasedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L26 `startedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L27 `completedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

## Associated Test Files

下列测试按本批主题匹配；执行时还必须根据编译和真实调用链补充测试。

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthAlertStrategyTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthCollectorTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/report/service/impl/ReportApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGatewayTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportTaskExecutorTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/restore/service/impl/RestoreApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/test/java/com/thundax/kuzhambu/operations/infra/health/repository/impl/HealthAlertRepositoryImplTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/test/java/com/thundax/kuzhambu/operations/infra/health/repository/impl/HealthCheckRepositoryImplTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/test/java/com/thundax/kuzhambu/operations/infra/report/repository/impl/ReportRepositoryImplTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/test/java/com/thundax/kuzhambu/operations/infra/restore/repository/impl/RestoreRepositoryImplTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/OperationsHealthAdminControllerTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/OperationsHealthAlertAdminControllerTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/OperationsHealthContractTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/report/controller/OperationsReportAdminControllerTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/report/controller/OperationsReportContractTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/OperationsRestoreAdminControllerTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/OperationsRestoreContractTest.java`

## Verification

- `rg 'java\.util\.Date|java\.sql\.(Date|Timestamp)' kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/query/OperationsHealthPageQuery.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/query/OperationsHealthTrendQuery.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/result/OperationsHealthAlertPageResult.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/result/OperationsHealthPageResult.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/result/OperationsHealthSummaryResult.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/service/impl/HealthAlertApplicationServiceImpl.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthAlertStrategy.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthCollector.java kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/model/entity/HealthAlertRecord.java kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/model/entity/HealthCheckRecord.java kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/repository/HealthCheckRepository.java kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/persistence/dataobject/HealthAlertDO.java kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/persistence/dataobject/HealthCheckDO.java kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/repository/impl/HealthCheckRepositoryImpl.java kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/request/OperationsHealthPageRequest.java kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/request/OperationsHealthTrendRequest.java kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/response/OperationsHealthAlertPageResponse.java kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/response/OperationsHealthAlertSummaryResponse.java kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/response/OperationsHealthPageResponse.java kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/response/OperationsHealthSummaryResponse.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/command/OperationsReportGenerateCommand.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/query/OperationsReportPageQuery.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/result/OperationsReportDetailResult.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/result/OperationsReportPageResult.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/service/impl/ReportApplicationServiceImpl.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportTaskExecutor.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/OperationsReportSnapshotAssembler.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/OperationsReportSupportModels.java kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/report/model/entity/ReportRecord.java kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/report/repository/ReportRepository.java kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/persistence/dataobject/ReportDO.java kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/repository/impl/ReportRepositoryImpl.java kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/report/controller/request/OperationsReportGenerateRequest.java kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/report/controller/request/OperationsReportPageRequest.java kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/report/controller/response/OperationsReportDetailResponse.java kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/report/controller/response/OperationsReportPageResponse.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/result/OperationsRestoreDetailResult.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/result/OperationsRestoreExecuteResult.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/result/OperationsRestorePageResult.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/service/impl/RestoreApplicationServiceImpl.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/support/OperationsRestoreWriteBlocker.java kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/restore/model/entity/RestoreRecord.java kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/restore/persistence/dataobject/RestoreDO.java kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/configure/OperationsRestoreWriteBlockConfiguration.java kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/response/OperationsRestoreDetailResponse.java kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/response/OperationsRestoreExecuteResponse.java kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/response/OperationsRestorePageResponse.java`。
- `cd kuzhambu-servers && mvn -pl :kuzhambu-operations-domain,:kuzhambu-operations-application,:kuzhambu-operations-interface,:kuzhambu-operations-infra spotless:apply`，随后检查 `git diff`。
- `cd kuzhambu-servers && mvn spotless:check`。
- `cd kuzhambu-servers && mvn checkstyle:check`。
- `cd kuzhambu-servers && mvn -pl :kuzhambu-operations-domain,:kuzhambu-operations-application,:kuzhambu-operations-interface,:kuzhambu-operations-infra -am -amd test`；确认 Reactor Build Order 包含上述 4 个叶子模块及其下游装配模块。
- 涉及 HTTP 时验证 JSON 精度、格式和时区；涉及 infra 时验证数据库往返。

## Closure

本批所有 Task 完成、验证通过并同步必要接口或数据库文档后删除本 RUNBOOK。未完成范围必须收窄为新的独立 RUNBOOK，不保留完成历史。
