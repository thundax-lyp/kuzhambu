# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 本清单按图谱素材/任务的 Servers 与 Admin Web RUNBOOK 固定顺序拆分；每项对应一个可独立验证的小步提交。
- 未经本轮人工确认的执行任务均在“待审阅任务项”；确认后才移入“当前任务项”执行。
- 已完成任务必须在其完成 commit 中删除；完成历史保留在 commit 和 PR。

## 当前任务项

## 待审阅任务项

- [ ] `admin-web knowledge routes`：冻结图谱素材与任务页面边界
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/router/index.tsx`、`kuzhambu-apps/admin-web/src/layouts/admin-layout.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-workbench/`
    - 处理动作：保留素材管理和提取任务入口，并移除新流程到旧 `graph-result`、`refinement` 的跳转依赖。
    - 验收点：路由单测通过，且 `rg` 确认新素材/任务组件不 import 旧 `graph-result` 或 `refinement` service。
    - 重要度：8/10

- [ ] `AW/graph-material service + AW/graph-extraction service`：建立图谱素材任务服务契约与 Mock
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/`
    - 处理动作：定义固定领域类型、HTTP service、Mock adapter、fixture 和 service contract tests。
    - 验收点：contract tests 覆盖 URL、body、`idempotencyKey`、任务版本/预期状态、`ApiResponse` 解包，且 Mock 与 HTTP adapter 使用同一组 service 方法。
    - 重要度：10/10

- [ ] `AW/graph-material catalog`：重构图谱素材管理列表
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/graph-material-page.tsx`、`material-table/`、`material-filters/`、`material-batch-actions/`
    - 处理动作：用 TanStack Query 和 material service 替换页面内置素材数组，完成素材表格、筛选和批量动作。
    - 验收点：组件测试覆盖未初始化、统计过期、空列表、权限不足、批量部分失败和“查看任务”跳转。
    - 重要度：9/10

- [ ] `AW/graph-material drawer`：增加图谱素材分段详情抽屉
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/material-detail-drawer/`、`material-overview-panel/`、`material-draft-canvas/`、`material-task-summary-panel/`、`publication-preview/`
    - 处理动作：用 `KuzhambuSegmentedDrawer` 实现素材概览、草稿图谱、任务摘要、发布与变更四段详情。
    - 验收点：测试覆盖四段可访问、任务段无草稿编辑控件、已发布素材画布只读和详情加载错误可恢复。
    - 重要度：9/10

- [ ] `AW/graph-extraction queue`：重构图谱提取任务队列
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-page.tsx`、`graph-extraction-task-table/`、`task-filters/`、`task-batch-create-panel/`
    - 处理动作：用任务 service 替换旧提取工作台，完成 flat/grouped 查询、任务表格、筛选和批量创建面板。
    - 验收点：测试覆盖默认 flat、服务端 grouped、`batchId` 过滤、批量输入互斥、运行任务取消和失败任务重试可见性。
    - 重要度：9/10

- [ ] `AW/graph-extraction task drawer`：增加图谱任务候选处置抽屉
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-task-detail/`、`task-detail-drawer/`、`task-execution-panel/`、`task-candidate-panel/`、`task-disposition-panel/`
    - 处理动作：用 `KuzhambuSegmentedDrawer` 实现任务概览、执行过程、候选预览和处置动作。
    - 验收点：组件测试覆盖四段、四类候选 diff、候选不可用、各状态动作、版本冲突刷新和处置后按钮消失。
    - 重要度：9/10

