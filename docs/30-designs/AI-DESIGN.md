# AI Design

## Purpose

本文档定义 AI 域设计，覆盖 AI 服务配置、模型能力、提示词管理、AI 调用、候选结果和 AI 内容生产流程。

AI 域是所有 AI 能力调用的治理入口。Java 业务域通过 AI application 能力发起 AI 调用；AI application 选择模型、提示词和变量，记录调用和候选结果，再通过 Python workers 执行具体 AI graph。

## Module

```text
kuzhambu-servers/biz/ai/
  kuzhambu-ai-interface/
  kuzhambu-ai-application/
  kuzhambu-ai-domain/
  kuzhambu-ai-infra/
```

## Business Boundary

AI 拥有模型、能力映射、提示词、调用记录、候选结果和 AI 执行任务。AI 不拥有正式古籍内容、标签治理、图谱结果或问答会话。

Workers 不拥有 AI 配置、提示词、调用记录或候选结果。Classics、Knowledge 和 Discovery 不得直接绕过 AI 域调用 workers 的 AI 接口。

## DDD Model

- `AiServiceConfig`
- `AiModel`
- `AiCapability`
- `AiCapabilityMapping`
- `PromptTemplate`
- `PromptVersion`
- `PromptVariable`
- `AiActionStatus`
- `AiCallRecord`
- `AiCandidate`
- `AiBatchJob`
- `ImageUnderstandingResult`
- `EntrySplitCandidate`
- `AiWorkerInvocation`
- `AiStreamEvent`

## Data Model

表名前缀统一使用 `ai_`。

核心表：

- `ai_service_config`
- `ai_model`
- `ai_model_check_record`
- `ai_capability`
- `ai_capability_mapping`
- `ai_prompt_template`
- `ai_prompt_version`
- `ai_prompt_variable`
- `ai_action_status`
- `ai_call_record`
- `ai_candidate`
- `ai_batch_job`
- `ai_image_understanding`
- `ai_entry_split_candidate`

数据模型必须记录 worker 调用所需的稳定追踪信息，包括模型、服务、能力、提示词版本、请求标识、链路标识、耗时、用量、失败类型和降级状态。stream 片段不是业务事实，默认不逐片段持久化；最终结果、失败或部分失败状态必须进入调用记录。

提示词模板能力归属固定规则：

- `PromptTemplate.capability` 是模板的运行时契约归属键，一旦创建不得修改。
- 模板名称、说明和启用状态可以更新；提示词正文、输出 schema 和变量快照通过新增 `PromptVersion` 表达。
- 更新已有模板时，请求中的 capability 必须与已保存模板一致；不一致时后端拒绝保存。
- `PromptVariable` 表示模板创建时的能力变量基线；更新提示词版本时不替换模板级变量列表，版本变量以 `PromptVersion.variablesSnapshotJson` 为准。
- 如需把提示词迁移到另一 capability，必须创建新的 `PromptTemplate`，不得复用原 template id。

AI 调用记录固定补充以下字段口径：

- `failureStage`
- `resultFormat`
- `resultPayload`
- `artifactReferenceJson`

AI 候选记录固定补充以下字段口径：

- `rejectedAt`
- `failureStage`
- `artifactReferenceJson`

文件类 AI 结果不得把大文件内容直接作为正式业务结果保存；只允许以 `artifactReferenceJson` 保存 Workers 返回的 `temporary artifact reference` 摘要。

排序字段规则：

- `ai_prompt_variable.priority` 和 `ai_entry_split_candidate.priority` 表达 AI 域内可信的后端内部全局排序权重，不表示父级对象内的第几个位置。
- 该值必须全局唯一；不同 `template_id` 或不同 `candidate_id` 下也不得复用同一个 `priority`。
- 查询仍可按父级过滤后使用 `priority asc` 展示局部列表，但排序权重的分配、去重和冲突处理以全局唯一为准。
- 前端协议不得读写 `priority`；排序时只提交对象 ID 顺序，由后端在事务内交换或重算内部排序权重。

## Application Layer

- `AiServiceConfigApplicationService`
- `AiModelApplicationService`
- `PromptApplicationService`
- `AiActionStatusApplicationService`
- `AiRefinementApplicationService`
- `AiBatchJobApplicationService`
- `AiWorkerInvocationApplicationService`

