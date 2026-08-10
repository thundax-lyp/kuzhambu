import { ADMIN_API_BASE_URL, postJson } from "@/api/http";
import type { AccessTokenRecord, LoginFormRecord } from "./auth-types";

export interface LoginCommand {
    loginToken: string;
    userName: string;
    password: string;
    captcha?: string;
}

export interface LogoutCommand {
    token: string;
}

export interface TokenRefreshCommand {
    clientId?: string;
    refreshToken: string;
}

export const createLoginForm = () => {
    return postJson<LoginFormRecord>("/auth/session/pre-auth-session/request");
};

export const refreshCaptcha = (loginToken: string) => {
    return postJson<{ refreshed: boolean }, { loginToken: string }>("/auth/captcha/refresh", {
        body: { loginToken }
    });
};

export const login = (request: LoginCommand) => {
    return postJson<AccessTokenRecord, LoginCommand>("/auth/session/login", {
        body: request
    });
};

export const refreshAccessToken = (request: TokenRefreshCommand) => {
    return postJson<AccessTokenRecord, TokenRefreshCommand>("/auth/session/token/refresh", {
        body: request
    });
};

export const logout = (request: LogoutCommand) => {
    return postJson<boolean, LogoutCommand>("/auth/session/logout", {
        body: request
    });
};

export const getCaptchaUrl = (loginToken: string, version: number) => {
    const params = new URLSearchParams({
        loginToken,
        width: "150",
        height: "40",
        _: String(version)
    });

    return `${ADMIN_API_BASE_URL}/auth/captcha?${params.toString()}`;
};
