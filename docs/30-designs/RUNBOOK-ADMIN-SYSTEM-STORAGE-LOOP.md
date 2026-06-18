# Runbook: Admin System Storage Loop

## Purpose

本 RUNBOOK 用于 `feat/admin-system-storage-alignment` 分支执行。目标是让 Admin Web 与当前 Java 后端接口对齐，把 System 域收口成第一个后台完整闭环，并补齐 Storage 最小可用闭环。

任务按功能和业务域拆分。每个任务只触达 2-5 个核心文件；超过 5 个文件时必须拆成下一个任务。

## Scope

覆盖：

- `kuzhambu-apps/admin-web`
- `kuzhambu-servers/biz/system`
- `kuzhambu-servers/biz/storage`
- `kuzhambu-servers/starter/kuzhambu-admin-starter`

不覆盖：

- `portal-web`
- Python workers
- Classics、AI、Knowledge、Discovery、Operations 业务页面实现
- 大文件分片上传完整闭环

## Execution Rules

- 每完成 1-3 个任务提交一次，提交信息使用 `Type(scope): 中文说明`。
- 每个任务完成后运行该任务列出的验证命令。
- 涉及后端接口的任务必须提供接口闭环验证：至少覆盖成功请求、关键失败请求和响应字段。
- 涉及 Admin Web 页面的任务必须提供页面闭环验证：使用 Playwright 打开页面、触发主操作并断言页面反馈。
- Playwright 验证应优先放入 `kuzhambu-apps/admin-web/e2e/`；只做临时人工验收时，需要在 PR 描述记录访问路径、操作步骤和截图。
- 修改接口路径、请求字段或响应字段时，同步修改前端 service、后端 request/response 和测试。
- 不新增临时 TODO；不能完成的内容写入本 RUNBOOK 的 `Deferred`。
- 本 RUNBOOK 在任务关闭后删除。

## Admin Web

### A1. 固定后台菜单到已实现页面

核心文件：

- `kuzhambu-apps/admin-web/src/router/index.tsx`
- `kuzhambu-apps/admin-web/src/layouts/admin-layout.tsx`
- `kuzhambu-apps/admin-web/src/app.test.tsx`

步骤：

- 为后端当前会返回、但前端尚未实现页面的菜单路径挂载 `PlaceholderPage`。
- 在布局菜单图标映射中保留 System、Storage、Audit、Dashboard 和占位菜单所需图标。
- 在 `app.test.tsx` 增加未实现业务菜单点击后展示占位页面的用例。
- 在 `e2e/layout/` 增加菜单导航 Playwright 用例，覆盖真实页面和占位页面。
- 运行 `npm run lint --workspace admin-web`。
- 运行 `npm test --workspace admin-web`。
- 运行 `npm run e2e --workspace admin-web`。

验收：

- 登录后菜单中出现的路径不会进入空白页面。
- System、Storage、Audit、Dashboard 真实页面仍按原路由进入。
- Playwright 验证菜单点击后 URL 和页面标题一致。

### A2. 对齐 Storage 对象列表和删除接口契约

核心文件：

- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-service.ts`
- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-types.ts`
- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-page.tsx`
- `kuzhambu-apps/admin-web/src/app.test.tsx`

步骤：

- 将 Storage 对象列表请求字段固定为后端 `StoragePageRequest` 已接收字段。
- 将 Storage 对象列表响应字段固定为后端 `StorageObjectResponse` 已返回字段。
- 将删除动作固定为批量提交对象 ID，并在页面删除成功后刷新列表。
- 在 `app.test.tsx` 覆盖 Storage 列表加载、删除确认和删除后刷新。
- 在 `e2e/storage/storage-object.spec.ts` 覆盖列表加载、删除确认、删除成功提示和列表刷新。
- 运行 `npm run lint --workspace admin-web`。
- 运行 `npm test --workspace admin-web`。
- 运行 `npm run e2e --workspace admin-web`。

验收：

- Storage 页面只调用后端已存在或本 RUNBOOK 后续任务补齐的接口。
- 删除按钮不会因为字段名不一致导致请求体错误。
- Playwright 验证删除动作会触发 `/storage/object/delete`，成功后列表重新请求 `/storage/object/page`。

### A3. 对齐 System 页面写操作的请求字段

核心文件：

- `kuzhambu-apps/admin-web/src/pages/system/user/user-service.ts`
- `kuzhambu-apps/admin-web/src/pages/system/role/role-service.ts`
- `kuzhambu-apps/admin-web/src/pages/system/menu/menu-service.ts`
- `kuzhambu-apps/admin-web/src/pages/system/department/department-service.ts`
- `kuzhambu-apps/admin-web/src/pages/system/dictionary/dictionary-service.ts`

步骤：

- 将用户创建、更新、启禁用、删除请求字段固定为后端 request 类字段。
- 将角色创建、更新、启禁用、排序、删除请求字段固定为后端 request 类字段。
- 将菜单创建、更新、显示隐藏、移动、删除请求字段固定为后端 request 类字段。
- 将部门和字典写操作请求字段固定为后端 request 类字段。
- 在页面测试或 Playwright 中覆盖用户、角色、菜单至少一个新增和编辑动作。
- 运行 `npm run lint --workspace admin-web`。
- 运行 `npm test --workspace admin-web`。
- 运行 `npm run e2e --workspace admin-web`。

验收：

- System 页面 service 不再向后端发送后端 request 类不存在的字段。
- System 页面写操作返回值统一按 `boolean` 或后端明确响应处理。
- Playwright 验证 System 页面表单提交的请求体字段与后端 request 类一致。

## System Domain

### S1. 固定登录到当前用户闭环

核心文件：

- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/auth/controller/AuthSessionController.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/current/controller/CurrentUserController.java`
- `kuzhambu-apps/admin-web/src/pages/auth/login/login-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/auth/login/auth-service.ts`
- `kuzhambu-apps/admin-web/src/service/current-user-service.ts`

步骤：

- 固定登录前会话、验证码刷新、账号密码登录、token 刷新和登出路径。
- 固定登录响应中 `accessToken`、`refreshToken`、`expiresAt`、`principal` 字段。
- 固定当前用户信息、菜单树和权限编码的前端读取字段。
- 增加登录后拉取当前用户、菜单和权限的前端测试覆盖。
- 增加登录接口 controller 测试，覆盖登录前会话、验证码刷新、登录、token 刷新和登出。
- 增加登录页面 Playwright 用例，覆盖登录成功进入后台首页。
- 运行 `npm run lint --workspace admin-web`。
- 运行 `npm test --workspace admin-web`。
- 运行 `npm run e2e --workspace admin-web`。
- 运行 `cd kuzhambu-servers && mvn -pl biz/system/kuzhambu-system-interface -am test`。

验收：

- 使用后端登录接口返回值能进入后台布局。
- 刷新页面后当前用户、菜单和权限能重新恢复。
- 接口测试验证登录到当前用户信息读取的响应字段完整。
- Playwright 验证登录页、后台首页、刷新后登录态恢复三段页面路径。

### S2. 固定用户、角色、菜单的后台管理闭环

核心文件：

- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/user/controller/UserController.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/role/controller/RoleController.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/menu/controller/MenuController.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/admin/SystemAdminControllerTest.java`

步骤：

- 固定用户分页、创建、更新、启禁用、删除、部门树和角色列表接口的 HTTP 路径。
- 固定角色列表、菜单树、选项、创建、更新、启禁用、排序和删除接口的 HTTP 路径。
- 固定菜单列表、创建、更新、显示隐藏、移动和删除接口的 HTTP 路径。
- 增加 System 管理接口 controller 测试，覆盖请求路径和请求体绑定。
- 增加 System 管理页面 Playwright 用例，覆盖用户、角色、菜单列表进入和主按钮打开表单。
- 使用 Playwright route 断言页面请求命中对应后端接口路径。
- 运行 `cd kuzhambu-servers && mvn -pl biz/system/kuzhambu-system-interface -am test`。
- 运行 `npm run e2e --workspace admin-web`。

验收：

- 用户、角色、菜单三个页面的服务端接口均能被前端 service 直接调用。
- controller 测试覆盖核心路径，避免后续路径漂移。
- Playwright 验证三个页面的列表请求、主按钮和表单打开路径可用。

### S3. 固定审计和系统日志查询闭环

核心文件：

- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/controller/AuditLogController.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/log/controller/SystemLogController.java`
- `kuzhambu-apps/admin-web/src/pages/audit/audit-log/audit-log-service.ts`
- `kuzhambu-apps/admin-web/src/pages/system/system-log/system-log-service.ts`

步骤：

