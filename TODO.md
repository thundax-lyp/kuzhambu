# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `01-ai-backend-config-model`：01 后端服务配置和模型检测契约验收
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-RUNTIME-ACCEPTANCE.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/config/controller/AiConfigController.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/model/command/AiModelCheckCommand.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/model/persistence/mapper/AiModelMapper.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/model/persistence/dataobject/AiServiceConfigDO.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/model/persistence/dataobject/AiModelCheckRecordDO.java`
    - 处理动作：验收 PRIMARY/BACKUP 服务配置读写、PRIMARY 模型登记、模型检测和检测历史。
    - 验收点：服务配置响应不暴露明文 API Key，`ai_service_config`、`ai_model`、`ai_model_check_record` 字段与 RUNBOOK 要求一致。
    - 重要度：10/10

- [ ] `02-ai-backend-mapping-prompt-status`：02 后端能力映射、提示词变量和动作状态验收
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-RUNTIME-ACCEPTANCE.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/capability/command/AiCapabilityMappingSaveCommand.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/prompt/command/PromptTemplateSaveCommand.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/capability/persistence/mapper/AiCapabilityMapper.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/prompt/persistence/mapper/PromptMapper.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/prompt/controller/PromptController.java`
    - 处理动作：验收 `classics + summary` 正例映射、缺标签模型负例、提示词保存、变量成功/失败校验和动作状态刷新。
    - 验收点：`ai_capability_mapping`、`ai_prompt_template`、`ai_prompt_version`、`ai_prompt_variable`、`ai_action_status` 字段与 RUNBOOK 要求一致。
    - 重要度：10/10

- [ ] `03-ai-backend-invocation-stats`：03 后端精修任务、调用记录和统计验收
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-RUNTIME-ACCEPTANCE.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/AiRefinementTaskController.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/AiInvocationController.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/command/AiInvokeCommand.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/result/AiInvokeResult.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/invocation/persistence/mapper/AiInvocationMapper.java`
    - 处理动作：创建 `classics + summary` 精修任务并验收任务终态、调用统计、调用分页和数据库追溯。
    - 验收点：`ai_refinement_task.call_id`、`ai_call_record.call_id`、`ai_candidate.call_id` 可互相追溯，调用详情包含模型、提示词版本、耗时、失败阶段和降级标记。
    - 重要度：10/10

- [ ] `04-admin-ai-governance-pages`：04 Admin AI 治理页面控件验收
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-RUNTIME-ACCEPTANCE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/ai/services/services-page.tsx`、`kuzhambu-apps/admin-web/src/pages/ai/model-configs/model-configs-page.tsx`、`kuzhambu-apps/admin-web/src/pages/ai/capability-mappings/capability-mappings-page.tsx`、`kuzhambu-apps/admin-web/src/pages/ai/prompts/prompts-page.tsx`、`kuzhambu-apps/admin-web/src/pages/ai/action-status/action-status-page.tsx`
    - 处理动作：按 RUNBOOK 验收服务卡片、模型表单、能力映射表单、提示词编辑器、变量校验控件和动作状态筛选刷新控件。
    - 验收点：每个页面控件触发对应 API，表单校验、保存成功、保存失败、刷新、筛选和错误展示均有证据。
    - 重要度：9/10

- [ ] `05-admin-classics-summary-invocation`：05 Admin Classics summary 精修和调用统计页面验收
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-RUNTIME-ACCEPTANCE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/common/ai-refinement-task-service.ts`、`kuzhambu-apps/admin-web/src/pages/classics/common/ai-candidate-service.ts`、`kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/ai/invocations/invocations-page.tsx`
    - 处理动作：在三才图会条目详情触发 summary 精修，验收任务状态、候选面板、调用统计和调用详情。
    - 验收点：前端任务状态、候选结果和调用统计能用同一 `taskId`、`candidateId`、`callId` 串联。
    - 重要度：9/10

- [ ] `06-ai-runtime-smoke-evidence`：06 沉淀 AI 运行时冒烟证据
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-RUNTIME-ACCEPTANCE.md`
    - 范围对象：`docs/40-readiness/AI-RUNTIME-SMOKE-EVIDENCE.md`、`docs/30-designs/RUNBOOK-AI-RUNTIME-ACCEPTANCE.md`
    - 处理动作：新增脱敏冒烟证据文档，记录 RUNBOOK 要求的真实运行证据。
    - 验收点：证据文档包含 worker health、登录脱敏 token、配置、模型检测、映射、提示词变量、动作状态、任务终态和调用统计摘要，且不包含 API Key、完整 token、完整 prompt 或业务敏感输入。
    - 重要度：10/10

- [ ] `07-ai-runtime-verification`：07 执行 AI 验收验证命令
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-RUNTIME-ACCEPTANCE.md`
    - 范围对象：`kuzhambu-servers/pom.xml`、`kuzhambu-apps/package.json`、`kuzhambu-apps/admin-web/package.json`、`kuzhambu-workers/pyproject.toml`
    - 处理动作：运行 RUNBOOK 窄集验证并记录结果。
    - 验收点：窄集验证命令、结果和任何失败归因记录到 readiness 证据或 PR 描述。
    - 重要度：9/10

- [ ] `08-sync-main-before-close`：08 收口前同步 main 分支代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/PR-RULES.md`
    - 范围对象：`feat/ai-runtime-acceptance-runbook`、`origin/main`
    - 处理动作：在收口前拉取远端并将当前分支同步到最新 `origin/main`。
    - 验收点：`git status --short --branch` 显示当前分支基于最新 `origin/main`，且无合并冲突。
    - 重要度：8/10

- [ ] `09-ai-runtime-final-coverage-cleanup`：09 同步全量验证、更新覆盖清单并清理 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`、`docs/40-readiness/AI-RUNTIME-SMOKE-EVIDENCE.md`、`docs/30-designs/RUNBOOK-AI-RUNTIME-ACCEPTANCE.md`、`TODO.md`
    - 处理动作：同步 main 后运行 PR 前全量验证，更新 AI Implementation Coverage，保留 readiness 证据，删除临时 RUNBOOK，并从 TODO 中删除已完成任务。
    - 验收点：全量验证结果已记录，AI coverage 反映运行时验收结果，PR 收口时无仍有价值的临时 RUNBOOK，TODO 只保留未完成任务。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
