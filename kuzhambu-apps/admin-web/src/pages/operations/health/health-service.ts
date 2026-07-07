import { postJson } from "@/api/http";
import type {
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
    alertLevel?: "WARNING" | "CRITICAL" | null;
    alertStatus?: "ACTIVE" | "ACKED" | "RECOVERED" | null;
    sourceRefType?: string | null;
    sourceRefId?: number | null;
    latestCheckId?: number | null;
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
