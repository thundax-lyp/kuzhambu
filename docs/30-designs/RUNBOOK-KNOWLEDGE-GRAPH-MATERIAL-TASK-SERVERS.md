# RUNBOOK Knowledge Graph Material Task Servers

## Purpose

本手册只实现 `kuzhambu-servers/` 的图谱素材管理和提取任务能力。一个提交单元就是一个 TODO 和一个独立提交。执行者按本手册的 DAG 依赖执行，每个单元结束后运行其验证，再开始依赖它的下一项。

HTTP 真相源是 [`KNOWLEDGE-GRAPH-INTERFACE.md`](../20-interfaces/KNOWLEDGE-GRAPH-INTERFACE.md)。不能因现有代码、旧表或旧接口不同而修改该契约；若契约本身需变更，先停止实现、更新契约并重新评审。

路径别名在全文固定且可直接展开：`KD` = `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge`，`KA` = `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge`，`KINF` = `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge`，`KIF` = `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge`。测试路径使用同模块的 `src/test/java` 替换 `src/main/java`。

## Scope

- Knowledge graph 的素材复合查询、素材统计快照、提取任务、候选处置、批量操作和到期清理。
- Classics facade 的最小读取能力，以及 AI facade 的最小候选读取、处置和清理协作能力。
- `db/schema/knowledge.sql`、Java domain/application/infra/interface、测试。

## Non-goals

- 不修改 `kuzhambu-apps/admin-web/`，不等待前端联调。
- 不由 Knowledge 读取 Classics/AI 的 mapper、repository、DO 或表。
- 不调用 Python worker；只能调用 `AiFacade`。
- 不实现旧 `/knowledge/graph-extraction/*`、`/knowledge/graph-result/*`、`/knowledge/refinement/*` 的兼容写路径。

## Fixed Data Structures

以下结构是本次后端的目标模型；字段名必须与接口契约一致。

| Object | Required fields | Rules |
| --- | --- | --- |
| `GraphMaterial` | `id`, `contentType`, `contentRefId`, `contentTitleSnapshot`, `status`, `currentExtractionTaskId`, `lockVersion` | `contentType + contentRefId` 唯一；`currentExtractionTaskId` 只指向活动任务。 |
| `GraphMaterialStats` | `materialId`, `draftNodeCount`, `draftEdgeCount`, `publishedNodeCount`, `publishedEdgeCount`, `activeTaskCount`, `pendingReviewTaskCount`, `failedTaskCount`, `statsRevision`, `calculatedAt` | 一素材一行；列表只读此快照，禁止逐行统计明细。 |
| `GraphExtractionTask` | `id`, `materialId`, `contentRef`, `contentSnapshotJson`, `modelSnapshotJson`, `promptSnapshotJson`, `outputSchemaJson`, `executionStatus`, `disposition`, `attemptNo`, `lockVersion`, `batchId`, `candidateId`, `currentStage`, `progress`, `idempotencyKey`, `regeneratedFromTaskId`, `supersededByTaskId`, `triggeredByTaskId`, `requestedAt`, `completedAt`, `disposedAt`, `purgeAfter` | `executionStatus` 与 `disposition` 不得复用。 |
| `GraphExtractionStage` | `extractionTaskId`, `stageOrder`, `stageName`, `status`, `progress`, `inputSummaryJson`, `outputSummaryJson`, `failureReason`, `startedAt`, `completedAt` | 同一任务的 `stageOrder` 唯一；不得保存正文、完整提示词、凭据。 |

固定枚举：

```text
executionStatus = PENDING | RUNNING | SUCCEEDED | FAILED | CANCELLED
disposition = null | PENDING | ADOPTED_MERGE | ADOPTED_REPLACE | DISCARDED | SUPERSEDED
```

固定转换：

```text
FAILED -> PENDING                         原任务重试，attemptNo + 1
SUCCEEDED + PENDING -> ADOPTED_MERGE      应用候选，合并草稿
SUCCEEDED + PENDING -> ADOPTED_REPLACE    应用候选，整体覆盖草稿
SUCCEEDED + PENDING -> DISCARDED          丢弃候选
SUCCEEDED + * -> SUPERSEDED               后续候选替代
disposedAt + 7 days <= now                可清理任务和阶段；不清理草稿、发布和统计
```

## Fixed HTTP Surface

