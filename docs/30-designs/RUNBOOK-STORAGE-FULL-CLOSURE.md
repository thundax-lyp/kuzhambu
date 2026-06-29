# RUNBOOK Storage Full Closure

## Purpose

记录本轮 Storage 收口的临时执行口径，避免后续实现阶段再次偏离职责边界。

本 RUNBOOK 只服务当前任务，不替代稳定需求与设计文档；任务完成后应删除或归档其有效内容到正式文档。

## Current Decision

- `portal` 只承担展示和业务读取，不提供 Storage 通用上传入口。
- Storage 分片上传的对外闭环必须补在 `Facade` 和 `admin controller`。
- 不接受仅在 `application` 层保留分片上传状态流转、但缺少对外调用入口的“半闭环”实现。
- Storage 引用建立语义属于业务规则，不再允许仅依赖 `Facade` 调用路径维持幂等；后续必须下沉到 `application` 作为统一规则。
- `Facade` 可以保留预检查与更友好的业务错误提示，但不再作为引用幂等和冲突校验的唯一保障点。
- 本 RUNBOOK 执行完成后，应作为本轮 Storage “可打满当前需求”的判断基线。
- 不在本 RUNBOOK 明确列入的增强项，不作为本轮打满阻塞项。

## Scope

本轮“Storage 完整闭环”里，分片上传相关职责按以下边界收敛：

- `admin controller`
  - 提供 admin 侧公开分片上传路由。
  - 至少覆盖 `initiate / uploadPart / complete / abort` 四段式入口。
  - 负责 admin 协议层参数接收、校验转换和响应返回。
- `Facade`
  - 作为跨业务域和稳定对外编程边界，补齐分片上传调用入口。
  - 对齐现有 `upload / open / bindOwner / unbindOwner` 的 facade 设计方式，避免业务域直接拼装底层 application 命令。
- `application`
  - 继续承载分片上传状态流转、完整性校验和对象创建逻辑。
  - 不单独承担“完整闭环已完成”的口径。

本轮“Storage 完整闭环”里，引用模型相关职责按以下边界收敛：

- `Facade`
  - 继续作为跨业务域稳定入口，对业务方暴露易用的 `bind / unbind / markInUse / markUnused` 语义。
  - 可以保留调用前预检查，用于尽早返回“已绑定其他对象”“已存在其他引用”等业务错误。
  - 但不得把最终一致性建立在“先查再插”的 facade 单路径假设上。
- `application`
  - 作为 Storage 统一业务入口，承担引用建立与清理的最终业务规则。
  - `addReferences` 不再允许“直接插入即结束”的薄封装语义。
  - 后续应由 `application` 统一定义：
    - 同一 `objectId + ownerType + ownerId` 重复建立引用时是否幂等。
    - 同一 `objectId` 被不同业务对象引用时是否允许共存。
    - 引用新增、移除后 `referenceStatus` 如何保持一致。
- `repository / database`
  - 为 `application` 规则提供可落地的持久化能力。
  - 若业务语义要求幂等或唯一性，最终仍需要数据库真实约束或等价持久化机制兜底。
  - 不允许仅依赖调用方约定维持引用一致性。

## Non-Goals

- 不为 `portal` 增加 Storage 通用上传入口。
- 不改变“业务域专用上传入口可复用 Storage”的稳定边界。
- 不把分片上传直接暴露为仅供前端绕过业务语义的 portal 能力。
- 不把“引用幂等补齐”误写成纯技术重构；该项本质上是 Storage 业务规则收敛。
- 不把引用历史、审计视图、引用关系可视化追踪面板纳入本轮打满条件。
- 不把 `StorageReadToken` 独立模型实现纳入本轮打满条件。
- 不把 S3 兼容存储的真实环境联调和运维上线证据纳入本轮打满条件；本轮以代码适配存在且本地运行路径明确为交付基线。

## Full-Coverage Baseline

本节定义“执行完即可打满”的判定范围。只有落在本节范围内的缺失项，才属于本轮必须关闭的阻塞项。

### In Scope For Full Coverage

- admin 侧普通上传、对象查询、对象读取、对象删除闭环。
- admin 侧分片上传四段式对外闭环。
- `Facade` 侧分片上传稳定入口补齐。
- 删除语义与需求/设计文档完全一致：
  - 仅允许删除无引用对象。
  - 删除先标记并清理引用。
  - 删除后不可再读。
  - 物理文件由异步任务最终删除。
