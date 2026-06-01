# RUNBOOK AI Domain

## Purpose

本文档定义 AI 域实现手册，覆盖 AI 服务配置、模型能力、提示词、worker 调用、stream、候选结果、批量任务和后台接口。

执行顺序固定为：`domain -> application -> infra -> interface -> starter -> verification`。每个任务项关联文件保持 2-5 个，避免把多个判断混入同一步。

本 RUNBOOK 是临时执行手册，任务关闭时应删除。

## Scope

覆盖：

- AI 服务配置、主备服务和模型管理。
- AI 能力定义、能力到模型映射和动作状态。
- 提示词模板、版本、变量校验、版本对比和回滚。
- AI 调用记录、worker 同步调用和 SSE stream 调用。
- AI 候选结果、图片理解结果、条目拆分候选。
- 批量任务、批量取消和单项失败归档。
- 后台 HTTP API、worker client、安全签名和验证入口。

不覆盖：

- Python workers 实现。
- Classics 正式内容写入。
- Knowledge 正式图谱结果写入。
- Discovery 问答会话和来源写入。
- 前端页面实现。

## Global Rules

- AI 域是所有 AI workers 调用的治理入口。
- Classics、Knowledge 和 Discovery 不得直接调用 workers 的 AI 接口。
- AI 域不向 workers 暴露模型配置读取、提示词读取、候选写入或任务状态回调接口。
- AI 域调用 workers 时必须传入完整执行上下文、模型配置和 prompt/messages。
- workers 结果只能通过当前 HTTP 响应或 SSE 流返回。
- stream 片段只用于展示过程，最终落库以 `completed` 或同步最终响应为准。
- AI Key 不得输出到前端、日志、审计、错误详情或持久化明文字段。
- 业务表不保存审计字段；操作者、创建者、更新者、删除者、发起人由 System 审计记录。
- `priority` 只作为单表内全局唯一排序字段，不参与普通 KEY 或组合 KEY。
- `starter` 只做运行时装配，不承载 Controller、业务规则、查询聚合或持久化实现。

## Domain Layer

### D1 Service And Model Domain

目标：定义 AI 服务配置、模型和模型检测记录领域模型。

关联文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/config/model/entity/AiServiceConfig.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/model/model/entity/AiModel.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/model/model/entity/AiModelCheckRecord.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/model/repository/AiModelRepository.java`

验收：模型覆盖主备服务、启用状态、模型能力标签、检测结果和检测历史查询。

### D2 Capability Domain

目标：定义 AI 能力、能力映射和动作状态领域模型。

关联文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/capability/model/entity/AiCapability.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/capability/model/entity/AiCapabilityMapping.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/capability/model/entity/AiActionStatus.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/capability/repository/AiCapabilityRepository.java`

验收：能力映射保存前可判断模型能力标签是否满足业务能力要求。

### D3 Prompt Domain

目标：定义提示词模板、版本、变量和变量校验规则。

关联文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/prompt/model/entity/PromptTemplate.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/prompt/model/entity/PromptVersion.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/prompt/model/entity/PromptVariable.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/prompt/repository/PromptRepository.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/prompt/service/PromptVariableDomainService.java`

验收：同一 scope + capability 只有一个当前模板，变量缺失时可拒绝保存或调用。

### D4 Invocation And Candidate Domain

目标：定义 AI 调用记录、候选结果和 worker stream 终态模型。

关联文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/model/entity/AiCallRecord.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/model/entity/AiCandidate.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/model/valueobject/AiUsageSnapshot.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/repository/AiInvocationRepository.java`

验收：调用记录能表达同步、stream 成功、stream 中断、主备降级、失败类型和用量摘要。

### D5 Batch And Specialized Result Domain

目标：定义批量任务、图片理解结果和条目拆分候选模型。

关联文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/batch/model/entity/AiBatchJob.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/vision/model/entity/ImageUnderstandingResult.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/split/model/entity/EntrySplitCandidate.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/batch/repository/AiBatchJobRepository.java`

验收：批量任务能记录总数、成功数、失败数、取消数和失败摘要；专项结果可关联 call 或 candidate。

## Application Layer

### A1 Service And Model Application

目标：实现服务配置、模型管理、连通性检测和检测历史用例。

关联文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/config/service/AiServiceConfigApplicationService.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/config/service/impl/AiServiceConfigApplicationServiceImpl.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/model/service/AiModelApplicationService.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/model/service/impl/AiModelApplicationServiceImpl.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/model/command/AiModelCheckCommand.java`

