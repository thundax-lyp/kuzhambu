# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 本清单按 `docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-SERVERS.md` 的 DAG 依赖拆分；每项对应 RUNBOOK 的一个提交单元和一个独立提交。
- 未经本轮人工确认的执行任务均在“待审阅任务项”；确认后才移入“当前任务项”执行。
- 已完成任务必须在其完成 commit 中删除；完成历史保留在 commit 和 PR。

## 当前任务项

## 待审阅任务项

- [ ] `knowledge graph legacy extraction cleanup`：删除旧图谱提取写路径
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-SERVERS.md` S6d
    - 范围对象：旧 `graph-extraction`、`graph-result`、`refinement` Controller/service/test 与 `db/schema/knowledge.sql` 兼容字段。
    - 处理动作：删除已无调用方的旧图谱提取写入口及兼容结构。
    - 验收点：S6c 证据已提交，`rg` 证明旧入口无调用方，迁移验证、相关 Maven 测试和静态检查通过。
    - 重要度：7/10

## 待讨论项
