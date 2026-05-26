# AI Config Prompt Design

## Purpose

本文档定义 AI 配置与提示词业务域的数据结构和接口设计。本文参考 redesign 中的 AI 配置、模型管理和提示词材料，但不照搬其大对象模型、数据结构或接口形态；本设计必须从 AI 服务配置、模型能力映射和提示词版本治理业务出发，按 DDD 风格重新划分业务边界、聚合和表结构。

AI Config Prompt 是配置治理域，不承载具体业务 AI 候选结果、人工精修、搜索问答编排或知识图谱抽取结果。它只拥有 AI 服务配置、模型条目、能力定义、能力到模型映射、模型检测历史、提示词版本和 AI 调用指标。

## Business Fit Rules

- 业务优先于旧设计：主备服务、模型列表、能力映射、能力校验、提示词版本、变量校验、回滚、动作可用性检查和调用统计必须能直接对应 AI 配置管理业务。
- 原 redesign 只能作为需求线索和素材来源，不能作为表结构、接口路径、模块划分或聚合边界的设计依据。
- AI Key 不得暴露给前端；数据库中只能保存加密后的密钥或密钥引用。
- 具体业务 AI 生成候选由 AI Refinement 或对应业务域承接，本模块只提供配置和调用前置能力。
- 不为未来增强、路线图或预留能力建表、建接口或增加状态字段。

## Module

- 模块名称：AI Config Prompt
- 业务域：aiconfig
- 对应需求文档：[AI-CONFIG-PROMPT-REQUIREMENTS.md](../10-requirements/AI-CONFIG-PROMPT-REQUIREMENTS.md)
- 后端 biz 子工程：`kuzhambu-servers/biz/kuzhambu-biz-ai-config`
- 后端 infra 子工程：`kuzhambu-servers/infra/kuzhambu-infra-ai-config`
- 后台接口入口：`kuzhambu-servers/interfaces/kuzhambu-admin-api`
- 前台接口入口：无
- 前端入口：`kuzhambu-apps/admin-web`
- Python worker 能力：无直接归属

## Business Boundary

- 本模块负责：主 AI 服务配置、备用 AI 服务配置、模型列表、模型启停、模型删除保护、模型连通性检测历史、AI 能力字典、能力到模型映射、模型能力校验、提示词版本、提示词变量解析、提示词回滚、动作可用性检查和 AI 调用指标。
- 本模块不负责：业务 AI 候选结果确认、内容字段写入、搜索问答业务编排、知识图谱抽取结果、视觉资产产物登记。
- 依赖的其他业务域能力：Auth/Core 提供管理员和权限；Audit 记录配置变更；Operations 消费健康状态和统计。
- 对外提供的业务能力：AI 配置管理、模型可用性管理、提示词治理、业务 AI 调用前的配置解析和能力校验。

## DDD Model

### Aggregates

- `AiServiceConfig`：AI 服务配置聚合，区分主服务和备用服务，保存 Base URL、密钥密文、启用状态和健康状态。
- `AiModel`：模型聚合根，归属某个 AI 服务，拥有模型名称、显示名、能力标签、启用状态和检测历史。
- `AiCapability`：AI 能力定义，保存能力编码、名称和所需模型能力标签。
- `AiCapabilityMapping`：能力到模型的映射，保证业务能力只能绑定能力匹配且启用的模型。
- `AiPrompt`：提示词版本聚合，按 `scope + capability` 管理多版本和当前生效版本。
- `AiCallMetric`：AI 调用指标记录，服务于延迟、失败和成本统计。

### Value Objects

- `AiApiSource`：`PRIMARY` / `SECONDARY`。
- `AiModelId`：模型 ULID。
- `AiCapabilityCode`：`translate` / `tags` / `visual` / `fusion` / `qa` / `split` / `image_analysis` / `image_gen` / `knowledge_graph` / `summary` / `version_summary`。
- `AiModelCapabilityTag`：`text` / `vision` / `image_gen` / `embedding`。
- `AiPromptScope`：`sancai` / `wangqi` / `ming_customs` / `knowledge`。
- `AiServiceStatus`：`AVAILABLE` / `DEGRADED` / `UNAVAILABLE`。
- `AiTestStatus`：`SUCCESS` / `FAILED`。

## Data Model

AI Config 表固定使用 `ai_` 前缀。所有对外标识使用 ULID，数据库内部可使用 `bigint id`。

### ai_service_config

保存主备 AI 服务配置。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `service_id` | `serviceId` | 是 | 服务配置 ULID |
| `api_source` | `apiSource` | 是 | `PRIMARY` / `SECONDARY` |
| `base_url` | `baseUrl` | 是 | AI 服务地址 |
| `encrypted_api_key` | `encryptedApiKey` | 否 | 加密后的 API Key 或密钥引用 |
| `enabled` | `enabled` | 是 | 是否启用 |
| `status` | `status` | 是 | 服务健康状态 |
| `last_checked_at` | `lastCheckedAt` | 否 | 最近检测时间 |

