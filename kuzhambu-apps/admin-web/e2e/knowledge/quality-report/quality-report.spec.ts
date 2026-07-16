import { expect, test } from "@playwright/test";
import type { Page, Route } from "@playwright/test";

const ADMIN_PERMISSIONS = [
    "knowledge:quality-report:view",
    "knowledge:quality-report:generate",
    "knowledge:graph:edit"
];

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
                id: "quality-report",
                parentId: "knowledge",
                name: "质量报告",
                url: "/knowledge/quality-report",
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

const createQualityReportMockHandlers = async (page: Page) => {
    let latestPayload: ApiPayload | null = null;
    let pagePayload: ApiPayload | null = null;
    let detailPayload: ApiPayload | null = null;
    let generatePayload: ApiPayload | null = null;
    let reextractPayload: ApiPayload | null = null;

    const report = {
        reportId: 5001,
        reportNo: "QR-20260709-001",
        graphVersionId: 71,
        sourceContentType: "SANCAI_ENTRY",
        sourceContentId: 1001,
        sourceCategoryCode: "medicine",
        sourceCategoryName: "医药",
        reportStatus: "PUBLISHED",
        entityTotalCount: 10,
        entityConfirmedCount: 8,
        relationTotalCount: 6,
        relationConfirmedCount: 5,
        lineageTotalCount: 4,
        lineageConfirmedCount: 3,
        entityCoverageRate: 0.8,
        relationAccuracyRate: 0.75,
        lineageCoverageRate: 0.7,
        completenessRate: 0.72,
        annotationCount: 3,
        issueCount: 2,
        generatedBy: 1,
        generatedAt: 1790000000000,
        publishedAt: 1790000100000
    };
    const detail = {
        report,
        issues: [
            {
                issueId: 91,
                issueType: "LOW_CONFIDENCE_ENTITY",
                severity: "high",
                objectType: "ENTITY",
                objectKey: "entity:li-shizhen",
                title: "实体置信度偏低",
                description: "实体来源证据不足",
                suggestion: "补充来源引用",
                href: "/knowledge/refinement"
            }
        ],
        sourceDetails: [
            {
                detailId: 81,
                sourceContentType: "SANCAI_ENTRY",
                sourceContentId: 1001,
                sourceCategoryCode: "medicine",
                sourceCategoryName: "医药",
                graphVersionId: 71,
                annotationCount: 3,
                issueCount: 2,
                status: "LOW_QUALITY",
                href: "/knowledge/graph-results?graphVersionId=71"
            }
        ],
        annotations: [
            {
                annotationId: 3001,
                objectType: "ENTITY",
                objectKey: "entity:li-shizhen",
                graphVersionId: 71,
                annotationStatus: "ISSUE",
                annotationLabel: "MISSING_SOURCE",
                comment: "缺少来源"
            }
        ],
        stale: true,
        staleReason: "REFINEMENT_APPLIED",
        lastRefinementAppliedAt: 1790000200000
    };

    await page.route("**/kuzhambu-admin-api/api/knowledge/quality/report/latest", async (route) => {
        latestPayload = readRequestBody(route.request().postData());
        await fulfillSuccess(route, detail);
    });
    await page.route("**/kuzhambu-admin-api/api/knowledge/quality/report/page", async (route) => {
        pagePayload = readRequestBody(route.request().postData());
        await fulfillSuccess(route, {
            pageNo: 1,
            pageSize: 20,
            totalCount: 1,
            count: 1,
            records: [report]
        });
    });
    await page.route("**/kuzhambu-admin-api/api/knowledge/quality/report/detail", async (route) => {
        detailPayload = readRequestBody(route.request().postData());
        await fulfillSuccess(route, detail);
    });
    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/quality/report/generate",
        async (route) => {
            generatePayload = readRequestBody(route.request().postData());
            await fulfillSuccess(route, {
                ...detail,
                stale: false
            });
        }
    );
    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/quality/report/reextract-low-quality-category",
        async (route) => {
            reextractPayload = readRequestBody(route.request().postData());
            await fulfillSuccess(route, {
                reportId: 5001,
                sourceCategoryCode: "medicine",
                sourceCategoryName: "医药",
                sourceContentType: "SANCAI_ENTRY",
                sourceContentId: 1001,
                taskId: 9002,
                batchJobId: 7002,
                taskType: "GRAPH",
                triggerSource: "QUALITY_REPORT",
                selectionScopeJson: '{"sourceCategoryCode":"medicine"}',
                replaceUnconfirmedOnly: true
            });
        }
    );

    return {
        getLatestPayload: () => latestPayload,
        getPagePayload: () => pagePayload,
        getDetailPayload: () => detailPayload,
        getGeneratePayload: () => generatePayload,
        getReextractPayload: () => reextractPayload
    };
};

