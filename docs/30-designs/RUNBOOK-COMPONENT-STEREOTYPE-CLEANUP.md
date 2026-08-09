# RUNBOOK Component Stereotype Cleanup

## Purpose

本 RUNBOOK 指导执行者清理 `kuzhambu-servers/` 中不合理或不精确的 Spring `@Component` 使用。

执行者必须按本文逐文件修改。不要自行扩大范围。不要把本文标记为“保留”的类改成其他注解。

本次清理目标：

- 将 8 个配置属性类从 `@Component` + `@Value` 改为 `@ConfigurationProperties` + `@EnableConfigurationProperties`。
- 将 1 个应用服务执行类从 `@Component` 改为 `@Service`。
- 保留其余 88 个 `@Component` 使用点，并记录保留原因。

## Scope

纳入范围：

- `kuzhambu-servers/` 生产代码中所有直接使用 `org.springframework.stereotype.Component` 的 Java 类。
- 因移动配置属性类而必须同步修改的生产代码 import。
- 因移动配置属性类而必须同步修改的测试代码 import。
- 为注册配置属性类而新增或修改的 `*Configuration` 类。

当前盘点结果：

- 没有发现 `@Components` 复数注解。
- 共有 97 个生产 Java 文件包含 `@Component`。
- 本 RUNBOOK 的 `Component Inventory` 必须覆盖这 97 个文件。

## Non-goals

- 不改变任何配置 key。
- 不改变业务行为、接口契约、数据库结构、消息协议或调度表达式。
- 不迁移、不重命名、不重构已标记为“保留”的 88 个类。
- 不改 `@RestController`、`@Repository`、MyBatis `Mapper` 或前端代码。
- 不删除本 RUNBOOK，直到全部代码清理和验证完成。

## Plan

### Step 0: 执行前检查

1. 在仓库根目录执行：

```sh
git status --short
```

2. 如果有用户未提交改动，不要回滚。只修改本文明确列出的文件。

3. 执行以下命令确认当前 `@Component` 数量：

```sh
rg -l '@Component' kuzhambu-servers -g '*.java' | sort | wc -l
```

期望输出为 `97`。如果不是 `97`，先重新盘点，不要直接执行后续步骤。

### Step 1: 修改 `AiWorkerGatewayProperties`

#### 1.1 移动并改写配置类

源文件：

`kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/invocation/gateway/AiWorkerGatewayProperties.java`

目标文件：

`kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/invocation/configure/AiWorkerGatewayProperties.java`

类名保持：

`AiWorkerGatewayProperties`

包名改为：

`com.thundax.kuzhambu.ai.infra.invocation.configure`

注解修改：

- 删除 `@Component`。
- 删除所有字段上的 `@Value`。
- 添加 `@ConfigurationProperties(prefix = "kuzhambu.ai.worker")`。
- 保留 `@Getter` 和 `@Setter`。

字段和默认值必须为：

| Field | Type | Default |
| --- | --- | --- |
| `baseUrl` | `String` | `"http://localhost:8000"` |
| `internalSecret` | `String` | `""` |
| `serviceName` | `String` | `"kuzhambu-ai"` |
| `timeoutMs` | `long` | `60000L` |
| `maxArtifactSizeBytes` | `long` | `52428800L` |

#### 1.2 新增配置注册类

新增文件：

`kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/invocation/configure/AiWorkerGatewayConfiguration.java`

类名：

`AiWorkerGatewayConfiguration`

包名：

`com.thundax.kuzhambu.ai.infra.invocation.configure`

注解：

- `@Configuration`
- `@EnableConfigurationProperties(AiWorkerGatewayProperties.class)`

该类不需要字段、构造器或业务方法。

#### 1.3 更新引用

