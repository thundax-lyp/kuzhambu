# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `operations-domain/backup`：生成 Backup Domain 核心
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DOMAIN-INFRA.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/backup/`
    - 处理动作：创建 `BackupId`、`BackupIdCodec`、`BackupRecord` 和 `BackupRepository`
    - 验收点：`operations_backup` 对应的领域标识、实体和仓储端口完整落地
    - 重要度：9/10

- [ ] `operations-infra/backup`：生成 Backup Infra 落表映射
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DOMAIN-INFRA.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/backup/`
    - 处理动作：创建 `BackupDO`、`BackupMapper`、`BackupPersistenceAssembler` 和 `BackupRepositoryImpl`
    - 验收点：`BackupRecord` 已能映射到 `operations_backup` 表结构
    - 重要度：9/10

- [ ] `operations-domain/restore`：生成 Restore Domain 核心
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DOMAIN-INFRA.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/restore/`
    - 处理动作：创建 `RestoreId`、`RestoreIdCodec`、`RestoreRecord` 和 `RestoreRepository`
    - 验收点：`operations_restore` 对应的领域标识、实体和仓储端口完整落地
    - 重要度：9/10

- [ ] `operations-infra/restore`：生成 Restore Infra 落表映射
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DOMAIN-INFRA.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/restore/`
    - 处理动作：创建 `RestoreDO`、`RestoreMapper`、`RestorePersistenceAssembler` 和 `RestoreRepositoryImpl`
    - 验收点：`RestoreRecord` 已能映射到 `operations_restore` 表结构
    - 重要度：9/10

- [ ] `operations-domain/cleanup`：生成 Cleanup Domain 核心
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DOMAIN-INFRA.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/cleanup/`
    - 处理动作：创建 `CleanupJobId`、`CleanupItemId`、`CleanupJob`、`CleanupItem` 和 `CleanupJobRepository`
    - 验收点：`CleanupJob` 聚合及其聚合内 `CleanupItem` 领域模型完整落地
    - 重要度：8/10

- [ ] `operations-infra/cleanup`：生成 Cleanup Infra 落表映射
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DOMAIN-INFRA.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/cleanup/`
    - 处理动作：创建清理任务与清理单项的 DO、Mapper 和 `CleanupJobRepositoryImpl`
    - 验收点：`CleanupJob` 聚合已能同时映射 `operations_cleanup_job` 与 `operations_cleanup_item`
    - 重要度：8/10

- [ ] `operations-domain/health`：生成 Health Domain 核心
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DOMAIN-INFRA.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/`
    - 处理动作：创建 `HealthCheckId`、`HealthCheckRecord` 和 `HealthCheckRepository`
    - 验收点：`operations_health_check` 对应的领域标识、实体和仓储端口完整落地
    - 重要度：7/10

- [ ] `operations-infra/health`：生成 Health Infra 落表映射
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DOMAIN-INFRA.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/`
    - 处理动作：创建 `HealthCheckDO`、`HealthCheckMapper`、`HealthCheckPersistenceAssembler` 和 `HealthCheckRepositoryImpl`
    - 验收点：`HealthCheckRecord` 已能映射到 `operations_health_check` 表结构
    - 重要度：7/10

- [ ] `operations-domain/task`：生成 Long Task Domain 核心
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DOMAIN-INFRA.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/task/`
    - 处理动作：创建 `LongTaskSnapshotId`、`LongTaskSnapshot` 和 `LongTaskSnapshotRepository`
    - 验收点：`operations_long_task_snapshot` 对应的领域标识、实体和仓储端口完整落地
    - 重要度：7/10

- [ ] `operations-infra/task`：生成 Long Task Infra 落表映射
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DOMAIN-INFRA.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/task/`
    - 处理动作：创建 `LongTaskSnapshotDO`、`LongTaskSnapshotMapper`、`LongTaskSnapshotPersistenceAssembler` 和 `LongTaskSnapshotRepositoryImpl`
    - 验收点：`LongTaskSnapshot` 已能映射到 `operations_long_task_snapshot` 表结构
    - 重要度：7/10

## 待讨论项
