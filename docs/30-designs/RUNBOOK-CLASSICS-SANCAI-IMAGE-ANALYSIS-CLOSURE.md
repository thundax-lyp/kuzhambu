# RUNBOOK: Classics / SANCAI image_analysis 标准化闭合

## 1. Goal

完成 `CLASSICS_SANCAI_IMAGE_ANALYSIS` 的标准化闭合，使其与其他 `Classics` 精修能力使用同一套 `resolver -> operation -> workerPath -> stream final-state` 调用协议。

完成后，Java 侧对 `SANCAI_ENTRY + image_analysis` 的调用固定满足：

- `AiInvokeCommand.capability = "image_analysis"`
- `AiInvokeCommand.operation = "CLASSICS_SANCAI_IMAGE_ANALYSIS"`
- `AiInvokeCommand.workerPath = "/internal/ai/classics/sancai/image-analysis"`
- `AiInvokeCommand.stream = true`
- `AiInvokeCommand.createCandidate = true`

完成后，`AI-IMPLEMENTATION-COVERAGE.md` 中 `CLASSICS_SANCAI_IMAGE_ANALYSIS` 可从 `部分完成` 改为 `已完成`。

## 2. Out Of Scope

以下内容不在本次收口范围内：

- 新增 `Workers` capability 或变更 `Workers` 路由契约
- 改造 `image_gen`、`fusion`、`translate-batch-item`
- 新增数据库字段、表结构或迁移脚本
- 改动 Admin Web 页面交互或新增页面能力
- 扩大到 `Knowledge`、`Discovery`、`Platform` 其他 AI usecase
- 同步修改 `WORKERS-IMPLEMENTATION-COVERAGE.md`

## 3. Fixed Contract

本次收口后的固定契约如下：

### 3.1 Worker usecase contract

- content type: `SANCAI_ENTRY`
- capability: `image_analysis`
- operation: `CLASSICS_SANCAI_IMAGE_ANALYSIS`
- worker path: `/internal/ai/classics/sancai/image-analysis`
- stream: `true`
- output format: `MARKDOWN`

### 3.2 Final-state result contract

本次不新增字段，只固定现有字段的使用方式：

- `AiInvokeResult.resultFormat = "MARKDOWN"`
- `AiInvokeResult.resultPayload` 保存最终图片分析正文
- `AiInvokeResult.failureStage` 只在失败时记录
- `AiInvokeResult.streamCompleted = true` 表示流式最终态完整收口
- `AiCallRecord.streamUsed = true`
- `AiCallRecord.resultFormat = "MARKDOWN"`
- `AiCallRecord.resultPayload` 保存最终图片分析正文
- `AiCandidate.resultFormat = "MARKDOWN"`
- `AiCandidate.resultPayload` 保存最终候选正文

### 3.3 Schema decision

本次不做 schema 变更。

不新增以下任何字段：

- `AiInvokeResult`
- `AiCallRecord`
- `AiCandidate`
- `AiRefinementRequestCommand`
- `AiInvokeCommand`

本次只变更现有字段赋值与 usecase 解析路径。
本次不修改以下任何 domain / persistence 结构定义：

- `AiInvokeResult`
- `AiCallRecord`
- `AiCandidate`
- `AiRefinementRequestCommand`
- `AiInvokeCommand`

## 4. Related Files

### Java application

- [AiRefinementApplicationServiceImpl.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/service/impl/AiRefinementApplicationServiceImpl.java)
- [AiRefinementRequestCommand.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/command/AiRefinementRequestCommand.java)
- [ClassicsAiWorkerUsecaseResolver.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/support/ClassicsAiWorkerUsecaseResolver.java)
- [AiInvokeCommand.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/command/AiInvokeCommand.java)
- [AiInvokeResult.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/result/AiInvokeResult.java)

### Java domain

- [AiCallRecord.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/model/entity/AiCallRecord.java)
- [AiCandidate.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/model/entity/AiCandidate.java)

### Tests

