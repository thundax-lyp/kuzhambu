# Workers AI Interface

## Purpose

本文档定义 Java AI 域与 Python workers 之间的内部 AI 执行接口。

该接口只表达“AI 域请求 workers 执行一次无状态 AI graph”。模型配置、提示词选择、变量校验、调用记录、候选结果、任务状态、权限和审计仍归 Java servers。

## Transport

- 协议：HTTP。
- 编码：UTF-8 JSON。
- 同步响应：`application/json`。
- 流式响应：`text/event-stream`，使用 Server-Sent Events。
- 调用方：AI 域 `WorkerAiClient`。
- 被调用方：Python workers。
- 认证：内部服务认证，不接收用户 access token。

## Internal Access Control

Workers AI 接口是内部接口，只允许 Java AI 域调用。

默认调用方向固定为：

```text
AI domain -> workers
```

AI 域不向 workers 开放业务写入、候选结果写入、任务状态回调、提示词读取或模型配置读取接口。业务域通过 AI 域 application 接口使用 AI 能力；AI 域在调用 workers 时把已选择和校验的 prompt/messages 放入请求体。workers 不通过 AI 域接口反向拉取提示词。workers 必须在当前 HTTP 响应或 SSE 流中返回执行结果；异步任务状态、失败归档、候选区和调用记录均由 AI 域在本地处理。

文件类 AI 结果统一使用 `temporary artifact reference`。workers 只返回临时产物引用，不返回最终业务 URL；Java AI 域必须根据该引用下载临时产物并转存到 `Storage`，业务侧最终只认 `Storage` 结果。

访问控制分三层：

1. 网络层：workers 不暴露公网入口，只允许 Java servers 所在内网、容器网络或服务网格访问。
2. 服务身份层：workers 必须校验调用方服务身份，确认请求来自受信任的 Java AI 域。
3. 请求完整性层：workers 必须校验请求时间、签名或内部令牌，避免非授权服务伪造调用。

内部接口使用 HMAC 签名；部署环境可以在网络层叠加 mTLS 或服务网格身份。

必需请求头：

- `X-Kuzhambu-Service`：调用方服务名，AI 域固定使用 `kuzhambu-ai`。
- `X-Kuzhambu-Request-Id`：单次请求标识，必须与请求体 `requestId` 一致。
- `X-Kuzhambu-Trace-Id`：链路标识，必须与请求体 `traceId` 一致。
- `X-Kuzhambu-Timestamp`：请求发起时间，Unix milliseconds。
- `X-Kuzhambu-Signature`：请求签名。

签名输入：

```text
METHOD + "\n" +
PATH + "\n" +
X-Kuzhambu-Timestamp + "\n" +
X-Kuzhambu-Request-Id + "\n" +
sha256(requestBody)
```

签名算法：

```text
hex(hmac_sha256(internalWorkerSecret, signingInput))
```

校验规则：

- `X-Kuzhambu-Service` 必须在 worker 允许列表中。
- 时间戳与 worker 当前时间偏差不得超过 5 分钟。
- 请求头 `requestId` 和 `traceId` 必须与请求体一致。
- 签名不匹配时必须返回 `401`。
- 服务名不允许访问该路径时必须返回 `403`。
- 请求体不合法时返回 `400`。
- worker 内部异常返回 `500`，但不得暴露密钥、完整 prompt 或完整业务输入。

环境变量：

- `KUZHAMBU_WORKER_ALLOWED_SERVICES`：允许的服务名列表。
- `KUZHAMBU_WORKER_INTERNAL_SECRET`：HMAC 共享密钥。
- `KUZHAMBU_WORKER_MAX_CLOCK_SKEW_MS`：允许的时间偏差，默认 `300000`。

边界规则：

