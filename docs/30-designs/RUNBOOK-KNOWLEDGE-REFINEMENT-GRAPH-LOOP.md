# Knowledge 精修与图谱联动闭环 RUNBOOK

## 目标

完成 Knowledge 数据精修与图谱联动闭环：管理员应用精修任务后，精修结果立即写入当前图谱版本的正式事实；Admin 页面刷新精修、图谱结果、图谱版本和质量报告相关数据；页面提供明确控件让管理员查看当前图谱版本、发起图谱重生成、重新生成质量报告。

## 已确认方案

- 精修应用后不自动触发图谱重生成，由前端按钮引导用户触发。
- 精修应用后不创建新的 `GraphVersion`，继续写回当前 `graphVersionId`。
- 图谱重生成候选应用后，继续由 `KnowledgeGraphCandidateApplySupport` 创建递增版本。
- 精修应用后不自动生成质量报告，由前端提示指定 `graphVersionId` 的报告需要重新生成。
- Knowledge 仍只通过 AI 域触发 AI 能力，不直接调用 workers。
- 本 RUNBOOK 不要求数据库结构变更；新增字段均为 application result、controller response 和前端类型字段。

## 范围

- 后端：Knowledge refinement apply、graph extraction regenerate、graph results、quality report。
- 前端：Admin Knowledge 的 `refinement`、`graph-extraction`、`graph-results`、`quality-report` 页面。
- 不修改 workers，不新增搜索或问答依赖，不改 Portal。

## 数据结构变更

### 1. 新增 `RefinementApplyResult`

新增文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/result/RefinementApplyResult.java`

字段：

| 字段 | 类型 | 必填 | 取值规则 |
| --- | --- | --- | --- |
| `refinementTaskId` | `Long` | 是 | `RefinementTask.refinementTaskId` |
| `graphVersionId` | `Long` | 是 | `RefinementTask.graphVersionId` |
| `taskType` | `String` | 是 | `GRAPH`、`RELATION` 或 `LINEAGE` |
| `sourceContentType` | `String` | 否 | 来自 `GraphVersion.sourceContentType`，为空时回退 `RefinementTask.sourceContentType` |
| `sourceContentId` | `Long` | 否 | 来自 `GraphVersion.sourceContentId`，为空时回退 `RefinementTask.sourceContentId` |
| `sourceCategoryCode` | `String` | 否 | 来自 `GraphVersion.sourceCategoryCode`，为空时回退 `RefinementTask.sourceCategoryCode` |
| `sourceCategoryName` | `String` | 否 | 来自 `GraphVersion.sourceCategoryName`，为空时回退 `RefinementTask.sourceCategoryName` |
| `status` | `String` | 是 | 固定为 `APPLIED` |
| `appliedAt` | `Long` | 是 | `RefinementTask.appliedAt` 转 epoch millis |
| `graphRefreshRequired` | `Boolean` | 是 | 固定为 `true` |
| `regenerateSupported` | `Boolean` | 是 | `sourceTaskId != null` |
| `sourceTaskId` | `Long` | 否 | 当前 `GraphVersion.taskId`，用于 `GraphExtractionRegenerateCommand.sourceTaskId` |
| `selectionScopeJson` | `String` | 否 | 优先源抽取任务 `selectionScopeJson`；为空时用 `graphVersionId`、`sourceContentType`、`sourceContentId`、`sourceCategoryCode` 组装 JSON |
| `replaceUnconfirmedOnly` | `Boolean` | 是 | 固定为 `true` |
| `triggerSource` | `String` | 是 | 固定为 `REFINEMENT_APPLIED` |
| `nextAction` | `String` | 是 | 固定为 `OPEN_GRAPH_VERSION` |
| `qualityReportRefreshRequired` | `Boolean` | 是 | 固定为 `true` |

### 2. 修改 refinement apply 接口响应

修改文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/response/RefinementResponses.java`

新增内部类：

- `RefinementResponses.ApplyResponse`

字段与 `RefinementApplyResult` 完全一致：

