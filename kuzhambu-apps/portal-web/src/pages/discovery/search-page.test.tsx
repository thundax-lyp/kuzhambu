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

const getPreviewButton = (container: HTMLElement, title: string) => {
    return container.querySelector(
        `button[aria-label="打开搜索预览：${title}"]`
    ) as HTMLButtonElement | null;
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

describe("DiscoverySearchPage", () => {
    afterEach(() => {
        mocks.previewSearchResult.mockReset();
        mocks.recordSearchClickEvent.mockReset();
        mocks.searchDiscovery.mockReset();
        document.body.innerHTML = "";
    });

    it("submits query and renders grouped discovery results", async () => {
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
                            permissionDebugTrace: "DO_NOT_RENDER_ITEM_TRACE",
                            resultRank: 1,
                            summary: "礼器条目摘要",
                            targetPath: "/shares/1001",
                            title: "礼器条目"
                        }
                    ]
                }
            ],
            permissionDebugTrace: "DO_NOT_RENDER_RESPONSE_TRACE",
            queryText: "礼器",
            searchEventId: "EVT-1001",
            totalCount: 1
        });

        const { container, root } = renderPage();

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
        expect(container.textContent).toContain("共 1 条命中");
        expect(container.textContent).toContain("三才图会");
        expect(container.textContent).toContain("礼器条目");
        expect(container.textContent).toContain("礼器条目摘要");
        expect(container.textContent).not.toContain("DO_NOT_RENDER_RESPONSE_TRACE");
        expect(container.textContent).not.toContain("DO_NOT_RENDER_ITEM_TRACE");

        const resultButton = getPreviewButton(container, "礼器条目");
        expect(resultButton?.textContent).toContain("礼器条目");

        await act(async () => {
            resultButton?.click();
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
        expect(mocks.previewSearchResult).toHaveBeenCalledWith({
            contentId: "1001",
            contentType: "SANCAI_ENTRY"
        });

        act(() => {
            root.unmount();
        });
    });

    it("opens a search preview sheet when a result is clicked", async () => {
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
                            targetPath: "/shares/1001",
                            title: "礼器条目"
                        }
                    ]
                }
            ],
            queryText: "礼器",
            searchEventId: "EVT-1008",
            totalCount: 1
        });
        mocks.previewSearchResult.mockResolvedValueOnce({
            bodyText: "来自 ES 索引的正文",
            categoryName: "器用",
            contentId: "1001",
            contentStatus: "PUBLISHED",
            contentType: "SANCAI_ENTRY",
            knowledgeBase: "SANCAI_ENTRY",
            summary: "索引摘要",
            tagNames: ["礼制"],
            targetPath: "/shares/1001",
            title: "礼器条目",
            visibility: "PUBLIC"
        });

        const { container, root } = renderPage("/discovery/search?q=%E7%A4%BC%E5%99%A8");
        await flushMutations();
        const resultButton = getPreviewButton(container, "礼器条目");

        await act(async () => {
            resultButton?.click();
        });
        await flushMutations();

        expect(mocks.previewSearchResult).toHaveBeenCalledWith({
            contentId: "1001",
            contentType: "SANCAI_ENTRY"
        });
        const previewDialog = document.body.querySelector('[role="dialog"]');
        expect(previewDialog?.textContent).toContain("检索预览");
        expect(previewDialog?.textContent).toContain("正文");
        expect(previewDialog?.textContent).toContain("来自 ES 索引的正文");
        expect(previewDialog?.textContent).not.toContain("来源路径");
        expect(previewDialog?.textContent).not.toContain("/shares/1001");
        expect(previewDialog?.textContent).toContain("关闭预览");

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
        const categoryInput = container.querySelector(
            'input[name="categoryCodes"]'
        ) as HTMLInputElement | null;
        expect(queryInput?.value).toBe("礼器");
        expect(categoryInput?.value).toBe("SANCAI_ENTRY");
        expect(getFilterOption(container, "知识库", "三才图会")?.getAttribute("aria-pressed")).toBe(
            "true"
        );
        expect(getFilterOption(container, "可见性", "公开内容")?.getAttribute("aria-pressed")).toBe(
            "true"
        );
        expect(mocks.searchDiscovery).toHaveBeenCalledWith({
            categoryCodes: ["SANCAI_ENTRY"],
            contentStatuses: [],
            dateFrom: null,
            dateTo: null,
            knowledgeBases: ["SANCAI_ENTRY"],
            pageNo: 2,
            pageSize: 20,
            queryText: "礼器",
            tagNames: [],
            visibilityScopes: ["PUBLIC"]
        });

        act(() => {
            root.unmount();
        });
    });

    it("syncs submitted search filters to url params without pagination", async () => {
        mocks.searchDiscovery.mockResolvedValueOnce({
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
        setInputValue(container, "categoryCodes", "WANGQI_DOCUMENT");
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
        expect(getLocation()).toContain("categoryCodes=WANGQI_DOCUMENT");
        expect(getLocation()).not.toContain("pageNo=");
        expect(getLocation()).not.toContain("pageSize=");
        expect(mocks.searchDiscovery).toHaveBeenCalledWith(
            expect.objectContaining({
                categoryCodes: ["WANGQI_DOCUMENT"],
                knowledgeBases: ["WANGQI_DOCUMENT"],
                pageNo: 2,
                pageSize: 20,
                queryText: "官制"
            })
        );

        act(() => {
            root.unmount();
        });
    });

    it("submits advanced filter controls only after an explicit search action", async () => {
        mocks.searchDiscovery.mockResolvedValueOnce({
            displayQueryText: "礼俗",
            groupCount: 0,
            groups: [],
            queryText: "礼俗",
            searchEventId: "EVT-1004",
            totalCount: 0
        });

        const { container, root } = renderPage();
        setInputValue(container, "queryText", "礼俗");
        setInputValue(container, "categoryCodes", "RITUAL, CUSTOM");
        setInputValue(container, "tagNames", "礼制、民俗");
        setInputValue(container, "dateFrom", "2026-01-02");
        setInputValue(container, "dateTo", "2026-01-31");
        await clickFilterOption(container, "知识库", "三才图会");
        await clickFilterOption(container, "知识库", "明代习俗");
        await clickFilterOption(container, "状态", "已发布");
        await clickFilterOption(container, "可见性", "非公开内容");

        expect(mocks.searchDiscovery).not.toHaveBeenCalled();

        const submitButton = container.querySelector(
            'button[type="submit"]'
        ) as HTMLButtonElement | null;

        await act(async () => {
            submitButton?.click();
        });
        await flushMutations();

        expect(mocks.searchDiscovery).toHaveBeenCalledWith({
            categoryCodes: ["RITUAL", "CUSTOM"],
            contentStatuses: ["PUBLISHED"],
            dateFrom: new Date("2026-01-02T00:00:00").toISOString(),
            dateTo: new Date("2026-01-31T23:59:59").toISOString(),
            knowledgeBases: ["SANCAI_ENTRY", "MING_CUSTOMS"],
            pageNo: 1,
            pageSize: 10,
            queryText: "礼俗",
            tagNames: ["礼制", "民俗"],
            visibilityScopes: ["PRIVATE"]
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
