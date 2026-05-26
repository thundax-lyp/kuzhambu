# QA Design

## Purpose

本文档定义智能问答业务域的数据结构和接口设计。本文参考 redesign 中的问答、检索增强、上下文调试和来源引用材料，但不照搬其大对象模型、数据结构或接口形态；本设计必须从用户围绕三类知识库发起自然语言问答的业务流程出发，按 DDD 风格重新划分业务边界、聚合和表结构。

QA 是问答会话域，不拥有三库内容正文、搜索索引、标签词典、AI 模型配置、知识图谱结果或文档内联问答对。它消费 Search、Taxonomy、可选 Knowledge Graph、AI Config Prompt 和内容域能力，负责会话、消息、上下文窗口、来源引用、回答重试、管理员调试、会话删除和会话导出。

## Business Fit Rules

- 业务优先于旧设计：自然语言提问、多库范围选择、王圻单文档追问、多轮会话、最近 3 轮上下文、来源引用、权限过滤、失败重试、会话列表、删除、导出和管理员调试必须能直接对应问答业务。
- QA 不维护搜索索引、同义词词典、标签治理、知识图谱节点边或 AI 模型配置，必须消费对应业务域能力。
- Knowledge Graph 可用于实体识别和上下文增强，但问答不得依赖知识图谱作为必需前置。

## Module

- 模块名称：QA
- 业务域：qa
- 对应需求文档：[QA-REQUIREMENTS.md](../10-requirements/QA-REQUIREMENTS.md)
- 后端 biz 子工程：`kuzhambu-servers/biz/kuzhambu-biz-qa`
- 后端 infra 子工程：`kuzhambu-servers/infra/kuzhambu-infra-qa`
- 后台接口入口：`kuzhambu-servers/interfaces/kuzhambu-admin-api`
- 前台接口入口：`kuzhambu-servers/interfaces/kuzhambu-portal-api`
- 前端入口：`kuzhambu-apps/admin-web`、`kuzhambu-apps/portal-web`
- Python worker 能力：可通过 HTTP 提供回答生成、实体识别或上下文压缩能力，业务结果由 Java 主系统控制

## Business Boundary

- 本模块负责：问答会话、用户消息、助手回答、最近 3 轮上下文选择、单文档追问上下文约束、来源引用快照、回答失败重试、会话删除、会话导出、管理员调试信息和问答检索记录。
- 本模块不负责：搜索页面展示、搜索索引维护、三库内容维护、AI 模型和提示词配置、同义词词典治理、知识图谱抽取、问答对生成和内容页内联问答对维护。
- 依赖的其他业务域能力：Search 提供候选检索和排序；Sancai/Wangqi/Ming Customs 提供内容可见性和详情跳转；Taxonomy 提供同义词扩展；Knowledge Graph 提供可选实体增强；AI Config Prompt 提供问答能力对应的模型和提示词配置；Auth/Core 提供当前用户和权限；Audit 记录高价值操作。
- 对外提供的业务能力：跨库问答、王圻单文档追问、问答历史、来源引用追溯、会话导出和管理员上下文调试。

## DDD Model

### Aggregates

- `QaSession`：问答会话聚合根，拥有标题、所有者、问答范围、单文档上下文、会话状态和最近消息时间。
- `QaMessage`：会话消息，记录用户问题、助手回答、生成状态、失败原因和上下文轮次。
- `QaMessageSource`：回答来源引用，保存用户可识别的知识库、标题、内容位置、跳转目标和片段快照。
- `QaRetrievalTrace`：问答检索追溯，保存查询改写、筛选条件、候选数量、实体增强和管理员调试信息。
- `QaSessionExport`：会话导出记录，保存导出格式、文件对象引用和导出状态。

### Value Objects

- `QaScope`：`ALL` / `SANCAI` / `WANGQI` / `MING_CUSTOMS`。
- `QaContextMode`：`MULTI_LIBRARY` / `WANGQI_DOCUMENT`。
- `QaSessionStatus`：`ACTIVE` / `DELETED`。
- `QaMessageRole`：`USER` / `ASSISTANT` / `SYSTEM`。
- `QaMessageStatus`：`PENDING` / `ANSWERED` / `FAILED`。
- `QaSourceStatus`：`AVAILABLE` / `UNAVAILABLE`。
- `QaExportFormat`：`MARKDOWN` / `HTML` / `TXT`。

## Data Model

QA 表固定使用 `qa_` 前缀。所有对外标识使用 ULID，数据库内部可使用 `bigint id`。

### qa_session

