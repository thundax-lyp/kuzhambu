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

const setInputValue = (container: HTMLElement, name: string, value: string) => {
    const input = container.querySelector(`input[name="${name}"]`) as HTMLInputElement | null;
    expect(input).not.toBeNull();

    act(() => {
        if (!input) {
            return;
        }

        const valueSetter = Object.getOwnPropertyDescriptor(
            window.HTMLInputElement.prototype,
            "value"
        )?.set;
        valueSetter?.call(input, value);
        input.dispatchEvent(new Event("input", { bubbles: true }));
    });
};

const getResultLink = (container: HTMLElement, title: string) => {
    return container.querySelector(
        `a[aria-label="打开搜索结果：${title}"]`
    ) as HTMLAnchorElement | null;
};

const emptySearchResponse = {
    displayQueryText: "",
    groupCount: 0,
    groups: [],
    id: "EVT-EMPTY",
    queryText: "",
    totalCount: 0
};

describe("DiscoverySearchPage", () => {
    afterEach(() => {
        mocks.previewSearchResult.mockReset();
        mocks.recordSearchClickEvent.mockReset();
        mocks.searchDiscovery.mockReset();
        document.body.innerHTML = "";
    });

    it("submits query and renders ordered discovery results without internal codes", async () => {
        mocks.searchDiscovery.mockResolvedValueOnce(emptySearchResponse).mockResolvedValueOnce({
            displayQueryText: "礼器",
            groupCount: 2,
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
                            permissionDebugTrace: "DO_NOT_RENDER_ITEM_TRACE",
                            resultRank: 1,
                            summary: "礼器条目摘要",
                            targetPath: "/shares/1001",
                            title: "礼器条目"
                        }
                    ]
                },
                {
                    count: 1,
                    groupKey: "WANGQI_DOCUMENT",
                    groupTitle: "王圻文档",
                    items: [
                        {
                            contentDomain: "CLASSICS",
                            contentId: "1002",
                            contentType: "WANGQI_DOCUMENT",
                            groupRank: 1,
                            resultRank: 2,
                            summary: "王圻文档中的礼器线索",
                            targetPath: "/shares/1002",
                            title: "王圻礼器"
                        }
                    ]
                }
            ],
            id: "EVT-1001",
            permissionDebugTrace: "DO_NOT_RENDER_RESPONSE_TRACE",
            queryText: "礼器",
            totalCount: 2
        });

        const { container, getLocation, root } = renderPage();
        await flushMutations();

        setInputValue(container, "queryText", "礼器");

        const submitButton = container.querySelector(
            'button[type="submit"]'
        ) as HTMLButtonElement | null;
        expect(submitButton).not.toBeNull();

        await act(async () => {
            submitButton?.click();
        });
        await flushMutations();

        expect(mocks.searchDiscovery).toHaveBeenCalledWith({
            categoryCodes: [],
            contentStatuses: [],
            dateFrom: null,
            dateTo: null,
            knowledgeBases: [],
            pageNo: 1,
            pageSize: 10,
            queryText: "礼器",
            tagNames: [],
            visibilityScopes: []
        });
        expect(container.textContent).toContain("共 2 条命中");
        expect(container.textContent).toContain("三才图会");
        expect(container.textContent).toContain("王圻文档");
        expect(container.textContent).toContain("礼器条目");
        expect(container.textContent).toContain("礼器条目摘要");
        expect(container.textContent?.indexOf("礼器条目")).toBeLessThan(
            container.textContent?.indexOf("王圻礼器") ?? Number.POSITIVE_INFINITY
        );
        expect(container.querySelector("mark")?.textContent).toBe("礼器");
        expect(container.textContent).not.toContain("EVT-1001");
        expect(container.textContent).not.toContain("回显词");
        expect(container.textContent).not.toContain("全局");
        expect(container.textContent).not.toContain("组内");
        expect(container.textContent).not.toContain("CLASSICS");
        expect(container.textContent).not.toContain("SANCAI_ENTRY");
        expect(container.textContent).not.toContain("WANGQI_DOCUMENT");
        expect(container.textContent).not.toContain("DO_NOT_RENDER_RESPONSE_TRACE");
        expect(container.textContent).not.toContain("DO_NOT_RENDER_ITEM_TRACE");

        const resultLink = getResultLink(container, "礼器条目");
        expect(resultLink?.textContent).toContain("礼器条目");
        expect(resultLink?.getAttribute("target")).toBe("_blank");
        expect(resultLink?.getAttribute("href")).toBe(
            "/discovery/search-item?type=SANCAI_ENTRY&id=1001"
        );

        await act(async () => {
            resultLink?.click();
        });

        expect(mocks.recordSearchClickEvent).toHaveBeenCalledWith({
            contentDomain: "CLASSICS",
            contentId: "1001",
            contentTitle: "礼器条目",
            contentType: "SANCAI_ENTRY",
            groupRank: 1,
            resultGroupKey: "SANCAI_ENTRY",
            resultRank: 1,
            searchEventId: "EVT-1001",
            targetPath: "/shares/1001"
        });
        expect(mocks.previewSearchResult).not.toHaveBeenCalled();
        expect(getLocation()).toBe("/discovery/search?q=%E7%A4%BC%E5%99%A8");

        act(() => {
            root.unmount();
        });
    });

    it("opens the unified item page in a new tab when a result is clicked", async () => {
        mocks.searchDiscovery.mockResolvedValueOnce({
            displayQueryText: "礼器",
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
                            highlightText: "礼器条目摘要",
                            resultRank: 1,
                            summary: "礼器条目摘要",
                            targetPath: "javascript:alert(1)",
                            title: "礼器条目"
                        }
                    ]
                }
            ],
            id: "EVT-1008",
            queryText: "礼器",
            totalCount: 1
        });

        const { container, getLocation, root } = renderPage(
            "/kuzhambu/discovery/search?q=%E7%A4%BC%E5%99%A8",
            "/kuzhambu"
        );
        await flushMutations();
        const resultLink = getResultLink(container, "礼器条目");
        expect(resultLink?.getAttribute("href")).toBe(
            "/kuzhambu/discovery/search-item?type=SANCAI_ENTRY&id=1001"
        );
        expect(resultLink?.getAttribute("target")).toBe("_blank");
        expect(resultLink?.getAttribute("rel")).toBe("noreferrer");

        await act(async () => {
            resultLink?.click();
        });
        await flushMutations();

        expect(getLocation()).toBe("/discovery/search?q=%E7%A4%BC%E5%99%A8");
        expect(mocks.previewSearchResult).not.toHaveBeenCalled();
        const previewDialog = document.body.querySelector('[role="dialog"]');
        expect(previewDialog).toBeNull();

        act(() => {
            root.unmount();
        });
    });

    it("normalizes Wangqi document result clicks into the unified item page", async () => {
        mocks.searchDiscovery.mockResolvedValueOnce({
            displayQueryText: "王圻",
            groupCount: 1,
            groups: [
                {
                    count: 1,
                    groupKey: "WANGQI_DOCUMENT",
                    groupTitle: "王圻文档",
                    items: [
                        {
                            contentDomain: "CLASSICS",
                            contentId: "5",
                            contentType: "WANGQI_DOCUMENT",
                            groupRank: 1,
                            highlightText: "王圻文档摘要",
                            resultRank: 1,
                            summary: "王圻文档摘要",
                            targetPath: "/classics/wangqi/5",
                            title: "王圻文档"
                        }
                    ]
                }
            ],
            id: "EVT-1009",
            queryText: "王圻",
            totalCount: 1
        });

        const { container, getLocation, root } = renderPage(
            "/discovery/search?q=%E7%8E%8B%E5%9C%BB"
        );
        await flushMutations();
        const resultLink = getResultLink(container, "王圻文档");
        expect(resultLink?.getAttribute("href")).toBe(
            "/discovery/search-item?type=WANGQI_DOCUMENT&id=5"
        );

        await act(async () => {
            resultLink?.click();
        });
        await flushMutations();

        expect(getLocation()).toBe("/discovery/search?q=%E7%8E%8B%E5%9C%BB");

        act(() => {
            root.unmount();
        });
    });

    it("normalizes Ming customs result clicks into the unified item page", async () => {
        mocks.searchDiscovery.mockResolvedValueOnce({
            displayQueryText: "节令",
            groupCount: 1,
            groups: [
                {
                    count: 1,
                    groupKey: "MING_CUSTOMS",
                    groupTitle: "明代习俗",
                    items: [
                        {
                            contentDomain: "CLASSICS",
                            contentId: "3001",
                            contentType: "MING_CUSTOMS",
                            groupRank: 1,
                            highlightText: "明代习俗摘要",
                            resultRank: 1,
                            summary: "明代习俗摘要",
                            targetPath: "/classics/ming-customs/3001",
                            title: "元旦朝贺"
                        }
                    ]
                }
            ],
            id: "EVT-1010",
            queryText: "节令",
            totalCount: 1
        });

        const { container, getLocation, root } = renderPage(
            "/discovery/search?q=%E8%8A%82%E4%BB%A4"
        );
        await flushMutations();
        const resultLink = getResultLink(container, "元旦朝贺");
        expect(resultLink?.getAttribute("href")).toBe(
            "/discovery/search-item?type=MING_CUSTOMS&id=3001"
        );

        await act(async () => {
            resultLink?.click();
        });
        await flushMutations();

        expect(getLocation()).toBe("/discovery/search?q=%E8%8A%82%E4%BB%A4");

        act(() => {
            root.unmount();
        });
    });
});
