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
- 后端 `/api/knowledge/taxonomy` 已提供标签分类分页/创建/更新/状态变更、标签分页/详情/创建/更新/状态变更、待审核标签分页/审核、标签批量合并预览/执行、标签批量废弃、标签批量审核、标签别名列表/新增/删除、同义词分页/创建/更新/状态变更/删除接口，并补齐 `knowledge:taxonomy:view|edit|review` 权限点。
- Admin Web 已接入 `/knowledge/taxonomy` 页面，支持标签分类分页、创建、编辑、启用、禁用。
- Admin Web 已接入统一标签分页、详情、创建、编辑、启用、禁用，并在详情中展示内容引用数量和内容引用明细。
- Admin Web 已接入待审核标签列表、标签审核抽屉、逐条通过/拒绝和批量通过/拒绝动作。
- Admin Web 已接入标签别名列表、新增和删除能力，并复用标签详情抽屉作为治理入口。
- Admin Web 已接入同义词分页、创建、编辑、启用、禁用和删除能力。
- Admin Web 已接入 taxonomy 治理补完，支持单条/批量标签合并影响预览、合并动作、标签废弃动作、待审核标签批量审核，以及标签使用排行、知识库分布、来源占比和月度新增趋势统计。
- Knowledge 已补充 `KnowledgeTagBindingDomainService`，为 Classics 通用标签提供统一标签解析、手工/AI 标签自动创建、内容引用同步和内容引用删除能力。
- Knowledge taxonomy 已补充与 Classics 协作的兼容口径：接受 `MING_CUSTOMS` 内容类型输入和 `AI` 标签来源输入，并在仓储写入时归一化为内部口径。
- Knowledge 已补齐后端自动化测试，覆盖标签绑定协作语义和 taxonomy 兼容口径。
- `db/data-source/system.json` 与 `db/data/system.sql` 已收敛到当前阶段的 `知识治理 / 标签与同义词` 菜单结构，并通过现有脚本重新生成。
- Knowledge 已新增 `knowledge_graph_extraction_task` 任务台账，并通过 `KnowledgeGraphExtractionApplicationService` 支持 `RELATION`、`GRAPH`、`LINEAGE` 三类抽取任务创建、分页、详情和候选应用动作。
- Knowledge 已补充 `KnowledgeAiExtractionDomainService` 协作链路，经 AI 域解析稳定 `operation + capability + workerPath` 后调用 workers 三个 Knowledge usecase，并写入 `ai_call_record` 与 `ai_candidate`。
- Knowledge 图谱抽取已补齐 `batchId` 关联、批量创建、批任务取消、重生成和请求快照持久化，`knowledge_graph_extraction_task` 现可追踪 `triggerSource`、`selectionScopeJson`、`replaceUnconfirmedOnly` 与可重放请求输入。
- Knowledge 已补齐候选结果应用链路，可将 `ai_candidate.result_payload` 应用到 `knowledge_entity`、`knowledge_relation`、`knowledge_graph_version`、`knowledge_lineage_node`、`knowledge_lineage_relation`，并生成或续增图谱版本号。
- Knowledge 已补充面向 Discovery 的 taxonomy 读协作服务，`expandSynonyms`、`getTagHint` 和 `listEntityHints` 可直接为搜索与问答提供同义词、标签和实体提示。
- Classics 三类内容编辑页已接入 Knowledge 治理协作入口：标签治理、问答对治理和 AI 候选确认面板已作为页面内联能力落地，并通过 Knowledge/AI 协作回写统一标签与正式内容。
- Admin Web 已接入 `/knowledge/graph-extraction` 页面，支持三类抽取任务创建、批量范围输入、质量触发模式、任务分页、任务详情抽屉、重生成、批任务取消和候选应用动作。
- Admin Web 已接入 `/knowledge/graph-results` 页面，支持图谱版本列表、版本详情，以及从版本下钻查看正式实体、正式关系和正式世系结果。
- Knowledge 已新增世系画布聚合读取服务，Admin `POST /api/knowledge/lineage/canvas` 与 Portal `GET /api/portal/knowledge/lineage` 均读取正式 `knowledge_lineage_node`、`knowledge_lineage_relation` 和 `knowledge_graph_version`，支持版本、关键词、节点类型、关系类型、确认状态、焦点节点、焦点关系和深度过滤。
- Admin Web 已接入独立 `/knowledge/lineage` 页面与 `知识治理 / 世系图浏览` 菜单，支持版本切换、筛选、重置、刷新、画布节点/关系点击、节点表格定位、关系表格定位和详情面板联动。
- Admin Web 已在 `/knowledge/refinement` 工作台接入人工质量标注，实体、关系、世系节点和世系关系均可打开标注 Drawer，保存、查看和删除质量标注。
- Admin Web `/knowledge/refinement` 应用精修后已展示后续操作提示，支持通过按钮打开当前图谱版本、进入图谱重生成并携带精修来源参数，以及进入质量报告重算入口。
- Knowledge 精修应用接口已返回 `RefinementApplyResult` / `ApplyResponse`，包含 `graphVersionId`、来源内容、`sourceTaskId`、`selectionScopeJson`、`replaceUnconfirmedOnly=true`、`triggerSource=REFINEMENT_APPLIED`、`graphRefreshRequired=true` 和 `qualityReportRefreshRequired=true`，用于前端承接图谱联动。
- Knowledge 图谱重生成已支持 `REFINEMENT_APPLIED` 触发来源，Admin Web `/knowledge/graph-extraction` 可从精修应用结果预填任务类型、源任务、选择范围、仅替换未确认结果和触发来源，并提交重生成请求。
- Knowledge 图谱版本响应已暴露精修应用状态，Admin Web `/knowledge/graph-results` 支持按 `graphVersionId` 定位当前正式结果，并在版本表格和详情中展示是否已精修、最近精修任务和应用时间。
- Admin Web 已接入 `/knowledge/quality-report` 页面，支持输入图谱版本生成报告、查看最新报告、历史报告、问题清单、来源明细和人工标注明细。
- Knowledge 质量报告详情已标记精修后过期状态，Admin Web `/knowledge/quality-report` 支持从精修应用跳转到指定 `graphVersionId`，展示报告需重算提示，并通过生成控件重新生成该版本质量报告。
- Admin Web `/knowledge/quality-report` 已支持从质量报告来源明细按低质量门类一键触发重提取；后端会从报告快照生成 `selectionScopeJson` 并复用 `/knowledge/graph-extraction` 任务台账、批次、候选应用和正式结果落库链路。
- Knowledge 已新增质量报告快照模型和 `knowledge_quality_report`、`knowledge_quality_report_issue`、`knowledge_quality_report_source_detail` 三张表，报告生成即发布为 `PUBLISHED`。
- Portal Web `/knowledge/quality` 已改为读取最新 `PUBLISHED` 质量报告快照；无报告时返回并展示明确空态，不再展示临时计算质量指标。
- Portal Web 已接入 Knowledge 只读门户：首页 `/knowledge`、质量页 `/knowledge/quality`、图谱分层浏览页 `/knowledge/atlas` 和世系图只读浏览页 `/knowledge/lineage` 已形成入口闭环。
- Portal Web 图谱浏览已支持 `overview -> category -> detail` 三层 URL 状态、门类下钻、实体下钻和 breadcrumb 返回导航。
- Portal Web 图谱浏览已接入只读可视化画布，支持鸟瞰层固定 14 门类节点、门类层实体关系节点、详情层单实体关系节点、缩放/平移/minimap 控件，以及节点点击下钻。
- Portal Web 世系图只读浏览已支持默认最新已应用版本、URL query 恢复、筛选、筛选清除、画布节点/关系点击和只读详情面板。
- Knowledge 运行时验证已完成收口：Java Servers、Python Workers、Admin Web format/lint/build/test、Knowledge Playwright 6 个页面冒烟均通过，详见 `KNOWLEDGE-RUNTIME-SMOKE-EVIDENCE.md`。

