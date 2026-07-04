# RUNBOOK Discovery 搜索与问答闭环收口

## 目标

完成 Discovery 搜索与问答闭环收口，使 `docs/40-readiness/DISCOVERY-IMPLEMENTATION-COVERAGE.md` 中以下需求项具备可追溯运行时代码、页面入口和测试，并可从 `部分完成` 或 `未完成` 更新为 `已完成`：

- 搜索组内相关性排序。
- 关键词高亮。
- 按知识库、门类、标签、状态、时间筛选。
- 搜索深链与状态保留。
- 无结果空状态提示。
- 王圻单文档追加式问答。
- 更细粒度搜索分析。

本 RUNBOOK 不变更 `discovery_*` 数据库表结构，不新增 worker 持久化能力，不让 Portal/Admin 直连 provider。

## 已确认决策

- 搜索高亮由后端返回 `highlightText` 字符串，命中词使用 `<mark>` 和 `</mark>` 包裹；Portal Web 只对白名单 `<mark>` 标签做安全渲染，其余内容按普通文本处理。
- 王圻单文档问答入口使用 Portal QA URL 参数，不新增独立页面和 provider 路由；入口格式为 `/discovery/qa?contextContentType=WANGQI_DOCUMENT&contextContentId=<id>&contextMode=SINGLE_DOCUMENT&title=<title>`。
- Workers 只保留 Discovery 查询理解、查询改写和回答生成技术 usecase；正式 Discovery QA 会话、消息、来源、trace 和知识同步仍由 Java Discovery 域拥有。
- 本轮不新增 `discovery_*` 表，不引入 Outbox，不扩大搜索内容源范围。

## 当前事实

- 当前分支：`feat/discovery-search-qa-closeout`。
- Discovery 后端模块：`kuzhambu-servers/biz/discovery/`。
- Portal 搜索页面：`kuzhambu-apps/portal-web/src/pages/discovery/search-page.tsx`。
- Portal 问答页面：`kuzhambu-apps/portal-web/src/pages/discovery/qa-page.tsx`。
- Admin 搜索分析页面：`kuzhambu-apps/admin-web/src/pages/discovery/search-admin/search-admin-page.tsx`。
- Admin QA 运维页面：`kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-page.tsx`。
- Workers 已存在 Discovery AI usecase 路径：
  - `/internal/ai/discovery/query-understanding`
  - `/internal/ai/discovery/query-rewrite`
  - `/internal/ai/discovery/answer-generation`
  - `/internal/ai/discovery/answer-generation/stream`
- Discovery 正式 QA 运行时走 Java `KnowledgeBaseClient`，不以 Workers 作为正式问答 runtime。

## 数据结构变更

无数据库 schema 变更。

继续使用现有字段：

