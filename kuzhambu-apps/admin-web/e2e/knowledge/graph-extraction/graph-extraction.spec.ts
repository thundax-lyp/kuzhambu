import { expect, test } from "@playwright/test";
import type { Page, Route } from "@playwright/test";

const ADMIN_PERMISSIONS = ["knowledge:graph:view", "knowledge:graph:edit", "knowledge:graph:apply"];

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
                id: "graph-extraction",
                parentId: "knowledge",
                name: "知识抽取",
                url: "/knowledge/graph-extraction",
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

const manuscriptTreeResponse = (requestBody: ApiPayload) => {
    if (requestBody.parentKey === "SOURCE_ROOT:SANCAI_ENTRY") {
        return [
            {
                nodeKey: "MANUSCRIPT:SANCAI_ENTRY:1001",
                parentKey: "SOURCE_ROOT:SANCAI_ENTRY",
                nodeType: "MANUSCRIPT",
                title: "三才稿件",
                sourceContentType: "SANCAI_ENTRY",
                sourceContentId: 1001,
                graphStatus: "CANDIDATE_READY"
            }
        ];
    }

    if (requestBody.parentKey) {
        return [];
    }

    return [
        {
            nodeKey: "SOURCE_ROOT:SANCAI_ENTRY",
            nodeType: "SOURCE_ROOT",
            title: "三才"
        }
    ];
};

const createGraphExtractionMockHandlers = async (page: Page) => {
    let createPayload: ApiPayload | null = null;
    let detailPayload: ApiPayload | null = null;
    let applyPayload: ApiPayload | null = null;
    let cancelPayload: ApiPayload | null = null;
    let regeneratePayload: ApiPayload | null = null;
    let extractPayload: ApiPayload | null = null;
    let workbenchApplyPayload: ApiPayload | null = null;
    const treePayloads: ApiPayload[] = [];

    const task = {
        taskId: "8008",
        batchJobId: 1001,
        triggerSource: "QUALITY_REPORT",
        taskType: "GRAPH",
        scopeType: "CLASSICS_ENTRY",
        scopeJson: '{"entryId":1001}',
        selectionScopeJson: '{"sourceContentIds":[1001,1002]}',
        replaceUnconfirmedOnly: true,
        sourceContentType: "SANCAI_ENTRY",
        sourceContentId: 1001,
        aiCallId: 6001,
        aiCandidateId: 7001,
        status: "SUCCEEDED",
        requestedBy: 99
    };

    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/graph-extraction/task/page",
        async (route) => {
            await fulfillSuccess(route, {
                pageNo: 1,
                pageSize: 20,
                totalCount: 1,
                count: 1,
                records: [task]
            });
        }
    );
    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/graph-extraction/task/get",
        async (route) => {
            detailPayload = readRequestBody(route.request().postData());
            await fulfillSuccess(route, task);
        }
    );
    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/graph-extraction/task/add",
        async (route) => {
            createPayload = readRequestBody(route.request().postData());
            await fulfillSuccess(route, {
                ...task,
                taskId: "9001",
                taskType: createPayload.taskType,
                triggerSource: createPayload.triggerSource,
                status: "PENDING"
            });
        }
    );
    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/graph-extraction/task/apply",
        async (route) => {
            applyPayload = readRequestBody(route.request().postData());
            await fulfillSuccess(route, {
                ...task,
                status: "APPLIED"
            });
        }
    );
    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/graph-extraction/task/cancel-batch",
        async (route) => {
            cancelPayload = readRequestBody(route.request().postData());
            await fulfillSuccess(route, {
                batchJobId: 1001,
                status: "CANCELLED",
                cancelledCount: 1,
                completedCount: 0,
                failedCount: 0
            });
        }
    );
    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/graph-extraction/task/regenerate",
        async (route) => {
            regeneratePayload = readRequestBody(route.request().postData());
            await fulfillSuccess(route, {
                ...task,
                taskId: "9002",
                triggerSource: regeneratePayload.triggerSource,
                status: "PENDING"
            });
        }
    );
    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/graph-workbench/manuscript-tree",
        async (route) => {
            const requestBody = readRequestBody(route.request().postData());
            treePayloads.push(requestBody);
            await fulfillSuccess(route, manuscriptTreeResponse(requestBody));
        }
    );
    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/graph-workbench/manuscript/get",
        async (route) => {
            await fulfillSuccess(route, {
                sourceContentType: "SANCAI_ENTRY",
                sourceContentId: 1001,
                title: "三才稿件",
                summary: "三才稿件摘要",
                sourcePath: "人物 / 三才稿件",
                currentVersionNo: 2,
                graphStatus: "CANDIDATE_READY",
                latestExtractionTask: {
                    ...task,
                    taskId: "9001"
                },
                latestGraphVersion: {
                    versionId: 8001,
                    taskId: "9001",
                    graphStatus: "APPLIED"
                }
            });
        }
    );
    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/graph-workbench/candidate/get",
        async (route) => {
            await fulfillSuccess(route, {
                taskId: 9001,
                aiCandidateId: 7001,
                taskType: "GRAPH",
                status: "SUCCEEDED",
                sourceContentType: "SANCAI_ENTRY",
                sourceContentId: 1001,
                candidatePayloadJson: '{"entities":[{"name":"黄帝"}]}'
            });
        }
    );
    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/graph-workbench/manuscript/extract",
        async (route) => {
            extractPayload = readRequestBody(route.request().postData());
            await fulfillSuccess(route, {
                ...task,
                taskId: "9001",
                status: "PENDING"
            });
        }
    );
    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/graph-workbench/candidate/apply",
        async (route) => {
            workbenchApplyPayload = readRequestBody(route.request().postData());
            await fulfillSuccess(route, {
                taskId: 9001,
                graphVersionId: 8001,
                graphStatus: "APPLIED"
            });
        }
    );

    return {
        getCreatePayload: () => createPayload,
        getDetailPayload: () => detailPayload,
        getApplyPayload: () => applyPayload,
        getCancelPayload: () => cancelPayload,
        getRegeneratePayload: () => regeneratePayload,
        getExtractPayload: () => extractPayload,
        getTreePayloads: () => treePayloads,
        getWorkbenchApplyPayload: () => workbenchApplyPayload
    };
};

