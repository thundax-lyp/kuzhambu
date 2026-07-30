# Database Rules

## Purpose

本文档定义 kuzhambu 的工程级数据库红线。当前项目仍处于初建阶段，具体业务表结构以 `db/schema/` 和 `db/data/` 为准，不维护增量迁移脚本。

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
- 业务域表默认使用业务域前缀。
- 当前业务域表前缀固定为 `system_`、`storage_`、`classics_`、`ai_`、`knowledge_`、`discovery_`、`operations_`。
- 业务域前缀必须与需求文档和模块设计文档中的业务域名称保持一致。
- 关系表名称必须显式表达关系语义，例如 `system_user_role`、`system_role_menu`。
- 审计日志表由 system 业务域定义，不在各业务表中机械追加通用审计字段。
- 操作者、创建者、更新者、删除者、发起人等审计归属不进入业务表，由 system 审计系统记录。
- 业务发生时间使用业务语义命名，例如 `occurred_at`、`requested_at`、`completed_at`、`expires_at`。

## SQL Files

- 当前阶段固定采用重建式初始化：先删除目标库中本项目业务表，再执行 `db/schema/` 下的业务域建表脚本，最后执行 `db/data/` 下的初始化数据脚本。
- 本地开发、联调和 E2E 冒烟需要额外数据时，在基础数据导入后再执行 `db/data/test.sql`。
- 当前阶段不得新增 Flyway、Liquibase 或手写 migration 脚本；表结构变更必须直接更新对应业务域的 `db/schema/*.sql`，初始化数据变更必须直接更新对应业务域的 `db/data/*.sql`。
- 只有项目进入需要保留历史数据的迁移阶段，并由治理文档明确调整规则后，才允许引入增量迁移目录和迁移脚本。
- 业务域 schema 文件固定放在 `db/schema/`。
- 业务域初始化数据文件固定放在 `db/data/`。
- 复杂初始化数据允许在 `db/data-source/` 维护结构化源文件，再由仓库级脚本生成 `db/data/` 下的 SQL 产物。
- 当前 system 初始化数据源为 `db/data-source/system.json`，生成脚本为 `scripts/generate-system-data-sql.ts`。
- 修改 system 初始化数据时必须先改 JSON 源，再重新生成并提交 `db/data/system.sql`。
- system 初始化数据的自增主键由生成脚本按表从 1 顺序分配；SQL 产物必须在显式插入后追加 `ALTER TABLE ... AUTO_INCREMENT = max(id) + 1`。
- 当前 AI 默认提示词数据源为 `db/data-source/ai-prompts/`，生成脚本为 `scripts/generate-ai-data-sql.ts`。
- 修改 AI 默认提示词时必须先改对应提示词目录下的 `meta.json`、`system-template.txt` 或 `user-template.txt`，再重新生成并提交 `db/data/ai.sql`。
- AI 默认提示词目录允许维护 `sample.md` 作为人工运行样例；`sample.md` 不进入 SQL 产物。
- 当前三才图会稿件初始化数据源为 `db/data-source/sancai-tree.json`，生成脚本为 `scripts/classics-json-to-sql.sh`；不得从运行库反向导出 JSON 作为新真相源。
- 当前三才图会标签初始化数据源为 `db/data-source/sancai-tags.json`，生成脚本为 `scripts/generate-sancai-knowledge-data-sql.mjs`。
- 修改三才图会稿件、标签库、标签别名或稿件标签绑定时必须先改 `db/data-source/` 下对应 JSON 源，再重新生成并提交 `db/data/classics.sql` 和/或 `db/data/knowledge.sql`；`scripts/classics-json-to-sql.sh` 必须从 `db/data-source/sancai-tags.json` 解析 `classics_content_tag.tag_id`。
- 当前 schema 文件固定为 `system.sql`、`storage.sql`、`classics.sql`、`ai.sql`、`knowledge.sql`、`discovery.sql`、`operations.sql`。
- 当前初始化数据文件固定为 `system.sql`、`storage.sql`、`classics.sql`、`ai.sql`、`knowledge.sql`、`discovery.sql`、`operations.sql`。
- SQL 文件名必须与业务域名称保持一致。
- `db/data/test.sql` 只保存本地开发、联调和 E2E 冒烟需要的测试数据，不属于生产初始化数据；导入前必须确认目标库是 dev/test 环境。
- `db/data/test.sql` 必须幂等，使用明确测试 ID 范围清理后再插入，测试 ID 默认保留 `990000000000` 以上区间，避免与基础种子和人工业务数据冲突。
- 设计阶段允许随业务域归并同步重命名表名、索引名和初始化数据引用。

## Table Types

- 主数据表：保存后台维护的主数据和配置。
- 运行时业务表：保存业务主状态和必要领域时间字段，不机械补齐无意义通用字段。
- 关系表：只保存关系本身的最小字段，关系唯一性用联合唯一约束表达。
- 台账表：只追加，不回写历史。
- 认证事件表：只追加，不保存敏感明文。
- 审计日志表：归属 system 业务域，只追加，不逻辑删除，不保存敏感明文。

## Ordering And Status Rules

- 排序字段统一命名为 `priority`。
- 排序字段类型统一使用 `int`。
- `priority` 表达单表内稳定全局排序值，必须建立单列唯一约束。
- 不得使用 `sort_order`、`display_order`、`order_no`、`sequence` 等其他排序字段名。
- `priority` 已经通过单列唯一约束支持排序，不得再参与普通 KEY 或组合 KEY。
- 业务状态字段统一使用 `varchar` 表达。
- 不得使用 `int`、`tinyint` 或数据库 enum 表达业务状态。

## Field Rules

- 是否增加时间字段取决于对象是否有独立生命周期。
- Java 中绝对时间点统一使用 `Instant`。
- `infra` 负责 `Instant` 与数据库 `datetime(3)` 的 UTC 转换。
- `LocalDateTime` 不作为跨模块和持久化真相时间。
- 业务表不得设置通用审计字段 `created_at`、`updated_at`、`deleted_at`、`created_by`、`updated_by`、`deleted_by`。
- 业务确实需要表达非审计型用户关系时，必须使用业务语义字段名；操作者、创建者、更新者、删除者、发起人等审计型用户关系不得进入业务表。

## Relationship Rules

- 默认不强制数据库外键。
- 关联一致性由 application 和 domain 保证。
- 跨域引用只保存必要业务键、ULID 或快照，不复制对端主表结构。
- 跨域访问不得直接读取或写入其他业务域的 Repository、Mapper 或底层表。

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

## Storage Reference Constraint Truth Source

- Storage 引用约束真相源固定为 `db/schema/storage.sql`。
- `storage_object_reference` 的约束说明：
  - `object_id + reference_owner_type + reference_owner_id` 为复合主键，承载“同一 owner 对同一 object 幂等”。
  - 该设计天然允许同一 `object_id` 被多个不同 owner 并发引用。
  - `storage_object_reference` 只保存当前有效引用记录；对象级别引用存在性由应用层按有效引用集合汇总为 `storage_object.reference_status`。

## Cache Boundary

- 正式业务数据以数据库为真相源。
- 缓存只能保存数据库结果的派生读模型，不能替代数据库。
- 运行时短期对象可以只走 Redis / cache，但必须在模块设计文档中声明为运行态对象。
- DTO、VO、值对象、聚合视图、命令返回模型默认不建表。

## Document Requirements

- 涉及数据库结构的模块设计文档必须遵守本文档。
- 文档中的表名、字段名、索引名必须与后续 DDL、DO、Mapper 保持一致。
- 模块设计文档如果偏离本文档，必须在该文档中写明业务原因。
