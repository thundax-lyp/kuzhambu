# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `storage-domain-model`：删除 object owner 与 reference 记录状态字段
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-OWNER-CLEANUP.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/entity/StoredObject.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/entity/StoredObjectReference.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/persistence/dataobject/StoredObjectDO.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/persistence/dataobject/StoredObjectReferenceDO.java`
    - 处理动作：从领域对象和 DO 中删除冗余 owner / referenceStatus 字段
    - 验收点：`StoredObject` 不再暴露 `ownerType / ownerId`，`StoredObjectReference` 不再暴露 `referenceStatus`
    - 重要度：10/10

- [ ] `storage-infra-mapping`：清理 persistence 与 cache 映射
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-OWNER-CLEANUP.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/persistence/assembler/StoragePersistenceAssembler.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/cache/StorageCacheSupport.java`
    - 处理动作：删除 object owner 和 reference 记录状态在 persistence / cache 中的读写映射
    - 验收点：persistence / cache 层不再读写 `storage_object.owner_type / owner_id` 和 `storage_object_reference.reference_status`
    - 重要度：9/10

- [ ] `storage-facade-binding`：删除 bindOwner 改 owner 的链路
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-OWNER-CLEANUP.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/facade/impl/StorageFacadeImpl.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/test/java/com/thundax/kuzhambu/storage/application/facade/impl/StorageFacadeImplTest.java`
    - 处理动作：移除 `bindOwner -> changeOwner` 逻辑并改写对应测试
    - 验收点：binding 链路不再更新 object owner，旧的 owner mutation 测试已删除或重写
    - 重要度：10/10

- [ ] `storage-application-reference`：统一只维护有效引用与派生 reference_status
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-OWNER-CLEANUP.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/StorageApplicationService.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/StorageApplicationServiceImpl.java`
    - 处理动作：将引用维护改成“新增有效记录 / 删除有效记录 / 汇总 object 级 reference_status”
    - 验收点：unbind 直接删除 reference 记录，`storage_object.reference_status` 由 application service 统一汇总维护，Storage 稳定支持多业务对象并发引用同一 object
    - 重要度：10/10

- [ ] `storage-repository-query`：收敛 repository 查询到新数据结构
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-OWNER-CLEANUP.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/repository/StoredObjectRepository.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/StoredObjectRepositoryImpl.java`
    - 处理动作：移除对 object owner 和 reference 记录状态字段的查询依赖
    - 验收点：repository 查询只依赖 `storage_object_reference` 有效记录和 object 级派生 `reference_status`
    - 重要度：9/10

- [ ] `admin-web-storage-object`：前端对象页改为引用语义
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-OWNER-CLEANUP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-types.ts`、`kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-service.ts`、`kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-page.tsx`、`kuzhambu-apps/admin-web/src/app.test.tsx`、`kuzhambu-apps/admin-web/e2e/storage/storage-object/storage-object.spec.ts`
    - 处理动作：将对象页从 owner 展示与筛选切换为 reference 语义并同步测试
    - 验收点：前端不再把 object owner 当成对象事实展示，筛选和断言与新的引用模型一致
    - 重要度：10/10

- [ ] `storage-http-response`：清理后端对象 response 的 owner 暴露
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-OWNER-CLEANUP.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/response/StorageObjectResponse.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/assembler/StorageInterfaceAssembler.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/test/java/com/thundax/kuzhambu/storage/interfaces/admin/StorageObjectUploadContractTest.java`
    - 处理动作：删除对象 response 中的 owner 字段并同步契约测试
    - 验收点：对象 response 不再把 object owner 作为对外对象事实暴露
    - 重要度：9/10

- [ ] `storage-http-upload-multipart`：删除 upload 和 multipart 的 owner 输入与响应暴露
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-OWNER-CLEANUP.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/test/java/com/thundax/kuzhambu/storage/interfaces/admin/StorageObjectMultipartUploadContractTest.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/facade/assembler/StorageUploadFacadeAssembler.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/request/InitMultipartUploadRequest.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/response/InitMultipartUploadResponse.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/response/CompleteMultipartUploadResponse.java`
    - 处理动作：删除 upload 和 multipart 的 owner 输入参数及响应字段
    - 验收点：upload / multipart 请求不再接收 `ownerType / ownerId`，响应也不再暴露 object owner
    - 重要度：9/10

- [ ] `storage-http-query`：将对象查询接口改成 reference 筛选语义
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-OWNER-CLEANUP.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/StorageObjectController.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/assembler/StorageInterfaceAssembler.java`
    - 处理动作：将按 owner 查询 object 的接口语义改成 reference 语义或显式移除
    - 验收点：若仍保留筛选，必须明确是按 reference owner 筛选，而不是按 object owner 筛选
    - 重要度：9/10

- [ ] `storage-tests`：验证派生 reference_status 与删除路径
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-OWNER-CLEANUP.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/test/java/com/thundax/kuzhambu/storage/application/service/impl/StorageOrphanObjectCleanupSchedulerTest.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/test/java/com/thundax/kuzhambu/storage/interfaces/admin/StorageObjectDeleteContractTest.java`
    - 处理动作：补齐“有 reference 则 referenced，无 reference 则 unreferenced”以及删除链路一致性测试
    - 验收点：测试能证明派生状态规则与删除/cleanup 链路一致
    - 重要度：9/10

- [ ] `storage-doc-cleanup`：清理现场与临时文档入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-OWNER-CLEANUP.md`
    - 范围对象：`TODO.md`、`docs/30-designs/RUNBOOK-STORAGE-OWNER-CLEANUP.md`、`docs/AGENTS.md`
    - 处理动作：在代码和接口收口后删除 RUNBOOK、收窄 TODO 并清理临时引用
    - 验收点：RUNBOOK 已删除，TODO 只保留未完成项，入口文档无残留旧 owner 口径
    - 重要度：8/10

## 待讨论项