每个管理端接口使用 `POST` 和 `ApiResponse<T>`，读取校验 `knowledge:graph:view`，写入校验 `knowledge:graph:edit`。实现前逐项对照接口文档；不得以“前端暂未使用”为由省略。

| Resource | Request key fields | Response |
| --- | --- | --- |
| `/knowledge/graph/material/page` | filters + `pageNo`, `pageSize` | Classics 分页结果叠加 `material?`, `materialStats?`, `latestTask?` |
| `/knowledge/graph/material/get` | `contentRef` | `source`, `material?`, `materialStats?`, `nodes`, `edges`, `taskSummary` |
| `/knowledge/graph/material/extraction/create` | `contentRef`, `idempotencyKey` | `task` |
| `/knowledge/graph/task/batch/create` | `selection.contentRefs` XOR `selection.volumeCode`, `idempotencyKey` | `batchExtractionResult` |
| `/knowledge/graph/task/page` | filters, `contentRefs?`, `batchId?`, `groupBy` | flat `Page<task>` or grouped page |
| `/knowledge/graph/task/get` | `taskId` | `task`, `source`, `materialStats`, `stages`, `relatedTasks`, `candidate` |
| `/knowledge/graph/task/retry`, `/cancel` | `taskId`, `taskLockVersion`, `expectedExecutionStatus`, `idempotencyKey` | `task` |
| `/knowledge/graph/task/candidate/apply` | task version/status + `expectedDisposition:PENDING`, `materialLockVersion`, `applyMode`, `idempotencyKey` | `task`, `material` |
| `/knowledge/graph/task/candidate/discard`, `/regenerate` | task version/status + `idempotencyKey` | `task` or `{task,material}` |
| `/knowledge/graph/publication/batch/withdrawal/*` | selected `contentRefs` / per-material lock version | per-material preview/result |

任务命令的固定错误码：`GRAPH_TASK_LOCK_CONFLICT`、`GRAPH_TASK_STATE_CONFLICT`、`GRAPH_TASK_ACTIVE_EXISTS`、`GRAPH_CANDIDATE_UNAVAILABLE`。重复 `idempotencyKey` 返回首次成功结果，不得重新创建任务或投递调用。

## Plan

### DAG Execution Topology

本 RUNBOOK 允许本地并行，但同时最多两条执行线、两个 worktree。不得为每个提交单元创建独立 worktree，也不得在两个执行线中修改同一个文件。每条执行线只提交本线所属单元；集成分支只接收已验证的完整提交。

开始前，从同一个干净的集成基线创建两条本地分支和 worktree：

```sh
git worktree add ../kuzhambu-kg-line-a -b feature/knowledge-graph-material-line-a <integration-base>
git worktree add ../kuzhambu-kg-line-b -b feature/knowledge-graph-material-line-b <integration-base>
```

`<integration-base>` 是准备合入的目标分支或其当前特性分支。每个同步点由集成人将已验证提交按编号顺序 cherry-pick 到集成分支；下一阶段的 worktree 必须先 rebase 到该集成分支。不要合并两个执行线分支，不要在未同步依赖时开始后续单元。

| 阶段 | 执行线 A：存储与任务 | 执行线 B：跨域与查询 | 开始条件 | 同步条件 |
| --- | --- | --- | --- | --- |
| G0 | `S0` | - | 无 | `S0` 已提交基线证据 |
| G1 | `S1a -> S1b -> S2` | `S3a -> S3b` | `S0` 已同步 | `S1b`、`S2`、`S3a`、`S3b` 均已同步 |
| G2 | `S4b -> S6b -> S5a` | `S4a -> S4c -> S5b` | G1 已同步；`S4b` 还依赖 `S3a` 与 `S3b` | `S4a`、`S4b`、`S4c`、`S5a`、`S5b`、`S6b` 均已同步 |
| G3 | - | `S6a` | G2 的 `S4b`、`S4c` 已同步 | `S6a` 已同步 |
| G4 | `S6c -> S6d` | - | G2、G3 全部同步 | `S6c` 后才可开始 `S6d` |

额外依赖固定如下：`S4a` 和 `S4c` 均依赖 `S1b` 与 `S3a`；`S5a` 依赖 `S4a` 与 `S4b`；`S6a` 依赖 `S4a`、`S4b`、`S4c`；`S6b` 依赖 `S1b`、`S2`、`S3b`；`S6c` 依赖 `S5a`、`S5b`、`S6a`、`S6b`。这些依赖优先于表中的执行线归属。

