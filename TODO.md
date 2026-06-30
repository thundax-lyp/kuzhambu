# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `storage-repository`：补齐 orphan 查询与状态推进仓储能力
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-COMPLETE.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/repository/StoredObjectRepository.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/StoredObjectRepositoryImpl.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/test/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/StoredObjectRepositoryImplTest.java`
    - 处理动作：补齐超时 `ACTIVE + UNREFERENCED` orphan 对象查询能力，并保持已删除对象物理清理查询语义稳定
    - 验收点：repository 能区分 orphan 自动删除候选与已删除物理清理候选，相关 infra 测试完成覆盖
    - 重要度：9/10

- [ ] `storage-scheduler`：收口调度器为先标记 orphan 再物理清理
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-COMPLETE.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/StorageOrphanObjectCleanupScheduler.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/test/java/com/thundax/kuzhambu/storage/application/service/impl/StorageOrphanObjectCleanupSchedulerTest.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/repository/StoredObjectRepository.java`
    - 处理动作：将调度器调整为先推进超时 orphan 到 `DELETED`，再对满足条件的已删除对象执行物理清理
    - 验收点：调度器能自动推进 orphan 并完成已删除对象清理，且不会误处理 `REFERENCED` 或未超时对象
    - 重要度：10/10

- [ ] `storage-tests`：补齐删除与清理失败边界测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-COMPLETE.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/test/java/com/thundax/kuzhambu/storage/application/service/impl/StorageOrphanObjectCleanupSchedulerTest.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/test/java/com/thundax/kuzhambu/storage/application/service/impl/StorageApplicationServiceDeleteTest.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/test/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/StoredObjectRepositoryImplTest.java`
    - 处理动作：补齐自动删除与显式删除在失败场景下的测试证据，确保失败可见且不会误删
    - 验收点：自动删除与显式删除失败边界均有测试覆盖，且测试可证明不会错误删除主记录或物理文件
    - 重要度：9/10

- [ ] `storage-docs`：同步 Storage 设计与覆盖率文档到完成口径
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-COMPLETE.md`
    - 范围对象：`docs/30-designs/STORAGE-DESIGN.md`、`docs/40-readiness/STORAGE-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-STORAGE-COMPLETE.md`
    - 处理动作：在代码与测试收口后，同步设计文档、覆盖率文档和 RUNBOOK 到 Storage 彻底完成口径
    - 验收点：文档不再保留自动 orphan 删除相关“部分完成”残留描述，RUNBOOK 达到可删除状态
    - 重要度：8/10

## 待审阅任务项

## 待讨论项
