# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `classics/lifecycle-final-validation`：运行同步 main 后的最终验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-LIFECYCLE-EDIT-LOOP.md`
    - 范围对象：`kuzhambu-servers/.mvn/maven.config`、`kuzhambu-servers/pom.xml`、`kuzhambu-apps/package.json`、`kuzhambu-apps/admin-web/package.json`
    - 处理动作：在同步最新 `main` 后，按 RUNBOOK 执行后端 formatter、Spotless、Checkstyle、测试和前端 format、lint、test。
    - 验收点：后端 Maven 验证和前端 npm 验证通过；若失败，TODO 收窄为明确剩余失败项。
    - 重要度：10/10

- [ ] `classics/lifecycle-doc-closure`：更新覆盖文档并清理 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-LIFECYCLE-EDIT-LOOP.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-CLASSICS-LIFECYCLE-EDIT-LOOP.md`、`TODO.md`
    - 处理动作：最终验证通过后更新 Implementation Coverage、删除临时 RUNBOOK，并按完成情况删除或收窄 TODO。
    - 验收点：`CLASSICS-IMPLEMENTATION-COVERAGE.md` 标记生命周期闭环已完成，RUNBOOK 不再保留，`TODO.md` 不记录已完成任务。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
