# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `ai-invocation-query`：补齐 AI 调用记录查询能力
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-ADMIN-GOVERNANCE.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/repository/AiInvocationRepository.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/invocation/persistence/mapper/AiInvocationMapper.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/invocation/repository/impl/AiInvocationRepositoryImpl.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/test/java/com/thundax/kuzhambu/ai/infra/invocation/repository/impl/AiInvocationRepositoryIT.java`
    - 处理动作：补齐调用记录筛选分页和 summary 聚合读取能力。
    - 验收点：可按 `scope`、`capability`、`contentType`、`contentId`、`status`、`serviceRole`、`modelName`、`fallbackUsed`、`requestedAt` 范围读取调用记录，并可聚合统计调用数、失败数、耗时、成本和能力排行。
    - 重要度：10/10

- [ ] `ai-invocation-admin-api`：暴露 AI 调用记录管理端接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-ADMIN-GOVERNANCE.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/AiInvocationController.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/request/AiInvocationRequests.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/response/AiInvocationResponses.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/assembler/AiInvocationInterfaceAssembler.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/test/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/AiInvocationControllerTest.java`
    - 处理动作：新增 `POST /api/ai/invocation/call/page` 和 `POST /api/ai/invocation/call/summary`。
    - 验收点：两个接口使用 `ai:invocation:view` 权限，返回字段与 RUNBOOK 的 `CallRecordResponse`、`CallSummaryResponse` 一致。
    - 重要度：10/10

- [ ] `ai-action-status-query`：补齐 AI 动作状态批量读取能力
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-ADMIN-GOVERNANCE.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/capability/repository/AiCapabilityRepository.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/capability/persistence/mapper/AiCapabilityMapper.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/capability/repository/impl/AiCapabilityRepositoryImpl.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/test/java/com/thundax/kuzhambu/ai/infra/capability/repository/impl/AiCapabilityRepositoryIT.java`
    - 处理动作：补齐按 `scope`、`capability`、`available` 查询 `ai_action_status` 列表的能力。
    - 验收点：动作状态列表可按三个筛选条件读取，且不新增数据库表字段。
    - 重要度：9/10

- [ ] `ai-action-status-admin-api`：暴露 AI 动作状态批量读取接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-ADMIN-GOVERNANCE.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/config/controller/AiConfigController.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/config/controller/request/AiConfigRequests.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/config/controller/response/AiConfigResponses.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/config/assembler/AiConfigInterfaceAssembler.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/test/java/com/thundax/kuzhambu/ai/interfaces/admin/config/controller/AiConfigControllerTest.java`
    - 处理动作：新增 `POST /api/ai/config/action/status/list`。
    - 验收点：接口使用 `ai:config:view` 权限，返回 `scope`、`capability`、`available`、`unavailableReason`、`checkedAt`。
    - 重要度：9/10

- [ ] `ai-menu-route`：补齐 AI 治理菜单、路由和图标
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-ADMIN-GOVERNANCE.md`
    - 范围对象：`db/data-source/system.json`、`db/data/system.sql`、`kuzhambu-apps/admin-web/src/router/index.tsx`、`kuzhambu-apps/admin-web/src/layouts/admin-layout.tsx`、`kuzhambu-servers/starter/kuzhambu-admin-starter/src/test/java/com/thundax/kuzhambu/starter/admin/AdminStarterArchitectureTest.java`
    - 处理动作：补齐 AI 一级菜单下 6 个治理页面入口并注册前端路由。
    - 验收点：`/ai/services`、`/ai/models`、`/ai/capability-mappings`、`/ai/prompts`、`/ai/invocations`、`/ai/action-status` 均可从菜单进入且图标不显示配置错误。
    - 重要度：10/10

- [ ] `ai-services-page`：实现 AI 服务配置页
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-ADMIN-GOVERNANCE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/ai/services/services-page.tsx`、`kuzhambu-apps/admin-web/src/pages/ai/services/services-service.ts`、`kuzhambu-apps/admin-web/src/pages/ai/services/services-types.ts`、`kuzhambu-apps/admin-web/src/pages/ai/services/services-page.css`、`kuzhambu-apps/admin-web/src/pages/ai/services/services-page.test.tsx`
    - 处理动作：实现主服务和备用服务的查看、编辑、启停和保存。
    - 验收点：页面展示 `PRIMARY` 与 `BACKUP` 配置卡，Drawer 保存不展示明文 AI Key，并按 `ai:config:edit` 控制操作。
    - 重要度：9/10

