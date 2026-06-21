import { buildApiUrl, getJson } from "@/api/http";
import type {
    ClassicsSharePortalListResponse,
    ClassicsSharePortalResponse,
    ClassicsShareResourceContentUrlCommand,
    ClassicsShareSearchQuery
} from "./share-types";

export const listShares = (query: ClassicsShareSearchQuery = {}) => {
    return getJson<ClassicsSharePortalListResponse>("/portal/classics/shares", { ...query });
};

export const getShare = (shareToken: string) => {
    return getJson<ClassicsSharePortalResponse>(`/portal/classics/shares/${shareToken}`);
};

export const getShareResourceContentUrl = (command: ClassicsShareResourceContentUrlCommand) => {
    return buildApiUrl(
        `/portal/classics/shares/${command.shareToken}/resources/${command.storageObjectId}/content`,
        {
            download: command.mode === "download" ? "true" : undefined
        }
    );
};
