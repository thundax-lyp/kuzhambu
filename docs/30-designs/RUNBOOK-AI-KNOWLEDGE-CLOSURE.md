# AI × Knowledge Closure Runbook

## 1. Purpose

本文档定义 `AI × Knowledge` 下一阶段闭环实施方案。

目标不是重做已经打通的最小链路，而是基于当前代码现状，把需求中仍未收口的 `Knowledge 图谱抽取 × AI 调用` 能力补成可验收闭环，并把范围明确限制在本轮可交付边界内。

本文档以需求为准，以当前仓库代码现状为实。

## 2. Scope

本轮覆盖：

- Knowledge 通过 AI 域发起 `RELATION / GRAPH / LINEAGE` 三类抽取的完整业务闭环收口。
- Knowledge 按筛选范围批量触发图谱抽取。
- Knowledge 对“重生成”的最小业务闭环。
- Knowledge 从质量摘要或低质量筛选结果中直接触发图谱抽取。
- AI 批量任务台账与 Knowledge 图谱抽取任务的关联收口。
- 任务状态、候选结果、失败原因、应用结果与版本结果的可追溯性补强。
- application / interface / admin-web / 测试口径对齐。

本轮不覆盖：

- 图谱可视化画布。
- 固定 14 门类空位展示。
- 独立 Knowledge 标签 AI 提取入口 `KNOWLEDGE_TAG_EXTRACTION`。

说明：

- `KNOWLEDGE_TAG_EXTRACTION` 在 workers 和 AI coverage 中是已知缺口，但当前 `KNOWLEDGE-REQUIREMENTS.md` 没有定义独立的 Knowledge 侧触发入口、候选确认页面和正式落点，因此不纳入本轮闭环范围，避免把“图谱抽取闭环”与“标签治理入口产品设计”混成一个大任务。

## 3. Success Criteria

完成后，以下能力必须同时成立：

1. 管理员能在 Knowledge 后台按筛选范围批量创建 `RELATION / GRAPH / LINEAGE` 抽取任务。
2. 管理员能对已有范围发起“重生成”，并明确区分“替换未人工确认结果”与“保留人工确认结果”的业务语义。
3. 管理员能从质量相关入口直接触发抽取，而不是只能手工填写单条任务。
4. 每个图谱抽取任务都能追溯到：
   - `knowledge_graph_extraction_task`
   - `ai_call_record`
   - `ai_candidate`
   - 应用后的 `knowledge_graph_version`
5. 失败任务必须保留明确失败类型和失败信息。
6. 批量任务必须支持 AI 域台账记录、派发控制和取消未开始单元；用户可主动取消批任务，已完成子任务结果保留。
7. Knowledge 不直接调用 workers AI 接口，仍然只通过 AI 域。
8. 本轮新增能力具备后端 application / interface 测试，以及 admin-web service / page 测试。

## 4. Current Baseline

### 4.1 已完成的最小链路

当前仓库已经具备以下基础：

- AI 侧已经存在 Knowledge 抽取调用入口：
  - `extractRelations`
  - `extractGraph`
  - `extractLineage`
- AI 侧已经存在三类 usecase resolver：
  - `KNOWLEDGE_RELATION_EXTRACTION`
  - `KNOWLEDGE_GRAPH_EXTRACTION`
  - `KNOWLEDGE_LINEAGE_EXTRACTION`
- Knowledge 侧已经存在图谱抽取任务 application service：
  - 创建任务
  - 分页查询
  - 详情查询
  - 应用候选结果
- Knowledge 侧已经存在候选应用支持：
  - `ai_candidate.result_payload` 可写回正式实体、关系、世系和版本表
- Admin Web 已有：
  - `/knowledge/graph-extraction`
  - `/knowledge/graph-results`

### 4.2 当前未闭环的部分

从需求和代码现状看，当前真正未收口的是：

