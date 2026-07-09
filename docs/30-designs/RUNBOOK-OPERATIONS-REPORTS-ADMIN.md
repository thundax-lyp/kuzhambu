# Operations 报表管理页 RUNBOOK

## 目标

补齐 Operations 报表管理闭环，让管理员在 `/operations/reports` 完成周报/月报生成、记录查询、详情查看、状态追踪、HTML/PDF 下载和失败原因排查。

## 交付边界

- 必须新增 Operations 报表下载代理接口，admin-web 不直接调用 Storage 内容读取接口。
- 既有报表生成、分页、详情接口和 render worker 不扩展。
- 不改 AI、Classics、Knowledge 页面和接口。
- 不新增第二套路由、请求、权限、状态或样式体系。

## 后端接口与字段

数据库结构变更：

- 不新增表。
- 不新增字段。
- 不新增索引。
- 复用 `operations_report.storage_object_id`、`operations_report.artifact_filename`、`operations_report.report_status` 和 `operations_report.format` 对应的领域/应用层字段。

既有接口：

- `POST /api/operations/report/generate`
- `POST /api/operations/report/page`
- `POST /api/operations/report/detail`

新增接口：

- `GET /api/operations/report/{reportId}/content?download=true`

新增下载接口规则：

- 权限：`operations:report:view`
- `reportId` 来自路径变量，类型为 `Long`
- 校验报表记录存在
- 校验 `reportStatus` 为 `SUCCEEDED`
- 校验 `storageObjectId` 非空
- 通过 `StorageFacade.open(OpenStorageFacadeRequest)` 读取对象内容，不直接访问 Storage repository、mapper、dataobject 或底层表
- `OpenStorageFacadeRequest.storageObjectId` 使用报表记录 `storageObjectId`
- `OpenStorageFacadeRequest.ownerType` 使用现有报表产物上传绑定值 `USER`
- `OpenStorageFacadeRequest.ownerId` 使用现有报表产物上传绑定值 `system`
- 本任务不迁移既有报表产物 Storage owner 归属；迁移为 `OPERATIONS_REPORT/{reportId}` 必须另拆数据兼容任务
- 响应透传 `Content-Type` 和 `Content-Length`
- `Content-Disposition` 文件名优先级：`artifactFilename`、Storage 原文件名、`operations-report-{reportId}.{html|pdf}`
- 报表不存在、未成功或缺少 `storageObjectId` 时返回 Operations 业务错误，不回退为裸 Storage 404
- 下载审计归属 Operations 报表下载，不要求调用方持有 `storage:object:view`

生成请求字段：

| 字段 | 类型 | 必填 | 值 |
| --- | --- | --- | --- |
| `reportType` | string | 是 | `WEEKLY`、`MONTHLY` |
| `format` | string | 是 | `HTML`、`PDF` |
| `periodStart` | Date / JSON string | 是 | 后端 Java `Date`，前端提交 ISO 字符串 |
| `periodEnd` | Date / JSON string | 是 | 后端 Java `Date`，前端提交 ISO 字符串 |

生成响应字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `reportId` | number | 报表记录 ID |
| `reportStatus` | string | 初始状态 |

分页请求字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `reportType` | string/null | 否 | 报表类型 |
| `format` | string/null | 否 | 导出格式 |
| `reportStatus` | string/null | 否 | 报表状态 |
| `requesterUserId` | number/null | 否 | 请求人用户 ID |
| `periodStart` | Date / JSON string / null | 否 | 后端 Java `Date`，前端提交 ISO 字符串 |
| `periodEnd` | Date / JSON string / null | 否 | 后端 Java `Date`，前端提交 ISO 字符串 |
| `pageNo` | number | 否 | 页码 |
| `pageSize` | number | 否 | 每页数量 |

