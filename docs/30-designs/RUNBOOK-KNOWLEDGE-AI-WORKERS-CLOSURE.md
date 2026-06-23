# RUNBOOK: Knowledge -> AI -> Workers 闭环

## Purpose

本 RUNBOOK 用于打通 `Knowledge -> AI -> Workers -> Knowledge` 后端执行链路，覆盖：

- `Knowledge` 发起实体关系抽取、图谱抽取、世系图抽取。
- `AI` 负责模型、提示词、调用记录、候选结果和 workers 编排。
- `Workers` 负责无状态 usecase 执行并返回结构化候选结果。
- `Knowledge` 持有正式实体、关系、图谱版本和世系图结果。

本文档只定义执行单元、数据结构、接口变更和验证方式，不记录判断过程。

## Scope

本轮范围：

- `Knowledge` 后端 application/domain/infra/interface。
- `AI` 后端 domain/application/infra。
- `kuzhambu-workers` 的 AI usecase 执行与契约测试。
- Knowledge Admin 后端接口。
- `kuzhambu-apps/admin-web` 的 Knowledge 抽取任务页面与应用动作。

本轮不包含：

- 标签抽取闭环；标签治理已由 `Classics <-> Knowledge` 链路承接。
- 搜索、问答、图谱质量报告、同义词下游消费。
- workers 回调、任务持久化、正式结果落库。

## Fixed Rules

- 调用方向固定为 `Knowledge -> AI -> Workers`，不得反向穿透。
- `Knowledge` 不直接调用 workers AI 接口。
- `AI` 不把正式图谱、正式实体、正式关系写回 `Knowledge` 表。
- `Workers` 不保存任务状态、候选结果、正式结果或业务审计。
- `Knowledge` 保持正式知识事实拥有方。
- `AI` 保持候选结果和调用记录拥有方。
- workers 必须使用 usecase path，不以 `/internal/ai/invoke` 或 `/internal/ai/stream` 作为长期业务入口。
- 本轮只覆盖三个 usecase：
  - `relation-extraction`
  - `graph-extraction`
  - `lineage-extraction`

## Existing Reuse

以下能力已存在，本轮直接复用，不重复设计：

- `AiWorkerInvocationApplicationService` 与 `WorkerAiHttpClient`
- workers `ai_usecase_routes.py`
- workers `usecase_registry.py` 中 Knowledge 三个 usecase path
- workers HMAC、安全路径 allowlist 与 OpenAPI 暴露
- `ai_call_record`
- `ai_candidate`

现有文件锚点：

- [AiWorkerInvocationApplicationService.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/service/AiWorkerInvocationApplicationService.java:1)
- [AiWorkerInvocationApplicationServiceImpl.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/service/impl/AiWorkerInvocationApplicationServiceImpl.java:1)
- [WorkerAiHttpClient.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/client/WorkerAiHttpClient.java:1)
- [ai_usecase_routes.py](/Volumes/storage/workspace/kuzhambu/kuzhambu-workers/src/kuzhambu_workers/api/ai_usecase_routes.py:1)
- [usecase_registry.py](/Volumes/storage/workspace/kuzhambu/kuzhambu-workers/src/kuzhambu_workers/ai/usecase_registry.py:1)

## Target Result

完成后必须达到：

1. `Knowledge` 能创建抽取任务，并区分 `RELATION`、`GRAPH`、`LINEAGE` 三类。
2. `Knowledge` 能通过稳定 AI 协作语义发起抽取，不直接依赖 workers。
3. `AI` 能按 Knowledge usecase 选择稳定 `operation + workerPath + capability`。
4. `AI` 能为 Knowledge 抽取写入 `ai_call_record` 和 `ai_candidate`。
5. `Knowledge` 能查询任务状态，并拿到 `aiCallId`、`aiCandidateId`、失败原因和时间戳。
6. `Knowledge` 能对候选结果执行“应用”，并写入正式知识表。
7. 应用正式结果时，能生成或更新图谱版本关联信息。
8. Admin Web 能创建三类抽取任务、查看任务明细并触发应用动作。

## Data Structure Changes

### Knowledge

新增表：

- `knowledge_graph_extraction_task`

字段固定为：

