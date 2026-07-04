# RUNBOOK Discovery QA Knowledge Base Rewrite

## Objective

按 `DISCOVERY-QA-KNOWLEDGE-SPECIAL-DESIGN.md` 重写 Discovery 知识库问答闭环：

- Portal 问答改为 OpenAI-compatible `chat/completions`。
- Discovery Server 通过 `kuzhambu-common-knowledge` 调用 Knowledge Base，不再通过 `biz/ai` 和 Workers 生成正式回答。
- Classics 可问答内容同步为 Knowledge Base 的 `knowledge item`。
- `portal-web` 调用 Discovery Portal QA API，不直连 Knowledge Base provider。
- `admin-web` 调用 Discovery Admin QA API 管理 health、sync、trace，不直连 Knowledge Base provider。
- `kuzhambu-workers` 不参与正式 Discovery QA 问答和知识同步链路。
- 保留 Discovery 会话、消息、来源、trace 和同步状态审计。

## Target Existing Foundation

基于以下目标依赖实施：

- `docs/30-designs/DISCOVERY-QA-KNOWLEDGE-SPECIAL-DESIGN.md`
- `kuzhambu-servers/common/kuzhambu-common-knowledge/src/main/java/com/thundax/kuzhambu/common/knowledge/client/KnowledgeBaseClient.java`
- `kuzhambu-servers/common/kuzhambu-common-knowledge/src/main/java/com/thundax/kuzhambu/common/knowledge/model/base/KnowledgeBaseEnsureRequest.java`
- `kuzhambu-servers/common/kuzhambu-common-knowledge/src/main/java/com/thundax/kuzhambu/common/knowledge/model/item/KnowledgeItemUpsertRequest.java`
- `kuzhambu-servers/common/kuzhambu-common-knowledge/src/main/java/com/thundax/kuzhambu/common/knowledge/model/chat/KnowledgeChatRequest.java`
- `kuzhambu-servers/common/kuzhambu-common-knowledge/src/main/java/com/thundax/kuzhambu/common/knowledge/model/chat/KnowledgeChatResult.java`
- `kuzhambu-servers/common/kuzhambu-common-knowledge/src/main/java/com/thundax/kuzhambu/common/knowledge/support/FastGptKnowledgeBaseClient.java`

## Target Data Structures

### Existing Tables To Replace In Place

File: `db/schema/discovery.sql`

Use these table names as final persistence boundaries:

- `discovery_qa_session`
- `discovery_qa_message`
- `discovery_qa_message_source`
- `discovery_qa_retrieval_trace`

Final `discovery_qa_session`:

| Field | Type | Meaning |
| --- | --- | --- |
| `session_id` | bigint | business session id |
| `owner_type` | varchar(32) | owner subject type, `USER` |
| `owner_id` | varchar(64) | owner subject id from auth context |
| `knowledge_base_name` | varchar(128) | OpenAI `model`, fixed value `kuzhambu-qa` |
| `title` | varchar(256) | session title |
| `scope` | varchar(64) | session default knowledge scope |
| `context_mode` | varchar(32) | `GENERAL` or content-scoped mode |
| `context_content_type` | varchar(64) | optional context content type |
| `context_content_id` | bigint | optional context content id |
| `status` | varchar(32) | `OPEN`, `CLOSED`, `REMOVED` |
| `opened_at` | datetime | opened time |
| `last_message_at` | datetime | latest message time |
| `removed_at` | datetime | removed time |

Remove from final `discovery_qa_session`:

- `owner_user_id`

Final `discovery_qa_message`:

| Field | Type | Meaning |
| --- | --- | --- |
| `message_id` | bigint | business message id |
| `session_id` | bigint | business session id |
| `role` | varchar(32) | `user`, `assistant`, `system` |
| `content` | mediumtext | message content |
| `answer_status` | varchar(32) | `SENT`, `SUCCEEDED`, `FAILED` |
| `context_turn_count` | int | request context turn count |
| `failure_reason` | varchar(1024) | provider or validation failure |
| `provider_chat_id` | varchar(128) | opaque provider chat id |
| `model` | varchar(128) | OpenAI-compatible knowledge base name |
| `finish_reason` | varchar(32) | provider finish reason |
| `sent_at` | datetime | user message time |
| `answered_at` | datetime | assistant answer time |

Remove from final `discovery_qa_message`:

- `message_status`

Final `discovery_qa_message_source`:

