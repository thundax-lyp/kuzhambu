import { AdminQueryProvider } from "@/query/query-client";
import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { replacePermissions } from "@/auth/permission-storage";
import { OperationsTaskPage } from "./task-page";
import * as service from "./task-service";

vi.mock("./task-service", () => ({
    pageTasks: vi.fn(),
    getTaskDetail: vi.fn()
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

describe("OperationsTaskPage", () => {
    beforeEach(() => {
        replacePermissions(["operations:task:view"]);
        vi.mocked(service.pageTasks).mockResolvedValue({
            pageNo: 1,
            pageSize: 20,
            totalPage: 1,
            count: 1,
            totalCount: 1,
            records: [
                {
                    snapshotId: "901",
                    sourceDomain: "operations",
                    taskType: "BACKUP_RESTORE",
                    taskStatus: "RUNNING",
                    successCount: 10,
                    failedCount: 0,
                    startedAt: "2026-06-29T01:10:00.000Z",
                    completedAt: "2026-06-29T01:11:00.000Z"
                }
            ]
        });
        vi.mocked(service.getTaskDetail).mockResolvedValue(null as never);
    });

    afterEach(() => {
        cleanup();
        vi.clearAllMocks();
    });

    it("renders page shell", async () => {
        render(
            <MemoryRouter>
                <AdminQueryProvider>
                    <OperationsTaskPage />
                </AdminQueryProvider>
            </MemoryRouter>
        );

        expect(await screen.findByRole("heading", { name: "运营任务台账" })).toBeInTheDocument();
        expect(await screen.findByText("operations")).toBeInTheDocument();
        expect(screen.getByText("长任务列表")).toBeInTheDocument();
        expect(screen.getByText("运营看板")).toBeInTheDocument();
    }, 30000);

    it("renders failed task hints in list and detail drawer", async () => {
        vi.mocked(service.pageTasks).mockResolvedValue({
            pageNo: 1,
            pageSize: 20,
            totalPage: 1,
            count: 1,
            totalCount: 1,
            records: [
                {
                    snapshotId: "902",
                    sourceDomain: "operations",
                    taskType: "REPORT_EXPORT",
                    taskStatus: "FAILED",
                    successCount: 1,
                    failedCount: 2,
                    failureReason: null,
                    startedAt: "2026-06-29T01:10:00.000Z",
                    completedAt: "2026-06-29T01:11:00.000Z"
                }
            ]
        });
        vi.mocked(service.getTaskDetail).mockResolvedValue({
            snapshotId: "902",
            sourceDomain: "operations",
            taskType: "REPORT_EXPORT",
            taskKey: "report-902",
            taskStatus: "FAILED",
            successCount: 1,
            failedCount: 2,
            failureReason: null,
            requestedByUserId: "1001",
            startedAt: "2026-06-29T01:10:00.000Z",
            completedAt: "2026-06-29T01:11:00.000Z"
        });

        render(
            <MemoryRouter>
                <AdminQueryProvider>
                    <OperationsTaskPage />
                </AdminQueryProvider>
            </MemoryRouter>
        );

        expect(await screen.findByText("REPORT_EXPORT")).toBeInTheDocument();
        expect(screen.getByText("未返回失败原因")).toBeInTheDocument();

        fireEvent.click(screen.getByRole("button", { name: "详情" }));

        expect(await screen.findByText("长任务执行失败")).toBeInTheDocument();
        expect(
            screen.getByText("未返回失败原因。请查看来源域任务状态，必要时重新发起业务动作。")
        ).toBeInTheDocument();
        expect(screen.getByRole("link", { name: "查看告警" })).toHaveAttribute(
            "href",
            "/operations/dashboard?sourceRefType=LONG_TASK&sourceRefId=902"
        );
        expect(screen.getByText("report-902")).toBeInTheDocument();
    }, 30000);
});
