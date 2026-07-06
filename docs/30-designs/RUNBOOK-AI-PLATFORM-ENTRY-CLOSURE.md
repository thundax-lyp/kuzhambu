# RUNBOOK AI Platform Entry Closure

## 目标

关闭 `docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md` 中 Platform 能力入口缺口：Java AI 域为 workers 已注册的 `PLATFORM_PROMPT_SUGGESTION` 与 `PLATFORM_VERSION_SUMMARY` 提供可调用入口，并复用统一 `AiWorkerInvocationApplicationService` 调用台账。

## 非目标

- 不处理 Knowledge `tag_extraction`。
- 不新增 workers 能力；workers 侧已注册。
- 不新增前端页面代码；本 RUNBOOK 只给出后续前端接入控件和操作要求。
- 不改变数据库表结构。

## 能力入口

| capability | operation | workerPath | Java entry | 默认候选 |
| --- | --- | --- | --- | --- |
| `prompt_suggestion` | `PLATFORM_PROMPT_SUGGESTION` | `/internal/ai/platform/prompt-suggestion` | `PlatformAiController#buildPromptSuggestion` | 是 |
| `version_summary` | `PLATFORM_VERSION_SUMMARY` | `/internal/ai/platform/version-summary` | `PlatformAiController#summarizeVersion` | 否 |

## 数据结构

### 请求字段

`PlatformAiRequests.InvokeRequest` 必须保留以下字段：

| field | type | required | note |
| --- | --- | --- | --- |
| `contentType` | `String` | 否 | 调用关联内容类型，例如 `PROMPT_TEMPLATE`。 |
| `contentId` | `Long` | 否 | 调用关联内容 ID。 |
| `objectId` | `Long` | 否 | 调用关联对象 ID。 |
| `serviceId` | `Long` | 否 | AI 服务配置 ID。 |
| `serviceRole` | `String` | 否 | AI 服务角色。 |
| `modelId` | `Long` | 是 | 模型 ID。 |
| `modelName` | `String` | 否 | 模型名称；为空时可由模型配置解析。 |
| `promptVersionId` | `Long` | 否 | 调用使用的提示词版本 ID。 |
| `requestId` | `String` | 是 | 幂等与 worker 请求标识。 |
| `traceId` | `String` | 是 | 链路追踪标识。 |
| `promptMessagesJson` | `String` | 是 | 发送给 worker 的消息模板 JSON。 |
| `promptVariablesJson` | `String` | 否 | 提示词变量 JSON。 |
| `promptHash` | `String` | 否 | 提示词内容 hash。 |
| `inputPayloadJson` | `String` | 是 | worker usecase 输入 payload JSON。 |
| `outputSchemaJson` | `String` | 否 | 结构化输出 schema JSON。 |
| `forceJson` | `Boolean` | 否 | 是否强制 JSON 输出。 |
| `locale` | `String` | 否 | 语言区域。 |
| `allowFallback` | `Boolean` | 否 | 是否允许 fallback。 |
| `createCandidate` | `Boolean` | 否 | 覆盖默认候选策略。 |

### 响应字段

`PlatformAiResponses.InvokeResponse` 必须保留以下字段：

| field | type | note |
| --- | --- | --- |
| `callId` | `Long` | AI 调用记录 ID。 |
| `candidateId` | `Long` | AI 候选 ID；未创建候选时为空。 |
| `requestId` | `String` | 请求标识。 |
| `traceId` | `String` | 链路标识。 |
| `status` | `String` | 最终状态。 |
| `capability` | `String` | worker capability。 |
| `resultFormat` | `String` | 结果格式。 |
| `resultPayload` | `String` | 结果内容。 |
| `artifactReferenceJson` | `String` | 文件类结果引用；Platform 当前通常为空。 |
| `warningsJson` | `String` | worker 警告信息。 |
| `errorType` | `String` | 失败类型。 |
| `errorMessage` | `String` | 失败说明。 |
| `failureStage` | `String` | 失败阶段。 |

### 调用命令字段

`PlatformAiInvokeCommand` 必须完整承接 `InvokeRequest` 字段，并在 `toInvokeCommand` 中补齐：

