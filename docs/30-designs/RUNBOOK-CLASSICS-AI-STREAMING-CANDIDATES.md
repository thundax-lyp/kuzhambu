# Classics AI 流式候选展示闭环 RUNBOOK

## 目标态

Classics 后台三才图会视觉资产 AI 任务形成完整闭环：

- 用户点击 `创建图片理解任务` 或 `创建生图任务` 后，页面立即出现该任务的流式过程卡片。
- 过程卡片实时展示阶段、增量内容、warning、失败原因和最终完成提示。
- 流式 `completed` 只表示过程流结束；页面必须再调用 `task/get` 确认最终 `status`、`candidateId`、`resultFormat`、`resultPreview`。
- `task/get` 返回 `SUCCEEDED` 且存在 `candidateId` 后，页面刷新 `AI 候选确认` 区，最终结果进入候选区。
- `FAILED`、`PARTIAL`、`CANCELLED` 展示失败原因；可重试时点击 `重试` 创建一条新任务。

## 交付范围

本次只做三才图会视觉资产流式候选：

- `SANCAI_ENTRY + image_analysis`
- `SANCAI_ENTRY + image_gen`

保持现状：

- `translate`、`summary`、`tags`、`qa`、`fusion`、`visual` 继续使用现有任务列表和轮询体验。
- 王圻、明代习俗页面不接入流式过程面板。
- workers 不新增业务写入、回调或数据库访问。

## 数据结构

### 数据库

不新增数据库字段，不新增表。

复用 `ai_refinement_task` 现有字段，字段定义已存在于 `AiRefinementTaskDO` 和 `AiRefinementTaskMapper.insertTask`：

| 字段 | 目标用途 |
| --- | --- |
| `task_id` | 前端订阅 `/stream?taskId=...` 和后续 `task/get` 的稳定任务号 |
| `capability` | 前端判断是否展示流式过程；本轮只认 `image_analysis`、`image_gen` |
| `content_type` | 本轮固定 `SANCAI_ENTRY` |
| `content_id` | 三才条目 ID |
| `object_id` | 视觉资产 ID；`image_analysis`、`image_gen` 必须写入当前视觉资产 ID |
| `request_id` | 本次 worker 调用 ID；重试必须重新生成 |
| `trace_id` | 本次链路 ID；重试必须重新生成 |
| `status` | `PENDING`、`RUNNING`、`SUCCEEDED`、`FAILED`、`PARTIAL`、`CANCELLED` |
| `call_id` | 成功或部分成功时关联 `ai_call_record.call_id` |
| `candidate_id` | 成功生成候选时关联 `ai_candidate.candidate_id` |
| `failure_stage` | 失败阶段，前端直接展示 |
| `error_type` | 失败类型，前端展示并判断是否可重试 |
| `error_message` | 失败详情，前端直接展示 |
| `result_format` | 候选结果格式；`image_analysis` 固定 `MARKDOWN`，`image_gen` 固定 `ARTIFACT` |
| `result_preview` | 任务卡片摘要，最大长度沿用 application 层现有 `500` 字符截断 |
| `stream_enabled` | 是否允许前端订阅流式过程；本轮 `image_analysis`、`image_gen` 为 `true` |
| `requested_at` | 任务创建时间 |
| `started_at` | 任务开始执行时间 |
| `completed_at` | 任务成功、失败或部分失败终态时间 |
| `cancelled_at` | 任务取消时间 |

复用 `ai_candidate` 现有字段，成功任务必须写入候选：

| 字段 | `image_analysis` 写入 | `image_gen` 写入 |
| --- | --- | --- |
| `candidate_id` | 新候选 ID | 新候选 ID |
| `call_id` | 本次调用记录 ID | 本次调用记录 ID |
| `capability` | `image_analysis` | `image_gen` |
| `content_type` | `SANCAI_ENTRY` | `SANCAI_ENTRY` |
| `content_id` | 三才条目 ID | 三才条目 ID |
| `object_id` | 视觉资产 ID | 视觉资产 ID |
| `artifact_reference_json` | `null` | Java AI 域转存或临时产物摘要 JSON；不得只保存 workers 临时 URL |
| `result_format` | `MARKDOWN` | `ARTIFACT` |
| `result_payload` | 图片理解 Markdown | 文件类结果摘要 JSON 或可供候选编辑器展示的 artifact payload |
| `status` | `PENDING` | `PENDING` |
| `prompt_version_id` | 本次 prompt 版本 ID | 本次 prompt 版本 ID |
| `model_name` | 本次模型名 | 本次模型名 |
| `failure_stage` | `null` | `null` |
| `error_type` | `null` | `null` |
| `error_message` | `null` | `null` |
| `requested_at` | 候选生成时间 | 候选生成时间 |
| `applied_at` | `null` | `null` |
| `rejected_at` | `null` | `null` |

