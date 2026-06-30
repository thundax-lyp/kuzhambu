# RUNBOOK: AI + Workers + Classics Text Closure

## 1. Scope

本 RUNBOOK 只覆盖以下业务闭环：

- `POST /api/ai/refinement/task/add` -> `SANCAI_ENTRY.translate`
- `POST /api/ai/refinement/task/add` -> `SANCAI_ENTRY.summary`
- `POST /api/ai/refinement/task/add` -> `WANGQI_DOCUMENT.summary`
- `POST /api/ai/refinement/task/add` -> `MING_CUSTOMS.summary`

闭环边界固定为：

- `Classics Interface`
- `AI Application / AI Infra`
- `Workers`
- `AiCandidate`
- `Classics applyAiCandidate`

本 RUNBOOK 不包含：

- `image_analysis`
- `fusion`
- `visual`
- `image_gen`
- `split`
- batch job
- Discovery answer
- Knowledge extraction
- OpenAI-compatible 之外的 provider

## 2. Done Criteria

以下条件全部满足，才算闭环完成：

1. `POST /api/ai/refinement/task/add` 成功时只返回受理成功的任务，不直接返回候选结果。
2. `POST /api/ai/refinement/task/get` 在任务完成后返回真实候选引用，不再返回 workers placeholder。
3. AI 域把最终调用状态完整写入 `ai_call_record`。
4. AI 域把候选快照完整写入 `ai_candidate`。
5. AI 域新增 `ai_refinement_task` 台账，作为前端任务状态真相源。
6. Admin Web 三类 Classics 页面通过任务接口触发 `translate` / `summary`。
7. Admin Web 通过 `task/get` 或 `task/page` 轮询任务，并在任务成功后刷新候选列表。
8. `POST /api/classics/content/ai-candidates/change` 能把 `translate` / `summary` 候选应用到正式内容。
9. `translate` / `summary` 成功候选必须是非空 `TEXT`。
10. `summary` 输出类型为 `TEXT`，本批次不限制长度，但必须是可直接展示的摘要文本。

## 3. Business Flow

1. Admin Web 在 Classics 页面调用 `POST /api/ai/refinement/task/add`。
2. `AiRefinementTaskController` 校验权限、内容类型、capability、模型和 prompt 版本参数。
3. AI 域创建 `ai_refinement_task`，状态记为 `PENDING`，立即返回 `taskId`。
4. Admin Web 进入轮询，调用 `POST /api/ai/refinement/task/get` 或 `POST /api/ai/refinement/task/page`。
5. AI application 读取业务内容快照，解析 `contentType + capability`，得到固定 `operation + workerPath`。
6. AI 域统一根据 `serviceRole + modelId` 组装 `modelConfig`。
7. AI 域统一根据 `promptVersionId` 绑定 prompt 真相源，并生成或校验：
   - `promptMessagesJson`
   - `promptVariablesJson`
   - `promptHash`
8. AI 域把任务状态更新为 `RUNNING`，并调用 workers 内部 usecase URL。
9. workers 通过 `OpenAI-compatible chat/completions` 执行真实文本调用。
10. 对 `translate` / `summary` 这类非流式 capability，workers 返回最终 `TEXT` 结果。
11. AI 域写入 `ai_call_record` 最终态。
12. AI 域写入 `ai_candidate` 候选态。
13. AI 域把 `callId`、`candidateId`、`resultFormat`、`resultPreview`、`completedAt` 回填到 `ai_refinement_task`，并把状态更新为 `SUCCEEDED`；失败时更新为 `FAILED` 或 `PARTIAL`。
14. Admin Web 轮询看到任务成功后，再调用 `POST /api/ai/invocation/candidate/list` 拉取待处理候选。
15. Admin Web 调用 `POST /api/classics/content/ai-candidates/change` 应用候选。
16. `ClassicsContentApplicationServiceImpl.applyAiCandidate()` 更新正式字段并生成 `AI_APPLIED` 版本。
17. AI 域把候选状态更新为 `APPLIED`。
18. Admin Web 刷新内容详情、任务状态与候选列表。

## 3.1 Frontend Protocol Decision

本批次前端协议固定为：

- 默认：异步任务
- 可选：任务之上的 SSE 订阅
- 不采用：前端默认 WebSocket

固定规则：

