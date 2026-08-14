# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 本清单按 `RUNBOOK-GRAPH-ADMIN-WEB.md` 的固定顺序拆分；每项预计触及 2–10 个文件，并对应一个可独立验证的小步提交。
- 未经本轮人工确认的执行任务均在“待审阅任务项”；确认后才移入“当前任务项”执行。
- 已完成任务必须在其完成 commit 中删除；完成历史保留在 commit 和 PR。

## 当前任务项

## 待审阅任务项

- [ ] `11` `admin-web graph deletion tasks`：实现删除任务列表
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-ADMIN-WEB.md`
    - 范围对象：`src/pages/knowledge/graph-deletion-task/graph-deletion-task-page.tsx`、`graph-deletion-task-types.ts`、`graph-deletion-task-page.test.tsx`、`deletion-task-detail-drawer/`（4–5 个文件）
    - 处理动作：实现删除任务、失败原因和重试入口的 Mock 列表。
    - 验收点：失败任务显示原因并可重试；测试覆盖详情、重试后的状态变化和空态。
    - 重要度：9/10

- [ ] `12` `admin-web graph mock e2e`：覆盖图谱 Mock 端到端流程
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/e2e/knowledge/graph/graph-mock.spec.ts`、`graph-mock.fixture.ts`（2 个文件）
    - 处理动作：通过 Mock provider 覆盖抽取至撤回、治理合并拆分、删除预检至重试和工作台截断。
    - 验收点：E2E 不访问后端端口；完整流程与发布部分失败、删除任务失败重试分支均通过。
    - 重要度：10/10

- [ ] `13` `graph admin-web closure`：清理图谱 Admin Web 任务现场
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-ADMIN-WEB.md`、`docs/00-governance/TODO-RULES.md`
    - 范围对象：`TODO.md`、`docs/30-designs/RUNBOOK-GRAPH-ADMIN-WEB.md`（2 个文件）
    - 处理动作：在全部前置任务及完整验证完成后，删除本任务 TODO 和 RUNBOOK。
    - 验收点：不保留已完成任务、完成历史、失效 RUNBOOK 或临时文件；Mock 路由、provider、页面单测与 Mock E2E 保留。
    - 重要度：10/10

## 待讨论项
