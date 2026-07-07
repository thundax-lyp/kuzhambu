# RUNBOOK Classics 跨内容批量候选治理闭环

## 目标

把 Classics 剩余核心未完成项“跨内容批量候选治理”推进到可运行、可验证、可收口的已完成状态。

完成后，后台用户可以在三才图会、王圻文档和明代习俗页面，基于当前已选内容打开批量候选治理抽屉，查看这些内容上的 `PENDING` AI 候选，逐条确认 payload，批量应用或批量拒绝。后端按候选逐条校验并返回成功数、失败数和失败明细；单条失败不影响其他候选。

## 已确认约束

- 批量入口放在 Classics，不放在 AI；AI 只保留候选读取和候选状态治理能力。
- 批量应用允许部分成功，复用现有 Classics 批量结果模型。
- 批量范围固定为“当前页面已选择内容对应的候选 ID 集合”，不做跨页长事务、不做后台全量扫描。
- 批量拒绝复用 AI 候选拒绝语义，不直接修改 AI 持久化表。
- 批量应用必须逐条校验 `PENDING`、`contentType`、`contentId`、`objectId`、`capability` 和当前用户 edit 权限。
- Sancai 视觉资产候选必须保留 `objectId`，禁止把视觉资产候选应用到错误资产。
- 三类页面共用一个前端批量候选治理组件。
- 只有实现、测试和页面验证完成后，才能把 `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md` 中该项改为已完成。

## 数据结构变更

### 后端 application 结果字段

文件：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/result/ClassicsBatchOperationItemResult.java`

在现有字段基础上新增：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `candidateId` | `Long` | 候选批量治理必填 | AI 候选 ID |
| `objectId` | `Long` | 可空 | Sancai 视觉资产候选使用；普通内容候选为空 |
| `capability` | `String` | 候选批量治理必填 | `summary`、`tags`、`qa`、`translate`、`image_analysis`、`fusion`、`visual`、`image_gen` |

保留现有字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `contentType` | `String` | `SANCAI_ENTRY`、`WANGQI_DOCUMENT`、`MING_CUSTOMS` |
| `contentId` | `Long` | 内容 ID |
| `resultId` | `Long` | 成功应用时为版本 ID；拒绝成功时可为候选 ID |
| `status` | `String` | 成功后的状态，如 `APPLIED` 或 `REJECTED` |
| `failureCode` | `String` | 稳定失败码 |
| `failureReason` | `String` | 前端展示用失败原因 |

### 后端 interface 响应字段

文件：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/common/response/ClassicsBatchOperationResponse.java`

在 `ClassicsBatchOperationResponse.Item` 新增 JSON 字段：

| JSON 字段 | Java 类型 | 来源 |
| --- | --- | --- |
| `candidateId` | `Long` | `ClassicsBatchOperationItemResult.candidateId` |
| `objectId` | `Long` | `ClassicsBatchOperationItemResult.objectId` |
| `capability` | `String` | `ClassicsBatchOperationItemResult.capability` |

响应整体仍为：

```json
{
  "successCount": 1,
  "failureCount": 1,
  "successes": [
    {
      "candidateId": 7001,
      "contentType": "SANCAI_ENTRY",
      "contentId": 1001,
      "objectId": 9001,
      "capability": "image_analysis",
      "resultId": 30001,
      "status": "APPLIED"
    }
  ],
  "failures": [
    {
      "candidateId": 7002,
      "contentType": "WANGQI_DOCUMENT",
      "contentId": 2001,
      "capability": "summary",
      "failureCode": "PERMISSION_DENIED",
      "failureReason": "当前用户无权编辑该内容"
    }
  ]
}
```

### 后端请求字段

