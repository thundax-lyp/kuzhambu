# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `admin-web System service`：对齐 System 页面写操作请求字段
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-SYSTEM-STORAGE-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/system/user/user-service.ts`、`kuzhambu-apps/admin-web/src/pages/system/role/role-service.ts`、`kuzhambu-apps/admin-web/src/pages/system/menu/menu-service.ts`、`kuzhambu-apps/admin-web/src/pages/system/department/department-service.ts`、`kuzhambu-apps/admin-web/src/pages/system/dictionary/dictionary-service.ts`
    - 处理动作：将用户、角色、菜单、部门和字典写操作请求字段固定为后端 request 类字段
    - 验收点：Playwright 断言 System 页面表单提交的请求体字段与后端 request 类一致
    - 重要度：8/10

- [ ] `System 登录与当前用户接口`：固定登录到当前用户闭环
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-SYSTEM-STORAGE-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/auth/controller/AuthSessionController.java`、`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/current/controller/CurrentUserController.java`、`kuzhambu-apps/admin-web/src/pages/auth/login/login-page.tsx`、`kuzhambu-apps/admin-web/src/pages/auth/login/auth-service.ts`、`kuzhambu-apps/admin-web/src/service/current-user-service.ts`
    - 处理动作：固定登录、刷新、登出、当前用户、菜单和权限字段契约
    - 验收点：接口测试覆盖登录到当前用户读取，Playwright 验证登录、首页和刷新后登录态恢复
    - 重要度：10/10

- [ ] `System 管理接口`：固定用户角色菜单后台管理闭环
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-SYSTEM-STORAGE-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/user/controller/UserController.java`、`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/role/controller/RoleController.java`、`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/menu/controller/MenuController.java`、`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/admin/SystemAdminControllerTest.java`
    - 处理动作：固定用户、角色和菜单管理接口路径并补充 controller 测试
    - 验收点：前端 service 可直接调用用户、角色和菜单接口，Playwright 验证列表请求和表单打开路径可用
    - 重要度：9/10

- [ ] `System 日志接口`：固定审计和系统日志查询闭环
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-SYSTEM-STORAGE-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/controller/AuditLogController.java`、`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/log/controller/SystemLogController.java`、`kuzhambu-apps/admin-web/src/pages/audit/audit-log/audit-log-service.ts`、`kuzhambu-apps/admin-web/src/pages/system/system-log/system-log-service.ts`
    - 处理动作：固定审计日志和系统日志分页查询请求字段与响应转换
    - 验收点：接口测试验证分页字段，Playwright 验证筛选提交后表格更新
    - 重要度：8/10

- [ ] `Storage 删除接口`：补齐 Storage 对象删除接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-SYSTEM-STORAGE-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/StorageObjectController.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/request/StorageDeleteRequest.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/assembler/StorageInterfaceAssembler.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/StorageApplicationService.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/StorageApplicationServiceImpl.java`
    - 处理动作：新增批量删除请求和 `/api/storage/object/delete` 后端处理
    - 验收点：接口测试覆盖成功删除、不存在对象和空 ID 集合
    - 重要度：10/10

- [ ] `Storage 上传接口`：补齐后台普通上传接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-SYSTEM-STORAGE-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/StorageObjectController.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/response/StorageObjectResponse.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/assembler/StorageInterfaceAssembler.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/helper/StorageUploadStreamHelper.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/StorageApplicationServiceImpl.java`
    - 处理动作：新增 `/api/storage/object/upload` multipart 上传接口
    - 验收点：接口测试验证成功上传、空文件、不允许类型和响应核心字段
    - 重要度：9/10

- [ ] `Storage 读取接口`：补齐文件内容读取接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-SYSTEM-STORAGE-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/StorageObjectController.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/store/StoredObjectStore.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/store/ObjectStorageStoredObjectStore.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/StorageApplicationServiceImpl.java`
    - 处理动作：新增 `/api/storage/object/{id}/content` 文件内容读取接口
    - 验收点：接口测试验证正常读取、删除态读取失败、`Content-Type` 和文件名响应头
    - 重要度：9/10

- [ ] `admin-web Storage 上传页面`：补齐 Storage 页面上传入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-SYSTEM-STORAGE-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-page.tsx`、`kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-service.ts`、`kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-types.ts`、`kuzhambu-apps/admin-web/src/app.test.tsx`、`kuzhambu-apps/admin-web/e2e/storage/storage-object.spec.ts`
    - 处理动作：在 Storage 页面新增普通文件上传入口并通过页面 service 调用上传接口
    - 验收点：Playwright 验证上传后表格出现新文件，删除后该文件从表格消失
    - 重要度：8/10

- [ ] `admin starter 与菜单种子数据`：固定后台启动模块依赖和菜单种子数据
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-SYSTEM-STORAGE-LOOP.md`
    - 范围对象：`kuzhambu-servers/starter/kuzhambu-admin-starter/pom.xml`、`kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/resources/application.yml`、`kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/resources/db/data/system.sql`、`kuzhambu-apps/admin-web/src/router/index.tsx`
    - 处理动作：固定 admin starter 依赖、API 前缀和后台菜单路径
    - 验收点：新库初始化后 admin 用户菜单全部落到前端路由，starter 能扫描 System 和 Storage 后台接口
    - 重要度：8/10

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
