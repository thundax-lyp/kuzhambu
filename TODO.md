# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `admin-web operations dashboard validation`：运行 dashboard 前端验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-PERMISSION-CHARTS.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.tsx`、`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.css`、`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.test.tsx`、`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-service-contract.test.ts`
    - 处理动作：运行 RUNBOOK 列出的 admin-web dashboard 前端窄验证与收口验证。
    - 验收点：dashboard Vitest、`format:check`、`lint` 和 `test` 通过或记录明确阻塞原因。
    - 重要度：10/10

- [ ] `operations dashboard readiness docs`：更新覆盖矩阵并清理临时 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-PERMISSION-CHARTS.md`
    - 范围对象：`docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-PERMISSION-CHARTS.md`、`TODO.md`
    - 处理动作：将按权限裁剪聚合图表标记为已完成并删除已完成 TODO 与临时 RUNBOOK。
    - 验收点：Implementation Coverage 反映完成态，已关闭 TODO 从 `TODO.md` 删除，RUNBOOK 文件被清理。
    - 重要度：10/10

## 待讨论项
