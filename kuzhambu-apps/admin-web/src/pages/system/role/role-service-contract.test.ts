import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as roleService from "@/pages/system/role/role-service";

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

describe("role service request contracts", () => {
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

    it("sends role write requests with Role request fields", async () => {
        const saveRequest: roleService.RoleSaveCommand = {
            id: "role-1",
            name: "管理员",
            admin: true,
            enable: true,
            remarks: "拥有全部后台能力",
            menus: [{ id: "menu-1" }]
        };

        await roleService.create(saveRequest);
        expectLastRequest("/sys/role/create", saveRequest);

        await roleService.changeInfo(saveRequest);
        expectLastRequest("/sys/role/update", saveRequest);

        await roleService.changeStatus({
            roles: [{ id: "role-1", enable: false }]
        });
        expectLastRequest("/sys/role/enable", [{ id: "role-1", enable: false }]);

        await roleService.sort({
            orderedIds: ["role-1", "role-2"],
            sortDirection: "ASC"
        });
        expectLastRequest("/sys/role/sort", {
            orderedIds: ["role-1", "role-2"],
            sortDirection: "ASC"
        });

        await roleService.remove(["role-1"]);
        expectLastRequest("/sys/role/delete", [{ id: "role-1" }]);
    });
});
