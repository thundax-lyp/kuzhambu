# Operations Runtime Closure Runbook

## Purpose

补齐 Operations 域剩余的三条最小运行态闭环：

- `cleanup` 手动触发、台账记录、详情查询
- `health` 摘要与明细查询
- `long task` 快照查询与统一运维入口

本轮只补现有表、现有菜单、现有路由下的闭环，不做新表、新菜单、自动调度、写入阻断或日志审计正文聚合。

## Fixed Decisions

- 不新增数据库表。
- 不新增菜单种子。
- `health` 放入现有 `/operations/tasks`。
- `long task` 读取源固定为 `operations_long_task_snapshot`。
- `operation record` 本轮不做统一总表，继续复用：
  - `operations_report`
  - `operations_backup`
  - `operations_restore`
  - `operations_cleanup_job`

## Current Baseline

- `cleanup`
  - 已有 domain：`CleanupJob`、`CleanupItem`、`CleanupJobRepository`
  - 已有表：`operations_cleanup_job`、`operations_cleanup_item`
  - 缺 infra / application / interface / admin 闭环
- `health`
  - 已有 domain + infra CRUD
  - 缺 application / interface / admin 闭环
- `long task`
  - 已有 domain + infra CRUD
  - 缺 application / interface / admin 闭环
- Admin Web
  - 已落地：`/operations/backup-restore`
  - 已有菜单但未落地：`/operations/tasks`、`/operations/cleanup`

## Target State

### Cleanup

- 支持手动触发四类 cleanup：
  - 过期备份
  - 过期分享
  - 过期草稿
  - 过期导出产物
- 每次执行写入 `operations_cleanup_job`
- 每个处理对象写入 `operations_cleanup_item`
- Admin 支持 `execute / page / detail`

### Health

- `/operations/tasks` 展示健康摘要
- 摘要口径：每个 `component` 最新一条记录
- 支持 health 明细分页查询

### Long Task

- `/operations/tasks` 展示 long task 列表
- 支持 long task `page / detail`

### Unified Entry

- `/operations/tasks` 作为统一运维入口
- 页面包含三块：
  - health summary
  - long task table
  - report / backup-restore / cleanup 跳转入口

## Data Structure Changes

### Database

无表结构变更。继续使用：

- [operations.sql](/Volumes/storage/workspace/kuzhambu/db/schema/operations.sql)

涉及表：

- `operations_cleanup_job`
- `operations_cleanup_item`
- `operations_health_check`
- `operations_long_task_snapshot`

### Repository Contract Changes

需要扩展 repository 查询能力：

- `CleanupJobRepository`
  - 新增 cleanup job 分页查询
  - 新增 cleanup detail 查询
  - 新增按 job 查询 items
- `HealthCheckRepository`
  - 新增按 component 最新记录聚合查询
  - 新增 health 明细分页查询
- `LongTaskSnapshotRepository`
  - 新增 long task 分页查询
  - 新增 long task detail 查询

### Application Contract Changes

需要新增 application 契约：

- cleanup
  - `OperationsCleanupExecuteCommand`
  - `OperationsCleanupPageQuery`
  - `OperationsCleanupDetailQuery`
  - `OperationsCleanupPageResult`
  - `OperationsCleanupDetailResult`
- health
  - `OperationsHealthSummaryResult`
  - `OperationsHealthPageQuery`
  - `OperationsHealthPageResult`
- task
  - `OperationsTaskPageQuery`
  - `OperationsTaskDetailQuery`
  - `OperationsTaskPageResult`

### Interface Contract Changes

需要新增 admin 接口：

- cleanup
  - `POST /api/operations/cleanup/execute`
  - `POST /api/operations/cleanup/page`
  - `POST /api/operations/cleanup/detail`
- health
  - `POST /api/operations/health/summary`
  - `POST /api/operations/health/page`
- task
  - `POST /api/operations/task/page`
  - `POST /api/operations/task/detail`

