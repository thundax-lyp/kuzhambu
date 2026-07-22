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
    contentType?: ClassicsShareContentType | string | null;
    contentVersionId?: number | null;
    contentVersionNo?: number | null;
    contentVisibilitySnapshot?: ClassicsShareVisibility | string | null;
    priority?: number | null;
    images?: ClassicsSharePortalImage[] | null;
    storageObject?: ClassicsShareResource | null;
    targetStatus?: ClassicsShareTargetStatus | string | null;
    titleSnapshot?: string | null;
}

export type ClassicsShareTargetStatus = "ACTIVE" | "AVAILABLE" | "CONTENT_DELETED";

export type ClassicsShareResourceContentMode = "download" | "preview";

export type ClassicsShareContentType = "MING_CUSTOMS" | "SANCAI_ENTRY" | "WANGQI_DOCUMENT";

export type ClassicsShareVisibility = "PRIVATE" | "PUBLIC";

export type ClassicsShareLinkStatus = "ACTIVE" | "EXPIRED" | "REVOKED";

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
    status?: ClassicsShareLinkStatus | string | null;
    targets?: ClassicsSharePortalTarget[] | null;
    title?: string | null;
    visibility?: ClassicsShareVisibility | string | null;
}
