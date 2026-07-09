# RUNBOOK AI Runtime Acceptance

## 目标

完成 AI 治理运行时验收闭环，形成可审阅的真实运行证据。验收范围固定为：

- AI 后端契约：服务配置、模型检测、能力映射、提示词变量校验、动作状态刷新、调用统计。
- Admin AI 页面测试：`/ai/services`、`/ai/models`、`/ai/capability-mappings`、`/ai/prompts`、`/ai/invocations`、`/ai/action-status`。
- `dev.env` 冒烟证据：本地 admin API、admin-web、workers 和 AI 模型服务使用同一组运行配置完成一次真实 `classics + summary` 调用闭环。

## 验收边界

本 RUNBOOK 只验收运行时闭环，不新增功能、不改 schema、不补业务页面能力。

通过标准：

- PRIMARY 服务必须完成真实模型检测；BACKUP 本轮只验收配置读写、启用和禁用状态，不强制验证真实降级。
- `classics + summary` 必须绑定到启用文本模型，并完成一次真实非流式 AI 精修任务。
- 能力标签不匹配负例必须使用缺少目标 capability tag 的测试模型构造。
- 提示词变量校验必须同时保留一次完整变量成功证据和一次缺失必填变量失败证据。
- 至少一次 AI 能力调用写入 `ai_call_record`，并可在 `/ai/invocations` 看到同一 `callId` 的统计和详情。
- Java AI 域必须通过 `WorkerAiClient` 调用 workers；前端不得直连 workers，不得伪造调用记录。
- 冒烟证据后续固定沉淀到 `docs/40-readiness/AI-RUNTIME-SMOKE-EVIDENCE.md`；本 RUNBOOK 任务关闭时删除。

## 依据

- `docs/10-requirements/AI-REQUIREMENTS.md`
- `docs/30-designs/AI-DESIGN.md`
- `docs/20-interfaces/WORKERS-AI-INTERFACE.md`
- `docs/20-interfaces/WORKERS-AI-USECASE-INTERFACE.md`
- `docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`
- `docs/00-governance/HOW-TO-ADMIN-LOGIN-SMOKE.md`

## 精确数据结构

本轮不允许新增或修改表字段。验收只核对现有字段是否被真实运行写入。

### 服务配置

表：`ai_service_config`

代码文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/model/persistence/dataobject/AiServiceConfigDO.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/config/controller/AiConfigController.java`
- `kuzhambu-apps/admin-web/src/pages/ai/services/services-service.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/services/services-page.tsx`

字段核对：

| 字段 | 验收要求 |
| --- | --- |
| `serviceId` | PRIMARY 和 BACKUP 读取响应必须有稳定值 |
| `serviceRole` | 必须分别为 `PRIMARY`、`BACKUP` |
| `apiSource` | 与 Admin 表单保存值一致 |
| `baseUrl` | 与 Admin 表单保存值一致 |
| `encryptedApiKey` | 可写入数据库，但不得出现在前端明文、日志证据或 readiness 证据 |
| `enabled` | PRIMARY 必须为 `true`；BACKUP 验收启用和禁用状态 |
| `status` | 保存后可读取；真实检测后状态应反映可用或不可用 |
| `lastCheckedAt` | PRIMARY 模型检测后应更新或能通过检测记录证明已检查 |
| `configuredAt` | 保存配置后应有配置时间 |

### 模型和检测历史

表：`ai_model`、`ai_model_check_record`

代码文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/model/persistence/dataobject/AiModelDO.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/model/persistence/dataobject/AiModelCheckRecordDO.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/model/command/AiModelCheckCommand.java`
- `kuzhambu-apps/admin-web/src/pages/ai/model-configs/model-configs-service.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/model-configs/model-configs-page.tsx`

字段核对：

| 表 | 字段 | 验收要求 |
| --- | --- | --- |
| `ai_model` | `modelId` | 能作为能力映射的 `modelId` 使用 |
| `ai_model` | `serviceId` | 指向 PRIMARY 服务 |
| `ai_model` | `modelName` | 与真实模型服务模型名一致 |
| `ai_model` | `displayName` | Admin 列表可读 |
| `ai_model` | `capabilityTagsJson` | 正例包含文本能力标签；负例缺少目标 capability tag |
| `ai_model` | `defaultParamsJson` | 保存后读取一致 |
| `ai_model` | `description` | 保存后读取一致 |
| `ai_model` | `enabled` | 正例模型必须为 `true` |
| `ai_model` | `registeredAt` | 新增模型后有登记时间 |
| `ai_model_check_record` | `checkId` | 每次检测生成记录 |
| `ai_model_check_record` | `modelId` | 等于本次检测模型 |
| `ai_model_check_record` | `serviceId` | 等于模型所属服务 |
| `ai_model_check_record` | `modelName` | 等于本次检测模型名 |
| `ai_model_check_record` | `status` | 成功或失败状态可读 |
| `ai_model_check_record` | `latencyMs` | 成功检测应有耗时 |
| `ai_model_check_record` | `errorType`、`errorMessage` | 失败检测必须有明确原因 |
| `ai_model_check_record` | `checkedAt` | 最新检测时间可追溯 |

