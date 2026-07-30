import { postJson } from "@/api/http";
import type {
    DiscoverySearchStatisticsSummaryRecord,
    DiscoverySearchEventDetailRecord,
    DiscoverySearchEventPageRecord
} from "./search-statistics-types";

export interface DiscoverySearchEventPageQuery {
    dateFrom?: string | null;
    dateTo?: string | null;
    intentTypes?: string[] | null;
    operatorId?: string | null;
    pageNo?: number | null;
    pageSize?: number | null;
    queryText?: string | null;
    searchStatuses?: string[] | null;
}

export interface DiscoverySearchEventGetCommand {
    id: string;
}

export interface DiscoverySearchIndexRebuildCommand {
    confirm: boolean;
}

export interface DiscoverySearchStatisticsSummaryQuery {
    dateFrom?: string | null;
    dateTo?: string | null;
}

export const pageSearchEvents = (query: DiscoverySearchEventPageQuery = {}) => {
    return postJson<DiscoverySearchEventPageRecord, DiscoverySearchEventPageQuery>(
        "/discovery/search-statistics/events/page",
        {
            body: query
        }
    );
};

export const getSearchEventDetail = (command: DiscoverySearchEventGetCommand) => {
    return postJson<DiscoverySearchEventDetailRecord, DiscoverySearchEventGetCommand>(
        "/discovery/search-statistics/events/get",
        {
            body: command
        }
    );
};

export const rebuildSearchIndex = (command: DiscoverySearchIndexRebuildCommand) => {
    return postJson<number, DiscoverySearchIndexRebuildCommand>(
        "/discovery/search-statistics/index/rebuild",
        {
            body: command
        }
    );
};

export const getSearchStatisticsSummary = (query: DiscoverySearchStatisticsSummaryQuery = {}) => {
    return postJson<DiscoverySearchStatisticsSummaryRecord, DiscoverySearchStatisticsSummaryQuery>(
        "/discovery/search-statistics/summary",
        {
            body: query
        }
    );
};
