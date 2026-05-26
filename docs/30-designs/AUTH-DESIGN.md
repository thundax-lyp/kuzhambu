# Auth Design

## Purpose

本文档定义 Auth 模块的数据结构和接口设计。Auth 只覆盖账号登录、验证码、认证凭据、token、认证会话和当前认证上下文。

Auth 固定区分用户主体、登录标识、认证凭据、访问 token、refresh token 和认证会话。

## Module

- 模块名称：Auth
- 业务域：auth
- 对应需求文档：[AUTH-REQUIREMENTS.md](../10-requirements/AUTH-REQUIREMENTS.md)
- 后端 biz 子工程：`kuzhambu-servers/biz/kuzhambu-biz-auth`
- 后端 infra 子工程：`kuzhambu-servers/infra/kuzhambu-infra-auth`
- 后台接口入口：`kuzhambu-servers/interfaces/kuzhambu-admin-api`
- 前台接口入口：`kuzhambu-servers/interfaces/kuzhambu-portal-api`
- 前端入口：`kuzhambu-apps/admin-web`、`kuzhambu-apps/portal-web`
- Python worker 能力：无

## Business Boundary

- 本模块负责：登录前会话、验证码、登录标识、密码凭据、access token、refresh token、认证会话、认证上下文和认证事件。
- 本模块不负责：用户主体、角色、菜单、权限资源、OAuth2、第三方登录、短信登录、用户自助注册。
- 依赖的其他业务域能力：Core 提供用户主体、用户状态和权限编码。
- 对外提供的业务能力：登录、登出、token 刷新、会话失效、认证资料维护。

## Data Model

Auth 数据库表固定使用 `auth_` 前缀。运行态对象使用 Redis / cache，不建立数据库表。

### auth_principal_identity

保存统一认证主体登录标识。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `identity_id` | `identityId` | 是 | 登录标识 ULID |
| `principal_type` | `principalKey.principalType` | 是 | 主体类型，当前固定 `USER` |
| `principal_id` | `principalKey.principalId` | 是 | Core 用户 ULID |
| `identity_type` | `identityType` | 是 | 当前固定 `USER_ACCOUNT`，可支持 `USER_MOBILE`、`USER_EMAIL` |
| `identity_value` | `identityValue` | 是 | 规范化后的登录标识 |
| `status` | `status` | 是 | `ENABLED` / `DISABLED` |

约束：
- 唯一约束：`identity_type + identity_value`。
- 索引：`principal_type + principal_id + status`。
- 登录标识不保存密码哈希。

### auth_principal_credential

保存统一认证主体认证凭据。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `credential_id` | `credentialId` | 是 | 凭据 ULID |
| `principal_type` | `principalKey.principalType` | 是 | 主体类型，当前固定 `USER` |
| `principal_id` | `principalKey.principalId` | 是 | Core 用户 ULID |
| `identity_id` | `identityId` | 是 | 登录标识 ULID |
| `credential_type` | `credentialType` | 是 | 当前固定 `USER_PASSWORD` |
| `credential_value` | `credentialValue` | 是 | 密码哈希 |
| `status` | `status` | 是 | `ACTIVE` / `LOCKED` / `EXPIRED` / `DISABLED` |
| `need_change_password` | `needChangePassword` | 是 | 是否强制改密 |
| `failed_count` | `failedCount` | 是 | 连续失败次数 |
| `failed_limit` | `failedLimit` | 是 | 最大失败次数 |
| `locked_until` | `lockedUntil` | 否 | 锁定截止时间 |
| `expires_at` | `expiresAt` | 否 | 凭据过期时间 |
| `last_verified_at` | `lastVerifiedAt` | 否 | 最近验证时间 |

约束：
- 唯一约束：`identity_id + credential_type`。
- 密码哈希固定保存到 `credential_value`，不得保存明文。
- 锁定语义发生在凭据维度。

### auth_principal_login_event

保存认证事件事实。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `event_id` | `eventId` | 是 | 事件 ULID |
| `principal_type` | `principalKey.principalType` | 否 | 主体类型 |
| `principal_id` | `principalKey.principalId` | 否 | 主体 ULID |
| `client_id` | `clientId` | 是 | `admin-api` / `portal-api` |
| `event_type` | `eventType` | 是 | `LOGIN_SUCCESS` / `LOGIN_FAILED` / `LOGOUT` / `TOKEN_REFRESH` / `SESSION_REVOKED` |
| `authentication_method` | `authenticationMethod` | 是 | `PASSWORD` / `REFRESH_TOKEN` |
| `identity_type` | `identityType` | 否 | 登录标识类型 |
| `occurred_at` | `occurredAt` | 是 | 发生时间 |
| `ip` | `ip` | 否 | 请求 IP |
| `user_agent` | `userAgent` | 否 | User-Agent |
| `reason` | `reason` | 否 | 固定原因值 |

