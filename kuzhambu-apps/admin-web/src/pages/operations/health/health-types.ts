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

export interface OperationsPageRecord<TRecord> {
    pageNo?: number | null;
    pageSize?: number | null;
    count?: number | null;
    records?: TRecord[] | null;
}