### 能力映射和动作状态

表：`ai_capability_mapping`、`ai_action_status`

代码文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/capability/persistence/dataobject/AiCapabilityMappingDO.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/capability/persistence/dataobject/AiActionStatusDO.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/capability/command/AiCapabilityMappingSaveCommand.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/capability/result/AiActionStatusResult.java`
- `kuzhambu-apps/admin-web/src/pages/ai/capability-mappings/capability-mappings-service.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/capability-mappings/capability-mappings-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/ai/action-status/action-status-service.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/action-status/action-status-page.tsx`

字段核对：

| 表 | 字段 | 验收要求 |
| --- | --- | --- |
| `ai_capability_mapping` | `mappingId` | 保存后可在列表读取 |
| `ai_capability_mapping` | `scope` | 固定验收 `classics` |
| `ai_capability_mapping` | `capability` | 固定验收 `summary` |
| `ai_capability_mapping` | `modelId` | 等于正例启用文本模型 |
| `ai_capability_mapping` | `enabled` | 正例必须为 `true` |
| `ai_capability_mapping` | `configuredAt` | 保存后有配置时间 |
| `ai_action_status` | `actionStatusId` | 刷新后可读取 |
| `ai_action_status` | `scope` | 固定验收 `classics` |
| `ai_action_status` | `capability` | 固定验收 `summary` |
| `ai_action_status` | `available` | 正例刷新后为 `true`；缺配置时可为 `false` |
| `ai_action_status` | `unavailableReason` | 不可用时必须有明确原因 |
| `ai_action_status` | `checkedAt` | 刷新后更新时间 |

### 提示词

表：`ai_prompt_template`、`ai_prompt_version`、`ai_prompt_variable`

代码文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/prompt/persistence/dataobject/PromptTemplateDO.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/prompt/persistence/dataobject/PromptVersionDO.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/prompt/persistence/dataobject/PromptVariableDO.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/prompt/command/PromptTemplateSaveCommand.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/prompt/result/PromptVersionResult.java`
- `kuzhambu-apps/admin-web/src/pages/ai/prompts/prompts-service.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/prompts/prompts-page.tsx`

字段核对：

| 表 | 字段 | 验收要求 |
| --- | --- | --- |
| `ai_prompt_template` | `templateId` | 当前模板稳定可读 |
| `ai_prompt_template` | `scope` | 固定验收 `classics` |
| `ai_prompt_template` | `capability` | 固定验收 `summary` |
| `ai_prompt_template` | `name`、`description` | Admin 表单保存后读取一致 |
| `ai_prompt_template` | `status` | 当前模板可用 |
| `ai_prompt_template` | `currentVersionNo` | 与当前版本接口一致 |
| `ai_prompt_template` | `registeredAt` | 新建或既有模板可追溯 |
| `ai_prompt_version` | `promptVersionId` | 调用记录必须引用该值 |
| `ai_prompt_version` | `templateId` | 等于当前模板 |
| `ai_prompt_version` | `versionNo` | 保存新版本后递增或可追溯 |
| `ai_prompt_version` | `messageTemplatesJson` | 保存渲染模板；证据中只保留脱敏摘要 |
| `ai_prompt_version` | `variablesSnapshotJson` | 与变量列表一致 |
| `ai_prompt_version` | `outputSchemaJson` | 与 summary 输出要求一致 |
| `ai_prompt_version` | `currentKey` | 当前版本唯一标识可核对 |
| `ai_prompt_version` | `changeSummary` | 本次修改说明可读 |
| `ai_prompt_version` | `registeredAt` | 版本登记时间可追溯 |
| `ai_prompt_variable` | `variableId` | 变量列表可读 |
| `ai_prompt_variable` | `templateId` | 等于当前模板 |
| `ai_prompt_variable` | `variableName` | 至少包含本次 summary prompt 的必填变量 |
| `ai_prompt_variable` | `required` | 缺失校验必须命中 `true` 的变量 |
| `ai_prompt_variable` | `description` | Admin 变量表可读 |
| `ai_prompt_variable` | `priority` | Admin 变量表排序稳定 |

### 调用记录、候选和任务

