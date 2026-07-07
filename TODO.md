# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `classics/sancai-lifecycle-application`：补齐三才条目生命周期状态流转与落库
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-LIFECYCLE-EDIT-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiEntryStatusCommand.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/model/enums/SancaiEntryLifecycleStatus.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiRepository.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiRepositoryImpl.java`
    - 处理动作：实现 `DRAFT -> PUBLISHED`、`PUBLISHED -> ARCHIVED`、`ARCHIVED -> PUBLISHED` 三条允许流转，并拒绝 `PUBLISHED/ARCHIVED -> DRAFT`。
    - 验收点：生命周期变更成功时写入 `classics_sancai_entry.lifecycle_status/content_updated_at/current_version_id/current_version_no/current_versioned_at`，并且新增正式版本快照中的 `lifecycleStatus` 与主表一致。
    - 重要度：10/10

- [ ] `classics/sancai-lifecycle-admin-api`：暴露单条生命周期变更 Admin 接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-LIFECYCLE-EDIT-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/SancaiAdminController.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiEntryRequest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiInterfaceAssembler.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/SancaiAdminControllerTest.java`
    - 处理动作：新增 `POST /api/classics/sancai/entries/lifecycle/change`，Controller 只读取 `id` 和 `lifecycleStatus` 并调用应用层生命周期入口。
    - 验收点：接口使用 `classics:sancai:edit` 权限，接口测试覆盖路径、请求体字段、权限口径和成功返回。
    - 重要度：10/10

- [ ] `classics/sancai-lifecycle-application-test`：锁定生命周期应用层规则
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-LIFECYCLE-EDIT-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/SancaiApplicationServiceImplTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/support/FakeSancaiRepositorySupport.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceImplTest.java`
    - 处理动作：补充生命周期成功流转、非法流转、权限失败和版本快照一致性的应用层测试。
    - 验收点：三条允许流转成功，`PUBLISHED -> DRAFT` 与 `ARCHIVED -> DRAFT` 失败，无编辑权限失败，成功用例断言主表状态和新增版本快照一致。
    - 重要度：9/10

- [ ] `admin-web/sancai-lifecycle-service`：新增前端生命周期 service 契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-LIFECYCLE-EDIT-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-service-contract.test.ts`
    - 处理动作：新增 `SancaiEntryLifecycleStatus`、`SancaiEntryLifecycleCommand` 和 `changeLifecycleStatus`，并保持 `SancaiEntryRecord.lifecycleStatus` 类型兼容。
    - 验收点：contract test 精确断言 `/classics/sancai/entries/lifecycle/change` 路径和 `{ id, lifecycleStatus }` 请求体，不复用完整条目编辑请求。
    - 重要度：9/10

- [ ] `admin-web/sancai-lifecycle-controls`：补齐三才条目列表生命周期操作
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-LIFECYCLE-EDIT-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-list.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.test.tsx`
    - 处理动作：在条目操作列按当前状态展示 `发布`、`归档`、`恢复发布` 单条动作，并接入 `useKuzhambuConfirm` 确认弹窗。
    - 验收点：动作位于 `查看` 后、`删除` 前；无编辑权限禁用；确认后调用 lifecycle service；成功后刷新列表、详情和版本历史并显示对应成功提示。
    - 重要度：10/10

- [ ] `git/main-sync`：同步最新 main 分支代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`feat/classics-lifecycle-edit-loop` 分支、`main` 分支
    - 处理动作：生命周期后端、前端和测试改动完成后，将最新 `main` 同步到当前功能分支并处理冲突。
    - 验收点：功能分支包含最新 `main`，生命周期相关改动仍保留，冲突处理不引入非任务改动。
    - 重要度：9/10

- [ ] `classics/lifecycle-final-validation`：运行同步 main 后的最终验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-LIFECYCLE-EDIT-LOOP.md`
    - 范围对象：`kuzhambu-servers/.mvn/maven.config`、`kuzhambu-servers/pom.xml`、`kuzhambu-apps/package.json`、`kuzhambu-apps/admin-web/package.json`
    - 处理动作：在同步最新 `main` 后，按 RUNBOOK 执行后端 formatter、Spotless、Checkstyle、测试和前端 format、lint、test。
    - 验收点：后端 Maven 验证和前端 npm 验证通过；若失败，TODO 收窄为明确剩余失败项。
    - 重要度：10/10

- [ ] `classics/lifecycle-doc-closure`：更新覆盖文档并清理 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-LIFECYCLE-EDIT-LOOP.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-CLASSICS-LIFECYCLE-EDIT-LOOP.md`、`TODO.md`
    - 处理动作：最终验证通过后更新 Implementation Coverage、删除临时 RUNBOOK，并按完成情况删除或收窄 TODO。
    - 验收点：`CLASSICS-IMPLEMENTATION-COVERAGE.md` 标记生命周期闭环已完成，RUNBOOK 不再保留，`TODO.md` 不记录已完成任务。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
