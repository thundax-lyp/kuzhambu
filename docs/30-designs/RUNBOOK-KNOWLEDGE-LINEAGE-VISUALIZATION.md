# Knowledge Lineage Visualization Runbook

## Purpose

本 RUNBOOK 用于把 Knowledge「世系图浏览」推进到独立可用闭环：正式世系事实可按版本读取，Admin 有独立画布和节点/关系浏览入口，Portal 有只读世系入口。任务完成后删除本 RUNBOOK。

## Agreed Decisions

- Admin 和 Portal 都做独立入口。
- Admin 权限沿用 `knowledge:graph:view`，不新增 `knowledge:lineage:view`。
- 新增只读画布聚合 API；既有世系节点/关系分页 API 继续保留。
- 数据只来自正式 `knowledge_lineage_node`、`knowledge_lineage_relation` 和 `knowledge_graph_version`。
- 不新增表，不修改抽取、候选应用、精修写入或质量报告生成规则。
- 不调用 AI，不写入 `knowledge_*` 表。
- 画布是首屏主体验，表格只是辅助检索。

## Target State

- Admin 新增 `/knowledge/lineage`，从侧栏菜单可达，页面首屏为世系画布。
- Portal 新增 `/knowledge/lineage`，从知识首页可达，页面只读展示最新已应用版本的世系画布。
- 点击画布节点后，同页详情面板展示节点字段、来源、相邻关系和确认状态。
- 点击画布关系后，同页详情面板展示关系字段、来源、两端节点和确认状态。
- 版本无数据、筛选无结果、无权限、接口失败都有明确状态。
- 现有 `/knowledge/graph-results` 继续保留正式结果表格读取能力。

## Current Grounding

- Admin 现有正式结果读取页：
  - `kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/graph-results-page.tsx`
  - `kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/graph-results-service.ts`
  - `kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/graph-results-types.ts`
- Admin 现有世系表格和详情组件：
  - `kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/components/graph-lineage-node-table.tsx`
  - `kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/components/graph-lineage-node-detail.tsx`
  - `kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/components/graph-lineage-relation-table.tsx`
  - `kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/components/graph-lineage-relation-detail.tsx`
- Admin 路由文件：
  - `kuzhambu-apps/admin-web/src/router/index.tsx`
- Portal 现有 Knowledge 页面和画布：
  - `kuzhambu-apps/portal-web/src/app.tsx`
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-home-page.tsx`
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-home-service.ts`
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-home-types.ts`
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.tsx`
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-graph-canvas.tsx`
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-graph-layout.ts`
- 后端现有正式世系分页与详情接口：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/controller/KnowledgeGraphExtractionController.java`
  - `POST /api/knowledge/graph-extraction/lineage/node/page`
  - `POST /api/knowledge/graph-extraction/lineage/node/get`
  - `POST /api/knowledge/graph-extraction/lineage/relation/page`
  - `POST /api/knowledge/graph-extraction/lineage/relation/get`
- 后端现有 Portal 读取入口：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/home/controller/KnowledgePortalHomeController.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/atlas/controller/KnowledgePortalAtlasController.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationService.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationServiceImpl.java`

## Data Structure Changes

不做数据库结构变更。新增的是 Java application/interface 传输结构和前端 TypeScript 类型。

### Java Query

新增文件 `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/lineage/query/LineageCanvasQuery.java`。

字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `versionId` | `Long` | 否 | Admin 可空时返回空态；Portal 可空时读取最新已应用版本。 |
| `focusNodeId` | `Long` | 否 | 初始选中的世系节点 ID。 |
| `focusRelationId` | `Long` | 否 | 初始选中的世系关系 ID。 |
| `keyword` | `String` | 否 | 匹配节点名称、节点 key、关系类型。 |
| `nodeType` | `String` | 否 | 过滤 `knowledge_lineage_node.node_type`。 |
| `relationType` | `String` | 否 | 过滤 `knowledge_lineage_relation.relation_type`。 |
| `confirmationStatus` | `String` | 否 | 同时过滤节点或关系确认状态。 |
| `depth` | `Integer` | 否 | 焦点节点相邻深度；默认 2，最大 4。 |

