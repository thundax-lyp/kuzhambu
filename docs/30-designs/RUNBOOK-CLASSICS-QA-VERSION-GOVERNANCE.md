# RUNBOOK Classics QA Version Governance

## Goal

完成 Classics common QA、王圻文档编辑页和明代习俗编辑页的问答对版本化治理闭环。

目标态：用户在 Wangqi/Ming 内容上下文内确认新增、编辑、删除或 AI 应用问答对后，Classics 生成正式版本，更新来源内容当前版本标定，并触发 Discovery QA Knowledge Base 只读同步。未确认候选、排序、导出、静态展示和 Operations 不进入本闭环。

## Scope

包含：

- `classics_content_qa_pair` 通用问答对正式事实。
- `classics_content_version` 正式版本、快照、版本号和变更类型。
- `classics_wangqi_document` 与 `classics_ming_customs_entry` 当前版本标定。
- Wangqi/Ming 编辑页的问答对控件、AI 候选确认/放弃控件和版本历史控件。
- Discovery QA Knowledge Base 对已确认公开内容的只读同步。

不包含：

- `classics_content_export_job`、导出模板、导出记录和 render workers 导出链路。
- `classics_sancai_showcase` 与三才图会静态展示页面。
- `operations_*` 表、Operations 周报、月报、统计聚合、备份恢复或运维台账。
- Knowledge 图谱、实体关系、数据精修工作台和质量报告。
- Portal 跨库智能问答会话和回答生成体验。

## Data Contract

### Data Change Matrix

| Target | Change Type | Exact Fields |
| --- | --- | --- |
| `db/schema/classics.sql` | No DDL column change | Reuse `classics_content_qa_pair`, `classics_content_version`, `classics_wangqi_document.current_version_*`, `classics_ming_customs_entry.current_version_*` |
| `ClassicsContentChangeType` | Java enum value | Add `QA_CHANGED` |
| `WangqiDocumentVersionSnapshot` | JSON snapshot structure | Add `tags`, `qaPairs` |
| `MingCustomsVersionSnapshot` | JSON snapshot structure | Add `tags`, `qaPairs` |
| `ClassicsContentQaPairPayload` | TypeScript request payload | Add delete request shape if absent: `{ id: number }` |
| `WangqiVersionSnapshot` | TypeScript snapshot type | Add `tags`, `qaPairs` |
| `MingCustomsVersionSnapshot` | TypeScript snapshot type | Add `tags`, `qaPairs` |

### Existing Tables

`db/schema/classics.sql` 中下列表结构保持为本任务真相源。

`classics_content_qa_pair` 字段：

| Field | Type | Rule |
| --- | --- | --- |
| `id` | `bigint` | PK, AUTO_INCREMENT |
| `content_type` | `varchar(32)` | `WANGQI_DOCUMENT` 或 `MING_CUSTOMS`，本任务不新增类型 |
| `content_id` | `bigint` | Wangqi document id 或 Ming customs entry id |
| `question` | `text` | 用户确认后的问题，非空 |
| `answer` | `longtext` | 用户确认后的答案，非空 |
| `source` | `varchar(16)` | `MANUAL` 或 `AI_EXTRACTED`；AI 候选确认应用后落 `AI_EXTRACTED` |
| `priority` | `int` | 单表全局排序，不作为版本或同步幂等键 |

`classics_content_version` 字段：

| Field | Type | Rule |
| --- | --- | --- |
| `id` | `bigint` | PK, AUTO_INCREMENT |
| `content_type` | `varchar(32)` | `WANGQI_DOCUMENT` 或 `MING_CUSTOMS` |
| `content_id` | `bigint` | 来源内容 ID |
| `version_no` | `int` | 按 `content_type + content_id` 递增 |
| `versioned_at` | `datetime(3)` | 用户确认动作生成版本的时间 |
| `snapshot_json` | `json` | 目标态快照，必须含主内容、标签快照、已确认问答对 |
| `change_type` | `varchar(32)` | 本任务新增枚举值 `QA_CHANGED` |
| `change_summary` | `varchar(512)` | 面向版本历史展示的结果摘要 |

`classics_wangqi_document` 当前版本字段：

- `current_version_id`
- `current_version_no`
- `current_versioned_at`
- `content_updated_at`

