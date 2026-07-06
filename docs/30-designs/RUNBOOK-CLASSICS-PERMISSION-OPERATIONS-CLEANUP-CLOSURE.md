# RUNBOOK: Classics/System Permission And Operations Cleanup Closure

## 目标

关闭两个未完成闭环：

1. Classics/System 权限过滤闭环：让 Classics 的私有内容、批量状态修改、分享、导出与管理列表按 System 权限稳定过滤或拒绝。
2. Operations cleanup 真实执行闭环：让 Operations 清理任务从“记录状态”推进到真实发现目标、执行清理、落库明细，并在 Admin Web 可核验结果。

本 RUNBOOK 只规划本分支实现边界。实现完成后应删除本文件，并更新对应 Implementation Coverage。

## 当前依据

- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
  - Classics 私有内容可见性、批量公开/私有、导出快照、分享权限仍存在部分完成项。
- `docs/40-readiness/SYSTEM-IMPLEMENTATION-COVERAGE.md`
  - System 已有 `@HasPermission` 与权限种子，但细粒度资源过滤接入点仍需闭环。
- `docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md`
  - Operations cleanup 入口存在，但执行仍偏占位，需要真实 discover/execute/item detail。
- `docs/00-governance/SERVERS-ARCHITECTURE.md`
  - 跨业务域只能通过 facade 或稳定接口协作，禁止跨域访问 infra/mapper。
- `docs/00-governance/TODO-RULES.md`
  - 后续 TODO 必须按 2-5 个文件粒度拆分，一任务一提交，收尾清理 RUNBOOK。

## 范围

### 纳入

- Classics 后端权限过滤与权限拒绝。
- System 权限种子与权限码一致性修正。
- Operations cleanup 发现目标、执行目标、记录 item 明细。
- Admin Web cleanup 结果可见性与权限码一致性。
- Admin Web Classics 权限过滤相关按钮/批量操作状态对齐。
- Portal Web 回归确认：公开内容与公开分享访问不被本次权限过滤破坏。
- Implementation Coverage 更新。

### 不纳入

- 新增租户/组织级 ACL 表。
- Portal Web 登录态或私有分享访问入口。
- Storage 真实物理对象删除。导出清理本次只关闭业务 export job 状态，Storage orphan cleanup 仍由 Storage 域负责。
- Cleanup 定时调度。本次只实现 Admin 触发后的真实执行。

## 数据结构变更

### 数据库 schema

本次不新增、不删除、不改名任何数据库字段。

不改动以下表结构：

- `classics_sancai_entry`
- `classics_wangqi_document`
- `classics_ming_customs`
- `classics_share_link`
- `classics_sancai_entry_draft`
- `classics_content_export_job`
- `operations_backup`
- `operations_cleanup_job`
- `operations_cleanup_item`
- `system_menu`
- `system_role`
- `system_role_menu`

### System 种子数据

修改文件：

- `db/data-source/system.json`
- `db/data/system.sql`

字段级变更：

- `db/data-source/system.json`
  - `menus[].perms`
  - 将“清理维护”菜单权限从：
    - `operations:cleanup:view`
    - `operations:cleanup:edit`
  - 调整为：
    - `operations:cleanup:view`
    - `operations:cleanup:execute`

保持现有 Classics 权限码，不新增权限码：

- `classics:sancai:view`
- `classics:sancai:edit`
- `classics:sancai:delete`
- `classics:wangqi:view`
- `classics:wangqi:edit`
- `classics:wangqi:delete`
- `classics:mingcustoms:view`
- `classics:mingcustoms:edit`
- `classics:mingcustoms:delete`
- `classics:content:view`
- `classics:content:edit`
- `classics:content:export`
- `classics:sharing:view`
- `classics:sharing:edit`

### Java DTO/Command/Result 字段

#### Classics 权限上下文

在 application 层需要权限过滤或权限拒绝的命令/查询对象补充以下字段：

