# Operations Cleanup Target Types Runbook

## 目标

将 Operations cleanup 目标类型扩展收口为已完成状态。

本次闭环完成后：

- `cleanup` 同时支持业务过期目标与 Operations 运行态过期目标。
- 每次执行写入 `operations_cleanup_job` 汇总台账。
- 每个目标写入 `operations_cleanup_item` 明细台账。
- 失败原因保留在 job 与 item 两层。
- Admin cleanup 页面可筛选、人工补偿执行、查看失败任务、查看失败项和跳转告警。
- `docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md` 将 `清理任务` 标记为 `已完成`。

## 已确认决策

本轮按以下口径执行：

- 新增运行态清理目标固定为 `EXPIRED_REPORT`、`EXPIRED_HEALTH_CHECK`、`EXPIRED_LONG_TASK`。
- 暂不自动清理 `operations_health_alert`、`operations_restore`、`operations_backup`。
- 报表默认保留 `90` 天，健康检查默认保留 `30` 天，长任务快照默认保留 `90` 天。
- 报表默认单轮 `200`，健康检查默认单轮 `500`，长任务快照默认单轮 `200`。
- 报表只清理 `SUCCEEDED` / `FAILED`，不清理 `PENDING` / `PROCESSING`。
- 长任务快照只清理非 `RUNNING`。
- 健康检查按 `checked_at` 清理历史记录。
- Admin cleanup 页面允许具备 `operations:cleanup:execute` 权限的用户人工触发全部 7 类 cleanup type。
- 删除 `operations_report` 记录时，本轮不直接删除 `storage_object`；报表产物对象继续交给 Storage orphan cleanup 生命周期处理。

## 数据结构

本轮不新增数据库表，不新增数据库字段，不修改现有表字段类型。

现有表字段必须继续按以下口径写入。

`operations_cleanup_job`：

| 字段 | 写入口径 |
| --- | --- |
| `cleanup_id` | 清理任务业务 ID，由持久化层生成并返回为 `CleanupJobId` |
| `cleanup_type` | 本次清理类型，取 7 类 cleanup type 之一 |
| `cleanup_status` | `RUNNING` / `SUCCEEDED` / `FAILED` |
| `total_count` | 本轮发现的目标总数 |
| `success_count` | item 状态为 `SUCCEEDED` 的数量 |
| `failed_count` | item 状态为 `FAILED` 的数量 |
| `failure_reason` | job 级异常原因；item 失败但 job 无异常时可为空，告警原因使用 `cleanup failed items: <failedCount>` |
| `requester_user_id` | 人工执行为当前用户 ID；调度执行为 `null` |
| `started_at` | job 创建时间 |
| `completed_at` | job 完成时间，失败异常也必须写入 |

`operations_cleanup_item`：

| 字段 | 写入口径 |
| --- | --- |
| `cleanup_item_id` | 清理明细业务 ID，由持久化层生成并返回为 `CleanupItemId` |
| `cleanup_id` | 所属 `operations_cleanup_job.cleanup_id` |
| `target_type` | item target type，取 `backup`、`export`、`share`、`draft`、`report`、`health-check`、`long-task` 之一 |
| `target_id` | 被清理目标业务 ID |
| `item_status` | `SUCCEEDED` / `FAILED` |
| `failure_reason` | item 失败原因；删除目标未命中固定写入 `TARGET_NOT_FOUND` |
| `processed_at` | item 处理时间 |

运行态目标读取字段：

| cleanup type | 来源表 | 读取字段 | 查询边界 |
| --- | --- | --- | --- |
| `EXPIRED_REPORT` | `operations_report` | `report_id`、`report_status`、`requested_at` | `requested_at <= threshold` 且 `report_status in ('SUCCEEDED', 'FAILED')` |
| `EXPIRED_HEALTH_CHECK` | `operations_health_check` | `check_id`、`checked_at` | `checked_at <= threshold` |
| `EXPIRED_LONG_TASK` | `operations_long_task_snapshot` | `snapshot_id`、`task_status`、`snapshot_at` | `snapshot_at <= threshold` 且 `task_status != 'RUNNING'` |

