import { QueryClientProvider } from "@tanstack/react-query";
import { App } from "antd";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { replacePermissions } from "@/auth/permission-storage";
import * as dashboardService from "@/pages/operations/dashboard/dashboard-service";
import { queryClient } from "@/query/query-client";
import { OperationsHealthPage } from "./health-page";
import * as service from "./health-service";

vi.mock("./health-service", () => ({
    getOperationsHealthPage: vi.fn()
}));

vi.mock("@/pages/operations/dashboard/dashboard-service", () => ({
    getHealthAlerts: vi.fn()
}));

const renderPage = () =>
    render(
        <App>
            <QueryClientProvider client={queryClient}>
                <OperationsHealthPage />
            </QueryClientProvider>
        </App>
    );

describe("OperationsHealthPage", () => {
    beforeEach(() => {
        queryClient.clear();
        replacePermissions(["operations:health:view"]);
        vi.mocked(service.getOperationsHealthPage).mockResolvedValue({
            pageNo: 1,
            pageSize: 20,
            count: 2,
            records: [
                {
                    checkId: 9101,
                    component: "admin-starter",
                    healthStatus: "UP",
                    latencyMs: 32,
                    message: "ok",
                    probeSource: "LOCAL",
                    probeTarget: "self",
                    detailsJson: '{"status":"ok","latencyMs":32}',
                    checkedAt: "2026-07-01T01:00:00+08:00"
                },
                {
                    checkId: 9102,
                    component: "search-worker",
                    healthStatus: "DOWN",
                    latencyMs: null,
                    message: null,
                    probeSource: "HTTP",
                    probeTarget: "http://127.0.0.1:20010/kuzhambu-admin-api/actuator/health",
                    detailsJson: "raw details",
                    checkedAt: "bad-date"
                }
            ]
        });
        vi.mocked(dashboardService.getHealthAlerts).mockResolvedValue({
            pageNo: 1,
            pageSize: 10,
            count: 1,
            records: [
                {
                    alertId: 9201,
                    component: "admin-starter",
                    alertLevel: "CRITICAL",
                    alertStatus: "ACTIVE",
                    latestCheckId: 9101,
                    message: "health down",
                    suggestion: "check probe",
                    recoveryAction: "OPEN_HEALTH_DETAIL",
                    lastTriggeredAt: "2026-07-01T01:01:00+08:00"
                }
            ]
        });
    });

    afterEach(() => {
        cleanup();
        queryClient.clear();
        vi.clearAllMocks();
    });

    it("loads first page and renders health records", async () => {
        renderPage();

        expect(await screen.findByRole("heading", { name: "健康检查" })).toBeInTheDocument();
        expect(await screen.findByText("admin-starter")).toBeInTheDocument();
        expect(screen.getByText("search-worker")).toBeInTheDocument();
        expect(screen.getByText("UP")).toBeInTheDocument();
        expect(screen.getByText("DOWN")).toBeInTheDocument();
        expect(screen.getByText("32 ms")).toBeInTheDocument();
        expect(screen.getByText("bad-date")).toBeInTheDocument();
        expect(service.getOperationsHealthPage).toHaveBeenCalledWith({
            component: null,
            healthStatus: null,
            probeSource: null,
            probeTarget: null,
            checkedAtStart: null,
            checkedAtEnd: null,
            pageNo: 1,
            pageSize: 20
        });
    });

    it("queries by component, source and probe target with trimmed values", async () => {
        renderPage();
        await screen.findByText("admin-starter");

        await userEvent.type(screen.getByPlaceholderText("组件"), " admin ");
        fireEvent.mouseDown(screen.getByLabelText("探针来源"));
        const httpOptions = await screen.findAllByText("HTTP");
        await userEvent.click(httpOptions[httpOptions.length - 1]);
        await userEvent.type(screen.getByPlaceholderText("探针目标"), " actuator ");
        await userEvent.click(screen.getByRole("button", { name: /查询/ }));

        await waitFor(() => {
            expect(service.getOperationsHealthPage).toHaveBeenLastCalledWith({
                component: "admin",
                healthStatus: null,
                probeSource: "HTTP",
                probeTarget: "actuator",
                checkedAtStart: null,
                checkedAtEnd: null,
                pageNo: 1,
                pageSize: 20
            });
        });
    });

    it("resets filters and refreshes current query", async () => {
        renderPage();
        await screen.findByText("admin-starter");

        await userEvent.type(screen.getByPlaceholderText("组件"), "database");
        await userEvent.click(screen.getByRole("button", { name: /查询/ }));
        await userEvent.click(screen.getByRole("button", { name: /重\s*置/ }));
        expect(screen.getByPlaceholderText("组件")).toHaveValue("");

        await userEvent.click(screen.getByRole("button", { name: /刷新/ }));
        await waitFor(() => {
            expect(service.getOperationsHealthPage).toHaveBeenLastCalledWith({
                component: null,
                healthStatus: null,
                probeSource: null,
                probeTarget: null,
                checkedAtStart: null,
                checkedAtEnd: null,
                pageNo: 1,
                pageSize: 20
            });
        });

        expect(service.getOperationsHealthPage).toHaveBeenCalled();
    });

    it("keeps filters when switching page", async () => {
        vi.mocked(service.getOperationsHealthPage).mockResolvedValue({
            pageNo: 1,
            pageSize: 20,
            count: 30,
            records: [
                {
                    checkId: 9101,
                    component: "admin-starter",
                    healthStatus: "DEGRADED",
                    probeSource: "HTTP",
                    probeTarget: "http://127.0.0.1:20010/health",
                    message: "slow",
                    checkedAt: "2026-07-01T01:00:00+08:00"
                }
            ]
        });
        renderPage();
        await screen.findByText("admin-starter");

        await userEvent.type(screen.getByPlaceholderText("组件"), "admin");
        await userEvent.click(screen.getByRole("button", { name: /查询/ }));
        await userEvent.click(screen.getByRole("button", { name: "下一页" }));

        await waitFor(() => {
            expect(service.getOperationsHealthPage).toHaveBeenLastCalledWith(
                expect.objectContaining({
                    component: "admin",
                    pageNo: 2,
                    pageSize: 20
                })
            );
        });
    });

    it("shows empty and error states", async () => {
        vi.mocked(service.getOperationsHealthPage).mockResolvedValueOnce({
            pageNo: 1,
            pageSize: 20,
            count: 0,
            records: []
        });
        const { unmount } = renderPage();
        expect(await screen.findByText("暂无健康记录")).toBeInTheDocument();
        unmount();
        cleanup();
        queryClient.clear();

        vi.mocked(service.getOperationsHealthPage).mockRejectedValueOnce(new Error("boom"));
        renderPage();
        await waitFor(() => expect(service.getOperationsHealthPage).toHaveBeenCalled());
    });

    it("opens detail drawer and formats details json", async () => {
        renderPage();
        await screen.findByText("admin-starter");

        await userEvent.click(screen.getAllByRole("button", { name: "详情" })[0]);

        expect(await screen.findByText("健康详情 #9101")).toBeInTheDocument();
        expect(screen.getByText(jsonDetailIncludes('"status": "ok"'))).toBeInTheDocument();
        expect(screen.getByText(jsonDetailIncludes('"latencyMs": 32'))).toBeInTheDocument();
        expect(screen.getAllByText("self").length).toBeGreaterThan(0);
    });

    it("keeps raw detail text and shows empty details", async () => {
        renderPage();
        await screen.findByText("search-worker");

        await userEvent.click(screen.getAllByRole("button", { name: "详情" })[1]);
        expect(await screen.findByText("raw details")).toBeInTheDocument();

        vi.mocked(service.getOperationsHealthPage).mockResolvedValue({
            pageNo: 1,
            pageSize: 20,
            count: 1,
            records: [
                {
                    checkId: 9103,
                    component: "empty-detail",
                    healthStatus: "UP",
                    detailsJson: null
                }
            ]
        });
        cleanup();
        queryClient.clear();
        renderPage();
        await screen.findByText("empty-detail");
        await userEvent.click(screen.getByRole("button", { name: "详情" }));
        expect(await screen.findByText("暂无诊断详情")).toBeInTheDocument();
    });

    it("opens associated alerts by latest check id", async () => {
        renderPage();
        await screen.findByText("admin-starter");

        await userEvent.click(screen.getAllByRole("button", { name: "查看告警" })[0]);

        await waitFor(() => {
            expect(dashboardService.getHealthAlerts).toHaveBeenCalledWith({
                latestCheckId: 9101,
                pageNo: 1,
                pageSize: 10
            });
        });
        expect(await screen.findByText("关联告警 #9101")).toBeInTheDocument();
        expect(screen.getByText("health down")).toBeInTheDocument();
        expect(screen.getByText("check probe")).toBeInTheDocument();
        expect(screen.getByText("OPEN_HEALTH_DETAIL")).toBeInTheDocument();
    });

    it("keeps alert drawer open for empty and failed alert requests", async () => {
        vi.mocked(dashboardService.getHealthAlerts).mockResolvedValueOnce({
            pageNo: 1,
            pageSize: 10,
            count: 0,
            records: []
        });
        const { unmount } = renderPage();
        await screen.findByText("admin-starter");
        await userEvent.click(screen.getAllByRole("button", { name: "查看告警" })[0]);
        expect(await screen.findByText("暂无关联告警")).toBeInTheDocument();
        expect(screen.getByText("关联告警 #9101")).toBeInTheDocument();
        unmount();
        cleanup();
        queryClient.clear();

        vi.mocked(dashboardService.getHealthAlerts).mockRejectedValueOnce(new Error("alert boom"));
        renderPage();
        await screen.findByText("admin-starter");
        await userEvent.click(screen.getAllByRole("button", { name: "查看告警" })[0]);
        expect(await screen.findByText("关联告警加载失败")).toBeInTheDocument();
        expect(screen.getByText("关联告警 #9101")).toBeInTheDocument();
    });
});

const jsonDetailIncludes = (text: string) => (_content: string, element: Element | null) =>
    element?.tagName.toLowerCase() === "pre" && element.textContent?.includes(text) === true;
