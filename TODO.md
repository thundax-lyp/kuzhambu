# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `01-governance-archunit`：01 同步 application service 契约治理规则和 ArchUnit testcase
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-APPLICATION-STRONG-CONTRACT.md`
    - 范围对象：`docs/00-governance/SERVERS-ARCHITECTURE.md`、`docs/00-governance/SERVERS-ARCHITECTURE-RULES.md`、`kuzhambu-servers/common/kuzhambu-common-test/src/main/java/com/thundax/kuzhambu/common/test/architecture/LayerArchitectureRuleSupport.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/test/java/com/thundax/kuzhambu/storage/application/StorageApplicationArchitectureTest.java`
    - 处理动作：同步本域 domain entity 可作为 application service 返回、count 固定 primitive `long`、公开入参单 Command/Query 的治理与架构测试口径
    - 验收点：storage application 架构测试挂载 service boundary 检查，治理文档与 ArchUnit 规则口径一致
    - 重要度：10/10

- [ ] `02-storage-object-commands`：02 强类型化 storage object 写入命令
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-APPLICATION-STRONG-CONTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/command/{CreateStorageCommand.java,ChangeStorageCommand.java,UploadStorageObjectCommand.java,RemoveStorageReferencesCommand.java,AddStorageReferencesCommand.java,StorageSortCommand.java}`
    - 处理动作：把 storage object 写入命令中的 owner、mime、bucket、object key、size 等字段收敛到本域强类型值对象或本域 domain entity
    - 验收点：命令对象仍为纯字段对象，`AddStorageReferencesCommand.references` 保留 `List<StoredObjectReference>`，无新增方法
    - 重要度：9/10

- [ ] `03-multipart-commands`：03 强类型化 multipart 上传命令
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-APPLICATION-STRONG-CONTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/command/{InitMultipartUploadCommand.java,UploadMultipartPartCommand.java,CompleteMultipartUploadCommand.java,AbortMultipartUploadCommand.java}`
    - 处理动作：把 multipart upload id、part number、part size、total size、mime、bucket、object key 等字段改为本域强类型值对象
    - 验收点：multipart 命令公开字段不再使用可替代的裸 `String`、`Long`、`Integer`
    - 重要度：9/10

- [ ] `04-storage-queries`：04 新增 storage object 查询契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-APPLICATION-STRONG-CONTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/query/{GetStorageObjectQuery.java,ListStorageObjectsQuery.java,StorageObjectPageQuery.java,ListStorageMimeTypesQuery.java,ListStorageReferenceOwnerTypesQuery.java}`
    - 处理动作：新增对象读取、列表、分页和枚举列表查询对象，并迁移 `StorageQuery` 中对应字段
    - 验收点：`StorageObjectPageQuery.pageNo` 和 `pageSize` 使用 primitive `int`，分页 service 方法只接收单个 query
    - 重要度：9/10

- [ ] `05-storage-content-reference-queries`：05 新增引用和内容读取查询契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-APPLICATION-STRONG-CONTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/query/{ListStorageReferencesQuery.java,GetReadableStorageContentQuery.java,OpenReadableStorageContentQuery.java,StorageQuery.java}`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/command/RemoveStorageObjectCommand.java`
    - 处理动作：新增引用列表、可读内容校验、内容打开和删除对象命令，并收窄或淘汰泛化 `StorageQuery`
    - 验收点：application service 公开入参不再出现裸 `StoredObjectId` 或 `PageQuery`
    - 重要度：9/10

- [ ] `06-application-service-interfaces`：06 拆分业务化 application service 接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-APPLICATION-STRONG-CONTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/{StorageObjectApplicationService.java,StorageReferenceApplicationService.java,StorageContentApplicationService.java,StorageUploadApplicationService.java,StorageMultipartUploadApplicationService.java}`
    - 处理动作：用业务化接口替换现有大而全 `StorageApplicationService` 和 multipart 接口公开签名
    - 验收点：公开方法参数为无参或单个 Command/Query，`create` 返回 `StoredObject`，`count` 返回 primitive `long`，无 `save*` 方法
    - 重要度：10/10

- [ ] `07-object-reference-service-impl`：07 迁移对象和引用 application service 实现
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-APPLICATION-STRONG-CONTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/{StorageApplicationServiceImpl.java,StorageObjectApplicationServiceImpl.java,StorageReferenceApplicationServiceImpl.java,StorageOrphanObjectCleanupScheduler.java}`
    - 处理动作：把对象管理、排序、状态变更和引用增删改查迁移到业务化实现类
    - 验收点：对象和引用用例实现编译通过，公开返回保留本域 domain entity，旧实现不再承载已迁移方法
    - 重要度：10/10

