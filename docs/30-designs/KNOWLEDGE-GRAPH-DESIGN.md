# Knowledge Graph Design

## Purpose

本文档定义三才图会知识图谱的实现设计。产品需求以 [`KNOWLEDGE-GRAPH-REQUIREMENTS.md`](../10-requirements/KNOWLEDGE-GRAPH-REQUIREMENTS.md) 为准；本文将其落实为 Knowledge 域的模型、持久化、应用服务、接口和前端读取设计。

图谱展示与探索是核心。`ContentRef` 表示一份素材；素材产生一组草稿 `GraphMaterialNode` 和 `GraphMaterialEdge`。发布库是另一套独立、可变的 `GraphPublishedNode` 和 `GraphPublishedEdge`；发布动作将素材图复制、按有效 Key 合并到发布库，并灌注两者的当前关联，不进行双向同步。

## Scope and Non-goals

- 只处理三才图会素材。
- 覆盖工作台、整体治理、素材空间、整体发布/撤回、抽取、删除任务、JSON 导入导出和质量待办。
- 不实现世系图。
- 不实现 Schema 后台管理或对外暴露 Schema。
- 不实现发布空间全量导入导出、发布库版本、正文证据片段定位或发布空间向素材空间自动回写。

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
| `GraphMaterial` | 以 `ContentRef` 标识的一份素材的图谱元数据与草稿锁定状态。 |
| `GraphMaterialNode` / `GraphMaterialEdge` | 均持有归属 `ContentRef` 的当前可编辑草稿图与来源（`AI`、`MANUAL`、`IMPORT`）；边的两个端点必须属于同一素材。 |
| `GraphMaterialVersion` | 每次成功发布后固化的、不可变的素材草稿图 JSON 快照；仅用于恢复草稿和审计。 |
| `GraphPublishedNode` / `GraphPublishedEdge` | 发布库当前有效的全局图，支持治理、合并、拆分和删除；`source` 为 `MATERIAL` 或 `MANUAL`；发布库没有版本。 |
| `GraphMaterialEvent` | 持久化的外部素材事件记录；后台按其处理状态消费。第一版事件类型仅为 `DELETED`。 |

`GraphMaterial` 的业务身份为 common-core `ContentRef(contentType, contentId)`，而不是裸 `materialId`；数据库技术主键仅用于内部关联。首期仅接受 `SANCAI_ENTRY`。除共享 `ContentRef` 外，图谱局部 Domain 的对象均以 `Graph` 开头。

`GraphMaterial` 只有在 `DRAFT` 或 `READY` 时可编辑、抽取或切换素材版本。`DRAFT` 表示当前没有节点和边；`READY` 表示至少存在一个草稿节点或边，因而可以发布。`PUBLISHED` 时草稿图只读；必须整体撤回后才能恢复编辑。发布空间治理永不反写草稿图。

### State

```text
GraphMaterial: DRAFT ──(节点或边存在)──> READY → PUBLISHED → READY
                  ▲                      │
                  └──(节点和边均为空)────┘

GraphMaterialEvent: SCHEDULED → PROCESSING → SUCCEEDED / FAILED
```

发布与撤回在单一数据库事务中完成，不持久化中间状态；删除处理中不得发起抽取、编辑、发布、撤回或第二个删除任务；同一素材同时只允许一个运行中的 AI 抽取 Job。

## Persistence Model

所有表使用 `knowledge_graph_` 前缀；主键遵从服务器统一 ID 设计。`lock_version` 用于乐观锁，时间、操作者和审计字段遵从项目基础字段约定。

### Material Space

