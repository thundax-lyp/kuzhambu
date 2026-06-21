# RUNBOOK Storage 文件读取预览闭环

## 目标

在 `feature/storage-preview-runbook` 分支完成 Storage 文件读取和预览的最小闭环：后台 Storage 对象可读取和预览，Classics 业务文件通过业务归属校验后读取，Portal 分享页通过 Classics 分享校验后读取分享快照中允许展示的文件资源。

本 RUNBOOK 不是纯设计文档。任务完成时必须落地关联模块接入，并在关闭任务前删除本文件。

## 当前基线

- Storage 已有后台读取接口：`GET /api/storage/object/{id}/content`。
- Storage 应用层已有 `StorageApplicationService.openReadableContent(StoredObjectId)` 和 `StoredObjectContent`。
- Wangqi 已有业务读取接口：`GET /api/classics/wangqi/documents/{id}/source-file/content`。
- Sancai 图片仅有元数据接口：`GET /api/classics/sancai/assets/images/{entryId}`，没有业务上传和图片内容读取接口。
- Portal 分享详情只返回 `contentSnapshotJson`，没有分享校验后的文件资源读取接口。
- `SANCAI_ENTRY` 版本快照当前不包含图片资源引用；`WANGQI_DOCUMENT` 快照包含 `storageObjectId`。

## 范围

本轮覆盖：

- Storage 内容读取响应元数据统一。
- Admin Web Storage 对象读取从“打开链接”升级为可区分预览和下载的资源动作。
- Classics Wangqi 原始文件读取继续复用业务接口，并补齐 Admin Web 预览展示。
- Classics Sancai 图片业务上传、引用绑定、内容读取接口和 Admin Web 图片预览接入。
- Portal 分享详情的资源读取接口和 Portal Web 预览接入。
- 版本快照中必要文件资源引用的补充。

本轮不覆盖：

- 在线格式转换、PDF 转图片、Office 文档预览转换。
- CDN、签名 URL、短期读取 token。
- 图片裁剪、压缩、水印、缩略图生成。
- Worker 导出产物生成和下载闭环。
- 通用 Portal 上传入口；本轮只补 Admin 业务域上传。

## 数据结构调整

### Storage

不新增 Storage 表字段。保持业务域只保存稳定 `storageObjectId`，不暴露 `bucketName`、`objectKey`、本地路径或底层存储实现。

对外展示接口不得只返回裸 `storageObjectId` 作为可展示资源。Controller/assembler 返回给前端的数据需要包装成业务资源对象，内部 command、领域对象、数据库字段和版本快照仍保存稳定 ID。展示资源对象至少包含：

- `storageObjectId`
- `originalFilename`
- `contentType`
- `size`
- `previewUrl`
- `downloadUrl`

`previewUrl` 和 `downloadUrl` 必须指向业务域提供的读取地址，不指向 Storage 通用读取地址。Storage 只负责保存、读取和引用事实；业务域负责上传入口、业务归属校验、资源 URL 生成和对外响应结构。

需要整理的应用层结构：

- `StoredObjectContent`
  - 文件：`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/content/StoredObjectContent.java`
  - 保留 `StoredObject storage` 和 `InputStream inputStream`。
  - 如实现需要，可新增只读辅助方法，但不得把 bytes 全量读入该对象。

需要约定的响应头：

- `Content-Type`：来自 `StoredObject.contentType`，为空时回退 `application/octet-stream`。
- `Content-Disposition`：
  - 预览接口使用 `inline`。
  - 下载接口或 `download=true` 使用 `attachment`。
  - 文件名只使用 `FilenameUtils.getName(originalFilename)` 过滤路径。
  - 同时设置 `filename` 和 RFC 5987 `filename*`，避免中文文件名下载乱码。
- `Content-Length`：如 `StoredObject.size` 存在则设置。

### Classics Sancai

需要补充图片资源上传、绑定和读取所需的应用层结构。

