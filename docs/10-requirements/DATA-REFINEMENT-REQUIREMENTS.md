# Data Refinement Requirements

## Purpose

数据精修工作台模块定义集中处理实体标注和关系抽取的需求。摘要编辑和问答对管理属于各内容编辑页或详情页的内联能力，不进入独立工作台。

## Scope

覆盖：
- 待精修内容筛选。
- 实体标注。
- 关系抽取和修正。
- 人工确认结果。
- 知识图谱质量评估所需的人工标注入口。

不覆盖：
- 图谱可视化。
- 搜索结果页面。
- AI 配置后台。
- 摘要编辑。
- 问答对管理。
- AI 摘要和问答对生成的底层执行。


## Functional Requirements

- 必须支持待精修内容筛选。
- 必须支持实体确认、编辑、删除和新增。
- 必须支持关系确认、编辑、删除和新增。
- 必须支持人工确认状态。
- 必须支持按门类筛选待精修内容。
- 必须支持为知识图谱质量评估提供人工标注入口。

## Business Rules

- 人工确认结果优先于 AI 初始结果。
- 实体和关系精修主要服务三才图会知识图谱质量。
- 摘要和问答对精修应在对应内容的编辑页或详情页内联完成。
- 实体和关系精修保存后，应触发知识图谱质量相关信息更新。
- 精修保存必须可追溯。

## Acceptance Criteria

- 用户能在集中工作台内完成实体和关系整理。
- 人工确认结果不会被新的 AI 结果静默覆盖。
- 删除实体或关系前必须二次确认。
- 用户能从待精修筛选进入具体内容的精修工作台。
- 实体或关系精修后，知识图谱质量相关信息能反映变化。

## Related Documents

- [KNOWLEDGE-GRAPH-REQUIREMENTS.md](./KNOWLEDGE-GRAPH-REQUIREMENTS.md)：消费本模块保存后的实体和关系修正结果，并反映到图谱质量。
- [SANCAI-KNOWLEDGE-REQUIREMENTS.md](./SANCAI-KNOWLEDGE-REQUIREMENTS.md)：提供三才图会内容上下文，本模块主要服务三才图会知识图谱质量。
- [AI-REFINEMENT-REQUIREMENTS.md](./AI-REFINEMENT-REQUIREMENTS.md)：负责摘要和问答对生成；本模块不负责摘要和问答对管理。
- [WANGQI-DOCUMENT-REQUIREMENTS.md](./WANGQI-DOCUMENT-REQUIREMENTS.md)、[MING-CUSTOMS-REQUIREMENTS.md](./MING-CUSTOMS-REQUIREMENTS.md)：摘要和问答对在对应内容页内联维护，不进入本模块。

## Open Items

无