| Field | Type | Meaning |
| --- | --- | --- |
| `source_id` | bigint | business source row id |
| `message_id` | bigint | assistant message id |
| `source_business_id` | varchar(128) | `{contentType}:{contentId}` |
| `content_type` | varchar(64) | source content type |
| `content_id` | bigint | source content id |
| `knowledge_base` | varchar(64) | business knowledge scope |
| `title_snapshot` | varchar(256) | source title snapshot |
| `location_label` | varchar(256) | optional location label |
| `snippet` | text | source excerpt snapshot |
| `source_path` | varchar(512) | Portal source path |
| `source_rank` | int | rank in answer sources |
| `score` | decimal(12,6) | retrieval score |
| `source_status` | varchar(32) | `AVAILABLE`, `UNAVAILABLE` |
| `referenced_at` | datetime | source capture time |

Final `discovery_qa_retrieval_trace`:

| Field | Type | Meaning |
| --- | --- | --- |
| `trace_id` | bigint | business trace id |
| `message_id` | bigint | assistant message id |
| `raw_question` | text | user question snapshot |
| `provider` | varchar(64) | configured provider, e.g. `fastgpt` |
| `external_knowledge_base_id` | varchar(128) | opaque provider knowledge base id |
| `external_knowledge_item_ids` | text | JSON array of opaque provider item ids |
| `external_chat_id` | varchar(128) | opaque provider chat id |
| `provider_request_id` | varchar(128) | provider request id |
| `latency_ms` | bigint | provider call latency |
| `failure_reason` | varchar(1024) | provider failure summary |
| `raw` | mediumtext | raw provider response JSON |
| `retrieved_at` | datetime | provider response time |

Remove from final `discovery_qa_retrieval_trace`:

- `rewritten_question`
- `scope`
- `filters_json`
- `expanded_terms_json`
- `linked_entities_json`
- `candidate_count`
- `context_snapshot`

### New Tables

File: `db/schema/discovery.sql`

Add `discovery_qa_knowledge_sync_item`:

| Field | Type | Meaning |
| --- | --- | --- |
| `id` | bigint | pk |
| `source_id` | varchar(128) | `{contentType}:{contentId}` unique business key |
| `content_type` | varchar(64) | `SANCAI_ENTRY`, `WANGQI_DOCUMENT`, `MING_CUSTOMS` |
| `content_id` | bigint | source content id |
| `knowledge_base_name` | varchar(128) | logical KB name, `kuzhambu-qa` |
| `current_version_no` | int | source content version |
| `knowledge_revision` | varchar(128) | hash of rendered knowledge fields |
| `provider` | varchar(64) | configured provider |
| `external_knowledge_base_id` | varchar(128) | opaque provider KB id |
| `external_knowledge_item_id` | varchar(128) | opaque provider item id |
| `sync_status` | varchar(32) | `PENDING`, `SYNCING`, `SUCCEEDED`, `FAILED`, `DELETED` |
| `failure_reason` | varchar(1024) | sync failure summary |
| `synced_at` | datetime | last success time |
| `created_at` | datetime | create time |
| `updated_at` | datetime | update time |

Add `discovery_qa_knowledge_sync_batch`:

| Field | Type | Meaning |
| --- | --- | --- |
| `id` | bigint | pk |
| `batch_id` | bigint | business id |
| `trigger_type` | varchar(32) | `FULL_REBUILD`, `MANUAL`, `CONTENT_EVENT` |
| `provider` | varchar(64) | configured provider |
| `total_count` | int | total items |
| `success_count` | int | success items |
| `failure_count` | int | failed items |
| `started_at` | datetime | start time |
| `finished_at` | datetime | finish time |

Update:

- `db/data/discovery.sql`
- `deploy/scripts/business-table-whitelist.txt`

## Target Java Files

### Domain

Modify:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaSession.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaMessage.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaSource.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaRetrievalTrace.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaSessionRepository.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaMessageRepository.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaSourceRepository.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaRetrievalTraceRepository.java`

Add:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaKnowledgeSyncItem.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaKnowledgeSyncBatch.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaKnowledgeSyncItemRepository.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaKnowledgeSyncBatchRepository.java`

### Application

Modify:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/pom.xml`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/QaApplicationService.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/command/OpenQaSessionCommand.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/result/QaAnswerResult.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/result/QaTraceResult.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaSourceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaTraceAssembler.java`

Add:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/command/ChatCompletionCommand.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/command/SyncKnowledgeContentCommand.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/query/KnowledgeSyncItemPageQuery.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/result/ChatCompletionResult.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/result/KnowledgeHealthResult.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/result/KnowledgeSyncItemResult.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/result/KnowledgeSyncBatchResult.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/KnowledgeQaApplicationService.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/KnowledgeSyncApplicationService.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeQaApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeSyncApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeDocument.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeDocumentAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeItemTextRenderer.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeRevisionCalculator.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeSourceResolver.java`

Remove:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaContextAssembler.java`
- `AiFacade` dependency inside `QaApplicationServiceImpl`
- `QueryUnderstandingApplicationService` dependency inside `QaApplicationServiceImpl`
- `ClassicsFacade.listPublicContents()` as the answer-time retrieval source

