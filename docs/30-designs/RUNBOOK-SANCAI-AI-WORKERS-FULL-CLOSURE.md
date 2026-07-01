# RUNBOOK Sancai AI Workers Full Closure

## 1. Purpose

本文档定义“三才图会 AI + Workers + Classics 完整闭环包”的执行手册。

本轮目标不是补单个按钮、单个 capability 或单个测试，而是将三才图会视觉资产 AI 工作流中已经写入需求、但当前仍未完全闭环的能力，推进到可以按事实宣称“已完成”的阶段。

本轮验收标准固定为：

- 需求项按事实关闭，不接受“部分完成”收口。
- 页面入口、Java 编排、Workers 最终态、候选治理、正式写回、版本追踪必须同时成立。
- 若范围过大，应缩范围，不得保留需求项但接受不完整交付。

## 2. Fixed Scope

### 2.1 In Scope

本轮只做三才图会，且只做视觉资产 AI 工作流。

纳入范围的能力：

- `image_analysis`
- `fusion`
- `visual`
- `image_gen`

纳入范围的业务对象：

- `SancaiEntry`
- `SancaiVisualAsset`

纳入范围的业务页面：

- `Admin Web /classics/sancai`

纳入范围的业务闭环：

- 单条视觉资产 AI 任务发起
- 批量图片理解
- 批量视觉资产处理
- AI 候选结果预览、编辑、接受、拒绝
- 失败原因展示
- 重试入口
- 取消入口
- 正式视觉资产 version 写回
- 当前使用 version 切换与页面刷新

### 2.2 Out Of Scope

本轮明确不做：

- 王圻文档 AI 闭环扩展
- 明代习俗 AI 闭环扩展
- `split`
- `translate`
- `summary`
- `tags`
- `qa`
- 批量公开/私有状态修改
- AI 后台模型治理、提示词治理、成本统计、主备状态面板
- Discovery / Knowledge 的新增 AI 能力接入
- 通用批量任务平台化抽象

## 3. Requirement Target

### 3.1 来自 `CLASSICS-REQUIREMENTS.md`

本轮必须关闭以下需求项：

- 必须支持从条目上下文进入该条目的视觉资产工作流。
- 必须支持基于图片理解结果进行信息融合。
- 必须支持文本和图片理解权重调节。
- 必须支持视觉描述生成、编辑和预览。
- 必须支持 AI 生图入口。
- 必须支持视觉资产产物历史和当前使用版本选择。
- 必须支持对多选条目批量执行视觉资产相关处理。

本轮必须同时满足以下业务规则：

- 视觉资产必须关联明确三才图会条目。
- 原图和 AI 生成图必须可区分。
- 生图失败不得影响已有原图和历史产物。
- 批量视觉资产处理必须复用单条视觉资产流程规则，单条失败不影响其他条目。
- 用户取消批量视觉资产处理后，已完成结果应保留。
- 用户可以选择当前使用版本。

### 3.2 来自 `AI-REQUIREMENTS.md`

本轮必须关闭以下需求项：

- 必须通过 workers 执行 AI 能力调用。
- 必须支持 workers 同步响应和流式响应。
- 必须支持接收 workers 的流式片段并转发给调用方展示。
- 必须在流式输出完成后保存最终 AI 调用结果。
- 必须支持三才图会图片理解。
- 图片理解结果必须支持 Markdown 编辑和预览。
- 图片理解必须支持使用已有结果和强制重新分析两种路径。
- 图片理解必须支持批量分析，单张失败不影响其他图片。
- AI 结果必须进入候选区，用户必须能接受、拒绝或编辑后接受 AI 结果。
- AI 调用失败时必须保留用户已输入内容，并提供明确失败原因和重试入口。

本轮必须同时满足以下业务规则：

- AI 能力调用必须经由 AI 域治理入口。
- AI 结果不得直接覆盖正式内容。
- 用户确认后才写入正式内容或产物。
- 图片理解人工编辑后的结果不应自动覆盖底层图片分析缓存。