- 引用语义下沉到 `application`：
  - 相同 owner 重复建引用幂等。
  - 冲突引用语义明确。
  - `referenceStatus` 与真实引用事实一致。
- `storage_object_reference` 的持久化真相源与 Java 代码口径一致。
- 本地对象存储运行链路可验证。
- 对应 contract test / application test / integration test 补齐到足以支撑 coverage 从“部分完成”升级。

### Explicitly Out Of Scope For Full Coverage

- `portal` 通用上传入口。
- 引用历史面板、审计视图、管理端引用编辑能力。
- `StorageReadToken` 独立模型及其读取 token 机制。
- S3 真实环境联调、上线演练、云资源侧运维证据。

若后续需要这些能力，应单独新增需求或 readiness 项，不回流为本轮 Storage 打满条件。

## Final Confirmations

本节用于固定本轮执行前必须确认、且确认后即作为实现基线的事项。以下事项一旦确认，不再在实现阶段反复摇摆。

### C1 引用模型

确认结论：

- 本轮按“允许多引用、相同 owner 幂等”执行。
- 语义定义如下：
  - 同一 `objectId + ownerType + ownerId` 重复建立引用：幂等。
  - 同一 `objectId` 被不同业务对象建立引用：允许，但必须受统一规则约束。
  - `referenceStatus` 必须反映“是否仍存在至少一条有效引用”这一事实，而不是依赖单一路径手工维护。

采用该结论的原因：

- 更符合 `reference` 语义本身。
- 能覆盖一个文件对象被多个业务对象复用的自然场景。
- 能避免将 Storage 错误收敛成“单绑定对象仓库”，从而与当前 `Facade`、业务复用场景和后续扩展产生冲突。

### C2 删除时的引用处理语义

确认结论：

- 删除文件对象时，先将对象置为删除标记态。
- 删除流程必须移除该对象当前的有效引用事实。
- 删除标记完成后，对象不得继续被正常业务链路读取、绑定或复用。
- 物理文件删除由异步计划任务完成，不在删除接口内同步完成。

这里的“清理 references”定义为：

- 从“当前有效引用集合”中移除该对象对应的引用关系。
- 若实现上采用物理删除引用记录，则删除对应记录。
- 若实现上采用状态失效，则必须保证这些引用不再被系统判定为有效引用，并且 `referenceStatus` 与之保持一致。

本轮不要求：

- 为引用历史保留专门的 Storage 管理端审计视图。
- 为删除链路新增独立的管理端引用修复入口。

### C3 真相源位置

确认结论：

- `storage_object_reference` 的约束真相源必须落到正式文档或正式 schema 中，不能只停留在 RUNBOOK。
- 推荐优先级：
  1. 正式 schema 文件
  2. 数据库治理文档中的明确表结构与约束说明

最低要求：

- 真相源必须明确说明：
  - 主键/唯一键策略
  - 是否允许同一 `object_id` 多条引用
  - 幂等判定维度
  - 有效引用与失效引用的表示方式

### C4 分片上传闭环标准

确认结论：

- 分片上传闭环必须包含真实内容链路，不接受仅有会话和分片元数据流转的“假闭环”。

本轮达标标准：

- admin 侧存在 `initiate / uploadPart / complete / abort` 四段式接口。
- facade 侧存在对应的稳定调用入口。
- 至少存在一条真实的分片内容暂存、合并、落对象存储、生成 `storage_object` 的运行链路。
- 测试覆盖至少包含：
  - interface contract
  - application service
  - 至少一条能证明内容链路成立的集成或等价验证

## Recommended Changes

### Data Structure Changes

以下变更为本轮建议的明确数据结构调整方向。若实际实现采用等价方案，必须在文档与测试中给出同等清晰的约束说明。

#### 1. `storage_object_reference`

建议目标：

- 从“当前 Java 代码中约束不清”的状态，收敛为明确支持多引用且可判定幂等的结构。

建议至少显式确定以下字段约束：

- 引用主键：
  - 若继续保留自然键模式，则应明确复合唯一键
  - 若改为独立 `id` 主键，则仍必须保留引用幂等所需唯一键
- 幂等唯一性维度：
  - `object_id`
  - `reference_owner_type`
  - `reference_owner_id`
- 若保留引用状态字段：
  - 明确 `reference_status` 是否代表记录级状态
  - 明确其与 `storage_object.reference_status` 的关系

推荐约束方向：

- 唯一键：`(object_id, reference_owner_type, reference_owner_id)`

理由：