验收：管理员能维护主备服务和模型，并触发检测记录落库。

### A2 Capability Application

目标：实现能力定义查询、能力映射保存、删除模型前占用检查和动作状态刷新。

关联文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/capability/service/AiCapabilityApplicationService.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/capability/service/impl/AiCapabilityApplicationServiceImpl.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/capability/command/AiCapabilityMappingSaveCommand.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/capability/result/AiActionStatusResult.java`

验收：能力不匹配时保存失败；仍被映射使用的模型不能删除。

### A3 Prompt Application

目标：实现提示词编辑、版本、变量解析、对比、回滚和优化建议入口。

关联文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/prompt/service/PromptApplicationService.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/prompt/service/impl/PromptApplicationServiceImpl.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/prompt/command/PromptTemplateSaveCommand.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/prompt/query/PromptVersionCompareQuery.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/prompt/result/PromptVersionResult.java`

验收：保存或回滚后能刷新相关 AI 动作可用状态；变量缺失时拒绝保存或调用。

### A4 Worker Invocation Application

目标：实现 AI 域统一调用 workers 的同步和 SSE 编排。

关联文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/service/AiWorkerInvocationApplicationService.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/service/impl/AiWorkerInvocationApplicationServiceImpl.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/command/AiInvokeCommand.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/result/AiInvokeResult.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/result/AiStreamEventResult.java`

验收：应用层能构造完整 worker 请求，保存调用记录，并把 completed 结果写为候选或返回调用方。

### A5 Refinement Application

目标：实现 Classics 内容上下文的翻译、摘要、标签、问答、图片理解、视觉描述和条目拆分入口。

关联文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/service/AiRefinementApplicationService.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/service/impl/AiRefinementApplicationServiceImpl.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/command/AiRefinementRequestCommand.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/result/AiCandidateResult.java`

验收：AI 结果先进入候选区；拒绝候选不改变正式内容。

### A6 Batch Application

目标：实现批量任务创建、单元派发、失败归档和取消语义。

关联文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/batch/service/AiBatchJobApplicationService.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/batch/service/impl/AiBatchJobApplicationServiceImpl.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/batch/command/AiBatchJobCreateCommand.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/batch/result/AiBatchJobResult.java`

验收：取消后不再派发未开始 worker 调用；已完成结果保留。

## Infrastructure Layer

### I1 Config And Model Persistence

目标：实现服务配置、模型和检测记录持久化。

关联文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/model/persistence/dataobject/AiModelDO.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/model/persistence/dataobject/AiModelCheckRecordDO.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/model/persistence/mapper/AiModelMapper.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/model/repository/impl/AiModelRepositoryImpl.java`

验收：DO 字段与 `db/schema/ai.sql` 一致，Repository 不泄漏 MyBatis 细节。

### I2 Capability And Prompt Persistence

目标：实现能力、能力映射、动作状态和提示词持久化。

关联文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/capability/persistence/mapper/AiCapabilityMapper.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/capability/repository/impl/AiCapabilityRepositoryImpl.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/prompt/persistence/mapper/PromptMapper.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/prompt/repository/impl/PromptRepositoryImpl.java`

验收：当前提示词只通过 `scope + capability` 定位，版本历史可查询和回滚。

### I3 Invocation Persistence

目标：实现调用记录、候选结果、批量任务和专项结果持久化。

关联文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/invocation/persistence/mapper/AiInvocationMapper.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/invocation/repository/impl/AiInvocationRepositoryImpl.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/batch/persistence/mapper/AiBatchJobMapper.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/batch/repository/impl/AiBatchJobRepositoryImpl.java`

验收：调用失败、stream 中断、候选状态和批量计数均能持久化。

### I4 Worker Client Contract

目标：实现 `WORKERS-AI-INTERFACE.md` 定义的 HTTP、SSE、HMAC 和错误归一化。