- 用户 access token 不得转发给 workers。
- workers 不调用 System 校验用户权限。
- workers 不根据用户身份改变输出。
- workers 不回调 AI 域业务接口。
- AI 域不暴露给 workers 读取模型配置、提示词、候选结果或任务状态的接口。
- Java servers 必须在调用 workers 前完成用户认证、权限、业务状态和内容可见性校验。
- workers 的内部服务认证只证明调用方服务可信，不证明最终用户有业务权限。
- 包含 `modelConfig.apiKey` 的请求必须在受保护的内部网络或 TLS 通道上传输。

Workers 不使用回调接口。任何回调能力都不得复用当前 AI 执行接口；如需引入，必须新增独立接口文档并满足：

- 只允许回调专用路径，不复用 Admin、Portal 或业务公开接口。
- 使用独立服务身份和 HMAC 或 mTLS 校验。
- 回调请求不得携带用户 token。
- 回调内容只能表达执行结果或产物元信息，不得直接触发正式内容写入。
- AI 域收到回调后仍必须按本域规则写调用记录、候选结果和任务状态。

## Endpoints

当前通用 AI 调试接口固定为两个：

- `POST /internal/ai/invoke`：一次性 JSON 响应。
- `POST /internal/ai/stream`：SSE 流式响应。
- `POST /internal/openai/v1/chat-completions`：内部 OpenAI-compatible chat facade，用于文本和 image2text。
- `POST /internal/openai/v1/images/generations`：内部 OpenAI-compatible image generations facade，用于 text2image。

这两个通用接口仅用于调试、平台联调和协议验证，不作为真实业务域长期集成入口。真实业务必须使用基于 usecase 定义的稳定接口，由 AI 域或对应业务域明确 path、请求模型、权限边界、审计语义和失败分类。workers 内部仍可以复用 graph registry、model adapter、artifact store 和 SSE 编码等通用能力。

OpenAI-compatible facade 只用于需要以 OpenAI 接口形态对接 workers 的内部调用方。该接口仍位于 `/internal/*`，仍必须使用 HMAC 请求头，并且请求体必须包含 `requestId`、`traceId`、`model` 和本次调用的 `extendParams`。Workers 不保存供应商配置、模型 Key 或跨请求路由状态；调用方必须在每次请求中提供完整下游供应商配置。

`model` 固定使用 `{apiSource}/{realModelName}` 格式：

- `apiSource` 当前支持 `OPENAI` 和 `BYTEDANCE`。
- `realModelName` 是下游供应商真实模型名。
- `OPENAI` 请求直接按 OpenAI-compatible 协议转发。
- `BYTEDANCE` 的 image2text 走 `chat-completions`，text2image 走 `images/generations`，下游 base URL 和 API Key 从 `extendParams` 获取。

OpenAI-compatible 请求示例：

```json
{
  "requestId": "req_20260601_000001",
  "traceId": "trace_20260601_000001",
  "model": "OPENAI/gpt-4o-mini",
  "messages": [
    {"role": "system", "content": "You are a helpful assistant."},
    {"role": "user", "content": "hello"}
  ],
  "stream": false,
  "temperature": 0.2,
  "extendParams": {
    "baseUrl": "https://model.example.internal/v1",
    "apiKey": "only-present-in-process-memory",
    "capabilityTags": ["text", "streaming_text"],
    "timeoutMs": 60000
  }
}
```

ByteDance text2image 请求示例：

```json
{
  "requestId": "req_20260601_000002",
  "traceId": "trace_20260601_000001",
  "model": "BYTEDANCE/doubao-seedream",
  "prompt": "一只白瓷杯，宋代器物风格",
  "response_format": "b64_json",
  "extendParams": {
    "baseUrl": "https://ark.cn-beijing.volces.com/api/v3",
    "apiKey": "only-present-in-process-memory",
    "size": "2K",
    "watermark": true
  }
}
```

