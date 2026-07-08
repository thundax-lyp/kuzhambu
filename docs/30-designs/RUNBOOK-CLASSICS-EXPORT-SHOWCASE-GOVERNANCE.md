# Classics 导出与静态展示治理 RUNBOOK

## 目标

Classics 导出记录和三才图会静态展示记录形成独立治理闭环：

- Admin 可在三类内容页面查看、搜索、筛选、下载、单条删除和批量删除导出记录。
- Admin 可在三才图会条目页查看、搜索、筛选、下载、单条删除和批量删除静态展示记录。
- 删除记录时由 Classics 主动释放对应 Storage 引用；Storage 对象只有在无其他引用时才进入删除流程。
- 不修改 Operations cleanup 的导出清理策略，不修改 Workers 临时 artifact 清理策略。

## 已确认决策

- 删除导出记录或静态展示记录时，业务记录直接删除，不做软删除状态。
- Storage 对象仍有其他引用时，Classics 记录删除成功，Storage 对象保留。
- `GET /api/classics/sancai/assets/showcases/{id}/content` 中的 `{id}` 表示 `classics_sancai_showcase.id`，不是 `storage_object.id`。
- 导出记录删除权限使用 `classics:content:export`。
- 静态展示记录删除权限使用 `classics:sancai:edit`。
- Storage owner 使用记录级 owner，不再用宽泛的 `USER + system` 表达导出或静态展示产物归属。

## 数据结构变更

本任务不新增数据库表，不新增数据库字段，不新增迁移脚本。

Storage owner 枚举新增两个取值，落在 `storage_object_reference.reference_owner_type`：

- `CLASSICS_CONTENT_EXPORT_JOB`
- `CLASSICS_SANCAI_SHOWCASE`

导出记录产物绑定规则：

- `classics_content_export_job.id` 作为导出记录 id。
- `classics_content_export_job.storage_object_id` 保存导出产物的 `storage_object.id`。
- `storage_object_reference.object_id = classics_content_export_job.storage_object_id`。
- `storage_object_reference.reference_owner_type = CLASSICS_CONTENT_EXPORT_JOB`。
- `storage_object_reference.reference_owner_id = export-job:{classics_content_export_job.id}`。
- `storage_object_reference.business_params = usage=CLASSICS_EXPORT_JOB;jobId={classics_content_export_job.id}`。

静态展示产物绑定规则：

- `classics_sancai_showcase.id` 作为静态展示记录 id。
- `classics_sancai_showcase.storage_object_id` 保存静态展示产物的 `storage_object.id`。
- `storage_object_reference.object_id = classics_sancai_showcase.storage_object_id`。
- `storage_object_reference.reference_owner_type = CLASSICS_SANCAI_SHOWCASE`。
- `storage_object_reference.reference_owner_id = showcase:{classics_sancai_showcase.id}`。
- `storage_object_reference.business_params = usage=SANCAI_SHOWCASE;showcaseId={classics_sancai_showcase.id}`。

删除后的 Storage 状态规则：

- Classics 删除记录前，按记录级 owner 删除 `storage_object_reference`。
- Storage 根据剩余引用刷新 `storage_object.reference_status`。
- 若 `storage_object.reference_status = UNREFERENCED`，Classics 可调用 Storage 删除接口推进删除。
- 若 Storage 删除接口因其他引用拒绝删除，Classics 不回滚业务记录删除。

## 后端小任务

### 任务 1：Storage owner 与导出删除契约

文件：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/enums/StorageOwnerType.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/ClassicsContentApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/repository/ClassicsContentRepository.java`

要求：

- 在 `StorageOwnerType.java` 新增 `CLASSICS_CONTENT_EXPORT_JOB` 和 `CLASSICS_SANCAI_SHOWCASE`。
- 在 `ClassicsContentApplicationService.java` 新增 `deleteExportJob(ClassicsContentExportJobId id)`。
- 在 `ClassicsContentRepository.java` 新增 `deleteExportJobById(ClassicsContentExportJobId id)`。
- 不修改 Storage orphan 阈值、物理删除调度、Operations cleanup 入口。

### 任务 2：Classics 导出记录删除实现与接口

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/repository/impl/ClassicsContentRepositoryImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/ClassicsContentAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`

