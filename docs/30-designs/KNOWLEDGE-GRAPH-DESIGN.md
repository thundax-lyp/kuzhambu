# Knowledge Graph Design

## Purpose

本文档定义三才图会知识图谱的实现设计。产品需求以 [`KNOWLEDGE-GRAPH-REQUIREMENTS.md`](../10-requirements/KNOWLEDGE-GRAPH-REQUIREMENTS.md) 为准；本文将其落实为 Knowledge 域的模型、持久化、应用服务、接口和前端读取设计。

图谱展示与探索是核心。素材草稿图负责抽取与编辑，发布空间负责统一展示与治理；两者通过发布映射关联，不进行双向同步。

## Scope and Non-goals

- 只处理三才图会素材。
- 覆盖工作台、图谱治理、素材管理、提取任务、整体发布/撤回、删除任务、JSON 导入导出和质量待办。
- 不实现世系图。
- 不实现 Schema 后台管理或对外暴露 Schema。
- 不实现发布空间全量导入导出、素材版本管理、正文证据片段定位或发布空间向素材空间自动回写。

## Module Ownership

代码归属 `kuzhambu-servers/biz/knowledge/`。Knowledge 是图谱表的唯一写入方；Classics 仍拥有素材正文、生命周期与访问控制主事实。

```text
Classics material
  └─ content snapshot and lifecycle collaboration
       ↓
Knowledge graph
  ├─ material graph (draft)
  ├─ publish mapping
  └─ published graph (read and governance)
       ↓
Admin workbench / material workspace / portal material view
```

Knowledge 发起图谱抽取必须通过 AI 域 application 协作语义，不得直接调用 Python worker。AI worker 只执行能力，不解析 Knowledge 的业务发布协议。

### Cross-domain Read Composition

素材管理、任务详情和其他图谱页面的 HTTP 接口均归 Knowledge。前端只请求 Knowledge，不得自行调用 Classics 接口后拼装图谱页面。

素材列表由 Knowledge application 按以下顺序组装：先校验 `knowledge:graph:view`，再携带当前主体和筛选条件调用 Classics facade 分页取得可见且可图谱化的稿件，最后按当前页 `ContentRef` 批量读取图谱素材、统计快照和任务摘要。Classics 对内容可见性和生命周期拥有最终决定权；Knowledge 不缓存完整正文或绕过该校验。素材详情、抽取和发布前也必须经 Classics facade 校验来源内容仍可用，并在抽取时冻结内容快照。

跨域 facade 仅返回页面所需的标题、来源类型、卷目/分类、可见性和内容快照等字段。Classics 删除或不可用事件通过异步协作触发 Knowledge 的素材删除/禁用流程；不得跨域访问对端表或由前端执行补偿调用。

## Schema as Code

图谱 Schema 是受控代码资产，建议归属 `knowledge-domain` 的 `graph/schema` 包，并为每个节点类型、关系类型定义：

- 节点类型、字段、字段类型、必填、多值、首选值和身份字段。
- 关系类型、建议端点组合、可选限定字段、并存/互斥约束和身份字段。
- 节点 `nodeKey`、边 `edgeKey` 的规范化与计算规则。
- 核心节点及其必须关系，用于质量待办。

接口和前端只接收 Schema 驱动的业务字段与校验结果。不得下发 `nodeKey`、`edgeKey` 原文、约束表达式或可编辑 Schema。

Schema 变更必须与代码评审、数据库迁移和现有数据迁移一同交付；变更后重新计算受影响 Key，并将命中冲突转为治理待办，禁止静默合并。

### Schema Contract

第一版完整 Schema 以 [`KNOWLEDGE-GRAPH-SCHEMA.json`](../20-interfaces/KNOWLEDGE-GRAPH-SCHEMA.json) 为唯一机器可读规范。该文件是 JSON Schema Draft 2020-12 文档：标准关键字只校验可由通用 validator 直接执行的宽松图文档形状、基础必填项和枚举；`x-kuzhambu-validation` 与类型/关系目录将 Key、端点兼容、引用、唯一性、无环和质量规则标注为后端分层处理规则。

