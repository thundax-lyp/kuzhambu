# RUNBOOK-CLASSICS-RENDER-CLOSURE

## Purpose

本文档用于指导 Classics 导出产物生成闭环和 Sancai 静态展示产物生成闭环的分步实现。

目标是把当前已经存在的任务申请入口真正闭环到：

- 调用 render workers；
- 接收 render 产物；
- 写入 Storage；
- 更新导出/展示任务状态；
- 提供后台查询、下载和过期控制所需的运行时基础。

本文档是执行手册，不替代：

- `docs/10-requirements/CLASSICS-REQUIREMENTS.md`
- `docs/20-interfaces/WORKERS-RENDER-INTERFACE.md`
- `docs/30-designs/CLASSICS-DESIGN.md`

## Scope

本次只覆盖：

- Classics 内容导出任务 `classics_content_export_job` 的 render 闭环。
- Sancai 静态展示任务 `classics_sancai_showcase` 的 render 闭环。
- Java servers 到 Python workers 的 render client 接入。
- render 成功后的 Storage 文件对象创建与任务记录回写。
- render 失败后的任务失败状态回写。
- Admin Web 导出与静态展示的前后端完整闭环。

本次不覆盖：

- Portal 展示改动。
- 新的导出模板设计。
- Operations 报表 render 闭环。
- 批量导出调度。
- MQ、队列、异步基础设施引入。
- workers 端模板大改或视觉改版。

## Current Baseline

当前已具备：

- Classics 已有“创建导出任务”入口。
- Sancai 已有“创建静态展示任务”入口。
- `classics_content_export_job` 和 `classics_sancai_showcase` 已有领域模型、Repository 和入库能力。
- workers 已提供：
  - `POST /internal/render/classics-export`
  - `POST /internal/render/sancai-showcase`
  - 对应 stream 接口
- workers render 协议、签名规则和测试已存在。
- Storage 已具备创建对象、读取内容、引用管理的基础能力。

当前缺口：

- Classics 未实现 render worker client。
- 创建任务后没有真正调用 workers。
- workers 返回产物后没有进入 Storage。
- 导出任务状态没有从 `REQUESTED` 演进到 `SUCCEEDED/FAILED/EXPIRED`。
- 静态展示任务状态没有从 `REQUESTED` 演进到 `SUCCEEDED/FAILED`。
- Admin Web 尚未形成“发起导出 / 发起静态展示 / 查看任务 / 下载产物”的完整闭环。

## Execution Rules

- 复用 AI 现有的 `WorkerAiHttpClient + SignatureSupport + Properties` 模式，不重新发明内部调用协议。
- 本轮只实现同步 render 闭环，不实现 stream 路径。
- 导出产物统一保存到 Storage，静态展示产物统一保存到 Storage。
- 导出任务默认有效期固定为 `7` 天：`expiresAt = requestedAt + 7 days`。
- 静态展示任务不设置过期时间，不参与自动过期。
- render 失败原因不落任务表，只记录失败状态并输出日志。
- Storage 文件写入统一复用 `StorageUploadStreamHelper`，不新增第二套文件落盘入口。
- 每个执行任务控制在 `1-4` 个文件内。
- 每个 commit 只表达一次明确工程判断。
- 不处理当前工作区中与本任务无关的 `docs/40-readiness/*.md` 未跟踪文件。

## Target Outcome

完成后应达到：

1. 管理员创建导出任务后，服务端能同步调用 workers 生成产物并写入 Storage。
2. 导出任务记录能写回 `status / storageObjectId / expiresAt / itemCount / assetCount`。
3. 管理员创建 Sancai 静态展示任务后，服务端能同步调用 workers 生成 HTML 展示产物并写入 Storage。
4. 展示任务记录能写回 `status / storageObjectId / entryCount`。
5. render 失败时，任务记录进入失败态，后台可见。
6. Admin Web 能发起导出和静态展示任务。
7. Admin Web 能分页查看导出任务和展示任务，并下载成功产物。
8. 过期导出任务在 Admin Web 中显示过期状态且不可下载。

