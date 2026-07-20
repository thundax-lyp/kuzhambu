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
        const queryText = requestBody.queryText ?? "";

        if (!queryText) {
            const isSecondPage = requestBody.pageNo === 2;
            await fulfillSuccess(route, {
                displayQueryText: "",
                groupCount: 1,
                queryText: "",
                searchEventId: "search-event-empty",
                totalCount: isSecondPage ? 21 : 1,
                groups: [
                    {
                        count: 1,
                        groupKey: "SANCAI_ENTRY",
                        groupTitle: isSecondPage ? "王圻文档" : "三才图会",
                        items: [
                            isSecondPage
                                ? {
                                      contentDomain: "CLASSICS",
                                      contentId: "2001",
                                      contentType: "WANGQI_DOCUMENT",
                                      groupRank: 1,
                                      highlightText:
                                          "明代知县王圻在万安任内审理疑案时，面对嫌犯以屠夫血迹辩解，并未轻信。他细致观察案几上陈旧血迹的分布形态，发现其呈左右分列状且集中于边缘，与屠宰痕迹不符。通过现场模拟，王圻推断血迹是凶手抱案几垫脚翻墙抛尸时留下的手印。在精准的物证推理面前，嫌犯最终认罪。此案展现了王圻重视物证、善用逻辑推理的司法智慧。",
                                      resultRank: 11,
                                      targetPath: "/classics/wangqi/2001",
                                      title: '"案几血迹案"：明代知县的血迹推理术'
                                  }
                                : {
                                      contentDomain: "CLASSICS",
                                      contentId: "1000",
                                      contentType: "SANCAI_ENTRY",
                                      groupRank: 1,
                                      highlightText: "三才图会凡例",
                                      resultRank: 1,
                                      targetPath: "/classics/sancai/1000",
                                      title: "三才图会凡例"
                                  }
                        ]
                    }
                ]
            });
            return;
        }

        await fulfillSuccess(route, {
            displayQueryText: "礼学",
            groupCount: 1,
            queryText: "礼学",
            searchEventId: "search-event-1",
            totalCount: 1,
            groups: [
                {
                    count: 1,
                    groupKey: "SANCAI_ENTRY",
                    groupTitle: "三才图会",
                    items: [
                        {
                            contentDomain: "CLASSICS",
                            contentId: "1001",
                            contentType: "SANCAI_ENTRY",
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
    test("loads default discovery results with an empty query", async ({ page }) => {
        const mocks = await createSearchMockHandlers(page);

        await page.goto("/discovery/search");

        await expect
            .poll(() => mocks.getSearchRequests().some((payload) => payload.queryText === ""))
            .toBe(true);
        await expect(page.getByText("检索失败")).toHaveCount(0);
        await expect(page.getByText("共 1 条命中")).toBeVisible();
        await expect(page.getByRole("heading", { name: "三才图会凡例" })).toBeVisible();
    });

    test("keeps page two result cards in a readable search layout", async ({ page }) => {
        await createSearchMockHandlers(page);

        await page.goto("/discovery/search?pageNo=2");

        await expect(page.getByText("共 21 条命中")).toBeVisible();
        await expect(
            page.getByRole("heading", { name: '"案几血迹案"：明代知县的血迹推理术' })
        ).toBeVisible();
        await expect(page.getByText("第 2 / 3 页")).toBeVisible();
        await expect(page.getByText("共 21 条", { exact: true })).toBeVisible();

        const layout = await page.evaluate(() => {
            const card = document.querySelector(".portal-discovery-hit");
            const title = document.querySelector(".portal-discovery-hit-title");
            const summary = document.querySelector(".portal-discovery-hit-summary");
            const cardRect = card?.getBoundingClientRect();
            const titleRect = title?.getBoundingClientRect();
            const summaryRect = summary?.getBoundingClientRect();

            return {
                cardHeight: cardRect?.height ?? 0,
                cardWidth: cardRect?.width ?? 0,
                summaryHeight: summaryRect?.height ?? 0,
                titleWidth: titleRect?.width ?? 0
            };
        });

        expect(layout.titleWidth).toBeGreaterThan(layout.cardWidth * 0.6);
        expect(layout.summaryHeight).toBeLessThan(110);
        expect(layout.cardHeight).toBeLessThan(190);
    });

    test("searches and records click payload", async ({ page }) => {
        const mocks = await createSearchMockHandlers(page);

        await page.goto("/discovery/search");
        await page.getByRole("textbox", { name: "搜索词" }).fill("礼学");
        await page.getByRole("button", { name: "高级筛选" }).click();
        await page
            .getByRole("group", { name: "知识库" })
            .getByRole("button", { name: "三才图会" })
            .click();
        await page
            .getByLabel("Discovery 搜索", { exact: true })
            .getByRole("button", { name: "开始检索" })
            .click();

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
        await expect(page.getByRole("heading", { name: "三才图会" })).toHaveCount(0);
        await expect(page.getByRole("heading", { name: "礼制条目" })).toBeVisible();
        await expect(page.getByText("古籍内容 · 三才图会")).toBeVisible();
        await expect(page.getByText("search-event-1")).toHaveCount(0);
        await expect(page.getByText("回显词")).toHaveCount(0);
        await expect(page.getByText("全局")).toHaveCount(0);
        await expect(page.getByText("组内")).toHaveCount(0);
        await expect(page.getByText("SANCAI_ENTRY")).toHaveCount(0);
        await expect(page.locator("mark", { hasText: "礼学" }).first()).toBeVisible();

        await page.getByRole("button", { name: "打开搜索结果：礼制条目" }).click();
        await expect(page).toHaveURL(/\/knowledge\/atlas\?level=detail&entityId=1001/);

        expect(mocks.getClickPayload()).toMatchObject({
            contentType: "SANCAI_ENTRY",
            contentId: "1001",
            resultGroupKey: "SANCAI_ENTRY",
            searchEventId: "search-event-1"
        });
        await expect(page.getByRole("dialog")).toHaveCount(0);
    });
});