- Knowledge 要求的“批量生成和重生成”尚未形成运行时闭环。
- Knowledge 要求的“从质量报告或筛选结果中批量触发图谱提取”尚未形成运行时闭环。
- AI 域已有批量任务基础台账，但尚未与 Knowledge 图谱抽取任务建立稳定关联。
- 当前图谱抽取以“单次单任务”模型为主，缺少基于轻量 `batchId` 的批量组织、取消和结果聚合。
- 当前验证重点集中在“单任务创建/应用”，缺少“批量派发/取消/重生成/质量触发”的测试与前端闭环。

## 5. Requirements Mapping

本轮重点对应以下需求：

### AI Requirements

- `Knowledge 实体关系候选抽取、图谱候选抽取和世系图候选抽取`
- `AI 结果候选确认、失败反馈、重试和批量取消`
- `批量任务必须由 AI 域保存任务状态并拆分为多个 worker 单元调用`
- `Knowledge 能通过 AI 域完成实体关系候选抽取，正式图谱结果仍由 Knowledge 保存`

### Knowledge Requirements

- `必须支持三才图会实体和关系 AI 提取`
- `必须支持异步提取任务和进度展示`
- `必须支持批量生成和重生成`
- `必须支持从图谱质量报告或筛选结果中批量触发图谱提取`
- `未被人工确认的实体和关系可在重生成时替换`
- `已人工确认的实体和关系应保留`
- `每次提取、重生成或精修保存后，应更新相关门类的质量指标`

## 6. Target Design

### 6.1 闭环边界

本轮采用以下责任分工：

- Knowledge：
  - 校验业务范围、内容范围、操作权限
  - 准备抽取输入快照
  - 创建图谱抽取任务
  - 管理批量图谱任务与重生成语义
  - 应用候选结果到正式事实
  - 维护图谱版本和质量摘要

- AI：
  - 解析 capability / operation / workerPath
  - 记录批量任务台账
  - 记录 call / candidate
  - 控制批量派发与取消
  - 调用 workers

- Workers：
  - 仅执行无状态抽取
  - 不管理 Knowledge 正式事实

### 6.2 本轮目标调用模型

1. Admin Web 发起单条或批量图谱抽取请求。
2. Knowledge application 校验范围并创建 `knowledge_graph_extraction_task`。
3. 若为批量请求，Knowledge 通过 AI 域创建轻量 `AiBatchJob`，并拿到 `batchId`。
4. Knowledge 使用同一个 `batchId` 拆分并创建多个同类型抽取子任务。
5. 每个子任务通过 AI 域发起对应抽取能力。
6. AI 域为每个子任务写入 `ai_call_record` 与 `ai_candidate`，并通过 `AiBatchJob` 聚合成功/失败/取消统计。
7. 管理员可按 `batchId` 取消未完成批任务；已完成子任务结果保留，未开始单元不再派发。
8. Knowledge 任务页可查询任务、批任务、失败原因和候选状态。
9. 管理员应用候选结果后，Knowledge 回写正式实体/关系/世系，并更新图谱版本和任务状态。

### 6.3 已确认决策

- 首版 `batch` 语义固定为：`一个 batchId = 一种 extractionType + 一个 selectionScope + 多个同类型子任务`。
- 本轮不引入额外 `taskGroup` 概念；批量组织统一复用 AI 域现有 `AiBatchJob` 轻台账。
- 不新增 `batchJobNo`；继续复用现有 `batchId` 作为批次业务关联标识。
- `AiBatchJob` 只承担展示、统计、取消和追踪职责，不承载 Knowledge 业务规则。
- 本轮纳入“取消批任务”能力，但只要求取消未开始单元；已完成结果保留，部分失败按批次汇总展示。
- 本轮不纳入独立 `KNOWLEDGE_TAG_EXTRACTION`、图谱画布和完整质量大盘。

## 7. Data Structure Adjustments

### 7.1 必做调整

