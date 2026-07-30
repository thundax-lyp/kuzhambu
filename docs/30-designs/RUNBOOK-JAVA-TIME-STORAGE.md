# storage Java Time 迁移 RUNBOOK

## Purpose

独立完成 `storage` 本批旧时间类型迁移，闭合字段、签名、调用方、持久化与协议边界。本文件包含执行所需的完整上下文，不依赖其他迁移 RUNBOOK。

## Scope

- 本批包含 **5** 个生产文件。
- 每个 Task 包含 2–8 个生产文件。
- 字段、签名和操作行以当前分支 `HEAD` 为盘点基线。
- 仅可依据真实编译或调用关系补充遗漏文件，并在任务结果中记录证据。

## Non-goals

- 不机械迁移其他业务域。
- 不修改与时间类型无关的业务逻辑。
- 不把精确时间点降为 `LocalDate`。
- 不默认修改数据库列类型；发现无法无损映射时先停止并补充数据库决策。

## Migration Rules

- domain、application、facade 的时间点使用 `Instant`。
- interface 的自然日期使用 `LocalDate`；精确时间点使用 `Instant`，显式偏移协议使用 `OffsetDateTime`。
- infra 的时间戳列使用 `Instant`，SQL `DATE` 列使用 `LocalDate`。
- `new Date()` 改为 `Instant.now()`；关键规则需要可控时间时注入 `Clock`。
- `before/after` 改为 `isBefore/isAfter`；毫秒算术改为 `Duration` 或 `ChronoUnit`。
- 禁止通过系统默认时区做隐式转换。

## File Inventory

| 层 | 主题 | 文件 | 字段 | 签名与操作 |
|---|---|---|---|---|
| `domain` | `object` | `kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/entity/MultipartUploadSession.java` | `completedDate`(L44), `abortedDate`(L45) | — |
| `domain` | `object` | `kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/entity/StoredObject.java` | — | 操作行 L146 |
| `infra` | `object` | `kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/persistence/dataobject/MultipartUploadSessionDO.java` | `completedDate`(L32), `abortedDate`(L33) | — |
| `infra` | `object` | `kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/StoredObjectContentRepositoryImpl.java` | — | 操作行 L70 |
| `application` | `service` | `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/MultipartUploadApplicationServiceImpl.java` | — | 操作行 L63, L120, L225 |

## Plan

### Task 1: object, service 时间类型闭环

涉及生产文件：**5** 个。

#### Files And Changes

- `kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/entity/MultipartUploadSession.java`
  - L44 `completedDate`：`Date` → `LocalDate` 候选；必须核对协议、数据库列和比较精度。
  - L45 `abortedDate`：`Date` → `LocalDate` 候选；必须核对协议、数据库列和比较精度。
- `kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/entity/StoredObject.java`
  - L146：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/persistence/dataobject/MultipartUploadSessionDO.java`
  - L32 `completedDate`：`Date` → `LocalDate` 候选；必须核对协议、数据库列和比较精度。
  - L33 `abortedDate`：`Date` → `LocalDate` 候选；必须核对协议、数据库列和比较精度。
- `kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/StoredObjectContentRepositoryImpl.java`
  - L70：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/MultipartUploadApplicationServiceImpl.java`
  - L63, L120, L225：调整当前时间构造、比较、算术或转换。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

## Associated Test Files

下列测试按本批主题匹配；执行时还必须根据编译和真实调用链补充测试。

- 当前基线无直接导入 `Date` 的测试，必须补充最窄行为验证。

## Verification

- `rg 'java\.util\.Date|java\.sql\.(Date|Timestamp)' kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/entity/MultipartUploadSession.java kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/entity/StoredObject.java kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/persistence/dataobject/MultipartUploadSessionDO.java kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/StoredObjectContentRepositoryImpl.java kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/MultipartUploadApplicationServiceImpl.java`。
- `cd kuzhambu-servers && mvn -pl :kuzhambu-storage-domain,:kuzhambu-storage-application,:kuzhambu-storage-facade,:kuzhambu-storage-interface,:kuzhambu-storage-infra spotless:apply`，随后检查 `git diff`。
- `cd kuzhambu-servers && mvn spotless:check`。
- `cd kuzhambu-servers && mvn checkstyle:check`。
- `cd kuzhambu-servers && mvn -pl :kuzhambu-storage-domain,:kuzhambu-storage-application,:kuzhambu-storage-facade,:kuzhambu-storage-interface,:kuzhambu-storage-infra -am -amd test`；确认 Reactor Build Order 包含上述 5 个叶子模块及依赖其 facade 的跨域消费者。
- 涉及 HTTP 时验证 JSON 精度、格式和时区；涉及 infra 时验证数据库往返。

## Closure

本批所有 Task 完成、验证通过并同步必要接口或数据库文档后删除本 RUNBOOK。未完成范围必须收窄为新的独立 RUNBOOK，不保留完成历史。
