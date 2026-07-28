# AI Runtime Interface

## Purpose

本文档定义 Java AI 域对 Admin Web 和跨业务域调用方暴露的 AI 运行时协议。

AI 运行时协议只接收业务调用意图和业务参数快照。模型选择、提示词模板选择、变量校验、提示词渲染、辅助参数组装、调用记录、候选结果和任务台账均由 AI 域在后端完成。

## Boundary

协议分三层：

- 外部运行时协议：Admin Web 或业务域调用 Java AI 域，只传业务 capability、内容标识和业务参数。
- AI 域内部命令：Java AI application 解析业务配置，保留业务 capability，并补齐模型、prompt、output schema 和 workers canonical capability。
- Workers 执行协议：Java AI infra 调用 `/internal/ai/invoke` 或 `/internal/ai/stream`，只发送 canonical worker capability 和已组装完成的无状态执行请求。

外部调用方不得直接选择 workers path、workers capability、模型服务密钥、prompt 模板内容或已渲染 messages。

## Business Capability

外部协议中的 `capability` 永远表示 AI 域业务能力编码，例如：

- `classics_translate`
- `classics_translate_batch_item`
- `classics_summary`
- `classics_tags`
- `classics_qa`
- `classics_split`
- `classics_image_describe`
- `classics_image_prompt_fusion`
- `classics_visual_describe`
- `classics_image_generate`
- `discovery_query_understanding`
- `discovery_answer_generation`
- `knowledge_relation_extract`
- `knowledge_graph_extract`
- `knowledge_lineage_extract`
- `knowledge_tags`
- `prompt_suggestion`
- `platform_version_summary`

该字段用于：

- 查找第一个启用的 `ai_business_config`。
- 选择当前 prompt template/version。
- 写入 `ai_invocation_log.capability`。
- 写入 `ai_candidate.capability`。
- 对前端、facade 和任务查询返回。

Workers canonical capability 是 Java AI 域内部传输字段，不进入外部协议。例如：

| 业务 capability | Workers canonical capability |
| --- | --- |
| `classics_translate` | `translate` |
| `classics_translate_batch_item` | `translate` |
| `classics_summary` | `summary` |
| `classics_tags` | `tags` |
| `classics_qa` | `qa` |
| `classics_split` | `split` |
| `classics_image_describe` | `image_analysis` |
| `classics_image_prompt_fusion` | `fusion` |
| `classics_visual_describe` | `visual` |
| `classics_image_generate` | `image_gen` |
| `discovery_query_understanding` | `query_understanding` |
| `discovery_answer_generation` | `answer_generation` |
| `knowledge_relation_extract` | `relation_extraction` |
| `knowledge_graph_extract` | `knowledge_graph` |
| `knowledge_lineage_extract` | `lineage_extraction` |
| `knowledge_tags` | `tags` |
| `prompt_suggestion` | `prompt_suggestion` |
| `platform_version_summary` | `version_summary` |

## Frontend Request

Admin Web 默认使用异步任务协议：

- `POST /api/ai/refinement/task/add`
- `POST /api/ai/refinement/task/get`
- `POST /api/ai/refinement/task/page`
- `POST /api/ai/refinement/task/cancel`

`task/add` 请求只包含业务字段：

```json
{
  "scope": "classics",
  "capability": "classics_summary",
  "contentType": "SANCAI_ENTRY",
  "contentId": 300000000001,
  "objectId": null,
  "requestId": "req_20260601_000001",
  "traceId": "trace_20260601_000001",
  "inputPayloadJson": "{\"sourceText\":\"天地玄黄\",\"tone\":\"concise\"}",
  "forceJson": false,
  "locale": "zh-CN"
}
```

字段规则：

