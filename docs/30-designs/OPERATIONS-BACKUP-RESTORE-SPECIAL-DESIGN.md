# Operations Backup Restore Special Design

## Purpose

本文档定义 kuzhambu 在当前部署前提下的备份与恢复专项设计，覆盖 Docker 单机部署、宿主机本地持久化盘、`bash` 执行器、`mysqldump` 逻辑备份、恢复前挂起写入，以及 `Operations` 台账协同规则。

本文档是 `Operations` 域的专项设计，不替代总设计文档 [`OPERATIONS-DESIGN.md`](./OPERATIONS-DESIGN.md)。

## Scope

覆盖：

- 基于 MySQL 逻辑导出的业务表备份方案。
- Storage 底层文件内容在 `local` 与 `s3` 兼容对象存储两种后端下的备份方案。
- 基于 `bash` 脚本的备份、恢复、恢复前快照和过期清理方案。
- 恢复期间的写入阻断与恢复后补偿动作。
- `operations_backup`、`operations_restore` 与清理台账的协作口径。
- 备份文件命名、目录布局、校验和保留期。

不覆盖：

- 整机、整盘、整容器镜像级灾备。
- `system_*` 与 `operations_*` 表的数据恢复。
- Elasticsearch 索引、缓存、临时文件等可重建运行时状态的直接恢复。
- 对象存储底层物理巡检或跨机房容灾。

## Background

当前 `Operations` 需求要求提供备份、恢复、恢复前快照、30 天保留、恢复期间写入阻断，以及可追溯的备份恢复台账能力。

结合当前部署假设：

- Java servers 与 workers 以 Docker 方式部署。
- MySQL 为主要业务数据真相源。
- 数据卷映射到宿主机本地持久化盘。
- 备份恢复脚本首阶段由 `admin-starter` 容器内执行。
- 首阶段目标是形成最小可用的可执行备份恢复闭环，而不是一步到位实现完整控制面编排。

因此本方案选择：

- 以 `bash` 作为底层执行器。
- 以 `mysqldump` 作为逻辑备份工具。
- 以业务表白名单作为恢复集。
- 明确排除 `system_*` 与 `operations_*`。
- 同时备份业务恢复集对应的 Storage 二进制内容。

## Design Decision

### D1 采用逻辑备份，不采用整盘覆盖恢复

备份恢复采用 MySQL 逻辑备份：

- 备份时只导出业务恢复集中的表。
- 恢复时只导入这些表。
- 不采用整库物理目录覆盖或卷快照直接回滚。

原因：

- 需要在恢复时排除 `system_*` 与 `operations_*`。
- 需要保留 `Operations` 台账，避免恢复时冲掉备份和恢复记录。
- 逻辑备份更适合首阶段用 `bash` 落地。

### D2 恢复集明确排除 `system_*` 与 `operations_*`

本专项设计按当前决策，恢复集不包含：

- 所有 `system_*` 表。
- 所有 `operations_*` 表。

本决策意味着：

- 恢复目标是“业务内容恢复”，不是“整个平台状态回滚”。
- 恢复后 `System` 的用户、权限、会话、审计和日志保持当前现场状态。
- 恢复后 `Operations` 的备份、恢复、清理和健康检查台账保持当前现场状态。

### D3 业务恢复集采用白名单，不采用前缀黑名单拼接

恢复集必须使用显式白名单文件维护，不直接用“排除 `system_*` 与 `operations_*` 后导出全部剩余表”的动态规则。

原因：

- 防止新增表时被误纳入恢复集。
- 防止中间态、临时态或可重建投影被误导出。
- 实现和审查成本更低。

白名单建议覆盖：

- `classics_*`
- `ai_*`
- `discovery_*`
- `knowledge_*`
- `storage_*`
- 其他经确认属于业务真相源的表

具体表名由实现阶段固化为独立清单文件。

### D4 导出产物不进入备份范围

根据需求，导出产物是临时产物，不进入数据备份范围。

因此以下对象不属于本方案恢复目标：

- Classics 导出产物文件。
- Operations 报表导出产物文件。
- workers 请求级临时文件。

## Deployment Assumptions

本方案依赖以下部署假设：

- Docker 单机或单节点部署。
- MySQL 可通过备份执行器访问。
- 应用容器、worker 容器、定时任务和消息消费可以被控制脚本统一停止或挂起。
- 备份文件落在宿主机本地目录或由控制容器挂载的备份目录，可由运维读取。

若未来部署切换为多实例、分布式或托管数据库，本方案需要重新评估。

