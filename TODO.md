# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `RefinementLineageRelationDraftPersistenceAssembler.java,RefinementLineageRelationDraftRepositoryImpl.java,RefinementLineageRelationDraftRepositoryTest.java`：完成世系关系草稿仓储实现
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/assembler/RefinementLineageRelationDraftPersistenceAssembler.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/RefinementLineageRelationDraftRepositoryImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/RefinementLineageRelationDraftRepositoryTest.java`
    - 处理动作：新增世系关系草稿专属装配器并完成世系关系草稿仓储实现与测试
    - 验收点：世系关系草稿仓储可稳定读取、保存和删除，查询条件具备自动化验证
    - 重要度：8/10

- [ ] `KnowledgeEntityRepository.java,KnowledgeRelationRepository.java,KnowledgeEntityRepositoryImpl.java,KnowledgeRelationRepositoryImpl.java`：扩展正式实体与关系仓储读删能力
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/repository/KnowledgeEntityRepository.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/repository/KnowledgeRelationRepository.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/repository/impl/KnowledgeEntityRepositoryImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/repository/impl/KnowledgeRelationRepositoryImpl.java`
    - 处理动作：为正式实体和关系仓储补 `listByVersionId` 与按业务键删除能力
    - 验收点：精修初始化和应用回写可读取并裁剪正式实体与关系事实
    - 重要度：9/10

- [ ] `KnowledgeLineageNodeRepository.java,KnowledgeLineageRelationRepository.java,KnowledgeLineageNodeRepositoryImpl.java,KnowledgeLineageRelationRepositoryImpl.java`：扩展正式世系仓储读删能力
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/repository/KnowledgeLineageNodeRepository.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/repository/KnowledgeLineageRelationRepository.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/repository/impl/KnowledgeLineageNodeRepositoryImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/repository/impl/KnowledgeLineageRelationRepositoryImpl.java`
    - 处理动作：为正式世系节点和世系关系仓储补 `listByVersionId` 与按业务键删除能力
    - 验收点：精修初始化和应用回写可读取并裁剪正式世系事实
    - 重要度：8/10

- [ ] `KnowledgeGraphRefinementApplicationService.java,RefinementWorkbenchPageQuery.java,RefinementDetailQuery.java,RefinementDetailResult.java`：定义精修应用服务读契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/KnowledgeGraphRefinementApplicationService.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/query/RefinementWorkbenchPageQuery.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/query/RefinementDetailQuery.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/result/RefinementDetailResult.java`
    - 处理动作：定义精修列表、详情和任务读取的最终版应用服务契约
    - 验收点：应用服务接口可稳定表达精修任务列表与详情结构
    - 重要度：10/10

- [ ] `UpsertRefinementEntityCommand.java,ConfirmRefinementEntityCommand.java,DeleteRefinementEntityCommand.java,UpsertRefinementRelationCommand.java,ConfirmRefinementRelationCommand.java`：定义实体与关系写入命令模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/command/UpsertRefinementEntityCommand.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/command/ConfirmRefinementEntityCommand.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/command/DeleteRefinementEntityCommand.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/command/UpsertRefinementRelationCommand.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/command/ConfirmRefinementRelationCommand.java`
    - 处理动作：定义实体与关系新增、更新、确认命令模型
    - 验收点：实体与关系写入口具备稳定命令契约
    - 重要度：9/10

- [ ] `DeleteRefinementRelationCommand.java,UpsertRefinementLineageNodeCommand.java,UpsertRefinementLineageRelationCommand.java,UpsertQualityAnnotationCommand.java,QualitySummaryResult.java`：定义删除、世系与质量结果模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/command/DeleteRefinementRelationCommand.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/command/UpsertRefinementLineageNodeCommand.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/command/UpsertRefinementLineageRelationCommand.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/command/UpsertQualityAnnotationCommand.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/result/QualitySummaryResult.java`
    - 处理动作：定义删除类命令、世系写命令和质量汇总结果模型
    - 验收点：世系、删除和质量汇总的应用层契约齐备
    - 重要度：8/10

