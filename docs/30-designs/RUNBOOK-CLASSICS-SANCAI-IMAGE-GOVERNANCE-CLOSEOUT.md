# RUNBOOK Classics Sancai Image Governance Closeout

## 1. Purpose

本文档定义三才图会图片治理收口的执行步骤。

目标是把 `docs/10-requirements/CLASSICS-REQUIREMENTS.md` 中三才图会的以下既有需求推进到“已完成”：

- 多张配图展示、缩略预览和放大浏览。
- 原图上传、删除和预览。
- 图片列表管理。
- 当前使用图片选择体验。
- Portal 分享和 Workers 静态展示使用三才图片快照。

本文只记录可执行事实步骤，不包含临时调查。

## 2. Confirmed Decisions

- 不生成独立缩略图文件。
- 不新增数据库字段。
- 缩略预览使用现有图片内容读取接口输出的图片 URL。
- `classics_sancai_entry_image.current_used` 和 `classics_sancai_visual_asset.current_used` 是两个独立状态。
- 删除图片只删除 Classics 图片记录并解绑 Storage 引用，不直接物理删除 Storage 对象。
- 删除当前使用图片后，同条目剩余图片按 `priority ASC` 第一张自动补位为当前使用图片；没有剩余图片时该条目没有当前使用图片。
- Portal 分享只读展示分享快照内允许访问的图片资源，不读取 admin 图片接口。
- Java 分享/导出/静态展示 payload 必须保存多图列表，不再只保存 `currentUsed=true` 图片。
- `images` snapshot 语义变化作为接口契约变化处理，必须同步更新 `docs/20-interfaces/CLASSICS-CONTENT-VERSION-SNAPSHOT-INTERFACE.md`。
- 图片排序必须限定在单个 `entryId` 内。
- 上传新图时如果 `currentUsed=true`，后端自动清空同条目其他当前图，不要求前端传 `replaceImageId`。
- Portal 分享页只做“缩略图切换主图”的只读预览，不新增 modal。
- Worker HTML 只渲染 payload 提供的 `src`，不做 base64 内嵌，不打包图片资源。
- 本轮不修改 `docs/10-requirements/CLASSICS-REQUIREMENTS.md`，只在完成后更新 `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`。

## 3. Scope

纳入范围：

- `kuzhambu-servers/biz/classics`：三才图片删除、当前图切换、多图 snapshot payload、showcase/export payload 和测试。
- `kuzhambu-apps/admin-web/src/pages/classics/sancai`：条目详情配图管理区、图片卡片、放大浏览、删除、当前图切换、上传、排序和测试。
- `kuzhambu-apps/portal-web/src/pages/share`：分享详情三才图片只读展示、图片预览和测试。
- `kuzhambu-workers/src/kuzhambu_workers/render`：三才静态展示和 Classics 导出图片输出。
- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`：覆盖状态收口。

不纳入范围：

- 图片裁剪、压缩、水印、CDN 分发。
- AI 图片理解、融合、视觉描述、生图能力本身。
- Storage orphan 清理策略变更。
- 三才条目标签、问答、生命周期和细粒度权限总治理。

## 4. Existing Data Structures

### Database Table

`db/schema/classics.sql` 已存在表 `classics_sancai_entry_image`，本轮不新增字段。

现有字段：

- `id bigint NOT NULL AUTO_INCREMENT`
- `entry_id bigint NOT NULL`
- `storage_object_id bigint NOT NULL`
- `image_type varchar(16) NOT NULL`
- `title varchar(512) DEFAULT NULL`
- `current_used tinyint(1) NOT NULL DEFAULT 0`
- `priority int NOT NULL`

现有约束和索引：

- `PRIMARY KEY (id)`
- `UNIQUE KEY uk_classics_sancai_entry_image_object (entry_id, storage_object_id)`
- `UNIQUE KEY uk_classics_sancai_entry_image_priority (priority)`
- `KEY idx_classics_sancai_entry_image_entry (entry_id)`

### Snapshot JSON

修改 `SancaiEntryVersionSnapshot.images` 的语义。

现有单个图片对象字段保持不变：

- `imageId`
- `storageObjectId`
- `originalFilename`
- `contentType`
- `size`
- `imageType`
- `title`
- `currentUsed`
- `priority`

本轮变更：

- `images` 从“只包含 `currentUsed=true` 图片”改为“包含当前条目全部图片”。
- `images` 固定按 `priority ASC` 输出。
- `currentUsed` 继续标识当前使用图片。
- Portal 和 Workers 从同一 `images` 列表识别当前图和多图列表。
- 已创建分享的 `classics_share_target.content_snapshot_json` 不回写；当前图变化只影响后续版本、分享、导出和展示 payload。

## 5. Existing Runtime Entrypoints

- Admin 图片上传：`POST /api/classics/sancai/assets/images/{entryId}/upload`
- Admin 图片列表：`GET /api/classics/sancai/assets/images/{entryId}`
- Admin 图片排序：`POST /api/classics/sancai/assets/images/sort`
- Admin 图片读取：`GET /api/classics/sancai/assets/images/{entryId}/{imageId}/content`
- application 已有：`SancaiAssetApplicationService#deleteImage(SancaiEntryImageId id)`
- admin-web 已有：`listImages`、`uploadImage`、`getImageContentUrl`
- Portal 分享 response 已有：`ClassicsSharePortalTarget.images`
- Worker 静态展示已支持 `entries[].images[]` 渲染

