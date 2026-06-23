# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `db/schema/knowledge.sql,GraphVersionDO,GraphVersionPersistenceAssembler,GraphVersionRepositoryTest`：扩展图谱版本门类冗余字段
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`db/schema/knowledge.sql`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/.../GraphVersionDO.java`、`.../GraphVersionPersistenceAssembler.java`、`.../GraphVersionRepositoryTest.java`
    - 处理动作：为 `knowledge_graph_version` 增加 `source_category_code` 和 `source_category_name` 并同步持久化映射与测试
    - 验收点：`knowledge_graph_version` 门类冗余字段完成落库、映射和仓储测试覆盖
    - 重要度：10/10

- [ ] `db/schema/knowledge.sql,RefinementTaskDO,RefinementTaskMapper,RefinementTaskRepositoryTest`：新增精修任务表
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`db/schema/knowledge.sql`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/.../RefinementTaskDO.java`、`.../RefinementTaskMapper.java`、`.../RefinementTaskRepositoryTest.java`
    - 处理动作：新增 `knowledge_refinement_task` 表及其持久化对象、Mapper 和仓储测试
    - 验收点：精修任务表结构、唯一键和索引按 RUNBOOK 落地且仓储测试可运行
    - 重要度：10/10

- [ ] `db/schema/knowledge.sql,RefinementEntityDraftDO,RefinementRelationDraftDO,RefinementEntityDraftRepositoryTest,RefinementRelationDraftRepositoryTest`：新增实体与关系草稿表
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`db/schema/knowledge.sql`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/.../RefinementEntityDraftDO.java`、`.../RefinementRelationDraftDO.java`、`.../RefinementEntityDraftRepositoryTest.java`、`.../RefinementRelationDraftRepositoryTest.java`
    - 处理动作：新增 `knowledge_refinement_entity_draft` 和 `knowledge_refinement_relation_draft` 表及其持久化对象与测试
    - 验收点：实体与关系草稿表按最终版结构落地并具备基础仓储测试
    - 重要度：10/10

- [ ] `db/schema/knowledge.sql,RefinementLineageNodeDraftDO,RefinementLineageRelationDraftDO,RefinementLineageNodeDraftRepositoryTest,RefinementLineageRelationDraftRepositoryTest`：新增世系草稿表
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`db/schema/knowledge.sql`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/.../RefinementLineageNodeDraftDO.java`、`.../RefinementLineageRelationDraftDO.java`、`.../RefinementLineageNodeDraftRepositoryTest.java`、`.../RefinementLineageRelationDraftRepositoryTest.java`
    - 处理动作：新增 `knowledge_refinement_lineage_node_draft` 和 `knowledge_refinement_lineage_relation_draft` 表及其持久化对象与测试
    - 验收点：世系草稿表按最终版结构落地并具备基础仓储测试
    - 重要度：8/10

- [ ] `db/schema/knowledge.sql,QualityAnnotationDO,QualityAnnotationMapper,QualityAnnotationRepositoryTest`：新增质量标注表
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`db/schema/knowledge.sql`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/.../QualityAnnotationDO.java`、`.../QualityAnnotationMapper.java`、`.../QualityAnnotationRepositoryTest.java`
    - 处理动作：新增 `knowledge_quality_annotation` 表及其持久化对象、Mapper 和仓储测试
    - 验收点：质量标注表按最终版结构落地并具备基础仓储测试
    - 重要度：8/10

- [ ] `RefinementTask.java,RefinementTaskId.java,RefinementTaskRepository.java,RefinementTaskRepositoryImpl.java`：新增精修任务领域模型与仓储端口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/.../RefinementTask.java`、`.../RefinementTaskId.java`、`.../RefinementTaskRepository.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/.../RefinementTaskRepositoryImpl.java`
    - 处理动作：新增精修任务领域实体、强类型 ID、仓储端口与实现
    - 验收点：应用层可通过精修任务仓储读取和写入 `knowledge_refinement_task`
    - 重要度：10/10

