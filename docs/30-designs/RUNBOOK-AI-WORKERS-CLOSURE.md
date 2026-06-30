# RUNBOOK AI + Workers Capability Closure

## Goal

完成 `AI + Workers` 能力闭环，使当前仓库满足以下最终目标：

- AI 域是所有 AI 能力调用的唯一治理入口。
- Workers 对业务只暴露稳定 usecase 协议；通用 debug invoke/stream 不作为长期业务集成入口。
- 同步调用与流式调用都能形成一致的最终调用记录、失败分类、用量摘要和结果归档。
- 文件类 AI 结果统一返回 `temporary artifact reference`；Java 负责下载临时产物并转存 `Storage`。
- `Knowledge` 只消费候选并自行应用正式结果；`Discovery` 只消费最终回答并自行保存会话、消息、来源与 trace。
- Workers 自身负责清理超过 `12` 小时的临时 artifact。

## In Scope

- AI 调用命令、结果对象、调用记录、候选结果字段口径收口。
- Workers AI 同步响应、SSE 完成事件与错误事件协议收口。
- 文件类 AI 结果的 artifact reference 协议、下载接口、Java 转存链路与清理任务收口。
- `Knowledge` 与 `Discovery` 对 AI 能力的消费闭环收口。
- AI / Workers 设计文档与实现覆盖文档收口。

## Out of Scope

- 不新增新的 AI capability。
- 不新增新的 render worker 能力。
- 不扩展 Classics 图片生成、视觉资产或批量交互产品能力。
- 不扩展模型服务供应商或模型路由策略。
- 不新增独立运营 dashboard。

## Completion Definition

完成后必须满足：

1. 每次 AI 调用都有稳定最终记录。
- 成功：记录 `status`、`resultFormat`、`resultPayload` 或 `artifactReferenceJson`、`usage`、`completedAt`。
- 失败：记录 `errorType`、`errorMessage`、`failureStage`。
- 流式：必须以 `completed` 或 `error` 收口；不得出现“只有片段没有最终态”。

2. 文件类 AI 结果有统一交付方式。
- Workers 只返回 `temporary artifact reference`。
- Java 只认 `artifact reference -> 下载 -> Storage 转存`。
- 业务侧只认 `Storage` 结果，不认 Workers 临时引用。

3. Workers 临时 artifact 生命周期受控。
- 每个 artifact 都有 `expiresAt`。
- 超过 `12` 小时的 artifact 会被 Workers 清理。

4. `Knowledge` 与 `Discovery` 的 AI 消费具有稳定追溯关系。
- `Knowledge` 能追溯到 `callId / candidateId`。
- `Discovery` 能追溯到 `callId`，并保留自己的会话、消息、来源与 trace。

## Fixed Semantics

### failureStage

固定只允许以下枚举值：

- `REQUEST_VALIDATE`
- `WORKER_REQUEST`
- `WORKER_STREAM`
- `WORKER_RESULT`
- `ARTIFACT_DOWNLOAD`
- `STORAGE_PERSIST`
- `CANDIDATE_PERSIST`

### fallbackUsed

`fallbackUsed` 只表示：
- AI 域在主服务不可用后切换到备用服务或备用模型并继续执行。

不表示：
- 提示词兜底
- 结果默认值兜底
- UI 或业务默认文案

### temporary artifact reference

文件类 AI 结果统一使用 `temporary artifact reference`。

它只表示：
- Workers 在当前请求结束后短期可读的临时产物引用。
- Java 可基于该引用下载产物并上传到 `Storage`。

它不表示：
- 最终业务下载地址
- 最终分享地址
- 最终 Storage URL

固定字段：
- `artifactId`
- `downloadPath`
- `contentType`
- `filename`
- `sizeBytes`
- `sha256`
- `expiresAt`

固定规则：
- 默认 TTL 为 `12` 小时。
- Java 不得把 `temporary artifact reference` 直接暴露为最终业务结果。

