# Storage Design

## Purpose

本文档定义 Storage 模块的数据结构和接口设计。Storage 继承 sandwich 的对象存储、对象引用和分片上传模型，并按 kuzhambu 命名和业务边界改写。

Storage 只表达文件对象本身、文件内容和文件引用状态，不表达具体业务内容规则。

## Module

- 模块名称：Storage
- 业务域：storage
- 对应需求文档：[STORAGE-REQUIREMENTS.md](../10-requirements/STORAGE-REQUIREMENTS.md)
- 后端 biz 子工程：`kuzhambu-servers/biz/kuzhambu-biz-storage`
- 后端 infra 子工程：`kuzhambu-servers/infra/kuzhambu-infra-storage`
- 后台接口入口：`kuzhambu-servers/interfaces/kuzhambu-admin-api`
- 前台接口入口：无通用上传入口；业务专用 portal 上传由具体业务模块定义
- 前端入口：`kuzhambu-apps/admin-web`
- Python worker 能力：无

## Business Boundary

- 本模块负责：文件对象、文件内容读取、引用关系、分片上传和底层对象存储适配。
- 本模块不负责：CDN、图片裁剪压缩水印、视频转码、音频处理、在线预览转换、文件安全扫描。
- 依赖的其他业务域能力：Auth/Core 提供上传操作者上下文。
- 对外提供的业务能力：上传、读取、查询、删除、引用管理和分片上传。

## Data Model

Storage 表固定使用 `storage_` 前缀。

### storage_object

保存已存储对象主数据，不保存文件二进制内容。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `object_id` | `objectId` | 是 | 存储对象 ULID |
| `name` | `name` | 是 | 文件基础名 |
| `extend_name` | `extendName` | 否 | 文件扩展名 |
| `mime_type` | `mimeType` | 否 | MIME 类型 |
| `owner_id` | `ownerId` | 否 | 上传或持有方标识 |
| `owner_type` | `ownerType` | 否 | `USER` / 业务 owner type |
| `bucket_name` | `bucketName` | 否 | 存储桶或本地逻辑目录 |
| `object_key` | `objectKey` | 是 | 底层对象键 |
| `original_filename` | `originalFilename` | 否 | 原始文件名 |
| `content_type` | `contentType` | 否 | 输出内容类型 |
| `size` | `size` | 是 | 文件大小 |
| `access_endpoint` | `accessEndpoint` | 否 | 派生访问端点 |
| `object_status` | `objectStatus` | 是 | `ACTIVE` / `DELETING` / `DELETED` |
| `reference_status` | `referenceStatus` | 是 | `UNREFERENCED` / `REFERENCED` |
| `created_at` | `createdAt` | 是 | 创建时间 |
| `updated_at` | `updatedAt` | 是 | 更新时间 |

约束：
- `object_id` 唯一。
- `bucket_name + object_key` 唯一。
- 业务模块不得保存 `bucket_name`、`object_key` 或物理路径。
- 删除对象通过 `object_status` 表达。

### storage_object_reference

保存业务模块对存储对象的引用关系。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `object_id` | `objectId` | 是 | 存储对象 ULID |
| `reference_owner_type` | `ownerType` | 是 | 引用方类型 |
| `reference_owner_id` | `ownerId` | 是 | 引用方业务对象标识 |
| `owner_params` | `ownerParams` | 否 | 引用方附加参数 JSON |
| `reference_status` | `referenceStatus` | 是 | `REFERENCED` |
| `created_at` | `createdAt` | 是 | 创建时间 |

约束：
- 唯一约束：`object_id + reference_owner_type + reference_owner_id`。
- 同一业务对象重复引用同一文件应幂等。
- 清理最后一个引用后，`storage_object.reference_status` 改为 `UNREFERENCED`。

### storage_multipart_upload

保存分片上传会话。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `upload_id` | `uploadId` | 是 | 分片上传会话 ULID |
| `owner_id` | `ownerId` | 是 | 上传发起人 |
| `owner_type` | `ownerType` | 是 | 上传发起人类型 |
| `business_type` | `businessType` | 否 | 业务分类 |
| `original_filename` | `originalFilename` | 是 | 原始文件名 |
| `mime_type` | `mimeType` | 是 | MIME 类型 |
| `bucket_name` | `bucketName` | 否 | 存储桶或本地逻辑目录 |
| `object_key` | `objectKey` | 是 | 最终对象键 |
| `provider_upload_id` | `providerUploadId` | 否 | 底层存储分片会话标识 |
| `total_size` | `totalSize` | 是 | 文件总大小 |
| `part_size` | `partSize` | 是 | 固定分片大小 |
| `uploaded_part_count` | `uploadedPartCount` | 是 | 已上传分片数 |
| `upload_status` | `uploadStatus` | 是 | `INITIATED` / `UPLOADING` / `COMPLETED` / `ABORTED` |
| `completed_at` | `completedAt` | 否 | 完成时间 |
| `aborted_at` | `abortedAt` | 否 | 取消时间 |
| `created_at` | `createdAt` | 是 | 创建时间 |
| `updated_at` | `updatedAt` | 是 | 更新时间 |

