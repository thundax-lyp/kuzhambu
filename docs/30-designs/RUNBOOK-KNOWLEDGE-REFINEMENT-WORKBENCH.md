# Knowledge Refinement Workbench Runbook

## Purpose

本文档定义 Knowledge 数据精修工作台的 `Phase 1` 实现手册。

`Phase 1` 交付能力：

- 待精修内容筛选。
- 按门类筛选待精修内容。
- 单条内容的实体确认、编辑、删除和新增。
- 单条内容的关系确认、编辑、删除和新增。
- 人工确认状态展示与保存。
- Admin Web 精修工作台页面。

`Phase 1` 不开放能力：

- 质量评估人工标注前端入口。
- 世系图精修前端入口。
- 质量报告前端页面。
- 批量生成、批量重生成和低质量门类触发提取入口。
- Portal 侧消费入口。

## Branch

- 当前工作分支：`feat/knowledge-refinement-workbench`

## Delivery Rule

- `Phase 1` 只收敛页面交付范围，不收缩最终版数据结构。
- 数据结构、仓储接口、应用服务接口、HTTP 接口和前端类型按最终版铺开。
- 未在 `Phase 1` 开放的能力必须有稳定数据结构和服务边界。
- 单个执行任务关联文件不得超过 `5` 个。
- 单个执行任务只表达一个主动作。

## Requirement Mapping

本 RUNBOOK 覆盖 `docs/10-requirements/KNOWLEDGE-REQUIREMENTS.md` 中数据精修相关最终需求，并按阶段开放：

- `待精修内容筛选`：`Phase 1` 开放。
- `实体确认、编辑、删除和新增`：`Phase 1` 开放。
- `关系确认、编辑、删除和新增`：`Phase 1` 开放。
- `人工确认状态`：`Phase 1` 开放。
- `按门类筛选待精修内容`：`Phase 1` 开放。
- `知识图谱质量评估人工标注入口`：本轮只落数据结构和后端接口，不开放前端入口。
- `精修保存后更新质量相关信息`：本轮按运行时聚合计算落接口和服务。
- `读取数据精修修正结果`：本轮按统一精修结构对 `GRAPH / RELATION / LINEAGE` 铺开。

## Final Structure Decisions

### 1. 独立精修结构

精修不直接改写 `knowledge_entity`、`knowledge_relation`、`knowledge_lineage_node`、`knowledge_lineage_relation`。

最终版精修结构新增以下表：

- `knowledge_refinement_task`
- `knowledge_refinement_entity_draft`
- `knowledge_refinement_relation_draft`
- `knowledge_refinement_lineage_node_draft`
- `knowledge_refinement_lineage_relation_draft`
- `knowledge_quality_annotation`

正式事实表继续保留：

- `knowledge_entity`
- `knowledge_relation`
- `knowledge_lineage_node`
- `knowledge_lineage_relation`
- `knowledge_graph_version`

### 2. Phase 1 页面范围

`Phase 1` 页面只开放：

- `GRAPH`
- `RELATION`

最终版结构同时覆盖：

- `GRAPH`
- `RELATION`
- `LINEAGE`

`LINEAGE` 本轮只落数据结构、仓储接口、应用服务接口和 HTTP 接口，不开放 Admin 页面交互。

### 3. 精修任务模型

`knowledge_refinement_task` 一行表示一个内容版本上的一次精修工作单。

固定键：

- `refinement_task_id`
- `task_type`
- `source_content_type`
- `source_content_id`
- `graph_version_id`

固定状态：

- `DRAFT`
- `SUBMITTED`
- `APPLIED`
- `CANCELLED`

`Phase 1` 页面只操作 `DRAFT`。

### 4. 草稿事实模型

每条精修草稿记录必须区分：

