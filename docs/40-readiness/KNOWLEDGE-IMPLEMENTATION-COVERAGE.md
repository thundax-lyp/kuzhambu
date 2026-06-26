# Knowledge Implementation Coverage

## Purpose

本文档记录 Knowledge 当前实现对需求文档的覆盖状态，用于后续补充开发、联调验收和范围控制。

本文档不替代 `docs/10-requirements/KNOWLEDGE-REQUIREMENTS.md`、`docs/30-designs/KNOWLEDGE-DESIGN.md` 或阶段性 RUNBOOK。

本文档要求：

- 覆盖 `KNOWLEDGE-REQUIREMENTS.md` 的全部需求项。
- 对已形成运行时代码、页面入口或数据种子闭环的能力明确标记 `已完成`。
- 对已有局部实现但仍缺关键联调、验证或端到端闭环的能力明确标记 `部分完成`。
- 对当前仓库尚未形成可执行交付物的能力统一标记 `未完成`。

## Status Definition

- `已完成`：当前仓库已有可追溯交付物，并已形成运行时代码、页面入口或数据种子闭环。
- `部分完成`：已有模型、接口或页面骨架，但仍缺关键联调、验证或端到端闭环。
- `未完成`：当前仓库尚未形成可执行交付物。

## Current Baseline

已完成：

- Knowledge 已按 `domain -> application -> infra -> interface` 分层落地 taxonomy 子域，标签分类、标签、标签别名、标签内容引用、同义词均已建立独立模型、Repository、Application Service 和 Admin Interface。
- 后端 `/api/knowledge/taxonomy` 已提供标签分类分页/创建/更新/状态变更、标签分页/详情/创建/更新/状态变更、待审核标签分页/审核、标签别名列表/新增/删除、同义词分页/创建/更新/状态变更/删除接口，并补齐 `knowledge:taxonomy:view|edit|review` 权限点。
- Admin Web 已接入 `/knowledge/taxonomy` 页面，支持标签分类分页、创建、编辑、启用、禁用。
- Admin Web 已接入统一标签分页、详情、创建、编辑、启用、禁用，并在详情中展示内容引用数量和内容引用明细。
- Admin Web 已接入待审核标签列表、标签审核抽屉、通过和拒绝动作。
- Admin Web 已接入标签别名列表、新增和删除能力，并复用标签详情抽屉作为治理入口。
- Admin Web 已接入同义词分页、创建、编辑、启用、禁用和删除能力。
- Admin Web 已接入 taxonomy 治理补完，支持标签合并影响预览、标签合并动作、标签废弃动作，以及标签使用排行、知识库分布、来源占比和月度新增趋势统计。
- Knowledge 已补充 `KnowledgeTagBindingDomainService`，为 Classics 通用标签提供统一标签解析、手工/AI 标签自动创建、内容引用同步和内容引用删除能力。
- Knowledge taxonomy 已补充与 Classics 协作的兼容口径：接受 `MING_CUSTOMS` 内容类型输入和 `AI` 标签来源输入，并在仓储写入时归一化为内部口径。
- Knowledge 已补齐后端自动化测试，覆盖标签绑定协作语义和 taxonomy 兼容口径。
- `db/data-source/system.json` 与 `db/data/system.sql` 已收敛到当前阶段的 `知识治理 / 标签与同义词` 菜单结构，并通过现有脚本重新生成。
- Knowledge 已新增 `knowledge_graph_extraction_task` 任务台账，并通过 `KnowledgeGraphExtractionApplicationService` 支持 `RELATION`、`GRAPH`、`LINEAGE` 三类抽取任务创建、分页、详情和候选应用动作。
- Knowledge 已补充 `KnowledgeAiExtractionDomainService` 协作链路，经 AI 域解析稳定 `operation + capability + workerPath` 后调用 workers 三个 Knowledge usecase，并写入 `ai_call_record` 与 `ai_candidate`。
- Knowledge 图谱抽取已补齐 `batchId` 关联、批量创建、批任务取消、重生成和请求快照持久化，`knowledge_graph_extraction_task` 现可追踪 `triggerSource`、`selectionScopeJson`、`replaceUnconfirmedOnly` 与可重放请求输入。
- Knowledge 已补齐候选结果应用链路，可将 `ai_candidate.result_payload` 应用到 `knowledge_entity`、`knowledge_relation`、`knowledge_graph_version`、`knowledge_lineage_node`、`knowledge_lineage_relation`，并生成或续增图谱版本号。
- Knowledge 已补充面向 Discovery 的 taxonomy 读协作服务，`expandSynonyms`、`getTagHint` 和 `listEntityHints` 可直接为搜索与问答提供同义词、标签和实体提示。
- Admin Web 已接入 `/knowledge/graph-extraction` 页面，支持三类抽取任务创建、批量范围输入、质量触发模式、任务分页、任务详情抽屉、重生成、批任务取消和候选应用动作。
- Admin Web 已接入 `/knowledge/graph-results` 页面，支持图谱版本列表、版本详情，以及从版本下钻查看正式实体、正式关系和正式世系结果。
- Portal Web 已接入 Knowledge 只读门户：首页 `/knowledge`、质量页 `/knowledge/quality` 和图谱分层浏览页 `/knowledge/atlas` 已形成入口闭环。
- Portal Web 图谱浏览已支持 `overview -> category -> detail` 三层 URL 状态、门类下钻、实体下钻和 breadcrumb 返回导航。

