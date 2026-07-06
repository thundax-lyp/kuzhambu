# Discovery 筛选和权限过滤闭环 RUNBOOK

## 目标

将 Discovery Search 的高级筛选和权限过滤推进到已完成。

目标态：

- 搜索请求支持按知识库、门类、标签、状态、可见性和更新时间范围筛选。
- Elasticsearch 查询消费全部筛选字段。
- Search application 在返回结果前执行权限裁剪。
- 匿名用户只能看到公开内容。
- 授权用户可按已有 Classics 权限查看对应非公开内容。
- 前端控件能完整表达筛选条件，并能清除、提交和恢复搜索状态。
- readiness 矩阵将“按知识库、门类、标签、状态、时间筛选”和“权限过滤”标为 `已完成`。

## 已确认决策

- 不新增 Discovery 专属内容权限码，复用已有 Classics 权限。
- 非公开内容放行权限固定为 `super`、`classics:content:view` 或内容类型对应权限。
- 未知 `contentType` 的非公开结果默认拒绝返回。
- `visibility` 为空的历史索引文档按公开内容处理。
- Portal 搜索响应继续隐藏权限裁剪内部元数据，保持响应契约稳定。
- 本轮需要把前端高级筛选控件补到可操作状态。

## 数据结构变更

### 请求字段

文件：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/controller/request/DiscoverySearchRequest.java`

字段：

- `queryText: String`：搜索词，必填。
- `knowledgeBases: List<String>`：知识库筛选，取值对应索引字段 `knowledgeBase`。
- `categoryCodes: List<String>`：门类筛选，取值对应索引字段 `categoryCode`。
- `tagNames: List<String>`：标签筛选，取值对应索引字段 `tagNames`。
- `contentStatuses: List<String>`：内容状态筛选，取值对应索引字段 `status`。
- `visibilityScopes: List<String>`：可见性筛选，取值对应索引字段 `visibility`，只能作为筛选条件，不能作为权限依据。
- `dateFrom: String`：ISO-8601 起始时间，对应 `updatedAt >= dateFrom`。
- `dateTo: String`：ISO-8601 结束时间，对应 `updatedAt <= dateTo`。
- `pageNo: Integer`：页码。
- `pageSize: Integer`：每页条数。

### Application 查询模型

文件：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/query/SearchQuery.java`

字段：

- `queryText: String`
- `knowledgeBases: List<String>`
- `categoryCodes: List<String>`
- `tagNames: List<String>`
- `contentStatuses: List<String>`
- `visibilityScopes: List<String>`
- `dateFrom: Date`
- `dateTo: Date`
- `pageNo: int`
- `pageSize: int`
- `operatorType: String`
- `operatorId: String`
- `requestId: String`
- `traceId: String`

### 检索范围值对象

文件：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/valueobject/SearchScope.java`

字段：

- `knowledgeBases: List<String>`
- `categoryCodes: List<String>`
- `tagNames: List<String>`
- `contentStatuses: List<String>`
- `visibilityScopes: List<String>`
- `dateFrom: Date`
- `dateTo: Date`

写入规则：

- `SearchApplicationServiceImpl` 必须用上述字段构造 `SearchScope`。
- 搜索成功或失败时，`SearchScope` 必须序列化进入 `discovery_search_log.search_scopes_json`。

### Elasticsearch 索引文档

文件：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/client/DiscoverySearchDocument.java`

筛选字段：

- `knowledgeBase: String`
- `categoryCode: String`
- `tagNames: List<String>`
- `status: String`
- `visibility: String`
- `updatedAt: Instant`
- `deleted: Boolean`

查询规则：

- `knowledgeBases` 对应 `knowledgeBase in (...)`。
- `categoryCodes` 对应 `categoryCode in (...)`。
- `tagNames` 对应 `tagNames in (...)`。
- `contentStatuses` 对应 `status in (...)`。
- `visibilityScopes` 对应 `visibility in (...)`。
- `dateFrom` 对应 `updatedAt >= dateFrom`。
- `dateTo` 对应 `updatedAt <= dateTo`。
- 所有搜索必须追加 `deleted = false`。

### SearchResult 内部裁剪元数据

