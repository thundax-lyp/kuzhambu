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
