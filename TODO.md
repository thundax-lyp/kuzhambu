# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

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
