import { postJson } from "@/api/http";
import type { ClassicsSharePortalListResponse, ClassicsShareSearchQuery } from "./share-list-types";

export const listShares = (query: ClassicsShareSearchQuery = {}) => {
    return postJson<ClassicsSharePortalListResponse>("/portal/classics/shares/list", { ...query });
};
