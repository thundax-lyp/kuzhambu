# RUNBOOK MySQL Instant Epoch Milliseconds

## Purpose

本 RUNBOOK 用于执行 Java `Instant` 与 MySQL 存储状态的统一校准。

本次目标是把当前 `Instant + datetime(3)` 组合升级为 `Instant + BIGINT epoch_ms` 组合：

- Java 代码继续以 `Instant` 表达绝对时间点。
- MySQL 绝对时间点字段统一存储 Unix epoch milliseconds，列类型使用 `BIGINT`。
- seed SQL 保留人类可读时间字面量；这些字面量按 `Asia/Shanghai` 展示时间解释，写入时必须通过统一 MySQL 表达式或生成脚本转换为 epoch milliseconds。
- 摘除为了兼容 `java.util.Date`、`datetime(3)` 或连接时区而存在的临时处理。

当前 `docs/00-governance/SERVERS-DATABASE-RULES.md` 中 `Instant <-> datetime(3)` 口径已经过时。本 RUNBOOK 是本分支执行依据；完成后必须把新的稳定规则沉淀回治理文档，并清理本 RUNBOOK。

## Target Storage Contract

绝对时间点统一使用：

```text
Java type: java.time.Instant
MySQL type: BIGINT
MySQL value: Unix epoch milliseconds
SQL readable source: 'YYYY-MM-DD HH:mm:ss.SSS' as Asia/Shanghai display time
```

实现约束：

- `BIGINT` 可为负数，用于承载 1970 年前的历史时间点。
- Java 写库：`Instant.toEpochMilli()`。
- Java 读库：`Instant.ofEpochMilli(value)`。
- 数据库只保留毫秒精度，小于毫秒的纳秒精度不进入数据库，语义上与原 `datetime(3)` 一致。
- SQL 初始化数据不得直接把时间字符串写入绝对时间点列。
- SQL 初始化数据允许保留人类可读字面量，但必须按 `Asia/Shanghai` 展示时间通过统一 MySQL 表达式或生成脚本转换。

建议 MySQL 表达式：

```sql
TIMESTAMPDIFF(MICROSECOND, '1970-01-01 08:00:00.000000', '1570-01-01 00:00:00.000000') DIV 1000
```

这里不使用 `CONVERT_TZ(..., 'Asia/Shanghai', 'UTC')`，避免依赖 MySQL 时区表。脚本中应封装生成函数，函数名和注释必须说明入参是 `Asia/Shanghai` 展示时间。

## Non-goals

- 不做旧库在线迁移，不写旧数据回填脚本。
- 不保留 `datetime(3)` 旧兼容读写路径。
- 不把所有 `Asia/Shanghai` 业务展示、文件命名、路径分桶逻辑机械删除。
- 不改变 HTTP JSON、MQ JSON、worker JSON 对外继续使用 `Instant` / ISO-8601 的语义，除非接口本身仍残留 `java.util.Date`。

## Schema Inventory

当前全域共发现 117 个 `datetime(3)` 字段，均应升级为 `BIGINT` epoch milliseconds。

### `db/schema/ai.sql`

- `ai_model.registered_at`
- `ai_business_config.configured_at`
- `ai_prompt_template.registered_at`
- `ai_prompt_version.registered_at`
- `ai_invocation_log.requested_at`
- `ai_invocation_log.completed_at`
- `ai_candidate.requested_at`
- `ai_candidate.applied_at`
- `ai_candidate.rejected_at`
- `ai_batch_job.requested_at`
- `ai_batch_job.cancelled_at`
- `ai_batch_job.completed_at`

### `db/schema/classics.sql`

- `classics_sancai_entry.current_versioned_at`
- `classics_sancai_entry.content_updated_at`
- `classics_sancai_entry_draft.autosaved_at`
- `classics_sancai_showcase.requested_at`
- `classics_wangqi_document.document_time`
- `classics_wangqi_document.current_versioned_at`
- `classics_wangqi_document.content_updated_at`
- `classics_wangqi_document_event.occurred_at`
- `classics_ming_customs_entry.current_versioned_at`
- `classics_ming_customs_entry.content_updated_at`
- `classics_content_version.versioned_at`
- `classics_content_export_job.requested_at`
- `classics_content_export_job.expires_at`
- `classics_share_link.issued_at`
- `classics_share_link.expires_at`
- `classics_share_access_record.accessed_at`