### Java Result

新增文件 `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/lineage/result/LineageCanvasResult.java`。

顶层字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `version` | `VersionView` | 当前图谱版本；无版本时为 `null`。 |
| `summary` | `SummaryView` | 画布统计。 |
| `nodes` | `List<NodeView>` | 画布节点。 |
| `relations` | `List<RelationView>` | 画布关系。 |
| `selectedNode` | `NodeView` | 当前选中节点；无选中时为 `null`。 |
| `selectedRelation` | `RelationView` | 当前选中关系；无选中时为 `null`。 |
| `availableFilters` | `AvailableFiltersView` | 可用筛选选项。 |
| `empty` | `EmptyView` | 空态说明；非空数据时为 `null`。 |

`VersionView` 字段：

| 字段 | 类型 |
| --- | --- |
| `versionId` | `Long` |
| `versionNo` | `Integer` |
| `taskType` | `String` |
| `status` | `String` |
| `sourceContentType` | `String` |
| `sourceContentId` | `Long` |
| `sourceCategoryCode` | `String` |
| `sourceCategoryName` | `String` |
| `appliedAt` | `Long` |

`SummaryView` 字段：

| 字段 | 类型 |
| --- | --- |
| `nodeCount` | `Long` |
| `relationCount` | `Long` |
| `confirmedNodeCount` | `Long` |
| `confirmedRelationCount` | `Long` |
| `focusNodeId` | `Long` |
| `focusRelationId` | `Long` |

`NodeView` 字段：

| 字段 | 类型 | 来源 |
| --- | --- | --- |
| `id` | `String` | 生成值：`lineage-node:{nodeId}` |
| `nodeId` | `Long` | `knowledge_lineage_node.node_id` |
| `nodeKey` | `String` | `knowledge_lineage_node.node_key` |
| `name` | `String` | `knowledge_lineage_node.name` |
| `nodeType` | `String` | `knowledge_lineage_node.node_type` |
| `generation` | `Integer` | `knowledge_lineage_node.generation` |
| `gender` | `String` | `knowledge_lineage_node.gender` |
| `confirmationStatus` | `String` | `knowledge_lineage_node.confirmation_status` |
| `confidence` | `Double` | 当前表若无置信字段则返回 `null`。 |
| `sourceRefsJson` | `String` | `knowledge_lineage_node.source_refs_json` |
| `sourceRefs` | `List<SourceRefView>` | 由 `sourceRefsJson` 解析。 |
| `firstExtractedAt` | `Long` | `knowledge_lineage_node.first_extracted_at` |
| `lastExtractedAt` | `Long` | `knowledge_lineage_node.last_extracted_at` |
| `x` | `Double` | 后端不持久化时返回 `null`，前端布局计算。 |
| `y` | `Double` | 后端不持久化时返回 `null`，前端布局计算。 |

`RelationView` 字段：

| 字段 | 类型 | 来源 |
| --- | --- | --- |
| `id` | `String` | 生成值：`lineage-relation:{relationId}` |
| `relationId` | `Long` | `knowledge_lineage_relation.relation_id` |
| `sourceNodeId` | `Long` | `knowledge_lineage_relation.source_node_id` |
| `sourceNodeName` | `String` | 从节点集合按 `sourceNodeId` 反查。 |
| `targetNodeId` | `Long` | `knowledge_lineage_relation.target_node_id` |
| `targetNodeName` | `String` | 从节点集合按 `targetNodeId` 反查。 |
| `relationType` | `String` | `knowledge_lineage_relation.relation_type` |
| `relationLabel` | `String` | 优先使用关系类型中文映射；无映射返回 `relationType`。 |
| `confirmationStatus` | `String` | `knowledge_lineage_relation.confirmation_status` |
| `confidence` | `Double` | 当前表若无置信字段则返回 `null`。 |
| `sourceRefsJson` | `String` | `knowledge_lineage_relation.source_refs_json` |
| `sourceRefs` | `List<SourceRefView>` | 由 `sourceRefsJson` 解析。 |
| `firstExtractedAt` | `Long` | `knowledge_lineage_relation.first_extracted_at` |
| `lastExtractedAt` | `Long` | `knowledge_lineage_relation.last_extracted_at` |

