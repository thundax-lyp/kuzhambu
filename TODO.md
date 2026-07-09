# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `07-ai-runtime-verification`：07 执行 AI 验收验证命令
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-RUNTIME-ACCEPTANCE.md`
    - 范围对象：`kuzhambu-servers/pom.xml`、`kuzhambu-apps/package.json`、`kuzhambu-apps/admin-web/package.json`、`kuzhambu-workers/pyproject.toml`
    - 处理动作：运行 RUNBOOK 窄集验证并记录结果。
    - 验收点：窄集验证命令、结果和任何失败归因记录到 readiness 证据或 PR 描述。
    - 重要度：9/10

- [ ] `08-sync-main-before-close`：08 收口前同步 main 分支代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/PR-RULES.md`
    - 范围对象：`feat/ai-runtime-acceptance-runbook`、`origin/main`
    - 处理动作：在收口前拉取远端并将当前分支同步到最新 `origin/main`。
    - 验收点：`git status --short --branch` 显示当前分支基于最新 `origin/main`，且无合并冲突。
    - 重要度：8/10

- [ ] `09-ai-runtime-final-coverage-cleanup`：09 同步全量验证、更新覆盖清单并清理 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`、`docs/40-readiness/AI-RUNTIME-SMOKE-EVIDENCE.md`、`docs/30-designs/RUNBOOK-AI-RUNTIME-ACCEPTANCE.md`、`TODO.md`
    - 处理动作：同步 main 后运行 PR 前全量验证，更新 AI Implementation Coverage，保留 readiness 证据，删除临时 RUNBOOK，并从 TODO 中删除已完成任务。
    - 验收点：全量验证结果已记录，AI coverage 反映运行时验收结果，PR 收口时无仍有价值的临时 RUNBOOK，TODO 只保留未完成任务。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
