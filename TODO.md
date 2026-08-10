# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 当前任务项按数字编号顺序执行；不得跳过前置契约任务直接做后续调用方或重命名任务。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `09-02 application taxonomy 标签生命周期与审核 command`：标签生命周期与审核 command record 化
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-09-KNOWLEDGE-TAXONOMY-PORTAL.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagBatchDeprecateCommand.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagBatchReviewCommand.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagCreateCommand.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagDeprecateCommand.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagReviewCommand.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagStatusCommand.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagUpdateCommand.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/TaxonomyApplicationService.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/assembler/KnowledgeTaxonomyInterfaceAssembler.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImplTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/KnowledgeApplicationCommandQueryRecordAllowances.java`
    - 处理动作：将标签生命周期与审核 command 迁移为 Java record 并删除对应 ArchUnit allowlist key。
    - 验收点：上述 command 不再出现在 `KnowledgeApplicationCommandQueryRecordAllowances`，相关调用方编译通过。
    - 重要度：9/10

- [ ] `09-03 application taxonomy 合并与抽取 command`：合并与抽取 command record 化
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-09-KNOWLEDGE-TAXONOMY-PORTAL.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagBatchMergeCommand.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagCandidateApplyCommand.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagExtractionCommand.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagMergeCommand.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/TaxonomyApplicationService.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/assembler/KnowledgeTaxonomyInterfaceAssembler.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImplTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/KnowledgeApplicationCommandQueryRecordAllowances.java`
    - 处理动作：将标签合并与 AI 抽取 command 迁移为 Java record 并删除对应 ArchUnit allowlist key。
    - 验收点：上述 command 不再出现在 `KnowledgeApplicationCommandQueryRecordAllowances`，相关调用方编译通过。
    - 重要度：9/10

- [ ] `09-04 application taxonomy query`：taxonomy query record 化
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-09-KNOWLEDGE-TAXONOMY-PORTAL.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/query/TagBatchMergePreviewQuery.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/query/TagCategoryQuery.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/query/TagGovernanceMetricsQuery.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/query/TagMergePreviewQuery.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/query/TagQuery.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/query/TagReviewQuery.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/TaxonomyApplicationService.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/assembler/KnowledgeTaxonomyInterfaceAssembler.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImplTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/KnowledgeApplicationCommandQueryRecordAllowances.java`
    - 处理动作：将 taxonomy query 迁移为 Java record 并删除对应 ArchUnit allowlist key。
    - 验收点：taxonomy query 不再出现在 `KnowledgeApplicationCommandQueryRecordAllowances`，相关调用方编译通过。
    - 重要度：9/10

- [ ] `09-05 application portal atlas query`：Portal atlas query record 化
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-09-KNOWLEDGE-TAXONOMY-PORTAL.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/query/KnowledgePortalAtlasQuery.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationService.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationServiceImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/atlas/controller/KnowledgePortalAtlasController.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/portal/atlas/controller/KnowledgePortalAtlasControllerTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationServiceImplTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/KnowledgeApplicationCommandQueryRecordAllowances.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/KnowledgeApplicationArchitectureTest.java`
    - 处理动作：将 `KnowledgePortalAtlasQuery` 迁移为 Java record 并删除对应 ArchUnit allowlist key。
    - 验收点：Portal atlas query 不再出现在 `KnowledgeApplicationCommandQueryRecordAllowances`，相关调用方编译通过。
    - 重要度：9/10

- [ ] `09-06 application taxonomy service 边界`：taxonomy read service 参数与 assembler nullness 收口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-09-KNOWLEDGE-TAXONOMY-PORTAL.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/facade/impl/KnowledgeFacadeImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/query/DiscoveryEntityHintQuery.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/query/DiscoveryTagHintQuery.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/KnowledgeTaxonomyReadApplicationService.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/KnowledgeTaxonomyReadApplicationServiceImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/assembler/TaxonomyApplicationAssembler.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/KnowledgeApplicationArchitectureTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/assembler/KnowledgeTaxonomyInterfaceAssembler.java`
    - 处理动作：引入 taxonomy hint query 并补齐 taxonomy application assembler 非空契约。
    - 验收点：`KnowledgeApplicationArchitectureTest` 不再包含 taxonomy service 方法参数和 `TaxonomyApplicationAssembler` nullness allowlist。
    - 重要度：9/10

- [ ] `09-07 domain taxonomy repository`：taxonomy repository 方法命名收口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-09-KNOWLEDGE-TAXONOMY-PORTAL.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/repository/TagGovernanceMetricsRepository.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/repository/TagRepository.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/repository/impl/TagGovernanceMetricsRepositoryImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/repository/impl/TagRepositoryImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/report/service/impl/KnowledgeReportApplicationServiceImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImplTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/report/service/impl/KnowledgeReportApplicationServiceImplTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/TagGovernanceMetricsRepositoryImplTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/test/java/com/thundax/kuzhambu/knowledge/domain/KnowledgeDomainArchitectureTest.java`
    - 处理动作：重命名 taxonomy repository legacy 方法并删除对应 ArchUnit allowlist key。
    - 验收点：`KnowledgeDomainArchitectureTest` 不再包含 taxonomy repository 方法命名 allowlist，所有引用编译通过。
    - 重要度：9/10