表：`ai_refinement_task`、`ai_call_record`、`ai_candidate`

代码文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/refinement/persistence/dataobject/AiRefinementTaskDO.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/invocation/persistence/dataobject/AiCallRecordDO.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/invocation/persistence/dataobject/AiCandidateDO.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/command/AiRefinementRequestCommand.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/result/AiCandidateResult.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/command/AiInvokeCommand.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/result/AiInvokeResult.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/AiRefinementTaskController.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/AiInvocationController.java`
- `kuzhambu-apps/admin-web/src/pages/classics/common/ai-refinement-task-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/ai-candidate-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/ai/invocations/invocations-service.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/invocations/invocations-page.tsx`

字段核对：

| 表 | 字段 | 验收要求 |
| --- | --- | --- |
| `ai_refinement_task` | `taskId` | `task/add` 返回并可用 `task/get` 查询 |
| `ai_refinement_task` | `scope` | 固定验收 `classics` |
| `ai_refinement_task` | `capability` | 固定验收 `summary` |
| `ai_refinement_task` | `contentType`、`contentId`、`objectId` | 指向本次 Classics 内容快照 |
| `ai_refinement_task` | `requestedBy` | 等于登录用户 |
| `ai_refinement_task` | `requestId`、`traceId` | 与调用记录一致 |
| `ai_refinement_task` | `status` | 最终为 `SUCCEEDED`、`FAILED` 或 `PARTIAL` |
| `ai_refinement_task` | `serviceRole`、`modelId`、`modelName` | 与 PRIMARY 正例模型一致 |
| `ai_refinement_task` | `promptVersionId` | 等于当前 summary 提示词版本 |
| `ai_refinement_task` | `callId` | 成功或失败终态必须可追溯到 `ai_call_record` |
| `ai_refinement_task` | `candidateId` | 成功且产生候选时必须可追溯到 `ai_candidate` |
| `ai_refinement_task` | `failureStage`、`errorType`、`errorMessage` | 失败或部分失败时必须有明确值 |
| `ai_refinement_task` | `resultFormat`、`resultPreview` | 成功时可在任务详情展示 |
| `ai_refinement_task` | `streamEnabled` | summary 非流式，预期为 `false` |
| `ai_refinement_task` | `requestedAt`、`startedAt`、`completedAt`、`cancelledAt` | 按任务生命周期写入 |
| `ai_call_record` | `callId` | 与任务和调用详情一致 |
| `ai_call_record` | `scope`、`capability` | 固定为 `classics`、`summary` |
| `ai_call_record` | `contentType`、`contentId`、`objectId` | 与任务一致 |
| `ai_call_record` | `serviceId`、`serviceRole`、`modelId`、`modelName` | 与 PRIMARY 正例模型一致 |
| `ai_call_record` | `promptVersionId` | 与任务一致 |
| `ai_call_record` | `requestId`、`traceId` | 与任务一致 |
| `ai_call_record` | `status` | 最终状态与任务终态一致 |
| `ai_call_record` | `streamUsed`、`streamCompleted` | summary 非流式，预期为 `false` 或空值符合实现口径 |
| `ai_call_record` | `fallbackUsed` | 本轮不测降级，预期为 `false` |
| `ai_call_record` | `latencyMs` | 成功或失败终态应有耗时 |
| `ai_call_record` | `inputTokens`、`outputTokens`、`costAmount` | 模型返回 usage 时必须写入 |
| `ai_call_record` | `failureStage`、`errorType`、`errorMessage` | 失败时必须写入 |
| `ai_call_record` | `resultFormat`、`resultPayload` | 成功时必须写入 summary 结果 |
| `ai_call_record` | `artifactReferenceJson` | summary 文本能力预期为空 |
| `ai_call_record` | `warningsJson` | 有 warning 时写入 |
| `ai_call_record` | `requestedAt`、`completedAt` | 调用生命周期时间可追溯 |
| `ai_candidate` | `candidateId` | 成功产生候选时与任务一致 |
| `ai_candidate` | `callId` | 等于调用记录 |
| `ai_candidate` | `capability`、`contentType`、`contentId`、`objectId` | 与任务一致 |
| `ai_candidate` | `resultFormat`、`resultPayload` | summary 候选结果可预览 |
| `ai_candidate` | `status` | 新候选为待处理状态；拒绝或应用后状态变化可追溯 |
| `ai_candidate` | `promptVersionId`、`modelName` | 与调用记录一致 |
| `ai_candidate` | `failureStage`、`errorType`、`errorMessage` | 候选失败时必须写入 |
| `ai_candidate` | `requestedAt`、`appliedAt`、`rejectedAt` | 候选生命周期可追溯 |

## 文件级任务拆分

大任务必须拆为 2-5 个文件范围的小任务执行和审核。每个小任务独立产出证据，不跨范围顺手修改。

### 任务 1：后端配置和模型契约

范围文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/config/controller/AiConfigController.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/model/command/AiModelCheckCommand.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/model/persistence/mapper/AiModelMapper.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/model/persistence/dataobject/AiServiceConfigDO.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/model/persistence/dataobject/AiModelCheckRecordDO.java`