- `operatorUserId: Long`
- `operatorPermissions: Set<String>`

字段变更对象：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/query/SancaiEntryPageQuery.java`
  - 新增 `operatorUserId: Long`
  - 新增 `operatorPermissions: Set<String>`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/query/WangqiDocumentPageQuery.java`
  - 新增 `operatorUserId: Long`
  - 新增 `operatorPermissions: Set<String>`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/query/MingCustomsPageQuery.java`
  - 新增 `operatorUserId: Long`
  - 新增 `operatorPermissions: Set<String>`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/command/ContentExportCommand.java`
  - 新增 `operatorUserId: Long`
  - 新增 `operatorPermissions: Set<String>`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/command/ShareLinkCreateCommand.java`
  - 新增 `operatorUserId: Long`
  - 新增 `operatorPermissions: Set<String>`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/command/BatchShareCreateCommand.java`
  - 新增 `operatorUserId: Long`
  - 新增 `operatorPermissions: Set<String>`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiEntryStatusCommand.java`
  - 新增 `operatorUserId: Long`
  - 新增 `operatorPermissions: Set<String>`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/command/WangqiDocumentVisibilityCommand.java`
  - 新增 `operatorUserId: Long`
  - 新增 `operatorPermissions: Set<String>`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/command/MingCustomsCommand.java`
  - 新增 `operatorUserId: Long`
  - 新增 `operatorPermissions: Set<String>`

约束：

- 不把 System infra/mapper 类型传入 Classics。
- Controller 统一从 `CurrentUserResolver.currentAuthorities()` 读取权限，再转换为 `Set<String>`。
- Application 层只能消费 `operatorUserId` 与 `operatorPermissions`，不能依赖 System controller/security 实现类。

#### Operations cleanup detail

补充 cleanup detail 响应字段，用于 Admin Web 查看真实执行结果。

Java result/response：

- `OperationsCleanupDetailResult`
- `OperationsCleanupDetailResponse`

新增字段：

- `OperationsCleanupDetailResult.items: List<OperationsCleanupItemResult>`
- `OperationsCleanupDetailResponse.items: List<OperationsCleanupItemResponse>`

item 字段：

- `cleanupItemId: Long`
- `targetType: String`
- `targetId: Long`
- `itemStatus: String`
- `failureReason: String`
- `processedAt: LocalDateTime`

不改动 `operations_cleanup_item` 表字段，字段映射如下：

- `cleanup_item_id -> cleanupItemId`
- `target_type -> targetType`
- `target_id -> targetId`
- `item_status -> itemStatus`
- `failure_reason -> failureReason`
- `processed_at -> processedAt`

### Cleanup 目标语义

本次保留既有 cleanup 类型：

- `EXPIRED_BACKUP`
- `EXPIRED_SHARE`
- `EXPIRED_DRAFT`
- `EXPIRED_EXPORT`

`operations_cleanup_item.target_type` 固定映射：

- `EXPIRED_BACKUP -> backup`
- `EXPIRED_SHARE -> share`
- `EXPIRED_DRAFT -> draft`
- `EXPIRED_EXPORT -> export`

`operations_cleanup_item.target_id` 固定映射：

- `backup`: `operations_backup.backup_id`
- `share`: `classics_share_link.share_link_id`
- `draft`: `classics_sancai_entry_draft.draft_id`
- `export`: `classics_content_export_job.export_job_id`

执行语义：

- `EXPIRED_BACKUP`: 删除已过期 Operations backup 记录。
- `EXPIRED_SHARE`: 将已过期且仍可访问的分享标记为 `EXPIRED`。
- `EXPIRED_DRAFT`: 删除超过保留期的 Sancai draft。
- `EXPIRED_EXPORT`: 将已过期 export job 标记为 `EXPIRED`。

已确认决策：

- `EXPIRED_DRAFT` 默认保留期为 30 天，按 `autosaved_at < now - 30 days` 判断。
- `EXPIRED_SHARE` 只标记 `EXPIRED`，不物理删除。
- `EXPIRED_EXPORT` 只标记 export job 为 `EXPIRED`，不删除 Storage 对象。
- Classics 私有内容权限过滤本次只覆盖 Admin 侧，不引入 Portal 登录态。
- Operations cleanup 执行权限统一为 `operations:cleanup:execute`。

## 权限规则

### Classics 内容类型权限

内容类型到权限码映射：

- `SANCAI_ENTRY`
  - view: `classics:sancai:view`
  - edit/status: `classics:sancai:edit`
  - delete: `classics:sancai:delete`
- `WANGQI_DOCUMENT`
  - view: `classics:wangqi:view`
  - edit/status: `classics:wangqi:edit`
  - delete: `classics:wangqi:delete`
- `MING_CUSTOMS`
  - view: `classics:mingcustoms:view`
  - edit/status: `classics:mingcustoms:edit`
  - delete: `classics:mingcustoms:delete`

公共内容：

- Admin 查询仍要求对应 `view` 权限。
- Portal 公共查询不新增 System 权限要求。

私有内容：

- Admin 查询私有内容必须有对应内容类型 `view` 权限。
- 批量公开/私有必须有对应内容类型 `edit` 权限。
- 导出私有内容必须同时具备对应内容类型 `view` 权限与 `classics:content:export`。
- 分享私有内容必须同时具备对应内容类型 `view` 权限与 `classics:sharing:edit`。

批量操作结果：

- 对无权限目标返回单项失败。
- 单项失败原因使用稳定码：`PERMISSION_DENIED`。
- 批量接口不因部分目标无权限整体失败，除非请求本身非法或调用者缺少入口权限。

### Operations cleanup 权限

Admin cleanup 执行入口使用：

- `operations:cleanup:execute`

Admin cleanup 查看入口使用：

- `operations:cleanup:view`

修正 System 种子后，Admin Web 现有 `hasPermission("operations:cleanup:execute")` 与后端 `@HasPermission` 保持一致。

## 任务拆分

### 任务 1: System 权限种子对齐

目标：修正 Operations cleanup 执行权限码，保证菜单种子、后端注解、Admin Web 权限判断一致。

涉及文件：

- `db/data-source/system.json`
- `db/data/system.sql`

执行要点：

- 修改“清理维护”菜单 `perms` 字段。
- 重新生成或手动同步 `db/data/system.sql`。
- 验证 `operations:cleanup:execute` 在 SQL 种子中存在。

验证：

- `node scripts/generate-system-data-sql.ts --check`

### 任务 2: Classics 权限上下文字段与策略基础

目标：建立 Classics application 层可复用的内容类型权限判断，并给内容导出、分享命令补充权限上下文字段。

涉及文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsContentPermissionSupport.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/command/ContentExportCommand.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/command/ShareLinkCreateCommand.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/command/BatchShareCreateCommand.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsContentPermissionSupportTest.java`

