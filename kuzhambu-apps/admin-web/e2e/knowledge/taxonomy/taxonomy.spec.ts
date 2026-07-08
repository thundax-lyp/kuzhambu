import { expect, test } from "@playwright/test";
import type { Page, Route } from "@playwright/test";

const ADMIN_PERMISSIONS = ["knowledge:taxonomy:view", "knowledge:taxonomy:edit"];

const readRequestBody = (postData: string | null) => {
    return postData ? (JSON.parse(postData) as Record<string, unknown>) : {};
};

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
    await page.route("**/kuzhambu-admin-api/api/sys/current-user/perms", async (route) => {
        await fulfillSuccess(route, {
            perms: ADMIN_PERMISSIONS
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
                id: "knowledge",
                name: "知识治理",
                displayParams: '{"icon":"knowledge"}'
            },
            {
                id: "taxonomy",
                parentId: "knowledge",
                name: "标签与同义词",
                url: "/knowledge/taxonomy",
                displayParams: '{"icon":"taxonomy"}'
            }
        ]);
    });
    await page.route("**/kuzhambu-admin-api/api/auth/session/token/refresh", async (route) => {
        await fulfillSuccess(route, {
            token: "test-token",
            refreshToken: "refresh-token",
            expireAt: Date.now() + 3600 * 1000
        });
    });
};

type ApiPayload = {
    id?: string;
    term?: string | null;
    pageNo?: number;
    pageSize?: number;
    synonym?: string;
    status?: string;
};

type SynonymApiSnapshot = {
    term: string;
    synonym: string;
    status: "ENABLED" | "DISABLED";
    statusLabel?: string;
    id: string;
};

const createSynonymMockHandlers = async (page: Page) => {
    const synonyms: Array<SynonymApiSnapshot> = [];

    let openCreatePayload: ApiPayload | null = null;
    let openUpdatePayload: ApiPayload | null = null;
    let changeStatusPayload: ApiPayload | null = null;
    let removePayload: ApiPayload | null = null;
    const pageRequestBodies: ApiPayload[] = [];

    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/taxonomy/category/page",
        async (route) => {
            await fulfillSuccess(route, {
                pageNo: 1,
                pageSize: 20,
                totalCount: 0,
                count: 0,
                records: []
            });
        }
    );
    await page.route("**/kuzhambu-admin-api/api/knowledge/taxonomy/tag/page", async (route) => {
        await fulfillSuccess(route, {
            pageNo: 1,
            pageSize: 20,
            totalCount: 0,
            count: 0,
            records: []
        });
    });
    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/taxonomy/tag/review/page",
        async (route) => {
            await fulfillSuccess(route, {
                pageNo: 1,
                pageSize: 20,
                totalCount: 0,
                count: 0,
                records: []
            });
        }
    );
    await page.route("**/kuzhambu-admin-api/api/knowledge/taxonomy/synonym/page", async (route) => {
        const body = readRequestBody(route.request().postData());
        pageRequestBodies.push(body as ApiPayload);
        await fulfillSuccess(route, {
            pageNo: body.pageNo ?? 1,
            pageSize: body.pageSize ?? 20,
            count: synonyms.length,
            totalCount: synonyms.length,
            records: synonyms
        });
    });

    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/taxonomy/synonym/create",
        async (route) => {
            openCreatePayload = readRequestBody(route.request().postData()) as ApiPayload;
            const body = readRequestBody(route.request().postData()) as {
                id?: string;
                term?: string;
                synonym?: string;
                status?: string | null;
            };
            const status = (body.status ?? "ENABLED").toString() as "ENABLED" | "DISABLED";
            synonyms.push({
                id: body.id || `syn-${Date.now()}`,
                term: body.term || "",
                synonym: body.synonym || "",
                status
            });
            await fulfillSuccess(route, true);
        }
    );

    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/taxonomy/synonym/update",
        async (route) => {
            openUpdatePayload = readRequestBody(route.request().postData()) as ApiPayload;
            const body = readRequestBody(route.request().postData()) as {
                id?: string;
                term?: string;
                synonym?: string;
            };
            const target = synonyms.find((item) => item.id === body.id);
            if (target) {
                target.term = body.term || target.term;
                target.synonym = body.synonym || target.synonym;
            }
            await fulfillSuccess(route, true);
        }
    );

    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/taxonomy/synonym/status",
        async (route) => {
            changeStatusPayload = readRequestBody(route.request().postData()) as ApiPayload;
            const body = readRequestBody(route.request().postData()) as {
                id?: string;
                status?: string | null;
            };
            const target = synonyms.find((item) => item.id === body.id);
            if (target) {
                target.status = body.status === "ENABLED" ? "ENABLED" : "DISABLED";
            }
            await fulfillSuccess(route, true);
        }
    );

    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/taxonomy/synonym/delete",
        async (route) => {
            removePayload = readRequestBody(route.request().postData()) as ApiPayload;
            const body = readRequestBody(route.request().postData()) as { id?: string };
            const index = synonyms.findIndex((item) => item.id === body.id);
            if (index >= 0) {
                synonyms.splice(index, 1);
            }
            await fulfillSuccess(route, true);
        }
    );

    return {
        getSynonymPageRequestBodies: () => pageRequestBodies,
        getCreatePayload: () => openCreatePayload,
        getUpdatePayload: () => openUpdatePayload,
        getChangeStatusPayload: () => changeStatusPayload,
        getRemovePayload: () => removePayload
    };
};

