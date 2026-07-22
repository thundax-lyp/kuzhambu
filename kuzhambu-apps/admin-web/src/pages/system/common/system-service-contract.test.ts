import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as departmentService from "@/pages/system/department/department-service";
import * as dictionaryService from "@/pages/system/dictionary/dictionary-service";
import * as menuService from "@/pages/system/menu/menu-service";
import * as roleService from "@/pages/system/role/role-service";
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

describe("system service request contracts", () => {
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
        expectLastRequest("/sys/user/enable", [{ id: "user-1", enable: false }]);

        await userService.remove(["user-1"]);
        expectLastRequest("/sys/user/delete", [{ id: "user-1" }]);
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

    it("sends department write requests with Department request fields", async () => {
        const saveRequest: departmentService.DepartmentSaveCommand = {
            id: "dept-1",
            parentId: "dept-root",
            name: "技术中心",
            shortName: "技术",
            remarks: "研发组织"
        };

        await departmentService.addDepartment(saveRequest);
        expectLastRequest("/sys/department/create", saveRequest);

        await departmentService.changeDepartmentInfo(saveRequest);
        expectLastRequest("/sys/department/update", saveRequest);

        await departmentService.moveDepartment({
            fromNodeId: "dept-1",
            toNodeId: "dept-2",
            type: "insideLast"
        });
        expectLastRequest("/sys/department/move", {
            fromNodeId: "dept-1",
            toNodeId: "dept-2",
            type: "insideLast"
        });

        await departmentService.removeDepartments(["dept-1"]);
        expectLastRequest("/sys/department/delete", [{ id: "dept-1" }]);
    });

    it("sends dictionary write requests with Dict request fields", async () => {
        const saveRequest: dictionaryService.DictSaveCommand = {
            id: "dict-1",
            type: "system_status",
            label: "启用",
            value: "ENABLED",
            remarks: "默认启用状态"
        };

        await dictionaryService.addDictionary(saveRequest);
        expectLastRequest("/sys/dict/create", saveRequest);

        await dictionaryService.changeDictionaryInfo(saveRequest);
        expectLastRequest("/sys/dict/update", saveRequest);

        await dictionaryService.removeDictionaries(["dict-1"]);
        expectLastRequest("/sys/dict/delete", [{ id: "dict-1" }]);
    });
});
