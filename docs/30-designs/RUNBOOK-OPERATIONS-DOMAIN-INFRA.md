# RUNBOOK-OPERATIONS-DOMAIN-INFRA

## 简介

本 RUNBOOK 用于生成 `Operations` 域的 `domain` 与 `infra` 层首批骨架代码。目标是把已确认的需求、设计和 DDL 转成可实现的领域模型、仓储端口、持久化对象和仓储实现，不在本轮引入 `application` 和 `interface` 细节。

范围只覆盖 `Operations` 自有台账对象：

- `operations_report`
- `operations_backup`
- `operations_restore`
- `operations_cleanup_job`
- `operations_cleanup_item`
- `operations_health_check`
- `operations_long_task_snapshot`

## 关联文件

- [docs/10-requirements/OPERATIONS-REQUIREMENTS.md](./OPERATIONS-REQUIREMENTS.md)
- [docs/30-designs/OPERATIONS-DESIGN.md](./OPERATIONS-DESIGN.md)
- [db/schema/operations.sql](/Users/lizixi/workspace/kuzhambu/db/schema/operations.sql:1)
- [docs/00-governance/SERVERS-ARCHITECTURE.md](../00-governance/SERVERS-ARCHITECTURE.md)
- [docs/00-governance/SERVERS-DATABASE-RULES.md](../00-governance/SERVERS-DATABASE-RULES.md)
- [docs/00-governance/SERVERS-UNIFIED-ID-DESIGN.md](../00-governance/SERVERS-UNIFIED-ID-DESIGN.md)

## 数据结构

| 子域 | 领域实体 | 表 | 主领域标识 | 仓储端口 |
| --- | --- | --- | --- | --- |
| `report` | `ReportRecord` | `operations_report` | `ReportId` | `ReportRepository` |
| `backup` | `BackupRecord` | `operations_backup` | `BackupId` | `BackupRepository` |
| `restore` | `RestoreRecord` | `operations_restore` | `RestoreId` | `RestoreRepository` |
| `cleanup` | `CleanupJob` | `operations_cleanup_job` | `CleanupJobId` | `CleanupJobRepository` |
| `cleanup` | `CleanupItem` | `operations_cleanup_item` | `CleanupItemId` | 由 `CleanupJobRepository` 聚合读写 |
| `health` | `HealthCheckRecord` | `operations_health_check` | `HealthCheckId` | `HealthCheckRepository` |
| `task` | `LongTaskSnapshot` | `operations_long_task_snapshot` | `LongTaskSnapshotId` | `LongTaskSnapshotRepository` |

字段约束：

- 数据库内部主键继续使用 `bigint id`。
- 领域标识使用强类型值对象，不在 `domain` 中直接暴露 `Long`。
- `requester_user_id`、`requested_by_user_id` 属于跨域引用，先保留基础类型，不引入 `System` 域对象复制。
- `operations_cleanup_item` 作为 `CleanupJob` 的聚合内对象处理，不单独暴露跨域仓储端口。
- `Operations` 不创建日志正文、审计正文或聚合统计快照表对应领域对象。

建议包结构：

```text
kuzhambu-servers/biz/operations/
  kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/
    report/
    backup/
    restore/
    cleanup/
    health/
    task/
  kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/
    report/
    backup/
    restore/
    cleanup/
    health/
    task/
```

## 任务块

### 任务块 1：Report Domain 核心

目标：落 `operations_report` 的领域标识、实体和仓储端口。

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/report/model/valueobject/ReportId.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/report/codec/ReportIdCodec.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/report/model/entity/ReportRecord.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/report/repository/ReportRepository.java`

### 任务块 2：Report Infra 落表映射

目标：把 `ReportRecord` 对接到 `operations_report`。

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/persistence/dataobject/ReportDO.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/persistence/mapper/ReportMapper.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/persistence/assembler/ReportPersistenceAssembler.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/repository/impl/ReportRepositoryImpl.java`

### 任务块 3：Backup Domain 核心