文件：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/result/SearchResult.java`

新增字段：

- `knowledgeBase: String`
- `categoryCode: String`
- `tagNames: List<String>`
- `contentStatus: String`
- `visibility: String`
- `updatedAt: Long`

约束：

- 这些字段只服务 application 权限裁剪和测试断言。
- Portal response 不输出这些字段。
- 旧构造方法必须保留，避免既有测试和调用点被迫改动。

## 后端任务拆分

### 任务 1：补齐结果元数据传递

范围文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/result/SearchResult.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/client/ElasticsearchSearchIndexGateway.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/client/ElasticsearchSearchIndexGatewayTest.java`

处理动作：

- 给 `SearchResult` 增加内部裁剪元数据字段。
- 在 `ElasticsearchSearchIndexGateway.toGroupedResults` 中把 `DiscoverySearchDocument` 的筛选字段带入 `SearchResult`。
- 测试断言 ES 命中结果带出 `visibility`、`status`、`knowledgeBase`、`categoryCode`、`tagNames` 和 `updatedAt`。

验收点：

- 检索命中结果具备权限过滤所需元数据。
- Portal response 不新增字段。

### 任务 2：实现权限过滤

范围文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/DefaultSearchPermissionFilter.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImplTest.java`

处理动作：

- `DefaultSearchPermissionFilter` 从 `KuzhambuContextHolder.currentAuthorities()` 读取当前主体权限。
- `PUBLIC` 或空 `visibility` 直接放行。
- 非公开内容按 `contentType` 映射权限：
  - `SANCAI_ENTRY -> classics:sancai:view`
  - `WANGQI_DOCUMENT -> classics:wangqi:view`
  - `MING_CUSTOMS -> classics:mingcustoms:view`
- `super` 和 `classics:content:view` 放行全部三类 Classics 非公开结果。
- 未知 `contentType` 的非公开结果拒绝。
- 过滤后重建 `SearchGroupResult`，`count` 使用过滤后的 item 数量。

验收点：

- 匿名搜索请求包含 `PRIVATE` 时也不能返回非公开结果。
- 授权主体可返回对应内容类型的非公开结果。
- 搜索日志中的 `resultTotalCount` 和 `groupTotalCount` 使用权限裁剪后的结果。

### 任务 3：锁定筛选条件和日志范围

范围文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/client/ElasticsearchSearchIndexGateway.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/client/ElasticsearchSearchIndexGatewayTest.java`

处理动作：

- 确认 `SearchApplicationServiceImpl.toSearchScope` 覆盖全部筛选字段。
- 确认 `ElasticsearchSearchIndexGateway.buildCriteria` 覆盖全部筛选字段和 `deleted=false`。
- 测试覆盖筛选字段进入 `SearchScope` 与搜索日志 JSON。

验收点：

- 高级筛选字段不丢失。
- 搜索日志可追溯本次搜索使用的完整筛选范围。

### 任务 4：更新文档和 coverage

范围文件：

- `docs/30-designs/RUNBOOK-DISCOVERY-FILTER-PERMISSION-CLOSURE.md`
- `docs/40-readiness/DISCOVERY-IMPLEMENTATION-COVERAGE.md`

处理动作：

- RUNBOOK 保留目标、决策、字段、文件拆分、前端控件、验收命令。
- coverage 将对应两项标为 `已完成`，未完成部分写 `无`。

验收点：

- 文档不记录中间状态。
- 文档能直接指导审核与后续 PR 描述。

## 前端任务拆分

### 任务 1：Portal 搜索高级筛选控件

范围文件：

- `kuzhambu-apps/portal-web/src/pages/discovery/search/search-page.tsx`
- `kuzhambu-apps/portal-web/src/pages/discovery/search/search-page.test.tsx`
- `kuzhambu-apps/portal-web/src/services/discovery-search.ts`

控件要求：

