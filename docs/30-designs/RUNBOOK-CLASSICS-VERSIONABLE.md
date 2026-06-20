# RUNBOOK Classics Versionable

## Purpose

本文档记录 Classics 三类内容统一版本标定能力的实施步骤。目标是在 `SANCAI_ENTRY`、`WANGQI_DOCUMENT`、`MING_CUSTOMS` 上建立一致的 `Versionable` 语义，让分享、历史恢复、版本对比和管理侧变更提示都能判断当前内容是否已有正式版本覆盖。

## Scope

- 新增三类主内容的版本标定字段。
- 新增 domain 层 `Versionable` 契约。
- 建立统一的版本判断、版本生成和主表回填流程。
- 第一阶段不处理 `Shareable`、分享目标绑定版本和 Portal 分享读取；这些在当前分支的后续阶段实现。

## Decisions

- 使用统一 `content_updated_at` 表达内容语义变更时间，不借用审计或技术更新时间。
- 使用 `current_version_id`、`current_version_no`、`current_versioned_at` 表达主内容当前被哪个正式版本标定。
- `classics_content_version` 仍是唯一历史版本表，通过 `content_type + content_id + version_no` 管理三类内容版本。
- 自动保存只用于防丢，不产生正式版本；手动保存、AI 结果应用、历史恢复产生正式版本。
- 当前内容若 `current_version_id` 为空，或 `content_updated_at > current_versioned_at`，则需要生成新版本。
- 版本由用户确认动作显式触发，不使用切面拦截所有 update。排序、状态刷新、访问统计、草稿自动保存等技术性更新不得隐式生成正式版本。

## File Group Rule

每个实施步骤控制在 2-5 个文件内。若某一步超过 5 个文件，必须拆成更小步骤再实施和验证。

## Step 1 Database Schema

文件范围：

- `db/schema/classics.sql`
- `db/data/classics.sql`
- `docs/30-designs/CLASSICS-DESIGN.md`

操作：

- 在 `classics_sancai_entry`、`classics_wangqi_document`、`classics_ming_customs_entry` 增加：
  - `current_version_id bigint`
  - `current_version_no int`
  - `current_versioned_at datetime(3)`
  - `content_updated_at datetime(3)`
- 为三类主表补充 `current_version_id` 普通索引。
- 初始化数据必须填充 `content_updated_at`，已有数据可使用导入时间或固定初始化时间。
- 更新 `CLASSICS-DESIGN.md` 对三类主表字段和版本规则的描述。

本地库同步门禁：

```sh
set -a
source dev.env
set +a
"${MYSQL_CLIENT_BIN:-mysql}" \
  -h "$MYSQL_HOST" \
  -P "$MYSQL_PORT" \
  -u "$MYSQL_USER" \
  -p"$MYSQL_PASSWORD" \
  "$MYSQL_DATABASE" < db/schema/classics.sql
"${MYSQL_CLIENT_BIN:-mysql}" \
  -h "$MYSQL_HOST" \
  -P "$MYSQL_PORT" \
  -u "$MYSQL_USER" \
  -p"$MYSQL_PASSWORD" \
  "$MYSQL_DATABASE" < db/data/classics.sql
```

完成标准：

- schema 文件、初始化数据和设计文档字段一致。
- `dev.env` 指向的本地数据库已经同步到最新 schema/data。

## Step 2 Domain Contract

文件范围：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/model/Versionable.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/model/entity/ClassicsContentVersion.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/repository/ClassicsContentRepository.java`

操作：

- 新增 `Versionable`，暴露：
  - `contentType()`
  - `contentId()`
  - `currentVersionId()`
  - `currentVersionNo()`
  - `currentVersionedAt()`
  - `contentUpdatedAt()`
  - `markVersioned(...)`
- 视需要让 `ClassicsContentVersion` 保持历史版本实体，不承载主内容 dirty 判断。
- 在 `ClassicsContentRepository` 增加版本查询和插入所需的最小方法，例如按 `content_type + content_id` 查询最新版本号。

完成标准：

- `Versionable` 只表达统一版本标定能力，不包含分享和具体 JSON 快照生成逻辑。

## Step 3 Content Entities

文件范围：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/model/entity/SancaiEntry.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/wangqi/model/entity/WangqiDocument.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/mingcustoms/model/entity/MingCustomsEntry.java`

操作：

- 三类内容实体实现 `Versionable`。
- 增加 `currentVersionId`、`currentVersionNo`、`currentVersionedAt`、`contentUpdatedAt` 字段。
- 在 `markVersioned` 中回填当前版本标定信息。

