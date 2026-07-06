export type OperationsDashboardPeriodType = "WEEK" | "MONTH" | "CUSTOM";
export type OperationsHealthTrendBucketType = "HOUR" | "DAY";

export interface OperationsBucketCountRecord {
    bucket?: string | null;
    count?: number | null;
}

export interface OperationsHealthSummaryRecord {
    checkId: number;
    component?: string | null;
    healthStatus?: string | null;
    latencyMs?: number | null;
    message?: string | null;
    probeSource?: string | null;
    probeTarget?: string | null;
    checkedAt?: string | null;
}

export interface OperationsTaskStatusSummaryRecord {
    taskStatus?: string | null;
    count?: number | null;
}

export interface OperationsTopContentRecord {
    contentId?: number | null;
    contentType?: string | null;
    title?: string | null;
    visitCount?: number | null;
}

export interface OperationsTopQueryRecord {
    queryText?: string | null;
    count?: number | null;
}

export interface OperationsTopTagRecord {
    tagName?: string | null;
    contentRefCount?: number | null;
}

export interface OperationsTopAiCapabilityRecord {
    capability?: string | null;
    invocationCount?: number | null;
}

export interface OperationsDashboardOverviewRecord {
    periodStart?: string | null;
    periodEnd?: string | null;
    contentCount?: number | null;
    translatedContentCount?: number | null;
    imageReadyContentCount?: number | null;
    visualAssetReadyContentCount?: number | null;
    shareVisitCount?: number | null;
    aiInvocationCount?: number | null;
    aiSucceededInvocationCount?: number | null;
    aiFailedInvocationCount?: number | null;
    aiAvgLatencyMs?: number | null;
    aiTotalCostAmount?: number | null;
    searchCount?: number | null;
    qaCount?: number | null;
    avgSearchLatencyMs?: number | null;
    tagCoverageRate?: number | null;
    unhealthyComponentCount?: number | null;
    runningTaskCount?: number | null;
    failedTaskCount?: number | null;
    contentGrowthSeries?: OperationsBucketCountRecord[] | null;
    searchTrendSeries?: OperationsBucketCountRecord[] | null;
    qaTrendSeries?: OperationsBucketCountRecord[] | null;
    tagGrowthSeries?: OperationsBucketCountRecord[] | null;
    healthSummaries?: OperationsHealthSummaryRecord[] | null;
    taskStatusSummaries?: OperationsTaskStatusSummaryRecord[] | null;
    topContents?: OperationsTopContentRecord[] | null;
    topQueries?: OperationsTopQueryRecord[] | null;
    topTags?: OperationsTopTagRecord[] | null;
    topAiCapabilities?: OperationsTopAiCapabilityRecord[] | null;
}

export interface OperationsHealthTrendBucketRecord {
    bucket?: string | null;
    upCount?: number | null;
    degradedCount?: number | null;
    downCount?: number | null;
    avgLatencyMs?: number | null;
}
