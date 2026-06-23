# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `admin-web/knowledge extraction create actions`：支持三类抽取任务创建
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-AI-WORKERS-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-create.tsx`、`graph-extraction-page.tsx`、`graph-extraction-service.ts`、组件测试文件
    - 处理动作：接通 relation、graph、lineage 三类抽取任务创建动作
    - 验收点：前端页面能发起三类抽取任务并反映创建结果
    - 重要度：7/10

- [ ] `admin-web/knowledge extraction detail apply`：支持详情查看与应用动作
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-AI-WORKERS-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-task-table.tsx`、`graph-extraction-task-detail.tsx`、`graph-extraction-page.tsx`、组件测试文件
    - 处理动作：接通任务列表、详情抽屉与候选结果应用动作
    - 验收点：前端能展示 `aiCallId`、`aiCandidateId`、错误信息、时间戳并触发应用
    - 重要度：8/10

- [ ] `docs/knowledge ai workers`：同步设计与 readiness 文档
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/30-designs/KNOWLEDGE-DESIGN.md`、`AI-DESIGN.md`、`WORKERS-DESIGN.md`、`docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：仅按已落地结果同步设计和覆盖状态文档
    - 验收点：文档口径与代码现状一致且不记录未落地能力
    - 重要度：6/10

- [ ] `runbook and todo cleanup`：收口 Knowledge AI Workers 现场
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-AI-WORKERS-CLOSURE.md`
    - 范围对象：`docs/30-designs/RUNBOOK-KNOWLEDGE-AI-WORKERS-CLOSURE.md`、`TODO.md`
    - 处理动作：在阶段任务完成后删除无剩余价值的 runbook 并清空或收窄 TODO
    - 验收点：PR 收口前 runbook 与 TODO 只保留下一阶段仍未关闭内容
    - 重要度：6/10

## 待审阅任务项

## 待讨论项