- 新增 `SancaiEntryImageResource`
  - 建议文件：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/result/SancaiEntryImageResource.java`
  - 字段：
    - `Long entryId`
    - `Long imageId`
    - `Long storageObjectId`
    - `String originalFilename`
    - `String contentType`
    - `Long size`
    - `String previewUrl`
    - `String downloadUrl`
- 新增 `SancaiEntryImageContent`
  - 建议文件：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/result/SancaiEntryImageContent.java`
  - 字段：
    - `Long entryId`
    - `Long imageId`
    - `Long storageObjectId`
    - `StoredObjectContent content`
- 新增 `SancaiEntryImageUploadCommand`
  - 建议文件：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiEntryImageUploadCommand.java`
  - 字段：
    - `Long entryId`
    - `InputStream inputStream`
    - `String originalFilename`
    - `String contentType`
    - `long size`
    - `String title`
    - `SancaiEntryImageType imageType`
    - `boolean currentUsed`
    - `Long replaceImageId`

需要补充应用服务方法：

- 文件：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/SancaiAssetApplicationService.java`
- 方法：
  - `SancaiEntryImage getImage(SancaiEntryImageId id)`
  - `SancaiEntryImageResource uploadImage(SancaiEntryImageUploadCommand command)`
  - `SancaiEntryImageContent getImageContent(SancaiEntryId entryId, SancaiEntryImageId imageId)`

上传、引用和读取规则：

- Admin Sancai 图片上传必须由 Sancai 业务域提供 multipart 接口，不使用 Storage 通用上传接口作为页面入口。
- Sancai 上传内部复用 `StorageUploadStreamHelper` 保存文件。
- Sancai 上传使用默认图片格式白名单：`jpg`、`jpeg`、`png`、`gif`、`webp`。空文件、超限文件、非图片后缀或不允许内容类型必须拒绝。
- Sancai 上传大小限制复用 `StorageUploadStreamHelper` 当前大小限制和配置，不另设 Sancai 私有大小限制。
- Sancai 图片上传永远创建新的图片记录，不复用旧 `imageId` 替换底层 Storage 对象。
- 新上传图片成为当前使用图时必须替换一张既有当前使用图，由 `replaceImageId` 指定被替换图片。
- 被替换图片进入历史；首版使用 `currentUsed=false` 承载历史图，不新增图片历史表或状态字段。
- 历史图仍保留 `sancai_entry_image` 记录和 Storage reference，以便后续审计或预览；只有物理删除图片记录、撤销无历史承载的关联，或图片记录不再需要保留时，才清理对应 Storage reference。
- `replaceImageId` 必须属于同一 `entryId` 且 `currentUsed=true`；不满足时拒绝上传。
- Sancai 新增、上传或删除图片时，必须由 Sancai 业务域设置 Storage owner、建立或清理 Storage reference。
- `StorageQuery.ownerType` 使用 Sancai 图片约定的 Storage owner type；如当前枚举没有专用值，先补充 `StorageOwnerType.CLASSICS_SANCAI_ENTRY_IMAGE`。
- `StorageQuery.ownerId` 固定为 `entry:{entryId}:image:{imageId}`。上传时若必须先创建 Storage 对象再创建图片记录，可在图片记录生成后回写 owner/reference，确保读取校验和清理使用同一 ownerId。
- 图片必须存在。
- `image.entryId` 必须等于路径中的 `entryId`。
- `storageObjectId` 必须存在。
- 使用 `StorageApplicationService.existsReadableContent(StorageQuery)` 校验可读。
- 删除图片记录或撤销无历史承载的图片关联时同步清理 Storage reference；不直接删除 Storage 对象，是否孤立清理交给 Storage 清理策略。

### Classics 版本快照

需要让分享页能从快照识别可展示资源。

调整 `SANCAI_ENTRY` 快照：

