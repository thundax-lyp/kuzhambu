import { expect, test, type Page, type Route } from "@playwright/test";

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

const createQaMockHandlers = async (page: Page) => {
    let openSessionPayload: ApiPayload | null = null;
    let chatPayload: ApiPayload | null = null;

    await page.route("**/kuzhambu-api/api/portal/discovery/qa/session/open", async (route) => {
        openSessionPayload = readRequestBody(route.request().postData());
        await fulfillSuccess(route, {
            contextContentId: openSessionPayload.contextContentId ?? null,
            contextContentType: openSessionPayload.contextContentType ?? null,
            contextMode: openSessionPayload.contextMode ?? "GENERAL",
            lastMessageAt: 1700001000000,
            openedAt: 1700000000000,
            scope: "PORTAL",
            sessionId: "7001",
            status: "OPEN",
            title: openSessionPayload.title ?? "知识中心问答"
        });
    });

    await page.route(
        "**/kuzhambu-api/api/portal/discovery/qa/chat/completions/stream",
        async (route) => {
            chatPayload = readRequestBody(route.request().postData());
            await route.fulfill({
                contentType: "text/event-stream",
                body: 'event:completed\ndata:{"answerStatus":"SUCCEEDED","choices":[{"finishReason":"stop","index":0,"message":{"content":"礼学可作为礼制相关内容的检索扩展。","role":"assistant"}}],"id":"chat-1","model":"kuzhambu-qa","questionMessageId":"1","answerMessageId":"2","sessionId":"7001","sources":[{"contentId":1001,"contentType":"ENTRY","knowledgeBase":"SANCAI_ENTRY","sourceId":"SANCAI_ENTRY:1001","sourcePath":"/knowledge/atlas?level=detail&entityId=1001","sourceStatus":"AVAILABLE","sourceRank":1,"titleSnapshot":"礼制条目"}]}\n\n'
            });
        }
    );

    return {
        getOpenSessionPayload: () => openSessionPayload,
        getChatPayload: () => chatPayload
    };
};

test.describe("portal discovery qa smoke", () => {
    test("auto-opens session and sends question", async ({ page }) => {
        const mocks = await createQaMockHandlers(page);

        await page.goto("/discovery/qa");
        await expect(page.getByRole("button", { name: "发送" })).toBeEnabled();
        await expect(page.getByText("会话列表")).toHaveCount(0);
        await expect(page.getByText("导出 CSV")).toHaveCount(0);
        await expect(page.getByText("删除会话")).toHaveCount(0);

        await page.getByRole("textbox", { name: "问题" }).fill("礼学和礼制有什么关系？");
        await page.getByRole("textbox", { name: "问题" }).press("Enter");

        await expect.poll(() => mocks.getOpenSessionPayload()).not.toBeNull();

        expect(mocks.getOpenSessionPayload()).toMatchObject({
            ownerUserId: 1001,
            scope: "PORTAL",
            title: "知识中心问答",
            contextMode: "GENERAL"
        });

        await expect.poll(() => mocks.getChatPayload()).not.toBeNull();

        expect(mocks.getChatPayload()).toMatchObject({
            model: "kuzhambu-qa",
            stream: true,
            messages: [
                {
                    role: "user",
                    content: "礼学和礼制有什么关系？"
                }
            ],
            sessionId: "7001",
            metadata: {
                sessionId: "7001",
                contextMode: "GENERAL"
            }
        });

        await expect(page.getByText("礼学和礼制有什么关系？")).toBeVisible();
        await expect(page.getByText("礼学可作为礼制相关内容的检索扩展。")).toBeVisible();
        await expect(page.getByText("礼制条目")).toBeVisible();
    });

    test("opens Wangqi single document context from url", async ({ page }) => {
        const mocks = await createQaMockHandlers(page);

        await page.goto(
            "/discovery/qa?contextContentType=WANGQI_DOCUMENT&contextContentId=3001&contextMode=SINGLE_DOCUMENT&title=%E7%8E%8B%E5%9C%BB%E5%AE%98%E5%88%B6"
        );

        await expect(page.getByText("当前文档")).toBeVisible();
        await expect(page.getByText("WANGQI_DOCUMENT #3001").first()).toBeVisible();
        await expect(page.getByText("会话元数据")).toHaveCount(0);

        await page.getByRole("textbox", { name: "问题" }).fill("这份文档说了什么？");
        await page.getByRole("textbox", { name: "问题" }).press("Enter");

        await expect
            .poll(() => mocks.getOpenSessionPayload())
            .toMatchObject({
                contextContentId: 3001,
                contextContentType: "WANGQI_DOCUMENT",
                contextMode: "SINGLE_DOCUMENT",
                title: "王圻官制"
            });
        await expect
            .poll(() => mocks.getChatPayload())
            .toMatchObject({
                sessionId: "7001",
                metadata: {
                    contextContentId: 3001,
                    contextContentType: "WANGQI_DOCUMENT",
                    contextMode: "SINGLE_DOCUMENT",
                    sessionId: "7001"
                }
            });
    });
});
