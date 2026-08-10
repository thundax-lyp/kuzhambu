import { expect, test } from "@playwright/test";
import type { Locator, Page, Route } from "@playwright/test";

const ADMIN_PERMISSIONS = ["knowledge:refinement:view", "knowledge:refinement:edit"];

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
                id: "refinement",
                parentId: "knowledge-graph",
                name: "图谱工作台",
                url: "/knowledge/refinement",
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

const createRefinementMockHandlers = async (page: Page) => {
    let taskPagePayload: ApiPayload | null = null;
    let taskOpenPayload: ApiPayload | null = null;
    let entityUpdatePayload: ApiPayload | null = null;
    let entityConfirmPayload: ApiPayload | null = null;
    let annotationUpdatePayload: ApiPayload | null = null;
    let taskApplyPayload: ApiPayload | null = null;

    const task = {
        refinementTaskId: 9001,
        graphVersionId: 71,
        taskType: "GRAPH",
        sourceContentType: "SANCAI_ENTRY",
        sourceContentId: 1001,
        sourceCategoryCode: "medicine",
        sourceCategoryName: "医药",
        status: "DRAFT",
        openedBy: 1,
        progressSummary: {
            entityPendingCount: 1,
            entityConfirmedCount: 0,
            relationPendingCount: 1,
            relationConfirmedCount: 0
        }
    };
    const detail = {
        ...task,
        entities: [
            {
                draftId: 11,
                entityId: 501,
                entityKey: "entity:li-shizhen",
                originType: "AI",
                operationType: "UPDATED",
                name: "李时珍",
                entityType: "PERSON",
                description: "明代医药学家",
                confirmationStatus: "PENDING",
                sourceRefsJson: '{"entryId":1001}',
                sortOrder: 1
            }
        ],
        relations: [
            {
                draftId: 21,
                relationId: 601,
                relationKey: "relation:author",
                sourceEntityKey: "entity:li-shizhen",
                targetEntityKey: "entity:bencao",
                sourceName: "李时珍",
                targetName: "本草纲目",
                relationType: "AUTHOR_OF",
                evidence: "李时珍撰本草纲目",
                confirmationStatus: "PENDING",
                sourceRefsJson: '{"entryId":1001}',
                sortOrder: 1
            }
        ],
        lineageNodes: [
            {
                draftId: 31,
                nodeId: 701,
                nodeKey: "lineage:li",
                name: "李氏",
                nodeType: "PERSON",
                generation: 0,
                confirmationStatus: "PENDING",
                operationType: "UPDATED",
                sourceRefsJson: '{"entryId":1001}',
                sortOrder: 1
            }
        ],
        lineageRelations: [
            {
                draftId: 41,
                relationId: 801,
                relationKey: "lineage:parent",
                sourceName: "李父",
                targetName: "李时珍",
                relationType: "PARENT_OF",
                confirmationStatus: "PENDING",
                operationType: "UPDATED",
                sourceRefsJson: '{"entryId":1001}',
                sortOrder: 1
            }
        ],
        entityOptions: [
            {
                entityKey: "entity:li-shizhen",
                name: "李时珍"
            }
        ]
    };
    const annotation = {
        annotationId: 3001,
        objectType: "ENTITY",
        objectKey: "entity:li-shizhen",
        graphVersionId: 71,
        annotationStatus: "ISSUE",
        annotationLabel: "WRONG_ENTITY",
        comment: "待人工复核"
    };

    await page.route("**/kuzhambu-admin-api/api/knowledge/refinement/task/page", async (route) => {
        taskPagePayload = readRequestBody(route.request().postData());
        await fulfillSuccess(route, {
            pageNo: 1,
            pageSize: 20,
            totalCount: 1,
            count: 1,
            records: [task]
        });
    });
    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/refinement/task/create",
        async (route) => {
            taskOpenPayload = readRequestBody(route.request().postData());
            await fulfillSuccess(route, detail);
        }
    );
    await page.route("**/kuzhambu-admin-api/api/knowledge/refinement/task/get", async (route) => {
        await readRequestBody(route.request().postData());
        await fulfillSuccess(route, detail);
    });
    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/refinement/quality/get",
        async (route) => {
            await fulfillSuccess(route, {
                entityCoverageRate: 0.8,
                relationAccuracyRate: 0.75,
                completenessRate: 0.7
            });
        }
    );
    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/refinement/annotation/page",
        async (route) => {
            await fulfillSuccess(route, {
                pageNo: 1,
                pageSize: 200,
                totalCount: 1,
                count: 1,
                records: [annotation]
            });
        }
    );
    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/refinement/entity/update",
        async (route) => {
            entityUpdatePayload = readRequestBody(route.request().postData());
            await fulfillSuccess(route, {
                ...detail.entities[0],
                ...entityUpdatePayload
            });
        }
    );
    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/refinement/entity/confirm",
        async (route) => {
            entityConfirmPayload = readRequestBody(route.request().postData());
            await fulfillSuccess(route, {
                ...detail.entities[0],
                confirmationStatus: "MANUAL_CONFIRMED"
            });
        }
    );
    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/refinement/annotation/update",
        async (route) => {
            annotationUpdatePayload = readRequestBody(route.request().postData());
            await fulfillSuccess(route, {
                ...annotation,
                ...annotationUpdatePayload
            });
        }
    );
    await page.route("**/kuzhambu-admin-api/api/knowledge/refinement/task/apply", async (route) => {
        taskApplyPayload = readRequestBody(route.request().postData());
        await fulfillSuccess(route, {
            refinementTaskId: 9001,
            graphVersionId: 72,
            taskType: "GRAPH",
            sourceContentType: "SANCAI_ENTRY",
            sourceContentId: 1001,
            sourceCategoryCode: "medicine",
            sourceCategoryName: "医药",
            status: "APPLIED",
            graphRefreshRequired: true,
            regenerateSupported: true,
            sourceTaskId: 8008,
            selectionScopeJson: '{"sourceContentIds":[1001]}',
            replaceUnconfirmedOnly: true,
            triggerSource: "REFINEMENT_APPLIED",
            qualityReportRefreshRequired: true
        });
    });

    return {
        getTaskPagePayload: () => taskPagePayload,
        getTaskOpenPayload: () => taskOpenPayload,
        getEntityUpdatePayload: () => entityUpdatePayload,
        getEntityConfirmPayload: () => entityConfirmPayload,
        getAnnotationUpdatePayload: () => annotationUpdatePayload,
        getTaskApplyPayload: () => taskApplyPayload
    };
};

