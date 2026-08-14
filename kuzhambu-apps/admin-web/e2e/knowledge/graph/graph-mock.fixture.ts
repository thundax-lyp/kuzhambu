import type { Page, Route } from "@playwright/test";

const PERMISSIONS = ["knowledge:graph:view", "knowledge:graph:edit", "knowledge:graph:apply"];

const fulfill = async (route: Route, data: unknown) =>
    route.fulfill({
        contentType: "application/json",
        body: JSON.stringify({ code: "COMMON-00000", message: "success", data })
    });

export const mockGraphShell = async (page: Page) => {
    const unexpectedBackendRequests: string[] = [];
    const unexpectedConsoleErrors: string[] = [];
    page.on("console", (message) => {
        if (message.type() === "error") {
            unexpectedConsoleErrors.push(message.text());
        }
    });
    page.on("pageerror", (error) => {
        unexpectedConsoleErrors.push(error.message);
    });
    await page.addInitScript(() => {
        localStorage.setItem("kuzhambu.admin.accessToken", "mock-token");
        localStorage.setItem("kuzhambu.admin.refreshToken", "mock-refresh");
        localStorage.setItem("kuzhambu.admin.accessTokenExpireAt", String(Date.now() + 3600000));
        localStorage.setItem(
            "kuzhambu.admin.permissions",
            JSON.stringify([
                "knowledge:graph:view",
                "knowledge:graph:edit",
                "knowledge:graph:apply"
            ])
        );
    });
    await page.route("**/kuzhambu-admin-api/**", async (route) => {
        unexpectedBackendRequests.push(route.request().url());
        await route.fulfill({ status: 404, contentType: "application/json", body: "{}" });
    });
    await page.route("**/kuzhambu-admin-api/api/sys/current-user/get", (route) =>
        fulfill(route, {
            id: "user-1",
            loginName: "developer",
            name: "Developer",
            ranks: 9,
            admin: true,
            superAdmin: true
        })
    );
    await page.route("**/kuzhambu-admin-api/api/sys/current-user/permission/list", (route) =>
        fulfill(route, { perms: PERMISSIONS })
    );
    await page.route("**/kuzhambu-admin-api/api/sys/current-user/menu/list", (route) =>
        fulfill(route, [
            { id: "dashboard", name: "仪表盘", url: "/dashboard" },
            { id: "knowledge", name: "知识治理" },
            {
                id: "knowledge-graph",
                parentId: "knowledge",
                name: "知识图谱",
                url: "/knowledge/graph"
            }
        ])
    );
    await page.route("**/kuzhambu-admin-api/api/auth/session/token/refresh", (route) =>
        fulfill(route, {
            token: "mock-token",
            refreshToken: "mock-refresh",
            expireAt: Date.now() + 3600000
        })
    );
    await page.route("**/kuzhambu-admin-api/api/auth/session/pre-auth-session/request", (route) =>
        fulfill(route, { loginToken: "mock-login-token", publicKey: "04mock-public-key" })
    );
    return { unexpectedBackendRequests, unexpectedConsoleErrors };
};