目标：落 `operations_backup` 的领域对象和仓储端口。

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/backup/model/valueobject/BackupId.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/backup/codec/BackupIdCodec.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/backup/model/entity/BackupRecord.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/backup/repository/BackupRepository.java`

### 任务块 4：Backup Infra 落表映射

目标：把 `BackupRecord` 对接到 `operations_backup`。

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/backup/persistence/dataobject/BackupDO.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/backup/persistence/mapper/BackupMapper.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/backup/persistence/assembler/BackupPersistenceAssembler.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/backup/repository/impl/BackupRepositoryImpl.java`

### 任务块 5：Restore Domain 核心

目标：落 `operations_restore` 的领域对象和仓储端口。

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/restore/model/valueobject/RestoreId.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/restore/codec/RestoreIdCodec.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/restore/model/entity/RestoreRecord.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/restore/repository/RestoreRepository.java`

### 任务块 6：Restore Infra 落表映射

目标：把 `RestoreRecord` 对接到 `operations_restore`。

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/restore/persistence/dataobject/RestoreDO.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/restore/persistence/mapper/RestoreMapper.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/restore/persistence/assembler/RestorePersistenceAssembler.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/restore/repository/impl/RestoreRepositoryImpl.java`

### 任务块 7：Cleanup Domain 核心

目标：把清理任务建成单聚合，`CleanupJob` 聚合持有 `CleanupItem`。

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/cleanup/model/valueobject/CleanupJobId.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/cleanup/model/valueobject/CleanupItemId.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/cleanup/model/entity/CleanupJob.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/cleanup/model/entity/CleanupItem.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/cleanup/repository/CleanupJobRepository.java`

### 任务块 8：Cleanup Infra 落表映射

目标：同时映射 `operations_cleanup_job` 与 `operations_cleanup_item`。

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/cleanup/persistence/dataobject/CleanupJobDO.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/cleanup/persistence/dataobject/CleanupItemDO.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/cleanup/persistence/mapper/CleanupJobMapper.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/cleanup/persistence/mapper/CleanupItemMapper.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/cleanup/repository/impl/CleanupJobRepositoryImpl.java`

### 任务块 9：Health Domain 核心

目标：落 `operations_health_check` 的领域对象和仓储端口。

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/model/valueobject/HealthCheckId.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/model/entity/HealthCheckRecord.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/repository/HealthCheckRepository.java`

### 任务块 10：Health Infra 落表映射

目标：把 `HealthCheckRecord` 对接到 `operations_health_check`。

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/persistence/dataobject/HealthCheckDO.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/persistence/mapper/HealthCheckMapper.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/persistence/assembler/HealthCheckPersistenceAssembler.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/repository/impl/HealthCheckRepositoryImpl.java`

### 任务块 11：Long Task Domain 核心

目标：落 `operations_long_task_snapshot` 的领域对象和仓储端口。

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/task/model/valueobject/LongTaskSnapshotId.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/task/model/entity/LongTaskSnapshot.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/task/repository/LongTaskSnapshotRepository.java`

### 任务块 12：Long Task Infra 落表映射

目标：把 `LongTaskSnapshot` 对接到 `operations_long_task_snapshot`。

文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/task/persistence/dataobject/LongTaskSnapshotDO.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/task/persistence/mapper/LongTaskSnapshotMapper.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/task/persistence/assembler/LongTaskSnapshotPersistenceAssembler.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/task/repository/impl/LongTaskSnapshotRepositoryImpl.java`

## 收口检查

- 每个表都有唯一对应的领域实体或聚合根。
- 每个领域标识都有独立值对象，不直接在 `domain` 暴露基础类型标识。
- `CleanupItem` 不单独暴露跨域仓储端口。
- `Operations` 不新建任何日志正文、审计正文或聚合统计结果持久化对象。
- 生成代码后按子模块先跑最窄 `spotless:apply`，再跑 `spotless:check` 与 `checkstyle:check`。
