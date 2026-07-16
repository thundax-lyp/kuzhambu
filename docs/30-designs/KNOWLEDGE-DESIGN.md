# Knowledge Design

## Purpose

本文档定义 Knowledge 域当前已落地设计，覆盖标签治理、同义词、知识抽取任务、数据精修、质量标注报告和正式结果读取闭环。

## Module

```text
kuzhambu-servers/biz/knowledge/
  kuzhambu-knowledge-interface/
  kuzhambu-knowledge-application/
  kuzhambu-knowledge-domain/
  kuzhambu-knowledge-infra/
```

## Business Boundary

Knowledge 拥有统一标签、同义词、知识抽取任务、图谱版本以及正式实体、关系和世系事实。Knowledge 消费 Classics 内容和 AI 提取能力，但不拥有正式内容主数据。对于 Classics 通用标签，Knowledge 负责统一标签解释权、别名解析、自动创建策略和内容引用投影；对于知识抽取候选结果，Knowledge 负责任务台账、候选应用和正式知识事实落库。

Knowledge 也是知识质量治理的唯一写入方。后台人工精修、质量标注和质量报告生成均在 Knowledge 域内闭合；Portal 只读取最新已发布报告快照，不临时计算质量指标。

## DDD Model

- `Tag`
- `TagCategory`
- `TagAlias`
- `TagReviewItem`
- `Synonym`
- `GraphExtractionTask`
- `GraphVersion`
- `KnowledgeEntity`
- `KnowledgeRelation`
- `KnowledgeLineageNode`
- `KnowledgeLineageRelation`
- `RefinementTask`
- `QualityAnnotation`
- `QualityReport`
- `QualityReportIssue`
- `QualityReportSourceDetail`

## Data Model

表名前缀统一使用 `knowledge_`。

核心表：

- `knowledge_tag`
- `knowledge_tag_category`
- `knowledge_tag_alias`
- `knowledge_tag_content_ref`
- `knowledge_tag_review_item`
- `knowledge_synonym`
- `knowledge_entity`
- `knowledge_relation`
- `knowledge_graph_extraction_task`
- `knowledge_graph_version`
- `knowledge_lineage_node`
- `knowledge_lineage_relation`
- `knowledge_refinement_task`
- `knowledge_refinement_entity_draft`
- `knowledge_refinement_relation_draft`
- `knowledge_refinement_lineage_node_draft`
- `knowledge_refinement_lineage_relation_draft`
- `knowledge_quality_annotation`
- `knowledge_quality_report`
- `knowledge_quality_report_issue`
- `knowledge_quality_report_source_detail`

## Application Layer

- `TaxonomyApplicationService`
- `SynonymApplicationService`
- `KnowledgeGraphExtractionApplicationService`
- `KnowledgeGraphRefinementApplicationService`
- `KnowledgeLineageReadApplicationService`
- `KnowledgeQualityReportApplicationService`

Application 层负责标签治理、同义词扩展、知识抽取任务编排、AI 候选状态回填、候选结果应用、图谱版本关联、精修草稿确认、正式事实回写、人工质量标注、质量报告快照生成、正式结果读取和世系画布聚合读取。

质量报告生成规则：

- 后台通过 `KnowledgeQualityReportApplicationService.generateReport(graphVersionId, generatedBy)` 手动生成报告，生成即发布为 `PUBLISHED`。
- 报告以 `graphVersionId` 为主锚点，保留 `sourceContentType`、`sourceContentId`、`sourceCategoryCode`、`sourceCategoryName` 展示维度。
- 核心指标为 `entityCoverageRate`、`relationAccuracyRate`、`lineageCoverageRate`、`completenessRate`。
- 报告问题、来源明细和人工标注作为快照读取，Portal 与后台展示同一份最新 `PUBLISHED` 报告。
- 当前质量报告闭环不调用 AI facade、图谱抽取接口、Python workers 或任何 worker client。

跨域协作语义：

