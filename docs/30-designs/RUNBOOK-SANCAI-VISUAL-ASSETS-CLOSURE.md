# 三才图会视觉资产闭环 RUNBOOK

## 1. 目的

本 RUNBOOK 用于收口“三才图会视觉资产闭环”。

本轮执行边界已经确认：只做“方案 A”。

本轮目标不是从零设计视觉资产体系，而是在现有 `SancaiVisualAsset` 数据模型和 `SancaiAssetApplicationService` 能力基础上，补齐：

- Admin API 对视觉资产的对外接口。
- Admin Web 对视觉资产的查看、切换、基础编辑入口。
- 三才条目上下文中的视觉资产最小闭环。

本轮明确不做：

- AI 图片理解、融合描述、视觉描述、生图的真实业务编排闭环。
- 批量视觉资产处理。
- 新的数据库表或字段。

本 RUNBOOK 聚焦当前已存在的三才图会视觉资产数据结构与代码入口，不扩展到新的顶层模块，也不引入新的前端技术栈。

## 2. 当前现状

### 2.1 已有能力

- 数据库已存在 `classics_sancai_visual_asset` 表，覆盖视觉资产版本、原图、生成图、文本/图片权重、图片理解结果、融合描述、视觉描述和生图参数快照。
- Java application 已具备：
  - `updateVisualAsset(SancaiVisualAsset visualAsset)`
  - `useVisualAsset(SancaiEntryId entryId, SancaiVisualAssetId visualAssetId)`
  - `listVisualAssets(SancaiEntryId entryId)`
- `SancaiEntryModel` 已具备原图上传、当前图预览、当前图下载入口，但只针对 `classics_sancai_entry_image`，未覆盖视觉资产历史版本。

### 2.2 缺口

- `SancaiAssetAdminController` 当前只暴露 `drafts`、`images`、`showcases`，没有 `visual-assets` 相关路由。
- `SancaiAssetRequest` / `SancaiAssetResponse` 还不足以完整表达视觉资产对象。
- admin-web `sancai-entry-service.ts`、`sancai-types.ts`、`sancai-entry-panel.tsx` 还没有视觉资产契约和页面区块。
- 当前三才条目弹窗只接了译文/摘要精修、导出和静态展示，没有视觉资产历史列表、当前版本切换、原图与生成图区分展示入口。
- `sancai-service-contract.test.ts` 还没有视觉资产请求路径断言。
- `SancaiAssetAdminControllerTest` 还没有视觉资产列表、保存、切换路由断言。

## 3. 范围

### 3.1 本次纳入范围

- 三才图会视觉资产 Admin API 暴露。
- 三才图会视觉资产 Admin Web 数据契约与页面集成。
- 三才图会条目详情弹窗中的视觉资产最小闭环。
- 视觉资产历史列表与当前使用版本切换。
- 前端对原图与视觉资产图的区分展示。
- 视觉资产基础字段的查看与保存：
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

### 3.2 本次不纳入范围

- 多选条目批量视觉资产处理。
- 复杂缩略图生成服务。
- Portal Web 的视觉资产管理能力。
- 新增数据库表。
- 真实 AI 图片理解、融合描述、视觉描述、生图调用链路。
- 全新异步任务体系。

## 4. 数据结构变更

### 4.1 数据库

本轮默认不新增表，不新增字段，复用已有表：

文件：

- `db/schema/classics.sql`

复用表：

- `classics_sancai_visual_asset`

已有字段：

- `id` `bigint`
- `entry_id` `bigint`
- `version_no` `int`
- `status` `varchar(16)`
- `source_image_storage_object_id` `bigint`
- `generated_image_storage_object_id` `bigint`
- `current_used` `tinyint(1)`
- `text_weight` `int`
- `image_weight` `int`
- `image_analysis_markdown` `longtext`
- `fusion_description` `longtext`
- `visual_description` `longtext`
- `generation_params_json` `json`

判断：

- 当前数据库结构已经能支撑“历史版本列表 + 当前使用切换 + 原图/生成图区分展示 + 中间字段存取”。
- 本轮不改 `db/schema/classics.sql`。
- 若实现时发现 `status` 枚举不足，视为下一轮需求，不在本 RUNBOOK 范围内扩表或改枚举。

### 4.2 Java domain / infra