部分完成：

- 当前已完成 Knowledge / AI 后端专项测试、workers 契约测试、Admin Web `format:check` / `lint` / `test` / `build` 和菜单 SQL 生成校验，但尚未补充 Playwright 闭环与跨服务联调冒烟记录。

未完成：

- 图谱可视化画布和按门类聚合的质量报告仍未落地。

## Requirement Coverage Matrix

### 标签与同义词

| 需求项                                           | 状态     | 已完成部分                                                                                                                     | 未完成部分                                      | 责任域                 |
| ------------------------------------------------ | -------- | ------------------------------------------------------------------------------------------------------------------------------ | ----------------------------------------------- | ---------------------- |
| 跨知识库统一标签查看、搜索和筛选                 | 已完成   | 后端标签分页接口和 Admin Web 列表已支持按名称、分类、状态、来源和审核状态查看与筛选统一标签                                    | 无                                              | Knowledge, Admin Web   |
| 按标签检索关联内容                               | 已完成   | 标签详情和内容引用明细已支持查看关联内容数量与内容清单                                                                         | 无                                              | Knowledge, Admin Web   |
| 标签详情查看                                     | 已完成   | 已展示标签名称、分类、别名、关联内容数量和标签状态                                                                             | 无                                              | Knowledge, Admin Web   |
| 待审核标签列表                                   | 已完成   | 已提供待审核分页、来源类型、关联内容数量和审核入口                                                                             | 无                                              | Knowledge, Admin Web   |
| 管理员逐条审核 AI 新标签                         | 已完成   | 已支持待审核标签逐条通过和拒绝                                                                                                 | 无                                              | Knowledge, Admin Web   |
| 审核通过时选择正式分类                           | 已完成   | 标签审核请求已支持分类选择并回写正式分类                                                                                       | 无                                              | Knowledge, Admin Web   |
| 审核拒绝并退出可用标签集合                       | 已完成   | 拒绝后标签保留治理记录，但不再进入待审核通过集合和正常可用集合                                                                 | 无                                              | Knowledge, Admin Web   |
| 标签分类维护                                     | 已完成   | 已支持分页、创建、更新、启用和禁用                                                                                             | 无                                              | Knowledge, Admin Web   |
| 标签别名维护                                     | 已完成   | 已支持别名列表、新增和删除                                                                                                     | 无                                              | Knowledge, Admin Web   |
| 标签合并                                         | 已完成   | 已支持标签合并影响预览、源标签并入目标标签、历史内容引用复制和合并后别名/名称解析到目标标签                                    | 无                                              | Knowledge, Admin Web   |
| 标签废弃                                         | 已完成   | 已支持标签废弃动作，并让废弃标签退出新的可用集合同时保留治理记录                                                               | 无                                              | Knowledge, Admin Web   |
| 标签合并前展示影响                               | 已完成   | 已支持预览源标签、目标标签、别名、内容引用和治理影响                                                                           | 无                                              | Knowledge, Admin Web   |
| 标签使用排行、知识库分布、来源占比、月度新增趋势 | 已完成   | 已提供完整治理统计读取和 Admin Web 展示入口                                                                                    | 无                                              | Knowledge, Admin Web   |
| 同义词新增、编辑、删除、查看和搜索               | 已完成   | 后端和 Admin Web 已支持分页、创建、更新、状态变更、删除                                                                        | 无                                              | Knowledge, Admin Web   |
| 同义词正向和反向查询                             | 部分完成 | 域模型和管理入口已支持词条治理；标签解析链路可消费治理结果                                                                     | 面向搜索和问答的独立正向/反向查询入口未对外提供 | Knowledge, Discovery   |
| 搜索和问答使用同义词扩展                         | 部分完成 | 同义词治理模型已可作为下游增强数据源                                                                                           | Discovery/Search/QA 消费链路当前仍未接通        | Knowledge, Discovery   |
| 标签审核、合并和废弃操作审计                     | 已完成   | 审核、合并和废弃操作继续由 System 审计承载；审计注解、快照模型和运行时 SPI 已下沉到 `common-audit`，Knowledge / System 已切换到统一审计契约 | 无                                              | Knowledge, System      |
| Classics 内容标签协作与兼容口径                  | 已完成   | 已提供统一标签绑定协作语义、内容引用同步/删除能力，并兼容 Classics 的 `MING_CUSTOMS` 与 `AI` 协作输入                          | 无                                              | Knowledge, Classics    |
| 后台菜单与页面入口                               | 已完成   | `system.json` 已新增 `知识治理 / 标签与同义词` 菜单；`system.sql` 已由脚本生成同步；Admin Web 路由已接入 `/knowledge/taxonomy` | 无                                              | System Data, Admin Web |
| taxonomy 权限点                                  | 已完成   | `knowledge:taxonomy:view`、`knowledge:taxonomy:edit`、`knowledge:taxonomy:review` 已进入菜单种子和后端 `@HasPermission` 控制   | 无                                              | Knowledge, System      |

