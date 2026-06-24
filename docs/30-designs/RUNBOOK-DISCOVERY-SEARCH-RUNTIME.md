# RUNBOOK Discovery Search Runtime

## Purpose

本文档用于执行 Discovery `Search` 子能力域的第一个真实需求闭环。

本轮目标不是继续扩展骨架，而是把当前已经存在的 Discovery Search 协议、仓储、Gateway 和 starter 装配推进成一条真实可运行链路：

- Classics 内容读取
- `DiscoverySearchDocument` 文档生成
- Elasticsearch 索引写入
- Elasticsearch 检索执行
- Search 日志落库
- Portal 搜索接口返回真实结果
- Admin 日志接口能查询到真实搜索日志

本轮只交付 Search 运行时闭环，不纳入 QA、多轮会话、来源引用、同义词扩展、实体增强、搜索分析页面。

## Scope

本轮纳入范围：

- Search 索引文档生成与全量重建。
- Search 真实检索实现。
- Search 成功 / 失败日志真实写入。
- Portal 搜索接口返回真实分组结果。
- Admin 搜索日志分页、详情读取真实日志。
- Search 运行时测试和最小 Maven 验证。

本轮不纳入范围：

- Query Understanding 真实实现。
- 同义词扩展、停用词、实体识别、实体链接。
- Elasticsearch 增量更新。
- Portal 搜索页面。
- Admin 搜索分析页面。
- 高亮、相关性调优、复杂排序规则。

## Fixed Decisions

- 业务子域仍固定为 `search`。
- 内容源范围固定为 `SANCAI_ENTRY`、`WANGQI_DOCUMENT`、`MING_CUSTOMS`。
- 搜索索引仍以 Elasticsearch 为默认实现。
- 本轮先做 `全量重建 + 实时查询`，不做增量同步。
- Portal 搜索接口仍使用 `POST /api/portal/discovery/search/search`。
- Admin 搜索日志接口仍使用：
  - `POST /api/discovery/search-admin/logs/page`
  - `POST /api/discovery/search-admin/logs/get`
- `sourcePath` 当前只定义为稳定占位路径，不承诺最终 Portal 路由口径。
- 本轮保留 Admin 手动重建索引接口，作为运行时联调与排错入口。
- Discovery 允许先组合调用现有 Classics application service，不允许直接依赖 Classics mapper 或 DO。
- Admin 触发索引重建时新增权限码 `discovery:search:edit`，不与 `discovery:search:view` 共用。
- Portal 搜索结果本轮只返回当前可公开消费内容，不返回“存在但当前前台不可访问”的内容。
- 本轮 Search 目标固定为“找得到”，不以“找得准”为交付目标；高亮、同义词、实体增强和相关性调优均不作为本轮验收前置。
- Admin 本轮只提供运维入口，不交付搜索分析看板、热词统计或点击分布分析。
- 失败请求必须写 `discovery_search_log`，不得只抛异常不留日志。
- `highlightText` 字段仍保留，但本轮允许返回 `null`。
- `QueryUnderstandingApplicationService` 本轮继续保留未实现异常，不并入主链路。

## Runtime Closure Definition

本轮闭环完成的判定标准：

1. 能从 Classics 三类内容源构造 `DiscoverySearchDocument`。
2. 能执行一次全量索引重建，将文档写入 Elasticsearch。
3. `ElasticsearchSearchIndexGateway.search()` 不再抛未实现异常。
4. Portal 搜索接口能返回真实 `groups/items/targetPath`。
5. `discovery_search_log` 能记录成功和失败搜索。
6. Admin 接口能分页查看搜索日志并查看详情。
7. 相关测试通过，且 starter 不缺 Discovery 运行时 bean。

## Runtime Data Mapping

### Content Source -> `DiscoverySearchDocument`

`SANCAI_ENTRY`

- `documentId`: `SANCAI_ENTRY:{entryId}`
- `contentDomain`: `CLASSICS`
- `contentType`: `SANCAI_ENTRY`
- `contentId`: `entryId`
- `knowledgeBase`: `SANCAI_ENTRY`
- `categoryCode`: 三才分类或门类编码；若当前 domain 无稳定编码，先用 `categoryId` 字符串
- `categoryName`: 分类标题
- `title`: 条目标题
- `summary`: 条目摘要，优先 `summary` 字段
- `bodyText`: `originalText + translationText + summary`
- `tagNames`: 当前没有稳定标签时先为空列表
- `status`: 生命周期状态
- `visibility`: 可见性
- `publishedAt`: 内容更新时间或当前版本时间
- `updatedAt`: 内容更新时间
- `sourcePath`: `/classics/sancai/{entryId}`，当前为稳定占位路径

`WANGQI_DOCUMENT`

