# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `07-admin-web-taxonomy-ui`：实现标签治理页 AI 抽取标签控件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-KNOWLEDGE-TAG-EXTRACTION.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-page.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/tag-extraction-drawer.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/tag-extraction-candidate-table.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-page.css`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-page.test.tsx`
    - 处理动作：新增 `AI 抽取标签` 按钮、抽取 drawer、候选表格和应用确认操作
    - 验收点：页面测试覆盖打开 drawer、填写控件、开始抽取、勾选候选、确认应用和刷新标签查询
    - 重要度：8/10

- [ ] `08-main-sync`：收口前同步 main 分支代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：当前工作分支与 `main`
    - 处理动作：在实现完成后同步 `main` 最新代码并解决冲突
    - 验收点：工作分支包含 `main` 最新代码且 `git status` 无未解释冲突
    - 重要度：9/10

- [ ] `09-final-validation`：运行 AI Knowledge Tag Extraction 最小闭环验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-KNOWLEDGE-TAG-EXTRACTION.md`
    - 范围对象：`kuzhambu-servers`、`kuzhambu-apps/admin-web`、`docs/30-designs/RUNBOOK-AI-KNOWLEDGE-TAG-EXTRACTION.md`
    - 处理动作：运行 RUNBOOK 指定的 Java 与 admin-web 格式化、静态检查和测试
    - 验收点：相关 Maven、Prettier、ESLint 和 taxonomy 测试通过，失败项已定位并修复
    - 重要度：10/10

- [ ] `10-coverage-runbook-cleanup`：更新 AI Implementation Coverage 并清理 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-KNOWLEDGE-TAG-EXTRACTION.md`
    - 范围对象：`docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-AI-KNOWLEDGE-TAG-EXTRACTION.md`
    - 处理动作：把 `KNOWLEDGE_TAG_EXTRACTION` 移入已完成矩阵并删除已完成 RUNBOOK
    - 验收点：`AI-IMPLEMENTATION-COVERAGE.md` 不再保留 `KNOWLEDGE_TAG_EXTRACTION` 未完成项，且 RUNBOOK 文件已清理
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