- 文件：`docs/20-interfaces/CLASSICS-CONTENT-VERSION-SNAPSHOT-INTERFACE.md`
- Java DTO：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/SancaiEntryVersionSnapshot.java`
- 新增字段：
  - `images`: array
  - 每项包含：
    - `imageId`: number/null
    - `storageObjectId`: number/null
    - `originalFilename`: string/null
    - `contentType`: string/null
    - `size`: number/null
    - `imageType`: string/null
    - `title`: string/null
    - `currentUsed`: boolean
    - `priority`: number

`SANCAI_ENTRY.images` 只包含 `currentUsed=true` 的图片，不把历史备选图和未启用图写入分享快照。若存在多张 `currentUsed=true` 图片，全部进入快照，并按 `priority ASC` 固定展示顺序。

`SANCAI_ENTRY.images` 快照只保存稳定资源 ID 和创建分享时的文件元数据，不保存 `previewUrl` 或 `downloadUrl`。Portal 分享详情响应层再根据 `shareToken` 和 `storageObjectId` 动态装配 URL。

快照生成需要调整 `ClassicsContentSnapshotAssembler` 当前只接收 `Versionable` 的结构，让 Sancai 快照能够拿到当前使用图片及其 Storage 元数据。可选方案：

- 给 `ClassicsContentApplicationService.ensureVersioned` 增加内容类型上下文组装能力，由 Sancai application service 传入当前使用图片资源。
- 或让 `ClassicsContentSnapshotAssembler` 由 Spring 管理并注入 `SancaiAssetRepository` 与 `StorageApplicationService`，但要避免 domain 层反向依赖。

当前手写 JSON 序列化只支持基本类型；新增 `images` 数组前，必须改用 `ObjectMapper` 或扩展 `ClassicsContentSnapshotAssembler` 支持 `List`/`Map`。

调整 `WANGQI_DOCUMENT` 资源响应：

- 内部领域对象、数据库字段和版本快照仍保留 `storageObjectId`，不把内部模型或 `snapshot_json` 改成只保存资源对象。
- Admin Controller/assembler 和 Portal 分享响应需要在 `storageObjectId` 基础上补充 `storageObject` 展示对象，保持内部稳定 ID 和外部展示信息分离。
- 旧快照中的 `WANGQI_DOCUMENT.storageObjectId` 仍有效；Portal 响应层读取该 ID 后动态补 `storageObject`。
- `storageObject.previewUrl` 和 `storageObject.downloadUrl` 指向 Wangqi 业务域读取地址。
- 公开分享中的下载能力只允许 Wangqi 文档业务关联的原始文件使用，不开放任意 Storage 对象下载。

快照规则：

- 分享创建时仍只复制 `classics_content_version.snapshot_json` 到 `classics_share_target.content_snapshot_json`。
- Portal 资源读取只允许读取目标分享快照中出现的 `storageObjectId` 对应资源。
- Portal API 不回查主内容重新组装正文展示，但响应层可以根据快照中的 `storageObjectId` 查询 Storage 元数据并动态生成 Portal 资源 URL。
- Sancai `images` 资源只用于分享展示，不参与 Sancai 历史版本恢复；`SancaiEntryVersionRestorer` 本轮不恢复图片列表和 Storage 引用。

### Portal 资源引用类型

Portal Web 增加快照解析类型：

- 文件：`kuzhambu-apps/portal-web/src/pages/share/share-types.ts`
- 新增：
  - `ShareSnapshotResource`
  - `SancaiSnapshotImage`
  - `WangqiSnapshot`

字段：

- `storageObject?: ShareSnapshotResource | null`
- `images?: SancaiSnapshotImage[] | null`
- `contentType?: string | null`
- `contentId?: number | null`
- `imageId?: number | null`
- `title?: string | null`

`ShareSnapshotResource` 字段：

- `storageObjectId?: number | null`
- `originalFilename?: string | null`
- `contentType?: string | null`
- `size?: number | null`
- `previewUrl?: string | null`
- `downloadUrl?: string | null`

Portal 分享详情响应的资源 enrichment 由 `ClassicsSharingPortalController` / `ClassicsSharingPortalInterfaceAssembler` 完成：

- Controller 获取 `SharePortalResult` 后，将当前请求的 `shareToken` 传给 assembler；最终响应不暴露 `shareToken` 字段。
- Assembler 解析每个 target 的 `contentSnapshotJson`，不修改或回写持久化快照。
- `WANGQI_DOCUMENT.storageObjectId` 在响应层装配为 `ClassicsSharePortalTarget.storageObject`。
- `SANCAI_ENTRY.images[].storageObjectId` 在响应层装配为 `ClassicsSharePortalTarget.images[].storageObject`。
- Portal 响应固定资源字段路径：Wangqi 使用 `target.storageObject`；Sancai 使用 `target.images[].storageObject`。Portal Web 不从原始 JSON 猜测资源字段。
- `storageObject.previewUrl` 和 `storageObject.downloadUrl` 使用 Portal 分享资源读取地址：`/api/portal/classics/shares/{shareToken}/resources/{storageObjectId}/content`。
- 资源元数据来自 Storage application 查询；资源缺失、不可读或已删除时，响应层可将对应 `storageObject` 置空，同时保留正文快照展示。
- 资源读取接口仍必须二次校验 `shareToken` 和快照中的 `storageObjectId`，不能只信任前端 URL。
- Portal 资源读取成功时记录分享访问；失败对外统一 404，内部可记录失败原因但不得在响应中区分。

## 后端接口

### Storage Admin

文件：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/StorageObjectController.java`

