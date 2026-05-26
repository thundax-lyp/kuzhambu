# Data Refinement Design

## Purpose

本文档定义数据精修工作台业务域的数据结构和接口设计。本文参考 redesign 中的数据精修材料，但不照搬其四面板工作台、大对象模型或接口形态；本设计必须从集中处理实体标注和关系抽取的业务出发，按 DDD 风格重新划分业务边界、聚合和表结构。

Data Refinement 是人工标注工作台域，不承载摘要编辑、问答对管理、图谱可视化、AI 配置或知识图谱最终展示。它只拥有待精修工作项、实体标注、关系标注和人工确认状态；精修操作追溯由 Audit 承载。

## Business Fit Rules

- 业务优先于旧设计：待精修筛选、实体确认、实体编辑、实体删除、实体新增、关系确认、关系编辑、关系删除、关系新增和质量评估标注入口必须能直接对应集中精修业务。
- 原 redesign 只能作为需求线索和素材来源，不能作为表结构、接口路径、模块划分或聚合边界的设计依据。
- 摘要和问答对属于各内容页内联能力，不进入本模块。
- 本模块保存人工标注结果和确认轨迹；Knowledge Graph 消费这些结果更新图谱和质量指标。
- 不为未来增强、路线图或预留能力建表、建接口或增加状态字段。

## Module

- 模块名称：Data Refinement
- 业务域：datarefinement
- 对应需求文档：[DATA-REFINEMENT-REQUIREMENTS.md](../10-requirements/DATA-REFINEMENT-REQUIREMENTS.md)
- 后端 biz 子工程：`kuzhambu-servers/biz/kuzhambu-biz-data-refinement`
- 后端 infra 子工程：`kuzhambu-servers/infra/kuzhambu-infra-data-refinement`
- 后台接口入口：`kuzhambu-servers/interfaces/kuzhambu-admin-api`
- 前台接口入口：无
- 前端入口：`kuzhambu-apps/admin-web`
- Python worker 能力：无直接归属

## Business Boundary

- 本模块负责：待精修内容筛选、三才条目精修工作项、实体标注、关系标注、人工确认、删除标记和质量评估抽样标记。
- 本模块不负责：摘要编辑、问答对管理、图谱可视化、知识图谱最终节点边存储、AI 实体关系抽取执行、搜索结果页面。
- 依赖的其他业务域能力：Sancai 提供条目上下文；Knowledge Graph 消费人工标注结果并更新质量指标；Audit 记录精修操作；Auth/Core 提供操作者和权限。
- 对外提供的业务能力：集中精修工作台、人工标注数据和图谱质量评估标注入口。

## DDD Model

### Aggregates

- `DataRefinementWorkItem`：精修工作项，绑定三才条目，记录精修状态、门类、是否质量评估样本和统计信息。
- `EntityAnnotation`：实体标注，记录实体名称、类型、来源片段、置信度、人工确认状态和删除状态。
- `RelationAnnotation`：关系标注，记录源实体、目标实体、关系类型、置信度、人工确认状态和删除状态。

### Value Objects

- `DataRefinementContentType`：当前为 `SANCAI_ENTRY`。
- `DataRefinementWorkStatus`：`PENDING` / `IN_PROGRESS` / `COMPLETED`。
- `AnnotationSource`：`AI_EXTRACTED` / `MANUAL`。
- `AnnotationStatus`：`ACTIVE` / `DELETED`。
- `EntityType`：`PERSON` / `PLACE` / `EVENT` / `CONCEPT` / `TIME` / `OBJECT` / `OTHER`。

## Data Model

Data Refinement 表固定使用 `data_refinement_` 前缀。所有对外标识使用 ULID，数据库内部可使用 `bigint id`。

### data_refinement_work_item

保存待精修工作项。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `work_item_id` | `workItemId` | 是 | 工作项 ULID |
| `content_type` | `contentType` | 是 | 内容类型 |
| `content_id` | `contentId` | 是 | 三才条目 ULID |
| `category_code` | `categoryCode` | 否 | 三才门类代码 |
| `status` | `status` | 是 | 精修状态 |
| `quality_sample` | `qualitySample` | 是 | 是否质量评估样本 |
| `entity_count` | `entityCount` | 是 | 实体标注数量 |
| `relation_count` | `relationCount` | 是 | 关系标注数量 |
| `verified_entity_count` | `verifiedEntityCount` | 是 | 已确认实体数量 |
| `verified_relation_count` | `verifiedRelationCount` | 是 | 已确认关系数量 |
| `completed_at` | `completedAt` | 否 | 完成时间 |

约束：
- `work_item_id` 唯一。
- `content_type + content_id` 唯一。
- 当前主要服务三才图会知识图谱质量。

### data_refinement_entity_annotation

保存实体标注。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `annotation_id` | `annotationId` | 是 | 实体标注 ULID |
| `work_item_id` | `workItemId` | 是 | 工作项 ULID |
| `content_id` | `contentId` | 是 | 三才条目 ULID |
| `entity_name` | `entityName` | 是 | 实体名称 |
| `entity_type` | `entityType` | 是 | 实体类型 |
| `normalized_name` | `normalizedName` | 否 | 规范名称 |
| `source_text` | `sourceText` | 否 | 来源片段 |
| `confidence` | `confidence` | 是 | 置信度 |
| `verified` | `verified` | 是 | 是否人工确认 |
| `source` | `source` | 是 | `AI_EXTRACTED` / `MANUAL` |
| `status` | `status` | 是 | `ACTIVE` / `DELETED` |
| `kg_entity_id` | `kgEntityId` | 否 | 对应知识图谱实体 ULID |

