# Knowledge Graph 14 Closure Runbook

## Purpose

本 RUNBOOK 用于把 Knowledge 剩余的 `图谱可视化画布` 和 `固定 14 门类空位展示` 收口到已完成状态。

完成态：

- Portal `/knowledge/atlas` 鸟瞰层固定展示三才图会 14 个正式门类，空门类也占位可见。
- Portal `/knowledge/atlas` 的 overview、category、detail 三层均有可交互只读图谱画布。
- 图谱画布使用 `@xyflow/react` 渲染节点和边，使用 `@dagrejs/dagre` 计算 category/detail 层布局。
- overview 层使用固定径向布局，中心节点为三才图会，外圈 14 门类节点完整可见。
- 画布只消费 Knowledge 已应用的正式事实，不触发 AI 抽取、重生成或 candidate apply。

## Scope

本轮覆盖：

- Knowledge Portal atlas 只读读模型。
- Portal Web atlas 数据类型、fallback、图谱控件、页面接入和测试。
- `@xyflow/react`、`@dagrejs/dagre` 前端依赖引入。
- Knowledge coverage 文档收口。

本轮不覆盖：

- AI 重新抽取。
- 从质量报告低质量门类一键触发重提取。
- Admin Web 图谱抽取页、质量报告页和精修工作台改造。
- Discovery 搜索或问答增强。
- 王圻文档、明代习俗或非三才图会图谱。

## Visual Baseline

设计和实现前必须阅读效果图：

- `docs/60-human/classics-ui-concepts/classics-knowledge-graph-rag.png`

视觉约束：

- 页面是知识图谱工作台，不是普通卡片列表。
- 顶部必须保留知识图谱标题、搜索框和层级切换语义。
- 主体按效果图的信息层级组织：左侧鸟瞰摘要，中间关系画布，右侧实体详情。
- 鸟瞰层必须呈现中心节点加 14 门类环形节点。
- 门类层必须呈现中心门类节点、周边实体/关系节点和连线。
- 详情层必须呈现焦点实体节点、一跳关系节点，并与右侧实体详情面板一致。
- 视觉风格保持浅底、水墨边界、深蓝焦点节点、青绿色普通节点、细线关系。

## Dependency Contract

修改文件：

- `kuzhambu-apps/portal-web/package.json`

新增 dependencies：

- `@xyflow/react`
- `@dagrejs/dagre`

暂不新增：

- `@types/d3-hierarchy`

安装与版本固定规则：

- 第一步先用带 `^` 的 semver 范围安装：
  - `npm --workspace portal-web install @xyflow/react@^12.11.1 @dagrejs/dagre@^3.0.0`
- 第二步检查安装后生成或更新的 lockfile。
- 第三步从 lockfile 中读取实际解析版本，并把 `kuzhambu-apps/portal-web/package.json` dependencies 改成精确版本号，不保留 `^`、`~` 或 `latest`。
- 固定后的 `package.json` 示例形态：
  - `"@xyflow/react": "x.y.z"`
  - `"@dagrejs/dagre": "x.y.z"`
- 如果安装命令生成新的 lockfile，lockfile 必须随依赖变更一起保留并进入审核。
- 如果安装命令只更新已有 lockfile，也必须保留更新后的 lockfile。
- 不得手写猜测版本号；版本号必须来自实际 lockfile 解析结果。

其他说明：

- 本轮 overview 使用固定 14 门类径向坐标，不使用 `d3-hierarchy`。
- 若后续改为 `d3-hierarchy` 自动径向布局，再同时引入 `d3-hierarchy` 和 `@types/d3-hierarchy`。
- 当前仓库未发现 `package-lock.json`、`pnpm-lock.yaml` 或 `yarn.lock`；执行安装后如果 npm 生成 `package-lock.json`，按上面的 lockfile 规则处理。

## API Data Contract

### Existing Fields Kept

`GET /api/portal/knowledge/atlas` 保持现有字段兼容：

- `currentLevel`
- `breadcrumbItems`
- `overviewView`
- `categoryView`
- `detailView`
- `availableFilters`

### New Field: canvasView

在 `KnowledgePortalAtlasResult` 和 `KnowledgePortalAtlasResponse` 新增字段：

