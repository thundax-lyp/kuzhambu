# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `StorageFacadeImpl multipart`：实现 facade 分片上传调用链
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/facade/impl/StorageFacadeImpl.java`、相关 facade assembler 与 facade tests
    - 处理动作：在 `StorageFacadeImpl` 中实现分片上传四段式 facade 调用链
    - 验收点：facade 可转发分片上传四段式调用且 tests 能验证 facade 到 application 的调用关系
    - 重要度：9/10

- [ ] `storage_object_reference 真相源文档`：明确引用约束真相源
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-FULL-CLOSURE.md`
    - 范围对象：正式 schema 真相源、数据库治理文档
    - 处理动作：明确 `storage_object_reference` 的主键或唯一键策略与多引用能力
    - 验收点：正式真相源能清晰说明引用约束
    - 重要度：9/10

- [ ] `storage_object_reference DO`：对齐引用约束代码口径
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/persistence/dataobject/StoredObjectReferenceDO.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/persistence/assembler/StoragePersistenceAssembler.java`
    - 处理动作：让 Java DO 与引用约束真相源保持一致
    - 验收点：DO 和 assembler 与正式真相源不存在冲突
    - 重要度：8/10

- [ ] `addReferences 语义`：为 application 建立相同 owner 幂等规则
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/StorageApplicationServiceImpl.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/StoredObjectReferenceRepositoryImpl.java`
    - 处理动作：让 `application.addReferences(...)` 对相同 owner 的重复引用建立保持幂等
    - 验收点：重复建立同一 owner 引用不会产生重复有效引用且有测试锁定
    - 重要度：10/10

- [ ] `referenceStatus 一致性`：收敛对象引用状态维护
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/StorageApplicationServiceImpl.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/StoredObjectRepositoryImpl.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/persistence/assembler/StoragePersistenceAssembler.java`
    - 处理动作：使 `referenceStatus` 与真实有效引用集合保持一致
    - 验收点：新增、移除最后一条有效引用后对象引用状态变化正确且有测试锁定
    - 重要度：10/10

- [ ] `delete 引用校验`：让 admin 删除入口拒绝被引用对象
    - 任务类型：执行任务
    - 依据文档：`docs/10-requirements/STORAGE-REQUIREMENTS.md`、`docs/30-designs/STORAGE-DESIGN.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/StorageObjectController.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/StorageApplicationServiceImpl.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/test/java/com/thundax/kuzhambu/storage/interfaces/admin/StorageObjectDeleteContractTest.java`
    - 处理动作：实现“仅允许删除无引用对象”的删除校验
    - 验收点：被引用对象无法通过 admin 通用删除接口删除且 contract test 覆盖该行为
    - 重要度：10/10

- [ ] `delete 引用释放`：收敛删除标记与引用释放逻辑
    - 任务类型：执行任务
    - 依据文档：`docs/10-requirements/STORAGE-REQUIREMENTS.md`、`docs/30-designs/STORAGE-DESIGN.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/StorageApplicationServiceImpl.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/StoredObjectReferenceRepositoryImpl.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/StoredObjectRepositoryImpl.java`
    - 处理动作：让删除对象时的删除标记与引用释放行为保持一致
    - 验收点：删除后对象不可读、有效引用被释放且行为有测试验证
    - 重要度：10/10

- [ ] `orphan cleanup 语义`：让异步物理删除任务与删除语义一致
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/STORAGE-DESIGN.md`、`docs/30-designs/RUNBOOK-STORAGE-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/StorageOrphanObjectCleanupScheduler.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/StoredObjectRepositoryImpl.java`、scheduler tests
    - 处理动作：收敛计划任务对最终物理删除对象的判定与执行语义
    - 验收点：异步物理删除任务与删除标记语义一致且有 scheduler 测试锁定
    - 重要度：9/10

- [ ] `multipart 暂存结构`：补齐分片内容暂存结构
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/MultipartUploadApplicationServiceImpl.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/MultipartUploadRepositoryImpl.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/StoredObjectContentRepositoryImpl.java`
    - 处理动作：明确并实现分片内容暂存位置与写入方式
    - 验收点：分片上传具备真实内容暂存结构且不再只保存元数据
    - 重要度：10/10

- [ ] `multipart complete`：实现分片合并落存储
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/MultipartUploadApplicationServiceImpl.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/StoredObjectContentRepositoryImpl.java`、`kuzhambu-servers/common/kuzhambu-common-oss/src/main/java/com/thundax/kuzhambu/common/oss/client/ObjectStorageClient.java`
    - 处理动作：让 `complete` 真正完成分片内容合并、落对象存储并生成 `storage_object`
    - 验收点：`complete` 后存在真实内容对象且有 application 或 integration 级验证
    - 重要度：10/10

- [ ] `multipart abort`：实现分片取消清理链路
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/MultipartUploadApplicationServiceImpl.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/MultipartUploadRepositoryImpl.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/StoredObjectContentRepositoryImpl.java`
    - 处理动作：让 `abort` 真正清理临时分片内容与会话残留
    - 验收点：取消分片上传后不会留下可用对象或临时残留且有测试验证
    - 重要度：9/10

- [ ] `Storage 正式文档`：回写 requirements、design 和 coverage
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-FULL-CLOSURE.md`、`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/10-requirements/STORAGE-REQUIREMENTS.md`、`docs/30-designs/STORAGE-DESIGN.md`、`docs/40-readiness/STORAGE-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：将已落实的稳定口径回写到正式需求、设计和 coverage 文档
    - 验收点：requirements、design、coverage 与实现口径一致
    - 重要度：8/10

- [ ] `Storage 现场清理`：清理 RUNBOOK 与临时说明
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-FULL-CLOSURE.md`、`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-STORAGE-FULL-CLOSURE.md`、与本轮直接相关的临时说明或测试桩
    - 处理动作：在正式文档沉淀完成后清理 RUNBOOK 中已过期的临时决策和临时现场
    - 验收点：RUNBOOK 只保留仍未沉淀的临时决策，过期说明和临时测试桩被清理
    - 重要度：7/10

## 待审阅任务项

## 待讨论项
