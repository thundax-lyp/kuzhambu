# Core Design

## Purpose

本文档定义 Core 模块的数据结构和接口设计。Core 从 sandwich 的 `System/sys` 设计继承用户、角色、菜单和授权关系能力，并按 kuzhambu 业务边界收窄为框架业务域。

Core 只负责用户主体、角色、菜单、权限编码和当前用户视图；登录标识、密码凭据、token 和认证会话归属 Auth。

## Module

- 模块名称：Core
- 业务域：core
- 对应需求文档：[CORE-REQUIREMENTS.md](../10-requirements/CORE-REQUIREMENTS.md)
- 后端 biz 子工程：`kuzhambu-servers/biz/kuzhambu-biz-core`
- 后端 infra 子工程：`kuzhambu-servers/infra/kuzhambu-infra-core`
- 后台接口入口：`kuzhambu-servers/interfaces/kuzhambu-admin-api`
- 前台接口入口：无通用入口
- 前端入口：`kuzhambu-apps/admin-web`
- Python worker 能力：无

## Business Boundary

- 本模块负责：后台用户主体、角色、菜单、权限编码、用户角色关系、角色菜单关系和当前用户视图。
- 本模块不负责：认证登录、密码校验、token、文件存储、业务审计、部门、岗位、字典和运行日志。
- 依赖的其他业务域能力：Auth 提供认证资料维护和会话失效；Storage 提供头像等文件对象；Audit 记录用户、角色、菜单变更。
- 对外提供的业务能力：用户管理、角色管理、菜单管理、当前用户资料、当前用户菜单和权限编码。

## Data Model

Core 表沿用系统管理语义，固定使用 `sys_` 前缀。业务对象对外标识使用 ULID；数据库可保留内部 `bigint id`。

### sys_user

保存后台用户主体资料。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `user_id` | `userId` | 是 | 对外用户 ULID |
| `name` | `name` | 是 | 用户显示名称 |
| `email` | `email` | 否 | 联系邮箱，敏感存储 |
| `mobile` | `mobile` | 否 | 联系手机号，敏感存储 |
| `tel` | `tel` | 否 | 联系电话 |
| `avatar_object_id` | `avatarObjectId` | 否 | Storage 文件对象标识 |
| `rank` | `rank` | 是 | 访问等级 |
| `privilege` | `privilege` | 是 | 用户权限等级：`NORMAL` / `ADMIN` / `SUPER` |
| `status` | `status` | 是 | 用户状态：`ENABLED` / `DISABLED` |
| `remarks` | `remarks` | 否 | 备注 |
| `created_at` | `createdAt` | 是 | 创建时间 |
| `updated_at` | `updatedAt` | 是 | 更新时间 |

约束：
- `user_id` 唯一。
- 用户主体不保存密码、token、最近登录 IP、登录次数和登录行为明细。
- `email` 和 `mobile` 不得明文输出到日志。

### sys_role

保存后台角色。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `role_id` | `roleId` | 是 | 对外角色 ULID |
| `name` | `name` | 是 | 角色名称 |
| `privilege` | `privilege` | 是 | 角色权限等级 |
| `status` | `status` | 是 | 角色状态：`ENABLED` / `DISABLED` |
| `priority` | `priority` | 是 | 排序值 |
| `remarks` | `remarks` | 否 | 备注 |
| `created_at` | `createdAt` | 是 | 创建时间 |
| `updated_at` | `updatedAt` | 是 | 更新时间 |

约束：
- `role_id` 唯一。
- `name` 在启用角色中应保持可区分。
- 角色删除必须清理用户角色关系和角色菜单关系。

### sys_menu

保存后台菜单和权限资源。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `menu_id` | `menuId` | 是 | 对外菜单 ULID |
| `parent_menu_id` | `parentMenuId` | 否 | 父菜单 ULID |
| `name` | `name` | 是 | 菜单名称 |
| `permission_codes` | `permissionCodes` | 否 | 权限编码集合，逗号或 JSON 表达 |
| `rank` | `rank` | 是 | 访问等级 |
| `visibility` | `visibility` | 是 | `VISIBLE` / `HIDDEN` |
| `display_params` | `displayParams` | 否 | 展示参数 JSON，例如 icon |
| `path` | `path` | 否 | 前端路由或资源路径 |
| `target` | `target` | 否 | 打开目标 |
| `priority` | `priority` | 是 | 同级排序值 |
| `remarks` | `remarks` | 否 | 备注 |
| `created_at` | `createdAt` | 是 | 创建时间 |
| `updated_at` | `updatedAt` | 是 | 更新时间 |

约束：
- 菜单树通过 `parent_menu_id` 表达业务父子关系。
- 可见菜单进入后台导航；隐藏菜单承载按钮、操作列和接口级权限。
- 权限编码必须稳定，不随展示文案变化。

### sys_user_role

保存用户与角色关系。

| Column | Required | Description |
| --- | --- | --- |
| `user_id` | 是 | 用户 ULID |
| `role_id` | 是 | 角色 ULID |

