# RUNBOOK Knowledge 标签批量治理闭环

## 目标

把 Knowledge taxonomy 的 `标签批量操作` 从未完成推进到已完成。

最终交付状态：

- 批量合并：在统一标签列表选择多个源标签，选择一个目标标签，预览影响后一次性合并。
- 批量废弃：在统一标签列表选择多个标签，二次确认后一次性废弃。
- 批量审核：在待审核标签列表选择多个标签，一次性通过或拒绝；批量通过时统一指定一个正式分类。
- Admin 控件：`/knowledge/taxonomy` 原页面内完成全部批量操作，不新增菜单或页面。
- 测试闭环：后端 application、后端 interface、Admin service、Admin 页面交互均有测试。
- 文档收口：全部实现和验证完成后，`docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md` 直接把 `标签批量操作` 更新为 `已完成`，不记录中间状态。

## 已确认口径

- 批量合并只支持 `多个源标签 -> 一个目标标签`，不支持一次请求内多组映射。
- 批量审核通过时所有标签使用同一个 `categoryId`。
- 批量动作先完整校验，再写入；遇到任一非法数据整体失败，不做部分成功。
- 不新增权限点，沿用 `knowledge:taxonomy:view`、`knowledge:taxonomy:edit`、`knowledge:taxonomy:review`。
- 不新增批量任务台账，不做异步批任务。
- 不新增数据库表，不修改现有数据库字段。

## 非目标

- 不改变单条标签治理接口语义。
- 不调整同义词治理。
- 不调整 Classics 页面内联标签治理。
- 不补 Playwright 或跨服务联调冒烟；本次只锁定单元/组件测试和构建验证。
- 不在 coverage、治理文档或 TODO 中记录执行过程。

## 数据结构变更

### 数据库

无数据库表结构变更。

沿用字段：

- `knowledge_tag.merged_to_tag_id`：批量合并时每个源标签写入目标标签 ID。
- `knowledge_tag.status`：批量废弃时写入 `DISABLED`。
- `knowledge_tag.deprecated_at`：批量废弃时写入废弃时间。
- `knowledge_tag.deprecated_by`：本次不新增操作者来源；继续按现有单条废弃口径写入 `null`。
- `knowledge_tag.review_status`：批量审核通过写入 `APPROVED`，批量审核拒绝写入 `REJECTED`。
- `knowledge_tag.review_note`：批量审核写入统一审核备注。
- `knowledge_tag.reviewed_at`：批量审核写入审核时间。
- `knowledge_tag.category_id`：批量审核通过时写入统一正式分类；批量拒绝不要求写入分类。

### Java Application DTO

新增文件和字段：

- `TagBatchMergeCommand.java`
  - `List<TagId> sourceTagIds`
  - `TagId targetTagId`
- `TagBatchMergePreviewQuery.java`
  - `List<TagId> sourceTagIds`
  - `TagId targetTagId`
- `TagBatchMergePreviewResult.java`
  - `List<TagResult> sourceTags`
  - `TagResult targetTag`
  - `List<TagAliasResult> aliasesToMerge`
  - `List<TagContentRefResult> impactedContentRefs`
  - `int pendingReviewCount`
  - `int governedRecordCount`
- `TagBatchDeprecateCommand.java`
  - `List<TagId> tagIds`
- `TagBatchReviewCommand.java`
  - `List<TagId> tagIds`
  - `String decision`
  - `TagCategoryId categoryId`
  - `String reviewNote`

### Java Interface DTO

新增文件和 JSON 字段：

- `TagBatchMergeRequest.java`
  - `sourceTagIds: string[]`
  - `targetTagId: string`
- `TagBatchMergePreviewResponse.java`
  - `sourceTags: TagResponse[]`
  - `targetTag: TagResponse`
  - `aliasesToMerge: TagAliasResponse[]`
  - `impactedContentRefs: TagContentRefResponse[]`
  - `pendingReviewCount: number`
  - `governedRecordCount: number`
- `TagBatchDeprecateRequest.java`
  - `tagIds: string[]`
- `TagBatchReviewRequest.java`
  - `tagIds: string[]`
  - `decision: "APPROVE" | "REJECT"`
  - `categoryId?: string`
  - `reviewNote?: string`

Validation 要求：

- `sourceTagIds`、`tagIds` 使用 `@NotEmpty`，字段类型使用 `List<@Size(max = 64) String>`。
- `decision` 使用 `@NotEmpty` 和 `@Size(max = 16)`。
- `reviewNote` 使用 `@Size(max = 512)`。
- `categoryId` 使用 `@Size(max = 64)`。

### Admin Web Types

在 `taxonomy-service.ts` 新增并由页面域使用：