更新以下文件的 import：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/invocation/gateway/AiWorkerHttpGateway.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/test/java/com/thundax/kuzhambu/ai/infra/invocation/gateway/AiWorkerHttpGatewayTest.java`

将 `AiWorkerGatewayProperties` 的 import 指向：

`com.thundax.kuzhambu.ai.infra.invocation.configure.AiWorkerGatewayProperties`

### Step 2: 修改 `WorkerRenderProperties`

#### 2.1 移动并改写配置类

源文件：

`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/client/WorkerRenderProperties.java`

目标文件：

`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/configure/WorkerRenderProperties.java`

类名保持：

`WorkerRenderProperties`

包名改为：

`com.thundax.kuzhambu.classics.infra.configure`

注解修改：

- 删除 `@Component`。
- 删除所有字段上的 `@Value`。
- 添加 `@ConfigurationProperties(prefix = "kuzhambu.classics.worker")`。
- 保留 `@Getter` 和 `@Setter`。

字段和默认值必须为：

| Field | Type | Default |
| --- | --- | --- |
| `baseUrl` | `String` | `"http://localhost:8000"` |
| `internalSecret` | `String` | `""` |
| `serviceName` | `String` | `"kuzhambu-classics"` |
| `timeoutMs` | `long` | `60000L` |

#### 2.2 新增配置注册类

新增文件：

`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/configure/WorkerRenderConfiguration.java`

类名：

`WorkerRenderConfiguration`

包名：

`com.thundax.kuzhambu.classics.infra.configure`

注解：

- `@Configuration`
- `@EnableConfigurationProperties(WorkerRenderProperties.class)`

#### 2.3 更新引用

更新以下文件的 import：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/client/WorkerRenderHttpClient.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/test/java/com/thundax/kuzhambu/classics/infra/client/WorkerRenderHttpClientTest.java`

将 `WorkerRenderProperties` 的 import 指向：

`com.thundax.kuzhambu.classics.infra.configure.WorkerRenderProperties`

### Step 3: 修改 `OperationsWorkerRenderProperties`

#### 3.1 移动并改写配置类

源文件：

`kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/client/OperationsWorkerRenderProperties.java`

目标文件：

`kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/configure/OperationsWorkerRenderProperties.java`

类名保持：

`OperationsWorkerRenderProperties`

包名改为：

`com.thundax.kuzhambu.operations.infra.report.configure`

注解修改：

- 删除 `@Component`。
- 删除所有字段上的 `@Value`。
- 添加 `@ConfigurationProperties(prefix = "kuzhambu.operations.worker")`。
- 保留 `@Getter` 和 `@Setter`。

字段和默认值必须为：

| Field | Type | Default |
| --- | --- | --- |
| `baseUrl` | `String` | `"http://localhost:8000"` |
| `internalSecret` | `String` | `""` |
| `serviceName` | `String` | `"kuzhambu-operations"` |
| `timeoutMs` | `long` | `60000L` |

#### 3.2 新增配置注册类

新增文件：

`kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/configure/OperationsWorkerRenderConfiguration.java`

类名：

`OperationsWorkerRenderConfiguration`

包名：

`com.thundax.kuzhambu.operations.infra.report.configure`

注解：

- `@Configuration`
- `@EnableConfigurationProperties(OperationsWorkerRenderProperties.class)`

#### 3.3 更新引用

