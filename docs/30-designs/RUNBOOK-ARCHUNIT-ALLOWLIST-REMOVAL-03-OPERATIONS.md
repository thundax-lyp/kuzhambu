# ArchUnit allowlist 清理 03：Operations

## Purpose

删除 Operations 域全部 ArchUnit legacy allowlist，并将每一条例外替换为符合共享架构规则的生产代码或测试代码。完成后 Operations 的 application、domain 和 interface 架构测试不得再传入 legacy allowance。

## Scope

本 RUNBOOK 修改 `kuzhambu-servers/biz/operations/` 内的 Java 源码、测试源码，以及 action path 变更对应的 `kuzhambu-apps/admin-web/e2e/operations/report/report.spec.ts` 和本文件。纳入清理的规则如下：

| 规则 | 当前 allowlist 数量 | 归属测试 |
| --- | ---: | --- |
| `COMMAND_QUERY_RECORD` | 20 | `OperationsApplicationCommandQueryRecordAllowances.java` |
| `METHOD_SHAPE` | 1 | `OperationsApplicationArchitectureTest.java` |
| `COMMAND_QUERY_CONSTRUCTION` | 2 | `OperationsApplicationArchitectureTest.java` |
| `COMMAND_QUERY_ASSEMBLER_NULL_RETURN` | 8 | `OperationsApplicationArchitectureTest.java` |
| Repository method name | 2 | `OperationsDomainArchitectureTest.java` |
| Response required annotations | 26 | `OperationsInterfaceArchitectureTest.java` |
| Controller action verb | 8 | `OperationsInterfaceArchitectureTest.java` |
| Boundary assembler non-null contract | 3 classes | `OperationsInterfaceArchitectureTest.java` |

## Non-goals

- 不修改 `kuzhambu-common-test` 中的共享 ArchUnit 规则。
- 不修改 Operations 以外业务域的后端源码、测试或 allowlist；仅修改本 RUNBOOK 明列的 admin-web E2E 路由 mock。
- 不改变备份、清理、健康检查、报表、恢复和任务的业务语义、HTTP 方法、请求字段或响应字段。为通过 Controller 动词规则而改名的方法，必须将其 action path 同步改为下文指定的目标动词。
- 不迁移批处理调度职责到其他业务域。

## Prerequisites

1. 在仓库根目录确认工作区仅包含本任务允许保留的变更：`git status --short`。
2. 在 `kuzhambu-servers/` 目录执行每个工作包前，运行 `mvn -pl biz/operations/kuzhambu-operations-application,biz/operations/kuzhambu-operations-domain,biz/operations/kuzhambu-operations-interface,biz/operations/kuzhambu-operations-infra spotless:apply`。
3. 严格按照下面编号顺序执行。后续工作包依赖前序工作包已经编译通过的 record 构造器、Assembler 非空约定和 Repository 方法名。
4. 每个工作包完成后，执行其“检查”命令；命令失败时停止，不进入下一包。

## Plan

### 1. 将 backup Command/Query 及直接调用点改为 record（10 个文件）

修改以下文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/command/OperationsBackupExecuteCommand.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/query/OperationsBackupDetailQuery.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/query/OperationsBackupQuery.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/service/impl/BackupApplicationServiceImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/backup/assembler/OperationsBackupInterfaceAssembler.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/backup/service/impl/BackupApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/backup/support/OperationsBackupSchedulerTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/OperationsApplicationArchitectureTest.java`

将三个 contract 保留类名、包名、字段名、字段类型和字段声明顺序，替换为同分量顺序的 Java `record`，并删除 Lombok 注解与 import。将上述调用点的 `getRequesterUserId()`、`getBackupId()`、`getBackupType()`、`getBackupStatus()` 改为对应 record accessor；无参构造与 setter 初始化改为完整构造器。不得修改本包以外文件。

检查：`mvn -pl biz/operations/kuzhambu-operations-application -am test -Dtest=OperationsApplicationArchitectureTest`。

### 2. 将 cleanup Command/Query 及直接调用点改为 record（8 个文件）

修改以下文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/command/OperationsCleanupExecuteCommand.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/query/OperationsCleanupDetailQuery.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/query/OperationsCleanupQuery.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/service/impl/CleanupApplicationServiceImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/support/OperationsCleanupScheduler.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/cleanup/assembler/OperationsCleanupInterfaceAssembler.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/cleanup/service/impl/CleanupApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/OperationsApplicationArchitectureTest.java`