- 搜索框：输入 `queryText`，按回车或点击搜索按钮提交。
- 知识库多选控件：选择 `SANCAI_ENTRY`、`WANGQI_DOCUMENT`、`MING_CUSTOMS`，写入 `knowledgeBases`。
- 门类多选控件：选择门类 code，写入 `categoryCodes`。
- 标签多选控件：输入或选择标签名，写入 `tagNames`。
- 状态多选控件：选择内容状态，写入 `contentStatuses`。
- 可见性多选控件：选择 `PUBLIC`、`PRIVATE`，写入 `visibilityScopes`。控件文案必须避免暗示这是权限开关。
- 时间范围控件：选择起止时间，分别写入 `dateFrom` 和 `dateTo` 的 ISO-8601 字符串。
- 搜索按钮：提交当前搜索词和所有筛选条件。
- 清除筛选按钮：清空 `knowledgeBases`、`categoryCodes`、`tagNames`、`contentStatuses`、`visibilityScopes`、`dateFrom`、`dateTo`，保留或清空 `queryText` 需在页面交互中明确。
- 空状态中的清除筛选操作：与清除筛选按钮行为一致。

操作要求：

- 修改任一筛选控件后不自动请求，点击搜索按钮或按回车才请求。
- 搜索提交后 URL 状态必须能恢复 `queryText` 和筛选条件。
- 返回结果页时恢复控件选中状态。
- 权限过滤由后端负责，前端不得根据权限自行补非公开结果或隐藏后端已返回结果。

验收点：

- 用户能组合知识库、门类、标签、状态、可见性和时间范围发起搜索。
- 清除筛选后结果和 URL 状态同步更新。
- 无结果时能通过空状态操作清空筛选。

### 任务 2：Portal 搜索请求和响应契约

范围文件：

- `kuzhambu-apps/portal-web/src/services/discovery-search.ts`
- `kuzhambu-apps/portal-web/src/pages/discovery/search/search-page.test.tsx`

处理动作：

- 请求 payload 精确包含：
  - `queryText`
  - `knowledgeBases`
  - `categoryCodes`
  - `tagNames`
  - `contentStatuses`
  - `visibilityScopes`
  - `dateFrom`
  - `dateTo`
  - `pageNo`
  - `pageSize`
- 响应继续只消费：
  - `searchLogId`
  - `queryText`
  - `displayQueryText`
  - `totalCount`
  - `groupCount`
  - `groups[].groupKey`
  - `groups[].groupTitle`
  - `groups[].count`
  - `groups[].items[].contentDomain`
  - `groups[].items[].contentType`
  - `groups[].items[].contentId`
  - `groups[].items[].title`
  - `groups[].items[].summary`
  - `groups[].items[].highlightText`
  - `groups[].items[].resultRank`
  - `groups[].items[].groupRank`
  - `groups[].items[].targetPath`

验收点：

- 前端不依赖后端内部裁剪元数据。
- 请求字段和后端 `DiscoverySearchRequest` 一致。

### 任务 3：Admin Web 不纳入本轮

范围文件：

- 无。

说明：

- Admin Web 当前 Discovery 搜索页面职责是搜索日志、搜索分析和索引运维。
- 本轮不新增 Admin Web 高级筛选 UI。
- 后续如要做 Admin 侧搜索体验，应另开任务，控件颗粒度至少与 Portal 搜索页一致。

## 验收命令

Java 后端：

```sh
cd kuzhambu-servers
mvn -pl biz/discovery/kuzhambu-discovery-application,biz/discovery/kuzhambu-discovery-infra -am spotless:apply
mvn -pl biz/discovery/kuzhambu-discovery-application,biz/discovery/kuzhambu-discovery-infra -am spotless:check
mvn -pl biz/discovery/kuzhambu-discovery-application,biz/discovery/kuzhambu-discovery-infra -am test
```

Portal 前端：

```sh
cd kuzhambu-apps
npm --workspace portal-web run format
npm run format:check
npm run lint
npm --workspace portal-web run test
```

人工冒烟：

- 匿名用户搜索公开内容：返回 `PUBLIC` 结果。
- 匿名用户搜索并选择 `PRIVATE` 可见性：不返回非公开结果。
- 带 `classics:sancai:view` 的用户搜索三才非公开内容：返回 `SANCAI_ENTRY` 非公开结果。
- 清除筛选按钮：清空全部高级筛选控件并重新请求。
- 空状态清除筛选操作：清空全部高级筛选控件并重新请求。
- 搜索后刷新页面：搜索框和所有筛选控件恢复到 URL 状态。
