# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `Admin quality report regenerate prompt`：质量报告页提示精修后重算
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-GRAPH-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-page.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/components/quality-report-generate-form.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/components/quality-report-summary.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-types.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-page.test.tsx`
    - 处理动作：让质量报告页按 `graphVersionId` 定位并在报告过期时展示重新生成入口。
    - 验收点：过期报告展示 warning Alert，点击重新生成后刷新报告历史、摘要、问题、来源和标注明细。
    - 重要度：8/10

- [ ] `Admin knowledge frontend verification`：补齐 Admin Knowledge 前端联动测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-GRAPH-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-page.test.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-page.test.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/graph-results-page.test.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-page.test.tsx`
    - 处理动作：为精修成功动作区、重生成预填、版本定位和质量报告重算提示补齐前端测试。
    - 验收点：前端测试能验证三个精修后续按钮、重生成 payload、版本定位和 stale Alert。
    - 重要度：9/10

- [ ] `branch sync main`：收口前同步 main 分支代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`feat/knowledge-refinement-graph-loop`、`origin/main`
    - 处理动作：在功能实现和定向测试补齐后同步 `origin/main` 到当前功能分支并解决冲突。
    - 验收点：当前分支包含最新 `origin/main`，且没有无关冲突残留。
    - 重要度：10/10

- [ ] `Knowledge final validation`：同步 main 后运行最终验证
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`kuzhambu-servers/`、`kuzhambu-apps/`
    - 处理动作：同步 main 后运行 RUNBOOK 指定的后端格式、静态检查、测试和前端格式、lint、测试。
    - 验收点：后端 `spotless:check`、`checkstyle:check`、Knowledge 测试和前端 `format:check`、`lint`、admin-web 测试通过或记录明确阻塞。
    - 重要度：10/10

- [ ] `Knowledge Implementation Coverage`：更新 Knowledge 实现覆盖状态
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：将 Knowledge 精修与图谱联动闭环、最终验证命令和剩余缺口同步到 Implementation Coverage。
    - 验收点：Coverage 记录精修应用、图谱重生成引导、版本定位和质量报告重算已完成。
    - 重要度：9/10

- [ ] `RUNBOOK cleanup`：清理 Knowledge 精修图谱联动 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-GRAPH-LOOP.md`、`TODO.md`
    - 处理动作：阶段完成并同步 coverage 后删除临时 RUNBOOK 并从 TODO 中删除已完成任务。
    - 验收点：PR 收口前无已完成 RUNBOOK 残留，`TODO.md` 只保留真实未完成事项或为空。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