为了支持“批量生成 / 重生成 / 质量触发”闭环，本轮需要扩展 `knowledge_graph_extraction_task` 领域与持久化模型，建议直接按新系统目标表结构处理。

#### `knowledge_graph_extraction_task` 新增字段

- `batchJobId`
  - 含义：关联 AI 域批量任务台账。
  - 说明：字段命名沿用 `batchJobId`，值来源为 AI 域现有 `batchId`；本轮不新增 `batchJobNo`。
- `triggerSource`
  - 取值建议：`MANUAL` / `QUALITY_REPORT` / `REGENERATE`
- `selectionScopeJson`
  - 含义：记录本次筛选范围或批量范围快照，便于审计和重试。
- `replaceUnconfirmedOnly`
  - 含义：标识重生成时是否仅替换未人工确认结果。
- `parentTaskId`
  - 含义：子任务挂在某个总任务或重生成母任务下。

#### 需要同步扩展的后端模型

- `GraphExtractionTask`
- `GraphExtractionTaskDO`
- `GraphExtractionTaskRepository`
- `GraphExtractionTaskRepositoryImpl`
- `GraphExtractionTaskPersistenceAssembler`
- `GraphExtractionTaskResult`
- `GraphExtractionResponses.TaskResponse`

#### 需要同步扩展的前端模型

- `GraphExtractionTaskRecord`
- `GraphExtractionCreateCommand`
- `GraphExtractionTaskPageQuery`

#### 字段语义约束

- `batchJobId`
  - 单任务可为空。
  - 批量任务子项必须有值。
- `triggerSource`
  - 首次手工触发使用 `MANUAL`。
  - 从质量报告或质量筛选入口触发使用 `QUALITY_REPORT`。
  - 从既有任务或既有范围重新发起使用 `REGENERATE`。
- `selectionScopeJson`
  - 必须保存用于拆分批任务的筛选快照，不依赖前端二次回放。
- `replaceUnconfirmedOnly`
  - 只在 `triggerSource=REGENERATE` 时有业务意义。
  - 首版默认值为 `true`。
- `parentTaskId`
  - 只在“基于既有任务重生成子任务”场景使用。
  - 普通批量创建可为空。

### 7.2 可选调整

如果本轮需要对质量触发做更强追溯，可增加：

- `qualitySnapshotJson`
  - 含义：记录触发当时的质量摘要快照。

本字段不是本轮最小闭环必需；如果实现复杂度过高，可先只保存 `triggerSource + selectionScopeJson`。

### 7.3 `AiBatchJob` 口径

- 本轮不新增 `AiBatchJob` 结构字段。
- 继续复用现有：
  - `batchId`
  - `scope`
  - `capability`
  - `contentType`
  - `status`
  - `totalCount / successCount / failedCount / cancelledCount`
  - `failureSummaryJson`
- `AiBatchJob` 的职责固定为：批次展示、统计、取消、追踪。
- `AiBatchJob` 不承担 Knowledge 的业务规则和正式事实写入。

## 8. Interface Changes

### 8.1 后端接口变更

保留现有接口：

- `POST /api/knowledge/graph-extraction/task/add`
  - 用途：首次创建单任务或批量任务；不承载重生成语义。
- `POST /api/knowledge/graph-extraction/task/page`
- `POST /api/knowledge/graph-extraction/task/get`
- `POST /api/knowledge/graph-extraction/task/apply`

新增接口：

- `POST /api/knowledge/graph-extraction/task/regenerate`
  - 用途：按已有任务或已有筛选范围发起重生成。
  - 规则：`sourceTaskId` 与 `selectionScopeJson` 至少提供一项。
- `POST /api/knowledge/graph-extraction/task/cancel-batch`
  - 用途：按 `batchJobId` 取消未完成批任务。
  - 规则：仅取消未开始单元，不回滚已完成结果。

扩展现有请求模型：

