# Database Rules

## Purpose

本文档定义 kuzhambu 的工程级数据库红线。具体业务表结构仍以各模块设计文档和 Flyway 脚本为准。

## Platform

- 数据库固定使用 MySQL 8.x。
- 存储引擎固定使用 InnoDB。
- 字符集固定使用 `utf8mb4`。
- 绝对时间点字段统一使用 `datetime(3)`。
- 数据库内部主键默认使用 `bigint`。
- 布尔字段统一使用 `tinyint(1)`。
- 金额字段统一使用 `decimal(18,2)`。
- 枚举字段统一使用 `varchar`。

## Naming

- 主键列默认命名为 `id`。
- Core 表固定使用 `sys_` 前缀。
- Auth 表固定使用 `auth_` 前缀。
- 其他业务域表默认使用业务域前缀，例如 `storage_`、`audit_`、`search_`。
- 业务域前缀必须与需求文档和模块设计文档中的业务域名称保持一致。
- 关系表名称必须显式表达关系语义，例如 `sys_user_role`、`sys_role_menu`。
- 审计日志表由 Audit 业务域定义，不在各业务表中机械追加通用审计字段。
- 业务发生时间使用业务语义命名，例如 `occurred_at`、`requested_at`、`completed_at`、`expires_at`。

## Table Types

- 主数据表：保存后台维护的主数据和配置。
- 运行时业务表：保存业务主状态和必要领域时间字段，不机械补齐无意义通用字段。
- 关系表：只保存关系本身的最小字段，关系唯一性用联合唯一约束表达。
- 台账表：只追加，不回写历史。
- 认证事件表：只追加，不保存敏感明文。
- 审计日志表：归属 Audit 业务域，只追加，不逻辑删除，不保存敏感明文。

## Field Rules

- 是否增加时间字段取决于对象是否有独立生命周期。
- Java 中绝对时间点统一使用 `Instant`。
- `infra` 负责 `Instant` 与数据库 `datetime(3)` 的 UTC 转换。
- `LocalDateTime` 不作为跨模块和持久化真相时间。
- 业务表不得设置通用审计字段 `created_at`、`updated_at`、`deleted_at`、`created_by`、`updated_by`、`deleted_by`。
- 业务确实需要表达用户或生命周期时，必须使用业务语义字段名，例如 `owner_user_id`、`requester_user_id`、`occurred_at`。

## Relationship Rules

- 默认不强制数据库外键。
- 关联一致性由 application 和 domain 保证。
- 跨域引用只保存必要业务键、ULID 或快照，不复制对端主表结构。
- 跨域访问不得直接读取或写入其他业务域的 DAO、Mapper 或底层表。

## Index And Uniqueness

- 主键必须唯一。
- 稳定业务键必须建立唯一约束。
- 联合唯一约束必须直接表达业务不变量。
- 高频查询条件必须有显式索引。
- 状态和时间类批处理查询必须建立组合索引。
- 关系表必须对关系两端业务标识建立联合唯一约束。

## Storage Rules

- 非高频过滤的集合结构可以使用 `json`。
- 审计摘要可以使用 `json`。
- 不保存明文密码、明文 token、明文密钥、明文验证码。
- 哈希类字段只保存哈希结果。
- access token、refresh token、分享 token、下载 token 等明文只允许返回一次，不得持久化明文。

## Cache Boundary

- 正式业务数据以数据库为真相源。
- 缓存只能保存数据库结果的派生读模型，不能替代数据库。
- 运行时短期对象可以只走 Redis / cache，但必须在模块设计文档中声明为运行态对象。
- DTO、VO、值对象、聚合视图、命令返回模型默认不建表。

## Document Requirements

- 涉及数据库结构的模块设计文档必须遵守本文档。
- 文档中的表名、字段名、索引名必须与后续 DDL、DO、Mapper 保持一致。
- 模块设计文档如果偏离本文档，必须在该文档中写明业务原因。
