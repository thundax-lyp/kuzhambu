# ArchUnit allowlist 清理 08：Knowledge 图谱、精修与世系

## Purpose

清理 Knowledge 域 `graph`、`refinement` 和 `lineage` 包中已有的 ArchUnit legacy allowlist。每项整改完成后，在同一改动中删除对应 allowance；不得为本任务新增 allowance。

## Scope

本任务只修改下列四个架构测试中的、归属 `graph`、`refinement` 或 `lineage` 的 key。每项 key 的生产代码、直接调用方和对应测试必须在同一提交内保持可编译、行为不变。

| allowlist 文件 | 本次处理内容 | 本次保留内容 |
| --- | --- | --- |
| `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/KnowledgeApplicationCommandQueryRecordAllowances.java` | 4 个 graph Command、1 个 lineage Query、17 个 refinement Command/Query 的 `COMMAND_QUERY_RECORD` key | `portal` 与 `taxonomy` key |
| `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/KnowledgeApplicationArchitectureTest.java` | `KnowledgeGraphExtractionApplicationService`、`KnowledgeGraphRefinementApplicationService`、`KnowledgeQualityReportApplicationService` 的 `METHOD_SHAPE` key | `workbench`、`taxonomy`、`report` key；现有 application assembler nullness allowance |
| `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/test/java/com/thundax/kuzhambu/knowledge/domain/KnowledgeDomainArchitectureTest.java` | `domain.graph.repository` 与 `domain.refinement.repository` 的方法命名 key | `taxonomy` repository key |
| `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/KnowledgeInterfaceArchitectureTest.java` | graph、refinement、lineage 的 Request/Response 注解、Controller 动词和 InterfaceAssembler nullness allowance | `workbench`、`taxonomy`、portal 的 allowance |

### 1. Application Command/Query record

将下列文件改为无 Lombok 注解、只含组件字段的 Java `record`，并删除 `KnowledgeApplicationCommandQueryRecordAllowances.java` 中同名 `COMMAND_QUERY_RECORD` key。

| 子域 | 精确文件 |
| --- | --- |
| graph | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/command/RegenerateGraphExtractionCommand.java`；`RequestGraphExtractionCommand.java`；`RequestLineageExtractionCommand.java`；`RequestRelationExtractionCommand.java` |
| lineage | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/lineage/query/LineageCanvasQuery.java` |
| refinement | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/command/ConfirmRefinementEntityCommand.java`；`ConfirmRefinementLineageNodeCommand.java`；`ConfirmRefinementLineageRelationCommand.java`；`ConfirmRefinementRelationCommand.java`；`DeleteQualityAnnotationCommand.java`；`DeleteRefinementEntityCommand.java`；`DeleteRefinementLineageNodeCommand.java`；`DeleteRefinementLineageRelationCommand.java`；`DeleteRefinementRelationCommand.java`；`GenerateQualityReportCommand.java`；`ReextractLowQualityCategoryCommand.java`；`UpsertQualityAnnotationCommand.java`；`UpsertRefinementEntityCommand.java`；`UpsertRefinementLineageNodeCommand.java`；`UpsertRefinementLineageRelationCommand.java`；`UpsertRefinementRelationCommand.java`；`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/query/RefinementDetailQuery.java` |

> `record` 迁移导致的 accessor 变更必须同步修正所有编译错误；不得保留 bean getter/setter 兼容方法，也不得将调用方移入不允许构造 Command/Query 的层。

### 2. ApplicationService 公开入参

消除下列接口中本任务范围内所有 `METHOD_SHAPE` key：单资源读取使用对应强类型 ID；多条件读取或写入定义单个 `*Query` / `*Command`。分页方法固定为“业务 `*Query` + `PageQuery`”，业务 Query 不携带分页字段。

| 接口文件 | 同步修改的实现文件 | 处理的公开方法 |
| --- | --- | --- |
| `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/service/KnowledgeGraphExtractionApplicationService.java` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/service/impl/KnowledgeGraphExtractionApplicationServiceImpl.java` | `getLineageRelationDetail`、`regenerateTask`、`cancelBatch`、`pageEntities`、`getLineageNodeDetail`、`getEntityDetail`、`pageRelations`、`getRelationDetail`、`pageTasks`、`pageLineageRelations`、`applyTaskCandidate`、`pageLineageNodes`、`pageVersions`、`getVersionDetail` |
| `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/KnowledgeGraphRefinementApplicationService.java` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/impl/KnowledgeGraphRefinementApplicationServiceImpl.java` | `openTask`、`qualitySummary`、`applyTask` |
| `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/KnowledgeQualityReportApplicationService.java` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/impl/KnowledgeQualityReportApplicationServiceImpl.java` | `detail`、`latest` |

