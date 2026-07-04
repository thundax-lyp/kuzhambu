# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `portal qa-service; admin qa-admin-service; discovery-service-contract.test.ts; portal qa-page.test.tsx`：增加跨前端契约检查
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/discovery/qa-service.ts`; `kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-service.ts`; `kuzhambu-apps/admin-web/src/pages/discovery/discovery-service-contract.test.ts`; `kuzhambu-apps/portal-web/src/pages/discovery/qa-page.test.tsx`
    - 处理动作：锁定 Portal/Admin 只调用 Discovery API 且不直连 provider。
    - 验收点：前端测试证明无 `/question/ask` 和 provider direct URL。
    - 重要度：9/10

- [ ] `DISCOVERY-DESIGN.md; DISCOVERY-IMPLEMENTATION-COVERAGE.md; RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`：收口设计和 readiness 文档
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`
    - 范围对象：`docs/30-designs/DISCOVERY-DESIGN.md`; `docs/40-readiness/DISCOVERY-IMPLEMENTATION-COVERAGE.md`; `docs/30-designs/RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`
    - 处理动作：同步最终 QA 知识库设计、覆盖状态，并在实现合并后删除 RUNBOOK。
    - 验收点：目标状态文档不再把旧 QA source、debug context 或 `/question/ask` 当成目标。
    - 重要度：8/10

## 待审阅任务项

## 待讨论项