约束：
- `annotation_id` 唯一。
- 人工确认的实体不得被新的 AI 结果静默覆盖。
- 删除实体前必须二次确认，删除后保留标注记录。

### data_refinement_relation_annotation

保存关系标注。

| Column | Domain Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 内部主键 |
| `annotation_id` | `annotationId` | 是 | 关系标注 ULID |
| `work_item_id` | `workItemId` | 是 | 工作项 ULID |
| `content_id` | `contentId` | 是 | 三才条目 ULID |
| `source_entity_annotation_id` | `sourceEntityAnnotationId` | 是 | 源实体标注 ULID |
| `target_entity_annotation_id` | `targetEntityAnnotationId` | 是 | 目标实体标注 ULID |
| `relation_type` | `relationType` | 是 | 关系类型 |
| `description` | `description` | 否 | 关系说明 |
| `confidence` | `confidence` | 是 | 置信度 |
| `verified` | `verified` | 是 | 是否人工确认 |
| `source` | `source` | 是 | `AI_EXTRACTED` / `MANUAL` |
| `status` | `status` | 是 | `ACTIVE` / `DELETED` |
| `kg_relation_id` | `kgRelationId` | 否 | 对应知识图谱关系 ULID |

约束：
- `annotation_id` 唯一。
- 同一工作项内 `source_entity_annotation_id + target_entity_annotation_id + relation_type` 不得重复。
- 人工确认的关系不得被新的 AI 结果静默覆盖。
- 删除关系前必须二次确认，删除后保留标注记录。

## Application Layer

- `DataRefinementWorkItemApplicationService`：待精修筛选、工作项创建、工作项详情和完成状态更新。
- `EntityAnnotationApplicationService`：实体查询、确认、编辑、删除和新增。
- `RelationAnnotationApplicationService`：关系查询、确认、编辑、删除和新增。
- `DataRefinementQualityApplicationService`：质量评估样本标记和结果通知。

事务边界：
- 实体或关系保存必须更新对应工作项统计，并由 Audit 记录确认、编辑、删除和新增轨迹。
- 人工确认必须设置 `verified=true`，并通知 Knowledge Graph 更新质量相关信息。
- 删除实体或关系必须保留标注记录，不做物理删除。
- 完成工作项必须基于当前实体和关系统计更新状态。

## Interface Layer

后台接口固定使用 `/api/admin/data-refinement/**`。

### Work Item API

- `GET /data-refinement/work-items`：查询待精修内容，支持门类、状态、质量样本筛选。
- `GET /data-refinement/work-items/{workItemId}`：读取工作项详情。
- `POST /data-refinement/work-items/{workItemId}/complete`：标记工作项完成。
- `POST /data-refinement/work-items/{workItemId}/quality-sample`：标记为质量评估样本。

### Entity API

- `GET /data-refinement/work-items/{workItemId}/entities`：读取实体标注列表。
- `POST /data-refinement/work-items/{workItemId}/entities`：新增实体标注。
- `PUT /data-refinement/entities/{annotationId}`：编辑实体标注。
- `POST /data-refinement/entities/{annotationId}/confirm`：确认实体标注。
- `DELETE /data-refinement/entities/{annotationId}`：删除实体标注。

### Relation API

- `GET /data-refinement/work-items/{workItemId}/relations`：读取关系标注列表。
- `POST /data-refinement/work-items/{workItemId}/relations`：新增关系标注。
- `PUT /data-refinement/relations/{annotationId}`：编辑关系标注。
- `POST /data-refinement/relations/{annotationId}/confirm`：确认关系标注。
- `DELETE /data-refinement/relations/{annotationId}`：删除关系标注。

本模块不提供 portal 接口。

## Infrastructure Layer

- Repository：`DataRefinementWorkItemRepository`、`EntityAnnotationRepository`、`RelationAnnotationRepository`。
- Mapper：按表建立 MyBatis Mapper。
- PersistenceAssembler：每个聚合根使用独立 `*PersistenceAssembler`。
- 外部客户端：Sancai Application Service、Knowledge Graph Application Service、Audit Application Service。
- 缓存：工作项详情和标注列表可按需短缓存。
- 消息：实体或关系精修保存后可发布事件供 Knowledge Graph 更新质量指标。

## Data Ownership

- 本模块拥有：`data_refinement_work_item`、`data_refinement_entity_annotation`、`data_refinement_relation_annotation`。
- 本模块只读引用：Sancai 条目上下文、Auth/Core 用户和权限。
- 禁止跨域直接访问：不得直接修改 Sancai 内容表，不得直接写入 Knowledge Graph 最终节点边表。
- Flyway 脚本归属：`kuzhambu-admin-api` 的 `db/migration/V1__init.sql`，按 Data Refinement 分段。

## Observability

- 运行日志：记录标注保存失败、确认失败、删除失败和通知 Knowledge Graph 失败原因。
- 访问日志：由接口层统一记录。
- 审计日志：实体和关系新增、编辑、确认、删除都应形成业务审计。
- 关键指标：待精修数、已完成数、实体确认率、关系确认率、质量样本数。

## Acceptance

- 用户能在集中工作台内完成实体和关系整理。
- 人工确认结果不会被新的 AI 结果静默覆盖。
- 删除实体或关系前必须二次确认。
- 用户能从待精修筛选进入具体内容的精修工作台。
- 实体或关系精修后，知识图谱质量相关信息能反映变化。
