# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 当前任务项按数字编号顺序执行；不得跳过前置契约任务直接做后续调用方或重命名任务。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `14-core-log-contract`：[14] 强类型化 Log get/delete 契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SYSTEM-APPLICATION-TYPED-CONTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/GetLogQuery.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/DeleteLogCommand.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/service/LogApplicationService.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/service/impl/LogApplicationServiceImpl.java`、`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/LogController.java`、`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/service/impl/SysLogMessageServiceImpl.java`
    - 处理动作：新增 `GetLogQuery.id` 和 `DeleteLogCommand.query`，替换 `get(LogId)` 与 `deleteByCondition(LogQuery)`。
    - 验收点：Log application service 的 get/delete 均使用 Query/Command，日志查询和清理 HTTP 契约不变。
    - 重要度：8/10

- [ ] `15-create-log-user-id`：[15] 强类型化 CreateLogCommand.userId 字段
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SYSTEM-APPLICATION-TYPED-CONTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/CreateLogCommand.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/service/LogApplicationService.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/service/impl/LogApplicationServiceImpl.java`、`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/service/impl/SysLogMessageServiceImpl.java`
    - 处理动作：将 `CreateLogCommand.userId` 从 `String` 改为 `UserId`，把字符串到值对象转换移动到 interface 层。
    - 验收点：`LogApplicationServiceImpl.toLog(...)` 不再调用 `UserIdCodec.toDomain(command.getUserId())`。
    - 重要度：8/10

- [ ] `16-current-user-avatar-query`：[16] 强类型化当前用户头像查询参数
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SYSTEM-APPLICATION-TYPED-CONTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/CurrentUserAvatarQuery.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/service/CurrentUserApplicationService.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/service/impl/CurrentUserApplicationServiceImpl.java`、`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/CurrentUserController.java`
    - 处理动作：新增 `CurrentUserAvatarQuery.userId`，替换头像读取、流读取和存在性检查中的 `UserId` 直接参数。
    - 验收点：头像上传、预览、删除接口仍可用，application service 头像读取入口只接收 Query。
    - 重要度：8/10

- [ ] `17-audit-object-ref`：[17] 强类型化审计对象和操作人引用
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SYSTEM-APPLICATION-TYPED-CONTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/command/CreateAuditLogCommand.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/query/AuditLogQuery.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/query/AuditMetaQuery.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/query/GetAuditLogQuery.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/service/AuditApplicationService.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/service/impl/AuditApplicationServiceImpl.java`
    - 处理动作：将审计 Command/Query 的 `objectType/objectId/operatorType/operatorId/operatorName` 收敛为 `AuditObjectRef` 和 `AuditOperatorRef`，并用 `GetAuditLogQuery.id` 替换 `getLog(AuditLogId)`。
    - 验收点：审计 application 契约不再暴露裸对象引用字段，审计日志分页和详情查询结果不变。
    - 重要度：8/10

- [ ] `18-audit-callers`：[18] 同步审计调用方使用强类型查询对象
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SYSTEM-APPLICATION-TYPED-CONTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/AuditLogAspect.java`、`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/controller/AuditController.java`、`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/assembler/AuditInterfaceAssembler.java`
    - 处理动作：同步审计切面、控制器和 assembler 构造 `AuditObjectRef`、`AuditOperatorRef`、`GetAuditLogQuery`。
    - 验收点：审计记录、审计分页筛选、审计详情抽屉对应 HTTP 契约不变。
    - 重要度：8/10

- [ ] `19-audit-loaders`：[19] 同步审计对象加载器使用 Get*Query
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SYSTEM-APPLICATION-TYPED-CONTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/DepartmentAuditObjectLoader.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/DictAuditObjectLoader.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/MenuAuditObjectLoader.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/RoleAuditObjectLoader.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/UserAuditObjectLoader.java`
    - 处理动作：将各审计 loader 中的 `*Service.get(*Id)` 调用改为 `*Service.get(Get*Query)`。
    - 验收点：五个审计 loader 均不再直接调用 `get(*Id)`，审计快照加载仍返回对应领域对象。
    - 重要度：7/10

- [ ] `20-service-rename-auth-audit`：[20] 业务化重命名 auth/audit ApplicationService
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SYSTEM-APPLICATION-TYPED-CONTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/service/AuditApplicationService.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/service/impl/AuditApplicationServiceImpl.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/service/AdminTokenApplicationService.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/service/impl/AdminTokenApplicationServiceImpl.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/service/PermissionApplicationService.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/service/impl/PermissionApplicationServiceImpl.java`
    - 处理动作：将 `AuditApplicationService`、`AdminTokenApplicationService`、`PermissionApplicationService` 分别重命名为业务化接口和实现类。
    - 验收点：类名、文件名、实现类名、import 和 Spring 注入类型一致，编译通过。
    - 重要度：7/10

