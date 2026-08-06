import { AdminQueryProvider } from "@/query/query-client";
import { App } from "antd";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { replacePermissions } from "@/auth/permission-storage";
import { AiModelPage } from "./ai-model-page";
import * as service from "./ai-model-service";

vi.mock("./ai-model-service", () => ({
    changeAiModel: vi.fn(),
    changeAiModels: vi.fn(),
    createAiModel: vi.fn(),
    deleteAiModel: vi.fn(),
    deleteAiModels: vi.fn(),
    listAiModels: vi.fn()
}));

vi.mock("@/components/kuzhambu-drawer", () => {
    const mockDrawer = ({
        children,
        footerActions,
        open,
        title
    }: {
        children: React.ReactNode;
        footerActions?: Array<{
            action: () => void;
            disabled?: boolean;
            testId?: string;
            title: React.ReactNode;
        }>;
        open?: boolean;
        title?: React.ReactNode;
    }) =>
        open ? (
            <div>
                <h3>{title}</h3>
                {children}
                {footerActions?.map((action) => (
                    <button
                        key={action.testId}
                        type="button"
                        data-testid={action.testId}
                        disabled={action.disabled}
                        onClick={action.action}
                    >
                        {action.title}
                    </button>
                ))}
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

    it("clears selected models after search filtering", async () => {
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
        expect(screen.getByTestId("ai-model-disable-button")).toBeDisabled();

        fireEvent.click(screen.getByTestId("ai-model-disable-button"));

        expect(service.changeAiModel).not.toHaveBeenCalled();
    });

    it("keeps failed models selected after a partial batch status update", async () => {
        const secondaryModel = {
            id: "2002",
            apiSource: "BYTEDANCE",
            baseUrl: "https://api.secondary.example",
            apiKeyConfigured: true,
            modelName: "secondary-model",
            displayName: "Secondary model",
            capabilities: ["TEXT2TEXT"],
            defaultParamsJson: "{}",
            description: "secondary model",
            enabled: true,
            registeredAt: "2026-07-02T00:00:00.000Z"
        };
        vi.mocked(service.listAiModels).mockResolvedValue([models[0], secondaryModel]);
        vi.mocked(service.changeAiModels).mockResolvedValue([
            { status: "fulfilled", value: models[0] },
            { status: "rejected", reason: new Error("update failed") }
        ]);
        renderPage();

        await screen.findByText("Secondary model");
        fireEvent.click(screen.getAllByRole("checkbox")[1]);
        fireEvent.click(screen.getAllByRole("checkbox")[2]);
        fireEvent.click(screen.getByTestId("ai-model-disable-button"));

        await waitFor(() => {
            expect(service.changeAiModels).toHaveBeenCalledWith(
                expect.objectContaining({
                    commands: expect.arrayContaining([
                        expect.objectContaining({ id: "2001", enabled: false }),
                        expect.objectContaining({ id: "2002", enabled: false })
                    ])
                }),
                expect.anything()
            );
            expect(screen.getByText("已选择 1 项")).toBeInTheDocument();
        });
        expect(await screen.findByText("批量禁用完成：成功 1，失败 1")).toBeInTheDocument();
    });

    it("owns create submission inside the edit drawer", async () => {
        renderPage();

        await screen.findByText("GPT 4o");
        fireEvent.click(screen.getByRole("button", { name: /新增模型/ }));
        fireEvent.change(screen.getByLabelText("AI模型名称"), {
            target: { value: "New model" }
        });
        fireEvent.change(screen.getByLabelText("AI模型服务地址"), {
            target: { value: "https://api.new.example/v1" }
        });
        fireEvent.change(screen.getByLabelText("AI模型标识"), {
            target: { value: "new-model" }
        });
        fireEvent.click(screen.getByTestId("ai-model-save-button"));

        await waitFor(() => {
            expect(service.createAiModel).toHaveBeenCalledWith(
                expect.objectContaining({
                    baseUrl: "https://api.new.example/v1",
                    displayName: "New model",
                    modelName: "new-model"
                }),
                expect.anything()
            );
        });
    });
});
