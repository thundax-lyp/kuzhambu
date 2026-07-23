export type GraphExtractionTaskType = "RELATION" | "GRAPH" | "LINEAGE" | string;
export type GraphExtractionTriggerSource =
    "MANUAL" | "QUALITY_REPORT" | "REGENERATE" | "REFINEMENT_APPLIED" | string;
export type GraphExtractionTaskStatus =
    "PENDING" | "RUNNING" | "SUCCEEDED" | "FAILED" | "APPLIED" | string;

export interface GraphExtractionTaskRecord {
    taskId: string;
    batchJobId?: number | null;
    triggerSource?: GraphExtractionTriggerSource | null;
    taskType?: GraphExtractionTaskType | null;
    scopeType?: string | null;
    scopeJson?: string | null;
    selectionScopeJson?: string | null;
    replaceUnconfirmedOnly?: boolean | null;
    parentTaskId?: number | null;
    sourceContentType?: string | null;
    sourceContentId?: number | null;
    aiCallId?: number | null;
    aiCandidateId?: number | null;
    status?: GraphExtractionTaskStatus | null;
    errorType?: string | null;
    errorMessage?: string | null;
    requestedBy?: number | null;
    requestedAt?: number | null;
    completedAt?: number | null;
    appliedAt?: number | null;
}

export interface GraphExtractionBatchCancelRecord {
    batchJobId: number;
    status?: GraphExtractionTaskStatus | null;
    cancelledCount?: number | null;
    completedCount?: number | null;
    failedCount?: number | null;
}
