# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `quality loop validation`：运行 Knowledge 质量闭环验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-QUALITY-LOOP.md`
    - 范围对象：`kuzhambu-servers/`、`kuzhambu-apps/`
    - 处理动作：运行 RUNBOOK 中 Java servers 与 admin-web 的格式化、静态检查和测试命令。
    - 验收点：`mvn spotless:check`、`mvn checkstyle:check`、Knowledge 相关 Maven test、`npm run format:check`、`npm run lint`、admin-web test 均通过，或记录明确阻塞。
    - 重要度：10/10

- [ ] `Knowledge documentation closure`：更新覆盖矩阵并清理 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-KNOWLEDGE-QUALITY-LOOP.md`
    - 范围对象：`docs/30-designs/KNOWLEDGE-DESIGN.md`、`docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-KNOWLEDGE-QUALITY-LOOP.md`、`TODO.md`
    - 处理动作：将质量标注报告闭环写入 Knowledge 设计和 Implementation Coverage，删除已完成 RUNBOOK，并按完成情况清理或收窄 TODO。
    - 验收点：Coverage 不再把质量报告闭环标为未完成，RUNBOOK 文件已删除，`TODO.md` 不保留已完成任务。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
