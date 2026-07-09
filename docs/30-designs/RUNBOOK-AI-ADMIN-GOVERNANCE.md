# RUNBOOK AI Admin Governance

## 目标

补齐管理后台 AI 治理闭环，让管理员能在后台完成 AI 服务配置、模型治理、能力映射、提示词版本、调用统计和功能动作状态检查。

完成后，`docs/10-requirements/AI-REQUIREMENTS.md` 中管理端治理类需求必须从“后端具备能力”推进到“管理员可通过后台页面完成配置、检查和追踪”。

## 边界

- 主范围：`kuzhambu-apps/admin-web/src/pages/ai/`、`kuzhambu-apps/admin-web/src/router/index.tsx`、`kuzhambu-apps/admin-web/src/layouts/admin-layout.tsx`。
- 菜单权限范围：`db/data-source/system.json`、`db/data/system.sql`、`scripts/generate-system-data-sql.ts`、`kuzhambu-servers/starter/kuzhambu-admin-starter/src/test/java/com/thundax/kuzhambu/starter/admin/AdminStarterArchitectureTest.java`。
- 后端补齐范围：仅在 `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/` 和必要的 `kuzhambu-servers/biz/ai/kuzhambu-ai-application/` 查询方法内补 admin 只读契约。
- 不做：不改 workers 协议，不让前端读取或展示明文 AI Key，不改 Classics/Discovery/Knowledge 正式内容写入流程，不新增第二套路由、请求或权限体系。

## 数据结构

### 数据库

本任务默认不新增数据库表，不修改 `db/schema/ai.sql` 字段。页面数据来自现有表：

- `ai_service_config`：`service_id`、`service_role`、`api_source`、`base_url`、`encrypted_api_key`、`enabled`、`status`、`last_checked_at`、`configured_at`。
- `ai_model`：`model_id`、`service_id`、`model_name`、`display_name`、`capability_tags_json`、`default_params_json`、`description`、`enabled`、`registered_at`。
- `ai_model_check_record`：`check_id`、`model_id`、`service_id`、`model_name`、`status`、`latency_ms`、`error_type`、`error_message`、`checked_at`。
- `ai_capability`：`capability`、`name`、`required_tags_json`、`output_mode`、`enabled`、`priority`。
- `ai_capability_mapping`：`mapping_id`、`scope`、`capability`、`model_id`、`enabled`、`configured_at`。
- `ai_prompt_template`：`template_id`、`scope`、`capability`、`name`、`description`、`status`、`current_version_no`、`registered_at`。
- `ai_prompt_version`：`prompt_version_id`、`template_id`、`version_no`、`message_templates_json`、`variables_snapshot_json`、`output_schema_json`、`current_key`、`change_summary`、`registered_at`。
- `ai_prompt_variable`：`variable_id`、`template_id`、`variable_name`、`required`、`description`、`priority`。
- `ai_action_status`：`action_status_id`、`scope`、`capability`、`available`、`unavailable_reason`、`checked_at`。
- `ai_call_record`：`call_id`、`batch_id`、`scope`、`capability`、`content_type`、`content_id`、`object_id`、`service_id`、`service_role`、`model_id`、`model_name`、`prompt_version_id`、`request_id`、`trace_id`、`status`、`stream_used`、`stream_completed`、`fallback_used`、`latency_ms`、`input_tokens`、`output_tokens`、`cost_amount`、`failure_stage`、`result_format`、`error_type`、`error_message`、`warnings_json`、`requested_at`、`completed_at`。

### 必须补齐的后端 Admin 契约

如现有接口无法支撑页面一次性读取，补以下 request/response。字段名必须与前端类型一致。

1. 调用记录分页

- API：`POST /api/ai/invocation/call/page`
- 权限：`ai:invocation:view`
- 返回：`Page<CallRecordResponse>`，分页结构沿用 `PageResponse` 或本模块现有分页响应；不得自定义第二套分页字段。

