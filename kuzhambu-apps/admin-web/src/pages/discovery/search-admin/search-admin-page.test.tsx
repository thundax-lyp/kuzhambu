import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import { queryClient } from "@/query/query-client";
import { SearchAdminPage } from "./search-admin-page";

const mocks = vi.hoisted(() => ({
    getSearchAnalysisSummary: vi.fn(async () => ({
        clickCount: 7,
        failedSearchCount: 2,
        searchCount: 12,
        topQueries: [{ count: 5, queryText: "礼器" }],
        zeroResultSearchCount: 3
    })),
    getSearchLogDetail: vi.fn(async () => null),
    pageSearchLogs: vi.fn(async () => ({
        pageNo: 1,
        pageSize: 10,
        count: 1,
        totalCount: 1,
        totalPage: 1,
        records: [
            {
                searchLogId: "LOG-1001",
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

vi.mock("./search-admin-service", () => mocks);

describe("SearchAdminPage", () => {
    beforeEach(() => {
        queryClient.clear();
        mocks.getSearchAnalysisSummary.mockClear();
        mocks.getSearchLogDetail.mockClear();
        mocks.pageSearchLogs.mockClear();
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
                    <SearchAdminPage />
                </AntdApp>
            </QueryClientProvider>
        );

        expect(await screen.findByRole("heading", { name: "搜索调试台" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "刷新分析" })).toBeInTheDocument();
        expect(screen.getByText("搜索次数")).toBeInTheDocument();
        expect(screen.getByText("失败次数")).toBeInTheDocument();
        expect(screen.getByText("零结果次数")).toBeInTheDocument();
        expect(screen.getByText("点击次数")).toBeInTheDocument();
        expect(screen.getByText("热门搜索词")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "查询日志" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "查看详情" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "触发重建" })).toBeInTheDocument();
    }, 30000);

    it("loads analysis summary on refresh", async () => {
        const user = userEvent.setup();
        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <SearchAdminPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(screen.getByRole("button", { name: "刷新分析" }));

        expect(mocks.getSearchAnalysisSummary).toHaveBeenCalledWith(
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
