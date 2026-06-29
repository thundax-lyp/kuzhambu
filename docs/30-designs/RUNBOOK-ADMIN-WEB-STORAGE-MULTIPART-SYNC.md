# RUNBOOK Admin Web Storage Multipart Sync

## Purpose

本文档定义 `kuzhambu-apps/admin-web` 对 Storage 分片上传能力的前端同步方案。

本文档服务当前任务执行，目标是把方案写成可以直接拆 TODO、拆 commit、拆测试的执行手册，而不是停留在宽泛建议层。

## Current State

当前 `admin-web` 的 Storage 页面只支持普通上传：

- 页面入口：`kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-page.tsx`
- 页面 service：`kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-service.ts`
- 现有接口：`POST /storage/object/upload`
- 现有行为：用户选中文件后直接普通上传，成功后刷新列表

当前缺口：

- 没有 multipart 四段式前端调用
- 没有上传进度
- 没有取消能力
- 没有上传失败后的前端清理链路
- 没有“大文件自动走分片”的页面策略

后端已具备：

- `POST /storage/object/multipart/initiate`
- `POST /storage/object/multipart/uploadPart`
- `POST /storage/object/multipart/complete`
- `POST /storage/object/multipart/abort`

以及：

- `complete` 的真实内容合并与落存储
- `abort` 的临时分片清理

因此本轮只做 `admin-web` 同步，不改后端职责。

## Final Decision

本轮采用以下固定结论：

- 页面继续保留一个“上传”入口，不新增“普通上传 / 分片上传”两个按钮
- 前端按文件大小自动分流：
  - 小文件走现有普通上传
  - 大文件走 multipart
- multipart 上传过程必须可见：
  - 当前阶段
  - 总体进度
  - 已完成分片数
  - 取消按钮
- 失败后若已创建 `uploadId`，前端必须尝试调用 `abort`
- 页面同一时间只支持一个上传任务，不做多任务队列

## Scope

本 RUNBOOK 只覆盖 `admin-web`：

- `kuzhambu-apps/admin-web/src/api/http.ts`
- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-service.ts`
- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-types.ts`
- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-page.css`
- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/components/*`
- `kuzhambu-apps/admin-web/e2e/storage/storage-object/storage-object.spec.ts`

不在本轮范围内：

- `portal-web`
- Java 后端接口签名调整
- 断点续传恢复
- 多文件队列上传
- 全局上传任务中心
- 上传历史面板

## Interface Contract

本节只固定本轮前端需要消费的接口，不替代后端正式契约文档。

### Existing Interface

普通上传：

- 方法：`POST`
- 路径：`/storage/object/upload`
- Content-Type：`multipart/form-data`
- 表单字段：
  - `file: File`
  - `ownerType?: string`
  - `ownerId?: string`
- 返回：
  - `StorageRecord`

### Multipart Interfaces

初始化分片上传：

- 方法：`POST`
- 路径：`/storage/object/multipart/initiate`
- Content-Type：`application/json`
- 请求字段：
  - `ownerType: string`（必填，`@NotBlank`）
  - `ownerId?: string`
  - `businessType?: string`
  - `originalFilename: string`（必填，`@NotBlank`）
  - `mimeType: string`（必填，`@NotBlank`）
  - `bucketName?: string`
  - `objectKey?: string`
  - `providerUploadId?: string`
  - `uploadId?: string`
  - `totalSize: number`（必填，`@NotNull` 且 `>0`）
  - `partSize: number`（必填，`@NotNull` 且 `>0`）
- 返回字段：
  - `uploadId: string`
  - `providerUploadId?: string`
  - `ownerType?: string`
  - `ownerId?: string`
  - `businessType?: string`
  - `originalFilename?: string`
  - `mimeType?: string`
  - `partSize: number`
  - `bucketName?: string`
  - `objectKey?: string`
  - `totalSize?: number`
  - `uploadedPartCount?: number`
  - `uploadStatus?: string`

上传单个分片：

- 方法：`POST`
- 路径：`/storage/object/multipart/uploadPart`
- Content-Type：`multipart/form-data`
- 表单字段：
  - `uploadId: string`
  - `partNumber: number`
  - `etag: string`（必填，`@NotBlank`）
  - `size: number`
  - `file: File`
- 返回字段：
  - `uploadId: string`
  - `partNumber: number`
  - `etag?: string`
  - `size?: number`
  - `uploadStatus?: string`

完成分片上传：

- 方法：`POST`
- 路径：`/storage/object/multipart/complete`
- Content-Type：`application/json`
- 请求字段：
  - `uploadId: string`
  - `bucketName?: string`
  - `objectKey?: string`
  - `size?: number`
  - `accessEndpoint?: string`
