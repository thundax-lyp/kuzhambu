import { expect, test } from "@playwright/test";

const readRequestBody = (postData: string | null) => {
    return postData ? (JSON.parse(postData) as Record<string, unknown>) : {};
};

const apiResponse = (data: unknown) => ({
    code: "COMMON-00000",
    message: "success",
    data
});

test.describe("classics wangqi page", () => {
    test.beforeEach(async ({ page }) => {
        await page.route("**/kuzhambu-admin-api/api/sys/current-user/info", async (route) => {
            await route.fulfill({
                contentType: "application/json",
                body: JSON.stringify(
                    apiResponse({ id: "1", loginName: "developer", name: "Developer" })
                )
            });
        });
        await page.route("**/kuzhambu-admin-api/api/sys/current-user/menus", async (route) => {
            await route.fulfill({
                contentType: "application/json",
                body: JSON.stringify(
                    apiResponse([
                        { id: "20", name: "古籍管理", displayParams: '{"icon":"classics"}' },
                        {
                            id: "23",
                            parentId: "20",
                            name: "王圻文档",
                            url: "/classics/wangqi",
                            displayParams: '{"icon":"wangqi"}'
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
                            "classics:wangqi:view",
                            "classics:wangqi:edit",
                            "classics:wangqi:delete"
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
                    "classics:wangqi:view",
                    "classics:wangqi:edit",
                    "classics:wangqi:delete"
                ])
            );
        });
    });

    test("links Wangqi list, editor, file and versions to APIs", async ({ page }) => {
        const pageRequests: Array<Record<string, unknown>> = [];
        const updateRequests: Array<Record<string, unknown>> = [];
        const uploadRequests: Array<string> = [];
        const resetRequests: Array<Record<string, unknown>> = [];
        const deleteRequests: Array<Record<string, unknown>> = [];

        const record = {
            id: 400000000001,
            title: "王圻文档",
            summary: "记录王圻古籍条目。",
            contentFormat: "MARKDOWN",
            content: "## 王圻\n\n古籍正文。",
            documentTime: "2026-01-01T00:00:00.000+00:00",
            storageObjectId: 7001,
            visibility: "PUBLIC"
        };

        await page.route(
            "**/kuzhambu-admin-api/api/classics/wangqi/documents/page",
            async (route) => {
                const body = readRequestBody(route.request().postData());
                pageRequests.push(body);
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify(
                        apiResponse({
                            pageNo: 1,
                            pageSize: 20,
                            totalCount: 1,
                            count: 1,
                            totalPage: 1,
                            records: [{ ...record, visibility: body.visibility ?? "PUBLIC" }]
                        })
                    )
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/wangqi/documents/timeline/list",
            async (route) => {
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify(apiResponse([record]))
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/wangqi/documents/get",
            async (route) => {
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify(
                        apiResponse({
                            ...record,
                            contentFormat: "HTML",
                            content: "<h2>王圻</h2><script>alert(1)</script>"
                        })
                    )
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/wangqi/documents/source-file/get",
            async (route) => {
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify(
                        apiResponse({
                            documentId: 400000000001,
                            storageObjectId: 7001,
                            originalFilename: "wangqi.pdf",
                            contentType: "application/pdf",
                            size: 10,
                            contentUrl:
                                "/classics/wangqi/documents/400000000001/source-file/content"
                        })
                    )
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/wangqi/documents/400000000001/source-file/upload",
            async (route) => {
                uploadRequests.push(route.request().method());
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify(
                        apiResponse({ documentId: 400000000001, storageObjectId: 7002 })
                    )
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/wangqi/documents/versions/list",
            async (route) => {
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify(
                        apiResponse([
                            {
                                id: 9001,
                                versionNo: 1,
                                versionedAt: "2026-01-01T00:00:00.000+00:00",
                                snapshotJson: JSON.stringify({
                                    title: "历史王圻文档",
                                    storageObjectId: 6001
                                }),
                                changeType: "MANUAL_SAVE",
                                changeSummary: "保存王圻文档"
                            }
                        ])
                    )
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/wangqi/documents/versions/get",
            async (route) => {
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify(
                        apiResponse({
                            id: 9001,
                            versionNo: 1,
                            snapshotJson: JSON.stringify({
                                title: "历史王圻文档",
                                storageObjectId: 6001
                            }),
                            changeType: "MANUAL_SAVE"
                        })
                    )
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/wangqi/documents/versions/reset",
            async (route) => {
                resetRequests.push(readRequestBody(route.request().postData()));
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify(apiResponse({ id: 9002, versionNo: 2 }))
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/wangqi/documents/update",
            async (route) => {
                updateRequests.push(readRequestBody(route.request().postData()));
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify(apiResponse({ id: 400000000001 }))
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/wangqi/documents/delete",
            async (route) => {
                deleteRequests.push(readRequestBody(route.request().postData()));
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify(apiResponse(true))
                });
            }
        );

        await page.setViewportSize({ width: 1280, height: 800 });
        await page.goto("/classics/wangqi");

        await expect(page.getByRole("heading", { name: "王圻文档" })).toBeVisible();
        await expect(page.getByRole("table", { name: "王圻文档表格" })).toBeVisible();
        await expect
            .poll(() => pageRequests.at(-1))
            .toEqual({
                pageNo: 1,
                pageSize: 20,
                sortDirection: "DESC"
            });

        await page.getByRole("textbox", { name: "搜索王圻文档" }).fill("万历");
        await expect.poll(() => pageRequests.at(-1)).toMatchObject({ keyword: "万历" });
        await page.getByRole("button", { name: "filter 筛选" }).click();
        await page.getByRole("combobox", { name: "王圻文档可见性" }).click();
        await page.getByTitle("私有").click();
        await page.getByRole("button", { name: /查\s*询/ }).click();
        await expect.poll(() => pageRequests.at(-1)).toMatchObject({ visibility: "PRIVATE" });

        await page.getByRole("button", { name: /查看或编辑 王圻文档/ }).click();
        await expect(page.getByLabel("王圻文档正文预览")).toBeVisible();
        await expect(page.getByLabel("王圻文档正文预览")).not.toContainText("alert(1)");

        await page.getByRole("textbox", { name: "王圻文档标题" }).fill("王圻文档修订");
        await page.getByRole("button", { name: "保存王圻文档" }).click();
        await expect
            .poll(() => updateRequests.at(-1))
            .toMatchObject({
                id: 400000000001,
                title: "王圻文档修订"
            });

        await page.getByRole("button", { name: /查看或编辑 王圻文档/ }).click();
        await page.setInputFiles('input[type="file"]', {
            name: "new-wangqi.pdf",
            mimeType: "application/pdf",
            buffer: Buffer.from("source-bin")
        });
        await expect.poll(() => uploadRequests.at(-1)).toBe("POST");
        await page.getByRole("button", { name: "查看王圻版本 1" }).click();
        await expect(page.getByText("历史：历史王圻文档")).toBeVisible();
        await page.getByRole("button", { name: "恢复王圻版本 1" }).click();
        await page.getByRole("button", { name: /^\s*恢\s*复\s*$/ }).click();
        await expect
            .poll(() => resetRequests.at(-1))
            .toEqual({
                id: 400000000001,
                versionId: 9001
            });
        await expect(page.getByRole("button", { name: /^\s*恢\s*复\s*$/ })).toBeHidden();
        await page
            .getByLabel("编辑王圻文档", { exact: true })
            .getByRole("button", { name: /^\s*取\s*消\s*$/ })
            .click();

        await page.getByRole("button", { name: /删除 王圻文档/ }).click();
        await page.getByRole("button", { name: /^\s*删\s*除\s*$/ }).click();
        await expect.poll(() => deleteRequests.at(-1)).toEqual({ id: 400000000001 });
    });
});