权限口径：

- `operations:cleanup:view`
- `operations:cleanup:edit`
- `operations:task:view`

## Related Files

需求与设计：

- [OPERATIONS-REQUIREMENTS.md](/Volumes/storage/workspace/kuzhambu/docs/10-requirements/OPERATIONS-REQUIREMENTS.md)
- [OPERATIONS-DESIGN.md](/Volumes/storage/workspace/kuzhambu/docs/30-designs/OPERATIONS-DESIGN.md)
- [OPERATIONS-IMPLEMENTATION-COVERAGE.md](/Volumes/storage/workspace/kuzhambu/docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md)

数据与菜单：

- [operations.sql](/Volumes/storage/workspace/kuzhambu/db/schema/operations.sql)
- [system.json](/Volumes/storage/workspace/kuzhambu/db/data-source/system.json)
- [system.sql](/Volumes/storage/workspace/kuzhambu/db/data/system.sql)

后端现有基线：

- [CleanupJobRepository.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/cleanup/repository/CleanupJobRepository.java)
- [HealthCheckRepository.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/repository/HealthCheckRepository.java)
- [LongTaskSnapshotRepository.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/task/repository/LongTaskSnapshotRepository.java)
- [HealthCheckRepositoryImpl.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/repository/impl/HealthCheckRepositoryImpl.java)
- [LongTaskSnapshotRepositoryImpl.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/task/repository/impl/LongTaskSnapshotRepositoryImpl.java)

前端现有基线：

- [router/index.tsx](/Volumes/storage/workspace/kuzhambu/kuzhambu-apps/admin-web/src/router/index.tsx)
- [backup-restore-page.tsx](/Volumes/storage/workspace/kuzhambu/kuzhambu-apps/admin-web/src/pages/operations/backup-restore/backup-restore-page.tsx)

## Small Tasks

### Task 1. Cleanup infra dataobjects + mappers

文件范围：2-4 个文件

- `.../cleanup/persistence/dataobject/CleanupJobDO.java`
- `.../cleanup/persistence/dataobject/CleanupItemDO.java`
- `.../cleanup/persistence/mapper/CleanupJobMapper.java`
- `.../cleanup/persistence/mapper/CleanupItemMapper.java`

完成标准：

- cleanup 两张表有对应 DO / Mapper

### Task 2. Cleanup infra assembler + repository impl

文件范围：2-4 个文件

- `.../cleanup/persistence/assembler/CleanupPersistenceAssembler.java`
- `.../cleanup/repository/impl/CleanupJobRepositoryImpl.java`
- `CleanupJobRepository.java`

完成标准：

- `CleanupJobRepository` 完整落地到 MyBatis
- 支持 job 与 item 的基础增改查

### Task 3. Cleanup repository query extension

文件范围：2-5 个文件

- `CleanupJobRepository.java`
- `CleanupJobRepositoryImpl.java`
- `CleanupJobMapper.java`
- 必要时补 `CleanupQuerySupport.java`

完成标准：

- 支持 cleanup job 分页
- 支持 detail
- 支持按 job 查询 item 列表

### Task 4. Cleanup application execute

文件范围：3-5 个文件

- `.../cleanup/command/OperationsCleanupExecuteCommand.java`
- `.../cleanup/result/OperationsCleanupDetailResult.java`
- `.../cleanup/service/CleanupApplicationService.java`
- `.../cleanup/service/impl/CleanupApplicationServiceImpl.java`
- 必要时补 `.../cleanup/support/*`

完成标准：

- `execute` 能写 job / item 台账
- 支持四类 cleanup type

### Task 5. Cleanup application queries

文件范围：3-5 个文件

- `.../cleanup/query/OperationsCleanupPageQuery.java`
- `.../cleanup/query/OperationsCleanupDetailQuery.java`
- `.../cleanup/result/OperationsCleanupPageResult.java`
- `CleanupApplicationService.java`
- `CleanupApplicationServiceImpl.java`

