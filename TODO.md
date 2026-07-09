# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `02-graph-extraction-e2e`：补齐图谱抽取 Playwright 冒烟
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-RUNTIME-VALIDATION-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/e2e/knowledge/graph-extraction/graph-extraction.spec.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-create.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-task-table.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-task-detail.tsx`
    - 处理动作：覆盖任务创建、详情、应用、重生成和批次取消的控件操作与请求 payload 断言
    - 验收点：`pnpm --filter ./admin-web run e2e -- e2e/knowledge/graph-extraction/graph-extraction.spec.ts` 通过
    - 重要度：10/10

- [ ] `03-graph-results-e2e`：补齐正式图谱结果 Playwright 冒烟
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-RUNTIME-VALIDATION-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/e2e/knowledge/graph-results/graph-results.spec.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/graph-results-page.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/components/graph-version-table.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/components/graph-version-detail.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/components/graph-entity-detail.tsx`
    - 处理动作：覆盖版本列表、版本详情、正式实体、正式关系和世系结果下钻的控件操作与请求 payload 断言
    - 验收点：`pnpm --filter ./admin-web run e2e -- e2e/knowledge/graph-results/graph-results.spec.ts` 通过
    - 重要度：9/10

- [ ] `04-lineage-e2e`：补齐世系图 Playwright 冒烟
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-RUNTIME-VALIDATION-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/e2e/knowledge/lineage/lineage.spec.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/lineage/lineage-page.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/lineage/components/lineage-filter-bar.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/lineage/components/lineage-canvas.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/lineage/components/lineage-detail-panel.tsx`
    - 处理动作：覆盖版本、搜索、节点类型、关系类型、确认状态、深度、刷新、重置和画布节点/关系详情联动
    - 验收点：`pnpm --filter ./admin-web run e2e -- e2e/knowledge/lineage/lineage.spec.ts` 通过
    - 重要度：9/10

- [ ] `05-refinement-e2e`：补齐图谱精修 Playwright 冒烟
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-RUNTIME-VALIDATION-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/e2e/knowledge/refinement/refinement.spec.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-page.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-filter-form.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-entity-table.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-quality-annotation-drawer.tsx`
    - 处理动作：覆盖任务筛选、打开任务、实体草稿编辑、确认、质量标注和应用精修联动
    - 验收点：`pnpm --filter ./admin-web run e2e -- e2e/knowledge/refinement/refinement.spec.ts` 通过
    - 重要度：9/10

- [ ] `06-quality-report-e2e`：补齐质量报告 Playwright 冒烟
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-RUNTIME-VALIDATION-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/e2e/knowledge/quality-report/quality-report.spec.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-page.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/components/quality-report-generate-form.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/components/quality-report-summary.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/components/quality-report-source-table.tsx`
    - 处理动作：覆盖报告生成、最新报告、历史报告、报告详情和低质量门类重提取入口
    - 验收点：`pnpm --filter ./admin-web run e2e -- e2e/knowledge/quality-report/quality-report.spec.ts` 通过
    - 重要度：9/10

- [ ] `07-runtime-evidence`：执行 Knowledge 运行时验证并记录 evidence
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-RUNTIME-VALIDATION-CLOSURE.md`
    - 范围对象：`docs/40-readiness/KNOWLEDGE-RUNTIME-SMOKE-EVIDENCE.md`、`kuzhambu-servers/pom.xml`、`kuzhambu-workers/pyproject.toml`、`kuzhambu-apps/admin-web/package.json`
    - 处理动作：运行 RUNBOOK 中后端、Workers、Admin Web 统一验证命令，并把环境、命令、结果和关键断言写入 evidence
    - 验收点：`KNOWLEDGE-RUNTIME-SMOKE-EVIDENCE.md` 记录后端、Workers、Admin Web 验证均通过且包含 Knowledge Playwright 关键断言
    - 重要度：10/10

- [ ] `08-main-coverage-runbook-closure`：同步 main 并完成 Knowledge 验收文档收口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-RUNTIME-VALIDATION-CLOSURE.md`
    - 范围对象：`docs/40-readiness/KNOWLEDGE-RUNTIME-SMOKE-EVIDENCE.md`、`docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-KNOWLEDGE-RUNTIME-VALIDATION-CLOSURE.md`、`TODO.md`
    - 处理动作：同步最新 `origin/main`，确认验证结果仍有效后更新 Implementation Coverage 为已完成、清理 RUNBOOK 并收窄或删除已完成 TODO
    - 验收点：分支基于最新 `origin/main`，`KNOWLEDGE-IMPLEMENTATION-COVERAGE.md` 的运行时验证为 `已完成`，RUNBOOK 已删除，TODO 仅保留未完成事项
    - 重要度：10/10


## 待审阅任务项

## 待讨论项