- [ ] `AW knowledge graph real service`：接入图谱素材任务真实服务
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/*-service.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/*-service.ts`、`kuzhambu-apps/admin-web/e2e/knowledge-graph-material-task.spec.ts`
    - 处理动作：保持领域类型和组件不变，将 adapter 从 Mock 切换到 Knowledge HTTP service 并补充 E2E。
    - 验收点：Network 只请求 `/knowledge/graph/**`，真实或阻塞记录覆盖素材页、素材 drawer、单项提取、任务 drawer、失败重试、候选采用和批量部分失败。
    - 重要度：8/10

- [ ] `AW legacy graph extraction cleanup`：独立清理旧图谱提取组件和旧 service
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-manuscript-tree/`、`graph-extraction-manuscript-detail/`、`graph-workbench-service.ts`、`graph-extraction-candidate-modal.tsx`
    - 处理动作：在 W4/W5 停止引用后，单独删除无引用的旧组件和旧 service。
    - 验收点：`rg` 证明无引用，相关单测、lint 和 build 通过。
    - 重要度：7/10

### Servers

- [ ] `knowledge graph migration baseline`：冻结图谱素材任务迁移基线
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-SERVERS.md` S0
    - 范围对象：`db/schema/knowledge.sql`、`kuzhambu-servers/biz/knowledge/**/graph/`
    - 处理动作：记录迁移前素材、图对象、发布映射和旧抽取任务的数量、状态及旧路径调用清单。
    - 验收点：形成可供迁移测试比对的本地基准，且不修改数据库数据或生产代码。
    - 重要度：10/10

- [ ] `knowledge graph task schema`：扩展图谱提取任务与统计快照表
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-SERVERS.md` S1
    - 范围对象：`db/schema/knowledge.sql`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/test/java/**/graph/migration/`
    - 处理动作：新增素材统计快照表，并为提取任务增加双状态、版本、快照、关联和到期清理字段及索引。
    - 验收点：DDL 测试覆盖表、字段、唯一约束和索引，且现有 schema 导入保持可执行。
    - 重要度：10/10

- [ ] `knowledge graph task persistence`：建立图谱提取任务与统计快照持久化端口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-SERVERS.md` S1
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/**/graph/model/`、`repository/`、`kuzhambu-knowledge-infra/src/main/java/**/graph/persistence/`、`repository/`
    - 处理动作：实现任务、阶段和统计快照的领域对象、DO、mapper、assembler 与 repository，并提供活动任务互斥和乐观锁存取。
    - 验收点：repository 集成测试覆盖按素材/批次/到期查询、并发活动任务互斥和版本更新。
    - 重要度：10/10

- [ ] `knowledge graph task state machine`：实现提取任务双状态与幂等状态转换
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-SERVERS.md` S2
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/**/graph/`、`kuzhambu-knowledge-application/src/main/java/**/graph/command/`、`result/`
    - 处理动作：实现失败原地重试、取消、候选待审、采用、丢弃、替代、重生成及幂等键校验。
    - 验收点：领域测试覆盖全部合法/非法转换、版本冲突、重复请求和候选重复应用。
    - 重要度：10/10

- [ ] `classics graph material facade`：提供图谱素材来源分页与冻结快照 Facade
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-SERVERS.md` S3
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-facade/`、`kuzhambu-classics-application/src/main/java/**/facade/`
    - 处理动作：以当前主体为输入提供可图谱化稿件分页、可用性校验和内容快照读取 Facade。
    - 验收点：Facade 测试证明分页与可见性由 Classics 完成，且返回字段不泄露完整内部实体。
    - 重要度：10/10

- [ ] `ai graph candidate facade`：提供图谱候选查询与清理 Facade
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-SERVERS.md` S3
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-facade/`、`kuzhambu-ai-application/src/main/java/**/facade/`
    - 处理动作：提供已冻结抽取快照的候选/阶段读取和到期清理协作，不暴露 AI 内部表。
    - 验收点：Facade 测试覆盖冻结快照入参、候选不可用和清理失败响应，Knowledge 无 AI 表访问。
    - 重要度：9/10

- [ ] `knowledge graph material composite query`：实现素材复合分页与详情应用服务
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-SERVERS.md` S4
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/**/graph/GraphMaterial*`、`GraphApplicationAssembler.java`
    - 处理动作：先调用 Classics Facade 分页/校验，再按当页 `ContentRef` 批量覆盖素材、统计和最新任务读模型。
    - 验收点：应用测试覆盖未初始化素材、来源不可见、详情校验和无逐行明细聚合。
    - 重要度：10/10

- [ ] `knowledge graph task application`：实现单项与批量提取、任务队列和候选处置应用服务
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-SERVERS.md` S4
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/**/graph/GraphExtraction*`、`GraphTask*`、`GraphTaskCandidateResolver.java`
    - 处理动作：实现创建、批量创建、flat/grouped 查询、详情、重试、取消、候选采用/丢弃和重生成编排。
    - 验收点：应用测试覆盖批量部分失败、活动任务冲突、候选不可用、按素材分组和输入顺序保留。
    - 重要度：10/10

- [ ] `knowledge graph batch withdrawal application`：实现多素材独立撤回编排
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-SERVERS.md` S4
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/**/graph/GraphPublication*`、`GraphBatchWithdrawal*`
    - 处理动作：实现批量撤回预览和执行，逐素材校验版本并返回稳定顺序的部分成功结果。
    - 验收点：应用测试证明没有跨素材事务，任一素材失败不阻断其余素材。
    - 重要度：8/10

- [ ] `knowledge graph material task HTTP`：暴露素材与任务管理 HTTP 接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-SERVERS.md` S5、`docs/20-interfaces/KNOWLEDGE-GRAPH-INTERFACE.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/**/interfaces/admin/graph/GraphController.java`、`request/GraphMaterialRequests.java`、`GraphExtractionRequests.java`、`response/GraphMaterialResponses.java`、`GraphExtractionResponses.java`、`GraphInterfaceAssembler.java`
    - 处理动作：按固定契约实现素材、任务和候选处置资源的 request/response/assembler/controller。
    - 验收点：Web 测试覆盖权限、字符串 ID/时间序列化、状态冲突、版本冲突、候选不可用和批量部分失败。
    - 重要度：10/10

