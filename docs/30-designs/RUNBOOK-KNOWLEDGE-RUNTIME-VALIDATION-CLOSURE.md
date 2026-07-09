# Knowledge Runtime Validation Closure Runbook

## 目标

把 `docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md` 的“运行时验证”从 `部分完成` 收口为 `已完成`。

本 RUNBOOK 只用于本次验收执行。任务关闭时删除本文件，最终结论保留在：

- `docs/40-readiness/KNOWLEDGE-RUNTIME-SMOKE-EVIDENCE.md`
- `docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`

## 最终验收标准

- Admin Web 全量 Vitest 通过：`pnpm --filter ./admin-web run test`。
- Knowledge Admin Web Playwright 覆盖 6 个页面：`taxonomy`、`graph-extraction`、`graph-results`、`refinement`、`lineage`、`quality-report`。
- Knowledge / AI / Workers / Admin Web 的跨服务冒烟结果写入 evidence。
- `KNOWLEDGE-IMPLEMENTATION-COVERAGE.md` 中“运行时验证”状态改为 `已完成`，`未完成部分` 改为 `无`。
- 本 RUNBOOK 在任务关闭 commit 中删除。

## 数据结构变更要求

本次目标是运行时验证收口，默认不新增字段、不迁移表结构。若执行中确认必须改数据结构，必须先把变更精确登记到本节，再实施代码修改。

### 必填登记格式

```text
表名：
新增字段：
修改字段：
删除字段：
索引变更：
涉及文件：
验证测试：
```

### 可变更文件边界

图谱抽取任务：

- `db/schema/knowledge.sql`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/GraphExtractionTaskDO.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/mapper/GraphExtractionTaskMapper.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/repository/impl/GraphExtractionTaskRepositoryImpl.java`

图谱正式结果：

- `db/schema/knowledge.sql`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/GraphVersionDO.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/KnowledgeEntityDO.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/KnowledgeRelationDO.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/KnowledgeLineageNodeDO.java`

精修与质量报告：