每个同步点固定执行：检查提交只对应一个 RUNBOOK 单元，运行该单元验收，cherry-pick 到集成分支，运行受影响模块的最窄 Maven 测试；发生冲突时停止后续单元，先在产生冲突的执行线修复并重新验证。

### S0. Freeze the Baseline

**Input:** 当前分支和现有图谱数据。

**Output:** 可比较的迁移前统计与明确的旧路径清单。
**Modify:** 新建 `docs/40-readiness/KNOWLEDGE-GRAPH-MATERIAL-TASK-BASELINE.md`；不修改数据库数据或生产代码。

1. 阅读现有文件：
   - `db/schema/knowledge.sql`
   - `GraphExtractionApplicationService.java`
   - `GraphExtractionApplicationServiceImpl.java`
   - `GraphMaterialApplicationService.java`
   - `GraphController.java`
   - `GraphMaterialDO.java` 和现有 extraction task DO/mapper/repository。
2. 记录现有素材、节点、边、发布映射、抽取任务数量和旧任务状态分布。
3. 列出所有仍调用 `retry_from_task_id`、旧 `status`、旧 graph extraction/result HTTP 入口的 Java 文件。

**Done when:** 基线文档记录采集时间、统计口径、素材/图对象/发布映射/抽取任务的数量与状态分布，以及旧调用路径清单；该文档与本单元一并提交。

### S1. Add Schema and Persistence Types

**Modify exactly:**

- `db/schema/knowledge.sql`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/model/entity/GraphMaterial.java`
- `KD/domain/graph/model/entity/GraphExtractionTask.java`（新建）
- `KD/domain/graph/model/entity/GraphMaterialStats.java`（新建）
- `KD/domain/graph/model/enums/GraphExtractionExecutionStatus.java`（新建）
- `KD/domain/graph/model/enums/GraphExtractionDisposition.java`（新建）
- `KD/domain/graph/repository/GraphExtractionTaskRepository.java`（新建）
- `KD/domain/graph/repository/GraphMaterialStatsRepository.java`（新建）
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/persistence/dataobject/GraphMaterialDO.java`
- `KINF/infra/graph/persistence/dataobject/GraphExtractionTaskDO.java`、`KINF/infra/graph/persistence/dataobject/GraphMaterialStatsDO.java`（新建）
- `KINF/infra/graph/persistence/mapper/GraphExtractionTaskMapper.java`、`KINF/infra/graph/persistence/mapper/GraphMaterialStatsMapper.java`（新建）
- `KINF/infra/graph/repository/impl/GraphExtractionTaskRepositoryImpl.java`、`KINF/infra/graph/repository/impl/GraphMaterialStatsRepositoryImpl.java`（新建）
- `KINF/infra/graph/persistence/assembler/GraphExtractionTaskPersistenceAssembler.java`、`KINF/infra/graph/persistence/assembler/GraphMaterialStatsPersistenceAssembler.java`（新建）。

**Steps:**

1. 给 `knowledge_graph_material` 补齐活动任务指针映射和乐观锁更新 SQL。
2. 新建 `knowledge_graph_material_stats`，以 `material_id` 唯一约束保证一对一。
3. 迁移 `knowledge_graph_extraction_task`：新增 S0 表中的字段和按 `(material_id, execution_status, requested_at)`、`batch_id`、`purge_after` 的查询索引。
4. 增加数据库级保护：采用唯一活动任务键、条件唯一索引或在事务内锁定 `GraphMaterial` 后检查；实现必须可在并发测试中证明不会有两条活动任务。
5. DO、mapper、repository 只做存取和转换；状态转换不得放入 mapper。

**Verify:** 新增 DDL 测试；repository 集成测试覆盖插入、按素材查询、按批次查询、到期查询和乐观锁更新。

**Commit units:**

1. S1a 只修改 `db/schema/knowledge.sql` 和 DDL 测试：新增统计表、任务字段、索引及数据库级活动任务互斥保护；验证 schema 导入和 DDL 断言。
2. S1b 修改本节其余 domain/infra 文件和 repository 集成测试：实现实体、DO、mapper、assembler、repository 与乐观锁读写；验证并发活动任务互斥、按素材/批次/到期查询和版本更新。