OpenAI-compatible 响应使用 OpenAI chat completion、`data: ...` chunk 或 image generations 形态返回。请求中的 OpenAI 参数扩展字段，例如 `temperature`，会转发到下游供应商；`extendParams` 中除 `baseUrl`、`apiKey`、`capabilityTags` 和 `timeoutMs` 外的字段也会作为供应商参数转发。

通用接口不承载业务权限、用例级审计或稳定业务语义。后续 usecase 接口示例：

- `POST /internal/ai/classics/translate`
- `POST /internal/ai/classics/summary`
- `POST /internal/ai/discovery/answer-generation`
- `POST /internal/ai/knowledge/lineage-extraction`

完整 usecase path、capability、stream 和输入快照要求见 [`WORKERS-AI-USECASE-INTERFACE.md`](./WORKERS-AI-USECASE-INTERFACE.md)。

Discovery `answer_generation` usecase 可以在 `input.payload` 接收单文档问答上下文：

- `contextMode`：单文档追问时固定为 `SINGLE_DOCUMENT`。
- `contextContentType`：当前支持 `WANGQI_DOCUMENT`。
- `contextContentId`：单文档内容 ID，字符串格式。

这些字段只作为 workers 本次无状态回答生成的输入上下文。正式 Discovery QA 会话、消息、来源、trace、知识同步状态和业务写入仍由 Java Discovery 域拥有；workers 不暴露 `/internal/ai/discovery/qa/session/*` 路径，也不提供 Discovery QA 会话运行时接口。

健康与能力发现接口：

- `GET /internal/health`
- `GET /internal/capabilities`

## Health Response

`GET /internal/health` 返回：

```json
{
  "status": "UP",
  "service": "kuzhambu-workers",
  "version": "0.0.1-dev",
  "startedAt": "2026-06-01T10:00:00.000Z",
  "time": "2026-06-01T10:05:00.000Z"
}
```

`status` 取值：

- `UP`
- `DEGRADED`
- `DOWN`

Health 接口不得检查数据库、Redis 或 MQ。

## Capabilities Response

`GET /internal/capabilities` 返回：

```json
{
  "ai": {
    "endpoints": ["/internal/ai/invoke", "/internal/ai/stream"],
    "stream": true,
    "capabilities": [
      "translate",
      "summary",
      "version_summary",
      "tags",
      "qa",
      "image_analysis",
      "image_gen",
      "visual",
      "fusion",
      "split",
      "query_understanding",
      "answer_generation",
      "knowledge_graph",
      "relation_extraction",
      "lineage_extraction",
      "prompt_suggestion"
    ],
    "resultFormats": ["TEXT", "MARKDOWN", "JSON", "STRUCTURED", "ARTIFACT"]
  },
  "render": {
    "endpoints": [
      "/internal/render/classics-export",
      "/internal/render/operations-report",
      "/internal/render/classics-export/stream",
      "/internal/render/operations-report/stream"
    ],
    "stream": true,
    "formats": ["CSV", "JSON", "HTML", "ZIP", "PDF"],
    "pdfEngine": "PLAYWRIGHT_CHROMIUM_PRINT",
    "browserPool": {
      "enabled": true,
      "maxPages": 4
    }
  },
  "limits": {
    "maxRequestBytes": 10485760,
    "maxArtifactBytes": 104857600,
    "artifactChunkBytes": 262144
  }
}
```

## Common Request

`/internal/ai/invoke` 和 `/internal/ai/stream` 使用同一请求模型。

