# RUNBOOK AI Candidate Confirmation

## Goal

打通 Classics 内容上下文中的 AI 候选确认闭环：

```text
AI 域精修接口写入 ai_candidate(PENDING)
-> Admin Web 预览/编辑候选 -> 用户确认或拒绝
-> Classics 写正式内容/标签/问答对/版本 -> AI 候选标记 APPLIED 或 REJECTED
```

本 RUNBOOK 只覆盖单条内容的确认闭环，不做批量任务、Discovery、Knowledge、Portal、私有权限过滤、AI 触发配置 UI 和真实模型配置自动选择。

## Existing Anchors

- AI 候选表：`db/schema/ai.sql` 的 `ai_candidate`。
- AI 候选领域对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/model/entity/AiCandidate.java`。
- AI 候选查询/拒绝/标记已应用入口：`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/AiInvocationController.java`。
- AI 精修触发入口：`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/AiRefinementController.java`。
- Classics 版本类型：`kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/model/enums/ClassicsContentChangeType.java` 已有 `AI_APPLIED`。
- Classics 当前接入点：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java` 的 `applyAiResult(...)` 当前抛错。
- Classics 主内容表和通用内容表：`db/schema/classics.sql` 的 `classics_sancai_entry`、`classics_wangqi_document`、`classics_ming_customs_entry`、`classics_content_tag`、`classics_content_qa_pair`、`classics_content_version`。

## Data Contract

不新增数据库表。

跨域依赖口径：

- 不新增 `AI application -> Classics application` 依赖。
- 不新增 `Classics application -> AI application` 依赖。
- AI 候选事实仍归 AI 域；Classics 确认用例通过 AI 域 `AiCandidateDomainService` 读取、校验和标记候选状态。
- Admin Web 调用 Classics 的候选应用入口完成“应用到正式内容”；AI 的 `candidate/mark-applied` 只保留为旧管理入口，本轮前端不再调用。

`ai_candidate` 状态只允许本轮使用：

- `PENDING`：候选处于待处理状态，支持预览、编辑后应用、拒绝。
- `APPLIED`：候选已被业务域应用到正式内容。
- `REJECTED`：候选已被用户拒绝。

候选应用请求使用新增 DTO，字段如下：

```json
{
  "candidateId": 7001,
  "contentType": "SANCAI_ENTRY",
  "contentId": 300000000001,
  "capability": "summary",
  "resultFormat": "TEXT",
  "resultPayload": "人工确认后的摘要或 JSON 字符串",
  "changeSummary": "AI 应用：摘要"
}
```

字段规则：

- `candidateId` 必填，用于读取 `ai_candidate` 并最终更新状态。
- `contentType` 必填，取值只支持 `SANCAI_ENTRY`、`WANGQI_DOCUMENT`、`MING_CUSTOMS`。
- `contentId` 必填，必须与 `ai_candidate.content_id` 一致。
- `capability` 必填，必须与 `ai_candidate.capability` 一致。
- `resultFormat` 必填，允许 `TEXT`、`MARKDOWN`、`STRUCTURED`、`JSON`；大小写按原值保存，解析时忽略大小写。
- `resultPayload` 必填，使用用户编辑后的最终候选内容；不得重新读取 workers。
- `changeSummary` 允许为空；为空时后端使用 `AI 应用：{capability}`。

候选 `resultPayload` 解析规则：

| capability | contentType | resultFormat | payload contract | 应用结果 |
| --- | --- | --- | --- | --- |
| `translate` | `SANCAI_ENTRY` | `TEXT` 或 `MARKDOWN` | payload 原文字符串 | 更新 `classics_sancai_entry.translation_text`，`translation_status` 设为 `TRANSLATED` |
| `summary` | 三类内容 | `TEXT` 或 `MARKDOWN` | payload 原文字符串 | 更新主内容 `summary` |
| `tags` | 三类内容 | `STRUCTURED` 或 `JSON` | `{"tags":["礼制","服饰"]}` 或 `["礼制","服饰"]` | 删除该内容现有 AI 来源标签，插入 `classics_content_tag(source=AI,status=ACTIVE)` |
| `qa` | 三类内容 | `STRUCTURED` 或 `JSON` | `{"qaPairs":[{"question":"...","answer":"..."}]}` 或 `[{"question":"...","answer":"..."}]` | 删除该内容现有 AI 来源问答对，插入 `classics_content_qa_pair(source=AI)` |

本轮不应用 `image_analysis`、`visual`、`fusion`、`image_gen`、`split`；这些 capability 在应用接口中返回业务异常。

## Backend Task Blocks

### B0 架构门禁同步

