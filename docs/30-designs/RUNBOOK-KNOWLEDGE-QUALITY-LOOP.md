# Knowledge 质量标注闭环 RUNBOOK

## 目标

把 Knowledge 质量治理交付为人工可用闭环：后台人员对知识对象做人工质量标注，手工触发质量报告生成，后台和 Portal 展示同一份已发布报告快照。该闭环不接入 AI，不接入 Python workers，不做定时任务，不做审批流，不把即时计算摘要当作最终报告。

## 已确认决策

- 报告主锚点使用 `graphVersionId`；`sourceContentType`、`sourceContentId`、`sourceCategoryCode`、`sourceCategoryName` 作为筛选和展示维度。
- 报告状态本轮只落地 `PUBLISHED`，生成即发布；历史报告保留，最新 `PUBLISHED` 报告是后台和 Portal 展示真相源。
- 标注对象覆盖 `ENTITY`、`RELATION`、`LINEAGE_NODE`、`LINEAGE_RELATION`。
- 指标固定为 `entityCoverageRate`、`relationAccuracyRate`、`lineageCoverageRate`、`completenessRate`。
- Portal 无报告时展示明确空态，不展示临时质量数值。
- 后台新增独立质量报告页 `/knowledge/quality-report`；refinement 页负责标注，quality-report 页负责生成和展示报告。

## 完成口径

- 后台 `知识图谱精修工作台` 能对实体、关系、世系节点、世系关系写入、查看、更新、删除人工质量标注。
- 后台 `质量报告` 页能选择图谱版本，点击生成报告，生成后展示最新报告详情。
- 报告刷新浏览器后仍从数据库恢复，指标、问题、来源明细和标注明细不丢失。
- Portal `GET /api/portal/knowledge/quality` 从最新 `PUBLISHED` 报告快照读取展示数据。
- 报告生成链路没有调用 AI facade、图谱抽取接口、Python workers 或任何 worker 客户端。

## 数据结构变更

### `knowledge_quality_annotation`

已有表继续使用，字段保持兼容；本轮只扩展 `object_type` 取值。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 数据库主键 |
| `annotation_id` | `bigint` | 质量标注业务 ID |
| `object_type` | `varchar(32)` | 标注对象类型，支持 `ENTITY`、`RELATION`、`LINEAGE_NODE`、`LINEAGE_RELATION` |
| `object_key` | `varchar(128)` | 标注对象业务 key |
| `source_content_type` | `varchar(64)` | 来源内容类型 |
| `source_content_id` | `bigint` | 来源内容 ID |
| `graph_version_id` | `bigint` | 图谱版本 ID |
| `annotation_status` | `varchar(32)` | 标注状态，建议取值 `PASSED`、`ISSUE`、`IGNORED` |
| `annotation_label` | `varchar(64)` | 标注标签，建议取值 `MISSING_SOURCE`、`WRONG_ENTITY`、`WRONG_RELATION`、`INCOMPLETE_LINEAGE`、`DUPLICATED`、`OTHER` |
| `comment` | `varchar(1000)` | 人工说明 |
| `created_by` | `bigint` | 创建人 |
| `created_at` | `datetime` | 创建时间 |
| `updated_by` | `bigint` | 更新人 |
| `updated_at` | `datetime` | 更新时间 |

### `knowledge_quality_report`

新增报告主表。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 数据库主键 |
| `report_id` | `bigint` | 报告业务 ID |
| `report_no` | `varchar(64)` | 报告编号，格式建议 `KQR-{yyyyMMddHHmmss}-{graphVersionId}` |
| `graph_version_id` | `bigint` | 报告对应图谱版本 ID，必填 |
| `source_content_type` | `varchar(64)` | 来源内容类型 |
| `source_content_id` | `bigint` | 来源内容 ID |
| `source_category_code` | `varchar(64)` | 来源门类编码 |
| `source_category_name` | `varchar(128)` | 来源门类名称 |
| `report_status` | `varchar(32)` | 报告状态，本轮固定 `PUBLISHED` |
| `entity_total_count` | `bigint` | 实体总数 |
| `entity_confirmed_count` | `bigint` | 人工确认实体数 |
| `relation_total_count` | `bigint` | 关系总数 |
| `relation_confirmed_count` | `bigint` | 人工确认关系数 |
| `lineage_total_count` | `bigint` | 世系节点和世系关系总数 |
| `lineage_confirmed_count` | `bigint` | 人工确认世系节点和世系关系数 |
| `entity_coverage_rate` | `decimal(10,4)` | 实体覆盖率 |
| `relation_accuracy_rate` | `decimal(10,4)` | 关系准确率 |
| `lineage_coverage_rate` | `decimal(10,4)` | 世系覆盖率 |
| `completeness_rate` | `decimal(10,4)` | 完整度 |
| `annotation_count` | `bigint` | 报告范围内人工标注数 |
| `issue_count` | `bigint` | 报告问题数 |
| `generated_by` | `bigint` | 生成人 |
| `generated_at` | `datetime` | 生成时间 |
| `published_at` | `datetime` | 发布时间 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

