# ArchUnit allowlist 清理 02：Discovery

## Purpose

删除 Discovery 的全部 legacy ArchUnit allowlist，并以符合当前架构规则的生产代码替代每一项例外。完成后，Discovery application、domain、interface 的架构测试不得传入任何 Discovery 专属 allowlist。

## Scope

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/`（仅限任务 10 的仓储实现和直接测试）
- 本 RUNBOOK。

## Non-goals

- 不修改 `kuzhambu-common-test` 的 ArchUnit support、规则白名单或规则实现。
- 不修改非 Discovery 域的生产代码；任务 12 至 15 仅更新本仓库内受 Discovery URL 变更影响的调用方、测试和 Discovery 接口文档。
- 不保留、替换或新增 Discovery legacy allowlist；每完成一个整改项即删除其对应 key。

## Execution Rules

1. 严格按下列任务编号执行；前置任务未完成时不得开始后续依赖任务。
2. 每个任务只改“Files”列出的 2 至 12 个文件。不得以“直接调用方”或“直接测试”为理由增加未列出的文件；需要更多文件时，新增一个后续任务。
3. 将 Lombok Command/Query 类改为 `record` 时，删除 Lombok 的 `@Getter`、`@Setter`、`@NoArgsConstructor`、`@AllArgsConstructor` 和对应 import；把调用方的 `getXxx()` 改为 `xxx()`，并把逐字段 setter 构造改为 record 构造器。
4. 每个任务完成后，从对应架构测试中删除已完成 key；不得先删 key 再修生产代码。
5. 每个任务完成后执行该任务模块的 `mvn -pl <module> -am test`；失败时只修复该任务涉及的代码和直接测试。

## Plan

### 1. 将 QA Command 改为 record

Files（7）：

- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/command/AskQuestionCommand.java`
- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/command/ChatCompletionCommand.java`
- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/command/DeleteQaSessionCommand.java`
- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/command/ExportQaSessionCommand.java`
- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/command/OpenQaSessionCommand.java`
- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/command/SyncKnowledgeContentCommand.java`
- `kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/DiscoveryApplicationCommandQueryRecordAllowances.java`

1. 将以上六个类逐一改为字段同序的 Java `record`，不改变字段名、字段类型、默认值语义或校验语义。
2. 更新 application 和 interface 中上述类型的直接构造与 accessor 调用。
3. 从 `DiscoveryApplicationCommandQueryRecordAllowances.java` 删除这六个 `COMMAND_QUERY_RECORD` key。

### 2. 将 Search Command/Query 改为 record

Files（9）：

- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/command/SearchClickEventCreateCommand.java`
- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/command/SearchPublicationPrepareCommand.java`
- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/command/SearchPublicationReferenceCommand.java`
- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/query/SearchPreviewQuery.java`
- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/query/SearchPublicationCategoryAggregationQuery.java`
- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/query/SearchQuery.java`
- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/query/SearchStatisticsSummaryQuery.java`
- `kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/DiscoveryApplicationCommandQueryRecordAllowances.java`
- `kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/DiscoveryApplicationArchitectureTest.java`

1. 依任务 1 的 record 迁移规则改造上述七个文件。
2. 更新 application、interface 和直接测试中的构造及 accessor 调用。
3. 从 `DiscoveryApplicationCommandQueryRecordAllowances.java` 删除其余七个 `COMMAND_QUERY_RECORD` key；该文件没有任何 key 后删除该文件，并删除 `DiscoveryApplicationArchitectureTest.java` 对它的引用。

### 3. 消除 ApplicationService 裸参数：Search 与索引同步

Files（11）：

- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/SearchApplicationService.java`
- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImpl.java`
- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/SearchIndexSyncApplicationService.java`
- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchIndexSyncApplicationServiceImpl.java`
- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/SearchIndexCleanupApplicationService.java`
- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchIndexCleanupApplicationServiceImpl.java`
- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/query/SearchEventQuery.java`
- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/command/SearchIndexSyncUpsertCommand.java`
- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/command/SearchIndexSyncDeleteCommand.java`
- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/command/SearchIndexCleanupCommand.java`
- `kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/DiscoveryApplicationArchitectureTest.java`

1. 为 `getEvent`、`syncUpsert`、`syncDelete` 和 `cleanupDeletedDocuments` 分别使用已有或新增的专用 Command/Query 作为唯一方法参数；禁止保留裸 `Long`、`String`、`Integer` 或 `int` 参数。
2. 同步修改接口、实现、直接调用方和测试，使参数传递只经过该专用契约。
3. 删除 `legacyApplicationServiceBoundaryAllowances()` 中这四个 `METHOD_SHAPE` key。