- `origin_type`：`AI_EXTRACTED` / `MANUAL_CREATED`
- `operation_type`：`UNCHANGED` / `UPDATED` / `DELETED` / `CONFIRMED` / `ADDED`
- `confirmation_status`：`PENDING` / `MANUAL_CONFIRMED`
- `source_refs_json`

草稿表保留稳定业务键：

- 实体草稿保留 `entity_key`
- 关系草稿保留 `relation_key`
- 世系节点草稿保留 `node_key`
- 世系关系草稿保留 `relation_key`

手工新增键规则：

- 实体：`manual:entity:{ULID}`
- 关系：`manual:relation:{ULID}`
- 世系节点：`manual:lineage-node:{ULID}`
- 世系关系：`manual:lineage-relation:{ULID}`

已有事实被人工编辑时，不重算既有业务键。

### 5. 质量标注模型

`knowledge_quality_annotation` 支持两类标注对象：

- `ENTITY`
- `RELATION`

本轮不开放前端入口，但后端数据结构和接口必须落地。

固定字段：

- `annotation_id`
- `object_type`
- `object_key`
- `source_content_type`
- `source_content_id`
- `graph_version_id`
- `annotation_status`
- `annotation_label`
- `comment`
- `created_by`
- `created_at`
- `updated_by`
- `updated_at`

### 6. 质量指标口径

质量指标采用运行时聚合计算，不新增质量指标持久化表。

固定指标：

- `entityCoverageRate`
- `relationAccuracyRate`
- `completenessRate`

固定聚合粒度：

- 全量
- 门类
- 单内容

### 7. 门类真相源与查询冗余

门类真相源固定来自 Classics 主内容。

Knowledge 侧为精修查询冗余以下字段：

- `source_category_code`
- `source_category_name`

冗余位置固定在：

- `knowledge_graph_version`
- `knowledge_refinement_task`

### 8. 审计字段定义

字段级 `before / after` 通过 System 审计体系承载。

实体精修审计字段固定为：

- `name`
- `entityType`
- `description`
- `confirmationStatus`
- `sourceRefsJson`

关系精修审计字段固定为：

- `sourceEntityKey`
- `targetEntityKey`
- `sourceName`
- `targetName`
- `relationType`
- `evidence`
- `confirmationStatus`
- `sourceRefsJson`

世系节点精修审计字段固定为：

- `name`
- `nodeType`
- `generation`
- `gender`
- `confirmationStatus`
- `sourceRefsJson`

世系关系精修审计字段固定为：

- `sourceNodeKey`
- `targetNodeKey`
- `sourceName`
- `targetName`
- `relationType`
- `evidence`
- `confirmationStatus`
- `sourceRefsJson`

质量标注审计字段固定为：

- `annotationStatus`
- `annotationLabel`
- `comment`

## Final HTTP Surface

最终版接口统一挂在 `/api/knowledge/refinement`。

`Phase 1` 开放：

- `POST /task/page`
- `POST /task/open`
- `POST /task/detail`
- `POST /entity/add`
- `POST /entity/update`
- `POST /entity/confirm`
- `POST /entity/delete`
- `POST /relation/add`
- `POST /relation/update`
- `POST /relation/confirm`
- `POST /relation/delete`
- `POST /task/apply`
- `POST /quality/summary`

本轮后端落地但前端不开放：

- `POST /lineage-node/add`
- `POST /lineage-node/update`
- `POST /lineage-node/confirm`
- `POST /lineage-node/delete`
- `POST /lineage-relation/add`
- `POST /lineage-relation/update`
- `POST /lineage-relation/confirm`
- `POST /lineage-relation/delete`
- `POST /annotation/add`
- `POST /annotation/update`
- `POST /annotation/delete`
- `POST /annotation/page`

## Data Structure Changes

### Schema Update

`db/schema/knowledge.sql` 需要新增以下表：

#### `knowledge_refinement_task`

