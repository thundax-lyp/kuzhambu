import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
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
            topAiCapabilities: [{ capability: "TRANSLATE", invocationCount: 9 }]
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
        renderPage();

        expect(await screen.findByRole("heading", { name: "运营看板" })).toBeInTheDocument();
        expect(await screen.findByText("热门内容")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /刷新/ })).toBeInTheDocument();
        expect(screen.getByText("admin-server")).toBeInTheDocument();
        expect(screen.getByText("TRANSLATE")).toBeInTheDocument();
        expect(screen.getByText(/分享访问\s+100/)).toBeInTheDocument();
        expect(screen.getByText("任务台账")).toBeInTheDocument();
        expect(service.getDashboardOverview).toHaveBeenCalledWith({ periodType: "WEEK" });
        expect(service.getHealthTrend).toHaveBeenCalledWith({ bucketType: "DAY" });
    }, 30000);

    it("requests overview again when period control changes", async () => {
        renderPage();

        await screen.findByText("热门内容");
        fireEvent.click(screen.getByText("近 30 天"));

        await waitFor(() => {
            expect(service.getDashboardOverview).toHaveBeenCalledWith({ periodType: "MONTH" });
        });
    }, 30000);

    it("refreshes overview and health trend when refresh button is clicked", async () => {
        renderPage();

        await screen.findByText("热门内容");
        fireEvent.click(screen.getByRole("button", { name: /刷新/ }));

        await waitFor(() => {
            expect(service.getDashboardOverview).toHaveBeenCalledTimes(2);
            expect(service.getHealthTrend).toHaveBeenCalledTimes(2);
        });
    }, 30000);

    it("opens health detail drawer from health list item", async () => {
        renderPage();

        fireEvent.click(await screen.findByText("admin-server"));

        expect(await screen.findByText("admin-server 健康明细")).toBeInTheDocument();
        expect(screen.getByText("采集来源：LOCAL")).toBeInTheDocument();
        expect(screen.getByText("消息：slow response")).toBeInTheDocument();
    }, 30000);

    it("renders real empty states when overview arrays are empty", async () => {
        vi.mocked(service.getDashboardOverview).mockResolvedValue({
            contentGrowthSeries: [],
            searchTrendSeries: [],
            healthSummaries: [],
            topContents: [],
            topQueries: [],
            topTags: [],
            topAiCapabilities: []
        });
        vi.mocked(service.getHealthTrend).mockResolvedValue([]);

        renderPage();

        await screen.findAllByText("暂无趋势数据");
        expect(screen.getByText("暂无健康摘要")).toBeInTheDocument();
        expect(screen.getByText("暂无健康趋势")).toBeInTheDocument();
        expect(screen.getByText("暂无内容排行")).toBeInTheDocument();
        expect(screen.getByText("暂无 AI 能力排行")).toBeInTheDocument();
    }, 30000);

    it("does not request dashboard data without permission", async () => {
        replacePermissions([]);

        renderPage();

        expect(screen.getByText("缺少 operations:dashboard:view 权限")).toBeInTheDocument();
        await waitFor(() => {
            expect(service.getDashboardOverview).not.toHaveBeenCalled();
            expect(service.getHealthTrend).not.toHaveBeenCalled();
        });
    }, 30000);
});

const renderPage = () => {
    return render(
        <MemoryRouter>
            <QueryClientProvider client={queryClient}>
                <OperationsDashboardPage />
            </QueryClientProvider>
        </MemoryRouter>
    );
};