- `scope = "platform"`
- `capability = PlatformAiWorkerUsecaseSpec.capability()`
- `operation = PlatformAiWorkerUsecaseSpec.operation()`
- `workerPath = PlatformAiWorkerUsecaseSpec.workerPath()`
- `stream = false`
- `createCandidate = request.createCandidate != null ? request.createCandidate : defaultCreateCandidate`

## 任务拆解

### 任务 1：Application 入口

涉及文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/platform/support/PlatformAiWorkerUsecaseSpec.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/platform/support/PlatformAiWorkerUsecaseResolver.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/platform/command/PlatformAiInvokeCommand.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/platform/service/PlatformAiApplicationService.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/platform/service/impl/PlatformAiApplicationServiceImpl.java`

验收点：

- `PlatformAiWorkerUsecaseResolver` 精确映射两个 Platform operation。
- `PlatformAiApplicationServiceImpl` 只调用同步 `AiWorkerInvocationApplicationService#invoke`。
- `prompt_suggestion` 默认创建候选，`version_summary` 默认不创建候选。
- `modelId / requestId / traceId / promptMessagesJson / inputPayloadJson` 为空时拒绝调用。

### 任务 2：Admin HTTP 入口

涉及文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/platform/controller/PlatformAiController.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/platform/controller/request/PlatformAiRequests.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/platform/controller/response/PlatformAiResponses.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/platform/assembler/PlatformAiInterfaceAssembler.java`

验收点：

- `POST /api/ai/platform/prompt-suggestion` 调用 `PlatformAiApplicationService#buildPromptSuggestion`。
- `POST /api/ai/platform/version-summary` 调用 `PlatformAiApplicationService#summarizeVersion`。
- `prompt-suggestion` 权限为 `ai:prompt:edit`。
- `version-summary` 权限为 `ai:prompt:view`。
- HTTP 层只做协议转换，不直接调用 worker client、repository 或 domain model。

### 任务 3：测试与 Coverage

涉及文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/platform/support/PlatformAiWorkerUsecaseResolverTest.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/platform/service/impl/PlatformAiApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/test/java/com/thundax/kuzhambu/ai/interfaces/admin/platform/controller/PlatformAiControllerTest.java`
- `docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`

验收点：

- resolver 测试锁定 `operation / workerPath / capability / defaultCreateCandidate`。
- application 测试锁定 Java → worker invocation 命令字段。
- controller 测试锁定 URL、权限和 application service 委托。
- coverage 中 Platform 两项进入已完成表；未完成表只保留 Knowledge `tag_extraction`。

### 任务 4：前端接入要求

本任务不修改前端文件。后续前端接入时，范围应限制在 `kuzhambu-apps/admin-web/` 内的 AI 提示词相关页面、服务和类型文件。

提示词优化建议控件要求：

- 在提示词版本编辑或详情区域增加“生成优化建议”按钮。
- 点击按钮后打开确认弹窗，展示目标模板、当前版本号、模型选择控件和可编辑补充说明输入框。
- 用户确认后调用 `POST /api/ai/platform/prompt-suggestion`。
- 返回 `candidateId` 时，在候选结果区域显示“采纳”“拒绝”操作按钮。
- “采纳”继续使用既有候选应用入口；“拒绝”继续使用既有候选拒绝入口。

版本摘要控件要求：

- 在提示词版本对比区域增加“生成版本摘要”按钮。
- 按钮点击后读取左右版本内容、变更上下文和模型选择控件值。
- 调用 `POST /api/ai/platform/version-summary`。
- 返回 `resultPayload` 后在摘要面板展示文本结果，不默认生成候选操作。

前端验收点：

- 两个按钮都有 loading、禁用和错误提示状态。
- 模型未选择、版本内容为空或请求字段缺失时，不发起请求并提示用户。
- `prompt_suggestion` 的候选采纳必须由用户显式点击完成。
- `version_summary` 只展示摘要，不出现候选采纳控件。

## 验证

```sh
cd kuzhambu-servers
mvn -pl biz/ai/kuzhambu-ai-application,biz/ai/kuzhambu-ai-interface spotless:apply
mvn -pl biz/ai/kuzhambu-ai-application,biz/ai/kuzhambu-ai-interface -DskipITs test
mvn spotless:check
mvn checkstyle:check
```

## 收口

- 本 RUNBOOK 在对应 PR 合并前保留，任务关闭后删除。
- 不提交 `dev.env`、真实 `.env` 或 worker 临时产物。