## 6. Delivery Definition

完成后必须满足：

- admin-web 条目详情中有 `配图管理` 区块。
- `配图管理` 区块展示当前条目的所有图片卡片。
- 图片卡片展示缩略图、标题、图片类型、文件大小、当前使用状态。
- 图片卡片提供 `预览图片`、`下载图片`、`设为当前使用图片`、`删除图片` 控件。
- `预览图片` 打开放大浏览抽屉，可切换上一张和下一张。
- `删除图片` 有二次确认。
- `设为当前使用图片` 成功后，同一条目只有目标图片 `currentUsed=true`。
- 删除当前图后页面展示自动补位后的当前图；删除最后一张图后展示空状态。
- 分享 snapshot 和静态展示 payload 保留多图列表。
- Portal 分享详情不暴露 admin 图片接口。
- Workers 静态展示和导出输出多图、当前图和缺图占位。

## 7. Execution Plan

### Task 1: Backend Image Delete And Current Selection

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/SancaiAssetApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiAssetRepository.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiAssetRepositoryImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/mapper/SancaiAssetMapper.java`

执行：

1. 在 `SancaiAssetApplicationService` 新增 `void useImage(SancaiEntryId entryId, SancaiEntryImageId imageId)`。
2. 在 `SancaiAssetApplicationServiceImpl#useImage` 中校验 `imageId` 存在且属于 `entryId`。
3. 在 `useImage` 中将同条目所有图片 `currentUsed=false`，再将目标图片 `currentUsed=true`。
4. 修改 `deleteImage`，删除前读取图片实体并校验存在。
5. 修改 `deleteImage`，删除图片记录后按 `ownerType=CLASSICS_SANCAI_ENTRY_IMAGE` 和 `ownerId=entry:{entryId}:image:{imageId}` 调用 Storage facade 精确解绑 owner/reference。
6. 修改 `deleteImage`，当被删图片 `currentUsed=true` 时，读取同条目剩余图片并按 `priority ASC` 自动设置第一张为当前使用。
7. 修改 `deleteImage`，当无剩余图片时不设置新的当前使用图片。
8. 修改 `uploadImage`，当 `command.currentUsed=true` 时自动清空同条目其他图片 `currentUsed`，不要求 `replaceImageId`。
9. 保留 `replaceImageId` 仅作为兼容入参，不作为上传当前图的必填条件。

验收：

- 删除不存在图片抛出业务错误。
- 切换当前图片时，目标图片必须属于当前 `entryId`。
- 同一 `entryId` 下最多一张图片为 `currentUsed=true`。
- 删除当前图后能自动补位。
- 删除最后一张图后 `listImages(entryId)` 返回空列表。
- 上传新图且 `currentUsed=true` 后，旧当前图变为 `currentUsed=false`。

