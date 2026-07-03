# RUNBOOK Classics AI Workers Closure

## Purpose

本 RUNBOOK 用于关闭 Classics + AI + Workers 当前明确的 3 个未完成或部分完成项：

1. Java 侧接入 `CLASSICS_SANCAI_TRANSLATE_BATCH_ITEM`。
2. 补齐 Wangqi / Ming Customs 导出产物生成与下载闭环。
3. 补齐 Classics 批量操作结果模型，覆盖批量公开/私有状态修改与批量分享创建。

本 RUNBOOK 是阶段性执行计划。任务完成并进入 PR 收口后，应删除本文档。

## Product Boundary

- Portal 不新增批量分享流程。
- 批量分享只负责在后台创建或更新分享记录、分享目标快照和状态。
- Portal 继续通过现有分享读取接口按 `share_token` 和记录状态读取内容。
- Admin Web 必须覆盖批量操作入口和 service contract。
- Portal Web 必须覆盖分享读取回归，确认批量分享生成的记录仍按现有状态语义展示或拒绝访问。
- 导出闭环复用现有 Render Worker + Storage 模式，不新增导出存储系统。
- AI 批量翻译只补 Java resolver 到现有 Worker usecase 的连通性，不扩展新的 AI 能力。

## Source Documents

- `docs/10-requirements/CLASSICS-REQUIREMENTS.md`
- `docs/10-requirements/AI-REQUIREMENTS.md`
- `docs/10-requirements/WORKERS-REQUIREMENTS.md`
- `docs/20-interfaces/WORKERS-AI-INTERFACE.md`
- `docs/20-interfaces/WORKERS-RENDER-INTERFACE.md`
- `docs/30-designs/CLASSICS-DESIGN.md`
- `docs/30-designs/AI-DESIGN.md`
- `docs/30-designs/WORKERS-DESIGN.md`
- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
- `docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`
- `docs/40-readiness/WORKERS-IMPLEMENTATION-COVERAGE.md`

## Exit Criteria

- `CLASSICS_SANCAI_TRANSLATE_BATCH_ITEM` 可以从 Java resolver 解析到 Worker path `/internal/ai/classics/sancai/translate-batch-item`。
- Wangqi export job 可以生成可下载 Storage artifact。
- Ming Customs export job 可以生成可下载 Storage artifact。
- 批量公开/私有状态修改返回成功数、失败数和每条失败原因。
- 批量分享创建返回成功数、失败数和每条失败原因。
- 批量分享创建出的记录可以继续被现有分享状态接口管理，并被现有 Portal 分享读取接口消费。
- 相关 coverage 文档只在代码和测试通过后更新。

## Data Structure Changes

### Database

本次默认不新增数据库字段。

复用 `db/schema/classics.sql` 里的现有表和字段：

- `classics_content_export_job`
  - `id`
  - `export_kind`
  - `content_type`
  - `export_format`
  - `scope_type`
  - `scope_json`
  - `requested_at`
  - `expires_at`
  - `status`
  - `storage_object_id`
  - `item_count`
  - `asset_count`
  - `visibility_risk_status`
  - `content_changed`
- `classics_share_link`
  - `id`
  - `share_token`
  - `token_hash`
  - `title`
  - `visibility`
  - `status`
  - `visibility_risk_status`
  - `issued_at`
  - `expires_at`
  - `access_count`
- `classics_share_target`
  - `id`
  - `share_link_id`
  - `content_type`
  - `content_id`
  - `content_version_id`
  - `content_version_no`
  - `title_snapshot`
  - `content_snapshot_json`
  - `content_visibility_snapshot`
  - `target_status`
  - `priority`
- `classics_share_access_record`
  - `id`
  - `share_link_id`
  - `share_target_id`
  - `accessed_at`
  - `access_result`
  - `client_snapshot`

如果实现过程中确实需要 schema 变更，必须先更新本节，精确列出表名、字段名、类型、默认值和迁移策略后再改 SQL。

### AI Usecase Mapping

在 `ClassicsAiWorkerUsecaseResolver` 中新增映射：

- source type: `SANCAI_ENTRY`
- capability key: `translate_batch_item`
- operation: `CLASSICS_SANCAI_TRANSLATE_BATCH_ITEM`
- worker path: `/internal/ai/classics/sancai/translate-batch-item`

