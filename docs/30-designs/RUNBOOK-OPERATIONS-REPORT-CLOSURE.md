# Operations Report Closure Runbook

## 目的

本文档定义 Operations “报表闭环”实施手册，目标是在 `kuzhambu-servers/` 内完成一条可运行、可追溯、可验证的周报/月报闭环。

闭环范围固定为：

1. admin 发起生成周报或月报任务并立即拿到任务号。
2. Operations 聚合关联域统计结果并形成报表快照。
3. Operations 写入 `operations_report` 台账并驱动状态流转。
4. Operations 调用 render workers 生成 HTML 或 PDF 临时产物。
5. Operations 将产物交给 Storage 保存，并回写 `storageObjectId`。
6. admin 基于任务号或记录列表轮询结果，完成后开放下载。

本文档是本轮执行依据。需求以 [`docs/10-requirements/OPERATIONS-REQUIREMENTS.md`](../10-requirements/OPERATIONS-REQUIREMENTS.md) 为准。

## 分支

- 当前工作分支：`feat/operations-report-runbook`

## 成功标准

本轮完成后，至少满足以下验收结果：

- Classics、AI、Discovery、Knowledge 面向 Operations 的 summary 统计规格先统一，再进入代码实现。
- admin 端存在 Operations 报表生成入口和列表/详情读取入口。
- `generate` 接口发起任务后立即返回 `reportId`，不等待文件生成完成。
- `ReportRecord` 能完整记录请求人、统计周期、生成状态、失败原因和导出产物定位信息。
- 周报/月报生成链路完成权限校验、聚合统计、worker 调用、Storage 入库、状态回写。
- 失败路径能记录失败原因，成功路径能落 `storageObjectId`。
- 相关 controller、application、worker client、storage 入库链路具备最小相关测试。

## 已确认项对应操作

### 任务式生成

- `generate` 接口只创建或启动报表任务，不同步等待 worker 完成。
- `generate` 返回 `reportId`。
- `detail` 和 `page` 必须返回 `reportStatus`、`failureReason`、`storageObjectId`、`artifactFilename`。
- 下载入口只在 `reportStatus=SUCCEEDED` 且 `storageObjectId` 不为空时开放。

### 权限编码

- `generate` 接口固定使用 `operations:report:generate`。
- `page`、`detail`、下载入口固定使用 `operations:report:view`。
- 不复用 `super` 作为首版默认权限编码。

### 独立响应模型

- Operations admin 接口只能返回自己的独立响应模型：
  - `OperationsReportGenerateResponse`
  - `OperationsReportPageResponse`
  - `OperationsReportDetailResponse`
- 关联域只能向 Operations 暴露 application result 或 read model。

### 趋势序列 bucket

- 周报趋势序列固定按日聚合。
- 月报趋势序列固定按周聚合。
- 关联域 summary result 内必须显式返回 `bucket` 字段，不允许 Operations 自己推导。

### `operations_report` 最终建表口径

- 本轮直接补齐：
  - `request_id`
  - `trace_id`
  - `template_version`
  - `artifact_filename`
- `report_status` 固定覆盖：
  - `PENDING`
  - `PROCESSING`
  - `SUCCEEDED`
  - `FAILED`

## 当前基线

已确认事实：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/` 与 `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/` 当前只有 `pom.xml`，没有 `src/main`。
- Operations 已有 `report` 的 domain/infra 持久化骨架：
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/report/model/entity/ReportRecord.java`
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/report/repository/ReportRepository.java`
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/persistence/dataobject/ReportDO.java`
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/repository/impl/ReportRepositoryImpl.java`
- render workers 的 Operations 报表接口契约已在 [`WORKERS-RENDER-INTERFACE.md`](../20-interfaces/WORKERS-RENDER-INTERFACE.md) 中定义，路径为 `POST /internal/render/operations-report`。
- Storage 已存在服务端产物入库 helper：
  - `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/helper/StorageUploadStreamHelper.java`
- 当前尚未发现 Operations 现成任务调度/异步执行骨架；首版需要以 report task 台账驱动“发起任务 -> 轮询结果 -> 完成下载”的最小异步模型。
- System 已存在 admin 权限和审计入口参考实现：
  - `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/CurrentUserController.java`
  - `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/controller/AuditController.java`
- Discovery 与 Knowledge 已存在可参考的 admin 聚合读接口：
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/DiscoverySearchAdminController.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/KnowledgeTaxonomyController.java`
- 本轮按“新系统重建表”处理数据结构调整，不受现有 migration/增量 DDL 路径约束；表结构以需求和最终建表口径为准。