- `translate`、`summary`、`tags`、`qa` 默认只走任务协议，不做前端 SSE。
- workers 的 SSE 是 `AI domain -> workers` 内部协议，不直接暴露为 `Admin Web -> Java` 默认协议。
- 前端任务的最终真相源是 `ai_refinement_task`，不是 SSE 片段。
- 即使未来为流式 capability 增加 `task/stream`，前端断线后也必须回退到 `task/get` 查最终状态。
- `ai_refinement_task` 只作为前端任务台账，允许按失效策略清理；调用记录与候选结果不随任务清理删除。

## 3.2 Expired Task Cleanup

精修任务必须补充自动失效清理流程，固定规则如下：

1. 清理对象只包含 `ai_refinement_task`。
2. 不清理：
   - `ai_call_record`
   - `ai_candidate`
   - `ai_batch_job`
3. 清理由 Java servers 计划任务执行，不由 workers 负责。
4. 计划任务执行频率建议为每 `1` 小时一次。
5. 失效阈值固定为 `12` 小时。
6. 对 `status in (PENDING, RUNNING)` 且 `requestedAt < now - 12h` 的任务，先自动收口，不直接删除：
   - `status = FAILED`
   - `failureStage = WORKER_RESULT`
   - `errorType = TASK_EXPIRED`
   - `errorMessage = 任务超过 12 小时未完成，系统自动关闭`
   - `completedAt = now`
7. 对 `status in (SUCCEEDED, FAILED, PARTIAL, CANCELLED)` 的终态任务，当终态时间早于 `now - 12h` 时执行物理删除。
8. 终态时间字段取值规则：
   - 优先 `completedAt`
   - 若为空则取 `cancelledAt`
   - 若仍为空则回退 `requestedAt`
9. 前端查询已被清理的任务时，接口应返回稳定业务提示，例如“任务不存在或已过期清理”，而不是让页面无限轮询。

## 4. Endpoint Contracts

### 4.1 `POST /api/ai/refinement/task/add`

用途：

- 创建精修任务。

请求字段：

- `capability: string`
- `scope: string`
- `contentType: string`
- `contentId: long`
- `objectId: long | null`
- `modelId: long`
- `promptVersionId: long`
- `requestId: string`
- `traceId: string`
- `requestedBy: long`

响应字段：

- `taskId: long`
- `status: string`
- `capability: string`
- `contentType: string`
- `contentId: long`
- `requestedAt: instant`

相关文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/AiRefinementTaskController.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/request/AiRefinementTaskRequests.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/response/AiRefinementTaskResponses.java`

### 4.2 `POST /api/ai/refinement/task/get`

用途：

- 查询单个精修任务详情与最终结果状态。

请求字段：

- `taskId: long`

响应字段：

- `taskId: long`
- `status: string`
- `scope: string`
- `capability: string`
- `contentType: string`
- `contentId: long`
- `objectId: long | null`
- `serviceRole: string | null`
- `modelId: long | null`
- `modelName: string | null`
- `promptVersionId: long | null`
- `requestId: string`
- `traceId: string`
- `callId: long | null`
- `candidateId: long | null`
- `failureStage: string | null`
- `errorType: string | null`
- `errorMessage: string | null`
- `resultFormat: string | null`
- `resultPreview: string | null`
- `requestedAt: instant`
- `startedAt: instant | null`
- `completedAt: instant | null`
- `cancelledAt: instant | null`

相关文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/AiRefinementTaskController.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/request/AiRefinementTaskRequests.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/response/AiRefinementTaskResponses.java`

### 4.3 `POST /api/ai/refinement/task/page`

用途：

- 分页查询精修任务，用于页面轮询和任务面板。

请求字段：

- `capability: string | null`
- `status: string | null`
- `contentType: string | null`
- `contentId: long | null`
- `requestedBy: long | null`
- `pageNo: int`
- `pageSize: int`

响应字段：

- `items: array`
- `items[].taskId: long`
- `items[].status: string`
- `items[].scope: string`
- `items[].capability: string`
- `items[].contentType: string`
- `items[].contentId: long`
- `items[].objectId: long | null`
- `items[].requestedBy: long | null`
- `items[].serviceRole: string | null`
- `items[].modelId: long | null`
- `items[].modelName: string | null`
- `items[].promptVersionId: long | null`
- `items[].callId: long | null`
- `items[].candidateId: long | null`
- `items[].failureStage: string | null`
- `items[].errorType: string | null`
- `items[].errorMessage: string | null`
- `items[].resultFormat: string | null`
- `items[].resultPreview: string | null`
- `items[].requestedAt: instant`
- `items[].startedAt: instant | null`
- `items[].completedAt: instant | null`
- `items[].cancelledAt: instant | null`
- `total: long`
- `pageNo: int`
- `pageSize: int`

