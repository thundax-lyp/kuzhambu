# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `classics-permission-context-support`：Classics 权限上下文字段与策略基础
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-PERMISSION-OPERATIONS-CLEANUP-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsContentPermissionSupport.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/command/ContentExportCommand.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/command/ShareLinkCreateCommand.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/command/BatchShareCreateCommand.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsContentPermissionSupportTest.java`
    - 处理动作：新增 Classics 内容权限支持类，并给导出与分享命令补充 `operatorUserId`、`operatorPermissions` 字段。
    - 验收点：权限支持类可按内容类型返回 view/edit 权限判断，相关命令暴露新增字段并通过 application 模块编译。
    - 重要度：10/10

- [ ] `classics-content-share-permission-filter`：Classics 导出与分享权限过滤
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-PERMISSION-OPERATIONS-CLEANUP-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/service/impl/ClassicsSharingApplicationServiceImpl.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceImplTest.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sharing/ClassicsSharingApplicationServiceImplTest.java`
    - 处理动作：为内容导出、单条分享、批量分享增加私有内容权限过滤或权限拒绝。
    - 验收点：缺少内容 view 或分享/export 权限时私有目标不可导出或分享，且 Portal 公共访问路径不受影响。
    - 重要度：10/10

- [ ] `classics-sancai-batch-visibility-permission`：Sancai 批量状态权限闭环
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-PERMISSION-OPERATIONS-CLEANUP-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiApplicationServiceImpl.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/query/SancaiEntryPageQuery.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiEntryStatusCommand.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/SancaiAdminController.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/SancaiApplicationServiceImplTest.java`
    - 处理动作：让 Sancai 管理查询与批量公开/私有按 `classics:sancai:view/edit` 接收并应用权限上下文。
    - 验收点：无 `classics:sancai:edit` 的批量目标返回 `PERMISSION_DENIED` 且不更新数据库。
    - 重要度：10/10

- [ ] `classics-wangqi-batch-visibility-permission`：Wangqi 批量状态权限闭环
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-PERMISSION-OPERATIONS-CLEANUP-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/service/impl/WangqiDocumentApplicationServiceImpl.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/query/WangqiDocumentPageQuery.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/command/WangqiDocumentVisibilityCommand.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/WangqiDocumentAdminController.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/wangqi/WangqiDocumentApplicationServiceImplTest.java`
    - 处理动作：让 Wangqi 管理查询与批量公开/私有按 `classics:wangqi:view/edit` 接收并应用权限上下文。
    - 验收点：无 `classics:wangqi:edit` 的批量目标返回 `PERMISSION_DENIED` 且不更新数据库。
    - 重要度：10/10

- [ ] `classics-mingcustoms-batch-visibility-permission`：Ming Customs 批量状态权限闭环
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-PERMISSION-OPERATIONS-CLEANUP-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/service/impl/MingCustomsApplicationServiceImpl.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/query/MingCustomsPageQuery.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/command/MingCustomsCommand.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/MingCustomsAdminController.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/mingcustoms/MingCustomsApplicationServiceImplTest.java`
    - 处理动作：让 Ming Customs 管理查询与批量公开/私有按 `classics:mingcustoms:view/edit` 接收并应用权限上下文。
    - 验收点：无 `classics:mingcustoms:edit` 的批量目标返回 `PERMISSION_DENIED` 且不更新数据库。
    - 重要度：10/10

- [ ] `classics-cleanup-facade`：Classics cleanup facade
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-PERMISSION-OPERATIONS-CLEANUP-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/ClassicsFacade.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/request/ClassicsCleanupTargetsFacadeRequest.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/response/ClassicsCleanupTargetsFacadeResponse.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/response/ClassicsCleanupExecutionFacadeResponse.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/facade/ClassicsFacadeImpl.java`
    - 处理动作：为 Operations 提供 Classics cleanup 目标发现与执行 facade。
    - 验收点：facade 支持 `EXPIRED_SHARE`、`EXPIRED_DRAFT`、`EXPIRED_EXPORT` 且不暴露 Classics 内部 entity。
    - 重要度：9/10

- [ ] `classics-cleanup-application`：Classics cleanup application
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-PERMISSION-OPERATIONS-CLEANUP-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/cleanup/service/ClassicsCleanupApplicationService.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/cleanup/service/impl/ClassicsCleanupApplicationServiceImpl.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sharing/repository/ClassicsSharingRepository.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiAssetRepository.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/repository/ClassicsContentRepository.java`
    - 处理动作：实现 Classics 过期分享、过期草稿、过期导出任务的 application cleanup 编排。
    - 验收点：每个 cleanup target 独立执行，单项失败不阻断后续目标。
    - 重要度：9/10

- [ ] `classics-cleanup-sharing-draft-infra`：Classics cleanup sharing/draft infra
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-PERMISSION-OPERATIONS-CLEANUP-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/repository/impl/ClassicsSharingRepositoryImpl.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiAssetRepositoryImpl.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/persistence/mapper/ClassicsShareLinkMapper.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/mapper/SancaiEntryDraftMapper.java`
    - 处理动作：补齐 expired share 与 expired draft 的 repository 实现和 mapper 查询/更新。
    - 验收点：share 按 `expires_at <= now` 标记 `EXPIRED`，draft 按 `autosaved_at < now - 30 days` 删除。
    - 重要度：9/10

