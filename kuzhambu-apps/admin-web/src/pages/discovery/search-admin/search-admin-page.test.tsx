import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen } from "@testing-library/react";
import { App as AntdApp } from "antd";
import { queryClient } from "@/query/query-client";
import { SearchAdminPage } from "./search-admin-page";

vi.mock("./search-admin-service", () => ({
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

describe("SearchAdminPage", () => {
    beforeEach(() => {
        queryClient.clear();
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
        expect(screen.getByRole("button", { name: "查询日志" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "查看详情" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "触发重建" })).toBeInTheDocument();
    }, 10000);
});