保存问答会话。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `session_id` | `sessionId` | 是 | 会话 ULID |
| `owner_user_id` | `ownerUserId` | 是 | 会话所属用户 |
| `title` | `title` | 是 | 会话标题 |
| `scope` | `scope` | 是 | 问答范围 |
| `context_mode` | `contextMode` | 是 | 跨库问答或王圻单文档追问 |
| `context_content_type` | `contextContentType` | 否 | 单文档上下文类型 |
| `context_content_id` | `contextContentId` | 否 | 单文档上下文业务 ULID |
| `status` | `status` | 是 | 会话状态 |
| `opened_at` | `openedAt` | 是 | 会话开启时间 |
| `last_message_at` | `lastMessageAt` | 否 | 最近消息时间 |
| `removed_at` | `removedAt` | 否 | 会话移除时刻 |

约束：
- `session_id` 唯一。
- `context_mode=MULTI_LIBRARY` 时不得填写单文档上下文。
- `context_mode=WANGQI_DOCUMENT` 时 `context_content_type=WANGQI_DOCUMENT` 且 `context_content_id` 必填。
- `status=DELETED` 只表示用户删除会话，不得删除原始知识库内容。

### qa_message

保存问答消息。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `message_id` | `messageId` | 是 | 消息 ULID |
| `session_id` | `sessionId` | 是 | 所属会话 ULID |
| `role` | `role` | 是 | 消息角色 |
| `content` | `content` | 是 | 消息正文 |
| `message_status` | `messageStatus` | 是 | 生成状态 |
| `context_turn_count` | `contextTurnCount` | 是 | 回答使用的历史轮次数 |
| `failure_reason` | `failureReason` | 否 | 失败原因 |
| `sent_at` | `sentAt` | 是 | 消息发送时间 |
| `answered_at` | `answeredAt` | 否 | 回答完成时间 |

约束：
- `message_id` 唯一。
- 多轮上下文最多使用最近 3 轮。
- 回答生成失败时必须保留用户问题，允许基于原问题重试。

### qa_message_source

保存助手回答的来源引用。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `source_id` | `sourceId` | 是 | 来源 ULID |
| `message_id` | `messageId` | 是 | 助手消息 ULID |
| `content_type` | `contentType` | 是 | 来源内容类型 |
| `content_id` | `contentId` | 是 | 来源内容业务 ULID |
| `knowledge_base` | `knowledgeBase` | 是 | 知识库名称 |
| `title_snapshot` | `titleSnapshot` | 是 | 标题快照 |
| `location_label` | `locationLabel` | 否 | 内容位置说明 |
| `snippet` | `snippet` | 否 | 来源片段快照 |
| `source_rank` | `sourceRank` | 是 | 引用排序 |
| `score` | `score` | 否 | 候选相关性分数 |
| `source_status` | `sourceStatus` | 是 | 来源可用状态 |
| `referenced_at` | `referencedAt` | 是 | 引用写入时间 |

约束：
- `source_id` 唯一。
- 来源必须能让用户识别知识库、标题和内容位置。
- 来源被删除或用户无权访问时，展示不可用提示，不展示正文片段。

### qa_retrieval_trace

保存问答检索和上下文调试信息。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `trace_id` | `traceId` | 是 | 追溯 ULID |
| `message_id` | `messageId` | 是 | 用户问题消息 ULID |
| `raw_question` | `rawQuestion` | 是 | 原始问题 |
| `rewritten_question` | `rewrittenQuestion` | 否 | 改写后问题 |
| `scope` | `scope` | 是 | 检索范围 |
| `filters_json` | `filtersJson` | 否 | 检索筛选条件 |
| `expanded_terms_json` | `expandedTermsJson` | 否 | 同义词扩展结果 |
| `linked_entities_json` | `linkedEntitiesJson` | 否 | 实体识别和链接结果 |
| `candidate_count` | `candidateCount` | 是 | 权限过滤后的候选数 |
| `context_snapshot` | `contextSnapshot` | 否 | 管理员可见的上下文调试快照 |
| `retrieved_at` | `retrievedAt` | 是 | 检索时间 |

约束：
- 检索追溯不得保存用户无权访问内容的正文。
- 管理员调试信息只能通过后台接口查看。
- 权限过滤必须发生在候选内容进入回答上下文前。

### qa_session_export

保存会话导出记录。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `export_id` | `exportId` | 是 | 导出 ULID |
| `session_id` | `sessionId` | 是 | 会话 ULID |
| `format` | `format` | 是 | 导出格式 |
| `storage_object_id` | `storageObjectId` | 否 | 导出文件对象 ULID |
| `export_status` | `exportStatus` | 是 | `PENDING` / `COMPLETED` / `FAILED` |
| `failure_reason` | `failureReason` | 否 | 失败原因 |
| `requester_user_id` | `requesterUserId` | 是 | 导出发起用户 |
| `requested_at` | `requestedAt` | 是 | 发起时间 |
| `completed_at` | `completedAt` | 否 | 完成时间 |

