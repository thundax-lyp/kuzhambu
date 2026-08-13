# Knowledge Graph Application Services Runbook

## Purpose

本 RUNBOOK 用于从当前代码基线开始，完整实现 Knowledge 图谱 Domain 行为、Repository 能力和七个 `Graph*ApplicationServiceImpl`。执行者不需要回看历史讨论；本文给出已确认边界、当前缺口、逐文件动作、目标方法、事务顺序、测试和停止条件。

完成后必须同时满足：

- 素材草稿图支持节点/边 CRUD、节点合并/拆分、版本恢复、JSON 导入/导出。
- 图谱抽取通过 AI 域的 `AiBatchJob`、`AiInvocationLog`、`AiCandidate` 执行和追溯，不建立图谱任务表。
- 发布、撤回是同步事务；发布空间与素材草稿空间相互分离。
- 发布空间支持节点/边 CRUD、合并、拆分、删除及影响预览。
- 外部素材删除通过 `GraphMaterialEvent(DELETED)` 异步、幂等、可重试地清理。
- Portal 和 Workbench 只读取发布空间，不加载发布全图后在 Java 内分页。
- `GraphMaterial`、`GraphPublishedNode`、`GraphPublishedEdge`、`GraphMaterialEvent` 的乐观锁由数据库 CAS 保证。

本文是临时执行手册，不替代以下真相源：

- [`KNOWLEDGE-GRAPH-REQUIREMENTS.md`](../10-requirements/KNOWLEDGE-GRAPH-REQUIREMENTS.md)
- [`KNOWLEDGE-GRAPH-DESIGN.md`](./KNOWLEDGE-GRAPH-DESIGN.md)
- [`KNOWLEDGE-GRAPH-SCHEMA.json`](../20-interfaces/KNOWLEDGE-GRAPH-SCHEMA.json)
- [`SERVERS-ARCHITECTURE.md`](../00-governance/SERVERS-ARCHITECTURE.md)
- [`SERVERS-DATABASE-RULES.md`](../00-governance/SERVERS-DATABASE-RULES.md)

## Scope

### Included Modules

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-facade`
- 为补齐抽取提交契约所必需的 `kuzhambu-ai-application`
- 复用现有 `kuzhambu-classics-facade` 素材快照读取契约
- 必要的 `db/schema/knowledge.sql` 索引校准
- 对应单元测试、Repository 集成测试和 ApplicationService 测试

### Target Application Services

以下接口已经存在，必须新建对应实现：

| 现有接口 | 新建实现文件 |
| --- | --- |
| `GraphMaterialApplicationService` | `$KG_APP_JAVA/application/graph/service/impl/GraphMaterialApplicationServiceImpl.java` |
| `GraphExtractionApplicationService` | `$KG_APP_JAVA/application/graph/service/impl/GraphExtractionApplicationServiceImpl.java` |
| `GraphPublicationApplicationService` | `$KG_APP_JAVA/application/graph/service/impl/GraphPublicationApplicationServiceImpl.java` |
| `GraphPublishedApplicationService` | `$KG_APP_JAVA/application/graph/service/impl/GraphPublishedApplicationServiceImpl.java` |
| `GraphMaterialEventApplicationService` | `$KG_APP_JAVA/application/graph/service/impl/GraphMaterialEventApplicationServiceImpl.java` |
| `GraphPortalApplicationService` | `$KG_APP_JAVA/application/graph/service/impl/GraphPortalApplicationServiceImpl.java` |
| `GraphWorkbenchApplicationService` | `$KG_APP_JAVA/application/graph/service/impl/GraphWorkbenchApplicationServiceImpl.java` |

本文使用以下路径别名；它们是文档缩写，不要求在 Shell 中设置环境变量：

```text
$KG_DOMAIN_JAVA = kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph
$KG_DOMAIN_TEST = kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/test/java/com/thundax/kuzhambu/knowledge/domain/graph
$KG_APP_JAVA = kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge
$KG_APP_TEST = kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge
$KG_INFRA_JAVA = kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph
$KG_INFRA_TEST = kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/graph
$AI_FACADE_JAVA = kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade
$AI_APP_JAVA = kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application
$AI_APP_TEST = kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application
```

后文只写测试类名时，领域测试放到 `$KG_DOMAIN_TEST` 下与生产 package 镜像的位置，应用测试放到 `$KG_APP_TEST/application/graph` 下与生产 package 镜像的位置，infra 测试放到 `$KG_INFRA_TEST` 下与生产 package 镜像的位置。

## Non-goals

- 不实现 Controller、HTTP Request/Response、权限菜单或前端页面。
- 不建立 `GraphPublishTask`、`GraphPublishRecord`、发布批次表或图谱专用抽取任务表。
- 不给发布空间建立版本。
- 不恢复已经删除的旧 `knowledge.graph` interface/application/infra 设计。
- 不实现世系图、Schema 后台管理、正文证据片段定位或发布库全量导入/导出。
- 不把 Repository、Facade、Spring 事务、MyBatis 或 JSON 序列化注入 Entity/领域聚合。
- 不预先建立空转的 `GraphMaterialDomainService`、`GraphPublicationDomainService` 或 `GraphPublishedDomainService`。
- 不在本任务中清理撤回后变成无素材关联的发布对象；撤回只删除当前素材关联。
- 本任务不在 ApplicationService 内重复实现 `module + domain + view/edit` 权限判断；权限由后续 interface 接入时按既定系统权限边界校验。ApplicationService 仍必须校验对象归属、状态和并发版本。

## Execution Baseline

执行前先确认以下基线仍成立；任何一项不成立，先更新本 RUNBOOK 再写代码。

### Existing Domain Objects

领域包根目录为：

```text
kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/
com/thundax/kuzhambu/knowledge/domain/graph
```

已经存在：

- Entity：`GraphMaterial`、`GraphMaterialNode`、`GraphMaterialEdge`、`GraphMaterialVersion`、`GraphMaterialEvent`。
- 发布 Entity：`GraphPublishedNode`、`GraphPublishedEdge`、两类 Property、两类 Material 关联。
- Value Object：全部 Graph ID、`GraphNodeKey`、`GraphEdgeKey`、`GraphPublishedEdgeSlice`。
- Key 实现：`GraphKeyHelper`、`GraphNodeKeyCodec`、`GraphEdgeKeyCodec`。
- Repository 接口及 infra 的 RepositoryImpl、Mapper、DO、`GraphPersistenceAssembler`。
- Schema 表定义：`db/schema/knowledge.sql`。

当前 Entity 行为不足：`GraphMaterial` 只有 `editable()`、`markReady()`、`markDraft()`、`publish()`、`withdraw()`；节点、边、发布对象和事件主要还是数据载体。当前 Repository 也缺少分页、CAS、删除、批量持久化和统计能力。

### Fixed Business Rules

- `GraphMaterial` 以 `ContentRef(contentType, contentId)` 为业务身份；首期只接受 `SANCAI_ENTRY`。
- 一份素材只有一张当前草稿图；只有 `DRAFT`、`READY` 可编辑、抽取、导入和恢复版本。
- 草稿存在至少一个节点或边时状态为 `READY`，空图为 `DRAFT`；边不得脱离节点存在。
- `PUBLISHED` 素材冻结；编辑或重新抽取前必须整体撤回。
- 每次成功发布创建一个不可变 `GraphMaterialVersion(snapshotJson)`；切换版本只发生在可编辑状态。
- 发布库是另一套可变 `GraphPublishedNode`、`GraphPublishedEdge`，没有版本。
- 发布时节点先于边；同 Key 创建或复用；边的两端必须先解析为发布节点。
- 发布对象和素材通过 `GraphPublishedNodeMaterial`、`GraphPublishedEdgeMaterial` 关联。
- 撤回只删除当前素材的发布关联，不删除发布对象，不回写草稿图。
- 发布空间人工删除使用 `GraphPublishedStatus.DELETED` 软删除；属性和素材映射保留用于历史追溯。Portal、Workbench 默认只读取 `ACTIVE`。
- 多值属性保留全部事实值；同一对象同一属性最多一个 `preferred=true`。
- 发布预览只读；确认时重新读取、重新匹配、重新校验，不使用 preview token/fingerprint。
- 批量发布是多份单素材发布的协调，每份素材独立成功或失败，不形成跨素材事务。
- 删除素材只处理 `DELETED` 事件，状态为 `SCHEDULED → PROCESSING → SUCCEEDED/FAILED`。
- Workbench 种子节点按 `modified_at DESC, id DESC` 取最多 100 个；边按游标渐进读取；最终展示去除孤立节点，节点总量上限 200。

### Current Cross-domain Contracts

`ClassicsFacade` 已有：

```java
ClassicsPublicContentFacadeResponse getWorkbenchContent(ClassicsPublicContentFacadeRequest request);
ClassicsPublicContentFacadeResponse getPublicContent(ClassicsPublicContentFacadeRequest request);
```

其中 `ClassicsPublicContentFacadeDto` 已提供 `contentType`、字符串 `contentId`、`title`、`summary`、`textSegments`、`status`、`visibility`、版本和时间字段。Knowledge 不得依赖 Classics application/domain/repository。

`AiFacade` 已有任务查询和 Candidate 操作：

```java
AiBatchJobFacadeResponse getLatestBatchJob(AiBatchJobQueryFacadeRequest request);
AiBatchJobPageFacadeResponse pageBatchJobs(AiBatchJobQueryFacadeRequest request);
AiCandidateFacadeDto requirePendingCandidate(RequirePendingAiCandidateFacadeRequest request);
AiCandidateFacadeDto markCandidateApplied(MarkAiCandidateAppliedFacadeRequest request);
```

但当前 `createBatchJob(CreateAiBatchJobFacadeRequest)` 只登记任务元数据，`CreateAiBatchJobFacadeRequest` 没有 `contentId` 和输入快照，也不负责执行图谱抽取。因此不能直接用它实现 `startExtraction()`；必须先执行 Phase 1 的 AI 契约补齐。

## Execution Protocol

- Phase 0-12 按顺序执行；只有前一 Phase 的完成条件和测试满足后才进入下一 Phase。
- 每个 Phase 开始前运行 `git status --short`，保留用户已有改动，不修改无关文件。
- 每个 Phase 结束后先格式化、运行该 Phase 的最窄测试、检查 diff，再按 Commit Plan 提交。
- 执行过程中在下表记录 commit；不要只勾选状态而不附验证证据。

| Phase | Status | Commit | Verification |
| --- | --- | --- | --- |
| 0 Contract Freeze | Pending | - | - |
| 1 AI Extraction Contract | Pending | - | - |
| 2 Entity Behavior | Pending | - | - |
| 3 Aggregates | Pending | - | - |
| 4 Repository/Infra | Pending | - | - |
| 5 Application Support | Pending | - | - |
| 6 Material ApplicationService | Pending | - | - |
| 7 Extraction ApplicationService | Pending | - | - |
| 8 Publication ApplicationService | Pending | - | - |
| 9 Published Governance | Pending | - | - |
| 10 Material Events | Pending | - | - |
| 11 Portal/Workbench Reads | Pending | - | - |
| 12 Cross-cutting Closure | Pending | - | - |

- Stop Condition 出现时把当前 Phase 标为 `Blocked`，写明阻塞文件和契约，不自行扩展需求。
- 一个 Phase 内出现新的架构决定时，先更新正式设计，再更新本 RUNBOOK 的具体步骤，然后继续代码。

## Design Rules

### Layer Ownership

```text
ApplicationServiceImpl
  ├─ 校验 Command / Query 和权限前置条件
  ├─ 调用本域 Repository、AiFacade、ClassicsFacade
  ├─ 加载完整且有限的领域对象集合
  ├─ 调用 Entity / Aggregate / Operation Model
  ├─ 控制事务和保存顺序
  └─ 通过 application assembler 组装 Result