部分完成：无。

未完成：无。


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
| 标签批量操作                                     | 已完成   | 已支持统一标签多选批量合并影响预览与执行、批量废弃确认，以及待审核标签批量通过和批量拒绝；批量通过可统一指定正式分类            | 无                                              | Knowledge, Admin Web   |
| 标签使用排行、知识库分布、来源占比、月度新增趋势 | 已完成   | 已提供完整治理统计读取和 Admin Web 展示入口                                                                                    | 无                                              | Knowledge, Admin Web   |
| 同义词新增、编辑、删除、查看和搜索               | 已完成   | 后端和 Admin Web 已支持分页、创建、更新、状态变更、删除                                                                        | 无                                              | Knowledge, Admin Web   |
| 同义词正向和反向查询                             | 已完成   | 已新增 `KnowledgeFacade.querySynonyms` 的方向查询入口，支持 FORWARD/REVERSE/BIDIRECTIONAL，保留 `expandSynonyms` 兼容。 | 无                                             | Knowledge, Discovery   |
| 搜索和问答使用同义词扩展                         | 已完成   | Knowledge 已通过 `expandSynonyms`、`getTagHint` 和 `listEntityHints` 读协作向 Discovery 搜索/问答提供增强数据；Discovery Search/QA 已消费该链路 | 无                                              | Knowledge, Discovery   |
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
| 质量评估人工标注入口       | 已完成   | 已提供 `knowledge_quality_annotation` 表、标注写入/删除/分页接口、质量汇总聚合与后端测试；Admin Web 精修工作台已支持实体、关系、世系节点和世系关系逐行打开标注 Drawer，保存、查看和删除人工标注 | 无                                                | Knowledge, Admin Web |