验收动作：

- 调用 `POST /ai/config/service/get-by-role` 分别读取 PRIMARY、BACKUP。
- 调用 `POST /ai/config/service/save` 保存 PRIMARY 和 BACKUP 配置。
- 调用 `POST /ai/config/model/create` 或 `POST /ai/config/model/update` 保存正例文本模型。
- 调用 `POST /ai/config/model/check` 检测 PRIMARY 模型。
- 调用 `POST /ai/config/model/check-records` 读取最新检测记录。

通过标准：

- 响应和数据库字段满足“服务配置”“模型和检测历史”两节要求。
- 证据不包含明文 API Key。

### 任务 2：后端能力映射、提示词和动作状态

范围文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/capability/command/AiCapabilityMappingSaveCommand.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/prompt/command/PromptTemplateSaveCommand.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/capability/persistence/mapper/AiCapabilityMapper.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/prompt/persistence/mapper/PromptMapper.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/prompt/controller/PromptController.java`

验收动作：

- 调用 `POST /ai/config/capability/mapping/save` 保存 `scope = classics`、`capability = summary`、`modelId = 正例模型`。
- 用缺少目标 capability tag 的测试模型调用同一接口，确认保存失败。
- 调用 `POST /ai/prompt/template/save` 保存 summary 模板和变量。
- 调用 `POST /ai/prompt/variable/validate` 传完整变量，确认成功。
- 调用 `POST /ai/prompt/variable/validate` 缺失一个 `required = true` 变量，确认失败。
- 调用 `POST /ai/config/action/status/refresh` 刷新 `classics + summary`。

通过标准：

- 响应和数据库字段满足“能力映射和动作状态”“提示词”两节要求。
- 失败响应必须能让 Admin 页面展示明确错误文案。

### 任务 3：后端调用闭环和统计

范围文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/AiRefinementTaskController.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/AiInvocationController.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/command/AiInvokeCommand.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/result/AiInvokeResult.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/invocation/persistence/mapper/AiInvocationMapper.java`

验收动作：

- 用 Admin Classics 页面或 `POST /ai/refinement/task/add` 创建 `classics + summary` 任务。
- 用 `POST /ai/refinement/task/get` 轮询终态。
- 用 `POST /ai/invocation/call/summary` 查询时间窗口统计。
- 用 `POST /ai/invocation/call/page` 查询本次 `callId`。
- 数据库核对 `ai_refinement_task`、`ai_call_record`、`ai_candidate`。

通过标准：

- 响应和数据库字段满足“调用记录、候选和任务”一节要求。
- `ai_call_record.callId`、`ai_refinement_task.callId`、`ai_candidate.callId` 可互相追溯。

### 任务 4：Admin AI 页面

范围文件：

- `kuzhambu-apps/admin-web/src/pages/ai/services/services-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/ai/model-configs/model-configs-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/ai/capability-mappings/capability-mappings-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/ai/prompts/prompts-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/ai/action-status/action-status-page.tsx`

验收动作：

- 按“Admin 前端控件验收”执行 5 个治理页面。
- DevTools Network 记录每个页面对应 service 文件里的 API 路径。
- 页面截图必须能看到筛选条件、表格结果、弹窗或抽屉、操作反馈。

通过标准：

- 页面控件操作能触发对应 API。
- 表单校验、保存成功、保存失败、刷新、筛选和详情展示都有证据。

### 任务 5：Admin Classics 精修和调用统计页面

范围文件：

- `kuzhambu-apps/admin-web/src/pages/classics/common/ai-refinement-task-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/ai-candidate-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/ai/invocations/invocations-page.tsx`

验收动作：

- 在三才图会条目详情触发 summary 精修。
- 观察任务状态从受理进入终态。
- 成功时打开候选面板查看 summary 候选。
- 打开 `/ai/invocations` 查询本次调用统计和详情。

通过标准：

- 前端任务状态、候选结果、调用统计都能用同一 `taskId`、`candidateId`、`callId` 串起来。

## Admin 前端控件验收

### `/ai/services`

文件：

- `kuzhambu-apps/admin-web/src/pages/ai/services/services-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/ai/services/services-service.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/services/services-types.ts`

控件和操作：

