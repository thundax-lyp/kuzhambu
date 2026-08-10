import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as userService from "@/pages/system/user/user-service";

interface CapturedCall {
    path: string;
    body: unknown;
}

const API_PREFIX = "http://localhost:20010";
const DEV_PROXY_PREFIX = "/kuzhambu-admin-api/api";

const capturedRequests: CapturedCall[] = [];

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
        const path = url.replace(API_PREFIX, "").replace(DEV_PROXY_PREFIX, "");
        capturedRequests.push({
            path,
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

const expectLastRequest = (path: string, body: unknown) => {
    expect(capturedRequests.at(-1)).toEqual({
        path,
        body
    });
};

describe("user service request contracts", () => {
    beforeEach(() => {
        capturedRequests.length = 0;
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

    it("sends user write requests with User request fields", async () => {
        const saveRequest: userService.SaveCommand = {
            id: "user-1",
            remarks: "系统管理员",
            loginName: "admin",
            loginPass: "secret",
            token: "totp-token",
            ranks: 1,
            name: "Admin",
            email: "admin@example.com",
            mobile: "13800000000",
            admin: true,
            enable: true,
            department: { id: "dept-1" },
            roles: [{ id: "role-1" }]
        };

        await userService.create(saveRequest);
        expectLastRequest("/sys/user/create", saveRequest);

        await userService.changeInfo(saveRequest);
        expectLastRequest("/sys/user/update", saveRequest);

        await userService.changeStatus({
            users: [{ id: "user-1", enable: false }]
        });
        expectLastRequest("/sys/user/status/update", [{ id: "user-1", enable: false }]);

        await userService.remove(["user-1"]);
        expectLastRequest("/sys/user/delete", [{ id: "user-1" }]);
    });
});
