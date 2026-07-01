# RUNBOOK Sancai Visual Asset Workflow Closure

## 1. Purpose

本文档定义“三才图会视觉资产工作流闭环包”的执行手册。

本轮目标不是继续补单个字段或单个按钮，而是将 `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md` 中与三才图会视觉资产相关的一组“部分完成”需求，推进到可按事实改写为“已完成”的阶段性交付。

本 RUNBOOK 只用于：

- 约束本轮范围和前置决策。
- 明确数据结构边界，精确到字段。
- 明确相关文件，精确到具体文件名。
- 将大块工作拆成可执行的小任务，每个小任务只覆盖 2 到 5 个文件。

本 RUNBOOK 审核通过后，后续再据此拆 TODO。

## 2. Confirmed Decisions

本轮已确认以下决策，后续实现不得再摇摆：

- `image_gen` 默认生成新的 `visual asset version`，不直接覆盖当前 version。
- 视觉资产面板只展示视觉工作流所需的最小上下文，不重做整套条目详情页。
- `fusion` 强依赖当前已确认的 `imageAnalysisMarkdown`，不允许绕过图片理解直接运行。
- 候选 payload 允许人工修改，但只发生在候选确认阶段，不引入独立草稿编辑模式。
- `image_gen` 最小闭环必须同时具备预览和下载。
- coverage 文档的目标是推进到“已完成”，但最终状态必须按真实交付结果落文档，不预先承诺。

## 3. Implementation Notes

本节记录已确认的实现说明。后续拆 TODO、写代码和做 code review 时，均以本节为准。

### 3.1 `image_analysis` / `visual` / `fusion` 字段职责固定

三类能力的写回字段固定如下：

- `image_analysis` -> `imageAnalysisMarkdown`
- `visual` -> `visualDescription`
- `fusion` -> `fusionDescription`

禁止行为：

- `image_analysis` 同时写入 `fusionDescription`
- `image_analysis` 同时写入 `visualDescription`
- `fusion` 写入 `imageAnalysisMarkdown`
- `visual` 写入 `fusionDescription`

### 3.2 `image_gen` 版本策略

`image_gen` 默认生成新的 `visual asset version`。

允许的窄兜底规则：

- 仅当当前 version 尚未形成生成图，且后端业务规则明确要求复用当前 version 时，才允许将生成图挂接到当前 version。

默认情况下，不允许用 `image_gen` 结果直接覆盖已有生成图的 version。

### 3.3 标签范围

本轮视觉资产面板中的标签仅要求“可见”，不要求“可编辑”。

标签在本轮中的职责：

- 作为视觉工作流最小上下文的一部分展示给操作者。
- 作为 `fusion` 和 `visual` 的任务输入上下文之一。

本轮不要求：

- 在视觉资产面板内重做标签治理入口。
- 在视觉资产面板内完成标签新增、删除、排序或批量确认。

### 3.4 权重规则

本轮权重字段固定遵守以下规则：

- `textWeight` 必须是整数。
- `imageWeight` 必须是整数。
- 两个字段都不能为空。
- 两个字段的组合约束由后端统一校验。

暂不在本 RUNBOOK 中锁死：

- `textWeight + imageWeight` 是否必须恒等于 `100`。

若实现阶段决定强制和为 `100`，必须同步更新本 RUNBOOK、前后端校验和接口测试。

### 3.5 artifact 与 Storage 的关系

`image_gen` 返回的 artifact 不得直接作为页面正式图片事实。

允许行为：

- 页面展示“处理中”状态。
- 后端接收 artifact 引用后转存到 Storage。

固定要求：

- 页面正式预览基于已转存的 `StorageObject`。
- 页面正式下载基于已转存的 `StorageObject`。
- `generatedImageStorageObjectId` 只保存转存后的正式对象，不保存临时 artifact id。

### 3.6 Workers 改动策略

Workers 侧固定遵循“按实际链路补最小文件集”原则。

若 `image_gen` 仍走现有 AI usecase 路由，优先改动：

- `kuzhambu-workers/src/kuzhambu_workers/schemas/ai.py`
- `kuzhambu-workers/src/kuzhambu_workers/ai/usecase_registry.py`
- `kuzhambu-workers/tests/test_ai_usecase_routes_classics.py`
- `kuzhambu-workers/tests/test_ai_usecase_registry.py`

