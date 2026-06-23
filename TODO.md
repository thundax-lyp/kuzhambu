# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `knowledge-application/graph extraction contract`：定义知识抽取应用层契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-AI-WORKERS-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/.../KnowledgeGraphExtractionApplicationService.java`、`RequestRelationExtractionCommand.java`、`RequestGraphExtractionCommand.java`、`RequestLineageExtractionCommand.java`、`GraphExtractionTaskResult.java`
    - 处理动作：定义三类抽取任务的应用层命令、结果与服务接口
    - 验收点：application 层输入输出能完整表达 relation、graph、lineage 三类任务
    - 重要度：8/10

- [ ] `knowledge-application/graph extraction orchestration`：实现知识抽取任务编排
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-AI-WORKERS-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/.../KnowledgeGraphExtractionApplicationServiceImpl.java`、`kuzhambu-knowledge-application/pom.xml`、`KnowledgeGraphExtractionApplicationServiceTest.java`
    - 处理动作：实现三类抽取任务创建、AI 协作调用与测试覆盖
    - 验收点：Knowledge application 能发起抽取任务并通过测试验证基础编排
    - 重要度：10/10

- [ ] `knowledge-interface/graph extraction admin api`：暴露知识抽取后台接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-AI-WORKERS-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/.../KnowledgeGraphExtractionController.java`、`GraphExtractionRequests.java`、`GraphExtractionResponses.java`、`KnowledgeGraphExtractionInterfaceAssembler.java`
    - 处理动作：实现知识抽取任务请求、详情与应用接口的协议层转换
    - 验收点：admin 接口可稳定接收三类任务请求并输出任务结果模型
    - 重要度：8/10

- [ ] `workers/knowledge extraction contract`：固化 workers 抽取输出契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-AI-WORKERS-CLOSURE.md`
    - 范围对象：`kuzhambu-workers/src/kuzhambu_workers/schemas/ai.py`、`kuzhambu_workers/ai/graphs/basic.py`、`tests/test_ai_usecase_routes_knowledge.py`、`tests/test_graph_registry.py`
    - 处理动作：为三类 Knowledge 抽取补齐结构化 schema、graph 注册与路由测试
    - 验收点：workers 返回的 `result_payload` 字段名与结构满足稳定契约
    - 重要度：9/10

- [ ] `knowledge-application/task detail sync`：回填抽取任务状态与 AI 结果关联
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-AI-WORKERS-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/.../KnowledgeGraphExtractionApplicationServiceImpl.java`、`GraphExtractionTaskRepository.java`、`GraphExtractionTaskRepositoryImpl.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-domain/.../AiInvocationRepository.java`
    - 处理动作：打通 `ai_call_id`、`ai_candidate_id` 到任务详情状态查询
    - 验收点：Knowledge 能查看任务状态、候选关联、失败原因与时间戳而不回查 workers
    - 重要度：9/10

- [ ] `knowledge-application/apply formal results`：应用候选结果到正式知识表
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-AI-WORKERS-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/.../KnowledgeGraphCandidateApplySupport.java`、`KnowledgeGraphExtractionApplicationServiceImpl.java`、`KnowledgeGraphCandidateApplySupportTest.java`
    - 处理动作：实现候选结果应用逻辑并补齐支持类测试
    - 验收点：Knowledge 能将候选实体、关系或世系结果写入正式表
    - 重要度：10/10

- [ ] `knowledge/version link`：补齐图谱版本关联落库
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-AI-WORKERS-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/.../GraphVersionRepository.java`、`kuzhambu-knowledge-infra/.../GraphVersionRepositoryImpl.java`、`kuzhambu-knowledge-application/.../KnowledgeGraphCandidateApplySupport.java`、`kuzhambu-knowledge-infra/src/test/.../GraphVersionRepositoryTest.java`
    - 处理动作：为正式结果应用补齐图谱版本仓储与关联写入
    - 验收点：应用正式结果时能生成或更新图谱版本关联记录
    - 重要度：8/10

- [ ] `admin-web/knowledge extraction service contract`：接通知识抽取前端服务契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-AI-WORKERS-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-service.ts`、`graph-extraction-types.ts`、`graph-extraction-service.test.ts`
    - 处理动作：定义前端请求响应模型与服务层调用契约
    - 验收点：admin-web 可稳定调用知识抽取任务接口并有服务层测试约束
    - 重要度：7/10

- [ ] `admin-web/knowledge extraction page shell`：搭建抽取任务页骨架
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-AI-WORKERS-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-page.tsx`、`graph-extraction-page.css`、`src/router/index.tsx`、`graph-extraction-page.test.tsx`
    - 处理动作：创建页面壳、路由入口与页面级基础测试
    - 验收点：后台路由可进入知识抽取任务页且页面骨架稳定可渲染
    - 重要度：7/10

- [ ] `admin-web/knowledge extraction create actions`：支持三类抽取任务创建
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-AI-WORKERS-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-create.tsx`、`graph-extraction-page.tsx`、`graph-extraction-service.ts`、组件测试文件
    - 处理动作：接通 relation、graph、lineage 三类抽取任务创建动作
    - 验收点：前端页面能发起三类抽取任务并反映创建结果
    - 重要度：7/10

- [ ] `admin-web/knowledge extraction detail apply`：支持详情查看与应用动作
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-AI-WORKERS-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-task-table.tsx`、`graph-extraction-task-detail.tsx`、`graph-extraction-page.tsx`、组件测试文件
    - 处理动作：接通任务列表、详情抽屉与候选结果应用动作
    - 验收点：前端能展示 `aiCallId`、`aiCandidateId`、错误信息、时间戳并触发应用
    - 重要度：8/10

- [ ] `docs/knowledge ai workers`：同步设计与 readiness 文档
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/30-designs/KNOWLEDGE-DESIGN.md`、`AI-DESIGN.md`、`WORKERS-DESIGN.md`、`docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：仅按已落地结果同步设计和覆盖状态文档
    - 验收点：文档口径与代码现状一致且不记录未落地能力
    - 重要度：6/10

- [ ] `runbook and todo cleanup`：收口 Knowledge AI Workers 现场
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-AI-WORKERS-CLOSURE.md`
    - 范围对象：`docs/30-designs/RUNBOOK-KNOWLEDGE-AI-WORKERS-CLOSURE.md`、`TODO.md`
    - 处理动作：在阶段任务完成后删除无剩余价值的 runbook 并清空或收窄 TODO
    - 验收点：PR 收口前 runbook 与 TODO 只保留下一阶段仍未关闭内容
    - 重要度：6/10

## 待审阅任务项

## 待讨论项
