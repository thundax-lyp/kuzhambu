# RUNBOOK Classics Wangqi Admin Web

## 目标

本分支目标是完成王圻文档 Admin Web 管理闭环，让后台用户能从菜单进入 `/classics/wangqi` 页面，并完成文档列表、筛选、时间线视图、详情阅读、新增、编辑、删除、可见性维护、原始文件上传/下载、版本历史、版本对比和历史恢复。

本 RUNBOOK 是执行手册，不是长期设计文档。任务完成并合并前应删除本文件，并把稳定完成状态沉淀到 `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`。

## 当前基线

已存在后端表：

- `db/schema/classics.sql`
    - `classics_wangqi_document`
        - `id bigint`
        - `title varchar(255)`
        - `summary text`
        - `content_format varchar(16)`
        - `content longtext`
        - `document_time datetime(3)`
        - `storage_object_id bigint`
        - `visibility varchar(16)`
        - `current_version_id bigint`
        - `current_version_no int`
        - `current_versioned_at datetime(3)`
        - `content_updated_at datetime(3)`

已存在后端 Admin API：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/WangqiDocumentAdminController.java`
    - `POST /api/classics/wangqi/documents/page`
    - `GET /api/classics/wangqi/documents/{id}`
    - `POST /api/classics/wangqi/documents/timeline/list`
    - `POST /api/classics/wangqi/documents/add`
    - `POST /api/classics/wangqi/documents/update`
    - `POST /api/classics/wangqi/documents/delete`

已存在 Storage Admin API：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/StorageObjectController.java`
    - `POST /api/storage/object/upload`
    - `GET /api/storage/object/{id}/content`

参考模式：

- 用户头像使用业务侧接口封装上传和读取：
    - 前端调用 `POST /api/sys/user/avatar/upload`
    - 后端 `UserController` 在 System 侧处理权限、业务归属和文件读取。
- 王圻原始文件应采用同样模式：
    - Storage 仍是统一资源查看页面和底层对象能力。
    - Wangqi 提供自己的上传/下载 API，权限挂在 `classics:wangqi:view/edit`。
    - 前端只调用 Wangqi API，不直接调用 Storage 页面 API。

已存在通用版本能力：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/ClassicsContentApplicationService.java`
    - `listVersions(String contentType, ClassicsContentId contentId)`
    - `getVersion(ClassicsContentVersionId id)`
    - `restoreHistoryVersion(ClassicsContentVersionId versionId)`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/WangqiDocumentVersionSnapshot.java`
    - 王圻正式版本快照字段已覆盖 `title`、`summary`、`contentFormat`、`content`、`documentTime`、`storageObjectId`、`visibility`。

已存在菜单和权限 seed：

- `db/data/system.sql`
    - 菜单 `王圻文档`，URL `/classics/wangqi`
    - 权限 `classics:wangqi:view`
    - 权限 `classics:wangqi:edit`
    - 权限 `classics:wangqi:delete`
- `db/data-source/system.json`
    - 同步维护菜单和权限来源数据。

当前缺口：

