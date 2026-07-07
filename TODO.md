# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

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
