import { postJson } from "@/api/http";
import type {
    OperationsDashboardOverviewRecord,
    OperationsDashboardPeriodType,
    OperationsHealthAlertRecord,
    OperationsHealthAlertLevel,
    OperationsHealthAlertStatus,
    OperationsPageRecord,
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

export interface OperationsHealthAlertPageQuery {
    component?: string | null;
    alertLevel?: OperationsHealthAlertLevel | null;
    alertStatus?: OperationsHealthAlertStatus | null;
    sourceRefType?: string | null;
    sourceRefId?: string | null;
    latestCheckId?: string | null;
    pageNo?: number | null;
    pageSize?: number | null;
}

export interface OperationsHealthAlertActionCommand {
    alertId: string;
}

export const getDashboardOverview = (query: OperationsDashboardOverviewQuery = {}) => {
    return postJson<OperationsDashboardOverviewRecord, OperationsDashboardOverviewQuery>(
        "/operations/dashboard/get",
        {
            body: query
        }
    );
};

export const getHealthAlerts = (query: OperationsHealthAlertPageQuery = {}) => {
    return postJson<
        OperationsPageRecord<OperationsHealthAlertRecord>,
        OperationsHealthAlertPageQuery
    >("/operations/health/alerts/page", {
        body: query
    });
};

export const confirmHealthAlert = (command: OperationsHealthAlertActionCommand) => {
    return postJson<void, OperationsHealthAlertActionCommand>("/operations/health/alerts/confirm", {
        body: command
    });
};

export const recoverHealthAlert = (command: OperationsHealthAlertActionCommand) => {
    return postJson<void, OperationsHealthAlertActionCommand>("/operations/health/alerts/recover", {
        body: command
    });
};

export const getHealthTrend = (query: OperationsHealthTrendQuery = {}) => {
    return postJson<OperationsHealthTrendBucketRecord[], OperationsHealthTrendQuery>(
        "/operations/health/get",
        {
            body: query
        }
    );
};
