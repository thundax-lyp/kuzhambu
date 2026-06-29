import { postJson } from "@/api/http";
import type { Page, PageQuery } from "@/types/page";
import type { OperationsCleanupRecord } from "./cleanup-types";

export interface CleanupPageQuery {
    cleanupType?: string | null;
    cleanupStatus?: string | null;
    requesterUserId?: number | null;
}

export interface CleanupExecuteCommand {
    cleanupType: string;
}

export interface CleanupDetailCommand {
    cleanupId: number;
}

export const requestCleanup = (command: CleanupExecuteCommand) => {
    return postJson<OperationsCleanupRecord, CleanupExecuteCommand>("/operations/cleanup/execute", {
        body: command
    });
};

export const pageCleanups = (query: PageQuery<CleanupPageQuery> = {}) => {
    return postJson<Page<OperationsCleanupRecord>, PageQuery<CleanupPageQuery>>(
        "/operations/cleanup/page",
        {
            body: query
        }
    );
};

export const getCleanupDetail = (command: CleanupDetailCommand) => {
    return postJson<OperationsCleanupRecord, CleanupDetailCommand>("/operations/cleanup/detail", {
        body: command
    });
};
