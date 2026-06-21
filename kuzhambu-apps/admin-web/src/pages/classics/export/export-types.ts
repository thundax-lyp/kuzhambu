export type ClassicsExportStatus =
    | "REQUESTED"
    | "RUNNING"
    | "COMPLETED"
    | "FAILED"
    | "EXPIRED";

export interface ClassicsExportJobRecord {
    id?: number | null;
    contentType?: string | null;
    exportKind?: string | null;
    exportFormat?: string | null;
    scopeType?: string | null;
    scopeJson?: string | null;
    requestedAt?: string | null;
    expiresAt?: string | null;
    status?: ClassicsExportStatus | null;
    storageObjectId?: number | null;
    itemCount?: number | null;
    assetCount?: number | null;
    visibilityRiskStatus?: string | null;
    contentChanged?: boolean | null;
    contentUrl?: string | null;
    downloadUrl?: string | null;
}