### S2. Implement Task State Machine

**Modify exactly:**

- S1 的 `GraphExtractionTask.java` 和两个枚举。
- `KD/domain/graph/service/GraphExtractionTaskDomainService.java`（新建）。
- `KA/application/graph/command/GraphExtractionCommand.java`
- `KA/application/graph/command/GraphExtractionRetryCommand.java`
- 新建 `GraphExtractionCancelCommand.java`、`GraphExtractionBatchCommand.java`、`GraphExtractionCandidateApplyCommand.java`、`GraphExtractionCandidateDiscardCommand.java`、`GraphExtractionRegenerateCommand.java`。
- `KA/application/graph/result/GraphExtractionTaskResult.java`、`GraphExtractionBatchResult.java`、`GraphExtractionCandidatePreviewResult.java`、`GraphExtractionStageResult.java`（新建）。
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/test/java/com/thundax/kuzhambu/knowledge/domain/graph/service/GraphExtractionTaskDomainServiceTest.java`（新建）。

**Steps:**

1. `retry` 仅接受 `FAILED`，清空本次错误，`attemptNo + 1`，转 `PENDING`；绝不创建第二条任务。
2. `cancel` 仅接受 `PENDING` 或 `RUNNING`，转 `CANCELLED`；成功后清空素材活动任务指针。
3. 成功回调只写候选引用、阶段和摘要，转 `SUCCEEDED + PENDING disposition`；不得写草稿节点或边。
4. `apply(MERGE|REPLACE)` 同时校验任务版本、任务预期状态、任务采纳状态和素材版本；成功后更新草稿、处置状态和素材统计。
5. `discard` 只写处置状态和审计；`regenerate` 读取当前来源和当前运行配置，创建新任务并填 `regeneratedFromTaskId`。
6. 为每个命令持久化并查询幂等键。重复请求只返回首次结果。

**Verify:** 领域单测覆盖全部状态转换、错误码、双重重试、双重取消、候选重复应用和版本冲突。

### S3. Add Classics and AI Facade Contracts

**Modify exactly:**

- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/ClassicsFacade.java`
- 同模块新增 `request/KnowledgeGraphMaterialPageFacadeRequest.java`、`request/KnowledgeGraphMaterialSnapshotFacadeRequest.java`、`response/KnowledgeGraphMaterialPageFacadeResponse.java`、`response/KnowledgeGraphMaterialSnapshotFacadeResponse.java`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/facade/impl/ClassicsFacadeImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/facade/assembler/ClassicsFacadeAssembler.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/AiFacade.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/GetKnowledgeGraphCandidateFacadeRequest.java`、`CleanupKnowledgeGraphCandidateFacadeRequest.java`（新建）
- `kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/response/KnowledgeGraphCandidateFacadeResponse.java`、`CleanupKnowledgeGraphCandidateFacadeResponse.java`（新建）
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/facade/impl/AiFacadeImpl.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/facade/assembler/AiFacadeAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/facade/impl/ClassicsFacadeImplTest.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/facade/impl/AiFacadeImplTest.java`。

**Steps:**

1. Classics page facade 入参必须包含当前主体、筛选和分页；返回最小来源字段：`ContentRef`、标题、来源类型、分类、卷目、可见性、可图谱化标志。
2. Classics snapshot facade 必须再次校验当前主体和内容可用性，返回冻结正文快照；Knowledge 不缓存完整正文以外的 Classics 实体。
3. AI facade 入参必须是已冻结的内容、模型、提示词、变量和 Schema 快照；返回任务引用、候选摘要和阶段摘要。
4. AI 候选的查询、采用标记、拒绝和清理必须只经 `AiFacade`；不得给 Knowledge repository 增加 AI 表访问。

**Verify:** facade 单测证明不可见稿件不返回、页面分页在 Classics 内完成、AI 调用拿到冻结快照且无完整正文/凭据日志。

**Commit units:**

1. S3a 只修改 Classics facade、其 request/response、实现、assembler 与 `ClassicsFacadeImplTest`。
2. S3b 只修改 AI facade、其 request/response、实现、assembler 与 `AiFacadeImplTest`；必须提供候选读取、采用标记、拒绝和清理协作。

### S4. Implement Application Queries and Commands