### 4. 消除 ApplicationService 裸参数：QA 查询

Files（8）：

- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/QaApplicationService.java`
- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImpl.java`
- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/query/PortalQaSessionDetailQuery.java`
- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/query/QaSessionDetailQuery.java`
- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/query/PortalQaSessionPageQuery.java`
- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/query/QaMessageSourcesQuery.java`
- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/query/QaRetrievalTraceQuery.java`
- `kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/DiscoveryApplicationArchitectureTest.java`

1. 为 `getPortalSessionDetail`、`getSessionDetail`、`listPortalSessions`、`listSourcesByMessageId`、`getTraceByTraceId` 分别定义或复用一个专用 Query；每个方法只接收一个 Query。
2. 更新 interface assembler、controller、实现和直接测试，删除所有旧形参调用。
3. 删除 `legacyApplicationServiceBoundaryAllowances()` 中这五个 QA `METHOD_SHAPE` key。

### 5. 消除 ApplicationService 裸参数：报表

Files（4）：

- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/report/service/DiscoveryReportApplicationService.java`
- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/report/service/impl/DiscoveryReportApplicationServiceImpl.java`
- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/report/query/DiscoveryReportSummaryQuery.java`
- `kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/DiscoveryApplicationArchitectureTest.java`

1. 新建或复用一个专用 Query，替换 `summary(Instant, Instant, String)` 的三个裸参数。
2. 更新全部直接调用方和测试为单一 Query 调用。
3. 删除最后一个 `METHOD_SHAPE` key；`legacyApplicationServiceBoundaryAllowances()` 为空后删除该方法及其 `ArchitectureRuleAllowance`、`List` import。

### 6. 将 QA Admin Controller 的 Command/Query 构造移至 assembler

Files（3）：

- `kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/DiscoveryQaAdminController.java`
- `kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/assembler/DiscoveryQaAdminInterfaceAssembler.java`
- `kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/DiscoveryApplicationArchitectureTest.java`

1. 在 `DiscoveryQaAdminInterfaceAssembler` 增加 request 到 `SyncKnowledgeContentCommand`、`KnowledgeSyncItemQuery`、`DeleteQaSessionCommand`、`QaSessionQuery`、`ExportQaSessionCommand` 的转换方法。
2. 将 controller 中七处 `new` 调用全部替换为 assembler 调用；controller 不得直接构造 application Command/Query。
3. 删除 `legacyCommandQueryConstructionAllowances()` 的全部七个 key；该方法为空后删除它及 `constructionViolation`。

### 7. 移除 portal assembler 的 null Command/Query 返回

Files（3）：

- `kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/assembler/DiscoveryQaPortalInterfaceAssembler.java`
- `kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/DiscoveryQaPortalController.java`
- `kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/DiscoveryApplicationArchitectureTest.java`

1. 将 `toOpenSessionCommand`、`toChatCompletionCommand`、`toDeleteSessionCommand`、`toExportSessionCommand` 改为非空输入、非空返回；移除每个 `return null` 分支。
2. controller 在调用 assembler 前使用 `@Valid @RequestBody` 输入，不传递 null；对应测试改为断言拒绝无效请求，而不是断言 null Command。
3. 删除这四个 `COMMAND_QUERY_ASSEMBLER_NULL_RETURN` key。

### 8. 移除 Search assembler 的 null Command/Query 返回

Files（3）：

- `kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/assembler/DiscoverySearchPortalInterfaceAssembler.java`
- `kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/assembler/DiscoverySearchStatisticsInterfaceAssembler.java`
- `kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/DiscoveryApplicationArchitectureTest.java`

1. 移除 portal assembler 三个和 statistics assembler 五个 Command/Query 转换方法中的 `return null` 分支。
2. 为所有 public 转换方法声明非空输入和输出，调用方只传入已验证 request。
3. 删除余下八个 `COMMAND_QUERY_ASSEMBLER_NULL_RETURN` key；该 allowlist 方法为空后删除它及 `nullReturnViolation`。

### 9. 移除 application facade assembler 的 nullness 例外

Files（3）：

- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/facade/assembler/DiscoveryFacadeAssembler.java`
- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/facade/assembler/DiscoverySearchPublicationFacadeAssembler.java`
- `kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/DiscoveryApplicationArchitectureTest.java`

1. 为两个 assembler 的每个 public 方法补齐非空契约，并消除 null 输入或 null 返回分支。
2. 删除 application architecture test 中 `BoundaryAssemblerNullnessAllowances.legacyClasses(...)` 的两个类名及无用 import。