### AiCandidate reject

本次不拆分 `USER_REJECTED / SYSTEM_REJECTED`。

固定口径：
- `status = REJECTED`
- `rejectedAt` 记录拒绝时间
- `errorType / errorMessage / failureStage` 记录拒绝原因或系统失败原因

## Target Data Structure Changes

### Java AI invocation

相关文件：
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/command/AiInvokeCommand.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/result/AiInvokeResult.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/result/AiStreamEventResult.java`

字段要求：

`AiInvokeCommand`
- 新增：`allowFallback`

`AiInvokeResult`
- 新增：`failureStage`
- 新增：`streamCompleted`
- 新增：`fallbackUsed`
- 新增：`artifactReferenceJson`

`AiStreamEventResult`
- 新增：`failureStage`
- 新增：`warningsJson`
- 新增：`fallbackUsed`
- 新增：`artifactReferenceJson`

约束：
- 文本类结果写入 `resultPayload`。
- 文件类结果写入 `artifactReferenceJson`，不得把大文件内容写入 `resultPayload`。

### AI call record / candidate

相关文件：
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/model/entity/AiCallRecord.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/model/entity/AiCandidate.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/invocation/persistence/dataobject/AiCallRecordDO.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/invocation/persistence/dataobject/AiCandidateDO.java`

字段要求：

`AiCallRecord`
- 新增：`failureStage`
- 新增：`resultFormat`
- 新增：`resultPayload`
- 新增：`artifactReferenceJson`

`AiCallRecordDO`
- 新增：`failureStage`
- 新增：`resultFormat`
- 新增：`resultPayload`
- 新增：`artifactReferenceJson`

`AiCandidate`
- 新增：`rejectedAt`
- 新增：`failureStage`
- 新增：`artifactReferenceJson`

`AiCandidateDO`
- 新增：`rejectedAt`
- 新增：`failureStage`
- 新增：`artifactReferenceJson`

约束：
- 只有最终 `SUCCEEDED` 的调用才允许创建候选。
- 文件类候选结果以 `artifactReferenceJson` 为真相源。

### Workers protocol

相关文件：
- `kuzhambu-workers/src/kuzhambu_workers/schemas/ai.py`
- `kuzhambu-workers/src/kuzhambu_workers/schemas/stream.py`
- `kuzhambu-workers/src/kuzhambu_workers/api/ai_routes.py`

字段要求：

`AiInvokeResponse`
- 新增：`failureStage`
- 新增：`fallbackUsed`
- 新增：`artifactReference`

`StreamEvent.extra`
- 新增：`status`
- 新增：`failureStage`
- 新增：`fallbackUsed`
- 新增：`artifactReference`

约束：
- `completed` 事件必须带最终 `result` 或 `artifactReference`。
- `error` 事件必须带 `error`，并提供 `failureStage`。

## Execution Tasks

### T1. 收口 Workers AI 最终态协议

目标：
- 让 Workers 同步响应与流式最终事件表达同一套最终态字段。

相关文件：
- `kuzhambu-workers/src/kuzhambu_workers/schemas/ai.py`
- `kuzhambu-workers/src/kuzhambu_workers/schemas/stream.py`
- `kuzhambu-workers/src/kuzhambu_workers/api/ai_routes.py`
- `kuzhambu-workers/src/kuzhambu_workers/api/ai_usecase_routes.py`

相关数据结构：
- `AiInvokeResponse.failureStage`
- `AiInvokeResponse.fallbackUsed`
- `AiInvokeResponse.artifactReference`
- `StreamEvent.extra.status`
- `StreamEvent.extra.failureStage`
- `StreamEvent.extra.fallbackUsed`
- `StreamEvent.extra.artifactReference`

处理动作：
- 为同步响应补齐最终态字段。
- 为 `completed/error` 事件补齐最终态字段。
- 固定 `failureStage` 枚举输出口径。

