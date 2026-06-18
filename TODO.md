# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `admin-web-sancai-volume-service`：拆分 Volume service
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-TREE-PAGE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-volume-service.ts`
    - 处理动作：提供 `list/add/update/deleteById/sort/listTypes`
    - 验收点：service contract 测试覆盖 volume 请求路径
    - 重要度：8/10

- [ ] `admin-web-sancai-entry-service`：拆分 Entry service
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-TREE-PAGE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`
    - 处理动作：提供 `list/add/update/deleteById/sort`
    - 验收点：service contract 测试覆盖 `entries/list/add/update/delete/sort`
    - 重要度：9/10

- [ ] `admin-web-sancai-category-list-model`：纯化 Category List 和 Model
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-TREE-PAGE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-category-list.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-category-model.tsx`
    - 处理动作：让 List 只展示事件、Model 只提交表单值
    - 验收点：两个组件不 import service、不调用 mutation
    - 重要度：8/10

- [ ] `admin-web-sancai-volume-list-model`：纯化 Volume List 和 Model
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-TREE-PAGE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-volume-list.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-volume-model.tsx`
    - 处理动作：让 List 只展示事件、Model 只提交表单值
    - 验收点：两个组件不 import service、不调用 mutation
    - 重要度：8/10

- [ ] `admin-web-sancai-entry-list-model`：新增 Entry List 和 Model
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-TREE-PAGE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-list.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-model.tsx`
    - 处理动作：新增 Entry 展示列表和纯表单 Model
    - 验收点：Entry List 只抛事件，Entry Model 只抛 submit values
    - 重要度：8/10

- [ ] `admin-web-sancai-category-panel`：Category Panel 接管 CRUD
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-TREE-PAGE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-category-panel.tsx`
    - 处理动作：在 Panel 中执行 category query、add、update、deleteById、sort
    - 验收点：新增、修改、删除、排序均由 Category Panel 调用 category service
    - 重要度：9/10

- [ ] `admin-web-sancai-volume-panel`：Volume Panel 接管 CRUD
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-TREE-PAGE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-volume-panel.tsx`
    - 处理动作：在 Panel 中执行 volume query、add、update、deleteById、sort
    - 验收点：新增、修改、删除、排序均由 Volume Panel 调用 volume service
    - 重要度：9/10

- [ ] `admin-web-sancai-entry-panel`：新增 Entry Panel 并接管 CRUD
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-TREE-PAGE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
    - 处理动作：在 Panel 中执行 entry query、add、update、deleteById、sort
    - 验收点：Entry CRUD 不发生在 Page、List 或 Model
    - 重要度：9/10

- [ ] `admin-web-sancai-tree-panel`：新增目录树 Panel
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-TREE-PAGE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-catalog-tree-panel.tsx`
    - 处理动作：渲染 `Category -> Volume -> Entry` 树并抛出选择和展开事件
    - 验收点：Tree Panel 不 import service，树节点点击能触发页面级选择
    - 重要度：9/10

- [ ] `admin-web-sancai-page-tree-state`：Page 维护树选中和联动
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-TREE-PAGE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.tsx`
    - 处理动作：由 Page 装配三列表树并维护 selected category、volume、entry
    - 验收点：树点击和右侧列表项点击共享同一组选中状态
    - 重要度：9/10

- [ ] `admin-web-sancai-page-toolbar`：Page 同步工具栏上下文
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-TREE-PAGE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.tsx`
    - 处理动作：接入 `KuzhambuListPage`，让新增按钮、搜索、筛选随当前 Panel 变化
    - 验收点：Category、Volume、Entry、Content 上下文下工具栏文案和字段均正确
    - 重要度：8/10

- [ ] `classics-sancai-content-interface`：补齐 Content 后端接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-TREE-PAGE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai`
    - 处理动作：新增 Content 的 `list/add/update/delete/sort` 明确接口
    - 验收点：接口测试覆盖按 `entryId` 查询 Content，且未新增或使用 `save`
    - 重要度：7/10

- [ ] `admin-web-sancai-content-service`：新增 Content service
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-TREE-PAGE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-content-service.ts`
    - 处理动作：提供 `listByEntry/add/update/deleteById/sort`
    - 验收点：service contract 测试覆盖 content 请求路径
    - 重要度：7/10

- [ ] `admin-web-sancai-content-panel`：新增 Content Panel/List/Model
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-TREE-PAGE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-content-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-content-list.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-content-model.tsx`
    - 处理动作：新增 Content Panel 并在 Panel 内执行 Content CRUD
    - 验收点：未选中 Entry 时不请求 Content，选中 Entry 时按当前 `entryId` 请求
    - 重要度：7/10

- [ ] `admin-web-sancai-cleanup`：清理旧三才页面结构
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-TREE-PAGE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai`
    - 处理动作：删除旧双列目录样式、旧 service 聚合函数和过渡状态
    - 验收点：三才页面 lint、相关测试和 build 通过，完成项从 TODO 删除
    - 重要度：7/10

## 待审阅任务项

## 待讨论项
