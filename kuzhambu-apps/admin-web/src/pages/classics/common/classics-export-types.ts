export type ClassicsExportStatus = "REQUESTED" | "RUNNING" | "COMPLETED" | "FAILED" | "EXPIRED";

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

export interface ClassicsExportScopePayloadItem {
    id: number;
    title: string;
    text: string;
    summary?: string | null;
    visibility?: string | null;
    category?: string | null;
    documentTime?: string | null;
    sourceFileStorageObjectId?: number | null;
}

export interface ClassicsExportScopePayload {
    title: string;
    contentType: "WANGQI_DOCUMENT" | "MING_CUSTOMS";
    scopeType: "SELECTED_ITEMS" | "FILTERED_RESULT";
    items: ClassicsExportScopePayloadItem[];
}

export interface ClassicsShowcaseJobRecord {
    id?: number | null;
    status?: string | null;
    requestedAt?: string | null;
    entryCount?: number | null;
    visibilityRiskStatus?: string | null;
    downloadUrl?: string | null;
}
