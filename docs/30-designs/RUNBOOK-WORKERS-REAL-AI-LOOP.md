# RUNBOOK Workers 真实 AI 执行闭环

## 目标

把 `kuzhambu-workers` 的 AI graph 从 placeholder 执行改为真实 OpenAI-compatible 模型调用闭环。

完成后，AI 域通过 workers 调用 Classics、Knowledge 和 Discovery usecase 时，workers 必须在单次无状态请求内完成真实模型调用、同步响应、SSE 流式响应、结构化输出解析、错误归一化、`usage` 和 `latencyMs` 返回。

## 已确认决策

- 本轮真实模型协议只支持 `modelConfig.apiSource == "OPENAI_COMPATIBLE"`。
- 本轮模型调用只使用 `/chat/completions`，不引入 OpenAI Responses API、vendor 私有 SDK 或图片生成专用协议。
- 本轮不实现真实 `image_gen`。`image_gen` 不得继续返回空 artifact，必须返回稳定错误。
- Workers 只返回 provider token usage 和本地 latency；`usage.costAmount` 固定保持 `"0.00"`，成本核算仍归 Java AI 域。
- 认证、请求解析和 path/service allowlist 失败继续使用现有 HTTP `400`、`401`、`403`。
- 模型执行失败返回 workers 业务失败响应或 SSE `error` 事件，由 Java AI 域记录调用失败。

## 范围

只允许改动以下生产文件：

- `kuzhambu-workers/src/kuzhambu_workers/ai/model_adapters.py`
- `kuzhambu-workers/src/kuzhambu_workers/ai/prompt_messages.py`
- `kuzhambu-workers/src/kuzhambu_workers/ai/structured_output.py`
- `kuzhambu-workers/src/kuzhambu_workers/ai/graphs/basic.py`
- `kuzhambu-workers/src/kuzhambu_workers/ai/graphs/text.py`
- `kuzhambu-workers/src/kuzhambu_workers/api/ai_routes.py`
- `kuzhambu-workers/src/kuzhambu_workers/core/errors.py`
- `kuzhambu-workers/src/kuzhambu_workers/schemas/ai.py`
- `kuzhambu-workers/src/kuzhambu_workers/schemas/common.py`
- `kuzhambu-workers/src/kuzhambu_workers/streaming/events.py`

允许新增以下生产文件：

- `kuzhambu-workers/src/kuzhambu_workers/ai/openai_compatible.py`
- `kuzhambu-workers/src/kuzhambu_workers/ai/usage.py`
- `kuzhambu-workers/src/kuzhambu_workers/ai/errors.py`

允许改动或新增以下测试文件：

- `kuzhambu-workers/tests/test_ai_openai_compatible.py`
- `kuzhambu-workers/tests/test_ai_streaming_model.py`
- `kuzhambu-workers/tests/test_ai_structured_output.py`
- `kuzhambu-workers/tests/test_ai_error_mapping.py`
- `kuzhambu-workers/tests/test_ai_routes.py`
- `kuzhambu-workers/tests/test_ai_usecase_routes.py`
- `kuzhambu-workers/tests/test_ai_usecase_routes_classics.py`
- `kuzhambu-workers/tests/test_ai_usecase_routes_discovery.py`
- `kuzhambu-workers/tests/test_ai_usecase_routes_knowledge.py`
- `kuzhambu-workers/tests/test_ai_usecase_routes_platform.py`
- `kuzhambu-workers/tests/test_graph_registry.py`
- `kuzhambu-workers/tests/test_prompt_messages.py`
- `kuzhambu-workers/tests/test_worker_e2e_ai.py`
- `kuzhambu-workers/tests/test_worker_e2e_ai_usecase_classics.py`
- `kuzhambu-workers/tests/test_worker_e2e_ai_usecase_discovery.py`
- `kuzhambu-workers/tests/test_worker_e2e_ai_usecase_security.py`

不改动：

- `kuzhambu-servers/`
- `kuzhambu-apps/`
- `deploy/`
- Java AI 域候选区、调用记录、成本核算和 Storage 持久化逻辑

## 前端影响

本任务没有前端改动。

- 不新增页面。
- 不新增菜单。
- 不新增按钮、输入框、开关、表格列、筛选项、弹窗或 toast。
- 不改变 Admin Web 或 Portal Web 的用户操作路径。
- 前端仍只消费 Java servers 返回的业务接口；不直接调用 workers。
- 若后续 Java servers 暴露更细的 AI 失败原因，前端展示颗粒度应另开任务定义到具体控件和操作，本 RUNBOOK 不处理。

