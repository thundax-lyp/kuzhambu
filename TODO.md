# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `Operations final validation after main sync`：同步 main 后执行最终验证
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`kuzhambu-servers/`、`kuzhambu-apps/`
    - 处理动作：在同步 `main` 并解决冲突后重新运行受影响的后端和前端验证。
    - 验收点：最终验证通过，失败时 TODO 收窄为剩余未完成范围。
    - 重要度：10/10

- [ ] `Operations implementation coverage`：更新 Operations 覆盖状态文档
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：将健康告警策略、异常状态记录、失败恢复联动和页面提示更新为已完成或明确剩余范围。
    - 验收点：coverage 文档与最终实现状态一致，不保留过期未完成描述。
    - 重要度：9/10

- [ ] `Operations health recovery closure`：清理 RUNBOOK 并收口 TODO
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-OPERATIONS-HEALTH-RECOVERY-LOOP.md`、`TODO.md`
    - 处理动作：阶段交付完成后删除临时 RUNBOOK，并按完成情况删除或收窄 TODO 项。
    - 验收点：PR 前不存在已完成任务残留，RUNBOOK 已按治理规则清理。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
