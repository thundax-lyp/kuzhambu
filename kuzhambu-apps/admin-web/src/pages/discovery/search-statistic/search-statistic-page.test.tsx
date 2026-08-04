import { AdminQueryProvider } from "@/query/query-client";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import { SearchStatisticPage } from "./search-statistic-page";

const mocks = vi.hoisted(() => ({
    getSearchStatisticsSummary: vi.fn(async () => ({
        clickCount: 7,
        failedSearchCount: 2,
        searchCount: 12,
        topQueries: [
            { count: 5, queryText: "礼器" },
            { count: 12, queryText: "论语" },
            { count: 11, queryText: "孟子" },
            { count: 10, queryText: "大学" },
            { count: 9, queryText: "中庸" },
            { count: 8, queryText: "诗经" },
            { count: 7, queryText: "尚书" },
            { count: 6, queryText: "周易" },
            { count: 4, queryText: "春秋" },
            { count: 3, queryText: "礼记" },
            { count: 1, queryText: "冷门词" }
        ],
        zeroResultSearchCount: 3
    })),
    getSearchEventDetail: vi.fn(async () => ({
        id: "EVT-1001",
        queryText: "礼器",
        normalizedQueryText: "礼器",
        displayQueryText: "礼器",
        intentType: "REWRITE",
        resultTotalCount: 1,
        groupTotalCount: 1,
        searchStatus: "SUCCESS",
        requestId: "REQ-1001",
        traceId: "TRACE-1001",
        searchScopesJson: '{"scope":"classics"}'
    })),
    pageSearchEvents: vi.fn(async () => ({
        pageNo: 1,
        pageSize: 10,
        count: 1,
        totalCount: 1,
        totalPage: 1,
        records: [
            {
                id: "EVT-1001",
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

vi.mock("./search-statistic-service", () => mocks);

describe("SearchStatisticPage", () => {
    beforeEach(() => {
        mocks.getSearchStatisticsSummary.mockClear();
        mocks.getSearchEventDetail.mockClear();
        mocks.pageSearchEvents.mockClear();
        mocks.rebuildSearchIndex.mockClear();
    });

    afterEach(() => {
        cleanup();
        vi.restoreAllMocks();
    });

    it("renders page shell", async () => {
        render(
            <AdminQueryProvider>
                <AntdApp>
                    <SearchStatisticPage />
                </AntdApp>
            </AdminQueryProvider>
        );

        expect(await screen.findByRole("heading", { name: "检索统计" })).toBeInTheDocument();
        expect(screen.getByText("统计摘要")).toBeInTheDocument();
        expect(screen.getByText("检索记录")).toBeInTheDocument();
        expect(screen.getByText("索引重建")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "统计" })).toBeInTheDocument();
        expect(screen.getByText("搜索次数")).toBeInTheDocument();
        expect(screen.getByText("失败次数")).toBeInTheDocument();
        expect(screen.getByText("零结果次数")).toBeInTheDocument();
        expect(screen.getByText("点击次数")).toBeInTheDocument();
        expect(screen.getByText("热门搜索词 Top 10")).toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "查询记录" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "触发重建" })).not.toBeInTheDocument();
        await waitFor(() =>
            expect(mocks.getSearchStatisticsSummary).toHaveBeenCalledWith({
                dateFrom: null,
                dateTo: null
            })
        );
    }, 30000);

    it("switches statistics panels with segmented control", async () => {
        const user = userEvent.setup();
        render(
            <AdminQueryProvider>
                <AntdApp>
                    <SearchStatisticPage />
                </AntdApp>
            </AdminQueryProvider>
        );

        await user.click(screen.getByText("检索记录"));

        expect(screen.getByRole("button", { name: "查询记录" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /重\s*置/u })).toBeInTheDocument();
        expect(screen.getByRole("table", { name: "检索记录表格" })).toBeInTheDocument();
        expect(screen.getByLabelText("搜索词")).toBeInTheDocument();
        expect(screen.getByLabelText("状态")).toBeInTheDocument();
        expect(screen.getAllByLabelText("检索记录时间范围")).toHaveLength(2);
        expect(screen.queryByLabelText("意图")).not.toBeInTheDocument();
        expect(screen.queryByLabelText("操作者")).not.toBeInTheDocument();
        expect(screen.queryByLabelText("页码")).not.toBeInTheDocument();
        expect(screen.queryByLabelText("页大小")).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "统计" })).not.toBeInTheDocument();

        await user.click(screen.getByText("索引重建"));

        expect(screen.getByRole("button", { name: "触发重建" })).toBeInTheDocument();
        expect(screen.queryByLabelText("索引重建进度")).not.toBeInTheDocument();
        await user.click(screen.getByRole("button", { name: "触发重建" }));

        expect(await screen.findByLabelText("索引重建进度")).toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "查询记录" })).not.toBeInTheDocument();
    }, 30000);

    it("loads analysis summary on refresh", async () => {
        const user = userEvent.setup();
        render(
            <AdminQueryProvider>
                <AntdApp>
                    <SearchStatisticPage />
                </AntdApp>
            </AdminQueryProvider>
        );

        expect(screen.getAllByLabelText("统计时间范围")).toHaveLength(2);
        await user.click(screen.getByRole("button", { name: "统计" }));

        expect(mocks.getSearchStatisticsSummary).toHaveBeenCalledWith({
            dateFrom: null,
            dateTo: null
        });
        expect(
            await screen.findByRole("list", { name: "热门搜索词前10名柱状图" })
        ).toBeInTheDocument();
        expect(await screen.findByText("礼器")).toBeInTheDocument();
        expect(screen.getByText("5 次")).toBeInTheDocument();
        expect(screen.getAllByRole("listitem")).toHaveLength(10);
        expect(screen.queryByText("冷门词")).not.toBeInTheDocument();
    }, 30000);

    it("loads records through filter form and opens row detail inline", async () => {
        const user = userEvent.setup();
        render(
            <AdminQueryProvider>
                <AntdApp>
                    <SearchStatisticPage />
                </AntdApp>
            </AdminQueryProvider>
        );

        await user.click(screen.getByText("检索记录"));
        await user.click(screen.getByRole("button", { name: "查询记录" }));

        expect(mocks.pageSearchEvents).toHaveBeenCalledWith(
            {
                dateFrom: null,
                dateTo: null,
                pageNo: 1,
                pageSize: 20,
                queryText: "礼器",
                searchStatuses: ["SUCCESS"]
            },
            expect.anything()
        );
        expect(await screen.findByText("EVT-1001")).toBeInTheDocument();

        await user.click(screen.getByRole("button", { name: /展开行|Expand row/u }));

        expect(mocks.getSearchEventDetail).toHaveBeenCalledWith(
            { id: "EVT-1001" },
            expect.anything()
        );
        expect(await screen.findByText("REQ-1001")).toBeInTheDocument();
        expect(screen.getByText('{"scope":"classics"}')).toBeInTheDocument();
    }, 30000);
});
