# ArchUnit allowlist 清理 09：Knowledge Taxonomy 与 Portal

## Purpose

清理 Knowledge 标签体系与 Portal 切片 legacy allowlist，使 application、domain、interface 三层重新满足当前 ArchUnit 规则。

本 RUNBOOK 只定义执行范围、文件级拆分、顺序和验证口径；具体代码修改在后续 TODO 中逐项执行。

## Scope

本切片覆盖以下 allowlist 来源文件：

| allowlist 类别 | 所在文件 | 本切片处理范围 |
| --- | --- | --- |
| application Command/Query record 例外 | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/KnowledgeApplicationCommandQueryRecordAllowances.java` | `application.portal.query.KnowledgePortalAtlasQuery` 与 `application.taxonomy.command/query` |
| application service 方法参数边界例外、application assembler nullness 例外 | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/KnowledgeApplicationArchitectureTest.java` | `KnowledgeTaxonomyReadApplicationService`、`TaxonomyApplicationAssembler` |
| domain repository 方法命名例外 | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/test/java/com/thundax/kuzhambu/knowledge/domain/KnowledgeDomainArchitectureTest.java` | `TagGovernanceMetricsRepository`、`TagRepository` |
| interface request/response 注解、Controller action verb、interface assembler nullness 例外 | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/KnowledgeInterfaceArchitectureTest.java` | taxonomy 与 portal 相关 request/response/controller/assembler |

## Non-goals

- 不处理 Knowledge graph、workbench、refinement、lineage 的 allowlist，除非文件同时属于本切片且必须随 taxonomy/portal 编译通过。
- 不改动 Portal 或 taxonomy 对外 HTTP 协议而不同步调用方。
- 不把 `build/seed-sql/` 作为持久资产处理。
- 不做无关重构、命名美化或 UI 行为改造。

## Plan

### 1. Application taxonomy 分类与别名 command record 化

目标：移除分类与别名 command 的 `COMMAND_QUERY_RECORD` 例外。

文件范围（10 个）：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagAliasCreateCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagAliasRemoveCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagCategoryCreateCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagCategoryStatusCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagCategoryUpdateCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/TaxonomyApplicationService.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/assembler/KnowledgeTaxonomyInterfaceAssembler.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/KnowledgeApplicationCommandQueryRecordAllowances.java`

执行要求：

- 将上述 5 个 Lombok command 类迁移为 Java record。
- 同步更新 service、service impl、interface assembler 和 application 测试中的构造方式与 getter 访问方式。
- 从 `KnowledgeApplicationCommandQueryRecordAllowances.java` 删除对应 key。

### 2. Application taxonomy 标签生命周期与审核 command record 化

目标：移除标签创建、状态、废弃、审核 command 的 `COMMAND_QUERY_RECORD` 例外。

文件范围（12 个）：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagBatchDeprecateCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagBatchReviewCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagCreateCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagDeprecateCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagReviewCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagStatusCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagUpdateCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/TaxonomyApplicationService.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/assembler/KnowledgeTaxonomyInterfaceAssembler.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/KnowledgeApplicationCommandQueryRecordAllowances.java`

执行要求：

- 将上述 7 个 Lombok command 类迁移为 Java record。
- 同步更新 service、service impl、interface assembler 和 application 测试中的构造方式与 getter 访问方式。
- 从 `KnowledgeApplicationCommandQueryRecordAllowances.java` 删除对应 key。

### 3. Application taxonomy 合并与抽取 command record 化

目标：移除标签合并与 AI 抽取 command 的 `COMMAND_QUERY_RECORD` 例外。

文件范围（9 个）：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagBatchMergeCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagCandidateApplyCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagExtractionCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagMergeCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/TaxonomyApplicationService.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/assembler/KnowledgeTaxonomyInterfaceAssembler.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/KnowledgeApplicationCommandQueryRecordAllowances.java`

执行要求：

- 将上述 4 个 Lombok command 类迁移为 Java record。
- 同步更新 service、service impl、interface assembler 和 application 测试中的构造方式与 getter 访问方式。
- 从 `KnowledgeApplicationCommandQueryRecordAllowances.java` 删除对应 key。

### 4. Application taxonomy query record 化

目标：移除 taxonomy query 的 `COMMAND_QUERY_RECORD` 例外。

文件范围（11 个）：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/query/TagBatchMergePreviewQuery.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/query/TagCategoryQuery.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/query/TagGovernanceMetricsQuery.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/query/TagMergePreviewQuery.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/query/TagQuery.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/query/TagReviewQuery.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/TaxonomyApplicationService.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/assembler/KnowledgeTaxonomyInterfaceAssembler.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/KnowledgeApplicationCommandQueryRecordAllowances.java`

执行要求：

- 将上述 6 个 Lombok query 类迁移为 Java record。
- 同步更新 service、service impl、interface assembler 和 application 测试中的构造方式与 getter 访问方式。
- 从 `KnowledgeApplicationCommandQueryRecordAllowances.java` 删除对应 taxonomy query key。

### 5. Application Portal atlas query record 化

