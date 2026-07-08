# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `05 Admin Web operations health page`：补齐关联告警确认与恢复控件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-RESTORE-ORCHESTRATION.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/health/health-page.tsx`、`kuzhambu-apps/admin-web/src/pages/operations/health/health-page.test.tsx`
    - 处理动作：在健康记录关联告警抽屉中增加 `确认`、`恢复`、权限禁用态、二次确认和成功刷新。
    - 验收点：页面测试覆盖 `查看告警` 打开抽屉、`确认` 调用、`恢复` 二次确认调用、无权限禁用、空态和失败态。
    - 重要度：9/10

- [ ] `06 Operations application validation`：验证后端 application 行为
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-RESTORE-ORCHESTRATION.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-application`、`HealthAlertApplicationServiceImplTest.java`、`RestoreApplicationServiceImplTest.java`
    - 处理动作：运行 Operations application 相关单元测试验证恢复编排和任务快照。
    - 验收点：健康告警恢复编排测试和恢复任务快照测试通过。
    - 重要度：10/10

- [ ] `07 Operations interface validation`：验证后端 interface 契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-RESTORE-ORCHESTRATION.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-interface`、`OperationsHealthAlertInterfaceAssembler.java`、`OperationsHealthAlertRecoverCommand.java`
    - 处理动作：运行 Operations interface 相关测试验证 recover 请求装配。
    - 验收点：interface 模块测试通过，recover command 的操作者来源保持为当前 subject。
    - 重要度：9/10

- [ ] `08 Admin Web health validation`：验证前端健康告警页面
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-RESTORE-ORCHESTRATION.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/health/health-page.test.tsx`、`kuzhambu-apps/admin-web/src/pages/operations/health/health-service-contract.test.ts`、`kuzhambu-apps/admin-web`
    - 处理动作：运行健康页相关 Vitest、format、lint 和 build 验证。
    - 验收点：健康页测试、服务契约测试、format:check、lint 和 build 通过；若存在非本任务既有失败，记录失败文件和原因。
    - 重要度：10/10

- [ ] `09 Main branch sync`：同步 main 分支最新代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`feat/operations-restore-orchestration`
    - 处理动作：在功能与最小验证通过后同步 `main` 最新代码并解决仅与本任务相关的冲突。
    - 验收点：当前分支基于最新 `main`，同步后相关验证仍通过。
    - 重要度：9/10

- [ ] `10 Operations Implementation Coverage`：更新实现覆盖说明
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-OPERATIONS-RESTORE-ORCHESTRATION.md`
    - 范围对象：`docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：同步 Operations health/alert/restore/task 自动恢复闭环的实现覆盖状态。
    - 验收点：Implementation Coverage 准确反映本次 health/alert/restore/task 闭环能力和剩余缺口。
    - 重要度：8/10

- [ ] `11 RUNBOOK cleanup`：清理临时 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-OPERATIONS-RESTORE-ORCHESTRATION.md`
    - 处理动作：在实现、验证、main 同步和 Implementation Coverage 完成后删除临时 RUNBOOK。
    - 验收点：RUNBOOK 文件从工作区移除，TODO 仅保留真实未关闭事项。
    - 重要度：7/10

## 待审阅任务项

## 待讨论项