### `db/schema/discovery.sql`

- `discovery_search_query_event.searched_at`
- `discovery_search_click_event.created_at`
- `discovery_search_event.created_at`
- `discovery_query_understanding.created_at`
- `discovery_qa_session.opened_at`
- `discovery_qa_session.last_message_at`
- `discovery_qa_session.removed_at`
- `discovery_qa_message.sent_at`
- `discovery_qa_message.answered_at`
- `discovery_qa_message_source.referenced_at`
- `discovery_qa_retrieval_trace.retrieved_at`
- `discovery_qa_knowledge_sync_batch.started_at`
- `discovery_qa_knowledge_sync_batch.finished_at`
- `discovery_qa_knowledge_sync_item.synced_at`
- `discovery_qa_knowledge_sync_item.created_at`
- `discovery_qa_knowledge_sync_item.updated_at`
- `discovery_qa_session_export.requested_at`
- `discovery_qa_session_export.completed_at`

### `db/schema/knowledge.sql`

- `knowledge_tag.created_at`
- `knowledge_tag.reviewed_at`
- `knowledge_tag.deprecated_at`
- `knowledge_graph_extraction_task.requested_at`
- `knowledge_graph_extraction_task.completed_at`
- `knowledge_graph_extraction_task.applied_at`
- `knowledge_graph_version.applied_at`
- `knowledge_entity.first_extracted_at`
- `knowledge_entity.last_extracted_at`
- `knowledge_entity.confirmed_at`
- `knowledge_relation.first_extracted_at`
- `knowledge_relation.last_extracted_at`
- `knowledge_relation.confirmed_at`
- `knowledge_lineage_node.first_extracted_at`
- `knowledge_lineage_node.last_extracted_at`
- `knowledge_lineage_node.confirmed_at`
- `knowledge_lineage_relation.first_extracted_at`
- `knowledge_lineage_relation.last_extracted_at`
- `knowledge_lineage_relation.confirmed_at`
- `knowledge_refinement_task.opened_at`
- `knowledge_refinement_task.submitted_at`
- `knowledge_refinement_task.applied_at`
- `knowledge_refinement_task.cancelled_at`
- `knowledge_refinement_entity_draft.created_at`
- `knowledge_refinement_entity_draft.updated_at`
- `knowledge_refinement_relation_draft.created_at`
- `knowledge_refinement_relation_draft.updated_at`
- `knowledge_refinement_lineage_node_draft.created_at`
- `knowledge_refinement_lineage_node_draft.updated_at`
- `knowledge_refinement_lineage_relation_draft.created_at`
- `knowledge_refinement_lineage_relation_draft.updated_at`
- `knowledge_quality_annotation.created_at`
- `knowledge_quality_annotation.updated_at`
- `knowledge_quality_report.generated_at`
- `knowledge_quality_report.published_at`
- `knowledge_quality_report.created_at`
- `knowledge_quality_report.updated_at`
- `knowledge_quality_report_issue.created_at`
- `knowledge_quality_report_source_detail.applied_at`
- `knowledge_quality_report_source_detail.created_at`

### `db/schema/operations.sql`

- `operations_report.requested_at`
- `operations_report.completed_at`
- `operations_backup.started_at`
- `operations_backup.completed_at`
- `operations_backup.expires_at`
- `operations_restore.write_block_started_at`
- `operations_restore.write_block_released_at`
- `operations_restore.started_at`
- `operations_restore.completed_at`
- `operations_cleanup_job.started_at`
- `operations_cleanup_job.completed_at`
- `operations_cleanup_item.processed_at`
- `operations_health_check.checked_at`
- `operations_health_alert.first_triggered_at`
- `operations_health_alert.last_triggered_at`
- `operations_health_alert.acked_at`
- `operations_health_alert.recovered_at`
- `operations_long_task_snapshot.started_at`
- `operations_long_task_snapshot.completed_at`
- `operations_long_task_snapshot.snapshot_at`

### `db/schema/storage.sql`

- `storage_object.stored_at`
- `storage_multipart_upload.completed_date`
- `storage_multipart_upload.aborted_date`