接口层必须通过下列 assembler 构造新契约；Controller 只传递契约，不能自行构造 Command/Query。

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/assembler/KnowledgeGraphExtractionInterfaceAssembler.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/assembler/KnowledgeGraphRefinementInterfaceAssembler.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/assembler/KnowledgeQualityReportInterfaceAssembler.java`

### 3. Repository 方法命名

将以下 repository 文件中列出的方法改为仓储动词白名单名称，并同步修改实现、调用方和测试；随后从 `KnowledgeDomainArchitectureTest.java` 删除精确 allowance 字符串。

| 文件 | 待替换方法 |
| --- | --- |
| `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/repository/GraphVersionRepository.java` | `findLatest`、`findLatestAppliedByCategoryCode` |
| `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/repository/KnowledgeEntityRepository.java` | `saveOrUpdateBatch` |
| `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/repository/KnowledgeLineageNodeRepository.java` | `saveOrUpdateBatch` |
| `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/repository/KnowledgeLineageRelationRepository.java` | `saveOrUpdateBatch` |
| `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/repository/KnowledgeRelationRepository.java` | `saveOrUpdateBatch` |
| `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/repository/QualityAnnotationRepository.java` | `saveOrUpdate` |
| `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/repository/QualityReportRepository.java` | `getLatestPublished` |
| `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/repository/RefinementEntityDraftRepository.java` | `saveOrUpdateBatch` |
| `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/repository/RefinementLineageNodeDraftRepository.java` | `saveOrUpdateBatch` |
| `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/repository/RefinementLineageRelationDraftRepository.java` | `saveOrUpdateBatch` |
| `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/repository/RefinementRelationDraftRepository.java` | `saveOrUpdateBatch` |
| `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/repository/RefinementTaskRepository.java` | `findLatestAppliedByGraphVersionId`、`findLatestDraft` |

### 4. Interface 协议模型、Controller 与 Assembler

移除下列文件中 graph、refinement、lineage 协议模型的注解 allowance。先按共享规则补齐 Request/Response 的必要注解；保持现有 JSON 字段名、校验语义和 HTTP 响应不变。

| 子域 | Request 文件 | Response 文件 |
| --- | --- | --- |
| graph | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/controller/request/GraphExtractionRequests.java` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/controller/response/GraphExtractionResponses.java` |
| refinement | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/request/RefinementRequests.java`；`QualityReportRequests.java` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/response/RefinementResponses.java`；`QualityReportResponses.java` |
| lineage | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/lineage/controller/request/LineageCanvasRequest.java` | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/lineage/controller/response/LineageCanvasResponse.java` |

在下列 Controller 中将 action 方法名和 action path 迁移为共享动词白名单允许的形式，更新对应 Controller 测试和前端/API 调用方；迁移不能改变所表达的业务动作。

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/controller/KnowledgeGraphExtractionController.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/KnowledgeGraphRefinementController.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/KnowledgeQualityReportController.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/lineage/controller/KnowledgeLineageController.java`

移除下列 InterfaceAssembler 的 nullness allowance。公开的 Command/Query 转换方法在输入有效时必须返回具体对象；可空输入由 Controller 校验或明确用例分支处理。

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/assembler/KnowledgeGraphExtractionInterfaceAssembler.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/assembler/KnowledgeGraphRefinementInterfaceAssembler.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/assembler/KnowledgeQualityReportInterfaceAssembler.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/lineage/assembler/KnowledgeLineageInterfaceAssembler.java`

## Non-goals

- 不处理 `taxonomy`、`workbench`、`portal`、`report` 或 `facade` 相关的任何 allowance、生产代码或接口契约。
- 不修改 `kuzhambu-common-test` 中的共享 ArchUnit 规则。
- 不借由新增、合并、泛化或放宽 allowance 保持测试通过。
- 不改变图谱抽取、精修、世系浏览或质量报告的业务语义、权限校验、事务边界与持久化数据。

## Plan

1. 以本 RUNBOOK 的四个 allowlist 文件为基线，逐条确认本次范围 key；`rg` 复核 key 不会误命中 non-goal 子域。
2. 先完成第 1 节的 record 迁移和第 2 节的 application 契约迁移；每个新契约由指定 InterfaceAssembler 或 ApplicationService 内部编排构造。
3. 完成第 3 节 repository 重命名；端口、infra 实现和每个调用方必须在同一改动中一致。
4. 完成第 4 节接口层整改；若 Controller path 变更，更新所有受影响的前端请求常量、Controller 测试及 OpenAPI 断言。
5. 每完成一个文件组，立即删除对应的精确 allowance 字符串或 class 名；不得保留 stale allowance。
6. 在每次格式化后检查 `git diff`，仅保留本 RUNBOOK 列出的范围及由编译器定位的直接调用链修正。

## Verification

1. 先对实际修改的 Java 文件执行最窄范围的 Spotless 格式化。
2. 在 `kuzhambu-servers/` 下执行：

   ```sh
   mvn -pl biz/knowledge/kuzhambu-knowledge-application,biz/knowledge/kuzhambu-knowledge-domain,biz/knowledge/kuzhambu-knowledge-interface -am test
   mvn spotless:check
   mvn checkstyle:check
   ```

3. 验证四个 allowlist 文件中不再存在本 RUNBOOK 范围的 key、Controller 类或 Assembler 类；保留的 non-goal allowance 不得变化。
4. 若 HTTP path 或 application service 签名发生变化，补充并执行受影响 Controller、ApplicationService 与前端/API 调用链的定向测试。

## Closure

当四个 allowlist 文件中本 RUNBOOK 范围的 key 全部清零、验证通过且无非目标范围变更时，删除本文档；不要把执行记录保留在 `docs/30-designs/`。