Entity / Aggregate / Operation Model
  ├─ 状态流转和业务不变量
  ├─ 素材归属、端点和 Key 校验
  ├─ 合并、拆分、删除、发布的内存变换
  └─ 产出待创建、待更新、待删除集合

Repository / Infra
  ├─ 查询、分页、计数和稳定游标
  ├─ 唯一约束和数据库 CAS
  ├─ 批量写入、迁移和删除
  └─ Entity / DO 转换
```

### Transaction Rules

- 所有 Impl 类使用 `@Service`、类级 `@Transactional(readOnly = true)`、`@BizExceptionBoundary`。
- 所有写方法使用 `@Transactional(rollbackFor = Exception.class)`。
- 单素材发布、撤回、版本恢复、导入、草稿合并/拆分、发布治理合并/拆分/删除分别在一个事务中完成。
- 批量发布必须逐素材开启独立事务；禁止通过同类 `publish()` 自调用期待 Spring 代理生效。
- 新建 `GraphPublicationExecutor`，其 `publishOne()` 是 `REQUIRES_NEW` 单素材事务入口；批量 ApplicationService 循环调用该 Bean。
- AI 外部调用不放入持有数据库写锁的事务。读取素材快照后调用 AI Facade；应用 Candidate 时再开启本地短事务。
- `GraphExtractionApplicationServiceImpl` 的提交/重试/应用编排方法和 `GraphMaterialEventApplicationServiceImpl.processEvent()` 使用 `Propagation.NOT_SUPPORTED`，由下游 Executor 开启短事务，避免加入类级 read-only 事务。

### CAS Rule

以下对象写入时必须执行数据库 compare-and-set：

以 Material 为例，CAS 不是单独“加版本”，而是在同一 SQL 中写业务字段并递增版本：

```sql
UPDATE knowledge_graph_material
SET content_title_snapshot = ?,
    status = ?,
    published_at = ?,
    lock_version = lock_version + 1
WHERE content_type = ?
  AND content_ref_id = ?
  AND lock_version = ?;
```

更新行数不是 `1` 时抛出 `BizException("数据已被其他操作修改，请刷新后重试")`。不得只在 Java 中比较版本后调用无条件 `update`。

## Plan

### Phase 0: Preflight and Contract Freeze

1. 运行并保存基线：

   ```sh
   git status --short
   cd kuzhambu-servers
   mvn -pl biz/knowledge/kuzhambu-knowledge-application,biz/knowledge/kuzhambu-knowledge-infra -am test
   ```

2. 阅读并核对七个现有 service 接口下的全部方法，禁止在实现过程中静默删除用例。
3. 核对 `db/schema/knowledge.sql` 与全部 Graph DO 字段一致。
4. 核对 `KNOWLEDGE-GRAPH-SCHEMA.json` 可以被 Jackson 读取，并记录当前 Schema 校验入口尚未实现。
5. 确认工作树中的用户改动；格式化后只能保留本任务文件。

在开始 Impl 前先修改三个现有 command、一个 publication result 和一个 Workbench 返回契约，消除执行者身份、批量失败和边端点节点缺口：

```java
// $KG_APP_JAVA/application/graph/command/GraphExtractionCommand.java
public record GraphExtractionCommand(ContentRef materialRef, Long requestedBy) {}

// $KG_APP_JAVA/application/graph/command/GraphExtractionRetryCommand.java
public record GraphExtractionRetryCommand(
        ContentRef materialRef, Long failedBatchJobId, Long requestedBy) {}

// $KG_APP_JAVA/application/graph/command/GraphPublicationCommand.java
public record GraphPublicationCommand(
        ContentRef materialRef, long materialLockVersion, Long publishedBy) {}

// $KG_APP_JAVA/application/graph/result/GraphPublicationResult.java
public record GraphPublicationResult(
        ContentRef materialRef,
        GraphMaterialStatus materialStatus,
        boolean success,
        String failureMessage,
        int createdNodeCount,
        int reusedNodeCount,
        int createdEdgeCount,
        int reusedEdgeCount,
        List<GraphValidationIssueResult> issues) {}

// 新建 $KG_APP_JAVA/application/graph/result/GraphIncidentEdgesResult.java
public record GraphIncidentEdgesResult(
        List<GraphPublishedNode> nodes,
        List<GraphPublishedEdge> edges,
        GraphPublishedEdgeId nextCursor,
        boolean truncated) {}

// 修改 $KG_APP_JAVA/application/graph/service/GraphWorkbenchApplicationService.java
GraphIncidentEdgesResult listIncidentEdges(
        GraphIncidentEdgesQuery query, PageQuery pageQuery);
```

删除该接口对 `GraphPublishedEdgeSlice` 的 import，但保留 `GraphPublishedEdgeSlice` 作为 Repository 内部游标读取值对象。后续 interface assembler 从当前登录上下文写入 `requestedBy/publishedBy`；application 不直接依赖 `KuzhambuContextHolder`。这不是恢复 `GraphPublicationOutcome`，只是让现有批量返回可以逐素材表达失败。

Phase 0 完成条件：当前分支能编译；已知失败被记录；接口、表和 Entity 基线与本文一致。

### Phase 1: Complete AI Extraction Submission Contract

目标：Knowledge 能提交一个绑定 `ContentRef` 和输入快照、随后真正执行的 `AiBatchJob`，而不是只创建空任务记录。

#### 1.1 Modify AI Facade

修改：

```text
kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/
com/thundax/kuzhambu/ai/facade/AiFacade.java
```

新增方法：

```java
AiBatchJobActionFacadeResponse submitKnowledgeGraphExtraction(
        KnowledgeGraphExtractionJobFacadeRequest request);