### `db/schema/system.sql`

- `system_auth_principal_credential.locked_until`
- `system_auth_principal_credential.expires_at`
- `system_auth_principal_credential.last_verified_at`
- `system_auth_principal_login_event.occurred_at`
- `system_log.log_date`
- `system_audit_meta.last_operated_at`
- `system_audit_meta.created_at`
- `system_audit_log.occurred_at`

## Related Files

### Governance And Design Files

- `docs/00-governance/SERVERS-DATABASE-RULES.md`
- `docs/30-designs/CLASSICS-DESIGN.md`
- `docs/30-designs/RUNBOOK-MYSQL-INSTANT-EPOCH-MS.md`

### Schema Files

- `db/schema/ai.sql`
- `db/schema/classics.sql`
- `db/schema/discovery.sql`
- `db/schema/knowledge.sql`
- `db/schema/operations.sql`
- `db/schema/storage.sql`
- `db/schema/system.sql`

### Seed JSON Source Files 1

- `db/data-source/sancai-manuscripts.json`
- `db/data-source/sancai-tags.json`
- `db/data-source/wangqi-documents-full.json`
- `db/data-source/ai-prompts/classics/fusion/meta.json`
- `db/data-source/ai-prompts/classics/image-analysis/meta.json`
- `db/data-source/ai-prompts/classics/image-generation/meta.json`
- `db/data-source/ai-prompts/classics/qa/meta.json`
- `db/data-source/ai-prompts/classics/split/meta.json`

### Seed JSON Source Files 2

- `db/data-source/ai-prompts/classics/summary/meta.json`
- `db/data-source/ai-prompts/classics/tags/meta.json`
- `db/data-source/ai-prompts/classics/translate-batch-item/meta.json`
- `db/data-source/ai-prompts/classics/translate/meta.json`
- `db/data-source/ai-prompts/classics/visual/meta.json`
- `db/data-source/ai-prompts/discovery/answer-generation/meta.json`
- `db/data-source/ai-prompts/discovery/query-understanding/meta.json`
- `db/data-source/ai-prompts/knowledge/graph-extraction/meta.json`

### Seed JSON Source Files 3

- `db/data-source/ai-prompts/knowledge/lineage-extraction/meta.json`
- `db/data-source/ai-prompts/knowledge/relation-extraction/meta.json`
- `db/data-source/ai-prompts/knowledge/tags/meta.json`
- `db/data-source/ai-prompts/platform/prompt-suggestion/meta.json`
- `db/data-source/ai-prompts/platform/version-summary/meta.json`
- `db/data-source/system.json`

### Seed SQL Files

- `db/data/ai.sql`
- `db/data/classics.sql`
- `db/data/discovery.sql`
- `db/data/knowledge.sql`
- `db/data/test.sql`

### Seed Script Files

- `scripts/classics-json-to-sql.sh`
- `scripts/generate-ai-data-sql.ts`
- `scripts/generate-sancai-knowledge-data-sql.mjs`
- `scripts/generate-system-data-sql.ts`
- `scripts/verify-classics.sh`

### MySQL Connection Files

- `.env.example`
- `deploy/docker-compose.yml`
- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/resources/application.yml`
- `kuzhambu-servers/starter/kuzhambu-portal-starter/src/main/resources/application.yml`

### Common MyBatis Files

- `kuzhambu-servers/common/kuzhambu-common-mybatis/src/main/java/com/thundax/kuzhambu/common/mybatis/configure/MybatisPlusConfiguration.java`
- `kuzhambu-servers/common/kuzhambu-common-mybatis/src/main/java/com/thundax/kuzhambu/common/mybatis/typehandler/InstantEpochMillisTypeHandler.java`
- `kuzhambu-servers/common/kuzhambu-common-mybatis/src/test/java/com/thundax/kuzhambu/common/mybatis/typehandler/InstantEpochMillisTypeHandlerTest.java`
- `kuzhambu-servers/common/kuzhambu-common-mybatis/src/test/java/com/thundax/kuzhambu/common/mybatis/typehandler/JdbcStatementStub.java`

### Date Residual Files

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/request/DiscoveryQaAdminRequests.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/assembler/DiscoverySearchPortalInterfaceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/DiscoveryQaAdminControllerTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/service/impl/OperationsCleanupLegacyTimeAdapter.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/support/OperationsDashboardLegacyTimeAdapter.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/auth/repository/impl/PrincipalAccessTokenRepositoryImpl.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/auth/repository/impl/PrincipalAuthSessionRepositoryImpl.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/auth/repository/impl/PrincipalRefreshTokenRepositoryImpl.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/test/java/com/thundax/kuzhambu/system/infra/auth/repository/impl/AuthCacheNamespaceTest.java`