仅当实际落地需要 artifact 路由时，才补充改动：

- `kuzhambu-workers/src/kuzhambu_workers/api/artifact_routes.py`
- `kuzhambu-workers/src/kuzhambu_workers/render/artifact_store.py`
- 对应 artifact 测试文件

### 3.7 `CLASSICS-DESIGN.md` 同步规则

若本轮只是收紧既有字段语义，可以仅在本 RUNBOOK 中说明，不强制立即修改 `CLASSICS-DESIGN.md`。

但只要出现以下任一情况，必须同步更新 `docs/30-designs/CLASSICS-DESIGN.md`：

- 新增视觉资产相关字段。
- 修改版本挂接规则。
- 修改 `classics_sancai_visual_asset` 字段的稳定语义。
- 将 artifact、中间态对象或新的资源归属规则引入稳定设计口径。

## 4. Coverage Goal

本 RUNBOOK 旨在集中推进以下 coverage 项：

- 三才图会：图片理解、信息融合、权重调节、视觉描述、AI 生图入口。
- 三才图会：从条目上下文进入视觉资产工作流。
- 三才图会：区分原始配图和视觉资产生成图。
- 三才图会：展示原文、译文、标签、配图和状态。

本 RUNBOOK 不承诺一次完成以下范围：

- 多条 entry 批量视觉资产治理。
- 多图缩略图生成和图库式放大浏览。
- 流式过程展示、复杂失败分类、批量重试平台。
- 脱离三才条目详情的独立视觉资产运营后台。

## 5. Bounded Context

本轮闭环固定围绕单个三才图会条目详情内的视觉资产工作流展开。

涉及责任域：

- `Classics`：视觉资产、图片、状态、版本、任务型入口和候选应用。
- `Admin Web`：条目详情、视觉资产面板、候选治理、预览下载和任务发起交互。
- `AI`：`image_analysis`、`visual`、`fusion`、`image_gen` 任务创建、候选结果、artifact 协作。
- `Storage`：生成图转存、资源读取 URL、预览和下载。
- `Workers`：若 `image_gen` 走 worker 产物链路，则负责产物执行和 artifact 输出。

## 6. Data Structure Change Plan

### 6.1 Database And Domain Fields

本轮默认不新增表，不新增三才视觉资产主表的新列，优先复用 `classics_sancai_visual_asset` 既有字段。

当前必须收口语义的字段：

- `classics_sancai_visual_asset.source_image_storage_object_id`
  - 语义：当前 version 对应的原图 Storage 对象。
- `classics_sancai_visual_asset.generated_image_storage_object_id`
  - 语义：当前 version 对应的 AI 生成图 Storage 对象。
- `classics_sancai_visual_asset.text_weight`
  - 语义：后续 `fusion` 和 `visual` 任务消费的文本权重。
- `classics_sancai_visual_asset.image_weight`
  - 语义：后续 `fusion` 和 `visual` 任务消费的图片权重。
- `classics_sancai_visual_asset.image_analysis_markdown`
  - 语义：仅保存 `image_analysis` 已确认结果。
- `classics_sancai_visual_asset.fusion_description`
  - 语义：仅保存 `fusion` 已确认结果。
- `classics_sancai_visual_asset.visual_description`
  - 语义：仅保存 `visual` 已确认结果。
- `classics_sancai_visual_asset.generation_params_json`
  - 语义：保存 `image_gen` 输入参数和版本化生成配置。
- `classics_sancai_visual_asset.status`
  - 语义：表示当前视觉资产 version 的处理阶段。
- `classics_sancai_visual_asset.current_used`
  - 语义：当前条目正在使用的视觉资产 version。

必须避免的错误语义：

- `image_analysis` 结果不得再同时写入 `fusion_description` 和 `visual_description`。
- `fusion_description` 不得兼作 `image_analysis` 的冗余副本。
- `visual_description` 不得兼作 `fusion` 的冗余副本。
- `generated_image_storage_object_id` 不得复用为“临时 artifact id”。

涉及文件：

