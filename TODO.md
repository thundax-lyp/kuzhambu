# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `classics-domain-sancai`：定义三才图会核心领域模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`SancaiCategory.java`、`SancaiVolume.java`、`SancaiEntry.java`、`SancaiEnums.java`、`SancaiRepository.java`
    - 处理动作：定义三才图会门类、卷、条目、状态枚举和仓储端口。
    - 验收点：模型覆盖浏览、编辑、生命周期、可见性和状态筛选所需字段。
    - 重要度：10/10

- [ ] `classics-domain-sancai-asset`：定义三才图会资产领域模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`SancaiEntryDraft.java`、`SancaiEntryImage.java`、`SancaiVisualAsset.java`、`SancaiShowcase.java`、`SancaiAssetRepository.java`
    - 处理动作：定义草稿、图片、视觉资产、静态展示页面和仓储端口。
    - 验收点：模型覆盖自动保存、配图、视觉资产版本和静态展示生成记录。
    - 重要度：9/10

- [ ] `classics-domain-wangqi`：定义王圻文档领域模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`WangqiDocument.java`、`WangqiEnums.java`、`WangqiDocumentRepository.java`
    - 处理动作：定义王圻文档实体、状态枚举和仓储端口。
    - 验收点：模型覆盖正文、摘要、文档时间、Storage 对象和可见性。
    - 重要度：8/10

- [ ] `classics-domain-mingcustoms`：定义明代习俗领域模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`MingCustomsEntry.java`、`MingCustomsKeyword.java`、`MingCustomsEnums.java`、`MingCustomsRepository.java`
    - 处理动作：定义明代习俗实体、关键词、状态枚举和仓储端口。
    - 验收点：模型覆盖分类、章节、正文、原文摘录、关键词和可见性。
    - 重要度：8/10

- [ ] `classics-domain-content`：定义通用内容领域模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`ClassicsContentTag.java`、`ClassicsContentQaPair.java`、`ClassicsContentVersion.java`、`ClassicsContentExportJob.java`、`ClassicsContentRepository.java`
    - 处理动作：定义标签、问答对、版本、导出和仓储端口。
    - 验收点：公共模型使用 `content_type + content_id` 表达内容身份。
    - 重要度：9/10

- [ ] `classics-domain-sharing`：定义分享领域模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`ClassicsShareLink.java`、`ClassicsShareTarget.java`、`ClassicsShareAccessRecord.java`、`ClassicsSharingEnums.java`、`ClassicsSharingRepository.java`
    - 处理动作：定义分享链接、分享目标、访问记录、状态枚举和仓储端口。
    - 验收点：分享 token 只持久化哈希，目标保留可见性快照。
    - 重要度：9/10

- [ ] `classics-application-sancai`：实现三才图会内容应用服务
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`SancaiApplicationService.java`、`SancaiApplicationServiceImpl.java`、`SancaiEntryPageQuery.java`、`SancaiEntrySaveCommand.java`、`SancaiEntryStatusCommand.java`
    - 处理动作：实现浏览、保存、生命周期和可见性用例。
    - 验收点：支持门类卷条目查询、条目保存、发布、归档、恢复和公开私有修改。
    - 重要度：10/10

- [ ] `classics-application-sancai-asset`：实现三才图会资产应用服务
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`SancaiAssetApplicationService.java`、`SancaiAssetApplicationServiceImpl.java`、`SancaiDraftSaveCommand.java`、`SancaiImageCommand.java`、`SancaiShowcaseCommand.java`
    - 处理动作：实现草稿、配图、视觉资产和静态展示页面用例。
    - 验收点：支持自动保存草稿、配图引用维护、视觉资产当前版本选择和静态展示记录。
    - 重要度：9/10

- [ ] `classics-application-wangqi`：实现王圻文档应用服务
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`WangqiDocumentApplicationService.java`、`WangqiDocumentApplicationServiceImpl.java`、`WangqiDocumentPageQuery.java`、`WangqiDocumentSaveCommand.java`、`WangqiDocumentVisibilityCommand.java`
    - 处理动作：实现王圻文档 CRUD、Storage 对象替换、时间线和可见性用例。
    - 验收点：文档正文和 Storage 对象替换互不破坏。
    - 重要度：8/10

