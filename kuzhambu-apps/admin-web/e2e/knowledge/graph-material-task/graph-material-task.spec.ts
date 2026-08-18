import { expect, test } from "@playwright/test";
import type { Page, Route } from "@playwright/test";

type ApiPayload = Record<string, unknown>;

const ADMIN_PERMISSIONS = ["knowledge:graph:view", "knowledge:graph:edit", "knowledge:graph:apply"];

const SANCAI_REF = {
    contentRefId: "1001",
    contentType: "SANCAI_ENTRY"
};
const PUBLISHED_REF = {
    contentRefId: "1002",
    contentType: "SANCAI_ENTRY"
};
const FAILED_REF = {
    contentRefId: "1003",
    contentType: "WANGQI_DOCUMENT"
};

const readRequestBody = (postData: string | null) => {
    return postData ? (JSON.parse(postData) as ApiPayload) : {};
};

const fulfillSuccess = async (route: Route, data: unknown) => {
    await route.fulfill({
        contentType: "application/json",
        body: JSON.stringify({
            code: "COMMON-00000",
            data,
            message: "success"
        })
    });
};

const mockShellApis = async (page: Page, permissions: string[]) => {
    await page.route("**/kuzhambu-admin-api/api/sys/current-user/get", async (route) => {
        await fulfillSuccess(route, {
            admin: true,
            id: "user-1",
            loginName: "developer",
            name: "Developer",
            ranks: 9,
            superAdmin: true
        });
    });
    await page.route(
        "**/kuzhambu-admin-api/api/sys/current-user/permission/list",
        async (route) => {
            await fulfillSuccess(route, {
                perms: permissions
            });
        }
    );
    await page.route("**/kuzhambu-admin-api/api/sys/current-user/menu/list", async (route) => {
        await fulfillSuccess(route, [
            {
                displayParams: '{"icon":"knowledge"}',
                id: "knowledge",
                name: "知识治理"
            },
            {
                displayParams: '{"icon":"submissions"}',
                id: "graph-materials",
                name: "图谱素材库",
                parentId: "knowledge",
                url: "/knowledge/graph-material"
            },
            {
                displayParams: '{"icon":"submissions"}',
                id: "graph-extraction",
                name: "知识抽取",
                parentId: "knowledge",
                url: "/knowledge/graph-extraction"
            }
        ]);
    });
    await page.route("**/kuzhambu-admin-api/api/auth/session/token/refresh", async (route) => {
        await fulfillSuccess(route, {
            expireAt: Date.now() + 3600 * 1000,
            refreshToken: "refresh-token",
            token: "test-token"
        });
    });
};

const installSession = async (page: Page, permissions: string[]) => {
    await page.addInitScript(() => {
        window.localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
        window.localStorage.setItem("kuzhambu.admin.refreshToken", "refresh-token");
        window.localStorage.setItem(
            "kuzhambu.admin.accessTokenExpireAt",
            String(Date.now() + 3600 * 1000)
        );
    });
    await page.addInitScript((storedPermissions) => {
        window.localStorage.setItem(
            "kuzhambu.admin.permissions",
            JSON.stringify(storedPermissions)
        );
    }, permissions);
};

const createApiPathCollector = (page: Page) => {
    const paths: string[] = [];
    page.on("request", (request) => {
        const url = new URL(request.url());
        const marker = "/kuzhambu-admin-api/api";
        if (!url.pathname.startsWith(marker)) {
            return;
        }
        const apiPath = url.pathname.slice(marker.length);
        if (apiPath.startsWith("/sys/") || apiPath.startsWith("/auth/")) {
            return;
        }
        paths.push(apiPath);
    });
    return {
        assertOnlyKnowledgeGraphApi: () => {
            expect(paths, "业务请求应只进入 Knowledge graph 新接口").not.toEqual([]);
            expect(paths.filter((path) => !path.startsWith("/knowledge/graph/"))).toEqual([]);
        },
        paths
    };
};

