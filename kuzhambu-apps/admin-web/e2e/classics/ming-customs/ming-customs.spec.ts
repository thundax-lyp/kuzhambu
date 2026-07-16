import { expect, test } from "@playwright/test";

const readRequestBody = (postData: string | null) => {
    return postData ? (JSON.parse(postData) as Record<string, unknown>) : {};
};

const apiResponse = (data: unknown) => ({
    code: "COMMON-00000",
    message: "success",
    data
});

test.describe("classics ming customs page", () => {
    const versionsListRequests: Array<Record<string, unknown>> = [];
    const versionsGetRequests: Array<Record<string, unknown>> = [];
    const versionsResetRequests: Array<Record<string, unknown>> = [];

    test.beforeEach(async ({ page }) => {
        versionsListRequests.length = 0;
        versionsGetRequests.length = 0;
        versionsResetRequests.length = 0;
        await page.route("**/kuzhambu-admin-api/api/sys/current-user/info", async (route) => {
            await route.fulfill({
                contentType: "application/json",
                body: JSON.stringify(
                    apiResponse({
                        id: "1",
                        loginName: "developer",
                        name: "Developer"
                    })
                )
            });
        });
        await page.route("**/kuzhambu-admin-api/api/sys/current-user/menus", async (route) => {
            await route.fulfill({
                contentType: "application/json",
                body: JSON.stringify(
                    apiResponse([
                        {
                            id: "20",
                            name: "古籍管理",
                            displayParams: '{"icon":"classics"}'
                        },
                        {
                            id: "22",
                            parentId: "20",
                            name: "明代习俗",
                            url: "/classics/ming-customs",
                            displayParams: '{"icon":"ming-customs"}'
                        }
                    ])
                )
            });
        });
        await page.route("**/kuzhambu-admin-api/api/sys/current-user/perms", async (route) => {
            await route.fulfill({
                contentType: "application/json",
                body: JSON.stringify(
                    apiResponse({
                        perms: [
                            "classics:mingcustoms:view",
                            "classics:mingcustoms:edit",
                            "classics:mingcustoms:delete"
                        ]
                    })
                )
            });
        });
        await page.addInitScript(() => {
            window.localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
            window.localStorage.setItem("kuzhambu.admin.refreshToken", "refresh-token");
            window.localStorage.setItem(
                "kuzhambu.admin.accessTokenExpireAt",
                String(Date.now() + 3600 * 1000)
            );
            window.localStorage.setItem(
                "kuzhambu.admin.permissions",
                JSON.stringify([
                    "classics:mingcustoms:view",
                    "classics:mingcustoms:edit",
                    "classics:mingcustoms:delete"
                ])
            );
        });
    });

    test("links list filters, editor, rich content, sharing and deletion to APIs", async ({
        page
    }) => {
        const pageRequests: Array<Record<string, unknown>> = [];
        const addRequests: Array<Record<string, unknown>> = [];
        const updateRequests: Array<Record<string, unknown>> = [];
        const deleteRequests: Array<Record<string, unknown>> = [];
        const shareRequests: Array<Record<string, unknown>> = [];

        await page.route("**/kuzhambu-admin-api/api/sys/dict/page", async (route) => {
            await route.fulfill({
                contentType: "application/json",
                body: JSON.stringify(
                    apiResponse({
                        pageNo: 1,
                        pageSize: 100,
                        totalCount: 2,
                        count: 2,
                        totalPage: 1,
                        records: [
                            {
                                type: "CLASSICS_MING_CUSTOMS_CATEGORY",
                                value: "RITUAL",
                                label: "礼制"
                            },
                            {
                                type: "CLASSICS_MING_CUSTOMS_CATEGORY",
                                value: "FESTIVAL",
                                label: "岁时节令"
                            }
                        ]
                    })
                )
            });
        });
        await page.route("**/kuzhambu-admin-api/api/classics/ming-customs/page", async (route) => {
            const body = readRequestBody(route.request().postData());
            pageRequests.push(body);
            await route.fulfill({
                contentType: "application/json",
                body: JSON.stringify(
                    apiResponse({
                        pageNo: body.pageNo ?? 1,
                        pageSize: body.pageSize ?? 20,
                        totalCount: 1,
                        count: 1,
                        totalPage: 1,
                        records: [
                            {
                                id: 500000000001,
                                title: "岁时礼仪：元旦朝贺",
                                category: body.category ?? "RITUAL",
                                chapter: "岁时礼仪",
                                section: "正旦",
                                summary: "记录明代正旦朝贺与家族拜礼。",
                                contentFormat: "MARKDOWN",
                                content: "## 正旦\n\n士民相贺。",
                                originalExcerpts: "正旦朝贺。",
                                visibility: body.visibility ?? "PUBLIC"
                            }
                        ]
                    })
                )
            });
        });
        await page.route(
            "**/kuzhambu-admin-api/api/classics/ming-customs/keyword-cloud**",
            async (route) => {
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify(
                        apiResponse([
                            { keyword: "礼制", count: 8 },
                            { keyword: "正旦", count: 2 }
                        ])
                    )
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/ming-customs/500000000001",
            async (route) => {
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify(
                        apiResponse({
                            id: 500000000001,
                            title: "岁时礼仪：元旦朝贺",
                            category: "RITUAL",
                            chapter: "岁时礼仪",
                            section: "正旦",
                            summary: "记录明代正旦朝贺与家族拜礼。",
                            contentFormat: "HTML",
                            content:
                                "<h2>正旦</h2><img src=x onerror=alert(1)><script>alert(1)</script>",
                            originalExcerpts: "正旦朝贺。",
                            visibility: "PUBLIC"
                        })
                    )
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/ming-customs/versions/list",
            async (route) => {
                versionsListRequests.push(readRequestBody(route.request().postData()));
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify(
                        apiResponse([
                            {
                                id: 9001,
                                contentType: "MING_CUSTOMS",
                                contentId: 500000000001,
                                versionNo: 1,
                                versionedAt: "2026-01-01T00:00:00.000+00:00",
                                snapshotJson: JSON.stringify({
                                    title: "旧标题",
                                    category: "RITUAL",
                                    chapter: "岁时礼仪",
                                    section: "正旦",
                                    summary: "旧版摘要",
                                    contentFormat: "MARKDOWN",
                                    content: "## 旧版",
                                    originalExcerpts: "旧版摘录",
                                    visibility: "PUBLIC"
                                }),
                                changeType: "HISTORY_RESTORED",
                                changeSummary: "恢复历史版本 v1"
                            }
                        ])
                    )
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/ming-customs/versions/get",
            async (route) => {
                const body = readRequestBody(route.request().postData());
                versionsGetRequests.push(body);
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify(
                        apiResponse({
                            id: 9001,
                            contentType: "MING_CUSTOMS",
                            contentId: 500000000001,
                            versionNo: 1,
                            versionedAt: "2026-01-01T00:00:00.000+00:00",
                            snapshotJson: JSON.stringify({
                                title: "旧标题",
                                category: "RITUAL",
                                chapter: "岁时礼仪",
                                section: "正旦",
                                summary: "旧版摘要",
                                contentFormat: "MARKDOWN",
                                content: "## 旧版",
                                originalExcerpts: "旧版摘录",
                                visibility: "PUBLIC"
                            }),
                            changeType: "HISTORY_RESTORED",
                            changeSummary: "恢复历史版本 v1"
                        })
                    )
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/ming-customs/versions/reset",
            async (route) => {
                const body = readRequestBody(route.request().postData());
                versionsResetRequests.push(body);
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify(
                        apiResponse({
                            id: 9002,
                            contentType: "MING_CUSTOMS",
                            contentId: body.id,
                            versionNo: 2,
                            versionedAt: "2026-01-02T00:00:00.000+00:00",
                            snapshotJson: JSON.stringify({
                                title: "恢复后标题",
                                category: "RITUAL",
                                chapter: "岁时礼仪",
                                section: "正旦",
                                summary: "恢复后摘要",
                                contentFormat: "MARKDOWN",
                                content: "## 恢复正文",
                                originalExcerpts: "恢复后摘录",
                                visibility: "PUBLIC"
                            }),
                            changeType: "HISTORY_RESTORED",
                            changeSummary: "恢复历史版本 v1"
                        })
                    )
                });
            }
        );
        await page.route("**/kuzhambu-admin-api/api/classics/ming-customs/add", async (route) => {
            addRequests.push(readRequestBody(route.request().postData()));
            await route.fulfill({
                contentType: "application/json",
                body: JSON.stringify(apiResponse({ id: 500000000003 }))
            });
        });
        await page.route(
            "**/kuzhambu-admin-api/api/classics/ming-customs/update",
            async (route) => {
                updateRequests.push(readRequestBody(route.request().postData()));
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify(apiResponse({ id: 500000000001 }))
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/ming-customs/delete",
            async (route) => {
                deleteRequests.push(readRequestBody(route.request().postData()));
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify(apiResponse(true))
                });
            }
        );
        await page.route("**/kuzhambu-admin-api/api/classics/shares/create", async (route) => {
            shareRequests.push(readRequestBody(route.request().postData()));
            await route.fulfill({
                contentType: "application/json",
                body: JSON.stringify(
                    apiResponse({
                        id: 900000000001,
                        shareToken: "abc123_-",
                        shareUrl: "http://127.0.0.1:5174/share/abc123_-",
                        title: "岁时礼仪：元旦朝贺 分享",
                        visibility: "PUBLIC"
                    })
                )
            });
        });

        await page.setViewportSize({ width: 1280, height: 800 });
        await page.goto("/classics/ming-customs");

        await expect(page.getByRole("heading", { name: "明代习俗" })).toBeVisible();
        await expect(page.getByRole("table", { name: "明代习俗表格" })).toBeVisible();
        await expect(page.getByText("岁时礼仪：元旦朝贺")).toBeVisible();
        await expect
            .poll(() => pageRequests.at(-1))
            .toEqual({
                pageNo: 1,
                pageSize: 20,
                sortDirection: "DESC"
            });

        await page.getByRole("textbox", { name: "搜索明代习俗" }).fill("礼制");
        await expect
            .poll(() => pageRequests.at(-1))
            .toMatchObject({
                keyword: "礼制",
                sortDirection: "DESC"
            });
        await page.getByRole("button", { name: "filter 筛选" }).click();
        await page.getByRole("combobox", { name: "明代习俗分类" }).click();
        await page.getByTitle("礼制").click();
        await page.getByRole("button", { name: /查\s*询/ }).click();
        await expect
            .poll(() => pageRequests.at(-1))
            .toMatchObject({
                keyword: "礼制",
                category: "RITUAL",
                sortDirection: "DESC"
            });

        const keywordCloud = page.getByLabel("明代习俗关键词云");
        await expect(keywordCloud.getByText("8")).toBeVisible();
        await keywordCloud.getByRole("button", { name: "筛选关键词 正旦，2 次" }).click();
        await expect
            .poll(() => pageRequests.at(-1))
            .toMatchObject({
                keyword: "正旦",
                category: "RITUAL",
                sortDirection: "DESC"
            });

        await page.getByRole("button", { name: "新增明代习俗" }).click();
        await page.getByRole("textbox", { name: "明代习俗标题" }).fill("上元灯市");
        await page.getByRole("combobox", { name: "明代习俗编辑分类" }).click();
        await page.getByTitle("岁时节令").last().click();
        await page.getByRole("textbox", { name: "明代习俗正文" }).fill("## 上元\n\n灯市连宵。");
        await page.getByTestId("classics-ming-customs-ming-customs-create-button").click();
        await expect
            .poll(() => addRequests.at(-1))
            .toMatchObject({
                title: "上元灯市",
                category: "FESTIVAL",
                contentFormat: "MARKDOWN",
                content: "## 上元\n\n灯市连宵。",
                visibility: "PUBLIC"
            });

        await page.getByLabel("编辑明代习俗 岁时礼仪：元旦朝贺").click();
        const preview = page.getByLabel("明代习俗正文预览");
        await expect(preview.getByRole("heading", { name: "正旦" })).toBeVisible();
        await expect(preview.locator("script")).toHaveCount(0);
        await expect(preview.getByText("alert(1)")).toHaveCount(0);
        await page.getByRole("textbox", { name: "明代习俗正文" }).fill("更新后的正文");
        await page.getByTestId("classics-ming-customs-ming-customs-create-button").click();
        await expect
            .poll(() => updateRequests.at(-1))
            .toMatchObject({
                id: 500000000001,
                content: "更新后的正文",
                contentFormat: "HTML",
                visibility: "PUBLIC"
            });

        await page.getByRole("button", { name: "分享 岁时礼仪：元旦朝贺" }).click();
        await expect
            .poll(() => shareRequests.at(-1))
            .toEqual({
                targets: [
                    {
                        contentId: 500000000001,
                        contentType: "MING_CUSTOMS"
                    }
                ],
                title: "岁时礼仪：元旦朝贺 分享",
                visibility: "PUBLIC"
            });

        await page.getByRole("button", { name: "删除 岁时礼仪：元旦朝贺" }).click();
        const confirmDialog = page.getByRole("dialog");
        await expect(page.getByText("确认删除 岁时礼仪：元旦朝贺？")).toBeVisible();
        await confirmDialog.getByRole("button", { name: /删\s*除/ }).click();
        await expect
            .poll(() => deleteRequests.at(-1))
            .toEqual({
                id: 500000000001
            });
    });

    test("restores selected ming customs version from history panel", async ({ page }) => {
        await page.setViewportSize({ width: 1280, height: 800 });
        await page.goto("/classics/ming-customs");

        await page.getByRole("button", { name: "编辑明代习俗 岁时礼仪：元旦朝贺" }).click();
        await expect(page.getByLabel("明代习俗版本历史面板")).toBeVisible();
        await page.getByRole("button", { name: "查看明代习俗版本 1" }).click();
        await expect(page.getByText("标题")).toBeVisible();
        await expect(page.getByText("当前：岁时礼仪：元旦朝贺")).toBeVisible();
        await expect(page.getByText("历史：旧标题")).toBeVisible();

        await expect.poll(() => versionsListRequests.at(-1)).toEqual({ id: 500000000001 });
        await expect
            .poll(() => versionsGetRequests.at(-1))
            .toMatchObject({ id: 500000000001, versionId: 9001 });

        await page.getByRole("button", { name: "恢复明代习俗版本 1" }).click();
        const confirmDialog = page.getByRole("dialog");
        await expect(confirmDialog.getByText("确认恢复明代习俗历史版本")).toBeVisible();
        await confirmDialog.getByRole("button", { name: /确认/ }).click();

        await expect(page.getByText("明代习俗版本已恢复")).toBeVisible();
        await expect
            .poll(() => versionsResetRequests.at(-1))
            .toEqual({ id: 500000000001, versionId: 9001 });
    });
});
