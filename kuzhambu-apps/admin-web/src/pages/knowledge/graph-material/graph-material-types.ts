export type GraphContentType = "SANCAI_ENTRY" | "WANGQI_DOCUMENT" | "MING_CUSTOMS" | string;

export type GraphMaterialStatus =
    "DRAFT" | "READY" | "PUBLISHING" | "PUBLISHED" | "WITHDRAWING" | "FAILED";

export type GraphTaskExecutionStatus = "PENDING" | "RUNNING" | "SUCCEEDED" | "FAILED" | "CANCELLED";

export type GraphTaskDisposition =
    "PENDING" | "ADOPTED_MERGE" | "ADOPTED_REPLACE" | "DISCARDED" | "SUPERSEDED";

export type GraphMaterialDrawerSection = "OVERVIEW" | "DRAFT_GRAPH" | "TASKS";

export interface GraphContentRefRecord {
    contentType: GraphContentType;
    contentRefId: string;
}

export interface GraphSourceRecord {
    contentRef: GraphContentRefRecord;
    title: string;
    summary?: string | null;
    contentType: GraphContentType;
    category?: string | null;
    volume?: string | null;
}

export interface GraphMaterialRecord {
    id: string;
    contentRef: GraphContentRefRecord;
    title: string;
    contentType: GraphContentType;
    category?: string | null;
    volume?: string | null;
    status: GraphMaterialStatus;
    lockVersion?: string | null;
    publishedAt?: string | null;
    failureReason?: string | null;
    failedOperation?: "PUBLISH" | "WITHDRAW" | null;
}

export interface GraphMaterialBatchPublicationResult {
    failureReason?: string;
    materialId: string;
    status: "PUBLISHED" | "FAILED";
}

export interface GraphPublicationIssueRecord {
    code: string;
    message: string;
    severity: "BLOCKING" | "WARNING" | "INFO" | string;
}

export interface GraphPublicationPreviewObjectRecord {
    issues?: GraphPublicationIssueRecord[] | null;
    matchedObjectId?: string | null;
    matchedObjectLockVersion?: string | null;
    matchType: "CREATE" | "REUSE" | "CONFLICT" | string;
    materialObjectId: string;
}

export interface GraphPublicationPreviewRecord {
    edges: GraphPublicationPreviewObjectRecord[];
    issues: GraphPublicationIssueRecord[];
    materialLockVersion: string;
    materialRef: GraphContentRefRecord;
    nodes: GraphPublicationPreviewObjectRecord[];
    previewToken: string;
    publishable: boolean;
}

export interface GraphPublicationConflictDecisionRecord {
    action: "REUSE_MATCH" | "CREATE_NEW";
    matchedObjectId?: string;
    materialObjectId: string;
    objectType: "NODE" | "EDGE";
}

export interface GraphPublicationConfirmationRecord {
    conflictDecisions: GraphPublicationConflictDecisionRecord[];
    contentRef: GraphContentRefRecord;
    materialLockVersion: string;
    previewToken: string;
}

export interface GraphPublicationResultRecord {
    contentRef: GraphContentRefRecord;
    createdEdgeCount: string;
    createdNodeCount: string;
    failureMessage?: string | null;
    issues?: GraphPublicationIssueRecord[] | null;
    materialStatus: GraphMaterialStatus;
    reusedEdgeCount: string;
    reusedNodeCount: string;
    success: boolean;
}

export interface GraphMaterialDraftObject {
    id: string;
    name: string;
    sourceText: string;
    type: string;
}

export interface GraphMaterialStatsRecord {
    draftNodeCount: string;
    draftEdgeCount: string;
    publishedNodeCount: string;
    publishedEdgeCount: string;
    publicationContributionCount: string;
    activeTaskCount: string;
    pendingReviewTaskCount: string;
    failedTaskCount: string;
    statsRevision: string;
    calculatedAt: string;
}

