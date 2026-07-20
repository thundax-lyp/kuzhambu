import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen, waitFor, within } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { DiscoverySearchItemPage } from "./search-item-page";

const apiResponse = (data: unknown) =>
    Promise.resolve(
        new Response(JSON.stringify({ code: "COMMON-00000", data, message: "success" }), {
            headers: { "Content-Type": "application/json" },
            status: 200
        })
    );

const installFetchMock = () => {
    vi.spyOn(globalThis, "fetch").mockImplementation((input, init) => {
        const url = String(input);
        const body = init?.body ? JSON.parse(String(init.body)) : {};

        if (url.includes("/portal/classics/sancai/entries/get")) {
            return apiResponse({
                id: Number(body.id),
                title: "三才图会引 (顾秉谦)",
                authorName: "顾秉谦",
                originalMarkdown: "## 原文小题\n原文**应该**展示",
                translationHtml:
                    '<p>译文应该展示</p><img src="javascript:alert(1)" onerror="bad()" />',
                summary: "摘要应该展示",
                imageStatus: "READY",
                lifecycleStatus: "PUBLISHED",
                refinementStatus: "REFINED",
                translationStatus: "READY",
                visibility: "PUBLIC",
                tags: [{ id: 1, tagName: "文献学" }],
                images: [
                    { currentUsed: true, id: 2, previewUrl: "/api/cover.png", title: "正文插图" }
                ],
                currentVisualAsset: {
                    imageAnalysisMarkdown: "- 图像说明应该展示",
                    status: "GENERATED",
                    visualDescription: "视觉描述应该展示",
                    visualAssetId: 3
                }
            });
        }

        return apiResponse(null);
    });
};

const renderPage = (initialEntry = "/discovery/search-item?type=SANCAI_ENTRY&id=300000000003") => {
    const client = new QueryClient({
        defaultOptions: {
            queries: {
                retry: false
            }
        }
    });

    render(
        <QueryClientProvider client={client}>
            <MemoryRouter initialEntries={[initialEntry]}>
                <DiscoverySearchItemPage />
            </MemoryRouter>
        </QueryClientProvider>
    );
};

describe("DiscoverySearchItemPage", () => {
    beforeEach(() => {
        installFetchMock();
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it("loads content from id query parameter through type renderer", async () => {
        renderPage();

        const detail = await screen.findByLabelText("检索内容详情");
        await waitFor(() => {
            expect(within(detail).getByText("三才图会引 (顾秉谦)")).toBeTruthy();
        });
        expect(within(detail).getByText("作者：顾秉谦")).toBeTruthy();
        expect(within(detail).getByText("摘要应该展示")).toBeTruthy();
        expect(within(detail).getByText("原文小题")).toBeTruthy();
        expect(detail.textContent).toContain("原文应该展示");
        expect(within(detail).getByText("译文应该展示")).toBeTruthy();
        expect(within(detail).getByText("图像说明应该展示")).toBeTruthy();
        expect(within(detail).getByText("文献学")).toBeTruthy();
        expect(within(detail).getByAltText("正文插图").getAttribute("src")).toBe(
            "/kuzhambu-api/api/cover.png"
        );
        expect(within(detail).queryByText("发布状态")).toBeNull();
        expect(detail.innerHTML).not.toContain("onerror");
        expect(detail.innerHTML).not.toContain("javascript:");

        const getEntryCall = vi
            .mocked(globalThis.fetch)
            .mock.calls.find(([input]) => String(input).includes("/entries/get"));
        expect(getEntryCall).toBeTruthy();
        expect(JSON.parse(String(getEntryCall?.[1]?.body))).toMatchObject({
            id: 300000000003
        });
    });
});