```json
{
  "canvasView": {
    "mode": "overview",
    "title": "十四门类知识鸟瞰",
    "description": "固定展示三才图会 14 个正式门类。",
    "focusNodeId": "root:sancai",
    "empty": false,
    "emptyTitle": null,
    "emptyDescription": null,
    "nodes": [],
    "edges": []
  }
}
```

字段定义：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `canvasView.mode` | `String` | 是 | 固定为 `overview`、`category` 或 `detail` |
| `canvasView.title` | `String` | 是 | 当前画布标题 |
| `canvasView.description` | `String` | 是 | 当前画布说明 |
| `canvasView.focusNodeId` | `String` | 否 | 当前中心或焦点节点 ID |
| `canvasView.empty` | `Boolean` | 是 | 当前画布是否为空态 |
| `canvasView.emptyTitle` | `String` | 否 | 空态标题 |
| `canvasView.emptyDescription` | `String` | 否 | 空态说明 |
| `canvasView.nodes` | `List<CanvasNode>` | 是 | 画布节点 |
| `canvasView.edges` | `List<CanvasEdge>` | 是 | 画布边 |

`CanvasNode` 字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | `String` | 是 | 画布内稳定节点 ID，例如 `root:sancai`、`category:BIRDS`、`entity:3001` |
| `kind` | `String` | 是 | `root`、`category`、`entity`、`relationGroup`、`source` |
| `label` | `String` | 是 | 主显示文本 |
| `subtitle` | `String` | 否 | 副文本，例如实体类型或版本状态 |
| `metricLabel` | `String` | 否 | 指标名，例如 `实体`、`关系` |
| `metricValue` | `Long` | 否 | 指标值 |
| `status` | `String` | 否 | `ACTIVE`、`EMPTY`、`FOCUS`、`CONFIRMED`、`DRAFT` |
| `categoryCode` | `String` | 否 | 门类 code |
| `entityId` | `Long` | 否 | 实体 ID |
| `href` | `String` | 否 | 节点点击跳转地址 |
| `weight` | `Double` | 否 | 节点视觉权重，前端用于节点大小 |
| `x` | `Double` | 否 | overview 固定径向布局 X 坐标 |
| `y` | `Double` | 否 | overview 固定径向布局 Y 坐标 |

`CanvasEdge` 字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | `String` | 是 | 画布内稳定边 ID |
| `source` | `String` | 是 | source node ID |
| `target` | `String` | 是 | target node ID |
| `label` | `String` | 否 | 关系显示文本 |
| `relationType` | `String` | 否 | 关系类型 |
| `weight` | `Double` | 否 | 关系权重 |
| `dashed` | `Boolean` | 是 | 空位或弱关系使用虚线 |

### Overview Rules

`overviewView.categoryCards` 必须固定返回 14 个正式门类。

空位门类字段：

- `entityCount = 0`
- `relationCount = 0`
- `appliedVersionCount = 0`
- `latestVersionNo = null`
- `entryHref = /knowledge/atlas?level=category&categoryCode={categoryCode}`

`canvasView` overview 规则：

- `mode = overview`
- `focusNodeId = root:sancai`
- `nodes` 固定 15 个：1 个 root 节点 + 14 个 category 节点。
- root 节点 `id = root:sancai`、`kind = root`、`label = 三才图会`、`metricValue = 14`。
- category 节点必须全部带 `categoryCode`、`href`、`status`。
- 无数据 category 节点 `status = EMPTY`、`weight = 0`。
- `edges` 固定 14 条，从 `root:sancai` 连到每个 category 节点。

### Category Rules

对有效但无 applied graph version 的正式门类：

- `currentLevel = category`
- `categoryView.categoryCode = {categoryCode}`
- `categoryView.categoryName = {categoryName}`
- `categoryView.latestVersionId = null`
- `categoryView.latestVersionNo = null`
- `categoryView.entityHighlights = []`
- `categoryView.relationGroups = []`
- `categoryView.sourceReferences` 返回一条空态说明
- `canvasView.mode = category`
- `canvasView.empty = true`
- `canvasView.nodes` 至少包含当前门类节点
- `canvasView.edges = []`

对有 applied graph version 的门类：

