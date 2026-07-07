# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `classics batch candidate result model`：补齐批量结果候选维度字段
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-BATCH-CANDIDATE-GOVERNANCE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/result/ClassicsBatchOperationItemResult.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/common/response/ClassicsBatchOperationResponse.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/content/ClassicsContentAdminControllerTest.java`
    - 处理动作：为 Classics 批量结果模型和 interface response 增加 `candidateId`、`objectId`、`capability` 输出。
    - 验收点：候选批量响应能输出候选维度字段，现有批量分享和批量可见性响应不被破坏。
    - 重要度：9/10

- [ ] `classics batch candidate commands`：新增批量候选 command 和 service 入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-BATCH-CANDIDATE-GOVERNANCE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/ClassicsContentApplicationService.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/command/AiCandidateBatchApplyContentCommand.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/command/AiCandidateBatchRejectContentCommand.java`
    - 处理动作：新增批量应用和批量拒绝 command，并在 `ClassicsContentApplicationService` 暴露批量方法。
    - 验收点：interface assembler 可构造批量 command，service interface 提供 `applyAiCandidates` 和 `rejectAiCandidates`。
    - 重要度：9/10

- [ ] `classics batch candidate application`：实现批量候选应用和拒绝
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-BATCH-CANDIDATE-GOVERNANCE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/service/AiCandidateDomainService.java`
    - 处理动作：逐条复用单候选应用和 AI 候选拒绝语义，形成允许部分成功的批量处理。
    - 验收点：测试覆盖成功、权限失败、目标不匹配、非 `PENDING` 拒绝和 Sancai `objectId` 校验。
    - 重要度：10/10

- [ ] `classics batch candidate admin api`：暴露批量候选治理接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-BATCH-CANDIDATE-GOVERNANCE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/ClassicsContentAdminController.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/request/ClassicsContentRequest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/assembler/ClassicsContentInterfaceAssembler.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/content/ClassicsContentAdminControllerTest.java`
    - 处理动作：新增 `/api/classics/content/ai-candidates/batch/apply` 和 `/api/classics/content/ai-candidates/batch/reject`。
    - 验收点：controller test 锁定路径、权限、请求字段、响应字段、空 items 和重复 candidateId 校验。
    - 重要度：9/10

- [ ] `admin-web batch candidate service`：补齐前端批量候选类型和 service
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-BATCH-CANDIDATE-GOVERNANCE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/common/classics-content-types.ts`、`kuzhambu-apps/admin-web/src/pages/classics/common/classics-content-service.ts`、`kuzhambu-apps/admin-web/src/pages/classics/common/classics-content-service-contract.test.ts`、`kuzhambu-apps/admin-web/src/pages/classics/common/ai-candidate-types.ts`
    - 处理动作：新增批量候选请求类型、结果字段和 `applyAiCandidatesBatch`、`rejectAiCandidatesBatch` service。
    - 验收点：service contract 锁定两个批量接口路径和完整 body。
    - 重要度：8/10

- [ ] `admin-web batch candidate drawer`：实现通用批量候选治理抽屉
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-BATCH-CANDIDATE-GOVERNANCE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-batch-drawer.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-batch-drawer.test.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-payload-editor.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/common/ai-candidate-service.ts`、`kuzhambu-apps/admin-web/src/pages/classics/common/ai-candidate-types.ts`
    - 处理动作：实现 `AI 候选批量治理` 抽屉、候选加载、多选、payload 校验、批量应用、批量拒绝和结果明细展示。
    - 验收点：测试覆盖候选加载、选择、payload 校验失败、批量应用结果、批量拒绝确认和 `objectId` 提交。
    - 重要度：10/10

- [ ] `admin-web sancai batch candidate entry`：接入三才图会批量候选治理入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-BATCH-CANDIDATE-GOVERNANCE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`
    - 处理动作：在条目列表批量操作区新增 `批量候选治理` 按钮并接入 `AiCandidateBatchDrawer`。
    - 验收点：未选条目或无 `classics:sancai:edit` 时按钮禁用，选择条目后可打开抽屉并处理候选。
    - 重要度：8/10

- [ ] `admin-web wangqi batch candidate entry`：接入王圻文档批量候选治理入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-BATCH-CANDIDATE-GOVERNANCE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.test.tsx`
    - 处理动作：在文档列表批量操作区新增 `批量候选治理` 按钮并接入 `AiCandidateBatchDrawer`。
    - 验收点：未选文档或无 `classics:wangqi:edit` 时按钮禁用，批量拒绝完成后展示成功数和失败数。
    - 重要度：8/10

- [ ] `admin-web ming customs batch candidate entry`：接入明代习俗批量候选治理入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-BATCH-CANDIDATE-GOVERNANCE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.test.tsx`
    - 处理动作：在习俗列表批量操作区新增 `批量候选治理` 按钮并接入 `AiCandidateBatchDrawer`。
    - 验收点：未选习俗或无 `classics:mingcustoms:edit` 时按钮禁用，批量应用完成后展示成功数和失败数。
    - 重要度：8/10

- [ ] `main sync before validation`：最终验证前同步 main 分支代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`feat/classics-batch-candidate-governance` 分支
    - 处理动作：在最终验证前同步最新 `origin/main` 到当前特性分支并解决冲突。
    - 验收点：当前分支包含最新 `origin/main`，同步冲突已解决且未混入无关修改。
    - 重要度：10/10

- [ ] `classics batch candidate final validation`：执行同步 main 后的最终验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-BATCH-CANDIDATE-GOVERNANCE.md`
    - 范围对象：`kuzhambu-servers/`、`kuzhambu-apps/admin-web/`
    - 处理动作：在同步最新 `origin/main` 后运行 RUNBOOK 指定的后端 formatter、静态检查、测试和前端 format、lint、test、build。
    - 验收点：后端 Maven 检查和测试通过，前端 format/lint/test/build 通过，失败时收窄到具体未完成任务。
    - 重要度：10/10

- [ ] `classics implementation coverage closeout`：更新 Classics 完成状态并清理 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-BATCH-CANDIDATE-GOVERNANCE.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-CLASSICS-BATCH-CANDIDATE-GOVERNANCE.md`、`TODO.md`
    - 处理动作：在能力完成且验证通过后更新 Implementation Coverage 为已完成，并删除已无继续价值的 RUNBOOK。
    - 验收点：覆盖清单中“跨内容批量候选治理”改为已完成，B2 改为已完成，RUNBOOK 被清理，相关 TODO 删除或收窄。
    - 重要度：10/10

## 待审阅任务项


## 待讨论项
