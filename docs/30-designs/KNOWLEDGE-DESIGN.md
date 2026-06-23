# Knowledge Design

## Purpose

本文档定义 Knowledge 域当前已落地设计，覆盖标签治理、同义词和知识抽取任务闭环。

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

## Application Layer

- `TagApplicationService`
- `SynonymApplicationService`
- `KnowledgeGraphExtractionApplicationService`

Application 层负责标签治理、同义词扩展、知识抽取任务编排、AI 候选状态回填、候选结果应用和图谱版本关联。

跨域协作语义：

- `KnowledgeTagBindingDomainService` 提供统一标签解析、手工标签自动创建、AI 标签自动创建、内容引用同步和内容引用删除。
- `knowledge_tag_content_ref` 是 Knowledge 侧派生引用模型，不承载 Classics 内容标签排序、绑定状态和标签展示快照。
- Classics 通过协作语义回写内容引用，Knowledge taxonomy 后台 CRUD 不作为跨域调用入口。

## Interface Layer

Admin 入口：

- 标签治理。
- 同义词维护。
- 知识抽取任务创建、分页、详情和候选应用。

Portal 入口：

- 当前仅开放标签相关只读能力；图谱浏览未在当前交付阈值内落地。

## Infrastructure Layer

- Repository 持久化 `knowledge_*` 表。
- AI 提取通过 `KnowledgeAiExtractionDomainService` 协作语义触发。
- `KnowledgeGraphCandidateApplySupport` 负责把候选 payload 应用到 `knowledge_entity`、`knowledge_relation`、`knowledge_graph_version`、`knowledge_lineage_node` 和 `knowledge_lineage_relation`。

## Data Ownership

Knowledge 是 `knowledge_*` 表的唯一写入方。Classics 删除或归档内容时，Knowledge 通过协作语义更新 `knowledge_tag_content_ref` 等来源引用和质量指标。

协作兼容口径：

- Knowledge 内容类型内部仍使用 `MING_CUSTOM`，但接受 Classics 传入的 `MING_CUSTOMS` 协作值。
- Knowledge 标签来源内部仍使用 `AI_EXTRACTED`，但接受 Classics 传入的 `AI` 协作值。

## Observability

- 标签审核、合并、废弃和精修保存通过 System 审计记录。
- 图谱提取任务记录 `aiCallId`、`aiCandidateId`、失败原因、完成时间和应用时间。

## Acceptance

- 标签、同义词、知识抽取任务和正式知识事实在一个业务域内闭合。
- 搜索和问答可消费 Knowledge 增强能力，但当前不依赖图谱浏览或质量报告作为前置。
