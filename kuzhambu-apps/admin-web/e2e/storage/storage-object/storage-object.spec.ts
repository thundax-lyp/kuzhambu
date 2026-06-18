import { expect, test } from "@playwright/test";
import type { Page } from "@playwright/test";

const mockShellApis = async (page: Page) => {
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
                        id: "1",
                        name: "仪表盘",
                        url: "/dashboard",
                        displayParams: '{"icon":"dashboard"}'
                    },
                    {
                        id: "2",
                        name: "系统管理",
                        displayParams: '{"icon":"system"}'
                    },
                    {
                        id: "3",
                        parentId: "2",
                        name: "存储管理",
                        url: "/storage",
                        displayParams: '{"icon":"storage"}'
                    },
                    {
                        id: "4",
                        parentId: "3",
                        name: "对象管理",
                        url: "/storage/objects",
                        displayParams: '{"icon":"storage-objects"}'
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
                    perms: ["storage:object:view", "storage:object:edit"]
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
};

test.describe("storage object page", () => {
    test.beforeEach(async ({ page }) => {
        await mockShellApis(page);
        await page.addInitScript(() => {
            window.localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
            window.localStorage.setItem("kuzhambu.admin.refreshToken", "refresh-token");
            window.localStorage.setItem(
                "kuzhambu.admin.accessTokenExpireAt",
                String(Date.now() + 3600 * 1000)
            );
            window.localStorage.setItem(
                "kuzhambu.admin.permissions",
                JSON.stringify(["storage:object:view", "storage:object:edit"])
            );
        });
    });

    test("uploads storage objects and refreshes after deleting one", async ({ page }) => {
        await page.setViewportSize({ width: 1280, height: 800 });
        let records = [
            {
                id: "storage-1",
                originalFilename: "sancai.png",
                contentType: "image/png",
                ownerId: "asset-1",
                ownerType: "USER",
                size: 1536,
                accessEndpoint: "/api/storage/object/storage-1/content",
                objectStatus: "ACTIVE",
                referenceStatus: "UNREFERENCED",
                priority: 100,
                remarks: "三才图会图片"
            }
        ];
        let pageRequestCount = 0;
        let deleteRequestBody: unknown;
        let uploadFileName = "";
        await page.route("**/admin-api/api/storage/object/page", async (route) => {
            pageRequestCount += 1;
            await route.fulfill({
                contentType: "application/json",
                body: JSON.stringify({
                    code: "COMMON-00000",
                    message: "success",
                    data: {
                        pageNo: 1,
                        pageSize: 20,
                        count: records.length,
                        records
                    }
                })
            });
        });
        await page.route("**/admin-api/api/storage/object/upload", async (route) => {
            uploadFileName = route.request().postData()?.includes("upload.txt") ? "upload.txt" : "";
            const uploadedRecord = {
                id: "storage-2",
                originalFilename: "upload.txt",
                contentType: "text/plain",
                ownerId: "",
                ownerType: "",
                size: 5,
                accessEndpoint: "/api/storage/object/storage-2/content",
                objectStatus: "ACTIVE",
                referenceStatus: "UNREFERENCED",
                priority: 101,
                remarks: ""
            };
            records = [uploadedRecord, ...records];
            await route.fulfill({
                contentType: "application/json",
                body: JSON.stringify({
                    code: "COMMON-00000",
                    message: "success",
                    data: uploadedRecord
                })
            });
        });
        await page.route("**/admin-api/api/storage/object/delete", async (route) => {
            deleteRequestBody = route.request().postDataJSON();
            records = records.filter((record) => record.id !== "storage-1");
            await route.fulfill({
                contentType: "application/json",
                body: JSON.stringify({
                    code: "COMMON-00000",
                    message: "success",
                    data: true
                })
            });
        });

        await page.goto("/storage/objects");

        await expect(page.getByRole("heading", { name: "存储对象" })).toBeVisible();
        await expect(page.getByText("sancai.png")).toBeVisible();
        await expect(page.getByText("1.50 KB").first()).toBeVisible();

        const fileChooserPromise = page.waitForEvent("filechooser");
        await page.getByRole("button", { name: "上传" }).click();
        const fileChooser = await fileChooserPromise;
        await fileChooser.setFiles({
            name: "upload.txt",
            mimeType: "text/plain",
            buffer: Buffer.from("hello")
        });

        await expect(page.getByText("upload.txt")).toBeVisible();
        await expect(page.getByRole("button", { name: "读取 upload.txt" })).toBeVisible();
        expect(uploadFileName).toBe("upload.txt");

        await page.getByRole("button", { name: "删除 sancai.png" }).click();
        const confirmDialog = page.getByRole("dialog");
        await expect(page.getByText("确认删除 sancai.png？")).toBeVisible();
        await confirmDialog.getByRole("button", { name: /删\s*除/ }).click();

        await expect(confirmDialog).toBeHidden();
        await expect(page.locator("tbody").getByText("sancai.png")).toBeHidden();
        expect(deleteRequestBody).toEqual({ ids: ["storage-1"] });
        expect(pageRequestCount).toBeGreaterThanOrEqual(2);
    });
});