索引：

- 唯一索引：`uk_quality_report_report_id(report_id)`
- 普通索引：`idx_quality_report_version_status(graph_version_id, report_status, generated_at)`
- 普通索引：`idx_quality_report_latest(report_status, generated_at)`

### `knowledge_quality_report_issue`

新增报告问题快照表。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 数据库主键 |
| `issue_id` | `bigint` | 问题业务 ID |
| `report_id` | `bigint` | 归属报告业务 ID |
| `issue_type` | `varchar(64)` | 问题类型，建议取值 `ANNOTATION_ISSUE`、`LOW_ENTITY_COVERAGE`、`LOW_RELATION_ACCURACY`、`LOW_LINEAGE_COVERAGE`、`PENDING_REFINEMENT_TASK`、`EMPTY_GOVERNABLE_OBJECT` |
| `severity` | `varchar(16)` | 严重级别，取值 `high`、`medium`、`low` |
| `object_type` | `varchar(32)` | 关联对象类型，可为空 |
| `object_key` | `varchar(128)` | 关联对象 key，可为空 |
| `title` | `varchar(128)` | 问题标题 |
| `description` | `varchar(1000)` | 问题说明 |
| `suggestion` | `varchar(1000)` | 处理建议 |
| `href` | `varchar(256)` | 后台或 Portal 跳转地址 |
| `priority` | `int` | 展示排序 |
| `created_at` | `datetime` | 创建时间 |

索引：

- 唯一索引：`uk_quality_report_issue_id(issue_id)`
- 普通索引：`idx_quality_report_issue_report(report_id, severity, priority)`

### `knowledge_quality_report_source_detail`

新增报告来源明细快照表。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 数据库主键 |
| `detail_id` | `bigint` | 明细业务 ID |
| `report_id` | `bigint` | 归属报告业务 ID |
| `source_content_type` | `varchar(64)` | 来源内容类型 |
| `source_content_id` | `bigint` | 来源内容 ID |
| `source_category_code` | `varchar(64)` | 来源门类编码 |
| `source_category_name` | `varchar(128)` | 来源门类名称 |
| `graph_version_id` | `bigint` | 图谱版本 ID |
| `applied_at` | `datetime` | 图谱版本应用时间 |
| `annotation_count` | `bigint` | 来源范围内标注数 |
| `issue_count` | `bigint` | 来源范围内问题数 |
| `status` | `varchar(32)` | 来源状态，建议取值 `APPLIED` |
| `href` | `varchar(256)` | Portal 跳转地址 |
| `created_at` | `datetime` | 创建时间 |

索引：

- 唯一索引：`uk_quality_report_source_detail_id(detail_id)`
- 普通索引：`idx_quality_report_source_detail_report(report_id, graph_version_id)`

## 后端小任务

### 任务 1：报告领域对象

目标：建立报告聚合和仓储边界，不写接口。

文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/QualityReport.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/QualityReportIssue.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/QualityReportSourceDetail.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/repository/QualityReportRepository.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/repository/QualityReportIssueRepository.java`

完成要求：

- 领域实体只声明字段，不做跨仓储查询。
- Repository 方法包含保存报告、保存问题、保存来源明细、按 `reportId` 读取、按最新 `PUBLISHED` 读取、分页读取。

### 任务 2：报告持久化

目标：完成三张报告表的 DO、Mapper、Assembler 和 RepositoryImpl。

文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/QualityReportDO.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/QualityReportIssueDO.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/QualityReportSourceDetailDO.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/mapper/QualityReportMapper.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/QualityReportRepositoryImpl.java`

完成要求：

- `QualityReportRepositoryImpl` 内部可注入 `QualityReportMapper`、`QualityReportIssueMapper`、`QualityReportSourceDetailMapper`。
- 如果需要单独 mapper 和 assembler，新增文件名固定为 `QualityReportIssueMapper.java`、`QualityReportSourceDetailMapper.java`、`QualityReportPersistenceAssembler.java`、`QualityReportIssuePersistenceAssembler.java`、`QualityReportSourceDetailPersistenceAssembler.java`，并保持每次提交改动不超过同一小任务边界。
- 保存报告时一次性保存主表、问题和来源明细；失败时整体回滚。

### 任务 3：报告生成应用服务

目标：从已应用图谱版本、正式事实、refinement 状态和人工标注生成报告快照。