| 控件 | 操作 | 预期 |
| --- | --- | --- |
| PRIMARY 服务卡片 | 点击刷新按钮 | 调用 `/ai/config/service/get-by-role`，卡片显示 `apiSource`、`baseUrl`、`enabled`、`status` |
| BACKUP 服务卡片 | 点击刷新按钮 | 调用 `/ai/config/service/get-by-role`，可看到启用或禁用状态 |
| 编辑按钮 | 打开编辑弹窗 | 表单回填 `serviceRole`、`apiSource`、`baseUrl`、`enabled`、`status` |
| API Key 输入框 | 输入或留空 | 页面不得显示既有明文 API Key |
| enabled 开关 | 切换启用状态 | 保存 payload 中 `enabled` 与开关一致 |
| 保存按钮 | 保存 PRIMARY 或 BACKUP | 调用 `/ai/config/service/save`，成功后卡片刷新 |

### `/ai/models`

文件：

- `kuzhambu-apps/admin-web/src/pages/ai/model-configs/model-configs-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/ai/model-configs/model-configs-service.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/model-configs/model-configs-types.ts`

控件和操作：

| 控件 | 操作 | 预期 |
| --- | --- | --- |
| 服务筛选下拉框 | 选择 PRIMARY 服务 | 调用 `/ai/config/model/list` 并按 `serviceId` 展示 |
| enabled 筛选 | 选择启用模型 | 列表只展示可用于映射的模型 |
| 新增模型按钮 | 打开模型表单 | 表单包含 `serviceId`、`modelName`、`displayName`、`capabilityTags`、`defaultParamsJson`、`description`、`enabled` |
| capabilityTags 控件 | 输入正例文本标签 | 保存后列表显示标签 |
| capabilityTags 控件 | 输入负例缺失标签 | 模型可保存，但映射负例必须失败 |
| 检测按钮 | 对 PRIMARY 正例模型执行检测 | 调用 `/ai/config/model/check` 并展示状态、耗时或失败原因 |
| 检测历史按钮 | 打开历史列表 | 调用 `/ai/config/model/check-records` 并展示最新记录 |
| 删除按钮 | 删除仍被映射使用的模型 | 必须失败并提示先修改映射 |

### `/ai/capability-mappings`

文件：

- `kuzhambu-apps/admin-web/src/pages/ai/capability-mappings/capability-mappings-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/ai/capability-mappings/capability-mappings-service.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/capability-mappings/capability-mappings-types.ts`

控件和操作：

| 控件 | 操作 | 预期 |
| --- | --- | --- |
| scope 输入或下拉 | 填写 `classics` | 查询条件进入 `/ai/config/capability/mapping/list` |
| capability 下拉 | 选择 `summary` | 显示 summary 能力 |
| enabled 筛选 | 选择启用 | 只显示启用映射 |
| 新增或编辑按钮 | 打开映射表单 | 表单包含 `scope`、`capability`、`modelId`、`enabled` |
| modelId 下拉 | 选择正例文本模型 | 保存调用 `/ai/config/capability/mapping/save` 成功 |
| modelId 下拉 | 选择负例缺标签模型 | 保存失败，页面展示后端错误 |
| 表格刷新 | 重新查询 | 返回 `classics + summary + 正例 modelId` |

### `/ai/prompts`

文件：

- `kuzhambu-apps/admin-web/src/pages/ai/prompts/prompts-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/ai/prompts/prompts-service.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/prompts/prompts-types.ts`

控件和操作：

| 控件 | 操作 | 预期 |
| --- | --- | --- |
| scope 输入框 | 填写 `classics` | 查询条件固定到本次验收 scope |
| capability 下拉 | 选择 `summary` | 调用 `/ai/prompt/template/get-by-scope` |
| 模板信息表单 | 填写 `name`、`description`、`status` | 保存后读取一致 |
| messageTemplatesJson 编辑器 | 输入 summary prompt 模板 | 调用 `/ai/prompt/template/save`，证据只保留脱敏摘要 |
| variables 配置表 | 新增至少一个 `required = true` 变量 | 保存后 `/ai/prompt/variable/list` 可读取 |
| 变量校验按钮 | 提交完整 `providedNames` | `/ai/prompt/variable/validate` 返回成功 |
| 变量校验按钮 | 去掉一个必填变量 | `/ai/prompt/variable/validate` 返回失败并展示变量名 |
| 版本列表 | 点击当前版本 | `/ai/prompt/version/current` 与列表当前版本一致 |
| 版本对比按钮 | 选择两个版本 | `/ai/prompt/version/compare` 返回对比数据 |
| 回滚按钮 | 回滚到旧版本 | `/ai/prompt/version/rollback` 后当前版本变化 |
| 优化建议按钮 | 生成建议 | `/ai/prompt/optimization/suggest` 返回建议，不能自动覆盖当前版本 |
| 动作状态区域 | 查看当前动作状态 | `/ai/config/action/status` 返回 `available` 和原因 |