- [ ] `RefinementEntityDraft.java,RefinementRelationDraft.java,RefinementEntityDraftRepository.java,RefinementRelationDraftRepository.java,RefinementEntityDraftRepositoryImpl.java`：新增实体与关系草稿领域模型与仓储端口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/.../RefinementEntityDraft.java`、`.../RefinementRelationDraft.java`、`.../RefinementEntityDraftRepository.java`、`.../RefinementRelationDraftRepository.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/.../RefinementEntityDraftRepositoryImpl.java`
    - 处理动作：新增实体与关系草稿领域对象、仓储端口和首个仓储实现
    - 验收点：应用层可通过领域仓储读写实体与关系草稿
    - 重要度：10/10

- [ ] `RefinementRelationDraftRepositoryImpl.java,RefinementLineageNodeDraft.java,RefinementLineageRelationDraft.java,RefinementLineageNodeDraftRepository.java,RefinementLineageRelationDraftRepository.java`：补齐关系与世系草稿仓储结构
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/.../RefinementRelationDraftRepositoryImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/.../RefinementLineageNodeDraft.java`、`.../RefinementLineageRelationDraft.java`、`.../RefinementLineageNodeDraftRepository.java`、`.../RefinementLineageRelationDraftRepository.java`
    - 处理动作：补齐关系仓储实现和世系草稿领域对象、仓储端口
    - 验收点：关系与世系草稿结构在 domain 和 infra 层闭合
    - 重要度：8/10

- [ ] `RefinementLineageNodeDraftRepositoryImpl.java,RefinementLineageRelationDraftRepositoryImpl.java,QualityAnnotation.java,QualityAnnotationRepository.java,QualityAnnotationRepositoryImpl.java`：补齐世系与质量标注仓储实现
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/.../RefinementLineageNodeDraftRepositoryImpl.java`、`.../RefinementLineageRelationDraftRepositoryImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/.../QualityAnnotation.java`、`.../QualityAnnotationRepository.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/.../QualityAnnotationRepositoryImpl.java`
    - 处理动作：完成世系草稿仓储实现和质量标注领域仓储闭合
    - 验收点：世系草稿与质量标注在 domain 和 infra 层形成完整仓储链路
    - 重要度：8/10

- [ ] `KnowledgeGraphRefinementApplicationService.java,RefinementWorkbenchPageQuery.java,RefinementDetailQuery.java,RefinementDetailResult.java`：新增最终版应用服务读契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/.../KnowledgeGraphRefinementApplicationService.java`、`.../RefinementWorkbenchPageQuery.java`、`.../RefinementDetailQuery.java`、`.../RefinementDetailResult.java`
    - 处理动作：定义精修工作台分页、详情和最终版服务接口读契约
    - 验收点：应用服务接口可稳定表达精修列表、详情和质量汇总读取
    - 重要度：10/10

- [ ] `UpsertRefinementEntityCommand.java,ConfirmRefinementEntityCommand.java,UpsertRefinementRelationCommand.java,ConfirmRefinementRelationCommand.java,DeleteRefinementRelationCommand.java`：新增实体与关系命令模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/.../UpsertRefinementEntityCommand.java`、`.../ConfirmRefinementEntityCommand.java`、`.../UpsertRefinementRelationCommand.java`、`.../ConfirmRefinementRelationCommand.java`、`.../DeleteRefinementRelationCommand.java`
    - 处理动作：新增实体与关系草稿新增、更新、确认和删除命令模型
    - 验收点：实体与关系写入口具备稳定命令契约
    - 重要度：9/10

- [ ] `DeleteRefinementEntityCommand.java,UpsertRefinementLineageNodeCommand.java,UpsertRefinementLineageRelationCommand.java,UpsertQualityAnnotationCommand.java,QualitySummaryResult.java`：补齐删除、世系与标注命令模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/.../DeleteRefinementEntityCommand.java`、`.../UpsertRefinementLineageNodeCommand.java`、`.../UpsertRefinementLineageRelationCommand.java`、`.../UpsertQualityAnnotationCommand.java`、`.../QualitySummaryResult.java`
    - 处理动作：补齐实体删除、世系草稿、质量标注和质量汇总契约模型
    - 验收点：最终版应用层命令和结果模型完整可用
    - 重要度：8/10

