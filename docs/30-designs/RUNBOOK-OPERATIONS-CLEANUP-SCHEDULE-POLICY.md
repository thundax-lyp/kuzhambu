# Operations Cleanup Schedule Policy Runbook

## Purpose

本文档定义 Operations 清理能力从“管理员手动真实执行”收口到“自动调度 + 长期规则策略”的目标态执行手册。

目标不是新增一次性清理脚本，也不是保留人工触发作为主路径。交付完成后，Operations cleanup 必须具备稳定调度入口、可配置长期保留策略、可追溯自动台账、失败告警联动和 admin 可见的执行结果。

## Target State

- `cleanup` 默认由后台调度自动执行，管理员手动执行只作为受控补偿入口。
- 每个清理类型都有明确的 `enabled`、`retentionDays` 和 `limit` 策略，策略由配置驱动，不硬编码在执行逻辑内。
- 自动调度每轮按固定顺序执行所有启用的清理类型，单个类型失败不得阻断后续类型。
- 自动任务写入现有 `operations_cleanup_job` 和 `operations_cleanup_item` 台账，详情页能看到自动清理结果、失败原因和 item 明细。
- 自动任务不新增数据库字段，以 `operations_cleanup_job.requester_user_id IS NULL` 作为系统自动触发的唯一数据语义；人工任务继续写入管理员用户 ID。
- 失败仍复用 `OperationsHealthAlertStrategy.recordCleanupFailed` 生成健康告警来源，dashboard 和 cleanup 页面保持可跳转。
- 清理发现与执行继续通过 Operations application 编排和各业务域 facade 完成，不直接访问其他业务域 infra、mapper、repository.impl 或主表。

## Scope

本次只闭环 Operations cleanup 的调度化与长期规则策略。

必须覆盖：

- `EXPIRED_BACKUP`
- `EXPIRED_EXPORT`
- `EXPIRED_SHARE`
- `EXPIRED_DRAFT`

不纳入本次：

- 新建独立清理脚本体系。
- 新增非 Operations 拥有的主事实表。
- 绕过 Classics facade 直接删除 Classics 主表数据。
- 引入分布式调度平台或外部告警系统。
- 改造 Storage orphan 清理；涉及导出产物对象时继续复用 Storage 已有 orphan 生命周期。

## Policy Contract

新增 Operations cleanup 调度配置，配置归属 `kuzhambu.operations.cleanup.schedule`。

目标配置口径：

```yaml
kuzhambu:
  operations:
    cleanup:
      schedule:
        enabled: ${KUZHAMBU_OPERATIONS_CLEANUP_SCHEDULE_ENABLED:true}
        startup-enabled: ${KUZHAMBU_OPERATIONS_CLEANUP_STARTUP_ENABLED:false}
        daily-cron: ${KUZHAMBU_OPERATIONS_CLEANUP_DAILY_CRON:0 30 3 * * ?}
        default-limit: ${KUZHAMBU_OPERATIONS_CLEANUP_DEFAULT_LIMIT:200}
        policies:
          expired-backup:
            enabled: ${KUZHAMBU_OPERATIONS_CLEANUP_EXPIRED_BACKUP_ENABLED:true}
            retention-days: ${KUZHAMBU_OPERATIONS_CLEANUP_EXPIRED_BACKUP_RETENTION_DAYS:30}
            limit: ${KUZHAMBU_OPERATIONS_CLEANUP_EXPIRED_BACKUP_LIMIT:200}
          expired-export:
            enabled: ${KUZHAMBU_OPERATIONS_CLEANUP_EXPIRED_EXPORT_ENABLED:true}
            retention-days: ${KUZHAMBU_OPERATIONS_CLEANUP_EXPIRED_EXPORT_RETENTION_DAYS:7}
            limit: ${KUZHAMBU_OPERATIONS_CLEANUP_EXPIRED_EXPORT_LIMIT:200}
          expired-share:
            enabled: ${KUZHAMBU_OPERATIONS_CLEANUP_EXPIRED_SHARE_ENABLED:true}
            retention-days: ${KUZHAMBU_OPERATIONS_CLEANUP_EXPIRED_SHARE_RETENTION_DAYS:90}
            limit: ${KUZHAMBU_OPERATIONS_CLEANUP_EXPIRED_SHARE_LIMIT:200}
          expired-draft:
            enabled: ${KUZHAMBU_OPERATIONS_CLEANUP_EXPIRED_DRAFT_ENABLED:true}
            retention-days: ${KUZHAMBU_OPERATIONS_CLEANUP_EXPIRED_DRAFT_RETENTION_DAYS:30}
            limit: ${KUZHAMBU_OPERATIONS_CLEANUP_EXPIRED_DRAFT_LIMIT:200}
```