- `id` bigint auto_increment
- `refinement_task_id` bigint not null
- `task_type` varchar(32) not null
- `source_content_type` varchar(32) not null
- `source_content_id` bigint not null
- `source_category_code` varchar(64) not null
- `source_category_name` varchar(128) not null
- `graph_version_id` bigint not null
- `status` varchar(32) not null
- `opened_by` bigint not null
- `opened_at` datetime(3) not null
- `submitted_by` bigint default null
- `submitted_at` datetime(3) default null
- `applied_by` bigint default null
- `applied_at` datetime(3) default null
- `cancelled_by` bigint default null
- `cancelled_at` datetime(3) default null

唯一键：

- `uk_refinement_task_id(refinement_task_id)`
- `uk_refinement_source_version(task_type, source_content_type, source_content_id, graph_version_id, status)`

索引：

- `idx_refinement_task_category(status, source_category_code, opened_at)`
- `idx_refinement_task_source(source_content_type, source_content_id)`

#### `knowledge_refinement_entity_draft`

- `id` bigint auto_increment
- `draft_id` bigint not null
- `refinement_task_id` bigint not null
- `entity_id` bigint default null
- `entity_key` varchar(160) not null
- `origin_type` varchar(32) not null
- `operation_type` varchar(32) not null
- `name` varchar(128) not null
- `entity_type` varchar(64) not null
- `description` varchar(1024) default null
- `confirmation_status` varchar(32) not null
- `source_refs_json` json default null
- `sort_order` int not null
- `created_by` bigint not null
- `created_at` datetime(3) not null
- `updated_by` bigint not null
- `updated_at` datetime(3) not null

#### `knowledge_refinement_relation_draft`

- `id` bigint auto_increment
- `draft_id` bigint not null
- `refinement_task_id` bigint not null
- `relation_id` bigint default null
- `relation_key` varchar(256) not null
- `origin_type` varchar(32) not null
- `operation_type` varchar(32) not null
- `source_entity_key` varchar(160) not null
- `target_entity_key` varchar(160) not null
- `source_name` varchar(128) not null
- `target_name` varchar(128) not null
- `relation_type` varchar(64) not null
- `evidence` varchar(1024) default null
- `confirmation_status` varchar(32) not null
- `source_refs_json` json default null
- `sort_order` int not null
- `created_by` bigint not null
- `created_at` datetime(3) not null
- `updated_by` bigint not null
- `updated_at` datetime(3) not null

#### `knowledge_refinement_lineage_node_draft`

- 字段结构与 `knowledge_refinement_entity_draft` 对齐，关键字段替换为 `node_id`、`node_key`、`node_type`、`generation`、`gender`

#### `knowledge_refinement_lineage_relation_draft`

- 字段结构与 `knowledge_refinement_relation_draft` 对齐，关键字段替换为 `source_node_key`、`target_node_key`

#### `knowledge_quality_annotation`

- `id` bigint auto_increment
- `annotation_id` bigint not null
- `object_type` varchar(32) not null
- `object_key` varchar(256) not null
- `source_content_type` varchar(32) not null
- `source_content_id` bigint not null
- `graph_version_id` bigint not null
- `annotation_status` varchar(32) not null
- `annotation_label` varchar(64) not null
- `comment` varchar(1024) default null
- `created_by` bigint not null
- `created_at` datetime(3) not null
- `updated_by` bigint not null
- `updated_at` datetime(3) not null

### Existing Table Update

`knowledge_graph_version` 需要新增：

- `source_category_code` varchar(64) not null
- `source_category_name` varchar(128) not null

## Task Breakdown

### A. Schema And DO

#### A1. 扩展 `knowledge_graph_version` 门类冗余字段

- 数据结构变更：为 `knowledge_graph_version` 新增 `source_category_code`、`source_category_name`
- 关联文件：
  - `db/schema/knowledge.sql`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/GraphVersionDO.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/assembler/GraphVersionPersistenceAssembler.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/graph/repository/impl/GraphVersionRepositoryTest.java`

#### A2. 新增 `knowledge_refinement_task`

- 数据结构变更：新增精修任务表
- 关联文件：
  - `db/schema/knowledge.sql`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/RefinementTaskDO.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/mapper/RefinementTaskMapper.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/RefinementTaskRepositoryTest.java`

