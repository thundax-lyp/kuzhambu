import { buildApiUrl, getJson, getJsonWithAccessToken } from "@/api/http";
import type {
    ClassicsSharePortalListResponse,
    ClassicsSharePortalResponse,
    ClassicsShareResourceContentUrlCommand,
    ClassicsShareSearchQuery
} from "./share-types";

const ACCESS_TOKEN_KEYS = ["kuzhambu.portal.accessToken", "kuzhambu.admin.accessToken"];

export const getAccessToken = () => {
    if (typeof window === "undefined") {
        return null;
    }
    for (const key of ACCESS_TOKEN_KEYS) {
        const token = window.localStorage.getItem(key);
        if (token?.trim()) {
            return token;
        }
    }
    return null;
};

export const listShares = (query: ClassicsShareSearchQuery = {}) => {
    return getJson<ClassicsSharePortalListResponse>("/portal/classics/shares", { ...query });
};

export const getShare = (shareToken: string) => {
    return getJson<ClassicsSharePortalResponse>(`/portal/classics/shares/${shareToken}`);
};

export const getPrivateShare = (shareToken: string, accessToken: string) => {
    return getJsonWithAccessToken<ClassicsSharePortalResponse>(
        `/portal/classics/private-shares/${shareToken}`,
        accessToken
    );
};

export const getAccessibleShare = async (shareToken: string) => {
    const publicShare = await getShare(shareToken);
    if (!publicShare?.loginRequired) {
        return publicShare;
    }
    const accessToken = getAccessToken();
    if (!accessToken) {
        return publicShare;
    }
    return getPrivateShare(shareToken, accessToken);
};

export const getShareResourceContentUrl = (command: ClassicsShareResourceContentUrlCommand) => {
    const accessToken = command.privateAccess ? getAccessToken() : null;
    const path = command.privateAccess
        ? `/portal/classics/private-shares/${command.shareToken}/resources/${command.storageObjectId}/content`
        : `/portal/classics/shares/${command.shareToken}/resources/${command.storageObjectId}/content`;
    return buildApiUrl(path, {
        download: command.mode === "download" ? "true" : undefined,
        token: accessToken ?? undefined
    });
};
