# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `admin-web multipart tests`：补齐普通上传与分片上传前端测试闭环
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-WEB-STORAGE-MULTIPART-SYNC.md`、`docs/00-governance/TODO-RULES.md`
    - 范围对象：`kuzhambu-apps/admin-web/e2e/storage/storage-object/storage-object.spec.ts`、`kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-page.tsx`、`kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-service.ts`
    - 处理动作：扩展页面测试和 E2E mock，验证普通上传、multipart 成功路径、进度展示和取消触发 abort
    - 验收点：保留现有普通上传验证，新增 multipart 成功与取消场景，至少一层测试可断言上传状态渲染
    - 重要度：8/10

## 待审阅任务项

## 待讨论项