- `refinementTaskId`
- `graphVersionId`
- `taskType`
- `sourceContentType`
- `sourceContentId`
- `sourceCategoryCode`
- `sourceCategoryName`
- `status`
- `appliedAt`
- `graphRefreshRequired`
- `regenerateSupported`
- `sourceTaskId`
- `selectionScopeJson`
- `replaceUnconfirmedOnly`
- `triggerSource`
- `nextAction`
- `qualityReportRefreshRequired`

修改文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/assembler/KnowledgeGraphRefinementInterfaceAssembler.java`

新增方法：

- `toResponse(RefinementApplyResult result)`

修改文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/KnowledgeGraphRefinementController.java`

接口变更：

- `POST /api/knowledge/refinement/task/apply` 返回类型从 `RefinementResponses.DetailResponse` 改为 `RefinementResponses.ApplyResponse`。

### 3. 修改 graph regenerate 请求

修改文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/controller/request/GraphExtractionRequests.java`

在 `RegenerateRequest` 增加字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `triggerSource` | `String` | 否 | 精修来源传 `REFINEMENT_APPLIED`；为空时使用现有 `REGENERATE` |

修改文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/service/KnowledgeGraphExtractionApplicationService.java`

将 `regenerateTask` 增加 `String triggerSource` 参数，或新增专用 command 方法。推荐新增 command 方法，避免继续扩大参数列表：

```java
GraphExtractionTaskResult regenerateTask(RegenerateGraphExtractionCommand command);
```

新增文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/command/RegenerateGraphExtractionCommand.java`

字段：

- `taskType: String`
- `sourceTaskId: GraphExtractionTaskId`
- `triggerSource: String`
- `selectionScopeJson: String`
- `replaceUnconfirmedOnly: Boolean`
- `requestedBy: Long`

### 4. 修改 graph version 响应

修改文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/result/GraphVersionResult.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/controller/response/GraphExtractionResponses.java`

新增字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `refinementApplied` | `Boolean` | 是 | 该 `versionId` 是否存在 `APPLIED` 精修任务 |
| `lastRefinementTaskId` | `Long` | 否 | 最新已应用精修任务 ID |
| `lastRefinementAppliedAt` | `Long` | 否 | 最新精修应用时间，epoch millis |

字段从 `RefinementTaskRepository` 推导，不新增表字段。

### 5. 修改 quality report 响应

修改文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/result/QualityReportDetailResult.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/response/QualityReportResponses.java`

在 detail 顶层增加字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `stale` | `Boolean` | 是 | 最新报告是否早于精修应用 |
| `staleReason` | `String` | 否 | 固定原因值 `REFINEMENT_APPLIED_AFTER_REPORT` |
| `lastRefinementAppliedAt` | `Long` | 否 | 最新精修应用时间，epoch millis |

`stale=true` 条件：

- `latest(graphVersionId)` 找到报告。
- 同一 `graphVersionId` 存在已应用精修任务。
- `QualityReport.generatedAt < RefinementTask.appliedAt`。

### 6. 前端类型

修改文件：

- `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-types.ts`

新增：

```ts
export type RefinementApplyNextAction = "OPEN_GRAPH_VERSION" | string;

export interface RefinementApplyRecord {
    refinementTaskId: number;
    graphVersionId: number;
    taskType?: RefinementTaskType | null;
    sourceContentType?: string | null;
    sourceContentId?: number | null;
    sourceCategoryCode?: string | null;
    sourceCategoryName?: string | null;
    status?: RefinementTaskStatus | null;
    appliedAt?: number | null;
    graphRefreshRequired: boolean;
    regenerateSupported: boolean;
    sourceTaskId?: number | null;
    selectionScopeJson?: string | null;
    replaceUnconfirmedOnly: boolean;
    triggerSource: "REFINEMENT_APPLIED" | string;
    nextAction: RefinementApplyNextAction;
    qualityReportRefreshRequired: boolean;
}
```

修改文件：

- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-types.ts`

字段变更：

- `GraphExtractionTriggerSource` 增加 `"REFINEMENT_APPLIED"`。
- `GraphExtractionRegenerateCommand` 增加 `triggerSource?: GraphExtractionTriggerSource | null`。

