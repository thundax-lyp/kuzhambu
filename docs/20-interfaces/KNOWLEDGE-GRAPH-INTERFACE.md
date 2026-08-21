# Knowledge Graph Interface

## Purpose

本文是双空间知识图谱唯一 HTTP 契约。所有管理端接口使用 `POST`、JSON body 和既有 `ApiResponse<T>` 包装；所有 `id`、`contentRefId`、`lockVersion` 和游标在 JSON 中均为字符串，避免 JavaScript 精度丢失。

权限：读取接口必须校验 `knowledge:graph:view`；写入、抽取、发布、撤回、导入、删除任务和治理操作必须校验 `knowledge:graph:edit`。服务端从登录上下文取得操作者，禁止请求体传 `operatorId`、`publishedBy` 或 `requestedBy`。页面接口归属 Knowledge：前端不得调用 Classics 接口后自行拼装素材或任务页面；Knowledge 必须同时通过 Classics facade 校验当前主体对来源稿件的可见性和可用性。

## Shared Data Structures

```json
{
  "contentRef": { "contentType": "SANCAI_ENTRY", "contentRefId": "1001" },
  "material": {
    "id": "2001", "contentRef": { "contentType": "SANCAI_ENTRY", "contentRefId": "1001" },
    "contentTitleSnapshot": "三才图会卷一", "status": "DRAFT", "lockVersion": "3", "publishedAt": null,
    "failureReason": null, "failedOperation": null
  },
  "materialStats": {
    "draftNodeCount": "128", "draftEdgeCount": "203",
    "publishedNodeCount": "64", "publishedEdgeCount": "98",
    "activeTaskCount": "1", "pendingReviewTaskCount": "2", "failedTaskCount": "0",
    "statsRevision": "3", "calculatedAt": "1723852800000"
  },
  "task": {
    "id": "7001", "materialRef": { "contentType": "SANCAI_ENTRY", "contentRefId": "1001" },
    "lockVersion": "5", "executionStatus": "SUCCEEDED", "disposition": "PENDING", "attemptNo": "2",
    "progress": 100, "currentStage": "CANDIDATE_READY", "candidateId": "8001",
    "resultSummary": { "nodeCount": 12, "edgeCount": 18, "warningCount": 1 },
    "failureReason": null, "regeneratedFromTaskId": null, "supersededByTaskId": null, "triggeredByTaskId": null,
    "batchId": null, "requestedAt": "1723852800000", "completedAt": "1723852810000",
    "disposedAt": null, "purgeAfter": null
  },
  "materialNode": {
    "id": "3001", "nodeType": "PERSON", "name": "张三", "properties": { "aliases": ["子某"] }, "source": "MANUAL"
  },
  "materialEdge": {
    "id": "4001", "sourceNodeId": "3001", "targetNodeId": "3002", "relationType": "AUTHORED",
    "qualifiers": { "role": "撰者" }, "source": "MANUAL"
  },
  "publishedNode": {
    "id": "5001", "nodeType": "PERSON", "name": "张三", "source": "MATERIAL", "status": "ACTIVE", "lockVersion": "2"
  },
  "publishedEdge": {
    "id": "6001", "sourceNodeId": "5001", "targetNodeId": "5002", "relationType": "AUTHORED",
    "qualifiers": { "role": "撰者" }, "source": "MATERIAL", "status": "ACTIVE", "lockVersion": "1"
  },
  "validationIssue": {
    "code": "NODE_KEY_UNRESOLVED", "severity": "BLOCKING", "objectType": "NODE", "objectId": "3001",
    "field": "identityQualifier", "message": "请补充身份限定信息后发布"
  }
}
```

