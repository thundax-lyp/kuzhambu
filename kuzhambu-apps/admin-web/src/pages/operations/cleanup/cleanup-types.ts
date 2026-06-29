export interface OperationsCleanupRecord {
    cleanupId: number;
    cleanupType?: string | null;
    cleanupStatus?: string | null;
    totalCount?: number | null;
    successCount?: number | null;
    failedCount?: number | null;
    failureReason?: string | null;
    requesterUserId?: number | null;
    startedAt?: string | null;
    completedAt?: string | null;
}