- [ ] `ConfirmRefinementLineageNodeCommand.java,ConfirmRefinementLineageRelationCommand.java,DeleteRefinementLineageNodeCommand.java,DeleteRefinementLineageRelationCommand.java,DeleteQualityAnnotationCommand.java`：定义后续开放能力命令模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/command/ConfirmRefinementLineageNodeCommand.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/command/ConfirmRefinementLineageRelationCommand.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/command/DeleteRefinementLineageNodeCommand.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/command/DeleteRefinementLineageRelationCommand.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/command/DeleteQualityAnnotationCommand.java`
    - 处理动作：定义世系确认删除和标注删除命令模型
    - 验收点：后续开放能力的命令模型补齐
    - 重要度：7/10

- [ ] `QualityAnnotationPageQuery.java,QualityAnnotationResult.java,RefinementWorkbenchItemResult.java,RefinementProgressSummaryResult.java,RefinementEntityOptionResult.java`：定义列表与标注结果模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/query/QualityAnnotationPageQuery.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/result/QualityAnnotationResult.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/result/RefinementWorkbenchItemResult.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/result/RefinementProgressSummaryResult.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/result/RefinementEntityOptionResult.java`
    - 处理动作：定义任务列表、进度摘要、实体选项和标注分页查询模型
    - 验收点：应用层读取结果模型覆盖任务列表、进度、选项和标注分页
    - 重要度：8/10

- [ ] `RefinementEntityResult.java,RefinementRelationResult.java,RefinementLineageNodeResult.java,RefinementLineageRelationResult.java`：定义草稿结果模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/result/RefinementEntityResult.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/result/RefinementRelationResult.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/result/RefinementLineageNodeResult.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/result/RefinementLineageRelationResult.java`
    - 处理动作：定义实体、关系、世系节点和世系关系草稿结果模型
    - 验收点：草稿详情结果模型覆盖四类对象
    - 重要度：8/10

- [ ] `RefinementDraftBootstrapSupport.java,KnowledgeGraphRefinementApplicationServiceImpl.java,KnowledgeGraphRefinementTaskOpenTest.java`：实现任务打开与草稿初始化
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/support/RefinementDraftBootstrapSupport.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/impl/KnowledgeGraphRefinementApplicationServiceImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/KnowledgeGraphRefinementTaskOpenTest.java`
    - 处理动作：实现打开精修任务并从正式事实初始化草稿
    - 验收点：`task/open` 可创建或复用 `DRAFT` 任务并生成实体、关系和世系草稿
    - 重要度：10/10

- [ ] `KnowledgeRefinementManualKeySupport.java,KnowledgeGraphRefinementApplicationServiceImpl.java,KnowledgeGraphRefinementEntityWriteTest.java,KnowledgeGraphRefinementRelationWriteTest.java`：实现实体与关系草稿写入
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/support/KnowledgeRefinementManualKeySupport.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/impl/KnowledgeGraphRefinementApplicationServiceImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/KnowledgeGraphRefinementEntityWriteTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/KnowledgeGraphRefinementRelationWriteTest.java`
    - 处理动作：实现实体与关系草稿新增、更新、确认和删除逻辑
    - 验收点：实体与关系操作只改草稿且手工新增键策略生效
    - 重要度：10/10

- [ ] `KnowledgeGraphRefinementApplicationServiceImpl.java,KnowledgeGraphRefinementLineageWriteTest.java,QualityAnnotationWriteTest.java`：实现世系草稿与质量标注写入
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/impl/KnowledgeGraphRefinementApplicationServiceImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/KnowledgeGraphRefinementLineageWriteTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/QualityAnnotationWriteTest.java`
    - 处理动作：实现世系草稿和质量标注的后端写入能力
    - 验收点：世系与标注接口在后端走通且具备最小测试
    - 重要度：7/10

