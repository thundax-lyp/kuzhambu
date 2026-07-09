import { expect, test } from "@playwright/test";
import type { Page } from "@playwright/test";
import type { Route } from "@playwright/test";

const DASHBOARD_USER_PERMISSIONS = [
    "user",
    "super",
    "operations:dashboard:view",
    "system:log:view",
    "audit:view"
];

const ONLY_DASHBOARD_PERMISSIONS = ["user", "super", "operations:dashboard:view"];

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

const mockShellApis = async (page: Page, permissions: string[]) => {
    await page.route("**/kuzhambu-admin-api/api/sys/current-user/info", async (route) => {
        await route.fulfill({
            contentType: "application/json",
            body: JSON.stringify({
                code: "COMMON-00000",
                message: "success",
                data: {
                    id: "user-1",
                    loginName: "developer",
                    name: "Developer",
                    ranks: 9,
                    admin: true,
                    superAdmin: true
                }
            })
        });
    });
    await page.route("**/kuzhambu-admin-api/api/sys/current-user/menus", async (route) => {
        await route.fulfill({
            contentType: "application/json",
            body: JSON.stringify({
                code: "COMMON-00000",
                message: "success",
                data: [
                    {
                        id: "dashboard",
                        name: "仪表盘",
                        url: "/dashboard",
                        displayParams: '{"icon":"dashboard"}'
                    },
                    {
                        id: "operations",
                        name: "运维",
                        displayParams: '{"icon":"operations"}'
                    },
                    {
                        id: "operations-dashboard",
                        parentId: "operations",
                        name: "运维看板",
                        url: "/operations/dashboard",
                        displayParams: '{"icon":"operations-dashboard"}'
                    },
                    {
                        id: "operations-task",
                        parentId: "operations",
                        name: "任务台账",
                        url: "/operations/tasks",
                        displayParams: '{"icon":"operations-task"}'
                    },
                    {
                        id: "operations-report",
                        parentId: "operations",
                        name: "报表管理",
                        url: "/operations/reports",
                        displayParams: '{"icon":"operations-report"}'
                    },
                    {
                        id: "operations-backup-restore",
                        parentId: "operations",
                        name: "备份恢复",
                        url: "/operations/backup-restore",
                        displayParams: '{"icon":"operations-backup-restore"}'
                    },
                    {
                        id: "operations-cleanup",
                        parentId: "operations",
                        name: "清理维护",
                        url: "/operations/cleanup",
                        displayParams: '{"icon":"operations-cleanup"}'
                    },
                    {
                        id: "system",
                        name: "系统管理",
                        displayParams: '{"icon":"system"}'
                    },
                    {
                        id: "system-log",
                        parentId: "system",
                        name: "系统日志",
                        url: "/system/logs",
                        displayParams: '{"icon":"system-log"}'
                    },
                    {
                        id: "audit",
                        name: "审计管理",
                        displayParams: '{"icon":"audit"}'
                    },
                    {
                        id: "audit-log",
                        parentId: "audit",
                        name: "审计日志",
                        url: "/audit/logs",
                        displayParams: '{"icon":"audit-log"}'
                    }
                ]
            })
        });
    });
    await page.route("**/kuzhambu-admin-api/api/sys/current-user/perms", async (route) => {
        await route.fulfill({
            contentType: "application/json",
            body: JSON.stringify({
                code: "COMMON-00000",
                message: "success",
                data: {
                    perms: permissions
                }
            })
        });
    });
};

test.describe("operations dashboard entries", () => {
    test.beforeEach(async ({ page }) => {
        await page.setViewportSize({ width: 1280, height: 800 });
        await page.addInitScript((permissions) => {
            window.localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
            window.localStorage.setItem(
                "kuzhambu.admin.accessTokenExpireAt",
                String(Date.now() + 3600 * 1000)
            );
            window.localStorage.setItem("kuzhambu.admin.permissions", JSON.stringify(permissions));
        }, DASHBOARD_USER_PERMISSIONS);
        await page.route("**/kuzhambu-admin-api/api/sys/log/page", (route) =>
            fulfillSuccess(route, {
                pageNo: 1,
                pageSize: 20,
                totalCount: 0,
                records: []
            })
        );
        await page.route("**/kuzhambu-admin-api/api/audit/log/options", (route) =>
            fulfillSuccess(route, {
                actions: [],
                objectTypes: [],
                operatorTypes: []
            })
        );
        await page.route("**/kuzhambu-admin-api/api/audit/log/page", (route) =>
            fulfillSuccess(route, {
                pageNo: 1,
                pageSize: 20,
                totalCount: 0,
                records: []
            })
        );
    });

    test("renders system/audit entries and navigates from operations dashboard", async ({
        page
    }) => {
        await mockShellApis(page, DASHBOARD_USER_PERMISSIONS);

        await page.goto("/operations/dashboard");

        await expect(page.getByRole("heading", { name: "运营看板" })).toBeVisible();
        await expect(page.locator(".menu-icon-config-error")).toHaveCount(0);

        await expect(page.getByRole("link", { name: "系统日志" })).toBeVisible();
        await expect(page.getByRole("link", { name: "审计日志" })).toBeVisible();

        await page.getByTestId("operations-entry-system-log").click();
        await expect(page.getByRole("heading", { name: "系统日志" })).toBeVisible();

        await page.goBack();
        await page.getByTestId("operations-entry-audit-log").click();
        await expect(page.getByRole("heading", { name: "审计日志" })).toBeVisible();
    });

    test("hides system and audit entries without target permissions", async ({ page }) => {
        await mockShellApis(page, ONLY_DASHBOARD_PERMISSIONS);

        await page.goto("/operations/dashboard");

        await expect(page.getByRole("heading", { name: "运营看板" })).toBeVisible();
        await expect(page.getByText("系统日志")).not.toBeVisible();
        await expect(page.getByText("审计日志")).not.toBeVisible();
    });
});
