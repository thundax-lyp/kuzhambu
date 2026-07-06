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

export interface ClassicsShareResource {
    contentType?: string | null;
    downloadUrl?: string | null;
    originalFilename?: string | null;
    previewUrl?: string | null;
    size?: number | null;
    storageObjectId?: number | null;
}

export interface ClassicsSharePortalImage {
    contentType?: string | null;
    currentUsed?: boolean | null;
    imageId?: number | null;
    imageType?: string | null;
    originalFilename?: string | null;
    priority?: number | null;
    size?: number | null;
    storageObject?: ClassicsShareResource | null;
    storageObjectId?: number | null;
    previewUrl?: string | null;
    downloadUrl?: string | null;
    title?: string | null;
}

export interface ClassicsSharePortalTarget {
    contentId?: number | null;
    contentSnapshotJson?: string | null;
    contentType?: string | null;
    contentVersionId?: number | null;
    contentVersionNo?: number | null;
    contentVisibilitySnapshot?: string | null;
    priority?: number | null;
    images?: ClassicsSharePortalImage[] | null;
    storageObject?: ClassicsShareResource | null;
    targetStatus?: ClassicsShareTargetStatus | string | null;
    titleSnapshot?: string | null;
}

export type ClassicsShareTargetStatus = "ACTIVE" | "AVAILABLE" | "CONTENT_DELETED";

export type ClassicsShareResourceContentMode = "download" | "preview";

export interface ClassicsShareResourceContentUrlCommand {
    mode?: ClassicsShareResourceContentMode;
    privateAccess?: boolean;
    shareToken: string;
    storageObjectId: number;
}

export interface ClassicsSharePortalResponse {
    expiresAt?: string | null;
    issuedAt?: string | null;
    loginRequired?: boolean | null;
    status?: string | null;
    targets?: ClassicsSharePortalTarget[] | null;
    title?: string | null;
    visibility?: string | null;
}
