# RUNBOOK Classics Domain

## Purpose

本文档定义 Classics 域第一阶段实现手册。目标是先完成三才图会只读闭环，再逐步接入内容附属能力和后台管理能力。

本 RUNBOOK 是临时执行手册，任务关闭后应删除。

## Scope

覆盖：

- Java servers 中 Classics 业务域模块骨架。
- 三才图会门类、卷、条目只读查询。
- 内容标签和问答对只读组合。
- 后台 API 契约和最小测试。
- 数据初始化脚本与 schema 对齐检查。

不覆盖：

- AI 生成执行。
- 知识图谱。
- 搜索引擎建设。
- 分享链接访问页。
- 导出异步任务。
- 前端页面实现。

## Implementation Principles

- 单个执行任务关联文件保持 2-5 个。
- 每个任务只做一个工程判断。
- 优先建立只读链路，避免提前实现写入状态流转。
- 业务表不写入审计字段；操作人、创建人、发起人由 System 审计系统记录。
- `priority` 只作为单表内全局唯一排序字段，不参与普通 KEY。
- 状态、类型、格式和可见性使用 `varchar` 对应 Java enum。

## Phase 1: Module Skeleton

目标：建立 Classics Java server 模块边界，先让 Maven reactor 能识别模块。

建议关联文件：

- `kuzhambu-servers/biz/pom.xml`
- `kuzhambu-servers/biz/classics/pom.xml`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/pom.xml`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/pom.xml`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/pom.xml`

验收：Classics 模块进入 Maven reactor，暂不要求业务 API 可用。

## Phase 2: Domain Model

目标：定义三才图会只读模型和状态枚举。

建议关联文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/.../sancai/SancaiCategory.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/.../sancai/SancaiVolume.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/.../sancai/SancaiEntry.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/.../sancai/SancaiEnums.java`

验收：领域模型不依赖 infra，不暴露数据库实现细节。

## Phase 3: Read Repository

目标：实现三才图会门类、卷、条目只读持久化访问。

建议关联文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/.../sancai/SancaiQueryRepository.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/.../sancai/SancaiQueryRepositoryImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/.../sancai/SancaiMapper.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/resources/mapper/SancaiMapper.xml`

验收：可按 `priority` 查询门类、按 `category_id` 查询卷、按 `volume_id` 查询条目。

## Phase 4: Application Query Service

目标：封装三才图会三级浏览和条目详情查询。

建议关联文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/.../SancaiQueryApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/.../SancaiCategoryView.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/.../SancaiVolumeView.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/.../SancaiEntryView.java`

验收：application 层返回稳定只读视图，不直接暴露 DO 或 Mapper 结果。

## Phase 5: Admin API

目标：提供后台三才图会只读接口。

建议关联文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/.../SancaiQueryApi.java`
- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/java/.../SancaiAdminController.java`
- `docs/20-interfaces/CLASSICS-API.md`

验收：接口契约记录路径、参数、响应字段和错误语义。

## Phase 6: Content Attachment Read Model

目标：为条目详情组合标签和问答对只读信息。

建议关联文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/.../content/ContentAttachmentRepository.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/.../content/ContentAttachmentRepositoryImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/resources/mapper/ContentAttachmentMapper.xml`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/.../SancaiEntryDetailAssembler.java`

验收：条目详情可返回标签和问答对，且不复制 Knowledge 标签主数据。

## Phase 7: Verification

目标：为 Classics 只读链路增加最小验证入口。

建议关联文件：

- `scripts/verify-classics.sh`
- `scripts/verify-all.sh`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/test/java/.../SancaiQueryRepositoryTest.java`

验收：验证脚本遵守 Prepare、Execute、Assert、Restore 协议，并接入总验证入口。

## Open Decisions

- Classics 是否在第一阶段接入 portal 只读 API，还是只做 admin API。
- 三才图会条目详情是否第一阶段返回图片和视觉资产，还是延后到视觉资产阶段。
- 初始化数据是否继续保留显式大 ID，还是迁移为自增 ID 加业务映射表。
