# discovery Java Time 迁移 RUNBOOK（第 2/2 批）

## Purpose

独立完成 `discovery` 本批旧时间类型迁移，闭合字段、签名、调用方、持久化与协议边界。本文件包含执行所需的完整上下文，不依赖其他迁移 RUNBOOK。

## Scope

- 本批包含 **25** 个生产文件。
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
| `application` | `search` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/query/SearchEventPageQuery.java` | `dateFrom`(L19), `dateTo`(L20) | — |
| `application` | `search` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/query/SearchQuery.java` | `dateFrom`(L23), `dateTo`(L24) | — |
| `application` | `search` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/query/SearchStatisticsSummaryQuery.java` | `dateFrom`(L14), `dateTo`(L15) | — |
| `application` | `search` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/result/SearchSourceContent.java` | `publishedAt`(L28), `updatedAt`(L29) | L44 `Date publishedAt,`<br>操作行 L45 |
| `application` | `search` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/SearchIndexSyncApplicationService.java` | — | L9 `Boolean syncDelete(String contentType, String contentId, Integer currentVersionNo, Date occurredAt);` |
| `application` | `search` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/QueryUnderstandingApplicationServiceImpl.java` | — | 操作行 L174, L199 |
| `application` | `search` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImpl.java` | — | L186 `Date dateFrom = query == null ? null : query.getDateFrom();`<br>L187 `Date dateTo = query == null ? null : query.getDateTo();`<br>操作行 L153, L224, L254 |
| `application` | `search` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchIndexSyncApplicationServiceImpl.java` | — | L50 `public Boolean syncDelete(String contentType, String contentId, Integer currentVersionNo, Date occurredAt) {` |
| `domain` | `search` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/entity/QueryUnderstanding.java` | `createdAt`(L31) | 操作行 L47 |
| `domain` | `search` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/entity/SearchClickEvent.java` | `createdAt`(L32) | 操作行 L50 |
| `domain` | `search` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/entity/SearchEvent.java` | `createdAt`(L34) | 操作行 L54 |
| `domain` | `search` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/valueobject/SearchScope.java` | `dateFrom`(L21), `dateTo`(L22) | — |
| `domain` | `search` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/repository/SearchClickEventRepository.java` | — | L17 `long countByCreatedAtRange(Date createdAtStart, Date createdAtEnd);` |
| `domain` | `search` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/repository/SearchEventRepository.java` | — | L19 `List<SearchEvent> listByCreatedAtRange(Date createdAtStart, Date createdAtEnd);` |
| `infra` | `search` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/dataobject/QueryUnderstandingDO.java` | `createdAt`(L31) | — |
| `infra` | `search` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/dataobject/SearchClickEventDO.java` | `createdAt`(L33) | — |
| `infra` | `search` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/dataobject/SearchEventDO.java` | `createdAt`(L35) | — |
| `infra` | `search` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/mapper/SearchClickEventMapper.java` | — | L19 `Long countByCreatedAtRange(@Param("createdAtStart") Date createdAtStart, @Param("createdAtEnd") Date createdAtEnd);` |
| `infra` | `search` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/mapper/SearchEventMapper.java` | — | L22 `@Param("createdAtStart") Date createdAtStart, @Param("createdAtEnd") Date createdAtEnd);` |
| `infra` | `search` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/repository/impl/SearchClickEventRepositoryImpl.java` | — | L35 `public long countByCreatedAtRange(Date createdAtStart, Date createdAtEnd) {` |
| `infra` | `search` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/repository/impl/SearchEventRepositoryImpl.java` | — | L41 `public List<SearchEvent> listByCreatedAtRange(Date createdAtStart, Date createdAtEnd) {` |
| `interface` | `search` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/assembler/DiscoverySearchStatisticsInterfaceAssembler.java` | — | L220 `private static Date toDate(Long value) {`<br>L285 `private static Date parseDate(String value, String fieldName) {`<br>操作行 L221, L290 |
| `interface` | `search` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/response/DiscoverySearchEventDetailResponse.java` | `createdAt`(L79) | — |
| `interface` | `search` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/response/DiscoverySearchEventResponse.java` | `createdAt`(L55) | — |
| `interface` | `search` | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/assembler/DiscoverySearchPortalInterfaceAssembler.java` | — | L177 `private static Date parseDateFrom(String value) {`<br>L181 `private static Date parseDateTo(String value) {`<br>L185 `private static Date parseDate(String value, boolean endOfDay) {`<br>L197 `private static Date parseLocalDate(String value, boolean endOfDay) {`<br>操作行 L191, L203 |

## Plan

### Task 1: search 时间类型闭环

涉及生产文件：**8** 个。

#### Files And Changes

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/query/SearchEventPageQuery.java`
  - L19 `dateFrom`：`Date` → `LocalDate` 候选；必须核对协议、数据库列和比较精度。
  - L20 `dateTo`：`Date` → `LocalDate` 候选；必须核对协议、数据库列和比较精度。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/query/SearchQuery.java`
  - L23 `dateFrom`：`Date` → `LocalDate` 候选；必须核对协议、数据库列和比较精度。
  - L24 `dateTo`：`Date` → `LocalDate` 候选；必须核对协议、数据库列和比较精度。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/query/SearchStatisticsSummaryQuery.java`
  - L14 `dateFrom`：`Date` → `LocalDate` 候选；必须核对协议、数据库列和比较精度。
  - L15 `dateTo`：`Date` → `LocalDate` 候选；必须核对协议、数据库列和比较精度。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/result/SearchSourceContent.java`
  - L28 `publishedAt`：`Date` → `Instant`。
  - L29 `updatedAt`：`Date` → `Instant`。
  - L44：将签名 `Date publishedAt,` 的 `Date` 与本调用链目标类型同步。
  - L45：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/SearchIndexSyncApplicationService.java`
  - L9：将签名 `Boolean syncDelete(String contentType, String contentId, Integer currentVersionNo, Date occurredAt);` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/QueryUnderstandingApplicationServiceImpl.java`
  - L174, L199：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImpl.java`
  - L186：将签名 `Date dateFrom = query == null ? null : query.getDateFrom();` 的 `Date` 与本调用链目标类型同步。
  - L187：将签名 `Date dateTo = query == null ? null : query.getDateTo();` 的 `Date` 与本调用链目标类型同步。
  - L153, L224, L254：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchIndexSyncApplicationServiceImpl.java`
  - L50：将签名 `public Boolean syncDelete(String contentType, String contentId, Integer currentVersionNo, Date occurredAt) {` 的 `Date` 与本调用链目标类型同步。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

### Task 2: search 时间类型闭环

涉及生产文件：**8** 个。

#### Files And Changes

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/entity/QueryUnderstanding.java`
  - L31 `createdAt`：`Date` → `Instant`。
  - L47：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/entity/SearchClickEvent.java`
  - L32 `createdAt`：`Date` → `Instant`。
  - L50：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/entity/SearchEvent.java`
  - L34 `createdAt`：`Date` → `Instant`。
  - L54：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/valueobject/SearchScope.java`
  - L21 `dateFrom`：`Date` → `LocalDate` 候选；必须核对协议、数据库列和比较精度。
  - L22 `dateTo`：`Date` → `LocalDate` 候选；必须核对协议、数据库列和比较精度。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/repository/SearchClickEventRepository.java`
  - L17：将签名 `long countByCreatedAtRange(Date createdAtStart, Date createdAtEnd);` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/repository/SearchEventRepository.java`
  - L19：将签名 `List<SearchEvent> listByCreatedAtRange(Date createdAtStart, Date createdAtEnd);` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/dataobject/QueryUnderstandingDO.java`
  - L31 `createdAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/dataobject/SearchClickEventDO.java`
  - L33 `createdAt`：`Date` → `Instant`。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

### Task 3: search 时间类型闭环

涉及生产文件：**7** 个。

#### Files And Changes

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/dataobject/SearchEventDO.java`
  - L35 `createdAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/mapper/SearchClickEventMapper.java`
  - L19：将签名 `Long countByCreatedAtRange(@Param("createdAtStart") Date createdAtStart, @Param("createdAtEnd") Date createdAtEnd);` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/mapper/SearchEventMapper.java`
  - L22：将签名 `@Param("createdAtStart") Date createdAtStart, @Param("createdAtEnd") Date createdAtEnd);` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/repository/impl/SearchClickEventRepositoryImpl.java`
  - L35：将签名 `public long countByCreatedAtRange(Date createdAtStart, Date createdAtEnd) {` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/repository/impl/SearchEventRepositoryImpl.java`
  - L41：将签名 `public List<SearchEvent> listByCreatedAtRange(Date createdAtStart, Date createdAtEnd) {` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/assembler/DiscoverySearchStatisticsInterfaceAssembler.java`
  - L220：将签名 `private static Date toDate(Long value) {` 的 `Date` 与本调用链目标类型同步。
  - L285：将签名 `private static Date parseDate(String value, String fieldName) {` 的 `Date` 与本调用链目标类型同步。
  - L221, L290：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/response/DiscoverySearchEventDetailResponse.java`
  - L79 `createdAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

### Task 4: search 时间类型闭环

涉及生产文件：**2** 个。

#### Files And Changes

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/response/DiscoverySearchEventResponse.java`
  - L55 `createdAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/assembler/DiscoverySearchPortalInterfaceAssembler.java`
  - L177：将签名 `private static Date parseDateFrom(String value) {` 的 `Date` 与本调用链目标类型同步。
  - L181：将签名 `private static Date parseDateTo(String value) {` 的 `Date` 与本调用链目标类型同步。
  - L185：将签名 `private static Date parseDate(String value, boolean endOfDay) {` 的 `Date` 与本调用链目标类型同步。
  - L197：将签名 `private static Date parseLocalDate(String value, boolean endOfDay) {` 的 `Date` 与本调用链目标类型同步。
  - L191, L203：调整当前时间构造、比较、算术或转换。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

## Associated Test Files

下列测试按本批主题匹配；执行时还必须根据编译和真实调用链补充测试。

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchIndexSyncApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/client/DiscoverySearchDocumentAssemblerTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/client/ElasticsearchSearchIndexGatewayTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/search/persistence/assembler/SearchEventPersistenceAssemblerTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/search/persistence/mapper/SearchMapperParameterTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/search/repository/impl/SearchClickEventRepositoryImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/DiscoverySearchStatisticsControllerTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/mq/RocketMqDiscoverySearchIndexSyncConsumerTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/assembler/DiscoverySearchPortalInterfaceAssemblerTest.java`

## Verification

- `rg 'java\.util\.Date|java\.sql\.(Date|Timestamp)' kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/query/SearchEventPageQuery.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/query/SearchQuery.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/query/SearchStatisticsSummaryQuery.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/result/SearchSourceContent.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/SearchIndexSyncApplicationService.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/QueryUnderstandingApplicationServiceImpl.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImpl.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchIndexSyncApplicationServiceImpl.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/entity/QueryUnderstanding.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/entity/SearchClickEvent.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/entity/SearchEvent.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/valueobject/SearchScope.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/repository/SearchClickEventRepository.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/repository/SearchEventRepository.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/dataobject/QueryUnderstandingDO.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/dataobject/SearchClickEventDO.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/dataobject/SearchEventDO.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/mapper/SearchClickEventMapper.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/mapper/SearchEventMapper.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/repository/impl/SearchClickEventRepositoryImpl.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/repository/impl/SearchEventRepositoryImpl.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/assembler/DiscoverySearchStatisticsInterfaceAssembler.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/response/DiscoverySearchEventDetailResponse.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/response/DiscoverySearchEventResponse.java kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/assembler/DiscoverySearchPortalInterfaceAssembler.java`。
- `cd kuzhambu-servers && mvn -pl :kuzhambu-discovery-domain,:kuzhambu-discovery-application,:kuzhambu-discovery-facade,:kuzhambu-discovery-interface,:kuzhambu-discovery-infra spotless:apply`，随后检查 `git diff`。
- `cd kuzhambu-servers && mvn spotless:check`。
- `cd kuzhambu-servers && mvn checkstyle:check`。
- `cd kuzhambu-servers && mvn -pl :kuzhambu-discovery-domain,:kuzhambu-discovery-application,:kuzhambu-discovery-facade,:kuzhambu-discovery-interface,:kuzhambu-discovery-infra -am -amd test`；确认 Reactor Build Order 包含上述 5 个叶子模块及依赖其 facade 的跨域消费者。
- 涉及 HTTP 时验证 JSON 精度、格式和时区；涉及 infra 时验证数据库往返。

## Closure

本批所有 Task 完成、验证通过并同步必要接口或数据库文档后删除本 RUNBOOK。未完成范围必须收窄为新的独立 RUNBOOK，不保留完成历史。
