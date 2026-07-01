# 三才图会单条图片理解闭环 RUNBOOK

## 目标

本 RUNBOOK 用于完成“三才图会单条图片理解闭环（任务创建 + 候选预览 + 人工确认 + 应用到 visual_asset）”。

本轮目标固定为：

- 在三才条目详情的视觉资产区块内，为单条 `visualAsset` 提供“创建图片理解任务”入口。
- 任务创建后，AI 域按既有 `image_analysis` 能力生成候选结果。
- Admin Web 仅展示当前选中 `visualAsset` 的 `image_analysis` 候选，并允许人工编辑、确认或拒绝。
- 确认后，将候选结果写入目标 `visual_asset.imageAnalysisMarkdown`，并顺手更新 `fusionDescription`、`visualDescription`，然后刷新视觉资产面板。

本轮不包含：

- 批量图片理解。
- `fusion`、`visual-description`、`image-gen` 闭环。
- 数据库 schema 变更。
- 多页面复用的通用图片理解组件抽象。

## 现状结论

### 已有能力

- workers 已登记三才图片理解 usecase：`/internal/ai/classics/sancai/image-analysis`。
  - 文件：`kuzhambu-workers/src/kuzhambu_workers/ai/usecase_registry.py`
- AI 精修接口已暴露 `image-analysis` 调用入口。
  - 文件：`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/AiRefinementController.java`
- AI 精修任务体系已支持 `capability = image_analysis`，任务记录中已有 `objectId`、`candidateId`、`resultPreview`。
  - 文件：`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/service/impl/AiRefinementTaskApplicationServiceImpl.java`
  - 文件：`kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/refinement/model/entity/AiRefinementTask.java`
- 三才视觉资产持久化与编辑已具备 `imageAnalysisMarkdown` 字段。
  - 文件：`kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/model/entity/SancaiVisualAsset.java`
  - 文件：`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/dataobject/SancaiVisualAssetDO.java`
- Admin Web 已具备视觉资产历史列表、当前版本切换、字段保存入口。
  - 文件：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-model.tsx`
  - 文件：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`

### 当前缺口

- `SancaiEntryPanel` 目前只支持创建 `translate`、`summary` 任务，没有单图 `image_analysis` 入口。
  - 文件：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
- `AiCandidatePanel` 当前 capability 白名单只有 `translate`、`summary`、`tags`、`qa`，不支持 `image_analysis`。
  - 文件：`kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-panel.tsx`
- AI 候选应用链路当前只支持 `translate`、`summary`、`tags`、`qa`，不能把 `image_analysis` 应用到 `SancaiVisualAsset.imageAnalysisMarkdown`。
  - 文件：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
- AI 候选应用请求与列表过滤没有 `objectId`，无法稳定限定到单条 `visualAsset`。
  - 文件：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/command/AiCandidateApplyContentCommand.java`
  - 文件：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/request/ClassicsContentRequest.java`
  - 文件：`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/request/AiInvocationRequests.java`
  - 文件：`kuzhambu-apps/admin-web/src/pages/classics/common/ai-candidate-service.ts`

## 闭环边界

### 单图目标定义

- 单图目标固定为一条 `SancaiVisualAsset`。
- 闭环关联键固定为：
  - `contentType = SANCAI_ENTRY`
  - `contentId = entryId`
  - `objectId = visualAssetId`

### 人工确认落点

- 人工确认后的正式写入字段固定为：
  - `classics_sancai_visual_asset.image_analysis_markdown`
  - `classics_sancai_visual_asset.fusion_description`
  - `classics_sancai_visual_asset.visual_description`

### 前端交互位置

- 交互入口固定放在三才条目详情弹窗的视觉资产面板中。
- 不新增独立页面。

## 数据结构变更

### 数据库表

本轮不改数据库 schema。

复用现有字段如下。

- 表：`classics_sancai_visual_asset`
  - `id`
  - `entry_id`
  - `version_no`
  - `status`
  - `source_image_storage_object_id`
  - `current_used`
  - `image_analysis_markdown`
  - `fusion_description`
  - `visual_description`
- 表：`ai_refinement_task`
  - `task_id`
  - `capability`
  - `content_type`
  - `content_id`
  - `object_id`
  - `candidate_id`
  - `result_preview`
  - `status`
- 表：`ai_candidate`
  - `candidate_id`
  - `capability`
  - `content_type`
  - `content_id`
  - `object_id`
  - `result_format`
  - `result_payload`
  - `status`

### Java servers 请求/命令变更

#### 1. AI 候选列表请求增加 `objectId`

- 文件：`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/request/AiInvocationRequests.java`
- 结构：`AiInvocationRequests.CandidateListRequest`
- 现有字段：
  - `String contentType`
  - `Long contentId`
  - `String capability`
  - `String status`
- 新增字段：
  - `Long objectId`

#### 2. Classics AI 候选应用请求增加 `objectId`

