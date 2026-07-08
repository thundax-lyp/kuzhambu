# Classics 静态展示列表与回源闭环 RUNBOOK

## 目标

完成三才图会静态展示 `showcase job` 的目标态闭环：Admin Web 在三才图会页面展示可搜索、可筛选、可生成、可下载的静态展示任务列表；Classics Java servers 负责权限、快照、任务状态、Storage 回源和审计；`sancai-showcase` render worker 只根据快照生成静态 HTML 产物。

本任务只做 `showcase job`。`classics_content_export_job` 继续只承载 CSV、JSON、HTML、ZIP 设定集导出；`classics_sancai_showcase` 只承载三才图会静态展示页面生成、下载和状态追踪。

## 确认结论

- `showcase job` 独立使用 `classics_sancai_showcase`，不复用 `classics_content_export_job`。
- Admin showcase section 放在 `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.tsx` 的三才图会页面内。
- 列表查询支持 `keyword`、`status`、`visibilityRiskStatus`、`requestedAtStart`、`requestedAtEnd`。
- worker payload 使用 Java servers 组装的快照和临时可读资源，不传用户 access token，不传 Storage 永久 URL。
- 默认使用 stream 调用；Java servers 只以 `completed.artifact` 或同步最终响应作为成功事实。
- 包含私有内容时复用 `CLASSICS-REQUIREMENTS.md` 固定确认文案。
- 失败态保存稳定错误类型和可读失败原因，不保存 worker 原始异常、临时路径、签名或私有正文。

## 数据结构变更

### `classics_sancai_showcase`

在现有字段基础上精确补齐闭环字段：

| Column | Type | Nullable | Index | 说明 |
| --- | --- | --- | --- | --- |
| `id` | `bigint` | no | PK, AUTO_INCREMENT | 静态展示任务 ID |
| `requested_at` | `datetime(3)` | no | KEY(`requested_at`) | 创建时间 |
| `completed_at` | `datetime(3)` | yes |  | 成功或失败结束时间 |
| `status` | `varchar(16)` | no | KEY(`status`, `requested_at`) | `REQUESTED`、`PROCESSING`、`COMPLETED`、`FAILED`、`EXPIRED` |
| `scope_json` | `json` | no |  | 生成范围快照，不保存用户 token |
| `scope_title` | `varchar(128)` | yes | KEY(`scope_title`) | 列表搜索展示用范围标题 |
| `storage_object_id` | `bigint` | yes |  | 回源后的 Storage 对象 ID |
| `entry_count` | `int` | no |  | 进入静态展示页的条目数 |
| `asset_count` | `int` | no |  | 进入静态展示页的图片和视觉资产数量 |
| `visibility_risk_status` | `varchar(16)` | no | KEY(`visibility_risk_status`, `requested_at`) | `PUBLIC_ONLY`、`CONTAINS_PRIVATE` |
| `filename` | `varchar(255)` | yes |  | worker 产物文件名 |
| `content_type` | `varchar(128)` | yes |  | 固定期望 `text/html; charset=utf-8` |
| `size_bytes` | `bigint` | yes |  | worker 产物大小 |
| `sha256` | `varchar(80)` | yes |  | worker 产物摘要，格式 `sha256:<hex>` |
| `failure_type` | `varchar(64)` | yes | KEY(`failure_type`) | 稳定失败类型 |
| `failure_message` | `varchar(512)` | yes |  | 前端可读失败原因 |

字段映射固定为：

| DB Column | Java Domain / DO Field | Admin Response Field | Admin Web Field |
| --- | --- | --- | --- |
| `requested_at` | `requestedAt` | `requestedAt` | `requestedAt` |
| `completed_at` | `completedAt` | `completedAt` | `completedAt` |
| `scope_json` | `scopeJson` | `scopeJson` | `scopeJson` |
| `scope_title` | `scopeTitle` | `scopeTitle` | `scopeTitle` |
| `storage_object_id` | `storageObjectId` | `storageObjectId` | `storageObjectId` |
| `entry_count` | `entryCount` | `entryCount` | `entryCount` |
| `asset_count` | `assetCount` | `assetCount` | `assetCount` |
| `visibility_risk_status` | `visibilityRiskStatus` | `visibilityRiskStatus` | `visibilityRiskStatus` |
| `content_type` | `contentType` | `contentType` | `contentType` |
| `size_bytes` | `sizeBytes` | `sizeBytes` | `sizeBytes` |
| `failure_type` | `failureType` | `failureType` | `failureType` |
| `failure_message` | `failureMessage` | `failureMessage` | `failureMessage` |