### Task 2: Backend Admin API Contract

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/SancaiAssetAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiAssetRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiAssetInterfaceAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiAssetResponse.java`

执行：

1. 新增 `POST /api/classics/sancai/assets/images/delete`。
2. `images/delete` 请求体使用 `SancaiAssetRequest.id` 和 `SancaiAssetRequest.entryId`。
3. `images/delete` 使用 `@HasPermission("classics:sancai:edit")`。
4. 新增 `POST /api/classics/sancai/assets/images/current/change`。
5. `images/current/change` 请求体使用 `SancaiAssetRequest.id` 和 `SancaiAssetRequest.entryId`。
6. `images/current/change` 使用 `@HasPermission("classics:sancai:edit")`。
7. 修改 `POST /api/classics/sancai/assets/images/sort` 请求语义，要求请求体包含 `SancaiEntryImageSortRequest.entryId`。
8. `images/sort` 校验 `orderedIds` 全部属于 `entryId`。
9. 保持图片列表响应使用 `SancaiAssetResponse`，不新增第二套图片 response。
10. 确认图片列表响应字段包含 `id`、`entryId`、`storageObjectId`、`imageType`、`title`、`currentUsed`、`priority`、`originalFilename`、`contentType`、`size`、`previewUrl`、`downloadUrl`。

验收：

- 删除接口返回 `true` 或空成功响应。
- 当前图切换接口返回 `true`。
- 请求体缺少 `id` 或 `entryId` 时返回参数错误。
- 排序请求缺少 `entryId` 时返回参数错误。
- 读取接口仍保持 `download=true` 输出 attachment，不带 `download` 输出 inline。

### Task 3: Backend Snapshot Payload

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/SancaiEntryVersionSnapshot.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsContentSnapshotAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsContentSnapshotAssemblerTest.java`

执行：

1. 修改 `SancaiEntryVersionSnapshot.imageResources(List<SancaiEntryImage>)`，删除 `filter(SancaiEntryImage::isCurrentUsed)`。
2. 修改 `SancaiEntryVersionSnapshot.fromImageResources(...)`，删除 `filter(ImageResource::currentUsed)`。
3. 保持排序为 `Comparator.comparingInt(...priority)`。
4. 保持 `ImageResource` 字段列表不变。
5. 在测试中断言 snapshot `images` 包含多张图片。
6. 在测试中断言 `currentUsed=true` 和 `currentUsed=false` 都会进入 snapshot。
7. 在测试中断言 `images` 按 `priority ASC` 排序。

验收：

- `content_snapshot_json.images` 是多图列表。
- 多图列表包含 `currentUsed` 字段用于识别当前图。
- 不修改 `classics_content_version` 表结构。

### Task 4: Backend Showcase Payload

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiShowcaseCommand.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/SancaiAssetApplicationServiceImplTest.java`

执行：

1. `requestShowcase` 使用的 render payload 必须支持 `entries[].images[]` 多图列表。
2. `entries[].images[]` 中每个图片对象固定包含 `imageId`、`storageObjectId`、`imageType`、`title`、`currentUsed`、`priority`、`src`、`alt`、`caption`。
3. `src` 使用静态展示 payload 中可被 worker 渲染的资源 URL 或相对资源引用，不使用 admin 鉴权 URL。
4. `alt` 对原图使用 `三才图会原图`，对生成图使用 `三才图会生成图`。
5. 当前图仍通过 `currentUsed=true` 标识，不单独新增 `currentImage` 字段。
6. application 测试断言 render payload 包含多图、当前图和缺图空列表。

验收：

- showcase payload 能让 worker 渲染多图。
- payload 不依赖 worker 再访问 Java API。

### Task 5: Backend Export Payload

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/result/ClassicsExportJobResult.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceImplTest.java`

执行：

1. Sancai 导出 snapshot 中为每个条目输出 `images` 多图列表。
2. 单个图片对象字段固定为 `imageId`、`storageObjectId`、`imageType`、`title`、`currentUsed`、`priority`、`originalFilename`、`contentType`、`size`。
3. 导出 payload 中不新增数据库字段，不新增 Storage 对象。
4. 导出测试断言 `currentUsed=true` 图片和 `currentUsed=false` 图片都进入 payload。

验收：

- CSV/JSON/HTML/ZIP 的数据源都能拿到图片元数据。
- 导出 payload 与分享 snapshot 的图片字段口径一致。

### Task 6: Snapshot Interface Documentation

文件：

- `docs/20-interfaces/CLASSICS-CONTENT-VERSION-SNAPSHOT-INTERFACE.md`

执行：

1. 将 `SANCAI_ENTRY` snapshot 的 `images` 字段说明改为多图列表。
2. 明确 `images` 按 `priority ASC` 排序。
3. 明确 `currentUsed=true` 表示当前使用图片。
4. 明确已创建分享的 `content_snapshot_json` 不因后续图片切换或删除回写。
5. 保持单个图片对象字段为 `imageId`、`storageObjectId`、`originalFilename`、`contentType`、`size`、`imageType`、`title`、`currentUsed`、`priority`。

