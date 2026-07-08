# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `StorageOwnerType.java`、`ClassicsContentApplicationService.java`、`ClassicsContentRepository.java`：补齐 Storage owner 与导出删除契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-EXPORT-SHOWCASE-GOVERNANCE.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/enums/StorageOwnerType.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/ClassicsContentApplicationService.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/repository/ClassicsContentRepository.java`
    - 处理动作：新增 Classics 导出/静态展示专属 Storage owner 枚举，并声明导出记录删除服务与仓储方法。
    - 验收点：存在 `CLASSICS_CONTENT_EXPORT_JOB`、`CLASSICS_SANCAI_SHOWCASE`、`deleteExportJob(ClassicsContentExportJobId id)`、`deleteExportJobById(ClassicsContentExportJobId id)`；未修改 Operations cleanup 与 Storage orphan 清理策略。
    - 重要度：10/10

- [ ] `ClassicsContentApplicationServiceImpl.java` 等 5 文件：实现导出记录主动删除闭环
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-EXPORT-SHOWCASE-GOVERNANCE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/repository/impl/ClassicsContentRepositoryImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/ClassicsContentAdminController.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceImplTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`
    - 处理动作：按“读取导出记录、解绑 owner、删除业务记录、尝试删除无引用 Storage object”的顺序实现导出记录删除。
    - 验收点：`POST /api/classics/content/exports/delete` 入参为 `id`，权限为 `classics:content:export`；Storage 引用字段为 `reference_owner_type=CLASSICS_CONTENT_EXPORT_JOB`、`reference_owner_id=export-job:{classics_content_export_job.id}`、`business_params=usage=CLASSICS_EXPORT_JOB;jobId={classics_content_export_job.id}`；Storage 仍被引用时不回滚业务记录删除。
    - 重要度：10/10

- [ ] `SancaiAssetApplicationService.java` 等 5 文件：实现静态展示回源与删除边界
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-EXPORT-SHOWCASE-GOVERNANCE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/SancaiAssetApplicationService.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiAssetRepository.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiAssetRepositoryImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/SancaiAssetAdminController.java`
    - 处理动作：按 showcase 记录 id 回源内容，并按记录级 owner 实现静态展示记录删除。
    - 验收点：`GET /api/classics/sancai/assets/showcases/{id}/content` 的 `{id}` 为 `classics_sancai_showcase.id`；`POST /api/classics/sancai/assets/showcases/delete` 入参为 `id`，权限为 `classics:sancai:edit`；Storage 引用字段为 `reference_owner_type=CLASSICS_SANCAI_SHOWCASE`、`reference_owner_id=showcase:{classics_sancai_showcase.id}`、`business_params=usage=SANCAI_SHOWCASE;showcaseId={classics_sancai_showcase.id}`。
    - 重要度：10/10

- [ ] `SancaiAssetInterfaceAssembler.java`、`SancaiAssetApplicationServiceImplTest.java`：改为记录 id 展示 URL 并锁定测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-EXPORT-SHOWCASE-GOVERNANCE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiAssetInterfaceAssembler.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/SancaiAssetApplicationServiceImplTest.java`
    - 处理动作：将静态展示 `contentUrl`、`downloadUrl` 改为使用 `classics_sancai_showcase.id`，并更新 owner 绑定断言。
    - 验收点：响应 URL 不暴露 `storage_object_id`；测试断言 `CLASSICS_SANCAI_SHOWCASE + showcase:{id}` 绑定。
    - 重要度：8/10

- [ ] `classics-export-service.ts` 等 4 文件：补齐导出记录删除服务与列表控件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-EXPORT-SHOWCASE-GOVERNANCE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/common/classics-export-service.ts`、`kuzhambu-apps/admin-web/src/pages/classics/common/classics-export-service-contract.test.ts`、`kuzhambu-apps/admin-web/src/pages/classics/common/components/classics-export-job-section.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/common/components/classics-export-job-section.css`
    - 处理动作：新增导出删除 service 契约，并在导出任务列表增加搜索、筛选、选择、单删和批删控件。
    - 验收点：`deleteById(jobId)` 请求 `POST /classics/content/exports/delete` 且 body 为 `{ id: jobId }`；列表包含关键词搜索输入框、状态下拉、仅过期复选框、单行选择框、全选当前可见复选框、单条删除按钮、批量删除按钮；下载按钮仅在 `COMPLETED`、未过期且存在 `downloadUrl` 时启用。
    - 重要度：9/10