- `kuzhambu-apps/admin-web/src/router/index.tsx` 尚未注册 `/classics/wangqi` 页面组件。
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/` 尚不存在。
- `db/data/classics.sql` 没有王圻文档初始化数据，dev.env 页面冒烟可能没有可浏览记录。
- `scripts/verify-classics.sh` 只校验三才图会初始化数据，没有校验王圻文档初始化数据。
- `WangqiDocumentAdminControllerTest` 仅校验 controller 类型存在，缺少请求契约烟测。
- `WangqiDocumentAdminController` 尚未暴露王圻文档版本历史、版本详情和历史恢复端点。
- `listTimeline` 后端查询需要与 page 查询一样支持 `keyword`，用于前端按标题、摘要和正文过滤时间线。
- Admin Web Storage 页面已有对象上传和读取能力，但 Wangqi 需要自己的原始文件上传/下载业务入口。

## 本轮范围

### 后端范围

本轮优先不新增 schema 字段，不修改 `classics_wangqi_document` 表结构。若实现版本恢复时发现现有 repository 不能完整回写王圻文档，应优先补齐应用服务和 repository 方法，不新增表字段。

需要发生的数据变更：

- 在 `db/data/classics.sql` 增加最少 2 条 `classics_wangqi_document` 初始化数据：
    - 1 条 `PUBLIC`
    - 1 条 `PRIVATE`
    - 覆盖 `MARKDOWN` 和 `HTML` 两类 `content_format`
    - `content_updated_at` 必须显式赋值
    - `storage_object_id` 可以为 `NULL`

需要发生的测试/校验变更：

- 在 `scripts/verify-classics.sh` 增加 `classics_wangqi_document` 初始化数据存在性校验。
- 补强 `WangqiDocumentAdminControllerTest`，覆盖 page/get/timeline/add/update/delete 的请求路径和基础请求体。
- 补充 `WangqiDocumentAdminControllerTest` 版本接口测试，覆盖版本列表、版本详情和恢复历史版本。

需要发生的后端接口变更：

- 响应协议边界：
    - JSON/API 数据请求遵循项目通用响应协议，业务失败通过 `COMMON-xxxxx` 表达，HTTP 状态保持通用协议约定的 `200`。
    - 本轮新增的 JSON/API 协议请求统一使用 `POST`，不新增协议型 `GET`。
    - 资源流 GET 请求用于 `<img>`、预览或下载时，不走 JSON 包装；认证失败可返回 `401`，资源不存在、未绑定或不可读返回 `404`。
    - 本轮资源流接口仅指 `GET /api/classics/wangqi/documents/{id}/source-file/content`。
- `WangqiDocumentResponse` 必须一次性补齐页面展示所需字段，不留实现时再补：
    - `currentVersionId`
    - `currentVersionNo`
    - `currentVersionedAt`
    - `contentUpdatedAt`
    - `versionDirty`
- 在 `WangqiDocumentAdminController` 增加/调整协议端点和原始文件端点：
    - `POST /api/classics/wangqi/documents/{id}/get`
    - `POST /api/classics/wangqi/documents/{id}/source-file/upload`
    - `POST /api/classics/wangqi/documents/{id}/source-file/get`
    - `GET /api/classics/wangqi/documents/{id}/source-file/content`
- Wangqi 原始文件上传权限：
    - 上传使用 `classics:wangqi:edit`
    - 元数据查询和下载/读取使用 `classics:wangqi:view`
- Wangqi 原始文件上传必须：
    - 校验 Wangqi 文档存在。
    - 由 `WangqiDocumentApplicationService` 承担业务编排和事务边界。
    - 在 application service 内复用 Storage 侧上传 helper 保存文件对象。
    - 将 Storage 对象归属写为 `ownerType=CLASSICS_WANGQI_DOCUMENT`、`ownerId={documentId}`。
    - 将 Storage 引用保持为历史的一部分，`ownerParams` 至少标明 `usage=WANGQI_SOURCE_FILE` 和 `documentId`。
    - 版本归属以 `classics_content_version.snapshot_json.storageObjectId` 为准；如 Storage reference 支持后续更新，可补写 `versionId/versionNo`，但不作为本轮硬要求。
    - 更新 Wangqi 文档 `storageObjectId`。
    - 更新 `contentUpdatedAt`。
    - 创建正式内容版本，`changeType=MANUAL_SAVE`。
    - 新增原始文件时 `changeSummary=上传原始文件`；替换已有原始文件时 `changeSummary=替换原始文件`。
    - 返回 Wangqi 原始文件响应，至少包含 `documentId`、`storageObjectId`、`originalFilename`、`contentType`、`size`、`contentUrl`。
- Wangqi 普通 add/update 请求允许携带 `storageObjectId` 作为原始文件绑定：
    - 若传入值与当前文档已有 `storageObjectId` 一致，视为保持原绑定。
    - 若传入对象未绑定，可绑定到当前 Wangqi 文档。
    - 若传入对象已绑定到其他业务对象或其他 Wangqi 文档，拒绝保存。
    - 绑定成功后应按用户保存行为更新 `contentUpdatedAt` 并创建正式版本。
- Wangqi 删除文档必须清理版本和 Storage 引用：
    - 删除该文档关联的历史版本。
    - 释放所有历史版本 snapshot 中关联的 Storage reference。
    - 删除后不保留该文档可查询版本历史。
- Wangqi 原始文件元数据查询必须：
    - 通过 Wangqi 文档 ID 找到当前 `storageObjectId`。
    - 校验文档存在且已关联原始文件。
    - 调用 Storage 侧元数据读取能力组装 `WangqiDocumentSourceFileResponse`。
    - 页面列表、详情和时间线接口不内联 `sourceFile`，前端在打开文件面板时二次查询元数据。
- Wangqi 原始文件下载必须：
    - 通过 Wangqi 文档 ID 找到 `storageObjectId`。
    - 校验文档存在且已关联原始文件。
    - 调用 Storage 侧读取能力输出文件内容。
    - 不接受任意 storage object id，避免绕过 Wangqi 权限边界。
    - 作为资源流 GET，未登录或认证失败按平台规则返回 `401`；文档不存在、未关联原始文件或文件不可读返回 `404`。
- 在 `WangqiDocumentAdminController` 增加版本端点：
    - `POST /api/classics/wangqi/documents/versions/list`，请求体包含 `id`
    - `POST /api/classics/wangqi/documents/versions/get`，请求体包含 `id`、`versionId`
    - `POST /api/classics/wangqi/documents/versions/reset`，请求体包含 `id`、`versionId`
- 通用 `ClassicsContentApplicationService.restoreHistoryVersion` 必须使用显式 `contentType` dispatcher：
    - `WANGQI_DOCUMENT` 分支委托普通 Spring Service/Handler 解析 `WangqiDocumentVersionSnapshot` 并回写王圻主表。
    - 非本轮支持的 contentType 不允许静默忽略，应返回明确业务异常。
    - dispatcher 不做反射和字符串拼接查找，使用枚举分支显式调用对应 domain 能力。
    - 不从通用 `ClassicsContentApplicationServiceImpl` 反向调用 `WangqiDocumentApplicationService`，避免 application service 之间形成循环依赖。
- 新增版本请求/响应模型：
    - `WangqiDocumentVersionRequest`
    - `WangqiDocumentVersionResponse`
- `WangqiDocumentVersionRequest` 字段：
    - `id`
    - `versionId`
- `WangqiDocumentVersionResponse` 字段：
    - `id`
    - `contentType`
    - `contentId`
    - `versionNo`
    - `versionedAt`
    - `snapshotJson`
    - `changeType`
    - `changeSummary`
- Wangqi controller 恢复历史版本入口调用 `ClassicsContentApplicationService.restoreHistoryVersion(versionId)`。
- Wangqi controller 在版本详情和恢复入口必须校验请求体中的 `id` 与版本记录归属一致：
    - `version.contentType == WANGQI_DOCUMENT`
    - `version.contentId == request.id`
    - 不一致时按 JSON/API 通用协议返回业务失败，HTTP 状态保持 `200`。
    - 不得跨文档读取或恢复版本。
- 恢复成功后，王圻文档详情应能看到恢复后的内容；本轮必须补齐 `WANGQI_DOCUMENT` 的恢复回写逻辑。
- 恢复历史版本是用户触发的内容变更，必须采用追加式版本语义：
    - 读取被恢复版本的 `snapshotJson`。
    - 用 snapshot 回写王圻主表。
    - 更新 `contentUpdatedAt`。
    - 创建新的正式版本，而不是把 `currentVersionId/currentVersionNo` 指回旧版本。
    - 新版本 `changeType=HISTORY_RESTORED`。
    - 新版本 `changeSummary=恢复历史版本 v{oldVersionNo}`。
    - 王圻主表 `currentVersionId/currentVersionNo/currentVersionedAt` 指向新版本。
    - 历史版本链不改写，恢复动作本身可追踪。
- 普通 add/update 保存 Wangqi 文档属于用户保存行为：
    - 保存成功后更新 `contentUpdatedAt`。
    - 创建正式内容版本，`changeType=MANUAL_SAVE`。
    - `changeSummary=保存王圻文档`。
- 版本历史发生在哪个页面，就使用哪个页面的 domain 权限：
    - Wangqi 页面查询版本列表和版本详情使用 `classics:wangqi:view`。
    - Wangqi 页面恢复历史版本使用 `classics:wangqi:edit`。
    - 版本 controller 入口在 `WangqiDocumentAdminController`，不新增 `classics:content:view/edit` 作为 Wangqi 页面版本入口权限。
- `PRIVATE` 是 Admin 侧内容可见性状态：
    - 本轮不引入用户归属模型。
    - 具备 `classics:wangqi:*` 权限的后台用户仍可按后台管理规则查看和维护。
    - Portal 或公开展示侧如何过滤 `PRIVATE` 不在本轮 Admin Web 闭环内处理。

需要发生的模块依赖和 Storage owner 类型变更：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/pom.xml`
    - 如仅响应模型需要 Storage 类型，不直接增加 Storage 业务编排依赖。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/pom.xml`
    - 增加 `kuzhambu-storage-application` 依赖，用于 Wangqi application service 复用 Storage 上传/读取 application 能力。
- `kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/enums/StorageOwnerType.java`
    - 增加 `CLASSICS_WANGQI_DOCUMENT`。
- Storage 需要知道对象归属哪个业务块，`ownerType` 是跨业务资源归属分类。
- Storage owner type 本轮明确采用 enum 扩展方案，不改为开放字符串。
- `CLASSICS_WANGQI_DOCUMENT` 是王圻文档原始文件的正式归属类型，不是临时标记。
- Storage 对象若因上传成功但业务事务失败产生孤立引用，由 Storage 模块内计划任务清理；王圻业务流程只负责写入正确归属和引用，不在异常路径直接删除对象。
- Storage 清理任务本轮必须实现：
    - 每 4 小时执行一次，cron 使用 `0 0 0/4 * * ?`。
    - 清理超过 12 小时仍处于未绑定状态的资源。
    - 未绑定以 Storage reference status 为准，目标对象应为 `referenceStatus=UNREFERENCED`。
    - 只清理可删除的正常对象，至少限定 `objectStatus=ACTIVE`，避免重复处理已删除或异常状态对象。
    - 清理必须物理删除数据库记录和底层存储对象。
    - 若 starter 尚未启用 scheduling，本轮必须在 admin starter 启用，确保 Storage 清理任务实际执行。
- 如果 owner type 后续需要进入字典或筛选选项，应同步 Storage 页面筛选来源；本轮只要求上传对象能带业务归属。

不计划发生的后端变更：

- 不新增 Wangqi 分类字典。王圻文档当前没有分类字段。
- 不新增标签、问答对、AI 候选确认接口。
- 不新增批量公开/私有修改接口。

### 前端范围

新增页面目录：

```text
kuzhambu-apps/admin-web/src/pages/classics/wangqi/
  wangqi-page.tsx
  wangqi-page.css
  wangqi-page.test.tsx
  wangqi-service.ts
  wangqi-service-contract.test.ts
  wangqi-types.ts
  components/
    wangqi-document-form-values.ts
    wangqi-document-list.tsx
    wangqi-document-model.tsx
    wangqi-storage-file-panel.tsx
    wangqi-version-history-panel.tsx
    wangqi-timeline-panel.tsx
