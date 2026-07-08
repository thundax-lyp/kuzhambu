# Wangqi Single Document QA Runbook

## 目标

把 Wangqi 单文档问答入口收口为真实可用闭环：

- Admin Web 的 Wangqi 文档详情页提供 `单文档问答` 按钮。
- Portal QA 根据 URL 上下文打开 `SINGLE_DOCUMENT` 会话并追问。
- Discovery 校验 Wangqi 文档权限，组装单文档上下文，保存会话、消息、来源和 trace。
- Discovery 通过 AI facade 调用 `DISCOVERY_ANSWER_GENERATION`，AI 写入 `ai_call_record` 并返回 `callId`。
- Admin QA 能查看会话、来源、trace、AI `callId`、AI 状态和失败原因。

不做：跨库问答增强、Provider 配置入口、Discovery 直连 workers、Classics 直连 workers、Portal Wangqi 只读详情入口。

## 数据结构变更

当前 `discovery_qa_session` 已有 `context_mode`、`context_content_type`、`context_content_id`。本任务只补 Discovery trace 到 AI 调用的追踪字段。

| 表 | 字段 | 类型 | 约束 | 写入来源 | 用途 |
| --- | --- | --- | --- | --- | --- |
| `discovery_qa_retrieval_trace` | `ai_call_id` | `bigint` | nullable, index | `DiscoveryAiFacadeResponse.callId` | 关联 `ai_call_record.call_id` |
| `discovery_qa_retrieval_trace` | `ai_status` | `varchar(32)` | nullable | `DiscoveryAiFacadeResponse.status` | 记录 AI 调用状态 |
| `discovery_qa_retrieval_trace` | `ai_error_type` | `varchar(64)` | nullable | `DiscoveryAiFacadeResponse.errorType` | 记录 AI 失败类型 |
| `discovery_qa_retrieval_trace` | `ai_error_message` | `varchar(1024)` | nullable | `DiscoveryAiFacadeResponse.errorMessage` | 记录 AI 失败信息 |

必须继续使用的已有字段：

| 表 | 字段 |
| --- | --- |
| `discovery_qa_session` | `context_mode`, `context_content_type`, `context_content_id` |
| `discovery_qa_message` | `answer_status`, `failure_reason`, `provider_chat_id`, `finish_reason`, `context_turn_count` |
| `discovery_qa_message_source` | `content_type`, `content_id`, `knowledge_base`, `title_snapshot`, `location_label`, `snippet`, `source_path`, `source_rank`, `score`, `source_status` |
| `discovery_qa_retrieval_trace` | `message_id`, `raw_question`, `provider`, `external_knowledge_base_id`, `external_knowledge_item_ids`, `external_chat_id`, `provider_request_id`, `latency_ms`, `failure_reason`, `raw` |

如果仓库存在集中 schema 初始化或迁移 SQL，同步新增 `ai_call_id`、`ai_status`、`ai_error_type`、`ai_error_message` 和索引 `idx_discovery_qa_trace_ai_call_id`。如果没有集中 SQL，在 PR 说明写明环境迁移 SQL。

## 小任务 1：Trace 字段落库

目标：让 Discovery trace 持久化 AI 调用追踪字段。

文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaRetrievalTrace.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaRetrievalTraceDO.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/mapper/QaRetrievalTraceMapper.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaRetrievalTraceRepositoryImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaRetrievalTraceRepositoryImplTest.java`

实现要求：

- `QaRetrievalTrace` 增加 `Long aiCallId`、`String aiStatus`、`String aiErrorType`、`String aiErrorMessage`。
- `QaRetrievalTraceDO` 增加对应字段。
- `QaRetrievalTraceMapper` 的 insert/select 映射覆盖新增字段。
- `QaRetrievalTraceRepositoryImpl` 的 DO/domain 转换覆盖新增字段。
- 仓储测试断言新增字段可保存、可读取。

## 小任务 2：Trace 结果和 Admin API

目标：Admin QA trace response 返回 AI 调用追踪字段。

文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/result/QaTraceResult.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaTraceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/response/DiscoveryQaAdminResponses.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/assembler/DiscoveryQaAdminInterfaceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/DiscoveryQaAdminControllerTest.java`

实现要求：

- `QaTraceResult` 增加 `aiCallId`、`aiStatus`、`aiErrorType`、`aiErrorMessage`。
- `QaTraceAssembler` 从 `QaRetrievalTrace` 映射新增字段。
- Admin trace response 增加 JSON 字段 `aiCallId`、`aiStatus`、`aiErrorType`、`aiErrorMessage`。
- Controller 测试断言 Admin trace 接口响应包含新增字段。

## 小任务 3：单文档会话校验

目标：Discovery 打开单文档会话前校验上下文合法性和 Wangqi 内容可访问性。

