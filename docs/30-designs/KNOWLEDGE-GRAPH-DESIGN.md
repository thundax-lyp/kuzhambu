# Knowledge Graph Design

## Purpose

本文档定义知识图谱业务域的数据结构和接口设计。本文参考 redesign 中的知识图谱材料，但不照搬其大对象模型、数据结构或接口形态；本设计必须从三才图会实体关系提取、三层浏览、质量报告和世系图业务出发，按 DDD 风格重新划分业务边界、聚合和表结构。

Knowledge Graph 是独立图谱域，只覆盖三才图会。它不承载王圻文档、明代习俗、搜索问答前置依赖或人工精修工作台。它拥有最终图谱实体、关系、来源引用、提取任务、质量指标和世系图数据。

## Business Fit Rules

- 业务优先于旧设计：实体、关系、来源条目、提取任务、重生成、鸟瞰层、门类层、详情层、质量报告和世系图必须能直接对应知识图谱业务。
- 原 redesign 只能作为需求线索和素材来源，不能作为表结构、接口路径、模块划分或聚合边界的设计依据。
- 知识图谱只覆盖三才图会；王圻文档和明代习俗不得进入本模块图谱表。
- 搜索和问答可以消费图谱增强，但不得依赖图谱作为必需前置。
- 实体标注和关系抽取的人工精修归 Data Refinement；本模块读取精修结果并更新最终图谱和质量指标。
- 不为未来增强、路线图或预留能力建表、建接口或增加状态字段。

## Module

- 模块名称：Knowledge Graph
- 业务域：knowledge-graph
- 对应需求文档：[KNOWLEDGE-GRAPH-REQUIREMENTS.md](../10-requirements/KNOWLEDGE-GRAPH-REQUIREMENTS.md)
- 后端 biz 子工程：`kuzhambu-servers/biz/kuzhambu-biz-knowledge-graph`
- 后端 infra 子工程：`kuzhambu-servers/infra/kuzhambu-infra-knowledge-graph`
- 后台接口入口：`kuzhambu-servers/interfaces/kuzhambu-admin-api`
- 前台接口入口：`kuzhambu-servers/interfaces/kuzhambu-portal-api`
- 前端入口：`kuzhambu-apps/admin-web`、`kuzhambu-apps/portal-web`
- Python worker 能力：可通过 HTTP 提供实体关系提取能力，最终写入由 Java 主系统负责

## Business Boundary

- 本模块负责：三才图会实体关系提取、异步提取任务、批量生成、重生成、已精修结果保留、图谱实体、图谱关系、来源引用、鸟瞰层数据、门类层数据、详情层数据、质量报告和世系图。
- 本模块不负责：王圻文档图谱、明代习俗图谱、跨库搜索、智能问答、人工实体标注工作台、人工关系抽取工作台、AI 配置和提示词管理。
- 依赖的其他业务域能力：Sancai 提供条目内容和生命周期；Data Refinement 提供人工标注结果；AI Config Prompt 提供模型和提示词配置；Audit 记录提取和重生成操作。
- 对外提供的业务能力：三才知识图谱浏览、提取任务、重生成、质量报告、世系图和图谱消费数据。

## DDD Model

### Aggregates

- `KnowledgeGraphEntity`：图谱实体聚合根，按名称和类型去重，拥有实体描述、置信度、人工确认状态、来源引用和状态。
- `KnowledgeGraphRelation`：图谱关系聚合根，连接两个实体，拥有关系类型、置信度、人工确认状态、来源引用和状态。
- `KnowledgeGraphExtractionJob`：图谱提取任务，记录筛选范围、批量进度、失败原因、提取版本和任务状态。
- `KnowledgeGraphQualityMetric`：质量指标，按门类保存实体覆盖率、关系准确率、完整度、最近提取版本和提取时间。
- `LineageGraph`：世系图专用聚合，保存 8 国、109 君、275 关系的节点和边。

### Value Objects