### 3.3 来自 `WORKERS-REQUIREMENTS.md`

本轮必须落实以下规则：

- Workers 只执行单次无状态调用。
- Workers AI 接口只允许 AI 域服务身份调用。
- Workers 每次请求必须包含完整执行上下文。
- Workers 必须支持 SSE 流式响应。
- Workers 流式片段只是展示过程，不是业务提交事实。
- Workers 生成的文件在进入 Storage 前只是临时产物。

## 4. Closure Definition

本 RUNBOOK 中“关闭”指以下五项同时成立：

1. 页面有真实入口。
2. Java 侧有真实业务编排。
3. Workers 返回稳定最终态，Java 按最终态消费。
4. 用户可完成“发起 -> 查看过程/状态 -> 查看候选或失败 -> 接受/拒绝/重试 -> 正式结果刷新”。
5. 自动化测试覆盖关键规则，不以人工冒烟代替。

不满足以上任一项，对应需求项不能写成“已完成”。

## 5. Confirmed Decisions

### 5.1 只做三才图会视觉资产 AI 工作流

本轮不扩到其他 Classics 子域，也不扩到其他 AI 业务域。

### 5.2 只做四类 capability

本轮 capability 固定为：

- `image_analysis`
- `fusion`
- `visual`
- `image_gen`

### 5.3 候选区是唯一正式写回前置

除任务状态和技术台账外：

- `image_analysis` 结果不得直接写正式 `imageAnalysisMarkdown`
- `fusion` 结果不得直接写正式 `fusionDescription`
- `visual` 结果不得直接写正式 `visualDescription`
- `image_gen` 结果不得直接写正式 `generatedImageStorageObjectId`

都必须先进入候选区，再由用户确认后写正式事实。

### 5.4 流式输出只负责展示

`delta / progress / artifact` 只用于页面展示。

正式写回、artifact 转存、Storage 绑定、version 追加，必须以稳定最终态为准：

- SSE `completed`
- 同步 `SUCCEEDED / PARTIAL / FAILED`

### 5.5 批量取消只停止未开始单元

取消后：

- 已完成单元保留结果
- 正在执行单元允许自然结束
- 未开始单元不得继续派发

### 5.6 `image_gen` 默认新建 version

`image_gen` 候选应用成功后，默认新建 `visual asset version`，不直接覆盖已有生成图 version。

### 5.7 本轮不引入新的业务真相源

复用现有：

- `ai_task`
- `ai_candidate`
- `classics_sancai_visual_asset`
- `storage_object`

优先收紧语义，不新增并行事实表。

## 6. Data Structure Plan

本节只写本轮必须锁定的字段语义。若实现需要改字段、改 request/response、改 worker schema，必须以本节为准。

### 6.1 `classics_sancai_visual_asset`

本轮锁定以下字段语义：

- `source_image_storage_object_id`
  - 当前视觉资产 version 对应的原图正式 `StorageObject`
- `generated_image_storage_object_id`
  - 当前视觉资产 version 对应的 AI 生成图正式 `StorageObject`
- `text_weight`
  - `fusion` 与 `visual` 消费的文本权重
- `image_weight`
  - `fusion` 与 `visual` 消费的图片理解权重
- `image_analysis_markdown`
  - 只保存用户确认后的 `image_analysis` 正式结果
- `fusion_description`
  - 只保存用户确认后的 `fusion` 正式结果
- `visual_description`
  - 只保存用户确认后的 `visual` 正式结果
- `generation_params_json`
  - 保存本次 `image_gen` 正式生成参数快照
- `status`
  - 当前视觉资产 version 的业务状态
- `current_used`
  - 当前条目正在使用的视觉资产 version

本轮禁止：

- `image_analysis` 同时写 `fusion_description`
- `image_analysis` 同时写 `visual_description`
- `fusion` 写 `image_analysis_markdown`
- `visual` 写 `fusion_description`
- `generated_image_storage_object_id` 保存 artifact id

### 6.2 `ai_candidate`

本轮固定以下字段语义：

- `objectId`
  - 三才视觉资产候选固定绑定 `visualAssetId`