- 返回字段：
  - `id`
  - `uploadId: string`
  - `ownerType?: string`
  - `ownerId?: string`
  - `businessType?: string`
  - `originalFilename?: string`
  - `mimeType?: string`
  - `bucketName?: string`
  - `objectKey?: string`
  - `size?: number`
  - `accessEndpoint?: string`
  - `objectStatus?: string`
  - `referenceStatus?: string`

取消分片上传：

- 方法：`POST`
- 路径：`/storage/object/multipart/abort`
- Content-Type：`application/json`
- 请求字段：
  - `uploadId: string`
- 返回字段：
  - `uploadId: string`
  - `uploadStatus: string`（固定值 `ABORTED`）

上传会话状态值（参考）：

- `INITIATED`
- `UPLOADING`
- `COMPLETED`
- `ABORTED`

若运行时接口字段名和这里不一致，以实际后端 controller 契约为准，但字段适配必须收口到 `storage-object-service.ts`，不得散落到页面组件。

## Data Structure

### Existing Data

已有页面结果对象：

```ts
interface StorageRecord {
    id: string;
    originalFilename?: string | null;
    contentType?: string | null;
    ownerId?: string | null;
    ownerType?: string | null;
    size?: number | null;
    accessEndpoint?: string | null;
    objectStatus?: string | null;
    referenceStatus?: string | null;
    priority?: number | null;
    remarks?: string | null;
}
```

### New Service Types

本轮建议新增以下 service 数据结构：

```ts
interface InitMultipartUploadCommand {
    originalFilename: string;
    mimeType: string;
    ownerType?: string | null;
    ownerId?: string | null;
    businessType?: string | null;
    bucketName?: string | null;
    objectKey?: string | null;
    providerUploadId?: string | null;
    uploadId?: string | null;
    totalSize: number;
    partSize: number;
}

interface InitMultipartUploadRecord {
    uploadId: string;
    partSize: number;
    objectKey?: string | null;
    bucketName?: string | null;
}

interface UploadMultipartPartCommand {
    uploadId: string;
    partNumber: number;
    etag: string;
    size: number;
    file: Blob;
}

interface CompleteMultipartUploadCommand {
    uploadId: string;
    bucketName?: string | null;
    objectKey?: string | null;
    size?: number | null;
    accessEndpoint?: string | null;
}

interface AbortMultipartUploadCommand {
    uploadId: string;
}
```

### New Page State Types

本轮建议把页面上传状态收敛成明确模型：

```ts
type UploadStage =
    | "idle"
    | "uploading-single"
    | "initiating-multipart"
    | "uploading-parts"
    | "completing-multipart"
    | "success"
    | "error"
    | "aborting"
    | "aborted";

interface StorageUploadTaskRecord {
    fileName: string;
    fileSize: number;
    stage: UploadStage;
    uploadId?: string | null;
    uploadedBytes: number;
    totalBytes: number;
    uploadedPartCount: number;
    totalPartCount: number;
    errorMessage?: string | null;
    canCancel: boolean;
}
```

### New HTTP Helper Types

若在 `http.ts` 增加带进度能力的 helper，建议结构如下：

```ts
interface FormDataProgressOptions {
    signal?: AbortSignal;
    onProgress?: (uploadedBytes: number, totalBytes: number) => void;
}
```

## Upload Strategy

### Single Entry

页面仍只保留一个上传动作：

- 按钮文案：`上传`
- 入口位置：`KuzhambuListPage.pageActions`
- 触发流程：选中文件后立即上传

### Routing Rule

前端统一入口为：

- `uploadStorageFile(file, options)`

内部路由规则：

- `file.size < MULTIPART_UPLOAD_THRESHOLD_BYTES`
  - 走普通上传
- `file.size >= MULTIPART_UPLOAD_THRESHOLD_BYTES`
  - 走 multipart

建议阈值：

```ts
const MULTIPART_UPLOAD_THRESHOLD_BYTES = 20 * 1024 * 1024;
```

建议分片大小：

```ts
const MULTIPART_PART_SIZE_BYTES = 5 * 1024 * 1024;
```

建议并发数：

```ts
const MULTIPART_UPLOAD_CONCURRENCY = 3;
```

## UI Decision

页面应新增上传状态展示，但不新增第二套上传入口。

建议新增页面私有组件：

- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/components/storage-upload-task-card.tsx`

必要样式：

- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/components/storage-upload-task-card.css`
  或并入
- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-page.css`

组件职责：

- 展示文件名
- 展示当前阶段文案
- 展示总进度百分比
- 展示 `uploadedPartCount / totalPartCount`
- 展示错误信息
- 展示取消按钮

放置位置：

- `StorageObjectPage` 标题描述区下方
- 保持其属于页面私有展示，不做全局组件

不采用的方案：

- 不新增独立“分片上传”按钮
- 不把状态只放在 `message.success/error`
- 不把状态塞进表格行内
- 不让最终用户看到 `uploadId`、part path 或底层 object key

## Execution Tasks

本节是本 RUNBOOK 的核心。每个执行任务必须控制在 `2-5` 个文件内，方便后续拆 TODO 和拆 commit。

### Task 1 HTTP Progress Helper

目标：

- 在不重写整套请求层的前提下，为 multipart 分片上传提供进度和取消能力

涉及文件：

- `kuzhambu-apps/admin-web/src/api/http.ts`
- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-service.ts`

接口影响：

- 复用 `POST /storage/object/multipart/uploadPart`

数据结构：

- `FormDataProgressOptions`
- `UploadMultipartPartCommand`

完成标准：

- `http.ts` 提供 `postFormDataWithProgress`
- `storage-object-service.ts` 能通过该 helper 上传单个分片
- 页面层不直接接触 `XMLHttpRequest` 或底层 `fetch`

### Task 2 Multipart Service Contract

目标：

- 把 multipart 的接口适配、分流策略和上传算法收口到页面 service

涉及文件：

- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-service.ts`
- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-types.ts`

接口影响：

- `POST /storage/object/upload`
- `POST /storage/object/multipart/initiate`
- `POST /storage/object/multipart/uploadPart`
- `POST /storage/object/multipart/complete`
- `POST /storage/object/multipart/abort`

数据结构：

- `InitMultipartUploadCommand`
- `InitMultipartUploadRecord`
- `UploadMultipartPartCommand`
- `CompleteMultipartUploadCommand`
- `AbortMultipartUploadCommand`
- `StorageUploadTaskRecord`

完成标准：

- service 层暴露统一入口 `uploadStorageFile`
- service 层能提供进度回调和取消能力
- 页面不再自行判断普通上传和 multipart 的底层步骤

### Task 3 Page State And UI

目标：

- 在 Storage 页面接入上传状态机和上传任务展示

涉及文件：

- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-page.css`
- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/components/storage-upload-task-card.tsx`
- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/components/storage-upload-task-card.css`

接口影响：

- 无新增后端接口
- 页面通过 `storage-object-service.ts` 的统一入口调用

数据结构：

- `UploadStage`
- `StorageUploadTaskRecord`

完成标准：

- 用户选择文件后可见当前上传任务
- multipart 上传时可见进度与阶段
- 上传中可取消
- 成功后刷新列表
- 失败后显示错误并收口状态

### Task 4 Test And Contract Mock

目标：

- 用单测和 E2E 证明普通上传与 multipart 上传主路径可用

涉及文件：

- `kuzhambu-apps/admin-web/e2e/storage/storage-object/storage-object.spec.ts`
- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-service.ts`

若页面域已有合适测试文件，则改已有文件；若没有，可新增一个页面域测试文件，但总文件数仍需控制在 `2-5` 个。

接口影响：

- mock `POST /storage/object/upload`
- mock `POST /storage/object/multipart/initiate`
- mock `POST /storage/object/multipart/uploadPart`
- mock `POST /storage/object/multipart/complete`
- mock `POST /storage/object/multipart/abort`

数据结构：

- `StorageUploadTaskRecord`
- multipart request / response mock payload

完成标准：

- 保留现有普通上传 E2E
- 新增 multipart 成功场景
- 新增 multipart 取消场景
- 至少有一层测试能断言进度状态渲染

## Error Handling

统一错误口径：

- 普通上传失败：`上传失败`
- 初始化分片上传失败：`初始化分片上传失败`
- 上传分片失败：`分片上传失败`
- 分片合并失败：`分片合并失败`
- 取消分片上传失败：`取消分片上传失败`

规则：

- 优先展示后端 message
- 页面状态卡片保留一条精简错误信息
- `abort` 失败不能覆盖主失败原因

## Permission Rule

沿用现有权限：

- `storage:object:edit`

页面规则：

- 无权限时上传按钮禁用
- 无权限时不显示取消入口

本轮不新增权限点。

## Acceptance

本 RUNBOOK 对应任务完成后，必须同时满足：

- Storage 页面仍只有一个“上传”入口
- 小文件仍走普通上传
- 大文件自动走 multipart
- multipart 上传时用户可见进度、阶段和取消入口
- 失败时会尝试调用 `abort`
- 成功后列表刷新并出现新对象
- 有测试覆盖普通上传和 multipart 上传主路径

## Non-Goals

- 不实现断点续传恢复
- 不实现多文件上传队列
- 不实现全局上传中心
- 不改 portal-web
- 不把底层 multipart 元数据暴露给最终用户

## Cleanup Rule

本 RUNBOOK 只服务本轮 admin-web multipart 同步。

完成后应：

- 将稳定口径回写正式需求或设计文档
- 清理 `TODO.md` 中对应任务
- 删除本 RUNBOOK 与残留引用