- `docs/30-designs/CLASSICS-DESIGN.md`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/model/entity/SancaiVisualAsset.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/dataobject/SancaiVisualAssetDO.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/assembler/SancaiAssetPersistenceAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiAssetRepositoryImpl.java`

### 6.2 Admin API Contract Fields

本轮如现有接口字段不足，可在既有 request/response 上补齐，但不得创建第二套并行 contract。

当前需要稳定支持的视觉资产字段：

- `entryId`
- `visualAssetId`
- `versionNo`
- `status`
- `sourceImageStorageObjectId`
- `generatedImageStorageObjectId`
- `currentUsed`
- `textWeight`
- `imageWeight`
- `imageAnalysisMarkdown`
- `fusionDescription`
- `visualDescription`
- `generationParamsJson`

如本轮接通 `image_gen` 最小闭环，允许新增以下接口字段：

- `generatedArtifactId`
  - 仅用于后端接收 worker 或 AI 返回的 artifact 引用，落 Storage 后不得继续作为页面主事实。
- `generatedImageFilename`
  - 仅当下载展示链路确有需要时新增。

涉及文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiAssetRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiAssetResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiAssetInterfaceAssembler.java`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`

### 6.3 AI Candidate And Task Fields

本轮视觉资产候选必须稳定依赖 `objectId`，以 `visualAssetId` 作为 AI 候选归属对象。

必须成立的字段语义：

- `AiCandidate.objectId`
  - 三才视觉资产候选固定绑定 `visualAssetId`。
- `AiCandidate.capability`
  - 本轮至少支持 `image_analysis`、`visual`、`fusion`。
- `AiTask.capability`
  - 本轮至少支持 `image_analysis`、`visual`、`fusion`、`image_gen`。

若现有 capability 枚举未覆盖 `visual` 或 `image_gen`，必须补齐到前后端和 workers 的统一枚举。

涉及文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/command/AiCandidateApplyContentCommand.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
- `kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-payload-editor.tsx`
- `kuzhambu-workers/src/kuzhambu_workers/schemas/ai.py`
- `kuzhambu-workers/src/kuzhambu_workers/ai/usecase_registry.py`

## 7. Related Files By Area

### 7.1 Admin Web

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-model.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/hooks/use-sancai-entry-panel-state.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-payload-editor.tsx`

### 7.2 Classics Backend

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/SancaiAssetAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiAssetRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiAssetResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiAssetInterfaceAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/SancaiAssetApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/model/entity/SancaiVisualAsset.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiAssetRepository.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiAssetRepositoryImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/assembler/SancaiAssetPersistenceAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/dataobject/SancaiVisualAssetDO.java`

### 7.3 AI And Workers

- `kuzhambu-workers/src/kuzhambu_workers/schemas/ai.py`
- `kuzhambu-workers/src/kuzhambu_workers/api/ai_usecase_routes.py`
- `kuzhambu-workers/src/kuzhambu_workers/ai/usecase_registry.py`
- `kuzhambu-workers/tests/test_ai_usecase_routes_classics.py`
- `kuzhambu-workers/tests/test_ai_usecase_registry.py`

说明：

- 若 `image_gen` 实际走 artifact 路由，还需补充检查 `kuzhambu-workers/src/kuzhambu_workers/api/artifact_routes.py` 和 `kuzhambu-workers/src/kuzhambu_workers/render/artifact_store.py`。
- 若本轮无需新增 worker 路由，则不要顺手扩散修改无关 AI graph 文件。

## 8. Work Packages And Small Tasks

### 8.1 Global Operation Process

所有工作包固定按以下操作过程执行：

1. 先确认当前工作包涉及的字段语义、contract 字段和文件范围，不跨包顺手扩散。
2. 先改 request/response、domain entity 或前端 type 等事实定义，再改页面行为或服务逻辑。
3. 页面入口改动必须同时补刷新逻辑，确保任务完成后当前 visual asset 能立即刷新。
4. 候选应用类改动必须同时补“确认”和“拒绝”两条状态链路。
5. 任一工作包完成后，先跑该包最小测试，再进入下一包。

### WP1 统一视觉资产面板

目标：

- 在三才条目详情中形成统一视觉资产面板。
- 在同一视图中展示最小上下文：原文、译文、标签、当前配图、视觉资产状态。
- 在同一视图中展示当前 version、历史 version、原图、生成图。

操作过程：

1. 先在 `sancai-types.ts` 和 `sancai-entry-service.ts` 确认面板所需字段齐全。
2. 再在 `use-sancai-entry-panel-state.ts` 汇总当前 entry、当前 visual asset、历史 visual assets 和预览 URL。
3. 再在 `sancai-entry-model.tsx` 与 `sancai-entry-panel.tsx` 重组 UI，让视觉资产成为单一面板入口。
4. 最后补 `sancai-entry-panel.test.tsx` 和 `SancaiAssetAdminControllerTest.java`，锁定面板展示与 contract。

小任务拆分：

1. 面板布局与状态汇总
   - 文件数：4
   - 文件：
     - `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
     - `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-model.tsx`
     - `kuzhambu-apps/admin-web/src/pages/classics/sancai/hooks/use-sancai-entry-panel-state.ts`
     - `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`