### 10. 修正 QA Repository 方法命名

Files（7）：

- `kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaSessionRepository.java`
- `kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSessionRepositoryImpl.java`
- `kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImpl.java`
- `kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/mapper/QaSessionMapper.java`
- `kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImplTest.java`
- `kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSessionRepositoryImplTest.java`
- `kuzhambu-discovery-domain/src/test/java/com/thundax/kuzhambu/discovery/domain/DiscoveryDomainArchitectureTest.java`

1. 将 `markRemoved(QaSessionId, Instant)` 重命名为架构规则接受的仓储动词 `delete(QaSessionId, Instant)`；保持软删除更新语义不变。
2. 更新 application、infra mapper 调用和直接测试中的引用；不修改 mapper 的 SQL 方法名，除非编译错误要求同步改名。
3. 从 `DiscoveryDomainArchitectureTest.java` 删除 `legacyRepositoryInterfaceMethodNameAllowances(...)`，改为不带 allowlist 的规则调用。

### 已废止：原任务 11

本节不得执行；执行任务 12A。

Files（2）：

- `kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/response/DiscoveryQaResponses.java`
- `kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/DiscoveryInterfaceArchitectureTest.java`

1. 为 `ChatCompletionsResponse`、`OpenSessionResponse`、`QaMessageResponse`、`QaSessionDetailResponse`、`QaSessionExportResponse`、`QaSessionResponse`、`QaSourceResponse` 添加 `@Builder`，移除 `@Setter`；保留 JSON 字段名和序列化注解。
2. 将所有上述 response 的生产构造改为 builder；不得保留无参构造后 setter 赋值。
3. 删除 `legacyResponseAnnotationAllowances()`、其中七个 key 及不再使用的 `ArchitectureRuleAllowance`/`List` import。
4. 删除 interface architecture test 中四个 `BoundaryAssemblerNullnessAllowances.legacyClasses(...)` 类名及无用 import。

### 已废止：原任务 12

本节不得执行；执行任务 12B、12C、12D。

Files（6）：

- `kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/DiscoveryQaPortalController.java`
- `kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/DiscoveryQaPortalStreamController.java`
- `kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/DiscoveryQaAdminController.java`
- `kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/DiscoveryQaConversationController.java`
- `kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/DiscoveryQaConversationStreamController.java`
- `kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/DiscoverySearchStatisticsController.java`

1. 仅使用共享白名单中的 action verb：`page`、`list`、`get`、`init`、`create`、`delete`、`update`、`search`、`rebuild`、`preview` 等。
2. 对每个 mapped method，同时把 Java 方法名和 `@PostMapping` 最后一个路径段改为同一个白名单动词；stream 与非 stream endpoint 使用不同白名单动词，避免同一 controller mapping 重复。
3. 全仓库搜索旧 URL 和旧 Java 方法名，更新 Discovery admin-web、portal-web 及接口测试中的调用；不得保留旧 route 别名。
4. 删除 `legacyActionVerbAllowances()`、`actionVerbAllowance()` 和六个 `CONTROLLER_ACTION_VERB` key。至此 `DiscoveryInterfaceArchitectureTest.java` 不得再引用任何 Discovery allowlist 辅助类或 `ArchitectureRuleAllowance`。

### 12A. Portal QA response 实施（替换原任务 11）

Files（5）：

- `kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/response/DiscoveryQaResponses.java`
- `kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/assembler/DiscoveryQaPortalInterfaceAssembler.java`
- `kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/DiscoveryQaPortalStreamController.java`
- `kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/DiscoveryQaConversationStreamController.java`
- `kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/DiscoveryInterfaceArchitectureTest.java`

1. 此任务替换原任务 11；不执行原任务 11 的 Files 或步骤。
2. 为除 `QaSessionDetailResponse` 外的六个 allowlisted response 添加 `@Builder`、移除 `@Setter`；为 `QaSessionDetailResponse` 使用 `@Builder(builderMethodName = "detailBuilder")`，避免与父类的 `builder()` 冲突。
3. 将列出的 assembler 和 stream controller 中的 response 无参构造与 setter 赋值全部改为 builder；子类使用 `detailBuilder()`。
4. 删除 response allowlist 和四个 interface assembler nullness allowlist。

### 12B. Portal QA route 迁移（替换原任务 12 的 Portal 部分）

