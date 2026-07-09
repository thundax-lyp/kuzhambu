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
                name: "知识抽取任务",
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

const createGraphExtractionMockHandlers = async (page: Page) => {
    let createPayload: ApiPayload | null = null;
    let detailPayload: ApiPayload | null = null;
    let applyPayload: ApiPayload | null = null;
    let cancelPayload: ApiPayload | null = null;
    let regeneratePayload: ApiPayload | null = null;

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

    return {
        getCreatePayload: () => createPayload,
        getDetailPayload: () => detailPayload,
        getApplyPayload: () => applyPayload,
        getCancelPayload: () => cancelPayload,
        getRegeneratePayload: () => regeneratePayload
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

        await expect(page.getByRole("heading", { name: "知识抽取任务" })).toBeVisible();
        await expect(page.getByText("8008")).toBeVisible();
        await expect(page.getByRole("cell", { name: "QUALITY_REPORT" })).toBeVisible();

        await page.getByRole("textbox", { name: "来源内容类型" }).fill("SANCAI_ENTRY");
        await page.getByRole("spinbutton", { name: "来源内容 ID" }).fill("1001");
        await page.getByRole("textbox", { name: "作用域类型" }).fill("CLASSICS_ENTRY");
        await page.getByRole("textbox", { name: "语言" }).fill("zh-CN");
        await page.getByRole("spinbutton", { name: "模型 ID" }).fill("1");
        await page.getByRole("textbox", { name: "模型名" }).fill("gpt-5.5");
        await page.getByRole("textbox", { name: "作用域 JSON" }).fill('{"entryId":1001}');
        await page
            .getByRole("textbox", { name: "批量范围 JSON" })
            .fill('{"sourceContentIds":[1001,1002]}');
        await page.getByRole("checkbox", { name: "仅替换未人工确认结果" }).check();
        await page
            .getByRole("textbox", { name: "Prompt Messages JSON" })
            .fill('[{"role":"system","content":"extract"}]');
        await page
            .getByRole("textbox", { name: "输入 Payload JSON" })
            .fill('{"content":"待抽取正文"}');
        await page.getByRole("button", { name: "创建图谱抽取任务" }).click();

        await expect
            .poll(() => mocks.getCreatePayload())
            .toMatchObject({
                taskType: "GRAPH",
                triggerSource: "MANUAL",
                sourceContentType: "SANCAI_ENTRY",
                sourceContentId: 1001,
                scopeType: "CLASSICS_ENTRY",
                scopeJson: '{"entryId":1001}',
                selectionScopeJson: '{"sourceContentIds":[1001,1002]}',
                replaceUnconfirmedOnly: true,
                modelId: 1,
                modelName: "gpt-5.5",
                promptMessagesJson: '[{"role":"system","content":"extract"}]',
                inputPayloadJson: '{"content":"待抽取正文"}',
                locale: "zh-CN"
            });
        await expect(page.getByText("最近创建任务")).toBeVisible();

        await page
            .getByLabel("知识抽取任务表格")
            .locator("button")
            .filter({ hasText: /查\s*看/ })
            .first()
            .click({ force: true });
        await expect.poll(() => mocks.getDetailPayload()).toEqual({ taskId: 8008 });
        await expect(page.getByLabel("抽取任务详情")).toContainText(
            '{"sourceContentIds":[1001,1002]}'
        );
        await expect(page.getByLabel("抽取任务详情")).toContainText("6001");
        await expect(page.getByLabel("抽取任务详情")).toContainText("7001");

        await page.getByRole("button", { name: "应用候选结果" }).click();
        await expect.poll(() => mocks.getApplyPayload()).toEqual({ taskId: 8008 });

        await page.keyboard.press("Escape");
        await page.getByRole("button", { name: "取消批任务" }).first().click();
        await expect
            .poll(() => mocks.getCancelPayload())
            .toEqual({
                batchJobId: 1001,
                requestedBy: 99
            });

        await page.goto(
            "/knowledge/graph-extraction?regenerate=1&taskType=GRAPH&sourceTaskId=88&triggerSource=REFINEMENT_APPLIED&replaceUnconfirmedOnly=true&selectionScopeJson=%7B%22sourceContentIds%22%3A%5B1001%5D%7D"
        );
        await expect(page.getByText("精修应用后的图谱重生成参数已载入")).toBeVisible();
        await page.getByRole("button", { name: "提交精修重生成" }).click();
        await expect
            .poll(() => mocks.getRegeneratePayload())
            .toEqual({
                taskType: "GRAPH",
                sourceTaskId: 88,
                triggerSource: "REFINEMENT_APPLIED",
                replaceUnconfirmedOnly: true,
                selectionScopeJson: '{"sourceContentIds":[1001]}'
            });
    });
});
