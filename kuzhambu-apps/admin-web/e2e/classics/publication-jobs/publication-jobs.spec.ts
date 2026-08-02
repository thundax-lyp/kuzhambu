import { expect, test } from "@playwright/test";

const apiResponse = (data: unknown) => ({
    code: "COMMON-00000",
    message: "success",
    data
});

test.use({ viewport: { width: 1280, height: 800 }, isMobile: false });

test.describe("classics publication jobs page", () => {
    test.beforeEach(async ({ page }) => {
        await page.route("**/kuzhambu-admin-api/api/sys/current-user/info", (route) =>
            route.fulfill({
                contentType: "application/json",
                body: JSON.stringify(
                    apiResponse({ id: "1", loginName: "developer", name: "Developer" })
                )
            })
        );
        await page.route("**/kuzhambu-admin-api/api/sys/current-user/menus", (route) =>
            route.fulfill({
                contentType: "application/json",
                body: JSON.stringify(
                    apiResponse([
                        { id: "20", name: "古籍管理", displayParams: '{"icon":"classics"}' },
                        {
                            id: "24",
                            parentId: "20",
                            name: "发布任务",
                            url: "/classics/publication-jobs"
                        }
                    ])
                )
            })
        );
        await page.route("**/kuzhambu-admin-api/api/sys/current-user/perms", (route) =>
            route.fulfill({
                contentType: "application/json",
                body: JSON.stringify(apiResponse({ perms: ["classics:publication:view"] }))
            })
        );
        await page.route("**/kuzhambu-admin-api/api/auth/session/token/refresh", (route) =>
            route.fulfill({
                contentType: "application/json",
                body: JSON.stringify(
                    apiResponse({
                        token: "test-token",
                        refreshToken: "refresh-token",
                        expireAt: Date.now() + 3600 * 1000
                    })
                )
            })
        );
        await page.addInitScript(() => {
            window.localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
            window.localStorage.setItem("kuzhambu.admin.refreshToken", "refresh-token");
            window.localStorage.setItem(
                "kuzhambu.admin.accessTokenExpireAt",
                String(Date.now() + 3600 * 1000)
            );
            window.localStorage.setItem(
                "kuzhambu.admin.permissions",
                JSON.stringify(["classics:publication:view"])
            );
        });
    });

    test("opens a failure detail while keeping the task surface read-only", async ({ page }) => {
        const consoleErrors: string[] = [];
        page.on("console", (message) => {
            if (message.type() === "error") {
                consoleErrors.push(message.text());
            }
        });
        const job = {
            id: "9007199254740993",
            jobType: "PUBLISH",
            jobStatus: "ES_PREPARED",
            jobResultStatus: "FAILED",
            failureStep: "ES_PREPARED",
            contentType: "SANCAI_ENTRY",
            contentId: "3001",
            contentTitleSnapshot: "天地",
            sourceLifecycleStatus: "DRAFT",
            targetLifecycleStatus: "PUBLISHED",
            attemptCount: 4,
            maxAttempts: 4,
            esDocumentId: "classics-sancai-3001",
            esCleanupStatus: "PENDING",
            fastgptCollectionId: null,
            fastgptCleanupStatus: "NONE",
            failureReason: "ES probe failed",
            detailJsonSummary: '{"provider":"ES","step":"probe"}',
            requestedAt: "2026-08-02T06:00:00Z"
        };
        await page.route("**/kuzhambu-admin-api/api/classics/publication-jobs/page", (route) =>
            route.fulfill({
                contentType: "application/json",
                body: JSON.stringify(
                    apiResponse({ pageNo: 1, pageSize: 20, count: 1, totalPage: 1, records: [job] })
                )
            })
        );
        await page.route("**/kuzhambu-admin-api/api/classics/publication-jobs/get", (route) =>
            route.fulfill({
                contentType: "application/json",
                body: JSON.stringify(apiResponse(job))
            })
        );

        await page.goto("/classics/publication-jobs");

        await expect(page).toHaveURL(/\/classics\/publication-jobs$/);
        await expect(page.getByRole("heading", { name: "发布任务" })).toBeVisible();
        await expect(page.getByRole("table", { name: "发布任务列表" })).toContainText("天地");
        await page.getByTestId("classics-publication-jobs-view-button").click();
        await expect(page.getByLabel("发布任务详情")).toContainText("ES probe failed");
        await expect(page.getByRole("button", { name: /取消|重试|编辑|推进|清理/ })).toHaveCount(0);
        expect(consoleErrors).toEqual([]);
    });
});