相关文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/AiRefinementTaskController.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/request/AiRefinementTaskRequests.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/response/AiRefinementTaskResponses.java`

### 4.4 `POST /api/ai/refinement/task/cancel`

用途：

- 取消尚未完成的精修任务。

请求字段：

- `taskId: long`
- `requestedBy: long`

响应字段：

- `taskId: long`
- `status: string`
- `cancelledAt: instant | null`

相关文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/AiRefinementTaskController.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/request/AiRefinementTaskRequests.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/response/AiRefinementTaskResponses.java`

### 4.5 `POST /api/classics/content/ai-candidates/change`

用途：

- 把 AI 候选应用到正式内容。

请求字段：

- `candidateId: long`
- `contentType: string`
- `contentId: long`
- `capability: string`
- `resultFormat: string`
- `resultPayload: string`
- `changeSummary: string | null`

响应字段：

- `contentType: string`
- `contentId: long`
- `versionId: long`
- `versionNo: int`

相关文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/ClassicsContentAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/request/ClassicsContentRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/response/ClassicsContentResponse.java`

### 4.6 `POST /api/ai/prompt/template/save`

用途：

- 保存或更新 prompt 模板和版本数据。

请求字段：

- `templateId: long | null`
- `scope: string`
- `capability: string`
- `name: string`
- `description: string | null`
- `status: string | null`
- `messageTemplatesJson: string`
- `variablesSnapshotJson: string | null`
- `outputSchemaJson: string | null`
- `changeSummary: string | null`
- `variables: array`

响应字段：

- `templateId: long`
- `scope: string`
- `capability: string`
- `name: string`
- `description: string | null`
- `status: string`
- `currentVersionNo: int`
- `registeredAt: instant`

### 4.7 `POST /api/ai/prompt/version/current`

用途：

- 获取当前生效 prompt 版本。

请求字段：

- `templateId: long`

响应字段：

- `promptVersionId: long`
- `templateId: long`
- `versionNo: int`
- `messageTemplatesJson: string`
- `variablesSnapshotJson: string | null`
- `outputSchemaJson: string | null`
- `current: boolean`
- `changeSummary: string | null`
- `registeredAt: instant`

### 4.8 `POST /api/ai/prompt/variable/list`

用途：

- 获取模板变量定义。

请求字段：

- `templateId: long`

响应字段：

- `variableId: long`
- `templateId: long`
- `variableName: string`
- `required: boolean`
- `description: string | null`
- `priority: int`

相关文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/prompt/controller/PromptController.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/prompt/controller/request/PromptRequests.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/prompt/controller/response/PromptResponses.java`

### 4.9 Workers Usecase URLs

必须使用以下固定 URL：

- `POST /internal/ai/classics/sancai/translate`
- `POST /internal/ai/classics/sancai/summary`
- `POST /internal/ai/classics/wangqi/summary`
- `POST /internal/ai/classics/ming-customs/summary`

Workers 请求字段：

- `requestId: string`
- `traceId: string`
- `callerDomain: string`
- `operation: string`
- `capability: string`
- `scope: string`
- `modelConfig.serviceRole: string`
- `modelConfig.apiSource: string`
- `modelConfig.baseUrl: string`
- `modelConfig.apiKey: string`
- `modelConfig.modelName: string`
- `modelConfig.capabilityTags: array<string>`
- `modelConfig.parameters: json`
- `modelConfig.timeoutMs: long`
- `prompt.templateId: string | null`
- `prompt.promptVersionId: string | null`
- `prompt.versionNo: int | null`
- `prompt.messages: json`
- `prompt.variables: json`
- `prompt.promptHash: string | null`
- `input.contentType: string`
- `input.contentId: string | null`
- `input.payload: json`
- `outputSchema: json`
- `options.stream: boolean`
- `options.forceJson: boolean`
- `options.locale: string`

Workers 响应字段：

