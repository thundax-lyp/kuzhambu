import { postJson } from "@/api/http";
import type { Page, PageQuery } from "@/types/page";
import type {
    OperationsBackupRecord,
    OperationsRestoreMode,
    OperationsRestoreRecord
} from "./backup-restore-types";

export interface BackupLedgerQuery {
    backupType?: string | null;
    backupStatus?: string | null;
    requesterUserId?: string | null;
}

export interface BackupDetailCommand {
    backupId: string;
}

export interface RestoreExecuteCommand {
    backupId: string;
    restoreMode: OperationsRestoreMode;
}

export interface RestoreLedgerQuery {
    backupId?: string | null;
    restoreMode?: OperationsRestoreMode | null;
    restoreStatus?: string | null;
    requesterUserId?: string | null;
}

export interface RestoreDetailCommand {
    restoreId: string;
}

export const createManualBackup = () => {
    return postJson<OperationsBackupRecord, Record<string, never>>("/operations/backup/execute", {
        body: {}
    });
};

export const pageBackups = (query: PageQuery<BackupLedgerQuery> = {}) => {
    return postJson<Page<OperationsBackupRecord>, PageQuery<BackupLedgerQuery>>(
        "/operations/backup/page",
        {
            body: query
        }
    );
};

export const getBackupDetail = (command: BackupDetailCommand) => {
    return postJson<OperationsBackupRecord, BackupDetailCommand>("/operations/backup/detail", {
        body: command
    });
};

export const recoverBackup = (command: RestoreExecuteCommand) => {
    return postJson<OperationsRestoreRecord, RestoreExecuteCommand>("/operations/restore/execute", {
        body: command
    });
};

export const pageRestores = (query: PageQuery<RestoreLedgerQuery> = {}) => {
    return postJson<Page<OperationsRestoreRecord>, PageQuery<RestoreLedgerQuery>>(
        "/operations/restore/page",
        {
            body: query
        }
    );
};

export const getRestoreDetail = (command: RestoreDetailCommand) => {
    return postJson<OperationsRestoreRecord, RestoreDetailCommand>("/operations/restore/detail", {
        body: command
    });
};
