# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 本清单按图谱素材/任务的 Admin Web RUNBOOK 固定顺序拆分；每项对应一个可独立验证的小步提交。
- 未经本轮人工确认的执行任务均在“待审阅任务项”；确认后才移入“当前任务项”执行。
- 已完成任务必须在其完成 commit 中删除；完成历史保留在 commit 和 PR。

## 当前任务项

## 待审阅任务项

- [ ] `AW/graph-material draft canvas`：改造素材草稿画布段
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/material-draft-canvas/material-draft-canvas.tsx`
    - 处理动作：将草稿画布限制为素材 drawer 的 `DRAFT_GRAPH` 段并处理已发布只读状态。
    - 验收点：测试覆盖已发布素材画布只读，且关闭抽屉后不保留旧草稿对象状态。
    - 重要度：8/10

- [ ] `AW/graph-material task summary panel`：实现素材任务摘要段
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/material-task-summary-panel/`
    - 处理动作：实现素材详情中的任务摘要和跳转入口。
    - 验收点：测试覆盖任务段无草稿编辑控件，且“查看任务”跳转到提取任务并携带素材引用。
    - 重要度：8/10

- [ ] `AW/graph-material publication panel`：改造发布与变更段
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/publication-preview/publication-preview.tsx`
    - 处理动作：将发布、撤回和删除预检入口收敛到素材 drawer 的发布与变更段。
    - 验收点：测试覆盖独立删除变更/删除任务菜单不再作为新流程入口。
    - 重要度：9/10

- [ ] `AW/graph-extraction drawer shell`：建立任务分段详情抽屉
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/task-detail-drawer/`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-task-detail/`
    - 处理动作：用 `KuzhambuSegmentedDrawer` 建立任务详情四段容器并接入 `getTask`。
    - 验收点：测试覆盖 `OVERVIEW`、`EXECUTION`、`CANDIDATE`、`DISPOSITION` 四段可访问。
    - 重要度：9/10

- [ ] `AW/graph-extraction execution panel`：实现任务执行过程段
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/task-execution-panel/`
    - 处理动作：按阶段展示进度、摘要、失败原因和时间字段。
    - 验收点：测试覆盖阶段进度、失败原因展示，且不显示完整正文或提示词。
    - 重要度：8/10

- [ ] `AW/graph-extraction candidate panel`：实现候选预览段
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/task-candidate-panel/`
    - 处理动作：展示候选节点、边、告警和 diff 预览。
    - 验收点：测试覆盖 `ADD`、`UPDATE`、`REMOVE`、`CONFLICT` 和 `candidate:null` 候选不可用空态。
    - 重要度：9/10

- [ ] `AW/graph-extraction disposition panel`：实现候选处置动作段
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/task-disposition-panel/`
    - 处理动作：按任务状态渲染重试、取消、合并、覆盖、丢弃和重新抽取动作。
    - 验收点：测试覆盖失败、运行中、成功待审和已处置状态下的按钮可见性。
    - 重要度：9/10

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
