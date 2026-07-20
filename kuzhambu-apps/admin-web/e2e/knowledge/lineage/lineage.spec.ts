import { expect, test } from "@playwright/test";
import type { Page, Route } from "@playwright/test";

const ADMIN_PERMISSIONS = ["knowledge:graph:view"];

type ApiPayload = Record<string, unknown>;

const readRequestBody = (postData: string | null) => {
    return postData ? (JSON.parse(postData) as ApiPayload) : {};
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
                id: "lineage",
                parentId: "knowledge",
                name: "世系图",
                url: "/knowledge/lineage",
                displayParams: '{"icon":"submissions"}'
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

const createLineageMockHandlers = async (page: Page) => {
    const canvasRequests: ApiPayload[] = [];
    const version = {
        versionId: 71,
        versionNo: 2,
        taskType: "LINEAGE",
        status: "APPLIED",
        sourceContentType: "SANCAI_ENTRY",
        sourceContentId: 1001,
        sourceCategoryCode: "person",
        sourceCategoryName: "三才图会"
    };
    const nodes = [
        {
            id: "node-1",
            nodeId: 501,
            nodeKey: "lineage:zhang-san",
            name: "张三",
            nodeType: "PERSON",
            generation: 0,
            gender: "MALE",
            confirmationStatus: "CONFIRMED",
            confidence: 0.96,
            sourceRefsJson: '{"entryId":1001}',
            sourceRefs: [
                {
                    sourceContentType: "SANCAI_ENTRY",
                    sourceContentId: 1001,
                    sourceTitle: "三才图会人物条",
                    snippet: "张三世系节点证据"
                }
            ],
            x: 100,
            y: 120
        },
        {
            id: "node-2",
            nodeId: 502,
            nodeKey: "lineage:zhang-fu",
            name: "张父",
            nodeType: "ANCESTOR",
            generation: -1,
            gender: "MALE",
            confirmationStatus: "PENDING",
            confidence: 0.88,
            sourceRefsJson: '{"entryId":1002}',
            sourceRefs: [
                {
                    sourceContentType: "SANCAI_ENTRY",
                    sourceContentId: 1002,
                    sourceTitle: "三才图会父系条",
                    snippet: "张父世系节点证据"
                }
            ],
            x: 360,
            y: 120
        }
    ];
    const relations = [
        {
            id: "relation-1",
            relationId: 601,
            sourceNodeId: 502,
            sourceNodeName: "张父",
            targetNodeId: 501,
            targetNodeName: "张三",
            relationType: "PARENT_OF",
            relationLabel: "父子",
            confirmationStatus: "CONFIRMED",
            confidence: 0.91,
            sourceRefsJson: '{"entryId":1003}',
            sourceRefs: [
                {
                    sourceContentType: "SANCAI_ENTRY",
                    sourceContentId: 1003,
                    sourceTitle: "三才图会世系条",
                    snippet: "张父为张三之父"
                }
            ]
        }
    ];

    await page.route("**/kuzhambu-admin-api/api/knowledge/lineage/canvas", async (route) => {
        const payload = readRequestBody(route.request().postData());
        canvasRequests.push(payload);
        await fulfillSuccess(route, {
            version,
            summary: {
                nodeCount: nodes.length,
                relationCount: relations.length,
                confirmedNodeCount: 1,
                confirmedRelationCount: 1,
                focusNodeId: payload.focusNodeId ?? null,
                focusRelationId: payload.focusRelationId ?? null
            },
            nodes,
            relations,
            selectedNode:
                typeof payload.focusNodeId === "number"
                    ? nodes.find((node) => node.nodeId === payload.focusNodeId) || null
                    : null,
            selectedRelation:
                typeof payload.focusRelationId === "number"
                    ? relations.find(
                          (relation) => relation.relationId === payload.focusRelationId
                      ) || null
                    : null,
            availableFilters: {
                versions: [version],
                nodeTypes: ["PERSON", "ANCESTOR"],
                relationTypes: ["PARENT_OF"],
                confirmationStatuses: ["CONFIRMED", "PENDING"]
            },
            empty: null
        });
    });

    return {
        getCanvasRequests: () => canvasRequests
    };
};

const selectOption = async (page: Page, comboboxName: string, optionTitle: string) => {
    await page.getByRole("combobox", { name: comboboxName }).click();
    await page.getByTitle(optionTitle).last().click();
};

test.describe("admin lineage smoke", () => {
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

    test("filters, refreshes, resets, and selects lineage canvas records", async ({ page }) => {
        const mocks = await createLineageMockHandlers(page);

        await page.goto("/knowledge/lineage");

        await expect(page.getByRole("heading", { name: "世系图浏览" })).toBeVisible();
        await expect(page.getByRole("img", { name: "世系图画布" })).toBeVisible();
        await expect(page.getByRole("table", { name: "世系节点列表" })).toContainText("张三");
        expect(mocks.getCanvasRequests().at(-1)).toMatchObject({
            versionId: null,
            depth: 2
        });

        await selectOption(page, "图谱版本", "版本 2 / 三才图会");
        await expect
            .poll(() => mocks.getCanvasRequests().at(-1))
            .toMatchObject({
                versionId: 71,
                focusNodeId: null,
                focusRelationId: null
            });

        await page.getByRole("searchbox", { name: "搜索世系节点或关系" }).fill("张三");
        await page.keyboard.press("Enter");
        await expect
            .poll(() => mocks.getCanvasRequests().at(-1))
            .toMatchObject({
                keyword: "张三",
                versionId: 71
            });

        await selectOption(page, "节点类型", "PERSON");
        await expect
            .poll(() => mocks.getCanvasRequests().at(-1))
            .toMatchObject({
                nodeType: "PERSON",
                focusNodeId: null,
                focusRelationId: null
            });

        await selectOption(page, "关系类型", "PARENT_OF");
        await expect
            .poll(() => mocks.getCanvasRequests().at(-1))
            .toMatchObject({
                relationType: "PARENT_OF"
            });

        await selectOption(page, "确认状态", "CONFIRMED");
        await expect
            .poll(() => mocks.getCanvasRequests().at(-1))
            .toMatchObject({
                confirmationStatus: "CONFIRMED"
            });

        await selectOption(page, "深度", "3 层");
        await expect
            .poll(() => mocks.getCanvasRequests().at(-1))
            .toMatchObject({
                depth: 3
            });

        const requestsBeforeRefresh = mocks.getCanvasRequests().length;
        await page.getByRole("button", { name: "刷新" }).click();
        await expect
            .poll(() => mocks.getCanvasRequests().length)
            .toBeGreaterThan(requestsBeforeRefresh);

        await page.locator("svg[aria-label='世系图画布'] text", { hasText: "张三" }).click();
        await expect
            .poll(() => mocks.getCanvasRequests().at(-1))
            .toMatchObject({
                focusNodeId: 501,
                focusRelationId: null
            });
        await expect(page.getByText("节点详情")).toBeVisible();
        await expect(page.getByText("lineage:zhang-san")).toBeVisible();
        await expect(page.getByText("张三世系节点证据")).toBeVisible();

        await page
            .locator("svg[aria-label='世系图画布'] .knowledge-lineage-canvas__relation text")
            .first()
            .evaluate((element) => {
                element.dispatchEvent(
                    new MouseEvent("click", { bubbles: true, cancelable: true, view: window })
                );
            });
        await expect
            .poll(() => mocks.getCanvasRequests().at(-1))
            .toMatchObject({
                focusNodeId: null,
                focusRelationId: 601
            });
        await expect(page.getByText("关系详情")).toBeVisible();
        await expect(page.getByText("张父为张三之父")).toBeVisible();

        await page.getByRole("tab", { name: "节点列表" }).click();
        await page.getByRole("table", { name: "世系节点列表" }).getByText("张父").click();
        await expect
            .poll(() => mocks.getCanvasRequests().at(-1))
            .toMatchObject({
                focusNodeId: 502,
                focusRelationId: null
            });
        await expect(page.getByText("lineage:zhang-fu")).toBeVisible();

        await page.getByRole("tab", { name: "关系列表" }).click();
        await page.getByRole("table", { name: "世系关系列表" }).getByText("父子").click();
        await expect(page.getByText("关系详情")).toBeVisible();
        await expect(page.getByText("张父为张三之父")).toBeVisible();

        await page
            .locator("button")
            .filter({ hasText: /重\s*置/ })
            .click({ force: true });
        await expect(page.getByRole("searchbox", { name: "搜索世系节点或关系" })).toHaveValue("");

        await page.getByRole("button", { name: "缩小世系画布" }).click();
        await page.getByRole("button", { name: "放大世系画布" }).click();
        await page.getByRole("button", { name: "适配世系画布" }).click();
    });
});