更新以下文件的 import：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/client/OperationsWorkerRenderHttpClient.java`

将 `OperationsWorkerRenderProperties` 的 import 指向：

`com.thundax.kuzhambu.operations.infra.report.configure.OperationsWorkerRenderProperties`

### Step 4: 修改 operations backup 配置类

#### 4.1 移动并改写 `OperationsBackupScheduleProperties`

源文件：

`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/support/OperationsBackupScheduleProperties.java`

目标文件：

`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/configure/OperationsBackupScheduleProperties.java`

包名改为：

`com.thundax.kuzhambu.operations.application.backup.configure`

注解修改：

- 删除 `@Component`。
- 删除所有字段上的 `@Value`。
- 添加 `@ConfigurationProperties(prefix = "kuzhambu.operations.backup.schedule")`。
- 保留 `@Getter` 和 `@Setter`。

字段和默认值必须为：

| Field | Type | Default |
| --- | --- | --- |
| `enabled` | `boolean` | `true` |
| `startupEnabled` | `boolean` | `true` |
| `dailyCron` | `String` | `"0 0 2 * * ?"` |

#### 4.2 移动并改写 `OperationsBackupScriptProperties`

源文件：

`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/support/OperationsBackupScriptProperties.java`

目标文件：

`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/configure/OperationsBackupScriptProperties.java`

包名改为：

`com.thundax.kuzhambu.operations.application.backup.configure`

注解修改：

- 删除 `@Component`。
- 删除 `scriptsRoot`、`backupScriptName`、`restoreScriptName`、`cleanupScriptName`、`backupRootPath`、`commandTimeoutMs`、`runPreRestore`、`postRestoreCommand` 字段上的 `@Value`。
- 保留 `datasourceUrl`、`datasourceUsername`、`datasourcePassword` 字段上的 `@Value`，因为这三个字段绑定 `spring.datasource.*`，不属于 `kuzhambu.operations.backup` 前缀。
- 添加 `@ConfigurationProperties(prefix = "kuzhambu.operations.backup")`。
- 保留 `@Getter` 和 `@Setter`。
- 保留 `resolveMysqlConnectionSettings()` 方法。
- 保留嵌套 record `MysqlConnectionSettings`。

字段和默认值必须为：

| Field | Type | Bound property | Default |
| --- | --- | --- | --- |
| `scriptsRoot` | `String` | `scripts-root` | `"/app/ops-scripts"` |
| `backupScriptName` | `String` | `backup-script-name` | `"backup-business-data.sh"` |
| `restoreScriptName` | `String` | `restore-script-name` | `"restore-business-data.sh"` |
| `cleanupScriptName` | `String` | `cleanup-script-name` | `"cleanup-backups.sh"` |
| `rootPath` | `String` | `root-path` | `System.getenv().getOrDefault("KUZHAMBU_BACKUP_ROOT_PATH", "/backup/kuzhambu")` |
| `commandTimeoutMs` | `long` | `command-timeout-ms` | `1800000L` |
| `runPreRestore` | `boolean` | `run-pre-restore` | `true` |
| `postRestoreCommand` | `String` | `post-restore-command` | `""` |
| `datasourceUrl` | `String` | do not bind from prefix | no inline default |
| `datasourceUsername` | `String` | do not bind from prefix | no inline default |
| `datasourcePassword` | `String` | do not bind from prefix | `""` |

Special handling for datasource fields:

- Keep `datasourceUrl`, `datasourceUsername`, and `datasourcePassword` in this class.
- Because their keys are outside prefix `kuzhambu.operations.backup`, keep `@Value` only for these three datasource fields.
- Do not bind datasource fields as `kuzhambu.operations.backup.datasource-url`.
- Do not rename existing Spring datasource keys.

Important compatibility rule:

- Existing callers may use `getBackupRootPath()` and `setBackupRootPath(...)`.
- If the field is renamed to `rootPath`, preserve `getBackupRootPath()` and `setBackupRootPath(String)` methods that delegate to `rootPath`.
- Use field name `rootPath` for binding property `kuzhambu.operations.backup.root-path`.
- Add manual methods `getBackupRootPath()` and `setBackupRootPath(String backupRootPath)` for compatibility with existing tests and callers.

#### 4.3 新增配置注册类

新增文件：

`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/configure/OperationsBackupConfiguration.java`

类名：

`OperationsBackupConfiguration`

包名：

`com.thundax.kuzhambu.operations.application.backup.configure`

注解：

- `@Configuration`
- `@EnableConfigurationProperties({OperationsBackupScheduleProperties.class, OperationsBackupScriptProperties.class})`

#### 4.4 更新引用

更新以下文件的 import：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/support/OperationsBackupScheduler.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/support/DefaultOperationsBackupScriptExecutor.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/backup/support/OperationsBackupSchedulerTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/backup/support/DefaultOperationsBackupScriptExecutorTest.java`

Nested record import update:

- Replace `com.thundax.kuzhambu.operations.application.backup.support.OperationsBackupScriptProperties.MysqlConnectionSettings`.
- Use `com.thundax.kuzhambu.operations.application.backup.configure.OperationsBackupScriptProperties.MysqlConnectionSettings`.

### Step 5: 修改 `OperationsCleanupScheduleProperties`

#### 5.1 移动并改写配置类

源文件：

`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/support/OperationsCleanupScheduleProperties.java`

目标文件：

`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/configure/OperationsCleanupScheduleProperties.java`

包名改为：

`com.thundax.kuzhambu.operations.application.cleanup.configure`

注解修改：