- [ ] `08-content-upload-service-impl`：08 迁移内容读取、普通上传和分片上传实现
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-APPLICATION-STRONG-CONTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/{StorageContentApplicationServiceImpl.java,StorageUploadApplicationServiceImpl.java,MultipartUploadApplicationServiceImpl.java,StorageInputStreamLimiter.java}`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/result/StorageUploadResult.java`
    - 处理动作：拆出内容读取和上传实现，删除或停用 `StorageUploadResult`，失败路径改抛 `BizException`
    - 验收点：普通上传成功返回 `StoredObject`，分片上传继续返回本域 domain entity，上传失败不再通过 `Result.error` 表达
    - 重要度：10/10

- [ ] `09-application-facade-callers`：09 迁移 storage application facade 调用方
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-APPLICATION-STRONG-CONTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/facade/impl/StorageFacadeImpl.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/facade/assembler/{StorageReadableContentFacadeAssembler.java,StorageOwnerBindingFacadeAssembler.java,StorageUploadFacadeAssembler.java}`
    - 处理动作：把 facade request 到 Command/Query 的转换改为强类型，并改用拆分后的业务化 application service
    - 验收点：facade 对外协议不变，application facade 不再依赖旧 `StorageApplicationService`
    - 重要度：9/10

- [ ] `10-admin-interface-callers`：10 迁移 storage admin interface 调用方
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-APPLICATION-STRONG-CONTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/StorageObjectController.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/assembler/StorageInterfaceAssembler.java`
    - 处理动作：把 controller 和 interface assembler 调整为新 Query/Command 和业务化 application service
    - 验收点：HTTP Request/Response 字段和路径不变，interface 层仍只通过 assembler 暴露 response，不直接暴露 domain entity
    - 重要度：9/10

- [ ] `11-application-tests`：11 更新 storage application 测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-APPLICATION-STRONG-CONTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/test/java/com/thundax/kuzhambu/storage/application/service/impl/{StorageApplicationServiceDeleteTest.java,StorageApplicationServiceSortTest.java,StorageApplicationServiceUploadTest.java,MultipartUploadApplicationServiceImplTest.java,MultipartUploadApplicationServiceImplAbortTest.java}`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/test/java/com/thundax/kuzhambu/storage/application/facade/impl/StorageFacadeImplTest.java`
    - 处理动作：按新接口、强类型 Command/Query 和异常失败路径更新 application 层测试
    - 验收点：application 相关测试不再 mock 或调用旧 application service 签名
    - 重要度：9/10

- [ ] `12-interface-tests`：12 更新 storage interface 契约测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-APPLICATION-STRONG-CONTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/test/java/com/thundax/kuzhambu/storage/interfaces/admin/{StorageObjectContentContractTest.java,StorageObjectDeleteContractTest.java,StorageObjectMultipartUploadContractTest.java,StorageObjectUploadContractTest.java}`、`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/test/java/com/thundax/kuzhambu/storage/interfaces/StorageInterfaceArchitectureTest.java`
    - 处理动作：按新 application service 注入和强类型契约更新 HTTP 契约测试
    - 验收点：storage interface 契约测试仍验证原 HTTP JSON 和文件内容行为
    - 重要度：8/10

- [ ] `13-validation`：13 运行格式、静态搜索和 storage 模块测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-APPLICATION-STRONG-CONTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application`、`kuzhambu-servers/biz/storage/kuzhambu-storage-interface`
    - 处理动作：运行 RUNBOOK 指定的 formatter、测试和静态搜索验证
    - 验收点：storage application/interface 相关验证通过，或明确记录不可运行原因和剩余风险
    - 重要度：10/10

- [ ] `14-runbook-closure`：14 清理 RUNBOOK 并收窄 TODO
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-STORAGE-APPLICATION-STRONG-CONTRACT.md`、`TODO.md`
    - 处理动作：任务完成后删除临时 RUNBOOK，并删除或收窄已完成 TODO 项
    - 验收点：PR 收口时无已完成 TODO 残留，临时 RUNBOOK 已删除或结论已迁移到稳定文档
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
