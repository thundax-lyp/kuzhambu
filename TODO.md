# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `admin-web/classics/sancai`：收口 Sancai 聚合内容治理面板
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-CONTENT-REFINEMENT.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-model.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.css`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`
    - 处理动作：在 `Sancai` 条目抽屉中补齐标签和问答对入口，并与摘要、翻译、版本和 AI 候选形成统一闭环
    - 验收点：`Sancai` 成为完整内容治理工作台，翻译候选、摘要候选、标签维护和问答维护可在同一抽屉连续完成
    - 重要度：10/10

- [ ] `docs/classics`：同步实现覆盖状态
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-CONTENT-REFINEMENT.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：在闭环实现完成后同步更新 `Classics` 实现覆盖文档
    - 验收点：Coverage 文档准确反映 `summary / tags / qa / AI 候选确认` 的交付状态
    - 重要度：8/10

## 待审阅任务项

## 待讨论项