列表记录字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `reportId` | number | 报表记录 ID |
| `reportType` | string/null | `WEEKLY`、`MONTHLY` |
| `format` | string/null | `HTML`、`PDF` |
| `periodStart` | string/null | 统计起始时间 |
| `periodEnd` | string/null | 统计结束时间 |
| `storageObjectId` | number/null | 产物对象 ID，仅内部判断使用 |
| `artifactFilename` | string/null | 产物文件名 |
| `reportStatus` | string/null | `PENDING`、`RUNNING`、`SUCCEEDED`、`FAILED` |
| `failureReason` | string/null | 失败原因 |
| `requesterUserId` | number/null | 请求人用户 ID |
| `requestedAt` | string/null | 请求时间 |
| `completedAt` | string/null | 完成时间 |

详情响应字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `reportId` | number | 报表记录 ID |
| `reportType` | string/null | 报表类型 |
| `format` | string/null | 导出格式 |
| `periodStart` | string/null | 统计起始时间 |
| `periodEnd` | string/null | 统计结束时间 |
| `requestId` | string/null | render 请求 ID |
| `traceId` | string/null | render 链路 ID |
| `templateVersion` | string/null | 模板版本 |
| `storageObjectId` | number/null | 产物对象 ID |
| `artifactFilename` | string/null | 产物文件名 |
| `reportStatus` | string/null | 报表状态 |
| `failureReason` | string/null | 失败原因 |
| `requesterUserId` | number/null | 请求人用户 ID |
| `requestedAt` | string/null | 请求时间 |
| `completedAt` | string/null | 完成时间 |

新增应用层下载结果字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `reportId` | `ReportId` | 报表记录 ID |
| `format` | string/null | 用于文件名后缀兜底 |
| `artifactFilename` | string/null | 报表产物文件名 |
| `contentType` | string/null | Storage 对象 content type |
| `contentLength` | Long/null | Storage 对象 size |
| `storageOriginalFilename` | string/null | Storage 原文件名 |
| `inputStream` | `InputStream` | 下载内容流 |

## 前端页面

页面文件：

- `kuzhambu-apps/admin-web/src/pages/operations/reports/reports-types.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/reports/reports-service.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/reports/reports-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/reports/reports-page.css`
- `kuzhambu-apps/admin-web/src/pages/operations/reports/reports-service-contract.test.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/reports/reports-page.test.tsx`
- `kuzhambu-apps/admin-web/e2e/operations/reports/reports.spec.ts`

路由与菜单文件：

- `kuzhambu-apps/admin-web/src/router/index.tsx`
- `kuzhambu-apps/admin-web/src/layouts/admin-layout.tsx`
- `kuzhambu-apps/admin-web/e2e/layout/admin-layout.spec.ts`
- `kuzhambu-apps/admin-web/e2e/operations/dashboard/dashboard.spec.ts`

权限：

- 页面可见、列表、详情、下载：`operations:report:view`
- 生成按钮：`operations:report:generate`
- 菜单名称：`报表管理`
- 菜单 ID：`operations-report`
- 菜单图标 key：`operations-report`
- 菜单 URL：`/operations/reports`

`reports-types.ts` 数据结构：

```ts
export type OperationsReportType = "WEEKLY" | "MONTHLY";
export type OperationsReportFormat = "HTML" | "PDF";
export type OperationsReportStatus = "PENDING" | "RUNNING" | "SUCCEEDED" | "FAILED";

export interface OperationsReportRecord {
    reportId: number;
    reportType?: string | null;
    format?: string | null;
    periodStart?: string | null;
    periodEnd?: string | null;
    storageObjectId?: number | null;
    artifactFilename?: string | null;
    reportStatus?: string | null;
    failureReason?: string | null;
    requesterUserId?: number | null;
    requestedAt?: string | null;
    completedAt?: string | null;
}

export interface OperationsReportDetailRecord extends OperationsReportRecord {
    requestId?: string | null;
    traceId?: string | null;
    templateVersion?: string | null;
}
```

`reports-service.ts` 数据结构：

```ts
export interface OperationsReportGenerateCommand {
    reportType: OperationsReportType;
    format: OperationsReportFormat;
    periodStart: string;
    periodEnd: string;
}

export interface OperationsReportGenerateResult {
    reportId: number;
    reportStatus?: string | null;
}

export interface OperationsReportPageQuery {
    reportType?: string | null;
    format?: string | null;
    reportStatus?: string | null;
    requesterUserId?: number | null;
    periodStart?: string | null;
    periodEnd?: string | null;
}

export interface OperationsReportDetailCommand {
    reportId: number;
}
```

