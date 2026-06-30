# Storage Implementation Coverage

## Purpose

本文档记录 Storage 域需求的当前实现覆盖状态，用于后续补充开发、跨域协作和验收确认。

本文档不替代 `docs/10-requirements/STORAGE-REQUIREMENTS.md` 和 `docs/30-designs/STORAGE-DESIGN.md`。

## Status Definition

- `已完成`：需求可在当前仓库里形成可追溯交付物，并可由运行时代码和接口闭环支持。
- `部分完成`：关键模型、数据结构或部分链路已就绪，但存在接口、调用点或闭环缺口。
- `未完成`：仓库当前未形成可执行实现、接口、测试或关键任务。
- `外部依赖`：能力边界不属于 Storage，或必须由其他域完成的运行时行为。

## Current Baseline

已完成：

- Storage 主体实现已落在 `kuzhambu-storage-domain / application / infra / interface`，并额外提供 `kuzhambu-storage-facade` 作为跨域稳定入口；启动模块已完成接线。
- 普通上传、分页查询、内容读取链路具备接口、运行时代码和前台管理页面闭环。
- 文件上传支持 multipart/form-data，含空文件校验、允许类型/后缀限制、内容类型透传、文件大小控制、对象创建与返回 `accessEndpoint`。
- `StorageApplicationService` 已提供对象状态、引用状态、排序、查询、读取、创建/删除/引用管理、内容读取等核心能力。
- 分片上传闭环已完成：admin controller 与 facade 提供完整 initiate/uploadPart/complete/abort；application 在 complete/abort 中补齐分片内容落存储、合并为正式对象和残留清理，分片场景契约测试与 application 测试均已补充。
- 已有定时清理器 `StorageOrphanObjectCleanupScheduler`，会清理超时未引用对象并触发底层存储删除。
- Storage 与 Classics / System 的业务读取闭环已打通：Wangqi 文档、Sancai 条目、分享读取和当前用户头像场景均通过业务域复用 Storage facade 进行上传、绑定与读取。
- interface 层已有契约测试覆盖上传、读取、删除等路由和核心响应字段（`StorageInterfaceArchitectureTest`、`StorageObjectUploadContractTest`、`StorageObjectContentContractTest`、`StorageObjectDeleteContractTest`），但当前仍以接口契约校验为主，未形成删除与物理清理一致性的端到端集成验证。
- Admin Web `/storage/storage-object` 页面已可分页查看对象、对象状态、引用状态、引用归属类型和归属 ID，并支持筛选、排序、上传、删除、预览和下载。
- portal 上传入口遵循职责分离：portal 上传由各业务域专用入口发起并复用 Storage，不由 Storage 通用入口承接。

部分完成：

未完成：
- 无。

## Requirement Coverage Matrix