- [ ] `sancai-entry-service.ts` 等 4 文件：补齐静态展示删除服务与列表控件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-EXPORT-SHOWCASE-GOVERNANCE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-service-contract.test.ts`、`kuzhambu-apps/admin-web/src/pages/classics/common/components/classics-showcase-job-section.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/common/components/classics-showcase-job-section.css`
    - 处理动作：新增静态展示删除 service 契约，并在静态展示任务列表增加搜索、筛选、选择、单删和批删控件。
    - 验收点：`deleteShowcase(id)` 请求 `POST /classics/sancai/assets/showcases/delete` 且 body 为 `{ id }`；列表包含关键词搜索输入框、状态下拉、单行选择框、全选当前可见复选框、单条删除按钮、批量删除按钮；内容查看和下载继续使用 showcase 记录 id。
    - 重要度：9/10

- [ ] `wangqi-page.tsx` 等 4 文件：接入三类 Admin 页面删除操作
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-EXPORT-SHOWCASE-GOVERNANCE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`
    - 处理动作：将导出/静态展示的单条删除、批量删除、危险确认和删除后刷新接入 Wangqi、Ming Customs、Sancai 页面。
    - 验收点：Wangqi 与 Ming Customs 可删除导出记录；Sancai 可删除导出记录和静态展示记录；删除确认使用 `useKuzhambuConfirm().danger`；成功后刷新对应 export/showcase 查询。
    - 重要度：9/10

- [ ] `kuzhambu-classics-*`、`admin-web`：完成窄范围验证
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-CLASSICS-EXPORT-SHOWCASE-GOVERNANCE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-domain`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface`、`kuzhambu-apps/admin-web`
    - 处理动作：执行本任务影响范围内的格式化检查、静态检查和测试。
    - 验收点：后端 `mvn -pl biz/classics/kuzhambu-classics-domain,biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-infra,biz/classics/kuzhambu-classics-interface,biz/storage/kuzhambu-storage-domain -am spotless:check checkstyle:check test` 通过；前端 `pnpm --filter ./admin-web run format:check && pnpm --filter ./admin-web run lint && pnpm --filter ./admin-web run test` 通过。
    - 重要度：10/10

- [ ] `main`、`feat/classics-export-showcase-governance`：同步最新 main 分支代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/PR-RULES.md`、`docs/00-governance/TODO-RULES.md`
    - 范围对象：`main`、`feat/classics-export-showcase-governance`
    - 处理动作：在功能实现与首轮验证完成后，将最新 `main` 同步到当前 feature 分支并处理冲突。
    - 验收点：当前 feature 分支包含最新 `main`；冲突文件只保留本任务相关判断；若同步影响 Classics/Admin/Storage/Worker 相关范围，重新执行对应窄范围验证。
    - 重要度：9/10

- [ ] `CLASSICS-IMPLEMENTATION-COVERAGE.md`：更新 Classics 实现覆盖表
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-CLASSICS-EXPORT-SHOWCASE-GOVERNANCE.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：同步记录导出记录主动删除、批量管理策略、静态展示搜索/筛选与回源边界的实现覆盖状态。
    - 验收点：Implementation Coverage 中相关 Classics export/showcase 条目与实际代码、接口和前端控件一致。
    - 重要度：10/10

- [ ] `RUNBOOK-CLASSICS-EXPORT-SHOWCASE-GOVERNANCE.md`、`TODO.md`：清理临时执行文档
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/00-governance/PR-RULES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-CLASSICS-EXPORT-SHOWCASE-GOVERNANCE.md`、`TODO.md`
    - 处理动作：任务关闭前删除临时 RUNBOOK，并从 `TODO.md` 删除已经完成的任务项。
    - 验收点：`RUNBOOK-CLASSICS-EXPORT-SHOWCASE-GOVERNANCE.md` 已移除；`TODO.md` 不保留已完成历史，只保留未关闭任务。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
