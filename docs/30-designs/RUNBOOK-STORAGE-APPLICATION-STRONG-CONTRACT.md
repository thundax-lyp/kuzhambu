# RUNBOOK Storage Application 强类型契约迁移

## Purpose

对 `com.thundax.kuzhambu.storage.application.**` 下的 application 契约进行强类型化：

- `*Command` / `*Query` 使用 storage domain 已有强类型值对象或本域 domain entity 承载业务标识、大小、bucket、object key、mime type、multipart upload id、引用关系等字段。
- `*ApplicationService` 可以公开返回本域 domain entity；不为本域 domain entity 机械套一层 `*Result`。
- `*ApplicationService` 接口名按业务用例聚合，方法名允许 `get` / `list` / `page` / `count`，禁止新增或保留模糊 `save`。
- 接口公开参数收敛为无参或单个 `*Command` / `*Query` / `*PageQuery`。
- application service 失败路径使用业务异常表达，不在 `Result` 中承载错误码或错误文案。

## Scope

纳入本次闭环：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/command/`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/query/`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/result/`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/`
- storage application facade 实现和 assembler 调用方。
- storage admin interface controller / assembler 调用方。
- storage application、interface 相关单元测试和契约测试。

主要调用方文件：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/facade/impl/StorageFacadeImpl.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/facade/assembler/StorageReadableContentFacadeAssembler.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/facade/assembler/StorageOwnerBindingFacadeAssembler.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/facade/assembler/StorageUploadFacadeAssembler.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/StorageObjectController.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/assembler/StorageInterfaceAssembler.java`

## Non-goals

- 不修改 `kuzhambu-storage-facade` 对外 facade 协议字段，除非 application 契约迁移导致 assembler 必须适配。
- 不改 storage domain entity、repository、infra mapper / DO 的持久化契约。
- 不改 HTTP Request / Response 的 JSON 字段名和接口路径。
- 不处理其他业务域 application 契约强类型化。
- 不新增 `save*` application 方法；底层 repository / content repository 的 `save` 不属于本次 application service API 禁令范围。
- 不新增本次迁移专用 LINT；但如果现有 common-test ArchUnit testcase 与本次确认的 application service 契约口径不一致，必须同步修改。

## Confirmed Decisions

- `StorageApplicationService` 拆分为业务化接口，不保留一个大而全的 application service。
- 分页查询对象命名为 `StorageObjectPageQuery`，不使用 `PageStorageObjectsQuery`。
- `StorageObjectPageQuery.pageNo` 和 `StorageObjectPageQuery.pageSize` 使用 primitive `int`。
- `count` 查询返回 primitive `long`，不返回 `Long` 或 `*Result`。
- `ownerType + ownerId` 在 application 契约中统一收敛为 `StorageOwnerRef`。
- `AddStorageReferencesCommand.references` 保留 `List<StoredObjectReference>`；它是本域已有 domain entity，且 command 是内部 application 输入契约。
- 上传失败等业务失败用 `BizException` 或其子类表达，不再通过 `StorageUploadResult.error` 传递。
- 本轮不新增任务专用 LINT；ArchUnit testcase 按新架构口径检查并同步修改。

### Domain Entity Boundary Decision

已确认存在可复用 domain entity：

- `StoredObject`
- `StoredObjectReference`
- `MultipartUploadSession`
- `MultipartUploadPart`

建议口径：

- application service 实现内部、repository 交互和 domain 规则计算中，domain entity 是第一选择。
- application service 公开输出中，本域 domain entity 也是第一选择；`Result` 仅用于不存在自然 domain entity 的复合结果、跨资源聚合结果或明确的非领域返回。
- application service 公开输入仍使用 `Command/Query`；Command 内部可以持有本域 domain entity，例如 `AddStorageReferencesCommand.references` 持有 `List<StoredObjectReference>`。

原因：本域 application service 是用例编排边界，不是 HTTP 或跨域 facade 协议边界；在这里机械包装 domain entity 会制造重复模型。需要同步修改 `docs/00-governance/SERVERS-ARCHITECTURE.md` 和 `SERVERS-ARCHITECTURE-RULES.md`，把现有“公开输出默认 `*Result`”调整为“本域 domain entity 优先，`Result` 用于复合/非实体输出”。interface 层和跨域 facade 仍不得直接暴露 domain entity。现有 `LayerArchitectureRuleSupport.assertServiceBoundaryTypesClean` 已允许 service 返回本域 `.domain.*.model.*` 类型，但 storage application 架构测试尚未挂载该规则，仍需同步检查 ArchUnit testcase。

## Current Inventory

### ApplicationService 现状

| 文件 | 当前接口 | 主要问题 | 建议接口 |
| --- | --- | --- | --- |
| `application/service/StorageApplicationService.java` | `StorageApplicationService` | 名称偏模块化，方法混合对象管理、引用绑定、内容读取、上传；公开返回 `StoredObject` / `StoredObjectReference` / `StoredObjectContent` / `StoredObjectId`；`page(StorageQuery, PageQuery)` 是双参数；`remove(StoredObjectId)` 不是 `Command/Query` 参数。 | 拆为 `StorageObjectApplicationService`、`StorageReferenceApplicationService`、`StorageContentApplicationService`、`StorageUploadApplicationService`，或至少重命名为业务对象语义的 `StorageObjectApplicationService` 并把引用/内容/上传用例拆出。 |
| `application/service/MultipartUploadApplicationService.java` | `MultipartUploadApplicationService` | 名称业务化基本合格；公开返回本域 domain entity 可保留。 | 保留接口名或重命名为 `StorageMultipartUploadApplicationService`；返回继续使用 `MultipartUploadSession` / `MultipartUploadPart` / `StoredObject`。 |

### ApplicationService 方法迁移清单

| 当前方法 | 问题 | 目标方法 |
| --- | --- | --- |
| `StoredObject get(StoredObjectId id)` | 参数是裸 domain value object。 | `StoredObject get(GetStorageObjectQuery query)` |
| `List<StoredObject> list(StorageQuery query)` | 参数名过泛。 | `List<StoredObject> list(ListStorageObjectsQuery query)` |
| `PageResult<StoredObject> page(StorageQuery query, PageQuery page)` | 双参数。 | `PageResult<StoredObject> page(StorageObjectPageQuery query)` |
| `StoredObjectId create(CreateStorageCommand command)` | 只返回 ID，调用方常需要对象。 | `StoredObject create(CreateStorageCommand command)`；如只需要 ID 的内部场景，调用方取 `storage.getId()`。 |
| `void change(ChangeStorageCommand command)` | 参数形态合格。 | 保持 `change(ChangeStorageCommand command)`，必要时返回 `void`。 |
| `int remove(StoredObjectId id)` | 参数是 domain value object。 | `int remove(RemoveStorageObjectCommand command)` |
| `List<String> listMimeTypes(StorageQuery query)` | 参数过宽但返回 plain type 可接受。 | `List<String> listMimeTypes(ListStorageMimeTypesQuery query)` |
| `List<String> listReferenceOwnerTypes(StorageQuery query)` | 参数过宽但返回 plain type 可接受。 | `List<String> listReferenceOwnerTypes(ListStorageReferenceOwnerTypesQuery query)` |
| `int changeObjectStatus(ChangeStorageObjectStatusCommand command)` | 参数形态合格。 | 保持。 |
| `int changeReferenceStatus(ChangeStorageReferenceStatusCommand command)` | 参数形态合格。 | 迁入 `StorageReferenceApplicationService` 更清晰。 |
| `int removeReferences(RemoveStorageReferencesCommand command)` | 参数形态合格。 | 迁入 `StorageReferenceApplicationService`。 |
| `void addReferences(AddStorageReferencesCommand command)` | 参数形态合格；`references` 可保留 `List<StoredObjectReference>`。 | 保持方法；字段保留 domain entity。 |
| `StorageUploadResult upload(UploadStorageObjectCommand command)` | 用 `error` 表达失败导致职责不清。 | `StoredObject upload(UploadStorageObjectCommand command)`；失败抛 `BizException`。 |
| `List<StoredObjectReference> listReferences(StorageQuery query)` | 参数过宽。 | `List<StoredObjectReference> listReferences(ListStorageReferencesQuery query)` |
| `boolean existsReadableContent(StorageQuery query)` | 参数过宽。 | `boolean existsReadableContent(GetReadableStorageContentQuery query)` |
| `StoredObjectContent openReadableContent(StoredObjectId id)` | 参数是裸 domain value object。 | `StoredObjectContent openReadableContent(OpenReadableStorageContentQuery query)` |
| `void sort(StorageSortCommand command)` | 参数形态合格。 | 保持。 |
| `MultipartUploadSession init(InitMultipartUploadCommand command)` | 参数形态合格，返回本域 domain entity 合格。 | 保持返回 `MultipartUploadSession`。 |
| `MultipartUploadPart uploadPart(UploadMultipartPartCommand command)` | 参数形态合格，返回本域 domain entity 合格。 | 保持返回 `MultipartUploadPart`。 |
| `StoredObject complete(CompleteMultipartUploadCommand command)` | 参数形态合格，返回本域 domain entity 合格。 | 保持返回 `StoredObject`。 |
| `int abort(AbortMultipartUploadCommand command)` | 参数和返回合格。 | 保持。 |

## Object And Property Inventory

### Existing Strong Types Available

| 语义 | domain value object | codec |
| --- | --- | --- |
| 存储对象 ID | `StoredObjectId` | `StoredObjectIdCodec` |
| multipart upload id | `MultipartUploadId` | `MultipartUploadIdCodec` |
| multipart session ID | `MultipartUploadSessionId` | `MultipartUploadSessionIdCodec` |
| multipart part ID | `MultipartUploadPartId` | `MultipartUploadPartIdCodec` |
| 分片序号 | `MultipartPartNumber` | `MultipartPartNumberCodec` |
| 分片大小 | `MultipartPartSize` | `MultipartPartSizeCodec` |
| bucket 名 | `StorageBucketName` | `StorageBucketNameCodec` |
| object key | `StorageObjectKey` | `StorageObjectKeyCodec` |
| 字节大小 | `StorageByteSize` | `StorageByteSizeCodec` |
| MIME type | `StorageMimeType` | `StorageMimeTypeCodec` |
| 引用 owner type 字符串 | `StorageReferenceOwnerType` | `StorageReferenceOwnerTypeCodec` |
| owner params | `StorageOwnerParams` | `StorageOwnerParamsCodec` |
| owner type + owner id | `StorageOwnerRef` | 暂无 codec，按需新增 `StorageOwnerRefCodec` 或继续在 assembler / service 编排处构造。 |

### Command / Query 字段迁移

| 文件 | 字段 | 当前类型 | 目标类型 |
| --- | --- | --- | --- |
| `CreateStorageCommand.java` | `id` | `StoredObjectId` | 保持。 |
| `CreateStorageCommand.java` | `mimeType` / `bucketName` / `objectKey` / `size` | `String` / `String` / `String` / `Long` | `StorageMimeType` / `StorageBucketName` / `StorageObjectKey` / `StorageByteSize` |
| `CreateStorageCommand.java` | `ownerId` + `ownerType` | `String` + `StorageOwnerType` | `StorageOwnerRef ownerRef`，或保持拆分但禁止裸 `ownerType` 外还散落 `ownerId`。 |
| `ChangeStorageCommand.java` | 同 `CreateStorageCommand` | 多个 plain 字段 | 与 `CreateStorageCommand` 对齐。 |
| `UploadStorageObjectCommand.java` | `size` | `long` | `StorageByteSize size`；文件流长度校验使用 `StorageByteSizeCodec.toValue`。 |
| `UploadStorageObjectCommand.java` | `ownerType` + `ownerId` | `StorageOwnerType` + `String` | `StorageOwnerRef ownerRef`。 |
| `InitMultipartUploadCommand.java` | `uploadId` / `providerUploadId` | `String` | `MultipartUploadId uploadId` / `MultipartUploadId providerUploadId`；如 provider upload id 允许非本域格式，需新增单独 value object。 |
| `InitMultipartUploadCommand.java` | `mimeType` / `bucketName` / `objectKey` | `String` | `StorageMimeType` / `StorageBucketName` / `StorageObjectKey` |
| `InitMultipartUploadCommand.java` | `totalSize` / `partSize` | `Long` / `Long` | `StorageByteSize` / `MultipartPartSize` |
| `InitMultipartUploadCommand.java` | `ownerType` + `ownerId` | `StorageOwnerType` + `String` | `StorageOwnerRef ownerRef`。 |
| `UploadMultipartPartCommand.java` | `uploadId` / `partNumber` / `size` | `String` / `Integer` / `Long` | `MultipartUploadId` / `MultipartPartNumber` / `StorageByteSize` |
| `CompleteMultipartUploadCommand.java` | `uploadId` / `bucketName` / `objectKey` / `size` | `String` / `String` / `String` / `Long` | `MultipartUploadId` / `StorageBucketName` / `StorageObjectKey` / `StorageByteSize` |
| `AbortMultipartUploadCommand.java` | `uploadId` | `String` | `MultipartUploadId` |
| `RemoveStorageReferencesCommand.java` | `ownerType` + `ownerId` | `StorageOwnerType` + `String` | `StorageOwnerRef ownerRef` |
| `AddStorageReferencesCommand.java` | `references` | `List<StoredObjectReference>` | 保持；这是本域已有 domain entity，适合作为内部 command 字段。 |
| `StorageQuery.java` | `contentType` | `String` | `StorageMimeType`，字段名建议同步为 `mimeType` 或保留兼容语义但类型强化。 |
| `StorageQuery.java` | `referenceOwnerType` / `referenceOwnerId` | `String` / `String` | `StorageReferenceOwnerType` + `String` 或 `StorageOwnerRef referenceOwnerRef`；如 reference owner type 与 enum owner type 语义一致，统一到 `StorageOwnerRef`。 |
| `StorageQuery.java` | `ownerType` + `ownerId` | `StorageOwnerType` + `String` | `StorageOwnerRef ownerRef`。 |

### Result 对象新增 / 调整

| 目标文件 | 用途 | 关键字段 |
| --- | --- | --- |
| 删除或停用 `application/result/StorageUploadResult.java` | 上传用例不再用结果对象包装成功/失败二义性。 | 成功返回 `StoredObject`；失败抛 `BizException`。 |
| 不新增 `StorageObjectResult` / `StorageReferenceResult` / `MultipartUpload*Result` | 本域已有自然 domain entity。 | `StoredObject`、`StoredObjectReference`、`MultipartUploadSession`、`MultipartUploadPart` 直接作为 application service 返回。 |

### Query 对象新增 / 调整

| 目标文件 | 用途 | 关键字段 |
| --- | --- | --- |
| `application/query/GetStorageObjectQuery.java` | `get` 单对象读取。 | `StoredObjectId id`。 |
| `application/query/ListStorageObjectsQuery.java` | 对象列表读取。 | 从 `StorageQuery` 拆出列表筛选字段。 |
| `application/query/StorageObjectPageQuery.java` | 分页读取，替代 `StorageQuery + PageQuery` 双参数。 | 列表筛选字段 + primitive `int pageNo` + primitive `int pageSize`；不继承/组合公开 `PageQuery`，公开方法只接收这一个 `*Query`。 |
| `application/query/ListStorageReferencesQuery.java` | 引用列表读取。 | `StoredObjectId id`。 |
| `application/query/GetReadableStorageContentQuery.java` | 可读内容存在性校验。 | `StoredObjectId id`、`StoredObjectReferenceStatus referenceStatus`、`StorageOwnerRef referenceOwnerRef`。 |
| `application/query/OpenReadableStorageContentQuery.java` | 打开可读内容。 | `StoredObjectId id`。 |
| `application/query/ListStorageMimeTypesQuery.java` | MIME 类型列表。 | 当前可为空；预留筛选字段时使用强类型。 |
| `application/query/ListStorageReferenceOwnerTypesQuery.java` | 引用 owner type 列表。 | 当前可为空；预留筛选字段时使用强类型。 |

## File Change Plan

### Step 1: 调整 application assembler

- 按需新增或调整 `application/assembler/StorageApplicationAssembler.java`。
- 承担 `Command` / `Query` 到 domain entity、domain value object 的转换，以及上传、分片等用例内的 domain entity 装配。
- 不处理 HTTP Request / Response，不访问 repository，不做权限和事务判断。

### Step 2: 新增 Query / 删除错误结果包装

- 新增 `GetStorageObjectQuery`、`ListStorageObjectsQuery`、`StorageObjectPageQuery`、`ListStorageReferencesQuery`、`GetReadableStorageContentQuery`、`OpenReadableStorageContentQuery`、`ListStorageMimeTypesQuery`、`ListStorageReferenceOwnerTypesQuery`。
- 新增 `RemoveStorageObjectCommand`，替代 `remove(StoredObjectId id)` 裸参数。
- 删除 `StorageUploadResult` 或停止在公开 service API 中使用；上传成功直接返回 `StoredObject`，失败抛 `BizException`。

### Step 3: 强类型化 Command / Query 字段

- 使用已有 `*Codec` 在 interface assembler 和 facade assembler 边界把 plain HTTP/facade 字段转换为 domain value object。
- application service 内部如需调用 repository，再从强类型字段取 `.value()` 或使用既有 domain entity setter。
- 保持 `Command` / `Query` 纯字段对象，不在对象里新增方法。

### Step 4: 重塑 ApplicationService 接口

建议最小业务化拆分：

- `StorageObjectApplicationService`
  - `StoredObject get(GetStorageObjectQuery query)`
  - `List<StoredObject> list(ListStorageObjectsQuery query)`
  - `PageResult<StoredObject> page(StorageObjectPageQuery query)`
  - `StoredObject create(CreateStorageCommand command)`
  - `void change(ChangeStorageCommand command)`
  - `int remove(RemoveStorageObjectCommand command)`
  - `void sort(StorageSortCommand command)`
  - `List<String> listMimeTypes(ListStorageMimeTypesQuery query)`
  - `List<String> listReferenceOwnerTypes(ListStorageReferenceOwnerTypesQuery query)`
- `StorageReferenceApplicationService`
  - `List<StoredObjectReference> list(ListStorageReferencesQuery query)`
  - `void addReferences(AddStorageReferencesCommand command)`
  - `int removeReferences(RemoveStorageReferencesCommand command)`
  - `int changeReferenceStatus(ChangeStorageReferenceStatusCommand command)`
- `StorageContentApplicationService`
  - `boolean existsReadableContent(GetReadableStorageContentQuery query)`
  - `StoredObjectContent openReadableContent(OpenReadableStorageContentQuery query)`
- `StorageUploadApplicationService`
  - `StoredObject upload(UploadStorageObjectCommand command)`
- `StorageMultipartUploadApplicationService`
  - `MultipartUploadSession init(InitMultipartUploadCommand command)`
  - `MultipartUploadPart uploadPart(UploadMultipartPartCommand command)`
  - `StoredObject complete(CompleteMultipartUploadCommand command)`
  - `int abort(AbortMultipartUploadCommand command)`

如果希望减少文件变更，允许第一轮只把 `StorageApplicationService` 重命名为 `StorageObjectApplicationService`，并将引用、内容、上传拆分作为同 PR 后续 commit；但最终不得保留裸 value object 参数。

### Step 5: 迁移实现类

- `StorageApplicationServiceImpl.java` 按接口拆分为：
  - `StorageObjectApplicationServiceImpl.java`
  - `StorageReferenceApplicationServiceImpl.java`
  - `StorageContentApplicationServiceImpl.java`
  - `StorageUploadApplicationServiceImpl.java`
- `MultipartUploadApplicationServiceImpl.java` 同步接口名和返回结果类型。
- 内部私有方法和公开返回都可以使用本域 domain entity；公开输入必须收敛为 `Command/Query`。
- 实现类之间依赖也使用业务化 application service 接口，例如 multipart complete 调用 `StorageObjectApplicationService.create(CreateStorageCommand)`。

### Step 6: 迁移调用方

- `StorageObjectController.java`
  - 注入新业务化 application service。
  - `page` 构造 `StorageObjectPageQuery`，不再传两个参数。
  - `delete` 先 `get(GetStorageObjectQuery)`，再 `remove(RemoveStorageObjectCommand)`。
  - multipart 响应从 domain entity 组装 HTTP response。
  - `content` 使用 `StoredObjectContent`。
- `StorageInterfaceAssembler.java`
  - `toQuery(StoragePageRequest)` 改为 `toStorageObjectPageQuery(StoragePageRequest)`。
  - `toResponse` 参数继续接收本域 domain entity 或 `StoredObjectContent` 等 application 复合返回对象。
  - 所有 ID、size、mime、bucket、object key 输出通过 codec 转 plain value。
- `StorageFacadeImpl.java`
  - 注入新业务化 application service。
  - `exists` / `open` / `list` / `upload` / `bindOwner` 等全部迁移到新 Query / Command 和本域 domain entity 返回。
- `application/facade/assembler/*.java`
  - facade request 到 command/query 的转换处完成强类型 codec 转换。
  - facade response 从本域 domain entity 或 application 复合返回对象组装。

### Step 7: 清理旧公开类型

- 删除旧 `StorageApplicationService.java` / `MultipartUploadApplicationService.java` 文件，或完成重命名后无旧引用。
- `rg` 确认 `application.service.*ApplicationService` 方法签名不包含以下类型：
  - `StoredObjectId`
  - `PageQuery`
  - `StorageUploadResult`

### Step 8: 同步治理文档和 ArchUnit testcase

- 修改 `docs/00-governance/SERVERS-ARCHITECTURE.md`：
  - `Fast Choice` 中 application 层输出从“默认 `Result` / `DTO` / `PageResult`”调整为“本域 domain entity / 强类型值对象优先；`Result` 用于复合、聚合或非实体输出”。
  - `application` 路径用途中同步说明 application service 可返回本域 domain entity，但 interface / facade 不得直接暴露 domain entity。
- 修改 `docs/00-governance/SERVERS-ARCHITECTURE-RULES.md`：
  - `SERVERS_APP_SERVICE_RETURN_SHAPE` 增加本域 domain entity 和 `PageResult<本域 domain entity>`。
  - `SERVERS_APP_SERVICE_INPUT_SHAPE` 保持公开入参为无参或单个 `*Command` / `*Query` / `*PageQuery`，但说明 Command / Query 字段可持有本域 domain entity。
  - 增加或调整 Review Rule：`count` 方法返回 primitive `long`，不返回 `Long` 或 `*Result`。
- 检查并按需修改 `kuzhambu-servers/common/kuzhambu-common-test/src/main/java/com/thundax/kuzhambu/common/test/architecture/LayerArchitectureRuleSupport.java`：
  - 现有 `assertServiceBoundaryTypesClean` 已允许本域 domain entity 返回，不需要为 `StoredObject` 新增特例。
  - 如要门禁 `count`，新增 `count` 方法返回 primitive `long` 的断言；不要允许 `java.lang.Long`。
  - 如要门禁 service 公开入参单参数形态，补充参数数量断言，避免 `page(query, PageQuery)` 复发。
- 修改 `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/test/java/com/thundax/kuzhambu/storage/application/StorageApplicationArchitectureTest.java`：
  - 挂载 `LayerArchitectureRuleSupport.assertServiceBoundaryTypesClean(classes)`。
  - 若 common-test 新增 `count` / 单参数形态断言，在 storage application 架构测试中同步调用。

## Verification

先运行窄格式，再运行 storage 模块验证：

```sh
cd kuzhambu-servers
mvn -pl biz/storage/kuzhambu-storage-application,biz/storage/kuzhambu-storage-interface -am spotless:apply
mvn -pl biz/storage/kuzhambu-storage-application,biz/storage/kuzhambu-storage-interface -am test
```

补充静态搜索：

```sh
rg -n "save\\w*\\(" biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service -g "*.java"
rg -n "public .*\\((StoredObjectId|.*PageQuery)|StorageUploadResult" biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service -g "*.java"
rg -n "StorageObjectResult|StorageReferenceResult|MultipartUploadSessionResult|MultipartUploadPartResult|CompleteMultipartUploadResult" biz/storage/kuzhambu-storage-application/src/main/java -g "*.java"
rg -n "Long\\s+count\\(|Result\\s+count\\(|count\\([^)]*\\).*Long|count\\([^)]*\\).*Result" biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service -g "*.java"
```

验收标准：

- application service 公开方法参数为无参或单个 `*Command` / `*Query` / `*PageQuery`。
- application service 公开方法返回为 `void`、本域 domain entity、强类型 value object、`List<...>`、`PageResult<...>` 或确有必要的复合 `*Result`，其中 `count` 类读取固定返回 primitive `long`。
- `StorageObjectPageQuery.pageNo` / `pageSize` 使用 primitive `int`。
- storage interface / facade 不再直接消费 storage domain entity。
- 无新增 `save*` application service 方法。
- 上传失败通过异常表达，不通过 `Result.error` 表达。
- storage application 架构测试已检查或明确记录为何暂不检查 service boundary / count 返回 / 单参数形态。
- storage 相关测试通过。

## Closure

本 RUNBOOK 是迁移执行手册，不是长期规则。迁移完成后：

- 若本轮修改 common-test ArchUnit 支持类，需同步跑受影响模块的架构测试，并在 readiness / evidence 文档记录结果。
- 若只完成 storage 局部清理，在 PR readiness / evidence 文档记录验证证据。
- 删除本 RUNBOOK，避免临时执行计划长期留存。
