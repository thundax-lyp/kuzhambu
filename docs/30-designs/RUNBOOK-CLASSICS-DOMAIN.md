# RUNBOOK Classics Domain

## Purpose

本文档定义 Classics 古籍域完整实现手册，覆盖三才图会、王圻文档、明代习俗、通用内容能力、导出、静态展示页面和分享。

执行顺序固定为：`domain -> application -> infra -> interface -> starter -> verification`。每层按子域拆分，单个 TODO 关联文件保持 2-5 个。

本 RUNBOOK 是临时执行手册，任务关闭时应删除。

## Scope

覆盖：

- 三才图会门类、卷、条目浏览与维护。
- 三才图会草稿、生命周期、可见性、配图、视觉资产和静态展示页面。
- 王圻文档浏览、维护、Storage 对象关联、时间线和单文档问答入口。
- 明代习俗浏览、维护、关键词、标签云和详情弹窗。
- 三类内容标签、问答对、版本和导出。
- 跨知识库分享链接、目标、访问记录和访问统计。
- 后台 HTTP API、接口文档、启动装配和验证入口。

不覆盖：

- AI 调用执行和提示词配置。
- Knowledge 标签治理和知识图谱。
- Discovery 搜索索引和跨库问答会话生成。
- 前端页面实现。

## Global Rules

- 业务表不保存审计字段；操作者、创建者、更新者、删除者、发起人由 System 审计记录。
- `priority` 只作为单表内全局唯一排序字段，不参与普通 KEY 或组合 KEY。
- 业务状态、业务类型、格式和可见性使用 `varchar` 对应 Java enum。
- `starter` 只做运行时装配，不承载 Controller、业务规则、查询聚合或持久化实现。
- Controller 位于 `interfaces/admin/<subdomain>/controller/`。
- Repository port 位于 `domain/<subdomain>/repository/`，实现位于 `infra/<subdomain>/repository/impl/`。
- Mapper 位于 `infra/<subdomain>/persistence/mapper/`，DO 位于 `infra/<subdomain>/persistence/dataobject/`，持久化转换位于 `infra/<subdomain>/persistence/assembler/`。
- 三才图会新增条目继续使用数据库自增主键，不引入强类型业务 ID。
- 导出和静态展示页面第一版只记录任务，不同步生成产物；实际生成由后续 worker 接入。
- 分享访问第一版返回完整内容快照，不只返回目标列表或内容摘要。

## Domain Layer

### D1 Sancai Domain Model

目标：定义三才图会核心领域模型、状态枚举和仓储端口。