```

新建：

```text
$AI_FACADE_JAVA/request/KnowledgeGraphExtractionJobFacadeRequest.java
```

请求必须包含：

```java
String scope;
String contentType;
Long contentId;
String contentTitle;
String contentSnapshotJson;
Long requestedBy;
```

`contentSnapshotJson` 固定保存触发时的标题、摘要和 `textSegments`；AI 域从自己的配置解析模型、Prompt、Schema 和参数，Knowledge 不传模型配置。

修改以下现有文件：

```text
$AI_APP_JAVA/facade/assembler/AiFacadeAssembler.java
$AI_APP_JAVA/facade/impl/AiFacadeImpl.java
```

Assembler 新增 `toKnowledgeGraphExtractionCommand(request)`；FacadeImpl 新增同名方法并委托 Phase 1.2 的 `KnowledgeGraphExtractionTaskApplicationService.submitGraph()`。不得在 FacadeImpl 中直接写 Repository。

#### 1.2 Add AI Application Submission Use Case

现有 `AiRefinementTaskApplicationServiceImpl` 已经实现“提交时固定配置 → 创建 Batch Job → transaction afterCommit 异步执行 → Candidate → Batch Job 成功/失败收口”的模式，但其 capability 白名单和 scope 固定为 Classics refinement。不要扩大该类职责；为图谱抽取新建独立 task service，同时复用相同 Executor 和 Batch Job application service。

修改现有 command：

```text
$AI_APP_JAVA/scenario/command/KnowledgeAiExtractionCommand.java
```

在 record 第一项增加可空 `AiBatchJobId batchId`；同步修改所有构造点。修改 `$AI_APP_JAVA/scenario/service/impl/KnowledgeAiExtractionApplicationServiceImpl.java` 的 `toInvokeCommand()`，把该 batchId 写入 `new AiInvokeContext(command.batchId(), "knowledge", capability)`，确保 InvocationLog 和 Candidate 都关联 Batch Job。

新建：

```text
$AI_APP_JAVA/scenario/support/KnowledgeAiExtractionSnapshot.java
$AI_APP_JAVA/scenario/support/KnowledgeAiExtractionSnapshotResolver.java
$AI_APP_JAVA/scenario/service/KnowledgeGraphExtractionTaskApplicationService.java
$AI_APP_JAVA/scenario/service/impl/KnowledgeGraphExtractionTaskApplicationServiceImpl.java
```

Resolver 方法：

```java
public KnowledgeAiExtractionSnapshot resolve(KnowledgeAiExtractionCommand command);
```

它使用现有 `AiBusinessInvokeConfigResolver` 固定 service、model、prompt version、prompt messages、variables、output schema 和输入 payload，返回 plain snapshot，不构造 application Command。修改 `KnowledgeAiExtractionApplicationServiceImpl` 复用该 Resolver，删除重复的私有 `enrichBusinessInvokeConfig()`。`KnowledgeGraphExtractionTaskApplicationServiceImpl` 根据 snapshot 构造最终 `KnowledgeAiExtractionCommand`；Command 构造仍位于 ApplicationServiceImpl，符合架构门禁。

Task service 接口：

```java
AiBatchJobId submitGraph(KnowledgeAiExtractionCommand command);
```

`KnowledgeGraphExtractionTaskApplicationServiceImpl.submitGraph()` 必须：

1. 校验 taskType 为 `GRAPH`、`sourceContentType/sourceContentId/inputPayloadJson/requestId/traceId` 完整。
2. 检查相同 `AiContentRef + KNOWLEDGE_GRAPH_EXTRACT` 没有 `RUNNING` Job。
3. 调用 Resolver 固定输入和 AI 配置，之后不得重新解析可变配置。
4. 调用 `AiBatchJobApplicationService.create(new AiBatchJobCreateCommand("knowledge", KNOWLEDGE_GRAPH_EXTRACT, contentRef, 1, null))`。
5. 复制 snapshot command 并写入新 `AiBatchJobId`。
6. 注册 `TransactionSynchronization.afterCommit()`，使用现有 `AiRefinementExecutorConfiguration.TASK_EXECUTOR` 执行 `KnowledgeAiExtractionApplicationService.extractGraph(snapshotCommand)`。
7. 成功时 `recordSuccessIfRunning()`；失败时 `recordFailureIfRunning()`。Invocation 层负责生成关联相同 batchId 的 Candidate。
8. 添加与 refinement task 相同的一小时 orphaned RUNNING Job 过期处理，但 scope 固定为 `knowledge`、capability 固定为 `KNOWLEDGE_GRAPH_EXTRACT`。

为保证并发提交时“同一素材同一能力只有一个 RUNNING Job”不是先查后插竞态，修改 `db/schema/ai.sql` 的 `ai_batch_job`：

```sql
`running_content_key` varchar(256)
    GENERATED ALWAYS AS (
        CASE
            WHEN `status` = 'RUNNING' AND `content_id` IS NOT NULL
            THEN concat(
                coalesce(`scope`, ''), ':', `capability`, ':',
                `content_type`, ':', cast(`content_id` AS char))
            ELSE NULL
        END
    ) STORED,
UNIQUE KEY `uk_ai_batch_job_running_content` (`running_content_key`)
```

该列是数据库派生列，不加入 `AiBatchJob`、`AiBatchJobDO` 或 PersistenceAssembler。Task service 保留可读的运行中预检；唯一约束负责关闭并发窗口，Repository/Fascade 把该唯一键冲突转换为“该素材已有运行中的 AI 任务”。

输入和配置快照在 afterCommit 异步命令中保持；进程在执行前崩溃时由 orphaned Job 过期为 FAILED，用户通过 retry 创建新 Job。这个行为与现有 refinement task 一致，不新增 snapshot 表，也不得在 Knowledge 表保存 AI 执行状态或 Prompt 快照。

#### 1.3 Tests

新增或修改：

- `AiFacadeImplTest.submitKnowledgeGraphExtractionDelegatesCompleteSnapshot()`。
- `KnowledgeGraphExtractionTaskApplicationServiceImplTest.submitGraphRejectsRunningDuplicate()`。
- `KnowledgeGraphExtractionTaskApplicationServiceImplTest.submitGraphCapturesSnapshotBeforeAfterCommit()`。
- `KnowledgeGraphExtractionTaskApplicationServiceImplTest.submitGraphLinksInvocationAndCandidateToBatch()`。
- `KnowledgeGraphExtractionTaskApplicationServiceImplTest.orphanedGraphJobBecomesFailed()`。
- 验证成功 Candidate 的 `batchId/contentType/contentId/capability` 可由 Knowledge 查询。

Phase 1 完成条件：给定一份 `SANCAI_ENTRY` 快照，Facade 返回真实 `batchId`，该 Job 可执行并能形成 Candidate。

### Phase 2: Complete Domain Entity Behavior

#### 2.1 Modify GraphMaterial

修改：

```text
$KG_DOMAIN_JAVA/model/entity/GraphMaterial.java
```

保留现有方法并补齐：

```java
public void requireEditable();
public void requireReady();
public void requirePublished();
public void requireLockVersion(long expectedLockVersion);
public void refreshStatus(boolean graphEmpty);
public void publish(Instant completedAt);
public void withdraw(boolean graphEmpty);
```

规则：`requireEditable()` 只接受 `DRAFT/READY`；`publish()` 只接受 `READY`；`withdraw()` 只接受 `PUBLISHED`；`refreshStatus()` 不允许把 `PUBLISHED` 隐式改回草稿状态。

#### 2.2 Modify Material Node and Edge

修改 `GraphMaterialNode.java`，新增：

```java
public void requireMaterial(ContentRef expectedMaterialRef);
public void validateRequiredFields();
public void refreshNodeKey(String identityQualifier);
public boolean sameBusinessKey(GraphMaterialNode other);
```

修改 `GraphMaterialEdge.java`，新增：

```java
public void requireMaterial(ContentRef expectedMaterialRef);
public void validateRequiredFields();
public boolean connects(GraphMaterialNodeId nodeId);
public void replaceEndpoint(GraphMaterialNodeId sourceId, GraphMaterialNodeId targetId);
public void refreshEdgeKey(
        GraphMaterialNode sourceNode,
        GraphMaterialNode targetNode,
        boolean directed,
        Map<String, String> keyQualifiers);
public boolean sameBusinessKey(GraphMaterialEdge other);
```

所有 Key 计算调用 `GraphKeyHelper`，不得在 Entity 内复制字符串拼接规则。

#### 2.3 Modify Published Objects and Event

修改 `GraphPublishedNode.java`、`GraphPublishedEdge.java`，分别新增：

```java
public void requireLockVersion(long expectedLockVersion);
public void touch(Instant modifiedAt);
public void delete(Instant modifiedAt);
public void activate(Instant modifiedAt);
public void refreshNodeKey(String identityQualifier); // GraphPublishedNode
public void refreshEdgeKey(                     // GraphPublishedEdge
        GraphNodeKey sourceNodeKey,
        GraphNodeKey targetNodeKey,
        boolean directed,
        Map<String, String> keyQualifiers);
public void validateRequiredFields();
```

修改 `GraphMaterialEvent.java`，新增：

```java
public void requireLockVersion(long expectedLockVersion);
public void startProcessing();
public void succeed();
public void fail();
public void scheduleRetry();
```

发布对象的 `delete()` 只允许 `ACTIVE → DELETED`，`activate()` 只允许 `DELETED → ACTIVE`，两者都更新 `modifiedAt`；重复删除/恢复返回业务错误，不物理删除属性或素材映射。事件状态规则：只有 `SCHEDULED/FAILED` 可领取，只有 `PROCESSING` 可成功或失败，只有 `FAILED` 可重试。

#### 2.4 Tests

新建：

```text
$KG_DOMAIN_TEST/model/entity/
GraphMaterialTest.java
GraphMaterialNodeTest.java
GraphMaterialEdgeTest.java
GraphPublishedNodeTest.java
GraphPublishedEdgeTest.java
GraphMaterialEventTest.java
```

每个公开领域方法至少覆盖成功路径和一个拒绝路径。

### Phase 3: Add Domain Aggregate and Operation Models

#### 3.1 New GraphMaterialGraph

新建：

```text
$KG_DOMAIN_JAVA/model/aggregate/GraphMaterialGraph.java
```

构造入口：

```java
public static GraphMaterialGraph of(
        GraphMaterial material,
        List<GraphMaterialNode> nodes,
        List<GraphMaterialEdge> edges);
```

公开行为：

```java
public GraphMaterialNode addNode(GraphMaterialNode node);
public void updateNode(GraphMaterialNode node);
public void removeNode(GraphMaterialNodeId nodeId);
public GraphMaterialEdge addEdge(GraphMaterialEdge edge);
public void updateEdge(GraphMaterialEdge edge);
public void removeEdge(GraphMaterialEdgeId edgeId);
public GraphMaterialChangeSet mergeNodes(
        GraphMaterialNodeId retainedNodeId,
        List<GraphMaterialNodeId> mergedNodeIds);
public GraphMaterialChangeSet splitNode(
        GraphMaterialNodeId sourceNodeId,
        GraphMaterialNode splitNode,
        List<GraphMaterialEdgeId> reassignedEdgeIds);
