# knowledge Java Time 迁移 RUNBOOK

## Purpose

独立完成 `knowledge` 本批旧时间类型迁移，闭合字段、签名、调用方、持久化与协议边界。本文件包含执行所需的完整上下文，不依赖其他迁移 RUNBOOK。

## Scope

- 本批包含 **49** 个生产文件。
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
| `facade` | `facade` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/request/KnowledgeSummaryFacadeRequest.java` | `periodStart`(L14), `periodEnd`(L15) | — |
| `facade` | `facade` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/response/KnowledgeSummaryFacadeResponse.java` | `periodStart`(L19), `periodEnd`(L20) | — |
| `application` | `graph` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/service/impl/KnowledgeGraphExtractionApplicationServiceImpl.java` | — | L1173 `private Long timeValue(Date value) {` |
| `domain` | `graph` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/model/entity/KnowledgeLineageNode.java` | `firstExtractedAt`(L23), `lastExtractedAt`(L24), `confirmedAt`(L25) | — |
| `domain` | `graph` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/model/entity/KnowledgeLineageRelation.java` | `firstExtractedAt`(L25), `lastExtractedAt`(L26), `confirmedAt`(L27) | — |
| `domain` | `graph` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/model/entity/KnowledgeRelation.java` | `firstExtractedAt`(L25), `lastExtractedAt`(L26), `confirmedAt`(L27) | — |
| `infra` | `graph` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/assembler/GraphVersionPersistenceAssembler.java` | — | L62 `private static Date toDate(Instant value) {`<br>L66 `private static Instant toInstant(Date value) {`<br>操作行 L63 |
| `infra` | `graph` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/assembler/KnowledgeEntityPersistenceAssembler.java` | — | L65 `private static Date toDate(Instant value) {`<br>L69 `private static Instant toInstant(Date value) {`<br>操作行 L66 |
| `infra` | `graph` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/assembler/KnowledgeGraphPersistenceAssembler.java` | — | L120 `private static Date toDate(Instant value) {`<br>L124 `private static Instant toInstant(Date value) {`<br>操作行 L121 |
| `infra` | `graph` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/GraphExtractionTaskDO.java` | `requestedAt`(L48), `completedAt`(L49), `appliedAt`(L50) | — |
| `infra` | `graph` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/GraphVersionDO.java` | `appliedAt`(L31) | — |
| `infra` | `graph` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/KnowledgeEntityDO.java` | `firstExtractedAt`(L27), `lastExtractedAt`(L28), `confirmedAt`(L29) | — |
| `infra` | `graph` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/KnowledgeLineageNodeDO.java` | `firstExtractedAt`(L28), `lastExtractedAt`(L29), `confirmedAt`(L30) | — |
| `infra` | `graph` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/KnowledgeLineageRelationDO.java` | `firstExtractedAt`(L30), `lastExtractedAt`(L31), `confirmedAt`(L32) | — |
| `infra` | `graph` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/KnowledgeRelationDO.java` | `firstExtractedAt`(L30), `lastExtractedAt`(L31), `confirmedAt`(L32) | — |
| `application` | `lineage` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/lineage/service/impl/KnowledgeLineageReadApplicationServiceImpl.java` | — | L433 `private Long toEpochMillis(Date date) {` |
| `application` | `refinement` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/result/QualityReportDetailResult.java` | `generatedAt`(L58), `publishedAt`(L59), `appliedAt`(L90) | — |
| `application` | `refinement` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/impl/KnowledgeGraphRefinementApplicationServiceImpl.java` | — | 操作行 L151, L215, L229, L258, L272, L302, L317, L347, L362, L380, L382, L440, L454, L479, L507, L533 |
| `application` | `refinement` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/impl/KnowledgeQualityReportApplicationServiceImpl.java` | — | L256 `Date now,`<br>L604 `private String reportNo(Date now, Long graphVersionId) {`<br>L614 `private Date toDate(Instant instant) {`<br>操作行 L140, L307, L359, L389, L411, L615 |
| `application` | `refinement` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/support/RefinementApplySupport.java` | — | 操作行 L127, L146, L163 |
| `application` | `refinement` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/support/RefinementDraftBootstrapSupport.java` | — | 操作行 L42, L72, L103, L132 |
| `domain` | `refinement` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/QualityAnnotation.java` | `createdAt`(L25), `updatedAt`(L27) | — |
| `domain` | `refinement` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/QualityReport.java` | `generatedAt`(L37), `publishedAt`(L38), `createdAt`(L39), `updatedAt`(L40) | — |
| `domain` | `refinement` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/QualityReportIssue.java` | `createdAt`(L26) | — |
| `domain` | `refinement` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/QualityReportSourceDetail.java` | `appliedAt`(L22), `createdAt`(L27) | — |
| `domain` | `refinement` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/RefinementEntityDraft.java` | `createdAt`(L28), `updatedAt`(L30) | — |
| `domain` | `refinement` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/RefinementLineageNodeDraft.java` | `createdAt`(L29), `updatedAt`(L31) | — |
| `domain` | `refinement` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/RefinementLineageRelationDraft.java` | `createdAt`(L31), `updatedAt`(L33) | — |
| `domain` | `refinement` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/RefinementRelationDraft.java` | `createdAt`(L31), `updatedAt`(L33) | — |
| `domain` | `refinement` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/RefinementTask.java` | `openedAt`(L25), `submittedAt`(L27), `appliedAt`(L29), `cancelledAt`(L31) | — |
| `infra` | `refinement` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/QualityAnnotationDO.java` | `createdAt`(L29), `updatedAt`(L31) | — |
| `infra` | `refinement` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/QualityReportDO.java` | `generatedAt`(L41), `publishedAt`(L42), `createdAt`(L43), `updatedAt`(L44) | — |
| `infra` | `refinement` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/QualityReportIssueDO.java` | `createdAt`(L30) | — |
| `infra` | `refinement` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/QualityReportSourceDetailDO.java` | `appliedAt`(L26), `createdAt`(L31) | — |
| `infra` | `refinement` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/RefinementEntityDraftDO.java` | `createdAt`(L32), `updatedAt`(L34) | — |
| `infra` | `refinement` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/RefinementLineageNodeDraftDO.java` | `createdAt`(L33), `updatedAt`(L35) | — |
| `infra` | `refinement` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/RefinementLineageRelationDraftDO.java` | `createdAt`(L35), `updatedAt`(L37) | — |
| `infra` | `refinement` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/RefinementRelationDraftDO.java` | `createdAt`(L35), `updatedAt`(L37) | — |
| `infra` | `refinement` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/RefinementTaskDO.java` | `openedAt`(L28), `submittedAt`(L30), `appliedAt`(L32), `cancelledAt`(L34) | — |
| `interface` | `refinement` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/response/QualityReportResponses.java` | `generatedAt`(L49), `publishedAt`(L50), `appliedAt`(L76) | — |
| `application` | `report` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/report/result/KnowledgeReportSummaryResult.java` | `periodStart`(L17), `periodEnd`(L18) | — |
| `application` | `report` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/report/service/KnowledgeReportApplicationService.java` | — | L10 `KnowledgeReportSummaryResult summary(Date periodStart, Date periodEnd, String bucketType);` |
| `application` | `report` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/report/service/impl/KnowledgeReportApplicationServiceImpl.java` | — | L29 `public KnowledgeReportSummaryResult summary(Date periodStart, Date periodEnd, String bucketType) {` |
| `domain` | `service` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/service/impl/KnowledgeTagBindingDomainServiceImpl.java` | — | L126 `private Tag createTag(String normalizedName, TagSource source, TagReviewStatus reviewStatus, Date reviewedAt) {`<br>操作行 L76, L128 |
| `application` | `taxonomy` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagCreateCommand.java` | `reviewedAt`(L23) | — |
| `application` | `taxonomy` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImpl.java` | — | 操作行 L389, L390, L459, L476, L544, L746, L1015 |
| `domain` | `taxonomy` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/model/entity/Tag.java` | `createdAt`(L28), `reviewedAt`(L29), `deprecatedAt`(L31) | L43 `Date createdAt,`<br>L92 `public void deprecate(Date operatedAt, Long operatorId) {`<br>操作行 L44, L100 |
| `infra` | `taxonomy` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/persistence/dataobject/TagDO.java` | `createdAt`(L28), `reviewedAt`(L29), `deprecatedAt`(L31) | — |
| `interface` | `taxonomy` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagCreateRequest.java` | `reviewedAt`(L56) | — |

## Plan

### Task 1: facade 时间类型闭环

涉及生产文件：**2** 个。

#### Files And Changes

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/request/KnowledgeSummaryFacadeRequest.java`
  - L14 `periodStart`：`Date` → `Instant`。
  - L15 `periodEnd`：`Date` → `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/response/KnowledgeSummaryFacadeResponse.java`
  - L19 `periodStart`：`Date` → `Instant`。
  - L20 `periodEnd`：`Date` → `Instant`。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

### Task 2: graph 时间类型闭环

涉及生产文件：**8** 个。

#### Files And Changes

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/service/impl/KnowledgeGraphExtractionApplicationServiceImpl.java`
  - L1173：将签名 `private Long timeValue(Date value) {` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/model/entity/KnowledgeLineageNode.java`
  - L23 `firstExtractedAt`：`Date` → `Instant`。
  - L24 `lastExtractedAt`：`Date` → `Instant`。
  - L25 `confirmedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/model/entity/KnowledgeLineageRelation.java`
  - L25 `firstExtractedAt`：`Date` → `Instant`。
  - L26 `lastExtractedAt`：`Date` → `Instant`。
  - L27 `confirmedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/model/entity/KnowledgeRelation.java`
  - L25 `firstExtractedAt`：`Date` → `Instant`。
  - L26 `lastExtractedAt`：`Date` → `Instant`。
  - L27 `confirmedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/assembler/GraphVersionPersistenceAssembler.java`
  - L62：将签名 `private static Date toDate(Instant value) {` 的 `Date` 与本调用链目标类型同步。
  - L66：将签名 `private static Instant toInstant(Date value) {` 的 `Date` 与本调用链目标类型同步。
  - L63：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/assembler/KnowledgeEntityPersistenceAssembler.java`
  - L65：将签名 `private static Date toDate(Instant value) {` 的 `Date` 与本调用链目标类型同步。
  - L69：将签名 `private static Instant toInstant(Date value) {` 的 `Date` 与本调用链目标类型同步。
  - L66：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/assembler/KnowledgeGraphPersistenceAssembler.java`
  - L120：将签名 `private static Date toDate(Instant value) {` 的 `Date` 与本调用链目标类型同步。
  - L124：将签名 `private static Instant toInstant(Date value) {` 的 `Date` 与本调用链目标类型同步。
  - L121：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/GraphExtractionTaskDO.java`
  - L48 `requestedAt`：`Date` → `Instant`。
  - L49 `completedAt`：`Date` → `Instant`。
  - L50 `appliedAt`：`Date` → `Instant`。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

### Task 3: graph, lineage 时间类型闭环

涉及生产文件：**6** 个。

#### Files And Changes

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/GraphVersionDO.java`
  - L31 `appliedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/KnowledgeEntityDO.java`
  - L27 `firstExtractedAt`：`Date` → `Instant`。
  - L28 `lastExtractedAt`：`Date` → `Instant`。
  - L29 `confirmedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/KnowledgeLineageNodeDO.java`
  - L28 `firstExtractedAt`：`Date` → `Instant`。
  - L29 `lastExtractedAt`：`Date` → `Instant`。
  - L30 `confirmedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/KnowledgeLineageRelationDO.java`
  - L30 `firstExtractedAt`：`Date` → `Instant`。
  - L31 `lastExtractedAt`：`Date` → `Instant`。
  - L32 `confirmedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/KnowledgeRelationDO.java`
  - L30 `firstExtractedAt`：`Date` → `Instant`。
  - L31 `lastExtractedAt`：`Date` → `Instant`。
  - L32 `confirmedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/lineage/service/impl/KnowledgeLineageReadApplicationServiceImpl.java`
  - L433：将签名 `private Long toEpochMillis(Date date) {` 的 `Date` 与本调用链目标类型同步。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

### Task 4: refinement 时间类型闭环

涉及生产文件：**8** 个。

#### Files And Changes

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/result/QualityReportDetailResult.java`
  - L58 `generatedAt`：`Date` → `Instant`。
  - L59 `publishedAt`：`Date` → `Instant`。
  - L90 `appliedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/impl/KnowledgeGraphRefinementApplicationServiceImpl.java`
  - L151, L215, L229, L258, L272, L302, L317, L347, L362, L380, L382, L440, L454, L479, L507, L533：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/impl/KnowledgeQualityReportApplicationServiceImpl.java`
  - L256：将签名 `Date now,` 的 `Date` 与本调用链目标类型同步。
  - L604：将签名 `private String reportNo(Date now, Long graphVersionId) {` 的 `Date` 与本调用链目标类型同步。
  - L614：将签名 `private Date toDate(Instant instant) {` 的 `Date` 与本调用链目标类型同步。
  - L140, L307, L359, L389, L411, L615：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/support/RefinementApplySupport.java`
  - L127, L146, L163：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/support/RefinementDraftBootstrapSupport.java`
  - L42, L72, L103, L132：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/QualityAnnotation.java`
  - L25 `createdAt`：`Date` → `Instant`。
  - L27 `updatedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/QualityReport.java`
  - L37 `generatedAt`：`Date` → `Instant`。
  - L38 `publishedAt`：`Date` → `Instant`。
  - L39 `createdAt`：`Date` → `Instant`。
  - L40 `updatedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/QualityReportIssue.java`
  - L26 `createdAt`：`Date` → `Instant`。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

### Task 5: refinement 时间类型闭环

涉及生产文件：**8** 个。

#### Files And Changes

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/QualityReportSourceDetail.java`
  - L22 `appliedAt`：`Date` → `Instant`。
  - L27 `createdAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/RefinementEntityDraft.java`
  - L28 `createdAt`：`Date` → `Instant`。
  - L30 `updatedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/RefinementLineageNodeDraft.java`
  - L29 `createdAt`：`Date` → `Instant`。
  - L31 `updatedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/RefinementLineageRelationDraft.java`
  - L31 `createdAt`：`Date` → `Instant`。
  - L33 `updatedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/RefinementRelationDraft.java`
  - L31 `createdAt`：`Date` → `Instant`。
  - L33 `updatedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/RefinementTask.java`
  - L25 `openedAt`：`Date` → `Instant`。
  - L27 `submittedAt`：`Date` → `Instant`。
  - L29 `appliedAt`：`Date` → `Instant`。
  - L31 `cancelledAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/QualityAnnotationDO.java`
  - L29 `createdAt`：`Date` → `Instant`。
  - L31 `updatedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/QualityReportDO.java`
  - L41 `generatedAt`：`Date` → `Instant`。
  - L42 `publishedAt`：`Date` → `Instant`。
  - L43 `createdAt`：`Date` → `Instant`。
  - L44 `updatedAt`：`Date` → `Instant`。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

### Task 6: refinement 时间类型闭环

涉及生产文件：**8** 个。

#### Files And Changes

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/QualityReportIssueDO.java`
  - L30 `createdAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/QualityReportSourceDetailDO.java`
  - L26 `appliedAt`：`Date` → `Instant`。
  - L31 `createdAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/RefinementEntityDraftDO.java`
  - L32 `createdAt`：`Date` → `Instant`。
  - L34 `updatedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/RefinementLineageNodeDraftDO.java`
  - L33 `createdAt`：`Date` → `Instant`。
  - L35 `updatedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/RefinementLineageRelationDraftDO.java`
  - L35 `createdAt`：`Date` → `Instant`。
  - L37 `updatedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/RefinementRelationDraftDO.java`
  - L35 `createdAt`：`Date` → `Instant`。
  - L37 `updatedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/RefinementTaskDO.java`
  - L28 `openedAt`：`Date` → `Instant`。
  - L30 `submittedAt`：`Date` → `Instant`。
  - L32 `appliedAt`：`Date` → `Instant`。
  - L34 `cancelledAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/response/QualityReportResponses.java`
  - L49 `generatedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L50 `publishedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L76 `appliedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

### Task 7: report 时间类型闭环

涉及生产文件：**3** 个。

#### Files And Changes

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/report/result/KnowledgeReportSummaryResult.java`
  - L17 `periodStart`：`Date` → `Instant`。
  - L18 `periodEnd`：`Date` → `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/report/service/KnowledgeReportApplicationService.java`
  - L10：将签名 `KnowledgeReportSummaryResult summary(Date periodStart, Date periodEnd, String bucketType);` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/report/service/impl/KnowledgeReportApplicationServiceImpl.java`
  - L29：将签名 `public KnowledgeReportSummaryResult summary(Date periodStart, Date periodEnd, String bucketType) {` 的 `Date` 与本调用链目标类型同步。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

### Task 8: service, taxonomy 时间类型闭环

涉及生产文件：**6** 个。

#### Files And Changes

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/service/impl/KnowledgeTagBindingDomainServiceImpl.java`
  - L126：将签名 `private Tag createTag(String normalizedName, TagSource source, TagReviewStatus reviewStatus, Date reviewedAt) {` 的 `Date` 与本调用链目标类型同步。
  - L76, L128：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagCreateCommand.java`
  - L23 `reviewedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImpl.java`
  - L389, L390, L459, L476, L544, L746, L1015：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/model/entity/Tag.java`
  - L28 `createdAt`：`Date` → `Instant`。
  - L29 `reviewedAt`：`Date` → `Instant`。
  - L31 `deprecatedAt`：`Date` → `Instant`。
  - L43：将签名 `Date createdAt,` 的 `Date` 与本调用链目标类型同步。
  - L92：将签名 `public void deprecate(Date operatedAt, Long operatorId) {` 的 `Date` 与本调用链目标类型同步。
  - L44, L100：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/persistence/dataobject/TagDO.java`
  - L28 `createdAt`：`Date` → `Instant`。
  - L29 `reviewedAt`：`Date` → `Instant`。
  - L31 `deprecatedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagCreateRequest.java`
  - L56 `reviewedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

## Associated Test Files

下列测试按本批主题匹配；执行时还必须根据编译和真实调用链补充测试。

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/facade/impl/KnowledgeFacadeImplTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/graph/KnowledgeGraphExtractionApplicationServiceTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/lineage/KnowledgeLineageReadApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/KnowledgeGraphRefinementApplyTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/KnowledgeGraphRefinementTaskOpenTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/KnowledgeQualityReportApplicationServiceTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/report/service/impl/KnowledgeReportApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/graph/repository/impl/GraphExtractionTaskRepositoryImplTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/graph/repository/impl/KnowledgeLineageNodeRepositoryTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/graph/repository/impl/KnowledgeLineageRelationRepositoryTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/graph/repository/impl/KnowledgeRelationRepositoryTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/QualityAnnotationRepositoryTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/QualityReportRepositoryTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/RefinementEntityDraftRepositoryTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/RefinementLineageNodeDraftRepositoryTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/RefinementLineageRelationDraftRepositoryTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/RefinementRelationDraftRepositoryTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/TagGovernanceMetricsRepositoryImplTest.java`

## Verification

- `rg 'java\.util\.Date|java\.sql\.(Date|Timestamp)' kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/request/KnowledgeSummaryFacadeRequest.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/response/KnowledgeSummaryFacadeResponse.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/service/impl/KnowledgeGraphExtractionApplicationServiceImpl.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/model/entity/KnowledgeLineageNode.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/model/entity/KnowledgeLineageRelation.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/model/entity/KnowledgeRelation.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/assembler/GraphVersionPersistenceAssembler.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/assembler/KnowledgeEntityPersistenceAssembler.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/assembler/KnowledgeGraphPersistenceAssembler.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/GraphExtractionTaskDO.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/GraphVersionDO.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/KnowledgeEntityDO.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/KnowledgeLineageNodeDO.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/KnowledgeLineageRelationDO.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/KnowledgeRelationDO.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/lineage/service/impl/KnowledgeLineageReadApplicationServiceImpl.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/result/QualityReportDetailResult.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/impl/KnowledgeGraphRefinementApplicationServiceImpl.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/impl/KnowledgeQualityReportApplicationServiceImpl.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/support/RefinementApplySupport.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/support/RefinementDraftBootstrapSupport.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/QualityAnnotation.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/QualityReport.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/QualityReportIssue.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/QualityReportSourceDetail.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/RefinementEntityDraft.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/RefinementLineageNodeDraft.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/RefinementLineageRelationDraft.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/RefinementRelationDraft.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/RefinementTask.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/QualityAnnotationDO.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/QualityReportDO.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/QualityReportIssueDO.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/QualityReportSourceDetailDO.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/RefinementEntityDraftDO.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/RefinementLineageNodeDraftDO.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/RefinementLineageRelationDraftDO.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/RefinementRelationDraftDO.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/RefinementTaskDO.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/response/QualityReportResponses.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/report/result/KnowledgeReportSummaryResult.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/report/service/KnowledgeReportApplicationService.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/report/service/impl/KnowledgeReportApplicationServiceImpl.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/service/impl/KnowledgeTagBindingDomainServiceImpl.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagCreateCommand.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImpl.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/model/entity/Tag.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/persistence/dataobject/TagDO.java kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagCreateRequest.java`。
- `cd kuzhambu-servers && mvn -pl :kuzhambu-knowledge-domain,:kuzhambu-knowledge-application,:kuzhambu-knowledge-facade,:kuzhambu-knowledge-interface,:kuzhambu-knowledge-infra spotless:apply`，随后检查 `git diff`。
- `cd kuzhambu-servers && mvn spotless:check`。
- `cd kuzhambu-servers && mvn checkstyle:check`。
- `cd kuzhambu-servers && mvn -pl :kuzhambu-knowledge-domain,:kuzhambu-knowledge-application,:kuzhambu-knowledge-facade,:kuzhambu-knowledge-interface,:kuzhambu-knowledge-infra -am -amd test`；确认 Reactor Build Order 包含上述 5 个叶子模块及依赖其 facade 的跨域消费者。
- 涉及 HTTP 时验证 JSON 精度、格式和时区；涉及 infra 时验证数据库往返。

## Closure

本批所有 Task 完成、验证通过并同步必要接口或数据库文档后删除本 RUNBOOK。未完成范围必须收窄为新的独立 RUNBOOK，不保留完成历史。