- 文件：`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/request/AiInvocationRequests.java`
- 新增 `CallRecordPageRequest` 字段：
  - `scope: String`
  - `capability: String`
  - `contentType: String`
  - `contentId: Long`
  - `status: String`
  - `serviceRole: String`
  - `modelName: String`
  - `fallbackUsed: Boolean`
  - `requestedAtStart: Instant`
  - `requestedAtEnd: Instant`
  - `pageNo: Integer`
  - `pageSize: Integer`
- 文件：`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/response/AiInvocationResponses.java`
- 复用或新增 `CallRecordResponse` 字段必须包含：
  - `callId: Long`
  - `batchId: Long`
  - `scope: String`
  - `capability: String`
  - `contentType: String`
  - `contentId: Long`
  - `objectId: Long`
  - `serviceRole: String`
  - `modelId: Long`
  - `modelName: String`
  - `promptVersionId: Long`
  - `requestId: String`
  - `traceId: String`
  - `status: String`
  - `streamUsed: Boolean`
  - `streamCompleted: Boolean`
  - `fallbackUsed: Boolean`
  - `latencyMs: Integer`
  - `inputTokens: Integer`
  - `outputTokens: Integer`
  - `costAmount: BigDecimal`
  - `failureStage: String`
  - `resultFormat: String`
  - `errorType: String`
  - `errorMessage: String`
  - `warningsJson: String`
  - `requestedAt: Instant`
  - `completedAt: Instant`

2. 调用统计 Summary

- API：`POST /api/ai/invocation/call/summary`
- 权限：`ai:invocation:view`
- 返回：`CallSummaryResponse`

- 文件：`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/request/AiInvocationRequests.java`
- 新增 `CallSummaryRequest` 字段：
  - `periodStart: Instant`
  - `periodEnd: Instant`
  - `bucketType: String`
  - `scope: String`
  - `capability: String`
  - `serviceRole: String`
- 文件：`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/response/AiInvocationResponses.java`
- 新增 `CallSummaryResponse` 字段：
  - `periodStart: Instant`
  - `periodEnd: Instant`
  - `invocationCount: Long`
  - `succeededInvocationCount: Long`
  - `failedInvocationCount: Long`
  - `avgLatencyMs: Long`
  - `totalCostAmount: BigDecimal`
  - `topCapabilities: List<TopCapabilityResponse>`
- 新增 `TopCapabilityResponse` 字段：
  - `capability: String`
  - `invocationCount: Long`

3. 动作状态批量读取

- API：`POST /api/ai/config/action/status/list`
- 权限：`ai:config:view`
- 返回：`List<ActionStatusResponse>`

- 文件：`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/config/controller/request/AiConfigRequests.java`
- 新增 `ActionStatusListRequest` 字段：
  - `scope: String`
  - `capability: String`
  - `available: Boolean`
- 文件：`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/config/controller/response/AiConfigResponses.java`
- `ActionStatusResponse` 字段必须保持：
  - `scope: String`
  - `capability: String`
  - `available: Boolean`
  - `unavailableReason: String`
  - `checkedAt: Instant`

### 前端类型字段

前端 `*-types.ts` 必须使用页面边界内的业务类型，字段与后端 JSON 字段同名：

- `AiServiceConfigRecord`：`serviceId`、`serviceRole`、`apiSource`、`baseUrl`、`apiKeyConfigured`、`enabled`、`status`、`lastCheckedAt`、`configuredAt`。
- `AiModelRecord`：`modelId`、`serviceId`、`modelName`、`displayName`、`capabilityTags`、`defaultParamsJson`、`description`、`enabled`、`registeredAt`。
- `AiModelCheckRecord`：`checkId`、`modelId`、`serviceId`、`modelName`、`status`、`latencyMs`、`errorType`、`errorMessage`、`checkedAt`。
- `AiCapabilityRecord`：`capability`、`name`、`requiredTags`、`outputMode`、`enabled`、`priority`。
- `AiCapabilityMappingRecord`：`mappingId`、`scope`、`capability`、`modelId`、`enabled`、`configuredAt`。
- `AiPromptTemplateRecord`：`templateId`、`scope`、`capability`、`name`、`description`、`status`、`currentVersionNo`、`registeredAt`。
- `AiPromptVersionRecord`：`promptVersionId`、`templateId`、`versionNo`、`messageTemplatesJson`、`variablesSnapshotJson`、`outputSchemaJson`、`current`、`changeSummary`、`registeredAt`。
- `AiPromptVariableRecord`：`variableId`、`templateId`、`variableName`、`required`、`description`、`priority`。
- `AiCallSummaryRecord`：`periodStart`、`periodEnd`、`invocationCount`、`succeededInvocationCount`、`failedInvocationCount`、`avgLatencyMs`、`totalCostAmount`、`topCapabilities`。
- `AiTopCapabilityRecord`：`capability`、`invocationCount`。
- `AiCallRecord`：`callId`、`batchId`、`scope`、`capability`、`contentType`、`contentId`、`objectId`、`serviceRole`、`modelId`、`modelName`、`promptVersionId`、`requestId`、`traceId`、`status`、`streamUsed`、`streamCompleted`、`fallbackUsed`、`latencyMs`、`inputTokens`、`outputTokens`、`costAmount`、`failureStage`、`resultFormat`、`errorType`、`errorMessage`、`warningsJson`、`requestedAt`、`completedAt`。
- `AiActionStatusRecord`：`scope`、`capability`、`available`、`unavailableReason`、`checkedAt`。