状态流转固定为：

```text
REQUESTED -> PROCESSING -> COMPLETED
REQUESTED -> PROCESSING -> FAILED
COMPLETED -> EXPIRED
```

不得从 `FAILED` 自动回到 `PROCESSING`；重试必须创建新任务。

## 后端接口

### Admin API

接口继续归属 `SancaiAssetAdminController`：

- `POST /api/classics/sancai/assets/showcases/request`
- `POST /api/classics/sancai/assets/showcases/page`
- `GET /api/classics/sancai/assets/showcases/{id}/content`
- `GET /api/classics/sancai/assets/showcases/{id}/content?download=true`

`showcases/request` 请求字段：

| Field | Type | Required | 说明 |
| --- | --- | --- | --- |
| `scopeJson` | string | yes | 前端选择范围的 JSON 快照；后端必须重新校验并重组 worker payload |
| `scopeTitle` | string | yes | 列表展示和搜索用范围标题 |
| `visibilityRiskStatus` | string | yes | `PUBLIC_ONLY` 或 `CONTAINS_PRIVATE` |
| `privateConfirmed` | boolean | no | 包含私有内容时必须为 `true` |

`showcases/request` 响应字段与 `showcases/page` 单条记录一致。

`showcases/page` 请求字段：

| Field | Type | 说明 |
| --- | --- | --- |
| `pageNo` | number | 页码 |
| `pageSize` | number | 每页数量 |
| `keyword` | string | 匹配任务 ID、`scopeTitle`、`filename` |
| `status` | string | 任务状态 |
| `visibilityRiskStatus` | string | 可见性风险 |
| `requestedAtStart` | string | ISO 时间，闭区间开始 |
| `requestedAtEnd` | string | ISO 时间，闭区间结束 |

`showcases/page` 响应记录字段：

| Field | Type | 说明 |
| --- | --- | --- |
| `id` | number | 任务 ID |
| `requestedAt` | string | 创建时间 |
| `completedAt` | string | 完成或失败时间 |
| `status` | string | 状态 |
| `scopeTitle` | string | 范围摘要 |
| `scopeJson` | string | 范围快照 |
| `storageObjectId` | number | Storage 对象 ID |
| `entryCount` | number | 条目数 |
| `assetCount` | number | 资产数 |
| `visibilityRiskStatus` | string | 可见性风险 |
| `filename` | string | 文件名 |
| `contentType` | string | 内容类型 |
| `sizeBytes` | number | 文件大小 |
| `sha256` | string | 文件摘要 |
| `failureType` | string | 失败类型 |
| `failureMessage` | string | 失败原因 |
| `contentUrl` | string | 预览地址 |
| `downloadUrl` | string | 下载地址 |

下载规则：

- `contentUrl` 只用于浏览器预览，响应 header 使用 inline。
- `downloadUrl` 用于保存文件，响应 header 使用 attachment。
- 非 `COMPLETED` 状态、缺少 `storageObjectId` 或 Storage 内容缺失时返回业务错误，不返回空文件。

### Worker Payload

`POST /internal/render/sancai-showcase/stream` 的 `input.payload` 固定目标结构：

```json
{
  "metadata": {
    "title": "三才图会静态展示",
    "generatedAt": "2026-07-08T10:00:00+08:00",
    "generatedBy": "admin",
    "templateVersion": "sancai-showcase-v1",
    "locale": "zh-CN"
  },
  "scope": {
    "scopeType": "FILTERED_RESULT",
    "scopeTitle": "天地门公开条目",
    "categoryIds": [1],
    "volumeIds": [11],
    "entryIds": [1001, 1002],
    "filters": {
      "keyword": "天地",
      "lifecycleStatus": "PUBLISHED",
      "visibility": "PUBLIC"
    }
  },
  "visibilityRisk": {
    "status": "PUBLIC_ONLY",
    "privateConfirmed": false
  },
  "catalogs": [
    {
      "id": 1,
      "title": "天地",
      "entryCount": 2,
      "imageEntryCount": 1,
      "thumbnailResourceId": "asset-1001-original"
    }
  ],
  "volumes": [
    {
      "id": 11,
      "categoryId": 1,
      "title": "卷一",
      "priority": 1
    }
  ],
  "entries": [
    {
      "id": 1001,
      "categoryId": 1,
      "volumeId": 11,
      "title": "天地",
      "originalText": "原文",
      "translationText": "译文",
      "tags": ["天文"],
      "images": [
        {
          "resourceId": "asset-1001-original",
          "imageType": "ORIGINAL",
          "caption": "原图",
          "currentUsed": false,
          "priority": 1
        }
      ],
      "visualAsset": {
        "resourceId": "asset-1001-current",
        "visualDescription": "视觉描述",
        "currentUsed": true
      }
    }
  ],
  "assets": [
    {
      "resourceId": "asset-1001-original",
      "temporaryUrl": "https://internal-temp-resource",
      "filename": "1001.png",
      "contentType": "image/png",
      "sha256": "sha256:..."
    }
  ],
  "options": {
    "enableSearch": true,
    "enableFilters": true,
    "enableBrowseModeSwitch": true,
    "offlineOpen": true,
    "printable": true
  }
}
```

