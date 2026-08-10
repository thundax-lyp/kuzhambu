# ArchUnit allowlist 清理 05：System Core

## Purpose

清理 System Core（当前用户、用户、角色、菜单、部门、字典、系统日志）切片的 legacy allowlist，并先处理 PR #232 遗留问题，避免 04 批次已发现的债务继续丢失跟踪。

## Execution Order

必须按以下顺序执行：

1. 先处理 PR #232 遗留问题。
2. 再转换 System Core application Command/Query 为 record。
3. 再把 System Core controller/service 中直接构造 Command/Query 的逻辑移动到 assembler 或允许的 application 编排边界。
4. 再收敛 System Core controller action verb。
5. 最后删除对应 allowlist，并执行验证和现场清理。

## PR #232 遗留问题

这些问题来自 GitHub PR #232 的 Codex review threads。PR #232 已合并，两个 thread 已按“下个版本处理”回复并 resolved；05 批次必须把这些 resolved thread 当作前置任务先执行。

| GitHub thread | Codex 标题 | GitHub 定位 | 必改文件 | 具体要求 |
| --- | --- | --- | --- | --- |
| [discussion_r3746603229](https://github.com/thundax-lyp/kuzhambu/pull/232#discussion_r3746603229) | Update the login smoke guide for the renamed route | `AuthController.java:95`，reviewed commit `01999d753d` | `docs/00-governance/HOW-TO-ADMIN-LOGIN-SMOKE.md` | GitHub 评论指出文档第 84、121 行仍使用 `/api/auth/session/pre-auth-session`，而代码已改为 `pre-auth-session/request`；必须将说明和 curl 命令同步为 `/api/auth/session/pre-auth-session/request`，否则按文档执行登录 smoke 会在预认证步骤 404。 |
| [discussion_r3746633916](https://github.com/thundax-lyp/kuzhambu/pull/232#discussion_r3746633916) | Move audit command construction to an allowed boundary | `AuditLogAspect.java:30`，reviewed commit `74c1dca431` | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/AuditLogAspect.java` | GitHub 评论指出 `CreateAuditLogCommand::new` 仍在 `AuditLogAspect` 中构造 application command，且现有 ArchUnit 只检测文本形式 `new XxxCommand(...)`，导致构造器引用绕过规则并丢失债务跟踪；必须将 command 创建移动到允许的 application assembler 或 `ApplicationService` 编排边界。 |
| [discussion_r3746633916](https://github.com/thundax-lyp/kuzhambu/pull/232#discussion_r3746633916) | Move audit command construction to an allowed boundary | `AuditLogAspect.java:30`，reviewed commit `74c1dca431` | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/SystemApplicationArchitectureTest.java` | 处理 audit command 构造后，必须确认 `assertApplicationCommandQueryConstructionInAssemblersOrApplicationServices` 不再被构造器引用绕过；如果不能通过生产代码彻底收口，必须显式保留债务或补强规则。 |

## Allowlist Inventory

### Application Command/Query record allowlist

所在文件：`kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/SystemApplicationCommandQueryRecordAllowances.java`

目标：以下 application contract 文件全部转换为 Java record，并删除对应 `COMMAND_QUERY_RECORD:*` allowlist。

| 分组 | 文件 |
| --- | --- |
| CurrentUser command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/ChangeCurrentUserAvatarCommand.java` |
| CurrentUser command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/ChangeCurrentUserInfoCommand.java` |
| CurrentUser command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/ChangeCurrentUserPasswordCommand.java` |
| CurrentUser command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/RemoveCurrentUserAvatarCommand.java` |
| CurrentUser command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/CurrentUserAvatarQuery.java` |
| CurrentUser command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/CurrentUserQuery.java` |
| Department command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/ChangeDepartmentInfoCommand.java` |
| Department command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/CreateDepartmentCommand.java` |
| Department command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/MoveDepartmentCommand.java` |
| Department command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/RemoveDepartmentCommand.java` |
| Department command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/DepartmentQuery.java` |
| Department command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/GetDepartmentQuery.java` |
| Dict command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/ChangeDictInfoCommand.java` |
| Dict command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/CreateDictCommand.java` |
| Dict command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/DictSortCommand.java` |
| Dict command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/RemoveDictCommand.java` |
| Dict command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/DictQuery.java` |
| Dict command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/GetDictQuery.java` |
| Log command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/CreateLogCommand.java` |
| Log command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/DeleteLogCommand.java` |
| Log command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/GetLogQuery.java` |
| Log command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/LogQuery.java` |
| Menu command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/ChangeMenuInfoCommand.java` |
| Menu command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/ChangeMenuVisibilityCommand.java` |
| Menu command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/CreateMenuCommand.java` |
| Menu command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/MoveMenuCommand.java` |
| Menu command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/RemoveMenuCommand.java` |
| Menu command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/GetMenuQuery.java` |
| Menu command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/MenuQuery.java` |
| Role command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/AssignRoleUsersCommand.java` |
| Role command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/ChangeRoleInfoCommand.java` |
| Role command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/ChangeRoleStatusCommand.java` |
| Role command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/CreateRoleCommand.java` |
| Role command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/RemoveRoleCommand.java` |
| Role command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/RoleSortCommand.java` |
| Role command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/GetRoleQuery.java` |
| Role command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/RoleQuery.java` |
| User command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/ChangeUserInfoCommand.java` |
| User command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/ChangeUserStatusCommand.java` |
| User command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/CreateUserCommand.java` |
| User command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/RemoveUserCommand.java` |
| User command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/GetUserQuery.java` |
| User command/query | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/UserQuery.java` |

### Command/Query construction allowlist

所在文件：`kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/SystemApplicationArchitectureTest.java`

目标：下列文件不得直接构造 application Command/Query；转换逻辑应进入同组 assembler，或进入允许的 application service 编排边界。

| 分组 | 当前直接构造所在文件 | 目标承接文件 |
| --- | --- | --- |
| CurrentUser / Personal | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/CurrentUserController.java` | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/assembler/PersonalInterfaceAssembler.java` |
| Department | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/DepartmentController.java` | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/assembler/DepartmentInterfaceAssembler.java` |
| Dict | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/DictController.java` | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/assembler/DictInterfaceAssembler.java` |
| Log | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/LogController.java` | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/assembler/LogInterfaceAssembler.java` |
| Log service | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/service/impl/SysLogMessageServiceImpl.java` | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/assembler/LogInterfaceAssembler.java` |
| Menu | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/MenuController.java` | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/assembler/MenuInterfaceAssembler.java` |
| Role | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/RoleController.java` | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/assembler/RoleInterfaceAssembler.java` |
| User | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/UserController.java` | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/assembler/UserInterfaceAssembler.java` |

### Interface action verb and assembler nullness allowlist

所在文件：`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/SystemInterfaceArchitectureTest.java`

目标：移除以下 controller action verb allowlist，并清理对应 assembler public method nullness allowlist。

| 分组 | Controller 文件 | Assembler 文件 |
| --- | --- | --- |
| CurrentUser / Personal | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/CurrentUserController.java` | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/assembler/PersonalInterfaceAssembler.java` |
| Department | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/DepartmentController.java` | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/assembler/DepartmentInterfaceAssembler.java` |
| Dict | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/DictController.java` | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/assembler/DictInterfaceAssembler.java` |
| Log | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/LogController.java` | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/assembler/LogInterfaceAssembler.java` |
| Menu | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/MenuController.java` | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/assembler/MenuInterfaceAssembler.java` |
| Role | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/RoleController.java` | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/assembler/RoleInterfaceAssembler.java` |
| User | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/UserController.java` | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/assembler/UserInterfaceAssembler.java` |

## Backend Caller And Contract Files

如果 controller action path 或 request/response shape 变化，必须同步以下测试和集成调用文件。

| 分组 | 文件 |
| --- | --- |
| System admin contract | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/admin/SystemAdminManagementContractTest.java` |
| System current user contract | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/admin/SystemAuthCurrentUserContractTest.java` |
| System log contract | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/admin/SystemLogContractTest.java` |
| System log service unit | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/admin/core/service/impl/SysLogMessageServiceImplTest.java` |
| System log AOP unit | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/admin/core/aop/SysLogMethodInterceptorTest.java` |
| Integration auth helper | `kuzhambu-servers/common/kuzhambu-common-test/src/main/java/com/thundax/kuzhambu/common/test/integration/IntegrationAuthClient.java` |

## Admin Web Sync Files

如果 System Core HTTP action path 改名，必须同步 admin-web service、contract test 和 E2E。

| 分组 | 文件 |
| --- | --- |
| CurrentUser | `kuzhambu-apps/admin-web/src/service/current-user-service.ts` |
| CurrentUser | `kuzhambu-apps/admin-web/src/service/current-user-types.ts` |
| Department | `kuzhambu-apps/admin-web/src/pages/system/department/department-service.ts` |
| Department | `kuzhambu-apps/admin-web/src/pages/system/department/department-service-contract.test.ts` |
| Dict | `kuzhambu-apps/admin-web/src/pages/system/dictionary/dictionary-service.ts` |
| Dict | `kuzhambu-apps/admin-web/src/pages/system/dictionary/dictionary-service-contract.test.ts` |
| Menu | `kuzhambu-apps/admin-web/src/pages/system/menu/menu-service.ts` |
| Menu | `kuzhambu-apps/admin-web/src/pages/system/menu/menu-service-contract.test.ts` |
| Role | `kuzhambu-apps/admin-web/src/pages/system/role/role-service.ts` |
| Role | `kuzhambu-apps/admin-web/src/pages/system/role/role-service-contract.test.ts` |
| User | `kuzhambu-apps/admin-web/src/pages/system/user/user-service.ts` |
| User | `kuzhambu-apps/admin-web/src/pages/system/user/user-service-contract.test.ts` |
| System Log | `kuzhambu-apps/admin-web/src/pages/system/system-log/system-log-service.ts` |
| System Log | `kuzhambu-apps/admin-web/src/pages/system/system-log/system-log-service-contract.test.ts` |
| E2E login legacy route check | `kuzhambu-apps/admin-web/e2e/auth/login/login.spec.ts` |
| E2E dictionary | `kuzhambu-apps/admin-web/e2e/system/dictionary/dictionary.spec.ts` |
| E2E logs | `kuzhambu-apps/admin-web/e2e/system/logs/logs.spec.ts` |
| E2E management | `kuzhambu-apps/admin-web/e2e/system/management/management.spec.ts` |

## Non-goals

- 不扩大到 System auth、audit 的常规 allowlist 清理；只处理 PR #232 明确遗留的两个 auth/audit 问题。
- 不修改共享 ArchUnit support，除非 audit 构造器引用问题证明现有规则存在可重复绕过且无法通过生产代码收口解决。
- 不处理 classics、knowledge、ai、operations、storage、workers、db seed 或 deploy 目录。

## Task Breakdown

执行任务必须保持以下顺序。每个任务控制在 2–12 个文件；如果实际执行时发现需要更多文件，先拆分任务再修改代码。

### Task 1：处理 GitHub PR #232 遗留问题（3 个文件）

| 文件 | 操作 |
| --- | --- |
| `docs/00-governance/HOW-TO-ADMIN-LOGIN-SMOKE.md` | 按 [discussion_r3746603229](https://github.com/thundax-lyp/kuzhambu/pull/232#discussion_r3746603229) 修复登录 smoke guide：把文档说明和 curl 命令中的 `/api/auth/session/pre-auth-session` 改为 `/api/auth/session/pre-auth-session/request`。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/AuditLogAspect.java` | 按 [discussion_r3746633916](https://github.com/thundax-lyp/kuzhambu/pull/232#discussion_r3746633916) 修复 audit command 构造边界：移除 `CreateAuditLogCommand::new` 构造器引用，不再通过本地 functional interface 在 runtime support 组件中构造 application command。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/SystemApplicationArchitectureTest.java` | 验证 `CreateAuditLogCommand` 构造移动后不会绕过 construction 规则；如果需要改规则或保留债务，只能在本文件中显式表达。 |

### Task 2：CurrentUser / Personal record 与构造边界（11 个文件）

| 文件 | 操作 |
| --- | --- |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/ChangeCurrentUserAvatarCommand.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/ChangeCurrentUserInfoCommand.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/ChangeCurrentUserPasswordCommand.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/RemoveCurrentUserAvatarCommand.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/CurrentUserAvatarQuery.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/CurrentUserQuery.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/CurrentUserController.java` | 移除直接构造 Command/Query；同步 action verb。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/assembler/PersonalInterfaceAssembler.java` | 承接 CurrentUser / Personal request 到 Command/Query 的转换。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/admin/SystemAuthCurrentUserContractTest.java` | 同步接口契约。 |
| `kuzhambu-apps/admin-web/src/service/current-user-service.ts` | 同步前端 current-user API 调用。 |
| `kuzhambu-apps/admin-web/src/service/current-user-types.ts` | 同步前端 current-user 请求/响应类型。 |

### Task 3：Department record 与构造边界（12 个文件）

| 文件 | 操作 |
| --- | --- |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/ChangeDepartmentInfoCommand.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/CreateDepartmentCommand.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/MoveDepartmentCommand.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/RemoveDepartmentCommand.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/DepartmentQuery.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/GetDepartmentQuery.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/DepartmentController.java` | 移除直接构造 Command/Query；同步 action verb。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/assembler/DepartmentInterfaceAssembler.java` | 承接 Department request 到 Command/Query 的转换。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/admin/SystemAdminManagementContractTest.java` | 同步 Department 相关契约。 |
| `kuzhambu-apps/admin-web/src/pages/system/department/department-service.ts` | 同步前端 Department API 调用。 |
| `kuzhambu-apps/admin-web/src/pages/system/department/department-service-contract.test.ts` | 同步前端 Department contract test。 |
| `kuzhambu-apps/admin-web/e2e/system/management/management.spec.ts` | 同步涉及 Department 的 E2E 调用路径。 |

### Task 4：Dict record 与构造边界（12 个文件）

| 文件 | 操作 |
| --- | --- |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/ChangeDictInfoCommand.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/CreateDictCommand.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/DictSortCommand.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/RemoveDictCommand.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/DictQuery.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/GetDictQuery.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/DictController.java` | 移除直接构造 Command/Query；同步 action verb。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/assembler/DictInterfaceAssembler.java` | 承接 Dict request 到 Command/Query 的转换。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/admin/SystemAdminManagementContractTest.java` | 同步 Dict 相关契约。 |
| `kuzhambu-apps/admin-web/src/pages/system/dictionary/dictionary-service.ts` | 同步前端 Dict API 调用。 |
| `kuzhambu-apps/admin-web/src/pages/system/dictionary/dictionary-service-contract.test.ts` | 同步前端 Dict contract test。 |
| `kuzhambu-apps/admin-web/e2e/system/dictionary/dictionary.spec.ts` | 同步 Dict E2E 调用路径。 |

### Task 5：Menu record 与构造边界（12 个文件）

| 文件 | 操作 |
| --- | --- |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/ChangeMenuInfoCommand.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/ChangeMenuVisibilityCommand.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/CreateMenuCommand.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/MoveMenuCommand.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/RemoveMenuCommand.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/GetMenuQuery.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/MenuQuery.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/MenuController.java` | 移除直接构造 Command/Query；同步 action verb。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/assembler/MenuInterfaceAssembler.java` | 承接 Menu request 到 Command/Query 的转换。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/admin/SystemAdminManagementContractTest.java` | 同步 Menu 相关契约。 |
| `kuzhambu-apps/admin-web/src/pages/system/menu/menu-service.ts` | 同步前端 Menu API 调用。 |
| `kuzhambu-apps/admin-web/src/pages/system/menu/menu-service-contract.test.ts` | 同步前端 Menu contract test。 |

### Task 6：Role record 与构造边界（12 个文件）

| 文件 | 操作 |
| --- | --- |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/AssignRoleUsersCommand.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/ChangeRoleInfoCommand.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/ChangeRoleStatusCommand.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/CreateRoleCommand.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/RemoveRoleCommand.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/RoleSortCommand.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/GetRoleQuery.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/RoleQuery.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/RoleController.java` | 移除直接构造 Command/Query；同步 action verb。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/assembler/RoleInterfaceAssembler.java` | 承接 Role request 到 Command/Query 的转换。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/admin/SystemAdminManagementContractTest.java` | 同步 Role 相关契约。 |
| `kuzhambu-apps/admin-web/src/pages/system/role/role-service.ts` | 同步前端 Role API 调用。 |

### Task 7：Role 前端契约补齐（2 个文件）

| 文件 | 操作 |
| --- | --- |
| `kuzhambu-apps/admin-web/src/pages/system/role/role-service-contract.test.ts` | 同步前端 Role contract test。 |
| `kuzhambu-apps/admin-web/e2e/system/management/management.spec.ts` | 同步涉及 Role 的 E2E 调用路径。 |

### Task 8：User record 与构造边界（11 个文件）

| 文件 | 操作 |
| --- | --- |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/ChangeUserInfoCommand.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/ChangeUserStatusCommand.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/CreateUserCommand.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/RemoveUserCommand.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/GetUserQuery.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/UserQuery.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/UserController.java` | 移除直接构造 Command/Query；同步 action verb。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/assembler/UserInterfaceAssembler.java` | 承接 User request 到 Command/Query 的转换。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/admin/SystemAdminManagementContractTest.java` | 同步 User 相关契约。 |
| `kuzhambu-apps/admin-web/src/pages/system/user/user-service.ts` | 同步前端 User API 调用。 |
| `kuzhambu-apps/admin-web/src/pages/system/user/user-service-contract.test.ts` | 同步前端 User contract test。 |

### Task 9：User E2E 与 Log record / 构造边界（12 个文件）

| 文件 | 操作 |
| --- | --- |
| `kuzhambu-apps/admin-web/e2e/system/management/management.spec.ts` | 同步涉及 User 的 E2E 调用路径。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/CreateLogCommand.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/DeleteLogCommand.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/GetLogQuery.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/LogQuery.java` | 转换为 record。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/LogController.java` | 移除直接构造 Command/Query；同步 action verb。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/service/impl/SysLogMessageServiceImpl.java` | 移除直接构造 `CreateLogCommand` / `DeleteLogCommand` / `LogQuery`。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/assembler/LogInterfaceAssembler.java` | 承接 Log request/service 到 Command/Query 的转换。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/admin/SystemLogContractTest.java` | 同步 Log 相关契约。 |
| `kuzhambu-apps/admin-web/src/pages/system/system-log/system-log-service.ts` | 同步前端 System Log API 调用。 |
| `kuzhambu-apps/admin-web/src/pages/system/system-log/system-log-service-contract.test.ts` | 同步前端 System Log contract test。 |
| `kuzhambu-apps/admin-web/e2e/system/logs/logs.spec.ts` | 同步 Log E2E 调用路径。 |

### Task 10：统一删除 allowlist 与最终契约收口（7 个文件）

| 文件 | 操作 |
| --- | --- |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/SystemApplicationCommandQueryRecordAllowances.java` | 删除已清理的 `application.core` record allowlist。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/SystemApplicationArchitectureTest.java` | 删除已清理的 System Core construction allowlist。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/SystemInterfaceArchitectureTest.java` | 删除已清理的 action verb 与 assembler nullness allowlist。 |
| `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/admin/core/aop/SysLogMethodInterceptorTest.java` | 确认日志 AOP 行为未因 Log command 构造移动而回退。 |
| `kuzhambu-servers/common/kuzhambu-common-test/src/main/java/com/thundax/kuzhambu/common/test/integration/IntegrationAuthClient.java` | 如 pre-auth 路由或 current-user 调用受影响，同步集成 helper。 |
| `kuzhambu-apps/admin-web/e2e/auth/login/login.spec.ts` | 如登录 smoke 路由变化影响 E2E，更新前端登录 E2E。 |
| `TODO.md` | 完成后清空任务面板；不保留完成历史。 |

## Verification

每个任务项完成后运行最窄验证；最终收口运行以下命令。

### Backend

在 `kuzhambu-servers/` 下运行：

```sh
mvn -pl biz/system/kuzhambu-system-application,biz/system/kuzhambu-system-interface -am spotless:apply
mvn -pl biz/system/kuzhambu-system-application,biz/system/kuzhambu-system-interface -am spotless:check
mvn -pl biz/system/kuzhambu-system-application,biz/system/kuzhambu-system-interface -am checkstyle:check
mvn -pl biz/system/kuzhambu-system-application,biz/system/kuzhambu-system-interface -am test
```

### Admin Web

如果修改 admin-web 文件，在 `kuzhambu-apps/` 下运行：

```sh
pnpm --filter ./admin-web run format:check
pnpm --filter ./admin-web run lint
pnpm --filter ./admin-web run test
```

### Repository

在仓库根目录运行：

```sh
git diff --check
```

## Closure

- `SystemApplicationCommandQueryRecordAllowances.java` 不再包含 `application.core` 条目。
- `SystemApplicationArchitectureTest.java` 不再包含 System Core controller/service 的 `COMMAND_QUERY_CONSTRUCTION:*` 条目。
- `SystemInterfaceArchitectureTest.java` 不再包含 CurrentUser、Department、Menu、Role、User controller action verb allowlist，也不再包含已修复 assembler 的 nullness allowlist。
- PR #232 两个遗留问题已完成，并在 PR 描述中说明处理结果。
- `TODO.md` 清空后删除本 RUNBOOK。