规则：

- `daily-cron` 默认晚于自动备份 `0 0 2 * * ?` 和 Discovery deleted cleanup `0 0 3 * * ?`，避免同一时刻争抢运行资源。
- `startup-enabled` 默认 `false`，避免服务重启后立即触发批量删除。
- `default-limit` 只作为单个 policy 未配置 `limit` 时的兜底。
- `retentionDays` 传入目标域发现接口，由目标域按自身时间字段判断是否过期。
- `limit` 是单轮单类型上限，不是永久总量上限。
- policy disabled 时不创建对应 cleanup job。
- 本次不支持 admin 在线编辑策略；配置变更通过环境变量和应用重启生效。

## Data Structure Decision

本次不变更数据库结构。

数据库字段继续使用现有结构：

| 表 | 字段 | 目标语义 |
| --- | --- | --- |
| `operations_cleanup_job` | `cleanup_id` | cleanup job 业务 ID，自动和人工任务共用 |
| `operations_cleanup_job` | `cleanup_type` | 清理类型，固定为 `EXPIRED_BACKUP`、`EXPIRED_EXPORT`、`EXPIRED_SHARE`、`EXPIRED_DRAFT` |
| `operations_cleanup_job` | `cleanup_status` | job 状态，固定为 `RUNNING`、`SUCCEEDED`、`FAILED` |
| `operations_cleanup_job` | `total_count` | 本 job 发现并尝试处理的目标总数 |
| `operations_cleanup_job` | `success_count` | 本 job 成功 item 数 |
| `operations_cleanup_job` | `failed_count` | 本 job 失败 item 数 |
| `operations_cleanup_job` | `failure_reason` | job 级失败摘要，最大 `1024` 字符 |
| `operations_cleanup_job` | `requester_user_id` | 人工任务写管理员用户 ID；自动任务必须写 `NULL` |
| `operations_cleanup_job` | `started_at` | job 开始时间 |
| `operations_cleanup_job` | `completed_at` | job 完成时间 |
| `operations_cleanup_item` | `cleanup_item_id` | cleanup item 业务 ID |
| `operations_cleanup_item` | `cleanup_id` | 所属 cleanup job 业务 ID |
| `operations_cleanup_item` | `target_type` | 目标类型，固定为 `backup`、`export`、`share`、`draft` |
| `operations_cleanup_item` | `target_id` | 目标对象 ID |
| `operations_cleanup_item` | `item_status` | item 状态，固定为 `SUCCEEDED`、`FAILED` |
| `operations_cleanup_item` | `failure_reason` | item 失败原因，最大 `1024` 字符 |
| `operations_cleanup_item` | `processed_at` | item 处理时间 |

明确不新增：

- 不新增 `trigger_source` 字段。
- 不新增 `cleanup_policy` 表。
- 不新增策略配置表。
- 不新增调度运行批次表。
- 不调整 `db/schema/operations.sql` 的 cleanup 表结构。

Java 应用层数据结构需要精确扩展：

| 文件 | 类型 | 字段 | 要求 |
| --- | --- | --- | --- |
| `OperationsCleanupExecuteCommand.java` | command | `cleanupType: String` | 保持现有字段 |
| `OperationsCleanupExecuteCommand.java` | command | `requesterUserId: Long` | 改为允许 `null`；HTTP 人工入口仍由 interface/application 校验非空 |
| `OperationsCleanupExecuteCommand.java` | command | `requestedAt: Date` | 新增；为空时 application 使用当前时间 |
| `OperationsCleanupExecuteCommand.java` | command | `retentionDays: Integer` | 新增；自动调度必须传入；人工入口可为空并使用 policy 或目标域默认 |
| `OperationsCleanupExecuteCommand.java` | command | `limit: Integer` | 新增；为空时使用 `OperationsCleanupScheduleProperties.defaultLimit` |
| `OperationsCleanupPageResult.java` | result | `requesterUserId: Long` | 保持现有字段；`null` 表示系统自动 |
| `OperationsCleanupDetailResult.java` | result | `requesterUserId: Long` | 保持现有字段；`null` 表示系统自动 |
| `OperationsCleanupPageResponse.java` | response | `requesterUserId: Long` | 保持现有字段；前端据此显示执行人 |
| `OperationsCleanupDetailResponse.java` | response | `requesterUserId: Long` | 保持现有字段；前端据此显示触发来源 |

