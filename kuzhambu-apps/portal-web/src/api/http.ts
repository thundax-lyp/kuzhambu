const PORTAL_API_BASE_URL = import.meta.env.VITE_PORTAL_API_BASE_URL || "/kuzhambu-api/api";

interface ApiResponse<T> {
    code?: string;
    data?: T;
    message?: string;
}

export const buildApiUrl = (
    path: string,
    query?: Record<string, string | number | null | undefined>
) => {
    const normalizedBaseUrl = PORTAL_API_BASE_URL.replace(/\/+$/, "");
    const normalizedPath = path.startsWith("/") ? path : `/${path}`;
    const url = new URL(`${normalizedBaseUrl}${normalizedPath}`, window.location.origin);

    Object.entries(query ?? {}).forEach(([key, value]) => {
        if (value !== null && value !== undefined && value !== "") {
            url.searchParams.set(key, String(value));
        }
    });

    if (url.origin === window.location.origin) {
        return `${url.pathname}${url.search}`;
    }

    return url.toString();
};

export const getJson = async <T>(
    path: string,
    query?: Record<string, string | number | null | undefined>
) => {
    const response = await fetch(buildApiUrl(path, query), {
        headers: {
            Accept: "application/json"
        },
        method: "GET"
    });

    if (!response.ok) {
        throw new Error("Portal API request failed");
    }

    const payload = (await response.json()) as ApiResponse<T>;
    return payload.data as T;
};

export const getJsonWithAccessToken = async <T>(
    path: string,
    accessToken: string,
    query?: Record<string, string | number | null | undefined>
) => {
    const response = await fetch(buildApiUrl(path, query), {
        headers: {
            Accept: "application/json",
            "Access-Token": accessToken
        },
        method: "GET"
    });

    if (!response.ok) {
        throw new Error("Portal API request failed");
    }

    const payload = (await response.json()) as ApiResponse<T>;
    return payload.data as T;
};

export const postJson = async <T, P extends object = object>(path: string, body: P) => {
    const response = await fetch(buildApiUrl(path), {
        body: JSON.stringify(body),
        headers: {
            Accept: "application/json",
            "Content-Type": "application/json"
        },
        method: "POST"
    });

    if (!response.ok) {
        throw new Error("Portal API request failed");
    }

    const payload = (await response.json()) as ApiResponse<T>;
    return payload.data as T;
};
