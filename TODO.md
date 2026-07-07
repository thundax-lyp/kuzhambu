# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `Operations health alert API`：新增健康告警 admin API
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-HEALTH-RECOVERY-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/service/HealthAlertApplicationService.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/service/impl/HealthAlertApplicationServiceImpl.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/OperationsHealthAlertAdminController.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/assembler/OperationsHealthAlertInterfaceAssembler.java`
    - 处理动作：新增告警分页、确认和手动恢复 admin API。
    - 验收点：`alerts/page` 使用 `operations:health:view`，`alerts/ack` 和 `alerts/recover` 使用 `operations:health:manage`。
    - 重要度：10/10

- [ ] `Operations health alert API models`：新增健康告警接口请求和响应模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-HEALTH-RECOVERY-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/request/OperationsHealthAlertPageRequest.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/request/OperationsHealthAlertAckRequest.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/request/OperationsHealthAlertRecoverRequest.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/response/OperationsHealthAlertPageResponse.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/response/OperationsHealthAlertSummaryResponse.java`
    - 处理动作：新增健康告警分页、确认、手动恢复请求模型和页面/摘要响应模型。
    - 验收点：请求与响应字段覆盖 RUNBOOK 定义的告警状态、级别、来源、恢复动作和恢复目标。
    - 重要度：9/10

- [ ] `Operations health alert API tests`：补齐健康告警接口契约测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-HEALTH-RECOVERY-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/OperationsHealthAlertAdminControllerTest.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/OperationsHealthContractTest.java`
    - 处理动作：为健康告警接口路径、权限、请求字段和响应字段补齐 contract 测试。
    - 验收点：`alerts/page`、`alerts/ack`、`alerts/recover` 的路径、权限和模型字段都被测试锁定。
    - 重要度：9/10

- [ ] `Operations dashboard alert response`：扩展 dashboard 告警摘要响应
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-HEALTH-RECOVERY-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/result/OperationsDashboardOverviewResult.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/dashboard/service/impl/OperationsDashboardApplicationServiceImpl.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/controller/response/OperationsDashboardOverviewResponse.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/dashboard/assembler/OperationsDashboardInterfaceAssembler.java`
    - 处理动作：在 dashboard overview 中返回未恢复告警数量、最高级别和最新告警摘要。
    - 验收点：dashboard response 不向无 `operations:health:view` 权限用户暴露告警详情。
    - 重要度：9/10

- [ ] `Admin Web dashboard alert service`：新增前端告警类型与服务
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-HEALTH-RECOVERY-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-types.ts`、`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-service.ts`、`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-service-contract.test.ts`
    - 处理动作：新增健康告警类型、overview 告警字段和 alerts/page、ack、recover 服务调用。
    - 验收点：service contract 测试断言 API path、请求体和 response 字段类型。
    - 重要度：9/10

- [ ] `Admin Web dashboard alert drawer`：实现看板告警横幅和告警抽屉
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-HEALTH-RECOVERY-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.tsx`、`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.css`、`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.test.tsx`
    - 处理动作：在 `/operations/dashboard` 增加顶部 `Alert`、告警列表 `KuzhambuDrawer`、确认、标记恢复和去处理按钮。
    - 验收点：无未恢复告警不展示横幅，无 manage 权限不展示确认或恢复按钮，抽屉 375px 宽度按钮不重叠。
    - 重要度：10/10

- [ ] `Admin Web dashboard health detail alerts`：实现健康明细关联告警控件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-HEALTH-RECOVERY-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.tsx`、`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.css`、`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.test.tsx`
    - 处理动作：为健康卡片增加告警角标，并在健康明细抽屉展示关联告警与查看全部告警按钮。
    - 验收点：健康明细原字段仍可见，当前 component 无告警时显示“暂无关联告警”空状态。
    - 重要度：8/10

- [ ] `Admin Web task failure hint`：补齐任务页失败提示
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-HEALTH-RECOVERY-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/tasks/tasks-page.tsx`、`kuzhambu-apps/admin-web/src/pages/operations/tasks/tasks-types.ts`、`kuzhambu-apps/admin-web/src/pages/operations/tasks/tasks-page.test.tsx`
    - 处理动作：在任务列表和详情抽屉为 `FAILED` 状态展示失败提示和告警入口。
    - 验收点：失败任务保留原详情字段，无失败原因时显示“未返回失败原因”。
    - 重要度：8/10

- [ ] `Admin Web backup cleanup failure hints`：补齐备份恢复和清理失败提示
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-HEALTH-RECOVERY-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/backup-restore/backup-restore-page.tsx`、`kuzhambu-apps/admin-web/src/pages/operations/backup-restore/backup-restore-types.ts`、`kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-page.tsx`、`kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-types.ts`、`kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-page.test.tsx`
    - 处理动作：为失败备份、失败恢复和失败清理 item 展示 `Alert`、失败原因和处置按钮。
    - 验收点：不新增自动重试，失败提示全部由接口字段驱动。
    - 重要度：8/10

- [ ] `Operations health alert permissions seed`：补齐健康告警管理权限种子
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-HEALTH-RECOVERY-LOOP.md`
    - 范围对象：`db/data/system.sql`、`db/data-source/system.json`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/health/controller/OperationsHealthAlertAdminControllerTest.java`
    - 处理动作：新增 `operations:health:manage` 权限种子并校验告警管理接口权限。
    - 验收点：健康告警查看和管理权限分离，种子数据与 controller 权限一致。
    - 重要度：9/10

- [ ] `Operations validation`：运行 Operations 后端与 Admin Web 定向验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-HEALTH-RECOVERY-LOOP.md`
    - 范围对象：`kuzhambu-servers/`、`kuzhambu-apps/`
    - 处理动作：运行 RUNBOOK 指定的 Maven 与 npm 格式、静态检查和定向测试。
    - 验收点：后端 Operations 测试和 Admin Web operations 测试通过，若失败需收窄到剩余 TODO。
    - 重要度：10/10

- [ ] `Branch sync main`：同步 main 分支最新代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`feat/operations-health-recovery-loop` 分支、`main` 分支
    - 处理动作：在功能验证通过后同步 `main` 最新代码并解决本分支冲突。
    - 验收点：同步后重新运行受影响的最小验证，工作区不混入无关修改。
    - 重要度：10/10

- [ ] `Operations final validation after main sync`：同步 main 后执行最终验证
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`kuzhambu-servers/`、`kuzhambu-apps/`
    - 处理动作：在同步 `main` 并解决冲突后重新运行受影响的后端和前端验证。
    - 验收点：最终验证通过，失败时 TODO 收窄为剩余未完成范围。
    - 重要度：10/10

- [ ] `Operations implementation coverage`：更新 Operations 覆盖状态文档
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：将健康告警策略、异常状态记录、失败恢复联动和页面提示更新为已完成或明确剩余范围。
    - 验收点：coverage 文档与最终实现状态一致，不保留过期未完成描述。
    - 重要度：9/10

- [ ] `Operations health recovery closure`：清理 RUNBOOK 并收口 TODO
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-OPERATIONS-HEALTH-RECOVERY-LOOP.md`、`TODO.md`
    - 处理动作：阶段交付完成后删除临时 RUNBOOK，并按完成情况删除或收窄 TODO 项。
    - 验收点：PR 前不存在已完成任务残留，RUNBOOK 已按治理规则清理。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
