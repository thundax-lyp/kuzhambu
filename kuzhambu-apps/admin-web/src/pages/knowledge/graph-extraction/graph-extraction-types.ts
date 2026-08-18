import type {
    GraphContentRefRecord,
    GraphContentType,
    GraphTaskDisposition,
    GraphTaskExecutionStatus
} from "@/pages/knowledge/graph-material/graph-material-types";

export type {
    GraphContentRefRecord,
    GraphContentType,
    GraphTaskDisposition,
    GraphTaskExecutionStatus
};

export interface GraphExtractionResultSummaryRecord {
    edgeCount: number;
    nodeCount: number;
    warningCount: number;
}

export interface GraphExtractionTaskRecord {
    id: string;
    materialRef: GraphContentRefRecord;
    lockVersion: string;
    executionStatus: GraphTaskExecutionStatus;
    disposition: GraphTaskDisposition | null;
    attemptNo: string;
    progress: number;
    currentStage: string;
    batchId?: string | null;
    candidateId?: string | null;
    resultSummary?: GraphExtractionResultSummaryRecord | null;
    materialTitle?: string | null;
    categoryName?: string | null;
    failureReason?: string | null;
    errorMessage?: string | null;
    errorType?: string | null;
    requestedAt?: number | string | null;
    completedAt?: number | string | null;
    disposedAt?: number | string | null;
    purgeAfter?: string | null;
    status?: string | null;
    taskId?: string | null;
}

export interface GraphExtractionTaskConflictRecord {
    code: "GRAPH_TASK_LOCK_CONFLICT" | "GRAPH_TASK_STATE_CONFLICT" | "GRAPH_TASK_ACTIVE_EXISTS";
    message: string;
}

export interface GraphExtractionTaskActionResultRecord {
    conflict?: GraphExtractionTaskConflictRecord;
    task?: GraphExtractionTaskRecord;
}