- `requestId: string`
- `traceId: string`
- `status: string`
- `capability: string`
- `result.format: string`
- `result.payload: json`
- `usage.latencyMs: int | null`
- `usage.inputTokens: int | null`
- `usage.outputTokens: int | null`
- `usage.costAmount: string | null`
- `failureStage: string | null`
- `fallbackUsed: boolean | null`
- `artifactReference: json | null`
- `warnings: json | null`
- `error.type: string | null`
- `error.code: string | null`
- `error.message: string | null`
- `error.retryable: boolean | null`
- `error.detail: json | null`

相关文件：

- `kuzhambu-workers/src/kuzhambu_workers/api/ai_usecase_routes.py`
- `kuzhambu-workers/src/kuzhambu_workers/ai/usecase_registry.py`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/client/dto/WorkerAiDtos.java`

### 4.10 Admin Web Classics AI Calls

Admin Web 本批次必须直接调用以下 URL：

- `POST /api/ai/refinement/task/add`
- `POST /api/ai/refinement/task/get`
- `POST /api/ai/refinement/task/page`
- `POST /api/ai/refinement/task/cancel`
- `POST /api/ai/invocation/candidate/list`
- `POST /api/ai/invocation/candidate/reject`
- `POST /api/classics/content/ai-candidates/change`

前端已有能力：

- 候选列表：`POST /api/ai/invocation/candidate/list`
- 候选拒绝：`POST /api/ai/invocation/candidate/reject`
- 候选应用：`POST /api/classics/content/ai-candidates/change`

前端本次新增能力：

- 三类 Classics 页面触发 `POST /api/ai/refinement/task/add`
- 三类 Classics 页面轮询 `POST /api/ai/refinement/task/get`
- 三类 Classics 页面可按内容过滤查询 `POST /api/ai/refinement/task/page`
- 三类 Classics 页面在任务成功后刷新候选面板

相关文件：

- `kuzhambu-apps/admin-web/src/pages/classics/common/ai-candidate-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/ai-candidate-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/common/ai-refinement-task-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/ai-refinement-task-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.tsx`

## 5. Data Structures

### 5.1 `ai_call_record`

文件：

- `db/schema/ai.sql`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/model/entity/AiCallRecord.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/invocation/persistence/dataobject/AiCallRecordDO.java`

目标字段：

- `call_id`
- `batch_id`
- `scope`
- `capability`
- `content_type`
- `content_id`
- `object_id`
- `service_id`
- `service_role`
- `model_id`
- `model_name`
- `prompt_version_id`
- `request_id`
- `trace_id`
- `status`
- `stream_used`
- `stream_completed`
- `fallback_used`
- `latency_ms`
- `input_tokens`
- `output_tokens`
- `cost_amount`
- `failure_stage`
- `result_format`
- `result_payload`
- `artifact_reference_json`
- `error_type`
- `error_message`
- `warnings_json`
- `requested_at`
- `completed_at`

本次必须补齐的 schema 字段：

- `failure_stage varchar(32) default null`
- `result_format varchar(32) default null`
- `result_payload longtext default null`
- `artifact_reference_json json default null`

### 5.2 `ai_candidate`

文件：

- `db/schema/ai.sql`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/model/entity/AiCandidate.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/invocation/persistence/dataobject/AiCandidateDO.java`

目标字段：

- `candidate_id`
- `call_id`
- `batch_id`
- `capability`
- `content_type`
- `content_id`
- `object_id`
- `artifact_reference_json`
- `result_format`
- `result_payload`
- `status`
- `prompt_version_id`
- `model_name`
- `failure_stage`
- `error_type`
- `error_message`
- `requested_at`
- `applied_at`
- `rejected_at`

本次必须补齐的 schema 字段：

- `artifact_reference_json json default null`
- `failure_stage varchar(32) default null`
- `rejected_at datetime(3) default null`

### 5.3 `AiInvokeCommand`

文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/command/AiInvokeCommand.java`

本次新增字段：

- `apiSource: String`
- `baseUrl: String`
- `apiKey: String`
- `capabilityTagsJson: String`
- `modelParametersJson: String`

字段来源：

- `apiSource <- AiServiceConfig.apiSource`
- `baseUrl <- AiServiceConfig.baseUrl`
- `apiKey <- 解密后的 AiServiceConfig.encryptedApiKey`
- `capabilityTagsJson <- AiModel.capabilityTags`
- `modelParametersJson <- AiModel.defaultParamsJson`

### 5.4 Workers `modelConfig`

文件：

