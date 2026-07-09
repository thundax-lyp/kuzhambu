# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `DISCOVERY-QUALITY-004`：执行 Discovery QA 来源跳转冒烟并归档证据
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QUALITY-SMOKE.md`
    - 范围对象：`/api/discovery/qa-admin/*`、`/api/portal/discovery/qa/*`、`discovery_qa_session`、`discovery_qa_message_source`、`discovery_qa_retrieval_trace`
    - 处理动作：按 RUNBOOK 验证 provider 预检、QA health、QA rebuild、Portal QA 带来源回答、来源跳转和 Admin trace
    - 验收点：证据包包含 QA health、rebuild、chat response、Admin session/source/trace、DB 查询和页面截图，所有 QA stop condition 均未触发
    - 重要度：10/10

- [ ] `DISCOVERY-QUALITY-005`：收口 Discovery 质量证据并同步 main
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/00-governance/PR-RULES.md`、`docs/40-readiness/DISCOVERY-IMPLEMENTATION-COVERAGE.md`
    - 范围对象：`docs/40-readiness/DISCOVERY-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-DISCOVERY-QUALITY-SMOKE.md`、`TODO.md`、当前工作分支
    - 处理动作：更新 Discovery Implementation Coverage，清理已完成 RUNBOOK 和对应 TODO，并在收口前同步 `main` 分支最新代码
    - 验收点：coverage 记录最终证据链接和剩余风险，RUNBOOK 已按规则清理，TODO 删除或收窄已完成项，当前分支已基于最新 `main` 复核
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