## 本轮范围

在范围内：

- Operations 报表的 application/service、interface/controller、任务发起、结果轮询、worker client、storage 入库、记录查询闭环。
- 关联域为 Operations 提供“报表所需聚合读结果”的最小公开读取能力。
- report 相关最小测试。
- 必要的数据结构补齐。

不在范围内：

- 仪表盘页面与图表化展示。
- 报表 SSE 进度展示。
- 自动调度。
- 备份、恢复、清理、健康检查、长任务闭环。
- 通用日志正文聚合。

## 关联域接口准备度

前置决策：

- 先统一 Classics、AI、Discovery、Knowledge 面向 Operations 的 summary 统计规格。
- 统一规格完成前，不进入关联域统计实现，也不进入 Operations 聚合实现。
- Operations 不直接消费他域 controller response；统一规格必须沉淀为 application result 或 domain read model。
- Operations 对 admin 侧输出只使用自己的独立 `XxxResponse`，不得把他域 `Response` 模型直接暴露为 Operations 接口契约。

### 可直接复用或薄适配

| 领域 | 现状 | 结论 | 关键文件 |
| --- | --- | --- | --- |
| System | 已有 `@HasPermission`、`CurrentUserResolver`、`AuditController` 参考实现 | Operations 只需复用权限门禁和“日志/审计入口跳转信息”装配方式 | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/CurrentUserController.java`, `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/controller/AuditController.java` |
| Storage | 已有 `StorageApplicationService` 与 `StorageUploadStreamHelper` | Operations 可直接复用服务端产物入库 helper | `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/StorageApplicationService.java`, `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/helper/StorageUploadStreamHelper.java` |
| Workers | Render 接口已定义，Classics 已有 DTO/Client 模式 | Operations 需要新增自己的 client，但协议可沿用 Classics 模式 | [`WORKERS-RENDER-INTERFACE.md`](../20-interfaces/WORKERS-RENDER-INTERFACE.md), `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/common/client/WorkerRenderClient.java`, `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/common/client/dto/WorkerRenderDtos.java` |
| Knowledge | 已有标签治理统计读模型和 admin controller 测试 | 可抽出 Operations 所需最小聚合读接口，优先复用现有 `TagGovernanceMetricsResult` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/TaxonomyApplicationService.java`, `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/result/TagGovernanceMetricsResult.java` |
| Discovery | 已有 admin 搜索/问答读入口和 repository/page 能力 | 可以先基于现有 admin 读能力做薄聚合，补一个面向 Operations 的 summary query | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/DiscoverySearchAdminController.java`, `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/DiscoveryQaAdminController.java` |

### 需要新增最小聚合读契约

| 领域 | 原因 | 本轮建议 |
| --- | --- | --- |
| Classics | 需求要求内容数量、翻译覆盖率、配图覆盖率、视觉资产覆盖率、热门内容、分享统计；当前未看到统一 Operations 读模型 | 新增一个只读 `LayerPublicApi` 统计入口，返回报表所需聚合结果 |
| AI | 需求要求 AI 调用统计；当前可见调用记录和 capability 状态，但未看到面向 Operations 的汇总结果 API | 新增 AI 调用汇总只读入口，提供调用量、失败量、延迟、成本摘要 |
| Discovery | 当前更偏日志/明细查询，缺少报表用 summary | 新增搜索/问答 summary result，避免 Operations 自己拼日志分页 |

## Summary 统计规格

统一要求：

- 统计结果必须面向“周报/月报快照生成”设计，而不是面向单页面零散展示。
- 统计结果必须带明确时间范围：`periodStart`、`periodEnd`。
- 统计结果必须只返回已聚合值、趋势序列、排行结果和必要说明，不返回明细日志列表。
- 统计结果必须可被 Operations 直接组装为报表快照，不允许 Operations 自己跨域二次拼接明细。
- 关联域对 Operations 暴露 application result；Operations 再通过自己的 assembler 映射为 `OperationsReportResponse`、`OperationsReportDetailResponse` 等独立响应模型。
- 趋势序列时间 bucket 固定统一为：
  - 周报：按日
  - 月报：按周

### Classics 规格

- `contentCount`
- `translatedContentCount`
- `imageReadyContentCount`
- `visualAssetReadyContentCount`
- `shareVisitCount`
- `topContents`
  - `contentId`
  - `contentType`
  - `title`
  - `visitCount`
- `contentGrowthSeries`
  - `bucket`
  - `createdCount`

### AI 规格

- `invocationCount`
- `succeededInvocationCount`
- `failedInvocationCount`
- `avgLatencyMs`
- `totalCostAmount`
- `topCapabilities`
  - `capability`
  - `invocationCount`

### Discovery 规格

- `searchCount`
- `qaCount`
- `avgSearchLatencyMs`
- `topQueries`
  - `queryText`
  - `count`
- `searchTrendSeries`
  - `bucket`
  - `searchCount`
- `qaTrendSeries`
  - `bucket`
  - `qaCount`

### Knowledge 规格

- `tagCoverageRate`
- `topTags`
  - `tagName`
  - `contentRefCount`
- `categoryDistributions`
  - `categoryName`
  - `tagCount`
- `monthlyNewTags`
  - `month`
  - `tagCount`

## 闭环设计

### Operations 内主链路

1. admin 调用 `generate` 接口，提交 `reportType`、`format`、`periodStart`、`periodEnd`。
2. controller 做参数校验并交给 `ReportApplicationService`。
3. `ReportApplicationService` 校验权限与周期，写入一条 `PENDING` 或 `PROCESSING` 状态 `ReportRecord`，并立即返回 `reportId`。
4. 后台执行单元根据 `reportId` 拉起报表生成。
5. `OperationsReportMetricsGateway` 聚合 Classics、AI、Discovery、Knowledge、System 的报表数据。
6. `OperationsReportSnapshotAssembler` 组装 worker 输入快照。
7. `OperationsWorkerRenderClient` 调用 `/internal/render/operations-report`。
8. `StorageUploadStreamHelper` 将 worker 返回产物写入 Storage。
9. `ReportApplicationService` 回写 `storageObjectId`、`completedAt`、最终 `reportStatus`。
10. admin 通过 `detail` 或 `page` 轮询任务结果，完成后读取下载信息。

### 失败路径

- 任一聚合读失败：更新 `reportStatus=FAILED`，写 `failureReason`，不写 `storageObjectId`。
- worker 渲染失败：更新 `reportStatus=FAILED`，写稳定失败原因。
- Storage 入库失败：更新 `reportStatus=FAILED`，写入 Storage 错误摘要。
- `generate` 已返回但后台执行失败：保留 `reportId`，由轮询接口返回 `FAILED` 与 `failureReason`。

## 数据结构调整

### 必做调整

#### 1. Operations 报表输入/输出结构

新增 Operations application/internal 读模型，避免 controller、worker client、关联域返回对象直接耦合：

- `OperationsReportGenerateCommand`
- `OperationsReportPageQuery`
- `OperationsReportDetailQuery`
- `OperationsReportResult`
- `OperationsReportPageResult`
- `OperationsReportSnapshot`
- `OperationsReportSection`
- `OperationsReportResponse`
- `OperationsReportDetailResponse`
- `OperationsReportGenerateResult`

建议文件位置：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/command/`
- `.../query/`
- `.../result/`
- `.../support/`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/report/controller/response/`

#### 2. 关联域聚合结果结构

为避免 Operations 直接依赖他域 admin controller response，本轮为每个关联域补一个最小 application result：

- `ClassicsOperationsStatsResult`
- `AiOperationsStatsResult`
- `DiscoveryOperationsStatsResult`
- `KnowledgeOperationsStatsResult`

这些结构只承载 Operations 报表需要的聚合值，不承载页面专属字段。

### 建议调整

#### 1. `operations_report` 台账增强

当前 `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/report/model/entity/ReportRecord.java` / `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/persistence/dataobject/ReportDO.java` 能覆盖需求中的基础字段，但为了形成稳定闭环，建议增加以下非业务事实字段：

- `requestId`
- `traceId`
- `templateVersion`
- `artifactFilename`

用途：

- `requestId` / `traceId`：关联 worker 与 Storage 问题排查。
- `templateVersion`：追踪渲染模板版本。
- `artifactFilename`：列表页直接展示导出文件名，避免再读 Storage 元数据。

本轮决策：

- `operations_report` 本轮直接补齐 `requestId`、`traceId`、`templateVersion`、`artifactFilename`。
- `operations_report.report_status` 要明确覆盖至少 `PENDING`、`PROCESSING`、`SUCCEEDED`、`FAILED`。
- Java 侧 `ReportRecord`、`ReportDO`、`ReportPersistenceAssembler`、`ReportRepositoryImpl` 与对应查询结果模型同步更新。
- 新系统按最终建表口径创建 `operations_report`，不保留“先运行时传递、后补入库”的过渡方案。

## 关联文件

### Operations

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/pom.xml`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/pom.xml`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/report/model/entity/ReportRecord.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/report/repository/ReportRepository.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/persistence/dataobject/ReportDO.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/repository/impl/ReportRepositoryImpl.java`