```

修改现有文件：

- `kuzhambu-apps/admin-web/src/router/index.tsx`
    - import `WangqiPage`
    - 注册 route：`path: "classics/wangqi"`

可复用现有共享组件：

- `kuzhambu-apps/admin-web/src/components/kuzhambu-list-page/`
- `kuzhambu-apps/admin-web/src/components/kuzhambu-drawer/`
- `kuzhambu-apps/admin-web/src/components/kuzhambu-confirm-modal/`
- `kuzhambu-apps/admin-web/src/components/kuzhambu-rich-content-viewer/`
- `kuzhambu-apps/admin-web/src/components/kuzhambu-table/`
- `kuzhambu-apps/admin-web/src/components/kuzhambu-tag/`

需要复用或补齐业务上传能力：

- 前端复用 `postFormData`，但只调用 Wangqi API。
- 上传路径固定为 `/classics/wangqi/documents/{id}/source-file/upload`。
- 元数据路径固定为 `/classics/wangqi/documents/{id}/source-file/get`。
- 下载/读取路径固定为 `/classics/wangqi/documents/{id}/source-file/content`，前端通过 `toAuthenticatedResourceUrl` 转换为带认证资源 URL。
- 新建文档尚无 `id` 时，先保存文档，再显示上传入口。
- 已有文档上传原始文件时，只传文件本身；`ownerType` 和 `ownerId` 由 Wangqi 后端封装。

不计划新增共享组件。若实现时发现 Wangqi 和 MingCustoms 或 Storage 页面有明显可抽取代码，只记录为后续重构，不在本轮抽象。

## 数据结构设计

### 后端 API 请求字段

对应 `WangqiDocumentRequest`：

```ts
interface WangqiDocumentRequest {
    id?: number | null;
    title?: string | null;
    summary?: string | null;
    contentFormat?: "MARKDOWN" | "HTML" | string | null;
    content?: string | null;
    documentTime?: string | null;
    storageObjectId?: number | null;
    visibility?: "PUBLIC" | "PRIVATE" | string | null;
    keyword?: string | null;
    sortDirection?: "ASC" | "DESC" | null;
    pageNo?: number;
    pageSize?: number;
}
```

### 后端 API 响应字段

对应 `WangqiDocumentResponse`：

```ts
interface WangqiDocumentResponse {
    id: number;
    title?: string | null;
    summary?: string | null;
    contentFormat?: string | null;
    content?: string | null;
    documentTime?: string | null;
    storageObjectId?: number | null;
    visibility?: string | null;
    currentVersionId?: number | null;
    currentVersionNo?: number | null;
    currentVersionedAt?: string | null;
    contentUpdatedAt?: string | null;
    versionDirty?: boolean;
}

