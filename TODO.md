# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `ming-customs-frontend-history-panel`: 新增明代习俗版本历史面板组件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-MING-CUSTOMS-HISTORY-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/components/ming-customs-version-history-panel.tsx`, `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.css`, `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.test.tsx`
    - 处理动作：实现版本历史面板与字段对比逻辑，支持查看与恢复按钮、快照异常警告，并在测试中覆盖面板呈现与字段对比。
    - 验收点：打开编辑弹窗后可见 `明代习俗版本历史面板`，点击版本可展示元信息与字段对比，快照解析异常时按钮禁用。
    - 重要度：9/10

- [ ] `ming-customs-frontend-history-integration`: 接入明代习俗页面查询、恢复与刷新
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-MING-CUSTOMS-HISTORY-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.tsx`, `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.css`, `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.test.tsx`
    - 处理动作：新增版本历史查询与详情查询状态管理、恢复 mutation 与刷新策略，并接入版本面板到明代习俗编辑弹窗。
    - 验收点：编辑弹窗打开后自动加载历史版本；恢复确认后提示并触发约定 queryKey 刷新。
    - 重要度：10/10

- [ ] `ming-customs-frontend-history-e2e`: 补齐明代习俗版本历史 E2E 回归
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-MING-CUSTOMS-HISTORY-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/e2e/classics/ming-customs/ming-customs.spec.ts`
    - 处理动作：补齐版本历史查看与恢复的端到端场景，覆盖恢复成功与版本对比展示。
    - 验收点：端到端场景能复现版本查询、查看并执行恢复。
    - 重要度：7/10

## 待审阅任务项

## 待讨论项