## 页面规格

所有页面遵守 `docs/00-governance/ADMIN-WEB-RULES.md`：页面文件为 `src/pages/<module>/<domain>/<domain>-page.tsx`，同目录放 `<domain>-service.ts`、`<domain>-types.ts`、`<domain>-page.css` 和测试。

### `/ai/services`

页面文件：

- `kuzhambu-apps/admin-web/src/pages/ai/services/services-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/ai/services/services-service.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/services/services-types.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/services/services-page.css`
- `kuzhambu-apps/admin-web/src/pages/ai/services/services-page.test.tsx`

控件和操作：

- 顶部 `刷新` 图标按钮：重新加载主服务和备用服务。
- 两张配置卡：`PRIMARY` 主服务、`BACKUP` 备用服务。
- 每张卡展示字段：`serviceRole`、`apiSource`、`baseUrl`、`apiKeyConfigured`、`enabled`、`status`、`lastCheckedAt`、`configuredAt`。
- 每张卡操作：`编辑` 打开 Drawer；`启用/停用` 在 Drawer 内保存 `enabled`。
- Drawer 表单控件：
  - `serviceRole`：只读文本。
  - `apiSource`：Select。
  - `baseUrl`：Input。
  - `encryptedApiKey`：Password Input，留空表示不更新密钥。
  - `enabled`：Switch。
  - `status`：Select，仅允许保存后端已支持状态。
  - 底部按钮：`取消`、`保存`。
- 权限：无 `ai:config:edit` 时隐藏或禁用保存类操作；仍可按 `ai:config:view` 只读查看。

### `/ai/models`

页面文件：

- `kuzhambu-apps/admin-web/src/pages/ai/models/models-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/ai/models/models-service.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/models/models-types.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/models/models-page.css`
- `kuzhambu-apps/admin-web/src/pages/ai/models/models-page.test.tsx`

控件和操作：

- 筛选区：服务 Select、启用状态 Select、模型名称 Input.Search。
- 主按钮：`新增模型` 打开 Drawer。
- 表格列：`displayName`、`modelName`、`serviceId`、`capabilityTags`、`enabled`、`registeredAt`、`actions`。
- 行操作：
  - `编辑`：打开 Drawer。
  - `检测`：调用真实检测接口；若后端没有真实执行接口，本任务先补接口，不让前端手工伪造检测结果。
  - `检测历史`：打开历史 Drawer。
  - `启用/禁用`：保存 `enabled`。
  - `删除`：二次确认；后端阻止已被映射使用的模型并返回原因。
- 模型 Drawer 控件：
  - `serviceId`：Select，选项来自服务配置。
  - `modelName`：Input。
  - `displayName`：Input。
  - `capabilityTags`：Select mode `tags`。
  - `defaultParamsJson`：TextArea，保存前前端校验 JSON 格式。
  - `description`：TextArea。
  - `enabled`：Switch。
  - 底部按钮：`取消`、`保存`。
- 检测历史 Drawer 表格列：`status`、`latencyMs`、`errorType`、`errorMessage`、`checkedAt`。