| Table | Key fields and responsibility |
| --- | --- |
| `knowledge_graph_material` | 技术主键 `id`；业务 `content_type + content_ref_id` 唯一；`content_title_snapshot`、`status`、`published_at`、`lock_version`。`ContentRef` 是业务身份；标题快照只供列表展示和删除后追溯，不参与 Key 或发布匹配，不复制正文。 |
| `knowledge_graph_material_node` | `material_id`、`node_key`、`node_type`、`name`、`source`、`properties_json`；同素材图内 `(material_id, node_key)` 唯一。未能计算 Key 的草稿对象允许 `node_key` 为空。 |
| `knowledge_graph_material_edge` | `material_id`、两端素材节点、`relation_type`、`source`、`qualifiers_json`、`edge_key`；同素材图内 `(material_id, edge_key)` 唯一。未能计算 Key 的草稿关系允许 `edge_key` 为空。 |
| `knowledge_graph_material_version` | `material_id`、递增 `version_no`、`snapshot_json`（`nodes + edges`）、发布人和发布时间；成功发布后写入，永不修改。 |

`properties_json` 与 `qualifiers_json` 是开放多值 JSON 载体：草稿写入只校验其为对象，细分属性和值域作为告警而非拒绝条件；它们不替代可查询的 Key、类型和关系字段。

`GraphKeyHelper` 统一以 Schema 指定的字段生成稳定 Key：节点由节点类型、名称和身份限定规范化后生成；边由两端节点 Key、关系类型、有向性和 Schema 选出的 Key 限定字段生成。`GraphNodeKeyCodec` 与 `GraphEdgeKeyCodec` 调用该 Helper 生成领域值对象，同时负责与持久化字符串互转。

### Published Space

| Table | Key fields and responsibility |
| --- | --- |
| `knowledge_graph_published_node` | `node_key` 全局唯一、`node_type`、展示名称、`source`、`status`、`modified_at`、`lock_version`；`modified_at` 在发布灌注或发布空间修改时刷新。 |
| `knowledge_graph_published_edge` | `edge_key` 全局唯一、两端发布节点、`relation_type`、`source`、`qualifiers_json`、`status`、`modified_at`、`lock_version`；`modified_at` 在发布灌注或发布空间修改时刷新。 |
| `knowledge_graph_published_node_property` | 发布节点的多值属性：字段、唯一 `value`、`is_preferred`。属性来源不在属性行重复保存，由对象级素材关联和系统审计追溯。 |
| `knowledge_graph_published_edge_property` | 发布边的多值限定或展示属性，字段语义同节点属性。 |

同一字段允许多个值时，由 Schema 决定是否可并存、是否必须且只能有一个 `is_preferred`。身份字段或互斥字段不得通过属性表绕过发布冲突。

### Mapping, Operations and Deletion

| Table | Key fields and responsibility |
| --- | --- |
| `knowledge_graph_published_node_material` | `GraphPublishedNodeMaterial`：当前发布节点与素材的关联；`published_node_id + content_type + content_ref_id` 唯一，不关联 `GraphMaterialVersion`。素材删除后保留贡献时，以 `ContentRef` 与来源快照追溯。 |
| `knowledge_graph_published_edge_material` | `GraphPublishedEdgeMaterial`：当前发布边与素材的关联；`published_edge_id + content_type + content_ref_id` 唯一，语义同节点关联。 |
| `knowledge_graph_material_event` | `GraphMaterialEvent`：外部素材 `ContentRef`、事件类型、调度处理状态、变更时间和 `lock_version`；不保存删除快照、用户决策或独立任务。 |

发布对象—素材关联是当前归属和撤回边界，不是版本映射或同步机制。撤回仅移除本素材与发布对象的关联。外部素材删除由 `GraphMaterialEvent(DELETED)` 的既定处理规则异步清理图谱素材及其关联；系统日志保留处理轨迹。

## Core Workflows

### Extraction and Draft Merge