### Java 响应字段

`AiRefinementResponses.TaskDetailResponse` 需要补充：

| JSON 字段 | Java 字段 | 类型 | 说明 |
| --- | --- | --- | --- |
| `streamEnabled` | `streamEnabled` | `Boolean` | 前端是否显示流式订阅入口 |

`AiStreamEventResult` 作为 SSE 事件 payload，字段口径固定为：

| JSON 字段 | 类型 | 事件 | 说明 |
| --- | --- | --- | --- |
| `eventType` | `string` | 全部 | `started`、`delta`、`progress`、`warning`、`error`、`completed` |
| `eventId` | `string` | 全部 | 单个事件 ID |
| `requestId` | `string` | 全部 | 与任务 `requestId` 一致 |
| `traceId` | `string` | 全部 | 与任务 `traceId` 一致 |
| `stage` | `string` | 全部 | 展示阶段，例如 `worker_request`、`model_stream`、`completed` |
| `timestamp` | `string` | 全部 | ISO 时间 |
| `deltaText` | `string` | `delta` | 增量文本或 Markdown |
| `status` | `string` | `completed`、`error` | 事件侧状态 |
| `resultFormat` | `string` | `completed` | workers 最终结果格式 |
| `resultPayload` | `string` | `completed` | workers 最终文本或结构化 payload 字符串 |
| `artifactReferenceJson` | `string` | `completed` | 文件类临时产物摘要 |
| `usage` | `object` | `completed` | token、耗时、成本摘要 |
| `errorType` | `string` | `error` | 失败类型 |
| `errorMessage` | `string` | `error` | 失败详情 |
| `failureStage` | `string` | `error` | 失败阶段 |
| `fallbackUsed` | `boolean` | `completed`、`error` | 是否使用备用服务 |

### 前端类型

`AiRefinementTaskRecord` 增加：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `streamEnabled?: boolean | null` | boolean | 是否可订阅流式过程 |

新增前端事件类型放在 `ai-refinement-task-types.ts`：

```ts
export type AiRefinementStreamEventType =
    "started" | "delta" | "progress" | "warning" | "error" | "completed" | string;

export interface AiRefinementStreamEventRecord {
    eventType: AiRefinementStreamEventType;
    eventId?: string | null;
    requestId?: string | null;
    traceId?: string | null;
    stage?: string | null;
    timestamp?: string | null;
    deltaText?: string | null;
    status?: string | null;
    resultFormat?: string | null;
    resultPayload?: string | null;
    artifactReferenceJson?: string | null;
    errorType?: string | null;
    errorMessage?: string | null;
    failureStage?: string | null;
    fallbackUsed?: boolean | null;
}
```

## 用户界面

### 三才图会视觉资产面板

位置：`SancaiEntryModel` 的 `afterForm` 区域，现有 `AI 精修任务` 卡片和 `AI 候选确认` 卡片之间。

新增控件：

- 卡片标题：`AI 流式过程`
- 卡片 aria-label：`三才图会 AI 流式过程`
- 任务状态 Tag：显示 `RUNNING`、`SUCCEEDED`、`FAILED`、`PARTIAL`、`CANCELLED`
- 阶段文本：显示最新 `stage`
- 增量内容预览：使用只读文本区域或 Markdown 文本块，累积展示 `deltaText`
- Warning Alert：有 `warning` 事件时展示 `warning` 内容
- Error Alert：有 `error` 事件或 `task/get` 返回失败时展示 `failureStage / errorType / errorMessage`
- `查看候选` 按钮：任务成功且存在 `candidateId` 时展示，点击后刷新候选 TanStack Query 缓存，并聚焦到 `AI 候选确认` 卡片
- `重试` 按钮：任务失败且可重试时展示，点击后调用现有重试逻辑
- `关闭过程` 按钮：只隐藏当前过程卡片，不取消后端任务

