import { expect, test } from "@playwright/test";
import type { Page } from "@playwright/test";

const USER_DEPARTMENT_PANEL_BOTTOM_GAP = 8;

const expectNoPageHorizontalOverflow = async (page: Page) => {
    await expect
        .poll(async () =>
            page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)
        )
        .toBe(true);

    const metrics = await page.evaluate(() => ({
        scrollWidth: document.documentElement.scrollWidth,
        viewportWidth: window.innerWidth
    }));
    expect(
        metrics.scrollWidth,
        `scrollWidth=${metrics.scrollWidth}, viewport=${metrics.viewportWidth}`
    ).toBeLessThanOrEqual(metrics.viewportWidth);
};

const mockUserManagementApis = async (page: Page) => {
    await page.route("**/admin-api/api/sys/user/department/tree", async (route) => {
        await route.fulfill({
            contentType: "application/json",
            body: JSON.stringify({
                code: "COMMON-00000",
                message: "success",
                data: [
                    {
                        id: "100",
                        name: "技术中心",
                        shortName: "技术",
                        namePath: "技术中心"
                    },
                    {
                        id: "101",
                        parentId: "100",
                        name: "平台研发部",
                        shortName: "平台",
                        namePath: "技术中心/平台研发部"
                    },
                    {
                        id: "102",
                        parentId: "100",
                        name: "质量保障部",
                        shortName: "质量",
                        namePath: "技术中心/质量保障部"
                    }
                ]
            })
        });
    });
    await page.route("**/admin-api/api/sys/user/page", async (route) => {
        await route.fulfill({
            contentType: "application/json",
            body: JSON.stringify({
                code: "COMMON-00000",
                message: "success",
                data: {
                    pageNo: 1,
                    pageSize: 20,
                    totalCount: 18,
                    records: Array.from({ length: 10 }, (_, index) => ({
                        id: String(index + 1),
                        loginName: `user${index + 1}`,
                        name: `User ${index + 1}`,
                        email: `user${index + 1}@example.com`,
                        ranks: index + 1,
                        enable: index % 3 !== 0,
                        department: {
                            id: index % 2 ? "101" : "102",
                            name: index % 2 ? "平台研发部" : "质量保障部",
                            namePath: index % 2 ? "技术中心/平台研发部" : "技术中心/质量保障部"
                        },
                        roles: [{ id: "r1", name: index % 2 ? "管理员" : "观察员" }]
                    }))
                }
            })
        });
    });
    await page.route("**/admin-api/api/sys/user/role/list", async (route) => {
        await route.fulfill({
            contentType: "application/json",
            body: JSON.stringify({
                code: "COMMON-00000",
                message: "success",
                data: [
                    {
                        id: "r1",
                        name: "管理员"
                    },
                    {
                        id: "r2",
                        name: "观察员"
                    }
                ]
            })
        });
    });
    await page.route("**/admin-api/api/sys/user/options", async (route) => {
        await route.fulfill({
            contentType: "application/json",
            body: JSON.stringify({
                code: "COMMON-00000",
                message: "success",
                data: {
                    rankOptions: [
                        {
                            label: "普通用户",
                            value: 1
                        }
                    ],
                    statusOptions: [
                        {
                            label: "启用",
                            value: true
                        },
                        {
                            label: "停用",
                            value: false
                        }
                    ]
                }
            })
        });
    });
};

const readUserDepartmentPanelMetrics = async (page: Page) => {
    return page.evaluate(() => {
        const rect = (selector: string) => {
            const element = document.querySelector(selector);
            if (!element) {
                throw new Error(`${selector} not found`);
            }
            const bounds = element.getBoundingClientRect();
            return {
                bottom: bounds.bottom,
                height: bounds.height,
                top: bounds.top
            };
        };

        return {
            panel: rect(".user-department-panel"),
            sidebar: rect(".sidebar"),
            topbar: rect(".topbar")
        };
    });
};

const readSidebarInkMetrics = async (page: Page) => {
    return page.evaluate(() => {
        const rect = (selector: string) => {
            const element = document.querySelector(selector);
            if (!element) {
                throw new Error(`${selector} not found`);
            }
            const bounds = element.getBoundingClientRect();
            return {
                bottom: bounds.bottom,
                height: bounds.height,
                left: bounds.left,
                right: bounds.right,
                top: bounds.top,
                width: bounds.width
            };
        };
        const ink = document.querySelector(".sidebar-ink-1");
        if (!ink) {
            throw new Error(".sidebar-ink-1 not found");
        }

        return {
            ink: rect(".sidebar-ink-1"),
            opacity: Number(getComputedStyle(ink).opacity),
            sidebar: rect(".sidebar")
        };
    });
};