- `documentId`: `WANGQI_DOCUMENT:{documentId}`
- `contentDomain`: `CLASSICS`
- `contentType`: `WANGQI_DOCUMENT`
- `contentId`: `documentId`
- `knowledgeBase`: `WANGQI_DOCUMENT`
- `categoryCode`: 文档类型或集合编码；若无稳定字段则留空
- `categoryName`: 文档类型名；若无则留空
- `title`: 文档标题
- `summary`: 文档摘要
- `bodyText`: 标题 + 摘要 + 正文纯文本
- `tagNames`: 当前先空
- `status`: 文档状态
- `visibility`: 文档可见性
- `publishedAt`: 发布时间或更新时间
- `updatedAt`: 更新时间
- `sourcePath`: `/classics/wangqi/{documentId}`，当前为稳定占位路径

`MING_CUSTOMS`

- `documentId`: `MING_CUSTOMS:{customsId}`
- `contentDomain`: `CLASSICS`
- `contentType`: `MING_CUSTOMS`
- `contentId`: `customsId`
- `knowledgeBase`: `MING_CUSTOMS`
- `categoryCode`: 习俗分类编码；无稳定字段时先用分类 ID 字符串
- `categoryName`: 分类标题
- `title`: 习俗标题
- `summary`: 习俗摘要
- `bodyText`: 标题 + 摘要 + 正文
- `tagNames`: 当前先空
- `status`: 习俗状态
- `visibility`: 可见性
- `publishedAt`: 发布时间或更新时间
- `updatedAt`: 更新时间
- `sourcePath`: `/classics/ming-customs/{customsId}`，当前为稳定占位路径

### Search Grouping Rule

Search 返回分组键固定按 `contentType` 分组：

- `SANCAI_ENTRY`
- `WANGQI_DOCUMENT`
- `MING_CUSTOMS`

分组标题固定为：

- `三才图会`
- `王圻文档`
- `明代习俗`

### Search Log Write Rule

成功搜索写入：

- `search_status = SUCCEEDED`
- `result_total_count`
- `group_total_count`

失败搜索写入：

- `search_status = FAILED`
- `failure_code`
- `failure_message`
- `result_total_count = 0`
- `group_total_count = 0`

## File-Level Execution Plan

### T1a 建立 Discovery 内容读取抽象

目标：先在 Discovery 内建立可复用的内容读取抽象，不直接读 Classics 表。

涉及文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/SearchContentProvider.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/result/SearchSourceContent.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/provider/ClassicsSearchContentProvider.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/pom.xml`

执行动作：

- 定义 `SearchContentProvider`，提供三类内容源的读取能力。
- 定义统一结果模型 `SearchSourceContent`，屏蔽 Classics 各对象差异。
- 在 `infra` 提供 `ClassicsSearchContentProvider` 默认实现，通过 Classics application 能力组装读取。
- 如需新增依赖，只允许增加 Classics application 依赖，不允许直接依赖 Classics persistence mapper。

验收点：

- Discovery application 不感知 Classics 内部 entity/DO。
- Discovery infra 能拿到三类内容源的统一搜索内容对象。

### T1b 补齐 Classics 可搜索内容读取能力

目标：在 Classics application 内新增统一 facade，稳定提供“当前可公开消费内容”。

涉及文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/search/service/ClassicsSearchContentApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/search/service/impl/ClassicsSearchContentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/search/result/ClassicsSearchSourceContent.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/search/ClassicsSearchContentApplicationServiceImplTest.java`

执行动作：

- 在 Classics application 内新增面向 Discovery 的统一搜索读取 facade。
- 由 facade 组合 `SancaiApplicationService`、`WangqiDocumentApplicationService`、`MingCustomsApplicationService`。
- 保证返回对象只包含可搜索、可公开消费内容。
- 不允许把 Discovery 的搜索模型反向带入 Classics。

验收点：

- Discovery 侧不需要直接读 mapper 或 persistence 对象。
- 三类内容源都能稳定读取到当前可公开消费内容。

### T2 建立索引文档转换器

目标：把统一内容模型稳定映射成 `DiscoverySearchDocument`。

涉及文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/client/DiscoverySearchDocumentAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/result/SearchSourceContent.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/client/DiscoverySearchDocumentAssemblerTest.java`

执行动作：

- 新增 `DiscoverySearchDocumentAssembler`。
- 明确三类内容源到 `DiscoverySearchDocument` 的字段映射。
- 为 `bodyText` 定义纯文本拼接规则，去掉 `null` 和空白片段。
- 为 `sourcePath` 定义稳定站内路径。

验收点：

- 三类内容源都能稳定得到 `documentId / title / bodyText / sourcePath`。
- 不允许把 HTML 或富文本原样写入 `bodyText`。

### T3a 新增索引重建应用服务

目标：先在 application 层形成正式的索引重建用例入口。

涉及文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/SearchIndexApplicationService.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchIndexApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/SearchContentProvider.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchIndexApplicationServiceImplTest.java`

执行动作：

- 新增 `SearchIndexApplicationService`。
- 在实现中读取三类内容源，转换为索引文档，再调用既有 Gateway 能力。
- 不为本轮引入增量同步、消息驱动重建或后台调度；先保证手动全量重建稳定可用。

验收点：

- Application 层有明确索引重建入口。
- 索引重建流程可单元测试，不依赖真实 ES 启动。

### T3b 扩展 SearchIndexGateway 重建签名

目标：让 Gateway 正式承载索引重建写入，不保留临时旁路。

涉及文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/SearchIndexGateway.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/client/ElasticsearchSearchIndexGateway.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/client/ElasticsearchSearchIndexGatewayTest.java`

执行动作：

- 扩展正式签名，使 Gateway 能接收索引文档批量重建和写入。
- 删除无意义的旧签名，不保留长期占位方法。

验收点：

- Application 不需要私有写入通道。
- Gateway 重建能力有稳定正式签名。

### T4 扩展 SearchIndexGateway 正式能力

目标：让 Gateway 能承载真实检索和批量写入。

涉及文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/SearchIndexGateway.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/client/ElasticsearchSearchIndexGateway.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/client/DiscoverySearchIndexProperties.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/client/ElasticsearchSearchIndexGatewayTest.java`

执行动作：

- 扩展 Gateway，使其具备：
  - `search(...)`
  - `rebuildIndex(List<DiscoverySearchDocument>)`
  - `upsertDocuments(List<DiscoverySearchDocument>)`
- 保留现有运行时兼容性，不保留无用旧签名。
- `DiscoverySearchIndexProperties` 增加批量大小、索引名等运行时参数。

验收点：

- application 不直接依赖 ES。
- infra 的 ES 代码只留在 `infra.client`。

### T5a 实现关键词检索与结果映射

目标：先让 `ElasticsearchSearchIndexGateway.search()` 返回真实结果项。

涉及文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/client/ElasticsearchSearchIndexGateway.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/client/DiscoverySearchDocument.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/client/ElasticsearchSearchIndexGatewayTest.java`

执行动作：

- 使用 Spring Data Elasticsearch 或当前公共模块提供的能力实现搜索。
- 先支持关键词匹配和分页。
- 先把命中文档映射为 `SearchResult`，保证 `contentType / contentId / title / summary / targetPath` 正确。
- 本轮不实现高亮、同义词、实体增强和复杂相关性调优；只保证三类内容“找得到”。

验收点：

- 不再抛 `UnsupportedOperationException`。
- 返回结果必须带 `contentType / contentId / title / summary / targetPath`。

### T5b 增加过滤条件与分组出参

目标：在真实检索基础上补齐基础过滤和按 `contentType` 分组。

涉及文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/client/ElasticsearchSearchIndexGateway.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/result/SearchGroupResult.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/client/ElasticsearchSearchIndexGatewayTest.java`

执行动作：

- 补齐基础过滤：
  - `knowledgeBases`
  - `categoryCodes`
  - `contentStatuses`
  - `visibilityScopes`
- 按 `contentType` 组装 `SearchGroupResult`。
- 固定组标题为 `三才图会 / 王圻文档 / 明代习俗`。

验收点：

- Search 接口返回真实分组结果。
- 过滤条件能影响命中结果。

### T6 写入真实搜索日志

目标：把 Portal 搜索链路中的成功 / 失败请求真实落到 `discovery_search_log`。

涉及文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/entity/SearchLog.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/repository/impl/SearchLogRepositoryImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImplTest.java`

执行动作：

- 搜索成功时保存日志，再返回 `searchLogId`。
- 搜索失败时也保存失败日志，然后抛原业务异常。
- `searchScopesJson` 要记录实际生效范围，不允许继续返回 `null`。

验收点：

- `SearchApplicationServiceImpl.search()` 返回中的 `searchLogId` 来自真实落库对象。
- 成功 / 失败两条路径都有测试覆盖。

### T7 保持点击日志链路可用

目标：校准 Portal 点击接口与真实搜索日志 ID 的一致性。

涉及文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/assembler/DiscoverySearchPortalInterfaceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/controller/DiscoverySearchPortalControllerTest.java`

执行动作：

- 保持点击接口使用真实 `searchLogId`。
- 如有必要，补充点击前校验：`searchLogId` 不存在时返回明确业务异常。
- 不引入异步化，本轮继续同步写点击日志。

验收点：

- Search 和 Click 能通过 `searchLogId` 串联。

### T8 暴露索引重建管理入口

目标：给后端一个可触发全量重建的管理入口，方便联调。