修改文件：

- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/graph-results-types.ts`

`GraphVersionRecord` 增加：

- `refinementApplied?: boolean | null`
- `lastRefinementTaskId?: number | null`
- `lastRefinementAppliedAt?: number | null`

修改文件：

- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-types.ts`

`QualityReportDetailRecord` 增加：

- `stale?: boolean | null`
- `staleReason?: string | null`
- `lastRefinementAppliedAt?: number | null`

## 后端任务拆分

### 任务 1：精修应用返回图谱联动结果

文件范围：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/KnowledgeGraphRefinementApplicationService.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/impl/KnowledgeGraphRefinementApplicationServiceImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/result/RefinementApplyResult.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/response/RefinementResponses.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/assembler/KnowledgeGraphRefinementInterfaceAssembler.java`

处理动作：

- 将 `applyTask(Long refinementTaskId, Long appliedBy)` 返回类型改为 `RefinementApplyResult`。
- `applyTask` 仍通过 `RefinementApplySupport` 写回当前 `graphVersionId`，不创建新 `GraphVersion`。
- 更新 `RefinementTask.status=APPLIED`、`appliedBy`、`appliedAt` 后，组装 `RefinementApplyResult`。
- 读取 `GraphVersionRepository.getByVersionId(task.getGraphVersionId())`，回填来源字段。
- 从 `GraphVersion.taskId` 设置 `sourceTaskId`；能读取源抽取任务时复用源任务 `selectionScopeJson`。
- 无源任务时返回 `regenerateSupported=false`，仍返回 `nextAction=OPEN_GRAPH_VERSION`。

验收点：

- `/api/knowledge/refinement/task/apply` 响应为 `ApplyResponse`。
- 响应包含 `graphVersionId`、`replaceUnconfirmedOnly=true`、`triggerSource=REFINEMENT_APPLIED`、`qualityReportRefreshRequired=true`。
- 精修事实写入当前版本，未创建新版本。

### 任务 2：图谱重生成支持精修来源

文件范围：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/command/RegenerateGraphExtractionCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/service/KnowledgeGraphExtractionApplicationService.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/service/impl/KnowledgeGraphExtractionApplicationServiceImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/controller/request/GraphExtractionRequests.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/assembler/KnowledgeGraphExtractionInterfaceAssembler.java`

处理动作：

- `RegenerateRequest` 接收 `triggerSource`。
- `RegenerateGraphExtractionCommand.triggerSource` 为空时使用 `REGENERATE`，从精修页进入时使用 `REFINEMENT_APPLIED`。
- `replaceUnconfirmedOnly` 为空时默认 `true`。
- `selectionScopeJson` 优先使用前端传入值；为空时复用源任务值。
- `requestTasks` 的 `triggerSource` 使用 command 中的值，不在实现里强制覆盖为 `REGENERATE`。

验收点：

- 精修页发起的重生成任务在任务表中显示 `triggerSource=REFINEMENT_APPLIED`。
- 重生成仍通过 `AiFacade` 发起 AI 域调用。

### 任务 3：图谱结果读取当前版本精修事实

文件范围：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/support/RefinementApplySupport.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/service/impl/KnowledgeGraphExtractionApplicationServiceImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/result/GraphVersionResult.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/controller/response/GraphExtractionResponses.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/assembler/KnowledgeGraphExtractionInterfaceAssembler.java`

处理动作：

- 保持 `RefinementApplySupport` 写回 `latestVersionId=graphVersionId`。
- 确认 `pageEntities`、`pageRelations`、`pageLineageNodes`、`pageLineageRelations` 使用传入 `versionId` 读取正式事实。
- `getVersionDetail` 和 `pageVersions` 返回 `refinementApplied`、`lastRefinementTaskId`、`lastRefinementAppliedAt`。
- 这些字段从 `RefinementTaskRepository` 查询当前 `graphVersionId` 最新 `APPLIED` 任务推导。

验收点：

- 精修应用后进入指定 `graphVersionId`，正式实体、关系、世系节点和世系关系表能看到人工精修内容。
- 版本详情展示精修应用标记和时间。

### 任务 4：质量报告过期提示与重新生成

文件范围：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/impl/KnowledgeQualityReportApplicationServiceImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/result/QualityReportDetailResult.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/response/QualityReportResponses.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/assembler/KnowledgeQualityReportInterfaceAssembler.java`

