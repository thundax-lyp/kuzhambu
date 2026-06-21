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
- 上传、分页查询、内容读取、删除和内容读取访问控制链路具备完整接口与运行时实现。
- 文件上传支持 multipart/form-data，含空文件校验、允许类型/后缀限制、内容类型透传、文件大小控制、对象创建与返回 `accessEndpoint`。
- `StorageApplicationService` 已提供对象状态、引用状态、排序、查询、读取、创建/删除/引用管理、内容读取等核心能力。
- 分片上传领域模型与应用服务已实现：初始化、上传分片、完成与取消、分片元数据持久化与校验逻辑已在代码层完成。
- 已有定时清理器 `StorageOrphanObjectCleanupScheduler`，会清理超时未引用对象并触发底层存储删除。
- Storage 与 Classics 的业务读取闭环打通：Wangqi 文档、Sancai 条目、图片与分享读取场景通过业务域调用 Storage 进行上传与读取。
- 上传/读取接口与返回内容有契约测试覆盖（`StorageInterfaceArchitectureTest`、`StorageObjectUploadContractTest`、`StorageObjectContentContractTest`、`StorageObjectDeleteContractTest`）。

部分完成：

- 分片上传能力在应用层实现完整，但缺少 admin/portal 的公开分片上传接口路由与契约闭环（目前仅对象上传和传统上传接口可直接调度）。
- 文件删除接口已能删除数据库记录，但未见对“删除应立即同步物理存储清理且无引用对象可继续读取”的端到端验证与错误策略说明；当前依赖 orphan 清理任务。
- 文件引用管理能力未在 Storage 的 admin controller 暴露为独立 API，业务域通过 application service 直接调用，导致运维可操作性不一致。

未完成：

- Storage 未覆盖“portal 专用上传”能力（符合需求中“portal 上传必须由业务域定义专用入口并复用 Storage”——此项为职责分离，不在 Storage 本身范围内）。

## Requirement Coverage Matrix

| 需求项 | 状态 | 已完成部分 | 未完成部分 | 责任域 |
| --- | --- | --- | --- | --- |
| 普通文件上传（multipart） | 已完成 | 已支持 `multipart/form-data`、空文件和类型/后缀校验、对象创建、返回读取地址、契约测试覆盖 | 无 | Storage |
| 文件内容读取 | 已完成 | 已有按 ID 读取内容接口，返回正确 Content-Type 与下载/预览响应头，读取失败抛异常 | 无 | Storage |
| 文件对象列表/查询 | 已完成 | 已支持按文件名、类型、上传人、对象状态、引用状态等条件查询与分页 | 无 | Storage |
| 文件对象删除 | 部分完成 | 已有删除接口、对象记录删除逻辑与引用状态变更能力 | 删除后物理文件同步清理策略与“引用中阻止误删”语义未形成一体化对外接口说明 | Storage |
| 分片上传（初始化/分片上传/完成/取消） | 部分完成 | domain/application/infra 层完整实现，状态流转与完整性校验就绪 | admin 侧未提供对应 HTTP 路由与契约测试，接口闭环未对外打通 | Storage |
| 业务文件引用建立与清理 | 部分完成 | application service 提供 add/remove/changeReferenceStatus，Clasics 已稳定调用并形成业务闭环 | 缺少通用管理端接口与引用链路可观测性（portal 或 admin 管理端） | Storage / Classics |
| 读取权限与对象可访问边界 | 已完成 | Content API 已有基于 id 的读取端点与鉴权控制过滤骨架，配合调用方 token 控制场景使用 | 访问策略尚未形成更细粒度“业务类型/场景”的统一规范文档 | Storage |
| 未引用对象清理 | 外部依赖（内部实现） | Storage 实现了 12 小时 orphan 清理任务，含物理存储删除尝试 | 清理窗口与外部调用域的策略约定需明确到业务 SLA 与监控告警 | Storage / 运维流程 |
| 对象创建/读取回收站与幂等重用 | 外部依赖 | Storage 层模型支持按 owner/reference 组织对象归属，便于业务回收与复用 | 业务侧是否采用同一对象重用策略由调用域统一决定 | Storage / Classics |

## Follow-up Backlog

### B1 Storage 分片上传接口打通

状态：未完成。

目标：在 admin interface 增补 multipart 上传四段式入口（initiate/uploadPart/complete/abort），并与现有上传策略保持统一校验（后缀、大小、token、幂等与安全日志）。

### B2 删除/引用一致性规范

状态：部分完成。

目标：明确并落地“删除已引用对象的处理策略”（禁止/标记/延迟清理），对外文档化，补充接口与集成测试验证“删除后对象内容不可读取”。

### B3 引用管理可观测性

状态：未完成。

目标：提供 reference 管理与审计可视接口（或在现有业务域日志中统一补齐），便于运维查看引用数和历史变更。

### B4 运行时清理策略收敛

状态：未完成。

目标：明确 orphan 阈值、扫描频率、失败重试与异常告警行为，补齐存储层运行手册。
