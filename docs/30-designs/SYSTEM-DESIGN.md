# System Design

## Purpose

本文档定义 System 域设计，覆盖用户、角色、菜单、权限资源、认证、token、认证会话、认证事件和业务数据审计。

## Module

```text
kuzhambu-servers/biz/system/
  kuzhambu-system-interface/
  kuzhambu-system-application/
  kuzhambu-system-domain/
  kuzhambu-system-infra/
```

Java package：

```text
com.thundax.kuzhambu.system.interfaces
com.thundax.kuzhambu.system.application
com.thundax.kuzhambu.system.domain
com.thundax.kuzhambu.system.infra
```

## Business Boundary

System 拥有平台用户、角色、菜单、权限资源、认证运行态和业务审计事实。其他业务域只能通过 System application 能力读取当前用户、权限判断、会话状态和写入业务审计，不得直接访问 System 底层表。

System 不拥有业务内容、文件对象、AI 任务、搜索问答或运营报表。

## DDD Model

- `User`：后台用户主体。
- `Role`：后台角色。
- `Menu`：后台菜单和隐藏权限承载节点。
- `PermissionResource`：稳定权限编码资源。
- `PreAuthSession`：登录前临时会话。
- `PrincipalIdentity`：登录标识。
- `PrincipalCredential`：认证凭据。
- `PrincipalAuthSession`：登录后认证会话。
- `PrincipalAccessToken`：访问 token 运行态。
- `PrincipalRefreshToken`：刷新 token 运行态。
- `PrincipalLoginEvent`：认证事件。
- `AuditLog`：单业务对象变更审计事实。
- `AuditState`：业务对象当前审计状态。

## Data Model

表名前缀统一使用 `system_`。

核心表：

- `system_user`
- `system_role`
- `system_menu`
- `system_permission_resource`
- `system_user_role`
- `system_role_menu`
- `system_role_permission`
- `system_pre_auth_session`
- `system_principal_identity`
- `system_principal_credential`
- `system_auth_session`
- `system_access_token`
- `system_refresh_token`
- `system_login_event`
- `system_audit_log`
- `system_audit_state`

规则：

- 密码、验证码、token、secret 和 privateKey 不保存明文。
- 审计日志只追加。
- 业务审计快照只保存展示所需稳定字段。
- 认证事件和业务审计分表表达，不混用。

## Application Layer

- `UserApplicationService`
- `RoleApplicationService`
- `MenuApplicationService`
- `PermissionApplicationService`
- `AuthApplicationService`
- `SessionApplicationService`
- `AuditApplicationService`

Application 层负责事务边界、认证资料同步、权限集合计算、token 生命周期、审计写入和跨域审计入口。

## Interface Layer

Admin 入口：

- 用户管理。
- 角色管理。
- 菜单和权限资源管理。
- 登录前会话、验证码、登录、登出、刷新 token。
- 审计历史和审计日志查询。

Portal 入口：

- 需要登录的 portal 资源复用同一认证能力。
- portal 不提供通用审计查询入口。

## Infrastructure Layer

- Repository 实现 System 领域仓储端口。
- Mapper 只位于 `system.infra.mapper`。
- DO 只位于 `system.infra.dataobject`。
- token 只保存哈希或运行态必要定位信息。

## Data Ownership

System 是 `system_*` 表的唯一写入方。其他业务域通过 application service 或当前认证上下文消费权限和主体信息。

## Observability

- 登录、登出、刷新、失败和会话失效记录认证事件。
- 业务数据变更记录业务审计。
- 安全敏感值不得进入日志、审计快照或异常信息。

## Acceptance

- System 域可以独立提供用户权限、认证会话和审计能力。
- 业务域写操作可通过 System 记录单对象审计事实。
- admin 和 portal 使用同一套认证业务规则。
