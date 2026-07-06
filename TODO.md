# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `AI Platform admin entry`：补齐 Platform AI admin HTTP 入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-PLATFORM-ENTRY-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/platform/controller/PlatformAiController.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/platform/controller/request/PlatformAiRequests.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/platform/controller/response/PlatformAiResponses.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/platform/assembler/PlatformAiInterfaceAssembler.java`
    - 处理动作：新增 `/api/ai/platform/prompt-suggestion` 与 `/api/ai/platform/version-summary` admin 接口并完成请求响应协议转换。
    - 验收点：两个接口分别委托 `PlatformAiApplicationService#buildPromptSuggestion` 和 `PlatformAiApplicationService#summarizeVersion`，权限分别为 `ai:prompt:edit` 与 `ai:prompt:view`。
    - 重要度：9/10

- [ ] `AI Platform test coverage`：锁定 Platform AI 入口测试覆盖
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-PLATFORM-ENTRY-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/platform/support/PlatformAiWorkerUsecaseResolverTest.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/platform/service/impl/PlatformAiApplicationServiceImplTest.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/test/java/com/thundax/kuzhambu/ai/interfaces/admin/platform/controller/PlatformAiControllerTest.java`
    - 处理动作：新增 resolver、application service 和 controller 单元测试锁定 Platform AI 调用链。
    - 验收点：测试精确校验 `operation / workerPath / capability / defaultCreateCandidate`、Java invocation 命令字段、HTTP 路径、权限和 service 委托。
    - 重要度：8/10

- [ ] `AI Platform readiness closure`：更新 coverage 并清理 RUNBOOK 收口
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-AI-PLATFORM-ENTRY-CLOSURE.md`
    - 范围对象：`docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-AI-PLATFORM-ENTRY-CLOSURE.md`、`TODO.md`
    - 处理动作：将 Platform 两项移动到已完成 coverage，并在 PR 收口前删除无剩余价值的 RUNBOOK 与已完成 TODO。
    - 验收点：`AI-IMPLEMENTATION-COVERAGE.md` 的未完成表仅保留真实剩余项，RUNBOOK 在任务关闭时清理，`TODO.md` 不保留已完成历史。
    - 重要度：8/10

## 待审阅任务项

## 待讨论项
