import { createRoot } from "react-dom/client";
import { act } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter, useLocation } from "react-router-dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import { DiscoverySearchPage } from "./search-page";

const mocks = vi.hoisted(() => ({
    previewSearchResult: vi.fn(),
    recordSearchClickEvent: vi.fn(),
    searchDiscovery: vi.fn()
}));

vi.mock("./search-service", () => mocks);

const LocationProbe = ({ onChange }: { onChange: (value: string) => void }) => {
    const location = useLocation();
    onChange(`${location.pathname}${location.search}`);

    return null;
};

const renderPage = (initialEntry = "/discovery/search", basename?: string) => {
    const container = document.createElement("div");
    document.body.appendChild(container);
    const locations: string[] = [];

    const queryClient = new QueryClient({
        defaultOptions: {
            mutations: {
                retry: false
            },
            queries: {
                retry: false
            }
        }
    });
    const root = createRoot(container);

    act(() => {
        root.render(
            <QueryClientProvider client={queryClient}>
                <MemoryRouter basename={basename} initialEntries={[initialEntry]}>
                    <DiscoverySearchPage />
                    <LocationProbe onChange={(value) => locations.push(value)} />
                </MemoryRouter>
            </QueryClientProvider>
        );
    });

    return { container, getLocation: () => locations[locations.length - 1], root };
};

const flushMutations = async () => {
    await act(async () => {
        await Promise.resolve();
        await new Promise((resolve) => {
            window.setTimeout(resolve, 0);
        });
        await Promise.resolve();
    });
};

describe("DiscoverySearchPage", () => {
    afterEach(() => {
        mocks.previewSearchResult.mockReset();
        mocks.recordSearchClickEvent.mockReset();
        mocks.searchDiscovery.mockReset();
        document.body.innerHTML = "";
    });

    it("changes result pages through the pagination component", async () => {
        mocks.searchDiscovery
            .mockResolvedValueOnce({
                displayQueryText: "",
                groupCount: 1,
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
                                resultRank: 1,
                                summary: "第一页摘要",
                                title: "第一页结果"
                            }
                        ]
                    }
                ],
                id: "EVT-PAGE-1",
                queryText: "",
                totalCount: 21
            })
            .mockResolvedValueOnce({
                displayQueryText: "",
                groupCount: 1,
                groups: [
                    {
                        count: 1,
                        groupKey: "SANCAI_ENTRY",
                        groupTitle: "三才图会",
                        items: [
                            {
                                contentDomain: "CLASSICS",
                                contentId: "1011",
                                contentType: "SANCAI_ENTRY",
                                groupRank: 1,
                                resultRank: 11,
                                summary: "第二页摘要",
                                title: "第二页结果"
                            }
                        ]
                    }
                ],
                id: "EVT-PAGE-2",
                queryText: "",
                totalCount: 21
            });

        const { container, getLocation, root } = renderPage();
        await flushMutations();

        const nextPageLink = container.querySelector(
            'a[aria-label="下一页"]'
        ) as HTMLAnchorElement | null;
        expect(nextPageLink).not.toBeNull();

        await act(async () => {
            nextPageLink?.click();
        });
        await flushMutations();

        expect(mocks.searchDiscovery).toHaveBeenLastCalledWith(
            expect.objectContaining({
                pageNo: 2,
                pageSize: 10,
                queryText: ""
            })
        );
        expect(getLocation()).toContain("pageNo=2");
        expect(container.textContent).toContain("第二页结果");

        act(() => {
            root.unmount();
        });
    });

    it("shows zero result state and clears filters while keeping query text", async () => {
        mocks.searchDiscovery
            .mockResolvedValueOnce({
                displayQueryText: "官制",
                groupCount: 0,
                groups: [],
                id: "EVT-1006",
                queryText: "官制",
                totalCount: 0
            })
            .mockResolvedValueOnce({
                displayQueryText: "官制",
                groupCount: 0,
                groups: [],
                id: "EVT-1007",
                queryText: "官制",
                totalCount: 0
            });

        const { container, getLocation, root } = renderPage(
            "/discovery/search?q=%E5%AE%98%E5%88%B6&knowledgeBases=WANGQI_DOCUMENT&categoryCodes=WANGQI_DOCUMENT&tagNames=%E5%88%B6%E5%BA%A6&visibilityScopes=PRIVATE"
        );
        await flushMutations();

        expect(container.textContent).toContain("没有找到匹配内容");

        const clearButton = Array.from(container.querySelectorAll("button")).find(
            (button) => button.textContent === "清除筛选条件"
        );
        expect(clearButton).toBeDefined();

        await act(async () => {
            clearButton?.click();
        });
        await flushMutations();

        expect(mocks.searchDiscovery).toHaveBeenLastCalledWith(
            expect.objectContaining({
                categoryCodes: [],
                knowledgeBases: [],
                queryText: "官制",
                tagNames: [],
                visibilityScopes: []
            })
        );
        expect(getLocation()).toContain("q=%E5%AE%98%E5%88%B6");
        expect(getLocation()).not.toContain("knowledgeBases");
        expect(getLocation()).not.toContain("categoryCodes");
        expect(getLocation()).not.toContain("tagNames");
        expect(getLocation()).not.toContain("visibilityScopes");

        act(() => {
            root.unmount();
        });
    });
});
