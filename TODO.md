# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 本清单按 `docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-SERVERS.md` 的 DAG 依赖拆分；每项对应 RUNBOOK 的一个提交单元和一个独立提交。
- 未经本轮人工确认的执行任务均在“待审阅任务项”；确认后才移入“当前任务项”执行。
- 已完成任务必须在其完成 commit 中删除；完成历史保留在 commit 和 PR。

## 当前任务项

## 待审阅任务项

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
