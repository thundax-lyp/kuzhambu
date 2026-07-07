# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `05 Admin Web Operations Dashboard`：前端控件消费真实 overview response
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-SUMMARY.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.tsx`；`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-types.ts`；`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-service.ts`；`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.test.tsx`；`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-service-contract.test.ts`
    - 处理动作：让周期分段、刷新按钮、核心指标卡、趋势、排行、健康抽屉和运维入口按 RUNBOOK 控件矩阵消费后端真实字段。
    - 验收点：周期切换请求正确，刷新重新拉取 overview 和 health trend，空数组显示真实空态，健康项点击打开明细抽屉，无 mock 和硬编码样例排行。
    - 重要度：9/10

- [ ] `06 main 分支同步`：在最终验证前同步 main 分支代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`feat/operations-dashboard-summary`；`main`
    - 处理动作：将最新 `main` 合入当前 worktree 分支并解决冲突。
    - 验收点：当前分支包含最新 `main`，无未解决冲突，后续验证任务基于同步后的代码运行。
    - 重要度：8/10

- [ ] `07 Operations Dashboard 验证`：在同步 main 后运行后端和前端验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-SUMMARY.md`
    - 范围对象：`kuzhambu-servers/biz/discovery`；`kuzhambu-servers/biz/operations`；`kuzhambu-apps/admin-web/src/pages/operations/dashboard`
    - 处理动作：按 RUNBOOK 在同步 main 后运行 Discovery、Operations 和 admin-web dashboard 的格式化、静态检查和测试。
    - 验收点：相关 Maven `spotless:check`、`checkstyle:check`、`test` 通过，admin-web dashboard 测试、`format:check`、`lint`、`build` 通过。
    - 重要度：9/10

- [ ] `08 Implementation Coverage 同步`：更新实现覆盖状态
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md`；`docs/40-readiness/DISCOVERY-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：将 Operations Dashboard 真实跨域 summary 和 Discovery 搜索耗时闭环同步到 Implementation Coverage。
    - 验收点：Operations 仪表盘与聚合展示不再标记为跨域 summary 占位，Discovery 搜索日志记录说明包含搜索耗时真实统计。
    - 重要度：8/10

- [ ] `09 RUNBOOK 清理`：任务关闭前删除临时 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-OPERATIONS-DASHBOARD-SUMMARY.md`
    - 处理动作：在功能、验证、main 同步和 Coverage 更新完成后删除本 RUNBOOK。
    - 验收点：RUNBOOK 文件已删除，`TODO.md` 对应任务随完成提交删除或收窄，不保留已完成历史。
    - 重要度：8/10

## 待讨论项