### `/ai/action-status`

文件：

- `kuzhambu-apps/admin-web/src/pages/ai/action-status/action-status-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/ai/action-status/action-status-service.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/action-status/action-status-types.ts`

控件和操作：

| 控件 | 操作 | 预期 |
| --- | --- | --- |
| scope 筛选 | 填写 `classics` | 列表请求包含 scope |
| capability 下拉 | 选择 `summary` | 列表请求包含 capability |
| available 筛选 | 选择可用或不可用 | 表格按可用状态过滤 |
| 单条刷新按钮 | 刷新 `classics + summary` | 调用 `/ai/config/action/status/refresh` |
| 批量或列表刷新按钮 | 刷新列表 | 调用 `/ai/config/action/status/list` |
| 表格不可用原因列 | 查看失败原因 | `available = false` 时展示 `unavailableReason` |

### Classics 精修入口

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/common/ai-refinement-task-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-panel.tsx`

控件和操作：

| 控件 | 操作 | 预期 |
| --- | --- | --- |
| 三才条目列表 | 选择一条有正文的条目 | 条目详情可操作 AI 精修 |
| summary 精修按钮 | 点击创建任务 | 调用 `/ai/refinement/task/add`，返回 `taskId` |
| 任务状态区域 | 等待或手动刷新 | 调用 `/ai/refinement/task/get`，状态进入终态 |
| 候选面板 | 打开 summary 候选 | 调用 `/ai/invocation/candidate/list` 并展示候选 |
| 重试按钮 | 在失败或可重试状态点击 | 再次创建任务，保留用户输入和失败原因 |

### `/ai/invocations`

文件：

- `kuzhambu-apps/admin-web/src/pages/ai/invocations/invocations-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/ai/invocations/invocations-service.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/invocations/invocations-types.ts`

控件和操作：

| 控件 | 操作 | 预期 |
| --- | --- | --- |
| 时间范围控件 | 选择本次冒烟窗口 | summary 和 page 请求使用同一时间窗口 |
| capability 下拉 | 选择 `summary` | 统计和列表聚焦本次能力 |
| serviceRole 下拉 | 选择 `PRIMARY` | 列表显示 PRIMARY 调用 |
| 查询按钮 | 查询统计 | 调用 `/ai/invocation/call/summary` |
| 能力排行表 | 查看 top capabilities | 出现 `summary` 调用次数 |
| 调用记录表 | 查询分页 | 调用 `/ai/invocation/call/page`，出现本次 `callId` |
| 详情按钮 | 打开调用详情 | 展示 `promptVersionId`、`modelName`、`latencyMs`、`failureStage`、`fallbackUsed`、`resultFormat` |

## 环境准备

从仓库根目录准备本地运行环境：

```sh
cp .env.example dev.env
```

`dev.env` 必须包含并实际被 admin starter 进程加载：

```sh
KUZHAMBU_AI_WORKER_BASE_URL=http://127.0.0.1:8000
KUZHAMBU_AI_WORKER_INTERNAL_SECRET=change-me
KUZHAMBU_AI_WORKER_SERVICE_NAME=kuzhambu-ai
KUZHAMBU_AI_WORKER_TIMEOUT_MS=60000
```

真实模型服务配置不写入本文档。验收人应在 Admin `AI / 服务配置` 页面写入 PRIMARY 或 BACKUP 的 `baseUrl`、`apiSource`、API Key 和启用状态，并保存截图或 HTTP 响应作为证据。

## 启动

启动 workers：

```sh
cd kuzhambu-workers
.venv/bin/python -m pip install -e '.[dev]'
.venv/bin/python -m uvicorn kuzhambu_workers.main:app --host 127.0.0.1 --port 8000
```

启动 admin API：

```sh
set -a
source dev.env
set +a
cd kuzhambu-servers
mvn -pl starter/kuzhambu-admin-starter -am -DskipTests install
cd starter/kuzhambu-admin-starter
mvn spring-boot:run
```

启动 admin-web：

```sh
cd kuzhambu-apps
pnpm install
pnpm --filter kuzhambu-admin-web dev
```

登录方式按 `docs/00-governance/HOW-TO-ADMIN-LOGIN-SMOKE.md` 执行。本地验证码使用 `6666`，优先使用 `developer` / `Q1w2e3r$`。

## 后端契约验收

以下接口全部以 admin API 前缀 `/kuzhambu-admin-api/api` 调用，并携带 `Access-Token`。

| 验收项 | 接口 | 证据 |
| --- | --- | --- |
| PRIMARY/BACKUP 服务配置可读取 | `POST /ai/config/service/get-by-role` | 响应包含 `serviceRole`、`apiSource`、`baseUrl`、`enabled`、`status`，不包含明文 API Key |
| 服务配置可保存 | `POST /ai/config/service/save` | 保存后重新读取返回相同角色配置 |
| 模型列表可读取 | `POST /ai/config/model/list` | 至少一个启用模型可用于能力映射 |
| 模型检测可执行 | `POST /ai/config/model/check` | 返回检测状态、耗时或失败原因 |
| 模型检测历史可读取 | `POST /ai/config/model/check-records` | 最新检测记录与本次检测模型一致 |
| 能力清单可读取 | `POST /ai/config/capability/list` | 包含 `summary` |
| 能力映射可保存 | `POST /ai/config/capability/mapping/save` | `classics + summary` 绑定到启用模型 |
| 能力映射可读取 | `POST /ai/config/capability/mapping/list` | 返回本次保存的 mapping |
| 提示词模板可读取或保存 | `POST /ai/prompt/template/get-by-scope`、`POST /ai/prompt/template/save` | 当前 `classics + summary` 有当前版本 |
| 提示词变量可校验 | `POST /ai/prompt/variable/validate` | 完整变量返回成功；缺失必填变量返回明确失败 |
| 提示词版本可查看 | `POST /ai/prompt/version/current`、`POST /ai/prompt/version/list` | 当前版本与版本列表一致 |
| 动作状态可刷新 | `POST /ai/config/action/status/refresh` | 返回 `available` 和不可用原因 |
| 动作状态可查询 | `POST /ai/config/action/status/list` | 刷新后的状态在列表中可见 |
| 精修任务可创建和查询 | `POST /ai/refinement/task/add`、`POST /ai/refinement/task/get` | 任务进入明确终态 |
| 调用统计可读取 | `POST /ai/invocation/call/summary` | 返回 `invocationCount`、成功数、失败数、平均耗时、成本和 top capabilities |
| 调用记录可分页 | `POST /ai/invocation/call/page` | 最新调用记录包含模型、提示词版本、状态、失败阶段和降级标记 |

本轮不强制构造主备降级运行证据；如单独验收降级，必须新增独立步骤记录 PRIMARY 故障、BACKUP 成功、`fallbackUsed = true` 和管理端降级状态。

## 真实调用冒烟

固定选择 `classics + summary` 完成低成本非流式文本能力闭环。

1. 在 `/ai/services` 保存并启用可用 PRIMARY 服务。
2. 在 `/ai/models` 登记并启用一个文本模型，执行模型检测。
3. 在 `/ai/capability-mappings` 为 `classics + summary` 绑定该模型。
4. 在 `/ai/prompts` 为同一 `scope + capability` 保存当前提示词版本，并确认变量校验通过。
5. 在 `/ai/action-status` 刷新同一动作，确认 `available = true`。
6. 从 Classics 三才图会条目详情触发 summary 精修任务，或用 `POST /ai/refinement/task/add` 创建任务。
7. 用 `POST /ai/refinement/task/get` 轮询到 `SUCCEEDED`、`FAILED` 或 `PARTIAL` 终态。
8. 在 `/ai/invocations` 查询同一时间窗口，确认调用统计和调用详情出现本次记录。

最终证据必须包含：

- `dev.env` 中 AI worker 变量的脱敏截图或摘录。
- workers `/internal/health` 响应。
- admin API 登录成功响应中的 token 字段脱敏摘录。
- 服务配置读取响应，API Key 必须脱敏或不可见。
- 模型检测响应和检测历史响应。
- 能力映射保存响应和映射列表响应。
- 提示词当前版本、变量列表、变量校验成功和变量缺失失败响应。
- 动作状态刷新响应。
- 精修任务终态响应，包含 `taskId`、`status`、`callId`、`candidateId` 或失败字段。
- 调用统计响应和调用分页中对应 `callId` 的记录。

## 数据库核对

冒烟完成后，用 `dev.env` 连接本地数据库核对 AI 表事实：

```sh
set -a
source dev.env
set +a

