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

- Storage 采用 `kuzhambu-storage-domain / application / infra / interface` 四层实现，模块边界与启动接入已就绪。
- 上传、分页查询、内容读取、删除链路具备完整接口与运行时实现。
- 文件上传支持 multipart/form-data，含空文件校验、允许类型/后缀限制、内容类型透传、文件大小控制、对象创建与返回 `accessEndpoint`。
- `StorageApplicationService` 已提供对象状态、引用状态、排序、查询、读取、创建/删除/引用管理、内容读取等核心能力。
- 分片上传能力已覆盖领域与应用层状态流转实现，但未见完整对外接口闭环（当前仅服务接口/应用服务具备能力）。
- 分片上传领域模型与应用服务已实现：初始化、上传分片、完成与取消、分片元数据持久化与校验逻辑已在代码层完成。
- 已有定时清理器 `StorageOrphanObjectCleanupScheduler`，会清理超时未引用对象并触发底层存储删除。
- Storage 与 Classics 的业务读取闭环打通：Wangqi 文档、Sancai 条目、图片与分享读取场景通过业务域调用 Storage 进行上传与读取。
- 上传/读取接口与返回内容有契约测试覆盖（`StorageInterfaceArchitectureTest`、`StorageObjectUploadContractTest`、`StorageObjectContentContractTest`、`StorageObjectDeleteContractTest`）。
- portal 上传入口遵循职责分离：portal 上传由各业务域专用入口发起并复用 Storage，不由 Storage 通用入口承接。

部分完成：

- 分片上传能力在应用层实现完整，但缺少 admin/portal 的公开分片上传接口路由与契约闭环（目前仅对象上传和传统上传接口可直接调度）。
- 文件删除接口已能删除数据库记录，但未见对“删除应立即同步物理存储清理且无引用对象可继续读取”的端到端验证与错误策略说明；当前依赖 orphan 清理任务。
- 文件引用管理能力主要通过 Storage 应用服务供业务域调用，当前缺少通用管理端 API，导致运维可视性不足。
- `storage:object:delete` 当前仅执行存储对象记录删除，不具备立即物理对象清理；物理清理依赖定时清理任务。

未完成：

无（该项为需求边界内职责分离：portal 专用上传由业务域负责专用入口）。

## Requirement Coverage Matrix

| 需求项 | 状态 | 已完成部分 | 未完成部分 | 责任域 |
| --- | --- | --- | --- | --- |
| 普通文件上传（multipart） | 已完成 | 已支持 `multipart/form-data`、空文件和类型/后缀校验、对象创建、返回读取地址、契约测试覆盖 | 无 | Storage |
| 文件内容读取 | 已完成 | 已有按 ID 读取内容接口，返回正确 Content-Type 与下载/预览响应头，读取失败抛异常 | 无 | Storage |
| 文件对象列表/查询 | 已完成 | 已支持按文件名、类型、上传人、对象状态、引用状态等条件查询与分页 | 无 | Storage |
| 文件对象删除 | 部分完成 | 已有删除接口、对象记录删除逻辑与引用状态变更能力 | 删除后物理文件同步清理策略与“引用中阻止误删”语义未形成一体化对外接口说明 | Storage |
| 分片上传（初始化/分片上传/完成/取消） | 部分完成 | domain/application/infra 层完整实现，状态流转与完整性校验就绪 | admin 侧未提供 `initiate/uploadPart/complete/abort` 对外路由与契约测试闭环 | Storage |
| 业务文件引用建立与清理 | 部分完成 | application service 提供 add/remove/changeReferenceStatus，Clasics 已稳定调用并形成业务闭环 | 缺少通用管理端接口与引用链路可观测性（portal 或 admin 管理端） | Storage / Classics |
| 文件对象状态与引用状态维护 | 已完成 | 对象状态、引用状态及更新接口已可用 | 无 | Storage |
| 未引用对象清理 | 部分完成 | Storage 实现了 orphan 清理任务并尝试同步物理删除 | 清理触发策略、阈值与外部告警未形成统一交付标准 | Storage |

## Follow-up Backlog

### B1 Storage 分片上传接口打通

状态：未完成。

目标：在 admin interface 增补 multipart 上传四段式入口（initiate/uploadPart/complete/abort），并与现有上传策略保持统一校验（后缀、大小、token、幂等与安全日志）。

### B2 删除/引用一致性规范

状态：部分完成（进行中）。

目标：明确并落地“删除已引用对象的处理策略”（禁止/标记/延迟清理），对外文档化，补充接口与集成测试验证“删除后对象内容不可读取”。

### B3 引用管理可观测性

状态：未完成。

目标：提供 reference 管理与审计可视接口（或在现有业务域日志中统一补齐），便于运维查看引用数和历史变更。

### B4 运行时清理策略收敛

状态：未完成。

目标：明确 orphan 阈值、扫描频率、失败重试与异常告警行为，补齐存储层运行手册。
