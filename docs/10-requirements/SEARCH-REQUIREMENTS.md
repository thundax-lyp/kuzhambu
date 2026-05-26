# Search Requirements

## Purpose

跨库搜索模块定义三才图会、王圻文档和明代习俗的统一检索需求。

## Scope

覆盖：
- 跨三库统一搜索。
- 按知识库分组展示结果。
- 关键词高亮。
- 高级筛选。
- 查询理解、同义词扩展、查询清洗、实体识别和实体链接。
- 查询改写。
- 相关性排序。
- 权限过滤。
- 搜索日志。
- 搜索结果深链和查询状态保留。

不覆盖：
- 智能问答生成。
- 知识图谱浏览。
- 知识图谱作为搜索前置依赖。
- 同义词词典维护。
- 标签治理。


## Functional Requirements

- 必须支持搜索三才图会、王圻文档和明代习俗。
- 搜索结果必须按知识库分组展示。
- 每个知识库分组内必须按相关性排序。
- 必须支持关键词高亮。
- 必须支持按知识库、门类、标签、状态和时间等条件筛选。
- 必须支持查询理解和意图识别。
- 必须支持同义词扩展、查询清洗、停用词过滤和查询改写。
- 必须支持实体识别和实体链接。
- 必须按当前用户权限过滤结果。
- 必须记录搜索行为用于质量分析。
- 必须记录查询词、识别意图、结果数量和用户点击。
- 必须支持搜索结果深链，并在返回搜索页时保留查询状态。

## Business Rules

- 知识图谱未生成或不可用时，搜索仍必须可用。
- 私有内容不得出现在非授权用户搜索结果中。
- 权限过滤必须在搜索结果展示前完成。
- 搜索结果必须能跳转到内容详情。
- 权限过滤后无结果时按无结果处理。
- 同义词扩展必须使用 Taxonomy Requirements 中维护的同义词词典。
- 同义词扩展必须有数量限制，避免查询失控。
- 实体识别和实体链接用于改进搜索召回和结果说明，不代表搜索依赖知识图谱。
- 已归档内容不参与默认搜索结果。

## Acceptance Criteria

- 用户输入关键词后能看到分组结果。
- 用户能看到组内按相关性排列的结果。
- 用户无权访问的内容不会出现在结果中。
- 用户能通过知识库、门类、标签、状态或时间筛选结果。
- 用户搜索别名时能召回主词相关内容。
- 用户搜索实体名称时能看到相关内容。
- 用户能从结果跳转到对应详情页。
- 用户打开搜索结果深链时能恢复查询状态。
- 无结果时展示明确空状态，并提供修改关键词或清除筛选的提示。

## Related Documents

- [AUTH-REQUIREMENTS.md](./AUTH-REQUIREMENTS.md)：提供搜索结果展示前的权限判断。
- [SANCAI-KNOWLEDGE-REQUIREMENTS.md](./SANCAI-KNOWLEDGE-REQUIREMENTS.md)、[WANGQI-DOCUMENT-REQUIREMENTS.md](./WANGQI-DOCUMENT-REQUIREMENTS.md)、[MING-CUSTOMS-REQUIREMENTS.md](./MING-CUSTOMS-REQUIREMENTS.md)：提供可搜索的三类知识库内容及公开、私有、归档、删除规则。
- [TAXONOMY-REQUIREMENTS.md](./TAXONOMY-REQUIREMENTS.md)：提供标签、同义词和别名能力；搜索消费这些能力但不维护词典。
- [KNOWLEDGE-GRAPH-REQUIREMENTS.md](./KNOWLEDGE-GRAPH-REQUIREMENTS.md)：搜索可被实体识别和实体链接增强，但不得依赖知识图谱作为前置条件。
- [OPERATIONS-REQUIREMENTS.md](./OPERATIONS-REQUIREMENTS.md)：消费搜索日志和点击行为用于质量分析。

## Open Items

无