新增前端状态：

| 状态 | 文件 | 用途 |
| --- | --- | --- |
| `streamingRefinementTask` | `use-sancai-entry-panel-state.ts` | 当前展示过程的任务详情 |
| `streamEvents` | `use-sancai-entry-panel-state.ts` | 当前任务收到的 SSE 事件列表 |
| `isStreamingRefinementTask` | `use-sancai-entry-panel-state.ts` | 当前 stream 是否连接中 |
| `streamErrorText` | `use-sancai-entry-panel-state.ts` | fetch/SSE 解析失败时的页面错误文案 |
| `candidatePanelRef` | `sancai-entry-panel.tsx` | `查看候选` 后聚焦 `AI 候选确认` 卡片 |

用户操作：

1. 用户选择三才条目并打开编辑弹窗。
2. 用户在视觉资产版本区选择一个有原图的视觉资产。
3. 用户点击 `创建图片理解任务` 或 `创建生图任务`。
4. 创建成功后，页面展示 `AI 流式过程` 卡片，并自动订阅该 `taskId`。
5. `delta` 到达时，增量内容预览持续追加。
6. `completed` 到达时，前端调用 `getTask({ taskId })`。
7. `getTask` 返回 `SUCCEEDED + candidateId` 后，页面刷新 `AI 候选确认`。
8. 用户在 `AI 候选确认` 中编辑候选内容，然后点击 `应用` 或 `拒绝`。
9. 应用或拒绝后，刷新条目详情、视觉资产列表和候选区。

失败操作：

1. 任务卡片展示 `失败原因` Alert。
2. 若任务状态是 `FAILED`、`PARTIAL` 或 `CANCELLED`，且页面能重新构造视觉资产输入快照，则展示 `重试`。
3. 用户点击 `重试` 后，新建任务；旧任务保留在任务列表。

## 小任务拆分

### 小任务 1：后端任务响应和 stream 订阅入口

目标：Admin Web 能对支持流式的任务建立 Java SSE 订阅，并能从 `task/get` 识别 `streamEnabled`。

文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/AiRefinementTaskController.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/response/AiRefinementResponses.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/assembler/AiRefinementInterfaceAssembler.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/test/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/AiRefinementTaskControllerTest.java`

要求：

- `TaskDetailResponse` 增加 `streamEnabled`。
- `AiRefinementInterfaceAssembler.toTaskDetailResponse` 映射 `AiRefinementTask.isStreamEnabled()`。
- `AiRefinementTaskController` 增加 `GET /api/ai/refinement/task/stream?taskId=...`。
- stream 接口权限使用 `@HasPermission("ai:refinement:view")`。
- stream 接口响应 `text/event-stream`。
- 测试覆盖路径、权限、返回 content type。

完成定义：

- `task/get` JSON 包含 `streamEnabled`。
- `GET /api/ai/refinement/task/stream?taskId=1` 在 controller 测试中匹配到 `stream` handler。
- 无 `ai:refinement:view` 权限时不能订阅。

### 小任务 2：后端任务执行区分流式和同步

目标：`image_analysis`、`image_gen` 通过 worker SSE 执行，最终写入任务终态和候选。

文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/service/AiRefinementTaskApplicationService.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/service/impl/AiRefinementTaskApplicationServiceImpl.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/support/ClassicsAiWorkerUsecaseResolver.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/refinement/service/impl/AiRefinementTaskApplicationServiceImplTest.java`

要求：

- 新增判断：`SANCAI_ENTRY + image_analysis`、`SANCAI_ENTRY + image_gen` 设置 `streamEnabled = true`。
- `addTask` 保存任务时必须写入 `streamEnabled`。
- 流式任务进入 `RUNNING` 后发布 `started`。
- 收到 worker `delta`、`progress`、`warning` 时只发布展示事件，不创建候选。
- 收到 worker `completed` 时，用 `resultPayload` 或 `artifactReferenceJson` 生成 `AiCandidateResult`，再调用现有 `applyResult` 写终态。
- worker `error`、断流未 completed、协议异常或超时，写入 `FAILED`，且 `candidateId = null`。
- 测试覆盖成功、error、断流未 completed 三条路径。