约束：
- `upload_id` 唯一。
- 完成后必须生成 `storage_object`。

### storage_multipart_upload_part

保存分片上传的单片记录。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `upload_id` | `uploadId` | 是 | 分片上传会话 ULID |
| `part_number` | `partNumber` | 是 | 分片序号 |
| `etag` | `etag` | 是 | 分片校验标识 |
| `size` | `size` | 是 | 分片大小 |
| `created_at` | `createdAt` | 是 | 创建时间 |

约束：
- 唯一约束：`upload_id + part_number`。
- `part_number` 从 1 开始。

## Application Layer

- `StorageApplicationService`：普通上传、对象查询、内容读取、对象删除。
- `StorageReferenceApplicationService`：建立引用、清理引用、刷新引用状态。
- `MultipartUploadApplicationService`：初始化分片、上传分片、完成分片、取消分片。

事务边界：
- 普通上传先写底层对象，再保存 `storage_object` 主数据。
- 删除对象必须更新对象状态，并调用底层存储释放内容。
- 建立和清理引用必须同步刷新对象引用状态。
- 完成分片上传必须合并底层对象、生成 `storage_object`、更新分片会话状态。

## Interface Layer

后台接口固定使用 `/api/admin/storage/**`。

- `POST /storage/objects/upload`：普通上传。
- `GET /storage/objects`：分页查询文件对象。
- `GET /storage/objects/{objectId}`：读取文件对象元数据。
- `GET /storage/objects/{objectId}/content`：读取文件内容。
- `DELETE /storage/objects/{objectId}`：删除文件对象。
- `POST /storage/objects/{objectId}/references`：建立引用。
- `DELETE /storage/objects/{objectId}/references`：清理引用。
- `POST /storage/multipart-uploads`：初始化分片上传。
- `POST /storage/multipart-uploads/{uploadId}/parts`：上传分片。
- `POST /storage/multipart-uploads/{uploadId}/complete`：完成分片上传。
- `POST /storage/multipart-uploads/{uploadId}/abort`：取消分片上传。

portal 不提供通用 Storage Controller。portal 需要上传时，由具体业务模块定义专用接口并复用 Storage Application Service。

## Infrastructure Layer

- Repository：`StoredObjectRepository`、`StoredObjectReferenceRepository`、`MultipartUploadRepository`。
- Mapper：`StoredObjectMapper`、`StoredObjectReferenceMapper`、`MultipartUploadSessionMapper`、`MultipartUploadPartMapper`。
- PersistenceAssembler：`StoredObjectPersistenceAssembler`、`StoredObjectReferencePersistenceAssembler`、`MultipartUploadPersistenceAssembler`。
- 外部客户端：`ObjectStorageClient`，来自 `kuzhambu-common-oss`。
- 缓存：文件对象元数据、引用状态。
- 文件存储：local / S3 兼容对象存储。

## Data Ownership

- 本模块拥有：`storage_object`、`storage_object_reference`、`storage_multipart_upload`、`storage_multipart_upload_part`。
- 本模块只读引用：当前认证上下文。
- 禁止跨域直接访问：业务模块不得直接读取底层 `object_key`。
- Flyway 脚本归属：`kuzhambu-admin-api` 的 `db/migration/V1__init.sql`，按 Storage 分段。

## Observability

- 运行日志：上传失败、底层存储失败、分片状态非法。
- 访问日志：由接口层统一记录。
- 审计日志：文件引用变化由业务对象审计记录；Storage 自身不承担业务审计。
- 关键指标：上传数、读取数、删除数、分片上传成功数、底层存储失败数。

## Acceptance

- 普通上传、内容读取、删除、引用管理和分片上传可用。
- 业务模块只能保存 `objectId` 或语义包装后的文件引用。
- 删除对象后无法继续读取。
- 分片上传完成后生成普通文件对象。
- 取消分片上传后不生成可用文件对象。
