# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `classics-sancai-version-contract`：补齐三才图会版本接口数据契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-VERSION-RESTORE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiEntryResponse.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiEntryVersionRequest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiEntryVersionResponse.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiInterfaceAssembler.java`
    - 处理动作：补齐条目版本状态字段、版本请求响应模型和版本响应转换。
    - 验收点：三才图会条目响应包含版本指针与 `versionDirty`，版本响应字段对齐 `ClassicsContentVersion`。
    - 重要度：9/10

- [ ] `classics-sancai-version-restore-core`：实现三才图会历史版本恢复核心能力
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-VERSION-RESTORE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/support/SancaiEntryVersionRestorer.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiRepository.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiRepositoryImpl.java`
    - 处理动作：新增 Restorer、接入 `SANCAI_ENTRY` 恢复分发，并用目标卷 `max(priority)+1` 写入恢复排序。
    - 验收点：`restoreHistoryVersion` 支持 `SANCAI_ENTRY`，恢复写入内容字段和状态字段并生成 `HISTORY_RESTORED` 新版本。
    - 重要度：10/10

- [ ] `classics-sancai-version-admin-api`：开放三才图会版本列表、详情和恢复接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-VERSION-RESTORE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/SancaiAdminController.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/SancaiAdminControllerTest.java`
    - 处理动作：增加 `entries/versions/list|get|reset` 管理接口并补归属校验。
    - 验收点：接口权限、路径、请求体、版本归属校验和 reset 调用链均被 controller 测试覆盖。
    - 重要度：9/10

- [ ] `classics-sancai-version-backend-tests`：锁定三才图会恢复后端行为
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-VERSION-RESTORE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/support/SancaiEntryVersionRestorerTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImplTest.java`
    - 处理动作：补 Restorer 和 Content service 分发的单元测试。
    - 验收点：测试覆盖快照归属、非法快照、条目不存在、目标卷末尾 priority、`HISTORY_RESTORED` 新版本和版本指针回写。
    - 重要度：9/10

- [ ] `admin-web-sancai-version-service`：接入三才图会版本前端类型和服务
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-VERSION-RESTORE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-service-contract.test.ts`
    - 处理动作：补版本字段、版本快照类型、详情 GET 和版本 list/get/reset 服务方法。
    - 验收点：service contract 测试覆盖新增路径、请求体和恢复后详情刷新入口。
    - 重要度：8/10

- [ ] `admin-web-sancai-version-panel`：新增三才图会版本历史对比面板
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-VERSION-RESTORE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-version-history-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.css`
    - 处理动作：实现版本列表、快照解析、当前/历史字段对比和恢复按钮状态。
    - 验收点：面板能展示版本列表、标记差异字段，并在快照不可解析时禁用恢复。
    - 重要度：8/10

- [ ] `admin-web-sancai-version-integration`：把版本恢复闭环接入三才图会条目抽屉
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-VERSION-RESTORE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-model.tsx`
    - 处理动作：接入版本查询、版本选择、恢复确认、恢复后详情刷新和成功 message box。
    - 验收点：恢复后抽屉保持打开、表单刷新为恢复内容，并弹窗说明已生成新正式版本且条目移动到卷末尾。
    - 重要度：9/10

- [ ] `admin-web-sancai-version-tests`：覆盖三才图会版本恢复前端交互
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-VERSION-RESTORE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.test.tsx`、`kuzhambu-apps/admin-web/e2e/classics/sancai/sancai.spec.ts`
    - 处理动作：补组件测试和 E2E 场景验证版本查看、对比、恢复与成功提示。
    - 验收点：测试断言恢复确认、reset 调用、当前条目刷新、message box 文案和版本闭环 E2E 均通过。
    - 重要度：8/10

- [ ] `classics-sancai-version-closeout`：完成三才图会版本恢复文档收口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-VERSION-RESTORE.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-VERSION-RESTORE.md`、`TODO.md`
    - 处理动作：完成验证后更新覆盖度、删除 RUNBOOK 并清理已完成 TODO。
    - 验收点：coverage 标记三才图会版本历史/对比/恢复完成，RUNBOOK 删除，TODO 仅保留未关闭任务。
    - 重要度：7/10

## 待审阅任务项

## 待讨论项