2. 视觉资产服务契约对齐
   - 文件数：3
   - 文件：
     - `kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`
     - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiAssetResponse.java`
     - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiAssetInterfaceAssembler.java`
3. 面板回归测试
   - 文件数：2
   - 文件：
     - `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`
     - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/SancaiAssetAdminControllerTest.java`

### WP2 `image_analysis` 闭环

目标：

- 发起 `image_analysis` 任务。
- 候选按 `visualAssetId` 过滤。
- 候选确认后只写回 `imageAnalysisMarkdown`。

操作过程：

1. 先在前端候选面板调用中固定传入当前 `visualAssetId` 作为 `objectId`。
2. 再在后端候选应用逻辑中只更新 `imageAnalysisMarkdown`，移除对 `fusionDescription` 和 `visualDescription` 的混写。
3. 再补当前 asset 刷新逻辑，确保确认或拒绝后页面立即看到最新字段和值。
4. 最后补前端面板测试、后端候选应用测试和 workers contract 测试。

小任务拆分：

1. 前端任务入口与当前 asset 刷新
   - 文件数：4
   - 文件：
     - `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
     - `kuzhambu-apps/admin-web/src/pages/classics/sancai/hooks/use-sancai-entry-panel-state.ts`
     - `kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-panel.tsx`
     - `kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-payload-editor.tsx`
2. 后端候选应用语义收口
   - 文件数：3
   - 文件：
     - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
     - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java`
     - `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/model/entity/SancaiVisualAsset.java`
3. 回归测试
   - 文件数：3
   - 文件：
     - `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`
     - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`
     - `kuzhambu-workers/tests/test_ai_usecase_routes_classics.py`

### WP3 权重调节闭环

目标：

- `textWeight`、`imageWeight` 可编辑、可保存、可消费。

操作过程：

1. 先在前端 type、service 和表单模型中把 `textWeight`、`imageWeight` 作为正式可编辑字段。
2. 再在后端 request、assembler、entity 和 persistence assembler 中落实整数、非空和统一校验。
3. 再把当前权重接入后续 `fusion` 和 `visual` 任务的输入组装。
4. 最后补参数校验和任务消费测试，确认保存值会真正进入下游任务。

小任务拆分：

1. 前端编辑与保存
   - 文件数：3
   - 文件：
     - `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-model.tsx`
     - `kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`
     - `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`
2. 后端保存与参数校验
   - 文件数：5
   - 文件：
     - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiAssetRequest.java`
     - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiAssetInterfaceAssembler.java`
     - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java`
     - `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/model/entity/SancaiVisualAsset.java`
     - `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/assembler/SancaiAssetPersistenceAssembler.java`
3. 任务消费校验
   - 文件数：3
   - 文件：
     - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
     - `kuzhambu-workers/src/kuzhambu_workers/schemas/ai.py`
     - `kuzhambu-workers/tests/test_ai_usecase_routes_classics.py`

### WP4 `visual` 闭环

目标：

- 发起 `visual` 任务。
- 候选确认后只写回 `visualDescription`。

操作过程：

1. 先补齐前端 capability 枚举和候选编辑器能力识别。
2. 再补齐 workers 和后端对 `visual` capability 的统一识别。
3. 再在候选应用逻辑中只写回 `visualDescription`。
4. 最后补 contract 和应用测试，确认 `visual` 不会写入其他描述字段。

小任务拆分：

1. capability 与前端入口补齐
   - 文件数：4
   - 文件：
     - `kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-panel.tsx`
     - `kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-payload-editor.tsx`
     - `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
     - `kuzhambu-workers/src/kuzhambu_workers/schemas/ai.py`