### 知识图谱

| 需求项                                     | 状态   | 已完成部分                                                                                                                                                    | 未完成部分                           | 责任域                            |
| ------------------------------------------ | ------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------ | --------------------------------- |
| 三才图会实体和关系 AI 提取                 | 已完成 | Knowledge 可创建 `RELATION` / `GRAPH` 抽取任务，经 AI 域调用 workers 并生成 `ai_call_record`、`ai_candidate`，再由 Knowledge 应用为正式实体和关系事实         | 无                                   | Knowledge, AI, Workers            |
| 异步提取任务和进度展示                     | 已完成 | 已提供任务台账、任务状态回填、失败原因、`aiCallId` / `aiCandidateId`、完成时间和应用时间，并在 Admin Web 提供任务页、详情抽屉与应用动作                       | 无                                   | Knowledge, AI, Admin Web          |
| 鸟瞰层、门类层和详情层图谱浏览             | 已完成 | Portal Web `/knowledge/atlas` 已支持 `overview -> category -> detail` 三层浏览、URL 状态恢复、breadcrumb、节点下钻和只读图谱画布                              | 无                                   | Knowledge, Portal Web            |
| 鸟瞰层展示 14 大门类实体数量和分布         | 已完成 | Portal 总览层已固定展示三才 14 门类卡片和图谱画布节点；无数据门类保留空位并展示实体数 0，有数据门类展示实体数、关系数、版本号和下钻入口                       | 无                                   | Knowledge, Portal Web            |
| 门类层展示卷间关联和实体关系               | 已完成 | Portal 门类层已展示版本信息、实体高亮、关系分组和来源摘要                                                                                                   | 无                                   | Knowledge, Portal Web            |
| 详情层展示单实体信息、关系和来源条目       | 已完成 | Portal 实体层已展示实体卷宗、关系分组、来源条目、时间线和相关标签                                                                                           | 无                                   | Knowledge, Portal Web            |
| 鸟瞰、门类和详情面包屑导航                 | 已完成 | Portal `/knowledge/atlas` 已使用后端 breadcrumb 驱动 overview/category/detail 返回导航                                                                      | 无                                   | Knowledge, Portal Web            |
| 批量生成和重生成                           | 已完成 | 已支持按 `selectionScopeJson` 批量创建同类型图谱抽取任务、复用 AI 域 `batchId` 聚合子任务，并支持基于源任务重生成且区分 `replaceUnconfirmedOnly` 语义           | 无                                   | Knowledge, AI                     |
| 从质量报告或筛选结果批量触发提取           | 已完成 | Admin Web 图谱抽取页已支持 `QUALITY_REPORT` 触发模式并可携带批量范围快照创建任务；质量报告来源明细已支持低质量门类一键触发，后端生成 `selectionScopeJson` 并保留 `triggerSource`、批次和请求快照 | 无 | Knowledge, AI                     |
| 读取数据精修修正结果                       | 已完成 | 精修工作台应用动作会将实体、关系和世系草稿写回当前 `graphVersionId` 的正式事实表；应用接口返回图谱联动结果，正式结果页可按版本定位并展示最近精修状态 | 无                                   | Knowledge, Admin Web              |
| 质量报告与指标展示                         | 已完成 | 已提供质量报告快照生成服务、后台 `/knowledge/quality-report` 页面、历史报告列表、问题清单、来源明细、人工标注明细、精修后过期提示和指定版本重算入口，以及 Portal `/knowledge/quality` 同源快照读取 | 无                                   | Knowledge, Admin Web, Portal Web |
| 质量报告按门类分组并支持低质量门类触发提取 | 已完成 | 质量报告来源明细已保留 `sourceCategoryCode`、`sourceCategoryName`、标注数、问题数和跳转链接；低质量门类可在质量报告页一键创建 `QUALITY_REPORT` 图谱抽取任务，并在任务台账查看 `selectionScopeJson` 与批次信息 | 无 | Knowledge, AI                     |
| 最近提取版本和提取时间展示                 | 已完成 | 已在 `knowledge_graph_version` 建立版本台账，应用正式结果时会生成或续增版本号，并在 Admin Web 正式结果页提供版本列表、版本详情和应用时间展示                  | 无                                   | Knowledge, Admin Web              |
| 世系图专用提取和展示                       | 已完成 | 已支持 `LINEAGE` 抽取任务、workers 世系候选输出、正式 `knowledge_lineage_node` / `knowledge_lineage_relation` 落库、Admin Web `/knowledge/lineage` 独立画布、节点/关系列表和详情联动，以及 Portal Web `/knowledge/lineage` 只读画布入口 | 无                                   | Knowledge, AI, Workers, Admin Web, Portal Web |

