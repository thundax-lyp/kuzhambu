# Audit Design

## Purpose

本文档定义 Audit 模块的数据结构和接口设计。Audit 直接继承 sandwich 的数据审计模型，用于记录业务对象成功提交后的数据变更事实。

Audit 不替代运行日志、访问日志、认证事件日志，也不提供业务数据回滚。

## Module

- 模块名称：Audit
- 业务域：audit
- 对应需求文档：[AUDIT-REQUIREMENTS.md](../10-requirements/AUDIT-REQUIREMENTS.md)
- 后端 biz 子工程：`kuzhambu-servers/biz/kuzhambu-biz-audit`
- 后端 infra 子工程：`kuzhambu-servers/infra/kuzhambu-infra-audit`
- 后台接口入口：`kuzhambu-servers/interfaces/kuzhambu-admin-api`
- 前台接口入口：无通用入口
- 前端入口：`kuzhambu-apps/admin-web`
- Python worker 能力：无

## Business Boundary

- 本模块负责：审计对象坐标、审计元数据、审计日志、审计快照、字段变更摘要和后台审计查询。
- 本模块不负责：运行日志、访问日志、登录登出事件、失败请求、业务数据回滚。
- 依赖的其他业务域能力：Auth/Core 提供当前操作者上下文。
- 对外提供的业务能力：业务写操作审计记录、对象审计历史查询、审计日志筛选。

## Data Model

### audit_meta

保存一个 `AuditObjectRef` 的当前审计状态。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `meta_id` | `metaId` | 是 | 审计元数据 ULID |
| `object_type` | `objectType` | 是 | 被审计对象类型 |
| `object_id` | `objectId` | 是 | 被审计对象 ULID 或稳定标识 |
| `version` | `version` | 是 | 当前审计版本 |
| `last_log_id` | `lastLogId` | 是 | 最后一条审计日志 ULID |
| `last_action` | `lastAction` | 是 | 最后一次动作 |
| `last_operator_type` | `lastOperatorType` | 是 | 最后一次操作者类型 |
| `last_operator_id` | `lastOperatorId` | 否 | 最后一次操作者标识 |
| `last_operator_name` | `lastOperatorName` | 否 | 最后一次操作者显示名 |
| `last_operated_at` | `lastOperatedAt` | 是 | 最后一次操作时间 |
| `created_log_id` | `createdLogId` | 是 | 创建元数据对应审计日志 ULID |
| `created_at` | `createdAt` | 是 | 创建时间 |

约束：
- 唯一约束：`object_type + object_id`。
- `version` 从 1 开始，随每条成功审计日志递增。
- `audit_meta` 不表达业务对象状态。

### audit_log

保存一次成功数据变更事实。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `log_id` | `logId` | 是 | 审计日志 ULID |
| `meta_id` | `metaId` | 是 | 审计元数据 ULID |
| `object_type` | `objectType` | 是 | 被审计对象类型 |
| `object_id` | `objectId` | 是 | 被审计对象标识 |
| `version` | `version` | 是 | 本次审计后的版本 |
| `previous_version` | `previousVersion` | 是 | 本次审计前版本 |
| `action` | `action` | 是 | 审计动作 |
| `idempotency_key` | `idempotencyKey` | 是 | 审计幂等键 |
| `operator_type` | `operatorType` | 是 | 操作者类型 |
| `operator_id` | `operatorId` | 否 | 操作者标识 |
| `operator_name` | `operatorName` | 否 | 操作者显示名 |
| `source` | `source` | 是 | 操作来源 |
| `request_id` | `requestId` | 否 | 请求标识 |
| `trace_id` | `traceId` | 否 | 链路标识 |
| `remote_addr` | `remoteAddr` | 否 | 来源地址 |
| `summary` | `summary` | 否 | 可读摘要 |
| `snapshot_schema_version` | `snapshotSchemaVersion` | 是 | 快照结构版本 |
| `before_snapshot` | `beforeSnapshot` | 否 | 变更前快照 JSON |
| `after_snapshot` | `afterSnapshot` | 否 | 变更后快照 JSON |
| `changed_fields` | `changedFields` | 否 | 字段变更摘要 JSON |
| `occurred_at` | `occurredAt` | 是 | 操作发生时间 |

约束：
- `log_id` 唯一。
- `idempotency_key` 唯一。
- `audit_log` 只追加，不回写既有记录。
- 快照字段只保存脱敏后的审计展示字段。

## Domain Model

- `AuditObjectRef`：`objectType + objectId`，被审计业务对象坐标。
- `AuditMeta`：对象当前审计游标。
- `AuditLog`：一次数据变更事实。
- `AuditSnapshot`：可读快照，不直接序列化完整 domain。
- `AuditChangedField`：字段变更摘要。
- `AuditAction`：`CREATE`、`UPDATE`、`DELETE`、`ENABLE`、`DISABLE`、`ARCHIVE`、`RESTORE`、`BIND`、`UNBIND`、`UPDATE_RELATION`、`RESET_CREDENTIAL`。

## Application Layer

- `AuditApplicationService`：记录审计日志、推进审计元数据、查询对象审计历史。
- `AuditQueryApplicationService`：后台审计日志筛选、审计元数据读取。
- `AuditObjectLoaderRegistry`：按 `objectType` 获取审计对象加载器。
- `AuditSnapshotAssemblerRegistry`：按 `objectType` 获取快照组装器。

事务边界：
- 审计写入与业务写操作同事务提交。
- 审计写入失败时业务写操作回滚。
- 批量业务入口拆解为多个单对象审计事实。

## Interface Layer

后台接口固定使用 `/api/admin/audit/**`。

- `GET /audit/objects/{objectType}/{objectId}/logs`：查询对象审计历史。
- `GET /audit/objects/{objectType}/{objectId}/meta`：读取对象当前审计状态。
- `GET /audit/logs`：筛选审计日志。
- `GET /audit/logs/{logId}`：读取审计日志详情。

筛选条件：
- `objectType`
- `objectId`
- `action`
- `operatorType`
- `operatorId`
- `source`
- `requestId`
- `occurredFrom`
- `occurredTo`

## Infrastructure Layer

- Repository：`AuditMetaRepository`、`AuditLogRepository`。
- Mapper：`AuditMetaMapper`、`AuditLogMapper`。
- PersistenceAssembler：`AuditMetaPersistenceAssembler`、`AuditLogPersistenceAssembler`。
- 外部客户端：无。
- 缓存：默认不缓存审计日志；可缓存审计对象 registry。

## Data Ownership

- 本模块拥有：`audit_meta`、`audit_log`。
- 本模块只读引用：当前认证上下文。
- 禁止跨域直接访问：不得直接读取业务对象 DAO；对象加载通过业务域提供的审计 loader。
- Flyway 脚本归属：`kuzhambu-admin-api` 的 `db/migration/V1__init.sql`，按 Audit 分段。

## Observability

- 运行日志：记录审计写入失败和 registry 缺失。
- 访问日志：由接口层统一记录。
- 审计日志：Audit 自身操作不形成递归业务审计。
- 关键指标：审计写入数、审计失败数、按对象类型统计的审计量。

## Acceptance

- 业务对象创建、更新、删除、启用、禁用、绑定和解绑后可查询审计日志。
- 审计历史按对象查询稳定。
- 审计日志按动作、操作者、对象和时间筛选稳定。
- 敏感字段不进入快照。
- 审计失败时业务事务回滚。
