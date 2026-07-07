# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `operations cleanup policy properties`：新增清理调度策略配置对象
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-CLEANUP-SCHEDULE-POLICY.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/support/OperationsCleanupScheduleProperties.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/support/OperationsCleanupSupport.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/cleanup/support/OperationsCleanupSchedulePropertiesTest.java`
    - 处理动作：新增 cleanup schedule properties 并提供四类 policy 的 enabled、retentionDays、limit 和固定执行顺序。
    - 验收点：策略对象能按默认值返回每日 03:30、启动不执行、默认 limit 200 和四类 cleanup policy。
    - 重要度：9/10

- [ ] `operations cleanup application context`：扩展清理执行上下文
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-CLEANUP-SCHEDULE-POLICY.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/command/OperationsCleanupExecuteCommand.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/service/CleanupApplicationService.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/service/impl/CleanupApplicationServiceImpl.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/cleanup/service/impl/CleanupApplicationServiceImplTest.java`
    - 处理动作：让 cleanup 执行命令携带 requestedAt、retentionDays、limit，并允许自动任务 requesterUserId 为 null。
    - 验收点：人工执行仍校验 requesterUserId 非空，自动执行写入 requesterUserId null，发现逻辑使用 policy retentionDays 和 limit。
    - 重要度：10/10

- [ ] `operations cleanup scheduler`：新增自动清理调度器并装配 starter 配置
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-CLEANUP-SCHEDULE-POLICY.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/support/OperationsCleanupScheduler.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/cleanup/support/OperationsCleanupSchedulerTest.java`、`kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/resources/application.yml`、`kuzhambu-servers/starter/kuzhambu-admin-starter/src/test/java/com/thundax/kuzhambu/starter/admin/AdminStarterArchitectureTest.java`
    - 处理动作：新增 OperationsCleanupScheduler 并在 admin starter 暴露 `kuzhambu.operations.cleanup.schedule` 配置块。
    - 验收点：总开关、启动开关、单 policy disabled 和单类型异常继续后续类型均有测试覆盖。
    - 重要度：10/10

- [ ] `classics cleanup retention contract`：补齐 Classics 清理策略参数传递
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-CLEANUP-SCHEDULE-POLICY.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/request/ClassicsCleanupTargetsFacadeRequest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/facade/impl/ClassicsFacadeImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/cleanup/service/ClassicsCleanupApplicationService.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/cleanup/service/impl/ClassicsCleanupApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/cleanup/ClassicsCleanupApplicationServiceImplTest.java`
    - 处理动作：让 Classics cleanup facade 和 application 明确接收并消费 retentionDays 与 limit。
    - 验收点：Operations 传入的 retentionDays 和 limit 能到达 Classics listTargets，且不新增跨域 infra 依赖。
    - 重要度：9/10

- [ ] `admin-web operations cleanup ux`：更新清理台账页面自动/人工展示
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-CLEANUP-SCHEDULE-POLICY.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-page.tsx`、`kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-types.ts`、`kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-service.ts`、`kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-page.test.tsx`、`kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-service-contract.test.ts`
    - 处理动作：更新 cleanup 页面控件、表格和详情抽屉，明确自动调度与人工补偿的展示和操作。
    - 验收点：执行清理类型 Select、确认弹窗、执行人列、详情触发来源、失败项和告警跳转均按 RUNBOOK 行为可测。
    - 重要度：8/10

- [ ] `operations cleanup runtime examples`：同步清理调度环境变量样例
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-CLEANUP-SCHEDULE-POLICY.md`
    - 范围对象：`.env.example`、`deploy/.env.example`
    - 处理动作：补齐所有 `KUZHAMBU_OPERATIONS_CLEANUP_*` 环境变量样例。
    - 验收点：本地和部署样例都包含总开关、启动开关、cron、default limit 和四类 policy 的 enabled、retention days、limit。
    - 重要度：7/10

- [ ] `main branch sync`：收口前同步 main 分支代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`main`、`feat/operations-cleanup-schedule-policy`
    - 处理动作：在最终验证和文档收口前从 main 同步最新代码并处理冲突。
    - 验收点：功能分支基于最新 main，无未解决冲突，最终 diff 只包含本任务相关改动。
    - 重要度：10/10

- [ ] `operations cleanup validation`：执行后端与前端最小验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-CLEANUP-SCHEDULE-POLICY.md`、`docs/00-governance/TODO-RULES.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-application`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface`、`kuzhambu-servers/starter/kuzhambu-admin-starter`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application`、`kuzhambu-apps/admin-web`
    - 处理动作：运行 RUNBOOK 指定的 Maven、Spotless、Checkstyle、npm format、lint 和 cleanup 页面测试。
    - 验收点：相关 Java 定向测试、前端 cleanup 测试、格式检查和静态检查均通过，或记录明确阻塞原因。
    - 重要度：10/10

- [ ] `operations cleanup documentation closure`：更新覆盖矩阵并清理 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-CLEANUP-SCHEDULE-POLICY.md`、`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-OPERATIONS-CLEANUP-SCHEDULE-POLICY.md`
    - 处理动作：将 cleanup 调度化清理和长期规则策略更新为已完成，并在任务关闭前删除临时 RUNBOOK。
    - 验收点：Implementation Coverage 准确反映已完成状态，RUNBOOK 文件和残留引用已清理。
    - 重要度：9/10

## 待讨论项