文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/KnowledgeQualityReportApplicationService.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/impl/KnowledgeQualityReportApplicationServiceImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/command/GenerateQualityReportCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/query/QualityReportPageQuery.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/result/QualityReportDetailResult.java`

完成要求：

- `generateReport` 输入字段：`graphVersionId`、`generatedBy`。
- `QualityReportPageQuery` 输入字段：`graphVersionId`、`sourceContentType`、`sourceContentId`、`reportStatus`、`pageNo`、`pageSize`。
- `QualityReportDetailResult` 输出主表字段、`issues`、`sourceDetails`、`annotations`。
- 生成报告只读 `GraphVersionRepository`、`KnowledgeEntityRepository`、`KnowledgeRelationRepository`、refinement draft repository 和 `QualityAnnotationRepository`。
- 生成报告不得依赖 `KnowledgeAiExtractionFacade` 或任何 worker client。

### 任务 4：后台和 Portal 接口

目标：暴露后台报告管理接口，并把 Portal 质量页改为读报告快照。

文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/KnowledgeQualityReportController.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/request/QualityReportRequests.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/response/QualityReportResponses.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/assembler/KnowledgeQualityReportInterfaceAssembler.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationServiceImpl.java`

后台接口：

- `POST /api/knowledge/quality/report/generate`
- `POST /api/knowledge/quality/report/page`
- `POST /api/knowledge/quality/report/detail`
- `POST /api/knowledge/quality/report/latest`

权限：

- `knowledge:quality-report:view`：`page`、`detail`、`latest`
- `knowledge:quality-report:generate`：`generate`

Portal 规则：

- `getQuality()` 读取最新 `PUBLISHED` 报告。
- 无报告时返回空 `qualityStats`、空 `trendSeries`、空 `sourceBreakdowns`、空 `sourceDetails`，并返回一条 `focusIssues`：`title=尚未生成质量报告`、`severity=high`、`href=/knowledge/quality`。

### 任务 5：后端测试

目标：覆盖报告生成、接口映射和 Portal 快照读取。

文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/KnowledgeQualityReportApplicationServiceTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/KnowledgeQualityReportControllerTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/portal/quality/controller/KnowledgePortalQualityControllerTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/QualityReportRepositoryTest.java`

测试断言：

- 生成报告时会保存主表、问题和来源明细。
- `PUBLISHED` 最新报告优先级按 `generatedAt` 倒序。
- Portal 无报告时返回空态问题。
- 后台接口权限描述和路径准确。

## 前端小任务

### 任务 1：refinement 标注控件

目标：在现有精修页补齐质量标注入口。

文件：

- `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-types.ts`
- `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-service.ts`
- `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-quality-annotation-drawer.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-quality-annotation-table.tsx`

控件和操作：

- 在实体表、关系表、世系节点表、世系关系表每行新增 `标注` 文本按钮。
- 点击 `标注` 打开右侧 Drawer，标题格式为 `质量标注 - {objectType} / {objectKey}`。
- Drawer 内包含 `Select` 控件 `annotationStatus`，选项为 `PASSED`、`ISSUE`、`IGNORED`。
- Drawer 内包含 `Select` 控件 `annotationLabel`，选项为 `MISSING_SOURCE`、`WRONG_ENTITY`、`WRONG_RELATION`、`INCOMPLETE_LINEAGE`、`DUPLICATED`、`OTHER`。
- Drawer 内包含 `TextArea` 控件 `comment`，最大 1000 字。
- Drawer footer 包含 `保存`、`删除标注`、`取消` 三个按钮；无 `annotationId` 时隐藏 `删除标注`。
- 保存成功后刷新任务详情、质量摘要和当前对象标注表。
- 删除成功后关闭 Drawer 并刷新当前对象标注表。

### 任务 2：质量报告服务和类型

目标：建立前端报告 API 契约。

文件：

- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-types.ts`
- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-service.ts`
- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-service.test.ts`

类型：

- `QualityReportRecord` 包含报告主表展示字段。
- `QualityReportIssueRecord` 包含 `issueId`、`issueType`、`severity`、`objectType`、`objectKey`、`title`、`description`、`suggestion`、`href`、`priority`。
- `QualityReportSourceDetailRecord` 包含来源明细字段。
- `GenerateQualityReportCommand` 只包含 `graphVersionId`、`generatedBy`。
- `QualityReportPageQuery` 包含 `graphVersionId`、`sourceContentType`、`sourceContentId`、`reportStatus`、`pageNo`、`pageSize`。

服务：

- `generateReport` 调用 `/knowledge/quality/report/generate`。
- `pageReports` 调用 `/knowledge/quality/report/page`。
- `getReportDetail` 调用 `/knowledge/quality/report/detail`。
- `getLatestReport` 调用 `/knowledge/quality/report/latest`。