### Business Timezone Files To Keep

以下文件中的 `Asia/Shanghai` 用于业务展示、报表口径、文件名或对象路径，不属于 MySQL 绝对时间点存储适配，不能机械删除：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/backup/service/impl/BackupApplicationServiceImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/restore/service/impl/RestoreApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/report/service/impl/ClassicsReportApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/report/service/impl/DiscoveryReportApplicationServiceImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/repository/impl/TagGovernanceMetricsRepositoryImpl.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/StoredObjectContentRepositoryImpl.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/entity/StoredObject.java`

## Persistence DO Validation Files

这些 DO 字段继续保持 `Instant`，本次不应改成 `Long`。校准由公共 TypeHandler 完成。每组不超过 8 个文件，执行时按组检查字段名是否覆盖 schema inventory。

### DO Group 1

- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/config/persistence/dataobject/AiBusinessConfigDO.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/config/persistence/dataobject/AiModelDO.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/config/persistence/dataobject/PromptTemplateDO.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/config/persistence/dataobject/PromptVersionDO.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/invocation/persistence/dataobject/AiBatchJobDO.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/invocation/persistence/dataobject/AiCandidateDO.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/invocation/persistence/dataobject/AiInvocationLogDO.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/persistence/dataobject/ClassicsContentExportJobDO.java`

### DO Group 2

- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/persistence/dataobject/ClassicsContentVersionDO.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/mingcustoms/persistence/dataobject/MingCustomsEntryDO.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/dataobject/SancaiEntryDO.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/dataobject/SancaiEntryDraftDO.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/dataobject/SancaiShowcaseDO.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/persistence/dataobject/ClassicsShareAccessRecordDO.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/persistence/dataobject/ClassicsShareLinkDO.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/persistence/dataobject/ClassicsSharePortalListItemDO.java`

### DO Group 3

- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/wangqi/persistence/dataobject/WangqiDocumentDO.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/wangqi/persistence/dataobject/WangqiDocumentEventDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaKnowledgeSyncBatchDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaKnowledgeSyncItemDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaMessageDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaRetrievalTraceDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaSessionDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaSessionExportDO.java`

### DO Group 4

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaSourceDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/dataobject/QueryUnderstandingDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/dataobject/SearchClickEventDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/dataobject/SearchEventDO.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/GraphExtractionTaskDO.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/GraphVersionDO.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/KnowledgeEntityDO.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/KnowledgeLineageNodeDO.java`

### DO Group 5

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/KnowledgeLineageRelationDO.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/KnowledgeRelationDO.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/QualityAnnotationDO.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/QualityReportDO.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/QualityReportIssueDO.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/QualityReportSourceDetailDO.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/RefinementEntityDraftDO.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/RefinementLineageNodeDraftDO.java`

### DO Group 6

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/RefinementLineageRelationDraftDO.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/RefinementRelationDraftDO.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/RefinementTaskDO.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/persistence/dataobject/TagDO.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/backup/persistence/dataobject/BackupDO.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/cleanup/persistence/dataobject/CleanupItemDO.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/cleanup/persistence/dataobject/CleanupJobDO.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/persistence/dataobject/HealthAlertDO.java`

### DO Group 7

- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/persistence/dataobject/HealthCheckDO.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/persistence/dataobject/ReportDO.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/restore/persistence/dataobject/RestoreDO.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/task/persistence/dataobject/LongTaskSnapshotDO.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/persistence/dataobject/MultipartUploadSessionDO.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/persistence/dataobject/StoredObjectDO.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/audit/persistence/dataobject/AuditLogDO.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/audit/persistence/dataobject/AuditMetaDO.java`

### DO Group 8

