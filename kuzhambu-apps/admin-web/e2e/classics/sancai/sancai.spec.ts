import { expect, test } from "@playwright/test";

const readRequestBody = (postData: string | null) => {
    return postData ? (JSON.parse(postData) as Record<string, unknown>) : {};
};

test.describe("classics sancai page", () => {
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
        await page.route("**/admin-api/api/sys/current-user/perms", async (route) => {
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
        const updateRequests: Array<Record<string, unknown>> = [];

        await page.route("**/admin-api/api/classics/sancai/categories/list", async (route) => {
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
        });
        await page.route("**/admin-api/api/classics/sancai/categories/types", async (route) => {
            await route.fulfill({
                contentType: "application/json",
                body: JSON.stringify({
                    code: "COMMON-00000",
                    message: "success",
                    data: [{ label: "正式门类", type: "SANCAI_CATEGORY_TYPE", value: "FORMAL" }]
                })
            });
        });
        await page.route("**/admin-api/api/classics/sancai/volumes/types", async (route) => {
            await route.fulfill({
                contentType: "application/json",
                body: JSON.stringify({
                    code: "COMMON-00000",
                    message: "success",
                    data: [{ label: "正式卷目", type: "SANCAI_VOLUME_TYPE", value: "FORMAL" }]
                })
            });
        });
        await page.route("**/admin-api/api/classics/sancai/volumes/list", async (route) => {
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
        });
        await page.route("**/admin-api/api/classics/sancai/entries/list", async (route) => {
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
                            originalText: "原文",
                            translationText: "译文",
                            summary: "天地初分，清浊定位。",
                            lifecycleStatus: body.lifecycleStatus ?? "PUBLISHED",
                            visibility: "PUBLIC",
                            translationStatus: "TRANSLATED",
                            imageStatus: "HAS_IMAGE",
                            visualAssetStatus: "READY",
                            refinementStatus: "COMPLETE"
                        }
                    ]
                })
            });
        });
        await page.route("**/admin-api/api/classics/sancai/entries/update", async (route) => {
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
        });

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