test.describe("admin graph extraction smoke", () => {
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

    test("creates, opens, applies, cancels, and regenerates graph extraction tasks", async ({
        page
    }) => {
        const mocks = await createGraphExtractionMockHandlers(page);

        await page.goto("/knowledge/graph-extraction");

        await expect(page.getByRole("heading", { name: "知识抽取" })).toBeVisible();
        await expect(page.getByRole("textbox", { name: "搜索稿件" })).toHaveCount(0);
        await expect(page.getByText("三才稿件")).toBeVisible();
        await expect(page.getByText("王圻稿件")).toHaveCount(0);
        await expect(page.getByText("明俗稿件")).toHaveCount(0);
        await expect(
            page.locator(".knowledge-graph-extraction-work-area .ant-splitter-bar")
        ).toHaveCount(1);
        await expect(page.getByRole("button", { name: "Toggle start panel" })).toBeVisible();
        await expect.poll(() => mocks.getTreePayloads()[0]).toEqual({});
        await expect
            .poll(() => mocks.getTreePayloads().map((payload) => payload.parentKey))
            .toEqual([undefined, "SOURCE_ROOT:SANCAI_ENTRY"]);

        await page.getByText("三才稿件").click();
        await expect(page.getByText("三才稿件摘要")).toBeVisible();
        await page.getByRole("button", { name: "抽取图谱" }).click();
        await expect
            .poll(() => mocks.getExtractPayload())
            .toEqual({
                sourceContentType: "SANCAI_ENTRY",
                sourceContentId: 1001,
                taskType: "GRAPH"
            });
        await page.getByRole("button", { name: "应用候选" }).click();
        await expect.poll(() => mocks.getWorkbenchApplyPayload()).toEqual({ taskId: "9001" });

        await page.getByRole("button", { name: "任务列表(1)" }).click();
        await expect(page.getByText("8008")).toBeVisible();
        await expect(page.getByRole("cell", { name: "QUALITY_REPORT" })).toBeVisible();

        await page
            .getByTestId("knowledge-graph-extraction-graph-extraction-task-view-button")
            .evaluate((element) => {
                (element as HTMLButtonElement).click();
            });
        await expect.poll(() => mocks.getDetailPayload()).toEqual({ taskId: "8008" });
        await expect(page.getByLabel("抽取任务详情")).toContainText(
            '{"sourceContentIds":[1001,1002]}'
        );
        await expect(page.getByLabel("抽取任务详情")).toContainText("6001");
        await expect(page.getByLabel("抽取任务详情")).toContainText("7001");

        await page.getByRole("button", { name: "应用候选结果" }).click();
        await expect.poll(() => mocks.getApplyPayload()).toEqual({ taskId: "8008" });

        await page.keyboard.press("Escape");
        await page
            .getByTestId("knowledge-graph-extraction-graph-extraction-task-action-button-2")
            .evaluate((element) => {
                (element as HTMLButtonElement).click();
            });
        await expect
            .poll(() => mocks.getCancelPayload())
            .toEqual({
                batchJobId: 1001,
                requestedBy: 99
            });

        await page.keyboard.press("Escape");
        await page.setViewportSize({ width: 560, height: 820 });
        await expect
            .poll(async () =>
                page.locator(".knowledge-graph-extraction-tree-panel").evaluate((element) => {
                    return Math.round(element.getBoundingClientRect().width);
                })
            )
            .toBeLessThanOrEqual(1);

        await page.goto(
            "/knowledge/graph-extraction?regenerate=1&taskType=GRAPH&sourceTaskId=88&triggerSource=REFINEMENT_APPLIED&replaceUnconfirmedOnly=true&selectionScopeJson=%7B%22sourceContentIds%22%3A%5B1001%5D%7D"
        );
        await expect(page.getByText("精修应用后的图谱重生成参数已载入")).toBeVisible();
        await page.getByRole("button", { name: "提交重生成" }).click();
        await expect
            .poll(() => mocks.getRegeneratePayload())
            .toEqual({
                taskType: "GRAPH",
                sourceTaskId: "88",
                triggerSource: "REFINEMENT_APPLIED",
                replaceUnconfirmedOnly: true,
                selectionScopeJson: '{"sourceContentIds":[1001]}'
            });
    });
});
