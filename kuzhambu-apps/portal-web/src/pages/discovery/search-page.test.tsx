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

const renderPage = (initialEntry = "/discovery/search") => {
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
                <MemoryRouter initialEntries={[initialEntry]}>
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

const clickFilterOption = async (
    container: HTMLElement,
    groupLabel: string,
    optionText: string
) => {
    const group = container.querySelector(
        `[role="group"][aria-label="${groupLabel}"]`
    ) as HTMLElement | null;
    expect(group).not.toBeNull();

    const button = Array.from(group?.querySelectorAll("button") ?? []).find(
        (candidate) => candidate.textContent === optionText
    );
    expect(button).toBeDefined();

    await act(async () => {
        button?.click();
    });
};

const getFilterOption = (container: HTMLElement, groupLabel: string, optionText: string) => {
    const group = container.querySelector(
        `[role="group"][aria-label="${groupLabel}"]`
    ) as HTMLElement | null;

    return Array.from(group?.querySelectorAll("button") ?? []).find(
        (candidate) => candidate.textContent === optionText
    );
};

const emptySearchResponse = {
    displayQueryText: "",
    groupCount: 0,
    groups: [],
    queryText: "",
    searchEventId: "EVT-EMPTY",
    totalCount: 0
};

const clickButtonByText = async (container: HTMLElement, buttonText: string) => {
    const button = Array.from(container.querySelectorAll("button")).find(
        (candidate) => candidate.textContent === buttonText
    );
    expect(button).toBeDefined();

    await act(async () => {
        button?.click();
    });
};

describe("DiscoverySearchPage", () => {
    afterEach(() => {
        mocks.previewSearchResult.mockReset();
        mocks.recordSearchClickEvent.mockReset();
        mocks.searchDiscovery.mockReset();
        document.body.innerHTML = "";
    });

    it("requests an empty query when the page opens without q", async () => {
        mocks.searchDiscovery.mockResolvedValueOnce(emptySearchResponse);

        const { container, root } = renderPage();
        await flushMutations();

        expect(mocks.searchDiscovery).toHaveBeenCalledWith({
            categoryCodes: [],
            contentStatuses: [],
            dateFrom: null,
            dateTo: null,
            knowledgeBases: [],
            pageNo: 1,
            pageSize: 10,
            queryText: "",
            tagNames: [],
            visibilityScopes: []
        });
        expect(container.textContent).toContain("共 0 条命中");
        expect(container.textContent).not.toContain("等待检索");

        act(() => {
            root.unmount();
        });
    });

    it("does not blame search conditions when the search request fails", async () => {
        mocks.searchDiscovery.mockRejectedValueOnce(new Error("network down"));

        const { container, root } = renderPage();
        await flushMutations();

        expect(container.textContent).toContain("检索暂时不可用");
        expect(container.textContent).not.toContain("检查输入条件");
        expect(container.textContent).not.toContain("调整条件");

        act(() => {
            root.unmount();
        });
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
            permissionDebugTrace: "DO_NOT_RENDER_RESPONSE_TRACE",
            queryText: "礼器",
            searchEventId: "EVT-1001",
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
            queryText: "礼器",
            searchEventId: "EVT-1008",
            totalCount: 1
        });

        const { container, getLocation, root } = renderPage(
            "/discovery/search?q=%E7%A4%BC%E5%99%A8"
        );
        await flushMutations();
        const resultLink = getResultLink(container, "礼器条目");
        expect(resultLink?.getAttribute("href")).toBe(
            "/discovery/search-item?type=SANCAI_ENTRY&id=1001"
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
            queryText: "王圻",
            searchEventId: "EVT-1009",
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
            queryText: "节令",
            searchEventId: "EVT-1010",
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

    it("restores search form from url state and searches automatically", async () => {
        mocks.searchDiscovery.mockResolvedValueOnce({
            displayQueryText: "礼器",
            groupCount: 0,
            groups: [],
            queryText: "礼器",
            searchEventId: "EVT-1002",
            totalCount: 0
        });

        const { container, root } = renderPage(
            "/discovery/search?q=%E7%A4%BC%E5%99%A8&knowledgeBases=SANCAI_ENTRY&categoryCodes=SANCAI_ENTRY&visibilityScopes=PUBLIC&pageNo=2&pageSize=20"
        );
        await flushMutations();

        const queryInput = container.querySelector(
            'input[name="queryText"]'
        ) as HTMLInputElement | null;
        expect(queryInput?.value).toBe("礼器");
        expect(getFilterOption(container, "知识库", "三才图会")?.getAttribute("aria-pressed")).toBe(
            "true"
        );
        expect(container.querySelector('input[name="categoryCodes"]')).toBeNull();
        expect(container.textContent).not.toContain("可见性");
        expect(mocks.searchDiscovery).toHaveBeenCalledWith({
            categoryCodes: [],
            contentStatuses: [],
            dateFrom: null,
            dateTo: null,
            knowledgeBases: ["SANCAI_ENTRY"],
            pageNo: 2,
            pageSize: 20,
            queryText: "礼器",
            tagNames: [],
            visibilityScopes: []
        });

        act(() => {
            root.unmount();
        });
    });

    it("syncs submitted search filters and pagination to url params", async () => {
        mocks.searchDiscovery.mockResolvedValueOnce(emptySearchResponse).mockResolvedValueOnce({
            displayQueryText: "官制",
            groupCount: 0,
            groups: [],
            queryText: "官制",
            searchEventId: "EVT-1003",
            totalCount: 0
        });

        const { container, getLocation, root } = renderPage(
            "/discovery/search?pageNo=2&pageSize=20"
        );
        setInputValue(container, "queryText", "官制");
        await clickButtonByText(container, "高级筛选");
        await clickFilterOption(container, "知识库", "王圻文档");

        const submitButton = container.querySelector(
            'button[type="submit"]'
        ) as HTMLButtonElement | null;

        await act(async () => {
            submitButton?.click();
        });
        await flushMutations();

        expect(getLocation()).toContain("q=%E5%AE%98%E5%88%B6");
        expect(getLocation()).toContain("knowledgeBases=WANGQI_DOCUMENT");
        expect(getLocation()).not.toContain("pageNo=2");
        expect(getLocation()).toContain("pageSize=20");
        expect(getLocation()).not.toContain("categoryCodes=");
        expect(mocks.searchDiscovery).toHaveBeenCalledWith(
            expect.objectContaining({
                categoryCodes: [],
                knowledgeBases: ["WANGQI_DOCUMENT"],
                pageNo: 1,
                pageSize: 20,
                queryText: "官制"
            })
        );

        act(() => {
            root.unmount();
        });
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
                queryText: "",
                searchEventId: "EVT-PAGE-1",
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
                queryText: "",
                searchEventId: "EVT-PAGE-2",
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

    it("submits advanced filter controls only after an explicit search action", async () => {
        mocks.searchDiscovery.mockResolvedValueOnce(emptySearchResponse).mockResolvedValueOnce({
            displayQueryText: "礼俗",
            groupCount: 0,
            groups: [],
            queryText: "礼俗",
            searchEventId: "EVT-1004",
            totalCount: 0
        });

        const { container, root } = renderPage();
        setInputValue(container, "queryText", "礼俗");
        await clickButtonByText(container, "高级筛选");
        setInputValue(container, "dateFrom", "2026-01-02");
        setInputValue(container, "dateTo", "2026-01-31");
        await clickFilterOption(container, "知识库", "三才图会");
        await clickFilterOption(container, "知识库", "明代习俗");

        expect(mocks.searchDiscovery).toHaveBeenCalledTimes(1);

        const submitButton = container.querySelector(
            'button[type="submit"]'
        ) as HTMLButtonElement | null;

        await act(async () => {
            submitButton?.click();
        });
        await flushMutations();

        expect(mocks.searchDiscovery).toHaveBeenCalledWith({
            categoryCodes: [],
            contentStatuses: [],
            dateFrom: new Date("2026-01-02T00:00:00").toISOString(),
            dateTo: new Date("2026-01-31T23:59:59").toISOString(),
            knowledgeBases: ["SANCAI_ENTRY", "MING_CUSTOMS"],
            pageNo: 1,
            pageSize: 10,
            queryText: "礼俗",
            tagNames: [],
            visibilityScopes: []
        });

        act(() => {
            root.unmount();
        });
    });

    it("renders whitelisted highlight marks without parsing arbitrary html", async () => {
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
                            contentId: "1002",
                            contentType: "SANCAI_ENTRY",
                            groupRank: 1,
                            highlightText: "命中 <mark>礼器</mark> <script>alert(1)</script>",
                            resultRank: 1,
                            summary: "普通摘要",
                            title: "礼器条目"
                        }
                    ]
                }
            ],
            queryText: "礼器",
            searchEventId: "EVT-1005",
            totalCount: 1
        });

        const { container, root } = renderPage("/discovery/search?q=%E7%A4%BC%E5%99%A8");
        await flushMutations();

        expect(container.querySelector("mark")?.textContent).toBe("礼器");
        expect(container.querySelector("script")).toBeNull();
        expect(container.textContent).toContain("<script>alert(1)</script>");

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
                queryText: "官制",
                searchEventId: "EVT-1006",
                totalCount: 0
            })
            .mockResolvedValueOnce({
                displayQueryText: "官制",
                groupCount: 0,
                groups: [],
                queryText: "官制",
                searchEventId: "EVT-1007",
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