- `KnowledgeGraphEntityType`：`PERSON` / `PLACE` / `EVENT` / `CONCEPT` / `TIME` / `OBJECT` / `OTHER`。
- `KnowledgeGraphRelationType`：`PARENT_OF` / `CHILD_OF` / `RELATED_TO` / `LOCATED_IN` / `OCCURS_AT` / `CREATED_BY` / `PART_OF` / `SAME_AS` / `PREDECESSOR_OF` / `SUCCESSOR_OF`。
- `KnowledgeGraphObjectStatus`：`ACTIVE` / `REMOVED`。
- `KnowledgeGraphExtractionStatus`：`RUNNING` / `COMPLETED` / `PARTIAL_FAILED` / `FAILED` / `CANCELLED`。
- `KnowledgeGraphScopeType`：`CATEGORY` / `FILTER` / `SELECTED` / `QUALITY_REPORT` / `LINEAGE`。
- `KnowledgeGraphLayer`：`OVERVIEW` / `CATEGORY` / `DETAIL`。

## Data Model

Knowledge Graph 表固定使用 `knowledge_graph_` 前缀。所有对外标识使用 ULID，数据库内部可使用 `bigint id`。

### knowledge_graph_entity

保存最终图谱实体。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `entity_id` | `entityId` | 是 | 实体 ULID |
| `name` | `name` | 是 | 实体名称 |
| `entity_type` | `entityType` | 是 | 实体类型 |
| `category_code` | `categoryCode` | 否 | 主要来源三才门类 |
| `description` | `description` | 否 | 实体描述 |
| `confidence` | `confidence` | 是 | 置信度 |
| `verified` | `verified` | 是 | 是否人工确认 |
| `status` | `status` | 是 | `ACTIVE` / `REMOVED` |
| `extract_version` | `extractVersion` | 是 | 最近提取版本 |
| `last_extracted_at` | `lastExtractedAt` | 否 | 最近提取时间 |
| `last_refined_at` | `lastRefinedAt` | 否 | 最近人工精修反映时间 |

约束：
- `entity_id` 唯一。
- `name + entity_type` 视为同一实体。
- 已人工确认实体不得被重生成静默覆盖。
- 失去来源支撑且未人工确认的实体可标记为 `REMOVED`。

### knowledge_graph_relation

保存最终图谱关系。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `relation_id` | `relationId` | 是 | 关系 ULID |
| `source_entity_id` | `sourceEntityId` | 是 | 源实体 ULID |
| `target_entity_id` | `targetEntityId` | 是 | 目标实体 ULID |
| `relation_type` | `relationType` | 是 | 关系类型 |
| `description` | `description` | 否 | 关系说明 |
| `confidence` | `confidence` | 是 | 置信度 |
| `verified` | `verified` | 是 | 是否人工确认 |
| `status` | `status` | 是 | `ACTIVE` / `REMOVED` |
| `extract_version` | `extractVersion` | 是 | 最近提取版本 |
| `last_extracted_at` | `lastExtractedAt` | 否 | 最近提取时间 |
| `last_refined_at` | `lastRefinedAt` | 否 | 最近人工精修反映时间 |

约束：
- `relation_id` 唯一。
- `source_entity_id + target_entity_id + relation_type` 视为同一关系。
- 已人工确认关系不得被重生成静默覆盖。
- 失去来源支撑且未人工确认的关系可标记为 `REMOVED`。

### knowledge_graph_source_ref

保存图谱对象和三才条目的来源引用。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `source_ref_id` | `sourceRefId` | 是 | 来源引用 ULID |
| `graph_object_type` | `graphObjectType` | 是 | `ENTITY` / `RELATION` |
| `graph_object_id` | `graphObjectId` | 是 | 实体或关系 ULID |
| `entry_id` | `entryId` | 是 | 三才条目 ULID |
| `category_code` | `categoryCode` | 是 | 三才门类 |
| `source_status` | `sourceStatus` | 是 | `ACTIVE` / `ARCHIVED` / `REMOVED` |

约束：
- `source_ref_id` 唯一。
- 同一图谱对象和同一条目来源引用唯一。
- 条目删除后必须移除或标记来源引用；失去来源支撑且未人工确认的图谱对象不应继续保留为活跃。

### knowledge_graph_extraction_job

保存图谱提取任务。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `job_id` | `jobId` | 是 | 任务 ULID |
| `scope_type` | `scopeType` | 是 | 提取范围类型 |
| `scope_json` | `scopeJson` | 是 | 范围参数 JSON |
| `status` | `status` | 是 | 任务状态 |
| `extract_version` | `extractVersion` | 是 | 提取版本 |
| `total_count` | `totalCount` | 是 | 总条目数 |
| `success_count` | `successCount` | 是 | 成功数 |
| `failed_count` | `failedCount` | 是 | 失败数 |
| `requester_user_id` | `requesterUserId` | 是 | 提取发起人 |
| `requested_at` | `requestedAt` | 是 | 发起时间 |
| `started_at` | `startedAt` | 否 | 开始时间 |
| `finished_at` | `finishedAt` | 否 | 完成时间 |
| `error_message` | `errorMessage` | 否 | 失败原因 |

