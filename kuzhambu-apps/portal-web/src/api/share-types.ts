export interface ClassicsShareSearchQuery {
    contentType?: string | null;
    issuedAfter?: string | null;
    issuedBefore?: string | null;
    pageNo?: number | null;
    pageSize?: number | null;
    title?: string | null;
}

export interface ClassicsSharePortalListItem {
    contentId?: number | null;
    contentType?: string | null;
    contentVersionId?: number | null;
    contentVersionNo?: number | null;
    contentVisibilitySnapshot?: string | null;
    expiresAt?: string | null;
    issuedAt?: string | null;
    priority?: number | null;
    shareLinkId?: number | null;
    shareTitle?: string | null;
    targetStatus?: string | null;
    titleSnapshot?: string | null;
}

export interface ClassicsSharePortalListResponse {
    pageNo: number;
    pageSize: number;
    records: ClassicsSharePortalListItem[];
    totalCount: number;
    totalPage: number;
}

export interface ClassicsSharePortalTarget {
    contentId?: number | null;
    contentSnapshotJson?: string | null;
    contentType?: string | null;
    contentVersionId?: number | null;
    contentVersionNo?: number | null;
    contentVisibilitySnapshot?: string | null;
    priority?: number | null;
    targetStatus?: string | null;
    titleSnapshot?: string | null;
}

export interface ClassicsSharePortalResponse {
    expiresAt?: string | null;
    issuedAt?: string | null;
    status?: string | null;
    targets?: ClassicsSharePortalTarget[] | null;
    title?: string | null;
    visibility?: string | null;
}