- 直接表达“同一业务对象对同一文件对象重复建引用幂等”。
- 不阻断多业务对象共享同一文件对象。

#### 2. `storage_object.reference_status`

建议目标：

- 由“可能被不同路径手工维护”的字段，收敛为“和真实有效引用事实一致”的派生业务状态。

要求：

- 新增引用后，若至少有一条有效引用，则对象应为 `REFERENCED`
- 移除最后一条有效引用后，对象应回到 `UNREFERENCED`
- 删除对象时，删除标记与引用状态变更顺序应有明确实现

#### 3. multipart 临时内容结构

若当前分片上传尚无真实内容链路，则本轮应补齐一套明确的暂存结构。可选方式包括：

- 方案 A：按 `uploadId/partNumber` 暂存到本地或对象存储临时路径
- 方案 B：通过底层对象存储 multipart 能力维护 provider 级分片内容

无论采用哪种方案，都必须明确：

- 分片内容存放位置
- `complete` 时如何合并
- `abort` 时如何清理
- orphan multipart 会话如何回收

## File List

以下为本轮执行大概率涉及的核心文件清单。若实际调整超出该范围，应在实现时补充说明。

### 文档

- [STORAGE-REQUIREMENTS.md](/Users/lizixi/workspace/kuzhambu/docs/10-requirements/STORAGE-REQUIREMENTS.md:1)
- [STORAGE-DESIGN.md](/Users/lizixi/workspace/kuzhambu/docs/30-designs/STORAGE-DESIGN.md:1)
- [STORAGE-IMPLEMENTATION-COVERAGE.md](/Users/lizixi/workspace/kuzhambu/docs/40-readiness/STORAGE-IMPLEMENTATION-COVERAGE.md:1)
- [RUNBOOK-STORAGE-FULL-CLOSURE.md](/Users/lizixi/workspace/kuzhambu/docs/30-designs/RUNBOOK-STORAGE-FULL-CLOSURE.md:1)

### Storage Interface

- [StorageObjectController.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/StorageObjectController.java:1)
- `kuzhambu-storage-interface/src/main/java/.../request/*`
- `kuzhambu-storage-interface/src/main/java/.../response/*`
- `kuzhambu-storage-interface/src/test/java/.../admin/*`

### Storage Application / Facade

- [StorageApplicationService.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/StorageApplicationService.java:1)
- [StorageApplicationServiceImpl.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/StorageApplicationServiceImpl.java:1)
- [MultipartUploadApplicationService.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/MultipartUploadApplicationService.java:1)
- [MultipartUploadApplicationServiceImpl.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/MultipartUploadApplicationServiceImpl.java:1)
- [StorageOrphanObjectCleanupScheduler.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/StorageOrphanObjectCleanupScheduler.java:1)
- [StorageFacadeImpl.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/facade/impl/StorageFacadeImpl.java:1)
- `kuzhambu-storage-facade/src/main/java/.../StorageFacade.java`
- `kuzhambu-storage-facade/src/main/java/.../request/*`
- `kuzhambu-storage-facade/src/main/java/.../response/*`

### Storage Infra / Domain

- [StoredObjectReferenceDO.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/persistence/dataobject/StoredObjectReferenceDO.java:1)
- `MultipartUploadSessionDO.java`
- `MultipartUploadPartDO.java`
- [StoredObjectReferenceRepositoryImpl.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/StoredObjectReferenceRepositoryImpl.java:1)
- [StoredObjectRepositoryImpl.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/StoredObjectRepositoryImpl.java:1)
- [MultipartUploadRepositoryImpl.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/MultipartUploadRepositoryImpl.java:1)
- [StoredObjectContentRepositoryImpl.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/StoredObjectContentRepositoryImpl.java:1)
- [StoragePersistenceAssembler.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/persistence/assembler/StoragePersistenceAssembler.java:1)
- [StoredObjectReference.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/entity/StoredObjectReference.java:1)
- [StoredObject.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/entity/StoredObject.java:1)

### Common OSS

- [ObjectStorageClient.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/common/kuzhambu-common-oss/src/main/java/com/thundax/kuzhambu/common/oss/client/ObjectStorageClient.java:1)
- [LocalFileObjectStorageClient.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/common/kuzhambu-common-oss/src/main/java/com/thundax/kuzhambu/common/oss/support/LocalFileObjectStorageClient.java:1)
- [S3ObjectStorageClient.java](/Users/lizixi/workspace/kuzhambu/kuzhambu-servers/common/kuzhambu-common-oss/src/main/java/com/thundax/kuzhambu/common/oss/support/S3ObjectStorageClient.java:1)

