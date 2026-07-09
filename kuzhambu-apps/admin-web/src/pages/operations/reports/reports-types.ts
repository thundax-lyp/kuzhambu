export type OperationsReportType = "WEEKLY" | "MONTHLY";
export type OperationsReportFormat = "HTML" | "PDF";
export type OperationsReportStatus = "PENDING" | "RUNNING" | "SUCCEEDED" | "FAILED";

export interface OperationsReportRecord {
    reportId: number;
    reportType?: string | null;
    format?: string | null;
    periodStart?: string | null;
    periodEnd?: string | null;
    storageObjectId?: number | null;
    artifactFilename?: string | null;
    reportStatus?: string | null;
    failureReason?: string | null;
    requesterUserId?: number | null;
    requestedAt?: string | null;
    completedAt?: string | null;
}

export interface OperationsReportDetailRecord extends OperationsReportRecord {
    requestId?: string | null;
    traceId?: string | null;
    templateVersion?: string | null;
}
