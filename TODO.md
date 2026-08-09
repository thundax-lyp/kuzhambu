# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 当前任务项按数字编号顺序执行；不得跳过前置契约任务直接做后续调用方或重命名任务。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `classics-infra worker render properties`：迁移 classics render worker 配置属性注册
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-COMPONENT-STEREOTYPE-CLEANUP.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/client/WorkerRenderProperties.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/configure/WorkerRenderProperties.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/configure/WorkerRenderConfiguration.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/client/WorkerRenderHttpClient.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/test/java/com/thundax/kuzhambu/classics/infra/client/WorkerRenderHttpClientTest.java`
    - 处理动作：按 RUNBOOK Step 2 将 `WorkerRenderProperties` 移入 `classics.infra.configure` 并改为 `@ConfigurationProperties` 注册
    - 验收点：`WorkerRenderProperties` 不再使用 `@Component` 或字段级 `@Value`，`WorkerRenderConfiguration` 使用 `@EnableConfigurationProperties(WorkerRenderProperties.class)`，生产和测试 import 指向新包
    - 重要度：8/10

- [ ] `operations-infra worker render properties`：迁移 operations render worker 配置属性注册
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-COMPONENT-STEREOTYPE-CLEANUP.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/client/OperationsWorkerRenderProperties.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/configure/OperationsWorkerRenderProperties.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/configure/OperationsWorkerRenderConfiguration.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/client/OperationsWorkerRenderHttpClient.java`
    - 处理动作：按 RUNBOOK Step 3 将 `OperationsWorkerRenderProperties` 移入 `operations.infra.report.configure` 并改为 `@ConfigurationProperties` 注册
    - 验收点：`OperationsWorkerRenderProperties` 不再使用 `@Component` 或字段级 `@Value`，`OperationsWorkerRenderConfiguration` 使用 `@EnableConfigurationProperties(OperationsWorkerRenderProperties.class)`，生产 import 指向新包
    - 重要度：8/10

- [ ] `operations backup properties`：迁移 operations backup 配置属性注册
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-COMPONENT-STEREOTYPE-CLEANUP.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/support/OperationsBackupScheduleProperties.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/support/OperationsBackupScriptProperties.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/configure/OperationsBackupScheduleProperties.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/configure/OperationsBackupScriptProperties.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/configure/OperationsBackupConfiguration.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/support/OperationsBackupScheduler.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/support/DefaultOperationsBackupScriptExecutor.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/backup/support/OperationsBackupSchedulerTest.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/backup/support/DefaultOperationsBackupScriptExecutorTest.java`
    - 处理动作：按 RUNBOOK Step 4 将 backup schedule/script 配置类移入 `operations.application.backup.configure` 并改为 `@ConfigurationProperties` 注册
    - 验收点：两个 backup `*Properties` 类不再使用 `@Component`，`OperationsBackupConfiguration` 注册两个配置类，datasource 三个字段仍绑定 `spring.datasource.*`，所有生产和测试 import 指向新包
    - 重要度：9/10

- [ ] `operations cleanup properties`：迁移 operations cleanup 配置属性注册
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-COMPONENT-STEREOTYPE-CLEANUP.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/support/OperationsCleanupScheduleProperties.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/configure/OperationsCleanupScheduleProperties.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/configure/OperationsCleanupConfiguration.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/support/OperationsCleanupScheduler.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/cleanup/support/OperationsCleanupSchedulerTest.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/cleanup/support/OperationsCleanupSchedulePropertiesTest.java`
    - 处理动作：按 RUNBOOK Step 5 将 `OperationsCleanupScheduleProperties` 移入 `operations.application.cleanup.configure` 并改为 `@ConfigurationProperties` 注册
    - 验收点：`OperationsCleanupScheduleProperties` 不再使用 `@Component` 或字段级 `@Value`，`OperationsCleanupConfiguration` 使用 `@EnableConfigurationProperties(OperationsCleanupScheduleProperties.class)`，cleanup 策略方法和嵌套 record 行为保持不变
    - 重要度：9/10

- [ ] `operations health alert properties`：迁移 operations health alert 配置属性注册
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-COMPONENT-STEREOTYPE-CLEANUP.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthAlertPolicyProperties.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/configure/OperationsHealthAlertPolicyProperties.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/configure/OperationsExternalHealthProbeConfiguration.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthAlertStrategy.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/health/support/OperationsHealthAlertStrategyTest.java`
    - 处理动作：按 RUNBOOK Step 6 将 `OperationsHealthAlertPolicyProperties` 移入 `operations.application.health.configure` 并注册到现有 health configuration
    - 验收点：`OperationsHealthAlertPolicyProperties` 不再使用 `@Component` 或字段级 `@Value`，`OperationsExternalHealthProbeConfiguration` 同时注册 external probe 和 alert policy 两个 properties 类
    - 重要度：8/10

- [ ] `classics publication properties and executor stereotype`：收敛 classics publication 配置注册和执行器 stereotype
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-COMPONENT-STEREOTYPE-CLEANUP.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/publication/configure/ClassicsPublicationProperties.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/publication/configure/ClassicsPublicationExecutorConfiguration.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/publication/service/impl/ClassicsPublicationStepExecutorImpl.java`
    - 处理动作：按 RUNBOOK Step 7 和 Step 8 删除 `ClassicsPublicationProperties` 的 `@Component` 并将 `ClassicsPublicationStepExecutorImpl` 改为 `@Service`
    - 验收点：`ClassicsPublicationExecutorConfiguration` 使用 `@EnableConfigurationProperties(ClassicsPublicationProperties.class)`，`ClassicsPublicationProperties` 无 `@Component`，`ClassicsPublicationStepExecutorImpl` 使用 `@Service` 且无 `@Component`
    - 重要度：8/10

- [ ] `component stereotype cleanup closure`：执行组件注解清理验证和现场清理
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-COMPONENT-STEREOTYPE-CLEANUP.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-infra`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application`、`kuzhambu-servers/biz/operations/kuzhambu-operations-infra`、`docs/30-designs/RUNBOOK-COMPONENT-STEREOTYPE-CLEANUP.md`、`TODO.md`
    - 处理动作：按 RUNBOOK Verification 执行静态搜索、Spotless、Checkstyle、相关 Maven tests、diff review，并按 RUNBOOK Closure 清理 TODO 和 RUNBOOK
    - 验收点：RUNBOOK 要求的静态检查无异常，`mvn spotless:check`、`mvn checkstyle:check` 和相关 `mvn ... test` 通过，diff 只包含 RUNBOOK 允许的文件范围，已完成 TODO 项被删除，无长期价值的 RUNBOOK 被删除，有长期价值的规则先迁移到 governance 后再删除 RUNBOOK
    - 重要度：10/10

## 待讨论项
