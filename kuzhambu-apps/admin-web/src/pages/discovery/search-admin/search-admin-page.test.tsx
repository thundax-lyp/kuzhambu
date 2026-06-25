import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import { queryClient } from "@/query/query-client";
import * as service from "./search-admin-service";
import { SearchAdminPage } from "./search-admin-page";

vi.mock("./search-admin-service", () => ({
    getSearchLogDetail: vi.fn(async () => ({
        searchLogId: "LOG-1001",
        queryText: "礼器",
        normalizedQueryText: "礼器",
        displayQueryText: "礼器",
        intentType: "REWRITE",
        resultTotalCount: 1,
        groupTotalCount: 1,
        searchStatus: "SUCCESS",
        requestId: "REQ-1001",
        traceId: "TRACE-1001",
        searchScopesJson: '{"contentType":"SANCAI_ENTRY"}',
        failureCode: null,
        failureMessage: null,
        createdAt: "2026-01-01 10:00:00"
    })),
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
    const mockedService = vi.mocked(service);

    beforeEach(() => {
        queryClient.clear();
    });

    afterEach(() => {
        cleanup();
        queryClient.clear();
        vi.restoreAllMocks();
    });

    it("loads logs, details and rebuild results", async () => {
        const user = userEvent.setup();
        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <SearchAdminPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(screen.getByRole("button", { name: "查询日志" }));
        expect(await screen.findByText("LOG-1001")).toBeInTheDocument();

        await user.click(screen.getByRole("button", { name: "查看详情" }));
        expect(await screen.findByText("TRACE-1001")).toBeInTheDocument();

        await user.click(screen.getByRole("button", { name: "触发重建" }));
        expect(await screen.findByText("重建结果：1")).toBeInTheDocument();

        await waitFor(() => {
            expect(mockedService.pageSearchLogs).toHaveBeenCalled();
            expect(mockedService.getSearchLogDetail).toHaveBeenCalled();
            expect(mockedService.rebuildSearchIndex).toHaveBeenCalled();
        });
        expect(mockedService.pageSearchLogs.mock.calls.at(-1)?.[0]).toEqual({
            dateFrom: null,
            dateTo: null,
            intentTypes: ["REWRITE"],
            operatorId: null,
            pageNo: 1,
            pageSize: 10,
            queryText: "礼器",
            searchStatuses: ["SUCCESS"]
        });
        expect(mockedService.getSearchLogDetail.mock.calls.at(-1)?.[0]).toEqual({
            searchLogId: "LOG-1001"
        });
        expect(mockedService.rebuildSearchIndex.mock.calls.at(-1)?.[0]).toEqual({ confirm: true });
    });
});
