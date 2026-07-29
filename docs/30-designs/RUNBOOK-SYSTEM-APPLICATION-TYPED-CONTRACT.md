# System Application 强类型契约改造 RUNBOOK

## Purpose

对 `com.thundax.kuzhambu.system.application.**` 下应用层公开契约做一次可闭环的强类型化改造：

- `*Command` / `*Query` / `*Result` 字段优先使用领域强类型值对象、枚举或明确的 application 契约对象。
- `*ApplicationService` 接口名业务化；有输入条件的方法参数统一收敛为 `*Command` / `*Query` / `PageQuery`，无输入条件的读查询允许无参方法。
- 应用层公开方法允许 `get` / `list` / `page` 等读语义，不引入 `save`。
- `ApplicationService` 可以返回领域实体；本次不把返回领域实体视为契约问题。已有 `*Result` 仍需完成字段强类型化。

当前分支：`task/system-application-typed-contract-runbook`。

## Scope

纳入本次闭环的模块：

- `kuzhambu-servers/biz/system/kuzhambu-system-application`
- `kuzhambu-servers/biz/system/kuzhambu-system-interface`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/test`

本次盘点到的 application 契约对象：

- `Command`：32 个。
- `Query`：13 个。
- `Result`：4 个。
- `ApplicationService`：14 个接口文件。

## Non-goals

- 不调整数据库表、Mapper XML、Repository 端口的持久化签名。
- 不重命名 HTTP API URL、Request、Response，除非 application 契约变更需要同步 assembler。
- 不把 application 内部 helper、runtime、audit aspect 全量重构为新分层。
- 不新增 facade 模块。
- 不处理其他业务域的 application 契约。

## Current Inventory

### ApplicationService 接口

| 子域 | 当前接口 | 当前主要问题 | 建议业务化接口名 |
| --- | --- | --- | --- |
| audit | `AuditApplicationService` | `getLog(AuditLogId)` 不是 Query 参数；`list(AuditMetaQuery)` 实际按对象列审计日志，Query 名称不准确 | `AuditLogApplicationService` 或 `AuditTrailApplicationService` |
| auth | `AdminTokenApplicationService` | 多个方法使用 `String token/clientId/ip/userAgent/reason` 与多参数；已有 Result 内 token 字段仍是 `String` | `AdminSessionTokenApplicationService` |
| auth | `PermissionApplicationService` | 参数全是 `String`，返回 `Set<String>`；`permission` 应用 `PermissionCode` | `PrincipalPermissionApplicationService` |
| auth | `PreAuthSessionApplicationService` | `countActiveSessions()` 可保留无参读查询；`get(PreAuthSessionId)` 与 `getIdBy*` 非 Query 参数 | `PreAuthSessionApplicationService` 可保留，方法契约需改 |
| auth | `PrincipalAuthApplicationService` | 接口名偏技术，但可接受为认证用例；方法参数已是 Command | `PrincipalAuthenticationApplicationService` |
| auth | `PrincipalCredentialApplicationService` | `PrincipalCredentialCommand` 直接包装领域实体，写命令语义不够明确 | `PrincipalCredentialApplicationService` 可保留，契约需拆细 |
| auth | `PrincipalIdentityApplicationService` | `PrincipalIdentityCommand` 直接包装领域实体，写命令语义不够明确 | `PrincipalIdentityApplicationService` 可保留，契约需拆细 |
| core | `CurrentUserApplicationService` | `getAvatar(UserId)` / `existsAvatar(UserId)` / `getAvatarInputStream(UserId)` 非 Query 参数 | `CurrentUserProfileApplicationService` |
| core | `DepartmentApplicationService` | `get(DepartmentId)` / `remove(DepartmentId)` 非 Query/Command 参数 | `DepartmentManagementApplicationService` |
| core | `DictApplicationService` | `get(DictId)` / `remove(DictId)` 非 Query/Command 参数；`listTypes/listLabels` 返回 `List<String>` 已确认为可保留 | `DictionaryManagementApplicationService` |
| core | `LogApplicationService` | `get(LogId)` 非 Query 参数；`deleteByCondition` 命名不是业务化写命令；`CreateLogCommand.userId` 是 `String` | `SystemLogApplicationService` |
| core | `MenuApplicationService` | `get(MenuId)` / `remove(MenuId)` 非 Query/Command 参数 | `MenuManagementApplicationService` |
| core | `RoleApplicationService` | `get(RoleId)` / `remove(RoleId)` 非 Query/Command 参数 | `RoleManagementApplicationService` |
| core | `UserApplicationService` | `get(UserId)` / `remove(UserId)` 非 Query/Command 参数 | `UserManagementApplicationService` |

未发现 application 或 interface 层公开调用 `save(...)`。

### 现有可复用强类型值对象

- audit：`AuditLogId`、`AuditMetaId`、`AuditObjectRef`、`AuditOperatorRef`。
- auth：`PreAuthSessionId`、`PreAuthSessionToken`、`PrincipalAccessTokenCode`、`PrincipalRefreshTokenCode`、`PrincipalClientId`、`PrincipalKey`、`PrincipalIdentityId`、`PrincipalCredentialId`。
- core：`UserId`、`RoleId`、`MenuId`、`DepartmentId`、`DictId`、`LogId`、`AccessRank`、`PermissionCode`。

### 属性级问题清单

必须改为已有强类型值对象或新增值对象的属性：

| 文件 | 当前属性 | 目标 |
| --- | --- | --- |
| `CreateAuditLogCommand` | `String objectType`、`String objectId` | `AuditObjectRef objectRef` |
| `CreateAuditLogCommand` | `AuditOperatorType operatorType`、`String operatorId`、`String operatorName` | `AuditOperatorRef operatorRef` |
| `AuditLogQuery` | `String objectType`、`String objectId` | `AuditObjectRef objectRef` |
| `AuditLogQuery` | `AuditOperatorType operatorType`、`String operatorId` | `AuditOperatorRef operatorRef` 或显式 operator 查询对象 |
| `AuditMetaQuery` | `String objectType`、`String objectId` | `AuditObjectRef objectRef` |
| `AdminAccessTokenResult` | `String token`、`String refreshToken` | `PrincipalAccessTokenCode token`、`PrincipalRefreshTokenCode refreshToken` |
| `AdminTokenQueryResult` | `String token` | `PrincipalAccessTokenCode token` |
| `AdminTokenRefreshResult` | `String refreshToken` | `PrincipalRefreshTokenCode refreshToken` |
| `CreateLogCommand` | `String userId` | `UserId userId` |
| `PermissionApplicationService` | `String token`、`String userId`、`String permission` | `PrincipalAccessTokenCode token`、`UserId userId`、`PermissionCode permission`，并收敛进 Command/Query |
| `AdminTokenApplicationService` | `String token`、`String clientId`、`String refreshToken` | `PrincipalAccessTokenCode token`、`PrincipalClientId clientId`、`PrincipalRefreshTokenCode refreshToken`，并收敛进 Command/Query |

已确认保留或暂缓值对象化的字段：

| 文件 | 当前属性 | 处理方向 |
| --- | --- | --- |
| `AuthenticateIdentityCommand`、`AuthenticatePasswordCommand`、`PrincipalIdentityQuery` | `String identityValue` | 可新增 `PrincipalIdentityValue`；如保留 `String`，需说明其是用户输入原文 |
| `AuthenticatePasswordCommand`、`ChangeCurrentUserPasswordCommand` | `String plainPassword`、`String oldPassword`、`String password` | 可保留敏感输入原文，不进入 Result、不落库明文 |
| `UpsertPreAuthSessionValueCommand`、`PreAuthSessionValueQuery`、`PreAuthSessionValueValidateQuery` | `String name/value/bindName/bindValue` | 可新增 `PreAuthSessionValueName`；验证码值等输入原文可保留 `String` |
| `CreatePreAuthSessionCommand`、`RefreshPreAuthSessionCommand` | `int expiredSeconds`、`int refreshTokenGraceSeconds` | 可保留时长数值；如统一时长语义，新增 application 层 `Duration` 字段 |
| `LogQuery`、`AuditLogQuery` | `Date beginDate/endDate` | 可保留时间范围；如统一时间语义，新增 `TimeRangeQuery` 或改 `Instant` |
| `UserAvatarResult` | `Long storageObjectId`、`String ownerId/ownerType/objectStatus/referenceStatus` | 依赖 storage facade 契约；本次不跨域发明 storage 值对象 |
| core 创建/更新命令 | `String name/email/mobile/tel/remarks/url/title/...` | 属于文本属性时可保留；若字段表达 code/key/token/id，必须强类型化 |

## Contract Object Worklist

### audit

文件：

- `application/audit/command/CreateAuditLogCommand.java`
- `application/audit/query/AuditLogQuery.java`
- `application/audit/query/AuditMetaQuery.java`
- `application/audit/service/AuditApplicationService.java`
- `application/audit/service/impl/AuditApplicationServiceImpl.java`

改造：

- `CreateAuditLogCommand.objectType/objectId` 合并为 `AuditObjectRef objectRef`。
- `CreateAuditLogCommand.operatorType/operatorId/operatorName` 合并为 `AuditOperatorRef operatorRef`，保留 `AuditOperatorType` 只在值对象内表达。
- `AuditLogQuery.objectType/objectId`、`AuditMetaQuery.objectType/objectId` 合并为 `AuditObjectRef objectRef`。
- 新增 `GetAuditLogQuery`，字段 `AuditLogId id`，替换 `getLog(AuditLogId id)`。
- 不要求新增 `AuditLogResult`、`AuditMetaResult`；若保留或新增 Result，仅处理 Result 内字段强类型化。
- `AuditApplicationService` 重命名后同步 `AuditLogAspect`、`AuditController`、`AuditInterfaceAssembler`。

### auth token / permission / pre-auth

文件：

- `application/auth/service/AdminTokenApplicationService.java`
- `application/auth/service/PermissionApplicationService.java`
- `application/auth/service/PreAuthSessionApplicationService.java`
- `application/auth/service/impl/AdminTokenApplicationServiceImpl.java`
- `application/auth/service/impl/PermissionApplicationServiceImpl.java`
- `application/auth/service/impl/PreAuthSessionApplicationServiceImpl.java`
- `application/auth/result/AdminAccessTokenResult.java`
- `application/auth/result/AdminTokenQueryResult.java`
- `application/auth/result/AdminTokenRefreshResult.java`
- `application/auth/command/CreatePreAuthSessionCommand.java`
- `application/auth/command/RefreshPreAuthSessionCommand.java`
- `application/auth/command/ReleasePreAuthSessionCommand.java`
- `application/auth/command/UpsertPreAuthSessionValueCommand.java`
- `application/auth/query/PreAuthSessionValueQuery.java`
- `application/auth/query/PreAuthSessionValueValidateQuery.java`

新增或调整契约：

- 新增 `CreateAdminAccessTokenCommand`：承载 `UserId userId`、`String loginName`、`String ip`、`String userAgent`、`PrincipalAuthenticationMethod authenticationMethod`、`PrincipalIdentityType identityType`。
- 新增 `AdminAccessTokenQuery`：承载 `PrincipalAccessTokenCode token`。
- 新增 `RefreshAdminAccessTokenCommand`：承载 `PrincipalClientId clientId`、`PrincipalRefreshTokenCode refreshToken`、`String ip`、`String userAgent`。
- 新增 `DeleteAdminAccessTokenCommand`：承载 `PrincipalAccessTokenCode token`、`String ip`、`String userAgent`。
- 新增 `InvalidateAdminSessionCommand`：承载 `PrincipalAccessTokenCode token` 或 `UserId userId`、`String reason`。
- 新增 `RecordPrincipalLoginFailureCommand`：合并两个 `recordLoginFailed(...)` 重载。
- 新增 `PermissionQuery`：承载 `PrincipalAccessTokenCode token`、可选 `PermissionCode permission`。
- 新增 `CreatePermissionsCommand`：承载 `PrincipalAccessTokenCode token`、`UserId userId`。
- `PermissionApplicationService` 返回 `Set<PermissionCode>`，禁止继续暴露 `Set<String>`。
- `countActiveSessions()` 是无输入条件读查询，可保留无参方法；如果后续需要过滤条件，再新增 `ActivePreAuthSessionQuery`。
- 新增 `PreAuthSessionQuery`：承载 `PreAuthSessionId id`、`PreAuthSessionToken token`、`PreAuthSessionToken refreshToken`，替换 `get(...)` / `getIdByToken(...)` / `getIdByRefreshToken(...)`。
- 不要求新增 `PreAuthSessionResult`；`PreAuthSession` 可继续作为 application service 返回值。
- `AdminAccessTokenResult.token/refreshToken` 改为 `PrincipalAccessTokenCode` / `PrincipalRefreshTokenCode`，或新增只面向 interface assembler 的 typed getter。
- `AdminTokenQueryResult.token` 改为 `PrincipalAccessTokenCode`；领域实体字段可保留。
- `AdminTokenRefreshResult.refreshToken` 改为 `PrincipalRefreshTokenCode`。

### auth principal identity / credential / authentication

文件：

- `application/auth/command/AuthenticateIdentityCommand.java`
- `application/auth/command/AuthenticatePasswordCommand.java`
- `application/auth/command/PrincipalCredentialCommand.java`
- `application/auth/command/PrincipalIdentityCommand.java`
- `application/auth/query/PrincipalCredentialQuery.java`
- `application/auth/query/PrincipalIdentityQuery.java`
- `application/auth/service/PrincipalAuthApplicationService.java`
- `application/auth/service/PrincipalCredentialApplicationService.java`
- `application/auth/service/PrincipalIdentityApplicationService.java`
- `application/auth/service/impl/PrincipalAuthApplicationServiceImpl.java`
- `application/auth/service/impl/PrincipalCredentialApplicationServiceImpl.java`
- `application/auth/service/impl/PrincipalIdentityApplicationServiceImpl.java`

改造：

- `AuthenticateIdentityCommand.identityValue` 可保留为凭证输入原文；如要彻底强类型化，新增 `PrincipalIdentityValue` 值对象并在 domain codec 统一转换。
- `AuthenticatePasswordCommand.plainPassword` 可保留为敏感输入原文；不要落库或进入 Result。
- `PrincipalIdentityCommand` 不再直接持有 `PrincipalIdentity principalIdentity`，拆为 `CreatePrincipalIdentityCommand`、`ChangePrincipalIdentityCommand`、`ChangePrincipalIdentityStatusCommand`。
- `PrincipalCredentialCommand` 不再直接持有 `PrincipalCredential principalCredential`，拆为 `CreatePrincipalCredentialCommand`、`ChangePrincipalCredentialCommand`、`ChangePrincipalCredentialStatusCommand`、`ChangePrincipalCredentialVerifyStateCommand`、`RecordPrincipalCredentialFailureCommand`。
- 不要求新增 `PrincipalIdentityResult`、`PrincipalCredentialResult`；`PrincipalIdentity`、`PrincipalCredential` 可继续作为 application service 返回值。
- `PrincipalAuthApplicationService.authenticate*` 可继续返回 `PrincipalIdentity`。

### core management

文件：

- `application/core/service/UserApplicationService.java`
- `application/core/service/RoleApplicationService.java`
- `application/core/service/MenuApplicationService.java`
- `application/core/service/DepartmentApplicationService.java`
- `application/core/service/DictApplicationService.java`
- `application/core/service/LogApplicationService.java`
- `application/core/service/CurrentUserApplicationService.java`
- `application/core/service/impl/UserApplicationServiceImpl.java`
- `application/core/service/impl/RoleApplicationServiceImpl.java`
- `application/core/service/impl/MenuApplicationServiceImpl.java`
- `application/core/service/impl/DepartmentApplicationServiceImpl.java`
- `application/core/service/impl/DictApplicationServiceImpl.java`
- `application/core/service/impl/LogApplicationServiceImpl.java`
- `application/core/service/impl/CurrentUserApplicationServiceImpl.java`
- `application/core/command/CreateLogCommand.java`
- `application/core/command/RemoveUserCommand.java`
- `application/core/command/RemoveRoleCommand.java`
- `application/core/command/RemoveMenuCommand.java`
- `application/core/command/RemoveDepartmentCommand.java`
- `application/core/command/RemoveDictCommand.java`
- `application/core/command/DeleteLogCommand.java`
- `application/core/query/GetUserQuery.java`
- `application/core/query/GetRoleQuery.java`
- `application/core/query/GetMenuQuery.java`
- `application/core/query/GetDepartmentQuery.java`
- `application/core/query/GetDictQuery.java`
- `application/core/query/GetLogQuery.java`
- `application/core/query/CurrentUserAvatarQuery.java`
- `application/core/result/UserAvatarResult.java`

新增或调整契约：

- 新增 `GetUserQuery`、`RemoveUserCommand`；对 `Role/Menu/Department/Dict/Log` 同步新增 `Get*Query` 与 `Remove*Command`。
- 对 `Role/Menu/Department/Dict/Log` 同步新增 `Get*Query` 与 `Remove*Command`，替换 `get(*Id)` / `remove(*Id)` / `deleteByCondition(LogQuery)`。
- `CreateLogCommand.userId` 改为 `UserId userId`，移除 `LogApplicationServiceImpl.toLog` 中 `UserIdCodec.toDomain(command.getUserId())`。
- `UserAvatarResult.storageObjectId`、`ownerId`、`ownerType`、`objectStatus`、`referenceStatus` 按 storage facade 已有强类型能力评估；如果 storage facade 对外仍是 `Long/String`，本次只在 RUNBOOK 记录残余风险，不跨域新增 storage 值对象。
- `CurrentUserApplicationService.getAvatar(UserId)`、`existsAvatar(UserId)`、`getAvatarInputStream(UserId)` 改为 `CurrentUserAvatarQuery`。
- `DictApplicationService.listTypes/listLabels` 已确认保留 `List<String>`。

基础属性如 `name`、`email`、`mobile`、`remarks`、`url`、`title` 可暂时保留 `String`，但必须确认它们表达的是业务文本值，而不是 ID、token、code、状态或外部引用。

## Caller Worklist

优先同步以下调用方：

- `interfaces/admin/auth/controller/AuthController.java`
- `interfaces/admin/auth/controller/CaptchaController.java`
- `interfaces/admin/auth/service/impl/AdminAuthServiceImpl.java`
- `interfaces/admin/auth/service/impl/PermissionServiceImpl.java`
- `interfaces/admin/auth/security/CurrentUserResolver.java`
- `interfaces/admin/auth/security/filter/AccessTokenAuthenticationFilter.java`
- `interfaces/admin/auth/configure/SpringSecurityConfiguration.java`
- `interfaces/admin/core/controller/UserController.java`
- `interfaces/admin/core/controller/RoleController.java`
- `interfaces/admin/core/controller/MenuController.java`
- `interfaces/admin/core/controller/DepartmentController.java`
- `interfaces/admin/core/controller/DictController.java`
- `interfaces/admin/core/controller/LogController.java`
- `interfaces/admin/core/service/impl/SysLogMessageServiceImpl.java`
- `interfaces/admin/audit/controller/AuditController.java`
- `interfaces/admin/audit/assembler/AuditInterfaceAssembler.java`
- `application/audit/runtime/AuditLogAspect.java`
- `application/audit/runtime/sys/DepartmentAuditObjectLoader.java`
- `application/audit/runtime/sys/DictAuditObjectLoader.java`
- `application/audit/runtime/sys/MenuAuditObjectLoader.java`
- `application/audit/runtime/sys/RoleAuditObjectLoader.java`
- `application/audit/runtime/sys/UserAuditObjectLoader.java`
- `application/core/service/impl/CurrentUserApplicationServiceImpl.java`
- `application/auth/service/impl/PrincipalAuthApplicationServiceImpl.java`
- `application/auth/service/impl/AdminTokenApplicationServiceImpl.java`
- application 单测：`CurrentUserApplicationServiceImplTest`、`PermissionApplicationServiceImplTest`、`PrincipalAuthApplicationServiceImplTest`、`AuditApplicationServiceImplTest`、`MenuApplicationServiceImplTest`。

## Frontend Impact

本次目标是 Java application 契约强类型化，默认不变更 HTTP URL、HTTP Request 字段名、HTTP Response JSON 字段名和页面交互。只有当 interface assembler 因 application Result 字段类型变化需要调整序列化时，才同步 admin-web contract test 或页面验证。

需要关注的前端文件：

- `kuzhambu-apps/admin-web/src/auth/auth-service.ts`
- `kuzhambu-apps/admin-web/src/auth/auth-session-service.ts`
- `kuzhambu-apps/admin-web/src/auth/auth-types.ts`
- `kuzhambu-apps/admin-web/src/auth/permission-storage.ts`
- `kuzhambu-apps/admin-web/src/service/current-user-service.ts`
- `kuzhambu-apps/admin-web/src/service/current-user-types.ts`
- `kuzhambu-apps/admin-web/src/pages/auth/login/login-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/system/user/user-service.ts`
- `kuzhambu-apps/admin-web/src/pages/system/user/user-types.ts`
- `kuzhambu-apps/admin-web/src/pages/system/user/user-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/system/role/role-service.ts`
- `kuzhambu-apps/admin-web/src/pages/system/role/role-types.ts`
- `kuzhambu-apps/admin-web/src/pages/system/role/role-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/system/menu/menu-service.ts`
- `kuzhambu-apps/admin-web/src/pages/system/menu/menu-types.ts`
- `kuzhambu-apps/admin-web/src/pages/system/menu/menu-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/system/department/department-service.ts`
- `kuzhambu-apps/admin-web/src/pages/system/department/department-types.ts`
- `kuzhambu-apps/admin-web/src/pages/system/department/department-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/system/dictionary/dictionary-service.ts`
- `kuzhambu-apps/admin-web/src/pages/system/dictionary/dictionary-types.ts`
- `kuzhambu-apps/admin-web/src/pages/system/dictionary/dictionary-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/system/system-log/system-log-service.ts`
- `kuzhambu-apps/admin-web/src/pages/system/system-log/system-log-types.ts`
- `kuzhambu-apps/admin-web/src/pages/system/system-log/system-log-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/audit/audit-log/audit-log-service.ts`
- `kuzhambu-apps/admin-web/src/pages/audit/audit-log/audit-log-types.ts`
- `kuzhambu-apps/admin-web/src/pages/audit/audit-log/audit-log-page.tsx`

前端控件和操作验收：

- 登录页：用户名输入框、密码输入框、验证码输入框、验证码图片刷新操作、登录按钮。
- 顶部会话：当前用户菜单、退出登录按钮、token 刷新后的继续访问。
- 当前用户资料：头像上传控件、头像删除按钮、资料表单保存按钮、修改密码表单保存按钮。
- 用户管理页：部门树节点点击、登录名/姓名/状态筛选控件、查询按钮、重置按钮、新增按钮、编辑按钮、删除按钮、启用/停用按钮、角色多选控件、保存按钮。
- 角色管理页：角色列表查询控件、新增按钮、编辑按钮、删除按钮、启用/停用按钮、菜单树勾选控件、分配用户控件、排序拖拽或排序按钮。
- 菜单管理页：菜单树或表格节点展开、查询按钮、新增按钮、编辑按钮、删除按钮、显示/隐藏控件、移动菜单操作。
- 部门管理页：部门树节点展开、新增按钮、编辑按钮、删除按钮、移动部门操作。
- 字典管理页：字典类型筛选控件、查询按钮、重置按钮、新增按钮、编辑按钮、删除按钮、排序操作；`listTypes/listLabels` 返回 `List<String>` 时前端类型保持不变。
- 系统日志页：日志类型筛选、用户名筛选、标题筛选、时间范围选择器、查询按钮、重置按钮、清理日志按钮。
- 审计日志页：对象类型筛选、对象 ID 输入框、操作类型筛选、操作人筛选、时间范围选择器、查询按钮、重置按钮、详情抽屉打开/关闭。

前端只在以下条件满足时改代码：

- HTTP Response 中 token、permission、audit object、user id 等字段 JSON 形态发生变化。
- HTTP Request 字段名或枚举值发生变化。
- contract test 证明现有 TypeScript 类型与后端接口不一致。

如果 HTTP JSON 形态不变，只运行前端 contract test 和必要 E2E，不改页面控件。

## Decisions

1. `ApplicationService` 返回领域实体是允许项。
   - 本次不为了输出层纯 Result 化而替换领域实体返回值。

2. 无输入条件的读查询允许无参方法。
   - `countActiveSessions()` 这类方法可保留；后续出现过滤条件时再新增业务化 `*Query`。

3. `get(*Id)` 不设置例外，统一改为 `get(Get*Query query)`。
   - `get(*Id)` 有输入条件，应按“有输入条件就 Query”的规则处理。

4. `remove(*Id)` 一律改为 `remove(Remove*Command command)`。
   - 删除是写操作，必须走 Command。

5. `PrincipalIdentityCommand` / `PrincipalCredentialCommand` 不继续包装领域实体。
   - 拆成创建、变更、变更状态、记录失败等细粒度 Command。

6. `PermissionApplicationService` 返回值强类型化为 `Set<PermissionCode>`。
   - 不新增 `PermissionSetResult`，也不继续返回 `Set<String>`。

7. `DictApplicationService.listTypes/listLabels` 保留 `List<String>`。
   - 本次不新增 `DictTypeCode`、`DictLabelValue` 或相关 Result。

8. `String identityValue`、验证码值、密码输入本次不强行值对象化。
   - 它们作为输入原文或敏感输入保留 `String`，但不得进入 Result 或明文落库。

9. `AuditObjectRef` 作为本次审计对象引用强类型。
   - 本次复用 `AuditObjectRef`，不继续拆更具体的对象 ID 类型。

10. token 相关 Result 字段改为 token 值对象。
    - `AdminAccessTokenResult`、`AdminTokenQueryResult`、`AdminTokenRefreshResult` 使用 `PrincipalAccessTokenCode` / `PrincipalRefreshTokenCode`，由 interface assembler 转成 HTTP 字符串。

11. `UserAvatarResult` 不跨 storage 域强类型化。
    - 本次不改 storage facade 契约，只记录残余风险。

## Recommendations

1. 第一批只做“低争议契约强类型化”。
   - 优先改 `AdminTokenApplicationService`、`PermissionApplicationService`、`CreateLogCommand.userId`、`Audit*Query.objectRef`。这些都是明确 ID/token/code 泄漏，收益高且判断稳定。

2. 保留 `ApplicationService` 返回领域实体。
   - 这符合当前项目 application 编排风格，也避免为简单读接口制造重复 Result。已有 `*Result` 只治理字段类型，不作为返回值替换运动。

3. 无参读方法保留。
   - `countActiveSessions()` 这类没有输入条件的方法不新增空 Query。后续出现过滤条件时，再引入业务化 Query。

4. 删除和状态变更统一使用 Command。
   - `remove(UserId)`、`remove(RoleId)`、`deleteByCondition(LogQuery)` 建议改为 `RemoveUserCommand`、`RemoveRoleCommand`、`DeleteLogCommand`，保持写操作契约一致。

5. `get(*Id)` 改为 `get(Get*Query)`。
   - 已确认不保留 `get(UserId)` 这类简洁 ID 读取例外。

6. 不把普通文本字段过度值对象化。
   - `name/email/mobile/remarks/title/url/plainPassword/captcha` 等可暂时保留 `String`。本次重点处理 ID、token、code、ref、status 和对象引用。

7. 接口重命名分两档执行。
   - 第一档重命名明显边界不清的接口：`AdminTokenApplicationService`、`PermissionApplicationService`、`AuditApplicationService`。
   - 第二档再处理 `User/Role/Menu/Department/Dict/Log` 管理类接口，避免一次性改动面过大。

8. 每个子域按“契约对象先行，调用方随后”的小步提交。
   - 推荐拆分提交：`Auth`、`Core management`、`Audit` 三批，每批控制在 1-5 个核心文件加必要调用方，符合仓库提交约定。

## Plan

按以下小任务执行。每个小任务核心改动控制在 2-6 个文件；调用方和测试文件可作为同批同步项，但提交时仍需保持 1-5 个文件的仓库约定，必要时继续拆提交。

### Task 1：token Result 字段强类型化

核心文件：

- `application/auth/result/AdminAccessTokenResult.java`
- `application/auth/result/AdminTokenQueryResult.java`
- `application/auth/result/AdminTokenRefreshResult.java`
- `interfaces/admin/auth/assembler/AuthInterfaceAssembler.java`

字段变更：

- `AdminAccessTokenResult.token`: `String` -> `PrincipalAccessTokenCode`
- `AdminAccessTokenResult.refreshToken`: `String` -> `PrincipalRefreshTokenCode`
- `AdminTokenQueryResult.token`: `String` -> `PrincipalAccessTokenCode`
- `AdminTokenRefreshResult.refreshToken`: `String` -> `PrincipalRefreshTokenCode`

同步要求：

- `AuthInterfaceAssembler.toAccessTokenResponse(...)` 输出 HTTP `token` 字符串。
- `AuthInterfaceAssembler.toRefreshTokenResponse(...)` 输出 HTTP `refreshToken` 字符串。
- `AuthInterfaceAssembler.toTokenInfoResponse(...)` 不改变前端 JSON 字段名。

### Task 2：AdminToken 创建和读取参数强类型化

核心文件：

- `application/auth/command/CreateAdminAccessTokenCommand.java`
- `application/auth/query/AdminAccessTokenQuery.java`
- `application/auth/service/AdminTokenApplicationService.java`
- `application/auth/service/impl/AdminTokenApplicationServiceImpl.java`
- `interfaces/admin/auth/controller/AuthController.java`
- `interfaces/admin/auth/service/impl/AdminAuthServiceImpl.java`

字段变更：

- 新增 `CreateAdminAccessTokenCommand.userId: UserId`
- 新增 `CreateAdminAccessTokenCommand.loginName: String`
- 新增 `CreateAdminAccessTokenCommand.ip: String`
- 新增 `CreateAdminAccessTokenCommand.userAgent: String`
- 新增 `CreateAdminAccessTokenCommand.authenticationMethod: PrincipalAuthenticationMethod`
- 新增 `CreateAdminAccessTokenCommand.identityType: PrincipalIdentityType`
- 新增 `AdminAccessTokenQuery.token: PrincipalAccessTokenCode`

方法变更：

- `createAccessToken(UserId, String, String, String, PrincipalAuthenticationMethod, PrincipalIdentityType)` -> `createAccessToken(CreateAdminAccessTokenCommand)`
- `getAccessToken(String token)` -> `getAccessToken(AdminAccessTokenQuery)`
- `validateToken(String token)` -> `validateToken(AdminAccessTokenQuery)`
- `activeAccessToken(String token)` -> `activeAccessToken(AdminAccessTokenQuery)` 或 `activateAccessToken(AdminAccessTokenQuery)`
- `getTokenInfo(String token)` -> `getTokenInfo(AdminAccessTokenQuery)`

### Task 3：AdminToken 刷新、删除、失效和失败记录强类型化

核心文件：

- `application/auth/command/RefreshAdminAccessTokenCommand.java`
- `application/auth/command/DeleteAdminAccessTokenCommand.java`
- `application/auth/command/InvalidateAdminSessionCommand.java`
- `application/auth/command/RecordPrincipalLoginFailureCommand.java`
- `application/auth/service/AdminTokenApplicationService.java`
- `application/auth/service/impl/AdminTokenApplicationServiceImpl.java`

字段变更：

- 新增 `RefreshAdminAccessTokenCommand.clientId: PrincipalClientId`
- 新增 `RefreshAdminAccessTokenCommand.refreshToken: PrincipalRefreshTokenCode`
- 新增 `RefreshAdminAccessTokenCommand.ip: String`
- 新增 `RefreshAdminAccessTokenCommand.userAgent: String`
- 新增 `DeleteAdminAccessTokenCommand.token: PrincipalAccessTokenCode`
- 新增 `DeleteAdminAccessTokenCommand.ip: String`
- 新增 `DeleteAdminAccessTokenCommand.userAgent: String`
- 新增 `InvalidateAdminSessionCommand.token: PrincipalAccessTokenCode`
- 新增 `InvalidateAdminSessionCommand.userId: UserId`
- 新增 `InvalidateAdminSessionCommand.reason: String`
- 新增 `RecordPrincipalLoginFailureCommand.principalKey: PrincipalKey`
- 新增 `RecordPrincipalLoginFailureCommand.authenticationMethod: PrincipalAuthenticationMethod`
- 新增 `RecordPrincipalLoginFailureCommand.identityType: PrincipalIdentityType`
- 新增 `RecordPrincipalLoginFailureCommand.ip: String`
- 新增 `RecordPrincipalLoginFailureCommand.userAgent: String`
- 新增 `RecordPrincipalLoginFailureCommand.reason: String`

方法变更：

- `refreshAccessToken(String clientId, String refreshToken, String ip, String userAgent)` -> `refreshAccessToken(RefreshAdminAccessTokenCommand)`
- `deleteAccessToken(String token, String ip, String userAgent)` -> `deleteAccessToken(DeleteAdminAccessTokenCommand)`
- `invalidateSessionByToken(String token, String reason)` -> `invalidateSession(InvalidateAdminSessionCommand)`
- `invalidateSessionsByUserId(UserId userId, String reason)` -> `invalidateSessions(InvalidateAdminSessionCommand)`
- 两个 `recordLoginFailed(...)` 重载合并为 `recordLoginFailed(RecordPrincipalLoginFailureCommand)`

### Task 4：Permission 契约强类型化

核心文件：

- `application/auth/command/CreatePermissionsCommand.java`
- `application/auth/query/PermissionQuery.java`
- `application/auth/service/PermissionApplicationService.java`
- `application/auth/service/impl/PermissionApplicationServiceImpl.java`
- `interfaces/admin/auth/service/impl/PermissionServiceImpl.java`

字段变更：

- 新增 `CreatePermissionsCommand.token: PrincipalAccessTokenCode`
- 新增 `CreatePermissionsCommand.userId: UserId`
- 新增 `PermissionQuery.token: PrincipalAccessTokenCode`
- 新增 `PermissionQuery.permission: PermissionCode`

方法变更：

- `createPermissions(String token, String userId)` -> `createPermissions(CreatePermissionsCommand)`，返回 `Set<PermissionCode>`
- `getPermissions(String token)` -> `getPermissions(PermissionQuery)`，返回 `Set<PermissionCode>`
- `isPermitted(String token, String permission)` -> `isPermitted(PermissionQuery)`

前端保持：

- `permission-storage.ts` 存储权限字符串集合不变，由 `PermissionServiceImpl` 或 interface assembler 转为字符串。

### Task 5：PreAuthSession 查询参数强类型化

核心文件：

- `application/auth/query/PreAuthSessionQuery.java`
- `application/auth/service/PreAuthSessionApplicationService.java`
- `application/auth/service/impl/PreAuthSessionApplicationServiceImpl.java`
- `interfaces/admin/auth/controller/AuthController.java`
- `interfaces/admin/auth/controller/CaptchaController.java`

字段变更：

- 新增 `PreAuthSessionQuery.id: PreAuthSessionId`
- 新增 `PreAuthSessionQuery.token: PreAuthSessionToken`
- 新增 `PreAuthSessionQuery.refreshToken: PreAuthSessionToken`

方法变更：

- `get(PreAuthSessionId id)` -> `get(PreAuthSessionQuery)`
- `getIdByToken(PreAuthSessionToken token)` -> `getIdByToken(PreAuthSessionQuery)` 或 `getId(PreAuthSessionQuery)`
- `getIdByRefreshToken(PreAuthSessionToken refreshToken)` -> `getIdByRefreshToken(PreAuthSessionQuery)` 或 `getId(PreAuthSessionQuery)`
- `countActiveSessions()` 保留无参。

### Task 6：PrincipalIdentity 写命令拆分

核心文件：

- `application/auth/command/CreatePrincipalIdentityCommand.java`
- `application/auth/command/ChangePrincipalIdentityCommand.java`
- `application/auth/command/ChangePrincipalIdentityStatusCommand.java`
- `application/auth/service/PrincipalIdentityApplicationService.java`
- `application/auth/service/impl/PrincipalIdentityApplicationServiceImpl.java`
- `application/auth/command/PrincipalIdentityCommand.java`

字段变更：

- 删除或废弃 `PrincipalIdentityCommand.principalIdentity: PrincipalIdentity`
- 新增 `CreatePrincipalIdentityCommand.identityType: PrincipalIdentityType`
- 新增 `CreatePrincipalIdentityCommand.identityValue: String`
- 新增 `CreatePrincipalIdentityCommand.principalKey: PrincipalKey`
- 新增 `CreatePrincipalIdentityCommand.status: PrincipalIdentityStatus`
- 新增 `ChangePrincipalIdentityCommand.id: PrincipalIdentityId`
- 新增 `ChangePrincipalIdentityCommand.identityValue: String`
- 新增 `ChangePrincipalIdentityCommand.principalKey: PrincipalKey`
- 新增 `ChangePrincipalIdentityStatusCommand.id: PrincipalIdentityId`
- 新增 `ChangePrincipalIdentityStatusCommand.status: PrincipalIdentityStatus`

方法变更：

- `create(PrincipalIdentityCommand)` -> `create(CreatePrincipalIdentityCommand)`
- `change(PrincipalIdentityCommand)` -> `change(ChangePrincipalIdentityCommand)`
- `changeStatus(PrincipalIdentityCommand)` -> `changeStatus(ChangePrincipalIdentityStatusCommand)`

### Task 7：PrincipalCredential 写命令拆分

核心文件：

- `application/auth/command/CreatePrincipalCredentialCommand.java`
- `application/auth/command/ChangePrincipalCredentialCommand.java`
- `application/auth/command/ChangePrincipalCredentialStatusCommand.java`
- `application/auth/service/PrincipalCredentialApplicationService.java`
- `application/auth/service/impl/PrincipalCredentialApplicationServiceImpl.java`
- `application/auth/command/PrincipalCredentialCommand.java`

字段变更：

- 删除或废弃 `PrincipalCredentialCommand.principalCredential: PrincipalCredential`
- 新增 `CreatePrincipalCredentialCommand.identityId: PrincipalIdentityId`
- 新增 `CreatePrincipalCredentialCommand.credentialType: PrincipalCredentialType`
- 新增 `CreatePrincipalCredentialCommand.principalKey: PrincipalKey`
- 新增 `CreatePrincipalCredentialCommand.secret: String`
- 新增 `CreatePrincipalCredentialCommand.status: PrincipalCredentialStatus`
- 新增 `ChangePrincipalCredentialCommand.id: PrincipalCredentialId`
- 新增 `ChangePrincipalCredentialCommand.secret: String`
- 新增 `ChangePrincipalCredentialStatusCommand.id: PrincipalCredentialId`
- 新增 `ChangePrincipalCredentialStatusCommand.status: PrincipalCredentialStatus`

方法变更：

- `create(PrincipalCredentialCommand)` -> `create(CreatePrincipalCredentialCommand)`
- `change(PrincipalCredentialCommand)` -> `change(ChangePrincipalCredentialCommand)`
- `changeStatus(PrincipalCredentialCommand)` -> `changeStatus(ChangePrincipalCredentialStatusCommand)`

后续小任务：

- 另建 `ChangePrincipalCredentialVerifyStateCommand.java`，字段 `id: PrincipalCredentialId`、`verified: boolean`、`failedCount: Integer`、`lockedUntil: Date`。
- 另建 `RecordPrincipalCredentialFailureCommand.java`，字段 `id: PrincipalCredentialId`、`failedAt: Date`、`lockSeconds: Integer`。
- 这两个文件与 `CurrentUserApplicationServiceImpl.java`、`PrincipalAuthApplicationServiceImpl.java` 同批同步，避免 Task 7 超过 6 个核心文件。

### Task 8：core get/remove 契约小步改造

按对象拆成独立小任务，每个小任务 4 个核心文件：

| 小任务 | 核心文件 | 新增字段 | 方法变更 |
| --- | --- | --- | --- |
| User | `GetUserQuery.java`、`RemoveUserCommand.java`、`UserApplicationService.java`、`UserApplicationServiceImpl.java` | `GetUserQuery.id: UserId`、`RemoveUserCommand.id: UserId` | `get(UserId)` -> `get(GetUserQuery)`；`remove(UserId)` -> `remove(RemoveUserCommand)` |
| Role | `GetRoleQuery.java`、`RemoveRoleCommand.java`、`RoleApplicationService.java`、`RoleApplicationServiceImpl.java` | `GetRoleQuery.id: RoleId`、`RemoveRoleCommand.id: RoleId` | `get(RoleId)` -> `get(GetRoleQuery)`；`remove(RoleId)` -> `remove(RemoveRoleCommand)` |
| Menu | `GetMenuQuery.java`、`RemoveMenuCommand.java`、`MenuApplicationService.java`、`MenuApplicationServiceImpl.java` | `GetMenuQuery.id: MenuId`、`RemoveMenuCommand.id: MenuId` | `get(MenuId)` -> `get(GetMenuQuery)`；`remove(MenuId)` -> `remove(RemoveMenuCommand)` |
| Department | `GetDepartmentQuery.java`、`RemoveDepartmentCommand.java`、`DepartmentApplicationService.java`、`DepartmentApplicationServiceImpl.java` | `GetDepartmentQuery.id: DepartmentId`、`RemoveDepartmentCommand.id: DepartmentId` | `get(DepartmentId)` -> `get(GetDepartmentQuery)`；`remove(DepartmentId)` -> `remove(RemoveDepartmentCommand)` |
| Dict | `GetDictQuery.java`、`RemoveDictCommand.java`、`DictApplicationService.java`、`DictApplicationServiceImpl.java` | `GetDictQuery.id: DictId`、`RemoveDictCommand.id: DictId` | `get(DictId)` -> `get(GetDictQuery)`；`remove(DictId)` -> `remove(RemoveDictCommand)` |
| Log | `GetLogQuery.java`、`DeleteLogCommand.java`、`LogApplicationService.java`、`LogApplicationServiceImpl.java` | `GetLogQuery.id: LogId`、`DeleteLogCommand.query: LogQuery` | `get(LogId)` -> `get(GetLogQuery)`；`deleteByCondition(LogQuery)` -> `delete(DeleteLogCommand)` |

每个对象小任务同步对应 controller：

- User：`interfaces/admin/core/controller/UserController.java`
- Role：`interfaces/admin/core/controller/RoleController.java`
- Menu：`interfaces/admin/core/controller/MenuController.java`
- Department：`interfaces/admin/core/controller/DepartmentController.java`
- Dict：`interfaces/admin/core/controller/DictController.java`
- Log：`interfaces/admin/core/controller/LogController.java`、`interfaces/admin/core/service/impl/SysLogMessageServiceImpl.java`

### Task 9：Log 命令字段强类型化

核心文件：

- `application/core/command/CreateLogCommand.java`
- `application/core/service/LogApplicationService.java`
- `application/core/service/impl/LogApplicationServiceImpl.java`
- `interfaces/admin/core/service/impl/SysLogMessageServiceImpl.java`

字段变更：

- `CreateLogCommand.userId`: `String` -> `UserId`

实现变更：

- 删除 `LogApplicationServiceImpl.toLog(...)` 中 `UserIdCodec.toDomain(command.getUserId())`。
- `SysLogMessageServiceImpl` 在 interface 层使用 `UserIdCodec.toDomain(...)` 完成 HTTP/security 字符串到 `UserId` 的转换。

### Task 10：CurrentUser avatar 查询强类型化

核心文件：

- `application/core/query/CurrentUserAvatarQuery.java`
- `application/core/service/CurrentUserApplicationService.java`
- `application/core/service/impl/CurrentUserApplicationServiceImpl.java`
- `interfaces/admin/core/controller/CurrentUserController.java`

字段变更：

- 新增 `CurrentUserAvatarQuery.userId: UserId`

方法变更：

- `getAvatar(UserId userId)` -> `getAvatar(CurrentUserAvatarQuery)`
- `getAvatarInputStream(UserId userId)` -> `getAvatarInputStream(CurrentUserAvatarQuery)`
- `existsAvatar(UserId userId)` -> `existsAvatar(CurrentUserAvatarQuery)`

前端控件验证：

- 用户头像上传控件选择文件后保存。
- 用户头像预览图片刷新。
- 删除头像按钮执行后头像恢复默认态。

### Task 11：audit 对象引用强类型化

核心文件：

- `application/audit/command/CreateAuditLogCommand.java`
- `application/audit/query/AuditLogQuery.java`
- `application/audit/query/AuditMetaQuery.java`
- `application/audit/service/AuditApplicationService.java`
- `application/audit/service/impl/AuditApplicationServiceImpl.java`
- `application/audit/query/GetAuditLogQuery.java`

字段变更：

- `CreateAuditLogCommand.objectType`: 删除，替换为 `objectRef.objectType`
- `CreateAuditLogCommand.objectId`: 删除，替换为 `objectRef.objectId`
- `CreateAuditLogCommand.operatorType`: 删除，替换为 `operatorRef.operatorType`
- `CreateAuditLogCommand.operatorId`: 删除，替换为 `operatorRef.operatorId`
- `CreateAuditLogCommand.operatorName`: 删除，替换为 `operatorRef.operatorName`
- 新增 `CreateAuditLogCommand.objectRef: AuditObjectRef`
- 新增 `CreateAuditLogCommand.operatorRef: AuditOperatorRef`
- `AuditLogQuery.objectType`: 删除，替换为 `objectRef.objectType`
- `AuditLogQuery.objectId`: 删除，替换为 `objectRef.objectId`
- `AuditLogQuery.operatorType`: 删除，替换为 `operatorRef.operatorType`
- `AuditLogQuery.operatorId`: 删除，替换为 `operatorRef.operatorId`
- 新增 `AuditLogQuery.objectRef: AuditObjectRef`
- 新增 `AuditLogQuery.operatorRef: AuditOperatorRef`
- `AuditMetaQuery.objectType`: 删除，替换为 `objectRef.objectType`
- `AuditMetaQuery.objectId`: 删除，替换为 `objectRef.objectId`
- 新增 `AuditMetaQuery.objectRef: AuditObjectRef`
- 新增 `GetAuditLogQuery.id: AuditLogId`

同步文件：

- `application/audit/runtime/AuditLogAspect.java`
- `interfaces/admin/audit/controller/AuditController.java`
- `interfaces/admin/audit/assembler/AuditInterfaceAssembler.java`

### Task 12：audit loader 调用 get 查询对象

核心文件：

- `application/audit/runtime/sys/DepartmentAuditObjectLoader.java`
- `application/audit/runtime/sys/DictAuditObjectLoader.java`
- `application/audit/runtime/sys/MenuAuditObjectLoader.java`
- `application/audit/runtime/sys/RoleAuditObjectLoader.java`
- `application/audit/runtime/sys/UserAuditObjectLoader.java`

字段变更：

- 无新增字段。

实现变更：

- 将 `departmentService.get(DepartmentIdCodec.toDomain(...))` 改为 `departmentService.get(new GetDepartmentQuery(...))`。
- 将 `dictService.get(DictIdCodec.toDomain(...))` 改为 `dictService.get(new GetDictQuery(...))`。
- 将 `menuService.get(MenuIdCodec.toDomain(...))` 改为 `menuService.get(new GetMenuQuery(...))`。
- 将 `roleService.get(RoleIdCodec.toDomain(...))` 改为 `roleService.get(new GetRoleQuery(...))`。
- 将 `userService.get(UserIdCodec.toDomain(...))` 改为 `userService.get(new GetUserQuery(...))`。

### Task 13：ApplicationService 接口业务化重命名

按接口拆小任务，每个小任务 2 个核心文件：

| 小任务 | 核心文件 | 重命名 |
| --- | --- | --- |
| Audit | `AuditApplicationService.java`、`AuditApplicationServiceImpl.java` | `AuditApplicationService` -> `AuditTrailApplicationService` |
| Admin token | `AdminTokenApplicationService.java`、`AdminTokenApplicationServiceImpl.java` | `AdminTokenApplicationService` -> `AdminSessionTokenApplicationService` |
| Permission | `PermissionApplicationService.java`、`PermissionApplicationServiceImpl.java` | `PermissionApplicationService` -> `PrincipalPermissionApplicationService` |
| User | `UserApplicationService.java`、`UserApplicationServiceImpl.java` | `UserApplicationService` -> `UserManagementApplicationService` |
| Role | `RoleApplicationService.java`、`RoleApplicationServiceImpl.java` | `RoleApplicationService` -> `RoleManagementApplicationService` |
| Menu | `MenuApplicationService.java`、`MenuApplicationServiceImpl.java` | `MenuApplicationService` -> `MenuManagementApplicationService` |
| Department | `DepartmentApplicationService.java`、`DepartmentApplicationServiceImpl.java` | `DepartmentApplicationService` -> `DepartmentManagementApplicationService` |
| Dict | `DictApplicationService.java`、`DictApplicationServiceImpl.java` | `DictApplicationService` -> `DictionaryManagementApplicationService` |
| Log | `LogApplicationService.java`、`LogApplicationServiceImpl.java` | `LogApplicationService` -> `SystemLogApplicationService` |
| Current user | `CurrentUserApplicationService.java`、`CurrentUserApplicationServiceImpl.java` | `CurrentUserApplicationService` -> `CurrentUserProfileApplicationService` |
| Principal auth | `PrincipalAuthApplicationService.java`、`PrincipalAuthApplicationServiceImpl.java` | `PrincipalAuthApplicationService` -> `PrincipalAuthenticationApplicationService` |

同步要求：

- 每次重命名只同步直接编译失败的 import 和构造器类型。
- 文件移动用 IDE 或 `git mv`，保持类名、文件名、实现类名一致。

### Task 14：前端契约和交互验收

核心文件：

- `src/auth/auth-service-contract.test.ts`
- `src/pages/system/common/system-service-contract.test.ts`
- `src/pages/system/common/log-service-contract.test.ts`
- `src/pages/system/user/components/user-edit-drawer/user-edit-drawer.test.tsx`
- `e2e/auth/login/login.spec.ts`
- `e2e/system/dictionary/dictionary.spec.ts`

检查点：

- 登录页用户名输入框、密码输入框、验证码输入框和登录按钮不因 token Result 强类型化改变 JSON。
- 权限拉取后 `permission-storage.ts` 仍保存字符串 permission code。
- 用户编辑抽屉的角色多选控件仍提交角色 ID 数组。
- 字典页类型筛选和 label 展示仍按 `List<String>` 处理。
- 系统日志筛选和清理按钮仍调用原 HTTP 契约。

如发现 HTTP 契约变化，再精确修改对应 service/types/page 文件；否则不改前端实现文件。

## Verification

每个小批次先运行窄测试：

```sh
cd kuzhambu-servers
mvn -pl biz/system/kuzhambu-system-application -Dtest='*ApplicationServiceImplTest,SystemApplicationArchitectureTest' test
mvn -pl biz/system/kuzhambu-system-interface -am -DskipTests compile
```

批次完成后运行格式和静态检查：

```sh
cd kuzhambu-servers
mvn -pl biz/system/kuzhambu-system-application,biz/system/kuzhambu-system-interface spotless:apply
mvn spotless:check
mvn checkstyle:check
```

最终运行：

```sh
cd kuzhambu-servers
mvn -pl biz/system -am test
```

人工检查：

- `rg -n "save\\s*\\(" kuzhambu-servers/biz/system/kuzhambu-system-application kuzhambu-servers/biz/system/kuzhambu-system-interface` 应无新增 application service 保存语义。
- `rg -n "ApplicationService.java:.*\\((String|Long|long|Integer|int) " ...` 不适合直接使用，因为接口方法可能跨行；改用人工检查所有 `*ApplicationService.java` 方法参数。
- `ApplicationService` 返回领域实体为允许项；不要用领域实体 import 清零作为验收条件。

## Closure

本 RUNBOOK 是临时执行手册。强类型化改造完成并通过验证后：

- 删除 `docs/30-designs/RUNBOOK-SYSTEM-APPLICATION-TYPED-CONTRACT.md`。
- 如有长期规则，迁移到 `docs/00-governance/SERVERS-ARCHITECTURE.md` 或 `docs/00-governance/SERVERS-ARCHITECTURE-RULES.md`。
- 如需保留验证证据，新增或更新 `docs/40-readiness/` 下对应 evidence 文档。
