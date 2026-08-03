import { AdminQueryProvider } from "@/query/query-client";
import { App } from "antd";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { replacePermissions } from "@/auth/permission-storage";
import { ActionStatusPage } from "./action-status-page";
import * as service from "./action-status-service";

vi.mock("./action-status-service", () => ({
    listActionCapabilities: vi.fn(),
    listActionStatuses: vi.fn(),
    refreshActionStatus: vi.fn()
}));

const statuses = [
    {
        scope: "classics",
        capability: "summary",
        available: false,
        unavailableReason: "No enabled business configuration",
        checkedAt: "2026-07-01T00:00:00.000Z"
    }
];

const renderPage = () =>
    render(
        <App>
            <AdminQueryProvider>
                <ActionStatusPage />
            </AdminQueryProvider>
        </App>
    );

describe("ActionStatusPage", () => {
    beforeEach(() => {
        replacePermissions(["ai:config:view", "ai:config:edit"]);
        vi.mocked(service.listActionCapabilities).mockResolvedValue([
            {
                capability: "summary",
                name: "摘要生成",
                requiredTags: ["chat"],
                outputMode: "JSON",
                enabled: true,
                priority: 1
            }
        ]);
        vi.mocked(service.listActionStatuses).mockResolvedValue(statuses);
        vi.mocked(service.refreshActionStatus).mockResolvedValue({
            ...statuses[0],
            available: true,
            unavailableReason: null
        });
    });

    afterEach(() => {
        cleanup();
        vi.clearAllMocks();
    });

    it("renders action status matrix with unavailable reason", async () => {
        renderPage();

        expect(await screen.findByRole("heading", { name: "AI 动作状态" })).toBeInTheDocument();
        expect(await screen.findByText("summary")).toBeInTheDocument();
        expect(screen.getByText("不可用")).toBeInTheDocument();
        expect(screen.getByText("No enabled business configuration")).toBeInTheDocument();
    });

    it("refreshes one action status", async () => {
        renderPage();
        await screen.findByText("summary");

        fireEvent.click(screen.getByRole("button", { name: /刷新状态/ }));

        await waitFor(() => {
            expect(service.refreshActionStatus).toHaveBeenCalledWith(
                {
                    scope: "classics",
                    capability: "summary"
                },
                expect.anything()
            );
        });
    });

    it("disables refresh actions without edit permission", async () => {
        replacePermissions(["ai:config:view"]);
        renderPage();

        await screen.findByText("summary");
        expect(screen.getByRole("button", { name: /刷新状态/ })).toBeDisabled();
        expect(screen.getByRole("button", { name: /刷新全部/ })).toBeDisabled();
    });
});