验收：

- 接口文档与 Java snapshot 输出字段一致。
- 接口文档不新增数据库字段承诺。

### Task 7: Backend Tests

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/SancaiAssetApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/SancaiAssetAdminControllerTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/test/java/com/thundax/kuzhambu/classics/infra/sancai/SancaiRepositoryTest.java`

执行：

1. application 测试覆盖 `useImage` 清空同条目其他当前图。
2. application 测试覆盖 `deleteImage` 删除当前图后补位。
3. application 测试覆盖 `deleteImage` 删除非当前图不改变当前图。
4. application 测试覆盖 `deleteImage` 调用 Storage 解绑。
5. controller 测试覆盖 `images/delete` 请求路径、权限和参数映射。
6. controller 测试覆盖 `images/current/change` 请求路径、权限和参数映射。
7. controller 测试覆盖 `images/sort` 必须携带 `entryId`。
8. infra 测试覆盖按条目读取图片按 `priority ASC` 返回。

验收：

- 失败测试能明确定位 delete/current/snapshot/payload 任一语义。

### Task 8: Admin Web Service Contract

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-service-contract.test.ts`

执行：

1. 新增 `deleteImage(command)`，调用 `POST /classics/sancai/assets/images/delete`。
2. 新增 `changeCurrentImage(command)`，调用 `POST /classics/sancai/assets/images/current/change`。
3. 新增或确认 `sortImages(command)`，调用 `POST /classics/sancai/assets/images/sort`。
4. `deleteImage` 入参类型字段为 `entryId: number`、`id: number`。
5. `changeCurrentImage` 入参类型字段为 `entryId: number`、`id: number`。
6. `sortImages` 入参类型字段为 `entryId: number`、`orderedIds: number[]`、`sortDirection?: "ASC" | "DESC" | null`。
7. `SancaiEntryImageRecord` 保持字段 `id`、`entryId`、`storageObjectId`、`imageType`、`title`、`currentUsed`、`priority`、`originalFilename`、`contentType`、`size`、`previewUrl`、`downloadUrl`。
8. contract 测试锁定三个新增/确认方法的 URL、HTTP method 和 body。

验收：

- 页面不直接调用 `postJson`。
- 页面只通过 `sancai-entry-service.ts` 调用图片 API。

### Task 9: Admin Web Image Management Controls

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.css`

执行：

1. 在条目详情抽屉内新增 section，标题文本固定为 `配图管理`。
2. section 容器设置 `aria-label="三才图会配图管理"`。
3. 图片列表使用卡片网格展示。
4. 每张图片卡片显示 `img` 缩略图。
5. 缩略图 `src` 优先使用 `record.previewUrl`，没有时使用 `getImageContentUrl({ entryId, imageId: record.id, mode: "preview" })`。
6. 缩略图 `alt` 根据 `imageType` 输出 `三才图会原图` 或 `三才图会生成图`。
7. 卡片显示控件或文本：`当前使用` Tag、图片标题、图片类型、文件大小。
8. 卡片提供按钮 `预览图片`。
9. 卡片提供链接或按钮 `下载图片`，URL 使用 `record.downloadUrl` 或 `getImageContentUrl(..., mode: "download")`。
10. 非当前图片卡片提供按钮 `设为当前使用图片`。
11. 当前图片卡片不显示可点击的 `设为当前使用图片`，改显示禁用按钮或 `当前使用` Tag。
12. 每张卡片提供危险按钮 `删除图片`。
13. `删除图片` 使用 `useKuzhambuConfirm` 二次确认。
14. 删除确认文案固定包含 `删除后不会直接物理删除 Storage 对象`。
15. 操作成功后刷新图片列表 query 和当前条目详情 query。
16. 图片列表为空时展示空状态文案 `暂无配图`。

验收：

- 测试能通过 `screen.getByLabelText("三才图会配图管理")` 定位区块。
- 测试能通过 role/name 找到 `预览图片`、`下载图片`、`设为当前使用图片`、`删除图片`。
- 删除当前图后 UI 展示补位后的当前图。

### Task 10: Admin Web Upload And Sort Controls

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.css`

执行：