### `/ai/capability-mappings`

页面文件：

- `kuzhambu-apps/admin-web/src/pages/ai/capability-mappings/capability-mappings-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/ai/capability-mappings/capability-mappings-service.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/capability-mappings/capability-mappings-types.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/capability-mappings/capability-mappings-page.css`
- `kuzhambu-apps/admin-web/src/pages/ai/capability-mappings/capability-mappings-page.test.tsx`

控件和操作：

- 筛选区：`scope` Select、`capability` Select、`enabled` Select。
- 表格列：`scope`、`capability`、`capabilityName`、`requiredTags`、`outputMode`、`modelName`、`enabled`、`configuredAt`、`actions`。
- 行操作：`配置模型` 打开 Drawer；`启用/禁用` 保存映射。
- Drawer 控件：
  - `scope`：Select。
  - `capability`：Select，展示 `name`、`requiredTags`、`outputMode`。
  - `modelId`：Select，仅展示 `enabled = true` 的模型；选项文本为 `displayName / modelName`。
  - `enabled`：Switch。
  - 必须展示模型能力标签和能力所需标签的匹配提示。
  - 底部按钮：`取消`、`保存`。
- 保存失败时直接展示后端返回原因，不在前端绕过后端校验。

### `/ai/prompts`

页面文件：

- `kuzhambu-apps/admin-web/src/pages/ai/prompts/prompts-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/ai/prompts/prompts-service.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/prompts/prompts-types.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/prompts/prompts-page.css`
- `kuzhambu-apps/admin-web/src/pages/ai/prompts/prompts-page.test.tsx`

控件和操作：

- 筛选区：`scope` Select、`capability` Select、`查询`按钮、`重置`按钮。
- 左侧模板信息区展示：`templateId`、`name`、`description`、`status`、`currentVersionNo`、`registeredAt`。
- 中部编辑区：
  - `name` Input。
  - `description` TextArea。
  - `messageTemplatesJson` TextArea 或 JSON 编辑区。
  - `outputSchemaJson` TextArea。
  - `changeSummary` Input。
  - `变量预览`表格，字段：`variableName`、`required`、`description`、`priority`。
  - 操作：`校验变量`、`保存新版本`。
- 右侧版本区：
  - 版本列表表格字段：`versionNo`、`current`、`changeSummary`、`registeredAt`。
  - 行操作：`查看`、`对比`、`回滚`。
  - `对比` Drawer 展示左右版本的 `messageTemplatesJson`、`variablesSnapshotJson`、`outputSchemaJson`、`changeSummary`。
  - `回滚` 必须二次确认，成功后刷新当前版本、变量列表和动作状态。
- 优化建议：
  - `生成优化建议`按钮调用 `/api/ai/platform/prompt-suggestion`。
  - 建议结果只进入预览 Drawer。
  - Drawer 操作：`放弃`、`应用为新版本`。

### `/ai/invocations`

页面文件：

- `kuzhambu-apps/admin-web/src/pages/ai/invocations/invocations-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/ai/invocations/invocations-service.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/invocations/invocations-types.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/invocations/invocations-page.css`
- `kuzhambu-apps/admin-web/src/pages/ai/invocations/invocations-page.test.tsx`

控件和操作：

- Summary 筛选区：周期 RangePicker、`bucketType` Select、`scope` Select、`capability` Select、`serviceRole` Select、`刷新`按钮。
- 指标卡字段：`invocationCount`、`succeededInvocationCount`、`failedInvocationCount`、`avgLatencyMs`、`totalCostAmount`。
- 能力排行表字段：`capability`、`invocationCount`。
- 调用记录筛选区：`status` Select、`contentType` Input、`contentId` InputNumber、`modelName` Input、`fallbackUsed` Select、`requestedAt` RangePicker、`查询`、`重置`。
- 调用记录表字段：`callId`、`scope`、`capability`、`contentType`、`contentId`、`serviceRole`、`modelName`、`status`、`fallbackUsed`、`latencyMs`、`costAmount`、`requestedAt`、`actions`。
- 行操作：`详情` Drawer。
- 详情 Drawer 展示：`requestId`、`traceId`、`promptVersionId`、`streamUsed`、`streamCompleted`、`inputTokens`、`outputTokens`、`failureStage`、`resultFormat`、`errorType`、`errorMessage`、格式化后的 `warningsJson`。