mysql --protocol=TCP \
  -h"$MYSQL_HOST" \
  -P"$MYSQL_PORT" \
  -u"$MYSQL_USER" \
  -p"$MYSQL_PASSWORD" \
  "$MYSQL_DATABASE" \
  -e "
SELECT service_id, service_role, api_source, base_url, enabled, status, last_checked_at, configured_at
FROM ai_service_config
ORDER BY service_id;

SELECT model_id, service_id, model_name, display_name, capability_tags_json, enabled, registered_at
FROM ai_model
ORDER BY model_id DESC
LIMIT 10;

SELECT check_id, model_id, service_id, model_name, status, latency_ms, error_type, error_message, checked_at
FROM ai_model_check_record
ORDER BY check_id DESC
LIMIT 10;

SELECT mapping_id, scope, capability, model_id, enabled, configured_at
FROM ai_capability_mapping
WHERE scope = 'classics' AND capability = 'summary'
ORDER BY mapping_id DESC
LIMIT 10;

SELECT template_id, scope, capability, name, status, current_version_no, registered_at
FROM ai_prompt_template
WHERE scope = 'classics' AND capability = 'summary'
ORDER BY template_id DESC
LIMIT 10;

SELECT prompt_version_id, template_id, version_no, current_key, change_summary, registered_at
FROM ai_prompt_version
ORDER BY prompt_version_id DESC
LIMIT 10;