目标：移除 Portal atlas query 的 `COMMAND_QUERY_RECORD` 例外。

文件范围（8 个）：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/query/KnowledgePortalAtlasQuery.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationService.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationServiceImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/atlas/controller/KnowledgePortalAtlasController.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/portal/atlas/controller/KnowledgePortalAtlasControllerTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/KnowledgeApplicationCommandQueryRecordAllowances.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/KnowledgeApplicationArchitectureTest.java`

执行要求：

- 将 `KnowledgePortalAtlasQuery` 从 Lombok 类迁移为 Java record。
- 同步更新 Portal atlas application service、controller 和测试中的构造方式与 getter 访问方式。
- 确认 `KnowledgeApplicationCommandQueryRecordAllowances.legacyAllowances()` 不再保留本 RUNBOOK 范围内 key；如无剩余 key，删除该 helper 并更新 `KnowledgeApplicationArchitectureTest.java`。

### 6. Application taxonomy service 边界与 assembler nullness

目标：移除 `KnowledgeApplicationArchitectureTest` 中 taxonomy 相关 application service 方法参数 allowlist 与 `TaxonomyApplicationAssembler` nullness allowlist。

文件范围（8 个）：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/facade/impl/KnowledgeFacadeImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/query/DiscoveryEntityHintQuery.java`（新增）
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/query/DiscoveryTagHintQuery.java`（新增）
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/KnowledgeTaxonomyReadApplicationService.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/KnowledgeTaxonomyReadApplicationServiceImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/assembler/TaxonomyApplicationAssembler.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/KnowledgeApplicationArchitectureTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/assembler/KnowledgeTaxonomyInterfaceAssembler.java`

执行要求：

- 为 `getTagHint(java.lang.String)`、`listEntityHints(java.lang.String)` 引入 `DiscoveryTagHintQuery`、`DiscoveryEntityHintQuery`。
- `TaxonomyApplicationAssembler` 对外 public 方法补齐非空契约，不能以放宽 allowlist 收口。
- 同步更新 interface assembler 到 application service 的调用。
- 从 `KnowledgeApplicationArchitectureTest.java` 删除 taxonomy 对应 key；保留非本切片 workbench/report key。

### 7. Domain taxonomy repository 方法命名

目标：移除 `KnowledgeDomainArchitectureTest` 中 taxonomy repository legacy method name allowlist。

文件范围（10 个）：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/repository/TagGovernanceMetricsRepository.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/repository/TagRepository.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/repository/impl/TagGovernanceMetricsRepositoryImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/repository/impl/TagRepositoryImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/report/service/impl/KnowledgeReportApplicationServiceImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/report/service/impl/KnowledgeReportApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/TagGovernanceMetricsRepositoryImplTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/test/java/com/thundax/kuzhambu/knowledge/domain/KnowledgeDomainArchitectureTest.java`

执行要求：

- 重命名 `TagGovernanceMetricsRepository.getMetrics`、`getTagCoverageRate`、`TagRepository.pagePending` 为符合 repository 命名规则的方法。
- 同步更新 application、infra 实现与相关单测引用。
- 从 `KnowledgeDomainArchitectureTest.java` 删除对应 legacy repository 方法 key；如无剩余 key，改为无 allowlist 调用。

### 8. Interface taxonomy response 注解

目标：移除 interface ArchUnit 中 taxonomy response 注解例外。

文件范围（10 个）：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagAliasResponse.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagBatchMergePreviewResponse.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagCategoryResponse.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagContentRefResponse.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagDetailResponse.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagExtractionResponse.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagGovernanceMetricsResponse.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagMergePreviewResponse.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagResponse.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/KnowledgeInterfaceArchitectureTest.java`

执行要求：

- 为 taxonomy response 类补齐当前 request/response model 注解规则要求的注解。
- 从 `KnowledgeInterfaceArchitectureTest.java` 删除 taxonomy response annotation key。

### 9. Interface taxonomy Controller action verb

目标：移除 interface ArchUnit 中 `KnowledgeTaxonomyController` action verb 例外。

文件范围（4 个）：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/KnowledgeTaxonomyController.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/KnowledgeInterfaceArchitectureTest.java`
- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-service.ts`
- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-service-contract.test.ts`

执行要求：

- 调整 `KnowledgeTaxonomyController` action path 或方法名，使其符合共享 verb whitelist。
- 同步 `taxonomy-service.ts` 与 `taxonomy-service-contract.test.ts`。
- 从 `KnowledgeInterfaceArchitectureTest.java` 删除 `KnowledgeTaxonomyController` action verb key；保留非本切片 `KnowledgeGraphWorkbenchController` key。

### 10. Interface taxonomy assembler nullness

目标：移除 interface ArchUnit 中 `KnowledgeTaxonomyInterfaceAssembler` nullness allowlist。

文件范围（3 个）：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/assembler/KnowledgeTaxonomyInterfaceAssembler.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/KnowledgeInterfaceArchitectureTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/assembler/TaxonomyApplicationAssembler.java`

执行要求：