- `capability`
  - 只使用 `image_analysis / fusion / visual / image_gen`
- `resultPayload`
  - 保存候选结果；用户确认前不写正式业务字段
  - `image_analysis` 时固定为 Markdown 文本，用户接受后只写 `image_analysis_markdown`
  - `fusion` 时固定为融合说明文本，用户接受后只写 `fusion_description`
  - `visual` 时固定为视觉描述文本，用户接受后只写 `visual_description`
  - `image_gen` 时必须能解析出正式生成图 `storageObjectId`，用户接受后交由 Java 创建新 version，不允许把 artifact id 当正式图片 id
- `artifactReferenceJson`
  - 只保存 workers 临时产物引用
  - 仅 `image_gen` 可依赖该字段辅助转存；该字段本身不是业务正式事实
- `failureStage`
  - 候选失败时保留失败阶段，页面必须可读
- `errorType`
  - 候选失败类型
- `errorMessage`
  - 候选失败详情，页面直接展示时不得为空串占位

### 6.3 `ai_task`

本轮固定以下字段语义：

- `capability`
  - 只使用 `image_analysis / fusion / visual / image_gen`
- `status`
  - 页面统一消费 `PENDING / RUNNING / PARTIAL / SUCCEEDED / FAILED / CANCELLED`
- `objectId`
  - 单条任务固定绑定 `visualAssetId`
- `batchId`
  - 批量图片理解或批量视觉资产处理的聚合标识
- `callId`
  - 单次 AI 调用记录标识，前端轮询与错误定位使用
- `candidateId`
  - 当前调用写入的候选记录标识；只有候选生成成功时才有值
- `failureStage`
  - 页面必须能直接展示失败发生在请求校验、worker 执行、结果解析还是 artifact 转存阶段
- `errorType`
  - 页面必须能直接展示机器可分类错误类型
- `errorMessage`
  - 页面必须能直接展示用户可读错误原因

### 6.4 Admin API 字段

本轮需要稳定支持的视觉资产请求/响应字段：

- `visualAssetId`
- `entryId`
- `versionNo`
- `status`
- `currentUsed`
- `sourceImageStorageObjectId`
- `generatedImageStorageObjectId`
- `textWeight`
- `imageWeight`
- `imageAnalysisMarkdown`
- `fusionDescription`
- `visualDescription`
- `generationParamsJson`
- `sourcePreviewUrl`
- `generatedPreviewUrl`
- `sourceDownloadUrl`
- `generatedDownloadUrl`

### 6.5 Workers AI schema

本轮 workers 请求至少要稳定承载：

- `requestId`
- `traceId`
- `callerDomain`
- `operation`
- `stream`
- `input`
- `prompt`
- `variables`

本轮 workers 最终态至少要稳定返回：

- `status`
- `result`
- `warnings`
- `usage`
- `artifactReference`
- `failureStage`
- `fallbackUsed`
- `errorType`
- `errorMessage`

语义约束：

- `image_analysis / fusion / visual` 最终态必须返回 `result` 文本，不依赖 `artifactReference`
- `image_gen` 最终态必须返回 `artifactReference`，并能支持 Java 后续转存出正式 `storageObjectId`
- SSE `completed` 事件和同步响应在上述字段上必须同构，不能一套返回 `error`，另一套返回 `errorType / errorMessage`

## 7. Related Files

### 7.1 Admin Web

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-model.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-list.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/hooks/use-sancai-entry-panel-state.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-payload-editor.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/common/ai-refinement-task-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/ai-candidate-service.ts`

### 7.2 Java AI

- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/AiRefinementController.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/AiRefinementTaskController.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/request/AiRefinementRequests.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/response/AiRefinementResponses.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/assembler/AiRefinementInterfaceAssembler.java`

