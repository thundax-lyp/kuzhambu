# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `Operations readiness coverage`：更新 Operations 实现覆盖文档
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-RUNTIME-CLOSURE.md`
    - 范围对象：`docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：根据 cleanup、health、long task 和统一运维入口的实际落地结果同步 Implementation Coverage
    - 验收点：`OPERATIONS-IMPLEMENTATION-COVERAGE.md` 与代码事实一致，能准确反映已完成、部分完成和未完成项
    - 重要度：8/10

- [ ] `Operations runtime runbook cleanup`：删除已完成 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-RUNTIME-CLOSURE.md`
    - 范围对象：`docs/30-designs/RUNBOOK-OPERATIONS-RUNTIME-CLOSURE.md`
    - 处理动作：在 Operations runtime closure 全部完成后删除本次专项 RUNBOOK
    - 验收点：RUNBOOK 已删除，仓库中不保留已失效的专项执行文档
    - 重要度：7/10

- [ ] `Operations runtime closure cleanup`：清理收口现场
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`TODO.md`、Operations runtime closure 相关文档与残留临时文件
    - 处理动作：删除已完成 TODO 项，清理本轮遗留的临时说明、无用占位和收口残留
    - 验收点：TODO 只保留未完成项，已完成任务不残留，工作区与文档现场整洁
    - 重要度：8/10

## 待讨论项
