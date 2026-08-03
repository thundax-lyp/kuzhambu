import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as systemLogService from "@/pages/system/system-log/system-log-service";

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

describe("system log service request contracts", () => {
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

    it("sends system log requests with backend request fields", async () => {
        const pageRequest: systemLogService.LogPageQuery = {
            pageNo: 1,
            pageSize: 20,
            title: "登录",
            userLoginName: "developer",
            userName: "Developer",
            remoteAddr: "127.0.0.1",
            requestUri: "/api/auth/session/login",
            beginDate: "2026-06-18 00:00:00",
            endDate: "2026-06-18 23:59:59"
        };

        await systemLogService.pageEvents(pageRequest);
        expectLastCall("/sys/log/page", pageRequest);
    });
});