涉及文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/DiscoverySearchAdminController.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/request/DiscoverySearchIndexRebuildRequest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/DiscoverySearchAdminControllerTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/SearchIndexApplicationService.java`

执行动作：

- 增加一个管理端手动重建入口，例如 `POST /api/discovery/search-admin/index/rebuild`。
- 当前请求体可为空或仅包含 `dryRun` 之类的稳定字段；不要设计一大堆未实现选项。
- 权限码固定为 `discovery:search:edit`。
- 该入口定位为运维入口，不承担分析看板或统计聚合职责。

验收点：

- 可以通过后端接口手动触发一次索引重建。

### T9 扩展 Admin 日志详情展示真实范围

目标：让 Admin `logs/get` 真正能看到本次搜索的范围和失败信息。

涉及文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/assembler/DiscoverySearchAdminInterfaceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/response/DiscoverySearchLogDetailResponse.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/DiscoverySearchAdminControllerTest.java`

执行动作：

- 保证 `searchScopesJson`、`failureCode`、`failureMessage`、`requestId`、`traceId` 都来自真实日志对象。
- 不在接口层拼新 JSON，不改写 repository 保存值。

验收点：

- 管理端详情响应与落库内容一致。

### T10a 验证 starter 装配

目标：保证 Discovery Search 运行时闭环不会因缺 bean 或扫描路径错误失败。

涉及文件：

- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/java/com/thundax/kuzhambu/starter/admin/KuzhambuAdminApplication.java`
- `kuzhambu-servers/starter/kuzhambu-portal-starter/src/main/java/com/thundax/kuzhambu/starter/portal/KuzhambuPortalApplication.java`
- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/test/java/com/thundax/kuzhambu/starter/admin/AdminStarterArchitectureTest.java`
- `kuzhambu-servers/starter/kuzhambu-portal-starter/src/test/java/com/thundax/kuzhambu/starter/portal/PortalStarterArchitectureTest.java`

执行动作：

- 确认 starter 扫描包与 mapper 包仍覆盖 Discovery Search 所需 bean。

验收点：

- 不出现 `SearchContentProvider`、`SearchIndexGateway`、`SearchIndexApplicationService` 缺 bean。

### T10b 收口控制器路径测试与最小运行时验证

目标：确认新增接口路径测试和最小 Maven 验证都覆盖到本轮运行时闭环。

涉及文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/DiscoverySearchAdminControllerTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/controller/DiscoverySearchPortalControllerTest.java`
- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/test/java/com/thundax/kuzhambu/starter/admin/AdminStarterArchitectureTest.java`
- `kuzhambu-servers/starter/kuzhambu-portal-starter/src/test/java/com/thundax/kuzhambu/starter/portal/PortalStarterArchitectureTest.java`

执行动作：

- 为新增索引重建入口补路径测试。
- 运行最小 Maven 验证。

验收点：

- 新增接口路径稳定。
- 本轮运行时闭环的最小测试链路可通过。

## Verification Plan

代码完成后按以下顺序验证：

1. `mvn -pl biz/discovery/kuzhambu-discovery-application,biz/discovery/kuzhambu-discovery-infra,biz/discovery/kuzhambu-discovery-interface spotless:apply`
2. `mvn -pl biz/discovery/kuzhambu-discovery-application,biz/discovery/kuzhambu-discovery-infra,biz/discovery/kuzhambu-discovery-interface spotless:check`
3. `mvn -pl biz/discovery/kuzhambu-discovery-interface -am test -Dtest=SearchApplicationServiceImplTest,SearchIndexApplicationServiceImplTest,DiscoverySearchPortalControllerTest,DiscoverySearchAdminControllerTest -Dsurefire.failIfNoSpecifiedTests=false`
4. `mvn -pl biz/discovery/kuzhambu-discovery-infra -am test -Dtest=DiscoverySearchDocumentAssemblerTest,ElasticsearchSearchIndexGatewayTest -Dsurefire.failIfNoSpecifiedTests=false`
5. `mvn test -Dtest=AdminStarterArchitectureTest -Dsurefire.failIfNoSpecifiedTests=false` in `starter/kuzhambu-admin-starter`
6. `mvn test -Dtest=PortalStarterArchitectureTest -Dsurefire.failIfNoSpecifiedTests=false` in `starter/kuzhambu-portal-starter`

## Closeout Rules

- 本轮完成后，`docs/40-readiness/DISCOVERY-IMPLEMENTATION-COVERAGE.md` 必须更新为“真实检索闭环已完成，查询理解仍未完成”。
- 本轮完成后，若无剩余执行价值，`RUNBOOK-DISCOVERY-SEARCH-RUNTIME.md` 应删除。
- 若过程中发现 Classics 当前 application 能力不足以支撑统一内容读取，不要绕过 application 直接读 mapper；应先补一层受控 application 能力，并在设计文档中登记。