## Data Structure Adjustments

本次固定复用已有表结构，不做数据库 schema 变更。

### 1. 复用的持久化字段

本轮固定使用以下现有字段，不新增同义字段：

- `classics_content_export_job.status`
- `classics_content_export_job.storage_object_id`
- `classics_content_export_job.expires_at`
- `classics_content_export_job.item_count`
- `classics_content_export_job.asset_count`
- `classics_content_export_job.visibility_risk_status`
- `classics_content_export_job.content_changed`
- `classics_sancai_showcase.status`
- `classics_sancai_showcase.storage_object_id`
- `classics_sancai_showcase.entry_count`
- `classics_sancai_showcase.visibility_risk_status`

对应 Java 领域对象文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/model/entity/ClassicsContentExportJob.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/model/entity/SancaiShowcase.java`

### 2. 本轮新增的 Java 侧结构

本轮允许新增以下“非表结构”数据结构：

- render client 配置类
- render 签名支持类
- render request/response DTO
- render 成功/失败结果对象
- 导出任务与静态展示任务的应用层结果对象

本轮新增文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/client/WorkerRenderProperties.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/client/WorkerRenderSignatureSupport.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/client/WorkerRenderClient.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/client/WorkerRenderHttpClient.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/client/dto/WorkerRenderDtos.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/result/ClassicsExportJobResult.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/result/SancaiShowcaseJobResult.java`

### 3. 本轮不做的数据库变更

本轮采用以下固定策略：

- 失败细节进入日志和审计摘要；
- 任务表只写失败状态；
- `classics_content_export_job` 不新增 `error_message`；
- `classics_sancai_showcase` 不新增 `error_message`。

## Work Breakdown

### Stage 1 Render Client 基础接入

#### T1-1 新建 render properties

- 范围文件：
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/client/WorkerRenderProperties.java`（新建）
- 处理动作：
  - 新建 render worker 基础配置，字段对齐 AI 侧 `baseUrl/internalSecret/serviceName/timeoutMs`。
- 数据结构变更：
  - 新增 `WorkerRenderProperties.baseUrl`
  - 新增 `WorkerRenderProperties.internalSecret`
  - 新增 `WorkerRenderProperties.serviceName`
  - 新增 `WorkerRenderProperties.timeoutMs`
- 验收点：
  - 配置类可被 Spring 注入；
  - 默认值与本地联调环境兼容。
- 提交：
  - `Feat(classics-infra): 增加 render worker 配置`

#### T1-2 新建 render signature support

- 范围文件：
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/client/WorkerRenderSignatureSupport.java`（新建）
- 处理动作：
  - 复制 AI 侧签名算法，封装 render 请求签名。
- 数据结构变更：
  - 新增 `sign(...)`
  - 新增 `signingInput(...)`
  - 新增 `sha256(...)`
- 验收点：
  - 签名输入与 `WORKERS-RENDER-INTERFACE.md` 一致；
  - 单测可直接复用 AI 侧断言方式。
- 提交：
  - `Feat(classics-infra): 增加 render worker 签名支持`

#### T1-3 新建 render DTO