## Small Tasks

以下拆分面向执行，不按“大功能”一次性推进，而是控制在每个小任务影响 2-5 个文件，方便实现、验证和回滚。若某项执行时发现超过 5 个核心文件，必须继续拆分，而不是直接扩大任务。

### T1 Admin 分片上传 request 模型

目标：

- 增补 admin 分片上传四段式 request 模型
- 不在本任务内修改 controller 或 response

建议文件范围：

- `request/InitMultipartUploadRequest.java`
- `request/UploadMultipartPartRequest.java`
- `request/CompleteMultipartUploadRequest.java`
- `request/AbortMultipartUploadRequest.java`

### T2 Admin 分片上传 response 模型

目标：

- 增补 admin 分片上传四段式 response 模型
- 不在本任务内修改 controller 行为

建议文件范围：

- `response/InitMultipartUploadResponse.java`
- `response/UploadMultipartPartResponse.java`
- `response/CompleteMultipartUploadResponse.java`
- `response/AbortMultipartUploadResponse.java`

### T3 Admin 分片上传 controller 路由

目标：

- 在 `StorageObjectController` 中补齐四段式分片上传路由
- 与既有 admin controller 风格一致

建议文件范围：

- `StorageObjectController.java`
- `StorageInterfaceAssembler.java`
- `StorageObjectUploadContractTest.java`
- 新增 multipart contract tests

### T4 Admin 分片上传前半段 contract tests

目标：

- 锁定 `initiate / uploadPart` 路由与核心协议字段

建议文件范围：

- `StorageMultipartInitiateContractTest.java`
- `StorageMultipartUploadPartContractTest.java`

### T5 Admin 分片上传后半段 contract tests

目标：

- 锁定 `complete / abort` 路由与核心协议字段

建议文件范围：

- `StorageMultipartCompleteContractTest.java`
- `StorageMultipartAbortContractTest.java`

### T6 Facade 分片上传 request/response 模型

目标：

- 在 facade 模块中补齐分片上传 request/response 模型
- 不在本任务内修改接口定义或实现

建议文件范围：

- facade `request/*Multipart*.java`
- facade `response/*Multipart*.java`

### T7 Facade 分片上传接口定义

目标：

- 在 facade 模块中补齐分片上传接口定义

建议文件范围：

- `StorageFacade.java`
- facade `request/*Multipart*.java`
- facade `response/*Multipart*.java`

### T8 Facade 分片上传实现

目标：

- 在 `StorageFacadeImpl` 中补齐分片上传入口实现
- 对接 application 分片上传服务

建议文件范围：

- `StorageFacadeImpl.java`
- facade assembler 或 application facade assembler
- facade tests

### T9 引用真相源文档收敛

目标：

- 明确 `storage_object_reference` 的主键、唯一键和多引用策略真相源

建议文件范围：

- 正式 schema 真相源文件
- 数据库治理文档

### T10 引用真相源代码对齐

目标：

- 让 Java DO 与引用真相源口径一致

建议文件范围：

- `StoredObjectReferenceDO.java`
- `StoragePersistenceAssembler.java`

### T11 引用幂等规则下沉

目标：

- 让 `application.addReferences(...)` 承担“相同 owner 幂等”的统一语义

建议文件范围：

- `StorageApplicationServiceImpl.java`
- `StoredObjectReferenceRepositoryImpl.java`
- application tests

### T12 引用状态一致性收敛

目标：

- 让 `referenceStatus` 与真实有效引用集合保持一致
- 不在本任务内处理删除语义

建议文件范围：

- `StorageApplicationServiceImpl.java`
- `StoredObjectRepositoryImpl.java`
- `StoragePersistenceAssembler.java`
- application tests

### T13 删除接口拒绝被引用对象

目标：

- 让 admin 删除入口只允许删除无引用对象

建议文件范围：

- `StorageObjectController.java`
- `StorageApplicationServiceImpl.java`
- `StorageObjectDeleteContractTest.java`

### T14 删除时引用释放

目标：

- 删除对象时完成引用释放与对象删除标记的一致性处理

建议文件范围：

- `StorageApplicationServiceImpl.java`
- `StoredObjectReferenceRepositoryImpl.java`
- `StoredObjectRepositoryImpl.java`
- application tests

### T15 异步物理删除任务对齐

目标：

- 让计划任务与删除标记语义保持一致
- 明确对哪些对象执行最终物理删除