验收点：
- 同步调用与流式调用都能表达 `status + usage + failureStage + fallbackUsed + result/artifactReference`。

### T2. 收口 Java AI 调用结果模型

目标：
- 让 Java 侧调用结果对象完整承载文本结果、文件结果与最终失败分类。

相关文件：
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/command/AiInvokeCommand.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/result/AiInvokeResult.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/result/AiStreamEventResult.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/service/impl/AiWorkerInvocationApplicationServiceImpl.java`

相关数据结构：
- `AiInvokeCommand.allowFallback`
- `AiInvokeResult.failureStage`
- `AiInvokeResult.streamCompleted`
- `AiInvokeResult.fallbackUsed`
- `AiInvokeResult.artifactReferenceJson`
- `AiStreamEventResult.failureStage`
- `AiStreamEventResult.artifactReferenceJson`

处理动作：
- 补齐调用结果字段。
- 让 stream `completed/error` 事件能完整映射到 `AiInvokeResult`。
- 固定文本结果写 `resultPayload`，文件结果写 `artifactReferenceJson`。

验收点：
- Java 侧无需按 capability 分支解释最终结果结构。

### T3. 收口 AI 调用记录与候选记录模型

目标：
- 让调用记录与候选记录足以承载长期治理与追溯。

相关文件：
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/model/entity/AiCallRecord.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/model/entity/AiCandidate.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/invocation/persistence/dataobject/AiCallRecordDO.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/invocation/persistence/dataobject/AiCandidateDO.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/invocation/repository/impl/AiInvocationRepositoryImpl.java`

相关数据结构：
- `AiCallRecord.failureStage`
- `AiCallRecord.resultFormat`
- `AiCallRecord.resultPayload`
- `AiCallRecord.artifactReferenceJson`
- `AiCandidate.rejectedAt`
- `AiCandidate.failureStage`
- `AiCandidate.artifactReferenceJson`

处理动作：
- 补齐领域对象、DO 与 repository 映射字段。
- 保证失败调用不创建候选。
- 保证 `REJECTED` 候选有 `rejectedAt`。

验收点：
- 调用记录、候选记录、数据库字段完全对齐。

### T4. 定义 temporary artifact reference 协议

目标：
- 固定文件类 AI 结果的统一返回结构。

相关文件：
- `docs/30-designs/AI-DESIGN.md`
- `docs/30-designs/WORKERS-DESIGN.md`
- `docs/20-interfaces/WORKERS-AI-INTERFACE.md`
- `kuzhambu-workers/src/kuzhambu_workers/schemas/ai.py`

相关数据结构：
- `artifactId`
- `downloadPath`
- `contentType`
- `filename`
- `sizeBytes`
- `sha256`
- `expiresAt`

处理动作：
- 固定 artifact reference 字段。
- 明确它不是最终业务 URL。
- 明确 TTL 为 `12` 小时。

验收点：
- 文档与代码使用同一套 artifact reference 字段定义。

### T5. Workers 临时产物落地与下载接口

目标：
- Workers 能落地临时产物，并提供仅供 Java 内部读取的下载接口。

相关文件：
- `kuzhambu-workers/src/kuzhambu_workers/render/artifact_store.py`
- `kuzhambu-workers/src/kuzhambu_workers/api/artifact_routes.py`
- `kuzhambu-workers/src/kuzhambu_workers/core/security.py`
- `kuzhambu-workers/src/kuzhambu_workers/core/config.py`
- `kuzhambu-workers/src/kuzhambu_workers/main.py`

相关数据结构：
- `artifactReference.downloadPath`
- `artifactReference.expiresAt`
- artifact store metadata

处理动作：
- 为文件类结果落地临时 artifact。
- 提供内部下载入口。
- 过期 artifact 返回稳定错误。

验收点：
- Java 可以凭内部服务身份下载指定 artifact。
- 已过期 artifact 无法读取。

### T6. Java 下载 artifact 并转存 Storage