关联文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/.../domain/sancai/model/entity/SancaiCategory.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/.../domain/sancai/model/entity/SancaiVolume.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/.../domain/sancai/model/entity/SancaiEntry.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/.../domain/sancai/model/enums/SancaiEnums.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/.../domain/sancai/repository/SancaiRepository.java`

验收：模型覆盖浏览、编辑、生命周期、可见性和状态筛选所需字段。

### D2 Sancai Asset Domain Model

目标：定义三才图会草稿、图片、视觉资产和静态展示页面模型。

关联文件：

- `.../domain/sancai/model/entity/SancaiEntryDraft.java`
- `.../domain/sancai/model/entity/SancaiEntryImage.java`
- `.../domain/sancai/model/entity/SancaiVisualAsset.java`
- `.../domain/sancai/model/entity/SancaiShowcase.java`
- `.../domain/sancai/repository/SancaiAssetRepository.java`

验收：模型覆盖自动保存、配图、视觉资产版本、当前使用版本和静态展示生成记录。

### D3 Wangqi Domain Model

目标：定义王圻文档模型、状态枚举和仓储端口。

关联文件：

- `.../domain/wangqi/model/entity/WangqiDocument.java`
- `.../domain/wangqi/model/enums/WangqiEnums.java`
- `.../domain/wangqi/repository/WangqiDocumentRepository.java`

验收：模型覆盖文档标题、摘要、正文、格式、文档时间、Storage 对象和可见性。

### D4 Ming Customs Domain Model

目标：定义明代习俗模型、关键词模型、状态枚举和仓储端口。

关联文件：

- `.../domain/mingcustoms/model/entity/MingCustomsEntry.java`
- `.../domain/mingcustoms/model/entity/MingCustomsKeyword.java`
- `.../domain/mingcustoms/model/enums/MingCustomsEnums.java`
- `.../domain/mingcustoms/repository/MingCustomsRepository.java`

验收：模型覆盖分类、章节、正文、原文摘录、关键词和可见性。

### D5 Content Common Domain Model

目标：定义三类内容共享的标签、问答对、版本和导出模型。

关联文件：

- `.../domain/content/model/entity/ClassicsContentTag.java`
- `.../domain/content/model/entity/ClassicsContentQaPair.java`
- `.../domain/content/model/entity/ClassicsContentVersion.java`
- `.../domain/content/model/entity/ClassicsContentExportJob.java`
- `.../domain/content/repository/ClassicsContentRepository.java`

验收：公共模型使用 `content_type + content_id` 表达内容身份，不复制各内容主表字段。

### D6 Sharing Domain Model

目标：定义分享链接、分享目标、访问记录和仓储端口。

关联文件：

- `.../domain/sharing/model/entity/ClassicsShareLink.java`
- `.../domain/sharing/model/entity/ClassicsShareTarget.java`
- `.../domain/sharing/model/entity/ClassicsShareAccessRecord.java`
- `.../domain/sharing/model/enums/ClassicsSharingEnums.java`
- `.../domain/sharing/repository/ClassicsSharingRepository.java`

验收：分享 token 只持久化哈希，分享目标保留可见性快照和删除占位状态。

## Application Layer

### A1 Sancai Content Application Service

目标：实现三才图会浏览、条目维护、生命周期和可见性用例。

关联文件：

- `.../application/sancai/service/SancaiApplicationService.java`
- `.../application/sancai/service/impl/SancaiApplicationServiceImpl.java`
- `.../application/sancai/query/SancaiEntryPageQuery.java`
- `.../application/sancai/command/SancaiEntrySaveCommand.java`
- `.../application/sancai/command/SancaiEntryStatusCommand.java`

验收：支持门类卷条目查询、条目保存、发布、归档、恢复和公开私有修改。

### A2 Sancai Asset Application Service

目标：实现草稿、配图、视觉资产和静态展示页面用例。

关联文件：

- `.../application/sancai/service/SancaiAssetApplicationService.java`
- `.../application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java`
- `.../application/sancai/command/SancaiDraftSaveCommand.java`
- `.../application/sancai/command/SancaiImageCommand.java`
- `.../application/sancai/command/SancaiShowcaseCommand.java`

验收：支持自动保存草稿、配图引用维护、视觉资产当前版本选择和静态展示生成记录。

### A3 Wangqi Application Service

目标：实现王圻文档浏览、维护、Storage 对象关联、时间线和单文档问答入口。

关联文件：

- `.../application/wangqi/service/WangqiDocumentApplicationService.java`
- `.../application/wangqi/service/impl/WangqiDocumentApplicationServiceImpl.java`
- `.../application/wangqi/query/WangqiDocumentPageQuery.java`
- `.../application/wangqi/command/WangqiDocumentSaveCommand.java`
- `.../application/wangqi/command/WangqiDocumentVisibilityCommand.java`

验收：支持文档 CRUD、原始文档 Storage 对象替换、时间线查询和可见性修改。

### A4 Ming Customs Application Service

目标：实现明代习俗浏览、维护、关键词和标签云用例。

关联文件：

- `.../application/mingcustoms/service/MingCustomsApplicationService.java`
- `.../application/mingcustoms/service/impl/MingCustomsApplicationServiceImpl.java`
- `.../application/mingcustoms/query/MingCustomsPageQuery.java`
- `.../application/mingcustoms/command/MingCustomsSaveCommand.java`
- `.../application/mingcustoms/command/MingCustomsKeywordCommand.java`

验收：支持习俗 CRUD、关键词维护、标签云筛选和可见性修改。

### A5 Content Common Application Service

目标：实现摘要、标签、问答对、版本和导出公共用例。

关联文件：

- `.../application/content/service/ClassicsContentApplicationService.java`
- `.../application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
- `.../application/content/command/ContentTagCommand.java`
- `.../application/content/command/ContentQaPairCommand.java`
- `.../application/content/command/ContentExportCommand.java`

验收：三类内容可复用标签、问答对、版本历史、历史恢复和导出记录能力。

### A6 Sharing Application Service

目标：实现分享链接创建、批量创建、撤销、恢复、过期判断、只读访问和访问统计。

关联文件：

- `.../application/sharing/service/ClassicsSharingApplicationService.java`
- `.../application/sharing/service/impl/ClassicsSharingApplicationServiceImpl.java`
- `.../application/sharing/command/ShareLinkCreateCommand.java`
- `.../application/sharing/command/ShareLinkStatusCommand.java`
- `.../application/sharing/query/ShareAccessQuery.java`