- `status`：素材固定为 `DRAFT`、`PUBLISHING`、`PUBLISHED`、`WITHDRAWING`、`FAILED`；发布对象为 `ACTIVE`、`DELETED`；删除任务为 `PRECHECKED`、`AWAITING_DECISION`、`PENDING`、`RUNNING`、`SUCCEEDED`、`FAILED`。素材不得返回 `READY`；`DRAFT` 表示未抽取、编辑中或已撤回且可编辑。`FAILED` 必须返回 `failureReason` 和 `failedOperation:"PUBLISH"|"WITHDRAW"`；其他状态两字段均返回 `null`。
- `materialStats` 是素材列表读模型。`statsRevision` 小于素材 `lockVersion` 时，客户端显示“统计更新中”，但不得自行聚合节点、关系或任务表。
- `task.executionStatus` 固定为 `PENDING`、`RUNNING`、`SUCCEEDED`、`FAILED`、`CANCELLED`。`PENDING` 表示尚未确认 AI 批任务正在执行，包括新建、重试和服务启动恢复后的任务；仅在 AI 批任务确认仍执行后转为 `RUNNING`。成功任务的 `disposition` 固定为 `PENDING`、`ADOPTED_MERGE`、`ADOPTED_REPLACE`、`DISCARDED`、`SUPERSEDED`；非成功任务 `disposition` 返回 `null`。用户手动重试 `FAILED` 任务时重新读取当前正文、模型、提示词版本、变量和输出 Schema，创建新任务，并以 `regeneratedFromTaskId` / `supersededByTaskId` 保留关联；原失败任务不原地覆盖。`lockVersion` 用于全部任务状态和候选处置命令的乐观锁校验。
- `properties` 和 `qualifiers` 是 JSON object；只接受 `KNOWLEDGE-GRAPH-SCHEMA.json` 定义的节点、关系和字段。
- `materialNode` / `materialEdge` 不含对象级 `lockVersion`；草稿全部写操作只使用 `materialLockVersion`。只有 `publishedNode` / `publishedEdge` 含对象级 `lockVersion`。
- 写入成功返回最新对象；乐观锁冲突返回业务码 `GRAPH_PREVIEW_STALE` 或 `GRAPH_LOCK_CONFLICT`，前端必须刷新后重新操作。

`publicationPreview` 固定为 `{previewToken,materialRef,materialLockVersion,nodes,edges,issues,publishable}`；`nodes`/`edges` 的每一项为 `{materialObjectId,matchType:"CREATE"|"REUSE"|"CONFLICT",matchedObjectId?,matchedObjectLockVersion?,issues}`。`previewToken` 是服务端随机 token，保存预览生成时的素材版本、所有匹配发布对象 ID/lockVersion 和过期时间；一次确认只能使用一个未过期 token。

`publicationConfirmation` 固定为 `{contentRef,materialLockVersion,previewToken,conflictDecisions}`，其中 `conflictDecisions` 为 `[{objectType:"NODE"|"EDGE",materialObjectId,action:"REUSE_MATCH"|"CREATE_NEW",matchedObjectId?}]`。只有 `CONFLICT` 对象必须提供一个决策；`REUSE_MATCH` 必须提供 preview 中的 `matchedObjectId`，`CREATE_NEW` 不得提供它。用户选择“返回修订”不调用确认接口。

`batchPublicationPreview` 固定为 `{materials:[publicationPreview]}`；`batchPublicationConfirmation` 固定为 `{materials:[publicationConfirmation]}`；`batchPublicationResult` 固定为 `{materials:[{contentRef,success,result?,failureCode?,failureMessage?}]}`。`materials` 按请求顺序返回，禁止服务端重排或去重；每份素材独立预览、校验、提交和返回结果，不创建跨素材事务、批次实体或跨素材回滚。任一素材失败不得阻止其余素材继续处理。

`governanceImpact` 固定为 `{impactToken,nodes,edges,nodeMappings,edgeMappings,issues,executable}`。所有删除、合并和拆分确认 body 必须携带对应 `impactToken`；服务端比较预览时所有受影响对象 ID/lockVersion 和依赖集合，不一致返回 `GRAPH_PREVIEW_STALE`，不写任何数据。

`Page<T>` 固定为 `{pageNo:number,pageSize:number,count:number,totalPage:number,records:[T]}`。`count` 是符合当前筛选条件的记录总数；前端内部如需兼容 `totalCount`，必须由 `count` 映射，HTTP 响应不得读取或返回 `totalCount`。所有 `occurredAt`、`requestedAt`、`completedAt`、`publishedAt` 为 epoch milliseconds 字符串；可空值显式返回 `null`，不省略字段。

