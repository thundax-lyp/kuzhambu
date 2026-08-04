import { AdminQueryProvider } from "@/query/query-client";
import { act, cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { replacePermissions } from "@/auth/permission-storage";
import { OperationsReportPage } from "./report-page";
import * as service from "./report-service";

vi.mock("./report-service", () => ({
    generateReport: vi.fn(),
    getReportDetail: vi.fn(),
    pageReports: vi.fn(),
    toReportDownloadUrl: vi.fn((reportId: string) => `/download/report/${reportId}`)
}));

vi.mock("@/components/kuzhambu-drawer", () => {
    const mockDrawer = ({
        children,
        open,
        title
    }: {
        children: React.ReactNode;
        open?: boolean;
        title?: React.ReactNode;
    }) =>
        open ? (
            <div>
                {title}
                {children}
            </div>
        ) : null;

    return {
        KuzhambuDrawer: mockDrawer
    };
});

const renderPage = () => {
    render(
        <MemoryRouter>
            <AdminQueryProvider>
                <OperationsReportPage />
            </AdminQueryProvider>
        </MemoryRouter>
    );
};

describe("OperationsReportPage", () => {
    beforeEach(() => {
        replacePermissions(["operations:report:view", "operations:report:generate"]);
        vi.mocked(service.generateReport).mockResolvedValue({
            reportId: "9003",
            reportStatus: "PENDING"
        });
        vi.mocked(service.pageReports).mockResolvedValue({
            pageNo: 1,
            pageSize: 20,
            totalPage: 1,
            count: 2,
            totalCount: 2,
            records: [
                {
                    reportId: "9001",
                    reportType: "WEEKLY",
                    format: "PDF",
                    periodStart: "2026-07-01T00:00:00.000Z",
                    periodEnd: "2026-07-07T23:59:59.000Z",
                    storageObjectId: "7001",
                    artifactFilename: "weekly.pdf",
                    reportStatus: "SUCCEEDED",
                    requesterUserId: "1001",
                    requestedAt: "2026-07-08T01:00:00.000Z",
                    completedAt: "2026-07-08T01:01:00.000Z"
                },
                {
                    reportId: "9002",
                    reportType: "MONTHLY",
                    format: "HTML",
                    periodStart: "2026-06-01T00:00:00.000Z",
                    periodEnd: "2026-06-30T23:59:59.000Z",
                    reportStatus: "FAILED",
                    failureReason: "render worker timeout",
                    requesterUserId: "1002",
                    requestedAt: "2026-07-08T02:00:00.000Z"
                }
            ]
        });
        vi.mocked(service.getReportDetail).mockResolvedValue({
            reportId: "9002",
            reportType: "MONTHLY",
            format: "HTML",
            periodStart: "2026-06-01T00:00:00.000Z",
            periodEnd: "2026-06-30T23:59:59.000Z",
            requestId: "req-9002",
            traceId: "trace-9002",
            templateVersion: "v1",
            reportStatus: "FAILED",
            failureReason: "render worker timeout",
            requesterUserId: "1002",
            requestedAt: "2026-07-08T02:00:00.000Z"
        });
    });

    afterEach(() => {
        vi.useRealTimers();
        cleanup();
        vi.clearAllMocks();
    });

    it("renders filters, generation controls, records and download actions", async () => {
        renderPage();

        expect(await screen.findByRole("heading", { name: "报表管理" })).toBeInTheDocument();
        expect(screen.getByLabelText("报表类型")).toBeInTheDocument();
        expect(screen.getByLabelText("导出格式")).toBeInTheDocument();
        expect(screen.getByLabelText("状态")).toBeInTheDocument();
        expect(screen.getByLabelText("请求人用户 ID")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /生成报表/ })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /提交生成/ })).toBeInTheDocument();
        expect(await screen.findByText("周报")).toBeInTheDocument();
        expect(screen.getByText("render worker timeout")).toBeInTheDocument();
        expect(screen.getByRole("link", { name: /下载/ })).toHaveAttribute(
            "href",
            "/download/report/9001"
        );
    });

    it("opens detail drawer and renders failed reason", async () => {
        renderPage();

        expect(await screen.findByText("render worker timeout")).toBeInTheDocument();
        fireEvent.click(screen.getAllByRole("button", { name: /详\s*情/ })[1]);

        expect(await screen.findByText("报表详情")).toBeInTheDocument();
        expect(await screen.findByText("req-9002")).toBeInTheDocument();
        expect(screen.getAllByText("render worker timeout").length).toBeGreaterThan(0);
        expect(service.getReportDetail).toHaveBeenCalledWith({ reportId: "9002" });
    });

    it("disables generation without generate permission", async () => {
        replacePermissions(["operations:report:view"]);
        renderPage();

        expect(await screen.findByRole("heading", { name: "报表管理" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /生成报表/ })).toBeDisabled();
        expect(screen.getByRole("button", { name: /提交生成/ })).toBeDisabled();
    });

    it("requires a positive decimal requester user id before filtering reports", async () => {
        renderPage();

        expect(await screen.findByRole("heading", { name: "报表管理" })).toBeInTheDocument();
        vi.mocked(service.pageReports).mockClear();

        fireEvent.change(screen.getByLabelText("请求人用户 ID"), { target: { value: "abc" } });
        expect(screen.getByRole("button", { name: /查询/ })).toBeDisabled();
        await waitFor(() =>
            expect(service.pageReports).toHaveBeenCalledWith(
                expect.objectContaining({ requesterUserId: undefined })
            )
        );

        vi.mocked(service.pageReports).mockClear();
        fireEvent.change(screen.getByLabelText("请求人用户 ID"), { target: { value: "1003" } });
        expect(screen.getByRole("button", { name: /查询/ })).toBeEnabled();
        await waitFor(() =>
            expect(service.pageReports).toHaveBeenCalledWith(
                expect.objectContaining({ requesterUserId: "1003" })
            )
        );
    });

    it("polls when report page contains running records", async () => {
        vi.useFakeTimers({ shouldAdvanceTime: true });
        vi.mocked(service.pageReports).mockResolvedValue({
            pageNo: 1,
            pageSize: 20,
            totalPage: 1,
            count: 1,
            totalCount: 1,
            records: [
                {
                    reportId: "9004",
                    reportType: "WEEKLY",
                    format: "PDF",
                    reportStatus: "RUNNING",
                    requestedAt: "2026-07-08T03:00:00.000Z"
                }
            ]
        });

        renderPage();
        await screen.findByText("RUNNING");
        await act(async () => {
            await vi.advanceTimersByTimeAsync(5000);
        });

        expect(service.pageReports).toHaveBeenCalledTimes(2);
    }, 30000);
});