要求：

- `createExportJob` 上传导出产物时使用 `CLASSICS_CONTENT_EXPORT_JOB + export-job:{jobId}`。
- 删除顺序固定为：读取导出记录、解绑 `export-job:{jobId}`、删除 `classics_content_export_job` 记录、尝试删除无引用 Storage 对象。
- 新增 HTTP 接口 `POST /api/classics/content/exports/delete`。
- 入参只需要 `id`。
- 权限固定为 `classics:content:export`。
- fake repository 补齐新增删除方法，避免接口扩展破坏既有测试。

### 任务 3：三才图会静态展示记录删除与回源边界

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/SancaiAssetApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiAssetRepository.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiAssetRepositoryImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/SancaiAssetAdminController.java`

要求：

- `requestShowcase` 上传静态展示产物时使用 `CLASSICS_SANCAI_SHOWCASE + showcase:{showcaseId}`。
- 新增 application 方法 `getShowcaseContent(SancaiShowcaseId showcaseId)`。
- `GET /api/classics/sancai/assets/showcases/{id}/content` 必须按 `classics_sancai_showcase.id` 查记录，再读取 `storage_object_id`。
- 新增 application 方法 `deleteShowcase(SancaiShowcaseId showcaseId)`。
- 删除顺序固定为：读取展示记录、解绑 `showcase:{showcaseId}`、删除 `classics_sancai_showcase` 记录、尝试删除无引用 Storage 对象。
- 新增 HTTP 接口 `POST /api/classics/sancai/assets/showcases/delete`。
- 入参只需要 `id`。
- 权限固定为 `classics:sancai:edit`。

### 任务 4：静态展示响应 URL 与后端测试

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiAssetInterfaceAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/SancaiAssetApplicationServiceImplTest.java`

要求：

- 静态展示 `contentUrl` 和 `downloadUrl` 使用 `classics_sancai_showcase.id` 生成 URL。
- 后端测试覆盖静态展示上传 owner 为 `CLASSICS_SANCAI_SHOWCASE + showcase:{showcaseId}`。

## 前端小任务

### 任务 1：导出任务 service 契约

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-export-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-export-service-contract.test.ts`

要求：

- 新增 `deleteById(jobId: number)`。
- 固定请求：`POST /classics/content/exports/delete`。
- 请求体固定为 `{ id: jobId }`。

### 任务 2：导出任务列表控件

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/common/components/classics-export-job-section.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/common/components/classics-export-job-section.css`

控件与操作：

- 搜索框：`Input.Search`
  - `aria-label = 搜索导出任务`
  - placeholder 为 `搜索任务编号、类型或范围`
  - 搜索字段包含任务编号、内容类型、导出类型、导出格式、范围类型、风险状态。
- 状态筛选：`Select`
  - `aria-label = 筛选导出任务状态`
  - 选项包含全部状态、已完成、排队中、进行中、失败、已过期。
- 过期筛选：`Checkbox`
  - 文案为 `仅过期`
  - 只在前端过滤当前列表，不调用后端改状态。
- 单条选择：每条记录显示 `Checkbox`
  - `aria-label = 选择导出任务 {id}`。
- 全选当前可见：底部 `Checkbox`
  - `aria-label = 选择全部可见导出任务`
  - 文案显示 `已选 x / y`。
- 单条删除按钮：`Button danger`
  - 文案为 `删除`
  - 调用页面传入的 `onDelete(job)`。
- 批量删除按钮：`Button danger`
  - 文案为 `删除选中`
  - 未选择记录时禁用。
  - 调用页面传入的 `onBatchDelete(jobs)`。
- 下载按钮：
  - 仅当 `status = COMPLETED`、`downloadUrl` 存在且未过期时启用。
  - 过期记录显示已过期标签，下载按钮禁用。

