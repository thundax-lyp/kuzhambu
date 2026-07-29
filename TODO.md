# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `04-storage-valueobject-codec-size-part`：新增 size、part 和 owner params codec
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-DOMAIN-STRONG-TYPING.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/codec/StorageByteSizeCodec.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/codec/MultipartPartSizeCodec.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/codec/MultipartPartNumberCodec.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/codec/StorageOwnerParamsCodec.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/test/java/com/thundax/kuzhambu/storage/domain/StorageValueObjectTest.java`
    - 处理动作：为 size、part size、part number 和 owner params 增加 `toDomain`、`toValue` 转换。
    - 验收点：`mvn -pl biz/storage/kuzhambu-storage-domain test` 通过，codec 能处理 null 和合法基础值。
    - 重要度：9/10

- [ ] `05-storage-stored-object-entity`：强类型化 StoredObject 和引用实体字段
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-DOMAIN-STRONG-TYPING.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/entity/StoredObject.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/entity/StoredObjectReference.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/persistence/assembler/StoragePersistenceAssembler.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/cache/StorageCacheSupport.java`
    - 处理动作：将 `mimeType`、`bucketName`、`objectKey`、`size`、`ownerParams` 改为目标值对象并在持久化和缓存边界转换。
    - 验收点：`referenceOwnerType` 仍为展示/查询用 `String`，DO 和 cache DTO 仍使用基础类型。
    - 重要度：10/10

- [ ] `06-storage-multipart-entity`：强类型化分片上传实体字段
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-DOMAIN-STRONG-TYPING.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/entity/MultipartUploadSession.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/entity/MultipartUploadPart.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/persistence/assembler/StoragePersistenceAssembler.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/MultipartUploadRepositoryImpl.java`
    - 处理动作：将分片会话和分片记录的 MIME、bucket、object key、size、part size、part number 改为值对象。
    - 验收点：`getUploadId()`、`setUploadId(String)` 兼容行为保留，MyBatis 查询使用 codec 转基础值。
    - 重要度：10/10

- [ ] `07-storage-object-repository-contract`：强类型化 StoredObjectRepository 平铺契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-DOMAIN-STRONG-TYPING.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/repository/StoredObjectRepository.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/StoredObjectRepositoryImpl.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/StorageApplicationServiceImpl.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/assembler/StorageInterfaceAssembler.java`
    - 处理动作：将 `listByIds`、`list`、`page`、`listMimeTypes` 的 ID、状态、owner type、MIME 参数改为强类型且保持平铺签名。
    - 验收点：`StoredObjectRepository` 不再暴露 `List<Long>` 对象 ID，且未引入 criteria 对象。
    - 重要度：10/10

- [ ] `08-storage-reference-repository-contract`：强类型化引用仓储契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-DOMAIN-STRONG-TYPING.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/repository/StoredObjectReferenceRepository.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/StoredObjectReferenceRepositoryImpl.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/StorageApplicationServiceImpl.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/facade/assembler/StorageOwnerBindingFacadeAssembler.java`
    - 处理动作：将 `deleteByObjectId` 改为 `StoredObjectId` 参数，并将 `listReferenceOwnerTypes` 改为返回 `StorageOwnerType`。
    - 验收点：`StorageOwnerRef.ownerId` 仍为 `String`，application 对外需要 `List<String>` 时显式转换。
    - 重要度：9/10

- [ ] `09-storage-multipart-repository-contract`：强类型化分片仓储契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-DOMAIN-STRONG-TYPING.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/repository/MultipartUploadRepository.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/MultipartUploadRepositoryImpl.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/MultipartUploadApplicationServiceImpl.java`
    - 处理动作：将 `getMultipartPart(MultipartUploadId, Integer)` 改为接收 `MultipartPartNumber`。
    - 验收点：multipart HTTP `partNumber` 仍为 number，进入 repository 前转换为强类型。
    - 重要度：9/10

- [ ] `10-storage-admin-web-contract-regression`：回归 Storage 管理页前端协议和控件操作
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-DOMAIN-STRONG-TYPING.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-types.ts`、`kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-service.ts`、`kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-page.tsx`、`kuzhambu-apps/admin-web/e2e/storage/storage-object/storage-object.spec.ts`
    - 处理动作：确认 Storage 管理页 request/response 字段保持基础类型，并回归搜索、筛选、上传、取消、预览、下载、删除、批量删除、拖拽排序和分页。
    - 验收点：前端 `id`、`contentType`、`size`、`referenceOwnerType`、`partNumber` 等协议字段保持现有类型，E2E 覆盖的控件操作通过。
    - 重要度：8/10

- [ ] `11-storage-strong-typing-closure`：执行全量验证并清理 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-DOMAIN-STRONG-TYPING.md`
    - 范围对象：`docs/30-designs/RUNBOOK-STORAGE-DOMAIN-STRONG-TYPING.md`、`TODO.md`、`kuzhambu-servers/biz/storage`、`kuzhambu-apps/admin-web`
    - 处理动作：运行 RUNBOOK 要求的后端和必要前端验证，完成后删除临时 RUNBOOK 并收窄或删除已完成 TODO。
    - 验收点：后端 storage 测试和必要 admin-web 契约测试通过，RUNBOOK 文件已删除或长期结论已迁移到治理/readiness 文档。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
