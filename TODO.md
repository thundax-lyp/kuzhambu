# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 本清单按 `docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-SERVERS.md` 的 DAG 依赖拆分；每项对应 RUNBOOK 的一个提交单元和一个独立提交。
- 未经本轮人工确认的执行任务均在“待审阅任务项”；确认后才移入“当前任务项”执行。
- 已完成任务必须在其完成 commit 中删除；完成历史保留在 commit 和 PR。

## 当前任务项

## 待审阅任务项

- [ ] `knowledge graph task persistence`：实现图谱提取任务持久化
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-SERVERS.md` S1b
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/**/graph/model/`、`repository/`、`kuzhambu-knowledge-infra/src/main/java/**/graph/persistence/`、`repository/`
    - 处理动作：实现任务、阶段和统计快照的领域对象、DO、mapper、assembler 与 repository。
    - 验收点：集成测试覆盖并发互斥、按素材/批次/到期查询和乐观锁更新。
    - 重要度：10/10

- [ ] `knowledge graph task state machine`：实现提取任务状态机
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-SERVERS.md` S2
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/**/graph/`、`kuzhambu-knowledge-application/src/main/java/**/graph/command/`、`result/`
    - 处理动作：实现重试、取消、候选处置、重生成和幂等状态转换。
    - 验收点：领域测试覆盖合法/非法转换、版本冲突、重复请求和候选重复应用。
    - 重要度：10/10

- [ ] `classics graph material facade`：提供图谱素材来源 Facade
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-SERVERS.md` S3a
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-facade/`、`kuzhambu-classics-application/src/main/java/**/facade/`
    - 处理动作：提供按当前主体读取的稿件分页、可用性校验和正文快照。
    - 验收点：Facade 测试证明分页与可见性由 Classics 完成，且不泄露内部实体。
    - 重要度：10/10

- [ ] `ai graph candidate facade`：提供图谱候选协作 Facade
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-SERVERS.md` S3b
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-facade/`、`kuzhambu-ai-application/src/main/java/**/facade/`
    - 处理动作：提供冻结快照的候选读取、采用标记、拒绝和到期清理协作。
    - 验收点：Facade 测试覆盖冻结入参、候选不可用、采用/拒绝和清理失败，Knowledge 无 AI 表访问。
    - 重要度：9/10

- [ ] `knowledge graph material composite query`：实现素材复合查询
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-SERVERS.md` S4a
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/**/graph/GraphMaterial*`、`GraphMaterialListQuery.java`、`GraphMaterialQuery.java`、`GraphApplicationAssembler.java`
    - 处理动作：实现 Classics 来源叠加素材、统计和最新任务的分页及详情查询。
    - 验收点：应用测试覆盖未初始化素材、来源不可见、详情校验和无逐行明细聚合。
    - 重要度：10/10

- [ ] `knowledge graph task application`：实现图谱提取任务应用服务
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-SERVERS.md` S4b
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/**/graph/GraphExtraction*`、`GraphTask*`、`GraphTaskCandidateResolver.java`
    - 处理动作：实现提取、任务查询、状态动作和候选处置编排。
    - 验收点：应用测试覆盖批量部分失败、活动任务冲突、候选不可用、分组和输入顺序。
    - 重要度：10/10

- [ ] `knowledge graph batch withdrawal application`：实现图谱批量撤回
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-SERVERS.md` S4c
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/**/graph/GraphPublication*`、`GraphBatchWithdrawal*`
    - 处理动作：实现多素材独立的撤回预览与执行编排。
    - 验收点：应用测试证明无跨素材事务、逐项版本校验和稳定的部分成功结果。
    - 重要度：8/10

- [ ] `knowledge graph material task HTTP`：暴露素材任务 HTTP 接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-SERVERS.md` S5a、`docs/20-interfaces/KNOWLEDGE-GRAPH-INTERFACE.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/**/interfaces/admin/graph/GraphController.java`、`GraphMaterialRequests.java`、`GraphExtractionRequests.java`、`GraphMaterialResponses.java`、`GraphExtractionResponses.java`、`GraphInterfaceAssembler.java`
    - 处理动作：实现素材、提取任务和候选处置资源的 HTTP 转换与权限控制。
    - 验收点：Web 测试覆盖权限、字符串序列化、状态/版本冲突、候选不可用和批量部分失败。
    - 重要度：10/10

- [ ] `knowledge graph batch withdrawal HTTP`：暴露批量撤回 HTTP 接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-SERVERS.md` S5b、`docs/20-interfaces/KNOWLEDGE-GRAPH-INTERFACE.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/**/interfaces/admin/graph/GraphController.java`、`GraphPublicationRequests.java`、`GraphPublicationResponses.java`、`GraphInterfaceAssembler.java`
    - 处理动作：实现批量撤回预览与执行的 HTTP 转换与权限控制。
    - 验收点：Web 测试覆盖逐项结果、输入顺序、乐观锁冲突和部分失败。
    - 重要度：8/10

- [ ] `knowledge graph material stats`：刷新素材统计快照
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-SERVERS.md` S6a
    - 范围对象：`GraphMaterialStatsRefresher.java`、统计 repository/mapper、`GraphExtractionApplicationServiceImpl.java`、`GraphMaterialApplicationServiceImpl.java`、`GraphPublicationApplicationServiceImpl.java`
    - 处理动作：在图谱变更后刷新对应素材的统计快照。
    - 验收点：集成测试证明列表读模型正确更新且不逐行聚合明细。
    - 重要度：9/10

- [ ] `knowledge graph task cleanup`：清理到期图谱提取任务
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-SERVERS.md` S6b
    - 范围对象：`GraphExtractionTaskCleanupScheduler.java`、`GraphExtractionTaskRepositoryImpl.java`、`AiFacade.java`
    - 处理动作：协调 AI 清理并删除处置满七天的 Knowledge 任务与阶段。
    - 验收点：时钟可控测试覆盖边界、清理失败重试及草稿/发布/统计保留。
    - 重要度：9/10

- [ ] `knowledge graph migration verification`：提交迁移结果对比证据
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-SERVERS.md` S6c
    - 范围对象：`docs/40-readiness/KNOWLEDGE-GRAPH-MATERIAL-TASK-BASELINE.md`
    - 处理动作：补充迁移后统计、差异结论和验证命令结果。
    - 验收点：文档可核对迁移前后统计，且包含已执行验证的结果。
    - 重要度：9/10

- [ ] `knowledge graph legacy extraction cleanup`：删除旧图谱提取写路径
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-SERVERS.md` S6d
    - 范围对象：旧 `graph-extraction`、`graph-result`、`refinement` Controller/service/test 与 `db/schema/knowledge.sql` 兼容字段。
    - 处理动作：删除已无调用方的旧图谱提取写入口及兼容结构。
    - 验收点：S6c 证据已提交，`rg` 证明旧入口无调用方，迁移验证、相关 Maven 测试和静态检查通过。
    - 重要度：7/10

## 待讨论项