interface WangqiSourceFileResponse {
    documentId: number;
    storageObjectId?: number | null;
    originalFilename?: string | null;
    contentType?: string | null;
    size?: number | null;
    contentUrl?: string | null;
}
```

版本状态计算规则：

- `versionDirty = currentVersionId == null || currentVersionedAt == null || contentUpdatedAt > currentVersionedAt`。
- `WangqiDocumentResponse.storageObjectId` 只表达当前文档关联的原始文件对象 ID。
- `sourceFile` 由 `POST /api/classics/wangqi/documents/{id}/source-file/get` 单独查询，后端根据 `storageObjectId` 查询 Storage 元数据后组装。
- `sourceFile.contentUrl` 固定为 `/api/classics/wangqi/documents/{id}/source-file/content`。
- page、get、timeline 返回同一个 `WangqiDocumentResponse` 结构；列表可以只展示其中部分字段，但不内联原始文件元数据。

### 前端领域类型

新增 `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-types.ts`：

```ts
export type WangqiContentFormat = "MARKDOWN" | "HTML";
export type WangqiDocumentVisibility = "PUBLIC" | "PRIVATE";

export interface WangqiDocumentRecord {
    id: number;
    title?: string | null;
    summary?: string | null;
    contentFormat?: string | null;
    content?: string | null;
    documentTime?: string | null;
    storageObjectId?: number | null;
    visibility?: string | null;
    currentVersionId?: number | null;
    currentVersionNo?: number | null;
    currentVersionedAt?: string | null;
    contentUpdatedAt?: string | null;
    versionDirty?: boolean;
}

export interface WangqiSourceFileRecord {
    documentId: number;
    storageObjectId?: number | null;
    originalFilename?: string | null;
    contentType?: string | null;
    size?: number | null;
    contentUrl?: string | null;
}
```

### 原始文件类型

Wangqi 页面新增局部类型，不直接导入 Storage 页面私有类型。该类型与后端 `WangqiSourceFileResponse` 对齐：

```ts
export interface WangqiSourceFileRecord {
    documentId: number;
    storageObjectId?: number | null;
    originalFilename?: string | null;
    contentType?: string | null;
    size?: number | null;
    contentUrl?: string | null;
}
```

Wangqi service 新增方法：

- `uploadSourceFile(documentId: number, file: File)`
    - `FormData.file = file`
    - `POST /classics/wangqi/documents/{documentId}/source-file/upload`
- `getSourceFile(documentId: number)`
    - `POST /classics/wangqi/documents/{documentId}/source-file/get`
    - 用于文件面板打开时二次查询元数据。
- `getSourceFileContentUrl(documentId: number)`
    - 返回 `/classics/wangqi/documents/{documentId}/source-file/content`，调用方用 `toAuthenticatedResourceUrl` 包装。

上传完成后的数据流：

1. 前端调用 Wangqi 原始文件上传接口。
2. Wangqi controller 构造 command 并调用 Wangqi application service。
3. Wangqi application service 在同一事务内复用 Storage helper 保存对象，并更新当前文档 `storageObjectId`。
4. Wangqi application service 更新 `contentUpdatedAt` 并创建正式版本。
5. 前端刷新 Wangqi detail/page/timeline/version list。
6. 前端文件面板按需调用 `getSourceFile(documentId)` 获取当前原始文件元数据。

### Version 类型

Wangqi 页面新增版本类型：

```ts
export interface WangqiContentVersionRecord {
    id: number;
    contentType?: string | null;
    contentId?: number | null;
    versionNo?: number | null;
    versionedAt?: string | null;
    snapshotJson?: string | null;
    changeType?: string | null;
    changeSummary?: string | null;
}

