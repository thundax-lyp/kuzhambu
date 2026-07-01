# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `SancaiVisualAsset/SancaiAssetRepository/SancaiAssetRepositoryImpl`：收紧视觉资产实体与仓储字段写回规则
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/model/entity/SancaiVisualAsset.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiAssetRepository.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiAssetRepositoryImpl.java`
    - 处理动作：按 RUNBOOK 6.1 固定 `image_analysis_markdown`、`fusion_description`、`visual_description`、`generated_image_storage_object_id` 的字段分工
    - 验收点：仓储更新路径不再允许跨能力污染字段，且 artifact id 不会写入正式生成图字段
    - 重要度：10/10

- [ ] `ai.py/events.py/ai_routes.py`：收紧 workers 最终态 schema 与 SSE 口径
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-workers/src/kuzhambu_workers/schemas/ai.py`、`kuzhambu-workers/src/kuzhambu_workers/streaming/events.py`、`kuzhambu-workers/src/kuzhambu_workers/api/ai_routes.py`
    - 处理动作：统一 workers 同步响应与 SSE `completed` 事件的最终态字段集合和错误字段语义
    - 验收点：`status`、`result`、`artifactReference`、`failureStage`、`errorType`、`errorMessage` 在同步与流式响应中同构
    - 重要度：10/10

- [ ] `sancai-entry-panel/sancai-entry-model/use-sancai-entry-panel-state/sancai-entry-service`：补齐单条视觉资产四类 AI 入口
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-model.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/hooks/use-sancai-entry-panel-state.ts`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`
    - 处理动作：在三才条目面板中统一接入 `image_analysis`、`fusion`、`visual`、`image_gen` 的单条任务入口
    - 验收点：页面可携带 `entryId`、`visualAssetId`、`capability` 发起四类任务，且入口不依赖临时调试路径
    - 重要度：9/10

- [ ] `AiRefinementController/AiRefinementRequests/AiRefinementInterfaceAssembler`：补齐三才视觉资产 AI 请求上下文
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/AiRefinementController.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/request/AiRefinementRequests.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/assembler/AiRefinementInterfaceAssembler.java`
    - 处理动作：让 Java AI 入口稳定接收三才视觉资产上下文并映射四类 capability 请求
    - 验收点：AI 接口请求体可稳定表达三才视觉资产任务上下文，且接口装配不丢失 capability 与对象标识
    - 重要度：9/10

- [ ] `SancaiAssetApplicationService/SancaiAssetApplicationServiceImpl/ClassicsContentApplicationServiceImpl/SancaiAssetRepositoryImpl`：补齐 fusion 正式写回链路
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/SancaiAssetApplicationService.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiAssetRepositoryImpl.java`
    - 处理动作：将 `fusion` 候选应用后的正式写回严格绑定到 `fusion_description` 并消费权重与图片理解结果
    - 验收点：`fusion` 只写 `fusion_description`，且正式写回依赖 `image_analysis_markdown + text_weight + image_weight`
    - 重要度：10/10

- [ ] `SancaiAssetApplicationServiceImpl/SancaiAssetRepository/SancaiAssetRepositoryImpl/artifact_store.py`：补齐 image_gen 新版本写回链路
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiAssetRepository.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiAssetRepositoryImpl.java`、`kuzhambu-workers/src/kuzhambu_workers/render/artifact_store.py`
    - 处理动作：将 `image_gen` 候选应用固定为正式转存并新建 visual asset version
    - 验收点：`image_gen` 应用后创建新 version、落正式 `StorageObject`、不覆盖旧 version，且业务字段不保存 artifact id
    - 重要度：10/10

- [ ] `ai-candidate-panel/ai-candidate-payload-editor/sancai-entry-panel/use-sancai-entry-panel-state`：补齐候选区编辑与接受拒绝交互
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-payload-editor.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/hooks/use-sancai-entry-panel-state.ts`
    - 处理动作：让三才候选区支持预览、编辑、接受、拒绝，并在接受后刷新正式视觉资产事实
    - 验收点：四类 capability 候选都可治理，且页面接受后能看到正式字段或版本切换结果刷新
    - 重要度：9/10

- [ ] `ClassicsContentApplicationServiceImpl/SancaiAssetApplicationServiceImpl/AiRefinementResponses`：锁定候选应用规则与失败响应
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/response/AiRefinementResponses.java`
    - 处理动作：固定四类 capability 的候选应用映射和失败返回字段
    - 验收点：`image_analysis`、`fusion`、`visual`、`image_gen` 各自只写指定正式字段或版本，失败响应稳定返回页面可读原因
    - 重要度：10/10

- [ ] `AiRefinementTaskController/AiRefinementResponses/sancai-entry-list/sancai-entry-service`：补齐批量任务编排与取消入口
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/AiRefinementTaskController.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/response/AiRefinementResponses.java`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-list.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`
    - 处理动作：为批量图片理解和批量视觉资产处理补齐 `batchId` 编排、状态查询和取消入口
    - 验收点：页面可创建批量任务、查询批量状态、取消未开始单元，且已完成结果保留
    - 重要度：9/10

- [ ] `ai_usecase_routes.py/ai.py/sse.py`：补齐 workers 批量最终态与失败聚合规则
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-workers/src/kuzhambu_workers/api/ai_usecase_routes.py`、`kuzhambu-workers/src/kuzhambu_workers/schemas/ai.py`、`kuzhambu-workers/src/kuzhambu_workers/streaming/sse.py`
    - 处理动作：让 workers 批量子单元最终态可稳定映射成功数、失败数和失败原因
    - 验收点：批量子单元失败不污染其他单元，且 Java 与前端可直接消费聚合状态
    - 重要度：9/10