- [AiRefinementApplicationServiceImplTest.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/refinement/service/impl/AiRefinementApplicationServiceImplTest.java)
- [ClassicsAiWorkerUsecaseResolverTest.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/refinement/support/ClassicsAiWorkerUsecaseResolverTest.java)

### Docs

- [AI-IMPLEMENTATION-COVERAGE.md](/Volumes/storage/workspace/kuzhambu/docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md)

## 5. Task Breakdown

### T1. Resolver 收入口径

目标：
将 `SANCAI_ENTRY + image_analysis` 收入 `ClassicsAiWorkerUsecaseResolver` 的标准映射表。

相关文件：

- [ClassicsAiWorkerUsecaseResolver.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/support/ClassicsAiWorkerUsecaseResolver.java)
- [ClassicsAiWorkerUsecaseResolverTest.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/refinement/support/ClassicsAiWorkerUsecaseResolverTest.java)

相关数据结构：

- `SUPPORTED_USECASES["SANCAI_ENTRY"]["image_analysis"]`
- `ClassicsAiWorkerUsecaseSpec.operation`
- `ClassicsAiWorkerUsecaseSpec.workerPath`

处理动作：

- 为 `SANCAI_ENTRY` 新增 `image_analysis` 映射
- 固定 operation 为 `CLASSICS_SANCAI_IMAGE_ANALYSIS`
- 固定 worker path 为 `/internal/ai/classics/sancai/image-analysis`
- 移除 resolver test 中“应抛 unsupported”断言，改为标准映射断言

验收点：

- resolver 可以成功解析 `SANCAI_ENTRY + image_analysis`
- 解析结果的 `operation` 与 `workerPath` 与 worker 接口文档一致

### T2. 精修入口去掉 legacy 分支

目标：
让 `analyzeImage(...)` 与其他 Classics 精修能力走同一套 resolver 分发，不再保留 legacy `operation/workerPath` 旁路。

相关文件：

- [AiRefinementApplicationServiceImpl.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/service/impl/AiRefinementApplicationServiceImpl.java)
- [AiRefinementApplicationServiceImplTest.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/refinement/service/impl/AiRefinementApplicationServiceImplTest.java)

相关数据结构：

- `AiInvokeCommand.capability`
- `AiInvokeCommand.operation`
- `AiInvokeCommand.workerPath`

处理动作：

- 删除 `image_analysis` 的特殊分支判断
- 统一通过 `ClassicsAiWorkerUsecaseResolver` 生成 `operation` 与 `workerPath`
- 将 `analyzeImage` 的测试从“保留 legacy operation”改为“使用标准 usecase path”

验收点：

- `analyzeImage(...)` 最终传给 invocation service 的 `operation` 为 `CLASSICS_SANCAI_IMAGE_ANALYSIS`
- `analyzeImage(...)` 最终传给 invocation service 的 `workerPath` 为 `/internal/ai/classics/sancai/image-analysis`

### T3. 固定 image_analysis 的 stream 调用语义

目标：
将 `image_analysis` 明确固定为流式调用能力，并在 Java 侧显式写入 `AiInvokeCommand.stream = true`。

相关文件：

- [AiRefinementApplicationServiceImpl.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/service/impl/AiRefinementApplicationServiceImpl.java)
- [AiRefinementRequestCommand.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/command/AiRefinementRequestCommand.java)
- [AiInvokeCommand.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/command/AiInvokeCommand.java)
- [AiRefinementApplicationServiceImplTest.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/refinement/service/impl/AiRefinementApplicationServiceImplTest.java)

相关数据结构：

- `AiInvokeCommand.stream`
- `AiInvokeCommand.createCandidate`
- `AiInvokeCommand.capability`

处理动作：

- 在 image analysis 调用组装阶段显式设置 `stream = true`
- 保持 `createCandidate = true`
- 为测试增加 `streamUsed` 断言，锁定标准调用语义
- 不扩大到其他 capability 的 `stream` 组装重构

验收点：

- image analysis 调用命令的 `stream = true`
- image analysis 调用命令的 `createCandidate = true`
- 不影响其他已有精修能力的命令组装