export interface GraphMaterialTaskSummaryRecord {
    id: string;
    materialRef: GraphContentRefRecord;
    lockVersion: string;
    executionStatus: GraphTaskExecutionStatus;
    disposition: GraphTaskDisposition | null;
    attemptNo: string;
    progress: number;
    currentStage: string;
    batchId?: string | null;
    purgeAfter?: string | null;
    requestedAt?: string | null;
    completedAt?: string | null;
    failureReason?: string | null;
}

export interface GraphMaterialListRecord {
    source: GraphSourceRecord;
    material?: GraphMaterialRecord | null;
    materialStats?: GraphMaterialStatsRecord | null;
    latestTask?: GraphMaterialTaskSummaryRecord | null;
}

export interface GraphMaterialNodeRecord {
    id: string;
    nodeType: string;
    name: string;
    properties: Record<string, unknown>;
    source: "AI" | "MANUAL" | "MATERIAL" | string;
}

export interface GraphMaterialEdgeRecord {
    id: string;
    sourceNodeId: string;
    targetNodeId: string;
    relationType: string;
    qualifiers: Record<string, unknown>;
    source: "AI" | "MANUAL" | "MATERIAL" | string;
}

export interface GraphMaterialDetailRecord {
    source: GraphSourceRecord;
    material?: GraphMaterialRecord | null;
    materialStats?: GraphMaterialStatsRecord | null;
    nodes: GraphMaterialNodeRecord[];
    edges: GraphMaterialEdgeRecord[];
    taskSummary?: GraphMaterialTaskSummarySnapshotRecord | null;
    extractionTasks?: GraphMaterialTaskSummaryRecord[];
    latestTaskCandidate?: GraphExtractionCandidatePreviewRecord | null;
}

export interface GraphExtractionCandidatePreviewRecord {
    candidateId: string;
    resultFormat: string;
    resultSummaryJson: string;
}

export interface GraphMaterialTaskSummarySnapshotRecord {
    activeTaskCount: string;
    pendingReviewTaskCount: string;
    failedTaskCount: string;
    totalTaskCount: string;
    latestTask?: GraphMaterialTaskSummaryRecord | null;
}

export interface GraphBatchMaterialResultRecord<TResult = unknown> {
    contentRef: GraphContentRefRecord;
    failureCode?: string | null;
    failureMessage?: string | null;
    result?: TResult;
    success: boolean;
}

export interface GraphBatchExtractionResultRecord {
    batchId?: string | null;
    materials: GraphBatchMaterialResultRecord<GraphMaterialTaskSummaryRecord>[];
}

export interface GraphBatchWithdrawalPreviewRecord {
    materials: GraphBatchMaterialResultRecord<{
        edgeMappingCount: string;
        governedEdges: string;
        governedNodes: string;
        materialRef: GraphContentRefRecord;
        nodeMappingCount: string;
    }>[];
}

export interface GraphBatchWithdrawalResultRecord {
    batchId?: string | null;
    materials: GraphBatchMaterialResultRecord<GraphMaterialRecord>[];
}

export interface GraphBatchPublicationPreviewRecord {
    materials: GraphBatchMaterialResultRecord<GraphPublicationPreviewRecord>[];
}

export interface GraphBatchPublicationResultRecord {
    materials: GraphBatchMaterialResultRecord<GraphPublicationResultRecord>[];
}

export interface GraphDeletionPrecheckRecord {
    changeId?: string;
    executable?: boolean;
    failureMessage?: string | null;
    issues?: GraphPublicationIssueRecord[] | null;
}

export type MaterialCatalogNodeType = "all" | "category" | "contentType" | "volume";

export interface MaterialCatalogNode {
    children?: MaterialCatalogNode[];
    key: string;
    leaf: boolean;
    nodeType: MaterialCatalogNodeType;
    title: string;
}

export interface GraphMaterialTreeNodeRecord {
    id: string;
    leaf: boolean;
    nodeType: MaterialCatalogNodeType;
    parentId?: string | null;
    title: string;
}
