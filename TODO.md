# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 当前任务项按数字编号顺序执行；不得跳过前置契约任务直接做后续调用方或重命名任务。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `06 graph 素材状态迁移 DDL`：落地状态字段与 READY 数据迁移
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-BACKEND.md`
    - 范围对象：`db/schema/knowledge.sql`、`scripts/migrate-graph-material-status.sql`，及迁移脚本测试（3 个文件）
    - 处理动作：为素材表增加失败字段，并以固定 SQL 将历史 `READY` 迁移为 `DRAFT`。
    - 验收点：字段顺序、类型和迁移 SQL 与 RUNBOOK 完全一致；不修改 Schema JSON；脚本测试验证 `READY→DRAFT`。
    - 重要度：10/10

- [ ] `07 graph 删除变更领域存储`：建立删除变更的乐观锁模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-BACKEND.md`
    - 范围对象：`kuzhambu-knowledge-domain/.../GraphMaterialDeletionChange.java`、其状态/决策 enum、value object、repository，`kuzhambu-knowledge-infra/.../GraphMaterialDeletionChangeDO.java`、Mapper、PersistenceAssembler、RepositoryImpl，及存储测试（10 个文件）
    - 处理动作：建立 deletion change 的领域、持久化和以 `lockVersion` 更新的仓储边界。
    - 验收点：entity、DO、mapper 均使用 `lockVersion`；版本不一致返回 `GRAPH_LOCK_CONFLICT`；mapper 不跨表编排。
    - 重要度：10/10

- [ ] `08 graph 删除任务领域存储`：建立删除任务的幂等与乐观锁模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-BACKEND.md`
    - 范围对象：`kuzhambu-knowledge-domain/.../GraphMaterialDeletionTask.java`、其状态 enum、value object、repository，`kuzhambu-knowledge-infra/.../GraphMaterialDeletionTaskDO.java`、Mapper、PersistenceAssembler、RepositoryImpl，及存储测试（10 个文件）
    - 处理动作：建立 deletion task 的领域、持久化、幂等键和以 `lockVersion` 更新的仓储边界。
    - 验收点：`idempotency_key` 唯一；重试和状态更新均做版本校验；版本不一致返回 `GRAPH_LOCK_CONFLICT`。
    - 重要度：10/10

- [ ] `09 graph 删除表重建 DDL`：安全重建删除变更和任务表
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-BACKEND.md`
    - 范围对象：`db/schema/knowledge.sql`、删除表重建迁移脚本、迁移前置检查测试（3 个文件）
    - 处理动作：按零行数前置检查、固定 drop 顺序和新 DDL 重建两张删除表。
    - 验收点：任一旧表非零行即停止；新表含 `lock_version bigint NOT NULL DEFAULT 0`；不执行存量删除数据迁移。
    - 重要度：10/10

- [ ] `10 graph 删除预检与决策`：实现素材删除影响确认
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-BACKEND.md`、`docs/20-interfaces/KNOWLEDGE-GRAPH-INTERFACE.md`
    - 范围对象：`kuzhambu-knowledge-application/.../GraphMaterialDeletionApplicationService.java`、其 impl、precheck/decision command、change query、service test（6 个文件）
    - 处理动作：实现预检、保留贡献或撤回关联的决策，并通过 change 乐观锁提交。
    - 验收点：先写 `source_snapshot_json` 再清空草稿引用；撤回只改变当前素材 ACTIVE mapping；决策冲突返回 `GRAPH_LOCK_CONFLICT`。
    - 重要度：10/10

- [ ] `11 graph 删除任务执行与重试`：实现删除后台任务闭环
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-BACKEND.md`
    - 范围对象：`kuzhambu-knowledge-application/.../GraphMaterialDeletionApplicationServiceImpl.java`、task page/get/retry query-command、任务 processor、service test（6 个文件）
    - 处理动作：实现幂等投递、执行前重读状态、任务查询、失败恢复和重试。
    - 验收点：重复投递不新增任务；任务逐项重读状态；失败后可恢复；保留贡献与撤回关联的五项服务测试通过；提交后按 RUNBOOK 回复并 resolve PR #248 的 deletion thread。
    - 重要度：10/10