- `TagBatchMergeCommand`
  - `sourceTagIds: string[]`
  - `targetTagId: string`
- `TagBatchDeprecateCommand`
  - `tagIds: string[]`
- `TagBatchReviewCommand`
  - `tagIds: string[]`
  - `decision: "APPROVE" | "REJECT"`
  - `categoryId?: string | null`
  - `reviewNote?: string | null`

在 `taxonomy-types.ts` 新增：

- `TagBatchMergePreviewRecord`
  - `sourceTags: TagRecord[]`
  - `targetTag: TagRecord`
  - `aliasesToMerge: TagAliasRecord[]`
  - `impactedContentRefs: TagContentRefRecord[]`
  - `pendingReviewCount?: number | null`
  - `governedRecordCount?: number | null`

### Admin Web Page State

在 `taxonomy-page.tsx` 新增状态：

- `selectedTagRowKeys: Key[]`
- `selectedReviewRowKeys: Key[]`
- `tagBatchMergeOpen: boolean`
- `tagBatchReviewOpen: boolean`
- `tagBatchReviewDecision: "APPROVE" | "REJECT" | null`
- `tagBatchMergePreview: TagBatchMergePreviewRecord | null`

在 `taxonomy-page.tsx` 新增 mutation：

- `previewTagBatchMergeMutation` 调用 `service.previewTagBatchMergeImpact`
- `applyTagBatchMergeMutation` 调用 `service.applyTagBatchMerge`
- `batchDeprecateTagsMutation` 调用 `service.batchDeprecateTags`
- `batchReviewTagsMutation` 调用 `service.batchReviewTags`

刷新范围：

- 批量合并成功：invalidate `tags`、`reviews`、`metrics`、`tag-detail`。
- 批量废弃成功：invalidate `tags`、`reviews`、`metrics`、`tag-detail`。
- 批量审核成功：invalidate `reviews`、`tags`、`metrics`、`tag-detail`。
- 批量动作成功后必须清空对应 selected row keys。

## 接口契约

新增 HTTP API：

| 接口 | 权限 | 请求 | 响应 |
| --- | --- | --- | --- |
| `POST /api/knowledge/taxonomy/tag/merge/batch-preview` | `knowledge:taxonomy:view` | `TagBatchMergeRequest` | `TagBatchMergePreviewResponse` |
| `POST /api/knowledge/taxonomy/tag/merge/batch-apply` | `knowledge:taxonomy:edit` | `TagBatchMergeRequest` | `Boolean` |
| `POST /api/knowledge/taxonomy/tag/deprecate/batch` | `knowledge:taxonomy:edit` | `TagBatchDeprecateRequest` | `Boolean` |
| `POST /api/knowledge/taxonomy/tag/review/batch` | `knowledge:taxonomy:review` | `TagBatchReviewRequest` | `Boolean` |

失败语义：

- ID 列表为空：失败。
- 去重后列表为空：失败。
- 批量合并源标签包含目标标签：失败。
- 任一源标签、目标标签或待审核标签不存在：失败。
- 任一源标签或目标标签不可用于新绑定：失败。
- 任一批量废弃标签已废弃：失败。
- 任一批量审核标签不是 `PENDING`：失败。
- 批量审核通过但 `categoryId` 缺失或分类不是启用状态：失败。

## 小任务拆分

### 任务 1：后端 Application 批量契约

范围文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagBatchMergeCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagBatchDeprecateCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagBatchReviewCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/query/TagBatchMergePreviewQuery.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/result/TagBatchMergePreviewResult.java`

处理动作：

- 新增 5 个 application DTO。
- DTO 使用现有 Lombok 风格。
- 字段严格匹配“数据结构变更”章节。

验收点：

- DTO 可被 `TaxonomyApplicationService` 引用。
- 不引入 interface 或 infra 依赖。

### 任务 2：后端 Application 行为实现

范围文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/TaxonomyApplicationService.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/repository/TagRepository.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/repository/impl/TagRepositoryImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/persistence/mapper/TagMapper.java`

处理动作：

- `TaxonomyApplicationService` 增加 `previewTagBatchMergeImpact`、`applyTagBatchMerge`、`batchDeprecateTags`、`batchReviewTags`。
- `TaxonomyApplicationServiceImpl` 增加批量校验和写入方法。
- 如实现需要，`TagRepository` / `TagRepositoryImpl` / `TagMapper` 增加 `listByTagIds(List<TagId> tagIds)`。
- 批量写入方法使用 `@Transactional(rollbackFor = Exception.class)`。

验收点：

