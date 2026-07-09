# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `08 kuzhambu-workers final validation`：运行同步 main 后的全量验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-WORKERS-REAL-AI-LOOP.md`
    - 范围对象：`kuzhambu-workers/`
    - 处理动作：在同步 `origin/main` 后执行 workers formatter、Ruff lint、pytest 和 placeholder/sensitive-field 复核命令。
    - 验收点：`ruff format --check`、`ruff check`、`pytest -p no:capture` 全部通过，placeholder 搜索无生产残留，敏感字段搜索只保留合法 redaction 断言。
    - 重要度：10/10

- [ ] `09 docs/40-readiness/WORKERS-IMPLEMENTATION-COVERAGE.md`：更新 Workers Implementation Coverage
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/40-readiness/WORKERS-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：记录 workers 真实 AI 执行闭环、验证命令和剩余缺口。
    - 验收点：Implementation Coverage 准确反映同步、SSE、结构化输出、错误归一化、usage/latency 的实现状态和验证结果。
    - 重要度：8/10

- [ ] `10 RUNBOOK-WORKERS-REAL-AI-LOOP`：清理临时 RUNBOOK 和 TODO 收口
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-WORKERS-REAL-AI-LOOP.md`、`TODO.md`
    - 处理动作：任务关闭时删除临时 RUNBOOK 并删除或收窄已完成 TODO。
    - 验收点：PR 收口前无已完成任务残留，临时 RUNBOOK 已删除或仅保留仍未关闭范围。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
