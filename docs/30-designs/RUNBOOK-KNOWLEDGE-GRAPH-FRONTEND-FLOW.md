# RUNBOOK Knowledge Graph Frontend Flow

## Purpose

本 RUNBOOK 用于整理后台知识图谱前端操作逻辑，目标是把当前“任务表驱动”的工程入口调整为“稿件驱动”的业务闭环。

目标闭环：

1. 用户从稿件树选择 `SANCAI_ENTRY`、`WANGQI_DOCUMENT` 或 `MING_CUSTOMS` 内容。
2. 稿件节点展示图谱处理状态。
3. 用户在稿件详情中触发图谱抽取。
4. 系统展示 AI 抽取候选。
5. 用户人工修改实体、关系、世系和质量标注。
6. 用户应用候选或精修结果，生成或更新图谱版本。
7. 稿件树、图谱结果和质量状态同步回到可继续操作的状态。

## Scope

本次整理覆盖 `kuzhambu-admin-web` 的知识图谱后台前端链路，以及与链路直接相关的后端接口和系统菜单种子数据。

### Frontend Routes

- `kuzhambu-apps/admin-web/src/router/index.tsx`
  - `knowledge/graph-extraction`：知识抽取任务页面。
  - `knowledge/graph-results`：图谱版本结果页面。
  - `knowledge/refinement`：图谱精修工作台。
  - `knowledge/quality-report`：图谱质量报告页面。

- `kuzhambu-apps/admin-web/src/layouts/admin-layout.tsx`
  - `getOpenKeys` 必须把 `/knowledge/graph-extraction` 归入 `/knowledge/graph` 菜单展开分组。

### Current Frontend Components

- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-page.tsx`
  - 当前抽取页容器。
  - 管理任务创建、任务分页、任务详情、候选应用、重生成和批量取消。

- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-create.tsx`
  - 当前创建任务表单。
  - 现状是工程化输入：用户手填 `sourceContentType`、`sourceContentId`、`scopeJson`、`selectionScopeJson`、`promptMessagesJson`、`inputPayloadJson`。
  - 目标是降级为高级/调试入口；主入口应由稿件详情自动组装这些字段。

- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-task-table.tsx`
  - 当前任务台账表。
  - 目标保留为运维视角，不作为业务主入口。

- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-task-detail.tsx`
  - 当前任务详情抽屉。
  - 目标用于查看 AI 候选、错误、请求快照和应用入口。

- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/components/graph-version-table.tsx`
  - 图谱版本表。
  - 已增加从版本进入精修的链接：`/knowledge/refinement?graphVersionId={versionId}`。

- `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-page.tsx`
  - 当前精修页容器。
  - 支持从 URL 查询参数 `graphVersionId` 打开后端精修任务。

- `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-task-drawer.tsx`
  - 当前分段精修抽屉。
  - 分段包括实体、关系、世系节点、世系关系、质量标注和应用后续动作。

- `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/hooks/use-refinement-workbench.ts`
  - 精修页本地状态 Hook。
  - 管理当前详情、编辑对象、删除对象、质量标注目标、应用后续结果和当前分段。

### Frontend Services And Types

- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-service.ts`
  - `addTask`：创建抽取任务。
  - `pageTasks`：分页查询任务。
  - `getTaskDetail`：查询任务详情。
  - `applyTaskCandidate`：应用 AI 候选。
  - `regenerateTask`：精修后重生成。
  - `cancelBatchTask`：取消批量任务。

- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-types.ts`
  - `GraphExtractionTaskType`：`RELATION`、`GRAPH`、`LINEAGE`。
  - `GraphExtractionTriggerSource`：`MANUAL`、`QUALITY_REPORT`、`REFINEMENT_APPLIED`、`REGENERATE` 等。
  - `GraphExtractionTaskRecord`：承载任务、来源、候选和状态。

- `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-service.ts`
  - `getTaskDraft`：通过 `graphVersionId` 打开或创建精修任务。
  - `applyTask`：应用精修任务。
  - `upsertEntity`、`upsertRelation` 等：编辑精修草稿。
  - `pageQualityAnnotations`、`upsertQualityAnnotation`、`deleteQualityAnnotation`：质量标注。

### Backend Integration Points

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/controller/KnowledgeGraphExtractionController.java`
  - 后台图谱抽取接口入口。

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/service/impl/KnowledgeGraphExtractionApplicationServiceImpl.java`
  - 抽取任务编排。
  - `validateTarget` 要求 `sourceContentType` 和 `sourceContentId`。
  - `resolveTargets` 支持 `selectionScopeJson` 批量拆分。
  - `applyTaskCandidate` 应用候选并生成图谱版本。

- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/knowledge/service/impl/KnowledgeAiExtractionApplicationServiceImpl.java`
  - 把知识抽取请求转为 AI 调用。
  - `contentType` 使用 `sourceContentType`。
  - `contentId` 使用 `sourceContentId`。
  - `inputPayloadJson` 是当前实际送入 AI 的抽取正文载体。

- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/knowledge/support/KnowledgeAiWorkerUsecaseResolver.java`
  - `RELATION` -> `KNOWLEDGE_RELATION_EXTRACTION`。
  - `GRAPH` -> `KNOWLEDGE_GRAPH_EXTRACTION`。
  - `LINEAGE` -> `KNOWLEDGE_LINEAGE_EXTRACTION`。
  - `TAG` -> `KNOWLEDGE_TAG_EXTRACTION`。

### System Data

- `db/data-source/system.json`
  - 当前缺少 `/knowledge/graph-extraction` 的菜单配置。
  - 应新增 `知识治理/知识图谱/知识抽取任务`，并赋予 `knowledge:graph:view`、`knowledge:graph:edit`、`knowledge:graph:apply`。

- `db/data/system.sql`
  - 如项目使用 SQL 种子初始化菜单，必须与 `system.json` 同步新增菜单和角色菜单关系。

## Non-goals

- 不在本 RUNBOOK 中改造后端领域模型。
- 不调整 AI worker 的抽取协议。
- 不改变三才图会、王圻文档、明代民俗的内容编辑页面本身。
- 不把任务台账删除；任务台账仍可作为运维视角保留。
- 不把本 RUNBOOK 作为长期需求真相源；长期结论应迁移到需求、设计或 readiness 文档。

## Current State

当前实现已经具备底层对象和基础链路，但前端业务入口不够自然。

### 已具备

- `/knowledge/graph-extraction` 路由存在。
- 抽取任务支持 `RELATION`、`GRAPH`、`LINEAGE`。
- 抽取任务可携带 `sourceContentType` 和 `sourceContentId`。
- 抽取任务支持 `selectionScopeJson` 批量范围。
- 抽取候选可以通过 `applyTaskCandidate` 应用为图谱事实和图谱版本。
- `/knowledge/graph-results` 可展示图谱版本。
- `/knowledge/refinement?graphVersionId=...` 可打开精修任务。
- 精修页已经拆出 `RefinementTaskDrawer`，适合承载大体量人工修改界面。

### 缺口

- 菜单种子没有配置 `/knowledge/graph-extraction`。
- 业务用户需要手填 `sourceContentType` 和 `sourceContentId`。
- 业务用户需要手填 `inputPayloadJson`。
- 没有统一稿件树作为知识图谱主入口。
- 稿件节点没有图谱状态展示。
- 稿件详情没有聚合抽取任务、候选、图谱版本、精修任务和质量报告。

## Target Operation Model

### Primary Flow

1. 用户进入 `知识治理/知识图谱/知识抽取任务`。
2. 页面左侧展示稿件树。
3. 用户选择来源库：
   - 三才图会：`SANCAI_ENTRY`
   - 王圻文档：`WANGQI_DOCUMENT`
   - 明代民俗：`MING_CUSTOMS`
4. 用户点击稿件节点。
5. 右侧稿件图谱详情展示：
   - 稿件标题。
   - 来源类型。
   - 来源 ID。
   - 当前图谱状态。
   - 最近抽取任务。
   - 最新图谱版本。
   - 质量摘要。
6. 用户点击 `抽取图谱`。
7. 系统根据稿件自动组装：
   - `sourceContentType`
   - `sourceContentId`
   - `scopeType`
   - `scopeJson`
   - `inputPayloadJson`
   - `promptMessagesJson`
8. 抽取完成后，详情区域展示 AI 候选实体、关系和世系。
9. 用户点击 `应用候选`。
10. 系统生成图谱版本。
11. 用户点击 `进入精修`。
12. `RefinementTaskDrawer` 打开，用户分段修改：
    - 实体。
    - 关系。
    - 世系节点。
    - 世系关系。
    - 质量标注。
13. 用户点击 `应用任务`。
14. 稿件状态更新为已精修、待质检或质量异常。

### Secondary Flow

- `/knowledge/graph-results` 保留版本视角。
- `/knowledge/refinement` 保留精修任务视角。
- `/knowledge/quality-report` 保留质量报告视角。
- 任务表保留为抽取任务台账，用于排查失败任务、重生成、批量取消和审计。

## Plan

### Step 1: Menu Entry

修改系统菜单配置。

Files:

- `db/data-source/system.json`
- `db/data/system.sql`
- `kuzhambu-apps/admin-web/src/layouts/admin-layout.tsx`

Actions:

- 在 `知识治理/知识图谱` 下新增 `知识抽取任务`。
- URL 使用 `/knowledge/graph-extraction`。
- 权限使用 `knowledge:graph:view`、`knowledge:graph:edit`、`knowledge:graph:apply`。
- 确认 `/knowledge/graph-extraction` 会展开 `/knowledge/graph` 菜单。

Verification:

- 登录后台后菜单能看到 `知识抽取任务`。
- 点击菜单进入 `/knowledge/graph-extraction`。
- 刷新页面后菜单分组仍展开。

### Step 2: Manuscript Tree Component

新增稿件树组件。

Files:

- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-manuscript-tree.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-page.css`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-types.ts`

Actions:

- 树根按来源库分组：三才图会、王圻文档、明代民俗。
- 树节点承载 `sourceContentType`、`sourceContentId`、`title`、`graphStatus`。
- 节点状态以 badge 或 tag 展示。
- 点击稿件节点后设置当前稿件上下文。

Status values:

- `NOT_EXTRACTED`
- `EXTRACTING`
- `EXTRACTION_FAILED`
- `CANDIDATE_READY`
- `APPLIED`
- `REFINING`
- `REFINED`
- `QUALITY_ISSUE`

Verification:

- 三类来源库均可展开。
- 点击稿件后右侧详情更新。
- 状态标签不挤压树节点文本。

### Step 3: Manuscript Graph Detail

新增稿件图谱详情组件。

Files:

- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-manuscript-detail.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-task-detail.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-service.ts`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-types.ts`

Actions:

- 展示稿件基础信息。
- 展示最近抽取任务。
- 展示最新 AI 候选摘要。
- 展示最新图谱版本。
- 展示质量摘要。
- 提供 `抽取图谱`、`应用候选`、`进入精修`、`查看版本` 操作。

Verification:

- 未选择稿件时显示空状态。
- 已选择稿件时按钮状态与权限、任务状态一致。
- `进入精修` 跳转到 `/knowledge/refinement?graphVersionId=...`。

### Step 4: Replace Manual JSON Entry

把 `GraphExtractionCreate` 从业务主入口改为高级入口。

Files:

- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-create.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-service.ts`