`publishedProperty` 固定为 `{id,propertyName,value,preferred,sourceType:"MATERIAL"|"MANUAL",sourceRef?}`；`sourceRef` 为 `{contentRef?,auditLogId?}`，当 `sourceType` 为 `MATERIAL` 时必须有 `contentRef`，为 `MANUAL` 时必须有 `auditLogId`。

`materialMapping` 固定为 `{id,mappingType:"NODE"|"EDGE",status:"ACTIVE"|"WITHDRAWN"|"SOURCE_DELETED_PRESERVED",contentRef,publishedObjectId,sourceSnapshot?}`。`sourceSnapshot` 只在 `SOURCE_DELETED_PRESERVED` 返回，包含删除前可追溯的类型、名称/关系、属性或限定字段摘要。

`governanceOperation` 固定为 `{id,operationType,targetType,targetId,reason,auditLogId,operatorId?,operatorName?,occurredAt,beforeSummary?,afterSummary?}`。`auditLogId` 必有；`operatorId`、`operatorName` 和 `occurredAt` 由 System Audit facade 按该 ID 查询。`beforeSummary` / `afterSummary` 只返回用户可读摘要，不返回内部 key 或完整快照。

`extractionStage` 固定为 `{stageNo,stageCode,status,progress,inputSummary?,outputSummary?,failureReason?,startedAt?,completedAt?}`；`status` 取 `PENDING`、`RUNNING`、`SUCCEEDED`、`FAILED`、`SKIPPED`。`inputSummary` 和 `outputSummary` 只返回用户可读摘要，不返回模型凭据、完整提示词、完整正文或 AI 域内部调用载荷。

`candidatePreview` 固定为 `{candidateId,nodes,edges,issues,diff,dispositionRecord?}`。其中 `nodes` 为 `[{candidateObjectId,nodeType,name,properties}]`，`edges` 为 `[{candidateObjectId,sourceCandidateNodeId,targetCandidateNodeId,relationType,qualifiers}]`，`diff` 为 `[{candidateObjectId,objectType:"NODE"|"EDGE",changeType:"ADD"|"UPDATE"|"REMOVE"|"CONFLICT",draftObjectId?,changedFields?,issues}]`。`REMOVE` 仅在 `REPLACE` 预览中返回。`dispositionRecord` 固定为 `{disposition,reason?,disposedAt?,auditLogId?}`；任务成功但候选尚未处置时它为 `null`。候选不存在、已由 AI 域清理或当前用户失去来源可见性时，`task/get` 返回 `candidate:null` 及业务码 `GRAPH_CANDIDATE_UNAVAILABLE`，不得返回残留候选载荷。

图谱提取任务的创建或状态变更命令必须带 `idempotencyKey`。既有任务的动作还必须带 `{taskId,taskLockVersion,expectedExecutionStatus,expectedDisposition?}`；服务端在一个原子状态转换中校验版本和预期状态。`retry` 的预期运行状态只能为 `FAILED`，并创建使用当前快照的新任务；`cancel` 只能为 `PENDING` 或 `RUNNING`；`regenerate` 只能为 `SUCCEEDED`；`delete` 只允许 `FAILED`、`CANCELLED` 或采纳状态已经处置的 `SUCCEEDED` 任务。版本不一致返回 `GRAPH_TASK_LOCK_CONFLICT`，状态或采纳状态不满足返回 `GRAPH_TASK_STATE_CONFLICT`，素材已有活动任务返回 `GRAPH_TASK_ACTIVE_EXISTS`。同一操作者、同一路径和同一 `idempotencyKey` 的重复请求返回首次成功结果，不重复创建任务或投递执行。

`batchExtractionResult` 与 `batchWithdrawalResult` 固定为 `{batchId?,materials:[{contentRef,success,result?,failureCode?,failureMessage?}]}`。`materials` 按请求顺序返回，禁止服务端重排或去重；每份素材独立校验和执行，任一失败不得阻止其余素材继续处理。