现有视觉资产领域对象：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/model/entity/SancaiVisualAsset.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/dataobject/SancaiVisualAssetDO.java`

本轮需要确保以下字段在 interface 层完整透传：

- `id: Long`
- `entryId: Long`
- `versionNo: Integer`
- `status: String`
- `sourceImageStorageObjectId: Long`
- `generatedImageStorageObjectId: Long`
- `currentUsed: Boolean`
- `textWeight: Integer`
- `imageWeight: Integer`
- `imageAnalysisMarkdown: String`
- `fusionDescription: String`
- `visualDescription: String`
- `generationParamsJson: String`

### 4.3 Java interface request/response

当前文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiAssetRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiAssetResponse.java`

#### 请求字段新增

`SancaiAssetRequest` 需要新增或补齐以下字段：

- `visualAssetId: Long`
- `versionNo: Integer`
- `sourceImageStorageObjectId: Long`
- `generatedImageStorageObjectId: Long`
- `textWeight: Integer`
- `imageWeight: Integer`
- `imageAnalysisMarkdown: String`
- `fusionDescription: String`
- `visualDescription: String`
- `generationParamsJson: String`

说明：

- `id` 当前已存在，但在视觉资产语义中容易与 `imageId` / `showcaseId` 混用。
- 本轮建议同时增加 `visualAssetId`，并在视觉资产路由中优先使用 `visualAssetId`，减少接口复用时的歧义。

#### 响应字段新增

`SancaiAssetResponse` 需要新增以下字段：

- `visualAssetId: Long`
- `versionNo: Integer`
- `sourceImageStorageObjectId: Long`
- `generatedImageStorageObjectId: Long`
- `textWeight: Integer`
- `imageWeight: Integer`
- `imageAnalysisMarkdown: String`
- `fusionDescription: String`
- `visualDescription: String`
- `generationParamsJson: String`

建议补充字段：

- `generatedPreviewUrl: String`
- `generatedDownloadUrl: String`
- `sourcePreviewUrl: String`
- `sourceDownloadUrl: String`

说明：

- 本轮推荐由后端直接返回原图和生成图资源 URL，减少 admin-web 拼接和判空复杂度。
- 若最终仍复用前端拼接方式，则至少需要保证 `sourceImageStorageObjectId`、`generatedImageStorageObjectId` 和稳定读取路径同时存在。

### 4.4 Admin Web types / service

当前文件：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`

本轮需要新增前端类型：

- `SancaiVisualAssetRecord`

建议字段：

- `id: number`
- `entryId?: number | null`
- `versionNo?: number | null`
- `status?: string | null`
- `sourceImageStorageObjectId?: number | null`
- `generatedImageStorageObjectId?: number | null`
- `currentUsed?: boolean | null`
- `textWeight?: number | null`
- `imageWeight?: number | null`
- `imageAnalysisMarkdown?: string | null`
- `fusionDescription?: string | null`
- `visualDescription?: string | null`
- `generationParamsJson?: string | null`
- `sourcePreviewUrl?: string | null`
- `sourceDownloadUrl?: string | null`
- `generatedPreviewUrl?: string | null`
- `generatedDownloadUrl?: string | null`

本轮需要新增前端 service 入参：

- `SancaiVisualAssetUpdateCommand`
- `SancaiVisualAssetUseCommand`

建议字段：

- `SancaiVisualAssetUpdateCommand`
  - `id?: number | null`
  - `entryId: number`
  - `versionNo?: number | null`
  - `status?: string | null`
  - `sourceImageStorageObjectId?: number | null`
  - `generatedImageStorageObjectId?: number | null`
  - `currentUsed?: boolean | null`
  - `textWeight?: number | null`
  - `imageWeight?: number | null`
  - `imageAnalysisMarkdown?: string | null`
  - `fusionDescription?: string | null`
  - `visualDescription?: string | null`
  - `generationParamsJson?: string | null`
- `SancaiVisualAssetUseCommand`
  - `entryId: number`
  - `visualAssetId: number`

本轮需要新增前端 service 方法：

- `listVisualAssets(entryId: number)`
- `updateVisualAsset(command: SancaiVisualAssetUpdateCommand)`
- `useVisualAsset(command: SancaiVisualAssetUseCommand)`

建议固定请求路径：

- `GET /classics/sancai/assets/visual-assets/{entryId}`
- `POST /classics/sancai/assets/visual-assets/update`
- `POST /classics/sancai/assets/visual-assets/use`

### 4.5 页面展示结构

本轮在三才条目详情弹窗中新增“视觉资产”区块，默认结构固定为：

- 当前使用视觉资产摘要
  - `versionNo`
  - `status`
  - `currentUsed`
  - 原图预览 / 下载
  - 生成图预览 / 下载
- 视觉资产历史列表
  - `versionNo`
  - `status`
  - `textWeight`
  - `imageWeight`
  - `currentUsed`
  - 操作：`设为当前使用`
- 视觉资产字段查看或基础编辑区
  - `imageAnalysisMarkdown`
  - `fusionDescription`
  - `visualDescription`
  - `generationParamsJson`

本轮不新增独立页面，只放在：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-model.tsx`

