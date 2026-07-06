# AI Knowledge Tag Extraction 入口闭环 RUNBOOK

## 目标

补齐 `KNOWLEDGE_TAG_EXTRACTION` 的 Java AI 治理入口，让 Knowledge 标签候选抽取形成同步闭环：

```text
admin-web 标签治理页 -> Knowledge 标签抽取入口 -> AI Facade -> AI Knowledge Domain Service -> Workers usecase -> AI Candidate -> Knowledge 标签审核/应用
```

本任务只做最小闭环：同步创建 AI 候选，前端展示候选并允许人工审核应用；不新增 worker 能力，不新增独立异步任务台账，不自动写正式标签。

本任务不涉及数据库表结构变更。

## 依据

- `docs/20-interfaces/WORKERS-AI-USECASE-INTERFACE.md`：Knowledge 标签候选抽取 usecase 为 `POST /internal/ai/knowledge/tag-extraction`，operation 为 `KNOWLEDGE_TAG_EXTRACTION`，capability 为 `tags`。
- `docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`：当前唯一未完成项为 Knowledge `tags` Java 入口缺失。
- `docs/40-readiness/WORKERS-IMPLEMENTATION-COVERAGE.md`：worker 侧 `KNOWLEDGE_TAG_EXTRACTION` 已完成。
- `docs/00-governance/SERVERS-ARCHITECTURE.md`：Knowledge 域通过 AI 域治理入口调用 workers，不直接调用 workers。
- `docs/00-governance/ADMIN-WEB-RULES.md`：admin-web 页面、service、组件、控件和可访问名称规则。

## 完成态

- AI 域支持 `TAG` 任务类型，稳定解析到 `KNOWLEDGE_TAG_EXTRACTION / /internal/ai/knowledge/tag-extraction / tags`。
- AI Facade 暴露 `extractKnowledgeTags(...)`，Knowledge 域通过 facade 调用，不直接依赖 worker client。
- Knowledge 标签治理域提供同步标签抽取入口，返回 `aiCallId / aiCandidateId / status / resultPayload / errorType / errorMessage`。
- AI 候选结果不自动变成正式标签；应用时进入 Knowledge 标签治理，生成待审核标签或复用既有审核规则。
- admin-web `taxonomy` 页面出现明确的 `AI 抽取标签` 操作，支持输入内容范围、查看候选、应用候选、刷新待审核标签。
- `AI-IMPLEMENTATION-COVERAGE.md` 中 `KNOWLEDGE_TAG_EXTRACTION` 只保留在已完成矩阵。
- 任务关闭时删除本 RUNBOOK。

## 字段契约

### Java AI Facade 请求

复用并扩展语义，不新增 parallel request 类型。

文件：`kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/KnowledgeAiExtractionFacadeRequest.java`

字段要求：

| 字段 | 类型 | 标签抽取要求 |
| --- | --- | --- |
| `taskType` | `String` | 固定为 `TAG` |
| `scopeType` | `String` | 固定为 `CONTENT` |
| `scopeJson` | `String` | JSON 字符串，包含 `contentType`、`contentIds`、`includeExistingTags` |
| `sourceContentType` | `String` | 必填，例如 `SANCAI_ENTRY`、`WANGQI_DOCUMENT`、`MING_CUSTOMS` |
| `sourceContentId` | `Long` | 单条入口必填；批量入口首版不做 |
| `requestedBy` | `Long` | 后台操作者 ID |
| `serviceId` | `Long` | 可选，模型服务 ID |
| `serviceRole` | `String` | 可选，默认模型服务角色 |
| `modelId` | `Long` | 必填 |
| `modelName` | `String` | 必填 |
| `promptVersionId` | `Long` | 可选，但传入时必须进入调用记录 |
| `requestId` | `String` | 必填，Java 生成稳定请求号 |
| `traceId` | `String` | 必填，贯穿 Knowledge -> AI |
| `promptMessagesJson` | `String` | 必填，AI prompt messages JSON |
| `promptVariablesJson` | `String` | 可选，必须包含实际渲染变量时传入 |
| `promptHash` | `String` | 可选，存在时进入调用记录 |
| `inputPayloadJson` | `String` | 必填，字段见下方 `inputPayloadJson` |
| `outputSchemaJson` | `String` | 必填，字段见下方 `outputSchemaJson` |
| `forceJson` | `boolean` | 固定为 `true` |
| `locale` | `String` | 默认 `zh-CN` |

