export interface ClassicsShareSearchQuery {
    contentType?: ClassicsShareContentType | string | null;
    issuedAfter?: string | null;
    issuedBefore?: string | null;
    pageNo?: number | null;
    pageSize?: number | null;
    title?: string | null;
}

export interface ClassicsSharePortalListItem {
    contentId?: number | null;
    contentType?: ClassicsShareContentType | string | null;
    contentVersionId?: number | null;
    contentVersionNo?: number | null;
    contentVisibilitySnapshot?: ClassicsShareVisibility | string | null;
    expiresAt?: string | null;
    issuedAt?: string | null;
    priority?: number | null;
    shareLinkId?: number | null;
    shareToken?: string | null;
    shareTitle?: string | null;
    targetStatus?: ClassicsShareTargetStatus | string | null;
    titleSnapshot?: string | null;
}

export interface ClassicsSharePortalListResponse {
    pageNo: number;
    pageSize: number;
    records: ClassicsSharePortalListItem[];
    totalCount: number;
    totalPage: number;
}

export type ClassicsShareContentType = "MING_CUSTOMS" | "SANCAI_ENTRY" | "WANGQI_DOCUMENT";

export type ClassicsShareVisibility = "PRIVATE" | "PUBLIC";

export type ClassicsShareTargetStatus = "ACTIVE" | "AVAILABLE" | "CONTENT_DELETED";