Application 层负责模型能力校验、提示词变量校验、主备切换、AI 调用编排、worker 请求构造、stream 转发、候选区管理、批量任务取消和统计记录。

Knowledge 抽取协作语义：

- `KnowledgeAiExtractionDomainService` 提供 `extractRelations`、`extractGraph`、`extractLineage` 三个明确业务动作。
- AI application 把 Knowledge 业务动作解析为稳定的 `operation + capability + workerPath`，并统一写入 `ai_call_record` 与 `ai_candidate`。
- `ai_candidate.result_payload` 是 Knowledge 抽取候选快照真相源；正式知识事实仍由 Knowledge 应用后写入。

调用流程：

1. 业务域通过 AI application 发起能力调用，并传入业务上下文快照。
2. AI application 校验 AI 功能动作、模型能力映射和当前生效提示词。
3. AI application 校验变量并渲染 messages，或构造经校验的 prompt 模板和变量。
4. AI application 创建调用记录或批量任务记录。
5. AI infra 通过 `WorkerAiClient` 调用 workers。
6. workers 返回同步结果或 stream 事件。
7. AI application 记录耗时、用量、失败类型和降级状态。
8. 文本类结果直接形成最终调用结果；文件类结果先保存 `temporary artifact reference`。
9. 文件类结果由 Java servers 根据 `temporary artifact reference` 下载并转存到 `Storage`。
10. 需要人工确认的结果进入 `AiCandidate`。
11. 正式内容、问答会话或图谱结果由对应业务域在确认或编排后写入。

批量流程：

- `AiBatchJob` 保存批量任务状态、总数、成功数、失败数、取消状态和失败原因摘要。
- AI application 将批量任务拆分为多个 worker 单元调用。
- 取消批量任务时，AI application 停止继续派发未开始的 worker 单元调用。
- workers 正在执行的单元调用不保存取消状态；返回成功、失败或超时后由 AI application 归档。
- 已完成结果必须保留。

Stream 流程：

- AI application 可以调用 workers 的 SSE 接口。
- AI application 可以把 `started`、`delta`、`progress`、`warning`、`error` 和 `completed` 事件转发给前端或调用方。
- 流式片段只用于展示过程，不直接写入正式内容。
- `completed` 事件或同步最终响应用于生成候选结果、问答消息或调用记录终态。
- stream 中断时，AI application 必须把调用记录为失败或部分失败，并允许重新发起完整调用。
- 流式调用必须以 `completed` 或 `error` 形成最终态；不得出现“只有 delta 没有最终结果”的业务闭环。

Admin Web 交互流程：

- Java servers 对前端的默认协议不是直接暴露 workers SSE，也不是默认使用 WebSocket。
- AI 能力默认采用 `createJob -> jobId -> get/page status` 的异步任务协议。
- workers 的 SSE 只表示 `AI domain -> workers` 的内部执行传输，不直接决定 `Admin Web -> Java` 的产品协议。
- 只有明确需要边生成边展示的能力，才在任务协议之上补充 Java 对前端的 SSE 订阅能力。
- 无论是否存在前端 SSE，任务台账、调用记录、候选结果和最终状态都必须先落到 AI 域本地表，再由前端读取。

默认前端任务协议：

- `POST /api/ai/refinement/task/add`
- `POST /api/ai/refinement/task/get`
- `POST /api/ai/refinement/task/page`
- `POST /api/ai/refinement/task/cancel`

可选流式订阅协议：

- `GET /api/ai/refinement/task/stream?taskId=...`

该订阅接口只用于展示 `RUNNING` 中的增量文本、阶段进度和 warning，不作为业务最终结果真相源。最终结果、失败信息、`callId`、`candidateId` 和可应用状态必须以 `task/get` 返回的数据为准。

默认任务协议详细流程：

