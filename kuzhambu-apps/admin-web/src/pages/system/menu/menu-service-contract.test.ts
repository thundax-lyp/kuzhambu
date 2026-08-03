import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as menuService from "@/pages/system/menu/menu-service";

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

describe("menu service request contracts", () => {
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

    it("sends menu write requests with Menu request fields", async () => {
        const saveRequest: menuService.MenuSaveCommand = {
            id: "menu-1",
            parentId: "menu-root",
            name: "菜单管理",
            perms: "sys:menu:edit",
            ranks: 10,
            display: true,
            displayParams: '{"icon":"menu"}',
            url: "/system/menus",
            remarks: "后台菜单"
        };

        await menuService.addMenu(saveRequest);
        expectLastRequest("/sys/menu/create", saveRequest);

        await menuService.changeMenuInfo(saveRequest);
        expectLastRequest("/sys/menu/update", saveRequest);

        await menuService.changeMenuDisplay("menu-1", false);
        expectLastRequest("/sys/menu/display", [{ id: "menu-1", display: false }]);

        await menuService.moveMenu({
            fromNodeId: "menu-1",
            toNodeId: "menu-2",
            type: "after"
        });
        expectLastRequest("/sys/menu/move", {
            fromNodeId: "menu-1",
            toNodeId: "menu-2",
            type: "after"
        });

        await menuService.removeMenus(["menu-1"]);
        expectLastRequest("/sys/menu/delete", [{ id: "menu-1" }]);
    });
});
