# AI Design

## Purpose

本文档定义 AI 域设计，覆盖 AI 服务配置、模型能力、提示词管理、AI 调用、候选结果和 AI 内容生产流程。

AI 域是所有 AI 能力调用的治理入口。Java 业务域通过 AI application 能力发起 AI 调用；AI application 选择模型、提示词和变量，记录调用和候选结果，再通过 Python workers 执行具体 AI graph。

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

Workers 不拥有 AI 配置、提示词、调用记录或候选结果。Classics、Knowledge 和 Discovery 不得直接绕过 AI 域调用 workers 的 AI 接口。

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
- `AiWorkerInvocation`
- `AiStreamEvent`

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

数据模型必须记录 worker 调用所需的稳定追踪信息，包括模型、服务、能力、提示词版本、请求标识、链路标识、耗时、用量、失败类型和降级状态。stream 片段不是业务事实，默认不逐片段持久化；最终结果、失败或部分失败状态必须进入调用记录。

## Application Layer

- `AiServiceConfigApplicationService`
- `AiModelApplicationService`
- `PromptApplicationService`
- `AiActionStatusApplicationService`
- `AiRefinementApplicationService`
- `AiBatchJobApplicationService`
- `AiWorkerInvocationApplicationService`

Application 层负责模型能力校验、提示词变量校验、主备切换、AI 调用编排、worker 请求构造、stream 转发、候选区管理、批量任务取消和统计记录。

调用流程：

1. 业务域通过 AI application 发起能力调用，并传入业务上下文快照。
2. AI application 校验 AI 功能动作、模型能力映射和当前生效提示词。
3. AI application 校验变量并渲染 messages，或构造经校验的 prompt 模板和变量。
4. AI application 创建调用记录或批量任务记录。
5. AI infra 通过 `WorkerAiClient` 调用 workers。
6. workers 返回同步结果或 stream 事件。
7. AI application 记录耗时、用量、失败类型和降级状态。
8. 需要人工确认的结果进入 `AiCandidate`。
9. 正式内容、问答会话或图谱结果由对应业务域在确认或编排后写入。

批量流程：

- `AiBatchJob` 保存批量任务状态、总数、成功数、失败数、取消状态和失败原因摘要。
- AI application 将批量任务拆分为多个 worker 单元调用。
- 取消批量任务时，AI application 停止继续派发未开始的 worker 单元调用。
- workers 正在执行的单元调用不保存取消状态；返回成功、失败或超时后由 AI application 归档。
- 已完成结果必须保留。

Stream 流程：

- AI application 可以调用 workers 的 SSE 接口。
- AI application 可以把 `started`、`delta`、`progress`、`warning`、`error` 和 `completed` 事件转发给前端或调用方。
- 流式片段只用于展示过程，不直接写入正式内容。
- `completed` 事件或同步最终响应用于生成候选结果、问答消息或调用记录终态。
- stream 中断时，AI application 必须把调用记录为失败或部分失败，并允许重新发起完整调用。

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

Knowledge 调用入口：

- 实体关系候选抽取。
- 图谱候选抽取。
- 世系图候选抽取。

Discovery 调用入口：

- 查询理解。
- 查询改写。
- 回答生成和流式回答生成。

## Infrastructure Layer

- `WorkerAiClient` 适配 Python workers 内部 HTTP 和 SSE 接口。
- AI 域向 workers 传入主服务或备用服务的模型配置、调用参数、渲染后 messages、结构化输出 schema 和完整上下文。
- workers 内部执行 LangGraph；AI 域不直接依赖 workers 内部 graph 实现。
- Repository 持久化 AI 配置、提示词、调用记录和候选结果。
- 外部调用失败需要区分网络传输层失败、worker 协议失败、AI 语义层失败和输出格式失败。
- 网络传输层失败允许由 AI application 决策切换备用服务并重新调用 workers。
- AI 语义层失败不得由 infra 自动无限重试。

## Data Ownership

AI 是 `ai_*` 表的唯一写入方。正式内容写入由 Classics 在用户确认后完成。

Discovery 问答会话、消息和来源由 Discovery 写入。Knowledge 标签、实体、关系、图谱版本和质量指标由 Knowledge 写入。workers 不写入任何业务表。

## Observability

- 记录 AI 调用延迟、失败、成本、服务降级状态和模型检测历史。
- AI Key 不输出到前端、日志或审计。
- 记录 worker 请求标识、链路标识、stream 是否完成、失败分类和用量摘要。

## Acceptance

- AI 配置、模型、提示词和调用链路在一个业务域内闭合。
- AI 结果必须先进入候选区，确认后才影响正式内容。
- AI 域能通过 `WorkerAiClient` 调用 workers 同步和 SSE 接口。
- Classics、Knowledge 和 Discovery 均通过 AI application 使用 AI 能力，不直接依赖 workers。
