# RUNBOOK-OPERATIONS-BACKUP-RESTORE

## Purpose

本文档用于指导 `Operations` 备份恢复能力的分步落地，覆盖专项设计收敛、脚本实现、部署挂载和后续 Java 编排接入。

本文档是执行手册，不替代最终专项设计文档。任务关闭后，应删除本 RUNBOOK。

## Scope

本 RUNBOOK 只覆盖当前已确认的首阶段方案：

- 备份恢复脚本由 `admin-starter` 容器内执行。
- 使用 `mysqldump` 做业务表白名单逻辑备份。
- 恢复集明确排除 `system_*` 与 `operations_*`。
- 同时备份 `Storage` 底层文件内容。
- 支持 `local` 与 `s3` 两种底层存储模式。
- 最终由 `admin-web` 提供管理端触发与结果查看入口。

不覆盖：

- 整机、整盘或整库物理回滚。
- 多节点分布式部署。
- `Operations` Java application / interface 的最终控制台入口。

## Final Decision

当前阶段固定采用以下口径：

- 执行主体：`admin-starter`
- 数据库备份方式：业务表白名单 `mysqldump`
- 本地文件备份方式：共享 `storage-data` 卷归档
- S3 备份方式：对象同步或对象归档，首阶段脚本以 `aws` CLI 为前提
- 备份文件命名：`backup_yyyyMMdd-HHmmss.sql`
- 恢复前快照命名：`prerestore_yyyyMMdd-HHmmss.sql`
- 恢复前必须挂起写业务数据的容器或调度
- 恢复后必须执行索引、缓存和文件可读性补偿检查

## Data Structure Changes

当前阶段的数据结构影响分为两类。

### 1. 已存在但要被消费的表

这些表不新增，但会被备份恢复流程显式使用：

- `operations_backup`
- `operations_restore`
- `operations_cleanup_job`
- `operations_cleanup_item`
- `storage_object`
- `storage_object_reference`

含义：

- `operations_*` 台账表用于记录动作，但不进入恢复覆盖集
- `storage_*` 表进入业务恢复集，并要求与底层文件内容一致

### 2. 当前不新增数据库表

本轮脚本化落地不引入新的数据库表。

当前新增的是文件级执行资产：

- 业务表白名单文件
- 备份脚本
- 恢复脚本
- 清理脚本

如果后续需要让 `Operations` 更完整地追踪文件级备份产物，可再评估是否扩展 `operations_backup` 字段，例如：

- `storageBackupType`
- `storageArtifactFilename`
- `storageManifestFilename`
- `storageObjectCount`

这些字段目前只是后续可选项，不属于本轮必须变更。

## Related Files

专项设计：

- [OPERATIONS-BACKUP-RESTORE-SPECIAL-DESIGN.md](/Volumes/storage/workspace/kuzhambu/docs/30-designs/OPERATIONS-BACKUP-RESTORE-SPECIAL-DESIGN.md:1)
- [OPERATIONS-DESIGN.md](/Volumes/storage/workspace/kuzhambu/docs/30-designs/OPERATIONS-DESIGN.md:1)

部署与脚本：

- [docker-compose.yml](/Volumes/storage/workspace/kuzhambu/deploy/docker-compose.yml:1)
- [.env.example](/Volumes/storage/workspace/kuzhambu/deploy/.env.example:1)
- [README.md](/Volumes/storage/workspace/kuzhambu/deploy/README.md:1)
- [backup-business-data.sh](/Volumes/storage/workspace/kuzhambu/deploy/scripts/backup-business-data.sh:1)
- [restore-business-data.sh](/Volumes/storage/workspace/kuzhambu/deploy/scripts/restore-business-data.sh:1)
- [cleanup-backups.sh](/Volumes/storage/workspace/kuzhambu/deploy/scripts/cleanup-backups.sh:1)
- [backup-lib.sh](/Volumes/storage/workspace/kuzhambu/deploy/scripts/backup-lib.sh:1)
- [business-table-whitelist.txt](/Volumes/storage/workspace/kuzhambu/deploy/scripts/business-table-whitelist.txt:1)

前端与管理入口：

- `kuzhambu-apps/admin-web/src`
- `kuzhambu-servers/biz/operations/`
- `system` 菜单与权限种子数据

关联规则与需求：

- [OPERATIONS-REQUIREMENTS.md](/Volumes/storage/workspace/kuzhambu/docs/10-requirements/OPERATIONS-REQUIREMENTS.md:1)
- [STORAGE-REQUIREMENTS.md](/Volumes/storage/workspace/kuzhambu/docs/10-requirements/STORAGE-REQUIREMENTS.md:1)
- [STORAGE-DESIGN.md](/Volumes/storage/workspace/kuzhambu/docs/30-designs/STORAGE-DESIGN.md:1)

## Full Flow

备份流程：