2. 后端写回语义补齐
   - 文件数：4
   - 文件：
     - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
     - `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/model/entity/SancaiVisualAsset.java`
     - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiAssetResponse.java`
     - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiAssetInterfaceAssembler.java`
3. 契约与回归测试
   - 文件数：3
   - 文件：
     - `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-service-contract.test.ts`
     - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`
     - `kuzhambu-workers/tests/test_ai_usecase_registry.py`

### WP5 `fusion` 闭环

目标：

- 发起 `fusion` 任务时强依赖 `imageAnalysisMarkdown` 与当前权重。
- 候选确认后只写回 `fusionDescription`。

操作过程：

1. 先在前端发起任务时显式携带当前 `imageAnalysisMarkdown`、文本上下文和权重。
2. 再在后端或 workers 输入组装中强校验 `imageAnalysisMarkdown` 已存在。
3. 再在候选应用逻辑中只写回 `fusionDescription`。
4. 最后补回归测试，确认 `fusion` 不绕过图片理解且不污染其他字段。

小任务拆分：

1. 前端发起参数与显示
   - 文件数：3
   - 文件：
     - `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
     - `kuzhambu-apps/admin-web/src/pages/classics/sancai/hooks/use-sancai-entry-panel-state.ts`
     - `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`
2. 后端输入组装与独立写回
   - 文件数：4
   - 文件：
     - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
     - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java`
     - `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/model/entity/SancaiVisualAsset.java`
     - `kuzhambu-workers/src/kuzhambu_workers/ai/usecase_registry.py`
3. 测试锁定
   - 文件数：3
   - 文件：
     - `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`
     - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`
     - `kuzhambu-workers/tests/test_ai_usecase_routes_classics.py`

### WP6 候选治理按 visual asset 收敛

目标：

- entry 级候选与 visual asset 级候选彻底分离。

操作过程：

1. 先统一前端 query key 和筛选参数，确保视觉资产候选查询始终包含当前 `objectId`。
2. 再检查后端按 `objectId` 查找 visual asset 的边界条件，避免空值或错对象误应用。
3. 再补前后端回归测试，覆盖“同一 entry 同时有 entry 级和 visual asset 级候选”的场景。

小任务拆分：

1. 前端查询 key 与筛选边界
   - 文件数：3
   - 文件：
     - `kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-panel.tsx`
     - `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
     - `kuzhambu-apps/admin-web/src/pages/classics/sancai/hooks/use-sancai-entry-panel-state.ts`
2. 后端 objectId 匹配边界
   - 文件数：2
   - 文件：
     - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
     - `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/test/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/AiInvocationControllerTest.java`
3. 回归测试
   - 文件数：2
   - 文件：
     - `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`
     - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`

### WP7 `image_gen` 最小闭环

目标：

- 发起 `image_gen`。
- 获取 artifact。
- 转存 Storage。
- 新建 visual asset version。
- 页面可预览和下载。

操作过程：

1. 先补齐前端、后端和 workers 对 `image_gen` capability 的统一识别。
2. 再确定后端接收 artifact 的入口和转存 Storage 的调用点。
3. 再在 `SancaiAssetApplicationServiceImpl.java` 与 repository 层落实“默认新建 version”的版本规则。
4. 再在 admin controller、service contract 和页面中补生成图预览与下载链路。
5. 最后补应用服务、controller、artifact store 和前端页面测试，锁定最小闭环。

小任务拆分：