Actions:

- 主流程不再要求用户手填 `sourceContentType`。
- 主流程不再要求用户手填 `sourceContentId`。
- 主流程不再要求用户手填 `inputPayloadJson`。
- 保留高级表单用于调试和异常补录。
- 从稿件上下文自动生成 `GraphExtractionCreateCommand`。

Verification:

- 用户选择稿件后可以一键创建 `GRAPH` 抽取任务。
- 生成请求仍包含后端必需字段。
- 高级入口仍可手工提交任务。

### Step 5: Candidate Preview And Apply

强化 AI 候选展示和应用入口。

Files:

- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-task-detail.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-task-table.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-types.ts`

Actions:

- 任务详情中展示 AI 候选 payload 的实体、关系、世系摘要。
- `SUCCEEDED` 且候选未应用时显示 `应用候选`。
- 应用成功后展示生成的 `graphVersionId`。
- 提供 `进入精修` 和 `查看图谱结果`。

Verification:

- 成功任务能看到候选摘要。
- 已应用任务不重复暴露危险应用操作。
- 应用成功后能进入精修。

### Step 6: Refinement Drawer As Detail Editor

继续使用分段抽屉作为人工修改界面。

Files:

- `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-task-drawer.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-entity-table.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-relation-table.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-lineage-node-table.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-lineage-relation-table.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-quality-annotation-table.tsx`

Actions:

- 从 `graphVersionId` 打开精修任务。
- 抽屉分段承载大体量内容。
- `应用任务` 后切到后续动作分段。
- 后续动作包括查看结果、发起重生成和查看质量报告。

Verification:

- URL 带 `graphVersionId` 时自动打开精修抽屉。
- 实体、关系、世系、标注分段切换不丢状态。
- 应用任务后展示后续动作。

### Step 7: Tests

补齐前端测试。

Files:

- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-page.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-task-table.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/components/graph-version-table.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-page.test.tsx`

