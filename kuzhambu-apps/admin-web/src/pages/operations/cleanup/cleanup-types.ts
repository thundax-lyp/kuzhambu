export interface OperationsCleanupRecord {
    cleanupId: string;
    cleanupType?: string | null;
    cleanupStatus?: string | null;
    totalCount?: number | null;
    successCount?: number | null;
    failedCount?: number | null;
    failureReason?: string | null;
    requesterUserId?: string | null;
    startedAt?: string | null;
    completedAt?: string | null;
    items?: OperationsCleanupItemRecord[] | null;
}

export interface OperationsCleanupItemRecord {
    cleanupItemId?: string | null;
    targetType?: string | null;
    targetId?: string | null;
    itemStatus?: string | null;
    failureReason?: string | null;
    processedAt?: string | null;
}
