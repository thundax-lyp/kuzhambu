# RUNBOOK Knowledge 同义词查询与运行时冒烟收口

## 目标

把 Knowledge 剩余未完成项“面向搜索和问答的独立正向/反向同义词查询入口”和“Playwright 与跨服务联调冒烟记录”推进到可运行、可验证、可收口的已完成状态。

完成后：

- Knowledge 通过 facade 暴露独立同义词方向查询入口，不新增用户可见 HTTP 查询接口。
- Discovery Search 和 Discovery QA 都通过同一个 Knowledge facade 入口消费同义词扩展。
- Admin Web `/knowledge/taxonomy` 有同义词治理 Playwright 冒烟。
- Portal Web Search 和 QA 有 Playwright 冒烟。
- 跨服务冒烟证据可复现，覆盖文档可以把相关缺口改为 `已完成`。

## 已确认约束

- Knowledge 是同义词词典和方向查询语义的唯一解释方，Discovery 不直接访问 `knowledge_synonym` 表。
- 独立查询入口是 `KnowledgeFacade.querySynonyms(...)`，不是 Admin/Portal HTTP API。
- 正向查询固定表达 `term -> synonym`；反向查询固定表达 `synonym -> term`；双向查询合并正反向结果。
- 保留现有 `KnowledgeFacade.expandSynonyms(...)`，实现上复用新的双向查询，保证既有调用方兼容。
- QA 同义词增强只进入 Discovery 检索/上下文增强，不改变 `KnowledgeBaseClient` provider 路由，不向前端暴露 Knowledge 内部词典行。
- Admin Playwright 只覆盖 `/knowledge/taxonomy` 的同义词治理冒烟；Portal Playwright 只覆盖 Discovery Search 和 QA 冒烟。
- 只有代码、测试、Playwright、跨服务冒烟证据和覆盖文档全部完成后，才能把 `docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md` 相关项改为 `已完成`。
- PR 收口时删除本 RUNBOOK，保留稳定覆盖文档和运行时证据文档。

## 拆分原则

- 每个后端实现任务固定控制在 2-5 个相关文件内；超过 5 个文件时继续拆分。
- 每个前端任务固定按“生产配置 / e2e spec / 操作控件 / 请求断言”描述，不把页面重构混入冒烟任务。
- 本次不改 Admin Web 或 Portal Web 的生产 UI 交互，除非 e2e 暴露出已经存在但无法操作的可访问性缺口。
- 每个任务的验收点必须能由单测、Playwright 或跨服务证据直接检查。

任务索引：

| 任务 | 范围 | 文件数 | 产出 |
| --- | --- | --- | --- |
| 任务 1 | Knowledge application 方向查询 | 4 | 新 application result 与读协作方法 |
| 任务 2 | Knowledge facade 契约与实现 | 5 | 新 facade request/response/dto 与 facade 方法 |
| 任务 3 | Knowledge facade 装配测试 | 2 | response 装配与兼容测试 |
| 任务 4 | Discovery Search 消费 | 3 | Search 使用新 facade 入口 |
| 任务 5 | Discovery QA 消费 | 3 | QA metadata 注入扩展词 |
| 任务 6 | Knowledge 方向查询单测 | 1 | 方向、limit、去重规则测试 |
| 任务 7 | Admin taxonomy Playwright | 1 | 同义词治理 e2e |
| 任务 8 | Portal Playwright 配置 | 2 | Portal e2e 启动能力 |
| 任务 9 | Portal Search Playwright | 1 | Search e2e |
| 任务 10 | Portal QA Playwright | 1 | QA e2e |

## 数据结构变更

### 后端 application 结果

新增文件：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/result/DiscoverySynonymQueryResult.java`

字段：

| 字段 | Java 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `term` | `String` | 是 | 调用方原始输入 |
| `normalizedTerm` | `String` | 否 | `StringUtils.trimToNull(term)` 后的查询词；空白输入时为空 |
| `direction` | `String` | 是 | 实际执行方向：`FORWARD`、`REVERSE`、`BIDIRECTIONAL` |
| `limit` | `int` | 是 | 实际使用的结果上限；默认 50；调用方传入值小于 1 时使用 50；大于 50 时压回 50 |
| `matches` | `List<DiscoverySynonymMatchResult>` | 是 | 去重后的方向命中明细；无结果为空列表 |

新增文件：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/result/DiscoverySynonymMatchResult.java`