前端验收口径：

- Admin Web：所有现有 AI 操作按钮的点击入口不变；本任务不新增或修改按钮文案、按钮 loading 状态、确认弹窗、结果弹窗、错误 toast 和表格状态列。
- Portal Web：搜索、问答和内容阅读控件不变；本任务不新增或修改搜索框、发送按钮、流式回答展示区、引用列表、错误提示和重试入口。
- 若实现过程中发现必须改变任一前端控件或操作，必须停止本 RUNBOOK，另开前端任务并精确描述控件名称、触发操作、空态、loading、成功态、失败态和可访问性要求。

## 数据结构变更

本任务不新增对外 HTTP JSON 字段，只改变现有字段的取值来源、校验规则和失败语义。新增的数据结构仅限 workers 内部 Python 类型。

### `schemas/ai.py`

`AiModelConfig` 字段保持现有对外字段名，不新增必填字段：

- `serviceRole: str`
- `apiSource: str`
- `baseUrl: str`
- `apiKey: str`
- `modelName: str`
- `capabilityTags: list[str]`
- `parameters: dict[str, Any]`
- `timeoutMs: int`

实现要求：

- `apiSource` 只接受 `"OPENAI_COMPATIBLE"` 进入真实调用。
- `baseUrl` 必须为 HTTP 或 HTTPS URL。
- `apiKey` 只在内存中用于请求头，不进入日志、错误 detail 或测试快照。
- `parameters` 禁止覆盖以下 provider payload 核心字段：
  - `model`
  - `messages`
  - `stream`
  - `response_format`

`AiInvokeResponse` 字段保持现有 JSON 契约：

- `requestId: str`
- `traceId: str`
- `status: WorkerStatus`
- `capability: AiCapability`
- `result: AiResult | None`
- `usage: UsageSummary`
- `failureStage: FailureStage | None`
- `fallbackUsed: bool`
- `artifactReference: ArtifactReference | None`
- `warnings: list[dict[str, Any]]`
- `error: WorkerErrorPayload | None`
- `errorType: str | None`
- `errorMessage: str | None`

成功响应字段要求：

- `status = "SUCCEEDED"`
- `result.format` 为 `TEXT`、`MARKDOWN` 或 `STRUCTURED`
- `result.payload` 不包含 OpenAI 原始 `choices` envelope
- `usage.latencyMs >= 0`
- `usage.inputTokens` 来自 provider `usage.prompt_tokens`，缺失时为 `0`
- `usage.outputTokens` 来自 provider `usage.completion_tokens`，缺失时为 `0`
- `usage.costAmount = "0.00"`
- `failureStage = null`
- `fallbackUsed = false`
- `artifactReference = null`
- `error = null`
- `errorType = null`
- `errorMessage = null`

失败响应字段要求：

- `status = "FAILED"`
- `result = null`
- `failureStage` 只允许 `WORKER_REQUEST`、`WORKER_STREAM` 或 `WORKER_RESULT`
- `fallbackUsed = false`
- `artifactReference = null`
- `error.type` 使用稳定 `WorkerErrorType`
- `error.code` 使用稳定错误 code
- `error.retryable` 必须按错误类型明确赋值
- `errorType == error.type`
- `errorMessage == error.message`

`AiResult` 字段保持：

- `format: ResultFormat`
- `payload: Any`

不同 `format` 的 `payload` 要求：

- `TEXT`：`payload` 必须是非空 `str`
- `MARKDOWN`：`payload` 必须是非空 `str`
- `STRUCTURED`：`payload` 必须是 JSON object 或 JSON array
- `ARTIFACT`：本轮 `image_gen` 不返回成功 artifact

### `schemas/common.py`

`UsageSummary` 字段保持现有 JSON 契约：

- `latencyMs: int`
- `inputTokens: int`
- `outputTokens: int`
- `costAmount: str`

实现要求：

- `latencyMs` 使用请求级 `time.monotonic()` 差值计算，单位毫秒。
- provider 未返回 usage 时，`inputTokens = 0` 且 `outputTokens = 0`。
- `costAmount` 固定为 `"0.00"`。

### `schemas/stream.py`

不新增事件类型。继续使用现有：

- `started`
- `delta`
- `progress`
- `artifact`
- `usage`
- `warning`
- `error`
- `completed`

SSE 成功流字段要求：

