# Storage Design

## Purpose

本文档定义 Storage 域设计，覆盖文件对象、文件引用、内容读取、普通上传和分片上传。

## Module

```text
kuzhambu-servers/biz/storage/
  kuzhambu-storage-interface/
  kuzhambu-storage-application/
  kuzhambu-storage-domain/
  kuzhambu-storage-infra/
```

## Business Boundary

Storage 拥有文件对象和文件引用事实。业务域只保存稳定文件对象标识或语义包装后的引用，不保存底层对象键、物理路径或存储实现细节。

## DDD Model

- `StorageObject`
- `StorageObjectReference`
- `MultipartUpload`
- `MultipartUploadPart`
- `StorageReadToken`

## Data Model

表名前缀统一使用 `storage_`。

核心表：

- `storage_object`
- `storage_object_reference`
- `storage_multipart_upload`
- `storage_multipart_upload_part`

## Application Layer

- `StorageUploadApplicationService`
- `StorageObjectApplicationService`
- `StorageReferenceApplicationService`
- `MultipartUploadApplicationService`
- `StorageReadApplicationService`

Application 层负责上传校验、文件对象创建、引用幂等建立和清理、分片上传状态流转、删除校验和内容读取。

## Interface Layer

Admin 入口：

- 普通文件上传。
- 文件对象查询和读取。
- 分片上传初始化、上传分片、完成和取消。

Portal 入口：

- 不提供通用上传入口。
- 需要上传时由具体业务域定义专用入口并复用 Storage application 能力。

## Infrastructure Layer

- 本地文件和 S3 兼容对象存储适配。
- Repository 持久化文件对象和引用状态。
- 文件内容读取不暴露底层存储实现。

## Data Ownership

Storage 是 `storage_*` 表的唯一写入方。业务域不得绕过 Storage 写入文件内容。

## Observability

- 上传失败、删除失败和底层存储异常记录运行日志。
- 文件引用变化由业务对象所在域通过 System 业务审计记录。

## Acceptance

- 文件对象、引用和底层存储实现解耦。
- 有引用文件不会被误清理，删除后不能继续读取。