- [ ] `KnowledgeGraphRefinementApplicationServiceImpl.java,RefinementDraftBootstrapSupport.java,KnowledgeGraphRefinementTaskOpenTest.java`：实现精修任务打开与草稿初始化
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/.../KnowledgeGraphRefinementApplicationServiceImpl.java`、`.../RefinementDraftBootstrapSupport.java`、`.../KnowledgeGraphRefinementTaskOpenTest.java`
    - 处理动作：实现打开精修任务并从正式事实初始化草稿
    - 验收点：`task/open` 可创建或复用 `DRAFT` 任务并生成草稿数据
    - 重要度：10/10

- [ ] `KnowledgeGraphRefinementApplicationServiceImpl.java,KnowledgeRefinementManualKeySupport.java,KnowledgeGraphRefinementEntityWriteTest.java,KnowledgeGraphRefinementRelationWriteTest.java`：实现实体与关系草稿写入
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/.../KnowledgeGraphRefinementApplicationServiceImpl.java`、`.../KnowledgeRefinementManualKeySupport.java`、`.../KnowledgeGraphRefinementEntityWriteTest.java`、`.../KnowledgeGraphRefinementRelationWriteTest.java`
    - 处理动作：实现实体与关系草稿的新增、更新、确认和删除写入逻辑
    - 验收点：实体与关系操作只改草稿且手工新增键策略生效
    - 重要度：10/10

- [ ] `KnowledgeGraphRefinementApplicationServiceImpl.java,KnowledgeGraphRefinementLineageWriteTest.java,QualityAnnotationWriteTest.java`：实现世系草稿与质量标注后端写入
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/.../KnowledgeGraphRefinementApplicationServiceImpl.java`、`.../KnowledgeGraphRefinementLineageWriteTest.java`、`.../QualityAnnotationWriteTest.java`
    - 处理动作：实现世系草稿和质量标注的后端写入能力
    - 验收点：最终版世系和标注接口可在后端走通且有测试
    - 重要度：7/10

- [ ] `RefinementApplySupport.java,KnowledgeGraphRefinementApplicationServiceImpl.java,KnowledgeGraphRefinementApplyTest.java`：实现草稿应用到正式事实
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/.../RefinementApplySupport.java`、`.../KnowledgeGraphRefinementApplicationServiceImpl.java`、`.../KnowledgeGraphRefinementApplyTest.java`
    - 处理动作：实现 `task/apply` 将实体、关系和世系草稿覆盖回正式事实
    - 验收点：精修任务应用后正式事实、任务状态和来源引用符合 RUNBOOK 口径
    - 重要度：10/10

- [ ] `QualitySummaryAggregationSupport.java,KnowledgeGraphRefinementApplicationServiceImpl.java,QualitySummaryAggregationSupportTest.java`：实现运行时质量指标聚合
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/.../QualitySummaryAggregationSupport.java`、`.../KnowledgeGraphRefinementApplicationServiceImpl.java`、`.../QualitySummaryAggregationSupportTest.java`
    - 处理动作：实现按全量、门类和单内容运行时聚合 `entityCoverageRate`、`relationAccuracyRate`、`completenessRate`
    - 验收点：`quality/summary` 可返回三项质量指标且具备自动化测试
    - 重要度：9/10

- [ ] `RefinementEntityDraftAuditSnapshotAssembler.java,RefinementRelationDraftAuditSnapshotAssembler.java,QualityAnnotationAuditSnapshotAssembler.java,RefinementAuditSnapshotAssemblerTest.java`：实现实体、关系和标注审计快照
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/.../RefinementEntityDraftAuditSnapshotAssembler.java`、`.../RefinementRelationDraftAuditSnapshotAssembler.java`、`.../QualityAnnotationAuditSnapshotAssembler.java`、`.../RefinementAuditSnapshotAssemblerTest.java`
    - 处理动作：为实体草稿、关系草稿和质量标注接入字段级 before/after 审计快照
    - 验收点：System 审计可识别精修对象字段差异且测试通过
    - 重要度：9/10