- [ ] `RefinementApplySupport.java,KnowledgeGraphRefinementApplicationServiceImpl.java,KnowledgeGraphRefinementApplyTest.java`：实现草稿应用回正式事实
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/support/RefinementApplySupport.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/impl/KnowledgeGraphRefinementApplicationServiceImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/KnowledgeGraphRefinementApplyTest.java`
    - 处理动作：实现 `task/apply` 将草稿覆盖回正式实体、关系和世系事实
    - 验收点：精修应用后正式事实、任务状态和删除裁剪口径符合 RUNBOOK
    - 重要度：10/10

- [ ] `QualitySummaryAggregationSupport.java,KnowledgeGraphRefinementApplicationServiceImpl.java,QualitySummaryAggregationSupportTest.java`：实现运行时质量指标聚合
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/support/QualitySummaryAggregationSupport.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/impl/KnowledgeGraphRefinementApplicationServiceImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/QualitySummaryAggregationSupportTest.java`
    - 处理动作：实现按任务运行时聚合 `entityCoverageRate`、`relationAccuracyRate`、`completenessRate`
    - 验收点：`quality/summary` 返回三项质量指标并具备自动化验证
    - 重要度：9/10

- [ ] `RefinementEntityDraftAuditSnapshotAssembler.java,RefinementRelationDraftAuditSnapshotAssembler.java,RefinementEntityDraftAuditObjectLoader.java,RefinementRelationDraftAuditObjectLoader.java,RefinementAuditSnapshotAssemblerTest.java`：接入实体与关系审计快照
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/audit/RefinementEntityDraftAuditSnapshotAssembler.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/audit/RefinementRelationDraftAuditSnapshotAssembler.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/audit/RefinementEntityDraftAuditObjectLoader.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/audit/RefinementRelationDraftAuditObjectLoader.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/RefinementAuditSnapshotAssemblerTest.java`
    - 处理动作：为实体与关系草稿接入字段级 before/after 审计快照与对象加载器
    - 验收点：System 审计可识别实体与关系草稿字段差异
    - 重要度：9/10

- [ ] `RefinementLineageNodeDraftAuditSnapshotAssembler.java,RefinementLineageRelationDraftAuditSnapshotAssembler.java,RefinementLineageNodeDraftAuditObjectLoader.java,RefinementLineageRelationDraftAuditObjectLoader.java,RefinementLineageAuditSnapshotAssemblerTest.java`：接入世系审计快照
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/audit/RefinementLineageNodeDraftAuditSnapshotAssembler.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/audit/RefinementLineageRelationDraftAuditSnapshotAssembler.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/audit/RefinementLineageNodeDraftAuditObjectLoader.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/audit/RefinementLineageRelationDraftAuditObjectLoader.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/RefinementLineageAuditSnapshotAssemblerTest.java`
    - 处理动作：为世系节点与世系关系草稿接入字段级审计快照与对象加载器
    - 验收点：System 审计可识别世系草稿字段差异
    - 重要度：7/10

- [ ] `QualityAnnotationAuditSnapshotAssembler.java,QualityAnnotationAuditObjectLoader.java,KnowledgeGraphRefinementApplicationServiceImpl.java,QualityAnnotationAuditTest.java`：接入质量标注审计
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/audit/QualityAnnotationAuditSnapshotAssembler.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/audit/QualityAnnotationAuditObjectLoader.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/impl/KnowledgeGraphRefinementApplicationServiceImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/QualityAnnotationAuditTest.java`
    - 处理动作：为质量标注接入字段级审计和服务侧审计记录入口
    - 验收点：质量标注创建、更新、删除可被 System 审计追溯
    - 重要度：7/10

- [ ] `RefinementRequests.java,RefinementResponses.java,KnowledgeGraphRefinementInterfaceAssembler.java,KnowledgeGraphRefinementController.java,KnowledgeGraphRefinementControllerTest.java`：实现精修接口协议层
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/request/RefinementRequests.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/response/RefinementResponses.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/assembler/KnowledgeGraphRefinementInterfaceAssembler.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/KnowledgeGraphRefinementController.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/KnowledgeGraphRefinementControllerTest.java`
    - 处理动作：实现最终版 refinement HTTP 请求、响应、协议装配、Controller 和控制器测试
    - 验收点：`/api/knowledge/refinement` 的 Phase 1 接口和保留接口定义完整且控制器测试通过
    - 重要度：10/10

