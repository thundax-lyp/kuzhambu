import { expect, test } from "@playwright/test";
import type { Page, Route } from "@playwright/test";

const ADMIN_PERMISSIONS = [
    "user",
    "sys:user:view",
    "sys:user:edit",
    "sys:role:view",
    "sys:role:edit",
    "super"
];

const fulfillSuccess = async (route: Route, data: unknown) => {
    await route.fulfill({
        contentType: "application/json",
        body: JSON.stringify({
            code: "COMMON-00000",
            message: "success",
            data
        })
    });
};

const mockShellApis = async (page: Page) => {
    await page.route("**/admin-api/api/sys/current-user/info", async (route) => {
        await fulfillSuccess(route, {
            id: "user-1",
            loginName: "developer",
            name: "Developer",
            ranks: 9,
            admin: true,
            superAdmin: true
        });
    });
    await page.route("**/admin-api/api/sys/current-user/menus", async (route) => {
        await fulfillSuccess(route, [
            {
                id: "dashboard",
                name: "仪表盘",
                url: "/dashboard",
                displayParams: '{"icon":"dashboard"}'
            },
            {
                id: "system",
                name: "系统管理",
                displayParams: '{"icon":"system"}'
            },
            {
                id: "users",
                parentId: "system",
                name: "用户管理",
                url: "/system/users",
                displayParams: '{"icon":"users"}'
            },
            {
                id: "roles",
                parentId: "system",
                name: "角色管理",
                url: "/system/roles",
                displayParams: '{"icon":"roles"}'
            },
            {
                id: "menus",
                parentId: "system",
                name: "菜单管理",
                url: "/system/menus",
                displayParams: '{"icon":"menus"}'
            }
        ]);
    });
    await page.route("**/admin-api/api/sys/current-user/perms", async (route) => {
        await fulfillSuccess(route, {
            perms: ADMIN_PERMISSIONS
        });
    });
    await page.route("**/admin-api/api/auth/session/token/refresh", async (route) => {
        await fulfillSuccess(route, {
            token: "test-token",
            refreshToken: "refresh-token",
            expireAt: Date.now() + 3600 * 1000
        });
    });
};

const mockManagementApis = async (page: Page) => {
    let userPageRequestCount = 0;
    let roleListRequestCount = 0;
    let menuListRequestCount = 0;

    await page.route("**/admin-api/api/sys/user/department/tree", async (route) => {
        await fulfillSuccess(route, [
            {
                id: "department-1",
                name: "技术中心",
                shortName: "技术",
                namePath: "技术中心"
            }
        ]);
    });
    await page.route("**/admin-api/api/sys/user/role/list", async (route) => {
        await fulfillSuccess(route, [
            {
                id: "role-1",
                name: "管理员"
            }
        ]);
    });
    await page.route("**/admin-api/api/sys/user/options", async (route) => {
        await fulfillSuccess(route, {
            rankOptions: [{ label: "管理员", value: 9 }],
            statusOptions: [
                { label: "启用", value: true },
                { label: "停用", value: false }
            ]
        });
    });
    await page.route("**/admin-api/api/sys/user/page", async (route) => {
        userPageRequestCount += 1;
        await fulfillSuccess(route, {
            pageNo: 1,
            pageSize: 20,
            totalCount: 1,
            records: [
                {
                    id: "user-1",
                    loginName: "developer",
                    name: "Developer",
                    ranks: 9,
                    enable: true,
                    department: {
                        id: "department-1",
                        name: "技术中心",
                        namePath: "技术中心"
                    },
                    roles: [{ id: "role-1", name: "管理员" }]
                }
            ]
        });
    });

    await page.route("**/admin-api/api/sys/role/list", async (route) => {
        roleListRequestCount += 1;
        await fulfillSuccess(route, [
            {
                id: "role-1",
                name: "管理员",
                admin: true,
                enable: true,
                menus: [{ id: "menu-1", name: "菜单管理" }]
            }
        ]);
    });
    await page.route("**/admin-api/api/sys/role/options", async (route) => {
        await fulfillSuccess(route, {
            privilegeOptions: [{ label: "管理权限", value: true }],
            statusOptions: [
                { label: "启用", value: "ENABLED" },
                { label: "停用", value: "DISABLED" }
            ]
        });
    });
    await page.route("**/admin-api/api/sys/role/menu/tree", async (route) => {
        await fulfillSuccess(route, [
            {
                id: "menu-1",
                name: "菜单管理",
                perms: "sys:menu:edit"
            }
        ]);
    });

    await page.route("**/admin-api/api/sys/menu/list", async (route) => {
        menuListRequestCount += 1;
        await fulfillSuccess(route, [
            {
                id: "menu-1",
                name: "菜单管理",
                url: "/system/menus",
                perms: "sys:menu:edit",
                ranks: 1,
                display: true
            }
        ]);
    });

    return {
        getMenuListRequestCount: () => menuListRequestCount,
        getRoleListRequestCount: () => roleListRequestCount,
        getUserPageRequestCount: () => userPageRequestCount
    };
};

test.describe("system management pages", () => {
    test.beforeEach(async ({ page }) => {
        await mockShellApis(page);
        await page.addInitScript((permissions) => {
            window.localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
            window.localStorage.setItem("kuzhambu.admin.refreshToken", "refresh-token");
            window.localStorage.setItem(
                "kuzhambu.admin.accessTokenExpireAt",
                String(Date.now() + 3600 * 1000)
            );
            window.localStorage.setItem("kuzhambu.admin.permissions", JSON.stringify(permissions));
        }, ADMIN_PERMISSIONS);
    });

    test("opens user, role and menu create forms from backend-backed list pages", async ({
        page
    }) => {
        await page.setViewportSize({ width: 1280, height: 800 });
        const managementApis = await mockManagementApis(page);

        await page.goto("/system/users");
        await expect(page.getByRole("heading", { name: "用户管理" })).toBeVisible();
        await expect.poll(managementApis.getUserPageRequestCount).toBeGreaterThan(0);
        await page.getByRole("button", { name: /新\s*增$/ }).click();
        await expect(page.getByRole("dialog", { name: "新增用户" })).toBeVisible();

        await page.goto("/system/roles");
        await expect(page.getByRole("heading", { name: "角色管理" })).toBeVisible();
        await expect.poll(managementApis.getRoleListRequestCount).toBeGreaterThan(0);
        await page.getByRole("button", { name: "新增角色" }).click();
        await expect(page.getByRole("dialog", { name: "新增角色" })).toBeVisible();

        await page.goto("/system/menus");
        await expect(page.getByRole("heading", { name: "菜单管理" })).toBeVisible();
        await expect.poll(managementApis.getMenuListRequestCount).toBeGreaterThan(0);
        await page.getByRole("button", { name: "新增菜单" }).click();
        await expect(page.getByRole("dialog", { name: "新增菜单" })).toBeVisible();
    });
});
