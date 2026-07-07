# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `三才视觉资产页面接入`：接通三才视觉任务流式展示、候选刷新和失败重试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-AI-STREAMING-CANDIDATES.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/hooks/use-sancai-entry-panel-state.ts`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.css`
    - 处理动作：在三才视觉资产编辑区展示流式过程卡片并在 completed 后刷新候选区。
    - 验收点：点击 `创建图片理解任务` 或 `创建生图任务` 后出现 `AI 流式过程`，completed 后 `AI 候选确认` 出现候选，失败后 `重试` 创建新任务且 `requestId/traceId` 变化。
    - 重要度：10/10

- [ ] `同步 main 分支`：收口前同步最新 main 代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`feat/classics-ai-streaming-candidates` worktree 分支
    - 处理动作：在收口前把最新 `main` 合入当前分支并处理冲突。
    - 验收点：当前分支包含最新 `main`，且无未解决冲突。
    - 重要度：10/10

- [ ] `最终验证`：运行后端和前端相关验证命令
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-AI-STREAMING-CANDIDATES.md`、`docs/00-governance/TODO-RULES.md`
    - 范围对象：`kuzhambu-servers/`、`kuzhambu-apps/`
    - 处理动作：在同步 main 后运行 RUNBOOK 中列出的 Java 和 Admin Web 格式、静态检查与测试命令。
    - 验收点：记录 `mvn spotless:check`、`mvn checkstyle:check`、相关 Maven test、`npm run format:check`、`npm run lint`、admin-web test 的结果。
    - 重要度：10/10

- [ ] `Implementation Coverage`：同步 AI 和 Classics 实现覆盖状态
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-AI-STREAMING-CANDIDATES.md`、`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`、`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：记录 Classics 三才视觉流式候选闭环的实现覆盖状态和剩余风险。
    - 验收点：coverage 文档包含 stream 展示、候选生成、失败重试、main 同步和验证结果的最新口径。
    - 重要度：8/10

- [ ] `RUNBOOK 清理`：完成闭环后删除临时 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/00-governance/DOCUMENT-RULES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-CLASSICS-AI-STREAMING-CANDIDATES.md`
    - 处理动作：在实现、文档同步、验证和 main 同步完成后删除临时 RUNBOOK。
    - 验收点：PR 收口前不再保留本 RUNBOOK，且稳定文档已承载必要口径。
    - 重要度：8/10

## 待审阅任务项

## 待讨论项