- 范围文件：
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/client/dto/WorkerRenderDtos.java`（新建）
- 处理动作：
  - 定义 Java 侧 render request/response/artifact DTO，字段严格对齐 workers 协议。
- 数据结构变更：
  - 新增 `WorkerRenderRequest`
  - 新增 `WorkerRenderResponse`
  - 新增 `RenderArtifact`
  - 新增 `RenderSummary`
  - 新增 `RenderUsage`
  - 新增 `RenderError`
- 验收点：
  - 可反序列化 workers 当前同步响应；
  - 覆盖 `CLASSICS_EXPORT` 和 `SANCAI_SHOWCASE` 两类 renderType。
- 提交：
  - `Feat(classics-infra): 增加 render worker DTO`

#### T1-4 新建 render http client

- 范围文件：
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/client/WorkerRenderClient.java`（新建）
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/client/WorkerRenderHttpClient.java`（新建）
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/test/java/com/thundax/kuzhambu/classics/infra/client/WorkerRenderHttpClientTest.java`（新建）
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/test/java/com/thundax/kuzhambu/classics/infra/client/WorkerRenderSignatureSupportTest.java`（新建）
- 处理动作：
  - 按 AI client 模式实现同步 `classics-export`、`sancai-showcase` 两个调用入口。
- 数据结构变更：
  - `WorkerRenderClient.renderClassicsExport(...)`
  - `WorkerRenderClient.renderSancaiShowcase(...)`
- 验收点：
  - 请求头包含内部鉴权字段；
  - 产物内容、文件名、contentType、sha256 可被正确解析；
  - 非 2xx、协议错误、超时能映射为稳定失败结果。
- 提交：
  - `Feat(classics-infra): 接入 render worker http client`

### Stage 2 导出任务闭环

#### T2-1 补导出任务状态流转方法

- 范围文件：
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/model/entity/ClassicsContentExportJob.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/repository/ClassicsContentRepository.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/repository/impl/ClassicsContentRepositoryImpl.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/persistence/mapper/ClassicsContentMapper.java`
- 处理动作：
  - 增补导出任务从请求态到成功/失败态的更新方法。
- 数据结构变更：
  - `ClassicsContentExportJob` 增加领域态迁移方法
  - `ClassicsContentRepository` 增加导出任务更新方法
  - `ClassicsContentMapper` 增加更新 SQL
- 验收点：
  - 能按任务 ID 回写 `status/storageObjectId/expiresAt/itemCount/assetCount`。
- 提交：
  - `Feat(classics-content): 补齐导出任务状态回写`

#### T2-2 在应用层真正执行导出 render

- 范围文件：
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/command/ContentExportCommand.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/result/ClassicsExportJobResult.java`（新建）
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceImplTest.java`
- 处理动作：
  - `createExportJob(...)` 改为：创建任务记录 -> 组装导出快照 -> 调用 `/internal/render/classics-export` -> 将返回产物写入 Storage -> 回写任务状态。
- 数据结构变更：
  - `ClassicsExportJobResult.jobId`
  - `ClassicsExportJobResult.status`
  - `ClassicsExportJobResult.storageObjectId`
- 验收点：
  - 成功时任务有 Storage 对象；
  - 失败时任务进入失败态；
  - 单测覆盖成功/失败两条路径。
- 提交：
  - `Feat(classics-content): 打通导出任务执行闭环`

#### T2-3 导出产物写入 Storage

- 范围文件：
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
  - `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/helper/StorageUploadStreamHelper.java`
  - `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/test/java/com/thundax/kuzhambu/storage/application/service/impl/StorageApplicationServiceImplTest.java`（新建）
- 处理动作：
  - 将 workers 返回的 inline artifact 转为 `InputStream`，通过 `StorageUploadStreamHelper.upload(...)` 写入 Storage，并绑定任务记录。
- 数据结构变更：
  - 不新增 Storage 公共接口；
  - 复用 `StorageUploadStreamHelper` 完成服务端产物入库。
- 验收点：
  - 产物可通过 Storage 读取；
  - 文件名、contentType、size 保持正确。
- 提交：
  - `Feat(classics-content): 导出产物接入 storage`

#### T2-4 导出任务分页/下载最小后台闭环

- 范围文件：
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/ClassicsContentAdminController.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/assembler/ClassicsContentInterfaceAssembler.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/request/ClassicsContentRequest.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/response/ClassicsContentResponse.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/content/ClassicsContentAdminControllerTest.java`（新建）
- 处理动作：
  - 新增导出任务分页接口和导出产物下载接口，下载时校验 `status == SUCCEEDED` 且 `expiresAt > now()`。
- 数据结构变更：
  - `ClassicsContentResponse` 增加导出任务分页/下载所需字段
  - `ClassicsContentRequest` 增加导出任务分页筛选入参
- 验收点：
  - 管理员可看到任务状态；
  - 成功任务可下载；
  - 过期任务不可下载。
- 提交：
  - `Feat(classics-admin): 增加导出任务查询与下载`

#### T2-5 Admin Web 导出闭环

- 范围文件：
  - `kuzhambu-apps/admin-web/src/api/classics/export-service.ts`（新建）
  - `kuzhambu-apps/admin-web/src/api/classics/export-service-contract.test.ts`（新建）
  - `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`
  - `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
