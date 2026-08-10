# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 当前任务项按数字编号顺序执行；不得跳过前置契约任务直接做后续调用方或重命名任务。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `08-14 refinement entity-relation draft repository 命名`：重命名精修实体与关系 draft repository 方法
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-08-KNOWLEDGE-GRAPH-REFINEMENT.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/repository/RefinementEntityDraftRepository.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/RefinementEntityDraftRepositoryImpl.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/RefinementEntityDraftRepositoryTest.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/repository/RefinementRelationDraftRepository.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/RefinementRelationDraftRepositoryImpl.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/RefinementRelationDraftRepositoryTest.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/test/java/com/thundax/kuzhambu/knowledge/domain/KnowledgeDomainArchitectureTest.java`
    - 处理动作：将实体 draft 和关系 draft `saveOrUpdateBatch` 改为仓储动词白名单允许的名称，并删除对应 domain allowance。
    - 验收点：接口、实现、测试和调用方名称一致，domain allowlist 中对应 repository method key 清零。
    - 重要度：9/10

- [ ] `08-15 refinement lineage draft repository 命名`：重命名精修世系 draft repository 方法
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-08-KNOWLEDGE-GRAPH-REFINEMENT.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/repository/RefinementLineageNodeDraftRepository.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/RefinementLineageNodeDraftRepositoryImpl.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/RefinementLineageNodeDraftRepositoryTest.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/repository/RefinementLineageRelationDraftRepository.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/RefinementLineageRelationDraftRepositoryImpl.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/RefinementLineageRelationDraftRepositoryTest.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/test/java/com/thundax/kuzhambu/knowledge/domain/KnowledgeDomainArchitectureTest.java`
    - 处理动作：将世系节点 draft 和世系关系 draft `saveOrUpdateBatch` 改为仓储动词白名单允许的名称，并删除对应 domain allowance。
    - 验收点：接口、实现、测试和调用方名称一致，domain allowlist 中对应 repository method key 清零。
    - 重要度：9/10

- [ ] `08-16 graph interface 协议与路径`：清理图谱接口协议模型、Controller 动词和 assembler nullness allowance
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-08-KNOWLEDGE-GRAPH-REFINEMENT.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/controller/request/GraphExtractionRequests.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/controller/response/GraphExtractionResponses.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/controller/KnowledgeGraphExtractionController.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/assembler/KnowledgeGraphExtractionInterfaceAssembler.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/controller/KnowledgeGraphExtractionControllerTest.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/KnowledgeInterfaceArchitectureTest.java`
    - 处理动作：补齐 graph Request/Response 必要注解，迁移 Controller 方法名和 action path，并移除 graph assembler nullness allowance。
    - 验收点：graph 接口层不再命中本轮 model annotation、controller verb/path 或 assembler nullness key，HTTP 语义保持不变。
    - 重要度：10/10

- [ ] `08-17 refinement interface 协议与路径`：清理精修和质量报告接口协议模型、Controller 动词和 assembler nullness allowance
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-08-KNOWLEDGE-GRAPH-REFINEMENT.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/request/RefinementRequests.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/response/RefinementResponses.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/KnowledgeGraphRefinementController.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/assembler/KnowledgeGraphRefinementInterfaceAssembler.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/KnowledgeGraphRefinementControllerTest.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/request/QualityReportRequests.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/response/QualityReportResponses.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/KnowledgeQualityReportController.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/assembler/KnowledgeQualityReportInterfaceAssembler.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/KnowledgeQualityReportControllerTest.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/KnowledgeInterfaceArchitectureTest.java`
    - 处理动作：补齐 refinement 与 quality report Request/Response 必要注解，迁移 Controller 方法名和 action path，并移除对应 assembler nullness allowance。
    - 验收点：精修与质量报告接口层不再命中本轮 model annotation、controller verb/path 或 assembler nullness key，HTTP 语义保持不变。
    - 重要度：10/10