按工作包 1 的 record 转换规则处理。保留 `OperationsCleanupExecuteCommand(String cleanupType, Long requesterUserId)` 的两分量调用语义：调用点统一改为完整五分量构造器，其中 `requestedAt`、`retentionDays`、`limit` 传入现有行为所用的值或 `null`。所有读取改为 record accessor。

检查：`mvn -pl biz/operations/kuzhambu-operations-application -am test -Dtest=OperationsApplicationArchitectureTest`。

### 3. 将 dashboard、health 的 Command/Query 改为 record（6 个文件）

修改以下文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/query/OperationsDashboardOverviewQuery.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/command/OperationsHealthAlertAckCommand.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/command/OperationsHealthAlertRecoverCommand.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/query/OperationsHealthAlertQuery.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/query/OperationsHealthQuery.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/query/OperationsHealthTrendQuery.java`

按工作包 1 的 record 转换规则处理。本工作包只改 contract 声明；在下一工作包集中处理其生产与测试调用点。

检查：不运行 Maven；继续执行工作包 4。

### 4. 删除 Command/Query record allowlist（2 个文件）

修改以下文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/OperationsApplicationCommandQueryRecordAllowances.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/OperationsApplicationArchitectureTest.java`

删除 `OperationsApplicationCommandQueryRecordAllowances.java`。在 `OperationsApplicationArchitectureTest.java` 中，将 `assertApplicationCommandQuerySourcesAreRecords` 的第二个参数替换为 `Collections.emptyList()`，并删除对已删除类的引用。不得保留任何 `COMMAND_QUERY_RECORD:` key。

检查：`mvn -pl biz/operations/kuzhambu-operations-application -am test -Dtest=OperationsApplicationArchitectureTest`。

### 5. 消除 application 层构造位置与无参写操作例外（11 个文件）

修改以下文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/service/BackupApplicationService.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/service/impl/BackupApplicationServiceImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/support/OperationsBackupScheduler.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/support/OperationsCleanupScheduler.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/service/BackupSchedulerApplicationService.java`（新增）
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/facade/assembler/OperationsCleanupSchedulerFacadeAssembler.java`（新增）
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/report/controller/OperationsReportAdminController.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/report/assembler/OperationsReportInterfaceAssembler.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/OperationsApplicationArchitectureTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/backup/support/OperationsBackupSchedulerTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/backup/service/impl/BackupApplicationServiceImplTest.java`

执行以下修改：

1. 删除 `BackupApplicationService.executeAutoBackup()` 及其实现。
2. 新增 `BackupSchedulerApplicationService`，由它持有 `BackupApplicationService` 并提供 `executeScheduledBackup()`。该方法调用迁入该类的自动备份专用执行路径，必须保留 `BackupType.AUTO`、空 requester 和 guard 已占用时写入 skipped AUTO record 的现有语义；`OperationsBackupScheduler` 只调用该方法。不得构造空 requester 的 `OperationsBackupExecuteCommand`，也不得调用人工 `execute(command)`。
3. 在 `OperationsCleanupSchedulerFacadeAssembler` 新增静态 `toCommand(CleanupPolicy policy, Instant requestedAt)`：两个引用参数均标注 `@NonNull`，方法开始处对二者执行 `Objects.requireNonNull`，并返回 `new OperationsCleanupExecuteCommand(policy.cleanupType(), null, requestedAt, policy.retentionDays(), policy.limit())`。令 `OperationsCleanupScheduler` 使用该方法的返回值调用 `executeScheduled`。
4. 在 `OperationsReportAdminController` 新增或复用 `OperationsReportInterfaceAssembler` 的 `toQuery` 方法，并以该方法的返回值调用 `download`；Controller 不得直接 `new OperationsReportDetailQuery`。
5. 从 `OperationsApplicationArchitectureTest.java` 删除 `METHOD_SHAPE` 与两条 `COMMAND_QUERY_CONSTRUCTION` allowlist 及其辅助方法；相关断言传入 `Collections.emptyList()`。

检查：`mvn -pl biz/operations/kuzhambu-operations-application,biz/operations/kuzhambu-operations-interface -am test`。

### 6. 使 task、report、restore Assembler 的 application contract 永不为 null（7 个文件）

修改以下文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/task/assembler/OperationsTaskInterfaceAssembler.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/report/assembler/OperationsReportInterfaceAssembler.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/assembler/OperationsRestoreInterfaceAssembler.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/task/controller/OperationsTaskAdminController.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/report/controller/OperationsReportAdminController.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/OperationsRestoreAdminController.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/OperationsApplicationArchitectureTest.java`