- `GraphExtractionRequests.CreateRequest`
  - 新增：`triggerSource`、`selectionScopeJson`、`replaceUnconfirmedOnly`
  - 继续保留：`taskType`、`scopeType`、`scopeJson`、模型与提示词上下文字段
- `GraphExtractionRequests.PageTaskRequest`
  - 新增：`batchJobId`、`triggerSource`
- `GraphExtractionRequests.TaskIdRequest`
  - 保持不变，仅用于单任务详情与单任务应用
- 新增 `GraphExtractionRequests.RegenerateRequest`
  - 建议字段：`taskType`、`sourceTaskId`、`selectionScopeJson`、`replaceUnconfirmedOnly`、`requestedBy`
- 新增 `GraphExtractionRequests.BatchCancelRequest`
  - 建议字段：`batchJobId`、`requestedBy`

扩展现有响应模型：

- `GraphExtractionResponses.TaskResponse`
  - 新增：`batchJobId`、`triggerSource`、`selectionScopeJson`、`replaceUnconfirmedOnly`、`parentTaskId`
- `GraphExtractionResponses.BatchCancelResponse`
  - 建议字段：`batchJobId`、`status`、`cancelledCount`、`completedCount`、`failedCount`

### 8.2 前端 service 与类型变更

保留现有方法：

- `addTask`
- `pageTasks`
- `getTaskDetail`
- `applyTaskCandidate`

新增前端 service 方法：

- `regenerateTask`
- `cancelBatchTask`

前端类型变更：

- `GraphExtractionTaskRecord`
  - 新增：`batchJobId`、`triggerSource`、`selectionScopeJson`、`replaceUnconfirmedOnly`、`parentTaskId`
- `GraphExtractionCreateCommand`
  - 新增：`triggerSource`、`selectionScopeJson`、`replaceUnconfirmedOnly`
- `GraphExtractionTaskPageQuery`
  - 新增：`batchJobId`、`triggerSource`
- 新增：
  - `GraphExtractionRegenerateCommand`
  - `GraphExtractionBatchCancelCommand`

## 9. Related Files

### 9.1 AI 侧关键文件

- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/knowledge/service/impl/KnowledgeAiExtractionApplicationServiceImpl.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/knowledge/support/KnowledgeAiWorkerUsecaseResolver.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/service/impl/AiWorkerInvocationApplicationServiceImpl.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/batch/service/AiBatchJobApplicationService.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/batch/service/impl/AiBatchJobApplicationServiceImpl.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/knowledge/service/KnowledgeAiExtractionDomainService.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/repository/AiInvocationRepository.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/response/AiInvocationResponses.java`

### 9.2 Knowledge 侧关键文件

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/command/RequestRelationExtractionCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/command/RequestGraphExtractionCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/command/RequestLineageExtractionCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/service/KnowledgeGraphExtractionApplicationService.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/service/impl/KnowledgeGraphExtractionApplicationServiceImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/result/GraphExtractionTaskResult.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/support/KnowledgeGraphCandidateApplySupport.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/model/entity/GraphExtractionTask.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/GraphExtractionTaskDO.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/repository/impl/GraphExtractionTaskRepositoryImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/controller/request/GraphExtractionRequests.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/controller/KnowledgeGraphExtractionController.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/assembler/KnowledgeGraphExtractionInterfaceAssembler.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/controller/response/GraphExtractionResponses.java`

### 9.3 Admin Web 关键文件

- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-types.ts`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-service.ts`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-create.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-task-table.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-task-detail.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/graph-results-page.tsx`

### 9.4 测试文件

- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/knowledge/service/impl/KnowledgeAiExtractionApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/graph/KnowledgeGraphExtractionApplicationServiceTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/controller/KnowledgeGraphExtractionControllerTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/graph/repository/impl/GraphExtractionTaskRepositoryImplTest.java`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-service.test.ts`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-page.test.tsx`

