# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `kuzhambu-servers`：执行 Classics 批量可见性 servers 验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-BATCH-VISIBILITY-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/`
    - 处理动作：按 RUNBOOK 对 Java servers 运行相关 formatter、checkstyle、compile 和测试验证。
    - 验收点：相关 Maven `spotless:check`、`checkstyle:check`、`compile`、`ClassicsContentAdminControllerTest`、`ClassicsInterfaceArchitectureTest` 通过，PR 前全量 servers 验证通过或记录明确阻塞。
    - 重要度：10/10

- [ ] `kuzhambu-apps/admin-web`：执行 Classics 批量可见性 admin-web 验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-BATCH-VISIBILITY-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/`、`kuzhambu-apps/`
    - 处理动作：按 RUNBOOK 对 admin-web 运行相关 prettier、lint、build 和测试验证。
    - 验收点：相关 `format:check`、`lint`、`build`、四个 Classics 页面/契约测试通过，PR 前全量 apps 验证通过或记录明确阻塞。
    - 重要度：10/10

- [ ] `RUNBOOK cleanup`：清理 Classics 批量可见性现场
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-CLASSICS-BATCH-VISIBILITY-CLOSURE.md`
    - 范围对象：`TODO.md`、`docs/30-designs/RUNBOOK-CLASSICS-BATCH-VISIBILITY-CLOSURE.md`
    - 处理动作：阶段任务关闭前删除已完成 TODO，并删除不再需要的临时 RUNBOOK。
    - 验收点：`TODO.md` 只保留未关闭任务，且本 RUNBOOK 在 PR 前已删除或收窄为仍未完成范围。
    - 重要度：9/10

## 待讨论项