字段：

| 字段 | Java 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `sourceTerm` | `String` | 是 | `knowledge_synonym.term` |
| `targetTerm` | `String` | 是 | `knowledge_synonym.synonym` |
| `matchedTerm` | `String` | 是 | 本次匹配到的词；正向为 `sourceTerm`，反向为 `targetTerm` |
| `expandedTerm` | `String` | 是 | 返回给 Discovery 扩展的词；正向为 `targetTerm`，反向为 `sourceTerm` |
| `direction` | `String` | 是 | 当前明细方向：`FORWARD` 或 `REVERSE` |

规则：

- `FORWARD` 只查 `term = normalizedTerm` 且 `status = ENABLED`。
- `REVERSE` 只查 `synonym = normalizedTerm` 且 `status = ENABLED`。
- `BIDIRECTIONAL` 先追加正向结果，再追加反向结果，按 `expandedTerm` 去重并保留首次出现顺序。
- `expandedTerm` 为空或等于 `normalizedTerm` 时不进入 `matches`。
- 空白输入返回 `normalizedTerm = null`、`matches = List.of()`，不抛异常。

### 后端 facade 请求

新增文件：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/request/KnowledgeSynonymQueryFacadeRequest.java`

字段：

| 字段 | Java 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `term` | `String` | 是 | 搜索或问答输入词 |
| `direction` | `String` | 否 | `FORWARD`、`REVERSE`、`BIDIRECTIONAL`；空值或非法值按 `BIDIRECTIONAL` 处理 |
| `limit` | `Integer` | 否 | 结果上限；由 Knowledge application 收窄到 1-50 |

### 后端 facade 响应

新增文件：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/response/KnowledgeSynonymQueryFacadeResponse.java`

字段：

| 字段 | Java 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `term` | `String` | 是 | 调用方原始输入 |
| `normalizedTerm` | `String` | 否 | 规范化查询词 |
| `direction` | `String` | 是 | 实际执行方向 |
| `limit` | `int` | 是 | 实际结果上限 |
| `matches` | `List<KnowledgeSynonymMatchFacadeDto>` | 是 | 方向命中明细 |
| `expandedTerms` | `List<String>` | 是 | 从 `matches.expandedTerm` 派生的轻量列表，方便 Discovery 直接消费 |