目标：同步单体跨域规则，撤销 `SERVERS_CROSS_DOMAIN_NO_REPOSITORY_PORT_DEPENDENCY` 对应的架构测试断言。

修改文件：

- `kuzhambu-servers/common/kuzhambu-common-test/src/main/java/com/thundax/kuzhambu/common/test/architecture/ModuleAndDependencyArchitectureRuleSupport.java`

实现要求：

- 修改 `assertCrossDomainDependencyBoundary(JavaClasses classes, String currentDomain)`。
- 保留跨域禁止依赖对端 `infra..` 的断言。
- 删除跨域禁止依赖对端 `domain..repository..` 的断言。
- 目标代码形态只保留：

```java
.resideInAnyPackage("com.thundax.kuzhambu." + domain + ".infra..")
```

- 不新增“必须通过对端 application 公开用例”的替代断言。
- 不新增禁止跨域依赖对端 `domain..service..` 的断言；本轮需要 Classics application 依赖 AI domain service。

验收点：

- `SERVERS_CROSS_DOMAIN_NO_REPOSITORY_PORT_DEPENDENCY` 不再出现在代码和治理文档中。
- 各业务域 `*ArchitectureTest` 仍会通过 `assertCrossDomainDependencyBoundary` 禁止跨域依赖对端 infra。

### B1 AI 候选 DomainService

目标：把 AI 候选状态能力沉淀为供跨域调用的 DomainService，不让 application service 互相依赖。

#### B1.1 新增校验 DTO

新增文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/service/AiCandidateApplyCheck.java`

- `AiCandidateApplyCheck` 字段：

```java
public class AiCandidateApplyCheck {
    private Long candidateId;
    private String contentType;
    private Long contentId;
    private String capability;
}
```

#### B1.2 新增 DomainService

新增文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/service/AiCandidateDomainService.java`

实现要求：

- `AiCandidateDomainService` 是纯 domain service，不使用 Spring MVC、MyBatis 或持久化注解。
- `AiCandidateDomainService` 构造参数为 `AiInvocationRepository`。
- `AiCandidateDomainService` 方法固定为：

```java
public AiCandidate requirePendingForApply(AiCandidateApplyCheck check);
public AiCandidate markApplied(Long candidateId, String resultFormat, String resultPayload, Instant appliedAt);
public AiCandidate reject(Long candidateId, String errorType, String errorMessage);
```

- `requirePendingForApply(...)` 流程固定为：
  1. 读取 `AiInvocationRepository.getCandidate(candidateId)`。
  2. 不存在则抛 `DomainException("AI-INVOCATION-404", "ai.candidate.not-found", "AI candidate not found: " + candidateId)`。
  3. 状态不是 `PENDING` 则抛 `DomainException("AI-INVOCATION-409", "ai.candidate.not-pending", "AI candidate is not pending: " + candidateId)`。
  4. 校验 `contentType/contentId/capability` 与候选一致，不一致抛 `DomainException("AI-INVOCATION-409", "ai.candidate.target-mismatch", "AI candidate target mismatch")`。
  5. 返回候选对象，不修改数据库。
- `markApplied(...)` 只更新候选 `resultFormat/resultPayload/status/appliedAt`，不写 Classics 内容。
- `reject(...)` 复用 `AiCandidate.reject(...)` 并更新候选。
- `markApplied(...)` 和 `reject(...)` 调用 `AiInvocationRepository.updateCandidate(candidate)`；返回值不是 `1` 时抛 `DomainException("AI-INVOCATION-409", "ai.candidate.update-failed", "AI candidate update failed: " + candidateId)`。

#### B1.3 Spring Bean 装配

新增文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/configuration/AiCandidateDomainServiceConfiguration.java`

实现要求：

- `AiCandidateDomainServiceConfiguration` 使用 `@Configuration`。
- 新增 `@Bean AiCandidateDomainService aiCandidateDomainService(AiInvocationRepository repository)`。
- 不在 `kuzhambu-ai-domain` 中新增 Spring 注解。

#### B1.4 AI 管理接口接入 DomainService

修改文件：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/AiInvocationController.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/request/AiInvocationRequests.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/assembler/AiInvocationInterfaceAssembler.java`

实现要求：

- 不新增 `POST /api/ai/invocation/candidate/apply`。
- 保留 `POST /api/ai/invocation/candidate/mark-applied` 不删除；本轮前端不调用它。
- `POST /api/ai/invocation/candidate/reject` 调用 `AiCandidateDomainService.reject(...)`。
- `POST /api/ai/invocation/candidate/mark-applied` 调用 `AiCandidateDomainService.markApplied(...)`。

跨域依赖：

