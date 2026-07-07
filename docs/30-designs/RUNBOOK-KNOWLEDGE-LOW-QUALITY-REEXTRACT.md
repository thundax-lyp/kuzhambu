# Knowledge 低质量门类一键重提取 RUNBOOK

## 目标

把 Knowledge 剩余能力“从质量报告低质量门类一键触发重提取”推进到已完成。

完成态：

- Admin Web `/knowledge/quality-report` 的质量报告来源明细支持按低质量门类一键触发重提取。
- 前端只提交 `reportId`、`sourceCategoryCode`、`taskType` 和必要模型快照，不在浏览器拼装批量来源范围。
- Knowledge 后端从质量报告快照读取同门类来源明细，生成 `selectionScopeJson`，并调用既有图谱抽取应用服务创建 `QUALITY_REPORT` 任务。
- 任务状态、批次、失败原因、候选应用、重生成和批任务取消继续复用 `/knowledge/graph-extraction` 任务台账。
- 候选应用后，正式图谱事实、图谱版本和后续质量报告读取同一条已落地链路。
- `docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md` 中相关剩余项调整为已完成，且不再保留低质量门类一键触发重提取未落地的风险描述。

## 非目标

- 不新增第二套质量报告任务模型。
- 不新增独立重提取状态机。
- 不改 workers Knowledge usecase 契约。
- 不绕过现有 `knowledge_graph_extraction_task`、AI 批任务、候选应用和正式结果落库链路。
- 不在前端直接拼装 `selectionScopeJson.sourceContentIds` 作为最终真相源。

## 完成态交互

### 页面入口

文件：

- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/components/quality-report-source-table.tsx`

控件和操作：

- 在 `QualityReportSourceTable` 的“操作”列保留现有“打开”按钮。
- 新增“重提取”按钮，放在“打开”按钮同一行。
- “重提取”按钮只在以下条件同时满足时启用：
  - 当前报告 `report.reportId` 存在。
  - 当前行 `sourceCategoryCode` 非空。
  - 当前行 `issueCount > 0`。
  - 当前用户具备 `knowledge:graph:edit`。
- 不满足条件时按钮禁用，按钮文案保持“重提取”，禁用原因通过 `Tooltip` 展示：
  - 无报告编号：`缺少报告编号`
  - 无门类编码：`缺少门类编码`
  - 无质量问题：`当前门类无质量问题`
  - 无权限：`缺少图谱编辑权限`
- 点击“重提取”后弹出 `Modal.confirm`：
  - 标题：`重提取低质量门类`
  - 内容展示门类名、门类编码、问题数、图谱版本 ID。
  - 主按钮：`创建重提取任务`
  - 取消按钮：`取消`
- 确认后调用质量报告重提取接口。
- 创建成功后：
  - `message.success` 展示 `重提取任务已创建`
  - 在页面内展示最近创建任务号、任务类型、触发来源、批次号。
  - 提供“打开任务台账”按钮，跳转 `/knowledge/graph-extraction`。
- 创建失败后：
  - `message.error` 展示后端错误消息。
  - 不清空当前报告详情和来源明细。

### 任务类型

- 默认任务类型为 `GRAPH`。
- 本轮不增加任务类型选择控件。
- 后续如果质量报告能区分世系专用问题，再单独增加 `LINEAGE` 选项。

### 权限

- 前端一键重提取按钮使用现有 `knowledge:graph:edit`。
- 后端接口使用 `@HasPermission("knowledge:graph:edit")`。
- 不新增 `knowledge:quality-report:reextract` 权限点，不改菜单种子。

## 数据结构变更

### 数据库

无数据库表结构变更。

### 后端请求

文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/request/QualityReportRequests.java`

新增内部类 `ReextractRequest`：

```java
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public static class ReextractRequest {
    @JsonProperty("reportId")
    private Long reportId;

    @JsonProperty("sourceCategoryCode")
    private String sourceCategoryCode;

    @JsonProperty("taskType")
    private String taskType;

    @JsonProperty("replaceUnconfirmedOnly")
    private Boolean replaceUnconfirmedOnly;

    @JsonProperty("modelId")
    private Long modelId;

    @JsonProperty("modelName")
    private String modelName;

    @JsonProperty("promptMessagesJson")
    private String promptMessagesJson;

    @JsonProperty("inputPayloadJson")
    private String inputPayloadJson;

    @JsonProperty("requestedBy")
    private Long requestedBy;
}
```