- `discovery_search_log.search_log_id`：搜索日志业务号。
- `discovery_search_log.query_text`：原始搜索词。
- `discovery_search_log.normalized_query_text`：清洗后的搜索词。
- `discovery_search_log.display_query_text`：前端回显搜索词。
- `discovery_search_log.intent_type`：查询意图。
- `discovery_search_log.search_scopes_json`：搜索范围 JSON。
- `discovery_search_log.result_total_count`：总命中数。
- `discovery_search_log.group_total_count`：分组数。
- `discovery_search_log.search_status`：搜索状态。
- `discovery_search_log.failure_code`：失败编码。
- `discovery_search_log.failure_message`：失败原因。
- `discovery_search_log.operator_type`：操作者类型。
- `discovery_search_log.operator_id`：操作者 ID。
- `discovery_search_log.request_id`：请求 ID。
- `discovery_search_log.trace_id`：链路 ID。
- `discovery_search_log.created_at`：创建时间。
- `discovery_search_click.search_click_id`：点击日志业务号。
- `discovery_search_click.search_log_id`：关联搜索日志业务号。
- `discovery_search_click.content_domain`：内容域。
- `discovery_search_click.content_type`：内容类型。
- `discovery_search_click.content_id`：内容 ID。
- `discovery_search_click.content_title`：内容标题快照。
- `discovery_search_click.result_group_key`：结果分组键。
- `discovery_search_click.result_rank`：全局排序。
- `discovery_search_click.group_rank`：组内排序。
- `discovery_search_click.target_path`：结果跳转路径。
- `discovery_search_click.operator_type`：操作者类型。
- `discovery_search_click.operator_id`：操作者 ID。
- `discovery_search_click.request_id`：请求 ID。
- `discovery_search_click.trace_id`：链路 ID。
- `discovery_search_click.created_at`：创建时间。
- `discovery_query_understanding.recognized_entities_json`：实体识别 JSON。
- `discovery_query_understanding.expanded_synonyms_json`：同义词扩展 JSON。
- `discovery_qa_session.context_mode`：问答上下文模式，王圻单文档追问使用 `SINGLE_DOCUMENT`。
- `discovery_qa_session.context_content_type`：问答上下文内容类型，王圻单文档追问固定为 `WANGQI_DOCUMENT`。
- `discovery_qa_session.context_content_id`：问答上下文内容 ID。
- `discovery_qa_message.context_turn_count`：上下文轮数。
- `discovery_qa_message_source.source_path`：来源跳转路径。
- `discovery_qa_retrieval_trace.provider_request_json`：provider 请求快照。
- `discovery_qa_retrieval_trace.provider_response_json`：provider 响应快照。

继续使用现有接口字段：

- Portal 搜索响应 `groups[].items[].highlightText`：搜索关键词高亮文本。
- Portal 搜索响应 `groups[].items[].targetPath`：搜索结果深链。
- Portal QA 请求 `metadata.sessionId`：当前会话 ID。
- Portal QA 请求 `metadata.contextContentType`：当前问答上下文内容类型。
- Portal QA 请求 `metadata.contextContentId`：当前问答上下文内容 ID。

## 执行任务

### 1. Backend 搜索高亮和排序收口

目标：让搜索结果项稳定返回 `highlightText`，并让组内排序依据 ES score 与现有 result rank 保持一致。

涉及文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/client/ElasticsearchSearchIndexGateway.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/client/ElasticsearchSearchIndexGatewayTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImplTest.java`

步骤：

1. 将 `ElasticsearchSearchIndexGateway.search(...)` 中调用的 `toGroupedResults(hits)` 改为 `toGroupedResults(hits, keyword.getNormalizedText())`，关键词为空时传 `null`。
2. 在 `ElasticsearchSearchIndexGateway` 增加私有方法 `buildHighlightText(DiscoverySearchDocument document, String keyword)`。
3. `buildHighlightText(...)` 命中顺序固定为 `title`、`summary`、`bodyText`。
4. 命中片段固定规则：取命中词前后各 60 个字符，命中词使用 `<mark>` 和 `</mark>` 包裹。
5. 未命中或关键词为空时，`highlightText` 固定为 `summary` 前 160 个字符；`summary` 为空时使用 `title`。
6. 构造 `SearchResult` 时将 `highlightText` 传入第 6 个构造参数。
7. 补 `ElasticsearchSearchIndexGatewayTest`，覆盖 title/summary/bodyText 命中、空关键词 fallback 和未命中 fallback。
8. 补 `SearchApplicationServiceImplTest`，断言 Portal 搜索结果中的 `highlightText` 非空并透传。

验收：

- 搜索结果项 `highlightText` 不再为 `null`。
- `highlightText` 包含 `<mark>` 时只包裹命中词，不改变 `title` 和 `summary` 原字段。
- 组内 `groupRank` 按 ES 返回顺序递增。

### 2. Backend 搜索点击聚合能力收口

目标：为搜索分析提供点击数只读聚合能力。

涉及文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/repository/SearchClickRepository.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/mapper/SearchClickMapper.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/repository/impl/SearchClickRepositoryImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/search/repository/impl/SearchClickRepositoryImplTest.java`

