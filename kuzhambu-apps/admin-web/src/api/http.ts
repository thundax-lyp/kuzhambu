import {
    clearAccessToken,
    getAccessToken,
    getAccessTokenExpireAt,
    getRefreshToken,
    saveTokenSession
} from "../auth/token-storage";

export const ADMIN_API_BASE_URL = import.meta.env.VITE_ADMIN_API_BASE_URL || "/admin-api/api";
const ACCESS_TOKEN_HEADER = "Access-Token";
const AUTH_INVALID_CODE = "COMMON-00002";
const ADMIN_CLIENT_ID = "admin-api";
const TOKEN_REFRESH_PATH = "/auth/session/token/refresh";
const TOKEN_REFRESH_AHEAD_MS = 60 * 1000;

interface ApiResponse<T> {
    code: string;
    message: string;
    data: T;
}

interface RequestOptions<TBody> {
    body?: TBody;
}

interface AccessTokenPayload {
    token: string;
    refreshToken?: string;
    expireAt?: number;
}

export class ApiError extends Error {
    readonly code: string | number;

    constructor(code: string | number, message: string) {
        super(message);
        this.name = "ApiError";
        this.code = code;
    }
}

const isSuccessCode = (code: string | undefined) => {
    return code === "COMMON-00000";
};

const isAuthInvalid = (response: Response, code: string | number | undefined) => {
    return response.status === 401 || code === AUTH_INVALID_CODE;
};

const shouldRefreshBeforeRequest = () => {
    const token = getAccessToken();
    const refreshToken = getRefreshToken();
    const expireAt = getAccessTokenExpireAt();
    return Boolean(
        token && refreshToken && expireAt && expireAt <= Date.now() + TOKEN_REFRESH_AHEAD_MS
    );
};

let refreshPromise: Promise<AccessTokenPayload | null> | null = null;

const requestTokenRefresh = async () => {
    const refreshToken = getRefreshToken();
    if (!refreshToken) {
        return null;
    }

    const response = await fetch(`${ADMIN_API_BASE_URL}${TOKEN_REFRESH_PATH}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            clientId: ADMIN_CLIENT_ID,
            refreshToken
        })
    });

    const payload = (await response.json()) as ApiResponse<AccessTokenPayload>;
    if (!response.ok || !isSuccessCode(payload.code) || !payload.data?.token) {
        clearAccessToken();
        return null;
    }

    saveTokenSession(payload.data);
    return payload.data;
};

export const refreshAccessTokenIfNeeded = async () => {
    if (refreshPromise) {
        const refreshedToken = await refreshPromise;
        return refreshedToken?.token || getAccessToken();
    }

    if (!shouldRefreshBeforeRequest()) {
        return getAccessToken();
    }

    const refreshedToken = await refreshAccessToken();
    return refreshedToken?.token || getAccessToken();
};

const refreshAccessToken = async () => {
    if (!refreshPromise) {
        refreshPromise = requestTokenRefresh().finally(() => {
            refreshPromise = null;
        });
    }

    return refreshPromise;
};

const requestJson = async <TResponse, TBody = unknown>(
    path: string,
    options: RequestOptions<TBody>,
    token: string | null
) => {
    const headers: HeadersInit = {
        "Content-Type": "application/json"
    };
    if (token) {
        headers[ACCESS_TOKEN_HEADER] = token;
    }

    const response = await fetch(`${ADMIN_API_BASE_URL}${path}`, {
        method: "POST",
        headers,
        body: options.body === undefined ? undefined : JSON.stringify(options.body)
    });

    const payload = (await response.json()) as ApiResponse<TResponse>;
    return { response, payload };
};

const requestFormData = async <TResponse>(path: string, body: FormData, token: string | null) => {
    const headers: HeadersInit = {};
    if (token) {
        headers[ACCESS_TOKEN_HEADER] = token;
    }

    const response = await fetch(`${ADMIN_API_BASE_URL}${path}`, {
        method: "POST",
        headers,
        body
    });

    const payload = (await response.json()) as ApiResponse<TResponse>;
    return { response, payload };
};

export const postJson = async <TResponse, TBody = unknown>(
    path: string,
    options: RequestOptions<TBody> = {}
) => {
    if (path !== TOKEN_REFRESH_PATH) {
        await refreshAccessTokenIfNeeded();
    }

    const token = getAccessToken();
    let { response, payload } = await requestJson<TResponse, TBody>(path, options, token);

    if (!response.ok || !isSuccessCode(payload.code)) {
        const code = payload.code ?? response.status;
        if (path !== TOKEN_REFRESH_PATH && isAuthInvalid(response, code) && getRefreshToken()) {
            const refreshedToken = await refreshAccessToken();
            if (refreshedToken?.token) {
                const retryResult = await requestJson<TResponse, TBody>(
                    path,
                    options,
                    refreshedToken.token
                );
                response = retryResult.response;
                payload = retryResult.payload;

                if (response.ok && isSuccessCode(payload.code)) {
                    return payload.data;
                }
            }
        }

        if (isAuthInvalid(response, code)) {
            clearAccessToken();
        }
        throw new ApiError(code, payload.message || "请求失败");
    }

    return payload.data;
};

export const postFormData = async <TResponse>(path: string, body: FormData) => {
    await refreshAccessTokenIfNeeded();

    const token = getAccessToken();
    let { response, payload } = await requestFormData<TResponse>(path, body, token);

    if (!response.ok || !isSuccessCode(payload.code)) {
        const code = payload.code ?? response.status;
        if (isAuthInvalid(response, code) && getRefreshToken()) {
            const refreshedToken = await refreshAccessToken();
            if (refreshedToken?.token) {
                const retryResult = await requestFormData<TResponse>(
                    path,
                    body,
                    refreshedToken.token
                );
                response = retryResult.response;
                payload = retryResult.payload;

                if (response.ok && isSuccessCode(payload.code)) {
                    return payload.data;
                }
            }
        }

        if (isAuthInvalid(response, code)) {
            clearAccessToken();
        }
        throw new ApiError(code, payload.message || "请求失败");
    }

    return payload.data;
};
