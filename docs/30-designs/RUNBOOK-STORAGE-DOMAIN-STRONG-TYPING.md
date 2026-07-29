# Storage Domain Strong Typing Runbook

## Purpose

本 RUNBOOK 用于执行 Storage domain 领域模型和 repository 契约强类型化，范围限定为 Java 后端领域层、仓储端口、仓储实现和必要调用方适配。

核心目标：

- `com.thundax.kuzhambu.storage.domain.*.model.entity.*` 中的关键业务字段使用 domain 值对象表达。
- `com.thundax.kuzhambu.storage.domain.*.repository` 中的 ID、状态、owner type、size、part number 等契约字段使用强类型。
- `interfaces`、`facade`、application `Command`/`Query`、infra `DO` 和 admin-web HTTP 协议继续使用基础类型，由 assembler、codec、repository impl 做边界转换。

## Decisions

- `StoredObjectId`、`MultipartUploadSessionId`、`MultipartUploadPartId` 保持 `BaseLongId`/`Long`；不迁移为 `String` 或 ULID。
- `MultipartUploadId` 保持 `BaseStringId`/`String`，它是分片上传业务过程标识。
- `StorageOwnerRef.ownerId` 暂时保持 `String`；owner 来源跨业务域，不新增独立 owner id 值对象。
- `StoredObject.referenceOwnerType` 保留；它用于查询和列表展示派生信息，不是 `storage_object` 持久化真相源。
- repository 查询参数保持平铺；不引入 `StorageObjectListCriteria` 或同类 criteria 对象。
- 本轮优先强类型化 `size` 和 `partNumber`，同批处理 Storage 对象 key、bucket、mime 和 owner params。

## Scope