- 固定审计日志分页、详情和筛选选项接口的请求字段。
- 固定系统日志分页接口的请求字段。
- 将前端日期范围字段转换为后端已接收的开始时间和结束时间字段。
- 将分页响应统一转换为前端 `Page<T>`。
- 增加审计日志和系统日志页面 Playwright 用例，覆盖筛选、查询和分页。
- 运行 `npm run lint --workspace admin-web`。
- 运行 `npm test --workspace admin-web`。
- 运行 `npm run e2e --workspace admin-web`。
- 运行 `cd kuzhambu-servers && mvn -pl biz/system/kuzhambu-system-interface -am test`。

验收：

- 审计日志页面可以按筛选条件分页查询并查看详情。
- 系统日志页面可以按筛选条件分页查询。
- 接口测试验证分页请求字段和响应字段。
- Playwright 验证筛选条件提交后表格更新。

## Storage Domain

### T1. 补齐 Storage 对象删除接口

核心文件：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/StorageObjectController.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/request/StorageDeleteRequest.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/assembler/StorageInterfaceAssembler.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/StorageApplicationService.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/StorageApplicationServiceImpl.java`

步骤：

- 新增 `StorageDeleteRequest`，接收待删除文件对象 ID 集合。
- 在 `StorageObjectController` 新增 `POST /api/storage/object/delete`。
- 在 `StorageInterfaceAssembler` 将请求转换为 application command。
- 在 `StorageApplicationService` 增加批量删除方法。
- 在 `StorageApplicationServiceImpl` 将文件对象状态切换为删除态，并拒绝不存在对象。
- 增加 Storage 删除接口 controller 测试，覆盖成功删除、不存在对象和空 ID 集合。
- 运行 `cd kuzhambu-servers && mvn -pl biz/storage/kuzhambu-storage-interface -am test`。

验收：

- Admin Web Storage 页面删除动作能命中后端接口。
- 删除后的对象不会继续以正常状态出现在对象列表中。
- 接口测试验证删除接口请求体绑定、状态变更和失败响应。

### T2. 补齐后台普通上传接口

核心文件：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/StorageObjectController.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/response/StorageObjectResponse.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/assembler/StorageInterfaceAssembler.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/helper/StorageUploadStreamHelper.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/StorageApplicationServiceImpl.java`

步骤：

- 在 `StorageObjectController` 新增 `POST /api/storage/object/upload`，接收 `multipart/form-data`。
- 使用 `StorageUploadStreamHelper` 保存上传流并创建文件对象。
- 在响应中返回文件对象 ID、原始文件名、内容类型、文件大小和读取地址。
- 对空文件、超限文件和不允许类型返回明确业务错误。
- 增加上传接口 controller 测试，覆盖成功上传、空文件和不允许类型。
- 运行 `cd kuzhambu-servers && mvn -pl biz/storage/kuzhambu-storage-interface -am test`。

验收：

- 管理员可以上传普通文件并获得稳定文件对象 ID。
- 上传响应可直接用于 Storage 对象列表展示。
- 接口测试验证上传响应包含文件对象 ID、原始文件名、内容类型、文件大小和读取地址。

### T3. 补齐文件内容读取接口