建议文件范围：

- `StorageOrphanObjectCleanupScheduler.java`
- `StoredObjectRepositoryImpl.java`
- scheduler tests

### T16 分片内容暂存结构

目标：

- 明确并实现分片内容暂存位置与写入方式

建议文件范围：

- `MultipartUploadApplicationServiceImpl.java`
- `MultipartUploadRepositoryImpl.java`
- `StoredObjectContentRepositoryImpl.java`
- 如需要则扩展一个对应测试文件

### T17 分片 complete 合并落存储

目标：

- 让 `complete` 真正完成内容合并、落对象存储并生成 `storage_object`

建议文件范围：

- `MultipartUploadApplicationServiceImpl.java`
- `StoredObjectContentRepositoryImpl.java`
- `ObjectStorageClient.java`
- 对应 application/integration tests

### T18 分片 abort 清理链路

目标：

- 让 `abort` 真正清理临时分片内容与会话残留

建议文件范围：

- `MultipartUploadApplicationServiceImpl.java`
- `MultipartUploadRepositoryImpl.java`
- `StoredObjectContentRepositoryImpl.java`
- 对应 tests

### T19 文档回写

目标：

- 将已经落实的稳定口径回写正式文档

建议文件范围：

- `STORAGE-REQUIREMENTS.md`
- `STORAGE-DESIGN.md`
- `STORAGE-IMPLEMENTATION-COVERAGE.md`

### T20 清理现场

目标：

- 清理 RUNBOOK、临时说明、漂移项和无用兼容逻辑

建议文件范围：

- `RUNBOOK-STORAGE-FULL-CLOSURE.md`
- 已过期的临时说明文件
- 与本轮决策直接相关的临时测试桩或 TODO

## Documentation And Cleanup

本轮执行结束前，必须完成以下收尾动作：

- 回写正式文档：
  - `STORAGE-REQUIREMENTS.md`
  - `STORAGE-DESIGN.md`
  - `STORAGE-IMPLEMENTATION-COVERAGE.md`
- 若 `StorageReadToken` 最终确认不做：
  - 从正式设计文档删除该项
- 若 `storage_object_reference` 约束已确定：
  - 将真相源落到正式 schema 或数据库治理文档
- 清理现场：
  - 删除或归档本 RUNBOOK 中已经沉淀进正式文档的临时决策
  - 删除临时说明、过期 TODO、临时测试桩和无用兼容逻辑
  - 确保 coverage 文档中不再保留已被正式排除的阻塞项

## Reference Model Adjustment

### Why This Is A Business Adjustment

本次调整不是单纯“代码写法优化”，而是对 Storage 引用语义的显式收敛。

原因：

- Storage 的“引用”决定文件对象是否仍被业务使用、是否可进入清理流程、以及业务对象与文件之间的事实关系。
- 若引用幂等和冲突规则只存在于 `Facade` 的某一条调用路径，那么同一仓库内其他调用者只要直接调用 `application.addReferences(...)`，就可能绕过该规则。
- 一旦出现重复引用、冲突引用或引用状态不一致，受影响的不是某个接口的代码风格，而是：
  - 对象是否仍被视为“已被业务使用”
  - 清理任务是否会误删文件
  - 业务对象解绑/删除时是否能正确释放资源
  - 审计、恢复和排障时对“谁引用了这个文件”的判断是否可信

因此，这一项必须被视为业务规则调整，而不是局部技术实现选择。

### Current Risk

当前风险主要来自“规则只在部分调用路径成立”：

- `Facade.bindOwner(...)` 已经做了部分预检查，因此经由 facade 的常规业务路径里，重复绑定同一 owner 往往不会重复插入。
- 但 `application.addReferences(...)` 当前仍是直接写入 repository 的薄封装，未承担统一幂等保障。
- 这意味着系统现状更接近：
  - “某些调用方式看起来安全”
  - 而不是“Storage 引用规则在所有正式业务入口都一致成立”

### Target Semantics

当前决策先明确到以下级别：

- 相同业务对象对相同文件对象重复建立引用：
  - 必须幂等。
  - 不应因为重复调用而生成重复引用事实或破坏状态。
- 不同业务对象对同一文件对象是否允许同时建立引用：
  - 这是业务语义问题，不是单纯数据库结构问题。
  - 当前 runbook 倾向按“允许多引用”方向继续分析和设计，因为这更符合 `reference` 一词的普遍语义。
  - 但在正式落实现前，仍需结合 Storage 与各业务域的真实约束最终确认。
