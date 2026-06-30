# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `AiWorkerModelConfigResolver/WorkerAiHttpClient`：实现 AI 域统一 modelConfig 组装与下发
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-WORKERS-CLASSICS-TEXT-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/command/AiInvokeCommand.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/config/service/AiServiceConfigApplicationService.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/model/service/AiModelApplicationService.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/support/AiWorkerModelConfigResolver.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/client/WorkerAiHttpClient.java`
    - 处理动作：在 AI 域形成单一 `modelConfig` 真相组装入口并将完整字段下发给 workers
    - 验收点：workers 请求中 `serviceRole/apiSource/baseUrl/apiKey/modelName/capabilityTags/parameters/timeoutMs` 全部真实可用
    - 重要度：10/10

- [ ] `AiRefinementApplicationServiceImpl/DiscoveryAiApplicationServiceImpl/KnowledgeAiExtractionApplicationServiceImpl`：统一复用 modelConfig resolver
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-WORKERS-CLASSICS-TEXT-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/service/impl/AiRefinementApplicationServiceImpl.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/discovery/service/impl/DiscoveryAiApplicationServiceImpl.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/knowledge/service/impl/KnowledgeAiExtractionApplicationServiceImpl.java`
    - 处理动作：让 `Refinement`、`Discovery`、`Knowledge` 三条入口统一接入 `AiWorkerModelConfigResolver`
    - 验收点：业务入口不再自行拼装 `apiSource/baseUrl/apiKey/capabilityTags/parameters`
    - 重要度：8/10

- [ ] `text.py/graph_registry.py/basic.py`：替换 workers 文本占位 graph
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-WORKERS-CLASSICS-TEXT-CLOSURE.md`
    - 范围对象：`kuzhambu-workers/src/kuzhambu_workers/ai/graphs/text.py`、`kuzhambu-workers/src/kuzhambu_workers/ai/graph_registry.py`、`kuzhambu-workers/src/kuzhambu_workers/ai/graphs/basic.py`、`kuzhambu-workers/tests/test_graph_registry.py`、`kuzhambu-workers/tests/test_worker_e2e_ai_usecase_classics.py`
    - 处理动作：新增真实文本 graph，并将 classics `translate/summary` usecase 切换到该 graph
    - 验收点：`POST /internal/ai/classics/sancai/translate`、`POST /internal/ai/classics/sancai/summary`、`POST /internal/ai/classics/wangqi/summary`、`POST /internal/ai/classics/ming-customs/summary` 返回非空 `TEXT`
    - 重要度：10/10

- [ ] `ai_routes.py/test_ai_routes.py`：收紧 workers 文本成功条件与失败映射
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-WORKERS-CLASSICS-TEXT-CLOSURE.md`
    - 范围对象：`kuzhambu-workers/src/kuzhambu_workers/api/ai_routes.py`、`kuzhambu-workers/tests/test_ai_routes.py`、`kuzhambu-workers/tests/test_worker_e2e_ai_usecase_classics.py`
    - 处理动作：把非空 `choices[0].message.content` 收口为唯一成功出口，并统一失败阶段映射
    - 验收点：空字符串、缺字段、非法 JSON 不再进入成功候选
    - 重要度：9/10

- [ ] `AiRefinementTaskController/AiRefinementTaskApplicationServiceImpl`：新增精修任务接口与编排
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-WORKERS-CLASSICS-TEXT-CLOSURE.md`、`docs/30-designs/AI-DESIGN.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/AiRefinementTaskController.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/request/AiRefinementTaskRequests.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/response/AiRefinementTaskResponses.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/service/impl/AiRefinementTaskApplicationServiceImpl.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/test/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/AiRefinementTaskControllerTest.java`
    - 处理动作：新增 `task/add`、`task/get`、`task/page`、`task/cancel` 接口，并把 worker 最终态回写到任务台账
    - 验收点：前端可以拿到 `taskId` 并轮询看到 `PENDING/RUNNING/SUCCEEDED/FAILED/PARTIAL/CANCELLED`
    - 重要度：10/10

- [ ] `AiRefinementTaskCleanupService/AiRefinementTaskCleanupServiceImpl`：补齐精修任务失效清理
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-WORKERS-CLASSICS-TEXT-CLOSURE.md`、`docs/30-designs/AI-DESIGN.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/service/AiRefinementTaskCleanupService.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/service/impl/AiRefinementTaskCleanupServiceImpl.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/refinement/persistence/mapper/AiRefinementTaskMapper.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/AiRefinementTaskController.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/refinement/service/impl/AiRefinementTaskCleanupServiceImplTest.java`
    - 处理动作：增加每小时扫描的失效任务清理，先超时收口再删除终态历史任务
    - 验收点：`PENDING/RUNNING` 超过 `12` 小时自动转成 `FAILED/TASK_EXPIRED`，终态任务保留 `12` 小时后可删除
    - 重要度：9/10