- `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/auth/persistence/dataobject/PrincipalCredentialDO.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/auth/persistence/dataobject/PrincipalLoginEventDO.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/core/persistence/dataobject/LogDO.java`

## Date Residual Data Structures

这些字段必须改造或删除，不能继续保留 `java.util.Date` 双轨模型。

- `DiscoveryQaAdminRequests.QaSessionPageRequest.openedAtStart`: `java.util.Date` -> `Instant`
- `DiscoveryQaAdminRequests.QaSessionPageRequest.openedAtEnd`: `java.util.Date` -> `Instant`
- `PrincipalAccessTokenRepositoryImpl.CacheDTO.issuedAt`: `Date` -> `Instant` 或 `Long epochMs`
- `PrincipalAccessTokenRepositoryImpl.CacheDTO.expireAt`: `Date` -> `Instant` 或 `Long epochMs`
- `PrincipalAuthSessionRepositoryImpl.CacheDTO.issuedAt`: `Date` -> `Instant` 或 `Long epochMs`
- `PrincipalAuthSessionRepositoryImpl.CacheDTO.lastAccessTime`: `Date` -> `Instant` 或 `Long epochMs`
- `PrincipalAuthSessionRepositoryImpl.CacheDTO.expireAt`: `Date` -> `Instant` 或 `Long epochMs`
- `PrincipalRefreshTokenRepositoryImpl.CacheDTO.issuedAt`: `Date` -> `Instant` 或 `Long epochMs`
- `PrincipalRefreshTokenRepositoryImpl.CacheDTO.expireAt`: `Date` -> `Instant` 或 `Long epochMs`

## Execution Plan

执行顺序必须从稳定规则到公共基础设施，再到 schema、seed、配置、业务残留、验证和现场清理。

### Task 1: 更新稳定文档口径

文件：

- `docs/00-governance/SERVERS-DATABASE-RULES.md`
- `docs/30-designs/CLASSICS-DESIGN.md`
- `docs/30-designs/RUNBOOK-MYSQL-INSTANT-EPOCH-MS.md`

动作：

- 将绝对时间点规则改为 `Instant + BIGINT epoch_ms`。
- 删除或改写 `Instant <-> datetime(3)` 旧规则。
- `CLASSICS-DESIGN.md` 中涉及 `datetime(3)` 的字段表同步为 `BIGINT epoch_ms`，避免设计文档与 schema 冲突。

验收：

- `rg "Instant.*datetime\\(3\\)|datetime\\(3\\).*Instant" docs/00-governance docs/30-designs` 无旧规则残留。

### Task 2: 增加公共 MyBatis TypeHandler

文件：

- `kuzhambu-servers/common/kuzhambu-common-mybatis/src/main/java/com/thundax/kuzhambu/common/mybatis/configure/MybatisPlusConfiguration.java`
- `kuzhambu-servers/common/kuzhambu-common-mybatis/src/main/java/com/thundax/kuzhambu/common/mybatis/typehandler/InstantEpochMillisTypeHandler.java`
- `kuzhambu-servers/common/kuzhambu-common-mybatis/src/test/java/com/thundax/kuzhambu/common/mybatis/typehandler/InstantEpochMillisTypeHandlerTest.java`
- `kuzhambu-servers/common/kuzhambu-common-mybatis/src/test/java/com/thundax/kuzhambu/common/mybatis/typehandler/JdbcStatementStub.java`

动作：

- 新增 `InstantEpochMillisTypeHandler`。
- 在 `MybatisPlusConfiguration` 注册 `Instant.class` 的默认 handler。
- 覆盖 null、正数 epoch、负数 epoch、毫秒精度截断、按列名读取、按列序号读取、CallableStatement 读取。

验收：

- `mvn -pl common/kuzhambu-common-mybatis test` 通过。

### Task 3: 修改 schema

文件：

- `db/schema/ai.sql`
- `db/schema/classics.sql`
- `db/schema/discovery.sql`
- `db/schema/knowledge.sql`
- `db/schema/operations.sql`
- `db/schema/storage.sql`
- `db/schema/system.sql`

动作：

- 将 Schema Inventory 中 117 个字段从 `datetime(3)` 改为 `BIGINT`。
- 保留字段名、nullable、索引和业务 comment。
- comment 中可补充 `epoch milliseconds`。