- `started`：包含 `eventId`、`requestId`、`traceId`、`stage`、`timestamp`
- `delta`：包含 `eventId`、`requestId`、`traceId`、`stage`、`timestamp`、`delta`
- `usage`：包含 `eventId`、`requestId`、`traceId`、`stage`、`timestamp`、`usage`
- `completed`：包含 `eventId`、`requestId`、`traceId`、`stage`、`timestamp`、`result`、`usage`，并在 `extra` 中包含 `status="SUCCEEDED"`、`fallbackUsed=false`

SSE 失败流字段要求：

- 必须发送 `error`
- 不得发送 `completed`
- `error` 事件在 `extra` 中包含 `status="FAILED"`、`failureStage`、`fallbackUsed=false`、`errorType`、`errorMessage`

### `ai/openai_compatible.py`

新增内部数据结构，不对外暴露 HTTP 契约：

`OpenAiChatCompletionRequest` 必须字段：

- `model: str`
- `messages: list[dict[str, str]]`
- `stream: bool`
- `response_format: dict[str, str] | None`
- `parameters: dict[str, Any]`

`OpenAiChatCompletionResult` 必须字段：

- `content: str`
- `usage: UsageSummary`
- `raw_finish_reason: str | None`

`OpenAiChatCompletionChunk` 必须字段：

- `delta: str`
- `usage: UsageSummary | None`
- `finish_reason: str | None`

字段映射：

- provider `choices[0].message.content` -> `OpenAiChatCompletionResult.content`
- provider `choices[*].delta.content` -> `OpenAiChatCompletionChunk.delta`
- provider `usage.prompt_tokens` -> `UsageSummary.inputTokens`
- provider `usage.completion_tokens` -> `UsageSummary.outputTokens`
- provider usage 缺失 -> token 字段为 `0`

## OpenAI-compatible 请求契约

目标请求：

```json
{
  "model": "modelConfig.modelName",
  "messages": [
    {"role": "system", "content": "system prompt"},
    {"role": "user", "content": "user prompt"}
  ],
  "stream": false,
  "response_format": {"type": "json_object"},
  "...modelConfig.parameters": "allowed values"
}
```

请求规则：

- URL：`modelConfig.baseUrl` 去掉末尾 `/` 后拼接 `/chat/completions`
- Header：
  - `Authorization: Bearer <modelConfig.apiKey>`
  - `Content-Type: application/json`
- `stream=false` 用于同步接口。
- `stream=true` 用于 SSE 接口。
- `response_format` 只在结构化输出或 `options.forceJson == true` 时发送。
- `modelConfig.parameters` 的键如果与核心字段冲突，返回 `MODEL_CONFIG_INVALID`。

## 结构化输出契约

触发条件：

- `request.outputSchema.type != "text"`，或
- `request.options.forceJson == true`，或
- capability 是 `tags`、`qa`、`split`、`query_understanding`、`knowledge_graph`、`relation_extraction`、`lineage_extraction`、`prompt_suggestion`

输出处理：

- provider content 必须 JSON parse 成功。
- parse 后必须是 object 或 array。
- parse 失败返回 `OUTPUT_FORMAT_FAILURE` 和 `MODEL_OUTPUT_INVALID_JSON`。
- parse 成功后，`AiResult.format = "STRUCTURED"`。

Knowledge capability 的 payload 字段必须固定：

- `relation_extraction`
  - `entities: list`
  - `relations: list`
  - `sourceSnippets: list`
  - `warnings: list`
- `knowledge_graph`
  - `entities: list`
  - `relations: list`
  - `entryRefs: list`
  - `warnings: list`
- `lineage_extraction`
  - `nodes: list`
  - `relations: list`
  - `sourceSnippets: list`
  - `warnings: list`

缺失字段由 workers 补空数组；字段类型不合法返回 `OUTPUT_FORMAT_FAILURE`。

## 错误归一化

必须补齐模型调用错误到 workers 错误类型的映射：