- `db/schema/knowledge.sql`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/RefinementTaskDO.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/QualityAnnotationDO.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/QualityReportDO.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/QualityReportSourceDetailDO.java`

### 字段基线

`knowledge_graph_extraction_task`：

- `id`, `task_id`, `task_type`, `scope_type`, `scope_json`, `source_content_type`, `source_content_id`, `ai_call_id`, `ai_candidate_id`, `status`, `error_type`, `error_message`, `requested_by`, `requested_at`, `completed_at`, `applied_at`
- 运行时验证必须覆盖的请求/响应字段：`batchJobId`, `triggerSource`, `selectionScopeJson`, `replaceUnconfirmedOnly`, `parentTaskId`, `modelId`, `modelName`, `promptVersionId`, `requestId`, `traceId`, `promptMessagesJson`, `promptVariablesJson`, `promptHash`, `inputPayloadJson`, `outputSchemaJson`, `forceJson`, `locale`

`knowledge_graph_version`：

- `id`, `version_id`, `task_id`, `candidate_id`, `task_type`, `scope_type`, `scope_json`, `source_content_type`, `source_content_id`, `source_category_code`, `source_category_name`, `version_no`, `status`, `applied_at`

`knowledge_entity`：

- `id`, `entity_id`, `entity_key`, `name`, `entity_type`, `description`, `confirmation_status`, `latest_version_id`, `source_refs_json`, `first_extracted_at`, `last_extracted_at`, `confirmed_at`

`knowledge_relation`：

- `id`, `relation_id`, `relation_key`, `source_entity_key`, `target_entity_key`, `source_name`, `target_name`, `relation_type`, `evidence`, `confirmation_status`, `latest_version_id`, `source_refs_json`, `first_extracted_at`, `last_extracted_at`, `confirmed_at`

`knowledge_lineage_node`：

- `id`, `node_id`, `node_key`, `name`, `node_type`, `generation`, `gender`, `confirmation_status`, `latest_version_id`, `source_refs_json`, `first_extracted_at`, `last_extracted_at`, `confirmed_at`

`knowledge_lineage_relation`：

- `id`, `relation_id`, `relation_key`, `source_node_key`, `target_node_key`, `source_name`, `target_name`, `relation_type`, `evidence`, `confirmation_status`, `latest_version_id`, `source_refs_json`, `first_extracted_at`, `last_extracted_at`, `confirmed_at`

`knowledge_refinement_task`：

- `id`, `refinement_task_id`, `task_type`, `source_content_type`, `source_content_id`, `source_category_code`, `source_category_name`, `graph_version_id`, `status`, `opened_by`, `opened_at`, `submitted_by`, `submitted_at`, `applied_by`, `applied_at`, `cancelled_by`, `cancelled_at`

`knowledge_quality_annotation`：

- `id`, `annotation_id`, `object_type`, `object_key`, `source_content_type`, `source_content_id`, `graph_version_id`, `annotation_status`, `annotation_label`, `comment`, `created_by`, `created_at`, `updated_by`, `updated_at`

`knowledge_quality_report`：

- `id`, `report_id`, `report_no`, `graph_version_id`, `source_content_type`, `source_content_id`, `source_category_code`, `source_category_name`, `report_status`, `entity_total_count`, `entity_confirmed_count`, `relation_total_count`, `relation_confirmed_count`, `lineage_total_count`, `lineage_confirmed_count`, `entity_coverage_rate`, `relation_accuracy_rate`, `lineage_coverage_rate`, `completeness_rate`, `annotation_count`, `issue_count`, `generated_by`, `generated_at`, `published_at`, `created_at`, `updated_at`

`knowledge_quality_report_issue`：

- `id`, `issue_id`, `report_id`, `issue_type`, `severity`, `object_type`, `object_key`, `title`, `description`, `suggestion`, `href`, `priority`, `created_at`

`knowledge_quality_report_source_detail`：

- `id`, `detail_id`, `report_id`, `source_content_type`, `source_content_id`, `source_category_code`, `source_category_name`, `graph_version_id`, `applied_at`, `annotation_count`, `issue_count`, `status`, `href`, `created_at`

## 小任务拆分

每个小任务主文件必须控制在 2-5 个。超过 5 个主文件时继续拆分。

### 1. 修复 Admin Web 全量 Vitest 超时

目标：全量 Admin Web Vitest 不再超时。

主文件：

- `kuzhambu-apps/admin-web/src/app.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-page.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`
- `kuzhambu-apps/admin-web/src/test/setup.ts`

处理动作：

- `src/app.test.tsx`：修复菜单加载、权限加载、路由跳转的等待条件。
- `graph-extraction-page.test.tsx`：修复 query string、page shell mock、任务列表 mock、详情 mock 的异步等待。
- `sancai-entry-panel.test.tsx`：修复权限 mock、AI 候选确认入口、面板渲染完成条件。
- `src/test/setup.ts`：仅在确有公共测试环境问题时调整，不得为单个用例写专用逻辑。

验收命令：

```sh
cd kuzhambu-apps
pnpm --filter ./admin-web run test
```

### 2. 补齐 Graph Extraction Playwright

目标：`/knowledge/graph-extraction` 覆盖任务创建、详情、应用、重生成、批次取消。

主文件：

- `kuzhambu-apps/admin-web/e2e/knowledge/graph-extraction/graph-extraction.spec.ts`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-create.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-task-table.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-task-detail.tsx`

控件与操作：

