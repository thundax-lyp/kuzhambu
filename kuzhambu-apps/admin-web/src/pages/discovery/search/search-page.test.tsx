import { AdminQueryProvider } from "@/query/query-client";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import { MemoryRouter, Route, Routes, useLocation } from "react-router-dom";
import { SearchPage } from "./search-page";

const mocks = vi.hoisted(() => ({
    clickSearchResult: vi.fn(async () => true),
    previewSearchResult: vi.fn(async () => ({
        bodyText: "第一段正文。\n\n第二段正文。",
        categoryName: "器用",
        contentId: "1001",
        contentType: "SANCAI_ENTRY",
        knowledgeBase: "SANCAI_ENTRY",
        summary: "索引摘要",
        tagNames: ["礼器"],
        targetPath: "/classics/sancai",
        title: "礼器图"
    })),
    searchDiscovery: vi.fn(async () => ({
        displayQueryText: "礼器",
        groupCount: 1,
        groups: [
            {
                count: 1,
                groupKey: "SANCAI_ENTRY",
                groupTitle: "三才图会",
                items: [
                    {
                        contentDomain: "classics",
                        contentId: "1001",
                        contentType: "SANCAI_ENTRY",
                        groupRank: 1,
                        highlightText: "命中 <mark>礼器</mark> 内容",
                        resultRank: 1,
                        targetPath: "/classics/sancai",
                        title: "礼器图"
                    }
                ]
            }
        ],
        queryText: "礼器",
        id: "EVT-1001",
        totalCount: 1
    }))
}));

vi.mock("./search-service", () => mocks);

const LocationProbe = () => {
    const location = useLocation();
    return <output data-testid="location-search">{location.search}</output>;
};

const renderPage = (initialEntry = "/discovery/search") => {
    return render(
        <AdminQueryProvider>
            <AntdApp>
                <MemoryRouter initialEntries={[initialEntry]}>
                    <Routes>
                        <Route
                            path="/discovery/search"
                            element={
                                <>
                                    <SearchPage />
                                    <LocationProbe />
                                </>
                            }
                        />
                        <Route path="/classics/sancai" element={<div>三才详情</div>} />
                    </Routes>
                </MemoryRouter>
            </AntdApp>
        </AdminQueryProvider>
    );
};

describe("SearchPage", () => {
    beforeEach(() => {
        mocks.clickSearchResult.mockClear();
        mocks.previewSearchResult.mockClear();
        mocks.searchDiscovery.mockClear();
    });

    afterEach(() => {
        cleanup();
        vi.restoreAllMocks();
    });

    it("renders search shell", () => {
        renderPage();

        expect(screen.getByRole("heading", { name: "检索" })).toBeInTheDocument();
        expect(screen.getByLabelText("搜索内容")).toBeInTheDocument();
        expect(screen.getByText(/公开已发布内容/u)).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "搜索" })).toBeInTheDocument();
    });

    it("loads publication-ready search on initial open", async () => {
        renderPage();

        await waitFor(() => {
            expect(mocks.searchDiscovery).toHaveBeenCalledWith(
                expect.objectContaining({ queryText: "" })
            );
        });
    });

    it("submits publication-ready search request and renders grouped results", async () => {
        const user = userEvent.setup();
        renderPage();

        await user.type(screen.getByLabelText("搜索内容"), "礼器");
        await user.click(screen.getByRole("button", { name: "搜索" }));

        await waitFor(() => {
            expect(mocks.searchDiscovery).toHaveBeenCalledWith(
                expect.objectContaining({ queryText: "礼器" })
            );
        });
        expect(await screen.findByText("三才图会")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "打开搜索预览：礼器图" })).toBeInTheDocument();
        expect(screen.queryByRole("link", { name: /礼器图/u })).not.toBeInTheDocument();
        expect(screen.getAllByText("礼器").length).toBeGreaterThanOrEqual(2);
        expect(screen.getByLabelText("检索结果")).toBeInTheDocument();
    });

    it("restores query string and records result clicks", async () => {
        const user = userEvent.setup();
        renderPage("/discovery/search?q=礼器&knowledgeBases=SANCAI_ENTRY&pageNo=1&pageSize=10");

        const result = await screen.findByRole("button", { name: "打开搜索预览：礼器图" });
        await user.click(result);

        expect(mocks.clickSearchResult).toHaveBeenCalledWith(
            {
                contentDomain: "classics",
                contentId: "1001",
                contentTitle: "礼器图",
                contentType: "SANCAI_ENTRY",
                groupRank: 1,
                resultGroupKey: "SANCAI_ENTRY",
                resultRank: 1,
                searchEventId: "EVT-1001",
                targetPath: "/classics/sancai"
            },
            expect.anything()
        );
        expect(mocks.previewSearchResult).toHaveBeenCalledWith({
            contentId: "1001",
            contentType: "SANCAI_ENTRY"
        });
        expect(await screen.findByText("第一段正文。")).toBeInTheDocument();
        expect(screen.getByText("第二段正文。")).toBeInTheDocument();
        expect(screen.getByText("正文")).toBeInTheDocument();
        expect(screen.getAllByText("知识库").length).toBeGreaterThan(0);
        expect(screen.getAllByText("三才图会").length).toBeGreaterThan(0);
        expect(screen.queryByText("来源路径")).not.toBeInTheDocument();
        expect(screen.queryByText("可见性")).not.toBeInTheDocument();
        expect(screen.queryByText("状态")).not.toBeInTheDocument();
        expect(screen.queryByText("/classics/sancai")).not.toBeInTheDocument();
    });

    it("persists pagination in the URL and restores it", async () => {
        mocks.searchDiscovery.mockResolvedValueOnce({
            displayQueryText: "礼器",
            groupCount: 0,
            groups: [],
            id: "EVT-2001",
            queryText: "礼器",
            totalCount: 25
        });
        const user = userEvent.setup();
        renderPage("/discovery/search?q=礼器&pageNo=2&pageSize=20");

        await waitFor(() => {
            expect(mocks.searchDiscovery).toHaveBeenCalledWith(
                expect.objectContaining({ pageNo: 2, pageSize: 20, queryText: "礼器" })
            );
        });
        expect(screen.getByTestId("location-search")).toHaveTextContent(
            "?q=礼器&pageNo=2&pageSize=20"
        );

        await user.click(screen.getByRole("button", { name: "搜索" }));
        await waitFor(() => {
            expect(screen.getByTestId("location-search")).toHaveTextContent(
                "?q=%E7%A4%BC%E5%99%A8&pageNo=1&pageSize=20"
            );
        });
    });

    it("keeps preview usable when click recording fails", async () => {
        mocks.clickSearchResult.mockRejectedValueOnce(new Error("tracking unavailable"));
        const user = userEvent.setup();
        renderPage("/discovery/search?q=礼器&pageNo=1&pageSize=10");

        await user.click(await screen.findByRole("button", { name: "打开搜索预览：礼器图" }));

        expect(await screen.findByText("第一段正文。")).toBeInTheDocument();
        expect(screen.getByTestId("discovery-search-preview-drawer")).toBeInTheDocument();
    });
});
