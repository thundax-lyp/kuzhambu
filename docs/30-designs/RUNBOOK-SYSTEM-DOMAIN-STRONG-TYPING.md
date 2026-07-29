# System Domain Strong Typing Runbook

## Purpose

本 RUNBOOK 用于执行 system domain 强类型化：

- `com.thundax.kuzhambu.system.domain.*.model.entity.*`：删除或收敛仍用基础类型表达的领域标识字段。
- `com.thundax.kuzhambu.system.domain.*.repository`：把 repository 端口中的业务对象 ID、树节点 ID、关系 ID 从 `Long` / `String` 改为已有强类型值对象。
- `kuzhambu-apps/admin-web`：本次后端强类型化不改变 HTTP 协议，但必须按控件和操作完成前端回归验证。

核心边界：`domain` 和 `domain.repository` 使用强类型；`interfaces`、HTTP Request / Response、admin-web TypeScript 类型、infra DO、Mapper 查询参数可继续使用协议或持久化所需基础类型。application 调用本域 repository 时不得再手动执行 `*IdCodec.toValue(...)` / `toValues(...)`。

## Scope

后端纳入本次闭环：

- `kuzhambu-servers/biz/system/kuzhambu-system-domain`
- `kuzhambu-servers/biz/system/kuzhambu-system-application`
- `kuzhambu-servers/biz/system/kuzhambu-system-infra`
- `kuzhambu-servers/biz/system/kuzhambu-system-interface` 中直接构造 system domain entity 的调用点和测试桩

前端纳入回归验证：

- `kuzhambu-apps/admin-web/src/pages/system/role`
- `kuzhambu-apps/admin-web/src/pages/system/user`
- `kuzhambu-apps/admin-web/src/pages/system/menu`
- `kuzhambu-apps/admin-web/src/pages/system/department`
- `kuzhambu-apps/admin-web/src/pages/audit/audit-log`

## Confirmed Decisions

- `PrincipalKey.principalId` 不纳入本轮改造；跨 principal 类型建模后续单独设计。
- `AuditObjectRef.objectId` 和 `AuditOperatorRef.operatorId` 字段继续保留 `String`。
- `LogRepository.listByIds(List<String>)` 属于历史不一致签名，本轮改为 `List<LogId>`。
- `Role.menuIdList` 不再作为 `Role` entity 字段表达；角色-菜单关联只由 repository 关系方法表达。
- application 层不再为调用本域 repository 执行 `*IdCodec.toValue(...)` / `toValues(...)`；基础类型转换下沉到 infra repository impl。
- admin-web 协议不改：前端仍通过 string ID、`menus: [{ id: string }]`、`roleIds: string[]`、`parentId: string | null` 等字段与 HTTP API 交互。

## Non-goals

- 不调整数据库字段类型、表结构、Mapper XML 或 DO 字段。
- 不把 HTTP Request / Response、application Command / Query / Result、admin-web TypeScript 协议类型统一改为 Java value object。
- 不把手机号、邮箱、登录名、字典 `type` / `label` / `value`、权限码、访问 token 原文、审计 `source`、`requestId`、`traceId`、`remoteAddr` 等检索值包装成 ID。
- 不迁移 legacy `BaseLongId` 到 ULID 字符串。
- 不修复与本次强类型化无关的 repository 方法命名白名单问题。

## Data Structure Changes

### Domain Entity Fields

