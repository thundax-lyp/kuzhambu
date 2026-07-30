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
            id: "EVT-1005",
            queryText: "礼器",
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
});