后端必须将该 JSON 加载为只读 Schema。AI 结构化输出、素材 JSON 导入和草稿写入只执行宽松结构校验；发布预览和提交额外执行发布一致性硬校验；类型细分属性、端点兼容、关系限定、环和核心关系输出为告警或质量待办。前端不得维护一份独立类型或关系枚举，只消费后端按此 Schema 提供的业务表单与校验结果。

该 Schema 不作为后台配置能力，也不直接下发完整定义或有效 Key 原文。变更 Schema 必须同步更新 JSON、后端实现、测试、数据迁移和 AI 输出契约。

## Domain Model

### Aggregates

| Aggregate | Responsibility |
| --- | --- |
| `MaterialGraph` | 一份三才图会素材的一张草稿图、锁定状态及素材侧节点/边编辑。 |
| `PublishedGraph` | 统一发布节点、边、属性值、人工治理、合并、拆分和删除。 |
| `GraphPublication` | 单素材整体发布的预览、确认、映射和审计；批量发布只是多个独立 publication 的前端聚合。 |
| `GraphExtractionTask` | 素材内容快照下的异步抽取、候选处置、原地重试、关联任务和到期清理。 |
| `MaterialDeletionTask` | 素材删除决策、异步删除和可重试执行。 |

`MaterialGraph` 以 `ContentRef` 为归属，而不是裸 `materialId`。`ContentRef` 为 `(contentType, refId)` 值对象；首期仅接受 `SANCAI_ENTRY`。图谱域定义自己的 `ContentRef` 值对象，与 taxonomy 的内容引用保持相同语义但不跨子域复用实体模型。

`MaterialGraph` 只能在 `DRAFT` 状态修改。已发布素材必须经整体撤回转回 `DRAFT`；发布空间治理永不反写草稿图。

### State

```text
MaterialGraph: DRAFT → PUBLISHING → PUBLISHED → WITHDRAWING → DRAFT
                         │                         │
                         └──────── FAILED ─────────┘

Deletion: PRECHECKED → AWAITING_DECISION → PENDING → RUNNING → SUCCEEDED / FAILED
```

同一素材在 `PUBLISHING`、`WITHDRAWING`、`PENDING` 或 `RUNNING` 状态不得发起抽取、编辑、发布、撤回或第二个删除任务。

## Persistence Model

所有表使用 `knowledge_graph_` 前缀；主键遵从服务器统一 ID 设计。`version` 用于乐观锁，时间、操作者和审计字段遵从项目基础字段约定。

### Material Space

| Table | Key fields and responsibility |
| --- | --- |
| `knowledge_graph_material` | 技术主键 `id`；业务 `content_type + content_ref_id` 唯一；`status`、`published_at`、`current_extraction_task_id`、`lock_version`。只保存 `ContentRef` 与图谱状态，不复制正文；`current_extraction_task_id` 只指向唯一活动任务。 |
| `knowledge_graph_material_node` | `material_id`、`node_key`、`node_type`、`name`、`properties_json`；同素材图内 `(material_id, node_key)` 唯一。未能计算 Key 的草稿对象允许 `node_key` 为空。 |
| `knowledge_graph_material_edge` | `material_id`、两端素材节点、`relation_type`、`qualifiers_json`、`edge_key`；同素材图内 `(material_id, edge_key)` 唯一。未能计算 Key 的草稿关系允许 `edge_key` 为空。 |
| `knowledge_graph_material_stats` | `material_id` 唯一；草稿节点/关系数、发布贡献节点/关系数、活动/待审核/失败任务数、`stats_revision`、`calculated_at`。它是素材库列表和筛选的读模型，不替代节点、关系和任务明细。 |
| `knowledge_graph_extraction_task` | `material_id`、`ContentRef`、内容/模型/提示词/Schema 快照、管道版本、`execution_status`、`disposition`、当前阶段、进度、尝试次数、候选/调用引用、结果摘要、失败原因、处置和清理时间、关联任务引用。任务不保存候选载荷。 |
| `knowledge_graph_extraction_stage` | `extraction_task_id`、阶段顺序、运行状态、输入/输出摘要、AI 调用、失败原因和起止时间。 |

