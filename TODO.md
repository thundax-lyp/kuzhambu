# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 当前任务项按数字编号顺序执行；不得跳过前置契约任务直接做后续调用方或重命名任务。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `2 三才资产CommandRecord与构造位置`：2. 清理三才资产 Command record 与 controller 构造 allowlist
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-07-CLASSICS-SANCAI-PUBLICATION.md`
    - 范围对象：
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiDraftCommand.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiEntryImageSortCommand.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiEntryImageUploadCommand.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiImageCommand.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiShowcaseCommand.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiAssetInterfaceAssembler.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/SancaiAssetAdminController.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/ClassicsApplicationCommandQueryRecordAllowances.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/ClassicsApplicationArchitectureTest.java`
    - 处理动作：将三才资产 Command 转为 Java record，并把资产 controller 中的 application Command 构造迁入 assembler。
    - 验收点：`SancaiAssetAdminController` 不再直接构造 application Command；对应 record 和 construction allowlist key 已删除。
    - 重要度：9/10

- [ ] `3 三才内容Portal构造位置`：3. 清理三才内容与 Portal 的 Command/Query 构造位置
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-07-CLASSICS-SANCAI-PUBLICATION.md`
    - 范围对象：
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/support/SancaiEntryVersionRestorer.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiInterfaceAssembler.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/SancaiAdminController.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/SancaiContentAdminController.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sancai/assembler/SancaiPortalInterfaceAssembler.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sancai/controller/SancaiPortalController.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/ClassicsApplicationArchitectureTest.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/ClassicsInterfaceArchitectureTest.java`
    - 处理动作：将三才 admin/portal controller 和版本恢复器中的 application Command/Query/PageQuery 构造迁移到合适转换边界。
    - 验收点：三才 admin/portal controller 不再直接构造 application Command/Query/PageQuery；对应 construction allowlist key 已删除。
    - 重要度：9/10

