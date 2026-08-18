import type {
    GraphContentRefRecord,
    GraphContentType,
    GraphMaterialStatsRecord,
    GraphSourceRecord,
    GraphTaskDisposition,
    GraphTaskExecutionStatus
} from "@/pages/knowledge/graph-material/graph-material-types";

export type {
    GraphContentRefRecord,
    GraphContentType,
    GraphTaskDisposition,
    GraphTaskExecutionStatus
};

export type GraphExtractionTaskDrawerSection = "OVERVIEW" | "EXECUTION" | "CANDIDATE";
export type GraphCandidateDiffChangeType = "ADD" | "UPDATE" | "REMOVE" | "CONFLICT";
export type GraphExtractionStageStatus = "PENDING" | "RUNNING" | "SUCCEEDED" | "FAILED" | "SKIPPED";
export type GraphCandidateObjectType = "NODE" | "EDGE";
export type GraphCandidateApplyMode = "MERGE" | "REPLACE";
export type GraphExtractionTaskType = "RELATION" | "GRAPH" | "LINEAGE" | string;
export type GraphExtractionTriggerSource =
    "MANUAL" | "QUALITY_REPORT" | "REGENERATE" | "REFINEMENT_APPLIED" | string;
export type GraphExtractionTaskStatus =
    "PENDING" | "RUNNING" | "SUCCEEDED" | "FAILED" | "APPLIED" | string;
export type GraphWorkbenchSourceContentType =
    "SANCAI_ENTRY" | "WANGQI_DOCUMENT" | "MING_CUSTOMS" | string;
export type GraphWorkbenchNodeType = "SOURCE_ROOT" | "CATEGORY" | "VOLUME" | "MANUSCRIPT" | string;
export type GraphWorkbenchStatus =
    | "NOT_EXTRACTED"
    | "EXTRACTING"
    | "EXTRACTION_FAILED"
    | "CANDIDATE_READY"
    | "APPLIED"
    | "REFINING"
    | "REFINED"
    | "QUALITY_ISSUE"
    | string;

export interface GraphExtractionResultSummaryRecord {
    edgeCount: number;
    nodeCount: number;
    warningCount: number;
}

export interface GraphExtractionTaskRecord {
    id: string;
    materialRef: GraphContentRefRecord;
    materialTitle?: string | null;
    categoryName?: string | null;
    lockVersion: string;
    executionStatus: GraphTaskExecutionStatus;
    disposition: GraphTaskDisposition | null;
    attemptNo: string;
    progress: number;
    currentStage: string;
    batchId?: string | null;
    candidateId?: string | null;
    resultSummary?: GraphExtractionResultSummaryRecord | null;
    failureReason?: string | null;
    regeneratedFromTaskId?: string | null;
    supersededByTaskId?: string | null;
    triggeredByTaskId?: string | null;
    requestedAt?: number | string | null;
    completedAt?: number | string | null;
    disposedAt?: number | string | null;
    purgeAfter?: string | null;
    aiCallId?: string | null;
    aiCandidateId?: string | null;
    appliedAt?: number | null;
    batchJobId?: string | null;
    errorMessage?: string | null;
    errorType?: string | null;
    parentTaskId?: string | null;
    replaceUnconfirmedOnly?: boolean | null;
    requestedBy?: string | null;
    selectionScopeJson?: string | null;
    sourceContentId?: string | null;
    sourceContentType?: string | null;
    status?: GraphExtractionTaskStatus | null;
    taskId?: string | null;
    taskType?: GraphExtractionTaskType | null;
    triggerSource?: GraphExtractionTriggerSource | null;
}

export interface GraphExtractionStageRecord {
    stageNo: string;
    stageCode: string;
    status: GraphExtractionStageStatus;
    progress: number;
    inputSummary?: string | null;
    outputSummary?: string | null;
    failureReason?: string | null;
    startedAt?: string | null;
    completedAt?: string | null;
}

export interface GraphCandidateNodeRecord {
    candidateObjectId: string;
    nodeType: string;
    name: string;
    properties: Record<string, unknown>;
}

export interface GraphCandidateEdgeRecord {
    candidateObjectId: string;
    sourceCandidateNodeId: string;
    targetCandidateNodeId: string;
    relationType: string;
    qualifiers: Record<string, unknown>;
}

export interface GraphCandidateIssueRecord {
    code: string;
    severity: "BLOCKING" | "WARNING" | "INFO" | string;
    objectType?: GraphCandidateObjectType | null;
    objectId?: string | null;
    field?: string | null;
    message: string;
}