test.describe("admin quality report smoke", () => {
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

    test("generates, opens history, reads report tabs, and reextracts low-quality categories", async ({
        page
    }) => {
        const mocks = await createQualityReportMockHandlers(page);

        await page.goto("/knowledge/quality-report?graphVersionId=71&regenerate=1");

        await expect(page.getByRole("heading", { name: "质量报告" })).toBeVisible();
        await expect.poll(() => mocks.getLatestPayload()).toEqual({ graphVersionId: 71 });
        await expect
            .poll(() => mocks.getPagePayload())
            .toEqual({
                pageNo: 1,
                pageSize: 20,
                graphVersionId: 71
            });
        await expect(page.getByLabel("报告摘要").getByText("QR-20260709-001")).toBeVisible();
        await expect(page.getByText("实体覆盖率")).toBeVisible();
        await expect(page.getByText("该版本质量报告早于最新精修应用")).toBeVisible();

        await page.getByRole("spinbutton").fill("72");
        await page.getByRole("button", { name: "重新生成报告" }).first().click();
        await expect
            .poll(() => mocks.getGeneratePayload())
            .toEqual({
                graphVersionId: 72,
                generatedBy: 1
            });

        await page
            .getByLabel("知识质量报告历史表格")
            .locator("button")
            .filter({ hasText: /查\s*看/ })
            .click({ force: true });
        await expect.poll(() => mocks.getDetailPayload()).toEqual({ reportId: 5001 });
        await expect(page.getByRole("table", { name: "知识质量报告问题清单表格" })).toContainText(
            "实体置信度偏低"
        );

        await page.getByRole("tab", { name: "来源明细" }).click();
        await expect(page.getByRole("table", { name: "知识质量报告来源明细表格" })).toContainText(
            "LOW_QUALITY"
        );
        await page.getByTestId("quality-report-source-81-reextract-button").click();
        await expect(page.getByRole("dialog", { name: "确认重提取低质量门类" })).toBeVisible();
        await page.getByRole("button", { name: "重提取" }).last().click();
        await expect
            .poll(() => mocks.getReextractPayload())
            .toMatchObject({
                reportId: 5001,
                sourceCategoryCode: "medicine",
                taskType: "GRAPH",
                replaceUnconfirmedOnly: true,
                modelId: 1,
                modelName: "gpt-5.5",
                promptMessagesJson:
                    '[{"role":"system","content":"extract knowledge graph from quality report low quality category"}]',
                inputPayloadJson: '{"triggerSource":"QUALITY_REPORT"}',
                requestedBy: 1
            });
        await expect(
            page.locator(".ant-alert").getByText("低质量门类重提取任务已创建")
        ).toBeVisible();
        await expect(page.getByText("任务号：9002")).toBeVisible();

        await page.getByRole("tab", { name: "人工标注" }).click();
        await expect(page.getByRole("table", { name: "知识质量报告人工标注表格" })).toContainText(
            "MISSING_SOURCE"
        );
    });
});