#### A3. 新增实体与关系草稿表

- 数据结构变更：新增 `knowledge_refinement_entity_draft`、`knowledge_refinement_relation_draft`
- 关联文件：
  - `db/schema/knowledge.sql`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/RefinementEntityDraftDO.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/RefinementRelationDraftDO.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/RefinementEntityDraftRepositoryTest.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/RefinementRelationDraftRepositoryTest.java`

#### A4. 新增世系草稿表

- 数据结构变更：新增 `knowledge_refinement_lineage_node_draft`、`knowledge_refinement_lineage_relation_draft`
- 关联文件：
  - `db/schema/knowledge.sql`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/RefinementLineageNodeDraftDO.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/RefinementLineageRelationDraftDO.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/RefinementLineageNodeDraftRepositoryTest.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/RefinementLineageRelationDraftRepositoryTest.java`

#### A5. 新增质量标注表

- 数据结构变更：新增 `knowledge_quality_annotation`
- 关联文件：
  - `db/schema/knowledge.sql`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/QualityAnnotationDO.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/mapper/QualityAnnotationMapper.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/QualityAnnotationRepositoryTest.java`

### B. Domain And Repository

#### B1. 新增精修任务领域模型与仓储接口

- 数据结构变更：新增 `RefinementTask` 领域对象和仓储端口
- 关联文件：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/RefinementTask.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/valueobject/RefinementTaskId.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/repository/RefinementTaskRepository.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/RefinementTaskRepositoryImpl.java`

#### B2. 新增实体与关系草稿领域模型与仓储接口

- 数据结构变更：新增实体草稿、关系草稿领域对象和仓储端口
- 关联文件：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/RefinementEntityDraft.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/RefinementRelationDraft.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/repository/RefinementEntityDraftRepository.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/repository/RefinementRelationDraftRepository.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/RefinementEntityDraftRepositoryImpl.java`

#### B3. 新增世系草稿领域模型与仓储接口

- 数据结构变更：新增世系草稿领域对象和仓储端口
- 关联文件：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/RefinementLineageNodeDraft.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/RefinementLineageRelationDraft.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/repository/RefinementLineageNodeDraftRepository.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/repository/RefinementLineageRelationDraftRepository.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/RefinementLineageNodeDraftRepositoryImpl.java`

#### B4. 新增质量标注领域模型与仓储接口

- 数据结构变更：新增质量标注领域对象和仓储端口
- 关联文件：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/QualityAnnotation.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/repository/QualityAnnotationRepository.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/QualityAnnotationRepositoryImpl.java`

### C. Application Contract

#### C1. 新增最终版应用服务接口

- 数据结构变更：无 DB 结构变更；新增最终版应用服务契约
- 关联文件：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/KnowledgeGraphRefinementApplicationService.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/query/RefinementWorkbenchPageQuery.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/query/RefinementDetailQuery.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/result/RefinementDetailResult.java`

#### C2. 新增实体与关系命令模型

- 数据结构变更：无 DB 结构变更；新增最终版命令模型
- 关联文件：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/command/UpsertRefinementEntityCommand.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/command/ConfirmRefinementEntityCommand.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/command/UpsertRefinementRelationCommand.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/command/ConfirmRefinementRelationCommand.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/command/DeleteRefinementRelationCommand.java`

#### C3. 新增世系与标注命令模型

- 数据结构变更：无 DB 结构变更；新增最终版命令模型
- 关联文件：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/command/UpsertRefinementLineageNodeCommand.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/command/UpsertRefinementLineageRelationCommand.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/command/UpsertQualityAnnotationCommand.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/result/QualitySummaryResult.java`