完成标准：

- 支持 cleanup `page / detail`

### Task 6. Cleanup admin interface

文件范围：4-5 个文件

- `.../cleanup/assembler/OperationsCleanupInterfaceAssembler.java`
- `.../cleanup/controller/OperationsCleanupAdminController.java`
- `.../cleanup/controller/request/*`
- `.../cleanup/controller/response/*`

完成标准：

- 暴露 `/operations/cleanup/execute`
- 暴露 `/operations/cleanup/page`
- 暴露 `/operations/cleanup/detail`

### Task 7. Health repository query extension

文件范围：2-5 个文件

- `HealthCheckRepository.java`
- `HealthCheckRepositoryImpl.java`
- `HealthCheckMapper.java`
- 必要时补 `HealthQuerySupport.java`

完成标准：

- 支持按 component 最新记录聚合
- 支持 health 明细分页

### Task 8. Health application + interface

文件范围：4-5 个文件

- `.../health/query/OperationsHealthPageQuery.java`
- `.../health/result/OperationsHealthSummaryResult.java`
- `.../health/service/HealthCheckApplicationService.java`
- `.../health/service/impl/HealthCheckApplicationServiceImpl.java`
- `.../health/controller/OperationsHealthAdminController.java`

完成标准：

- 暴露 `summary / page`
- summary 口径固定为 component 最新记录

### Task 9. Long task repository query extension

文件范围：2-5 个文件

- `LongTaskSnapshotRepository.java`
- `LongTaskSnapshotRepositoryImpl.java`
- `LongTaskSnapshotMapper.java`
- 必要时补 `LongTaskQuerySupport.java`

完成标准：

- 支持 long task `page / detail`

### Task 10. Long task application + interface

文件范围：4-5 个文件

- `.../task/query/OperationsTaskPageQuery.java`
- `.../task/query/OperationsTaskDetailQuery.java`
- `.../task/result/OperationsTaskPageResult.java`
- `.../task/service/LongTaskApplicationService.java`
- `.../task/controller/OperationsTaskAdminController.java`

完成标准：

- 暴露 `/operations/task/page`
- 暴露 `/operations/task/detail`

### Task 11. Admin page `/operations/tasks`

文件范围：3-5 个文件

- `admin-web/src/pages/operations/tasks/tasks-page.tsx`
- `admin-web/src/pages/operations/tasks/tasks-service.ts`
- `admin-web/src/pages/operations/tasks/tasks-types.ts`
- `admin-web/src/pages/operations/tasks/tasks-page.test.tsx`
- `admin-web/src/router/index.tsx`

完成标准：

- 展示 health summary
- 展示 long task table
- 提供 report / backup-restore / cleanup 入口

### Task 12. Admin page `/operations/cleanup`

文件范围：3-5 个文件

- `admin-web/src/pages/operations/cleanup/cleanup-page.tsx`
- `admin-web/src/pages/operations/cleanup/cleanup-service.ts`
- `admin-web/src/pages/operations/cleanup/cleanup-types.ts`
- `admin-web/src/pages/operations/cleanup/cleanup-page.test.tsx`
- `admin-web/src/router/index.tsx`

完成标准：

- 支持 cleanup 触发
- 支持 job 列表
- 支持 detail / failure item 查看

## Validation

后端：

- `mvn -pl biz/operations/kuzhambu-operations-infra -am test`
- `mvn -pl biz/operations/kuzhambu-operations-application -am test`
- `mvn -pl biz/operations/kuzhambu-operations-interface -am test`

前端：

- `npm --workspace kuzhambu-admin-web run format`
- `npm run format:check`
- `npm run lint`
- `npm run test`

## Exit Criteria

- `/operations/cleanup` 可执行、可分页、可看失败项
- `/operations/tasks` 可看 health summary 与 long task
- 不新增新表，不改菜单种子
- `OPERATIONS-IMPLEMENTATION-COVERAGE.md` 可据此更新