`properties_json` 与 `qualifiers_json` 是开放多值 JSON 载体：草稿写入只校验其为对象，细分属性和值域作为告警而非拒绝条件；它们不替代可查询的 Key、类型和关系字段。

`execution_status` 固定为 `PENDING`、`RUNNING`、`SUCCEEDED`、`FAILED`、`CANCELLED`；`disposition` 在成功后固定为 `PENDING`、`ADOPTED_MERGE`、`ADOPTED_REPLACE`、`DISCARDED`、`SUPERSEDED`。运行状态和采纳状态不得复用同一字段。`FAILED -> PENDING` 是同一任务的原地重试，递增 `attempt_no` 并保留尝试历史；重新抽取创建新任务，通过 `regenerated_from_task_id` 关联来源。`batch_id`、`regenerated_from_task_id`、`superseded_by_task_id`、`triggered_by_task_id` 用于任务详情的关联任务读取。

### Published Space

| Table | Key fields and responsibility |
| --- | --- |
| `knowledge_graph_published_node` | `node_key` 全局唯一、`node_type`、展示名称、`version`、归档/删除状态。 |
| `knowledge_graph_published_edge` | `edge_key` 全局唯一、两端发布节点、`relation_type`、`qualifiers_json`、`version`。 |
| `knowledge_graph_published_node_property` | 发布节点的多值属性：字段、规范化值、展示值、`is_preferred`、来源类型、素材引用或人工操作引用。 |
| `knowledge_graph_published_edge_property` | 发布边的多值限定或展示属性，字段与来源语义同节点属性。 |
| `knowledge_graph_manual_source` | 人工创建或补充的原因、操作者和目标发布节点/边；来源类型为 `MANUAL`。 |

同一字段允许多个值时，由 Schema 决定是否可并存、是否必须且只能有一个 `is_preferred`。身份字段或互斥字段不得通过属性表绕过发布冲突。

### Mapping, Operations and Deletion

| Table | Key fields and responsibility |
| --- | --- |
| `knowledge_graph_material_node_mapping` | 素材尚存在时以 `material_node_id` 关联 `published_node_id`；保留贡献后改以不可变 `source_snapshot_json`（含 ContentRef、节点 Key、类型、名称和属性快照）追溯，原素材节点引用置空；状态为 `ACTIVE`、`WITHDRAWN`、`SOURCE_DELETED_PRESERVED`。 |
| `knowledge_graph_material_edge_mapping` | 素材尚存在时以 `material_edge_id` 关联 `published_edge_id`；保留贡献后存不可变边快照（含 ContentRef、边 Key、端点发布 Key、关系和限定字段）并置空原素材边引用；状态语义同节点映射。 |
| `knowledge_graph_publish_record` | 单素材发布记录、发布预览摘要、冲突决策、创建/关联/复用结果和端点补齐结果。 |
| `knowledge_graph_governance_operation` | 发布空间 CRUD、合并、拆分、删除的前后快照、理由、操作者和恢复信息。 |
| `knowledge_graph_material_deletion_change` | 删除前快照、用户选择 `PRESERVE_CONTRIBUTION` 或 `WITHDRAW_ASSOCIATIONS`、状态和结果摘要。 |
| `knowledge_graph_material_deletion_task` | 关联变更项、任务状态、进度、失败原因、幂等键和执行结果。 |

