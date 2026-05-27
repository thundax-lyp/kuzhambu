# AI Design

## Purpose

本文档定义 AI 域设计，覆盖 AI 服务配置、模型能力、提示词管理、AI 调用、候选结果和 AI 内容生产流程。

## Module

```text
kuzhambu-servers/biz/ai/
  kuzhambu-ai-interface/
  kuzhambu-ai-application/
  kuzhambu-ai-domain/
  kuzhambu-ai-infra/
```

## Business Boundary

AI 拥有模型、能力映射、提示词、调用记录、候选结果和 AI 执行任务。AI 不拥有正式古籍内容、标签治理、图谱结果或问答会话。

## DDD Model

- `AiServiceConfig`
- `AiModel`
- `AiCapabilityMapping`
- `PromptTemplate`
- `PromptVersion`
- `PromptVariable`
- `AiActionStatus`
- `AiCallRecord`
- `AiCandidate`
- `AiBatchJob`
- `ImageUnderstandingResult`
- `EntrySplitCandidate`

## Data Model

表名前缀统一使用 `ai_`。

核心表：

- `ai_service_config`
- `ai_model`
- `ai_model_check_record`
- `ai_capability_mapping`
- `ai_prompt_template`
- `ai_prompt_version`
- `ai_prompt_variable`
- `ai_action_status`
- `ai_call_record`
- `ai_candidate`
- `ai_batch_job`
- `ai_image_understanding`
- `ai_entry_split_candidate`

## Application Layer

- `AiServiceConfigApplicationService`
- `AiModelApplicationService`
- `PromptApplicationService`
- `AiActionStatusApplicationService`
- `AiRefinementApplicationService`
- `AiBatchJobApplicationService`

Application 层负责模型能力校验、提示词变量校验、主备切换、AI 调用编排、候选区管理、批量任务取消和统计记录。

## Interface Layer

Admin 入口：

- AI 服务和模型配置。
- 模型检测历史。
- 能力映射。
- 提示词编辑、版本对比和回滚。
- AI 功能动作状态面板。
- AI 调用统计。

Classics 内容上下文入口：

- 翻译、摘要、标签、问答对、图片理解、条目拆分、视觉描述、信息融合和生图触发。
- 候选结果预览、编辑、确认和拒绝。

## Infrastructure Layer

- AI 客户端适配主服务和备用服务。
- Repository 持久化 AI 配置、提示词、调用记录和候选结果。
- 外部调用失败需要区分网络传输层失败和 AI 语义层失败。

## Data Ownership

AI 是 `ai_*` 表的唯一写入方。正式内容写入由 Classics 在用户确认后完成。

## Observability

- 记录 AI 调用延迟、失败、成本、服务降级状态和模型检测历史。
- AI Key 不输出到前端、日志或审计。

## Acceptance

- AI 配置、模型、提示词和调用链路在一个业务域内闭合。
- AI 结果必须先进入候选区，确认后才影响正式内容。