**Modify exactly:**

- `GraphMaterialApplicationService.java`、`GraphMaterialApplicationServiceImpl.java`
- `GraphExtractionApplicationService.java`、`GraphExtractionApplicationServiceImpl.java`
- `KA/application/graph/service/GraphExtractionApplicationService.java`、`KA/application/graph/service/impl/GraphExtractionApplicationServiceImpl.java`（任务创建、读取、状态动作和候选处置全部留在此服务）。
- `GraphMaterialListQuery.java`、`GraphMaterialQuery.java`、`GraphExtractionQuery.java`。
- 新建 `GraphTaskPageQuery.java`、`GraphTaskDetailQuery.java`、`GraphBatchWithdrawalCommand.java`、`GraphBatchWithdrawalPreviewQuery.java`。
- `KA/application/graph/support/GraphApplicationAssembler.java`
- `KA/application/graph/operator/GraphMaterialStatsRefresher.java`、`KA/application/graph/operator/GraphTaskCandidateResolver.java`（新建）。

**Steps:**

1. 素材分页：先调用 Classics page facade，获得一页来源；再按当页 `ContentRef` 批量读取 material/stats/latestTask；没有 material 时返回 `material:null`。
2. 素材详情/抽取/发布前：调用 Classics snapshot facade 做可见性和可用性校验。
3. 批量提取：按输入顺序逐素材执行，捕获每项业务失败并填 result；不得建立跨素材事务。
4. 任务分页支持 flat 和 `groupBy=MATERIAL`；`contentRefs` 和 `batchId` 只过滤 Knowledge 任务，不回到前端过滤。
5. 任务详情按固定 DTO 组装 stage、candidate preview、related tasks；候选不可用返回指定业务码。
6. 批量撤回按素材独立预览/执行，返回输入顺序一致的逐项结果。

**Verify:** application 测试覆盖未初始化素材、来源不可见、批量部分失败、候选无载荷、按素材分组和活动任务冲突。

**Commit units:**

1. S4a 只修改 `GraphMaterialApplicationService*`、`GraphMaterialListQuery`、`GraphMaterialQuery` 和 `GraphApplicationAssembler`，实现素材复合分页与详情。
2. S4b 只修改 `GraphExtractionApplicationService*`、`GraphExtractionQuery`、`GraphTaskPageQuery`、`GraphTaskDetailQuery`、任务 command/result 与 `GraphTaskCandidateResolver`，实现提取、任务查询、状态动作和候选处置。
3. S4c 只修改 `GraphPublicationApplicationService*`、`GraphBatchWithdrawalCommand`、`GraphBatchWithdrawalPreviewQuery` 及其测试，实施批量撤回预览与执行。

### S5. Expose HTTP DTOs and Controller Methods

