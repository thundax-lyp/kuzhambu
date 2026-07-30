# system Java Time 迁移 RUNBOOK

## Purpose

独立完成 `system` 本批旧时间类型迁移，闭合字段、签名、调用方、持久化与协议边界。本文件包含执行所需的完整上下文，不依赖其他迁移 RUNBOOK。

## Scope

- 本批包含 **43** 个生产文件。
- 每个 Task 包含 2–8 个生产文件。
- 字段、签名和操作行以当前分支 `HEAD` 为盘点基线。
- 仅可依据真实编译或调用关系补充遗漏文件，并在任务结果中记录证据。

## Non-goals

- 不机械迁移其他业务域。
- 不修改与时间类型无关的业务逻辑。
- 不把精确时间点降为 `LocalDate`。
- 不默认修改数据库列类型；发现无法无损映射时先停止并补充数据库决策。

## Migration Rules

- domain、application、facade 的时间点使用 `Instant`。
- interface 的自然日期使用 `LocalDate`；精确时间点使用 `Instant`，显式偏移协议使用 `OffsetDateTime`。
- infra 的时间戳列使用 `Instant`，SQL `DATE` 列使用 `LocalDate`。
- `new Date()` 改为 `Instant.now()`；关键规则需要可控时间时注入 `Clock`。
- `before/after` 改为 `isBefore/isAfter`；毫秒算术改为 `Duration` 或 `ChronoUnit`。
- 禁止通过系统默认时区做隐式转换。

## File Inventory