- 删除 `@Component`。
- 删除所有字段上的 `@Value`。
- 添加 `@ConfigurationProperties(prefix = "kuzhambu.operations.cleanup.schedule")`。
- 保留 `@Getter` 和 `@Setter`。
- 保留 `orderedPolicies()` 方法。
- 保留 `policyFor(String cleanupType)` 方法。
- 保留私有方法 `effectiveLimit(Integer policyLimit)`。
- 保留嵌套 record `CleanupPolicy`。

字段和默认值必须保持现有语义：

| Field | Type | Default |
| --- | --- | --- |
| `enabled` | `boolean` | `true` |
| `startupEnabled` | `boolean` | `false` |
| `dailyCron` | `String` | `"0 30 3 * * ?"` |
| `defaultLimit` | `int` | `200` |
| `expiredBackupEnabled` | `boolean` | `true` |
| `expiredBackupRetentionDays` | `int` | `30` |
| `expiredBackupLimit` | `Integer` | `200` |
| `expiredExportEnabled` | `boolean` | `true` |
| `expiredExportRetentionDays` | `int` | `7` |
| `expiredExportLimit` | `Integer` | `200` |
| `expiredShareEnabled` | `boolean` | `true` |
| `expiredShareRetentionDays` | `int` | `90` |
| `expiredShareLimit` | `Integer` | `200` |
| `expiredDraftEnabled` | `boolean` | `true` |
| `expiredDraftRetentionDays` | `int` | `30` |
| `expiredDraftLimit` | `Integer` | `200` |
| `expiredReportEnabled` | `boolean` | `true` |
| `expiredReportRetentionDays` | `int` | `90` |
| `expiredReportLimit` | `Integer` | `200` |
| `expiredHealthCheckEnabled` | `boolean` | `true` |
| `expiredHealthCheckRetentionDays` | `int` | `30` |
| `expiredHealthCheckLimit` | `Integer` | `500` |
| `expiredLongTaskEnabled` | `boolean` | `true` |
| `expiredLongTaskRetentionDays` | `int` | `90` |
| `expiredLongTaskLimit` | `Integer` | `200` |

#### 5.2 新增配置注册类

新增文件：

`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/configure/OperationsCleanupConfiguration.java`

类名：

`OperationsCleanupConfiguration`

包名：

`com.thundax.kuzhambu.operations.application.cleanup.configure`

注解：

- `@Configuration`
- `@EnableConfigurationProperties(OperationsCleanupScheduleProperties.class)`

#### 5.3 更新引用

更新以下文件的 import：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/support/OperationsCleanupScheduler.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/cleanup/support/OperationsCleanupSchedulerTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/cleanup/support/OperationsCleanupSchedulePropertiesTest.java`

Nested record references:

- Replace same-package references to `OperationsCleanupScheduleProperties.CleanupPolicy` with an import from `com.thundax.kuzhambu.operations.application.cleanup.configure.OperationsCleanupScheduleProperties`.

### Step 6: 修改 `OperationsHealthAlertPolicyProperties`

#### 6.1 移动并改写配置类

源文件：

`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthAlertPolicyProperties.java`

目标文件：

`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/configure/OperationsHealthAlertPolicyProperties.java`

包名改为：

`com.thundax.kuzhambu.operations.application.health.configure`

注解修改：

- 删除 `@Component`。
- 删除所有字段上的 `@Value`。
- 添加 `@ConfigurationProperties(prefix = "kuzhambu.operations.health.alert")`。
- 保留 `@Getter` 和 `@Setter`。

字段和默认值必须为：

| Field | Type | Default |
| --- | --- | --- |
| `degradedThreshold` | `int` | `3` |
| `recoveryUpThreshold` | `int` | `2` |
| `staleMinutes` | `int` | `10` |
| `writeBlockStaleMinutes` | `int` | `30` |

#### 6.2 修改现有配置注册类

修改文件：

`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/configure/OperationsExternalHealthProbeConfiguration.java`

当前注解：

`@EnableConfigurationProperties(OperationsExternalHealthProbeProperties.class)`

目标注解：

`@EnableConfigurationProperties({OperationsExternalHealthProbeProperties.class, OperationsHealthAlertPolicyProperties.class})`

不要新增第二个 health configuration 类。

#### 6.3 更新引用

更新以下文件的 import：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthAlertStrategy.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthAlertStrategyTest.java`