export interface WangqiVersionSnapshot {
    contentType?: string | null;
    contentId?: number | null;
    contentUpdatedAt?: string | null;
    title?: string | null;
    summary?: string | null;
    contentFormat?: string | null;
    content?: string | null;
    documentTime?: string | null;
    storageObjectId?: number | null;
    visibility?: string | null;
}
```

Wangqi service 新增方法：

- `listVersions(documentId: number)`
    - `POST /classics/wangqi/documents/versions/list`
- `getVersion(documentId: number, versionId: number)`
    - `POST /classics/wangqi/documents/versions/get`
- `restoreVersion(documentId: number, versionId: number)`
    - `POST /classics/wangqi/documents/versions/reset`

版本对比规则：

- 左侧默认选择当前文档实时内容。
- 右侧选择历史版本 `snapshotJson`。
- 对比字段固定为：
    - `title`
    - `summary`
    - `contentFormat`
    - `content`
    - `documentTime`
    - `storageObjectId`
    - `visibility`
- 第一版可使用字段级文本差异展示，不要求引入 diff 库。

新增 `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-service.ts`：

```ts
export type WangqiDocumentQuery = PageQuery<{
    keyword?: string | null;
    visibility?: string | null;
    sortDirection?: "ASC" | "DESC" | null;
}>;

export interface WangqiDocumentCommand {
    id?: number | null;
    title?: string | null;
    summary?: string | null;
    contentFormat?: string | null;
    content?: string | null;
    documentTime?: string | null;
    storageObjectId?: number | null;
    visibility?: string | null;
}
```

Service 方法固定为：

- `page(request?: WangqiDocumentQuery)`
- `get(id: number)`
- `get` 调用 `POST /classics/wangqi/documents/{id}/get`，不新增协议型 GET。
- `listTimeline(request?: WangqiDocumentQuery)`
- `add(request: WangqiDocumentCommand)`
- `update(request: WangqiDocumentCommand)`
- `deleteById(id: number)`
- `uploadSourceFile(documentId: number, file: File)`
- `getSourceFile(documentId: number)`
- `getSourceFileContentUrl(documentId: number)`
- `listVersions(documentId: number)`
- `getVersion(documentId: number, versionId: number)`
- `restoreVersion(documentId: number, versionId: number)`

### 前端表单字段

`wangqi-document-form-values.ts` 固定转换以下字段：

- `id`
- `title`
- `summary`
- `contentFormat`
- `content`
- `documentTime`
- `storageObjectId`
- `visibility`

时间字段规则：

- UI 使用 Ant Design `DatePicker`。
- 表单内部可以使用 `dayjs`。
- 提交给后端时转换为 ISO 字符串或后端可解析的日期字符串。
- 从后端加载时按 `documentTime` 反填。

Storage 字段规则：

- 本轮接入 Wangqi 自有原始文件上传和下载。
- `storageObjectId` 是王圻原始文件绑定字段，可以由后端上传接口写入，也可以在普通保存中绑定已有 Storage 对象。
- 普通保存绑定已有 Storage 对象时，后端必须校验该对象与原绑定一致，或对象处于未绑定状态；不得绑定已归属其他业务对象的 Storage 对象。
- 文件选择器只上传到 Wangqi source-file API。
- 文案需明确这是“王圻原始文件”，不引导用户进入统一 Storage 页面完成业务上传。

## 页面行为设计

### 路由和菜单

- 浏览器地址：`/classics/wangqi`
- 后台菜单 seed 已存在；前端只补 route。
- 权限由后端菜单和 current-user perms 控制；页面内部仍按按钮语义展示，不新增前端权限系统。

### 列表页

列表筛选：

- `keyword`
    - 搜索标题、摘要和正文。
    - placeholder：`搜索王圻文档标题、摘要或正文`
- `visibility`
    - `全部`
    - `公开`
    - `私有`
- `sortDirection`
    - 默认 `DESC`
    - 可切换 `DESC` / `ASC`，按 `documentTime` 时间线顺序理解。

列表列：

- 标题
- 摘要预览
- 文档时间
- 原始文件对象 ID
- 可见性
- 操作

操作：

- `查看/编辑`
- `删除`

### 时间线面板

`wangqi-timeline-panel.tsx` 读取 `listTimeline`：

- 使用当前 `keyword`、`visibility`、`sortDirection`。
- 展示标题、文档时间、摘要预览。
- 点击时间线项打开详情抽屉。
- 时间线不是独立 route。

### 详情和编辑抽屉

`wangqi-document-model.tsx` 使用 `KuzhambuDrawer`。

表单字段：

- 标题：必填。
- 摘要：多行文本。
- 正文格式：`MARKDOWN` / `HTML`。
- 正文：多行文本。
- 文档时间：日期时间选择。
- 原始文件对象 ID：作为绑定字段展示；若页面支持手工绑定已有对象，保存时必须经过后端绑定校验。
- 可见性：公开/私有。

原始文件区域：

- 无 `storageObjectId` 时显示“未关联原始文件”。
- 有 `storageObjectId` 时显示对象 ID 和下载/读取入口。
- 支持选择文件并上传到 Wangqi source-file API。
- 上传成功后后端自动把返回的 Storage 对象 ID 写回当前 Wangqi 文档。
- 上传成功后后端自动产生正式版本，版本快照记录新的 `storageObjectId`。
- 上传/替换前必须提示这是替换原始文件关联，不会删除旧 Storage 对象。

预览区域：

- 使用 `KuzhambuRichContentViewer` 展示正文。
- `MARKDOWN` 和 `HTML` 都必须走同一个清洗展示控件。

版本区域：

- 展示版本历史列表。
- 点击版本后加载版本详情。
- 支持当前内容和历史版本字段级对比。
- 支持恢复历史版本，恢复前必须二次确认。
- 恢复成功后刷新详情、page、timeline 和版本列表；列表中应出现一条新的 `HISTORY_RESTORED` 版本。
- 如果恢复后的 snapshot 关联了不同 `storageObjectId`，文件面板重新调用 `getSourceFile(documentId)`。

保存规则：

- 无 `id` 调用 `add`。
- 有 `id` 调用 `update`。
- 成功后刷新 page 和 timeline query。

删除规则：

- 使用 `useKuzhambuConfirm` 二次确认。
- 调用 `deleteById(id)`。
- 删除 Wangqi 文档时必须释放该文档所有历史版本关联的 Storage reference。
- 删除 Wangqi 文档后不保留可查询版本历史；相关版本和 Storage reference 按删除流程清理。
- 成功后刷新 page 和 timeline query。

## 分步实施计划

每次提交控制在 2-5 个文件。若某一步实现中出现必须跨越更多文件的情况，应先拆分为更小的提交或更新本 RUNBOOK。

### Step 1：补齐王圻初始化数据

文件：

- `db/data/classics.sql`
- `scripts/verify-classics.sh`

动作：

- 在 `classics_wangqi_document` 增加 2 条 seed。
- 在 `verify-classics.sh` 增加 Wangqi seed 校验。
- 若 dev.env 已存在 schema，但没有 seed，需要同步执行对应 insert。不得依赖页面手工新增作为长期数据来源。

验证：

- `scripts/verify-classics.sh`

### Step 2：补强后端 Controller 冒烟测试

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/WangqiDocumentAdminControllerTest.java`
- 如测试需要 fixture，可只在同文件内定义。