`reports-service.ts` 方法：

- `generateReport(command: OperationsReportGenerateCommand)`
- `pageReports(query: PageQuery<OperationsReportPageQuery>)`
- `getReportDetail(command: OperationsReportDetailCommand)`
- `toReportDownloadUrl(reportId: number)`

页面布局：

- 页面根节点 class：`operations-reports-page`
- 顺序固定为：标题区、筛选区、生成区、记录列表、详情抽屉

标题区控件：

- 标题文本：`报表管理`
- 描述文本说明周报、月报、HTML/PDF 产物和失败排查
- 主按钮：`生成报表`
- 主按钮无 `operations:report:generate` 时禁用或隐藏

筛选区控件：

- `Select`：报表类型，选项 `全部`、`周报`、`月报`
- `Select`：导出格式，选项 `全部`、`HTML`、`PDF`
- `Select`：状态，选项 `全部`、`等待中`、`生成中`、`已完成`、`失败`
- `InputNumber`：请求人用户 ID
- `RangePicker`：统计周期
- `Button`：`查询`
- `Button`：`重置`

生成区控件：

- `Segmented`：报表类型，选项 `周报`、`月报`
- `Segmented`：导出格式，选项 `HTML`、`PDF`
- 快捷周期按钮：`上周`、`本周`、`上月`、`本月`
- `RangePicker`：统计周期，可覆盖快捷周期
- `Button`：`提交生成`
- 提交成功后展示 `reportId` 和 `reportStatus`，并刷新列表

记录列表控件：

- 表格可访问名称：`报表记录列表`
- 列顺序：报表 ID、报表类型、导出格式、统计周期、状态、文件名、请求人、请求时间、完成时间、操作
- `FAILED` 状态行展示失败原因摘要
- `SUCCEEDED` 且 `storageObjectId` 非空时显示 `下载`
- 非成功状态或无 `storageObjectId` 时不显示下载操作
- 操作列包含：`详情`、`下载`
- 分页位于表格下方

详情抽屉控件：

- 抽屉标题：`报表详情`
- 展示字段：报表 ID、报表类型、导出格式、统计周期、状态、文件名、请求人、请求时间、完成时间、`requestId`、`traceId`、`templateVersion`
- 失败记录展示完整 `failureReason`
- 成功且有 `storageObjectId` 时展示 `下载报表` 按钮

轮询：

- 生成后刷新列表一次
- 列表存在 `PENDING` 或 `RUNNING` 时每 5 秒刷新一次
- 无运行中记录或页面卸载时停止轮询

文案映射：

| 原始值 | 展示 |
| --- | --- |
| `WEEKLY` | 周报 |
| `MONTHLY` | 月报 |
| `HTML` | HTML |
| `PDF` | PDF |
| `PENDING` | 等待中 |
| `RUNNING` | 生成中 |
| `SUCCEEDED` | 已完成 |
| `FAILED` | 失败 |

未识别状态展示原始值，避免吞掉后端新增状态。

## 小任务拆解

### 任务 1：后端下载用例

目标：在 Operations application 层提供报表产物读取用例，完成报表状态校验和 Storage facade 读取。

相关文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/service/ReportApplicationService.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/service/impl/ReportApplicationServiceImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/query/OperationsReportDetailQuery.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/result/OperationsReportDownloadResult.java`

验收：

- `ReportApplicationService` 新增 `download(OperationsReportDetailQuery query)`。
- `OperationsReportDownloadResult` 包含 `reportId`、`format`、`artifactFilename`、`contentType`、`contentLength`、`storageOriginalFilename`、`inputStream`。
- 报表不存在、`reportStatus != SUCCEEDED`、`storageObjectId == null` 时抛出 Operations 业务错误。
- Storage 读取只使用 `StorageFacade`、`OpenStorageFacadeRequest`、`OpenStorageFacadeResponse`。

### 任务 2：后端下载 HTTP 入口

目标：提供 Operations 自有 HTTP 下载接口，把应用层下载结果写回 `HttpServletResponse`。

相关文件：

- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/report/controller/OperationsReportAdminController.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/report/controller/OperationsReportAdminControllerTest.java`