1. capability 与页面入口
   - 文件数：4
   - 文件：
     - `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
     - `kuzhambu-apps/admin-web/src/pages/classics/sancai/hooks/use-sancai-entry-panel-state.ts`
     - `kuzhambu-workers/src/kuzhambu_workers/schemas/ai.py`
     - `kuzhambu-workers/src/kuzhambu_workers/ai/usecase_registry.py`
2. artifact 转 Storage 与新 version 规则
   - 文件数：5
   - 文件：
     - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java`
     - `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiAssetRepository.java`
     - `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiAssetRepositoryImpl.java`
     - `kuzhambu-workers/src/kuzhambu_workers/api/artifact_routes.py`
     - `kuzhambu-workers/src/kuzhambu_workers/render/artifact_store.py`
3. API 展示与下载
   - 文件数：4
   - 文件：
     - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/SancaiAssetAdminController.java`
     - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiAssetResponse.java`
     - `kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`
     - `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
4. 最小闭环测试
   - 文件数：5
   - 文件：
     - `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`
     - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/SancaiAssetAdminControllerTest.java`
     - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/SancaiAssetApplicationServiceImplTest.java`
     - `kuzhambu-workers/tests/test_artifact_store.py`
     - `kuzhambu-workers/tests/test_ai_usecase_routes_classics.py`

## 9. Execution Order

建议固定顺序：

1. `WP1` 统一视觉资产面板。
2. `WP6` 候选治理按 visual asset 收敛。
3. `WP2` `image_analysis` 闭环。
4. `WP3` 权重调节闭环。
5. `WP4` `visual` 闭环。
6. `WP5` `fusion` 闭环。
7. `WP7` `image_gen` 最小闭环。
8. 更新 coverage，清理 TODO，清理本 RUNBOOK。

理由：

- 先统一入口和候选边界，后续 AI 任务不会重复返工 UI 组织。
- `fusion` 依赖 `imageAnalysisMarkdown` 和权重，应晚于 `image_analysis` 与权重闭环。
- `image_gen` 跨 AI、Storage、版本规则和下载链路，放最后收口更稳。

## 10. Acceptance Checklist

本 RUNBOOK 关闭前，必须同时满足：

- 三才条目详情内存在统一视觉资产面板。
- 当前视觉资产与历史版本都能查看原图、生成图、状态和关键描述字段。
- `image_analysis`、`visual`、`fusion` 都能从面板发起，并完成候选确认/拒绝闭环。
- `imageAnalysisMarkdown`、`visualDescription`、`fusionDescription` 三个字段只保存各自能力的确认结果。
- `textWeight`、`imageWeight` 可保存，并被后续任务实际消费。
- 候选列表按 `visualAsset.objectId` 精确治理。
- `image_gen` 至少完成 artifact -> Storage -> 新 version -> 页面预览/下载闭环。
- 对应前端、后端、workers 契约与必要测试已补齐。
- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md` 已按真实完成度更新。

## 11. Verification Strategy

执行期至少覆盖以下验证层次：

- Java servers：`mvn -q spotless:check`、`mvn -q checkstyle:check`、`mvn -q test`。
- Admin Web：`npm run format:check`、`npm run lint`、`npm test`、`npm run build`。
- Python workers：`ruff format --check .`、`ruff check .`、`pytest -p no:capture`。
- 页面闭环：至少覆盖当前 asset 查看、任务发起、候选确认/拒绝、生成图预览下载。
- Storage 冒烟：如 `image_gen` 接通 Storage，必须记录资源归属校验、预览路径、下载路径和人工冒烟结论。

## 12. Document Sync

本 RUNBOOK 推进过程中，至少检查以下文档：

- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
- `docs/30-designs/CLASSICS-DESIGN.md`
- `docs/20-interfaces/` 下与 AI、Storage、Workers 相关契约文档

规则：

- 只要视觉资产字段语义变化，就必须同步 `CLASSICS-DESIGN.md`。
- 只要前后端 contract 字段变化，就必须同步相关接口文档。
- 若本轮只补齐实现闭环、未改变稳定设计口径，则不扩散修改无关文档。

## 13. Closure Rule

本 RUNBOOK 关闭前必须满足：

- coverage 文档已按真实完成度更新。
- 本轮 TODO 已删除或收窄为剩余未完成项。
- 若 `image_gen` 仍有平台化缺口，必须明确保留在 coverage 的“未完成部分”中。
- PR 描述已记录本轮工作流闭环的验证命令、结果和风险边界。

任务关闭后，应删除本 RUNBOOK 及残留引用。