### 任务 3：质量报告页面

目标：新增独立报告页面，完成生成、历史和详情展示。

文件：

- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-page.css`
- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/components/quality-report-generate-form.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/components/quality-report-summary.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/components/quality-report-history-table.tsx`

控件和操作：

- 页面标题 `质量报告`，eyebrow 使用 `Knowledge / Quality Report`。
- 顶部生成区使用 `Card`，包含 `InputNumber` 控件 `graphVersionId` 和主按钮 `生成报告`。
- 点击 `生成报告` 后调用 `generateReport`；按钮 loading，成功后刷新最新报告和历史列表。
- 无报告时显示 `Empty`，说明 `尚未生成质量报告`，并保留生成区可操作。
- 摘要区使用四个 `Statistic`：实体覆盖率、关系准确率、世系覆盖率、完整度。
- 历史表列：报告编号、图谱版本、来源门类、状态、问题数、生成时间、操作。
- 历史表操作列包含 `查看` 按钮；点击后加载详情并更新下方详情区。

### 任务 4：质量报告详情控件和路由

目标：补齐问题、来源、标注明细，并挂载路由。

文件：

- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/components/quality-report-issue-table.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/components/quality-report-source-table.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/components/quality-report-annotation-table.tsx`
- `kuzhambu-apps/admin-web/src/router/index.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-page.test.tsx`

控件和操作：

- 详情区使用 `Tabs`，tab 文案为 `问题清单`、`来源明细`、`人工标注`。
- `问题清单` 表列：级别、类型、对象、标题、说明、建议、操作；操作列 `打开` 跳转到 `href`。
- `来源明细` 表列：来源类型、来源 ID、门类、图谱版本、标注数、问题数、状态、操作；操作列 `打开` 跳转到 `href`。
- `人工标注` 表列：对象类型、对象 key、状态、标签、说明。
- 路由新增 import `QualityReportPage`，并新增 path `knowledge/quality-report`。

## 报告生成规则

- `entityCoverageRate = entityConfirmedCount / entityTotalCount`。
- `relationAccuracyRate = relationConfirmedCount / relationTotalCount`。
- `lineageCoverageRate = lineageConfirmedCount / lineageTotalCount`。
- `completenessRate = (entityConfirmedCount + relationConfirmedCount + lineageConfirmedCount) / (entityTotalCount + relationTotalCount + lineageTotalCount)`。
- 分母为 0 时指标返回 `0`，并生成 `EMPTY_GOVERNABLE_OBJECT` 问题。
- `annotationCount` 统计报告 `graphVersionId` 范围内所有人工标注。
- `issueCount` 统计报告生成出的 `QualityReportIssue` 数量。
- `annotationStatus=ISSUE` 的标注生成 `ANNOTATION_ISSUE` 问题。
- 任一覆盖率低于 `0.8` 生成对应低覆盖率问题。
- 存在 `status=DRAFT` 的 refinement 任务生成 `PENDING_REFINEMENT_TASK` 问题。

## 验收清单

- 后台打开已应用图谱版本对应的 refinement 任务，能对实体、关系、世系节点、世系关系保存质量标注。
- 标注保存后，刷新页面仍可看到标注记录。
- 后台进入 `/knowledge/quality-report`，输入 `graphVersionId`，点击 `生成报告` 后出现最新报告。
- 报告页四个指标、问题清单、来源明细、人工标注、历史记录均来自接口数据。
- Portal 质量页展示与后台最新报告一致的四个核心指标和问题。
- 未生成报告时，后台和 Portal 都展示明确空态。
- 报告生成链路没有新增任何 AI 或 worker 调用。

## 验证命令

Java servers：

```sh
cd kuzhambu-servers
mvn -pl biz/knowledge/kuzhambu-knowledge-interface,biz/knowledge/kuzhambu-knowledge-application,biz/knowledge/kuzhambu-knowledge-domain,biz/knowledge/kuzhambu-knowledge-infra -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/knowledge/kuzhambu-knowledge-interface,biz/knowledge/kuzhambu-knowledge-application,biz/knowledge/kuzhambu-knowledge-domain,biz/knowledge/kuzhambu-knowledge-infra -am test
```

后台前端：

```sh
cd kuzhambu-apps
npm --workspace kuzhambu-admin-web run format
npm run format:check
npm run lint
npm --workspace kuzhambu-admin-web run test
```

## 收口

- 实现完成后更新 `docs/30-designs/KNOWLEDGE-DESIGN.md`，把质量报告纳入 Knowledge 已落地设计。
- 任务合并前删除本 RUNBOOK，避免把一次性执行手册沉淀为稳定设计。
