# ArchUnit allowlist 清理 04：System 认证与审计

## Purpose

清理 System 域认证、会话、权限与审计切片的 legacy allowlist。

## Scope

本批只清理 System 认证、会话、权限与审计切片的 allowlist。允许修改的 allowlist 声明文件只有以下 3 个：

- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/SystemApplicationCommandQueryRecordAllowances.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/SystemApplicationArchitectureTest.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/SystemInterfaceArchitectureTest.java`

### Rule Inventory

| 规则 | 本批 allowlist 范围 | 整改目标 |
| --- | --- | --- |
| `COMMAND_QUERY_RECORD` | 33 个 audit/auth command/query key | 对应类改为 Java `record`，移除 Lombok，并同步构造调用方。 |
| `METHOD_SHAPE`、`COUNT_RETURN` | `PreAuthSessionApplicationService.countActiveSessions()` 2 个 key | 返回值改为 `long`；保留合法无参读操作或改为显式 Query。 |
| `COMMAND_QUERY_CONSTRUCTION` | 55 个 key，owner 限定在本 RUNBOOK 下方列出的 14 个生产文件 | 将 Command/Query 构造移动到 `*InterfaceAssembler` 或应用服务内部。 |
| `CONTROLLER_ACTION_VERB` | `AuditController`、`AuthController`、`CaptchaController` 3 个 key | controller 方法名和 action path 改为共享动词白名单。 |
| `BOUNDARY_ASSEMBLER_NULLNESS` | `AuditInterfaceAssembler`、`AuthInterfaceAssembler` 2 个 class key | public assembler 方法不返回 `null`。 |

### Production File Inventory

`COMMAND_QUERY_RECORD` 只覆盖以下 33 个生产文件：

- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/command/CreateAuditLogCommand.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/query/AuditLogQuery.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/query/AuditMetaQuery.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/query/GetAuditLogQuery.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/AuthenticateIdentityCommand.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/AuthenticatePasswordCommand.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/ChangePrincipalCredentialCommand.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/ChangePrincipalCredentialStatusCommand.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/ChangePrincipalCredentialVerifyStateCommand.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/ChangePrincipalIdentityCommand.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/ChangePrincipalIdentityStatusCommand.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/CreateAdminAccessTokenCommand.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/CreatePermissionsCommand.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/CreatePreAuthSessionCommand.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/CreatePrincipalCredentialCommand.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/CreatePrincipalIdentityCommand.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/DeleteAdminAccessTokenCommand.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/InvalidateAdminSessionCommand.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/PrincipalCredentialCommand.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/PrincipalIdentityCommand.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/RecordPrincipalCredentialFailureCommand.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/RecordPrincipalLoginFailureCommand.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/RefreshAdminAccessTokenCommand.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/RefreshPreAuthSessionCommand.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/ReleasePreAuthSessionCommand.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/UpsertPreAuthSessionValueCommand.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/query/AdminAccessTokenQuery.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/query/PermissionQuery.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/query/PreAuthSessionQuery.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/query/PreAuthSessionValueQuery.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/query/PreAuthSessionValueValidateQuery.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/query/PrincipalCredentialQuery.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/query/PrincipalIdentityQuery.java`

`METHOD_SHAPE`、`COUNT_RETURN` 只覆盖以下 2 个生产文件：

- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/service/PreAuthSessionApplicationService.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/service/impl/PreAuthSessionApplicationServiceImpl.java`

`COMMAND_QUERY_CONSTRUCTION` 只覆盖以下 14 个 owner 生产文件：

- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/AuditLogAspect.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/DepartmentAuditObjectLoader.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/DictAuditObjectLoader.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/MenuAuditObjectLoader.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/RoleAuditObjectLoader.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/UserAuditObjectLoader.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/controller/AuditController.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/auth/controller/AuthController.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/auth/controller/CaptchaController.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/auth/security/CurrentUserResolver.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/auth/service/impl/AdminAuthServiceImpl.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/CurrentUserController.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/UserController.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/auth/service/impl/PermissionServiceImpl.java`

其中 `CurrentUserController.java` 和 `UserController.java` 只处理下方 inventory 明确列出的 auth contract construction key。