### System

- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/CurrentUserController.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/auth/security/CurrentUserResolver.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/controller/AuditController.java`

### Workers / Storage

- [`WORKERS-RENDER-INTERFACE.md`](../20-interfaces/WORKERS-RENDER-INTERFACE.md)
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/common/client/WorkerRenderClient.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/common/client/dto/WorkerRenderDtos.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/helper/StorageUploadStreamHelper.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/StorageApplicationService.java`

### 关联域统计

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/KnowledgeTaxonomyController.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/result/TagGovernanceMetricsResult.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/DiscoverySearchAdminController.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/DiscoveryQaAdminController.java`

## 任务拆分

说明：

- 每个执行任务控制在 `2-6` 个主改动文件。
- 一个任务只表达一个主动作。
- 测试文件与实现文件同任务收口。

### T1 统一 Summary 统计规格

- 任务目标：把 Classics、AI、Discovery、Knowledge 面向 Operations 的 summary 统计字段、时间范围和 bucket 口径固定到文档。
- 文件数：5
- 文件清单：
  - `docs/30-designs/RUNBOOK-OPERATIONS-REPORT-CLOSURE.md`
  - `docs/10-requirements/OPERATIONS-REQUIREMENTS.md`
  - `docs/10-requirements/CLASSICS-REQUIREMENTS.md`
  - `docs/10-requirements/AI-REQUIREMENTS.md`
  - `docs/10-requirements/DISCOVERY-REQUIREMENTS.md`
- 验收点：报表所需统计字段、时间范围、排行/趋势口径在文档中统一，关联域实现不再各自发挥。

### T2 Operations 报表 application 入口骨架

- 任务目标：建立 Operations 报表 application 主入口与 command/query/result 骨架。
- 文件数：6
- 文件清单：
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/service/ReportApplicationService.java`
  - `.../service/impl/ReportApplicationServiceImpl.java`
  - `.../command/OperationsReportGenerateCommand.java`
  - `.../query/OperationsReportPageQuery.java`
  - `.../query/OperationsReportDetailQuery.java`
  - `.../result/OperationsReportGenerateResult.java`