- `id`
- `task_id`
- `task_type`
- `scope_type`
- `scope_json`
- `source_content_type`
- `source_content_id`
- `ai_call_id`
- `ai_candidate_id`
- `status`
- `error_type`
- `error_message`
- `requested_by`
- `requested_at`
- `completed_at`
- `applied_at`

字段说明：

- `task_type` 取值固定：`RELATION`、`GRAPH`、`LINEAGE`
- `status` 取值固定：`REQUESTED`、`SUCCEEDED`、`FAILED`、`APPLIED`
- `scope_json` 保存本次抽取的范围快照，不回查前端筛选状态

正式结果写入沿用现有 Knowledge 正式表，不新增候选表：

- `knowledge_entity`
- `knowledge_relation`
- `knowledge_graph_version`
- `knowledge_lineage_node`
- `knowledge_lineage_relation`

### AI

本轮不新增 `ai_*` 表。

直接复用：

- `ai_call_record`
- `ai_candidate`

说明：

- `ai_candidate.result_payload` 作为 Knowledge 抽取候选快照真相源。
- `Knowledge` 只保存任务关联和正式结果，不复制候选 payload。

### Workers

本轮不新增持久化结构。

## Interface Changes

### Knowledge Admin HTTP

新增后端接口：

- `POST /api/knowledge/graph/extraction/request-relation`
- `POST /api/knowledge/graph/extraction/request-graph`
- `POST /api/knowledge/graph/extraction/request-lineage`
- `GET /api/knowledge/graph/extraction/task/detail`
- `POST /api/knowledge/graph/extraction/task/apply`

接口职责：

- `request-*`：创建任务并触发 AI 抽取
- `task/detail`：查看任务状态和关联 AI 候选
- `task/apply`：应用候选结果到正式 Knowledge 表

### Admin Web

新增页面能力：

- Knowledge 抽取任务列表
- 抽取任务创建入口
- 抽取任务详情抽屉
- 应用候选结果动作

前端固定落点：

- `src/pages/knowledge/graph-extraction/graph-extraction-page.tsx`
- `src/pages/knowledge/graph-extraction/graph-extraction-service.ts`
- `src/pages/knowledge/graph-extraction/graph-extraction-types.ts`
- `src/pages/knowledge/graph-extraction/components/*`

### Knowledge -> AI

新增稳定跨域语义，不直接依赖 AI application 入口类：

- `KnowledgeAiExtractionDomainService`

必须提供：

- `extractRelations`
- `extractGraph`
- `extractLineage`

### AI -> Workers

固定使用以下 usecase path：

- `/internal/ai/knowledge/relation-extraction`
- `/internal/ai/knowledge/graph-extraction`
- `/internal/ai/knowledge/lineage-extraction`

## Operation Units

以下单元是本 RUNBOOK 的最小执行颗粒度。每个单元控制在 `2-5` 个文件。

