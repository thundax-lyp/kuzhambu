# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `knowledge graph command/result`：扩展抽取 command 与 application result 字段
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-KNOWLEDGE-CLOSURE.md`
    - 范围对象：`RequestRelationExtractionCommand.java`、`RequestGraphExtractionCommand.java`、`RequestLineageExtractionCommand.java`、`GraphExtractionTaskResult.java`
    - 处理动作：补齐批量范围、触发来源、重生成与取消批任务所需字段并保持 application result 独立
    - 验收点：command/result 字段语义一致且不透传 AI 域 response
    - 重要度：9/10

- [ ] `knowledge graph interface dto`：扩展图谱抽取接口请求响应模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-KNOWLEDGE-CLOSURE.md`
    - 范围对象：`GraphExtractionRequests.java`、`GraphExtractionResponses.java`
    - 处理动作：补齐 `CreateRequest / PageTaskRequest / RegenerateRequest / BatchCancelRequest` 与 `TaskResponse / BatchCancelResponse`
    - 验收点：接口 request/response 与 application result 对齐且不复用 AI controller response
    - 重要度：9/10

- [ ] `knowledge graph batch create`：增加批量创建与重生成编排入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-KNOWLEDGE-CLOSURE.md`
    - 范围对象：`KnowledgeGraphExtractionApplicationService.java`、`KnowledgeGraphExtractionApplicationServiceImpl.java`、`KnowledgeGraphExtractionInterfaceAssembler.java`、`KnowledgeGraphExtractionController.java`
    - 处理动作：新增批量创建与重生成入口并复用 AI 域 `batchId` 组织同批任务
    - 验收点：单条创建不回归且批量创建、重生成都能返回可追踪批次信息
    - 重要度：10/10

- [ ] `ai batch linkage`：打通 Knowledge 任务与 AI 批量台账关联
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-KNOWLEDGE-CLOSURE.md`
    - 范围对象：`AiBatchJobApplicationService.java`、`AiBatchJobApplicationServiceImpl.java`、`KnowledgeGraphExtractionApplicationServiceImpl.java`
    - 处理动作：复用 AI 批量任务能力并在 Knowledge 图谱任务中稳定回写 `batchJobId`
    - 验收点：一个批量图谱抽取请求能稳定生成 AI batch 关联并可查询批次状态
    - 重要度：10/10

- [ ] `knowledge graph batch cancel`：增加取消批任务后端入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-KNOWLEDGE-CLOSURE.md`
    - 范围对象：`GraphExtractionRequests.java`、`GraphExtractionResponses.java`、`KnowledgeGraphExtractionApplicationService.java`、`KnowledgeGraphExtractionApplicationServiceImpl.java`、`KnowledgeGraphExtractionController.java`
    - 处理动作：新增 `task/cancel-batch` 接口并实现仅取消未开始单元的批任务取消能力
    - 验收点：用户可按 `batchJobId` 取消批任务且已完成结果仍可查看和应用
    - 重要度：10/10

- [ ] `knowledge graph quality trigger`：收口质量触发图谱抽取入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-KNOWLEDGE-CLOSURE.md`
    - 范围对象：`KnowledgeGraphExtractionApplicationServiceImpl.java`、`KnowledgeGraphExtractionController.java`、`graph-extraction-service.ts`、`graph-extraction-page.tsx`
    - 处理动作：增加从质量筛选结果触发抽取的入口参数并统一写入 `triggerSource=QUALITY_REPORT`
    - 验收点：前端可从质量相关场景发起图谱抽取且后端任务记录可追溯触发来源
    - 重要度：8/10

- [ ] `admin graph extraction service`：扩展前端 service 与类型契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-KNOWLEDGE-CLOSURE.md`
    - 范围对象：`graph-extraction-types.ts`、`graph-extraction-service.ts`、`graph-extraction-service.test.ts`
    - 处理动作：补齐批次相关字段并新增 `regenerateTask / cancelBatchTask` service 方法
    - 验收点：前端 contract 与后端接口一致且 service 测试通过
    - 重要度：8/10

- [ ] `admin graph extraction page`：扩展批量任务、重生成与取消页面交互
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-KNOWLEDGE-CLOSURE.md`
    - 范围对象：`graph-extraction-create.tsx`、`graph-extraction-task-table.tsx`、`graph-extraction-task-detail.tsx`、`graph-extraction-page.tsx`、`graph-extraction-page.test.tsx`
    - 处理动作：增加批量范围输入、重生成动作、取消批任务动作与批次字段展示
    - 验收点：页面可创建批量任务、取消未完成批任务并区分普通抽取与重生成
    - 重要度：8/10

- [ ] `ai knowledge backend tests`：补齐 AI × Knowledge 后端测试覆盖
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-KNOWLEDGE-CLOSURE.md`
    - 范围对象：`KnowledgeAiExtractionApplicationServiceImplTest.java`、`KnowledgeGraphExtractionApplicationServiceTest.java`、`KnowledgeGraphExtractionControllerTest.java`、`GraphExtractionTaskRepositoryImplTest.java`
    - 处理动作：覆盖批量创建、重生成、质量触发、取消批任务与 `batchJobId` 追踪
    - 验收点：application、interface、repository 测试通过
    - 重要度：9/10

- [ ] `ai knowledge closure cleanup`：清理 TODO 与 RUNBOOK 现场
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-KNOWLEDGE-CLOSURE.md`
    - 范围对象：`TODO.md`、`docs/30-designs/RUNBOOK-AI-KNOWLEDGE-CLOSURE.md`、相关实现覆盖文档与 PR 描述
    - 处理动作：在闭环完成后删除对应 TODO 项、移除已完成 RUNBOOK，并同步实现覆盖与 PR 收口信息
    - 验收点：TODO 不残留已完成项、已完成 RUNBOOK 被删除且收口文档状态一致
    - 重要度：7/10

## 待讨论项