纳入修改的后端模块：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-domain`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-infra`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface`

纳入前端回归确认的模块：

- `kuzhambu-apps/admin-web`

已修改的治理文档：

- `docs/00-governance/SERVERS-UNIFIED-ID-DESIGN.md`

临时执行文档：

- `docs/30-designs/RUNBOOK-STORAGE-DOMAIN-STRONG-TYPING.md`

## Non-goals

- 不修改 `db/schema/storage.sql` 字段类型和索引。
- 不修改 HTTP request/response JSON 字段名。
- 不修改 admin-web 页面布局、筛选项、表格列和按钮行为，除非后端协议意外变化。
- 不重构上传、读取、orphan cleanup、排序、引用绑定和分片上传流程。

## Data Structure Changes

### Keep Existing ID Types

| Type | File | Field value type | Required action |
| --- | --- | --- | --- |
| `StoredObjectId` | `kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/valueobject/StoredObjectId.java` | `Long` | 保持不变 |
| `MultipartUploadSessionId` | `kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/valueobject/MultipartUploadSessionId.java` | `Long` | 保持不变 |
| `MultipartUploadPartId` | `kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/valueobject/MultipartUploadPartId.java` | `Long` | 保持不变 |
| `MultipartUploadId` | `kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/valueobject/MultipartUploadId.java` | `String` | 保持不变 |
| `StorageOwnerRef.ownerId` | `kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/valueobject/StorageOwnerRef.java` | `String` | 保持不变 |

### Add Value Objects

| New type | File to add | Underlying type | Validation |
| --- | --- | --- | --- |
| `StorageMimeType` | `kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/valueobject/StorageMimeType.java` | `String` | nullable at boundary; non-null value must trim and not be blank |
| `StorageBucketName` | `kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/valueobject/StorageBucketName.java` | `String` | nullable at boundary; non-null value must trim and not be blank |
| `StorageObjectKey` | `kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/valueobject/StorageObjectKey.java` | `String` | non-null persisted value must trim and not be blank |
| `StorageByteSize` | `kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/valueobject/StorageByteSize.java` | `Long` | value must be `>= 0` |
| `MultipartPartSize` | `kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/valueobject/MultipartPartSize.java` | `Long` | value must be `> 0` |
| `MultipartPartNumber` | `kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/valueobject/MultipartPartNumber.java` | `Integer` | value must be `>= 1` |
| `StorageOwnerParams` | `kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/valueobject/StorageOwnerParams.java` | `String` | nullable; non-null value preserves content except outer trim |

Add matching codecs:

| Codec | File to add | Required methods |
| --- | --- | --- |
| `StorageMimeTypeCodec` | `kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/codec/StorageMimeTypeCodec.java` | `toDomain(String)`, `toValue(StorageMimeType)` |
| `StorageBucketNameCodec` | `kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/codec/StorageBucketNameCodec.java` | `toDomain(String)`, `toValue(StorageBucketName)` |
| `StorageObjectKeyCodec` | `kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/codec/StorageObjectKeyCodec.java` | `toDomain(String)`, `toValue(StorageObjectKey)` |
| `StorageByteSizeCodec` | `kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/codec/StorageByteSizeCodec.java` | `toDomain(Long)`, `toValue(StorageByteSize)` |
| `MultipartPartSizeCodec` | `kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/codec/MultipartPartSizeCodec.java` | `toDomain(Long)`, `toValue(MultipartPartSize)` |
| `MultipartPartNumberCodec` | `kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/codec/MultipartPartNumberCodec.java` | `toDomain(Integer)`, `toValue(MultipartPartNumber)` |
| `StorageOwnerParamsCodec` | `kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/codec/StorageOwnerParamsCodec.java` | `toDomain(String)`, `toValue(StorageOwnerParams)` |

### Entity Field Changes

| Entity file | Field | Current type | Target type |
| --- | --- | --- | --- |
| `StoredObject.java` | `mimeType` | `String` | `StorageMimeType` |
| `StoredObject.java` | `bucketName` | `String` | `StorageBucketName` |
| `StoredObject.java` | `objectKey` | `String` | `StorageObjectKey` |
| `StoredObject.java` | `size` | `Long` | `StorageByteSize` |
| `StoredObject.java` | `referenceOwnerType` | `String` | `String`, keep |
| `StoredObjectReference.java` | `ownerParams` | `String` | `StorageOwnerParams` |
| `StoredObjectReference.java` | `referenceOwnerRef.ownerId` | `String` | `String`, keep |
| `MultipartUploadSession.java` | `mimeType` | `String` | `StorageMimeType` |
| `MultipartUploadSession.java` | `bucketName` | `String` | `StorageBucketName` |
| `MultipartUploadSession.java` | `objectKey` | `String` | `StorageObjectKey` |
| `MultipartUploadSession.java` | `totalSize` | `Long` | `StorageByteSize` |
| `MultipartUploadSession.java` | `partSize` | `Long` | `MultipartPartSize` |
| `MultipartUploadPart.java` | `partPath` | `String` | `StorageObjectKey` |
| `MultipartUploadPart.java` | `partNumber` | `Integer` | `MultipartPartNumber` |
| `MultipartUploadPart.java` | `size` | `Long` | `StorageByteSize` |

Compatibility rule:

- Keep string/number getters and setters temporarily when application or interface code still calls them, for example `getMimeType(): String`, `setMimeType(String)`, `getSize(): Long`, `setSize(Long)`, `getPartNumber(): Integer`, `setPartNumber(Integer)`.
- Add explicit typed accessors when needed, for example `getMimeTypeRef()`, `getObjectKeyRef()`, `getSizeRef()`, `getPartNumberRef()`.
- Do not change entity class-level Lombok annotations; they must remain exactly `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`.

### Repository Signature Changes

| File | Method | Current signature | Target signature |
| --- | --- | --- | --- |
| `StoredObjectRepository.java` | `listByIds` | `List<StoredObject> listByIds(List<Long> idList)` | `List<StoredObject> listByIds(List<StoredObjectId> idList)` |
| `StoredObjectRepository.java` | `list` | `list(String mimeType, String objectStatus, String referenceStatus, String referenceOwnerId, String referenceOwnerType, String name, String remarks, SortDirection sortDirection)` | `list(StorageMimeType mimeType, StoredObjectStatus objectStatus, StoredObjectReferenceStatus referenceStatus, String referenceOwnerId, StorageOwnerType referenceOwnerType, String name, String remarks, SortDirection sortDirection)` |
| `StoredObjectRepository.java` | `page` | `page(String mimeType, String objectStatus, String referenceStatus, String referenceOwnerId, String referenceOwnerType, String name, String remarks, SortDirection sortDirection, int pageNo, int pageSize)` | `page(StorageMimeType mimeType, StoredObjectStatus objectStatus, StoredObjectReferenceStatus referenceStatus, String referenceOwnerId, StorageOwnerType referenceOwnerType, String name, String remarks, SortDirection sortDirection, int pageNo, int pageSize)` |
| `StoredObjectRepository.java` | `listMimeTypes` | `List<String> listMimeTypes()` | `List<StorageMimeType> listMimeTypes()` |
| `StoredObjectReferenceRepository.java` | `listReferenceOwnerTypes` | `List<String> listReferenceOwnerTypes()` | `List<StorageOwnerType> listReferenceOwnerTypes()` |
| `StoredObjectReferenceRepository.java` | `deleteByObjectId` | `void deleteByObjectId(String id)` | `void deleteByObjectId(StoredObjectId objectId)` |
| `MultipartUploadRepository.java` | `getMultipartPart` | `MultipartUploadPart getMultipartPart(MultipartUploadId uploadId, Integer partNumber)` | `MultipartUploadPart getMultipartPart(MultipartUploadId uploadId, MultipartPartNumber partNumber)` |

`StoredObjectRepository.list/page` must remain flat. Do not replace the parameter list with a criteria object.

## Task Breakdown

每个任务包控制在 2-5 个主要文件。测试文件不计入主要文件限制，但应随对应任务同步更新。

### Task 1: Governance And Runbook

Files:

- `docs/00-governance/SERVERS-UNIFIED-ID-DESIGN.md`
- `docs/30-designs/RUNBOOK-STORAGE-DOMAIN-STRONG-TYPING.md`

Actions:

- Keep the Storage ID correction: file object and multipart record identities use `BaseLongId`.
- Keep this RUNBOOK as the only execution checklist for this migration.

Verification:

- `rg -n 'Storage 对象标识默认使用|StoredObjectId.*ULID' docs/00-governance docs/30-designs` returns no stale positive instruction.
- `git diff --check`

### Task 2: Add First-Batch Value Objects

Files:

- `StorageMimeType.java`
- `StorageBucketName.java`
- `StorageObjectKey.java`
- `StorageByteSize.java`
- `MultipartPartNumber.java`

Actions:

- Add immutable value objects under `domain/object/model/valueobject`.
- Use single-value wrappers with constructor validation.
- Do not add framework annotations.

Tests:

- Add or update a focused domain test file under `kuzhambu-storage-domain/src/test/java/com/thundax/kuzhambu/storage/domain/`.
- Cover null handling through codecs, blank string rejection, trimming, negative size rejection and part number lower bound.

### Task 3: Add Remaining Value Objects And Codecs

Files:

- `MultipartPartSize.java`
- `StorageOwnerParams.java`
- `StorageMimeTypeCodec.java`
- `StorageBucketNameCodec.java`
- `StorageObjectKeyCodec.java`

Actions:

- Add remaining value objects and boundary conversion helpers.
- Ensure codecs, not value objects, own nullable conversion.

Tests:

- Extend the focused domain codec/value-object test from Task 2.

### Task 4: Add Size, Part And Owner Codecs

Files:

- `StorageByteSizeCodec.java`
- `MultipartPartSizeCodec.java`
- `MultipartPartNumberCodec.java`
- `StorageOwnerParamsCodec.java`

Actions:

- Add `toDomain` and `toValue` for each type.
- Use codecs in later entity, persistence and repository changes.

Verification:

- `mvn -pl biz/storage/kuzhambu-storage-domain test`

### Task 5: Convert Stored Object Entities

Files:

- `StoredObject.java`
- `StoredObjectReference.java`
- `StoragePersistenceAssembler.java`
- `StorageCacheSupport.java`

Actions:

- Change `StoredObject.mimeType`, `bucketName`, `objectKey`, `size` to target value objects.
- Change `StoredObjectReference.ownerParams` to `StorageOwnerParams`.
- Keep `StoredObject.referenceOwnerType` as `String`.
- Keep compatibility getters/setters required by existing callers.
- Convert to/from DO primitive fields only in `StoragePersistenceAssembler`.
- Convert to/from cache primitive fields only in `StorageCacheSupport`.

Tests:

- Update `StoredObjectRepositoryImplTest.java`.
- Update `StoredObjectContentRepositoryImplTest.java` if object key accessors change.
- Update application tests that instantiate `StoredObject` directly.

### Task 6: Convert Multipart Entities

Files:

- `MultipartUploadSession.java`
- `MultipartUploadPart.java`
- `StoragePersistenceAssembler.java`
- `MultipartUploadRepositoryImpl.java`

Actions:

- Change `MultipartUploadSession.mimeType`, `bucketName`, `objectKey`, `totalSize`, `partSize`.
- Change `MultipartUploadPart.partPath`, `partNumber`, `size`.
- Keep `getUploadId(): String` and `setUploadId(String)` compatibility.
- Add typed accessors for typed repository calls, for example `getPartNumberRef()`.
- Use codecs when building MyBatis wrappers.

Tests:

- Update `MultipartUploadApplicationServiceImplTest.java`.
- Update `MultipartUploadApplicationServiceImplAbortTest.java`.

### Task 7: Strong-Type Stored Object Repository Contract

Files:

- `StoredObjectRepository.java`
- `StoredObjectRepositoryImpl.java`
- `StorageApplicationServiceImpl.java`
- `StorageInterfaceAssembler.java`

Actions:

- Apply the signature changes listed in Repository Signature Changes.
- In `StorageApplicationServiceImpl.list/page`, convert `StorageQuery.contentType`, `objectStatus`, `referenceStatus`, `referenceOwnerType` before calling repository.
- Keep `StorageQuery.referenceOwnerId` as `String`.
- Keep admin interface request/response JSON unchanged.

Tests:

- Update `StorageApplicationServiceSortTest.java`.
- Update `StorageApplicationServiceDeleteTest.java`.
- Update `StorageApplicationServiceUploadTest.java`.
- Update `StorageObjectUploadContractTest.java` if response conversion changes.

### Task 8: Strong-Type Reference Repository Contract

Files:

- `StoredObjectReferenceRepository.java`
- `StoredObjectReferenceRepositoryImpl.java`
- `StorageApplicationServiceImpl.java`
- `StorageOwnerBindingFacadeAssembler.java`

Actions:

- Change `deleteByObjectId(String)` to `deleteByObjectId(StoredObjectId)`.
- Change `listReferenceOwnerTypes()` to return `List<StorageOwnerType>`.
- Keep `StorageOwnerRef.ownerId` as `String`.
- Ensure `StorageApplicationServiceImpl.listReferenceOwnerTypes` converts back to `List<String>` if the application service contract stays `List<String>`.

Tests:

- Update `StorageApplicationServiceDeleteTest.java`.
- Update `StorageFacadeImplTest.java`.

### Task 9: Strong-Type Multipart Repository Contract

Files:

- `MultipartUploadRepository.java`
- `MultipartUploadRepositoryImpl.java`
- `MultipartUploadApplicationServiceImpl.java`

Actions:

- Change `getMultipartPart(MultipartUploadId, Integer)` to `getMultipartPart(MultipartUploadId, MultipartPartNumber)`.
- Convert command `partNumber` to `MultipartPartNumber` before repository calls.
- Keep multipart HTTP request field `partNumber` as number.

Tests:

- Update multipart application tests and interface multipart contract tests.

### Task 10: Admin-Web Contract Regression

Files:

- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-types.ts`
- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-service.ts`
- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-page.tsx`
- `kuzhambu-apps/admin-web/e2e/storage/storage-object/storage-object.spec.ts`