1. 在 `配图管理` section 顶部提供上传控件，按钮文本固定为 `上传配图`。
2. 上传控件包含标题输入框，label 固定为 `图片标题`。
3. 上传控件包含图片类型选择框，label 固定为 `图片类型`，选项至少包含 `ORIGINAL` 和 `GENERATED`。
4. 上传控件包含 checkbox，label 固定为 `上传后设为当前使用`。
5. 上传提交后调用 `uploadImage`，并传入 `entryId`、`file`、`title`、`imageType`、`currentUsed`。
6. 图片卡片提供排序按钮 `上移图片` 和 `下移图片`。
7. 排序按钮按当前列表顺序生成 `orderedIds`，调用 `sortImages` 时同时传入当前 `entryId`。
8. 第一张图片禁用 `上移图片`。
9. 最后一张图片禁用 `下移图片`。
10. 上传和排序成功后刷新图片列表。

验收：

- 测试能选择文件并点击 `上传配图`。
- 测试能断言 `uploadImage` 收到 `currentUsed`。
- 测试能点击 `上移图片` 或 `下移图片` 并断言 `sortImages` 收到完整 `orderedIds`。

### Task 11: Admin Web Multi Image Preview

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-image-preview.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.css`

执行：

1. 新增页面私有组件 `SancaiEntryImagePreview`。
2. `SancaiEntryImagePreview` 使用 `KuzhambuDrawer`，抽屉标题固定为 `配图预览`。
3. 组件 props 包含 `images`、`currentImageId`、`entryId`、`open`、`onClose`。
4. 抽屉内显示大图。
5. 大图 `src` 使用当前图片 preview URL。
6. 大图下方显示图片标题、图片类型、当前使用状态。
7. 抽屉内提供按钮 `上一张图片`。
8. 抽屉内提供按钮 `下一张图片`。
9. 抽屉内提供链接 `下载当前图片`。
10. 单图时 `上一张图片` 和 `下一张图片` 禁用。
11. 关闭按钮使用 `KuzhambuDrawer` 默认关闭能力。

验收：

- 点击图片卡片 `预览图片` 后显示 `配图预览` 抽屉。
- 多图时可以切换上一张和下一张。
- 下载链接指向当前预览图片。

### Task 12: Portal Share Image Display

文件：

- `kuzhambu-apps/portal-web/src/pages/share/share-page.tsx`
- `kuzhambu-apps/portal-web/src/pages/share/share-form.tsx`
- `kuzhambu-apps/portal-web/src/pages/share/share-types.ts`
- `kuzhambu-apps/portal-web/src/pages/share/share-form.test.tsx`

执行：

1. 分享详情中对 `contentType === "SANCAI_ENTRY"` 的 target 渲染 `images`。
2. 主图选择规则：优先 `currentUsed=true` 图片；没有当前图时使用 `priority` 最小的图片。
3. 主图 `src` 使用 `image.storageObject.previewUrl`。
4. 主图 `alt` 根据 `imageType` 输出 `三才图会原图` 或 `三才图会生成图`。
5. 主图下方显示缩略图列表。
6. 缩略图列表按 `priority ASC` 排序。
7. 点击缩略图切换主图。
8. 提供 `下载图片` 链接，URL 使用 `image.storageObject.downloadUrl`。
9. 不拼接或调用 `/api/classics/sancai/assets/images`。
10. 不新增 modal 或 drawer；主图切换即为本轮 Portal 只读预览。

验收：

- 分享详情展示当前使用图片。
- 多图时缩略图可切换主图。
- 测试断言页面中不存在 admin 图片 API 路径。

### Task 13: Portal Share Service Contract

文件：

- `kuzhambu-apps/portal-web/src/pages/share/share-service.ts`
- `kuzhambu-apps/portal-web/src/pages/share/share-service.test.ts`
- `kuzhambu-apps/portal-web/src/pages/share/share-types.ts`

执行：

1. 保持 `ClassicsSharePortalImage.storageObject.previewUrl` 作为图片预览 URL。
2. 保持 `ClassicsSharePortalImage.storageObject.downloadUrl` 作为图片下载 URL。
3. service 测试覆盖 response 中 `images[].storageObject.previewUrl` 和 `downloadUrl` 的保留。
4. service 测试覆盖三才 target 多图字段解析。

验收：

- portal service 不生成 admin 图片 URL。
- portal service 不丢弃 `currentUsed=false` 图片。

### Task 14: Worker Showcase Render

文件：

- `kuzhambu-workers/src/kuzhambu_workers/render/sancai_showcase.py`
- `kuzhambu-workers/src/kuzhambu_workers/render/templates/sancai_showcase.html`
- `kuzhambu-workers/tests/test_sancai_showcase.py`

执行：

1. `_entry_images` 对 `entry.images` 按 `priority ASC` 排序。
2. `_entry_images` 对 `currentUsed=true` 图片增加 `data-current="true"`。
3. `_entry_images` 对缺少 `src` 的图片输出占位 figure，不输出破损图片。
4. template 增加当前图和缩略图列表样式。
5. 测试覆盖多图排序。
6. 测试覆盖当前图标记。
7. 测试覆盖缺图占位。
8. 不做 base64 内嵌，不打包图片资源。

验收：

- HTML 中多图顺序稳定。
- 当前图可被 CSS 或 DOM 标识识别。
- 缺图不会阻断条目正文渲染。

### Task 15: Worker Classics Export Render

文件：

- `kuzhambu-workers/src/kuzhambu_workers/render/classics_export.py`
- `kuzhambu-workers/src/kuzhambu_workers/render/templates/classics_export.html`
- `kuzhambu-workers/tests/test_classics_export.py`

执行：

1. JSON 导出保留 `items[].images[]` 原始数组。
2. HTML 导出渲染 `items[].images[]` 的图片元数据。
3. HTML 导出显示字段：`imageId`、`title`、`imageType`、`currentUsed`、`priority`。
4. CSV 导出保留条目主字段，不把多图展开为多行。
5. 测试覆盖 JSON 中多图字段保留。
6. 测试覆盖 HTML 中当前图标记。

验收：

- 导出不会丢失图片元数据。
- CSV 行数仍等于内容条目数。

### Task 16: Coverage Documentation

文件：

- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`

