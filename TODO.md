# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `server-artifact facade contract`：定义 `ServerArtifactStorageFacade` 协议骨架
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-facade/src/main/java/com/thundax/kuzhambu/storage/facade/ServerArtifactStorageFacade.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-facade/src/main/java/com/thundax/kuzhambu/storage/facade/request/StoreServerArtifactFacadeRequest.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-facade/src/main/java/com/thundax/kuzhambu/storage/facade/response/StoreServerArtifactFacadeResponse.java`
    - 处理动作：定义 `ServerArtifactStorageFacade` 及其 `request/response` 协议并固定不可变对象风格。
    - 验收点：服务端产物存储 facade 协议类全部位于规定包路径且只暴露 `Getter + Builder + private constructor` 形态。
    - 重要度：9/10

- [ ] `readable-content facade contract`：定义 `StorageReadableContentFacade` 协议骨架
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-facade/src/main/java/com/thundax/kuzhambu/storage/facade/StorageReadableContentFacade.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-facade/src/main/java/com/thundax/kuzhambu/storage/facade/request/GetReadableContentFacadeRequest.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-facade/src/main/java/com/thundax/kuzhambu/storage/facade/response/GetReadableContentFacadeResponse.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-facade/src/main/java/com/thundax/kuzhambu/storage/facade/dto/ReadableStoredObjectFacadeDto.java`
    - 处理动作：定义只读内容 facade 及其 `request/response/dto` 协议并固定不可变对象风格。
    - 验收点：只读内容 facade 协议类全部位于规定包路径且 `dto` 命名与包归位满足 RUNBOOK hard rule。
    - 重要度：9/10

- [ ] `reference facade contract`：定义 `StorageReferenceFacade` 协议骨架
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-facade/src/main/java/com/thundax/kuzhambu/storage/facade/StorageReferenceFacade.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-facade/src/main/java/com/thundax/kuzhambu/storage/facade/request/AddStorageReferencesFacadeRequest.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-facade/src/main/java/com/thundax/kuzhambu/storage/facade/request/RemoveStorageReferencesFacadeRequest.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-facade/src/main/java/com/thundax/kuzhambu/storage/facade/request/ChangeStorageReferenceStatusFacadeRequest.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-facade/src/main/java/com/thundax/kuzhambu/storage/facade/response/StorageReferenceFacadeResponse.java`
    - 处理动作：定义引用关系 facade 及其 `request/response` 协议并固定不可变对象风格。
    - 验收点：引用关系 facade 协议可独立表达新增引用、删除引用和变更引用状态三类跨域动作。
    - 重要度：9/10

- [ ] `server-artifact facade impl`：实现 `ServerArtifactStorageFacadeImpl` 与 `FacadeAssembler`
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/facade/impl/ServerArtifactStorageFacadeImpl.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/facade/assembler/ServerArtifactStorageFacadeAssembler.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/ServerArtifactStorageApplicationServiceImpl.java`
    - 处理动作：实现服务端产物 facade 并完成 `FacadeRequest -> internal params -> FacadeResponse` 适配。
    - 验收点：`ServerArtifactStorageFacade` 可由 storage application 提供稳定实现且不向外暴露内部 command/result。
    - 重要度：9/10

- [ ] `readable-content facade impl`：实现 `StorageReadableContentFacadeImpl` 与 `FacadeAssembler`
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/facade/impl/StorageReadableContentFacadeImpl.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/facade/assembler/StorageReadableContentFacadeAssembler.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/StorageApplicationService.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/content/StoredObjectContent.java`
    - 处理动作：实现只读内容 facade 并把跨域只读出口收敛到 facade 协议。
    - 验收点：外域可通过 `StorageReadableContentFacade` 获取只读内容而不再需要直接认识 `StorageApplicationService` 的只读形状。
    - 重要度：9/10

- [ ] `reference facade impl`：实现 `StorageReferenceFacadeImpl` 与 `FacadeAssembler`
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/facade/impl/StorageReferenceFacadeImpl.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/facade/assembler/StorageReferenceFacadeAssembler.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/StorageApplicationService.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/command/AddStorageReferencesCommand.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/command/RemoveStorageReferencesCommand.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/command/ChangeStorageReferenceStatusCommand.java`
    - 处理动作：实现引用关系 facade 并把跨域引用操作收敛到 facade 协议。
    - 验收点：外域可通过 `StorageReferenceFacade` 完成引用新增、删除和状态变更而不再直接依赖 storage application command 语义。
    - 重要度：9/10