Worker 侧现有文件保持不变，除非 Java 测试发现 registry contract 不一致：

- `kuzhambu-workers/src/kuzhambu_workers/ai/usecase_registry.py`
- `kuzhambu-workers/src/kuzhambu_workers/core/service_paths.py`

### Export Scope Payload

`classics_content_export_job.scope_json` 必须存储可直接交给 Render Worker 的导出快照。Wangqi 与 Ming Customs 使用同一结构：

```json
{
  "title": "string",
  "contentType": "WANGQI_DOCUMENT | MING_CUSTOMS",
  "scopeType": "SELECTED_ITEMS | FILTERED_RESULT",
  "items": [
    {
      "id": 1,
      "title": "string",
      "text": "string",
      "summary": "string",
      "visibility": "PUBLIC | PRIVATE",
      "category": "string",
      "documentTime": "string",
      "sourceFileStorageObjectId": 1
    }
  ]
}
```

字段要求：

- `title`: 导出产物标题，必填。
- `contentType`: 必填，Wangqi 固定 `WANGQI_DOCUMENT`，Ming Customs 固定 `MING_CUSTOMS`。
- `scopeType`: 必填，选中项导出用 `SELECTED_ITEMS`，筛选结果导出用 `FILTERED_RESULT`。
- `items`: 必填，不允许为空。
- `items[].id`: 内容 ID，必填。
- `items[].title`: 内容标题，必填。
- `items[].text`: 导出正文，必填。
- `items[].summary`: 可选，JSON/HTML/ZIP 可保留。
- `items[].visibility`: 可选，用于人工核对风险。
- `items[].category`: 可选，Wangqi/Ming 可按现有领域字段填充。
- `items[].documentTime`: 可选，Wangqi 文献时间。
- `items[].sourceFileStorageObjectId`: 可选，Wangqi 原始文件引用。

Render Worker 当前 CSV/HTML 至少消费 `id`、`title`、`text`；JSON/ZIP 保留完整 payload。

### Batch Operation Result

新增或复用 Java 应用层结果模型时，字段必须保持如下语义：

`ClassicsBatchOperationResult`

- `successCount`: `int`，成功条数。
- `failureCount`: `int`，失败条数。
- `successes`: `List<ClassicsBatchOperationItemResult>`，成功明细。
- `failures`: `List<ClassicsBatchOperationItemResult>`，失败明细。

`ClassicsBatchOperationItemResult`

- `contentType`: `String`，内容类型。
- `contentId`: `Long`，内容 ID。
- `resultId`: `Long`，成功时的结果 ID；批量分享为 `share_link.id`，批量改状态为内容 ID。
- `status`: `String`，成功或失败后的状态。
- `failureCode`: `String`，失败码；成功时为空。
- `failureReason`: `String`，失败原因；成功时为空。

### Batch Visibility Request

Java 接口层新增批量状态修改请求时，字段必须为：

`ClassicsBatchVisibilityRequest`

- `contentType`: `String`，允许 `SANCAI_ENTRY`、`WANGQI_DOCUMENT`、`MING_CUSTOMS`。
- `contentIds`: `List<Long>`，目标内容 ID，去重后不能为空。
- `visibility`: `String`，目标状态，允许现有领域枚举中的公开/私有值。

### Batch Share Request

Java 接口层新增批量分享请求时，字段必须为：

`ClassicsBatchShareCreateRequest`

- `titlePrefix`: `String`，分享标题前缀；为空时使用内容快照标题。
- `visibility`: `String`，分享可见性，允许现有 `PUBLIC`、`PRIVATE`。
- `status`: `String`，创建后的分享状态，默认 `ACTIVE`。
- `visibilityRiskStatus`: `String`，沿用现有风险状态。
- `expiresAt`: `Date`，可选，沿用现有分享过期语义。
- `privateContentConfirmed`: `boolean`，请求中存在私有内容时必须为 `true`。
- `targets`: `List<ClassicsShareTargetRequest>`，每个目标创建一条 share link。

`targets[]` 复用现有字段：

- `contentType`: `String`
- `contentId`: `Long`

批量分享规则：

- 每个 target 创建一条 `classics_share_link` 和一条 `classics_share_target`。
- 单条失败不回滚其他成功项。
- 请求级校验失败时不执行任何 item。
- 重复 target 必须确定性处理：要么请求级拒绝，要么作为 item failure 返回，不允许静默忽略。