- `kuzhambu-workers/src/kuzhambu_workers/schemas/ai.py`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/client/dto/WorkerAiDtos.java`

必须真实下发的字段：

- `serviceRole`
- `apiSource`
- `baseUrl`
- `apiKey`
- `modelName`
- `capabilityTags`
- `parameters`
- `timeoutMs`

协议约束：

- 本批次只支持 `OpenAI-compatible`
- 文本调用只使用 `chat/completions`

### 5.5 Classics Official Fields

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/model/entity/SancaiEntry.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/wangqi/model/entity/WangqiDocument.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/mingcustoms/model/entity/MingCustomsEntry.java`

正式落库字段：

- `SancaiEntry.translationText`
- `SancaiEntry.translationStatus`
- `SancaiEntry.summary`
- `WangqiDocument.summary`
- `MingCustomsEntry.summary`

### 5.6 Admin Web Refinement Request

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/common/ai-refinement-task-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/ai-refinement-task-service.ts`

前端创建精修任务时必须显式构造以下字段：

- `capability`
- `scope`
- `contentType`
- `contentId`
- `objectId`
- `modelId`
- `promptVersionId`
- `requestId`
- `traceId`
- `requestedBy`

前端轮询任务时必须显式构造以下字段：

- `taskId`

前端分页查询任务时必须显式构造以下字段：

- `capability: string | null`
- `status: string | null`
- `contentType: string | null`
- `contentId: long | null`
- `requestedBy: long | null`
- `pageNo: int`
- `pageSize: int`

前端约束：

- `scope` 固定为 `classics`
- `contentType` 只能是 `SANCAI_ENTRY`、`WANGQI_DOCUMENT`、`MING_CUSTOMS`
- `translate` 只允许 `SANCAI_ENTRY`
- `summary` 允许三类内容

### 5.7 `ai_refinement_task`

文件：

- `db/schema/ai.sql`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/refinement/model/entity/AiRefinementTask.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/refinement/persistence/dataobject/AiRefinementTaskDO.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/refinement/persistence/mapper/AiRefinementTaskMapper.java`

目标字段：

- `id`
- `task_id`
- `scope`
- `capability`
- `content_type`
- `content_id`
- `object_id`
- `requested_by`
- `request_id`
- `trace_id`
- `status`
- `service_role`
- `model_id`
- `model_name`
- `prompt_version_id`
- `call_id`
- `candidate_id`
- `result_format`
- `result_preview`
- `failure_stage`
- `error_type`
- `error_message`
- `stream_enabled`
- `requested_at`
- `started_at`
- `completed_at`
- `cancelled_at`

本次必须补齐的 schema 字段：

- `task_id bigint not null`
- `status varchar(32) not null`
- `call_id bigint default null`
- `candidate_id bigint default null`
- `result_format varchar(32) default null`
- `result_preview text default null`
- `failure_stage varchar(32) default null`
- `error_type varchar(64) default null`
- `error_message text default null`
- `stream_enabled tinyint(1) not null default 0`

字段语义补充：

- `requested_at`：任务创建时间，也是超时判定起点。
- `completed_at`：任务进入 `SUCCEEDED`、`FAILED`、`PARTIAL` 的终态时间。
- `cancelled_at`：任务进入 `CANCELLED` 的终态时间。
- 终态清理时使用 `completed_at -> cancelled_at -> requested_at` 的优先级判定可删除时间。
- 任务删除前不得删除关联 `ai_call_record` 和 `ai_candidate`。

## 6. Prompt Data

### 6.1 Prompt Truth Source

prompt 真相源固定为：

- `ai_prompt_template`
- `ai_prompt_version`
- `ai_prompt_variable`

禁止长期依赖：

- 业务域自由拼装 `promptMessagesJson`
- 前端自由拼装 `promptVariablesJson`

如果实现节奏上暂时保留这些字段入参，AI 域也必须校验它们与当前 `promptVersionId` 对应版本一致。

### 6.2 Required Prompt Templates

由于 `ai_prompt_template(scope, capability)` 是唯一键，本批次只能准备两套模板：

1. `scope = classics`, `capability = translate`
2. `scope = classics`, `capability = summary`

### 6.3 Prompt Template Records

`ai_prompt_template` 需要的记录 1：

- `scope = classics`
- `capability = translate`
- `name = Classics Translate`
- `description = Classics translate template`
- `status = ACTIVE`
- `current_version_no = 1`

`ai_prompt_template` 需要的记录 2：

