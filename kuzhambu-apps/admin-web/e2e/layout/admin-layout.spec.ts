import { expect, test } from "@playwright/test";
import type { Page } from "@playwright/test";

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
    await page.route("**/kuzhambu-admin-api/api/sys/user/department/tree", async (route) => {
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
                    },
                    {
                        id: "103",
                        name: "战略发展部",
                        shortName: "战略",
                        namePath: "战略发展部"
                    }
                ]
            })
        });
    });
    await page.route("**/kuzhambu-admin-api/api/sys/user/page", async (route) => {
        const requestBody = route.request().postDataJSON() as { departmentId?: string } | null;
        const selectedDepartmentId = requestBody?.departmentId;
        const records =
            selectedDepartmentId === "103"
                ? [
                      {
                          id: "strategy-1",
                          loginName: "strategy1",
                          name: "Strategy User",
                          email: "strategy1@example.com",
                          ranks: 1,
                          enable: true,
                          department: {
                              id: "103",
                              name: "战略发展部",
                              namePath: "战略发展部"
                          },
                          roles: [{ id: "r1", name: "管理员" }]
                      }
                  ]
                : Array.from({ length: 10 }, (_, index) => ({
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
                  }));
        await route.fulfill({
            contentType: "application/json",
            body: JSON.stringify({
                code: "COMMON-00000",
                message: "success",
                data: {
                    pageNo: 1,
                    pageSize: 20,
                    totalCount: records.length,
                    records
                }
            })
        });
    });
    await page.route("**/kuzhambu-admin-api/api/sys/user/role/list", async (route) => {
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
    await page.route("**/kuzhambu-admin-api/api/sys/user/options", async (route) => {
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
            workPanel: rect(".user-work-panel"),
            topbar: rect(".topbar")
        };
    });
};

