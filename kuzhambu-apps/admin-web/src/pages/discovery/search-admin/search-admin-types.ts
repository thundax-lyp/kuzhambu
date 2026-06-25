import type { Page } from "@/types/page";

export interface DiscoverySearchLogRecord {
    createdAt?: string | null;
    displayQueryText?: string | null;
    groupTotalCount?: number | null;
    intentType?: string | null;
    operatorId?: string | null;
    queryText?: string | null;
    resultTotalCount?: number | null;
    searchLogId?: string | null;
    searchStatus?: string | null;
}

export interface DiscoverySearchLogDetailRecord {
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
    searchLogId?: string | null;
    searchScopesJson?: string | null;
    searchStatus?: string | null;
    traceId?: string | null;
}

export type DiscoverySearchLogPageRecord = Page<DiscoverySearchLogRecord>;
