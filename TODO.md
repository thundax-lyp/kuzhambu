# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `admin-web shared component`：新增 `KuzhambuSpace` 共享组件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-WEB-KUZHAMBU-SPACE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/components/kuzhambu-space/index.ts`、`kuzhambu-apps/admin-web/src/components/kuzhambu-space/kuzhambu-space.tsx`、`kuzhambu-apps/admin-web/src/components/kuzhambu-space/kuzhambu-space.test.tsx`
    - 处理动作：新增 `KuzhambuSpace` 和 `KuzhambuSpaceCompact`，并在类型层排除 `direction`
    - 验收点：业务代码可用共享组件替代 `antd Space`，且 `direction` 无法通过类型检查
    - 重要度：9/10

- [ ] `admin-web lint boundary`：增加禁止直连 `antd Space` 的 hard rule
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-WEB-KUZHAMBU-SPACE.md`
    - 范围对象：`kuzhambu-apps/admin-web/eslint.config.js`、`docs/00-governance/ADMIN-WEB-RULES.md`
    - 处理动作：增加 `ADMIN_WEB_UI_NO_ANTD_SPACE_DIRECT` 门禁并同步治理文档
    - 验收点：直接从 `antd` 导入 `Space` 或 `SpaceProps` 会被 ESLint 拦截，规则标签与文档一致
    - 重要度：10/10

- [ ] `admin-web shared entry`：替换共享骨架中的 `Space`
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-WEB-KUZHAMBU-SPACE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/layouts/admin-layout.tsx`、`kuzhambu-apps/admin-web/src/components/kuzhambu-list-page/kuzhambu-list-page.tsx`、`kuzhambu-apps/admin-web/src/components/placeholder-page.tsx`
    - 处理动作：把共享骨架和布局入口改为使用 `KuzhambuSpace`
    - 验收点：高复用入口不再导入 `antd Space`
    - 重要度：8/10

- [ ] `admin-web discovery pages`：替换已知 `direction` warning 页面
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-WEB-KUZHAMBU-SPACE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/discovery/search-admin/search-admin-page.tsx`、`kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-page.tsx`
    - 处理动作：将 discovery admin 页面中的 `Space` 改为共享组件并统一使用 `orientation`
    - 验收点：当前已知 `direction` 废弃 warning 页面完成替换
    - 重要度：9/10

- [ ] `admin-web knowledge compact batch-1`：替换第一批 `Space.Compact` 表格
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-WEB-KUZHAMBU-SPACE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-workbench-table.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-entity-table.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-relation-table.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-task-table.tsx`
    - 处理动作：把第一批高频表格操作列替换为 `KuzhambuSpaceCompact`
    - 验收点：该批文件不再直连 `Space.Compact`
    - 重要度：7/10

- [ ] `admin-web knowledge compact batch-2`：替换第二批图谱结果表格
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-WEB-KUZHAMBU-SPACE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/components/graph-version-table.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/components/graph-entity-table.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/components/graph-relation-table.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/components/graph-lineage-node-table.tsx`
    - 处理动作：把图谱结果表格中的 `Space.Compact` 替换为共享 compact 入口
    - 验收点：该批图谱结果表格不再直连 `Space.Compact`
    - 重要度：7/10

- [ ] `admin-web final verify`：收尾 lineage 表格并完成最终验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-WEB-KUZHAMBU-SPACE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/components/graph-lineage-relation-table.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/graph-results-page.tsx`、`kuzhambu-apps/admin-web/src/test/setup.ts`、`kuzhambu-apps/admin-web/package.json`
    - 处理动作：补齐最后一批替换并把测试验证入口收口到稳定状态
    - 验收点：`npm run format:check`、`npm run lint`、`npm run test` 可作为本轮统一验证入口
    - 重要度：8/10

## 待讨论项