步骤：

1. 在 `SearchClickRepository` 增加按 `createdAt` 范围查询点击总数的方法。
2. 在 `SearchClickMapper` 增加基于 `created_at` 的计数 SQL。
3. 在 `SearchClickRepositoryImpl` 实现点击总数查询。
4. 补 `SearchClickRepositoryImplTest`，覆盖时间范围内点击数和空结果为 0。

验收：

- 不新增表。
- 点击聚合只读 `discovery_search_click`。
- 空点击结果返回 `0`，不返回 `null`。

### 3. Backend 搜索分析 application 收口

目标：Admin 展示热词、失败统计、点击统计和零结果搜索，形成搜索质量分析闭环。

涉及文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/query/SearchAnalysisSummaryQuery.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/result/SearchAnalysisSummaryResult.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/SearchApplicationService.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImplTest.java`

步骤：

1. 新增 `SearchAnalysisSummaryQuery`，字段固定为：
   - `dateFrom`
   - `dateTo`
2. 新增 `SearchAnalysisSummaryResult`，字段固定为：
   - `searchCount`
   - `failedSearchCount`
   - `zeroResultSearchCount`
   - `clickCount`
   - `topQueries[]`
3. `SearchAnalysisSummaryResult.TopQuery` 字段固定为：
   - `queryText`
   - `count`
4. 在 `SearchApplicationService` 增加 `getAnalysisSummary(SearchAnalysisSummaryQuery query)` 方法。
5. 在 `SearchApplicationServiceImpl` 基于 `SearchLogRepository.listByCreatedAtRange(...)` 和 `SearchClickRepository` 聚合分析字段。
6. 聚合规则固定为：
   - `searchCount` 等于范围内搜索日志数。
   - `failedSearchCount` 等于 `searchStatus` 为 `FAILED` 的日志数。
   - `zeroResultSearchCount` 等于 `resultTotalCount` 为 `0` 的成功日志数。
   - `clickCount` 等于范围内点击日志数。
   - `topQueries` 按 `queryText` 分组计数，按 `count DESC`、`queryText ASC` 排序，最多返回 10 条。
7. 补 application 测试，覆盖成功搜索、失败搜索、零结果搜索、点击数和热门搜索词聚合。

验收：

- 聚合只读现有 `discovery_search_log` 和 `discovery_search_click`。
- 返回字段可直接供 Admin Web 展示。

### 4. Backend Admin 搜索分析接口收口

目标：Admin 搜索调试台可读取搜索分析聚合结果。

涉及文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/DiscoverySearchAdminController.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/request/DiscoverySearchAnalysisSummaryRequest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/response/DiscoverySearchAnalysisSummaryResponse.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/assembler/DiscoverySearchAdminInterfaceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/DiscoverySearchAdminControllerTest.java`

步骤：

1. 新增 Admin 接口 `POST /api/discovery/search-admin/analysis/summary`。
2. 请求字段复用日志分页时间口径：
   - `dateFrom`
   - `dateTo`
3. 响应 `DiscoverySearchAnalysisSummaryResponse` 字段固定为：
   - `searchCount`
   - `failedSearchCount`
   - `zeroResultSearchCount`
   - `clickCount`
   - `topQueries`
4. `DiscoverySearchAnalysisSummaryResponse.TopQueryResponse` 字段固定为：
   - `queryText`
   - `count`
5. 权限描述沿用 `discovery:search:view`。
6. 补 controller 测试，断言路径、请求字段和响应字段。

验收：

- Admin 搜索分析不复用 Operations summary response。
- `analysis/summary` 只暴露 Discovery 搜索质量分析字段。

### 5. Portal 搜索深链、状态恢复和空状态收口

目标：Portal 搜索页支持 URL 查询参数恢复、搜索结果深链点击、无结果空状态和清除筛选提示。

涉及文件：