### 9.5 Workers 关键文件

- `kuzhambu-workers/tests/test_ai_usecase_routes_knowledge.py`
- `kuzhambu-workers/tests/test_ai_usecase_registry.py`

## 10. Task Breakdown

以下任务必须按小步推进；每项控制在 `2-6` 个文件。

### T1 扩展 Knowledge 图谱任务模型以承载批量/重生成语义

范围文件：

- `GraphExtractionTask.java`
- `GraphExtractionTaskDO.java`
- `GraphExtractionTaskPersistenceAssembler.java`
- `GraphExtractionTaskRepository.java`
- `GraphExtractionTaskRepositoryImpl.java`

处理动作：

- 增加 `batchJobId / triggerSource / selectionScopeJson / replaceUnconfirmedOnly / parentTaskId`
- 保持现有单任务读写兼容

验收点：

- 单任务创建不回归
- 新字段可正常入库和读回

### T2 扩展 Knowledge application command 与 application result

范围文件：

- `RequestRelationExtractionCommand.java`
- `RequestGraphExtractionCommand.java`
- `RequestLineageExtractionCommand.java`
- `GraphExtractionTaskResult.java`

处理动作：

- 补齐批量范围、触发来源、重生成语义和取消批任务所需字段
- 为后续 interface 层返回 Knowledge 自有 response 准备完整 application result

验收点：

- command/result 字段语义一致
- 不在 application result 中透传 AI 域 response

### T3 扩展 Knowledge interface request/response

范围文件：

- `GraphExtractionRequests.java`
- `GraphExtractionResponses.java`

处理动作：

- 补齐 `CreateRequest / PageTaskRequest / RegenerateRequest / BatchCancelRequest`
- 补齐 `TaskResponse / BatchCancelResponse`
- 保持 interface 仅返回 Knowledge 自有 response

验收点：

- request/response 字段与 application result 对齐
- 不复用 AI controller response

### T4 在 Knowledge application 增加批量创建与重生成编排

范围文件：

- `KnowledgeGraphExtractionApplicationService.java`
- `KnowledgeGraphExtractionApplicationServiceImpl.java`
- `KnowledgeGraphExtractionInterfaceAssembler.java`
- `KnowledgeGraphExtractionController.java`

处理动作：

- 新增批量创建入口
- 新增重生成入口
- 复用 AI 域 `batchId` 组织同批任务
- 明确“仅替换未人工确认结果”的默认规则

验收点：

- 单条创建仍可用
- 批量创建返回批任务追踪信息
- 批量任务和子任务能通过 `batchId` 关联查询
- 重生成入口可区分普通抽取

### T5 让 Knowledge 批量任务稳定关联 AI 批量台账

范围文件：

- `AiBatchJobApplicationService.java`
- `AiBatchJobApplicationServiceImpl.java`
- `KnowledgeGraphExtractionApplicationServiceImpl.java`

处理动作：

- 复用 AI 批量任务能力
- 在 Knowledge 图谱任务中回写 `batchJobId`
- 形成“Knowledge 母任务 -> AI batch -> 子任务”追踪链

验收点：

- 一个批量图谱抽取请求能稳定生成 AI batch 关联
- 可查询批量任务状态

### T6 增加取消批任务后端接口与返回模型

范围文件：

- `GraphExtractionRequests.java`
- `GraphExtractionResponses.java`
- `KnowledgeGraphExtractionApplicationService.java`
- `KnowledgeGraphExtractionApplicationServiceImpl.java`
- `KnowledgeGraphExtractionController.java`

处理动作：

- 新增 `task/cancel-batch` 接口
- 增加 `BatchCancelRequest / BatchCancelResponse`
- 取消未开始单元并保留已完成结果

验收点：

- 用户可取消批任务
- 取消后已完成结果仍可查看和应用
- controller response 不透传 AI response

### T7 收口质量触发图谱抽取入口