### Infra

Modify:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaSessionDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaMessageDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaSourceDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaRetrievalTraceDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/assembler/QaPersistenceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSessionRepositoryImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaMessageRepositoryImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSourceRepositoryImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaRetrievalTraceRepositoryImpl.java`

Add:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaKnowledgeSyncItemDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaKnowledgeSyncBatchDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/mapper/QaKnowledgeSyncItemMapper.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/mapper/QaKnowledgeSyncBatchMapper.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaKnowledgeSyncItemRepositoryImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaKnowledgeSyncBatchRepositoryImpl.java`

### Interface

Modify:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/DiscoveryQaPortalController.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/request/DiscoveryQaRequests.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/response/DiscoveryQaResponses.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/assembler/DiscoveryQaPortalInterfaceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/DiscoveryQaAdminController.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/request/DiscoveryQaAdminRequests.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/response/DiscoveryQaAdminResponses.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/assembler/DiscoveryQaAdminInterfaceAssembler.java`

Target Portal APIs:

| API | Action |
| --- | --- |
| `POST /api/portal/discovery/qa/session/open` | final open-session API; request body has no `ownerUserId`; owner comes from auth context |
| `POST /api/portal/discovery/qa/chat/completions` | final question API; OpenAI-compatible request/response |
| `POST /api/portal/discovery/qa/session/page` | final session history API |
| `POST /api/portal/discovery/qa/session/get` | final session detail API |

Remove Portal API:

- `POST /api/portal/discovery/qa/question/ask`

Target Admin APIs:

| API | Action |
| --- | --- |
| `POST /api/discovery/qa-admin/knowledge/health` | final health API |
| `POST /api/discovery/qa-admin/knowledge/rebuild` | final full rebuild API |
| `POST /api/discovery/qa-admin/knowledge/sync` | final single content sync API |
| `POST /api/discovery/qa-admin/knowledge/sync/page` | final sync state page API |
| `POST /api/discovery/qa-admin/session/get` | final session detail API |
| `POST /api/discovery/qa-admin/source/list` | final source list API |
| `POST /api/discovery/qa-admin/trace/get` | final provider trace API |

## Frontend Files

Portal:

- `kuzhambu-apps/portal-web/src/pages/discovery/qa-service.ts`
- `kuzhambu-apps/portal-web/src/pages/discovery/qa-types.ts`
- `kuzhambu-apps/portal-web/src/pages/discovery/qa-page.tsx`
- `kuzhambu-apps/portal-web/src/pages/discovery/qa-page.test.tsx`

Required changes:

- Replace `/portal/discovery/qa/question/ask` with `/portal/discovery/qa/chat/completions`.
- Request uses `model`, `messages`, `stream`, `metadata`, `options`.
- Response reads answer from `choices[0].message.content`.
- Sources read from top-level `sources`.
- Remove `ownerUserId` from open session request.

Portal page target:

- Page file `qa-page.tsx` remains the only reader-facing Discovery QA page.
- Left area shows current session list, ordered by `lastMessageAt` descending.
- Main area shows message timeline for the selected session.
- Bottom composer sends one user message per submit.
- Session creation happens before the first message when no active session exists.
- The composer is disabled while a request is pending.
- Pending state shows the user message immediately and an assistant loading row.
- Success state replaces the loading row with `choices[0].message.content`.
- Failure state keeps the user message and shows a retryable assistant error row using backend `failureReason` when available.
- Source area is attached to the assistant message, not to the whole page.
- Each source row renders `titleSnapshot`, `snippet`, `locationLabel`, `score`, and source availability.
- Available sources link to `sourcePath`.
- Unavailable sources render as disabled text with `UNAVAILABLE` status.
- The page never renders provider names, provider app ids, dataset ids, collection ids, file ids, or FastGPT-specific labels.
- The visible model selector is not required; the service sends fixed `model: "kuzhambu-qa"`.
- `metadata.sessionId` carries the active session id.
- `metadata.contextContentType` and `metadata.contextContentId` are sent only when the page is opened from content context.
- `stream` is fixed to `false` in this rewrite.
- Tests cover first question, follow-up question, source rendering, unavailable source rendering, error state, and removal of `/question/ask`.

Admin:

- `kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-service.ts`
- `kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-types.ts`
- `kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-page.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/discovery/discovery-service-contract.test.ts`

Required changes:

- Add health/sync/rebuild/sync page calls.
- Render provider trace fields.
- Keep source and session read APIs.

Admin page target:

- Page file `qa-admin-page.tsx` remains the Discovery QA operations page.
- Page header title is `Discovery QA 知识库运维`.
- Top status block shows health result: `provider`, `knowledgeBaseName`, `status`, `checkedAt`, and `failureReason`.
- Primary action is `重建知识库`; it calls `POST /api/discovery/qa-admin/knowledge/rebuild`.
- Secondary action is `刷新状态`; it calls `POST /api/discovery/qa-admin/knowledge/health`.
- Sync table shows `sourceId`, `contentType`, `contentId`, `knowledgeBaseName`, `currentVersionNo`, `knowledgeRevision`, `syncStatus`, `failureReason`, `syncedAt`, and `updatedAt`.
- Sync table filters include `contentType` and `syncStatus`.
- Row action `同步` calls `POST /api/discovery/qa-admin/knowledge/sync` with `contentType` and `contentId`.
- Session detail view shows session metadata and message timeline.
- Message detail view shows associated sources and provider trace.
- Provider trace panel shows `provider`, `externalKnowledgeBaseId`, `externalKnowledgeItemIds`, `externalChatId`, `providerRequestId`, `latencyMs`, `failureReason`, `retrievedAt`, and formatted `raw`.
- The page may display provider names from backend trace, but it must not allow editing provider app id, dataset id, collection id, or file id.
- Admin service calls use only `/api/discovery/qa-admin/*`.
- Tests cover health load, rebuild action, sync page filters, row sync action, provider trace rendering, and no provider direct call.

## Removed Structures And Interfaces

Remove Java dependencies from Discovery QA answer path:

- `com.thundax.kuzhambu.ai.facade.AiFacade` in Discovery QA answer path.
- `com.thundax.kuzhambu.discovery.application.search.service.QueryUnderstandingApplicationService` in Discovery QA answer path.
- `QaContextAssembler` prompt/context construction for formal QA.
- `ClassicsFacade.listPublicContents()` as runtime answer corpus.

Remove API:

- `POST /api/portal/discovery/qa/question/ask`

Remove request fields:

- `DiscoveryQaRequests.OpenSessionRequest.ownerUserId`
- `DiscoveryQaRequests.AskQuestionRequest.operatorType`
- `DiscoveryQaRequests.AskQuestionRequest.operatorId`

Remove trace fields:

- `rewritten_question`
- `filters_json`
- `expanded_terms_json`
- `linked_entities_json`
- `candidate_count`
- `context_snapshot`

Update docs after implementation:

- Update `docs/30-designs/DISCOVERY-DESIGN.md` QA sections that mention `discovery_qa_source` or `discovery_qa_debug_context`.
- Update `docs/40-readiness/DISCOVERY-IMPLEMENTATION-COVERAGE.md` rows for QA route and source behavior.

## Target Decisions To Confirm

| Decision | Target |
| --- | --- |
| Portal question API | only `POST /api/portal/discovery/qa/chat/completions` |
| OpenAI `model` | fixed logical knowledge base name `kuzhambu-qa` |
| Owner source | auth context only; request body does not carry owner |
| Provider app routing | `kuzhambu-common-knowledge` adapter only; Discovery does not see `appId` |
| Runtime answer path | Discovery Server -> `KnowledgeBaseClient.chat()` -> Knowledge Base provider |
| Removed answer path | no `biz/ai`, no Workers, no in-memory Classics corpus retrieval |
| Portal frontend | `portal-web` calls only Discovery Portal QA APIs |
| Admin frontend | `admin-web` calls only Discovery Admin QA APIs |
| Worker boundary | `kuzhambu-workers` has no Discovery QA runtime endpoint or task |
| Knowledge sync triggers | full rebuild, manual sync, content publish/version apply, visibility change, confirmed tag change, confirmed QA pair change |
| Provider item delete | remove provider knowledge item and mark local sync item `DELETED` |
| Source visibility | re-check Kuzhambu source visibility before returning sources |
| Trace semantics | provider trace only |

## Execution Plan

每个任务限制在 2-5 个文件内。任务完成后按小步提交。

### Task 1: Schema

Files:

- `db/schema/discovery.sql`
- `db/data/discovery.sql`
- `deploy/scripts/business-table-whitelist.txt`

Changes:

- Replace final fields of `discovery_qa_session`.
- Replace final fields of `discovery_qa_message`.
- Replace final fields of `discovery_qa_message_source`.
- Replace final fields of `discovery_qa_retrieval_trace`.
- Add `discovery_qa_knowledge_sync_item`.
- Add `discovery_qa_knowledge_sync_batch`.

Validation:

- Schema contains no `owner_user_id`.
- Schema contains no `message_status`.
- Schema contains no old trace fields listed in `Remove trace fields`.
- Whitelist contains both new sync tables.

### Task 2: QA Session And Message Domain

Files:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaSession.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaMessage.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaSessionRepository.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaMessageRepository.java`

Changes:

- `QaSession` final fields: `sessionId`, `ownerType`, `ownerId`, `knowledgeBaseName`, `title`, `scope`, `contextMode`, `contextContentType`, `contextContentId`, `status`, `openedAt`, `lastMessageAt`, `removedAt`.
- `QaMessage` final fields: `messageId`, `sessionId`, `role`, `content`, `answerStatus`, `contextTurnCount`, `failureReason`, `providerChatId`, `model`, `finishReason`, `sentAt`, `answeredAt`.
- Repository methods use `ownerType + ownerId` for owner queries.
- Repository methods do not expose `ownerUserId` or `messageStatus`.

Validation:

- Domain compile has no references to `ownerUserId`.
- Domain compile has no references to `messageStatus`.

### Task 3: QA Source And Trace Domain

Files:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaSource.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaRetrievalTrace.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaSourceRepository.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaRetrievalTraceRepository.java`

Changes:

- `QaSource` final fields: `sourceId`, `messageId`, `sourceBusinessId`, `contentType`, `contentId`, `knowledgeBase`, `titleSnapshot`, `locationLabel`, `snippet`, `sourcePath`, `sourceRank`, `score`, `sourceStatus`, `referencedAt`.
- `QaRetrievalTrace` final fields: `traceId`, `messageId`, `rawQuestion`, `provider`, `externalKnowledgeBaseId`, `externalKnowledgeItemIds`, `externalChatId`, `providerRequestId`, `latencyMs`, `failureReason`, `raw`, `retrievedAt`.
- Trace repository queries by `traceId` and `messageId`.

Validation:

- Domain compile has no references to `rewrittenQuestion`, `expandedTermsJson`, `linkedEntitiesJson`, `candidateCount`, or `contextSnapshot`.

### Task 4: Sync Domain

Files:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaKnowledgeSyncItem.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaKnowledgeSyncBatch.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaKnowledgeSyncItemRepository.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaKnowledgeSyncBatchRepository.java`

Changes:

- `QaKnowledgeSyncItem` fields match `discovery_qa_knowledge_sync_item`.
- `QaKnowledgeSyncBatch` fields match `discovery_qa_knowledge_sync_batch`.
- Sync item repository supports get by `sourceId`, save, update, page.
- Sync batch repository supports save and update counts.

Validation:

- Domain architecture test passes.

### Task 5: QA DOs

Files:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaSessionDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaMessageDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaSourceDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaRetrievalTraceDO.java`

Changes:

- DO fields match final table fields.
- DOs remove fields that are removed from final schema.

Validation:

- MyBatis mapping compile passes.

### Task 6: QA Persistence Mapping

Files:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/assembler/QaPersistenceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSessionRepositoryImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaMessageRepositoryImpl.java`

Changes:

- Map final `QaSession` fields to `QaSessionDO`.
- Map final `QaMessage` fields to `QaMessageDO`.
- Owner queries use `ownerType + ownerId`.

Validation:

- `QaSessionRepositoryImplTest.java` passes.
- `QaMessageRepositoryImplTest.java` passes.

### Task 7: Source And Trace Persistence

Files:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/assembler/QaPersistenceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSourceRepositoryImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaRetrievalTraceRepositoryImpl.java`

Changes:

- Map final `QaSource` fields to `QaSourceDO`.
- Map final provider trace fields to `QaRetrievalTraceDO`.

Validation:

- `QaSourceRepositoryImplTest.java` passes.
- `QaRetrievalTraceRepositoryImplTest.java` passes.

### Task 8: Sync Persistence

Files:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaKnowledgeSyncItemDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaKnowledgeSyncBatchDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/mapper/QaKnowledgeSyncItemMapper.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/mapper/QaKnowledgeSyncBatchMapper.java`

Changes:

- Add DOs for both sync tables.
- Add MyBatis mappers for both sync tables.

Validation:

- Infra compile passes.

### Task 9: Sync Repository

Files:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaKnowledgeSyncItemRepositoryImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaKnowledgeSyncBatchRepositoryImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaKnowledgeSyncItemRepositoryImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaKnowledgeSyncBatchRepositoryImplTest.java`

Changes:

- Implement sync item and sync batch repositories.
- Cover save/update/page and status transitions.

Validation:

- New sync repository tests pass.

### Task 10: Classics QA Knowledge Facade

Files:

- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/ClassicsFacade.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsQaKnowledgeFacadeDto.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/request/ClassicsQaKnowledgeFacadeRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/response/ClassicsQaKnowledgeFacadeResponse.java`

Changes:

- Add focused facade contract for QA knowledge reads.
- Response exposes only confirmed, public, QA-eligible knowledge fields.

Validation:

- Classics facade architecture test passes.

### Task 11: Classics QA Knowledge Implementation

Files:

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/facade/impl/ClassicsFacadeImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/facade/assembler/ClassicsFacadeAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/facade/impl/ClassicsFacadeImplTest.java`

Changes:

- Implement QA knowledge facade method.
- Map fields for `SANCAI_ENTRY`, `WANGQI_DOCUMENT`, `MING_CUSTOMS`.
- Include confirmed tags and confirmed QA pairs.

Validation:

- New facade tests cover all three content types.

### Task 12: Knowledge Document Assembly

Files:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeDocument.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeDocumentAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeItemTextRenderer.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeDocumentAssemblerTest.java`

Changes:

- Build `metadata` and `knowledge` exactly as target design.
- Render only `knowledge` fields into item text.
- Skip empty knowledge fields.

Validation:

- Test verifies metadata fields do not appear in rendered text.
- Test verifies confirmed QA pairs render as knowledge.

### Task 13: Knowledge Revision And Source Resolver

Files:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeRevisionCalculator.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeSourceResolver.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeRevisionCalculatorTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeSourceResolverTest.java`

Changes:

- Revision includes knowledge fields, confirmed tags, confirmed QA pairs.
- Source resolver re-checks current visibility and returns `AVAILABLE` or `UNAVAILABLE`.

Validation:

- Tag changes alter `knowledgeRevision`.
- Confirmed QA pair changes alter `knowledgeRevision`.
- Private source resolves to `UNAVAILABLE`.

### Task 14: Knowledge Sync Application Contract

Files:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/command/SyncKnowledgeContentCommand.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/query/KnowledgeSyncItemPageQuery.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/result/KnowledgeSyncItemResult.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/KnowledgeSyncApplicationService.java`

Changes:

- Define sync command, page query, result and service methods.
- Service methods: `health`, `rebuild`, `syncContent`, `deleteContent`, `pageSyncItems`.

Validation:

- Application compile passes.

### Task 15: Knowledge Sync Implementation

Files:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeSyncApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/result/KnowledgeHealthResult.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/result/KnowledgeSyncBatchResult.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeSyncApplicationServiceImplTest.java`

Changes:

- Use `KnowledgeBaseClient.ensureKnowledgeBase`.
- Use `KnowledgeBaseClient.upsertKnowledgeItem`.
- Use `KnowledgeBaseClient.deleteKnowledgeItem`.
- Persist sync item and sync batch status.

Validation:

- Test covers success, failed sync and provider delete.

### Task 16: Knowledge QA Application Contract

Files:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/command/ChatCompletionCommand.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/result/ChatCompletionResult.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/KnowledgeQaApplicationService.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/QaApplicationService.java`

Changes:

- Add OpenAI-compatible chat application contract.
- Keep session read methods under QA application boundary.
- Remove `askQuestion` from final formal QA contract.

Validation:

- Application compile has no formal QA reference to `AskQuestionCommand`.

### Task 17: Knowledge QA Implementation

Files:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeQaApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaSourceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaTraceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeQaApplicationServiceImplTest.java`

Changes:

- Call `KnowledgeBaseClient.chat()`.
- Save user message, assistant message, source snapshots and provider trace.
- Re-check source visibility before returning sources.

Validation:

- Test covers successful answer, provider failure, unavailable source.

### Task 18: Remove Old QA Answer Path

Files:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaContextAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImplTest.java`

Changes:

- Delete `QaContextAssembler`.
- Remove `AiFacade`, `QueryUnderstandingApplicationService`, and answer-time `ClassicsFacade.listPublicContents()` dependencies.
- Replace old service tests with `KnowledgeQaApplicationServiceImplTest`.

Validation:

- `rg "generateDiscoveryAnswer|QaContextAssembler|QueryUnderstandingApplicationService" kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java` returns no formal QA answer path references.

### Task 19: Portal QA Interface

Files:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/DiscoveryQaPortalController.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/request/DiscoveryQaRequests.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/response/DiscoveryQaResponses.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/assembler/DiscoveryQaPortalInterfaceAssembler.java`

Changes:

- Remove `question/ask` endpoint.
- Add `chat/completions` endpoint.
- Remove `ownerUserId`, `operatorType`, `operatorId` request fields.
- Bind owner from auth context.

Validation:

- `DiscoveryQaPortalControllerTest.java` covers session open and chat completions.

### Task 20: Admin QA Interface

Files:

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/DiscoveryQaAdminController.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/request/DiscoveryQaAdminRequests.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/response/DiscoveryQaAdminResponses.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/assembler/DiscoveryQaAdminInterfaceAssembler.java`

Changes:

- Add knowledge health, rebuild, sync and sync page APIs.
- Change trace response to provider trace fields.

Validation:

- `DiscoveryQaAdminControllerTest.java` covers all admin QA endpoints.

### Task 21: Portal Web

Files:

- `kuzhambu-apps/portal-web/src/pages/discovery/qa-service.ts`
- `kuzhambu-apps/portal-web/src/pages/discovery/qa-types.ts`
- `kuzhambu-apps/portal-web/src/pages/discovery/qa-page.tsx`
- `kuzhambu-apps/portal-web/src/pages/discovery/qa-page.test.tsx`

Changes:

- Replace `/portal/discovery/qa/question/ask` with `/portal/discovery/qa/chat/completions`.
- Keep all Portal requests under Discovery API prefix.
- Do not add FastGPT or Knowledge Base provider calls in `portal-web`.
- Request uses `model`, `messages`, `stream`, `metadata`, `options`.
- Response reads answer from `choices[0].message.content`.
- Sources read from top-level `sources`.
- Remove `ownerUserId` from open session request.
- `qa-types.ts` defines OpenAI-compatible page types:
  - `QaChatCompletionRequest.model`
  - `QaChatCompletionRequest.messages`
  - `QaChatCompletionRequest.stream`
  - `QaChatCompletionRequest.metadata.sessionId`
  - `QaChatCompletionRequest.metadata.contextContentType`
  - `QaChatCompletionRequest.metadata.contextContentId`
  - `QaChatCompletionRequest.options`
  - `QaChatCompletionResponse.id`
  - `QaChatCompletionResponse.model`
  - `QaChatCompletionResponse.choices`
  - `QaChatCompletionResponse.sources`
- `qa-service.ts` exposes:
  - `openQaSession(command)`
  - `pageQaSessions(query)`
  - `getQaSession(command)`
  - `createQaChatCompletion(command)`
- `qa-page.tsx` implements:
  - session list loading and selected-session state
  - first-message auto session creation
  - message timeline
  - composer pending state
  - retryable failure state
  - source list below each assistant answer
  - unavailable source rendering
  - fixed logical model `kuzhambu-qa`
- `qa-page.tsx` does not expose provider, app, dataset, collection, or file controls.

Validation:

- `qa-page.test.tsx` covers:
  - first question opens a session then calls `chat/completions`
  - follow-up question reuses current `sessionId`
  - answer text is read from `choices[0].message.content`
  - sources are read from top-level `sources`
  - unavailable sources are rendered without a link
  - failed answer shows retryable error state
  - service never calls `/portal/discovery/qa/question/ask`
- Portal QA tests pass.

### Task 22: Admin Web

Files:

- `kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-service.ts`
- `kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-types.ts`
- `kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-page.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/discovery/discovery-service-contract.test.ts`

Changes:

- Add health, rebuild, sync and sync page calls.
- Keep all Admin requests under Discovery Admin API prefix.
- Do not add FastGPT or Knowledge Base provider calls in `admin-web`.
- Render sync state.
- Render provider trace fields.
- Keep session and source read views.
- `qa-admin-types.ts` defines:
  - `KnowledgeHealthRecord.provider`
  - `KnowledgeHealthRecord.knowledgeBaseName`
  - `KnowledgeHealthRecord.status`
  - `KnowledgeHealthRecord.checkedAt`
  - `KnowledgeHealthRecord.failureReason`
  - `KnowledgeSyncItemRecord.sourceId`
  - `KnowledgeSyncItemRecord.contentType`
  - `KnowledgeSyncItemRecord.contentId`
  - `KnowledgeSyncItemRecord.knowledgeBaseName`
  - `KnowledgeSyncItemRecord.currentVersionNo`
  - `KnowledgeSyncItemRecord.knowledgeRevision`
  - `KnowledgeSyncItemRecord.syncStatus`
  - `KnowledgeSyncItemRecord.failureReason`
  - `KnowledgeSyncItemRecord.syncedAt`
  - `KnowledgeSyncItemRecord.updatedAt`
  - `ProviderTraceRecord.provider`
  - `ProviderTraceRecord.externalKnowledgeBaseId`
  - `ProviderTraceRecord.externalKnowledgeItemIds`
  - `ProviderTraceRecord.externalChatId`
  - `ProviderTraceRecord.providerRequestId`
  - `ProviderTraceRecord.latencyMs`
  - `ProviderTraceRecord.failureReason`
  - `ProviderTraceRecord.retrievedAt`
  - `ProviderTraceRecord.raw`
- `qa-admin-service.ts` exposes:
  - `getKnowledgeHealth()`
  - `rebuildKnowledge(command)`
  - `syncKnowledgeContent(command)`
  - `pageKnowledgeSyncItems(query)`
  - `getQaSession(command)`
  - `listQaSources(query)`
  - `getQaTrace(command)`
- `qa-admin-page.tsx` implements:
  - health status block
  - primary rebuild action
  - refresh health action
  - sync item table
  - `contentType` and `syncStatus` filters
  - row sync action
  - session detail drawer or panel
  - source list
  - provider trace panel with formatted raw JSON
- `qa-admin-page.tsx` does not expose provider routing configuration.

Validation:

- `qa-admin-page.test.tsx` covers:
  - health block load
  - rebuild action
  - sync table rendering
  - `contentType` filter
  - `syncStatus` filter
  - row sync action
  - provider trace rendering
  - raw trace JSON formatting
- `discovery-service-contract.test.ts` covers:
  - admin service uses `/discovery/qa-admin/knowledge/health`
  - admin service uses `/discovery/qa-admin/knowledge/rebuild`
  - admin service uses `/discovery/qa-admin/knowledge/sync`
  - admin service uses `/discovery/qa-admin/knowledge/sync/page`
  - admin service has no provider direct URL
- Admin QA tests and discovery service contract tests pass.

### Task 23: Worker Boundary

Files:

- `kuzhambu-workers/README.md`
- `kuzhambu-workers/src/kuzhambu_workers/__init__.py`
- `kuzhambu-workers/src/kuzhambu_workers/main.py`
- `kuzhambu-workers/tests/test_ai_usecase_routes_discovery.py`
- `kuzhambu-workers/tests/test_workers_architecture.py`

Changes:

- Do not add Discovery QA worker capability.
- Do not add Discovery QA answer endpoint.
- Do not add Discovery QA knowledge sync worker.
- Keep existing worker capabilities outside formal Discovery QA route.

Validation:

- `rg "discovery.*qa|qa.*discovery|knowledge.*sync" kuzhambu-workers` has no formal Discovery QA route or task.
- Worker tests pass if worker files are touched.

### Task 24: Cross-app Contract Checks

Files:

- `kuzhambu-apps/portal-web/src/pages/discovery/qa-service.ts`
- `kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-service.ts`
- `kuzhambu-apps/admin-web/src/pages/discovery/discovery-service-contract.test.ts`
- `kuzhambu-apps/portal-web/src/pages/discovery/qa-page.test.tsx`

Changes:

- Assert portal-web uses `/portal/discovery/qa/chat/completions`.
- Assert portal-web does not use `/portal/discovery/qa/question/ask`.
- Assert admin-web uses `/discovery/qa-admin/knowledge/*`.
- Assert no frontend service calls provider URLs.

Validation:

- `cd kuzhambu-apps && npm run test`

### Task 25: Readiness Docs

Files:

- `docs/30-designs/DISCOVERY-DESIGN.md`
- `docs/40-readiness/DISCOVERY-IMPLEMENTATION-COVERAGE.md`
- `docs/30-designs/RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`

Changes:

- Update Discovery QA design references to final Knowledge Base route.
- Update readiness coverage rows for QA route, sync and source behavior.
- Delete this RUNBOOK after implementation is merged.

Validation:

- No docs reference `discovery_qa_source`, `discovery_qa_debug_context`, or `/question/ask` as target state.

## Validation

Backend:

```sh
cd kuzhambu-servers
mvn -pl common/kuzhambu-common-knowledge,biz/discovery -am spotless:apply
mvn -pl common/kuzhambu-common-knowledge,biz/discovery -am spotless:check
mvn -pl common/kuzhambu-common-knowledge,biz/discovery -am checkstyle:check
mvn -pl common/kuzhambu-common-knowledge,biz/discovery -am test
```

Frontend:

```sh
cd kuzhambu-apps
npm --workspace portal-web run format
npm --workspace admin-web run format
npm run format:check
npm run lint
npm run test
```

Manual API checks:

```text
POST /api/discovery/qa-admin/knowledge/health
POST /api/discovery/qa-admin/knowledge/rebuild
POST /api/portal/discovery/qa/session/open
POST /api/portal/discovery/qa/chat/completions
POST /api/discovery/qa-admin/session/get
POST /api/discovery/qa-admin/source/list
POST /api/discovery/qa-admin/trace/get
```

## Acceptance

- `question/ask` 不再是 Portal 正式问答入口。
- Portal chat 请求和响应符合 OpenAI-compatible `model/messages/choices` 习惯。
- `model` 是逻辑知识库名，不是 provider app id。
- `appId`、dataset、collection、file 不出现在 Discovery API 请求中。
- Discovery QA 正式回答不依赖 `biz/ai` 或 Workers。
- 同步状态可查，失败原因可追踪。
- 来源回显以当前 Kuzhambu 权限和可见性为准。
- `mvn` 和 `npm` 验证通过。
