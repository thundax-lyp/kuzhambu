import { AdminQueryProvider } from "@/query/query-client";
import { App } from "antd";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { replacePermissions } from "@/auth/permission-storage";
import { AiModelPage } from "./ai-model-page";
import * as service from "./ai-model-service";

vi.mock("./ai-model-service", () => ({
    changeAiModel: vi.fn(),
    createAiModel: vi.fn(),
    deleteAiModel: vi.fn(),
    listAiModels: vi.fn()
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
        id: "2001",
        apiSource: "OPENAI",
        baseUrl: "https://api.primary.example",
        apiKeyConfigured: true,
        modelName: "gpt-4o",
        displayName: "GPT 4o",
        capabilities: ["TEXT2TEXT", "IMAGE2TEXT"],
        defaultParamsJson: '{"temperature":0.2}',
        description: "primary model",
        enabled: true,
        registeredAt: "2026-07-01T00:00:00.000Z"
    }
];

const renderPage = () =>
    render(
        <App>
            <AdminQueryProvider>
                <AiModelPage />
            </AdminQueryProvider>
        </App>
    );

describe("AiModelPage", () => {
    beforeEach(() => {
        replacePermissions(["ai:config:view", "ai:config:edit"]);
        vi.mocked(service.listAiModels).mockResolvedValue(models);
        vi.mocked(service.changeAiModel).mockResolvedValue(models[0]);
        vi.mocked(service.createAiModel).mockResolvedValue(models[0]);
        vi.mocked(service.deleteAiModel).mockResolvedValue(true);
    });

    afterEach(() => {
        cleanup();
        vi.clearAllMocks();
    });

    it("renders filters and model table", async () => {
        renderPage();

        expect(await screen.findByRole("heading", { name: "模型管理" })).toBeInTheDocument();
        expect(screen.getByText("维护 AI 模型、供应商、能力和调用参数。")).toBeInTheDocument();
        expect(await screen.findByText("GPT 4o")).toBeInTheDocument();
        expect(screen.getByText("gpt-4o")).toBeInTheDocument();
        expect(screen.getByText("OpenAI 兼容（旧）")).toBeInTheDocument();
        expect(screen.getByText("文本生成")).toBeInTheDocument();
        expect(screen.getByText("图像理解")).toBeInTheDocument();
        expect(screen.getByRole("columnheader", { name: "模型名称" })).toBeInTheDocument();
        expect(screen.getByRole("columnheader", { name: "模型标识" })).toBeInTheDocument();
        expect(screen.getByRole("columnheader", { name: "供应商" })).toBeInTheDocument();
        expect(screen.getByRole("columnheader", { name: "能力" })).toBeInTheDocument();
    });

    it("disables edit actions without edit permission", async () => {
        replacePermissions(["ai:config:view"]);
        renderPage();

        await screen.findByText("GPT 4o");
        expect(screen.queryByRole("button", { name: /新增模型/ })).not.toBeInTheDocument();
        expect(screen.getByRole("button", { name: "编辑 GPT 4o" })).toBeDisabled();
    });

    it("updates selected hidden models after search filtering", async () => {
        vi.mocked(service.listAiModels).mockResolvedValue([
            models[0],
            {
                id: "2002",
                apiSource: "BYTEDANCE",
                baseUrl: "https://api.secondary.example",
                apiKeyConfigured: true,
                modelName: "claude-sonnet-4",
                displayName: "Claude Sonnet 4",
                capabilities: ["TEXT2TEXT"],
                defaultParamsJson: "{}",
                description: "secondary model",
                enabled: true,
                registeredAt: "2026-07-02T00:00:00.000Z"
            }
        ]);
        renderPage();

        await screen.findByText("GPT 4o");
        fireEvent.click(screen.getAllByRole("checkbox")[1]);
        fireEvent.change(screen.getByPlaceholderText("搜索模型..."), {
            target: { value: "Claude" }
        });
        expect(screen.queryByText("GPT 4o")).not.toBeInTheDocument();

        fireEvent.click(screen.getByTestId("ai-model-disable-button"));

        await waitFor(() => {
            expect(service.changeAiModel).toHaveBeenCalledWith(
                expect.objectContaining({
                    id: "2001",
                    modelName: "gpt-4o",
                    enabled: false
                }),
                expect.anything()
            );
        });
    });
});