- 验收点：Operations application 模块具备可编译的“发起任务、分页、详情”入口定义，`generate` 返回 `reportId`。

### T3 `operations_report` 字段补齐

- 任务目标：把已确认的 report 台账字段落实到 domain/infra 模型。
- 文件数：4
- 文件清单：
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/report/model/entity/ReportRecord.java`
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/persistence/dataobject/ReportDO.java`
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/persistence/assembler/ReportPersistenceAssembler.java`
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/repository/impl/ReportRepositoryImpl.java`
- 验收点：`requestId`、`traceId`、`templateVersion`、`artifactFilename` 和最终 `reportStatus` 状态集进入 Java 持久化模型。

### T4 Report 查询能力补齐

- 任务目标：让 ReportRepository 支持分页和详情读取，满足轮询结果场景。
- 文件数：5
- 文件清单：
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/report/repository/ReportRepository.java`
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/persistence/mapper/ReportMapper.java`
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/repository/impl/ReportRepositoryImpl.java`
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/result/OperationsReportPageResult.java`
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/assembler/OperationsReportAssembler.java`
- 验收点：报表记录支持分页、按 `reportId` 读取详情，并能表达“等待结果/处理中/成功/失败”。

### T5 Operations 报表接口层

- 任务目标：补齐 admin controller、request 和 assembler。
- 文件数：5
- 文件清单：
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/report/controller/OperationsReportController.java`
  - `.../controller/request/OperationsReportGenerateRequest.java`
  - `.../controller/request/OperationsReportPageRequest.java`
  - `.../controller/request/OperationsReportDetailRequest.java`
  - `.../assembler/OperationsReportInterfaceAssembler.java`