## 类型清单

最终支持的 cleanup type：

- `EXPIRED_BACKUP`
- `EXPIRED_EXPORT`
- `EXPIRED_SHARE`
- `EXPIRED_DRAFT`
- `EXPIRED_REPORT`
- `EXPIRED_HEALTH_CHECK`
- `EXPIRED_LONG_TASK`

最终支持的 cleanup item target type：

- `backup`
- `export`
- `share`
- `draft`
- `report`
- `health-check`
- `long-task`

固定执行顺序：

1. `EXPIRED_BACKUP`
2. `EXPIRED_EXPORT`
3. `EXPIRED_SHARE`
4. `EXPIRED_DRAFT`
5. `EXPIRED_REPORT`
6. `EXPIRED_HEALTH_CHECK`
7. `EXPIRED_LONG_TASK`

## 相关文件

后端 domain：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/report/repository/ReportRepository.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/repository/HealthCheckRepository.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/task/repository/LongTaskSnapshotRepository.java`

后端 application：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/support/OperationsCleanupSupport.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/support/OperationsCleanupScheduleProperties.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/service/impl/CleanupApplicationServiceImpl.java`

后端 infra：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/repository/impl/ReportRepositoryImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/repository/impl/HealthCheckRepositoryImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/task/repository/impl/LongTaskSnapshotRepositoryImpl.java`

运行时配置：

- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/resources/application.yml`
- `.env.example`
- `deploy/.env.example`

前端：

- `kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-types.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-service.ts`