### Admin Web Batch Types

`kuzhambu-apps/admin-web/src/pages/classics/common/classics-share-types.ts` 新增字段必须与 Java 接口一致：

`ClassicsBatchShareCreateCommand`

- `titlePrefix?: string`
- `visibility: ClassicsShareVisibility`
- `status?: ClassicsShareLinkStatus`
- `visibilityRiskStatus?: string`
- `expiresAt?: string`
- `privateContentConfirmed: boolean`
- `targets: ClassicsShareTargetRef[]`

`ClassicsBatchOperationItemResult`

- `contentType: ClassicsShareContentType`
- `contentId: number`
- `resultId?: number`
- `status?: string`
- `failureCode?: string`
- `failureReason?: string`

`ClassicsBatchOperationResult`

- `successCount: number`
- `failureCount: number`
- `successes: ClassicsBatchOperationItemResult[]`
- `failures: ClassicsBatchOperationItemResult[]`

### Portal Web Share Contract

Portal Web 不新增独立批量数据结构。下列现有文件只允许做兼容性测试或字段透传：

- `kuzhambu-apps/portal-web/src/pages/share/share-types.ts`
- `kuzhambu-apps/portal-web/src/pages/share/share-service.ts`

如果后端分享读取 response 字段未变化，Portal Web 类型不得新增字段。

## Task Breakdown

### Task A: AI Batch Translate Resolver

Files:

- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/support/ClassicsAiWorkerUsecaseResolver.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/refinement/support/ClassicsAiWorkerUsecaseResolverTest.java`
- `docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`

Changes:

- 在 `SANCAI_ENTRY` 映射里新增 `translate_batch_item`。
- 测试 resolver 解析出的 operation 和 path。
- 代码验证通过后，将 coverage 中对应项从未完成改为已完成。

Checks:

```sh
cd kuzhambu-servers
mvn -pl biz/ai/kuzhambu-ai-application -am spotless:apply
mvn -pl biz/ai/kuzhambu-ai-application -am spotless:check
mvn -pl biz/ai/kuzhambu-ai-application -am checkstyle:check
mvn -pl biz/ai/kuzhambu-ai-application -am test
```

### Task B1: Export Payload Backend Closure

Files:

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/repository/ClassicsContentRepository.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/repository/impl/ClassicsContentRepositoryImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceImplTest.java`

Changes:

- 确认 `scope_json` 进入 `renderPayloadJson` 前已经符合 Export Scope Payload。
- 若 repository 已能读取生成 payload 所需字段，不改 repository；若缺字段，只在上述 repository 文件补查询方法。
- 覆盖 Wangqi 与 Ming Customs 的 `item_count`、`asset_count`、`storage_object_id` 完成态。
- 测试 task create 到 Worker render request 的 payload 字段。

Checks:

```sh
cd kuzhambu-servers
mvn -pl biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-domain,biz/classics/kuzhambu-classics-infra -am spotless:apply
mvn -pl biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-domain,biz/classics/kuzhambu-classics-infra -am spotless:check
mvn -pl biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-domain,biz/classics/kuzhambu-classics-infra -am checkstyle:check
mvn -pl biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-domain,biz/classics/kuzhambu-classics-infra -am test
```

### Task B2: Export Admin Web Closure

Files:

- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-export-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-export-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-export-service-contract.test.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.tsx`

Changes:

- Wangqi 导出请求写入 `contentType: "WANGQI_DOCUMENT"`。
- Ming Customs 导出请求写入 `contentType: "MING_CUSTOMS"`。
- 两类导出请求都写入符合 Export Scope Payload 的 `scopeJson`。
- `ClassicsExportJobRecord` 保留 `downloadUrl`、`itemCount`、`assetCount`、`expiresAt`。
- contract test 固定 create/page/content download 字段。

Checks:

```sh
cd kuzhambu-apps
npm --workspace admin-web run format
npm run format:check
npm run lint
npm --workspace admin-web run test
```

### Task B3: Worker Export Regression

Files:

- `kuzhambu-workers/src/kuzhambu_workers/render/classics_export.py`
- `kuzhambu-workers/tests/test_classics_export.py`
- `kuzhambu-workers/tests/test_render_routes.py`
- `docs/40-readiness/WORKERS-IMPLEMENTATION-COVERAGE.md`

Changes:

- 默认不改 Worker。
- 如果 Wangqi/Ming payload 发现 Worker 兼容性问题，只在 `classics_export.py` 做向后兼容处理。
- 测试 CSV/JSON/HTML/ZIP 至少保留 `id`、`title`、`text`。
- 只有 Worker 代码或 contract 变更时才更新 Workers coverage。

Checks:

```sh
cd kuzhambu-workers
.venv/bin/python -m ruff format .
.venv/bin/python -m ruff format --check .
.venv/bin/python -m ruff check .
.venv/bin/python -m pytest -p no:capture tests/test_classics_export.py tests/test_render_routes.py
```

### Task C1: Shared Batch Result Model

Files:

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/result/ClassicsBatchOperationResult.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/result/ClassicsBatchOperationItemResult.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/common/response/ClassicsBatchOperationResponse.java`

