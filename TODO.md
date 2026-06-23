# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

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
