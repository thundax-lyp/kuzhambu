# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `10 Knowledge 重提取分支同步 main`：收口前同步主分支最新代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`feat/knowledge-low-quality-reextract`、`main`
    - 处理动作：在功能、后端验证、前端验证和冒烟通过后，同步最新 `main` 到当前分支并处理冲突。
    - 验收点：当前分支基于最新 `main`，同步后重新确认工作区只包含本任务相关改动。
    - 重要度：9/10
- [ ] `11 Knowledge 同步 main 后回归验证`：同步主分支后重跑受影响验证
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge`、`kuzhambu-apps/admin-web`
    - 处理动作：同步最新 `main` 后重跑 Knowledge 后端和 Admin Web 受影响验证。
    - 验收点：同步 `main` 后没有回归，工作区仍只包含低质量门类重提取闭环相关改动。
    - 重要度：10/10
- [ ] `12 Knowledge Implementation Coverage 与 RUNBOOK 收口`：更新覆盖状态并清理临时 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-LOW-QUALITY-REEXTRACT.md`
    - 范围对象：`docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-KNOWLEDGE-LOW-QUALITY-REEXTRACT.md`、`TODO.md`
    - 处理动作：将低质量门类一键触发重提取相关覆盖项改为已完成，并删除已完成 TODO 和临时 RUNBOOK。
    - 验收点：Implementation Coverage 不再保留该未完成项或残留风险，RUNBOOK 已删除，`TODO.md` 仅保留真实未关闭任务。
    - 重要度：10/10

## 待讨论项