## Execution Model

### Purpose

本方案采用“`admin-starter` 容器内执行脚本”的模式。

典型形态：

- 容器 A：`admin-starter`
- 容器 B：MySQL 容器
- 容器 C：Portal 或其他业务容器共享的本地文件卷

即：

- `admin-starter` 发起并执行备份恢复脚本
- `admin-starter` 连接 MySQL 容器导出与导入数据库
- `admin-starter` 读取共享本地存储卷或访问 S3 兼容对象存储

### Admin Starter Responsibility

首阶段 `admin-starter` 除承载业务 HTTP 入口外，还负责：

- 执行 `mysqldump`
- 执行 `mysql` 导入
- 归档本地文件卷或执行 S3 兼容同步
- 生成 checksum
- 输出执行结果供 `Operations` 记录

### Access Requirement

`admin-starter` 必须具备：

- 到 MySQL 容器 B 的网络访问能力
- 到备份输出目录的写权限
- 到本地文件卷的只读备份权限和恢复时的写权限
- 访问 S3 兼容对象存储所需的凭证

在 `local` 存储模式下，`admin-starter` 必须满足以下任一条件：

- 与容器 C 共享同一宿主机卷挂载路径
- 通过共享 named volume 访问同一文件根目录

首阶段推荐：

- 优先让 `admin-starter` 与容器 C 共享同一宿主机 volume。
- 避免在首阶段引入独立控制容器。

### Network And Volume Rule

推荐规则：

- `admin-starter` 与 MySQL 在同一 Docker network 中，使用服务名访问数据库。
- `admin-starter` 挂载单独的 backup volume。
- 如使用 `local` 存储，`admin-starter` 与 Portal 共享同一 storage volume。

示意：

```text
admin-starter (A)
  - mount: /backup/kuzhambu
  - mount: /app/storage
  - network: app-net

mysql (B)
  - network: app-net

portal-starter / other app container (C)
  - mount: /app/storage
```

### Suspend And Resume

进入恢复模式时，`admin-starter` 或其上层控制逻辑需要负责编排以下动作：

- 停止写业务数据的 Java 容器
- 停止会消费并写业务事实的 worker 容器
- 停止定时任务容器或调度进程
- 保持 MySQL 容器 B 可读写，但不再接受新的业务写入

因此“挂起系统”不等于停止 MySQL，而是：

- 停止所有业务写入方
- 保留数据库容器供备份和恢复使用

恢复完成后，再由 A 统一恢复相关容器。

## Backup Boundary

### Included Data

备份范围包含两部分：

- 业务恢复集白名单中的 MySQL 表。
- 这些业务表引用到的 Storage 二进制内容。

这些表用于恢复：

- 古籍内容主事实。
- AI 候选与调用相关业务事实。
- Discovery 搜索和问答相关业务事实。
- Knowledge 标签、图谱与精修相关业务事实。
- Storage 对象元数据与引用关系。

### Excluded Data

以下数据明确排除：

- `system_*`
- `operations_*`
- 导出产物文件
- 报表文件
- 缓存
- 搜索索引
- workers 临时文件
- 可通过业务真相源重建的投影或派生运行时状态

## Storage Backend Backup Design

### Purpose

本方案中的业务恢复不只恢复数据库表，还必须恢复与业务表关联的文件内容。否则会出现：

- `storage_object` 元数据已恢复，但底层文件不存在。
- Classics、Knowledge 或其他业务页面能查到文件对象，但读取内容 404。

因此备份恢复必须同时覆盖：

- `storage_*` 元数据表。
- 底层存储中的业务文件内容。

### Common Rules

无论底层存储类型是 `local` 还是 `s3` 兼容对象存储，都必须满足：

- 恢复集内业务数据引用到的文件内容必须可恢复。
- 备份时必须在系统挂起后进行，保证数据库引用关系与文件内容处于同一静止窗口。
- 恢复时必须先恢复数据库业务表，再恢复文件内容，或两者在同一恢复窗口内完成。
- 恢复完成后，必须执行文件可读性抽样校验。

### Local Storage Mode

当 `kuzhambu.oss.type=local` 时，底层文件内容位于宿主机目录。

在这种模式下，备份对象包括：

- MySQL 业务恢复集 SQL 文件。
- 本地存储根目录下的业务文件内容目录。

建议做法：

- 在恢复模式下由 `admin-starter` 执行 `mysqldump`。
- 由 `admin-starter` 对共享的本地存储根目录执行目录级归档，例如 `tar.gz`。
- 为该归档生成独立 `sha256` 校验文件。