约束：
- 导出会话不得重新暴露已不可用或当前用户无权访问的来源正文。
- 导出文件由 Storage 保存，本表只保存对象引用。

## Application Layer

- `QaSessionApplicationService`：会话创建、列表、标题维护、删除和历史读取。
- `QaConversationApplicationService`：提问、回答生成、最近 3 轮上下文选择、失败重试和王圻单文档追问编排。
- `QaRetrievalApplicationService`：调用 Search、Taxonomy 和可选 Knowledge Graph，完成权限过滤前置编排。
- `QaSourceApplicationService`：来源引用生成、可用性检查、来源跳转和不可用提示。
- `QaDebugApplicationService`：管理员上下文调试信息查询。
- `QaExportApplicationService`：会话导出、导出文件写入 Storage 和导出结果查询。

事务边界：
- 用户消息必须先落库，再执行回答生成。
- 回答生成失败不得回滚用户消息。
- 来源引用必须与助手回答关联保存。
- 会话删除只更新 QA 会话状态，不删除内容域数据。
- 会话导出完成后写入 Storage 对象引用。

## Interface Layer

后台接口固定使用 `/api/admin/qa/**`，portal 接口固定使用 `/api/portal/qa/**`。

### Session API

- `GET /qa/sessions`：查询当前用户会话列表。
- `POST /qa/sessions`：创建问答会话。
- `GET /qa/sessions/{sessionId}`：读取会话详情和消息列表。
- `DELETE /qa/sessions/{sessionId}`：删除会话。

### Conversation API

- `POST /qa/sessions/{sessionId}/messages`：提交问题并生成回答。
- `POST /qa/messages/{messageId}/retry`：重试失败回答。
- `GET /qa/messages/{messageId}/sources`：读取回答来源。

### Wangqi Document QA API

- `POST /qa/wangqi-documents/{documentId}/sessions`：从王圻文档详情创建单文档追问会话。
- `POST /qa/wangqi-documents/{documentId}/ask`：围绕当前王圻文档追加提问。

### Export And Debug API

- `POST /qa/sessions/{sessionId}/exports`：发起会话导出。
- `GET /qa/exports/{exportId}`：查询导出结果。
- `GET /qa/messages/{messageId}/debug`：后台查看问答上下文调试信息。

portal 不提供管理员调试接口。

## Infrastructure Layer

- Repository：`QaSessionRepository`、`QaMessageRepository`、`QaMessageSourceRepository`、`QaRetrievalTraceRepository`、`QaSessionExportRepository`。
- Mapper：按表建立 MyBatis Mapper。
- PersistenceAssembler：每个聚合根使用独立 `*PersistenceAssembler`。
- 外部客户端：Search Application Service、Sancai Application Service、Wangqi Application Service、Ming Customs Application Service、Taxonomy Application Service、Knowledge Graph Application Service、AI Config Prompt Application Service、Storage Application Service、Auth/Core Application Service、Python worker HTTP Client。
- 缓存：会话列表、短期上下文窗口和来源可用性结果可短缓存。

## Data Ownership

- 本模块拥有：`qa_session`、`qa_message`、`qa_message_source`、`qa_retrieval_trace`、`qa_session_export`。
- 本模块只读引用：三才、王圻、明代习俗内容，Search 检索结果，Taxonomy 同义词，Knowledge Graph 实体，AI Config Prompt 配置，Storage 文件对象，Auth/Core 用户和权限。
- 禁止跨域直接访问：不得直接修改内容表、搜索索引、标签词典、知识图谱表、AI 配置表或 Storage 文件物理数据。
- Flyway 脚本归属：`kuzhambu-admin-api` 的 `db/migration/V1__init.sql`，按 QA 分段。

## Observability

- 运行日志：记录回答生成失败、检索失败、权限过滤异常、来源可用性检查异常和导出失败。
- 访问日志：由接口层统一记录。
- 审计日志：会话删除、导出和管理员调试查看应形成业务审计。
- 关键指标：问答次数、失败率、平均来源数、无来源回答数、重试次数、导出次数和平均响应耗时。

## Acceptance

- 用户能提出自然语言问题并得到带来源的回答。
- 用户能选择一个或多个知识库作为问答范围。
- 用户能在王圻文档详情中围绕当前文档追问。
- 用户刷新页面后仍可查看历史会话。
- 多轮追问能使用最近上下文回答。
- 私有或无权访问内容不会进入非授权用户问答上下文。
- 来源内容被删除后显示不可用提示。
- 管理员能查看问答使用的上下文和来源追溯信息。
