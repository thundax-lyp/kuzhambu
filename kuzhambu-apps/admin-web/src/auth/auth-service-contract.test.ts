import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { createLoginForm, login, logout, refreshAccessToken, refreshCaptcha } from "./auth-service";
import * as currentUserService from "../service/current-user-service";

interface CapturedCall {
    path: string;
    body: unknown;
}

const API_PREFIX = "http://localhost:20010";
const DEV_PROXY_PREFIX = "/kuzhambu-admin-api/api";

const capturedCalls: CapturedCall[] = [];

const readFetchUrl = (input: RequestInfo | URL) => {
    if (typeof input === "string") {
        return input;
    }
    if (input instanceof URL) {
        return input.href;
    }
    return input.url;
};

const installFetchRecorder = () => {
    vi.spyOn(globalThis, "fetch").mockImplementation(async (input, init) => {
        const url = readFetchUrl(input);
        capturedCalls.push({
            path: url.replace(API_PREFIX, "").replace(DEV_PROXY_PREFIX, ""),
            body: init?.body ? JSON.parse(String(init.body)) : undefined
        });

        return new Response(
            JSON.stringify({
                code: "COMMON-00000",
                message: "success",
                data: true
            }),
            {
                headers: {
                    "Content-Type": "application/json"
                },
                status: 200
            }
        );
    });
};

const expectLastCall = (path: string, body: unknown) => {
    expect(capturedCalls.at(-1)).toEqual({
        path,
        body
    });
};

describe("auth and current user service request contracts", () => {
    beforeEach(() => {
        capturedCalls.length = 0;
        localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
        localStorage.setItem(
            "kuzhambu.admin.accessTokenExpireAt",
            String(Date.now() + 3600 * 1000)
        );
        installFetchRecorder();
    });

    afterEach(() => {
        vi.restoreAllMocks();
        localStorage.clear();
    });

    it("sends auth session requests with backend request fields", async () => {
        await createLoginForm();
        expectLastCall("/auth/session/pre-auth-session", undefined);

        await refreshCaptcha("login-token");
        expectLastCall("/auth/captcha/refresh", {
            loginToken: "login-token"
        });

        await login({
            loginToken: "login-token",
            userName: "developer",
            password: "encrypted-password",
            captcha: "1234"
        });
        expectLastCall("/auth/session/login", {
            loginToken: "login-token",
            userName: "developer",
            password: "encrypted-password",
            captcha: "1234"
        });

        await refreshAccessToken({
            clientId: "admin-api",
            refreshToken: "refresh-token"
        });
        expectLastCall("/auth/session/token/refresh", {
            clientId: "admin-api",
            refreshToken: "refresh-token"
        });

        await logout({
            token: "test-token"
        });
        expectLastCall("/auth/session/logout", {
            token: "test-token"
        });
    });

    it("sends current user reads without frontend-only request fields", async () => {
        await currentUserService.getCurrentUserInfo();
        expectLastCall("/sys/current-user/info", undefined);

        await currentUserService.listCurrentUserMenus();
        expectLastCall("/sys/current-user/menus", undefined);

        await currentUserService.listCurrentUserPerms();
        expectLastCall("/sys/current-user/perms", undefined);
    });
});
