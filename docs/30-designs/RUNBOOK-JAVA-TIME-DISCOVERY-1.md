# discovery Java Time 迁移 RUNBOOK（第 1/2 批）

## Purpose

独立完成 `discovery` 本批旧时间类型迁移，闭合字段、签名、调用方、持久化与协议边界。本文件包含执行所需的完整上下文，不依赖其他迁移 RUNBOOK。

## Scope

- 本批包含 **34** 个生产文件。
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
| `facade` | `facade` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-facade/src/main/java/com/thundax/kuzhambu/discovery/facade/request/DiscoverySummaryFacadeRequest.java` | `periodStart`(L14), `periodEnd`(L15) | — |
| `facade` | `facade` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-facade/src/main/java/com/thundax/kuzhambu/discovery/facade/response/DiscoverySummaryFacadeResponse.java` | `periodStart`(L18), `periodEnd`(L19) | — |
| `application` | `qa` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/query/QaSessionPageQuery.java` | `openedAtStart`(L15), `openedAtEnd`(L16) | — |
| `application` | `qa` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/result/QaMessageResult.java` | `sentAt`(L21), `answeredAt`(L22) | — |
| `application` | `qa` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/result/QaTraceResult.java` | `retrievedAt`(L29) | — |
| `application` | `qa` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeQaApplicationServiceImpl.java` | — | L504 `Date startedAt,`<br>L967 `Date startedAt,`<br>L968 `Date endAt,`<br>L993 `Date startedAt,`<br>L994 `Date endAt,`<br>L1153 `Date sentAt,`<br>L1175 `Date sentAt,`<br>操作行 L141, L182, L193, L204, L249, L258, L299, L341, L355, L366, L411, L420, L481, L518, L536, L555, L570, L1193, L1210 |
| `application` | `qa` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeSyncApplicationServiceImpl.java` | — | L506 `Date now,`<br>操作行 L112, L143, L177, L231, L328, L533 |
| `application` | `qa` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImpl.java` | — | 操作行 L115, L149, L171, L202, L209 |
| `application` | `qa` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeDocument.java` | — | 操作行 L18 |
| `application` | `qa` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaSessionCsvExporter.java` | — | L230 `private Long millis(Date value) {` |
| `application` | `qa` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaSourceAssembler.java` | — | 操作行 L87, L110, L133 |
| `application` | `qa` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaTraceAssembler.java` | — | 操作行 L57, L87 |
| `domain` | `qa` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaKnowledgeSyncBatch.java` | `startedAt`(L20), `finishedAt`(L21) | L31 `Date startedAt,`<br>操作行 L32 |
| `domain` | `qa` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaKnowledgeSyncItem.java` | `syncedAt`(L29), `createdAt`(L30), `updatedAt`(L31) | L46 `Date syncedAt,`<br>L47 `Date createdAt,`<br>操作行 L48 |
| `domain` | `qa` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaMessage.java` | `sentAt`(L30), `answeredAt`(L31) | L44 `Date sentAt,`<br>L74 `Date sentAt,`<br>操作行 L45, L75 |
| `domain` | `qa` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaRetrievalTrace.java` | `retrievedAt`(L29) | 操作行 L48 |
| `domain` | `qa` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaSession.java` | `openedAt`(L30), `lastMessageAt`(L31), `removedAt`(L32) | L46 `Date openedAt,`<br>L47 `Date lastMessageAt,`<br>L62 `public void markRemoved(Date removedAt) {`<br>操作行 L48 |
| `domain` | `qa` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaSessionExport.java` | `requestedAt`(L22), `completedAt`(L23) | L34 `Date requestedAt,`<br>操作行 L35 |
| `domain` | `qa` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaSource.java` | `referencedAt`(L28) | 操作行 L45 |
| `domain` | `qa` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaSessionRepository.java` | — | L18 `List<QaSession> listByOpenedAtRange(java.util.Date openedAtStart, java.util.Date openedAtEnd);`<br>L20 `PageResult<QaSession> page(String title, Date openedAtStart, Date openedAtEnd, int pageNo, int pageSize);`<br>L28 `int markRemoved(QaSessionId id, Date removedAt);` |
| `infra` | `qa` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaKnowledgeSyncBatchDO.java` | `startedAt`(L25), `finishedAt`(L26) | — |
| `infra` | `qa` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaKnowledgeSyncItemDO.java` | `syncedAt`(L31), `createdAt`(L32), `updatedAt`(L33) | — |
| `infra` | `qa` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaMessageDO.java` | `sentAt`(L29), `answeredAt`(L30) | — |
| `infra` | `qa` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaRetrievalTraceDO.java` | `retrievedAt`(L34) | — |
| `infra` | `qa` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaSessionDO.java` | `openedAt`(L29), `lastMessageAt`(L30), `removedAt`(L31) | — |
| `infra` | `qa` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaSessionExportDO.java` | `requestedAt`(L26), `completedAt`(L27) | — |
| `infra` | `qa` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaSourceDO.java` | `referencedAt`(L33) | — |
| `infra` | `qa` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/mapper/QaSessionMapper.java` | — | L23 `@Param("openedAtStart") Date openedAtStart, @Param("openedAtEnd") Date openedAtEnd);`<br>L33 `int markRemoved(@Param("id") Long id, @Param("removedAt") Date removedAt);` |
| `infra` | `qa` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSessionRepositoryImpl.java` | — | L40 `public List<QaSession> listByOpenedAtRange(Date openedAtStart, Date openedAtEnd) {`<br>L45 `public PageResult<QaSession> page(String title, Date openedAtStart, Date openedAtEnd, int pageNo, int pageSize) {`<br>L72 `private QueryWrapper<QaSessionDO> buildPageWrapper(String title, Date openedAtStart, Date openedAtEnd) {`<br>L100 `public int markRemoved(QaSessionId id, Date removedAt) {` |
| `interface` | `qa` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/response/DiscoveryQaAdminResponses.java` | `sentAt`(L210), `answeredAt`(L214) | — |
| `interface` | `qa` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/assembler/DiscoveryQaPortalInterfaceAssembler.java` | — | L256 `private static Long toTimestamp(Date date) {` |
| `application` | `report` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/report/result/DiscoveryReportSummaryResult.java` | `periodStart`(L16), `periodEnd`(L17) | — |
| `application` | `report` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/report/service/DiscoveryReportApplicationService.java` | — | L10 `DiscoveryReportSummaryResult summary(Date periodStart, Date periodEnd, String bucketType);` |
| `application` | `report` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/report/service/impl/DiscoveryReportApplicationServiceImpl.java` | — | L40 `public DiscoveryReportSummaryResult summary(Date periodStart, Date periodEnd, String bucketType) {`<br>L80 `Date createdAt = searchEvent == null ? null : searchEvent.getCreatedAt();`<br>L98 `Date openedAt = qaSession == null ? null : qaSession.getOpenedAt();`<br>L112 `private boolean outOfRange(Date value, Date periodStart, Date periodEnd) {`<br>L129 `private String toBucket(Date value, String bucketType) {`<br>操作行 L77, L95 |

## Plan

### Task 1: facade 时间类型闭环

涉及生产文件：**2** 个。

#### Files And Changes

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-facade/src/main/java/com/thundax/kuzhambu/discovery/facade/request/DiscoverySummaryFacadeRequest.java`
  - L14 `periodStart`：`Date` → `Instant`。
  - L15 `periodEnd`：`Date` → `Instant`。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-facade/src/main/java/com/thundax/kuzhambu/discovery/facade/response/DiscoverySummaryFacadeResponse.java`
  - L18 `periodStart`：`Date` → `Instant`。
  - L19 `periodEnd`：`Date` → `Instant`。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

### Task 2: qa 时间类型闭环

涉及生产文件：**8** 个。

#### Files And Changes

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/query/QaSessionPageQuery.java`
  - L15 `openedAtStart`：`Date` → `Instant`。
  - L16 `openedAtEnd`：`Date` → `Instant`。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/result/QaMessageResult.java`
  - L21 `sentAt`：`Date` → `Instant`。
  - L22 `answeredAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/result/QaTraceResult.java`
  - L29 `retrievedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeQaApplicationServiceImpl.java`
  - L504：将签名 `Date startedAt,` 的 `Date` 与本调用链目标类型同步。
  - L967：将签名 `Date startedAt,` 的 `Date` 与本调用链目标类型同步。
  - L968：将签名 `Date endAt,` 的 `Date` 与本调用链目标类型同步。
  - L993：将签名 `Date startedAt,` 的 `Date` 与本调用链目标类型同步。
  - L994：将签名 `Date endAt,` 的 `Date` 与本调用链目标类型同步。
  - L1153：将签名 `Date sentAt,` 的 `Date` 与本调用链目标类型同步。
  - L1175：将签名 `Date sentAt,` 的 `Date` 与本调用链目标类型同步。
  - L141, L182, L193, L204, L249, L258, L299, L341, L355, L366, L411, L420, L481, L518, L536, L555, L570, L1193, L1210：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeSyncApplicationServiceImpl.java`
  - L506：将签名 `Date now,` 的 `Date` 与本调用链目标类型同步。
  - L112, L143, L177, L231, L328, L533：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImpl.java`
  - L115, L149, L171, L202, L209：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeDocument.java`
  - L18：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaSessionCsvExporter.java`
  - L230：将签名 `private Long millis(Date value) {` 的 `Date` 与本调用链目标类型同步。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

### Task 3: qa 时间类型闭环

涉及生产文件：**8** 个。

#### Files And Changes

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaSourceAssembler.java`
  - L87, L110, L133：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaTraceAssembler.java`
  - L57, L87：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaKnowledgeSyncBatch.java`
  - L20 `startedAt`：`Date` → `Instant`。
  - L21 `finishedAt`：`Date` → `Instant`。
  - L31：将签名 `Date startedAt,` 的 `Date` 与本调用链目标类型同步。
  - L32：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaKnowledgeSyncItem.java`
  - L29 `syncedAt`：`Date` → `Instant`。
  - L30 `createdAt`：`Date` → `Instant`。
  - L31 `updatedAt`：`Date` → `Instant`。
  - L46：将签名 `Date syncedAt,` 的 `Date` 与本调用链目标类型同步。
  - L47：将签名 `Date createdAt,` 的 `Date` 与本调用链目标类型同步。
  - L48：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaMessage.java`
  - L30 `sentAt`：`Date` → `Instant`。
  - L31 `answeredAt`：`Date` → `Instant`。
  - L44：将签名 `Date sentAt,` 的 `Date` 与本调用链目标类型同步。
  - L74：将签名 `Date sentAt,` 的 `Date` 与本调用链目标类型同步。
  - L45, L75：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaRetrievalTrace.java`
  - L29 `retrievedAt`：`Date` → `Instant`。
  - L48：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaSession.java`
  - L30 `openedAt`：`Date` → `Instant`。
  - L31 `lastMessageAt`：`Date` → `Instant`。
  - L32 `removedAt`：`Date` → `Instant`。
  - L46：将签名 `Date openedAt,` 的 `Date` 与本调用链目标类型同步。
  - L47：将签名 `Date lastMessageAt,` 的 `Date` 与本调用链目标类型同步。
  - L62：将签名 `public void markRemoved(Date removedAt) {` 的 `Date` 与本调用链目标类型同步。
  - L48：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaSessionExport.java`
  - L22 `requestedAt`：`Date` → `Instant`。
  - L23 `completedAt`：`Date` → `Instant`。
  - L34：将签名 `Date requestedAt,` 的 `Date` 与本调用链目标类型同步。
  - L35：调整当前时间构造、比较、算术或转换。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

### Task 4: qa 时间类型闭环

涉及生产文件：**8** 个。

#### Files And Changes

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaSource.java`
  - L28 `referencedAt`：`Date` → `Instant`。
  - L45：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaSessionRepository.java`
  - L18：将签名 `List<QaSession> listByOpenedAtRange(java.util.Date openedAtStart, java.util.Date openedAtEnd);` 的 `Date` 与本调用链目标类型同步。
  - L20：将签名 `PageResult<QaSession> page(String title, Date openedAtStart, Date openedAtEnd, int pageNo, int pageSize);` 的 `Date` 与本调用链目标类型同步。
  - L28：将签名 `int markRemoved(QaSessionId id, Date removedAt);` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaKnowledgeSyncBatchDO.java`
  - L25 `startedAt`：`Date` → `Instant`。
  - L26 `finishedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaKnowledgeSyncItemDO.java`
  - L31 `syncedAt`：`Date` → `Instant`。
  - L32 `createdAt`：`Date` → `Instant`。
  - L33 `updatedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaMessageDO.java`
  - L29 `sentAt`：`Date` → `Instant`。
  - L30 `answeredAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaRetrievalTraceDO.java`
  - L34 `retrievedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaSessionDO.java`
  - L29 `openedAt`：`Date` → `Instant`。
  - L30 `lastMessageAt`：`Date` → `Instant`。
  - L31 `removedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaSessionExportDO.java`
  - L26 `requestedAt`：`Date` → `Instant`。
  - L27 `completedAt`：`Date` → `Instant`。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

### Task 5: qa 时间类型闭环

涉及生产文件：**5** 个。

#### Files And Changes

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaSourceDO.java`
  - L33 `referencedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/mapper/QaSessionMapper.java`
  - L23：将签名 `@Param("openedAtStart") Date openedAtStart, @Param("openedAtEnd") Date openedAtEnd);` 的 `Date` 与本调用链目标类型同步。
  - L33：将签名 `int markRemoved(@Param("id") Long id, @Param("removedAt") Date removedAt);` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSessionRepositoryImpl.java`
  - L40：将签名 `public List<QaSession> listByOpenedAtRange(Date openedAtStart, Date openedAtEnd) {` 的 `Date` 与本调用链目标类型同步。
  - L45：将签名 `public PageResult<QaSession> page(String title, Date openedAtStart, Date openedAtEnd, int pageNo, int pageSize) {` 的 `Date` 与本调用链目标类型同步。
  - L72：将签名 `private QueryWrapper<QaSessionDO> buildPageWrapper(String title, Date openedAtStart, Date openedAtEnd) {` 的 `Date` 与本调用链目标类型同步。
  - L100：将签名 `public int markRemoved(QaSessionId id, Date removedAt) {` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/response/DiscoveryQaAdminResponses.java`
  - L210 `sentAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L214 `answeredAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/assembler/DiscoveryQaPortalInterfaceAssembler.java`
  - L256：将签名 `private static Long toTimestamp(Date date) {` 的 `Date` 与本调用链目标类型同步。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

### Task 6: report 时间类型闭环

涉及生产文件：**3** 个。

#### Files And Changes

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/report/result/DiscoveryReportSummaryResult.java`
  - L16 `periodStart`：`Date` → `Instant`。
  - L17 `periodEnd`：`Date` → `Instant`。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/report/service/DiscoveryReportApplicationService.java`
  - L10：将签名 `DiscoveryReportSummaryResult summary(Date periodStart, Date periodEnd, String bucketType);` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/report/service/impl/DiscoveryReportApplicationServiceImpl.java`
  - L40：将签名 `public DiscoveryReportSummaryResult summary(Date periodStart, Date periodEnd, String bucketType) {` 的 `Date` 与本调用链目标类型同步。
  - L80：将签名 `Date createdAt = searchEvent == null ? null : searchEvent.getCreatedAt();` 的 `Date` 与本调用链目标类型同步。
  - L98：将签名 `Date openedAt = qaSession == null ? null : qaSession.getOpenedAt();` 的 `Date` 与本调用链目标类型同步。
  - L112：将签名 `private boolean outOfRange(Date value, Date periodStart, Date periodEnd) {` 的 `Date` 与本调用链目标类型同步。
  - L129：将签名 `private String toBucket(Date value, String bucketType) {` 的 `Date` 与本调用链目标类型同步。
  - L77, L95：调整当前时间构造、比较、算术或转换。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

## Associated Test Files

下列测试按本批主题匹配；执行时还必须根据编译和真实调用链补充测试。

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/facade/impl/DiscoveryFacadeImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeQaApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImplAdminReadTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeDocumentAssemblerTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/report/service/impl/DiscoveryReportApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/mapper/QaSessionMapperTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaKnowledgeSyncBatchRepositoryImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaKnowledgeSyncItemRepositoryImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaMessageRepositoryImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaRetrievalTraceRepositoryImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSessionExportRepositoryImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSessionRepositoryImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSourceRepositoryImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/DiscoveryQaAdminControllerTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/DiscoveryQaPortalControllerTest.java`

## Verification

- `rg 'java\.util\.Date|java\.sql\.(Date|Timestamp)' kuzhambu-servers/biz/discovery/kuzhambu-discovery-facade/src/main/java/com/thundax/kuzhambu/discovery/facade/request/DiscoverySummaryFacadeRequest.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-facade/src/main/java/com/thundax/kuzhambu/discovery/facade/response/DiscoverySummaryFacadeResponse.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/query/QaSessionPageQuery.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/result/QaMessageResult.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/result/QaTraceResult.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeQaApplicationServiceImpl.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeSyncApplicationServiceImpl.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImpl.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeDocument.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaSessionCsvExporter.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaSourceAssembler.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaTraceAssembler.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaKnowledgeSyncBatch.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaKnowledgeSyncItem.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaMessage.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaRetrievalTrace.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaSession.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaSessionExport.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaSource.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaSessionRepository.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaKnowledgeSyncBatchDO.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaKnowledgeSyncItemDO.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaMessageDO.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaRetrievalTraceDO.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaSessionDO.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaSessionExportDO.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaSourceDO.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/mapper/QaSessionMapper.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSessionRepositoryImpl.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/response/DiscoveryQaAdminResponses.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/assembler/DiscoveryQaPortalInterfaceAssembler.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/report/result/DiscoveryReportSummaryResult.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/report/service/DiscoveryReportApplicationService.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/report/service/impl/DiscoveryReportApplicationServiceImpl.java`。
- `cd kuzhambu-servers && mvn -pl :kuzhambu-discovery-domain,:kuzhambu-discovery-application,:kuzhambu-discovery-facade,:kuzhambu-discovery-interface,:kuzhambu-discovery-infra spotless:apply`，随后检查 `git diff`。
- `cd kuzhambu-servers && mvn spotless:check`。
- `cd kuzhambu-servers && mvn checkstyle:check`。
- `cd kuzhambu-servers && mvn -pl :kuzhambu-discovery-domain,:kuzhambu-discovery-application,:kuzhambu-discovery-facade,:kuzhambu-discovery-interface,:kuzhambu-discovery-infra -am -amd test`；确认 Reactor Build Order 包含上述 5 个叶子模块及依赖其 facade 的跨域消费者。
- 涉及 HTTP 时验证 JSON 精度、格式和时区；涉及 infra 时验证数据库往返。

## Closure

本批所有 Task 完成、验证通过并同步必要接口或数据库文档后删除本 RUNBOOK。未完成范围必须收窄为新的独立 RUNBOOK，不保留完成历史。