目标：
- Java 能基于 artifact reference 下载产物并转存到 Storage。

相关文件：
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/client/WorkerAiClient.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/client/WorkerAiHttpClient.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/service/impl/AiWorkerInvocationApplicationServiceImpl.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-facade/src/main/java/com/thundax/kuzhambu/storage/facade/StorageFacade.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-facade/src/main/java/com/thundax/kuzhambu/storage/facade/request/UploadStorageFacadeRequest.java`

相关数据结构：
- `AiInvokeResult.artifactReferenceJson`
- `AiCallRecord.artifactReferenceJson`
- `AiCandidate.artifactReferenceJson`
- `storageObjectId`

处理动作：
- Java 下载 artifact。
- Java 上传到 Storage。
- 业务侧最终只拿 Storage 结果。

验收点：
- Java 不直接把 Workers 临时引用暴露为最终业务结果。

### T7A. 大文件下载阈值与 multipart 初始化

目标：
- 为图片、视频、ZIP 等大产物固定下载阈值与 multipart 入口选择规则。

相关文件：
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/client/WorkerAiHttpClient.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/MultipartUploadApplicationService.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-facade/src/main/java/com/thundax/kuzhambu/storage/facade/StorageFacade.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-facade/src/main/java/com/thundax/kuzhambu/storage/facade/request/InitMultipartUploadFacadeRequest.java`

相关数据结构：
- `artifactReference.sizeBytes`
- `multipartThresholdBytes`
- `uploadId`

处理动作：
- 固定小文件与大文件转存阈值。
- 超过阈值的文件走 `流式下载 + multipart init`。
- 禁止默认一次性内存上传大文件。

验收点：
- Java 能按阈值区分普通上传与 multipart 转存入口。

### T7B. 大文件 multipart 分片上传与完成

目标：
- 为图片、视频、ZIP 等大产物完成分片上传与完成提交流程，并同步 Storage 设计口径。

相关文件：
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/MultipartUploadApplicationService.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-facade/src/main/java/com/thundax/kuzhambu/storage/facade/request/UploadMultipartPartFacadeRequest.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-facade/src/main/java/com/thundax/kuzhambu/storage/facade/request/CompleteMultipartUploadFacadeRequest.java`
- `docs/30-designs/STORAGE-DESIGN.md`

相关数据结构：
- `uploadId`
- `partNumber`
- `etag`
- `sizeBytes`

处理动作：
- 让大文件按分片上传。
- 完成 multipart 完成提交。
- 同步 Storage 设计文档口径。

验收点：
- 大文件不会因单次内存装载或普通上传路径导致实现不可行。

### T8. Workers 临时 artifact 清理任务

目标：
- Workers 自动清理超过 `12` 小时的临时 artifact。

相关文件：
- `kuzhambu-workers/src/kuzhambu_workers/render/artifact_store.py`
- `kuzhambu-workers/src/kuzhambu_workers/core/config.py`
- `kuzhambu-workers/src/kuzhambu_workers/main.py`
- `kuzhambu-workers/tests/test_artifact_store.py`
- `kuzhambu-workers/tests/test_artifact_cleanup_job.py`

相关数据结构：
- `artifactReference.expiresAt`
- `artifactTtlHours = 12`

处理动作：
- 增加定时清理任务。
- 清理超过 `12` 小时的 artifact。
- 保证未过期 artifact 不误删。

验收点：
- 超时文件会被清理。
- 未超时文件不会误删。

### T9. 文件类结果失败边界

目标：
- 定义 artifact 下载失败、Storage 转存失败的最终态。

相关文件：
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/service/impl/AiWorkerInvocationApplicationServiceImpl.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/client/WorkerAiHttpClient.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-facade/src/main/java/com/thundax/kuzhambu/storage/facade/StorageFacade.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-facade/src/main/java/com/thundax/kuzhambu/storage/facade/request/UploadStorageFacadeRequest.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/invocation/service/impl/AiWorkerInvocationApplicationServiceImplTest.java`