### T4. 锁定最终结果落库语义

目标：
确保 `image_analysis` 作为 `MARKDOWN` 能力时，最终结果继续走统一的 `AiInvokeResult -> AiCallRecord -> AiCandidate` 正文落库语义，不引入单独结果模型。

相关文件：

- [AiInvokeResult.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/result/AiInvokeResult.java)
- [AiCallRecord.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/model/entity/AiCallRecord.java)
- [AiCandidate.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/model/entity/AiCandidate.java)
- [AiRefinementApplicationServiceImplTest.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/refinement/service/impl/AiRefinementApplicationServiceImplTest.java)

相关数据结构：

- `AiInvokeResult.resultFormat`
- `AiInvokeResult.resultPayload`
- `AiInvokeResult.failureStage`
- `AiInvokeResult.streamCompleted`
- `AiCallRecord.streamUsed`
- `AiCallRecord.resultFormat`
- `AiCallRecord.resultPayload`
- `AiCandidate.resultFormat`
- `AiCandidate.resultPayload`

处理动作：

- 不新增字段，不新增表结构
- 不改动 `AiCallRecord` 与 `AiCandidate` 的字段定义
- 通过测试锁定 `image_analysis` 最终结果仍为 `MARKDOWN + resultPayload`
- 通过测试锁定 `streamCompleted` 与 `streamUsed` 的最终态语义

验收点：

- `image_analysis` 不引入新的结果模型字段
- `image_analysis` 的最终正文继续落在 `resultPayload`
- `image_analysis` 的最终态继续复用统一调用记录与候选记录模型

### T5. 覆盖文档收口

目标：
将 `AI-IMPLEMENTATION-COVERAGE.md` 中 `CLASSICS_SANCAI_IMAGE_ANALYSIS` 从 `部分完成` 收口为 `已完成`，并移除旧的 legacy 说明。

相关文件：

- [AI-IMPLEMENTATION-COVERAGE.md](/Volumes/storage/workspace/kuzhambu/docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md)
- [RUNBOOK-CLASSICS-SANCAI-IMAGE-ANALYSIS-CLOSURE.md](/Volumes/storage/workspace/kuzhambu/docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-ANALYSIS-CLOSURE.md)

相关数据结构：

- `CLASSICS_SANCAI_IMAGE_ANALYSIS` 覆盖状态
- `note` 中关于 `stream=true` 与 legacy Java 入口的说明

处理动作：

- 将该项状态改为 `已完成`
- 将说明改为“已切到标准 classics usecase path 与 stream final-state 协议”
- 保持 `WORKERS-IMPLEMENTATION-COVERAGE.md` 不变
- 保持其他未完成项状态不变

验收点：

- `AI-IMPLEMENTATION-COVERAGE.md` 中该项不再保留 `部分完成`
- 文档说明与代码目标一致

## 6. Definition Of Done

本次任务完成的定义如下：

1. `ClassicsAiWorkerUsecaseResolver` 正式支持 `SANCAI_ENTRY + image_analysis`
2. `AiRefinementApplicationServiceImpl.analyzeImage(...)` 不再保留 legacy `operation/workerPath` 分支
3. image analysis 调用命令固定包含：
   - `capability = image_analysis`
   - `operation = CLASSICS_SANCAI_IMAGE_ANALYSIS`
   - `workerPath = /internal/ai/classics/sancai/image-analysis`
   - `stream = true`
   - `createCandidate = true`
4. 最终结果继续以 `MARKDOWN + resultPayload` 进入统一调用记录与候选记录
5. `AI-IMPLEMENTATION-COVERAGE.md` 将该项标记为 `已完成`
6. `WORKERS-IMPLEMENTATION-COVERAGE.md` 保持不变

## 7. Execution Order

固定执行顺序：

1. `T1 Resolver 收入口径`
2. `T2 精修入口去掉 legacy 分支`
3. `T3 固定 image_analysis 的 stream 调用语义`
4. `T4 锁定最终结果落库语义`
5. `T5 覆盖文档收口`