前端类型需要精确保持：

| 文件 | 类型 | 字段 | 要求 |
| --- | --- | --- | --- |
| `cleanup-types.ts` | `OperationsCleanupRecord` | `requesterUserId?: number | null` | 必须保留，用于显示 `系统自动` 或用户 ID |
| `cleanup-types.ts` | `OperationsCleanupRecord` | `cleanupType?: string | null` | 四类 cleanup type |
| `cleanup-types.ts` | `OperationsCleanupRecord` | `cleanupStatus?: string | null` | 三类 cleanup status |
| `cleanup-types.ts` | `OperationsCleanupItemRecord` | `targetType?: string | null` | `backup`、`export`、`share`、`draft` |
| `cleanup-service.ts` | `CleanupExecuteCommand` | `cleanupType: string` | 手动补偿入口唯一前端入参 |

## Application Design

`CleanupApplicationService` 保留现有人工入口，并补齐调度入口。

目标职责：

- 人工入口继续要求 `requesterUserId`，权限仍由 interface 层 `operations:cleanup:execute` 控制。
- 调度入口由 `OperationsCleanupScheduler` 调用，不依赖 HTTP controller，不要求管理员用户。
- 调度入口对每个启用 policy 构造统一执行上下文，至少包含 `cleanupType`、`requestedAt`、`retentionDays`、`limit` 和执行来源。
- 自动任务以 `requesterUserId = null` 表示系统自动触发，并在 application result、interface response 和 admin 页面中稳定映射为“系统自动”。
- `discoverCleanupTargets` 不再读取 `OperationsCleanupSupport.DEFAULT_CLEANUP_TARGET_LIMIT` 作为唯一来源，必须使用执行上下文的 `limit`。
- backup 清理发现必须按 `requestedAt` 与 `retentionDays` 策略查找过期备份；Classics 清理发现必须把 `retentionDays` 和 `limit` 传给 `ClassicsCleanupTargetsFacadeRequest`。
- 单个 cleanup item 执行失败只影响当前 job 的 `failedCount` 与状态，不中断本 job 后续 item。
- 单个 cleanup type job 失败不阻断调度器继续执行后续 cleanup type。

## Scheduler Design

新增 `OperationsCleanupScheduler`，放在 Operations application 的 cleanup support 包。

目标行为：

- 使用 `@Scheduled(cron = "${kuzhambu.operations.cleanup.schedule.daily-cron:0 30 3 * * ?}")`。
- 启动事件只在 `enabled=true` 且 `startup-enabled=true` 时执行。
- 日常调度只在 `enabled=true` 时执行。
- 每轮读取启用的 policy，按 `EXPIRED_BACKUP`、`EXPIRED_EXPORT`、`EXPIRED_SHARE`、`EXPIRED_DRAFT` 顺序执行。
- 每个 type 调用 application 调度入口并捕获异常，异常记录日志并继续下一个 type。
- 调度器不直接发现目标、不直接删除目标、不直接写 repository。

## Domain And Persistence

复用现有 `CleanupJob` / `CleanupItem` 和 `operations_cleanup_job` / `operations_cleanup_item`。