`SourceRefView` 字段：

| 字段 | 类型 |
| --- | --- |
| `sourceContentType` | `String` |
| `sourceContentId` | `Long` |
| `sourceTitle` | `String` |
| `snippet` | `String` |
| `href` | `String` |

`AvailableFiltersView` 字段：

| 字段 | 类型 |
| --- | --- |
| `versions` | `List<VersionOptionView>` |
| `nodeTypes` | `List<String>` |
| `relationTypes` | `List<String>` |
| `confirmationStatuses` | `List<String>` |

`EmptyView` 字段：

| 字段 | 类型 | 取值 |
| --- | --- | --- |
| `reason` | `String` | `NO_VERSION`、`NO_LINEAGE_DATA`、`FILTER_NO_RESULT`、`NO_PERMISSION`、`ERROR` |
| `title` | `String` | 页面可直接展示的短标题。 |
| `description` | `String` | 页面可直接展示的说明。 |
| `actionLabel` | `String` | 可选操作文案。 |
| `actionHref` | `String` | 可选操作链接。 |

## API Contract

Admin 新增：

- `POST /api/knowledge/lineage/canvas`
- 权限：`@HasPermission("knowledge:graph:view")`
- Request body：`LineageCanvasRequest`
- Response：`LineageCanvasResponse`

Portal 新增：

- `GET /api/portal/knowledge/lineage`
- Query params：`versionId`、`focusNodeId`、`focusRelationId`、`keyword`、`nodeType`、`relationType`、`confirmationStatus`、`depth`
- Response：`KnowledgePortalLineageResponse`

Admin 与 Portal response 字段保持同构；Portal 不返回后台操作权限字段。

## Frontend TypeScript Types

Admin 新增 `kuzhambu-apps/admin-web/src/pages/knowledge/lineage/lineage-types.ts`，Portal 新增 `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-lineage-types.ts`。两个文件字段保持同构，命名前缀分别使用 `Lineage*` 和 `KnowledgeLineage*`。

`LineageCanvasQuery` 字段：

| 字段 | 类型 |
| --- | --- |
| `versionId` | `number \| null` |
| `focusNodeId` | `number \| null` |
| `focusRelationId` | `number \| null` |
| `keyword` | `string \| null` |
| `nodeType` | `string \| null` |
| `relationType` | `string \| null` |
| `confirmationStatus` | `string \| null` |
| `depth` | `number` |

`LineageCanvasRecord` 字段：

| 字段 | 类型 |
| --- | --- |
| `version` | `LineageVersionRecord \| null` |
| `summary` | `LineageSummaryRecord` |
| `nodes` | `LineageNodeRecord[]` |
| `relations` | `LineageRelationRecord[]` |
| `selectedNode` | `LineageNodeRecord \| null` |
| `selectedRelation` | `LineageRelationRecord \| null` |
| `availableFilters` | `LineageAvailableFiltersRecord` |
| `empty` | `LineageEmptyRecord \| null` |

`LineageNodeRecord` 字段：

| 字段 | 类型 |
| --- | --- |
| `id` | `string` |
| `nodeId` | `number` |
| `nodeKey` | `string` |
| `name` | `string` |
| `nodeType` | `string` |
| `generation` | `number \| null` |
| `gender` | `string \| null` |
| `confirmationStatus` | `string` |
| `confidence` | `number \| null` |
| `sourceRefsJson` | `string \| null` |
| `sourceRefs` | `LineageSourceRefRecord[]` |
| `firstExtractedAt` | `number \| null` |
| `lastExtractedAt` | `number \| null` |
| `x` | `number \| null` |
| `y` | `number \| null` |

`LineageRelationRecord` 字段：

