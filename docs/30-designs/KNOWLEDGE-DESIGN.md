# Knowledge Design

## Purpose

本文档定义 Knowledge 域设计，覆盖标签治理、同义词、实体关系精修和三才图会知识图谱。

## Module

```text
kuzhambu-servers/biz/knowledge/
  kuzhambu-knowledge-interface/
  kuzhambu-knowledge-application/
  kuzhambu-knowledge-domain/
  kuzhambu-knowledge-infra/
```

## Business Boundary

Knowledge 拥有统一标签、同义词、实体、关系、精修状态、图谱提取版本、质量报告和世系图。Knowledge 消费 Classics 内容和 AI 提取能力，但不拥有正式内容主数据。

## DDD Model

- `Tag`
- `TagCategory`
- `TagAlias`
- `TagReviewItem`
- `Synonym`
- `Entity`
- `Relation`
- `RefinementTask`
- `GraphExtractionTask`
- `GraphVersion`
- `GraphQualityReport`
- `LineageGraph`

## Data Model

表名前缀统一使用 `knowledge_`。

核心表：

- `knowledge_tag`
- `knowledge_tag_category`
- `knowledge_tag_alias`
- `knowledge_tag_review_item`
- `knowledge_synonym`
- `knowledge_entity`
- `knowledge_relation`
- `knowledge_refinement_task`
- `knowledge_graph_extraction_task`
- `knowledge_graph_version`
- `knowledge_graph_quality_report`
- `knowledge_lineage_node`
- `knowledge_lineage_relation`

## Application Layer

- `TagApplicationService`
- `SynonymApplicationService`
- `DataRefinementApplicationService`
- `KnowledgeGraphApplicationService`
- `GraphQualityApplicationService`
- `LineageGraphApplicationService`

Application 层负责标签治理、同义词扩展、人工精修优先级、图谱提取任务、质量指标更新和世系图展示。

## Interface Layer

Admin 入口：

- 标签治理。
- 同义词维护。
- 数据精修工作台。
- 图谱生成、质量报告和世系图管理。

Portal 入口：

- 图谱浏览和标签相关只读能力按授权开放。

## Infrastructure Layer

- Repository 持久化 `knowledge_*` 表。
- 图谱可视化读模型由 infra 组装。
- AI 提取通过 AI application 能力触发。

## Data Ownership

Knowledge 是 `knowledge_*` 表的唯一写入方。Classics 删除或归档内容时，Knowledge 通过 application 协作更新来源引用和质量指标。

## Observability

- 标签审核、合并、废弃和精修保存通过 System 审计记录。
- 图谱提取任务记录进度、版本、失败原因和更新时间。

## Acceptance

- 标签、同义词、实体关系和图谱质量在一个业务域内闭合。
- 搜索和问答可消费 Knowledge 增强能力，但不依赖图谱作为前置。