执行：

1. 将 `多张配图、缩略预览、放大浏览` 状态改为 `已完成`。
2. 将 `原图上传、删除和预览` 状态改为 `已完成`。
3. 将 Current Baseline 中“三才图会仍保留缩略图生成、多图放大浏览和图片列表管理等补充项”改为已收口事实。
4. 增加说明：本轮缩略图为浏览器缩略预览，不生成独立缩略图文件。
5. 保留细粒度权限过滤等非本轮缺口。

验收：

- 文档只记录完成事实和剩余缺口。
- 文档不记录命令流水和执行日志。

## 8. Verification

### Backend

```sh
cd kuzhambu-servers
mvn -pl biz/classics -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/classics -am test
```

### Admin Web

```sh
cd kuzhambu-apps
npm --workspace kuzhambu-admin-web run format
npm run format:check
npm run lint
npm --workspace kuzhambu-admin-web test -- src/pages/classics/sancai
```

### Portal Web

```sh
cd kuzhambu-apps
npm --workspace @kuzhambu/portal-web run format
npm run format:check
npm run lint
npm --workspace @kuzhambu/portal-web test -- src/pages/share
```

### Workers

```sh
cd kuzhambu-workers
.venv/bin/python -m ruff format .
.venv/bin/python -m ruff format --check .
.venv/bin/python -m ruff check .
.venv/bin/python -m pytest -p no:capture tests/test_sancai_showcase.py tests/test_classics_export.py
```

### Final Diff Check

```sh
git diff
```

验收：

- `git diff` 只包含本 RUNBOOK 范围内文件。
- 不提交 `target/`、`dist/`、`node_modules/`、`.venv/`、缓存文件或本地环境文件。

## 9. Commit Plan

执行时按以下提交粒度组织：

1. `Feat(classics): 补齐三才图片治理后端语义`
2. `Feat(classics): 输出三才多图快照和渲染 payload`
3. `Feat(admin-web): 补齐三才配图管理体验`
4. `Feat(portal-web): 同步三才分享多图展示`
5. `Feat(workers): 同步三才多图渲染输出`
6. `Docs(classics): 收口三才图片治理覆盖状态`

本任务使用一个 PR 完成，PR 内保留上述小步 commit。不要拆成多个 PR，以避免 snapshot payload 和消费端中间不一致。

## 10. Closure

- 本 RUNBOOK 完成前保留。
- 所有任务完成并进入 PR 收口时删除本 RUNBOOK。
- 如果只完成部分任务，收窄本文档为剩余未完成任务。
