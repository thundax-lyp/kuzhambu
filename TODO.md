# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 当前任务项按数字编号顺序执行；不得跳过前置契约任务直接做后续调用方或重命名任务。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `素材仓储端口和实现`：补齐 GraphMaterial 及草稿节点边仓储能力
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-APPLICATION-SERVICES.md`
    - 范围对象：`GraphMaterialRepository.java`、`GraphMaterialNodeRepository.java`、`GraphMaterialEdgeRepository.java`、对应 RepositoryImpl、Mapper 和测试（预计 8-10 个文件）
    - 处理动作：新增分页、强类型自增 ID 回填、批量写入、批量删除和 Material CAS 更新。
    - 验收点：Material CAS 冲突返回业务错误，节点和边 insert 返回真实数据库 ID。
    - 重要度：10/10

- [ ] `素材版本和事件仓储`：补齐版本与 MaterialEvent 仓储能力
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-APPLICATION-SERVICES.md`
    - 范围对象：`GraphMaterialVersionRepository.java`、`GraphMaterialEventRepository.java`、对应 RepositoryImpl、Mapper 和测试（预计 6-8 个文件）
    - 处理动作：新增版本号查询、按素材删除、事件分页、领取查询、插入和事件 CAS 更新。
    - 验收点：事件领取使用数据库 CAS，重复事件按 `ContentRef + type` 幂等处理。
    - 重要度：9/10

- [ ] `发布节点仓储`：补齐 GraphPublishedNode 仓储能力
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-APPLICATION-SERVICES.md`
    - 范围对象：`GraphPublishedNodeRepository.java`、`GraphPublishedNodeRepositoryImpl.java`、`GraphPublishedNodeMapper.java`、`GraphPersistenceAssembler.java`、`GraphPublishedNodeRepositoryImplTest.java`（预计 5 个文件）
    - 处理动作：新增条件分页、插入 ID 回填、CAS 更新、状态计数和最近 ACTIVE 节点查询。
    - 验收点：发布节点分页稳定按 `modified_at DESC, id DESC` 返回。
    - 重要度：9/10

- [ ] `发布边仓储`：补齐 GraphPublishedEdge 仓储和 incident edges 游标查询
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-APPLICATION-SERVICES.md`
    - 范围对象：`GraphPublishedEdgeRepository.java`、`GraphPublishedEdgeRepositoryImpl.java`、`GraphPublishedEdgeMapper.java`、`GraphPersistenceAssembler.java`、`GraphPublishedEdgeRepositoryImplTest.java`、`db/schema/knowledge.sql`（预计 6 个文件）
    - 处理动作：新增条件分页、插入 ID 回填、CAS 更新、状态计数和数据库层 incident edges 稳定游标读取。
    - 验收点：incident edges 使用数据库 `limit + 1` 截断，不先全量加载再 Java 分页。
    - 重要度：10/10

- [ ] `发布属性仓储`：补齐发布节点/边属性批量持久化和删除
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-APPLICATION-SERVICES.md`
    - 范围对象：节点属性和边属性 Repository、RepositoryImpl、Mapper、测试（预计 6-8 个文件）
    - 处理动作：新增 insert、batchInsert、按发布对象删除和按发布对象集合删除能力。
    - 验收点：唯一约束重复插入按幂等处理，其他数据库异常不会被吞掉。
    - 重要度：8/10

- [ ] `发布素材映射仓储`：补齐发布节点/边素材映射批量持久化和删除
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-APPLICATION-SERVICES.md`
    - 范围对象：节点素材映射和边素材映射 Repository、RepositoryImpl、Mapper、测试（预计 6-8 个文件）
    - 处理动作：新增批量插入、按发布对象删除、按素材删除和 distinct 素材计数。
    - 验收点：撤回或素材删除能只清理当前素材映射，不删除发布对象。
    - 重要度：9/10

- [ ] `Workbench 读仓储`：新增图谱工作台统计、搜索和质量快照查询
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-APPLICATION-SERVICES.md`
    - 范围对象：`GraphWorkbenchMetrics.java`、`GraphPublishedSearchHit.java`、`GraphQualitySnapshot.java`、`GraphWorkbenchRepository.java`、`GraphWorkbenchRepositoryImpl.java`、`GraphWorkbenchMapper.java`、Mapper XML、测试（预计 8 个文件）
    - 处理动作：把 overview、search 和 quality 查询下沉到 Repository/SQL 层。
    - 验收点：overview 不统计未发布或已删除对象，search 不做内存分页。
    - 重要度：8/10

- [ ] `JSON Schema 装配`：打包并编译知识图谱 JSON Schema
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-APPLICATION-SERVICES.md`
    - 范围对象：`kuzhambu-servers/pom.xml`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/pom.xml`、`GraphSchemaProvider.java`、`GraphSchemaProviderTest.java`（预计 4 个文件）
    - 处理动作：引入 `json-schema-validator` 并在构建时复制 `KNOWLEDGE-GRAPH-SCHEMA.json` 到 classpath。
    - 验收点：合法文档通过校验，缺少边端点的非法文档被拒绝。
    - 重要度：9/10

- [ ] `图谱文档解析和 Schema 支持`：新增 GraphDocument、SnapshotSupport 和 SchemaSupport
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-APPLICATION-SERVICES.md`
    - 范围对象：`GraphDocument.java`、`GraphDocumentNode.java`、`GraphDocumentEdge.java`、`GraphSnapshotSupport.java`、`GraphSchemaSupport.java`、对应测试（预计 6 个文件）
    - 处理动作：实现导入、Candidate、版本快照解析和草稿图 JSON 序列化。
    - 验收点：JSON 文档内 String ID 不会被当作数据库 ID 使用。
    - 重要度：10/10

