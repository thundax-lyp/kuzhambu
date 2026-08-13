# Knowledge Requirements

## Purpose

Knowledge 域定义跨知识库标签治理，并为知识图谱能力提供域级边界。

Knowledge 负责把古籍内容组织成可治理、可检索、可增强问答和可评估质量的结构化知识。

## Scope

覆盖：

- 跨三才图会、王圻文档和明代习俗的统一标签治理。
- 标签检索、关联内容查看、审核、分类、合并、废弃、别名和详情管理。
- 标签使用排行、知识库分布、来源占比和新增趋势统计。

不覆盖：

- AI 标签提取和问答生成的模型配置。
- AI workers 直接调用。
- 内容页内标签人工编辑入口。
- 搜索结果页面展示。
- 问答会话。
- 知识图谱的产品需求、交互、发布模型、Schema 和抽取任务；见 `KNOWLEDGE-GRAPH-REQUIREMENTS.md`。

## Functional Requirements

### 标签治理

- 必须支持跨知识库查看、搜索和筛选统一标签。
- 必须支持按标签检索关联内容。
- 必须支持标签详情查看，展示标签名称、分类、别名、关联内容数量和标签状态。
- 必须支持待审核标签列表，展示标签名称、来源类型、关联内容数量和提取时间。
- 必须支持管理员逐条审核 AI 自动提取的新标签。
- 必须支持审核通过时为标签选择正式分类。
- 必须支持审核拒绝，并使被拒绝标签退出可用标签集合。
- 必须支持标签分类维护、标签别名维护、标签合并和标签废弃。
- 标签合并前必须展示合并影响。
- 必须支持标签使用排行、按知识库查看标签使用分布、AI 自动提取标签和人工标签占比、月度新增标签趋势。
- 必须记录标签审核、合并和废弃操作，记录由 System 审计承载。
- 必须为 Operations 周报和月报提供聚合后的 summary 只读结果。
- Operations summary 结果必须显式返回 `periodStart` 和 `periodEnd`。
- 周报趋势序列必须按日聚合，月报趋势序列必须按周聚合。
- Operations summary 结果必须包含：
  - `tagCoverageRate`
  - `topTags`：`tagName`、`contentRefCount`
  - `categoryDistributions`：`categoryName`、`tagCount`
  - `monthlyNewTags`：`bucket`、`tagCount`

## Business Rules

- 统一标签跨知识库共享，标签不归属单个知识库。
- AI 自动提取的新标签必须进入人工治理流程。
- 审核通过的标签才能作为正式分类标签参与平台治理。
- 审核拒绝的标签不得继续作为可用标签参与检索或筛选。
- 标签合并后历史内容应能通过目标标签检索。
- 标签合并后源标签名称和源标签已有别名必须并入目标标签别名。
- 废弃标签不参与新的推荐、语义匹配、搜索扩展或问答扩展。
- 废弃标签的历史关联必须保留，用于回溯和统计。
- 标签为空不得阻止内容保存。
- 标签治理操作必须可追溯到操作人和操作时间。
- 摘要和问答对精修应在对应内容的编辑页或详情页内联完成。
- 面向 Operations 的 summary 输出必须以 application result 或 read model 暴露，不得直接复用 Knowledge admin controller response。

## Acceptance Criteria

- 管理员能处理待审核标签，并在通过时选择分类。
- 管理员拒绝标签后，该标签不再出现在可用标签集合中。
- 标签合并后源标签关联内容可通过目标标签找到。
- 管理员废弃标签后，该标签不再用于新的检索扩展，但历史统计仍可查看。
- 管理员能查看标签使用 Top 20、知识库分布、AI 自动提取与人工标签占比、月度新增趋势。
- 用户搜索标签别名时能召回相关内容。
- 内容没有标签时仍可保存。

## Related Documents

- [CLASSICS-REQUIREMENTS.md](./CLASSICS-REQUIREMENTS.md)：提供知识组织覆盖的古籍内容范围和生命周期规则。
- [KNOWLEDGE-GRAPH-REQUIREMENTS.md](./KNOWLEDGE-GRAPH-REQUIREMENTS.md)：定义三才图会知识图谱的素材空间、发布空间、抽取和治理需求。
- [AI-REQUIREMENTS.md](./AI-REQUIREMENTS.md)：提供实体关系提取和标签提取所需 AI 能力。
- [WORKERS-REQUIREMENTS.md](./WORKERS-REQUIREMENTS.md)：定义 workers 边界；Knowledge 不直接调用 AI workers。
- [DISCOVERY-REQUIREMENTS.md](./DISCOVERY-REQUIREMENTS.md)：消费标签、实体和图谱增强知识检索和知识问答。
- [SYSTEM-REQUIREMENTS.md](./SYSTEM-REQUIREMENTS.md)：承载标签治理和精修操作审计。