```json
{
  "requestId": "req_20260601_000001",
  "traceId": "trace_20260601_000001",
  "callerDomain": "AI",
  "operation": "SANCAI_TRANSLATE",
  "capability": "translate",
  "scope": "SANCAI",
  "modelConfig": {
    "serviceRole": "PRIMARY",
    "apiSource": "OPENAI_COMPATIBLE",
    "baseUrl": "https://model.example.internal/v1",
    "apiKey": "only-present-in-process-memory",
    "modelName": "example-model",
    "capabilityTags": ["text"],
    "parameters": {
      "temperature": 0.2,
      "maxTokens": 2048
    },
    "timeoutMs": 60000
  },
  "prompt": {
    "templateId": "123",
    "promptVersionId": "456",
    "versionNo": 18,
    "messages": [
      {
        "role": "system",
        "content": "你是古文翻译助手。"
      },
      {
        "role": "user",
        "content": "请翻译：..."
      }
    ],
    "variables": {
      "originalText": "..."
    },
    "promptHash": "sha256:..."
  },
  "input": {
    "contentType": "SANCAI_ENTRY",
    "contentId": "10001",
    "payload": {
      "title": "天地一",
      "originalText": "..."
    }
  },
  "outputSchema": {
    "type": "text"
  },
  "options": {
    "stream": false,
    "forceJson": false,
    "locale": "zh-CN"
  }
}
```

字段规则：

- `requestId`：AI 域生成的单次 worker 请求标识。
- `traceId`：跨服务链路标识。
- `callerDomain`：固定为调用来源域，AI 域调用时使用 `AI`。
- `operation`：AI 域侧业务动作，用于日志和排查。
- `capability`：AI 能力编码，必须来自 AI 域能力定义。
- `scope`：知识库或业务范围，例如 `SANCAI`、`WANGQI`、`MING_CUSTOMS`、`DISCOVERY`、`KNOWLEDGE`。
- `modelConfig`：AI 域选择后的模型服务配置。workers 只使用，不持久化。
- `prompt.messages`：AI 域渲染后的最终 messages，workers 必须优先使用该字段。
- `prompt.variables`：用于调试和 workers 侧辅助二次校验；变量真相源仍在 AI 域。
- workers 不得仅凭 `templateId` 或 `promptVersionId` 回调 AI 域读取提示词内容。
- `input.payload`：执行所需完整业务快照，不得要求 workers 回查 Java servers 或数据库。
- `outputSchema`：结构化输出约束；文本类能力可使用 `{ "type": "text" }`。
- `options.stream`：调用 `/internal/ai/stream` 时必须为 `true`。

## Common Response

同步响应：

```json
{
  "requestId": "req_20260601_000001",
  "traceId": "trace_20260601_000001",
  "status": "SUCCEEDED",
  "capability": "translate",
  "result": {
    "format": "TEXT",
    "payload": "白话译文..."
  },
  "usage": {
    "latencyMs": 1820,
    "inputTokens": 1200,
    "outputTokens": 340,
    "costAmount": "0.03"
  },
  "failureStage": null,
  "fallbackUsed": false,
  "artifactReference": null,
  "warnings": [],
  "error": null
}
```

失败响应：

```json
{
  "requestId": "req_20260601_000001",
  "traceId": "trace_20260601_000001",
  "status": "FAILED",
  "capability": "translate",
  "result": null,
  "usage": {
    "latencyMs": 900,
    "inputTokens": 0,
    "outputTokens": 0,
    "costAmount": "0.00"
  },
  "failureStage": "WORKER_RESULT",
  "fallbackUsed": false,
  "artifactReference": null,
  "warnings": [],
  "error": {
    "type": "MODEL_SEMANTIC_FAILURE",
    "code": "MODEL_REFUSED",
    "message": "模型未能按要求返回结果。",
    "retryable": false,
    "detail": {
      "providerStatus": 200
    }
  }
}
```

文件类成功响应：

```json
{
  "requestId": "req_20260601_000001",
  "traceId": "trace_20260601_000001",
  "status": "SUCCEEDED",
  "capability": "image_gen",
  "result": null,
  "usage": {
    "latencyMs": 4120,
    "inputTokens": 980,
    "outputTokens": 0,
    "costAmount": "0.08"
  },
  "failureStage": null,
  "fallbackUsed": false,
  "artifactReference": {
    "artifactId": "art_20260601_000001",
    "downloadPath": "/internal/artifacts/art_20260601_000001",
    "contentType": "image/png",
    "filename": "sancai-image.png",
    "sizeBytes": 204800,
    "sha256": "sha256:...",
    "expiresAt": "2026-06-01T22:00:00.000Z"
  },
  "warnings": [],
  "error": null
}
```