字段规则：

- `reportId` 必填，来自当前质量报告 `report.reportId`。
- `sourceCategoryCode` 必填，来自来源明细行 `sourceCategoryCode`。
- `taskType` 可选，缺省为 `GRAPH`；本轮前端固定传 `GRAPH`。
- `replaceUnconfirmedOnly` 可选，缺省为 `true`。
- `modelId`、`modelName`、`promptMessagesJson`、`inputPayloadJson` 沿用抽取任务创建必需快照；本轮前端使用页面常量，不让用户在表格行内编辑。
- `requestedBy` 可选，保持现有调用风格。
- `requestId`、`traceId` 不开放给前端，后端轻编排服务创建抽取任务前生成。

### 后端应用命令

新增文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/command/ReextractLowQualityCategoryCommand.java`

字段：

```java
private Long reportId;
private String sourceCategoryCode;
private String taskType;
private Boolean replaceUnconfirmedOnly;
private Long modelId;
private String modelName;
private String promptMessagesJson;
private String inputPayloadJson;
private Long requestedBy;
```

字段规则与 `ReextractRequest` 一致。

服务端生成字段：

- `requestId`：调用既有图谱抽取服务前生成，格式建议为 `quality-reextract-${UUID}`。
- `traceId`：调用既有图谱抽取服务前生成，格式建议为 `quality-reextract-trace-${UUID}`。

### 后端返回

文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/response/QualityReportResponses.java`

新增 `ReextractResponse`：

```java
@Getter
@Builder
public static class ReextractResponse {
    private Long reportId;
    private String sourceCategoryCode;
    private String sourceCategoryName;
    private String sourceContentType;
    private Long sourceContentId;
    private Long taskId;
    private Long batchJobId;
    private String taskType;
    private String triggerSource;
    private String selectionScopeJson;
    private Boolean replaceUnconfirmedOnly;
}
```

字段语义：

- `reportId`：本次触发来源报告 ID。
- `sourceCategoryCode`、`sourceCategoryName`：本次重提取门类。
- `sourceContentType`：服务端从同门类来源明细聚合出的来源类型。
- `sourceContentId`：同门类第一个来源 ID，用于兼容现有抽取任务创建字段。
- `taskId`：创建出的父任务或单任务 ID。
- `batchJobId`：多来源批量任务的 AI 批次 ID；单来源允许为空。
- `taskType`：本次抽取任务类型，缺省 `GRAPH`。
- `triggerSource`：固定 `QUALITY_REPORT`。
- `selectionScopeJson`：服务端生成的批量范围快照。
- `replaceUnconfirmedOnly`：最终采用的替换策略。

### 应用结果

新增文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/result/ReextractLowQualityCategoryResult.java`

字段与 `ReextractResponse` 一一对应：

```java
private Long reportId;
private String sourceCategoryCode;
private String sourceCategoryName;
private String sourceContentType;
private Long sourceContentId;
private Long taskId;
private Long batchJobId;
private String taskType;
private String triggerSource;
private String selectionScopeJson;
private Boolean replaceUnconfirmedOnly;
```

### `selectionScopeJson`

服务端生成，前端不得手写。

JSON 字段必须精确包含：

```json
{
  "triggerSource": "QUALITY_REPORT",
  "qualityReportId": 2001,
  "graphVersionId": 3001,
  "sourceCategoryCode": "SANCAI_ENTRY",
  "sourceCategoryName": "三才图会",
  "sourceContentType": "SANCAI_ENTRY",
  "sourceContentIds": [1001, 1002]
}
```

字段规则：

- `triggerSource` 固定为 `QUALITY_REPORT`。
- `qualityReportId` 取质量报告 `reportId`。
- `graphVersionId` 取质量报告 `report.graphVersionId`。
- `sourceCategoryCode` 取请求中的门类编码。
- `sourceCategoryName` 取报告来源明细中同门类名称；多个名称不一致时取第一条非空值。
- `sourceContentType` 必须单一；同一门类存在多个 `sourceContentType` 时，服务端拒绝本次创建，错误消息为 `低质量门类包含多个来源类型，请按来源类型拆分重提取`。
- `sourceContentIds` 取同一报告、同一门类、同一来源类型下所有 `sourceContentId`，去重后升序。

## 后端任务拆分

每个小任务控制在 2-5 个文件。

### 后端任务 1：接口和数据结构

修改文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/request/QualityReportRequests.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/response/QualityReportResponses.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/command/ReextractLowQualityCategoryCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/result/ReextractLowQualityCategoryResult.java`