### 运行时验证

| 需求项             | 状态     | 已完成部分                                                                                                                                                                   | 未完成部分                           | 责任域                            |
| ------------------ | -------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------ | --------------------------------- |
| 当前阶段运行时验证 | 已完成 | 已通过 `mvn -pl biz/knowledge,biz/ai -am spotless:check checkstyle:check test`、Workers `ruff format --check` / `ruff check` / `pytest`、Admin Web `format:check` / `lint` / `build` / `test`，以及 Knowledge Playwright 6 个页面冒烟；验证证据记录在 `KNOWLEDGE-RUNTIME-SMOKE-EVIDENCE.md`。 | 无 | Knowledge, AI, Workers, Admin Web |

## Unfinished Focus

| 能力项                              | 状态   | 说明                                                                                                   |
| ----------------------------------- | ------ | ------------------------------------------------------------------------------------------------------ |
| 数据精修                            | 已完成 | 实体、关系和世系精修工作台已落地，正式事实回写、质量汇总和人工质量标注入口均已接通                  |
| 图谱浏览与质量报告                  | 已完成 | 当前已交付 Portal 图谱分层浏览页、Portal 图谱可视化画布、固定 14 门类空位、Portal 质量报告页、后台质量报告页、抽取任务、批量生成、重生成、候选应用、正式结果落库、后台读取页，以及从质量报告低质量门类一键触发重提取 |
| 世系图浏览                          | 已完成 | 当前已交付 Admin `/knowledge/lineage` 独立世系画布、节点/关系列表、节点/关系详情联动、菜单入口、Portal `/knowledge/lineage` 只读入口，以及正式世系事实聚合读取 API |
| Portal 页面                         | 已完成 | Portal 侧已形成 `/knowledge`、`/knowledge/quality`、`/knowledge/atlas`、`/knowledge/lineage` 四个可执行只读入口 |
| 数据精修与图谱联动                  | 已完成 | 精修应用后已返回图谱联动结果，Admin Web 已引导打开当前版本、携带精修参数发起图谱重生成，并提示指定版本质量报告重算；图谱版本和质量报告响应已暴露精修状态与过期状态 |
| Discovery 搜索或问答接入            | 已完成 | taxonomy 治理、同义词扩展、标签提示和实体提示已被 Discovery 搜索 / 问答消费，形成最小闭环             |
| Classics 内容编辑页内联知识治理入口 | 部分完成 | Wangqi、Sancai、MingCustoms 编辑页已内联标签治理、问答对治理和 AI 候选确认入口                           | 标签分类、同义词、审核与合并等完整 taxonomy 治理仍只在独立页面提供 |

## Residual Risks

- 菜单种子重生成后 `system_menu` 的树编号和自增值已随节点数收缩变化，后续若依赖固定菜单 ID，需要以当前生成结果为准重新校对。
- taxonomy 页面、图谱抽取、正式结果、世系图、精修工作台和质量报告已补齐 Knowledge Playwright 冒烟；后续再改权限、字段或接口返回时，建议同步更新对应前端契约测试与 Playwright 断言。
- 同义词、标签和实体提示已接通 Discovery 搜索 / 问答主链路，后续主要关注命中质量与提示规则调优。
- 当前已补齐 Portal 只读入口、图谱分层浏览、图谱可视化画布、固定 14 门类空位、独立世系图只读画布、图谱抽取批量闭环、人工质量报告闭环和低质量门类一键触发重提取；后续扩展到更多触发来源或世系专用重提取前，仍需先明确触发边界与回放策略。
