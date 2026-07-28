export type ClassicsShareContentType = "MING_CUSTOMS" | "SANCAI_ENTRY" | "WANGQI_DOCUMENT";

export type ClassicsShareLinkStatus = "ACTIVE" | "EXPIRED" | "REVOKED";

export type ClassicsShareTargetStatus = "AVAILABLE" | "CONTENT_DELETED";

export type ClassicsShareVisibility = "PRIVATE" | "PUBLIC";

export type ClassicsShareAccessType = "DETAIL_VIEW" | "RESOURCE_READ";

export interface ClassicsShareAccessClientSnapshot {
    accessType?: ClassicsShareAccessType | string | null;
    download?: boolean | null;
    privateAccess?: boolean | null;
    storageObjectId?: string | null;
}

export interface ClassicsShareTargetRef {
    contentId: string;
    contentType: ClassicsShareContentType;
}

export interface ClassicsBatchOperationItemRecord {
    contentId: string;
    contentType: ClassicsShareContentType;
    failureCode?: string | null;
    failureReason?: string | null;
    resultId?: string | null;
    status?: string | null;
}

export interface ClassicsBatchOperationRecord {
    failureCount: number;
    failures: ClassicsBatchOperationItemRecord[];
    successCount: number;
    successes: ClassicsBatchOperationItemRecord[];
}

export interface ClassicsShareTargetRecord {
    contentChangedAfterShare?: boolean | null;
    contentId?: string | null;
    contentType?: ClassicsShareContentType | string | null;
    contentVersionId?: string | null;
    contentVersionNo?: number | null;
    contentVisibilitySnapshot?: string | null;
    currentContentVersionId?: string | null;
    currentContentVersionNo?: number | null;
    id?: string | null;
    priority?: number | null;
    targetStatus?: ClassicsShareTargetStatus | string | null;
    titleSnapshot?: string | null;
}

export interface ClassicsShareRecord {
    accessCount?: number | null;
    expiresAt?: string | null;
    id: string;
    issuedAt?: string | null;
    shareToken: string;
    shareUrl: string;
    status?: ClassicsShareLinkStatus | string | null;
    targets?: ClassicsShareTargetRecord[] | null;
    title?: string | null;
    visibility?: ClassicsShareVisibility | string | null;
}

export interface ClassicsShareAccessRecord {
    accessResult?: string | null;
    accessedAt?: string | null;
    clientSnapshot?: string | null;
    id?: string | null;
    shareLinkId?: string | null;
    shareTargetId?: string | null;
}