test.describe("admin layout", () => {
    test.beforeEach(async ({ page }) => {
        await page.route("**/admin-api/api/sys/current-user/info", async (route) => {
            await route.fulfill({
                contentType: "application/json",
                body: JSON.stringify({
                    code: "COMMON-00000",
                    message: "success",
                    data: {
                        id: "1",
                        loginName: "developer",
                        name: "Developer"
                    }
                })
            });
        });
        await page.route("**/admin-api/api/sys/current-user/menus", async (route) => {
            await route.fulfill({
                contentType: "application/json",
                body: JSON.stringify({
                    code: "COMMON-00000",
                    message: "success",
                    data: [
                        {
                            id: "10",
                            name: "仪表盘",
                            url: "/dashboard",
                            displayParams: '{"icon":"dashboard"}'
                        },
                        {
                            id: "11",
                            name: "系统管理",
                            displayParams: '{"icon":"system"}'
                        },
                        {
                            id: "12",
                            parentId: "11",
                            name: "用户管理",
                            url: "/system/users",
                            displayParams: '{"icon":"users"}'
                        }
                    ]
                })
            });
        });
        await page.route("**/admin-api/api/sys/current-user/perms", async (route) => {
            await route.fulfill({
                contentType: "application/json",
                body: JSON.stringify({
                    code: "COMMON-00000",
                    message: "success",
                    data: {
                        perms: ["sys:user:view"]
                    }
                })
            });
        });
        await page.route("**/admin-api/api/sys/dict/page", async (route) => {
            await route.fulfill({
                contentType: "application/json",
                body: JSON.stringify({
                    code: "COMMON-00000",
                    message: "success",
                    data: {
                        pageNo: 1,
                        pageSize: 20,
                        totalCount: 1,
                        records: [
                            {
                                id: "100",
                                type: "system_status",
                                label: "启用",
                                value: "ENABLED",
                                remarks: "默认状态"
                            }
                        ]
                    }
                })
            });
        });
        await page.route("**/admin-api/api/auth/session/token/refresh", async (route) => {
            await route.fulfill({
                contentType: "application/json",
                body: JSON.stringify({
                    code: "COMMON-00000",
                    message: "success",
                    data: {
                        token: "test-token",
                        refreshToken: "refresh-token",
                        expireAt: Date.now() + 3600 * 1000
                    }
                })
            });
        });

        await page.addInitScript(() => {
            window.localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
            window.localStorage.setItem("kuzhambu.admin.refreshToken", "refresh-token");
            window.localStorage.setItem(
                "kuzhambu.admin.accessTokenExpireAt",
                String(Date.now() + 3600 * 1000)
            );
        });
    });

    test("opens and closes the mobile sidebar from the topbar", async ({ page }) => {
        await page.setViewportSize({ width: 390, height: 844 });
        await page.goto("/dashboard");

        const sidebar = page.locator(".sidebar");
        await expect(page.getByRole("heading", { name: "仪表盘" })).toBeVisible();
        await expect(sidebar).not.toHaveClass(/sidebar-mobile-open/);

        await page.getByLabel("展开菜单").click();
        await expect(sidebar).toHaveClass(/sidebar-mobile-open/);
        await expect(page.getByLabel("关闭菜单")).toBeVisible();

        await page.getByLabel("关闭菜单").click();
        await expect(sidebar).not.toHaveClass(/sidebar-mobile-open/);
    });

    test("closes the mobile sidebar after selecting a menu item", async ({ page }) => {
        await page.setViewportSize({ width: 390, height: 844 });
        await page.goto("/dashboard");

        const sidebar = page.locator(".sidebar");
        await page.getByLabel("展开菜单").click();
        await expect(sidebar).toHaveClass(/sidebar-mobile-open/);

        await page.getByRole("menuitem", { name: "仪表盘" }).click();

        await expect(sidebar).not.toHaveClass(/sidebar-mobile-open/);
    });

    test("navigates implemented menu pages without blank content", async ({ page }) => {
        await mockUserManagementApis(page);
        await page.setViewportSize({ width: 1280, height: 800 });
        await page.goto("/dashboard");

        await expect(page.getByRole("heading", { name: "仪表盘" })).toBeVisible();

        await page.getByRole("menuitem", { name: "系统管理" }).click();
        await page.getByRole("menuitem", { name: "用户管理" }).click();
        await expect(page).toHaveURL(/\/system\/users$/);
        await expect(page.getByRole("heading", { name: "用户管理" })).toBeVisible();
    });

    test("keeps the workspace content within the expanded and collapsed desktop widths", async ({
        page
    }) => {
        await page.setViewportSize({ width: 1280, height: 800 });
        await page.goto("/system/dictionaries");

        const main = page.locator(".admin-main");
        const workspaceContent = page.locator(".dictionary-page");
        await expect(page.getByRole("heading", { name: "字典管理" })).toBeVisible();
        await expect(main).toHaveAttribute("data-sidebar-state", "expanded");
        await expect(main).toHaveCSS("width", "996px");
        await expect(workspaceContent).toHaveCSS("width", "996px");
        await expectNoPageHorizontalOverflow(page);

        await page.getByLabel("收起菜单").click();
        await expect(main).toHaveAttribute("data-sidebar-state", "collapsed");
        await expect(main).toHaveCSS("width", "1156px");
        await expect(workspaceContent).toHaveCSS("width", "1156px");
        await expectNoPageHorizontalOverflow(page);

        await page.getByLabel("展开菜单").click();
        await expect(main).toHaveAttribute("data-sidebar-state", "expanded");
        await expect(main).toHaveCSS("width", "996px");
        await expect(workspaceContent).toHaveCSS("width", "996px");
        await expectNoPageHorizontalOverflow(page);
    });

    test("stretches the ink sidebar background across the desktop sidebar", async ({ page }) => {
        await page.setViewportSize({ width: 1280, height: 800 });
        await page.goto("/dashboard");

        await expect(page.getByRole("menuitem", { name: "仪表盘" })).toBeVisible();
        const metrics = await readSidebarInkMetrics(page);
        expect(Math.abs(metrics.ink.top - metrics.sidebar.top)).toBeLessThanOrEqual(1);
        expect(Math.abs(metrics.ink.right - metrics.sidebar.right)).toBeLessThanOrEqual(1);
        expect(Math.abs(metrics.ink.bottom - metrics.sidebar.bottom)).toBeLessThanOrEqual(1);
        expect(Math.abs(metrics.ink.left - metrics.sidebar.left)).toBeLessThanOrEqual(1);
        expect(metrics.opacity).toBe(1);
    });

    test("uses the padded viewport width when the mobile menu is closed or open", async ({
        page
    }) => {
        await page.setViewportSize({ width: 390, height: 844 });
        await page.goto("/system/dictionaries");

        const main = page.locator(".admin-main");
        const workspaceContent = page.locator(".dictionary-page");
        await expect(page.getByRole("heading", { name: "字典管理" })).toBeVisible();
        await expect(main).toHaveAttribute("data-sidebar-state", "closed");
        await expect(main).toHaveCSS("width", "370px");
        await expect(workspaceContent).toHaveCSS("width", "370px");
        await expectNoPageHorizontalOverflow(page);

        await page.getByLabel("展开菜单").click();
        await expect(main).toHaveAttribute("data-sidebar-state", "open");
        await expect(main).toHaveCSS("width", "370px");
        await expect(workspaceContent).toHaveCSS("width", "370px");
        await expectNoPageHorizontalOverflow(page);
    });

    test("keeps the user department tree floating below the topbar", async ({ page }) => {
        await mockUserManagementApis(page);
        await page.setViewportSize({ width: 1280, height: 800 });
        await page.goto("/system/users");

        await expect(page.getByRole("heading", { name: "用户管理" })).toBeVisible();
        const initialMetrics = await readUserDepartmentPanelMetrics(page);
        expect(initialMetrics.panel.top).toBeGreaterThan(initialMetrics.topbar.bottom);
        expect(
            Math.abs(
                initialMetrics.panel.bottom -
                    (initialMetrics.sidebar.bottom - USER_DEPARTMENT_PANEL_BOTTOM_GAP)
            )
        ).toBeLessThanOrEqual(2);

        await page.evaluate(() => window.scrollTo(0, 160));
        await expect
            .poll(async () => {
                const metrics = await readUserDepartmentPanelMetrics(page);
                return {
                    bottomWithinSidebar:
                        metrics.panel.bottom <=
                        metrics.sidebar.bottom - USER_DEPARTMENT_PANEL_BOTTOM_GAP + 2
                };
            })
            .toEqual({
                bottomWithinSidebar: true
            });
    });
});