const materialListRecords = [
    {
        latestTask: {
            attemptNo: "1",
            currentStage: "CANDIDATE_READY",
            disposition: "PENDING",
            executionStatus: "SUCCEEDED",
            id: "7001",
            lockVersion: "5",
            materialRef: SANCAI_REF,
            progress: 100
        },
        material: null,
        materialStats: null,
        source: {
            category: "天文",
            contentRef: SANCAI_REF,
            contentType: "SANCAI_ENTRY",
            summary: "三才图会天文类条目",
            title: "三才图会 天文一",
            volume: "卷一"
        }
    },
    {
        latestTask: null,
        material: {
            category: "人物",
            contentRef: PUBLISHED_REF,
            contentType: "SANCAI_ENTRY",
            id: "2002",
            lockVersion: "4",
            publishedAt: "1723852820000",
            status: "PUBLISHED",
            title: "三才图会 人物一",
            volume: "卷二"
        },
        materialStats: {
            activeTaskCount: "0",
            calculatedAt: "1723852820000",
            draftEdgeCount: "98",
            draftNodeCount: "64",
            failedTaskCount: "0",
            pendingReviewTaskCount: "0",
            publicationContributionCount: "162",
            publishedEdgeCount: "98",
            publishedNodeCount: "64",
            statsRevision: "4"
        },
        source: {
            category: "人物",
            contentRef: PUBLISHED_REF,
            contentType: "SANCAI_ENTRY",
            summary: "已发布的人物类素材",
            title: "三才图会 人物一",
            volume: "卷二"
        }
    },
    {
        latestTask: {
            attemptNo: "1",
            currentStage: "VALIDATE",
            disposition: null,
            executionStatus: "FAILED",
            failureReason: "候选实体名称缺少身份限定。",
            id: "7003",
            lockVersion: "3",
            materialRef: FAILED_REF,
            progress: 74
        },
        material: {
            category: "方志",
            contentRef: FAILED_REF,
            contentType: "WANGQI_DOCUMENT",
            failedOperation: "PUBLISH",
            failureReason: "发布预览存在未解决冲突。",
            id: "2003",
            lockVersion: "6",
            status: "FAILED",
            title: "王祺札记 山川",
            volume: "册一"
        },
        materialStats: {
            activeTaskCount: "0",
            calculatedAt: "1723852810000",
            draftEdgeCount: "18",
            draftNodeCount: "12",
            failedTaskCount: "1",
            pendingReviewTaskCount: "0",
            publicationContributionCount: "0",
            publishedEdgeCount: "0",
            publishedNodeCount: "0",
            statsRevision: "5"
        },
        source: {
            category: "方志",
            contentRef: FAILED_REF,
            contentType: "WANGQI_DOCUMENT",
            summary: "抽取失败的王祺文档",
            title: "王祺札记 山川",
            volume: "册一"
        }
    }
];

const materialDetail = {
    edges: [
        {
            id: "edge-1002",
            qualifiers: { evidence: "正文段落" },
            relationType: "MENTIONS",
            source: "AI",
            sourceNodeId: "node-1002-1",
            targetNodeId: "node-1002-2"
        }
    ],
    material: materialListRecords[1].material,
    materialStats: materialListRecords[1].materialStats,
    nodes: [
        {
            id: "node-1002-1",
            name: "三才图会 人物一",
            nodeType: "WORK",
            properties: {},
            source: "AI"
        },
        {
            id: "node-1002-2",
            name: "人物",
            nodeType: "CATEGORY",
            properties: {},
            source: "AI"
        }
    ],
    source: materialListRecords[1].source,
    taskSummary: {
        activeTaskCount: "0",
        failedTaskCount: "0",
        latestTask: null,
        pendingReviewTaskCount: "0"
    }
};

const extractionTasks = [
    {
        attemptNo: "1",
        candidateId: "8001",
        completedAt: "1723852810000",
        currentStage: "CANDIDATE_READY",
        disposition: "PENDING",
        executionStatus: "SUCCEEDED",
        id: "7001",
        lockVersion: "5",
        materialRef: SANCAI_REF,
        progress: 100,
        requestedAt: "1723852800000",
        selectionScopeJson: '{"contentRefIds":["1001"]}',
        taskId: "7001",
        taskType: "GRAPH",
        triggerSource: "MANUAL"
    },
    {
        attemptNo: "1",
        completedAt: "1723852840000",
        currentStage: "VALIDATE",
        disposition: null,
        executionStatus: "FAILED",
        failureReason: "候选实体名称缺少身份限定。",
        id: "7003",
        lockVersion: "3",
        materialRef: FAILED_REF,
        progress: 74,
        requestedAt: "1723852830000",
        taskId: "7003",
        taskType: "GRAPH",
        triggerSource: "MANUAL"
    }
];

