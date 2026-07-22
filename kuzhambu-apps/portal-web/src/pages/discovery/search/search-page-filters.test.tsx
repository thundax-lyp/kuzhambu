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
});