- 输入框“来源内容类型”：输入 `SANCAI_ENTRY`。
- 数字输入“来源内容 ID”：输入 `1001`。
- 输入框“作用域类型”：输入 `CLASSICS_ENTRY`。
- 输入框“语言”：输入 `zh-CN`。
- 数字输入“模型 ID”：输入 `1`。
- 输入框“模型名”：输入 `gpt-5.5`。
- 文本域“作用域 JSON”：输入 `{"entryId":1001}`。
- 文本域“批量范围 JSON”：输入 `{"sourceContentIds":[1001,1002]}`。
- 复选框“仅替换未人工确认结果”：勾选。
- 文本域“Prompt Messages JSON”：输入 `[{"role":"system","content":"extract"}]`。
- 文本域“输入 Payload JSON”：输入 `{"content":"待抽取正文"}`。
- 按钮“创建图谱抽取任务”：点击后断言创建请求。
- 任务表按钮“查看详情”：点击后断言详情抽屉或详情区域字段。
- 任务表按钮“应用候选结果”：点击后断言应用请求。
- 批次取消按钮：点击后断言取消请求。
- URL 参数 `regenerate=1&taskType=GRAPH&sourceTaskId=88&triggerSource=REFINEMENT_APPLIED`：进入后点击“提交精修重生成”。

请求 payload 断言：

- 创建任务：`taskType`, `sourceContentType`, `sourceContentId`, `scopeType`, `scopeJson`, `selectionScopeJson`, `replaceUnconfirmedOnly`, `modelId`, `modelName`, `promptMessagesJson`, `inputPayloadJson`, `locale`
- 详情读取：`taskId`
- 应用候选：`taskId`
- 批次取消：`batchJobId`
- 精修重生成：`taskType`, `sourceTaskId`, `triggerSource`, `selectionScopeJson`, `replaceUnconfirmedOnly`

### 3. 补齐 Graph Results Playwright

目标：`/knowledge/graph-results` 覆盖版本读取、详情、正式实体/关系/世系结果下钻。

主文件：

