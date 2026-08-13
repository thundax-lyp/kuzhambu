# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 当前任务项按数字编号顺序执行；不得跳过前置契约任务直接做后续调用方或重命名任务。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `素材删除事件应用服务`：实现 GraphMaterialEventApplicationServiceImpl
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-APPLICATION-SERVICES.md`
    - 范围对象：`GraphMaterialEventApplicationServiceImpl.java`、`GraphMaterialEventApplicationServiceImplTest.java`（预计 2 个文件）
    - 处理动作：实现事件记录、分页、重试和处理编排。
    - 验收点：只接受 `DELETED` 事件，重复投递返回原事件 ID。
    - 重要度：9/10

- [ ] `素材删除事件执行器和调度器`：实现事件领取、清理、失败记录和后台消费
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-APPLICATION-SERVICES.md`
    - 范围对象：`GraphMaterialEventClaimExecutor.java`、`GraphMaterialEventCleanupExecutor.java`、`GraphMaterialEventFailureRecorder.java`、`GraphMaterialEventScheduler.java`、对应测试（预计 8 个文件）
    - 处理动作：按 `claim → cleanup → failure recorder` 顺序实现异步幂等清理。
    - 验收点：单条事件失败不影响其余事件，多实例并发依赖 CAS 只有一个处理者成功领取。
    - 重要度：9/10

- [ ] `Portal 图谱读取应用服务`：实现 GraphPortalApplicationServiceImpl
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-APPLICATION-SERVICES.md`
    - 范围对象：`GraphPortalApplicationServiceImpl.java`、`GraphPortalApplicationServiceImplTest.java`（预计 2 个文件）
    - 处理动作：实现 Portal 只读发布空间的素材图查询。
    - 验收点：不可见、未发布、已撤回和已删除素材都返回空图。
    - 重要度：8/10

- [ ] `Workbench 图谱读取应用服务`：实现 GraphWorkbenchApplicationServiceImpl
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-APPLICATION-SERVICES.md`
    - 范围对象：`GraphWorkbenchApplicationServiceImpl.java`、`GraphWorkbenchApplicationServiceImplTest.java`（预计 2 个文件）
    - 处理动作：实现 overview、最近种子节点、incident edges、搜索和质量快照读取。
    - 验收点：种子节点最多 100、局部图节点最多 200，incident edges 通过 Repository 游标分页。
    - 重要度：8/10

- [ ] `图谱实现设计同步`：同步最终聚合边界、事务行为和仓储语义
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-APPLICATION-SERVICES.md`
    - 范围对象：`docs/30-designs/KNOWLEDGE-GRAPH-DESIGN.md`、必要的 `docs/40-readiness/*` 证据文档（预计 1-3 个文件）
    - 处理动作：把已实现的领域聚合、AI 提交契约、事务行为和 Repository 语义同步到正式文档。
    - 验收点：RUNBOOK 中仍有长期价值的结论已迁移到正式设计或 readiness 证据。
    - 重要度：8/10

- [ ] `AI 接口文档同步`：同步图谱抽取 AI Facade 契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-APPLICATION-SERVICES.md`
    - 范围对象：`docs/20-interfaces/AI-RUNTIME-INTERFACE.md`、必要关联接口文档（预计 1-2 个文件）
    - 处理动作：记录 `submitKnowledgeGraphExtraction` 请求字段、快照边界和 batch/candidate 追溯要求。
    - 验收点：接口文档与 AI Facade 和 Knowledge 调用方签名一致。
    - 重要度：8/10

- [ ] `图谱收口验证和 RUNBOOK 清理`：完成跨阶段验证并清理临时执行手册
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-APPLICATION-SERVICES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-APPLICATION-SERVICES.md`、`TODO.md`、PR 描述或 readiness 证据文档（预计 2-3 个文件）
    - 处理动作：完成 RUNBOOK Phase 12 检查、记录验证证据并删除已无剩余价值的 RUNBOOK。
    - 验收点：七个 Impl 无占位返回，禁止残留搜索通过，TODO 已收窄或清空。
    - 重要度：9/10

## 待讨论项
