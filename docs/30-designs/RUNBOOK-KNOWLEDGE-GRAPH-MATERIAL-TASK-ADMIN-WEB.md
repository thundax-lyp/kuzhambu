# RUNBOOK Knowledge Graph Material Task Admin Web

## Purpose

本手册只实现 `kuzhambu-apps/admin-web/` 的图谱素材管理和提取任务页面。前端必须先使用本地 Mock 完成所有交互，再切换到 Knowledge HTTP service；不等待 Servers 开发，不调用 Classics 或 AI HTTP 接口。

唯一 HTTP 真相源是 [`KNOWLEDGE-GRAPH-INTERFACE.md`](../20-interfaces/KNOWLEDGE-GRAPH-INTERFACE.md)。组件只能消费页面 service 暴露的领域类型，不能直接定义或猜测后端 payload。

路径别名在全文固定且可直接展开：`AW` = `kuzhambu-apps/admin-web/src/pages/knowledge`，`AWE2E` = `kuzhambu-apps/admin-web/e2e`。所有未加前缀的文件名均位于其所在条目指定的目录内。

## Scope

- `/knowledge/graph-materials` 素材管理页面与素材详情 `KuzhambuSegmentedDrawer`。
- `/knowledge/graph-extraction` 提取任务页面与任务详情 `KuzhambuSegmentedDrawer`。
- 同域 service、types、Mock adapter、fixtures、单元测试、Playwright E2E 和真实接口切换。
- 工作台及其他图谱页面到上述两个入口的跳转修正。

## Non-goals

- 不实现 Java、数据库、Classics facade、AI 调用、调度或清理。
- 不从浏览器调用 Classics、AI、旧 graph extraction/result/refinement 接口。
- 不把删除任务/删除变更重新做成独立菜单；它们只在素材 drawer 的“发布与变更”段进入。
- 不在页面组件中保存散乱 mock 数据；所有 mock 必须经过 service adapter。

## Fixed Frontend Types

以下类型放在指定 `*-types.ts`。HTTP request/response 类型只放在同域 `*-service.ts` 且不导出。

| Type | Required fields | File |
| --- | --- | --- |
| `GraphMaterialRecord` | `id?`, `contentRef`, `title`, `contentType`, `category?`, `volume?`, `status?`, `lockVersion?` | `graph-material-types.ts` |
| `GraphMaterialStatsRecord` | 节点/边、发布贡献、活动/待审/失败任务数、`statsRevision`, `calculatedAt` | `graph-material-types.ts` |
| `GraphMaterialListRecord` | `source`, `material?`, `materialStats?`, `latestTask?` | `graph-material-types.ts` |
| `GraphExtractionTaskRecord` | `id`, `materialRef`, `lockVersion`, `executionStatus`, `disposition`, `attemptNo`, `progress`, `currentStage`, `batchId?`, `purgeAfter?` | `graph-extraction-types.ts` |
| `GraphExtractionStageRecord` | `stageNo`, `stageCode`, `status`, `progress`, 摘要、失败和时间字段 | `graph-extraction-types.ts` |
| `GraphCandidatePreviewRecord` | `candidateId`, `nodes`, `edges`, `issues`, `diff`, `dispositionRecord?` | `graph-extraction-types.ts` |

固定 UI 状态：

```text
素材 drawer section = OVERVIEW | DRAFT_GRAPH | TASKS | PUBLICATION_CHANGES
任务 drawer section = OVERVIEW | EXECUTION | CANDIDATE | DISPOSITION
任务列表 mode = NONE | MATERIAL
候选 diff = ADD | UPDATE | REMOVE | CONFLICT
```

## Service Contract Rules

1. `graph-material-service.ts` 和 `graph-extraction-service.ts` 是页面唯一数据入口；只有其中可以使用 `postJson`。
2. 所有 service 方法名必须是业务动词，例如 `pageMaterials`、`getMaterial`、`createExtraction`、`retryTask`、`applyCandidate`，不能叫 `save` 或 `handle`。
3. 任务写操作生成新的 `idempotencyKey`，并发送当前任务 `lockVersion`、预期执行状态和必要的采纳状态；`GRAPH_TASK_LOCK_CONFLICT`、`GRAPH_TASK_STATE_CONFLICT`、`GRAPH_TASK_ACTIVE_EXISTS`、`GRAPH_CANDIDATE_UNAVAILABLE` 必须转换为页面可处理的结果。
4. Mock 与 HTTP adapter 必须实现同一组 service 方法，输入/输出类型相同；页面不得识别 adapter 类型。

