import { postJson } from "@/api/http";
import type {
    OperationsDashboardOverviewRecord,
    OperationsDashboardPeriodType,
    OperationsHealthTrendBucketRecord,
    OperationsHealthTrendBucketType
} from "./dashboard-types";

export interface OperationsDashboardOverviewQuery {
    periodType?: OperationsDashboardPeriodType | null;
    periodStart?: string | null;
    periodEnd?: string | null;
}

export interface OperationsHealthTrendQuery {
    component?: string | null;
    probeSource?: string | null;
    periodStart?: string | null;
    periodEnd?: string | null;
    bucketType?: OperationsHealthTrendBucketType | null;
}

export const getDashboardOverview = (query: OperationsDashboardOverviewQuery = {}) => {
    return postJson<OperationsDashboardOverviewRecord, OperationsDashboardOverviewQuery>(
        "/operations/dashboard/overview",
        {
            body: query
        }
    );
};

export const getHealthTrend = (query: OperationsHealthTrendQuery = {}) => {
    return postJson<OperationsHealthTrendBucketRecord[], OperationsHealthTrendQuery>(
        "/operations/health/trend",
        {
            body: query
        }
    );
};
