import { QueryClientProvider } from "@tanstack/react-query";
import { App } from "antd";
import { cleanup, render, screen } from "@testing-library/react";
import { replacePermissions } from "@/auth/permission-storage";
import { queryClient } from "@/query/query-client";
import { ModelsPage } from "./model-configs-page";
import * as service from "./model-configs-service";

vi.mock("./model-configs-service", () => ({
    changeModelConfig: vi.fn(),
    createModelConfig: vi.fn(),
    deleteModelConfig: vi.fn(),
    listModelConfigs: vi.fn()
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

const models = [
    {
        id: 2001,
        apiSource: "OPENAI",
        baseUrl: "https://api.primary.example",
        apiKeyConfigured: true,
        modelName: "gpt-4o",
        displayName: "GPT 4o",
        capabilities: ["TEXT_TO_TEXT", "IMAGE_TO_TEXT"],
        defaultParamsJson: '{"temperature":0.2}',
        description: "primary model",
        enabled: true,
        registeredAt: "2026-07-01T00:00:00.000Z"
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
        vi.mocked(service.listModelConfigs).mockResolvedValue(models);
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
        expect(screen.getByText("TEXT_TO_TEXT")).toBeInTheDocument();
        expect(screen.getByText("IMAGE_TO_TEXT")).toBeInTheDocument();
    });

    it("disables edit actions without edit permission", async () => {
        replacePermissions(["ai:config:view"]);
        renderPage();

        await screen.findByText("GPT 4o");
        expect(screen.getByRole("button", { name: /新增模型/ })).toBeDisabled();
        expect(screen.getByRole("button", { name: /编辑/ })).toBeDisabled();
    });
});