约束：
- 批量提取必须展示进度和失败原因。
- 单条失败不影响其他条目。
- 重生成必须保留人工确认结果。

### knowledge_graph_extraction_job_item

保存图谱提取任务明细。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `job_item_id` | `jobItemId` | 是 | 明细 ULID |
| `job_id` | `jobId` | 是 | 任务 ULID |
| `entry_id` | `entryId` | 是 | 三才条目 ULID |
| `category_code` | `categoryCode` | 是 | 三才门类 |
| `status` | `status` | 是 | 明细状态 |
| `entity_count` | `entityCount` | 是 | 提取实体数 |
| `relation_count` | `relationCount` | 是 | 提取关系数 |
| `error_message` | `errorMessage` | 否 | 失败原因 |

约束：
- 同一任务内同一条目不得重复。
- 失败明细必须保留失败原因。

### knowledge_graph_quality_metric

保存门类质量指标。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `metric_id` | `metricId` | 是 | 指标 ULID |
| `category_code` | `categoryCode` | 是 | 三才门类 |
| `extract_version` | `extractVersion` | 是 | 最近提取版本 |
| `entity_coverage_rate` | `entityCoverageRate` | 是 | 实体覆盖率 |
| `relation_accuracy_rate` | `relationAccuracyRate` | 是 | 关系准确率 |
| `completeness_rate` | `completenessRate` | 是 | 完整度 |
| `sampled_relation_count` | `sampledRelationCount` | 是 | 抽样关系数 |
| `verified_relation_count` | `verifiedRelationCount` | 是 | 已确认关系数 |
| `last_extracted_at` | `lastExtractedAt` | 否 | 最近提取时间 |
| `calculated_at` | `calculatedAt` | 是 | 指标计算时间 |

约束：
- `category_code` 唯一。
- 每次提取、重生成或精修保存后，应更新对应门类质量指标。

### knowledge_graph_lineage_node

保存世系图节点。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `lineage_node_id` | `lineageNodeId` | 是 | 世系节点 ULID |
| `polity_name` | `polityName` | 是 | 国名 |
| `ruler_name` | `rulerName` | 是 | 君主名 |
| `title` | `title` | 否 | 称号 |
| `reign_label` | `reignLabel` | 否 | 在位说明 |
| `sort_order` | `sortOrder` | 是 | 排序 |

约束：
- 世系图必须覆盖 8 国、109 君。

### knowledge_graph_lineage_relation

保存世系图关系。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `lineage_relation_id` | `lineageRelationId` | 是 | 世系关系 ULID |
| `source_node_id` | `sourceNodeId` | 是 | 源节点 ULID |
| `target_node_id` | `targetNodeId` | 是 | 目标节点 ULID |
| `relation_type` | `relationType` | 是 | 世系关系类型 |
| `description` | `description` | 否 | 说明 |

约束：
- 世系图必须覆盖 275 条关系。

## Application Layer

- `KnowledgeGraphExtractionApplicationService`：图谱提取、批量提取、重生成、任务进度和取消。
- `KnowledgeGraphEntityApplicationService`：实体查询、详情、来源引用和 Data Refinement 结果应用。
- `KnowledgeGraphRelationApplicationService`：关系查询、来源引用和 Data Refinement 结果应用。
- `KnowledgeGraphBrowseApplicationService`：鸟瞰层、门类层、详情层和面包屑导航数据。
- `KnowledgeGraphQualityApplicationService`：质量指标计算、质量报告和低质量门类触发提取。
- `LineageGraphApplicationService`：世系图提取结果保存和展示。

事务边界：
- 单条提取成功必须写入实体、关系和来源引用。
- 重生成必须先处理未确认图谱对象，再合并已确认对象来源，不得覆盖人工确认结果。
- Data Refinement 结果应用必须更新最终实体或关系，并更新质量指标。
- 条目删除后必须清理来源引用，并处理失去来源支撑的未确认图谱对象。
- 提取任务进度、失败原因和质量指标更新必须在业务操作完成后保持一致。

