# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `Operations cleanup infra tests`：补运行态目标查询测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-CLEANUP-TARGET-TYPES.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/test/java/com/thundax/kuzhambu/operations/infra/report/repository/impl/ReportRepositoryImplTest.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/test/java/com/thundax/kuzhambu/operations/infra/health/repository/impl/HealthCheckRepositoryImplTest.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/test/java/com/thundax/kuzhambu/operations/infra/task/repository/impl/LongTaskSnapshotRepositoryImplTest.java`
    - 处理动作：为三类运行态过期目标 ID 查询补充 repository 测试。
    - 验收点：测试覆盖阈值字段、状态过滤和 `LIMIT` 条件，防止误清理运行中记录。
    - 重要度：8/10

- [ ] `Admin cleanup page`：补全部 cleanup type 的页面展示与人工操作
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-CLEANUP-TARGET-TYPES.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-page.tsx`、`kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-page.test.tsx`、`kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-service-contract.test.ts`
    - 处理动作：在 Admin cleanup 页面清理类型筛选和人工补偿执行控件中补齐三类新增 cleanup type。
    - 验收点：`清理类型` 与 `执行清理类型` 控件展示全部 7 类，选择新增类型会弹出确认，失败行、失败项抽屉和告警跳转保持可用。
    - 重要度：8/10

- [ ] `Operations cleanup backend validation`：完成后端定向验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-CLEANUP-TARGET-TYPES.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-domain`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application`、`kuzhambu-servers/biz/operations/kuzhambu-operations-infra`、`kuzhambu-servers/starter/kuzhambu-admin-starter`
    - 处理动作：运行 RUNBOOK 指定的 Maven 后端格式检查、静态检查和定向测试。
    - 验收点：`spotless:check`、`checkstyle:check` 和 cleanup 相关后端定向测试通过；若失败，记录失败命令、原因和剩余风险。
    - 重要度：9/10

- [ ] `Admin cleanup validation`：完成前端定向验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-CLEANUP-TARGET-TYPES.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-page.tsx`、`kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-page.test.tsx`、`kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-service-contract.test.ts`、`kuzhambu-apps/admin-web`
    - 处理动作：运行 admin-web 格式检查、lint 和 cleanup 页面定向测试。
    - 验收点：`format:check`、`lint`、`cleanup-page.test.tsx` 和 `cleanup-service-contract.test.ts` 通过；若失败，记录失败命令、原因和剩余风险。
    - 重要度：9/10

- [ ] `Operations cleanup closeout`：同步 main 并完成文档与临时文件收口
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-OPERATIONS-CLEANUP-TARGET-TYPES.md`
    - 范围对象：`git main sync`、`TODO.md`、`docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-OPERATIONS-CLEANUP-TARGET-TYPES.md`
    - 处理动作：收口前同步最新 `main`，更新 Implementation Coverage，删除已完成 RUNBOOK，并按完成状态清理 TODO。
    - 验收点：当前分支包含最新 `main` 代码，`清理任务` 覆盖状态为 `已完成`，RUNBOOK 已删除，TODO 删除已完成项或仅保留真实剩余项。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
