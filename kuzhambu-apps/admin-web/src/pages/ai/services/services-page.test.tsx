import { QueryClientProvider } from "@tanstack/react-query";
import { App } from "antd";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { replacePermissions } from "@/auth/permission-storage";
import { queryClient } from "@/query/query-client";
import { ServicesPage } from "./services-page";
import * as service from "./services-service";

vi.mock("./services-service", () => ({
    changeServiceConfig: vi.fn(),
    listGovernanceServices: vi.fn()
}));

vi.mock("@/components/kuzhambu-drawer", () => {
    const mockDrawer = ({
        children,
        footer,
        open,
        title
    }: {
        children: React.ReactNode;
        footer?: React.ReactNode;
        open?: boolean;
        title?: React.ReactNode;
    }) =>
        open ? (
            <div>
                <h3>{title}</h3>
                {children}
                {footer}
            </div>
        ) : null;

    return {
        KuzhambuDrawer: mockDrawer
    };
});

const records = [
    {
        serviceId: 1001,
        serviceRole: "PRIMARY" as const,
        apiSource: "OPENAI",
        baseUrl: "https://api.primary.example",
        apiKeyConfigured: true,
        enabled: true,
        status: "AVAILABLE",
        lastCheckedAt: "2026-07-01T01:00:00.000Z",
        configuredAt: "2026-07-01T00:00:00.000Z"
    },
    {
        serviceId: 1002,
        serviceRole: "BACKUP" as const,
        apiSource: "OPENAI",
        baseUrl: "https://api.backup.example",
        apiKeyConfigured: false,
        enabled: false,
        status: "UNAVAILABLE",
        lastCheckedAt: null,
        configuredAt: "2026-07-01T00:00:00.000Z"
    }
];

const renderPage = () =>
    render(
        <App>
            <QueryClientProvider client={queryClient}>
                <ServicesPage />
            </QueryClientProvider>
        </App>
    );

describe("ServicesPage", () => {
    beforeEach(() => {
        queryClient.clear();
        replacePermissions(["ai:config:view", "ai:config:edit"]);
        vi.mocked(service.listGovernanceServices).mockResolvedValue(records);
        vi.mocked(service.changeServiceConfig).mockResolvedValue(records[0]);
    });

    afterEach(() => {
        cleanup();
        queryClient.clear();
        vi.clearAllMocks();
    });

    it("renders primary and backup service cards", async () => {
        renderPage();

        expect(await screen.findByRole("heading", { name: "AI 服务配置" })).toBeInTheDocument();
        expect(screen.getByText("PRIMARY 主服务")).toBeInTheDocument();
        expect(screen.getByText("BACKUP 备用服务")).toBeInTheDocument();
        expect(await screen.findByText("https://api.primary.example")).toBeInTheDocument();
        expect(screen.getByText("https://api.backup.example")).toBeInTheDocument();
        expect(screen.queryByText("encrypted-api-key")).not.toBeInTheDocument();
    });

    it("opens drawer and saves without requiring api key text", async () => {
        renderPage();
        await screen.findByText("https://api.primary.example");

        fireEvent.click(screen.getAllByRole("button", { name: /编辑/ })[0]);
        expect(await screen.findByRole("heading", { name: "编辑 AI 服务" })).toBeInTheDocument();
        fireEvent.click(screen.getByRole("button", { name: /保存/ }));

        await waitFor(() => {
            expect(service.changeServiceConfig).toHaveBeenCalled();
        });
        const request = vi.mocked(service.changeServiceConfig).mock.calls[0]?.[0];
        expect(request).toEqual(
            expect.objectContaining({
                serviceId: 1001,
                serviceRole: "PRIMARY"
            })
        );
        expect(request).not.toHaveProperty("encryptedApiKey");
    });

    it("opens editor for missing backup service", async () => {
        vi.mocked(service.listGovernanceServices).mockResolvedValue([records[0]]);
        renderPage();
        await screen.findByText("https://api.primary.example");

        fireEvent.click(screen.getByRole("button", { name: /配置/ }));
        expect(await screen.findByDisplayValue("BACKUP")).toBeInTheDocument();
        fireEvent.change(screen.getByLabelText("baseUrl"), {
            target: { value: "https://api.backup.example" }
        });
        fireEvent.click(screen.getByRole("button", { name: /保存/ }));

        await waitFor(() => {
            expect(service.changeServiceConfig).toHaveBeenCalled();
        });
        const request = vi.mocked(service.changeServiceConfig).mock.calls[0]?.[0];
        expect(request).toEqual(
            expect.objectContaining({
                serviceId: null,
                serviceRole: "BACKUP",
                baseUrl: "https://api.backup.example",
                enabled: true,
                status: "UNAVAILABLE"
            })
        );
    });

    it("disables edit actions without edit permission", async () => {
        replacePermissions(["ai:config:view"]);
        renderPage();

        await screen.findByText("PRIMARY 主服务");
        expect(screen.getAllByRole("button", { name: /编辑|配置/ })[0]).toBeDisabled();
    });
});
