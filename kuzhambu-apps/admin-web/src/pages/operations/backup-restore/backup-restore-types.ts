export type OperationsBackupType = "AUTO" | "MANUAL" | "PRE_RESTORE";
export type OperationsRestoreMode = "REAL" | "DRILL";

export interface OperationsBackupRecord {
    backupId: number;
    backupType?: OperationsBackupType | null;
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
    restoreMode?: OperationsRestoreMode | null;
    restoreStatus?: string | null;
    writeBlockEnabled?: boolean | null;
    writeBlockStartedAt?: string | null;
    writeBlockReleasedAt?: string | null;
    failureReason?: string | null;
    requesterUserId?: number | null;
    startedAt?: string | null;
    completedAt?: string | null;
}