public void validate();
public GraphMaterial material();
public List<GraphMaterialNode> nodes();
public List<GraphMaterialEdge> edges();
```

新建同 package 的 `GraphMaterialChangeSet.java`，只表达本次内存变换产生的 `created/updated/deleted` 节点和边，不持久化。

`GraphMaterialGraph.of()` 必须校验：

- 所有对象 `materialRef` 相同。
- 所有边端点存在。
- 素材内 `nodeKey` 唯一、`edgeKey` 唯一。
- 删除节点时关联边自动进入 deleted 集合。
- 合并/拆分后重新计算受影响边 Key 并去重。
- 每次变换后调用 `material.refreshStatus(nodes.isEmpty())`。
- JSON 文档的 `MERGE/REPLACE` 由 Phase 5 的 `GraphDocumentPlanner` 先解决 String 引用和数据库 ID 映射，再通过此聚合逐项执行；聚合不接收 JSON 协议对象。

#### 3.2 New GraphPublication

新建：

```text
$KG_DOMAIN_JAVA/model/operation/GraphPublication.java
$KG_DOMAIN_JAVA/model/operation/GraphPublicationContext.java
$KG_DOMAIN_JAVA/model/operation/GraphPublicationChangeSet.java
```

`GraphPublication` 只在一次 preview/publish 调用期间存在，不建表。`GraphPublicationContext` 必须持有 `GraphMaterialGraph materialGraph`、按素材节点 ID 索引的已命中发布节点、按素材边 ID 索引的已命中发布边、命中对象现有属性，以及执行发布所需的 `Instant modifiedAt`。Context 不得持有 Repository。

公开行为：

```java
public static GraphPublication plan(GraphPublicationContext context);
public void validateForPublication();
public GraphPublicationChangeSet changes();
public int createdNodeCount();
public int reusedNodeCount();
public int createdEdgeCount();
public int reusedEdgeCount();
```

`GraphPublicationChangeSet` 必须包含：待创建/复用发布节点、待创建/复用发布边、待新增属性、待新增素材关联和 validation issues。边必须通过“素材节点 ID → 发布节点 ID”映射重建，禁止把素材节点 ID 写入发布边。

Key 命中 `DELETED` 发布对象时返回 blocking issue `PUBLISHED_OBJECT_DELETED`，不得由发布动作自动恢复用户已删除对象。用户必须先通过发布空间 update 用例把对象恢复为 `ACTIVE`，再重新预览发布。

#### 3.3 New GraphPublishedSubgraph

新建：

```text
$KG_DOMAIN_JAVA/model/aggregate/GraphPublishedSubgraph.java
$KG_DOMAIN_JAVA/model/aggregate/GraphPublishedChangeSet.java
```

公开行为：

```java
public GraphPublishedChangeSet deleteNode(
        GraphPublishedNodeId nodeId, boolean cascadeEdges, Instant modifiedAt);
public GraphPublishedChangeSet deleteEdge(GraphPublishedEdgeId edgeId, Instant modifiedAt);
public GraphPublishedChangeSet mergeNodes(
        GraphPublishedNodeId retainedNodeId,
        List<GraphPublishedNodeId> mergedNodeIds,
        Instant modifiedAt);
public GraphPublishedChangeSet splitNode(
        GraphPublishedNodeSplitSpec spec,
        Instant modifiedAt);
```

新建 `GraphPublishedNodeSplitSpec.java`，领域层只接收发布对象和 ID/`ContentRef` 分配，不依赖 application command。

#### 3.4 Aggregate Tests

新建：

```text
$KG_DOMAIN_TEST/model/aggregate/
GraphMaterialGraphTest.java
GraphPublishedSubgraphTest.java

$KG_DOMAIN_TEST/model/operation/
GraphPublicationTest.java
```

### Phase 4: Complete Repository Ports and Infra

先修改 domain Repository 接口，再同步每个 RepositoryImpl；ApplicationService 不得临时访问 Mapper。

#### 4.1 Material Repositories

修改 `GraphMaterialRepository.java`：

```java
PageResult<GraphMaterial> page(
        String keyword, GraphMaterialStatus status, int pageNo, int pageSize);
int updateIfLockVersion(GraphMaterial material, long expectedLockVersion);
```

Material 分页按 `published_at DESC`（NULL 最后）、`id DESC` 稳定排序；keyword 匹配 `content_title_snapshot`。

修改 `GraphMaterialNodeRepository.java`：

```java
GraphMaterialNodeId insert(GraphMaterialNode node);
void batchInsert(List<GraphMaterialNode> nodes);
void batchUpdate(List<GraphMaterialNode> nodes);
void deleteByIds(List<GraphMaterialNodeId> ids);
```

将原 `int insert(GraphMaterialNode node)` 改成返回强类型 ID；MyBatis insert 后从 DO 的自增 `id` 转成 `GraphMaterialNodeId`。

所有 Graph 表的 `id` 都是普通数据库物理主键，继续由 MySQL `AUTO_INCREMENT` 发号；不得在 application、domain 或 RepositoryImpl 中为这些 ID 创建 `SnowflakeIdGenerator`。

修改 `GraphMaterialEdgeRepository.java`，增加对称方法并将 insert 返回值改为 `GraphMaterialEdgeId`。

修改 `GraphMaterialVersionRepository.java`：

```java
long maxVersionNo(ContentRef materialRef);
GraphMaterialVersionId insert(GraphMaterialVersion version);
int deleteByMaterial(ContentRef materialRef);
```

#### 4.2 Event Repository

修改 `GraphMaterialEventRepository.java`：

```java
GraphMaterialEvent getByMaterialRefAndType(
        ContentRef materialRef, GraphMaterialEventType type);
PageResult<GraphMaterialEvent> page(
        ContentRef materialRef,
        GraphMaterialEventType type,
        GraphMaterialEventStatus status,
        int pageNo,
        int pageSize);
List<GraphMaterialEvent> listByStatus(GraphMaterialEventStatus status, int limit);
GraphMaterialEventId insert(GraphMaterialEvent event);
int updateIfLockVersion(GraphMaterialEvent event, long expectedLockVersion);
```

删除语义不准确的 `getByMaterialRef()`；同一素材未来可以有不同 event type。

Event 分页和 scheduler 领取都按 `changed_at ASC, id ASC` 处理最早待办；后台列表展示层如需最新优先，由 page 方法按 query 场景固定为 `changed_at DESC, id DESC`，不要复用 scheduler 查询。

#### 4.3 Published Repositories

修改 `GraphPublishedNodeRepository.java`：

```java
PageResult<GraphPublishedNode> page(
        String keyword,
        GraphNodeType nodeType,
        GraphPublishedStatus status,
        GraphSourceType source,
        int pageNo,
        int pageSize);
GraphPublishedNodeId insert(GraphPublishedNode node);
int updateIfLockVersion(GraphPublishedNode node, long expectedLockVersion);
long count(GraphPublishedStatus status);
```

保留 `getByNodeKey()`、`listByIds()`、`listRecentlyUpdated(100)`。

Published node/edge 管理分页统一按 `modified_at DESC, id DESC`；`listRecentlyUpdated()` 只返回 `ACTIVE`。

修改 `GraphPublishedEdgeRepository.java`：

```java
PageResult<GraphPublishedEdge> page(
        String keyword,
        String relationType,
        GraphPublishedStatus status,
        GraphSourceType source,
        int pageNo,
        int pageSize);
GraphPublishedEdgeId insert(GraphPublishedEdge edge);
int updateIfLockVersion(GraphPublishedEdge edge, long expectedLockVersion);
long count(GraphPublishedStatus status);
```

重写现有 `listIncidentEdges(nodeIds, afterEdgeId, limit)`：SQL 必须使用 source/target 索引、`id > afterEdgeId` 或统一确定的游标方向、稳定排序和 `limit + 1`；不得先 `listByNodeIds()` 后 Java 截断。

修改 `GraphPublishedNodePropertyRepository.java`：

```java
GraphPublishedNodePropertyId insert(GraphPublishedNodeProperty property);
void batchInsert(List<GraphPublishedNodeProperty> properties);
int deleteByPublishedNodeId(GraphPublishedNodeId publishedNodeId);
int deleteByPublishedNodeIds(List<GraphPublishedNodeId> publishedNodeIds);
```

修改 `GraphPublishedEdgePropertyRepository.java`：

```java
GraphPublishedEdgePropertyId insert(GraphPublishedEdgeProperty property);
void batchInsert(List<GraphPublishedEdgeProperty> properties);
int deleteByPublishedEdgeId(GraphPublishedEdgeId publishedEdgeId);
int deleteByPublishedEdgeIds(List<GraphPublishedEdgeId> publishedEdgeIds);
```

修改 `GraphPublishedNodeMaterialRepository.java`：

```java
void batchInsert(List<GraphPublishedNodeMaterial> relations);
int deleteByPublishedNodeId(GraphPublishedNodeId publishedNodeId);
int deleteByPublishedNodeIds(List<GraphPublishedNodeId> publishedNodeIds);
long countDistinctMaterials();
```

修改 `GraphPublishedEdgeMaterialRepository.java`：

```java
void batchInsert(List<GraphPublishedEdgeMaterial> relations);
int deleteByPublishedEdgeId(GraphPublishedEdgeId publishedEdgeId);
int deleteByPublishedEdgeIds(List<GraphPublishedEdgeId> publishedEdgeIds);
long countDistinctMaterials();
```

插入属性和素材关联必须依赖现有唯一约束实现幂等；RepositoryImpl 将 duplicate key 转成“已存在”语义时不得吞掉其他数据库异常。

#### 4.4 Workbench Read Repository

新建领域 read model：

```text
$KG_DOMAIN_JAVA/model/readmodel/GraphWorkbenchMetrics.java
$KG_DOMAIN_JAVA/model/readmodel/GraphPublishedSearchHit.java
$KG_DOMAIN_JAVA/model/readmodel/GraphQualitySnapshot.java
```

新建：

```text
$KG_DOMAIN_JAVA/repository/GraphWorkbenchRepository.java
$KG_INFRA_JAVA/repository/impl/GraphWorkbenchRepositoryImpl.java
```

接口方法：

```java
GraphWorkbenchMetrics getOverview();
PageResult<GraphPublishedSearchHit> search(
        String keyword,
        GraphNodeType nodeType,
        String relationType,
        int pageNo,
        int pageSize);