`classics_ming_customs_entry` 当前版本字段：

- `current_version_id`
- `current_version_no`
- `current_versioned_at`
- `content_updated_at`

### Required Enum Change

文件：`kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/model/enums/ClassicsContentChangeType.java`

新增枚举：

```java
QA_CHANGED
```

使用规则：

- 手工新增、编辑、删除问答对使用 `QA_CHANGED`。
- AI 候选一次确认应用仍使用 `AI_APPLIED`，即使 payload 同时包含摘要、标签和问答对。
- 主内容手动保存继续使用 `MANUAL_SAVE`。
- 历史恢复继续使用 `HISTORY_RESTORED`。
- `SHARE_CREATED` 不应由本任务新增使用点；若无业务使用，应另起治理任务处理，不在本轮清理。

### Snapshot Shape

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/WangqiDocumentVersionSnapshot.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/MingCustomsVersionSnapshot.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsContentSnapshotAssembler.java`

Wangqi `snapshot_json` 必须包含：

- `contentType`
- `contentId`
- `contentUpdatedAt`
- `title`
- `summary`
- `contentFormat`
- `content`
- `documentTime`
- `storageObjectId`
- `visibility`
- `tags`: array of `{ id, tagId, tagNameSnapshot, source, status, priority }`
- `qaPairs`: array of `{ id, question, answer, source, priority }`

Ming `snapshot_json` 必须包含：

- `contentType`
- `contentId`
- `contentUpdatedAt`
- `title`
- `category`
- `chapter`
- `section`
- `summary`
- `contentFormat`
- `content`
- `originalExcerpts`
- `visibility`
- `tags`: array of `{ id, tagId, tagNameSnapshot, source, status, priority }`
- `qaPairs`: array of `{ id, question, answer, source, priority }`

历史恢复规则：

- 恢复必须以 `snapshot_json` 为准覆盖主内容、标签快照和已确认问答对。
- 恢复后生成新的 `classics_content_version`，`change_type = HISTORY_RESTORED`。
- 恢复不得恢复未确认 AI 候选、导出记录、静态展示记录、Discovery provider external id 或 Operations 台账。

## Task Breakdown

### Task 1: Backend QA Versioning

目标：common QA 的手工新增、编辑、删除都生成 `QA_CHANGED` 版本，并更新当前版本标定。

核心文件，控制在 5 个以内：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/model/enums/ClassicsContentChangeType.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/ClassicsContentApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/ClassicsContentAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/request/ClassicsContentRequest.java`

实现要求：

- `addQaPair`、`updateQaPair`、`deleteQaPair` 成功后调用统一治理方法，传入 `ClassicsContentChangeType.QA_CHANGED` 和摘要：
  - 新增：`新增问答对`
  - 编辑：`更新问答对`
  - 删除：`删除问答对`
- 统一治理方法必须先更新 `content_updated_at`，再生成版本，再持久化 `current_version_*` 标定，再发布 Discovery/Search 同步信号。
- `sortQaPairs` 只保存 `priority`，不得生成版本；若 Discovery `knowledgeRevision` 当前包含 QA 顺序，排序成功后可以触发同步，但不得改 `current_version_*`。
- `ClassicsContentAdminController` 必须提供通用删除接口：`POST /classics/content/qa-pairs/delete`，请求体使用 `id`，删除成功返回 `true`。
- 权限仍由 existing `ClassicsContentPermissionSupport`/controller 权限口径承接，不新增第二套权限判断。

测试文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/ClassicsContentAdminControllerTest.java`

### Task 2: Snapshot And Restore

目标：Wangqi/Ming 版本快照包含标签和问答对，历史恢复能恢复这些上下文内容。

核心文件，控制在 5 个以内：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/WangqiDocumentVersionSnapshot.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/MingCustomsVersionSnapshot.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsContentSnapshotAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/support/WangqiDocumentVersionRestorer.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`

实现要求：