### 7.3 Java Classics

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/SancaiAssetApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/model/entity/SancaiVisualAsset.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiAssetRepository.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiAssetRepositoryImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/SancaiAssetAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiAssetRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiContentRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiAssetResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiAssetInterfaceAssembler.java`

### 7.4 Workers

- `kuzhambu-workers/src/kuzhambu_workers/api/ai_routes.py`
- `kuzhambu-workers/src/kuzhambu_workers/api/ai_usecase_routes.py`
- `kuzhambu-workers/src/kuzhambu_workers/ai/usecase_registry.py`
- `kuzhambu-workers/src/kuzhambu_workers/schemas/ai.py`
- `kuzhambu-workers/src/kuzhambu_workers/streaming/events.py`
- `kuzhambu-workers/src/kuzhambu_workers/streaming/sse.py`
- `kuzhambu-workers/src/kuzhambu_workers/render/artifact_store.py`

## 8. Work Packages

执行顺序必须严格按照 `WP1 -> WP2 -> WP3 -> WP4 -> WP5 -> WP6`。

理由：

- 先统一字段和入口，再补单条闭环。
- 单条闭环稳定后，再做候选治理。
- 候选治理稳定后，再做批量和取消。
- 最后补统一状态、回归测试和文档收口。

### WP1 字段语义与接口口径收紧

目标：

- 锁定视觉资产字段语义、API 字段语义和 workers 最终态口径。

#### T1 视觉资产 request/response 字段收紧

文件数：3

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiAssetRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiAssetResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiAssetInterfaceAssembler.java`

关闭标准：

- request/response 与本 RUNBOOK `6.4` 一致
- 不再混用字段语义

#### T2 领域对象与 repository 更新语义收紧

文件数：3

- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/model/entity/SancaiVisualAsset.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiAssetRepository.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiAssetRepositoryImpl.java`

关闭标准：

- `imageAnalysisMarkdown / fusionDescription / visualDescription` 分工固定
- repository 更新方法不再允许跨字段污染

#### T3 workers 最终态 schema 收紧

文件数：3

- `kuzhambu-workers/src/kuzhambu_workers/schemas/ai.py`
- `kuzhambu-workers/src/kuzhambu_workers/streaming/events.py`
- `kuzhambu-workers/src/kuzhambu_workers/api/ai_routes.py`

关闭标准：

- 最终态字段满足本 RUNBOOK `6.5`
- SSE 事件和同步响应口径一致

### WP2 单条视觉资产 AI 入口闭环

目标：

- 单条视觉资产可直接发起 `image_analysis / fusion / visual / image_gen`

#### T1 页面动作入口统一

文件数：4

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-model.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/hooks/use-sancai-entry-panel-state.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`

关闭标准：

- 四类能力都有真实入口
- 入口携带 `entryId + visualAssetId + capability`

#### T2 AI controller 请求口径补齐

文件数：3

- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/AiRefinementController.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/request/AiRefinementRequests.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/assembler/AiRefinementInterfaceAssembler.java`

关闭标准：

- 四类能力都可被 Java AI 入口稳定接收
- 请求体能带上视觉资产上下文

### WP3 单条正式闭环

目标：

- 补齐 `fusion` 与 `image_gen` 业务主链

#### T1 `fusion` 正式写回闭环

文件数：4

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/SancaiAssetApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiAssetRepositoryImpl.java`

关闭标准：

- `fusion` 只写 `fusionDescription`
- `fusion` 消费 `imageAnalysisMarkdown + textWeight + imageWeight`

#### T2 `image_gen` 正式 version 闭环

文件数：4

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiAssetRepository.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiAssetRepositoryImpl.java`
- `kuzhambu-workers/src/kuzhambu_workers/render/artifact_store.py`

关闭标准：

- `image_gen` 候选应用后转存正式 `StorageObject`
- 默认新建 visual asset version
- 不覆盖旧 version

### WP4 候选区完整治理

目标：

- 候选区从“可看”推进到“可治理”

#### T1 前端候选编辑与动作收紧

文件数：4

- `kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-payload-editor.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/hooks/use-sancai-entry-panel-state.ts`

关闭标准：

- 可预览、编辑、接受、拒绝
- 接受后刷新正式视觉资产事实

#### T2 后端候选应用规则锁定

文件数：3

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/response/AiRefinementResponses.java`