`CONTROLLER_ACTION_VERB` 只覆盖以下 3 个生产文件：

- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/controller/AuditController.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/auth/controller/AuthController.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/auth/controller/CaptchaController.java`

`BOUNDARY_ASSEMBLER_NULLNESS` 只覆盖以下 2 个生产文件：

- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/assembler/AuditInterfaceAssembler.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/auth/assembler/AuthInterfaceAssembler.java`

### Allowlist Key Inventory

`SystemApplicationCommandQueryRecordAllowances.java` 删除以下 33 个 key：

- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.audit.command.CreateAuditLogCommand`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.audit.query.AuditLogQuery`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.audit.query.AuditMetaQuery`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.audit.query.GetAuditLogQuery`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.AuthenticateIdentityCommand`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.AuthenticatePasswordCommand`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.ChangePrincipalCredentialCommand`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.ChangePrincipalCredentialStatusCommand`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.ChangePrincipalCredentialVerifyStateCommand`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.ChangePrincipalIdentityCommand`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.ChangePrincipalIdentityStatusCommand`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.CreateAdminAccessTokenCommand`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.CreatePermissionsCommand`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.CreatePreAuthSessionCommand`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.CreatePrincipalCredentialCommand`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.CreatePrincipalIdentityCommand`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.DeleteAdminAccessTokenCommand`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.InvalidateAdminSessionCommand`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.PrincipalCredentialCommand`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.PrincipalIdentityCommand`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.RecordPrincipalCredentialFailureCommand`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.RecordPrincipalLoginFailureCommand`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.RefreshAdminAccessTokenCommand`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.RefreshPreAuthSessionCommand`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.ReleasePreAuthSessionCommand`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.UpsertPreAuthSessionValueCommand`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.query.AdminAccessTokenQuery`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.query.PermissionQuery`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.query.PreAuthSessionQuery`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.query.PreAuthSessionValueQuery`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.query.PreAuthSessionValueValidateQuery`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.query.PrincipalCredentialQuery`
- `COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.query.PrincipalIdentityQuery`

`SystemApplicationArchitectureTest.java` 删除以下 57 个 key：