建议文件命名：

```text
backup_20260629-153045.sql
backup_20260629-153045.storage.tar.gz
backup_20260629-153045.storage.tar.gz.sha256
```

恢复时：

1. 清理业务恢复集数据库表。
2. 导入 SQL。
3. 清理并回写本地存储根目录中的业务文件内容。
4. 执行文件抽样读取验证。

本地存储模式的注意事项：

- 备份输出目录不得位于被归档的本地存储根目录内部，避免递归打包。
- 如本地存储目录中同时存在临时目录或非业务文件，应由实现阶段明确排除规则。
- 若本地文件内容由容器 C 提供，`admin-starter` 与 C 必须共享同一个 volume 视图，避免脚本归档到的目录与业务容器实际读取目录不一致。

### S3 Compatible Storage Mode

当 `kuzhambu.oss.type=s3` 时，底层文件内容位于 S3 兼容对象存储。

在这种模式下，不再对宿主机目录做归档，而是对对象存储执行对象级备份。

建议做法：

- 在恢复模式下由 `admin-starter` 执行 `mysqldump`。
- 对业务对象所在 bucket 或 prefix 执行同步备份。
- 生成一份对象清单 `manifest`，记录：
  - object key
  - size
  - checksum 或 etag
  - backup timestamp

建议文件命名：

```text
backup_20260629-153045.sql
backup_20260629-153045.storage-manifest.json
```

对象内容本身可以采用以下任一模式：

- 复制到专用备份 bucket。
- 同步到专用备份 prefix。
- 下载到宿主机归档目录再二次封装。

首阶段推荐：

- 优先使用“同步到专用备份 bucket 或 prefix”。
- manifest 只记录这次备份包含的对象信息，不重复保存业务表元数据。

恢复时：

1. 清理业务恢复集数据库表。
2. 导入 SQL。
3. 按 manifest 将对象内容回写到目标 bucket 或 prefix。
4. 执行文件抽样读取验证。

### Why Storage Needs Independent Backup

`storage_*` 表只保存文件对象元数据、引用关系和读取语义，不保存文件二进制内容。

因此仅有 SQL 备份并不足以完成业务恢复，必须额外恢复：

- 本地文件根目录内容，或
- S3 兼容对象存储中的对象内容

## Backup Artifact Set

一次完整备份的产物集合包含：

- 数据库 SQL 文件
- 数据库 SQL checksum
- 存储内容备份产物
- 存储内容备份 checksum 或 manifest

在 `local` 模式下：

```text
backup_20260629-153045.sql
backup_20260629-153045.sql.sha256
backup_20260629-153045.storage.tar.gz
backup_20260629-153045.storage.tar.gz.sha256
```

在 `s3` 兼容模式下：

```text
backup_20260629-153045.sql
backup_20260629-153045.sql.sha256
backup_20260629-153045.storage-manifest.json
```

如采用“下载到本地再归档”的 S3 备份变体，也可附加本地归档文件和 checksum。

## File Naming And Layout

### Backup Filename

标准备份文件名：

```text
backup_yyyyMMdd-HHmmss.sql
```

示例：

```text
backup_20260629-153045.sql
```

恢复前快照文件名：

```text
prerestore_yyyyMMdd-HHmmss.sql
```

示例：

```text
prerestore_20260629-160500.sql
```

### Companion Files

每个 SQL 文件应生成伴随校验文件：

```text
backup_20260629-153045.sql.sha256
prerestore_20260629-160500.sql.sha256
```

### Directory Layout

建议宿主机目录：

```text
/backup/kuzhambu/
  backup_20260629-153045.sql
  backup_20260629-153045.sql.sha256
  prerestore_20260629-160500.sql
  prerestore_20260629-160500.sql.sha256
```

如需扩展，可在后续引入 `archive/` 子目录，但首阶段不是必需条件。

## Backup Execution Design

### Input

备份脚本输入：

- MySQL 连接信息。
- 业务恢复集白名单文件路径。
- 备份输出目录。
- 备份类型：`AUTO`、`MANUAL` 或 `PRE_RESTORE`。
- 当前存储类型：`local` 或 `s3`
- `local` 模式下的存储卷挂载路径，或 `s3` 模式下的 bucket / prefix 信息。

### Flow

标准备份流程：