`inputPayloadJson` 固定包含：

```json
{
  "contentType": "SANCAI_ENTRY",
  "contentId": 1001,
  "contentTitle": "条目标题",
  "contentText": "用于抽取的正文片段",
  "existingTags": [
    {
      "tagId": "tag_001",
      "name": "礼制",
      "categoryId": "cat_001",
      "categoryName": "制度"
    }
  ],
  "categories": [
    {
      "categoryId": "cat_001",
      "name": "制度"
    }
  ],
  "aliases": [
    {
      "tagId": "tag_001",
      "name": "礼法"
    }
  ],
  "constraints": {
    "maxTags": 10,
    "allowNewTags": true,
    "reviewRequired": true
  }
}
```

`outputSchemaJson` 固定表达：

```json
{
  "type": "object",
  "properties": {
    "tags": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "name": {"type": "string"},
          "categoryId": {"type": "string"},
          "categoryName": {"type": "string"},
          "confidence": {"type": "number"},
          "reason": {"type": "string"},
          "matchedExistingTagId": {"type": "string"}
        },
        "required": ["name", "confidence"]
      }
    }
  },
  "required": ["tags"]
}
```

### Java Knowledge Application 命令与响应

文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagExtractionCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagCandidateApplyCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/result/TagExtractionResult.java`

`TagExtractionCommand` 字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `sourceContentType` | `String` | 必填，例如 `SANCAI_ENTRY` |
| `sourceContentId` | `Long` | 必填，单条内容 ID |
| `contentTitle` | `String` | 可选，内容标题 |
| `contentText` | `String` | 必填，待抽取正文片段 |
| `modelId` | `Long` | 必填 |
| `modelName` | `String` | 必填 |
| `promptVersionId` | `Long` | 可选 |
| `maxTags` | `Integer` | 可选，默认 `10` |
| `allowNewTags` | `Boolean` | 可选，默认 `true` |
| `requestedBy` | `Long` | 必填，后台操作者 ID |

`TagCandidateApplyCommand` 字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `aiCandidateId` | `Long` | 必填，AI 候选 ID |
| `selectedTags` | `List<TagCandidateApplyItemCommand>` | 必填，前端勾选的候选标签 |
| `reviewNote` | `String` | 可选，审核备注 |
| `reviewedBy` | `Long` | 必填，后台操作者 ID |

`TagCandidateApplyItemCommand` 字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `name` | `String` | 必填，候选标签名 |
| `categoryId` | `String` | 可选，分类 ID |
| `categoryName` | `String` | 可选，分类名 |
| `confidence` | `BigDecimal` | 可选，置信度 |
| `reason` | `String` | 可选，抽取理由 |
| `matchedExistingTagId` | `String` | 可选，匹配到的既有标签 ID |

`TagExtractionResult` 字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `aiCallId` | `Long` | AI 调用记录 ID |
| `aiCandidateId` | `Long` | AI 候选 ID |
| `status` | `String` | `SUCCEEDED / FAILED` 等 AI 最终态 |
| `resultFormat` | `String` | 预期为 `STRUCTURED` |
| `resultPayload` | `String` | worker 返回的标签候选 JSON |
| `errorType` | `String` | 失败类型 |
| `errorMessage` | `String` | 失败说明 |

### Java Knowledge HTTP 请求与响应

文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagExtractionRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagCandidateApplyRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagExtractionResponse.java`

`TagExtractionRequest` 字段：

| 字段 | 类型 | 校验 |
| --- | --- | --- |
| `sourceContentType` | `String` | `@NotBlank` |
| `sourceContentId` | `Long` | `@NotNull` |
| `contentTitle` | `String` | 可空 |
| `contentText` | `String` | `@NotBlank` |
| `modelId` | `Long` | `@NotNull` |
| `modelName` | `String` | `@NotBlank` |
| `promptVersionId` | `Long` | 可空 |
| `maxTags` | `Integer` | 可空，最小 `1` |
| `allowNewTags` | `Boolean` | 可空 |
| `requestedBy` | `Long` | `@NotNull` |

`TagCandidateApplyRequest` 字段：

| 字段 | 类型 | 校验 |
| --- | --- | --- |
| `aiCandidateId` | `Long` | `@NotNull` |
| `selectedTags` | `List<TagCandidateApplyItemRequest>` | `@NotEmpty` |
| `reviewNote` | `String` | 可空 |
| `reviewedBy` | `Long` | `@NotNull` |

