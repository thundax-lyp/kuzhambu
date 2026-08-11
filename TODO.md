# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 当前任务项按数字编号顺序执行；不得跳过前置契约任务直接做后续调用方或重命名任务。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `PromptController and PlatformAiController`：清理提示词 Controller 动词 allowlist
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-01-AI-STORAGE.md`
    - 范围对象：`PromptController.java`、`PlatformAiController.java`、`PlatformAiControllerTest.java`、`AiInterfaceArchitectureTest.java`、`prompt-service.ts`、`prompt-service-contract.test.ts`、`RepositoryApiHardRulesArchitectureTest.java`
    - 处理动作：按 RUNBOOK 改名 9 个提示词与平台动作路径，并同步调用方与 allowlist。
    - 验收点：16 个精确 key 不存在，`mvn -pl biz/ai/kuzhambu-ai-interface -am test` 通过。
    - 重要度：9/10

- [ ] `AiRefinementController`：清理即时 AI 能力 Controller 动词 allowlist
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-01-AI-STORAGE.md`
    - 范围对象：`AiRefinementController.java`
    - 处理动作：按 RUNBOOK 的 9 组固定映射改名即时能力方法和 POST 路径。
    - 验收点：`mvn -pl biz/ai/kuzhambu-ai-interface -Dtest=AiInterfaceArchitectureTest test` 通过。
    - 重要度：8/10

- [ ] `AI refinement architecture allowances`：删除 Refinement Controller 通配 allowance
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-01-AI-STORAGE.md`
    - 范围对象：`AiRefinementTaskController.java`、`AiInterfaceArchitectureTest.java`、`RepositoryApiHardRulesArchitectureTest.java`
    - 处理动作：即时能力整改通过后删除两个 Controller 的通配 allowance 与无用 import。
    - 验收点：AI interface 模块架构测试通过且两个通配 key 不存在。
    - 重要度：8/10

- [ ] `Storage record contracts 7.1–7.7`：转换第一组 Storage Command 为 record
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-01-AI-STORAGE.md`
    - 范围对象：`AbortMultipartUploadCommand.java`、`AddStorageReferencesCommand.java`、`ChangeStorageCommand.java`、`ChangeStorageObjectStatusCommand.java`、`ChangeStorageReferenceStatusCommand.java`、`CompleteMultipartUploadCommand.java`、`CreateStorageCommand.java`、`StorageApplicationCommandQueryRecordAllowances.java`、`StorageApplicationArchitectureTest.java`
    - 处理动作：按 RUNBOOK 7.1–7.7 逐 contract 转为 record、更新 accessor 调用并删除各自 key。
    - 验收点：7 个 contract 均为无 Lombok record，逐项 Maven 架构测试通过。
    - 重要度：9/10

- [ ] `Storage record contracts 7.8–7.14`：转换第二组 Storage Command/Query 为 record
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-01-AI-STORAGE.md`
    - 范围对象：`InitMultipartUploadCommand.java`、`RemoveStorageObjectCommand.java`、`RemoveStorageReferencesCommand.java`、`StorageSortCommand.java`、`UploadMultipartPartCommand.java`、`UploadStorageObjectCommand.java`、`GetReadableStorageContentQuery.java`、`StorageApplicationCommandQueryRecordAllowances.java`、`StorageApplicationArchitectureTest.java`
    - 处理动作：按 RUNBOOK 7.8–7.14 逐 contract 转为 record、更新 accessor 调用并删除各自 key。
    - 验收点：7 个 contract 均为无 Lombok record，逐项 Maven 架构测试通过。
    - 重要度：9/10

- [ ] `Storage record contracts 7.15–7.21`：转换第三组 Storage Query 为 record并删除 allowance 文件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-01-AI-STORAGE.md`
    - 范围对象：`GetStorageObjectQuery.java`、`ListStorageMimeTypesQuery.java`、`ListStorageObjectsQuery.java`、`ListStorageReferenceOwnerTypesQuery.java`、`ListStorageReferencesQuery.java`、`OpenReadableStorageContentQuery.java`、`StorageQuery.java`、`StorageApplicationCommandQueryRecordAllowances.java`、`StorageApplicationArchitectureTest.java`
    - 处理动作：按 RUNBOOK 7.15–7.21 逐 contract 转为 record、更新 accessor 调用，最后删除 allowance 文件与 architecture test 引用。
    - 验收点：21 个 record key 全部不存在，allowance 文件已删除，Maven 架构测试通过。
    - 重要度：9/10

- [ ] `StorageFacadeImpl`：迁移 Facade 内 Command/Query 构造
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-01-AI-STORAGE.md`
    - 范围对象：`StorageFacadeImpl.java`、`StorageReadableContentFacadeAssembler.java`、`StorageOwnerBindingFacadeAssembler.java`、`StorageApplicationArchitectureTest.java`
    - 处理动作：把 3 个 Facade 直接构造移至对应 FacadeAssembler，并删除 3 个 allowance key。
    - 验收点：Facade 不再直接构造 application contract，Maven application 测试通过。
    - 重要度：8/10

- [ ] `StorageObjectController`：迁移 Interface 内 Command/Query 构造
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-01-AI-STORAGE.md`
    - 范围对象：`StorageObjectController.java`、`StorageInterfaceAssembler.java`、`StorageApplicationArchitectureTest.java`
    - 处理动作：把 9 个 Controller 直接构造移至 InterfaceAssembler，并删除对应 allowance key。
    - 验收点：Controller 不再直接构造 application contract，Storage application/interface 测试通过。
    - 重要度：8/10

- [ ] `Storage multipart API`：清理 multipart Controller 动词 allowlist
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-01-AI-STORAGE.md`
    - 范围对象：`StorageObjectController.java`、`StorageInterfaceArchitectureTest.java`、`StorageObjectMultipartUploadContractTest.java`、`storage-object-service.ts`、`storage-object-service.test.ts`、`storage-object.spec.ts`、`RepositoryApiHardRulesArchitectureTest.java`
    - 处理动作：改名 multipart 初始化和分片上传方法/路径，并同步测试、前端调用和 allowlist。
    - 验收点：Storage Controller 通配 allowance 不存在，Maven interface 测试通过。
    - 重要度：8/10

- [ ] `AI and Storage allowlist verification`：执行格式化、静态门禁与全量受影响模块测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-01-AI-STORAGE.md`
    - 范围对象：`kuzhambu-servers/biz/ai/`、`kuzhambu-servers/biz/storage/`、`kuzhambu-servers/common/kuzhambu-common-test/`
    - 处理动作：按 RUNBOOK Final Verification 顺序运行 Spotless、Maven 测试、Checkstyle 与 allowance 搜索。
    - 验收点：所有命令通过，RUNBOOK 范围内 allowance 定义和调用搜索结果为空。
    - 重要度：10/10

- [ ] `ArchUnit allowlist cleanup closure`：清理 RUNBOOK、TODO 与交付现场
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-01-AI-STORAGE.md`
    - 范围对象：`TODO.md`、`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-01-AI-STORAGE.md`、`.github/pull_request_template.md`
    - 处理动作：全量验证通过后删除已完成 TODO、删除 RUNBOOK，并在 PR 模板实例中记录验证证据和风险。
    - 验收点：`TODO.md` 不保留已完成项，RUNBOOK 与其引用均不存在，PR 描述包含验证结果。
    - 重要度：10/10

## 待讨论项