- `ClassicsContentSnapshotAssembler.toSnapshotJson(...)` 为 `WANGQI_DOCUMENT` 和 `MING_CUSTOMS` 装配当前有效 `classics_content_tag` 和 `classics_content_qa_pair`。
- `WangqiDocumentVersionSnapshot` 和 `MingCustomsVersionSnapshot` 增加 `tags`、`qaPairs` 字段及 `from(JsonNode)` 解析能力。
- `WangqiDocumentVersionRestorer` 恢复 Wangqi 主内容后，同步用快照替换该内容的标签和问答对。
- Ming 现有恢复逻辑在 `ClassicsContentApplicationServiceImpl.restoreMingCustomsFromSnapshot(...)`，必须同步用快照替换该内容的标签和问答对。
- 替换标签时仍遵守 Knowledge 标签协作语义；不得把 `tag_name_snapshot` 直接回写 Knowledge。
- 替换问答对时只写 `classics_content_qa_pair`，不得写 Discovery 或 Knowledge 表。

测试文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsContentSnapshotAssemblerTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/wangqi/WangqiDocumentApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/mingcustoms/MingCustomsApplicationServiceImplTest.java`

### Task 3: AI Apply And Discovery Sync

目标：AI 候选确认应用按一次用户确认生成一个版本，Discovery 只读消费当前 Classics 正式内容。

核心文件，控制在 5 个以内：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsAiCandidatePayloadParser.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsQaKnowledgeFacadeDto.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/facade/assembler/ClassicsFacadeAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeRevisionCalculator.java`

实现要求：

- `applyAiCandidate(...)` 对同一次候选确认只生成一个 `AI_APPLIED` 版本。
- 若 AI payload 同时包含 `summary`、`tags`、`qaPairs`，必须先落 Classics 正式事实，再基于最终态生成一个版本快照。
- `rejectAiCandidates(...)` 与单个候选拒绝只更新 AI 候选状态，不改 `classics_content_qa_pair`、不生成版本、不触发 Discovery 同步。
- `ClassicsQaKnowledgeFacadeDto.qaPairs` 只返回已确认并落库的问答对。
- Discovery `knowledgeRevision` 必须包含主 knowledge fields、确认标签和确认问答对；不包含 `id`、`priority`、provider external id、导出状态、静态展示状态。
- 私有 Wangqi/Ming 内容进入同步链路时必须产生 delete/disable 语义，不得 upsert 到默认 QA Knowledge Base。

测试文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/facade/impl/ClassicsFacadeImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeRevisionCalculatorTest.java`

### Task 4: Admin Web QA Controls

目标：Wangqi/Ming 编辑页在控件级完成问答对治理、AI 候选确认/放弃和页面刷新。

核心文件，控制在 5 个以内：

- `kuzhambu-apps/admin-web/src/pages/classics/common/components/classics-content-qa-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-content-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-content-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.tsx`

控件与操作要求：

- `ClassicsContentQaPanel` 展示区域标题为 `问答对治理`，Wangqi 传入 `王圻问答对`，Ming 传入 `明代习俗问答对`。
- 表格 `ariaLabel="问答对列表"`，列保持：`问题`、`答案`、`来源`、`操作`。
- `新增问答对` 按钮打开 `Modal`，弹窗标题 `新增问答对`。
- 弹窗内控件：
  - `Input.TextArea aria-label="问答问题"`，必填，空值提示 `请输入问题`。
  - `Input.TextArea aria-label="问答答案"`，必填，空值提示 `请输入答案`。
  - `Select aria-label="问答来源"`，选项 `MANUAL/手工`、`AI_EXTRACTED/AI 提取`；手工新增默认 `MANUAL`。
- 表格每行 `编辑` 按钮打开 `Modal`，弹窗标题 `编辑问答对`，保存成功后刷新问答对、当前内容详情和版本历史。
- 新增每行 `删除` 按钮，按钮可访问名称为 `删除问答对 {id}`；点击后使用 `useKuzhambuConfirm` 二次确认，确认标题 `确认删除问答对`，确认文案 `删除后会生成新的正式版本。是否继续？`。
- 删除成功提示 `问答对已删除`，并刷新问答对、当前内容详情和版本历史。
- 拖拽/排序保存成功提示 `问答对顺序已保存`；排序不得展示“已生成版本”的文案。
- `AiCandidatePanel` 中 `应用` 按钮成功后必须触发 Wangqi/Ming 页面 `invalidate`：刷新内容详情、问答对列表、标签列表、版本历史和候选列表。
- `AiCandidatePanel` 中 `拒绝` 按钮成功后只刷新候选列表，不刷新版本历史，不展示“已生成版本”。

测试文件：

- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-content-service-contract.test.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/components/classics-content-qa-panel.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.test.tsx`