GraphQualitySnapshot getQuality(
        String issueType,
        GraphNodeType nodeType,
        int sampleLimit);
```

`getOverview()` 返回发布节点数、发布边数、覆盖素材数、孤立节点数、核心关系缺失节点数。SQL 可放到新建的 `GraphWorkbenchMapper.java` 及对应 XML；复杂统计不得拼在 ApplicationService 中。

#### 4.5 Mapper and SQL Changes

每个 domain port 必须同步修改下表中的现有文件，不能只改接口：

| Domain Repository | Infra RepositoryImpl | Mapper |
| --- | --- | --- |
| `$KG_DOMAIN_JAVA/repository/GraphMaterialRepository.java` | `$KG_INFRA_JAVA/repository/impl/GraphMaterialRepositoryImpl.java` | `$KG_INFRA_JAVA/persistence/mapper/GraphMaterialMapper.java` |
| `$KG_DOMAIN_JAVA/repository/GraphMaterialNodeRepository.java` | `$KG_INFRA_JAVA/repository/impl/GraphMaterialNodeRepositoryImpl.java` | `$KG_INFRA_JAVA/persistence/mapper/GraphMaterialNodeMapper.java` |
| `$KG_DOMAIN_JAVA/repository/GraphMaterialEdgeRepository.java` | `$KG_INFRA_JAVA/repository/impl/GraphMaterialEdgeRepositoryImpl.java` | `$KG_INFRA_JAVA/persistence/mapper/GraphMaterialEdgeMapper.java` |
| `$KG_DOMAIN_JAVA/repository/GraphMaterialVersionRepository.java` | `$KG_INFRA_JAVA/repository/impl/GraphMaterialVersionRepositoryImpl.java` | `$KG_INFRA_JAVA/persistence/mapper/GraphMaterialVersionMapper.java` |
| `$KG_DOMAIN_JAVA/repository/GraphMaterialEventRepository.java` | `$KG_INFRA_JAVA/repository/impl/GraphMaterialEventRepositoryImpl.java` | `$KG_INFRA_JAVA/persistence/mapper/GraphMaterialEventMapper.java` |
| `$KG_DOMAIN_JAVA/repository/GraphPublishedNodeRepository.java` | `$KG_INFRA_JAVA/repository/impl/GraphPublishedNodeRepositoryImpl.java` | `$KG_INFRA_JAVA/persistence/mapper/GraphPublishedNodeMapper.java` |
| `$KG_DOMAIN_JAVA/repository/GraphPublishedEdgeRepository.java` | `$KG_INFRA_JAVA/repository/impl/GraphPublishedEdgeRepositoryImpl.java` | `$KG_INFRA_JAVA/persistence/mapper/GraphPublishedEdgeMapper.java` |
| `$KG_DOMAIN_JAVA/repository/GraphPublishedNodePropertyRepository.java` | `$KG_INFRA_JAVA/repository/impl/GraphPublishedNodePropertyRepositoryImpl.java` | `$KG_INFRA_JAVA/persistence/mapper/GraphPublishedNodePropertyMapper.java` |
| `$KG_DOMAIN_JAVA/repository/GraphPublishedEdgePropertyRepository.java` | `$KG_INFRA_JAVA/repository/impl/GraphPublishedEdgePropertyRepositoryImpl.java` | `$KG_INFRA_JAVA/persistence/mapper/GraphPublishedEdgePropertyMapper.java` |
| `$KG_DOMAIN_JAVA/repository/GraphPublishedNodeMaterialRepository.java` | `$KG_INFRA_JAVA/repository/impl/GraphPublishedNodeMaterialRepositoryImpl.java` | `$KG_INFRA_JAVA/persistence/mapper/GraphPublishedNodeMaterialMapper.java` |
| `$KG_DOMAIN_JAVA/repository/GraphPublishedEdgeMaterialRepository.java` | `$KG_INFRA_JAVA/repository/impl/GraphPublishedEdgeMaterialRepositoryImpl.java` | `$KG_INFRA_JAVA/persistence/mapper/GraphPublishedEdgeMaterialMapper.java` |

实体字段或 Mapper 参数变化时同步修改 `$KG_INFRA_JAVA/persistence/assembler/GraphPersistenceAssembler.java`；所有 `ContentRef` 和强类型 ID 转换继续调用既有 Codec，禁止恢复通用 `id(Function...)`、`ref(...)`、`enumValue(...)` helper。

为四个 CAS 更新在以下 Mapper 增加带 `@Update` 的明确方法，或在对应 XML 定义 SQL：

- `GraphMaterialMapper.updateIfLockVersion(GraphMaterialDO row, Long expectedLockVersion)`
- `GraphPublishedNodeMapper.updateIfLockVersion(GraphPublishedNodeDO row, Long expectedLockVersion)`
- `GraphPublishedEdgeMapper.updateIfLockVersion(GraphPublishedEdgeDO row, Long expectedLockVersion)`
- `GraphMaterialEventMapper.updateIfLockVersion(GraphMaterialEventDO row, Long expectedLockVersion)`

四条 SQL 的更新列固定为：

- Material：`content_title_snapshot/status/published_at/lock_version`，WHERE 使用 `content_type/content_ref_id/expected lock_version`。
- PublishedNode：`node_key/node_type/name/source/status/modified_at/lock_version`，WHERE 使用 `id/expected lock_version`。
- PublishedEdge：`edge_key/source_published_node_id/target_published_node_id/relation_type/source/qualifiers_json/status/modified_at/lock_version`，WHERE 使用 `id/expected lock_version`。
- MaterialEvent：`status/changed_at/lock_version`，WHERE 使用 `id/expected lock_version`。

每条 SQL 都设置 `lock_version = lock_version + 1`；RepositoryImpl 不得先 select 再调用 MyBatis-Plus `updateById()` 代替 CAS。`GraphMaterialEventClaimExecutor.claim()` 成功后重新 `getById()`，把数据库中的新 lockVersion 传给 cleanup/failure recorder。

核对 `db/schema/knowledge.sql`：

- 保留四张表的 `lock_version bigint NOT NULL DEFAULT 0`。
- 保留节点/边 Key 唯一约束、属性值唯一约束、素材映射复合主键。
- 若 `listIncidentEdges` 的执行计划不能同时利用 source/target 索引，在本文件补充所需索引并同步 dev 环境；不得只在 dev 手改。

#### 4.6 Repository Tests

每个新增端口至少验证：空结果、分页、强类型 ID 回填、批量空列表、CAS 成功、CAS 冲突、唯一约束和删除范围。重点新增：

- `GraphMaterialRepositoryImplTest.updateIfLockVersionUsesContentRefAndExpectedVersion()`
- `GraphPublishedNodeRepositoryImplTest.insertReturnsGeneratedId()`
- `GraphPublishedEdgeRepositoryImplTest.listIncidentEdgesUsesStableCursor()`
- `GraphMaterialEventRepositoryImplTest.claimIsCompareAndSet()`
- `GraphWorkbenchRepositoryImplTest.overviewDoesNotCountUnpublishedObjects()`

Phase 4 完成条件：后续七个 ApplicationServiceImpl 只依赖 Repository/Fascade 即可完成用例。

### Phase 5: Add Application Support and Assemblers

#### 5.0 Package the JSON Schema Validator

仓库当前没有 JSON Schema validator。修改 `kuzhambu-servers/pom.xml`：

```xml
<json-schema-validator.version>2.0.4</json-schema-validator.version>
```

并在 `dependencyManagement` 增加 `com.networknt:json-schema-validator:${json-schema-validator.version}`。选择 2.x 是因为当前工程仍使用 Jackson 2；3.x 已切换 Jackson 3。修改 `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/pom.xml` 增加无版本 dependency。

同一 application POM 使用 `maven-resources-plugin` 的 `copy-resources` execution，在 `process-resources` 阶段把：

```text
${project.basedir}/../../../../docs/20-interfaces/KNOWLEDGE-GRAPH-SCHEMA.json
```

复制到：

```text
${project.build.outputDirectory}/schema/KNOWLEDGE-GRAPH-SCHEMA.json
```

不在 `src/main/resources` 手工维护第二份 JSON。

新建：

```text
$KG_APP_JAVA/application/graph/support/GraphSchemaProvider.java
```

方法：

```java
public JsonNode rawSchema();
public JsonSchema schema();
```

Provider 启动时从 classpath 读取一次并按 Draft 2020-12 编译，失败直接阻止应用启动；不得每次请求重新读取文件。新增 `GraphSchemaProviderTest` 验证 classpath 资源存在、`schemaVersion=1.0.0`，并使用一个合法文档和缺少 edge endpoint 的非法文档验证 validator 生效。

#### 5.1 GraphApplicationAssembler

新建：

```text
$KG_APP_JAVA/application/graph/assembler/GraphApplicationAssembler.java
```

只提供无状态转换：

```java
public static GraphMaterialResult toMaterialResult(GraphMaterialGraph graph);
public static GraphPublishedNodeDetailResult toNodeDetail(
        GraphPublishedNode node,
        List<GraphPublishedNodeProperty> properties,
        List<GraphPublishedNodeMaterial> materials,
        List<GraphPublishedEdge> incidentEdges);
public static GraphPublishedEdgeDetailResult toEdgeDetail(
        GraphPublishedEdge edge,
        GraphPublishedNode sourceNode,
        GraphPublishedNode targetNode,
        List<GraphPublishedEdgeProperty> properties,
        List<GraphPublishedEdgeMaterial> materials);
public static GraphPublicationPreviewResult toPublicationPreview(GraphPublication publication);
public static GraphPublicationResult toPublicationResult(
        ContentRef materialRef,
        GraphMaterialStatus materialStatus,
        GraphPublication publication);