映射是溯源和撤回边界，不是同步机制。撤回仅将本素材的 `ACTIVE` 映射变为 `WITHDRAWN`；素材删除且选择保留贡献时，先写入不可变来源快照、再置空对草稿节点/边的引用并将映射变为 `SOURCE_DELETED_PRESERVED`，从而保留历史来源而不再依赖已删除草稿图。

## Core Workflows

### Extraction and Draft Merge

1. 素材管理发起抽取；应用层校验 `knowledge:graph:edit`、素材属于三才图会、当前为 `DRAFT` 且没有 `PENDING` 或 `RUNNING` 任务。
2. 通过 Classics facade 读取并固定内容快照，以及模型、提示词、变量和输出 Schema 快照；经 AI 域创建异步调用，写入 `GraphExtractionTask`，并将素材的 `current_extraction_task_id` 指向该任务。
3. 回调或轮询成功后，应用层先执行宽松结构校验并生成 AI 域候选。任务转为 `SUCCEEDED + disposition:PENDING`，清空素材的活动任务指针；候选未处置前不得改变草稿图。
4. 用户选择 `MERGE` 或 `REPLACE` 时，以素材 `lock_version` 校验当前草稿未被并发修改，再把候选写入草稿图并记录处置结果；选择丢弃则只记录 `DISCARDED`。被后续候选替代的旧候选标记 `SUPERSEDED`。
5. 失败任务保留冻结输入并可原地重试：递增尝试次数、清空本次运行错误、重置到 `PENDING`；已发布素材必须先撤回，不能绕过冻结直接抽取。正文或运行配置变化时，显式重新抽取创建新任务。

### Task Cleanup

处置终态 `ADOPTED_MERGE`、`ADOPTED_REPLACE`、`DISCARDED` 和 `SUPERSEDED` 写入 `disposed_at` 与 `purge_after = disposed_at + 7 days`。定时任务仅清理到期的任务和阶段明细；草稿图、发布映射、发布记录、素材统计和最近处置摘要保留。AI 候选、调用和批任务由 AI 域拥有，Knowledge 只能通过 facade 发起同一保留策略下的清理协作，禁止直接删除 AI 表。

### Publication

发布预览是只读计划：对素材全部节点和边计算 Key、执行发布一致性硬校验、匹配发布对象，并同时返回不阻断的质量告警，生成 `CREATE`、`REUSE`、`CONFLICT` 结果。

确认发布时，在事务内重新执行匹配：

1. 以 `node_key` 获取或创建发布节点，并写入节点映射。
2. 用映射后的发布端点与关系限定字段计算 `edge_key`，获取或创建发布边，并写入边映射。
3. 合并可并存属性；Key 不唯一、对象标识重复、端点无效或必须人工决定的身份冲突中止该素材发布。端点兼容、细分属性、限定字段、环和质量问题保留为告警或治理待办。
4. 写入发布记录并将素材状态置为 `PUBLISHED`。

发布节点、边、映射和状态更新同事务提交。数据库唯一约束防止并发重复；若预览时命中的对象 `version` 已改变，则返回 `PREVIEW_STALE`，不写入并要求刷新预览。

批量发布接口只协调多份素材的独立预览与提交结果，不创建批次聚合实体，也不提供跨素材回滚。

### Withdrawal and Deletion

整体撤回以素材为边界，在事务内把该素材所有有效节点/边映射置为 `WITHDRAWN`，解除素材锁定并回到 `DRAFT`。它不删除发布对象、不更改其他映射、不删除人工维护内容。

素材删除先创建 `MaterialDeletionChange` 和发布快照，用户选择后再创建异步任务：

- `PRESERVE_CONTRIBUTION`：先将来源节点/边写入映射快照、置空原素材对象引用，再将映射改为 `SOURCE_DELETED_PRESERVED`，删除草稿图和素材图谱引用。
- `WITHDRAW_ASSOCIATIONS`：映射改为 `WITHDRAWN`，删除草稿图和素材图谱引用。

任务按 `ContentRef` 与决策建立幂等键。执行前再次读取任务和素材状态；重复投递或重试不得重复修改映射或删除已经删除的草稿记录。