### Task 5: Admin Web Version Snapshot Display

目标：Wangqi/Ming 版本历史面板能展示新增的 `tags` 和 `qaPairs` 快照摘要。

核心文件，控制在 5 个以内：

- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/components/wangqi-version-history-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/components/ming-customs-version-history-panel.tsx`

控件与操作要求：

- Wangqi 版本历史面板 `aria-label="王圻版本历史面板"` 保持不变。
- Ming 版本历史面板 `aria-label="明代习俗版本历史面板"` 保持不变。
- `查看王圻版本 {versionNo}`、`恢复王圻版本 {versionNo}`、`查看明代习俗版本 {versionNo}`、`恢复明代习俗版本 {versionNo}` 的可访问名称保持不变。
- 选中版本后，在版本详情区域新增 `确认标签` 展示：用 tag 文本列表展示 `tags[].tagNameSnapshot`；为空显示 `-`。
- 选中版本后，在版本详情区域新增 `确认问答` 展示：用紧凑列表展示 `Q: {question}` 和 `A: {answer}`；为空显示 `-`。
- `snapshot_json` 为空或无法解析时仍展示现有警告 `版本快照为空或无法解析`，恢复按钮禁用规则保持不变。

测试文件：

- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.test.tsx`

## Acceptance

- Wangqi 文档手工新增、编辑、删除问答对后，新增一条 `classics_content_version`，`change_type = QA_CHANGED`，并更新 `classics_wangqi_document.current_version_id/current_version_no/current_versioned_at/content_updated_at`。
- Ming Customs 条目手工新增、编辑、删除问答对后，新增一条 `classics_content_version`，`change_type = QA_CHANGED`，并更新 `classics_ming_customs_entry.current_version_id/current_version_no/current_versioned_at/content_updated_at`。
- AI 候选问答对只有在用户点击 `应用` 后才进入 `classics_content_qa_pair` 和版本快照；点击 `拒绝` 不改变正式内容和版本。
- 一次 AI `应用` 同时改摘要、标签和问答对时只生成一个 `AI_APPLIED` 版本。
- 历史恢复能恢复主内容、标签快照和已确认问答对，并生成新的 `HISTORY_RESTORED` 版本。
- Discovery QA Knowledge Base 同步正文只包含公开当前态知识、确认标签和确认问答对；私有 Wangqi/Ming 内容不 upsert。
- 本轮 diff 不包含 `classics_content_export_job`、`classics_sancai_showcase`、`operations_*`、render worker 导出代码或 Operations 前端页面。

## Validation

后端：

- `ClassicsContentApplicationServiceImplTest` 覆盖 QA 新增、编辑、删除生成 `QA_CHANGED` 版本。
- `ClassicsContentApplicationServiceAiCandidateTest` 覆盖 AI 应用聚合为单版本、候选拒绝不生成版本。
- `WangqiDocumentApplicationServiceImplTest` 覆盖 Wangqi 历史恢复包含标签和问答对。
- `MingCustomsApplicationServiceImplTest` 覆盖 Ming 历史恢复包含标签和问答对。
- `KnowledgeRevisionCalculatorTest` 覆盖确认问答对变化会改变 `knowledgeRevision`，排序和 id 不影响 revision。

前端：

- `classics-content-service-contract.test.ts` 覆盖 `POST /classics/content/qa-pairs/delete`。
- 新增或扩展 `classics-content-qa-panel` 组件测试，覆盖新增、编辑、删除确认、必填校验和排序不生成版本文案。
- `wangqi-page.test.tsx` 覆盖 AI 应用后刷新版本历史，AI 拒绝不刷新版本历史。
- `ming-customs-page.test.tsx` 覆盖问答对删除确认和版本历史展示 tags/qaPairs 摘要。

命令：

```sh
cd kuzhambu-servers
mvn -pl biz/classics -am spotless:apply
mvn -pl biz/classics -am test
mvn spotless:check
mvn checkstyle:check
```

```sh
cd kuzhambu-apps
pnpm --filter kuzhambu-admin-web run format
pnpm run format:check
pnpm run lint
pnpm run test
```
