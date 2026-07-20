import type { Page } from "@/types/page";

export interface DiscoverySearchEventRecord {
    createdAt?: string | null;
    displayQueryText?: string | null;
    groupTotalCount?: number | null;
    intentType?: string | null;
    operatorId?: string | null;
    queryText?: string | null;
    resultTotalCount?: number | null;
    searchEventId?: string | null;
    searchStatus?: string | null;
}

export interface DiscoverySearchEventDetailRecord {
    createdAt?: string | null;
    displayQueryText?: string | null;
    failureCode?: string | null;
    failureMessage?: string | null;
    groupTotalCount?: number | null;
    intentType?: string | null;
    normalizedQueryText?: string | null;
    operatorId?: string | null;
    queryText?: string | null;
    requestId?: string | null;
    resultTotalCount?: number | null;
    searchEventId?: string | null;
    searchScopesJson?: string | null;
    searchStatus?: string | null;
    traceId?: string | null;
}

export type DiscoverySearchEventPageRecord = Page<DiscoverySearchEventRecord>;

export interface DiscoverySearchStatisticsTopQueryRecord {
    count?: number | null;
    queryText?: string | null;
}

export interface DiscoverySearchStatisticsSummaryRecord {
    clickCount?: number | null;
    failedSearchCount?: number | null;
    searchCount?: number | null;
    topQueries?: DiscoverySearchStatisticsTopQueryRecord[] | null;
    zeroResultSearchCount?: number | null;
}