`TagCandidateApplyItemRequest` 字段：

| 字段 | 类型 | 校验 |
| --- | --- | --- |
| `name` | `String` | `@NotBlank` |
| `categoryId` | `String` | 可空 |
| `categoryName` | `String` | 可空 |
| `confidence` | `BigDecimal` | 可空 |
| `reason` | `String` | 可空 |
| `matchedExistingTagId` | `String` | 可空 |

`TagExtractionResponse` 字段与 `TagExtractionResult` 一一对应，不额外解析 `resultPayload`。

### admin-web 类型

文件：`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-types.ts`

新增：

```ts
export interface TagExtractionCandidateRecord {
    name: string;
    categoryId?: string | null;
    categoryName?: string | null;
    confidence?: number | null;
    reason?: string | null;
    matchedExistingTagId?: string | null;
}

export interface TagExtractionResultRecord {
    aiCallId?: number | null;
    aiCandidateId?: number | null;
    status?: string | null;
    resultFormat?: string | null;
    resultPayload?: string | null;
    errorType?: string | null;
    errorMessage?: string | null;
    candidates?: TagExtractionCandidateRecord[] | null;
}
```

文件：`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-service.ts`

新增 service command：

```ts
export interface TagExtractionCommand {
    sourceContentType: string;
    sourceContentId: string;
    contentTitle?: string | null;
    contentText: string;
    modelId: number;
    modelName: string;
    promptVersionId?: number | null;
    maxTags?: number | null;
    allowNewTags?: boolean | null;
}

export interface TagCandidateApplyCommand {
    aiCandidateId: number;
    selectedTags: TagExtractionCandidateRecord[];
    reviewNote?: string | null;
}
```

新增 service 方法：

- `extractTags(request: TagExtractionCommand)`
- `applyExtractedTags(request: TagCandidateApplyCommand)`

API 路径固定为：

- `POST /knowledge/taxonomy/tag/extract`
- `POST /knowledge/taxonomy/tag/extract/apply`

## 小任务拆分

### 任务 1：AI 域补标签 usecase 映射

文件范围：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/knowledge/service/KnowledgeAiExtractionDomainService.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/knowledge/service/impl/KnowledgeAiExtractionApplicationServiceImpl.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/knowledge/support/KnowledgeAiWorkerUsecaseResolver.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/knowledge/support/KnowledgeAiWorkerUsecaseResolverTest.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/knowledge/service/impl/KnowledgeAiExtractionApplicationServiceImplTest.java`

动作：

- 新增 `extractTags(KnowledgeAiExtractionRequest request)`。
- `KnowledgeAiWorkerUsecaseResolver` 新增 `TAG` 映射。
- 测试断言 `TAG` 解析为 `KNOWLEDGE_TAG_EXTRACTION / /internal/ai/knowledge/tag-extraction / tags`。
- 测试断言 `extractTags` 传入 `AiInvokeCommand` 时 `scope=knowledge`、`forceJson=true`、`createCandidate=true`。

### 任务 2：AI Facade 暴露 Knowledge 标签抽取

文件范围：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/AiFacade.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/facade/impl/AiFacadeImpl.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/facade/assembler/AiFacadeAssembler.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/facade/impl/AiFacadeImplTest.java`

动作：

- `AiFacade` 新增 `KnowledgeAiExtractionFacadeResponse extractKnowledgeTags(KnowledgeAiExtractionFacadeRequest request)`。
- facade impl 复用现有 `KnowledgeAiExtractionFacadeRequest -> KnowledgeAiExtractionRequest` 转换。
- 测试断言 `taskType=TAG` 被保留，响应保留 `callId/candidateId/status/resultPayload`。

### 任务 3：Knowledge 标签抽取后端入口