核心文件：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/StorageObjectController.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/store/StoredObjectStore.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/store/ObjectStorageStoredObjectStore.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/StorageApplicationServiceImpl.java`

步骤：

- 在 `StorageObjectController` 新增 `GET /api/storage/object/{id}/content`。
- 从 application 层按文件对象 ID 读取对象元数据并拒绝删除态对象。
- 通过 `StoredObjectStore` 读取文件内容流。
- 在 HTTP 响应中设置 `Content-Type` 和下载文件名。
- 增加读取接口 controller 测试，覆盖正常读取和删除态读取失败。
- 运行 `cd kuzhambu-servers && mvn -pl biz/storage/kuzhambu-storage-interface -am test`。

验收：

- 上传成功后的读取地址可以返回文件内容。
- 删除态文件对象读取时返回业务错误或 404。
- 接口测试验证读取响应的 `Content-Type` 和文件名响应头。

### T4. 补齐 Storage 页面上传入口

核心文件：

- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-service.ts`
- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-types.ts`
- `kuzhambu-apps/admin-web/src/app.test.tsx`

步骤：

- 在 Storage 页面顶部增加普通文件上传入口。
- 在 service 中新增 `uploadStorageObject`，使用 `postFormData` 调用 `/storage/object/upload`。
- 上传成功后刷新对象列表。
- 增加上传成功刷新列表的前端测试。
- 在 `e2e/storage/storage-object.spec.ts` 覆盖上传、读取链接出现、删除三个页面动作。
- 运行 `npm run lint --workspace admin-web`。
- 运行 `npm test --workspace admin-web`。
- 运行 `npm run e2e --workspace admin-web`。

验收：

- Storage 页面可完成上传、列表刷新、删除三个最小动作。
- 上传入口不绕过页面 service。
- Playwright 验证上传成功后表格出现新文件，删除成功后该文件从表格消失。

## Starter And Seed Data

### R1. 固定后台启动模块依赖和菜单种子数据

核心文件：

- `kuzhambu-servers/starter/kuzhambu-admin-starter/pom.xml`
- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/resources/application.yml`
- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/resources/db/data/system.sql`
- `kuzhambu-apps/admin-web/src/router/index.tsx`

步骤：

- 确认 admin starter 依赖包含 System 和 Storage 的 interface、application、infra 模块。
- 确认后台 API 前缀与 Admin Web `VITE_ADMIN_API_BASE_URL` 的默认值匹配。
- 将 `system.sql` 中后台菜单路径固定到前端真实页面或占位页面。
- 保持已删除的 `/open/client/*` 和 `/submission/*` 不再出现在种子菜单中。
- 运行 `cd kuzhambu-servers && mvn -pl starter/kuzhambu-admin-starter -am test`。
- 运行 `npm test --workspace admin-web`。

验收：

- 新库初始化后，admin 用户菜单可以全部落到前端路由。
- admin starter 启动时能扫描 System 和 Storage 后台接口。

## Cross-Domain Verification

### V1. 执行接口闭环验证

核心文件：

- `dev.env`
- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/resources/application.yml`
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/admin/SystemAdminControllerTest.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/test/java/com/thundax/kuzhambu/storage/interfaces/admin/StorageObjectControllerTest.java`

步骤：

- 加载 `dev.env` 并启动 `kuzhambu-admin-starter`。
- 调用登录前会话、验证码刷新、登录、当前用户信息、菜单、权限和登出接口。
- 调用用户、角色、菜单、字典、审计日志和系统日志的列表接口。
- 调用 Storage 上传、列表、读取和删除接口。
- 对每个接口记录 HTTP 状态、关键请求字段和关键响应字段。
- 运行 `cd kuzhambu-servers && mvn -pl starter/kuzhambu-admin-starter -am test`。

验收：

- 接口闭环覆盖 System 登录态、System 管理查询、Storage 上传、Storage 读取和 Storage 删除。
- 接口闭环包含成功路径和至少一个失败路径。
- 接口闭环不依赖 Admin Web 页面才能成立。

### V2. 执行 Admin Web 页面闭环验证

核心文件：

- `kuzhambu-apps/admin-web/package.json`
- `kuzhambu-apps/admin-web/e2e/auth/login.spec.ts`
- `kuzhambu-apps/admin-web/e2e/system/system-pages.spec.ts`
- `kuzhambu-apps/admin-web/e2e/storage/storage-object.spec.ts`

步骤：

- 加载 `dev.env` 并启动 `kuzhambu-admin-starter`。
- 启动 `admin-web` 本地开发服务。
- 使用 Playwright 登录后台。
- 使用 Playwright 打开 System 用户、角色、菜单、字典、审计日志和系统日志页面。
- 使用 Playwright 在 Storage 对象页面执行上传、列表刷新、读取链接出现和删除。
- 使用 Playwright 断言页面主标题、表格数据、成功提示和接口请求路径。
- 将 Playwright 截图或 trace 作为本地验证材料，不提交到仓库。
- 运行 `npm run e2e --workspace admin-web`。

验收：

- Admin Web 页面闭环覆盖登录、菜单导航、System 页面访问、Storage 上传和 Storage 删除。
- 页面闭环通过 Playwright 断言完成，不只依赖人工点击。
- PR 描述记录 Playwright 命令、通过结果和关键截图位置。

## Deferred

- Storage 分片上传完整闭环。
- Storage 引用建立和清理在业务域中的接入。
- Portal 专用上传入口。
- Classics、AI、Knowledge、Discovery、Operations 的真实后台页面。
