export interface ClassicsExportJobRecord {
    id?: number | null;
    status?: string | null;
    requestedAt?: string | null;
    expiresAt?: string | null;
    itemCount?: number | null;
    assetCount?: number | null;
    downloadUrl?: string | null;
}

export interface ClassicsShowcaseJobRecord {
    id?: number | null;
    status?: string | null;
    requestedAt?: string | null;
    entryCount?: number | null;
    visibilityRiskStatus?: string | null;
    downloadUrl?: string | null;
}