| File | Type | Field | Current type | Target type | Operation |
| --- | --- | --- | --- | --- | --- |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/model/entity/Role.java` | `Role` | `menuIdList` | `List<Long>` | none | 删除字段。 |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/model/entity/Role.java` | `Role` | `getMenuIdList()` | `List<Long>` | none | 删除手写 getter。 |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/model/entity/Role.java` | `Role` | `setMenuIdList(List<Long> menuIdList)` | method | none | 删除手写 setter。 |

以下字段已是强类型，本轮只验证调用链不泄漏基础 ID：

| File | Type | Field | Current type | Action |
| --- | --- | --- | --- | --- |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/model/entity/User.java` | `User` | `id` | `UserId` | 保持。 |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/model/entity/User.java` | `User` | `departmentId` | `DepartmentId` | 保持。 |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/model/entity/User.java` | `User` | `rank` | `AccessRank` | 保持。 |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/model/entity/Menu.java` | `Menu` | `id` | `MenuId` | 保持。 |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/model/entity/Menu.java` | `Menu` | `parentId` | `MenuId` | 保持。 |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/model/entity/Menu.java` | `Menu` | `rank` | `AccessRank` | 保持。 |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/model/entity/Department.java` | `Department` | `id` | `DepartmentId` | 保持。 |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/model/entity/Department.java` | `Department` | `parentId` | `DepartmentId` | 保持。 |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/model/entity/Dict.java` | `Dict` | `id` | `DictId` | 保持。 |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/model/entity/Log.java` | `Log` | `id` | `LogId` | 保持。 |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/model/entity/Log.java` | `Log` | `userId` | `UserId` | 保持。 |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/model/entity/AuditMeta.java` | `AuditMeta` | `objectRef` | `AuditObjectRef` | 保持；其内部 `objectId` 继续是 `String`。 |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/model/entity/AuditMeta.java` | `AuditMeta` | `lastOperatorRef` | `AuditOperatorRef` | 保持；其内部 `operatorId` 继续是 `String`。 |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/model/entity/AuditLog.java` | `AuditLog` | `objectRef` | `AuditObjectRef` | 保持；其内部 `objectId` 继续是 `String`。 |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/model/entity/AuditLog.java` | `AuditLog` | `operatorRef` | `AuditOperatorRef` | 保持；其内部 `operatorId` 继续是 `String`。 |

### Repository Contract Fields