### 任务 3：静态展示任务 service 与列表控件

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-service-contract.test.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/components/classics-showcase-job-section.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/common/components/classics-showcase-job-section.css`

控件与操作：

- 新增 `deleteShowcase(id: number)`。
- 固定请求：`POST /classics/sancai/assets/showcases/delete`。
- 请求体固定为 `{ id }`。
- 搜索框：`Input.Search`
  - `aria-label = 搜索静态展示任务`
  - placeholder 为 `搜索任务编号或风险状态`
  - 搜索字段包含任务编号、任务状态、风险状态。
- 状态筛选：`Select`
  - `aria-label = 筛选静态展示任务状态`
  - 选项包含全部状态、已完成、排队中、进行中、失败、已过期。
- 单条选择：每条记录显示 `Checkbox`
  - `aria-label = 选择静态展示任务 {id}`。
- 全选当前可见：底部 `Checkbox`
  - `aria-label = 选择全部可见静态展示任务`
  - 文案显示 `已选 x / y`。
- 单条删除按钮：`Button danger`
  - 文案为 `删除`
  - 调用页面传入的 `onDelete(job)`。
- 批量删除按钮：`Button danger`
  - 文案为 `删除选中`
  - 未选择记录时禁用。
  - 调用页面传入的 `onBatchDelete(jobs)`。
- 下载按钮：
  - 仅当 `status = COMPLETED` 且 `downloadUrl` 存在时启用。

### 任务 4：Admin 三类内容页面接线

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`

控件与操作：

- 王圻文档页面：
  - 导出任务区启用单条删除和批量删除。
  - 删除确认弹窗使用 `useKuzhambuConfirm().danger`。
  - 单条删除确认标题为 `删除导出记录`。
  - 批量删除确认标题为 `批量删除导出记录`。
  - 删除成功后刷新 `["classics", "wangqi", "exports", "jobs"]`。
- 明代习俗页面：
  - 导出任务区启用单条删除和批量删除。
  - 删除确认弹窗使用 `useKuzhambuConfirm().danger`。
  - 删除成功后刷新 `["classics", "ming-customs", "exports", "jobs"]`。
- 三才图会条目页：
  - 导出任务区启用单条删除和批量删除。
  - 静态展示任务区启用单条删除和批量删除。
  - 删除确认弹窗使用 `useKuzhambuConfirm().danger`。
  - 导出删除成功后刷新 `["classics", "sancai", "exports", "jobs"]`。
  - 静态展示删除成功后刷新 `["classics", "sancai", "showcases", "jobs"]`。
- 权限：
  - 导出任务删除控件只在页面具备 Classics export 权限时传入删除处理函数。
  - 三才图会静态展示删除控件复用 `SANCAI_ENTRY export` 权限判断。

## 验证清单

- 创建三类内容导出后，Admin 导出任务列表可按任务编号、类型、范围、状态和过期条件筛选。
- 点击导出任务 `删除` 后，弹出危险确认；确认后列表不再返回该记录。
- 点击导出任务 `删除选中` 后，只删除当前已选记录。
- 删除导出记录后，对应 Storage 引用 owner 不再包含 `CLASSICS_CONTENT_EXPORT_JOB + export-job:{jobId}`。
- 如果对应 Storage 对象无其他引用，Storage 删除流程可推进。
- 如果对应 Storage 对象仍有其他引用，导出记录删除成功，Storage 对象保留。
- 创建三才图会静态展示后，Admin 静态展示任务列表可按任务编号、风险状态和状态筛选。
- 静态展示下载 URL 使用展示记录 id 回源，不能绕过记录直接暴露 Storage object id。
- 点击静态展示任务 `删除` 后，弹出危险确认；确认后列表不再返回该记录。
- 点击静态展示任务 `删除选中` 后，只删除当前已选记录。
- 删除静态展示记录后，对应 Storage 引用 owner 不再包含 `CLASSICS_SANCAI_SHOWCASE + showcase:{showcaseId}`。
- 前端批量删除不修改内容公开/私有状态、分享链接状态或 Operations cleanup 策略。

## 验证命令

后端：

```sh
cd kuzhambu-servers
mvn -pl biz/classics/kuzhambu-classics-domain,biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-infra,biz/classics/kuzhambu-classics-interface,biz/storage/kuzhambu-storage-domain -am spotless:check checkstyle:check test
```

前端：

```sh
cd kuzhambu-apps
pnpm --filter ./admin-web run format:check
pnpm --filter ./admin-web run lint
pnpm --filter ./admin-web run test
```
