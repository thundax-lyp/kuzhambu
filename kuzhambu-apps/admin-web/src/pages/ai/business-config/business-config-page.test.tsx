import { AdminQueryProvider } from "@/query/query-client";
import { App } from "antd";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { replacePermissions } from "@/auth/permission-storage";
import { BusinessConfigPage } from "./business-config-page";
import * as service from "./business-config-service";

vi.mock("./business-config-service", () => ({
    changeBusinessConfig: vi.fn(),
    createBusinessConfig: vi.fn(),
    deleteBusinessConfig: vi.fn(),
    listBusinessConfigCapabilities: vi.fn(),
    listBusinessConfigModels: vi.fn(),
    listBusinessConfigPrompts: vi.fn(),
    listBusinessConfigs: vi.fn()
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
            loading?: boolean;
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
                        key={String(action.title)}
                        type="button"
                        disabled={action.disabled || action.loading}
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

const capabilities = [
    {
        capability: "CLASSICS_SUMMARY",
        name: "古籍摘要",
        requiredTags: ["text"],
        outputMode: "TEXT",
        enabled: true,
        priority: 10
    }
];

const models = [
    {
        id: "900000",
        apiSource: "OPENAI",
        baseUrl: "https://example.test/v1",
        modelName: "dall-e-3",
        displayName: "DALL-E 3",
        capabilities: ["image"],
        defaultParamsJson: "{}",
        description: "image only",
        enabled: true,
        registeredAt: "2026-07-01T00:00:00.000Z"
    },
    {
        id: "900001",
        apiSource: "OPENAI",
        baseUrl: "https://example.test/v1",
        modelName: "gpt-4o",
        displayName: "GPT 4o",
        capabilities: ["text"],
        defaultParamsJson: "{}",
        description: "primary",
        enabled: true,
        registeredAt: "2026-07-01T00:00:00.000Z"
    }
];

const prompts = [
    {
        id: "800001",
        capability: "CLASSICS_SUMMARY",
        name: "古籍摘要提示词",
        enabled: true,
        currentVersionNo: 3,
        registeredAt: "2026-07-01T00:00:00.000Z"
    }
];

const configs = [
    {
        id: "700001",
        capability: "CLASSICS_SUMMARY",
        promptTemplateId: "800001",
        modelId: "900001",
        defaultParamsJson: '{"temperature":0.2}',
        enabled: true,
        configuredAt: "2026-07-01T00:00:00.000Z"
    }
];

const renderPage = () =>
    render(
        <App>
            <AdminQueryProvider>
                <BusinessConfigPage />
            </AdminQueryProvider>
        </App>
    );

describe("BusinessConfigPage", () => {
    beforeEach(() => {
        replacePermissions(["ai:config:view", "ai:config:edit"]);
        vi.mocked(service.listBusinessConfigCapabilities).mockResolvedValue(capabilities);
        vi.mocked(service.listBusinessConfigModels).mockResolvedValue(models);
        vi.mocked(service.listBusinessConfigPrompts).mockResolvedValue(prompts);
        vi.mocked(service.listBusinessConfigs).mockResolvedValue(configs);
        vi.mocked(service.createBusinessConfig).mockResolvedValue(configs[0]);
        vi.mocked(service.changeBusinessConfig).mockResolvedValue(configs[0]);
        vi.mocked(service.deleteBusinessConfig).mockResolvedValue(true);
    });

    afterEach(() => {
        cleanup();
        vi.clearAllMocks();
    });

    it("renders business configs with prompt and model details", async () => {
        renderPage();

        expect(await screen.findByRole("heading", { name: "业务配置" })).toBeInTheDocument();
        expect(await screen.findByText("古籍摘要")).toBeInTheDocument();
        expect(screen.getByText("古籍摘要提示词")).toBeInTheDocument();
        expect(screen.getByText("GPT 4o / gpt-4o")).toBeInTheDocument();
    });

    it("creates a business config from default enabled options", async () => {
        renderPage();
        await screen.findByText("古籍摘要");

        fireEvent.click(screen.getByRole("button", { name: /新增业务配置/ }));
        expect(await screen.findByRole("heading", { name: "新增业务配置" })).toBeInTheDocument();
        fireEvent.click(screen.getByRole("button", { name: /保存/ }));

        await waitFor(() => {
            expect(service.createBusinessConfig).toHaveBeenCalledWith(
                {
                    id: null,
                    capability: "CLASSICS_SUMMARY",
                    promptTemplateId: "800001",
                    modelId: "900001",
                    defaultParamsJson: "{}",
                    enabled: true
                },
                expect.anything()
            );
        });
    });

    it("keeps a create draft when option data refreshes", async () => {
        vi.mocked(service.listBusinessConfigCapabilities)
            .mockResolvedValueOnce(capabilities)
            .mockResolvedValue([
                ...capabilities,
                {
                    capability: "CLASSICS_TRANSLATION",
                    name: "古籍翻译",
                    requiredTags: ["text"],
                    outputMode: "TEXT",
                    enabled: true,
                    priority: 20
                }
            ]);
        renderPage();
        await screen.findByText("古籍摘要");

        fireEvent.click(screen.getByRole("button", { name: /新增业务配置/ }));
        const paramsInput = await screen.findByLabelText("默认参数 JSON");
        fireEvent.change(paramsInput, { target: { value: '{"temperature":0.8}' } });
        fireEvent.click(screen.getByTestId("ai-business-config-refresh-button"));

        await waitFor(() => {
            expect(service.listBusinessConfigCapabilities).toHaveBeenCalledTimes(2);
        });
        expect(paramsInput).toHaveValue('{"temperature":0.8}');
    });

    it("keeps business capability immutable while editing a config", async () => {
        const { container } = renderPage();
        await screen.findByText("古籍摘要");

        fireEvent.click(screen.getByRole("button", { name: /编辑 古籍摘要 业务配置/ }));
        expect(await screen.findByRole("heading", { name: "编辑业务配置" })).toBeInTheDocument();

        expect(container.querySelector(".ant-select-disabled")).toBeInTheDocument();
        fireEvent.click(screen.getByRole("button", { name: /保存/ }));

        await waitFor(() => {
            expect(service.changeBusinessConfig).toHaveBeenCalledWith(
                {
                    id: "700001",
                    capability: "CLASSICS_SUMMARY",
                    promptTemplateId: "800001",
                    modelId: "900001",
                    defaultParamsJson: '{"temperature":0.2}',
                    enabled: true
                },
                expect.anything()
            );
        });
    });
});
