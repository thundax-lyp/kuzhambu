import { expect, test } from "@playwright/test";
import type { Page, Route } from "@playwright/test";

const REPORT_PERMISSIONS = [
    "user",
    "super",
    "operations:report:view",
    "operations:report:generate"
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
    await page.route("**/kuzhambu-admin-api/api/sys/current-user/info", (route) =>
        fulfillSuccess(route, {
            id: "user-1",
            loginName: "developer",
            name: "Developer",
            ranks: 9,
            admin: true,
            superAdmin: true
        })
    );
    await page.route("**/kuzhambu-admin-api/api/sys/current-user/menus", (route) =>
        fulfillSuccess(route, [
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
                id: "operations-report",
                parentId: "operations",
                name: "报表管理",
                url: "/operations/reports",
                displayParams: '{"icon":"operations-report"}'
            }
        ])
    );
    await page.route("**/kuzhambu-admin-api/api/sys/current-user/perms", (route) =>
        fulfillSuccess(route, {
            perms: REPORT_PERMISSIONS
        })
    );
};

const mockReportApis = async (page: Page) => {
    await page.route("**/kuzhambu-admin-api/api/operations/report/page", (route) =>
        fulfillSuccess(route, {
            pageNo: 1,
            pageSize: 20,
            totalPage: 1,
            count: 2,
            totalCount: 2,
            records: [
                {
                    reportId: 9001,
                    reportType: "WEEKLY",
                    format: "PDF",
                    periodStart: "2026-07-01T00:00:00.000Z",
                    periodEnd: "2026-07-07T23:59:59.000Z",
                    storageObjectId: 7001,
                    artifactFilename: "weekly.pdf",
                    reportStatus: "SUCCEEDED",
                    requesterUserId: 1001,
                    requestedAt: "2026-07-08T01:00:00.000Z"
                },
                {
                    reportId: 9002,
                    reportType: "MONTHLY",
                    format: "HTML",
                    periodStart: "2026-06-01T00:00:00.000Z",
                    periodEnd: "2026-06-30T23:59:59.000Z",
                    reportStatus: "FAILED",
                    failureReason: "render worker timeout",
                    requesterUserId: 1002,
                    requestedAt: "2026-07-08T02:00:00.000Z"
                }
            ]
        })
    );
    await page.route("**/kuzhambu-admin-api/api/operations/report/detail", (route) =>
        fulfillSuccess(route, {
            reportId: 9002,
            reportType: "MONTHLY",
            format: "HTML",
            periodStart: "2026-06-01T00:00:00.000Z",
            periodEnd: "2026-06-30T23:59:59.000Z",
            requestId: "req-9002",
            traceId: "trace-9002",
            templateVersion: "v1",
            reportStatus: "FAILED",
            failureReason: "render worker timeout",
            requesterUserId: 1002,
            requestedAt: "2026-07-08T02:00:00.000Z"
        })
    );
};

test.describe("operations reports", () => {
    test.beforeEach(async ({ page }) => {
        await page.setViewportSize({ width: 1280, height: 800 });
        await page.addInitScript((permissions) => {
            window.localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
            window.localStorage.setItem(
                "kuzhambu.admin.accessTokenExpireAt",
                String(Date.now() + 3600 * 1000)
            );
            window.localStorage.setItem("kuzhambu.admin.permissions", JSON.stringify(permissions));
        }, REPORT_PERMISSIONS);
        await mockShellApis(page);
        await mockReportApis(page);
    });

    test("opens from menu, generates, shows detail and exposes operations download url", async ({
        page
    }) => {
        let generatedBody: unknown = null;
        await page.route("**/kuzhambu-admin-api/api/operations/report/generate", async (route) => {
            generatedBody = route.request().postDataJSON();
            await fulfillSuccess(route, {
                reportId: 9003,
                reportStatus: "PENDING"
            });
        });

        await page.goto("/dashboard");
        await page.getByRole("menuitem", { name: "运维" }).click();
        await page.getByRole("menuitem", { name: "报表管理" }).click();

        await expect(page).toHaveURL(/\/operations\/reports$/);
        await expect(page.getByRole("heading", { name: "报表管理" })).toBeVisible();
        await expect(page.locator(".menu-icon-config-error")).toHaveCount(0);
        await expect(page.getByText("render worker timeout")).toBeVisible();
        await expect(page.getByRole("link", { name: /下载/ })).toHaveAttribute(
            "href",
            "/kuzhambu-admin-api/api/operations/report/9001/content?download=true"
        );

        await page
            .getByText("生成周期")
            .locator("..")
            .locator("input")
            .first()
            .fill("2026-07-01 00:00:00");
        await page
            .getByText("生成周期")
            .locator("..")
            .locator("input")
            .nth(1)
            .fill("2026-07-07 23:59:59");
        await page.keyboard.press("Tab");
        await page.getByRole("button", { name: /提交生成/ }).click();
        await expect.poll(() => generatedBody).not.toBeNull();

        await page.getByRole("button", { name: "详情" }).nth(1).click();
        await expect(page.getByText("报表生成失败")).toBeVisible();
        await expect(page.getByText("req-9002")).toBeVisible();
    });
});