| File | Method | Current ID fields | Target ID fields |
| --- | --- | --- | --- |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/UserRepository.java` | `listByIds` | `List<Long> idList` | `List<UserId> idList` |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/UserRepository.java` | `list` | `Long departmentId` | `DepartmentId departmentId` |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/UserRepository.java` | `page` | `Long departmentId` | `DepartmentId departmentId` |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/UserRepository.java` | `listUserRoles` | `Long userId`; return `List<Long>` | `UserId userId`; return `List<RoleId>` |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/UserRepository.java` | `deleteUserRole` | `Long userId` | `UserId userId` |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/UserRepository.java` | `insertUserRole` | `Long userId`, `List<Long> roleIdList` | `UserId userId`, `List<RoleId> roleIdList` |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/RoleRepository.java` | `listByIds` | `List<Long> idList` | `List<RoleId> idList` |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/RoleRepository.java` | `listRoleMenus` | `Long roleId`; return `List<Long>` | `RoleId roleId`; return `List<MenuId>` |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/RoleRepository.java` | `deleteRoleMenu` | `Long roleId` | `RoleId roleId` |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/RoleRepository.java` | `insertRoleMenu` | `Long roleId`, `List<Long> menuIdList` | `RoleId roleId`, `List<MenuId> menuIdList` |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/RoleRepository.java` | `listRoleUsers` | `Long roleId`; return `List<Long>` | `RoleId roleId`; return `List<UserId>` |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/RoleRepository.java` | `deleteRoleUser` | `Long roleId` | `RoleId roleId` |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/RoleRepository.java` | `insertRoleUser` | `Long roleId`, `List<Long> userIdList` | `RoleId roleId`, `List<UserId> userIdList` |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/MenuRepository.java` | `listByIds` | `List<Long> idList` | `List<MenuId> idList` |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/MenuRepository.java` | `list` | `Long parentId` | `MenuId parentId` |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/MenuRepository.java` | `page` | `Long parentId` | `MenuId parentId` |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/MenuRepository.java` | `moveTreeNode` | `Long fromId`, `Long toId` | `MenuId fromId`, `MenuId toId` |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/MenuRepository.java` | `isChildOf` | `Long childId`, `Long parentId` | `MenuId childId`, `MenuId parentId` |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/MenuRepository.java` | `deleteMenuRole` | `Long menuId` | `MenuId menuId` |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/DepartmentRepository.java` | `listByIds` | `List<Long> idList` | `List<DepartmentId> idList` |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/DepartmentRepository.java` | `list` | `Long parentId` | `DepartmentId parentId` |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/DepartmentRepository.java` | `page` | `Long parentId` | `DepartmentId parentId` |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/DepartmentRepository.java` | `moveTreeNode` | `Long fromId`, `Long toId` | `DepartmentId fromId`, `DepartmentId toId` |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/DepartmentRepository.java` | `isChildOf` | `Long childId`, `Long parentId` | `DepartmentId childId`, `DepartmentId parentId` |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/DictRepository.java` | `listByIds` | `List<Long> idList` | `List<DictId> idList` |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/LogRepository.java` | `listByIds` | `List<String> idList` | `List<LogId> idList` |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/repository/AuditLogRepository.java` | `listByObject` | `String objectType`, `String objectId` | `AuditObjectRef objectRef` |
| `kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/repository/AuditLogRepository.java` | `page` | `String objectType`, `String objectId`, `AuditOperatorType operatorType`, `String operatorId` | `AuditObjectRef objectRef`, `AuditOperatorRef operatorRef` |

## Frontend Impact

前端不做数据结构迁移。后端 application/interface 仍负责把 Java 强类型值对象转换成 HTTP 字符串 ID，因此 admin-web TypeScript 类型保持不变。

### Frontend Types And API Payloads To Preserve

| File | Field | Type to preserve |
| --- | --- | --- |
| `kuzhambu-apps/admin-web/src/pages/system/role/role-types.ts` | `RoleMenuNode.id` | `string` |
| `kuzhambu-apps/admin-web/src/pages/system/role/role-types.ts` | `RoleMenuNode.parentId` | `string | null | undefined` |
| `kuzhambu-apps/admin-web/src/pages/system/role/role-types.ts` | `RoleRecord.id` | `string` |
| `kuzhambu-apps/admin-web/src/pages/system/role/role-types.ts` | `RoleRecord.menus` | `RoleMenuNode[] | null | undefined` |
| `kuzhambu-apps/admin-web/src/pages/system/role/role-service.ts` | `RoleMenuCommand.id` | `string` |
| `kuzhambu-apps/admin-web/src/pages/system/role/role-service.ts` | `RoleSaveCommand.menus` | `RoleMenuCommand[] | null | undefined` |
| `kuzhambu-apps/admin-web/src/pages/system/role/role-service.ts` | `RoleSortCommand.orderedIds` | `string[]` |
| `kuzhambu-apps/admin-web/src/pages/system/user/components/user-form-values.ts` | `departmentId` | `string | null` |
| `kuzhambu-apps/admin-web/src/pages/system/user/components/user-form-values.ts` | `roleIds` | `string[]` |
| `kuzhambu-apps/admin-web/src/pages/system/menu/menu-service.ts` | `MenuSaveCommand.parentId` | `string | null | undefined` |
| `kuzhambu-apps/admin-web/src/pages/system/menu/menu-service.ts` | `MenuMoveCommand.fromNodeId` | `string` |
| `kuzhambu-apps/admin-web/src/pages/system/menu/menu-service.ts` | `MenuMoveCommand.toNodeId` | `string` |
| `kuzhambu-apps/admin-web/src/pages/system/department/department-service.ts` | `DepartmentSaveCommand.parentId` | `string | null | undefined` |
| `kuzhambu-apps/admin-web/src/pages/system/department/department-service.ts` | `DepartmentMoveCommand.fromNodeId` | `string` |
| `kuzhambu-apps/admin-web/src/pages/system/department/department-service.ts` | `DepartmentMoveCommand.toNodeId` | `string` |
| `kuzhambu-apps/admin-web/src/pages/audit/audit-log/components/audit-log-filter.tsx` | `objectId` / `operatorId` | `string` |

### Frontend Controls And Operations To Regress

| Page | File | Control | User operation | Expected payload / behavior |
| --- | --- | --- | --- | --- |
| 角色管理 | `kuzhambu-apps/admin-web/src/pages/system/role/components/role-edit-drawer/role-edit-drawer.tsx` | `Input` label `角色名称` | 输入名称后点击 drawer footer `保存` | `RoleSaveCommand.name` 为字符串；不影响 `menus`。 |
| 角色管理 | `kuzhambu-apps/admin-web/src/pages/system/role/components/role-edit-drawer/role-edit-drawer.tsx` | `KuzhambuSwitch` label `管理权限` | 切换管理员/普通角色后保存 | `RoleSaveCommand.admin` 为 boolean。 |
| 角色管理 | `kuzhambu-apps/admin-web/src/pages/system/role/components/role-edit-drawer/role-edit-drawer.tsx` | `KuzhambuSwitch` label `角色状态` | 切换启用/禁用后保存 | `RoleSaveCommand.enable` 为 boolean。 |
| 角色管理 | `kuzhambu-apps/admin-web/src/pages/system/role/components/menu-tree-field/menu-tree-field.tsx` | AntD `Tree checkable` under label `菜单权限` | 勾选/取消勾选菜单节点后保存 | `RoleSaveCommand.menus` 保持 `[{ id: string }]`。 |
| 用户管理 | `kuzhambu-apps/admin-web/src/pages/system/user/components/user-edit-drawer/user-edit-drawer.tsx` | `TreeSelect` label `部门` | 选择部门后保存 | `UserFormValues.departmentId` 保持 string ID 或 null。 |
| 用户管理 | `kuzhambu-apps/admin-web/src/pages/system/user/components/user-edit-drawer/user-edit-drawer.tsx` | `KuzhambuSelect mode="multiple"` label `角色` | 多选/取消角色后保存 | `UserFormValues.roleIds` 保持 `string[]`。 |
| 菜单管理 | `kuzhambu-apps/admin-web/src/pages/system/menu/menu-page.tsx` | `新增` button | 打开新增菜单 drawer 并保存 | `parentId` 保持 string 或 null。 |
| 菜单管理 | `kuzhambu-apps/admin-web/src/pages/system/menu/menu-page.tsx` | 行级编辑 button | 修改菜单父级后保存 | 不允许选择自身和后代作为父级。 |
| 菜单管理 | `kuzhambu-apps/admin-web/src/pages/system/menu/menu-page.tsx` | 行级显示开关 | 切换显示状态 | 只提交当前菜单 string ID 和显示状态。 |
| 菜单管理 | `kuzhambu-apps/admin-web/src/pages/system/menu/menu-page.tsx` | 行级升/降级或排序操作 | 移动菜单节点 | `MenuMoveCommand.fromNodeId` / `toNodeId` 保持 string ID。 |
| 部门管理 | `kuzhambu-apps/admin-web/src/pages/system/department/department-page.tsx` | `新增` / 行级编辑 button | 打开部门 drawer，选择父部门后保存 | `parentId` 保持 string 或 null。 |
| 部门管理 | `kuzhambu-apps/admin-web/src/pages/system/department/department-page.tsx` | 部门树移动操作 | 移动部门节点 | move command ID 保持 string。 |
| 审计日志 | `kuzhambu-apps/admin-web/src/pages/audit/audit-log/components/audit-log-filter.tsx` | `KuzhambuSelect` label `对象类型` | 选择对象类型后点击查询 | 后端 application 组装 `AuditObjectRef`；前端仍传 string。 |
| 审计日志 | `kuzhambu-apps/admin-web/src/pages/audit/audit-log/components/audit-log-filter.tsx` | `Input` label `对象 ID` | 输入对象 ID 后点击查询 | `objectId` 仍为 string。 |
| 审计日志 | `kuzhambu-apps/admin-web/src/pages/audit/audit-log/components/audit-log-filter.tsx` | `KuzhambuSelect` label `操作者类型` | 选择操作者类型后点击查询 | 后端 application 组装 `AuditOperatorRef`；前端仍传 string。 |
| 审计日志 | `kuzhambu-apps/admin-web/src/pages/audit/audit-log/components/audit-log-filter.tsx` | `Input` label `操作者 ID` | 输入操作者 ID 后点击查询 | `operatorId` 仍为 string。 |

## Plan

按 2-5 个文件一个小任务执行。每个小任务完成后先运行最窄编译或测试暴露调用点，再进入下一批。

### Task 1: Role Entity And Role Command Boundary

文件：

- `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/model/entity/Role.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/assembler/RoleInterfaceAssembler.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/service/impl/RoleApplicationServiceImpl.java`

操作：

- 删除 `Role.menuIdList`、`getMenuIdList()`、`setMenuIdList(...)`。
- `RoleInterfaceAssembler` 不再向 `Role` entity 设置菜单列表；只把菜单 ID 放进 command。
- `RoleApplicationServiceImpl` 创建/修改角色后直接用 command 的 `List<MenuId>` 调 `RoleRepository.insertRoleMenu(...)`。

完成标准：

- 生产代码不再出现 `role.getMenuIdList()` 或 `role.setMenuIdList(...)`。
- `Role` 类级 Lombok 注解仍只有 `@Getter`、`@Setter`、`@NoArgsConstructor`、`@AllArgsConstructor`。

### Task 2: User And Role Repository Contracts

文件：

- `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/UserRepository.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/RoleRepository.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/core/repository/impl/UserRepositoryImpl.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/core/repository/impl/RoleRepositoryImpl.java`

操作：

- 按 `Repository Contract Fields` 表修改 `UserRepository` 和 `RoleRepository` 签名。
- impl 方法签名同步。
- impl 内部靠近 mapper/cache 处用 `UserIdCodec`、`RoleIdCodec`、`MenuIdCodec`、`DepartmentIdCodec` 转基础类型。
- 关联 ID 查询返回前转成 `List<RoleId>`、`List<MenuId>`、`List<UserId>`。

完成标准：

- `UserRepository`、`RoleRepository` 不再用 `Long` 表达已有业务 ID。

### Task 3: Menu Repository Contract

文件：

- `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/MenuRepository.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/core/repository/impl/MenuRepositoryImpl.java`

操作：

- `MenuRepository` 的 `idList`、`parentId`、`fromId`、`toId`、`childId`、`menuId` 改为 `MenuId` 或 `List<MenuId>`。
- impl 内部靠近 mapper/cache/tree helper 处使用 `MenuIdCodec.toValue(...)` / `toValues(...)`。

完成标准：

- `MenuRepository` 不再暴露 `Long` 菜单 ID。
- 菜单树移动、祖先判断、删除菜单角色关系的 null 语义与旧实现一致。

### Task 4: Department Repository Contract

文件：

- `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/DepartmentRepository.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/core/repository/impl/DepartmentRepositoryImpl.java`

操作：

- `DepartmentRepository` 的 `idList`、`parentId`、`fromId`、`toId`、`childId` 改为 `DepartmentId` 或 `List<DepartmentId>`。
- impl 内部靠近 mapper/cache/tree helper 处使用 `DepartmentIdCodec.toValue(...)` / `toValues(...)`。

完成标准：

- `DepartmentRepository` 不再暴露 `Long` 部门 ID。
- 部门树移动和祖先判断的 null/root 语义与旧实现一致。

### Task 5: Dict And Log Repository Contracts

文件：

- `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/DictRepository.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/LogRepository.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/core/repository/impl/DictRepositoryImpl.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/core/repository/impl/LogRepositoryImpl.java`

操作：

- `DictRepository.listByIds(List<Long>)` 改为 `List<DictId>`。
- `LogRepository.listByIds(List<String>)` 改为 `List<LogId>`。
- impl 内部用 `DictIdCodec.toValues(...)`、`LogIdCodec.toValues(...)` 转 mapper 所需基础类型。

完成标准：

- core repository 中 `listByIds(...)` 全部接收对应 `List<*Id>`。

### Task 6: Core Application Call Sites

文件：

- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/service/impl/UserApplicationServiceImpl.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/service/impl/RoleApplicationServiceImpl.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/service/impl/MenuApplicationServiceImpl.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/service/impl/DepartmentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/service/impl/DictApplicationServiceImpl.java`