1. 生成时间戳。
2. 校验输出目录存在且可写。
3. 读取业务恢复集白名单。
4. 执行 `mysqldump` 导出这些表。
5. 根据当前 OSS 类型执行存储内容备份。
6. 写出 `backup_yyyyMMdd-HHmmss.sql` 或 `prerestore_yyyyMMdd-HHmmss.sql`。
7. 为 SQL 与存储备份产物生成校验信息。
8. 记录文件大小、对象数量或 manifest 信息。
9. 返回文件名、大小、checksum、开始时间、完成时间与执行结果。

### Command Shape

命令形态：

```bash
mysqldump -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" -p"${DB_PASSWORD}" \
  "${DB_NAME}" ${TABLES} > "${OUTPUT_FILE}"
```

实现阶段可按数据库版本补充一致性参数，例如：

- `--single-transaction`
- `--set-gtid-purged=OFF`
- `--default-character-set=utf8mb4`

但不在本文档中强行固化到某个具体版本参数集。

## Restore Execution Design

### Restore Goal

恢复的目标是将业务表恢复到某个备份时刻的状态，同时保留当前 `system_*` 与 `operations_*`。

### Precondition

恢复前必须满足：

- 有合法可读的备份 SQL 文件。
- 有对应 `sha256` 校验文件。
- 有与本次备份对应的存储内容备份产物。
- 当前系统已进入恢复模式。
- 当前现场已创建恢复前快照。

### Restore Mode

恢复模式的最小实现是“挂起所有业务写入”。

首阶段采用 `admin-starter` 上层控制逻辑或宿主机统一编排：

- 停止 Java admin starter。
- 停止 Java portal starter。
- 停止会写业务事实的 worker 或消费容器。
- 停止会写业务事实的定时任务。

只要仍有任何组件可写业务数据，就不视为进入恢复模式。

### Flow

标准恢复流程：

1. 管理员发起恢复。
2. 系统创建一条 `operations_restore` 运行中记录。
3. 挂起应用写入，进入恢复模式。
4. 执行一次 `PRE_RESTORE` 备份。
5. 校验待恢复 SQL 文件、存储备份产物和 checksum / manifest。
6. 清理业务恢复集中的现有数据。
7. 导入待恢复 SQL 文件。
8. 恢复存储内容。
9. 执行恢复后补偿动作。
10. 解除恢复模式，恢复应用运行。
11. 将 `operations_restore` 标记为成功或失败。

### Import Shape

命令形态：

```bash
mysql -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" -p"${DB_PASSWORD}" \
  "${DB_NAME}" < "${INPUT_FILE}"
```

恢复脚本不得导入任何 `system_*` 或 `operations_*` 表内容，因为备份 SQL 中本身不应包含这些表。

## Data Cleanup Before Import

为避免历史数据残留，恢复前应清理业务恢复集中的当前数据。

首阶段推荐策略：

- 在恢复模式下，对业务恢复集中的表执行有序清空。
- 再导入备份 SQL。

实现时必须考虑外键约束、表间依赖和清理顺序。

如后续发现部分表适合 `DROP + CREATE` 或 `TRUNCATE`，可在实现阶段细化，但对外语义保持“先清理，再导入”。

恢复存储内容前，也应处理已有文件内容残留。

处理原则：

- `local` 模式：在受控目录内先清理旧业务文件，再回写归档内容。
- `s3` 模式：在目标 prefix 或 bucket 范围内按 manifest 对齐，避免旧对象残留影响读取结果。

若采用 A、B、C 分容器模型，还必须满足：

- A 清理和回写到的本地目录，正是 C 运行时实际挂载的业务目录。
- A 恢复完成后，C 重新启动时不需要再做二次文件迁移。

## Post Restore Compensation

恢复完成后，必须执行补偿动作，确保派生状态与业务真相源重新一致。

至少包括：

- 重建 Discovery 搜索索引。
- 清理或刷新缓存。
- 重建必要的统计或投影数据。
- 校验关键业务表可读。
- 校验关键文件对象内容可读。

必要时可加入：

- MQ 积压处理。
- 长任务状态矫正。
- Portal 关键页面冒烟检查。

## Retention And Cleanup

### Retention

备份保留期为 30 天。

超出保留期的备份文件和恢复前快照文件应自动清理。

### Cleanup Scope

清理对象包括：

- `backup_*.sql`
- `backup_*.sql.sha256`
- `backup_*.storage.tar.gz`
- `backup_*.storage.tar.gz.sha256`
- `backup_*.storage-manifest.json`
- `prerestore_*.sql`
- `prerestore_*.sql.sha256`
- `prerestore_*.storage.tar.gz`
- `prerestore_*.storage.tar.gz.sha256`
- `prerestore_*.storage-manifest.json`