新增文件：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/dto/KnowledgeSynonymMatchFacadeDto.java`

字段：

| 字段 | Java 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `sourceTerm` | `String` | 是 | 词典主词 |
| `targetTerm` | `String` | 是 | 词典同义词 |
| `matchedTerm` | `String` | 是 | 匹配词 |
| `expandedTerm` | `String` | 是 | 扩展词 |
| `direction` | `String` | 是 | `FORWARD` 或 `REVERSE` |

### Discovery Search 数据流

不新增数据库字段。继续写入已有字段：

- `discovery_query_understanding.expanded_synonyms_json`

写入内容保持 JSON 字符串数组，只保存 `KnowledgeSynonymQueryFacadeResponse.expandedTerms`，不保存内部 `matches` 明细。

### Discovery QA provider metadata/options

不新增对外 API 字段。`KnowledgeQaApplicationServiceImpl` 在构造 `KnowledgeChatRequest` 时，把扩展词放入 provider metadata 或 options 的内部键：

| 位置 | Key | Java 类型 | 说明 |
| --- | --- | --- | --- |
| `metadata` | `expandedSynonyms` | `List<String>` | 用于 provider 检索增强和 trace 观察 |
| `metadata` | `synonymQueryTerm` | `String` | 当前用户最新问题 |

不得把 `matches`、`sourceTerm`、`targetTerm` 透传给 Portal 响应。

### Admin Web e2e 请求字段

Admin Web 不新增生产类型，只验证现有 `taxonomy-service.ts` 请求字段。

`SynonymPageQuery`：

| 字段 | TypeScript 类型 | 冒烟断言 |
| --- | --- | --- |
| `pageNo` | `number` | 搜索后为 `1` |
| `pageSize` | `number` | 保持当前分页大小 |
| `term` | `string \| null` | 搜索框输入 `礼制` 后为 `礼制` |
| `synonym` | `string \| null` | 本次不作为控件输入 |
| `status` | `string \| null` | 本次不作为控件输入 |

`SynonymCreateCommand`：

| 字段 | TypeScript 类型 | 冒烟断言 |
| --- | --- | --- |
| `id` | `string` | 非空 |
| `term` | `string` | `礼制` |
| `synonym` | `string` | `礼学` |
| `status` | `string \| null` | 可为空 |

`SynonymUpdateCommand`：

| 字段 | TypeScript 类型 | 冒烟断言 |
| --- | --- | --- |
| `id` | `string` | 当前编辑行 ID |
| `term` | `string` | 当前编辑后的术语 |
| `synonym` | `string` | `典礼` |

`SynonymStatusCommand`：

| 字段 | TypeScript 类型 | 冒烟断言 |
| --- | --- | --- |
| `id` | `string` | 当前行 ID |
| `status` | `string` | `ENABLED` 或 `DISABLED` |

`SynonymRemoveCommand`：

| 字段 | TypeScript 类型 | 冒烟断言 |
| --- | --- | --- |
| `id` | `string` | 当前行 ID |

### Portal Web e2e 请求字段

Portal Web 不新增生产类型，只验证现有 Discovery 请求。

`DiscoverySearchRequest`：

| 字段 | TypeScript 类型 | 冒烟断言 |
| --- | --- | --- |
| `queryText` | `string` | `礼学` |
| `knowledgeBases` | `string[]` | 包含 `SANCAI_ENTRY` |
| `categoryCodes` | `string[]` | 默认空数组 |
| `tagNames` | `string[]` | 默认空数组 |
| `contentStatuses` | `string[]` | 默认空数组 |
| `visibilityScopes` | `string[]` | 默认空数组 |
| `pageNo` | `number` | `1` |
| `pageSize` | `number` | `10` |

`DiscoveryQaChatCompletionRequest`：

| 字段 | TypeScript 类型 | 冒烟断言 |
| --- | --- | --- |
| `model` | `string` | `kuzhambu-qa` |
| `stream` | `boolean` | `false` |
| `messages[0].role` | `string` | `user` |
| `messages[0].content` | `string` | `礼学和礼制有什么关系？` |
| `metadata.sessionId` | `number` | `7001` |
| `metadata.contextMode` | `string \| null` | `GENERAL` 或为空 |
| `requestId` | `string \| null` | 表单为空时为空 |
| `traceId` | `string \| null` | 表单为空时为空 |

## 后端任务拆分

### 任务 1：Knowledge application 方向查询

文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/KnowledgeTaxonomyReadApplicationService.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/KnowledgeTaxonomyReadApplicationServiceImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/result/DiscoverySynonymQueryResult.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/result/DiscoverySynonymMatchResult.java`

动作：

- 在 `KnowledgeTaxonomyReadApplicationService` 新增 `DiscoverySynonymQueryResult querySynonyms(String term, String direction, Integer limit)`。
- 在 `KnowledgeTaxonomyReadApplicationServiceImpl` 增加方向解析、limit 收窄、正向收集、反向收集和去重。
- 将当前 `expandSynonyms(String term)` 改为调用 `querySynonyms(term, "BIDIRECTIONAL", null)`，再从 `matches` 派生 `DiscoverySynonymExpandResult.expandedTerms`。
- 保持 `DEFAULT_EXPAND_LIMIT = 50` 作为最大上限。

验收：