必须修改或确认的文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/cleanup/model/entity/CleanupJob.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/cleanup/model/entity/CleanupItem.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/cleanup/persistence/dataobject/CleanupJobDO.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/cleanup/persistence/dataobject/CleanupItemDO.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/cleanup/persistence/assembler/CleanupPersistenceAssembler.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/cleanup/repository/impl/CleanupJobRepositoryImpl.java`

目标要求：

- `CleanupJob.requesterUserId` 允许为 `null`。
- `CleanupPersistenceAssembler` 必须保持 `requesterUserId = null` 往返不丢失。
- `CleanupJobRepositoryImpl.page` 不新增自动/人工筛选参数；现有 `requesterUserId` 过滤保持原语义。
- `db/schema/operations.sql` 不做 cleanup 字段变更。

## Cross Domain Contract

Classics facade 已是 Operations cleanup 对 Classics 主事实的唯一跨域边界。本次必须继续使用 facade。

目标约束：

- `ClassicsCleanupTargetsFacadeRequest.retentionDays` 必须成为有效输入，不再只依赖目标域默认值。
- `ClassicsCleanupTargetsFacadeRequest.limit` 必须成为有效输入。
- `ClassicsCleanupExecutionFacadeResponse` 继续返回逐 item 结果，Operations 只记录结果，不反向解释 Classics 内部删除细节。
- backup 属于 Operations 自有台账，可继续通过 `BackupRepository` 删除，但发现阈值要由 policy 输入控制。

## Admin Web

`/operations/cleanup` 页面必须体现调度化后的目标态。

目标行为：

- 顶部继续使用现有 `Card` + `Statistic` 摘要，不新增策略编辑控件。
- 查询区继续使用 `Card`，标题为 `清理任务查询`。
- 查询区 `清理类型` 使用 Ant Design `Select`，`aria-label="清理类型"`，选项精确为 `全部类型`、`过期备份`、`过期导出`、`过期分享`、`过期草稿`。
- 查询区 `清理状态` 使用 Ant Design `Select`，`aria-label="清理状态"`，选项精确为 `全部状态`、`进行中`、`成功`、`失败`。
- 查询区 `刷新` 使用 Ant Design `Button` + `ReloadOutlined`，点击后调用 `refreshPage()`。
- 手动补偿入口保留为 Ant Design `Select`，`aria-label="执行清理类型"`，placeholder 改为 `选择清理类型并立即执行一次`，仍按 `operations:cleanup:execute` 权限显隐。
- 选择手动执行类型后继续使用 `useKuzhambuConfirm().danger` 二次确认，确认标题为 `立即执行一次清理任务`，确认按钮文案为 `确认执行`，取消保持确认组件默认取消行为，不发请求。
- 确认说明必须提示“自动调度是主路径，本操作仅用于人工补偿”。
- 表格新增 `执行人` 列，位于 `失败提示` 与 `开始时间` 之间；`requesterUserId == null` 显示 `系统自动`，非空显示用户 ID。
- 表格 `操作` 列保留 `详情` button；失败记录继续保留 `失败项` button 和 `查看告警` button。
- 空状态 `colSpan` 必须随新增 `执行人` 列从 `10` 调整为 `11`。
- 详情抽屉继续使用 `KuzhambuDrawer`。
- 详情抽屉 `Descriptions` 新增 `触发来源` 项；`requesterUserId == null` 显示 `系统自动`，非空显示 `人工执行：<userId>`。
- 详情抽屉 item 明细表列保持 `明细 ID`、`目标类型`、`目标 ID`、`状态`、`处理时间`、`失败原因`。
- 不新增独立“配置编辑”页面；本次策略来自运行时配置。

必须修改或确认的前端文件：

- `kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-types.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-service.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-page.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-service-contract.test.ts`

## Runtime Configuration

必须同步以下文件：

- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/resources/application.yml`
- `.env.example`
- `deploy/.env.example`
- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/test/java/com/thundax/kuzhambu/starter/admin/AdminStarterArchitectureTest.java`

配置命名必须使用 `KUZHAMBU_OPERATIONS_CLEANUP_*` 前缀，不复用 backup 或 discovery 的环境变量。

## Implementation Tasks

### Task 1: Cleanup Policy Properties

目标：新增 cleanup 调度策略配置对象，精确承载总开关、启动开关、cron、默认 limit 和四类 policy。

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/support/OperationsCleanupScheduleProperties.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/support/OperationsCleanupSupport.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/cleanup/support/OperationsCleanupSchedulePropertiesTest.java`

字段要求：

- `enabled: boolean`
- `startupEnabled: boolean`
- `dailyCron: String`
- `defaultLimit: int`
- `expiredBackup.enabled: boolean`
- `expiredBackup.retentionDays: int`
- `expiredBackup.limit: Integer`
- `expiredExport.enabled: boolean`
- `expiredExport.retentionDays: int`
- `expiredExport.limit: Integer`
- `expiredShare.enabled: boolean`
- `expiredShare.retentionDays: int`
- `expiredShare.limit: Integer`
- `expiredDraft.enabled: boolean`
- `expiredDraft.retentionDays: int`
- `expiredDraft.limit: Integer`

交付要求：

- `OperationsCleanupScheduleProperties` 使用 `@Component` 与 `@Value`，风格对齐 `OperationsBackupScheduleProperties`。
- 每个 policy 提供归一化后的有效 `limit`，当 policy limit 为空或小于等于 `0` 时使用 `defaultLimit`。
- `OperationsCleanupSupport` 保持四类 cleanup type 常量唯一来源，新增按固定顺序返回支持类型的方法。

### Task 2: Cleanup Application Execution Context

