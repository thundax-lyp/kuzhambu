export type OperationsBackupType = "AUTO" | "MANUAL" | "PRE_RESTORE";
export type OperationsRestoreMode = "REAL" | "DRILL";

export interface OperationsBackupRecord {
    backupId: string;
    backupType?: OperationsBackupType | null;
    backupStatus?: string | null;
    storageObjectId?: string | null;
    fileName?: string | null;
    fileSizeBytes?: number | null;
    checksum?: string | null;
    failureReason?: string | null;
    requesterUserId?: string | null;
    startedAt?: string | null;
    completedAt?: string | null;
    expiresAt?: string | null;
}

export interface OperationsRestoreRecord {
    restoreId: string;
    backupId?: string | null;
    preRestoreBackupId?: string | null;
    restoreMode?: OperationsRestoreMode | null;
    restoreStatus?: string | null;
    writeBlockEnabled?: boolean | null;
    writeBlockStartedAt?: string | null;
    writeBlockReleasedAt?: string | null;
    failureReason?: string | null;
    requesterUserId?: string | null;
    startedAt?: string | null;
    completedAt?: string | null;
}
