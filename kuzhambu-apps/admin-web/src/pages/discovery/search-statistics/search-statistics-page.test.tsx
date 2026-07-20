import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import { queryClient } from "@/query/query-client";
import { SearchStatisticsPage } from "./search-statistics-page";

const mocks = vi.hoisted(() => ({
    getSearchStatisticsSummary: vi.fn(async () => ({
        clickCount: 7,
        failedSearchCount: 2,
        searchCount: 12,
        topQueries: [{ count: 5, queryText: "礼器" }],
        zeroResultSearchCount: 3
    })),
    getSearchEventDetail: vi.fn(async () => null),
    pageSearchEvents: vi.fn(async () => ({
        pageNo: 1,
        pageSize: 10,
        count: 1,
        totalCount: 1,
        totalPage: 1,
        records: [
            {
                searchEventId: "EVT-1001",
                queryText: "礼器",
                displayQueryText: "礼器",
                intentType: "REWRITE",
                resultTotalCount: 1,
                groupTotalCount: 1,
                searchStatus: "SUCCESS",
                operatorId: "admin",
                createdAt: "2026-01-01 10:00:00"
            }
        ]
    })),
    rebuildSearchIndex: vi.fn(async () => 1)
}));

vi.mock("./search-statistics-service", () => mocks);

describe("SearchStatisticsPage", () => {
    beforeEach(() => {
        queryClient.clear();
        mocks.getSearchStatisticsSummary.mockClear();
        mocks.getSearchEventDetail.mockClear();
        mocks.pageSearchEvents.mockClear();
        mocks.rebuildSearchIndex.mockClear();
    });

    afterEach(() => {
        cleanup();
        queryClient.clear();
        vi.restoreAllMocks();
    });

    it("renders page shell", async () => {
        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <SearchStatisticsPage />
                </AntdApp>
            </QueryClientProvider>
        );

        expect(await screen.findByRole("heading", { name: "检索统计" })).toBeInTheDocument();
        expect(screen.getByText("统计摘要")).toBeInTheDocument();
        expect(screen.getByText("检索记录")).toBeInTheDocument();
        expect(screen.getByText("索引重建")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "刷新统计" })).toBeInTheDocument();
        expect(screen.getByText("搜索次数")).toBeInTheDocument();
        expect(screen.getByText("失败次数")).toBeInTheDocument();
        expect(screen.getByText("零结果次数")).toBeInTheDocument();
        expect(screen.getByText("点击次数")).toBeInTheDocument();
        expect(screen.getByText("热门搜索词")).toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "查询记录" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "查看详情" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "触发重建" })).not.toBeInTheDocument();
    }, 30000);

    it("switches statistics panels with segmented control", async () => {
        const user = userEvent.setup();
        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <SearchStatisticsPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(screen.getByText("检索记录"));

        expect(screen.getByRole("button", { name: "查询记录" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "查看详情" })).toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "刷新统计" })).not.toBeInTheDocument();

        await user.click(screen.getByText("索引重建"));

        expect(screen.getByRole("button", { name: "触发重建" })).toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "查询记录" })).not.toBeInTheDocument();
    }, 30000);

    it("loads analysis summary on refresh", async () => {
        const user = userEvent.setup();
        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <SearchStatisticsPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(screen.getByRole("button", { name: "刷新统计" }));

        expect(mocks.getSearchStatisticsSummary).toHaveBeenCalledWith(
            {
                dateFrom: null,
                dateTo: null
            },
            expect.anything()
        );
        expect(await screen.findByText("礼器")).toBeInTheDocument();
        expect(screen.getByText("5 次")).toBeInTheDocument();
    }, 30000);
});