- `kuzhambu-apps/admin-web/e2e/knowledge/graph-results/graph-results.spec.ts`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/graph-results-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/components/graph-version-table.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/components/graph-version-detail.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/components/graph-entity-detail.tsx`

控件与操作：

- 打开 `/knowledge/graph-results?graphVersionId=71`。
- 表格“知识图谱版本表格”：断言列 `版本号`, `任务类型`, `状态`, `来源类型`, `来源 ID`, `版本序号`, `精修状态`, `操作`。
- 按钮“查看详情”：点击后断言 `versionId`, `taskId`, `candidateId`, `taskType`, `sourceContentType`, `sourceContentId`, `versionNo`, `status`, `appliedAt`, `refinementApplied`, `lastRefinementTaskId`, `lastRefinementAppliedAt`。
- 按钮“查看正式结果”：点击后断言实体、关系、世系节点、世系关系结果区加载。
- 实体行详情按钮：点击后断言 `entityKey`, `name`, `entityType`, `confirmationStatus`, `sourceRefsJson`。
- 关系行详情按钮：点击后断言 `relationKey`, `sourceName`, `targetName`, `relationType`, `evidence`。
- 世系节点详情按钮：点击后断言 `nodeKey`, `name`, `nodeType`, `generation`, `gender`。
- 世系关系详情按钮：点击后断言 `relationKey`, `sourceName`, `targetName`, `relationType`, `evidence`。

请求 payload 断言：

- 版本分页：`pageNo`, `pageSize`, `taskType`, `status`, `sourceContentType`, `sourceContentId`
- 版本详情：`versionId`
- 实体分页：`versionId`, `keyword`, `entityType`, `confirmationStatus`
- 关系分页：`versionId`, `keyword`, `relationType`, `confirmationStatus`
- 世系节点分页：`versionId`, `keyword`, `nodeType`, `confirmationStatus`
- 世系关系分页：`versionId`, `keyword`, `relationType`, `confirmationStatus`

### 4. 补齐 Lineage Playwright

目标：`/knowledge/lineage` 覆盖画布读取、筛选、刷新、重置、节点/关系详情联动。

主文件：

- `kuzhambu-apps/admin-web/e2e/knowledge/lineage/lineage.spec.ts`
- `kuzhambu-apps/admin-web/src/pages/knowledge/lineage/lineage-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/lineage/components/lineage-filter-bar.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/lineage/components/lineage-canvas.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/lineage/components/lineage-detail-panel.tsx`

控件与操作：

- Select“图谱版本”：选择版本 `71`。
- 搜索框“搜索世系节点或关系”：输入关键词并触发搜索。
- Select“节点类型”：选择 `PERSON`。
- Select“关系类型”：选择 `PARENT_CHILD`。
- Select“确认状态”：选择 `MANUAL_CONFIRMED`。
- Select“深度”：选择 `3 层`。
- 按钮“刷新”：点击后断言 canvas 请求。
- 画布节点：点击后断言详情面板展示 `nodeId`, `nodeKey`, `name`, `nodeType`, `generation`, `gender`, `confirmationStatus`。
- 画布关系：点击后断言详情面板展示 `relationId`, `relationType`, `sourceNodeName`, `targetNodeName`, `confirmationStatus`。
- 按钮“重置”：点击后断言筛选清空，`depth` 回到默认值。

请求 payload 断言：

- `versionId`, `keyword`, `nodeType`, `relationType`, `confirmationStatus`, `depth`, `focusNodeId`, `focusRelationId`

### 5. 补齐 Refinement Playwright

目标：`/knowledge/refinement` 覆盖精修任务打开、草稿编辑、确认、质量标注和应用后联动。

主文件：

- `kuzhambu-apps/admin-web/e2e/knowledge/refinement/refinement.spec.ts`
- `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-filter-form.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-entity-table.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-quality-annotation-drawer.tsx`

控件与操作：

- 筛选表单：按 `taskType`, `sourceContentType`, `sourceContentId`, `sourceCategoryCode`, `status` 查询。
- 工作台列表按钮“打开任务”：点击后断言 `graphVersionId`, `openedBy`。
- 实体表按钮“新增”或“编辑”：填写 `entityKey`, `name`, `entityType`, `description`, `sourceRefsJson`, `sortOrder`。
- 实体行按钮“确认”：点击后断言 `refinementTaskId`, `entityKey`, `operatorId`。
- 关系表按钮“新增”或“编辑”：填写 `relationKey`, `sourceEntityKey`, `targetEntityKey`, `sourceName`, `targetName`, `relationType`, `evidence`, `sourceRefsJson`, `sortOrder`。
- 质量标注 Drawer：填写 `objectType`, `objectKey`, `graphVersionId`, `annotationStatus`, `annotationLabel`, `comment`。
- 按钮“应用精修”：点击后断言页面展示或跳转入口包含 `graphVersionId`, `sourceTaskId`, `selectionScopeJson`, `replaceUnconfirmedOnly`, `triggerSource=REFINEMENT_APPLIED`, `graphRefreshRequired`, `qualityReportRefreshRequired`。

请求 payload 断言：

- 任务分页：`taskType`, `sourceContentType`, `sourceContentId`, `sourceCategoryCode`, `status`
- 打开任务：`graphVersionId`, `openedBy`
- 实体保存：`refinementTaskId`, `entityId`, `entityKey`, `name`, `entityType`, `description`, `sourceRefsJson`, `sortOrder`, `operatorId`
- 关系保存：`refinementTaskId`, `relationId`, `relationKey`, `sourceEntityKey`, `targetEntityKey`, `sourceName`, `targetName`, `relationType`, `evidence`, `sourceRefsJson`, `sortOrder`, `operatorId`
- 质量标注：`objectType`, `objectKey`, `sourceContentType`, `sourceContentId`, `graphVersionId`, `annotationStatus`, `annotationLabel`, `comment`
- 应用精修：`refinementTaskId`, `appliedBy`

### 6. 补齐 Quality Report Playwright

目标：`/knowledge/quality-report` 覆盖报告生成、最新报告、历史报告、详情、低质量门类重提取。

主文件：

- `kuzhambu-apps/admin-web/e2e/knowledge/quality-report/quality-report.spec.ts`
- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/components/quality-report-generate-form.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/components/quality-report-summary.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/components/quality-report-source-table.tsx`

控件与操作：