- 文件：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/request/ClassicsContentRequest.java`
- 结构：`ClassicsContentRequest.AiCandidateApplyRequest`
- 现有字段：
  - `Long candidateId`
  - `String contentType`
  - `Long contentId`
  - `String capability`
  - `String resultFormat`
  - `String resultPayload`
  - `String changeSummary`
- 新增字段：
  - `Long objectId`

#### 3. Classics AI 候选应用命令增加 `objectId`

- 文件：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/command/AiCandidateApplyContentCommand.java`
- 现有字段：
  - `Long candidateId`
  - `ClassicsContentType contentType`
  - `Long contentId`
  - `String capability`
  - `String resultFormat`
  - `String resultPayload`
  - `String changeSummary`
- 新增字段：
  - `Long objectId`

### Admin Web service/types 变更

#### 1. AI 候选列表查询增加 `objectId`

- 文件：`kuzhambu-apps/admin-web/src/pages/classics/common/ai-candidate-service.ts`
- 结构：`AiCandidateListQuery`
- 现有字段：
  - `contentType?: string | null`
  - `contentId?: number | null`
  - `capability?: string | null`
  - `status?: "PENDING" | "APPLIED" | "REJECTED" | string | null`
- 新增字段：
  - `objectId?: number | null`

#### 2. AI 候选应用命令增加 `objectId`

- 文件：`kuzhambu-apps/admin-web/src/pages/classics/common/ai-candidate-service.ts`
- 结构：`AiCandidateApplyCommand`
- 现有字段：
  - `candidateId: number`
  - `contentType: string`
  - `contentId: number`
  - `capability: string`
  - `resultFormat: string`
  - `resultPayload: string`
  - `changeSummary?: string | null`
- 新增字段：
  - `objectId: number`

#### 3. 三才图片理解任务创建命令复用既有 `objectId`

- 文件：`kuzhambu-apps/admin-web/src/pages/classics/common/ai-refinement-task-types.ts`
- 结构：`AiRefinementTaskCreatePayload`
- 本轮实际使用字段：
  - `capability: string`
  - `scope: string`
  - `contentType: string`
  - `contentId: number`
  - `objectId?: number | null`
  - `requestedBy: number`
  - `serviceRole?: string | null`
  - `modelId: number`
  - `modelName: string`
  - `requestId: string`
  - `traceId: string`
  - `promptMessagesJson: string`
  - `inputPayloadJson: string`
  - `locale?: string | null`

#### 4. 三才视觉资产前端记录复用既有字段

- 文件：`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`
- 结构：`SancaiVisualAssetRecord`
- 本轮读写字段：
  - `visualAssetId?: number | null`
  - `entryId?: number | null`
  - `status?: string | null`
  - `versionNo?: number | null`
  - `imageAnalysisMarkdown?: string | null`
  - `fusionDescription?: string | null`
  - `visualDescription?: string | null`
  - `sourceImageStorageObjectId?: number | null`
  - `currentUsed?: boolean | null`

#### 5. 三才视觉资产后台请求复用既有字段

- 文件：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiAssetRequest.java`
- 本轮实际写入字段：
  - `Long visualAssetId`
  - `Long entryId`
  - `String status`
  - `String imageAnalysisMarkdown`
  - `String fusionDescription`
  - `String visualDescription`

## 设计决策

### 候选预览只看当前选中视觉资产

- `AiCandidatePanel` 或三才专用包装层必须按 `objectId = selectedVisualAssetId` 过滤。
- 不允许把同条目下其他视觉资产的图片理解候选混入当前面板。

### 应用后直接写正式字段

- 应用 `image_analysis` 候选后，直接更新目标 `SancaiVisualAsset.imageAnalysisMarkdown`。
- 同一次应用中顺手更新目标 `SancaiVisualAsset.fusionDescription`、`SancaiVisualAsset.visualDescription`。
- `fusionDescription`、`visualDescription` 的生成方式由本轮实现决定，但必须在 RUNBOOK 对应任务内明确为“基于图片理解结果生成的默认补全”，不能要求用户额外再走独立 AI 任务。
- 本轮不引入“图片理解候选快照表”或“二次中间表”。

### 版本策略

- 本轮默认不为 `imageAnalysisMarkdown` 单独新增 `SancaiVisualAsset` 新版本。
- 应用行为只更新现有目标 `visualAsset` 记录。
- 如实现中发现当前服务层必须通过新版本表达变更，应补充决策说明后再调整。

## 任务拆分

### 任务 A：补齐后端 AI 候选 objectId 契约

目标：让候选列表和候选应用都能稳定定位到单条 `visualAsset`。

文件范围：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/request/AiInvocationRequests.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/request/ClassicsContentRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/command/AiCandidateApplyContentCommand.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/assembler/ClassicsContentInterfaceAssembler.java`

完成定义：