### D. Application Implementation

#### D1. 实现精修任务打开与草稿初始化

- 数据结构变更：无 DB 结构变更；从正式事实复制到草稿表
- 关联文件：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/impl/KnowledgeGraphRefinementApplicationServiceImpl.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/support/RefinementDraftBootstrapSupport.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/KnowledgeGraphRefinementTaskOpenTest.java`

#### D2. 实现实体与关系草稿写入

- 数据结构变更：无 DB 结构变更；写入草稿表，不直接改正式事实
- 关联文件：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/impl/KnowledgeGraphRefinementApplicationServiceImpl.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/support/KnowledgeRefinementManualKeySupport.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/KnowledgeGraphRefinementEntityWriteTest.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/KnowledgeGraphRefinementRelationWriteTest.java`

#### D3. 实现世系草稿写入与标注接口

- 数据结构变更：无 DB 结构变更；落最终版后端接口
- 关联文件：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/impl/KnowledgeGraphRefinementApplicationServiceImpl.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/KnowledgeGraphRefinementLineageWriteTest.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/QualityAnnotationWriteTest.java`

#### D4. 实现精修应用到正式事实

- 数据结构变更：无 DB 结构变更；`task/apply` 把草稿覆盖到正式事实
- 关联文件：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/support/RefinementApplySupport.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/impl/KnowledgeGraphRefinementApplicationServiceImpl.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/KnowledgeGraphRefinementApplyTest.java`

#### D5. 实现运行时质量指标聚合

- 数据结构变更：无 DB 结构变更；基于正式事实、草稿和标注运行时聚合
- 关联文件：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/support/QualitySummaryAggregationSupport.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/impl/KnowledgeGraphRefinementApplicationServiceImpl.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/QualitySummaryAggregationSupportTest.java`

### E. Audit Integration

#### E1. 新增精修审计快照组装器

- 数据结构变更：无 DB 结构变更；接入 System 审计快照体系
- 关联文件：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/audit/RefinementEntityDraftAuditSnapshotAssembler.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/audit/RefinementRelationDraftAuditSnapshotAssembler.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/audit/QualityAnnotationAuditSnapshotAssembler.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/RefinementAuditSnapshotAssemblerTest.java`

#### E2. 新增世系精修审计快照组装器

- 数据结构变更：无 DB 结构变更；接入 System 审计快照体系
- 关联文件：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/audit/RefinementLineageNodeDraftAuditSnapshotAssembler.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/audit/RefinementLineageRelationDraftAuditSnapshotAssembler.java`
  - `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/AuditSnapshotAssemblerRegistry.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/RefinementLineageAuditSnapshotAssemblerTest.java`

### F. HTTP Interface

#### F1. 新增最终版请求模型

- 数据结构变更：无 DB 结构变更；覆盖实体、关系、世系、标注和质量汇总接口
- 关联文件：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/request/RefinementWorkbenchPageRequest.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/request/RefinementDetailRequest.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/request/UpsertRefinementEntityRequest.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/request/UpsertQualityAnnotationRequest.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/request/QualitySummaryRequest.java`

#### F2. 新增最终版响应模型

- 数据结构变更：无 DB 结构变更；覆盖实体、关系、世系、标注和质量汇总返回
- 关联文件：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/response/RefinementDetailResponse.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/response/RefinementEntityResponse.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/response/RefinementRelationResponse.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/response/QualityAnnotationResponse.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/response/QualitySummaryResponse.java`

#### F3. 实现 Controller 与 Assembler

- 数据结构变更：无 DB 结构变更；完成最终版接口落地
- 关联文件：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/KnowledgeGraphRefinementController.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/assembler/KnowledgeGraphRefinementInterfaceAssembler.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/KnowledgeGraphRefinementControllerTest.java`

### G. Frontend Types And Service

#### G1. 新增最终版前端类型

