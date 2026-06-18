import { expect, test } from "@playwright/test";
import type { Page, Route } from "@playwright/test";

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
            id: "1",
            loginName: "developer",
            name: "Developer"
        });
    });
    await page.route("**/admin-api/api/sys/current-user/menus", async (route) => {
        await fulfillSuccess(route, [
            {
                id: "1",
                name: "仪表盘",
                url: "/dashboard",
                displayParams: "{\"icon\":\"dashboard\"}"
            },
            {
                id: "2",
                name: "系统管理",
                displayParams: "{\"icon\":\"system\"}"
            },
            {
                id: "3",
                parentId: "2",
                name: "字典管理",
                url: "/system/dictionaries",
                displayParams: "{\"icon\":\"dictionary\"}"
            }
        ]);
    });
    await page.route("**/admin-api/api/sys/current-user/perms", async (route) => {
        await fulfillSuccess(route, {
            perms: ["sys:dict:view", "sys:dict:edit"]
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

test.describe("dictionary page", () => {
    test.beforeEach(async ({ page }) => {
        await mockShellApis(page);
        await page.addInitScript(() => {
            window.localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
            window.localStorage.setItem("kuzhambu.admin.refreshToken", "refresh-token");
            window.localStorage.setItem(
                "kuzhambu.admin.accessTokenExpireAt",
                String(Date.now() + 3600 * 1000)
            );
            window.localStorage.setItem(
                "kuzhambu.admin.permissions",
                JSON.stringify(["sys:dict:view", "sys:dict:edit"])
            );
        });
    });

    test("submits create form with backend dictionary request fields", async ({ page }) => {
        await page.setViewportSize({ width: 1280, height: 800 });
        let created = false;
        let createRequestBody: unknown;
        await page.route("**/admin-api/api/sys/dict/page", async (route) => {
            await fulfillSuccess(route, {
                pageNo: 1,
                pageSize: 20,
                totalCount: created ? 1 : 0,
                records: created
                    ? [
                          {
                              id: "dict-1",
                              type: "system_status",
                              label: "启用",
                              value: "ENABLED",
                              remarks: "默认启用状态"
                          }
                      ]
                    : []
            });
        });
        await page.route("**/admin-api/api/sys/dict/create", async (route) => {
            createRequestBody = route.request().postDataJSON();
            created = true;
            await fulfillSuccess(route, {
                id: "dict-1",
                type: "system_status",
                label: "启用",
                value: "ENABLED",
                remarks: "默认启用状态"
            });
        });

        await page.goto("/system/dictionaries");

        await expect(page.getByRole("heading", { name: "字典管理" })).toBeVisible();
        await page.getByRole("button", { name: "新增字典项" }).click();

        const drawer = page.getByRole("dialog", { name: "新增字典项" });
        await expect(drawer).toBeVisible();
        await drawer.getByLabel("字典类型").fill(" system_status ");
        await drawer.getByLabel("标签").fill(" 启用 ");
        await drawer.getByLabel("值").fill(" ENABLED ");
        await drawer.getByLabel("备注").fill(" 默认启用状态 ");
        await drawer.getByRole("button", { name: /保\s*存/ }).click();

        await expect(drawer).toBeHidden();
        await expect(page.getByText("system_status").first()).toBeVisible();
        expect(createRequestBody).toEqual({
            type: "system_status",
            label: "启用",
            value: "ENABLED",
            remarks: "默认启用状态"
        });
    });
});
