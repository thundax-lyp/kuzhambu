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

`storage_object_reference` 是对象与业务对象关系的唯一真相源。`storage_object` 不再保留 `owner_type / owner_id` 这类归属字段；对象级 `reference_status` 只表示由有效引用集合汇总出的派生状态。

## DDD Model

- `StorageObject`
- `StorageObjectReference`
- `MultipartUpload`
- `MultipartUploadPart`

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

引用模型采用以下稳定语义：

- `storage_object_reference` 只保存当前有效引用记录。
- 同一 owner 对同一 object 的幂等由复合主键 `(object_id, reference_owner_type, reference_owner_id)` 保证。
- 解绑引用时直接删除对应引用记录，不维护引用关系历史状态。
- `storage_object.reference_status` 由是否存在有效引用记录派生：
  - 存在有效引用记录：`REFERENCED`
  - 不存在有效引用记录：`UNREFERENCED`
- 一个 object 是否允许被多个业务对象同时引用，由业务域规则控制，不由 Storage 主表字段控制。

分片上传闭环实现为四段式入口：

- `initiateMultipartUpload` 创建会话。
- `uploadPart` 接收分片并持久化为临时对象，写入 `storage_multipart_upload_part`。
- `completeMultipartUpload` 校验分片完整性与顺序，按顺序合并临时对象内容并落为 `storage_object`。
- `abortMultipartUpload` 删除会话内已上传分片内容并清理分片记录，更新会话状态为 `ABORTED`。

AI / Workers 文件类结果转存补充语义：

- Java AI 域从 Workers 下载 `temporary artifact reference` 对应临时产物后，再决定进入普通上传或分片上传链路。
- 小文件允许直接复用普通上传入口。
- 超过阈值的大文件必须走 `流式下载 + multipart upload`，不得默认一次性装入单次普通上传路径。
- 转存完成后，业务侧只认 `storage_object` 结果，不认 Workers 临时引用。

删除链路采用两阶段语义：

- 删除接口仅允许删除无引用对象，并先将 `storage_object` 标记为已删除。
- 长时间处于 `UNREFERENCED` 的对象会被视为 orphan，由计划任务自动推进到删除流程。
- orphan 保留阈值当前固定为 `12 小时`。
- 当前设计明确不支持“仍被引用对象由 Storage 自动释放全部引用后再删除”的通用删除语义；引用释放必须先由业务域按 owner 边界完成，再进入 Storage 删除流程。
- 无论人工删除还是自动 orphan 删除，只要对象进入删除标记状态，就不得继续通过正常业务链路读取或绑定。
- 底层物理文件删除不要求在接口调用内同步完成，由异步计划任务扫描删除标记对象并执行最终删除。
- 当前实现允许同一轮调度内完成“自动标记 orphan 为 `DELETED`”和“物理清理已删除对象”。

## Interface Layer

Admin 入口：

- 普通文件上传。
- 文件对象查询和读取。
- 分片上传初始化、上传分片、完成和取消。
- 仅允许删除无引用文件对象。

Admin 管理界面定位：

- 用于查看文件对象、当前引用状态和引用信息。
- 用于清理无引用文件对象。
- 不承担业务引用关系编辑职责。
- 不允许管理员手工修改文件对象引用状态，或直接建立/移除引用关系。

Portal 入口：

- 不提供通用上传入口。
- 需要上传时由具体业务域定义专用入口并复用 Storage application 能力。

## Infrastructure Layer

- 本地文件和 S3 兼容对象存储适配。
- Repository 持久化文件对象和有效引用记录，并维护对象级引用汇总状态。
- 文件内容读取不暴露底层存储实现。

## Data Ownership

Storage 是 `storage_*` 表的唯一写入方。业务域不得绕过 Storage 写入文件内容。

## Observability

- 上传失败、删除失败和底层存储异常记录运行日志。
- 文件引用变化由业务对象所在域通过 System 业务审计记录。
- 异步物理删除任务需要记录扫描、删除失败和重试相关运行日志。
- 本轮不引入新的失败重试机制，也不接外部告警系统；要求是异常可见且不会误删对象。

## Acceptance

- 文件对象、引用和底层存储实现解耦。
- 有引用文件不会被误清理，删除后不能继续读取。
- 删除接口完成后，对象已进入删除标记态；物理文件最终由计划任务删除。
- 超时 `UNREFERENCED` 对象会自动推进到 `DELETED`，并按当前调度语义进入异步物理清理。