## Small Tasks

### W0. Freeze Page Boundaries and Legacy Dependencies

**Modify exactly:**

- `kuzhambu-apps/admin-web/src/router/index.tsx`
- `kuzhambu-apps/admin-web/src/layouts/admin-layout.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-workbench/workbench-detail-drawer/workbench-detail-drawer.tsx`
- 所有引用 `graph-result`、`refinement` 作为新素材/任务流程跳转的文件。

**Steps:**

1. 保留 `knowledge/graph-materials` 和 `knowledge/graph-extraction` 路由。
2. 删除新流程到旧 graph-result/refinement 的跳转；工作台“素材”跳转统一到素材管理，“任务”跳转统一到提取任务。
3. 删除素材空间下独立删除变更/删除任务菜单依赖；保留旧页面仅到后端旧接口下线后的独立清理提交。

**Verify:** 路由单测和 `rg` 确认新素材/任务组件不 import `graph-result` 或 `refinement` service。

### W1. Build Service Types and Adapters Before UI

**Modify exactly:**

- 新建 `kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/graph-material-service.ts`
- `AW/graph-material/graph-material-types.ts`
- 新建 `AW/graph-material/__mocks__/graph-material-service-mock.ts`
- 新建 `AW/graph-material/graph-material-service-contract.test.ts`
- `AW/graph-material/__mocks__/graph-mock-data.ts`
- `AW/graph-extraction/graph-extraction-service.ts`
- `AW/graph-extraction/graph-extraction-types.ts`
- 新建 `AW/graph-extraction/__mocks__/graph-extraction-service-mock.ts`
- `AW/graph-extraction/graph-extraction-service-contract.test.ts`

**Steps:**

1. 定义 Fixed Frontend Types；不要导出 HTTP `Request`/`Response`。
2. 实现 material service：`pageMaterials`、`getMaterial`、`createExtraction`、`createBatchExtraction`、`previewBatchWithdrawal`、`withdrawBatch`。
3. 实现 extraction service：`pageTasks`、`getTask`、`retryTask`、`cancelTask`、`applyCandidate`、`discardCandidate`、`regenerateTask`。
4. Mock fixture 至少提供：未初始化素材、发布素材、失败任务、运行任务、成功待审候选、已处置任务、候选不可用、统计过期、批量部分失败、来源不可见。
5. Mock 的返回 ID、版本和时间必须是字符串；分页为正式 `Page<T>` 形状；错误必须使用正式业务码。

**Verify:** service contract test 断言 URL、body、`idempotencyKey`、任务版本/预期状态和 `ApiResponse` 解包；Mock 与 HTTP adapter 跑同一测试用例。

### W2. Replace Material List with Composite Material Catalog

**Modify exactly:**

- `AW/graph-material/graph-material-page.tsx`
- `AW/graph-material/graph-material-page.css`
- 新建 `AW/graph-material/material-table/material-table.tsx` 和 `AW/graph-material/material-table/index.ts`
- 新建 `AW/graph-material/material-filters/material-filters.tsx` 和 `AW/graph-material/material-filters/index.ts`
- 新建 `AW/graph-material/material-batch-actions/material-batch-actions.tsx` 和 `AW/graph-material/material-batch-actions/index.ts`
- `AW/graph-material/graph-material-page.test.tsx`
- 新建 `AW/graph-material/material-table/material-table.test.tsx`、`AW/graph-material/material-filters/material-filters.test.tsx`、`AW/graph-material/material-batch-actions/material-batch-actions.test.tsx`。

**Steps:**

1. 移除页面内置素材数组和“切换 Mock 空态/失败”演示按钮；改用 TanStack Query 调用 `pageMaterials`。
2. 表格固定显示：标题、来源类型、卷目/分类、草稿节点/关系、发布状态、任务摘要、最近候选处置、发布贡献、风险、最近变更。
3. 筛选固定为关键字、来源类型、分类、卷目、素材状态、任务运行状态、任务采纳状态和分页。
4. 行选择后只显示批量提取、批量发布、批量撤回、查看任务；批量执行后显示逐素材结果。
5. `material:null` 显示“未初始化/未抽取”，但允许发起提取；`statsRevision < lockVersion` 显示“统计更新中”。