1. Admin Web 调用 `POST /api/ai/refinement/task/add`，传入 `contentType`、`contentId`、`capability`、`requestId`、`requestedBy` 和业务参数 JSON；前端默认不传 `modelId`、`promptVersionId` 或已渲染 prompt。
2. AI interface 校验权限、请求字段、AI 动作开关和内容类型与 capability 的匹配关系。
3. AI application 创建 `ai_refinement_task`，初始状态为 `PENDING`。
4. AI application 根据 capability 读取第一个启用的 `ai_business_config`，从业务配置中取得 `model_id`、`prompt_template_id` 和 `default_params_json`。
5. AI application 根据 `prompt_template_id` 读取当前 prompt version，把业务参数 JSON 按 `{{variableName}}` 注入提示词模板，生成本次调用的 `promptMessagesJson`、`promptVariablesJson` 和 `promptVersionId`。
6. AI application 合并模型默认参数和业务配置辅助参数；业务配置参数覆盖模型默认参数。
7. AI application 保留业务 `capability` 作为业务配置、调用记录和候选归档字段，同时把本次调用映射为 workers canonical `workerCapability`，例如 `classics_summary -> summary`、`knowledge_graph_extract -> knowledge_graph`。
8. AI application 把任务状态更新为 `RUNNING`，并以解析后的模型、提示词、参数和 `workerCapability` 开始本次 worker 调用。
9. 对同步 capability，AI application 等待 worker JSON 完成，再统一写入：
   - `ai_call_record`
   - `ai_candidate`
   - `ai_refinement_task.callId`
   - `ai_refinement_task.candidateId`
   - `ai_refinement_task.status`
10. 对流式 capability，AI application 内部消费 workers SSE；`delta/progress/warning` 只更新任务进度快照，`completed/error` 才形成最终态。
11. 成功时，AI application 把任务状态更新为 `SUCCEEDED`，并写入 `candidateId`、`resultFormat`、`resultPreview`、`completedAt`。
12. 失败时，AI application 把任务状态更新为 `FAILED` 或 `PARTIAL`，并写入 `failureStage`、`errorType`、`errorMessage`、`completedAt`。
13. Admin Web 通过 `POST /api/ai/refinement/task/get` 或 `POST /api/ai/refinement/task/page` 轮询任务状态。
14. 当任务进入 `SUCCEEDED` 且存在 `candidateId` 后，Admin Web 刷新候选面板，用户再通过候选应用接口把结果落到正式内容。
15. `task/add` 的成功只表示“任务已受理”，不表示候选已生成；候选是否可用必须以后续 `task/get` 返回的最终状态判断。

运行时接口协议见 [`AI-RUNTIME-INTERFACE.md`](../20-interfaces/AI-RUNTIME-INTERFACE.md)。该协议固定：

- Admin Web 和跨域 facade 默认只传业务 capability、内容标识和业务参数 JSON。
- `modelId`、`promptVersionId`、`promptMessagesJson`、`workerCapability` 和 `workerPath` 不属于默认外部运行时协议。
- Java AI 域对外响应和持久化字段始终使用业务 capability；workers canonical capability 只存在于 Java AI 域到 workers 的内部请求。

精修任务失效清理流程：

1. 清理范围只包含 `ai_refinement_task`。
2. 不清理：
   - `ai_call_record`
   - `ai_candidate`
   - `ai_batch_job`
3. 清理由 Java servers 计划任务执行，建议频率为每 `1` 小时一次。
4. 默认失效阈值固定为 `12` 小时。
5. 对 `status in (PENDING, RUNNING)` 且 `requestedAt < now - 12h` 的任务，不直接删除，先自动收口为失败终态：
   - `status = FAILED`
   - `failureStage = WORKER_RESULT`
   - `errorType = TASK_EXPIRED`
   - `errorMessage = 任务超过 12 小时未完成，系统自动关闭`
   - `completedAt = now`
6. 对 `status in (SUCCEEDED, FAILED, PARTIAL, CANCELLED)` 的终态任务，当终态时间早于 `now - 12h` 时执行物理删除。
7. 终态时间字段取值规则：
   - 优先 `completedAt`
   - 若为空则取 `cancelledAt`
   - 若仍为空则回退 `requestedAt`
8. 删除 `ai_refinement_task` 不得级联删除 `ai_call_record` 或 `ai_candidate`；调用记录和候选快照继续作为追踪真相源保留。
9. Admin Web 轮询到已被清理的 `taskId` 时，Java 应返回稳定业务提示，例如“任务不存在或已过期清理”，而不是前端无限重试。

前端 SSE 的适用边界：

- 默认不为 `translate`、`summary`、`tags`、`qa` 提供前端 SSE，因为这些能力不需要逐 token 展示。
- `answer_generation`、`image_analysis`、`image_gen` 或未来其他明显长时任务，可以在任务协议之上增加 `task/stream`。
- 即使存在 `task/stream`，前端断线重连后也必须退回 `task/get` 拉最终状态，不依赖流恢复。