Files（7）：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/DiscoveryQaPortalController.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/DiscoveryQaPortalStreamController.java`
- `kuzhambu-apps/portal-web/src/pages/discovery/qa/qa-service.ts`
- `kuzhambu-apps/portal-web/e2e/discovery/qa/qa.spec.ts`
- `kuzhambu-apps/portal-web/src/pages/discovery/qa/qa-page-context.test.tsx`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/DiscoveryQaPortalControllerTest.java`
- `docs/30-designs/DISCOVERY-QA-KNOWLEDGE-SPECIAL-DESIGN.md`

1. 固定替换：`openSession`/`session/open` 为 `initSession`/`session/init`；`exportSession`/`session/export` 为 `downloadSession`/`session/download`；`chatCompletions`/`chat/completions` 为 `createChatCompletion`/`chat/create`；`chatCompletionsStream`/`chat/completions/stream` 为 `submitChatCompletion`/`chat/submit`。
2. 在其余六个文件中替换上述旧 URL；不保留旧 route。

### 12C. Admin QA route 迁移（替换原任务 12 的 Admin 部分）

Files（10）：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/DiscoveryQaConversationController.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/DiscoveryQaConversationStreamController.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/DiscoveryQaAdminController.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/DiscoverySearchStatisticsController.java`
- `kuzhambu-apps/admin-web/src/pages/discovery/qa/qa-service.ts`
- `kuzhambu-apps/admin-web/src/pages/discovery/qa/qa-service-contract.test.ts`
- `kuzhambu-apps/admin-web/src/pages/discovery/qa-console/qa-console-service.ts`
- `kuzhambu-apps/admin-web/src/pages/discovery/qa-console/qa-console-service-contract.test.ts`
- `kuzhambu-apps/admin-web/src/pages/discovery/search-statistic/search-statistic-service.ts`
- `kuzhambu-apps/admin-web/src/pages/discovery/search-statistic/search-statistic-service-contract.test.ts`

1. Conversation controller 应用任务 12B 的四组映射；QA-admin 固定替换 `knowledge/health -> knowledge/get`、`syncKnowledge`/`knowledge/sync -> updateKnowledge`/`knowledge/update`、`exportSession`/`session/export -> downloadSession`/`session/download`；statistics 固定替换 `getStatisticsSummary`/`summary -> getSummary`/`summary/get`。
2. 更新列出的六个 admin-web service 与契约测试；不保留旧 route。

### 12D. Interface allowlist 收口与 API 文档

Files（3）：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/DiscoveryInterfaceArchitectureTest.java`
- `docs/30-designs/DISCOVERY-QA-KNOWLEDGE-SPECIAL-DESIGN.md`
- `docs/30-designs/DISCOVERY-DESIGN.md`

1. 在两个设计文档中以任务 12B 和 12C 的映射替换所有旧 API 表项。
2. 删除 `legacyActionVerbAllowances()`、`actionVerbAllowance()`、六个 `CONTROLLER_ACTION_VERB` key，以及无用 `ArchitectureRuleAllowance` 和 `BoundaryAssemblerNullnessAllowances` import。

## Verification

在每个任务完成后，先格式化该任务触及的 Discovery Java 模块：

```sh
cd kuzhambu-servers
mvn -pl <changed-module> spotless:apply
mvn -pl <changed-module> test
```

全部任务完成后，按以下顺序执行：

```sh
cd kuzhambu-servers
mvn -pl biz/discovery/kuzhambu-discovery-application,biz/discovery/kuzhambu-discovery-domain,biz/discovery/kuzhambu-discovery-interface,biz/discovery/kuzhambu-discovery-infra -am test
mvn spotless:check
mvn checkstyle:check
```

在最终验证前执行以下静态确认，三条命令均须无输出：

```sh
rg -n 'legacyAllowances|legacyApplicationServiceBoundaryAllowances|legacyCommandQueryConstructionAllowances|legacyAssemblerNullReturnAllowances|legacyResponseAnnotationAllowances|legacyActionVerbAllowances' biz/discovery
rg -n 'ArchitectureRuleAllowance|BoundaryAssemblerNullnessAllowances' biz/discovery/kuzhambu-discovery-{application,interface}/src/test/java
rg -n 'markRemoved\(' biz/discovery
```

格式化后检查 `git diff -- docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-02-DISCOVERY.md kuzhambu-servers`，仅保留本 RUNBOOK 和 Discovery allowlist 清理相关变更。

## Closure

全部静态确认和 Maven 验证通过后，删除本 RUNBOOK，并删除仓库中对其的残留引用。若需要保留验证结果，将命令、日期和结果摘要写入对应的 `docs/40-readiness/` evidence 文档；不得保留临时 RUNBOOK 作为完成证据。