处理动作：

- `generateReport(graphVersionId)` 仍从正式图谱事实计算，不自动生成报告。
- `latest(graphVersionId)` 和 `detail(reportId)` 返回 `stale`、`staleReason`、`lastRefinementAppliedAt`。
- 当最新报告生成时间早于最新精修应用时间时，返回 `stale=true`。

验收点：

- 精修应用后质量报告页能识别旧报告已过期。
- 用户重新生成报告后，报告指标基于精修后的正式事实。

## 前端任务拆分

### 任务 1：精修页展示联动动作

文件范围：

- `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-service.ts`
- `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-types.ts`
- `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-page.test.tsx`

控件与操作：

- 现有 `应用精修` Button 点击后调用 `service.applyTask`。
- 成功后用 `messageApi.success` 显示：`精修已应用，图谱结果已刷新`。
- 在当前精修详情区增加一个操作区，使用三个 Button：
  - `查看图谱结果`：primary Button，始终展示；点击跳转 `/knowledge/graph-results?graphVersionId={graphVersionId}`。
  - `重生成图谱`：default Button，仅当 `regenerateSupported=true && sourceTaskId` 时展示；点击跳转 `/knowledge/graph-extraction`，通过 query 或 `location.state` 携带 `taskType`、`sourceTaskId`、`selectionScopeJson`、`replaceUnconfirmedOnly=true`、`triggerSource=REFINEMENT_APPLIED`。
  - `重新生成质量报告`：default Button，当 `qualityReportRefreshRequired=true` 时展示；点击跳转 `/knowledge/quality-report?graphVersionId={graphVersionId}&stale=1`。
- 应用成功后不要把 `detail` 清空；保留当前任务详情，更新操作区。

React Query 刷新：

- `["knowledge", "refinement", "tasks"]`
- `["knowledge", "refinement", "quality-summary", refinementTaskId]`
- `["knowledge", "refinement", "annotations", refinementTaskId]`
- `["knowledge", "graph-extraction"]`
- `["knowledge", "graph-results"]`
- `["knowledge", "quality-report"]`

验收点：

- 应用成功后页面出现三个明确后续动作，不需要用户复制 ID。
- `重生成图谱` 不在 `regenerateSupported=false` 时展示。

### 任务 2：图谱抽取页承接精修重生成参数

文件范围：

- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-create.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-service.ts`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-types.ts`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-page.test.tsx`

控件与操作：

- 页面进入时读取 `taskType`、`sourceTaskId`、`selectionScopeJson`、`replaceUnconfirmedOnly`、`triggerSource`。
- 如果存在 `sourceTaskId`，自动打开 `graph-extraction-create` 表单。
- 表单控件预填：
  - `任务类型` Select：值为 `taskType`。
  - `触发来源` Select：值为 `REFINEMENT_APPLIED`；如该字段不面向用户展示，则用隐藏字段提交。
  - `源任务 ID` InputNumber：值为 `sourceTaskId`；可只读。
  - `重生成范围` TextArea：值为 `selectionScopeJson`。
  - `仅替换未人工确认结果` Switch：默认开启，提交值为 `replaceUnconfirmedOnly=true`。
- 用户点击 `提交` Button 后调用 `service.regenerateTask`。
- 创建成功后关闭表单，刷新任务表，并高亮或选中新任务行。

验收点：

- 从精修页点击 `重生成图谱` 进入时，表单自动打开并完成预填。
- 提交 payload 包含 `triggerSource=REFINEMENT_APPLIED` 和 `replaceUnconfirmedOnly=true`。

### 任务 3：图谱结果页按版本定位

文件范围：

- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/graph-results-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/components/graph-version-table.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/components/graph-version-detail.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/graph-results-types.ts`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/graph-results-page.test.tsx`

控件与操作：

- 页面读取 `graphVersionId` query。
- `graph-version-table` 的当前选中行设置为该 `graphVersionId`。
- 如果当前分页没有该版本，调用 `getVersionDetail({ versionId: graphVersionId })` 并展示详情面板。
- `graph-version-detail` 展示字段：
  - `versionNo`
  - `status`
  - `appliedAt`
  - `refinementApplied`
  - `lastRefinementTaskId`
  - `lastRefinementAppliedAt`
- 实体表、关系表、世系节点表、世系关系表的查询参数统一使用选中的 `graphVersionId`。

验收点：

- 从精修页点击 `查看图谱结果` 后，页面直接定位对应版本。
- 用户切换版本后，四个结果表同步刷新。

### 任务 4：质量报告页按版本提示重算

文件范围：

- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/components/quality-report-generate-form.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/components/quality-report-summary.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-types.ts`
- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-page.test.tsx`

控件与操作：

- 页面读取 `graphVersionId` query，并填入 `quality-report-generate-form` 的 `图谱版本 ID` InputNumber。
- 页面加载后调用 `getLatestReport({ graphVersionId })`。
- 若没有报告，展示 Empty，并提供 `生成质量报告` Button。
- 若 `stale=true`，在 `quality-report-summary` 上方展示 Alert：
  - `type="warning"`
  - message：`精修已应用，当前报告需要重新生成`
  - action：`重新生成` Button
- 用户点击 `生成质量报告` 或 `重新生成` 后调用 `generateReport({ graphVersionId, generatedBy })`。
- 生成成功后刷新报告历史、摘要、问题清单、来源明细和人工标注明细。

验收点：

- 从精修页点击 `重新生成质量报告` 后，质量报告页定位到对应版本。
- 过期报告不会被展示成最新有效结论。

## 测试与验证

### 后端测试

测试文件范围：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/service/impl/KnowledgeGraphRefinementApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/KnowledgeGraphRefinementControllerTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/graph/service/impl/KnowledgeGraphExtractionApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/service/impl/KnowledgeQualityReportApplicationServiceImplTest.java`

覆盖点：

- `applyTask` 返回 `RefinementApplyResult`。
- 精修应用写回当前版本，不创建新版本。
- `/api/knowledge/refinement/task/apply` 返回 `ApplyResponse` 字段。
- `REFINEMENT_APPLIED` 重生成来源被保留。
- `replaceUnconfirmedOnly` 默认 `true`。
- 质量报告 stale 判断和重新生成后指标刷新。

后端验证命令：

```sh
cd kuzhambu-servers
mvn -pl biz/knowledge -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/knowledge -am test
```

### 前端测试

测试文件范围：

- `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-page.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-page.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/graph-results-page.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-page.test.tsx`

覆盖点：

- 精修应用成功后展示 `查看图谱结果`、`重生成图谱`、`重新生成质量报告`。
- `regenerateSupported=false` 时不展示 `重生成图谱`。
- 图谱抽取页从精修来源进入时自动打开并预填表单。
- 图谱结果页按 `graphVersionId` 定位版本，并刷新四个结果表。
- 质量报告页展示 stale Alert，并可点击 `重新生成`。

前端验证命令：

```sh
cd kuzhambu-apps
pnpm --filter kuzhambu-admin-web run format
pnpm run format:check
pnpm run lint
pnpm --filter kuzhambu-admin-web run test
```

## 完成验收

- 精修应用后，正式图谱事实立即包含人工精修内容。
- 精修应用后，Admin 精修页出现 `查看图谱结果`、`重生成图谱`、`重新生成质量报告` 的明确操作。
- 图谱重生成请求来源为 `REFINEMENT_APPLIED`，默认只替换未人工确认结果。
- 精修应用不创建新 `GraphVersion`；重生成候选应用后才创建递增版本。
- 质量报告不会在精修应用后被误认为自动更新；页面明确提示并支持重新生成。
- 后端、前端测试覆盖精修应用、图谱重生成引导、版本定位和质量报告重算。