验收：分享创建不修改内容可见性，私有分享按权限访问，访问记录可统计。

## Infrastructure Layer

### I1 Sancai Persistence

目标：实现三才图会主数据持久化。

关联文件：

- `.../infra/sancai/persistence/dataobject/SancaiEntryDO.java`
- `.../infra/sancai/persistence/mapper/SancaiMapper.java`
- `.../infra/sancai/persistence/assembler/SancaiPersistenceAssembler.java`
- `.../infra/sancai/repository/impl/SancaiRepositoryImpl.java`

验收：Mapper 只被 RepositoryImpl 调用，DO 不出 infra。

### I2 Sancai Asset Persistence

目标：实现三才图会草稿、图片、视觉资产和静态展示页面持久化。

关联文件：

- `.../infra/sancai/persistence/dataobject/SancaiAssetDO.java`
- `.../infra/sancai/persistence/mapper/SancaiAssetMapper.java`
- `.../infra/sancai/persistence/assembler/SancaiAssetPersistenceAssembler.java`
- `.../infra/sancai/repository/impl/SancaiAssetRepositoryImpl.java`

验收：Storage 只保存 `storage_object_id` 引用，不直接访问 Storage 表结构。

### I3 Wangqi Persistence

目标：实现王圻文档持久化。

关联文件：

- `.../infra/wangqi/persistence/dataobject/WangqiDocumentDO.java`
- `.../infra/wangqi/persistence/mapper/WangqiDocumentMapper.java`
- `.../infra/wangqi/persistence/assembler/WangqiDocumentPersistenceAssembler.java`
- `.../infra/wangqi/repository/impl/WangqiDocumentRepositoryImpl.java`

验收：支持文档列表、详情、时间线和 Storage 对象引用更新。

### I4 Ming Customs Persistence

目标：实现明代习俗和关键词持久化。

关联文件：

- `.../infra/mingcustoms/persistence/dataobject/MingCustomsDO.java`
- `.../infra/mingcustoms/persistence/mapper/MingCustomsMapper.java`
- `.../infra/mingcustoms/persistence/assembler/MingCustomsPersistenceAssembler.java`
- `.../infra/mingcustoms/repository/impl/MingCustomsRepositoryImpl.java`

验收：支持习俗列表、详情、关键词查询和关键词维护。

### I5 Content Common Persistence

目标：实现通用内容标签、问答对、版本和导出持久化。

关联文件：

- `.../infra/content/persistence/dataobject/ClassicsContentDO.java`
- `.../infra/content/persistence/mapper/ClassicsContentMapper.java`
- `.../infra/content/persistence/assembler/ClassicsContentPersistenceAssembler.java`
- `.../infra/content/repository/impl/ClassicsContentRepositoryImpl.java`

验收：公共表通过 `content_type + content_id` 查询，不穿透其他域 Repository。

### I6 Sharing Persistence

目标：实现分享链接、分享目标和访问记录持久化。

关联文件：

- `.../infra/sharing/persistence/dataobject/ClassicsSharingDO.java`
- `.../infra/sharing/persistence/mapper/ClassicsSharingMapper.java`
- `.../infra/sharing/persistence/assembler/ClassicsSharingPersistenceAssembler.java`
- `.../infra/sharing/repository/impl/ClassicsSharingRepositoryImpl.java`

验收：明文 token 不落库，访问记录可按链接和目标查询。

## Interface Layer

### F1 Sancai Admin Interface

目标：实现三才图会后台浏览和维护接口。

关联文件：

- `.../interfaces/admin/sancai/controller/SancaiAdminController.java`
- `.../interfaces/admin/sancai/controller/request/SancaiEntrySaveRequest.java`
- `.../interfaces/admin/sancai/controller/response/SancaiEntryResponse.java`
- `.../interfaces/admin/sancai/assembler/SancaiInterfaceAssembler.java`
- `docs/20-interfaces/CLASSICS-API.md`

验收：Controller 只依赖 `SancaiApplicationService`，请求响应模型不暴露 domain 类型。

### F2 Sancai Asset Admin Interface

目标：实现三才图会草稿、图片、视觉资产和静态展示接口。

关联文件：

