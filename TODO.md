# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `KnowledgeGraphRefinementApplicationServiceImpl.java,QualityAnnotationWriteTest.java`：实现质量标注写入
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/impl/KnowledgeGraphRefinementApplicationServiceImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/QualityAnnotationWriteTest.java`
    - 处理动作：实现质量标注的后端写入能力
    - 验收点：质量标注接口在后端走通且具备最小测试
    - 重要度：7/10

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

## 待讨论项