执行要点：

- 新增 `ClassicsContentPermissionSupport`。
- 在三个 command 文件中新增 `operatorUserId` 与 `operatorPermissions` 字段。
- 权限支持类统一维护 content type 到 view/edit 权限码映射。

验证：

- `cd kuzhambu-servers && mvn -pl biz/classics/kuzhambu-classics-application -am spotless:apply`
- `cd kuzhambu-servers && mvn -pl biz/classics/kuzhambu-classics-application -am spotless:check checkstyle:check -DskipTests compile`

### 任务 3: Classics 内容导出与分享权限过滤

目标：让内容导出、单条分享、批量分享按权限过滤或拒绝。

涉及文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/service/impl/ClassicsSharingApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sharing/ClassicsSharingApplicationServiceImplTest.java`

执行要点：

- 为导出、分享增加权限过滤或权限拒绝。
- 保持 Portal 公共访问路径不依赖 System 权限。
- 对私有内容缺少权限的场景增加单元测试。

验证：

- `cd kuzhambu-servers && mvn -pl biz/classics/kuzhambu-classics-application -am spotless:apply`
- `cd kuzhambu-servers && mvn -pl biz/classics/kuzhambu-classics-application -am spotless:check checkstyle:check -DskipTests compile`

### 任务 4: Classics Sancai 批量状态权限闭环

目标：Sancai 批量公开/私有接口按 `classics:sancai:edit` 逐项过滤，并返回成功数、失败数、失败原因。

涉及文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/query/SancaiEntryPageQuery.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiEntryStatusCommand.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/SancaiAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/SancaiApplicationServiceImplTest.java`