- 打开 `/knowledge/quality-report?graphVersionId=71&regenerate=1`。
- 数字输入 `graphVersionId`：输入 `71`。
- 按钮“生成报告”：点击后断言生成请求。
- 报告摘要：断言 `entityCoverageRate`, `relationAccuracyRate`, `lineageCoverageRate`, `completenessRate`, `annotationCount`, `issueCount`。
- 历史报告表行详情：点击后断言详情请求。
- 来源明细表低质量门类重提取按钮：点击后断言重提取请求。
- 重提取结果区域：断言展示 `taskId`, `batchJobId`, `triggerSource=QUALITY_REPORT`, `selectionScopeJson`, `replaceUnconfirmedOnly`。

请求 payload 断言：

- 生成报告：`graphVersionId`, `generatedBy`
- 最新报告：`graphVersionId`
- 历史报告分页：`graphVersionId`, `sourceContentType`, `sourceContentId`, `reportStatus`
- 报告详情：`reportId`
- 低质量门类重提取：`reportId`, `sourceCategoryCode`, `taskType`, `replaceUnconfirmedOnly`, `modelId`, `modelName`, `promptMessagesJson`, `inputPayloadJson`, `requestedBy`

### 7. 更新 Evidence 与 Coverage

目标：所有验证通过后再更新文档状态。

主文件：

- `docs/40-readiness/KNOWLEDGE-RUNTIME-SMOKE-EVIDENCE.md`
- `docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`
- `docs/30-designs/RUNBOOK-KNOWLEDGE-RUNTIME-VALIDATION-CLOSURE.md`

处理动作：

- `KNOWLEDGE-RUNTIME-SMOKE-EVIDENCE.md` 记录日期、分支、提交、Java/Maven/Node/pnpm/Python 版本、数据库或 stub 边界、命令结果和关键断言。
- `KNOWLEDGE-IMPLEMENTATION-COVERAGE.md` 的 `Current Baseline` 增加运行时验证完成记录。
- `KNOWLEDGE-IMPLEMENTATION-COVERAGE.md` 的 `部分完成` 删除运行时验证阻塞描述。
- `KNOWLEDGE-IMPLEMENTATION-COVERAGE.md` 的“运行时验证”矩阵行改成 `已完成`，`未完成部分` 改成 `无`。
- 删除 coverage 中关于 Admin Web 全量 Vitest 超时的过期风险；若仍有非阻塞限制，只能写入 `Residual Risks`。
- 删除本 RUNBOOK。

## 统一验证命令

后端：

```sh
cd kuzhambu-servers
mvn -pl biz/knowledge,biz/ai -am spotless:check checkstyle:check test
```

Workers：

```sh
cd kuzhambu-workers
.venv/bin/python -m ruff format --check .
.venv/bin/python -m ruff check .
.venv/bin/python -m pytest -p no:capture
```

Admin Web：

```sh
cd kuzhambu-apps
pnpm --filter ./admin-web run format:check
pnpm --filter ./admin-web run lint
pnpm --filter ./admin-web run build
pnpm --filter ./admin-web run test
pnpm --filter ./admin-web run e2e -- e2e/knowledge/taxonomy/taxonomy.spec.ts e2e/knowledge/graph-extraction/graph-extraction.spec.ts e2e/knowledge/graph-results/graph-results.spec.ts e2e/knowledge/lineage/lineage.spec.ts e2e/knowledge/refinement/refinement.spec.ts e2e/knowledge/quality-report/quality-report.spec.ts
```

## 收口检查

- `git diff` 只包含 Knowledge 验收收口相关改动。
- 每个实现小任务主文件为 2-5 个。
- 数据结构如发生变更，evidence 必须列出表名、字段名、索引、DO、Mapper、Repository 和测试文件。
- Playwright 必须同时断言控件操作、请求 payload 和页面结果。
- coverage 只在所有验证通过后改为 `已完成`。
- 本 RUNBOOK 必须在任务关闭 commit 中删除。
