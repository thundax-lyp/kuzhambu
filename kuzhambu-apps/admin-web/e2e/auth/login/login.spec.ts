import { expect, test } from "@playwright/test";
import type { Page, Route } from "@playwright/test";

const TEST_PUBLIC_KEY =
    "04699c2c544c520f22fdf600809234c8a04db38283ab3e9cab9e7b1a4c6d11927e2666a55ec88c4d1177783301c2ea5214ca9d4df9392e5cd3acd3e499076ba819";

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

const mockCurrentUserApis = async (page: Page) => {
    await page.route("**/admin-api/api/sys/current-user/info", async (route) => {
        await fulfillSuccess(route, {
            id: "user-1",
            loginName: "developer",
            name: "Developer",
            ranks: 1,
            admin: true,
            superAdmin: false
        });
    });
    await page.route("**/admin-api/api/sys/current-user/menus", async (route) => {
        await fulfillSuccess(route, [
            {
                id: "dashboard",
                name: "仪表盘",
                url: "/dashboard",
                displayParams: '{"icon":"dashboard"}'
            }
        ]);
    });
    await page.route("**/admin-api/api/sys/current-user/perms", async (route) => {
        await fulfillSuccess(route, {
            perms: ["user", "sys:user:view"]
        });
    });
};

test.describe("login page", () => {
    test.beforeEach(async ({ page }) => {
        await page.addInitScript(() => window.localStorage.clear());
        await mockCurrentUserApis(page);
    });

    test("logs in with backend auth request fields and restores the dashboard", async ({
        page
    }) => {
        let loginRequestBody: Record<string, unknown> | undefined;

        await page.route("**/admin-api/api/auth/session/pre-auth-session", async (route) => {
            await fulfillSuccess(route, {
                loginToken: "login-form-token",
                refreshToken: "pre-auth-refresh-token",
                expiredAt: Date.now() + 5 * 60 * 1000,
                publicKey: TEST_PUBLIC_KEY
            });
        });
        await page.route("**/admin-api/api/auth/captcha?**", async (route) => {
            await route.fulfill({
                contentType: "image/png",
                body: Buffer.from(
                    "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAFgwJ/lUzE4QAAAABJRU5ErkJggg==",
                    "base64"
                )
            });
        });
        await page.route("**/admin-api/api/auth/session/login", async (route) => {
            loginRequestBody = route.request().postDataJSON();
            await fulfillSuccess(route, {
                token: "login-access-token",
                refreshToken: "login-refresh-token",
                expireAt: Date.now() + 60 * 60 * 1000
            });
        });

        await page.goto("/login");

        await expect(page.getByRole("heading", { name: "登录" })).toBeVisible();
        await page.getByPlaceholder("请输入后台账号").fill("developer");
        await page.getByPlaceholder("请输入密码").fill("kuzhambu");
        await page.getByPlaceholder("验证码").fill("1234");
        await page.getByRole("button", { name: /登\s*录/ }).click();

        await expect(page.getByRole("heading", { name: "仪表盘" })).toBeVisible();
        expect(loginRequestBody).toEqual({
            loginToken: "login-form-token",
            userName: "developer",
            password: expect.any(String),
            captcha: "1234"
        });
        expect(loginRequestBody?.password).not.toBe("kuzhambu");
        await expect
            .poll(() =>
                page.evaluate(() => ({
                    accessToken: window.localStorage.getItem("kuzhambu.admin.accessToken"),
                    permissions: window.localStorage.getItem("kuzhambu.admin.permissions"),
                    refreshToken: window.localStorage.getItem("kuzhambu.admin.refreshToken")
                }))
            )
            .toEqual({
                accessToken: "login-access-token",
                permissions: JSON.stringify(["user", "sys:user:view"]),
                refreshToken: "login-refresh-token"
            });
    });
});
