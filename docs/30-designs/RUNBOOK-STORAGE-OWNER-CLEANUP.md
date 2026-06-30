# Storage Owner Cleanup RUNBOOK

## 1. Purpose

本文档用于指导 Storage 域清理 `storage_object.owner_type / owner_id` 这组冗余字段，收敛到“`storage_object_reference` 是唯一关系真相源”的目标模型。

本次任务的核心目标是：

- 删除 `storage_object` 上的 `owner_type / owner_id`。
- 删除 `storage_object_reference` 上的 `reference_status`。
- 统一使用 `storage_object_reference` 表达对象与业务对象的关系。
- 将 `storage_object.reference_status` 明确定义为派生状态，而不是真相源。
- 将 admin-web storage object 页面从 object owner 语义切换到 reference 语义。
- 保留业务侧对一对一关系的限制，但不把该限制写死在 Storage 通用层。

本文档是当前任务阶段的执行手册，不替代稳定 requirements / design / coverage 文档。任务关闭后，应删除本 RUNBOOK 并清理残留引用。

## 2. Decision

本次任务先锁定以下设计决策，后续实现不得再摇摆：

- `storage_object_reference` 是唯一关系真相源。
- `storage_object` 不再保留 `owner_type / owner_id`。
- `storage_object_reference` 不再保留 `reference_status`。
- `storage_object.reference_status` 由“是否存在有效 references”派生得到。
- Storage 稳定对外能力允许一个 object 被多个业务对象同时引用。
- 具体业务是否要求一对一，由业务域自己控制。
- `object_status` 继续保留，负责表达对象生命周期，例如 `ACTIVE`、`DELETED`。

## 3. Current Conflict

当前冲突不是单点 bug，而是双真相源冲突：

- `storage_object.owner_type / owner_id` 在表达“对象归属”。
- `storage_object_reference` 在表达“对象被谁引用”。
- `StorageFacadeImpl.bindOwner(...)` 又会同时改 owner 和 reference，导致“归属”和“引用”混用。

当前结果是：

- schema 允许一个 object 被多个不同 owner 引用。
- facade 稳定能力却拒绝 cross-owner 引用。
- object 表与 reference 表都在回答“这个 object 属于谁”，语义冲突。

## 4. Target Model

### 4.1 Object Table

`storage_object` 只保留对象元数据和生命周期状态。

保留字段重点：

- `id`
- `name`
- `extend_name`
- `mime_type`
- `bucket_name`
- `object_key`
- `size`
- `access_endpoint`
- `stored_at`
- `object_status`
- `reference_status`
- `priority`
- `remarks`

删除字段：

- `owner_type`
- `owner_id`

### 4.2 Reference Table

`storage_object_reference` 是唯一关系真相源。

关键字段：

- `object_id`
- `reference_owner_type`
- `reference_owner_id`
- `business_params`

稳定语义：

- 一条记录表示“某个业务对象对某个 storage object 的引用事实”。
- 同一 owner 对同一 object 的幂等由复合主键保证。
- `storage_object_reference` 只保存当前有效引用；解绑时直接删除记录。
- Storage 稳定支持一个 object 被多个业务对象同时引用。
- 具体业务是否限制一对一，由业务层决定，不由 Storage 主表字段表达。

### 4.3 Derived Reference Status

`storage_object.reference_status` 的定义收敛为：

- 存在至少一条有效 reference：`REFERENCED`
- 不存在任何有效 reference：`UNREFERENCED`

也就是说：

- `storage_object_reference` 是真相源。
- `storage_object.reference_status` 是派生汇总字段，可继续保留用于查询优化和批处理索引。

## 5. Data Structure Changes

### 5.0 Before / After Summary

本次变更后的目标结构如下。

`storage_object`

- 变更前：
  - `owner_type`
  - `owner_id`
  - `reference_status`
- 变更后：
  - 删除 `owner_type`
  - 删除 `owner_id`
  - 保留 `reference_status`

`storage_object_reference`

- 变更前：
  - `object_id`
  - `reference_owner_type`
  - `reference_owner_id`
  - `business_params`
  - `reference_status`
- 变更后：
  - `object_id`
  - `reference_owner_type`
  - `reference_owner_id`
  - `business_params`

派生规则

- `storage_object.reference_status = REFERENCED`
  - 条件：存在至少一条有效 `storage_object_reference` 记录
- `storage_object.reference_status = UNREFERENCED`
  - 条件：不存在任何有效 `storage_object_reference` 记录

### 5.1 Schema Changes

本次数据结构调整目标如下。

`db/schema/storage.sql`：