- [ ] `21-service-rename-core`：[21] 业务化重命名 core 管理 ApplicationService
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SYSTEM-APPLICATION-TYPED-CONTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/service/UserApplicationService.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/service/impl/UserApplicationServiceImpl.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/service/RoleApplicationService.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/service/impl/RoleApplicationServiceImpl.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/service/MenuApplicationService.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/service/impl/MenuApplicationServiceImpl.java`
    - 处理动作：将 User、Role、Menu 管理服务接口和实现类重命名为业务化名称。
    - 验收点：类名、文件名、实现类名、import 和 Spring 注入类型一致，编译通过。
    - 重要度：7/10

- [ ] `22-service-rename-core-secondary`：[22] 业务化重命名剩余 core ApplicationService
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SYSTEM-APPLICATION-TYPED-CONTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/service/DepartmentApplicationService.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/service/impl/DepartmentApplicationServiceImpl.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/service/DictApplicationService.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/service/impl/DictApplicationServiceImpl.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/service/LogApplicationService.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/service/impl/LogApplicationServiceImpl.java`
    - 处理动作：将 Department、Dict、Log 管理服务接口和实现类重命名为业务化名称。
    - 验收点：类名、文件名、实现类名、import 和 Spring 注入类型一致，编译通过。
    - 重要度：7/10

- [ ] `23-service-rename-profile-authentication`：[23] 业务化重命名当前用户和认证 ApplicationService
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SYSTEM-APPLICATION-TYPED-CONTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/service/CurrentUserApplicationService.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/service/impl/CurrentUserApplicationServiceImpl.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/service/PrincipalAuthApplicationService.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/service/impl/PrincipalAuthApplicationServiceImpl.java`
    - 处理动作：将 CurrentUser 和 PrincipalAuth 应用服务接口及实现类重命名为业务化名称。
    - 验收点：类名、文件名、实现类名、import 和 Spring 注入类型一致，编译通过。
    - 重要度：7/10

- [ ] `24-frontend-contract-check`：[24] 验证前端契约和关键控件操作
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SYSTEM-APPLICATION-TYPED-CONTRACT.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/auth/auth-service-contract.test.ts`、`kuzhambu-apps/admin-web/src/pages/system/common/system-service-contract.test.ts`、`kuzhambu-apps/admin-web/src/pages/system/common/log-service-contract.test.ts`、`kuzhambu-apps/admin-web/src/pages/system/user/components/user-edit-drawer/user-edit-drawer.test.tsx`、`kuzhambu-apps/admin-web/e2e/auth/login/login.spec.ts`、`kuzhambu-apps/admin-web/e2e/system/dictionary/dictionary.spec.ts`
    - 处理动作：验证登录、权限、用户编辑、字典和日志相关前端契约测试与关键控件操作。
    - 验收点：登录输入框/验证码/登录按钮、权限存储、用户角色多选、字典类型筛选、日志筛选和清理按钮均符合 RUNBOOK 的前端验收描述。
    - 重要度：7/10

- [ ] `25-system-contract-validation`：[25] 运行 system 契约强类型化后端验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SYSTEM-APPLICATION-TYPED-CONTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-application`、`kuzhambu-servers/biz/system/kuzhambu-system-interface`、`kuzhambu-servers/biz/system`
    - 处理动作：运行 RUNBOOK 规定的 Maven 窄测试、编译、格式检查、checkstyle 和最终 system 测试。
    - 验收点：`mvn -pl biz/system/kuzhambu-system-application -Dtest='*ApplicationServiceImplTest,SystemApplicationArchitectureTest' test`、`mvn -pl biz/system/kuzhambu-system-interface -am -DskipTests compile`、`mvn spotless:check`、`mvn checkstyle:check`、`mvn -pl biz/system -am test` 均通过或有明确记录。
    - 重要度：9/10

- [ ] `26-runbook-closure`：[26] 收口并清理强类型契约 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-SYSTEM-APPLICATION-TYPED-CONTRACT.md`
    - 范围对象：`docs/30-designs/RUNBOOK-SYSTEM-APPLICATION-TYPED-CONTRACT.md`、`TODO.md`
    - 处理动作：完成实现和验证后删除临时 RUNBOOK，并按剩余工作收窄或清空 TODO。
    - 验收点：PR 收口前 RUNBOOK 已删除或长期价值内容已迁移到治理/readiness 文档，`TODO.md` 不保留已完成任务。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
