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
            if (entryId === 1002) {
                return apiResponse({
                    id: 1002,
                    volumeId: 11,
                    title: "地",
                    originalText: "地者，万物之成。",
                    translationText: "地承载万物。",
                    summary: "地理条目",
                    lifecycleStatus: "PUBLISHED",
                    visibility: "PUBLIC"
                });
            }
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
            if (body.categoryId === 1) {
                return apiResponse({
                    count: 1,
                    pageNo: 1,
                    pageSize: 100,
                    totalCount: 1,
                    totalPage: 1,
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
            if (body.pageNo === 2) {
                return apiResponse({
                    count: 2,
                    pageNo: 2,
                    pageSize: 100,
                    totalCount: 2,
                    totalPage: 2,
                    records: [
                        {
                            id: 1003,
                            volumeId: 11,
                            title: "人",
                            originalText: "人者，万物之灵。",
                            translationText: "人是万物之灵。",
                            summary: "人物条目",
                            lifecycleStatus: "PUBLISHED",
                            visibility: "PUBLIC"
                        }
                    ]
                });
            }
            return apiResponse({
                count: 2,
                pageNo: 1,
                pageSize: 100,
                totalCount: 2,
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
        expect(await screen.findByText("古籍阅览")).toBeTruthy();
        expect(await screen.findByText("总目")).toBeTruthy();
        expect(screen.queryByText("全部公开条目")).toBeNull();
        expect(screen.queryByText("分目")).toBeNull();
        expect(screen.queryByText("13 条 / 配图 9")).toBeNull();
        expect(screen.queryByRole("button", { name: "全部卷目" })).toBeNull();
        expect(screen.queryByRole("button", { name: "下一页" })).toBeNull();

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
        expect(within(detail).getByAltText("天图像").getAttribute("src")).toBe(
            "/kuzhambu-api/api/portal/classics/sancai/visual-assets/1001/9001/generated-content"
        );
        expect(within(detail).getByLabelText("三才图会正文图版")).toBeTruthy();
        expect(within(detail).getByText("天图")).toBeTruthy();
        expect(within(detail).getByText("融合图像")).toBeTruthy();
        expect(within(detail).queryByText("发布状态")).toBeNull();
    });

    it("clears selected entry when changing catalog scope", async () => {
        const user = userEvent.setup();
        renderPage();

        const list = await screen.findByLabelText("三才图会条目列表");
        await user.click(await within(list).findByRole("button", { name: /天/ }));
        await screen.findByText("天地门条目");

        await user.click(await screen.findByRole("button", { name: /天地/ }));

        const detail = await screen.findByLabelText("三才图会条目详情");
        await waitFor(() => {
            expect(within(detail).getByRole("heading", { name: "地" })).toBeTruthy();
        });
        expect(within(detail).getByText("地理条目")).toBeTruthy();
        expect(within(detail).getByText("地承载万物。")).toBeTruthy();

        const categoryEntryPageCall = vi
            .mocked(globalThis.fetch)
            .mock.calls.find(
                ([input, init]) =>
                    String(input).includes("/entries/page") &&
                    JSON.parse(String(init?.body)).categoryId === 1
            );
        expect(categoryEntryPageCall).toBeTruthy();
    });

    it("loads additional entry pages from the catalog list", async () => {
        const user = userEvent.setup();
        renderPage();

        const list = await screen.findByLabelText("三才图会条目列表");
        const moreButton = await within(list).findByRole("button", {
            name: /加载更多条目（1 \/ 2）/
        });

        await user.click(moreButton);

        expect(await within(list).findByRole("button", { name: /人/ })).toBeTruthy();
        await waitFor(() => {
            expect(within(list).queryByRole("button", { name: /加载更多条目/ })).toBeNull();
        });

        const nextPageCall = vi
            .mocked(globalThis.fetch)
            .mock.calls.find(
                ([input, init]) =>
                    String(input).includes("/entries/page") &&
                    JSON.parse(String(init?.body)).pageNo === 2
            );
        expect(nextPageCall).toBeTruthy();
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