- `kuzhambu-apps/portal-web/src/pages/discovery/search-page.tsx`
- `kuzhambu-apps/portal-web/src/pages/discovery/search-page.test.tsx`
- `kuzhambu-apps/portal-web/src/pages/discovery/search-service.ts`
- `kuzhambu-apps/portal-web/src/pages/discovery/search-types.ts`

步骤：

1. 在 `search-page.tsx` 使用 `useSearchParams` 读取和写入搜索条件。
2. URL 参数固定为：
   - `q`
   - `knowledgeBases`
   - `categoryCodes`
   - `tagNames`
   - `contentStatuses`
   - `visibilityScopes`
   - `dateFrom`
   - `dateTo`
   - `pageNo`
   - `pageSize`
3. 页面首次加载时，如果 `q` 非空，按 URL 参数自动发起搜索。
4. 点击 `开始检索` 后同步更新 URL 参数。
5. 点击 `重置条件` 后清空 URL 参数并清空结果。
6. 结果项标题渲染为链接控件，控件文本为内容标题，`href` 使用 `item.targetPath`。
7. 点击结果链接前调用 `recordSearchClick(...)`，保留当前点击日志行为。
8. 当 `response.totalCount === 0` 时展示空状态文案：`没有找到匹配内容`。
9. 空状态提供按钮 `清除筛选条件`，点击后保留 `queryText`，清空筛选字段并重新检索。
10. 新增本文件内私有渲染方法 `renderHighlightText(highlightText: string | null | undefined)`。
11. `renderHighlightText(...)` 只识别 `<mark>...</mark>`，将 mark 内文本渲染为 `<mark>` React 元素；其他片段作为普通文本节点输出。
12. 不使用 `dangerouslySetInnerHTML`。
13. 补页面测试，覆盖 URL 恢复、自动搜索、点击日志、空状态、清除筛选和高亮展示。

验收：

- 刷新搜索页后查询条件和结果可恢复。
- 搜索结果返回页时 URL 仍保留查询状态。
- 无结果时有明确空状态和可执行恢复操作。

### 6. Admin 搜索分析页面收口

目标：Admin 搜索调试台展示分析卡片、热词列表和零结果统计。

涉及文件：

- `kuzhambu-apps/admin-web/src/pages/discovery/search-admin/search-admin-service.ts`
- `kuzhambu-apps/admin-web/src/pages/discovery/search-admin/search-admin-types.ts`
- `kuzhambu-apps/admin-web/src/pages/discovery/search-admin/search-admin-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/discovery/search-admin/search-admin-page.test.tsx`

步骤：

1. 在 `search-admin-service.ts` 新增 `getSearchAnalysisSummary(...)`，调用 `/discovery/search-admin/analysis/summary`。
2. 在 `search-admin-types.ts` 新增 `DiscoverySearchAnalysisSummaryRecord`，字段与后端响应一致。
3. 在 `search-admin-page.tsx` 新增卡片 `搜索分析摘要`。
4. 卡片控件固定为：
   - 按钮 `刷新分析`
   - 指标 `搜索次数`
   - 指标 `失败次数`
   - 指标 `零结果次数`
   - 指标 `点击次数`
   - 列表 `热门搜索词`
5. `刷新分析` 使用当前页面 `起始时间` 和 `结束时间` 输入框的值。
6. 当 `topQueries` 为空时展示文案 `暂无热门搜索词`。
7. 补页面测试，覆盖刷新分析、指标展示和空热门词。

验收：

- Admin 用户不需要手动查数据库即可看到搜索质量摘要。
- 页面控件有稳定可访问名称。

### 7. Portal 王圻单文档追问入口收口

目标：Portal QA 支持从 URL 进入王圻单文档追问，并固定会话上下文。

涉及文件：