Expected frontend data contract:

- `StorageRecord.id`: remains `string`.
- `StorageRecord.contentType`: remains `string | null`.
- `StorageRecord.size`: remains `number | null`.
- `StorageRecord.objectStatus`: remains `string | null`.
- `StorageRecord.referenceStatus`: remains `string | null`.
- `StorageRecord.referenceOwnerType`: remains `string | null`.
- `StoragePageQuery.referenceOwnerId`: remains `string | null`.
- `StoragePageQuery.referenceOwnerType`: remains `string | null`.
- `UploadMultipartPartCommand.partNumber`: remains `number`.
- `InitMultipartUploadCommand.totalSize` and `partSize`: remain `number`.

Controls and operations to verify:

- Search input placeholder `搜索文件名...`: typing text sends `originalFilename`.
- Filter field `文件名` input: applies `originalFilename`.
- Filter field `MIME` input placeholder `image/png`: applies `contentType`.
- Filter field `对象状态` select: options `全部`, `可用`, `删除中`, `已删除`; sends `objectStatus` except `ALL`.
- Filter field `引用状态` select: options `全部`, `已引用`, `未引用`; sends `referenceStatus` except `ALL`.
- Filter field `引用归属类型` input placeholder `reference_owner_type`: sends `referenceOwnerType`.
- Filter field `引用归属ID` input placeholder `123e4567-e89b-12d3-a456-426614174000`: sends `referenceOwnerId`.
- Filter field `备注` input placeholder `业务说明`: sends `remarks`.
- Filter action `查询`: applies current filter values and resets to page 1.
- Filter action `重置`: clears all filter fields and query params.
- Page action `上传`: opens hidden file input `选择上传文件`; small files call `/storage/object/upload`.
- Upload task card action `取消`: aborts multipart upload and calls `/storage/object/multipart/abort` when an upload session exists.
- Row action `预览`: opens authenticated preview content URL.
- Row action `下载`: opens authenticated download content URL.
- Row action `删除`: opens confirm modal and posts `{ ids: [id] }`.
- Batch action `批量删除`: posts selected row ids as strings.
- Table drag sort: posts `orderedIds` as strings and keeps current visual order.
- Pagination: sends `pageNo` and `pageSize`.