文件：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/request/ClassicsContentRequest.java`

新增内部类 `AiCandidateBatchApplyRequest`：

| JSON 字段 | Java 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `items` | `List<AiCandidateApplyRequest>` | 是 | 待应用候选；每项沿用现有单条应用字段 |

沿用现有 `AiCandidateApplyRequest` 字段：

| JSON 字段 | Java 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `candidateId` | `Long` | 是 | AI 候选 ID |
| `contentType` | `String` | 是 | 内容类型 |
| `contentId` | `Long` | 是 | 内容 ID |
| `capability` | `String` | 是 | 能力类型 |
| `objectId` | `Long` | Sancai 视觉资产候选必填 | 视觉资产或扩展对象 ID |
| `resultFormat` | `String` | 是 | `TEXT`、`STRUCTURED`、`MARKDOWN`、`ARTIFACT` |
| `resultPayload` | `String` | 是 | 用户确认后的候选 payload |
| `changeSummary` | `String` | 否 | 版本变更说明 |

新增内部类 `AiCandidateBatchRejectRequest`：

| JSON 字段 | Java 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `items` | `List<AiCandidateRejectItemRequest>` | 是 | 待拒绝候选 |
| `errorType` | `String` | 否 | 默认 `USER_REJECTED` |
| `errorMessage` | `String` | 否 | 默认 `用户已批量拒绝该 AI 候选` |

新增内部类 `AiCandidateRejectItemRequest`：

| JSON 字段 | Java 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `candidateId` | `Long` | 是 | AI 候选 ID |
| `contentType` | `String` | 是 | 内容类型 |
| `contentId` | `Long` | 是 | 内容 ID |
| `capability` | `String` | 是 | 能力类型 |
| `objectId` | `Long` | 可空 | Sancai 视觉资产候选对象 ID |

### 后端 application command 字段

文件：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/command/AiCandidateBatchApplyContentCommand.java`

新增类字段：

| 字段 | Java 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `items` | `List<AiCandidateApplyContentCommand>` | 是 | 批量应用候选项，沿用现有单条应用 command |

文件：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/command/AiCandidateBatchRejectContentCommand.java`

新增类字段：

| 字段 | Java 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `items` | `List<AiCandidateBatchRejectContentCommand.Item>` | 是 | 批量拒绝候选项 |
| `errorType` | `String` | 否 | 空值时 application 固定使用 `USER_REJECTED` |
| `errorMessage` | `String` | 否 | 空值时 application 固定使用 `用户已批量拒绝该 AI 候选` |

内部类 `Item` 字段：

| 字段 | Java 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `candidateId` | `Long` | 是 | AI 候选 ID |
| `contentType` | `ClassicsContentType` | 是 | 内容类型 |
| `contentId` | `Long` | 是 | 内容 ID |
| `objectId` | `Long` | 可空 | Sancai 视觉资产候选对象 ID |
| `capability` | `String` | 是 | 能力类型 |

### 前端类型字段

文件：`kuzhambu-apps/admin-web/src/pages/classics/common/classics-content-types.ts`

在 `ClassicsBatchOperationItemRecord` 新增：

| 字段 | TypeScript 类型 |
| --- | --- |
| `candidateId?: number \| null` |
| `objectId?: number \| null` |
| `capability?: string \| null` |

新增：

```ts
export interface ClassicsAiCandidateBatchApplyPayload {
    items: AiCandidateApplyCommand[];
}

export interface ClassicsAiCandidateBatchRejectItemPayload {
    candidateId: number;
    contentType: ClassicsContentType;
    contentId: number;
    capability: string;
    objectId?: number | null;
}

