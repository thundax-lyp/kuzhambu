# ArchUnit allowlist 清理 07：Classics 三才与发布

## Purpose

先处理 PR #233 已确认遗留问题，再清理 Classics 的三才、发布、清理、报表和搜索切片 legacy allowlist。

本 RUNBOOK 用于生成文件级 TODO 和执行顺序；每个任务必须保持在 2-12 个文件范围内，超过时继续拆分。

## Scope

| 范围 | 文件 |
| --- | --- |
| PR #233 明俗遗留问题 | `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/MingCustomsAdminController.java` |
| PR #233 明俗遗留问题测试 | `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/MingCustomsAdminControllerTest.java` |
| PR #233 王圻遗留问题 | `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/WangqiDocumentAdminController.java` |
| PR #233 王圻遗留问题测试 | `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/WangqiDocumentAdminControllerTest.java` |
| 三才 Command/Query record 例外 | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/ClassicsApplicationCommandQueryRecordAllowances.java` |
| 三才、发布、清理、报表、搜索 ApplicationService 与 Assembler 例外 | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/ClassicsApplicationArchitectureTest.java` |
| 三才、发布 Repository 方法命名例外 | `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/test/java/com/thundax/kuzhambu/classics/domain/ClassicsDomainArchitectureTest.java` |
| 三才、发布 Request/Response 注解与 Assembler 例外 | `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/ClassicsInterfaceArchitectureTest.java` |

## Non-goals

- 不处理内容、明俗和王圻的 allowlist 清理。
- 不改变发布状态机、跨域 facade 契约、Portal 公开读取契约或 Discovery/FastGPT 发布协议。
- 不扩大到 admin-web、portal-web、workers 或 DB seed。

## Inputs From PR #233

PR #233 Codex review 已确认两个后续修复项，必须作为本 RUNBOOK 的前置任务先处理。两个 thread 在 PR #233 已回复“后续 PR 修复”，本轮修复完成后必须回到对应 GitHub discussion 逐条回复修复结果，不得只做统一说明。

1. `https://github.com/thundax-lyp/kuzhambu/pull/233#discussion_r3746699628`
    - 原始评论文件：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/assembler/MingCustomsInterfaceAssembler.java`
    - 触发：`POST /api/classics/mingcustoms/get` 请求不存在或并发删除的明俗条目。
    - 当前风险：`MingCustomsApplicationServiceImpl#get` 返回 `null` 后，controller 直接调用 `MingCustomsInterfaceAssembler.toResponse`，新增非空契约会变成 NPE/500。
    - 目标：controller 或 service 在映射前把缺失资源转换为明确业务错误，并补测试。
    - GitHub 收口：修复提交后，在该 discussion 回复明俗缺失资源处理和测试已补齐。
2. `https://github.com/thundax-lyp/kuzhambu/pull/233#discussion_r3746699633`
    - 原始评论文件：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/assembler/WangqiDocumentInterfaceAssembler.java`
    - 触发：`POST /api/classics/wangqi/documents/get` 请求不存在或并发删除的王圻文档。
    - 当前风险：`WangqiDocumentApplicationServiceImpl#get` 返回 `null` 后，controller 直接调用 `WangqiDocumentInterfaceAssembler.toResponse`，新增非空契约会变成 NPE/500。
    - 目标：controller 或 service 在映射前把缺失资源转换为明确业务错误，并补测试。
    - GitHub 收口：修复提交后，在该 discussion 回复王圻文档缺失资源处理和测试已补齐。

## Plan

按以下顺序执行。每个任务完成后只删除对应精确 allowlist key，不顺手清理其他切片。

### 0. 前置任务：处理 PR #233 缺失资源遗留问题

文件范围：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/MingCustomsAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/MingCustomsAdminControllerTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/WangqiDocumentAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/WangqiDocumentAdminControllerTest.java`

处理动作：

- 明俗 `get` 在调用 `MingCustomsInterfaceAssembler.toResponse` 前处理 service 返回 `null`。
- 王圻 `get` 在调用 `WangqiDocumentInterfaceAssembler.toResponse` 前处理 service 返回 `null`。
- 缺失资源统一抛明确业务错误；不得让 `Objects.requireNonNull` 成为 HTTP 500 的来源。
- 测试覆盖缺失 `id` 和不存在资源两个分支。
- 修复提交后，到 PR #233 两个原始 discussion 逐条回复：
    - `https://github.com/thundax-lyp/kuzhambu/pull/233#discussion_r3746699628`：说明明俗缺失资源已在本轮修复，并注明覆盖的测试。
    - `https://github.com/thundax-lyp/kuzhambu/pull/233#discussion_r3746699633`：说明王圻文档缺失资源已在本轮修复，并注明覆盖的测试。

验收点：

- 两个 PR #233 遗留触发条件不再产生 NPE/500。
- 对应 controller 测试能证明缺失资源会得到明确业务错误。
- 两个 GitHub discussion 均已针对原 comment 回复修复结果。

### 1. 三才基础 Command/Query record 清理

