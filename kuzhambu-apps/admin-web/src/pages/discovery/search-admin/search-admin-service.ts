import { postJson } from "@/api/http";
import type {
    DiscoverySearchLogDetailRecord,
    DiscoverySearchLogPageRecord
} from "./search-admin-types";

export interface DiscoverySearchLogPageQuery {
    dateFrom?: string | null;
    dateTo?: string | null;
    intentTypes?: string[] | null;
    operatorId?: string | null;
    pageNo?: number | null;
    pageSize?: number | null;
    queryText?: string | null;
    searchStatuses?: string[] | null;
}

export interface DiscoverySearchLogGetCommand {
    searchLogId: string;
}

export interface DiscoverySearchIndexRebuildCommand {
    confirm: boolean;
}

export const pageSearchLogs = (query: DiscoverySearchLogPageQuery = {}) => {
    return postJson<DiscoverySearchLogPageRecord, DiscoverySearchLogPageQuery>(
        "/discovery/search-admin/logs/page",
        {
            body: query
        }
    );
};

export const getSearchLogDetail = (command: DiscoverySearchLogGetCommand) => {
    return postJson<DiscoverySearchLogDetailRecord, DiscoverySearchLogGetCommand>(
        "/discovery/search-admin/logs/get",
        {
            body: command
        }
    );
};

export const rebuildSearchIndex = (command: DiscoverySearchIndexRebuildCommand) => {
    return postJson<number, DiscoverySearchIndexRebuildCommand>(
        "/discovery/search-admin/index/rebuild",
        {
            body: command
        }
    );
};