- 本轮 Classics application 依赖 `kuzhambu-ai-domain`，并只使用 `AiCandidateDomainService`。
- 禁止 `kuzhambu-ai-application` 依赖 `kuzhambu-classics-application`。
- 禁止 `kuzhambu-classics-application` 依赖 `kuzhambu-ai-application`。

验收点：

- 应用已拒绝或已应用候选会失败。
- 应用时传入错误 `contentId` 或 `capability` 会失败。
- 应用成功后 `ai_candidate.status=APPLIED` 且 `applied_at` 非空。

### B2 Classics 候选应用命令、解析和持久化

目标：Classics 按候选 payload 写正式内容，并生成 `AI_APPLIED` 版本。

#### B2.1 新增命令和结果

新增文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/command/AiCandidateApplyContentCommand.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/result/AiCandidateApplyContentResult.java`

命令结构：

```java
public class AiCandidateApplyContentCommand {
    private Long candidateId;
    private ClassicsContentType contentType;
    private Long contentId;
    private String capability;
    private String resultFormat;
    private String resultPayload;
    private String changeSummary;
}
```

结果结构：

```java
public class AiCandidateApplyContentResult {
    private ClassicsContentType contentType;
    private Long contentId;
    private Long versionId;
    private Integer versionNo;
}
```

#### B2.2 新增 payload 解析器

新增文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsAiCandidatePayloadParser.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/AiCandidateQaPairPayload.java`

`AiCandidateQaPairPayload` 字段：

```java
public class AiCandidateQaPairPayload {
    private String question;
    private String answer;
}
```

`ClassicsAiCandidatePayloadParser` 方法固定为：

```java
public String parseText(String resultPayload);
public List<String> parseTags(String resultPayload);
public List<AiCandidateQaPairPayload> parseQaPairs(String resultPayload);
```

解析规则：

- `parseText(...)` 返回 `resultPayload.trim()`；空字符串抛 `BizException`。
- `parseTags(...)` 支持 `{"tags":["礼制","服饰"]}` 和 `["礼制","服饰"]`，按首次出现顺序去重，过滤空字符串。
- `parseQaPairs(...)` 支持 `{"qaPairs":[{"question":"...","answer":"..."}]}` 和 `[{"question":"...","answer":"..."}]`，按 `question + "\n" + answer` 首次出现顺序去重，过滤 question 或 answer 为空的项。
- `parseTags(...)` 和 `parseQaPairs(...)` 解析后为空时抛 `BizException`。
- JSON 解析使用 `ObjectMapper`，不得手写字符串拆分 JSON。

#### B2.3 Repository 窄方法

修改文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/repository/ClassicsContentRepository.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/repository/impl/ClassicsContentRepositoryImpl.java`

新增方法固定为：

```java
SancaiEntry getSancaiEntryForAiApply(ClassicsContentId contentId);
int updateSancaiEntryAiFields(SancaiEntry entry);
WangqiDocument getWangqiDocumentForAiApply(ClassicsContentId contentId);
int updateWangqiDocumentAiFields(WangqiDocument document);
MingCustomsEntry getMingCustomsEntryForAiApply(ClassicsContentId contentId);
int updateMingCustomsEntryAiFields(MingCustomsEntry entry);
int deleteAiTags(String contentType, ClassicsContentId contentId);
int deleteAiQaPairs(String contentType, ClassicsContentId contentId);
```

实现要求：

- `get*ForAiApply(...)` 读取对应主内容实体，不存在时返回 `null`。
- `updateSancaiEntryAiFields(...)` 只更新 `summary`、`translation_text`、`translation_status` 和 `updated_at`。
- `updateWangqiDocumentAiFields(...)` 只更新 `summary` 和 `updated_at`。
- `updateMingCustomsEntryAiFields(...)` 只更新 `summary` 和 `updated_at`。
- `deleteAiTags(...)` 只删除同一 `content_type/content_id` 且 `source='AI'` 的记录。
- `deleteAiQaPairs(...)` 只删除同一 `content_type/content_id` 且 `source='AI'` 的记录。

#### B2.4 ApplicationService 应用候选

修改文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/ClassicsContentApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`

`ClassicsContentApplicationService` 新增方法：

```java
AiCandidateApplyContentResult applyAiCandidate(AiCandidateApplyContentCommand command);
```

实现要求：