约束：
- 唯一约束：`user_id + role_id`。
- 用户角色关系由 User Application Service 重写。

### sys_role_menu

保存角色与菜单关系。

| Column | Required | Description |
| --- | --- | --- |
| `role_id` | 是 | 角色 ULID |
| `menu_id` | 是 | 菜单 ULID |

约束：
- 唯一约束：`role_id + menu_id`。
- 角色菜单关系由 Role Application Service 重写。

## Application Layer

- `UserApplicationService`：创建用户、更新用户、启用用户、禁用用户、删除用户、分配角色、头像绑定。
- `RoleApplicationService`：创建角色、更新角色、启用角色、禁用角色、删除角色、维护角色用户、授权菜单。
- `MenuApplicationService`：创建菜单、更新菜单、删除菜单、移动菜单、排序菜单、切换可见性。
- `CurrentUserApplicationService`：读取当前用户资料、更新当前用户资料、修改当前用户密码、读取当前用户菜单和权限编码。

事务边界：
- 用户创建必须在同一应用事务中保存用户主体、用户角色关系，并调用 Auth 创建默认登录标识和密码凭据。
- 用户禁用必须在用户状态变更后调用 Auth 失效该用户已有认证会话。
- 角色授权菜单和用户分配角色采用关系表重写。

## Interface Layer

后台接口固定使用 `/api/admin/core/**`。

### User API

- `GET /core/users`：分页查询用户。
- `GET /core/users/{userId}`：读取用户详情。
- `POST /core/users`：创建用户并派发账号。
- `PUT /core/users/{userId}`：更新用户资料。
- `POST /core/users/{userId}/enable`：启用用户。
- `POST /core/users/{userId}/disable`：禁用用户。
- `DELETE /core/users/{userId}`：删除用户。
- `PUT /core/users/{userId}/roles`：重写用户角色。
- `POST /core/users/{userId}/password/reset`：重置用户密码。

### Current User API

- `GET /core/current-user`：读取当前用户资料。
- `PUT /core/current-user/profile`：更新当前用户资料。
- `PUT /core/current-user/password`：修改当前用户密码。
- `GET /core/current-user/menus`：读取当前用户可见菜单树。
- `GET /core/current-user/permissions`：读取当前用户权限编码。
- `POST /core/current-user/avatar`：上传或绑定当前用户头像。
- `DELETE /core/current-user/avatar`：删除当前用户头像。

### Role API

- `GET /core/roles`：分页查询角色。
- `GET /core/roles/{roleId}`：读取角色详情。
- `POST /core/roles`：创建角色。
- `PUT /core/roles/{roleId}`：更新角色。
- `POST /core/roles/{roleId}/enable`：启用角色。
- `POST /core/roles/{roleId}/disable`：禁用角色。
- `DELETE /core/roles/{roleId}`：删除角色。
- `PUT /core/roles/{roleId}/menus`：重写角色菜单授权。
- `PUT /core/roles/{roleId}/users`：重写角色用户关系。

### Menu API

- `GET /core/menus`：查询菜单树或平铺列表。
- `GET /core/menus/{menuId}`：读取菜单详情。
- `POST /core/menus`：创建菜单。
- `PUT /core/menus/{menuId}`：更新菜单。
- `DELETE /core/menus/{menuId}`：删除菜单。
- `POST /core/menus/{menuId}/move`：移动菜单。
- `POST /core/menus/{menuId}/show`：显示菜单。
- `POST /core/menus/{menuId}/hide`：隐藏菜单。

## Infrastructure Layer

- Repository：`UserRepository`、`RoleRepository`、`MenuRepository`。
- Mapper：`SysUserMapper`、`SysRoleMapper`、`SysMenuMapper`、`SysUserRoleMapper`、`SysRoleMenuMapper`。
- PersistenceAssembler：`UserPersistenceAssembler`、`RolePersistenceAssembler`、`MenuPersistenceAssembler`。
- 缓存：用户资料、角色授权、菜单树、权限编码集合。
- 文件存储：头像只保存 Storage 文件对象标识。

## Data Ownership

- 本模块拥有：`sys_user`、`sys_role`、`sys_menu`、`sys_user_role`、`sys_role_menu`。
- 本模块只读引用：Auth 认证上下文、Storage 文件对象摘要。
- 禁止跨域直接访问：不得直接访问 Auth 凭据表或 Storage 底层对象表。
- Flyway 脚本归属：`kuzhambu-admin-api` 的 `db/migration/V1__init.sql`，按 Core 分段。

## Observability

- 运行日志：记录用户、角色、菜单管理关键失败原因。
- 访问日志：由接口层统一记录。
- 审计日志：用户、角色、菜单写操作必须接入 Audit。
- 关键指标：用户数、启用用户数、角色数、菜单数、权限编码数。

## Acceptance

- 管理员可完成用户创建、派发账号、禁用、角色分配。
- 用户可读取当前用户资料、菜单和权限编码。
- 禁用用户后 Auth 会话失效。
- 删除角色后用户不再获得该角色权限。
- 隐藏菜单不出现在导航树，但权限编码可用于鉴权。