要求：

- 增加请求、命令、结果和响应字段。
- 不改数据库表。
- 不新增权限点。

### 后端任务 2：质量报告轻编排服务

修改文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/KnowledgeQualityReportApplicationService.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/impl/KnowledgeQualityReportApplicationServiceImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/assembler/KnowledgeQualityReportInterfaceAssembler.java`

要求：

- 在 `KnowledgeQualityReportApplicationService` 增加 `reextractLowQualityCategory(ReextractLowQualityCategoryCommand command)`。
- 在实现类中根据 `reportId` 读取报告详情。
- 过滤同一 `sourceCategoryCode` 且 `issueCount > 0` 的 `sourceDetails`。
- 校验 `sourceContentIds` 非空。
- 校验聚合后 `sourceContentType` 唯一。
- 生成 `selectionScopeJson`。
- 生成 `requestId` 和 `traceId`，不要要求前端传入。
- 调用既有 `KnowledgeGraphExtractionApplicationService` 创建任务，固定 `triggerSource=QUALITY_REPORT`。
- `replaceUnconfirmedOnly` 缺省为 `true`。

### 后端任务 3：Controller 与测试

修改文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/KnowledgeQualityReportController.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/KnowledgeQualityReportControllerTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/KnowledgeQualityReportApplicationServiceTest.java`

要求：

- 新增接口 `POST /api/knowledge/quality/report/reextract-low-quality-category`。
- Controller 方法加 `@HasPermission("knowledge:graph:edit")`。
- Controller 测试断言路径、方法和权限。
- 应用测试覆盖：
  - 单来源创建单任务。
  - 多来源生成批量任务，`selectionScopeJson.sourceContentIds` 去重升序。
  - 无质量问题拒绝创建。
  - 多来源类型拒绝创建。
  - 返回 `triggerSource=QUALITY_REPORT`。

## 前端任务拆分

每个小任务控制在 2-5 个文件。

### 前端任务 1：类型和服务

修改文件：

- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-types.ts`
- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-service.ts`
- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-service.test.ts`

新增类型：

```ts
export interface ReextractLowQualityCategoryCommand {
    reportId: number;
    sourceCategoryCode: string;
    taskType?: "GRAPH" | "RELATION" | "LINEAGE" | string;
    replaceUnconfirmedOnly?: boolean;
    modelId?: number;
    modelName?: string;
    promptMessagesJson?: string;
    inputPayloadJson?: string;
    requestedBy?: number | null;
}

export interface ReextractLowQualityCategoryRecord {
    reportId: number;
    sourceCategoryCode?: string | null;
    sourceCategoryName?: string | null;
    sourceContentType?: string | null;
    sourceContentId?: number | null;
    taskId?: number | null;
    batchJobId?: number | null;
    taskType?: string | null;
    triggerSource?: string | null;
    selectionScopeJson?: string | null;
    replaceUnconfirmedOnly?: boolean | null;
}
```

新增服务：

- `reextractLowQualityCategory(request: ReextractLowQualityCategoryCommand)`
- 请求路径：`/knowledge/quality/report/reextract-low-quality-category`
- 请求方法：`POST`

测试要求：

- 断言请求路径、方法和 body 字段完整。

### 前端任务 2：来源明细表控件

修改文件：

- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/components/quality-report-source-table.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-page.css`

`QualityReportSourceTableProps` 增加：

```ts
reportId?: number | null;
graphVersionId?: number | null;
canReextract?: boolean;
reextractingKey?: string | null;
onReextract?: (sourceDetail: QualityReportSourceDetailRecord) => void;
```

控件要求：

- 操作列保持 `KuzhambuSpaceCompact`。
- “打开”按钮保持原行为。
- 新增 `Tooltip + Button`：
  - 文案：`重提取`
  - `type="primary"`
  - `disabled` 根据完成态交互规则计算。
  - `loading` 在 `reextractingKey === sourceDetail.detailId.toString()` 时为 `true`。
  - `onClick` 调用 `onReextract(sourceDetail)`。
- 不在表格中增加 JSON 文本域、模型选择器或任务类型选择器。

### 前端任务 3：页面编排和成功反馈

修改文件：

- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-page.test.tsx`

页面逻辑：

- 引入 `hasPermission("knowledge:graph:edit")` 得到 `canReextract`。
- 增加 `useMutation` 调用 `service.reextractLowQualityCategory`。
- 在 `quality-report-page.tsx` 顶部增加本页常量：

```ts
const QUALITY_REEXTRACT_TASK_TYPE = "GRAPH";
const QUALITY_REEXTRACT_MODEL_ID = 1;
const QUALITY_REEXTRACT_MODEL_NAME = "gpt-5.5";
const QUALITY_REEXTRACT_PROMPT_MESSAGES_JSON =
    '[{"role":"system","content":"extract knowledge graph from quality report low quality category"}]';
const QUALITY_REEXTRACT_INPUT_PAYLOAD_JSON = '{"triggerSource":"QUALITY_REPORT"}';
```

- 点击来源行“重提取”时弹出 `Modal.confirm`。
- mutation body 固定：
  - `reportId`: 当前报告 ID。
  - `sourceCategoryCode`: 当前行门类编码。
  - `taskType`: `QUALITY_REEXTRACT_TASK_TYPE`。
  - `replaceUnconfirmedOnly`: `true`。
  - `modelId`: `QUALITY_REEXTRACT_MODEL_ID`。
  - `modelName`: `QUALITY_REEXTRACT_MODEL_NAME`。
  - `promptMessagesJson`: `QUALITY_REEXTRACT_PROMPT_MESSAGES_JSON`。
  - `inputPayloadJson`: `QUALITY_REEXTRACT_INPUT_PAYLOAD_JSON`。
- 成功后保存最近创建结果，页面展示：
  - `任务号`
  - `任务类型`
  - `触发来源`
  - `批次号`
  - “打开任务台账”按钮
- 测试覆盖：
  - 无 `knowledge:graph:edit` 时“重提取”禁用。
  - `issueCount=0` 时“重提取”禁用。
  - 点击有问题门类弹出确认框。
  - 确认后调用服务并传 `taskType=GRAPH`、`replaceUnconfirmedOnly=true`。
  - 成功后展示任务号并保留质量报告内容。

## Readiness 文档收口

实现完成后修改：

- `docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`

调整项：

- 顶部“未完成”移除“从质量报告低质量门类一键触发重提取仍未落地”。
- “从质量报告或筛选结果批量触发提取”调整为已完成。
- “质量报告按门类分组并支持低质量门类触发提取”调整为已完成。
- `Unfinished Focus` 中“图谱浏览与质量报告”移除该剩余项。
- `Residual Risks` 删除该剩余风险，仅保留真实未完成项。
- 任务关闭前删除本 RUNBOOK。

## 验收标准

- 在质量报告页打开一个含低质量门类的报告，来源明细有“重提取”按钮。
- 无问题门类、缺门类编码、缺报告编号、无 `knowledge:graph:edit` 权限时，“重提取”按钮禁用且原因明确。
- 点击低质量门类“重提取”后出现确认弹窗。
- 确认后调用 `POST /api/knowledge/quality/report/reextract-low-quality-category`。
- 后端创建的任务 `triggerSource=QUALITY_REPORT`。
- 后端返回 `selectionScopeJson`，其中包含 `qualityReportId`、`graphVersionId`、`sourceCategoryCode`、`sourceCategoryName`、`sourceContentType`、`sourceContentIds`。
- `/knowledge/graph-extraction` 可看到该任务；多来源时可看到父任务和 `batchJobId`。
- 任务成功后可使用既有候选应用动作。
- 应用后正式结果页可读取新版本或续增版本。
- 重新生成质量报告后，相关门类指标读取最新正式事实。

## 建议验证

后端：

```sh
cd kuzhambu-servers
mvn -pl biz/knowledge/kuzhambu-knowledge-application -am spotless:apply
mvn -pl biz/knowledge/kuzhambu-knowledge-interface -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/knowledge -am test
```

前端：

```sh
cd kuzhambu-apps
npm --workspace kuzhambu-admin-web run format
npm run format:check
npm run lint
npm run test
npm run build
```

冒烟：

- 使用 Admin Web 生成或打开已有质量报告。
- 对低质量门类点击“重提取”。
- 在抽取任务页查看 `QUALITY_REPORT` 任务和批次。
- 完成候选应用后生成新质量报告，确认同门类指标来自最新正式结果。