| ID | Result | Files | Notes |
| --- | --- | --- | --- |
| `kaw-01-task-domain` | 定义 Knowledge 抽取任务领域模型 | `knowledge-domain/.../model/entity/GraphExtractionTask.java`、`knowledge-domain/.../model/valueobject/GraphExtractionTaskId.java`、`knowledge-domain/.../repository/GraphExtractionTaskRepository.java` | 只承载任务与 AI 关联，不承载正式图谱结果 |
| `kaw-02-task-persistence` | 落地任务持久化 | `knowledge-infra/.../persistence/dataobject/GraphExtractionTaskDO.java`、`knowledge-infra/.../persistence/mapper/GraphExtractionTaskMapper.java`、`knowledge-infra/.../repository/impl/GraphExtractionTaskRepositoryImpl.java`、`knowledge-infra/.../persistence/assembler/KnowledgeGraphPersistenceAssembler.java` | SQL 与 mapper 同步 |
| `kaw-03-task-service-contract` | 定义 Knowledge 抽取用例契约 | `knowledge-application/.../service/KnowledgeGraphExtractionApplicationService.java`、`.../command/RequestRelationExtractionCommand.java`、`.../command/RequestGraphExtractionCommand.java`、`.../command/RequestLineageExtractionCommand.java`、`.../result/GraphExtractionTaskResult.java` | 只定义用例输入输出 |
| `kaw-04-task-service-impl` | Knowledge 能发起三类抽取任务 | `knowledge-application/.../service/impl/KnowledgeGraphExtractionApplicationServiceImpl.java`、`knowledge-application/pom.xml`、`knowledge-application/src/test/.../KnowledgeGraphExtractionApplicationServiceTest.java` | 由 application 编排权限后调用 AI 协作语义 |
| `kaw-05-task-admin-api` | 暴露 Knowledge 抽取后端接口 | `knowledge-interface/.../controller/KnowledgeGraphExtractionController.java`、`.../controller/request/GraphExtractionRequests.java`、`.../controller/response/GraphExtractionResponses.java`、`.../assembler/KnowledgeGraphExtractionInterfaceAssembler.java` | 不涉及 admin-web 页面 |
| `kaw-06-ai-domain-contract` | 定义 Knowledge 调 AI 的稳定跨域语义 | `ai-domain/.../service/KnowledgeAiExtractionDomainService.java`、`ai-domain/.../model/valueobject/KnowledgeAiExtractionRequest.java`、`ai-domain/.../model/valueobject/KnowledgeAiExtractionResult.java` | `Knowledge` 只依赖此契约 |
| `kaw-07-ai-usecase-resolver` | AI 能解析 Knowledge usecase 到 `operation + workerPath + capability` | `ai-application/.../support/KnowledgeAiWorkerUsecaseSpec.java`、`ai-application/.../support/KnowledgeAiWorkerUsecaseResolver.java`、`ai-application/src/test/.../KnowledgeAiWorkerUsecaseResolverTest.java` | 模式对齐 `ClassicsAiWorkerUsecaseResolver` |
| `kaw-08-ai-service-impl` | AI 实现 Knowledge 抽取协作语义 | `ai-application/.../service/impl/KnowledgeAiExtractionDomainServiceImpl.java`、`ai-application/.../service/impl/AiWorkerInvocationApplicationServiceImpl.java`、`ai-application/src/test/.../KnowledgeAiExtractionDomainServiceImplTest.java` | 负责 prompt/model/invoke/candidate/call record |
| `kaw-09-workers-contract` | workers 固化 Knowledge 三个 usecase 的结构化输出契约 | `kuzhambu_workers/schemas/ai.py`、`kuzhambu_workers/ai/graphs/basic.py`、`tests/test_ai_usecase_routes_knowledge.py`、`tests/test_graph_registry.py` | 输出必须稳定为可入 `ai_candidate.result_payload` 的结构 |
| `kaw-10-task-detail-sync` | Knowledge 任务状态能回填 AI 调用结果 | `knowledge-application/.../service/impl/KnowledgeGraphExtractionApplicationServiceImpl.java`、`knowledge-domain/.../repository/GraphExtractionTaskRepository.java`、`knowledge-infra/.../repository/impl/GraphExtractionTaskRepositoryImpl.java`、`ai-domain/.../repository/AiInvocationRepository.java` | 通过 `ai_call_id` / `ai_candidate_id` 查询，不回查 workers |
| `kaw-11-apply-formal-results` | Knowledge 能应用候选结果到正式表 | `knowledge-application/.../support/KnowledgeGraphCandidateApplySupport.java`、`knowledge-application/.../service/impl/KnowledgeGraphExtractionApplicationServiceImpl.java`、`knowledge-application/src/test/.../KnowledgeGraphCandidateApplySupportTest.java` | 应用时只写 Knowledge 正式表 |
| `kaw-12-version-link` | 应用正式结果时生成图谱版本关联 | `knowledge-domain/.../repository/GraphVersionRepository.java`、`knowledge-infra/.../repository/impl/GraphVersionRepositoryImpl.java`、`knowledge-application/.../support/KnowledgeGraphCandidateApplySupport.java`、`knowledge-infra/src/test/.../GraphVersionRepositoryTest.java` | 版本归 Knowledge 拥有 |
| `kaw-13-admin-service-contract` | 前端接通 Knowledge 抽取接口契约 | `admin-web/src/pages/knowledge/graph-extraction/graph-extraction-service.ts`、`graph-extraction-types.ts`、`graph-extraction-service.test.ts` | 只定义 request/response 与页面类型 |
| `kaw-14-admin-page-shell` | 搭建任务页骨架和路由入口 | `graph-extraction-page.tsx`、`graph-extraction-page.css`、`src/router/...`、页面级测试文件 | 只搭建页面骨架，不塞复杂逻辑 |
| `kaw-15-admin-create-actions` | 前端支持三类抽取任务创建 | `components/graph-extraction-create.tsx`、`graph-extraction-page.tsx`、`graph-extraction-service.ts`、组件测试 | 创建动作分 relation/graph/lineage 三个入口 |
| `kaw-16-admin-detail-apply` | 前端支持详情查看和应用动作 | `components/graph-extraction-task-table.tsx`、`components/graph-extraction-task-detail.tsx`、`graph-extraction-page.tsx`、组件测试 | 详情展示 `aiCallId`、`aiCandidateId`、错误信息、时间戳 |
| `kaw-17-docs-readiness` | 同步设计和 readiness 文档 | `docs/30-designs/KNOWLEDGE-DESIGN.md`、`docs/30-designs/AI-DESIGN.md`、`docs/30-designs/WORKERS-DESIGN.md`、`docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md` | 只写已落地结果 |
| `kaw-18-cleanup` | 清理现场 | 删除本 RUNBOOK，清空 `TODO.md` 剩余项或收窄为下一阶段 | PR 前执行 |

