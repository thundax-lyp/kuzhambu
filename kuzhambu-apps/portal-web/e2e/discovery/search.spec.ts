import { expect, test, type Page, type Route } from "@playwright/test";

type ApiPayload = {
    categoryCodes?: unknown[];
    contentStatuses?: unknown[];
    dateFrom?: unknown;
    dateTo?: unknown;
    knowledgeBases?: unknown[];
    pageNo?: number;
    pageSize?: number;
    queryText?: string;
    tagNames?: unknown[];
    visibilityScopes?: unknown[];
};

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

type DiscoverySearchPayload = ApiPayload;

const createSearchMockHandlers = async (page: Page) => {
    const searchRequests: DiscoverySearchPayload[] = [];
    let clickPayload: Record<string, unknown> | null = null;

    await page.route("**/kuzhambu-api/api/portal/discovery/search/search", async (route) => {
        const requestBody = readRequestBody(route.request().postData()) as DiscoverySearchPayload;
        searchRequests.push(requestBody);

        await fulfillSuccess(route, {
            displayQueryText: "礼学",
            groupCount: 1,
            queryText: "礼学",
            searchLogId: "search-log-1",
            totalCount: 1,
            groups: [
                {
                    count: 1,
                    groupKey: "SANCAI_ENTRY",
                    groupTitle: "三才图会",
                    items: [
                        {
                            contentDomain: "三才图会",
                            contentId: "1001",
                            contentType: "ENTRY",
                            groupRank: 1,
                            highlightText: "<mark>礼学</mark> 与礼制",
                            resultRank: 1,
                            targetPath: "/knowledge/atlas?level=detail&entityId=1001",
                            title: "礼制条目"
                        }
                    ]
                }
            ]
        });
    });

    await page.route("**/kuzhambu-api/api/portal/discovery/search/click", async (route) => {
        clickPayload = readRequestBody(route.request().postData());
        await fulfillSuccess(route, true);
    });

    return {
        getSearchRequests: () => searchRequests,
        getClickPayload: () => clickPayload
    };
};

test.describe("portal discovery search smoke", () => {
    test("searches and records click payload", async ({ page }) => {
        const mocks = await createSearchMockHandlers(page);

        await page.goto("/discovery/search");
        await page.getByRole("textbox", { name: "搜索词" }).fill("礼学");
        await page
            .getByRole("group", { name: "知识库" })
            .getByRole("button", { name: "三才图会" })
            .click();
        await page.getByRole("button", { name: "开始检索" }).click();

        await expect
            .poll(() => mocks.getSearchRequests().some((payload) => payload.queryText === "礼学"))
            .toBe(true);

        const searchRequest = mocks
            .getSearchRequests()
            .find((payload) => payload.queryText === "礼学");
        expect(searchRequest).toMatchObject({
            queryText: "礼学",
            knowledgeBases: ["SANCAI_ENTRY"],
            categoryCodes: [],
            tagNames: [],
            contentStatuses: [],
            visibilityScopes: [],
            pageNo: 1,
            pageSize: 10
        });

        await expect(page).toHaveURL(/q=%E7%A4%BC%E5%AD%A6/);
        await expect(page).toHaveURL(/knowledgeBases=SANCAI_ENTRY/);

        await expect(page.getByText("共 1 条命中")).toBeVisible();
        await expect(page.getByRole("heading", { name: "三才图会" })).toBeVisible();
        await expect(page.getByRole("heading", { name: "礼制条目" })).toBeVisible();
        await expect(page.getByText("礼学", { exact: false })).toBeVisible();

        await page.getByRole("link", { name: "礼制条目" }).click();

        expect(mocks.getClickPayload()).toMatchObject({
            contentType: "ENTRY",
            contentId: "1001",
            resultGroupKey: "SANCAI_ENTRY",
            searchLogId: "search-log-1"
        });
    });
});