- `querySynonyms("礼制", "FORWARD", 10)` 只返回 `term = 礼制` 的同义词。
- `querySynonyms("礼制", "REVERSE", 10)` 只返回 `synonym = 礼制` 的主词。
- `querySynonyms("礼制", "BIDIRECTIONAL", null)` 合并正反向结果并去重。
- `querySynonyms(" ", "BIDIRECTIONAL", null)` 返回空 `matches`。

### 任务 2：Knowledge facade 契约与装配

文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/KnowledgeFacade.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/request/KnowledgeSynonymQueryFacadeRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/response/KnowledgeSynonymQueryFacadeResponse.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/dto/KnowledgeSynonymMatchFacadeDto.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/facade/impl/KnowledgeFacadeImpl.java`

动作：

- 在 `KnowledgeFacade` 新增 `KnowledgeSynonymQueryFacadeResponse querySynonyms(KnowledgeSynonymQueryFacadeRequest request)`。
- 新增 request、response、dto，字段按“后端 facade 请求/响应”定义。
- 在 `KnowledgeFacadeImpl` 对 `null` request 返回 `null`，保持现有 facade 兼容口径。
- `KnowledgeFacadeImpl.querySynonyms` 调用 `KnowledgeTaxonomyReadApplicationService.querySynonyms(...)`。

验收：

- 外域可以通过 facade 明确传入方向和 limit。
- `expandSynonyms(...)` 仍可用，旧测试不因新增入口破坏。

### 任务 3：Knowledge facade assembler

文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/facade/assembler/KnowledgeFacadeAssembler.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/facade/impl/KnowledgeFacadeImplTest.java`

动作：

- 在 `KnowledgeFacadeAssembler` 新增 `toSynonymQueryResponse(DiscoverySynonymQueryResult result)`。
- assembler 同时组装 `matches` 和 `expandedTerms`。
- `KnowledgeFacadeImplTest` 增加断言：`direction`、`limit`、`matches[0].sourceTerm`、`matches[0].targetTerm`、`matches[0].expandedTerm`、`expandedTerms`。
- `KnowledgeFacadeImplTest` 保留旧 `expandSynonyms` 兼容断言。

验收：

- facade response 不需要 Discovery 再解析明细即可直接读取扩展词。

### 任务 4：Discovery Search 消费新入口

文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/DiscoveryKnowledgeEnhancementProvider.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/QueryUnderstandingApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/QueryUnderstandingApplicationServiceImplTest.java`

动作：

- `DiscoveryKnowledgeEnhancementProvider.enhance(String term)` 改为构造 `KnowledgeSynonymQueryFacadeRequest.builder().term(term).direction("BIDIRECTIONAL").limit(50).build()`。
- 从 `KnowledgeSynonymQueryFacadeResponse.expandedTerms` 读取扩展词。
- 保留 `getTagHint` 和 `listEntityHints` 现有逻辑。
- `QueryUnderstandingApplicationServiceImpl` 不新增数据库字段，继续把扩展词写入 `expanded_synonyms_json`。
- 测试用 Mockito 验证 `KnowledgeFacade.querySynonyms(...)` 被调用，且 query understanding result 的 `expandedSynonyms` 包含正反向结果。

验收：

- Search 对 `礼制` 可消费正向和反向扩展词。
- `discovery_query_understanding.expanded_synonyms_json` 仍是扩展词数组 JSON。

### 任务 5：Discovery QA 消费新入口

文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeQaApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/DiscoveryKnowledgeEnhancementProvider.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeQaApplicationServiceImplTest.java`

动作：

- 给 `KnowledgeQaApplicationServiceImpl` 注入 `DiscoveryKnowledgeEnhancementProvider`。
- 在 `chatCompletion(...)` 中使用 `extractLatestQuestion(command.getMessages())` 得到的问题调用 `enhance(question)`。
- 在 `enrichedMetadata(...)` 中追加：
  - `synonymQueryTerm`: 最新问题文本。
  - `expandedSynonyms`: `KnowledgeEnhancementResult.expandedSynonyms()`。
- 不改变 `KnowledgeChatRequest.model/messages/stream/options` 的现有语义。
- 测试断言传给 `KnowledgeBaseClient.chat(...)` 的 request metadata 包含 `expandedSynonyms`，且 Portal 响应 sources/choices 不包含词典明细。

