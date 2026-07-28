import type { AiCandidateApplyPayload } from "./ai-candidate-types";

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
    contentId: string;
    contentType: ClassicsContentType;
}

export interface ClassicsContentTagRecord {
    id?: string | null;
    tagId?: string | null;
    contentType?: ClassicsContentType | null;
    contentId?: string | null;
    tagNameSnapshot?: string | null;
    status?: string | null;
    source?: string | null;
    priority?: number | null;
}

export interface ClassicsContentTagPayload extends ClassicsContentRef {
    id?: string | null;
    tagId?: string | null;
    tagNameSnapshot: string;
    status?: string | null;
    source?: string | null;
}

export interface ClassicsContentTagSortPayload extends ClassicsContentRef {
    orderedIds: string[];
    sortDirection?: string | null;
}

export interface ClassicsContentTagDeletePayload {
    id: string;
}

export interface ClassicsContentQaPairRecord {
    id?: string | null;
    contentType?: ClassicsContentType | null;
    contentId?: string | null;
    question?: string | null;
    answer?: string | null;
    source?: string | null;
}

export interface ClassicsContentQaPairPayload extends ClassicsContentRef {
    id?: string | null;
    question: string;
    answer: string;
    source?: string | null;
}

export interface ClassicsContentQaPairDeletePayload {
    id: string;
}

export interface ClassicsContentQaPairSortPayload {
    orderedIds: string[];
    sortDirection?: string | null;
}

export interface ClassicsContentListPayload {
    contentType: ClassicsContentType;
    contentId: string;
}

export interface ClassicsBatchVisibilityPayload {
    contentIds: string[];
    contentType: ClassicsContentType;
    visibility: ClassicsContentVisibility;
}

export interface ClassicsBatchOperationItemRecord {
    candidateId?: string | null;
    objectId?: string | null;
    capability?: string | null;
    contentId: string | null;
    contentType: ClassicsContentType | string | null;
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

export interface ClassicsAiCandidateBatchApplyPayload {
    items: AiCandidateApplyPayload[];
}

export interface ClassicsAiCandidateBatchRejectItemPayload {
    candidateId: string;
    contentType: ClassicsContentType;
    contentId: string;
    capability: string;
    objectId?: string | null;
}

export interface ClassicsAiCandidateBatchRejectPayload {
    errorMessage?: string | null;
    errorType?: string | null;
    items: ClassicsAiCandidateBatchRejectItemPayload[];
}