- [ ] `refinement-types.ts,refinement-service.ts,refinement-service.test.ts`：实现前端契约层
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-types.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-service.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-service.test.ts`
    - 处理动作：定义前端 refinement 类型、服务和服务契约测试
    - 验收点：前端契约层覆盖 `GRAPH / RELATION / LINEAGE / ANNOTATION / QUALITY`
    - 重要度：9/10

- [ ] `refinement-filter-form.tsx,refinement-workbench-table.tsx,refinement-progress-summary.tsx`：实现精修列表区组件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-filter-form.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-workbench-table.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-progress-summary.tsx`
    - 处理动作：实现待精修列表筛选、任务表格和进度统计组件
    - 验收点：页面可按门类和来源筛选待精修任务并展示进度
    - 重要度：9/10

- [ ] `refinement-entity-table.tsx,refinement-entity-editor.tsx,refinement-entity-delete-modal.tsx`：实现实体精修区组件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-entity-table.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-entity-editor.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-entity-delete-modal.tsx`
    - 处理动作：实现实体表格、编辑弹窗和删除确认弹窗
    - 验收点：用户可新增、编辑、确认和删除实体草稿
    - 重要度：10/10

- [ ] `refinement-relation-table.tsx,refinement-relation-editor.tsx,refinement-relation-delete-modal.tsx`：实现关系精修区组件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-relation-table.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-relation-editor.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-relation-delete-modal.tsx`
    - 处理动作：实现关系表格、编辑弹窗和删除确认弹窗
    - 验收点：用户可新增、编辑、确认和删除关系草稿
    - 重要度：10/10

- [ ] `refinement-page.tsx,refinement-page.css,refinement-page.test.tsx,router/index.tsx`：实现精修页面壳与路由入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-page.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-page.css`、`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-page.test.tsx`、`kuzhambu-apps/admin-web/src/router/index.tsx`
    - 处理动作：编排精修工作台页面并接入 `/knowledge/refinement` 路由
    - 验收点：页面完成“筛选任务 -> 打开任务 -> 编辑实体关系 -> 应用任务”的主交互
    - 重要度：10/10

- [ ] `system.json,system.sql`：新增精修菜单与权限种子
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`db/data-source/system.json`、`db/data/system.sql`
    - 处理动作：新增 `知识图谱精修` 菜单和 `knowledge:refinement:view`、`knowledge:refinement:edit` 权限种子
    - 验收点：菜单生成结果包含精修入口且权限编码与前后端一致
    - 重要度：8/10

- [ ] `KnowledgeGraphRefinementTaskOpenTest.java,KnowledgeGraphRefinementApplyTest.java,KnowledgeGraphRefinementControllerTest.java,RefinementTaskRepositoryTest.java`：补齐后端主链路最小验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/KnowledgeGraphRefinementTaskOpenTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/KnowledgeGraphRefinementApplyTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/KnowledgeGraphRefinementControllerTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/RefinementTaskRepositoryTest.java`
    - 处理动作：补齐精修任务打开、应用、接口和仓储的最小验证集合
    - 验收点：后端 refinement 主链路具备可运行自动化验证
    - 重要度：9/10

- [ ] `refinement-service.test.ts,refinement-page.test.tsx`：补齐前端主链路最小验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-service.test.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-page.test.tsx`
    - 处理动作：补齐 refinement 服务契约和页面主交互测试
    - 验收点：前端 refinement 服务和页面主交互具备自动化测试
    - 重要度：8/10

## 待讨论项
