import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { SancaiPage } from "./sancai-page";

const apiResponse = (data: unknown) =>
    Promise.resolve(
        new Response(JSON.stringify({ code: "COMMON-00000", message: "success", data }), {
            headers: { "Content-Type": "application/json" },
            status: 200
        })
    );

const installFetchMock = () => {
    vi.spyOn(globalThis, "fetch").mockImplementation((input, init) => {
        const url = String(input);
        const body = init?.body ? JSON.parse(String(init.body)) : {};

        if (url.includes("/portal/classics/sancai/categories/list")) {
            return apiResponse([
                {
                    categoryType: "FORMAL",
                    id: 1,
                    title: "天地",
                    publicEntryCount: 13,
                    illustratedEntryCount: 9,
                    thumbnailUrl: "/api/portal/classics/sancai/images/1001/8001/content",
                    thumbnailTitle: "天图"
                }
            ]);
        }
        if (url.includes("/portal/classics/sancai/volumes/list")) {
            return apiResponse([{ categoryId: 1, id: 11, title: "卷一", volumeType: "MAIN" }]);
        }
        if (url.includes("/portal/classics/sancai/entries/get")) {
            const entryId = Number(body.id) || 1001;
            return apiResponse({
                id: entryId,
                volumeId: 11,
                title: entryId === 300000000003 ? "礼器图" : "天",
                originalText: "天者，万物之始。",
                translationText: entryId === 300000000003 ? "礼器图详情。" : "天是万物的开端。",
                summary: entryId === 300000000003 ? "搜索直达条目" : "天地门条目",
                imageStatus: "READY",
                lifecycleStatus: "PUBLISHED",
                refinementStatus: "REFINED",
                translationStatus: "READY",
                visualAssetStatus: "GENERATED",
                visibility: "PUBLIC",
                tags: [{ id: 5001, tagId: 6001, tagName: "天文", source: "MANUAL", priority: 1 }],
                images: [
                    {
                        id: 8001,
                        title: "天图",
                        currentUsed: true,
                        previewUrl: "/api/portal/classics/sancai/images/1001/8001/content"
                    }
                ],
                currentVisualAsset: {
                    status: "GENERATED",
                    visualAssetId: 9001,
                    visualDescription: "天图视觉描述",
                    generatedPreviewUrl:
                        "/api/portal/classics/sancai/visual-assets/1001/9001/generated-content"
                }
            });
        }
        if (url.includes("/portal/classics/sancai/entries/page")) {
            if (body.pageNo === 2) {
                return apiResponse({
                    pageNo: 2,
                    pageSize: 12,
                    totalCount: 13,
                    totalPage: 2,
                    records: [
                        {
                            id: 1002,
                            volumeId: 11,
                            title: "地",
                            originalText: "地者，万物之成。",
                            translationText: "地承载万物。",
                            summary: "地理条目",
                            lifecycleStatus: "PUBLISHED",
                            visibility: "PUBLIC"
                        }
                    ]
                });
            }
            return apiResponse({
                pageNo: 1,
                pageSize: 12,
                totalCount: 13,
                totalPage: 2,
                records: [
                    {
                        id: 1001,
                        volumeId: 11,
                        title: "天",
                        originalText: "天者，万物之始。",
                        translationText: "天是万物的开端。",
                        summary: "天地门条目",
                        lifecycleStatus: "PUBLISHED",
                        visibility: "PUBLIC"
                    }
                ]
            });
        }

        return apiResponse(null);
    });
};

const renderPage = (initialEntry = "/classics/sancai") => {
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
                <SancaiPage />
            </MemoryRouter>
        </QueryClientProvider>
    );
};

describe("SancaiPage", () => {
    beforeEach(() => {
        installFetchMock();
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it("renders public sancai catalog and entry detail", async () => {
        const user = userEvent.setup();
        renderPage();

        expect(await screen.findByRole("heading", { name: "三才图会" })).toBeTruthy();
        expect(await screen.findByText("13 条 / 配图 9")).toBeTruthy();
        expect((await screen.findByAltText("天图")).getAttribute("src")).toBe(
            "/kuzhambu-api/api/portal/classics/sancai/images/1001/8001/content"
        );
        await user.click(await screen.findByRole("button", { name: /天地/ }));
        await user.click(await screen.findByRole("button", { name: "卷一" }));

        const list = await screen.findByLabelText("三才图会条目列表");
        await user.click(await within(list).findByRole("button", { name: /天/ }));

        const detail = await screen.findByLabelText("三才图会条目详情");
        await waitFor(() => {
            expect(within(detail).getByText("天地门条目")).toBeTruthy();
        });
        expect(within(detail).getByText("天是万物的开端。")).toBeTruthy();
        expect(within(detail).getByText("天者，万物之始。")).toBeTruthy();
        expect(within(detail).getByText("天图视觉描述")).toBeTruthy();
        expect(within(detail).getByText("天文")).toBeTruthy();
        expect(within(detail).getByAltText("天图").getAttribute("src")).toBe(
            "/kuzhambu-api/api/portal/classics/sancai/images/1001/8001/content"
        );
        expect(within(detail).queryByText("发布状态")).toBeNull();
    });

    it("clears selected entry when changing page", async () => {
        const user = userEvent.setup();
        renderPage();

        const list = await screen.findByLabelText("三才图会条目列表");
        await user.click(await within(list).findByRole("button", { name: /天/ }));
        await screen.findByText("天地门条目");

        await user.click(screen.getByRole("button", { name: "下一页" }));

        const detail = await screen.findByLabelText("三才图会条目详情");
        await waitFor(() => {
            expect(within(detail).getByRole("heading", { name: "地" })).toBeTruthy();
        });
        expect(within(detail).getByText("地理条目")).toBeTruthy();
        expect(within(detail).getByText("地承载万物。")).toBeTruthy();
    });

    it("loads entry detail from id query parameter", async () => {
        renderPage("/classics/sancai?id=300000000003");

        const detail = await screen.findByLabelText("三才图会条目详情");
        await waitFor(() => {
            expect(within(detail).getByText("礼器图")).toBeTruthy();
        });
        expect(within(detail).getByText("搜索直达条目")).toBeTruthy();
        expect(within(detail).getByText("礼器图详情。")).toBeTruthy();
        expect(within(detail).queryByText("发布状态")).toBeNull();

        const getEntryCall = vi
            .mocked(globalThis.fetch)
            .mock.calls.find(([input]) => String(input).includes("/entries/get"));
        expect(getEntryCall).toBeTruthy();
        expect(JSON.parse(String(getEntryCall?.[1]?.body))).toMatchObject({
            id: 300000000003
        });
    });
});