const selectOption = async (page: Page, comboboxName: string, optionTitle: string) => {
    const combobox = page.getByRole("combobox", { name: comboboxName });
    await combobox.evaluate((element) => element.scrollIntoView({ block: "center" }));
    await combobox.focus();
    await page.keyboard.press("ArrowDown");
    await page
        .getByTitle(optionTitle)
        .last()
        .evaluate((element) => (element as HTMLElement).click());
};

const rowByText = (table: Locator, text: string) => {
    return table.getByRole("row").filter({ hasText: text });
};

const rowButtonByText = (table: Locator, rowText: string, buttonText: RegExp) => {
    return rowByText(table, rowText).locator("button").filter({ hasText: buttonText });
};

test.describe("admin refinement smoke", () => {
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

    test("filters, opens, edits, annotates, and applies refinement tasks", async ({ page }) => {
        const mocks = await createRefinementMockHandlers(page);

        await page.goto("/knowledge/refinement");

        await expect(page.getByRole("heading", { name: "知识图谱工作台" })).toBeVisible();
        await expect(page.getByRole("table", { name: "知识图谱精修任务表格" })).toContainText(
            "9001"
        );

        await selectOption(page, "任务类型", "图谱");
        await page.getByRole("textbox", { name: "门类编码" }).fill("medicine");
        await page.getByRole("textbox", { name: "来源类型" }).fill("SANCAI_ENTRY");
        await selectOption(page, "状态", "草稿");
        await page.keyboard.press("Escape");
        await page
            .locator("button")
            .filter({ hasText: /筛\s*选/ })
            .click({ force: true });
        await expect
            .poll(() => mocks.getTaskPagePayload())
            .toMatchObject({
                pageNo: 1,
                pageSize: 20,
                taskType: "GRAPH",
                sourceCategoryCode: "medicine",
                sourceContentType: "SANCAI_ENTRY",
                status: "DRAFT"
            });

        await page
            .locator("button")
            .filter({ hasText: /重\s*置/ })
            .click({ force: true });
        await expect(page.getByRole("textbox", { name: "门类编码" })).toHaveValue("");
        await expect(page.getByRole("textbox", { name: "来源类型" })).toHaveValue("");

        await page.getByRole("button", { name: "打开任务" }).click();
        await expect
            .poll(() => mocks.getTaskOpenPayload())
            .toEqual({
                graphVersionId: 71,
                openedBy: "user-1"
            });
        await expect(page.getByRole("table", { name: "知识图谱精修实体表格" })).toContainText(
            "李时珍"
        );
        await expect(page.getByText("实体覆盖率")).toBeVisible();

        const entityTable = page.getByRole("table", { name: "知识图谱精修实体表格" });
        await rowButtonByText(entityTable, "李时珍", /编\s*辑/).click();
        const entityDialog = page.getByRole("dialog", { name: "编辑实体草稿" });
        await entityDialog.getByRole("textbox", { name: "名称" }).fill("李时珍修订");
        await entityDialog.getByRole("textbox", { name: "实体类型" }).fill("PERSON");
        await entityDialog.getByRole("textbox", { name: "描述" }).fill("明代医药学家，精修确认");
        await entityDialog
            .getByRole("button", { name: /确\s*定/ })
            .evaluate((element) => (element as HTMLButtonElement).click());
        await expect
            .poll(() => mocks.getEntityUpdatePayload())
            .toMatchObject({
                refinementTaskId: 9001,
                entityId: 501,
                entityKey: "entity:li-shizhen",
                name: "李时珍修订",
                entityType: "PERSON",
                description: "明代医药学家，精修确认",
                operatorId: "user-1"
            });

        await rowButtonByText(entityTable, "李时珍", /确\s*认/).click();
        await expect
            .poll(() => mocks.getEntityConfirmPayload())
            .toEqual({
                refinementTaskId: 9001,
                entityKey: "entity:li-shizhen",
                operatorId: "user-1"
            });

        await rowButtonByText(entityTable, "李时珍", /标\s*注/).click();
        await expect(page.getByText("质量标注 / ENTITY / entity:li-shizhen")).toBeVisible();
        await selectOption(page, "标注状态", "ISSUE");
        await selectOption(page, "标注标签", "WRONG_ENTITY");
        await page.getByRole("textbox", { name: "备注" }).fill("实体边界需要复核");
        await page
            .getByTestId("knowledge-refinement-refinement-quality-annotation-save-button")
            .evaluate((element) => (element as HTMLButtonElement).click());
        await expect
            .poll(() => mocks.getAnnotationUpdatePayload())
            .toMatchObject({
                annotationId: 3001,
                objectType: "ENTITY",
                objectKey: "entity:li-shizhen",
                sourceContentType: "SANCAI_ENTRY",
                sourceContentId: 1001,
                graphVersionId: 71,
                annotationStatus: "ISSUE",
                annotationLabel: "WRONG_ENTITY",
                comment: "实体边界需要复核",
                operatorId: "user-1"
            });

        await page.getByText("应用任务").click();
        await expect
            .poll(() => mocks.getTaskApplyPayload())
            .toEqual({
                refinementTaskId: 9001,
                appliedBy: "user-1"
            });
        await expect(page.getByText("精修已应用，图谱与质量报告需要继续联动处理")).toBeVisible();
        await expect(page.getByRole("link", { name: "查看图谱结果" })).toHaveAttribute(
            "href",
            "/knowledge/graph-results?graphVersionId=72"
        );
        await expect(page.getByRole("link", { name: "重生成图谱" })).toHaveAttribute(
            "href",
            "/knowledge/graph-extraction?regenerate=1&taskType=GRAPH&sourceTaskId=8008&triggerSource=REFINEMENT_APPLIED&replaceUnconfirmedOnly=true&selectionScopeJson=%7B%22sourceContentIds%22%3A%5B1001%5D%7D"
        );
        await expect(page.getByRole("link", { name: "重新生成质量报告" })).toHaveAttribute(
            "href",
            "/knowledge/quality-report?graphVersionId=72&regenerate=1"
        );
    });
});