test.describe("admin taxonomy synonyms smoke", () => {
    test.beforeEach(async ({ page }) => {
        await mockShellApis(page);
        await page.addInitScript((permissions) => {
            window.localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
            window.localStorage.setItem("kuzhambu.admin.refreshToken", "refresh-token");
            window.localStorage.setItem(
                "kuzhambu.admin.accessTokenExpireAt",
                String(Date.now() + 3600 * 1000)
            );
            window.localStorage.setItem("kuzhambu.admin.permissions", JSON.stringify(permissions));
        }, ADMIN_PERMISSIONS);
        await page.setViewportSize({ width: 1280, height: 900 });
    });

    test("searches, adds, edits, toggles, and removes a synonym", async ({ page }) => {
        const mocks = await createSynonymMockHandlers(page);

        await page.goto("/knowledge/taxonomy");
        await page.getByRole("tab", { name: /同义词/ }).click();

        await expect(page.getByText("同义词管理")).toBeVisible();
        await page.getByRole("textbox", { name: "搜索同义词" }).fill("礼制");

        await expect
            .poll(() => mocks.getSynonymPageRequestBodies().some((body) => body.term === "礼制"))
            .toBe(true);
        expect(mocks.getSynonymPageRequestBodies().find((body) => body.term === "礼制")).toEqual(
            expect.objectContaining({
                term: "礼制",
                pageNo: 1,
                pageSize: 20,
                synonym: undefined
            })
        );

        await page.getByRole("button", { name: "新增同义词" }).click();
        await expect(page.getByRole("heading", { name: "新增同义词" })).toBeVisible();
        await page.getByLabel("术语").fill("礼制");
        await page.getByLabel("同义词").fill("礼学");
        await page.getByRole("button", { name: "新增", exact: true }).click();

        expect(mocks.getCreatePayload()).toMatchObject({
            id: expect.any(String),
            term: "礼制",
            synonym: "礼学"
        });

        await expect(page.getByText("同义词已保存")).toBeVisible();

        const createdRow = page
            .locator("tr", { hasText: "礼学" })
            .filter({ hasText: "礼制" })
            .first();
        await expect(createdRow).toBeVisible();

        await createdRow.getByRole("button", { name: "编辑" }).click();
        await expect(page.getByRole("heading", { name: "编辑同义词" })).toBeVisible();
        await page.getByLabel("同义词").fill("典礼");
        await page.getByRole("button", { name: "保存" }).click();

        expect(mocks.getUpdatePayload()).toMatchObject({
            id: mocks.getCreatePayload()?.id,
            term: "礼制",
            synonym: "典礼"
        });

        await expect(page.getByText("同义词已保存")).toBeVisible();
        const statusSwitch = page.getByRole("switch", { name: /切换 .* 状态/ });
        await statusSwitch.click();

        expect(mocks.getChangeStatusPayload()).toMatchObject({
            id: mocks.getCreatePayload()?.id,
            status: "DISABLED"
        });

        await createdRow.getByRole("button", { name: "删除" }).click();
        await page.getByRole("button", { name: "确认" }).click();

        expect(mocks.getRemovePayload()).toEqual({ id: mocks.getCreatePayload()?.id });
    });
});
