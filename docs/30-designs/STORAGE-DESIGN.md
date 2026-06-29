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

Application 层负责上传校验、文件对象创建、引用幂等建立和清理、分片上传状态流转、内容暂存与合并、删除校验和内容读取。

分片上传闭环实现为四段式入口：

- `initiateMultipartUpload` 创建会话。
- `uploadPart` 接收分片并持久化为临时对象，写入 `storage_multipart_upload_part`。
- `completeMultipartUpload` 校验分片完整性与顺序，按顺序合并临时对象内容并落为 `storage_object`。
- `abortMultipartUpload` 删除会话内已上传分片内容并清理分片记录，更新会话状态为 `ABORTED`。

删除链路采用两阶段语义：

- 删除接口先将 `storage_object` 标记为已删除或删除中，并清理该对象的引用关系。
- 删除标记完成后，对象不得继续通过正常业务链路读取或绑定。
- 底层物理文件删除不要求在接口调用内同步完成，由异步计划任务扫描并执行最终删除。

## Interface Layer

Admin 入口：

- 普通文件上传。
- 文件对象查询和读取。
- 分片上传初始化、上传分片、完成和取消。
- 仅允许删除无引用文件对象。

Admin 管理界面定位：

- 用于查看文件对象、当前引用状态和归属信息。
- 用于清理无引用文件对象。
- 不承担业务引用关系编辑职责。
- 不允许管理员手工修改文件对象归属、引用状态，或直接建立/移除引用关系。

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
- 异步物理删除任务需要记录扫描、删除失败和重试相关运行日志。

## Acceptance

- 文件对象、引用和底层存储实现解耦。
- 有引用文件不会被误清理，删除后不能继续读取。
- 删除接口完成后，对象已进入删除标记态并完成引用清理；物理文件最终由计划任务删除。