- `kuzhambu-apps/portal-web/src/pages/discovery/qa-page.tsx`
- `kuzhambu-apps/portal-web/src/pages/discovery/qa-page.test.tsx`
- `kuzhambu-apps/portal-web/src/pages/discovery/qa-service.ts`
- `kuzhambu-apps/portal-web/src/pages/discovery/qa-types.ts`

步骤：

1. 在 `qa-page.tsx` 使用 `useSearchParams` 读取：
   - `contextContentType`
   - `contextContentId`
   - `contextMode`
   - `title`
2. 当 `contextContentType=WANGQI_DOCUMENT` 且 `contextContentId` 为数字时，页面进入单文档追问模式。
3. 单文档追问模式下，`contextMode` 固定为 `SINGLE_DOCUMENT`。
4. 单文档追问模式下，页面展示提示条 `当前围绕王圻文档追问`。
5. 单文档追问模式下，`会话元数据` 中 `上下文` 显示 `WANGQI_DOCUMENT #<contextContentId>`。
6. 首问自动创建会话时，请求 `session/open` 携带：
   - `contextContentType: "WANGQI_DOCUMENT"`
   - `contextContentId`
   - `contextMode: "SINGLE_DOCUMENT"`
   - `title` 使用 URL `title`，为空时使用 `王圻文档问答`
7. 追问 `chat/completions` 时，请求 `metadata` 携带相同 `contextContentType` 和 `contextContentId`。
8. 补页面测试，覆盖 URL 进入单文档追问、自动创建会话、追问 metadata 和提示条展示。

验收：

- 王圻单文档追问不新增独立 provider 路由。
- Portal QA 仍只调用 Discovery API。
- 单文档上下文通过现有 QA session 和 chat metadata 字段传递。

### 8. Backend 王圻单文档问答上下文约束收口

目标：后端对 `WANGQI_DOCUMENT + SINGLE_DOCUMENT` 上下文形成明确约束，并把上下文写入 provider 请求和 trace。