- `KnowledgeTaxonomyInterfaceAssembler` public 方法补齐非空契约。
- 如非空契约影响 application assembler 调用链，同步 `TaxonomyApplicationAssembler`。
- 从 `KnowledgeInterfaceArchitectureTest.java` 删除 `KnowledgeTaxonomyInterfaceAssembler` nullness key。

### 11. Interface Portal atlas/home/quality request 与 response 注解

目标：移除 Portal atlas/home/quality request 注解例外，并清理 Portal atlas/home/quality response 注解例外。

文件范围（9 个）：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/atlas/controller/request/KnowledgePortalAtlasRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/atlas/controller/response/KnowledgePortalAtlasResponse.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/home/controller/request/KnowledgePortalHomeRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/home/controller/response/KnowledgePortalHomeResponse.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/quality/controller/request/KnowledgePortalQualityRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/quality/controller/response/KnowledgePortalQualityResponse.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/KnowledgeInterfaceArchitectureTest.java`
- `kuzhambu-apps/portal-web/src/pages/knowledge/atlas/atlas-service.ts`
- `kuzhambu-apps/portal-web/src/pages/knowledge/quality/quality-service.ts`

执行要求：

- 为 request/response 与嵌套 response 补齐当前模型注解规则要求的注解。
- 如果 request 字段、默认值或序列化名称变化，必须同步 portal-web service。
- 从 `KnowledgeInterfaceArchitectureTest.java` 删除 atlas/home/quality request 与 response annotation key。

### 12. Interface Portal lineage response 注解与 Portal assembler nullness

目标：处理本切片内 Portal assembler nullness allowlist，并只处理 `KnowledgePortalLineageResponse` 的 response 注解例外；不扩展到 lineage 业务逻辑或其他 lineage allowlist。

文件范围（6 个）：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/atlas/assembler/KnowledgePortalAtlasInterfaceAssembler.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/home/assembler/KnowledgePortalHomeInterfaceAssembler.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/lineage/assembler/KnowledgePortalLineageInterfaceAssembler.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/lineage/controller/response/KnowledgePortalLineageResponse.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/quality/assembler/KnowledgePortalQualityInterfaceAssembler.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/KnowledgeInterfaceArchitectureTest.java`

执行要求：

- Portal atlas/home/lineage/quality assembler public 方法补齐非空契约。
- 为 `KnowledgePortalLineageResponse` 及其嵌套 response 补齐模型注解。
- 从 `KnowledgeInterfaceArchitectureTest.java` 删除 Portal assembler nullness key 与 `KnowledgePortalLineageResponse` response annotation key。
- 不处理 `KnowledgeGraphWorkbenchController` action verb key。

### 13. 收口验证与 RUNBOOK 清理

目标：确认本切片 allowlist 已清零，验证通过后删除临时 RUNBOOK。

文件范围（4 个）：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/KnowledgeApplicationCommandQueryRecordAllowances.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/KnowledgeApplicationArchitectureTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/test/java/com/thundax/kuzhambu/knowledge/domain/KnowledgeDomainArchitectureTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/KnowledgeInterfaceArchitectureTest.java`

执行要求：

- 使用 `rg` 确认本 RUNBOOK 范围内 taxonomy 与 portal legacy key 不再存在。
- 保留明确非本切片的 graph、workbench、report allowlist。
- 完成最终验证后删除本 RUNBOOK。

## Verification

每个 TODO 任务完成后，在 `kuzhambu-servers/` 下运行与任务相关的最窄验证：

```sh
mvn -pl biz/knowledge/kuzhambu-knowledge-application,biz/knowledge/kuzhambu-knowledge-domain,biz/knowledge/kuzhambu-knowledge-interface -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/knowledge/kuzhambu-knowledge-application,biz/knowledge/kuzhambu-knowledge-domain,biz/knowledge/kuzhambu-knowledge-interface -am -DskipTests compile
```

涉及 `kuzhambu-apps/admin-web` 或 `kuzhambu-apps/portal-web` 调用方时，在 `kuzhambu-apps/` 下额外运行：

```sh
pnpm --filter admin-web run format
pnpm --filter portal-web run format
pnpm run format:check
pnpm run lint
pnpm --filter admin-web run build
pnpm --filter portal-web run build
```

每 5 个 TODO 任务后运行一次测试；最终收口至少运行：

```sh
cd kuzhambu-servers
mvn -pl biz/knowledge/kuzhambu-knowledge-application,biz/knowledge/kuzhambu-knowledge-domain,biz/knowledge/kuzhambu-knowledge-interface -am test
mvn spotless:check
mvn checkstyle:check
```

如果本切片修改了前端调用方，最终额外运行：

```sh
cd kuzhambu-apps
pnpm run format:check
pnpm run lint
pnpm run build
pnpm run test
```

## Closure

- 本切片 taxonomy 与 portal allowlist key 清零后删除本文档。
- 删除前确认没有其他文档或 TODO 继续引用本 RUNBOOK。
- 如果执行过程中发现长期规则缺口，先沉淀到 `docs/00-governance/` 对应治理文档，再删除本 RUNBOOK。