约束：
- `api_source` 唯一。
- API Key 不得通过查询接口返回明文。

### ai_model

保存 AI 模型条目。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `model_id` | `modelId` | 是 | 模型 ULID |
| `api_source` | `apiSource` | 是 | 所属服务 |
| `model_name` | `modelName` | 是 | 模型标识名 |
| `display_name` | `displayName` | 是 | 展示名 |
| `capability_tags` | `capabilityTags` | 是 | 能力标签 JSON |
| `description` | `description` | 否 | 能力描述 |
| `enabled` | `enabled` | 是 | 是否启用 |

约束：
- `model_id` 唯一。
- `api_source + model_name` 唯一。
- 删除模型前必须确认不存在能力映射引用。

### ai_model_test_record

保存模型连通性检测历史。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `test_id` | `testId` | 是 | 检测 ULID |
| `model_id` | `modelId` | 是 | 模型 ULID |
| `api_source` | `apiSource` | 是 | 检测时服务来源 |
| `model_name` | `modelName` | 是 | 检测时模型名快照 |
| `status` | `status` | 是 | `SUCCESS` / `FAILED` |
| `latency_ms` | `latencyMs` | 否 | 响应耗时 |
| `error_message` | `errorMessage` | 否 | 失败原因 |

约束：
- 每个模型详情默认展示最近检测记录；保留数量由应用服务控制。

### ai_capability

保存 AI 能力定义和所需模型能力标签。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `capability` | `capability` | 是 | 能力编码 |
| `name` | `name` | 是 | 能力名称 |
| `required_tags` | `requiredTags` | 是 | 所需模型能力标签 JSON |
| `enabled` | `enabled` | 是 | 是否启用 |
| `sort_order` | `sortOrder` | 是 | 排序 |

约束：
- `capability` 唯一。
- 模型映射保存时必须校验模型能力标签覆盖 `required_tags`。

### ai_capability_mapping

保存能力到模型的当前映射。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `mapping_id` | `mappingId` | 是 | 映射 ULID |
| `capability` | `capability` | 是 | AI 能力编码 |
| `model_id` | `modelId` | 是 | 模型 ULID |

约束：
- `capability` 唯一。
- 只能绑定已启用模型。
- 模型能力与业务能力不匹配时不得保存。

### ai_prompt

保存提示词版本。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `prompt_id` | `promptId` | 是 | 提示词版本 ULID |
| `scope` | `scope` | 是 | 知识库范围 |
| `capability` | `capability` | 是 | AI 能力编码 |
| `version_no` | `versionNo` | 是 | 版本号 |
| `content` | `content` | 是 | 提示词正文 |
| `variables_snapshot` | `variablesSnapshot` | 是 | 变量列表 JSON |
| `description` | `description` | 否 | 用途说明 |
| `active` | `active` | 是 | 是否当前生效 |

约束：
- `prompt_id` 唯一。
- `scope + capability + version_no` 唯一。
- 同一 `scope + capability` 只能有一个当前生效提示词，由应用服务事务保证。
- 提示词变量必须使用 `{{variable}}` 格式。
- 提示词缺失时业务 AI 调用必须失败，不得使用隐藏兜底提示词。

### ai_call_metric

保存 AI 调用指标。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `metric_id` | `metricId` | 是 | 指标 ULID |
| `scope` | `scope` | 否 | 调用范围 |
| `capability` | `capability` | 是 | AI 能力编码 |
| `api_source` | `apiSource` | 是 | 实际服务来源 |
| `model_id` | `modelId` | 否 | 模型 ULID |
| `model_name` | `modelName` | 是 | 模型名快照 |
| `success` | `success` | 是 | 是否成功 |
| `fallback_used` | `fallbackUsed` | 是 | 是否使用备用服务 |
| `latency_ms` | `latencyMs` | 否 | 延迟 |
| `input_tokens` | `inputTokens` | 是 | 输入 token 数 |
| `output_tokens` | `outputTokens` | 是 | 输出 token 数 |
| `cost_amount` | `costAmount` | 是 | 成本金额 |
| `error_message` | `errorMessage` | 否 | 失败原因 |

约束：
- 统计接口基于该表聚合延迟、失败和成本。
- 配置变更不得修改历史指标快照。

## Application Layer

- `AiServiceConfigApplicationService`：主备服务配置、启停、状态展示和健康状态更新。
- `AiModelApplicationService`：模型新增、编辑、启用、禁用、删除保护和列表查询。
- `AiModelTestApplicationService`：模型连通性检测和检测历史查询。
- `AiCapabilityMappingApplicationService`：能力到模型映射、候选模型查询和能力校验。
- `AiPromptApplicationService`：提示词保存、变量解析、版本列表、版本对比、回滚和缺失检查。
- `AiActionStatusApplicationService`：AI 功能动作可用性检查和不可用原因展示。
- `AiCallMetricApplicationService`：AI 调用延迟、失败和成本统计。