调整：

- 保留 `GET /api/storage/object/{id}/content`，默认 `inline`。
- 可增加 `download` query 参数：
  - `GET /api/storage/object/{id}/content?download=true`
  - `download=true` 时 `Content-Disposition` 为 `attachment`。
- 设置 `Content-Length`。

测试：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/test/java/com/thundax/kuzhambu/storage/interfaces/admin/StorageObjectContentContractTest.java`

### Classics Admin Wangqi

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/WangqiDocumentAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/service/impl/WangqiDocumentApplicationServiceImpl.java`

调整：

- 保留 `GET /api/classics/wangqi/documents/{id}/source-file/content`。
- 增加 `download` query 参数。
- 设置 `Content-Length`。
- 确认读取前仍使用业务归属校验，不允许 Admin Web 直接拼 Storage 通用读取接口绕过 Wangqi 归属。

测试：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/WangqiDocumentAdminControllerTest.java`

### Classics Admin Sancai

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/SancaiAssetAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/SancaiAssetApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiAssetRepository.java`

新增接口：

- `POST /api/classics/sancai/assets/images/{entryId}/upload`
- `GET /api/classics/sancai/assets/images/{entryId}/{imageId}/content`

行为：

- 上传接口使用 `multipart/form-data`，由 Sancai 业务域调用 Storage 保存文件、创建图片记录、建立 Storage reference，并返回 `SancaiEntryImageResource`。
- 默认 `inline`。
- `download=true` 时 `attachment`。
- 路径 `entryId` 和图片归属不匹配返回 404 或业务不可见错误。
- 文件不存在、Storage 不可读、已删除对象统一返回不可读错误，不泄露底层路径。
- 读取响应头由 Sancai controller 自己设置；可抽取本模块私有 helper，但不依赖 Storage interface controller。

测试：

- 新增或扩展 `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/SancaiAdminControllerTest.java`
- 覆盖：
  - 路由路径。
  - 上传后返回业务资源对象，URL 指向 Sancai 业务域地址。
  - 空文件、超限文件、非图片格式或不允许内容类型会被拒绝。
  - 新图替换指定 `replaceImageId` 后，旧图 `currentUsed=false` 且 Storage reference 保留。
  - 删除图片记录或撤销无历史承载关联时清理 Storage reference。
  - Content-Type。
  - inline 和 attachment。
  - `Content-Disposition` 同时包含 `filename` 和 `filename*`。
  - entry/image 不匹配。