- [ ] `classics-application-mingcustoms`：实现明代习俗应用服务
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`MingCustomsApplicationService.java`、`MingCustomsApplicationServiceImpl.java`、`MingCustomsPageQuery.java`、`MingCustomsSaveCommand.java`、`MingCustomsKeywordCommand.java`
    - 处理动作：实现明代习俗 CRUD、关键词、标签云和可见性用例。
    - 验收点：支持习俗列表、详情、关键词维护和标签云筛选。
    - 重要度：8/10

- [ ] `classics-application-content`：实现通用内容应用服务
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`ClassicsContentApplicationService.java`、`ClassicsContentApplicationServiceImpl.java`、`ContentTagCommand.java`、`ContentQaPairCommand.java`、`ContentExportCommand.java`
    - 处理动作：实现摘要、标签、问答对、版本和导出公共用例。
    - 验收点：三类内容可复用公共能力并按权限过滤导出范围。
    - 重要度：9/10

- [ ] `classics-application-sharing`：实现分享应用服务
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`ClassicsSharingApplicationService.java`、`ClassicsSharingApplicationServiceImpl.java`、`ShareLinkCreateCommand.java`、`ShareLinkStatusCommand.java`、`ShareAccessQuery.java`
    - 处理动作：实现分享创建、撤销、恢复、过期判断、只读访问和访问统计。
    - 验收点：分享创建不修改内容可见性，访问记录可统计。
    - 重要度：9/10

- [ ] `classics-infra-sancai`：实现三才图会主数据持久化
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`SancaiEntryDO.java`、`SancaiMapper.java`、`SancaiPersistenceAssembler.java`、`SancaiRepositoryImpl.java`
    - 处理动作：实现三才图会门类、卷、条目持久化。
    - 验收点：Mapper 只被 RepositoryImpl 调用，DO 不出 infra。
    - 重要度：10/10

- [ ] `classics-infra-sancai-asset`：实现三才图会资产持久化
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`SancaiAssetDO.java`、`SancaiAssetMapper.java`、`SancaiAssetPersistenceAssembler.java`、`SancaiAssetRepositoryImpl.java`
    - 处理动作：实现草稿、图片、视觉资产和静态展示页面持久化。
    - 验收点：Storage 只保存 `storage_object_id` 引用。
    - 重要度：9/10

- [ ] `classics-infra-wangqi`：实现王圻文档持久化
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`WangqiDocumentDO.java`、`WangqiDocumentMapper.java`、`WangqiDocumentPersistenceAssembler.java`、`WangqiDocumentRepositoryImpl.java`
    - 处理动作：实现王圻文档列表、详情、时间线和 Storage 对象引用更新。
    - 验收点：DO、Mapper 和 RepositoryImpl 路径符合服务器架构规则。
    - 重要度：8/10

- [ ] `classics-infra-mingcustoms`：实现明代习俗持久化
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`MingCustomsDO.java`、`MingCustomsMapper.java`、`MingCustomsPersistenceAssembler.java`、`MingCustomsRepositoryImpl.java`
    - 处理动作：实现习俗列表、详情、关键词查询和关键词维护。
    - 验收点：支持按分类、关键词和可见性筛选。
    - 重要度：8/10

- [ ] `classics-infra-content`：实现通用内容持久化
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`ClassicsContentDO.java`、`ClassicsContentMapper.java`、`ClassicsContentPersistenceAssembler.java`、`ClassicsContentRepositoryImpl.java`
    - 处理动作：实现标签、问答对、版本和导出持久化。
    - 验收点：公共表通过 `content_type + content_id` 查询。
    - 重要度：9/10

- [ ] `classics-infra-sharing`：实现分享持久化
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`ClassicsSharingDO.java`、`ClassicsSharingMapper.java`、`ClassicsSharingPersistenceAssembler.java`、`ClassicsSharingRepositoryImpl.java`
    - 处理动作：实现分享链接、分享目标和访问记录持久化。
    - 验收点：明文 token 不落库，访问记录可按链接和目标查询。
    - 重要度：9/10

- [ ] `classics-interface-sancai`：实现三才图会后台接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`SancaiAdminController.java`、`SancaiEntrySaveRequest.java`、`SancaiEntryResponse.java`、`SancaiInterfaceAssembler.java`、`docs/20-interfaces/CLASSICS-API.md`
    - 处理动作：实现三才图会后台浏览和维护接口。
    - 验收点：Controller 只依赖 `SancaiApplicationService`，响应模型不暴露 domain 类型。
    - 重要度：9/10

