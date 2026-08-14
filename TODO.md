# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 当前任务项按数字编号顺序执行；不得跳过前置契约任务直接做后续调用方或重命名任务。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

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