### Classics Portal 分享资源

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/controller/ClassicsSharingPortalController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/service/ClassicsSharingApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/service/impl/ClassicsSharingApplicationServiceImpl.java`

新增接口：

- `GET /api/portal/classics/shares/{shareToken}/resources/{storageObjectId}/content`

行为：

- 先执行和 `getPortalShare(shareToken)` 相同的分享可见性校验。
- 只允许按 target `contentType` 读取分享快照中的资源 ID：`WANGQI_DOCUMENT` 只看 `storageObjectId`，`SANCAI_ENTRY` 只看 `images[].storageObjectId`，不得跨内容类型匹配。
- 不存在、过期、撤销、非公开、资源不属于分享快照时统一 404。
- 通过校验后调用 `StorageApplicationService.openReadableContent`。
- 默认 `inline`，支持 `download=true`。
- `download=true` 仅允许 `WANGQI_DOCUMENT.storageObject` 代表的 Wangqi 原始文件；其他分享资源请求下载时应拒绝或退回 inline，首选统一返回 404 避免暴露资源能力差异。
- Portal 对外返回的资源 URL 使用分享资源读取接口；Admin 业务资源 URL 不得直接透传到 Portal。
- 资源读取成功应调用分享访问记录能力；失败可记录内部失败类型，但对外响应仍统一 404。

测试：

- 新增或扩展 portal 分享接口测试。
- 覆盖：
  - 分享可见资源可以读取。
  - 不在快照内的 Storage 对象不可读取。
  - 资源读取按 target `contentType` 匹配，不能跨 Wangqi/Sancai 资源字段误读。
  - 资源读取成功记录分享访问；失败对外统一 404。
  - 过期或撤销分享不可读取。

## 前端接入

### Admin Web Storage 页面

文件：

- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-service.ts`
- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-types.ts`

调整：

- 增加 `getStorageObjectContentUrl(id, download?)`，优先用稳定路由拼接，不依赖后端返回的 `accessEndpoint` 缺省值。
- Admin Storage 页面资源 URL 也必须由前端通过 `toAuthenticatedResourceUrl` 拼接当前 token 后再用于新窗口、图片预览或下载；不能只拼接 `ADMIN_API_BASE_URL` 后直接打开。后端接口仍保持鉴权，不提供可匿名访问地址。
- 表格操作拆成：
  - `预览`：打开 `download=false`。
  - `下载`：打开 `download=true`。
- 图片类型可在页面内使用 Ant Design `Image` 或统一 Drawer 预览；非图片继续新窗口打开。

### Admin Web Wangqi

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/components/wangqi-storage-file-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-types.ts`

调整：

- `getSourceFileContentUrl(documentId, download?)` 支持下载参数。
- Wangqi 文件面板继续使用 `toAuthenticatedResourceUrl` 拼接 token 后访问资源。
- `WangqiStorageFilePanel` 增加预览动作：
  - 图片：内联预览。
  - PDF/文本/Markdown/HTML：新窗口预览。
  - 其他类型：只提供下载。
- 下载按钮使用 `download=true`。