- `KnowledgeTagBindingDomainService` 提供统一标签解析、手工标签自动创建、AI 标签自动创建、内容引用同步和内容引用删除。
- `knowledge_tag_content_ref` 是 Knowledge 侧派生引用模型，不承载 Classics 内容标签排序、绑定状态和标签展示快照。
- Classics 通过协作语义回写内容引用，Knowledge taxonomy 后台 CRUD 不作为跨域调用入口。

## Interface Layer

Admin 入口：

- 标签治理。
- 同义词维护。
- 知识抽取任务创建、分页、详情和候选应用。
- 知识图谱精修工作台，支持实体、关系、世系节点和世系关系的人工确认、编辑、删除、新增与质量标注。
- 正式结果读取，包括图谱版本列表/详情，以及从版本下钻的正式实体、正式关系和正式世系结果。
- `/knowledge/lineage` 世系图浏览，支持版本切换、关键词筛选、节点类型筛选、关系类型筛选、确认状态筛选、深度选择、刷新、重置、画布节点/关系点击、节点表格定位、关系表格定位和详情面板联动。
- 质量报告页 `/knowledge/quality-report`，支持输入图谱版本、手工生成报告、查看历史报告、问题清单、来源明细和人工标注明细。

Portal 入口：

- `/knowledge` 门户首页。
- `/knowledge/atlas` 图谱分层浏览，支持 overview、category 和 detail 三层。
- `/knowledge/lineage` 世系图只读浏览，支持默认最新已应用版本、URL query 恢复、关键词筛选、节点类型筛选、关系类型筛选、确认状态筛选、筛选清除、画布节点/关系点击和只读详情面板。
- `/knowledge/quality` 质量页，从最新 `PUBLISHED` 质量报告快照读取指标、问题和来源明细；无报告时展示明确空态。

## Infrastructure Layer

- Repository 持久化 `knowledge_*` 表。
- AI 提取通过 `KnowledgeAiExtractionDomainService` 协作语义触发。
- `KnowledgeGraphCandidateApplySupport` 负责把候选 payload 应用到 `knowledge_entity`、`knowledge_relation`、`knowledge_graph_version`、`knowledge_lineage_node` 和 `knowledge_lineage_relation`。
- 精修仓储持久化 `knowledge_refinement_*` 草稿表和人工质量标注表。
- 世系画布读取只消费正式 `knowledge_lineage_node`、`knowledge_lineage_relation` 和 `knowledge_graph_version`，不写入知识事实表，不触发 AI 或 workers。
- 质量报告仓储一次性保存 `knowledge_quality_report`、`knowledge_quality_report_issue` 和 `knowledge_quality_report_source_detail`，报告详情按快照读取。

## Data Ownership

Knowledge 是 `knowledge_*` 表的唯一写入方。Classics 删除或下线内容时，Knowledge 通过协作语义更新 `knowledge_tag_content_ref` 等来源引用和质量指标。

协作兼容口径：

- Knowledge 内容类型内部仍使用 `MING_CUSTOM`，但接受 Classics 传入的 `MING_CUSTOMS` 协作值。
- Knowledge 标签来源内部仍使用 `AI_EXTRACTED`，但接受 Classics 传入的 `AI` 协作值。

## Observability

- 标签审核、合并和废弃通过 System 审计记录。
- 图谱提取任务记录 `aiCallId`、`aiCandidateId`、失败原因、完成时间和应用时间。

## Acceptance

- 标签、同义词、知识抽取任务、正式知识事实和正式结果读取在一个业务域内闭合。
- 独立世系图浏览在 Admin 和 Portal 入口内闭合，画布、节点/关系列表、节点/关系详情均读取同一份正式世系事实。
- 人工精修、人工质量标注、质量报告生成、后台报告展示和 Portal 质量展示在 Knowledge 域内闭合。
- 搜索和问答可消费 Knowledge 增强能力，但当前不依赖图谱浏览或质量报告作为前置。
