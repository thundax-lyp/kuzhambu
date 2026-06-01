import { getAccessToken } from "./token-storage";

export const toAuthenticatedResourceUrl = (
    resourceUrl?: string | null,
    accessToken: string | null = getAccessToken()
) => {
    if (!resourceUrl || !accessToken) {
        return resourceUrl || undefined;
    }

    const url = new URL(resourceUrl, window.location.origin);
    url.searchParams.set("token", accessToken);
    return url.origin === window.location.origin
        ? `${url.pathname}${url.search}${url.hash}`
        : url.toString();
};