执行要点：

- Controller 传入 `operatorUserId` 与 `operatorPermissions`。
- Application 按 `classics:sancai:edit` 做逐项判断。
- 无权限项不更新数据库，结果中记录失败。
- 若调用者完全缺少入口权限，后端注解仍可直接拒绝。

验证：

- `cd kuzhambu-servers && mvn -pl biz/classics/kuzhambu-classics-interface -am spotless:apply`
- `cd kuzhambu-servers && mvn -pl biz/classics/kuzhambu-classics-interface -am spotless:check checkstyle:check -DskipTests compile`

### 任务 5: Classics Wangqi 批量状态权限闭环

目标：Wangqi 批量公开/私有接口按 `classics:wangqi:edit` 逐项过滤。

涉及文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/service/impl/WangqiDocumentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/query/WangqiDocumentPageQuery.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/command/WangqiDocumentVisibilityCommand.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/WangqiDocumentAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/wangqi/WangqiDocumentApplicationServiceImplTest.java`

执行要点：

- Wangqi 使用 `classics:wangqi:edit`。
- 无权限项不更新数据库，失败原因使用 `PERMISSION_DENIED`。

验证：

- `cd kuzhambu-servers && mvn -pl biz/classics/kuzhambu-classics-interface -am spotless:apply`
- `cd kuzhambu-servers && mvn -pl biz/classics/kuzhambu-classics-interface -am spotless:check checkstyle:check -DskipTests compile`

### 任务 6: Classics Ming Customs 批量状态权限闭环

目标：Ming Customs 批量公开/私有接口按 `classics:mingcustoms:edit` 逐项过滤。

涉及文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/service/impl/MingCustomsApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/query/MingCustomsPageQuery.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/command/MingCustomsCommand.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/MingCustomsAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/mingcustoms/MingCustomsApplicationServiceImplTest.java`

执行要点：

- Ming Customs 使用 `classics:mingcustoms:edit`。
- 无权限项不更新数据库，失败原因使用 `PERMISSION_DENIED`。

验证：

- `cd kuzhambu-servers && mvn -pl biz/classics/kuzhambu-classics-interface -am spotless:apply`
- `cd kuzhambu-servers && mvn -pl biz/classics/kuzhambu-classics-interface -am spotless:check checkstyle:check -DskipTests compile`

### 任务 7: Classics cleanup facade

目标：为 Operations 提供跨域 cleanup 目标发现与执行接口，避免 Operations 直接访问 Classics repository/mapper。

涉及文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/ClassicsFacade.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/request/ClassicsCleanupTargetsFacadeRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/response/ClassicsCleanupTargetsFacadeResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/response/ClassicsCleanupExecutionFacadeResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/facade/ClassicsFacadeImpl.java`

执行要点：

- facade 只暴露 cleanup type、target id、执行结果，不暴露 Classics 内部 entity。
- 支持 `EXPIRED_SHARE`、`EXPIRED_DRAFT`、`EXPIRED_EXPORT`。
- 对未知 cleanup type 返回失败或空结果，不能抛出不透明异常给 Operations。

验证：

- `cd kuzhambu-servers && mvn -pl biz/classics/kuzhambu-classics-facade,biz/classics/kuzhambu-classics-interface -am spotless:apply`
- `cd kuzhambu-servers && mvn -pl biz/classics/kuzhambu-classics-facade,biz/classics/kuzhambu-classics-interface -am spotless:check checkstyle:check -DskipTests compile`

### 任务 8: Classics cleanup application

目标：实现 Classics 三类 cleanup 的真实业务动作。

涉及文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/cleanup/service/ClassicsCleanupApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/cleanup/service/impl/ClassicsCleanupApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sharing/repository/ClassicsSharingRepository.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiAssetRepository.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/repository/ClassicsContentRepository.java`