约束：
- 登录失败且无法识别主体时，`principal_type` 和 `principal_id` 允许为空。
- 不记录异常堆栈、密码、验证码、token 明文。
- 认证事件不进入 Audit 业务审计。

### Runtime Objects

以下对象只进入 Redis / cache，不建数据库表：

- `PreAuthSession`：登录前临时会话。
- `PrincipalAuthSession`：登录后认证会话。
- `PrincipalAccessToken`：访问 token 运行态。
- `PrincipalRefreshToken`：刷新 token 运行态。

运行态索引：
- token code -> token hash -> token runtime。
- session id -> auth session。
- principal key -> session ids。

## Application Layer

- `PreAuthApplicationService`：创建登录前会话、刷新验证码、校验登录 token。
- `AuthenticationApplicationService`：账号密码登录、登出、刷新 token。
- `TokenApplicationService`：access token 校验、refresh token 轮换、token 撤销。
- `AuthSessionApplicationService`：会话 touch、按 token 失效、按用户失效。
- `CredentialApplicationService`：创建默认账号、更新登录名、重置密码、修改密码、禁用标识和凭据。

事务边界：
- 登录成功流程必须创建 access token、refresh token、auth session，并写入登录事件。
- 密码失败次数、锁定状态和最近验证时间必须在凭据校验流程内写回。
- Core 创建用户时由 Core Application Service 编排调用 Auth 创建认证资料。

## Interface Layer

后台接口固定使用 `/api/admin/auth/**`。portal 需要登录时使用 `/api/portal/auth/**`，复用同一业务流程。

### Admin Auth API

- `POST /auth/pre-auth-sessions`：创建登录前会话，返回登录 token 和验证码信息。
- `POST /auth/pre-auth-sessions/{loginToken}/captcha/refresh`：刷新验证码。
- `POST /auth/login`：账号密码登录。
- `POST /auth/logout`：当前 token 登出。
- `POST /auth/token/refresh`：刷新访问 token。
- `GET /auth/session`：读取当前认证会话摘要。
- `POST /auth/users/{userId}/sessions/revoke`：管理员按用户失效会话。

### Portal Auth API

- `POST /auth/pre-auth-sessions`：创建登录前会话。
- `POST /auth/login`：账号密码登录。
- `POST /auth/logout`：登出。
- `POST /auth/token/refresh`：刷新访问 token。
- `GET /auth/session`：读取当前认证会话摘要。

接口返回规则：
- access token 和 refresh token 明文只在登录或刷新响应中返回一次。
- 错误响应不得泄露账号存在性、密码哈希、token hash 或内部 session id。

## Infrastructure Layer

- Repository：`PrincipalIdentityRepository`、`PrincipalCredentialRepository`、`PrincipalLoginEventRepository`、`PreAuthSessionRepository`、`PrincipalAuthSessionRepository`、`PrincipalAccessTokenRepository`、`PrincipalRefreshTokenRepository`。
- Mapper：`PrincipalIdentityMapper`、`PrincipalCredentialMapper`、`PrincipalLoginEventMapper`。
- PersistenceAssembler：`PrincipalIdentityPersistenceAssembler`、`PrincipalCredentialPersistenceAssembler`、`PrincipalLoginEventPersistenceAssembler`。
- 外部客户端：无。
- 缓存：登录前会话、认证会话、access token、refresh token、用户会话索引。

## Data Ownership

- 本模块拥有：`auth_principal_identity`、`auth_principal_credential`、`auth_principal_login_event`。
- 本模块只读引用：Core 用户主体状态、Core 权限编码。
- 禁止跨域直接访问：不得直接写 Core 用户表和菜单表。
- Flyway 脚本归属：`kuzhambu-admin-api` 的 `db/migration/V1__init.sql`，按 Auth 分段。

## Observability

- 运行日志：记录认证流程异常摘要，不记录敏感值。
- 访问日志：由接口层统一记录。
- 审计日志：认证事件进入 `auth_principal_login_event`，不进入 Audit。
- 关键指标：登录成功数、登录失败数、凭据锁定数、活跃会话数、token 刷新数。

## Acceptance

- 账号密码登录、验证码、token 刷新、登出和会话失效可用。
- 密码错误达到限制后凭据锁定。
- refresh token 不能重复使用。
- 用户禁用后已有会话失效。
- 认证事件可查询和追溯。