- `referenceStatus`：
  - 不应由单一路径的偶然更新维持。
  - 应与“系统中是否仍存在有效引用”这一事实保持一致。

### Why The Rule Must Sink To Application

`application` 是 Storage 的统一业务入口，因此引用规则必须在此层成立，原因如下：

- `application` 才是所有上层入口都可以复用的共享边界。
- `Facade` 只是其中一个调用入口，不能代表所有调用方。
- 如果幂等和冲突校验只存在于 facade：
  - 其他业务模块未来只要直接调 `application`
  - 或新增其他 facade / controller / batch 路径
  - 就可能再次引入重复引用和状态不一致问题

因此，后续的正确方向不是“继续加强 facade 预检查”，而是：

- 将引用幂等和冲突规则正式落到 `application`
- 再由 `repository / database` 提供最终一致性保障
- `Facade` 只保留用户友好的预检查和错误信息收敛

### Persistence Follow-Up

本次 runbook 先记录方向，不在此处假定最终表结构结论，但需要明确后续必须核对：

- `storage_object_reference` 是否真实支持一对象多引用。
- 是否存在复合唯一键、主键或等价约束来支撑幂等与冲突规则。
- Java DO、repository 语义和数据库真相源是否一致。

若这三者不一致，应优先收敛真相源，再决定最终实现，而不是让业务规则继续隐含在调用约定中。

本轮打满的最低要求是：

- 至少存在一个清晰、可追溯的 schema 真相源或数据库治理真相源，能说明 `storage_object_reference` 的约束策略。
- DO、repository、application 的实现不得继续与该真相源冲突。

## Implementation Notes

- 若分片上传需要被具体业务域复用，应优先通过 `Facade` 暴露稳定能力，再由业务域决定是否提供自己的专用入口。
- admin 侧 contract test 应覆盖四段式分片上传路由，而不只验证 application service。
- coverage 文档口径应以“admin + facade + application + infra”形成闭环为准。
- 引用建立与清理的幂等、冲突和状态维护应以 `application` 为最终业务规则落点。
- 若 `Facade` 与 `application` 对同一引用行为的判断不一致，应以 `application` 规则为准，并回调校准 facade。
- 引用模型的最终正确性不能只依赖 Java 代码中的“先查再插”；需要后续补齐持久化约束或等价机制。

## Design Drift Notes

### StorageReadToken

当前判断：`StorageReadToken` 先视为设计残留项，而不是已确认的实现缺陷。

背景：

- `STORAGE-DESIGN.md` 当前仍把 `StorageReadToken` 列为 DDD 模型之一。
- 但现有实现中，Storage 读取链路并未落成独立的 read token 模型。
- 当前真实读取方式更接近：
  - admin 侧按对象 ID + 权限直接读取
  - 业务域侧按对象 ID + owner / 分享校验后读取

因此，本轮收口先记录为“设计与实现存在漂移”，不立即要求补代码。

后续处理方向二选一：

- 若后续确认不需要临时读取凭证语义：
  - 从正式设计文档移除 `StorageReadToken`
- 若后续确认需要受控读取凭证能力：
  - 再正式补充 `StorageReadToken` 的领域模型、生成/校验规则、过期机制和接口绑定方式

本轮原则：

- 不把 `StorageReadToken` 当作当前 Storage 闭环的阻塞项。
- 不在没有明确业务需求的前提下补一个孤立 token 模型实现。

## Exit Criteria

- `Storage Implementation Coverage` 中所有当前 `部分完成` 且落入本 RUNBOOK `In Scope For Full Coverage` 的项目均已关闭。
- admin 侧存在可调用的分片上传四段式接口。
- facade 侧存在对应的稳定调用边界。
- application / infra 不再只是“内部已实现”，而是被对外链路真实消费。
- `Storage Implementation Coverage` 中分片上传项可从“部分完成”提升的前提被满足。
- `application.addReferences(...)` 不再只是直接插入，而是承担统一引用语义。
- facade、application、repository/DB 对引用幂等与冲突处理的口径一致。
- 删除接口、引用释放和异步物理删除的行为已有测试或等价验收证据支撑。
- `STORAGE-REQUIREMENTS.md`、`STORAGE-DESIGN.md`、`STORAGE-IMPLEMENTATION-COVERAGE.md` 与实现口径一致。
- 本轮被明确排除的增强项未再出现在 coverage 的“部分完成”或 backlog 阻塞项中。