- 从 `storage_object` 删除 `owner_type`
- 从 `storage_object` 删除 `owner_id`
- 删除索引 `idx_storage_object_owner`
- 保留 `idx_storage_object_status`
- 保留 `idx_storage_object_cleanup`

`storage_object_reference`：

- 不新增 owner 列
- 删除 `reference_status`
- 保持复合主键 `(object_id, reference_owner_type, reference_owner_id)`
- 继续作为唯一关系真相源

### 5.2 Java Model Changes

以下结构需要同步收敛：

- `StoredObject`
- `StoredObjectDO`
- `StoredObjectReference`
- `StoredObjectReferenceDO`
- `StoragePersistenceAssembler`
- `StorageCacheSupport`
- 任何直接暴露 `ownerType / ownerId` 的 response / facade response

### 5.3 Query And Index Changes

以下查询逻辑需要同步检查：

- 按 owner 查询 object 的列表查询
- 按 owner 筛选 object 的 admin 接口
- 依赖 `reference_status` 的删除与 cleanup 查询
- 任何依赖 `storage_object_reference.reference_status` 的 reference 查询与解绑逻辑

凡是原来直接查 `storage_object.owner_type / owner_id` 的地方，都要改成基于 `storage_object_reference` 或业务层显式约束来实现。

## 6. Impact Scope

预期影响模块：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-domain`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-infra`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface`
- `kuzhambu-apps/admin-web`
- `docs/10-requirements/STORAGE-REQUIREMENTS.md`
- `docs/30-designs/STORAGE-DESIGN.md`
- `docs/40-readiness/STORAGE-IMPLEMENTATION-COVERAGE.md`

重点文件统一使用相对路径表示：

- `db/schema/storage.sql`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/entity/StoredObject.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/repository/StoredObjectRepository.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/facade/impl/StorageFacadeImpl.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/StorageApplicationServiceImpl.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/persistence/dataobject/StoredObjectDO.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/persistence/dataobject/StoredObjectReferenceDO.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/persistence/assembler/StoragePersistenceAssembler.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/cache/StorageCacheSupport.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/StorageObjectController.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/response/StorageObjectResponse.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/test/java/com/thundax/kuzhambu/storage/application/facade/impl/StorageFacadeImplTest.java`
- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-service.ts`
- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-page.tsx`
- `kuzhambu-apps/admin-web/e2e/storage/storage-object/storage-object.spec.ts`

## 7. Interface Change Rules

本任务很可能触发接口变更；如果发生，必须在对应小任务下显式记录。

### 7.1 HTTP Interface

重点检查：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/StorageObjectController.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/response/StorageObjectResponse.java`

需要记录的接口变更包括：

- 是否移除 response 中的 `ownerType / ownerId`
- 是否移除按 object owner 查询 object 的筛选参数
- 是否新增按 reference owner 查询的筛选语义
- 本轮不新增 storage 专项接口契约文档，由各业务域自行维护接口变更说明

### 7.1A Frontend Contract

重点检查：

- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-types.ts`
- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-service.ts`
- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-page.tsx`

需要记录的接口变更包括：

- 前端类型是否删除 `ownerType / ownerId`
- 页面列名、筛选项和展示文案是否从 owner 改成 reference
- e2e 与页面单测是否同步更新为新字段语义

### 7.2 Application And Facade Interface

重点检查：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/facade/StorageFacade.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/StorageApplicationService.java`

需要记录的接口变更包括：

- `bindOwner / unbindOwner / markInUse / markUnused` 的语义是否调整
- facade request / response 是否还暴露 object owner
- upload / multipart request 中的 `ownerType / ownerId` 是否直接删除

### 7.3 Repository Interface

重点检查：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/repository/StoredObjectRepository.java`

需要记录的接口变更包括：

- 是否删除按 object owner 查询的方法
- 是否新增基于 reference owner 的查询方法
- 是否删除 reference entity / repository 中对 `reference_status` 的读写依赖
- 哪些实现类和测试需要同步修改

## 8. Small Tasks

以下任务必须控制在每个任务改动 2-5 个文件，避免一次性大面积改造。

### Task A. Freeze The New Model In Docs

目标：

- 将“删除 object owner、reference 成为唯一真相源”的决策写入稳定文档。

相关文件：

- `docs/10-requirements/STORAGE-REQUIREMENTS.md`
- `docs/30-designs/STORAGE-DESIGN.md`
- `docs/40-readiness/STORAGE-IMPLEMENTATION-COVERAGE.md`

完成标准：

