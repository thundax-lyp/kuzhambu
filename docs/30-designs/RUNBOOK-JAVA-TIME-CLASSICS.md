# classics Java Time 迁移 RUNBOOK

## Purpose

一次性管理 `classics` 全域 Java Time 迁移的完整契约闭包。本文是唯一 Classics 执行入口；阶段只用于控制提交与验证规模，不再作为可独立删除或任意排序的 RUNBOOK。

## Scope

- 覆盖原 versioning、content-report、sancai-search-sharing 三个批次的全部生产文件、关联文件与测试。
- 单个 Task 保持 2–8 个主要生产文件；单次提交保持 1–8 个文件。
- 允许同一共享文件跨 Task 出现，但第一次修改时必须完成该文件内所有已知 `Date` 调用点，后续 Task 只能验证，不得重复接管。
- 阶段必须按文档顺序执行；每个 Task 完成局部验证，每个阶段完成 reactor 验证，全部阶段完成后统一删除本文。

## Non-goals

- 不修改与时间类型无关的业务逻辑。
- 不把精确时间点降为 `LocalDate`。
- 不默认修改数据库列类型；发现无法无损映射时先停止并补充数据库决策。

## Global Migration Rules

- domain、application、facade 的时间点使用 `Instant`。
- interface 的自然日期使用 `LocalDate`；精确时间点使用 `Instant`，显式偏移协议使用 `OffsetDateTime`。
- infra 的时间戳列使用 `Instant`，SQL `DATE` 列使用 `LocalDate`。
- `new Date()` 改为 `Instant.now()`；关键规则需要可控时间时注入 `Clock`。
- `before/after` 改为 `isBefore/isAfter`；毫秒算术改为 `Duration` 或 `ChronoUnit`。
- 禁止通过系统默认时区做隐式转换。

## Shared Contract Ownership

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
  - 阶段 1 首次接管该文件：L377、L1508 的 `ClassicsContentVersioningService.newVersion(...)` 参数必须与 `Instant` 契约同时修改。
  - 阶段 2 只完成该文件其余 L1062、L1361、L1389、L1417 export 时间点；不得重新修改版本契约。
- facade 六个时间契约不得只改定义。修改 `ClassicsPublicContentFacadeDto`、`ClassicsQaKnowledgeFacadeDto`、`ClassicsSearchIndexSyncMessageFacadeDto`、`ClassicsCleanupTargetsFacadeRequest`、`ClassicsSummaryFacadeRequest`、`ClassicsSummaryFacadeResponse` 时，必须同步检查并修改 Classics producer/assembler 以及 Operations、Discovery、Knowledge 的所有直接消费者和测试。
- 如果共享契约闭包使某个 Task 超过 8 个实际改动文件，将该 Task 拆为 2–8 文件的小提交连续完成，但不得在中间提交声称 reactor 可编译，也不得拆成新的 RUNBOOK。

### Facade Consumer Closure

