# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `branch-main-sync`：同步 main 分支代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：当前分支 `feat/ai-admin-governance` 与 `main`
    - 处理动作：完成实现任务后同步 `main` 最新代码到当前分支并解决冲突。
    - 验收点：当前分支包含 `main` 最新代码，冲突已解决，且后续最终验证基于同步后的代码执行。
    - 重要度：10/10

- [ ] `ai-admin-verification`：执行 AI 治理后台闭环验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-ADMIN-GOVERNANCE.md`
    - 范围对象：`scripts/generate-system-data-sql.ts`、`kuzhambu-servers/`、`kuzhambu-apps/`
    - 处理动作：在同步 `main` 后运行菜单生成校验、后端 Maven 校验和 admin-web 前端校验。
    - 验收点：菜单 SQL check、后端 spotless/checkstyle/test、前端 format/lint/test/build 均通过或记录明确阻塞原因。
    - 重要度：10/10

- [ ] `ai-implementation-coverage`：更新 AI Implementation Coverage
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：同步记录 AI 管理端治理闭环的最终覆盖状态和验证结果。
    - 验收点：覆盖矩阵明确标注服务配置、模型配置、能力映射、提示词版本、调用统计和动作状态页面已完成。
    - 重要度：8/10

- [ ] `runbook-cleanup`：清理 AI 治理 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-AI-ADMIN-GOVERNANCE.md`
    - 处理动作：任务完成并同步覆盖文档后删除临时 RUNBOOK。
    - 验收点：PR 收口前仓库不再保留已完成任务的临时 RUNBOOK。
    - 重要度：8/10

## 待审阅任务项

## 待讨论项