| 需求项 | 状态 | 已完成部分 | 未完成部分 | 责任域 |
| --- | --- | --- | --- | --- |
| 普通文件上传（multipart） | 已完成 | 已支持 `multipart/form-data`、空文件和类型/后缀校验、对象创建、返回读取地址、契约测试覆盖 | 无 | Storage |
| 文件内容读取 | 已完成 | 已有按 ID 读取内容接口，返回正确 Content-Type 与下载/预览响应头，读取失败抛异常 | 无 | Storage |
| 文件对象列表/查询 | 已完成 | 已支持按文件名、类型、引用 owner、对象状态与引用状态等条件查询与分页 | 无 | Storage |
| 文件对象删除 | 已完成 | 已有删除接口；明确只允许删除无引用对象；删除时将对象标记为 `DELETED`，常规读取链路随即不可见；超时 `UNREFERENCED` orphan 会被自动推进到 `DELETED`；物理删除由后续计划任务异步完成 | 无 | Storage |
| 分片上传（初始化/分片上传/完成/取消） | 已完成 | admin/facade 与 application 全链路闭环，uploadPart 写入临时内容、complete 合并落库、abort 清理残留分片，测试覆盖 contract 与 application service | 无 | Storage |
| 业务文件引用建立与清理 | 已完成 | facade / application service 提供 bind/unbind、add/remove、referenceStatus 维护；application 去重并跳过已存在引用；`db/schema/storage.sql` 已明确引用复合主键；`storage_object.owner_type / owner_id` 与 `storage_object_reference.reference_status` 已从实现、接口和 schema 中移除，引用关系统一由有效 `storage_object_reference` 记录承载；Classics、System 已稳定复用并形成业务闭环；Admin Web 对象页已切换到引用 owner 语义，可查看引用状态和引用筛选 | 管理界面按边界不提供引用编辑；多 owner 并发引用是否作为稳定对外能力开放仍待业务决策 | Storage / Classics / System |
| 文件对象状态与引用状态维护 | 已完成 | 对象状态、引用状态及更新接口已可用 | 无 | Storage |
| 未引用对象清理 | 已完成 | Storage 已实现 orphan 清理任务：会将超时 `UNREFERENCED` 对象自动推进到 `DELETED`，并对已标记删除且满足阈值条件的对象执行底层物理删除；当前阈值固定为 `12 小时`，允许同一轮完成标记与清理 | 本轮不引入新的失败重试机制，也不接外部告警系统 | Storage |
| 本地文件和 S3 兼容对象存储适配 | 已完成 | Storage 已通过通用对象存储客户端抽象接入底层存储，当前本地存储运行路径明确；按本轮 RUNBOOK 口径，S3 真实环境联调与运维证据不作为当前打满阻塞项 | 无 | Storage / Common OSS |

## Follow-up Backlog

### B1 Storage Admin 分片上传接口打通

状态：已完成。

目标：在 admin interface 增补 multipart 上传四段式入口（initiate/uploadPart/complete/abort），并与现有上传策略保持统一校验（后缀、大小、token、幂等与安全日志）。

说明：`portal` 只承担展示与业务读取，不提供 Storage 通用上传入口；如需上传，必须由具体业务域定义专用入口并复用 Storage。

### B2 删除/引用一致性规范

状态：已完成。

目标：保持“仅允许删除无引用对象”的现行显式删除策略，并补齐“超时 `UNREFERENCED` 自动标记 `DELETED`”的自动删除线；同时补充接口与集成测试验证“删除后对象内容不可读取”“业务域先解绑后删除”的调用约束以及“物理清理何时发生”。

更新：Storage 已完成“显式删除 + 自动 orphan 删除”双路径收口；自动线会将超时 `UNREFERENCED` 对象推进到 `DELETED`，并由清理任务完成异步物理删除，相关 application / infra 测试已补齐失败边界。

### B3 运行时清理策略收敛

状态：已完成。

目标：明确 orphan 阈值、扫描频率、`UNREFERENCED -> DELETED` 推进规则、失败重试与异常告警行为，补齐存储层运行手册。

更新：当前稳定口径已固定为“orphan 阈值 12 小时、允许同一轮完成标记与清理”；本轮不引入新的失败重试机制，也不接外部告警系统。

### B4 Storage 引用模型与 Schema 真相源收敛

状态：已完成。

目标：继续收敛 `storage_object_reference` 的关系真相源语义，并移除冗余 owner / reference 状态字段。

更新：`db/schema/storage.sql`、`SERVERS-DATABASE-RULES.md`、domain/application/infra/interface 与 Admin Web 已对齐到统一语义：`storage_object_reference` 作为唯一引用关系真相源，`storage_object.owner_type / owner_id` 与 `storage_object_reference.reference_status` 已删除，`storage_object.reference_status` 仅保留对象级派生汇总状态，`bindOwner -> changeOwner` 的 owner 更新链路也已移除。

### B5 Classics 导出闭环产物入库

状态：已完成。

目标：在 Classics 导出任务与 Sancai 静态展示任务可执行路径上，明确 `StorageUploadStreamHelper` 的集成与产物入库字段归属；当前进度为前端导出/展示任务查询与下载入口与过期控制已接入。

更新：产物已通过 `StorageUploadStreamHelper` 写入 Storage 并进入导出/静态展示下载闭环，前端查询与下载入口已可见且导出过期禁用下载。