Frontend changes:

- If backend JSON stays unchanged, do not edit admin-web source files; only run tests.
- If any response/request field changes, update the four files listed in this task and the E2E expectations in the same task.

## Verification

Backend formatting and checks:

```sh
cd kuzhambu-servers
mvn -pl biz/storage/kuzhambu-storage-domain spotless:apply
mvn -pl biz/storage/kuzhambu-storage-application,biz/storage/kuzhambu-storage-infra,biz/storage/kuzhambu-storage-interface -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/storage -am test
```

Frontend contract checks when admin-web files are changed or backend JSON contract is touched:

```sh
cd kuzhambu-apps
pnpm --filter kuzhambu-admin-web run format
pnpm run format:check
pnpm run lint
pnpm --filter kuzhambu-admin-web run test
pnpm --filter kuzhambu-admin-web run e2e -- e2e/storage/storage-object/storage-object.spec.ts
```

Diff review:

```sh
git diff -- docs/00-governance/SERVERS-UNIFIED-ID-DESIGN.md docs/30-designs/RUNBOOK-STORAGE-DOMAIN-STRONG-TYPING.md kuzhambu-servers/biz/storage kuzhambu-apps/admin-web/src/pages/storage/storage-object kuzhambu-apps/admin-web/e2e/storage/storage-object
```

Expected evidence:

- Storage entity IDs remain `BaseLongId`.
- `StoredObjectRepository` no longer exposes `List<Long>` object IDs.
- `StoredObjectReferenceRepository.deleteByObjectId` no longer accepts raw `String`.
- `StoredObjectRepository.list/page` remain flat and use strong status/owner/mime parameters.
- `StorageOwnerRef.ownerId` remains `String`.
- `StoredObject.referenceOwnerType` remains present for query and display.
- `StoragePersistenceAssembler` and repository impls are the only persistence primitive conversion points.
- Admin-web Storage page request/response JSON remains compatible unless explicitly changed in Task 10.

## Closure

本 RUNBOOK 是临时执行手册。完成强类型化并通过验证后：

- 删除 `docs/30-designs/RUNBOOK-STORAGE-DOMAIN-STRONG-TYPING.md`。
- 如新增稳定值对象命名规则或 repository 平铺强类型参数规则，将长期规则沉淀到 `docs/00-governance/SERVERS-ARCHITECTURE-RULES.md` 或 `docs/00-governance/SERVERS-UNIFIED-ID-DESIGN.md`。
- 如形成可复用验收证据，将结果沉淀到 `docs/40-readiness/`。