以下文件是 facade Task 的关联生产文件。只在类型适配或编译要求发生变化时修改，但必须逐项检查，并把实际改动拆成 2–8 文件的小提交：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/facade/assembler/ClassicsFacadeAssembler.java`
  - 检查六个 facade 时间字段由 application `Instant` 直接映射，不保留 `Date` 适配。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/facade/impl/ClassicsFacadeImpl.java`
  - 检查 cleanup、summary、public content 与 QA knowledge 的 facade 参数和返回值均使用 `Instant`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/searchsync/service/ClassicsSearchIndexSyncPublisher.java`
  - 检查 `occurredAt` 发布签名与 facade message 的 `Instant` 契约一致。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/searchsync/support/ClassicsSearchIndexSyncPublishSupport.java`
  - 检查 message 构造和当前时间生成统一使用 `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/service/impl/CleanupApplicationServiceImpl.java`
  - 将 `ClassicsCleanupTargetsFacadeRequest.requestedAt` 的构造参数与读取链同步为 `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/support/DefaultOperationsDashboardSummaryGateway.java`
  - 将 `ClassicsSummaryFacadeRequest` 的构造参数及 `ClassicsSummaryFacadeResponse` 的读取链同步为 `Instant`。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/service/impl/OperationsDashboardApplicationServiceImpl.java`
  - 检查 Classics summary 时间值进入 dashboard result 的调用链类型一致。
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/support/OperationsDashboardSummaryModels.java`
  - 检查 Classics summary 中间模型的 period 字段与 `Instant` 契约一致。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/mq/RocketMqDiscoverySearchIndexSyncConsumer.java`
  - 将 `message.getOccurredAt()` 下游签名同步为 `Instant`。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/ClassicsSearchContentProvider.java`
  - 将 public content 的 `publishedAt`、`updatedAt` 下游构造链同步为 `Instant`。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/client/DiscoverySearchDocumentAssembler.java`
  - 删除 public content 时间字段上只接受 `Date` 的 `toInstant(...)` 适配，改为 `Instant` 直传。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeQaApplicationServiceImpl.java`
  - 检查 `ClassicsQaKnowledgeFacadeDto.updatedAt` 的读取和比较链同步为 `Instant`。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImpl.java`
  - 检查 Classics QA knowledge 时间值的调用链同步为 `Instant`。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeDocumentAssembler.java`
  - 检查 QA knowledge 文档组装的 `updatedAt` 使用 `Instant`。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaSourceAssembler.java`
  - 检查 QA source 组装的 Classics 时间值使用 `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/workbench/service/impl/KnowledgeGraphWorkbenchApplicationServiceImpl.java`
  - 检查 public content 与 QA knowledge 时间值的选择、比较和组装链同步为 `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/workbench/support/KnowledgeGraphManuscriptPayloadBuilder.java`
  - 检查 manuscript payload 中的 Classics 时间值使用 `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/workbench/support/KnowledgeGraphManuscriptTreeAssembler.java`
  - 检查 manuscript tree 中的 Classics 时间值使用 `Instant`。

执行 facade Task 前再次使用 `rg` 搜索六个 facade 类型；发现新增消费者必须加入当前 closure，不得推迟到其他域 RUNBOOK。完成后以 `-amd` reactor 验证所有消费者。

## Plan

## Phase 1: versioning 基础


### Purpose

完成 `classics` 共享版本契约及 `mingcustoms`、`sancai`、`wangqi` 时间类型迁移。该阶段先执行并形成可编译基线；后续阶段不再修改 `Versionable` 或其三个实现。

### Scope

- 本批包含 **34** 个生产文件。
- 每个 Task 包含 2–8 个生产文件。
- 字段、签名和操作行以当前分支 `HEAD` 为盘点基线。
- 本文件包含完整文件清单、字段动作、关联文件和验证命令，作为本阶段执行输入。

### Preconditions

- 无其他 Classics Java Time RUNBOOK 前置依赖。
- 从当前迁移基线执行。

### Non-goals

- 不迁移本文件清单之外的业务主题。
- 不修改与时间类型无关的业务逻辑。
- 不把精确时间点降为 `LocalDate`。
- 不默认修改数据库列类型；发现无法无损映射时先停止并补充数据库决策。

### Migration Rules

- domain、application、facade 的时间点使用 `Instant`。
- interface 的自然日期使用 `LocalDate`；精确时间点使用 `Instant`，显式偏移协议使用 `OffsetDateTime`。
- infra 的时间戳列使用 `Instant`，SQL `DATE` 列使用 `LocalDate`。
- `new Date()` 改为 `Instant.now()`；关键规则需要可控时间时注入 `Clock`。
- `before/after` 改为 `isBefore/isAfter`；毫秒算术改为 `Duration` 或 `ChronoUnit`。
- 禁止通过系统默认时区做隐式转换。

### File Inventory

| 层 | 主题 | 文件 | 字段 | 签名与操作 |
|---|---|---|---|---|
| `application` | `content` | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/MingCustomsVersionSnapshot.java` | — | L197 `private static String date(Date date) {` |
| `application` | `content` | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/SancaiEntryVersionSnapshot.java` | — | L114 `private static String date(Date date) {` |
| `application` | `content` | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/WangqiDocumentVersionSnapshot.java` | — | L190 `private static String date(Date date) {` |
| `domain` | `content` | `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/model/Versionable.java` | — | L19 `Date currentVersionedAt();`<br>L21 `Date contentUpdatedAt();` |
| `domain` | `content` | `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/model/entity/ClassicsContentVersion.java` | `versionedAt`(L22) | — |
| `domain` | `content` | `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/service/ClassicsContentVersioningService.java` | — | L17 `Date contentUpdatedAt = content.contentUpdatedAt();`<br>L28 `Date versionedAt,` |
| `infra` | `content` | `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/persistence/dataobject/ClassicsContentVersionDO.java` | `versionedAt`(L22) | — |
| `application` | `mingcustoms` | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/service/impl/MingCustomsApplicationServiceImpl.java` | — | 操作行 L90, L105, L182, L307 |
| `domain` | `mingcustoms` | `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/mingcustoms/model/entity/MingCustomsEntry.java` | `currentVersionedAt`(L35), `contentUpdatedAt`(L36) | L82 `public Date currentVersionedAt() {`<br>L87 `public Date contentUpdatedAt() {` |
| `infra` | `mingcustoms` | `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/mingcustoms/persistence/assembler/MingCustomsPersistenceAssembler.java` | — | L115 `private static Date contentUpdatedAt(Date contentUpdatedAt) {`<br>操作行 L116 |
| `infra` | `mingcustoms` | `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/mingcustoms/persistence/dataobject/MingCustomsEntryDO.java` | `currentVersionedAt`(L30), `contentUpdatedAt`(L31) | — |
| `interface` | `mingcustoms` | `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/response/MingCustomsVersionResponse.java` | `versionedAt`(L33) | — |
| `application` | `sancai` | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/assembler/SancaiApplicationAssembler.java` | — | 操作行 L17 |
| `application` | `sancai` | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiApplicationServiceImpl.java` | — | 操作行 L305, L327, L353, L421, L558 |
| `application` | `sancai` | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/support/SancaiEntryVersionRestorer.java` | — | 操作行 L49 |
| `domain` | `sancai` | `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/model/entity/SancaiEntry.java` | `currentVersionedAt`(L44), `contentUpdatedAt`(L45) | L97 `public Date currentVersionedAt() {`<br>L102 `public Date contentUpdatedAt() {` |
| `infra` | `sancai` | `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/assembler/SancaiPersistenceAssembler.java` | — | L195 `private static Date contentUpdatedAt(Date contentUpdatedAt) {`<br>操作行 L196 |
| `infra` | `sancai` | `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/dataobject/SancaiEntryDO.java` | `currentVersionedAt`(L33), `contentUpdatedAt`(L34) | — |
| `interface` | `sancai` | `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiInterfaceAssembler.java` | — | L142–143, L160：保持 `Instant` 直传<br>L208：`after` → `isAfter` |
| `interface` | `sancai` | `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiEntryResponse.java` | `currentVersionedAt`(L61), `contentUpdatedAt`(L64) | — |
| `interface` | `sancai` | `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiEntryVersionResponse.java` | `versionedAt`(L29) | — |
| `interface` | `sancai` | `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sancai/controller/response/SancaiPortalEntryResponse.java` | `contentUpdatedAt`(L56) | — |
| `application` | `wangqi` | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/command/WangqiDocumentCommand.java` | `documentTime`(L21) | — |
| `application` | `wangqi` | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/service/impl/WangqiDocumentApplicationServiceImpl.java` | — | 操作行 L110, L125, L152, L263, L305 |
| `application` | `wangqi` | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/support/WangqiDocumentVersionRestorer.java` | — | L252 `private static Date date(String value) {`<br>操作行 L65, L253 |
| `domain` | `wangqi` | `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/wangqi/model/entity/WangqiDocument.java` | `documentTime`(L30), `currentVersionedAt`(L35), `contentUpdatedAt`(L36) | L45 `Date documentTime,`<br>L79 `public Date currentVersionedAt() {`<br>L84 `public Date contentUpdatedAt() {` |
| `domain` | `wangqi` | `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/wangqi/model/entity/WangqiDocumentEvent.java` | `occurredAt`(L19) | — |
| `infra` | `wangqi` | `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/wangqi/persistence/assembler/WangqiDocumentPersistenceAssembler.java` | — | L69 `private static Date contentUpdatedAt(Date contentUpdatedAt) {`<br>操作行 L70 |
| `infra` | `wangqi` | `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/wangqi/persistence/dataobject/WangqiDocumentDO.java` | `documentTime`(L23), `currentVersionedAt`(L28), `contentUpdatedAt`(L29) | — |
| `infra` | `wangqi` | `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/wangqi/persistence/dataobject/WangqiDocumentEventDO.java` | `occurredAt`(L21) | — |
| `interface` | `wangqi` | `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/request/WangqiDocumentRequest.java` | `documentTime`(L32) | — |
| `interface` | `wangqi` | `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/response/WangqiDocumentEventResponse.java` | `occurredAt`(L32) | — |
| `interface` | `wangqi` | `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/response/WangqiDocumentResponse.java` | `documentTime`(L33) | — |
| `interface` | `wangqi` | `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/response/WangqiDocumentVersionResponse.java` | `versionedAt`(L29) | — |

### Associated Production Files

这些文件当前不直接声明旧时间类型，但位于共享契约的真实转发链。执行对应 Task 时必须检查；只有类型适配或编译要求发生变化时才修改，并将实际改动计入该 Task 的 8 文件上限：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsContentSnapshotAssembler.java`
  - L17–46：检查三个 snapshot 入口继续接受已迁移的 `Versionable` 实现；不新增 `Date` 适配。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/persistence/assembler/ClassicsContentPersistenceAssembler.java`
  - L111–144：检查 `ClassicsContentVersion` 与 `ClassicsContentVersionDO` 的 `versionedAt` 双向映射保持 `Instant` 直传。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/assembler/MingCustomsInterfaceAssembler.java`
  - L104–121：检查 `versionedAt` 从 domain version 到 response 保持 `Instant` 直传。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sancai/assembler/SancaiPortalInterfaceAssembler.java`
  - L130：检查 `contentUpdatedAt` 从 entity 到 response 保持 `Instant` 直传。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/assembler/WangqiDocumentInterfaceAssembler.java`
  - L67、L90–107、L135：检查 `documentTime`、`versionedAt`、`occurredAt` 保持 `Instant` 直传。

### Plan

#### Task 1: content 时间类型闭环

涉及生产文件：**7** 个。

##### Files And Changes

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/MingCustomsVersionSnapshot.java`
  - L197：将签名 `private static String date(Date date) {` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/SancaiEntryVersionSnapshot.java`
  - L114：将签名 `private static String date(Date date) {` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/WangqiDocumentVersionSnapshot.java`
  - L190：将签名 `private static String date(Date date) {` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/model/Versionable.java`
  - L19：将签名 `Date currentVersionedAt();` 的 `Date` 与本调用链目标类型同步。
  - L21：将签名 `Date contentUpdatedAt();` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/model/entity/ClassicsContentVersion.java`
  - L22 `versionedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/service/ClassicsContentVersioningService.java`
  - L17：将签名 `Date contentUpdatedAt = content.contentUpdatedAt();` 的 `Date` 与本调用链目标类型同步。
  - L28：将签名 `Date versionedAt,` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/persistence/dataobject/ClassicsContentVersionDO.java`
  - L22 `versionedAt`：`Date` → `Instant`。

##### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 当前 Task 与其共享契约闭包完成后，最窄模块格式、静态检查、编译和测试通过。

#### Task 2: mingcustoms 时间类型闭环

涉及生产文件：**5** 个。

##### Files And Changes

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/service/impl/MingCustomsApplicationServiceImpl.java`
  - L90, L105, L182, L307：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/mingcustoms/model/entity/MingCustomsEntry.java`
  - L35 `currentVersionedAt`：`Date` → `Instant`。
  - L36 `contentUpdatedAt`：`Date` → `Instant`。
  - L82：将签名 `public Date currentVersionedAt() {` 的 `Date` 与本调用链目标类型同步。
  - L87：将签名 `public Date contentUpdatedAt() {` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/mingcustoms/persistence/assembler/MingCustomsPersistenceAssembler.java`
  - L115：将签名 `private static Date contentUpdatedAt(Date contentUpdatedAt) {` 的 `Date` 与本调用链目标类型同步。
  - L116：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/mingcustoms/persistence/dataobject/MingCustomsEntryDO.java`
  - L30 `currentVersionedAt`：`Date` → `Instant`。
  - L31 `contentUpdatedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/response/MingCustomsVersionResponse.java`
  - L33 `versionedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。

##### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 当前 Task 与其共享契约闭包完成后，最窄模块格式、静态检查、编译和测试通过。

#### Task 3: sancai 时间类型闭环

涉及生产文件：**7** 个。

##### Files And Changes

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/assembler/SancaiApplicationAssembler.java`
  - L17：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiApplicationServiceImpl.java`
  - L305, L327, L353, L421, L558：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/support/SancaiEntryVersionRestorer.java`
  - L49：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/model/entity/SancaiEntry.java`
  - L44 `currentVersionedAt`：`Date` → `Instant`。
  - L45 `contentUpdatedAt`：`Date` → `Instant`。
  - L97：将签名 `public Date currentVersionedAt() {` 的 `Date` 与本调用链目标类型同步。
  - L102：将签名 `public Date contentUpdatedAt() {` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/assembler/SancaiPersistenceAssembler.java`
  - L195：将签名 `private static Date contentUpdatedAt(Date contentUpdatedAt) {` 的 `Date` 与本调用链目标类型同步。
  - L196：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/dataobject/SancaiEntryDO.java`
  - L33 `currentVersionedAt`：`Date` → `Instant`。
  - L34 `contentUpdatedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiEntryResponse.java`
  - L61 `currentVersionedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L64 `contentUpdatedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。

##### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 当前 Task 与其共享契约闭包完成后，最窄模块格式、静态检查、编译和测试通过。

#### Task 4: sancai 时间类型闭环

涉及生产文件：**3** 个。

##### Files And Changes

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiInterfaceAssembler.java`
  - L142–143：确认 `currentVersionedAt`、`contentUpdatedAt` 从 entity 到 response 保持 `Instant` 直传。
  - L160：确认 `versionedAt` 从 `ClassicsContentVersion` 到 response 保持 `Instant` 直传。
  - L208：`entity.getContentUpdatedAt().after(entity.getCurrentVersionedAt())` 改为 `entity.getContentUpdatedAt().isAfter(entity.getCurrentVersionedAt())`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiEntryVersionResponse.java`
  - L29 `versionedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sancai/controller/response/SancaiPortalEntryResponse.java`
  - L56 `contentUpdatedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。

##### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 当前 Task 与其共享契约闭包完成后，最窄模块格式、静态检查、编译和测试通过。

#### Task 5: wangqi 时间类型闭环

涉及生产文件：**8** 个。

##### Files And Changes

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/command/WangqiDocumentCommand.java`
  - L21 `documentTime`：`Date` → `Instant`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/service/impl/WangqiDocumentApplicationServiceImpl.java`
  - L110, L125, L152, L263, L305：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/support/WangqiDocumentVersionRestorer.java`
  - L252：将签名 `private static Date date(String value) {` 的 `Date` 与本调用链目标类型同步。
  - L65, L253：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/wangqi/model/entity/WangqiDocument.java`
  - L30 `documentTime`：`Date` → `Instant`。
  - L35 `currentVersionedAt`：`Date` → `Instant`。
  - L36 `contentUpdatedAt`：`Date` → `Instant`。
  - L45：将签名 `Date documentTime,` 的 `Date` 与本调用链目标类型同步。
  - L79：将签名 `public Date currentVersionedAt() {` 的 `Date` 与本调用链目标类型同步。
  - L84：将签名 `public Date contentUpdatedAt() {` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/wangqi/model/entity/WangqiDocumentEvent.java`
  - L19 `occurredAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/wangqi/persistence/assembler/WangqiDocumentPersistenceAssembler.java`
  - L69：将签名 `private static Date contentUpdatedAt(Date contentUpdatedAt) {` 的 `Date` 与本调用链目标类型同步。
  - L70：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/wangqi/persistence/dataobject/WangqiDocumentDO.java`
  - L23 `documentTime`：`Date` → `Instant`。
  - L28 `currentVersionedAt`：`Date` → `Instant`。
  - L29 `contentUpdatedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/wangqi/persistence/dataobject/WangqiDocumentEventDO.java`
  - L21 `occurredAt`：`Date` → `Instant`。

##### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 当前 Task 与其共享契约闭包完成后，最窄模块格式、静态检查、编译和测试通过。

#### Task 6: wangqi 时间类型闭环

涉及生产文件：**4** 个。

##### Files And Changes

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/request/WangqiDocumentRequest.java`
  - L32 `documentTime`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/response/WangqiDocumentEventResponse.java`
  - L32 `occurredAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/response/WangqiDocumentResponse.java`
  - L33 `documentTime`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/response/WangqiDocumentVersionResponse.java`
  - L29 `versionedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。

##### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 当前 Task 与其共享契约闭包完成后，最窄模块格式、静态检查、编译和测试通过。

### Associated Test Files

执行时必须根据真实编译和调用链补充测试；当前匹配的测试包括：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsContentSnapshotAssemblerTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/SancaiApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/search/ClassicsSearchContentApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/test/java/com/thundax/kuzhambu/classics/domain/content/ClassicsContentVersioningServiceTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/test/java/com/thundax/kuzhambu/classics/infra/sancai/SancaiVersionableMappingTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/content/ClassicsContentAdminControllerTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/MingCustomsAdminControllerTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/WangqiDocumentAdminControllerTest.java`

### Verification

- `rg '\.after\(' kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiInterfaceAssembler.java` 应无输出。
- `rg 'java\.util\.Date|java\.sql\.(Date|Timestamp)' kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/MingCustomsVersionSnapshot.java kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/SancaiEntryVersionSnapshot.java kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/WangqiDocumentVersionSnapshot.java kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/model/Versionable.java kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/model/entity/ClassicsContentVersion.java kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/service/ClassicsContentVersioningService.java kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/persistence/dataobject/ClassicsContentVersionDO.java kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/service/impl/MingCustomsApplicationServiceImpl.java kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/mingcustoms/model/entity/MingCustomsEntry.java kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/mingcustoms/persistence/assembler/MingCustomsPersistenceAssembler.java kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/mingcustoms/persistence/dataobject/MingCustomsEntryDO.java kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/response/MingCustomsVersionResponse.java kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/assembler/SancaiApplicationAssembler.java kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiApplicationServiceImpl.java kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/support/SancaiEntryVersionRestorer.java kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/model/entity/SancaiEntry.java kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/assembler/SancaiPersistenceAssembler.java kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/dataobject/SancaiEntryDO.java kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiEntryResponse.java kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiEntryVersionResponse.java kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sancai/controller/response/SancaiPortalEntryResponse.java kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/command/WangqiDocumentCommand.java kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/service/impl/WangqiDocumentApplicationServiceImpl.java kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/support/WangqiDocumentVersionRestorer.java kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/wangqi/model/entity/WangqiDocument.java kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/wangqi/model/entity/WangqiDocumentEvent.java kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/wangqi/persistence/assembler/WangqiDocumentPersistenceAssembler.java kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/wangqi/persistence/dataobject/WangqiDocumentDO.java kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/wangqi/persistence/dataobject/WangqiDocumentEventDO.java kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/request/WangqiDocumentRequest.java kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/response/WangqiDocumentEventResponse.java kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/response/WangqiDocumentResponse.java kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/response/WangqiDocumentVersionResponse.java`。
- `cd kuzhambu-servers && mvn -pl :kuzhambu-classics-domain,:kuzhambu-classics-application,:kuzhambu-classics-facade,:kuzhambu-classics-interface,:kuzhambu-classics-infra spotless:apply`，随后检查 `git diff`。
- `cd kuzhambu-servers && mvn spotless:check`。
- `cd kuzhambu-servers && mvn checkstyle:check`。
- `cd kuzhambu-servers && mvn -pl :kuzhambu-classics-domain,:kuzhambu-classics-application,:kuzhambu-classics-facade,:kuzhambu-classics-interface,:kuzhambu-classics-infra -am -amd test`；确认 Reactor Build Order 包含 Classics 叶子模块、跨域 facade 消费者和 starter。
- 涉及 HTTP 时验证 JSON 精度、格式和时区；涉及 infra 时验证数据库往返。

### Closure

本阶段所有 Task 完成并通过阶段验证后，在本文记录剩余阻塞并进入下一阶段；不得把未完成范围移出为新的 Classics RUNBOOK。

## Phase 2: content-report


### Purpose

在 versioning 基础批次完成后，完成 `classics` 的 `cleanup`、非版本化 `content`、`facade` 与 `report` 时间类型迁移。

### Scope

- 本批包含 **20** 个生产文件。
- 每个 Task 包含 2–8 个生产文件。
- 字段、签名和操作行以当前分支 `HEAD` 为盘点基线。
- 本文件包含完整文件清单、字段动作、关联文件和验证命令，作为本阶段执行输入。

### Preconditions

- `本文阶段 1` 已完成并通过验证。
- `Versionable.currentVersionedAt()`、`contentUpdatedAt()` 及三个实现均已使用 `Instant`。

### Non-goals

- 不迁移本文件清单之外的业务主题。
- 不修改与时间类型无关的业务逻辑。
- 不把精确时间点降为 `LocalDate`。
- 不默认修改数据库列类型；发现无法无损映射时先停止并补充数据库决策。

### Migration Rules

- domain、application、facade 的时间点使用 `Instant`。
- interface 的自然日期使用 `LocalDate`；精确时间点使用 `Instant`，显式偏移协议使用 `OffsetDateTime`。
- infra 的时间戳列使用 `Instant`，SQL `DATE` 列使用 `LocalDate`。
- `new Date()` 改为 `Instant.now()`；关键规则需要可控时间时注入 `Clock`。
- `before/after` 改为 `isBefore/isAfter`；毫秒算术改为 `Duration` 或 `ChronoUnit`。
- 禁止通过系统默认时区做隐式转换。

### File Inventory

| 层 | 主题 | 文件 | 字段 | 签名与操作 |
|---|---|---|---|---|
| `application` | `cleanup` | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/cleanup/service/ClassicsCleanupApplicationService.java` | — | L13 `List<CleanupTarget> listTargets(String cleanupType, Date requestedAt, Integer retentionDays, Integer limit);` |
| `application` | `cleanup` | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/cleanup/service/impl/ClassicsCleanupApplicationServiceImpl.java` | — | L47 `public List<CleanupTarget> listTargets(String cleanupType, Date requestedAt, Integer retentionDays, Integer limit) {`<br>L134 `private static Date retentionCutoff(Date now, Integer retentionDays) {`<br>操作行 L49, L137 |
| `application` | `content` | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/command/ContentExportCommand.java` | `requestedAt`(L27), `expiresAt`(L28) | L44 `Date requestedAt,`<br>L45 `Date expiresAt,` |
| `application` | `content` | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java` | — | 操作行 L377, L1062, L1361, L1389, L1417, L1508 |
| `domain` | `content` | `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/model/entity/ClassicsContentExportJob.java` | `requestedAt`(L28), `expiresAt`(L29) | L37 `public void markCompleted(StorageObjectId storageObjectId, Date expiresAt, int itemCount, int assetCount) {` |
| `domain` | `content` | `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/repository/ClassicsContentRepository.java` | — | L114 `Date expiresAt,`<br>L124 `default List<ClassicsContentExportJobId> listExpiredExportJobIds(Date now, int limit) {` |
| `infra` | `content` | `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/persistence/dataobject/ClassicsContentExportJobDO.java` | `requestedAt`(L24), `expiresAt`(L25) | — |
| `infra` | `content` | `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/persistence/mapper/ClassicsContentMapper.java` | — | L27 `@Param("expiresAt") Date expiresAt,` |
| `infra` | `content` | `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/repository/impl/ClassicsContentRepositoryImpl.java` | — | L403 `Date expiresAt,`<br>L437 `public List<ClassicsContentExportJobId> listExpiredExportJobIds(Date now, int limit) {` |
| `interface` | `content` | `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/request/ClassicsContentRequest.java` | `expiresAt`(L62) | — |
| `interface` | `content` | `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/response/ClassicsContentResponse.java` | `requestedAt`(L56), `expiresAt`(L59) | — |
| `facade` | `facade` | `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsPublicContentFacadeDto.java` | `publishedAt`(L27), `updatedAt`(L28) | — |
| `facade` | `facade` | `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsQaKnowledgeFacadeDto.java` | `updatedAt`(L24) | — |
| `facade` | `facade` | `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsSearchIndexSyncMessageFacadeDto.java` | `occurredAt`(L19) | — |
| `facade` | `facade` | `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/request/ClassicsCleanupTargetsFacadeRequest.java` | `requestedAt`(L19) | — |
| `facade` | `facade` | `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/request/ClassicsSummaryFacadeRequest.java` | `periodStart`(L14), `periodEnd`(L15) | — |
| `facade` | `facade` | `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/response/ClassicsSummaryFacadeResponse.java` | `periodStart`(L17), `periodEnd`(L18) | — |
| `application` | `report` | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/report/result/ClassicsReportSummaryResult.java` | `periodStart`(L16), `periodEnd`(L17) | — |
| `application` | `report` | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/report/service/ClassicsReportApplicationService.java` | — | L10 `ClassicsReportSummaryResult summary(Date periodStart, Date periodEnd, String bucketType);` |
| `application` | `report` | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/report/service/impl/ClassicsReportApplicationServiceImpl.java` | — | L57 `public ClassicsReportSummaryResult summary(Date periodStart, Date periodEnd, String bucketType) {`<br>L91 `List<Date> growthDates = new ArrayList<>();`<br>L126 `for (Date contentUpdatedAt : contentUpdatedDates) {`<br>L140 `private String toBucket(Date value, String bucketType) {`<br>操作行 L121 |

### Plan

#### Task 1: cleanup 时间类型闭环

涉及生产文件：**2** 个。

##### Files And Changes

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/cleanup/service/ClassicsCleanupApplicationService.java`
  - L13：将签名 `List<CleanupTarget> listTargets(String cleanupType, Date requestedAt, Integer retentionDays, Integer limit);` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/cleanup/service/impl/ClassicsCleanupApplicationServiceImpl.java`
  - L47：将签名 `public List<CleanupTarget> listTargets(String cleanupType, Date requestedAt, Integer retentionDays, Integer limit) {` 的 `Date` 与本调用链目标类型同步。
  - L134：将签名 `private static Date retentionCutoff(Date now, Integer retentionDays) {` 的 `Date` 与本调用链目标类型同步。
  - L49, L137：调整当前时间构造、比较、算术或转换。

##### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 当前 Task 与其共享契约闭包完成后，最窄模块格式、静态检查、编译和测试通过。

#### Task 2: content 时间类型闭环

涉及生产文件：**7** 个。

##### Files And Changes

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/command/ContentExportCommand.java`
  - L27 `requestedAt`：`Date` → `Instant`。
  - L28 `expiresAt`：`Date` → `Instant`。
  - L44：将签名 `Date requestedAt,` 的 `Date` 与本调用链目标类型同步。
  - L45：将签名 `Date expiresAt,` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
  - L377, L1062, L1361, L1389, L1417, L1508：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/model/entity/ClassicsContentExportJob.java`
  - L28 `requestedAt`：`Date` → `Instant`。
  - L29 `expiresAt`：`Date` → `Instant`。
  - L37：将签名 `public void markCompleted(StorageObjectId storageObjectId, Date expiresAt, int itemCount, int assetCount) {` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/repository/ClassicsContentRepository.java`
  - L114：将签名 `Date expiresAt,` 的 `Date` 与本调用链目标类型同步。
  - L124：将签名 `default List<ClassicsContentExportJobId> listExpiredExportJobIds(Date now, int limit) {` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/persistence/dataobject/ClassicsContentExportJobDO.java`
  - L24 `requestedAt`：`Date` → `Instant`。
  - L25 `expiresAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/persistence/mapper/ClassicsContentMapper.java`
  - L27：将签名 `@Param("expiresAt") Date expiresAt,` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/repository/impl/ClassicsContentRepositoryImpl.java`
  - L403：将签名 `Date expiresAt,` 的 `Date` 与本调用链目标类型同步。
  - L437：将签名 `public List<ClassicsContentExportJobId> listExpiredExportJobIds(Date now, int limit) {` 的 `Date` 与本调用链目标类型同步。

##### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 当前 Task 与其共享契约闭包完成后，最窄模块格式、静态检查、编译和测试通过。

#### Task 3: content 时间类型闭环

涉及生产文件：**2** 个。

##### Files And Changes

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/request/ClassicsContentRequest.java`
  - L62 `expiresAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/response/ClassicsContentResponse.java`
  - L56 `requestedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L59 `expiresAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。

##### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 当前 Task 与其共享契约闭包完成后，最窄模块格式、静态检查、编译和测试通过。

#### Task 4: facade 时间类型闭环

涉及生产文件：**6** 个。

##### Files And Changes

- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsPublicContentFacadeDto.java`
  - L27 `publishedAt`：`Date` → `Instant`。
  - L28 `updatedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsQaKnowledgeFacadeDto.java`
  - L24 `updatedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsSearchIndexSyncMessageFacadeDto.java`
  - L19 `occurredAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/request/ClassicsCleanupTargetsFacadeRequest.java`
  - L19 `requestedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/request/ClassicsSummaryFacadeRequest.java`
  - L14 `periodStart`：`Date` → `Instant`。
  - L15 `periodEnd`：`Date` → `Instant`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/response/ClassicsSummaryFacadeResponse.java`
  - L17 `periodStart`：`Date` → `Instant`。
  - L18 `periodEnd`：`Date` → `Instant`。

##### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 当前 Task 与其共享契约闭包完成后，最窄模块格式、静态检查、编译和测试通过。

#### Task 5: report 时间类型闭环

涉及生产文件：**3** 个。

##### Files And Changes

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/report/result/ClassicsReportSummaryResult.java`
  - L16 `periodStart`：`Date` → `Instant`。
  - L17 `periodEnd`：`Date` → `Instant`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/report/service/ClassicsReportApplicationService.java`
  - L10：将签名 `ClassicsReportSummaryResult summary(Date periodStart, Date periodEnd, String bucketType);` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/report/service/impl/ClassicsReportApplicationServiceImpl.java`
  - L57：将签名 `public ClassicsReportSummaryResult summary(Date periodStart, Date periodEnd, String bucketType) {` 的 `Date` 与本调用链目标类型同步。
  - L91：将签名 `List<Date> growthDates = new ArrayList<>();` 的 `Date` 与本调用链目标类型同步。
  - L126：将签名 `for (Date contentUpdatedAt : contentUpdatedDates) {` 的 `Date` 与本调用链目标类型同步。
  - L140：将签名 `private String toBucket(Date value, String bucketType) {` 的 `Date` 与本调用链目标类型同步。
  - L121：调整当前时间构造、比较、算术或转换。

##### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 当前 Task 与其共享契约闭包完成后，最窄模块格式、静态检查、编译和测试通过。

### Associated Test Files

执行时必须根据真实编译和调用链补充测试；当前匹配的测试包括：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsContentSnapshotAssemblerTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/facade/impl/ClassicsFacadeImplTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/report/service/impl/ClassicsReportApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/search/ClassicsSearchContentApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/test/java/com/thundax/kuzhambu/classics/domain/content/ClassicsContentVersioningServiceTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/content/ClassicsContentAdminControllerTest.java`

### Verification

- `rg 'java\.util\.Date|java\.sql\.(Date|Timestamp)' kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/cleanup/service/ClassicsCleanupApplicationService.java kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/cleanup/service/impl/ClassicsCleanupApplicationServiceImpl.java kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/command/ContentExportCommand.java kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/model/entity/ClassicsContentExportJob.java kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/repository/ClassicsContentRepository.java kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/persistence/dataobject/ClassicsContentExportJobDO.java kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/persistence/mapper/ClassicsContentMapper.java kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/repository/impl/ClassicsContentRepositoryImpl.java kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/request/ClassicsContentRequest.java kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/response/ClassicsContentResponse.java kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsPublicContentFacadeDto.java kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsQaKnowledgeFacadeDto.java kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsSearchIndexSyncMessageFacadeDto.java kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/request/ClassicsCleanupTargetsFacadeRequest.java kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/request/ClassicsSummaryFacadeRequest.java kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/response/ClassicsSummaryFacadeResponse.java kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/report/result/ClassicsReportSummaryResult.java kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/report/service/ClassicsReportApplicationService.java kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/report/service/impl/ClassicsReportApplicationServiceImpl.java`。
- `cd kuzhambu-servers && mvn -pl :kuzhambu-classics-domain,:kuzhambu-classics-application,:kuzhambu-classics-facade,:kuzhambu-classics-interface,:kuzhambu-classics-infra spotless:apply`，随后检查 `git diff`。
- `cd kuzhambu-servers && mvn spotless:check`。
- `cd kuzhambu-servers && mvn checkstyle:check`。
- `cd kuzhambu-servers && mvn -pl :kuzhambu-classics-domain,:kuzhambu-classics-application,:kuzhambu-classics-facade,:kuzhambu-classics-interface,:kuzhambu-classics-infra -am -amd test`；确认 Reactor Build Order 包含 Classics 叶子模块、跨域 facade 消费者和 starter。
- 涉及 HTTP 时验证 JSON 精度、格式和时区；涉及 infra 时验证数据库往返。

### Closure

本阶段所有 Task 完成并通过阶段验证后，在本文记录剩余阻塞并进入下一阶段；不得把未完成范围移出为新的 Classics RUNBOOK。

## Phase 3: sancai-search-sharing


### Purpose

在 versioning 基础批次完成后，完成 `classics` 的其余 `sancai`、`search`、`searchsync` 与 `sharing` 时间类型迁移。

### Scope

- 本批包含 **37** 个生产文件。
- 每个 Task 包含 2–8 个生产文件。
- 字段、签名和操作行以当前分支 `HEAD` 为盘点基线。
- 本文件包含完整文件清单、字段动作、关联文件和验证命令，作为本阶段执行输入。

### Preconditions

- `本文阶段 1` 已完成并通过验证。
- `Versionable.currentVersionedAt()`、`contentUpdatedAt()` 及三个实现均已使用 `Instant`。

### Non-goals

- 不迁移本文件清单之外的业务主题。
- 不修改与时间类型无关的业务逻辑。
- 不把精确时间点降为 `LocalDate`。
- 不默认修改数据库列类型；发现无法无损映射时先停止并补充数据库决策。

### Migration Rules

- domain、application、facade 的时间点使用 `Instant`。
- interface 的自然日期使用 `LocalDate`；精确时间点使用 `Instant`，显式偏移协议使用 `OffsetDateTime`。
- infra 的时间戳列使用 `Instant`，SQL `DATE` 列使用 `LocalDate`。
- `new Date()` 改为 `Instant.now()`；关键规则需要可控时间时注入 `Clock`。
- `before/after` 改为 `isBefore/isAfter`；毫秒算术改为 `Duration` 或 `ChronoUnit`。
- 禁止通过系统默认时区做隐式转换。

### File Inventory

| 层 | 主题 | 文件 | 字段 | 签名与操作 |
|---|---|---|---|---|
| `application` | `sancai` | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiDraftCommand.java` | `autosavedAt`(L15) | — |
| `application` | `sancai` | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiShowcaseCommand.java` | `requestedAt`(L17) | L27 `Date requestedAt,` |
| `application` | `sancai` | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/SancaiAssetApplicationService.java` | — | L98 `Date requestedAtStart,`<br>L99 `Date requestedAtEnd,` |
| `application` | `sancai` | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java` | — | L457 `Date requestedAtStart,`<br>L458 `Date requestedAtEnd,`<br>操作行 L122 |
| `domain` | `sancai` | `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/model/entity/SancaiEntryDraft.java` | `autosavedAt`(L18) | — |
| `domain` | `sancai` | `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/model/entity/SancaiShowcase.java` | `requestedAt`(L19), `completedAt`(L20) | 操作行 L48, L66 |
| `domain` | `sancai` | `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiAssetRepository.java` | — | L24 `default List<SancaiEntryDraftId> listExpiredDraftIds(Date cutoff, int limit) {`<br>L118 `Date requestedAtStart,`<br>L119 `Date requestedAtEnd,` |
| `infra` | `sancai` | `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/dataobject/SancaiEntryDraftDO.java` | `autosavedAt`(L20) | — |
| `infra` | `sancai` | `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/dataobject/SancaiShowcaseDO.java` | `requestedAt`(L19), `completedAt`(L20) | — |
| `infra` | `sancai` | `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiAssetRepositoryImpl.java` | — | L84 `public List<SancaiEntryDraftId> listExpiredDraftIds(Date cutoff, int limit) {`<br>L359 `Date requestedAtStart,`<br>L360 `Date requestedAtEnd,`<br>操作行 L306, L330 |
| `interface` | `sancai` | `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiAssetRequest.java` | `requestedAtStart`(L56), `requestedAtEnd`(L59) | — |
| `interface` | `sancai` | `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiAssetResponse.java` | `requestedAt`(L59), `completedAt`(L62) | — |
| `application` | `search` | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/search/result/ClassicsSearchSourceContent.java` | `publishedAt`(L27), `updatedAt`(L28) | L42 `Date publishedAt,`<br>操作行 L43 |
| `application` | `searchsync` | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/searchsync/model/ClassicsSearchIndexSyncMessage.java` | `occurredAt`(L19) | — |
| `application` | `searchsync` | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/searchsync/support/ClassicsSearchIndexSyncPublishSupport.java` | — | 操作行 L68 |
| `application` | `sharing` | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/command/BatchShareCreateCommand.java` | `expiresAt`(L23) | L34 `Date expiresAt,` |
| `application` | `sharing` | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/command/ShareLinkCreateCommand.java` | `issuedAt`(L23), `expiresAt`(L24) | L34 `Date issuedAt,`<br>L35 `Date expiresAt,` |
| `application` | `sharing` | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/result/ShareLinkCreateResult.java` | `expiresAt`(L21) | — |
| `application` | `sharing` | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/result/SharePortalResult.java` | `issuedAt`(L17), `expiresAt`(L18) | — |
| `application` | `sharing` | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/service/ClassicsSharingApplicationService.java` | — | 操作行 L33 |
| `application` | `sharing` | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/service/impl/ClassicsSharingApplicationServiceImpl.java` | — | 操作行 L135, L154, L343, L351, L502, L853 |
| `domain` | `sharing` | `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sharing/model/entity/ClassicsShareAccessRecord.java` | `accessedAt`(L21) | — |
| `domain` | `sharing` | `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sharing/model/entity/ClassicsShareLink.java` | `issuedAt`(L26), `expiresAt`(L27) | — |
| `domain` | `sharing` | `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sharing/model/entity/ClassicsSharePortalListItem.java` | `issuedAt`(L23), `expiresAt`(L24) | — |
| `domain` | `sharing` | `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sharing/repository/ClassicsSharingRepository.java` | — | L25 `default List<ClassicsShareLinkId> listExpiredShareLinkIds(Date now, int limit) {`<br>操作行 L30 |
| `infra` | `sharing` | `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/persistence/dataobject/ClassicsShareAccessRecordDO.java` | `accessedAt`(L21) | — |
| `infra` | `sharing` | `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/persistence/dataobject/ClassicsShareLinkDO.java` | `issuedAt`(L31), `expiresAt`(L32) | — |
| `infra` | `sharing` | `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/persistence/dataobject/ClassicsSharePortalListItemDO.java` | `issuedAt`(L15), `expiresAt`(L16) | — |
| `infra` | `sharing` | `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/persistence/mapper/ClassicsShareTargetMapper.java` | — | L59 `@Param("issuedAfter") Date issuedAfter,`<br>L60 `@Param("issuedBefore") Date issuedBefore);` |
| `infra` | `sharing` | `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/repository/impl/ClassicsSharingRepositoryImpl.java` | — | L122 `public List<ClassicsShareLinkId> listExpiredShareLinkIds(Date now, int limit) {`<br>操作行 L79 |
| `interface` | `sharing` | `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/controller/request/ClassicsBatchShareCreateRequest.java` | `expiresAt`(L33) | — |
| `interface` | `sharing` | `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/controller/request/ClassicsSharingRequest.java` | `expiresAt`(L33), `issuedAfter`(L36), `issuedBefore`(L39) | — |
| `interface` | `sharing` | `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/controller/response/ClassicsSharingAccessRecordResponse.java` | `accessedAt`(L27) | — |
| `interface` | `sharing` | `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/controller/response/ClassicsSharingResponse.java` | `issuedAt`(L36), `expiresAt`(L39) | — |
| `interface` | `sharing` | `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/controller/request/ClassicsSharePortalSearchRequest.java` | `issuedAfter`(L17), `issuedBefore`(L20) | — |
| `interface` | `sharing` | `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/controller/response/ClassicsSharePortalListItemResponse.java` | `issuedAt`(L26), `expiresAt`(L29) | — |
| `interface` | `sharing` | `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/controller/response/ClassicsSharePortalResponse.java` | `issuedAt`(L27), `expiresAt`(L30) | — |

### Plan

#### Task 1: sancai 时间类型闭环

涉及生产文件：**8** 个。

##### Files And Changes

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiDraftCommand.java`
  - L15 `autosavedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiShowcaseCommand.java`
  - L17 `requestedAt`：`Date` → `Instant`。
  - L27：将签名 `Date requestedAt,` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/SancaiAssetApplicationService.java`
  - L98：将签名 `Date requestedAtStart,` 的 `Date` 与本调用链目标类型同步。
  - L99：将签名 `Date requestedAtEnd,` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java`
  - L457：将签名 `Date requestedAtStart,` 的 `Date` 与本调用链目标类型同步。
  - L458：将签名 `Date requestedAtEnd,` 的 `Date` 与本调用链目标类型同步。
  - L122：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/model/entity/SancaiEntryDraft.java`
  - L18 `autosavedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/model/entity/SancaiShowcase.java`
  - L19 `requestedAt`：`Date` → `Instant`。
  - L20 `completedAt`：`Date` → `Instant`。
  - L48, L66：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiAssetRepository.java`
  - L24：将签名 `default List<SancaiEntryDraftId> listExpiredDraftIds(Date cutoff, int limit) {` 的 `Date` 与本调用链目标类型同步。
  - L118：将签名 `Date requestedAtStart,` 的 `Date` 与本调用链目标类型同步。
  - L119：将签名 `Date requestedAtEnd,` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/dataobject/SancaiEntryDraftDO.java`
  - L20 `autosavedAt`：`Date` → `Instant`。

##### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 当前 Task 与其共享契约闭包完成后，最窄模块格式、静态检查、编译和测试通过。

#### Task 2: sancai 时间类型闭环

涉及生产文件：**4** 个。

##### Files And Changes

- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/dataobject/SancaiShowcaseDO.java`
  - L19 `requestedAt`：`Date` → `Instant`。
  - L20 `completedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiAssetRepositoryImpl.java`
  - L84：将签名 `public List<SancaiEntryDraftId> listExpiredDraftIds(Date cutoff, int limit) {` 的 `Date` 与本调用链目标类型同步。
  - L359：将签名 `Date requestedAtStart,` 的 `Date` 与本调用链目标类型同步。
  - L360：将签名 `Date requestedAtEnd,` 的 `Date` 与本调用链目标类型同步。
  - L306, L330：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiAssetRequest.java`
  - L56 `requestedAtStart`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L59 `requestedAtEnd`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiAssetResponse.java`
  - L59 `requestedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L62 `completedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。

##### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 当前 Task 与其共享契约闭包完成后，最窄模块格式、静态检查、编译和测试通过。

#### Task 3: search/searchsync 时间类型闭环

涉及生产文件：**3** 个。

##### Files And Changes

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/search/result/ClassicsSearchSourceContent.java`
  - L27 `publishedAt`：`Date` → `Instant`。
  - L28 `updatedAt`：`Date` → `Instant`。
  - L42：将签名 `Date publishedAt,` 的 `Date` 与本调用链目标类型同步。
  - L43：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/searchsync/model/ClassicsSearchIndexSyncMessage.java`
  - L19 `occurredAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/searchsync/support/ClassicsSearchIndexSyncPublishSupport.java`
  - L68：调整当前时间构造、比较、算术或转换。

##### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 当前 Task 与其共享契约闭包完成后，最窄模块格式、静态检查、编译和测试通过。

#### Task 4: sharing 时间类型闭环

涉及生产文件：**8** 个。

##### Files And Changes

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/command/BatchShareCreateCommand.java`
  - L23 `expiresAt`：`Date` → `Instant`。
  - L34：将签名 `Date expiresAt,` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/command/ShareLinkCreateCommand.java`
  - L23 `issuedAt`：`Date` → `Instant`。
  - L24 `expiresAt`：`Date` → `Instant`。
  - L34：将签名 `Date issuedAt,` 的 `Date` 与本调用链目标类型同步。
  - L35：将签名 `Date expiresAt,` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/result/ShareLinkCreateResult.java`
  - L21 `expiresAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/result/SharePortalResult.java`
  - L17 `issuedAt`：`Date` → `Instant`。
  - L18 `expiresAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/service/ClassicsSharingApplicationService.java`
  - L33：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/service/impl/ClassicsSharingApplicationServiceImpl.java`
  - L135, L154, L343, L351, L502, L853：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sharing/model/entity/ClassicsShareAccessRecord.java`
  - L21 `accessedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sharing/model/entity/ClassicsShareLink.java`
  - L26 `issuedAt`：`Date` → `Instant`。
  - L27 `expiresAt`：`Date` → `Instant`。

##### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 当前 Task 与其共享契约闭包完成后，最窄模块格式、静态检查、编译和测试通过。

#### Task 5: sharing 时间类型闭环

涉及生产文件：**8** 个。

##### Files And Changes

- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sharing/model/entity/ClassicsSharePortalListItem.java`
  - L23 `issuedAt`：`Date` → `Instant`。
  - L24 `expiresAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sharing/repository/ClassicsSharingRepository.java`
  - L25：将签名 `default List<ClassicsShareLinkId> listExpiredShareLinkIds(Date now, int limit) {` 的 `Date` 与本调用链目标类型同步。
  - L30：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/persistence/dataobject/ClassicsShareAccessRecordDO.java`
  - L21 `accessedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/persistence/dataobject/ClassicsShareLinkDO.java`
  - L31 `issuedAt`：`Date` → `Instant`。
  - L32 `expiresAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/persistence/dataobject/ClassicsSharePortalListItemDO.java`
  - L15 `issuedAt`：`Date` → `Instant`。
  - L16 `expiresAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/persistence/mapper/ClassicsShareTargetMapper.java`
  - L59：将签名 `@Param("issuedAfter") Date issuedAfter,` 的 `Date` 与本调用链目标类型同步。
  - L60：将签名 `@Param("issuedBefore") Date issuedBefore);` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/repository/impl/ClassicsSharingRepositoryImpl.java`
  - L122：将签名 `public List<ClassicsShareLinkId> listExpiredShareLinkIds(Date now, int limit) {` 的 `Date` 与本调用链目标类型同步。
  - L79：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/controller/request/ClassicsBatchShareCreateRequest.java`
  - L33 `expiresAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。

##### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 当前 Task 与其共享契约闭包完成后，最窄模块格式、静态检查、编译和测试通过。

#### Task 6: sharing 时间类型闭环

涉及生产文件：**6** 个。

##### Files And Changes

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/controller/request/ClassicsSharingRequest.java`
  - L33 `expiresAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L36 `issuedAfter`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L39 `issuedBefore`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/controller/response/ClassicsSharingAccessRecordResponse.java`
  - L27 `accessedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/controller/response/ClassicsSharingResponse.java`
  - L36 `issuedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L39 `expiresAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/controller/request/ClassicsSharePortalSearchRequest.java`
  - L17 `issuedAfter`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L20 `issuedBefore`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/controller/response/ClassicsSharePortalListItemResponse.java`
  - L26 `issuedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L29 `expiresAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/controller/response/ClassicsSharePortalResponse.java`
  - L27 `issuedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L30 `expiresAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。

##### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 当前 Task 与其共享契约闭包完成后，最窄模块格式、静态检查、编译和测试通过。

### Associated Test Files

执行时必须根据真实编译和调用链补充测试；当前匹配的测试包括：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/search/ClassicsSearchContentApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sharing/ClassicsSharingApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/ClassicsSharingAdminControllerTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/ClassicsSharingPortalControllerTest.java`

### Verification

- `rg 'java\.util\.Date|java\.sql\.(Date|Timestamp)' kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiDraftCommand.java kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiShowcaseCommand.java kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/SancaiAssetApplicationService.java kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/model/entity/SancaiEntryDraft.java kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/model/entity/SancaiShowcase.java kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiAssetRepository.java kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/dataobject/SancaiEntryDraftDO.java kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/dataobject/SancaiShowcaseDO.java kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiAssetRepositoryImpl.java kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiAssetRequest.java kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiAssetResponse.java kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/search/result/ClassicsSearchSourceContent.java kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/searchsync/model/ClassicsSearchIndexSyncMessage.java kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/searchsync/support/ClassicsSearchIndexSyncPublishSupport.java kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/command/BatchShareCreateCommand.java kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/command/ShareLinkCreateCommand.java kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/result/ShareLinkCreateResult.java kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/result/SharePortalResult.java kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/service/ClassicsSharingApplicationService.java kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/service/impl/ClassicsSharingApplicationServiceImpl.java kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sharing/model/entity/ClassicsShareAccessRecord.java kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sharing/model/entity/ClassicsShareLink.java kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sharing/model/entity/ClassicsSharePortalListItem.java kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sharing/repository/ClassicsSharingRepository.java kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/persistence/dataobject/ClassicsShareAccessRecordDO.java kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/persistence/dataobject/ClassicsShareLinkDO.java kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/persistence/dataobject/ClassicsSharePortalListItemDO.java kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/persistence/mapper/ClassicsShareTargetMapper.java kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/repository/impl/ClassicsSharingRepositoryImpl.java kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/controller/request/ClassicsBatchShareCreateRequest.java kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/controller/request/ClassicsSharingRequest.java kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/controller/response/ClassicsSharingAccessRecordResponse.java kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/controller/response/ClassicsSharingResponse.java kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/controller/request/ClassicsSharePortalSearchRequest.java kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/controller/response/ClassicsSharePortalListItemResponse.java kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/controller/response/ClassicsSharePortalResponse.java`。
- `cd kuzhambu-servers && mvn -pl :kuzhambu-classics-domain,:kuzhambu-classics-application,:kuzhambu-classics-facade,:kuzhambu-classics-interface,:kuzhambu-classics-infra spotless:apply`，随后检查 `git diff`。
- `cd kuzhambu-servers && mvn spotless:check`。
- `cd kuzhambu-servers && mvn checkstyle:check`。
- `cd kuzhambu-servers && mvn -pl :kuzhambu-classics-domain,:kuzhambu-classics-application,:kuzhambu-classics-facade,:kuzhambu-classics-interface,:kuzhambu-classics-infra -am -amd test`；确认 Reactor Build Order 包含 Classics 叶子模块、跨域 facade 消费者和 starter。
- 涉及 HTTP 时验证 JSON 精度、格式和时区；涉及 infra 时验证数据库往返。

### Closure

本阶段所有 Task 完成并通过阶段验证后，在本文记录剩余阻塞并进入最终验证；不得把未完成范围移出为新的 Classics RUNBOOK。

## Final Verification

- 三个阶段的 `rg` 检查均无旧时间类型输出。
- 执行 Classics 全部叶子模块的 `spotless:apply` 后检查 `git diff`，只保留迁移相关修改。
- 执行 `mvn spotless:check`、`mvn checkstyle:check`。
- 执行 Classics domain、application、facade、interface、infra 的 `-am -amd test`，确认跨域消费者与 starter 全部进入 Reactor Build Order 并通过。
- 对 HTTP JSON、MQ JSON、数据库往返、时间比较和边界值完成针对性测试。

## Closure

全部 Task 和最终验证完成，并同步必要接口、数据库或 readiness 文档后，删除本 RUNBOOK。未完成内容只能在本文内继续拆 Task，不再建立互相依赖的 Classics RUNBOOK。