| 字段 | 类型 |
| --- | --- |
| `id` | `string` |
| `relationId` | `number` |
| `sourceNodeId` | `number` |
| `sourceNodeName` | `string` |
| `targetNodeId` | `number` |
| `targetNodeName` | `string` |
| `relationType` | `string` |
| `relationLabel` | `string` |
| `confirmationStatus` | `string` |
| `confidence` | `number \| null` |
| `sourceRefsJson` | `string \| null` |
| `sourceRefs` | `LineageSourceRefRecord[]` |
| `firstExtractedAt` | `number \| null` |
| `lastExtractedAt` | `number \| null` |

## Task Breakdown

每个任务的核心改动控制在 2-5 个文件。测试文件与验证文件列在同任务下，允许随任务一起改。

### Task 1: 后端画布读取用例

核心文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/lineage/query/LineageCanvasQuery.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/lineage/result/LineageCanvasResult.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/lineage/service/KnowledgeLineageReadApplicationService.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/lineage/service/impl/KnowledgeLineageReadApplicationServiceImpl.java`

验证文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/lineage/KnowledgeLineageReadApplicationServiceImplTest.java`

要求：

- `versionId` 为空时，Admin 调用返回 `NO_VERSION` 空态；Portal 调用由 Portal controller 或 application helper 填入最新已应用版本。
- `depth` 小于 1 时按 1 处理，大于 4 时按 4 处理。
- 查询节点后只返回两端节点都在结果集内的关系。
- `focusNodeId` 命中时，`selectedNode` 返回该节点，相关边高亮逻辑交给前端。
- `focusRelationId` 命中时，`selectedRelation` 返回该关系。

### Task 2: 后端 Admin 接口

核心文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/lineage/controller/KnowledgeLineageController.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/lineage/controller/request/LineageCanvasRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/lineage/controller/response/LineageCanvasResponse.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/lineage/assembler/KnowledgeLineageInterfaceAssembler.java`

验证文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/admin/lineage/controller/KnowledgeLineageControllerTest.java`

要求：

- Admin controller 使用 `@RequestMapping("/api/knowledge/lineage")` 和 `@PostMapping("canvas")`。
- Controller 不拼装业务数据，只做 request/result/response 转换。
- `LineageCanvasRequest` 字段必须与 `LineageCanvasQuery` 一一对应。
- `LineageCanvasResponse` 字段必须与 `LineageCanvasResult` 一一对应。

### Task 3: 后端 Portal 接口

核心文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/lineage/controller/KnowledgePortalLineageController.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/lineage/controller/response/KnowledgePortalLineageResponse.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/lineage/assembler/KnowledgePortalLineageInterfaceAssembler.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationService.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationServiceImpl.java`

验证文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/portal/lineage/controller/KnowledgePortalLineageControllerTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationServiceImplTest.java`

要求：

- Portal controller 使用 `@RequestMapping("/api/portal/knowledge/lineage")` 和 `@GetMapping`。
- Query 参数名称与 `LineageCanvasQuery` 字段一致。
- `versionId` 为空时读取最新已应用版本。
- `KnowledgePortalLineageResponse` 字段与 `LineageCanvasResponse` 同构，但不包含后台权限或后台操作字段。

### Task 4: Admin 菜单与路由入口

核心文件：