将 `OperationsHealthAlertPolicyProperties` 的 import 指向：

`com.thundax.kuzhambu.operations.application.health.configure.OperationsHealthAlertPolicyProperties`

### Step 7: 修改 `ClassicsPublicationProperties`

修改文件：

`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/publication/configure/ClassicsPublicationProperties.java`

执行：

- 删除 import `org.springframework.stereotype.Component`。
- 删除 `@Component`。
- 保留 `@ConfigurationProperties(prefix = "kuzhambu.classics.publication")`。
- 不移动文件。
- 不改字段、默认值、getter 或 setter。

修改文件：

`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/publication/configure/ClassicsPublicationExecutorConfiguration.java`

执行：

- 添加 import `org.springframework.boot.context.properties.EnableConfigurationProperties`。
- 在类上添加 `@EnableConfigurationProperties(ClassicsPublicationProperties.class)`。
- 保留已有 `@Configuration`。
- 不改 bean 名称 `classicsPublicationTaskExecutor`。
- 不改 `TASK_EXECUTOR` 常量。

### Step 8: 修改 `ClassicsPublicationStepExecutorImpl`

修改文件：

`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/publication/service/impl/ClassicsPublicationStepExecutorImpl.java`

执行：

- 删除 import `org.springframework.stereotype.Component`。
- 添加 import `org.springframework.stereotype.Service`。
- 将类注解 `@Component` 改为 `@Service`。
- 不移动文件。
- 不改类名、接口、构造器或业务方法。

## Component Inventory

执行者只能修改本节 `Action` 为 `Change` 的类。`Action` 为 `Keep` 的类不得修改 stereotype。

