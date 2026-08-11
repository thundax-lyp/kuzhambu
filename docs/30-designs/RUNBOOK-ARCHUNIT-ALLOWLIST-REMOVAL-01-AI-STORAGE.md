# ArchUnit allowlist 清理 01：AI 与 Storage

## Purpose

消除 AI 与 Storage 域现有的 ArchUnit legacy allowlist。每个 allowlist key 必须由对应生产代码整改取代；不得修改 `kuzhambu-common-test` 的规则实现、不得新增或扩大 allowlist。同步删除或更新 `RepositoryApiHardRulesArchitectureTest` 中与已整改代码完全相同的 legacy key，不视为修改共享规则。

本 RUNBOOK 只定义执行顺序和文件级改动。Controller HTTP 路径或方法名发生变化时，必须在同一任务中更新仓库内所有调用方；不得保留旧路径兼容入口。

## Scope

| 编号 | 整改类别 | 现有 allowlist 所在文件 | 完成判定 |
| --- | --- | --- | --- |
| A | AI Repository 方法命名 | `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/test/java/com/thundax/kuzhambu/ai/domain/AiDomainArchitectureTest.java` | 11 个 repository key 已删除，规则通过。 |
| B | AI Controller 动词与路径 | `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/test/java/com/thundax/kuzhambu/ai/interfaces/AiInterfaceArchitectureTest.java` | 26 个精确 key 与 2 个通配 key 已删除，规则通过。 |
| C | Storage Command/Query record | `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/test/java/com/thundax/kuzhambu/storage/application/StorageApplicationCommandQueryRecordAllowances.java` | 21 个 contract 均为无 Lombok 的 Java `record`，所有 key 已删除。 |
| D | Storage Command/Query 构造位置 | `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/test/java/com/thundax/kuzhambu/storage/application/StorageApplicationArchitectureTest.java` | 12 个构造位置 key 已删除；构造仅在 assembler 或 application service。 |
| E | Storage Controller 动词与路径 | `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/test/java/com/thundax/kuzhambu/storage/interfaces/StorageInterfaceArchitectureTest.java` | `StorageObjectController` 通配 key 已删除，规则通过。 |

## Non-goals

- 不修改 `kuzhambu-servers/common/kuzhambu-common-test/` 中的生产规则或规则断言算法；只同步清理 `RepositoryApiHardRulesArchitectureTest.java` 的已失效 allowlist 项。
- 不修改 AI、Storage 以外业务域的生产代码或 allowlist。
- 不新增旧 HTTP 路径的兼容映射。
- 不改变请求字段、响应字段、权限值、事务语义或 Storage 文件读写行为；本批只改变命名、构造位置和 contract 实现形态。

## Mandatory Rules

- 所有 `*Command`、`*Query` 必须是只声明字段的 Java `record`；移除 Lombok 注解及 import。
- 生产代码中的 `new *Command`、`new *Query` 只能位于 `*InterfaceAssembler`、`*FacadeAssembler`，或 `*ApplicationService` / `*ApplicationServiceImpl` 的下游编排代码。
- Controller 方法名必须以共享动词白名单中的动词开头；`@PostMapping` 的最后一个路径段必须是白名单动词。白名单以 `ApiAnnotationArchitectureRuleSupport` 为准。
- 每完成一个 allowlist 类别，立即删除该类别已不再命中的 key；不得留下 stale allowance。

## Execution Plan

按以下顺序执行。每个小任务修改 2--12 个文件；一个任务完成并通过其指定架构测试后，再开始下一任务。

### 1. AI Repository 命名：配置仓储（每组 2--12 个文件）

