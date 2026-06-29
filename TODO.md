# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `admin-web multipart service`：收口 Storage 页面分片上传契约与分流策略
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-WEB-STORAGE-MULTIPART-SYNC.md`、`docs/00-governance/TODO-RULES.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-service.ts`、`kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-types.ts`
    - 处理动作：在页面 service 中定义 multipart 接口契约、上传状态数据结构和统一入口 `uploadStorageFile`
    - 验收点：service 层具备普通上传与 multipart 自动分流、进度回调和取消能力，页面不再拼接 multipart 底层步骤
    - 重要度：9/10

- [ ] `admin-web multipart page`：为 Storage 页面接入上传状态与任务卡片
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-WEB-STORAGE-MULTIPART-SYNC.md`、`docs/00-governance/TODO-RULES.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-page.tsx`、`kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-page.css`、`kuzhambu-apps/admin-web/src/pages/storage/storage-object/components/storage-upload-task-card.tsx`、`kuzhambu-apps/admin-web/src/pages/storage/storage-object/components/storage-upload-task-card.css`
    - 处理动作：在 Storage 页面接入单任务上传状态机、进度展示、取消入口和上传完成后的列表刷新
    - 验收点：页面仍只有一个上传入口，大文件上传时可见阶段与进度，可取消，成功后刷新列表，失败后显示错误
    - 重要度：8/10

- [ ] `admin-web multipart tests`：补齐普通上传与分片上传前端测试闭环
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-WEB-STORAGE-MULTIPART-SYNC.md`、`docs/00-governance/TODO-RULES.md`
    - 范围对象：`kuzhambu-apps/admin-web/e2e/storage/storage-object/storage-object.spec.ts`、`kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-page.tsx`、`kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-service.ts`
    - 处理动作：扩展页面测试和 E2E mock，验证普通上传、multipart 成功路径、进度展示和取消触发 abort
    - 验收点：保留现有普通上传验证，新增 multipart 成功与取消场景，至少一层测试可断言上传状态渲染
    - 重要度：8/10

## 待审阅任务项

## 待讨论项
