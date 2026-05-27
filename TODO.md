# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

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