文件范围：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/TaxonomyApplicationService.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagExtractionCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/result/TagExtractionResult.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImplTest.java`

动作：

- 新增 `extractTags(TagExtractionCommand command)`。
- command 字段精确为：
  - `sourceContentType: String`
  - `sourceContentId: Long`
  - `contentTitle: String`
  - `contentText: String`
  - `modelId: Long`
  - `modelName: String`
  - `promptVersionId: Long`
  - `maxTags: Integer`
  - `allowNewTags: Boolean`
  - `requestedBy: Long`
- result 字段精确为：
  - `aiCallId: Long`
  - `aiCandidateId: Long`
  - `status: String`
  - `resultFormat: String`
  - `resultPayload: String`
  - `errorType: String`
  - `errorMessage: String`
- service 组装 `KnowledgeAiExtractionFacadeRequest`，固定 `taskType=TAG`、`forceJson=true`、`locale=zh-CN`。
- service 组装 `scopeType=CONTENT`，`scopeJson` 固定包含 `contentType`、`contentIds`、`includeExistingTags=true`。
- `inputPayloadJson` 必须包含内容快照、已有标签、分类、别名和约束。

### 任务 4：Knowledge 标签候选应用后端入口

文件范围：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/TaxonomyApplicationService.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagCandidateApplyCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImplTest.java`

动作：

- 新增 `applyExtractedTags(TagCandidateApplyCommand command)`。
- command 字段精确为：
  - `aiCandidateId: Long`
  - `selectedTags: List<TagCandidateApplyItemCommand>`
  - `reviewNote: String`
  - `reviewedBy: Long`
- item 字段精确为：
  - `name: String`
  - `categoryId: String`
  - `categoryName: String`
  - `confidence: BigDecimal`
  - `reason: String`
  - `matchedExistingTagId: String`
- 应用规则：
  - `matchedExistingTagId` 存在时复用既有标签。
  - 不存在时创建 `source=AI_EXTRACTED`、`reviewStatus=PENDING` 的标签。
  - 不创建重复标签名。
  - 应用完成后调用 AI facade `markCandidateApplied(...)`。

### 任务 5：Knowledge interface 暴露 HTTP API

文件范围：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/KnowledgeTaxonomyController.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagExtractionRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagCandidateApplyRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagExtractionResponse.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/KnowledgeTaxonomyControllerTest.java`

动作：

- 新增 `POST /api/knowledge/taxonomy/tag/extract`，权限 `knowledge:taxonomy:edit`。
- 新增 `POST /api/knowledge/taxonomy/tag/extract/apply`，权限 `knowledge:taxonomy:edit`。
- request/response 字段与 application command/result 一一对应。
- controller test 覆盖 request 转 command 和 response 字段不丢失。
- controller 不解析 `resultPayload`，只把 AI 最终态和原始结构化 payload 返回给前端。

### 任务 6：admin-web service 与类型

文件范围：

- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-types.ts`
- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-service.ts`
- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-service.test.ts`

动作：

- 新增 `TagExtractionCommand`、`TagCandidateApplyCommand`、`TagExtractionCandidateRecord`、`TagExtractionResultRecord`。
- 新增 `extractTags` 调用 `/knowledge/taxonomy/tag/extract`。
- 新增 `applyExtractedTags` 调用 `/knowledge/taxonomy/tag/extract/apply`。
- `extractTags` 收到 `resultPayload` 后在 service 或 drawer 边界解析 `JSON.parse(resultPayload).tags`，写入 `TagExtractionResultRecord.candidates`；解析失败时保留原始 `resultPayload` 并让页面展示错误提示。
- service contract test 精确断言 body 字段：
  - `sourceContentType`
  - `sourceContentId`
  - `contentTitle`
  - `contentText`
  - `modelId`
  - `modelName`
  - `promptVersionId`
  - `maxTags`
  - `allowNewTags`
  - `aiCandidateId`
  - `selectedTags`
  - `reviewNote`

### 任务 7：admin-web 标签抽取控件

文件范围：

- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/tag-extraction-drawer.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/tag-extraction-candidate-table.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-page.css`
- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-page.test.tsx`

控件与操作：

- 在 `统一标签` tab 的 `pageActions` 增加按钮：
  - 文案：`AI 抽取标签`
  - 可访问名称：`AI 抽取标签`
  - 权限：仅 `knowledge:taxonomy:edit` 可见或可用
  - 点击：打开 `TagExtractionDrawer`
- `TagExtractionDrawer` 使用 `KuzhambuDrawer`，标题 `AI 抽取标签`，包含表单控件：
  - `Select`：标签 `内容类型`，选项 `SANCAI_ENTRY / WANGQI_DOCUMENT / MING_CUSTOMS`
  - `Input`：标签 `内容 ID`
  - `Input`：标签 `内容标题`
  - `TextArea`：标签 `内容片段`，必填，`rows=6`
  - `InputNumber`：标签 `模型 ID`
  - `Input`：标签 `模型名称`
  - `InputNumber`：标签 `提示词版本 ID`
  - `InputNumber`：标签 `最大标签数`，默认 `10`
  - `Switch`：标签 `允许创建新标签`，默认开启
  - 主按钮：`开始抽取`
  - 次按钮：`取消`
