# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `Sancai Coverage And Closure`：更新覆盖文档并完成任务收口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-VISUAL-ASSETS-CLOSURE.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-SANCAI-VISUAL-ASSETS-CLOSURE.md`、`TODO.md`
    - 处理动作：更新三才视觉资产相关 Implementation Coverage 口径，删除已完成 RUNBOOK，并将 TODO 收窄为剩余未完成内容或清空
    - 验收点：`CLASSICS-IMPLEMENTATION-COVERAGE.md` 已同步当前实现状态，RUNBOOK 已删除，`TODO.md` 不保留已完成任务
    - 重要度：8/10

- [ ] `Sancai Final Verify`：执行全量格式化、静态检查、构建与测试
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`kuzhambu-servers/`、`kuzhambu-apps/`
    - 处理动作：按仓库规则执行本次改动涉及模块的格式化与最终校验，包括 servers 的 `spotless/checkstyle/test` 和 apps 的 `format:check/lint/build/test`
    - 验收点：至少完成 `cd kuzhambu-servers && mvn -q spotless:check && mvn -q checkstyle:check && mvn -q test` 与 `cd kuzhambu-apps && npm run format:check && npm run lint && npm run build && npm test`，结果可用于 PR 描述
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