### Published-space Governance

- 人工节点/边先通过 Schema 校验，再写发布对象和 `MANUAL` 来源。
- 合并选择保留节点，将全部映射、边端点、可并存属性和人工来源迁移到保留节点；若迁移后产生重复边，按 `edge_key` 合并。
- 拆分创建新节点，逐项分配属性、边和素材映射；所有受影响对象必须分配完毕才能提交。
- 删除发布节点前必须显式删除关联边或解除依赖；历史映射和治理操作保留，目标状态标记已删除而非物理抹除审计数据。

## Query and Rendering

工作台默认查询最近成功发布的 100 个发布节点，不加载全图。前端分批请求这些种子节点的关联边；每批边返回即渲染边、补齐另一端节点并更新连接度。查询结束后移除连接度为零的节点并稳定布局。

最终画布最多 200 个节点；边随已渲染节点集合返回，不另设边数上限。超过上限时，后端以发布时间倒序、再以关系数和稳定 ID 截断，返回 `truncated=true` 与继续展开游标。种子节点在命中首条边前使用骨架或淡化状态，避免孤立节点闪烁。

鸟瞰层返回门类统计和质量指标；门类层按门类/类型过滤局部图；详情层返回节点或边的属性、来源素材、映射、人工来源和治理记录。前台只按稿件读取其素材图，不读取发布空间工作台数据；仅当素材处于 `PUBLISHED` 且稿件本身通过既有内容可见性校验时，才返回该稿件当前有效映射对应的图谱内容，草稿、撤回和已删除素材返回空状态。

质量查询仅计算并暴露：孤立节点数、核心节点关系缺失数及对应对象列表。核心节点规则由 Schema 提供：`coreRelationPolicy.mode = ANY_INCIDENT_RELATION` 表示节点只要存在一条入边或出边，其关系类型属于 `relationTypes`，即不计为关系缺失。

## Interface Design

后台接口位于 Knowledge admin entry subdomain。下列为后台资源边界，具体 request/response DTO 在 `docs/20-interfaces/` 固化：

| Resource | Main operations |
| --- | --- |
| `/knowledge/graph/workbench` | 概览统计、最近发布种子、关联边分页、门类图、搜索、对象详情、质量待办。 |
| `/knowledge/graph/material` | Classics 可见稿件分页后的素材目录、素材 `SegmentedDrawer`、草稿节点/边 CRUD、JSON 导入导出和批量动作。 |
| `/knowledge/graph/task` | 单份、整卷和批量抽取任务的创建、处理队列、按素材分组、详情、取消、原地重试、重新抽取、候选预览与处置。 |
| `/knowledge/graph/publication` | 预览、确认发布、撤回和多素材独立批量发布。 |
| `/knowledge/graph/published` | 发布节点/边 CRUD、来源、治理记录、合并、拆分、删除。 |
| `/knowledge/graph/deletion-changes` | 删除前快照、用户决策和结果查询。 |
| `/knowledge/graph/deletion-tasks` | 删除任务创建、进度、失败原因和重试。 |

门户入口另提供只读资源 `/portal/knowledge/graph/materials/{contentType}/{contentRefId}`：先执行稿件既有内容可见性校验，再按素材状态和有效映射返回该稿件图谱；该资源不暴露发布空间搜索、治理、人工来源或跨素材关系。

JSON 导入只允许写入未发布素材草稿图。先执行格式和 Schema 校验，返回对象级错误；确认后按照用户选择的 `MERGE` 或 `REPLACE` 执行。下载仅导出当前单素材草稿节点、边、属性、限定字段和 Schema 版本，不导出发布空间、映射、审计或人工治理数据。

## Frontend Design

Admin 使用现有 `knowledge:graph:view` 与 `knowledge:graph:edit`：

