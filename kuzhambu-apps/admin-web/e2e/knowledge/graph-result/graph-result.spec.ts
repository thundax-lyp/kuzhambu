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
    await page.route("**/kuzhambu-admin-api/api/sys/current-user/get", async (route) => {
        await fulfillSuccess(route, {
            id: "user-1",
            loginName: "developer",
            name: "Developer",
            ranks: 9,
            admin: true,
            superAdmin: true
        });
    });
    await page.route(
        "**/kuzhambu-admin-api/api/sys/current-user/permission/list",
        async (route) => {
            await fulfillSuccess(route, {
                perms: ADMIN_PERMISSIONS
            });
        }
    );
    await page.route("**/kuzhambu-admin-api/api/sys/current-user/menu/list", async (route) => {
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
                id: "knowledge-graph",
                parentId: "knowledge",
                name: "知识图谱",
                url: "/knowledge/graph",
                displayParams: '{"icon":"book"}'
            },
            {
                id: "graph-results",
                parentId: "knowledge-graph",
                name: "图谱结果",
                url: "/knowledge/graph-results",
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

const createGraphResultsMockHandlers = async (page: Page) => {
    const versionRequests: ApiPayload[] = [];
    let versionDetailPayload: ApiPayload | null = null;
    let entityPagePayload: ApiPayload | null = null;
    let entityDetailPayload: ApiPayload | null = null;
    let relationPagePayload: ApiPayload | null = null;
    let relationDetailPayload: ApiPayload | null = null;
    let lineageNodePagePayload: ApiPayload | null = null;
    let lineageNodeDetailPayload: ApiPayload | null = null;
    let lineageRelationPagePayload: ApiPayload | null = null;
    let lineageRelationDetailPayload: ApiPayload | null = null;

    const graphVersion = {
        versionId: 71,
        taskId: "task-71",
        candidateId: 7001,
        taskType: "GRAPH",
        sourceContentType: "SANCAI_ENTRY",
        sourceContentId: 1001,
        versionNo: 3,
        status: "APPLIED",
        appliedAt: 1790000000000,
        refinementApplied: true,
        lastRefinementTaskId: 8001,
        lastRefinementAppliedAt: 1790000100000
    };
    const graphEntity = {
        entityId: 501,
        entityKey: "person:li-shizhen",
        name: "李时珍",
        entityType: "PERSON",
        description: "明代医药学家",
        confirmationStatus: "CONFIRMED",
        latestVersionId: 71,
        sourceRefsJson: '{"entryId":1001}'
    };
    const graphRelation = {
        relationId: 601,
        relationKey: "li-shizhen-author-bencao",
        sourceName: "李时珍",
        targetName: "本草纲目",
        relationType: "AUTHOR_OF",
        evidence: "李时珍撰本草纲目",
        confirmationStatus: "CONFIRMED",
        latestVersionId: 71,
        sourceRefsJson: '{"entryId":1001}'
    };
    const lineageNode = {
        nodeId: 701,
        nodeKey: "lineage:zu",
        name: "祖父",
        nodeType: "ANCESTOR",
        generation: -2,
        gender: "MALE",
        confirmationStatus: "CONFIRMED",
        latestVersionId: 71,
        sourceRefsJson: '{"entryId":1001}'
    };
    const lineageRelation = {
        relationId: 801,
        relationKey: "lineage:zu-father",
        sourceName: "祖父",
        targetName: "父亲",
        relationType: "PARENT_OF",
        evidence: "世系原文证据",
        confirmationStatus: "CONFIRMED",
        latestVersionId: 71,
        sourceRefsJson: '{"entryId":1001}'
    };

    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/graph-extraction/version/page",
        async (route) => {
            versionRequests.push(readRequestBody(route.request().postData()));
            await fulfillSuccess(route, {
                pageNo: 1,
                pageSize: 20,
                totalCount: 1,
                count: 1,
                records: [graphVersion]
            });
        }
    );
    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/graph-extraction/version/get",
        async (route) => {
            versionDetailPayload = readRequestBody(route.request().postData());
            await fulfillSuccess(route, graphVersion);
        }
    );
    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/graph-extraction/entity/page",
        async (route) => {
            entityPagePayload = readRequestBody(route.request().postData());
            await fulfillSuccess(route, {
                pageNo: 1,
                pageSize: 20,
                totalCount: 1,
                count: 1,
                records: [graphEntity]
            });
        }
    );
    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/graph-extraction/entity/get",
        async (route) => {
            entityDetailPayload = readRequestBody(route.request().postData());
            await fulfillSuccess(route, graphEntity);
        }
    );
    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/graph-extraction/relation/page",
        async (route) => {
            relationPagePayload = readRequestBody(route.request().postData());
            await fulfillSuccess(route, {
                pageNo: 1,
                pageSize: 20,
                totalCount: 1,
                count: 1,
                records: [graphRelation]
            });
        }
    );
    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/graph-extraction/relation/get",
        async (route) => {
            relationDetailPayload = readRequestBody(route.request().postData());
            await fulfillSuccess(route, graphRelation);
        }
    );
    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/graph-extraction/lineage/node/page",
        async (route) => {
            lineageNodePagePayload = readRequestBody(route.request().postData());
            await fulfillSuccess(route, {
                pageNo: 1,
                pageSize: 20,
                totalCount: 1,
                count: 1,
                records: [lineageNode]
            });
        }
    );
    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/graph-extraction/lineage/node/get",
        async (route) => {
            lineageNodeDetailPayload = readRequestBody(route.request().postData());
            await fulfillSuccess(route, lineageNode);
        }
    );
    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/graph-extraction/lineage/relation/page",
        async (route) => {
            lineageRelationPagePayload = readRequestBody(route.request().postData());
            await fulfillSuccess(route, {
                pageNo: 1,
                pageSize: 20,
                totalCount: 1,
                count: 1,
                records: [lineageRelation]
            });
        }
    );
    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/graph-extraction/lineage/relation/get",
        async (route) => {
            lineageRelationDetailPayload = readRequestBody(route.request().postData());
            await fulfillSuccess(route, lineageRelation);
        }
    );

    return {
        getVersionRequests: () => versionRequests,
        getVersionDetailPayload: () => versionDetailPayload,
        getEntityPagePayload: () => entityPagePayload,
        getEntityDetailPayload: () => entityDetailPayload,
        getRelationPagePayload: () => relationPagePayload,
        getRelationDetailPayload: () => relationDetailPayload,
        getLineageNodePagePayload: () => lineageNodePagePayload,
        getLineageNodeDetailPayload: () => lineageNodeDetailPayload,
        getLineageRelationPagePayload: () => lineageRelationPagePayload,
        getLineageRelationDetailPayload: () => lineageRelationDetailPayload
    };
};