export interface GraphCandidateDiffRecord {
    candidateObjectId: string;
    objectType: GraphCandidateObjectType;
    changeType: GraphCandidateDiffChangeType;
    draftObjectId?: string | null;
    changedFields?: string[] | null;
    issues?: GraphCandidateIssueRecord[] | null;
}

export interface GraphCandidateDispositionRecord {
    disposition: GraphTaskDisposition;
    reason?: string | null;
    disposedAt?: string | null;
    auditLogId?: string | null;
}

export interface GraphCandidatePreviewRecord {
    candidateId: string;
    nodes: GraphCandidateNodeRecord[];
    edges: GraphCandidateEdgeRecord[];
    issues: GraphCandidateIssueRecord[];
    diff: GraphCandidateDiffRecord[];
    dispositionRecord?: GraphCandidateDispositionRecord | null;
}

export interface GraphRelatedTaskRecord {
    id: string;
    materialRef: GraphContentRefRecord;
    executionStatus: GraphTaskExecutionStatus;
    disposition: GraphTaskDisposition | null;
    requestedAt?: string | null;
}

export interface GraphExtractionTaskDetailRecord {
    task: GraphExtractionTaskRecord;
    source: GraphSourceRecord;
    materialStats?: GraphMaterialStatsRecord | null;
    stages: GraphExtractionStageRecord[];
    relatedTasks: GraphRelatedTaskRecord[];
    candidate?: GraphCandidatePreviewRecord | null;
}

export interface GraphExtractionMaterialGroupRecord {
    source: GraphSourceRecord;
    materialStats?: GraphMaterialStatsRecord | null;
    tasks: GraphExtractionTaskRecord[];
}

export interface GraphBatchExtractionResultRecord {
    batchId?: string | null;
    materials: Array<{
        contentRef: GraphContentRefRecord;
        failureCode?: string | null;
        failureMessage?: string | null;
        result?: GraphExtractionTaskRecord;
        success: boolean;
    }>;
}

export interface GraphExtractionTaskConflictRecord {
    code:
        | "GRAPH_TASK_LOCK_CONFLICT"
        | "GRAPH_TASK_STATE_CONFLICT"
        | "GRAPH_TASK_ACTIVE_EXISTS"
        | "GRAPH_CANDIDATE_UNAVAILABLE";
    message: string;
}

export interface GraphExtractionTaskActionResultRecord {
    conflict?: GraphExtractionTaskConflictRecord;
    task?: GraphExtractionTaskRecord;
}

export interface GraphWorkbenchManuscriptNode {
    children?: GraphWorkbenchManuscriptNode[] | null;
    graphStatus?: GraphWorkbenchStatus | null;
    latestGraphVersionId?: string | null;
    latestTaskId?: string | null;
    nodeKey: string;
    nodeType?: GraphWorkbenchNodeType | null;
    parentKey?: string | null;
    sourceContentId?: string | null;
    sourceContentType?: GraphWorkbenchSourceContentType | null;
    sourcePath?: string | null;
    title?: string | null;
}

export interface GraphWorkbenchManuscriptRecord {
    currentVersionNo?: number | null;
    graphStatus?: GraphWorkbenchStatus | null;
    latestExtractionTask?: GraphExtractionTaskRecord | null;
    latestGraphVersion?: { versionId?: string | null } | null;
    qualitySummary?: unknown;
    sourceContentId?: string | null;
    sourceContentType?: GraphWorkbenchSourceContentType | null;
    sourcePath?: string | null;
    summary?: string | null;
    title?: string | null;
}

export interface GraphWorkbenchCandidateEntityRecord {
    description?: string | null;
    entityType?: string | null;
    name?: string | null;
}

export interface GraphWorkbenchCandidateRelationRecord {
    evidence?: string | null;
    relationType?: string | null;
    sourceName?: string | null;
    sourceType?: string | null;
    targetName?: string | null;
    targetType?: string | null;
}

export interface GraphWorkbenchCandidateRecord {
    aiCandidateId?: string | null;
    candidatePayloadJson?: string | null;
    entities?: GraphWorkbenchCandidateEntityRecord[] | null;
    relations?: GraphWorkbenchCandidateRelationRecord[] | null;
    sourceContentId?: string | null;
    sourceContentType?: GraphWorkbenchSourceContentType | null;
    status?: string | null;
    taskId?: string | null;
    taskType?: GraphExtractionTaskType | null;
    warnings?: string[] | null;
}

export interface GraphWorkbenchCandidateApplyRecord {
    graphStatus?: GraphWorkbenchStatus | null;
    graphVersionId?: string | null;
    taskId?: string | null;
}

export interface GraphWorkbenchRelationRecord {
    relationId: string;
    relationType?: string | null;
    sourceName?: string | null;
    sourceType?: string | null;
    targetName?: string | null;
    targetType?: string | null;
}