- [ ] `RefinementLineageNodeDraftAuditSnapshotAssembler.java,RefinementLineageRelationDraftAuditSnapshotAssembler.java,AuditSnapshotAssemblerRegistry.java,RefinementLineageAuditSnapshotAssemblerTest.java`：实现世系审计快照注册
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/.../RefinementLineageNodeDraftAuditSnapshotAssembler.java`、`.../RefinementLineageRelationDraftAuditSnapshotAssembler.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/.../AuditSnapshotAssemblerRegistry.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/.../RefinementLineageAuditSnapshotAssemblerTest.java`
    - 处理动作：为世系草稿对象接入并注册字段级审计快照
    - 验收点：System 审计注册表可识别世系草稿对象审计快照
    - 重要度：7/10

- [ ] `RefinementWorkbenchPageRequest.java,RefinementDetailRequest.java,UpsertRefinementEntityRequest.java,UpsertQualityAnnotationRequest.java,QualitySummaryRequest.java`：新增最终版接口请求模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/.../RefinementWorkbenchPageRequest.java`、`.../RefinementDetailRequest.java`、`.../UpsertRefinementEntityRequest.java`、`.../UpsertQualityAnnotationRequest.java`、`.../QualitySummaryRequest.java`
    - 处理动作：新增精修列表、详情、实体写入、质量标注和质量汇总的最终版 HTTP 请求模型
    - 验收点：HTTP 请求模型可稳定覆盖 Phase 1 和后续未开放接口
    - 重要度：9/10

- [ ] `补齐剩余接口请求模型`：补齐关系、世系与删除类请求模型
    - 任务类型：拆解任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/request/`
    - 处理动作：将关系、世系、删除和确认类请求模型继续拆分为不超过 5 个文件的执行任务
    - 验收点：剩余请求模型被拆成可独立验收的 TODO 项
    - 重要度：8/10

- [ ] `RefinementDetailResponse.java,RefinementEntityResponse.java,RefinementRelationResponse.java,QualityAnnotationResponse.java,QualitySummaryResponse.java`：新增最终版接口响应模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/.../RefinementDetailResponse.java`、`.../RefinementEntityResponse.java`、`.../RefinementRelationResponse.java`、`.../QualityAnnotationResponse.java`、`.../QualitySummaryResponse.java`
    - 处理动作：新增精修详情、实体、关系、标注和质量汇总响应模型
    - 验收点：接口响应模型可直接支撑前端 Phase 1 页面和后续扩展接口
    - 重要度：9/10

- [ ] `KnowledgeGraphRefinementController.java,KnowledgeGraphRefinementInterfaceAssembler.java,KnowledgeGraphRefinementControllerTest.java`：实现精修 Controller 与协议装配
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/.../KnowledgeGraphRefinementController.java`、`.../KnowledgeGraphRefinementInterfaceAssembler.java`、`.../KnowledgeGraphRefinementControllerTest.java`
    - 处理动作：实现 `/api/knowledge/refinement` 最终版接口入口、协议装配和控制器测试
    - 验收点：Phase 1 开放接口和后续保留接口在控制器层定义完整且测试通过
    - 重要度：10/10

- [ ] `refinement-types.ts,refinement-service.ts,refinement-service.test.ts`：新增前端最终版类型与服务
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-types.ts`、`.../refinement-service.ts`、`.../refinement-service.test.ts`
    - 处理动作：定义前端最终版类型并实现 Phase 1 使用的精修服务和契约测试
    - 验收点：前端类型覆盖 `GRAPH / RELATION / LINEAGE / ANNOTATION / QUALITY`，服务契约测试通过
    - 重要度：9/10

