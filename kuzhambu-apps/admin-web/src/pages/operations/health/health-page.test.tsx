import { QueryClientProvider } from "@tanstack/react-query";
import { App } from "antd";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { replacePermissions } from "@/auth/permission-storage";
import { queryClient } from "@/query/query-client";
import { OperationsHealthPage } from "./health-page";
import * as service from "./health-service";

vi.mock("./health-service", () => ({
    getOperationsHealthPage: vi.fn()
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
                    checkedAt: "bad-date"
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
        await userEvent.click(await screen.findByText("HTTP"));
        await userEvent.type(screen.getByPlaceholderText("探针目标"), " actuator ");
        await userEvent.click(screen.getByRole("button", { name: "查询" }));

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
        await userEvent.click(screen.getByRole("button", { name: "查询" }));
        await userEvent.click(screen.getByRole("button", { name: "重置" }));

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

        await userEvent.click(screen.getByRole("button", { name: "刷新" }));
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
        await userEvent.click(screen.getByRole("button", { name: "查询" }));
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
});