| 层 | 主题 | 文件 | 字段 | 签名与操作 |
|---|---|---|---|---|
| `application` | `audit` | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/query/AuditLogQuery.java` | `beginDate`(L23), `endDate`(L24) | — |
| `application` | `audit` | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/service/impl/AuditTrailApplicationServiceImpl.java` | — | 操作行 L65 |
| `domain` | `audit` | `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/model/entity/AuditLog.java` | `occurredAt`(L43) | — |
| `domain` | `audit` | `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/model/entity/AuditMeta.java` | `lastOperatedAt`(L28), `createdAt`(L30) | — |
| `domain` | `audit` | `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/repository/AuditLogRepository.java` | — | L28 `Date beginDate,`<br>L29 `Date endDate,` |
| `infra` | `audit` | `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/audit/persistence/dataobject/AuditLogDO.java` | `occurredAt`(L38) | — |
| `infra` | `audit` | `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/audit/persistence/dataobject/AuditMetaDO.java` | `lastOperatedAt`(L27), `createdAt`(L29) | — |
| `infra` | `audit` | `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/audit/repository/impl/AuditLogRepositoryImpl.java` | — | L72 `Date beginDate,`<br>L73 `Date endDate,`<br>L93 `Date beginDate,`<br>操作行 L94 |
| `interface` | `audit` | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/controller/request/AuditLogPageRequest.java` | `beginDate`(L40), `endDate`(L43) | — |
| `interface` | `audit` | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/controller/response/AuditLogDetailResponse.java` | `occurredAt`(L71) | — |
| `interface` | `audit` | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/controller/response/AuditLogResponse.java` | `occurredAt`(L72) | — |
| `interface` | `audit` | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/controller/response/AuditMetaResponse.java` | `lastOperatedAt`(L37), `createdAt`(L40) | — |
| `application` | `auth` | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/ChangePrincipalCredentialCommand.java` | `lockedUntil`(L28), `expiresAt`(L29), `lastVerifiedAt`(L30) | — |
| `application` | `auth` | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/ChangePrincipalCredentialVerifyStateCommand.java` | `lockedUntil`(L19), `lastVerifiedAt`(L20) | — |
| `application` | `auth` | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/CreatePrincipalCredentialCommand.java` | `lockedUntil`(L26), `expiresAt`(L27), `lastVerifiedAt`(L28) | — |
| `application` | `auth` | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/RecordPrincipalCredentialFailureCommand.java` | `lockedUntil`(L17) | — |
| `application` | `auth` | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/service/impl/AdminSessionTokenApplicationServiceImpl.java` | — | L288 `private PrincipalAuthSession getActivePrincipalAuthSession(PrincipalAccessToken accessToken, Date now) {`<br>L378 `Date issuedAt,`<br>操作行 L94, L134, L137, L169, L213, L237, L300, L304, L342, L358, L367, L386 |
| `application` | `auth` | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/service/impl/PrincipalAuthenticationApplicationServiceImpl.java` | — | 操作行 L65, L89, L135 |
| `application` | `auth` | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/service/impl/PrincipalPermissionApplicationServiceImpl.java` | — | 操作行 L174, L178, L239 |
| `domain` | `auth` | `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/auth/model/entity/PrincipalAccessToken.java` | `issuedAt`(L29), `expireAt`(L30) | L33 `public boolean canAccess(Date now) {`<br>L57 `public boolean isExpired(Date now) {` |
| `domain` | `auth` | `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/auth/model/entity/PrincipalAuthSession.java` | `issuedAt`(L28), `lastAccessTime`(L29), `expireAt`(L30) | L66 `Date issuedAt,`<br>L67 `Date lastAccessTime,`<br>L87 `Date issuedAt,`<br>L88 `Date lastAccessTime,`<br>L100 `public boolean isExpired(Date now) {`<br>L104 `public int remainingSeconds(Date now) {`<br>操作行 L33, L53, L57, L68, L89 |
| `domain` | `auth` | `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/auth/model/entity/PrincipalCredential.java` | `lockedUntil`(L28), `expiresAt`(L29), `lastVerifiedAt`(L30) | L40 `public boolean isLocked(Date now) {`<br>L47 `public boolean isExpired(Date now) {`<br>L54 `public void markVerified(Date verifiedAt) {`<br>L61 `public void markFailed(Date lockedUntil) {`<br>L68 `public void lock(Date lockedUntil) {` |
| `domain` | `auth` | `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/auth/model/entity/PrincipalLoginEvent.java` | `occurredAt`(L43) | — |
| `domain` | `auth` | `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/auth/model/entity/PrincipalRefreshToken.java` | `issuedAt`(L28), `expireAt`(L29) | L32 `public boolean canRefresh(Date now) {`<br>L60 `public boolean isExpired(Date now) {` |
| `domain` | `auth` | `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/auth/repository/PrincipalAuthSessionRepository.java` | — | L13 `void touch(PrincipalAuthSessionId id, Date accessTime, int expireSeconds);` |
| `domain` | `auth` | `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/auth/repository/PrincipalRefreshTokenRepository.java` | — | L27 `int markUsedIfActive(PrincipalRefreshToken refreshToken, Date now);` |
| `infra` | `auth` | `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/auth/persistence/dataobject/PrincipalCredentialDO.java` | `lockedUntil`(L28), `expiresAt`(L29), `lastVerifiedAt`(L30) | — |
| `infra` | `auth` | `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/auth/persistence/dataobject/PrincipalLoginEventDO.java` | `occurredAt`(L25) | — |
| `infra` | `auth` | `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/auth/persistence/mapper/PrincipalCredentialMapper.java` | — | L31 `@Param("id") Long id, @Param("failedLimit") Integer failedLimit, @Param("lockedUntil") Date lockedUntil);` |
| `infra` | `auth` | `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/auth/repository/impl/PrincipalAccessTokenRepositoryImpl.java` | `issuedAt`(L276), `expireAt`(L277) | L168 `Date expireAt = accessToken.getExpireAt();` |
| `infra` | `auth` | `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/auth/repository/impl/PrincipalAuthSessionRepositoryImpl.java` | `issuedAt`(L164), `lastAccessTime`(L165), `expireAt`(L166) | L56 `public void touch(PrincipalAuthSessionId id, Date accessTime, int expireSeconds) {` |
| `infra` | `auth` | `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/auth/repository/impl/PrincipalRefreshTokenRepositoryImpl.java` | `issuedAt`(L276), `expireAt`(L277) | L141 `public int markUsedIfActive(PrincipalRefreshToken refreshToken, Date now) {`<br>L188 `Date expireAt = refreshToken.getExpireAt();` |
| `interface` | `auth` | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/auth/controller/AuthController.java` | — | 操作行 L337, L351 |
| `application` | `core` | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/CreateLogCommand.java` | `logDate`(L20) | — |
| `application` | `core` | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/LogQuery.java` | `beginDate`(L21), `endDate`(L22) | — |
| `domain` | `core` | `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/model/entity/Log.java` | `logDate`(L22), `createDate`(L30) | — |
| `domain` | `core` | `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/LogRepository.java` | — | L22 `Date beginDate,`<br>L32 `Date beginDate,`<br>L33 `Date endDate,`<br>L45 `int batchDelete(String type, String remoteAddr, String title, String requestUri, Date beginDate, Date endDate);`<br>操作行 L23 |
| `infra` | `core` | `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/core/persistence/dataobject/LogDO.java` | `logDate`(L21) | — |
| `infra` | `core` | `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/core/repository/impl/LogRepositoryImpl.java` | — | L49 `Date beginDate,`<br>L63 `Date beginDate,`<br>L64 `Date endDate,`<br>L120 `Date beginDate,`<br>操作行 L50, L109, L121, L138 |
| `interface` | `core` | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/aop/SysLogMethodInterceptor.java` | — | 操作行 L134 |
| `interface` | `core` | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/request/LogPageRequest.java` | `beginDate`(L49), `endDate`(L54) | — |
| `interface` | `core` | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/response/LogResponse.java` | `createDate`(L31) | — |
| `interface` | `core` | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/service/impl/SysLogMessageServiceImpl.java` | — | L100 `Date logDate,`<br>操作行 L82, L83 |

## Plan

### Task 1: audit 时间类型闭环

涉及生产文件：**8** 个。

#### Files And Changes

- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/query/AuditLogQuery.java`
  - L23 `beginDate`：`Date` → `LocalDate` 候选；必须核对协议、数据库列和比较精度。
  - L24 `endDate`：`Date` → `LocalDate` 候选；必须核对协议、数据库列和比较精度。
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/service/impl/AuditTrailApplicationServiceImpl.java`
  - L65：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/model/entity/AuditLog.java`
  - L43 `occurredAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/model/entity/AuditMeta.java`
  - L28 `lastOperatedAt`：`Date` → `Instant`。
  - L30 `createdAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/repository/AuditLogRepository.java`
  - L28：将签名 `Date beginDate,` 的 `Date` 与本调用链目标类型同步。
  - L29：将签名 `Date endDate,` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/audit/persistence/dataobject/AuditLogDO.java`
  - L38 `occurredAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/audit/persistence/dataobject/AuditMetaDO.java`
  - L27 `lastOperatedAt`：`Date` → `Instant`。
  - L29 `createdAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/audit/repository/impl/AuditLogRepositoryImpl.java`
  - L72：将签名 `Date beginDate,` 的 `Date` 与本调用链目标类型同步。
  - L73：将签名 `Date endDate,` 的 `Date` 与本调用链目标类型同步。
  - L93：将签名 `Date beginDate,` 的 `Date` 与本调用链目标类型同步。
  - L94：调整当前时间构造、比较、算术或转换。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

### Task 2: audit 时间类型闭环

涉及生产文件：**4** 个。

#### Files And Changes

- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/controller/request/AuditLogPageRequest.java`
  - L40 `beginDate`：`Date` → `LocalDate` 候选；必须核对协议、数据库列和比较精度。
  - L43 `endDate`：`Date` → `LocalDate` 候选；必须核对协议、数据库列和比较精度。
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/controller/response/AuditLogDetailResponse.java`
  - L71 `occurredAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/controller/response/AuditLogResponse.java`
  - L72 `occurredAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/controller/response/AuditMetaResponse.java`
  - L37 `lastOperatedAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。
  - L40 `createdAt`：`Date` → `Instant`；若协议要求显式偏移则使用 `OffsetDateTime`。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

### Task 3: auth 时间类型闭环

涉及生产文件：**8** 个。

#### Files And Changes

- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/ChangePrincipalCredentialCommand.java`
  - L28 `lockedUntil`：`Date` → `Instant`。
  - L29 `expiresAt`：`Date` → `Instant`。
  - L30 `lastVerifiedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/ChangePrincipalCredentialVerifyStateCommand.java`
  - L19 `lockedUntil`：`Date` → `Instant`。
  - L20 `lastVerifiedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/CreatePrincipalCredentialCommand.java`
  - L26 `lockedUntil`：`Date` → `Instant`。
  - L27 `expiresAt`：`Date` → `Instant`。
  - L28 `lastVerifiedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/RecordPrincipalCredentialFailureCommand.java`
  - L17 `lockedUntil`：`Date` → `Instant`。
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/service/impl/AdminSessionTokenApplicationServiceImpl.java`
  - L288：将签名 `private PrincipalAuthSession getActivePrincipalAuthSession(PrincipalAccessToken accessToken, Date now) {` 的 `Date` 与本调用链目标类型同步。
  - L378：将签名 `Date issuedAt,` 的 `Date` 与本调用链目标类型同步。
  - L94, L134, L137, L169, L213, L237, L300, L304, L342, L358, L367, L386：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/service/impl/PrincipalAuthenticationApplicationServiceImpl.java`
  - L65, L89, L135：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/service/impl/PrincipalPermissionApplicationServiceImpl.java`
  - L174, L178, L239：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/auth/model/entity/PrincipalAccessToken.java`
  - L29 `issuedAt`：`Date` → `Instant`。
  - L30 `expireAt`：`Date` → `Instant`。
  - L33：将签名 `public boolean canAccess(Date now) {` 的 `Date` 与本调用链目标类型同步。
  - L57：将签名 `public boolean isExpired(Date now) {` 的 `Date` 与本调用链目标类型同步。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

### Task 4: auth 时间类型闭环

涉及生产文件：**8** 个。

#### Files And Changes

- `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/auth/model/entity/PrincipalAuthSession.java`
  - L28 `issuedAt`：`Date` → `Instant`。
  - L29 `lastAccessTime`：`Date` → `Instant`。
  - L30 `expireAt`：`Date` → `Instant`。
  - L66：将签名 `Date issuedAt,` 的 `Date` 与本调用链目标类型同步。
  - L67：将签名 `Date lastAccessTime,` 的 `Date` 与本调用链目标类型同步。
  - L87：将签名 `Date issuedAt,` 的 `Date` 与本调用链目标类型同步。
  - L88：将签名 `Date lastAccessTime,` 的 `Date` 与本调用链目标类型同步。
  - L100：将签名 `public boolean isExpired(Date now) {` 的 `Date` 与本调用链目标类型同步。
  - L104：将签名 `public int remainingSeconds(Date now) {` 的 `Date` 与本调用链目标类型同步。
  - L33, L53, L57, L68, L89：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/auth/model/entity/PrincipalCredential.java`
  - L28 `lockedUntil`：`Date` → `Instant`。
  - L29 `expiresAt`：`Date` → `Instant`。
  - L30 `lastVerifiedAt`：`Date` → `Instant`。
  - L40：将签名 `public boolean isLocked(Date now) {` 的 `Date` 与本调用链目标类型同步。
  - L47：将签名 `public boolean isExpired(Date now) {` 的 `Date` 与本调用链目标类型同步。
  - L54：将签名 `public void markVerified(Date verifiedAt) {` 的 `Date` 与本调用链目标类型同步。
  - L61：将签名 `public void markFailed(Date lockedUntil) {` 的 `Date` 与本调用链目标类型同步。
  - L68：将签名 `public void lock(Date lockedUntil) {` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/auth/model/entity/PrincipalLoginEvent.java`
  - L43 `occurredAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/auth/model/entity/PrincipalRefreshToken.java`
  - L28 `issuedAt`：`Date` → `Instant`。
  - L29 `expireAt`：`Date` → `Instant`。
  - L32：将签名 `public boolean canRefresh(Date now) {` 的 `Date` 与本调用链目标类型同步。
  - L60：将签名 `public boolean isExpired(Date now) {` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/auth/repository/PrincipalAuthSessionRepository.java`
  - L13：将签名 `void touch(PrincipalAuthSessionId id, Date accessTime, int expireSeconds);` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/auth/repository/PrincipalRefreshTokenRepository.java`
  - L27：将签名 `int markUsedIfActive(PrincipalRefreshToken refreshToken, Date now);` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/auth/persistence/dataobject/PrincipalCredentialDO.java`
  - L28 `lockedUntil`：`Date` → `Instant`。
  - L29 `expiresAt`：`Date` → `Instant`。
  - L30 `lastVerifiedAt`：`Date` → `Instant`。
- `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/auth/persistence/dataobject/PrincipalLoginEventDO.java`
  - L25 `occurredAt`：`Date` → `Instant`。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

### Task 5: auth 时间类型闭环

涉及生产文件：**5** 个。

#### Files And Changes

- `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/auth/persistence/mapper/PrincipalCredentialMapper.java`
  - L31：将签名 `@Param("id") Long id, @Param("failedLimit") Integer failedLimit, @Param("lockedUntil") Date lockedUntil);` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/auth/repository/impl/PrincipalAccessTokenRepositoryImpl.java`
  - L276 `issuedAt`：`Date` → `Instant`。
  - L277 `expireAt`：`Date` → `Instant`。
  - L168：将签名 `Date expireAt = accessToken.getExpireAt();` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/auth/repository/impl/PrincipalAuthSessionRepositoryImpl.java`
  - L164 `issuedAt`：`Date` → `Instant`。
  - L165 `lastAccessTime`：`Date` → `Instant`。
  - L166 `expireAt`：`Date` → `Instant`。
  - L56：将签名 `public void touch(PrincipalAuthSessionId id, Date accessTime, int expireSeconds) {` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/auth/repository/impl/PrincipalRefreshTokenRepositoryImpl.java`
  - L276 `issuedAt`：`Date` → `Instant`。
  - L277 `expireAt`：`Date` → `Instant`。
  - L141：将签名 `public int markUsedIfActive(PrincipalRefreshToken refreshToken, Date now) {` 的 `Date` 与本调用链目标类型同步。
  - L188：将签名 `Date expireAt = refreshToken.getExpireAt();` 的 `Date` 与本调用链目标类型同步。
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/auth/controller/AuthController.java`
  - L337, L351：调整当前时间构造、比较、算术或转换。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

### Task 6: core 时间类型闭环

涉及生产文件：**8** 个。

#### Files And Changes

- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/CreateLogCommand.java`
  - L20 `logDate`：`Date` → `LocalDate` 候选；必须核对协议、数据库列和比较精度。
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/LogQuery.java`
  - L21 `beginDate`：`Date` → `LocalDate` 候选；必须核对协议、数据库列和比较精度。
  - L22 `endDate`：`Date` → `LocalDate` 候选；必须核对协议、数据库列和比较精度。
- `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/model/entity/Log.java`
  - L22 `logDate`：`Date` → `LocalDate` 候选；必须核对协议、数据库列和比较精度。
  - L30 `createDate`：`Date` → `LocalDate` 候选；必须核对协议、数据库列和比较精度。
- `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/LogRepository.java`
  - L22：将签名 `Date beginDate,` 的 `Date` 与本调用链目标类型同步。
  - L32：将签名 `Date beginDate,` 的 `Date` 与本调用链目标类型同步。
  - L33：将签名 `Date endDate,` 的 `Date` 与本调用链目标类型同步。
  - L45：将签名 `int batchDelete(String type, String remoteAddr, String title, String requestUri, Date beginDate, Date endDate);` 的 `Date` 与本调用链目标类型同步。
  - L23：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/core/persistence/dataobject/LogDO.java`
  - L21 `logDate`：`Date` → `LocalDate` 候选；必须核对协议、数据库列和比较精度。
- `kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/core/repository/impl/LogRepositoryImpl.java`
  - L49：将签名 `Date beginDate,` 的 `Date` 与本调用链目标类型同步。
  - L63：将签名 `Date beginDate,` 的 `Date` 与本调用链目标类型同步。
  - L64：将签名 `Date endDate,` 的 `Date` 与本调用链目标类型同步。
  - L120：将签名 `Date beginDate,` 的 `Date` 与本调用链目标类型同步。
  - L50, L109, L121, L138：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/aop/SysLogMethodInterceptor.java`
  - L134：调整当前时间构造、比较、算术或转换。
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/request/LogPageRequest.java`
  - L49 `beginDate`：`Date` → `LocalDate` 候选；必须核对协议、数据库列和比较精度。
  - L54 `endDate`：`Date` → `LocalDate` 候选；必须核对协议、数据库列和比较精度。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

### Task 7: core 时间类型闭环

涉及生产文件：**2** 个。

#### Files And Changes

- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/response/LogResponse.java`
  - L31 `createDate`：`Date` → `LocalDate` 候选；必须核对协议、数据库列和比较精度。
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/service/impl/SysLogMessageServiceImpl.java`
  - L100：将签名 `Date logDate,` 的 `Date` 与本调用链目标类型同步。
  - L82, L83：调整当前时间构造、比较、算术或转换。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

## Associated Test Files

下列测试按本批主题匹配；执行时还必须根据编译和真实调用链补充测试。

- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/auth/service/impl/PrincipalPermissionApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/admin/core/service/impl/SysLogMessageServiceImplTest.java`

## Verification

- `rg 'java\.util\.Date|java\.sql\.(Date|Timestamp)' kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/query/AuditLogQuery.java kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/service/impl/AuditTrailApplicationServiceImpl.java kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/model/entity/AuditLog.java kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/model/entity/AuditMeta.java kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/repository/AuditLogRepository.java kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/audit/persistence/dataobject/AuditLogDO.java kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/audit/persistence/dataobject/AuditMetaDO.java kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/audit/repository/impl/AuditLogRepositoryImpl.java kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/controller/request/AuditLogPageRequest.java kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/controller/response/AuditLogDetailResponse.java kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/controller/response/AuditLogResponse.java kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/controller/response/AuditMetaResponse.java kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/ChangePrincipalCredentialCommand.java kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/ChangePrincipalCredentialVerifyStateCommand.java kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/CreatePrincipalCredentialCommand.java kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/command/RecordPrincipalCredentialFailureCommand.java kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/service/impl/AdminSessionTokenApplicationServiceImpl.java kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/service/impl/PrincipalAuthenticationApplicationServiceImpl.java kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/auth/service/impl/PrincipalPermissionApplicationServiceImpl.java kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/auth/model/entity/PrincipalAccessToken.java kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/auth/model/entity/PrincipalAuthSession.java kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/auth/model/entity/PrincipalCredential.java kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/auth/model/entity/PrincipalLoginEvent.java kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/auth/model/entity/PrincipalRefreshToken.java kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/auth/repository/PrincipalAuthSessionRepository.java kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/auth/repository/PrincipalRefreshTokenRepository.java kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/auth/persistence/dataobject/PrincipalCredentialDO.java kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/auth/persistence/dataobject/PrincipalLoginEventDO.java kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/auth/persistence/mapper/PrincipalCredentialMapper.java kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/auth/repository/impl/PrincipalAccessTokenRepositoryImpl.java kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/auth/repository/impl/PrincipalAuthSessionRepositoryImpl.java kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/auth/repository/impl/PrincipalRefreshTokenRepositoryImpl.java kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/auth/controller/AuthController.java kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/CreateLogCommand.java kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/LogQuery.java kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/model/entity/Log.java kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/core/repository/LogRepository.java kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/core/persistence/dataobject/LogDO.java kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/core/repository/impl/LogRepositoryImpl.java kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/aop/SysLogMethodInterceptor.java kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/request/LogPageRequest.java kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/response/LogResponse.java kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/service/impl/SysLogMessageServiceImpl.java`。
- `cd kuzhambu-servers && mvn -pl :kuzhambu-system-domain,:kuzhambu-system-application,:kuzhambu-system-interface,:kuzhambu-system-infra spotless:apply`，随后检查 `git diff`。
- `cd kuzhambu-servers && mvn spotless:check`。
- `cd kuzhambu-servers && mvn checkstyle:check`。
- `cd kuzhambu-servers && mvn -pl :kuzhambu-system-domain,:kuzhambu-system-application,:kuzhambu-system-interface,:kuzhambu-system-infra -am -amd test`；确认 Reactor Build Order 包含上述 4 个叶子模块及其下游装配模块。
- 涉及 HTTP 时验证 JSON 精度、格式和时区；涉及 infra 时验证数据库往返。

## Closure

本批所有 Task 完成、验证通过并同步必要接口或数据库文档后删除本 RUNBOOK。未完成范围必须收窄为新的独立 RUNBOOK，不保留完成历史。
