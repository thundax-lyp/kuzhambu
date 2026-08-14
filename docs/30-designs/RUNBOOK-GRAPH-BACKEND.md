# RUNBOOK Graph Backend

## Purpose

将双空间图谱 application/domain/infra 骨架变为可调用、可审计、可迁移的后端交付。本手册只允许执行者按顺序操作；接口真相源为 `docs/20-interfaces/KNOWLEDGE-GRAPH-INTERFACE.md`。

## Scope

`kuzhambu-servers/biz/knowledge/`、`db/schema/knowledge.sql`、Graph 接口测试和迁移 RUNBOOK。包含 Admin/Portal HTTP 入口、删除任务、审计来源、旧写入路径下线。

## Non-goals

不修改 Schema JSON 定义；不实现世系图；不改 Admin Web；不删除旧表，直到迁移核对完成。

## Mandatory File Plan

1. 新增 `kuzhambu-knowledge-interface/.../interfaces/admin/graph/GraphController.java`、`assembler/GraphInterfaceAssembler.java`、`controller/request/`、`controller/response/`。一个 controller 固定映射 `/knowledge/graph`，逐项实现接口文档所有 Admin URL。Controller 只做权限注解、request→command/query、response 映射；不得直接调用 repository。
2. 新增 `interfaces/portal/graph/GraphPortalController.java` 和 `assembler/GraphPortalInterfaceAssembler.java`，只实现 `/portal/knowledge/graph/material/get`，调用现有 `GraphPortalApplicationService`。
3. 替换 `kuzhambu-knowledge-domain/.../model/enums/GraphMaterialStatus.java`：枚举固定为 `DRAFT`、`PUBLISHING`、`PUBLISHED`、`WITHDRAWING`、`FAILED`，删除 `READY`。在 `db/schema/knowledge.sql` 的 `knowledge_graph_material` 表中，`published_at` 后固定新增 `failure_reason varchar(1024) DEFAULT NULL` 和 `failed_operation varchar(16) DEFAULT NULL`；在 `scripts/migrate-graph-material-status.sql` 固定执行 `ALTER TABLE knowledge_graph_material ADD COLUMN failure_reason varchar(1024) DEFAULT NULL AFTER published_at, ADD COLUMN failed_operation varchar(16) DEFAULT NULL AFTER failure_reason;`，再执行 `UPDATE knowledge_graph_material SET status = 'DRAFT' WHERE status = 'READY';`。同步修改 `GraphMaterial.java`、`GraphMaterialGraph.java`、`GraphPublicationExecutor.java`、`GraphPublicationApplicationServiceImpl.java`、抽取服务和全部 GraphMaterial 测试：仅 `DRAFT` 可编辑/抽取；发布成功 `DRAFT→PUBLISHING→PUBLISHED`，失败 `PUBLISHING→FAILED` 且 `failedOperation=PUBLISH`；撤回成功 `PUBLISHED→WITHDRAWING→DRAFT`，失败 `WITHDRAWING→FAILED` 且 `failedOperation=WITHDRAW`；重试前 `FAILED(PUBLISH)→DRAFT`、`FAILED(WITHDRAW)→PUBLISHED`。成功转换必须清空两个失败字段。`knowledge_graph_material.status` 仅写上述五值，响应固定返回 `failureReason`、`failedOperation`（非 `FAILED` 均为 `null`）。
4. 新增 domain `graph/model/entity/GraphMaterialDeletionChange.java`、`GraphMaterialDeletionTask.java`、对应 valueobject、enum、repository；新增 infra `graph/persistence/{dataobject,mapper,assembler}/` 与 `repository/impl/` 同名实现。删除并新建 deletion change/task 两张表，不做存量迁移；新表 DDL 含 `lock_version bigint NOT NULL DEFAULT 0`，对应 entity、DO、mapper 属性名均为 `lockVersion`。`decision` 与 `retry` 仅以该版本更新，版本不一致返回 `GRAPH_LOCK_CONFLICT`。
5. 新增 `application/graph/service/GraphMaterialDeletionApplicationService.java` 和 impl；实现 `precheck`、`decide`、`pageChanges`、`pageTasks`、`getTask`、`retry`、`processPendingTasks`。任务必须按 `idempotency_key` 幂等，执行前重新读取状态；保留贡献先写 `source_snapshot_json`，再清空草稿引用；撤回只改变本素材 ACTIVE mapping。
6. 新增 `GraphGovernanceOperation` 与 `GraphManualSource` 的 domain/infra/repository；在 `GraphPublishedApplicationServiceImpl` 的 create/update/delete/merge/split 中写入理由、前后快照和 `auditLogId`。新增 `kuzhambu-servers/biz/system/kuzhambu-system-facade/` 模块：在其 `pom.xml` 定义 `SystemAuditFacade.java`、`SystemAuditFacadeRequest.java`、`SystemAuditFacadeResponse.java`；在 `kuzhambu-system-application/.../facade/SystemAuditFacadeImpl.java` 实现 `record()` 与 `get()`；将 facade 加入 `kuzhambu-servers/biz/system/pom.xml` reactor，并在 `kuzhambu-knowledge-application/pom.xml` 添加该 facade 依赖。Knowledge application 只能依赖这个 facade，禁止导入 System application/domain/infra。`system_audit_log` 是操作者唯一真相，Graph 详情通过 facade 的 `get(auditLogId)` 返回 `operatorId`、`operatorName`、`occurredAt`；不得在 knowledge 表增加操作者 ID。不得在 mapper 中跨表编排。
7. 修改 `application/graph/command/GraphPublicationCommand.java`：加入 `String previewToken` 和 `List<GraphPublicationConflictDecision> conflictDecisions`；新增 `command/GraphPublicationConflictDecision.java`，字段固定为 `objectType`、`materialObjectId`、`action`、`matchedObjectId`。`GraphBatchPublicationPreviewQuery`、`GraphBatchPublicationCommand` 和 result 固定按输入顺序保存每份素材的预览、确认和成功/失败结果；每份素材独立执行，禁止跨素材事务或失败短路。新增 domain `model/entity/GraphPublicationPreviewToken.java`、`repository/GraphPublicationPreviewTokenRepository.java`、infra `persistence/dataobject/GraphPublicationPreviewTokenDO.java`、`persistence/mapper/GraphPublicationPreviewTokenMapper.java`、`persistence/assembler/GraphPublicationPreviewTokenPersistenceAssembler.java`、`repository/impl/GraphPublicationPreviewTokenRepositoryImpl.java`。`GraphPublicationExecutor` 只接受该 token 的决策；确认时校验所有版本，写 `knowledge_graph_publish_record.conflict_decisions_json` 后原子消费 token；任一变化返回 `GRAPH_PREVIEW_STALE`。
8. 修改 `application/graph/command/GraphPublishedNodeDeleteCommand.java`、`GraphPublishedEdgeDeleteCommand.java`、`GraphPublishedNodeMergeCommand.java`、`GraphPublishedNodeSplitCommand.java`：每个确认 command 加 `impactToken`。新增 domain `model/entity/GraphGovernanceImpactToken.java`、`repository/GraphGovernanceImpactTokenRepository.java`、infra `persistence/dataobject/GraphGovernanceImpactTokenDO.java`、`persistence/mapper/GraphGovernanceImpactTokenMapper.java`、`persistence/assembler/GraphGovernanceImpactTokenPersistenceAssembler.java`、`repository/impl/GraphGovernanceImpactTokenRepositoryImpl.java`，保存预览中 incident edge、mapping 和对象版本。`GraphPublishedApplicationServiceImpl` 确认前必须逐项校验 token；节点级联删除只删除 preview 中的边，任何新增/变更依赖均返回 `GRAPH_PREVIEW_STALE`。
9. 扩展 `GraphWorkbenchOverviewResult.java`、`GraphWorkbenchRepository.java`、`GraphWorkbenchRepositoryImpl.java` 和 `GraphWorkbenchMapper.java`：新增 `recentActivities`、`pendingConflictCount` 的 read model、SQL 与测试；不得让 controller 拼装或伪造字段。
10. 删除或停止暴露旧 `graph-extraction`、graph version、refinement 的图谱写入口；保留旧表只读迁移数据。新旧接口不得同时写正式图谱。

