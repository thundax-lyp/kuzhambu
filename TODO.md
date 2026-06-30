# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `AI-WORKERS T7B`：收口大文件 multipart 分片上传与完成
  - 任务类型：执行任务
  - 依据文档：`docs/30-designs/RUNBOOK-AI-WORKERS-CLOSURE.md#T7B`
  - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/MultipartUploadApplicationService.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-facade/src/main/java/com/thundax/kuzhambu/storage/facade/request/UploadMultipartPartFacadeRequest.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-facade/src/main/java/com/thundax/kuzhambu/storage/facade/request/CompleteMultipartUploadFacadeRequest.java`、`docs/30-designs/STORAGE-DESIGN.md`
  - 处理动作：完成 multipart 分片上传、完成提交与设计文档口径同步。
  - 验收点：大文件不会因单次内存装载或普通上传路径导致实现不可行。
  - 重要度：10/10

- [ ] `AI-WORKERS T8`：收口 Workers 临时 artifact 清理任务
  - 任务类型：执行任务
  - 依据文档：`docs/30-designs/RUNBOOK-AI-WORKERS-CLOSURE.md#T8`
  - 范围对象：`kuzhambu-workers/src/kuzhambu_workers/render/artifact_store.py`、`kuzhambu-workers/src/kuzhambu_workers/core/config.py`、`kuzhambu-workers/src/kuzhambu_workers/main.py`、`kuzhambu-workers/tests/test_artifact_store.py`、`kuzhambu-workers/tests/test_artifact_cleanup_job.py`
  - 处理动作：增加定时清理任务并清理超过 `12` 小时的 artifact。
  - 验收点：超时文件会被清理，未超时文件不会误删。
  - 重要度：10/10

- [ ] `AI-WORKERS T9`：收口文件类结果失败边界
  - 任务类型：执行任务
  - 依据文档：`docs/30-designs/RUNBOOK-AI-WORKERS-CLOSURE.md#T9`
  - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/service/impl/AiWorkerInvocationApplicationServiceImpl.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/client/WorkerAiHttpClient.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-facade/src/main/java/com/thundax/kuzhambu/storage/facade/StorageFacade.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-facade/src/main/java/com/thundax/kuzhambu/storage/facade/request/UploadStorageFacadeRequest.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/invocation/service/impl/AiWorkerInvocationApplicationServiceImplTest.java`
  - 处理动作：固定 `ARTIFACT_DOWNLOAD`、`STORAGE_PERSIST` 并在失败时保留调用记录与 artifact reference 摘要。
  - 验收点：能区分 worker 生成失败、artifact 下载失败、Storage 转存失败。
  - 重要度：10/10

- [ ] `AI-WORKERS T10`：收口 Knowledge AI 候选闭环
  - 任务类型：执行任务
  - 依据文档：`docs/30-designs/RUNBOOK-AI-WORKERS-CLOSURE.md#T10`
  - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/knowledge/service/impl/KnowledgeAiExtractionApplicationServiceImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/service/impl/KnowledgeGraphExtractionApplicationServiceImpl.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/dto/AiCandidateFacadeDto.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/support/KnowledgeGraphCandidateApplySupport.java`
  - 处理动作：保证 Knowledge 只消费候选并让抽取任务、候选、正式图谱应用三者可追溯。
  - 验收点：Knowledge 图谱提取任务可追到 AI `callId/candidateId`。
  - 重要度：8/10

- [ ] `AI-WORKERS T11`：收口 Discovery AI 消费闭环
  - 任务类型：执行任务
  - 依据文档：`docs/30-designs/RUNBOOK-AI-WORKERS-CLOSURE.md#T11`
  - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/discovery/service/impl/DiscoveryAiApplicationServiceImpl.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImpl.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/QueryUnderstandingApplicationServiceImpl.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaRetrievalTrace.java`
  - 处理动作：统一 Discovery 同步与流式调用最终结果消费口径并将最终 AI 调用标识稳定挂到 QA trace。
  - 验收点：Discovery 会话、消息、来源仍由 Discovery 保存，AI 域只输出最终回答结果与调用可追溯标识。
  - 重要度：8/10

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