- `CandidateListRequest` 支持 `objectId`。
- `AiCandidateApplyRequest` / `AiCandidateApplyContentCommand` 支持 `objectId`。
- interface -> application 装配链路完整。

### 任务 B：补齐后端 `image_analysis` 候选应用到 visual asset

目标：让确认后的候选结果写入 `imageAnalysisMarkdown`，并顺手补齐 `fusionDescription`、`visualDescription`。

文件范围：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/assembler/ClassicsContentInterfaceAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/content/ClassicsContentAdminControllerTest.java`

完成定义：

- `applyAiCandidate(...)` 支持 `capability = image_analysis`。
- 仅允许 `contentType = SANCAI_ENTRY` 走该路径。
- 应用时必须校验 `objectId`，并将 `resultPayload` 解析后写入目标 `SancaiVisualAsset.imageAnalysisMarkdown`。
- 同一次应用中顺手更新：
  - `SancaiVisualAsset.fusionDescription`
  - `SancaiVisualAsset.visualDescription`
- `changeSummary` 默认值增加：
  - `AI 应用：图片理解`

### 任务 C：补齐三才图片理解任务创建入口

目标：在视觉资产面板内创建单图图片理解任务。

文件范围：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-model.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/common/ai-refinement-task-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`

完成定义：

- 当前选中 `visualAsset` 时显示“创建图片理解任务”按钮。
- 创建任务请求必须带：
  - `capability = "image_analysis"`
  - `contentType = "SANCAI_ENTRY"`
  - `contentId = entryId`
  - `objectId = visualAssetId`
- 创建前必须校验：
  - `selectedVisualAsset` 存在
  - `sourceImageStorageObjectId` 存在
- 任务创建成功后刷新任务列表并给出成功提示。

### 任务 D：补齐 AI 候选通用契约与单图过滤

目标：让通用 AI 候选能力支持 `image_analysis` 和 `objectId` 过滤。

文件范围：

- `kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/common/ai-candidate-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/ai-candidate-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-service-contract.test.ts`

完成定义：

- `AiCandidatePanel` capability 白名单增加 `image_analysis`。
- 候选列表请求支持 `objectId` 过滤。
- 候选应用请求携带 `objectId`。
- service contract 覆盖 `objectId` 请求体。

### 任务 E：补齐三才单图候选预览与人工确认接线

目标：在视觉资产上下文中预览、编辑、应用或拒绝 `image_analysis` 候选。

文件范围：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-model.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.test.tsx`

完成定义：

- 在三才视觉资产面板中，仅展示当前选中 `visualAsset` 的 `image_analysis` 候选。
- 创建任务成功后，面板能够读取当前 `visualAssetId` 对应的候选。
- 人工应用后，面板中 `imageAnalysisMarkdown`、`fusionDescription`、`visualDescription` 显示刷新后的正式值。
- 应用成功后刷新：
  - `["classics", "sancai", "entries", "detail", selectedEntryId]`
  - `["classics", "sancai", "entries", "visual-assets", selectedEntryId]`
  - `["ai", "candidates", "SANCAI_ENTRY", selectedEntryId]`

### 任务 F：补齐 AI/前端/Workers 契约回归

目标：保证闭环行为被测试固定。

文件范围：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/test/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/AiInvocationControllerTest.java`
- `kuzhambu-workers/tests/test_ai_usecase_routes_classics.py`
- `kuzhambu-workers/tests/test_ai_usecase_registry.py`

完成定义：

- AI invocation controller contract 覆盖 candidate list 的 `objectId` 入参。
- workers route contract 继续固定 `/internal/ai/classics/sancai/image-analysis` 不漂移。
- workers registry contract 继续固定 `image_analysis -> CLASSICS_SANCAI_IMAGE_ANALYSIS` 的映射不漂移。

## 验证顺序

### Java servers

先对涉及模块做窄范围格式化，再执行：

```sh
cd kuzhambu-servers
mvn -pl biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-interface,biz/ai/kuzhambu-ai-interface spotless:apply
mvn -q spotless:check
mvn -q checkstyle:check
mvn -q test
```

### Admin Web

前端必须按以下顺序执行：

```sh
cd kuzhambu-apps
npm --workspace admin-web run format
npm run format:check
npm run lint
npm run build
npm test
```

### Workers

如果本轮改到 workers 测试或路由契约，再执行：

```sh
cd kuzhambu-workers
.venv/bin/python -m ruff format .
.venv/bin/python -m ruff format --check .
.venv/bin/python -m ruff check .
.venv/bin/python -m pytest -p no:capture
```

## 文档收口

完成实现后必须同步：

- 更新 `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
  - 将“三才图会 -> 图片理解、信息融合、权重调节、视觉描述、AI 生图入口”的状态从当前“部分完成”拆细或更新为符合真实结果的描述。
- 新建 `TODO.md` 拆分执行项。
- 任务完成后删除本 RUNBOOK。