- 验收点：存在 `generate`、`page`、`detail` 三个 admin 接口，`generate` 立即返回任务号，权限编码固定为 `operations:report:view` / `operations:report:generate`。

### T6 Operations 独立响应模型

- 任务目标：把已确认的独立 `XxxResponse` 落到具体接口模型，不透传他域响应。
- 文件数：3
- 文件清单：
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/report/controller/response/OperationsReportGenerateResponse.java`
  - `.../controller/response/OperationsReportPageResponse.java`
  - `.../controller/response/OperationsReportDetailResponse.java`
- 验收点：Operations admin 接口输出只使用自己的独立响应模型。

### T7 Operations Worker Client

- 任务目标：补齐 Operations 专属 render worker client 和 DTO。
- 文件数：3
- 文件清单：
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/report/client/OperationsWorkerRenderClient.java`
  - `.../client/dto/OperationsWorkerRenderDtos.java`
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/report/client/OperationsWorkerRenderHttpClient.java`
- 验收点：Operations 可以按 `WORKERS-RENDER-INTERFACE` 契约调用 `/internal/render/operations-report`。

### T8 Operations 快照装配与任务执行单元

- 任务目标：补齐报表快照装配和基于 `reportId` 的后台执行单元。
- 文件数：4
- 文件清单：
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/OperationsReportSnapshotAssembler.java`
  - `.../result/OperationsReportSnapshot.java`
  - `.../result/OperationsReportArtifactResult.java`
  - `.../support/OperationsReportTaskExecutor.java`
- 验收点：后台执行单元能基于 `reportId` 拉起生成、组装快照并消费 worker client。

### T9 Operations Storage 入库与状态回写

- 任务目标：把 worker 产物入 Storage，并把结果回写 report 台账。
- 文件数：3
- 文件清单：
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/service/impl/ReportApplicationServiceImpl.java`
  - `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/helper/StorageUploadStreamHelper.java`
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/result/OperationsReportResult.java`
- 验收点：成功时写 `storageObjectId` 与 `artifactFilename`，失败时写 `failureReason` 与最终状态。

### T10 Classics 报表统计读接口

- 任务目标：给 Operations 暴露最小内容/覆盖率/热门内容统计读结果。
- 文件数：4
- 文件清单：
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/report/service/ClassicsOperationsStatsApplicationService.java`
  - `.../service/impl/ClassicsOperationsStatsApplicationServiceImpl.java`
  - `.../result/ClassicsOperationsStatsResult.java`
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/repository/ClassicsOperationsStatsRepository.java`
- 验收点：Operations 不直接拼 Classics 多处表/接口，统一通过一个应用层读接口获取报表所需统计。

### T11 AI 报表统计读接口

- 任务目标：为 Operations 提供 AI 调用 summary。
- 文件数：3
- 文件清单：
  - `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/report/service/AiOperationsStatsApplicationService.java`
  - `.../service/impl/AiOperationsStatsApplicationServiceImpl.java`
  - `.../result/AiOperationsStatsResult.java`
- 验收点：Operations 可以单次读取 AI 调用量、失败量、平均延迟和总成本。

### T12 Discovery 报表统计读接口

- 任务目标：为 Operations 提供搜索/问答 summary。
- 文件数：3
- 文件清单：
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/report/service/DiscoveryOperationsStatsApplicationService.java`
  - `.../service/impl/DiscoveryOperationsStatsApplicationServiceImpl.java`
  - `.../result/DiscoveryOperationsStatsResult.java`
- 验收点：Operations 不再自己查询 Discovery 日志明细。

### T13 Knowledge 报表统计读接口

- 任务目标：为 Operations 提供标签覆盖率和标签治理 summary。
- 文件数：3
- 文件清单：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/report/service/KnowledgeOperationsStatsApplicationService.java`
  - `.../service/impl/KnowledgeOperationsStatsApplicationServiceImpl.java`
  - `.../result/KnowledgeOperationsStatsResult.java`
- 验收点：Operations 可以按统一规格读取 Knowledge summary。

### T14 Operations 聚合网关