## 5. 相关文件

### 5.1 后端

- `db/schema/classics.sql`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/model/entity/SancaiVisualAsset.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiAssetRepository.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/SancaiAssetApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/SancaiAssetAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiAssetRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiAssetResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiAssetInterfaceAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/SancaiAssetAdminControllerTest.java`

### 5.2 前端

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-model.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-service-contract.test.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.test.tsx`

### 5.3 文档

- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`

## 6. 分任务执行

以下任务按 2-5 个文件一组拆分，避免出现难以验收的大块提交。

### 任务 A：补齐后端视觉资产 Admin 契约

目标：

- 让视觉资产列表、保存、当前使用切换具备正式 Admin API。

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiAssetRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiAssetResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiAssetInterfaceAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/SancaiAssetAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/SancaiAssetAdminControllerTest.java`

验收点：

- 新增以下固定路由：
  - `GET /api/classics/sancai/assets/visual-assets/{entryId}`
  - `POST /api/classics/sancai/assets/visual-assets/update`
  - `POST /api/classics/sancai/assets/visual-assets/use`
- request / response 能完整表达视觉资产字段。
- controller test 覆盖新增路由和字段映射。

### 任务 B：补齐前端视觉资产契约与 service

目标：

- 让 admin-web 能调用视觉资产接口，并拥有稳定 TypeScript 类型。

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-service-contract.test.ts`

验收点：

- 存在 `SancaiVisualAssetRecord`。
- service 存在以下固定方法：
  - `listVisualAssets`
  - `updateVisualAsset`
  - `useVisualAsset`
- service contract test 固定请求路径与请求体。

### 任务 C：三才条目弹窗接入视觉资产区块

目标：

- 在条目详情弹窗中展示视觉资产历史、当前使用版本、原图与生成图入口。

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-model.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`

验收点：

- 打开三才条目时可看到视觉资产列表。
- 当前使用版本有明确标识。
- 可执行“设为当前使用版本”。
- 可查看原图与生成图的独立预览/下载入口。
- 测试覆盖视觉资产区块展示与切换回调。
- 不新建独立视觉资产页面。

### 任务 D：前端契约与文档收口

目标：

- 固定视觉资产字段展示/保存契约，并同步覆盖文档口径。

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-model.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`
- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`

验收点：

- `SancaiVisualAssetRecord` 与页面展示字段保持一致。
- 条目弹窗对视觉资产字段的展示和编辑契约稳定。
- 覆盖文档同步更新“已完成/未完成”口径。

说明：

- 若任务 C 已经同时改动 `sancai-types.ts`，则任务 D 只保留对字段口径的复查和文档收口，不重复扩大实现范围。

## 7. 验证建议

后端：

- `cd kuzhambu-servers && mvn -pl ... spotless:apply`
- `cd kuzhambu-servers && mvn -q spotless:check`
- `cd kuzhambu-servers && mvn -q checkstyle:check`
- `cd kuzhambu-servers && mvn -q test`

前端：

- `cd kuzhambu-apps && npm --workspace admin-web run format`
- `cd kuzhambu-apps && npm run format:check`
- `cd kuzhambu-apps && npm run lint`
- `cd kuzhambu-apps && npm test`

文档：

- 检查 `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md` 是否同步最新闭环状态。

不需要执行的验证：

- 本轮不新增 workers 业务实现，因此不把 workers 作为必跑模块。

## 8. 收口规则

- 任务关闭后，应删除本 RUNBOOK，或将剩余未完成范围收窄到新的 RUNBOOK。
- 若实现过程中新增了稳定接口、固定校验规则或统一工作流，应把稳定规则沉淀到治理文档，而不是长期保留在 RUNBOOK 中。

## 9. 默认实现顺序

1. 先做任务 A，确认后端接口和 response 字段稳定。
2. 再做任务 B，固定 admin-web service 契约与请求路径。
3. 然后做任务 C，把视觉资产区块挂到三才条目弹窗。
4. 最后做任务 D，收口字段口径、测试和覆盖文档。