不默认选择 WebSocket 的原因：

- 当前 workers、AI 域和接口文档都围绕 HTTP + SSE 设计，没有现成 WebSocket 会话模型。
- WebSocket 会引入连接鉴权、重连、节点粘性和服务治理复杂度，但本批次 `translate/summary` 不需要这类实时性。
- 对管理后台任务，状态轮询更符合现有 `task/add -> task/page -> task/get -> task/apply` 交互风格。

失败阶段固定枚举：

- `REQUEST_VALIDATE`
- `WORKER_REQUEST`
- `WORKER_STREAM`
- `WORKER_RESULT`
- `ARTIFACT_DOWNLOAD`
- `STORAGE_PERSIST`
- `CANDIDATE_PERSIST`

`fallbackUsed` 只表示主服务不可用后切换到备用服务或备用模型继续执行，不表示提示词兜底、结果默认值或 UI 默认文案。

## Interface Layer

Admin 入口：

- AI 服务和模型配置。
- 模型检测历史。
- 能力映射。
- 提示词编辑、版本对比和回滚。
- AI 功能动作状态面板。
- AI 调用统计。

Classics 内容上下文入口：

- 翻译、摘要、标签、问答对、图片理解、条目拆分、视觉描述、信息融合和生图触发。
- 候选结果预览、编辑、确认和拒绝。

Knowledge 调用入口：

- 实体关系候选抽取。
- 图谱候选抽取。
- 世系图候选抽取。
- 候选结果应用不在 AI 域执行，而由 Knowledge 读取候选并落正式结果。

Discovery 调用入口：

- 查询理解。
- 查询改写。
- 回答生成和流式回答生成。

文件类结果规则：

- 图片、视频、ZIP、PDF 等文件类结果统一由 Workers 返回 `temporary artifact reference`。
- Java servers 必须根据该引用下载临时产物，并转存 `Storage`。
- 业务侧最终只认 `Storage` 结果，不认 Workers 临时引用。
- 对大文件转存必须支持 `multipart upload`，不得默认走一次性内存上传路径。

## Infrastructure Layer

- `WorkerAiClient` 适配 Python workers 内部 HTTP、SSE 和临时 artifact 下载接口。
- AI 域向 workers 传入主服务或备用服务的模型配置、调用参数、渲染后 messages、结构化输出 schema 和完整上下文。
- Knowledge 抽取能力通过统一 workers AI 接口 `/internal/ai/invoke` 或 `/internal/ai/stream` 执行；业务差异由 AI 域选择的业务配置、提示词版本、模型配置、辅助参数和输出 schema 表达，不再依赖 workers 业务专项 path。
- workers 内部执行 LangGraph；AI 域不直接依赖 workers 内部 graph 实现。
- AI 域与 workers 的协议见 [`WORKERS-AI-INTERFACE.md`](../20-interfaces/WORKERS-AI-INTERFACE.md)。
- Repository 持久化 AI 配置、提示词、调用记录和候选结果。
- 外部调用失败需要区分网络传输层失败、worker 协议失败、AI 语义层失败、artifact 下载失败、Storage 转存失败和输出格式失败。
- 网络传输层失败允许由 AI application 决策切换备用服务并重新调用 workers。
- AI 语义层失败不得由 infra 自动无限重试。

## Data Ownership

AI 是 `ai_*` 表的唯一写入方。正式内容写入由 Classics 或 Knowledge 在用户确认或业务编排后完成。

Discovery 问答会话、消息和来源由 Discovery 写入；QA trace 可以挂载 AI `callId` 作为调用追溯标识，但该 trace 仍归 Discovery 持有。Knowledge 标签、实体、关系、图谱版本和质量指标由 Knowledge 写入。workers 不写入任何业务表。

## Observability

- 记录 AI 调用延迟、失败、成本、服务降级状态和模型检测历史。
- AI Key 不输出到前端、日志或审计。
- 记录 worker 请求标识、链路标识、stream 是否完成、失败分类、失败阶段、降级状态和用量摘要。

## Acceptance

- AI 配置、模型、提示词和调用链路在一个业务域内闭合。
- AI 结果必须先进入候选区，确认后才影响正式内容。
- AI 域能通过 `WorkerAiClient` 调用 workers 同步和 SSE 接口。
- Classics、Knowledge 和 Discovery 均通过 AI application 使用 AI 能力，不直接依赖 workers。