动作：

- 覆盖 page/get/timeline/add/update/delete 基础调用。
- 固定 Wangqi detail 数据请求使用 `POST {id}/get`。
- 固定 API path 和 request body 关键字段。
- 覆盖 Wangqi source-file upload/metadata/content 的路径、权限注解和 multipart 契约。
- 覆盖 Wangqi version list/get/restore 基础调用。

### Step 3：补齐 Wangqi 时间线查询链路

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/query/WangqiDocumentPageQuery.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/service/impl/WangqiDocumentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/wangqi/repository/WangqiDocumentRepository.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/wangqi/repository/impl/WangqiDocumentRepositoryImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/wangqi/persistence/mapper/WangqiDocumentMapper.java`

动作：

- 固定 `listTimeline` 与 `page` 使用同一个 `WangqiDocumentPageQuery`。
- timeline 查询支持 `keyword`、`visibility`、`sortDirection`。
- `keyword` 搜索标题、摘要和正文。
- repository 和 mapper 不新增独立 DTO，沿用 application query 转换后的查询参数。

验证：

- `cd kuzhambu-servers`
- `mvn -pl biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-infra -am spotless:check checkstyle:check test`

### Step 4：补齐 Storage owner 类型和物理删除能力

文件：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/enums/StorageOwnerType.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/repository/StoredObjectRepository.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/store/StoredObjectStore.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/StoredObjectRepositoryImpl.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/store/ObjectStorageStoredObjectStore.java`

动作：

- 增加 `CLASSICS_WANGQI_DOCUMENT` owner type。
- 明确 Storage owner type 用于表达业务块归属，王圻原始文件必须写入 `CLASSICS_WANGQI_DOCUMENT`。
- 增加 Storage 物理删除能力，覆盖数据库记录和底层存储对象。
- repository 负责物理删除数据库记录，store 负责删除底层对象。
- 删除底层对象失败时不得静默吞掉，应暴露异常让调用方感知清理失败。

验证：

- `cd kuzhambu-servers`
- `mvn -pl biz/storage/kuzhambu-storage-domain,biz/storage/kuzhambu-storage-application,biz/storage/kuzhambu-storage-infra -am spotless:check checkstyle:check test`

### Step 5：实现 Storage 孤立对象清理计划任务

文件：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/StorageOrphanObjectCleanupScheduler.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/test/java/com/thundax/kuzhambu/storage/application/StorageOrphanObjectCleanupSchedulerTest.java`
- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/java/com/thundax/kuzhambu/starter/admin/KuzhambuAdminApplication.java`
- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/test/java/com/thundax/kuzhambu/starter/admin/AdminStarterArchitectureTest.java`

动作：

- 增加 Storage 模块内孤立对象清理计划任务，每 4 小时执行一次。
- 清理条件固定为：`objectStatus=ACTIVE`、`referenceStatus=UNREFERENCED`、创建时间早于当前时间 12 小时。
- 清理动作必须物理删除数据库记录和底层存储对象。
- 清理任务不得删除 `referenceStatus=REFERENCED` 的对象，即使它们只存在于历史版本 snapshot 中。
- 如果 admin starter 尚未启用 scheduling，在 `KuzhambuAdminApplication` 启用 scheduling，确保清理任务实际执行。
- 不让 Admin Web Wangqi 页面直接调用 Storage 页面接口。

验证：

- `cd kuzhambu-servers`
- `mvn -pl biz/storage/kuzhambu-storage-application,starter/kuzhambu-admin-starter -am spotless:check checkstyle:check test`

### Step 6：暴露 Wangqi 版本 Admin API

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/WangqiDocumentAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/request/WangqiDocumentVersionRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/response/WangqiDocumentVersionResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/assembler/WangqiDocumentInterfaceAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/support/WangqiDocumentVersionRestorer.java`

动作：

