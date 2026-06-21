import { expect, test } from "@playwright/test";

const readRequestBody = (postData: string | null) => {
    return postData ? (JSON.parse(postData) as Record<string, unknown>) : {};
};

test.describe("classics sancai page", () => {
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
                        perms: ["classics:sancai:view", "classics:sancai:edit"]
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
            window.localStorage.setItem(
                "kuzhambu.admin.permissions",
                JSON.stringify(["classics:sancai:view", "classics:sancai:edit"])
            );
        });
    });

    test("links category, volume, keyword and status filters to backend request bodies", async ({
        page
    }) => {
        const volumeRequests: Array<Record<string, unknown>> = [];
        const entryListRequests: Array<Record<string, unknown>> = [];
        const resetRequests: Array<Record<string, unknown>> = [];
        const updateRequests: Array<Record<string, unknown>> = [];
        let entryRestored = false;

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
                            },
                            {
                                id: 3,
                                title: "地理",
                                categoryType: "FORMAL",
                                priority: 20
                            }
                        ]
                    })
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/sancai/categories/types",
            async (route) => {
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify({
                        code: "COMMON-00000",
                        message: "success",
                        data: [{ label: "正式门类", type: "SANCAI_CATEGORY_TYPE", value: "FORMAL" }]
                    })
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/sancai/volumes/types",
            async (route) => {
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify({
                        code: "COMMON-00000",
                        message: "success",
                        data: [{ label: "正式卷目", type: "SANCAI_VOLUME_TYPE", value: "FORMAL" }]
                    })
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/sancai/volumes/list",
            async (route) => {
                const body = readRequestBody(route.request().postData());
                volumeRequests.push(body);
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify({
                        code: "COMMON-00000",
                        message: "success",
                        data: [
                            {
                                id: 101,
                                categoryId: body.categoryId ?? 2,
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
            "**/kuzhambu-admin-api/api/classics/sancai/entries/list",
            async (route) => {
                const body = readRequestBody(route.request().postData());
                entryListRequests.push(body);
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify({
                        code: "COMMON-00000",
                        message: "success",
                        data: [
                            {
                                id: 3001,
                                volumeId: 101,
                                title: "天地",
                                originalText: entryRestored ? "历史原文" : "原文",
                                translationText: entryRestored ? "历史译文" : "译文",
                                summary: entryRestored ? "历史摘要" : "天地初分，清浊定位。",
                                lifecycleStatus: body.lifecycleStatus ?? "PUBLISHED",
                                visibility: "PUBLIC",
                                translationStatus: "TRANSLATED",
                                imageStatus: "HAS_IMAGE",
                                visualAssetStatus: "READY",
                                refinementStatus: "COMPLETE",
                                currentVersionId: entryRestored ? 9002 : 9001,
                                currentVersionNo: entryRestored ? 2 : 1,
                                currentVersionedAt: entryRestored
                                    ? "2026-06-21T01:00:00.000+08:00"
                                    : "2026-06-20T01:00:00.000+08:00",
                                contentUpdatedAt: entryRestored
                                    ? "2026-06-21T01:00:00.000+08:00"
                                    : "2026-06-20T01:00:00.000+08:00",
                                versionDirty: false
                            }
                        ]
                    })
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/sancai/entries/3001",
            async (route) => {
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify({
                        code: "COMMON-00000",
                        message: "success",
                        data: {
                            id: 3001,
                            volumeId: 101,
                            title: entryRestored ? "历史天地" : "天地",
                            originalText: entryRestored ? "历史原文" : "原文",
                            translationText: entryRestored ? "历史译文" : "译文",
                            summary: entryRestored ? "历史摘要" : "天地初分，清浊定位。",
                            lifecycleStatus: "PUBLISHED",
                            visibility: "PUBLIC",
                            translationStatus: "TRANSLATED",
                            imageStatus: "HAS_IMAGE",
                            visualAssetStatus: "READY",
                            refinementStatus: "COMPLETE",
                            currentVersionId: entryRestored ? 9002 : 9001,
                            currentVersionNo: entryRestored ? 2 : 1,
                            currentVersionedAt: entryRestored
                                ? "2026-06-21T01:00:00.000+08:00"
                                : "2026-06-20T01:00:00.000+08:00",
                            contentUpdatedAt: entryRestored
                                ? "2026-06-21T01:00:00.000+08:00"
                                : "2026-06-20T01:00:00.000+08:00",
                            versionDirty: false
                        }
                    })
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/sancai/entries/versions/list",
            async (route) => {
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify({
                        code: "COMMON-00000",
                        message: "success",
                        data: [
                            {
                                id: 9001,
                                contentType: "SANCAI_ENTRY",
                                contentId: 3001,
                                versionNo: 1,
                                versionedAt: "2026-06-20T01:00:00.000+08:00",
                                snapshotJson: JSON.stringify({
                                    contentType: "SANCAI_ENTRY",
                                    contentId: 3001,
                                    volumeId: 101,
                                    title: "历史天地",
                                    originalText: "历史原文",
                                    translationText: "历史译文",
                                    summary: "历史摘要",
                                    lifecycleStatus: "PUBLISHED",
                                    visibility: "PUBLIC",
                                    translationStatus: "TRANSLATED",
                                    imageStatus: "HAS_IMAGE",
                                    visualAssetStatus: "READY",
                                    refinementStatus: "COMPLETE",
                                    priority: 1
                                }),
                                changeType: "MANUAL_SAVE",
                                changeSummary: "手动保存"
                            }
                        ]
                    })
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/sancai/entries/versions/get",
            async (route) => {
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify({
                        code: "COMMON-00000",
                        message: "success",
                        data: {
                            id: 9001,
                            contentType: "SANCAI_ENTRY",
                            contentId: 3001,
                            versionNo: 1,
                            versionedAt: "2026-06-20T01:00:00.000+08:00",
                            snapshotJson: JSON.stringify({
                                title: "历史天地",
                                originalText: "历史原文",
                                summary: "历史摘要"
                            }),
                            changeType: "MANUAL_SAVE",
                            changeSummary: "手动保存"
                        }
                    })
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/sancai/entries/versions/reset",
            async (route) => {
                resetRequests.push(readRequestBody(route.request().postData()));
                entryRestored = true;
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify({
                        code: "COMMON-00000",
                        message: "success",
                        data: {
                            id: 9002,
                            contentType: "SANCAI_ENTRY",
                            contentId: 3001,
                            versionNo: 2,
                            changeType: "HISTORY_RESTORED",
                            changeSummary: "恢复历史版本 v1"
                        }
                    })
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/sancai/entries/update",
            async (route) => {
                updateRequests.push(readRequestBody(route.request().postData()));
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify({
                        code: "COMMON-00000",
                        message: "success",
                        data: {
                            id: 3001
                        }
                    })
                });
            }
        );

        await page.setViewportSize({ width: 1280, height: 800 });
        await page.goto("/classics/sancai");

        await expect(page.getByRole("heading", { name: "三才图会" })).toBeVisible();
        const catalogTree = page.getByLabel("三才图会目录树", { exact: true });
        await expect(catalogTree).toBeVisible();
        await expect(page.getByRole("table", { name: "三才图会门类表格" })).toBeVisible();

        expect(entryListRequests).toHaveLength(0);
        await catalogTree.getByText("天文", { exact: true }).click();
        await expect(catalogTree.getByText("天文卷一")).toBeVisible();
        expect(volumeRequests.at(-1)).toEqual({});
        expect(entryListRequests).toHaveLength(0);

        await catalogTree.getByText("天文卷一").click();
        await expect(page.getByRole("table", { name: "三才图会条目表格" })).toBeVisible();
        await expect(page.getByRole("columnheader", { name: "条目" })).toBeVisible();
        await expect(page.getByRole("columnheader", { name: "卷" })).toBeVisible();
        await expect(page.getByRole("columnheader", { name: "状态" })).toBeVisible();
        await expect(page.getByRole("columnheader", { name: "摘要" })).toBeVisible();
        await expect(page.getByRole("columnheader", { name: "操作" })).toBeVisible();
        await expect(page.getByRole("button", { name: /查看/ })).toBeVisible();
        await expect
            .poll(() => entryListRequests.at(-1))
            .toEqual({
                categoryId: 2,
                volumeId: 101,
                keyword: null,
                lifecycleStatus: null,
                sortDirection: "ASC"
            });

        await page.getByRole("textbox", { name: "搜索三才图会条目" }).fill("天地");
        await page.getByRole("button", { name: "筛选" }).click();
        await page.getByRole("combobox", { name: "三才图会条目状态" }).click();
        await page.getByTitle("已发布").click();
        await page.getByRole("button", { name: /查\s*询/ }).click();
        await expect
            .poll(() => entryListRequests.at(-1))
            .toEqual({
                categoryId: 2,
                volumeId: 101,
                keyword: "天地",
                lifecycleStatus: "PUBLISHED",
                sortDirection: "ASC"
            });

        await page.getByRole("button", { name: "筛选" }).click();
        await page.getByRole("button", { name: /重\s*置/ }).click();
        await expect(page.getByRole("textbox", { name: "搜索三才图会条目" })).toHaveValue("");
        await expect(page.locator(".kuzhambu-filter-panel").getByText("全部状态")).toBeVisible();

        await page.getByRole("button", { name: /查看/ }).click();
        await expect(page.getByLabel("三才图会版本历史面板")).toBeVisible();
        await page.getByRole("button", { name: "查看三才图会版本 1" }).click();
        await expect(page.getByText("历史：历史天地")).toBeVisible();
        await page.getByRole("button", { name: "恢复三才图会版本 1" }).click();
        await page
            .locator(".ant-modal-wrap")
            .getByRole("button", { name: /恢\s*复/ })
            .click();
        await expect
            .poll(() => resetRequests.at(-1))
            .toEqual({
                id: 3001,
                versionId: 9001
            });
        await expect(
            page.locator(".ant-modal-confirm-title", { hasText: "三才图会版本已恢复" })
        ).toBeVisible();
        await page.locator(".ant-modal-wrap").getByRole("button", { name: "知道了" }).click();
        await expect(page.getByRole("textbox", { name: "三才图会条目标题" })).toHaveValue(
            "历史天地"
        );

        await page.getByRole("textbox", { name: "三才图会条目标题" }).fill("天地新解");
        await page.getByRole("textbox", { name: "三才图会原文" }).fill("新原文");
        await page.getByRole("textbox", { name: "三才图会译文" }).fill("新译文");
        await page.getByRole("textbox", { name: "三才图会摘要" }).fill("新摘要");
        await page.getByRole("switch", { name: "三才图会公开状态" }).click();
        await page.getByRole("button", { name: "保存三才图会条目" }).click();
        await expect
            .poll(() => updateRequests.at(-1))
            .toEqual({
                id: 3001,
                volumeId: 101,
                title: "天地新解",
                originalText: "新原文",
                translationText: "新译文",
                summary: "新摘要",
                lifecycleStatus: "PUBLISHED",
                visibility: "PRIVATE",
                translationStatus: "TRANSLATED",
                imageStatus: "HAS_IMAGE",
                visualAssetStatus: "READY",
                refinementStatus: "COMPLETE"
            });
    });
});