- 三份文档不再把 `storage_object.owner_type / owner_id` 作为目标模型的一部分。
- 三份文档不再把 `storage_object_reference.reference_status` 作为目标模型的一部分。
- 三份文档明确 `reference_status` 为派生状态。

### Task B1. Remove Owner From Domain And DO

目标：

- 从 object 领域模型、DO、assembler 中移除 owner 字段。

相关文件：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/entity/StoredObject.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/persistence/dataobject/StoredObjectDO.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/entity/StoredObjectReference.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/persistence/dataobject/StoredObjectReferenceDO.java`

完成标准：

- `StoredObject` 不再暴露 `ownerType / ownerId`。
- `StoredObjectReference` 不再暴露 `referenceStatus`。
- 若接口签名变更，必须补充记录影响文件。

### Task B2. Remove Owner From Persistence And Cache Mapping

目标：

- 从 persistence assembler 和 cache 映射中删除 object owner 读写。
- 从 reference assembler 映射中删除 `referenceStatus` 读写。

相关文件：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/persistence/assembler/StoragePersistenceAssembler.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/cache/StorageCacheSupport.java`

完成标准：

- persistence 层不再写入或读取 `storage_object.owner_type / owner_id`。
- persistence 层不再写入或读取 `storage_object_reference.reference_status`。

### Task C1. Remove Owner Mutation Logic From Facade

目标：

- 删除 `bindOwner -> changeOwner` 这条链路。
- 统一改成只维护 `storage_object_reference`，并在引用变化后汇总 `storage_object.reference_status`。

相关文件：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/facade/impl/StorageFacadeImpl.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/test/java/com/thundax/kuzhambu/storage/application/facade/impl/StorageFacadeImplTest.java`

完成标准：

- binding 逻辑不再更新 `storage_object` owner 字段。
- 旧的 owner mutation 测试被删除或改写。

### Task C2. Rework Reference Maintenance In Application Service

目标：

- application service 只维护有效引用记录和对象级 `reference_status` 汇总。

相关文件：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/StorageApplicationService.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/StorageApplicationServiceImpl.java`

完成标准：

- unbind 逻辑直接删除 reference 记录，而不是更新 reference 记录状态。
- `reference_status` 汇总规则由 application service 统一维护。
- Storage 稳定支持 cross-owner reference；一对一限制只存在于业务域。

### Task D1. Clean Up Admin Object Response

目标：

- 清理 admin / facade 对 object owner 的对外暴露。

相关文件：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/response/StorageObjectResponse.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/assembler/StorageInterfaceAssembler.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/test/java/com/thundax/kuzhambu/storage/interfaces/admin/StorageObjectUploadContractTest.java`

完成标准：

- 对外 response 不再把 object owner 当成对象事实返回。
- 接口变更已在 RUNBOOK 中标明。

### Task D2. Clean Up Upload And Multipart Response Exposure

目标：

- 清理 upload / multipart 响应中的 object owner 暴露。
- 删除 upload / multipart request 中的 owner 输入参数。

相关文件：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/test/java/com/thundax/kuzhambu/storage/interfaces/admin/StorageObjectMultipartUploadContractTest.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/facade/assembler/StorageUploadFacadeAssembler.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/request/InitMultipartUploadRequest.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/response/InitMultipartUploadResponse.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/response/CompleteMultipartUploadResponse.java`

完成标准：

- upload / multipart 响应不再把 object owner 当成对象事实返回。
- upload / multipart 请求不再接收 `ownerType / ownerId`。

### Task D3. Rework Admin Query Semantics

目标：

- 将按 owner 查询 object 的接口语义改成 reference 语义，或显式移除。

相关文件：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/StorageObjectController.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/assembler/StorageInterfaceAssembler.java`

完成标准：

- 若仍需要筛选，必须明确是按 reference owner 筛选，而不是按 object owner 筛选。
- 接口变更已在 RUNBOOK 中标明。

### Task D4. Rework Admin Web Storage Object Page

目标：

- 将 admin-web storage object 页面从 object owner 展示切换到 reference 展示。

相关文件：

- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-types.ts`
- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-service.ts`
- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-page.tsx`
- `kuzhambu-apps/admin-web/src/app.test.tsx`
- `kuzhambu-apps/admin-web/e2e/storage/storage-object/storage-object.spec.ts`

完成标准：

- 页面不再展示 object owner 字段。
- 页面筛选改成 reference 语义，或在不保留筛选时显式删除。
- 前端类型、服务层、页面测试和 e2e 与后端接口变更一致。

### Task E1. Migrate Schema And Database Rules

目标：

- 完成 schema 迁移设计，并验证 `storage_object_reference` 只保留有效引用记录。