- [ ] `knowledge graph batch withdrawal HTTP`：暴露批量撤回 HTTP 接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-SERVERS.md` S5、`docs/20-interfaces/KNOWLEDGE-GRAPH-INTERFACE.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/**/interfaces/admin/graph/GraphController.java`、`GraphPublicationRequests.java`、`GraphPublicationResponses.java`、`GraphInterfaceAssembler.java`
    - 处理动作：暴露批量撤回预览和执行接口，并保持单素材撤回接口语义不变。
    - 验收点：Web 测试覆盖逐素材结果、输入顺序、乐观锁冲突和部分失败。
    - 重要度：8/10

- [ ] `knowledge graph material stats`：在图谱变更后刷新素材统计快照
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-SERVERS.md` S6
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/**/graph/GraphMaterialStatsRefresher.java`、`GraphExtractionApplicationServiceImpl.java`、`GraphMaterialApplicationServiceImpl.java`、`GraphPublicationApplicationServiceImpl.java`
    - 处理动作：在草稿、任务、候选、发布和撤回变化后刷新素材统计快照。
    - 验收点：集成测试证明列表读模型更新正确，且列表查询不聚合节点、边或任务明细。
    - 重要度：9/10

- [ ] `knowledge graph task cleanup`：实现处置终态任务的七天清理
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-SERVERS.md` S6
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/**/graph/scheduler/GraphExtractionTaskCleanupScheduler.java`、`GraphExtractionTaskRepositoryImpl.java`、`AiFacade.java`
    - 处理动作：定时协调 AI 候选清理并物理删除已处置满七天的 Knowledge 任务和阶段明细。
    - 验收点：时钟可控测试覆盖边界、AI 清理失败重试和草稿/发布/统计保留。
    - 重要度：9/10

- [ ] `knowledge graph legacy extraction cleanup`：独立删除旧图谱提取写路径
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-SERVERS.md` S6
    - 范围对象：旧 `graph-extraction`、`graph-result`、`refinement` Controller/service/test 与 `db/schema/knowledge.sql` 兼容字段。
    - 处理动作：在新接口和新调用方验证完成后删除旧写入口、兼容列和无引用测试。
    - 验收点：`rg` 证明旧入口无调用方，迁移验证、相关 Maven 测试和静态检查通过。
    - 重要度：7/10

## 待讨论项

- [ ] 真实 Knowledge HTTP service 是否已经完整可联调
    - 任务类型：待讨论项
    - 关联任务：`AW knowledge graph real service`
    - 决策要求：确认 Servers 是否已提供 `KNOWLEDGE-GRAPH-INTERFACE.md` 所需完整接口；如果未提供，W6 保留 Mock 并在 readiness 记录阻塞点。
    - 重要度：8/10
