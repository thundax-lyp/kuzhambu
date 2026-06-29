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
- 分片上传领域模型与应用服务已实现：初始化、上传分片、完成与取消、分片元数据持久化与校验逻辑已在代码层完成。
- 已有定时清理器 `StorageOrphanObjectCleanupScheduler`，会清理超时未引用对象并触发底层存储删除。
- Storage 与 Classics / System 的业务读取闭环已打通：Wangqi 文档、Sancai 条目、分享读取和当前用户头像场景均通过业务域复用 Storage facade 进行上传、绑定与读取。
- interface 层已有契约测试覆盖上传、读取、删除等路由和核心响应字段（`StorageInterfaceArchitectureTest`、`StorageObjectUploadContractTest`、`StorageObjectContentContractTest`、`StorageObjectDeleteContractTest`），但当前仍以接口契约校验为主，未形成删除与物理清理一致性的端到端集成验证。
- Admin Web `/storage/storage-object` 页面已可分页查看对象、对象状态、引用状态、引用归属类型和归属 ID，并支持筛选、排序、上传、删除、预览和下载。
- portal 上传入口遵循职责分离：portal 上传由各业务域专用入口发起并复用 Storage，不由 Storage 通用入口承接。

部分完成：

- 分片上传能力在 domain / application / infra 层实现完整，但缺少 admin 的公开 `initiate / uploadPart / complete / abort` 路由、契约测试与管理页面闭环；portal 侧按需求本就不提供通用入口。
- 文件删除接口当前执行软删除，将 `storage_object.object_status` 置为 `DELETED`；物理文件删除依赖 orphan 清理任务，已符合“先标记、后异步物理删除”的目标口径，但引用清理语义仍需和实现完全对齐。
- 文件删除链路未显式阻止“仍存在引用的对象被普通删除接口删除”；当前更多依赖业务域调用约束和后续清理逻辑，未形成统一对外语义。
- 文件引用管理能力主要通过 Storage facade / application service 供业务域调用；当前虽可在 Admin Web 查看引用状态和归属，且管理界面按设计不承担引用编辑职责。
- 引用幂等和多引用语义在 facade 调用路径上部分成立，但 application `addReferences` 仍是直接插入；`storage_object_reference` 的 Java 持久化模型未显式体现复合主键或唯一键，仓库内也未找到可检索的 `storage.sql` 真相源，引用模型约束仍需补强或文档化。
- 设计文档列出的 `StorageReadToken` 目前未见对应代码实现，设计与实现存在轻微偏差。

未完成：

- 无新增独立能力项；未完成内容主要集中在已有能力的对外闭环与一致性收口。

## Requirement Coverage Matrix

| 需求项 | 状态 | 已完成部分 | 未完成部分 | 责任域 |
| --- | --- | --- | --- | --- |
| 普通文件上传（multipart） | 已完成 | 已支持 `multipart/form-data`、空文件和类型/后缀校验、对象创建、返回读取地址、契约测试覆盖 | 无 | Storage |
| 文件内容读取 | 已完成 | 已有按 ID 读取内容接口，返回正确 Content-Type 与下载/预览响应头，读取失败抛异常 | 无 | Storage |
| 文件对象列表/查询 | 已完成 | 已支持按文件名、类型、上传人、对象状态、引用状态等条件查询与分页 | 无 | Storage |
| 文件对象删除 | 部分完成 | 已有删除接口、软删除逻辑，删除后对象无法再通过常规读取链路获取；按当前文档口径，物理删除允许由后续计划任务异步完成 | 引用清理与“存在引用时如何删除”的统一语义、端到端验证仍未完全收口 | Storage |
| 分片上传（初始化/分片上传/完成/取消） | 部分完成 | domain/application/infra 层完整实现，状态流转与完整性校验就绪 | admin 侧未提供 `initiate/uploadPart/complete/abort` 对外路由与契约测试闭环 | Storage |
| 业务文件引用建立与清理 | 部分完成 | facade / application service 提供 bind/unbind、add/remove、referenceStatus 维护；Classics、System 已稳定复用并形成业务闭环；Admin Web 对象页已可查看引用状态和归属筛选 | 管理界面按边界不提供引用编辑；application 直接插入引用，幂等与多引用约束更多依赖 facade 与数据库真实约束 | Storage / Classics / System |
| 文件对象状态与引用状态维护 | 已完成 | 对象状态、引用状态及更新接口已可用 | 无 | Storage |
| 未引用对象清理 | 部分完成 | Storage 实现了 orphan 清理任务，并对超时未引用对象尝试执行底层物理删除 | 清理触发策略、阈值、失败重试与外部告警未形成统一交付标准 | Storage |
| 本地文件和 S3 兼容对象存储适配 | 已完成 | Storage 已通过通用对象存储客户端抽象接入底层存储，当前本地存储运行路径明确；按本轮 RUNBOOK 口径，S3 真实环境联调与运维证据不作为当前打满阻塞项 | 无 | Storage / Common OSS |

## Follow-up Backlog

### B1 Storage Admin 分片上传接口打通

状态：未完成。

目标：在 admin interface 增补 multipart 上传四段式入口（initiate/uploadPart/complete/abort），并与现有上传策略保持统一校验（后缀、大小、token、幂等与安全日志）。

说明：`portal` 只承担展示与业务读取，不提供 Storage 通用上传入口；如需上传，必须由具体业务域定义专用入口并复用 Storage。

### B2 删除/引用一致性规范

状态：部分完成（进行中）。

目标：明确并落地“删除已引用对象的处理策略”（禁止/标记/延迟清理），并补充接口与集成测试验证“删除后对象内容不可读取”“引用如何释放”以及“物理清理何时发生”。

### B3 运行时清理策略收敛

状态：未完成。

目标：明确 orphan 阈值、扫描频率、失败重试与异常告警行为，补齐存储层运行手册。

### B4 Storage 引用模型与 Schema 真相源收敛

状态：未完成。

目标：明确 `storage_object_reference` 的唯一键/主键策略、多引用能力和幂等语义；补齐可检索 schema 文件或数据库治理文档中的真相源，并校准 DO/mapper/coverage 文档口径。

### B5 Classics 导出闭环产物入库

状态：已完成。

目标：在 Classics 导出任务与 Sancai 静态展示任务可执行路径上，明确 `StorageUploadStreamHelper` 的集成与产物入库字段归属；当前进度为前端导出/展示任务查询与下载入口与过期控制已接入。

更新：产物已通过 `StorageUploadStreamHelper` 写入 Storage 并进入导出/静态展示下载闭环，前端查询与下载入口已可见且导出过期禁用下载。
