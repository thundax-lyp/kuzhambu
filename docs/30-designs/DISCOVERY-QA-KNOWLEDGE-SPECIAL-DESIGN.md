# Discovery QA Knowledge Base Design

## Goal

Discovery QA 的目标是把已确认、可公开问答的 Classics 内容同步到 Knowledge Base，并通过 OpenAI-compatible chat 接口完成 Portal 问答、来源回显和审计留痕。

目标链路：

```text
portal-web -> Discovery Server -> kuzhambu-common-knowledge -> Knowledge Base provider
```

同步链路：

```text
Classics content change -> Discovery Server -> kuzhambu-common-knowledge -> Knowledge Base provider
```

边界：

- Discovery 负责知识选择、知识渲染、同步调度、会话、消息、来源快照和 trace。
- `kuzhambu-common-knowledge` 负责统一 Knowledge Base 接口和 provider adapter。
- Knowledge Base provider 负责知识承载、向量化、检索和 chat。
- 正式问答链路不经过 `biz/ai` 或 `kuzhambu-workers`。
- 业务层不可见 `appId`、dataset、collection、file 等 provider 对象；这些对象只存在于 adapter 配置和 trace 中。
- Admin 问答诊断查看归 FastGPT 产品；admin-web 不提供本地来源列表或 provider trace 详情界面，只提供 FastGPT 控制台跳转。

## Runtime Boundary With AI Usecases

正式 Portal/Admin QA 固定使用 FastGPT Knowledge Base 链路：

```text
portal-web/admin-web -> Discovery Server -> kuzhambu-common-knowledge -> FastGPT
```

`biz/ai` 的 Discovery `answer_generation` capability 只服务非正式知识库问答链路，例如查询理解实验、离线评估、后备能力或经设计文档明确切换的独立 AI 编排。该 capability 不创建、不恢复、不删除 Discovery QA 会话，不负责 provider trace 诊断，也不得替代 `KnowledgeBaseClient.chat()` 成为正式 QA 默认运行时入口。

当未来需要把正式 QA 从 FastGPT Knowledge Base 链路切换到 AI 域 `answer_generation` 时，必须先更新本专项设计、Discovery requirements、AI requirements 和 workers AI interface，并同步迁移会话、来源、trace、权限过滤和运维诊断口径。

## Knowledge Scope

进入问答的主知识：

| Source | `contentType` | Condition |
| --- | --- | --- |
| 三才图会条目 | `SANCAI_ENTRY` | `lifecycle_status = PUBLISHED` and `visibility = PUBLIC` |
| 王圻文档 | `WANGQI_DOCUMENT` | `visibility = PUBLIC` |
| 明代习俗条目 | `MING_CUSTOMS` | `visibility = PUBLIC` |

进入问答的补充知识：

| Source | Fields |
| --- | --- |
| `classics_content_tag` | effective `tag_name_snapshot` |
| `classics_content_qa_pair` | confirmed and applied `question`, `answer` |

不进入问答：

- 私有、未发布、已下线内容。
- 草稿、历史版本、导出任务、展示生成记录。
- 图片、Storage 对象、排序、内部状态、主键、类型码等技术字段。
- 未确认 AI 候选问答对。

## Knowledge Document

同步到 Knowledge Base 的标准结构：

```json
{
  "metadata": {
    "sourceId": "SANCAI_ENTRY:1001",
    "contentType": "SANCAI_ENTRY",
    "contentId": "1001",
    "knowledgeBase": "SANCAI_ENTRY",
    "currentVersionNo": 3,
    "knowledgeRevision": "sha256:...",
    "visibility": "PUBLIC",
    "status": "PUBLISHED",
    "sourcePath": "/classics/sancai/entries/1001",
    "updatedAt": "2026-07-04T12:00:00+08:00"
  },
  "knowledge": {
    "title": "天地玄黄",
    "categoryPath": "天文 / 卷一",
    "summary": "内容摘要",
    "body": "正文",
    "originalText": "原文",
    "translationText": "译文",
    "originalExcerpts": "原文摘录",
    "tags": ["天文", "礼制"],
    "qaPairs": [
      {
        "question": "这段内容说明什么？",
        "answer": "说明..."
      }
    ]
  }
}
```

规则：

- `metadata` 不参与向量正文，只用于幂等、权限、来源定位和 trace。
- `knowledge` 参与向量化和回答生成。
- 空知识字段不渲染。
- 已确认问答对是知识补充，不替代正文。

### Metadata Fields

| Field | Required | Meaning |
| --- | --- | --- |
| `sourceId` | yes | 业务唯一键，格式 `{contentType}:{contentId}` |
| `contentType` | yes | 来源类型 |
| `contentId` | yes | 来源内容 ID |
| `knowledgeBase` | yes | 业务知识范围 |
| `currentVersionNo` | yes | Classics 内容版本 |
| `knowledgeRevision` | yes | 由 knowledge fields、确认标签、确认问答对计算的内容指纹 |
| `visibility` | yes | 当前可见性 |
| `status` | yes | 当前业务状态 |
| `sourcePath` | yes | Portal 来源跳转路径 |
| `updatedAt` | yes | 来源内容更新时间 |

