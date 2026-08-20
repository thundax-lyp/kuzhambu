# Knowledge Implementation Coverage

## Status

- 当前状态：标签治理已完成；双空间图谱的 Admin 主链路、Portal 三才图会总谱预览和稿件详情图谱视图已实现。Portal Atlas 已完成本地真实数据运行时验收；Portal 稿件图与 Admin 全链路真实运行时验收仍待补齐。
- 覆盖范围：统一标签治理、Classics / Discovery 读协作，以及三才图会的图谱工作台、发布空间治理、素材草稿、抽取任务、发布撤回和删除生命周期。
- 真相源：[`KNOWLEDGE-REQUIREMENTS.md`](../10-requirements/KNOWLEDGE-REQUIREMENTS.md)、[`KNOWLEDGE-GRAPH-REQUIREMENTS.md`](../10-requirements/KNOWLEDGE-GRAPH-REQUIREMENTS.md)、当前代码和本文件。

## Code-Calibrated Summary

- Taxonomy 已覆盖分类、标签、待审核标签、别名、合并、废弃、批量审核和治理统计；Classics 通过 Knowledge 协作标签引用，Discovery 消费标签和实体提示。
- 图谱已改为“素材草稿空间 — 整体发布映射 — 发布空间”的模型。`GraphController` 以 `/api/knowledge/graph` 提供工作台、素材、任务、发布/撤回、删除变更/任务和发布对象治理接口，全部使用 `knowledge:graph:view` 或 `knowledge:graph:edit`。
- Admin seed 仅显示工作台、图谱治理、素材管理和提取任务；删除变更和删除任务为直达页面，不是独立菜单。
- Admin 工作台已接通快照概览、最近关系、一跳关系的渐进只读画布和活动时间线。它目前没有页面级搜索、质量待办、门类层导航或对象详情分流。
- 图谱治理已接通发布节点/边分页、详情、创建/编辑、删除影响预览和确认，以及节点合并；后端仍有节点拆分接口，但当前 Admin 页面不把它作为已交付流程。
- 素材管理已由 Knowledge 服务组合 Classics 可见稿件和图谱统计，并提供素材详情、草稿节点/边编辑、候选采用、发布/撤回和批量操作的接口。提取任务页只承担跨素材任务查询和失败重试；候选处置留在素材详情。
- Portal `/knowledge/atlas` 已替换为三才图会总谱预览：调用 `POST /api/portal/knowledge/graph/atlas/overview/get`、`recent-edges/list` 与 `one-hop-edges/list`，显示正式节点、关系和覆盖素材统计。首帧完成后每 1.2 秒按节点在既有关系数据中的总边数降序选择当前可见的核心节点渐进展开，每步最多追加 6 条关系，自动阶段最多保留 100 个节点；最后一批对受影响簇执行 180 轮局部力导向计算，节点双击仍可追加一跳关系。簇归属由增量并查集维护，常规新增边仅对所属或合并后的簇执行 48 轮局部力导向松弛，其他簇位置保持不变；新节点从其既有邻接节点发射，旧节点随局部斥力向外过渡，已展开节点以加粗边框区分。
- 三才图会稿件详情已提供“阅读 / 知识图谱”双视图；首次打开图谱时调用 `POST /api/portal/knowledge/graph/material/get`，展示当前稿件已发布对象、关系、搜索高亮和完整关系三元组。稿件图与 Atlas 共用 `KnowledgeGraphCanvas`，但稿件详情仍独立承担加载、失败、空状态和对象关系面板。
- `graph-result`、`refinement` 等旧 Admin 路由仍在代码中，但不在当前图谱菜单 seed 内；它们不能与新双空间图谱的完成状态混写。

## Open Items and Validation Gaps

- 对真实 Java、MySQL、Redis、AI 和 Classics facade 的 Admin 浏览器 smoke 尚未补齐；当前图谱证据主要是单元、契约、mock browser E2E 和定向 Maven 验证。
- Portal 稿件图已使用临时只读演示数据完成浏览器交互验证，但尚未连接真实 Portal API 验证已发布稿件，以及未发布、撤回、删除和不可见素材的空状态；演示数据不构成真实后端运行时证据。
- 工作台的搜索、质量待办、门类/详情导航未接入当前页面；不要以服务端端点存在代替用户流程验收。
- 图谱删除、JSON 导入导出、取消/重新抽取、节点拆分及全量发布冲突处理需要逐项以实际页面入口和真实运行时结果复核。
- 历史 Knowledge 质量报告、世系、旧图谱结果和精修页面不属于当前双空间图谱模型；如仍需保留，应另行说明兼容边界和下线计划。