关闭标准：

- 四类 capability 候选应用规则固定
- 失败返回页面可读错误信息

### WP5 批量、取消、失败与重试

目标：

- 只做批量图片理解与批量视觉资产处理

#### T1 批量任务编排与取消

文件数：4

- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/AiRefinementTaskController.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/response/AiRefinementResponses.java`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-list.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`

关闭标准：

- 批量任务有 `batchId`
- 可取消未开始单元
- 已完成结果保留

#### T2 workers 批量最终态与失败口径

文件数：3

- `kuzhambu-workers/src/kuzhambu_workers/api/ai_usecase_routes.py`
- `kuzhambu-workers/src/kuzhambu_workers/schemas/ai.py`
- `kuzhambu-workers/src/kuzhambu_workers/streaming/sse.py`

关闭标准：

- 批量子单元失败不污染其他单元
- 最终态可映射成功数、失败数、失败原因

#### T3 页面失败与重试统一

文件数：3

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/hooks/use-sancai-entry-panel-state.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/ai-refinement-task-service.ts`

关闭标准：

- 页面可展示失败原因
- 页面可针对失败任务重试

### WP6 回归测试与文档收口

目标：

- 锁定规则，防止本轮闭环回退

#### T1 Admin Web 测试

文件数：3

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-service-contract.test.ts`

关闭标准：

- 覆盖单条入口、候选治理、批量状态、失败与重试

#### T2 Java 测试

文件数：4

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/SancaiAssetApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/SancaiAssetAdminControllerTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/test/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/AiInvocationControllerTest.java`

关闭标准：

- 覆盖字段写回规则、version 追加、批量取消、接口契约

#### T3 workers 测试

文件数：3

- `kuzhambu-workers/tests/test_ai_usecase_routes_classics.py`
- `kuzhambu-workers/tests/test_ai_usecase_registry.py`
- `kuzhambu-workers/tests/test_artifact_store.py`

关闭标准：

- 覆盖 `image_analysis / fusion / visual / image_gen`
- 覆盖 stream final-state
- 覆盖 artifact 元信息

## 9. Verification Strategy

每个 TODO 必须按以下顺序验证：

### Java

- `mvn -pl ... spotless:apply`
- `mvn spotless:check`
- `mvn checkstyle:check`
- `mvn -pl ... -am -DskipTests compile`
- `mvn -pl ... -am test`

### Apps

- `npm --workspace admin-web run format`
- `npm run format:check`
- `npm run lint`
- `npm run build`
- `npm run test`

### Workers

- `.venv/bin/python -m ruff format ...`
- `.venv/bin/python -m ruff format --check ...`
- `.venv/bin/python -m ruff check ...`
- `.venv/bin/python -m pytest -p no:capture ...`

## 10. Acceptance Checklist

本 RUNBOOK 关闭前必须同时满足：

- 三才条目页可直接发起 `image_analysis / fusion / visual / image_gen`
- `fusion` 真正消费图片理解结果与权重
- `image_gen` 真正形成正式 visual asset version
- 候选区支持预览、编辑、接受、拒绝
- 页面能看到失败原因并重试
- 批量图片理解与批量视觉资产处理支持取消，且已完成结果保留
- 页面能查看历史 version 并切换当前使用 version
- 自动化测试已锁定正式写回与 version 规则
- 本轮纳入范围的需求项在评审后可以按事实改成“已完成”

## 11. TODO Rule

本 RUNBOOK 通过评审后，再拆 `TODO.md`。

拆分原则：

- 一任务一提交
- 每个任务只覆盖一个小任务
- 每个任务文件范围控制在 2-5 个文件
- 每个任务都要能独立验证

## 12. Closure Rule

本 RUNBOOK 关闭前必须满足：

- 范围内需求项按事实关闭
- `TODO.md` 对应任务已删除
- 若仍存在缺口，必须缩 scope，不得用“部分完成”兜底
- 任务结束后删除本 RUNBOOK 及残留引用