Workers 只能使用 `input.payload` 和 `assets[].temporaryUrl`。`renderType` 固定为 `SANCAI_SHOWCASE`，`output.format` 固定为 `HTML`。

worker 成功事实固定来自：

- 同步接口：`RenderResponse.status=SUCCEEDED` 且 `artifact` 存在。
- stream 接口：最终 `completed` 事件中 `status=SUCCEEDED` 且 `artifact` 存在。

Java servers 写入 `classics_sancai_showcase` 的文件元数据必须来自 worker artifact：

| Worker Artifact Field | Showcase Column |
| --- | --- |
| `filename` | `filename` |
| `contentType` | `content_type` |
| `sizeBytes` | `size_bytes` |
| `sha256` | `sha256` |

## 小任务拆分

每个小任务控制在 2-5 个生产文件，测试文件不计入该限制。

### 任务 1：补齐 showcase 记录字段和查询条件

生产文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/model/entity/SancaiShowcase.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/dataobject/SancaiShowcaseDO.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/assembler/SancaiAssetPersistenceAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiAssetRepository.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiAssetRepositoryImpl.java`

实现要求：

- `SancaiShowcase` 增加 `completedAt`、`scopeTitle`、`assetCount`、`filename`、`contentType`、`sizeBytes`、`sha256`、`failureType`、`failureMessage`。
- `SancaiShowcaseDO` 字段与 `classics_sancai_showcase` 表一一对应，不新增表外计算字段。
- `SancaiAssetPersistenceAssembler` 必须双向映射新增字段，空值保持空值，不用默认字符串占位。
- `markCompleted(...)` 同时写入 `completedAt`、Storage object id、文件元数据、`entryCount`、`assetCount`。
- `markFailed(...)` 同时写入 `completedAt`、`failureType`、`failureMessage`。
- `pageShowcases(...)` 支持 `keyword`、`status`、`visibilityRiskStatus`、`requestedAtStart`、`requestedAtEnd`，默认 `requested_at desc`。
- `keyword` 匹配规则为：纯数字时同时匹配 `id`，所有输入均模糊匹配 `scope_title` 和 `filename`。
- `requestedAtStart`、`requestedAtEnd` 使用 `requested_at` 闭区间筛选。

输出：

- Repository 可返回包含新增字段的 `PageResult<SancaiShowcase>`。
- Repository 可将成功、失败、过期状态更新限制在单条 showcase 记录。

测试文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/test/java/com/thundax/kuzhambu/classics/infra/sancai/SancaiRepositoryTest.java`

### 任务 2：收口 Java 应用服务回源闭环

生产文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiShowcaseCommand.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/result/SancaiShowcaseJobResult.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/SancaiAssetApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java`

实现要求：

- `SancaiShowcaseCommand` 增加 `scopeTitle`、`privateConfirmed`，创建任务时只接受前端范围描述，不接受前端传入 `storageObjectId`。
- `SancaiShowcaseJobResult` 返回 `showcaseId`、`status`、`storageObjectId`、`filename`、`sizeBytes`、`sha256`、`failureType`、`failureMessage`。
- `requestShowcase(...)` 先创建 `REQUESTED` 记录，再进入 `PROCESSING`，再调用 `/internal/render/sancai-showcase/stream`。
- Java servers 组装完整 worker payload，默认只包含 `PUBLISHED` 且 `PUBLIC` 的未归档条目。
- `visibilityRiskStatus=CONTAINS_PRIVATE` 时必须要求 `privateConfirmed=true`。
- 只在收到同步最终响应或 stream `completed.artifact` 后下载 worker 临时产物，并调用 Storage facade 创建对象。
- 摘要、大小、内容类型校验通过后才 `markCompleted(...)`。
- worker、Storage 或写回异常统一 `markFailed(failureType, failureMessage)`。

输出：

- 成功任务落库为 `COMPLETED`，拥有 `storageObjectId`、`filename`、`contentType`、`sizeBytes`、`sha256`。
- 失败任务落库为 `FAILED`，拥有 `failureType`、`failureMessage`，没有新的 `storageObjectId`。
- 任务生成过程不调用 workers AI 接口。

测试文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/SancaiAssetApplicationServiceImplTest.java`

