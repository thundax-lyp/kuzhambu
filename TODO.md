# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `sancai-entry-panel.tsx`：接通三才图会精修任务创建与轮询
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-WORKERS-CLASSICS-TEXT-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`
    - 处理动作：在三才图会详情页接通 `translate/summary` 的任务创建与轮询，并在任务成功后刷新候选与详情
    - 验收点：页面可以先创建 `translate` 与 `summary` 任务，再在任务成功后看到候选并刷新 `translationText`、`translationStatus`、`summary`
    - 重要度：10/10

- [ ] `wangqi-page.tsx/ming-customs-page.tsx`：接通汪耆与明俗 summary 任务创建与轮询
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-WORKERS-CLASSICS-TEXT-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.test.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.test.tsx`
    - 处理动作：在汪耆文稿和明代风俗详情页接通 `summary` 的任务创建与轮询，并在候选应用后刷新详情
    - 验收点：页面可以先创建 `summary` 任务，再在任务成功后看到候选并刷新 `WANGQI_DOCUMENT.summary` 与 `MING_CUSTOMS.summary`
    - 重要度：9/10

- [ ] `Implementation Coverage/RUNBOOK`：执行全域验证并完成文档收口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-WORKERS-CLASSICS-TEXT-CLOSURE.md`、`docs/00-governance/TODO-RULES.md`、`docs/40-readiness/PR-WORKFLOW.md`
    - 范围对象：`docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`、`docs/40-readiness/WORKERS-IMPLEMENTATION-COVERAGE.md`、`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-AI-WORKERS-CLASSICS-TEXT-CLOSURE.md`
    - 处理动作：执行 `kuzhambu-servers`、`kuzhambu-apps`、`kuzhambu-workers` 全域 `format/lint/test/build`，更新 Implementation Coverage，并在任务完成后清理 RUNBOOK
    - 验收点：完成 servers/apps/workers 全域验证，Coverage 文档更新到真实闭环状态，已完成且无剩余价值的 RUNBOOK 被删除
    - 重要度：10/10

## 待讨论项
