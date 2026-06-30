# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `storage-http-response`：清理后端对象 response 的 owner 暴露
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-OWNER-CLEANUP.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/response/StorageObjectResponse.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/assembler/StorageInterfaceAssembler.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/test/java/com/thundax/kuzhambu/storage/interfaces/admin/StorageObjectUploadContractTest.java`
    - 处理动作：删除对象 response 中的 owner 字段并同步契约测试
    - 验收点：对象 response 不再把 object owner 作为对外对象事实暴露
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
