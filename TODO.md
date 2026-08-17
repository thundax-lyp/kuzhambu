# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 本清单按图谱素材/任务的 Admin Web RUNBOOK 固定顺序拆分；每项对应一个可独立验证的小步提交。
- 未经本轮人工确认的执行任务均在“待审阅任务项”；确认后才移入“当前任务项”执行。
- 已完成任务必须在其完成 commit 中删除；完成历史保留在 commit 和 PR。

## 当前任务项

## 待审阅任务项

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
