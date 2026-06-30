import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { replacePermissions } from "@/auth/permission-storage";
import { queryClient } from "@/query/query-client";
import { OperationsTasksPage } from "./tasks-page";
import * as service from "./tasks-service";

vi.mock("./tasks-service", () => ({
    getHealthSummary: vi.fn(),
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

describe("OperationsTasksPage", () => {
    beforeEach(() => {
        queryClient.clear();
        replacePermissions(["operations:task:view"]);
        vi.mocked(service.getHealthSummary).mockResolvedValue([
            {
                checkId: 1,
                component: "MySQL",
                healthStatus: "OK",
                latencyMs: 120,
                message: "All good",
                checkedAt: "2026-06-29T01:00:00.000Z"
            }
        ]);
        vi.mocked(service.pageTasks).mockResolvedValue({
            pageNo: 1,
            pageSize: 20,
            totalPage: 1,
            count: 1,
            totalCount: 1,
            records: [
                {
                    snapshotId: 901,
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
        queryClient.clear();
        vi.clearAllMocks();
    });

    it("renders page shell", async () => {
        render(
            <MemoryRouter>
                <QueryClientProvider client={queryClient}>
                    <OperationsTasksPage />
                </QueryClientProvider>
            </MemoryRouter>
        );

        expect(await screen.findByRole("heading", { name: "运营任务台账" })).toBeInTheDocument();
        expect(await screen.findByText("All good")).toBeInTheDocument();
        expect(screen.getByText("长任务列表")).toBeInTheDocument();
    }, 10000);
});
