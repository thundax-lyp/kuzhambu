export type ClassicsExportStatus = "REQUESTED" | "RUNNING" | "COMPLETED" | "FAILED" | "EXPIRED";

export interface ClassicsExportJobRecord {
    id?: string | null;
    contentType?: string | null;
    exportKind?: string | null;
    exportFormat?: string | null;
    scopeType?: string | null;
    scopeJson?: string | null;
    requestedAt?: string | null;
    expiresAt?: string | null;
    status?: ClassicsExportStatus | null;
    storageObjectId?: string | null;
    itemCount?: number | null;
    assetCount?: number | null;
    visibilityRiskStatus?: string | null;
    contentChanged?: boolean | null;
    contentUrl?: string | null;
    downloadUrl?: string | null;
}

export interface ClassicsExportScopePayloadItem {
    id: string;
    title: string;
    text: string;
    summary?: string | null;
    visibility?: string | null;
    category?: string | null;
    documentTime?: string | null;
    sourceFileStorageObjectId?: string | null;
}

export interface ClassicsExportScopePayload {
    title: string;
    contentType: "WANGQI_DOCUMENT" | "MING_CUSTOMS";
    scopeType: "SELECTED_ITEMS" | "FILTERED_RESULT";
    items: ClassicsExportScopePayloadItem[];
}