- [ ] `12 system audit facade 契约`：建立知识域可依赖的系统审计门面
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-BACKEND.md`、`docs/00-governance/SERVERS-ARCHITECTURE.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-facade/pom.xml`、`SystemAuditFacade.java`、`SystemAuditFacadeRequest.java`、`SystemAuditFacadeResponse.java`、`kuzhambu-servers/biz/system/pom.xml`（5 个文件）
    - 处理动作：新增 system facade 模块并将其加入 system reactor。
    - 验收点：facade 仅声明 `record()` 与 `get()` 所需协议；模块可由 knowledge application 依赖；不暴露 System domain 或 infra 类型。
    - 重要度：10/10

- [ ] `13 system audit facade 实现`：实现系统审计门面的记录与查询
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-BACKEND.md`
    - 范围对象：`kuzhambu-system-application/.../SystemAuditFacadeImpl.java`、其单测、`kuzhambu-knowledge-application/pom.xml`（3 个文件）
    - 处理动作：实现 facade 的 `record()` 和 `get()`，并把 knowledge application 改为仅依赖该 facade。
    - 验收点：`get(auditLogId)` 返回操作者与发生时间；knowledge application 无 System application/domain/infra 导入；模块测试通过。
    - 重要度：10/10

- [ ] `14 graph 治理审计存储`：保存治理理由、快照与审计引用
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-BACKEND.md`、`docs/20-interfaces/KNOWLEDGE-GRAPH-INTERFACE.md`
    - 范围对象：`GraphGovernanceOperation.java`、`GraphManualSource.java`、两个 repository、两个 DO、两个 mapper、审计持久化测试（9 个文件）
    - 处理动作：建立治理操作和人工来源的审计引用持久化，属性统一为 `auditLogId`。
    - 验收点：仅保存 `auditLogId`，不复制操作者字段；两个 mapper 不跨表编排；持久化测试覆盖理由、前后快照和审计引用。
    - 重要度：10/10

- [ ] `15 graph 治理审计写入`：在发布空间变更中记录审计操作
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-BACKEND.md`、`docs/20-interfaces/KNOWLEDGE-GRAPH-INTERFACE.md`
    - 范围对象：`GraphPublishedApplicationServiceImpl.java`、`GraphPublishedNodeDetailResult.java`、`GraphPublishedEdgeDetailResult.java`、application assembler、service test（5 个文件）
    - 处理动作：在发布节点和边的 create、update、delete、merge、split 中记录审计并经 facade 填充详情。
    - 验收点：每次治理操作都有理由、前后快照和 `auditLogId`；详情由 facade 查询操作者；测试覆盖五种写入动作。
    - 重要度：10/10

- [ ] `16 graph 治理审计 DDL`：重建审计引用字段与索引
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-BACKEND.md`
    - 范围对象：`db/schema/knowledge.sql`、审计表迁移脚本、DDL 结构测试（3 个文件）
    - 处理动作：在 governance operation 和 manual source 表加入非空 `audit_log_id` 与固定索引。
    - 验收点：字段位置、非空约束与索引名符合 RUNBOOK；不在 knowledge 表增加操作者 ID。
    - 重要度：9/10

- [ ] `17 graph 发布预览令牌存储`：持久化预览快照与原子消费条件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-BACKEND.md`
    - 范围对象：`GraphPublicationPreviewToken.java`、Repository、`GraphPublicationPreviewTokenDO.java`、Mapper、PersistenceAssembler、RepositoryImpl、`db/schema/knowledge.sql`、token repository test（8 个文件）
    - 处理动作：保存素材版本、发布对象 ID/版本、过期时间和消费状态的完整发布预览快照。
    - 验收点：`snapshot_json` 含所有受影响对象的 ID、`lockVersion` 和关联集合；过期或已消费返回 `GRAPH_PREVIEW_STALE`；消费更新原子化。
    - 重要度：10/10

- [ ] `18 graph 发布确认冲突决策`：绑定单素材决策并校验版本
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-BACKEND.md`、`docs/20-interfaces/KNOWLEDGE-GRAPH-INTERFACE.md`
    - 范围对象：`GraphPublicationCommand.java`、`GraphPublicationConflictDecision.java`、`GraphPublicationExecutor.java`、`GraphPublicationApplicationServiceImpl.java`、`GraphPublicationResult.java`、service test（6 个文件）
    - 处理动作：将每对象冲突决策绑定未过期 preview token，并在确认时校验全部版本。
    - 验收点：仅 `CONFLICT` 对象可有对应决策；决策与 token 不符或任一版本变化均返回 `GRAPH_PREVIEW_STALE`；写入 `conflict_decisions_json` 后消费 token；提交并验证后按 RUNBOOK 回复并 resolve PR #248/#249 的三个 publication thread。
    - 重要度：10/10

- [ ] `19 graph 批量发布独立执行`：保持批量预览、确认和结果输入顺序
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-BACKEND.md`、`docs/20-interfaces/KNOWLEDGE-GRAPH-INTERFACE.md`
    - 范围对象：`GraphBatchPublicationPreviewQuery.java`、`GraphBatchPublicationCommand.java`、batch preview/result 模型、`GraphPublicationApplicationServiceImpl.java`、batch service test（6 个文件）
    - 处理动作：按请求顺序独立预览和确认每份素材，不建立跨素材事务。
    - 验收点：结果严格按输入顺序；单项失败不短路或回滚其他素材；测试覆盖部分失败和每份素材独立 token。
    - 重要度：10/10

