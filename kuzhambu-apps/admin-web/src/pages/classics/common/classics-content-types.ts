export type ClassicsContentType = "SANCAI_ENTRY" | "WANGQI_DOCUMENT" | "MING_CUSTOMS" | string;
export type ClassicsContentVisibility = "PRIVATE" | "PUBLIC";
export type ClassicsContentPermissionAction = "edit" | "export" | "share";

export const CLASSICS_CONTENT_EXPORT_PERMISSION = "classics:content:export";
export const CLASSICS_SHARING_EDIT_PERMISSION = "classics:sharing:edit";

const CLASSICS_CONTENT_VIEW_PERMISSIONS: Record<string, string> = {
    MING_CUSTOMS: "classics:mingcustoms:view",
    SANCAI_ENTRY: "classics:sancai:view",
    WANGQI_DOCUMENT: "classics:wangqi:view"
};

const CLASSICS_CONTENT_EDIT_PERMISSIONS: Record<string, string> = {
    MING_CUSTOMS: "classics:mingcustoms:edit",
    SANCAI_ENTRY: "classics:sancai:edit",
    WANGQI_DOCUMENT: "classics:wangqi:edit"
};

export const hasClassicsContentPermission = (
    contentType: ClassicsContentType,
    action: ClassicsContentPermissionAction,
    hasPermission: (permission: string) => boolean
) => {
    const viewPermission = CLASSICS_CONTENT_VIEW_PERMISSIONS[contentType];
    const editPermission = CLASSICS_CONTENT_EDIT_PERMISSIONS[contentType];

    if (action === "edit") {
        return Boolean(editPermission && hasPermission(editPermission));
    }

    if (!viewPermission || !hasPermission(viewPermission)) {
        return false;
    }

    if (action === "export") {
        return hasPermission(CLASSICS_CONTENT_EXPORT_PERMISSION);
    }

    return hasPermission(CLASSICS_SHARING_EDIT_PERMISSION);
};

export interface ClassicsContentRef {
    contentId: number;
    contentType: ClassicsContentType;
}

export interface ClassicsContentTagRecord {
    id?: number | null;
    tagId?: number | null;
    contentType?: ClassicsContentType | null;
    contentId?: number | null;
    tagNameSnapshot?: string | null;
    status?: string | null;
    source?: string | null;
}

export interface ClassicsContentTagPayload extends ClassicsContentRef {
    id?: number | null;
    tagId?: number | null;
    tagNameSnapshot: string;
    status?: string | null;
    source?: string | null;
}

export interface ClassicsContentTagSortPayload extends ClassicsContentRef {
    orderedIds: number[];
    sortDirection?: string | null;
}

export interface ClassicsContentQaPairRecord {
    id?: number | null;
    contentType?: ClassicsContentType | null;
    contentId?: number | null;
    question?: string | null;
    answer?: string | null;
    source?: string | null;
}

export interface ClassicsContentQaPairPayload extends ClassicsContentRef {
    id?: number | null;
    question: string;
    answer: string;
    source?: string | null;
}

export interface ClassicsContentQaPairSortPayload {
    orderedIds: number[];
    sortDirection?: string | null;
}

export interface ClassicsContentListPayload {
    contentType: ClassicsContentType;
    contentId: number;
}

export interface ClassicsBatchVisibilityPayload {
    contentIds: number[];
    contentType: ClassicsContentType;
    visibility: ClassicsContentVisibility;
}

export interface ClassicsBatchOperationItemRecord {
    contentId: number | null;
    contentType: ClassicsContentType | string | null;
    failureCode?: string | null;
    failureReason?: string | null;
    resultId?: number | null;
    status?: string | null;
}

export interface ClassicsBatchOperationRecord {
    failureCount: number;
    failures: ClassicsBatchOperationItemRecord[];
    successCount: number;
    successes: ClassicsBatchOperationItemRecord[];
}