- 工作台、图谱治理、素材管理、提取任务，以及素材管理内删除任务和删除变更读取使用 `view`。
- 草稿 CRUD、抽取、JSON 上传下载、发布、撤回、删除任务、发布空间治理、合并与拆分使用 `edit`。

菜单固定为“知识治理 / 知识图谱 / 工作台、图谱治理、素材管理、提取任务”。素材管理包含素材列表、单素材草稿图、删除任务和删除变更入口；删除任务、删除变更不再作为菜单层级。提取任务集中承载单份、整卷和批量抽取的运行状态、候选预览、应用、取消和重生成；候选应用后回到对应素材草稿图。高风险动作必须展示影响预览、二次确认和操作结果。

工作台只读取正式发布空间，用于概览、搜索、局部图浏览、来源追溯与质量待办分流，不提供直接治理写操作。图谱治理只处理发布空间的跨素材节点、关系和人工来源变更；合并、拆分、删除必须先显示影响对象、关系、映射和待处理事项，再允许确认应用。素材管理只处理来源素材、草稿、整体发布/撤回及删除生命周期；提取任务不直接编辑草稿，也不执行发布空间治理。

发布预览在素材画布内完成：绿色新建、橙色关联、红色冲突、蓝色已发布。红色对象点击后在右侧抽屉展示素材对象、候选发布对象、关键差异和动作；存在未决冲突时禁止确认。

## Migration and Compatibility

现有 `graph-extraction`、候选应用、图谱版本、正式实体/关系、精修草稿、世系和旧质量报告接口与本设计不兼容。实施采用独立迁移阶段：

1. 新建上述图谱表、Schema 代码和新接口，不复用旧表的状态语义。
2. 按已确认的迁移规则导入仍需保留的三才图会数据；未定义迁移规则的数据不得自动映射为已发布知识。
3. 前端切换到新菜单与接口后，删除旧图谱入口、API、测试和旧表写入路径。
4. 确认没有调用方后，再按数据库治理规则删除旧表或归档其只读历史。

迁移规则、数据核对清单和切换顺序属于独立 RUNBOOK，不在本设计中假定。

## Verification

- 单元测试：Schema Key 与约束、草稿合并、发布匹配、冲突、乐观锁、属性首选、合并、拆分、撤回和删除决策。
- 集成测试：Classics 可见稿件分页与 Knowledge 批量补齐、发布事务原子性、单素材活动任务互斥、任务原地重试、候选处置、7 天清理、AI 域快照协作、删除任务幂等重试、素材删除后映射状态、权限拒绝。
- 前端 E2E：素材列表统计快照、素材/任务 `SegmentedDrawer`、任务队列和按素材分组、抽取到草稿、候选合并/覆盖/丢弃、发布预览冲突、冻结与撤回、发布治理、删除列表决策、最近发布渐进渲染、200 节点截断与 JSON 导入校验。
- 数据迁移验证：迁移前后素材数、节点/边数、映射数和人工治理操作抽样核对；旧入口移除后确认不存在旧模型写入。

## Related Documents

- [`KNOWLEDGE-GRAPH-REQUIREMENTS.md`](../10-requirements/KNOWLEDGE-GRAPH-REQUIREMENTS.md)：正式产品需求。
- [`KNOWLEDGE-GRAPH-SCHEMA.json`](../20-interfaces/KNOWLEDGE-GRAPH-SCHEMA.json)：后端处理、AI 输出、JSON 导入与前端表单共用的机器可读 Schema 规范。
- [`KNOWLEDGE-REQUIREMENTS.md`](../10-requirements/KNOWLEDGE-REQUIREMENTS.md)：Knowledge 域总览与标签治理。
- [`WORKERS-AI-INTERFACE.md`](../20-interfaces/WORKERS-AI-INTERFACE.md)：AI 域与 worker 的调用边界。
- [`SERVERS-DATABASE-RULES.md`](../00-governance/SERVERS-DATABASE-RULES.md)：数据库事实来源与迁移规则。