- `METHOD_SHAPE:com.thundax.kuzhambu.system.application.auth.service.PreAuthSessionApplicationService.countActiveSessions()`
- `COUNT_RETURN:com.thundax.kuzhambu.system.application.auth.service.PreAuthSessionApplicationService.countActiveSessions()`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.application.audit.runtime.AuditLogAspect#CreateAuditLogCommand:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.application.audit.runtime.sys.DepartmentAuditObjectLoader#GetDepartmentQuery:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.application.audit.runtime.sys.DictAuditObjectLoader#GetDictQuery:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.application.audit.runtime.sys.MenuAuditObjectLoader#GetMenuQuery:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.application.audit.runtime.sys.RoleAuditObjectLoader#GetRoleQuery:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.application.audit.runtime.sys.UserAuditObjectLoader#GetUserQuery:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.audit.controller.AuditController#PageQuery:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.controller.AuthController#CreatePreAuthSessionCommand:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.controller.AuthController#PreAuthSessionQuery:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.controller.AuthController#PreAuthSessionQuery:2`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.controller.AuthController#PreAuthSessionQuery:3`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.controller.AuthController#PreAuthSessionValueQuery:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.controller.AuthController#PreAuthSessionValueValidateQuery:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.controller.AuthController#PreAuthSessionValueValidateQuery:2`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.controller.AuthController#RefreshPreAuthSessionCommand:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.controller.AuthController#ReleasePreAuthSessionCommand:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.controller.AuthController#UpsertPreAuthSessionValueCommand:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.controller.AuthController#UpsertPreAuthSessionValueCommand:2`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.controller.AuthController#UpsertPreAuthSessionValueCommand:3`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.controller.CaptchaController#PreAuthSessionQuery:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.controller.CaptchaController#PreAuthSessionValueQuery:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.controller.CaptchaController#UpsertPreAuthSessionValueCommand:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.security.CurrentUserResolver#GetUserQuery:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl.AdminAuthServiceImpl#AdminAccessTokenQuery:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl.AdminAuthServiceImpl#AuthenticateIdentityCommand:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl.AdminAuthServiceImpl#AuthenticatePasswordCommand:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl.AdminAuthServiceImpl#CreateAdminAccessTokenCommand:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl.AdminAuthServiceImpl#DeleteAdminAccessTokenCommand:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl.AdminAuthServiceImpl#DeleteAdminAccessTokenCommand:2`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl.AdminAuthServiceImpl#GetUserQuery:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl.AdminAuthServiceImpl#InvalidateAdminSessionCommand:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl.AdminAuthServiceImpl#InvalidateAdminSessionCommand:2`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl.AdminAuthServiceImpl#PrincipalIdentityQuery:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl.AdminAuthServiceImpl#RecordPrincipalLoginFailureCommand:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl.AdminAuthServiceImpl#RecordPrincipalLoginFailureCommand:2`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl.AdminAuthServiceImpl#RecordPrincipalLoginFailureCommand:3`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl.AdminAuthServiceImpl#RecordPrincipalLoginFailureCommand:4`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl.AdminAuthServiceImpl#RecordPrincipalLoginFailureCommand:5`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl.AdminAuthServiceImpl#RecordPrincipalLoginFailureCommand:6`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl.AdminAuthServiceImpl#RefreshAdminAccessTokenCommand:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl.PermissionServiceImpl#CreatePermissionsCommand:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl.PermissionServiceImpl#PermissionQuery:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl.PermissionServiceImpl#PermissionQuery:2`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.core.controller.CurrentUserController#PreAuthSessionQuery:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.core.controller.CurrentUserController#PreAuthSessionValueQuery:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.core.controller.CurrentUserController#PrincipalIdentityQuery:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.core.controller.UserController#ChangePrincipalCredentialCommand:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.core.controller.UserController#ChangePrincipalIdentityCommand:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.core.controller.UserController#CreatePrincipalCredentialCommand:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.core.controller.UserController#CreatePrincipalIdentityCommand:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.core.controller.UserController#PreAuthSessionQuery:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.core.controller.UserController#PreAuthSessionValueQuery:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.core.controller.UserController#PrincipalCredentialQuery:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.core.controller.UserController#PrincipalIdentityQuery:1`
- `COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.system.interfaces.admin.core.controller.UserController#PrincipalIdentityQuery:2`

`SystemInterfaceArchitectureTest.java` 删除以下 5 个 key/class：

- `CONTROLLER_ACTION_VERB:*AuditController.java*`
- `CONTROLLER_ACTION_VERB:*AuthController.java*`
- `CONTROLLER_ACTION_VERB:*CaptchaController.java*`
- `com.thundax.kuzhambu.system.interfaces.admin.audit.assembler.AuditInterfaceAssembler`
- `com.thundax.kuzhambu.system.interfaces.admin.auth.assembler.AuthInterfaceAssembler`

## Non-goals

不处理 `application.core` 的 record 例外。不处理 `CurrentUserController`、`DepartmentController`、`DictController`、`LogController`、`MenuController`、`RoleController`、`UserController` 的 Controller 动词 allowlist。不处理 core assembler 空返回 allowlist。

`CurrentUserController.java` 和 `UserController.java` 只在删除上方列出的 auth contract construction key 时作为调用方纳入；这两个文件内的 core command/query construction key 留给 System core 批次。

## Plan

1. 先按 `Allowlist Key Inventory` 建立删除清单；任何不在清单内的 System allowlist 不属于本批。
2. 逐个 `Production File Inventory` 文件完成整改；每改完一个生产文件，只删除该文件对应的精确 key。
3. `CurrentUserController.java`、`UserController.java` 只删除本 RUNBOOK 列出的 auth construction key，保留 core construction key。
4. Controller 动词变更必须同步更新 HTTP 调用方；不得以新增 wildcard 替代现有 key。

## Verification

在 `kuzhambu-servers/` 下运行：`mvn -pl biz/system/kuzhambu-system-application,biz/system/kuzhambu-system-interface -am test`，再执行 `mvn spotless:check`、`mvn checkstyle:check`。

## Closure

认证与审计条目清零后删除本文档。