- [ ] `09-08 interface taxonomy response`：taxonomy response 注解收口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-09-KNOWLEDGE-TAXONOMY-PORTAL.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagAliasResponse.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagBatchMergePreviewResponse.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagCategoryResponse.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagContentRefResponse.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagDetailResponse.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagExtractionResponse.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagGovernanceMetricsResponse.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagMergePreviewResponse.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagResponse.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/KnowledgeInterfaceArchitectureTest.java`
    - 处理动作：为 taxonomy response 补齐模型注解并删除对应 ArchUnit allowlist key。
    - 验收点：`KnowledgeInterfaceArchitectureTest` 不再包含 taxonomy response annotation allowlist。
    - 重要度：8/10

- [ ] `09-09 interface taxonomy controller action verb`：taxonomy Controller action verb 收口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-09-KNOWLEDGE-TAXONOMY-PORTAL.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/KnowledgeTaxonomyController.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/KnowledgeInterfaceArchitectureTest.java`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-service.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-service-contract.test.ts`
    - 处理动作：调整 taxonomy Controller action path 或方法名并同步 admin-web 调用契约。
    - 验收点：`KnowledgeInterfaceArchitectureTest` 不再包含 `KnowledgeTaxonomyController` action verb allowlist，admin-web contract test 期望已同步。
    - 重要度：9/10

- [ ] `09-10 interface taxonomy assembler`：taxonomy interface assembler nullness 收口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-09-KNOWLEDGE-TAXONOMY-PORTAL.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/assembler/KnowledgeTaxonomyInterfaceAssembler.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/KnowledgeInterfaceArchitectureTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/assembler/TaxonomyApplicationAssembler.java`
    - 处理动作：补齐 taxonomy interface assembler 非空契约并删除对应 ArchUnit allowlist key。
    - 验收点：`KnowledgeInterfaceArchitectureTest` 不再包含 `KnowledgeTaxonomyInterfaceAssembler` nullness allowlist。
    - 重要度：8/10

- [ ] `09-11 interface portal atlas home quality models`：Portal atlas/home/quality 模型注解收口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-09-KNOWLEDGE-TAXONOMY-PORTAL.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/atlas/controller/request/KnowledgePortalAtlasRequest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/atlas/controller/response/KnowledgePortalAtlasResponse.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/home/controller/request/KnowledgePortalHomeRequest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/home/controller/response/KnowledgePortalHomeResponse.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/quality/controller/request/KnowledgePortalQualityRequest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/quality/controller/response/KnowledgePortalQualityResponse.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/KnowledgeInterfaceArchitectureTest.java`、`kuzhambu-apps/portal-web/src/pages/knowledge/atlas/atlas-service.ts`、`kuzhambu-apps/portal-web/src/pages/knowledge/quality/quality-service.ts`
    - 处理动作：为 Portal atlas/home/quality request/response 补齐模型注解并同步 portal-web 调用契约。
    - 验收点：`KnowledgeInterfaceArchitectureTest` 不再包含 atlas/home/quality request/response annotation allowlist。
    - 重要度：8/10

- [ ] `09-12 interface portal lineage and assemblers`：Portal lineage response 与 assembler nullness 收口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-09-KNOWLEDGE-TAXONOMY-PORTAL.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/atlas/assembler/KnowledgePortalAtlasInterfaceAssembler.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/home/assembler/KnowledgePortalHomeInterfaceAssembler.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/lineage/assembler/KnowledgePortalLineageInterfaceAssembler.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/lineage/controller/response/KnowledgePortalLineageResponse.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/quality/assembler/KnowledgePortalQualityInterfaceAssembler.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/KnowledgeInterfaceArchitectureTest.java`
    - 处理动作：补齐 Portal assembler 非空契约与 lineage response 模型注解并删除对应 ArchUnit allowlist key。
    - 验收点：`KnowledgeInterfaceArchitectureTest` 不再包含 Portal assembler nullness key 与 `KnowledgePortalLineageResponse` annotation key。
    - 重要度：8/10

- [ ] `09-13 cleanup`：收口验证与现场清理
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-09-KNOWLEDGE-TAXONOMY-PORTAL.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/KnowledgeApplicationCommandQueryRecordAllowances.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/KnowledgeApplicationArchitectureTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/test/java/com/thundax/kuzhambu/knowledge/domain/KnowledgeDomainArchitectureTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/KnowledgeInterfaceArchitectureTest.java`、`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-09-KNOWLEDGE-TAXONOMY-PORTAL.md`、`TODO.md`
    - 处理动作：确认本切片 allowlist 清零后删除 RUNBOOK 并清空对应 TODO。
    - 验收点：本 RUNBOOK 被删除，`TODO.md` 不再保留本切片任务，工作区无无关修改。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
