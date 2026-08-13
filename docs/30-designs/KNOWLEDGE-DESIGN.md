# Knowledge Design

## Purpose

本文档定义 Knowledge 域中标签治理的已落地设计。

知识图谱的正式产品需求由 [`KNOWLEDGE-GRAPH-REQUIREMENTS.md`](../10-requirements/KNOWLEDGE-GRAPH-REQUIREMENTS.md) 定义。图谱的素材空间、发布空间、抽取、Schema、治理、删除任务与接口详细设计必须另建专项设计，不得复用本文件此前的候选确认、图谱版本或正式结果模型。

## Module

```text
kuzhambu-servers/biz/knowledge/
  kuzhambu-knowledge-interface/
  kuzhambu-knowledge-application/
  kuzhambu-knowledge-domain/
  kuzhambu-knowledge-infra/
```

## Business Boundary

Knowledge 拥有统一标签及其全局治理解释权，不拥有 Classics 正式内容主数据。

对于 Classics 通用标签，Knowledge 负责统一标签解析、别名、自动创建策略和内容引用投影；内容域拥有内容上的标签绑定主事实。Knowledge 标签治理负责全局改名、合并、复制、分拆、废弃、影响预览和审计。

## DDD Model

- `Tag`
- `TagCategory`
- `TagAlias`
- `TagReviewItem`

## Data Model

表名前缀统一使用 `knowledge_`。

核心表：

- `knowledge_tag`
- `knowledge_tag_category`
- `knowledge_tag_alias`
- `knowledge_tag_content_ref`
- `knowledge_tag_review_item`

## Application Layer

- `TaxonomyApplicationService`

Application 层负责标签治理、审核、别名管理、全局改名、合并、复制、分拆、废弃、影响预览和统计聚合。

跨域协作语义：

- `KnowledgeTagBindingDomainService` 提供统一标签解析、手工标签自动创建、AI 标签自动创建、内容引用同步和内容引用删除。
- `knowledge_tag_content_ref` 是 Knowledge 侧派生引用模型，不承载 Classics 内容标签排序、绑定状态和标签展示快照。
- Classics 通过协作语义回写内容引用，Knowledge taxonomy 后台 CRUD 不作为跨域调用入口。

标签库全局治理语义：

- 内容侧标签编辑只改变当前内容的标签绑定；全局标签改名、合并、复制、分拆和废弃必须在 Knowledge taxonomy 治理内完成。
- Knowledge taxonomy 是全局标签治理的发起方、预览方和审计方；跨内容的绑定变更必须调用内容所属域的协作接口更新绑定主事实，再由内容域同步 `knowledge_tag_content_ref` 投影。
- 标签合并用于把源标签下选定或全部内容绑定迁移到目标标签，并保留源标签的合并、别名或废弃状态以支持历史追溯。
- 标签复制用于把源标签下选定或全部内容追加绑定到目标标签，不移除源标签原有绑定。
- 标签分拆用于把源标签下选定内容绑定迁移到新标签或既有目标标签；实现语义等同于先复制选定绑定到目标标签，再从源标签移除这些绑定。
- 标签废弃禁止后续新增绑定继续使用该标签，但历史引用、统计和审计仍可查看。
- 全局标签改名直接修改 `knowledge_tag` 主数据名称，并通过内容域协作刷新当前内容标签快照和检索投影；历史版本快照保持原值。执行前必须按内容类型提供影响范围预览，并由 Knowledge 侧记录治理审计。

## Interface Layer

Admin 入口：

- 标签治理。

图谱 Admin、Portal 和图谱质量入口由后续知识图谱专项设计定义。

## Infrastructure Layer

- Repository 持久化标签相关 `knowledge_*` 表。
- 标签协作通过 `KnowledgeTagBindingDomainService` 与内容域交互。

## Data Ownership

Knowledge 是本文件列出的标签 `knowledge_*` 表的唯一写入方。Classics 删除或下线内容时，Knowledge 通过协作语义更新 `knowledge_tag_content_ref`。

协作兼容口径：

- Knowledge 内容类型内部仍使用 `MING_CUSTOM`，但接受 Classics 传入的 `MING_CUSTOMS` 协作值。
- Knowledge 标签来源内部仍使用 `AI_EXTRACTED`，但接受 Classics 传入的 `AI` 协作值。

## Observability

- 标签审核、合并和废弃通过 System 审计记录。

## Acceptance

- 标签及其治理操作在 Knowledge 域内闭合。
- 全局标签治理通过内容域协作更新内容标签绑定主事实，并同步 `knowledge_tag_content_ref` 投影。
- 搜索和问答可消费 Knowledge 增强能力，但不依赖知识图谱作为前置。