- `canvasView.nodes` 包含一个 category focus 节点和当前版本实体节点。
- `canvasView.edges` 来自 `knowledge_relation`，source/target 必须能匹配实体节点。
- 节点点击实体进入 `/knowledge/atlas?level=detail&entityId={entityId}`。

### Detail Rules

对有效实体：

- `canvasView.mode = detail`
- `canvasView.focusNodeId = entity:{entityId}`
- `canvasView.nodes` 包含焦点实体节点、相邻实体节点和来源摘要节点。
- `canvasView.edges` 包含一跳实体关系边，来源摘要用 `kind = source` 节点承载。
- 焦点节点 `status = FOCUS`。

## Task Breakdown

每个任务控制在 2-5 个文件内，完成后单独看 diff。

### Task 1: 后端读模型字段

文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalAtlasResult.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/atlas/controller/response/KnowledgePortalAtlasResponse.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/atlas/assembler/KnowledgePortalAtlasInterfaceAssembler.java`

动作：

- 在 application result 新增 `CanvasView`、`CanvasNode`、`CanvasEdge` 嵌套类。
- 在 interface response 新增同名 response 嵌套类。
- assembler 完整映射 `canvasView`、`nodes`、`edges`。

### Task 2: 后端 14 门类和画布装配

文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationServiceImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationServiceImplTest.java`

动作：

- `buildOverviewAtlas` 合并固定 14 门类清单和 applied version 统计。
- `buildCategoryAtlas` 对有效空门类返回 category 空态，不回退 overview。
- 新增 `buildOverviewCanvasView`、`buildCategoryCanvasView`、`buildDetailCanvasView` 私有方法。
- 测试覆盖 overview 返回 14 门类、空门类零值、空门类 category 空态、detail 焦点节点。

门类真相源：

- 优先使用 Classics 已有正式门类读协作。
- 如果当前无合适协作入口，本轮可在 Knowledge application 内使用只读常量适配，但必须用注释标明“临时适配 Classics 正式门类排序”，并在 coverage 风险中记录后续应收敛为协作入口。

### Task 3: 前端依赖和类型

文件：

- `kuzhambu-apps/portal-web/package.json`
- `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-types.ts`
- `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-service.ts`

动作：

- 用 `npm --workspace portal-web install @xyflow/react@^12.11.1 @dagrejs/dagre@^3.0.0` 安装依赖。
- 根据生成或更新后的 lockfile，把 `package.json` dependencies 中 `@xyflow/react` 和 `@dagrejs/dagre` 改成精确版本号。
- `knowledge-atlas-types.ts` 新增 `KnowledgeAtlasCanvasView`、`KnowledgeAtlasCanvasNode`、`KnowledgeAtlasCanvasEdge`。
- `KnowledgeAtlasResponse` 新增 `canvasView: KnowledgeAtlasCanvasView | null`。
- fallback 数据补齐 14 门类和三层 `canvasView`，不得继续只返回 2 个示例门类。

### Task 4: 前端图谱控件

文件：

- `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-graph-canvas.tsx`
- `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-graph-layout.ts`
- `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-graph-canvas.css`
- `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.css`

控件用法：

- `knowledge-graph-canvas.tsx` 使用 `ReactFlow`、`Background`、`Controls`、`MiniMap`。
- `ReactFlow` 设置 `nodesDraggable={false}`、`nodesConnectable={false}`、`elementsSelectable={false}`、`fitView`。
- `ReactFlow` 使用 `nodeTypes` 注册 `knowledgeGraphNode` 自定义节点。
- 节点点击读取 `node.data.href`，通过 `useNavigate` 跳转。
- `Controls` 只保留 zoom in、zoom out、fit view；不提供编辑动作。
- `MiniMap` 仅在桌面宽度显示，移动端隐藏。

布局规则：

- overview 不走 dagre，使用 `node.x`、`node.y` 或本地固定径向算法；中心点固定，14 门类按稳定顺序环形排列。
- category 使用 `@dagrejs/dagre`，rank direction 使用 `LR`，门类 focus 节点在左，实体节点向右展开。
- detail 使用 `@dagrejs/dagre`，rank direction 使用 `LR`，焦点实体在左中，相邻实体和来源节点向右展开。
- 所有布局输出转换为 `@xyflow/react` 的 `Node` 和 `Edge`。

