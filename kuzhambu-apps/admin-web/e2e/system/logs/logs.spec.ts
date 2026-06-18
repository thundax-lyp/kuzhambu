import { expect, test } from "@playwright/test";
import type { Page, Route } from "@playwright/test";

const PERMISSIONS = ["user", "super", "audit:view"];

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
        ]);
    });
    await page.route("**/admin-api/api/sys/current-user/perms", async (route) => {
        await fulfillSuccess(route, {
            perms: PERMISSIONS
        });
    });
};

test.describe("system and audit logs", () => {
    test.beforeEach(async ({ page }) => {
        await mockShellApis(page);
        await page.addInitScript((permissions) => {
            window.localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
            window.localStorage.setItem(
                "kuzhambu.admin.accessTokenExpireAt",
                String(Date.now() + 3600 * 1000)
            );
            window.localStorage.setItem("kuzhambu.admin.permissions", JSON.stringify(permissions));
        }, PERMISSIONS);
    });

    test("submits filters and refreshes system and audit log tables", async ({ page }) => {
        await page.setViewportSize({ width: 1280, height: 800 });
        let systemLogRequestBody: Record<string, unknown> | undefined;
        await page.route("**/admin-api/api/sys/log/page", async (route) => {
            const requestBody = route.request().postDataJSON();
            systemLogRequestBody = requestBody;
            const filtered = requestBody.userLoginName === "developer";
            await fulfillSuccess(route, {
                pageNo: 1,
                pageSize: 20,
                totalCount: filtered ? 1 : 0,
                records: filtered
                    ? [
                          {
                              id: "log-1",
                              createDate: "2026-06-18 12:00:00",
                              type: "ACCESS",
                              title: "系统-登录-成功",
                              remoteAddr: "127.0.0.1",
                              method: "POST",
                              requestUri: "/api/auth/session/login",
                              createUser: {
                                  id: "user-1",
                                  loginName: "developer",
                                  name: "Developer"
                              }
                          }
                      ]
                    : []
            });
        });

        await page.goto("/system/logs");

        await expect(page.getByRole("heading", { name: "系统日志" })).toBeVisible();
        await page.getByRole("button", { name: /筛\s*选/ }).click();
        await page.getByPlaceholder("admin").fill("developer");
        await page.getByRole("button", { name: /查\s*询/ }).click();

        await expect(page.getByText("系统-登录-成功")).toBeVisible();
        expect(systemLogRequestBody).toEqual(
            expect.objectContaining({
                pageNo: 1,
                pageSize: 20,
                userLoginName: "developer"
            })
        );

        let auditLogRequestBody: Record<string, unknown> | undefined;
        await page.route("**/admin-api/api/audit/log/options", async (route) => {
            await fulfillSuccess(route, {
                actions: [{ label: "新增", value: "CREATE" }],
                objectTypes: [{ label: "用户", value: "USER" }],
                operatorTypes: [{ label: "用户", value: "USER" }]
            });
        });
        await page.route("**/admin-api/api/audit/log/page", async (route) => {
            const requestBody = route.request().postDataJSON();
            auditLogRequestBody = requestBody;
            const filtered = requestBody.beginDate === "2026-06-18 00:00:00";
            await fulfillSuccess(route, {
                pageNo: 1,
                pageSize: 20,
                totalCount: filtered ? 1 : 0,
                records: filtered
                    ? [
                          {
                              id: "audit-1",
                              objectType: "USER",
                              objectId: "user-1",
                              objectDisplayName: "Developer",
                              objectTypeLabel: "用户",
                              action: "CREATE",
                              actionLabel: "新增",
                              operatorType: "USER",
                              operatorTypeLabel: "用户",
                              operatorId: "user-1",
                              operatorName: "Developer",
                              source: "ADMIN_WEB",
                              requestId: "request-1",
                              remoteAddr: "127.0.0.1",
                              summary: "新增用户 Developer",
                              occurredAt: "2026-06-18 12:00:00",
                              changedFields: []
                          }
                      ]
                    : []
            });
        });

        await page.goto("/audit/logs");

        await expect(page.getByRole("heading", { name: "审计日志" })).toBeVisible();
        await page.getByRole("button", { name: /筛\s*选/ }).click();
        await page.getByPlaceholder("2026-05-19 00:00:00").fill("2026-06-18 00:00:00");
        await page.getByPlaceholder("2026-05-19 23:59:59").fill("2026-06-18 23:59:59");
        await page.getByRole("button", { name: /查\s*询/ }).click();

        await expect(page.getByText("新增用户 Developer")).toBeVisible();
        expect(auditLogRequestBody).toEqual(
            expect.objectContaining({
                beginDate: "2026-06-18 00:00:00",
                endDate: "2026-06-18 23:59:59",
                pageNo: 1,
                pageSize: 20
            })
        );
    });
});
