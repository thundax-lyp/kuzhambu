# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 当前任务项按数字编号顺序执行；不得跳过前置契约任务直接做后续调用方或重命名任务。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `component stereotype cleanup closure`：执行组件注解清理验证和现场清理
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-COMPONENT-STEREOTYPE-CLEANUP.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-infra`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application`、`kuzhambu-servers/biz/operations/kuzhambu-operations-infra`、`docs/30-designs/RUNBOOK-COMPONENT-STEREOTYPE-CLEANUP.md`、`TODO.md`
    - 处理动作：按 RUNBOOK Verification 执行静态搜索、Spotless、Checkstyle、相关 Maven tests、diff review，并按 RUNBOOK Closure 清理 TODO 和 RUNBOOK
    - 验收点：RUNBOOK 要求的静态检查无异常，`mvn spotless:check`、`mvn checkstyle:check` 和相关 `mvn ... test` 通过，diff 只包含 RUNBOOK 允许的文件范围，已完成 TODO 项被删除，无长期价值的 RUNBOOK 被删除，有长期价值的规则先迁移到 governance 后再删除 RUNBOOK
    - 重要度：10/10

## 待讨论项