export interface ClassicsAiCandidateBatchRejectPayload {
    errorMessage?: string | null;
    errorType?: string | null;
    items: ClassicsAiCandidateBatchRejectItemPayload[];
}
```

同步调整：`AiCandidateApplyCommand` 从 `ai-candidate-service.ts` 移动到 `ai-candidate-types.ts`，`ai-candidate-service.ts` 和 `classics-content-types.ts` 都从 `ai-candidate-types.ts` 引入，避免 service 与 types 互相依赖。

### 前端组件 props

文件：`kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-batch-drawer.tsx`

新增组件 `AiCandidateBatchDrawer`，props 固定为：

| Prop | TypeScript 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `open` | `boolean` | 是 | 是否打开抽屉 |
| `contentType` | `ClassicsContentType` | 是 | 当前页面内容类型 |
| `contentIds` | `number[]` | 是 | 当前页面已选择内容 ID |
| `capabilities` | `AiCandidateCapability[]` | 是 | 当前页面允许治理的能力集合 |
| `contentTitleById` | `Record<number, string>` | 否 | 列表中展示内容标题；缺失时显示内容 ID |
| `canEdit` | `boolean` | 是 | 当前用户是否有对应内容 edit 权限 |
| `onClose` | `() => void` | 是 | 关闭抽屉 |
| `onChanged` | `() => void \| Promise<void>` | 是 | 批量应用或拒绝成功后刷新父级数据 |

组件内部只查询 `status=PENDING` 候选；只展示 `contentIds` 内内容、`capabilities` 内能力的候选。

## 后端任务拆分

### 任务 1：批量结果模型补候选字段

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/result/ClassicsBatchOperationItemResult.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/common/response/ClassicsBatchOperationResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/content/ClassicsContentAdminControllerTest.java`

动作：

- 给 application item result 增加 `candidateId`、`objectId`、`capability`。
- 保留现有 `success(...)` 和 `failure(...)` 工厂方法，新增候选专用工厂方法，避免影响批量分享和批量可见性。
- interface response 的 `Item` 输出新增三个字段。
- 测试锁定新字段 JSON 输出。

验收：

- 现有批量分享、批量可见性测试不需要改调用方即可通过。
- 候选批量响应能输出 `candidateId/objectId/capability`。

### 任务 2：新增 application 批量 command 与服务入口

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/ClassicsContentApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/command/AiCandidateBatchApplyContentCommand.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/command/AiCandidateBatchRejectContentCommand.java`

动作：

- 在 service interface 增加：
  - `ClassicsBatchOperationResult applyAiCandidates(AiCandidateBatchApplyContentCommand command)`
  - `ClassicsBatchOperationResult rejectAiCandidates(AiCandidateBatchRejectContentCommand command)`
- 新增两个 command 类，字段严格按“后端 application command 字段”定义。
- command 构造函数防御空 `items`，application 实现层仍做业务参数校验。

验收：

- service interface 暴露批量方法。
- command 字段、构造函数和 getter 可供 interface assembler 使用。

### 任务 3：Classics application 批量处理实现与测试

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/service/AiCandidateDomainService.java`

动作：

- `applyAiCandidates` 逐条调用现有 `applyAiCandidate`，不复制 `summary/tags/qa/translate/image_analysis/fusion/visual/image_gen` 写回逻辑。
- `rejectAiCandidates` 逐条读取候选并校验归属，然后调用 `AiCandidateDomainService.reject`，不直接写 mapper。
- 每条失败 catch 后转为 `ClassicsBatchOperationItemResult.failure(...)`，继续处理下一条。
- 失败码固定为：
  - `PERMISSION_DENIED`
  - `CANDIDATE_NOT_PENDING`
  - `CANDIDATE_TARGET_MISMATCH`
  - `UNSUPPORTED_CAPABILITY`
  - `CONTENT_NOT_FOUND`
  - `VALIDATION_FAILED`
  - `UNKNOWN_FAILURE`

验收：

- 一批三条候选：一条成功、一条权限失败、一条目标不匹配，返回 `successCount=1/failureCount=2`。
- Sancai `image_analysis/fusion/visual/image_gen` 带 `objectId` 时只应用到匹配视觉资产。
- 非 `PENDING` 候选拒绝进入失败明细，不修改正式内容。

### 任务 4：Classics admin interface 暴露批量接口

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/ClassicsContentAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/request/ClassicsContentRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/assembler/ClassicsContentInterfaceAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/content/ClassicsContentAdminControllerTest.java`

动作：

- 新增接口：
  - `POST /api/classics/content/ai-candidates/batch/apply`
  - `POST /api/classics/content/ai-candidates/batch/reject`
- 两个接口都使用 `@HasPermission("classics:content:edit")`。
- request 中 `items` 必须非空且按 `candidateId` 去重；重复 ID 直接请求参数错误。
- assembler 把 request 映射到 application command。
- response 使用 `ClassicsBatchOperationResponse.from(...)`。

验收：

- controller test 锁定路径、权限描述、请求字段和响应字段。
- 空 `items`、重复 `candidateId`、缺少 `contentType/contentId/capability` 都失败。

## 前端任务拆分

### 任务 5：批量候选 service 与类型

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-content-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-content-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-content-service-contract.test.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/ai-candidate-types.ts`