- [ ] `08-18 lineage interface 协议与路径`：清理世系接口协议模型、Controller 动词和 assembler nullness allowance
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-08-KNOWLEDGE-GRAPH-REFINEMENT.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/lineage/controller/request/LineageCanvasRequest.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/lineage/controller/response/LineageCanvasResponse.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/lineage/controller/KnowledgeLineageController.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/lineage/assembler/KnowledgeLineageInterfaceAssembler.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/admin/lineage/controller/KnowledgeLineageControllerTest.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/KnowledgeInterfaceArchitectureTest.java`
    - 处理动作：补齐 lineage Request/Response 必要注解，迁移 Controller 方法名和 action path，并移除 lineage assembler nullness allowance。
    - 验收点：世系接口层不再命中本轮 model annotation、controller verb/path 或 assembler nullness key，HTTP 语义保持不变。
    - 重要度：9/10

- [ ] `08-19 graph frontend API 调用同步`：同步图谱抽取和图谱结果前端 API 路径
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-08-KNOWLEDGE-GRAPH-REFINEMENT.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-service.ts`；`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-service-contract.test.ts`；`kuzhambu-apps/admin-web/src/pages/knowledge/graph-result/graph-result-service.ts`；`kuzhambu-apps/admin-web/src/pages/knowledge/graph-result/graph-result-service-contract.test.ts`；`kuzhambu-apps/admin-web/e2e/knowledge/graph-extraction/graph-extraction.spec.ts`；`kuzhambu-apps/admin-web/e2e/knowledge/graph-result/graph-result.spec.ts`
    - 处理动作：将 graph Controller action path 变更同步到 admin-web service、契约测试和 e2e 入口。
    - 验收点：前端请求路径与后端 graph Controller 保持一致，graph-extraction 与 graph-result 契约测试覆盖更新后的路径。
    - 重要度：8/10

- [ ] `08-20 refinement-lineage frontend API 调用同步`：同步精修、质量报告和世系前端 API 路径
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-08-KNOWLEDGE-GRAPH-REFINEMENT.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-service.ts`；`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-service-contract.test.ts`；`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-service.ts`；`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-service-contract.test.ts`；`kuzhambu-apps/admin-web/src/pages/knowledge/lineage/lineage-service.ts`；`kuzhambu-apps/admin-web/src/pages/knowledge/lineage/lineage-service-contract.test.ts`；`kuzhambu-apps/admin-web/e2e/knowledge/refinement/refinement.spec.ts`；`kuzhambu-apps/admin-web/e2e/knowledge/quality-report/quality-report.spec.ts`；`kuzhambu-apps/admin-web/e2e/knowledge/lineage/lineage.spec.ts`
    - 处理动作：将 refinement、quality report 与 lineage Controller action path 变更同步到 admin-web service、契约测试和 e2e 入口。
    - 验收点：前端请求路径与后端 Controller 保持一致，三个 service contract 测试覆盖更新后的路径。
    - 重要度：8/10

- [ ] `08-21 validation`：执行本轮 Knowledge allowlist 清理验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-08-KNOWLEDGE-GRAPH-REFINEMENT.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/KnowledgeApplicationCommandQueryRecordAllowances.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/KnowledgeApplicationArchitectureTest.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/test/java/com/thundax/kuzhambu/knowledge/domain/KnowledgeDomainArchitectureTest.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/KnowledgeInterfaceArchitectureTest.java`；`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-service-contract.test.ts`；`kuzhambu-apps/admin-web/src/pages/knowledge/graph-result/graph-result-service-contract.test.ts`；`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-service-contract.test.ts`；`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-service-contract.test.ts`；`kuzhambu-apps/admin-web/src/pages/knowledge/lineage/lineage-service-contract.test.ts`
    - 处理动作：先对实际修改的 Java 与前端文件执行最窄格式化，再运行 Knowledge Maven 测试、Spotless、Checkstyle 和受影响前端契约测试。
    - 验收点：本 RUNBOOK 范围 key 全部清零，保留 key 未变化，相关 Maven 与前端契约验证通过。
    - 重要度：10/10

- [ ] `08-22 cleanup`：清理本轮临时 RUNBOOK 与 TODO 现场
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`；`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-08-KNOWLEDGE-GRAPH-REFINEMENT.md`
    - 范围对象：`TODO.md`；`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-08-KNOWLEDGE-GRAPH-REFINEMENT.md`
    - 处理动作：在代码、测试和 allowlist 清理全部完成后删除本 RUNBOOK，并从 `TODO.md` 删除已完成任务项。
    - 验收点：工作区不再保留已完成 TODO 或临时 RUNBOOK，剩余改动只包含本轮交付所需文件。
    - 重要度：10/10

## 待讨论项