SELECT variable_id, template_id, variable_name, required, description, priority
FROM ai_prompt_variable
ORDER BY variable_id DESC
LIMIT 20;

SELECT action_status_id, scope, capability, available, unavailable_reason, checked_at
FROM ai_action_status
WHERE scope = 'classics' AND capability = 'summary'
ORDER BY action_status_id DESC
LIMIT 10;

SELECT task_id, scope, capability, content_type, content_id, object_id, status,
       service_role, model_id, model_name, prompt_version_id, call_id, candidate_id,
       failure_stage, error_type, error_message, result_format, stream_enabled,
       requested_at, started_at, completed_at
FROM ai_refinement_task
WHERE scope = 'classics' AND capability = 'summary'
ORDER BY task_id DESC
LIMIT 10;

SELECT call_id, scope, capability, content_type, content_id, object_id, status,
       service_role, model_id, model_name, prompt_version_id, request_id, trace_id,
       stream_used, stream_completed, fallback_used, latency_ms, input_tokens,
       output_tokens, cost_amount, failure_stage, result_format, error_type,
       error_message, requested_at, completed_at
FROM ai_call_record
WHERE scope = 'classics' AND capability = 'summary'
ORDER BY call_id DESC
LIMIT 10;

SELECT candidate_id, call_id, capability, content_type, content_id, object_id,
       result_format, status, prompt_version_id, model_name, failure_stage,
       error_type, error_message, requested_at, applied_at, rejected_at
FROM ai_candidate
WHERE capability = 'summary'
ORDER BY candidate_id DESC
LIMIT 10;
"
```

核对标准：

- 上述查询能覆盖本次验收涉及的全部字段。
- `ai_refinement_task.call_id`、`ai_call_record.call_id`、`ai_candidate.call_id` 可互相追溯。
- API Key、完整 prompt 和完整业务输入不得出现在日志截图或证据文档中。

## 自动化验证

验收前先执行窄集验证锁定当前 RUNBOOK 对应影响面；PR 前再执行 servers、apps 和 workers 全量验证。

窄集验证：

```sh
cd kuzhambu-servers
mvn -pl biz/ai/kuzhambu-ai-interface,biz/ai/kuzhambu-ai-application,biz/ai/kuzhambu-ai-infra -am test
```

```sh
cd kuzhambu-apps
pnpm --filter kuzhambu-admin-web run test -- --maxWorkers=1
pnpm --filter kuzhambu-admin-web run build
```

```sh
cd kuzhambu-workers
.venv/bin/python -m pytest -p no:capture tests/test_ai_routes.py tests/test_graph_registry.py
```

PR 前全量验证：

```sh
cd kuzhambu-servers
mvn spotless:check
mvn checkstyle:check
mvn test
```

```sh
cd kuzhambu-apps
pnpm run format:check
pnpm run lint
pnpm run test
pnpm run build
```

```sh
cd kuzhambu-workers
.venv/bin/python -m ruff format --check .
.venv/bin/python -m ruff check .
.venv/bin/python -m pytest -p no:capture
```

本 RUNBOOK 自身关闭前不要求把以上命令结果写入稳定治理文档；PR 描述应记录实际执行命令和结果。

## 关闭条件

满足以下条件后，删除本 RUNBOOK，并在 `docs/40-readiness/AI-RUNTIME-SMOKE-EVIDENCE.md` 与 PR 描述中保留验收摘要：

- 5 个文件级任务全部完成。
- 后端契约验收表全部完成。
- Admin 前端控件验收全部完成。
- 真实调用冒烟形成完整证据链。
- 自动化验证通过，或未通过项已明确归因并有后续任务。
- 没有未脱敏 API Key、token、完整 prompt 或业务敏感输入进入仓库。