动作：

- 在 `ClassicsBatchOperationItemRecord` 增加 `candidateId/objectId/capability`。
- 把 `AiCandidateApplyCommand` 移到 `ai-candidate-types.ts`，`ai-candidate-service.ts` 改为引入该类型。
- 增加批量应用 payload 和批量拒绝 payload 类型。
- 新增 service 方法：
  - `applyAiCandidatesBatch(request)`
  - `rejectAiCandidatesBatch(request)`
- 请求路径固定：
  - `/classics/content/ai-candidates/batch/apply`
  - `/classics/content/ai-candidates/batch/reject`
- contract test 锁定请求 body。

验收：

- service contract 能断言两个新增接口路径和 body 完整字段。

### 任务 6：通用批量候选治理抽屉

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-batch-drawer.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-batch-drawer.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-payload-editor.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/common/ai-candidate-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/ai-candidate-types.ts`

控件和操作：

- 抽屉标题：`AI 候选批量治理`。
- 顶部统计条：
  - `已选内容 N 个`
  - `待处理候选 M 个`
  - `已选择候选 K 个`
- 顶部操作按钮：
  - `刷新候选`
  - `批量应用`
  - `批量拒绝`
  - `关闭`
- 候选列表使用表格或列表，必须包含：
  - 多选框
  - 内容标题或内容 ID
  - `contentType`
  - `contentId`
  - `capability`
  - `objectId`
  - `requestedAt`
  - payload 编辑区
  - 单条校验状态
- 候选查询：
  - 对每个 `contentId` 调用 `/ai/invocation/candidate/list`。
  - 请求字段固定为 `contentType`、`contentId`、`status="PENDING"`。
  - 前端再按 `capabilities` 过滤；Sancai 视觉资产候选保留响应内 `objectId`。
- payload 编辑复用 `AiCandidatePayloadEditor`；只有校验通过的候选可以进入批量应用请求。
- `批量应用` 点击后：
  - 未选择候选时提示 `请选择要应用的候选`。
  - 存在 payload 校验失败时提示 `请先修正候选内容`。
  - 组装 `items` 调用 `applyAiCandidatesBatch`。
  - 成功后展示 `批量候选应用结果：成功 X，失败 Y`。
  - 展示失败明细，至少显示 `candidateId`、`capability`、`failureReason`。
  - 刷新候选列表并调用父级 `onChanged`。
- `批量拒绝` 点击后：
  - 未选择候选时提示 `请选择要拒绝的候选`。
  - 弹出确认框，文案：`确认批量拒绝已选择的 AI 候选？拒绝后不会修改正式内容。`
  - 调用 `rejectAiCandidatesBatch`，默认 `errorType=USER_REJECTED`，`errorMessage=用户已批量拒绝该 AI 候选`。
  - 成功后展示 `批量候选拒绝结果：成功 X，失败 Y`。
  - 刷新候选列表并调用父级 `onChanged`。

验收：

- 测试覆盖加载候选、选择候选、payload 校验失败、批量应用成功/失败明细、批量拒绝确认。
- `objectId` 在 Sancai 视觉资产候选行中可见，并随请求提交。

### 任务 7：Sancai 页面接入入口

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`

控件和操作：

- 在条目列表批量操作区新增按钮 `批量候选治理`。
- 按钮位置与 `批量分享`、`批量公开`、`批量私有` 同组。
- 禁用条件：
  - 当前未选择条目。
  - 当前用户无 `classics:sancai:edit`。
- 点击后打开 `AiCandidateBatchDrawer`。
- 传入：
  - `contentType="SANCAI_ENTRY"`
  - `contentIds=selectedEntryIds`
  - `capabilities=["translate","summary","tags","qa","image_analysis","fusion","visual","image_gen"]`
  - `onChanged` 刷新条目列表、当前打开条目详情、视觉资产候选区。

