export interface OperationsHealthSummaryRecord {
    checkId: number;
    component?: string | null;
    healthStatus?: string | null;
    latencyMs?: number | null;
    message?: string | null;
    checkedAt?: string | null;
}

export interface OperationsTaskRecord {
    snapshotId: number;
    sourceDomain?: string | null;
    taskType?: string | null;
    taskKey?: string | null;
    taskStatus?: string | null;
    totalCount?: number | null;
    successCount?: number | null;
    failedCount?: number | null;
    failureReason?: string | null;
    requestedByUserId?: number | null;
    startedAt?: string | null;
    completedAt?: string | null;
    snapshotAt?: string | null;
}