- 处理动作：
  - 在 Admin Web 增加导出任务 API，接入三才图会条目页的“导出”动作、任务列表和下载入口。
- 数据结构变更：
  - 新增 `ClassicsExportJobRecord`
  - 新增 `ClassicsExportCreateCommand`
  - 新增 `ClassicsExportPageQuery`
- 验收点：
  - 管理员能从三才图会条目面板发起导出；
  - 管理员能查看导出任务状态；
  - 成功任务可下载，过期任务按钮禁用。
- 提交：
  - `Feat(admin-web): 接入 classics 导出任务闭环`

### Stage 3 Sancai 静态展示闭环

#### T3-1 补静态展示任务状态流转方法

- 范围文件：
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/model/entity/SancaiShowcase.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiAssetRepository.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiAssetRepositoryImpl.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/mapper/SancaiAssetMapper.java`
- 处理动作：
  - 增补静态展示任务成功/失败状态回写。
- 数据结构变更：
  - `SancaiShowcase` 增加领域态迁移方法
  - `SancaiAssetRepository` 增加展示任务更新方法
  - `SancaiAssetMapper` 增加更新 SQL
- 验收点：
  - 能按任务 ID 回写 `status/storageObjectId/entryCount`。
- 提交：
  - `Feat(sancai): 补齐静态展示任务状态回写`

#### T3-2 在应用层真正执行 showcase render

- 范围文件：
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiShowcaseCommand.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/result/SancaiShowcaseJobResult.java`（新建）
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/SancaiAssetApplicationServiceImplTest.java`
- 处理动作：
  - `requestShowcase(...)` 改为：创建任务记录 -> 组装展示快照 -> 调用 `/internal/render/sancai-showcase` -> 将返回 HTML 写入 Storage -> 回写任务状态。
- 数据结构变更：
  - `SancaiShowcaseJobResult.showcaseId`
  - `SancaiShowcaseJobResult.status`
  - `SancaiShowcaseJobResult.storageObjectId`
- 验收点：
  - 生成成功时能拿到 HTML 文件对象；
  - 失败时进入失败态；
  - 单测覆盖成功/失败路径。
- 提交：
  - `Feat(sancai): 打通静态展示任务执行闭环`

#### T3-3 静态展示任务分页/下载最小后台闭环

- 范围文件：
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/SancaiAssetAdminController.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiAssetInterfaceAssembler.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiAssetRequest.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiAssetResponse.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/SancaiAssetAdminControllerTest.java`
- 处理动作：
  - 新增静态展示任务分页接口和产物下载接口，下载时校验 `status == SUCCEEDED`。
- 数据结构变更：
  - `SancaiAssetResponse` 增加展示任务分页/下载所需字段
  - `SancaiAssetRequest` 增加展示任务分页筛选入参
- 验收点：
  - 管理员可查看展示任务状态并下载成功产物。
- 提交：
  - `Feat(sancai-admin): 增加静态展示任务查询与下载`

#### T3-4 Admin Web 静态展示闭环