关联文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/client/WorkerAiClient.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/client/WorkerAiHttpClient.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/client/WorkerAiSignatureSupport.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/client/WorkerAiProperties.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/client/dto/WorkerAiDtos.java`

验收：请求头、签名输入、错误类型和 stream completed 处理与接口文档一致。

## Interface Layer

### F1 Admin Config Interface

目标：提供 AI 服务、模型、模型检测和能力映射后台接口。

关联文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/config/controller/AiConfigController.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/config/controller/request/AiConfigRequests.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/config/controller/response/AiConfigResponses.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/config/assembler/AiConfigInterfaceAssembler.java`

验收：后台能配置主备服务、模型、能力映射，并查看检测历史。

### F2 Admin Prompt Interface

目标：提供提示词编辑、变量解析、版本对比、回滚和动作状态接口。

关联文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/prompt/controller/PromptController.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/prompt/controller/request/PromptRequests.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/prompt/controller/response/PromptResponses.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/prompt/assembler/PromptInterfaceAssembler.java`

验收：管理员能管理提示词版本并看到相关动作是否可用。

### F3 Admin Invocation Interface

目标：提供 AI 调用统计、调用记录、批量任务和候选管理接口。

关联文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/AiInvocationController.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/request/AiInvocationRequests.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/response/AiInvocationResponses.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/assembler/AiInvocationInterfaceAssembler.java`

验收：管理员能查看调用延迟、失败、成本、批量状态和失败原因。

### F4 Admin Refinement Interface

目标：提供 Classics 内容上下文可调用的 AI 精修接口。

关联文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/AiRefinementController.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/request/AiRefinementRequests.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/response/AiRefinementResponses.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/assembler/AiRefinementInterfaceAssembler.java`

验收：翻译、摘要、标签、问答对、图片理解和条目拆分可进入候选区。

## Starter Layer

### S1 Admin Starter Assembly

目标：装配 AI interface、application、infra 和 worker client 配置。

关联文件：

- `kuzhambu-servers/starter/kuzhambu-admin-starter/pom.xml`
- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/resources/application.yml`
- `.env.example`
- `deploy/.env.example`

验收：后台 starter 能扫描 AI 模块，worker endpoint 和 HMAC secret 通过环境变量配置。

## Verification Layer

### V1 AI Architecture Verification

目标：新增或补齐 AI 模块架构验证。

关联文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/test/java/com/thundax/kuzhambu/ai/domain/AiDomainArchitectureTest.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/AiApplicationArchitectureTest.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/test/java/com/thundax/kuzhambu/ai/infra/AiInfraArchitectureTest.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/test/java/com/thundax/kuzhambu/ai/interfaces/AiInterfaceArchitectureTest.java`

验收：架构测试覆盖层依赖、包路径、Controller、Repository、DO 和 Mapper 归属。

### V2 Worker Client Verification

目标：验证 worker client 请求签名、错误归一化和 stream completed 处理。

关联文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/test/java/com/thundax/kuzhambu/ai/infra/client/WorkerAiSignatureSupportTest.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/test/java/com/thundax/kuzhambu/ai/infra/client/WorkerAiHttpClientTest.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/invocation/AiWorkerInvocationApplicationServiceTest.java`

验收：签名输入与接口文档一致；stream 未收到 completed 时记录为失败或部分失败。

### V3 AI Persistence Verification

目标：验证 AI SQL、DO、Mapper 和 Repository 最小读写。

关联文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/test/java/com/thundax/kuzhambu/ai/infra/model/AiModelRepositoryIT.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/test/java/com/thundax/kuzhambu/ai/infra/prompt/PromptRepositoryIT.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/test/java/com/thundax/kuzhambu/ai/infra/invocation/AiInvocationRepositoryIT.java`

验收：`db/schema/ai.sql` 和 `db/data/ai.sql` 可加载，核心表最小 CRUD 通过。

### V4 Module Verification

目标：执行 AI 模块最小格式、静态检查和编译验证。

关联文件：

- `kuzhambu-servers/biz/ai/pom.xml`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/pom.xml`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/pom.xml`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-infra/pom.xml`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/pom.xml`

验收：从 `kuzhambu-servers/` 执行 `mvn spotless:apply`、`mvn checkstyle:check` 和 AI 模块相关测试通过。
