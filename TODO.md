# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `System Storage 接口闭环`：执行接口闭环验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-SYSTEM-STORAGE-LOOP.md`
    - 范围对象：`dev.env`、`kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/resources/application.yml`、`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/admin/SystemAdminControllerTest.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/test/java/com/thundax/kuzhambu/storage/interfaces/admin/StorageObjectControllerTest.java`
    - 处理动作：验证 System 登录态、System 管理查询、Storage 上传读取删除的接口链路
    - 验收点：接口闭环包含成功路径和至少一个失败路径，且不依赖 Admin Web 页面
    - 重要度：10/10

- [ ] `admin-web 页面闭环`：执行 Admin Web 页面闭环验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-SYSTEM-STORAGE-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/package.json`、`kuzhambu-apps/admin-web/e2e/auth/login.spec.ts`、`kuzhambu-apps/admin-web/e2e/system/system-pages.spec.ts`、`kuzhambu-apps/admin-web/e2e/storage/storage-object.spec.ts`
    - 处理动作：用 Playwright 验证登录、菜单导航、System 页面访问和 Storage 上传删除页面链路
    - 验收点：`npm run e2e --workspace admin-web` 通过并在 PR 描述记录命令、结果和关键截图位置
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