test.describe("admin graph results smoke", () => {
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

    test("opens version, entity, relation, and lineage result details", async ({ page }) => {
        const mocks = await createGraphResultsMockHandlers(page);

        await page.goto("/knowledge/graph-results");

        await expect(page.getByRole("heading", { name: "正式结果读取" })).toBeVisible();
        await expect(page.getByRole("table", { name: "知识图谱版本表格" })).toContainText("71");
        await expect(page.getByRole("table", { name: "知识图谱版本表格" })).toContainText("已精修");
        expect(mocks.getVersionRequests().at(-1)).toEqual({ pageNo: 1, pageSize: 20 });

        await page.getByRole("button", { name: "查看详情" }).first().click();
        await expect.poll(() => mocks.getVersionDetailPayload()).toEqual({ versionId: 71 });
        await expect(page.getByText("图谱版本详情")).toBeVisible();
        await expect(page.getByText("task-71")).toBeVisible();
        await page.getByRole("button", { name: "查看此版本正式结果" }).click();

        await expect(page.getByRole("tab", { name: "正式实体" })).toHaveAttribute(
            "aria-selected",
            "true"
        );
        await expect.poll(() => mocks.getEntityPagePayload()).toMatchObject({ versionId: "71" });
        await expect(page.getByRole("table", { name: "知识正式实体表格" })).toContainText("李时珍");
        await page
            .getByRole("table", { name: "知识正式实体表格" })
            .getByRole("button", { name: "查看详情" })
            .click();
        await expect.poll(() => mocks.getEntityDetailPayload()).toEqual({ entityId: 501 });
        await expect(page.getByText("正式实体详情")).toBeVisible();
        await expect(page.getByText("明代医药学家")).toBeVisible();
        await page.keyboard.press("Escape");

        await page.getByRole("tab", { name: "正式关系" }).click();
        await expect.poll(() => mocks.getRelationPagePayload()).toMatchObject({ versionId: "71" });
        await expect(page.getByRole("table", { name: "知识正式关系表格" })).toContainText(
            "AUTHOR_OF"
        );
        await page
            .getByRole("table", { name: "知识正式关系表格" })
            .getByRole("button", { name: "查看详情" })
            .click();
        await expect.poll(() => mocks.getRelationDetailPayload()).toEqual({ relationId: 601 });
        await expect(page.getByText("正式关系详情")).toBeVisible();
        await expect(page.getByText("李时珍撰本草纲目")).toBeVisible();
        await page.keyboard.press("Escape");

        await page.getByRole("tab", { name: "正式世系" }).click();
        await expect
            .poll(() => mocks.getLineageNodePagePayload())
            .toMatchObject({ versionId: "71" });
        await expect
            .poll(() => mocks.getLineageRelationPagePayload())
            .toMatchObject({
                versionId: "71"
            });
        await expect(page.getByRole("table", { name: "知识正式世系节点表格" })).toContainText(
            "祖父"
        );
        await page
            .getByRole("table", { name: "知识正式世系节点表格" })
            .getByRole("button", { name: "查看详情" })
            .click();
        await expect.poll(() => mocks.getLineageNodeDetailPayload()).toEqual({ nodeId: 701 });
        await expect(page.getByText("正式世系节点详情")).toBeVisible();
        await expect(page.getByText("lineage:zu")).toBeVisible();
        await page.keyboard.press("Escape");

        await expect(page.getByRole("table", { name: "知识正式世系关系表格" })).toContainText(
            "PARENT_OF"
        );
        await page
            .getByRole("table", { name: "知识正式世系关系表格" })
            .getByRole("button", { name: "查看详情" })
            .click();
        await expect
            .poll(() => mocks.getLineageRelationDetailPayload())
            .toEqual({
                relationId: 801
            });
        await expect(page.getByText("正式世系关系详情")).toBeVisible();
        await expect(page.getByText("世系原文证据")).toBeVisible();
    });
});