- 范围文件：
  - `kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`
  - `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`
  - `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
  - `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-service-contract.test.ts`
- 处理动作：
  - 在三才图会条目页接入“生成静态展示”动作、任务列表和下载入口。
- 数据结构变更：
  - 新增 `SancaiShowcaseRecord`
  - 新增 `SancaiShowcaseCreateCommand`
  - 新增 `SancaiShowcasePageQuery`
- 验收点：
  - 管理员能从三才图会条目面板发起静态展示；
  - 管理员能查看静态展示任务状态；
  - 成功任务可下载 HTML 产物。
- 提交：
  - `Feat(admin-web): 接入 sancai 静态展示任务闭环`

### Stage 4 联调与收口

#### T4-1 workers 冒烟联调

- 范围文件：
  - `kuzhambu-workers/tests/...`
  - `kuzhambu-servers/...` 仅补必要 testcase
- 处理动作：
  - 用最小快照跑通 `classics-export` 和 `sancai-showcase` 实际调用。
- 验收点：
  - Java servers 与 workers 的协议字段完全对齐；
  - 文件可落 Storage 并可读取。
- 提交：
  - `Test(classics-workers): 锁定 render 闭环联调`

#### T4-2 过期控制与下载校验

- 范围文件：
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/ClassicsContentAdminController.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/content/ClassicsContentAdminControllerTest.java`
- 处理动作：
  - 固定导出任务过期规则：创建后 `7` 天过期；过期任务下载返回不可下载错误；前端展示过期状态。
- 数据结构变更：
  - 无新增字段，只使用 `expiresAt`
- 验收点：
  - 过期导出任务不可下载；
  - 前端任务列表能展示“已过期”。
- 提交：
  - `Feat(classics-export): 增加导出过期控制`

#### T4-3 文档和 TODO 收口

- 范围文件：
  - `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
  - `docs/40-readiness/STORAGE-IMPLEMENTATION-COVERAGE.md`
  - `TODO.md`（如存在本任务）
  - 当前 RUNBOOK
- 处理动作：
  - 更新覆盖文档中的“仅记录任务”表述；
  - 删除已完成 TODO；
  - 本任务完成后删除本 RUNBOOK。
- 验收点：
  - 文档口径与代码一致；
  - 无过期 RUNBOOK 残留。
- 提交：
  - `Docs(readiness): 更新 classics render 闭环覆盖状态`

## Suggested Commit Order

固定按以下顺序推进：

1. `classics-infra` render client 基础
2. `classics-content` 导出任务状态回写
3. `classics-content` 导出执行闭环
4. `classics-admin` 导出查询/下载
5. `admin-web` 导出闭环
6. `sancai` 展示任务状态回写
7. `sancai` 展示执行闭环
8. `sancai-admin` 展示查询/下载
9. `admin-web` 展示闭环
10. `test/docs` 联调与收口

## Verification Strategy

每个阶段按影响面跑最小验证，最终收口跑完整闭环验证。

Java servers：

- `cd kuzhambu-servers`
- `mvn spotless:check`
- `mvn checkstyle:check`
- 针对受影响模块跑最小单测/契约测试

Workers：

- `cd kuzhambu-workers`
- `.venv/bin/python -m ruff format --check .`
- `.venv/bin/python -m ruff check .`
- `.venv/bin/python -m pytest -p no:capture tests/test_render_routes.py tests/test_classics_export.py tests/test_sancai_showcase.py`

联调冒烟：

- 本地启动 workers
- 本地启动 admin starter
- 通过 Admin API 创建导出任务和静态展示任务
- 校验任务记录状态和产物下载
- 打开 Admin Web 三才图会页面，实际点击导出和静态展示入口完成一次前后端闭环操作

## Risks

- `createExportJob` / `requestShowcase` 当前方法签名返回任务 ID，本轮保持这个响应语义不变，前端通过后续分页查询拿到最终状态。
- workers 当前同步接口返回 inline artifact，本轮导出格式和展示格式以中小产物为前提，不引入 stream。
- 导出范围快照与展示范围快照本轮只支持当前 Admin Web 实际发起的范围，不扩展未接入的批量格式。

## Exit Criteria

满足以下条件时，本轮任务可判定完成：

- Classics 导出任务能够真正生成文件并写入 Storage。
- Sancai 静态展示任务能够真正生成 HTML 并写入 Storage。
- Admin Web 能发起任务、查看任务状态并下载成功产物。
- 失败任务不会停留在请求态。
- 过期导出任务不可下载。
- 覆盖文档已更新，RUNBOOK 可删除。