目标：让人工和自动执行共用同一条 application 执行链路，自动任务写 `requesterUserId = null`，并让 `retentionDays`、`limit` 真正进入发现逻辑。

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/command/OperationsCleanupExecuteCommand.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/service/CleanupApplicationService.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/service/impl/CleanupApplicationServiceImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/cleanup/service/impl/CleanupApplicationServiceImplTest.java`

字段要求：

- `OperationsCleanupExecuteCommand.cleanupType`
- `OperationsCleanupExecuteCommand.requesterUserId`
- `OperationsCleanupExecuteCommand.requestedAt`
- `OperationsCleanupExecuteCommand.retentionDays`
- `OperationsCleanupExecuteCommand.limit`

行为要求：

- HTTP 人工执行仍校验 `requesterUserId` 非空。
- 调度执行允许 `requesterUserId` 为空。
- `EXPIRED_BACKUP` 发现逻辑必须使用 `requestedAt`、`retentionDays` 和 `limit`。
- Classics facade 请求必须传入 `cleanupType`、`requestedAt`、`retentionDays` 和 `limit`。
- 自动任务写入 `CleanupJob.requesterUserId = null`。
- `OperationsCleanupPageResult.requesterUserId` 和 `OperationsCleanupDetailResult.requesterUserId` 必须原样返回 `null`，不得改成 `0`、`-1` 或字符串。

### Task 3: Cleanup Scheduler

目标：新增自动调度器，默认每日 `03:30` 执行，启动执行默认关闭。

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/support/OperationsCleanupScheduler.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/cleanup/support/OperationsCleanupSchedulerTest.java`
- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/resources/application.yml`
- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/test/java/com/thundax/kuzhambu/starter/admin/AdminStarterArchitectureTest.java`

行为要求：

- 总开关关闭时不执行启动清理，也不执行每日清理。
- `startup-enabled=false` 时启动事件不创建 cleanup job。
- policy disabled 时跳过对应类型，不创建空 job。
- 单个类型抛异常时记录日志并继续后续类型。
- 调度顺序固定为 `EXPIRED_BACKUP`、`EXPIRED_EXPORT`、`EXPIRED_SHARE`、`EXPIRED_DRAFT`。
- `application.yml` 必须新增 `kuzhambu.operations.cleanup.schedule` 完整配置块。
- `AdminStarterArchitectureTest` 必须断言 `KUZHAMBU_OPERATIONS_CLEANUP_SCHEDULE_ENABLED`、`KUZHAMBU_OPERATIONS_CLEANUP_STARTUP_ENABLED`、`KUZHAMBU_OPERATIONS_CLEANUP_DAILY_CRON` 和至少一个 policy env placeholder。

### Task 4: Cross Domain Retention Contract