- `scope = classics`
- `capability = summary`
- `name = Classics Summary`
- `description = Classics summary template`
- `status = ACTIVE`
- `current_version_no = 1`

### 6.4 Prompt Version Records

`translate.message_templates_json` 建议值：

```json
[
  {
    "role": "system",
    "content": "你是古籍整理助手。任务是把输入的古文或文言文内容准确翻译成现代中文。不得输出说明、前缀、标题或注释，只输出最终译文。遇到不确定处应保守翻译，不得编造。"
  },
  {
    "role": "user",
    "content": "内容类型：{{contentType}}\n标题：{{title}}\n卷/章节：{{contextPath}}\n原文：\n{{sourceText}}\n\n要求：\n1. 输出现代中文译文。\n2. 保留原文专有名词，不要擅自改写。\n3. 不要输出“译文：”等前缀。\n4. 输出必须是纯文本。"
  }
]
```

`translate.output_schema_json`：

```json
{"type":"text"}
```

`translate.variables_snapshot_json`：

```json
[
  {"name":"contentType","required":true},
  {"name":"title","required":false},
  {"name":"contextPath","required":false},
  {"name":"sourceText","required":true}
]
```

`summary.message_templates_json` 建议值：

```json
[
  {
    "role": "system",
    "content": "你是古籍整理助手。任务是根据给定内容生成简洁、准确、可直接展示的中文摘要。不得编造未出现的信息，不得输出解释性前缀。输出结果为纯文本摘要，不限制字数，但必须保持摘要性，不得机械复述原文。"
  },
  {
    "role": "user",
    "content": "内容类型：{{contentType}}\n标题：{{title}}\n分类信息：{{categoryPath}}\n原文：\n{{originalText}}\n\n译文：\n{{translationText}}\n\n正文：\n{{bodyText}}\n\n已有摘要：{{existingSummary}}\n\n要求：\n1. 生成可直接展示的中文摘要，不限制长度，但必须保持摘要性。\n2. 优先使用译文和正文中的确定信息。\n3. 不重复标题，不输出“摘要：”前缀。\n4. 不得机械复述整段原文。\n5. 输出必须是纯文本。"
  }
]
```

`summary.output_schema_json`：

```json
{"type":"text"}
```

`summary.variables_snapshot_json`：

```json
[
  {"name":"contentType","required":true},
  {"name":"title","required":false},
  {"name":"categoryPath","required":false},
  {"name":"originalText","required":false},
  {"name":"translationText","required":false},
  {"name":"bodyText","required":false},
  {"name":"existingSummary","required":false}
]
```

### 6.5 Prompt Variable Records

`classics + translate` 必须有：

- `contentType`
- `title`
- `contextPath`
- `sourceText`

`classics + summary` 必须有：

- `contentType`
- `title`
- `categoryPath`
- `originalText`
- `translationText`
- `bodyText`
- `existingSummary`

变量命名规则：

- 必须使用稳定英文名
- 不允许使用同义重复变量
- 不允许用临时页面字段名替代领域字段名

## 7. Tasks

### Task 0A: AI Base Config Data

目标：

- 准备 `ai_service_config`、`ai_model`、`ai_capability_mapping`。

文件范围，3 个文件：

1. `db/schema/ai.sql`
2. `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/prompt/controller/PromptController.java`
3. `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/test/java/com/thundax/kuzhambu/ai/infra/prompt/repository/impl/PromptRepositoryIT.java`

### Task 0B: Prompt Template Data

目标：

- 准备 `ai_prompt_template`、`ai_prompt_version`、`ai_prompt_variable`。

文件范围，4 个文件：

1. `db/schema/ai.sql`
2. `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/prompt/controller/request/PromptRequests.java`
3. `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/prompt/controller/response/PromptResponses.java`
4. `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/test/java/com/thundax/kuzhambu/ai/infra/prompt/repository/impl/PromptRepositoryIT.java`

### Task A: AI Call Record And Candidate Persistence

目标：

- 把 `ai_call_record` 和 `ai_candidate` 最终态写完整。

文件范围，5 个文件：

1. `db/schema/ai.sql`
2. `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/invocation/persistence/mapper/AiInvocationMapper.java`
3. `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/service/impl/AiWorkerInvocationApplicationServiceImpl.java`
4. `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/result/AiInvokeResult.java`
5. `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/test/java/com/thundax/kuzhambu/ai/infra/invocation/repository/impl/AiInvocationRepositoryIT.java`

