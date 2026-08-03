import { AdminQueryProvider } from "@/query/query-client";
import { App } from "antd";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { replacePermissions } from "@/auth/permission-storage";
import { CapabilityMappingsPage } from "./capability-mappings-page";
import * as service from "./capability-mappings-service";

vi.mock("./capability-mappings-service", () => ({
    changeCapabilityMapping: vi.fn(),
    listCapabilities: vi.fn(),
    listCapabilityMappings: vi.fn(),
    listEnabledModels: vi.fn()
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
            type?: "default" | "primary";
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
        capability: "summary",
        name: "摘要生成",
        requiredTags: ["chat", "long-context"],
        outputMode: "JSON",
        enabled: true,
        priority: 10
    }
];

const models = [
    {
        modelId: "2001",
        serviceId: "1001",
        modelName: "gpt-4o",
        displayName: "GPT 4o",
        capabilityTags: ["chat", "long-context"],
        defaultParamsJson: "{}",
        description: "primary model",
        enabled: true,
        registeredAt: "2026-07-01T00:00:00.000Z"
    }
];

const mappings = [
    {
        mappingId: "3001",
        scope: "classics",
        capability: "summary",
        modelId: "2001",
        enabled: true,
        configuredAt: "2026-07-01T00:00:00.000Z"
    }
];

const renderPage = () =>
    render(
        <App>
            <AdminQueryProvider>
                <CapabilityMappingsPage />
            </AdminQueryProvider>
        </App>
    );

describe("CapabilityMappingsPage", () => {
    beforeEach(() => {
        replacePermissions(["ai:config:view", "ai:config:edit"]);
        vi.mocked(service.listCapabilities).mockResolvedValue(capabilities);
        vi.mocked(service.listCapabilityMappings).mockResolvedValue(mappings);
        vi.mocked(service.listEnabledModels).mockResolvedValue(models);
        vi.mocked(service.changeCapabilityMapping).mockResolvedValue({ id: "3001" });
    });

    afterEach(() => {
        cleanup();
        vi.clearAllMocks();
    });

    it("renders mapping table with capability and model details", async () => {
        renderPage();

        expect(await screen.findByRole("heading", { name: "AI 能力映射" })).toBeInTheDocument();
        expect(await screen.findByText("摘要生成")).toBeInTheDocument();
        expect(screen.getByText("GPT 4o / gpt-4o")).toBeInTheDocument();
        expect(screen.getByText("long-context")).toBeInTheDocument();
    });

    it("opens drawer and shows model tag match hint", async () => {
        renderPage();
        await screen.findByText("摘要生成");

        fireEvent.click(screen.getByRole("button", { name: /配置模型/ }));

        expect(await screen.findByRole("heading", { name: "配置模型" })).toBeInTheDocument();
        expect(screen.getByText("能力标签匹配")).toBeInTheDocument();
    });

    it("saves mapping changes", async () => {
        renderPage();
        await screen.findByText("摘要生成");

        fireEvent.click(screen.getByRole("button", { name: /配置模型/ }));
        fireEvent.click(await screen.findByRole("button", { name: /保存/ }));

        await waitFor(() => {
            expect(service.changeCapabilityMapping).toHaveBeenCalledWith(
                {
                    mappingId: "3001",
                    scope: "classics",
                    capability: "summary",
                    modelId: "2001",
                    enabled: true
                },
                expect.anything()
            );
        });
    });

    it("sends disabled mapping state from row action", async () => {
        renderPage();
        await screen.findByText("摘要生成");

        fireEvent.click(screen.getByRole("button", { name: /禁\s*用/ }));

        await waitFor(() => {
            expect(service.changeCapabilityMapping).toHaveBeenCalledWith(
                {
                    mappingId: "3001",
                    scope: "classics",
                    capability: "summary",
                    modelId: "2001",
                    enabled: false
                },
                expect.anything()
            );
        });
    });

    it("disables edit actions without edit permission", async () => {
        replacePermissions(["ai:config:view"]);
        renderPage();

        await screen.findByText("摘要生成");
        expect(screen.getByRole("button", { name: /新增映射/ })).toBeDisabled();
        expect(screen.getByRole("button", { name: /配置模型/ })).toBeDisabled();
        expect(screen.getByRole("button", { name: /禁\s*用/ })).toBeDisabled();
    });
});
