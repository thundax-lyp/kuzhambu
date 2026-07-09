import { QueryClientProvider } from "@tanstack/react-query";
import { App } from "antd";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { replacePermissions } from "@/auth/permission-storage";
import { queryClient } from "@/query/query-client";
import { ModelsPage } from "./model-configs-page";
import * as service from "./model-configs-service";

vi.mock("./model-configs-service", () => ({
    changeModelConfig: vi.fn(),
    createModelConfig: vi.fn(),
    deleteModelConfig: vi.fn(),
    listModelCheckRecords: vi.fn(),
    listModelConfigs: vi.fn(),
    listModelServices: vi.fn(),
    refreshModelCheck: vi.fn()
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

const services = [
    {
        serviceId: 1001,
        serviceRole: "PRIMARY" as const,
        apiSource: "OPENAI",
        baseUrl: "https://api.primary.example",
        enabled: true,
        status: "AVAILABLE"
    }
];

const models = [
    {
        modelId: 2001,
        serviceId: 1001,
        modelName: "gpt-4o",
        displayName: "GPT 4o",
        capabilityTags: ["chat", "vision"],
        defaultParamsJson: '{"temperature":0.2}',
        description: "primary model",
        enabled: true,
        registeredAt: "2026-07-01T00:00:00.000Z"
    }
];

const checks = [
    {
        checkId: 3001,
        modelId: 2001,
        serviceId: 1001,
        modelName: "gpt-4o",
        status: "SUCCEEDED",
        latencyMs: 12,
        errorType: null,
        errorMessage: null,
        checkedAt: "2026-07-01T01:00:00.000Z"
    }
];

const renderPage = () =>
    render(
        <App>
            <QueryClientProvider client={queryClient}>
                <ModelsPage />
            </QueryClientProvider>
        </App>
    );

describe("ModelsPage", () => {
    beforeEach(() => {
        queryClient.clear();
        replacePermissions(["ai:config:view", "ai:config:edit"]);
        vi.mocked(service.listModelServices).mockResolvedValue(services);
        vi.mocked(service.listModelConfigs).mockResolvedValue(models);
        vi.mocked(service.listModelCheckRecords).mockResolvedValue(checks);
        vi.mocked(service.refreshModelCheck).mockResolvedValue(checks[0]);
        vi.mocked(service.changeModelConfig).mockResolvedValue(models[0]);
        vi.mocked(service.createModelConfig).mockResolvedValue(models[0]);
        vi.mocked(service.deleteModelConfig).mockResolvedValue(true);
    });

    afterEach(() => {
        cleanup();
        queryClient.clear();
        vi.clearAllMocks();
    });

    it("renders filters and model table", async () => {
        renderPage();

        expect(await screen.findByRole("heading", { name: "AI 模型配置" })).toBeInTheDocument();
        expect(await screen.findByText("GPT 4o")).toBeInTheDocument();
        expect(screen.getByText("gpt-4o")).toBeInTheDocument();
        expect(screen.getByText("chat")).toBeInTheDocument();
        expect(screen.getByText("vision")).toBeInTheDocument();
    });

    it("calls backend model check endpoint from row action", async () => {
        renderPage();
        await screen.findByText("GPT 4o");

        fireEvent.click(screen.getByRole("button", { name: /检测$/ }));

        await waitFor(() => {
            expect(service.refreshModelCheck).toHaveBeenCalledWith(2001, expect.anything());
        });
    });

    it("opens check history drawer", async () => {
        renderPage();
        await screen.findByText("GPT 4o");

        fireEvent.click(screen.getByRole("button", { name: /检测历史/ }));

        expect(await screen.findByRole("heading", { name: "检测历史" })).toBeInTheDocument();
        expect(await screen.findByText("SUCCEEDED")).toBeInTheDocument();
        expect(screen.getByText("12")).toBeInTheDocument();
    });

    it("disables edit actions without edit permission", async () => {
        replacePermissions(["ai:config:view"]);
        renderPage();

        await screen.findByText("GPT 4o");
        expect(screen.getByRole("button", { name: /新增模型/ })).toBeDisabled();
        expect(screen.getByRole("button", { name: /编辑/ })).toBeDisabled();
        expect(screen.getByRole("button", { name: /检测$/ })).toBeDisabled();
    });
});
