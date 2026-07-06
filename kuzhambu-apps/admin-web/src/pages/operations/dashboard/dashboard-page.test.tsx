import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import type { ReactNode } from "react";
import { MemoryRouter } from "react-router-dom";
import { replacePermissions } from "@/auth/permission-storage";
import { queryClient } from "@/query/query-client";
import { OperationsDashboardPage } from "./dashboard-page";
import * as service from "./dashboard-service";

vi.mock("./dashboard-service", () => ({
    getDashboardOverview: vi.fn(),
    getHealthTrend: vi.fn()
}));

vi.mock("@/components/kuzhambu-drawer", () => {
    const mockDrawer = ({
        children,
        open,
        title
    }: {
        children: ReactNode;
        open?: boolean;
        title?: ReactNode;
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

describe("OperationsDashboardPage", () => {
    beforeEach(() => {
        queryClient.clear();
        replacePermissions(["operations:dashboard:view"]);
        vi.mocked(service.getDashboardOverview).mockResolvedValue({
            periodStart: "2026-07-01T00:00:00+08:00",
            periodEnd: "2026-07-07T23:59:59+08:00",
            contentCount: 20,
            translatedContentCount: 8,
            imageReadyContentCount: 5,
            visualAssetReadyContentCount: 4,
            shareVisitCount: 100,
            aiInvocationCount: 12,
            aiSucceededInvocationCount: 10,
            aiFailedInvocationCount: 2,
            aiAvgLatencyMs: 230,
            aiTotalCostAmount: 18,
            searchCount: 30,
            qaCount: 6,
            avgSearchLatencyMs: 120,
            tagCoverageRate: 0.8,
            unhealthyComponentCount: 1,
            runningTaskCount: 2,
            failedTaskCount: 1,
            contentGrowthSeries: [{ bucket: "2026-07-01", count: 3 }],
            searchTrendSeries: [{ bucket: "2026-07-01", count: 9 }],
            qaTrendSeries: [],
            tagGrowthSeries: [],
            healthSummaries: [
                {
                    checkId: 1,
                    component: "admin-server",
                    healthStatus: "DEGRADED",
                    latencyMs: 180,
                    message: "slow response",
                    probeSource: "LOCAL",
                    probeTarget: "http://localhost:8080",
                    checkedAt: "2026-07-01T01:00:00+08:00"
                }
            ],
            taskStatusSummaries: [],
            topContents: [
                {
                    contentId: 101,
                    contentType: "ARTICLE",
                    title: "热门内容",
                    visitCount: 88
                }
            ],
            topQueries: [{ queryText: "三彩", count: 12 }],
            topTags: [{ tagName: "陶瓷", contentRefCount: 6 }],
            topAiCapabilities: []
        });
        vi.mocked(service.getHealthTrend).mockResolvedValue([
            {
                bucket: "2026-07-01",
                upCount: 2,
                degradedCount: 1,
                downCount: 0,
                avgLatencyMs: 150
            }
        ]);
    });

    afterEach(() => {
        cleanup();
        queryClient.clear();
        vi.clearAllMocks();
    });

    it("renders dashboard controls and data", async () => {
        render(
            <MemoryRouter>
                <QueryClientProvider client={queryClient}>
                    <OperationsDashboardPage />
                </QueryClientProvider>
            </MemoryRouter>
        );

        expect(await screen.findByRole("heading", { name: "运营看板" })).toBeInTheDocument();
        expect(await screen.findByText("热门内容")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "刷新" })).toBeInTheDocument();
        expect(screen.getByText("admin-server")).toBeInTheDocument();
        expect(screen.getByText("任务台账")).toBeInTheDocument();
    }, 30000);

    it("does not request dashboard data without permission", async () => {
        replacePermissions([]);

        render(
            <MemoryRouter>
                <QueryClientProvider client={queryClient}>
                    <OperationsDashboardPage />
                </QueryClientProvider>
            </MemoryRouter>
        );

        expect(screen.getByText("缺少 operations:dashboard:view 权限")).toBeInTheDocument();
        await waitFor(() => {
            expect(service.getDashboardOverview).not.toHaveBeenCalled();
            expect(service.getHealthTrend).not.toHaveBeenCalled();
        });
    }, 30000);
});