- [ ] `refinement-filter-form.tsx,refinement-workbench-table.tsx,refinement-progress-summary.tsx`：实现精修列表组件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-filter-form.tsx`、`.../refinement-workbench-table.tsx`、`.../refinement-progress-summary.tsx`
    - 处理动作：实现待精修列表、门类筛选和进度汇总组件
    - 验收点：页面可按门类和来源筛选待精修内容并展示进度统计
    - 重要度：9/10

- [ ] `refinement-entity-table.tsx,refinement-entity-editor.tsx,refinement-entity-delete-modal.tsx`：实现实体精修组件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-entity-table.tsx`、`.../refinement-entity-editor.tsx`、`.../refinement-entity-delete-modal.tsx`
    - 处理动作：实现实体草稿表、编辑抽屉和删除确认组件
    - 验收点：用户可在页面中新增、编辑、确认和删除实体草稿
    - 重要度：10/10

- [ ] `refinement-relation-table.tsx,refinement-relation-editor.tsx,refinement-relation-delete-modal.tsx`：实现关系精修组件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-relation-table.tsx`、`.../refinement-relation-editor.tsx`、`.../refinement-relation-delete-modal.tsx`
    - 处理动作：实现关系草稿表、编辑抽屉和删除确认组件
    - 验收点：用户可在页面中新增、编辑、确认和删除关系草稿
    - 重要度：10/10

- [ ] `refinement-page.tsx,refinement-page.css,refinement-page.test.tsx,router/index.tsx`：实现精修页面壳与路由
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-page.tsx`、`.../refinement-page.css`、`.../refinement-page.test.tsx`、`kuzhambu-apps/admin-web/src/router/index.tsx`
    - 处理动作：编排精修页面壳并接入 `/knowledge/refinement` 路由
    - 验收点：页面可完成“列表筛选 -> 打开任务 -> 编辑实体/关系 -> 应用任务”的主交互
    - 重要度：10/10

- [ ] `db/data-source/system.json,db/data/system.sql`：新增精修菜单与权限种子
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`db/data-source/system.json`、`db/data/system.sql`
    - 处理动作：新增 `知识图谱精修` 菜单和 `knowledge:refinement:view`、`knowledge:refinement:edit` 权限种子
    - 验收点：菜单生成结果包含精修入口且权限编码与前后端一致
    - 重要度：8/10

- [ ] `KnowledgeGraphRefinementTaskOpenTest,KnowledgeGraphRefinementApplyTest,KnowledgeGraphRefinementControllerTest,RefinementTaskRepositoryTest`：补齐后端最小验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/KnowledgeGraphRefinementTaskOpenTest.java`、`.../KnowledgeGraphRefinementApplyTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/.../KnowledgeGraphRefinementControllerTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/.../RefinementTaskRepositoryTest.java`
    - 处理动作：补齐精修任务打开、应用、控制器和仓储的最小后端验证
    - 验收点：后端 refinement 主链路具备最小自动化验证集合
    - 重要度：9/10

- [ ] `refinement-service.test.ts,refinement-page.test.tsx`：补齐前端最小验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-service.test.ts`、`.../refinement-page.test.tsx`
    - 处理动作：补齐精修服务契约和页面主交互测试
    - 验收点：前端 refinement 服务和页面主交互具备自动化测试
    - 重要度：8/10

- [ ] `KNOWLEDGE-DESIGN.md,KNOWLEDGE-IMPLEMENTATION-COVERAGE.md,AI-IMPLEMENTATION-COVERAGE.md`：同步设计与覆盖文档
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`docs/30-designs/KNOWLEDGE-DESIGN.md`、`docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`、`docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：在代码闭环后同步 Knowledge 设计和覆盖状态文档
    - 验收点：文档口径与最终实现一致且覆盖矩阵反映 refinement 进展
    - 重要度：7/10

## 待讨论项