对以下全部 public 方法执行同一规则：返回类型和每个引用参数标注 `@NonNull`；方法开始处对每个引用参数执行 `Objects.requireNonNull`；删除所有 `return null` 分支。对 result 转换方法，Controller 必须先保留当前行为：service result 为 `null` 时直接返回 `null`，仅在 result 非 null 时调用 Assembler。对 request 转换方法，Controller 依赖 `@Valid` 的既有必填校验；Assembler 只处理 non-null request：

- `OperationsTaskInterfaceAssembler`：两个 `toQuery`、`toResponse`、`toDetailResponse`。
- `OperationsReportInterfaceAssembler`：`toCommand`、三个 `toQuery`、两个 `toResponse`、`toDetailResponse`。
- `OperationsRestoreInterfaceAssembler`：`toCommand`、两个 `toQuery`、两个 `toResponse`、`toDetailResponse`。

Assembler 始终返回完整的 non-null Command/Query 或 Response。删除 `OperationsApplicationArchitectureTest.java` 的全部八条 `COMMAND_QUERY_ASSEMBLER_NULL_RETURN` allowlist 与 `nullReturn` 辅助方法；断言改为 `Collections.emptyList()`。

检查：`mvn -pl biz/operations/kuzhambu-operations-application,biz/operations/kuzhambu-operations-interface -am test`。

### 7. 规范 Repository 方法名并同步所有 Operations 调用点（10 个文件）

修改以下文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/cleanup/repository/CleanupJobRepository.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/cleanup/repository/impl/CleanupJobRepositoryImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/repository/HealthAlertRepository.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/repository/impl/HealthAlertRepositoryImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthAlertStrategy.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/test/java/com/thundax/kuzhambu/operations/domain/OperationsDomainArchitectureTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/test/java/com/thundax/kuzhambu/operations/infra/health/repository/impl/HealthAlertRepositoryImplTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthAlertStrategyTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/dashboard/service/impl/OperationsDashboardApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/cleanup/service/impl/CleanupApplicationServiceImplTest.java`

执行以下重命名；只修改本工作包列出的 10 个文件中的声明、实现、调用、内存 Repository 实现和测试方法名：

1. `CleanupJobRepository.deleteItemsByJobId` 改为 `deleteByJobId`。
2. `HealthAlertRepository.getOpenBySource` 改为 `findOpenBySource`。
3. 从 `OperationsDomainArchitectureTest.java` 删除 `legacyRepositoryInterfaceMethodNameAllowances` 的两个参数，改为直接调用 `assertRepositoryInterfaceMethodNames(classes, Collections.emptyList())`，并补充 `java.util.Collections` import。

检查：`mvn -pl biz/operations/kuzhambu-operations-domain,biz/operations/kuzhambu-operations-application,biz/operations/kuzhambu-operations-infra -am test`。

### 8. 为 backup、cleanup、dashboard Response 加齐模型注解（8 个文件）

修改以下文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/backup/controller/response/OperationsBackupDetailResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/backup/controller/response/OperationsBackupExecuteResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/backup/controller/response/OperationsBackupPageResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/cleanup/controller/response/OperationsCleanupDetailResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/cleanup/controller/response/OperationsCleanupExecuteResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/cleanup/controller/response/OperationsCleanupPageResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/controller/response/OperationsDashboardOverviewResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/OperationsInterfaceArchitectureTest.java`