完成标准：

- 三类内容能用统一接口判断身份和版本标定状态。

## Step 4A Sancai Persistence Mapping

文件范围：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/dataobject/SancaiEntryDO.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/assembler/SancaiPersistenceAssembler.java`

操作：

- `SancaiEntryDO` 增加版本标定字段。
- `SancaiPersistenceAssembler` 完成 domain 与 DO 的字段映射。
- 三才条目内容语义变更时写入新的 `content_updated_at`。

完成标准：

- 三才条目的数据库字段、DO、domain entity 三层字段一致。
- 保存三才条目后能可靠更新 `content_updated_at`。

## Step 4B Wangqi Persistence Mapping

文件范围：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/wangqi/persistence/dataobject/WangqiDocumentDO.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/wangqi/persistence/assembler/WangqiDocumentPersistenceAssembler.java`

操作：

- `WangqiDocumentDO` 增加版本标定字段。
- `WangqiDocumentPersistenceAssembler` 完成 domain 与 DO 的字段映射。
- 王圻文档内容语义变更时写入新的 `content_updated_at`。

完成标准：

- 王圻文档的数据库字段、DO、domain entity 三层字段一致。
- 保存王圻文档后能可靠更新 `content_updated_at`。

## Step 4C Ming Customs Persistence Mapping

文件范围：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/mingcustoms/persistence/dataobject/MingCustomsEntryDO.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/mingcustoms/persistence/assembler/MingCustomsPersistenceAssembler.java`

操作：

- `MingCustomsEntryDO` 增加版本标定字段。
- `MingCustomsPersistenceAssembler` 完成 domain 与 DO 的字段映射。
- 明代习俗内容语义变更时写入新的 `content_updated_at`。

完成标准：

- 明代习俗的数据库字段、DO、domain entity 三层字段一致。
- 保存明代习俗后能可靠更新 `content_updated_at`。

## Step 5 Version Service

文件范围：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/service/ClassicsContentVersioningService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsContentSnapshotAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`

操作：

- 新增统一版本服务，提供：
  - `needsVersion(Versionable content)`
  - `nextVersionNo(contentType, contentId)`
  - `markVersioned(content, version)`
- 新增内容快照 assembler，按三类内容输出稳定 JSON，不序列化 Java domain entity 原样结构。
- 在 application 层提供 `ensureVersioned(...)` 或等价方法，供当前分支后续分享阶段复用。

完成标准：

- 对任意 `Versionable` 内容，可以判断是否需要版本，并能生成 `classics_content_version`。
- 版本生成后主表 `current_version_*` 被回填。

## Step 6A Manual Write Flow Integration

文件范围：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/service/impl/WangqiDocumentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/service/impl/MingCustomsApplicationServiceImpl.java`

操作：

- 手动保存三类内容后生成 `MANUAL_SAVE` 正式版本。
- 自动保存草稿不调用正式版本生成。

完成标准：

- 三类内容的手动保存都会写入 `classics_content_version`。
- 自动保存不会写入 `classics_content_version`。

## Step 6B AI And Restore Flow Integration

文件范围：

- AI 应用结果写入入口，实施前先用 `rg "AI_APPLIED|apply"` 精确定位。
- 历史恢复入口，实施前先用 `rg "HISTORY_RESTORED|restore|rollback"` 精确定位。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`

操作：

- AI 结果由用户确认应用后生成 `AI_APPLIED` 正式版本。
- 历史恢复先应用快照，再生成 `HISTORY_RESTORED` 正式版本。
- 自动保存草稿不调用正式版本生成。

完成标准：

- AI 应用和历史恢复都有可追溯版本。
- 单次实施定位后的文件范围必须控制在 2-5 个文件内。

## Step 7 Tests

