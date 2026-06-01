# Workers AI Usecase Interface

## Purpose

本文档定义 AI 域调用 Python workers 的 usecase 化 AI 内部接口。

Workers 内部可以继续复用 LangGraph graph registry、model adapter、prompt message、structured output 和 SSE 编码等通用能力；但真实业务集成必须使用稳定 usecase path，不得长期依赖 `/internal/ai/invoke` 或 `/internal/ai/stream` 通用调试接口。

## Scope

覆盖：

- Classics 内容生产、视觉资产和条目拆分所需 AI usecase。
- Discovery 查询理解、查询改写和回答生成所需 AI usecase。
- Knowledge 实体关系、图谱和世系图抽取所需 AI usecase。
- AI 域自身的提示词优化建议和版本摘要 usecase。
- 同步和 SSE 流式执行的路径命名、请求响应边界和权限控制。

不覆盖：

- AI 域提示词、模型配置、能力映射、候选结果和调用记录持久化。
- Classics、Discovery、Knowledge 或 Operations 的业务权限判断。
- 正式内容写入、候选结果确认、问答会话保存或图谱正式结果保存。
- 数据库、Redis、MQ、任务状态或跨请求会话。

## Principles

- Usecase path 表达业务执行意图，不能只表达底层 capability。
- 每个 usecase 请求仍必须携带完整上下文：`modelConfig`、`prompt`、`input`、`outputSchema` 和 `options`。
- Workers 不根据业务 ID、prompt ID、template ID 回查 Java servers。
- Workers 不拥有业务权限、候选区、调用记录、正式内容或任务台账。
- 通用 `/internal/ai/invoke` 和 `/internal/ai/stream` 仅用于调试、平台联调和协议验证。
- 真实业务 usecase 由 AI 域作为唯一 AI 治理入口调用 workers；Classics、Discovery 和 Knowledge 不得直接调用 workers AI 接口。

## Transport

- 协议：HTTP。
- 编码：UTF-8 JSON。
- 同步响应：`application/json`。
- 流式响应：`text/event-stream`，使用 Server-Sent Events。
- 调用方：AI 域 `WorkerAiClient`。
- 被调用方：Python workers。
- 认证：内部 HMAC，与 [`WORKERS-AI-INTERFACE.md`](./WORKERS-AI-INTERFACE.md) 一致。

## Access Control

AI usecase 接口只允许 `kuzhambu-ai` 服务身份调用。

允许路径固定由 workers HMAC path allowlist 控制。业务域不得以自身服务身份调用 AI usecase path。

| 服务名 | 允许路径范围 | 用途 |
| --- | --- | --- |
| `kuzhambu-ai` | `/internal/ai/classics/*` | Classics AI usecase 执行 |
| `kuzhambu-ai` | `/internal/ai/discovery/*` | Discovery AI usecase 执行 |
| `kuzhambu-ai` | `/internal/ai/knowledge/*` | Knowledge AI usecase 执行 |
| `kuzhambu-ai` | `/internal/ai/platform/*` | AI 域自身 usecase 执行 |

## Common Request

Usecase 接口复用 AI 执行请求模型，但 `operation`、`scope`、`capability` 和 path 必须互相匹配。