- `ClassicsContentApplicationServiceImpl` 构造参数新增 `AiCandidateDomainService`。
- `ClassicsContentApplicationServiceImpl` 新增字段 `private final ClassicsAiCandidatePayloadParser aiCandidatePayloadParser;`，在构造函数中实例化。
- `applyAiCandidate(...)` 先调用 `aiCandidateDomainService.requirePendingForApply(...)`，再写 Classics 内容。
- `applyAiResult(Versionable content, String changeSummary)` 实现为调用 `ensureVersioned(content, AI_APPLIED, changeSummary)`，不得再抛错。
- `applyAiCandidate(...)` 负责按 `contentType` 读取主内容、改字段、调用 repository 窄方法持久化、生成版本。
- 对 `SANCAI_ENTRY`：
  - `translate`：更新 `translationText`，同时将 `translationStatus` 设为 `SancaiEntryTranslationStatus.TRANSLATED`。
  - `summary`：更新 `summary`。
  - 使用 `ClassicsContentRepository.getSancaiEntryForAiApply(...)` 读取实体。
  - 使用 `ClassicsContentRepository.updateSancaiEntryAiFields(...)` 持久化字段。
- 对 `WANGQI_DOCUMENT`：
  - `summary`：更新 `summary`。
  - 使用 `ClassicsContentRepository.getWangqiDocumentForAiApply(...)` 读取实体。
  - 使用 `ClassicsContentRepository.updateWangqiDocumentAiFields(...)` 持久化字段。
- 对 `MING_CUSTOMS`：
  - `summary`：更新 `summary`。
  - 使用 `ClassicsContentRepository.getMingCustomsEntryForAiApply(...)` 读取实体。
  - 使用 `ClassicsContentRepository.updateMingCustomsEntryAiFields(...)` 持久化字段。
- 对 `tags`：
  - 解析 payload 得到去重后的 tagName 列表，空列表失败。
  - 删除同一 `contentType/contentId` 下 `source=AI` 的旧标签。
  - 逐个调用 `addTag(new ContentTagCommand(null, contentType, contentId, null, tagName, AI, ACTIVE))`。
  - 保留 `source=MANUAL` 标签不动。
- 对 `qa`：
  - 解析 payload 得到去重后的 question/answer 列表，空列表失败。
  - 删除同一 `contentType/contentId` 下 `source=AI` 的旧问答对。
  - 逐个调用 `addQaPair(new ContentQaPairCommand(null, contentType, contentId, question, answer, AI))`。
  - 保留 `source=MANUAL` 问答对不动。
- 每次成功应用后必须创建一条 `classics_content_version(change_type=AI_APPLIED)`。
- Classics 内容和版本写入成功后，再调用 `aiCandidateDomainService.markApplied(candidateId, resultFormat, resultPayload, Instant.now())`。
- 如果 Classics 写入失败，不得标记候选为 `APPLIED`。
- `change_summary` 默认值：
  - `translate` -> `AI 应用：译文`
  - `summary` -> `AI 应用：摘要`
  - `tags` -> `AI 应用：标签`
  - `qa` -> `AI 应用：问答对`
- `update*AiFields(...)` 返回值不是 `1` 时抛 `BizException`，不得继续调用 `markApplied(...)`。
- `get*ForAiApply(...)` 返回 `null` 时抛 `BizException`，不得继续调用 `markApplied(...)`。

#### B2.5 Interface 接口

修改文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/ClassicsContentAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/request/ClassicsContentRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/response/ClassicsContentResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/assembler/ClassicsContentInterfaceAssembler.java`

实现要求：

- 新增 `POST /api/classics/content/ai-candidates/apply`。
- `ClassicsContentRequest` 新增 `AiCandidateApplyRequest`，字段与 `AiCandidateApplyContentCommand` 一一对应。
- `ClassicsContentResponse` 新增 `AiCandidateApplyResponse`，字段与 `AiCandidateApplyContentResult` 一一对应。
- `ClassicsContentInterfaceAssembler` 新增 `toCommand(AiCandidateApplyRequest request)`。
- `ClassicsContentInterfaceAssembler` 新增 `toResponse(AiCandidateApplyContentResult result)`。
- Controller 方法只做请求映射、assembler 转换、调用 `ClassicsContentApplicationService.applyAiCandidate(...)`、返回 response。

验收点：

- AI 应用不会删除人工标签和人工问答对。
- AI 应用会生成 `AI_APPLIED` 版本。
- 应用 unsupported capability 返回业务异常，不更新候选状态。

### B3 后端接口测试

新增或修改测试：

- `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/test/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/AiInvocationControllerTest.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/test/java/com/thundax/kuzhambu/ai/domain/invocation/service/AiCandidateDomainServiceTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/content/ClassicsContentAdminControllerTest.java`

测试用例：

- `/api/classics/content/ai-candidates/apply` 请求体字段映射正确。
- PENDING 候选应用成功后状态为 APPLIED。
- REJECTED 候选应用失败。
- `summary` 应用到 Sancai/Wangqi/MingCustoms 后生成 `AI_APPLIED` 版本。
- `tags` 只替换 AI 来源标签，不影响 MANUAL。
- `qa` 只替换 AI 来源问答对，不影响 MANUAL。

## Frontend Task Blocks

### F1 AI 候选 API

新增文件：

- `kuzhambu-apps/admin-web/src/api/ai/ai-candidate-service.ts`
- `kuzhambu-apps/admin-web/src/api/ai/ai-candidate-types.ts`

接口：

```ts
export interface AiCandidateRecord {
    candidateId: number;
    callId?: number | null;
    capability: string;
    contentType: string;
    contentId: number;
    objectId?: number | null;
    resultFormat: string;
    resultPayload?: string | null;
    status: "PENDING" | "APPLIED" | "REJECTED" | string;
    promptVersionId?: number | null;
    modelName?: string | null;
    errorType?: string | null;
    errorMessage?: string | null;
    requestedAt?: string | null;
    appliedAt?: string | null;
}