- 数据结构变更：无 DB 结构变更；前端类型同时包含 `GRAPH / RELATION / LINEAGE / ANNOTATION / QUALITY`
- 关联文件：
  - `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-types.ts`

#### G2. 新增前端服务

- 数据结构变更：无 DB 结构变更；前端 service 只调用 `Phase 1` 开放接口
- 关联文件：
  - `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-service.ts`
  - `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-service.test.ts`

### H. Frontend Page

#### H1. 新增精修列表组件

- 数据结构变更：无 DB 结构变更；支持按门类筛选
- 关联文件：
  - `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-filter-form.tsx`
  - `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-workbench-table.tsx`
  - `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-progress-summary.tsx`

#### H2. 新增实体精修组件

- 数据结构变更：无 DB 结构变更；只消费实体草稿接口
- 关联文件：
  - `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-entity-table.tsx`
  - `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-entity-editor.tsx`
  - `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-entity-delete-modal.tsx`

#### H3. 新增关系精修组件

- 数据结构变更：无 DB 结构变更；只消费关系草稿接口
- 关联文件：
  - `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-relation-table.tsx`
  - `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-relation-editor.tsx`
  - `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-relation-delete-modal.tsx`

#### H4. 新增页面壳与路由

- 数据结构变更：无 DB 结构变更；挂载 `/knowledge/refinement`
- 关联文件：
  - `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-page.tsx`
  - `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-page.css`
  - `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-page.test.tsx`
  - `kuzhambu-apps/admin-web/src/router/index.tsx`

### I. Menu And Seed

#### I1. 新增菜单与权限种子

- 数据结构变更：无业务表结构变更；新增 `knowledge:refinement:view`、`knowledge:refinement:edit`
- 关联文件：
  - `db/data-source/system.json`
  - `db/data/system.sql`

### J. Validation And Docs

#### J1. 后端验证

- 数据结构变更：无 DB 结构变更；补齐 refinement application / interface / infra 测试
- 关联文件：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/KnowledgeGraphRefinementTaskOpenTest.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/KnowledgeGraphRefinementApplyTest.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/KnowledgeGraphRefinementControllerTest.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/RefinementTaskRepositoryTest.java`

#### J2. 前端验证

- 数据结构变更：无 DB 结构变更；补齐 service contract 和页面主交互测试
- 关联文件：
  - `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-service.test.ts`
  - `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-page.test.tsx`

#### J3. 文档同步

- 数据结构变更：无 DB 结构变更；同步设计与覆盖文档
- 关联文件：
  - `docs/30-designs/KNOWLEDGE-DESIGN.md`
  - `docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`
  - `docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`

## Verification Commands

后端：

```sh
cd kuzhambu-servers
mvn -pl biz/knowledge/kuzhambu-knowledge-application spotless:apply
mvn -pl biz/knowledge/kuzhambu-knowledge-interface spotless:apply
mvn -pl biz/knowledge/kuzhambu-knowledge-infra spotless:apply
mvn -pl biz/knowledge/kuzhambu-knowledge-application -am test
mvn -pl biz/knowledge/kuzhambu-knowledge-interface -am test
mvn -pl biz/knowledge/kuzhambu-knowledge-infra -am test
```

前端：

```sh
cd kuzhambu-apps
npm --workspace admin-web run format
npm run format:check
npm run lint
npm --workspace admin-web run test -- --runInBand
npm run build
```

菜单种子：

```sh
node scripts/generate-system-data-sql.ts --check
```

## Review Focus

本 RUNBOOK 已按已确认口径收敛：

- `Phase 1` 只收页面交付范围，不收结构范围。
- 精修采用完整独立结构，允许新增表。
- 质量标注对象固定为 `ENTITY`、`RELATION`，前端本轮不开放。
- 质量指标固定为运行时聚合计算。
- 门类以 Classics 主内容为真相源，Knowledge 侧做查询冗余。
- 审计固定做到字段级 `before / after`。