为每个顶层 Response 与 `OperationsDashboardOverviewResponse` 的所有嵌套 Response 添加且仅添加 `@Getter`、`@Builder`、`@Schema`、`@JsonInclude`、`@JsonIgnoreProperties`；注解参数沿用本文件现有值或同目录 Response 的既有值。删除这些 12 个类对应的 `legacyResponseAnnotationAllowances` 条目。不得修改响应字段、JSON 名称或泛型。

检查：`mvn -pl biz/operations/kuzhambu-operations-interface -am test -Dtest=OperationsInterfaceArchitectureTest`。

### 9. 为 health、report Response 加齐模型注解（9 个文件）

修改以下文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/response/OperationsHealthAlertPageResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/response/OperationsHealthAlertSummaryResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/response/OperationsHealthPageResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/response/OperationsHealthSummaryResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/response/OperationsHealthTrendResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/report/controller/response/OperationsReportDetailResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/report/controller/response/OperationsReportGenerateResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/report/controller/response/OperationsReportPageResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/OperationsInterfaceArchitectureTest.java`

按工作包 8 的注解规则处理。删除这 8 个顶层类对应的 `legacyResponseAnnotationAllowances` 条目；health Response 不含 allowlist 列出的嵌套 Response，不新增不存在的嵌套类型。

检查：`mvn -pl biz/operations/kuzhambu-operations-interface -am test -Dtest=OperationsInterfaceArchitectureTest`。

### 10. 为 restore、task Response 加齐模型注解并删除 Response allowlist（6 个文件）

修改以下文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/response/OperationsRestoreDetailResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/response/OperationsRestoreExecuteResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/response/OperationsRestorePageResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/task/controller/response/OperationsTaskDetailResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/task/controller/response/OperationsTaskPageResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/OperationsInterfaceArchitectureTest.java`

按工作包 8 的注解规则处理。删除所有剩余 `legacyResponseAnnotationAllowances` 条目与该方法；将 `assertResponseClassAnnotationsRequired` 的 allowlist 参数改为 `Collections.emptyList()`。删除只为该方法保留的 import。

检查：`mvn -pl biz/operations/kuzhambu-operations-interface -am test -Dtest=OperationsInterfaceArchitectureTest`。

### 11. 规范全部 Operations Admin Controller 方法名（9 个文件）

