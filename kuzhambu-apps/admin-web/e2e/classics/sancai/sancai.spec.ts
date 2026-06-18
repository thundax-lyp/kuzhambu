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
            window.localStorage.setItem(
                "kuzhambu.admin.accessTokenExpireAt",
                String(Date.now() + 3600 * 1000)
            );
        });
    });

    test("links category, volume, keyword and status filters to backend request bodies", async ({
        page
    }) => {
        const volumeRequests: Array<Record<string, unknown>> = [];
        const entryRequests: Array<Record<string, unknown>> = [];

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
        await page.route("**/admin-api/api/classics/sancai/entries/page", async (route) => {
            const body = readRequestBody(route.request().postData());
            entryRequests.push(body);
            await route.fulfill({
                contentType: "application/json",
                body: JSON.stringify({
                    code: "COMMON-00000",
                    message: "success",
                    data: {
                        pageNo: body.pageNo ?? 1,
                        pageSize: body.pageSize ?? 20,
                        totalCount: 1,
                        records: [
                            {
                                id: 3001,
                                volumeId: 101,
                                title: "天地",
                                summary: "天地初分，清浊定位。",
                                lifecycleStatus: body.lifecycleStatus ?? "PUBLISHED"
                            }
                        ]
                    }
                })
            });
        });

        await page.goto("/classics/sancai");

        await expect(page.getByRole("heading", { name: "三才图会" })).toBeVisible();
        const categoryList = page.getByLabel("三才图会门类");
        await expect(categoryList.getByRole("button", { name: /天文/ })).toBeVisible();
        expect(entryRequests.at(-1)).toEqual({
            pageNo: 1,
            pageSize: 20
        });

        await categoryList.getByRole("button", { name: /天文/ }).click();
        await expect
            .poll(() => volumeRequests.at(-1))
            .toEqual({
                categoryId: 2
            });
        await expect
            .poll(() => entryRequests.at(-1))
            .toEqual({
                categoryId: 2,
                pageNo: 1,
                pageSize: 20
            });

        await page.getByRole("button", { name: /天文卷一/ }).click();
        await expect
            .poll(() => entryRequests.at(-1))
            .toEqual({
                categoryId: 2,
                volumeId: 101,
                pageNo: 1,
                pageSize: 20
            });

        await page.getByPlaceholder("搜索标题、原文或摘要").fill("天地");
        await page.getByRole("combobox").click();
        await page.getByTitle("已发布").click();
        await page.getByRole("button", { name: /查\s*询/ }).click();
        await expect
            .poll(() => entryRequests.at(-1))
            .toEqual({
                categoryId: 2,
                volumeId: 101,
                keyword: "天地",
                lifecycleStatus: "PUBLISHED",
                pageNo: 1,
                pageSize: 20
            });

        await page.getByRole("button", { name: /重\s*置/ }).click();
        await expect
            .poll(() => entryRequests.at(-1))
            .toEqual({
                pageNo: 1,
                pageSize: 20
            });
    });
});