public static GraphExtractionResult toExtractionResult(AiBatchJobFacadeResponse response);
public static GraphSearchResult toSearchResult(GraphPublishedSearchHit hit);
public static GraphIncidentEdgesResult toIncidentEdgesResult(
        List<GraphPublishedNode> nodes, GraphPublishedEdgeSlice edgeSlice);
```

#### 5.2 GraphSnapshotSupport

新建：

```text
$KG_APP_JAVA/application/graph/support/GraphDocument.java
$KG_APP_JAVA/application/graph/support/GraphDocumentNode.java
$KG_APP_JAVA/application/graph/support/GraphDocumentEdge.java
$KG_APP_JAVA/application/graph/support/GraphSnapshotSupport.java
```

三个 `GraphDocument*` 是 JSON Schema 边界的 application plain type，不是对外 DTO，也不进入 Domain。字段严格对应 `KNOWLEDGE-GRAPH-SCHEMA.json`：Document 持有 `schemaVersion/nodes/edges`；Node 的 `id` 是文档内 String 引用；Edge 使用文档内 String source/target 引用。不得把 JSON 的 String `id` 直接构造为数据库 Long ID。

方法：

```java
public GraphDocument parseImport(String graphJson);
public GraphDocument parseCandidate(String resultPayload);
public GraphDocument parseVersion(GraphMaterialVersion version);
public String serialize(GraphMaterialGraph graph);
```

该类注入统一 `ObjectMapper` 和只读 Schema provider。JSON 解析、兼容提取和 Schema 映射留在 application，不进入 Entity。

新建 `$KG_APP_JAVA/application/graph/support/GraphSchemaSupport.java`，集中把 Schema 规则转换成 Key 输入：

```java
public String identityQualifier(String propertiesJson);
public boolean directed(String relationType);
public Map<String, String> keyQualifiers(String relationType, String qualifiersJson);
public Map<String, List<String>> nodePropertyValues(String propertiesJson);
public Map<String, List<String>> edgePropertyValues(String qualifiersJson);
public List<GraphValidationIssueResult> validateLoose(GraphDocument document);
public List<GraphValidationIssueResult> validateForPublication(GraphMaterialGraph graph);
```

所有 ApplicationService、Planner 和聚合调用 Entity 的 `refreshNodeKey/refreshEdgeKey` 前都从该类取得参数；不得相信 command、导入 JSON 或 AI Candidate 自带的 `nodeKey/edgeKey`。

再新建：

```text
$KG_APP_JAVA/application/graph/support/GraphDocumentPlan.java
$KG_APP_JAVA/application/graph/support/GraphDocumentPlanner.java
```

明确方法：

```java
public GraphDocumentPlan plan(
        GraphMaterialGraph current,
        GraphDocument document,
        GraphSourceType source,
        String strategy);
```

`GraphDocumentPlan` 按文档 node String ID 保存以下信息：匹配到的现有素材节点、待创建节点、待更新节点，以及引用文档 node ID 的待创建/更新边规格。Preview 直接使用该 Plan 统计变化；它不得伪造 `GraphMaterialNodeId`。

#### 5.3 GraphMaterialContentResolver

新建：

```text
$KG_APP_JAVA/application/graph/support/GraphMaterialContentResolver.java
$KG_APP_JAVA/application/graph/support/GraphMaterialContentSnapshot.java
```

方法：

```java
public GraphMaterialContentSnapshot resolveWorkbench(ContentRef ref);
public boolean isPortalVisible(ContentRef ref);
```

实现必须调用 `ClassicsFacade.getWorkbenchContent()`/`getPublicContent()`；使用 `ContentRefCodec` 转换 String/Long，不手写 `new ContentRef(type, id)` 边界 helper。非 `SANCAI_ENTRY` 直接抛业务异常。

#### 5.4 GraphMaterialGraphLoader and Saver

新建：

```text
$KG_APP_JAVA/application/graph/support/GraphMaterialGraphLoader.java
$KG_APP_JAVA/application/graph/support/GraphMaterialGraphSaver.java
```

Loader：

```java
public GraphMaterialGraph require(ContentRef materialRef);
public GraphMaterialGraph getOrCreate(
        ContentRef materialRef, String contentTitleSnapshot);
```

Saver：

```java
public void save(GraphMaterialGraph graph, GraphMaterialChangeSet changes, long expectedLockVersion);
public GraphMaterialGraph applyDocument(
        GraphMaterialGraph current,
        GraphDocumentPlan plan,
        long expectedLockVersion);
```

Saver 固定顺序：删除边 → 删除节点 → 新增/更新节点 → 新增/更新边 → CAS 更新 Material。`applyDocument()` 必须先逐个插入新节点并取得真实 `GraphMaterialNodeId`，建立“文档 String node ID → 数据库 GraphMaterialNodeId”映射，再构造和插入边；不得把文档 String ID、数组下标或临时负数写入领域 Entity。它只执行 Plan 和持久化机械顺序，不重新决定 MERGE/REPLACE 业务规则。

### Phase 6: Implement GraphMaterialApplicationServiceImpl

新建 `GraphMaterialApplicationServiceImpl.java`，注入：

- `GraphMaterialRepository`
- `GraphMaterialNodeRepository`
- `GraphMaterialEdgeRepository`
- `GraphMaterialVersionRepository`
- `GraphMaterialGraphLoader`
- `GraphMaterialGraphSaver`
- `GraphSnapshotSupport`

逐方法实现：

| 方法 | 精确执行步骤 |
| --- | --- |
| `pageMaterials()` | 规范化 `PageQuery`；调用 `GraphMaterialRepository.page()`；不得内存分页。 |
| `getMaterialGraph()` | Loader 按 `ContentRef` 加载 Material、全部节点和边；不存在则返回业务错误。 |
| `createNode()` | `requireEditable`、校验 command node 归属、聚合 `addNode`、Repository insert 回填 ID、CAS Material。 |
| `updateNode()` | 加载聚合、校验 ID/归属、聚合更新并重算受影响边 Key、Saver 保存。 |
| `deleteNode()` | 聚合删除节点和关联边，Saver 按边先删。 |
| `createEdge()` | 校验两端存在且同素材，重算 `edgeKey`，insert 返回 ID，CAS Material。 |
| `updateEdge()` | 校验对象归属和端点，重算 Key，保存并 CAS。 |
| `deleteEdge()` | 删除边，刷新状态并 CAS。 |
| `previewNodeMerge()` | 只加载和执行内存 merge，转为 `GraphMaterialChangeImpactResult`，不写库。 |
| `mergeNodes()` | 事务内重新加载、执行 merge、保存 change set。 |
| `previewNodeSplit()` | 返回可迁移关系、校验问题和 executable；不写库。 |
| `splitNode()` | 新节点必须归属当前素材；指定关系必须都连接 source；保存并 CAS。 |
| `listVersions()` | `GraphMaterialVersionRepository.listByMaterial()`，按 `versionNo DESC`。 |
| `restoreVersion()` | `requireEditable`、校验 lockVersion、版本快照解析为 `GraphDocument`、Planner 使用 `REPLACE`、Saver `applyDocument()`，Material 保持 `DRAFT/READY`。 |
| `previewImport()` | 解析为 `GraphDocument`；用 `GraphDocumentPlanner` 按 `MERGE/REPLACE` 计算 Plan；返回计数、issues、importable。 |
| `importGraph()` | 重新解析和规划；事务内调用 `GraphMaterialGraphSaver.applyDocument()`；不可相信前端预览。 |
| `exportGraph()` | serialize 当前单素材草稿图，直接返回 JSON 字符串。 |

新建测试 `GraphMaterialApplicationServiceImplTest.java`，逐一覆盖接口所有方法，特别覆盖 `PUBLISHED` 写入拒绝、跨素材 ID、节点删除级联和 CAS 冲突。

### Phase 7: Implement GraphExtractionApplicationServiceImpl

新建 `GraphExtractionApplicationServiceImpl.java`，注入 `AiFacade`、`GraphMaterialContentResolver`、Loader、Saver、`GraphSnapshotSupport`。

常量固定：

```java
private static final String AI_SCOPE = "KNOWLEDGE_GRAPH";
private static final String AI_CAPABILITY = "KNOWLEDGE_GRAPH_EXTRACT";
```

逐方法实现：

#### `startExtraction(GraphExtractionCommand)`

1. 校验 `materialRef`，通过 resolver 读取并固定素材快照。
2. Loader `getOrCreate()`；已有 Material 时调用 `requireEditable()`。
3. 用 `AiFacade.getLatestBatchJob()` 检查相同 `ContentRef + capability` 的运行中任务。
4. 调用 `AiFacade.submitKnowledgeGraphExtraction()`。
5. 用返回的 `batchId` 查询 Job 并组装 `GraphExtractionResult`。

#### `retryExtraction(GraphExtractionRetryCommand)`

1. `AiFacade.getBatchJob(failedBatchJobId)`。
2. 校验 Job 属于 command 的 `ContentRef`、capability 正确、状态为 `FAILED`。
3. 重新读取当前素材快照并提交新 Job；不得复用或覆盖旧 Job。

#### `getCurrentExtraction()` and `pageExtractionHistory()`

构造 `AiBatchJobQueryFacadeRequest(scope, capability, contentType, contentId, page)`；分别调用 `getLatestBatchJob()` 和 `pageBatchJobs()`。空当前任务返回 `null`；历史页保持 AI 域总数和页码。

#### `applyExtractionResult(GraphExtractionApplyCommand)`

1. 通过 `requirePendingCandidate(candidateId)` 领取 Candidate。
2. 校验 Candidate 的 `contentType/contentId/capability` 与 command 完全一致。
3. `GraphSnapshotSupport.parseCandidate()` 得到 `GraphDocument` 并做宽松结构校验。
4. Executor 事务内重新 Loader，校验 `materialLockVersion` 和 editable，再用 `GraphDocumentPlanner` 以 `AI + MERGE` 重新生成 Plan；不得使用事务外预先计算的 Plan。
5. 调用 `GraphMaterialGraphSaver.applyDocument()`，按 Key 合并追加、先落节点取得真实 ID、再落边。
6. 本地事务提交后调用 `markCandidateApplied()`。
7. 若第 6 步失败，重试必须通过 Key 幂等；不得再次创建重复节点/边。

为避免本类内部事务自调用，新建：

```text
$KG_APP_JAVA/application/graph/support/GraphExtractionApplyExecutor.java
```

其 `@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class) GraphMaterialResult apply(ContentRef materialRef, GraphDocument document, long expectedLockVersion)` 只负责步骤 4-5；ApplicationService 在事务外执行 Candidate 领取和最终标记。

新增 `GraphExtractionApplicationServiceImplTest.java` 和 `GraphExtractionApplyExecutorTest.java`。

### Phase 8: Implement Publication and Withdrawal

#### 8.1 New GraphPublicationExecutor

新建：

```text
$KG_APP_JAVA/application/graph/support/GraphPublicationExecutor.java
```

公开方法：

```java
@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
public GraphPublicationResult publishOne(GraphPublicationCommand command);

