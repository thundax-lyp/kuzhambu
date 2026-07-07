import { expect, test } from "@playwright/test";
import type { Page, Route } from "@playwright/test";

const SYSTEM_LOG_VIEWER_PERMISSIONS = ["user", "super", "system:log:view"];
const AUDIT_LOG_VIEWER_PERMISSIONS = ["user", "super", "audit:view"];
const FULL_PERMISSIONS = ["user", "super", "system:log:view", "audit:view"];

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
        await fulfillSuccess(route, {
            id: "user-1",
            loginName: "developer",
            name: "Developer",
            ranks: 9,
            admin: true,
            superAdmin: true
        });
    });
    await page.route("**/kuzhambu-admin-api/api/sys/current-user/menus", async (route) => {
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
    await page.route("**/kuzhambu-admin-api/api/sys/current-user/perms", async (route) => {
        await fulfillSuccess(route, {
            perms: permissions
        });
    });
};

const mockSystemLogPage = async (page: Page) => {
    let systemLogRequest: Record<string, unknown> | undefined;
    await page.route("**/kuzhambu-admin-api/api/sys/log/page", async (route) => {
        const requestBody = route.request().postDataJSON();
        systemLogRequest = requestBody;
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

    return { getRequest: () => systemLogRequest };
};

const mockAuditLogApis = async (page: Page) => {
    let optionsRequested = 0;
    let pageRequest: Record<string, unknown> | undefined;
    let detailRequest: { id?: string } | undefined;

    await page.route("**/kuzhambu-admin-api/api/audit/log/options", async (route) => {
        optionsRequested += 1;
        await fulfillSuccess(route, {
            actions: [{ label: "新增", value: "CREATE" }],
            objectTypes: [{ label: "用户", value: "USER" }],
            operatorTypes: [{ label: "用户", value: "USER" }]
        });
    });

    await page.route("**/kuzhambu-admin-api/api/audit/log/page", async (route) => {
        const requestBody = route.request().postDataJSON();
        pageRequest = requestBody;
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

    await page.route("**/kuzhambu-admin-api/api/audit/log/detail", async (route) => {
        const requestBody = route.request().postDataJSON();
        detailRequest = requestBody;
        await fulfillSuccess(route, {
            id: requestBody.id || "audit-1",
            objectType: "USER",
            objectId: "user-1",
            objectDisplayName: "Developer",
            objectTypeLabel: "用户",
            version: 1,
            action: "CREATE",
            actionLabel: "新增",
            operatorType: "USER",
            operatorTypeLabel: "用户",
            operatorId: "user-1",
            operatorName: "Developer",
            source: "ADMIN_WEB",
            requestId: "request-1",
            remoteAddr: "127.0.0.1",
            summary: "新增用户",
            occurredAt: "2026-06-18 12:00:00",
            changedFields: [],
            traceId: "trace-1",
            idempotencyKey: "idempotency-1",
            previousVersion: 0
        });
    });

    return {
        getOptionsRequestCount: () => optionsRequested,
        getPageRequest: () => pageRequest,
        getDetailRequest: () => detailRequest
    };
};

test.describe("system and audit logs", () => {
    test.beforeEach(async ({ page }) => {
        await page.setViewportSize({ width: 1280, height: 800 });
        await page.addInitScript((permissions) => {
            window.localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
            window.localStorage.setItem(
                "kuzhambu.admin.accessTokenExpireAt",
                String(Date.now() + 3600 * 1000)
            );
            window.localStorage.setItem("kuzhambu.admin.permissions", JSON.stringify(permissions));
        }, FULL_PERMISSIONS);
    });

    test("submits filters and refreshes system and audit log tables", async ({ page }) => {
        await mockShellApis(page, FULL_PERMISSIONS);
        const systemLog = await mockSystemLogPage(page);
        const auditLog = await mockAuditLogApis(page);

        await page.goto("/system/logs");

        await expect(page.getByRole("heading", { name: "系统日志" })).toBeVisible();
        await page.getByRole("button", { name: /筛\s*选/ }).click();
        await page.getByPlaceholder("admin").fill("developer");
        await page.getByRole("button", { name: /查\s*询/ }).click();

        await expect(page.getByText("系统-登录-成功")).toBeVisible();
        expect(systemLog.getRequest()).toEqual(
            expect.objectContaining({
                pageNo: 1,
                pageSize: 20,
                userLoginName: "developer"
            })
        );

        await page.goto("/audit/logs");

        await expect(page.getByRole("heading", { name: "审计日志" })).toBeVisible();
        await page.getByRole("button", { name: /筛\s*选/ }).click();
        await page.getByPlaceholder("2026-05-19 00:00:00").fill("2026-06-18 00:00:00");
        await page.getByPlaceholder("2026-05-19 23:59:59").fill("2026-06-18 23:59:59");
        await page.getByRole("button", { name: /查\s*询/ }).click();

        await expect(page.getByText("新增用户 Developer")).toBeVisible();
        expect(auditLog.getPageRequest()).toEqual(
            expect.objectContaining({
                beginDate: "2026-06-18 00:00:00",
                endDate: "2026-06-18 23:59:59",
                pageNo: 1,
                pageSize: 20
            })
        );
    });

    test("blocks system log queries without system:log:view", async ({ page }) => {
        const systemLogPage = { requestCount: 0 };
        let systemLogRequest: Record<string, unknown> | undefined;
        await mockShellApis(page, AUDIT_LOG_VIEWER_PERMISSIONS);
        await page.route("**/kuzhambu-admin-api/api/sys/log/page", async (route) => {
            systemLogPage.requestCount += 1;
            systemLogRequest = route.request().postDataJSON();
            await route.fulfill({ status: 403, body: "forbidden" });
        });

        await page.goto("/system/logs");
        await expect(page.getByText("缺少 system:log:view 权限")).toBeVisible();

        expect(systemLogPage.requestCount).toBe(0);
        expect(systemLogRequest).toBeUndefined();
    });

    test("blocks audit log queries without audit:view", async ({ page }) => {
        let optionsRequestCount = 0;
        let auditPageRequestCount = 0;
        let detailRequestCount = 0;
        await mockShellApis(page, SYSTEM_LOG_VIEWER_PERMISSIONS);
        await page.route("**/kuzhambu-admin-api/api/audit/log/options", async (route) => {
            optionsRequestCount += 1;
            await route.fulfill({ status: 403, body: "forbidden" });
        });
        await page.route("**/kuzhambu-admin-api/api/audit/log/page", async (route) => {
            auditPageRequestCount += 1;
            await route.fulfill({ status: 403, body: "forbidden" });
        });
        await page.route("**/kuzhambu-admin-api/api/audit/log/detail", async (route) => {
            detailRequestCount += 1;
            await route.fulfill({ status: 403, body: "forbidden" });
        });

        await page.goto("/audit/logs");
        await expect(page.getByText("缺少 audit:view 权限")).toBeVisible();

        expect(optionsRequestCount).toBe(0);
        expect(auditPageRequestCount).toBe(0);
        expect(detailRequestCount).toBe(0);
    });
});
