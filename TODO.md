# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `AI-WORKERS T12A`：收口 AI + Workers 关键测试
  - 任务类型：执行任务
  - 依据文档：`docs/30-designs/RUNBOOK-AI-WORKERS-CLOSURE.md#T12`
  - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/discovery/service/impl/DiscoveryAiApplicationServiceImplTest.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/knowledge/service/impl/KnowledgeAiExtractionApplicationServiceImplTest.java`、`kuzhambu-workers/tests/test_ai_routes.py`、`kuzhambu-workers/tests/test_ai_usecase_routes.py`、`kuzhambu-workers/tests/test_artifact_store.py`、`kuzhambu-workers/tests/test_artifact_cleanup_job.py`
  - 处理动作：为关键协议、artifact 生命周期、消费闭环补齐测试。
  - 验收点：关键协议、artifact 生命周期、消费闭环都有测试。
  - 重要度：8/10

- [ ] `AI-WORKERS T12B`：收口 AI + Workers 设计文档与覆盖文档
  - 任务类型：执行任务
  - 依据文档：`docs/30-designs/RUNBOOK-AI-WORKERS-CLOSURE.md#T12`
  - 范围对象：`docs/30-designs/AI-DESIGN.md`、`docs/30-designs/WORKERS-DESIGN.md`、`docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`、`docs/40-readiness/WORKERS-IMPLEMENTATION-COVERAGE.md`
  - 处理动作：同步 AI / Workers 设计文档与实现覆盖文档。
  - 验收点：设计文档与实现覆盖文档口径一致。
  - 重要度：8/10

## 待讨论项
