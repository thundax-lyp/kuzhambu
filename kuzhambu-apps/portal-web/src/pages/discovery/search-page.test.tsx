import { createRoot } from "react-dom/client";
import { act } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter, useLocation } from "react-router-dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import { DiscoverySearchPage } from "./search-page";

const mocks = vi.hoisted(() => ({
    recordSearchClick: vi.fn(),
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

describe("DiscoverySearchPage", () => {
    afterEach(() => {
        mocks.recordSearchClick.mockReset();
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
                            resultRank: 1,
                            summary: "礼器条目摘要",
                            targetPath: "/shares/1001",
                            title: "礼器条目"
                        }
                    ]
                }
            ],
            queryText: "礼器",
            searchLogId: "LOG-1001",
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

        const resultLink = container.querySelector(
            'a[href="/shares/1001"]'
        ) as HTMLAnchorElement | null;
        expect(resultLink?.textContent).toContain("礼器条目");

        await act(async () => {
            resultLink?.click();
        });

        expect(mocks.recordSearchClick).toHaveBeenCalledWith({
            contentDomain: "CLASSICS",
            contentId: "1001",
            contentTitle: "礼器条目",
            contentType: "SANCAI_ENTRY",
            groupRank: 1,
            resultGroupKey: "SANCAI_ENTRY",
            resultRank: 1,
            searchLogId: "LOG-1001",
            targetPath: "/shares/1001"
        });

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
            searchLogId: "LOG-1002",
            totalCount: 0
        });

        const { container, root } = renderPage(
            "/discovery/search?q=%E7%A4%BC%E5%99%A8&categoryCodes=SANCAI_ENTRY&pageNo=2&pageSize=20"
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
        expect(mocks.searchDiscovery).toHaveBeenCalledWith({
            categoryCodes: ["SANCAI_ENTRY"],
            contentStatuses: [],
            dateFrom: null,
            dateTo: null,
            knowledgeBases: [],
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

    it("syncs submitted search state to url params", async () => {
        mocks.searchDiscovery.mockResolvedValueOnce({
            displayQueryText: "官制",
            groupCount: 0,
            groups: [],
            queryText: "官制",
            searchLogId: "LOG-1003",
            totalCount: 0
        });

        const { container, getLocation, root } = renderPage();
        setInputValue(container, "queryText", "官制");
        setInputValue(container, "categoryCodes", "WANGQI_DOCUMENT");

        const submitButton = container.querySelector(
            'button[type="submit"]'
        ) as HTMLButtonElement | null;

        await act(async () => {
            submitButton?.click();
        });
        await flushMutations();

        expect(getLocation()).toContain("q=%E5%AE%98%E5%88%B6");
        expect(getLocation()).toContain("categoryCodes=WANGQI_DOCUMENT");
        expect(mocks.searchDiscovery).toHaveBeenCalledWith(
            expect.objectContaining({
                categoryCodes: ["WANGQI_DOCUMENT"],
                queryText: "官制"
            })
        );

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
            searchLogId: "LOG-1004",
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
                searchLogId: "LOG-1005",
                totalCount: 0
            })
            .mockResolvedValueOnce({
                displayQueryText: "官制",
                groupCount: 0,
                groups: [],
                queryText: "官制",
                searchLogId: "LOG-1006",
                totalCount: 0
            });

        const { container, getLocation, root } = renderPage(
            "/discovery/search?q=%E5%AE%98%E5%88%B6&categoryCodes=WANGQI_DOCUMENT&tagNames=%E5%88%B6%E5%BA%A6"
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
                queryText: "官制",
                tagNames: []
            })
        );
        expect(getLocation()).toContain("q=%E5%AE%98%E5%88%B6");
        expect(getLocation()).not.toContain("categoryCodes");
        expect(getLocation()).not.toContain("tagNames");

        act(() => {
            root.unmount();
        });
    });
});