验收：

- `rg "datetime\\(3\\)|timestamp\\(3\\)" db/schema` 无结果。
- `rg "\\bBIGINT\\b" db/schema` 能覆盖 Schema Inventory 中 117 个字段。

### Task 4: 修改 seed JSON、JSON-to-SQL 脚本和生成后的 SQL

原则：

- 若 seed 数据有 JSON 源文件，必须优先修改 JSON 源文件和 JSON-to-SQL 生成脚本，不得只直接修改生成后的 `db/data/*.sql`。
- `db/data/*.sql` 对 JSON 驱动的数据只作为再生成后的同步结果。
- JSON 源中的人类可读时间字面量继续按 `Asia/Shanghai` 展示时间解释。

#### Task 4A: 修改 seed JSON 源 1

文件：

- `db/data-source/sancai-manuscripts.json`
- `db/data-source/sancai-tags.json`
- `db/data-source/wangqi-documents-full.json`
- `db/data-source/ai-prompts/classics/fusion/meta.json`
- `db/data-source/ai-prompts/classics/image-analysis/meta.json`
- `db/data-source/ai-prompts/classics/image-generation/meta.json`
- `db/data-source/ai-prompts/classics/qa/meta.json`
- `db/data-source/ai-prompts/classics/split/meta.json`

动作：

- 保留 JSON 中人类可读时间字面量。
- 确认生成脚本读取这些字面量时按 `Asia/Shanghai` 展示时间转换为 epoch milliseconds。
- 不在 JSON 中直接写 epoch 数字，除非对应字段在 JSON 语义上就是机器时间。

验收：

- JSON 源仍可读，时间字段没有被批量替换成无上下文整数。

#### Task 4B: 修改 seed JSON 源 2

文件：

- `db/data-source/ai-prompts/classics/summary/meta.json`
- `db/data-source/ai-prompts/classics/tags/meta.json`
- `db/data-source/ai-prompts/classics/translate-batch-item/meta.json`
- `db/data-source/ai-prompts/classics/translate/meta.json`
- `db/data-source/ai-prompts/classics/visual/meta.json`
- `db/data-source/ai-prompts/discovery/answer-generation/meta.json`
- `db/data-source/ai-prompts/discovery/query-understanding/meta.json`
- `db/data-source/ai-prompts/knowledge/graph-extraction/meta.json`

动作：

- 同 Task 4A。

验收：

- 同 Task 4A。

#### Task 4C: 修改 seed JSON 源 3

文件：

- `db/data-source/ai-prompts/knowledge/lineage-extraction/meta.json`
- `db/data-source/ai-prompts/knowledge/relation-extraction/meta.json`
- `db/data-source/ai-prompts/knowledge/tags/meta.json`
- `db/data-source/ai-prompts/platform/prompt-suggestion/meta.json`
- `db/data-source/ai-prompts/platform/version-summary/meta.json`
- `db/data-source/system.json`

动作：

- 同 Task 4A。

验收：

- 同 Task 4A。

#### Task 4D: 修改 JSON-to-SQL 生成脚本

文件：

- `scripts/classics-json-to-sql.sh`
- `scripts/generate-ai-data-sql.ts`
- `scripts/generate-sancai-knowledge-data-sql.mjs`
- `scripts/generate-system-data-sql.ts`
- `scripts/verify-classics.sh`

动作：

- 将写入绝对时间点字段的字符串字面量改为 epoch milliseconds 表达式或生成后的整数。
- JSON-to-SQL 生成脚本必须封装统一转换函数，保留 JSON 源中的人类可读源字面量，并明确源字面量按 `Asia/Shanghai` 展示时间解释。
- `scripts/classics-json-to-sql.sh` 负责 `db/data-source/sancai-manuscripts.json`、`db/data-source/sancai-tags.json` 到 `db/data/classics.sql` 的时间转换，不得绕过脚本只改 `db/data/classics.sql`。
- `scripts/generate-ai-data-sql.ts` 负责 `db/data-source/ai-prompts/**/meta.json` 到 `db/data/ai.sql` 的时间转换，不得绕过脚本只改 `db/data/ai.sql`。
- `scripts/generate-sancai-knowledge-data-sql.mjs` 负责 `db/data-source/sancai-tags.json`、`db/data-source/sancai-manuscripts.json` 到 `db/data/knowledge.sql` 的时间转换，不得绕过脚本只改 `db/data/knowledge.sql`。
- `scripts/generate-system-data-sql.ts` 负责 `db/data-source/system.json` 到 `db/data/system.sql` 的生成；当前未发现绝对时间点写入，执行时仍必须扫描确认无需改动。
- `db/data-source/wangqi-documents-full.json` 当前由 `db/data/classics.sql` 尾部保留段承载；如果本分支触碰其中时间字段，必须补齐或更新明确的 JSON-to-SQL 生成路径，不能只手改成品 SQL。
- `scripts/verify-classics.sh` 同步改断言，不再要求 `datetime(3)` 字符串直接出现在 SQL 值位置。