- `scope`：业务域范围，按 AI 域已有值传入。
- `capability`：业务能力编码，必须能匹配 `ai_business_config`。
- `contentType`：业务内容类型。
- `contentId`：业务内容 ID。
- `objectId`：可选业务子对象 ID，例如图片或视觉资产 ID。
- `requestId`：单次调用 ID，由调用方或网关生成并保持幂等可追踪。
- `traceId`：链路追踪 ID。
- `inputPayloadJson`：业务参数 JSON 对象字符串，必须包含 prompt 模板所需变量。
- `forceJson`：是否要求结构化输出。
- `locale`：语言区域。

默认请求不得要求前端传：

- `modelId`
- `modelName`
- `serviceId`
- `serviceRole`
- `promptVersionId`
- `promptMessagesJson`
- `promptVariablesJson`
- `promptHash`
- `outputSchemaJson`
- `workerCapability`
- `workerPath`

上述字段只可作为后端兼容字段或诊断字段存在；默认运行时由 AI 域根据业务配置解析。

## Platform Request

平台 AI 能力仍使用固定 endpoint 表达动作：

- `POST /api/ai/platform/prompt-suggestion`
- `POST /api/ai/platform/version-summary`

请求使用同一轻量协议：

```json
{
  "contentType": "PROMPT_TEMPLATE",
  "contentId": 930001,
  "objectId": 940001,
  "requestId": "req_20260601_000002",
  "traceId": "trace_20260601_000001",
  "inputPayloadJson": "{\"template\":\"...\",\"changeGoal\":\"压缩输出\",\"knownIssues\":\"输出太长\"}",
  "forceJson": true,
  "locale": "zh-CN",
  "createCandidate": true
}
```

平台 endpoint 自身决定业务 capability：

- `prompt-suggestion` 固定为 `prompt_suggestion`。
- `version-summary` 固定为 `platform_version_summary`。

平台请求默认不传模型和 prompt 字段。AI 域按 endpoint 对应的业务 capability 查找业务配置并渲染 prompt。

## Facade Request

跨域 facade 调用遵循同一规则：

- 调用方传业务上下文快照和 `inputPayloadJson`。
- Discovery 和 Knowledge 的业务动作由 facade 方法名或任务类型决定，不由调用方传 workers capability。
- `modelId`、`promptVersionId`、`promptMessagesJson` 等字段仅作为旧调用方兼容字段；新调用方不得依赖这些字段。

## Response

Java AI 域对外响应中的 `capability` 固定返回业务 capability，不返回 workers canonical capability。

示例：

```json
{
  "callId": 120001,
  "candidateId": 130001,
  "status": "SUCCEEDED",
  "capability": "classics_summary",
  "resultFormat": "TEXT",
  "resultPayload": "摘要内容",
  "failureStage": null,
  "errorType": null,
  "errorMessage": null
}
```

失败时也必须返回业务 capability，便于前端用同一业务能力查询调用记录、候选结果和任务状态。

## Backend Flow

1. Interface 层校验业务字段和权限，不校验模型或 prompt 是否由前端提供。
2. Application 层按业务动作确定业务 capability。
3. `AiBusinessInvokeConfigResolver` 用业务 capability 查询第一个启用的业务配置。
4. Resolver 读取当前 prompt version 和变量快照，用 `inputPayloadJson` 渲染 `promptMessagesJson`。
5. Resolver 使用业务配置的 `modelId` 解析模型服务配置，并合并模型默认参数与业务辅助参数。
6. Application 层设置 workers canonical `workerCapability`。
7. Infra 层固定调用 `/internal/ai/invoke` 或 `/internal/ai/stream`，请求体 `capability` 使用 workers canonical capability。
8. Workers 返回结果后，Java AI 域把结果 capability 归一回业务 capability，再写调用记录、候选结果、任务状态并返回调用方。

## Related Documents

- [AI-REQUIREMENTS.md](../10-requirements/AI-REQUIREMENTS.md)
- [AI-DESIGN.md](../30-designs/AI-DESIGN.md)
- [WORKERS-AI-INTERFACE.md](./WORKERS-AI-INTERFACE.md)