文件范围：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiCategoryCommand.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiCategorySortCommand.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiVolumeCommand.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiVolumeSortCommand.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiEntryCommand.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiEntrySortCommand.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiEntryStatusCommand.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/query/SancaiEntryQuery.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/ClassicsApplicationCommandQueryRecordAllowances.java`

处理动作：

- 将以上三才基础 `Command` / `Query` 从 Lombok class 转为 Java record。
- 更新调用方访问方式。
- 删除对应 `COMMAND_QUERY_RECORD` allowlist key。

验收点：

- 以上类型不再需要 record allowlist。
- 三才基础 application/controller 测试仍通过。

### 2. 三才资产 Command record 与构造位置清理

文件范围：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiDraftCommand.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiEntryImageSortCommand.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiEntryImageUploadCommand.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiImageCommand.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiShowcaseCommand.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiAssetInterfaceAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/SancaiAssetAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/ClassicsApplicationCommandQueryRecordAllowances.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/ClassicsApplicationArchitectureTest.java`

处理动作：

- 将三才资产相关 `Command` 转为 Java record。
- 将 controller 中直接 `new SancaiEntryImageUploadCommand`、`new SancaiEntryImageSortCommand` 的构造迁入 `SancaiAssetInterfaceAssembler`。
- 删除对应 record 与 `COMMAND_QUERY_CONSTRUCTION` allowlist key。

验收点：

- `SancaiAssetAdminController` 不再直接构造 application `Command`。
- 资产相关 assembler 公开方法满足非空契约，或只保留仍未处理的精确 key。

### 3. 三才内容/Portal 构造位置清理

文件范围：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/support/SancaiEntryVersionRestorer.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiInterfaceAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/SancaiAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/SancaiContentAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sancai/assembler/SancaiPortalInterfaceAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sancai/controller/SancaiPortalController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/ClassicsApplicationArchitectureTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/ClassicsInterfaceArchitectureTest.java`

处理动作：

- 将 `SancaiAdminController` 的排序 command 构造迁入 `SancaiInterfaceAssembler`。
- 将 `SancaiContentAdminController` 的内容 QA command 构造迁入合适的 interface assembler。
- 将 `SancaiPortalController` 的 `PageQuery` 构造迁入 `SancaiPortalInterfaceAssembler`，或改为使用统一接口层转换方法。
- 处理 `SancaiEntryVersionRestorer` 中内容 tag/QA command 构造；如属于 application 内部编排，可移动到明确 application service 编排点或封装为 application assembler。
- 删除对应 `COMMAND_QUERY_CONSTRUCTION` allowlist key。

验收点：

- 三才 admin/portal controller 不再直接构造 application `Command` / `Query` / `PageQuery`。
- 三才 interface/portal assembler 公共转换方法满足非空契约。

### 4. 三才 ApplicationService 方法形态清理

文件范围：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/SancaiAssetApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/query/SancaiShowcaseQuery.java`（拟新增）
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiVisualAssetUseCommand.java`（拟新增）
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiVisualAssetVersionCommand.java`（拟新增）
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/ClassicsApplicationArchitectureTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/SancaiAssetApplicationServiceImplTest.java`

处理动作：

- 为 `SancaiAssetApplicationService` 中多参数或裸值方法引入专用 `Command` / `Query`。
- 保持现有业务语义、权限判断和 Storage 读取行为不变。
- 删除对应 `METHOD_SHAPE` allowlist key。

验收点：

- 资产 service 公开方法输入符合 application 边界规则。
- 既有资产测试更新后通过。

### 5. 发布 ApplicationService 方法形态清理

文件范围：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/publication/service/ClassicsPublicationApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/publication/service/ClassicsPublicationCleanupApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/publication/service/ClassicsPublicationExecutionApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/publication/service/ClassicsPublicationReconcileApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/publication/service/ClassicsPublicationSnapshotBindApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/publication/service/ClassicsPublicationContentCommitApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/ClassicsApplicationArchitectureTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/publication/ClassicsPublicationStepExecutorTest.java`

处理动作：

- 对发布内部 workflow 方法做最小结构调整：优先迁入 support/executor 语义，或引入专用 publication `Command`。
- 不改变发布/下线状态机 milestone、租约、重试、cleanup 与成功/失败对账契约。
- 删除可完成的 publication `METHOD_SHAPE` allowlist key；无法安全收敛的 key 必须保留并在执行记录中说明。

验收点：

- 发布状态机相关测试通过。
- `CLASSICS-PUBLICATION-INTERFACE.md` 与专项设计约束未被改变。

### 6. 清理、报表、搜索 ApplicationService 方法形态清理

