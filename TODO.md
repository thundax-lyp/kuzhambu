# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

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