验收：

- `GET /api/operations/report/{reportId}/content?download=true` 存在。
- 方法权限为 `operations:report:view`。
- controller 不注入 Storage 类型，只调用 `ReportApplicationService.download(...)`。
- 响应写回 `Content-Type`、`Content-Length`、`Content-Disposition` 和 `InputStream`。
- 文件名兜底顺序为 `artifactFilename`、`storageOriginalFilename`、`operations-report-{reportId}.{html|pdf}`。

### 任务 3：前端数据类型与 service

目标：建立报表页面类型和接口调用。

相关文件：

- `kuzhambu-apps/admin-web/src/pages/operations/reports/reports-types.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/reports/reports-service.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/reports/reports-service-contract.test.ts`

验收：

- service 方法包含 `generateReport`、`pageReports`、`getReportDetail`、`toReportDownloadUrl`
- `generateReport` 调用 `/operations/report/generate`
- `pageReports` 调用 `/operations/report/page`
- `getReportDetail` 调用 `/operations/report/detail`
- `toReportDownloadUrl` 返回 `/operations/report/{reportId}/content?download=true`

### 任务 4：前端页面与样式

目标：实现 `/operations/reports` 页面完整交互。

相关文件：

- `kuzhambu-apps/admin-web/src/pages/operations/reports/reports-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/reports/reports-page.css`
- `kuzhambu-apps/admin-web/src/pages/operations/reports/reports-page.test.tsx`

验收：

- 页面按标题区、筛选区、生成区、记录列表、详情抽屉渲染
- 筛选控件、生成控件、表格操作和详情抽屉均可通过可访问名称定位
- 无生成权限时不能提交生成
- 失败原因在列表摘要和详情完整展示
- 下载按钮只对成功且有 `storageObjectId` 的记录出现
- 运行中记录触发 5 秒轮询

### 任务 5：路由、菜单与 E2E

目标：让报表管理成为 Operations 菜单下的可访问页面。

相关文件：

- `kuzhambu-apps/admin-web/src/router/index.tsx`
- `kuzhambu-apps/admin-web/src/layouts/admin-layout.tsx`
- `kuzhambu-apps/admin-web/e2e/layout/admin-layout.spec.ts`
- `kuzhambu-apps/admin-web/e2e/operations/reports/reports.spec.ts`
- `kuzhambu-apps/admin-web/e2e/operations/dashboard/dashboard.spec.ts`

验收：

- `/operations/reports` 路由渲染报表管理页
- Operations 菜单下存在“报表管理”
- `admin-layout.tsx` 支持 `operations-report` 菜单图标配置，不产生 `.menu-icon-config-error`
- E2E 覆盖菜单进入、生成、列表刷新、详情、失败原因和下载 URL

## 验证命令

后端下载代理在 `kuzhambu-servers/` 下执行：

```sh
mvn -pl biz/operations/kuzhambu-operations-interface -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/operations/kuzhambu-operations-interface -am test
```

前端在 `kuzhambu-apps/` 下执行：

```sh
pnpm --filter kuzhambu-admin-web run format
pnpm run format:check
pnpm run lint
pnpm run test
pnpm run build
pnpm --filter kuzhambu-admin-web exec playwright test e2e/operations/reports/reports.spec.ts
```

## 最终验收

- `/operations/reports` 不是空白页，受登录和菜单权限保护。
- 管理员可以按控件筛选记录、发起周报/月报生成、查看详情和识别状态。
- `FAILED` 记录展示失败原因。
- `SUCCEEDED` 且有产物的记录通过 Operations 下载入口下载 HTML/PDF。
- 页面下载不要求 `storage:object:view`，不暴露 Storage 内容读取能力。
- 每个小任务改动控制在 2-5 个核心文件内。
- `git diff` 只包含本任务相关文件。