文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/command/OpenQaSessionCommand.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/assembler/DiscoveryQaPortalInterfaceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/DiscoveryQaPortalControllerTest.java`

实现要求：

- `contextMode=SINGLE_DOCUMENT` 时，`contextContentType` 必须是 `WANGQI_DOCUMENT`。
- `contextContentId` 必须非空。
- 通过 `ClassicsFacade.getQaKnowledge(ClassicsQaKnowledgeFacadeRequest)` 读取当前 Wangqi 文档快照。
- `knowledge.status` 不得是已删除或归档状态。
- 权限判断复用 Classics 返回的可见性语义；无权访问时拒绝打开会话。
- 业务错误覆盖：缺 ID、非 Wangqi 类型、内容不存在、不可访问、已归档或已删除。

## 小任务 4：Discovery 组装 AI 请求

目标：Discovery 使用单文档快照构造给 AI 的 prompt 和 input payload。

文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeQaApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/command/ChatCompletionCommand.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaSourceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeQaApplicationServiceImplTest.java`

实现要求：

- `KnowledgeQaApplicationServiceImpl` 不让 AI 或 workers 读取业务表。
- 单文档模式下，通过 `ClassicsFacade.getQaKnowledge` 获取 `ClassicsQaKnowledgeFacadeDto`。
- `inputPayloadJson` 精确包含：
  - `sessionId`
  - `question`
  - `contextMode`
  - `contextContentType`
  - `contextContentId`
  - `knowledge.title`
  - `knowledge.summary`
  - `knowledge.body`
  - `knowledge.tags`
  - `knowledge.qaPairs`
  - `recentMessages`，最多最近 3 轮
  - `sources`，至少包含当前 Wangqi 文档 source
- 当前文档 source 字段：
  - `contentType = "WANGQI_DOCUMENT"`
  - `contentId = contextContentId`
  - `knowledgeBase = knowledge.knowledgeBase`
  - `titleSnapshot = knowledge.title`
  - `sourcePath = knowledge.sourcePath`
  - `sourceStatus = "AVAILABLE"`
- `promptMessagesJson` 由 Discovery 根据问题、最近 3 轮和单文档知识快照生成。
- 测试断言 `inputPayloadJson` 和 `promptMessagesJson` 不缺上述字段。

## 小任务 5：Discovery 调 AI 并落消息

目标：正式回答生成链路从 `KnowledgeBaseClient` 切到 `AiFacade.generateDiscoveryAnswer`，Discovery 仍持有业务落库。

文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeQaApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaTraceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeQaApplicationServiceImplTest.java`

实现要求：

- 注入 `AiFacade`，调用 `AiFacade.generateDiscoveryAnswer(DiscoveryAiFacadeRequest)`。
- `serviceId`、`serviceRole`、`modelId`、`modelName`、`promptVersionId` 先复用现有可用默认配置；配置来源不完整时集中封装默认值并在 PR 说明中标注。
- AI 返回 `status=SUCCEEDED` 时，将 `resultPayload` 解析为回答文本，写入 assistant 消息。
- AI 返回失败或抛异常时，保留 user 消息，写入失败 assistant 消息。
- `QaTraceAssembler` 写入 `aiCallId`、`aiStatus`、`aiErrorType`、`aiErrorMessage`。
- trace `raw` 同时保留 Discovery 给 AI 的 request snapshot 和 AI response snapshot。
- 测试覆盖成功、AI 失败、AI 异常、chat metadata/options 与 session 上下文不一致。

## 小任务 6：AI facade 调用记录

目标：确保 Discovery answer-generation 通过 AI 域写入 `ai_call_record`，并返回可追踪 `callId`。

文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/DiscoveryAiFacadeRequest.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/response/DiscoveryAiFacadeResponse.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/discovery/service/impl/DiscoveryAiApplicationServiceImpl.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/discovery/service/impl/DiscoveryAiApplicationServiceImplTest.java`

实现要求：

- `DiscoveryAiApplicationServiceImpl.generateAnswer` 继续解析到 `/internal/ai/discovery/answer-generation`。
- `DiscoveryAiFacadeResponse.callId` 必须来自 `AiInvokeResult.callId`。
- Discovery answer-generation 不创建 `ai_candidate`，`candidateId` 可为空。
- 测试断言 worker path、`createCandidate=false`、`callId` 返回和失败字段透传。

## 小任务 7：Admin Web Trace 展示

目标：Admin QA 运维页能看到并复制 AI 调用追踪字段。

文件：

- `kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-types.ts`
- `kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-service.ts`
- `kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-page.test.tsx`

控件和操作：

- QA Admin 详情页 trace 面板增加字段行：`AI 调用 ID`，值为 `aiCallId`，无值显示 `-`。
- trace 面板增加字段行：`AI 状态`、`AI 错误类型`、`AI 错误信息`。
- `aiCallId` 存在时，在 `AI 调用 ID` 同行显示复制按钮。
- 点击复制按钮复制 `aiCallId`，成功后显示现有消息提示。
- 不新增跳转到 AI 调用详情，除非当前应用已有稳定路由。
- 测试覆盖字段展示、空值展示和复制按钮操作。

## 小任务 8：前端入口和 Portal 交互

