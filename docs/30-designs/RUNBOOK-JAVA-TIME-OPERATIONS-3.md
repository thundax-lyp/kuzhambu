# operations Java Time 迁移 RUNBOOK（第 3/3 批）

## Purpose

独立完成 `operations` 本批旧时间类型迁移，闭合字段、签名、调用方、持久化与协议边界。本文件包含执行所需的完整上下文，不依赖其他迁移 RUNBOOK。

## Scope

- 本批包含 **8** 个生产文件。
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
| `application` | `task` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/task/result/OperationsTaskDetailResult.java` | `startedAt`(L26), `completedAt`(L27), `snapshotAt`(L28) | — |
| `application` | `task` | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/task/result/OperationsTaskPageResult.java` | `startedAt`(L26), `completedAt`(L27), `snapshotAt`(L28) | — |
| `domain` | `task` | `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/task/model/entity/LongTaskSnapshot.java` | `startedAt`(L26), `completedAt`(L27), `snapshotAt`(L28) | — |
| `domain` | `task` | `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/task/repository/LongTaskSnapshotRepository.java` | — | L22 `default List<LongTaskSnapshotId> listExpiredSnapshotIds(Date snapshotBefore, int limit) {` |
| `infra` | `task` | `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/task/persistence/dataobject/LongTaskSnapshotDO.java` | `startedAt`(L30), `completedAt`(L31), `snapshotAt`(L32) | — |
| `infra` | `task` | `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/task/repository/impl/LongTaskSnapshotRepositoryImpl.java` | — | L85 `public List<LongTaskSnapshotId> listExpiredSnapshotIds(Date snapshotBefore, int limit) {` |
| `interface` | `task` | `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/task/controller/response/OperationsTaskDetailResponse.java` | `startedAt`(L26), `completedAt`(L27), `snapshotAt`(L28) | — |
| `interface` | `task` | `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/task/controller/response/OperationsTaskPageResponse.java` | `startedAt`(L26), `completedAt`(L27), `snapshotAt`(L28) | — |

## Plan

### Task 1: task 时间类型闭环

涉及生产文件：**8** 个。

#### Files And Changes

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/task/result/OperationsTaskDetailResult.java`
  - L26 `startedAt`：`Date` → `Instant`。
  - L27 `completedAt`：`Date` → `Instant`。
  - L28 `snapshotAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/task/result/OperationsTaskPageResult.java`
  - L26 `startedAt`：`Date` → `Instant`。
  - L27 `completedAt`：`Date` → `Instant`。
  - L28 `snapshotAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/task/model/entity/LongTaskSnapshot.java`
  - L26 `startedAt`：`Date` → `Instant`。
  - L27 `completedAt`：`Date` → `Instant`。
  - L28 `snapshotAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/task/repository/LongTaskSnapshotRepository.java`
  - L22：将签名 `default List<LongTaskSnapshotId> listExpiredSnapshotIds(Date snapshotBefore, int limit) {` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/task/persistence/dataobject/LongTaskSnapshotDO.java`
  - L30 `startedAt`：`Date` → `Instant`。
  - L31 `completedAt`：`Date` → `Instant`。
  - L32 `snapshotAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/task/repository/impl/LongTaskSnapshotRepositoryImpl.java`
  - L85：将签名 `public List<LongTaskSnapshotId> listExpiredSnapshotIds(Date snapshotBefore, int limit) {` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/task/controller/response/OperationsTaskDetailResponse.java`
  - L26 `startedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L27 `completedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L28 `snapshotAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/task/controller/response/OperationsTaskPageResponse.java`
  - L26 `startedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L27 `completedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L28 `snapshotAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

## Associated Test Files

下列测试按本批主题匹配；执行时还必须根据编译和真实调用链补充测试。

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportTaskExecutorTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/test/java/com/thundax/kuzhambu/operations/infra/task/repository/impl/LongTaskSnapshotRepositoryImplTest.java`

## Verification

- `rg 'java\.util\.Date|java\.sql\.(Date|Timestamp)' kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/task/result/OperationsTaskDetailResult.java kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/task/result/OperationsTaskPageResult.java kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/task/model/entity/LongTaskSnapshot.java kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/task/repository/LongTaskSnapshotRepository.java kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/task/persistence/dataobject/LongTaskSnapshotDO.java kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/task/repository/impl/LongTaskSnapshotRepositoryImpl.java kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/task/controller/response/OperationsTaskDetailResponse.java kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/task/controller/response/OperationsTaskPageResponse.java`。
- `cd kuzhambu-servers && mvn -pl :kuzhambu-operations-domain,:kuzhambu-operations-application,:kuzhambu-operations-interface,:kuzhambu-operations-infra spotless:apply`，随后检查 `git diff`。
- `cd kuzhambu-servers && mvn spotless:check`。
- `cd kuzhambu-servers && mvn checkstyle:check`。
- `cd kuzhambu-servers && mvn -pl :kuzhambu-operations-domain,:kuzhambu-operations-application,:kuzhambu-operations-interface,:kuzhambu-operations-infra -am -amd test`；确认 Reactor Build Order 包含上述 4 个叶子模块及其下游装配模块。
- 涉及 HTTP 时验证 JSON 精度、格式和时区；涉及 infra 时验证数据库往返。

## Closure

本批所有 Task 完成、验证通过并同步必要接口或数据库文档后删除本 RUNBOOK。未完成范围必须收窄为新的独立 RUNBOOK，不保留完成历史。