完成定义：

- `image_analysis` 成功后 `ai_refinement_task.status=SUCCEEDED`、`stream_enabled=1`、`candidate_id` 非空。
- `image_analysis` 对应 `ai_candidate.status=PENDING`、`result_format=MARKDOWN`、`result_payload` 为 Markdown。
- `image_gen` 成功后 `ai_refinement_task.status=SUCCEEDED`、`stream_enabled=1`、`candidate_id` 非空。
- `image_gen` 对应 `ai_candidate.status=PENDING`、`result_format=ARTIFACT`、`artifact_reference_json` 非空。
- 断流未 completed 时 `ai_refinement_task.status=FAILED`、`candidate_id` 为空。

### 小任务 3：Worker SSE 客户端解析

目标：AI infra 可以稳定消费 workers SSE，并把事件转换为 `AiStreamEventResult`。

文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/client/WorkerAiClient.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/client/WorkerAiHttpClient.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/client/dto/WorkerAiDtos.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/test/java/com/thundax/kuzhambu/ai/infra/client/WorkerAiHttpClientTest.java`

要求：

- `WorkerAiClient` 增加 stream 方法，输入使用现有 `AiInvokeCommand`。
- `WorkerAiHttpClient` 复用 HMAC 头、timeout、path 解析。
- 解析 SSE event 名称和 data JSON。
- 支持事件：`started`、`delta`、`progress`、`warning`、`error`、`completed`。
- `completed` 后关闭连接。
- `error` 后关闭连接并返回失败事件。
- 连接结束但未收到 `completed` 或 `error` 时，调用方必须能识别为协议失败。

完成定义：

- 测试输入包含 `event: delta`、`data: {...}` 时输出 `AiStreamEventResult.eventType=delta`。
- 测试输入包含 `event: completed` 时输出 completed 事件并结束消费。
- 测试输入提前 EOF 且没有 completed/error 时，application 能得到失败信号。

### 小任务 4：前端 stream 服务和过程组件

目标：前端有可复用的 stream 订阅服务和过程展示组件。

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/common/ai-refinement-task-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/ai-refinement-task-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-refinement-stream-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/common/ai-refinement-task-service.test.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-refinement-stream-panel.test.tsx`

要求：

- `AiRefinementTaskRecord` 增加 `streamEnabled?: boolean | null`。
- 新增 `AiRefinementStreamEventRecord` 类型。
- `ai-refinement-task-service.ts` 新增 `streamTask(taskId, handlers)`。
- `streamTask` 使用 `fetch` 订阅 `${ADMIN_API_BASE_URL}/ai/refinement/task/stream?taskId=${taskId}`，并设置 `Access-Token` 请求头；不得使用原生 `EventSource`，因为项目鉴权依赖自定义请求头。
- `streamTask` 使用 `ReadableStream` 逐行解析 SSE：识别 `event:`、`data:` 和空行分隔符。
- handlers 至少包含 `onEvent`、`onError`、`onClosed`。
- `streamTask` 返回 `{ close: () => void }`，组件卸载或点击 `关闭过程` 时必须中断 fetch。
- `AiRefinementStreamPanel` props：
  - `task: AiRefinementTaskRecord`
  - `events: AiRefinementStreamEventRecord[]`
  - `isStreaming: boolean`
  - `onRetry: (task) => void`
  - `onRefreshCandidate: (task) => void`
  - `onClose: () => void`
- 控件：
  - `Card` title `AI 流式过程`
  - `Tag` 显示任务状态
  - `Typography.Text` 显示最新阶段
  - 只读内容区域显示 delta 累积文本
  - `Alert` 显示 warning 和失败原因
  - `Button` `查看候选`
  - `Button` `重试`
  - `Button` `关闭过程`

完成定义：

- service 测试断言请求 URL 为 `${ADMIN_API_BASE_URL}/ai/refinement/task/stream?taskId=8801`。
- service 测试断言请求头包含当前 `Access-Token`。
- 组件测试能通过 `getByLabelText("三才图会 AI 流式过程")` 定位卡片。
- 组件测试能看到 delta 累积文本和 `失败原因` Alert。
- 点击 `关闭过程` 调用 `onClose`，不调用取消任务服务。

### 小任务 5：三才视觉资产页面接入