相关数据结构：
- `failureStage`
- `errorType`
- `errorMessage`
- `artifactReferenceJson`

处理动作：
- artifact 下载失败固定为 `ARTIFACT_DOWNLOAD`。
- Storage 转存失败固定为 `STORAGE_PERSIST`。
- 失败时保留调用记录与 artifact reference 摘要。

验收点：
- 能区分 worker 生成失败、artifact 下载失败、Storage 转存失败。

### T10. Knowledge AI 候选闭环

目标：
- Knowledge 对 AI 的消费稳定围绕“候选 -> 应用正式结果”展开。

相关文件：
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/knowledge/service/impl/KnowledgeAiExtractionApplicationServiceImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/service/impl/KnowledgeGraphExtractionApplicationServiceImpl.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/dto/AiCandidateFacadeDto.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/support/KnowledgeGraphCandidateApplySupport.java`

相关数据结构：
- `KnowledgeAiExtractionResult.callId`
- `KnowledgeAiExtractionResult.candidateId`
- `AiCandidate.status`
- `AiCandidate.failureStage`

处理动作：
- 保证 Knowledge 只消费候选。
- 保证抽取任务、候选、正式图谱应用三者可追溯。

验收点：
- Knowledge 图谱提取任务可追到 AI `callId/candidateId`。

### T11. Discovery AI 消费闭环

目标：
- Discovery 对 AI 的消费停留在稳定最终结果，不依赖 worker 过程态。

相关文件：
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/discovery/service/impl/DiscoveryAiApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/QueryUnderstandingApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaRetrievalTrace.java`

相关数据结构：
- `DiscoveryAiResult.callId`
- `DiscoveryAiResult.status`
- `DiscoveryAiResult.errorType`
- `DiscoveryAiResult.errorMessage`
- `QaRetrievalTrace.callId`

处理动作：
- 统一 Discovery 同步与流式调用最终结果消费口径。
- 将最终 AI 调用标识稳定挂到 QA trace。

验收点：
- Discovery 会话、消息、来源仍由 Discovery 保存。
- AI 域只输出最终回答结果与调用可追溯标识。

### T12. 测试与设计文档收口

目标：
- 协议、记录、消费、artifact 生命周期都有测试与文档口径。

相关文件：
- `kuzhambu-servers/biz/ai/.../DiscoveryAiApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/ai/.../KnowledgeAiExtractionApplicationServiceImplTest.java`
- `kuzhambu-workers/tests/test_ai_routes.py`
- `kuzhambu-workers/tests/test_ai_usecase_routes.py`
- `kuzhambu-workers/tests/test_artifact_store.py`
- `kuzhambu-workers/tests/test_artifact_cleanup_job.py`
- `docs/30-designs/AI-DESIGN.md`
- `docs/30-designs/WORKERS-DESIGN.md`
- `docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`
- `docs/40-readiness/WORKERS-IMPLEMENTATION-COVERAGE.md`

相关数据结构：
- 覆盖本 RUNBOOK 中新增的最终态字段与 artifact reference 字段。

处理动作：
- 为同步成功、流式成功、流式无 completed、artifact 下载失败、Storage 转存失败、候选应用、候选拒绝补齐测试。
- 同步 AI / Workers 设计文档。
- 同步实现覆盖文档。

验收点：
- 关键协议、artifact 生命周期、消费闭环都有测试。
- 设计文档与实现覆盖文档口径一致。

## Recommended Execution Order

1. `T1`
2. `T2`
3. `T3`
4. `T4`
5. `T5`
6. `T6`
7. `T7`
8. `T8`
9. `T9`
10. `T10`
11. `T11`
12. `T12`

## Files To Leave Untouched In This Runbook Phase

当前工作区已有未提交文档改动，RUNBOOK 阶段不处理：
- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
- `docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md`
