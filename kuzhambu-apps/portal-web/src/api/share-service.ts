import { getJson } from "./http";
import type {
    ClassicsSharePortalListResponse,
    ClassicsSharePortalResponse,
    ClassicsShareSearchQuery
} from "./share-types";

export const listShares = (query: ClassicsShareSearchQuery = {}) => {
    return getJson<ClassicsSharePortalListResponse>("/portal/classics/shares", { ...query });
};

export const getShare = (shareToken: string) => {
    return getJson<ClassicsSharePortalResponse>(`/portal/classics/shares/${shareToken}`);
};
