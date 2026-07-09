# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `2. 前端目录数据传递`：把门类和卷目录数据传入 Sancai 条目编辑抽屉
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-EDIT-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-model.tsx`
    - 处理动作：从页面向条目面板和编辑抽屉传递 `categories`、`volumes` 和 `categoryOptions`。
    - 验收点：编辑抽屉不重复请求目录数据，门类选项能显示门类标题。
    - 重要度：9/10
- [ ] `3. 前端门类卷控件`：在 Sancai 条目编辑抽屉补齐门类和卷选择交互
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-EDIT-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-form-values.ts`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-model.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`
    - 处理动作：新增表单态 `categoryId`、`volumeId`，添加门类 `Select` 和卷 `Select`，保存时提交目标 `volumeId`。
    - 验收点：切换门类会过滤并必要时清空卷，未选卷不能提交，保存请求包含目标 `volumeId`。
    - 重要度：10/10
- [ ] `4. 后端迁移测试`：锁定 Sancai 跨卷迁移和接口透传行为
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-EDIT-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/SancaiApplicationServiceImplTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/SancaiAdminControllerTest.java`
    - 处理动作：补充更新条目跨卷、同卷排序保留、目标卷不存在和 update 请求 `volumeId` 透传测试。
    - 验收点：后端测试能证明迁移更新 `volume_id`、追加 `priority` 并生成正式版本。
    - 重要度：9/10
- [ ] `5. 前端迁移测试`：锁定 Sancai 编辑抽屉门类卷选择和保存请求
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-EDIT-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-service-contract.test.ts`、`kuzhambu-apps/admin-web/e2e/classics/sancai/sancai.spec.ts`
    - 处理动作：补充编辑抽屉默认门类卷、切换门类清空卷、选择新卷保存和 service 请求体测试。
    - 验收点：前端测试能证明用户选择目标卷后 update 请求携带新 `volumeId`，E2E 条件稳定时覆盖卷 A 到卷 B 迁移。
    - 重要度：9/10
- [ ] `6. 文档矩阵收口`：同步 Sancai 条目迁移需求、设计和 implementation coverage
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-EDIT-CLOSURE.md`
    - 范围对象：`docs/10-requirements/CLASSICS-REQUIREMENTS.md`、`docs/30-designs/CLASSICS-DESIGN.md`、`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：补充迁移规则和排序策略，并将 coverage 中对应项从 `部分完成` 收口为 `已完成`。
    - 验收点：需求、设计和 coverage 对“编辑标题、门类、卷、原文、译文和标签”的口径一致且未完成部分为 `无`。
    - 重要度：8/10
- [ ] `7. 格式和验证`：运行后端、前端和文档检查
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-EDIT-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/`、`kuzhambu-apps/`、`TODO.md`、`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-EDIT-CLOSURE.md`
    - 处理动作：运行 RUNBOOK 指定的 Maven、pnpm 和 `git diff --check` 验证。
    - 验收点：相关格式、静态检查、单测和构建通过，或失败原因明确记录为非本任务既有问题。
    - 重要度：10/10
- [ ] `8. 同步 main 并最终收口`：同步 main 分支代码、清理 RUNBOOK 和 TODO
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-EDIT-CLOSURE.md`
    - 范围对象：`main`、`feat/classics-sancai-edit-closure`、`TODO.md`、`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-EDIT-CLOSURE.md`
    - 处理动作：同步最新 `main` 到当前分支，确认无冲突后复核验证状态，删除已完成 TODO 项并删除 RUNBOOK。
    - 验收点：当前分支包含最新 `main`，Implementation Coverage 已更新，已完成任务不留在 `TODO.md`，RUNBOOK 被清理。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