- [ ] `classics-cleanup-export-infra`：Classics cleanup export infra
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-PERMISSION-OPERATIONS-CLEANUP-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/repository/impl/ClassicsContentRepositoryImpl.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/persistence/mapper/ClassicsContentMapper.java`
    - 处理动作：补齐 expired export 的 repository 实现和 mapper 查询/更新。
    - 验收点：export job 按 `expires_at <= now` 标记 `EXPIRED`，且不删除 Storage 对象。
    - 重要度：8/10

- [ ] `operations-cleanup-executor`：Operations cleanup 真实执行
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-PERMISSION-OPERATIONS-CLEANUP-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/service/impl/CleanupApplicationServiceImpl.java`, `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/support/OperationsCleanupSupport.java`, `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/backup/repository/BackupRepository.java`, `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/backup/repository/impl/BackupRepositoryImpl.java`, `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/cleanup/service/impl/CleanupApplicationServiceImplTest.java`
    - 处理动作：替换 cleanup 占位 discover/execute，真实执行 backup 与 Classics cleanup 并写入 job/item 结果。
    - 验收点：`CleanupJob` 汇总字段与 `CleanupItem` 明细字段按真实执行结果落库。
    - 重要度：10/10

- [ ] `operations-cleanup-detail-api`：Operations cleanup detail API
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-PERMISSION-OPERATIONS-CLEANUP-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/cleanup/controller/OperationsCleanupAdminController.java`, `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/cleanup/assembler/OperationsCleanupInterfaceAssembler.java`, `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/cleanup/controller/response/OperationsCleanupDetailResponse.java`, `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/cleanup/result/OperationsCleanupDetailResult.java`, `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/cleanup/controller/OperationsCleanupAdminControllerTest.java`
    - 处理动作：让 cleanup detail API 返回 item 明细。
    - 验收点：detail response 包含 `items`，每项包含 `cleanupItemId`、`targetType`、`targetId`、`itemStatus`、`failureReason`、`processedAt`。
    - 重要度：9/10

- [ ] `admin-web-operations-cleanup-controls`：Admin Web cleanup 控件闭环
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-PERMISSION-OPERATIONS-CLEANUP-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-types.ts`, `kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-service.ts`, `kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-page.tsx`, `kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-page.css`, `kuzhambu-apps/admin-web/src/pages/operations/cleanup/cleanup-service-contract.test.ts`
    - 处理动作：补齐 cleanup 执行按钮、详情 drawer、失败项入口与 item 表展示。
    - 验收点：Admin Web 可按权限执行四类 cleanup，并在 detail drawer 查看失败项 target 与原因。
    - 重要度：9/10

- [ ] `admin-web-classics-permission-controls`：Admin Web Classics 权限控件对齐
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-PERMISSION-OPERATIONS-CLEANUP-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/common/classics-content-types.ts`, `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-list.tsx`, `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.tsx`, `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.tsx`, `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`
    - 处理动作：按内容类型权限禁用或启用批量公开/私有、分享、导出控件并展示失败反馈。
    - 验收点：无权限时相关按钮不可误触发，批量结果区可见 `PERMISSION_DENIED` 失败项。
    - 重要度：9/10

- [ ] `portal-web-classics-public-regression`：Portal Web 公共访问回归
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-PERMISSION-OPERATIONS-CLEANUP-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/share/share-page.tsx`, `kuzhambu-apps/portal-web/src/pages/share/share-form.tsx`, `kuzhambu-apps/portal-web/src/pages/discovery/search-page.tsx`, `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-home-page.tsx`, `kuzhambu-apps/portal-web/src/app.tsx`
    - 处理动作：确认并最小兼容 Portal 公开分享、公开搜索、Knowledge 入口不受 Admin 权限过滤影响。
    - 验收点：Portal 不引入登录态，公开分享查询与公开内容搜索仍可用。
    - 重要度：8/10

- [ ] `servers-full-validation`：Servers 全量验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-PERMISSION-OPERATIONS-CLEANUP-CLOSURE.md`
    - 范围对象：`kuzhambu-servers`
    - 处理动作：执行 servers 全量 format/checkstyle/compile/test 验证。
    - 验收点：`mvn spotless:check`, `mvn checkstyle:check`, `mvn -DskipTests compile`, `mvn test` 均通过。
    - 重要度：10/10

- [ ] `admin-web-full-validation`：Admin Web 全量验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-PERMISSION-OPERATIONS-CLEANUP-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web`
    - 处理动作：执行 Admin Web 全量 format/lint/build/test 验证。
    - 验收点：`npm --workspace admin-web run format:check`, `lint`, `build`, `test` 均通过。
    - 重要度：9/10

- [ ] `portal-web-full-validation`：Portal Web 全量验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-PERMISSION-OPERATIONS-CLEANUP-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/portal-web`
    - 处理动作：执行 Portal Web 全量 format/lint/build/test 验证。
    - 验收点：`npm --workspace portal-web run format:check`, `lint`, `build`, `test` 均通过。
    - 重要度：9/10

- [ ] `system-data-seed-validation`：System 数据种子验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-PERMISSION-OPERATIONS-CLEANUP-CLOSURE.md`
    - 范围对象：`db/data-source/system.json`, `db/data/system.sql`, `scripts/generate-system-data-sql.ts`
    - 处理动作：执行 System 数据种子一致性检查。
    - 验收点：`node scripts/generate-system-data-sql.ts --check` 通过。
    - 重要度：9/10

- [ ] `coverage-runbook-cleanup`：Implementation Coverage 与 RUNBOOK 收口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-PERMISSION-OPERATIONS-CLEANUP-CLOSURE.md`, `docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`, `docs/40-readiness/SYSTEM-IMPLEMENTATION-COVERAGE.md`, `docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md`, `docs/30-designs/RUNBOOK-CLASSICS-PERMISSION-OPERATIONS-CLEANUP-CLOSURE.md`
    - 处理动作：更新三个 Implementation Coverage 并删除已完成 RUNBOOK。
    - 验收点：Coverage 反映本闭环完成状态，RUNBOOK 被清理，`TODO.md` 无已完成残留项。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