### Task A2: AI Refinement Task Schema And Repository

目标：

- 新增 `ai_refinement_task` 台账，作为前端任务状态真相源。

文件范围，5 个文件：

1. `db/schema/ai.sql`
2. `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/refinement/model/entity/AiRefinementTask.java`
3. `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/refinement/persistence/dataobject/AiRefinementTaskDO.java`
4. `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/refinement/persistence/mapper/AiRefinementTaskMapper.java`
5. `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/test/java/com/thundax/kuzhambu/ai/infra/refinement/repository/impl/AiRefinementTaskRepositoryIT.java`

### Task B1: AI Unified modelConfig Resolver

目标：

- 在 AI 域统一生成 workers 需要的 `modelConfig`。

文件范围，5 个文件：

1. `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/command/AiInvokeCommand.java`
2. `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/config/service/AiServiceConfigApplicationService.java`
3. `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/model/service/AiModelApplicationService.java`
4. `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/support/AiWorkerModelConfigResolver.java`
5. `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/client/WorkerAiHttpClient.java`

### Task B2: AI Entry Services Use Unified Resolver

目标：

- `Refinement`、`Discovery`、`Knowledge` 统一复用 `AiWorkerModelConfigResolver`。

文件范围，3 个文件：

1. `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/service/impl/AiRefinementApplicationServiceImpl.java`
2. `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/discovery/service/impl/DiscoveryAiApplicationServiceImpl.java`
3. `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/knowledge/service/impl/KnowledgeAiExtractionApplicationServiceImpl.java`

### Task C1: Workers Text Graph

目标：

- 用真实文本 graph 替换 placeholder。

文件范围，5 个文件：

1. `kuzhambu-workers/src/kuzhambu_workers/ai/graphs/text.py`
2. `kuzhambu-workers/src/kuzhambu_workers/ai/graph_registry.py`
3. `kuzhambu-workers/src/kuzhambu_workers/ai/graphs/basic.py`
4. `kuzhambu-workers/tests/test_graph_registry.py`
5. `kuzhambu-workers/tests/test_worker_e2e_ai_usecase_classics.py`

### Task C2: Workers Text Protocol Validation

目标：

- 收紧 workers 文本调用成功条件。

文件范围，3 个文件：

1. `kuzhambu-workers/src/kuzhambu_workers/api/ai_routes.py`
2. `kuzhambu-workers/tests/test_worker_e2e_ai_usecase_classics.py`
3. `kuzhambu-workers/tests/test_ai_routes.py`

### Task D: Classics Candidate Apply And Acceptance

目标：

- 验收 `translate` / `summary` 候选应用到正式内容。

文件范围，4 个文件：

1. `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`
2. `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/content/ClassicsContentAdminControllerTest.java`
3. `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/refinement/service/impl/AiRefinementApplicationServiceImplTest.java`

### Task D2: AI Refinement Task Interface And Orchestration

目标：

- 新增 `task/add`、`task/get`、`task/page`、`task/cancel`，并把 worker 执行结果回写到任务台账。

文件范围，5 个文件：

1. `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/AiRefinementTaskController.java`
2. `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/request/AiRefinementTaskRequests.java`
3. `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/response/AiRefinementTaskResponses.java`
4. `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/service/impl/AiRefinementTaskApplicationServiceImpl.java`
5. `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/test/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/AiRefinementTaskControllerTest.java`

### Task D3: AI Refinement Task Expiration Cleanup

目标：

- 为 `ai_refinement_task` 增加计划任务清理：先超时收口，再删除终态历史任务。

文件范围，5 个文件：

1. `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/service/AiRefinementTaskCleanupService.java`
2. `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/service/impl/AiRefinementTaskCleanupServiceImpl.java`
3. `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/refinement/persistence/mapper/AiRefinementTaskMapper.java`
4. `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/AiRefinementTaskController.java`
5. `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/refinement/service/impl/AiRefinementTaskCleanupServiceImplTest.java`

### Task E1: Admin Web Refinement Service And Types

目标：

- 为 Admin Web 新增精修任务服务与请求类型。

文件范围，3 个文件：

1. `kuzhambu-apps/admin-web/src/pages/classics/common/ai-refinement-task-types.ts`
2. `kuzhambu-apps/admin-web/src/pages/classics/common/ai-refinement-task-service.ts`
3. `kuzhambu-apps/admin-web/src/pages/classics/common/ai-refinement-task-service.test.ts`