### Required DDL Additions

在 `db/schema/knowledge.sql` 新增下列两张表；字段名、类型、索引不得自行变更。

```sql
CREATE TABLE IF NOT EXISTS `knowledge_graph_publication_preview_token` (
    `token` varchar(64) NOT NULL,
    `material_id` bigint NOT NULL,
    `material_lock_version` bigint NOT NULL,
    `snapshot_json` json NOT NULL,
    `expires_at` BIGINT NOT NULL,
    `consumed_at` BIGINT DEFAULT NULL,
    PRIMARY KEY (`token`),
    KEY `idx_knowledge_graph_publication_preview_material_expiry` (`material_id`, `expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图谱发布预览令牌';

CREATE TABLE IF NOT EXISTS `knowledge_graph_governance_impact_token` (
    `token` varchar(64) NOT NULL,
    `operation_type` varchar(32) NOT NULL,
    `snapshot_json` json NOT NULL,
    `expires_at` BIGINT NOT NULL,
    `consumed_at` BIGINT DEFAULT NULL,
    PRIMARY KEY (`token`),
    KEY `idx_knowledge_graph_governance_impact_expiry` (`operation_type`, `expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图谱治理影响预览令牌';
```

`snapshot_json` 必须含全部受影响对象的 `id`、`lockVersion` 和关联集合；不得只保存 hash 或只保存对象 ID。过期或已消费 token 返回 `GRAPH_PREVIEW_STALE`。

重建 `knowledge_graph_governance_operation` 时，`reason` 后必须新增 `audit_log_id bigint NOT NULL,`，并新增 `KEY idx_knowledge_graph_governance_operation_audit (audit_log_id)`。重建 `knowledge_graph_manual_source` 时，`reason` 后必须新增 `audit_log_id bigint NOT NULL,`，并新增 `KEY idx_knowledge_graph_manual_source_audit (audit_log_id)`。Graph 的 entity/DO 属性名固定为 `auditLogId`；它只保存审计日志引用，绝不复制操作者字段。

删除表重建固定步骤：先在目标数据库查询 `knowledge_graph_material_deletion_change` 与 `knowledge_graph_material_deletion_task` 的行数；任何一表行数非 `0` 时停止并报告，不得删除。两表均为 `0` 时，先执行 `DROP TABLE IF EXISTS knowledge_graph_material_deletion_task;`，再执行 `DROP TABLE IF EXISTS knowledge_graph_material_deletion_change;`，最后用本文件确定的新 DDL 重新创建。新 DDL 的 `status` 后必须有 `lock_version bigint NOT NULL DEFAULT 0,`；对应 entity/DO 属性名均为 `lockVersion`。

### Fixed Interface Files

新增的 request/response 与 assembler 文件固定如下，禁止按个人偏好拆分或合并：

```text
interfaces/admin/graph/GraphController.java
interfaces/admin/graph/assembler/GraphInterfaceAssembler.java
interfaces/admin/graph/controller/request/GraphWorkbenchRequests.java
interfaces/admin/graph/controller/request/GraphMaterialRequests.java
interfaces/admin/graph/controller/request/GraphPublicationRequests.java
interfaces/admin/graph/controller/request/GraphPublishedRequests.java
interfaces/admin/graph/controller/request/GraphDeletionRequests.java
interfaces/admin/graph/controller/response/GraphWorkbenchResponses.java
interfaces/admin/graph/controller/response/GraphMaterialResponses.java
interfaces/admin/graph/controller/response/GraphPublicationResponses.java
interfaces/admin/graph/controller/response/GraphPublishedResponses.java
interfaces/admin/graph/controller/response/GraphDeletionResponses.java
interfaces/portal/graph/GraphPortalController.java
interfaces/portal/graph/assembler/GraphPortalInterfaceAssembler.java
interfaces/portal/graph/controller/request/GraphPortalMaterialRequest.java
interfaces/portal/graph/controller/response/GraphPortalMaterialResponse.java
```

## Exact Execution Steps

1. 先运行 `mvn -pl biz/knowledge -am test`，记录失败；不得把既有失败算作通过。
2. 按 Mandatory File Plan 的 1-2 完成接口 DTO 和接口单测；每个 URL 至少覆盖权限拒绝、成功映射、错误码映射。批量发布测试还必须断言输入顺序保持、单项失败不短路和无跨素材回滚。
3. 完成第 3 项状态迁移后运行 GraphMaterial 状态机单测：五个状态均可持久化；`READY` 被拒绝；发布/撤回成功和失败转换均通过。
4. 完成 4-5 后运行 deletion service 单测：重复投递、重试、保留贡献、撤回关联、失败后恢复，五项必须通过。
5. 完成 6-9 后覆盖发布并发、冲突未决、批量单项失败不影响其余素材、撤回不删发布对象、合并/拆分映射完整性和审计快照。
6. 最后新增固定脚本 `scripts/verify-graph-migration.sh`：脚本按 `content_type=SANCAI_ENTRY` 对比迁移前后素材、节点、边、映射数；未定义映射时以非 `0` 退出且禁止自动发布。验证命令和结果只写入 PR 描述，不新增 readiness 文件。

## Required PR 248/249 Review Closure

以下是未解决评论的唯一处理顺序。每一项完成代码、测试和提交后，使用 GitHub 回复说明提交 SHA 与验证命令，再 resolve 对应 thread；不得在代码未合入本分支时回复或 resolve。

1. PR #248 `PRRT_kwDOSkforM6ZJiun`：按 Mandatory File Plan 第 7 项实现 publication conflict decisions。回复：`已在 <SHA> 增加每对象决策并由 preview token 绑定；<command> 通过。`，再 resolve。
2. PR #248 `PRRT_kwDOSkforM6ZJiuq` 与 PR #249 `PRRT_kwDOSkforM6ZJpt5`：按第 7 项实现 preview token 和发布对象版本校验。两条均回复同一 SHA/验证证据，再分别 resolve。
3. PR #248 `PRRT_kwDOSkforM6ZJiuv`：按第 4-5 项实现 deletion change/decision/task；不得继续由 `GraphMaterialEventCommand` 直接删除发布映射。回复包含保留贡献与撤回关联的测试名，再 resolve。
4. PR #248 `PRRT_kwDOSkforM6ZJiuw`：按第 8 项实现 impact token；测试必须在 preview 后新增 incident edge 并断言 delete 返回 `GRAPH_PREVIEW_STALE`，再回复/resolve。

## Verification

每次 Java 改动先运行 `mvn -pl <touched module> spotless:apply`，再运行：

```sh
cd kuzhambu-servers
mvn -pl biz/knowledge -am spotless:check checkstyle:check test
```

必须新增 controller integration tests 和 service tests；不允许仅以编译通过交付。对真实数据库测试，验证素材删除后不存在指向已删除草稿节点/边的 ACTIVE mapping。

## Commit Boundaries

每完成下列一项且其指定测试通过，立即单独提交；禁止把两个编号的实现放进同一提交，也禁止夹带格式化以外的重构。

1. `Feat(services/knowledge): 新增图谱管理 HTTP 接口组`：`GraphController`、Admin request/response、assembler 和 Admin controller tests。
2. `Feat(services/knowledge): 新增图谱门户素材查询接口`：`GraphPortalController`、Portal request/response、assembler 和 Portal controller tests。
3. `Feat(services/knowledge): 迁移图谱素材状态机`：仅 `GraphMaterialStatus`、状态转换、持久化值、失败原因响应和状态机测试。
4. `Feat(services/knowledge): 重建素材删除变更与任务存储`：仅 deletion change/task 的 domain、infra、DDL、`lockVersion` 乐观锁测试；不含 application service。
5. `Feat(services/knowledge): 实现素材删除预检与决策`：仅 `precheck`、`decide`、change 查询与保留贡献/撤回关联测试。
6. `Feat(services/knowledge): 实现素材删除任务执行与重试`：仅 task 查询、幂等投递、执行、失败恢复、重试和对应测试。
7. `Feat(services/knowledge): 记录图谱治理操作审计`：System audit facade、governance operation/manual source 存储、审计详情查询和测试。
8. `Feat(services/knowledge): 校验图谱发布预览令牌与批量发布`：publication preview token DDL/domain/infra、冲突决策、版本校验、原子消费、逐素材批量预览/提交与测试。
9. `Feat(services/knowledge): 校验图谱治理影响预览`：impact token DDL/domain/infra、删除/合并/拆分确认校验、级联边变更拒绝测试。
10. `Feat(services/knowledge): 补齐图谱工作台活动读模型`：仅 `recentActivities`、`pendingConflictCount` 的 result/repository/mapper SQL 和测试。
11. `Feat(services/knowledge): 下线旧图谱写入入口`：仅旧写入口下线、只读迁移保护和 `verify-graph-migration.sh`；迁移核对测试必须随该提交完成。

## Closure

在 PR 描述记录接口、迁移与测试命令及结果后删除本 RUNBOOK。不得新增或修改任何 readiness 文件，也不得修改 `KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`。