@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
public GraphMaterial withdrawOne(GraphWithdrawalCommand command);
```

`publishOne()` 的固定写入顺序：

1. 加载 `GraphMaterialGraph`，`requireReady()`，校验 command lockVersion。
2. 重新执行发布 Schema 硬校验；有 blocking issue 立即失败。
3. 按每个 `nodeKey` 查询发布节点；创建缺失节点并取得真实 `GraphPublishedNodeId`。
4. 建立所有素材节点 ID 到发布节点 ID 的内存映射。
5. 用映射重建发布边端点和 `edgeKey`；按 Key 查询、创建或复用发布边。
6. 幂等合并节点/边多值属性；相同 `propertyName + value` 不重复插入。已有 preferred 时全部新值为非 preferred；没有 preferred 时把素材文档顺序中的第一个值设为 preferred。Schema 标记单值但出现多个事实值时保留全部值并输出冲突 issue，不覆盖旧值。
7. 批量写入 `GraphPublishedNodeMaterial`、`GraphPublishedEdgeMaterial`，`sourceSnapshotJson` 保存本次来源对象快照。
8. `versionNo = versionRepository.maxVersionNo(ref) + 1`；序列化完整素材草稿图，使用 command 的 `publishedBy` 构造并插入 `GraphMaterialVersion`。
9. `material.publish(now)`；`GraphMaterialRepository.updateIfLockVersion(material, expected)`。
10. 返回创建/复用计数和非阻断 issues。

唯一 Key 并发创建时：捕获唯一键冲突后按 Key 重查并转为 reuse；其他 SQL 异常继续抛出并回滚。

`withdrawOne()`：

1. 加载 Material，`requirePublished()`，校验 lockVersion。
2. `GraphPublishedEdgeMaterialRepository.deleteByMaterial(ref)`。
3. `GraphPublishedNodeMaterialRepository.deleteByMaterial(ref)`。
4. 读取草稿节点是否为空，调用 `material.withdraw(graphEmpty)`。
5. CAS 更新 Material；不删除发布对象、属性或版本。

#### 8.2 GraphPublicationApplicationServiceImpl

新建实现并逐方法处理：

- `previewPublication()`：加载草稿图、查询 Key 命中对象、`GraphPublication.plan()`，只组装预览。
- `previewBatchPublication()`：逐 `materialRef` 调用私有只读 preview helper，保持输入顺序。
- `publish()`：委托 `GraphPublicationExecutor.publishOne()`。
- `publishBatch()`：逐个调用 executor；每项独立事务。失败项返回 `success=false`、原 `materialStatus`、`failureMessage` 和 blocking issue；不得让一个异常回滚此前成功项。
- `previewWithdrawal()`：读取当前素材节点/边映射数和受治理修改的发布对象。
- `withdraw()`：委托 executor。

不得恢复已删除的 `GraphPublicationOutcome`、`GraphPublicationObjectDecision`、PlanAction 或 preview fingerprint。

新增：

- `GraphPublicationApplicationServiceImplTest.java`
- `GraphPublicationExecutorTest.java`
- 发布原子性集成测试：任一步失败时节点、边、属性、映射、版本和 Material 状态全部回滚。
- 批量测试：第一项成功、第二项失败时第一项仍提交，第三项继续执行。

### Phase 9: Implement Published Governance

新建 `GraphPublishedApplicationServiceImpl.java`，注入所有 published Repository 和 `Clock`。

逐方法实现：

| 方法 | 执行方式 |
| --- | --- |
| `pageNodes()` / `pageEdges()` | 直接调用 Repository 条件分页。 |
| `getNodeDetail()` | 节点 + 属性 +素材映射 + incident edges。 |
| `getEdgeDetail()` | 边 + 两端节点 + 属性 + 素材映射。 |
| `createNode()` | 校验 ID 为空、`source=MANUAL`、重算 Key、插入节点和属性。 |
| `updateNode()` | 校验 lockVersion；普通字段更新要求对象 ACTIVE；command 明确把 DELETED 改为 ACTIVE 时调用 `activate()`；更新 Key/modifiedAt 后 CAS，属性按差异保存。 |
| `previewNodeDeletion()` | 加载节点、incident edges、节点/边素材映射，交给 `GraphPublishedSubgraph` 计算。 |
| `deleteNode()` | 事务内重新预览；不 cascade 且存在 ACTIVE 边则拒绝；cascade 时先把关联边 CAS 为 `DELETED`，再把节点 CAS 为 `DELETED`。属性和素材映射不删。 |
| `createEdge()` | 校验两端存在，`source=MANUAL`，重算 Key，插入边和属性。 |
| `updateEdge()` | 校验端点、lockVersion；普通字段更新要求对象 ACTIVE；command 明确恢复时调用 `activate()`；重算 Key、touch、CAS，保存属性差异。 |
| `previewEdgeDeletion()` | 返回边、属性、素材映射影响。 |
| `deleteEdge()` | 校验 lockVersion、调用领域行为改为 `DELETED`、更新 `modifiedAt`、CAS；属性和素材映射不删。 |
| `previewNodeMerge()` | 加载 retained、merged、全部 incident edges/属性/映射，内存计算迁移和去重。 |
| `mergeNodes()` | 重新加载；向 retained 合并属性和当前映射；重定向边并去重；merged 节点 CAS 为 `DELETED`，原属性和映射保留追溯；touch retained 和受影响边。 |
| `previewNodeSplit()` | 返回必须分配的属性、关系和素材映射。 |
| `splitNode()` | 校验 command 覆盖全部 required 分配；创建 split 节点；移动/复制属性、边、素材映射；touch 两节点和受影响边。 |

合并/拆分不得修改 `GraphMaterialNode`、`GraphMaterialEdge` 或历史 `GraphMaterialVersion`。所有受影响发布对象更新 `modifiedAt`；更新既有对象使用 CAS。

新增 `GraphPublishedApplicationServiceImplTest.java`，覆盖所有 16 个接口方法及删除顺序、合并边去重、拆分完整分配、素材映射迁移和并发冲突。

### Phase 10: Implement Material Event Processing

#### 10.1 Application Service

新建 `GraphMaterialEventApplicationServiceImpl.java`：

- `recordEvent()`：只接受 `DELETED`；按 `ContentRef + type` 查询，已存在则返回原 ID；否则插入 `SCHEDULED`。
- `pageEvents()`：调用 Repository 条件分页。
- `retryEvent()`：加载事件、校验 command lockVersion、`scheduleRetry()`、CAS。
- `processEvent()`：委托独立 executor，避免同类事务调用。

#### 10.2 Processing Executor

新建：

```text
$KG_APP_JAVA/application/graph/support/GraphMaterialEventClaimExecutor.java
$KG_APP_JAVA/application/graph/support/GraphMaterialEventCleanupExecutor.java
$KG_APP_JAVA/application/graph/support/GraphMaterialEventFailureRecorder.java
```

领取方法必须独立提交，使后台和其他实例能观察到 `PROCESSING`：

```java
@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
public GraphMaterialEvent claim(GraphMaterialEventProcessCommand command);
```

`claim()` 加载事件、校验 command lockVersion，调用 `startProcessing()`，再执行 `updateIfLockVersion()`。CAS 失败表示其他实例已经领取，当前调用直接结束，不做清理。

清理方法：

```java
@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
public GraphMaterialEvent cleanup(GraphMaterialEventId eventId, long processingLockVersion);
```

`cleanup()` 固定步骤：

1. 重新加载事件，要求状态为 `PROCESSING` 且 lockVersion 等于 `processingLockVersion`。
2. 删除 `GraphPublishedEdgeMaterial` 当前素材关联。
3. 删除 `GraphPublishedNodeMaterial` 当前素材关联。
4. 删除素材边。
5. 删除素材节点。
6. 删除素材版本。
7. 删除 `GraphMaterial`。
8. `event.succeed()` 并用 `processingLockVersion` CAS 更新事件。

任一清理操作删除 0 行都视为幂等成功。异常时 cleanup 事务回滚；ApplicationService 捕获异常后调用：

```java
@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
public GraphMaterialEvent recordFailure(
        GraphMaterialEventId eventId,
        long processingLockVersion,
        Throwable cause);