操作：

- 删除仅为调用本域 repository 服务的 `*IdCodec.toValue(...)` / `toValues(...)`。
- `UserApplicationServiceImpl.listUserRoles(...)` 直接处理 `List<RoleId>`。
- `RoleApplicationServiceImpl.listRoleUsers(...)` 直接处理 `List<UserId>`；`listRoleMenus(...)` 直接处理 `List<MenuId>`。
- `MenuApplicationServiceImpl` 和 `DepartmentApplicationServiceImpl` 的树节点移动、祖先判断直接传强类型 ID。
- `DictApplicationServiceImpl` 的 `listByIds(...)` 直接传 `List<DictId>`。

完成标准：

- 这 5 个 application service 不再为了调用本域 repository 把本域 ID 转基础类型。

### Task 7: Log Application And Core Tests

文件：

- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/service/impl/LogApplicationServiceImpl.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/core/service/impl/MenuApplicationServiceImplTest.java`

操作：

- 如 `LogApplicationServiceImpl` 存在 `listByIds(...)` 调用，改为传 `List<LogId>`。
- 更新 `MenuApplicationServiceImplTest` 中 `MenuRepository` stub 签名和断言。

完成标准：

- core application 编译不再出现 repository 签名不匹配。

### Task 8: Audit Repository Ref Contract

文件：

- `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/repository/AuditLogRepository.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/audit/repository/impl/AuditLogRepositoryImpl.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/service/impl/AuditApplicationServiceImpl.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/audit/service/impl/AuditApplicationServiceImplTest.java`

操作：

- `AuditLogRepository.listByObject(...)` 改为接收 `AuditObjectRef`。
- `AuditLogRepository.page(...)` 改为接收 `AuditObjectRef` 和 `AuditOperatorRef`。
- `AuditApplicationServiceImpl` 从 query 字段装配 Ref。
- `AuditLogRepositoryImpl` 从 Ref 提取 `objectType`、`objectId`、`operatorType`、`operatorId` 后调用 mapper。
- 更新 audit application test 的 mock 参数。

完成标准：

- `AuditLogRepository` 不再暴露分散的对象 ID 和操作者 ID 字符串参数。
- `AuditObjectRef.objectId`、`AuditOperatorRef.operatorId` 内部仍为 `String`。

### Task 9: Frontend Role And User Regression

文件：

- `kuzhambu-apps/admin-web/src/pages/system/role/role-types.ts`
- `kuzhambu-apps/admin-web/src/pages/system/role/role-service.ts`
- `kuzhambu-apps/admin-web/src/pages/system/role/components/role-edit-drawer/role-edit-drawer.tsx`
- `kuzhambu-apps/admin-web/src/pages/system/role/components/menu-tree-field/menu-tree-field.tsx`
- `kuzhambu-apps/admin-web/src/pages/system/user/components/user-edit-drawer/user-edit-drawer.tsx`

操作：

- 不修改上述 TypeScript 类型，确认仍使用 string ID。
- 回归角色 drawer：点击 `新增角色` / 行级 `编辑`，输入 `角色名称`，切换 `管理权限`、`角色状态`，勾选 `菜单权限` Tree 节点，点击 `保存`。
- 回归用户 drawer：点击 `新增用户` / 行级 `编辑`，在 `部门` TreeSelect 选择部门，在 `角色` multiple select 选择多个角色，点击 `保存`。

完成标准：

- 角色保存请求中的 `menus` 仍是 `[{ id: string }]`。
- 用户保存请求中的 `departmentId` 仍是 string 或 null，`roleIds` 仍是 `string[]`。

### Task 10: Frontend Menu, Department, And Audit Regression

文件：

- `kuzhambu-apps/admin-web/src/pages/system/menu/menu-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/system/menu/components/menu-edit-drawer.tsx`
- `kuzhambu-apps/admin-web/src/pages/system/department/department-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/system/department/components/department-edit-drawer.tsx`
- `kuzhambu-apps/admin-web/src/pages/audit/audit-log/components/audit-log-filter.tsx`

操作：

- 回归菜单：点击 `新增` 打开 drawer，选择父菜单并保存；点击行级编辑修改父菜单；使用行级显示开关；执行升/降级或排序移动。
- 回归部门：点击 `新增` / 行级编辑打开 drawer，选择父部门并保存；执行部门树移动。
- 回归审计日志：打开筛选面板，选择 `对象类型`、填写 `对象 ID`、选择 `操作者类型`、填写 `操作者 ID`、点击 `查询`。

完成标准：

- 菜单新增/编辑/移动请求中的菜单 ID 和 `parentId` 仍为 string 或 null。
- 部门新增/编辑/移动请求中的部门 ID 和 `parentId` 仍为 string 或 null。
- 审计筛选请求中的 `objectId`、`operatorId` 仍为 string。

## Verification

后端在 `kuzhambu-servers/` 下执行：

```sh
mvn -pl biz/system/kuzhambu-system-domain,biz/system/kuzhambu-system-application,biz/system/kuzhambu-system-infra,biz/system/kuzhambu-system-interface -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/system/kuzhambu-system-domain,biz/system/kuzhambu-system-application,biz/system/kuzhambu-system-infra,biz/system/kuzhambu-system-interface -am test
```

后端补充扫描：

```sh
rg "List<Long>|List<String>|Long .*Id|String .*Id" \
  biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/*/model/entity \
  biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/*/repository

rg "Codec\\.toValue|Codec\\.toValues" \
  biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application
```

前端在 `kuzhambu-apps/` 下执行：

```sh
pnpm --filter kuzhambu-admin-web run format
pnpm run format:check
pnpm run lint
pnpm --filter kuzhambu-admin-web run test
```

前端补充扫描：

```sh
rg "menus\\?:|roleIds|departmentId|parentId|objectId|operatorId" \
  admin-web/src/pages/system/role \
  admin-web/src/pages/system/user \
  admin-web/src/pages/system/menu \
  admin-web/src/pages/system/department \
  admin-web/src/pages/audit/audit-log
```

## Closure

任务完成并通过验证后删除本 RUNBOOK。若执行中发现需要新增长期规则，先迁移到 `docs/00-governance/SERVERS-UNIFIED-ID-DESIGN.md` 或 `docs/00-governance/SERVERS-ARCHITECTURE-RULES.md`，再删除本 RUNBOOK。