**Verify:** 组件测试覆盖未初始化、统计过期、空列表、权限不足、批量部分失败和点击“查看任务”带 `contentRefs` 跳转。

### W3. Implement Material Segmented Drawer

**Modify exactly:**

- 新建 `AW/graph-material/material-detail-drawer/material-detail-drawer.tsx` 和 `AW/graph-material/material-detail-drawer/index.ts`
- 新建 `AW/graph-material/material-overview-panel/material-overview-panel.tsx` 和 `AW/graph-material/material-overview-panel/index.ts`
- 改造 `AW/graph-material/material-draft-canvas/material-draft-canvas.tsx`
- 新建 `AW/graph-material/material-task-summary-panel/material-task-summary-panel.tsx` 和 `AW/graph-material/material-task-summary-panel/index.ts`
- 改造 `AW/graph-material/publication-preview/publication-preview.tsx`
- `AW/graph-material/material-object-drawer/material-object-drawer.tsx` 和 `index.ts` 不在本任务修改；仅在后续确认草稿画布仍调用它时保留。

**Steps:**

1. 用 `KuzhambuSegmentedDrawer`，不手写 `KuzhambuDrawer + Segmented`；section 仅使用 Fixed Frontend Types 中四个值。
2. `OVERVIEW` 显示来源、统计、风险、最近活动；`DRAFT_GRAPH` 只放草稿画布；`TASKS` 只放任务摘要和跳转；`PUBLICATION_CHANGES` 放发布、撤回、删除预检入口。
3. 抽屉打开后才调用 `getMaterial(contentRef)`；关闭后清除选中素材 ID，不保留旧草稿对象状态。
4. 提取按钮成功后关闭当前动作状态并跳转/定位到提取任务，不能直接把候选写进草稿画布。

**Verify:** 测试四段均可访问；任务段无草稿编辑控件；已发布素材画布只读；抽屉详情加载错误有可恢复提示。

### W4. Replace Legacy Extraction Workbench with Task Queue

**Modify exactly:**

- `AW/graph-extraction/graph-extraction-page.tsx`
- `AW/graph-extraction/graph-extraction-page.css`
- `AW/graph-extraction/graph-extraction-task-table/graph-extraction-task-table.tsx`
- `AW/graph-extraction/graph-extraction-task-table/graph-extraction-task-table.test.tsx`
- 删除或停止引用 `graph-extraction-manuscript-tree/*`、`graph-extraction-manuscript-detail/*`、`graph-workbench-service.ts`；物理删除放在确认没有其他引用的单独提交。
- 新建 `AW/graph-extraction/task-filters/task-filters.tsx` 和 `AW/graph-extraction/task-filters/index.ts`
- 新建 `AW/graph-extraction/task-batch-create-panel/task-batch-create-panel.tsx` 和 `AW/graph-extraction/task-batch-create-panel/index.ts`

**Steps:**

1. 默认以 `groupBy=NONE` 查询全局任务队列；切换为 `MATERIAL` 后渲染素材组，不在浏览器重新分组服务端 flat 结果。
2. 筛选固定为关键字、来源类型、分类、卷目、`contentRefs`、`batchId`、运行状态、采纳状态和分页。
3. 表格固定显示素材、运行状态、采纳状态、进度/阶段、尝试次数、输入摘要、结果摘要、失败原因、关联任务、清理时间。
4. 批量创建面板只调用 `createBatchExtraction`：已选素材传 `contentRefs`，整卷传 `volumeCode`，两者不得同时发送。
5. 删除对 `knowledge:graph:apply` 的依赖；候选采用使用既有 `knowledge:graph:edit`。

**Verify:** 测试默认 flat、服务端 grouped、过滤 `batchId`、批量输入互斥、运行任务取消与失败任务重试的可见性。

### W5. Implement Task Segmented Drawer and Candidate Actions

**Modify exactly:**

