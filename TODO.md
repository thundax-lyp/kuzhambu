# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 当前任务项按数字编号顺序执行；不得跳过前置契约任务直接做后续调用方或重命名任务。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `ARCHUNIT-SYSTEM-AUTH-04-01`：迁移 audit application contract 与 audit runtime 构造点
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-04-SYSTEM-AUTH-AUDIT.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/command/CreateAuditLogCommand.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/query/AuditLogQuery.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/query/AuditMetaQuery.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/query/GetAuditLogQuery.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/AuditLogAspect.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/DepartmentAuditObjectLoader.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/DictAuditObjectLoader.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/MenuAuditObjectLoader.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/RoleAuditObjectLoader.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/UserAuditObjectLoader.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/SystemApplicationCommandQueryRecordAllowances.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/SystemApplicationArchitectureTest.java`
    - 处理动作：将 audit command/query 改为 record，并移除 audit runtime 中本批列出的直接构造 allowlist。
    - 验收点：RUNBOOK 中 audit record key 和 audit runtime construction key 已删除，system application 架构测试通过。
    - 重要度：9/10

- [ ] `ARCHUNIT-SYSTEM-AUTH-04-02`：迁移 pre-auth session contract 与 count 方法形态
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-04-SYSTEM-AUTH-AUDIT.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/CreatePreAuthSessionCommand.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/RefreshPreAuthSessionCommand.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/ReleasePreAuthSessionCommand.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/UpsertPreAuthSessionValueCommand.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/query/PreAuthSessionQuery.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/query/PreAuthSessionValueQuery.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/query/PreAuthSessionValueValidateQuery.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/service/PreAuthSessionApplicationService.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/service/impl/PreAuthSessionApplicationServiceImpl.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/SystemApplicationCommandQueryRecordAllowances.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/SystemApplicationArchitectureTest.java`
    - 处理动作：将 pre-auth session command/query 改为 record，并修正 `countActiveSessions()` 的返回值和合法方法形态。
    - 验收点：RUNBOOK 中 pre-auth session record key、`METHOD_SHAPE` key 和 `COUNT_RETURN` key 已删除，system application 架构测试通过。
    - 重要度：9/10

- [ ] `ARCHUNIT-SYSTEM-AUTH-04-03`：迁移 auth controller 与 captcha controller 的 pre-auth 构造点
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-04-SYSTEM-AUTH-AUDIT.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/auth/controller/AuthController.java`；`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/auth/controller/CaptchaController.java`；`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/auth/assembler/AuthInterfaceAssembler.java`；`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/auth/assembler/CaptchaInterfaceAssembler.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/SystemApplicationArchitectureTest.java`
    - 处理动作：将 pre-auth session command/query 的构造从 controller 移入 interface assembler。
    - 验收点：RUNBOOK 中 `AuthController`、`CaptchaController` 的 pre-auth construction key 已删除，system application 架构测试通过。
    - 重要度：8/10

- [ ] `ARCHUNIT-SYSTEM-AUTH-04-04`：迁移 admin auth 登录与 token contract 及服务构造点
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-04-SYSTEM-AUTH-AUDIT.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/AuthenticateIdentityCommand.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/AuthenticatePasswordCommand.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/CreateAdminAccessTokenCommand.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/DeleteAdminAccessTokenCommand.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/InvalidateAdminSessionCommand.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/RecordPrincipalLoginFailureCommand.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/RefreshAdminAccessTokenCommand.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/query/AdminAccessTokenQuery.java`；`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/auth/security/CurrentUserResolver.java`；`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/auth/service/impl/AdminAuthServiceImpl.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/SystemApplicationCommandQueryRecordAllowances.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/SystemApplicationArchitectureTest.java`
    - 处理动作：将登录与 token command/query 改为 record，并将服务层直接构造迁移到合法边界。
    - 验收点：RUNBOOK 中登录与 token record key、`AdminAuthServiceImpl` 和 `CurrentUserResolver` 对应 construction key 已删除，system application 架构测试通过。
    - 重要度：9/10