```json
{
  "requestId": "req_20260601_000001",
  "traceId": "trace_20260601_000001",
  "callerDomain": "AI",
  "operation": "CLASSICS_SANCAI_TRANSLATE",
  "capability": "translate",
  "scope": "classics",
  "modelConfig": {
    "serviceRole": "PRIMARY",
    "apiSource": "OPENAI_COMPATIBLE",
    "baseUrl": "https://model.example.internal/v1",
    "apiKey": "only-present-in-process-memory",
    "modelName": "example-model",
    "capabilityTags": ["text"],
    "parameters": {},
    "timeoutMs": 60000
  },
  "prompt": {
    "templateId": "prompt_template_id",
    "promptVersionId": "prompt_version_id",
    "versionNo": 1,
    "messages": [
      {"role": "system", "content": "system prompt"},
      {"role": "user", "content": "user prompt"}
    ],
    "variables": {},
    "promptHash": "sha256:..."
  },
  "input": {
    "contentType": "SANCAI_ENTRY",
    "contentId": "10001",
    "payload": {}
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

## Common Response

同步响应复用 `AiInvokeResponse`：

```json
{
  "requestId": "req_20260601_000001",
  "traceId": "trace_20260601_000001",
  "status": "SUCCEEDED",
  "capability": "translate",
  "result": {
    "format": "TEXT",
    "payload": "白话译文"
  },
  "usage": {
    "latencyMs": 1200,
    "inputTokens": 100,
    "outputTokens": 80,
    "costAmount": "0.00"
  },
  "warnings": [],
  "error": null
}
```

流式响应复用 workers SSE 事件：`started`、`delta`、`progress`、`artifact`、`usage`、`warning`、`error`、`completed`。

## Usecase Matrix

### Classics

| Usecase | Path | Capability | Stream | Output | Content snapshot |
| --- | --- | --- | --- | --- | --- |
| 三才图会古文翻译 | `POST /internal/ai/classics/sancai/translate` | `translate` | 否 | `TEXT` | 条目标题、原文、门类、卷、上下文 |
| 三才图会批量古文翻译单元 | `POST /internal/ai/classics/sancai/translate-batch-item` | `translate` | 否 | `TEXT` | 单条待翻译条目快照和批量上下文 |
| 三才图会摘要生成 | `POST /internal/ai/classics/sancai/summary` | `summary` | 否 | `TEXT` | 条目原文、译文、标签、已有摘要 |
| 三才图会标签提取 | `POST /internal/ai/classics/sancai/tags` | `tags` | 否 | `STRUCTURED` | 条目原文、译文、门类、卷、已有标签 |
| 三才图会问答对生成 | `POST /internal/ai/classics/sancai/qa` | `qa` | 否 | `STRUCTURED` | 条目正文、译文、摘要、标签 |
| 三才图会图片理解 | `POST /internal/ai/classics/sancai/image-analysis` | `image_analysis` | 是 | `MARKDOWN` | 图片内容或临时可读资源、条目上下文、已有分析 |
| 三才图会视觉信息融合 | `POST /internal/ai/classics/sancai/fusion` | `fusion` | 否 | `MARKDOWN` | 原文、译文、图片理解结果、融合权重 |
| 三才图会视觉描述生成 | `POST /internal/ai/classics/sancai/visual-description` | `visual` | 否 | `TEXT` | 融合说明、条目上下文、风格参数 |
| 三才图会图片生成 | `POST /internal/ai/classics/sancai/image-gen` | `image_gen` | 是 | `ARTIFACT` | 视觉描述、风格参数、参考图信息 |
| 三才图会条目拆分 | `POST /internal/ai/classics/sancai/split` | `split` | 否 | `STRUCTURED` | 长条目原文、译文、当前卷和目标归属提示 |
| 王圻文档摘要生成 | `POST /internal/ai/classics/wangqi/summary` | `summary` | 否 | `TEXT` | 文档标题、正文、时间线、已有摘要 |
| 王圻文档标签提取 | `POST /internal/ai/classics/wangqi/tags` | `tags` | 否 | `STRUCTURED` | 文档标题、正文、已有标签 |
| 王圻文档问答对生成 | `POST /internal/ai/classics/wangqi/qa` | `qa` | 否 | `STRUCTURED` | 文档正文、摘要、标签 |
| 明代习俗摘要生成 | `POST /internal/ai/classics/ming-customs/summary` | `summary` | 否 | `TEXT` | 习俗标题、概述、正文、原文摘录 |
| 明代习俗标签提取 | `POST /internal/ai/classics/ming-customs/tags` | `tags` | 否 | `STRUCTURED` | 习俗标题、正文、分类、关键词 |
| 明代习俗问答对生成 | `POST /internal/ai/classics/ming-customs/qa` | `qa` | 否 | `STRUCTURED` | 习俗正文、摘要、标签 |

### Discovery

| Usecase | Path | Capability | Stream | Output | Content snapshot |
| --- | --- | --- | --- | --- | --- |
| 查询理解 | `POST /internal/ai/discovery/query-understanding` | `query_understanding` | 否 | `STRUCTURED` | 查询词、用户可见范围、筛选条件、搜索上下文 |
| 查询改写 | `POST /internal/ai/discovery/query-rewrite` | `query_understanding` | 否 | `STRUCTURED` | 查询词、同义词扩展、实体识别候选 |
| 回答生成 | `POST /internal/ai/discovery/answer-generation` | `answer_generation` | 否 | `TEXT` | 用户问题、已权限过滤来源、上下文片段 |
| 流式回答生成 | `POST /internal/ai/discovery/answer-generation/stream` | `answer_generation` | 是 | `TEXT` | 用户问题、已权限过滤来源、最近会话上下文 |

### Knowledge

| Usecase | Path | Capability | Stream | Output | Content snapshot |
| --- | --- | --- | --- | --- | --- |
| 实体关系候选抽取 | `POST /internal/ai/knowledge/relation-extraction` | `relation_extraction` | 否 | `STRUCTURED` | 条目正文、人工精修上下文、已有实体关系 |
| 知识图谱候选抽取 | `POST /internal/ai/knowledge/graph-extraction` | `knowledge_graph` | 否 | `STRUCTURED` | 三才图会门类、卷、条目集合和来源片段 |
| 世系图候选抽取 | `POST /internal/ai/knowledge/lineage-extraction` | `lineage_extraction` | 否 | `STRUCTURED` | 世系相关原文、已知君主和关系约束 |
| 标签候选抽取 | `POST /internal/ai/knowledge/tag-extraction` | `tags` | 否 | `STRUCTURED` | 跨库内容片段、已有标签体系和分类约束 |

### Platform AI

| Usecase | Path | Capability | Stream | Output | Content snapshot |
| --- | --- | --- | --- | --- | --- |
| 提示词优化建议 | `POST /internal/ai/platform/prompt-suggestion` | `prompt_suggestion` | 否 | `TEXT` 或 `STRUCTURED` | 当前提示词、变量定义、失败样例和目标能力 |
| 版本摘要 | `POST /internal/ai/platform/version-summary` | `version_summary` | 否 | `TEXT` | 变更前后内容、操作上下文和业务域 |

## Path And Capability Validation

Workers 必须校验 usecase path 与 `capability` 的匹配关系。

- path 与 `capability` 不匹配时返回 `WORKER_PROTOCOL_FAILURE`。
- path 与 `options.stream` 不匹配时返回 `WORKER_PROTOCOL_FAILURE`。
- 未实现 usecase path 返回 `404` 或稳定 `UNSUPPORTED_CAPABILITY`。
- usecase path 仍必须进入 graph registry，不能绕过 LangGraph 直接拼模型调用。

## OpenAPI Rules

- `/internal/docs` 必须展示 usecase path。
- 通用 `/internal/ai/invoke` 和 `/internal/ai/stream` 必须标注为调试接口。
- usecase path 必须标注业务用途、调用方、capability、stream 语义和输入快照要求。
- OpenAPI 不得暗示 workers 会读取数据库、Redis、MQ、提示词或业务记录。

## Related Documents

- [AI-REQUIREMENTS.md](../10-requirements/AI-REQUIREMENTS.md)：AI 域治理入口、提示词、模型、候选结果和调用记录需求。
- [WORKERS-REQUIREMENTS.md](../10-requirements/WORKERS-REQUIREMENTS.md)：workers 无状态执行、LangGraph 和跨域边界。
- [WORKERS-AI-INTERFACE.md](./WORKERS-AI-INTERFACE.md)：通用 AI 调试接口、请求响应和 SSE 协议。
- [WORKERS-DESIGN.md](../30-designs/WORKERS-DESIGN.md)：workers 工程结构和 graph registry 设计。