目标：用户从 Wangqi 详情进入 Portal QA，并在单文档上下文内提问和重试。

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.test.tsx`
- `kuzhambu-apps/portal-web/src/pages/discovery/qa-page.tsx`
- `kuzhambu-apps/portal-web/src/pages/discovery/qa-page.test.tsx`
- `kuzhambu-apps/portal-web/e2e/discovery/qa.spec.ts`

Admin Web 控件和操作：

- 在 Wangqi 编辑详情抽屉或详情操作区新增按钮：`单文档问答`。
- 按钮与现有详情操作同级。
- `activeDocument.id` 存在且当前用户有 `classics:wangqi:view` 权限时可点击。
- 文档未保存、无 ID 或无查看权限时按钮禁用，提示 `保存后可发起单文档问答` 或 `无查看权限`。
- 点击按钮跳转到 `/discovery/qa?contextContentType=WANGQI_DOCUMENT&contextContentId=<activeDocument.id>&contextMode=SINGLE_DOCUMENT&title=<encodedTitle>`。
- 不在按钮或 URL 中暴露 provider、dataset、collection、file、model 配置。

Portal Web 控件和操作：

- URL 含 `contextMode=SINGLE_DOCUMENT&contextContentType=WANGQI_DOCUMENT&contextContentId=<id>` 时进入单文档模式。
- 页面顶部显示上下文条：主文本为 URL `title`，副文本为 `WANGQI_DOCUMENT #<id>`。
- 单文档模式隐藏或锁定 `contextMode`、`contextContentType`、`contextContentId` 手工输入控件。
- 提问输入框继续使用现有 `Textarea`。
- 提交按钮继续使用现有发送按钮。
- 首次提交先调用 `session/open`，body 包含 `contextMode`、`contextContentType`、`contextContentId`、`title`。
- 随后调用 `chat/completions`，body 的 `metadata` 和 `options` 都包含同一组上下文字段。
- 追问复用已选会话上下文，不使用用户可编辑表单覆盖 session 上下文。
- AI 失败时，在 assistant 消息位置显示失败状态、失败原因和重试按钮；重试按钮重新提交同一问题和同一上下文。

测试要求：

- Admin 测试覆盖按钮渲染、禁用态、URL 参数。
- Portal 单测覆盖 URL 初始化、上下文条展示、上下文控件锁定、`session/open` payload、`chat/completions` metadata/options、失败重试。
- Portal e2e 覆盖 Wangqi URL 进入、首问、来源展示、刷新后会话详情读取。

## 小任务 9：文档收口

目标：实现完成后更新稳定进度文档，任务关闭前删除本 RUNBOOK。

文件：

- `docs/30-designs/DISCOVERY-DESIGN.md`
- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
- `docs/40-readiness/DISCOVERY-IMPLEMENTATION-COVERAGE.md`
- `docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`

更新要求：

- `DISCOVERY-DESIGN.md` 补充 `discovery_qa_retrieval_trace` 的 AI 调用追踪字段。
- `CLASSICS-IMPLEMENTATION-COVERAGE.md` 中 Wangqi “单文档问答入口”改为已完成。
- `DISCOVERY-IMPLEMENTATION-COVERAGE.md` 记录 Discovery 经 AI facade 生成回答、保存 `aiCallId` trace。
- `AI-IMPLEMENTATION-COVERAGE.md` 记录 Discovery answer-generation 被 Wangqi 单文档 QA 消费。
- 任务关闭前删除本 RUNBOOK。

## 验证命令

Java：

```sh
cd kuzhambu-servers
mvn -pl biz/discovery/kuzhambu-discovery-interface,biz/discovery/kuzhambu-discovery-application,biz/discovery/kuzhambu-discovery-infra,biz/ai/kuzhambu-ai-application,biz/ai/kuzhambu-ai-facade,biz/classics/kuzhambu-classics-facade -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/discovery/kuzhambu-discovery-interface,biz/discovery/kuzhambu-discovery-application,biz/discovery/kuzhambu-discovery-infra,biz/ai/kuzhambu-ai-application,biz/ai/kuzhambu-ai-facade,biz/classics/kuzhambu-classics-facade -am test
```

Frontend：

```sh
cd kuzhambu-apps
pnpm --filter @kuzhambu/admin-web run format
pnpm --filter @kuzhambu/portal-web run format
pnpm run format:check
pnpm run lint
pnpm --filter @kuzhambu/admin-web test
pnpm --filter @kuzhambu/portal-web test
pnpm --filter @kuzhambu/portal-web run e2e -- e2e/discovery/qa.spec.ts
```

Workers：

```sh
cd kuzhambu-workers
.venv/bin/python -m ruff format .
.venv/bin/python -m ruff format --check .
.venv/bin/python -m ruff check .
.venv/bin/python -m pytest -p no:capture tests/test_worker_e2e_ai_usecase_discovery.py
```

手工冒烟：

- 打开 Admin Web Wangqi 文档详情页。
- 点击 `单文档问答`。
- 确认 Portal QA 顶部展示当前文档标题和 `WANGQI_DOCUMENT #<id>`。
- 输入问题并提交。
- Network 中确认先调用 `session/open`，再调用 `chat/completions`，两次请求均包含单文档上下文。
- 确认回答展示、来源列表含当前 Wangqi 文档、刷新后会话仍可读取。
- 打开 Admin QA 运维详情，确认 trace 中有 `aiCallId`、AI 状态和失败字段。