- [ ] `sancai-entry-panel/use-sancai-entry-panel-state/ai-refinement-task-service`：统一失败展示与重试交互
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/hooks/use-sancai-entry-panel-state.ts`、`kuzhambu-apps/admin-web/src/pages/classics/common/ai-refinement-task-service.ts`
    - 处理动作：统一单条与批量任务的失败原因展示和重试入口
    - 验收点：页面能展示 `failureStage`、`errorType`、`errorMessage`，并可对失败任务发起重试
    - 重要度：8/10

- [ ] `admin-web 三才测试文件`：补齐单条入口、候选治理、批量状态与失败重试测试
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.test.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-service-contract.test.ts`
    - 处理动作：补齐三才页面 AI 单条入口、候选治理、批量状态和失败重试前端回归测试
    - 验收点：前端测试覆盖单条任务入口、候选接受拒绝、批量状态展示和失败重试
    - 重要度：8/10

- [ ] `Java 三才与 AI 接口测试文件`：补齐字段写回、版本追加、批量取消与接口契约测试
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/SancaiAssetApplicationServiceImplTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/SancaiAssetAdminControllerTest.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/test/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/AiInvocationControllerTest.java`
    - 处理动作：补齐 Java 侧正式字段写回、候选应用、版本追加、批量取消和接口契约测试
    - 验收点：后端测试锁定四类 capability 的正式写回规则、版本行为和批量任务契约
    - 重要度：9/10

- [ ] `workers 三才 AI 测试文件`：补齐 capability、final-state 与 artifact 元信息测试
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-workers/tests/test_ai_usecase_routes_classics.py`、`kuzhambu-workers/tests/test_ai_usecase_registry.py`、`kuzhambu-workers/tests/test_artifact_store.py`
    - 处理动作：补齐 workers 侧四类 capability、stream final-state 和 artifact 元信息测试
    - 验收点：workers 测试覆盖 `image_analysis`、`fusion`、`visual`、`image_gen` 的最终态、失败路径和产物元信息
    - 重要度：8/10

- [ ] `Implementation Coverage 文档`：同步三才 AI + Workers + Classics 闭环完成口径
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`、`docs/40-readiness/WORKERS-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：按本轮实际交付结果更新 Classics、AI、Workers 三份 Implementation Coverage 的完成口径
    - 验收点：三份覆盖文档准确反映四类 capability、候选治理、批量处理、正式写回与测试收口状态
    - 重要度：7/10

- [ ] `kuzhambu-servers/`：按 format -> checkstyle -> compile -> test 顺序完成 Java 收口验证
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/40-readiness/PR-WORKFLOW.md`、`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/`
    - 处理动作：按 Java formatter、静态检查、编译、测试顺序执行 servers 全域验证
    - 验收点：`kuzhambu-servers/` 按 `format -> checkstyle -> compile -> test` 顺序通过，且结果可写入 PR 验证记录
    - 重要度：10/10

- [ ] `kuzhambu-apps/`：按 format -> lint -> build -> test 顺序完成前端收口验证
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/40-readiness/PR-WORKFLOW.md`、`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/`
    - 处理动作：按前端 formatter、lint、build、test 顺序执行 apps 全域验证
    - 验收点：`kuzhambu-apps/` 按 `format -> lint -> build -> test` 顺序通过，且结果可写入 PR 验证记录
    - 重要度：10/10

- [ ] `kuzhambu-workers/`：按 format -> lint -> test 顺序完成 workers 收口验证
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/40-readiness/PR-WORKFLOW.md`、`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-workers/`
    - 处理动作：按 Ruff format、Ruff check、pytest 顺序执行 workers 全域验证
    - 验收点：`kuzhambu-workers/` 按 `format -> lint -> test` 顺序通过，且结果可写入 PR 验证记录
    - 重要度：10/10

- [ ] `RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`：在本轮需求关闭后清理执行手册
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 范围对象：`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 处理动作：在功能、测试、文档和验证全部完成后删除本轮 RUNBOOK
    - 验收点：PR 收口前该 RUNBOOK 已清理，且 TODO 仅保留真实剩余未完成任务
    - 重要度：8/10

## 待讨论项