范围文件：

- `KnowledgeGraphExtractionApplicationServiceImpl.java`
- `KnowledgeGraphExtractionController.java`
- `graph-extraction-service.ts`
- `graph-extraction-page.tsx`

处理动作：

- 增加从质量筛选结果触发抽取的入口参数
- 统一写入 `triggerSource=QUALITY_REPORT`

验收点：

- 前端可以从质量相关场景发起图谱抽取
- 后端任务记录可追溯触发来源

### T8 扩展 Admin Web 的 service 与 types

范围文件：

- `graph-extraction-types.ts`
- `graph-extraction-service.ts`
- `graph-extraction-service.test.ts`

处理动作：

- 增加 `batchJobId / triggerSource / selectionScopeJson / replaceUnconfirmedOnly / parentTaskId`
- 增加 `regenerateTask / cancelBatchTask`
- 校验请求体与后端 contract 对齐

验收点：

- service contract 与后端一致
- 类型字段完整
- service 测试通过

### T9 扩展 Admin Web 的批量任务、重生成与取消页面交互

范围文件：

- `graph-extraction-create.tsx`
- `graph-extraction-task-table.tsx`
- `graph-extraction-task-detail.tsx`
- `graph-extraction-page.tsx`
- `graph-extraction-page.test.tsx`

处理动作：

- 增加批量范围输入
- 增加重生成动作
- 增加取消批任务动作
- 展示 `batchJobId / triggerSource / replaceUnconfirmedOnly`

验收点：

- 页面可创建批量任务
- 页面可取消未完成批任务
- 任务详情可看出普通抽取与重生成差异

### T10 补齐 AI × Knowledge 后端测试

范围文件：

- `KnowledgeAiExtractionApplicationServiceImplTest.java`
- `KnowledgeGraphExtractionApplicationServiceTest.java`
- `KnowledgeGraphExtractionControllerTest.java`
- `GraphExtractionTaskRepositoryImplTest.java`

处理动作：

- 覆盖批量创建
- 覆盖重生成
- 覆盖质量触发
- 覆盖取消批任务
- 覆盖 `batchJobId` 追踪

验收点：

- application / interface / repository 测试通过

## 11. Validation Plan

后端：

- `mvn -pl biz/ai/kuzhambu-ai-application,biz/knowledge/kuzhambu-knowledge-application,biz/knowledge/kuzhambu-knowledge-interface -am spotless:apply`
- `mvn -pl biz/ai/kuzhambu-ai-application,biz/knowledge/kuzhambu-knowledge-application,biz/knowledge/kuzhambu-knowledge-interface -am test`

前端：

- `cd kuzhambu-apps && npm --workspace kuzhambu-admin-web run format`
- `cd kuzhambu-apps && npm run format:check`
- `cd kuzhambu-apps && npm run lint`
- `cd kuzhambu-apps && npm test`

Workers：

- `cd kuzhambu-workers && .venv/bin/python -m pytest -q`

## 12. Risks

- 如果把 `KNOWLEDGE_TAG_EXTRACTION` 一起并入本轮，会把“图谱抽取闭环”扩张成“图谱抽取 + taxonomy AI 入口重设计”，风险过高。
- 如果不先给 `knowledge_graph_extraction_task` 增加批量与触发来源字段，后续批量任务和质量触发只能靠临时字段拼装，追溯性会很脆弱。
- `qualitySummary` 当前已经存在，但“按门类低质量直接触发抽取”仍缺产品级入口和请求模型，容易在前后端语义上产生二义性。

## 13. Out of Scope Follow-up

以下内容保留到后续轮次：

- `KNOWLEDGE_TAG_EXTRACTION` 的独立业务入口、候选确认与 taxonomy 正式落点。
- 图谱画布和可视化交互。
- 固定 14 门类空位与高级门类分布可视化。
- 按门类的完整质量报告大盘。