- [ ] `4 三才ApplicationService方法形态`：4. 清理三才资产 ApplicationService 方法形态
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-07-CLASSICS-SANCAI-PUBLICATION.md`
    - 范围对象：
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/SancaiAssetApplicationService.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/query/SancaiShowcaseQuery.java`（拟新增）
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiVisualAssetUseCommand.java`（拟新增）
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiVisualAssetVersionCommand.java`（拟新增）
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/ClassicsApplicationArchitectureTest.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/SancaiAssetApplicationServiceImplTest.java`
    - 处理动作：为三才资产 service 多参数或裸值方法引入专用 Command/Query。
    - 验收点：资产 service 公开方法输入符合 application 边界规则；对应 `METHOD_SHAPE` allowlist key 已删除；资产测试通过。
    - 重要度：9/10

- [ ] `5 发布ApplicationService方法形态`：5. 清理发布 ApplicationService 方法形态
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-07-CLASSICS-SANCAI-PUBLICATION.md`
    - 范围对象：
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/publication/service/ClassicsPublicationApplicationService.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/publication/service/ClassicsPublicationCleanupApplicationService.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/publication/service/ClassicsPublicationExecutionApplicationService.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/publication/service/ClassicsPublicationReconcileApplicationService.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/publication/service/ClassicsPublicationSnapshotBindApplicationService.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/publication/service/ClassicsPublicationContentCommitApplicationService.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/ClassicsApplicationArchitectureTest.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/publication/ClassicsPublicationStepExecutorTest.java`
    - 处理动作：对发布 workflow service 方法做最小结构调整，引入专用 Command 或迁入 support/executor 语义边界。
    - 验收点：发布状态机语义不变；发布相关测试通过；可安全收敛的 publication `METHOD_SHAPE` allowlist key 已删除。
    - 重要度：8/10

- [ ] `6 清理报表搜索方法形态`：6. 清理 cleanup/report/search ApplicationService 方法形态
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-07-CLASSICS-SANCAI-PUBLICATION.md`
    - 范围对象：
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/cleanup/service/ClassicsCleanupApplicationService.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/cleanup/service/impl/ClassicsCleanupApplicationServiceImpl.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/report/service/ClassicsReportApplicationService.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/report/service/impl/ClassicsReportApplicationServiceImpl.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/search/service/ClassicsSearchContentApplicationService.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/search/service/impl/ClassicsSearchContentApplicationServiceImpl.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/ClassicsApplicationArchitectureTest.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/search/ClassicsSearchContentApplicationServiceImplTest.java`
    - 处理动作：为 cleanup/report/search 的裸值或散列查询参数引入专用 Command/Query 或强类型输入。
    - 验收点：cleanup/report/search service 方法形态符合规则；对应 `METHOD_SHAPE` allowlist key 已删除；相关单测和 ArchUnit 通过。
    - 重要度：8/10

- [ ] `7 三才发布Repository命名`：7. 清理三才与发布 Repository 方法命名 allowlist
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-07-CLASSICS-SANCAI-PUBLICATION.md`
    - 范围对象：
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiRepository.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiAssetRepository.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/publication/repository/ClassicsPublicationJobRepository.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiRepositoryImpl.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiAssetRepositoryImpl.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/publication/repository/impl/ClassicsPublicationJobRepositoryImpl.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/test/java/com/thundax/kuzhambu/classics/domain/ClassicsDomainArchitectureTest.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/test/java/com/thundax/kuzhambu/classics/infra/sancai/SancaiRepositoryTest.java`
    - 处理动作：按 repository 命名规则调整三才与发布 repository 方法名并同步 infra/test 调用方。
    - 验收点：三才 repository allowlist key 已删除；发布 repository 只保留确有状态机语义必要的精确 key。
    - 重要度：8/10

- [ ] `8 发布API模型注解`：8. 清理发布 API request/response 模型注解 allowlist
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-07-CLASSICS-SANCAI-PUBLICATION.md`
    - 范围对象：
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/publication/controller/request/ClassicsPublicationActionRequest.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/publication/controller/request/ClassicsPublicationBatchActionRequest.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/publication/controller/request/ClassicsPublicationJobGetRequest.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/publication/controller/request/ClassicsPublicationJobPageRequest.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/publication/controller/response/ClassicsPublicationCreateResponse.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/publication/controller/response/ClassicsPublicationBatchResponse.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/publication/controller/response/ClassicsPublicationBatchItemResponse.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/publication/controller/response/ClassicsPublicationJobResponse.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/ClassicsInterfaceArchitectureTest.java`
    - 处理动作：补齐发布 request/response 模型要求的注解并删除对应 annotation allowlist key。
    - 验收点：发布字段名、HTTP 路径和接口契约不变；发布 API 模型注解 key 清零或仅保留非本任务范围项。
    - 重要度：7/10

- [ ] `9 三才Request与Assembler契约`：9. 清理三才 request 注解与 admin assembler 非空契约 allowlist
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-07-CLASSICS-SANCAI-PUBLICATION.md`
    - 范围对象：
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiAssetRequest.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiCategoryRequest.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiEntryRequest.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiEntryPageRequest.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiEntryVersionRequest.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiVolumeRequest.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiInterfaceAssembler.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiAssetInterfaceAssembler.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/ClassicsInterfaceArchitectureTest.java`
    - 处理动作：补齐三才 admin request 模型注解并收敛 admin assembler 公共方法非空契约。
    - 验收点：三才 admin request 注解 key 已删除；三才 admin assembler non-null key 已删除或收窄为未处理文件的精确说明。
    - 重要度：7/10

- [ ] `10 三才Response与Portal注解`：10. 清理三才 response 与 Portal API 模型注解 allowlist
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-07-CLASSICS-SANCAI-PUBLICATION.md`
    - 范围对象：
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiAssetResponse.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiCategoryResponse.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiContentResponse.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiEntryResponse.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiEntryVersionResponse.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiVolumeResponse.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sancai/controller/request/SancaiPortalEntrySearchRequest.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sancai/controller/response/SancaiPortalCategoryResponse.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sancai/controller/response/SancaiPortalVolumeResponse.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/ClassicsInterfaceArchitectureTest.java`
    - 处理动作：补齐三才 response 与 portal request/response 模型注解。
    - 验收点：Portal 只读 ES READY 且未删除内容契约不变；三才 response 与 portal API 模型注解 key 清零。
    - 重要度：7/10

- [ ] `11 最终收口清理现场`：11. 清理 allowlist、TODO 和 RUNBOOK 现场
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-07-CLASSICS-SANCAI-PUBLICATION.md`
    - 范围对象：
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/ClassicsApplicationCommandQueryRecordAllowances.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/ClassicsApplicationArchitectureTest.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/test/java/com/thundax/kuzhambu/classics/domain/ClassicsDomainArchitectureTest.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/ClassicsInterfaceArchitectureTest.java`
        - `docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-07-CLASSICS-SANCAI-PUBLICATION.md`
    - 处理动作：确认本切片 allowlist key 清零或仅保留有理由的非本切片 key，并删除已完成 TODO 与本 RUNBOOK。
    - 验收点：本 RUNBOOK 不再留在仓库；`TODO.md` 不保留已完成项；工作区不包含临时执行现场。
    - 重要度：10/10

## 待讨论项