- 任务目标：在 Operations 内聚合多域报表输入，屏蔽多域 service 细节。
- 文件数：2
- 文件清单：
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/OperationsReportMetricsGateway.java`
  - `.../support/DefaultOperationsReportMetricsGateway.java`
- 验收点：Operations 只依赖统一 metrics gateway，不直接耦合多域 service 细节。

### T15 Operations 接口测试

- 任务目标：锁定 controller 路由、权限和响应契约。
- 文件数：2
- 文件清单：
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/report/controller/OperationsReportControllerTest.java`
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/report/controller/OperationsReportContractTest.java`
- 验收点：`generate/page/detail` 路由、权限和响应模型契约可回归。

### T16 Operations 应用与基础设施测试

- 任务目标：锁定 application、worker client、storage 入库和状态流转。
- 文件数：3
- 文件清单：
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/report/service/impl/ReportApplicationServiceImplTest.java`
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/test/java/com/thundax/kuzhambu/operations/infra/report/client/OperationsWorkerRenderHttpClientTest.java`
  - `kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/test/java/com/thundax/kuzhambu/operations/infra/report/repository/impl/ReportRepositoryImplTest.java`
- 验收点：发起任务、完成回写、失败回写和 worker 协议解析有最小自动化保障。

### T17 关联域统计测试

- 任务目标：锁定 Classics、AI、Discovery、Knowledge 的 summary 读接口契约。
- 文件数：4
- 文件清单：
  - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/report/service/impl/ClassicsOperationsStatsApplicationServiceImplTest.java`
  - `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/report/service/impl/AiOperationsStatsApplicationServiceImplTest.java`
  - `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/report/service/impl/DiscoveryOperationsStatsApplicationServiceImplTest.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/report/service/impl/KnowledgeOperationsStatsApplicationServiceImplTest.java`
- 验收点：关联域 summary 统计口径被测试固定。

## 执行顺序

建议固定顺序：

1. `T1`
2. `T2`
3. `T3`
4. `T4`
5. `T10`
6. `T11`
7. `T12`
8. `T13`
9. `T14`
10. `T7`
11. `T8`
12. `T9`
13. `T5`
14. `T6`
15. `T15`
16. `T16`
17. `T17`

原因：

- 先锁定文档规格和 `operations_report` 最终字段，避免后续返工。
- 再把 Operations 自己的 application、repository、response 契约搭起来。
- 然后逐域补 summary 读接口，再接入统一聚合网关。
- 最后接 worker、后台执行单元、Storage 入库和接口测试，形成完整闭环。

## 验证策略

本轮至少执行：

```sh
cd kuzhambu-servers
mvn -pl biz/operations/kuzhambu-operations-application -am spotless:apply
mvn -pl biz/operations/kuzhambu-operations-interface -am spotless:apply
mvn -pl biz/operations/kuzhambu-operations-infra -am spotless:apply
mvn -pl biz/operations/kuzhambu-operations-interface -am test
```

如果关联域同时改动，还需追加对应模块最小测试：

```sh
cd kuzhambu-servers
mvn -pl biz/classics/kuzhambu-classics-application -am test
mvn -pl biz/ai/kuzhambu-ai-application -am test
mvn -pl biz/discovery/kuzhambu-discovery-application -am test
mvn -pl biz/knowledge/kuzhambu-knowledge-application -am test
```

## 风险与决策点

### 风险

- Classics / AI / Discovery 的 summary 统计口径如果不先统一，Operations 很容易变成“拼装域”。
- 如果 Operations 不维持自己的独立 `XxxResponse`，而是透传他域输出模型，后续字段演化和权限裁剪会非常脆弱。

## 已确认交付策略

- 首版不做 SSE 进度。
- `generate` 只负责发起任务并返回 `reportId`。
- 前端或调用方通过 `detail` / `page` 轮询任务结果。
- 只有当 `reportStatus=SUCCEEDED` 且 `storageObjectId` 已落库后，才开放下载。
- admin 权限编码固定为：
  - `operations:report:view`
  - `operations:report:generate`
- summary 统计趋势序列时间 bucket 固定为：
  - 周报按日
  - 月报按周

## 收口要求

- 任务关闭时删除本 RUNBOOK。
- 如果实现过程中发现需求口径需要收窄或补充，优先更新需求/设计/接口文档，再调整实现。
