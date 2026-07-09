# Storage Implementation Coverage

## Status

- 当前状态：已完成
- 覆盖范围：普通上传、分片上传、对象查询、内容读取、对象删除、引用关系、orphan 清理、本地/S3 兼容存储抽象。
- 真相源：`docs/10-requirements/STORAGE-REQUIREMENTS.md`、本文件。

## Completion Summary

- Storage 已按 `domain / application / infra / interface / facade` 落地，并接入 starter。
- 普通上传已支持 `multipart/form-data`、空文件校验、类型/后缀校验、大小控制、对象创建和读取地址返回。
- 分片上传已完成 initiate、uploadPart、complete、abort 全链路。
- 对象列表已支持文件名、类型、对象状态、引用 owner 和引用状态筛选。
- 内容读取已返回正确 `Content-Type`、预览/下载响应头和失败异常。
- 删除策略已固定为仅允许删除无引用对象；删除后常规读取不可见。
- `storage_object_reference` 已成为引用关系真相源，`storage_object.owner_type / owner_id` 与 `storage_object_reference.reference_status` 已移除。
- orphan 清理任务已支持超时 `UNREFERENCED -> DELETED`，并对满足阈值的删除对象执行底层物理删除。
- Classics 和 System 已稳定复用 Storage facade 完成 Wangqi 文件、Sancai 图片、分享资源、导出产物和用户头像等场景。
- Admin Web `/storage/storage-object` 已支持分页、筛选、排序、上传、删除、预览和下载。

## Open Items

- 无当前需求阻塞项。
- 多 owner 并发引用是否作为稳定对外能力开放仍待业务决策。
- 真实 S3 环境联调与外部告警不作为当前需求完成阻塞项。

## Validation Evidence

- Storage interface 契约测试覆盖上传、读取、删除和核心响应字段。
- 分片上传 application 和 contract 测试已覆盖 complete、abort、残留清理和合并。
- 删除与 orphan 清理已补充 application / infra 失败边界测试。

## Requirement Coverage Matrix

| 需求范围 | 状态 | 说明 |
| --- | --- | --- |
| 普通文件上传 | 已完成 | multipart、校验、对象创建和读取地址已完成 |
| 文件内容读取 | 已完成 | content type、预览/下载响应头和失败异常已完成 |
| 文件对象列表/查询 | 已完成 | 文件名、类型、owner、对象状态和引用状态筛选已完成 |
| 文件对象删除 | 已完成 | 无引用对象删除、删除后不可读已完成 |
| 分片上传 | 已完成 | 初始化、上传分片、完成、取消和残留清理已完成 |
| 业务文件引用 | 已完成 | bind/unbind、去重、引用真相源和跨域复用已完成 |
| 对象状态维护 | 已完成 | 对象状态和派生引用状态已完成 |
| 未引用对象清理 | 已完成 | 12 小时 orphan 阈值和物理删除推进已完成 |
| 本地/S3 兼容存储 | 已完成 | 通用对象存储抽象和本地路径已完成 |