### 任务 3：补齐 Admin API 契约

生产文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/SancaiAssetAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiAssetRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiAssetResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiAssetInterfaceAssembler.java`

实现要求：

- `SancaiAssetRequest` 增加 showcase page 查询字段：`keyword`、`status`、`visibilityRiskStatus`、`requestedAtStart`、`requestedAtEnd`。
- `SancaiAssetRequest` 增加 showcase create 字段：`scopeJson`、`scopeTitle`、`visibilityRiskStatus`、`privateConfirmed`。
- `SancaiAssetResponse` 增加响应字段：`completedAt`、`scopeTitle`、`assetCount`、`filename`、`contentType`、`sizeBytes`、`sha256`、`failureType`、`failureMessage`、`contentUrl`、`downloadUrl`。
- `requestShowcase` 权限使用 `classics:sancai:edit`；`pageShowcases` 和 `downloadShowcaseContent` 权限使用 `classics:sancai:view`。
- 下载接口只允许 `COMPLETED` 且存在 `storageObjectId` 的任务返回内容。

输出：

- `SancaiAssetAdminController` 的 showcase 方法只调用 `SancaiAssetApplicationService`，不直接操作 Storage facade 或 worker client。
- `SancaiAssetInterfaceAssembler` 负责 request 到 command、entity 到 response、content/download URL 的转换。
- Controller test 覆盖新增请求字段、响应字段、权限注解和 content disposition。

测试文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/SancaiAssetAdminControllerTest.java`

### 任务 4：收口 worker payload 和静态 HTML 能力

生产文件：

- `kuzhambu-workers/src/kuzhambu_workers/schemas/render.py`
- `kuzhambu-workers/src/kuzhambu_workers/render/sancai_showcase.py`
- `kuzhambu-workers/src/kuzhambu_workers/render/templates/sancai_showcase.html`
- `docs/20-interfaces/WORKERS-RENDER-INTERFACE.md`

实现要求：

- `sancai_showcase.py` 读取 `catalogs`，兼容旧字段 `catalog` 仅作为测试过渡输入。
- HTML 内置搜索输入、状态筛选、门类/卷导航、条目详情区域和浏览模式切换。
- 图片资源优先使用 `assets[].temporaryUrl` 读取并内联为 data URL；无法读取时显示占位，不中断整页生成。
- `RenderSummary.metadata` 返回 `catalogCount`、`volumeCount`、`assetCount`、`visibilityRiskStatus`。
- `WORKERS-RENDER-INTERFACE.md` 同步固化 `SANCAI_SHOWCASE` payload 字段。

输出：

- 生成产物为单个 HTML 文件，`contentType` 为 `text/html; charset=utf-8`。
- HTML 不依赖公网脚本、样式、字体或图片。
- worker 失败只使用稳定错误类型：`RENDER_INPUT_FAILURE`、`RENDER_TEMPLATE_FAILURE`、`RENDER_OUTPUT_FAILURE` 或 `INTERNAL_FAILURE`。

测试文件：

- `kuzhambu-workers/tests/test_sancai_showcase.py`
- `kuzhambu-workers/tests/test_render_routes.py`
- `kuzhambu-workers/tests/test_worker_e2e_render.py`

### 任务 5：Admin Web showcase section

生产文件：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/components/classics-showcase-job-section.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/common/components/classics-showcase-job-section.css`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.tsx`

前端控件与操作：