执行要点：

- 分享 cleanup：查找 `expires_at <= now` 且 `status` 仍为可访问状态的 share link，执行后置为 `EXPIRED`。
- Draft cleanup：查找 `autosaved_at < now - retentionDays` 的 draft，执行删除。
- Export cleanup：查找 `expires_at <= now` 且未过期的 export job，执行 `markExportJobExpired`。
- 每个 target 单独执行，单个失败不阻断后续 target。

验证：

- `cd kuzhambu-servers && mvn -pl biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-infra -am spotless:apply`
- `cd kuzhambu-servers && mvn -pl biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-infra -am spotless:check checkstyle:check -DskipTests compile`

### 任务 9: Classics cleanup sharing/draft infra 与 mapper

目标：补齐 expired share 与 expired draft cleanup 所需 repository 实现与 SQL。

涉及文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/repository/impl/ClassicsSharingRepositoryImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiAssetRepositoryImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/persistence/mapper/ClassicsShareLinkMapper.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/mapper/SancaiEntryDraftMapper.java`

执行要点：

- 分享查询条件包含 `expires_at <= now` 与可访问状态。
- Draft 查询条件包含 `autosaved_at < now - 30 days`。

验证：

- `cd kuzhambu-servers && mvn -pl biz/classics/kuzhambu-classics-infra -am spotless:apply`
- `cd kuzhambu-servers && mvn -pl biz/classics/kuzhambu-classics-infra -am spotless:check checkstyle:check -DskipTests compile`

### 任务 10: Classics cleanup export infra 与 mapper

目标：补齐 expired export cleanup 所需 repository 实现与 SQL。

涉及文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/repository/impl/ClassicsContentRepositoryImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/persistence/mapper/ClassicsContentMapper.java`

执行要点：

- Export 查询条件包含 `expires_at <= now` 与未过期状态。
- 执行时复用或补齐 `markExportJobExpired`。
- 不删除 `storage_object_id` 指向的 Storage 对象。

验证：

- `cd kuzhambu-servers && mvn -pl biz/classics/kuzhambu-classics-infra -am spotless:apply`
- `cd kuzhambu-servers && mvn -pl biz/classics/kuzhambu-classics-infra -am spotless:check checkstyle:check -DskipTests compile`

### 任务 11: Operations cleanup executor 真实执行

目标：替换 `CleanupApplicationServiceImpl` 中的占位 discover/execute，写入真实 item 明细与 job 汇总。

涉及文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/service/impl/CleanupApplicationServiceImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/support/OperationsCleanupSupport.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/backup/repository/BackupRepository.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/backup/repository/impl/BackupRepositoryImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/cleanup/service/impl/CleanupApplicationServiceImplTest.java`

执行要点：

- `discoverCleanupItems` 真实返回 target 列表。
- `executeCleanupItem` 对每个 target 执行真实动作。
- `CleanupJob.totalCount/successCount/failedCount/failureReason/completedAt` 按执行结果更新。
- `CleanupItem.itemStatus/failureReason/processedAt` 每项落库。
- `EXPIRED_BACKUP` 使用 Operations 自己的 backup repository。
- `EXPIRED_SHARE`、`EXPIRED_DRAFT`、`EXPIRED_EXPORT` 通过 `ClassicsFacade`。

验证：

- `cd kuzhambu-servers && mvn -pl biz/operations/kuzhambu-operations-application,biz/operations/kuzhambu-operations-infra -am spotless:apply`
- `cd kuzhambu-servers && mvn -pl biz/operations/kuzhambu-operations-application,biz/operations/kuzhambu-operations-infra -am spotless:check checkstyle:check -DskipTests compile`

### 任务 12: Operations cleanup detail API

目标：后端 detail API 返回 cleanup item 明细，失败项可定位 target 与原因。

涉及文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/cleanup/controller/OperationsCleanupAdminController.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/cleanup/assembler/OperationsCleanupInterfaceAssembler.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/cleanup/controller/response/OperationsCleanupDetailResponse.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/result/OperationsCleanupDetailResult.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/cleanup/controller/OperationsCleanupAdminControllerTest.java`