事务边界：
- 保存能力映射必须校验模型启用状态和能力标签。
- 删除模型必须先检查能力映射引用。
- 保存提示词必须解析变量、关闭旧活跃版本、创建新活跃版本，并触发动作可用性检查。
- 回滚提示词必须切换活跃版本，并触发动作可用性检查。
- 主服务网络传输失败时允许切换备用服务并记录降级状态；AI 语义层失败不得自动重试。

## Interface Layer

后台接口固定使用 `/api/admin/ai-config/**`。

### Service And Model API

- `GET /ai-config/services`：查询主备服务配置和状态。
- `PUT /ai-config/services/{apiSource}`：更新服务配置。
- `POST /ai-config/services/{apiSource}/test`：测试服务连通性。
- `GET /ai-config/models`：查询模型列表。
- `POST /ai-config/models`：新增模型。
- `PUT /ai-config/models/{modelId}`：编辑模型。
- `POST /ai-config/models/{modelId}/enable`：启用模型。
- `POST /ai-config/models/{modelId}/disable`：禁用模型。
- `DELETE /ai-config/models/{modelId}`：删除模型。
- `POST /ai-config/models/{modelId}/test`：检测模型连通性。
- `GET /ai-config/models/{modelId}/tests`：查询模型检测历史。

### Capability And Mapping API

- `GET /ai-config/capabilities`：查询 AI 能力定义。
- `GET /ai-config/mappings`：查询能力到模型映射。
- `PUT /ai-config/mappings/{capability}`：保存能力到模型映射。
- `GET /ai-config/mappings/{capability}/candidates`：查询某能力可选模型。

### Prompt API

- `GET /ai-config/prompts`：查询提示词列表。
- `GET /ai-config/prompts/{scope}/{capability}`：读取当前提示词。
- `POST /ai-config/prompts/{scope}/{capability}`：保存提示词新版本。
- `GET /ai-config/prompts/{scope}/{capability}/versions`：读取版本历史。
- `GET /ai-config/prompts/{scope}/{capability}/versions/{promptId}`：读取版本详情。
- `POST /ai-config/prompts/{scope}/{capability}/versions/{promptId}/rollback`：回滚到指定版本。
- `POST /ai-config/prompts/parse-variables`：解析提示词变量。
- `POST /ai-config/prompts/optimize`：生成提示词优化建议。

### Status And Metrics API

- `GET /ai-config/actions/status`：查询 AI 功能动作状态面板。
- `POST /ai-config/actions/check`：重新检查动作可用性。
- `GET /ai-config/metrics/latency`：查询延迟统计。
- `GET /ai-config/metrics/failures`：查询失败统计。
- `GET /ai-config/metrics/cost`：查询成本统计。

本模块不提供 portal 接口。

## Infrastructure Layer

- Repository：`AiServiceConfigRepository`、`AiModelRepository`、`AiModelTestRecordRepository`、`AiCapabilityRepository`、`AiCapabilityMappingRepository`、`AiPromptRepository`、`AiCallMetricRepository`。
- Mapper：按表建立 MyBatis Mapper。
- PersistenceAssembler：每个聚合根使用独立 `*PersistenceAssembler`。
- 外部客户端：AI Provider HTTP Client、Audit Application Service。
- 缓存：当前服务配置、能力映射、当前生效提示词、动作状态。
- 消息：配置变更后可发布本域事件供运行时缓存刷新。

## Data Ownership

- 本模块拥有：`ai_service_config`、`ai_model`、`ai_model_test_record`、`ai_capability`、`ai_capability_mapping`、`ai_prompt`、`ai_call_metric`。
- 本模块只读引用：Auth/Core 用户和权限。
- 禁止跨域直接访问：不得直接写入业务内容表、AI Refinement 候选结果、Search 索引、QA 会话或 Knowledge Graph 结果。
- Flyway 脚本归属：`kuzhambu-admin-api` 的 `db/migration/V1__init.sql`，按 AI Config 分段。

## Observability

- 运行日志：记录模型检测、能力映射失败、提示词变量校验失败、主备切换和 AI 调用异常。
- 访问日志：由接口层统一记录。
- 审计日志：服务配置、模型、能力映射和提示词变更都应形成业务审计。
- 关键指标：模型数、启用模型数、提示词缺失数、动作不可用数、平均延迟、失败率、成本。

## Acceptance

- 管理员能测试模型连通性并看到结果。
- 管理员能查看模型检测历史。
- 管理员无法删除仍被 AI 能力映射使用的模型。
- 管理员为某项 AI 能力选择能力不匹配的模型时保存失败并看到原因。
- 提示词变量缺失时保存或调用失败并给出明确提示。
- 管理员能对比两个提示词版本。
- 管理员能回滚提示词历史版本。
- 提示词保存或回滚后，管理员能看到受影响 AI 功能动作是否可用。
- AI 服务异常时管理员能看到降级或失败状态。
- 主服务不可用且备用服务可用时，业务 AI 功能不中断，管理后台显示降级状态。
