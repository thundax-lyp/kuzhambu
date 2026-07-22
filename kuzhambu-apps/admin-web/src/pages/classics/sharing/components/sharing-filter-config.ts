import type {
    ClassicsShareContentType,
    ClassicsShareLinkStatus,
    ClassicsShareVisibility
} from "@/pages/classics/common/classics-share-types";

export type ShareContentTypeFilter = "ALL" | ClassicsShareContentType;
export type ShareStatusFilter = "ALL" | ClassicsShareLinkStatus;
export type ShareVisibilityFilter = "ALL" | ClassicsShareVisibility;

export interface ShareFilters {
    contentType: ShareContentTypeFilter;
    status: ShareStatusFilter;
    visibility: ShareVisibilityFilter;
}

export const DEFAULT_FILTERS: ShareFilters = {
    contentType: "ALL",
    status: "ALL",
    visibility: "ALL"
};
