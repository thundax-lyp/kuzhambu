import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { App } from "antd";
import type { ReactNode } from "react";
import { MemoryRouter, Route, Routes, useLocation } from "react-router-dom";
import { replacePermissions } from "@/auth/permission-storage";
import { queryClient } from "@/query/query-client";
import { OperationsDashboardPage } from "./dashboard-page";
import * as service from "./dashboard-service";

vi.mock("./dashboard-service", () => ({
    confirmHealthAlert: vi.fn(),
    getDashboardOverview: vi.fn(),
    getHealthAlerts: vi.fn(),
    getHealthTrend: vi.fn(),
    recoverHealthAlert: vi.fn()
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
        vi.mocked(service.getHealthAlerts).mockResolvedValue({
            pageNo: 1,
            pageSize: 20,
            count: 1,
            records: [
                {
                    alertId: 9201,
                    component: "admin-server",
                    alertLevel: "CRITICAL",
                    alertStatus: "ACTIVE",
                    sourceRefType: "TASK",
                    sourceRefId: 6101,
                    message: "slow response",
                    suggestion: "检查 admin-server",
                    recoveryAction: "重启服务",
                    recoveryTarget: "task",
                    lastTriggeredAt: "2026-07-01T01:10:00+08:00",
                    failureReason: "连续失败"
                }
            ]
        });
        vi.mocked(service.confirmHealthAlert).mockResolvedValue(undefined);
        vi.mocked(service.recoverHealthAlert).mockResolvedValue(undefined);
    });

    afterEach(() => {
        cleanup();
        queryClient.clear();
        vi.clearAllMocks();
    });

    it("renders system and audit entries when permissions are granted", async () => {
        replacePermissions([
            "operations:dashboard:view",
            "operations:task:view",
            "operations:backup:view",
            "operations:cleanup:view",
            "system:log:view",
            "audit:view"
        ]);

        renderPage();

        expect(await screen.findByText("任务台账")).toBeInTheDocument();
        expect(screen.getByText("备份恢复")).toBeInTheDocument();
        expect(screen.getByText("清理维护")).toBeInTheDocument();
        expect(screen.getByText("系统日志")).toBeInTheDocument();
        expect(screen.getByText("审计日志")).toBeInTheDocument();
        expect(screen.getByTestId("operations-entry-system-log")).toHaveAttribute(
            "href",
            "/system/logs"
        );
        expect(screen.getByTestId("operations-entry-audit-log")).toHaveAttribute(
            "href",
            "/audit/logs"
        );
    }, 30000);

    it("hides system log entry without permission", () => {
        replacePermissions([
            "operations:dashboard:view",
            "operations:task:view",
            "operations:backup:view",
            "operations:cleanup:view",
            "audit:view"
        ]);

        renderPage();

        expect(screen.getByText("任务台账")).toBeInTheDocument();
        expect(screen.getByText("备份恢复")).toBeInTheDocument();
        expect(screen.getByText("清理维护")).toBeInTheDocument();
        expect(screen.queryByText("系统日志")).not.toBeInTheDocument();
        expect(screen.getByText("审计日志")).toBeInTheDocument();
    });

    it("hides audit log entry without permission", () => {
        replacePermissions([
            "operations:dashboard:view",
            "operations:task:view",
            "operations:backup:view",
            "operations:cleanup:view",
            "system:log:view"
        ]);

        renderPage();

        expect(screen.getByText("任务台账")).toBeInTheDocument();
        expect(screen.getByText("备份恢复")).toBeInTheDocument();
        expect(screen.getByText("清理维护")).toBeInTheDocument();
        expect(screen.getByText("系统日志")).toBeInTheDocument();
        expect(screen.queryByText("审计日志")).not.toBeInTheDocument();
    });

    it("renders empty operation entries state when no entry permissions are granted", () => {
        replacePermissions(["operations:dashboard:view"]);

        renderPage();

        expect(screen.getByText("暂无可访问的运维入口")).toBeInTheDocument();
    }, 30000);

    it("navigates to system logs page when system log entry is clicked", async () => {
        replacePermissions([
            "operations:dashboard:view",
            "system:log:view",
            "operations:task:view",
            "operations:backup:view",
            "operations:cleanup:view"
        ]);

        renderPage();

        await screen.findByText("系统日志");
        fireEvent.click(screen.getByTestId("operations-entry-system-log"));
        expect(screen.getByTestId("current-path")).toHaveTextContent("/system/logs");
    }, 30000);

    it("navigates to audit logs page when audit log entry is clicked", async () => {
        replacePermissions([
            "operations:dashboard:view",
            "audit:view",
            "operations:task:view",
            "operations:backup:view",
            "operations:cleanup:view"
        ]);

        renderPage();

        await screen.findByText("审计日志");
        fireEvent.click(screen.getByTestId("operations-entry-audit-log"));
        expect(screen.getByTestId("current-path")).toHaveTextContent("/audit/logs");
    }, 30000);

    it("renders dashboard controls and data", async () => {
        replacePermissions([
            "operations:dashboard:view",
            "operations:health:view",
            "operations:task:view"
        ]);

        renderPage();

        expect(await screen.findByRole("heading", { name: "运营看板" })).toBeInTheDocument();
        expect(await screen.findByText("热门内容")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /刷新/ })).toBeInTheDocument();
        expect(screen.getByText("admin-server")).toBeInTheDocument();
        expect(screen.getByText("TRANSLATE")).toBeInTheDocument();
        expect(screen.getByText(/分享访问\s+100/)).toBeInTheDocument();
        expect(screen.getByText("健康告警 1 个，严重 1 个")).toBeInTheDocument();
        expect(screen.getByText("任务台账")).toBeInTheDocument();
        expect(screen.getByRole("link", { name: "查看全部" })).toHaveAttribute(
            "href",
            "/operations/health"
        );
        expect(service.getDashboardOverview).toHaveBeenCalledWith({ periodType: "WEEK" });
        expect(service.getHealthTrend).toHaveBeenCalledWith({ bucketType: "DAY" });
        expect(service.getHealthAlerts).toHaveBeenCalledWith({ pageNo: 1, pageSize: 20 });
    }, 30000);

    it("navigates to operations health page from health summary card", async () => {
        replacePermissions(["operations:dashboard:view", "operations:health:view"]);

        renderPage();

        await screen.findByText("健康巡检");
        fireEvent.click(screen.getByRole("link", { name: "查看全部" }));
        expect(screen.getByTestId("current-path")).toHaveTextContent("/operations/health");
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
        expect(screen.getByText("告警 1")).toBeInTheDocument();
        expect(screen.getByText("关联告警")).toBeInTheDocument();
        expect(screen.getByText("检查 admin-server")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "查看全部告警" })).toBeInTheDocument();
    }, 30000);

    it("renders empty related alerts for health detail without component alerts", async () => {
        vi.mocked(service.getDashboardOverview).mockResolvedValue({
            healthSummaries: [
                {
                    checkId: 2,
                    component: "worker",
                    healthStatus: "UP",
                    probeSource: "LOCAL"
                }
            ]
        });
        vi.mocked(service.getHealthAlerts).mockResolvedValue({
            records: []
        });

        renderPage();

        fireEvent.click(await screen.findByText("worker"));

        expect(await screen.findByText("worker 健康明细")).toBeInTheDocument();
        expect(screen.getByText("暂无关联告警")).toBeInTheDocument();
    }, 30000);

    it("opens health alert drawer and hides management buttons without manage permission", async () => {
        renderPage();

        fireEvent.click(await screen.findByRole("button", { name: "查看告警" }));

        expect(await screen.findByText("健康告警")).toBeInTheDocument();
        expect(screen.getByText("连续失败")).toBeInTheDocument();
        expect(screen.getByRole("link", { name: "去处理" })).toHaveAttribute(
            "href",
            "/operations/tasks"
        );
        expect(screen.queryByRole("button", { name: "确认" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "标记恢复" })).not.toBeInTheDocument();
    }, 30000);

    it("allows health alert confirmation and recovery with manage permission", async () => {
        replacePermissions([
            "operations:dashboard:view",
            "operations:health:view",
            "operations:health:manage"
        ]);
        renderPage();

        fireEvent.click(await screen.findByRole("button", { name: "查看告警" }));
        expect(await screen.findByText("连续失败")).toBeInTheDocument();
        expect(screen.getByText("状态：未确认")).toBeInTheDocument();
        fireEvent.click(screen.getByRole("button", { name: /确\s*认/ }));
        fireEvent.click(screen.getByRole("button", { name: "标记恢复" }));

        await waitFor(() => {
            expect(vi.mocked(service.confirmHealthAlert).mock.calls[0]?.[0]).toEqual({
                alertId: 9201
            });
            expect(vi.mocked(service.recoverHealthAlert).mock.calls[0]?.[0]).toEqual({
                alertId: 9201
            });
        });
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
        vi.mocked(service.getHealthAlerts).mockResolvedValue({
            records: []
        });

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
    const CurrentLocation = () => {
        const location = useLocation();
        return <div data-testid="current-path">{location.pathname}</div>;
    };

    return render(
        <MemoryRouter>
            <QueryClientProvider client={queryClient}>
                <App>
                    <Routes>
                        <Route
                            element={
                                <>
                                    <CurrentLocation />
                                    <OperationsDashboardPage />
                                </>
                            }
                            path="*"
                        />
                    </Routes>
                </App>
            </QueryClientProvider>
        </MemoryRouter>
    );
};
