import { postJson } from "@/api/http";
import type {
    OperationsHealthAlertLevel,
    OperationsHealthAlertRecord,
    OperationsHealthAlertStatus,
    OperationsHealthRecord,
    OperationsHealthStatus,
    OperationsPageRecord
} from "./health-types";

export interface OperationsHealthPageQuery {
    component?: string | null;
    healthStatus?: OperationsHealthStatus | null;
    probeSource?: string | null;
    probeTarget?: string | null;
    checkedAtStart?: string | null;
    checkedAtEnd?: string | null;
    pageNo?: number | null;
    pageSize?: number | null;
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

export const getOperationsHealthPage = (query: OperationsHealthPageQuery = {}) => {
    return postJson<OperationsPageRecord<OperationsHealthRecord>, OperationsHealthPageQuery>(
        "/operations/health/page",
        {
            body: query
        }
    );
};

export interface OperationsHealthAlertActionCommand {
    alertId: string;
}

export const getOperationsHealthAlerts = (query: OperationsHealthAlertPageQuery = {}) => {
    return postJson<
        OperationsPageRecord<OperationsHealthAlertRecord>,
        OperationsHealthAlertPageQuery
    >("/operations/health/alerts/page", {
        body: query
    });
};

export const confirmOperationsHealthAlert = (command: OperationsHealthAlertActionCommand) => {
    return postJson<void, OperationsHealthAlertActionCommand>("/operations/health/alerts/ack", {
        body: command
    });
};

export const recoverOperationsHealthAlert = (command: OperationsHealthAlertActionCommand) => {
    return postJson<void, OperationsHealthAlertActionCommand>("/operations/health/alerts/recover", {
        body: command
    });
};