修改以下文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/backup/controller/OperationsBackupAdminController.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/cleanup/controller/OperationsCleanupAdminController.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/controller/OperationsDashboardAdminController.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/OperationsHealthAdminController.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/OperationsHealthAlertAdminController.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/report/controller/OperationsReportAdminController.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/restore/controller/OperationsRestoreAdminController.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/task/controller/OperationsTaskAdminController.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/OperationsInterfaceArchitectureTest.java`
- `kuzhambu-apps/admin-web/e2e/operations/report/report.spec.ts`

执行以下 Java 方法名与 action path 的一一替换；保留 HTTP 方法、参数、返回类型和服务调用不变：

| 文件 | 当前方法 / path | 目标方法 / path |
| --- | --- | --- |
| `OperationsBackupAdminController.java` | `execute` / `execute` | `create` / `create` |
| `OperationsBackupAdminController.java` | `detail` / `detail` | `getDetail` / `get` |
| `OperationsCleanupAdminController.java` | `execute` / `execute` | `create` / `create` |
| `OperationsCleanupAdminController.java` | `detail` / `detail` | `getDetail` / `get` |
| `OperationsDashboardAdminController.java` | `overview` / `overview` | `getOverview` / `get` |
| `OperationsHealthAdminController.java` | `summary` / `summary` | `listSummary` / `list` |
| `OperationsHealthAdminController.java` | `trend` / `trend` | `listTrend` / `list` |
| `OperationsHealthAlertAdminController.java` | `ack` / `ack` | `confirm` / `confirm` |
| `OperationsReportAdminController.java` | `generate` / `generate` | `create` / `create` |
| `OperationsReportAdminController.java` | `detail` / `detail` | `getDetail` / `get` |
| `OperationsRestoreAdminController.java` | `execute` / `execute` | `create` / `create` |
| `OperationsRestoreAdminController.java` | `detail` / `detail` | `getDetail` / `get` |
| `OperationsTaskAdminController.java` | `detail` / `detail` | `getDetail` / `get` |

不修改已经符合白名单的 `page`、`content`、`recover`。删除 `legacyActionVerbAllowances` 与 `actionVerbAllowance`，并让 `assertControllerActionsUseVerbWhitelist` 接收 `Collections.emptyList()`。

将 E2E mock 中的 `/api/operations/report/detail` 改为 `/api/operations/report/get`，`/api/operations/report/generate` 改为 `/api/operations/report/create`。检查：`mvn -pl biz/operations/kuzhambu-operations-interface -am test -Dtest=OperationsInterfaceArchitectureTest`，再执行 `pnpm --dir ../../kuzhambu-apps --filter admin-web exec playwright test e2e/operations/report/report.spec.ts`。

### 12. 删除 Assembler class allowlist 并完成 Operations 测试收口（2 个文件）

修改以下文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/OperationsInterfaceArchitectureTest.java`
- `docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-03-OPERATIONS.md`

在 `OperationsInterfaceArchitectureTest.java` 中，将 `assertBoundaryAssemblerPublicMethodsUseNonNullContracts` 的第二个参数改为 `Collections.emptyList()`，删除 `BoundaryAssemblerNullnessAllowances` import。确认工作包 6 已使三个 Assembler 的所有 public 方法只接受并返回 non-null contract。运行全部验证均通过后，删除本 RUNBOOK。

检查：执行本 RUNBOOK 的“Verification”全部命令。

## Verification

在 `kuzhambu-servers/` 下按以下顺序执行，任一命令失败即停止并修复失败项后重跑该命令：

1. `mvn -pl biz/operations/kuzhambu-operations-application,biz/operations/kuzhambu-operations-domain,biz/operations/kuzhambu-operations-interface,biz/operations/kuzhambu-operations-infra -am test`
2. `mvn -pl biz/operations/kuzhambu-operations-application,biz/operations/kuzhambu-operations-domain,biz/operations/kuzhambu-operations-interface,biz/operations/kuzhambu-operations-infra spotless:apply`
3. `git diff --check`
4. `mvn spotless:check`
5. `mvn checkstyle:check`
6. `git status --short`
7. `rg -n 'COMMAND_QUERY_RECORD:|METHOD_SHAPE:.*BackupApplicationService\.executeAutoBackup|COMMAND_QUERY_CONSTRUCTION:|COMMAND_QUERY_ASSEMBLER_NULL_RETURN:|legacyRepositoryInterfaceMethodNameAllowances\(|legacyResponseAnnotationAllowances|legacyActionVerbAllowances|BoundaryAssemblerNullnessAllowances' biz/operations`

第 7 步必须无输出；否则不得删除 RUNBOOK。

## Closure

当 Verification 全部通过且第 7 步无输出时，删除本 RUNBOOK。提交前确认 `git diff --check` 无输出，且变更仅位于 `kuzhambu-servers/biz/operations/` 与本 RUNBOOK；RUNBOOK 删除后不应存在任何指向它的引用。