1. 素材列表发起抽取；应用层校验 `knowledge:graph:edit`、素材属于三才图会且当前为 `DRAFT` 或 `READY`。
2. 读取并固定素材内容快照，经 AI 域创建 `AiBatchJob`；不创建图谱专用抽取任务表或在 `GraphMaterial` 保存 Job 引用。图谱读取通过 AI facade 按 `ContentRef + GRAPH_EXTRACTION` 查询最新 Job 或分页历史，不直接依赖 AI application、domain 或 repository。
3. AI 域以 `AiInvocationLog` 记录调用、以 `AiCandidate` 保存候选输出。回调或轮询成功后，图谱应用层先执行宽松结构校验，再按素材内可计算的有效 Key 合并追加：同 Key 补充属性，不同 Key 新增节点或边；无法计算 Key 或命中质量规则的对象保留为草稿告警，不丢弃抽取结果。
4. 重试创建新的 `AiBatchJob`，不覆盖既有 AI 执行历史。已发布素材必须先撤回，不能绕过冻结直接抽取。

### Publication

发布预览是只读计划：对素材全部节点和边计算 Key、执行发布一致性硬校验、匹配发布对象，并同时返回不阻断的质量告警，生成 `CREATE`、`REUSE`、`CONFLICT` 结果。

确认发布时，在事务内重新执行匹配：

1. 以 `node_key` 获取或创建发布节点，并灌注发布节点与本素材的当前关联。
2. 用对应发布端点与关系限定字段计算 `edge_key`，获取或创建发布边，并灌注发布边与本素材的当前关联。
3. 合并可并存属性；Key 不唯一、对象标识重复、端点无效或必须人工决定的身份冲突中止该素材发布。端点兼容、细分属性、限定字段、环和质量问题保留为告警或治理待办。
4. 写入 `GraphMaterialVersion` JSON 快照，并将素材状态置为 `PUBLISHED`；发布命令、校验结果与结果摘要写入系统审计日志。

发布节点、边、当前素材关联、素材版本和状态更新同事务提交。发布流程不是异步任务：页面先预检、让用户一次性处理冲突，确认提交时在事务内重新查询并强校验当前素材、目标节点、目标边及其依赖；任一步失败整体回滚。数据库唯一约束和对象 `lock_version` 防止并发重复或覆盖，不额外保存或传递预览凭证。

批量发布接口只协调多份素材的独立预览与提交结果，不创建批次聚合实体，也不提供跨素材回滚。

### Withdrawal and Deletion

整体撤回以素材为边界，在事务内删除该素材所有发布节点/边关联，解除素材锁定并转为 `READY`；如果草稿节点和边均为空则转为 `DRAFT`。它不删除或回写发布对象、不更改其他素材关联、不删除人工维护内容。发布库没有版本；素材版本只可在可编辑状态下恢复为当前草稿，随后重新发布会生成新的素材版本。

外部素材删除时创建 `GraphMaterialEvent(DELETED, SCHEDULED)`。后台领取事件后置为 `PROCESSING`，按既定删除规则清理草稿图与该素材的发布关联，成功置为 `SUCCEEDED`，失败置为 `FAILED`。事件按 `ContentRef + eventType` 去重，并以 `lock_version` 防止并发领取；重复投递或重试不得重复删除已经清理的记录。失败原因和处理轨迹写入系统日志。

### Published-space Governance

- 人工节点/边先通过 Schema 校验，再写发布对象和 `MANUAL` 来源。
- 合并选择保留节点，将全部映射、边端点和可并存属性迁移到保留节点；若迁移后产生重复边，按 `edge_key` 合并。
- 拆分创建新节点，逐项分配属性、边和素材映射；所有受影响对象必须分配完毕才能提交。
- 删除发布节点前必须显式删除关联边或解除依赖；素材映射按既有撤回与删除规则处理，操作轨迹由系统日志保留。
- 合并、拆分和删除在确认时重新查询全部节点、边、属性和素材映射并执行强校验，不能只依赖预览时的影响列表；预览后新增的依赖必须按当前操作规则处理或拒绝提交。

## Application and Repository Semantics

Java 实现按素材空间、发布空间和读取空间拆分应用服务：

