export interface OperationsBackupRecord {
    backupId: number;
    backupType?: string | null;
    backupStatus?: string | null;
    storageObjectId?: number | null;
    fileName?: string | null;
    fileSizeBytes?: number | null;
    checksum?: string | null;
    failureReason?: string | null;
    requesterUserId?: number | null;
    startedAt?: string | null;
    completedAt?: string | null;
    expiresAt?: string | null;
}

export interface OperationsRestoreRecord {
    restoreId: number;
    backupId?: number | null;
    preRestoreBackupId?: number | null;
    restoreStatus?: string | null;
    writeBlockEnabled?: boolean | null;
    failureReason?: string | null;
    requesterUserId?: number | null;
    startedAt?: string | null;
    completedAt?: string | null;
}
