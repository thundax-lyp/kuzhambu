# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 本清单按图谱素材/任务的 Admin Web RUNBOOK 固定顺序拆分；每项对应一个可独立验证的小步提交。
- 未经本轮人工确认的执行任务均在“待审阅任务项”；确认后才移入“当前任务项”执行。
- 已完成任务必须在其完成 commit 中删除；完成历史保留在 commit 和 PR。

## 当前任务项

## 待审阅任务项

- [ ] `AW/graph-extraction task mutations`：实现任务 mutation 刷新与冲突处理
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/task-detail-drawer/`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/task-disposition-panel/`
    - 处理动作：每次任务写操作发送当前 `lockVersion` 和预期状态，并在成功后失效相关 query。
    - 验收点：测试覆盖版本冲突只刷新不猜测最终状态，且处置成功后按钮消失。
    - 重要度：9/10

- [ ] `AW knowledge graph service switch`：切换真实 Knowledge HTTP service
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/graph-material-service.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-service.ts`
    - 处理动作：保持领域类型和组件不变，将 adapter 入口从 Mock 切换到 `postJson`。
    - 验收点：contract tests 仍通过，且错误处理通过业务码映射而不是字符串 message。
    - 重要度：8/10

- [ ] `AW knowledge graph e2e`：补充素材任务端到端验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/e2e/knowledge-graph-material-task.spec.ts`
    - 处理动作：补充素材页、素材 drawer、单项提取、任务 drawer、失败重试、候选采用和批量部分失败 E2E。
    - 验收点：E2E 验证 Network 只请求 `/knowledge/graph/**`，不请求 Classics、AI 或旧图谱 URL。
    - 重要度：8/10

- [ ] `AW knowledge graph readiness evidence`：记录真实联调开发中证据
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`docs/40-readiness/`
    - 处理动作：记录 Knowledge HTTP service 完整可联调前提下的 Admin Web 开发中验证结果。
    - 验收点：readiness 证据覆盖素材页、素材 drawer、任务 drawer、失败重试、候选采用和批量部分失败。
    - 重要度：8/10

- [ ] `AW legacy graph extraction cleanup`：独立清理旧图谱提取组件和旧 service
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-manuscript-tree/`、`graph-extraction-manuscript-detail/`、`graph-workbench-service.ts`、`graph-extraction-candidate-modal.tsx`
    - 处理动作：在 W4/W5 停止引用后，单独删除无引用的旧组件和旧 service。
    - 验收点：`rg` 证明无引用，相关单测、lint 和 build 通过。
    - 重要度：7/10