## Validation Evidence

| Date | Evidence | Result and boundary |
| --- | --- | --- |
| 2026-08-19 | `mvn -pl starter/kuzhambu-admin-starter -am test` | Passed; 覆盖受影响图谱、AI、Operations 和 starter 依赖闭包。 |
| 2026-08-19 | Admin Web `format:check`、`lint`、`test` | Passed; Node 26，125 files / 479 tests。 |
| 2026-08-19 | Portal Web `format:check`、`lint`、`test` | Passed; Node 26，21 files / 57 tests；不包含新图谱 Portal 接入。 |
| 2026-08-19 | `admin-web/e2e/knowledge/graph/graph.spec.ts` | Passed; 验证工作台概览、只读画布、活动时间线和 `/knowledge/graph/**` 网络边界；使用 fixture，不证明真实后端运行时。 |
| 2026-08-20 | `mvn -pl starter/kuzhambu-portal-starter -am -DskipTests install` | Passed; 编译 Portal starter 及新增公开总谱接口。 |
| 2026-08-20 | Portal Web `format`、`lint`、`build`、`test` | Passed; 54 tests。 |
| 2026-08-20 | 本地 Portal Atlas 浏览器验收 | Passed; `http://localhost:5174/knowledge/atlas` 通过运行中 Portal API 显示 44 个节点、40 条真实关系及统计数据。 |
| 2026-08-20 | Portal 稿件图共享画布与详情视图验证 | Passed; `format:check`、`lint`、`build` 和 20 files / 56 tests；浏览器以临时只读演示数据验证稿件图懒加载、节点选择和“来源对象—关系—目标对象”三元组，未验证真实 Portal API。 |
| 2026-08-20 | Portal Atlas 增量簇与自动展开验证 | Passed; `format:check`、`lint`、`build` 和 21 files / 58 tests；真实 Portal API 浏览器首帧显示 44 个节点，单步自动展开后显示 46 个节点和 1 个加粗边框节点，无控制台错误。 |

工作台专项证据见 [`KNOWLEDGE-GRAPH-WORKBENCH-EVIDENCE.md`](./KNOWLEDGE-GRAPH-WORKBENCH-EVIDENCE.md)。旧版 2026-07-09 的“图谱版本/世系/精修已完成”验证不再用于判断当前双空间图谱。

## Requirement Coverage Matrix

| 子域 | 当前状态 | 代码证据与边界 |
| --- | --- | --- |
| Taxonomy | 已完成 | 分类、审核、别名、合并、废弃、统计及跨域标签协作保持现有实现。 |
| 图谱权限与菜单 | 已完成 | `system.json`、Admin 路由和 `GraphController` 均使用统一 `knowledge:graph:view/edit`。 |
| 工作台概览与局部图 | 部分完成 | 概览、最近关系、渐进画布和时间线已接通；搜索、质量待办、门类/详情导航待完成。 |
| 发布空间治理 | 部分完成 | 节点/边 CRUD、详情、来源/操作记录、删除预览和节点合并已接通；节点拆分未形成当前 Admin 交付流。 |
| 素材草稿与抽取 | 部分完成 | 素材分页、草稿节点/边编辑、任务创建/查询/重试、候选处置和状态约束已有代码；需真实运行时覆盖。 |
| 整体发布与撤回 | 部分完成 | 单份和批量预览/确认接口与素材页服务层存在；需逐项验证冲突、冻结和撤回的真实事务语义。 |
| 删除生命周期 | 部分完成 | 删除预检、变更决策、任务查询和重试接口及直达页面存在；需真实运行时验证保留贡献与撤回关联。 |
| JSON 导入导出 | 后端已实现，前端验收缺失 | 接口存在；尚未确认当前 Admin 页面提供完整入口和运行时校验。 |
| Portal Atlas 总谱预览 | 已完成，本地运行时已验收 | Portal 调用 Atlas overview、recent edges 和 one-hop edges 公开接口；通过共享 `KnowledgeGraphCanvas` 展示总谱，复用最近关系数据定时展开，通过增量并查集维护簇合并，并保留节点双击一跳展开。 |
| Portal 稿件图 | 前端已接入，真实运行时待验收 | 三才图会详情懒加载 `GraphPortalController` 的按稿件可见性接口，通过共享画布展示当前稿件已发布图谱，并提供搜索、对象关系三元组及加载、失败和空状态；演示数据浏览器流程已通过，真实发布和不可见空状态待验收。 |
| 旧图谱页面迁移 | 未完成 | `graph-result`、`refinement` 及旧 Portal 知识展示仍在代码中，需单独迁移或明确保留。 |
