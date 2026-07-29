# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `08-content-upload-service-impl`：08 迁移内容读取、普通上传和分片上传实现
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-APPLICATION-STRONG-CONTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/{StorageContentApplicationServiceImpl.java,StorageUploadApplicationServiceImpl.java,MultipartUploadApplicationServiceImpl.java,StorageInputStreamLimiter.java}`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/result/StorageUploadResult.java`
    - 处理动作：拆出内容读取和上传实现，删除或停用 `StorageUploadResult`，失败路径改抛 `BizException`
    - 验收点：普通上传成功返回 `StoredObject`，分片上传继续返回本域 domain entity，上传失败不再通过 `Result.error` 表达
    - 重要度：10/10

- [ ] `09-application-facade-callers`：09 迁移 storage application facade 调用方
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-APPLICATION-STRONG-CONTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/facade/impl/StorageFacadeImpl.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/facade/assembler/{StorageReadableContentFacadeAssembler.java,StorageOwnerBindingFacadeAssembler.java,StorageUploadFacadeAssembler.java}`
    - 处理动作：把 facade request 到 Command/Query 的转换改为强类型，并改用拆分后的业务化 application service
    - 验收点：facade 对外协议不变，application facade 不再依赖旧 `StorageApplicationService`
    - 重要度：9/10

- [ ] `10-admin-interface-callers`：10 迁移 storage admin interface 调用方
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-APPLICATION-STRONG-CONTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/StorageObjectController.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/assembler/StorageInterfaceAssembler.java`
    - 处理动作：把 controller 和 interface assembler 调整为新 Query/Command 和业务化 application service
    - 验收点：HTTP Request/Response 字段和路径不变，interface 层仍只通过 assembler 暴露 response，不直接暴露 domain entity
    - 重要度：9/10

- [ ] `11-application-tests`：11 更新 storage application 测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-APPLICATION-STRONG-CONTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/test/java/com/thundax/kuzhambu/storage/application/service/impl/{StorageApplicationServiceDeleteTest.java,StorageApplicationServiceSortTest.java,StorageApplicationServiceUploadTest.java,MultipartUploadApplicationServiceImplTest.java,MultipartUploadApplicationServiceImplAbortTest.java}`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/test/java/com/thundax/kuzhambu/storage/application/facade/impl/StorageFacadeImplTest.java`
    - 处理动作：按新接口、强类型 Command/Query 和异常失败路径更新 application 层测试
    - 验收点：application 相关测试不再 mock 或调用旧 application service 签名
    - 重要度：9/10

- [ ] `12-interface-tests`：12 更新 storage interface 契约测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-APPLICATION-STRONG-CONTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/test/java/com/thundax/kuzhambu/storage/interfaces/admin/{StorageObjectContentContractTest.java,StorageObjectDeleteContractTest.java,StorageObjectMultipartUploadContractTest.java,StorageObjectUploadContractTest.java}`、`kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/test/java/com/thundax/kuzhambu/storage/interfaces/StorageInterfaceArchitectureTest.java`
    - 处理动作：按新 application service 注入和强类型契约更新 HTTP 契约测试
    - 验收点：storage interface 契约测试仍验证原 HTTP JSON 和文件内容行为
    - 重要度：8/10

- [ ] `13-validation`：13 运行格式、静态搜索和 storage 模块测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-APPLICATION-STRONG-CONTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application`、`kuzhambu-servers/biz/storage/kuzhambu-storage-interface`
    - 处理动作：运行 RUNBOOK 指定的 formatter、测试和静态搜索验证
    - 验收点：storage application/interface 相关验证通过，或明确记录不可运行原因和剩余风险
    - 重要度：10/10

- [ ] `14-runbook-closure`：14 清理 RUNBOOK 并收窄 TODO
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-STORAGE-APPLICATION-STRONG-CONTRACT.md`、`TODO.md`
    - 处理动作：任务完成后删除临时 RUNBOOK，并删除或收窄已完成 TODO 项
    - 验收点：PR 收口时无已完成 TODO 残留，临时 RUNBOOK 已删除或结论已迁移到稳定文档
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