### 数据精修

| 需求项                     | 状态     | 已完成部分                                                                                                                                         | 未完成部分                                        | 责任域               |
| -------------------------- | -------- | -------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------- | -------------------- |
| 待精修内容筛选             | 已完成   | 已提供 `knowledge_refinement_task`、后端 `/api/knowledge/refinement/task/page` 与 Admin Web `/knowledge/refinement` 工作台，支持按任务状态和来源筛选 | 无                                                | Knowledge, Admin Web |
| 实体确认、编辑、删除和新增 | 已完成   | 已提供实体草稿表、实体新增/更新/确认/删除接口、应用服务与页面编辑表格；保存后可将结果应用回正式实体事实                                           | 无                                                | Knowledge, Admin Web |
| 关系确认、编辑、删除和新增 | 已完成   | 已提供关系草稿表、关系新增/更新/确认/删除接口、应用服务与页面编辑表格；保存后可将结果应用回正式关系事实                                           | 无                                                | Knowledge, Admin Web |
| 人工确认状态               | 已完成   | 实体、关系和世系草稿均已支持 `DRAFT / CONFIRMED / DELETED` 状态流转，应用任务时会保留人工确认结果并更新任务状态                                   | 无                                                | Knowledge            |
| 按门类筛选待精修内容       | 已完成   | `knowledge_graph_version` 与 `knowledge_refinement_task` 已冗余 `source_category_code/name`，工作台已支持按门类筛选                               | 无                                                | Knowledge, Admin Web |
| 质量评估人工标注入口       | 部分完成 | 已提供 `knowledge_quality_annotation` 表、标注写入/删除/分页接口、质量汇总聚合与后端测试，标注对象覆盖 `entity` / `relation`                      | Admin Web 当前未开放独立质量标注入口              | Knowledge            |

### 知识图谱

| 需求项                                     | 状态   | 已完成部分                                                                                                                                                    | 未完成部分                           | 责任域                            |
| ------------------------------------------ | ------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------ | --------------------------------- |
| 三才图会实体和关系 AI 提取                 | 已完成 | Knowledge 可创建 `RELATION` / `GRAPH` 抽取任务，经 AI 域调用 workers 并生成 `ai_call_record`、`ai_candidate`，再由 Knowledge 应用为正式实体和关系事实         | 无                                   | Knowledge, AI, Workers            |
| 异步提取任务和进度展示                     | 已完成 | 已提供任务台账、任务状态回填、失败原因、`aiCallId` / `aiCandidateId`、完成时间和应用时间，并在 Admin Web 提供任务页、详情抽屉与应用动作                       | 无                                   | Knowledge, AI, Admin Web          |
| 鸟瞰层、门类层和详情层图谱浏览             | 已完成 | Portal Web `/knowledge/atlas` 已支持 `overview -> category -> detail` 三层浏览、URL 状态恢复和下钻入口                                                    | 无                                   | Knowledge, Portal Web            |
| 鸟瞰层展示 14 大门类实体数量和分布         | 部分完成 | Portal 总览层已按当前正式图谱中的真实门类数据展示门类卡片、实体数、关系数和版本号                                                                          | 仍未补齐固定 14 类空位展示与可视分布 | Knowledge, Portal Web            |
| 门类层展示卷间关联和实体关系               | 已完成 | Portal 门类层已展示版本信息、实体高亮、关系分组和来源摘要                                                                                                   | 无                                   | Knowledge, Portal Web            |
| 详情层展示单实体信息、关系和来源条目       | 已完成 | Portal 实体层已展示实体卷宗、关系分组、来源条目、时间线和相关标签                                                                                           | 无                                   | Knowledge, Portal Web            |
| 鸟瞰、门类和详情面包屑导航                 | 已完成 | Portal `/knowledge/atlas` 已使用后端 breadcrumb 驱动 overview/category/detail 返回导航                                                                      | 无                                   | Knowledge, Portal Web            |
| 批量生成和重生成                           | 已完成 | 已支持按 `selectionScopeJson` 批量创建同类型图谱抽取任务、复用 AI 域 `batchId` 聚合子任务，并支持基于源任务重生成且区分 `replaceUnconfirmedOnly` 语义           | 无                                   | Knowledge, AI                     |
| 从质量报告或筛选结果批量触发提取           | 部分完成 | Admin Web 图谱抽取页已支持 `QUALITY_REPORT` 触发模式并可携带批量范围快照创建任务，后端已保留 `triggerSource` 和 `selectionScopeJson` 追溯信息                      | 尚未从独立质量报告页直接一键下钻触发 | Knowledge, AI                     |
| 读取数据精修修正结果                       | 已完成 | 精修工作台应用动作会将实体、关系和世系草稿写回正式事实表；正式结果页继续从正式事实和图谱版本读取，因此已消费精修结果                                | 无                                   | Knowledge                         |
| 质量报告与指标展示                         | 部分完成 | 已提供质量汇总运行时聚合能力和工作台摘要读取接口，可基于实体确认、关系确认和标注结果返回质量摘要                                                      | 独立质量报告页面和门类级统计大盘未完成 | Knowledge                         |
| 质量报告按门类分组并支持低质量门类触发提取 | 未完成 | 需求已沉淀                                                                                                                                                    | 当前仓库尚未提供质量报告与重提取入口 | Knowledge, AI                     |
| 最近提取版本和提取时间展示                 | 已完成 | 已在 `knowledge_graph_version` 建立版本台账，应用正式结果时会生成或续增版本号，并在 Admin Web 正式结果页提供版本列表、版本详情和应用时间展示                  | 无                                   | Knowledge, Admin Web              |
| 世系图专用提取和展示                       | 已完成 | 已支持 `LINEAGE` 抽取任务、workers 世系候选输出、正式 `knowledge_lineage_node` / `knowledge_lineage_relation` 落库，以及 Admin Web 正式世系结果列表和详情读取 | 无                                   | Knowledge, AI, Workers, Admin Web |