Actions:

- 覆盖稿件选择。
- 覆盖一键抽取请求组装。
- 覆盖候选应用后的版本跳转。
- 覆盖图谱版本进入精修。
- 覆盖 `graphVersionId` 自动打开精修任务。

Commands:

```sh
cd kuzhambu-apps
pnpm --filter kuzhambu-admin-web exec vitest run \
  src/pages/knowledge/graph-extraction/graph-extraction-page.test.tsx \
  src/pages/knowledge/graph-results/components/graph-version-table.test.tsx \
  src/pages/knowledge/refinement/refinement-page.test.tsx
pnpm --filter kuzhambu-admin-web run format
pnpm run format:check
pnpm run lint
pnpm run build
```

## Verification

### Automated

Run from `kuzhambu-apps/`:

```sh
pnpm --filter kuzhambu-admin-web exec vitest run \
  src/pages/knowledge/graph-extraction/graph-extraction-page.test.tsx \
  src/pages/knowledge/graph-results/components/graph-version-table.test.tsx \
  src/pages/knowledge/refinement/refinement-page.test.tsx
pnpm run format:check
pnpm run lint
pnpm run build
```

### Manual

Manual smoke path:

1. Open `/knowledge/graph-extraction`.
2. Select a `SANCAI_ENTRY` manuscript from the tree.
3. Confirm the right detail panel displays source type, source ID and graph status.
4. Click `抽取图谱`.
5. Confirm a graph extraction task is created.
6. Open the task detail and inspect AI candidate summary.
7. Click `应用候选`.
8. Confirm a graph version is produced.
9. Click `进入精修`.
10. Confirm `/knowledge/refinement?graphVersionId=...` opens `RefinementTaskDrawer`.
11. Edit one entity or relation.
12. Apply the refinement task.
13. Confirm follow-up actions render and the manuscript graph status changes.

## Commit Boundaries

Keep each commit to 1-5 files.

Suggested sequence:

1. `Feat(knowledge): 配置图谱抽取菜单`
   - `db/data-source/system.json`
   - `db/data/system.sql`
   - `kuzhambu-apps/admin-web/src/layouts/admin-layout.tsx`

2. `Feat(knowledge): 新增稿件树图谱入口`
   - `graph-extraction-manuscript-tree.tsx`
   - `graph-extraction-page.tsx`
   - `graph-extraction-page.css`
   - `graph-extraction-types.ts`

3. `Feat(knowledge): 增加稿件图谱详情`
   - `graph-extraction-manuscript-detail.tsx`
   - `graph-extraction-page.tsx`
   - `graph-extraction-service.ts`
   - `graph-extraction-types.ts`

4. `Feat(knowledge): 串联候选应用后续动作`
   - `graph-extraction-task-detail.tsx`
   - `graph-extraction-task-table.tsx`
   - `graph-extraction-page.tsx`

5. `Test(knowledge): 覆盖稿件图谱操作闭环`
   - `graph-extraction-page.test.tsx`
   - `graph-version-table.test.tsx`
   - `refinement-page.test.tsx`

## Closure

本 RUNBOOK 是临时执行手册。知识图谱前端闭环完成并通过验证后，应执行以下收口：

1. 把稳定的信息架构迁移到 `docs/30-designs/KNOWLEDGE-DESIGN.md`。
2. 把上线验证证据迁移到 `docs/40-readiness/KNOWLEDGE-RUNTIME-SMOKE-EVIDENCE.md` 或新增对应 readiness 文档。
3. 删除本 RUNBOOK。
4. 确认没有其他文档继续引用本 RUNBOOK。
