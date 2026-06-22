# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `knowledge taxonomy tag components A`：创建标签表格与编辑组件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-MVP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/tag-table.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/tag-edit.tsx`
    - 处理动作：创建统一标签表格与编辑组件。
    - 验收点：2 个组件存在且能支撑标签列表、新增、编辑、启用、禁用。
    - 重要度：9/10

- [ ] `knowledge taxonomy tag components B`：创建标签审核与详情组件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-MVP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/tag-review-table.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/tag-detail-drawer.tsx`
    - 处理动作：创建待审核标签表格与标签详情抽屉组件。
    - 验收点：2 个组件存在且能支撑标签审核、详情查看、内容引用只读展示。
    - 重要度：9/10

- [ ] `knowledge taxonomy tag alias component`：创建标签别名列表组件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-MVP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/tag-alias-list.tsx`
    - 处理动作：创建标签别名列表组件。
    - 验收点：组件存在且能支撑标签详情中的别名列表、新增、删除。
    - 重要度：7/10

- [ ] `knowledge taxonomy synonym components`：创建同义词表格与编辑组件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-MVP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/synonym-table.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/synonym-edit.tsx`
    - 处理动作：创建同义词表格与编辑组件。
    - 验收点：2 个组件存在且能支撑同义词分页、新增、编辑、启用、禁用、删除。
    - 重要度：8/10

- [ ] `knowledge menu seed source`：补充知识治理菜单 JSON 源
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-MVP.md`
    - 范围对象：`db/data-source/system.json`
    - 处理动作：按现有菜单结构新增 `知识治理` 一级菜单与 `标签与同义词` 二级菜单。
    - 验收点：`system.json` 中存在 `/knowledge` 与 `/knowledge/taxonomy` 菜单项且权限码、图标、优先级字段完整。
    - 重要度：9/10

- [ ] `knowledge menu seed sql`：根据 JSON 生成菜单 SQL
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-MVP.md`
    - 范围对象：`db/data-source/system.json`、`db/data/system.sql`
    - 处理动作：基于更新后的菜单 JSON 重新生成 system 初始化 SQL。
    - 验收点：`db/data/system.sql` 包含知识治理菜单数据且与 `db/data-source/system.json` 一致。
    - 重要度：9/10

- [ ] `knowledge implementation coverage`：补充 Knowledge Implementation Coverage
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-MVP.md`
    - 范围对象：`docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：新增 Knowledge Implementation Coverage 文档并记录本次 MVP 的需求覆盖矩阵与未覆盖范围。
    - 验收点：`docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md` 存在且明确标记本次已覆盖、未覆盖、超出范围项。
    - 重要度：8/10

- [ ] `knowledge mvp closeout`：清理现场
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-MVP.md`、`docs/00-governance/TODO-RULES.md`
    - 范围对象：`TODO.md`、`docs/30-designs/RUNBOOK-KNOWLEDGE-MVP.md`
    - 处理动作：在全部实现与覆盖文档完成后删除已完成 TODO 项并清理不再有价值的 RUNBOOK。
    - 验收点：`TODO.md` 仅保留剩余未完成任务且 `RUNBOOK-KNOWLEDGE-MVP.md` 在任务关闭时被删除或收窄为仍有残余价值的内容。
    - 重要度：10/10

## 待讨论项