export interface AiCandidateApplyCommand {
    candidateId: number;
    contentType: string;
    contentId: number;
    capability: string;
    resultFormat: string;
    resultPayload: string;
    changeSummary?: string | null;
}
```

方法：

- `listCandidates({ contentType, contentId, capability, status })` -> `POST /ai/invocation/candidate/list`
- `applyCandidate(command)` -> `POST /classics/content/ai-candidates/apply`
- `rejectCandidate({ candidateId, errorType, errorMessage })` -> `POST /ai/invocation/candidate/reject`

本轮前端不新增 AI refinement 触发 API 文件，不调用 `/ai/refinement/*`。

### F2 通用候选面板

新增文件：

- `kuzhambu-apps/admin-web/src/components/ai/ai-candidate-panel.tsx`
- `kuzhambu-apps/admin-web/src/components/ai/ai-candidate-payload-editor.tsx`

组件职责：

- 展示 PENDING 候选列表。
- 按 capability 渲染编辑器：
  - `summary`、`translate`：`Input.TextArea`。
  - `tags`：一行一个标签，保存时转为 `{"tags":[...]}`。
  - `qa`：问题/答案成对编辑，保存时转为 `{"qaPairs":[...]}`。
- 提供“应用”和“拒绝”按钮。
- 应用成功后刷新候选列表，并调用父组件 `onApplied()` 刷新当前内容详情。

组件入参：

```ts
interface AiCandidatePanelProps {
    contentType: "SANCAI_ENTRY" | "WANGQI_DOCUMENT" | "MING_CUSTOMS";
    contentId: number;
    capabilities: Array<"translate" | "summary" | "tags" | "qa">;
    onApplied?: () => void;
}
```

### F3 接入 Classics 页面

修改文件：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/components/wangqi-document-model.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/components/ming-customs-model.tsx`

接入规则：

- Sancai 详情接入 capabilities：`translate`、`summary`、`tags`、`qa`。
- Wangqi 详情接入 capabilities：`summary`、`tags`、`qa`。
- MingCustoms 详情接入 capabilities：`summary`、`tags`、`qa`。
- 本轮页面只展示、编辑、应用、拒绝已有候选，不新增生成按钮和 AI 触发配置表单。

验收点：

- 应用候选后主详情刷新，summary/translation/tags/qa 展示最新值。
- 拒绝候选后候选列表不再展示该 PENDING 候选。

## Validation Commands

Java 后端：

```sh
cd kuzhambu-servers
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/ai/kuzhambu-ai-application,biz/ai/kuzhambu-ai-interface,biz/classics/kuzhambu-classics-application -am test
```

前端：

```sh
cd kuzhambu-apps
npm run format:check
npm run lint
npm --workspace admin-web run test
```

## Implementation Order

1. B0：先同步架构门禁，撤销跨域 repository port 禁令。
2. B1：抽出 AI 候选 DomainService，供 Classics 确认用例读取、校验和标记候选状态。
3. B2：让 Classics 能独立应用候选 payload 并生成版本。
4. B3：锁后端契约和核心状态机。
5. F1：补前端 AI API。
6. F2：做通用候选面板。
7. F3：接入三类 Classics 页面。

## Out Of Scope

- 不实现批量候选应用。
- 不实现视觉资产、图片理解、生图、条目拆分确认。
- 不新增 AI 模型选择、提示词选择、变量编辑 UI。
- 不改 workers usecase path。
- 不改 Knowledge 标签治理模型；AI 标签先落 `tag_id=null` 和 `tag_name_snapshot`。
- 不改权限过滤策略。