`status` 取值：

- `SUCCEEDED`
- `FAILED`
- `PARTIAL`

`result.format` 取值：

- `TEXT`
- `MARKDOWN`
- `JSON`
- `STRUCTURED`
- `ARTIFACT`

固定补充字段：

- `failureStage`：固定枚举，取值为 `REQUEST_VALIDATE`、`WORKER_REQUEST`、`WORKER_STREAM`、`WORKER_RESULT`、`ARTIFACT_DOWNLOAD`、`STORAGE_PERSIST`、`CANDIDATE_PERSIST`。
- `fallbackUsed`：只表示主服务不可用后切换到备用服务或备用模型继续执行。
- `artifactReference`：仅用于文件类结果；字段固定为 `artifactId`、`downloadPath`、`contentType`、`filename`、`sizeBytes`、`sha256`、`expiresAt`。TTL 固定为 `12` 小时。

## SSE Stream

`POST /internal/ai/stream` 返回 SSE。

每个事件必须包含 JSON data：

```text
event: delta
data: {"eventId":"evt_0002","requestId":"req_20260601_000001","traceId":"trace_20260601_000001","stage":"model_stream","timestamp":"2026-06-01T10:00:01.123Z","delta":{"text":"白话"}}
```

事件类型：

- `started`：执行开始。
- `delta`：模型增量文本或增量结构化片段。
- `progress`：阶段进度。
- `artifact`：产物元信息。
- `usage`：阶段性用量摘要。
- `warning`：非阻断问题。
- `error`：失败信息。
- `completed`：最终完成。

`started` 示例：

```json
{
  "eventId": "evt_0001",
  "requestId": "req_20260601_000001",
  "traceId": "trace_20260601_000001",
  "stage": "start",
  "timestamp": "2026-06-01T10:00:00.000Z",
  "message": "started"
}
```

`completed` 示例：

```json
{
  "eventId": "evt_0099",
  "requestId": "req_20260601_000001",
  "traceId": "trace_20260601_000001",
  "stage": "completed",
  "timestamp": "2026-06-01T10:00:05.000Z",
  "result": {
    "format": "TEXT",
    "payload": "完整最终结果..."
  },
  "usage": {
    "latencyMs": 5000,
    "inputTokens": 1200,
    "outputTokens": 340,
    "costAmount": "0.03"
  },
  "extra": {
    "status": "SUCCEEDED",
    "failureStage": null,
    "fallbackUsed": false,
    "artifactReference": null
  }
}
```

文件类 `completed` 示例：

```json
{
  "eventId": "evt_0100",
  "requestId": "req_20260601_000001",
  "traceId": "trace_20260601_000001",
  "stage": "completed",
  "timestamp": "2026-06-01T10:00:05.500Z",
  "result": null,
  "usage": {
    "latencyMs": 5500,
    "inputTokens": 980,
    "outputTokens": 0,
    "costAmount": "0.08"
  },
  "extra": {
    "status": "SUCCEEDED",
    "failureStage": null,
    "fallbackUsed": false,
    "artifactReference": {
      "artifactId": "art_20260601_000001",
      "downloadPath": "/internal/artifacts/art_20260601_000001",
      "contentType": "image/png",
      "filename": "sancai-image.png",
      "sizeBytes": 204800,
      "sha256": "sha256:...",
      "expiresAt": "2026-06-01T22:00:00.000Z"
    }
  }
}
```

流式规则：

