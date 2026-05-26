# QA Requirements

## Purpose

智能问答模块定义基于用户可见知识库内容回答自然语言问题的需求。

## Scope

覆盖：
- 三才图会、王圻文档和明代习俗多库问答。
- 王圻文档单文档追加式问答。
- 多轮会话。
- 来源引用。
- 会话列表、删除和导出。
- 管理员上下文调试。
- 权限过滤。
- 同义词扩展和实体识别增强。

不覆盖：
- 知识图谱作为问答前置依赖。
- AI 模型和提示词后台管理。
- 搜索页面展示。
- 问答对生成和管理。


## Functional Requirements

- 必须支持自然语言提问。
- 必须支持选择问答覆盖的知识库范围。
- 问答覆盖范围必须支持三才图会、王圻文档和明代习俗。
- 必须支持在王圻文档详情中围绕单篇文档追加提问。
- 必须支持多轮对话。
- 多轮对话必须保留最近 3 轮上下文。
- 回答必须标注来源。
- 来源必须可跳转到对应内容。
- 来源必须能让用户识别知识库、标题和内容位置。
- 必须支持会话列表、删除和导出。
- 管理员必须能查看问答调试信息。
- 必须支持同义词扩展和实体识别增强问答检索。

## Business Rules

- 问答上下文必须按当前用户权限过滤。
- 私有内容不得进入非授权用户上下文。
- 权限过滤必须在问答候选内容进入回答上下文前完成。
- 知识图谱未生成或不可用时，问答仍必须基于用户可见内容工作。
- 会话删除不得删除原始知识库内容。
- 被删除或无权访问的来源不得展示正文内容，应显示不可用提示。
- 单文档追加式问答只能使用当前文档和用户有权访问的关联上下文。
- 问答对生成属于 AI Refinement 和各内容编辑页内联能力，不属于智能问答会话模块。
- 回答生成失败时必须保留用户问题，并允许用户重试。

## Acceptance Criteria

- 用户能提出自然语言问题并得到带来源的回答。
- 用户能选择一个或多个知识库作为问答范围。
- 用户能在王圻文档详情中围绕当前文档追问。
- 用户刷新页面后仍可查看历史会话。
- 多轮追问能使用最近上下文回答。
- 私有或无权访问内容不会进入非授权用户问答上下文。
- 来源内容被删除后显示不可用提示。
- 管理员能查看问答使用的上下文和来源追溯信息。

## Related Documents

- [AUTH-REQUIREMENTS.md](./AUTH-REQUIREMENTS.md)：提供问答上下文进入回答前的权限判断。
- [SANCAI-KNOWLEDGE-REQUIREMENTS.md](./SANCAI-KNOWLEDGE-REQUIREMENTS.md)、[WANGQI-DOCUMENT-REQUIREMENTS.md](./WANGQI-DOCUMENT-REQUIREMENTS.md)、[MING-CUSTOMS-REQUIREMENTS.md](./MING-CUSTOMS-REQUIREMENTS.md)：提供问答使用的三类知识库内容及公开、私有、归档、删除规则；其中王圻文档还提供单文档追加式问答入口和当前文档上下文。
- [TAXONOMY-REQUIREMENTS.md](./TAXONOMY-REQUIREMENTS.md)：提供同义词扩展能力；问答消费词典但不维护词典。
- [AI-CONFIG-PROMPT-REQUIREMENTS.md](./AI-CONFIG-PROMPT-REQUIREMENTS.md)：提供回答生成所需的模型和提示词配置。
- [AI-REFINEMENT-REQUIREMENTS.md](./AI-REFINEMENT-REQUIREMENTS.md)：负责问答对生成；本模块负责自然语言问答会话。
- [KNOWLEDGE-GRAPH-REQUIREMENTS.md](./KNOWLEDGE-GRAPH-REQUIREMENTS.md)：问答不得依赖知识图谱作为前置条件。

## Open Items

无