```

`recordFailure()` 重新加载 `PROCESSING` 事件、调用 `fail()`、CAS 为 `FAILED` 并写系统错误日志；不得把 failure reason 加到 Graph 表。`processEvent()` 的完整顺序固定为 `claim → cleanup`，catch 后 `recordFailure → rethrow/返回 FAILED`，不得把领取和清理放在同一事务，否则失败回滚会把领取状态也回滚。

#### 10.3 Background Trigger

新建：

```text
$KG_APP_JAVA/application/graph/scheduler/GraphMaterialEventScheduler.java
```

方法：

```java
@Scheduled(fixedDelayString = "${kuzhambu.knowledge.graph.material-event-fixed-delay:30s}")
public void processScheduledEvents();
```

每轮调用 `GraphMaterialEventRepository.listByStatus(SCHEDULED, 20)`，逐条调用 `GraphMaterialEventApplicationService.processEvent(new GraphMaterialEventProcessCommand(id, lockVersion))`。单条失败只记录日志，不停止其余事件；多实例依赖 claim CAS 竞争，不使用 JVM 本地锁。

新增 `GraphMaterialEventApplicationServiceImplTest.java`、`GraphMaterialEventClaimExecutorTest.java`、`GraphMaterialEventCleanupExecutorTest.java`、`GraphMaterialEventSchedulerTest.java`，覆盖重复投递、重复处理、失败重试、单条失败继续和并发领取。

### Phase 11: Implement Portal and Workbench Reads

#### 11.1 GraphPortalApplicationServiceImpl

新建实现，`getMaterialGraph(GraphMaterialQuery query)`：

1. 校验 `ContentRef`。
2. `GraphMaterialContentResolver.isPortalVisible(ref)`；不可见返回 `visible=false` 和空列表。
3. 查询 `GraphMaterial`；不存在或状态不是 `PUBLISHED` 返回空图。
4. 从 node/edge Material Repository 按素材取得发布 ID。
5. 批量读取发布节点和边；过滤非有效状态对象。
6. 返回 `GraphPublishedGraphResult(ref, true, nodes, edges)`。

禁止回退读取素材草稿节点/边；浏览接口不得触发 AI。

#### 11.2 GraphWorkbenchApplicationServiceImpl

新建实现：

- `getOverview()`：`GraphWorkbenchRepository.getOverview()` 转 Result。
- `listRecentSeedNodes()`：`GraphPublishedNodeRepository.listRecentlyUpdated(100)`。
- `listIncidentEdges()`：规范化 page size；调用 Edge Repository 得到 `GraphPublishedEdgeSlice`，汇总 source/target IDs，批量调用 Node Repository 补齐另一端 ACTIVE 节点，组装 `GraphIncidentEdgesResult`；限制最终局部图节点最多 200。
- `search()`：委托 `GraphWorkbenchRepository.search()`，Assembler 转换，不做内存分页。
- `getQuality()`：只接受孤立节点和核心关系缺失两类，委托 read Repository。

新增 `GraphPortalApplicationServiceImplTest.java`、`GraphWorkbenchApplicationServiceImplTest.java`。测试种子数量 100、边 `limit + 1` 截断、补齐另一端节点、空图去除孤立节点和 200 节点上限。

### Phase 12: Cross-cutting Validation and Documentation Sync

1. 检查七个接口的每个方法都被对应 Impl 覆盖：

   ```sh
   rg -n '^\s*(public )?.*\);' \
     kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/\
com/thundax/kuzhambu/knowledge/application/graph/service
   ```

2. 搜索禁止残留：

   ```sh
   rg -n 'GraphPublishTask|GraphPublishRecord|GraphPreview|GraphPublicationOutcome|GraphPublicationPlanAction' \
     kuzhambu-servers/biz/knowledge docs db
   ```

3. 检查 ApplicationServiceImpl 不导入 infra：

   ```sh
   rg -n 'knowledge\.infra|persistence\.(mapper|dataobject)' \
     kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/\
com/thundax/kuzhambu/knowledge/application/graph
   ```

4. 检查 Domain 不依赖 Spring/Jackson/Facade/Repository 实现。
5. 将最终聚合边界、AI 提交契约、事务行为、Repository 语义同步到 `KNOWLEDGE-GRAPH-DESIGN.md`。
6. AI Facade 契约变化同步到 `docs/20-interfaces/` 中对应 AI 接口文档。
7. 如 SQL 发生变化，从 `db/schema/knowledge.sql` 同步 dev，禁止把 dev 手工 DDL 当真相源。

## Commit Plan

每个提交必须可独立编译和验证，建议顺序：

1. `Feat(ai): 补齐图谱抽取任务提交契约`
2. `Feat(knowledge): 完善图谱实体业务行为`
3. `Feat(knowledge): 建立素材图与发布治理聚合`
4. `Feat(knowledge): 补齐图谱仓储并发和查询能力`
5. `Feat(knowledge): 建立图谱应用层公共支持`
6. `Feat(knowledge): 实现素材草稿图应用服务`
7. `Feat(knowledge): 实现图谱抽取应用编排`
8. `Feat(knowledge): 实现同步发布和撤回`
9. `Feat(knowledge): 实现发布空间治理`
10. `Feat(knowledge): 实现素材删除事件处理`
11. `Feat(knowledge): 实现门户和工作台图谱查询`
12. `Docs(knowledge): 校准图谱实现设计`

不要把 Repository 补齐、领域模型、AI 跨域契约和七个 Impl 压入一个提交。

## Verification

### Per-phase Commands

修改 Java 后，先对受影响模块执行最窄格式化，再测试：

```sh
cd kuzhambu-servers
mvn -pl biz/knowledge/kuzhambu-knowledge-domain,biz/knowledge/kuzhambu-knowledge-application,biz/knowledge/kuzhambu-knowledge-infra spotless:apply
mvn -pl biz/knowledge/kuzhambu-knowledge-application,biz/knowledge/kuzhambu-knowledge-infra -am test
```

修改 AI 契约后执行：

```sh
cd kuzhambu-servers
mvn -pl biz/ai/kuzhambu-ai-facade,biz/ai/kuzhambu-ai-application spotless:apply
mvn -pl biz/ai/kuzhambu-ai-application,biz/knowledge/kuzhambu-knowledge-application -am test
```

每次提交前：

```sh
cd kuzhambu-servers
mvn spotless:check
mvn checkstyle:check
git diff --check
git status --short
```

阶段收口：

```sh
cd kuzhambu-servers
mvn test
```

### Required Integration Evidence

- 两个并发 Material CAS 更新只有一个成功。
- 两份素材发布相同 Key 时复用同一发布节点/边，并各自建立素材关联。
- 发布任一步失败时，不留下部分节点、边、属性、映射、版本或 `PUBLISHED` 状态。
- 批量发布其中一项失败，不回滚其他成功素材，也不停止后续素材。
- 撤回一份素材后，另一份素材关联和发布对象仍存在。
- Candidate 重复应用不会重复创建草稿节点和边。
- 发布空间合并/拆分不修改草稿图和历史版本。
- Material Event 重复执行与失败重试最终收敛。
- Workbench 的 incident edges 在数据库层稳定分页，没有全表加载。
- Portal 对不可见、未发布、已撤回和已删除素材返回空图。

### Manual Flow

1. 选一份 `SANCAI_ENTRY`，发起抽取；确认 AI Job 可见且携带正确 `ContentRef`。
2. 等待 Candidate，应用后 Material 从 `DRAFT` 变为 `READY`。
3. 手工编辑节点和边，确认 lockVersion 增长。
4. 预览发布，处理 blocking conflict 后同步发布。
5. 确认 Material 为 `PUBLISHED`、新版本产生、草稿被冻结。
6. 用第二份素材发布相同 Key，确认复用发布对象。
7. 在发布空间修改对象，确认草稿不回写且 `modifiedAt` 更新。
8. 撤回第一份素材，确认只删除第一份素材映射。
9. 记录并处理第二份素材的 `DELETED` Event，确认草稿、版本和该素材映射清理。
10. 打开 Workbench，确认最近节点、渐进边、搜索和两类质量指标可读。

## Stop Conditions

遇到以下情况立即停止当前 Phase，不在 ApplicationService 内写临时旁路：

- AI 域不能持久化提交时输入/模型/Prompt/Schema/参数快照，或没有真正执行 Batch Job 的入口。
- AI Candidate 无法按 `candidateId` 返回 payload，或无法证明它属于指定 `ContentRef + batchId + capability`。
- Classics Facade 无法提供确定性的 Workbench 素材标题和正文快照。
- Repository 缺少数据库 CAS、稳定分页、强类型生成 ID、批量删除或事务所需能力。
- `KNOWLEDGE-GRAPH-SCHEMA.json` 与 Java 类型、Key 或发布硬校验冲突。
- 批量发布独立提交与现有返回类型无法同时表达；先调整接口和设计。
- Workbench 的 100 个种子、200 节点上限或游标方向无法由现有接口表达。
- 后台没有消费 `GraphMaterialEvent(SCHEDULED)` 的基础设施入口。
- 实现要求新增 Graph 表、跨域直接写库或改变已确认发布/撤回边界。

停止后先修改对应需求、设计或接口契约，再更新本 RUNBOOK；不得用 TODO、catch-all 或内存补偿掩盖阻塞。

## Closure

本 RUNBOOK 只有在以下条件全部满足后才能删除：

- 七个 `Graph*ApplicationServiceImpl` 已完整实现，接口无空方法或占位返回。
- AI Job 从提交、执行、Candidate 查询到应用形成真实闭环。
- Repository、领域聚合、事务和 CAS 集成测试通过。
- SQL 真相源、dev Schema、设计和 AI 接口文档已经同步。
- 完成证据进入 PR 描述或对应 `docs/40-readiness/` 文档。
- `TODO.md` 不再引用已完成工作。

若只完成部分 Phase，保留本 RUNBOOK，但删除已完成步骤的过程性细节并把长期结论迁入正式设计；不得把 RUNBOOK 当成永久需求文档。