| 场景 | `WorkerErrorType` | `code` | `retryable` | `failureStage` |
| --- | --- | --- | --- | --- |
| `apiSource` 不支持 | `WORKER_PROTOCOL_FAILURE` | `UNSUPPORTED_MODEL_API_SOURCE` | false | `WORKER_REQUEST` |
| `baseUrl`、`modelName`、`apiKey` 缺失或非法 | `WORKER_PROTOCOL_FAILURE` | `MODEL_CONFIG_INVALID` | false | `WORKER_REQUEST` |
| `parameters` 覆盖核心字段 | `WORKER_PROTOCOL_FAILURE` | `MODEL_CONFIG_INVALID` | false | `WORKER_REQUEST` |
| 连接失败、DNS、TLS、连接重置 | `MODEL_TRANSPORT_FAILURE` | `MODEL_TRANSPORT_ERROR` | true | `WORKER_REQUEST` |
| 请求超时或读超时 | `WORKER_TIMEOUT` | `MODEL_TIMEOUT` | true | `WORKER_REQUEST` |
| provider 429 | `MODEL_TRANSPORT_FAILURE` | `MODEL_RATE_LIMITED` | true | `WORKER_REQUEST` |
| provider 5xx | `MODEL_TRANSPORT_FAILURE` | `MODEL_PROVIDER_UNAVAILABLE` | true | `WORKER_REQUEST` |
| provider 4xx 且非 429 | `MODEL_SEMANTIC_FAILURE` | `MODEL_REQUEST_REJECTED` | false | `WORKER_REQUEST` |
| provider 响应缺少 content | `OUTPUT_FORMAT_FAILURE` | `MODEL_OUTPUT_EMPTY` | false | `WORKER_RESULT` |
| JSON 结构化解析失败 | `OUTPUT_FORMAT_FAILURE` | `MODEL_OUTPUT_INVALID_JSON` | false | `WORKER_RESULT` |
| SSE chunk 非法 | `OUTPUT_FORMAT_FAILURE` | `MODEL_STREAM_CHUNK_INVALID` | false | `WORKER_STREAM` |
| `image_gen` 本轮未支持 | `UNSUPPORTED_CAPABILITY` | `UNSUPPORTED_CAPABILITY` | false | `WORKER_REQUEST` |

`error.detail` 只允许包含：

- `statusCode`
- `errorClass`
- `providerErrorType`
- `requestId`
- `traceId`
- `capability`
- `modelName`

`error.detail` 禁止包含：

- `apiKey`
- `Authorization`
- 完整 `prompt.messages`
- 完整 `input.payload`
- 完整 provider response body

## 小任务拆分

每个小任务只改 2-5 个文件，完成后先跑相关窄测试，再进入下一项。

### 任务 1：模型配置和错误归一化

目标：建立 OpenAI-compatible invocation 和错误映射，不接入 graph。

文件：

- `kuzhambu-workers/src/kuzhambu_workers/ai/model_adapters.py`
- `kuzhambu-workers/src/kuzhambu_workers/ai/errors.py`
- `kuzhambu-workers/src/kuzhambu_workers/core/errors.py`
- `kuzhambu-workers/tests/test_ai_error_mapping.py`
- `kuzhambu-workers/tests/test_prompt_messages.py`

完成标准：

- `prepare_openai_compatible_invocation()` 校验 `apiSource`、`baseUrl`、`apiKey`、`modelName`、`parameters`。
- 非 `OPENAI_COMPATIBLE` 返回 `UNSUPPORTED_MODEL_API_SOURCE`。
- 核心字段冲突返回 `MODEL_CONFIG_INVALID`。
- 测试覆盖错误 code、`retryable` 和 redaction。

### 任务 2：OpenAI-compatible 同步 client

目标：用 `httpx` 完成 `/chat/completions` 同步调用和 usage 映射。

文件：

- `kuzhambu-workers/src/kuzhambu_workers/ai/openai_compatible.py`
- `kuzhambu-workers/src/kuzhambu_workers/ai/usage.py`
- `kuzhambu-workers/src/kuzhambu_workers/ai/prompt_messages.py`
- `kuzhambu-workers/tests/test_ai_openai_compatible.py`

完成标准：

- 请求 URL、headers、body 字段与本 RUNBOOK 契约一致。
- provider `choices[0].message.content` 映射为 content。
- provider usage 映射为 `UsageSummary`。
- timeout、429、4xx、5xx、连接失败映射为稳定 `WorkerError`。
- 测试全部使用 mocked `httpx` transport。

### 任务 3：结构化输出解析

目标：把 JSON 约束、JSON parse 和 Knowledge payload shape 固定下来。

文件：

- `kuzhambu-workers/src/kuzhambu_workers/ai/structured_output.py`
- `kuzhambu-workers/src/kuzhambu_workers/ai/openai_compatible.py`
- `kuzhambu-workers/src/kuzhambu_workers/schemas/ai.py`
- `kuzhambu-workers/tests/test_ai_structured_output.py`
- `kuzhambu-workers/tests/test_graph_registry.py`