目标：三才视觉资产 `image_analysis`、`image_gen` 创建后展示流式过程，完成后刷新候选区。

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/hooks/use-sancai-entry-panel-state.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.css`

要求：

- `createRefinementTaskMutation.onSuccess` 拿到 `TaskAcceptedResponse.taskId` 后，如果 capability 是 `image_analysis` 或 `image_gen`，调用 `getTask({ taskId })` 获取含 `streamEnabled` 的详情，并记录为当前流式任务。
- 只对 `task.streamEnabled === true` 且 capability 属于 `image_analysis`、`image_gen` 的任务打开 stream 面板。
- `completed` 事件到达后调用 `getTask({ taskId })`。
- `getTask` 返回 `SUCCEEDED + candidateId` 后刷新：
  - `["ai", "candidates", "SANCAI_ENTRY", selectedEntryId, selectedVisualAssetId]`
  - 当前页面已有 refinement task TanStack Query 缓存
  - `["classics", "sancai", "entries", "visual-assets", selectedEntryId]`
- `查看候选` 按钮触发候选 TanStack Query 缓存失效，并聚焦到 `AI 候选确认` 卡片。
- `重试` 按钮复用 `retryRefinementTask(task)`；重试必须生成新的 `requestId` 和 `traceId`。
- `关闭过程` 只清理前端当前面板状态，不调用 `cancelTask`。
- `sancai-page.css` 增加流式过程卡片样式，类名前缀使用 `sancai-`。
- 测试覆盖：
  - 点击 `创建图片理解任务` 后出现 `AI 流式过程`。
  - 收到 delta 后展示增量内容。
  - completed 后调用 `getTask` 并刷新候选。
  - error 后展示 `失败原因` 和 `重试`。
  - 点击 `关闭过程` 不调用取消接口。

完成定义：

- 点击 `创建图片理解任务` 后，页面展示 `AI 流式过程` 卡片。
- 点击 `创建生图任务` 后，页面展示 `AI 流式过程` 卡片。
- completed 后候选区刷新，`AI 候选确认` 可见对应 `image_analysis` 或 `image_gen` 候选。
- 点击 `查看候选` 后焦点进入 `AI 候选确认` 卡片。
- 点击 `重试` 后 `createTask` 再次调用，且请求体中的 `requestId`、`traceId` 与原任务不同。

## 验收口径

后端：

- `task/get` 返回 `streamEnabled`。
- `/api/ai/refinement/task/stream?taskId=...` 只返回展示事件，不直接暴露 workers 地址。
- `image_analysis` 成功后：`status=SUCCEEDED`、`candidate_id` 非空、`result_format=MARKDOWN`、`result_preview` 非空。
- `image_gen` 成功后：`status=SUCCEEDED`、`candidate_id` 非空、文件类结果不使用 workers 临时 URL 作为正式结果。
- worker 断流未 completed：`status=FAILED`、`failure_stage=WORKER_STREAM` 或 `WORKER_RESULT`、`candidate_id` 为空。

前端：

- `创建图片理解任务` 和 `创建生图任务` 后出现 `AI 流式过程` 卡片。
- 过程卡片能展示增量文本、最新阶段、warning、失败原因。
- 成功后 `AI 候选确认` 出现对应候选。
- 失败后可点击 `重试` 新建任务。
- 页面刷新后，任务列表和候选区仍能通过 `task/page`、`candidate/list` 恢复最终状态。

## 验证命令

后端：

```sh
cd kuzhambu-servers
mvn -pl biz/ai/kuzhambu-ai-interface,biz/ai/kuzhambu-ai-application,biz/ai/kuzhambu-ai-infra -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/ai/kuzhambu-ai-interface,biz/ai/kuzhambu-ai-application,biz/ai/kuzhambu-ai-infra,biz/classics/kuzhambu-classics-application -am test
```

前端：

```sh
cd kuzhambu-apps
npm --workspace kuzhambu-admin-web run format
npm run format:check
npm run lint
npm --workspace kuzhambu-admin-web run test
```

## 收口

- 实现完成并通过验收后删除本 RUNBOOK。
- 不把本 RUNBOOK 引用沉淀到稳定治理文档。
- PR 说明必须包含：流式过程展示、候选生成、失败重试、未新增 schema 字段、验证命令结果。