涉及文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeQaApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaTraceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeQaApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/DiscoveryQaPortalControllerTest.java`

步骤：

1. `QaApplicationServiceImpl.openSession(...)` 接收 `contextMode=SINGLE_DOCUMENT` 时校验 `contextContentType` 和 `contextContentId` 非空。
2. 单文档模式仅支持 `contextContentType=WANGQI_DOCUMENT`。
3. `KnowledgeQaApplicationServiceImpl.chatCompletion(...)` 对比 session 上下文和 `metadata.contextContentType/contextContentId`，不一致时抛业务异常。
4. `KnowledgeQaApplicationServiceImpl.toKnowledgeChatRequest(...)` 将单文档上下文写入 provider request `options`，字段固定为：
   - `contextMode`
   - `contextContentType`
   - `contextContentId`
5. `QaTraceAssembler` 的 provider request trace 保留上述上下文字段。
6. 补 application 测试，覆盖合法单文档追问、上下文不一致失败和非 Wangqi 单文档失败。
7. 补 Portal controller 测试，覆盖请求字段映射。

验收：

- 单文档追问只能围绕 `WANGQI_DOCUMENT`。
- provider trace 可看到单文档上下文。
- 回答失败仍保留用户问题和失败 trace。

### 9. Workers Discovery usecase 契约收口

目标：Workers 继续作为 Discovery 查询理解、改写和回答生成技术能力，但不承载正式 QA 会话和知识同步业务事实。

涉及文件：

- `kuzhambu-workers/src/kuzhambu_workers/ai/usecase_registry.py`
- `kuzhambu-workers/tests/test_ai_usecase_routes_discovery.py`
- `docs/20-interfaces/WORKERS-AI-INTERFACE.md`

步骤：

1. 保留现有 Discovery usecase 路径，不新增正式 QA 会话路径。
2. 在 `test_ai_usecase_routes_discovery.py` 增加断言：Workers 不暴露 `/internal/ai/discovery/qa/session/*`。
3. 在 `test_ai_usecase_routes_discovery.py` 增加断言：`answer-generation` 接受 `contextMode/contextContentType/contextContentId` 作为请求 payload 字段。
4. 在 `WORKERS-AI-INTERFACE.md` 明确 `answer_generation` 可接收单文档上下文，但返回结果仍是技术执行结果，不写 Discovery 会话、消息、来源或 trace。

验收：

- Workers 契约支持 Discovery 传入单文档上下文。
- Workers 不新增业务状态、业务写入或 Discovery 正式 QA runtime 路由。

### 10. 文档和覆盖状态收口

目标：实现完成后同步更新需求覆盖并清理本 RUNBOOK。

涉及文件：

- `docs/40-readiness/DISCOVERY-IMPLEMENTATION-COVERAGE.md`
- `docs/20-interfaces/WORKERS-AI-INTERFACE.md`
- `docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-QA-CLOSEOUT.md`

步骤：

1. 更新 `DISCOVERY-IMPLEMENTATION-COVERAGE.md`。
2. 将以下需求项更新为 `已完成`：
   - 组内相关性排序。
   - 关键词高亮。
   - 按知识库、门类、标签、状态、时间筛选。
   - 搜索深链与状态保留。
   - 无结果空状态提示。
   - 王圻单文档追加式问答。
3. 将搜索分析深化从未完成焦点中移除，保留高级相关性运营优化作为 residual risk。
4. 若 Workers 契约补充了单文档上下文字段，同步更新 `WORKERS-AI-INTERFACE.md`。
5. 任务关闭前删除本 RUNBOOK 文件。

验收：

- 覆盖矩阵中的本轮需求项状态与代码事实一致。
- RUNBOOK 不保留在最终收口提交中。

## 前端控件清单

Portal 搜索页需要稳定控件：

- `搜索词`
- `知识库`
- `门类`
- `标签`
- `状态`
- `可见性`
- `起始日期`
- `结束日期`
- `页码`
- `每页数量`
- `开始检索`
- `重置条件`
- `清除筛选条件`
- 结果标题链接，文本使用内容标题。

Portal QA 页需要稳定控件：

- `问题`
- `发送问题`
- `删除会话`
- `导出 CSV`
- `重试`
- 单文档提示条 `当前围绕王圻文档追问`

Admin 搜索页需要稳定控件：

- `刷新分析`
- `搜索次数`
- `失败次数`
- `零结果次数`
- `点击次数`
- `热门搜索词`
- `查询日志`
- `清空结果`
- `查看详情`
- `确认重建索引`
- `重建索引`

## 验证命令

Java：

```sh
cd kuzhambu-servers
mvn -pl biz/discovery -am spotless:apply
mvn -pl biz/discovery -am spotless:check
mvn -pl biz/discovery -am checkstyle:check
mvn -pl biz/discovery -am test
```

Portal Web：

```sh
cd kuzhambu-apps
npm --workspace portal-web run format
npm --workspace portal-web run test -- search-page qa-page
npm run format:check
npm run lint
```

Admin Web：

```sh
cd kuzhambu-apps
npm --workspace admin-web run format
npm --workspace admin-web run test -- search-admin-page discovery-service-contract
npm run format:check
npm run lint
```

Workers：

```sh
cd kuzhambu-workers
.venv/bin/python -m ruff format .
.venv/bin/python -m ruff format --check .
.venv/bin/python -m ruff check .
.venv/bin/python -m pytest tests/test_ai_usecase_routes_discovery.py
```

## 不做事项

- 不新增 `discovery_*` 数据库表。
- 不让 Discovery 直接调用 Workers AI HTTP 接口。
- 不让 Portal/Admin 直连 provider。
- 不把 Workers 变成 Discovery QA 会话、消息、来源、trace 或知识同步状态的事实拥有方。
- 不引入 Outbox。
- 不扩大 Discovery 搜索内容源范围，仍固定 `SANCAI_ENTRY`、`WANGQI_DOCUMENT`、`MING_CUSTOMS`。
