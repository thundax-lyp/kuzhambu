# RUNBOOK STORAGE COMPLETE

## Purpose

本文档定义 Storage 域“彻底完成”任务的执行边界、改动范围、验收口径和收口条件。

本文档只覆盖当前仍未完成的 Storage 能力，不重复已完成的上传、读取、引用真相源收敛和 admin 页面语义切换。

## Goal

完成 Storage 删除链路的最后闭环，使需求、设计、实现、测试和覆盖率文档在同一口径下成立：

- 管理界面对无引用对象发起显式删除时，对象先进入 `DELETED`。
- 长时间处于 `UNREFERENCED` 的对象会被系统自动识别为 orphan，并进入删除流程。
- 自动删除与显式删除共享同一删除标记语义。
- 进入删除标记态的对象不得继续被正常读取、引用或用于业务绑定。
- 物理文件删除由异步计划任务完成，不要求接口同步完成。

## Source Of Truth

- 需求：[`docs/10-requirements/STORAGE-REQUIREMENTS.md`](../10-requirements/STORAGE-REQUIREMENTS.md)
- 设计：[`docs/30-designs/STORAGE-DESIGN.md`](./STORAGE-DESIGN.md)
- 当前覆盖率：[`docs/40-readiness/STORAGE-IMPLEMENTATION-COVERAGE.md`](../40-readiness/STORAGE-IMPLEMENTATION-COVERAGE.md)

## Current Gap

当前 Storage 已完成以下能力：

- 普通上传与分片上传闭环。
- 文件内容读取闭环。
- 引用关系真相源收敛到 `storage_object_reference`。
- 显式删除时，仅允许删除 `UNREFERENCED` 对象，且删除操作先将对象标记为 `DELETED`。
- 异步清理任务已能物理删除“已标记 `DELETED` 且满足阈值”的对象。

当前仍缺的能力只有一条主线：

- “超时 `UNREFERENCED` 自动推进到 `DELETED`”尚未实现。

因此，Storage 当前并不是缺上传、缺读取、缺引用模型，而是缺“自动 orphan 删除线”和它的测试、文档、运行时口径收口。

## In Scope

- 为 `StoredObjectRepository` 增补 orphan 自动删除所需查询或状态推进能力。
- 调整 `StorageOrphanObjectCleanupScheduler`，使其同时承担：
  - 自动识别超时 orphan。
  - 将 orphan 推进到 `DELETED`。
  - 物理清理已进入删除标记态且满足条件的对象。
- 补齐 application / infra 测试，覆盖自动推进、物理清理和失败边界。
- 同步 Storage 设计文档与实现覆盖率文档，消除“部分完成”残留口径。

## Out Of Scope

- 不新增 portal 通用上传入口。
- 不新增 admin 引用编辑能力。
- 不引入新的对象状态枚举。
- 不补 CDN、预览转换、图片处理、音视频处理、安全扫描等非 Storage 当前范围能力。
- 不扩展 workers、Classics、System 的业务语义。
- 不在本轮引入复杂运维平台、任务台账或外部告警系统接线。

## Stable Rules

- `storage_object_reference` 仍然是对象与业务对象关系的唯一真相源。
- `storage_object.reference_status` 仍然只表示派生汇总状态，不是独立真相源。
- 自动 orphan 删除只允许作用于 `UNREFERENCED` 对象。
- 自动 orphan 删除先推进 `DELETED`，不能直接跳过删除标记态做物理删除。
- 物理清理只允许作用于 `DELETED + UNREFERENCED + 超时` 对象。
- orphan 保留阈值当前固定为 `12 小时`，本轮不调整策略值，也不改成配置项。
- 仍被引用的对象不能因为清理任务被误删。
- 清理任务失败时不得伪造成功状态，也不得继续删除数据库主记录。
- 本轮不引入新的失败重试机制，不接外部告警系统，只要求异常可见、行为可测、不会误删。

## Target Design

建议使用两段式调度语义：

### Stage 1 自动 orphan 标记

扫描满足以下条件的对象：

- `object_status = ACTIVE`
- `reference_status = UNREFERENCED`
- 已超过 orphan 保留阈值

对命中的对象执行：

- 更新 `object_status = DELETED`

该阶段不做物理文件删除。

### Stage 2 物理清理

扫描满足以下条件的对象：

- `object_status = DELETED`
- `reference_status = UNREFERENCED`
- 已超过物理清理阈值

对命中的对象执行：

- 删除底层物理文件内容
- 删除 `storage_object_reference` 残留记录
- 删除 `storage_object` 主记录
- 清理相关缓存

### Stage Relationship

- 本轮确认允许“同一轮调度立即物理清理”。
- 即：对象在 Stage 1 被推进到 `DELETED` 后，如果仍满足 Stage 2 条件，可以在同一轮继续进入物理清理。
- 当前口径下，是否进入物理清理由对象的 `storedAt` 与 orphan 阈值共同决定，不额外引入“进入 `DELETED` 后再等待一段时间”的新规则。

## Decisions Confirmed

### D1 同一轮调度行为

- 已确认允许“同一轮调度立即物理清理”。
- 本轮不拆成“先标记，下一轮再清理”的两轮语义。

### D2 orphan 阈值

- 已确认继续沿用 `12 小时`。
- 本轮不将该阈值改造成配置项。

### D3 失败处理边界