const createMaterialHandlers = async (page: Page) => {
    let batchCreatePayload: ApiPayload | null = null;
    let materialGetPayload: ApiPayload | null = null;

    await page.route("**/kuzhambu-admin-api/api/knowledge/graph/material/page", async (route) => {
        await fulfillSuccess(route, {
            pageNo: "1",
            pageSize: "20",
            records: materialListRecords,
            totalCount: String(materialListRecords.length),
            totalPage: "1"
        });
    });
    await page.route("**/kuzhambu-admin-api/api/knowledge/graph/material/get", async (route) => {
        materialGetPayload = readRequestBody(route.request().postData());
        await fulfillSuccess(route, materialDetail);
    });
    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/graph/task/batch/create",
        async (route) => {
            batchCreatePayload = readRequestBody(route.request().postData());
            await fulfillSuccess(route, {
                batchId: "batch-001",
                materials: [
                    {
                        contentRef: PUBLISHED_REF,
                        result: {
                            ...extractionTasks[0],
                            id: "7010",
                            materialRef: PUBLISHED_REF,
                            taskId: "7010"
                        },
                        success: true
                    },
                    {
                        contentRef: FAILED_REF,
                        failureCode: "GRAPH_TASK_ACTIVE_EXISTS",
                        failureMessage: "素材已有活动任务。",
                        success: false
                    }
                ]
            });
        }
    );

    return {
        getBatchCreatePayload: () => batchCreatePayload,
        getMaterialGetPayload: () => materialGetPayload
    };
};

const selectMaterialRow = async (page: Page, rowIndex: number) => {
    await page.getByRole("checkbox").nth(rowIndex).click();
};

test.describe("admin knowledge graph material and task flow", () => {
    test.beforeEach(async ({ page }) => {
        await page.setViewportSize({ width: 1280, height: 900 });
    });

    test("opens material drawer and handles single extraction plus partial batch results", async ({
        page
    }) => {
        await installSession(page, ADMIN_PERMISSIONS);
        await mockShellApis(page, ADMIN_PERMISSIONS);
        const apiCollector = createApiPathCollector(page);
        const mocks = await createMaterialHandlers(page);

        await page.goto("/knowledge/graph-material");

        await expect(page.getByRole("heading", { name: "图谱素材库" })).toBeVisible();
        await expect(page.getByRole("table", { name: "图谱素材复合表格" })).toBeVisible();

        await page.getByTestId("knowledge-graph-material-open-2002-link").evaluate((element) => {
            (element as HTMLButtonElement).click();
        });
        await expect(page.getByTestId("knowledge-graph-material-detail-drawer")).toBeVisible();
        await expect
            .poll(() => mocks.getMaterialGetPayload())
            .toEqual({
                contentRef: PUBLISHED_REF
            });

        const materialDrawer = page.getByTestId("knowledge-graph-material-detail-drawer");
        await materialDrawer.getByText("任务", { exact: true }).click();
        await expect(
            page.getByTestId("knowledge-graph-material-detail-tasks-section")
        ).toBeVisible();
        await materialDrawer.getByText("发布变更", { exact: true }).click();
        await expect(
            page.getByTestId("knowledge-graph-material-detail-publication-changes-section")
        ).toBeVisible();
        await page.getByTestId("knowledge-graph-material-detail-close-button").click();

        await selectMaterialRow(page, 2);
        await page.getByTestId("knowledge-graph-material-batch-extract-button").click();
        await expect
            .poll(() => mocks.getBatchCreatePayload())
            .toMatchObject({
                selection: {
                    contentRefs: [PUBLISHED_REF]
                }
            });
        await expect(
            page.getByTestId("knowledge-graph-material-batch-result-SANCAI_ENTRY:1002")
        ).toContainText("任务已创建 #7010");

        await selectMaterialRow(page, 3);
        await page.getByTestId("knowledge-graph-material-batch-extract-button").click();
        await expect
            .poll(() => mocks.getBatchCreatePayload())
            .toMatchObject({
                selection: {
                    contentRefs: [PUBLISHED_REF, FAILED_REF]
                }
            });
        await expect(page.getByText("部分素材处理失败，其余逐素材结果已保留。")).toBeVisible();
        await expect(
            page.getByTestId("knowledge-graph-material-batch-result-WANGQI_DOCUMENT:1003")
        ).toContainText("素材已有活动任务。");

        await page.getByTestId("knowledge-graph-material-batch-publish-button").click();
        await expect(
            page.getByTestId("knowledge-graph-material-batch-result-SANCAI_ENTRY:1002")
        ).toContainText("已发布");
        await expect(
            page.getByTestId("knowledge-graph-material-batch-result-WANGQI_DOCUMENT:1003")
        ).toContainText("发布预览存在未解决冲突。");

        apiCollector.assertOnlyKnowledgeGraphApi();
    });
});