### `/ai/action-status`

页面文件：

- `kuzhambu-apps/admin-web/src/pages/ai/action-status/action-status-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/ai/action-status/action-status-service.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/action-status/action-status-types.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/action-status/action-status-page.css`
- `kuzhambu-apps/admin-web/src/pages/ai/action-status/action-status-page.test.tsx`

控件和操作：

- 筛选区：`scope` Select、`capability` Select、`available` Select、`查询`、`重置`、`刷新全部`。
- 表格列：`scope`、`capability`、`available`、`unavailableReason`、`checkedAt`、`actions`。
- 行操作：`刷新状态`，调用单项刷新接口。
- `available = false` 时用警告状态展示，不可用原因必须完整可读。
- 无 `ai:config:edit` 时禁用 `刷新状态` 和 `刷新全部`。

## 文件级拆分

每个小任务必须控制在 2-5 个文件，完成一个小任务后运行对应最小验证并检查 `git diff`。

### Task 1：后端调用记录查询能力

文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/repository/AiInvocationRepository.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/invocation/persistence/mapper/AiInvocationMapper.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/invocation/repository/impl/AiInvocationRepositoryImpl.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/test/java/com/thundax/kuzhambu/ai/infra/invocation/repository/impl/AiInvocationRepositoryIT.java`

要求：支持按 `scope`、`capability`、`contentType`、`contentId`、`status`、`serviceRole`、`modelName`、`fallbackUsed`、`requestedAtStart`、`requestedAtEnd` 查询调用记录；支持 summary 聚合读取。不要新增表字段。

### Task 2：后端调用记录 Admin 接口

文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/AiInvocationController.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/request/AiInvocationRequests.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/response/AiInvocationResponses.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/assembler/AiInvocationInterfaceAssembler.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/test/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/AiInvocationControllerTest.java`

要求：新增分页和 summary admin 接口；权限为 `ai:invocation:view`；不得复用 Operations dashboard response。

### Task 3：后端动作状态批量读取能力

文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/capability/repository/AiCapabilityRepository.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/capability/persistence/mapper/AiCapabilityMapper.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/capability/repository/impl/AiCapabilityRepositoryImpl.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/test/java/com/thundax/kuzhambu/ai/infra/capability/repository/impl/AiCapabilityRepositoryIT.java`

要求：支持按 `scope`、`capability`、`available` 读取 `ai_action_status` 列表；不要新增表字段。

### Task 4：后端动作状态 Admin 接口

文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/config/controller/AiConfigController.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/config/controller/request/AiConfigRequests.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/config/controller/response/AiConfigResponses.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/config/assembler/AiConfigInterfaceAssembler.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/test/java/com/thundax/kuzhambu/ai/interfaces/admin/config/controller/AiConfigControllerTest.java`

要求：支持按 `scope`、`capability`、`available` 批量查询动作状态；单项刷新接口继续保留。

### Task 5：菜单、路由和图标

文件：