验收：

- `rg "'\\d{4}-\\d{2}-\\d{2}[ T]\\d{2}:\\d{2}:\\d{2}" db/data scripts` 的剩余结果必须只出现在转换表达式、脚本源常量或非绝对时间点文本中。
- `rg "'\\d{4}-\\d{2}-\\d{2}[ T]\\d{2}:\\d{2}:\\d{2}" db/data-source` 的剩余结果必须仍是人类可读源字面量，不得要求 JSON 源改成整数。

#### Task 4E: 同步生成后的 seed SQL

文件：

- `db/data/ai.sql`
- `db/data/classics.sql`
- `db/data/discovery.sql`
- `db/data/knowledge.sql`
- `db/data/test.sql`

动作：

- 运行 Task 4D 中对应 JSON-to-SQL 脚本，更新由 JSON 源驱动的成品 SQL。
- 对没有 JSON 源的手写 seed SQL，直接将写入绝对时间点字段的字符串字面量改为 epoch milliseconds 表达式或生成后的整数。
- 手写 SQL 表达式统一使用 `TIMESTAMPDIFF(MICROSECOND, '1970-01-01 08:00:00.000000', '<display_time>') DIV 1000`。

验收：

- `rg "'\\d{4}-\\d{2}-\\d{2}[ T]\\d{2}:\\d{2}:\\d{2}" db/data` 的剩余结果必须只出现在转换表达式或非绝对时间点文本中。
- 使用 `db/schema/*.sql` 重建库并导入 `db/data/*.sql` 成功。

### Task 5: 删除 MySQL 连接时区适配

文件：

- `.env.example`
- `deploy/docker-compose.yml`
- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/resources/application.yml`
- `kuzhambu-servers/starter/kuzhambu-portal-starter/src/main/resources/application.yml`

动作：

- 删除 `serverTimezone=Asia/Shanghai`。
- 不新增用于 `datetime` 兼容的 `connectionTimeZone` 或 `preserveInstants`。

验收：

- `rg "serverTimezone=Asia/Shanghai|connectionTimeZone|preserveInstants" .env.example deploy kuzhambu-servers/starter` 无结果或仅有明确非适配说明。

### Task 6: 清理 discovery 和 operations Date 残留

文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/request/DiscoveryQaAdminRequests.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/assembler/DiscoverySearchPortalInterfaceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/DiscoveryQaAdminControllerTest.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/service/impl/OperationsCleanupLegacyTimeAdapter.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/support/OperationsDashboardLegacyTimeAdapter.java`

动作：

- `openedAtStart`、`openedAtEnd` 改为 `Instant`。
- 删除 operations legacy adapter，调用方直接使用 `Instant`。
- 检查 `DiscoverySearchPortalInterfaceAssembler` 的日期边界转换是否为业务查询语义；如果保留，必须在 PR 说明中列为非 MySQL 存储适配。

验收：

- 上述文件无 `java.util.Date`、`Date.from`、`Date.toInstant`。

### Task 7: 清理 system auth Redis Date 残留

文件：

- `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/auth/repository/impl/PrincipalAccessTokenRepositoryImpl.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/auth/repository/impl/PrincipalAuthSessionRepositoryImpl.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/auth/repository/impl/PrincipalRefreshTokenRepositoryImpl.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/test/java/com/thundax/kuzhambu/system/infra/auth/repository/impl/AuthCacheNamespaceTest.java`

动作：