- `kuzhambu-apps/admin-web/src/router/index.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/lineage/lineage-page.tsx`
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/MenuController.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/request/MenuSaveRequest.java`
- `kuzhambu-apps/admin-web/src/pages/system/menu/menu-service.ts`

验证文件：

- `kuzhambu-apps/admin-web/src/pages/knowledge/lineage/lineage-page.test.tsx`
- `kuzhambu-apps/admin-web/src/app.test.tsx`

菜单记录要求：

| 字段 | 目标值 |
| --- | --- |
| `name` | `世系图浏览` |
| `url` | `/knowledge/lineage` |
| `permission` | `knowledge:graph:view` |
| `icon` | 复用 Knowledge/graph 现有图标；若无现有图标，使用 `BookOutlined` 对应配置。 |
| `parent` | Knowledge 菜单节点 |
| `sort` | 放在「正式结果读取」附近，优先紧邻其后。 |

要求：

- 当前仓库未发现菜单 seed 文件；不要为本任务新造一套 seed 体系。
- 菜单数据通过既有 System 菜单接口或部署环境初始化数据补齐，并在 PR 描述列出菜单记录。
- 路由 path 为 `knowledge/lineage`。
- 页面无权限时展示「当前账号暂无知识图谱查看权限。」并不请求 `/knowledge/lineage/canvas`。
- 页面标题为「世系图浏览」，描述说明这是正式世系结果的独立画布。

### Task 5: Admin 画布页面

核心文件：

- `kuzhambu-apps/admin-web/src/pages/knowledge/lineage/lineage-types.ts`
- `kuzhambu-apps/admin-web/src/pages/knowledge/lineage/lineage-service.ts`
- `kuzhambu-apps/admin-web/src/pages/knowledge/lineage/components/lineage-filter-bar.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/lineage/components/lineage-canvas.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/lineage/components/lineage-detail-panel.tsx`

验证文件：

- `kuzhambu-apps/admin-web/src/pages/knowledge/lineage/lineage-service.test.ts`
- `kuzhambu-apps/admin-web/src/pages/knowledge/lineage/components/lineage-canvas.test.tsx`

控件要求：

- `Select`「图谱版本」：显示 `版本 {versionNo} / {sourceCategoryName}`，切换后刷新画布。
- `Input.Search`「搜索世系节点或关系」：回车或点击搜索后提交 `keyword`。
- `Select`「节点类型」：选项来自 `availableFilters.nodeTypes`。
- `Select`「关系类型」：选项来自 `availableFilters.relationTypes`。
- `Select`「确认状态」：选项来自 `availableFilters.confirmationStatuses`。
- `InputNumber` 或 `Select`「深度」：允许 1、2、3、4，默认 2。
- `Button`「重置」：清空 keyword、nodeType、relationType、confirmationStatus、focusNodeId、focusRelationId，保留当前版本。
- `Button`「刷新」：重新请求当前查询。
- `Tabs`：`节点列表`、`关系列表`。

操作要求：

- 点击画布节点：设置 `focusNodeId`，清空 `focusRelationId`，详情面板切到节点详情。
- 点击画布关系：设置 `focusRelationId`，清空 `focusNodeId`，详情面板切到关系详情。
- 点击节点列表行：联动画布选中节点。
- 点击关系列表行：联动画布选中关系。
- 点击 `fit view` 控件：画布回到全局视图。
- 画布允许缩放和平移，不允许连接节点，不允许拖动节点写回。
- 节点卡片显示：名称、节点类型、代际、确认状态。
- 关系边标签显示：`relationLabel`。
- 相邻高亮：选中节点时高亮连接该节点的关系和相邻节点；其他节点降噪。

### Task 6: Portal 世系页面

核心文件：

- `kuzhambu-apps/portal-web/src/app.tsx`
- `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-lineage-types.ts`
- `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-lineage-service.ts`
- `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-lineage-page.tsx`
- `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-lineage-page.css`

验证文件：

- `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-lineage-page.test.tsx`
- `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-lineage-service.test.ts`

控件要求：

- 顶部返回链接：`返回知识馆`，指向 `/knowledge`。
- 版本选择：只展示 `availableFilters.versions`，默认最新已应用版本。
- 搜索框：placeholder 为「搜索人物、谱系节点或关系」。
- 节点类型筛选：选项来自后端。
- 关系类型筛选：选项来自后端。
- `Button`「清除筛选」：清空筛选并保留版本。
- 画布控件：缩放、平移、mini map、fit view。
- 详情区域：只读显示节点或关系详情，不出现编辑、确认、删除、应用候选按钮。

操作要求：

- 访问 `/knowledge/lineage`：请求 `/portal/knowledge/lineage`。
- URL 带 `versionId`：请求对应版本。
- URL 带 `focusNodeId` 或 `focusRelationId`：初始选中对应对象。
- 点击节点或关系：更新页面内选中态；可以同步更新 URL query，但不得触发写接口。
- 空态操作：`返回知识馆` 指向 `/knowledge`，`查看图谱浏览` 指向 `/knowledge/atlas`。

### Task 7: Portal 首页入口与文案

核心文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationServiceImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/portal/home/controller/KnowledgePortalHomeControllerTest.java`
- `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-home-page.tsx`
- `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-home-page.test.tsx`