- [ ] `classics-interface-sancai-asset`：实现三才图会资产后台接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`SancaiAssetAdminController.java`、`SancaiAssetRequest.java`、`SancaiAssetResponse.java`、`SancaiAssetInterfaceAssembler.java`、`docs/20-interfaces/CLASSICS-API.md`
    - 处理动作：实现草稿、图片、视觉资产和静态展示接口。
    - 验收点：接口覆盖草稿保存、图片引用、视觉资产当前版本和静态展示记录。
    - 重要度：8/10

- [ ] `classics-interface-wangqi`：实现王圻文档后台接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`WangqiDocumentAdminController.java`、`WangqiDocumentRequest.java`、`WangqiDocumentResponse.java`、`WangqiDocumentInterfaceAssembler.java`、`docs/20-interfaces/CLASSICS-API.md`
    - 处理动作：实现王圻文档后台接口。
    - 验收点：接口覆盖文档列表、详情、保存、删除、Storage 对象替换和时间线。
    - 重要度：8/10

- [ ] `classics-interface-mingcustoms`：实现明代习俗后台接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`MingCustomsAdminController.java`、`MingCustomsRequest.java`、`MingCustomsResponse.java`、`MingCustomsInterfaceAssembler.java`、`docs/20-interfaces/CLASSICS-API.md`
    - 处理动作：实现明代习俗后台接口。
    - 验收点：接口覆盖习俗列表、详情、保存、删除、关键词和标签云。
    - 重要度：8/10

- [ ] `classics-interface-content`：实现通用内容后台接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`ClassicsContentAdminController.java`、`ClassicsContentRequest.java`、`ClassicsContentResponse.java`、`ClassicsContentInterfaceAssembler.java`、`docs/20-interfaces/CLASSICS-API.md`
    - 处理动作：实现摘要、标签、问答对、版本和导出接口。
    - 验收点：接口覆盖三类内容公共能力，不暴露 Knowledge 内部标签结构。
    - 重要度：8/10

- [ ] `classics-interface-sharing`：实现分享后台和 portal 接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`ClassicsSharingAdminController.java`、`ClassicsSharingPortalController.java`、`ClassicsSharingRequest.java`、`ClassicsSharingResponse.java`、`docs/20-interfaces/CLASSICS-API.md`
    - 处理动作：实现分享管理接口和分享只读访问接口。
    - 验收点：后台可管理分享链接，前台可只读访问分享内容并记录访问结果。
    - 重要度：9/10

- [ ] `classics-starter-admin`：装配 Classics 后台能力
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`kuzhambu-servers/starter/kuzhambu-admin-starter/pom.xml`、`kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/resources/application.yml`
    - 处理动作：将 Classics 后台模块依赖和运行时配置接入 admin starter。
    - 验收点：starter 不新增业务 Controller 或业务规则。
    - 重要度：8/10

- [ ] `classics-starter-portal`：装配 Classics portal 分享能力
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`kuzhambu-servers/starter/kuzhambu-portal-starter/pom.xml`、`kuzhambu-servers/starter/kuzhambu-portal-starter/src/main/resources/application.yml`
    - 处理动作：将 Classics 分享访问模块依赖和运行时配置接入 portal starter。
    - 验收点：portal starter 只做运行时装配。
    - 重要度：7/10

- [ ] `classics-verification-persistence`：增加 Classics 持久化验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`scripts/verify-classics.sh`、`scripts/verify-all.sh`、`SancaiRepositoryTest.java`
    - 处理动作：验证 schema、data 和 Repository 基础查询。
    - 验收点：可验证 15 个门类、99 卷和 3359 条三才图会条目可查询。
    - 重要度：8/10

- [ ] `classics-verification-api`：增加 Classics API 验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`SancaiAdminControllerTest.java`、`WangqiDocumentAdminControllerTest.java`、`MingCustomsAdminControllerTest.java`、`ClassicsSharingPortalControllerTest.java`
    - 处理动作：验证后台 API 和分享 portal API 基础契约。
    - 验收点：接口响应结构、权限标记和错误语义符合接口文档。
    - 重要度：8/10