- `db/data-source/system.json`
- `db/data/system.sql`
- `kuzhambu-apps/admin-web/src/router/index.tsx`
- `kuzhambu-apps/admin-web/src/layouts/admin-layout.tsx`
- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/test/java/com/thundax/kuzhambu/starter/admin/AdminStarterArchitectureTest.java`

要求：

- `system.json` 是源，运行 `node scripts/generate-system-data-sql.ts` 生成 `db/data/system.sql`。
- AI 一级菜单保留 `AI 管理`；子菜单改为 6 个治理页面：`服务配置`、`模型配置`、`能力映射`、`提示词版本`、`调用统计`、`动作状态`。
- URL 分别为 `/ai/services`、`/ai/models`、`/ai/capability-mappings`、`/ai/prompts`、`/ai/invocations`、`/ai/action-status`。
- `admin-layout` 增加使用到的 icon key，不允许菜单显示 `!`。

### Task 6：服务配置页

文件：

- `kuzhambu-apps/admin-web/src/pages/ai/services/services-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/ai/services/services-service.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/services/services-types.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/services/services-page.css`
- `kuzhambu-apps/admin-web/src/pages/ai/services/services-page.test.tsx`

要求：完成主备服务配置展示和 Drawer 保存闭环。

### Task 7：模型配置页

文件：

- `kuzhambu-apps/admin-web/src/pages/ai/models/models-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/ai/models/models-service.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/models/models-types.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/models/models-page.css`
- `kuzhambu-apps/admin-web/src/pages/ai/models/models-page.test.tsx`

要求：完成模型列表、编辑、启停、删除、检测和检测历史。

### Task 8：能力映射页

文件：

- `kuzhambu-apps/admin-web/src/pages/ai/capability-mappings/capability-mappings-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/ai/capability-mappings/capability-mappings-service.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/capability-mappings/capability-mappings-types.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/capability-mappings/capability-mappings-page.css`
- `kuzhambu-apps/admin-web/src/pages/ai/capability-mappings/capability-mappings-page.test.tsx`

要求：完成 `scope + capability -> modelId` 映射配置和能力标签匹配提示。

### Task 9：提示词版本页

文件：

- `kuzhambu-apps/admin-web/src/pages/ai/prompts/prompts-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/ai/prompts/prompts-service.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/prompts/prompts-types.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/prompts/prompts-page.css`
- `kuzhambu-apps/admin-web/src/pages/ai/prompts/prompts-page.test.tsx`

要求：完成模板保存、变量校验、版本列表、版本对比、回滚和优化建议确认应用。

### Task 10：调用统计页

文件：

- `kuzhambu-apps/admin-web/src/pages/ai/invocations/invocations-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/ai/invocations/invocations-service.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/invocations/invocations-types.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/invocations/invocations-page.css`
- `kuzhambu-apps/admin-web/src/pages/ai/invocations/invocations-page.test.tsx`

要求：完成 summary、排行、调用记录分页和详情 Drawer。

### Task 11：动作状态页

文件：

- `kuzhambu-apps/admin-web/src/pages/ai/action-status/action-status-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/ai/action-status/action-status-service.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/action-status/action-status-types.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/action-status/action-status-page.css`
- `kuzhambu-apps/admin-web/src/pages/ai/action-status/action-status-page.test.tsx`

要求：完成动作状态矩阵、不可用原因展示、单项刷新和刷新全部。

## 验证

菜单 SQL：

```sh
node scripts/generate-system-data-sql.ts
node scripts/generate-system-data-sql.ts --check
```

后端：

```sh
cd kuzhambu-servers
mvn -pl biz/ai/kuzhambu-ai-interface -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/ai/kuzhambu-ai-interface -am test
mvn -pl starter/kuzhambu-admin-starter -am test
```

前端：

```sh
cd kuzhambu-apps
pnpm --filter kuzhambu-admin-web run format
pnpm run format:check
pnpm run lint
pnpm test
pnpm run build
```

人工验收：

- 管理员能看到 AI 一级菜单和 6 个治理页面，点击每个菜单能进入对应 URL。
- 主服务和备用服务能分别保存；页面只展示密钥是否配置，不展示明文 Key。
- 模型能新增、编辑、启停、删除、检测并查看检测历史；删除被映射使用的模型时展示后端原因。
- 能力映射只能绑定启用模型；能力标签不匹配时保存失败并展示原因。
- 提示词能保存新版本、查看变量、对比版本、回滚版本、生成优化建议并确认应用。
- 提示词保存或回滚后，动作状态能刷新并展示受影响能力是否可用。
- 调用统计能按周期展示调用数、成功数、失败数、平均耗时、成本和能力排行。
- 调用记录支持筛选、分页和详情查看。
- 动作状态页展示每个 `scope + capability` 的可用状态、不可用原因和刷新入口。

## 收口

- 本 RUNBOOK 是临时执行手册，任务关闭后删除。
- 若新增接口、字段或开发流程成为稳定契约，同步更新 `docs/20-interfaces/` 或 `docs/00-governance/`。
- 提交前检查 `git diff`，只保留本任务相关文件。