- [ ] `ClassicsContentApplicationServiceAiCandidateTest/ClassicsContentAdminControllerTest`：验收 classics 候选应用
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-WORKERS-CLASSICS-TEXT-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/content/ClassicsContentAdminControllerTest.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/refinement/service/impl/AiRefinementApplicationServiceImplTest.java`
    - 处理动作：补齐 `translate/summary` 候选应用的后端与接口验收测试
    - 验收点：`SANCAI_ENTRY.translate`、`SANCAI_ENTRY.summary`、`WANGQI_DOCUMENT.summary`、`MING_CUSTOMS.summary` 应用后正式字段和版本号正确
    - 重要度：10/10

- [ ] `ai-refinement-task-service.ts`：补齐 Admin Web 精修任务服务与请求类型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-WORKERS-CLASSICS-TEXT-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/common/ai-refinement-task-types.ts`、`kuzhambu-apps/admin-web/src/pages/classics/common/ai-refinement-task-service.ts`、`kuzhambu-apps/admin-web/src/pages/classics/common/ai-refinement-task-service.test.ts`
    - 处理动作：新增 Admin Web `task/add`、`task/get`、`task/page`、`task/cancel` 的类型与服务封装
    - 验收点：前端存在稳定的 Classics AI 任务入口，字段覆盖 `capability/scope/contentType/contentId/modelId/promptVersionId/requestId/traceId/requestedBy`
    - 重要度：9/10

- [ ] `sancai-entry-panel.tsx`：接通三才图会精修任务创建与轮询
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-WORKERS-CLASSICS-TEXT-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`
    - 处理动作：在三才图会详情页接通 `translate/summary` 的任务创建与轮询，并在任务成功后刷新候选与详情
    - 验收点：页面可以先创建 `translate` 与 `summary` 任务，再在任务成功后看到候选并刷新 `translationText`、`translationStatus`、`summary`
    - 重要度：10/10

- [ ] `wangqi-page.tsx/ming-customs-page.tsx`：接通汪耆与明俗 summary 任务创建与轮询
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-WORKERS-CLASSICS-TEXT-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.test.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.test.tsx`
    - 处理动作：在汪耆文稿和明代风俗详情页接通 `summary` 的任务创建与轮询，并在候选应用后刷新详情
    - 验收点：页面可以先创建 `summary` 任务，再在任务成功后看到候选并刷新 `WANGQI_DOCUMENT.summary` 与 `MING_CUSTOMS.summary`
    - 重要度：9/10

- [ ] `Implementation Coverage/RUNBOOK`：执行全域验证并完成文档收口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-WORKERS-CLASSICS-TEXT-CLOSURE.md`、`docs/00-governance/TODO-RULES.md`、`docs/40-readiness/PR-WORKFLOW.md`
    - 范围对象：`docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`、`docs/40-readiness/WORKERS-IMPLEMENTATION-COVERAGE.md`、`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-AI-WORKERS-CLASSICS-TEXT-CLOSURE.md`
    - 处理动作：执行 `kuzhambu-servers`、`kuzhambu-apps`、`kuzhambu-workers` 全域 `format/lint/test/build`，更新 Implementation Coverage，并在任务完成后清理 RUNBOOK
    - 验收点：完成 servers/apps/workers 全域验证，Coverage 文档更新到真实闭环状态，已完成且无剩余价值的 RUNBOOK 被删除
    - 重要度：10/10

## 待讨论项