### Admin Web Sancai

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-model.tsx`
- 如需要拆分组件，新增 `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-image-preview-panel.tsx`

调整：

- 增加 `SancaiEntryImageRecord` 类型。
- 增加服务方法：
  - `uploadImage(entryId, file, metadata)`
  - `listImages(entryId)`
  - `getImageContentUrl(entryId, imageId, download?)`
- 在条目详情中展示图片资源：
  - 使用图片标题、类型、当前使用状态、排序值。
  - 图片 preview 使用业务读取接口，不直接使用 `/storage/object/{id}/content`。
  - 图片 preview 和下载 URL 使用 `toAuthenticatedResourceUrl` 拼接 token 后访问。
  - 下载使用 `download=true`。
  - 上传图片使用 Sancai 业务上传接口，不使用 Storage 通用上传接口。

### Portal Web Share 页面

文件：

- `kuzhambu-apps/portal-web/src/pages/share/share-types.ts`
- `kuzhambu-apps/portal-web/src/pages/share/share-service.ts`
- `kuzhambu-apps/portal-web/src/pages/share/share-form.tsx`

调整：

- 增加 `getShareResourceUrl(shareToken, storageObjectId, download?)`。
- 渲染 Portal 响应层已装配的资源字段：
  - `target.storageObject` 渲染为 Wangqi 原始文件资源。
  - `target.images[].storageObject` 渲染为 Sancai 图片资源。
- 页面不展示裸 JSON 作为主要内容；JSON 可以保留为调试/原始快照折叠块。
- 图片资源直接展示；只有 Wangqi 原始文件显示下载按钮，其他分享资源不展示下载按钮。
- 所有资源 URL 使用 `/api/portal/classics/shares/{shareToken}/resources/{storageObjectId}/content`。

## 实施顺序

1. Storage 读取响应头收敛。
2. Classics Sancai 图片业务上传、Storage owner/reference 绑定和删除清理。
3. Classics Sancai 图片读取后端接口。
4. Classics 快照生成支持 Sancai 当前使用图片 ID 列表和数组序列化，响应层补业务资源对象。
5. Classics 分享资源读取后端接口。
6. Admin Web Storage/Wangqi/Sancai 接入预览、下载和 Sancai 上传。
7. Portal Web 分享页资源解析和预览接入。
8. 契约测试、前端测试和必要 Playwright 冒烟。
9. 更新 readiness 覆盖记录。
10. 任务关闭前删除本 RUNBOOK。

## 提交拆分建议

- `Docs(runbook): 规划 Storage 文件读取预览闭环`
- `Backend(storage): 收敛文件内容读取响应`
- `Backend(classics): 接入三才图片上传和引用绑定`
- `Backend(classics): 接入三才图片内容读取`
- `Backend(classics): 补充分享快照资源 ID 和响应资源对象`
- `Backend(classics): 接入分享资源读取`
- `Frontend(admin): 接入 Storage 与 Classics 资源预览上传`
- `Frontend(portal): 接入分享资源预览`
- `Docs(readiness): 更新 Storage 预览闭环覆盖`
- `Docs(runbook): 移除 Storage 预览闭环临时计划`

## 验证

后端验证：

```sh
cd kuzhambu-servers
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/storage/kuzhambu-storage-interface -am test
mvn -pl biz/classics/kuzhambu-classics-interface -am test
```

前端验证：

```sh
cd kuzhambu-apps
npm run format:check
npm run lint
npm --workspace admin-web test
npm --workspace portal-web test
```

人工冒烟：

- Admin Storage 上传 PNG，列表点击预览能看到图片，点击下载得到原文件。
- Admin Wangqi 上传原始文件，详情文件面板能预览或下载。
- Admin Sancai 条目能上传图片、展示当前使用图片、预览和下载图片；归属不匹配 URL 返回不可读。
- Admin 创建包含 Wangqi 原始文件或 Sancai 图片的公开分享。
- Portal `/share/{shareToken}` 能展示资源预览。
- Portal 使用同一分享 token 读取不属于该分享快照资源对象的 `storageObjectId` 返回 404。

## 完成标准

- Storage、Classics Admin、Classics Portal、Admin Web、Portal Web 都使用稳定文件对象 ID 串起读取或预览。
- Portal 资源读取必须先经过分享链接和分享目标校验。
- 业务页面不直接暴露底层对象键、桶名或物理路径。
- 文档、接口契约、测试和 readiness 记录同步。
- 本 RUNBOOK 在 PR 合并前移除。