执行要点：

- detail API 返回 `items`。
- `items` 字段元素包含 `cleanupItemId`、`targetType`、`targetId`、`itemStatus`、`failureReason`、`processedAt`。

验证：

- `cd kuzhambu-servers && mvn -pl biz/operations/kuzhambu-operations-interface -am spotless:apply`
- `cd kuzhambu-servers && mvn -pl biz/operations/kuzhambu-operations-interface -am spotless:check checkstyle:check -DskipTests compile`

### 任务 13: Admin Web Operations cleanup 控件闭环

目标：Admin Web 能发起真实 cleanup，查看 job 与 item 结果。

涉及文件：

- `kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-types.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-service.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-page.css`
- `kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-service-contract.test.ts`

前端控件与操作：

- `cleanupTypeOptions` 下拉菜单：保留 `EXPIRED_BACKUP`、`EXPIRED_SHARE`、`EXPIRED_DRAFT`、`EXPIRED_EXPORT` 四项。
- “执行清理”下拉按钮：仅 `hasPermission("operations:cleanup:execute")` 为 true 时可点击；mutation pending 时显示 loading 并禁止重复提交。
- 执行确认弹窗：确认文案包含所选 cleanup type；确认后调用 `/operations/cleanup/execute`。
- cleanup 记录表“详情”按钮：点击后打开 detail drawer，并调用 `/operations/cleanup/detail`。
- cleanup 记录表“失败项”按钮：仅当 `failedCount > 0` 时可点击；点击后打开 detail drawer 并自动定位失败项区域。
- detail drawer 基础信息区：展示 `cleanupId`、`cleanupType`、`cleanupStatus`、`totalCount`、`successCount`、`failedCount`、`failureReason`、`startedAt`、`completedAt`。
- detail drawer item 表：展示 `targetType`、`targetId`、`itemStatus`、`failureReason`、`processedAt`；失败项用 warning/error 状态标识。
- 空 item 状态：展示“暂无清理明细”，不报错。

验证：

- `cd kuzhambu-apps && npm --workspace admin-web run format`
- `cd kuzhambu-apps && npm --workspace admin-web run format:check`
- `cd kuzhambu-apps && npm --workspace admin-web run lint`
- `cd kuzhambu-apps && npm --workspace admin-web run build`

### 任务 14: Admin Web Classics 权限状态对齐

目标：前端入口与后端权限规则一致，减少无权限批量操作的误触发。

涉及文件：

- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-content-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-list.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`

前端控件与操作：

- Sancai entry 列表“批量公开”按钮：需要选中至少一条记录，且 `classics:sancai:edit` 为 true；无权限时 disabled。
- Sancai entry 列表“批量私有”按钮：需要选中至少一条记录，且 `classics:sancai:edit` 为 true；无权限时 disabled。
- Sancai entry 列表批量结果 Alert：继续展示 `successCount`、`failureCount` 与 failures；`PERMISSION_DENIED` 失败项必须可见。
- Sancai entry 行级“分享”操作：需要 `classics:sharing:edit`；对私有内容还需要 `classics:sancai:view`。
- Sancai entry 行级“导出”操作：需要 `classics:content:export`；对私有内容还需要 `classics:sancai:view`。
- Wangqi 页面“批量公开/批量私有”操作：需要 `classics:wangqi:edit`；结果区展示 `PERMISSION_DENIED` 失败项。
- Ming Customs 页面“批量公开/批量私有”操作：需要 `classics:mingcustoms:edit`；结果区展示 `PERMISSION_DENIED` 失败项。
- 前端只做控件状态与反馈优化，后端仍是权限事实来源。

验证：

- `cd kuzhambu-apps && npm --workspace admin-web run format`
- `cd kuzhambu-apps && npm --workspace admin-web run format:check`
- `cd kuzhambu-apps && npm --workspace admin-web run lint`
- `cd kuzhambu-apps && npm --workspace admin-web run build`

### 任务 15: Portal Web 回归确认

目标：确认 Portal Web 公开内容、公开分享、搜索展示不因 System 权限过滤受影响。

涉及文件：

- `kuzhambu-apps/portal-web/src/pages/share/share-page.tsx`
- `kuzhambu-apps/portal-web/src/pages/share/share-form.tsx`
- `kuzhambu-apps/portal-web/src/pages/discovery/search-page.tsx`
- `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-home-page.tsx`
- `kuzhambu-apps/portal-web/src/app.tsx`

前端控件与操作：

- 默认不修改 Portal Web。
- Share 页面 token 输入框与查询按钮：公开分享仍可查询。
- Share 页面结果展示区：公开分享仍展示标题、内容摘要、来源信息。
- Discovery 搜索框与搜索结果列表：公开内容仍可搜索。
- Knowledge home 内容入口：公开内容卡片仍可进入详情或搜索。
- 不在本闭环引入 Portal 登录态。
- 如果后端 response 字段或错误码影响 Portal Web，只做最小兼容调整。

验证：

- `cd kuzhambu-apps && npm --workspace portal-web run format:check`
- `cd kuzhambu-apps && npm --workspace portal-web run lint`
- `cd kuzhambu-apps && npm --workspace portal-web run build`

### 任务 16: Coverage 与现场清理

目标：实现完成后更新进度文档，并删除本 RUNBOOK。

涉及文件：

- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
- `docs/40-readiness/SYSTEM-IMPLEMENTATION-COVERAGE.md`
- `docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md`
- `docs/30-designs/RUNBOOK-CLASSICS-PERMISSION-OPERATIONS-CLEANUP-CLOSURE.md`

执行要点：

- 将已完成闭环从未完成/部分完成调整为完成或写明残余限制。
- 删除本 RUNBOOK。
- 确认 `TODO.md` 无已完成残留任务。

验证：

- `git status --short`
- `git diff --check`

## 最终全量验证

按业务域拆开执行，避免单个任务过大。

### Servers

```sh
cd kuzhambu-servers
mvn spotless:check
mvn checkstyle:check
mvn -DskipTests compile
mvn test
```

### Admin Web

```sh
cd kuzhambu-apps
npm --workspace admin-web run format:check
npm --workspace admin-web run lint
npm --workspace admin-web run build
npm --workspace admin-web run test
```

### Portal Web

```sh
cd kuzhambu-apps
npm --workspace portal-web run format:check
npm --workspace portal-web run lint
npm --workspace portal-web run build
npm --workspace portal-web run test
```

### 数据种子

```sh
node scripts/generate-system-data-sql.ts --check
```

## 已确认结论

以下结论已由本轮审核确认，后续 TODO 与实现按此执行：

1. `EXPIRED_DRAFT` 使用 30 天默认保留期。
2. `EXPIRED_SHARE` 标记为 `EXPIRED`，不物理删除。
3. `EXPIRED_EXPORT` 标记 export job 为 `EXPIRED`，不删除 Storage 对象。
4. Classics 私有内容权限过滤只覆盖 Admin 侧，本次不引入 Portal 登录态。
5. Operations cleanup 执行权限码统一为 `operations:cleanup:execute`。
