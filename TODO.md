# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `kuzhambu-servers/biz/classics`：创建 Classics Maven 模块骨架
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`kuzhambu-servers/biz/pom.xml`、`kuzhambu-servers/biz/classics/pom.xml`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/pom.xml`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/pom.xml`、`kuzhambu-servers/biz/classics/kuzhambu-classics-domain/pom.xml`
    - 处理动作：新增 Classics 多模块骨架并接入 Maven reactor。
    - 验收点：Maven 能识别 Classics interface、application、domain 模块。
    - 重要度：9/10

- [ ] `classics-domain/sancai`：定义三才图会只读领域模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`SancaiCategory.java`、`SancaiVolume.java`、`SancaiEntry.java`、`SancaiEnums.java`
    - 处理动作：定义三才图会门类、卷、条目和状态枚举。
    - 验收点：领域模型不依赖 infra，状态字段使用 enum 封装 varchar 值。
    - 重要度：8/10

- [ ] `classics-infra/sancai`：实现三才图会 Repository
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`SancaiRepository.java`、`SancaiRepositoryImpl.java`、`SancaiMapper.java`、`SancaiMapper.xml`
    - 处理动作：实现门类、卷、条目只读查询。
    - 验收点：可按门类查询卷、按卷查询条目、按条目 ID 查询详情。
    - 重要度：9/10

- [ ] `classics-application/sancai`：封装三才图会三级浏览服务
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`SancaiQueryApplicationService.java`、`SancaiCategoryView.java`、`SancaiVolumeView.java`、`SancaiEntryView.java`
    - 处理动作：将 Repository 查询结果组装为稳定只读视图。
    - 验收点：application 层不暴露 DO、Mapper 或 SQL 细节。
    - 重要度：8/10

- [ ] `classics-admin-api`：提供三才图会后台只读接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`SancaiAdminController.java`、`SancaiCategoryResponse.java`、`docs/20-interfaces/CLASSICS-API.md`
    - 处理动作：定义并实现后台三才图会门类、卷、条目查询接口。
    - 验收点：接口文档记录路径、参数、响应字段和错误语义。
    - 重要度：8/10

- [ ] `classics-content-attachments`：为条目详情组合标签和问答对
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`ContentAttachmentRepository.java`、`ContentAttachmentRepositoryImpl.java`、`ContentAttachmentMapper.xml`、`SancaiEntryDetailAssembler.java`
    - 处理动作：读取 `classics_content_tag` 和 `classics_content_qa_pair` 并组合到条目详情。
    - 验收点：条目详情可返回标签和问答对，不复制 Knowledge 标签主数据。
    - 重要度：7/10

- [ ] `scripts/verify-classics.sh`：增加 Classics 只读链路验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`scripts/verify-classics.sh`、`scripts/verify-all.sh`、`SancaiRepositoryTest.java`
    - 处理动作：新增 Classics 最小自动化验证入口并接入总验证。
    - 验收点：验证脚本符合 Prepare、Execute、Assert、Restore 协议。
    - 重要度：7/10

## 待讨论项

- [ ] 是否第一阶段只提供 admin API
    - 任务类型：待讨论项
    - 关联任务：`classics-admin-api`
    - 决策要求：确认 portal 只读 API 是否延后。
    - 重要度：6/10

- [ ] 条目详情是否第一阶段返回图片和视觉资产
    - 任务类型：待讨论项
    - 关联任务：`classics-content-attachments`
    - 决策要求：确认图片和视觉资产是否延后到视觉资产阶段。
    - 重要度：6/10