- [ ] `ai-models-page`：实现 AI 模型配置页
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-ADMIN-GOVERNANCE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/ai/models/models-page.tsx`、`kuzhambu-apps/admin-web/src/pages/ai/models/models-service.ts`、`kuzhambu-apps/admin-web/src/pages/ai/models/models-types.ts`、`kuzhambu-apps/admin-web/src/pages/ai/models/models-page.css`、`kuzhambu-apps/admin-web/src/pages/ai/models/models-page.test.tsx`
    - 处理动作：实现模型筛选、列表、新增、编辑、启停、删除、检测和检测历史。
    - 验收点：模型表格和 Drawer 控件符合 RUNBOOK，删除被映射模型时展示后端原因。
    - 重要度：9/10

- [ ] `ai-capability-mappings-page`：实现 AI 能力映射页
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-ADMIN-GOVERNANCE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/ai/capability-mappings/capability-mappings-page.tsx`、`kuzhambu-apps/admin-web/src/pages/ai/capability-mappings/capability-mappings-service.ts`、`kuzhambu-apps/admin-web/src/pages/ai/capability-mappings/capability-mappings-types.ts`、`kuzhambu-apps/admin-web/src/pages/ai/capability-mappings/capability-mappings-page.css`、`kuzhambu-apps/admin-web/src/pages/ai/capability-mappings/capability-mappings-page.test.tsx`
    - 处理动作：实现 `scope + capability -> modelId` 映射配置和启停。
    - 验收点：Drawer 只展示启用模型选项，并展示模型能力标签与能力所需标签的匹配提示。
    - 重要度：9/10

- [ ] `ai-prompts-page`：实现 AI 提示词版本页
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-ADMIN-GOVERNANCE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/ai/prompts/prompts-page.tsx`、`kuzhambu-apps/admin-web/src/pages/ai/prompts/prompts-service.ts`、`kuzhambu-apps/admin-web/src/pages/ai/prompts/prompts-types.ts`、`kuzhambu-apps/admin-web/src/pages/ai/prompts/prompts-page.css`、`kuzhambu-apps/admin-web/src/pages/ai/prompts/prompts-page.test.tsx`
    - 处理动作：实现模板保存、变量校验、版本列表、版本对比、回滚和优化建议确认应用。
    - 验收点：保存或回滚后刷新当前版本、变量列表和动作状态，优化建议必须确认后才应用为新版本。
    - 重要度：10/10

- [ ] `ai-invocations-page`：实现 AI 调用统计页
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-ADMIN-GOVERNANCE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/ai/invocations/invocations-page.tsx`、`kuzhambu-apps/admin-web/src/pages/ai/invocations/invocations-service.ts`、`kuzhambu-apps/admin-web/src/pages/ai/invocations/invocations-types.ts`、`kuzhambu-apps/admin-web/src/pages/ai/invocations/invocations-page.css`、`kuzhambu-apps/admin-web/src/pages/ai/invocations/invocations-page.test.tsx`
    - 处理动作：实现调用 summary、能力排行、调用记录分页和详情 Drawer。
    - 验收点：页面可按周期和筛选条件展示调用数、成功数、失败数、平均耗时、成本、能力排行和调用详情。
    - 重要度：9/10

- [ ] `ai-action-status-page`：实现 AI 动作状态页
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-ADMIN-GOVERNANCE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/ai/action-status/action-status-page.tsx`、`kuzhambu-apps/admin-web/src/pages/ai/action-status/action-status-service.ts`、`kuzhambu-apps/admin-web/src/pages/ai/action-status/action-status-types.ts`、`kuzhambu-apps/admin-web/src/pages/ai/action-status/action-status-page.css`、`kuzhambu-apps/admin-web/src/pages/ai/action-status/action-status-page.test.tsx`
    - 处理动作：实现动作状态矩阵、不可用原因展示、单项刷新和刷新全部。
    - 验收点：页面按 `scope`、`capability`、`available` 筛选动作状态，无 `ai:config:edit` 时禁用刷新操作。
    - 重要度：9/10

- [ ] `branch-main-sync`：同步 main 分支代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：当前分支 `feat/ai-admin-governance` 与 `main`
    - 处理动作：完成实现任务后同步 `main` 最新代码到当前分支并解决冲突。
    - 验收点：当前分支包含 `main` 最新代码，冲突已解决，且后续最终验证基于同步后的代码执行。
    - 重要度：10/10

- [ ] `ai-admin-verification`：执行 AI 治理后台闭环验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-ADMIN-GOVERNANCE.md`
    - 范围对象：`scripts/generate-system-data-sql.ts`、`kuzhambu-servers/`、`kuzhambu-apps/`
    - 处理动作：在同步 `main` 后运行菜单生成校验、后端 Maven 校验和 admin-web 前端校验。
    - 验收点：菜单 SQL check、后端 spotless/checkstyle/test、前端 format/lint/test/build 均通过或记录明确阻塞原因。
    - 重要度：10/10

- [ ] `ai-implementation-coverage`：更新 AI Implementation Coverage
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：同步记录 AI 管理端治理闭环的最终覆盖状态和验证结果。
    - 验收点：覆盖矩阵明确标注服务配置、模型配置、能力映射、提示词版本、调用统计和动作状态页面已完成。
    - 重要度：8/10

- [ ] `runbook-cleanup`：清理 AI 治理 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-AI-ADMIN-GOVERNANCE.md`
    - 处理动作：任务完成并同步覆盖文档后删除临时 RUNBOOK。
    - 验收点：PR 收口前仓库不再保留已完成任务的临时 RUNBOOK。
    - 重要度：8/10

## 待审阅任务项

## 待讨论项