修改文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/config/repository/AiBusinessConfigRepository.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/config/repository/AiModelRepository.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/config/repository/PromptRepository.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/test/java/com/thundax/kuzhambu/ai/domain/AiDomainArchitectureTest.java`
- `kuzhambu-servers/common/kuzhambu-common-test/src/test/java/com/thundax/kuzhambu/common/test/architecture/RepositoryApiHardRulesArchitectureTest.java`

执行：

1. 固定改名：`AiBusinessConfigRepository.get(AiBusinessConfigId)` -> `getById`；`get(AiBusinessCapability)` -> `getByCapability`；`AiModelRepository.get(AiModelId)` -> `getById`；`PromptRepository.get(PromptTemplateId)` -> `getTemplateById`；`get(AiBusinessCapability)` -> `getTemplateByCapability`；`getCurrentVersion` -> `getCurrentVersionByTemplateId`；`getVersion` -> `getVersionById`；`markCurrentVersion` -> `updateCurrentVersion`；`replaceVariables` -> `replaceTemplateVariables`。
2. 每个 repository 的实现、生产调用方和测试调用方使用 `rg -l '精确旧方法名'` 列出。按输出路径字典序，每批最多取 10 个调用文件；每批再加入该 repository interface 与其 `RepositoryImpl`，总数不得超过 12。每批完成后运行该模块的相关测试；最后一批删除该 repository 的 domain 与 common-test allowance key。
3. 完成三个配置 repository 后，运行 `mvn -pl biz/ai/kuzhambu-ai-domain -am test`。

### 2. AI Repository 命名：调用仓储（每组 2--12 个文件）

修改文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/repository/AiBatchJobRepository.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/repository/AiInvocationRepository.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/test/java/com/thundax/kuzhambu/ai/domain/AiDomainArchitectureTest.java`
- `kuzhambu-servers/common/kuzhambu-common-test/src/test/java/com/thundax/kuzhambu/common/test/architecture/RepositoryApiHardRulesArchitectureTest.java`

执行：

1. 固定改名：`AiBatchJobRepository.get` -> `getById`；`AiInvocationRepository.getCandidate` -> `getCandidateById`；`getInvocationLog` -> `getInvocationLogById`；`pageInvocationLogs` -> `pageInvocationLogsByFilter`。将 `matchesContentRef` 从 Repository interface 移至其唯一的 application/domain helper；该 interface 不保留该方法。
2. 对每个改名按步骤 1 的确定性分批规则更新 `RepositoryImpl`、生产调用方和测试调用方。
3. 删除余下 4 个 domain allowance 以及 common-test 中相同 key，运行 `mvn -pl biz/ai/kuzhambu-ai-domain -am test`。

### 3. AI Invocation Controller 动词整改（5 个文件）

修改文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/AiInvocationController.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/test/java/com/thundax/kuzhambu/ai/interfaces/AiInterfaceArchitectureTest.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/test/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/AiInvocationControllerTest.java`
- `kuzhambu-apps/admin-web/src/pages/ai/invocation/invocation-service.ts`
- `kuzhambu-servers/common/kuzhambu-common-test/src/test/java/com/thundax/kuzhambu/common/test/architecture/RepositoryApiHardRulesArchitectureTest.java`

执行：

1. 将 `summarizeInvocationLogs` 改为 `getInvocationLogSummary`，路径改为 `invocation-log/summary/get`。
2. 将 `markCandidateApplied` 改为 `applyCandidate`，路径改为 `candidate/apply`。
3. 将 `recordBatchSuccess`、`recordBatchFailure` 分别改为 `updateBatchSuccess`、`updateBatchFailure`，路径分别改为 `batch/success/update`、`batch/failure/update`。
4. 将 `canDispatchBatch` 改为 `getBatchDispatchable`，路径改为 `batch/dispatch/get`。
5. 更新列出的调用方；从 `AiInterfaceArchitectureTest.java` 和 `RepositoryApiHardRulesArchitectureTest.java` 删除该 Controller 的 10 个精确 allowance。
6. 运行 `mvn -pl biz/ai/kuzhambu-ai-interface -am test`。

### 4. AI Prompt 与 Platform Controller 动词整改（7 个文件）

修改文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/config/prompt/controller/PromptController.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/platform/controller/PlatformAiController.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/test/java/com/thundax/kuzhambu/ai/interfaces/AiInterfaceArchitectureTest.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/test/java/com/thundax/kuzhambu/ai/interfaces/admin/platform/controller/PlatformAiControllerTest.java`
- `kuzhambu-apps/admin-web/src/pages/ai/prompt/prompt-service.ts`
- `kuzhambu-apps/admin-web/src/pages/ai/prompt/prompt-service-contract.test.ts`
- `kuzhambu-servers/common/kuzhambu-common-test/src/test/java/com/thundax/kuzhambu/common/test/architecture/RepositoryApiHardRulesArchitectureTest.java`

执行：

1. 依次把路径改为 `template/capability/get`、`template/update`、`version/latest`、`version/compare/get`、`version/rollback/update`、`variable/validation/get`、`optimization/suggestion/create`、`prompt-suggestion/create`、`version-summary/get`；对应 Java 方法名分别以 `get`、`update`、`latest` 或 `create` 开头。
2. 更新全部调用方。
3. 从 `AiInterfaceArchitectureTest.java` 和 `RepositoryApiHardRulesArchitectureTest.java` 删除 `promptActionVerbAllowance` 和 `platformAiActionVerbAllowance` 产生的 16 个 key。
4. 运行 `mvn -pl biz/ai/kuzhambu-ai-interface -am test`。