完成标准：

- 结构化 capability 自动发送 JSON 约束。
- JSON parse 失败返回 `MODEL_OUTPUT_INVALID_JSON`。
- Knowledge 三类 payload 缺失字段补空数组。
- Knowledge 三类 payload 字段类型错误返回 `OUTPUT_FORMAT_FAILURE`。
- 删除 `GraphRegistry` 的 placeholder 断言。

### 任务 4：LangGraph 同步执行接入

目标：移除 `basic.py` placeholder，所有非 stream capability 通过 graph 调用真实模型。

文件：

- `kuzhambu-workers/src/kuzhambu_workers/ai/graphs/basic.py`
- `kuzhambu-workers/src/kuzhambu_workers/ai/graphs/text.py`
- `kuzhambu-workers/src/kuzhambu_workers/api/ai_routes.py`
- `kuzhambu-workers/tests/test_ai_routes.py`
- `kuzhambu-workers/tests/test_ai_usecase_routes_knowledge.py`

完成标准：

- `rg -n "placeholder_only|placeholder\": True|占位结果|_placeholder" kuzhambu-workers/src/kuzhambu_workers/ai` 无生产命中。
- `/internal/ai/invoke` 返回真实 provider content。
- `usage.latencyMs` 不再固定为默认值。
- `image_gen` 返回稳定不支持错误。
- usecase path、capability、stream mismatch 的现有校验不退化。

### 任务 5：SSE 真实流式执行

目标：把 stream route 从“一次性 completed”改为 provider chunk -> SSE delta。

文件：

- `kuzhambu-workers/src/kuzhambu_workers/ai/openai_compatible.py`
- `kuzhambu-workers/src/kuzhambu_workers/api/ai_routes.py`
- `kuzhambu-workers/src/kuzhambu_workers/streaming/events.py`
- `kuzhambu-workers/tests/test_ai_streaming_model.py`
- `kuzhambu-workers/tests/test_ai_usecase_routes_discovery.py`

完成标准：

- `/internal/ai/stream` 至少输出 `started`、`delta`、`usage`、`completed`。
- stream usecase path 输出真实 `delta`。
- provider 非法 chunk 返回 `error`，且不返回 `completed`。
- provider 无 usage 时，结束前输出 latency-only `usage`。

### 任务 6：E2E 和收口

目标：验证 AI route、usecase route、SSE、security 和 OpenAPI 不回归。

文件：

- `kuzhambu-workers/tests/test_worker_e2e_ai.py`
- `kuzhambu-workers/tests/test_worker_e2e_ai_usecase_classics.py`
- `kuzhambu-workers/tests/test_worker_e2e_ai_usecase_discovery.py`
- `kuzhambu-workers/tests/test_worker_e2e_ai_usecase_security.py`
- `kuzhambu-workers/tests/test_openapi.py`

完成标准：

- E2E 测试使用 mocked provider。
- HMAC 和 path allowlist 测试不退化。
- OpenAPI usecase path 仍完整展示。
- 没有真实 API Key、外网请求或 provider 依赖。

## 验证命令

在 `kuzhambu-workers/` 下执行：

```sh
.venv/bin/python -m ruff format .
.venv/bin/python -m ruff format --check .
.venv/bin/python -m ruff check .
.venv/bin/python -m pytest -p no:capture
```

收口前额外执行：

```sh
rg -n "placeholder_only|placeholder\": True|占位结果|_placeholder" src/kuzhambu_workers/ai tests
rg -n "apiKey|Authorization|prompt.messages|input.payload" tests/test_ai_error_mapping.py tests/test_ai_openai_compatible.py
```

第二条命令用于人工复核测试断言是否误把敏感字段固化到错误快照；命中后必须逐项判断是否为合法 redaction 断言。

## 验收标准

- 生产 AI 代码无 placeholder 执行路径。
- 同步 AI route 和 usecase route 返回真实模型响应转换后的 `AiInvokeResponse`。
- SSE route 逐 chunk 输出真实 provider delta，并以 `completed` 作为唯一最终事实。
- 结构化输出失败不会伪装成功。
- `usage.latencyMs` 在成功和失败路径均有值。
- 所有模型调用失败都进入稳定 workers 错误协议。
- `image_gen` 不再返回空 artifact。
- Ruff format、Ruff lint、pytest 全部通过。
- 未修改 Java servers、前端应用和部署文件。
- 本 RUNBOOK 在任务关闭前删除。