- AI 域可以将 `delta` 转发给前端展示。
- AI 域只能以 `completed.result`、`completed.extra.artifactReference` 或同步最终响应生成候选结果、问答消息或调用终态。
- workers 不负责 stream 恢复。
- HTTP 连接中断但未收到 `completed` 时，AI 域必须按失败或部分失败处理。
- `error` 事件后 workers 应结束流；如无法发送 `error`，AI 域按连接中断处理。

## Error Types

稳定错误类型：

- `WORKER_PROTOCOL_FAILURE`：请求字段缺失、响应格式不合法或 SSE 协议异常。
- `WORKER_TIMEOUT`：worker 调用超时。
- `WORKER_UNAVAILABLE`：worker 不可达。
- `MODEL_TRANSPORT_FAILURE`：workers 到模型服务的网络、限流或 5xx 失败。
- `MODEL_SEMANTIC_FAILURE`：模型拒答、空答、答非所问或语义失败。
- `OUTPUT_FORMAT_FAILURE`：模型输出无法解析为要求格式。
- `IMAGE_INPUT_FAILURE`：图片读取、格式或尺寸不满足要求。
- `UNSUPPORTED_CAPABILITY`：workers 不支持请求能力。
- `INTERNAL_FAILURE`：未分类内部错误。

重试规则：

- `WORKER_UNAVAILABLE`、`WORKER_TIMEOUT` 和 `MODEL_TRANSPORT_FAILURE` 可以由 AI 域决定重试或切换备用服务。
- `MODEL_SEMANTIC_FAILURE` 和 `OUTPUT_FORMAT_FAILURE` 不得由 infra 自动无限重试。
- 用户手动重试必须由 AI 域重新发起完整请求。

## Capability Results

各能力的 `result.payload` 形态：

- `translate`：纯文本译文，不要求 JSON。
- `summary`：纯文本摘要。
- `tags`：结构化数组，元素包含 `name`、`dimension` 和可选 `confidence`。
- `qa`：结构化数组，元素包含 `question`、`answer` 和可选 `sourceHint`。
- `image_analysis`：Markdown 文本。
- `image_gen`：`result` 为空，最终结果通过 `artifactReference` 返回。
- `fusion`：纯文本或 Markdown 信息融合说明。
- `visual`：纯文本视觉描述。
- `split`：结构化数组，元素包含 `title`、`originalText`、`translationText` 和 `targetVolumeHint`。
- `query_understanding`：结构化对象，包含 `intent`、`rewrittenQuery`、`entities` 和 `filters`。
- `answer_generation`：文本回答；stream 时通过 `delta` 输出，最终通过 `completed.result` 返回完整回答。
- `knowledge_graph`：结构化对象，包含 `entities`、`relations`、`sourceSpans` 和可选 `confidence`。
- `relation_extraction`：结构化对象，包含 `entities`、`relations` 和 `sourceSpans`。
- `lineage_extraction`：结构化对象，包含 `nodes`、`relations` 和 `sourceSpans`。
- `version_summary`：纯文本版本摘要。
- `prompt_suggestion`：文本或结构化建议，必须由用户确认后才可应用。

## Security

- AI Key 只允许出现在 `modelConfig.apiKey` 的进程内请求对象中。
- workers 不得把 AI Key 写入日志、错误、响应、临时文件或产物。
- workers 不得保存 `prompt.messages`、`input.payload` 或模型原始响应。
- Java AI 域记录调用时必须脱敏敏感字段。
- workers 不接收用户 token，不判断用户权限。

## Related Documents

- [WORKERS-REQUIREMENTS.md](../10-requirements/WORKERS-REQUIREMENTS.md)：workers 无状态执行和 stream 需求。
- [AI-REQUIREMENTS.md](../10-requirements/AI-REQUIREMENTS.md)：AI 域治理、提示词、候选和调用统计需求。
- [AI-DESIGN.md](../30-designs/AI-DESIGN.md)：AI 域分层设计和 `WorkerAiClient` 归属。
