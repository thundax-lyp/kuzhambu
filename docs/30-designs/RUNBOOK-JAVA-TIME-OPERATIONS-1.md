# operations Java Time 迁移 RUNBOOK（第 1/3 批）

## Purpose

独立完成 `operations` 本批旧时间类型迁移，闭合字段、签名、调用方、持久化与协议边界。本文件包含执行所需的完整上下文，不依赖其他迁移 RUNBOOK。

## Scope

- 本批包含 **30** 个生产文件。
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
| `application` | `backup` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/result/OperationsBackupDetailResult.java` | `startedAt`(L24), `completedAt`(L25), `expiresAt`(L26) | — |
| `application` | `backup` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/result/OperationsBackupExecuteResult.java` | `startedAt`(L22), `completedAt`(L23), `expiresAt`(L24) | — |
| `application` | `backup` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/result/OperationsBackupPageResult.java` | `startedAt`(L23), `completedAt`(L24), `expiresAt`(L25) | — |
| `application` | `backup` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/service/impl/BackupApplicationServiceImpl.java` | — | L238 `private String formatTimestamp(Date date) {`<br>操作行 L91, L105, L114, L119, L127, L141 |
| `domain` | `backup` | `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/backup/model/entity/BackupRecord.java` | `startedAt`(L25), `completedAt`(L26), `expiresAt`(L27) | — |
| `domain` | `backup` | `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/backup/repository/BackupRepository.java` | — | L24 `default List<BackupId> listExpiredBackupIds(Date now, int limit) {` |
| `infra` | `backup` | `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/backup/persistence/dataobject/BackupDO.java` | `startedAt`(L29), `completedAt`(L30), `expiresAt`(L31) | — |
| `infra` | `backup` | `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/backup/repository/impl/BackupRepositoryImpl.java` | — | L92 `public List<BackupId> listExpiredBackupIds(Date now, int limit) {` |
| `interface` | `backup` | `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/backup/controller/response/OperationsBackupDetailResponse.java` | `startedAt`(L25), `completedAt`(L26), `expiresAt`(L27) | — |
| `interface` | `backup` | `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/backup/controller/response/OperationsBackupExecuteResponse.java` | `startedAt`(L23), `completedAt`(L24), `expiresAt`(L25) | — |
| `interface` | `backup` | `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/backup/controller/response/OperationsBackupPageResponse.java` | `startedAt`(L24), `completedAt`(L25), `expiresAt`(L26) | — |
| `application` | `cleanup` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/command/OperationsCleanupExecuteCommand.java` | `requestedAt`(L16) | — |
| `application` | `cleanup` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/result/OperationsCleanupDetailResult.java` | `startedAt`(L25), `completedAt`(L26), `processedAt`(L39) | — |
| `application` | `cleanup` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/result/OperationsCleanupPageResult.java` | `startedAt`(L23), `completedAt`(L24) | — |
| `application` | `cleanup` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/service/impl/CleanupApplicationServiceImpl.java` | — | L327 `private Date backupCleanupThreshold(Date requestedAt, Integer retentionDays) {`<br>L331 `private Date cleanupThreshold(Date requestedAt, Integer retentionDays) {`<br>操作行 L118, L151, L159, L205, L218, L224, L231, L335 |
| `application` | `cleanup` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/support/OperationsCleanupScheduler.java` | — | L53 `private void executePolicy(OperationsCleanupScheduleProperties.CleanupPolicy policy, Date requestedAt) {`<br>操作行 L44 |
| `domain` | `cleanup` | `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/cleanup/model/entity/CleanupItem.java` | `processedAt`(L22) | — |
| `domain` | `cleanup` | `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/cleanup/model/entity/CleanupJob.java` | `startedAt`(L26), `completedAt`(L27) | — |
| `infra` | `cleanup` | `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/cleanup/persistence/dataobject/CleanupItemDO.java` | `processedAt`(L26) | — |
| `infra` | `cleanup` | `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/cleanup/persistence/dataobject/CleanupJobDO.java` | `startedAt`(L28), `completedAt`(L29) | — |
| `interface` | `cleanup` | `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/cleanup/controller/response/OperationsCleanupDetailResponse.java` | `startedAt`(L25), `completedAt`(L26), `processedAt`(L40) | — |
| `interface` | `cleanup` | `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/cleanup/controller/response/OperationsCleanupExecuteResponse.java` | `startedAt`(L24), `completedAt`(L25) | — |
| `interface` | `cleanup` | `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/cleanup/controller/response/OperationsCleanupPageResponse.java` | `startedAt`(L24), `completedAt`(L25) | — |
| `application` | `dashboard` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/query/OperationsDashboardOverviewQuery.java` | `periodStart`(L15), `periodEnd`(L16) | — |
| `application` | `dashboard` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/result/OperationsDashboardOverviewResult.java` | `periodStart`(L17), `periodEnd`(L18), `lastTriggeredAt`(L77) | — |
| `application` | `dashboard` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/service/impl/OperationsDashboardApplicationServiceImpl.java` | — | L184 `Date periodStart = query == null ? null : query.getPeriodStart();`<br>L185 `Date periodEnd = query == null ? null : query.getPeriodEnd();`<br>L354 `private record PeriodRange(Date periodStart, Date periodEnd) {}`<br>操作行 L204 |
| `application` | `dashboard` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/support/DefaultOperationsDashboardSummaryGateway.java` | — | 操作行 L41, L50, L64, L78, L92 |
| `application` | `dashboard` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/support/OperationsDashboardSummaryGateway.java` | — | 操作行 L9 |
| `interface` | `dashboard` | `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/controller/request/OperationsDashboardOverviewRequest.java` | `periodStart`(L24), `periodEnd`(L28) | — |
| `interface` | `dashboard` | `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/controller/response/OperationsDashboardOverviewResponse.java` | `periodStart`(L20), `periodEnd`(L21) | — |

## Plan

### Task 1: backup 时间类型闭环

涉及生产文件：**8** 个。

#### Files And Changes

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/result/OperationsBackupDetailResult.java`
  - L24 `startedAt`：`Date` → `Instant`。
  - L25 `completedAt`：`Date` → `Instant`。
  - L26 `expiresAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/result/OperationsBackupExecuteResult.java`
  - L22 `startedAt`：`Date` → `Instant`。
  - L23 `completedAt`：`Date` → `Instant`。
  - L24 `expiresAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/result/OperationsBackupPageResult.java`
  - L23 `startedAt`：`Date` → `Instant`。
  - L24 `completedAt`：`Date` → `Instant`。
  - L25 `expiresAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/service/impl/BackupApplicationServiceImpl.java`
  - L238：将签名 `private String formatTimestamp(Date date) {` 的 `Date` 与本调用链目标类型同步。
  - L91, L105, L114, L119, L127, L141：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/backup/model/entity/BackupRecord.java`
  - L25 `startedAt`：`Date` → `Instant`。
  - L26 `completedAt`：`Date` → `Instant`。
  - L27 `expiresAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/backup/repository/BackupRepository.java`
  - L24：将签名 `default List<BackupId> listExpiredBackupIds(Date now, int limit) {` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/backup/persistence/dataobject/BackupDO.java`
  - L29 `startedAt`：`Date` → `Instant`。
  - L30 `completedAt`：`Date` → `Instant`。
  - L31 `expiresAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/backup/repository/impl/BackupRepositoryImpl.java`
  - L92：将签名 `public List<BackupId> listExpiredBackupIds(Date now, int limit) {` 的 `Date` 与本调用链目标类型同步。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

### Task 2: backup 时间类型闭环

涉及生产文件：**3** 个。

#### Files And Changes

- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/backup/controller/response/OperationsBackupDetailResponse.java`
  - L25 `startedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L26 `completedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L27 `expiresAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/backup/controller/response/OperationsBackupExecuteResponse.java`
  - L23 `startedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L24 `completedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L25 `expiresAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/backup/controller/response/OperationsBackupPageResponse.java`
  - L24 `startedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L25 `completedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L26 `expiresAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

### Task 3: cleanup 时间类型闭环

涉及生产文件：**8** 个。

#### Files And Changes

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/command/OperationsCleanupExecuteCommand.java`
  - L16 `requestedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/result/OperationsCleanupDetailResult.java`
  - L25 `startedAt`：`Date` → `Instant`。
  - L26 `completedAt`：`Date` → `Instant`。
  - L39 `processedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/result/OperationsCleanupPageResult.java`
  - L23 `startedAt`：`Date` → `Instant`。
  - L24 `completedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/service/impl/CleanupApplicationServiceImpl.java`
  - L327：将签名 `private Date backupCleanupThreshold(Date requestedAt, Integer retentionDays) {` 的 `Date` 与本调用链目标类型同步。
  - L331：将签名 `private Date cleanupThreshold(Date requestedAt, Integer retentionDays) {` 的 `Date` 与本调用链目标类型同步。
  - L118, L151, L159, L205, L218, L224, L231, L335：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/support/OperationsCleanupScheduler.java`
  - L53：将签名 `private void executePolicy(OperationsCleanupScheduleProperties.CleanupPolicy policy, Date requestedAt) {` 的 `Date` 与本调用链目标类型同步。
  - L44：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/cleanup/model/entity/CleanupItem.java`
  - L22 `processedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/cleanup/model/entity/CleanupJob.java`
  - L26 `startedAt`：`Date` → `Instant`。
  - L27 `completedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/cleanup/persistence/dataobject/CleanupItemDO.java`
  - L26 `processedAt`：`Date` → `Instant`。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

### Task 4: cleanup 时间类型闭环

涉及生产文件：**4** 个。

#### Files And Changes

- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/cleanup/persistence/dataobject/CleanupJobDO.java`
  - L28 `startedAt`：`Date` → `Instant`。
  - L29 `completedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/cleanup/controller/response/OperationsCleanupDetailResponse.java`
  - L25 `startedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L26 `completedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L40 `processedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/cleanup/controller/response/OperationsCleanupExecuteResponse.java`
  - L24 `startedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L25 `completedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/cleanup/controller/response/OperationsCleanupPageResponse.java`
  - L24 `startedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L25 `completedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

### Task 5: dashboard 时间类型闭环

涉及生产文件：**7** 个。

#### Files And Changes

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/query/OperationsDashboardOverviewQuery.java`
  - L15 `periodStart`：`Date` → `Instant`。
  - L16 `periodEnd`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/result/OperationsDashboardOverviewResult.java`
  - L17 `periodStart`：`Date` → `Instant`。
  - L18 `periodEnd`：`Date` → `Instant`。
  - L77 `lastTriggeredAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/service/impl/OperationsDashboardApplicationServiceImpl.java`
  - L184：将签名 `Date periodStart = query == null ? null : query.getPeriodStart();` 的 `Date` 与本调用链目标类型同步。
  - L185：将签名 `Date periodEnd = query == null ? null : query.getPeriodEnd();` 的 `Date` 与本调用链目标类型同步。
  - L354：将签名 `private record PeriodRange(Date periodStart, Date periodEnd) {}` 的 `Date` 与本调用链目标类型同步。
  - L204：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/support/DefaultOperationsDashboardSummaryGateway.java`
  - L41, L50, L64, L78, L92：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/support/OperationsDashboardSummaryGateway.java`
  - L9：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/controller/request/OperationsDashboardOverviewRequest.java`
  - L24 `periodStart`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L28 `periodEnd`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/controller/response/OperationsDashboardOverviewResponse.java`
  - L20 `periodStart`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L21 `periodEnd`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

## Associated Test Files

下列测试按本批主题匹配；执行时还必须根据编译和真实调用链补充测试。

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/backup/service/impl/BackupApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/cleanup/service/impl/CleanupApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/dashboard/service/impl/OperationsDashboardApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/dashboard/support/DefaultOperationsDashboardSummaryGatewayTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/backup/controller/OperationsBackupAdminControllerTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/backup/controller/OperationsBackupContractTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/cleanup/controller/OperationsCleanupAdminControllerTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/controller/OperationsDashboardAdminControllerTest.java`

## Verification

- `rg 'java\.util\.Date|java\.sql\.(Date|Timestamp)' kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/result/OperationsBackupDetailResult.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/result/OperationsBackupExecuteResult.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/result/OperationsBackupPageResult.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/service/impl/BackupApplicationServiceImpl.java kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/backup/model/entity/BackupRecord.java kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/backup/repository/BackupRepository.java kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/backup/persistence/dataobject/BackupDO.java kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/backup/repository/impl/BackupRepositoryImpl.java kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/backup/controller/response/OperationsBackupDetailResponse.java kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/backup/controller/response/OperationsBackupExecuteResponse.java kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/backup/controller/response/OperationsBackupPageResponse.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/command/OperationsCleanupExecuteCommand.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/result/OperationsCleanupDetailResult.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/result/OperationsCleanupPageResult.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/service/impl/CleanupApplicationServiceImpl.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/support/OperationsCleanupScheduler.java kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/cleanup/model/entity/CleanupItem.java kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/cleanup/model/entity/CleanupJob.java kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/cleanup/persistence/dataobject/CleanupItemDO.java kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/cleanup/persistence/dataobject/CleanupJobDO.java kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/cleanup/controller/response/OperationsCleanupDetailResponse.java kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/cleanup/controller/response/OperationsCleanupExecuteResponse.java kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/cleanup/controller/response/OperationsCleanupPageResponse.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/query/OperationsDashboardOverviewQuery.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/result/OperationsDashboardOverviewResult.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/service/impl/OperationsDashboardApplicationServiceImpl.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/support/DefaultOperationsDashboardSummaryGateway.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/support/OperationsDashboardSummaryGateway.java kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/controller/request/OperationsDashboardOverviewRequest.java kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/controller/response/OperationsDashboardOverviewResponse.java`。
- `cd kuzhambu-servers && mvn -pl :kuzhambu-operations-domain,:kuzhambu-operations-application,:kuzhambu-operations-interface,:kuzhambu-operations-infra spotless:apply`，随后检查 `git diff`。
- `cd kuzhambu-servers && mvn spotless:check`。
- `cd kuzhambu-servers && mvn checkstyle:check`。
- `cd kuzhambu-servers && mvn -pl :kuzhambu-operations-domain,:kuzhambu-operations-application,:kuzhambu-operations-interface,:kuzhambu-operations-infra -am -amd test`；确认 Reactor Build Order 包含上述 4 个叶子模块及其下游装配模块。
- 涉及 HTTP 时验证 JSON 精度、格式和时区；涉及 infra 时验证数据库往返。

## Closure

本批所有 Task 完成、验证通过并同步必要接口或数据库文档后删除本 RUNBOOK。未完成范围必须收窄为新的独立 RUNBOOK，不保留完成历史。
