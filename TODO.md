# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 当前任务项按数字编号顺序执行；不得跳过前置契约任务直接做后续调用方或重命名任务。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `25-system-contract-validation`：[25] 运行 system 契约强类型化后端验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SYSTEM-APPLICATION-TYPED-CONTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-application`、`kuzhambu-servers/biz/system/kuzhambu-system-interface`、`kuzhambu-servers/biz/system`
    - 处理动作：运行 RUNBOOK 规定的 Maven 窄测试、编译、格式检查、checkstyle 和最终 system 测试。
    - 验收点：`mvn -pl biz/system/kuzhambu-system-application -Dtest='*ApplicationServiceImplTest,SystemApplicationArchitectureTest' test`、`mvn -pl biz/system/kuzhambu-system-interface -am -DskipTests compile`、`mvn spotless:check`、`mvn checkstyle:check`、`mvn -pl biz/system -am test` 均通过或有明确记录。
    - 重要度：9/10

- [ ] `26-runbook-closure`：[26] 收口并清理强类型契约 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-SYSTEM-APPLICATION-TYPED-CONTRACT.md`
    - 范围对象：`docs/30-designs/RUNBOOK-SYSTEM-APPLICATION-TYPED-CONTRACT.md`、`TODO.md`
    - 处理动作：完成实现和验证后删除临时 RUNBOOK，并按剩余工作收窄或清空 TODO。
    - 验收点：PR 收口前 RUNBOOK 已删除或长期价值内容已迁移到治理/readiness 文档，`TODO.md` 不保留已完成任务。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