文件范围：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/test/...`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/...`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/test/...`

操作：

- 新增 domain 单元测试，覆盖 `Versionable` 和 `ClassicsContentVersioningService` 判断规则。
- 新增 application 测试，覆盖三类内容手动保存生成版本，自动保存不生成版本。
- 新增 infra 测试或 repository 测试，覆盖版本插入、版本号查询、主表 `current_version_*` 回填。
- 测试 `needsVersion`：
  - `currentVersionId == null`
  - `currentVersionedAt == null`
  - `contentUpdatedAt > currentVersionedAt`
  - `contentUpdatedAt <= currentVersionedAt`
- 测试版本生成：
  - 版本号递增。
  - `snapshot_json` 稳定。
  - 主表 `current_version_*` 回填。
- 测试自动保存不产生正式版本。

完成标准：

- 覆盖版本判断、版本生成和自动保存边界。
- 测试必须明确断言非用户确认 update 不会写入 `classics_content_version`。

## Step 8 Validation

命令：

```sh
cd kuzhambu-servers
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/classics/kuzhambu-classics-domain,biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-infra -am test
```

如果涉及 starter 集成验证：

```sh
set -a
source dev.env
set +a
cd kuzhambu-servers
mvn spotless:check
mvn checkstyle:check
mvn -pl starter/kuzhambu-admin-starter -am -DskipTests install
cd starter/kuzhambu-admin-starter
mvn spring-boot:run
```

完成标准：

- Maven 格式、静态检查和相关测试通过。
- 本地数据库已按 `dev.env` 同步。
- 未引入 `Shareable` 或分享 target 版本绑定逻辑。

## Step 9 Dev Smoke Test

文件范围：

- `dev.env`
- `db/schema/classics.sql`
- `db/data/classics.sql`
- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/resources/application.yml`

操作：

- 使用 `dev.env` 同步本地数据库：

```sh
set -a
source dev.env
set +a
"${MYSQL_CLIENT_BIN:-mysql}" \
  -h "$MYSQL_HOST" \
  -P "$MYSQL_PORT" \
  -u "$MYSQL_USER" \
  -p"$MYSQL_PASSWORD" \
  "$MYSQL_DATABASE" < db/schema/classics.sql
"${MYSQL_CLIENT_BIN:-mysql}" \
  -h "$MYSQL_HOST" \
  -P "$MYSQL_PORT" \
  -u "$MYSQL_USER" \
  -p"$MYSQL_PASSWORD" \
  "$MYSQL_DATABASE" < db/data/classics.sql
```

- 启动 admin starter：

```sh
set -a
source dev.env
set +a
cd kuzhambu-servers
mvn spotless:check
mvn checkstyle:check
mvn -pl starter/kuzhambu-admin-starter -am -DskipTests install
cd starter/kuzhambu-admin-starter
mvn spring-boot:run
```

- 健康检查：

```sh
curl -fsS http://127.0.0.1:20010/admin-api/actuator/health
```

- 数据库冒烟检查字段存在：

```sh
set -a
source dev.env
set +a
"${MYSQL_CLIENT_BIN:-mysql}" \
  -h "$MYSQL_HOST" \
  -P "$MYSQL_PORT" \
  -u "$MYSQL_USER" \
  -p"$MYSQL_PASSWORD" \
  "$MYSQL_DATABASE" \
  -e "show columns from classics_sancai_entry like 'content_updated_at'; show columns from classics_wangqi_document like 'current_version_id'; show columns from classics_ming_customs_entry like 'current_versioned_at';"
```

- 业务冒烟检查：
  - 通过 admin API 或前端手动保存一条三才条目、王圻文档或明代习俗。
  - 查询 `classics_content_version`，确认新增一条对应 `content_type + content_id` 的 `MANUAL_SAVE` 版本。
  - 查询对应主表，确认 `current_version_id`、`current_version_no`、`current_versioned_at` 已回填，且能追到 `classics_content_version.id`。
  - 执行一次排序、状态刷新或自动保存草稿，确认 `classics_content_version` 未新增正式版本。

参考 SQL：

```sh
set -a
source dev.env
set +a
"${MYSQL_CLIENT_BIN:-mysql}" \
  -h "$MYSQL_HOST" \
  -P "$MYSQL_PORT" \
  -u "$MYSQL_USER" \
  -p"$MYSQL_PASSWORD" \
  "$MYSQL_DATABASE" \
  -e "select id, content_type, content_id, version_no, change_type, versioned_at from classics_content_version order by id desc limit 10;"
```

完成标准：

- 本地 starter 能使用 `dev.env` 启动。
- 健康检查返回成功。
- 至少一类内容手动保存后生成正式版本并回填主表。
- 至少一个非正式版本动作不生成 `classics_content_version`。

## Later Steps

以下步骤仍发生在当前分支，但不属于 `Versionable` 的第一阶段实施范围。必须等本文档前述步骤完成并通过验证后，再继续处理：

- `classics_share_target` 增加 `content_version_id`、`content_version_no`。
- 分享创建时调用 `ensureVersioned(...)`。
- 分享 target 绑定正式版本并冻结 `content_snapshot_json`。
- 管理侧展示分享版本与当前内容版本差异。