| Action | File | Class | Instruction |
| --- | --- | --- | --- |
| Change | `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/invocation/gateway/AiWorkerGatewayProperties.java` | `AiWorkerGatewayProperties` | 按 Step 1 移动并改为配置属性类 |
| Change | `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/client/WorkerRenderProperties.java` | `WorkerRenderProperties` | 按 Step 2 移动并改为配置属性类 |
| Change | `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/client/OperationsWorkerRenderProperties.java` | `OperationsWorkerRenderProperties` | 按 Step 3 移动并改为配置属性类 |
| Change | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/support/OperationsBackupScheduleProperties.java` | `OperationsBackupScheduleProperties` | 按 Step 4 移动并改为配置属性类 |
| Change | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/support/OperationsBackupScriptProperties.java` | `OperationsBackupScriptProperties` | 按 Step 4 移动并改为配置属性类 |
| Change | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/support/OperationsCleanupScheduleProperties.java` | `OperationsCleanupScheduleProperties` | 按 Step 5 移动并改为配置属性类 |
| Change | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthAlertPolicyProperties.java` | `OperationsHealthAlertPolicyProperties` | 按 Step 6 移动并改为配置属性类 |
| Change | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/publication/configure/ClassicsPublicationProperties.java` | `ClassicsPublicationProperties` | 按 Step 7 删除 `@Component` 并由 configuration 注册 |
| Change | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/publication/service/impl/ClassicsPublicationStepExecutorImpl.java` | `ClassicsPublicationStepExecutorImpl` | 按 Step 8 改为 `@Service` |
| Keep | `kuzhambu-servers/common/kuzhambu-common-audit/src/main/java/com/thundax/kuzhambu/common/audit/runtime/AuditObjectLoaderRegistry.java` | `AuditObjectLoaderRegistry` | registry bean，保留 |
| Keep | `kuzhambu-servers/common/kuzhambu-common-audit/src/main/java/com/thundax/kuzhambu/common/audit/runtime/AuditSnapshotAssemblerRegistry.java` | `AuditSnapshotAssemblerRegistry` | registry bean，保留 |
| Keep | `kuzhambu-servers/common/kuzhambu-common-core/src/main/java/com/thundax/kuzhambu/common/core/exception/BizExceptionBoundaryAspect.java` | `BizExceptionBoundaryAspect` | `@Aspect` bean，保留 |
| Keep | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/auth/security/CurrentUserResolver.java` | `CurrentUserResolver` | interface support bean，保留 |
| Keep | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/mq/RocketMqSysLogConsumer.java` | `RocketMqSysLogConsumer` | RocketMQ listener bean，保留 |
| Keep | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/support/AdminAvatarUrlBuilder.java` | `AdminAvatarUrlBuilder` | interface support bean，保留 |
| Keep | `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/scenario/support/PlatformAiWorkerUsecaseResolver.java` | `PlatformAiWorkerUsecaseResolver` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/scenario/support/ClassicsAiWorkerUsecaseResolver.java` | `ClassicsAiWorkerUsecaseResolver` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/scenario/support/KnowledgeAiWorkerUsecaseResolver.java` | `KnowledgeAiWorkerUsecaseResolver` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/scenario/support/DiscoveryAiWorkerUsecaseResolver.java` | `DiscoveryAiWorkerUsecaseResolver` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/facade/assembler/AiFacadeAssembler.java` | `AiFacadeAssembler` | facade assembler bean，保留 |
| Keep | `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/support/AiWorkerModelConfigResolver.java` | `AiWorkerModelConfigResolver` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/support/AiBusinessInvokeConfigResolver.java` | `AiBusinessInvokeConfigResolver` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/core/cache/DepartmentCacheSupport.java` | `DepartmentCacheSupport` | infra cache bean，保留 |
| Keep | `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/core/cache/RoleCacheSupport.java` | `RoleCacheSupport` | infra cache bean，保留 |
| Keep | `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/core/cache/UserCacheSupport.java` | `UserCacheSupport` | infra cache bean，保留 |
| Keep | `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/core/cache/DictCacheSupport.java` | `DictCacheSupport` | infra cache bean，保留 |
| Keep | `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/core/cache/MenuCacheSupport.java` | `MenuCacheSupport` | infra cache bean，保留 |
| Keep | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/AuditLogAspect.java` | `AuditLogAspect` | `@Aspect` bean，保留 |
| Keep | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/DepartmentAuditSnapshotAssembler.java` | `DepartmentAuditSnapshotAssembler` | audit extension bean，保留 |
| Keep | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/RoleAuditSnapshotAssembler.java` | `RoleAuditSnapshotAssembler` | audit extension bean，保留 |
| Keep | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/MenuAuditObjectLoader.java` | `MenuAuditObjectLoader` | audit extension bean，保留 |
| Keep | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/UserAuditObjectLoader.java` | `UserAuditObjectLoader` | audit extension bean，保留 |
| Keep | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/MenuAuditSnapshotAssembler.java` | `MenuAuditSnapshotAssembler` | audit extension bean，保留 |
| Keep | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/RoleAuditObjectLoader.java` | `RoleAuditObjectLoader` | audit extension bean，保留 |
| Keep | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/UserAuditSnapshotAssembler.java` | `UserAuditSnapshotAssembler` | audit extension bean，保留 |
| Keep | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/DictAuditSnapshotAssembler.java` | `DictAuditSnapshotAssembler` | audit extension bean，保留 |
| Keep | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/DepartmentAuditObjectLoader.java` | `DepartmentAuditObjectLoader` | audit extension bean，保留 |
| Keep | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/DictAuditObjectLoader.java` | `DictAuditObjectLoader` | audit extension bean，保留 |
| Keep | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/AuditObjectLoaderRegistry.java` | `AuditObjectLoaderRegistry` | named registry bean，保留 |
| Keep | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/AuditSnapshotAssemblerRegistry.java` | `AuditSnapshotAssemblerRegistry` | named registry bean，保留 |
| Keep | `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/invocation/gateway/AiWorkerRequestSigner.java` | `AiWorkerRequestSigner` | infra support bean，保留 |
| Keep | `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/invocation/gateway/AiWorkerHttpGateway.java` | `AiWorkerHttpGateway` | infra gateway bean，保留 |
| Keep | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/support/KnowledgeGraphCandidateApplySupport.java` | `KnowledgeGraphCandidateApplySupport` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/workbench/support/KnowledgeGraphManuscriptTreeAssembler.java` | `KnowledgeGraphManuscriptTreeAssembler` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/workbench/support/KnowledgeGraphManuscriptPayloadBuilder.java` | `KnowledgeGraphManuscriptPayloadBuilder` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/cache/StorageCacheSupport.java` | `StorageCacheSupport` | infra cache bean，保留 |
| Keep | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/support/QualitySummaryAggregationSupport.java` | `QualitySummaryAggregationSupport` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/support/RefinementApplySupport.java` | `RefinementApplySupport` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/support/RefinementDraftBootstrapSupport.java` | `RefinementDraftBootstrapSupport` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/support/KnowledgeRefinementManualKeySupport.java` | `KnowledgeRefinementManualKeySupport` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/facade/assembler/KnowledgeFacadeAssembler.java` | `KnowledgeFacadeAssembler` | facade assembler bean，保留 |
| Keep | `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/facade/assembler/StorageOwnerBindingFacadeAssembler.java` | `StorageOwnerBindingFacadeAssembler` | facade assembler bean，保留 |
| Keep | `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/facade/assembler/StorageReadableContentFacadeAssembler.java` | `StorageReadableContentFacadeAssembler` | facade assembler bean，保留 |
| Keep | `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/facade/assembler/StorageUploadFacadeAssembler.java` | `StorageUploadFacadeAssembler` | facade assembler bean，保留 |
| Keep | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/support/OperationsRestoreWriteBlocker.java` | `OperationsRestoreWriteBlocker` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/support/DefaultOperationsBackupScriptExecutor.java` | `DefaultOperationsBackupScriptExecutor` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/support/OperationsBackupScheduler.java` | `OperationsBackupScheduler` | scheduler bean，保留 |
| Keep | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/support/OperationsBackupExecutionGuard.java` | `OperationsBackupExecutionGuard` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/support/OperationsCleanupScheduler.java` | `OperationsCleanupScheduler` | scheduler bean，保留 |
| Keep | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGateway.java` | `DefaultOperationsReportMetricsGateway` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/OperationsReportSnapshotAssembler.java` | `OperationsReportSnapshotAssembler` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportTaskExecutor.java` | `DefaultOperationsReportTaskExecutor` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/support/DefaultOperationsDashboardSummaryGateway.java` | `DefaultOperationsDashboardSummaryGateway` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/support/OperationsDashboardPermissionResolver.java` | `OperationsDashboardPermissionResolver` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthAlertStrategy.java` | `OperationsHealthAlertStrategy` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthCollector.java` | `OperationsHealthCollector` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/LocalOperationsHealthProbe.java` | `LocalOperationsHealthProbe` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthRecoveryLinkFactory.java` | `OperationsHealthRecoveryLinkFactory` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/client/OperationsWorkerRenderHttpClient.java` | `OperationsWorkerRenderHttpClient` | infra client bean，保留 |
| Keep | `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/client/OperationsWorkerRenderSignatureSupport.java` | `OperationsWorkerRenderSignatureSupport` | infra support bean，保留 |
| Keep | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/ClassicsSearchContentProvider.java` | `ClassicsSearchContentProvider` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/DiscoveryKnowledgeEnhancementProvider.java` | `DiscoveryKnowledgeEnhancementProvider` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/DefaultSearchPermissionFilter.java` | `DefaultSearchPermissionFilter` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/QueryUnderstandingPayloadBuilder.java` | `QueryUnderstandingPayloadBuilder` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/client/ElasticsearchSearchIndexGateway.java` | `ElasticsearchSearchIndexGateway` | infra gateway bean，保留 |
| Keep | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/client/DiscoverySearchDocumentAssembler.java` | `DiscoverySearchDocumentAssembler` | infra assembler bean，保留 |
| Keep | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeItemTextRenderer.java` | `KnowledgeItemTextRenderer` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeRevisionCalculator.java` | `KnowledgeRevisionCalculator` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaSessionCsvExporter.java` | `QaSessionCsvExporter` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaSourceAssembler.java` | `QaSourceAssembler` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeSourceResolver.java` | `KnowledgeSourceResolver` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/facade/assembler/DiscoverySearchPublicationFacadeAssembler.java` | `DiscoverySearchPublicationFacadeAssembler` | facade assembler bean，保留 |
| Keep | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaTraceAssembler.java` | `QaTraceAssembler` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeDocumentAssembler.java` | `KnowledgeDocumentAssembler` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/facade/assembler/DiscoveryFacadeAssembler.java` | `DiscoveryFacadeAssembler` | facade assembler bean，保留 |
| Keep | `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/client/WorkerRenderSignatureSupport.java` | `WorkerRenderSignatureSupport` | infra support bean，保留 |
| Keep | `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/client/WorkerRenderHttpClient.java` | `WorkerRenderHttpClient` | infra client bean，保留 |
| Keep | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/publication/support/ClassicsPublicationPayloadAssembler.java` | `ClassicsPublicationPayloadAssembler` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/publication/support/ClassicsPublicationWriteGuard.java` | `ClassicsPublicationWriteGuard` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/publication/support/ClassicsPublicationFastGptGateway.java` | `ClassicsPublicationFastGptGateway` | application support bean，保留 |
| Keep | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/publication/scheduler/ClassicsPublicationEsCleanupScheduler.java` | `ClassicsPublicationEsCleanupScheduler` | scheduler bean，保留 |
| Keep | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/publication/scheduler/ClassicsPublicationDispatchScheduler.java` | `ClassicsPublicationDispatchScheduler` | scheduler bean，保留 |
| Keep | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/publication/scheduler/ClassicsPublicationSuccessReconcileScheduler.java` | `ClassicsPublicationSuccessReconcileScheduler` | scheduler bean，保留 |
| Keep | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/publication/scheduler/ClassicsPublicationFailureReconcileScheduler.java` | `ClassicsPublicationFailureReconcileScheduler` | scheduler bean，保留 |
| Keep | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/publication/scheduler/ClassicsPublicationFastGptCleanupScheduler.java` | `ClassicsPublicationFastGptCleanupScheduler` | scheduler bean，保留 |
| Keep | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/facade/assembler/ClassicsFacadeAssembler.java` | `ClassicsFacadeAssembler` | facade assembler bean，保留 |
| Keep | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsTagBindingSupport.java` | `ClassicsTagBindingSupport` | application support bean，保留 |

## Verification

### Required static checks

执行：

```sh
rg -n '@Component' kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/invocation/configure kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/configure kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/configure kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/configure kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/configure kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/configure
```

期望：

- 没有输出。

执行：

```sh
rg -n '@ConfigurationProperties|@EnableConfigurationProperties|class .*Properties|class .*Configuration' \
  kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/invocation/configure \
  kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/configure \
  kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/publication/configure \
  kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/configure \
  kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/configure \
  kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/configure \
  kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/configure
