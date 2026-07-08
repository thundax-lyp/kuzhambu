export type OperationsHealthStatus = "UP" | "DEGRADED" | "DOWN";

export interface OperationsHealthRecord {
    checkId: number;
    component?: string | null;
    healthStatus?: OperationsHealthStatus | null;
    latencyMs?: number | null;
    message?: string | null;
    probeSource?: string | null;
    probeTarget?: string | null;
    detailsJson?: string | null;
    checkedAt?: string | null;
}

export type OperationsHealthAlertLevel = "WARNING" | "CRITICAL";
export type OperationsHealthAlertStatus = "ACTIVE" | "ACKED" | "RECOVERED";

export interface OperationsHealthAlertRecord {
    alertId: number;
    component?: string | null;
    alertType?: string | null;
    alertLevel?: OperationsHealthAlertLevel | null;
    alertStatus?: OperationsHealthAlertStatus | null;
    sourceRefType?: string | null;
    sourceRefId?: number | null;
    latestCheckId?: number | null;
    message?: string | null;
    suggestion?: string | null;
    recoveryAction?: string | null;
    recoveryTarget?: string | null;
    firstTriggeredAt?: string | null;
    lastTriggeredAt?: string | null;
    ackedAt?: string | null;
    ackedByUserId?: number | null;
    recoveredAt?: string | null;
    failureReason?: string | null;
}

export interface OperationsPageRecord<TRecord> {
    pageNo?: number | null;
    pageSize?: number | null;
    count?: number | null;
    records?: TRecord[] | null;
}