### Cleanup Record

清理动作应记录到 `Operations` 清理台账中，至少包含：

- 清理类型：`EXPIRED_BACKUP`
- 发起方式：自动或手动
- 扫描总数
- 删除成功数
- 删除失败数
- 单项失败原因

## Operations Collaboration

### Backup Record

`operations_backup` 负责记录：

- `backupType`
- `backupStatus`
- `storageObjectId` 或本地文件定位信息
- `fileName`
- `fileSizeBytes`
- `checksum`
- `failureReason`
- `requesterUserId`
- `startedAt`
- `completedAt`
- `expiresAt`

如后续字段允许扩展，建议增加：

- `storageBackupType`
- `storageManifestFilename`
- `storageArtifactFilename`
- `storageObjectCount`

建议备份类型枚举：

- `AUTO`
- `MANUAL`
- `PRE_RESTORE`

### Restore Record

`operations_restore` 负责记录：

- `backupId`
- `preRestoreBackupId`
- `restoreStatus`
- `writeBlockEnabled`
- `failureReason`
- `requesterUserId`
- `startedAt`
- `completedAt`

### Control Plane Rule

`Operations` 在本方案中扮演控制面：

- 记录发生过哪些备份和恢复动作。
- 记录恢复前快照。
- 提供后续 admin 查询入口。

`Operations` 自身台账不参与业务恢复覆盖。

## Failure Handling

### Backup Failure

备份失败时：

- 保留失败记录。
- 不生成成功状态台账。
- 若输出文件不完整，应立即删除损坏文件并记录失败原因。

### Restore Failure

恢复失败时：

- 保留失败状态的 `operations_restore` 记录。
- 保留本次 `PRE_RESTORE` 快照。
- 允许管理员基于恢复前快照回退。

### Partial Recovery

若恢复后补偿动作失败：

- 业务数据已恢复成功，但系统未完全回到可服务状态。
- 应将恢复记录标记为失败或部分失败，并记录失败原因。
- 不得静默视为成功。

若数据库已恢复但存储内容恢复失败：

- 应视为恢复失败或部分失败。
- 不得将该次恢复标记为成功。
- 必须记录失败发生在 SQL 导入后还是存储内容恢复阶段。

## Risks And Trade-offs

本方案的主要权衡：

- 优点：
  - 实现简单，适合首阶段快速落地。
  - 能明确排除 `system_*` 与 `operations_*`。
  - 恢复覆盖范围可审查、可控制。
- 代价：
  - 不是全平台灾备。
  - 依赖人工或脚本正确维护业务表白名单。
- 依赖脚本正确处理 `local` 与 `s3` 两类底层存储备份。
- 依赖 `admin-starter` 上层控制逻辑正确编排其他容器的暂停、恢复与卷访问。
- 对多实例、分布式写入和跨节点一致性支持有限。

## Rollout Plan

建议实施顺序：

1. 固化业务恢复集白名单文件。
2. 编写 `bash` 备份脚本。
3. 固化 `local` 与 `s3` 兼容存储的备份实现分支。
4. 编写 `bash` 恢复脚本。
5. 编写 30 天清理脚本。
6. 在测试环境完成一次完整“备份 -> 恢复前快照 -> 恢复 -> 补偿动作”演练。
7. 再将脚本接入 `Operations` application/interface。

## Acceptance

满足以下条件时，本专项设计视为完成实现准备：

- 能生成 `backup_yyyyMMdd-HHmmss.sql` 文件。
- 能生成 `prerestore_yyyyMMdd-HHmmss.sql` 文件。
- 备份与恢复均只作用于业务恢复集白名单。
- `system_*` 与 `operations_*` 不被恢复覆盖。
- `local` 模式下能恢复底层文件目录内容。
- `s3` 兼容模式下能恢复对象内容或完成对象级同步回写。
- 恢复前能挂起系统写入。
- 恢复后能执行索引和缓存补偿动作。
- `Operations` 能追溯备份、恢复和清理动作结果。

## Related Documents

- [OPERATIONS-REQUIREMENTS.md](../10-requirements/OPERATIONS-REQUIREMENTS.md)
- [OPERATIONS-DESIGN.md](./OPERATIONS-DESIGN.md)
- [WORKERS-REQUIREMENTS.md](../10-requirements/WORKERS-REQUIREMENTS.md)
- [STORAGE-REQUIREMENTS.md](../10-requirements/STORAGE-REQUIREMENTS.md)