- `.../interfaces/admin/sancai/controller/SancaiAssetAdminController.java`
- `.../interfaces/admin/sancai/controller/request/SancaiAssetRequest.java`
- `.../interfaces/admin/sancai/controller/response/SancaiAssetResponse.java`
- `.../interfaces/admin/sancai/assembler/SancaiAssetInterfaceAssembler.java`
- `docs/20-interfaces/CLASSICS-API.md`

验收：接口覆盖草稿保存、图片引用、视觉资产当前版本和静态展示生成记录。

### F3 Wangqi Admin Interface

目标：实现王圻文档后台接口。

关联文件：

- `.../interfaces/admin/wangqi/controller/WangqiDocumentAdminController.java`
- `.../interfaces/admin/wangqi/controller/request/WangqiDocumentRequest.java`
- `.../interfaces/admin/wangqi/controller/response/WangqiDocumentResponse.java`
- `.../interfaces/admin/wangqi/assembler/WangqiDocumentInterfaceAssembler.java`
- `docs/20-interfaces/CLASSICS-API.md`

验收：接口覆盖文档列表、详情、保存、删除、Storage 对象替换和时间线。

### F4 Ming Customs Admin Interface

目标：实现明代习俗后台接口。

关联文件：

- `.../interfaces/admin/mingcustoms/controller/MingCustomsAdminController.java`
- `.../interfaces/admin/mingcustoms/controller/request/MingCustomsRequest.java`
- `.../interfaces/admin/mingcustoms/controller/response/MingCustomsResponse.java`
- `.../interfaces/admin/mingcustoms/assembler/MingCustomsInterfaceAssembler.java`
- `docs/20-interfaces/CLASSICS-API.md`

验收：接口覆盖习俗列表、详情、保存、删除、关键词和标签云。

### F5 Content Common Admin Interface

目标：实现摘要、标签、问答对、版本和导出接口。

关联文件：

- `.../interfaces/admin/content/controller/ClassicsContentAdminController.java`
- `.../interfaces/admin/content/controller/request/ClassicsContentRequest.java`
- `.../interfaces/admin/content/controller/response/ClassicsContentResponse.java`
- `.../interfaces/admin/content/assembler/ClassicsContentInterfaceAssembler.java`
- `docs/20-interfaces/CLASSICS-API.md`

验收：接口覆盖三类内容公共能力，不暴露 Knowledge 内部标签表结构。

### F6 Sharing Admin And Portal Interface

目标：实现分享管理接口和分享只读访问接口。

关联文件：

- `.../interfaces/admin/sharing/controller/ClassicsSharingAdminController.java`
- `.../interfaces/portal/sharing/controller/ClassicsSharingPortalController.java`
- `.../interfaces/admin/sharing/controller/request/ClassicsSharingRequest.java`
- `.../interfaces/admin/sharing/controller/response/ClassicsSharingResponse.java`
- `docs/20-interfaces/CLASSICS-API.md`

验收：后台可管理分享链接，前台可只读访问分享内容并记录访问结果。

## Starter Layer

### S1 Admin Starter Assembly

目标：将 Classics 后台能力装配进后台启动应用。

关联文件：

- `kuzhambu-servers/starter/kuzhambu-admin-starter/pom.xml`
- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/resources/application.yml`

验收：starter 只新增模块依赖或运行时配置，不新增业务 Controller 或业务规则。

### S2 Portal Starter Assembly

目标：将分享访问等 portal 能力装配进前台启动应用。

关联文件：

- `kuzhambu-servers/starter/kuzhambu-portal-starter/pom.xml`
- `kuzhambu-servers/starter/kuzhambu-portal-starter/src/main/resources/application.yml`

验收：portal starter 只装配分享访问所需模块依赖或配置。

## Verification Layer

### V1 Classics Persistence Verification

目标：验证 schema、data 和 Repository 基础查询。

关联文件：

- `scripts/verify-classics.sh`
- `scripts/verify-all.sh`
- `.../infra/sancai/SancaiRepositoryTest.java`

验收：能验证 15 个门类、99 卷和 3359 条三才图会条目可查询。

### V2 Classics API Verification

目标：验证后台 API 和分享 portal API 基础契约。

关联文件：

- `.../interfaces/admin/sancai/SancaiAdminControllerTest.java`
- `.../interfaces/admin/wangqi/WangqiDocumentAdminControllerTest.java`
- `.../interfaces/admin/mingcustoms/MingCustomsAdminControllerTest.java`
- `.../interfaces/portal/sharing/ClassicsSharingPortalControllerTest.java`

验收：接口响应结构、权限标记和错误语义符合 `docs/20-interfaces/CLASSICS-API.md`。
