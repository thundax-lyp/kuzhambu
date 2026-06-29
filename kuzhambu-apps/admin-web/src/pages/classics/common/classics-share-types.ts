export type ClassicsShareContentType = "MING_CUSTOMS" | "SANCAI_ENTRY" | "WANGQI_DOCUMENT";

export type ClassicsShareLinkStatus = "ACTIVE" | "EXPIRED" | "REVOKED";

export type ClassicsShareVisibility = "PRIVATE" | "PUBLIC";

export interface ClassicsShareTargetRef {
    contentId: number;
    contentType: ClassicsShareContentType;
}

export interface ClassicsShareTargetRecord {
    contentChangedAfterShare?: boolean | null;
    contentId?: number | null;
    contentType?: ClassicsShareContentType | string | null;
    contentVersionId?: number | null;
    contentVersionNo?: number | null;
    contentVisibilitySnapshot?: string | null;
    currentContentVersionId?: number | null;
    currentContentVersionNo?: number | null;
    id?: number | null;
    priority?: number | null;
    targetStatus?: string | null;
    titleSnapshot?: string | null;
}

export interface ClassicsShareRecord {
    accessCount?: number | null;
    expiresAt?: string | null;
    id: number;
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
    id?: number | null;
    shareLinkId?: number | null;
    shareTargetId?: number | null;
}