要求：

- Home quick link 新增或替换现有 `lineage` 链接，目标为 `/knowledge/lineage`。
- 文案使用「世系图浏览」或「来源世系」之一，避免继续指向 `/knowledge/atlas`。
- 首页卡片点击后进入独立世系页面，不进入 atlas。

## Frontend Layout Details

### Admin Layout

页面结构固定为：

```text
KuzhambuPage
  PageHeader: title / description
  FilterBar: version / keyword / nodeType / relationType / confirmationStatus / depth / reset / refresh
  MainSplit
    Left: LineageCanvas
    Right: LineageDetailPanel
  Tabs
    NodeTable
    RelationTable
```

详情面板字段：

- 节点详情：名称、节点 key、节点类型、代际、性别、确认状态、首次抽取时间、最近抽取时间、来源摘录。
- 关系详情：关系类型、关系标签、源节点、目标节点、确认状态、首次抽取时间、最近抽取时间、来源摘录。

### Portal Layout

页面结构固定为：

```text
Page
  TopNav: 返回知识馆 / 图谱浏览 / 质量总览
  Header: 世系图浏览 / 当前版本摘要
  FilterBar: version / keyword / nodeType / relationType / clear
  CanvasBand: LineageCanvas
  DetailBand: selected node or selected relation
```

Portal 视觉要求：

- 保持学术极简和内容优先，不做营销 hero。
- 画布占首屏主体，不放入装饰卡片。
- 小屏时筛选折为单列，详情区在画布下方。

## Acceptance

- Admin 用户拥有 `knowledge:graph:view` 时，可以从侧栏进入「世系图浏览」。
- Admin 选择有世系数据的图谱版本后，画布展示节点和关系。
- Admin 点击节点后，节点高亮，详情面板展示节点详情，相邻关系高亮。
- Admin 点击关系后，关系高亮，详情面板展示关系详情，两端节点高亮。
- Admin 节点列表和关系列表能联动画布选中态。
- Admin 无权限时不发起业务查询，并展示无权限空态。
- Portal 用户可以访问 `/knowledge/lineage`，默认展示最新已应用版本的只读世系图。
- Portal 首页存在可见的「世系图浏览」入口，点击进入 `/knowledge/lineage`。
- Portal 无已应用版本或无世系数据时展示明确空态，并提供返回知识首页或图谱浏览入口。
- 所有新增 API 不调用 AI，不写入 `knowledge_*` 表。
- 现有 `/knowledge/graph-results` 仍可读取正式实体、关系和世系表格。

## Validation

Java servers:

```sh
cd kuzhambu-servers
mvn -pl biz/knowledge/kuzhambu-knowledge-interface,biz/knowledge/kuzhambu-knowledge-application,biz/knowledge/kuzhambu-knowledge-infra spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/knowledge/kuzhambu-knowledge-interface,biz/knowledge/kuzhambu-knowledge-application,biz/knowledge/kuzhambu-knowledge-infra -am test
```

Admin web:

```sh
cd kuzhambu-apps
npm --workspace kuzhambu-admin-web run format
npm run format:check
npm run lint
npm --workspace kuzhambu-admin-web run test
```

Portal web:

```sh
cd kuzhambu-apps
npm --workspace kuzhambu-portal-web run format
npm run format:check
npm run lint
npm --workspace kuzhambu-portal-web run test
```

Manual smoke:

- Admin 登录后进入 `/knowledge/lineage`，验证菜单、权限、版本切换、筛选、重置、刷新、画布渲染、节点详情、关系详情、列表联动、空态。
- Portal 进入 `/knowledge/lineage`，验证默认版本、URL query、筛选、清除筛选、画布渲染、只读详情、空态。
- 对比 `/knowledge/graph-results`，确认原正式结果读取页仍可正常查看世系节点和世系关系表格。

## Cleanup

- 任务完成并合入前删除本 RUNBOOK。
- 如果实现过程中需要把世系画布规则沉淀为稳定设计，只更新 `docs/30-designs/KNOWLEDGE-DESIGN.md`，不要把临时执行过程写入治理文档。
