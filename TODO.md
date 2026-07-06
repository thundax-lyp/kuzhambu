# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `admin-web operations tasks`：迁移健康摘要出任务页
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-HEALTH-METRICS.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/tasks/tasks-page.tsx`、`kuzhambu-apps/admin-web/src/pages/operations/tasks/tasks-service.ts`、`kuzhambu-apps/admin-web/src/pages/operations/tasks/tasks-types.ts`、`kuzhambu-apps/admin-web/src/pages/operations/tasks/tasks-page.test.tsx`
    - 处理动作：删除任务页健康摘要请求与卡片，保留任务筛选、分页、详情抽屉和返回看板入口。
    - 验收点：任务页不再请求 `/operations/health/summary`，任务台账行为不变。
    - 重要度：8/10

- [ ] `Operations menu route permissions`：对齐运营看板菜单、路由和权限
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-HEALTH-METRICS.md`
    - 范围对象：`db/data-source/system.json`、`db/data/system.sql`、`kuzhambu-apps/admin-web/src/router/index.tsx`
    - 处理动作：核对 `/operations/dashboard` 菜单、权限种子和前端路由一致性。
    - 验收点：`运营看板` 使用 `operations:dashboard:view`，且 dashboard 不提供 `/operations/reports` 可点击死链。
    - 重要度：8/10

- [ ] `Operations readiness docs`：更新覆盖矩阵并清理 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-HEALTH-METRICS.md`、`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-HEALTH-METRICS.md`
    - 处理动作：在实现完成后更新 Operations 覆盖矩阵并删除临时 RUNBOOK。
    - 验收点：覆盖矩阵反映 Dashboard 与 Health Metrics 真实交付状态，RUNBOOK 已清理。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