- [ ] `ARCHUNIT-SYSTEM-AUTH-04-05`：迁移 principal identity contract 与 core auth 调用方
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-04-SYSTEM-AUTH-AUDIT.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/ChangePrincipalIdentityCommand.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/ChangePrincipalIdentityStatusCommand.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/CreatePrincipalIdentityCommand.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/PrincipalIdentityCommand.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/query/PrincipalIdentityQuery.java`；`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/CurrentUserController.java`；`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/UserController.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/SystemApplicationCommandQueryRecordAllowances.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/SystemApplicationArchitectureTest.java`
    - 处理动作：将 principal identity command/query 改为 record，并仅迁移 core controller 中本批列出的 auth construction key。
    - 验收点：RUNBOOK 中 principal identity record key 和 `CurrentUserController`、`UserController` 的 identity auth construction key 已删除，core construction key 保留。
    - 重要度：8/10

- [ ] `ARCHUNIT-SYSTEM-AUTH-04-06`：迁移 principal credential contract 与 core auth 调用方
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-04-SYSTEM-AUTH-AUDIT.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/ChangePrincipalCredentialCommand.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/ChangePrincipalCredentialStatusCommand.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/ChangePrincipalCredentialVerifyStateCommand.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/CreatePrincipalCredentialCommand.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/PrincipalCredentialCommand.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/RecordPrincipalCredentialFailureCommand.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/query/PrincipalCredentialQuery.java`；`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/UserController.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/SystemApplicationCommandQueryRecordAllowances.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/SystemApplicationArchitectureTest.java`
    - 处理动作：将 principal credential command/query 改为 record，并仅迁移 `UserController` 中本批列出的 auth construction key。
    - 验收点：RUNBOOK 中 principal credential record key 和 `UserController` 的 credential auth construction key 已删除，core construction key 保留。
    - 重要度：8/10

- [ ] `ARCHUNIT-SYSTEM-AUTH-04-07`：迁移 permission contract 与权限服务构造点
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-04-SYSTEM-AUTH-AUDIT.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/CreatePermissionsCommand.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/query/PermissionQuery.java`；`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/auth/service/impl/PermissionServiceImpl.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/SystemApplicationCommandQueryRecordAllowances.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/SystemApplicationArchitectureTest.java`
    - 处理动作：将 permission command/query 改为 record，并迁移 `PermissionServiceImpl` 中的直接构造点。
    - 验收点：RUNBOOK 中 permission record key 和 `PermissionServiceImpl` construction key 已删除，system application 架构测试通过。
    - 重要度：8/10

- [ ] `ARCHUNIT-SYSTEM-AUTH-04-08`：收口 auth、captcha、audit controller 动词和 assembler 空返回
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-04-SYSTEM-AUTH-AUDIT.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/controller/AuditController.java`；`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/auth/controller/AuthController.java`；`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/auth/controller/CaptchaController.java`；`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/assembler/AuditInterfaceAssembler.java`；`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/auth/assembler/AuthInterfaceAssembler.java`；`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/SystemInterfaceArchitectureTest.java`
    - 处理动作：将三个 controller 的 action verb 和两个 assembler 的空返回整改到规则允许形态。
    - 验收点：RUNBOOK 中 `CONTROLLER_ACTION_VERB` 三个 key 和两个 assembler nullness class key 已删除，system interface 架构测试通过。
    - 重要度：8/10

- [ ] `ARCHUNIT-SYSTEM-AUTH-04-09`：清理 System 认证与审计 allowlist 清理现场
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`；`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-04-SYSTEM-AUTH-AUDIT.md`
    - 范围对象：`TODO.md`；`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-04-SYSTEM-AUTH-AUDIT.md`
    - 处理动作：在本批 allowlist 清零且验证通过后，删除临时 RUNBOOK 并移除本批已完成 TODO。
    - 验收点：`TODO.md` 不再保留本批已完成任务，临时 RUNBOOK 已删除或按治理要求迁移后删除。
    - 重要度：7/10

## 待讨论项