## Workers Output Contract

Knowledge 三个 usecase 的 `result_payload` 固定为结构化 JSON：

- `relation-extraction`
  - `entities`
  - `relations`
  - `sourceSnippets`
  - `warnings`
- `graph-extraction`
  - `entities`
  - `relations`
  - `entryRefs`
  - `warnings`
- `lineage-extraction`
  - `nodes`
  - `relations`
  - `sourceSnippets`
  - `warnings`

要求：

- `Workers` 只返回候选结构，不返回正式表主键。
- `Knowledge` 应用时自行生成正式实体、关系和图谱版本主键。
- `Workers` 输出字段名必须稳定，禁止依赖模型自然语言解释作为解析依据。

## Verification

### Knowledge / AI Servers

每个执行单元完成后至少执行相关模块：

```sh
cd kuzhambu-servers
mvn -pl biz/knowledge/kuzhambu-knowledge-application,biz/knowledge/kuzhambu-knowledge-domain,biz/knowledge/kuzhambu-knowledge-infra,biz/knowledge/kuzhambu-knowledge-interface,biz/ai/kuzhambu-ai-application,biz/ai/kuzhambu-ai-domain,biz/ai/kuzhambu-ai-infra -am spotless:apply
```

关键单测与架构测试必须覆盖：

- Knowledge application 触发任务
- Knowledge apply 正式结果
- AI usecase resolver
- AI Knowledge 抽取协作服务
- AI worker invocation 记录 `ai_call_record` 与 `ai_candidate`

PR 前执行：

```sh
cd kuzhambu-servers
mvn -q clean
mvn -q spotless:check
mvn -q checkstyle:check
mvn -q test
```

### Workers

每个 workers 单元完成后执行：

```sh
cd kuzhambu-workers
.venv/bin/python -m ruff format .
.venv/bin/python -m ruff check .
.venv/bin/python -m pytest tests/test_ai_usecase_routes_knowledge.py tests/test_ai_usecase_registry.py tests/test_graph_registry.py -p no:capture
```

PR 前执行：

```sh
cd kuzhambu-workers
.venv/bin/python -m ruff format --check .
.venv/bin/python -m ruff check .
.venv/bin/python -m pytest -p no:capture
```

### Admin Web

每个前端单元完成后执行：

```sh
cd kuzhambu-apps
npm --workspace admin-web run format
npm run format:check
npm run lint
npm --workspace admin-web test -- --runInBand
```

## Exit Criteria

同时满足以下条件才算闭环完成：

1. `Knowledge` 后端可以创建三类抽取任务。
2. 每个任务都能关联到 `ai_call_record` 和 `ai_candidate`。
3. workers 三个 Knowledge usecase path 有稳定 OpenAPI、契约测试和 e2e 路由测试。
4. `Knowledge` 可以应用候选结果到正式表。
5. 图谱版本能与本次应用结果建立关联。
6. Admin Web 能完成创建、查看详情和应用动作。
7. 文档已更新。
8. 本 RUNBOOK 已删除。
