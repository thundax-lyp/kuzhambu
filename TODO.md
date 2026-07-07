# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `knowledge backend validation`：运行标签批量治理后端验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-TAG-BATCH-GOVERNANCE.md`
    - 范围对象：`kuzhambu-servers/pom.xml`、`kuzhambu-servers/biz/knowledge/pom.xml`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/pom.xml`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/pom.xml`
    - 处理动作：运行 Knowledge 后端 formatter、静态检查和 Maven 测试。
    - 验收点：`spotless:check`、`checkstyle:check` 和 Knowledge application/interface 相关 `test` 通过或记录明确阻塞。
    - 重要度：10/10

- [ ] `admin taxonomy validation`：运行标签批量治理 Admin Web 验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-TAG-BATCH-GOVERNANCE.md`
    - 范围对象：`kuzhambu-apps/package.json`、`kuzhambu-apps/package-lock.json`、`kuzhambu-apps/admin-web/package.json`、`kuzhambu-apps/admin-web/vite.config.ts`
    - 处理动作：运行 Admin Web format、lint、test 和 build。
    - 验收点：`format:check`、`lint`、Admin Web `test` 和 `build` 通过或记录明确阻塞。
    - 重要度：10/10

- [ ] `knowledge final sync and cleanup`：同步 main 后完成 coverage 与 RUNBOOK 收口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-TAG-BATCH-GOVERNANCE.md`
    - 范围对象：`main` 分支最新代码、`docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-KNOWLEDGE-TAG-BATCH-GOVERNANCE.md`、`TODO.md`
    - 处理动作：最终收口前同步 `main` 最新代码，更新 Knowledge Implementation Coverage，并删除已完成 RUNBOOK 与对应 TODO。
    - 验收点：分支包含最新 `main` 基线，coverage 中 `标签批量操作` 为 `已完成` 且未完成部分为 `无`，RUNBOOK 已删除，完成项已从 `TODO.md` 清理。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