- [ ] `图谱文档规划器`：新增导入和 AI MERGE/REPLACE 规划
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-APPLICATION-SERVICES.md`
    - 范围对象：`GraphDocumentPlan.java`、`GraphDocumentPlanner.java`、`GraphDocumentPlannerTest.java`（预计 3 个文件）
    - 处理动作：根据当前草稿图和 GraphDocument 生成创建、更新、删除和引用映射计划。
    - 验收点：Preview 不伪造 `GraphMaterialNodeId`，持久化前能保留文档节点引用映射。
    - 重要度：10/10

- [ ] `图谱素材加载保存支持`：新增 ContentResolver、GraphLoader 和 GraphSaver
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-APPLICATION-SERVICES.md`
    - 范围对象：`GraphMaterialContentResolver.java`、`GraphMaterialContentSnapshot.java`、`GraphMaterialGraphLoader.java`、`GraphMaterialGraphSaver.java`、对应测试（预计 5 个文件）
    - 处理动作：封装 Classics Facade 快照读取、素材图加载创建和按固定顺序保存。
    - 验收点：Saver 先落节点取得真实 ID 后再构造并落边，最后 CAS 更新 Material。
    - 重要度：10/10

- [ ] `图谱应用层组装器`：新增 GraphApplicationAssembler
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-APPLICATION-SERVICES.md`
    - 范围对象：`GraphApplicationAssembler.java`、`GraphApplicationAssemblerTest.java`（预计 2 个文件）
    - 处理动作：集中实现领域对象、发布规划、AI 任务和 Workbench 读模型到 Result 的无状态转换。
    - 验收点：ApplicationServiceImpl 不重复拼装 Result 细节。
    - 重要度：7/10

- [ ] `素材草稿应用服务`：实现 GraphMaterialApplicationServiceImpl
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-APPLICATION-SERVICES.md`
    - 范围对象：`GraphMaterialApplicationServiceImpl.java`、`GraphMaterialApplicationServiceImplTest.java`、必要的测试 fixture（预计 2-4 个文件）
    - 处理动作：实现素材分页、草稿图 CRUD、合并/拆分、版本恢复、导入预览、导入和导出。
    - 验收点：接口所有方法都有实现，`PUBLISHED` 状态写入、跨素材 ID 和 CAS 冲突被拒绝。
    - 重要度：10/10

- [ ] `图谱抽取应用编排`：实现 GraphExtractionApplicationServiceImpl 和 ApplyExecutor
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-APPLICATION-SERVICES.md`
    - 范围对象：`GraphExtractionApplicationServiceImpl.java`、`GraphExtractionApplyExecutor.java`、`GraphExtractionApplicationServiceImplTest.java`、`GraphExtractionApplyExecutorTest.java`（预计 4 个文件）
    - 处理动作：实现抽取提交、重试、当前任务、历史分页和 Candidate 应用。
    - 验收点：Candidate 重复应用通过 Key 幂等，不重复创建草稿节点和边。
    - 重要度：10/10

- [ ] `图谱发布执行器`：实现单素材发布和撤回事务入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-APPLICATION-SERVICES.md`
    - 范围对象：`GraphPublicationExecutor.java`、`GraphPublicationExecutorTest.java`、发布原子性集成测试（预计 2-3 个文件）
    - 处理动作：按固定顺序创建/复用发布对象、写入属性和映射、创建版本并 CAS 更新 Material。
    - 验收点：发布任一步失败时节点、边、属性、映射、版本和 Material 状态全部回滚。
    - 重要度：10/10

- [ ] `图谱发布应用服务`：实现 GraphPublicationApplicationServiceImpl
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-APPLICATION-SERVICES.md`
    - 范围对象：`GraphPublicationApplicationServiceImpl.java`、`GraphPublicationApplicationServiceImplTest.java`、批量发布测试（预计 2-3 个文件）
    - 处理动作：实现发布预览、批量预览、发布、批量发布、撤回预览和撤回。
    - 验收点：批量发布单项失败不回滚其他成功项，也不阻止后续素材继续执行。
    - 重要度：10/10

- [ ] `发布空间治理应用服务`：实现 GraphPublishedApplicationServiceImpl
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-APPLICATION-SERVICES.md`
    - 范围对象：`GraphPublishedApplicationServiceImpl.java`、`GraphPublishedApplicationServiceImplTest.java`、必要测试 fixture（预计 2-4 个文件）
    - 处理动作：实现发布节点/边分页、详情、创建、更新、删除预览、删除、合并预览、合并、拆分预览和拆分。
    - 验收点：所有受影响发布对象使用 CAS，治理操作不修改素材草稿和历史版本。
    - 重要度：10/10

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