const readUserDepartmentSplitMetrics = async (page: Page) => {
    return page.evaluate(() => {
        const panel = document.querySelector(".user-department-panel");
        const dragger = document.querySelector(
            ".user-department-work-area > .ant-splitter-bar .ant-splitter-bar-dragger"
        );
        if (!panel || !dragger) {
            throw new Error("user department splitter not found");
        }
        const panelBounds = panel.getBoundingClientRect();
        const draggerBounds = dragger.getBoundingClientRect();
        const hitElement = document.elementFromPoint(
            draggerBounds.left + draggerBounds.width / 2,
            draggerBounds.top + draggerBounds.height / 2
        );
        return {
            draggerHeight: draggerBounds.height,
            draggerLeft: draggerBounds.left,
            draggerTop: draggerBounds.top,
            draggerWidth: draggerBounds.width,
            hitDragger: Boolean(hitElement?.closest(".ant-splitter-bar-dragger")),
            panelWidth: panelBounds.width
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
        await page.route("**/kuzhambu-admin-api/api/sys/current-user/info", async (route) => {
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
        await page.route("**/kuzhambu-admin-api/api/sys/current-user/menus", async (route) => {
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
                        },
                        {
                            id: "20",
                            name: "古籍管理",
                            displayParams: '{"icon":"classics"}'
                        },
                        {
                            id: "21",
                            parentId: "20",
                            name: "三才图会",
                            url: "/classics/sancai",
                            displayParams: '{"icon":"sancai"}'
                        },
                        {
                            id: "30",
                            name: "运维",
                            displayParams: '{"icon":"operations"}'
                        },
                        {
                            id: "31",
                            parentId: "30",
                            name: "报表管理",
                            url: "/operations/reports",
                            displayParams: '{"icon":"operations-report"}'
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
                        perms: ["sys:user:view", "operations:report:view"]
                    }
                })
            });
        });
        await page.route("**/kuzhambu-admin-api/api/operations/report/page", async (route) => {
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
                                reportId: 9001,
                                reportType: "WEEKLY",
                                format: "PDF",
                                periodStart: "2026-07-01T00:00:00.000Z",
                                periodEnd: "2026-07-07T23:59:59.000Z",
                                reportStatus: "SUCCEEDED",
                                storageObjectId: 7001,
                                requestedAt: "2026-07-08T01:00:00.000Z"
                            }
                        ]
                    }
                })
            });
        });
        await page.route("**/kuzhambu-admin-api/api/sys/dict/page", async (route) => {
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
        await page.route(
            "**/kuzhambu-admin-api/api/classics/sancai/categories/list",
            async (route) => {
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify({
                        code: "COMMON-00000",
                        message: "success",
                        data: [
                            {
                                id: 2,
                                title: "天文",
                                categoryType: "FORMAL",
                                priority: 10
                            }
                        ]
                    })
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/sancai/volumes/list",
            async (route) => {
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify({
                        code: "COMMON-00000",
                        message: "success",
                        data: [
                            {
                                id: 101,
                                categoryId: 2,
                                title: "天文卷一",
                                volumeType: "FORMAL",
                                priority: 101
                            }
                        ]
                    })
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/sancai/entries/page",
            async (route) => {
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
                                    id: 3001,
                                    volumeId: 101,
                                    title: "天地",
                                    summary: "天地初分，清浊定位。",
                                    lifecycleStatus: "PUBLISHED"
                                }
                            ]
                        }
                    })
                });
            }
        );
        await page.route("**/kuzhambu-admin-api/api/auth/session/token/refresh", async (route) => {
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

        await page.getByRole("menuitem", { name: "古籍管理" }).click();
        await page.getByRole("menuitem", { name: "三才图会" }).click();
        await expect(page).toHaveURL(/\/classics\/sancai$/);
        await expect(page.getByRole("heading", { name: "三才图会" })).toBeVisible();

        await page.getByRole("menuitem", { name: "运维" }).click();
        await page.getByRole("menuitem", { name: "报表管理" }).click();
        await expect(page).toHaveURL(/\/operations\/reports$/);
        await expect(page.getByRole("heading", { name: "报表管理" })).toBeVisible();
        await expect(page.locator(".menu-icon-config-error")).toHaveCount(0);
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

    test("renders the user department tree and filters users", async ({ page }) => {
        await mockUserManagementApis(page);
        await page.setViewportSize({ width: 1280, height: 800 });
        await page.goto("/system/users");

        await expect(page.getByRole("heading", { name: "用户管理" })).toBeVisible();
        const initialMetrics = await readUserDepartmentPanelMetrics(page);
        expect(initialMetrics.panel.top).toBeGreaterThan(initialMetrics.topbar.bottom);
        expect(initialMetrics.panel.height).toBeGreaterThan(0);
        expect(initialMetrics.workPanel.height).toBeGreaterThan(0);

        await page.getByText("战略发展部").click();
        await expect(page.getByText("Strategy User")).toBeVisible();
    });

    test("keeps the user department splitter easy to drag", async ({ page }) => {
        await mockUserManagementApis(page);
        await page.setViewportSize({ width: 1280, height: 800 });
        await page.goto("/system/users");

        await expect(page.getByRole("heading", { name: "用户管理" })).toBeVisible();
        const initialMetrics = await readUserDepartmentSplitMetrics(page);
        expect(initialMetrics.hitDragger).toBe(true);

        await page.mouse.move(
            initialMetrics.draggerLeft + initialMetrics.draggerWidth / 2,
            initialMetrics.draggerTop + initialMetrics.draggerHeight / 2
        );
        await page.mouse.down();
        await page.mouse.move(
            initialMetrics.draggerLeft + initialMetrics.draggerWidth / 2 + 80,
            initialMetrics.draggerTop + initialMetrics.draggerHeight / 2,
            { steps: 6 }
        );
        await page.mouse.up();

        await expect
            .poll(async () => {
                const metrics = await readUserDepartmentSplitMetrics(page);
                return metrics.panelWidth;
            })
            .toBeGreaterThan(initialMetrics.panelWidth + 40);
    });
});