### Knowledge Fields

| Field | Type | Used By |
| --- | --- | --- |
| `title` | string | all sources |
| `categoryPath` | string | all sources |
| `summary` | string | all sources |
| `body` | string | `WANGQI_DOCUMENT`, `MING_CUSTOMS` |
| `originalText` | string | `SANCAI_ENTRY` |
| `translationText` | string | `SANCAI_ENTRY` |
| `originalExcerpts` | string | `MING_CUSTOMS` |
| `tags` | list string | all sources |
| `qaPairs.question` | string | all sources |
| `qaPairs.answer` | string | all sources |

Knowledge item text template:

```text
标题：{title}
位置：{categoryPath}
摘要：{summary}
正文：{body}
原文：{originalText}
译文：{translationText}
原文摘录：{originalExcerpts}
标签：{tags}

已确认问答：
Q: {question}
A: {answer}
```

## Common Knowledge Interface

`kuzhambu-common-knowledge` 暴露业务抽象，不暴露 provider 对象：

Package layout:

| Package | Responsibility |
| --- | --- |
| `model.base` | logical knowledge base requests/results |
| `model.item` | knowledge item requests/results |
| `model.chat` | OpenAI-compatible chat models and source hits |
| `model.sync` | sync requests/results |
| `model.health` | provider health result |

```text
KnowledgeBaseClient.health()
KnowledgeBaseClient.listKnowledgeBases(KnowledgeBaseListRequest)
KnowledgeBaseClient.ensureKnowledgeBase(KnowledgeBaseEnsureRequest)
KnowledgeBaseClient.listKnowledgeItems(KnowledgeItemListRequest)
KnowledgeBaseClient.upsertKnowledgeItem(KnowledgeItemUpsertRequest)
KnowledgeBaseClient.syncKnowledgeItem(KnowledgeSyncRequest)
KnowledgeBaseClient.deleteKnowledgeItem(KnowledgeItemDeleteRequest)
KnowledgeBaseClient.chat(KnowledgeChatRequest)
```

Provider adapter 负责映射：

| Common Concept | Meaning | Provider Mapping |
| --- | --- | --- |
| knowledge base | 逻辑知识库 | dataset, index, namespace, space |
| knowledge item | 一条业务知识 | collection, document, file, record |
| OpenAI `model` | 逻辑 knowledge base name，例如 `kuzhambu-qa` | provider app, bot, workflow |
| source | 回答来源 | provider retrieval hit |

Chat 中传入的 `model` 是逻辑知识库名。`appId` 由 adapter 根据 `model` 和配置解析，Discovery 不传递、不保存、不展示。

### Common Model Fields

| Model | Fields |
| --- | --- |
| `KnowledgeBaseEnsureRequest` | `name`, `description`, `options` |
| `KnowledgeBaseResult` | `knowledgeBaseId`, `name`, `raw` |
| `KnowledgeItemUpsertRequest` | `knowledgeBaseName`, `itemKey`, `title`, `text`, `metadata`, `options` |
| `KnowledgeItemDeleteRequest` | `knowledgeBaseName`, `knowledgeItemId`, `itemKey`, `options` |
| `KnowledgeItemResult` | `knowledgeItemId`, `knowledgeBaseId`, `itemKey`, `title`, `raw` |
| `KnowledgeSyncRequest` | `knowledgeBaseName`, `knowledgeItemId`, `options` |
| `KnowledgeSyncResult` | `syncId`, `status`, `raw` |

Request 中的 `knowledgeBaseName` 是业务定义；result 中的 `knowledgeBaseId` 和 `knowledgeItemId` 是 provider opaque references，仅用于后续同步和 trace。

### OpenAI-compatible Chat

`KnowledgeChatRequest`:

```json
{
  "model": "kuzhambu-qa",
  "messages": [
    {
      "role": "user",
      "content": "王圻文档中如何描述明代礼制？"
    }
  ],
  "stream": false,
  "metadata": {
    "chatId": "session-1001",
    "knowledgeBases": ["WANGQI_DOCUMENT", "MING_CUSTOMS"],
    "contextItemKey": "WANGQI_DOCUMENT:2001",
    "traceId": "trace-1"
  },
  "options": {
    "temperature": 0.2,
    "max_tokens": 1200
  }
}
```

`KnowledgeChatResult`:

```json
{
  "id": "chatcmpl-1",
  "object": "chat.completion",
  "created": 1783156800,
  "model": "kuzhambu-qa",
  "choices": [
    {
      "index": 0,
      "message": {
        "role": "assistant",
        "content": "回答内容"
      },
      "finishReason": "stop"
    }
  ],
  "usage": {
    "promptTokens": 100,
    "completionTokens": 200,
    "totalTokens": 300
  },
  "sources": [
    {
      "sourceId": "WANGQI_DOCUMENT:2001",
      "knowledgeBase": "WANGQI_DOCUMENT",
      "contentType": "WANGQI_DOCUMENT",
      "contentId": "2001",
      "title": "文档标题",
      "snippet": "来源摘录",
      "score": 0.82
    }
  ]
}
```

## Discovery APIs

Portal 只调用 Discovery，不直接调用 Knowledge Base provider。

| API | Purpose |
| --- | --- |
| `POST /api/portal/discovery/qa/session/open` | 创建或恢复会话 |
| `POST /api/portal/discovery/qa/chat/completions` | OpenAI-compatible 提问，`model` 为知识库名 |
| `POST /api/portal/discovery/qa/session/page` | 查询会话列表 |
| `POST /api/portal/discovery/qa/session/get` | 查询会话详情 |

`chat/completions` request 使用 `KnowledgeChatRequest` 结构；Discovery 从登录态绑定 owner，不从请求体接收 owner。

Admin API：

| API | Purpose |
| --- | --- |
| `POST /api/discovery/qa-admin/knowledge/health` | Knowledge Base 健康检查 |
| `POST /api/discovery/qa-admin/knowledge/rebuild` | 全量重建 QA 知识库 |
| `POST /api/discovery/qa-admin/knowledge/sync` | 手动同步单个内容 |
| `POST /api/discovery/qa-admin/knowledge/sync/page` | 查询同步状态 |
| `POST /api/discovery/qa-admin/session/page` | 查询会话列表 |
| `POST /api/discovery/qa-admin/session/get` | 查询会话详情 |
| `POST /api/discovery/qa-admin/session/delete` | 删除会话 |
| `POST /api/discovery/qa-admin/session/export` | 导出会话 |

Admin 不提供本地来源列表或 provider trace 查询 API；来源、分段召回、provider 请求和 trace 诊断在 FastGPT 产品中查看。

## Local Persistence

保留会话审计：

| Table | Required Fields |
| --- | --- |
| `discovery_qa_session` | `id`, `ownerType`, `ownerId`, `title`, `status`, `createdAt`, `updatedAt` |
| `discovery_qa_message` | `id`, `sessionId`, `role`, `content`, `answerStatus`, `createdAt` |
| `discovery_qa_message_source` | `id`, `messageId`, `sourceBusinessId`, `contentType`, `contentId`, `knowledgeBase`, `titleSnapshot`, `snippet`, `sourcePath`, `score`, `sourceStatus` |
| `discovery_qa_retrieval_trace` | `messageId`, `provider`, `externalKnowledgeBaseId`, `externalKnowledgeItemIds`, `externalChatId`, `providerRequestId`, `latencyMs`, `failureReason`, `raw` |

新增同步状态：

| Concept | Required Fields |
| --- | --- |
| Sync item | `sourceId`, `contentType`, `contentId`, `currentVersionNo`, `knowledgeRevision`, `provider`, `externalKnowledgeBaseId`, `externalKnowledgeItemId`, `syncStatus`, `failureReason`, `syncedAt` |
| Sync batch | `id`, `triggerType`, `provider`, `totalCount`, `successCount`, `failureCount`, `startedAt`, `finishedAt` |

## Flows

Full rebuild:

1. Admin triggers rebuild.
2. Discovery enumerates eligible sources.
3. Discovery builds `KnowledgeDocument`.
4. Discovery ensures logical knowledge base `kuzhambu-qa`.
5. Discovery upserts one knowledge item per source.
6. Discovery deletes provider knowledge items that no longer satisfy sync conditions.
7. Discovery records sync batch and item results.

Incremental sync:

1. Triggered by publish/version apply, visibility change, confirmed tag change, confirmed QA pair change.
2. Discovery reads current Classics source.
3. Eligible source is upserted.
4. Ineligible source is deleted.
5. Sync item records result and failure reason.

Portal chat:

1. Portal opens or resumes session.
2. Portal posts OpenAI-compatible `chat/completions`.
3. Discovery validates session owner, question, `knowledgeBases`, and optional `contextItemKey`.
4. Discovery calls `KnowledgeBaseClient.chat()`.
5. Discovery stores user message, assistant message, source snapshots and trace.
6. Discovery re-checks source visibility before returning source links.

## Acceptance

- 业务代码只依赖 `KnowledgeBaseClient`，不调用 provider HTTP API。
- `appId` 不出现在 Discovery API、业务命令、同步状态和会话状态中。
- Chat request/response 符合 OpenAI-compatible `model` / `messages` / `choices` 习惯，且 `model` 为逻辑知识库名。
- QA 正文只包含 `knowledge` 字段，不包含 technical metadata。
- 确认标签和确认问答对变化会触发重同步。
- 来源回显以 Kuzhambu 当前权限为准。