- 在 Wangqi controller 暴露版本列表、版本详情、恢复历史版本接口。
- Wangqi detail 数据请求使用 `POST {id}/get`；资源流以外不新增协议型 GET。
- 版本入口使用 Wangqi domain 权限：`classics:wangqi:view/edit`。
- Wangqi controller 调用通用 `ClassicsContentApplicationService`。
- 版本详情和恢复必须校验版本归属：`contentType=WANGQI_DOCUMENT` 且 `contentId` 等于请求体中的文档 ID。
- `restoreHistoryVersion` 使用显式 contentType dispatcher；本轮实现 `WANGQI_DOCUMENT` 分支并委托 `WangqiDocumentVersionRestorer`。
- `WangqiDocumentVersionRestorer` 是普通 Handler，只封装王圻恢复回写和新版本追加，不暴露为跨域 ApplicationService。

验证：

- `cd kuzhambu-servers`
- `mvn -pl biz/classics/kuzhambu-classics-interface -am spotless:check checkstyle:check test`

### Step 7：封装 Wangqi 原始文件 application 编排

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/command/WangqiDocumentSourceFileCommand.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/service/WangqiDocumentApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/service/impl/WangqiDocumentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/pom.xml`

动作：

- application 增加 `changeSourceFile(WangqiDocumentSourceFileCommand command)`。
- application 增加 `getSourceFile(documentId)` 和 `getSourceFileContent(documentId)` 所需能力，统一通过 Wangqi 文档 ID 进入。
- `WangqiDocumentSourceFileCommand` 包含 `documentId`、`inputStream`、`originalFilename`、`contentType`、`size`。
- application service 在一个事务里完成文档存在校验、Storage 保存、`storageObjectId` 更新、`contentUpdatedAt` 更新和正式版本创建。
- 普通 add/update 保存成功后创建 `MANUAL_SAVE` 版本，`changeSummary=保存王圻文档`。
- 上传或替换原始文件必须产生正式版本。
- 替换原始文件时不删除旧 Storage 对象，旧对象由历史版本 snapshot 和 Storage reference 保持可追踪。
- 删除 Wangqi 文档时删除该文档历史版本并释放历史版本关联的 Storage reference。
- Classics application 增加 `kuzhambu-storage-application` 依赖。

验证：

- `cd kuzhambu-servers`
- `mvn -pl biz/classics/kuzhambu-classics-application -am spotless:check checkstyle:check test`

### Step 8：封装 Wangqi 原始文件 Admin API

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/WangqiDocumentAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/response/WangqiDocumentSourceFileResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/assembler/WangqiDocumentInterfaceAssembler.java`

动作：

- 增加 `POST {id}/source-file/upload`，参数名固定为 `file`。
- 增加 `POST {id}/source-file/get` 返回当前原始文件元数据。
- 增加 `GET {id}/source-file/content` 返回当前原始文件内容。
- controller 只处理 HTTP/multipart/response，不串联业务动作。
- 下载和元数据查询都通过文档 ID 找到 `storageObjectId`，不接受任意 storage object id。
- `POST {id}/source-file/get` 是 JSON/API 数据请求，失败按通用响应协议返回。
- `GET {id}/source-file/content` 是资源流 GET，认证失败返回 `401`；文档不存在、未关联原始文件或文件不可读返回 `404`。
- 返回 `WangqiDocumentSourceFileResponse`。

验证：

- `cd kuzhambu-servers`
- `mvn -pl biz/classics/kuzhambu-classics-interface -am spotless:check checkstyle:check test`

### Step 9：新增前端 service 和类型契约

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-service-contract.test.ts`

动作：

- 定义 `WangqiDocumentRecord`。
- 定义 `WangqiSourceFileRecord`。
- 定义 `WangqiContentVersionRecord` 和 `WangqiVersionSnapshot`。
- 定义 `WangqiDocumentQuery` 和 `WangqiDocumentCommand`。
- 实现 page/get/listTimeline/add/update/deleteById。
- 实现 uploadSourceFile/getSourceFile/getSourceFileContentUrl/listVersions/getVersion/restoreVersion。
- uploadSourceFile 只调用 Wangqi API，不调用 Storage API。
- 测试固定所有 API path 和 request body。

验证：

- `cd kuzhambu-apps`
- `npm run format:check`
- `npm run lint`
- `npm run test -- --run wangqi-service-contract`

### Step 10：实现王圻列表、表单和时间线组件

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/components/wangqi-document-form-values.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/components/wangqi-document-list.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/components/wangqi-document-model.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/components/wangqi-timeline-panel.tsx`

动作：

- 实现列表、抽屉表单、正文预览和时间线面板。
- `wangqi-timeline-panel.tsx` 支持 keyword、起止时间和排序查询。
- 使用 `KuzhambuRichContentViewer` 展示正文。
- 所有业务交互控件必须有稳定可访问名称。

验证：

- `cd kuzhambu-apps`
- `npm run format:check`
- `npm run lint`
- `npm run test -- --run wangqi`

### Step 11：实现王圻原始文件和版本组件

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/components/wangqi-storage-file-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/components/wangqi-version-history-panel.tsx`

动作：

- 实现 Wangqi 原始文件上传/元数据查询/下载。
- 实现版本历史、版本对比和历史恢复。
- 文件面板打开时调用 `getSourceFile(documentId)` 二次查询当前原始文件元数据。
- 恢复历史版本成功后刷新详情、列表、时间线、版本列表和文件元数据。
- 所有业务交互控件必须有稳定可访问名称。

验证：

- `cd kuzhambu-apps`
- `npm run format:check`
- `npm run lint`
- `npm run test -- --run wangqi`

### Step 12：注册页面和路由

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.css`
- `kuzhambu-apps/admin-web/src/router/index.tsx`

动作：

