# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `servers-full-validation`：Servers 全量验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-PERMISSION-OPERATIONS-CLEANUP-CLOSURE.md`
    - 范围对象：`kuzhambu-servers`
    - 处理动作：执行 servers 全量 format/checkstyle/compile/test 验证。
    - 验收点：`mvn spotless:check`, `mvn checkstyle:check`, `mvn -DskipTests compile`, `mvn test` 均通过。
    - 重要度：10/10

- [ ] `admin-web-full-validation`：Admin Web 全量验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-PERMISSION-OPERATIONS-CLEANUP-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web`
    - 处理动作：执行 Admin Web 全量 format/lint/build/test 验证。
    - 验收点：`npm --workspace admin-web run format:check`, `lint`, `build`, `test` 均通过。
    - 重要度：9/10

- [ ] `portal-web-full-validation`：Portal Web 全量验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-PERMISSION-OPERATIONS-CLEANUP-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/portal-web`
    - 处理动作：执行 Portal Web 全量 format/lint/build/test 验证。
    - 验收点：`npm --workspace portal-web run format:check`, `lint`, `build`, `test` 均通过。
    - 重要度：9/10

- [ ] `system-data-seed-validation`：System 数据种子验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-PERMISSION-OPERATIONS-CLEANUP-CLOSURE.md`
    - 范围对象：`db/data-source/system.json`, `db/data/system.sql`, `scripts/generate-system-data-sql.ts`
    - 处理动作：执行 System 数据种子一致性检查。
    - 验收点：`node scripts/generate-system-data-sql.ts --check` 通过。
    - 重要度：9/10

- [ ] `coverage-runbook-cleanup`：Implementation Coverage 与 RUNBOOK 收口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-PERMISSION-OPERATIONS-CLEANUP-CLOSURE.md`, `docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`, `docs/40-readiness/SYSTEM-IMPLEMENTATION-COVERAGE.md`, `docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md`, `docs/30-designs/RUNBOOK-CLASSICS-PERMISSION-OPERATIONS-CLEANUP-CLOSURE.md`
    - 处理动作：更新三个 Implementation Coverage 并删除已完成 RUNBOOK。
    - 验收点：Coverage 反映本闭环完成状态，RUNBOOK 被清理，`TODO.md` 无已完成残留项。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