相关文件：

- `db/schema/storage.sql`
- `docs/00-governance/SERVERS-DATABASE-RULES.md`

完成标准：

- schema 已明确移除 object owner 列、reference 表上的 `reference_status` 以及相关索引/注释口径。

### Task E2. Rework Repository Query Semantics

目标：

- repository 查询不再依赖 object owner 或 reference 记录状态字段。

相关文件：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/repository/StoredObjectRepository.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/StoredObjectRepositoryImpl.java`

完成标准：

- 删除、cleanup、读取逻辑只依赖 `reference_status` 的派生结果。
- repository 不再读写 `storage_object_reference.reference_status`。

### Task E3. Verify Derived Status And Delete Path

目标：

- 测试证明“有 reference 则 referenced，无 reference 则 unreferenced”，以及删除路径仍然成立。

相关文件：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/test/java/com/thundax/kuzhambu/storage/application/service/impl/StorageOrphanObjectCleanupSchedulerTest.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/test/java/com/thundax/kuzhambu/storage/interfaces/admin/StorageObjectDeleteContractTest.java`

完成标准：

- 测试能够证明“有 reference 则 referenced，无 reference 则 unreferenced”。
- 删除链路和 cleanup 链路与新的派生状态模型一致。

### Task F. Cleanup Temporary Artifacts

目标：

- 在代码和接口全部收口后，清理本轮执行现场。

相关文件：

- `TODO.md`
- `docs/30-designs/RUNBOOK-STORAGE-OWNER-CLEANUP.md`

完成标准：

- 已完成的 TODO 项被删除或收窄为剩余内容。
- 本 RUNBOOK 在任务关闭时删除，不保留失效执行手册。

## 9. Constraints

### 9.1 Truth Source Constraint

- `storage_object_reference` 是唯一关系真相源。
- `storage_object_reference` 只保存当前有效引用记录。
- `storage_object.reference_status` 不能单独决定事实，只能反映汇总结果。

### 9.2 Storage Boundary Constraint

- Storage 层不保存“创建归属”。
- Storage 层不保存“主 owner”。
- Storage 通用模型允许多引用；是否限制一对一属于业务域规则。

### 9.3 Delete And Cleanup Constraint

- `object_status` 继续保留，用于表示对象生命周期。
- 删除判断最终以是否存在有效 references 为准。
- orphan cleanup 与显式删除都不得回退到 object owner 语义。

## 10. Verification Commands

按仓库规则，Java 相关改动至少执行：

```sh
cd kuzhambu-servers
mvn -pl biz/storage/kuzhambu-storage-application spotless:apply
mvn -pl biz/storage/kuzhambu-storage-application -am spotless:check
mvn -pl biz/storage/kuzhambu-storage-application -am checkstyle:check
mvn -pl biz/storage/kuzhambu-storage-application -am test
```

若改动扩展到 `infra`、`interface` 或 schema，对应扩大最小必要模块范围。

执行后必须检查：

- `git diff` 仅保留任务相关变更。
- 测试覆盖 object owner 删除后的主要路径。
- 接口变更已标明且调用方影响可追踪。

## 11. Risks

- 现有上传 / multipart 流程仍然使用 owner 参数，直接移除会牵连初始引用建立路径与调用方。
- admin 查询若仍依赖 object owner 过滤，需要补 reference 维度的新查询方案。
- 业务域如果默认把 Storage owner 当成一对一事实，改造后可能需要同步收口调用逻辑。
- `reference_status` 若维护不完整，会造成“reference 真相源”和“object 汇总状态”短暂不一致。
- 删除 `storage_object_reference.reference_status` 后，旧代码若仍尝试更新该字段，会出现编译或运行时不一致。

## 12. Done Criteria

只有同时满足以下条件，才算本 RUNBOOK 对应任务完成：

- `storage_object.owner_type / owner_id` 已从目标模型中移除。
- `storage_object_reference.reference_status` 已从目标模型中移除。
- 稳定文档已经明确 `reference` 为唯一关系真相源。
- `reference_status` 已降级为派生汇总字段。
- Storage 稳定对外能力已允许多业务对象并发引用同一 object。
- facade / service / interface 不再依赖 object owner 语义。
- schema 变更、接口变更、测试变更都已在 RUNBOOK 中标明。

## 13. Cleanup

任务关闭时必须执行：

- 删除本文档 `docs/30-designs/RUNBOOK-STORAGE-OWNER-CLEANUP.md`
- 清理其它文档中对本 RUNBOOK 的临时引用
- 将最终稳定结论只保留在 requirements / design / implementation coverage 中