验收：

- 未选择条目时按钮禁用。
- 无 `classics:sancai:edit` 时按钮禁用。
- 选择条目后可打开抽屉并处理候选。

### 任务 8：Wangqi 页面接入入口

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.test.tsx`

控件和操作：

- 在文档列表批量操作区新增按钮 `批量候选治理`。
- 按钮位置与 `批量分享`、`批量公开`、`批量私有` 同组。
- 禁用条件：
  - 当前未选择文档。
  - 当前用户无 `classics:wangqi:edit`。
- 传入：
  - `contentType="WANGQI_DOCUMENT"`
  - `contentIds=selectedDocumentIds`
  - `capabilities=["summary","tags","qa"]`
  - `onChanged` 刷新文档列表、当前打开文档详情、单内容候选面板。

验收：

- 未选择文档时按钮禁用。
- 无 `classics:wangqi:edit` 时按钮禁用。
- 批量拒绝完成后显示 `批量候选拒绝结果：成功 1，失败 1`。

### 任务 9：Ming Customs 页面接入入口

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.test.tsx`

控件和操作：

- 在习俗列表批量操作区新增按钮 `批量候选治理`。
- 按钮位置与 `批量分享`、`批量公开`、`批量私有` 同组。
- 禁用条件：
  - 当前未选择习俗。
  - 当前用户无 `classics:mingcustoms:edit`。
- 传入：
  - `contentType="MING_CUSTOMS"`
  - `contentIds=selectedEntryIds`
  - `capabilities=["summary","tags","qa"]`
  - `onChanged` 刷新习俗列表、当前打开习俗详情、单内容候选面板。

验收：

- 未选择习俗时按钮禁用。
- 无 `classics:mingcustoms:edit` 时按钮禁用。
- 批量应用完成后显示 `批量候选应用结果：成功 1，失败 1`。

## 验证命令

后端：

```sh
cd kuzhambu-servers
mvn -pl biz/classics/kuzhambu-classics-interface,biz/classics/kuzhambu-classics-application,biz/ai/kuzhambu-ai-domain,biz/ai/kuzhambu-ai-application -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/classics/kuzhambu-classics-interface,biz/classics/kuzhambu-classics-application,biz/ai/kuzhambu-ai-domain,biz/ai/kuzhambu-ai-application -am test
```

前端：

```sh
cd kuzhambu-apps
npm --workspace kuzhambu-admin-web run format
npm run format:check
npm run lint
npm --workspace kuzhambu-admin-web run test -- --maxWorkers=1
npm --workspace kuzhambu-admin-web run build
```

页面冒烟：

- 三才图会：选择两个条目，打开 `批量候选治理`，选择一个 `summary` 候选和一个带 `objectId` 的视觉资产候选，点击 `批量应用`，确认成功数、失败数、失败明细和详情刷新。
- 王圻文档：选择两个文档，打开 `批量候选治理`，选择候选后点击 `批量拒绝`，确认正式正文和摘要不变化，候选列表减少。
- 明代习俗：选择两个习俗，打开 `批量候选治理`，混合成功和失败候选，确认失败原因逐条展示。

## 收口

- 检查 `git diff`，只保留跨内容批量候选治理相关改动。
- 将 `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md` 中以下位置改为完成态：
  - Current Baseline 删除“跨内容批量候选治理仍保留在后续项中”，改为“跨内容批量候选治理已完成”。
  - `未完成` 删除“复杂业务闭环仍未完全接通：跨内容批量候选治理仍需后续专项补齐”；若无剩余项，写“无”。
  - Requirement Coverage Matrix 的“AI 生成候选预览、修改、确认和放弃”改为 `已完成`。
  - Follow-up Backlog `B2 AI 候选结果协作设计` 改为 `已完成`。
- 若本 RUNBOOK 已无继续执行价值，在同一收口提交中删除本文件。
- 不在实现未完成、测试未通过或页面未验收时提前改覆盖文档为已完成。