## Admin Resources

| URL | request | response | purpose |
| --- | --- | --- | --- |
| `/knowledge/graph/workbench/overview/get` | `{}` | `{snapshotAt,publishedNodeCount,publishedEdgeCount,coveredMaterialCount,isolatedNodeCount,missingCoreRelationNodeCount,recentActivities:[{type,contentRef?,occurredAt,summary}],pendingConflictCount}` | Redis 快照工作台统计；快照未就绪时返回 `WORKBENCH_SNAPSHOT_UNAVAILABLE` |
| `/knowledge/graph/workbench/recent-edges/list` | `{}` | `{nodes:[publishedNode],edges:[publishedEdge]}` | 最近更新的最多 200 条 ACTIVE 正式关系及其去重端点 |
| `/knowledge/graph/workbench/one-hop-edges/list` | `{nodeIds:[string(1..400)],afterEdgeId?:string}` | `{nodes:[publishedNode],edges:[publishedEdge],nextCursor?:string,truncated:boolean}` | 固定每批最多 50 条的一跳渐进关系 |
| `/knowledge/graph/workbench/search/page` | `{keyword?,nodeType?,relationType?,pageNo,pageSize}` | `Page<{objectType,node?:publishedNode,edge?:publishedEdge}>` | 全局搜索 |
| `/knowledge/graph/workbench/quality/get` | `{issueType?:"ISOLATED_NODE"|"MISSING_CORE_RELATION",nodeType?}` | `{isolatedNodeCount,missingCoreRelationNodeCount,isolatedNodes,missingCoreRelationNodes}` | 质量待办 |
| `/knowledge/graph/material/page` | `{keyword?,contentType?,categoryCode?,volumeCode?,status?,taskExecutionStatus?,taskDisposition?,pageNo,pageSize}` | `Page<{source:{contentRef,title,contentType,category?,volume?},material?,materialStats?,latestTask?}>` | Classics 可见稿件分页后补齐 Knowledge 素材、统计和任务摘要 |
| `/knowledge/graph/material/get` | `{contentRef}` | `{source:{contentRef,title,summary,category?,volume?},material?,materialStats?,nodes:[materialNode],edges:[materialEdge],taskSummary}` | 素材 `SegmentedDrawer` 的概览和草稿图数据 |
| `/knowledge/graph/material/node/create` / `update` / `delete` | create/update `{contentRef,node:materialNode,materialLockVersion}`；delete `{contentRef,nodeId,materialLockVersion}` | `{material,nodes:[materialNode],edges:[materialEdge]}` | 草稿节点 CRUD |
| `/knowledge/graph/material/edge/create` / `update` / `delete` | create/update `{contentRef,edge:materialEdge,materialLockVersion}`；delete `{contentRef,edgeId,materialLockVersion}` | `{material,nodes:[materialNode],edges:[materialEdge]}` | 草稿边 CRUD |
| `/knowledge/graph/material/node/merge/preview` / `apply` | preview `{contentRef,retainedNodeId,mergedNodeIds}`；apply 再加 `materialLockVersion` | `{nodes,edges,issues,executable}` / `{material,nodes,edges}` | 草稿合并 |
| `/knowledge/graph/material/node/split/preview` / `apply` | preview `{contentRef,sourceNodeId}`；apply `{contentRef,sourceNodeId,splitNode,reassignedEdgeIds,materialLockVersion}` | 同上 | 草稿拆分 |
| `/knowledge/graph/task/page` | `{keyword?,contentType?,categoryCode?,volumeCode?,contentRefs?,batchId?,executionStatus?,disposition?,groupBy:"NONE"|"MATERIAL",pageNo,pageSize}` | `Page<task>` 或 `Page<{source,materialStats,tasks:[task]}>` | 默认跨素材处理队列；可按素材分组或查看批量操作关联任务 |
| `/knowledge/graph/task/get` | `{taskId}` | `{task,source,materialStats,stages,relatedTasks,candidate?}` | 任务 `SegmentedDrawer` 详情 |
| `/knowledge/graph/material/extraction/create` | `{contentRef,idempotencyKey}` | `task` | 创建单素材提取；同一素材至多一条活动任务 |
| `/knowledge/graph/task/batch/create` | `{selection:{contentRefs?:[contentRef],volumeCode?:string},idempotencyKey}`，`contentRefs` 与 `volumeCode` 二选一 | `batchExtractionResult` | 批量或整卷创建；服务端只处理当前用户可见且可抽取的素材 |
| `/knowledge/graph/task/retry` / `cancel` | `{taskId,taskLockVersion,expectedExecutionStatus,idempotencyKey}` | `task` | 使用当前快照创建重试任务，或取消活动任务 |
| `/knowledge/graph/task/delete` | `{taskId,taskLockVersion,expectedExecutionStatus,idempotencyKey}` | `{deletedTaskId}` | 删除失败、取消或已处置的终态任务 |
| `/knowledge/graph/task/candidate/apply` / `discard` / `regenerate` | apply `{taskId,taskLockVersion,expectedExecutionStatus:"SUCCEEDED",expectedDisposition:"PENDING",applyMode:"MERGE"|"REPLACE",materialLockVersion,idempotencyKey}`；discard 同上再加 `reason?`；regenerate `{taskId,taskLockVersion,expectedExecutionStatus,expectedDisposition?,idempotencyKey}` | `task` 或 `{task,material}` | 候选采用、丢弃或创建新任务 |
| `/knowledge/graph/material/import/preview` / `apply` | preview `{contentRef,graphJson}`；apply `{contentRef,graphJson,applyMode:"MERGE"|"REPLACE",materialLockVersion}` | `{importedGraph,createdNodeCount,updatedNodeCount,createdEdgeCount,updatedEdgeCount,issues,importable}` / graph | JSON 导入 |
| `/knowledge/graph/material/export` | `{contentRef}` | `{fileName,graphJson}` | JSON 下载 |
| `/knowledge/graph/publication/preview` | `{contentRef}` | `publicationPreview` | 发布预览 |
| `/knowledge/graph/publication/publish` | `publicationConfirmation` | `{contentRef,materialStatus,success,failureMessage,createdNodeCount,reusedNodeCount,createdEdgeCount,reusedEdgeCount,issues}` | 确认整体发布 |
| `/knowledge/graph/publication/batch/preview` | `{contentRefs:[contentRef]}` | `batchPublicationPreview` | 多素材独立发布预览 |
| `/knowledge/graph/publication/batch/publish` | `batchPublicationConfirmation` | `batchPublicationResult` | 多素材独立确认发布 |
| `/knowledge/graph/publication/withdrawal/preview` / `withdraw` | preview `{contentRef}`；withdraw `{contentRef,materialLockVersion}` | `{materialRef,nodeMappingCount,edgeMappingCount,governedNodes,governedEdges}` / `material` | 整体撤回 |
| `/knowledge/graph/publication/batch/withdrawal/preview` / `withdraw` | preview `{contentRefs:[contentRef]}`；withdraw `{materials:[{contentRef,materialLockVersion}],idempotencyKey}` | `{materials:[{contentRef,preview?,failureCode?,failureMessage?}]}` / `batchWithdrawalResult` | 多素材独立撤回 |
| `/knowledge/graph/published/node/page` / `get` | page `{keyword?,nodeType?,status?,source?,pageNo,pageSize}`；get `{nodeId}` | `Page<publishedNode>` / `{node:publishedNode,properties:[publishedProperty],materials:[materialMapping],incidentEdges:[publishedEdge],operations:[governanceOperation]}` | 节点浏览 |
| `/knowledge/graph/published/node/create` / `update` | create `{node:publishedNode,properties:[publishedProperty],reason}`；update `{node:publishedNode,properties:[publishedProperty],reason,lockVersion}` | `{node:publishedNode,properties:[publishedProperty],materials:[materialMapping],incidentEdges:[publishedEdge],operations:[governanceOperation]}` | 节点维护 |
| `/knowledge/graph/published/node/delete/preview` / `delete` | preview `{nodeId,cascadeEdges}`；delete `{nodeId,cascadeEdges,lockVersion,impactToken,reason}` | `governanceImpact` / `{deletedNodeId}` | 节点删除 |
| `/knowledge/graph/published/node/merge/preview` / `merge` | preview `{retainedNodeId,mergedNodeIds}`；merge `{retainedNodeId,mergedNodeIds,retainedNodeLockVersion,impactToken,reason}` | `governanceImpact` / node detail | 节点合并 |
| `/knowledge/graph/published/node/split/preview` / `split` | preview `{sourceNodeId}`；split `{sourceNodeId,splitNode,movedPropertyIds,copiedPropertyIds,reassignedEdgeIds,copiedEdges,movedMaterialRefs,copiedMaterialRefs,sourceNodeLockVersion,impactToken,reason}` | `governanceImpact` / node detail | 节点拆分 |
| `/knowledge/graph/published/edge/page` / `get` | page `{keyword?,relationType?,status?,source?,pageNo,pageSize}`；get `{edgeId}` | `Page<publishedEdge>` / `{edge:publishedEdge,sourceNode:publishedNode,targetNode:publishedNode,properties:[publishedProperty],materials:[materialMapping],operations:[governanceOperation]}` | 关系浏览 |
| `/knowledge/graph/published/edge/create` / `update` | create `{edge:publishedEdge,properties:[publishedProperty],reason}`；update `{edge:publishedEdge,properties:[publishedProperty],reason,lockVersion}` | edge detail | 关系维护 |
| `/knowledge/graph/published/edge/delete/preview` / `delete` | preview `{edgeId}`；delete `{edgeId,lockVersion,impactToken,reason}` | `governanceImpact` / `{deletedEdgeId}` | 关系删除 |
| `/knowledge/graph/deletion-change/precheck` / `page` / `decision` | precheck `{contentRef}`；decision `{changeId,decision:"PRESERVE_CONTRIBUTION"|"WITHDRAW_ASSOCIATIONS",lockVersion}` | change / Page<change> / task | 删除变更列表 |
| `/knowledge/graph/deletion-task/page` / `get` / `retry` | page `{status?,pageNo,pageSize}`；retry `{taskId,lockVersion}` | Page<task> / task | 删除后台任务 |