- [ ] `20 graph 治理影响令牌存储`：持久化治理预览影响快照
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-BACKEND.md`
    - 范围对象：`GraphGovernanceImpactToken.java`、Repository、`GraphGovernanceImpactTokenDO.java`、Mapper、PersistenceAssembler、RepositoryImpl、`db/schema/knowledge.sql`、token repository test（8 个文件）
    - 处理动作：保存删除、合并、拆分预览所涉节点、边、mapping 和对象版本的完整快照。
    - 验收点：token 过期或已消费返回 `GRAPH_PREVIEW_STALE`；快照不是 hash 或仅 ID；DDL 类型与索引不变。
    - 重要度：10/10

- [ ] `21 graph 治理确认令牌校验`：拒绝预览后变化的删除、合并和拆分
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-BACKEND.md`、`docs/20-interfaces/KNOWLEDGE-GRAPH-INTERFACE.md`
    - 范围对象：四个 GraphPublished * Delete/Merge/Split command、`GraphPublishedApplicationServiceImpl.java`、`GraphGovernanceImpactResult.java`、service test（7 个文件）
    - 处理动作：为四个确认 command 加入 `impactToken`，确认前逐项比对 token 快照。
    - 验收点：节点级联删除只删 preview 中的边；预览后新增 incident edge 时返回 `GRAPH_PREVIEW_STALE` 且不写数据；合并和拆分映射完整性受测试覆盖；提交并验证后按 RUNBOOK 回复并 resolve PR #248 的 impact-token thread。
    - 重要度：10/10

- [ ] `22 graph 工作台活动读模型`：补齐近期活动与待处理冲突统计
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-BACKEND.md`、`docs/20-interfaces/KNOWLEDGE-GRAPH-INTERFACE.md`
    - 范围对象：`GraphWorkbenchOverviewResult.java`、`GraphWorkbenchRepository.java`、`GraphWorkbenchRepositoryImpl.java`、`GraphWorkbenchMapper.java`、repository test（5 个文件）
    - 处理动作：由 repository 提供 `recentActivities` 与 `pendingConflictCount` 的 read model 和 SQL。
    - 验收点：controller 不拼装或伪造字段；统计与活动查询受测试覆盖；结果匹配工作台接口结构。
    - 重要度：8/10

- [ ] `23 graph 旧写入口下线与迁移核对`：阻断旧接口写入并验证迁移完整性
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-BACKEND.md`、`docs/20-interfaces/KNOWLEDGE-GRAPH-INTERFACE.md`、`docs/00-governance/TODO-RULES.md`
    - 范围对象：旧 `graph-extraction`、graph version、`refinement` 写入口（最多 6 个文件）、`scripts/verify-graph-migration.sh`、迁移脚本测试（8 个文件）
    - 处理动作：停止旧接口对正式图谱的写入，并以固定脚本按 `SANCAI_ENTRY` 核对迁移前后数量。
    - 验收点：旧表仅可读迁移；新旧接口不同时写正式图谱；未定义映射时脚本非零退出且不自动发布；脚本遵守 Prepare/Execute/Assert/Restore。
    - 重要度：10/10

- [ ] `24 graph backend 现场清理`：清理已完成任务、RUNBOOK 与临时执行痕迹
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-BACKEND.md`、`docs/00-governance/TODO-RULES.md`、`docs/00-governance/DOCUMENT-RULES.md`
    - 范围对象：`TODO.md`、`docs/30-designs/RUNBOOK-GRAPH-BACKEND.md`，以及本任务创建的临时迁移验证文件（2–10 个文件）
    - 处理动作：在全部交付、迁移核对和 PR 验证记录完成后删除本组 TODO、已完成 RUNBOOK 与临时文件。
    - 验收点：`TODO.md` 不保留完成任务或历史；RUNBOOK 及残留引用已删除；PR 描述已记录接口、迁移和验证结果；工作区无本任务临时文件。
    - 重要度：10/10

## 待讨论项