测试：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/cleanup/service/impl/CleanupApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/cleanup/support/OperationsCleanupSchedulePropertiesTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/cleanup/support/OperationsCleanupSchedulerTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/test/java/com/thundax/kuzhambu/operations/infra/report/repository/impl/ReportRepositoryImplTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/test/java/com/thundax/kuzhambu/operations/infra/health/repository/impl/HealthCheckRepositoryImplTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/test/java/com/thundax/kuzhambu/operations/infra/task/repository/impl/LongTaskSnapshotRepositoryImplTest.java`
- `kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-page.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-service-contract.test.ts`

文档：

- `docs/30-designs/RUNBOOK-OPERATIONS-CLEANUP-TARGET-TYPES.md`
- `docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md`

## 任务拆分

每个任务控制在 2-5 个主要文件内。

### 任务 1：登记 cleanup type 与调度配置

文件：

- `OperationsCleanupSupport.java`
- `OperationsCleanupScheduleProperties.java`
- `application.yml`
- `.env.example`
- `deploy/.env.example`

处理动作：

- 新增 `EXPIRED_REPORT`、`EXPIRED_HEALTH_CHECK`、`EXPIRED_LONG_TASK`。
- 新增 `report`、`health-check`、`long-task` target type。
- 将执行顺序扩展为 7 类。
- 为三类新增 policy 的 `enabled`、`retention-days`、`limit` 提供配置。

验收点：

- `OperationsCleanupSupport.orderedCleanupTypes()` 返回 7 类并保持固定顺序。
- `OperationsCleanupScheduleProperties.policyFor(...)` 能解析三类新增 policy。
- 三处配置文件的环境变量名称与默认值一致。

### 任务 2：补运行态目标 domain repository 边界

文件：

- `ReportRepository.java`
- `HealthCheckRepository.java`
- `LongTaskSnapshotRepository.java`

处理动作：

- `ReportRepository` 增加 `listExpiredReportIds(Date requestedBefore, int limit)`。
- `HealthCheckRepository` 增加 `listExpiredCheckIds(Date checkedBefore, int limit)`。
- `LongTaskSnapshotRepository` 增加 `listExpiredSnapshotIds(Date snapshotBefore, int limit)`。
- 新方法默认返回空列表，降低测试桩和非目标实现的改造面。

验收点：

- domain repository 编译通过。
- 旧测试桩不需要为了非本轮验证目标强制实现新增方法。

### 任务 3：补运行态目标 infra 查询实现

文件：

- `ReportRepositoryImpl.java`
- `HealthCheckRepositoryImpl.java`
- `LongTaskSnapshotRepositoryImpl.java`

处理动作：

- `ReportRepositoryImpl.listExpiredReportIds` 查询 `operations_report.report_id`。
- `HealthCheckRepositoryImpl.listExpiredCheckIds` 查询 `operations_health_check.check_id`。
- `LongTaskSnapshotRepositoryImpl.listExpiredSnapshotIds` 查询 `operations_long_task_snapshot.snapshot_id`。

验收点：

- 报表查询限定 `report_status in ('SUCCEEDED', 'FAILED')`。
- 健康检查查询按 `checked_at` 升序取 ID。
- 长任务查询排除 `task_status = 'RUNNING'`。
- 每个查询都按 `limit` 限制单轮目标数量。

### 任务 4：补 cleanup application 执行闭环

文件：

- `CleanupApplicationServiceImpl.java`
- `CleanupApplicationServiceImplTest.java`
- `OperationsCleanupSchedulePropertiesTest.java`
- `OperationsCleanupSchedulerTest.java`

处理动作：

- 在发现阶段接入 report、health check、long task 三类目标。
- 在执行阶段按 target type 删除对应目标。
- 删除未命中时写入 item `FAILED` 与 `TARGET_NOT_FOUND`。
- 任一 item 失败时 job 进入 `FAILED` 并联动健康告警。
- 调度器继续按顺序执行 enabled policy，单 policy 异常不阻断后续 policy。

验收点：

- application test 覆盖成功删除运行态目标。
- application test 覆盖删除未命中的 item 失败原因。
- scheduler test 覆盖 7 类执行顺序和失败继续执行。

### 任务 5：补 infra 查询测试

文件：

- `ReportRepositoryImplTest.java`
- `HealthCheckRepositoryImplTest.java`
- `LongTaskSnapshotRepositoryImplTest.java`

处理动作：

- 为三类过期 ID 查询补单元测试。
- 检查 SQL 条件包含阈值字段、状态过滤和 `LIMIT`。

验收点：

- 三个 repository test 均通过。
- 测试能防止后续误清理运行中记录。

### 任务 6：补 Admin cleanup 页面展示与操作

文件：

- `cleanup-page.tsx`
- `cleanup-page.test.tsx`
- `cleanup-service-contract.test.ts`

处理动作：

- 清理类型筛选控件增加三类新增 cleanup type。
- 人工补偿执行选择器增加三类新增 cleanup type。
- 保持失败任务行、失败项按钮、详情抽屉和告警跳转现有行为。
- 页面测试覆盖新增 cleanup type 的人工触发确认。

验收点：

- 具备 `operations:cleanup:execute` 权限时，执行选择器可选择 `过期报表`、`过期健康检查`、`过期长任务快照`。
- 选择新增类型后弹出确认，确认文案包含对应 cleanup type。
- 列表失败行仍展示失败原因与 `查看告警` 链接。
- 点击 `失败项` 仍打开只展示失败 item 的详情抽屉。

### 任务 7：收口 readiness 与 RUNBOOK

文件：

- `RUNBOOK-OPERATIONS-CLEANUP-TARGET-TYPES.md`
- `OPERATIONS-IMPLEMENTATION-COVERAGE.md`

处理动作：

- RUNBOOK 记录已确认决策、字段口径、文件落点、任务拆分和验收命令。
- readiness 将 `清理任务` 从 `部分完成` 调整为 `已完成`。
- readiness 删除“更多目标类型扩展未闭环”表述。

验收点：

- `Current Baseline` 不再把 cleanup 放在 `部分完成`。
- `Requirement Coverage Matrix` 中 `清理任务` 状态为 `已完成`。

## 前端页面口径

页面：`/operations/cleanup`

权限：

- `operations:cleanup:view`：允许加载列表、详情和失败项。
- `operations:cleanup:execute`：允许显示人工补偿执行控件。

控件与操作：

| 控件 | 类型 | 展示条件 | 操作 | 预期行为 |
| --- | --- | --- | --- | --- |
| `清理类型` | `Select` | 可查看页面时展示 | 选择 cleanup type | 更新 `filter.cleanupType`，重置到第一页，重新查询列表 |
| `清理状态` | `Select` | 可查看页面时展示 | 选择 `RUNNING` / `SUCCEEDED` / `FAILED` | 更新 `filter.cleanupStatus`，重置到第一页，重新查询列表 |
| `刷新` | `Button` | 可查看页面时展示 | 点击 | invalidate cleanup page query |
| `执行清理类型` | `Select` | 具备 `operations:cleanup:execute` 时展示 | 选择任一非 `ALL` cleanup type | 打开危险确认弹窗 |
| `确认执行` | confirm ok button | 人工补偿确认弹窗内 | 点击 | 调用 `/operations/cleanup/execute`，成功后刷新列表 |
| `详情` | `Button` | 每行展示 | 点击 | 打开详情抽屉，展示全部 item |
| `失败项` | `Button` | 行 `failedCount > 0` 时展示 | 点击 | 打开详情抽屉，只展示 `itemStatus === 'FAILED'` 的 item |
| `查看告警` | `Button` / link | job 失败时展示 | 点击 | 跳转 `/operations/dashboard?sourceRefType=CLEANUP&sourceRefId=<cleanupId>` |
| `上一页` | `Button` | 列表底部分页 | 点击 | `pageNo - 1` 后重新查询 |
| `下一页` | `Button` | 列表底部分页 | 点击 | `pageNo + 1` 后重新查询 |

清理类型控件选项：

| 展示文案 | value |
| --- | --- |
| `全部类型` | `ALL` |
| `过期备份` | `EXPIRED_BACKUP` |
| `过期导出` | `EXPIRED_EXPORT` |
| `过期分享` | `EXPIRED_SHARE` |
| `过期草稿` | `EXPIRED_DRAFT` |
| `过期报表` | `EXPIRED_REPORT` |
| `过期健康检查` | `EXPIRED_HEALTH_CHECK` |
| `过期长任务快照` | `EXPIRED_LONG_TASK` |

列表列：

- `清理 ID`
- `类型`
- `状态`
- `总量`
- `成功`
- `失败`
- `失败提示`
- `执行人`
- `开始时间`
- `完成时间`
- `操作`

详情抽屉字段：

- `清理 ID`
- `清理类型`
- `任务状态`
- `触发来源`
- `处理总量`
- `成功数量`
- `失败数量`
- `开始时间`
- `完成时间`

item 明细列：

- `明细 ID`
- `目标类型`
- `目标 ID`
- `状态`
- `处理时间`
- `失败原因`

## 验证命令

后端：

```sh
cd kuzhambu-servers
mvn -pl biz/operations/kuzhambu-operations-domain,biz/operations/kuzhambu-operations-application,biz/operations/kuzhambu-operations-infra,starter/kuzhambu-admin-starter -am spotless:check
mvn -pl biz/operations/kuzhambu-operations-domain,biz/operations/kuzhambu-operations-application,biz/operations/kuzhambu-operations-infra,starter/kuzhambu-admin-starter -am checkstyle:check
mvn -pl biz/operations/kuzhambu-operations-application,biz/operations/kuzhambu-operations-infra -am -Dtest=CleanupApplicationServiceImplTest,OperationsCleanupSchedulePropertiesTest,OperationsCleanupSchedulerTest,ReportRepositoryImplTest,HealthCheckRepositoryImplTest,LongTaskSnapshotRepositoryImplTest -Dsurefire.failIfNoSpecifiedTests=false test
```

前端：

```sh
cd kuzhambu-apps
pnpm --filter kuzhambu-admin-web run format
pnpm run format:check
pnpm run lint
pnpm --filter kuzhambu-admin-web test -- cleanup-page.test.tsx cleanup-service-contract.test.ts
```

已知验证注意点：

- 带 `-am` 的 Maven 定向测试需要 `-Dsurefire.failIfNoSpecifiedTests=false`，避免依赖模块没有匹配测试时失败。
- `OperationsCleanupSchedulerTest` 的失败继续执行用例会输出预期 WARN 日志，测试通过即可。