文件范围：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/cleanup/service/ClassicsCleanupApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/cleanup/service/impl/ClassicsCleanupApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/report/service/ClassicsReportApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/report/service/impl/ClassicsReportApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/search/service/ClassicsSearchContentApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/search/service/impl/ClassicsSearchContentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/ClassicsApplicationArchitectureTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/search/ClassicsSearchContentApplicationServiceImplTest.java`

处理动作：

- 为 cleanup、report、search 的裸值或散列查询参数引入专用 `Command` / `Query`，或改为允许的强类型值对象输入。
- 保持清理目标、报表统计和公开/工作台搜索语义不变。
- 删除对应 `METHOD_SHAPE` allowlist key。

验收点：

- cleanup/report/search application service 方法形态符合规则。
- 相关单测和 ArchUnit 通过。

### 7. 三才与发布 Repository 命名清理

文件范围：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiRepository.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiAssetRepository.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/publication/repository/ClassicsPublicationJobRepository.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiRepositoryImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiAssetRepositoryImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/publication/repository/impl/ClassicsPublicationJobRepositoryImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/test/java/com/thundax/kuzhambu/classics/domain/ClassicsDomainArchitectureTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/test/java/com/thundax/kuzhambu/classics/infra/sancai/SancaiRepositoryTest.java`

处理动作：

- 将 repository 方法名调整到项目 repository 命名规则允许范围。
- 同步 application/infra 调用方与测试。
- 发布 repository 的状态机语义不得被重命名削弱；如规则与语义冲突，保留精确 key 并记录原因。

验收点：

- 三才 repository allowlist key 能删除。
- 发布 repository key 只保留确有状态机语义必要的项目。

### 8. 三才与发布 API 模型注解清理

文件范围：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/publication/controller/request/ClassicsPublicationActionRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/publication/controller/request/ClassicsPublicationBatchActionRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/publication/controller/request/ClassicsPublicationJobGetRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/publication/controller/request/ClassicsPublicationJobPageRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/publication/controller/response/ClassicsPublicationCreateResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/publication/controller/response/ClassicsPublicationBatchResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/publication/controller/response/ClassicsPublicationBatchItemResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/publication/controller/response/ClassicsPublicationJobResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/ClassicsInterfaceArchitectureTest.java`

处理动作：

- 补齐发布 request/response 模型要求的注解。
- 不改变字段名、HTTP 路径或发布接口契约。
- 删除对应 request/response annotation allowlist key。

验收点：

- 发布 API 模型注解 allowlist key 清零或仅保留非本任务范围项。

### 9. 三才 API 模型注解与 assembler 非空契约清理

文件范围：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiAssetRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiCategoryRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiEntryRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiEntryPageRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiEntryVersionRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiVolumeRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiInterfaceAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiAssetInterfaceAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/ClassicsInterfaceArchitectureTest.java`

处理动作：

- 补齐三才 request 模型注解。
- 收敛三才 admin assembler 公共方法非空契约。
- 删除对应 request annotation 与 assembler non-null allowlist key。

验收点：

- 三才 admin request 注解 key 删除。
- 三才 admin assembler 不再需要 legacy non-null class allowlist，或只保留未处理文件的精确说明。

### 10. 三才 Response 与 Portal API 模型注解清理

文件范围：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiAssetResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiCategoryResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiContentResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiEntryResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiEntryVersionResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiVolumeResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sancai/controller/request/SancaiPortalEntrySearchRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sancai/controller/response/SancaiPortalCategoryResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sancai/controller/response/SancaiPortalVolumeResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/ClassicsInterfaceArchitectureTest.java`

处理动作：

- 补齐三才 response 与 portal request/response 模型注解。
- 保持 Portal 只读 ES READY 且未删除内容的契约不变。
- 删除对应 annotation allowlist key。

验收点：

- 三才 response 与 portal API 模型注解 key 清零。

### 11. 最终收口

文件范围：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/ClassicsApplicationCommandQueryRecordAllowances.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/ClassicsApplicationArchitectureTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/test/java/com/thundax/kuzhambu/classics/domain/ClassicsDomainArchitectureTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/ClassicsInterfaceArchitectureTest.java`
- `docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-07-CLASSICS-SANCAI-PUBLICATION.md`

处理动作：

- 确认本切片 key 已清零或仅保留有明确理由的非本切片 key。
- 删除本 RUNBOOK。
- 确认 `TODO.md` 中本轮任务已删除或收窄。

验收点：

- 本 RUNBOOK 不再留在仓库。
- 工作区不包含临时执行现场。

## Verification

按任务运行最小验证；最终在 `kuzhambu-servers/` 下运行：

```sh
mvn -pl biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-domain,biz/classics/kuzhambu-classics-interface -am test
mvn spotless:check
mvn checkstyle:check
```

前置 PR #233 遗留问题完成后至少运行：

```sh
mvn -pl biz/classics/kuzhambu-classics-interface -am -Dtest=com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.MingCustomsAdminControllerTest,com.thundax.kuzhambu.classics.interfaces.admin.wangqi.WangqiDocumentAdminControllerTest -Dsurefire.failIfNoSpecifiedTests=false test
```

## Closure

- 前置 PR #233 遗留问题已修复并提交。
- PR #233 两个遗留 discussion 已逐条回复修复结果：
    - `https://github.com/thundax-lyp/kuzhambu/pull/233#discussion_r3746699628`
    - `https://github.com/thundax-lyp/kuzhambu/pull/233#discussion_r3746699633`
- 本切片 key 清零后删除本文档。
- `TODO.md` 不保留已完成项；未完成项必须收窄为剩余文件级任务。