## Interface Layer

后台接口固定使用 `/api/admin/knowledge-graph/**`。

### Extraction API

- `POST /knowledge-graph/extractions`：创建图谱提取任务。
- `GET /knowledge-graph/extractions/{jobId}`：查询提取任务进度。
- `GET /knowledge-graph/extractions/{jobId}/items`：查询提取任务明细。
- `POST /knowledge-graph/extractions/{jobId}/cancel`：取消未完成任务。
- `POST /knowledge-graph/extractions/regenerate`：按范围重生成图谱。

### Browse API

- `GET /knowledge-graph/overview`：读取鸟瞰层数据。
- `GET /knowledge-graph/categories/{categoryCode}`：读取门类层图谱。
- `GET /knowledge-graph/entities/{entityId}`：读取实体详情层。
- `GET /knowledge-graph/entities/{entityId}/relations`：读取实体关联关系。
- `GET /knowledge-graph/breadcrumbs`：读取面包屑导航数据。

### Quality And Lineage API

- `GET /knowledge-graph/quality`：读取质量报告。
- `POST /knowledge-graph/quality/categories/{categoryCode}/extract`：从低质量门类触发提取。
- `GET /knowledge-graph/lineage`：读取世系图。
- `POST /knowledge-graph/lineage/extract`：触发世系图专用提取。

portal 接口固定使用 `/api/portal/knowledge-graph/**`：
- `GET /knowledge-graph/overview`
- `GET /knowledge-graph/categories/{categoryCode}`
- `GET /knowledge-graph/entities/{entityId}`
- `GET /knowledge-graph/entities/{entityId}/relations`
- `GET /knowledge-graph/lineage`

portal 只返回公开可访问内容支撑的图谱数据；归档来源可展示来源标记。

## Infrastructure Layer

- Repository：`KnowledgeGraphEntityRepository`、`KnowledgeGraphRelationRepository`、`KnowledgeGraphSourceRefRepository`、`KnowledgeGraphExtractionJobRepository`、`KnowledgeGraphQualityMetricRepository`、`LineageGraphRepository`。
- Mapper：按表建立 MyBatis Mapper。
- PersistenceAssembler：每个聚合根使用独立 `*PersistenceAssembler`。
- 外部客户端：Sancai Application Service、Data Refinement Application Service、AI Config Prompt Application Service、Audit Application Service、Python worker HTTP Client。
- 缓存：鸟瞰层、门类层图谱、实体详情和质量报告。
- 消息：提取任务可通过 RocketMQ 分发条目明细。

## Data Ownership

- 本模块拥有：`knowledge_graph_entity`、`knowledge_graph_relation`、`knowledge_graph_source_ref`、`knowledge_graph_extraction_job`、`knowledge_graph_extraction_job_item`、`knowledge_graph_quality_metric`、`knowledge_graph_lineage_node`、`knowledge_graph_lineage_relation`。
- 本模块只读引用：Sancai 条目、Data Refinement 人工标注、Auth/Core 用户和权限。
- 禁止跨域直接访问：不得直接修改 Sancai 内容表，不得直接写入 Data Refinement 标注表，不得要求 Search 或 QA 依赖本模块作为前置。
- Flyway 脚本归属：`kuzhambu-admin-api` 的 `db/migration/V1__init.sql`，按 Knowledge Graph 分段。

## Observability

- 运行日志：记录提取任务创建、单条提取失败、重生成合并、来源引用清理和质量指标计算失败原因。
- 访问日志：由接口层统一记录。
- 审计日志：提取、重生成、世系图提取和 Data Refinement 结果应用都应形成业务审计。
- 关键指标：实体数、关系数、来源引用数、提取成功率、质量覆盖率、关系准确率、完整度、世系节点数、世系关系数。

## Acceptance

- 用户能触发提取任务并查看进度。
- 用户能对筛选范围批量触发图谱提取。
- 用户能从概览进入门类图谱，再查看实体详情。
- 用户能通过面包屑在鸟瞰层、门类层和详情层之间返回。
- 用户能在质量报告中看到按门类统计的覆盖率、准确率和完整度。
- 用户精修后质量指标能反映变化。
- 用户能查看世系图。
- 未生成知识图谱时，搜索和问答功能仍能正常使用。