Changes:

- 新增应用层 batch result 与 item result。
- 新增接口层 response，字段与 Batch Operation Result 一一对应。
- response 只做字段映射，不放业务判断。

Checks:

```sh
cd kuzhambu-servers
mvn -pl biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-interface -am spotless:apply
mvn -pl biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-interface -am spotless:check
mvn -pl biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-interface -am checkstyle:check
```

### Task C2a: Batch Visibility Shared Interface

Files:

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/request/ClassicsBatchVisibilityRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/ClassicsContentAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/content/ClassicsContentAdminControllerTest.java`

Changes:

- 请求级校验包含 `contentType`、`contentIds`、`visibility`。
- 接口新增批量 visibility endpoint，返回 `ClassicsBatchOperationResponse`。

### Task C2b: Sancai Batch Visibility

Files:

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/SancaiApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/SancaiApplicationServiceImplTest.java`

Changes:

- 批量方法逐条调用现有 `changeEntryVisibility` 语义。
- 保留 version 更新、content updated time 和 search sync。
- 单条失败写入 `ClassicsBatchOperationResult.failures`，不影响其他条。

### Task C2c: Wangqi Batch Visibility

Files:

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/service/WangqiDocumentApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/service/impl/WangqiDocumentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/wangqi/WangqiDocumentApplicationServiceImplTest.java`

Changes:

- 批量方法逐条调用现有 `changeVisibility` 语义。
- 保留 version 更新和 search sync。
- 单条失败写入 `ClassicsBatchOperationResult.failures`，不影响其他条。

### Task C2d: Ming Customs Batch Visibility

Files:

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/service/MingCustomsApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/service/impl/MingCustomsApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/mingcustoms/MingCustomsApplicationServiceImplTest.java`

Changes:

- 批量方法逐条调用现有 `changeVisibility` 语义。
- 保留 version 更新和 search sync。
- 单条失败写入 `ClassicsBatchOperationResult.failures`，不影响其他条。

### Task C3: Batch Share Backend

Files:

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/command/BatchShareCreateCommand.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/service/ClassicsSharingApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/service/impl/ClassicsSharingApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sharing/ClassicsSharingApplicationServiceImplTest.java`

Changes:

- 新增 `BatchShareCreateCommand`，字段按 Batch Share Request。
- service 新增 `batchCreateLinks(BatchShareCreateCommand command)`。
- 每个 target 调用现有分享创建与 snapshot 绑定逻辑。
- 存在私有内容且 `privateContentConfirmed=false` 时请求级拒绝。
- duplicate target 使用 deterministic item failure 或请求级拒绝，测试必须固定预期。

### Task C4: Batch Share Interface

Files:

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/controller/request/ClassicsBatchShareCreateRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/assembler/ClassicsSharingInterfaceAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/controller/ClassicsSharingAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/ClassicsSharingAdminControllerTest.java`

Changes:

- 新增批量分享 request。
- 新增 endpoint：`POST /api/classics/shares/batch-create`。
- response 使用 `ClassicsBatchOperationResponse`。
- 不新增 Portal controller 或 Portal endpoint。

### Task C5a: Batch Share Admin Web Contract

Files:

- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-share-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-share-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-share-service-contract.test.ts`

Changes:

- `classics-share-types.ts` 新增 batch request/result TS 类型，字段与 Java request/response 一致。
- `classics-share-service.ts` 新增 `batchCreate`，路径固定 `/classics/shares/batch-create`。
- contract test 固定 request/response 字段。
- Admin Web 必须能从批量结果看到 `successCount`、`failureCount`、`successes[]`、`failures[]`。

### Task C5b: Sancai Admin Batch Share Entry

Files:

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.test.tsx`

Changes:

- 页面只接入已有选择态；如果没有稳定选择态，先不新增临时 UI。
- 调用 `classics-share-service.batchCreate`。
- 批量结果展示 `successCount`、`failureCount` 和失败原因。

### Task C5c: Wangqi Admin Batch Share Entry

Files:

- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.test.tsx`

Changes:

- 页面只接入已有选择态；如果没有稳定选择态，先不新增临时 UI。
- 调用 `classics-share-service.batchCreate`。
- 批量结果展示 `successCount`、`failureCount` 和失败原因。

### Task C5d: Ming Customs Admin Batch Share Entry

Files:

- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.test.tsx`

Changes:

- Admin Web 页面只接入已有选择态；如果某页面没有稳定选择态，先只交付 service contract，不临时发明大 UI。
- 调用 `classics-share-service.batchCreate`。
- 批量结果展示 `successCount`、`failureCount` 和失败原因。

### Task C5e: Sharing Admin Page Regression

Files:

- `kuzhambu-apps/admin-web/src/pages/classics/sharing/sharing-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sharing/sharing-page.test.tsx`

Changes:

- 确认批量创建出的 share link 仍可在现有分享管理页展示。
- 继续使用现有 status update 行为管理 `ACTIVE`、`EXPIRED`、`REVOKED`。

### Task C6: Portal Web Share Regression

Files:

- `kuzhambu-apps/portal-web/src/pages/share/share-service.ts`
- `kuzhambu-apps/portal-web/src/pages/share/share-types.ts`
- `kuzhambu-apps/portal-web/src/pages/share/share-service.test.ts`
- `kuzhambu-apps/portal-web/src/pages/share/share-page.tsx`
- `kuzhambu-apps/portal-web/src/pages/share/share-form.tsx`

Changes:

- 默认不新增 Portal endpoint、不新增批量分享入口。
- 确认 Portal Web 仍通过现有 share token 读取接口消费分享记录。
- `share-service.test.ts` 增加或保留状态回归：`ACTIVE` 可读，`EXPIRED` / `REVOKED` 按现有错误语义处理。
- 如果后端 response 字段不变，不改 `share-types.ts`、`share-page.tsx`、`share-form.tsx`，只补测试。
- 如果批量分享导致 Portal 必须展示多 target 记录，字段必须仍来自现有 response，不新增独立 Portal 数据结构。

Checks:

```sh
cd kuzhambu-apps
npm --workspace portal-web run format
npm run format:check
npm run lint
npm --workspace portal-web run test
```

### Task C7: Coverage Closure

Files:

- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
- `docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`
- `docs/40-readiness/WORKERS-IMPLEMENTATION-COVERAGE.md`
- `docs/30-designs/RUNBOOK-CLASSICS-AI-WORKERS-CLOSURE.md`

Changes:

- 只在对应代码和测试通过后更新 coverage。
- 如果三项全部关闭，PR 收口前删除本 RUNBOOK。
- 如果只关闭部分项，在 coverage 中写明剩余项和阻塞原因。

## Verification Plan

Backend full check before PR:

```sh
cd kuzhambu-servers
mvn spotless:check
mvn checkstyle:check
mvn test
```

Admin Web full check before PR:

```sh
cd kuzhambu-apps
npm run format:check
npm run lint
npm run build
npm run test
```

Portal Web focused check before PR:

```sh
cd kuzhambu-apps
npm --workspace portal-web run test
```

Workers full check before PR:

```sh
cd kuzhambu-workers
.venv/bin/python -m ruff format --check .
.venv/bin/python -m ruff check .
.venv/bin/python -m pytest -p no:capture
```

## Risks

- 批量分享不要扩展 Portal 产品边界；本分支只创建分享记录、目标快照和状态。
- 导出闭环必须复用现有 Render Worker + Storage 模式，避免三类 Classics 内容出现三套导出路径。
- 批量状态修改必须复用单条状态修改语义，避免绕过版本更新和 search sync。
- AI batch translate item 只补 resolver，不改变 Worker registry 和 candidate final-state 语义。