`task` 固定为 `{id,materialRef,materialTitle?,categoryName?,volumeName?,lockVersion,executionStatus,disposition,attemptNo,progress,currentStage,candidateId?,resultSummary?,failureReason?,batchId?,regeneratedFromTaskId?,supersededByTaskId?,triggeredByTaskId?,requestedAt,completedAt?,disposedAt?,purgeAfter?}`。`materialTitle`、`categoryName` 和 `volumeName` 来自任务提交时冻结的来源快照，页面按 `categoryName / volumeName / materialTitle` 显示完整素材路径并省略空层级。`stages` 固定为 `[extractionStage]`，`candidate` 固定为 `candidatePreview|null`。`relatedTasks` 仅返回重新抽取来源、替代关系、同批任务和上游触发任务的 `{id,materialRef,executionStatus,disposition,requestedAt}`。候选应用必须携带当前 `materialLockVersion`；冲突时返回 `GRAPH_LOCK_CONFLICT`，不得覆盖已更新草稿。`conflictDecisions` 只在本次发布有效；存在 `BLOCKING` issue 或未决冲突时 `publish` 必须拒绝。

## Portal Resource

`POST /portal/knowledge/graph/material/get`

Request: `{ "contentType": "SANCAI_ENTRY", "contentRefId": "1001" }`。

Response: `{ "visible": true, "contentRef": { ... }, "nodes": [publishedNode], "edges": [publishedEdge] }`。素材未发布、稿件不可见或不存在时返回 HTTP 200 及 `visible:false` 和空数组；不得返回发布空间搜索、人工来源、治理记录、草稿或映射详情。

## Explicit Exclusions

旧接口 `/knowledge/graph-extraction/*`、`/knowledge/graph-result/*`、`/knowledge/refinement/*` 和 `/portal/knowledge/atlas/get` 不得被新页面调用。世系图不属于本接口范围。