目标：让 Classics cleanup facade 明确消费 `retentionDays` 和 `limit`，避免 Operations 策略只停留在配置层。

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/request/ClassicsCleanupTargetsFacadeRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/facade/impl/ClassicsFacadeImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/cleanup/service/ClassicsCleanupApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/cleanup/service/impl/ClassicsCleanupApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/cleanup/ClassicsCleanupApplicationServiceImplTest.java`

行为要求：

- `retentionDays` 为空时使用 Classics 既有默认策略。
- `limit` 为空时使用 Operations 传入前已归一化的默认值或 Classics 既有默认值。
- 不新增 Operations 到 Classics infra 的直接依赖。
- `ClassicsCleanupTargetsFacadeRequest` 字段必须精确包含 `cleanupType`、`requestedAt`、`retentionDays`、`limit`、`targetIds`。
- `ClassicsCleanupApplicationService.listTargets` 方法签名必须接收 `retentionDays` 与 `limit`，不能在 facade 层吞掉策略参数。

### Task 5: Admin Web Cleanup UX

目标：让 cleanup 页面清楚区分自动调度和人工补偿，且不提供在线策略编辑。

文件：

- `kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-types.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-service.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-page.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-service-contract.test.ts`

控件和操作要求：

- `cleanupTypeOptions`：包含 `ALL`、`EXPIRED_BACKUP`、`EXPIRED_EXPORT`、`EXPIRED_SHARE`、`EXPIRED_DRAFT`，展示名分别为 `全部类型`、`过期备份`、`过期导出`、`过期分享`、`过期草稿`。
- `cleanupStatusOptions`：包含 `ALL`、`RUNNING`、`SUCCEEDED`、`FAILED`，展示名分别为 `全部状态`、`进行中`、`成功`、`失败`。
- 查询区 `Select[aria-label="清理类型"]`：变更后重置 `pageNo` 为 `DEFAULT_PAGE_NO`。
- 查询区 `Select[aria-label="清理状态"]`：变更后重置 `pageNo` 为 `DEFAULT_PAGE_NO`。
- `Button[刷新]`：点击后调用 `refreshPage()`。
- `Select[aria-label="执行清理类型"]`：只展示四个具体类型，不展示 `ALL`；选择后调用 `executeCleanup(value)`。
- `executeCleanup`：使用 `useKuzhambuConfirm().danger`，`okText` 必须为 `确认执行`，`onConfirm` 调用 `executeMutation.mutateAsync({ cleanupType })`。
- table header：新增 `执行人` 列，表格空态 `colSpan=11`。
- table row `执行人` cell：`record.requesterUserId == null ? "系统自动" : record.requesterUserId`。
- `Button[详情]`：打开 `KuzhambuDrawer` 并展示全部 item。
- `Button[失败项]`：只在 `failedCount > 0` 时展示，打开 `KuzhambuDrawer` 并过滤失败 item。
- `Button[查看告警]`：失败行保留 `href={buildAlertPath(record.cleanupId)}`。
- `KuzhambuDrawer` 的 `Descriptions`：新增 `触发来源`，并展示开始时间、完成时间、处理总量、成功数量、失败数量。
- item 明细 table：保持 `明细 ID`、`目标类型`、`目标 ID`、`状态`、`处理时间`、`失败原因` 六列。

### Task 6: Runtime Examples And Coverage

目标：同步配置样例和覆盖矩阵，任务关闭时清理本 RUNBOOK。

文件：

- `.env.example`
- `deploy/.env.example`
- `docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md`
- `docs/30-designs/RUNBOOK-OPERATIONS-CLEANUP-SCHEDULE-POLICY.md`

要求：

- `.env.example` 和 `deploy/.env.example` 必须包含所有 `KUZHAMBU_OPERATIONS_CLEANUP_*` 变量。
- 覆盖矩阵在实现完成后将 cleanup 的“调度化清理、长期规则策略”更新为已完成。
- 任务关闭前删除本 RUNBOOK 和残留引用。

## Tests

Java servers 最小验证：

```sh
cd kuzhambu-servers
mvn -pl biz/operations/kuzhambu-operations-application -Dtest=CleanupApplicationServiceImplTest,OperationsCleanupSchedulerTest test
mvn -pl biz/operations/kuzhambu-operations-interface -Dtest=OperationsCleanupAdminControllerTest test
mvn -pl starter/kuzhambu-admin-starter -Dtest=AdminStarterArchitectureTest test
mvn spotless:check
mvn checkstyle:check
```

如果触及 Classics facade 或 application，补充并运行对应 Classics 定向测试：

```sh
cd kuzhambu-servers
mvn -pl biz/classics/kuzhambu-classics-application -Dtest=ClassicsCleanupApplicationServiceImplTest,ClassicsFacadeImplTest test
```

Admin Web 最小验证：

```sh
cd kuzhambu-apps
npm --workspace kuzhambu-admin-web run format
npm run format:check
npm run lint
npm --workspace kuzhambu-admin-web run test -- cleanup
```

## Acceptance

- 自动调度默认开启，每日 `03:30` 执行；启动自动清理默认关闭。
- 关闭总开关后，启动事件和每日调度都不会创建 cleanup job。
- 关闭单个 policy 后，该类型不会创建 cleanup job，其他启用类型继续执行。
- 单个 policy 的 `retentionDays` 和 `limit` 会进入对应发现逻辑，并被测试验证。
- 调度一轮中某个类型失败时，后续类型仍会被尝试执行。
- 自动执行的 cleanup job 能在 page/detail 接口和 admin 页面被识别为系统自动触发。
- item 失败会保留 item 明细、job 失败摘要和健康告警来源。
- 手动执行入口仍可用，仍受 `operations:cleanup:execute` 权限控制。
- 文档覆盖矩阵更新为 cleanup 调度化与长期规则策略已完成；本 RUNBOOK 在任务关闭前删除。

## Review Checklist

- 没有新增绕过 application/facade 的清理路径。
- 没有把长期策略写死在 `CleanupApplicationServiceImpl`。
- 没有让 startup cleanup 默认开启。
- 没有让某个 cleanup type 的失败阻断整轮调度。
- 没有把 Storage orphan 生命周期复制到 Operations。
- 没有新增不可配置的批量删除上限。
- 没有遗漏 `.env.example`、`deploy/.env.example` 和 starter 配置测试。