- 在三才图会页面新增静态展示 section，位置在 `Splitter` 工作区下方或右侧内容区底部，不影响目录树和条目维护主流程。
- section header 左侧标题为 `静态展示任务`，右侧包含 `生成静态展示` 主按钮和 `刷新` 图标按钮。
- 搜索控件使用 `Input.Search`，可访问名称为 `搜索静态展示任务`，placeholder 为 `搜索任务 ID、范围或文件名`，回车或点击搜索触发查询。
- 状态筛选使用 `Select`，可访问名称为 `静态展示任务状态`，选项为 `全部状态`、`排队中`、`进行中`、`已完成`、`失败`、`已过期`。
- 风险筛选使用 `Select`，可访问名称为 `静态展示可见性风险`，选项为 `全部风险`、`仅公开内容`、`包含私有内容`。
- 时间筛选使用 `DatePicker.RangePicker`，可访问名称为 `静态展示生成时间`。
- 操作按钮包含 `筛选`、`重置`、`刷新`；筛选条件变更后不自动请求，点击 `筛选` 后请求。
- 列表使用现有 `KuzhambuList` 或升级为 `KuzhambuTable`；必须展示任务 ID、状态、范围、条目数、资产数、风险、生成时间、完成时间、失败原因、操作。
- 每行操作：成功任务显示 `预览` 和 `下载`；失败任务显示禁用下载并展示 `failureMessage`；处理中任务显示禁用下载。
- 点击 `生成静态展示` 时按当前三才图会筛选条件构造 `scopeJson` 和 `scopeTitle`。
- 如果当前范围包含私有内容，弹窗正文必须逐字使用：`你正在将平台内私有内容写入静态展示页面。生成后的 HTML 文件将脱离平台登录、权限、撤销和搜索可见性控制；任何获得文件或访问地址的人都可能查看这些内容。此操作不会改变内容在平台内的私有状态。是否继续生成？`
- 确认后调用 `entryService.requestShowcase({ scopeJson, scopeTitle, visibilityRiskStatus, privateConfirmed })`，成功后刷新任务列表。

前端状态与请求：

- `sancai-page.tsx` 维护 `showcaseKeyword`、`showcaseStatus`、`showcaseVisibilityRiskStatus`、`showcaseRequestedAtRange`、`appliedShowcaseQuery`。
- `pageShowcases` query key 固定包含 `["classics", "sancai", "showcases", appliedShowcaseQuery]`。
- 输入搜索框后不立即请求；点击 `筛选` 或 `Input.Search` 搜索按钮才更新 `appliedShowcaseQuery`。
- 点击 `重置` 清空控件值并立刻以空查询刷新列表。
- 点击 `刷新` 使用当前 `appliedShowcaseQuery` 调用 `refetch`，不重置控件。
- 点击 `预览` 使用 `window.open(job.contentUrl, "_blank", "noopener,noreferrer")`。
- 点击 `下载` 使用 `window.open(job.downloadUrl, "_blank", "noopener,noreferrer")`。
- 请求失败时 section 内展示 `Alert`，文案为 `静态展示任务加载失败`。
- 创建成功后展示成功反馈并刷新列表；创建失败保留当前筛选控件值。

输出：

- `SancaiShowcaseRecord` 包含后端响应的全部 showcase 字段。
- `SancaiShowcasePageQuery` 包含 `keyword`、`status`、`visibilityRiskStatus`、`requestedAtStart`、`requestedAtEnd`。
- `ClassicsShowcaseJobSection` 不再从 `classics-export-types.ts` 引入 showcase 类型；类型归属三才图会页面域。

测试文件：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-service-contract.test.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.test.tsx`

## 验收

- Admin Web 可按 `keyword`、状态、风险、生成时间筛选 showcase 任务。
- showcase 列表默认按 `requestedAt` 倒序展示。
- showcase 任务和 export 任务在表、service、接口、前端组件和测试 fixture 上不混用。
- 默认生成范围只包含公开且未归档三才图会条目。
- 包含私有内容时必须确认固定风险文案。
- worker payload 不包含用户 access token，不要求 workers 判断用户权限。
- 未收到 `completed.artifact` 或同步最终响应时不得写入 `storageObjectId`，不得展示可下载状态。
- 成功任务必须展示 `filename`、`sizeBytes`、`sha256`，下载内容来自 Storage。
- 失败任务必须展示 `failureMessage`，且不泄露临时路径、签名、私有正文或底层堆栈。
- 生成的 HTML 可离线打开，支持门类概览、卷列表、条目详情、搜索、筛选、浏览模式切换、响应式布局、浏览器打印和 PDF 生成。

## 验证命令

```sh
cd kuzhambu-servers
mvn -pl biz/classics -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/classics -am test
```

```sh
cd kuzhambu-workers
.venv/bin/python -m ruff format .
.venv/bin/python -m ruff format --check .
.venv/bin/python -m ruff check .
.venv/bin/python -m pytest -p no:capture
```

```sh
cd kuzhambu-apps
pnpm --filter kuzhambu-admin-web run format
pnpm run format:check
pnpm run lint
pnpm run test
```

## 关闭条件

- 5 个小任务全部达到验收。
- `docs/20-interfaces/WORKERS-RENDER-INTERFACE.md` 与最终 worker payload 一致。
- 任务关闭前删除本 RUNBOOK 及残留引用。
