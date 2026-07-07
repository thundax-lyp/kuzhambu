# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

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