**Modify exactly:**

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/GraphController.java`
- `KIF/interfaces/admin/graph/controller/request/GraphMaterialRequests.java`
- `KIF/interfaces/admin/graph/controller/request/GraphExtractionRequests.java`（新建）
- `KIF/interfaces/admin/graph/controller/request/GraphPublicationRequests.java`
- `KIF/interfaces/admin/graph/controller/response/GraphMaterialResponses.java`
- `KIF/interfaces/admin/graph/controller/response/GraphExtractionResponses.java`（新建）
- `KIF/interfaces/admin/graph/controller/response/GraphPublicationResponses.java`
- `KIF/interfaces/admin/graph/assembler/GraphInterfaceAssembler.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/GraphControllerTest.java`（新建）。

**Steps:**

1. Fixed HTTP Surface 表中的每个 URL 都有一条 Controller 方法、独立 request/response 类型和 assembler 转换。
2. request 不允许传操作者 ID；任务动作必须校验 `taskLockVersion`、预期状态、幂等键。
3. response 的 `id`、`lockVersion`、时间、分页字段全部序列化为字符串；`candidate` 只返回 `candidatePreview|null`。
4. 注解权限与接口读写语义一致；Controller 不写业务状态逻辑。

**Verify:** MockMvc/WebMvc 测试覆盖每个 URL 的 200、无权限、状态冲突、版本冲突、候选不可用和批量部分失败。

**Commit units:**

1. S5a 只修改素材、提取任务、候选处置相关 request/response、assembler、`GraphController` 方法和 Web 测试。
2. S5b 只修改批量撤回相关 request/response、assembler、`GraphController` 方法和 Web 测试；不得改变已在 S5a 完成的资源。

### S6. Refresh Stats, Schedule Cleanup, Remove Legacy Writes

**Modify exactly:**

- 新建 `KA/application/graph/scheduler/GraphExtractionTaskCleanupScheduler.java`。
- `GraphMaterialEventScheduler.java`（仅在需要事件驱动统计刷新时修改）。
- `GraphMaterialStatsRepositoryImpl.java` 和相关 mapper。
- `GraphExtractionApplicationServiceImpl.java`、`GraphMaterialApplicationServiceImpl.java`、`GraphPublicationApplicationServiceImpl.java`（只补统计刷新点）。
- 旧 extraction/result Controller、service、测试和 schema 兼容列的删除仅在 S0 清单确认无调用方后另一个小提交执行。

**Steps:**

1. 在草稿变更、任务状态变更、候选处置、发布和撤回后刷新对应素材快照。
2. 清理 scheduler 查询 `purgeAfter <= now` 的处置终态任务；先通过 AI facade 清理，再删除 Knowledge 任务和阶段。
3. 清理失败可重试，不得删除草稿、映射、发布记录或最近处置摘要。
4. 旧接口删除与旧列删除不可与 scheduler 首次上线放在同一提交。

**Verify:** 时钟可控的测试覆盖 7 天前后边界、AI 清理失败重试、统计刷新和保留数据不被删除。

**Commit units:**

1. S6a 只修改 `GraphMaterialStatsRefresher`、统计 repository/mapper，以及三个应用服务的统计刷新点和测试。
2. S6b 只修改 `GraphExtractionTaskCleanupScheduler`、任务 repository 清理查询、`AiFacade` 清理协作和时钟可控测试。
3. S6c 只修改 `docs/40-readiness/KNOWLEDGE-GRAPH-MATERIAL-TASK-BASELINE.md`，补充迁移后统计、差异结论和验证命令结果。
4. S6d 只删除旧 extraction/result/refinement 写路径、兼容 schema 字段及无引用测试；开始前必须确认 S6c 证据已提交。

## Required Commit Boundaries

1. `Docs(knowledge): 记录图谱素材任务迁移基线`：S0。
2. `Feat(knowledge): 扩展图谱素材任务表结构`：S1a。
3. `Feat(knowledge): 实现图谱素材任务持久化`：S1b。
4. `Feat(knowledge): 实现图谱提取任务状态机`：S2。
5. `Feat(classics): 提供图谱素材跨域读取门面`：S3a。
6. `Feat(ai): 提供图谱候选协作门面`：S3b。
7. `Feat(knowledge): 实现图谱素材复合查询`：S4a。
8. `Feat(knowledge): 实现图谱提取任务应用服务`：S4b。
9. `Feat(knowledge): 实现图谱批量撤回`：S4c。
10. `Feat(knowledge): 暴露图谱素材任务接口`：S5a。
11. `Feat(knowledge): 暴露图谱批量撤回接口`：S5b。
12. `Feat(knowledge): 刷新图谱素材统计快照`：S6a。
13. `Feat(knowledge): 清理到期图谱提取任务`：S6b。
14. `Docs(knowledge): 核对图谱素材任务迁移结果`：S6c。
15. `Refactor(knowledge): 清理旧图谱提取写路径`：S6d。

## Verification

每个小任务先格式化所改模块，再运行最窄 Maven 测试。S6 完成后运行：

```sh
cd kuzhambu-servers
mvn -pl biz/knowledge/kuzhambu-knowledge-domain,biz/knowledge/kuzhambu-knowledge-application,biz/knowledge/kuzhambu-knowledge-infra,biz/knowledge/kuzhambu-knowledge-interface -am spotless:check checkstyle:check test
```

最后启动 admin starter，用具有和不具有 `knowledge:graph:view/edit` 的真实登录主体调用素材和任务接口。验证浏览器/HTTP 客户端只请求 Knowledge 接口，且 Knowledge 不泄露 Classics 正文、提示词、模型凭据或 AI 内部载荷。

## Closure

完成条件：S1-S6 均有测试证据；迁移前后统计已核对；接口契约全部实现；跨域边界和 7 天清理已验证。将证据写入 `docs/40-readiness/` 后删除本 RUNBOOK。