- `GraphExtractionApplicationService` 只负责提交、查询、重试和应用 AI 结果。提交时固定模型、提示词版本、消息、变量、schema 和参数快照，后续执行不得重新解析可变业务配置。
- `GraphMaterialApplicationService` 管理单素材草稿图、JSON 导入导出和素材版本恢复。写入统一经 `GraphMaterialGraphSaver` 保存 change set，并以 `GraphMaterial.lockVersion` 做数据库 CAS。
- `GraphPublicationApplicationService` 提供发布/撤回预览和执行。单素材发布由 `GraphPublicationExecutor` 在独立事务内重新加载、重新匹配和强校验；批量发布逐素材隔离，单项失败不回滚已成功素材，也不阻止后续素材。
- `GraphPublishedApplicationService` 管理发布空间治理。所有更新既有发布节点、边和事件状态的操作都调用对应 Repository 的 `updateIfLockVersion`，发布空间治理不修改 `GraphMaterialNode`、`GraphMaterialEdge` 或 `GraphMaterialVersion`。
- `GraphMaterialEventApplicationService` 记录外部素材删除事件并委托 `claim -> cleanup -> failure recorder` 执行器链。`claim`、`cleanup` 和失败记录均使用 `REQUIRES_NEW`，使多实例并发只由数据库 CAS 决定。
- `GraphPortalApplicationService` 只读发布空间映射，不回退读取草稿；素材不可见、未发布、撤回或已删除均返回空图。
- `GraphWorkbenchApplicationService` 只使用发布空间读仓储；最近种子节点限制 100，局部图节点限制 200，关联边依赖 Repository 游标分页。

Repository 语义固定为领域端口：`db/data-source/**` 是种子事实来源，`build/seed-sql/` 仅为临时生成物；所有 CAS SQL 必须在同一语句写业务字段并递增 `lock_version`，不得先查询再用普通 `updateById()` 代替 CAS。发布对象唯一键并发冲突由数据库约束兜底；发布执行器捕获唯一冲突后按 Key 重新查询并复用，其他 SQL 异常继续抛出并回滚。

## Query and Rendering

工作台默认查询最近更新的 100 个有效发布节点，不加载全图。前端分批请求这些种子节点的关联边；每批边返回即渲染边、补齐另一端节点并更新连接度。查询结束后移除连接度为零的节点并稳定布局。

领域读取端口以 `GraphPublishedNodeRepository.listRecentlyUpdated(limit)` 取得种子节点，并以 `GraphPublishedEdgeRepository.listIncidentEdges(nodeIds, afterEdgeId, limit)` 返回边、下一游标和截断标记；种子节点按 `modified_at` 倒序、稳定 ID 次序读取。

最终画布最多 200 个节点；边随已渲染节点集合返回，不另设边数上限。超过上限时，后端以 `modified_at` 倒序、再以关系数和稳定 ID 截断，返回 `truncated=true` 与继续展开游标。种子节点在命中首条边前使用骨架或淡化状态，避免孤立节点闪烁。

鸟瞰层返回门类统计和质量指标；门类层按门类/类型过滤局部图；详情层返回节点或边的属性、来源素材、映射和 `source`。前台只按稿件读取其素材图，不读取发布空间工作台数据；仅当素材处于 `PUBLISHED` 且稿件本身通过既有内容可见性校验时，才返回该稿件当前有效映射对应的图谱内容，草稿、撤回和已删除素材返回空状态。

质量查询仅计算并暴露：孤立节点数、核心节点关系缺失数及对应对象列表。核心节点规则由 Schema 提供：`coreRelationPolicy.mode = ANY_INCIDENT_RELATION` 表示节点只要存在一条入边或出边，其关系类型属于 `relationTypes`，即不计为关系缺失。

## Interface Design

后台接口位于 Knowledge admin entry subdomain。下列为后台资源边界，具体 request/response DTO 在 `docs/20-interfaces/` 固化：

| Resource | Main operations |
| --- | --- |
| `/knowledge/graph/workbench` | 概览统计、最近发布种子、关联边分页、门类图、搜索、对象详情、质量待办。 |
| `/knowledge/graph/materials` | 素材分页、素材草稿图读取、节点/边 CRUD、JSON 导入导出、抽取任务查看与触发。 |
| `/knowledge/graph/materials/{id}/publish` | 预览、确认发布、撤回；批量发布请求逐素材返回结果。 |
| `/knowledge/graph/published` | 发布节点/边 CRUD、来源、合并、拆分、删除。 |
| `/knowledge/graph/material-events` | 外部素材事件的状态查询与失败重试。 |