- 已确认本轮不引入失败重试机制。
- 已确认本轮不接外部告警系统。
- 本轮只要求失败时异常可见，且不会误删数据库主记录或物理文件。

## Task Breakdown

### T1 补齐 orphan 查询与状态推进仓储能力

目标：

- 为自动 orphan 删除线补齐最小 repository 能力。
- 保留现有物理清理 repository 能力不变。

相关文件：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/repository/StoredObjectRepository.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/StoredObjectRepositoryImpl.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/test/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/StoredObjectRepositoryImplTest.java`

相关数据结构：

- `StoredObject`
- `StoredObjectStatus`
- `StoredObjectReferenceStatus`
- `StoredObjectDO`
- `storage_object`

处理动作：

- 增加“查询超时 `ACTIVE + UNREFERENCED` 对象”的 repository 能力。
- 复用已有 `updateObjectStatus` 将 orphan 推进到 `DELETED`。
- 保持 `listExpiredDeletedUnreferenced` 与 `physicalDeleteById` 语义不变。

验收点：

- repository 能区分 `ACTIVE + UNREFERENCED` orphan 与 `DELETED + UNREFERENCED` 待物理清理对象。
- infra 测试覆盖新增查询能力和已有物理删除语义。

### T2 收口调度器为“先标记 orphan，再物理清理”

目标：

- 让 `StorageOrphanObjectCleanupScheduler` 成为 Storage 删除闭环的唯一调度入口。

相关文件：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/StorageOrphanObjectCleanupScheduler.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/test/java/com/thundax/kuzhambu/storage/application/service/impl/StorageOrphanObjectCleanupSchedulerTest.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/repository/StoredObjectRepository.java`

相关数据结构：

- `StoredObject`
- `StoredObjectStatus`
- `StoredObjectReferenceStatus`
- `StoredObjectId`

处理动作：

- 调整调度顺序为：
  1. 查询超时 orphan。
  2. 将 orphan 推进为 `DELETED`。
  3. 查询超时 `DELETED` 对象。
  4. 删除物理文件并删除主记录。
- 允许同一轮完成“标记 + 清理”。

验收点：

- 超时 orphan 能自动进入 `DELETED`。
- 已删除且满足条件的对象会被物理删除。
- `REFERENCED` 对象和未超时对象不会被误处理。

### T3 补齐删除与清理失败边界测试

目标：

- 让“不会误删”和“失败可见”形成稳定测试证据。

相关文件：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/test/java/com/thundax/kuzhambu/storage/application/service/impl/StorageOrphanObjectCleanupSchedulerTest.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/test/java/com/thundax/kuzhambu/storage/application/service/impl/StorageApplicationServiceDeleteTest.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/test/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/StoredObjectRepositoryImplTest.java`

相关数据结构：

- `StoredObject`
- `StoredObjectId`
- `BizException`

处理动作：

- 覆盖物理删除失败时的异常暴露。
- 覆盖失败时不会错误删除数据库主记录。
- 保持显式删除仍只允许删除 `UNREFERENCED` 对象。

验收点：

- 自动删除与显式删除的失败边界都有测试覆盖。
- 测试能证明失败不会导致误删。

### T4 同步设计与覆盖率文档到完成口径

目标：

- 让需求、设计、实现覆盖率文档对自动 orphan 删除闭环使用同一口径。

相关文件：

- `docs/30-designs/STORAGE-DESIGN.md`
- `docs/40-readiness/STORAGE-IMPLEMENTATION-COVERAGE.md`
- `docs/30-designs/RUNBOOK-STORAGE-COMPLETE.md`

相关数据结构：

- `storage_object`
- `StoredObjectStatus`
- `StoredObjectReferenceStatus`

处理动作：

- 将删除/清理相关条目从“部分完成”收口到“已完成”。
- 明确记录“12 小时阈值”“同一轮立即物理清理”“本轮不做重试和外部告警”。

验收点：

- 文档不再保留与当前实现冲突的“部分完成”口径。
- RUNBOOK 可在任务完成后删除。

## Execution Order

1. `T1 补齐 orphan 查询与状态推进仓储能力`
2. `T2 收口调度器为“先标记 orphan，再物理清理”`
3. `T3 补齐删除与清理失败边界测试`
4. `T4 同步设计与覆盖率文档到完成口径`

## Acceptance Criteria

以下条件全部满足后，Storage 才视为“彻底完成”：

1. 代码层已存在自动 orphan 删除线。
2. 自动 orphan 删除不会误处理 `REFERENCED` 对象。
3. 删除标记态对象不会继续通过正常业务链路读取或绑定。
4. 物理清理只发生在 `DELETED + UNREFERENCED + 超时` 对象上。
5. application / infra 测试已覆盖自动推进、物理清理和失败边界。
6. `STORAGE-DESIGN.md` 与 `STORAGE-IMPLEMENTATION-COVERAGE.md` 已同步到完成口径。

## Verification

本轮收口最小验证应覆盖：

- Java servers 单测与相关模块验证。
- Storage 调度器测试。
- Storage repository 测试。
- 删除链路相关 application 测试。

PR 收口时遵循仓库统一验证入口与 `PR Verify` workflow。

## Completion Definition

本 RUNBOOK 可以删除的条件是：

- 自动 orphan 删除线已落地。
- 删除与清理相关测试已补齐。
- Storage 覆盖率文档已不再把删除/清理标为“部分完成”。
- 没有剩余需要继续拆分的 Storage 删除/清理执行任务。