- 改造 `AW/graph-extraction/graph-extraction-task-detail/graph-extraction-task-detail.tsx`
- 新建 `AW/graph-extraction/task-detail-drawer/task-detail-drawer.tsx` 和 `AW/graph-extraction/task-detail-drawer/index.ts`
- 新建 `AW/graph-extraction/task-execution-panel/task-execution-panel.tsx` 和 `AW/graph-extraction/task-execution-panel/index.ts`
- 新建 `AW/graph-extraction/task-candidate-panel/task-candidate-panel.tsx` 和 `AW/graph-extraction/task-candidate-panel/index.ts`
- 新建 `AW/graph-extraction/task-disposition-panel/task-disposition-panel.tsx` 和 `AW/graph-extraction/task-disposition-panel/index.ts`
- 删除或停止引用 `graph-extraction-candidate-modal.tsx`；物理删除在无引用后单独提交。

**Steps:**

1. 详情使用 `KuzhambuSegmentedDrawer`，sections 为 `OVERVIEW`、`EXECUTION`、`CANDIDATE`、`DISPOSITION`。
2. `EXECUTION` 按 `stages` 显示阶段、进度、摘要、失败原因和时间；不显示完整正文/提示词。
3. `CANDIDATE` 按 `candidatePreview` 显示节点、边、告警和 diff；`REMOVE` 只在覆盖预览中显示。`candidate:null` 显示服务端候选不可用空态。
4. 按状态渲染动作：`FAILED` 只有重试；`PENDING/RUNNING` 只有取消；`SUCCEEDED + PENDING` 才有合并、覆盖、丢弃；成功任务可重新抽取。
5. 每次 mutation 使用当前 task `lockVersion` 和预期状态；成功后失效素材列表、素材详情、任务列表和任务详情 query；冲突后只刷新，不在客户端猜测最终状态。

**Verify:** 组件测试覆盖四段、候选四类差异、候选不可用、每种动作状态、版本冲突刷新和处置后按钮消失。

### W6. Switch from Mock to Real Knowledge Service

**Modify exactly:**

- W1 的两个 `*-service.ts` 和 Mock adapter 选择入口。
- `AW/graph-material/graph-material-service-contract.test.ts`
- `AW/graph-extraction/graph-extraction-service-contract.test.ts`
- 新增或修改 `kuzhambu-apps/admin-web/e2e/knowledge-graph-material-task.spec.ts`。

**Steps:**

1. 保持领域类型和组件不变，只将 adapter 从 Mock 换为 `postJson` 实现。
2. 映射 Servers 的正式业务码到已测试的 UI 状态；不要通过字符串 message 判断错误类型。
3. 浏览器 Network 验证页面只请求 `/knowledge/graph/**`，不请求 Classics、AI 或旧图谱 URL。
4. 联调前端不得绕过缺失后端接口；接口缺失时保留 Mock 并在 readiness 记录阻塞点。

**Verify:** 真实环境跑一次素材页、素材 drawer、单项提取、任务 drawer、失败重试、候选采用和批量部分失败；保存 Network 与 E2E 证据。

## Required Commit Boundaries

1. `Feat(admin-web): 建立图谱素材任务服务契约与 Mock`：W1。
2. `Feat(admin-web): 重构图谱素材管理列表`：W2。
3. `Feat(admin-web): 增加图谱素材分段详情抽屉`：W3。
4. `Feat(admin-web): 重构图谱提取任务队列`：W4。
5. `Feat(admin-web): 增加图谱任务候选处置抽屉`：W5。
6. `Feat(admin-web): 接入图谱素材任务服务`：W6。
7. 旧组件和旧 service 的物理删除单独提交，先用 `rg` 证明无引用。

## Verification

每个小任务先运行同页面域的 unit/service tests。W5 完成后运行：

```sh
cd kuzhambu-apps
pnpm --filter kuzhambu-admin-web run format
pnpm run format:check
pnpm run lint
pnpm --filter kuzhambu-admin-web run test
pnpm --filter kuzhambu-admin-web run build
```

Mock E2E 必须验证：未初始化素材、统计更新中、单项/批量提取、批量部分失败、失败原地重试、取消、候选合并/覆盖/丢弃、候选不可用、版本冲突和来源不可见。真实联调只在 Servers 提供完整接口后执行。

## Closure

完成条件：W0-W6 都有测试证据；Mock 和真实 service 使用相同领域类型；页面无跨域业务请求；旧路径已独立清理或有明确保留原因。将证据写入 `docs/40-readiness/` 后删除本 RUNBOOK。