### 5. AI Refinement Controller 动词整改：即时能力（2 个文件）

修改文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/AiRefinementController.java`

执行：

1. 固定改名与路径：`translate` -> `createTranslation` / `translation/create`；`summarize` -> `createSummary` / `summary/create`；`generateTags` -> `createTags` / `tags/create`；`generateQa` -> `createQa` / `qa/create`；`analyzeImage` -> `createImageAnalysis` / `image-analysis/create`；`fuseVisualContext` -> `createVisualFusion` / `visual-fusion/create`；`describeVisual` -> `createVisualDescription` / `visual-description/create`；`generateImage` -> `createImage` / `image/create`；`splitEntry` -> `createEntrySplit` / `entry-split/create`。
2. 运行 `mvn -pl biz/ai/kuzhambu-ai-interface -Dtest=AiInterfaceArchitectureTest test`。

### 6. AI Refinement Controller 动词整改：任务能力（3 个文件）

修改文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/AiRefinementTaskController.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/test/java/com/thundax/kuzhambu/ai/interfaces/AiInterfaceArchitectureTest.java`
- `kuzhambu-servers/common/kuzhambu-common-test/src/test/java/com/thundax/kuzhambu/common/test/architecture/RepositoryApiHardRulesArchitectureTest.java`

执行：

1. 此 Controller 的方法与 POST 路径均已满足白名单；不改生产 Controller。
2. 在步骤 5 的即时能力整改通过后，删除 `legacyControllerActionVerbAllowance` 方法、其两个调用，以及 common-test 中两个 Controller 的通配 allowance；删除无用 import。
3. 运行 `mvn -pl biz/ai/kuzhambu-ai-interface -am test`。

### 7. Storage Command/Query record（21 个 contract，各为一个 2--12 文件小任务）

每一行是一个独立小任务。contract 的绝对相对路径分别为 `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/command/<文件名>` 或 `.../query/<文件名>`；每个小任务固定包含该 contract、`StorageApplicationCommandQueryRecordAllowances.java` 与 `StorageApplicationArchitectureTest.java`。

| 顺序 | contract 文件 |
| --- | --- |
| 7.1 | `AbortMultipartUploadCommand.java` |
| 7.2 | `AddStorageReferencesCommand.java` |
| 7.3 | `ChangeStorageCommand.java` |
| 7.4 | `ChangeStorageObjectStatusCommand.java` |
| 7.5 | `ChangeStorageReferenceStatusCommand.java` |
| 7.6 | `CompleteMultipartUploadCommand.java` |
| 7.7 | `CreateStorageCommand.java` |
| 7.8 | `InitMultipartUploadCommand.java` |
| 7.9 | `RemoveStorageObjectCommand.java` |
| 7.10 | `RemoveStorageReferencesCommand.java` |
| 7.11 | `StorageSortCommand.java` |
| 7.12 | `UploadMultipartPartCommand.java` |
| 7.13 | `UploadStorageObjectCommand.java` |
| 7.14 | `GetReadableStorageContentQuery.java` |
| 7.15 | `GetStorageObjectQuery.java` |
| 7.16 | `ListStorageMimeTypesQuery.java` |
| 7.17 | `ListStorageObjectsQuery.java` |
| 7.18 | `ListStorageReferenceOwnerTypesQuery.java` |
| 7.19 | `ListStorageReferencesQuery.java` |
| 7.20 | `OpenReadableStorageContentQuery.java` |
| 7.21 | `StorageQuery.java` |

每个小任务按以下固定动作执行：

1. 运行 `rg -l '\b<ContractSimpleName>\b' kuzhambu-servers --glob '*.java' | sort`；去除该 contract、allowance 文件与 architecture test 后，余下文件为调用方清单。
2. 调用方清单不超过 9 个文件时，在同一小任务内全部修改；超过 9 个时按字典序每 9 个文件拆为一个调用方子任务，所有子任务完成后才执行第 3 步。每个子任务只改调用方，文件数为 2--9；不删除 allowance。
3. 将 contract 改为字段与字段顺序不变、无 Lombok 的 Java `record`；将本 contract 的所有 `getXxx()` 调用改为 `xxx()` accessor；删除该 contract 的唯一 allowance key。
4. 运行 `mvn -pl biz/storage/kuzhambu-storage-application -am test`。7.21 完成后，删除 `StorageApplicationCommandQueryRecordAllowances.java`，并从 `StorageApplicationArchitectureTest.java` 删除其 import 和调用。