- [ ] `cross-application isolation rule`：引入 cross-application isolation rule 与迁移白名单
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/common/kuzhambu-common-test/src/main/java/com/thundax/kuzhambu/common/test/architecture/CrossApplicationIsolationArchitectureRuleSupport.java`、`kuzhambu-servers/common/kuzhambu-common-test/src/main/java/com/thundax/kuzhambu/common/test/architecture/ArchitectureSourceSupport.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/test/java/com/thundax/kuzhambu/storage/application/StorageApplicationArchitectureTest.java`
    - 处理动作：新增跨 application 隔离门禁并按模块级粒度引入 legacy allowlist。
    - 验收点：ArchUnit 默认禁止新的业务域 `application -> application` 横向依赖且仅对白名单中的历史模块依赖暂时放行。
    - 重要度：10/10

- [ ] `facade placement rule`：引入 facade placement rule 与 facade 协议门禁
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/common/kuzhambu-common-test/src/main/java/com/thundax/kuzhambu/common/test/architecture/FacadeArchitectureRuleSupport.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-facade/src/test/java/com/thundax/kuzhambu/storage/facade/StorageFacadeArchitectureTest.java`、`kuzhambu-servers/common/kuzhambu-common-test/src/main/java/com/thundax/kuzhambu/common/test/architecture/NamingArchitectureRuleSupport.java`
    - 处理动作：新增 facade 包归位、命名与 `request/response/dto` 协议门禁。
    - 验收点：ArchUnit 能检查 `Facade`、`FacadeRequest`、`FacadeResponse` 与 `FacadeDto` 的包路径和命名是否满足 RUNBOOK hard rule。
    - 重要度：10/10

- [ ] `operations to storage-facade`：首批迁移 `operations-application -> storage-facade`
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportTaskExecutor.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-facade/src/main/java/com/thundax/kuzhambu/storage/facade/ServerArtifactStorageFacade.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportTaskExecutorTest.java`
    - 处理动作：将报表产物存储入口从 storage application 改为 `ServerArtifactStorageFacade`。
    - 验收点：`operations-application` 不再直接依赖 `ServerArtifactStorageApplicationService` 即可完成报表产物落库。
    - 重要度：8/10

- [ ] `classics upload to storage-facade`：首批迁移 `classics-application` 的最小上传入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-facade/src/main/java/com/thundax/kuzhambu/storage/facade/StorageUploadFacade.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImplTest.java`
    - 处理动作：将三才图会资源上传入口改为依赖 `StorageUploadFacade` 并开始移除 `StorageUploadStreamHelper` 外溢。
    - 验收点：`SancaiAssetApplicationServiceImpl` 的最小上传路径不再直接依赖 `StorageUploadStreamHelper`。
    - 重要度：8/10

- [ ] `classics readable-content to storage-facade`：首批迁移 `classics-application` 的最小只读内容入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/service/impl/ClassicsSharingApplicationServiceImpl.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-facade/src/main/java/com/thundax/kuzhambu/storage/facade/StorageReadableContentFacade.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sharing/service/impl/ClassicsSharingApplicationServiceImplTest.java`
    - 处理动作：将分享只读内容调用改为依赖 `StorageReadableContentFacade`。
    - 验收点：`ClassicsSharingApplicationServiceImpl` 不再直接使用 storage application 的只读内容入口。
    - 重要度：8/10

- [ ] `system avatar to storage-facade`：首批迁移 `system-application` 的最小头像入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/service/impl/CurrentUserApplicationServiceImpl.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-facade/src/main/java/com/thundax/kuzhambu/storage/facade/StorageReadableContentFacade.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/core/service/impl/CurrentUserApplicationServiceImplTest.java`
    - 处理动作：将头像读取入口改为依赖 `StorageReadableContentFacade` 并新增对应应用层测试。
    - 验收点：`CurrentUserApplicationServiceImpl#getAvatarInputStream` 不再直接依赖 storage application 或 `StoredObjectStore` 的跨域只读形状。
    - 重要度：8/10

- [ ] `storage facade closure cleanup`：清理 Storage facade 试点现场
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-FACADE-ISOLATION.md`
    - 范围对象：`TODO.md`、`docs/30-designs/RUNBOOK-STORAGE-FACADE-ISOLATION.md`、相关实现覆盖文档与 PR 描述
    - 处理动作：在试点闭环完成后删除已完成 TODO、移除已完成 RUNBOOK 并同步实现覆盖与 PR 收口信息。
    - 验收点：`TODO.md` 不保留已完成项、已完成 RUNBOOK 被删除且文档与 PR 对试点收口状态口径一致。
    - 重要度：7/10

## 待讨论项
