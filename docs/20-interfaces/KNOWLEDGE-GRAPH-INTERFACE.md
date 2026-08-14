# Knowledge Graph Interface

## Purpose

本文是双空间知识图谱唯一 HTTP 契约。所有管理端接口使用 `POST`、JSON body 和既有 `ApiResponse<T>` 包装；所有 `id`、`contentRefId`、`lockVersion` 和游标在 JSON 中均为字符串，避免 JavaScript 精度丢失。

权限：读取接口必须校验 `knowledge:graph:view`；写入、抽取、发布、撤回、导入、删除任务和治理操作必须校验 `knowledge:graph:edit`。服务端从登录上下文取得操作者，禁止请求体传 `operatorId`、`publishedBy` 或 `requestedBy`。

## Shared Data Structures

```json
{
  "contentRef": { "contentType": "SANCAI_ENTRY", "contentRefId": "1001" },
  "material": {
    "id": "2001", "contentRef": { "contentType": "SANCAI_ENTRY", "contentRefId": "1001" },
    "contentTitleSnapshot": "三才图会卷一", "status": "DRAFT", "lockVersion": "3", "publishedAt": null,
    "failureReason": null, "failedOperation": null
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
- `properties` 和 `qualifiers` 是 JSON object；只接受 `KNOWLEDGE-GRAPH-SCHEMA.json` 定义的节点、关系和字段。
- `materialNode` / `materialEdge` 不含对象级 `lockVersion`；草稿全部写操作只使用 `materialLockVersion`。只有 `publishedNode` / `publishedEdge` 含对象级 `lockVersion`。
- 写入成功返回最新对象；乐观锁冲突返回业务码 `GRAPH_PREVIEW_STALE` 或 `GRAPH_LOCK_CONFLICT`，前端必须刷新后重新操作。

`publicationPreview` 固定为 `{previewToken,materialRef,materialLockVersion,nodes,edges,issues,publishable}`；`nodes`/`edges` 的每一项为 `{materialObjectId,matchType:"CREATE"|"REUSE"|"CONFLICT",matchedObjectId?,matchedObjectLockVersion?,issues}`。`previewToken` 是服务端随机 token，保存预览生成时的素材版本、所有匹配发布对象 ID/lockVersion 和过期时间；一次确认只能使用一个未过期 token。

`publicationConfirmation` 固定为 `{contentRef,materialLockVersion,previewToken,conflictDecisions}`，其中 `conflictDecisions` 为 `[{objectType:"NODE"|"EDGE",materialObjectId,action:"REUSE_MATCH"|"CREATE_NEW",matchedObjectId?}]`。只有 `CONFLICT` 对象必须提供一个决策；`REUSE_MATCH` 必须提供 preview 中的 `matchedObjectId`，`CREATE_NEW` 不得提供它。用户选择“返回修订”不调用确认接口。

`batchPublicationPreview` 固定为 `{materials:[publicationPreview]}`；`batchPublicationConfirmation` 固定为 `{materials:[publicationConfirmation]}`；`batchPublicationResult` 固定为 `{materials:[{contentRef,success,result?,failureCode?,failureMessage?}]}`。`materials` 按请求顺序返回，禁止服务端重排或去重；每份素材独立预览、校验、提交和返回结果，不创建跨素材事务、批次实体或跨素材回滚。任一素材失败不得阻止其余素材继续处理。

`governanceImpact` 固定为 `{impactToken,nodes,edges,nodeMappings,edgeMappings,issues,executable}`。所有删除、合并和拆分确认 body 必须携带对应 `impactToken`；服务端比较预览时所有受影响对象 ID/lockVersion 和依赖集合，不一致返回 `GRAPH_PREVIEW_STALE`，不写任何数据。

`Page<T>` 固定为 `{pageNo:string,pageSize:string,totalCount:string,totalPage:string,records:[T]}`。所有 `occurredAt`、`requestedAt`、`completedAt`、`publishedAt` 为 epoch milliseconds 字符串；可空值显式返回 `null`，不省略字段。

`publishedProperty` 固定为 `{id,propertyName,value,preferred,sourceType:"MATERIAL"|"MANUAL",sourceRef?}`；`sourceRef` 为 `{contentRef?,auditLogId?}`，当 `sourceType` 为 `MATERIAL` 时必须有 `contentRef`，为 `MANUAL` 时必须有 `auditLogId`。

`materialMapping` 固定为 `{id,mappingType:"NODE"|"EDGE",status:"ACTIVE"|"WITHDRAWN"|"SOURCE_DELETED_PRESERVED",contentRef,publishedObjectId,sourceSnapshot?}`。`sourceSnapshot` 只在 `SOURCE_DELETED_PRESERVED` 返回，包含删除前可追溯的类型、名称/关系、属性或限定字段摘要。

`governanceOperation` 固定为 `{id,operationType,targetType,targetId,reason,auditLogId,operatorId?,operatorName?,occurredAt,beforeSummary?,afterSummary?}`。`auditLogId` 必有；`operatorId`、`operatorName` 和 `occurredAt` 由 System Audit facade 按该 ID 查询。`beforeSummary` / `afterSummary` 只返回用户可读摘要，不返回内部 key 或完整快照。

## Admin Resources

| URL | request | response | purpose |
| --- | --- | --- | --- |
| `/knowledge/graph/workbench/overview/get` | `{}` | `{publishedNodeCount,publishedEdgeCount,coveredMaterialCount,isolatedNodeCount,missingCoreRelationNodeCount,recentActivities:[{type,contentRef?,occurredAt,summary}],pendingConflictCount}` | 工作台统计 |
| `/knowledge/graph/workbench/seeds/list` | `{}` | `{nodes:[publishedNode]}` | 最近发布 100 个种子 |
| `/knowledge/graph/workbench/incident-edges/list` | `{nodeIds:[string],afterEdgeId?:string,pageSize:number}` | `{nodes:[publishedNode],edges:[publishedEdge],nextCursor?:string,truncated:boolean}` | 渐进子图 |
| `/knowledge/graph/workbench/search/page` | `{keyword?,nodeType?,relationType?,pageNo,pageSize}` | `Page<{objectType,node?:publishedNode,edge?:publishedEdge}>` | 全局搜索 |
| `/knowledge/graph/workbench/quality/get` | `{issueType?:"ISOLATED_NODE"|"MISSING_CORE_RELATION",nodeType?}` | `{isolatedNodeCount,missingCoreRelationNodeCount,isolatedNodes,missingCoreRelationNodes}` | 质量待办 |
| `/knowledge/graph/material/page` | `{keyword?,status?,pageNo,pageSize}` | `Page<material>` | 素材库 |
| `/knowledge/graph/material/get` | `{contentRef}` | `{material,nodes:[materialNode],edges:[materialEdge],extractionTasks:[task]}` | 单素材画布 |
| `/knowledge/graph/material/node/create` / `update` / `delete` | create/update `{contentRef,node:materialNode,materialLockVersion}`；delete `{contentRef,nodeId,materialLockVersion}` | `{material,nodes:[materialNode],edges:[materialEdge]}` | 草稿节点 CRUD |
| `/knowledge/graph/material/edge/create` / `update` / `delete` | create/update `{contentRef,edge:materialEdge,materialLockVersion}`；delete `{contentRef,edgeId,materialLockVersion}` | `{material,nodes:[materialNode],edges:[materialEdge]}` | 草稿边 CRUD |
| `/knowledge/graph/material/node/merge/preview` / `apply` | preview `{contentRef,retainedNodeId,mergedNodeIds}`；apply 再加 `materialLockVersion` | `{nodes,edges,issues,executable}` / `{material,nodes,edges}` | 草稿合并 |
| `/knowledge/graph/material/node/split/preview` / `apply` | preview `{contentRef,sourceNodeId}`；apply `{contentRef,sourceNodeId,splitNode,reassignedEdgeIds,materialLockVersion}` | 同上 | 草稿拆分 |
| `/knowledge/graph/material/extraction/create` / `get` / `retry` | create `{contentRef}`；get `{contentRef}`；retry `{contentRef,failedTaskId}` | `task` | 异步抽取 |
| `/knowledge/graph/material/import/preview` / `apply` | preview `{contentRef,graphJson}`；apply `{contentRef,graphJson,applyMode:"MERGE"|"REPLACE",materialLockVersion}` | `{importedGraph,createdNodeCount,updatedNodeCount,createdEdgeCount,updatedEdgeCount,issues,importable}` / graph | JSON 导入 |
| `/knowledge/graph/material/export` | `{contentRef}` | `{fileName,graphJson}` | JSON 下载 |
| `/knowledge/graph/publication/preview` | `{contentRef}` | `publicationPreview` | 发布预览 |
| `/knowledge/graph/publication/publish` | `publicationConfirmation` | `{contentRef,materialStatus,success,failureMessage,createdNodeCount,reusedNodeCount,createdEdgeCount,reusedEdgeCount,issues}` | 确认整体发布 |
| `/knowledge/graph/publication/batch/preview` | `{contentRefs:[contentRef]}` | `batchPublicationPreview` | 多素材独立发布预览 |
| `/knowledge/graph/publication/batch/publish` | `batchPublicationConfirmation` | `batchPublicationResult` | 多素材独立确认发布 |
| `/knowledge/graph/publication/withdrawal/preview` / `withdraw` | preview `{contentRef}`；withdraw `{contentRef,materialLockVersion}` | `{materialRef,nodeMappingCount,edgeMappingCount,governedNodes,governedEdges}` / `material` | 整体撤回 |
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

`task` 固定为 `{id,status,progress,inputSnapshotVersion,resultSummary,failureReason,retryFromTaskId,requestedAt,completedAt}`。`conflictDecisions` 只在本次发布有效；存在 `BLOCKING` issue 或未决冲突时 `publish` 必须拒绝。

## Portal Resource

`POST /portal/knowledge/graph/material/get`

Request: `{ "contentType": "SANCAI_ENTRY", "contentRefId": "1001" }`。

Response: `{ "visible": true, "contentRef": { ... }, "nodes": [publishedNode], "edges": [publishedEdge] }`。素材未发布、稿件不可见或不存在时返回 HTTP 200 及 `visible:false` 和空数组；不得返回发布空间搜索、人工来源、治理记录、草稿或映射详情。

## Explicit Exclusions

旧接口 `/knowledge/graph-extraction/*`、`/knowledge/graph-result/*`、`/knowledge/refinement/*` 和 `/portal/knowledge/atlas/get` 不得被新页面调用。世系图不属于本接口范围。