### 8. Storage Facade 构造位置（4 个文件）

修改文件：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/facade/impl/StorageFacadeImpl.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/facade/assembler/StorageReadableContentFacadeAssembler.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/facade/assembler/StorageOwnerBindingFacadeAssembler.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/test/java/com/thundax/kuzhambu/storage/application/StorageApplicationArchitectureTest.java`

执行：将 `StorageFacadeImpl` 中 `RemoveStorageObjectCommand`、`GetStorageObjectQuery`、`ListStorageReferencesQuery` 的构造分别移动到对应 `*FacadeAssembler` 的公开非空转换方法；Facade 只调用 assembler。删除 3 个 facade 构造位置 allowance，运行 `mvn -pl biz/storage/kuzhambu-storage-application -am test`。

### 9. Storage Interface 构造位置（3 个文件）

修改文件：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/StorageObjectController.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/assembler/StorageInterfaceAssembler.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/test/java/com/thundax/kuzhambu/storage/application/StorageApplicationArchitectureTest.java`

执行：将 `StorageSortCommand`、`GetStorageObjectQuery`、`RemoveStorageObjectCommand`、`UploadStorageObjectCommand`、`OpenReadableStorageContentQuery` 和全部 multipart Command 的构造移至 `StorageInterfaceAssembler`。Controller 只调用 assembler；assembler 的公开转换方法不得返回 `null`。删除余下 9 个构造位置 allowance，运行 `mvn -pl biz/storage/kuzhambu-storage-application,biz/storage/kuzhambu-storage-interface -am test`。

### 10. Storage multipart Controller 动词整改（7 个文件）

修改文件：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/main/java/com/thundax/kuzhambu/storage/interfaces/admin/object/controller/StorageObjectController.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/test/java/com/thundax/kuzhambu/storage/interfaces/StorageInterfaceArchitectureTest.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/test/java/com/thundax/kuzhambu/storage/interfaces/admin/StorageObjectMultipartUploadContractTest.java`
- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-service.ts`
- `kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-service.test.ts`
- `kuzhambu-apps/admin-web/e2e/storage/storage-object/storage-object.spec.ts`
- `kuzhambu-servers/common/kuzhambu-common-test/src/test/java/com/thundax/kuzhambu/common/test/architecture/RepositoryApiHardRulesArchitectureTest.java`

执行：将 `initiate` 改为 `initMultipartUpload`，路径改为 `multipart/init`；将 `uploadPart` 改为 `uploadMultipartPart`，路径改为 `multipart/part/upload`；更新全部列出的调用方。从 `StorageInterfaceArchitectureTest.java` 和 `RepositoryApiHardRulesArchitectureTest.java` 删除对应 allowance，删除无用 import，运行 `mvn -pl biz/storage/kuzhambu-storage-interface -am test`。

## Final Verification

按顺序执行：

1. 在 `kuzhambu-servers/` 运行 `mvn -pl biz/ai/kuzhambu-ai-domain,biz/ai/kuzhambu-ai-interface,biz/storage/kuzhambu-storage-application,biz/storage/kuzhambu-storage-interface -am spotless:apply`。
2. 检查 `git diff --check` 与 `git diff -- docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-01-AI-STORAGE.md kuzhambu-servers kuzhambu-apps`；仅保留本 RUNBOOK 范围内改动。
3. 运行 `mvn -pl biz/ai/kuzhambu-ai-domain,biz/ai/kuzhambu-ai-interface,biz/storage/kuzhambu-storage-application,biz/storage/kuzhambu-storage-interface -am test`。
4. 运行 `mvn spotless:check` 与 `mvn checkstyle:check`。
5. 运行 `rg -n 'legacyRepositoryInterfaceMethodNameAllowances|legacyActionVerbAllowances|StorageApplicationCommandQueryRecordAllowances|legacyCommandQueryConstructionAllowances' kuzhambu-servers/biz/ai kuzhambu-servers/biz/storage`，确认本 RUNBOOK 覆盖的 allowance 定义和调用均不存在。

## Closure

所有最终验证通过后，删除本 RUNBOOK，并在 PR 的验证记录中写明完整 Maven 命令及结果。若出现超出本 RUNBOOK 范围的 ArchUnit 违规，不得添加 allowlist；另建新的、文件级拆分的 RUNBOOK 处理。