### 运行时验证

| 需求项             | 状态     | 已完成部分                                                                                                                                                                   | 未完成部分                           | 责任域                            |
| ------------------ | -------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------ | --------------------------------- |
| 当前阶段运行时验证 | 部分完成 | 已完成 taxonomy、图谱抽取、精修后端专项测试，workers Knowledge usecase 契约测试，Admin Web `format:check` / `lint` / `test` / `build`，以及 `node scripts/generate-system-data-sql.ts --check` | 缺少 Playwright 与跨服务联调冒烟记录 | Knowledge, AI, Workers, Admin Web |

## Unfinished Focus

| 能力项                              | 状态   | 说明                                                                                                   |
| ----------------------------------- | ------ | ------------------------------------------------------------------------------------------------------ |
| 数据精修                            | 部分完成 | 实体、关系和世系精修工作台已落地，正式事实回写和质量汇总已接通；独立质量标注前端入口仍未开放           |
| 图谱浏览与质量报告                  | 部分完成 | 当前已交付 Portal 图谱分层浏览页、质量摘要页、抽取任务、批量生成、重生成、候选应用、正式结果落库和后台读取页 |
| 世系图浏览                          | 未完成 | 当前只交付世系抽取、正式结果落库和后台读取，不含可视化页面                                             |
| Portal 页面                         | 已完成 | Portal 侧已形成 `/knowledge`、`/knowledge/quality`、`/knowledge/atlas` 三个可执行只读入口              |
| 数据精修与图谱联动                  | 部分完成 | 精修应用已回写正式事实并被正式结果读取链路消费，图谱抽取也已具备批量重生成能力                             |
| Discovery 搜索或问答接入            | 已完成 | taxonomy 治理、同义词扩展、标签提示和实体提示已被 Discovery 搜索 / 问答消费，形成最小闭环             |
| 标签批量操作                        | 未完成 | 当前只交付单条治理动作，未支持批量合并、批量废弃或批量审核                                             |
| Classics 内容编辑页内联知识治理入口 | 未完成 | 当前只提供独立 taxonomy 页面，未内联到 Classics 内容编辑页                                             |

## Residual Risks

- 菜单种子重生成后 `system_menu` 的树编号和自增值已随节点数收缩变化，后续若依赖固定菜单 ID，需要以当前生成结果为准重新校对。
- taxonomy 页面、精修工作台和正式结果页目前以页面级查询和抽屉交互为主；后续再改权限、字段或接口返回时，建议补前端契约测试和 Playwright 冒烟。
- 同义词、标签和实体提示已接通 Discovery 搜索 / 问答主链路，后续主要关注命中质量与提示规则调优。
- 当前已补齐 Portal 只读入口、图谱分层浏览和图谱抽取批量闭环，但仍未提供图可视化画布、固定 14 门类空位展示或按门类聚合的质量报告；后续扩展前仍需先明确读模型和展示边界。