1. 管理员在 `admin-web` 发起备份请求。
2. `Operations` application 校验权限并记录运行中台账。
3. `admin-starter` 读取业务表白名单。
4. 连接 `mysql` 执行 `mysqldump`。
5. 按当前 `KUZHAMBU_OSS_TYPE` 备份底层文件内容。
6. 生成 SQL 和存储归档的 `sha256`。
7. 输出到 `${KUZHAMBU_BACKUP_ROOT_PATH}`。
8. 管理端可查看备份结果和失败原因。

恢复流程：

1. 管理员在 `admin-web` 发起恢复请求。
2. `Operations` application 记录恢复中的台账并进入恢复模式。
3. 挂起写业务数据的容器和任务。
4. 先执行一次 `PRE_RESTORE` 备份。
5. 校验 SQL 文件和存储归档 checksum。
6. 清空业务恢复集中的现有表数据。
7. 导入 SQL。
8. 回写底层文件内容。
9. 执行索引、缓存和文件可读性补偿动作。
10. 解除恢复模式。
11. 管理端可查看恢复结果、失败原因和 `PRE_RESTORE` 快照记录。

## Task Split

该任务已拆为 6 个小任务，建议按顺序推进。

### Task 1: 固化设计与执行口径

范围对象：

- `docs/30-designs/OPERATIONS-BACKUP-RESTORE-SPECIAL-DESIGN.md`
- `docs/30-designs/RUNBOOK-OPERATIONS-BACKUP-RESTORE.md`

处理动作：

- 收敛首阶段执行主体、恢复边界、存储模式和命名规则

验收点：

- 专项设计和 RUNBOOK 对执行主体、恢复边界、存储模式口径一致

### Task 2: 落地脚本与白名单

范围对象：

- `deploy/scripts/backup-business-data.sh`
- `deploy/scripts/restore-business-data.sh`
- `deploy/scripts/cleanup-backups.sh`
- `deploy/scripts/backup-lib.sh`
- `deploy/scripts/business-table-whitelist.txt`

处理动作：

- 实现 `backup / restore / cleanup` 的 bash 执行骨架

验收点：

- 脚本可通过 `bash -n`
- 白名单文件不包含 `system_*` 和 `operations_*`

### Task 3: 对齐 Docker Compose 挂载

范围对象：

- `deploy/docker-compose.yml`
- `deploy/.env.example`
- `deploy/README.md`

处理动作：

- 让 `admin-starter` 拥有执行脚本、访问备份目录和共享存储卷的能力

验收点：

- `admin-starter` 挂载 `storage-data`
- `admin-starter` 挂载 `backup-data`
- `admin-starter` 挂载 `deploy/scripts`

### Task 4: 接入 Java 编排与台账

范围对象：

- `kuzhambu-servers/biz/operations/`
- `kuzhambu-servers/starter/kuzhambu-admin-starter/`

处理动作：

- 在 `Operations` application 中增加脚本调用、恢复模式编排和台账写入闭环

验收点：

- 管理端可触发备份和恢复
- `operations_backup` / `operations_restore` 可追溯
- 恢复失败时保留 `PRE_RESTORE` 快照记录

### Task 5: 接入 Admin Web 页面

范围对象：

- `kuzhambu-apps/admin-web/src`
- `kuzhambu-servers/biz/operations/`

处理动作：

- 为备份、恢复、清理和结果查看补齐 `admin-web` 页面、服务调用和状态展示

验收点：

- 管理员可在 `admin-web` 触发备份
- 管理员可在 `admin-web` 触发恢复
- 管理员可在 `admin-web` 查看台账、失败原因和快照记录

### Task 6: 补齐菜单与权限种子数据

范围对象：

- `system` 菜单种子数据
- `system` 权限点种子数据
- `admin-web` 菜单与路由映射

处理动作：

- 为备份、恢复、清理和健康检查相关页面补齐菜单入口与权限点

验收点：

- 管理员登录后可看到 `Operations` 下的备份恢复相关菜单
- 菜单权限与后端接口权限点一致
- 页面可由菜单稳定进入

## Verify Checklist

当前阶段最小检查清单：

- `bash -n deploy/scripts/*.sh`
- 核对白名单是否排除 `system_*` 和 `operations_*`
- 核对 `docker-compose.yml` 是否将脚本和卷挂给 `admin-starter`
- 核对专项设计与 RUNBOOK 口径是否一致

后续接入 Java 编排后的最小检查清单：

- 菜单种子数据已补齐并能生成运行时菜单
- `admin-web` 可见备份与恢复入口
- 管理端触发备份一次
- 管理端触发恢复前快照一次
- 测试环境完成一次真实恢复演练
- 恢复后验证搜索重建和文件读取

## Closure Rule

当以下条件满足时，可删除本 RUNBOOK：

- `Operations` 备份恢复入口完成 Java 编排闭环
- `admin-web` 已具备稳定的备份恢复操作入口
- 部署与脚本路径稳定
- 恢复演练已形成稳定验证入口
- 专项设计文档已足以作为长期实现依据
