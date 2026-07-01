# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `AI Candidate Frontend Contract`：补齐前端 AI 候选 objectId 契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-SINGLE-IMAGE-ANALYSIS-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/common/ai-candidate-service.ts`、`kuzhambu-apps/admin-web/src/pages/classics/common/ai-candidate-types.ts`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-service-contract.test.ts`
    - 处理动作：为前端候选列表与候选应用契约增加 `objectId` 并固定 service contract
    - 验收点：前端 service contract 覆盖 `objectId`，请求路径与请求体稳定
    - 重要度：9/10

- [ ] `Sancai Candidate Panel Wiring`：补齐单图候选预览与人工确认接线
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-SINGLE-IMAGE-ANALYSIS-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-model.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.test.tsx`
    - 处理动作：在三才视觉资产上下文中只展示当前 `visualAsset` 的 `image_analysis` 候选，并完成应用、拒绝与刷新联动
    - 验收点：候选面板只看当前单图，应用后 `imageAnalysisMarkdown`、`fusionDescription`、`visualDescription` 与视觉资产列表都会刷新
    - 重要度：10/10

- [ ] `Workers Image Analysis Contract`：补齐 workers 图片理解契约回归
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-SINGLE-IMAGE-ANALYSIS-CLOSURE.md`
    - 范围对象：`kuzhambu-workers/tests/test_ai_usecase_routes_classics.py`、`kuzhambu-workers/tests/test_ai_usecase_registry.py`
    - 处理动作：固定三才图片理解 usecase 的 route 与 registry 映射契约
    - 验收点：`/internal/ai/classics/sancai/image-analysis` 路径与 `image_analysis` registry 映射都有测试保护
    - 重要度：7/10

- [ ] `Sancai Coverage And Closure`：更新覆盖文档并清理 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-SINGLE-IMAGE-ANALYSIS-CLOSURE.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-SANCAI-SINGLE-IMAGE-ANALYSIS-CLOSURE.md`、`TODO.md`
    - 处理动作：更新三才图片理解闭环覆盖口径并删除本轮 RUNBOOK，同时收窄或清空 TODO
    - 验收点：Coverage 与真实实现一致，RUNBOOK 删除，`TODO.md` 只保留剩余未完成任务
    - 重要度：8/10

- [ ] `Sancai Final Verify`：执行全量格式化、静态检查、构建与测试
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`kuzhambu-servers/`、`kuzhambu-apps/`、`kuzhambu-workers/`
    - 处理动作：按仓库规则执行本轮涉及模块的最终格式化、静态检查、构建与测试
    - 验收点：至少完成 `cd kuzhambu-servers && mvn -q spotless:check && mvn -q checkstyle:check && mvn -q test`、`cd kuzhambu-apps && npm run format:check && npm run lint && npm run build && npm test`，若 workers 改动则完成 `cd kuzhambu-workers && .venv/bin/python -m ruff format --check . && .venv/bin/python -m ruff check . && .venv/bin/python -m pytest -p no:capture`
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