- 批量合并先校验全部源标签和目标标签，再写入每个源标签的 `mergedToTagId`，并同步每个源标签历史内容引用到目标标签。
- 批量废弃先校验全部标签，再复用 `Tag.deprecate(new Date(), null)`。
- 批量审核先校验全部标签，再统一写入审核结果。
- 任一校验失败时没有 repository update 调用发生。

### 任务 3：后端 Interface 契约与映射

范围文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagBatchMergeRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagBatchDeprecateRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagBatchReviewRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagBatchMergePreviewResponse.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/assembler/KnowledgeTaxonomyInterfaceAssembler.java`

处理动作：

- 新增 3 个 request 和 1 个 response。
- 在 `KnowledgeTaxonomyInterfaceAssembler` 增加 request -> command/query、result -> response 映射。
- 字段、校验注解和 JSON 名称严格匹配“数据结构变更”章节。

验收点：

- 批量接口不复用单条 request。
- response 内复用 `TagResponse`、`TagAliasResponse`、`TagContentRefResponse`。

### 任务 4：后端 Controller 与测试

范围文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/KnowledgeTaxonomyController.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/KnowledgeTaxonomyControllerTest.java`

处理动作：

- 在 controller 增加 4 个批量接口。
- application 测试覆盖批量合并、批量废弃、批量审核的成功与整体失败。
- controller 测试覆盖请求字段到 command/query 的映射。

验收点：

- 每个新增接口的 `@HasPermission` 与接口契约一致。
- `@SysLogger` 文案分别表达 `批量预览标签合并`、`批量执行标签合并`、`批量废弃标签`、`批量审核标签`。
- 测试能证明批量失败不会执行部分写入。

### 任务 5：Admin Web Service 与类型

范围文件：

- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-service.ts`
- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-types.ts`
- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-service.test.ts`

处理动作：

- 新增 `TagBatchMergeCommand`、`TagBatchDeprecateCommand`、`TagBatchReviewCommand`。
- 新增 `TagBatchMergePreviewRecord`。
- 新增 service 方法：
  - `previewTagBatchMergeImpact`
  - `applyTagBatchMerge`
  - `batchDeprecateTags`
  - `batchReviewTags`
- 更新 service 测试，断言 URL 和 payload。

验收点：

- URL 精确对应新增 HTTP API。
- request payload 字段为 `sourceTagIds`、`targetTagId`、`tagIds`、`decision`、`categoryId`、`reviewNote`。
- 不把后端 response 类型写进页面组件文件。

### 任务 6：Admin Web 统一标签批量控件

范围文件：

- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/tag-table.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/tag-batch-merge-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-page.css`
- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-page.test.tsx`

控件和操作：

- `TagTable` props 新增：
  - `selectedRowKeys: Key[]`
  - `onSelectedRowKeysChange: (keys: Key[]) => void`
  - `onBatchMerge: () => void`
  - `onBatchDeprecate: () => void`
- `TagTable` 通过 `KuzhambuTable` 的 `rowSelection` 增加多选框列：
  - `selectedRowKeys` 绑定页面状态。
  - `onChange` 调用 `onSelectedRowKeysChange`。
  - `getCheckboxProps` 对不可编辑状态禁用。
- `TagTable` toolbar 左侧显示 `已选择 N 个统一标签`，N 来自 `selectedRowKeys.length`。
- `TagTable` toolbar 增加 `批量合并` Button。
  - 未选中时 disabled。
  - 点击后打开 `TagBatchMergePanel`。
- `TagTable` toolbar 增加 `批量废弃` danger Button。
  - 未选中时 disabled。
  - 放在批量操作末尾。
  - 点击后调用 `useKuzhambuConfirm`，确认文案包含选中数量。
- `TagBatchMergePanel` 使用 `KuzhambuDrawer` 承载，不使用页面内常驻 Card。
- `TagBatchMergePanel` props：
  - `open: boolean`
  - `sourceTags: TagRecord[]`
  - `candidateTargetTags: TagRecord[]`
  - `preview?: TagBatchMergePreviewRecord | null`
  - `previewing: boolean`
  - `applying: boolean`
  - `onClose: () => void`
  - `onPreview: (request: TagBatchMergeCommand) => void`
  - `onApply: (request: TagBatchMergeCommand) => void`
- `TagBatchMergePanel` 控件：
  - Drawer 标题：`批量合并标签`。
  - 已选源标签摘要：展示 `sourceTags.length` 和前 5 个源标签名，超出显示 `等 N 个`。
  - `Select`：字段 `targetTagId`，aria-label 为 `批量合并目标标签`，options 来自 `candidateTargetTags`，必须排除源标签。
  - `Button`：文本 `预览影响`，未选择 `targetTagId` 时 disabled，点击提交 `{ sourceTagIds, targetTagId }`。
  - 预览区 `Descriptions`：展示源标签数、目标标签名、待迁移别名数、受影响内容引用数、待审核数、治理记录数。
  - 预览明细列表：别名列表展示 `name/source`；内容引用列表展示 `contentTitle/contentType/source`。
  - `Button`：文本 `执行合并`，danger + primary，未生成预览时 disabled。
  - `Button`：文本 `取消`，点击 `onClose`。
- 批量合并成功后关闭 panel、清空选择、刷新 tags/reviews/metrics/tag-detail。
- 批量废弃成功后清空选择、刷新 tags/reviews/metrics/tag-detail。

验收点：

- 所有无纯文本按钮必须有 `aria-label`。
- 选中项翻页后不保留不可见页选择。
- 合并目标不能选择当前已选源标签。

### 任务 7：Admin Web 待审核批量控件

范围文件：

- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/tag-review-table.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/tag-batch-review-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-page.css`
- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-page.test.tsx`

控件和操作：

- `TagReviewTable` props 新增：
  - `selectedRowKeys: Key[]`
  - `onSelectedRowKeysChange: (keys: Key[]) => void`
  - `onBatchApprove: () => void`
  - `onBatchReject: () => void`
- `TagReviewTable` 通过 `KuzhambuTable` 的 `rowSelection` 增加多选框列：
  - `selectedRowKeys` 绑定页面状态。
  - `onChange` 调用 `onSelectedRowKeysChange`。
- `TagReviewTable` toolbar 左侧显示 `已选择 N 个待审核标签`，N 来自 `selectedRowKeys.length`。
- `TagReviewTable` toolbar 增加 `批量通过` Button。
  - 未选中时 disabled。
  - 点击后打开 `TagBatchReviewPanel`，模式为 `APPROVE`。
- `TagReviewTable` toolbar 增加 `批量拒绝` danger Button。
  - 未选中时 disabled。
  - 点击后打开 `TagBatchReviewPanel`，模式为 `REJECT`。
- `TagBatchReviewPanel` 使用 `KuzhambuDrawer` 承载。
- `TagBatchReviewPanel` props：
  - `open: boolean`
  - `decision: "APPROVE" | "REJECT"`
  - `selectedTags: TagRecord[]`
  - `categories: TagCategoryRecord[]`
  - `reviewing: boolean`
  - `onClose: () => void`
  - `onSubmit: (request: TagBatchReviewCommand) => void`
- `TagBatchReviewPanel` 控件：
  - Drawer 标题：`批量通过标签` 或 `批量拒绝标签`。
  - 已选标签摘要：展示 `selectedTags.length` 和前 5 个标签名，超出显示 `等 N 个`。
  - `Select`：字段 `categoryId`，aria-label 为 `批量审核正式分类`，只在 `APPROVE` 模式显示，options 来自启用分类。
  - `Input.TextArea`：字段 `reviewNote`，aria-label 为 `批量审核备注`，`maxLength={512}`，`showCount`。
  - `Button`：`APPROVE` 模式文本 `确认通过`；未选择 `categoryId` 时 disabled。
  - `Button`：`REJECT` 模式文本 `确认拒绝`；无需选择分类即可提交。
  - `Button`：文本 `取消`，点击 `onClose`。
- 批量审核成功后关闭 panel、清空选择、刷新 reviews/tags/tag-detail/metrics。

验收点：

- 批量通过未选择分类时确认按钮 disabled。
- 批量拒绝可直接提交。
- 成功后待审核表格已选数量归零。

### 任务 8：Coverage 收口

范围文件：

- `docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`
- `docs/30-designs/RUNBOOK-KNOWLEDGE-TAG-BATCH-GOVERNANCE.md`

处理动作：

- 全部实现和验证完成后，更新 coverage。
- `标签批量操作` 直接标记 `已完成`，未完成部分写 `无`。
- 删除或收窄 `Unfinished Focus` 中 `标签批量操作` 未完成描述。
- PR 合入前删除本 RUNBOOK。

验收点：

- coverage 不出现 `标签批量操作` 的 `部分完成`。
- coverage 不记录执行过程。

## 验证命令

后端先格式化本次触碰模块，再执行：

```sh
cd kuzhambu-servers
mvn -pl biz/knowledge/kuzhambu-knowledge-application,biz/knowledge/kuzhambu-knowledge-interface -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/knowledge/kuzhambu-knowledge-application,biz/knowledge/kuzhambu-knowledge-interface -am test
```

前端先格式化 admin-web，再执行：

```sh
cd kuzhambu-apps
npm --workspace kuzhambu-admin-web run format
npm run format:check
npm run lint
npm --workspace kuzhambu-admin-web run test
npm run build
```

收口前检查：

```sh
git diff
```

只保留本任务相关代码、测试和文档变化。
