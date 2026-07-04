# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `portal-web qa-service.ts; qa-types.ts; qa-page.tsx; qa-page.test.tsx`：实现 Portal QA 页面闭环
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/discovery/qa-service.ts`; `kuzhambu-apps/portal-web/src/pages/discovery/qa-types.ts`; `kuzhambu-apps/portal-web/src/pages/discovery/qa-page.tsx`; `kuzhambu-apps/portal-web/src/pages/discovery/qa-page.test.tsx`
    - 处理动作：将 Portal QA 页面改为 Discovery `chat/completions`、会话消息流和来源展示。
    - 验收点：Portal QA 测试覆盖首问、追问、答案、来源、失败态和移除 `/question/ask`。
    - 重要度：10/10

- [ ] `admin-web qa-admin-service.ts; qa-admin-types.ts; qa-admin-page.tsx; qa-admin-page.test.tsx; discovery-service-contract.test.ts`：实现 Admin QA 运维页面
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-service.ts`; `kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-types.ts`; `kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-page.tsx`; `kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-page.test.tsx`; `kuzhambu-apps/admin-web/src/pages/discovery/discovery-service-contract.test.ts`
    - 处理动作：实现 QA 知识库健康、重建、同步、同步列表、来源和 trace 运维视图。
    - 验收点：Admin QA 页面和 service contract 测试覆盖 health、rebuild、sync、trace 和无 provider 直连。
    - 重要度：10/10

- [ ] `kuzhambu-workers README/main/tests`：锁定 Workers 边界
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`
    - 范围对象：`kuzhambu-workers/README.md`; `kuzhambu-workers/src/kuzhambu_workers/__init__.py`; `kuzhambu-workers/src/kuzhambu_workers/main.py`; `kuzhambu-workers/tests/test_ai_usecase_routes_discovery.py`; `kuzhambu-workers/tests/test_workers_architecture.py`
    - 处理动作：确认并测试 Workers 不承载正式 Discovery QA 问答或知识同步能力。
    - 验收点：Workers 无 Discovery QA runtime endpoint 或知识同步 task。
    - 重要度：8/10

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