视觉规则：

- `root` 和 `FOCUS` 节点使用深蓝背景。
- 普通 category/entity 节点使用青绿色背景。
- `EMPTY` 节点使用浅灰绿背景、虚线边框。
- 空位边 `dashed = true` 时使用虚线。
- 节点内显示 `label`、`subtitle`、`metricLabel + metricValue`。

### Task 5: 前端页面接入和测试

文件：

- `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.tsx`
- `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.test.tsx`
- `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.css`
- `docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`

页面操作：

- 在 `knowledge-atlas-page.tsx` 的主 stage 区域渲染 `KnowledgeGraphCanvas`。
- overview 层：画布在上，14 门类卡片或摘要在下。
- category 层：画布在上，实体列表和关系摘要在下。
- detail 层：画布在上，右侧详情面板展示实体属性、关联关系和来源证据。
- breadcrumb、URL 参数和现有 Link 行为保持不变。
- 空门类 category 页显示空画布和“尚未应用图谱版本”文案。

测试：

- overview 测试断言 14 个门类文本都出现。
- overview 测试断言存在图谱画布容器。
- category 空态测试断言停留 category 层，不回到 overview。
- category 测试断言点击实体节点会跳转 detail URL。
- detail 测试断言焦点实体、关系边标签和来源摘要可见。

coverage 更新：

- 只把 `固定 14 类空位展示` 和 `图谱可视化画布` 相关项改为已完成。
- 不把 `从质量报告低质量门类一键触发重提取` 改为已完成。
- 世系图独立画布未覆盖时继续保留未完成说明。

## Acceptance

验收点：

- RUNBOOK 和实现说明已明确引用 `docs/60-human/classics-ui-concepts/classics-knowledge-graph-rag.png`。
- `/knowledge/atlas?level=overview` 展示中心节点和 14 个门类节点。
- 14 个门类都有卡片或摘要，空位显示 `0` 和 `未应用`。
- 点击空位门类进入 category 空态页，URL 为 `level=category&categoryCode=...`。
- category 有数据时展示门类节点、实体节点和关系边。
- detail 有数据时展示焦点实体节点、一跳关系节点、来源摘要节点和右侧详情面板。
- 画布支持缩放、平移、fit view，不支持拖拽编辑、连线、删除。
- Network 面板浏览 atlas 不出现抽取、重生成、candidate apply 或 worker 调用。
- 搜索和问答在未生成图谱时仍可使用。

## Validation

后端：

```sh
cd kuzhambu-servers
mvn -pl biz/knowledge -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/knowledge -am test
```

前端：

```sh
cd kuzhambu-apps
npm --workspace portal-web install @xyflow/react@^12.11.1 @dagrejs/dagre@^3.0.0
# Inspect the generated or updated lockfile, then pin both dependencies in portal-web/package.json
# to the exact resolved versions before continuing.
npm --workspace portal-web run format
npm run format:check
npm run lint
npm --workspace portal-web run test -- knowledge-atlas-page
npm run build
```

视觉冒烟：

- 启动 Portal Web。
- 桌面视口打开 `/knowledge/atlas?level=overview`、`/knowledge/atlas?level=category&categoryCode={emptyCategory}`、`/knowledge/atlas?level=category&categoryCode={filledCategory}`、`/knowledge/atlas?level=detail&entityId={entityId}`。
- 移动端视口重复打开以上 4 个 URL。
- 对照效果图确认三层结构、画布非空、文字不重叠、空位门类清晰可见。

## Closeout

收口顺序：

1. 完成 Task 1 和 Task 2，后端读模型返回完整 `canvasView`。
2. 完成 Task 3，引入依赖并补齐前端类型和 fallback。
3. 完成 Task 4，实现只读图谱控件和布局。
4. 完成 Task 5，接入页面、补测试、更新 coverage。
5. 运行后端、前端和视觉冒烟验证。
6. 实现审核通过后删除本 RUNBOOK 或在任务关闭 PR 中清理残留引用。

本 RUNBOOK 不要求提交；实现完成后先保留工作区 diff 供审核。