门户入口另提供只读资源 `/portal/knowledge/graph/materials/{contentType}/{contentRefId}`：先执行稿件既有内容可见性校验，再按素材状态和有效映射返回该稿件图谱；该资源不暴露发布空间搜索、治理或跨素材关系。

JSON 导入只允许写入未发布素材草稿图。先执行格式和 Schema 校验，返回对象级错误；确认后按照用户选择的 `MERGE` 或 `REPLACE` 执行。下载仅导出当前单素材草稿节点、边、属性、限定字段和 Schema 版本，不导出发布空间、映射、审计或人工治理数据。

## Frontend Design

Admin 使用现有 `knowledge:graph:view` 与 `knowledge:graph:edit`：

- 工作台、整体治理、素材空间和素材事件列表读取使用 `view`。
- 草稿 CRUD、抽取、JSON 上传下载、发布、撤回、素材事件重试、发布空间治理、合并与拆分使用 `edit`。

菜单固定为“知识图谱 / 工作台 / 整体治理 / 素材空间”。素材空间包含素材库、单素材图谱和素材事件列表。高风险动作必须展示影响预览、二次确认和操作结果。

发布预览在素材画布内完成：绿色新建、橙色关联、红色冲突、蓝色已发布。红色对象点击后在右侧抽屉展示素材对象、候选发布对象、关键差异和动作；存在未决冲突时禁止确认。

## Migration and Compatibility

现有 `graph-extraction`、候选应用、图谱版本、正式实体/关系、精修草稿、世系和旧质量报告接口与本设计不兼容。实施采用独立迁移阶段：

1. 新建上述图谱表、Schema 代码和新接口，不复用旧表的状态语义。
2. 按已确认的迁移规则导入仍需保留的三才图会数据；未定义迁移规则的数据不得自动映射为已发布知识。
3. 前端切换到新菜单与接口后，删除旧图谱入口、API、测试和旧表写入路径。
4. 确认没有调用方后，再按数据库治理规则删除旧表或归档其只读历史。

迁移规则、数据核对清单和切换顺序属于独立 RUNBOOK，不在本设计中假定。

## Verification

- 单元测试：Schema Key 与约束、草稿合并、发布匹配、冲突、乐观锁、属性首选、合并、拆分、撤回和素材事件状态。
- 集成测试：发布事务原子性、唯一约束并发、AI 域协作与快照、素材事件幂等重试、素材删除后映射状态、权限拒绝。
- 前端 E2E：抽取到草稿、发布预览冲突、冻结与撤回、发布治理、素材事件状态、最近发布渐进渲染、200 节点截断与 JSON 导入校验。
- 数据迁移验证：迁移前后素材数、节点/边数和映射数抽样核对；旧入口移除后确认不存在旧模型写入。

## Related Documents

- [`KNOWLEDGE-GRAPH-REQUIREMENTS.md`](../10-requirements/KNOWLEDGE-GRAPH-REQUIREMENTS.md)：正式产品需求。
- [`KNOWLEDGE-GRAPH-SCHEMA.json`](../20-interfaces/KNOWLEDGE-GRAPH-SCHEMA.json)：后端处理、AI 输出、JSON 导入与前端表单共用的机器可读 Schema 规范。
- [`KNOWLEDGE-REQUIREMENTS.md`](../10-requirements/KNOWLEDGE-REQUIREMENTS.md)：Knowledge 域总览与标签治理。
- [`WORKERS-AI-INTERFACE.md`](../20-interfaces/WORKERS-AI-INTERFACE.md)：AI 域与 worker 的调用边界。
- [`SERVERS-DATABASE-RULES.md`](../00-governance/SERVERS-DATABASE-RULES.md)：数据库事实来源与迁移规则。