- 抽取成功后 drawer 内展示候选表格 `TagExtractionCandidateTable`：
  - `Checkbox` 列：选择候选
  - `标签名` 列：展示 `name`
  - `分类` 列：展示 `categoryName || categoryId || -`
  - `置信度` 列：展示百分比
  - `匹配标签` 列：展示 `matchedExistingTagId || 新标签`
  - `理由` 列：展示 `reason`
- 候选表格下方控件：
  - `TextArea`：标签 `审核备注`
  - 主按钮：`应用选中标签`
  - 次按钮：`重新抽取`
- `应用选中标签` 前使用 `useKuzhambuConfirm`：
  - 标题：`应用 AI 标签候选`
  - 内容：提示将把选中候选进入标签审核治理
  - 确认按钮：`应用`
- 应用成功后：
  - 关闭 drawer
  - invalidate `["knowledge", "taxonomy", "tags"]`
  - invalidate `["knowledge", "taxonomy", "reviews"]`
  - invalidate `["knowledge", "taxonomy", "metrics"]`
  - 切换到 `待审核标签` tab 或展示成功提示 `AI 标签候选已进入待审核`
- 页面测试覆盖：
  - 点击 `AI 抽取标签` 打开 drawer。
  - 填写内容类型、内容 ID、内容片段、模型 ID、模型名称后点击 `开始抽取`。
  - 候选返回后勾选候选并点击 `应用选中标签`。
  - 确认弹窗点击 `应用` 后调用 `applyExtractedTags`。

### 任务 8：coverage 收口

文件范围：

- `docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`
- `docs/30-designs/RUNBOOK-AI-KNOWLEDGE-TAG-EXTRACTION.md`

动作：

- `AI-IMPLEMENTATION-COVERAGE.md` 的 `Current Baseline` 删除 Knowledge `tag_extraction` 未完成表述。
- `已完成` 矩阵新增：
  - domain：`knowledge`
  - contentType：`-`
  - capability：`tags`
  - javaEntry：实际 Knowledge 标签抽取入口，例如 `TaxonomyApplicationService#extractTags`
  - operation：`KNOWLEDGE_TAG_EXTRACTION`
  - workerPath：`/internal/ai/knowledge/tag-extraction`
  - status：`已完成`
  - note：`已通过 Knowledge 标签治理入口接入 AI candidate 闭环`
- `未完成` 矩阵删除 `KNOWLEDGE_TAG_EXTRACTION` 行。
- 实现 PR 收口前删除本 RUNBOOK。

## 验证命令

Java 最小相关验证：

```sh
cd kuzhambu-servers
mvn -pl biz/ai/kuzhambu-ai-application,biz/ai/kuzhambu-ai-domain,biz/ai/kuzhambu-ai-facade,biz/knowledge/kuzhambu-knowledge-application,biz/knowledge/kuzhambu-knowledge-interface -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/ai/kuzhambu-ai-application,biz/ai/kuzhambu-ai-domain,biz/ai/kuzhambu-ai-facade,biz/knowledge/kuzhambu-knowledge-application,biz/knowledge/kuzhambu-knowledge-interface -am test
```

前端最小相关验证：

```sh
cd kuzhambu-apps
npm --workspace admin-web run format
npm run format:check
npm run lint
npm --workspace admin-web run test -- taxonomy
```

最终检查：

```sh
git diff
rg -n "KNOWLEDGE_TAG_EXTRACTION|tag-extraction|extractTags|AI 抽取标签|未完成" docs/40-readiness kuzhambu-servers/biz/ai kuzhambu-servers/biz/knowledge kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy
```

## 不做事项

- 不新增 workers 路由、registry、graph 或 prompt runner。
- 不新增独立 Knowledge 标签抽取异步任务台账。
- 不把标签候选自动写成已通过正式标签。
- 不在 `KnowledgeGraphExtractionController` 里复用图谱抽取入口承载标签抽取。
- 不让 Knowledge 域直接调用 `/internal/ai/knowledge/tag-extraction`。