- 组装 query、mutations、page list、timeline 和 drawer。
- 组装 Wangqi source-file upload mutation 和 version restore mutation。
- 注册 `/classics/wangqi` route。
- 页面根 class 使用 `wangqi-page` 前缀。
- 风格延续 Admin Web 古风口径，避免新增独立视觉体系。

验证：

- `cd kuzhambu-apps`
- `npm run format:check`
- `npm run lint`
- `npm run test`
- `npm run build`

### Step 13：补页面单测和 E2E

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.test.tsx`
- `kuzhambu-apps/admin-web/e2e/classics/wangqi/wangqi.spec.ts`

动作：

- 单测覆盖列表加载、筛选请求、打开详情、保存新增、保存更新、删除确认、上传原始文件、版本列表、版本对比和恢复确认。
- E2E mock current-user menus/perms，并覆盖菜单进入、筛选、时间线、富文本清洗预览、文件上传/下载入口、版本对比、恢复、保存和删除请求。

验证：

- `cd kuzhambu-apps`
- `npm run test`
- `npm --workspace admin-web run e2e -- e2e/classics/wangqi/wangqi.spec.ts`

### Step 14：dev.env 冒烟

前置：

- 使用 Java 17。
- 启动前加载 repo root `dev.env`。
- 若 Step 1 增加了 seed，需要同步到 dev.env 数据库。

动作：

1. 安装 starter 依赖：

```sh
cd kuzhambu-servers
mvn spotless:check
mvn checkstyle:check
mvn -pl starter/kuzhambu-admin-starter -am -DskipTests install
```

2. 启动 admin starter：

```sh
set -a
source ../../../dev.env
set +a
cd kuzhambu-servers/starter/kuzhambu-admin-starter
mvn spring-boot:run
```

3. 使用 `docs/00-governance/HOW-TO-ADMIN-LOGIN-SMOKE.md` 登录。

4. 冒烟 API：

- `POST /admin-api/api/classics/wangqi/documents/page`
- `POST /admin-api/api/classics/wangqi/documents/timeline/list`
- `POST /admin-api/api/classics/wangqi/documents/{id}/get`
- `POST /admin-api/api/classics/wangqi/documents/{id}/source-file/upload`
- `POST /admin-api/api/classics/wangqi/documents/{id}/source-file/get`
- `GET /admin-api/api/classics/wangqi/documents/{id}/source-file/content`
- `POST /admin-api/api/classics/wangqi/documents/versions/list`
- `POST /admin-api/api/classics/wangqi/documents/versions/get`
- `POST /admin-api/api/classics/wangqi/documents/versions/reset`

验收：

- page 返回 `COMMON-00000`。
- records 至少包含 seed 数据。
- timeline 返回 `COMMON-00000`。
- detail 返回正文和可见性字段。
- 上传返回 Storage 对象 ID。
- 元数据接口返回当前原始文件信息。
- 下载/读取接口返回文件内容。
- 版本列表可看到保存或恢复产生的正式版本。
- 上传或替换原始文件后，版本列表新增 `MANUAL_SAVE` 版本，快照包含新的 `storageObjectId`。
- 使用不匹配的 `{documentId}` 和 `{versionId}` 请求版本详情或恢复时必须返回通用协议业务失败，不得跨文档读取或恢复。
- 版本恢复后 detail 返回恢复后的字段。
- 版本恢复后版本列表新增 `HISTORY_RESTORED` 版本，`changeSummary` 标明恢复的旧版本号。

### Step 15：文档和现场收口

文件：

- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
- `TODO.md`
- `docs/30-designs/RUNBOOK-CLASSICS-WANGQI-ADMIN-WEB.md`

动作：

- 更新 Wangqi 覆盖状态。
- 删除完成 TODO。
- 删除本 RUNBOOK。
- 清理本地服务、临时日志、Playwright 产物和 dev.env 临时测试数据。

验证：

- `git status --short` 只显示预期收口文件，最终应为空。
- `lsof -iTCP:20010 -sTCP:LISTEN -n -P || true` 无残留 admin starter。
- `lsof -iTCP:5173 -sTCP:LISTEN -n -P || true` 无残留前端 dev server。

## 完整验证清单

后端：

- `cd kuzhambu-servers && mvn spotless:check`
- `cd kuzhambu-servers && mvn checkstyle:check`
- `cd kuzhambu-servers && mvn test`
- Storage 孤立对象清理测试必须覆盖：
    - 超过 12 小时且 `UNREFERENCED` 的 `ACTIVE` 对象会被清理。
    - 未超过 12 小时的对象不会被清理。
    - `REFERENCED` 对象不会被清理。
    - 非 `ACTIVE` 对象不会被重复清理。

前端：

- `cd kuzhambu-apps && npm run format:check`
- `cd kuzhambu-apps && npm run lint`
- `cd kuzhambu-apps && npm run test`
- `cd kuzhambu-apps && npm run build`
- `cd kuzhambu-apps && npm --workspace admin-web run e2e -- e2e/classics/wangqi/wangqi.spec.ts`

数据：

- `scripts/verify-classics.sh`
- dev.env 数据库存在 Wangqi seed。

冒烟：

- dev.env 登录成功。
- Wangqi page/timeline/detail API 返回成功。

## 风险和边界

- 王圻标签、问答对和 AI 候选确认不在本轮实现，仍属于后续 Classics/Knowledge/AI 协作任务。
- Portal 或公开展示侧的 `PRIVATE` 过滤不在本轮实现，本轮只完成 Admin Web 管理闭环。
- Storage owner type 后续如要进入页面筛选字典，需要在 Storage 页面任务中继续补齐；本轮只保证 `CLASSICS_WANGQI_DOCUMENT` 可被写入和用于归属判断。