```

期望：

- 每个 moved `*Properties` 类都有 `@ConfigurationProperties`。
- 每个新增或修改的 `*Configuration` 类都有 `@EnableConfigurationProperties`。
- `ClassicsPublicationProperties` 没有 `@Component`。

执行：

```sh
rg -n '@Service|@Component' kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/publication/service/impl/ClassicsPublicationStepExecutorImpl.java
```

期望：

- 有 `@Service`。
- 没有 `@Component`。

### Required formatting and static validation

先运行涉及模块 formatter：

```sh
cd kuzhambu-servers
mvn -pl biz/ai/kuzhambu-ai-infra,biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-infra,biz/operations/kuzhambu-operations-application,biz/operations/kuzhambu-operations-infra spotless:apply
```

再运行：

```sh
cd kuzhambu-servers
mvn spotless:check
mvn checkstyle:check
```

### Required tests

执行：

```sh
cd kuzhambu-servers
mvn -pl biz/ai/kuzhambu-ai-infra,biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-infra,biz/operations/kuzhambu-operations-application,biz/operations/kuzhambu-operations-infra -am test
```

### Required diff review

执行：

```sh
git diff -- docs/30-designs/RUNBOOK-COMPONENT-STEREOTYPE-CLEANUP.md kuzhambu-servers
```

检查：

- 只能包含本 RUNBOOK 明确列出的 `Change` 文件、必要新增 configuration 文件、必要 import 更新和测试 import 更新。
- 不能包含业务逻辑重构。
- 不能包含配置 key 改名。
- 不能包含数据库、前端或 worker 改动。

## Closure

全部代码修改、格式化、静态检查和测试完成后，删除本 RUNBOOK。

如果执行过程中需要沉淀长期规则，先更新 `docs/00-governance/SERVERS-ARCHITECTURE.md` 或 `docs/00-governance/SERVERS-ARCHITECTURE-RULES.md`，再删除本 RUNBOOK。