### Task E2: Admin Web Sancai Actions

目标：

- 在三才图会详情页接入 `translate` 和 `summary` 的任务创建与轮询，并复用现有候选面板完成应用后刷新。

文件范围，3 个文件：

1. `kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-panel.tsx`
2. `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
3. `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`

### Task E3: Admin Web Wangqi And Ming Summary Actions

目标：

- 在汪耆文稿和明代风俗详情页接入 `summary` 的任务创建与轮询，并在应用候选后刷新详情。

文件范围，5 个文件：

1. `kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-panel.tsx`
2. `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.tsx`
3. `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.test.tsx`
4. `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.tsx`
5. `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.test.tsx`

### Task F: Coverage Update And Closure

目标：

- 更新实现覆盖文档，执行全域验证，并在任务完成后清理 RUNBOOK。

文件范围，4 个文件：

1. `docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`
2. `docs/40-readiness/WORKERS-IMPLEMENTATION-COVERAGE.md`
3. `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
4. `docs/30-designs/RUNBOOK-AI-WORKERS-CLASSICS-TEXT-CLOSURE.md`

## 8. Acceptance Rules

### 8.1 translate

- 只验收 `SANCAI_ENTRY.translate`
- 成功结果必须为非空 `TEXT`
- 应用后必须更新：
  - `translationText`
  - `translationStatus = READY`
- 必须生成 `AI_APPLIED` 版本

### 8.2 summary

- 验收：
  - `SANCAI_ENTRY.summary`
  - `WANGQI_DOCUMENT.summary`
  - `MING_CUSTOMS.summary`
- 成功结果必须为非空 `TEXT`
- 不限制长度
- 必须满足：
  - 可直接展示
  - 不输出“摘要：”前缀
  - 不重复标题
  - 不机械复述整段原文
  - 不编造信息

### 8.3 workers text call

- 仅当上游返回可解析 JSON 且存在非空 `choices[0].message.content` 时视为成功
- 空字符串、缺字段、非法 JSON 统一按 `WORKER_RESULT` 失败处理

### 8.4 refinement task cleanup

- `PENDING`、`RUNNING` 超过 `12` 小时不得直接删除，必须先自动收口为 `FAILED/TASK_EXPIRED`
- `SUCCEEDED`、`FAILED`、`PARTIAL`、`CANCELLED` 的任务在终态保留 `12` 小时后允许删除
- 删除任务不得级联删除 `ai_call_record`、`ai_candidate`
- `task/get` 查询已清理任务时必须返回稳定业务提示

## 9. Verification

Java 验证建议用单线程 Maven：

```sh
cd kuzhambu-servers
mvn -T 1 -pl biz/ai/kuzhambu-ai-application -am spotless:apply
mvn -T 1 -pl biz/ai/kuzhambu-ai-application -am -Dtest=AiRefinementApplicationServiceImplTest,AiInvokeResultTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -T 1 -pl biz/ai/kuzhambu-ai-infra -am -Dtest=AiInvocationRepositoryIT,PromptRepositoryIT -Dsurefire.failIfNoSpecifiedTests=false test
mvn -T 1 -pl biz/classics/kuzhambu-classics-application -am -Dtest=ClassicsContentApplicationServiceAiCandidateTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -T 1 -pl biz/classics/kuzhambu-classics-interface -am -Dtest=ClassicsContentAdminControllerTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Workers 验证：

```sh
cd kuzhambu-workers
.venv/bin/python -m ruff format src/kuzhambu_workers tests
.venv/bin/python -m ruff check src/kuzhambu_workers tests
.venv/bin/python -m pytest tests/test_graph_registry.py tests/test_ai_routes.py tests/test_worker_e2e_ai_usecase_classics.py -p no:capture
```

Admin Web 验证：

```sh
cd kuzhambu-apps
npm --workspace admin-web run format
npm run format:check
npm run lint
npm run test -- --runInBand admin-web/src/pages/classics/common/ai-refinement-task-service.test.ts admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx admin-web/src/pages/classics/wangqi/wangqi-page.test.tsx admin-web/src/pages/classics/ming-customs/ming-customs-page.test.tsx
npm run build
```

## 10. Branch

当前分支：

- `feat/ai-workers-classics-text-closure`