验收：

- QA 消费 Knowledge 同义词扩展，但 provider 路由仍由 `KnowledgeBaseClient` 和 model 决定。

### 任务 6：Knowledge application 单测

文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/KnowledgeTaxonomyReadApplicationServiceImplTest.java`

动作：

- 增加 `querySynonymsShouldReturnForwardMatchesOnly`。
- 增加 `querySynonymsShouldReturnReverseMatchesOnly`。
- 增加 `querySynonymsShouldMergeBidirectionalMatchesWithDeduplication`。
- 增加 `querySynonymsShouldClampLimitAndIgnoreBlankExpandedTerm`。
- 增加 `expandSynonymsShouldReuseBidirectionalQuery` 或保留并强化现有 `expandSynonymsShouldMergeDirectAndReverseMatches`。

验收：

- 方向查询的核心规则由 Knowledge 自己的单测锁定。

## 前端与 Playwright 任务拆分

### 任务 7：Admin taxonomy 同义词 Playwright 冒烟

文件：

- `kuzhambu-apps/admin-web/e2e/knowledge/taxonomy/taxonomy.spec.ts`

控件和操作：

- 打开 `/knowledge/taxonomy`。
- 预置 `localStorage`：
  - `kuzhambu.admin.accessToken = test-token`
  - 权限包含 `knowledge:taxonomy:view` 和 `knowledge:taxonomy:edit`。
- route mock 以下接口：
  - `POST /api/knowledge/taxonomy/category/page`
  - `POST /api/knowledge/taxonomy/tag/page`
  - `POST /api/knowledge/taxonomy/tag/review/page`
  - `POST /api/knowledge/taxonomy/synonym/page`
  - `POST /api/knowledge/taxonomy/synonym/create`
  - `POST /api/knowledge/taxonomy/synonym/update`
  - `POST /api/knowledge/taxonomy/synonym/status`
  - `POST /api/knowledge/taxonomy/synonym/delete`
- 点击 `同义词` tab。
- 在 `SynonymTable` 的搜索框输入 `礼制`，断言 `/synonym/page` 请求体包含 `term: "礼制"`、`pageNo: 1`。
- 点击 `新增` 按钮，打开 `SynonymEdit` 抽屉。
- 在 `术语` 输入框填写 `礼制`。
- 在 `同义词` 输入框填写 `礼学`。
- 点击抽屉 footer 的 `新增` 按钮，断言 `/synonym/create` 请求体包含 `id`、`term: "礼制"`、`synonym: "礼学"`。
- 点击表格行操作 `编辑`，打开 `编辑同义词` 抽屉。
- 修改 `同义词` 为 `典礼`，点击 `保存`，断言 `/synonym/update` 请求体包含 `id`、`term`、`synonym: "典礼"`。
- 点击状态开关，断言 `/synonym/status` 请求体包含 `id` 和 `status: "DISABLED"` 或 `ENABLED`。
- 点击表格行操作 `删除`，确认弹窗后断言 `/synonym/delete` 请求体包含 `id`。

验收：

- `pnpm --filter kuzhambu-admin-web run e2e -- e2e/knowledge/taxonomy/taxonomy.spec.ts` 通过。
- 冒烟证明 Admin 同义词治理入口可渲染、可搜索、可新增、可编辑、可改状态、可删除。

### 任务 8：Portal Web Playwright 配置

文件：

- `kuzhambu-apps/portal-web/package.json`
- `kuzhambu-apps/portal-web/playwright.config.ts`

动作：

- 在 `package.json` 增加脚本：`"e2e": "playwright test"`。
- 增加 `playwright.config.ts`，配置：
  - `testDir: "./e2e"`
  - `baseURL: "http://127.0.0.1:5174"`
  - `webServer.command: "pnpm run dev -- --port 5174"`
  - `webServer.url: "http://127.0.0.1:5174"`
  - `reuseExistingServer: true`
  - 至少一个 `mobile-chrome` project，沿用 Admin Web 风格。

验收：

- Portal e2e 可以由 `pnpm --filter @kuzhambu/portal-web run e2e` 启动。

### 任务 9：Portal Search Playwright 冒烟

文件：

- `kuzhambu-apps/portal-web/e2e/discovery/search.spec.ts`

控件和操作：

- route mock `POST /api/portal/discovery/search/search`，返回：
  - `searchLogId: "search-log-1"`
  - `queryText: "礼学"`
  - `displayQueryText: "礼学"`
  - `totalCount: 1`
  - `groupCount: 1`
  - `groups[0].groupKey: "SANCAI_ENTRY"`
  - `groups[0].groupTitle: "三才图会"`
  - `groups[0].items[0].title: "礼制条目"`
  - `groups[0].items[0].highlightText: "<mark>礼学</mark> 与礼制"`
  - `groups[0].items[0].targetPath: "/knowledge/atlas?level=detail&entityId=1001"`
- route mock `POST /api/portal/discovery/search/click`。
- 打开 `/discovery/search`。
- 在 `搜索词` 输入框输入 `礼学`。
- 点击 `三才图会` 知识库按钮。
- 点击提交按钮。
- 断言 URL query 包含 `q=礼学` 和 `knowledgeBases=SANCAI_ENTRY`。
- 断言页面展示 `共 1 条命中`、`三才图会`、`礼制条目` 和高亮片段。
- 点击结果项链接或结果动作，断言 `/search/click` 请求体包含 `searchLogId`、`contentType`、`contentId`、`resultGroupKey`。

验收：

- Search 页面在浏览器环境下完成查询、筛选、结果展示和点击记录。

### 任务 10：Portal QA Playwright 冒烟

文件：

- `kuzhambu-apps/portal-web/e2e/discovery/qa.spec.ts`

控件和操作：

- route mock `POST /api/portal/discovery/qa/session/page`，首次返回空会话列表。
- route mock `POST /api/portal/discovery/qa/session/open`，返回：
  - `sessionId: 7001`
  - `title: "知识中心问答"`
  - `scope: "PORTAL"`
  - `contextMode: "GENERAL"`
  - `status: "OPEN"`
- route mock `POST /api/portal/discovery/qa/session/get`，返回同一个会话。
- route mock `POST /api/portal/discovery/qa/chat/completions`，返回：
  - `sessionId: 7001`
  - `answerStatus: "SUCCEEDED"`
  - `choices[0].message.role: "assistant"`
  - `choices[0].message.content: "礼学可作为礼制相关内容的检索扩展。"`
  - `sources[0].sourceId: "SANCAI_ENTRY:1001"`
  - `sources[0].titleSnapshot: "礼制条目"`
  - `sources[0].sourcePath: "/knowledge/atlas?level=detail&entityId=1001"`
- 打开 `/discovery/qa`。
- 在 `问题` textarea 输入 `礼学和礼制有什么关系？`。
- 点击发送按钮。
- 断言自动调用 `/session/open`。
- 断言 `/chat/completions` 请求体包含：
  - `model: "kuzhambu-qa"`
  - `stream: false`
  - `messages[0].role: "user"`
  - `messages[0].content: "礼学和礼制有什么关系？"`
  - `metadata.sessionId: 7001`
- 断言页面展示用户问题、回答文本、来源 `礼制条目`。
- 点击 `导出 CSV`，route mock `/session/export` 并断言按钮操作可触发。
- 点击 `删除会话`，确认后 route mock `/session/delete` 并断言会话移出当前选择。

验收：

- QA 页面在浏览器环境下完成自动建会话、提问、回答、来源展示、导出和删除最小路径。

## 跨服务冒烟证据

新增文件：`docs/40-readiness/KNOWLEDGE-RUNTIME-SMOKE-EVIDENCE.md`

字段结构：

| 小节 | 必填内容 |
| --- | --- |
| `环境` | 日期、分支、commit、Java 版本、Maven 版本、Node 版本、pnpm 版本、Python 版本、数据库/ES/provider stub 状态 |
| `数据准备` | 同义词 `礼制 -> 礼学`、反向样例 `典礼 -> 礼制`、可搜索内容、可问答来源 |
| `Knowledge 同义词` | 正向查询请求/响应摘要、反向查询请求/响应摘要、双向查询请求/响应摘要 |
| `Discovery Search` | 请求词、扩展词、搜索结果数量、`searchLogId`、`expanded_synonyms_json` 验证方式 |
| `Discovery QA` | 用户问题、扩展词进入 metadata/options 的证据、回答来源摘要、trace 验证方式 |
| `Admin Web Playwright` | 命令、spec 文件、结果 |
| `Portal Web Playwright` | 命令、spec 文件、结果 |
| `跨服务命令` | Admin starter、Portal starter、workers/provider stub 启动命令和关键日志 |
| `结论` | 是否满足 Knowledge 覆盖文档中两个未完成项清零条件 |

要求：

- 证据文档必须写实际命令和结果，不写“已验证”占位。
- 如果 provider 使用 stub，必须写明 stub 地址、返回样例和限制。
- 如果某项失败，覆盖文档不得改为 `已完成`。

## 文档收口

文件：

- `docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`
- `docs/40-readiness/DISCOVERY-IMPLEMENTATION-COVERAGE.md`
- `docs/40-readiness/KNOWLEDGE-RUNTIME-SMOKE-EVIDENCE.md`
- `docs/30-designs/RUNBOOK-KNOWLEDGE-SYNONYM-SMOKE-CLOSURE.md`

动作：

- 代码、测试、Playwright 和跨服务证据全部完成后，将 Knowledge 覆盖文档中“同义词正向和反向查询”和“当前阶段运行时验证”改为 `已完成`。
- Discovery 覆盖文档若仍记录同义词消费或 QA/Search 验证缺口，同步改为 `已完成` 并引用证据文档。
- PR 收口时删除本 RUNBOOK。

验收：

- 覆盖文档不再保留“缺少独立正向/反向查询入口”或“缺少 Playwright 与跨服务联调冒烟记录”的未完成描述。
- RUNBOOK 不随已关闭任务长期留存。

## 验证命令

Java 后端：

```sh
cd kuzhambu-servers
mvn -pl biz/knowledge -am spotless:apply
mvn -pl biz/discovery -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/knowledge,biz/discovery -am test
```

Admin Web：

```sh
cd kuzhambu-apps
pnpm --filter kuzhambu-admin-web run format
pnpm run format:check
pnpm run lint
pnpm --filter kuzhambu-admin-web run test
pnpm --filter kuzhambu-admin-web run e2e -- e2e/knowledge/taxonomy/taxonomy.spec.ts
```

Portal Web：

```sh
cd kuzhambu-apps
pnpm --filter @kuzhambu/portal-web run format
pnpm run format:check
pnpm run lint
pnpm --filter @kuzhambu/portal-web run test
pnpm --filter @kuzhambu/portal-web run e2e -- e2e/discovery/search.spec.ts e2e/discovery/qa.spec.ts
```

跨服务冒烟：

```sh
set -a
source dev.env
set +a
cd kuzhambu-servers
mvn -pl starter/kuzhambu-admin-starter -am -DskipTests install
cd starter/kuzhambu-admin-starter
mvn spring-boot:run
```

Portal starter、workers 或 provider stub 的启动命令按实际环境写入 `docs/40-readiness/KNOWLEDGE-RUNTIME-SMOKE-EVIDENCE.md`。

## 最终验收

- Knowledge read service 和 facade 都支持独立正向、反向、双向同义词查询。
- Discovery Search 与 QA 均通过 `KnowledgeFacade.querySynonyms(...)` 消费同一个入口。
- 后端单测覆盖方向查询、facade 装配、Search 消费和 QA 消费。
- Admin Web 有同义词治理 Playwright 冒烟。
- Portal Web 有 Search 与 QA Playwright 冒烟。
- 跨服务冒烟证据文档包含命令、数据、结果和结论。
- Knowledge 和 Discovery 覆盖文档完成收口，不再保留本目标的未完成描述。