- Redis DTO 时间字段改为 `Instant` 或 `Long epochMs`。
- 删除 `Date.from` / `Date.toInstant` 转换方法。
- 更新缓存序列化测试。

验收：

- 上述文件无 `java.util.Date`、`Date.from`、`Date.toInstant`。

### Task 8: 按组校验 DO 映射

文件：

- 按 `Persistence DO Validation Files` 中 DO Group 1 到 DO Group 8 执行，每组不超过 8 个文件。

动作：

- 确认 DO 时间字段保持 `Instant`。
- 确认字段名与 Schema Inventory 一一对应。
- 如存在 mapper XML、注解 SQL 或手写类型处理，必须改为依赖公共 TypeHandler。

验收：

- `rg "private (Long|long) .*(_at|At|Date|Time)|private java\\.time\\.LocalDateTime|java\\.sql\\.Timestamp" kuzhambu-servers/biz/*/*-infra/src/main/java/com/thundax/kuzhambu/**/persistence/dataobject` 不出现本次绝对时间点字段误改。

### Task 9: 格式化和静态检查

文件：

- 本分支已修改的 Java、SQL、脚本和文档文件。

动作：

- 先对 touched Java 文件运行最窄范围 `spotless:apply`。
- 再运行 Maven 格式、静态检查和测试。

验收：

```sh
cd kuzhambu-servers
mvn -pl common/kuzhambu-common-mybatis spotless:apply
mvn -pl common/kuzhambu-common-mybatis test
mvn spotless:check
mvn checkstyle:check
mvn test
```

### Task 10: 数据库重建和导入验证

文件：

- `db/schema/ai.sql`
- `db/schema/classics.sql`
- `db/schema/discovery.sql`
- `db/schema/knowledge.sql`
- `db/schema/operations.sql`
- `db/schema/storage.sql`
- `db/schema/system.sql`
- `db/data/*.sql`

动作：

- 使用 schema 重建数据库。
- 重新导入 seed 数据。
- 启动 admin 或 portal starter，执行至少一个写入和读取 `Instant` 的接口或仓储验证。

验收：

- seed 字面量 `'2026-02-27 04:00:00.000'` 按 `Asia/Shanghai` 解释为 `2026-02-26T20:00:00Z`，写库后为 `1772136000000`。
- seed 字面量 `'1570-01-01 00:00:00.000'` 按 `Asia/Shanghai` 解释为 `1569-12-31T16:00:00Z`，通过 seed 表达式写库后为 `-12622809600000`。
- 同一时间值经过写库、读库、HTTP JSON 输出后仍表示同一瞬时时间。
- MySQL URL 删除 `serverTimezone=Asia/Shanghai` 后读写结果不变。

### Task 11: 全域残留扫描

文件：

- 全仓库。

动作：

```sh
rg "datetime\\(3\\)|timestamp\\(3\\)|serverTimezone=Asia/Shanghai|connectionTimeZone|preserveInstants|java\\.util\\.Date|Date\\.from|Date\\.toInstant|LocalDateTime|java\\.sql\\.Timestamp" . -g '!**/target/**' -g '!**/.git/**'
```

验收：

- 无 `datetime(3)` / `timestamp(3)` schema 残留。
- 无 MySQL 时区适配参数残留。
- 无需要摘除的 `java.util.Date` 兼容残留。
- 对保留的业务时区、查询边界转换、非 MySQL 存储时间处理逐项记录原因。

### Task 12: 清理现场

文件：

- `docs/30-designs/RUNBOOK-MYSQL-INSTANT-EPOCH-MS.md`
- `TODO.md`
- PR 描述或 `docs/40-readiness/` 证据文档，按实际使用情况处理。

动作：

- 将长期规则迁移到治理文档后删除本 RUNBOOK。
- 删除或收窄已完成 TODO。
- 检查 `git diff`，剔除无关格式化、生成物和临时文件。
- 确认没有 `target/`、日志、临时 SQL dump、IDE 文件、`dev.env` 等现场残留。

验收：

- `git status --short` 只包含本分支目标相关文件。
- PR 描述记录全域 grep、数据库重建导入、Maven 验证结果。

## Closure

本 RUNBOOK 是临时执行文件。任务关闭前必须完成 Task 12。
