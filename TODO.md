# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `01 System 日志接口权限`：收敛系统日志分页接口查看权限
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-SYSTEM-AUDIT-ENTRY.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/LogController.java`、`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/admin/SystemLogContractTest.java`
    - 处理动作：将 `LogController.page` 的查看权限从 `super` 收敛为 `system:log:view`。
    - 验收点：`SystemLogContractTest` 断言 `/api/sys/log/page` 权限为 `system:log:view`，且路由、请求字段和响应字段契约不变。
    - 重要度：9/10
- [ ] `02 Operations 运维入口控件`：补齐 System 日志和审计入口卡片
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-SYSTEM-AUDIT-ENTRY.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.tsx`、`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.test.tsx`
    - 处理动作：按 RUNBOOK 的 `OperationEntry` 字段渲染 Operations 运维入口卡片。
    - 验收点：组件测试覆盖系统日志、审计日志入口可见、无权限隐藏、空状态、`data-testid` 和点击跳转到 `/system/logs`、`/audit/logs`。
    - 重要度：9/10
- [ ] `03 System 日志审计目标页`：补齐目标页查看权限门禁
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-SYSTEM-AUDIT-ENTRY.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/system/system-log/system-log-page.tsx`、`kuzhambu-apps/admin-web/src/pages/audit/audit-log/audit-log-page.tsx`、`kuzhambu-apps/admin-web/e2e/system/logs/logs.spec.ts`
    - 处理动作：为系统日志页和审计日志页增加目标页查看权限门禁。
    - 验收点：E2E 覆盖无 `system:log:view` 不请求 `/sys/log/page`、无 `audit:view` 不请求 `/audit/log/options|page|detail`，有权限时搜索、筛选、刷新和审计详情 Drawer 行为保持可用。
    - 重要度：9/10
- [ ] `04 Operations E2E 与菜单图标`：补齐入口跳转和图标渲染闭环
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-SYSTEM-AUDIT-ENTRY.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/layouts/admin-layout.tsx`、`kuzhambu-apps/admin-web/e2e/operations/dashboard/dashboard.spec.ts`、`kuzhambu-apps/admin-web/e2e/system/logs/logs.spec.ts`
    - 处理动作：补齐菜单 icon key 并新增 Operations 看板入口 E2E。
    - 验收点：E2E 验证入口可见、点击后到达目标页 heading、权限不足时入口不可见，菜单不出现 `menu-icon-config-error`。
    - 重要度：8/10
- [ ] `05 收口文档与分支同步`：完成覆盖状态、主干同步和临时文档清理
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-OPERATIONS-SYSTEM-AUDIT-ENTRY.md`
    - 范围对象：`docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md`、`docs/40-readiness/SYSTEM-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-OPERATIONS-SYSTEM-AUDIT-ENTRY.md`、`TODO.md`
    - 处理动作：完成最终收口文档同步和临时任务文档清理。
    - 验收点：收口前已同步最新 `main` 分支代码并重跑相关验证；Operations/System Implementation Coverage 反映日志审计入口闭环状态；RUNBOOK 已清理；`TODO.md` 只保留未完成任务。
    - 重要度：10/10

## 待讨论项
